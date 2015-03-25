package com.clover.spika.enterprise.chat.caching;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.text.TextUtils;

import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.api.robospice.EmojiSpice;
import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.models.Stickers;
import com.clover.spika.enterprise.chat.models.StickersHolder;
import com.clover.spika.enterprise.chat.models.greendao.StickersDao;
import com.clover.spika.enterprise.chat.models.greendao.StickersDao.Properties;
import com.clover.spika.enterprise.chat.services.robospice.CustomSpiceListener;
import com.clover.spika.enterprise.chat.services.robospice.CustomSpiceRequest;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Utils;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;

public class StickersCaching {

	public static StickersHolder getData(final Activity activity, final SpiceManager spiceManager, final OnStickersDBChanged onDBChangeListener,
			final OnStickersNetworkResult onNetworkListener) {

		StickersHolder resultArray = getDBData(activity);

		EmojiSpice.GetEmoji getEmoji = new EmojiSpice.GetEmoji(activity);
		spiceManager.execute(getEmoji, new CustomSpiceListener<StickersHolder>() {

			@Override
			public void onRequestFailure(SpiceException ex) {
				super.onRequestFailure(ex);
				Utils.onFailedUniversal(null, activity);
			}

			@Override
			public void onRequestSuccess(final StickersHolder result) {
				super.onRequestSuccess(result);

				String message = activity.getResources().getString(R.string.e_something_went_wrong);

				if (result.getCode() == Const.API_SUCCESS) {

					if (onNetworkListener != null) {
						onNetworkListener.onStickersNetworkResult();
					}

					HandleNewData handleNewData = new HandleNewData(activity, result.stickers, onDBChangeListener);
					spiceManager.execute(handleNewData, null);

				} else {

					if (result != null && !TextUtils.isEmpty(result.getMessage())) {
						message = result.getMessage();
					}

					Utils.onFailedUniversal(message, activity);
				}
			}
		});

		return resultArray;
	}

	private static StickersHolder getDBData(Activity activity) {

		List<Stickers> resultArray = new ArrayList<Stickers>();

		if (activity instanceof BaseActivity) {

			StickersDao stickersDao = ((BaseActivity) activity).getDaoSession().getStickersDao();
			List<com.clover.spika.enterprise.chat.models.greendao.Stickers> lista = stickersDao.queryBuilder().orderDesc(Properties.Id).build().list();

			if (lista != null) {

				for (com.clover.spika.enterprise.chat.models.greendao.Stickers sticekrs : lista) {
					resultArray.add(handleOldData(sticekrs));
				}
			}
		}

		StickersHolder holder = new StickersHolder();
		holder.setStickersList(resultArray);
		return holder;
	}

	private static Stickers handleOldData(com.clover.spika.enterprise.chat.models.greendao.Stickers stickers) {

		Stickers finalStickers = new Stickers();

		finalStickers.id = (int) stickers.getId();
		finalStickers.filename = stickers.getFilename();
		finalStickers.is_deleted = stickers.getIs_deleted();
		finalStickers.created = stickers.getCreated();
		finalStickers.url = stickers.getUrl();
		finalStickers.organization_id = stickers.getOrganization_id();
		finalStickers.setUsedTimes(stickers.getUsedTimes());

		return finalStickers;
	}

	private static class HandleNewData extends CustomSpiceRequest<Void> {

		private Activity activity;
		private List<Stickers> stickers;
		private OnStickersDBChanged onDBChangeListener;

		public HandleNewData(Activity activity, List<Stickers> stickers, OnStickersDBChanged onDBChangeListener) {
			super(Void.class);

			this.activity = activity;
			this.stickers = stickers;
			this.onDBChangeListener = onDBChangeListener;
		}

		@Override
		public Void loadDataFromNetwork() throws Exception {
			
			handleNewData(activity, stickers);
			
			activity.runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					if (onDBChangeListener != null) {
						onDBChangeListener.onStickersDBChanged(getDBData(activity));
					}
				}
			});
			
			return null;
		}
	}

	private static void handleNewData(Activity activity, List<Stickers> networkData) {
		
		if (activity instanceof BaseActivity) {

			StickersDao stickersDao = ((BaseActivity) activity).getDaoSession().getStickersDao();
			stickersDao.deleteAll();
			
			for (Stickers sticker : networkData) {

				com.clover.spika.enterprise.chat.models.greendao.Stickers finalStickersModel = new com.clover.spika.enterprise.chat.models.greendao.Stickers(
						Long.valueOf(sticker.id), sticker.filename, sticker.is_deleted, sticker.created, sticker.url, sticker.organization_id, sticker.getUsedTimes());
				
				stickersDao.insert(finalStickersModel);
			}
		}
	}

	public interface OnStickersDBChanged {
		public void onStickersDBChanged(StickersHolder usableData);
	}

	public interface OnStickersNetworkResult {
		public void onStickersNetworkResult();
	}

}

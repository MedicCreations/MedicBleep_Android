package com.medicbleep.app.chat.caching;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.text.TextUtils;

import com.medicbleep.app.chat.R;
import com.medicbleep.app.chat.api.robospice.StickersSpice;
import com.medicbleep.app.chat.extendables.BaseActivity;
import com.medicbleep.app.chat.models.Stickers;
import com.medicbleep.app.chat.models.StickersHolder;
import com.medicbleep.app.chat.models.greendao.StickersDao;
import com.medicbleep.app.chat.models.greendao.StickersDao.Properties;
import com.medicbleep.app.chat.services.robospice.CustomSpiceListener;
import com.medicbleep.app.chat.services.robospice.CustomSpiceRequest;
import com.medicbleep.app.chat.utils.Const;
import com.medicbleep.app.chat.utils.Utils;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;

public class StickersCaching {

	public static StickersHolder getData(final Activity activity, final SpiceManager spiceManager, final OnStickersDBChanged onDBChangeListener,
			final OnStickersNetworkResult onNetworkListener) {

		StickersHolder resultArray = getDBData(activity);

		StickersSpice.GetEmoji getEmoji = new StickersSpice.GetEmoji();
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

					Utils.onFailedUniversal(message, activity, result.getCode(), false);
				}
			}
		});

		return resultArray;
	}

	private static StickersHolder getDBData(Activity activity) {

		List<Stickers> resultArray = new ArrayList<Stickers>();

		if (activity instanceof BaseActivity) {

			StickersDao stickersDao = ((BaseActivity) activity).getDaoSession().getStickersDao();
			List<com.medicbleep.app.chat.models.greendao.Stickers> lista = stickersDao.queryBuilder().orderDesc(Properties.Id).build().list();

			if (lista != null) {

				for (com.medicbleep.app.chat.models.greendao.Stickers sticekrs : lista) {
					resultArray.add(handleOldData(sticekrs));
				}
			}
		}

		StickersHolder holder = new StickersHolder();
		holder.setStickersList(resultArray);
		return holder;
	}

	private static Stickers handleOldData(com.medicbleep.app.chat.models.greendao.Stickers stickers) {

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
			
			final StickersHolder finalResult = getDBData(activity);
			
			activity.runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					if (onDBChangeListener != null) {
						onDBChangeListener.onStickersDBChanged(finalResult);
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

				com.medicbleep.app.chat.models.greendao.Stickers finalStickersModel = new com.medicbleep.app.chat.models.greendao.Stickers(
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

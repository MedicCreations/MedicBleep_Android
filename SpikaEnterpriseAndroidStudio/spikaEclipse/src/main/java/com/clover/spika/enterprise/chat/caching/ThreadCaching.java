package com.clover.spika.enterprise.chat.caching;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.text.TextUtils;

import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.ThreadsActivity;
import com.clover.spika.enterprise.chat.api.robospice.ChatSpice;
import com.clover.spika.enterprise.chat.caching.utils.DaoUtils;
import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.models.Chat;
import com.clover.spika.enterprise.chat.models.Message;
import com.clover.spika.enterprise.chat.models.greendao.MessageDao;
import com.clover.spika.enterprise.chat.models.greendao.MessageDao.Properties;
import com.clover.spika.enterprise.chat.services.robospice.CustomSpiceListener;
import com.clover.spika.enterprise.chat.services.robospice.CustomSpiceRequest;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Utils;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;

public class ThreadCaching {

	public static List<Message> getData(final Activity activity, final SpiceManager spiceManager, final String messageId, final OnThreadDBChanged onDBChangeListener,
			final OnThreadNetworkResult onNetworkListener) {

		List<Message> resultArray = getDBData(activity, Long.valueOf(messageId));

		ChatSpice.GetThreads getThread = new ChatSpice.GetThreads(messageId);
		
		spiceManager.execute(getThread, new CustomSpiceListener<Chat>() {

			@Override
			public void onRequestFailure(SpiceException ex) {
				super.onRequestFailure(ex);
				if(activity instanceof ThreadsActivity){
					Utils.onFailedUniversal(null, activity, 0, false, ex, ((ThreadsActivity)activity).getInternetErrorListener());
				}else{
					Utils.onFailedUniversal(null, activity, 0, false, ex, null);
				}
			}

			@Override
			public void onRequestSuccess(final Chat result) {
				super.onRequestSuccess(result);

				String message = activity.getResources().getString(R.string.e_something_went_wrong);

				if (result.getCode() == Const.API_SUCCESS) {
					
					if (onNetworkListener != null) {
						onNetworkListener.onThreadNetworkResult();
					}

					HandleNewData handleNewData = new HandleNewData(activity, result, messageId, onDBChangeListener);
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
	
	private static List<Message> getDBData(Activity activity, long id) {
		
		List<Message> messages = new ArrayList<Message>();

		if (activity instanceof BaseActivity) {
			
			MessageDao messDao = ((BaseActivity) activity).getDaoSession().getMessageDao();
			List<com.clover.spika.enterprise.chat.models.greendao.Message> listOfDaoMessage = 
					messDao.queryBuilder().where(Properties.Root_id.eq(id)).build().list();
			
			com.clover.spika.enterprise.chat.models.greendao.Message rootMess = messDao.queryBuilder().where(Properties.Id.eq(id)).build().unique();
			
			listOfDaoMessage.add(rootMess);

			messages = DaoUtils.converDaoMessagesToMessagesModel(listOfDaoMessage);

		}
		
		return messages;
	}

	public static class HandleNewData extends CustomSpiceRequest<Void> {

		private Activity activity;
		private Chat chat;
		private Long messageId;
		private OnThreadDBChanged onDBChangeListener;

		public HandleNewData(Activity activity, Chat chat, String messId, OnThreadDBChanged onDBChangeListener) {
			super(Void.class);

			this.activity = activity;
			this.chat = chat;
			this.onDBChangeListener = onDBChangeListener;
			this.messageId = Long.valueOf(messId);
		}

		@Override
		public Void loadDataFromNetwork() throws Exception {
			
			handleNewData(activity, chat);
			
			activity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if (onDBChangeListener != null) {
						onDBChangeListener.onThreadDBChanged(getDBData(activity, messageId));
					}
				}
			});

			return null;
		}
	}

	private static void handleNewData(Activity activity, Chat networkData) {

		if (activity instanceof BaseActivity) {
			
			MessageDao messageDao = ((BaseActivity) activity).getDaoSession().getMessageDao();

			for (Message mess : networkData.messages) {

//				Log.d("LOG", "SAVING TO DATABASE " + mess.getParentId() + ", root: "+mess.getRootId()+", chatid: "+mess.getChat_id()+", id: "+mess.getId());

				com.clover.spika.enterprise.chat.models.greendao.Message finalMessageModel = new com.clover.spika.enterprise.chat.models.greendao.Message(Long.valueOf(mess.id),
						Long.valueOf(mess.chat_id), Long.valueOf(mess.user_id), mess.firstname, mess.lastname, mess.image, mess.text, mess.file_id, mess.thumb_id, mess.longitude,
						mess.latitude, mess.created, mess.modified, mess.child_list, mess.image_thumb, mess.type, mess.root_id, mess.parent_id, mess.isMe, mess.isFailed,
						mess.attributes, mess.country_code, mess.seen_timestamp, Long.valueOf(mess.getChat_id()));

				messageDao.insertOrReplace(finalMessageModel);

			}

		}
	}

	public interface OnThreadDBChanged {
		public void onThreadDBChanged(List<Message> usableData);
	}

	public interface OnThreadNetworkResult {
		public void onThreadNetworkResult();
	}

}

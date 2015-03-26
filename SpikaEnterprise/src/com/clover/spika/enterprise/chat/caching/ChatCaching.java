package com.clover.spika.enterprise.chat.caching;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;

import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.api.robospice.ChatSpice;
import com.clover.spika.enterprise.chat.caching.utils.DaoUtils;
import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.models.Chat;
import com.clover.spika.enterprise.chat.models.Message;
import com.clover.spika.enterprise.chat.models.greendao.CategoryDao;
import com.clover.spika.enterprise.chat.models.greendao.ChatDao;
import com.clover.spika.enterprise.chat.models.greendao.ChatDao.Properties;
import com.clover.spika.enterprise.chat.models.greendao.MessageDao;
import com.clover.spika.enterprise.chat.models.greendao.OrganizationDao;
import com.clover.spika.enterprise.chat.models.greendao.UserDao;
import com.clover.spika.enterprise.chat.services.robospice.CustomSpiceListener;
import com.clover.spika.enterprise.chat.services.robospice.CustomSpiceRequest;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Utils;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;

public class ChatCaching {

	public static Chat getData(final Activity activity, final SpiceManager spiceManager, final boolean isClear, boolean isPagging, boolean isNewMsg, boolean isSend, boolean isRefresh, String chatId,
			String msgId, int adapterCount, final OnChatDBChanged onDBChangeListener, final OnChatNetworkResult onNetworkListener) {

		Chat resultArray = getDBData(activity, Long.valueOf(chatId));

		ChatSpice.GetMessages getMessage = new ChatSpice.GetMessages(isClear, isPagging, isNewMsg, isSend, isRefresh, chatId, msgId, adapterCount, activity);
		spiceManager.execute(getMessage, new CustomSpiceListener<Chat>() {

			@Override
			public void onRequestFailure(SpiceException ex) {
				super.onRequestFailure(ex);
				Utils.onFailedUniversal(null, activity);
			}

			@Override
			public void onRequestSuccess(final Chat result) {
				super.onRequestSuccess(result);

				String message = activity.getResources().getString(R.string.e_something_went_wrong);

				if (result.getCode() == Const.API_SUCCESS) {

					if (onNetworkListener != null) {
						onNetworkListener.onChatNetworkResult(result.total_count);
					}

					HandleNewData handleNewData = new HandleNewData(activity, result, isClear, onDBChangeListener);
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

	private static Chat getDBData(Activity activity, long chatId) {

		Chat chat = new Chat();

		if (activity instanceof BaseActivity) {

			ChatDao chatDao = ((BaseActivity) activity).getDaoSession().getChatDao();
			com.clover.spika.enterprise.chat.models.greendao.Chat chatBase = chatDao.queryBuilder().where(Properties.Chat_id.eq(chatId)).build().unique();
			
			Log.d("LOG", "SIZE OFF2222: " + chatBase.getMessageList().size());

			chat = handleOldData(chatBase);
		}

		return chat;
	}

	private static Chat handleOldData(com.clover.spika.enterprise.chat.models.greendao.Chat chat) {
		return DaoUtils.convertDaoChatToChatModel(chat);
	}

	public static class HandleNewData extends CustomSpiceRequest<Void> {

		private Activity activity;
		private Chat chat;
		private boolean toClear;
		private OnChatDBChanged onDBChangeListener;

		public HandleNewData(Activity activity, Chat chat, boolean toClear, OnChatDBChanged onDBChangeListener) {
			super(Void.class);

			this.activity = activity;
			this.chat = chat;
			this.onDBChangeListener = onDBChangeListener;
			this.toClear = toClear;
		}

		@Override
		public Void loadDataFromNetwork() throws Exception {

			handleNewData(activity, chat);

			activity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if (onDBChangeListener != null) {
						onDBChangeListener.onChatDBChanged(getDBData(activity, (long) chat.chat_id), toClear);
					}
				}
			});

			return null;
		}
	}

	private static void handleNewData(Activity activity, Chat networkData) {

		if (activity instanceof BaseActivity) {
			
			CategoryDao categoryDao = ((BaseActivity) activity).getDaoSession().getCategoryDao();
			OrganizationDao organizationDao = ((BaseActivity) activity).getDaoSession().getOrganizationDao();
			UserDao userDao = ((BaseActivity) activity).getDaoSession().getUserDao();
			MessageDao messageDao = ((BaseActivity) activity).getDaoSession().getMessageDao();
			ChatDao chatDao = ((BaseActivity) activity).getDaoSession().getChatDao();

			Long finalCategoryModelId = 0L;
			if (networkData.category != null) {
				com.clover.spika.enterprise.chat.models.greendao.Category finalCategoryModel = new com.clover.spika.enterprise.chat.models.greendao.Category(Long.valueOf(networkData.category.id),
						networkData.category.name);

				categoryDao.insertOrReplace(finalCategoryModel);
				finalCategoryModelId = finalCategoryModel.getId();
			}

			Long finalUserModelId = 0L;
			if (networkData.user != null) {

				com.clover.spika.enterprise.chat.models.greendao.Organization finalOrganizationModel = null;
				if (networkData.user.organization != null) {

					finalOrganizationModel = new com.clover.spika.enterprise.chat.models.greendao.Organization((Long.valueOf(networkData.user.organization.id)), networkData.user.organization.name);

					organizationDao.insertOrReplace(finalOrganizationModel);
				}

				if (networkData.user.details != null && !networkData.user.details.isEmpty()) {

					// TODO user details needs to implemented in the DB
					// com.clover.spika.enterprise.chat.models.greendao.ListUserDetails
				}

				com.clover.spika.enterprise.chat.models.greendao.User finalUserModel = new com.clover.spika.enterprise.chat.models.greendao.User((long) networkData.user.id,
						(long) networkData.user.user_id, networkData.user.firstname, networkData.user.lastname, networkData.user.type, networkData.user.image, networkData.user.image_thumb,
						networkData.user.is_member, networkData.user.is_admin, networkData.user.name, networkData.user.groupname, networkData.user.chat_id, networkData.user.is_user,
						networkData.user.is_group, networkData.user.is_room);

				userDao.insertOrReplace(finalUserModel);
				finalUserModelId = finalUserModel.getId();
			}

			Long finalMessageModelId = 0L;
			if (networkData.last_message != null) {
				com.clover.spika.enterprise.chat.models.greendao.Message finalMessageModel = new com.clover.spika.enterprise.chat.models.greendao.Message(Long.valueOf(networkData.last_message.id),
						Long.valueOf(networkData.last_message.chat_id), Long.valueOf(networkData.last_message.user_id), networkData.last_message.firstname, networkData.last_message.lastname,
						networkData.last_message.image, networkData.last_message.text, networkData.last_message.file_id, networkData.last_message.thumb_id, networkData.last_message.longitude,
						networkData.last_message.latitude, networkData.last_message.created, networkData.last_message.modified, networkData.last_message.child_list,
						networkData.last_message.image_thumb, networkData.last_message.type, networkData.last_message.root_id, networkData.last_message.parent_id, networkData.last_message.isMe,
						networkData.last_message.isFailed, (long) networkData.chat_id);

				messageDao.insertOrReplace(finalMessageModel);
				finalMessageModelId = finalMessageModel.getId();
			}

			// TODO messages

			for (Message mess : networkData.messages) {

//				Log.d("LOG", "SAVING TO DATABASE");

				com.clover.spika.enterprise.chat.models.greendao.Message finalMessageModel = new com.clover.spika.enterprise.chat.models.greendao.Message(Long.valueOf(mess.id),
						Long.valueOf(mess.chat_id), Long.valueOf(mess.user_id), mess.firstname, mess.lastname, mess.image, mess.text, mess.file_id, mess.thumb_id, mess.longitude, mess.latitude,
						mess.created, mess.modified, mess.child_list, mess.image_thumb, mess.type, mess.root_id, mess.parent_id, mess.isMe, mess.isFailed, (long) networkData.chat_id);

				messageDao.insertOrReplace(finalMessageModel);

			}
			
			if (chatDao.queryBuilder().where(Properties.Chat_id.eq(networkData.chat.chat_id)).count() > 0) {
				Log.d("LOG", "update " + networkData.chat.chat_id);
				com.clover.spika.enterprise.chat.models.greendao.Chat usedChatModel = chatDao.queryBuilder().where(Properties.Chat_id.eq(networkData.chat_id)).unique();
				
				Log.d("LOG", "SIZE OFF22: " + usedChatModel.getMessageList().size());
				
				usedChatModel.setChat_id(Long.valueOf(networkData.chat.chat_id));
				usedChatModel.setId(Long.valueOf(networkData.chat.chat_id));
				usedChatModel.setChat_name(networkData.chat.chat_name);
				usedChatModel.setSeen_by(networkData.chat.seen_by);
				usedChatModel.setTotal_count(networkData.chat.total_count);
				usedChatModel.setImage_thumb(networkData.chat.image_thumb);
				usedChatModel.setImage(networkData.chat.image);
				usedChatModel.setAdmin_id(networkData.chat.image);
				usedChatModel.setIs_active(networkData.chat.is_active);
				usedChatModel.setType(networkData.chat.type);
				usedChatModel.setIs_private(networkData.chat.is_private);
				usedChatModel.setPassword(networkData.chat.password);
				usedChatModel.setUnread(networkData.chat.unread);
				usedChatModel.setIs_member(networkData.chat.is_member);
				usedChatModel.setModified(networkData.chat.modified);
				usedChatModel.setCategoryId(finalCategoryModelId);
				usedChatModel.setUserIdProperty(finalUserModelId);
				usedChatModel.setMessageIdProperty(usedChatModel.getMessageIdProperty());
				
				chatDao.update(usedChatModel);
			} else {
				Log.d("LOG", "insert");
				com.clover.spika.enterprise.chat.models.greendao.Chat finalChatModel = new com.clover.spika.enterprise.chat.models.greendao.Chat(Long.valueOf(networkData.chat_id),
						Long.valueOf(networkData.chat_id), networkData.chat_name, networkData.seen_by, networkData.total_count, networkData.image_thumb, networkData.image, networkData.admin_id,
						networkData.is_active, networkData.type, networkData.is_private, networkData.password, networkData.unread, networkData.is_member, networkData.modified, true, finalCategoryModelId,
						finalUserModelId, finalMessageModelId);
				chatDao.insert(finalChatModel);
			}
		}
	}

	public interface OnChatDBChanged {
		public void onChatDBChanged(Chat usableData, boolean isClear);
	}

	public interface OnChatNetworkResult {
		public void onChatNetworkResult(int totalCount);
	}

}

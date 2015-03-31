package com.clover.spika.enterprise.chat.caching;

import java.util.List;

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

	public static Chat getData(final Activity activity, final SpiceManager spiceManager, final boolean isClear, final boolean isPagging, final boolean isNewMsg,
			final boolean isSend, final boolean isRefresh, String chatId, String msgId, int adapterCount, final OnChatDBChanged onDBChangeListener,
			final OnChatNetworkResult onNetworkListener) {

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

					HandleNewData handleNewData = new HandleNewData(activity, result, isClear, isPagging, isNewMsg, isSend, isRefresh, onDBChangeListener);
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
	
	public static Chat startChat(final Activity activity, final SpiceManager spiceManager, final boolean isClear, final boolean isPagging, final boolean isNewMsg,
			final boolean isSend, final boolean isRefresh, String chatId, String msgId, int adapterCount, final OnChatDBChanged onDBChangeListener,
			final OnChatNetworkResult onNetworkListener, Chat chat) {
		
		Chat resultArray = getDBData(activity, Long.valueOf(chatId));
		
		HandleNewData handleNewData = new HandleNewData(activity, chat, isClear, isPagging, isNewMsg, isSend, isRefresh, onDBChangeListener);
		spiceManager.execute(handleNewData, null);

		return resultArray;
	}

	private static Chat getDBData(Activity activity, long id) {

		Chat chat = new Chat();

		if (activity instanceof BaseActivity) {

			ChatDao chatDao = ((BaseActivity) activity).getDaoSession().getChatDao();
			com.clover.spika.enterprise.chat.models.greendao.Chat chatBase = chatDao.queryBuilder().where(Properties.Id.eq(id)).build().unique();

			if (chatBase == null)
				return null;

			long tempCount = ((BaseActivity) activity).getDaoSession().getMessageDao().queryBuilder()
					.where(com.clover.spika.enterprise.chat.models.greendao.MessageDao.Properties.Chat_id.eq(id),
							com.clover.spika.enterprise.chat.models.greendao.MessageDao.Properties.Root_id.eq(0)).count();
			Log.e("LOG", "TEMP COUNT: " + tempCount + ", messageList size: " + chatBase.getMessageList().size());

			chat = handleOldData(chatBase);

			if (tempCount != chatBase.getMessageList().size()) {
				Log.e("LOG", "RECORRECT MESSAGE LIST");
				List<com.clover.spika.enterprise.chat.models.greendao.Message> tempMess = ((BaseActivity) activity).getDaoSession().getMessageDao().queryBuilder()
						.where(com.clover.spika.enterprise.chat.models.greendao.MessageDao.Properties.Chat_id.eq(id),
								com.clover.spika.enterprise.chat.models.greendao.MessageDao.Properties.Root_id.eq(0)).build().list();
				chat.messages = DaoUtils.converDaoMessagesToMessagesModel(tempMess);
			}

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
		private boolean isPagging;
		private boolean isNewMsg;
		private boolean isSend;
		private boolean isRefresh;
		private OnChatDBChanged onDBChangeListener;

		public HandleNewData(Activity activity, Chat chat, boolean toClear, boolean isPagging, boolean isNewMsg, boolean isSend, boolean isRefresh,
				OnChatDBChanged onDBChangeListener) {
			super(Void.class);

			this.activity = activity;
			this.chat = chat;
			this.onDBChangeListener = onDBChangeListener;
			this.toClear = toClear;
			this.isPagging = isPagging;
			this.isNewMsg = isNewMsg;
			this.isSend = isSend;
			this.isRefresh = isRefresh;
		}

		@Override
		public Void loadDataFromNetwork() throws Exception {
			
			/* if chat.messages == null or size == 0, don't save chat in database*/
			if (chat.messages != null && chat.messages.size() > 0) { 
				Log.d("LOG", "saving chat to database");
				handleNewData(activity, chat);
			} else {
				Log.d("LOG", "dont save chat to database");
			}
			
			final Chat finalResult = getDBData(activity, (long) chat.chat.getId());

			activity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if (onDBChangeListener != null) {
						onDBChangeListener.onChatDBChanged(finalResult, toClear, isPagging, isNewMsg, isSend, isRefresh);
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
				com.clover.spika.enterprise.chat.models.greendao.Category finalCategoryModel = new com.clover.spika.enterprise.chat.models.greendao.Category(
						Long.valueOf(networkData.category.id), networkData.category.name);

				categoryDao.insertOrReplace(finalCategoryModel);
				finalCategoryModelId = finalCategoryModel.getId();
			}

			Long finalUserModelId = 0L;
			if (networkData.user != null) {

				com.clover.spika.enterprise.chat.models.greendao.Organization finalOrganizationModel = null;
				if (networkData.user.organization != null) {

					finalOrganizationModel = new com.clover.spika.enterprise.chat.models.greendao.Organization((Long.valueOf(networkData.user.organization.id)),
							networkData.user.organization.name);

					organizationDao.insertOrReplace(finalOrganizationModel);
				}

				com.clover.spika.enterprise.chat.models.greendao.User finalUserModel = new com.clover.spika.enterprise.chat.models.greendao.User((long) networkData.user.getId(),
						networkData.user.firstname, networkData.user.lastname, networkData.user.type, networkData.user.image, networkData.user.image_thumb,
						networkData.user.is_member, networkData.user.is_admin, networkData.user.name, networkData.user.groupname, networkData.user.chat_id,
						networkData.user.is_user, networkData.user.is_group, networkData.user.is_room, finalOrganizationModel != null ? finalOrganizationModel.getId() : 0L);

				userDao.insertOrReplace(finalUserModel);
				finalUserModelId = finalUserModel.getId();
			}

			Long finalMessageModelId = 0L;
			if (networkData.last_message != null) {
				com.clover.spika.enterprise.chat.models.greendao.Message finalMessageModel = new com.clover.spika.enterprise.chat.models.greendao.Message(
						Long.valueOf(networkData.last_message.id), Long.valueOf(networkData.last_message.chat_id), Long.valueOf(networkData.last_message.user_id),
						networkData.last_message.firstname, networkData.last_message.lastname, networkData.last_message.image, networkData.last_message.text,
						networkData.last_message.file_id, networkData.last_message.thumb_id, networkData.last_message.longitude, networkData.last_message.latitude,
						networkData.last_message.created, networkData.last_message.modified, networkData.last_message.child_list, networkData.last_message.image_thumb,
						networkData.last_message.type, networkData.last_message.root_id, networkData.last_message.parent_id, networkData.last_message.isMe,
						networkData.last_message.isFailed, (long) networkData.chat_id);

				messageDao.insertOrReplace(finalMessageModel);
				finalMessageModelId = finalMessageModel.getId();
			}

			// TODO messages

			for (Message mess : networkData.messages) {

				// Log.d("LOG", "SAVING TO DATABASE");

				com.clover.spika.enterprise.chat.models.greendao.Message finalMessageModel = new com.clover.spika.enterprise.chat.models.greendao.Message(Long.valueOf(mess.id),
						Long.valueOf(mess.chat_id), Long.valueOf(mess.user_id), mess.firstname, mess.lastname, mess.image, mess.text, mess.file_id, mess.thumb_id, mess.longitude,
						mess.latitude, mess.created, mess.modified, mess.child_list, mess.image_thumb, mess.type, mess.root_id, mess.parent_id, mess.isMe, mess.isFailed,
						(long) networkData.chat.getId());

				messageDao.insertOrReplace(finalMessageModel);

			}

			if (chatDao.queryBuilder().where(Properties.Id.eq(networkData.chat.getId())).count() > 0) {
				Log.d("LOG", "update " + networkData.chat.getId());
				com.clover.spika.enterprise.chat.models.greendao.Chat usedChatModel = chatDao.queryBuilder().where(Properties.Id.eq(networkData.chat.getId())).unique();

				usedChatModel.setId(Long.valueOf(networkData.chat.getId()));
				if (networkData.chat.chat_name != null)
					usedChatModel.setChat_name(networkData.chat.chat_name);
				if (networkData.chat.seen_by != null)
					usedChatModel.setSeen_by(networkData.chat.seen_by);
				if ((Integer) networkData.total_count != null)
					usedChatModel.setTotal_count(networkData.total_count);
				if (networkData.chat.image_thumb != null)
					usedChatModel.setImage_thumb(networkData.chat.image_thumb);
				if (networkData.chat.image != null)
					usedChatModel.setImage(networkData.chat.image);
				if (networkData.chat.admin_id != null)
					usedChatModel.setAdmin_id(networkData.chat.admin_id);
				if ((Integer) networkData.chat.is_active != null)
					usedChatModel.setIs_active(networkData.chat.is_active);
				if ((Integer) networkData.chat.type != null)
					usedChatModel.setType(networkData.chat.type);
				if ((Integer) networkData.chat.is_private != null)
					usedChatModel.setIs_private(networkData.chat.is_private);
				if (networkData.chat.password != null)
					usedChatModel.setPassword(networkData.chat.password);
				if (networkData.chat.unread != null)
					usedChatModel.setUnread(networkData.chat.unread);
				if ((Integer) networkData.chat.is_member != null)
					usedChatModel.setIs_member(networkData.chat.is_member);
				if ((Long) networkData.chat.modified != null)
					usedChatModel.setModified(networkData.chat.modified);
				if (finalCategoryModelId != 0) {
					usedChatModel.setCategoryId(finalCategoryModelId);
				}
				if (finalUserModelId != 0) {
					usedChatModel.setUserIdProperty(finalUserModelId);
				}
				if (finalMessageModelId != 0) {
					usedChatModel.setMessageIdProperty(finalMessageModelId);
				}

				chatDao.update(usedChatModel);
			} else {
				Log.d("LOG", "insert");
				com.clover.spika.enterprise.chat.models.greendao.Chat finalChatModel = new com.clover.spika.enterprise.chat.models.greendao.Chat(Long.valueOf(networkData.getId()),
						networkData.chat_name, networkData.seen_by, networkData.total_count, networkData.image_thumb, networkData.image, networkData.admin_id,
						networkData.is_active, networkData.type, networkData.is_private, networkData.password, networkData.unread, networkData.is_member, networkData.modified,
						true, finalCategoryModelId, finalUserModelId, finalMessageModelId);
				chatDao.insert(finalChatModel);
			}
		}
	}

	public interface OnChatDBChanged {
		public void onChatDBChanged(Chat usableData, boolean isClear, boolean isPagging, boolean isNewMsg, boolean isSend, boolean isRefresh);
	}

	public interface OnChatNetworkResult {
		public void onChatNetworkResult(int totalCount);
	}

}

package com.clover.spika.enterprise.chat.caching;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.text.TextUtils;

import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.api.robospice.LobbySpice;
import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.models.Chat;
import com.clover.spika.enterprise.chat.models.LobbyModel;
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

public class RecentFragmentCaching {

	public static List<Chat> getData(final Activity activity, final SpiceManager spiceManager, int page, final int toClear, final OnRecentFragmentDBChanged onDBChangeListener,
			final OnRecentFragmentNetworkResult onNetworkListener) {

		List<Chat> resultArray = getDBData(activity);

		LobbySpice.GetLobbyByType getLobbyByType = new LobbySpice.GetLobbyByType(page, Const.ALL_TOGETHER_TYPE, activity);
		spiceManager.execute(getLobbyByType, new CustomSpiceListener<LobbyModel>() {

			@Override
			public void onRequestFailure(SpiceException ex) {
				super.onRequestFailure(ex);
				Utils.onFailedUniversal(null, activity);
			}

			@Override
			public void onRequestSuccess(final LobbyModel result) {
				super.onRequestSuccess(result);

				String message = activity.getResources().getString(R.string.e_something_went_wrong);

				if (result.getCode() == Const.API_SUCCESS) {

					if (onNetworkListener != null) {
						onNetworkListener.onNetworkResult(result.all_chats.total_count);
					}

					HandleNewData handleNewData = new HandleNewData(activity, result.all_chats.chats, toClear, onDBChangeListener);
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

	private static List<Chat> getDBData(Activity activity) {

		List<Chat> resultArray = new ArrayList<Chat>();

		if (activity instanceof BaseActivity) {

			ChatDao chatDao = ((BaseActivity) activity).getDaoSession().getChatDao();
			List<com.clover.spika.enterprise.chat.models.greendao.Chat> lista = chatDao.queryBuilder().orderDesc(Properties.Modified).build().list();

			if (lista != null) {

				for (com.clover.spika.enterprise.chat.models.greendao.Chat chat : lista) {
					resultArray.add(handleOldData(chat));
				}
			}
		}

		return resultArray;
	}

	private static Chat handleOldData(com.clover.spika.enterprise.chat.models.greendao.Chat chat) {

		Chat finalChat = new Chat();

		finalChat.id = (int) chat.getId();
		finalChat.chat_id = chat.getChat_id().intValue();
		finalChat.chat_name = chat.getChat_name();
		finalChat.seen_by = chat.getSeen_by();
		finalChat.total_count = chat.getTotal_count();
		finalChat.image_thumb = chat.getImage_thumb();
		finalChat.image = chat.getImage();
		finalChat.admin_id = chat.getAdmin_id();
		finalChat.is_active = chat.getIs_active();
		finalChat.type = chat.getType();
		finalChat.is_private = chat.getIs_private();
		finalChat.password = chat.getPassword();
		finalChat.unread = chat.getUnread();
		finalChat.is_member = chat.getIs_member();

		if (chat.getCategory() != null) {

			com.clover.spika.enterprise.chat.models.Category finalCategory = new com.clover.spika.enterprise.chat.models.Category();

			finalCategory.id = (int) chat.getCategory().getId();
			finalCategory.name = chat.getCategory().getName();

			finalChat.category = finalCategory;
		}

		if (chat.getUser() != null) {

			com.clover.spika.enterprise.chat.models.User finalUser = new com.clover.spika.enterprise.chat.models.User();

			finalUser.id = (int) chat.getUser().getId();
			finalUser.user_id = chat.getUser().getUser_id().intValue();
			finalUser.firstname = chat.getUser().getFirstname();
			finalUser.lastname = chat.getUser().getLastname();
			finalUser.type = chat.getUser().getType();
			finalUser.image = chat.getUser().getImage();
			finalUser.image_thumb = chat.getUser().getImage_thumb();
			finalUser.is_member = chat.getUser().getIs_member();
			finalUser.is_admin = chat.getUser().getIs_admin();
			finalUser.name = chat.getUser().getName();
			finalUser.groupname = chat.getUser().getGroupname();
			finalUser.chat_id = chat.getUser().getChat_id();
			finalUser.is_user = chat.getUser().getIs_user();
			finalUser.is_group = chat.getUser().getIs_group();
			finalUser.is_room = chat.getUser().getIs_room();

			finalChat.user = finalUser;
		}

		if (chat.getMessage() != null) {

			com.clover.spika.enterprise.chat.models.Message finalMessage = new com.clover.spika.enterprise.chat.models.Message();

			finalMessage.id = String.valueOf(chat.getMessage().getId());
			finalMessage.chat_id = String.valueOf(chat.getMessage().getChat_id());
			finalMessage.user_id = String.valueOf(chat.getMessage().getUser_id());
			finalMessage.firstname = chat.getMessage().getFirstname();
			finalMessage.lastname = chat.getMessage().getLastname();
			finalMessage.image = chat.getMessage().getImage();
			finalMessage.text = chat.getMessage().getText();
			finalMessage.file_id = chat.getMessage().getFile_id();
			finalMessage.thumb_id = chat.getMessage().getThumb_id();
			finalMessage.longitude = chat.getMessage().getLongitude();
			finalMessage.latitude = chat.getMessage().getLatitude();
			finalMessage.created = chat.getMessage().getCreated();
			finalMessage.modified = chat.getMessage().getModified();
			finalMessage.child_list = chat.getMessage().getChild_list();
			finalMessage.image_thumb = chat.getMessage().getImage_thumb();
			finalMessage.type = chat.getMessage().getType();
			finalMessage.root_id = chat.getMessage().getRoot_id();
			finalMessage.parent_id = chat.getMessage().getParent_id();
			finalMessage.isMe = chat.getMessage().getIsMe();
			finalMessage.isFailed = chat.getMessage().getIsFailed();

			finalChat.last_message = finalMessage;
		}

		return finalChat;
	}

	private static class HandleNewData extends CustomSpiceRequest<Void> {

		private Activity activity;
		private List<Chat> chats;
		private int toClear;
		private OnRecentFragmentDBChanged onDBChangeListener;

		public HandleNewData(Activity activity, List<Chat> chats, int toClear, OnRecentFragmentDBChanged onDBChangeListener) {
			super(Void.class);

			this.activity = activity;
			this.chats = chats;
			this.toClear = toClear;
			this.onDBChangeListener = onDBChangeListener;
		}

		@Override
		public Void loadDataFromNetwork() throws Exception {

			handleNewData(activity, chats, toClear, onDBChangeListener);

			return null;
		}
	}

	private static void handleNewData(Activity activity, List<Chat> networkData, int isClear, OnRecentFragmentDBChanged onDBChangeListener) {

		if (activity instanceof BaseActivity) {

			CategoryDao categoryDao = ((BaseActivity) activity).getDaoSession().getCategoryDao();
			OrganizationDao organizationDao = ((BaseActivity) activity).getDaoSession().getOrganizationDao();
			UserDao userDao = ((BaseActivity) activity).getDaoSession().getUserDao();
			MessageDao messageDao = ((BaseActivity) activity).getDaoSession().getMessageDao();
			ChatDao chatDao = ((BaseActivity) activity).getDaoSession().getChatDao();

			for (Chat chat : networkData) {

				Long finalCategoryModelId = 0L;
				if (chat.category != null) {
					com.clover.spika.enterprise.chat.models.greendao.Category finalCategoryModel = new com.clover.spika.enterprise.chat.models.greendao.Category(
							Long.valueOf(chat.category.id), chat.category.name);

					categoryDao.insertOrReplace(finalCategoryModel);
					finalCategoryModelId = finalCategoryModel.getId();
				}

				Long finalUserModelId = 0L;
				if (chat.user != null) {

					com.clover.spika.enterprise.chat.models.greendao.Organization finalOrganizationModel = null;
					if (chat.user.organization != null) {

						finalOrganizationModel = new com.clover.spika.enterprise.chat.models.greendao.Organization((Long.valueOf(chat.user.organization.id)),
								chat.user.organization.name);

						organizationDao.insertOrReplace(finalOrganizationModel);
					}

					if (chat.user.details != null && !chat.user.details.isEmpty()) {

						// TODO user details needs to implemented in the DB
						// com.clover.spika.enterprise.chat.models.greendao.ListUserDetails
					}

					com.clover.spika.enterprise.chat.models.greendao.User finalUserModel = new com.clover.spika.enterprise.chat.models.greendao.User((long) chat.user.id,
							(long) chat.user.user_id, chat.user.firstname, chat.user.lastname, chat.user.type, chat.user.image, chat.user.image_thumb, chat.user.is_member,
							chat.user.is_admin, chat.user.name, chat.user.groupname, chat.user.chat_id, chat.user.is_user, chat.user.is_group, chat.user.is_room);

					userDao.insertOrReplace(finalUserModel);
					finalUserModelId = finalUserModel.getId();
				}

				Long finalMessageModelId = 0L;
				if (chat.last_message != null) {
					com.clover.spika.enterprise.chat.models.greendao.Message finalMessageModel = new com.clover.spika.enterprise.chat.models.greendao.Message(
							Long.valueOf(chat.last_message.id), Long.valueOf(chat.last_message.chat_id), Long.valueOf(chat.last_message.user_id), chat.last_message.firstname,
							chat.last_message.lastname, chat.last_message.image, chat.last_message.text, chat.last_message.file_id, chat.last_message.thumb_id,
							chat.last_message.longitude, chat.last_message.latitude, chat.last_message.created, chat.last_message.modified, chat.last_message.child_list,
							chat.last_message.image_thumb, chat.last_message.type, chat.last_message.root_id, chat.last_message.parent_id, chat.last_message.isMe,
							chat.last_message.isFailed, (long) chat.chat_id);

					messageDao.insertOrReplace(finalMessageModel);
					finalMessageModelId = finalMessageModel.getId();
				}

				com.clover.spika.enterprise.chat.models.greendao.Chat finalChatModel = new com.clover.spika.enterprise.chat.models.greendao.Chat(Long.valueOf(chat.chat_id),
						Long.valueOf(chat.chat_id), chat.chat_name, chat.seen_by, chat.total_count, chat.image_thumb, chat.image, chat.admin_id, chat.is_active, chat.type,
						chat.is_private, chat.password, chat.unread, chat.is_member, chat.modified, finalCategoryModelId, finalUserModelId, finalMessageModelId);

				chatDao.insertOrReplace(finalChatModel);
			}

			if (onDBChangeListener != null) {
				onDBChangeListener.onDBChanged(getDBData(activity), isClear);
			}
		}
	}

	public interface OnRecentFragmentDBChanged {
		public void onDBChanged(List<Chat> usableData, int isClear);
	}

	public interface OnRecentFragmentNetworkResult {
		public void onNetworkResult(int totalCount);
	}

}

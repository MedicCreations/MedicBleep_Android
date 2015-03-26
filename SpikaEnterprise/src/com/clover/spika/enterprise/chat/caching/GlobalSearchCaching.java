package com.clover.spika.enterprise.chat.caching;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;

import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.api.robospice.GlobalSpice;
import com.clover.spika.enterprise.chat.caching.utils.DaoUtils;
import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.models.Chat;
import com.clover.spika.enterprise.chat.models.GlobalModel;
import com.clover.spika.enterprise.chat.models.GlobalResponse;
import com.clover.spika.enterprise.chat.models.Group;
import com.clover.spika.enterprise.chat.models.User;
import com.clover.spika.enterprise.chat.models.greendao.CategoryDao;
import com.clover.spika.enterprise.chat.models.greendao.ChatDao;
import com.clover.spika.enterprise.chat.models.greendao.ChatDao.Properties;
import com.clover.spika.enterprise.chat.models.greendao.GroupsDao;
import com.clover.spika.enterprise.chat.models.greendao.Groups;
import com.clover.spika.enterprise.chat.models.greendao.MessageDao;
import com.clover.spika.enterprise.chat.models.greendao.OrganizationDao;
import com.clover.spika.enterprise.chat.models.greendao.UserDao;
import com.clover.spika.enterprise.chat.services.robospice.CustomSpiceListener;
import com.clover.spika.enterprise.chat.services.robospice.CustomSpiceRequest;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Utils;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;

public class GlobalSearchCaching {

	public static List<GlobalModel> getData(final Activity activity, final SpiceManager spiceManager, int page, String chatId, String categoryId, final int type,
			String searchTerm, final int toClear, final OnGlobalSearchDBChanged onDBChangeListener, final OnGlobalSearchNetworkResult onNetworkListener) {

		List<GlobalModel> resultArray = getDBData(activity, type);

		GlobalSpice.GlobalSearch globalSearch = new GlobalSpice.GlobalSearch(page, chatId, categoryId, type, searchTerm, activity);
		spiceManager.execute(globalSearch, new CustomSpiceListener<GlobalResponse>() {

			@Override
			public void onRequestFailure(SpiceException arg0) {
				super.onRequestFailure(arg0);
				Utils.onFailedUniversal(null, activity);
			}

			@Override
			public void onRequestSuccess(GlobalResponse result) {
				super.onRequestSuccess(result);

				if (result.getCode() == Const.API_SUCCESS) {

					if (onNetworkListener != null) {
						onNetworkListener.onGlobalSearchNetworkResult(result.getTotalCount());
					}

					HandleNewData handleNewData = new HandleNewData(activity, result.getModelsList(), toClear, type, onDBChangeListener);
					spiceManager.execute(handleNewData, null);

				} else {
					String message = activity.getString(R.string.e_something_went_wrong);
					Utils.onFailedUniversal(message, activity);
				}
			}
		});

		return resultArray;
	}

	private static List<GlobalModel> getDBData(Activity activity, int type) {

		List<GlobalModel> resultArray = new ArrayList<GlobalModel>();

		if (activity instanceof BaseActivity) {

			if (type == GlobalModel.Type.CHAT) {

				ChatDao chatDao = ((BaseActivity) activity).getDaoSession().getChatDao();
				List<com.clover.spika.enterprise.chat.models.greendao.Chat> lista = chatDao.queryBuilder().build().list();

				if (lista != null) {

					for (com.clover.spika.enterprise.chat.models.greendao.Chat chat : lista) {
						resultArray.add(handleOldChatData(chat));
					}
				}

			} else if (type == GlobalModel.Type.GROUP) {

				GroupsDao groupDao = ((BaseActivity) activity).getDaoSession().getGroupsDao();
				List<Groups> lista = groupDao.queryBuilder().build().list();

				if (lista != null) {

					for (com.clover.spika.enterprise.chat.models.greendao.Groups group : lista) {
						resultArray.add(handleOldGroupData(group));
					}
				}

			} else if (type == GlobalModel.Type.USER) {

				UserDao userDao = ((BaseActivity) activity).getDaoSession().getUserDao();
				List<com.clover.spika.enterprise.chat.models.greendao.User> lista = userDao.queryBuilder().build().list();

				if (lista != null) {

					for (com.clover.spika.enterprise.chat.models.greendao.User user : lista) {
						resultArray.add(handleOldUserData(user));
					}
				}

			} else if (type == GlobalModel.Type.ALL) {

				ChatDao chatDao = ((BaseActivity) activity).getDaoSession().getChatDao();
				List<com.clover.spika.enterprise.chat.models.greendao.Chat> chatList = chatDao.queryBuilder().build().list();

				if (chatList != null) {

					for (com.clover.spika.enterprise.chat.models.greendao.Chat chat : chatList) {
						resultArray.add(handleOldChatData(chat));
					}
				}

				GroupsDao groupDao = ((BaseActivity) activity).getDaoSession().getGroupsDao();
				List<com.clover.spika.enterprise.chat.models.greendao.Groups> groupList = groupDao.queryBuilder().build().list();

				if (groupList != null) {

					for (com.clover.spika.enterprise.chat.models.greendao.Groups group : groupList) {
						resultArray.add(handleOldGroupData(group));
					}
				}

				UserDao userDao = ((BaseActivity) activity).getDaoSession().getUserDao();
				List<com.clover.spika.enterprise.chat.models.greendao.User> userList = userDao.queryBuilder().build().list();

				if (userList != null) {

					for (com.clover.spika.enterprise.chat.models.greendao.User user : userList) {
						resultArray.add(handleOldUserData(user));
					}
				}
			}
		}

		return resultArray;
	}

	private static GlobalModel handleOldChatData(com.clover.spika.enterprise.chat.models.greendao.Chat chat) {

		GlobalModel result = new GlobalModel();
		result.type = GlobalModel.Type.CHAT;
		result.chat = DaoUtils.convertDaoChatToChatModel(chat);

		return result;
	}

	private static GlobalModel handleOldGroupData(com.clover.spika.enterprise.chat.models.greendao.Groups group) {

		GlobalModel result = new GlobalModel();
		result.type = GlobalModel.Type.GROUP;
		result.group = DaoUtils.convertDaoGroupToGroupModel(group);

		return result;
	}

	private static GlobalModel handleOldUserData(com.clover.spika.enterprise.chat.models.greendao.User user) {

		GlobalModel result = new GlobalModel();
		result.type = GlobalModel.Type.USER;
		result.user = DaoUtils.convertDaoUserToUserModel(user);

		return result;
	}

	public static class HandleNewData extends CustomSpiceRequest<Void> {

		private Activity activity;
		private List<GlobalModel> globalModel;
		private int toClear;
		private int type;
		private OnGlobalSearchDBChanged onDBChangeListener;

		public HandleNewData(Activity activity, List<GlobalModel> globalModel, int toClear, int type, OnGlobalSearchDBChanged onDBChangeListener) {
			super(Void.class);

			this.activity = activity;
			this.globalModel = globalModel;
			this.toClear = toClear;
			this.type = type;
			this.onDBChangeListener = onDBChangeListener;
		}

		@Override
		public Void loadDataFromNetwork() throws Exception {

			handleNewData(activity, globalModel);

			activity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if (onDBChangeListener != null) {
						onDBChangeListener.onGlobalSearchDBChanged(getDBData(activity, type), toClear);
					}
				}
			});

			return null;
		}
	}

	private static void handleNewData(Activity activity, List<GlobalModel> networkData) {

		if (activity instanceof BaseActivity) {

			UserDao userDao = ((BaseActivity) activity).getDaoSession().getUserDao();
			CategoryDao categoryDao = ((BaseActivity) activity).getDaoSession().getCategoryDao();
			OrganizationDao organizationDao = ((BaseActivity) activity).getDaoSession().getOrganizationDao();
			MessageDao messageDao = ((BaseActivity) activity).getDaoSession().getMessageDao();
			ChatDao chatDao = ((BaseActivity) activity).getDaoSession().getChatDao();
			GroupsDao groupDao = ((BaseActivity) activity).getDaoSession().getGroupsDao();

			for (GlobalModel globalModel : networkData) {

				if (globalModel.type == GlobalModel.Type.USER) {

					User user = globalModel.user;

					com.clover.spika.enterprise.chat.models.greendao.User finalUserModel = new com.clover.spika.enterprise.chat.models.greendao.User((long) user.id,
							(long) user.user_id, user.firstname, user.lastname, user.type, user.image, user.image_thumb, user.is_member, user.is_admin, user.name, user.groupname,
							user.chat_id, user.is_user, user.is_group, user.is_room);

					// TODO
					userDao.insertOrReplace(finalUserModel);

				} else if (globalModel.type == GlobalModel.Type.GROUP) {

					Group group = globalModel.group;

					// TODO
					if (groupDao.queryBuilder().where(com.clover.spika.enterprise.chat.models.greendao.GroupsDao.Properties.Id.eq(group.id)).count() > 0) {

						com.clover.spika.enterprise.chat.models.greendao.Groups groupModel = groupDao.queryBuilder()
								.where(com.clover.spika.enterprise.chat.models.greendao.GroupsDao.Properties.Id.eq(group.id)).unique();

						com.clover.spika.enterprise.chat.models.greendao.Groups finalGroupModel = new com.clover.spika.enterprise.chat.models.greendao.Groups();

						finalGroupModel.setId(groupModel.getId());
						finalGroupModel.setType(group.type);
						finalGroupModel.setGroupname(group.groupname);
						finalGroupModel.setImage(group.image);
						finalGroupModel.setImage_thumb(group.image_thumb);
						finalGroupModel.setIs_member(group.is_member);

						groupDao.update(finalGroupModel);

					} else {

						com.clover.spika.enterprise.chat.models.greendao.Groups finalGroupModel = new com.clover.spika.enterprise.chat.models.greendao.Groups(group.id, group.type,
								group.groupname, group.image, group.image_thumb, group.is_member);

						groupDao.insert(finalGroupModel);
					}

				} else if (globalModel.type == GlobalModel.Type.CHAT) {

					Chat chat = globalModel.chat;

					Long finalCategoryModelId = 0L;
					if (chat.category != null) {
						com.clover.spika.enterprise.chat.models.greendao.Category finalCategoryModel = new com.clover.spika.enterprise.chat.models.greendao.Category(
								Long.valueOf(chat.category.id), chat.category.name);

						// TODO
						categoryDao.insertOrReplace(finalCategoryModel);
						finalCategoryModelId = finalCategoryModel.getId();
					}

					Long finalUserModelId = 0L;
					if (chat.user != null) {

						com.clover.spika.enterprise.chat.models.greendao.Organization finalOrganizationModel = null;
						if (chat.user.organization != null) {

							finalOrganizationModel = new com.clover.spika.enterprise.chat.models.greendao.Organization((Long.valueOf(chat.user.organization.id)),
									chat.user.organization.name);

							// TODO
							organizationDao.insertOrReplace(finalOrganizationModel);
						}

						com.clover.spika.enterprise.chat.models.greendao.User finalUserModel = new com.clover.spika.enterprise.chat.models.greendao.User((long) chat.user.id,
								(long) chat.user.user_id, chat.user.firstname, chat.user.lastname, chat.user.type, chat.user.image, chat.user.image_thumb, chat.user.is_member,
								chat.user.is_admin, chat.user.name, chat.user.groupname, chat.user.chat_id, chat.user.is_user, chat.user.is_group, chat.user.is_room);

						// TODO
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

						// TODO
						messageDao.insertOrReplace(finalMessageModel);
						finalMessageModelId = finalMessageModel.getId();
					}

					// TODO
					if (chatDao.queryBuilder().where(Properties.Chat_id.eq(chat.chat_id)).count() > 0) {

						com.clover.spika.enterprise.chat.models.greendao.Chat usedChatModel = chatDao.queryBuilder().where(Properties.Chat_id.eq(chat.chat_id)).unique();

						usedChatModel.setChat_id(Long.valueOf(chat.chat_id));
						usedChatModel.setId(Long.valueOf(chat.chat_id));
						usedChatModel.setChat_name(chat.chat_name);
						usedChatModel.setSeen_by(chat.seen_by);
						usedChatModel.setTotal_count(chat.total_count);
						usedChatModel.setImage_thumb(chat.image_thumb);
						usedChatModel.setImage(chat.image);
						usedChatModel.setAdmin_id(chat.image);
						usedChatModel.setIs_active(chat.is_active);
						usedChatModel.setType(chat.type);
						usedChatModel.setIs_private(chat.is_private);
						usedChatModel.setPassword(chat.password);
						usedChatModel.setUnread(chat.unread);
						usedChatModel.setIs_member(chat.is_member);
						usedChatModel.setModified(chat.modified);
						usedChatModel.setCategoryId(finalCategoryModelId);
						usedChatModel.setUserIdProperty(finalUserModelId);
						usedChatModel.setMessageIdProperty(finalMessageModelId);

						chatDao.update(usedChatModel);
					} else {

						com.clover.spika.enterprise.chat.models.greendao.Chat finalChatModel = new com.clover.spika.enterprise.chat.models.greendao.Chat(
								Long.valueOf(chat.chat_id), Long.valueOf(chat.chat_id), chat.chat_name, chat.seen_by, chat.total_count, chat.image_thumb, chat.image,
								chat.admin_id, chat.is_active, chat.type, chat.is_private, chat.password, chat.unread, chat.is_member, chat.modified, false, finalCategoryModelId,
								finalUserModelId, finalMessageModelId);

						chatDao.insert(finalChatModel);
					}
				}
			}
		}
	}

	public interface OnGlobalSearchDBChanged {
		public void onGlobalSearchDBChanged(List<GlobalModel> usableData, int isClear);
	}

	public interface OnGlobalSearchNetworkResult {
		public void onGlobalSearchNetworkResult(int totalCount);
	}

}

package com.clover.spika.enterprise.chat.caching;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;

import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.api.robospice.GlobalSpice;
import com.clover.spika.enterprise.chat.caching.utils.DaoUtils;
import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.models.Chat;
import com.clover.spika.enterprise.chat.models.GlobalModel;
import com.clover.spika.enterprise.chat.models.GlobalModel.Type;
import com.clover.spika.enterprise.chat.models.GlobalResponse;
import com.clover.spika.enterprise.chat.models.Group;
import com.clover.spika.enterprise.chat.models.User;
import com.clover.spika.enterprise.chat.models.greendao.CategoryDao;
import com.clover.spika.enterprise.chat.models.greendao.ChatDao;
import com.clover.spika.enterprise.chat.models.greendao.ChatMembers;
import com.clover.spika.enterprise.chat.models.greendao.ChatMembersDao;
import com.clover.spika.enterprise.chat.models.greendao.GroupsDao;
import com.clover.spika.enterprise.chat.models.greendao.MessageDao;
import com.clover.spika.enterprise.chat.models.greendao.OrganizationDao;
import com.clover.spika.enterprise.chat.models.greendao.UserDao;
import com.clover.spika.enterprise.chat.models.greendao.ChatDao.Properties;
import com.clover.spika.enterprise.chat.services.robospice.CustomSpiceListener;
import com.clover.spika.enterprise.chat.services.robospice.CustomSpiceRequest;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Utils;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;

import de.greenrobot.dao.query.QueryBuilder;

public class ChatMembersCaching {

	/* start: Caching calls */
	public static List<GlobalModel> GetChatMembers(final Activity activity, final SpiceManager spiceManager, final String chatId,
			final OnChatMembersDBChanged onDBChangeListener) {

		List<GlobalModel> resultArray = getDBData(activity, chatId);

		GlobalSpice.GlobalMembers globalMembers = new GlobalSpice.GlobalMembers(-1, chatId, null, Type.ALL, activity);
		spiceManager.execute(globalMembers, new CustomSpiceListener<GlobalResponse>() {

			@Override
			public void onRequestFailure(SpiceException arg0) {
				super.onRequestFailure(arg0);
				Utils.onFailedUniversal(null, activity);
			}

			@Override
			public void onRequestSuccess(GlobalResponse result) {
				super.onRequestSuccess(result);

				if (result.getCode() == Const.API_SUCCESS) {

					HandleNewSearchData handleNewData = new HandleNewSearchData(activity, result.getModelsList(), chatId, onDBChangeListener);
					spiceManager.execute(handleNewData, null);

				} else {
					String message = activity.getString(R.string.e_something_went_wrong);
					Utils.onFailedUniversal(message, activity, result.getCode(), false);
				}
			}
		});

		return resultArray;
	}

	/* end: Caching calls */

	/* start: Interface callbacks */
	public interface OnChatMembersDBChanged {
		public void onChatMembersDBChanged(List<GlobalModel> usableData);
	}

	/* end: Interface callbacks */

	/* start: HandleNewData */
	public static class HandleNewSearchData extends CustomSpiceRequest<Void> {

		private Activity activity;
		private List<GlobalModel> globalModel;
		private String chatId;
		private OnChatMembersDBChanged onDBChangeListener;

		public HandleNewSearchData(Activity activity, List<GlobalModel> globalModel, String chatId, OnChatMembersDBChanged onDBChangeListener) {
			super(Void.class);

			this.activity = activity;
			this.globalModel = globalModel;
			this.chatId = chatId;
			this.onDBChangeListener = onDBChangeListener;
		}

		@Override
		public Void loadDataFromNetwork() throws Exception {

			handleNewData(activity, globalModel, chatId);

			final List<GlobalModel> finalResult = getDBData(activity, chatId);

			activity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if (onDBChangeListener != null) {
						onDBChangeListener.onChatMembersDBChanged(finalResult);
					}
				}
			});

			return null;
		}
	}

	/* end: HandleNewData */

	/* start: Data handling */

	private static List<GlobalModel> getDBData(Activity activity, String chatId) {

		List<GlobalModel> resultArray = new ArrayList<GlobalModel>();

		if (activity instanceof BaseActivity) {

			ChatMembersDao chatMembersDao = ((BaseActivity) activity).getDaoSession().getChatMembersDao();

			ChatMembers chatMembers = chatMembersDao.queryBuilder()
					.where(com.clover.spika.enterprise.chat.models.greendao.ChatMembersDao.Properties.Id.eq(Integer.valueOf(chatId))).build()
					.unique();

			if (chatMembers != null) {

				try {

					JSONObject members = new JSONObject(chatMembers.getChatMembers());
					JSONArray users = members.getJSONArray("users");
					JSONArray groups = members.getJSONArray("groups");
					JSONArray chats = members.getJSONArray("chats");

					UserDao userDao = ((BaseActivity) activity).getDaoSession().getUserDao();

					for (int i = 0; i < users.length(); i++) {

						String value = users.getString(i);

						com.clover.spika.enterprise.chat.models.greendao.User user = userDao.queryBuilder()
								.where(com.clover.spika.enterprise.chat.models.greendao.UserDao.Properties.Id.eq(Integer.valueOf(value))).unique();

						if (user != null) {

							GlobalModel item = handleOldUserData(user);
							resultArray.add(item);
						}
					}

					GroupsDao groupsDao = ((BaseActivity) activity).getDaoSession().getGroupsDao();

					for (int i = 0; i < groups.length(); i++) {

						String value = groups.getString(i);

						com.clover.spika.enterprise.chat.models.greendao.Groups group = groupsDao.queryBuilder()
								.where(com.clover.spika.enterprise.chat.models.greendao.GroupsDao.Properties.Id.eq(Integer.valueOf(value))).unique();

						if (group != null) {

							GlobalModel item = handleOldGroupData(group);
							resultArray.add(item);
						}
					}

					ChatDao chatDao = ((BaseActivity) activity).getDaoSession().getChatDao();

					for (int i = 0; i < chats.length(); i++) {

						String value = chats.getString(i);

						QueryBuilder<com.clover.spika.enterprise.chat.models.greendao.Chat> qb = chatDao.queryBuilder();

						qb.whereOr(Properties.Type.eq(GlobalModel.Type.CHAT), Properties.Type.eq(GlobalModel.Type.GROUP));
						qb.where(Properties.Id.eq(Integer.valueOf(value)));

						com.clover.spika.enterprise.chat.models.greendao.Chat chat = qb.build().unique();

						if (chat != null) {

							GlobalModel item = handleOldChatData(chat);
							resultArray.add(item);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		return resultArray;
	}

	private static void handleNewData(Activity activity, List<GlobalModel> networkData, String chatId) {

		if (activity instanceof BaseActivity) {

			UserDao userDao = ((BaseActivity) activity).getDaoSession().getUserDao();
			CategoryDao categoryDao = ((BaseActivity) activity).getDaoSession().getCategoryDao();
			OrganizationDao organizationDao = ((BaseActivity) activity).getDaoSession().getOrganizationDao();
			MessageDao messageDao = ((BaseActivity) activity).getDaoSession().getMessageDao();
			ChatDao chatDao = ((BaseActivity) activity).getDaoSession().getChatDao();
			GroupsDao groupDao = ((BaseActivity) activity).getDaoSession().getGroupsDao();

			List<String> userIds = new ArrayList<String>();
			List<String> groupIds = new ArrayList<String>();
			List<String> chatIds = new ArrayList<String>();

			for (GlobalModel globalModel : networkData) {

				if (globalModel.type == GlobalModel.Type.USER) {

					User user = globalModel.user;

					if (userDao.queryBuilder().where(com.clover.spika.enterprise.chat.models.greendao.UserDao.Properties.Id.eq(user.getId())).count() > 0) {

						com.clover.spika.enterprise.chat.models.greendao.User finalUserModel = userDao.queryBuilder()
								.where(com.clover.spika.enterprise.chat.models.greendao.UserDao.Properties.Id.eq(user.getId())).unique();
						finalUserModel = DaoUtils.convertUserModelToUserDao(finalUserModel, user);

						userDao.update(finalUserModel);

					} else {

						com.clover.spika.enterprise.chat.models.greendao.User finalUserModel = DaoUtils.convertUserModelToUserDao(null, user);

						userDao.insert(finalUserModel);
					}

					userIds.add(String.valueOf(user.getId()));

				} else if (globalModel.type == GlobalModel.Type.GROUP) {

					Group group = globalModel.group;

					if (groupDao.queryBuilder().where(com.clover.spika.enterprise.chat.models.greendao.GroupsDao.Properties.Id.eq(group.id)).count() > 0) {

						com.clover.spika.enterprise.chat.models.greendao.Groups finalGroupModel = groupDao.queryBuilder()
								.where(com.clover.spika.enterprise.chat.models.greendao.GroupsDao.Properties.Id.eq(group.id)).unique();
						finalGroupModel = DaoUtils.convertGroupModelToGroupDao(finalGroupModel, group);

						groupDao.update(finalGroupModel);

					} else {

						com.clover.spika.enterprise.chat.models.greendao.Groups finalGroupModel = DaoUtils.convertGroupModelToGroupDao(null, group);

						groupDao.insert(finalGroupModel);
					}

					groupIds.add(String.valueOf(group.getId()));

				} else if (globalModel.type == GlobalModel.Type.CHAT) {

					Chat chat = globalModel.chat;

					Long finalCategoryModelId = 0L;
					if (chat.category != null) {

						if (categoryDao.queryBuilder()
								.where(com.clover.spika.enterprise.chat.models.greendao.CategoryDao.Properties.Id.eq(chat.category.id)).count() > 0) {

							com.clover.spika.enterprise.chat.models.greendao.Category finalCategoryModel = categoryDao.queryBuilder()
									.where(com.clover.spika.enterprise.chat.models.greendao.CategoryDao.Properties.Id.eq(chat.category.id)).unique();

							categoryDao.update(finalCategoryModel);
							finalCategoryModelId = finalCategoryModel.getId();

						} else {

							com.clover.spika.enterprise.chat.models.greendao.Category finalCategoryModel = DaoUtils
									.convertCategoryModelToCategoryDao(null, chat.category);

							categoryDao.insert(finalCategoryModel);
							finalCategoryModelId = finalCategoryModel.getId();
						}
					}

					Long finalUserModelId = 0L;
					if (chat.user != null) {

						if (chat.user.organization != null) {

							if (organizationDao
									.queryBuilder()
									.where(com.clover.spika.enterprise.chat.models.greendao.OrganizationDao.Properties.Id
											.eq(chat.user.organization.id)).count() > 0) {

								com.clover.spika.enterprise.chat.models.greendao.Organization finalOrganizationModel = organizationDao
										.queryBuilder()
										.where(com.clover.spika.enterprise.chat.models.greendao.OrganizationDao.Properties.Id
												.eq(chat.user.organization.id)).unique();
								finalOrganizationModel = DaoUtils.convertOrganizationModelToOrganizationDao(finalOrganizationModel,
										chat.user.organization);

								organizationDao.update(finalOrganizationModel);

							} else {
								com.clover.spika.enterprise.chat.models.greendao.Organization finalOrganizationModel = DaoUtils
										.convertOrganizationModelToOrganizationDao(null, chat.user.organization);
								organizationDao.insert(finalOrganizationModel);
							}
						}

						if (userDao.queryBuilder()
								.where(com.clover.spika.enterprise.chat.models.greendao.UserDao.Properties.Id.eq(chat.user.organization.id)).count() > 0) {

							com.clover.spika.enterprise.chat.models.greendao.User finalUserModel = userDao.queryBuilder()
									.where(com.clover.spika.enterprise.chat.models.greendao.UserDao.Properties.Id.eq(chat.user.organization.id))
									.unique();
							finalUserModel = DaoUtils.convertUserModelToUserDao(finalUserModel, chat.user);

							userDao.update(finalUserModel);
							finalUserModelId = finalUserModel.getId();

						} else {

							com.clover.spika.enterprise.chat.models.greendao.User finalUserModel = DaoUtils
									.convertUserModelToUserDao(null, chat.user);

							userDao.insert(finalUserModel);
							finalUserModelId = finalUserModel.getId();
						}
					}

					Long finalMessageModelId = 0L;
					if (chat.last_message != null) {

						if (messageDao.queryBuilder()
								.where(com.clover.spika.enterprise.chat.models.greendao.MessageDao.Properties.Id.eq(chat.last_message.id)).count() > 0) {

							com.clover.spika.enterprise.chat.models.greendao.Message finalMessageModel = messageDao.queryBuilder()
									.where(com.clover.spika.enterprise.chat.models.greendao.MessageDao.Properties.Id.eq(chat.last_message.id))
									.unique();
							finalMessageModel = DaoUtils.convertMessageModelToMessageDao(finalMessageModel, chat.last_message, chat.chat_id);

							messageDao.update(finalMessageModel);
							finalMessageModelId = finalMessageModel.getId();

						} else {
							com.clover.spika.enterprise.chat.models.greendao.Message finalMessageModel = DaoUtils.convertMessageModelToMessageDao(
									null, chat.last_message, chat.chat_id);

							messageDao.insert(finalMessageModel);
							finalMessageModelId = finalMessageModel.getId();
						}
					}

					if (chatDao.queryBuilder().where(Properties.Id.eq(chat.getId())).count() > 0) {

						com.clover.spika.enterprise.chat.models.greendao.Chat usedChatModel = chatDao.queryBuilder()
								.where(Properties.Id.eq(chat.getId())).unique();
						usedChatModel = DaoUtils.convertChatModelToChatDao(usedChatModel, chat, finalCategoryModelId, finalUserModelId,
								finalMessageModelId, usedChatModel.getIsRecent());

						chatDao.update(usedChatModel);

					} else {

						com.clover.spika.enterprise.chat.models.greendao.Chat finalChatModel = DaoUtils.convertChatModelToChatDao(null, chat,
								finalCategoryModelId, finalUserModelId, finalMessageModelId, false);

						chatDao.insert(finalChatModel);
					}

					chatIds.add(String.valueOf(chat.getId()));
				}
			}

			try {

				JSONArray usersArray = new JSONArray();
				for (String string : userIds) {
					usersArray.put(string);
				}

				JSONArray groupsArray = new JSONArray();
				for (String string : groupIds) {
					groupsArray.put(string);
				}

				JSONArray chatsArray = new JSONArray();
				for (String string : chatIds) {
					chatsArray.put(string);
				}

				JSONObject chatMembers = new JSONObject();

				chatMembers.put("users", usersArray);
				chatMembers.put("groups", groupsArray);
				chatMembers.put("chats", chatsArray);

				ChatMembers members = new ChatMembers(Long.valueOf(chatId), chatMembers.toString());

				ChatMembersDao chatMembersDao = ((BaseActivity) activity).getDaoSession().getChatMembersDao();
				chatMembersDao.insertOrReplace(members);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
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

	/* end: Data handling */

}

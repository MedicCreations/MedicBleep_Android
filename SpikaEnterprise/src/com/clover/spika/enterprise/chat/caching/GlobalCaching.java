package com.clover.spika.enterprise.chat.caching;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.text.TextUtils;

import com.clover.spika.enterprise.chat.MainActivity;
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
import com.clover.spika.enterprise.chat.models.greendao.MessageDao;
import com.clover.spika.enterprise.chat.models.greendao.OrganizationDao;
import com.clover.spika.enterprise.chat.models.greendao.UserDao;
import com.clover.spika.enterprise.chat.services.robospice.CustomSpiceListener;
import com.clover.spika.enterprise.chat.services.robospice.CustomSpiceRequest;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Helper;
import com.clover.spika.enterprise.chat.utils.Utils;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;

public class GlobalCaching {

	/* start: Caching calls */
	public static List<GlobalModel> GlobalSearch(final Activity activity, final SpiceManager spiceManager, int page, String chatId,
			String categoryId, final int type, final String searchTerm, final boolean toClear, final OnGlobalSearchDBChanged onDBChangeListener,
			final OnGlobalSearchNetworkResult onNetworkListener) {

		final String myId = Helper.getUserId(activity);

		List<GlobalModel> resultArray = getDBData(activity, type, Integer.valueOf(myId), searchTerm);

		GlobalSpice.GlobalSearch globalSearch = new GlobalSpice.GlobalSearch(page, chatId, categoryId, type, searchTerm, activity);
		spiceManager.execute(globalSearch, new CustomSpiceListener<GlobalResponse>() {

			@Override
			public void onRequestFailure(SpiceException arg0) {
				super.onRequestFailure(arg0);
				if(activity instanceof MainActivity){
					Utils.onFailedUniversal(null, activity, 0 , false, arg0, ((MainActivity)activity).getInternetErrorListener());
				}else{
					Utils.onFailedUniversal(null, activity, 0 , false);
				}
			}

			@Override
			public void onRequestSuccess(GlobalResponse result) {
				super.onRequestSuccess(result);

				if (result.getCode() == Const.API_SUCCESS) {

					if (onNetworkListener != null) {
						onNetworkListener.onGlobalSearchNetworkResult(result.getTotalCount());
					}

					HandleNewSearchData handleNewData = new HandleNewSearchData(activity, result.getModelsList(), toClear, type, Integer
							.valueOf(myId), searchTerm, onDBChangeListener);
					spiceManager.execute(handleNewData, null);

				} else {
					String message = activity.getString(R.string.e_something_went_wrong);
					Utils.onFailedUniversal(message, activity);
				}
			}
		});

		return resultArray;
	}

	public static List<GlobalModel> GlobalMembers(final Activity activity, final SpiceManager spiceManager, int page, String chatId, String groupId,
			final int type, final boolean isToClear, final OnGlobalMemberDBChanged onDBChangeListener,
			final OnGlobalMemberNetworkResult onNetworkListener) {

		final String myId = Helper.getUserId(activity);

		List<GlobalModel> resultArray = getDBData(activity, type, Integer.valueOf(myId), null);

		GlobalSpice.GlobalMembers globalMembers = new GlobalSpice.GlobalMembers(page, chatId, groupId, type, activity);
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

					if (onNetworkListener != null) {
						onNetworkListener.onGlobalMemberNetworkResult(result.getTotalCount());
					}

					HandleNewMemberData handleNewData = new HandleNewMemberData(activity, result.getModelsList(), isToClear, type, Integer
							.valueOf(myId), onDBChangeListener);
					spiceManager.execute(handleNewData, null);

				} else {
					String message = activity.getString(R.string.e_something_went_wrong);
					Utils.onFailedUniversal(message, activity);
				}
			}
		});

		return resultArray;
	}

	/* end: Caching calls */

	/* start: Interface callbacks */
	public interface OnGlobalSearchDBChanged {
		public void onGlobalSearchDBChanged(List<GlobalModel> usableData, boolean isClear);
	}

	public interface OnGlobalSearchNetworkResult {
		public void onGlobalSearchNetworkResult(int totalCount);
	}

	public interface OnGlobalMemberDBChanged {
		public void onGlobalMemberDBChanged(List<GlobalModel> usableData, boolean isClear);
	}

	public interface OnGlobalMemberNetworkResult {
		public void onGlobalMemberNetworkResult(int totalCount);
	}

	/* end: Interface callbacks */

	/* start: HandleNewData */
	public static class HandleNewSearchData extends CustomSpiceRequest<Void> {

		private Activity activity;
		private List<GlobalModel> globalModel;
		private boolean toClear;
		private int type;
		private int myId = 0;
		private String search = null;
		private OnGlobalSearchDBChanged onDBChangeListener;

		public HandleNewSearchData(Activity activity, List<GlobalModel> globalModel, boolean toClear, int type, int myId, String search,
				OnGlobalSearchDBChanged onDBChangeListener) {
			super(Void.class);

			this.activity = activity;
			this.globalModel = globalModel;
			this.toClear = toClear;
			this.type = type;
			this.myId = myId;
			this.search = search;
			this.onDBChangeListener = onDBChangeListener;
		}

		@Override
		public Void loadDataFromNetwork() throws Exception {

			handleNewData(activity, globalModel);

			final List<GlobalModel> finalResult = getDBData(activity, type, myId, search);

			activity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if (onDBChangeListener != null) {
						onDBChangeListener.onGlobalSearchDBChanged(finalResult, toClear);
					}
				}
			});

			return null;
		}
	}

	public static class HandleNewMemberData extends CustomSpiceRequest<Void> {

		private Activity activity;
		private List<GlobalModel> globalModel;
		private boolean toClear;
		private int type;
		private int myId = 0;
		private OnGlobalMemberDBChanged onDBChangeListener;

		public HandleNewMemberData(Activity activity, List<GlobalModel> globalModel, boolean toClear, int type, int myId,
				OnGlobalMemberDBChanged onDBChangeListener) {
			super(Void.class);

			this.activity = activity;
			this.globalModel = globalModel;
			this.toClear = toClear;
			this.type = type;
			this.myId = myId;
			this.onDBChangeListener = onDBChangeListener;
		}

		@Override
		public Void loadDataFromNetwork() throws Exception {

			handleNewData(activity, globalModel);

			final List<GlobalModel> finalResult = getDBData(activity, type, myId, null);

			activity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if (onDBChangeListener != null) {
						onDBChangeListener.onGlobalMemberDBChanged(finalResult, toClear);
					}
				}
			});

			return null;
		}
	}

	/* end: HandleNewData */

	/* start: Data handling */

	private static List<GlobalModel> getDBData(Activity activity, int type, int myUserId, String search) {
		
		List<GlobalModel> resultArray = new ArrayList<GlobalModel>();

		if (activity instanceof BaseActivity) {

			if (type == GlobalModel.Type.CHAT) {

				ChatDao chatDao = ((BaseActivity) activity).getDaoSession().getChatDao();
				
				List<com.clover.spika.enterprise.chat.models.greendao.Chat> lista;
				
				if(TextUtils.isEmpty(search)){
					lista = chatDao.queryBuilder()
							.whereOr(Properties.Type.eq(GlobalModel.Type.CHAT), Properties.Type.eq(GlobalModel.Type.GROUP)).build().list();
				}else{
					lista = chatDao.queryBuilder()
							.whereOr(Properties.Type.eq(GlobalModel.Type.CHAT), Properties.Type.eq(GlobalModel.Type.GROUP))
							.where(Properties.Chat_name.like("%" + search + "%")).build().list();
				}

				if (lista != null) {

					for (com.clover.spika.enterprise.chat.models.greendao.Chat chat : lista) {

						GlobalModel item = handleOldChatData(chat);

						if (item.chat != null) {
							resultArray.add(item);
						}
					}
				}

			} else if (type == GlobalModel.Type.GROUP) {

				GroupsDao groupDao = ((BaseActivity) activity).getDaoSession().getGroupsDao();
				List<com.clover.spika.enterprise.chat.models.greendao.Groups> groupList = groupDao.queryBuilder().build().list();

				if (groupList != null) {

					for (com.clover.spika.enterprise.chat.models.greendao.Groups group : groupList) {

						GlobalModel item = handleOldGroupData(group);

						if (item.group != null) {
							resultArray.add(item);
						}
					}
				}

			} else if (type == GlobalModel.Type.USER) {

				UserDao userDao = ((BaseActivity) activity).getDaoSession().getUserDao();
				
				List<com.clover.spika.enterprise.chat.models.greendao.User> lista;

				if(TextUtils.isEmpty(search)){
					lista = userDao.queryBuilder()
							.where(com.clover.spika.enterprise.chat.models.greendao.UserDao.Properties.Id.notEq(myUserId)).build().list();
				}else{
					lista = userDao.queryBuilder()
							.where(com.clover.spika.enterprise.chat.models.greendao.UserDao.Properties.Id.notEq(myUserId))
							.whereOr(com.clover.spika.enterprise.chat.models.greendao.UserDao.Properties.Firstname.like("%" + search +"%"), 
									com.clover.spika.enterprise.chat.models.greendao.UserDao.Properties.Lastname.like("%" + search +"%")).build().list();
				}

				if (lista != null) {

					for (com.clover.spika.enterprise.chat.models.greendao.User user : lista) {

						GlobalModel item = handleOldUserData(user);

						if (item.user != null) {
							resultArray.add(item);
						}
					}
				}

			} else if (type == GlobalModel.Type.ALL) {

				ChatDao chatDao = ((BaseActivity) activity).getDaoSession().getChatDao();
				List<com.clover.spika.enterprise.chat.models.greendao.Chat> chatList = chatDao.queryBuilder().build().list();

				if (chatList != null) {

					for (com.clover.spika.enterprise.chat.models.greendao.Chat chat : chatList) {

						GlobalModel item = handleOldChatData(chat);

						if (item.chat != null) {
							resultArray.add(item);
						}
					}
				}

				GroupsDao groupDao = ((BaseActivity) activity).getDaoSession().getGroupsDao();
				List<com.clover.spika.enterprise.chat.models.greendao.Groups> groupList = groupDao.queryBuilder().build().list();

				if (groupList != null) {

					for (com.clover.spika.enterprise.chat.models.greendao.Groups group : groupList) {

						GlobalModel item = handleOldGroupData(group);

						if (item.group != null) {
							resultArray.add(item);
						}
					}
				}

				UserDao userDao = ((BaseActivity) activity).getDaoSession().getUserDao();
				List<com.clover.spika.enterprise.chat.models.greendao.User> userList = userDao.queryBuilder().build().list();

				if (userList != null) {

					for (com.clover.spika.enterprise.chat.models.greendao.User user : userList) {

						GlobalModel item = handleOldUserData(user);

						if (item.user != null) {
							resultArray.add(item);
						}
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

					if (userDao.queryBuilder().where(com.clover.spika.enterprise.chat.models.greendao.UserDao.Properties.Id.eq(user.getId())).count() > 0) {

						com.clover.spika.enterprise.chat.models.greendao.User finalUserModel = userDao.queryBuilder()
								.where(com.clover.spika.enterprise.chat.models.greendao.UserDao.Properties.Id.eq(user.getId())).unique();
						finalUserModel = DaoUtils.convertUserModelToUserDao(finalUserModel, user);

						userDao.update(finalUserModel);

					} else {

						com.clover.spika.enterprise.chat.models.greendao.User finalUserModel = DaoUtils.convertUserModelToUserDao(null, user);

						userDao.insert(finalUserModel);
					}

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
				}
			}
		}
	}

	/* end: Data handling */

}

package com.clover.spika.enterprise.chat.caching;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.text.TextUtils;

import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.api.robospice.LobbySpice;
import com.clover.spika.enterprise.chat.caching.utils.DaoUtils;
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

public class LobbyCaching {

	/* start: Caching calls */
	public static List<Chat> getData(final Activity activity, final SpiceManager spiceManager, int page, final boolean toClear, final OnLobbyDBChanged onDBChangeListener,
			final OnLobbyNetworkResult onNetworkListener) {

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
						onNetworkListener.onRecentNetworkResult(result.all_chats.total_count);
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

	/* end: Caching calls */

	/* start: Interface callbacks */

	public interface OnLobbyDBChanged {
		public void onRecentDBChanged(List<Chat> usableData, boolean isClear);
	}

	public interface OnLobbyNetworkResult {
		public void onRecentNetworkResult(int totalCount);
	}

	/* end: Interface callbacks */

	/* start: HandleNewData */

	public static class HandleNewData extends CustomSpiceRequest<Void> {

		private Activity activity;
		private List<Chat> chats;
		private boolean toClear;
		private OnLobbyDBChanged onDBChangeListener;

		public HandleNewData(Activity activity, List<Chat> chats, boolean toClear, OnLobbyDBChanged onDBChangeListener) {
			super(Void.class);

			this.activity = activity;
			this.chats = chats;
			this.toClear = toClear;
			this.onDBChangeListener = onDBChangeListener;
		}

		@Override
		public Void loadDataFromNetwork() throws Exception {

			handleNewData(activity, chats);
			
			final List<Chat> finalResult = getDBData(activity);

			activity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if (onDBChangeListener != null) {
						onDBChangeListener.onRecentDBChanged(finalResult, toClear);
					}
				}
			});

			return null;
		}
	}

	/* end: HandleNewData */

	/* start: Data handling */

	private static List<Chat> getDBData(Activity activity) {

		List<Chat> resultArray = new ArrayList<Chat>();

		if (activity instanceof BaseActivity) {

			ChatDao chatDao = ((BaseActivity) activity).getDaoSession().getChatDao();
			List<com.clover.spika.enterprise.chat.models.greendao.Chat> lista = chatDao.queryBuilder().where(Properties.IsRecent.eq(true)).orderDesc(Properties.Modified).build()
					.list();

			if (lista != null) {

				for (com.clover.spika.enterprise.chat.models.greendao.Chat chat : lista) {

					Chat item = handleOldData(chat);

					if (item != null) {
						resultArray.add(item);
					}
				}
			}
		}

		return resultArray;
	}

	private static Chat handleOldData(com.clover.spika.enterprise.chat.models.greendao.Chat chat) {
		return DaoUtils.convertDaoChatToChatModel(chat);
	}

	private static void handleNewData(Activity activity, List<Chat> networkData) {

		if (activity instanceof BaseActivity) {

			CategoryDao categoryDao = ((BaseActivity) activity).getDaoSession().getCategoryDao();
			OrganizationDao organizationDao = ((BaseActivity) activity).getDaoSession().getOrganizationDao();
			UserDao userDao = ((BaseActivity) activity).getDaoSession().getUserDao();
			MessageDao messageDao = ((BaseActivity) activity).getDaoSession().getMessageDao();
			ChatDao chatDao = ((BaseActivity) activity).getDaoSession().getChatDao();

			for (Chat chat : networkData) {

				Long finalCategoryModelId = 0L;
				if (chat.category != null) {

					if (categoryDao.queryBuilder().where(com.clover.spika.enterprise.chat.models.greendao.CategoryDao.Properties.Id.eq(chat.category.id)).count() > 0) {

						com.clover.spika.enterprise.chat.models.greendao.Category finalCategoryModel = categoryDao.queryBuilder()
								.where(com.clover.spika.enterprise.chat.models.greendao.CategoryDao.Properties.Id.eq(chat.category.id)).unique();

						categoryDao.update(finalCategoryModel);
						finalCategoryModelId = finalCategoryModel.getId();

					} else {

						com.clover.spika.enterprise.chat.models.greendao.Category finalCategoryModel = DaoUtils.convertCategoryModelToCategoryDao(null, chat.category);

						categoryDao.insert(finalCategoryModel);
						finalCategoryModelId = finalCategoryModel.getId();
					}
				}

				Long finalUserModelId = 0L;
				if (chat.user != null) {

					if (chat.user.organization != null) {

						if (organizationDao.queryBuilder().where(com.clover.spika.enterprise.chat.models.greendao.OrganizationDao.Properties.Id.eq(chat.user.organization.id))
								.count() > 0) {

							com.clover.spika.enterprise.chat.models.greendao.Organization finalOrganizationModel = organizationDao.queryBuilder()
									.where(com.clover.spika.enterprise.chat.models.greendao.OrganizationDao.Properties.Id.eq(chat.user.organization.id)).unique();
							finalOrganizationModel = DaoUtils.convertOrganizationModelToOrganizationDao(finalOrganizationModel, chat.user.organization);

							organizationDao.update(finalOrganizationModel);

						} else {
							com.clover.spika.enterprise.chat.models.greendao.Organization finalOrganizationModel = DaoUtils.convertOrganizationModelToOrganizationDao(null,
									chat.user.organization);
							organizationDao.insert(finalOrganizationModel);
						}
					}

					if (userDao.queryBuilder().where(com.clover.spika.enterprise.chat.models.greendao.UserDao.Properties.Id.eq(chat.user.organization.id)).count() > 0) {

						com.clover.spika.enterprise.chat.models.greendao.User finalUserModel = userDao.queryBuilder()
								.where(com.clover.spika.enterprise.chat.models.greendao.UserDao.Properties.Id.eq(chat.user.organization.id)).unique();
						finalUserModel = DaoUtils.convertUserModelToUserDao(finalUserModel, chat.user);

						userDao.update(finalUserModel);
						finalUserModelId = finalUserModel.getId();

					} else {

						com.clover.spika.enterprise.chat.models.greendao.User finalUserModel = DaoUtils.convertUserModelToUserDao(null, chat.user);

						userDao.insert(finalUserModel);
						finalUserModelId = finalUserModel.getId();
					}
				}

				Long finalMessageModelId = 0L;
				if (chat.last_message != null) {

					if (messageDao.queryBuilder().where(com.clover.spika.enterprise.chat.models.greendao.MessageDao.Properties.Id.eq(chat.last_message.id)).count() > 0) {

						com.clover.spika.enterprise.chat.models.greendao.Message finalMessageModel = messageDao.queryBuilder()
								.where(com.clover.spika.enterprise.chat.models.greendao.MessageDao.Properties.Id.eq(chat.last_message.id)).unique();
						finalMessageModel = DaoUtils.convertMessageModelToMessageDao(finalMessageModel, chat.last_message, chat.chat_id);

						messageDao.update(finalMessageModel);
						finalMessageModelId = finalMessageModel.getId();

					} else {
						com.clover.spika.enterprise.chat.models.greendao.Message finalMessageModel = DaoUtils
								.convertMessageModelToMessageDao(null, chat.last_message, chat.chat_id);

						messageDao.insert(finalMessageModel);
						finalMessageModelId = finalMessageModel.getId();
					}
				}

				if (chatDao.queryBuilder().where(Properties.Id.eq(chat.getId())).count() > 0) {

					com.clover.spika.enterprise.chat.models.greendao.Chat usedChatModel = chatDao.queryBuilder().where(Properties.Id.eq(chat.getId())).unique();
					usedChatModel = DaoUtils.convertChatModelToChatDao(usedChatModel, chat, finalCategoryModelId, finalUserModelId, finalMessageModelId, true);

					chatDao.update(usedChatModel);

				} else {

					com.clover.spika.enterprise.chat.models.greendao.Chat finalChatModel = DaoUtils.convertChatModelToChatDao(null, chat, finalCategoryModelId, finalUserModelId,
							finalMessageModelId, true);

					chatDao.insert(finalChatModel);
				}
			}
		}
	}
	/* end: Data handling */

}

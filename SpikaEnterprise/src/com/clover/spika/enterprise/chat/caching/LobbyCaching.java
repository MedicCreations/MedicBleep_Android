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

	public static List<Chat> getData(final Activity activity, final SpiceManager spiceManager, int page, final int toClear, final OnLobbyDBChanged onDBChangeListener,
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

	private static List<Chat> getDBData(Activity activity) {

		List<Chat> resultArray = new ArrayList<Chat>();

		if (activity instanceof BaseActivity) {

			ChatDao chatDao = ((BaseActivity) activity).getDaoSession().getChatDao();
			List<com.clover.spika.enterprise.chat.models.greendao.Chat> lista = chatDao.queryBuilder().where(Properties.IsRecent.eq(true)).orderDesc(Properties.Modified).build()
					.list();

			if (lista != null) {

				for (com.clover.spika.enterprise.chat.models.greendao.Chat chat : lista) {
					resultArray.add(handleOldData(chat));
				}
			}
		}

		return resultArray;
	}

	private static Chat handleOldData(com.clover.spika.enterprise.chat.models.greendao.Chat chat) {
		return DaoUtils.convertDaoChatToChatModel(chat);
	}

	public static class HandleNewData extends CustomSpiceRequest<Void> {

		private Activity activity;
		private List<Chat> chats;
		private int toClear;
		private OnLobbyDBChanged onDBChangeListener;

		public HandleNewData(Activity activity, List<Chat> chats, int toClear, OnLobbyDBChanged onDBChangeListener) {
			super(Void.class);

			this.activity = activity;
			this.chats = chats;
			this.toClear = toClear;
			this.onDBChangeListener = onDBChangeListener;
		}

		@Override
		public Void loadDataFromNetwork() throws Exception {

			handleNewData(activity, chats);

			activity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if (onDBChangeListener != null) {
						onDBChangeListener.onRecentDBChanged(getDBData(activity), toClear);
					}
				}
			});

			return null;
		}
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

						if (organizationDao.queryBuilder().where(com.clover.spika.enterprise.chat.models.greendao.OrganizationDao.Properties.Id.eq(chat.user.organization.id))
								.count() > 0) {

							finalOrganizationModel = organizationDao.queryBuilder()
									.where(com.clover.spika.enterprise.chat.models.greendao.OrganizationDao.Properties.Id.eq(chat.user.organization.id)).unique();

							if (chat.user.organization.name != null) {
								finalOrganizationModel.setName(chat.user.organization.name);
							}

							organizationDao.update(finalOrganizationModel);

						} else {
							finalOrganizationModel = new com.clover.spika.enterprise.chat.models.greendao.Organization((Long.valueOf(chat.user.organization.id)),
									chat.user.organization.name);
							organizationDao.insert(finalOrganizationModel);
						}
					}

					if (userDao.queryBuilder().where(com.clover.spika.enterprise.chat.models.greendao.UserDao.Properties.Id.eq(chat.user.organization.id)).count() > 0) {

						com.clover.spika.enterprise.chat.models.greendao.User finalUserModel = userDao.queryBuilder()
								.where(com.clover.spika.enterprise.chat.models.greendao.UserDao.Properties.Id.eq(chat.user.organization.id)).unique();
						
						
						finalUserModel.setId((long) chat.user.id);
						
						if(chat.user.user_id != 0){
							
						}
//						finalUserModel
//						
//						(long) chat.user.user_id,
//						chat.user.firstname,
//						chat.user.lastname,
//						chat.user.type,
//						chat.user.image,
//						chat.user.image_thumb,
//						chat.user.is_member,
//						chat.user.is_admin,
//						chat.user.name,
//						chat.user.groupname,
//						chat.user.chat_id,
//						chat.user.is_user,
//						chat.user.is_group,
//						chat.user.is_room

						userDao.update(finalUserModel);
						finalUserModelId = finalUserModel.getId();
						
					} else {
						
						com.clover.spika.enterprise.chat.models.greendao.User finalUserModel = new com.clover.spika.enterprise.chat.models.greendao.User((long) chat.user.id,
								(long) chat.user.user_id, chat.user.firstname, chat.user.lastname, chat.user.type, chat.user.image, chat.user.image_thumb, chat.user.is_member,
								chat.user.is_admin, chat.user.name, chat.user.groupname, chat.user.chat_id, chat.user.is_user, chat.user.is_group, chat.user.is_room);

						userDao.insert(finalUserModel);
						finalUserModelId = finalUserModel.getId();
					}
				}

				Long finalMessageModelId = 0L;
				if (chat.last_message != null) {

					if (messageDao.queryBuilder().where(com.clover.spika.enterprise.chat.models.greendao.MessageDao.Properties.Id.eq(chat.last_message.id)).count() > 0) {

						com.clover.spika.enterprise.chat.models.greendao.Message finalMessageModel = messageDao.queryBuilder()
								.where(com.clover.spika.enterprise.chat.models.greendao.MessageDao.Properties.Id.eq(chat.last_message.id)).unique();

						finalMessageModel.setId(Long.valueOf(chat.last_message.id));

						if (chat.last_message.chat_id != null && Long.valueOf(chat.last_message.chat_id) != 0L) {
							finalMessageModel.setChat_id(Long.valueOf(chat.last_message.chat_id));
						}

						if (chat.last_message.user_id != null && Long.valueOf(chat.last_message.user_id) != 0L) {
							finalMessageModel.setUser_id(Long.valueOf(chat.last_message.user_id));
						}

						if (chat.last_message.firstname != null) {
							finalMessageModel.setFirstname(chat.last_message.firstname);
						}

						if (chat.last_message.lastname != null) {
							finalMessageModel.setLastname(chat.last_message.lastname);
						}

						if (chat.last_message.text != null) {
							finalMessageModel.setText(chat.last_message.text);
						}

						if (chat.last_message.file_id != null) {
							finalMessageModel.setFile_id(chat.last_message.file_id);
						}

						if (chat.last_message.thumb_id != null) {
							finalMessageModel.setThumb_id(chat.last_message.thumb_id);
						}

						if (chat.last_message.longitude != null) {
							finalMessageModel.setLongitude(chat.last_message.longitude);
						}

						if (chat.last_message.latitude != null) {
							finalMessageModel.setLongitude(chat.last_message.latitude);
						}

						if (chat.last_message.created != null) {
							finalMessageModel.setCreated(chat.last_message.created);
						}

						if (chat.last_message.modified != null) {
							finalMessageModel.setModified(chat.last_message.modified);
						}

						if (chat.last_message.child_list != null) {
							finalMessageModel.setChild_list(chat.last_message.child_list);
						}

						if (chat.last_message.image_thumb != null) {
							finalMessageModel.setImage_thumb(chat.last_message.image_thumb);
						}

						if (chat.last_message.image_thumb != null) {
							finalMessageModel.setImage_thumb(chat.last_message.image_thumb);
						}

						if (chat.last_message.type != 0) {
							finalMessageModel.setType(chat.last_message.type);
						}

						if (chat.last_message.root_id != 0) {
							finalMessageModel.setRoot_id(chat.last_message.root_id);
						}

						if (chat.last_message.parent_id != 0) {
							finalMessageModel.setParent_id(chat.last_message.parent_id);
						}

						finalMessageModel.setIsMe(chat.last_message.isMe);
						finalMessageModel.setIsFailed(chat.last_message.isFailed);

						if (chat.chat_id != 0) {
							finalMessageModel.setParent_id(chat.chat_id);
						}

					} else {
						com.clover.spika.enterprise.chat.models.greendao.Message finalMessageModel = new com.clover.spika.enterprise.chat.models.greendao.Message(
								Long.valueOf(chat.last_message.id), Long.valueOf(chat.last_message.chat_id), Long.valueOf(chat.last_message.user_id), chat.last_message.firstname,
								chat.last_message.lastname, chat.last_message.image, chat.last_message.text, chat.last_message.file_id, chat.last_message.thumb_id,
								chat.last_message.longitude, chat.last_message.latitude, chat.last_message.created, chat.last_message.modified, chat.last_message.child_list,
								chat.last_message.image_thumb, chat.last_message.type, chat.last_message.root_id, chat.last_message.parent_id, chat.last_message.isMe,
								chat.last_message.isFailed, (long) chat.chat_id);

						messageDao.insertOrReplace(finalMessageModel);
						finalMessageModelId = finalMessageModel.getId();
					}
				}

				if (chatDao.queryBuilder().where(Properties.Chat_id.eq(chat.chat_id)).count() > 0) {

					com.clover.spika.enterprise.chat.models.greendao.Chat usedChatModel = chatDao.queryBuilder().where(Properties.Chat_id.eq(chat.chat_id)).unique();

					usedChatModel.setChat_id(Long.valueOf(chat.chat_id));
					usedChatModel.setId(Long.valueOf(chat.id));

					if (chat.chat_name != null) {
						usedChatModel.setChat_name(chat.chat_name);
					}

					if (chat.seen_by != null) {
						usedChatModel.setSeen_by(chat.seen_by);
					}

					if ((Integer) chat.total_count != null || chat.total_count != 0) {
						usedChatModel.setTotal_count(chat.total_count);
					}

					if (chat.image_thumb != null) {
						usedChatModel.setImage_thumb(chat.image_thumb);
					}

					if (chat.image != null) {
						usedChatModel.setImage(chat.image);
					}

					if (chat.admin_id != null) {
						usedChatModel.setAdmin_id(chat.admin_id);
					}

					usedChatModel.setIs_active(chat.is_active);
					usedChatModel.setType(chat.type);
					usedChatModel.setIs_private(chat.is_private);

					if (chat.password != null) {
						usedChatModel.setPassword(chat.password);
					}

					if (chat.unread != null) {
						usedChatModel.setUnread(chat.unread);
					}

					usedChatModel.setIs_member(chat.is_member);

					if (chat.modified != 0L) {
						usedChatModel.setModified(chat.modified);
					}

					if (finalCategoryModelId != 0L) {
						usedChatModel.setCategoryId(finalCategoryModelId);
					}

					if (finalUserModelId != 0L) {
						usedChatModel.setUserIdProperty(finalUserModelId);
					}

					if (finalMessageModelId != 0L) {
						usedChatModel.setMessageIdProperty(finalMessageModelId);
					}

					usedChatModel.setIsRecent(true);

					chatDao.update(usedChatModel);
				} else {
					com.clover.spika.enterprise.chat.models.greendao.Chat finalChatModel = new com.clover.spika.enterprise.chat.models.greendao.Chat(Long.valueOf(chat.chat_id),
							Long.valueOf(chat.chat_id), chat.chat_name, chat.seen_by, chat.total_count, chat.image_thumb, chat.image, chat.admin_id, chat.is_active, chat.type,
							chat.is_private, chat.password, chat.unread, chat.is_member, chat.modified, true, finalCategoryModelId, finalUserModelId, finalMessageModelId);
					chatDao.insert(finalChatModel);
				}
			}
		}
	}

	public interface OnLobbyDBChanged {
		public void onRecentDBChanged(List<Chat> usableData, int isClear);
	}

	public interface OnLobbyNetworkResult {
		public void onRecentNetworkResult(int totalCount);
	}

}

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
			String searchTerm, final boolean toClear, final OnGlobalSearchDBChanged onDBChangeListener, final OnGlobalSearchNetworkResult onNetworkListener) {

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

						GlobalModel item = handleOldChatData(chat);

						if (item.chat != null) {
							resultArray.add(item);
						}
					}
				}

			} else if (type == GlobalModel.Type.GROUP) {

				GroupsDao groupDao = ((BaseActivity) activity).getDaoSession().getGroupsDao();
				List<Groups> lista = groupDao.queryBuilder().build().list();

				if (lista != null) {

					for (com.clover.spika.enterprise.chat.models.greendao.Groups group : lista) {

						GlobalModel item = handleOldGroupData(group);

						if (item.group != null) {
							resultArray.add(item);
						}
					}
				}

			} else if (type == GlobalModel.Type.USER) {

				UserDao userDao = ((BaseActivity) activity).getDaoSession().getUserDao();
				List<com.clover.spika.enterprise.chat.models.greendao.User> lista = userDao.queryBuilder().build().list();

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

	public static class HandleNewData extends CustomSpiceRequest<Void> {

		private Activity activity;
		private List<GlobalModel> globalModel;
		private boolean toClear;
		private int type;
		private OnGlobalSearchDBChanged onDBChangeListener;

		public HandleNewData(Activity activity, List<GlobalModel> globalModel, boolean toClear, int type, OnGlobalSearchDBChanged onDBChangeListener) {
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

					if (userDao.queryBuilder().where(com.clover.spika.enterprise.chat.models.greendao.UserDao.Properties.Id.eq(user.getId())).count() > 0) {

						com.clover.spika.enterprise.chat.models.greendao.User finalUserModel = userDao.queryBuilder()
								.where(com.clover.spika.enterprise.chat.models.greendao.UserDao.Properties.Id.eq(user.getId())).unique();

						finalUserModel.setId((long) user.getId());

						if (user.user_id != 0) {
							finalUserModel.setId((long) user.user_id);
						}

						if (user.firstname != null) {
							finalUserModel.setFirstname(user.firstname);
						}

						if (user.lastname != null) {
							finalUserModel.setLastname(user.lastname);
						}

						if (user.type != 0) {
							finalUserModel.setType(user.type);
						}

						if (user.image != null) {
							finalUserModel.setImage(user.image);
						}

						if (user.image_thumb != null) {
							finalUserModel.setImage_thumb(user.image_thumb);
						}

						finalUserModel.setIs_member(user.is_member);
						finalUserModel.setIs_admin(user.is_admin);

						if (user.name != null) {
							finalUserModel.setName(user.name);
						}

						if (user.groupname != null) {
							finalUserModel.setGroupname(user.groupname);
						}

						if (user.chat_id != null) {
							finalUserModel.setChat_id(user.chat_id);
						}

						finalUserModel.setIs_user(user.is_user);
						finalUserModel.setIs_group(user.is_group);
						finalUserModel.setIs_room(user.is_room);

						userDao.update(finalUserModel);

					} else {

						com.clover.spika.enterprise.chat.models.greendao.User finalUserModel = new com.clover.spika.enterprise.chat.models.greendao.User((long) user.getId(),
								user.firstname, user.lastname, user.type, user.image, user.image_thumb, user.is_member, user.is_admin, user.name, user.groupname, user.chat_id,
								user.is_user, user.is_group, user.is_room);

						userDao.insert(finalUserModel);
					}

				} else if (globalModel.type == GlobalModel.Type.GROUP) {

					Group group = globalModel.group;

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

						if (categoryDao.queryBuilder().where(com.clover.spika.enterprise.chat.models.greendao.CategoryDao.Properties.Id.eq(chat.category.id)).count() > 0) {

							com.clover.spika.enterprise.chat.models.greendao.Category finalCategoryModel = categoryDao.queryBuilder()
									.where(com.clover.spika.enterprise.chat.models.greendao.CategoryDao.Properties.Id.eq(chat.category.id)).unique();

							categoryDao.update(finalCategoryModel);
							finalCategoryModelId = finalCategoryModel.getId();

						} else {

							com.clover.spika.enterprise.chat.models.greendao.Category finalCategoryModel = new com.clover.spika.enterprise.chat.models.greendao.Category(
									Long.valueOf(chat.category.id), chat.category.name);

							categoryDao.insert(finalCategoryModel);
							finalCategoryModelId = finalCategoryModel.getId();
						}
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

							finalUserModel.setId((long) chat.user.getId());

							if (chat.user.user_id != 0) {
								finalUserModel.setId((long) chat.user.user_id);
							}

							if (chat.user.firstname != null) {
								finalUserModel.setFirstname(chat.user.firstname);
							}

							if (chat.user.lastname != null) {
								finalUserModel.setLastname(chat.user.lastname);
							}

							if (chat.user.type != 0) {
								finalUserModel.setType(chat.user.type);
							}

							if (chat.user.image != null) {
								finalUserModel.setImage(chat.user.image);
							}

							if (chat.user.image_thumb != null) {
								finalUserModel.setImage_thumb(chat.user.image_thumb);
							}

							finalUserModel.setIs_member(chat.user.is_member);
							finalUserModel.setIs_admin(chat.user.is_admin);

							if (chat.user.name != null) {
								finalUserModel.setName(chat.user.name);
							}

							if (chat.user.groupname != null) {
								finalUserModel.setGroupname(chat.user.groupname);
							}

							if (chat.user.chat_id != null) {
								finalUserModel.setChat_id(chat.user.chat_id);
							}

							finalUserModel.setIs_user(chat.user.is_user);
							finalUserModel.setIs_group(chat.user.is_group);
							finalUserModel.setIs_room(chat.user.is_room);

							userDao.update(finalUserModel);
							finalUserModelId = finalUserModel.getId();

						} else {

							com.clover.spika.enterprise.chat.models.greendao.User finalUserModel = new com.clover.spika.enterprise.chat.models.greendao.User(
									(long) chat.user.getId(), chat.user.firstname, chat.user.lastname, chat.user.type, chat.user.image, chat.user.image_thumb, chat.user.is_member,
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

							messageDao.update(finalMessageModel);
							finalMessageModelId = finalMessageModel.getId();

						} else {
							com.clover.spika.enterprise.chat.models.greendao.Message finalMessageModel = new com.clover.spika.enterprise.chat.models.greendao.Message(
									Long.valueOf(chat.last_message.id), Long.valueOf(chat.last_message.chat_id), Long.valueOf(chat.last_message.user_id),
									chat.last_message.firstname, chat.last_message.lastname, chat.last_message.image, chat.last_message.text, chat.last_message.file_id,
									chat.last_message.thumb_id, chat.last_message.longitude, chat.last_message.latitude, chat.last_message.created, chat.last_message.modified,
									chat.last_message.child_list, chat.last_message.image_thumb, chat.last_message.type, chat.last_message.root_id, chat.last_message.parent_id,
									chat.last_message.isMe, chat.last_message.isFailed, (long) chat.chat_id);

							messageDao.insert(finalMessageModel);
							finalMessageModelId = finalMessageModel.getId();
						}
					}

					if (chatDao.queryBuilder().where(Properties.Id.eq(chat.getId())).count() > 0) {

						com.clover.spika.enterprise.chat.models.greendao.Chat usedChatModel = chatDao.queryBuilder().where(Properties.Id.eq(chat.getId())).unique();

						usedChatModel.setId(Long.valueOf(chat.getId()));

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

						com.clover.spika.enterprise.chat.models.greendao.Chat finalChatModel = new com.clover.spika.enterprise.chat.models.greendao.Chat(
								Long.valueOf(chat.getId()), chat.chat_name, chat.seen_by, chat.total_count, chat.image_thumb, chat.image, chat.admin_id, chat.is_active, chat.type,
								chat.is_private, chat.password, chat.unread, chat.is_member, chat.modified, false, finalCategoryModelId, finalUserModelId, finalMessageModelId);

						chatDao.insert(finalChatModel);
					}
				}
			}
		}
	}

	public interface OnGlobalSearchDBChanged {
		public void onGlobalSearchDBChanged(List<GlobalModel> usableData, boolean isClear);
	}

	public interface OnGlobalSearchNetworkResult {
		public void onGlobalSearchNetworkResult(int totalCount);
	}

}

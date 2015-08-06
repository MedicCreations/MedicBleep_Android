package com.medicbleep.app.chat.caching.robospice;

import android.app.Activity;

import com.medicbleep.app.chat.caching.utils.DaoUtils;
import com.medicbleep.app.chat.extendables.BaseActivity;
import com.medicbleep.app.chat.models.Chat;
import com.medicbleep.app.chat.models.GlobalModel;
import com.medicbleep.app.chat.models.greendao.ChatDao;
import com.medicbleep.app.chat.models.greendao.GroupsDao;
import com.medicbleep.app.chat.models.greendao.UserDao;
import com.medicbleep.app.chat.services.robospice.CustomSpiceRequest;

public class EntryUtilsCaching {

	public static class DeleteEntry extends CustomSpiceRequest<GlobalModel> {

		private Activity activity;
		private int id;
		private int type;

		public DeleteEntry(Activity activity, int id, int type) {
			super(GlobalModel.class);

			this.activity = activity;
			this.id = id;
			this.type = type;
		}

		@Override
		public GlobalModel loadDataFromNetwork() throws Exception {

			if (activity instanceof BaseActivity) {

				if (type == GlobalModel.Type.CHAT) {

					ChatDao chatDao = ((BaseActivity) activity).getDaoSession().getChatDao();
					chatDao.deleteByKey(Long.valueOf(id));

				} else if (type == GlobalModel.Type.GROUP) {

					GroupsDao groupDao = ((BaseActivity) activity).getDaoSession().getGroupsDao();
					groupDao.deleteByKey(Long.valueOf(id));

				} else if (type == GlobalModel.Type.USER) {

					UserDao userDao = ((BaseActivity) activity).getDaoSession().getUserDao();
					userDao.deleteByKey(Long.valueOf(id));
				}
			}

			return null;
		}
	}
	
	public static class GetEntry extends CustomSpiceRequest<GlobalModel> {

		private Activity activity;
		private int id;
		private int type;

		public GetEntry(Activity activity, int id, int type) {
			super(GlobalModel.class);

			this.activity = activity;
			this.id = id;
			this.type = type;
		}

		@Override
		public GlobalModel loadDataFromNetwork() throws Exception {

			if (activity instanceof BaseActivity) {
				
				GlobalModel result = new GlobalModel();

				if (type == GlobalModel.Type.CHAT) {

					ChatDao chatDao = ((BaseActivity) activity).getDaoSession().getChatDao();
					com.medicbleep.app.chat.models.greendao.Chat chat = chatDao.load((long) id);
					
					if(result.chat != null){
						result.chat = DaoUtils.convertDaoChatToChatModel(chat);
					}

				} else if (type == GlobalModel.Type.GROUP) {

					GroupsDao groupDao = ((BaseActivity) activity).getDaoSession().getGroupsDao();
					com.medicbleep.app.chat.models.greendao.Groups group = groupDao.load((long) id);
					
					result.group = DaoUtils.convertDaoGroupToGroupModel(group);
					
				} else if (type == GlobalModel.Type.USER) {

					UserDao userDao = ((BaseActivity) activity).getDaoSession().getUserDao();
					com.medicbleep.app.chat.models.greendao.User user = userDao.load((long) id);
					
					result.user = DaoUtils.convertDaoUserToUserModel(user);
				}
				
				return result;
			}

			return null;
		}
	}


	public static class UpdateEntry extends CustomSpiceRequest<Chat> {

		private Activity activity;
		private int type;
		private GlobalModel entry;

		public UpdateEntry(Activity activity, int type, GlobalModel entry) {
			super(Chat.class);

			this.activity = activity;
			this.type = type;
			this.entry = entry;
		}

		@Override
		public Chat loadDataFromNetwork() throws Exception {

			if (activity instanceof BaseActivity) {

				if (type == GlobalModel.Type.CHAT) {

					ChatDao chatDao = ((BaseActivity) activity).getDaoSession().getChatDao();

					com.medicbleep.app.chat.models.greendao.Chat chat = chatDao.load((long) entry.chat.getId());

					chat = DaoUtils.convertChatModelToChatDao(chat, entry.chat, 0L, 0L, 0L, true);

					if (chat != null) {
						chatDao.update(chat);
					}
				} else if (type == GlobalModel.Type.GROUP) {

					GroupsDao groupDao = ((BaseActivity) activity).getDaoSession().getGroupsDao();

					com.medicbleep.app.chat.models.greendao.Groups group = groupDao.load((long) entry.group.getId());

					group = DaoUtils.convertGroupModelToGroupDao(group, entry.group);

					if (group != null) {
						groupDao.update(group);
					}
				} else if (type == GlobalModel.Type.USER) {

					UserDao userDao = ((BaseActivity) activity).getDaoSession().getUserDao();
					
					com.medicbleep.app.chat.models.greendao.User user = userDao.load((long) entry.user.getId());
					
					user = DaoUtils.convertUserModelToUserDao(user, entry.user);

					if (user != null) {
						userDao.update(user);
					}
				}
			}

			return null;
		}
	}

}

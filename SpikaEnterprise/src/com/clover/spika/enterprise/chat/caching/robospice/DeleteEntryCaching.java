package com.clover.spika.enterprise.chat.caching.robospice;

import android.app.Activity;

import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.models.Chat;
import com.clover.spika.enterprise.chat.models.GlobalModel;
import com.clover.spika.enterprise.chat.models.greendao.ChatDao;
import com.clover.spika.enterprise.chat.models.greendao.GroupsDao;
import com.clover.spika.enterprise.chat.models.greendao.UserDao;
import com.clover.spika.enterprise.chat.services.robospice.CustomSpiceRequest;

public class DeleteEntryCaching {

	public static class DeleteEntry extends CustomSpiceRequest<Chat> {

		private Activity activity;
		private int id;
		private int type;

		public DeleteEntry(Activity activity, int id, int type) {
			super(Chat.class);

			this.activity = activity;
			this.id = id;
			this.type = type;
		}

		@Override
		public Chat loadDataFromNetwork() throws Exception {

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

}

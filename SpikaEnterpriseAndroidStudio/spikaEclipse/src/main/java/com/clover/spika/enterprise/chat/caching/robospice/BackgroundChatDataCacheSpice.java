package com.clover.spika.enterprise.chat.caching.robospice;

import android.app.Activity;

import com.clover.spika.enterprise.chat.caching.BackgroundChatCaching;
import com.clover.spika.enterprise.chat.caching.BackgroundChatCaching.OnChatDBChanged;
import com.clover.spika.enterprise.chat.models.greendao.DaoSession;
import com.clover.spika.enterprise.chat.services.robospice.CustomSpiceRequest;
import com.octo.android.robospice.SpiceManager;

public class BackgroundChatDataCacheSpice {

	public static class GetData extends CustomSpiceRequest<Integer> {

		private DaoSession daoSession;
		private SpiceManager spiceManager;
		private String chatId;
		private String msgId;
		private OnChatDBChanged onDBChangeListener;
		
		
		public GetData(DaoSession daoSession, SpiceManager spiceManager, String chatId, String msgId, OnChatDBChanged onDBChangeListener) {
			super(Integer.class);

			this.daoSession = daoSession;
			this.spiceManager = spiceManager;
			this.chatId = chatId;
			this.msgId = msgId;
			this.onDBChangeListener = onDBChangeListener;
		}

		@Override
		public Integer loadDataFromNetwork() throws Exception {

			return BackgroundChatCaching.getData(daoSession, spiceManager, chatId, msgId, onDBChangeListener);
		}
	}
	
}

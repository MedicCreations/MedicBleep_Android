package com.medicbleep.app.chat.caching.robospice;

import com.medicbleep.app.chat.caching.BackgroundChatCaching;
import com.medicbleep.app.chat.caching.BackgroundChatCaching.OnChatDBChanged;
import com.medicbleep.app.chat.models.greendao.DaoSession;
import com.medicbleep.app.chat.services.robospice.CustomSpiceRequest;
import com.octo.android.robospice.SpiceManager;

public class BackgroundChatDataCacheSpice {

	public static class GetData extends CustomSpiceRequest<Integer> {

		private DaoSession daoSession;
		private SpiceManager spiceManager;
		private String chatId;
		private String msgId;
        private boolean isChatActive;
		private OnChatDBChanged onDBChangeListener;
		
		
		public GetData(DaoSession daoSession, SpiceManager spiceManager, String chatId, String msgId, boolean isChatActive, OnChatDBChanged onDBChangeListener) {
			super(Integer.class);

			this.daoSession = daoSession;
			this.spiceManager = spiceManager;
			this.chatId = chatId;
			this.msgId = msgId;
            this.isChatActive = isChatActive;
			this.onDBChangeListener = onDBChangeListener;
		}

		@Override
		public Integer loadDataFromNetwork() throws Exception {

			return BackgroundChatCaching.getData(daoSession, spiceManager, chatId, msgId, isChatActive, onDBChangeListener);
		}
	}
	
}

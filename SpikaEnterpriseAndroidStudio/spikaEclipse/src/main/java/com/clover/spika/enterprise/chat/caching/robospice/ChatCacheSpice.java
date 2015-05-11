package com.clover.spika.enterprise.chat.caching.robospice;

import android.app.Activity;

import com.clover.spika.enterprise.chat.caching.ChatCaching;
import com.clover.spika.enterprise.chat.caching.ChatCaching.OnChatDBChanged;
import com.clover.spika.enterprise.chat.caching.ChatCaching.OnChatNetworkResult;
import com.clover.spika.enterprise.chat.models.Chat;
import com.clover.spika.enterprise.chat.services.robospice.CustomSpiceRequest;
import com.octo.android.robospice.SpiceManager;

public class ChatCacheSpice {

	public static class GetData extends CustomSpiceRequest<Chat> {

		private Activity activity;
		private SpiceManager spiceManager;
		private boolean isClear;
		private boolean isPagging;
		private boolean isNewMsg;
		private boolean isSend;
		private boolean isRefresh;
		private boolean isFirstTime;
		private String chatId;
		private String msgId;
		private int adapterCount;
		private OnChatDBChanged onDBChangeListener;
		private OnChatNetworkResult onNetworkListener;
		
		
		public GetData(Activity activity, SpiceManager spiceManager, boolean isClear, boolean isPagging, boolean isNewMsg, 
				boolean isSend, boolean isRefresh, boolean isFirstTime, String chatId, String msgId, int adapterCount,
				OnChatDBChanged onDBChangeListener, OnChatNetworkResult onNetworkListener) {
			super(Chat.class);

			this.activity = activity;
			this.spiceManager = spiceManager;
			this.isClear = isClear;
			this.isPagging = isPagging;
			this.isNewMsg = isNewMsg;
			this.isSend = isSend;
			this.isRefresh = isRefresh;
			this.isFirstTime = isFirstTime;
			this.chatId = chatId;
			this.msgId = msgId;
			this.adapterCount = adapterCount;
			this.onDBChangeListener = onDBChangeListener;
			this.onNetworkListener = onNetworkListener;
		}

		@Override
		public Chat loadDataFromNetwork() throws Exception {

			return ChatCaching.getData(activity, spiceManager, isClear, isPagging, isNewMsg, isSend, isRefresh, isFirstTime, chatId, msgId, adapterCount, onDBChangeListener, onNetworkListener);
		}
	}
	
	public static class StartChat extends CustomSpiceRequest<Chat> {

		private Activity activity;
		private SpiceManager spiceManager;
		private Chat chat;
		private String chatId;
		private String msgId;
		private OnChatDBChanged onDBChangeListener;
		private OnChatNetworkResult onNetworkListener;
		
		
		public StartChat(Activity activity, SpiceManager spiceManager, String chatId, String msgId, 
				OnChatDBChanged onDBChangeListener, OnChatNetworkResult onNetworkListener, Chat chat) {
			super(Chat.class);

			this.activity = activity;
			this.spiceManager = spiceManager;
			this.chat = chat;
			this.chatId = chatId;
			this.msgId = msgId;
			this.onDBChangeListener = onDBChangeListener;
			this.onNetworkListener = onNetworkListener;
		}

		@Override
		public Chat loadDataFromNetwork() throws Exception {
			
			return ChatCaching.startChat(activity, spiceManager, true, false, false, false, false, chatId, 
					msgId, 0, onDBChangeListener, onNetworkListener, chat);

		}
	}
}

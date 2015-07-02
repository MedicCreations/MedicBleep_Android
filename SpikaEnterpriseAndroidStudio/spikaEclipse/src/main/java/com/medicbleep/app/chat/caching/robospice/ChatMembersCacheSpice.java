package com.medicbleep.app.chat.caching.robospice;

import java.util.List;

import android.app.Activity;

import com.medicbleep.app.chat.caching.ChatMembersCaching;
import com.medicbleep.app.chat.caching.ChatMembersCaching.OnChatMembersDBChanged;
import com.medicbleep.app.chat.models.GlobalModel;
import com.medicbleep.app.chat.services.robospice.CustomSpiceRequest;
import com.octo.android.robospice.SpiceManager;

public class ChatMembersCacheSpice {

	@SuppressWarnings("rawtypes")
	public static class GetChatMembers extends CustomSpiceRequest<List> {

		private Activity activity;

		private SpiceManager spiceManager;
		private String chatId;

		private OnChatMembersDBChanged onDBChangeListener;

		public GetChatMembers(Activity activity, SpiceManager spiceManager, String chatId, OnChatMembersDBChanged onDBChangeListener) {
			super(List.class);

			this.activity = activity;

			this.spiceManager = spiceManager;
			this.chatId = chatId;
			this.onDBChangeListener = onDBChangeListener;
		}

		@Override
		public List<GlobalModel> loadDataFromNetwork() throws Exception {
			return ChatMembersCaching.GetChatMembers(activity, spiceManager, chatId, onDBChangeListener);
		}
	}

}

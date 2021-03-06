package com.clover.spika.enterprise.chat.caching.robospice;

import java.util.List;

import android.app.Activity;

import com.clover.spika.enterprise.chat.caching.GlobalCaching;
import com.clover.spika.enterprise.chat.caching.GlobalCaching.OnGlobalMemberDBChanged;
import com.clover.spika.enterprise.chat.caching.GlobalCaching.OnGlobalMemberNetworkResult;
import com.clover.spika.enterprise.chat.caching.GlobalCaching.OnGlobalSearchDBChanged;
import com.clover.spika.enterprise.chat.caching.GlobalCaching.OnGlobalSearchNetworkResult;
import com.clover.spika.enterprise.chat.models.GlobalModel;
import com.clover.spika.enterprise.chat.services.robospice.CustomSpiceRequest;
import com.octo.android.robospice.SpiceManager;

public class GlobalCacheSpice {

	@SuppressWarnings("rawtypes")
	public static class GlobalSearch extends CustomSpiceRequest<List> {

		private Activity activity;

		private SpiceManager spiceManager;
		private int page;
		private String chatId;
		private String categoryId;
		private int type;
		private String searchTerm;
		private boolean toClear;
		private boolean justDatabase = false;

		private OnGlobalSearchDBChanged onDBChangeListener;
		private OnGlobalSearchNetworkResult onNetworkListener;

		public GlobalSearch(Activity activity, SpiceManager spiceManager, int page, String chatId, String categoryId, int type, String searchTerm, boolean toClear,
				OnGlobalSearchDBChanged onDBChangeListener, OnGlobalSearchNetworkResult onNetworkListener) {
			super(List.class);

			this.activity = activity;

			this.spiceManager = spiceManager;
			this.page = page;
			this.chatId = chatId;
			this.categoryId = categoryId;
			this.type = type;
			this.searchTerm = searchTerm;
			this.toClear = toClear;
			this.onDBChangeListener = onDBChangeListener;
			this.onNetworkListener = onNetworkListener;
		}
		
		public GlobalSearch(Activity activity, SpiceManager spiceManager, int page, String chatId, String categoryId, int type, String searchTerm, 
				boolean toClear, boolean justDatabase, OnGlobalSearchDBChanged onDBChangeListener, OnGlobalSearchNetworkResult onNetworkListener) {
			super(List.class);

			this.activity = activity;

			this.spiceManager = spiceManager;
			this.page = page;
			this.chatId = chatId;
			this.categoryId = categoryId;
			this.type = type;
			this.searchTerm = searchTerm;
			this.toClear = toClear;
			this.justDatabase = justDatabase;
			this.onDBChangeListener = onDBChangeListener;
			this.onNetworkListener = onNetworkListener;
		}

		@Override
		public List<GlobalModel> loadDataFromNetwork() throws Exception {
			return GlobalCaching.GlobalSearch(justDatabase, activity, spiceManager, page, chatId, categoryId, type, searchTerm, toClear, onDBChangeListener, onNetworkListener);
		}
	}

	@SuppressWarnings("rawtypes")
	public static class GlobalMember extends CustomSpiceRequest<List> {

		private Activity activity;

		private SpiceManager spiceManager;
		private int page;
		private String chatId;
		private String groupId;
		private int type;
		private boolean isToClear;

		private OnGlobalMemberDBChanged onDBChangeListener;
		private OnGlobalMemberNetworkResult onNetworkListener;

		public GlobalMember(Activity activity, SpiceManager spiceManager, int page, String chatId, String groupId, int type, boolean isToClear, OnGlobalMemberDBChanged onDBChangeListener,
				OnGlobalMemberNetworkResult onNetworkListener) {
			super(List.class);

			this.activity = activity;

			this.spiceManager = spiceManager;
			this.page = page;
			this.chatId = chatId;
			this.groupId = groupId;
			this.type = type;
			this.isToClear = isToClear;
			this.onDBChangeListener = onDBChangeListener;
			this.onNetworkListener = onNetworkListener;
		}

		@Override
		public List<GlobalModel> loadDataFromNetwork() throws Exception {
			return GlobalCaching.GlobalMembers(activity, spiceManager, page, chatId, groupId, type, isToClear, onDBChangeListener, onNetworkListener);
		}
	}

}

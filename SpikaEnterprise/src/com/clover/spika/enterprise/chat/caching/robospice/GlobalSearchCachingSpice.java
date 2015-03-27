package com.clover.spika.enterprise.chat.caching.robospice;

import java.util.List;

import android.app.Activity;

import com.clover.spika.enterprise.chat.caching.GlobalSearchCaching;
import com.clover.spika.enterprise.chat.caching.GlobalSearchCaching.OnGlobalSearchDBChanged;
import com.clover.spika.enterprise.chat.caching.GlobalSearchCaching.OnGlobalSearchNetworkResult;
import com.clover.spika.enterprise.chat.models.GlobalModel;
import com.clover.spika.enterprise.chat.services.robospice.CustomSpiceRequest;
import com.octo.android.robospice.SpiceManager;

public class GlobalSearchCachingSpice {

	@SuppressWarnings("rawtypes")
	public static class GetData extends CustomSpiceRequest<List> {

		private Activity activity;

		private SpiceManager spiceManager;
		private int page;
		private String chatId;
		private String categoryId;
		private int type;
		private String searchTerm;
		private boolean toClear;

		private OnGlobalSearchDBChanged onDBChangeListener;
		private OnGlobalSearchNetworkResult onNetworkListener;

		public GetData(Activity activity, SpiceManager spiceManager, int page, String chatId, String categoryId, int type, String searchTerm, boolean toClear,
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

		@Override
		public List<GlobalModel> loadDataFromNetwork() throws Exception {

			return GlobalSearchCaching.getData(activity, spiceManager, page, chatId, categoryId, type, searchTerm, toClear, onDBChangeListener, onNetworkListener);
		}
	}

}

package com.clover.spika.enterprise.chat.caching.robospice;

import java.util.List;

import android.app.Activity;

import com.clover.spika.enterprise.chat.caching.RecentFragmentCaching;
import com.clover.spika.enterprise.chat.caching.RecentFragmentCaching.OnRecentFragmentDBChanged;
import com.clover.spika.enterprise.chat.caching.RecentFragmentCaching.OnRecentFragmentNetworkResult;
import com.clover.spika.enterprise.chat.models.Chat;
import com.clover.spika.enterprise.chat.services.robospice.CustomSpiceRequest;
import com.octo.android.robospice.SpiceManager;

public class RecentFragmentCacheSpice {

	@SuppressWarnings("rawtypes")
	public static class GetData extends CustomSpiceRequest<List> {

		private Activity activity;
		private SpiceManager spiceManager;
		private int page;
		private int toClear;
		private OnRecentFragmentDBChanged onDBChangeListener;
		private OnRecentFragmentNetworkResult onNetworkListener;

		public GetData(Activity activity, SpiceManager spiceManager, int page, int toClear, OnRecentFragmentDBChanged onDBChangeListener,
				OnRecentFragmentNetworkResult onNetworkListener) {
			super(List.class);

			this.activity = activity;
			this.spiceManager = spiceManager;
			this.page = page;
			this.toClear = toClear;
			this.onDBChangeListener = onDBChangeListener;
			this.onNetworkListener = onNetworkListener;
		}

		@Override
		public List<Chat> loadDataFromNetwork() throws Exception {

			return RecentFragmentCaching.getData(activity, spiceManager, page, toClear, onDBChangeListener, onNetworkListener);
		}
	}
	
}

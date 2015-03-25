package com.clover.spika.enterprise.chat.caching.robospice;

import java.util.List;

import android.app.Activity;

import com.clover.spika.enterprise.chat.caching.LobbyCaching;
import com.clover.spika.enterprise.chat.caching.LobbyCaching.OnLobbyDBChanged;
import com.clover.spika.enterprise.chat.caching.LobbyCaching.OnLobbyNetworkResult;
import com.clover.spika.enterprise.chat.models.Chat;
import com.clover.spika.enterprise.chat.services.robospice.CustomSpiceRequest;
import com.octo.android.robospice.SpiceManager;

public class LobbyCacheSpice {

	@SuppressWarnings("rawtypes")
	public static class GetData extends CustomSpiceRequest<List> {

		private Activity activity;
		private SpiceManager spiceManager;
		private int page;
		private int toClear;
		private OnLobbyDBChanged onDBChangeListener;
		private OnLobbyNetworkResult onNetworkListener;

		public GetData(Activity activity, SpiceManager spiceManager, int page, int toClear, OnLobbyDBChanged onDBChangeListener, OnLobbyNetworkResult onNetworkListener) {
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

			return LobbyCaching.getData(activity, spiceManager, page, toClear, onDBChangeListener, onNetworkListener);
		}
	}
}

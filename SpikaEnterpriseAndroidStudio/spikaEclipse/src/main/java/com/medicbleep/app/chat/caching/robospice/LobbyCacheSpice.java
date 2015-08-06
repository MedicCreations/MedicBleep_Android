package com.medicbleep.app.chat.caching.robospice;

import java.util.List;

import android.app.Activity;

import com.medicbleep.app.chat.caching.LobbyCaching;
import com.medicbleep.app.chat.caching.LobbyCaching.OnLobbyDBChanged;
import com.medicbleep.app.chat.caching.LobbyCaching.OnLobbyNetworkResult;
import com.medicbleep.app.chat.models.Chat;
import com.medicbleep.app.chat.services.robospice.CustomSpiceRequest;
import com.octo.android.robospice.SpiceManager;

public class LobbyCacheSpice {

	@SuppressWarnings("rawtypes")
	public static class GetData extends CustomSpiceRequest<List> {

		private Activity activity;
		private SpiceManager spiceManager;
		private int page;
		private boolean toClear;
		private OnLobbyDBChanged onDBChangeListener;
		private OnLobbyNetworkResult onNetworkListener;

		public GetData(Activity activity, SpiceManager spiceManager, int page, boolean toClear, OnLobbyDBChanged onDBChangeListener, OnLobbyNetworkResult onNetworkListener) {
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

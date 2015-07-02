package com.medicbleep.app.chat.caching.robospice;


import android.app.Activity;

import com.medicbleep.app.chat.caching.StickersCaching;
import com.medicbleep.app.chat.caching.StickersCaching.OnStickersDBChanged;
import com.medicbleep.app.chat.caching.StickersCaching.OnStickersNetworkResult;
import com.medicbleep.app.chat.models.StickersHolder;
import com.medicbleep.app.chat.services.robospice.CustomSpiceRequest;
import com.octo.android.robospice.SpiceManager;

public class StickersCacheSpice {

	public static class GetData extends CustomSpiceRequest<StickersHolder> {

		private Activity activity;
		private SpiceManager spiceManager;
		private OnStickersDBChanged onDBChangeListener;
		private OnStickersNetworkResult onNetworkListener;

		public GetData(Activity activity, SpiceManager spiceManager, OnStickersDBChanged onDBChangeListener,
				OnStickersNetworkResult onNetworkListener) {
			super(StickersHolder.class);

			this.activity = activity;
			this.spiceManager = spiceManager;
			this.onDBChangeListener = onDBChangeListener;
			this.onNetworkListener = onNetworkListener;
		}

		@Override
		public StickersHolder loadDataFromNetwork() throws Exception {

			return StickersCaching.getData(activity, spiceManager, onDBChangeListener, onNetworkListener);
		}
	}
	
}

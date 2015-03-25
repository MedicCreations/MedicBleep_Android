package com.clover.spika.enterprise.chat.caching.robospice;

import java.util.List;

import android.app.Activity;

import com.clover.spika.enterprise.chat.caching.StickersCaching;
import com.clover.spika.enterprise.chat.caching.StickersCaching.OnStickersDBChanged;
import com.clover.spika.enterprise.chat.caching.StickersCaching.OnStickersNetworkResult;
import com.clover.spika.enterprise.chat.models.Stickers;
import com.clover.spika.enterprise.chat.models.StickersHolder;
import com.clover.spika.enterprise.chat.services.robospice.CustomSpiceRequest;
import com.octo.android.robospice.SpiceManager;

public class StickersCacheSpice {

	@SuppressWarnings("rawtypes")
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

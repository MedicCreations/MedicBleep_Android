package com.medicbleep.app.chat.caching.robospice;

import java.util.List;

import android.app.Activity;

import com.medicbleep.app.chat.caching.ThreadCaching;
import com.medicbleep.app.chat.caching.ThreadCaching.OnThreadDBChanged;
import com.medicbleep.app.chat.caching.ThreadCaching.OnThreadNetworkResult;
import com.medicbleep.app.chat.models.Message;
import com.medicbleep.app.chat.services.robospice.CustomSpiceRequest;
import com.octo.android.robospice.SpiceManager;

public class ThreadCacheSpice {

	@SuppressWarnings("rawtypes")
	public static class GetData extends CustomSpiceRequest<List> {

		private Activity activity;
		private SpiceManager spiceManager;
		private String msgId;
		private OnThreadDBChanged onDBChangeListener;
		private OnThreadNetworkResult onNetworkListener;
		
		
		public GetData(Activity activity, SpiceManager spiceManager, String msgId,  
				OnThreadDBChanged onDBChangeListener, OnThreadNetworkResult onNetworkListener) {
			super(List.class);

			this.activity = activity;
			this.spiceManager = spiceManager;
			this.msgId = msgId;
			this.onDBChangeListener = onDBChangeListener;
			this.onNetworkListener = onNetworkListener;
		}

		@Override
		public List<Message> loadDataFromNetwork() throws Exception {

			return ThreadCaching.getData(activity, spiceManager, msgId, onDBChangeListener, onNetworkListener);
		}
	}
}

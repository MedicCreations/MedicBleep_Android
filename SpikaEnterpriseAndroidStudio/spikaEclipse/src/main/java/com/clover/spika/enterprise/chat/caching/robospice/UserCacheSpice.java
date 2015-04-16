package com.clover.spika.enterprise.chat.caching.robospice;

import android.app.Activity;
import com.clover.spika.enterprise.chat.caching.UserCaching;
import com.clover.spika.enterprise.chat.caching.UserCaching.OnUserGetDetailsDBChanged;
import com.clover.spika.enterprise.chat.models.UserWrapper;
import com.clover.spika.enterprise.chat.services.robospice.CustomSpiceRequest;
import com.octo.android.robospice.SpiceManager;

public class UserCacheSpice {
	
	public static class GetProfile extends CustomSpiceRequest<UserWrapper> {

		private Activity activity;

		private SpiceManager spiceManager;
		private String userId;
		private boolean getDetailValues;

		private OnUserGetDetailsDBChanged onDBChangeListener;

		public GetProfile(Activity activity, SpiceManager spiceManager, String userId, boolean getDetailValues,
				OnUserGetDetailsDBChanged onDBChangeListener) {
			super(UserWrapper.class);

			this.activity = activity;

			this.spiceManager = spiceManager;
			this.userId = userId;
			this.getDetailValues = getDetailValues;
			this.onDBChangeListener = onDBChangeListener;
		}

		@Override
		public UserWrapper loadDataFromNetwork() throws Exception {
			return UserCaching.GetProfile(activity, spiceManager, userId, getDetailValues, onDBChangeListener);
		}
	}

}

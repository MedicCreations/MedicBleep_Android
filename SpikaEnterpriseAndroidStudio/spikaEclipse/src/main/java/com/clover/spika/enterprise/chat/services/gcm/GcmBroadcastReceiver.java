package com.clover.spika.enterprise.chat.services.gcm;

import com.google.android.gcm.GCMBroadcastReceiver;

import android.content.Context;

public class GcmBroadcastReceiver extends GCMBroadcastReceiver {

	@Override
	protected String getGCMIntentServiceClassName(Context context) {
		return GcmIntentService.class.getName();
	}

}

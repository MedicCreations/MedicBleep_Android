package com.clover.spika.enterprise.chat.services.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;

import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Logger;
import com.clover.spika.enterprise.chat.utils.PushHandle;
import com.google.android.gms.gcm.GoogleCloudMessaging;

/**
 * GCMIntentService
 * 
 * Handles push broadcast and generates HookUp notification if application is in
 * foreground or Android notification if application is in background.
 */

public class GcmIntentService extends IntentService {

	public GcmIntentService() {
		super("GcmIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle extras = intent.getExtras();
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
		String messageType = gcm.getMessageType(intent);

		if (!extras.isEmpty()) {

			Logger.i("PushReceived: " + extras.toString());

			if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {

				String chatId = "";
				String firstName = "";
				String type = "";
				String chatPassword = "";

				if (extras.containsKey(Const.CHAT_ID)) {
					chatId = extras.getString(Const.CHAT_ID);
				}

				if (extras.containsKey(Const.FIRSTNAME)) {
					firstName = extras.getString(Const.FIRSTNAME);
				}

				if (extras.containsKey(Const.TYPE)) {
					type = extras.getString(Const.TYPE);
				}

				if (extras.containsKey(Const.PUSH_CHAT_PASSWORD)) {
					chatPassword = extras.getString(Const.PUSH_CHAT_PASSWORD);
				}

				PushHandle.handlePushNotification(chatId, firstName, chatPassword, type, this);
			}
		}

		GcmBroadcastReceiver.completeWakefulIntent(intent);
	}

}

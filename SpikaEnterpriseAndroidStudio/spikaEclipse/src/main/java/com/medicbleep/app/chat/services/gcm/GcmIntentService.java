package com.medicbleep.app.chat.services.gcm;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.medicbleep.app.chat.utils.Const;
import com.medicbleep.app.chat.utils.GoogleUtils;
import com.medicbleep.app.chat.utils.Logger;
import com.medicbleep.app.chat.utils.PushHandle;
import com.google.android.gcm.GCMBaseIntentService;

public class GcmIntentService extends GCMBaseIntentService {

	public GcmIntentService() {
		super(Const.GCM_SENDER_ID);
	}

	@Override
	protected void onMessage(Context arg0, Intent intent) {

		Bundle extras = intent.getExtras();

		if (!extras.isEmpty()) {

			Logger.i("PushReceived: " + extras.toString());

			String chatId = "";
			String messageId = null;
			String organizationId = "";
			String firstName = "";
			String type = "";
			String chatPassword = "";

			if (extras.containsKey(Const.CHAT_ID)) {
				chatId = extras.getString(Const.CHAT_ID);
			}

			if (extras.containsKey(Const.ORGANIZATION_ID)) {
				organizationId = extras.getString(Const.ORGANIZATION_ID);
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

			if (extras.containsKey(Const.MESSAGE_ID)) {
				messageId = extras.getString(Const.MESSAGE_ID);
			}

			PushHandle.handlePushNotification(chatId, messageId, organizationId, firstName, chatPassword, type, this);
		}
	}

	@Override
	protected void onRegistered(Context ctx, String regId) {
		new GoogleUtils().storeRegistrationId(ctx, regId);
	}

	@Override
	protected void onUnregistered(Context arg0, String arg1) {
	}

	@Override
	protected void onError(Context arg0, String arg1) {
	}

}

package com.clover.spika.enterprise.chat.gcm;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.SplashActivity;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Logger;
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

	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle extras = intent.getExtras();
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
		String messageType = gcm.getMessageType(intent);

		if (!extras.isEmpty()) {

			Logger.info("PushReceived: " + extras.toString());

			if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {

				String chatId = "";
				String firstName = "";
				String chatName = "";
				String chatImage = "";

				if (extras.containsKey(Const.CHAT_ID)) {
					chatId = extras.getString(Const.CHAT_ID);
				}

				if (extras.containsKey(Const.FIRSTNAME)) {
					firstName = extras.getString(Const.FIRSTNAME);
				}

				if (extras.containsKey(Const.CHAT_NAME)) {
					chatName = extras.getString(Const.CHAT_NAME);
				}

				if (extras.containsKey(Const.PUSH_CHAT_THUMB)) {
					chatImage = extras.getString(Const.PUSH_CHAT_THUMB);
				}

				String message = getResources().getString(R.string.msg_from) + " " + firstName;

				// TODO
				Intent inBroadcast = new Intent();
				inBroadcast.setAction(Const.PUSH_INTENT_ACTION);
				inBroadcast.putExtra(Const.CHAT_ID, chatId);
				inBroadcast.putExtra(Const.CHAT_NAME, chatName);
				inBroadcast.putExtra(Const.IMAGE, chatImage);
				inBroadcast.putExtra(Const.PUSH_MESSAGE, message);
				sendBroadcast(inBroadcast);

				NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

				Intent pushIntent = new Intent(this, SplashActivity.class);
				pushIntent.putExtra(Const.CHAT_ID, chatId);
				pushIntent.putExtra(Const.CHAT_NAME, chatName);
				pushIntent.putExtra(Const.IMAGE, chatImage);
				pushIntent.putExtra(Const.FROM_NOTIFICATION, true);

				PendingIntent contentIntent = PendingIntent.getActivity(this, 0, pushIntent, PendingIntent.FLAG_UPDATE_CURRENT);

				Notification notification = null;

				if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
					notification = new Notification.Builder(this).setContentTitle(getResources().getString(R.string.app_name)).setWhen(System.currentTimeMillis()).setContentIntent(contentIntent).setDefaults(Notification.DEFAULT_SOUND).setAutoCancel(true).setContentText(message)
							.setSmallIcon(R.drawable.ic_launcher).build();
				} else {
					notification = new Notification(R.drawable.ic_launcher, message, System.currentTimeMillis());
					notification.defaults = Notification.DEFAULT_ALL;
					notification.flags = Notification.FLAG_AUTO_CANCEL;
					notification.setLatestEventInfo(this, getResources().getString(R.string.app_name), message, contentIntent);
				}

				mNotificationManager.notify(getIntId(chatId), notification);
			}
		}

		GcmBroadcastReceiver.completeWakefulIntent(intent);
	}

	private int getIntId(String groupId) {

		int sum = 0;

		char[] charArray = groupId.toCharArray();

		for (char c : charArray) {
			sum = sum + Character.getNumericValue(c);
		}

		return sum;
	}

}

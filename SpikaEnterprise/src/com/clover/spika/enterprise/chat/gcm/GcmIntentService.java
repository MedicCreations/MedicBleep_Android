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

				if (extras.containsKey(Const.CHAT_ID)) {
					chatId = extras.getString(Const.CHAT_ID);
				}

				if (extras.containsKey(Const.FIRSTNAME)) {
					firstName = extras.getString(Const.FIRSTNAME);
				}

				if (extras.containsKey(Const.CHAT_NAME)) {
					chatName = extras.getString(Const.CHAT_NAME);
				}

				sendNotification(chatId, firstName, chatName);
			}
		}

		GcmBroadcastReceiver.completeWakefulIntent(intent);
	}

	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	private void sendNotification(final String chatId, final String firstName, final String chatName) {

		String message = getResources().getString(R.string.msg_from) + " " + firstName;

		// TODO
		// if (ChatActivity.instance != null) {
		// ChatActivity.instance.callAfterPush(groupId, msg, Const.PT_MESSAGE);
		// } else if (BaseActivity.instance != null) {
		// BaseActivity.instance.showPopUp(msg, groupId, Const.PT_MESSAGE);
		// } else {

		NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

		Intent intent = new Intent(this, SplashActivity.class);
		intent.putExtra(Const.CHAT_ID, chatId);
		intent.putExtra(Const.CHAT_NAME, chatName);
		intent.putExtra(Const.FROM_NOTIFICATION, true);

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

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
		// }
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

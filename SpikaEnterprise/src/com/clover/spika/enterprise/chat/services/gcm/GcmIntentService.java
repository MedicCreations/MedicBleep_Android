package com.clover.spika.enterprise.chat.services.gcm;

import java.util.List;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;

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

			Logger.i("PushReceived: " + extras.toString());

			if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {

				String chatId = "";
				String firstName = "";
				String chatName = "";
				String chatThumb = "";
				String chatImage = "";
				String chatType = "";
				String type = "";
				int isActive = 0;
				// TODO category id and name from push
				int isPrivate = 0;
				String adminId = "";
				String chatPassword = "";

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
					chatThumb = extras.getString(Const.PUSH_CHAT_THUMB);
				}

				if (extras.containsKey(Const.PUSH_CHAT_IMAGE)) {
					chatImage = extras.getString(Const.PUSH_CHAT_IMAGE);
				}

				if (extras.containsKey(Const.PUSH_CHAT_TYPE)) {
					chatType = extras.getString(Const.PUSH_CHAT_TYPE);
				}

				if (extras.containsKey(Const.TYPE)) {
					type = extras.getString(Const.TYPE);
				}

				if (extras.containsKey(Const.ADMIN_ID)) {
					adminId = extras.getString(Const.ADMIN_ID);
				}

				if (extras.containsKey(Const.IS_ACTIVE)) {
					isActive = extras.getString(Const.IS_ACTIVE).equals("1") ? 1 : 0;
				}

				if (extras.containsKey(Const.IS_PRIVATE)) {
					isPrivate = extras.getString(Const.IS_PRIVATE).equals("1") ? 1 : 0;
				}

				if (extras.containsKey(Const.PUSH_CHAT_PASSWORD)) {
					chatPassword = extras.getString(Const.PUSH_CHAT_PASSWORD);
				}

				String message = getResources().getString(R.string.msg_from) + " " + firstName;

				ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
				List<RunningTaskInfo> taskInfo = am.getRunningTasks(1);
				ComponentName componentInfo = taskInfo.get(0).topActivity;

				boolean isScreenOn = true;
				PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
				isScreenOn = pm.isScreenOn();

				if (componentInfo.getPackageName().equalsIgnoreCase("com.clover.spika.enterprise.chat") && isScreenOn) {

					Intent inBroadcast = new Intent();
					inBroadcast.setAction(Const.PUSH_INTENT_ACTION);
					inBroadcast.putExtra(Const.CHAT_ID, chatId);
					inBroadcast.putExtra(Const.CHAT_NAME, chatName);
					inBroadcast.putExtra(Const.IMAGE, chatImage);
					inBroadcast.putExtra(Const.IMAGE_THUMB, chatThumb);
					inBroadcast.putExtra(Const.PUSH_TYPE, type);
					inBroadcast.putExtra(Const.PUSH_MESSAGE, message);
					inBroadcast.putExtra(Const.TYPE, chatType);
					inBroadcast.putExtra(Const.IS_ACTIVE, isActive);
					inBroadcast.putExtra(Const.ADMIN_ID, adminId);
					inBroadcast.putExtra(Const.PASSWORD, chatPassword);
					inBroadcast.putExtra(Const.IS_PRIVATE, isPrivate);

					LocalBroadcastManager.getInstance(this).sendBroadcast(inBroadcast);

				} else {

					if (Integer.parseInt(type) == Const.PUSH_TYPE_SEEN) {
						return;
					}

					NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

					Intent pushIntent = new Intent(this, SplashActivity.class);
					pushIntent.putExtra(Const.CHAT_ID, chatId);
					pushIntent.putExtra(Const.CHAT_NAME, chatName);
					pushIntent.putExtra(Const.IMAGE, chatImage);
					pushIntent.putExtra(Const.IMAGE_THUMB, chatThumb);
					pushIntent.putExtra(Const.PUSH_TYPE, type);
					pushIntent.putExtra(Const.FROM_NOTIFICATION, true);
					pushIntent.putExtra(Const.TYPE, chatType);
					pushIntent.putExtra(Const.IS_ACTIVE, isActive);
					pushIntent.putExtra(Const.ADMIN_ID, adminId);
					pushIntent.putExtra(Const.PASSWORD, chatPassword);
					pushIntent.putExtra(Const.IS_PRIVATE, isPrivate);

					PendingIntent contentIntent = PendingIntent.getActivity(this, 0, pushIntent, PendingIntent.FLAG_UPDATE_CURRENT);

					Notification notification = null;

					int rgbLed = 0x43A5DA;
					int ledOn = 2000;
					int ledOff = 5000;

					if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {

						Notification.Builder notifBuilder = new Notification.Builder(this);
						notifBuilder.setContentTitle(getResources().getString(R.string.app_name));
						notifBuilder.setWhen(System.currentTimeMillis());
						notifBuilder.setContentIntent(contentIntent);
						notifBuilder.setAutoCancel(true);
						notifBuilder.setContentText(message);
						notifBuilder.setSmallIcon(R.drawable.ic_launcher);

						notifBuilder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE | Notification.FLAG_GROUP_SUMMARY | Notification.FLAG_SHOW_LIGHTS);
						notifBuilder.setLights(rgbLed, ledOn, ledOff);

						notification = notifBuilder.build();
					} else {
						notification = new Notification(R.drawable.ic_launcher, message, System.currentTimeMillis());
						notification.defaults = Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE | Notification.FLAG_GROUP_SUMMARY | Notification.FLAG_SHOW_LIGHTS;
						notification.ledARGB = rgbLed;
						notification.ledOnMS = ledOn;
						notification.ledOffMS = ledOff;
						notification.flags = Notification.FLAG_AUTO_CANCEL;
						notification.setLatestEventInfo(this, getResources().getString(R.string.app_name), message, contentIntent);
					}

					mNotificationManager.notify(getIntId(chatId), notification);
				}
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

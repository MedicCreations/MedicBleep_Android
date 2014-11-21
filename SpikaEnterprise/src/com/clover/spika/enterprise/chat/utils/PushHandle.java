package com.clover.spika.enterprise.chat.utils;

import java.util.List;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;

import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.SplashActivity;

public class PushHandle {

	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	public static void handlePushNotification(String chatId, String firstName, String chatPassword, String type, Context context) {

		String message = context.getResources().getString(R.string.msg_from) + " " + firstName;

		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> taskInfo = am.getRunningTasks(1);
		ComponentName componentInfo = taskInfo.get(0).topActivity;

		boolean isScreenOn = true;
		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		isScreenOn = pm.isScreenOn();

		if (componentInfo.getPackageName().equalsIgnoreCase("com.clover.spika.enterprise.chat") && isScreenOn) {

			Intent inBroadcast = new Intent();
			inBroadcast.setAction(Const.PUSH_INTENT_ACTION);
			inBroadcast.putExtra(Const.CHAT_ID, chatId);
			inBroadcast.putExtra(Const.PUSH_TYPE, type);
			inBroadcast.putExtra(Const.PUSH_MESSAGE, message);
			inBroadcast.putExtra(Const.PASSWORD, chatPassword);

			LocalBroadcastManager.getInstance(context).sendBroadcast(inBroadcast);

		} else {

			if (Integer.valueOf(type) == Const.PUSH_TYPE_SEEN) {
				return;
			}

			NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

			Intent pushIntent = new Intent(context, SplashActivity.class);
			pushIntent.putExtra(Const.CHAT_ID, chatId);
			pushIntent.putExtra(Const.PUSH_TYPE, type);
			pushIntent.putExtra(Const.FROM_NOTIFICATION, true);
			pushIntent.putExtra(Const.PASSWORD, chatPassword);
			pushIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			pushIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

			PendingIntent contentIntent = PendingIntent.getActivity(context, getIntId(chatId), pushIntent, PendingIntent.FLAG_UPDATE_CURRENT);

			Notification notification = null;

			int rgbLed = 0x43A5DA;
			int ledOn = 2000;
			int ledOff = 5000;

			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {

				Notification.Builder notifBuilder = new Notification.Builder(context);
				notifBuilder.setContentTitle(context.getResources().getString(R.string.app_name));
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
				notification.setLatestEventInfo(context, context.getResources().getString(R.string.app_name), message, contentIntent);
			}

			mNotificationManager.notify(getIntId(chatId), notification);
		}
	}

	private static int getIntId(String groupId) {

		int sum = 0;

		char[] charArray = groupId.toCharArray();

		for (char c : charArray) {
			sum = sum + Character.getNumericValue(c);
		}

		return sum;
	}

}

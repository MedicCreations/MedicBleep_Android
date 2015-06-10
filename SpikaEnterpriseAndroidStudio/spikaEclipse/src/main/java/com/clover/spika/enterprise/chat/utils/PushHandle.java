package com.clover.spika.enterprise.chat.utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;

import com.clover.spika.enterprise.chat.ChatActivity;
import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.caching.BackgroundChatCaching;
import com.clover.spika.enterprise.chat.caching.robospice.BackgroundChatDataCacheSpice;
import com.clover.spika.enterprise.chat.models.GetBackroundDataResponse;
import com.clover.spika.enterprise.chat.models.greendao.DaoMaster;
import com.clover.spika.enterprise.chat.models.greendao.DaoSession;
import com.clover.spika.enterprise.chat.services.robospice.CustomSpiceListener;
import com.clover.spika.enterprise.chat.services.robospice.CustomSpiceManager;
import com.clover.spika.enterprise.chat.services.robospice.OkHttpService;
import com.octo.android.robospice.SpiceManager;

public class PushHandle {

	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	public static void handlePushNotification(final String chatId, String messageId, String organizationId, String firstName, String chatPassword, String type, Context context) {

		String message = context.getResources().getString(R.string.msg_from) + " " + firstName;

		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		ComponentName componentInfo = null;
		boolean isActive = false;
		
		if(Utils.isBuildOver(Build.VERSION_CODES.KITKAT_WATCH)){
			isActive = isActivePCG(am);
		} else {
			List<RunningTaskInfo> taskInfo = am.getRunningTasks(1);
			componentInfo = taskInfo.get(0).topActivity;
			isActive = componentInfo.getPackageName().equalsIgnoreCase("com.clover.spika.enterprise.chat");
		}

		boolean isScreenOn = true;
		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		if(Utils.isBuildOver(Build.VERSION_CODES.KITKAT)){
			isScreenOn = pm.isInteractive();
		}else{
			isScreenOn = pm.isScreenOn();
		}

		if (isActive && isScreenOn) {

			Intent inBroadcast = new Intent();
			inBroadcast.setAction(Const.PUSH_INTENT_ACTION);
			inBroadcast.putExtra(Const.CHAT_ID, chatId);
			inBroadcast.putExtra(Const.MESSAGE_ID, messageId);
			inBroadcast.putExtra(Const.PUSH_TYPE, type);
			inBroadcast.putExtra(Const.PUSH_MESSAGE, message);
			inBroadcast.putExtra(Const.PASSWORD, chatPassword);

			LocalBroadcastManager.getInstance(context).sendBroadcast(inBroadcast);

		} else {

			if (Integer.valueOf(type) == Const.PUSH_TYPE_SEEN) {
				return;
			}

			NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

			Intent pushIntent = new Intent(context, ChatActivity.class);
			pushIntent.putExtra(Const.CHAT_ID, chatId);
			pushIntent.putExtra(Const.MESSAGE_ID, messageId);
			pushIntent.putExtra(Const.PUSH_TYPE, type);
			pushIntent.putExtra(Const.FROM_NOTIFICATION, true);
			pushIntent.putExtra(Const.PASSWORD, chatPassword);
			pushIntent.putExtra(Const.ORGANIZATION_ID, organizationId);
			pushIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			pushIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

			PendingIntent contentIntent = PendingIntent.getActivity(context, Integer.valueOf(chatId), pushIntent, PendingIntent.FLAG_ONE_SHOT);

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
				notifBuilder.setSmallIcon(R.drawable.ic_stat_notify);
                notifBuilder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher));

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

			mNotificationManager.notify(Integer.valueOf(chatId), notification);

			if(messageId != null){
				updateDBInBackground(context, chatId, messageId);
			}
		}
	}

	private static void updateDBInBackground(Context context, final String chatId, String messageId){
		SQLiteDatabase db;
		DaoMaster daoMaster;
		final DaoSession daoSession;
		final DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(context, Const.DATABASE_NAME_SPIKA, null);
		db = helper.getWritableDatabase();
		daoMaster = new DaoMaster(db);
		daoSession = daoMaster.newSession();

		final SpiceManager spiceManager = new CustomSpiceManager(OkHttpService.class);
		spiceManager.start(context);
        boolean isChatActive = false;
		BackgroundChatDataCacheSpice.GetData spice = new BackgroundChatDataCacheSpice.GetData(daoSession, spiceManager, chatId, messageId, isChatActive,
				new BackgroundChatCaching.OnChatDBChanged() {

					@Override
					public void onChatDBChanged(GetBackroundDataResponse response) {
						helper.close();
						spiceManager.shouldStop();
					}

				});
		spiceManager.execute(spice, new CustomSpiceListener<Integer>());
	}


	public static boolean isActivePCG(ActivityManager am) {
		final Set<String> activePackages = new HashSet<String>();
		final List<ActivityManager.RunningAppProcessInfo> processInfos = am.getRunningAppProcesses();
		for (ActivityManager.RunningAppProcessInfo processInfo : processInfos) {
			if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
				activePackages.addAll(Arrays.asList(processInfo.pkgList));
			}
		}
		String[] activePCG;
		activePCG = activePackages.toArray(new String[activePackages.size()]);
		if (activePCG != null) {
		    for (String activePackage : activePCG) {
		      if (activePackage.equals("com.clover.spika.enterprise.chat")) {
		        return true;
		      }
		    }
		  }
		return false;
	}

}

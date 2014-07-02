package jp.co.vector.chat.gcm;

import jp.co.vector.chat.CharacterListActivity;
import jp.co.vector.chat.ChatActivity;
import jp.co.vector.chat.extendables.BaseActivity;
import jp.co.vector.chat.utils.Const;
import jp.co.vector.chat.utils.Logger;
import jp.co.vector.chat.R;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

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

		String type = "";
		String groupId = "";
		String groupName = "";
		String ownerId = "";

		if (extras.containsKey(Const.MSG_TYPE)) {
		    type = extras.getString(Const.MSG_TYPE);
		}

		if (extras.containsKey(Const.GROUP_ID)) {
		    groupId = extras.getString(Const.GROUP_ID);
		}

		if (extras.containsKey(Const.GROUP_NAME)) {
		    groupName = extras.getString(Const.GROUP_NAME);
		}

		if (extras.containsKey(Const.OWNER_ID)) {
		    ownerId = extras.getString(Const.OWNER_ID);
		}

		sendNotification(type, groupId, groupName, ownerId);
	    }
	}

	GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void sendNotification(final String type, final String groupId, final String groupName, final String ownerId) {

	NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

	if (Integer.parseInt(type) == Const.PT_MESSAGE) {
	    String msg = "You have received a message in group " + groupName;

	    if (ChatActivity.instance != null) {
		ChatActivity.instance.callAfterPush(groupId, msg, Const.PT_MESSAGE);
	    } else if (BaseActivity.instance != null) {
		BaseActivity.instance.showPopUp(msg, groupId, Const.PT_MESSAGE);
	    } else {

		PendingIntent contentIntent;

		Intent intent = new Intent(this, ChatActivity.class);
		intent.putExtra(Const.GROUP_ID, groupId);
		intent.putExtra(Const.OWNER_ID, ownerId);
		intent.putExtra(Const.GROUP_NAME, groupName);
		intent.putExtra(Const.FROM_NOTIFICATION, true);

		contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		// Push with display
		Notification notif = new Notification(R.drawable.ic_launcher, msg, System.currentTimeMillis());
		notif.defaults = Notification.DEFAULT_ALL;
		notif.flags = Notification.FLAG_AUTO_CANCEL;

		CharSequence message = msg;
		notif.setLatestEventInfo(this, Const.VECTOR_CHAT, message, contentIntent);

		mNotificationManager.notify(getIntId(groupId), notif);
		//
	    }
	} else if (Integer.parseInt(type) == Const.PT_REPORT) {
	    String msg = "One of your messages has been reported.";

	    if (BaseActivity.instance != null) {
		if (ChatActivity.instance != null) {
		    ChatActivity.instance.callAfterPush(groupId, msg, Const.PT_REPORT);
		} else {
		    BaseActivity.instance.showPopUp(msg, groupId, Const.PT_REPORT);
		}
	    } else {

		PendingIntent contentIntent;

		Intent intent = new Intent(this, CharacterListActivity.class);

		contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		// Push with display
		Notification notif = new Notification(R.drawable.ic_launcher, msg, System.currentTimeMillis());
		notif.defaults = Notification.DEFAULT_ALL;
		notif.flags = Notification.FLAG_AUTO_CANCEL;

		CharSequence message = msg;
		notif.setLatestEventInfo(this, Const.VECTOR_CHAT, message, contentIntent);

		mNotificationManager.notify(getIntId(groupId), notif);
		//
	    }
	} else if (Integer.parseInt(type) == Const.PT_GROUP_CREATED) {
	    String msg = "New group named " + groupName + " has been created.";

	    if (BaseActivity.instance != null) {
		BaseActivity.instance.showPopUp(msg, groupId, Const.PT_GROUP_CREATED);
	    } else {

		PendingIntent contentIntent;

		Intent intent = new Intent(this, ChatActivity.class);
		intent.putExtra(Const.GROUP_ID, groupId);
		intent.putExtra(Const.OWNER_ID, ownerId);
		intent.putExtra(Const.GROUP_NAME, groupName);
		intent.putExtra(Const.FROM_NOTIFICATION, true);

		contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		// Push with display
		Notification notif = new Notification(R.drawable.ic_launcher, msg, System.currentTimeMillis());
		notif.defaults = Notification.DEFAULT_ALL;
		notif.flags = Notification.FLAG_AUTO_CANCEL;

		CharSequence message = msg;
		notif.setLatestEventInfo(this, Const.VECTOR_CHAT, message, contentIntent);

		mNotificationManager.notify(getIntId(groupId), notif);
		//
	    }
	}
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

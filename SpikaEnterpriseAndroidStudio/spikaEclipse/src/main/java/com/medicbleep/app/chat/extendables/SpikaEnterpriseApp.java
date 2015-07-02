package com.medicbleep.app.chat.extendables;

import java.io.File;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.medicbleep.app.chat.R;
import com.medicbleep.app.chat.security.JNAesCrypto;
import com.medicbleep.app.chat.services.custom.PoolingService;
import com.medicbleep.app.chat.utils.ApplicationStateManager;
import com.medicbleep.app.chat.utils.Const;
import com.medicbleep.app.chat.utils.Preferences;
import com.medicbleep.app.chat.webrtc.socket.SocketService;

public class SpikaEnterpriseApp extends Application {

	private static Context mAppContext;
	private static Preferences mAppPreferences;

	private static Intent socketIntent;

	private static boolean isCallInBackground = false;

	@Override
	public void onCreate() {
		super.onCreate();
		
		JNAesCrypto.isEncryptionEnabled = getResources().getBoolean(R.bool.enable_global_encryption);
		setAppContext(getApplicationContext());

		Intent poolingIntent = new Intent(this, PoolingService.class);
		if (getResources().getBoolean(R.bool.enable_polling)) {
			startService(poolingIntent);
		} else {
			stopService(poolingIntent);
		}

        new ApplicationStateManager(this);
	}
	
	public static void startSocket() {
		if (!mAppContext.getResources().getBoolean(R.bool.enable_web_rtc))
			return;
		if (socketIntent != null)
			return;
		if (isMyServiceRunning(SocketService.class))
			return;
		socketIntent = new Intent(mAppContext, SocketService.class);
		socketIntent.putExtra(Const.IS_APLICATION_OPEN, true);
		mAppContext.startService(socketIntent);
	}

	public static void stopSocket() {
		if (!mAppContext.getResources().getBoolean(R.bool.enable_web_rtc))
			return;
		mAppContext.stopService(new Intent(mAppContext, SocketService.class));
		socketIntent = null;
	}

	public static void stopSocketWithCon(Context c) {
		if (!c.getResources().getBoolean(R.bool.enable_web_rtc))
			return;
		c.stopService(new Intent(c, SocketService.class));
		socketIntent = null;
	}

	public static void restartSocket() {
		if (!mAppContext.getResources().getBoolean(R.bool.enable_web_rtc))
			return;
		mAppContext.stopService(new Intent(mAppContext, SocketService.class));
		if (isMyServiceRunning(SocketService.class)) {
			socketIntent = new Intent(mAppContext, SocketService.class);
			socketIntent.putExtra(Const.IS_APLICATION_OPEN, false);
			mAppContext.startService(socketIntent);
		} else {
			startSocket();
		}
	}

	public static Context getAppContext() {
		return mAppContext;
	}

	public void setAppContext(Context mAppContext) {
		SpikaEnterpriseApp.mAppContext = mAppContext;
	}

	public static Preferences getSharedPreferences() {

		if (mAppPreferences == null) {
			return new Preferences(getAppContext());
		} else {
			return mAppPreferences;
		}
	}

	// Video activity and path variables and methods
	private static boolean mCheckForRestartVideoActivity = false;
	private static String mFilePathForVideo = null;

	public static boolean checkForRestartVideoActivity() {
		return mCheckForRestartVideoActivity;
	}

	public static void setCheckForRestartVideoActivity(boolean check) {
		mCheckForRestartVideoActivity = check;
	}

	public static String videoPath() {
		return mFilePathForVideo;
	}

	public static void setVideoPath(String path) {
		mFilePathForVideo = path;
	}

	// Samsung image path variable and methods
	private static String mSamsungPath = null;

	public static String samsungImagePath() {
		return mSamsungPath;
	}

	public static void setSamsungImagePath(String path) {
		mSamsungPath = path;
	}

	public static void deleteSamsungPathImage() {
		if (mSamsungPath != null && !mSamsungPath.equals("-1")) {
			File f = new File(mSamsungPath);
			if (f.exists())
				f.delete();
		}
		setSamsungImagePath(null);
	}

	private static boolean isMyServiceRunning(Class<?> serviceClass) {
		ActivityManager manager = (ActivityManager) mAppContext.getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (serviceClass.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	public static boolean isCallInBackground() {
		return isCallInBackground;
	}

	public static void setCallInBackground(boolean isInBack) {
		isCallInBackground = isInBack;
	}
}

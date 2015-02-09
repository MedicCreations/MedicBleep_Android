package com.clover.spika.enterprise.chat.extendables;

import java.io.File;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.security.JNAesCrypto;
import com.clover.spika.enterprise.chat.services.custom.PoolingService;
import com.clover.spika.enterprise.chat.utils.Preferences;

public class SpikaEnterpriseApp extends Application {

	private static Context mAppContext;
	private static Preferences mAppPreferences;

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
	}

	public static Context getAppContext() {
		return mAppContext;
	}

	public void setAppContext(Context mAppContext) {
		SpikaEnterpriseApp.mAppContext = mAppContext;
	}

	public static Preferences getSharedPreferences(Context ctx) {

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
}

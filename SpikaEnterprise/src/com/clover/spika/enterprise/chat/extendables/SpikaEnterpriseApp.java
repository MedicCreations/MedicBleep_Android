package com.clover.spika.enterprise.chat.extendables;

import java.io.File;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.security.JNAesCrypto;
import com.clover.spika.enterprise.chat.services.custom.PoolingService;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Preferences;
import com.zzz.test.socket.SocketService;

public class SpikaEnterpriseApp extends Application {

	private static SpikaEnterpriseApp mInstance;
	private static Context mAppContext;
	private boolean mCheckForRestartVideoActivity = false;
	private String mFilePathForVideo = null;
	private String mSamsungPath = null;

	private Intent poolingIntent;
	private Intent socketIntent;
	
	private boolean isCallInBackground = false;

	@Override
	public void onCreate() {
		super.onCreate();

		mInstance = this;
		JNAesCrypto.isEncryptionEnabled = getApplicationContext().getResources().getBoolean(R.bool.enable_global_encryption);

		this.setAppContext(getApplicationContext());

		poolingIntent = new Intent(this, PoolingService.class);
		if (getResources().getBoolean(R.bool.enable_polling)) {
			startService(poolingIntent);
		} else {
			stopService(poolingIntent);
		}
		
	}
	
	public void startSocket(){
		if(socketIntent != null) return;
		if(isMyServiceRunning(SocketService.class)) return;
		socketIntent = new Intent(this, SocketService.class);
		socketIntent.putExtra(Const.IS_APLICATION_OPEN, true);
		startService(socketIntent);
	}
	
	public void stopSocket() {
		stopService(new Intent(this, SocketService.class));
		socketIntent = null;
	}
	
	public static Preferences getSharedPreferences(Context ctx) {
		return new Preferences(ctx);
	}

	public static SpikaEnterpriseApp getInstance() {
		return mInstance;
	}

	public static Context getAppContext() {
		return mAppContext;
	}

	public void setAppContext(Context mAppContext) {
		SpikaEnterpriseApp.mAppContext = mAppContext;
	}

	public boolean checkForRestartVideoActivity() {
		return mCheckForRestartVideoActivity;
	}

	public void setCheckForRestartVideoActivity(boolean check) {
		mCheckForRestartVideoActivity = check;
	}

	public String videoPath() {
		return mFilePathForVideo;
	}

	public void setVideoPath(String path) {
		mFilePathForVideo = path;
	}

	public String samsungImagePath() {
		return mSamsungPath;
	}

	public void setSamsungImagePath(String path) {
		mSamsungPath = path;
	}

	public void deleteSamsungPathImage() {
		if (mSamsungPath != null && !mSamsungPath.equals("-1")) {
			File f = new File(mSamsungPath);
			if (f.exists())
				f.delete();
		}
		setSamsungImagePath(null);
	}
	
	private boolean isMyServiceRunning(Class<?> serviceClass) {
	    ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if (serviceClass.getName().equals(service.service.getClassName())) {
	            return true;
	        }
	    }
	    return false;
	}
	
	public boolean isCallInBackground(){
		return isCallInBackground;
	}
	
	public void setCallInBackground(boolean isInBack){
		isCallInBackground = isInBack;
	}

}

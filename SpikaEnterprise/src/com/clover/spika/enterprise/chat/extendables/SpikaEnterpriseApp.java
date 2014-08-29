package com.clover.spika.enterprise.chat.extendables;

import com.clover.spika.enterprise.chat.utils.Preferences;

import android.app.Application;
import android.content.Context;

public class SpikaEnterpriseApp extends Application {

	private static SpikaEnterpriseApp mInstance;
	private static Context mAppContext;

	@Override
	public void onCreate() {
		super.onCreate();

		mInstance = this;

		this.setAppContext(getApplicationContext());
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

}

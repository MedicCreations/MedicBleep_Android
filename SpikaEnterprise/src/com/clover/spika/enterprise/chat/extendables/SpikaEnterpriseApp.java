package com.clover.spika.enterprise.chat.extendables;

import com.clover.spika.enterprise.chat.utils.Preferences;

import android.app.Application;
import android.content.Context;

public class SpikaEnterpriseApp extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
	}

	public static Preferences getSharedPreferences(Context ctx) {
		return new Preferences(ctx);
	}

}

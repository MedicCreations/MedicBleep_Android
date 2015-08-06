package com.medicbleep.app.chat.services.robospice;

import roboguice.util.temp.Ln;
import android.util.Log;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.SpiceService;

public class CustomSpiceManager extends SpiceManager {

	public CustomSpiceManager(Class<? extends SpiceService> spiceServiceClass) {
		super(spiceServiceClass);
		Ln.getConfig().setLoggingLevel(Log.ERROR);
	}
	
}

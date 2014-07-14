package com.clover.spika.enterprise.chat.utils;

import android.util.Log;

public class Logger {

    public static void custom(String tag, String message) {

	if (!Const.IS_DEBUG) {
	    return;
	}

	int maxLogSize = 1000;
	for (int i = 0; i <= message.length() / maxLogSize; i++) {

	    int start = i * maxLogSize;
	    int end = (i + 1) * maxLogSize;

	    end = end > message.length() ? message.length() : end;

	    Log.d(tag, message.substring(start, end));
	}
    }

    public static void debug(String message) {

	if (!Const.IS_DEBUG) {
	    return;
	}

	Log.d(Const.DEBUG, message);
    }

    public static void info(String message) {

	if (!Const.IS_DEBUG) {
	    return;
	}

	Log.i(Const.INFO, message);
    }

    public static void verbose(String type, String message) {

	if (!Const.IS_DEBUG) {
	    return;
	}

	Log.i(Const.VERBOSE + type, message);
    }

    public static void error(String message) {

	if (!Const.IS_DEBUG) {
	    return;
	}

	Log.e(Const.ERROR, message);
    }
}
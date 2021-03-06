package com.medicbleep.app.chat.utils;

import android.util.Log;

import com.squareup.okhttp.Request;

import java.io.IOException;

import okio.Buffer;

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
	
	public static void custom(String type, String tag, String message) {

		if (!Const.IS_DEBUG) {
			return;
		}

		int maxLogSize = 1000;
		for (int i = 0; i <= message.length() / maxLogSize; i++) {

			int start = i * maxLogSize;
			int end = (i + 1) * maxLogSize;

			end = end > message.length() ? message.length() : end;
			if(type.equals("e")){
				Log.e(tag, message.substring(start, end));
			} else if(type.equals("w")){
				Log.w(tag, message.substring(start, end));
			} else if(type.equals("i")){
				Log.i(tag, message.substring(start, end));
			} else if(type.equals("v")){
				Log.v(tag, message.substring(start, end));
			} else {
				Log.d(tag, message.substring(start, end));
			}
		}
	}

    public static void logRequest(String i, String log, Request.Builder requestBuilder){
        try {
            final Request copy = requestBuilder.build();
            final Buffer buffer = new Buffer();
            copy.body().writeTo(buffer);
            Logger.custom(i, log, "REQ: " + buffer.readUtf8());

        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

	public static void d(String message) {

		if (!Const.IS_DEBUG) {
			return;
		}

		Log.d(Const.DEBUG, message);
	}

	public static void i(String message) {

		if (!Const.IS_DEBUG) {
			return;
		}

		Log.i(Const.INFO, message);
	}

	public static void v(String type, String message) {

		if (!Const.IS_DEBUG) {
			return;
		}

		Log.v(Const.VERBOSE + type, message);
	}

	public static void e(String message) {

		if (!Const.IS_DEBUG) {
			return;
		}

		Log.e(Const.ERROR, message);
	}
}
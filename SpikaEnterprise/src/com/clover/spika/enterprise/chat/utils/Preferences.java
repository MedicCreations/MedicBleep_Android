package com.clover.spika.enterprise.chat.utils;

import java.io.IOException;
import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Preferences {

	private SharedPreferences sharedPreferences;
	private SharedPreferences passcodePreferences;

	public Preferences(Context context) {
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		passcodePreferences = context.getSharedPreferences("passcode", Context.MODE_PRIVATE);
	}

	public String getCustomString(String key) {
		return sharedPreferences.getString(key, "");
	}

	public void setCustomString(String key, String value) {
		SharedPreferences.Editor editor = sharedPreferences.edit();

		editor.putString(key, value);

		editor.apply();
	}

	public void setUserTokenId(String token) {
		SharedPreferences.Editor editor = sharedPreferences.edit();

		long tokenExpires = (System.currentTimeMillis() / 1000L) + Const.DAY;

		editor.putLong(Const.CLIENT_TOKEN_EXPIRES, tokenExpires);
		editor.putString(Const.TOKEN, token);

		editor.apply();
	}

	public String getToken() throws ClientProtocolException, IOException, JSONException {
		long tokenTime = sharedPreferences.getLong(Const.CLIENT_TOKEN_EXPIRES, 0L);
		long currentTime = System.currentTimeMillis() / 1000L;

		return (tokenTime > currentTime) ? sharedPreferences.getString(Const.TOKEN, null) : null;
	}

	public void setCustomBoolean(String key, boolean value) {
		SharedPreferences.Editor editor = sharedPreferences.edit();

		editor.putBoolean(key, value);

		editor.apply();
	}

	public boolean getCustomBoolean(String key) {
		return sharedPreferences.getBoolean(key, false);
	}

	public void setCustomInt(String key, int value) {
		SharedPreferences.Editor editor = sharedPreferences.edit();

		editor.putInt(key, value);

		editor.apply();
	}

	public int getCustomInt(String key) {
		return sharedPreferences.getInt(key, -1);
	}

	public void removePreference(String key) {
		SharedPreferences.Editor sharedEditor = sharedPreferences.edit();
		sharedEditor.remove(key);
		sharedEditor.apply();

		SharedPreferences.Editor passcodeEditor = sharedPreferences.edit();
		passcodeEditor.remove(key);
		passcodeEditor.apply();
	}

	public void setPasscodeEnabled(boolean enabled) {
		SharedPreferences.Editor editor = passcodePreferences.edit();
		editor.putBoolean(Const.PREFERENCES_IS_PASSCODE_ENABLED, enabled);
		editor.apply();
	}

	public boolean isPasscodeEnabled() {
		return passcodePreferences.getBoolean(Const.PREFERENCES_IS_PASSCODE_ENABLED, false);
	}

	public void setPasscode(String passcode) {
		SharedPreferences.Editor editor = passcodePreferences.edit();
		editor.putString(Const.PREFERENCES_STORED_PASSCODE, passcode);
		editor.apply();
	}

	public String getPasscode() {
		return passcodePreferences.getString(Const.PREFERENCES_STORED_PASSCODE, "");
	}

	public void clear() {
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.clear();
		editor.apply();
	}

}
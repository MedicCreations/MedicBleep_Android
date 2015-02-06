package com.clover.spika.enterprise.chat.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class Preferences {

	private SharedPreferences spikaSharedPreferences;

	public Preferences(Context context) {
		spikaSharedPreferences = context.getSharedPreferences("SpikaSharedPreferences", Context.MODE_PRIVATE);
	}

	public String getCustomString(String key) {
		return spikaSharedPreferences.getString(key, "");
	}

	public void setCustomString(String key, String value) {
		SharedPreferences.Editor editor = spikaSharedPreferences.edit();

		editor.putString(key, value);

		editor.apply();
	}

	public void setUserTokenId(String token) {
		SharedPreferences.Editor editor = spikaSharedPreferences.edit();

		long tokenExpires = (System.currentTimeMillis() / 1000L) + Const.DAY;

		editor.putLong(Const.CLIENT_TOKEN_EXPIRES, tokenExpires);
		editor.putString(Const.TOKEN, token);

		editor.apply();
	}

	public String getToken() {
		long tokenTime = spikaSharedPreferences.getLong(Const.CLIENT_TOKEN_EXPIRES, 0L);
		long currentTime = System.currentTimeMillis() / 1000L;

		return (tokenTime > currentTime) ? spikaSharedPreferences.getString(Const.TOKEN, null) : null;
	}

	public void setCustomBoolean(String key, boolean value) {
		SharedPreferences.Editor editor = spikaSharedPreferences.edit();

		editor.putBoolean(key, value);

		editor.apply();
	}

	public boolean getCustomBoolean(String key) {
		return spikaSharedPreferences.getBoolean(key, false);
	}

	public void setCustomInt(String key, int value) {
		SharedPreferences.Editor editor = spikaSharedPreferences.edit();

		editor.putInt(key, value);

		editor.apply();
	}

	public int getCustomInt(String key) {
		return spikaSharedPreferences.getInt(key, -1);
	}

	public void removePreference(String key) {
		SharedPreferences.Editor sharedEditor = spikaSharedPreferences.edit();
		sharedEditor.remove(key);
		sharedEditor.apply();

		SharedPreferences.Editor passcodeEditor = spikaSharedPreferences.edit();
		passcodeEditor.remove(key);
		passcodeEditor.apply();
	}

	public void setPasscodeEnabled(boolean enabled) {
		SharedPreferences.Editor editor = spikaSharedPreferences.edit();
		editor.putBoolean(Const.PREFERENCES_IS_PASSCODE_ENABLED, enabled);
		editor.apply();
	}

	public boolean isPasscodeEnabled() {
		return spikaSharedPreferences.getBoolean(Const.PREFERENCES_IS_PASSCODE_ENABLED, false);
	}

	public void setPasscode(String passcode) {
		SharedPreferences.Editor editor = spikaSharedPreferences.edit();
		editor.putString(Const.PREFERENCES_STORED_PASSCODE, passcode);
		editor.apply();
	}

	public String getPasscode() {
		return spikaSharedPreferences.getString(Const.PREFERENCES_STORED_PASSCODE, "");
	}

	public void clear() {
		SharedPreferences.Editor editor = spikaSharedPreferences.edit();
		editor.clear();
		editor.apply();
	}

}
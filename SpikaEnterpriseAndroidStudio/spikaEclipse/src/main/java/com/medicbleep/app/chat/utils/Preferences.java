package com.medicbleep.app.chat.utils;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.medicbleep.app.chat.security.JNAesCrypto;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

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

	public String getToken() {
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
	}

	public void setPasscode(String passcode) {
		setEncryptedString(Const.PREFERENCES_STORED_PASSCODE, passcode, passcodePreferences);
	}

	public String getPasscode() {
		return getEncryptedString(Const.PREFERENCES_STORED_PASSCODE, passcodePreferences);
	}

	public void clear() {
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.clear();
		editor.apply();
	}

	public String getEncryptedString(String key) {
		return getEncryptedString(key, sharedPreferences); 
	}
	
	public String getEncryptedString(String key, SharedPreferences preferences) {
		String encrypted = preferences.getString(md5(key), "");
		String decrypted;
		if(TextUtils.isEmpty(encrypted)) {
			return "";
		}
		try {
			decrypted = JNAesCrypto.decryptJN(encrypted);
		} catch (Exception e) {
			e.printStackTrace();
			decrypted = "";
		}
		return decrypted;
	}

	public void setEncryptedString(String key, String value) {
		setEncryptedString(key, value, sharedPreferences);
	}
	
	public void setEncryptedString(String key, String value, SharedPreferences preferences) {
		if ((value == null) || (value.length() == 0)) {
			removeEncryptedPreference(key, preferences);
			return;
		}
		SharedPreferences.Editor editor = preferences.edit();
		String encrypted;
		try {
			encrypted = JNAesCrypto.encryptJN(value);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		editor.putString(md5(key), encrypted);
		editor.apply();
	}
	
	public void removeEncryptedPreference(String key) {
		removeEncryptedPreference(key, sharedPreferences);
	}
	
	public void removeEncryptedPreference(String key, SharedPreferences preferences) {
		SharedPreferences.Editor sharedEditor = preferences.edit();
		sharedEditor.remove(md5(key));
		sharedEditor.apply();
	}
	
	public static String md5(String s) {
	    MessageDigest digest;
	    try {
	        digest = MessageDigest.getInstance("MD5");
	        digest.update(s.getBytes(),0,s.length());
	        String hash = new BigInteger(1, digest.digest()).toString(16);
	        return hash;
	    } 
	    catch (NoSuchAlgorithmException e) {
	        e.printStackTrace();
	    }
	    return "";
	}
}
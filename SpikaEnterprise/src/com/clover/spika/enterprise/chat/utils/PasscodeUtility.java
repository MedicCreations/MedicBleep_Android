package com.clover.spika.enterprise.chat.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.clover.spika.enterprise.chat.extendables.SpikaEnterpriseApp;

public class PasscodeUtility {

	private static PasscodeUtility sInstance = new PasscodeUtility();

	public static PasscodeUtility getInstance() {
		return sInstance;
	}

	private PasscodeUtility() {
	}

	private boolean isSessionValid = false;
	private boolean isInApp = false;

	// private int handlerTimeToLive = 3 * 1000;
	private int handlerTimeToLive = 500;
	private Handler mValidationSessionHandler = new Handler();

	private String passcode;
	private String temporaryPasscode;

	public boolean isInApp() {
		return this.isInApp;
	}

	/**
	 * Checks if the passcode is enabled for the application.
	 * 
	 * @param context
	 *            context of the activity (or application context)
	 * @return true if passcode has been enabled
	 */
	public boolean isPasscodeEnabled(Context ctx) {
		return SpikaEnterpriseApp.getSharedPreferences().isPasscodeEnabled();
	}

	/**
	 * Sets the boolean value which is checked for passcode availability
	 * 
	 * @param context
	 *            context of the activity (or application context)
	 * @param isPasscodeEnabled
	 *            sets the internal variable which is later checked for passcode
	 *            availability
	 */
	public void setPasscodeEnabled(Context ctx, boolean isPasscodeEnabled) {
		if (Looper.myLooper() != Looper.getMainLooper()) {
			throw new IllegalAccessError("You can only set new values on main thread!");
		}

		SpikaEnterpriseApp.getSharedPreferences().setPasscodeEnabled(isPasscodeEnabled);
	}

	/**
	 * @return true if current session is valid. Current session is valid only
	 *         if passcode was entered correctly in the near past.
	 */
	public boolean isSessionValid() {
		Logger.d("isSessionValid: " + isSessionValid);
		return isSessionValid;
	}

	/**
	 * Sets the internal field which is checked for valid session. This method
	 * should only be called from
	 * {@link com.clover.spika.enterprise.chat.MainActivity}!
	 * 
	 * @param isSessionValid
	 *            set by {@link com.clover.spika.enterprise.chat.MainActivity}
	 */
	public void setSessionValid(boolean isSessionValid) {
		if (Looper.myLooper() != Looper.getMainLooper()) {
			throw new IllegalAccessError("You can only set new values on main thread!");
		}

		Logger.d("setSessionValid: " + isSessionValid);
		this.isSessionValid = isSessionValid;
	}

	public boolean validate(Context ctx, String requestedPasscode) {
		
		this.passcode = SpikaEnterpriseApp.getSharedPreferences().getPasscode();

		// if, by any chance, passcode length is wrong, react as if entered
		// passcode is false and thus not validated
		if (this.passcode.length() != 4)
			return false;

		return this.passcode.equals(requestedPasscode);
	}

	public void setPasscode(Context ctx, String requestedPasscode) {
		if (TextUtils.isEmpty(requestedPasscode)) {
			SpikaEnterpriseApp.getSharedPreferences().removePreference(Const.PREFERENCES_STORED_PASSCODE);
			return;
		}

		if (requestedPasscode.length() == 4) {
			SpikaEnterpriseApp.getSharedPreferences().setPasscode(requestedPasscode);
		}
	}

	/**
	 * @return temporary passcode or empty String if none is set. Temporary
	 *         passcode should be stored for a short time only.
	 */
	public String getTemporaryPasscode() {
		return temporaryPasscode == null ? "" : temporaryPasscode;
	}

	public void setTemporaryPasscode(String tempPasscode) {
		if (Looper.myLooper() != Looper.getMainLooper()) {
			throw new IllegalAccessError("You can only set new values on main thread!");
		}

		this.temporaryPasscode = tempPasscode;
	}

	/**
	 * Starts a handler with short time-to-live set by internal property. When
	 * handler expires, session is automatically considered as expired. <br />
	 * <br />
	 * NOTE: Should be called from BaseActivity only.
	 */
	public void onPause() {
		mValidationSessionHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				PasscodeUtility.getInstance().setSessionValid(false);
				isInApp = false;
			}
		}, handlerTimeToLive);
	}

	/**
	 * Kills a handler waiting for expired session. <br />
	 * <br />
	 * NOTE: Should be called from BaseActivity only.
	 */
	public void onResume() {
		mValidationSessionHandler.removeCallbacksAndMessages(null);
		isInApp = true;
	}

}

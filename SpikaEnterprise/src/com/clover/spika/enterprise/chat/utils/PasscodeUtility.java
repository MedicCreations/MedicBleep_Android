package com.clover.spika.enterprise.chat.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.clover.spika.enterprise.chat.extendables.SpikaEnterpriseApp;

public class PasscodeUtility {
    
    private static PasscodeUtility sInstance = new PasscodeUtility();

    public static PasscodeUtility getInstance() {
        return sInstance;
    }

    private PasscodeUtility() { }

    private boolean isSessionValid = false;

    private int handlerTimeToLive = 3 * 1000;
    private Handler mValidationSessionHandler = new Handler();

    private String passcode;
    private String temporaryPasscode;

    /**
     * Checks if the passcode is enabled for the application.
     * @param context context of the activity (or application context)
     * @return true if passcode has been enabled
     */
    public boolean isPasscodeEnabled(Context context) {
        return SpikaEnterpriseApp.getSharedPreferences(context).getCustomBoolean(Const.PREFERENCES_IS_PASSCODE_ENABLED);
    }

    /**
     * Sets the boolean value which is checked for passcode availability
     * @param context context of the activity (or application context)
     * @param isPasscodeEnabled sets the internal variable which is later checked for passcode availability
     */
    public void setPasscodeEnabled(Context context, boolean isPasscodeEnabled) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new IllegalAccessError("You can only set new values on main thread!");
        }

        SpikaEnterpriseApp.getSharedPreferences(context)
                .setCustomBoolean(Const.PREFERENCES_IS_PASSCODE_ENABLED, isPasscodeEnabled);
    }

    /**
     * @return true if current session is valid. Current session is valid only if passcode was entered
     * correctly in the near past.
     */
    public boolean isSessionValid() {
        Logger.debug("isSessionValid: " + isSessionValid);
        return isSessionValid;
    }

    /**
     * Sets the internal field which is checked for valid session. This method should only be
     * called from {@link com.clover.spika.enterprise.chat.extendables.BaseActivity}!
     * @param isSessionValid set by {@link com.clover.spika.enterprise.chat.extendables.BaseActivity}
     */
    public void setSessionValid(boolean isSessionValid) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new IllegalAccessError("You can only set new values on main thread!");
        }

        Logger.debug("setSessionValid: " + isSessionValid);
        this.isSessionValid = isSessionValid;
    }

    public boolean validate(Context context, String requestedPasscode) {
        if (this.passcode == null) {
            this.passcode = SpikaEnterpriseApp.getSharedPreferences(context).getCustomString(Const.PREFERENCES_STORED_PASSCODE);

            // if, by any chance, passcode length is wrong, react as if entered passcode is false and thus not validated
            if (this.passcode.length() == 4) return false;
        }

        return this.passcode.equals(requestedPasscode);
    }

    public void setPasscode(Context context, String requestedPasscode) {
        if (requestedPasscode.length() == 4) {
            SpikaEnterpriseApp.getSharedPreferences(context).setCustomString(Const.PREFERENCES_STORED_PASSCODE, requestedPasscode);
        }
    }

    /**
     * Starts a handler with short time-to-live set by internal property.
     * When handler expires, session is automatically considered as expired.
     * <br /> <br />
     * NOTE: Should be called from BaseActivity only.
     */
    public void onPause() {
        mValidationSessionHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                PasscodeUtility.getInstance().setSessionValid(false);
            }
        }, handlerTimeToLive);
    }

    /**
     * Kills a handler waiting for expired session.
     * <br /> <br />
     * NOTE: Should be called from BaseActivity only.
     */
    public void onResume() {
        mValidationSessionHandler.removeCallbacksAndMessages(null);
    }

}

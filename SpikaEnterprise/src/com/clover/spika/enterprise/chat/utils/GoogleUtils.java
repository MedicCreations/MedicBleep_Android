package com.clover.spika.enterprise.chat.utils;

import java.io.IOException;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;

import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.api.ApiCallback;
import com.clover.spika.enterprise.chat.api.UserApi;
import com.clover.spika.enterprise.chat.dialogs.AppDialog;
import com.clover.spika.enterprise.chat.extendables.BaseAsyncTask;
import com.clover.spika.enterprise.chat.extendables.BaseModel;
import com.clover.spika.enterprise.chat.extendables.SpikaEnterpriseApp;
import com.clover.spika.enterprise.chat.models.Result;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class GoogleUtils {

	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

	private GoogleCloudMessaging gcm;

	public String getPushToken(Context ctx) {

		String regId = "";

		if (checkPlayServices(ctx)) {

			if (!ctx.getResources().getBoolean(R.bool.enable_polling)) {
				registerInBackground(ctx);
			}

			regId = getRegistrationId(ctx);
			Logger.i("PUSH_TOKEN: " + regId);

			return regId;
		} else {
			Logger.i("Google Play Services are missing");
			return null;
		}
	}

	/**
	 * Registers the application with GCM servers asynchronously.
	 * <p>
	 * Stores the registration ID and app versionCode in the application's
	 * shared preferences.
	 */
	private void registerInBackground(Context ctx) {
		new BaseAsyncTask<Void, Void, String>(ctx, false) {

			protected String doInBackground(Void... params) {

				String msg = "";

				try {
					if (gcm == null) {
						gcm = GoogleCloudMessaging.getInstance(context);
					}

					String regId = gcm.register(Const.GCM_SENDER_ID);
					msg = "Device registered, registration ID=" + regId;

					// if (regId != null &&
					// !regId.equals(getRegistrationId(context))) {
					storeRegistrationId(context, regId);
					Logger.i("NEW PUSH_TOKEN: " + regId);
					// }
				} catch (IOException ex) {
					msg = "Error :" + ex.getMessage();
					ex.printStackTrace();
					// If there is an error, don't just keep trying to register.
					// Require the user to click a button again, or perform
					// exponential back-off.
				}

				return msg;
			};
		}.execute();
	}

	/**
	 * Stores the registration ID and app versionCode in the application's
	 * {@code SharedPreferences}.
	 * 
	 * @param context
	 *            application's context.
	 * @param regId
	 *            registration ID
	 */
	private void storeRegistrationId(Context ctx, String regId) {
		Helper.updateAppVersion(ctx);
		SpikaEnterpriseApp.getSharedPreferences(ctx).setCustomString(Const.PUSH_TOKEN_LOCAL, regId);
		new UserApi().updateUserToken(ctx, new ApiCallback<BaseModel>() {

			@Override
			public void onApiResponse(Result<BaseModel> result) {
				if (result.isSuccess()) {
					Logger.i("NEW PUSH_TOKEN: DONE");
				} else {
					Logger.i("NEW PUSH_TOKEN: FAILED");
				}
			}
		});
	}

	/**
	 * Check the device to make sure it has the Google Play Services APK. If it
	 * doesn't, display a dialog that allows users to download the APK from the
	 * Google Play Store or enable it in the device's system settings.
	 */
	public boolean checkPlayServices(Context ctx) {
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(ctx);
		if (resultCode != ConnectionResult.SUCCESS) {

			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				GooglePlayServicesUtil.getErrorDialog(resultCode, (Activity) ctx, PLAY_SERVICES_RESOLUTION_REQUEST).show();
			} else {
				AppDialog dialog = new AppDialog(ctx, false);
				dialog.setInfo(ctx.getResources().getString(R.string.e_google_play_error));
			}

			return false;
		}

		return true;
	}

	/**
	 * Gets the current registration ID for application on GCM service.
	 * 
	 * If result is empty, the app needs to register.
	 * 
	 * @return registration ID, or empty string if there is no existing
	 *         registration ID.
	 */
	public String getRegistrationId(Context context) {

		String registrationId = SpikaEnterpriseApp.getSharedPreferences(context).getCustomString(Const.PUSH_TOKEN_LOCAL);

		if (registrationId == null || registrationId.isEmpty()) {
			Logger.i("GCM registration ID not found");
			return "";
		}

		// Check if app was updated; if so, it must clear the registration ID
		// since the existing regID is not guaranteed to work with the new
		// app version.
		if (Helper.isUpdated(context)) {
			Logger.i("App has been updated, we need to register GCM again.");
			return "";
		}

		return registrationId;
	}

	public boolean checkGooglePlayServicesForUpdate(Context ctx) {

		int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(ctx);

		if (status == ConnectionResult.SUCCESS) {
			return false;
		} else {

			Dialog d = GooglePlayServicesUtil.getErrorDialog(status, (Activity) ctx, 1337);
			d.setCancelable(false);
			d.show();

			return true;
		}
	}

}

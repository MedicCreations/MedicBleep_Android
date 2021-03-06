package com.medicbleep.app.chat.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;

import com.medicbleep.app.chat.R;
import com.medicbleep.app.chat.api.ApiCallback;
import com.medicbleep.app.chat.api.UserApi;
import com.medicbleep.app.chat.dialogs.AppDialog;
import com.medicbleep.app.chat.extendables.BaseAsyncTask;
import com.medicbleep.app.chat.extendables.BaseModel;
import com.medicbleep.app.chat.extendables.SpikaEnterpriseApp;
import com.medicbleep.app.chat.models.Result;
import com.google.android.gcm.GCMRegistrar;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class GoogleUtils {

	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

	public String getPushToken(Context ctx) {

		String regId = "";

		if (!ctx.getResources().getBoolean(R.bool.enable_polling)) {
			registerInBackground(ctx);
		} else {
			storeRegistrationId(ctx, "");
		}

		regId = getRegistrationId(ctx);
		Logger.i("PUSH_TOKEN: " + regId);

		return regId;
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

				GCMRegistrar.checkDevice(context);
				GCMRegistrar.checkManifest(context);
				GCMRegistrar.register(context, Const.GCM_SENDER_ID);
				
				String regId = GCMRegistrar.getRegistrationId(context);
				msg = "Device registered, registration ID=" + regId;

				storeRegistrationId(context, regId);
				Logger.i("NEW PUSH_TOKEN: " + regId);

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
	public void storeRegistrationId(Context ctx, String regId) {
		Helper.updateAppVersion(ctx);
		SpikaEnterpriseApp.getSharedPreferences().setCustomString(Const.PUSH_TOKEN_LOCAL, regId);
		
		
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
	public String getRegistrationId(Context ctx) {

		String registrationId = SpikaEnterpriseApp.getSharedPreferences().getCustomString(Const.PUSH_TOKEN_LOCAL);

		if (registrationId == null || registrationId.isEmpty()) {
			Logger.i("GCM registration ID not found");
			return "";
		}

		// Check if app was updated; if so, it must clear the registration ID
		// since the existing regID is not guaranteed to work with the new
		// app version.
		if (Helper.isUpdated(ctx)) {
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

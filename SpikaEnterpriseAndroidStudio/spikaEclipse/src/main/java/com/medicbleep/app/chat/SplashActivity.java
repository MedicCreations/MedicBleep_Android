package com.medicbleep.app.chat;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.medicbleep.app.chat.dialogs.AppDialog;
import com.medicbleep.app.chat.extendables.LoginBaseActivity;
import com.medicbleep.app.chat.extendables.SpikaEnterpriseApp;
import com.medicbleep.app.chat.utils.Const;
import com.medicbleep.app.chat.utils.Helper;
import com.medicbleep.app.chat.utils.LocationUtility;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.medicbleep.app.chat.utils.Logger;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

public class SplashActivity extends LoginBaseActivity {

	static final int REQUEST_CODE_RECOVER_PLAY_SERVICES = 1234567890;

	Bundle extras;
	boolean goToLogin;

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);

		int errorCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

		if (errorCode == ConnectionResult.SUCCESS) {
			Log.e("PlayServicesAvailable", "SUCCESS");
			LocationUtility.createInstance(this);
		}
		else {
			GooglePlayServicesUtil.getErrorDialog(errorCode, this, REQUEST_CODE_RECOVER_PLAY_SERVICES).show();
			Log.e("PlayServicesAvailable", "FAIL");
			return;
		}

		if (SpikaEnterpriseApp.getSharedPreferences().getCustomBoolean(Const.REMEMBER_CREDENTIALS)) {
			goToLogin = false;
		} else {
			goToLogin = true;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0) {
			getIntent().putExtra("medic_bleep_outside_start", false);
		}
		extras = getIntent().getExtras();

		IntentFilter intentFilter = new IntentFilter(LocationUtility.COUNTRY_CODE_UPDATED);
		intentFilter.addAction(LocationUtility.LOCATION_SETTINGS_ERROR);
		LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiverImplementation, intentFilter);
		Log.i("Broadcast", "Broadcast receiver set up: " + broadcastReceiverImplementation);
		continueToNextScreen();
	}

	@Override
	protected void onPause() {
		super.onPause();
		LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiverImplementation);
	}

	private void login() {

		try {
			
			executePreLoginApi(Helper.getUsername(), Helper.getPassword(), extras, false);
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void startActivity(Intent intent) {
		super.startActivity(intent);
		overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
	}

	BroadcastReceiver broadcastReceiverImplementation = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(LocationUtility.COUNTRY_CODE_UPDATED)) {
				continueToNextScreen();
			}
			else if (intent.getAction().equals(LocationUtility.LOCATION_SETTINGS_ERROR)) {
				showLocationSettings();
			}
		}
	};

	void continueToNextScreen () {
		if (LocationUtility.getInstance() == null) {
			return;
		}
		if (!TextUtils.isEmpty(LocationUtility.getInstance().getCountryCode())) {
			if (goToLogin) {
				Intent i = new Intent(SplashActivity.this, LoginActivity.class);
				if (extras != null) {
					i.putExtras(extras);
				}
				startActivity(i);
				finish();
			}
			else {
				login();
			}
		}
	}

	boolean dialogExists = false;
	protected void showLocationSettings () {
		if (dialogExists) return;
		dialogExists = true;
		final AppDialog dialog = new AppDialog(SplashActivity.this, true);
		dialog.setYesNo(getString(R.string.location_services_turned_off_go_to_settings), getString(R.string.yes), getString(R.string.no));
		dialog.setOnPositiveButtonClick(new AppDialog.OnPositiveButtonClickListener() {
			@Override
			public void onPositiveButtonClick(View v, Dialog d) {
				dialogExists = false;
				d.dismiss();
				Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			}
		});
		dialog.setOnNegativeButtonClick(new AppDialog.OnNegativeButtonCLickListener() {
			@Override
			public void onNegativeButtonClick(View v, Dialog d) {
				dialogExists = false;
				d.dismiss();
				finish();
			}
		});
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case REQUEST_CODE_RECOVER_PLAY_SERVICES:
				if (resultCode == RESULT_CANCELED) {
					Toast.makeText(this, "Google Play Services must be installed.",
							Toast.LENGTH_SHORT).show();
					finish();
				}
				else if (resultCode == RESULT_OK) {
					Intent i = getIntent();
					finish();
					startActivity(i);
				}
				return;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
}

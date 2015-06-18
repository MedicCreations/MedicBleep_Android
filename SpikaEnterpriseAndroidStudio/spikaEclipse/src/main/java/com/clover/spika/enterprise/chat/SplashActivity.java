package com.clover.spika.enterprise.chat;

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

import com.clover.spika.enterprise.chat.dialogs.AppDialog;
import com.clover.spika.enterprise.chat.extendables.LoginBaseActivity;
import com.clover.spika.enterprise.chat.extendables.SpikaEnterpriseApp;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Helper;
import com.clover.spika.enterprise.chat.utils.LocationUtility;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

public class SplashActivity extends LoginBaseActivity {

	Bundle extras;
	boolean goToLogin;

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);

		if (SpikaEnterpriseApp.getSharedPreferences().getCustomBoolean(Const.REMEMBER_CREDENTIALS)) {
			goToLogin = false;
		} else {
			goToLogin = true;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
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
}

package com.clover.spika.enterprise.chat;

import android.content.Intent;
import android.os.Bundle;

import com.clover.spika.enterprise.chat.extendables.BaseAsyncTask;
import com.clover.spika.enterprise.chat.extendables.LoginBaseActivity;
import com.clover.spika.enterprise.chat.extendables.SpikaEnterpriseApp;
import com.clover.spika.enterprise.chat.utils.Const;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

public class SplashActivity extends LoginBaseActivity {

	Bundle extras;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);

		if (SpikaEnterpriseApp.getSharedPreferences(this).getCustomBoolean(Const.REMEMBER_CREDENTIALS)) {
			pause(0, false);
		} else {
			pause(750, true);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		extras = getIntent().getExtras();
	}
	
	private void pause(final int time, final boolean toLogin) {
		new BaseAsyncTask<Void, Void, Void>(this, false) {

			boolean goToLogin = toLogin;

			@Override
			protected Void doInBackground(Void... params) {

				try {
					Thread.sleep(time);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				if (goToLogin) {

					Intent intent = new Intent(context, LoginActivity.class);

					if (extras != null) {
						intent.putExtras(extras);
					}

					startActivity(intent);
					finish();

					return;
				}

				login();

			}
		}.execute();
	}

	private void login() {

		try {
			
			executePreLoginApi(SpikaEnterpriseApp.getSharedPreferences(this).getCustomString(Const.USERNAME),
					SpikaEnterpriseApp.getSharedPreferences(this).getCustomString(Const.PASSWORD), extras, false);
			
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

}

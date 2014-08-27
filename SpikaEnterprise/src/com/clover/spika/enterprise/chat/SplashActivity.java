package com.clover.spika.enterprise.chat;

import android.content.Intent;
import android.os.Bundle;

import com.clover.spika.enterprise.chat.extendables.BaseAsyncTask;
import com.clover.spika.enterprise.chat.extendables.LoginBaseActivity;
import com.clover.spika.enterprise.chat.extendables.SpikaEnterpriseApp;
import com.clover.spika.enterprise.chat.lazy.ImageLoader;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.GoogleUtils;

public class SplashActivity extends LoginBaseActivity {

	Bundle extras;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);

		extras = getIntent().getExtras();
        ImageLoader.init(this);

		if (SpikaEnterpriseApp.getSharedPreferences(this).getCustomBoolean(Const.REMEMBER_CREDENTIALS)) {
			pause(1000, false);
		} else {
			pause(2000, true);
		}
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

				if (!goToLogin) {
					new GoogleUtils().getPushToken(context);
				}

				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				if (goToLogin) {
					Intent intent = new Intent(context, LoginActivity.class);
					startActivity(intent);
					finish();
					return;
				}
				login();
			}
		}.execute();
	}

	private void login() {
		executeLoginApi(SpikaEnterpriseApp.getSharedPreferences(this).getCustomString(Const.USERNAME), SpikaEnterpriseApp.getSharedPreferences(this).getCustomString(Const.PASSWORD), SpikaEnterpriseApp.getSharedPreferences(this).getCustomString(Const.PUSH_TOKEN_LOCAL), extras,
				false);
	}

	@Override
	public void startActivity(Intent intent) {
		super.startActivity(intent);
		overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
	}

}

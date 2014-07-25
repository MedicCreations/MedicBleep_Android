package com.clover.spika.enterprise.chat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.clover.spika.enterprise.chat.extendables.BaseAsyncTask;
import com.clover.spika.enterprise.chat.extendables.LoginBaseActivity;
import com.clover.spika.enterprise.chat.extendables.SpikaEnterpriseApp;
import com.clover.spika.enterprise.chat.utils.Const;

public class SplashActivity extends LoginBaseActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		
		Log.e("LOG", SpikaEnterpriseApp.getSharedPreferences(this).getCustomBoolean(Const.REMEMBER_CREDENTIALS)+"333");
		
		if(SpikaEnterpriseApp.getSharedPreferences(this).getCustomBoolean(Const.REMEMBER_CREDENTIALS)){
			pause(1000, false);
		}else{
			pause(2000, true);
		}
		
	}

	// XXX do something smart
	private void pause(final int time, final boolean toLogin) {
		new BaseAsyncTask<Void, Void, Void>(this, false) {

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
				if(toLogin){
					Intent intent = new Intent(context, LoginActivity.class);
					startActivity(intent);
					finish();
					return;
				}
				login();
			}
		}.execute();
	}
	
	private void login(){
		executeLoginApi(SpikaEnterpriseApp.getSharedPreferences(this).getCustomString(Const.USERNAME),
				SpikaEnterpriseApp.getSharedPreferences(this).getCustomString(Const.PASSWORD), false);
	}
	
	@Override
	public void startActivity(Intent intent) {
		super.startActivity(intent);
		overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
	}

}

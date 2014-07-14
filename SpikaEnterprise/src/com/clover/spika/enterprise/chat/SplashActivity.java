package com.clover.spika.enterprise.chat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.clover.spika.enterprise.chat.extendables.BaseAsyncTask;

public class SplashActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);

		pause();
	}

	// XXX do something smart
	private void pause() {
		new BaseAsyncTask<Void, Void, Void>(this, false) {

			@Override
			protected Void doInBackground(Void... params) {

				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				Intent intent = new Intent(context, LoginActivity.class);
				context.startActivity(intent);
			}
		}.execute();
	}

}

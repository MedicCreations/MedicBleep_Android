package com.clover.spika.enterprise.chat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.clover.spika.enterprise.chat.extendables.BaseAsyncTask;
import com.clover.spika.enterprise.chat.views.RobotoThinEditText;

public class LoginActivity extends Activity {

	RobotoThinEditText username;
	Button loginBtn;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		username = (RobotoThinEditText) findViewById(R.id.username);

		loginBtn = (Button) findViewById(R.id.loginBtn);
		loginBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				login();
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		hideKeyboard(username);
	}

	@Override
	public void startActivity(Intent intent) {
		super.startActivity(intent);
		overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
	}

	public void hideKeyboard(EditText et) {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
	}

	private void login() {
		new BaseAsyncTask<Void, Void, Void>(this, true) {

			protected Void doInBackground(Void... params) {

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				return null;
			};

			protected void onPostExecute(Void result) {
				Intent intent = new Intent(context, LobbyActivity.class);
				startActivity(intent);
				finish();
			};

		}.execute();
	}
}

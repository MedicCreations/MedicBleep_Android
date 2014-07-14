package com.clover.spika.enterprise.chat;

import android.os.Bundle;

import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.views.RobotoThinEditText;

public class LoginActivity extends BaseActivity {

	RobotoThinEditText username;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		username = (RobotoThinEditText) findViewById(R.id.username);
	}

	@Override
	protected void onResume() {
		super.onResume();

		hideKeyboard(username);
	}

}

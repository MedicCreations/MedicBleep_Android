package com.clover.spika.enterprise.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.clover.spika.enterprise.chat.extendables.LoginBaseActivity;
import com.clover.spika.enterprise.chat.extendables.SpikaEnterpriseApp;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class LoginActivity extends LoginBaseActivity {

	private EditText username;
	private EditText password;
	private CheckBox rememberMeCheckBox;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		username = (EditText) findViewById(R.id.username);
		password = (EditText) findViewById(R.id.password);
		rememberMeCheckBox = (CheckBox) findViewById(R.id.checkBoxRememberLogin);
		rememberMeCheckBox.setChecked(SpikaEnterpriseApp.getSharedPreferences(this).getCustomBoolean(Const.REMEMBER_CREDENTIALS));
		rememberMeCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (!isChecked) {
					SpikaEnterpriseApp.getSharedPreferences(LoginActivity.this).removePreference(Const.USERNAME);
					SpikaEnterpriseApp.getSharedPreferences(LoginActivity.this).removePreference(Const.PASSWORD);
				}

				SpikaEnterpriseApp.getSharedPreferences(LoginActivity.this).setCustomBoolean(Const.REMEMBER_CREDENTIALS, isChecked);
			}
		});

	}

	@Override
	protected void onResume() {
		super.onResume();
		hideKeyboard(username);

		username.setText(SpikaEnterpriseApp.getSharedPreferences(this).getCustomString(Const.USERNAME));
		password.setText(SpikaEnterpriseApp.getSharedPreferences(this).getCustomString(Const.PASSWORD));

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

	public void onLoginClick(View view) {
		login();
	}

	private void login() {
		boolean errorLock = false;
		username.setError(null);
		password.setError(null);

		if (TextUtils.isEmpty(username.getText().toString())) {
			username.setError("Username should not be empty");
			errorLock = true;
		}
		if (TextUtils.isEmpty(password.getText().toString())) {
			password.setError("Password should not be empty");
			errorLock = true;
		}

		if (!errorLock) {

			String hasPass;
			try {
				byte[] digest = MessageDigest.getInstance("MD5").digest(password.getText().toString().getBytes("UTF-8"));
				hasPass = Utils.convertByteArrayToHexString(digest);

				executeLoginApi(username.getText().toString(), hasPass, null, true);

				if (rememberMeCheckBox.isChecked()) {
					SpikaEnterpriseApp.getSharedPreferences(this).setCustomString(Const.USERNAME, username.getText().toString());
					SpikaEnterpriseApp.getSharedPreferences(this).setCustomString(Const.PASSWORD, password.getText().toString());
				}
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

		}
	}
}

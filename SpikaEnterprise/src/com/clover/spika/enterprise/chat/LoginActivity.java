package com.clover.spika.enterprise.chat;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.clover.spika.enterprise.chat.extendables.LoginBaseActivity;
import com.clover.spika.enterprise.chat.extendables.SpikaEnterpriseApp;
import com.clover.spika.enterprise.chat.models.greendao.DaoMaster;
import com.clover.spika.enterprise.chat.models.greendao.DaoMaster.DevOpenHelper;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.views.RobotoRegularTextView;

public class LoginActivity extends LoginBaseActivity {

	private EditText username;
	private EditText password;
	private CheckBox rememberMeCheckBox;
	Bundle extras;
	private RobotoRegularTextView tvForgotPassword;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		extras = getIntent().getExtras();

		username = (EditText) findViewById(R.id.username);
		password = (EditText) findViewById(R.id.password);
		tvForgotPassword = (RobotoRegularTextView) findViewById(R.id.tvForgotPassword);
		tvForgotPassword.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), ForgotPasswordActivity.class);
				startActivity(intent);

			}
		});
		rememberMeCheckBox = (CheckBox) findViewById(R.id.checkBoxRememberLogin);
		rememberMeCheckBox.setChecked(SpikaEnterpriseApp.getSharedPreferences().getCustomBoolean(Const.REMEMBER_CREDENTIALS));
		rememberMeCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (!isChecked) {
					SpikaEnterpriseApp.getSharedPreferences().removePreference(Const.USERNAME);
					SpikaEnterpriseApp.getSharedPreferences().removePreference(Const.PASSWORD);
				}

				SpikaEnterpriseApp.getSharedPreferences().setCustomBoolean(Const.REMEMBER_CREDENTIALS, isChecked);
			}
		});

	}

	@Override
	protected void onResume() {
		super.onResume();
		hideKeyboard(username);

		username.setText(SpikaEnterpriseApp.getSharedPreferences().getCustomString(Const.USERNAME));
		password.setText(SpikaEnterpriseApp.getSharedPreferences().getCustomString(Const.PASSWORD));

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
			username.setError(getString(R.string.login_empty_username));
			errorLock = true;
		}
		if (TextUtils.isEmpty(password.getText().toString())) {
			password.setError(getString(R.string.login_empty_password));
			errorLock = true;
		}

		if (!errorLock) {

			try {
				
				if(SpikaEnterpriseApp.getSharedPreferences().getCustomString(Const.USERNAME).equals(username.getText().toString())){
					//no need to recreated empty database
				}else{
					//recreate empty database
					DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "SpikaEnterprise.db", null);
					SQLiteDatabase db;
					db = helper.getWritableDatabase();
					DaoMaster.dropAllTables(db, true);
					DaoMaster.createAllTables(db, true);
				}

				executePreLoginApi(username.getText().toString(), password.getText().toString(), extras, true);

				if (rememberMeCheckBox.isChecked()) {
					SpikaEnterpriseApp.getSharedPreferences().setCustomString(Const.USERNAME, username.getText().toString());
					SpikaEnterpriseApp.getSharedPreferences().setCustomString(Const.PASSWORD, password.getText().toString());
					SpikaEnterpriseApp.getSharedPreferences().setCustomBoolean(Const.REMEMBER_CREDENTIALS, true);
				}
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

		}
	}

}

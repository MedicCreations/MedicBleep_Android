package com.medicbleep.app.chat;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.medicbleep.app.chat.extendables.LoginBaseActivity;
import com.medicbleep.app.chat.extendables.SpikaEnterpriseApp;
import com.medicbleep.app.chat.models.greendao.DaoMaster;
import com.medicbleep.app.chat.models.greendao.DaoMaster.DevOpenHelper;
import com.medicbleep.app.chat.utils.Const;
import com.medicbleep.app.chat.utils.Helper;
import com.medicbleep.app.chat.utils.Logger;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

public class LoginActivity extends LoginBaseActivity {

	private EditText username;
	private EditText password;
	private CheckBox rememberMeCheckBox;
	Bundle extras;
	private TextView tvForgotPassword;

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		extras = getIntent().getExtras();

		username = (EditText) findViewById(R.id.username);
		password = (EditText) findViewById(R.id.password);
		tvForgotPassword = (TextView) findViewById(R.id.tvForgotPassword);
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
					Helper.setUsername(null);
					Helper.setPassword(null);
				}

				SpikaEnterpriseApp.getSharedPreferences().setCustomBoolean(Const.REMEMBER_CREDENTIALS, isChecked);
			}
		});

        TextView whatIsThis = (TextView) findViewById(R.id.tvWhatIsThis);
        SpannableString span = new SpannableString(getString(R.string.what_is_this_));
        span.setSpan(new UnderlineSpan(), 0, span.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        whatIsThis.setText(span);

		findViewById(R.id.tvWhatIsThis).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Const.WHAT_IS_THIS)));
			}
		});

	}

    @Override
	protected void onResume() {
		super.onResume();
		hideKeyboard(username);

        username.setText(Helper.getUsername());
        password.setText(Helper.getPassword());

        if (this.extras != null) {

			Logger.e("IMA EXTRE:" + extras.toString() + "\n" + username + "\n" + password);

            boolean outsideStart = extras.getBoolean("medic_bleep_outside_start", false);

            if (outsideStart == true) {

                String username = extras.getString("medic_bleep_email");
                String password = extras.getString("medic_bleep_password");

				Logger.e(extras.toString() + "\n" + username + "\n" + password);

                if (username.length() == 0 || password.length() == 0){
                    Toast.makeText(getApplicationContext(), "Missing login parameter", Toast.LENGTH_LONG).show();
                }else{
                    this.username.setText(username);
                    this.password.setText(password);

					Helper.setUsername(this.username.getText().toString());
					Helper.setPassword(this.password.getText().toString());
					login();
                }
            }
        }
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
				
				if(Helper.getUsername().equals(username.getText().toString())){
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
					Helper.setUsername(username.getText().toString());
					Helper.setPassword(password.getText().toString());
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

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
import android.widget.Toast;

import com.clover.spika.enterprise.chat.api.ApiCallback;
import com.clover.spika.enterprise.chat.api.LoginApi;
import com.clover.spika.enterprise.chat.models.Login;
import com.clover.spika.enterprise.chat.models.Result;

public class LoginActivity extends Activity {

	private EditText username;
    private EditText password;

	Button loginBtn;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);

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
        new LoginApi().loginWithCredentials(
                username.getText().toString(),
                password.getText().toString(),
                this, true, new ApiCallback<Login>() {
            @Override
            public void onApiResponse(Result<Login> result) {
                // TODO: srediti logiku za fail i success response
                if (result.isSuccess()) {
                    Toast.makeText(LoginActivity.this,
                            result.getResultData() == null ? "no data" : result.getResultData().toString(),
                            Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(LoginActivity.this, LobbyActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this,
                            "Failure",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
	}
}

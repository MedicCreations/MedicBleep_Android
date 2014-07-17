package com.clover.spika.enterprise.chat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.clover.spika.enterprise.chat.api.ApiCallback;
import com.clover.spika.enterprise.chat.api.LoginApi;
import com.clover.spika.enterprise.chat.models.Login;
import com.clover.spika.enterprise.chat.models.Result;

public class LoginActivity extends Activity {

    private EditText username;
    private EditText password;

    @Override

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);

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
            executeLoginApi();
        }
    }

    private void executeLoginApi() {
        new LoginApi().loginWithCredentials(
                username.getText().toString(),
                password.getText().toString(),
                this, true, new ApiCallback<Login>() {
                    @Override
                    public void onApiResponse(Result<Login> result) {
                        // TODO: srediti logiku za fail i success response
                        if (result.isSuccess()) {
                            Intent intent = new Intent(LoginActivity.this, LobbyActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            if (result.hasResultData()) {
                                Toast.makeText(LoginActivity.this,
                                        result.getResultData().getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
        );
    }

    public void onLoginClick(View view) {
        login();
    }
}

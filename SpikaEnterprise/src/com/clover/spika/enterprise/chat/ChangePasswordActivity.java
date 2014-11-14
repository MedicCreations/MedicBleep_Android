package com.clover.spika.enterprise.chat;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.clover.spika.enterprise.chat.api.ApiCallback;
import com.clover.spika.enterprise.chat.api.UserApi;
import com.clover.spika.enterprise.chat.dialogs.AppDialog;
import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.extendables.BaseModel;
import com.clover.spika.enterprise.chat.models.Result;

public class ChangePasswordActivity extends BaseActivity {

	Button goBack;

	EditText newPassword;
	EditText confirmNewPassword;
	Button confirmBtn;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_change_password);

		goBack = (Button) findViewById(R.id.goBack);
		goBack.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});

		newPassword = (EditText) findViewById(R.id.newPassword);
		confirmNewPassword = (EditText) findViewById(R.id.confirmNewPassword);

		confirmBtn = (Button) findViewById(R.id.confirmBtn);
		confirmBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				changePassword();
			}
		});
	}

	private void changePassword() {

		newPassword.setError(null);
		confirmNewPassword.setError(null);

		String password = newPassword.getText().toString();
		String confirmPassword = confirmNewPassword.getText().toString();

		if (TextUtils.isEmpty(password)) {
			newPassword.setError(getString(R.string.new_password));
			return;
		}

		if (TextUtils.isEmpty(confirmPassword)) {
			confirmNewPassword.setError(getString(R.string.re_type_password));
			return;
		}

		if (!password.equals(confirmPassword)) {
			confirmNewPassword.setError(getString(R.string.new_passwords_not_identical));
			return;
		}

		new UserApi().updateUserPassword(password, this, new ApiCallback<BaseModel>() {

			@Override
			public void onApiResponse(Result<BaseModel> result) {
				if (result.isSuccess()) {
					AppDialog dialog = new AppDialog(ChangePasswordActivity.this, true);
					dialog.setSucceed();
				} else {
					AppDialog dialog = new AppDialog(ChangePasswordActivity.this, false);
					dialog.setFailed(result.getResultData().getCode());
				}
			}
		});
	}
}

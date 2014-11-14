package com.clover.spika.enterprise.chat;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.clover.spika.enterprise.chat.api.ApiCallback;
import com.clover.spika.enterprise.chat.api.UserApi;
import com.clover.spika.enterprise.chat.dialogs.AppDialog;
import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.extendables.BaseModel;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.views.RobotoThinButton;
import com.clover.spika.enterprise.chat.views.RobotoThinEditText;

public class ForgotPasswordActivity extends BaseActivity implements OnClickListener {

	private RobotoThinEditText etUsername;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_forgot_password);

		etUsername = (RobotoThinEditText) findViewById(R.id.username);

		RobotoThinButton submitBtn = (RobotoThinButton) findViewById(R.id.submitBtn);
		RobotoThinButton cancelBtn = (RobotoThinButton) findViewById(R.id.cancelBtn);

		submitBtn.setOnClickListener(this);
		cancelBtn.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.submitBtn:
			String username = etUsername.getText().toString();
			hideKeyboard(etUsername);
			forgotPassword(username);
			break;
		case R.id.cancelBtn:
			finish();
			break;

		default:
			break;
		}
	}

	private void forgotPassword(String username) {

		new UserApi().forgotPassword(username, this, new ApiCallback<BaseModel>() {

			@Override
			public void onApiResponse(Result<BaseModel> result) {

				AppDialog dialog = new AppDialog(ForgotPasswordActivity.this, true);
				if (result.isSuccess()) {
					dialog.setInfo(getString(R.string.email_sent));
				} else {
					dialog.setFailed(result.getResultData().getCode());
				}
			}
		});

	}

}

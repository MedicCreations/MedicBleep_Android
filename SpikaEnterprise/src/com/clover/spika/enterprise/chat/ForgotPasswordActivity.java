package com.clover.spika.enterprise.chat;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.clover.spika.enterprise.chat.api.robospice.UserSpice;
import com.clover.spika.enterprise.chat.dialogs.AppDialog;
import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.extendables.BaseModel;
import com.clover.spika.enterprise.chat.services.robospice.CustomSpiceListener;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Helper;
import com.clover.spika.enterprise.chat.utils.Utils;
import com.clover.spika.enterprise.chat.views.RobotoThinButton;
import com.clover.spika.enterprise.chat.views.RobotoThinEditText;
import com.octo.android.robospice.persistence.exception.SpiceException;

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

		handleProgress(true);

		UserSpice.ForgotPassword updateUserImage = new UserSpice.ForgotPassword(username, this);
		spiceManager.execute(updateUserImage, new CustomSpiceListener<BaseModel>() {

			@Override
			public void onRequestFailure(SpiceException arg0) {
				super.onRequestFailure(arg0);
				handleProgress(false);
				Utils.onFailedUniversal(null, ForgotPasswordActivity.this);
			}

			@Override
			public void onRequestSuccess(BaseModel result) {
				super.onRequestSuccess(result);
				handleProgress(false);

				if (result.getCode() == Const.API_SUCCESS) {

					AppDialog dialog = new AppDialog(ForgotPasswordActivity.this, true);
					dialog.setInfo(getString(R.string.email_sent));

				} else {
					Utils.onFailedUniversal(Helper.errorDescriptions(ForgotPasswordActivity.this, result.getCode()), ForgotPasswordActivity.this);
				}
			}
		});
	}

}

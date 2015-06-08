package com.clover.spika.enterprise.chat;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.clover.spika.enterprise.chat.api.robospice.UserSpice;
import com.clover.spika.enterprise.chat.dialogs.AppDialog;
import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.extendables.BaseModel;
import com.clover.spika.enterprise.chat.services.robospice.CustomSpiceListener;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Helper;
import com.clover.spika.enterprise.chat.utils.Utils;
import com.octo.android.robospice.persistence.exception.SpiceException;

public class ForgotPasswordActivity extends BaseActivity implements OnClickListener {

	private EditText etUsername;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_forgot_password);

		etUsername = (EditText) findViewById(R.id.username);

		Button submitBtn = (Button) findViewById(R.id.submitBtn);
        ImageButton cancelBtn = (ImageButton) findViewById(R.id.cancelBtn);

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

        boolean errorLock = false;
        etUsername.setError(null);

        if (TextUtils.isEmpty(etUsername.getText().toString())) {
            etUsername.setError(getString(R.string.login_empty_username));
            errorLock = true;
        }

        if(errorLock){
            return;
        }

		handleProgress(true);

		UserSpice.ForgotPassword updateUserImage = new UserSpice.ForgotPassword(username);
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

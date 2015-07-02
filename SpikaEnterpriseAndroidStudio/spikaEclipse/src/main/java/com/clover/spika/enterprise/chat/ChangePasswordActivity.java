package com.clover.spika.enterprise.chat;

import java.io.Serializable;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.clover.spika.enterprise.chat.api.robospice.UserSpice;
import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.models.Login;
import com.clover.spika.enterprise.chat.models.Organization;
import com.clover.spika.enterprise.chat.services.robospice.CustomSpiceListener;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Helper;
import com.clover.spika.enterprise.chat.utils.Utils;
import com.octo.android.robospice.persistence.exception.SpiceException;

public class ChangePasswordActivity extends BaseActivity {

	ImageButton goBack;

	EditText newPassword;
	EditText confirmNewPassword;
	Button confirmBtn;

	String tempPassword;
	String username;

	boolean isUpdate = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_change_password);

		Bundle extras = getIntent().getExtras();

		if (extras.containsKey(Const.IS_UPDATE_PASSWORD)) {
			isUpdate = true;
		} else {
			tempPassword = extras.getString(Const.TEMP_PASSWORD);
			username = extras.getString(Const.USERNAME);
		}

		goBack = (ImageButton) findViewById(R.id.cancelBtn);
		goBack.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});

		newPassword = (EditText) findViewById(R.id.newPassword);
		confirmNewPassword = (EditText) findViewById(R.id.confirmNewPassword);

		confirmBtn = (Button) findViewById(R.id.submitBtn);
		confirmBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				changePassword();
			}
		});
	}

    @Override
    protected void onPause() {
        hideKeyboard(confirmNewPassword);
        super.onPause();
    }

    private void changePassword() {

		newPassword.setError(null);
		confirmNewPassword.setError(null);

		final String password = newPassword.getText().toString();
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

        boolean atLeastOneAlpha = password.matches(".*[a-zA-Z]+.*");
        if (!atLeastOneAlpha) {
            confirmNewPassword.setError(getString(R.string.password_at_least_one_letter));
            return;
        }

        boolean atLeastOneNumber = password.matches(".*[0-9]+.*");
        if (!atLeastOneNumber) {
            confirmNewPassword.setError(getString(R.string.password_at_least_one_number));
            return;
        }
		
		handleProgress(true);

        hideKeyboard(confirmNewPassword);

		UserSpice.UpdateUserPassword updateUserImage = new UserSpice.UpdateUserPassword(isUpdate, tempPassword, password);
		spiceManager.execute(updateUserImage, new CustomSpiceListener<Login>() {

			@Override
			public void onRequestFailure(SpiceException arg0) {
				super.onRequestFailure(arg0);
				handleProgress(false);
				Utils.onFailedUniversal(null, ChangePasswordActivity.this);
			}

			@Override
			public void onRequestSuccess(Login result) {
				super.onRequestSuccess(result);
				handleProgress(false);

				if (result.getCode() == Const.API_SUCCESS) {
					
					if (!isUpdate) {
						List<Organization> organizations = result.organizations;

						if (organizations.size() > 1) {
							Intent intent = new Intent(ChangePasswordActivity.this, ChooseOrganizationActivity.class);

							intent.putExtra(Const.ORGANIZATIONS, (Serializable) organizations);
							intent.putExtra(Const.USERNAME, username);
							intent.putExtra(Const.PASSWORD, password);

							startActivity(intent);
						}else{
                            Intent intent = new Intent(ChangePasswordActivity.this, MainActivity.class);
                            startActivity(intent);
                        }

					}

					finish();
					
				} else {
					Utils.onFailedUniversal(Helper.errorDescriptions(ChangePasswordActivity.this, result.getCode()), ChangePasswordActivity.this);
				}
			}
		});
	}
}

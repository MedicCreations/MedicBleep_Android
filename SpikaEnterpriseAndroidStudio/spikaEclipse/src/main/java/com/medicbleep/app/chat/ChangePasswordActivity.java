package com.medicbleep.app.chat;

import java.io.Serializable;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.medicbleep.app.chat.api.robospice.UserSpice;
import com.medicbleep.app.chat.dialogs.AppDialog;
import com.medicbleep.app.chat.extendables.BaseActivity;
import com.medicbleep.app.chat.models.Login;
import com.medicbleep.app.chat.models.Organization;
import com.medicbleep.app.chat.services.robospice.CustomSpiceListener;
import com.medicbleep.app.chat.utils.Const;
import com.medicbleep.app.chat.utils.Helper;
import com.medicbleep.app.chat.utils.Utils;
import com.octo.android.robospice.exception.NoNetworkException;
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
			newPassword.requestFocus();
			newPassword.setError(getString(R.string.new_password));
			return;
		}

		if (TextUtils.isEmpty(confirmPassword)) {
			confirmNewPassword.requestFocus();
			confirmNewPassword.setError(getString(R.string.re_type_password));
			return;
		}

		if (!password.equals(confirmPassword)) {
			confirmNewPassword.requestFocus();
			confirmNewPassword.setError(getString(R.string.new_passwords_not_identical));
			return;
		}

		boolean isGoodLenght = password.length() > 3 && password.length() < 16;
		if (!isGoodLenght) {
			confirmNewPassword.requestFocus();
			confirmNewPassword.setError(getString(R.string.password_lenghth_error));
			return;
		}

        boolean atLeastOneAlpha = password.matches(".*[a-zA-Z]+.*");
        if (!atLeastOneAlpha) {
			confirmNewPassword.requestFocus();
            confirmNewPassword.setError(getString(R.string.password_at_least_one_letter));
            return;
        }

        boolean atLeastOneNumber = password.matches(".*[0-9]+.*");
        if (!atLeastOneNumber) {
			confirmNewPassword.requestFocus();
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
				if(arg0 instanceof NoNetworkException){
					new AppDialog(ChangePasswordActivity.this, false).setFailed(getResources().getString(R.string.no_internet_connection_));
				}else{
					Utils.onFailedUniversal(null, ChangePasswordActivity.this, arg0);
				}
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
                            Intent intent = new Intent(ChangePasswordActivity.this, LoginActivity.class);
							intent.putExtra(Const.FROM_CHANGE_PASS, true);
							Helper.setUsername(username);
							Helper.setPassword(password);
                            startActivity(intent);
                        }

					}

					finish();
					
				} else {
					Utils.onFailedUniversal(Helper.errorDescriptions(ChangePasswordActivity.this, result.getCode()), ChangePasswordActivity.this, null);
				}
			}
		});
	}
}

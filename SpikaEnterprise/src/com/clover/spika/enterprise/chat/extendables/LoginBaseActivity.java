package com.clover.spika.enterprise.chat.extendables;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.Bundle;

import com.clover.spika.enterprise.chat.ChangePasswordActivity;
import com.clover.spika.enterprise.chat.LoginActivity;
import com.clover.spika.enterprise.chat.MainActivity;
import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.api.ApiCallback;
import com.clover.spika.enterprise.chat.api.LoginApi;
import com.clover.spika.enterprise.chat.dialogs.AppDialog;
import com.clover.spika.enterprise.chat.models.Login;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.GoogleUtils;
import com.clover.spika.enterprise.chat.utils.Helper;
import com.clover.spika.enterprise.chat.utils.Logger;
import com.clover.spika.enterprise.chat.utils.Utils;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

public abstract class LoginBaseActivity extends Activity {

	protected void executeLoginApi(String user, final String pass, final Bundle extras, boolean showProgress) throws UnsupportedEncodingException, NoSuchAlgorithmException {
		String hashPassword = Utils.getHexString(pass);
		new LoginApi().loginWithCredentials(user, hashPassword, this, showProgress, new ApiCallback<Login>() {
			@Override
			public void onApiResponse(Result<Login> result) {
				if (result.isSuccess()) {

					Logger.d("Success");

					Helper.setUserProperties(getApplicationContext(), result.getResultData().getUserId(), result.getResultData().getImage(), result.getResultData().getFirstname(),
							result.getResultData().getLastname(), result.getResultData().getToken());

					new GoogleUtils().getPushToken(LoginBaseActivity.this, result.getResultData().getToken());

					Intent intent = new Intent(LoginBaseActivity.this, MainActivity.class);

					if (extras != null) {
						intent.putExtras(extras);
					}

					startActivity(intent);
					finish();
					
				} else {

					Logger.d("Not Success");
					String message = "";
					
					if (result.hasResultData()) {
						
						if (result.getResultData().getCode() == Const.E_INVALID_TOKEN) {
							
							Intent intent = new Intent(LoginBaseActivity.this, LoginActivity.class);
							intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(intent);
							finish();
							
						} else if (result.getResultData().getCode() == Const.E_LOGIN_WITH_TEMP_PASS) {
							
							Intent intent = new Intent(LoginBaseActivity.this, ChangePasswordActivity.class);
							intent.putExtra(Const.TEMP_PASSWORD, pass);
							startActivity(intent);
							finish();
							
							return;
							
						} else {
							message = result.getResultData().getMessage();
						}
					} else {
						message = getString(R.string.e_something_went_wrong);
					}

					AppDialog dialog = new AppDialog(LoginBaseActivity.this, false);
					dialog.setFailed(message);
					dialog.setOnDismissListener(new OnDismissListener() {

						@Override
						public void onDismiss(DialogInterface dialog) {
							
							Intent intent = new Intent(LoginBaseActivity.this, LoginActivity.class);
							intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(intent);
//							finish();
						}
					});
				}
			}
		});
	}

}

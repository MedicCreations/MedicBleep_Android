package com.clover.spika.enterprise.chat.extendables;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.Bundle;

import com.clover.spika.enterprise.chat.MainActivity;
import com.clover.spika.enterprise.chat.LoginActivity;
import com.clover.spika.enterprise.chat.api.ApiCallback;
import com.clover.spika.enterprise.chat.api.LoginApi;
import com.clover.spika.enterprise.chat.dialogs.AppDialog;
import com.clover.spika.enterprise.chat.models.Login;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.utils.Helper;

public abstract class LoginBaseActivity extends Activity {

	protected void executeLoginApi(String user, String pass, String token, final Bundle extras, boolean showProgress) {
		new LoginApi().loginWithCredentials(user, pass, token, this, showProgress, new ApiCallback<Login>() {
			@Override
			public void onApiResponse(Result<Login> result) {
				if (result.isSuccess()) {
					Helper.setUserProperties(getApplicationContext(), result.getResultData().getUserId(), result.getResultData().getImage(), result.getResultData().getFirstname(), result.getResultData().getLastname());
					Intent intent = new Intent(LoginBaseActivity.this, MainActivity.class);

					if (extras != null) {
						intent.putExtras(extras);
					}

					startActivity(intent);
					finish();
				} else {
					if (result.hasResultData()) {

						AppDialog dialog = new AppDialog(LoginBaseActivity.this, false);
						dialog.setFailed(result.getResultData().getMessage());
						dialog.setOnDismissListener(new OnDismissListener() {

							@Override
							public void onDismiss(DialogInterface dialog) {
								Intent intent = new Intent(LoginBaseActivity.this, LoginActivity.class);
								intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
								startActivity(intent);
							}
						});
					}
				}
			}
		});
	}

}

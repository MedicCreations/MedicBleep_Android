package com.clover.spika.enterprise.chat.extendables;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.Bundle;

import com.clover.spika.enterprise.chat.ChangePasswordActivity;
import com.clover.spika.enterprise.chat.ChooseOrganizationActivity;
import com.clover.spika.enterprise.chat.LoginActivity;
import com.clover.spika.enterprise.chat.MainActivity;
import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.api.ApiCallback;
import com.clover.spika.enterprise.chat.api.LoginApi;
import com.clover.spika.enterprise.chat.dialogs.AppDialog;
import com.clover.spika.enterprise.chat.models.Login;
import com.clover.spika.enterprise.chat.models.Organization;
import com.clover.spika.enterprise.chat.models.PreLogin;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.GoogleUtils;
import com.clover.spika.enterprise.chat.utils.Helper;
import com.clover.spika.enterprise.chat.utils.Logger;
import com.clover.spika.enterprise.chat.utils.Utils;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public abstract class LoginBaseActivity extends Activity {
	
	protected void executePreLoginApi(final String user, final String pass, final Bundle extras, final boolean showProgress) throws UnsupportedEncodingException, NoSuchAlgorithmException {
		String hashPassword = Utils.getHexString(pass);
		new LoginApi().preLoginWithCredentials(user, hashPassword, this, showProgress, new ApiCallback<PreLogin>() {
			
			@Override
			public void onApiResponse(Result<PreLogin> result) {
				if (result.isSuccess()) {

					Logger.d("Success");
					List<Organization> organizations = result.getResultData().getOrganizations();
					
					if (organizations.size()==1){
						try {
							executeLoginApi(user, pass, organizations.get(0).getId(), extras, showProgress);
						} catch (UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (NoSuchAlgorithmException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else {
						
						Intent intent = new Intent(LoginBaseActivity.this, ChooseOrganizationActivity.class);
	
						if (extras != null) {
							extras.putSerializable(Const.ORGANIZATIONS, (Serializable) organizations);
							extras.putString(Const.USERNAME, user);
							extras.putString(Const.PASSWORD, pass);
							intent.putExtras(extras);
						} else {
							intent.putExtra(Const.ORGANIZATIONS, (Serializable) organizations);
							intent.putExtra(Const.USERNAME, user);
							intent.putExtra(Const.PASSWORD, pass);
						}
	
						startActivity(intent);
						finish();
						
					}
					
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

	protected void executeLoginApi(String user, final String pass, String organization_id, final Bundle extras, boolean showProgress) throws UnsupportedEncodingException, NoSuchAlgorithmException {
		String hashPassword = Utils.getHexString(pass);
		new LoginApi().loginWithCredentials(user, hashPassword, organization_id, this, showProgress, new ApiCallback<Login>() {
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

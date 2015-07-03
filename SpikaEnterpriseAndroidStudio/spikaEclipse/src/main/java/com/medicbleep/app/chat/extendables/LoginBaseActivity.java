package com.medicbleep.app.chat.extendables;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.medicbleep.app.chat.ChangePasswordActivity;
import com.medicbleep.app.chat.ChooseOrganizationActivity;
import com.medicbleep.app.chat.LoginActivity;
import com.medicbleep.app.chat.MainActivity;
import com.medicbleep.app.chat.NewPasscodeActivity;
import com.medicbleep.app.chat.R;
import com.medicbleep.app.chat.SMSVerificationActivity;
import com.medicbleep.app.chat.api.robospice.LoginSpice;
import com.medicbleep.app.chat.dialogs.AppDialog;
import com.medicbleep.app.chat.dialogs.AppProgressAlertDialog;
import com.medicbleep.app.chat.models.Login;
import com.medicbleep.app.chat.models.Organization;
import com.medicbleep.app.chat.models.PreLogin;
import com.medicbleep.app.chat.services.robospice.CustomSpiceListener;
import com.medicbleep.app.chat.services.robospice.OkHttpService;
import com.medicbleep.app.chat.utils.Const;
import com.medicbleep.app.chat.utils.GoogleUtils;
import com.medicbleep.app.chat.utils.Helper;
import com.medicbleep.app.chat.utils.LocationUtility;
import com.medicbleep.app.chat.utils.Logger;
import com.medicbleep.app.chat.utils.PasscodeUtility;
import com.medicbleep.app.chat.utils.Utils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;

public abstract class LoginBaseActivity extends Activity {

	protected SpiceManager spiceManager = new SpiceManager(OkHttpService.class);

	private AppProgressAlertDialog progressBar;
	
	private Bundle tempExtras;

	public void handleProgress(boolean showProgress) {

		try {

			if (showProgress) {

				if (progressBar != null && progressBar.isShowing()) {
					progressBar.dismiss();
					progressBar = null;
				}

				progressBar = new AppProgressAlertDialog(this);
				progressBar.show();

			} else {

				if (progressBar != null && progressBar.isShowing()) {
					progressBar.dismiss();
				}

				progressBar = null;
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		Bundle extras = getIntent().getExtras();

		if (extras != null) {

			boolean outsideStart = extras.getBoolean("medic_bleep_outside_start", false);

			if (outsideStart == true) {

				String username = extras.getString("medic_bleep_email");
				String password = extras.getString("medic_bleep_password");
				String ocrUserId = String.valueOf(extras.getInt("ocr_user_id"));

				Logger.e("LOGIN:" + "\n" + extras.toString() + "\n" + username + "\n" + password + "\n" + ocrUserId);

				if (username.length() == 0 || password.length() == 0){
					Toast.makeText(getApplicationContext(), "Missing login parameter", Toast.LENGTH_LONG).show();
				}else{

				}
			}
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		spiceManager.start(this);
	}

	@Override
	protected void onStop() {
		spiceManager.shouldStop();
		super.onStop();
	}

	protected void executePreLoginApi(final String user, final String pass, final Bundle extras, final boolean showProgress) throws UnsupportedEncodingException,
			NoSuchAlgorithmException {

		if (TextUtils.isEmpty(LocationUtility.getInstance().getCountryCode())) {
			return;
		}

		handleProgress(showProgress);
		String hashPassword = Utils.getHexString(pass);

		LoginSpice.PreLoginWithCredentials preLoginWithCredentials = new LoginSpice.PreLoginWithCredentials(user, hashPassword);
		spiceManager.execute(preLoginWithCredentials, new CustomSpiceListener<PreLogin>() {

			@Override
			public void onRequestFailure(SpiceException ex) {
				handleProgress(false);
				Utils.onFailedUniversal(null, LoginBaseActivity.this);
			}

			@Override
			public void onRequestSuccess(PreLogin result) {
				handleProgress(false);

				if (result.getCode() == Const.API_SUCCESS) {

					List<Organization> organizations = result.organizations;

					if (organizations.size() == 1) {
						try {
							executeLoginApi(user, pass, organizations.get(0).id, extras, showProgress);
						} catch (Exception e) {
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

					String message = "";

					if (result.getCode() == Const.E_INVALID_TOKEN) {

						Intent intent = new Intent(LoginBaseActivity.this, LoginActivity.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(intent);
						finish();

					} else if (result.getCode() == Const.E_LOGIN_WITH_TEMP_PASS) {

						Intent intent = new Intent(LoginBaseActivity.this, ChangePasswordActivity.class);
						intent.putExtra(Const.TEMP_PASSWORD, pass);
						startActivity(intent);
						finish();

						return;

					} else {

						message = result.getMessage();
					}

					Utils.onFailedUniversal(message, LoginBaseActivity.this, result.getCode(), true);
				}
			}
		});
	}

	protected void executeLoginApi(String user, final String pass, String organization_id, final Bundle extras, boolean showProgress) throws UnsupportedEncodingException,
			NoSuchAlgorithmException {

		handleProgress(showProgress);
		String hashPassword = Utils.getHexString(pass);

		LoginSpice.LoginWithCredentials loginWithCredentials = new LoginSpice.LoginWithCredentials(user, hashPassword, organization_id);
		spiceManager.execute(loginWithCredentials, new CustomSpiceListener<Login>() {

			@Override
			public void onRequestFailure(SpiceException ex) {
				handleProgress(false);
				Utils.onFailedUniversal(null, LoginBaseActivity.this);
			}

			@Override
			public void onRequestSuccess(Login result) {
				handleProgress(false);

				if (result.getCode() == Const.API_SUCCESS) {
                    
                    Helper.setUserProperties(result.getUserId(), result.image, result.image_thumb, result.firstname, result.lastname, result.getToken(), result.email);
					checkPasscodeSet(extras);
                    
				} else {

					String message = "";

					if (result.getCode() == Const.E_INVALID_TOKEN) {

						Intent intent = new Intent(LoginBaseActivity.this, LoginActivity.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(intent);
						finish();

					} else if (result.getCode() == Const.E_LOGIN_WITH_TEMP_PASS) {

						Intent intent = new Intent(LoginBaseActivity.this, ChangePasswordActivity.class);
						intent.putExtra(Const.TEMP_PASSWORD, pass);
						startActivity(intent);
						finish();

						return;

					} else {

						message = result.getMessage();
					}

					Utils.onFailedUniversal(message, LoginBaseActivity.this, result.getCode(), false);
				}
			}
		});
	}

	
	void checkPasscodeSet (final Bundle extras) {
		if (!PasscodeUtility.getInstance().isPasscodeEnabled(this)) {
			tempExtras = extras;
			Intent intent = new Intent(this, SMSVerificationActivity.class);
			intent.putExtra(Const.TYPE, SMSVerificationActivity.TYPE_PHONE_NUMBER);
			startActivityForResult(intent, Const.REQUEST_PHONE_NUMBER);
//			startActivityForResult(new Intent(this, NewPasscodeActivity.class), Const.REQUEST_NEW_PASSCODE);
		}
		else {
			if (this instanceof LoginActivity) {
				PasscodeUtility.getInstance().setSessionValid(true);
			}
			continueToMainActivity(extras);
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (Const.REQUEST_PHONE_NUMBER == requestCode) {
			startActivityForResult(new Intent(this, NewPasscodeActivity.class), Const.REQUEST_NEW_PASSCODE);
		}
		else if (Const.REQUEST_NEW_PASSCODE == requestCode) {
			if (resultCode == Activity.RESULT_OK) {
				PasscodeUtility.getInstance().setSessionValid(true);

				if (data != null && data.hasExtra(NewPasscodeActivity.EXTRA_PASSCODE)) {										
					PasscodeUtility.getInstance().setPasscode(this, data.getStringExtra(NewPasscodeActivity.EXTRA_PASSCODE));
				}
			} else {
				PasscodeUtility.getInstance().setSessionValid(false);
			}
			continueToMainActivity(tempExtras);
		}
	}
	
	void continueToMainActivity (final Bundle extras) {
//		new GoogleUtils().getPushToken(LoginBaseActivity.this);
//
//		final Intent intent = new Intent(LoginBaseActivity.this, MainActivity.class);
//
//		if (extras != null) {
//			intent.putExtras(extras);
//		}
//		
//		SpikaEnterpriseApp.startSocket();
//		
//		startActivity(intent);
//		finish();
        
        int googlePlayServiceResult = GooglePlayServicesUtil.isGooglePlayServicesAvailable(LoginBaseActivity.this);
        
        if(googlePlayServiceResult == ConnectionResult.SUCCESS){
            new GoogleUtils().getPushToken(LoginBaseActivity.this);
        }
        
        final Intent intent = new Intent(LoginBaseActivity.this, MainActivity.class);
        
        if (extras != null) {
            intent.putExtras(extras);
        }
        
        SpikaEnterpriseApp.startSocket();
        
        AppDialog dialog = new AppDialog(LoginBaseActivity.this, false);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            
            @Override
            public void onDismiss(DialogInterface dialog) {
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
            }
        });
        
        if(googlePlayServiceResult == ConnectionResult.SUCCESS){
            if (LoginBaseActivity.this instanceof LoginActivity) {
                PasscodeUtility.getInstance().setSessionValid(true);
            }
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();
        }else if(GooglePlayServicesUtil.isGooglePlayServicesAvailable(LoginBaseActivity.this) == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED){
            dialog.setInfo(getString(R.string.please_update_your_google_play_service_for_receiving_push_notification_));
        }else{
            dialog.setInfo(getString(R.string.please_install_google_play_service_for_receiving_push_notification_));
        }
	}
}

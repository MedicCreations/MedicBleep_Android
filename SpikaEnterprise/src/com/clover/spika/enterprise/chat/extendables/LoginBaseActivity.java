package com.clover.spika.enterprise.chat.extendables;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.clover.spika.enterprise.chat.ChangePasswordActivity;
import com.clover.spika.enterprise.chat.ChooseOrganizationActivity;
import com.clover.spika.enterprise.chat.LoginActivity;
import com.clover.spika.enterprise.chat.MainActivity;
import com.clover.spika.enterprise.chat.NewPasscodeActivity;
import com.clover.spika.enterprise.chat.SMSVerificationActivity;
import com.clover.spika.enterprise.chat.api.robospice.LoginSpice;
import com.clover.spika.enterprise.chat.dialogs.AppProgressAlertDialog;
import com.clover.spika.enterprise.chat.models.Login;
import com.clover.spika.enterprise.chat.models.Organization;
import com.clover.spika.enterprise.chat.models.PreLogin;
import com.clover.spika.enterprise.chat.services.robospice.CustomSpiceListener;
import com.clover.spika.enterprise.chat.services.robospice.OkHttpService;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.GoogleUtils;
import com.clover.spika.enterprise.chat.utils.Helper;
import com.clover.spika.enterprise.chat.utils.PasscodeUtility;
import com.clover.spika.enterprise.chat.utils.Utils;
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

		handleProgress(showProgress);
		String hashPassword = Utils.getHexString(pass);

		LoginSpice.PreLoginWithCredentials preLoginWithCredentials = new LoginSpice.PreLoginWithCredentials(user, hashPassword, this);
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

					Utils.onFailedUniversal(message, LoginBaseActivity.this);
				}
			}
		});
	}

	protected void executeLoginApi(String user, final String pass, String organization_id, final Bundle extras, boolean showProgress) throws UnsupportedEncodingException,
			NoSuchAlgorithmException {

		handleProgress(showProgress);
		String hashPassword = Utils.getHexString(pass);

		LoginSpice.LoginWithCredentials loginWithCredentials = new LoginSpice.LoginWithCredentials(user, hashPassword, organization_id, this);
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

					Helper.setUserProperties(getApplicationContext(), result.getUserId(), result.image, result.image_thumb, result.firstname, result.lastname, result.getToken());

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

					Utils.onFailedUniversal(message, LoginBaseActivity.this);
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
		}
		else {
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
					PasscodeUtility.getInstance().setPasscodeEnabled(this, true);
				}
			} else {
				PasscodeUtility.getInstance().setSessionValid(false);
			}
			continueToMainActivity(tempExtras);
		}
	}
	
	void continueToMainActivity (final Bundle extras) {
		new GoogleUtils().getPushToken(LoginBaseActivity.this);

		final Intent intent = new Intent(LoginBaseActivity.this, MainActivity.class);

		if (extras != null) {
			intent.putExtras(extras);
		}
		
		SpikaEnterpriseApp.startSocket();
		
		startActivity(intent);
		finish();
	}
}

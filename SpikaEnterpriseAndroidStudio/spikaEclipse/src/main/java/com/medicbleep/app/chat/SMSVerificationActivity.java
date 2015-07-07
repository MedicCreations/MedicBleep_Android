package com.medicbleep.app.chat;

import java.util.ArrayList;
import java.util.Locale;

import com.medicbleep.app.chat.api.robospice.UserSpice;
import com.medicbleep.app.chat.extendables.BaseActivity;
import com.medicbleep.app.chat.extendables.BaseModel;
import com.medicbleep.app.chat.models.UserDetail;
import com.medicbleep.app.chat.services.robospice.CustomSpiceListener;
import com.medicbleep.app.chat.utils.Const;
import com.medicbleep.app.chat.utils.Logger;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.octo.android.robospice.persistence.exception.SpiceException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class SMSVerificationActivity extends BaseActivity {

	public static final String PHONE_NUMBER = "phone_number";
	
	public static final int TYPE_PHONE_NUMBER = 0;
	public static final int TYPE_VERIFICATION_CODE = 1;
	
	private int type;

	EditText editText;
	TextView textView;
	String phoneNumber;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sms_verfication);
		
		editText = (EditText)findViewById(R.id.inputEditText);
		textView = (TextView)findViewById(R.id.phoneNumberTextView);
		
		type = getIntent().getIntExtra(Const.TYPE, TYPE_PHONE_NUMBER);
		if (type == TYPE_PHONE_NUMBER) {
			findViewById(R.id.changePhoneNumberButton).setVisibility(View.GONE);
			textView.setVisibility(View.GONE);
			editText.setHint(R.string.phone_number);
			editText.setOnEditorActionListener(new OnEditorActionListener() {
				@Override
				public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
					if (actionId == EditorInfo.IME_ACTION_DONE) {
						checkPhoneNumber();
					}
					return false;
				}
			});
		}
		else {
			Button changePhoneNumberButton = (Button)findViewById(R.id.changePhoneNumberButton);
			changePhoneNumberButton.setVisibility(View.VISIBLE);
			changePhoneNumberButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					setResult(Activity.RESULT_CANCELED);
					finish();
				}
			});
			findViewById(R.id.phoneNumberTextView).setVisibility(View.VISIBLE);
			editText.setVisibility(View.GONE);
			textView.setVisibility(View.VISIBLE);
			phoneNumber = getIntent().getStringExtra(PHONE_NUMBER);
			textView.setText(getString(R.string.verify_phone_number) + phoneNumber);
		}
		
		Button submitButton = (Button) findViewById(R.id.submitButton);
		submitButton.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				if (type == TYPE_PHONE_NUMBER) {
					submitPhoneNumberAPI();
				}
				else {
					submitVerificationCodeAPI();
				}
			}
		});
	}
	
	void submitPhoneNumberAPI () {
		if (checkPhoneNumber()) {
			Intent intent = new Intent(this, SMSVerificationActivity.class);
			intent.putExtra(Const.TYPE, SMSVerificationActivity.TYPE_VERIFICATION_CODE);
			intent.putExtra(PHONE_NUMBER, editText.getText().toString());
			startActivityForResult(intent, Const.REQUEST_VERIFICATION_CODE);
		}
	}
	
	void submitVerificationCodeAPI () {
		UserSpice.UpdateUserDetails updateDetails = new UserSpice.UpdateUserDetails(phoneNumber);
		spiceManager.execute(updateDetails, new CustomSpiceListener<BaseModel>() {
			@Override
			public void onRequestSuccess(BaseModel arg0) {
				super.onRequestSuccess(arg0);
				Logger.e(arg0.toString());
				setResult(RESULT_OK);
				finish();
			}

			@Override
			public void onRequestFailure(SpiceException arg0) {
				super.onRequestFailure(arg0);
				Logger.e(arg0.getMessage());
			}
		});
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (Const.REQUEST_VERIFICATION_CODE == requestCode) {
			if (resultCode == Activity.RESULT_OK) {
				finish();
			}
			else {
				
			}
		}
	}
	
	@Override
	public void onBackPressed() {
		if (type == TYPE_PHONE_NUMBER) {
			return;
		}
		else {
			super.onBackPressed();
		}
	}
	
	boolean checkPhoneNumber () {
		boolean result = false;
		
		PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
		PhoneNumber phoneNumber;

		String defaultLocale = Locale.getDefault().getCountry();

		String phoneNumberToCheck = editText.getText().toString();

		if (phoneNumberToCheck.startsWith("00")) {
			phoneNumberToCheck = phoneNumberToCheck.replaceFirst("00", "+");
		}

		try {
			phoneNumber = phoneUtil.parse(phoneNumberToCheck, defaultLocale);
			if (phoneUtil.isValidNumber(phoneNumber)) {
				editText.setText(phoneUtil.format(phoneNumber, PhoneNumberFormat.INTERNATIONAL));
				result = true;
			} else {
				result = false;
			}
		} catch (NumberParseException e) {
			e.printStackTrace();
			result = false;
		}
		return result;
		
//		return true;
	}
}

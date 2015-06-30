package com.clover.spika.enterprise.chat;

import java.util.Locale;

import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.PasscodeUtility;
import com.clover.spika.enterprise.chat.views.RobotoThinEditText;
import com.clover.spika.enterprise.chat.views.RobotoThinTextView;
import com.google.i18n.phonenumbers.NumberParseException;
//import com.google.i18n.phonenumbers.PhoneNumberUtil;
//import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
//import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class SMSVerificationActivity extends Activity {

	public static final String PHONE_NUMBER = "phone_number";
	
	public static final int TYPE_PHONE_NUMBER = 0;
	public static final int TYPE_VERIFICATION_CODE = 1;
	
	private int type;
	
	RobotoThinEditText editText;
	RobotoThinTextView textView;
	String phoneNumber;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sms_verfication);
		
		editText = (RobotoThinEditText)findViewById(R.id.inputEditText);
		textView = (RobotoThinTextView)findViewById(R.id.phoneNumberTextView);
		
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
			editText.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					editText.setBackgroundColor(Color.WHITE);
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
			editText.setHint(R.string.verification_code);
			textView.setVisibility(View.VISIBLE);
			phoneNumber = getIntent().getStringExtra(PHONE_NUMBER);
			textView.setText(getString(R.string.sms_sent_to) + phoneNumber);
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
		Intent resultData = new Intent();
		resultData.putExtra(Const.TYPE, PasscodeUtility.getInstance().getTemporaryPasscode());
		setResult(RESULT_OK, resultData);
		finish();
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
		
//		PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
//		PhoneNumber phoneNumber;
//
//		String defaultLocale = Locale.getDefault().getCountry();
//
//		String phoneNumberToCheck = editText.getText().toString();
//
//		if (phoneNumberToCheck.startsWith("00")) {
//			phoneNumberToCheck = phoneNumberToCheck.replaceFirst("00", "+");
//		}
//		
//		try {
//			phoneNumber = phoneUtil.parse(phoneNumberToCheck, defaultLocale);
//			if (phoneUtil.isValidNumber(phoneNumber)) {
//				editText.setText(phoneUtil.format(phoneNumber, PhoneNumberFormat.INTERNATIONAL));
//				editText.setBackgroundColor(Color.GREEN);
//				result = true;
//			} else {
//				editText.setBackgroundColor(Color.RED);
//				result = false;
//			}
//		} catch (NumberParseException e) {
//			e.printStackTrace();
//			editText.setBackgroundColor(Color.RED);
//			result = false;
//		} 
//		return result;
		
		return true;
	}
}

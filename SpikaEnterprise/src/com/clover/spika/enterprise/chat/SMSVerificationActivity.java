package com.clover.spika.enterprise.chat;

import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.PasscodeUtility;
import com.clover.spika.enterprise.chat.views.RobotoThinEditText;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class SMSVerificationActivity extends Activity {

	public static final int TYPE_PHONE_NUMBER = 0;
	public static final int TYPE_VERIFICATION_CODE = 1;
	
	private int type;
	
	RobotoThinEditText editText;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sms_verfication);
		
		editText = (RobotoThinEditText)findViewById(R.id.inputEditText);
		
		type = getIntent().getIntExtra(Const.TYPE, TYPE_PHONE_NUMBER);
		if (type == TYPE_PHONE_NUMBER) {
			findViewById(R.id.changePhoneNumberButton).setVisibility(View.GONE);
			findViewById(R.id.phoneNumberTextView).setVisibility(View.GONE);
			editText.setHint(R.string.phone_number);
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
		Intent intent = new Intent(this, SMSVerificationActivity.class);
		intent.putExtra(Const.TYPE, SMSVerificationActivity.TYPE_VERIFICATION_CODE);
		startActivityForResult(intent, Const.REQUEST_VERIFICATION_CODE);
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
}

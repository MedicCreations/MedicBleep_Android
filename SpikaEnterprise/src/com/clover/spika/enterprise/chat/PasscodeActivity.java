package com.clover.spika.enterprise.chat;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.clover.spika.enterprise.chat.utils.PasscodeUtility;

public class PasscodeActivity extends NewPasscodeActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_passcode);

		ImageButton backButton = (ImageButton) findViewById(R.id.goBack);
		if (backButton != null) {
			backButton.setVisibility(View.GONE);
		}
	}

	/**
	 * Calls for validation logic. Either closes this activity if passcode is
	 * correct or throws an exception.
	 */
	@Override
	protected void validate() {
		if (getEnteredValuesList().size() == MAX_CAPACITY) {
			StringBuilder builder = new StringBuilder();
			for (Integer i : getEnteredValuesList()) {
				builder.append(i);
			}

			if (PasscodeUtility.getInstance().validate(this, builder.toString())) {
				setResult(RESULT_OK);
				finish();
			} else {
				errorValidateAnimation();
				super.reDraw();
			}
		}
	}

}

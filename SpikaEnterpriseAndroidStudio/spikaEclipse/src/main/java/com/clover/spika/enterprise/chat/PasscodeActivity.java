package com.clover.spika.enterprise.chat;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.PasscodeUtility;
import com.clover.spika.enterprise.chat.utils.Utils;

public class PasscodeActivity extends NewPasscodeActivity {

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_passcode);

        if(Utils.isBuildOver(Build.VERSION_CODES.KITKAT_WATCH)){
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
//			window.setNavigationBarColor(getResources().getColor(R.color.default_blue));
            window.setBackgroundDrawableResource(R.drawable.shape_black_hole);
            window.setStatusBarColor(getResources().getColor(R.color.default_blue));
        }

		ImageButton backButton = (ImageButton) findViewById(R.id.goBack);
		if (backButton != null) {
			backButton.setVisibility(View.INVISIBLE);
		}
		
		((TextView)findViewById(R.id.description)).setText(R.string.enter_your_passcode);
	}

	@Override
	protected void onResume() {
		super.onResume();

		Bundle extras = getIntent().getExtras();
		boolean isToOff = false;
		if (extras != null) {
			isToOff = extras.getBoolean(Const.CHANGE_PASSCODE_INTENT, false);
		}

		if (PasscodeUtility.getInstance().isSessionValid() && !isToOff) {
			finish();
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
				PasscodeUtility.getInstance().setSessionValid(true);
				finish();
			} else {
				errorValidateAnimation();
				super.reDraw();
			}
		}
	}
	
	@Override
	public void onBackPressed() {}

}

package com.clover.spika.enterprise.chat;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.clover.spika.enterprise.chat.animation.AnimUtils;
import com.clover.spika.enterprise.chat.utils.Helper;
import com.clover.spika.enterprise.chat.utils.PasscodeUtility;

import java.util.LinkedList;
import java.util.NoSuchElementException;

public class NewPasscodeActivity extends Activity {

    public static final String EXTRA_PASSCODE = "com.cloverstudio.enterprise.chat.NewPasscodeActivity#passcode";

    protected static final int MAX_CAPACITY = 4;

    private LinkedList<Integer> enteredValuesList = new LinkedList<Integer>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passcode);

        ImageButton backButton = (ImageButton) findViewById(R.id.goBack);
        if (backButton != null) {
            backButton.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Triggered when digit button is clicked
     */
    public void onPasscodeExecute(View view) {
        int inserted;
        try {
            inserted = Integer.parseInt(((Button) view).getText().toString());
            insertDigit(inserted);
        } catch (NumberFormatException ignored) {
            removeDigit();
        }

        validate();
    }

    /**
     * Triggered when Back ImageButton is clicked
     */
    public void exitActivity(View view) {
    	PasscodeUtility.getInstance().setTemporaryPasscode(null);
        setResult(RESULT_CANCELED);
        finish();
    }

    protected void insertDigit(int digit) {
        if (enteredValuesList.size() < MAX_CAPACITY) {
            enteredValuesList.add(digit);
        }

        reDraw();
    }

    protected void removeDigit() {
        try {
            enteredValuesList.removeLast();
        } catch (NoSuchElementException ignored) { /* List is empty, do nothing */ }

        reDraw();
    }

    protected LinkedList<Integer> getEnteredValuesList() {
        return this.enteredValuesList;
    }

    /**
     * Redraws circles representing hidden digits.
     */
    protected void reDraw() {
        LinearLayout linearCodeHolder = (LinearLayout) findViewById(R.id.linear_layout_characters_in_passcode);
        for (int i = 0; i < linearCodeHolder.getChildCount(); i++) {
            if (i < enteredValuesList.size()) {
                Helper.setViewBackgroundResource(linearCodeHolder.getChildAt(i), R.drawable.white_circle);
            } else {
                Helper.setViewBackgroundResource(linearCodeHolder.getChildAt(i), R.drawable.circle_with_white_border);
            }
        }
    }

    protected void validate() {
        if (enteredValuesList.size() == 4) {
            StringBuilder builder = new StringBuilder();
            for (Integer i : getEnteredValuesList()) {
                builder.append(i);
            }

            // first attempt
            if (TextUtils.isEmpty(PasscodeUtility.getInstance().getTemporaryPasscode())) {
                PasscodeUtility.getInstance().setTemporaryPasscode(builder.toString());
                enteredValuesList.clear();

                reDraw();
                return;
            }

            // confirmation attempt
            if (builder.toString().equals(PasscodeUtility.getInstance().getTemporaryPasscode())) {
                Intent resultData = new Intent();
                resultData.putExtra(EXTRA_PASSCODE, PasscodeUtility.getInstance().getTemporaryPasscode());
                setResult(RESULT_OK, resultData);

                PasscodeUtility.getInstance().setTemporaryPasscode(null);

                finish();
            } else{
            	//error password
            	errorValidateAnimation();
            }
        }
    }
    
    protected void errorValidateAnimation() {
    	LinearLayout linearCodeHolder = (LinearLayout) findViewById(R.id.linear_layout_characters_in_passcode);
    	AnimUtils.goToLeftThenToRightAndBackInPosition(linearCodeHolder, 50, 100, new AnimatorListenerAdapter() {
    		@Override
    		public void onAnimationEnd(Animator animation) {
    			enteredValuesList.clear();
    			reDraw();
    		}
		});
    	Vibrator vibra = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    	long pattern[]={0,50,50,200};
    	vibra.vibrate(pattern, -1);
	}
}
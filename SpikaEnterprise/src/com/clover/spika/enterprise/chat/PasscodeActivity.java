package com.clover.spika.enterprise.chat;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.clover.spika.enterprise.chat.utils.Helper;
import com.clover.spika.enterprise.chat.utils.PasscodeUtility;

import java.util.LinkedList;
import java.util.NoSuchElementException;

public class PasscodeActivity extends Activity {

    private static final int MAX_CAPACITY = 4;

    private LinkedList<Integer> enteredValuesList = new LinkedList<Integer>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passcode);
    }

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

    private void insertDigit(int digit) {
        if (enteredValuesList.size() < MAX_CAPACITY) {
            enteredValuesList.add(digit);
        }

        reDraw();
    }

    private void removeDigit() {
        try {
            enteredValuesList.removeLast();
        } catch (NoSuchElementException ignored) { /* List is empty, do nothing */ }

        reDraw();
    }

    /**
     * Redraws circles representing hidden digits.
     */
    private void reDraw() {
        LinearLayout linearCodeHolder = (LinearLayout) findViewById(R.id.linear_layout_characters_in_passcode);
        for (int i = 0; i < linearCodeHolder.getChildCount(); i++) {
            if (i < enteredValuesList.size()) {
                Helper.setViewBackgroundResource(linearCodeHolder.getChildAt(i), R.drawable.white_circle);
            } else {
                Helper.setViewBackgroundResource(linearCodeHolder.getChildAt(i), R.drawable.circle_with_white_border);
            }
        }
    }

    /**
     * Calls for validation logic.
     * Either closes this activity if passcode is correct or throws an exception.
     */
    private void validate() {
        if (enteredValuesList.size() == MAX_CAPACITY) {
            StringBuilder builder = new StringBuilder();
            for (Integer i : enteredValuesList) {
                builder.append(i);
            }

            if (PasscodeUtility.getInstance().validate(this, builder.toString())) {
                setResult(RESULT_OK);
                finish();
            } else {
                enteredValuesList.clear();
                reDraw();
            }
        }
    }
}

package com.clover.spika.enterprise.chat;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class PasscodeActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passcode);
    }

    public void onPasscodeExecute(View view) {
        setResult(RESULT_OK);
        finish();
    }
}
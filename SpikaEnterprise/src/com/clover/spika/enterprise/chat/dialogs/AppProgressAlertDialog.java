package com.clover.spika.enterprise.chat.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import com.clover.spika.enterprise.chat.R;

public class AppProgressAlertDialog extends AlertDialog {
	
	TextView dot1;
	TextView dot2;
	TextView dot3;

	Handler handler;
	Runnable run;

	public AppProgressAlertDialog(Context context) {
		super(context, R.style.Theme_Dialog);

		setCancelable(false);
		setCanceledOnTouchOutside(false);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_progress);
		
		dot1 = (TextView) findViewById(R.id.dot1);
		dot2 = (TextView) findViewById(R.id.dot2);
		dot3 = (TextView) findViewById(R.id.dot3);

		handler = new Handler();
		run = new Runnable() {
			public void run() {

				if (dot1.getVisibility() == View.VISIBLE && dot2.getVisibility() == View.VISIBLE && dot3.getVisibility() == View.VISIBLE) {
					dot1.setVisibility(View.INVISIBLE);
					dot2.setVisibility(View.INVISIBLE);
					dot3.setVisibility(View.INVISIBLE);
				} else if (dot1.getVisibility() == View.INVISIBLE && dot2.getVisibility() == View.INVISIBLE && dot3.getVisibility() == View.INVISIBLE) {
					dot1.setVisibility(View.VISIBLE);
					dot2.setVisibility(View.INVISIBLE);
					dot3.setVisibility(View.INVISIBLE);
				} else if (dot1.getVisibility() == View.VISIBLE && dot2.getVisibility() == View.INVISIBLE && dot3.getVisibility() == View.INVISIBLE) {
					dot1.setVisibility(View.VISIBLE);
					dot2.setVisibility(View.VISIBLE);
					dot3.setVisibility(View.INVISIBLE);
				} else if (dot1.getVisibility() == View.VISIBLE && dot2.getVisibility() == View.VISIBLE && dot3.getVisibility() == View.INVISIBLE) {
					dot1.setVisibility(View.VISIBLE);
					dot2.setVisibility(View.VISIBLE);
					dot3.setVisibility(View.VISIBLE);
				}

				handler.postDelayed(run, 500);
			}
		};
	}
	
	@Override
	public void show() {
		super.show();
		handler.postDelayed(run, 500);
	}

	@Override
	protected void onStop() {
		super.onStop();
		handler.removeCallbacks(run);
	}

}

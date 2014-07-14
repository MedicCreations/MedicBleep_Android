package com.clover.spika.enterprise.chat.view;

import com.clover.spika.enterprise.chat.R;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

/**
 * Shows a dialog while loading something, the three dots are animated
 * 
 */
public class AppProgressDialog extends Dialog {

	TextView dot1;
	TextView dot2;
	TextView dot3;

	Handler handler;
	Runnable run;

	public AppProgressDialog(Context context) {
		super(context, R.style.Theme_Transparent);
		setContentView(R.layout.dialog_progress);

		dot1 = (TextView) findViewById(R.id.dot1);
		dot2 = (TextView) findViewById(R.id.dot2);
		dot3 = (TextView) findViewById(R.id.dot3);

		handler = new Handler();
		run = new Runnable() {
			public void run() {

				if (dot1.getVisibility() == View.VISIBLE
						&& dot2.getVisibility() == View.VISIBLE
						&& dot3.getVisibility() == View.VISIBLE) {
					dot1.setVisibility(View.INVISIBLE);
					dot2.setVisibility(View.INVISIBLE);
					dot3.setVisibility(View.INVISIBLE);
				} else if (dot1.getVisibility() == View.INVISIBLE
						&& dot2.getVisibility() == View.INVISIBLE
						&& dot3.getVisibility() == View.INVISIBLE) {
					dot1.setVisibility(View.VISIBLE);
					dot2.setVisibility(View.INVISIBLE);
					dot3.setVisibility(View.INVISIBLE);
				} else if (dot1.getVisibility() == View.VISIBLE
						&& dot2.getVisibility() == View.INVISIBLE
						&& dot3.getVisibility() == View.INVISIBLE) {
					dot1.setVisibility(View.VISIBLE);
					dot2.setVisibility(View.VISIBLE);
					dot3.setVisibility(View.INVISIBLE);
				} else if (dot1.getVisibility() == View.VISIBLE
						&& dot2.getVisibility() == View.VISIBLE
						&& dot3.getVisibility() == View.INVISIBLE) {
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

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		if (!hasFocus) {
			this.dismiss();
		}
		super.onWindowFocusChanged(hasFocus);
	}
}
package com.clover.spika.enterprise.chat.dialogs;

import com.clover.spika.enterprise.chat.R;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Shows a dialog while loading something, the three dots are animated
 * 
 */
public class AppProgressDialogWithBar extends Dialog {

	TextView dot1;
	TextView dot2;
	TextView dot3;
	
	ProgressBar loadingBar;

	Handler handler;
	Runnable run;

	public AppProgressDialogWithBar(Context context) {
		super(context, R.style.Theme_Dialog);
		setContentView(R.layout.dialog_progress_with_bar);

		dot1 = (TextView) findViewById(R.id.dot1);
		dot2 = (TextView) findViewById(R.id.dot2);
		dot3 = (TextView) findViewById(R.id.dot3);
		loadingBar = (ProgressBar) findViewById(R.id.loadingBar);
		loadingBar.setMax(1);

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
	
	public void setMaxBar(int max){
		if(loadingBar == null) return;
		loadingBar.setMax(max);
	}
	
	public int getMaxBar(){
		if(loadingBar == null) return 0;
		return loadingBar.getMax();
	}
	
	public void updateBar(int current){
		if(loadingBar == null) return;
		loadingBar.setProgress(current);
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		if (!hasFocus) {
			this.dismiss();
		}
		super.onWindowFocusChanged(hasFocus);
	}
}
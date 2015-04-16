package com.clover.spika.enterprise.chat.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.clover.spika.enterprise.chat.R;

/**
 * Shows a dialog while loading something, the three dots are animated
 * 
 */
public class AppProgressDialogWithBar extends Dialog {

	Context ctx;

	TextView dot1;
	TextView dot2;
	TextView dot3;
	TextView tvDecrypting;

	ProgressBar loadingBar;

	Animation blink;

	Handler handler;
	Runnable run;

	public AppProgressDialogWithBar(Context context) {
		super(context, R.style.Theme_Dialog);

		this.ctx = context;
		blink = AnimationUtils.loadAnimation(ctx, R.anim.anim_blink);
	}

	public void showProgress() {
		setContentView(R.layout.dialog_progress_with_bar);

		show();
	}

	public void showDecrypting() {
		setContentView(R.layout.dialog_processing_with_decrypt);

		show();
	}

	@Override
	public void onContentChanged() {
		super.onContentChanged();

		dot1 = (TextView) findViewById(R.id.dot1);
		dot2 = (TextView) findViewById(R.id.dot2);
		dot3 = (TextView) findViewById(R.id.dot3);

		loadingBar = (ProgressBar) findViewById(R.id.loadingBar);

		if (loadingBar != null) {
			loadingBar.setMax(1);
		}

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

		handler.postDelayed(run, 500);

		tvDecrypting = (TextView) findViewById(R.id.tvDecrypting);

		if (tvDecrypting != null) {
			tvDecrypting.startAnimation(blink);
		}
	}

	@Override
	public void show() {

		setCancelable(false);
		setCanceledOnTouchOutside(false);

		super.show();
	}

	@Override
	protected void onStop() {
		super.onStop();
		handler.removeCallbacks(run);
	}

	public void setMaxBar(int max) {
		if (loadingBar == null)
			return;
		loadingBar.setMax(max);
	}

	public int getMaxBar() {
		if (loadingBar == null)
			return 0;
		return loadingBar.getMax();
	}

	public void updateBar(int current) {
		if (loadingBar == null)
			return;
		loadingBar.setProgress(current);
	}

}
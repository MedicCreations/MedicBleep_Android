package com.clover.spika.enterprise.chat.extendables;

import java.util.ArrayList;
import java.util.List;

import com.clover.spika.enterprise.chat.ChatActivity;
import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.gcm.PushBroadcastReceiver;
import com.clover.spika.enterprise.chat.models.Push;
import com.clover.spika.enterprise.chat.utils.Const;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class BaseActivity extends SlidingFragmentActivity {

	/* Handling push notifications display */
	List<Push> qPush = new ArrayList<Push>();
	boolean isPushShowing = false;

	PushBroadcastReceiver myPushRecevier;
	IntentFilter intentFilter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setBehindContentView(R.layout.sidebar_layout_empty);

		// start: handle notifications
		if (getIntent().getExtras() != null && getIntent().getExtras().getBoolean(Const.FROM_NOTIFICATION, false)) {
			Intent intent = new Intent(this, ChatActivity.class);
			intent.putExtras(getIntent().getExtras());
			startActivity(intent);
		}

		intentFilter = new IntentFilter(Const.PUSH_INTENT_ACTION);
		myPushRecevier = new PushBroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {

				String message = intent.getExtras().getString(Const.PUSH_MESSAGE);
				String chatId = intent.getExtras().getString(Const.CHAT_ID);
				String chatName = intent.getExtras().getString(Const.CHAT_NAME);
				String chatImage = intent.getExtras().getString(Const.IMAGE);

				pushCall(message, chatId, chatName, chatImage);
			}
		};
		// end: handle notifications
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(myPushRecevier, intentFilter);
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(myPushRecevier);
	}

	public void pushCall(String msg, String chatIdPush, String chatName, String chatImage) {
		showPopUp(msg, chatIdPush, chatName, chatImage);
	}

	/**
	 * Show push notification pop-up message
	 * 
	 * @param msg
	 * @param chatId
	 * @param chatName
	 * @param chatImage
	 */
	public void showPopUp(final String msg, final String chatId, final String chatName, final String chatImage) {

		if (isPushShowing) {
			Push push = new Push();
			push.setId(chatId);
			push.setMessage(msg);
			push.setChatName(chatName);
			push.setChatImage(chatImage);

			qPush.add(push);

			return;
		}

		new BaseAsyncTask<Void, Void, Integer>(this, false) {

			protected void onPreExecute() {
				isPushShowing = true;
			};

			protected Integer doInBackground(Void... paramss) {
				return Const.API_SUCCESS;
			};

			@SuppressLint("InflateParams")
			protected void onPostExecute(Integer result) {
				ViewGroup contentRoot = ((ViewGroup) findViewById(android.R.id.content).getRootView());
				final View view = LayoutInflater.from(context).inflate(R.layout.in_app_notification_layout, null);

				view.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						Intent intent = new Intent(context, ChatActivity.class);
						intent.putExtra(Const.CHAT_ID, chatId);
						intent.putExtra(Const.CHAT_NAME, chatName);
						intent.putExtra(Const.IMAGE, chatImage);
						startActivity(intent);
					}
				});

				TextView text = (TextView) view.findViewById(R.id.msgPop);
				text.setText(msg);

				int pix = 0;
				int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
				if (resourceId > 0) {
					pix = getResources().getDimensionPixelSize(resourceId);
				}

				final RelativeLayout notificationLayout = (RelativeLayout) view.findViewById(R.id.notificationLayout);

				View paddingTopView = (View) view.findViewById(R.id.paddingTopView);
				RelativeLayout.LayoutParams paramsV = (RelativeLayout.LayoutParams) paddingTopView.getLayoutParams();
				paramsV.height = pix;
				paddingTopView.setLayoutParams(paramsV);

				final RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
				params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);

				contentRoot.addView(view, params);

				final Handler handler = new Handler();

				final Animation notAnimIn = AnimationUtils.loadAnimation(context, R.anim.in_app_notification_anim_in);
				final Animation notAnimOut = AnimationUtils.loadAnimation(context, R.anim.in_app_notification_anim_out);

				notAnimIn.setAnimationListener(new AnimationListener() {

					@Override
					public void onAnimationStart(Animation animation) {
						notificationLayout.setVisibility(View.VISIBLE);
					}

					@Override
					public void onAnimationRepeat(Animation animation) {
					}

					@Override
					public void onAnimationEnd(Animation animation) {
						handler.postDelayed(new Runnable() {
							@Override
							public void run() {
								view.startAnimation(notAnimOut);
							}
						}, 3000);
					}
				});

				notAnimOut.setAnimationListener(new AnimationListener() {

					@Override
					public void onAnimationStart(Animation animation) {
					}

					@Override
					public void onAnimationRepeat(Animation animation) {
					}

					@Override
					public void onAnimationEnd(Animation animation) {
						((ViewGroup) findViewById(android.R.id.content).getRootView()).removeView(view);
						isPushShowing = false;

						if (qPush.size() > 0) {
							showPopUp(qPush.get(0).getMessage(), qPush.get(0).getId(), qPush.get(0).getChatName(), qPush.get(0).getChatImage());
							qPush.remove(0);
						}
					}
				});

				view.startAnimation(notAnimIn);
			};
		}.execute();
	}

	@Override
	public void startActivity(Intent intent) {
		super.startActivity(intent);
		overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
	}

	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
	}

	public void hideKeyboard(View et) {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
	}

	public void showKeyboard(EditText et) {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.showSoftInput(et, 0);
	}

	public void showKeyboardForced(EditText et) {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInputFromWindow(et.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED, 0);
		et.requestFocus();
	}

}

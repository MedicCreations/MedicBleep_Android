package com.clover.spika.enterprise.chat.extendables;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.clover.spika.enterprise.chat.ChatActivity;
import com.clover.spika.enterprise.chat.PasscodeActivity;
import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.animation.AnimUtils;
import com.clover.spika.enterprise.chat.lazy.ImageLoader;
import com.clover.spika.enterprise.chat.models.LocalPush;
import com.clover.spika.enterprise.chat.models.User;
import com.clover.spika.enterprise.chat.services.gcm.PushBroadcastReceiver;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.PasscodeUtility;
import com.clover.spika.enterprise.chat.views.RoundImageView;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import com.zzz.socket.models.WebRtcSDPMessage;
import com.zzz.test.socket.SocketService;
import com.zzz.test.socket.SocketService.LocalBinder;
import com.zzz.test.webrtc.CallActivity;
import com.zzz.test.webrtc.ConnectActivity;

public class BaseActivity extends SlidingFragmentActivity {

	/* Handling push notifications display */
	List<LocalPush> qPush = new ArrayList<LocalPush>();
	boolean isPushShowing = false;
	boolean isOpenSearch = false;

	PushBroadcastReceiver myPushRecevier;
	IntentFilter intentFilter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (ImageLoader.getInstance(this) == null) {
			ImageLoader.init(this);
		}

		if (PasscodeUtility.getInstance().isPasscodeEnabled(this)) {
			getWindow().setFlags(LayoutParams.FLAG_SECURE, LayoutParams.FLAG_SECURE);
		}

		setBehindContentView(R.layout.sidebar_layout_empty);

		// start: handle notifications
		if (getIntent().getExtras() != null && getIntent().getExtras().getBoolean(Const.FROM_NOTIFICATION, false)) {
			ChatActivity.startFromNotification(this, getIntent());
		}

		intentFilter = new IntentFilter(Const.PUSH_INTENT_ACTION);
		myPushRecevier = new PushBroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {

				String message = intent.getExtras().getString(Const.PUSH_MESSAGE);
				String chatId = intent.getExtras().getString(Const.CHAT_ID);
				String pushType = intent.getExtras().getString(Const.PUSH_TYPE);
				String password = intent.getExtras().getString(Const.PASSWORD);

				pushCall(message, chatId, pushType, password);
			}
		};
		// end: handle notifications

		getSlidingMenu().setTouchModeBehind(SlidingMenu.TOUCHMODE_NONE);
		getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
	}

	@Override
	protected void onResume() {
		super.onResume();

		LocalBroadcastManager.getInstance(this).registerReceiver(myPushRecevier, intentFilter);

		// passcode callback injected methods are important for tracking active
		// session
		PasscodeUtility.getInstance().onResume();
		if (PasscodeUtility.getInstance().isPasscodeEnabled(this)) {
			if (!PasscodeUtility.getInstance().isSessionValid()) {
				startActivityForResult(new Intent(this, PasscodeActivity.class), Const.PASSCODE_ENTRY_VALIDATION_REQUEST);
			}
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		PasscodeUtility.getInstance().setSessionValid(true);
	}

	@Override
	protected void onPause() {
		super.onPause();
		LocalBroadcastManager.getInstance(this).unregisterReceiver(myPushRecevier);

		// passcode callback injected methods are important for tracking active
		// session
		PasscodeUtility.getInstance().onPause();
	}

	public void pushCall(String msg, String chatIdPush, String pushType, String password) {

		if (Integer.parseInt(pushType) != Const.PUSH_TYPE_SEEN) {
			showPopUp(msg, chatIdPush, password);
			lobbyPushHandle(chatIdPush);
		}
	}

	public void lobbyPushHandle(String chatId) {
	}

	/**
	 * Show push notification pop-up message
	 * 
	 * @param msg
	 * @param chatId
	 * @param chatName
	 * @param chatImage
	 */
	public void showPopUp(final String msg, final String chatId, final String password) {

		if (isPushShowing) {

			LocalPush push = new LocalPush();
			push.setChatId(chatId);
			push.setPassword(password);
			push.setMessage(msg);

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

				final ViewGroup contentRoot = ((ViewGroup) findViewById(android.R.id.content));
				final View view = LayoutInflater.from(context).inflate(R.layout.in_app_notification_layout, null);

				view.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						ChatActivity.startWithChatId(context, chatId, password);
					}
				});

				TextView text = (TextView) view.findViewById(R.id.msgPop);
				text.setText(msg);

				final RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
				params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);

				contentRoot.addView(view, params);

				final Handler handler = new Handler();

				final Animation notAnimIn = AnimationUtils.loadAnimation(context, R.anim.in_app_notification_anim_in);
				final Animation notAnimOut = AnimationUtils.loadAnimation(context, R.anim.in_app_notification_anim_out);

				notAnimIn.setAnimationListener(new AnimationListener() {

					@Override
					public void onAnimationStart(Animation animation) {
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
						contentRoot.removeView(view);
						isPushShowing = false;

						if (qPush.size() > 0) {
							showPopUp(qPush.get(0).getMessage(), qPush.get(0).getChatId(), qPush.get(0).getPassword());
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

	/* HANDLING SEARCH LAYOUTS */
	/**
	 * Set search bar
	 * 
	 * @param listener
	 */
	public void setSearch(ImageButton search, OnClickListener lis, EditText searchEt, OnEditorActionListener editorLis) {

		search.setVisibility(View.VISIBLE);

		search.setOnClickListener(lis);

		searchEt.setOnEditorActionListener(editorLis);
		searchEt.setImeActionLabel("Search", EditorInfo.IME_ACTION_SEARCH);
	}

	/**
	 * Disable search bar
	 */
	public void disableSearch(ImageButton search, EditText searchEt, ImageButton sidebar, final ImageButton closeSearch, TextView title, int width, int animSpeed) {
		disableSearch(search, searchEt, sidebar, closeSearch, title, width, animSpeed, null, null);
	}

	public void disableSearch(ImageButton search, EditText searchEt, ImageButton sidebar, ImageButton closeSearch, TextView title, int width, int animSpeed, ImageButton invite) {
		disableSearch(search, searchEt, sidebar, closeSearch, title, width, animSpeed, invite, null);
	}

	public void disableSearch(ImageButton search, EditText searchEt, ImageButton sidebar, ImageButton closeSearch, TextView title, int width, int animSpeed, LinearLayout layout) {
		disableSearch(search, searchEt, sidebar, closeSearch, title, width, animSpeed, null, layout);
	}

	public void disableSearch(ImageButton search, EditText searchEt, ImageButton sidebar, ImageButton closeSearch, TextView title, int width, int animSpeed, ImageButton invite,
			LinearLayout layout) {

		if (isOpenSearch) {
			closeSearchAnimation(search, sidebar, closeSearch, searchEt, invite, title, width, animSpeed, layout);
		}

		search.setVisibility(View.GONE);
		searchEt.setVisibility(View.GONE);
	}

	protected void openSearchAnimation(final ImageButton search, final ImageButton sidebar, final ImageButton closeSearch, final EditText searchEt, TextView title, int width,
			int animSpeed) {
		openSearchAnimation(search, sidebar, closeSearch, searchEt, null, title, width, animSpeed, null);
	}

	protected void openSearchAnimation(final ImageButton search, final ImageButton sidebar, final ImageButton closeSearch, final EditText searchEt, TextView title, int width,
			int animSpeed, LinearLayout layout) {
		openSearchAnimation(search, sidebar, closeSearch, searchEt, null, title, width, animSpeed, layout);
	}

	protected void openSearchAnimation(final ImageButton search, final ImageButton sidebar, final ImageButton closeSearch, final EditText searchEt, final ImageButton invite,
			TextView title, int width, int animSpeed) {
		openSearchAnimation(search, sidebar, closeSearch, searchEt, invite, title, width, animSpeed, null);
	}

	protected void openSearchAnimation(final ImageButton search, final ImageButton sidebar, final ImageButton closeSearch, final EditText searchEt, final ImageButton invite,
			TextView title, int width, int animSpeed, final LinearLayout layout) {
		search.setClickable(false);
		sidebar.setClickable(false);
		if (invite != null)
			invite.setClickable(false);
		searchEt.setVisibility(View.VISIBLE);

		AnimUtils.translationX(searchEt, width, 0f, animSpeed, new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				super.onAnimationEnd(animation);
				search.setClickable(true);
				sidebar.setClickable(true);
				if (invite != null)
					invite.setClickable(true);
				closeSearch.setVisibility(View.VISIBLE);
				showKeyboardForced(searchEt);
				isOpenSearch = true;
			}
		});
		if (layout != null) {
			AnimUtils.translationX(layout, 0, -(width - layout.getWidth()), animSpeed, null);
		} else {
			AnimUtils.translationX(search, 0, -(width - search.getWidth()), animSpeed, null);
		}
		AnimUtils.fadeAnim(sidebar, 1, 0, animSpeed);
		AnimUtils.translationX(title, 0, -width, animSpeed, null);
	}

	protected void closeSearchAnimation(final ImageButton search, final ImageButton sidebar, final ImageButton closeSearch, final EditText searchEt, TextView title, int width,
			int animSpeed) {
		closeSearchAnimation(search, sidebar, closeSearch, searchEt, null, title, width, animSpeed, null);
	}

	protected void closeSearchAnimation(final ImageButton search, final ImageButton sidebar, final ImageButton closeSearch, final EditText searchEt, TextView title, int width,
			int animSpeed, final LinearLayout layout) {
		closeSearchAnimation(search, sidebar, closeSearch, searchEt, null, title, width, animSpeed, layout);
	}

	protected void closeSearchAnimation(final ImageButton search, final ImageButton sidebar, final ImageButton closeSearch, final EditText searchEt, final ImageButton invite,
			TextView title, int width, int animSpeed) {
		closeSearchAnimation(search, sidebar, closeSearch, searchEt, invite, title, width, animSpeed, null);
	}

	protected void closeSearchAnimation(final ImageButton search, final ImageButton sidebar, final ImageButton closeSearch, final EditText searchEt, final ImageButton invite,
			TextView title, int width, int animSpeed, final LinearLayout layout) {
		search.setClickable(false);
		sidebar.setClickable(false);
		if (invite != null)
			invite.setClickable(false);
		hideKeyboard(searchEt);
		closeSearch.setVisibility(View.GONE);

		AnimUtils.translationX(searchEt, 0f, width, animSpeed, new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				searchEt.setVisibility(View.GONE);
				searchEt.setText("");
				if (invite != null)
					invite.setClickable(true);
				super.onAnimationEnd(animation);
				search.setClickable(true);
				sidebar.setClickable(true);
				isOpenSearch = false;
			}
		});
		if (layout != null) {
			AnimUtils.translationX(layout, -(width - layout.getWidth()), 0, animSpeed, null);
		} else {
			AnimUtils.translationX(search, -(width - search.getWidth()), 0, animSpeed, null);
		}

		AnimUtils.fadeAnim(sidebar, 0, 1, animSpeed);
		AnimUtils.translationX(title, -width, 0, animSpeed, null);
	}
	
	//SOCKET SERVICE
	protected SocketService mService;
	protected boolean mBound = false;
	
	/** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocalBinder binder = (LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            onServiceBaseConnected();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        	mBound = false;
        }
    };
    
    protected void onServiceBaseConnected(){
    	
    };
    
    @Override
    protected void onStart() {
    	super.onStart();
    	Intent intent = new Intent(this, SocketService.class);
    	bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    	
    	intentFilterSocket = new IntentFilter(Const.SOCKET_ACTION);
		LocalBroadcastManager.getInstance(this).registerReceiver(rec, intentFilterSocket);
    };
    
    @Override
	protected void onStop() {
		if (mBound) {
			unbindService(mConnection);
			mBound = false;
		}
		LocalBroadcastManager.getInstance(this).unregisterReceiver(rec);
		super.onStop();
    }
    
    IntentFilter intentFilterSocket;
	BroadcastReceiver rec = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			int typeOfReceiver = intent.getIntExtra(Const.TYPE_OF_SOCKET_RECEIVER, -1);
			if(typeOfReceiver == Const.CHECK_USER_AVAILABLE){
				String typeOfAvailable = intent.getStringExtra(Const.AVAILABLE_TYPE);
				String sessionId = intent.getStringExtra(Const.SESSION_ID);
				
				Log.d("LOG", "SESSION: "+sessionId+", TYPE: "+typeOfAvailable);
				
				if(typeOfAvailable.equals(Const.USER_AVAILABLE)){
					mService.call(sessionId, true);
					updateTextViewAction("Calling");
				}else{
					//TODO show error
				}
			}else if(typeOfReceiver == Const.CALL_USER){
				final String sessionId = intent.getStringExtra(Const.SESSION_ID);
				
				Log.d("LOG", "SESSION: "+sessionId+", TYPE: CALLING");
				
//				new Handler().postDelayed(new Runnable() {
//					
//					@Override
//					public void run() {
//						mService.callCancel(sessionId);
//					}
//				}, 30000);
				
			}else if(typeOfReceiver == Const.CALL_RECEIVE){
				String sessionId = intent.getStringExtra(Const.SESSION_ID);
				User user = (User) intent.getSerializableExtra(Const.USER);
				
				Log.d("LOG", "SESSION: "+sessionId+", TYPE: RINGING");
				mService.callRinging(sessionId);
				
				showCallingPopup(user, sessionId, true, false);
				
			}else if(typeOfReceiver == Const.CALL_ANSWER){
				String sessionId = intent.getStringExtra(Const.SESSION_ID);
				User user = (User) intent.getSerializableExtra(Const.USER);
				
				Log.d("LOG", "SESSION: "+sessionId+", TYPE: ANSWER");
				
				mService.leaveMyRoom();
				
			}else if(typeOfReceiver == Const.CALL_CONNECT){
				Log.d("LOG", "CALL CONNECT");
				
				Intent intent2 = new Intent(BaseActivity.this, com.zzz.my.webrtc.CallActivity.class);
				intent2.putExtra(CallActivity.EXTRA_VIDEO_BITRATE, 322);
				intent2.putExtra(CallActivity.EXTRA_VIDEO_WIDTH, 400);
				intent2.putExtra(CallActivity.EXTRA_VIDEO_HEIGHT, 300);
				intent2.putExtra(CallActivity.EXTRA_VIDEO_FPS, 30);
				intent2.putExtra(CallActivity.EXTRA_RUNTIME, 0);

				startActivityForResult(intent2, 1);
				
//				startActivity(new Intent(BaseActivity.this, ConnectActivity.class));
			}else if(typeOfReceiver == Const.CALL_ENDED){
				updateTextViewAction("Call ended");
				dissmisCallingPopup();
			}else if(typeOfReceiver == Const.CALL_ACCEPTED){
//				WebRtcSDPMessage item = (WebRtcSDPMessage) intent.getSerializableExtra(Const.CANDIDATE);
//				updateTextViewAction("Call accepted");
//				Uri uri = Uri.parse("https://www.spikaent.com:32443");
//				Intent intent2 = new Intent(BaseActivity.this, CallActivity.class);
//				intent2.setData(uri);
//				intent2.putExtra(CallActivity.EXTRA_ROOMID, String.valueOf(item.getArgs().get(0).getPayload().getUser().getId()));
//				intent2.putExtra(CallActivity.EXTRA_LOOPBACK, false);
//				intent2.putExtra(Const.CANDIDATE, item);
//				intent2.putExtra(CallActivity.EXTRA_VIDEO_BITRATE, 322);
//				intent2.putExtra(CallActivity.EXTRA_VIDEO_WIDTH, 400);
//				intent2.putExtra(CallActivity.EXTRA_VIDEO_HEIGHT, 300);
//				intent2.putExtra(CallActivity.EXTRA_VIDEO_FPS, 30);
//				intent2.putExtra(CallActivity.EXTRA_RUNTIME, 0);

//				startActivityForResult(intent2, 1);
			}
			
		}
	};
	
	private View popupCall = null;
	
	protected void updateTextViewAction(String text) {
		if(popupCall == null) return;
		
		TextView tv = (TextView) ((ViewGroup)popupCall).getChildAt(2);
		tv.setText(text);
	}
	
	protected void showCallingPopup(final User user, final String sessionId, final boolean receive, boolean isVideo){
		final ViewGroup contentRoot = ((ViewGroup) findViewById(android.R.id.content));
		popupCall = LayoutInflater.from(this).inflate(R.layout.www_ringing_socker_layout, null);
		
		TextView name = (TextView) popupCall.findViewById(R.id.nameRing);
		name.setText(user.getFirstName() + " " + user.getLastName());
		
		TextView action = (TextView) popupCall.findViewById(R.id.actionRing);
		
		action.setText("Calling");
		if(!receive) action.setText("Connecting");
		
		ImageLoader.getInstance(this).setDefaultImage(R.drawable.default_user_image);
		ImageView image = (ImageView) popupCall.findViewById(R.id.imageRing);
		((RoundImageView)image).setBorderColor(getResources().getColor(R.color.light_light_gray));
		ImageLoader.getInstance(this).displayImage(this, user.getImage(), image);
		
		popupCall.findViewById(R.id.callDeclineRing).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				updateTextViewAction("Call ending");
				mService.callDecline(sessionId);
			}
		});
		
		if(!receive){
			popupCall.findViewById(R.id.callAcceptRing).setVisibility(View.GONE);
			popupCall.findViewById(R.id.videoAcceptRing).setVisibility(View.GONE);
		}
		
		popupCall.findViewById(R.id.callAcceptRing).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				//ACCEPT
//				mService.callAccept(sessionId);
				
				Intent intent2 = new Intent(BaseActivity.this, com.zzz.my.webrtc.CallActivity.class);
				intent2.putExtra(Const.SESSION_ID, sessionId);
				intent2.putExtra(CallActivity.EXTRA_VIDEO_BITRATE, 322);
				intent2.putExtra(CallActivity.EXTRA_VIDEO_WIDTH, 400);
				intent2.putExtra(CallActivity.EXTRA_VIDEO_HEIGHT, 300);
				intent2.putExtra(CallActivity.EXTRA_VIDEO_FPS, 30);
				intent2.putExtra(CallActivity.EXTRA_RUNTIME, 0);

				startActivityForResult(intent2, 1);
				
//				startActivity(new Intent(BaseActivity.this, ConnectActivity.class));
			}
		});

		popupCall.findViewById(R.id.videoAcceptRing).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				//VIDEO ACCEPT
			}
		});
		
		AnimUtils.translationY(popupCall, -getResources().getDisplayMetrics().heightPixels, 0, 300, new AnimatorListenerAdapter() {
			
			@Override
			public void onAnimationEnd(Animator animation) {
				super.onAnimationEnd(animation);
				if(!receive) mService.callOffer(String.valueOf(user.getId()));
			}
			
		});
		
		contentRoot.addView(popupCall);

	}
	
	private void dissmisCallingPopup(){
		AnimUtils.translationY(popupCall, 0, getResources().getDisplayMetrics().heightPixels, 300, new AnimatorListenerAdapter() {
			
			@Override
			public void onAnimationEnd(Animator animation) {
				super.onAnimationEnd(animation);
				if(popupCall != null)((ViewGroup) popupCall.getParent()).removeView(popupCall);
				popupCall = null;
			}
			
		});
	}
	
	public SocketService getService(){
		return mService;
	}
	
}

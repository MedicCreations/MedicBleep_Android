package com.medicbleep.app.chat.extendables;

import java.util.ArrayList;
import java.util.List;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
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
import android.widget.Toast;

import com.medicbleep.app.chat.ChatActivity;
import com.medicbleep.app.chat.LoginActivity;
import com.medicbleep.app.chat.MainActivity;
import com.medicbleep.app.chat.PasscodeActivity;
import com.medicbleep.app.chat.R;
import com.medicbleep.app.chat.animation.AnimUtils;
import com.medicbleep.app.chat.api.robospice.UserSpice;
import com.medicbleep.app.chat.caching.ChatCaching;
import com.medicbleep.app.chat.caching.robospice.BackgroundChatDataCacheSpice;
import com.medicbleep.app.chat.dialogs.AppDialog;
import com.medicbleep.app.chat.dialogs.AppDialog.OnNegativeButtonCLickListener;
import com.medicbleep.app.chat.dialogs.AppDialog.OnPositiveButtonClickListener;
import com.medicbleep.app.chat.dialogs.AppProgressAlertDialog;
import com.medicbleep.app.chat.lazy.ImageLoaderSpice;
import com.medicbleep.app.chat.models.LocalPush;
import com.medicbleep.app.chat.models.User;
import com.medicbleep.app.chat.models.UserWrapper;
import com.medicbleep.app.chat.models.greendao.DaoMaster;
import com.medicbleep.app.chat.models.greendao.DaoSession;
import com.medicbleep.app.chat.services.gcm.PushBroadcastReceiver;
import com.medicbleep.app.chat.services.robospice.CustomSpiceListener;
import com.medicbleep.app.chat.services.robospice.CustomSpiceManager;
import com.medicbleep.app.chat.services.robospice.OkHttpService;
import com.medicbleep.app.chat.services.robospice.SpiceOfflineService;
import com.medicbleep.app.chat.utils.Const;
import com.medicbleep.app.chat.utils.Helper;
import com.medicbleep.app.chat.utils.Logger;
import com.medicbleep.app.chat.utils.PasscodeUtility;
import com.medicbleep.app.chat.utils.Utils;
import com.medicbleep.app.chat.views.RoundImageView;
import com.medicbleep.app.chat.webrtc.CallActivity;
import com.medicbleep.app.chat.webrtc.socket.SocketService;
import com.medicbleep.app.chat.webrtc.socket.SocketService.LocalBinder;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

public class BaseActivity extends SlidingFragmentActivity {

	/* GreenDAO cache */
	private static SQLiteDatabase db;
	private static DaoMaster daoMaster;
	private static DaoSession daoSession;

	/* Handling push notifications display */
	List<LocalPush> qPush = new ArrayList<LocalPush>();
	boolean isPushShowing = false;
	boolean isOpenSearch = false;

	PushBroadcastReceiver myPushRecevier;
	IntentFilter intentFilter;

	protected boolean shouldReceiveBroadcast = true;
	protected boolean isActive = true;

	private View popupCall = null;
	private boolean isVideo = false;
	private boolean gotoCallActivity = false;

	private String activeClass = MainActivity.class.getName();

	protected TextView viewForReturnToCall = null;
	protected TextView viewForNoInternetConnection = null;

	private User tempActiveUser = null;
	private boolean isAllreadyDissmis = false;

	public SpiceManager spiceManager = new CustomSpiceManager(OkHttpService.class);
	public SpiceManager offlineSpiceManager = new CustomSpiceManager(SpiceOfflineService.class);
	private ImageLoaderSpice imageLoaderSpice;
	
	private boolean isPasscodeEnabled = false;

	private IntentFilter intentFilterSocket;
	private IntentFilter intentFilterInternetChangeState = new IntentFilter(Const.INTERNET_CONNECTION_CHANGE_ACTION);

	public ImageLoaderSpice getImageLoader() {
		return imageLoaderSpice;
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		spiceManager.start(this);
		offlineSpiceManager.start(this);

		if (getResources().getBoolean(R.bool.enable_web_rtc)) {
			Intent intent = new Intent(this, SocketService.class);
			bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
		}

		intentFilterSocket = new IntentFilter(Const.SOCKET_ACTION);
		LocalBroadcastManager.getInstance(this).registerReceiver(rec, intentFilterSocket);
	}

	@Override
	protected void onStop() {

		if (mBound) {
			unbindService(mConnection);
			mBound = false;
		}

		LocalBroadcastManager.getInstance(this).unregisterReceiver(rec);
		spiceManager.shouldStop();
		offlineSpiceManager.shouldStop();
		super.onStop();
	}

	private AppProgressAlertDialog progressBar;

	public void handleProgress(boolean showProgress) {

		try {

			if (showProgress) {

				if (progressBar != null && progressBar.isShowing()) {
					progressBar.dismiss();
					progressBar = null;
				}

				progressBar = new AppProgressAlertDialog(this);
				progressBar.show();

			} else {

				if (progressBar != null && progressBar.isShowing()) {
					progressBar.dismiss();
				}

				progressBar = null;
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        setStatusColor();

		/* GreenDAO Singletons */
		if (db == null) {
			db = new DaoMaster.DevOpenHelper(this, "SpikaEnterprise.db", null).getWritableDatabase();
		}
		if (daoMaster == null) {
			daoMaster = new DaoMaster(db);
		}
		if (daoSession == null) {
			daoSession = daoMaster.newSession();
		}

//		/* GreenDAO */
//		DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "SpikaEnterprise.db", null);
//		db = helper.getWritableDatabase();
//		daoMaster = new DaoMaster(db);
//		daoSession = daoMaster.newSession();

		imageLoaderSpice = ImageLoaderSpice.getInstance(this);
		imageLoaderSpice.setSpiceManager(spiceManager);

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
				String messageId = intent.getExtras().getString(Const.MESSAGE_ID);

				pushCall(message, chatId, pushType, password, messageId);
			}
		};
		// end: handle notifications

		getSlidingMenu().setTouchModeBehind(SlidingMenu.TOUCHMODE_NONE);
		getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);

		LocalBroadcastManager.getInstance(this).registerReceiver(internetChageStateRec, intentFilterInternetChangeState);


	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
    protected void setStatusColor(){
        if(Utils.isBuildOver(Build.VERSION_CODES.KITKAT_WATCH)){
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.setBackgroundDrawableResource(R.drawable.shape_black_hole);
            window.setStatusBarColor(getResources().getColor(R.color.default_blue));
        }
    }

	// ***********DEBUG DROP TABLE MESSAGES
	protected void dropAllMessages() {
		daoSession.getMessageDao().deleteAll();
	}
	//***********************
	
	// ***********DEBUG DROP DATABASE
	public void dropDatabase() {
		DaoMaster.dropAllTables(db, true);
		DaoMaster.createAllTables(db, true);
	}

	// ***********************

	public DaoSession getDaoSession() {
		return daoSession;
	}

	protected void setActiveClass(String actClass) {
		activeClass = actClass;
	}

	@Override
	protected void onResume() {
		super.onResume();

		isActive = true;

		LocalBroadcastManager.getInstance(this).registerReceiver(myPushRecevier, intentFilter);

		// passcode callback injected methods are important for tracking active
		// session
		PasscodeUtility.getInstance().onResume();
		if (PasscodeUtility.getInstance().isPasscodeEnabled(this)) {
			isPasscodeEnabled = true;
			startTimeout();
			if (!PasscodeUtility.getInstance().isSessionValid()) {
				startActivityForResult(new Intent(this, PasscodeActivity.class), Const.PASSCODE_ENTRY_VALIDATION_REQUEST);
			}
		}else{
			isPasscodeEnabled = false;
		}

		ChatCaching.updateTimestamps(this);

		Bundle extras = getIntent().getExtras();

		if (extras != null) {

			boolean outsideStart = extras.getBoolean("medic_bleep_outside_start", false);

			if (outsideStart && PasscodeUtility.getInstance().isSessionValid()) {

				final String username = extras.getString("medic_bleep_email");
				final String password = extras.getString("medic_bleep_password");
				final String ocrUserId = String.valueOf(extras.getInt("ocr_user_id"));

				Logger.e("CHAT: " + extras.toString() + "\n" + username + "\n" + password + "\n" + ocrUserId);

				getIntent().removeExtra("medic_bleep_outside_start");
				getIntent().removeExtra("medic_bleep_email");
				getIntent().removeExtra("medic_bleep_password");
				getIntent().removeExtra("ocr_user_id");

				if (username.length() == 0 || password.length() == 0){
					Toast.makeText(getApplicationContext(), "Missing login parameter", Toast.LENGTH_LONG).show();
				}
				else {
					spiceManager.execute(new UserSpice.GetOcrUser(ocrUserId), new RequestListener<UserWrapper>() {
						@Override
						public void onRequestFailure(SpiceException e) {
							Logger.e("USER ID FAILED");
						}

						@Override
						public void onRequestSuccess(UserWrapper userWrapper) {
							Logger.e("USER ID COOLIO\n" + userWrapper.toString());
							User user = userWrapper.user;

							String userLoggedInEmail = Helper.getUserEmail();

							if (username.equals(userLoggedInEmail)) {
								if (user != null) {
									ChatActivity.startWithUserId(BaseActivity.this, String.valueOf(user.id), false, user.firstname, user.lastname, user);
								}
								else if (!ocrUserId.equals("0")) {
									AppDialog dialog = new AppDialog(BaseActivity.this, false);
									dialog.setFailed(getResources().getString(R.string.e_user_not_found));
								}
							}
							else {
								handleProgress(true);
								UserSpice.Logout logout = new UserSpice.Logout();
								spiceManager.execute(logout, new CustomSpiceListener<BaseModel>() {

									@Override
									public void onRequestFailure(SpiceException arg0) {
										super.onRequestFailure(arg0);
										handleProgress(false);
										new AppDialog(BaseActivity.this, false).setFailed(getResources().getString(R.string.e_error_while_logout));
									}

									@Override
									public void onRequestSuccess(BaseModel arg0) {
										super.onRequestSuccess(arg0);
										handleProgress(false);
//										Helper.logout(BaseActivity.this);

										SpikaEnterpriseApp.getSharedPreferences().clear();
										Intent logoutIntent = new Intent(BaseActivity.this, LoginActivity.class);
										logoutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

										logoutIntent.putExtra("medic_bleep_outside_start", true);
										logoutIntent.putExtra("medic_bleep_email", username);
										logoutIntent.putExtra("medic_bleep_password", password);
										logoutIntent.putExtra("ocr_user_id", Integer.valueOf(ocrUserId));

										SpikaEnterpriseApp.stopSocket();
										BaseActivity.this.startActivity(logoutIntent);
										((Activity) BaseActivity.this).finish();

										finish();
									}
								});

							}
						}
					});
				}
			}
		}
	}

	@Override
	protected void onDestroy() {
		LocalBroadcastManager.getInstance(this).unregisterReceiver(internetChageStateRec);
		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		PasscodeUtility.getInstance().setSessionValid(true);

		updateTextViewAction("Call ended");
		if (requestCode == Const.CALL_ACTIVITY_REQUEST) {
			new Handler().postDelayed(new Runnable() {

				@Override
				public void run() {
					dissmisCallingPopup();
				}
			}, 1000);
		}
	}

	@Override
	protected void onPause() {
		if (popupCall != null && !gotoCallActivity && !SpikaEnterpriseApp.isCallInBackground()) {
			if (callTimeoutRunnable != null)
				callTimeoutHandler.removeCallbacks(callTimeoutRunnable);
			updateTextViewAction("Call ending");
			mService.callDecline(null);
			callEnded();
		}
		gotoCallActivity = false;
		super.onPause();
		isActive = false;
		LocalBroadcastManager.getInstance(this).unregisterReceiver(myPushRecevier);

		// passcode callback injected methods are important for tracking active
		// session
		PasscodeUtility.getInstance().onPause();
		stopTimeout();
	}

	public void pushCall(String msg, String chatIdPush, String pushType, String password, String messageId) {
		try {
			if (Integer.parseInt(pushType) != Const.PUSH_TYPE_SEEN) {
				showPopUp(msg, chatIdPush, password);
				lobbyPushHandle(chatIdPush);
				handleNewPushMessageInBackground(chatIdPush, messageId);
			}
		}
		catch (NumberFormatException e) {
			Log.e(this.getClass().toString(), e.getMessage());
		}
	}

	protected void handleNewPushMessageInBackground(final String chatIdPush, String messageId) {
        boolean isChatActive = false;
		BackgroundChatDataCacheSpice.GetData spice = new BackgroundChatDataCacheSpice.GetData(daoSession, spiceManager, chatIdPush, messageId, isChatActive, null);
		spiceManager.execute(spice, new CustomSpiceListener<Integer>());
	}


	public void lobbyPushHandle(String chatId) {
	}

	/**
	 * Show push notification pop-up message
	 * 
	 * @param msg
	 * @param chatId
	 */
	public void showPopUp(final String msg, final String chatId, final String password) {

		if (isPushShowing) {

			LocalPush push = new LocalPush();
			push.chat_id = chatId;
			push.password = password;
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
						ChatActivity.startWithChatIdNoModel(context, chatId, password);
					}
				});

				TextView text = (TextView) view.findViewById(R.id.msgPop);
				text.setText(msg);

				final RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
						RelativeLayout.LayoutParams.WRAP_CONTENT);
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
							showPopUp(qPush.get(0).getMessage(), qPush.get(0).chat_id, qPush.get(0).password);
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
	public void startActivityForResult(Intent intent, int requestCode) {
		super.startActivityForResult(intent, requestCode);
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
	public void disableSearch(ImageButton search, EditText searchEt, ImageButton sidebar, final ImageButton closeSearch, TextView title, int width,
			int animSpeed) {
		disableSearch(search, searchEt, sidebar, closeSearch, title, width, animSpeed, null, null);
	}

	public void disableSearch(ImageButton search, EditText searchEt, ImageButton sidebar, ImageButton closeSearch, TextView title, int width,
			int animSpeed, ImageButton invite) {
		disableSearch(search, searchEt, sidebar, closeSearch, title, width, animSpeed, invite, null);
	}

	public void disableSearch(ImageButton search, EditText searchEt, ImageButton sidebar, ImageButton closeSearch, TextView title, int width,
			int animSpeed, LinearLayout layout) {
		disableSearch(search, searchEt, sidebar, closeSearch, title, width, animSpeed, null, layout);
	}

	public void disableSearch(ImageButton search, EditText searchEt, ImageButton sidebar, ImageButton closeSearch, TextView title, int width,
			int animSpeed, ImageButton invite, LinearLayout layout) {

		if (isOpenSearch) {
			closeSearchAnimation(search, sidebar, closeSearch, searchEt, invite, title, width, animSpeed, layout);
		}

		search.setVisibility(View.GONE);
		searchEt.setVisibility(View.GONE);
	}

	protected void openSearchAnimation(final ImageButton search, final ImageButton sidebar, final ImageButton closeSearch, final EditText searchEt,
			TextView title, int width, int animSpeed) {
		openSearchAnimation(search, sidebar, closeSearch, searchEt, null, title, width, animSpeed, null);
	}

	protected void openSearchAnimation(final ImageButton search, final ImageButton sidebar, final ImageButton closeSearch, final EditText searchEt,
			TextView title, int width, int animSpeed, LinearLayout layout) {
		openSearchAnimation(search, sidebar, closeSearch, searchEt, null, title, width, animSpeed, layout);
	}

	protected void openSearchAnimation(final ImageButton search, final ImageButton sidebar, final ImageButton closeSearch, final EditText searchEt,
			final ImageButton invite, TextView title, int width, int animSpeed) {
		openSearchAnimation(search, sidebar, closeSearch, searchEt, invite, title, width, animSpeed, null);
	}

	protected void openSearchAnimation(final ImageButton search, final ImageButton sidebar, final ImageButton closeSearch, final EditText searchEt,
			final ImageButton invite, TextView title, int width, int animSpeed, final LinearLayout layout) {
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

	protected void closeSearchAnimation(final ImageButton search, final ImageButton sidebar, final ImageButton closeSearch, final EditText searchEt,
			TextView title, int width, int animSpeed) {
		closeSearchAnimation(search, sidebar, closeSearch, searchEt, null, title, width, animSpeed, null);
	}

	protected void closeSearchAnimation(final ImageButton search, final ImageButton sidebar, final ImageButton closeSearch, final EditText searchEt,
			TextView title, int width, int animSpeed, final LinearLayout layout) {
		closeSearchAnimation(search, sidebar, closeSearch, searchEt, null, title, width, animSpeed, layout);
	}

	protected void closeSearchAnimation(final ImageButton search, final ImageButton sidebar, final ImageButton closeSearch, final EditText searchEt,
			final ImageButton invite, TextView title, int width, int animSpeed) {
		closeSearchAnimation(search, sidebar, closeSearch, searchEt, invite, title, width, animSpeed, null);
	}

	protected void closeSearchAnimation(final ImageButton search, final ImageButton sidebar, final ImageButton closeSearch, final EditText searchEt,
			final ImageButton invite, TextView title, int width, int animSpeed, final LinearLayout layout) {
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

	// SOCKET SERVICE
	protected SocketService mService;
	protected boolean mBound = false;

	/** Defines callbacks for service binding, passed to bindService() */
	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			// We've bound to LocalService, cast the IBinder and get
			// LocalService instance
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

	protected void onServiceBaseConnected() {

	};

	BroadcastReceiver rec = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (!shouldReceiveBroadcast)
				return;
			int typeOfReceiver = intent.getIntExtra(Const.TYPE_OF_SOCKET_RECEIVER, -1);
			OnPositiveButtonClickListener positiveListener = new OnPositiveButtonClickListener() {

				@Override
				public void onPositiveButtonClick(View v, Dialog d) {
					d.dismiss();
					openRecordActivity(tempActiveUser);
				}
			};
			OnNegativeButtonCLickListener negativeListener = new OnNegativeButtonCLickListener() {

				@Override
				public void onNegativeButtonClick(View v, Dialog d) {
					d.dismiss();
				}
			};
			OnDismissListener dissmisListener = new OnDismissListener() {

				@Override
				public void onDismiss(DialogInterface dialog) {
					callEnded();
				}
			};
			if (typeOfReceiver == Const.CHECK_USER_AVAILABLE) {
				String typeOfAvailable = intent.getStringExtra(Const.AVAILABLE_TYPE);
				String sessionId = intent.getStringExtra(Const.SESSION_ID);
				if (typeOfAvailable.equals(Const.USER_AVAILABLE)) {
					mService.call(sessionId, true);
					updateTextViewAction("Calling");
				} else if (typeOfAvailable.equals(Const.USER_NOT_CONNECTED)) {
					if (callTimeoutRunnable != null)
						callTimeoutHandler.removeCallbacks(callTimeoutRunnable);
					if (mPlayer != null)
						mPlayer.stop();
					AppDialog dialog = new AppDialog(BaseActivity.this, false);
					dialog.setYesNo(context.getString(R.string.user_is_not_available_at_the_moment_do_you_want_to_leave_voice_message_), "Yes", "No");
					dialog.setOnPositiveButtonClick(positiveListener);
					dialog.setOnNegativeButtonClick(negativeListener);
					dialog.setOnDismissListener(dissmisListener);
				} else {
					if (callTimeoutRunnable != null)
						callTimeoutHandler.removeCallbacks(callTimeoutRunnable);
					if (mPlayer != null)
						mPlayer.stop();
					AppDialog dialog = new AppDialog(BaseActivity.this, false);
					dialog.setYesNo(context.getString(R.string.user_is_not_available_at_the_moment_do_you_want_to_leave_voice_message_), "Yes", "No");
					dialog.setOnPositiveButtonClick(positiveListener);
					dialog.setOnNegativeButtonClick(negativeListener);
					dialog.setOnDismissListener(dissmisListener);
				}
			} else if (typeOfReceiver == Const.CALL_USER) {

				Logger.custom("d", "LOG", "CALLING");

			} else if (typeOfReceiver == Const.CALL_RECEIVE) {
				String sessionId = intent.getStringExtra(Const.SESSION_ID);
				User user = (User) intent.getSerializableExtra(Const.USER);
				Logger.custom("d", "LOG", "RINGING");

				mService.callRinging(sessionId);
				showCallingPopup(user, sessionId, true, false);

			} else if (typeOfReceiver == Const.CALL_ANSWER) {

				Logger.custom("d", "LOG", "ANSWER");
				mService.leaveMyRoom();

			} else if (typeOfReceiver == Const.CALL_CONNECT) {
				Logger.custom("d", "LOG", "CALL CONNECT");
				if (callTimeoutRunnable != null)
					callTimeoutHandler.removeCallbacks(callTimeoutRunnable);
				if (mPlayer != null)
					mPlayer.stop();

				User user = (User) intent.getSerializableExtra(Const.USER);

				Intent intent2 = new Intent(BaseActivity.this, CallActivity.class);
				intent2.putExtra(CallActivity.EXTRA_VIDEO_BITRATE, 1000);
				intent2.putExtra(CallActivity.EXTRA_VIDEO_WIDTH, 400);
				intent2.putExtra(CallActivity.EXTRA_VIDEO_HEIGHT, 300);
				intent2.putExtra(CallActivity.EXTRA_VIDEO_FPS, 30);
				intent2.putExtra(CallActivity.EXTRA_RUNTIME, 0);
				intent2.putExtra(Const.ACTIVE_CLASS, activeClass);
				intent2.putExtra(Const.IS_VIDEO_ACCEPT, isVideo);
				intent2.putExtra(Const.USER, user);

				gotoCallActivity = true;

				startActivityForResult(intent2, Const.CALL_ACTIVITY_REQUEST);

			} else if (typeOfReceiver == Const.CALL_RINGING) {
				Logger.custom("d", "LOG", "CALL RINGING");
				updateTextViewAction("Ringing");
				mPlayer = MediaPlayer.create(BaseActivity.this, R.raw.ringing_voice);
				mPlayer.start();
			} else if (typeOfReceiver == Const.CALL_CANCELED) {
				if (callTimeoutRunnable != null)
					callTimeoutHandler.removeCallbacks(callTimeoutRunnable);
				if (mPlayer != null)
					mPlayer.stop();
				Logger.custom("d", "LOG", "CALL CANCELED");
				updateTextViewAction("Call Canceled");
				new Handler().postDelayed(new Runnable() {

					@Override
					public void run() {
						callEnded();
					}
				}, 500);
			} else if (typeOfReceiver == Const.CALL_ENDED) {
				if (callTimeoutRunnable != null)
					callTimeoutHandler.removeCallbacks(callTimeoutRunnable);
				if (mPlayer != null && mPlayer.isPlaying()) {
					AppDialog dialog = new AppDialog(BaseActivity.this, false);
					dialog.setYesNo(context.getString(R.string.user_is_not_available_at_the_moment_do_you_want_to_leave_voice_message_), "Yes", "No");
					dialog.setOnPositiveButtonClick(positiveListener);
					dialog.setOnNegativeButtonClick(negativeListener);
					dialog.setOnDismissListener(dissmisListener);
				}
				if (mPlayer != null)
					mPlayer.stop();
				Logger.custom("d", "LOG", "CALL ENDED");
				callEnded();
			} else if (typeOfReceiver == Const.CALL_ACCEPTED) {
				Logger.custom("d", "LOG", "CALL ACCEPTED");
				// LOGIC WHEN CALL IS ACCEPTED IS IN CALL ACTIVITY
			} else if (typeOfReceiver == Const.WEB_SOCKET_OPENED) {
				webSocketOpenedCallback();
			}

		}
	};

	protected void openRecordActivity(User tempActiveUser2) {
		// Overide this in activities
	}

	protected void webSocketOpenedCallback() {
		// Overide this in activities
	}

	protected void callEnded() {
		if (viewForReturnToCall != null) {
			Intent intent = new Intent(BaseActivity.this, CallActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			intent.putExtra(Const.IS_CALL_ACTIVE, true);
			intent.putExtra(Const.TYPE_OF_SOCKET_RECEIVER, Const.CALL_ENDED);
			startActivity(intent);

			showPopupCall();
			((ViewGroup) viewForReturnToCall.getParent()).removeView(viewForReturnToCall);
			viewForReturnToCall = null;
			return;
		}
		updateTextViewAction("Call ended");
		dissmisCallingPopup();
	}

	protected void updateTextViewAction(String text) {
		if (popupCall == null)
			return;

		TextView tv = (TextView) ((ViewGroup) popupCall).getChildAt(2);
		tv.setText(text);
	}

	private Handler callTimeoutHandler = new Handler();
	private Runnable callTimeoutRunnable;
	private MediaPlayer mPlayer = null;

	@SuppressLint("InflateParams")
	protected void showCallingPopup(final User user, final String sessionId, final boolean receive, boolean isVideo) {
		tempActiveUser = user;
		this.isVideo = isVideo;
		final ViewGroup contentRoot = ((ViewGroup) findViewById(android.R.id.content));
		popupCall = LayoutInflater.from(this).inflate(R.layout.www_ringing_socker_layout, null);

		TextView name = (TextView) popupCall.findViewById(R.id.nameRing);
		name.setText(user.getFirstName() + " " + user.getLastName());

		TextView action = (TextView) popupCall.findViewById(R.id.actionRing);

		action.setText("Calling");
		if (!receive)
			action.setText("Connecting");

		ImageView image = (ImageView) popupCall.findViewById(R.id.imageRing);
		((RoundImageView) image).setBorderColor(getResources().getColor(R.color.light_light_gray));
		imageLoaderSpice.displayImage(image, user.getImage(), R.drawable.default_user_image);

		popupCall.findViewById(R.id.callDeclineRing).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mPlayer != null)
					mPlayer.stop();

				if (callTimeoutRunnable != null)
					callTimeoutHandler.removeCallbacks(callTimeoutRunnable);
				updateTextViewAction("Call ending");
				mService.callDecline(sessionId);
				callEnded();
			}
		});

		if (!receive) {
			popupCall.findViewById(R.id.callAcceptRing).setVisibility(View.GONE);
			popupCall.findViewById(R.id.videoAcceptRing).setVisibility(View.GONE);
		}

		popupCall.findViewById(R.id.callAcceptRing).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// ACCEPT
				if (mPlayer != null)
					mPlayer.stop();

				Intent intent2 = new Intent(BaseActivity.this, CallActivity.class);
				intent2.putExtra(Const.SESSION_ID, sessionId);
				intent2.putExtra(CallActivity.EXTRA_VIDEO_BITRATE, 322);
				intent2.putExtra(CallActivity.EXTRA_VIDEO_WIDTH, 400);
				intent2.putExtra(CallActivity.EXTRA_VIDEO_HEIGHT, 300);
				intent2.putExtra(CallActivity.EXTRA_VIDEO_FPS, 30);
				intent2.putExtra(CallActivity.EXTRA_RUNTIME, 0);
				intent2.putExtra(Const.ACTIVE_CLASS, activeClass);
				intent2.putExtra(Const.IS_VIDEO_ACCEPT, false);

				gotoCallActivity = true;

				startActivityForResult(intent2, Const.CALL_ACTIVITY_REQUEST);

			}
		});

		popupCall.findViewById(R.id.videoAcceptRing).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mPlayer != null)
					mPlayer.stop();

				Intent intent2 = new Intent(BaseActivity.this, CallActivity.class);
				intent2.putExtra(Const.SESSION_ID, sessionId);
				intent2.putExtra(CallActivity.EXTRA_VIDEO_BITRATE, 322);
				intent2.putExtra(CallActivity.EXTRA_VIDEO_WIDTH, 400);
				intent2.putExtra(CallActivity.EXTRA_VIDEO_HEIGHT, 300);
				intent2.putExtra(CallActivity.EXTRA_VIDEO_FPS, 30);
				intent2.putExtra(CallActivity.EXTRA_RUNTIME, 0);
				intent2.putExtra(Const.ACTIVE_CLASS, activeClass);
				intent2.putExtra(Const.IS_VIDEO_ACCEPT, true);

				gotoCallActivity = true;

				startActivityForResult(intent2, Const.CALL_ACTIVITY_REQUEST);
			}
		});

		AnimUtils.translationY(popupCall, -getResources().getDisplayMetrics().heightPixels, 0, 300, new AnimatorListenerAdapter() {

			@Override
			public void onAnimationEnd(Animator animation) {
				super.onAnimationEnd(animation);
				if (!receive){
                    boolean isSuccess = mService.callOffer(String.valueOf(user.getId()));
                    if(!isSuccess) {
                        callTimeoutHandler.removeCallbacks(callTimeoutRunnable);
                        mService.callCancel(sessionId);
                        if (mPlayer != null)
                            mPlayer.stop();

                        AppDialog dialog = new AppDialog(BaseActivity.this, false);
                        dialog.setYesNo(tempActiveUser.getFirstName() + " " + tempActiveUser.getLastName()
                                + getString(R.string._didn_t_answer_on_your_call_do_you_want_to_leave_voice_message_), "Yes", "No");
                        OnPositiveButtonClickListener positiveListener = new OnPositiveButtonClickListener() {

                            @Override
                            public void onPositiveButtonClick(View v, Dialog d) {
                                d.dismiss();
                                openRecordActivity(tempActiveUser);
                            }
                        };
                        OnNegativeButtonCLickListener negativeListener = new OnNegativeButtonCLickListener() {

                            @Override
                            public void onNegativeButtonClick(View v, Dialog d) {
                                d.dismiss();
                            }
                        };
                        OnDismissListener dissmisListener = new OnDismissListener() {

                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                callEnded();
                            }
                        };
                        dialog.setOnPositiveButtonClick(positiveListener);
                        dialog.setOnNegativeButtonClick(negativeListener);
                        dialog.setOnDismissListener(dissmisListener);
                    }
                }
					mService.callOffer(String.valueOf(user.getId()));
			}

		});

		contentRoot.addView(popupCall);

		if (receive) {
			mPlayer = MediaPlayer.create(this, R.raw.ringing_voice);
			mPlayer.start();
			return;
		}
		callTimeoutRunnable = new Runnable() {

			@Override
			public void run() {
				mService.callCancel(sessionId);
				if (mPlayer != null)
					mPlayer.stop();
				AppDialog dialog = new AppDialog(BaseActivity.this, false);
				dialog.setYesNo(tempActiveUser.getFirstName() + " " + tempActiveUser.getLastName()
						+ getString(R.string._didn_t_answer_on_your_call_do_you_want_to_leave_voice_message_), "Yes", "No");
				OnPositiveButtonClickListener positiveListener = new OnPositiveButtonClickListener() {

					@Override
					public void onPositiveButtonClick(View v, Dialog d) {
						d.dismiss();
						openRecordActivity(tempActiveUser);
					}
				};
				OnNegativeButtonCLickListener negativeListener = new OnNegativeButtonCLickListener() {

					@Override
					public void onNegativeButtonClick(View v, Dialog d) {
						d.dismiss();
					}
				};
				OnDismissListener dissmisListener = new OnDismissListener() {

					@Override
					public void onDismiss(DialogInterface dialog) {
						callEnded();
					}
				};
				dialog.setOnPositiveButtonClick(positiveListener);
				dialog.setOnNegativeButtonClick(negativeListener);
				dialog.setOnDismissListener(dissmisListener);
			}
		};
		callTimeoutHandler.postDelayed(callTimeoutRunnable, Const.TIMEOUT_FOR_CALL);
	}

	protected void hidePopupCall() {
		if (popupCall != null)
			popupCall.setVisibility(View.INVISIBLE);
	}

	protected void showPopupCall() {
		if (popupCall != null)
			popupCall.setVisibility(View.VISIBLE);
	}

	protected void setViewWhenCallIsInBackground(final int idFoBaseView, int idOfActionLayout, boolean isMainActivity) {
		hidePopupCall();

		viewForReturnToCall = new TextView(this);
		viewForReturnToCall.setBackgroundColor(getResources().getColor(R.color.green_in_people_row));
		viewForReturnToCall.setText(getString(R.string.touch_to_return_to_call));
		viewForReturnToCall.setTextColor(Color.WHITE);
		viewForReturnToCall.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
		viewForReturnToCall.setGravity(Gravity.CENTER);
		((ViewGroup) findViewById(idFoBaseView)).addView(viewForReturnToCall);
		viewForReturnToCall.getLayoutParams().height = (int) getResources().getDimension(R.dimen.menu_height);
		viewForReturnToCall.getLayoutParams().width = android.widget.RelativeLayout.LayoutParams.MATCH_PARENT;
		viewForReturnToCall.setId(1);

		((android.widget.RelativeLayout.LayoutParams) findViewById(idOfActionLayout).getLayoutParams()).addRule(RelativeLayout.BELOW,
				viewForReturnToCall.getId());

		viewForReturnToCall.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(BaseActivity.this, CallActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				intent.putExtra(Const.IS_CALL_ACTIVE, true);
				startActivity(intent);

				showPopupCall();
				((ViewGroup) findViewById(idFoBaseView)).removeView(viewForReturnToCall);
				viewForReturnToCall = null;
			}
		});
	}

	protected void setViewNoInternetConnection(final int idFoBaseView, int idOfActionLayout) {
		if (viewForNoInternetConnection != null) {
			return;
		}
		viewForNoInternetConnection = new TextView(this);
		viewForNoInternetConnection.setBackgroundColor(getResources().getColor(R.color.red));
		viewForNoInternetConnection.setText(getString(R.string.no_internet_connection_));
		viewForNoInternetConnection.setTextColor(Color.WHITE);
		viewForNoInternetConnection.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
		viewForNoInternetConnection.setGravity(Gravity.CENTER);
		((ViewGroup) findViewById(idFoBaseView)).addView(viewForNoInternetConnection);
		viewForNoInternetConnection.getLayoutParams().height = (int) getResources().getDimension(R.dimen.menu_height);
		viewForNoInternetConnection.getLayoutParams().width = android.widget.RelativeLayout.LayoutParams.MATCH_PARENT;
		viewForNoInternetConnection.setId(2);

		((android.widget.RelativeLayout.LayoutParams) findViewById(idOfActionLayout).getLayoutParams()).addRule(RelativeLayout.BELOW,
				viewForNoInternetConnection.getId());
	}

	protected void removeViewNoInternetConnection() {
		((ViewGroup) viewForNoInternetConnection.getParent()).removeView(viewForNoInternetConnection);
		viewForNoInternetConnection = null;
	}

	protected BroadcastReceiver internetChageStateRec = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getIntExtra(Const.INTERNET_STATE, Const.HAS_NOT_INTERNET) == Const.HAS_INTERNET) {
				if (viewForNoInternetConnection != null) {
					((ViewGroup) viewForNoInternetConnection.getParent()).removeView(viewForNoInternetConnection);
					viewForNoInternetConnection = null;
				}
			}
		}
	};

	private void dissmisCallingPopup() {
		if (popupCall == null)
			return;
		if (isAllreadyDissmis)
			return;
		isAllreadyDissmis = true;
		AnimUtils.translationY(popupCall, 0, getResources().getDisplayMetrics().heightPixels, 300, new AnimatorListenerAdapter() {

			@Override
			public void onAnimationEnd(Animator animation) {
				super.onAnimationEnd(animation);
				if (popupCall != null)
					((ViewGroup) popupCall.getParent()).removeView(popupCall);
				popupCall = null;
				isAllreadyDissmis = false;
			}

		});
	}

	public SocketService getService() {
		return mService;
	}

	public void callUser(User user, boolean isVideo) {
		if (SpikaEnterpriseApp.isCallInBackground()) {
			AppDialog dialog = new AppDialog(this, false);
			dialog.setInfo(getString(R.string.other_call_is_in_progress));
			return;
		}
		showCallingPopup(user, null, false, isVideo);
	}
	
	//timeout for passcode activity
	@Override
	public void onUserInteraction() {
		super.onUserInteraction();
		startTimeout();
	}
	
	private static Handler timeoutForPasscodeHandler;
	private Runnable timeoutForPasscodeRunnable = new Runnable() {
		
		@Override
		public void run() {
			Logger.custom("i", "TIMEOUT", "ACTIVATE");
			if (PasscodeUtility.getInstance().isPasscodeEnabled(BaseActivity.this)) {
				PasscodeUtility.getInstance().setSessionValid(false);
				startActivityForResult(new Intent(BaseActivity.this, PasscodeActivity.class), Const.PASSCODE_ENTRY_VALIDATION_REQUEST);
			}
		}
	};
	
	private void startTimeout(){
		if (timeoutForPasscodeHandler == null) {
			timeoutForPasscodeHandler = new Handler();
		}
		timeoutForPasscodeHandler.removeCallbacksAndMessages(null);
		if(!isPasscodeEnabled) return;
		Logger.custom("i", "TIMEOUT", "START");
		timeoutForPasscodeHandler.postDelayed(timeoutForPasscodeRunnable, Const.PASSCODE_TIMEOUT_TIME);
	}
	
	private void stopTimeout(){
		if(!isPasscodeEnabled) return;
		Logger.custom("i", "TIMEOUT", "STOP");
		timeoutForPasscodeHandler.removeCallbacksAndMessages(null);
	}

}

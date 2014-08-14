package com.clover.spika.enterprise.chat;

import java.util.ArrayList;
import java.util.List;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.clover.spika.enterprise.chat.animation.AnimUtils;
import com.clover.spika.enterprise.chat.extendables.BaseAsyncTask;
import com.clover.spika.enterprise.chat.fragments.LobbyFragment;
import com.clover.spika.enterprise.chat.fragments.ProfileFragment;
import com.clover.spika.enterprise.chat.fragments.SidebarFragment;
import com.clover.spika.enterprise.chat.gcm.PushBroadcastReceiver;
import com.clover.spika.enterprise.chat.lazy.ImageLoader;
import com.clover.spika.enterprise.chat.listeners.OnSearchListener;
import com.clover.spika.enterprise.chat.models.Push;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.PasscodeUtility;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;

public class MainActivity extends SlidingFragmentActivity {

	List<Push> qPush = new ArrayList<Push>();
	boolean isPushShowing = false;

	public ImageView tabGames;
	public ImageView tabTalk;
	public ImageView tabFriends;
	public ImageView tabSettings;

	public final static int slidingDuration = 160;

	public SlidingMenu slidingMenu;
	private ImageButton sidebarBtn;

	private ImageButton searchBtn;
	private TextView screenTitle;
	private EditText searchEt;

	private int screenWidth;
	private int speedSearchAnimation = 300;// android.R.integer.config_shortAnimTime;
	private OnSearchListener mSearchListener;

	public PushBroadcastReceiver myPushRecevier;
	public IntentFilter intentFilter;

	private ImageLoader imageLoader;

	private Fragment mFragment;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setBehindContentView(R.layout.sidebar_layout_empty);
		// set the Behind View Fragment
		getFragmentManager().beginTransaction().replace(R.id.emptyLayout, new SidebarFragment()).commit();

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

		screenWidth = getResources().getDisplayMetrics().widthPixels;

		slidingMenu = getSlidingMenu();
		slidingMenu.setMode(SlidingMenu.LEFT);
		slidingMenu.setTouchModeBehind(SlidingMenu.TOUCHMODE_MARGIN);
		slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
		slidingMenu.setBehindScrollScale(0.35f);
		slidingMenu.setShadowDrawable(null);
		slidingMenu.setFadeDegree(0.35f);
		// Value 950 is not used, library method has been changed
		slidingMenu.setBehindWidth(80);

		// set the Above View
		if (savedInstanceState != null)
			mFragment = getFragmentManager().getFragment(savedInstanceState, "mainContent");
		if (mFragment == null)
			mFragment = new LobbyFragment();

		// set the Above View
		setContentView(R.layout.activity_base);
		getFragmentManager().beginTransaction().replace(R.id.mainContent, mFragment).commit();

		imageLoader = new ImageLoader(this);
		imageLoader.setDefaultImage(R.drawable.default_user_image);
		screenTitle = (TextView) findViewById(R.id.screenTitle);
	}

	public void setScreenTitle(String title) {
		screenTitle.setText(title);
	}

	public ImageLoader getImageLoader() {
		return imageLoader;
	}

	public void switchContent(Fragment fragment) {
		mFragment = fragment;
		getFragmentManager().beginTransaction().replace(R.id.mainContent, fragment).commit();
		getSlidingMenu().showContent();
	}

	public void pushCall(String msg, String chatIdPush, String chatName, String chatImage) {
		showPopUp(msg, chatIdPush, chatName, chatImage);
	}

	@Override
	protected void onResume() {
		super.onResume();

		registerReceiver(myPushRecevier, intentFilter);

		// passcode callback injected methods are important for tracking active
		// session
		PasscodeUtility.getInstance().onResume();
		if (PasscodeUtility.getInstance().isPasscodeEnabled(this)) {
			if (!PasscodeUtility.getInstance().isSessionValid()) {
				startActivityForResult(new Intent(this, PasscodeActivity.class), Const.PASSCODE_ENTRY_VALIDATION_REQUEST);
			}
		}

		getIntentData(getIntent());
	}

	@Override
	protected void onPause() {
		super.onPause();

		unregisterReceiver(myPushRecevier);

		// passcode callback injected methods are important for tracking active
		// session
		PasscodeUtility.getInstance().onPause();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		getIntentData(intent);
	}

	private void getIntentData(Intent intent) {
		if (intent != null && intent.getExtras() != null) {
			if (mFragment != null && mFragment instanceof ProfileFragment) {
				((ProfileFragment) mFragment).setData(intent);
			}
		}
	}

	@Override
	public void setContentView(int id) {
		super.setContentView(id);

		sidebarBtn = (ImageButton) findViewById(R.id.sidebarBtn);

		if (sidebarBtn != null) {
			sidebarBtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					slidingMenu.toggle(true);
				}
			});
		}
	}

	/**
	 * Call this to disable the side bar, it will still work on button click
	 */
	public void disableSidebar() {
		slidingMenu.setTouchModeBehind(SlidingMenu.TOUCHMODE_NONE);
		slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
	}

	public void setSearch(OnSearchListener listener) {
		searchBtn = (ImageButton) findViewById(R.id.searchBtn);
		searchEt = (EditText) findViewById(R.id.searchEt);
		if (searchBtn == null || searchEt == null)
			return;

		mSearchListener = listener;

		searchBtn.setOnClickListener(searchOnClickListener);

		searchEt.setOnEditorActionListener(editorActionListener);
		searchEt.setImeActionLabel("Search", EditorInfo.IME_ACTION_SEARCH);
	}

	private OnClickListener searchOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (searchEt.getVisibility() == View.GONE) {
				openSearchAnimation();
			} else {
				if (mSearchListener != null) {
					String data = searchEt.getText().toString();
					hideKeyboard(searchEt);
					mSearchListener.onSearch(data);
				}
			}
		}
	};

	private OnEditorActionListener editorActionListener = new OnEditorActionListener() {

		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			if (actionId == EditorInfo.IME_ACTION_SEARCH) {
				hideKeyboard(searchEt);
				if (mSearchListener != null)
					mSearchListener.onSearch(v.getText().toString());
			}
			return false;
		}
	};

	private void openSearchAnimation() {
		searchBtn.setClickable(false);
		sidebarBtn.setClickable(false);
		searchEt.setVisibility(View.VISIBLE);

		AnimUtils.translationX(searchEt, screenWidth, 0f, speedSearchAnimation, new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				super.onAnimationEnd(animation);
				searchBtn.setClickable(true);
				sidebarBtn.setClickable(true);
				showKeyboardForced(searchEt);
			}
		});
		AnimUtils.translationX(searchBtn, 0, -(screenWidth - searchBtn.getWidth()), speedSearchAnimation, null);
		AnimUtils.fadeAnim(sidebarBtn, 1, 0, speedSearchAnimation);
		AnimUtils.translationX(screenTitle, 0, -screenWidth, speedSearchAnimation, null);
	}

	private void closeSearchAnimation() {
		searchBtn.setClickable(false);
		sidebarBtn.setClickable(false);
		hideKeyboard(searchEt);

		AnimUtils.translationX(searchEt, 0f, screenWidth, speedSearchAnimation, new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				searchEt.setVisibility(View.GONE);
				searchEt.setText("");
				super.onAnimationEnd(animation);
				searchBtn.setClickable(true);
				sidebarBtn.setClickable(true);
			}
		});
		AnimUtils.translationX(searchBtn, -(screenWidth - searchBtn.getWidth()), 0, speedSearchAnimation, null);
		AnimUtils.fadeAnim(sidebarBtn, 0, 1, speedSearchAnimation);
		AnimUtils.translationX(screenTitle, -screenWidth, 0, speedSearchAnimation, null);

	}

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
	public void onBackPressed() {
		if (searchEt != null && searchEt.getVisibility() == View.VISIBLE) {
			closeSearchAnimation();
			return;
		}

		finish();
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

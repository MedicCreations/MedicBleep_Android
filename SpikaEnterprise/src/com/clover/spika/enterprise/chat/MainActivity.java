package com.clover.spika.enterprise.chat;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.extendables.CustomFragment;
import com.clover.spika.enterprise.chat.extendables.SpikaEnterpriseApp;
import com.clover.spika.enterprise.chat.fragments.HomeFragment;
import com.clover.spika.enterprise.chat.fragments.PeopleFragment;
import com.clover.spika.enterprise.chat.fragments.SidebarFragment;
import com.clover.spika.enterprise.chat.listeners.OnCreateRoomListener;
import com.clover.spika.enterprise.chat.listeners.OnEditProfileListener;
import com.clover.spika.enterprise.chat.listeners.OnInternetErrorListener;
import com.clover.spika.enterprise.chat.listeners.OnSearchListener;
import com.clover.spika.enterprise.chat.models.User;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Logger;
import com.clover.spika.enterprise.chat.utils.PasscodeUtility;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu.OnClosedListener;

public class MainActivity extends BaseActivity {

	/* Menu/Header */
	SlidingMenu slidingMenu;
	ImageButton sidebarBtn;

	/* Search bar */
	ImageButton searchBtn;
	EditText searchEt;
	ImageButton closeSearchBtn;

	/* create room */
	ImageButton createRoomBtn;
	ImageButton filterRoomBtn;

	/* edit profile */
	ImageView editProfileBtn;

	int screenWidth;
	int speedSearchAnimation = 300;// android.R.integer.config_shortAnimTime;
	OnSearchListener mSearchListener;
	OnCreateRoomListener mCreateRoomListener;
	OnEditProfileListener mEditProfileListener;

	/* Fragment currently in use */
	CustomFragment mFragment;
	TextView screenTitle;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// start: set behind view (menu)
		setBehindContentView(R.layout.sidebar_layout_empty);
		getSupportFragmentManager().beginTransaction().replace(R.id.emptyLayout, new SidebarFragment()).commit();
		// end: set behind view (menu)

		// start: set the above view (content)
		if (savedInstanceState != null)
			mFragment = (CustomFragment) getSupportFragmentManager().getFragment(savedInstanceState, "mainContent");
		if (mFragment == null)
			mFragment = new HomeFragment();

		setContentView(R.layout.activity_main);
		getSupportFragmentManager().beginTransaction().replace(R.id.mainContent, mFragment, HomeFragment.class.getSimpleName()).commit();
		// end: set the above view (content)

		// start: set sliding menu options
		slidingMenu = getSlidingMenu();
		slidingMenu.setMode(SlidingMenu.LEFT);
		slidingMenu.setTouchModeBehind(SlidingMenu.TOUCHMODE_MARGIN);
		slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
		slidingMenu.setBehindScrollScale(0.35f);
		slidingMenu.setShadowDrawable(null);
		slidingMenu.setFadeDegree(0.35f);
		slidingMenu.setBehindWidth(0.7f);

		slidingMenu.setOnClosedListener(new OnClosedListener() {

			@Override
			public void onClosed() {
				mFragment.onClosed();
			}
		});
		// end: set sliding menu options

		sidebarBtn = (ImageButton) findViewById(R.id.sidebarBtn);
		sidebarBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				getSlidingMenu().toggle(true);
			}
		});

		screenWidth = getResources().getDisplayMetrics().widthPixels;

		searchBtn = (ImageButton) findViewById(R.id.searchBtn);
		createRoomBtn = (ImageButton) findViewById(R.id.createRoom);
		filterRoomBtn = (ImageButton) findViewById(R.id.filterRoom);
		editProfileBtn = (ImageView) findViewById(R.id.editProfile);
		searchEt = (EditText) findViewById(R.id.searchEt);
		closeSearchBtn = (ImageButton) findViewById(R.id.close_search);

		screenTitle = (TextView) findViewById(R.id.screenTitle);

		closeSearchBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				closeSearchAnimation(searchBtn, sidebarBtn, closeSearchBtn, searchEt, screenTitle, screenWidth, speedSearchAnimation);
			}
		});
		
		if(getIntent().getBooleanExtra(Const.IS_CALL_ACTIVE, false)){
			TextView view = new TextView(this);
			view.setBackgroundColor(getResources().getColor(R.color.green_in_people_row));
			view.setText(getString(R.string.touch_to_return_to_call));
			view.setTextColor(Color.WHITE);
			view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
			view.setGravity(Gravity.CENTER);
			((ViewGroup) findViewById(R.id.baseLayout)).addView(view);
			view.getLayoutParams().height = (int) getResources().getDimension(R.dimen.menu_height);
			view.getLayoutParams().width = android.widget.RelativeLayout.LayoutParams.MATCH_PARENT;
			view.setId(1);
			
			((android.widget.RelativeLayout.LayoutParams)findViewById(R.id.actionBarLayout).getLayoutParams()).addRule(RelativeLayout.BELOW, view.getId());
			
			view.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					finish();
				}
			});
		}
		
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		//if activity is in background and receive call offer
		if(intent.hasExtra(Const.TYPE_OF_SOCKET_RECEIVER)){
			if(intent.getIntExtra(Const.TYPE_OF_SOCKET_RECEIVER, -1) == Const.CALL_RECEIVE){
				User user = (User) intent.getSerializableExtra(Const.USER);
				String sessionId = intent.getStringExtra(Const.SESSION_ID);
				mService.callRinging(sessionId);
				showCallingPopup(user, sessionId, true, false);
			}
		}else if(intent.getBooleanExtra(Const.IS_CALL_ACTIVE, false)){
			setViewWhenCallIsInBackground(R.id.baseLayout, R.id.actionBarLayout, true);
		}
	}
	
	@Override
	protected void onServiceBaseConnected() {
		//if activity is killed and receive call offer
		if(getIntent().hasExtra(Const.TYPE_OF_SOCKET_RECEIVER)){
			if(getIntent().getIntExtra(Const.TYPE_OF_SOCKET_RECEIVER, -1) == Const.CALL_RECEIVE){
				User user = (User) getIntent().getSerializableExtra(Const.USER);
				String sessionId = getIntent().getStringExtra(Const.SESSION_ID);
				mService.callRinging(sessionId);
				showCallingPopup(user, sessionId, true, false);
				getIntent().removeExtra(Const.TYPE_OF_SOCKET_RECEIVER);
			}
		}
	}
	
	@Override
	protected void openRecordActivity(User user) {
		if(user != null) ChatActivity.startWithUserIdWithLeaveMessage(this, user);
	}

	public void setScreenTitle(String title) {
		if (screenTitle != null) {
			screenTitle.setText(title);
		}
	}

	public void switchContent(Fragment fragment) {
		mFragment = (CustomFragment) fragment;
		if(getSupportFragmentManager().findFragmentByTag(fragment.getClass().toString()) != null){
			Logger.custom("i", "LOG", "same fragment");// it is same fragment
		}else{
			getSupportFragmentManager().beginTransaction().replace(R.id.mainContent, fragment, fragment.getClass().toString()).commit();
		}
		getSlidingMenu().showContent();
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		if(SpikaEnterpriseApp.isCallInBackground()){
			if(viewForReturnToCall == null) setViewWhenCallIsInBackground(R.id.baseLayout, R.id.actionBarLayout, false);
		}else{
			if(viewForReturnToCall != null) ((ViewGroup) findViewById(R.id.baseLayout)).removeView(viewForReturnToCall);
		}

		if (PasscodeUtility.getInstance().isPasscodeEnabled(this)) {
			getWindow().setFlags(LayoutParams.FLAG_SECURE, LayoutParams.FLAG_SECURE);
		} else {
			getWindow().clearFlags(LayoutParams.FLAG_SECURE);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	/**
	 * Call this to disable the side bar, it will still work on button click
	 */
	public void disableSidebar() {
		slidingMenu.setTouchModeBehind(SlidingMenu.TOUCHMODE_NONE);
		slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
	}

	/**
	 * Set search bar
	 * 
	 * @param listener
	 */
	public void setSearch(OnSearchListener listener) {
		mSearchListener = listener;
		setSearch(searchBtn, searchOnClickListener, searchEt, editorActionListener);
	}

	public void disableSearch() {
		disableSearch(searchBtn, searchEt, sidebarBtn, closeSearchBtn, screenTitle, screenWidth, speedSearchAnimation);
	}

	/**
	 * set create room btn
	 */
	public void setCreateRoom(OnCreateRoomListener listener, boolean showFilter) {

		createRoomBtn.setVisibility(View.VISIBLE);
		mCreateRoomListener = listener;

		createRoomBtn.setOnClickListener(createRoomOnClickListener);
		
		if(!showFilter || !getResources().getBoolean(R.bool.enable_categories)) return;
		
		filterRoomBtn.setVisibility(View.VISIBLE);

		filterRoomBtn.setOnClickListener(filterRoomOnClickListener);
	}
	
	public void setFilterActivate(boolean isActivate){
		filterRoomBtn.setActivated(isActivate);
	}

	/**
	 * disable create room btn
	 */
	public void disableCreateRoom() {
		createRoomBtn.setVisibility(View.INVISIBLE);
		filterRoomBtn.setVisibility(View.INVISIBLE);
	}

	/**
	 * enabled create room btn
	 */
	public void enableCreateRoom(boolean showFilter) {
		createRoomBtn.setVisibility(View.VISIBLE);
		if(!showFilter || !getResources().getBoolean(R.bool.enable_categories)) return;
		filterRoomBtn.setVisibility(View.VISIBLE);
	}

	/**
	 * enabled edit profile btn
	 */
	public void enableEditProfile(OnEditProfileListener listener) {

		editProfileBtn.setVisibility(View.VISIBLE);
		mEditProfileListener = listener;

		editProfileBtn.setOnClickListener(editProfileOnClickListener);
	}

	/**
	 * disable edit profile btn
	 */
	public void disableEditProfile() {
		editProfileBtn.setVisibility(View.INVISIBLE);
	}

	private OnClickListener searchOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (searchEt.getVisibility() == View.GONE) {
				openSearchAnimation(searchBtn, sidebarBtn, closeSearchBtn, searchEt, screenTitle, screenWidth, speedSearchAnimation);
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

	private OnClickListener createRoomOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {

			if (mCreateRoomListener != null) {

				mCreateRoomListener.onCreateRoom();

			}
		}
	};
	
	private OnClickListener filterRoomOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {

			if (mCreateRoomListener != null) {

				mCreateRoomListener.onFilterClick();

			}
		}
	};

	private OnClickListener editProfileOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {

			if (mEditProfileListener != null) {

				mEditProfileListener.onEditProfile();

			}
		}
	};

	@Override
	public void onBackPressed() {
		if (searchEt != null && searchEt.getVisibility() == View.VISIBLE) {
			closeSearchAnimation(searchBtn, sidebarBtn, closeSearchBtn, searchEt, screenTitle, screenWidth, speedSearchAnimation);
			return;
		}

		finish();
	}

	public void showSmallLoading(int visible) {
		findViewById(R.id.loadingPB).setVisibility(visible);
	}

	@Override
	public void lobbyPushHandle(String chatId) {
		if (mFragment != null && mFragment instanceof HomeFragment) {
			((HomeFragment) mFragment).handlePushNotificationInFragment(chatId);
		}
	}
	
	public PeopleFragment getPeopleFragment(){
		if(mFragment instanceof HomeFragment){
			return ((HomeFragment) mFragment).getPeopleFragment();
		}
		return null;
	}
	
	public OnInternetErrorListener getInternetErrorListener(){
		return onInternetErrorListener;
	}
	
	protected OnInternetErrorListener onInternetErrorListener = new OnInternetErrorListener() {
		
		@Override
		public void onInternetError() {
			setViewNoInternetConnection(R.id.baseLayout, R.id.actionBarLayout);
		}
	};
	
}

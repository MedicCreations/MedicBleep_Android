package com.clover.spika.enterprise.chat;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.extendables.CustomFragment;
import com.clover.spika.enterprise.chat.fragments.LobbyFragment;
import com.clover.spika.enterprise.chat.fragments.SidebarFragment;
import com.clover.spika.enterprise.chat.lazy.ImageLoader;
import com.clover.spika.enterprise.chat.listeners.OnCreateRoomListener;
import com.clover.spika.enterprise.chat.listeners.OnSearchListener;
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
	TextView createRoomBtn;
	
	int screenWidth;
	int speedSearchAnimation = 300;// android.R.integer.config_shortAnimTime;
	OnSearchListener mSearchListener;
	OnCreateRoomListener mCreateRoomListener;

	/* Main ImageLoader */
	ImageLoader imageLoader;

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
			mFragment = new LobbyFragment();

		setContentView(R.layout.activity_main);
		getSupportFragmentManager().beginTransaction().replace(R.id.mainContent, mFragment, LobbyFragment.class.getSimpleName()).commit();
		// end: set the above view (content)

		// start: set sliding menu options
		slidingMenu = getSlidingMenu();
		slidingMenu.setMode(SlidingMenu.LEFT);
		slidingMenu.setTouchModeBehind(SlidingMenu.TOUCHMODE_MARGIN);
		slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
		slidingMenu.setBehindScrollScale(0.35f);
		slidingMenu.setShadowDrawable(null);
		slidingMenu.setFadeDegree(0.35f);
		slidingMenu.setBehindWidth(80);

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

		imageLoader = ImageLoader.getInstance();
		imageLoader.setDefaultImage(R.drawable.default_user_image);

		screenWidth = getResources().getDisplayMetrics().widthPixels;

		searchBtn = (ImageButton) findViewById(R.id.searchBtn);
		createRoomBtn = (TextView) findViewById(R.id.createRoom);
		searchEt = (EditText) findViewById(R.id.searchEt);
		closeSearchBtn = (ImageButton) findViewById(R.id.close_search);

		screenTitle = (TextView) findViewById(R.id.screenTitle);

		closeSearchBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				closeSearchAnimation(searchBtn, sidebarBtn, closeSearchBtn, searchEt, screenTitle, screenWidth, speedSearchAnimation);
			}
		});
	}

	public void setScreenTitle(String title) {
		if (screenTitle != null) {
			screenTitle.setText(title);
		}
	}

	public ImageLoader getImageLoader() {
		return imageLoader;
	}

	public void switchContent(Fragment fragment) {
		mFragment = (CustomFragment) fragment;
		getSupportFragmentManager().beginTransaction().replace(R.id.mainContent, fragment).commit();
		getSlidingMenu().showContent();
	}

	@Override
	protected void onResume() {
		super.onResume();

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
	public void setSearch(OnSearchListener listener){
		mSearchListener = listener;
		setSearch(searchBtn, searchOnClickListener, searchEt, editorActionListener);
	}
	
	public void disableSearch(){
		disableSearch(searchBtn, searchEt, sidebarBtn, closeSearchBtn, screenTitle, screenWidth, speedSearchAnimation);
	}
	
	/**
	 * set create room btn
	 */
	public void setCreateRoom(OnCreateRoomListener listener){
		
		createRoomBtn.setVisibility(View.VISIBLE);
		mCreateRoomListener = listener;
		
		createRoomBtn.setOnClickListener(createRoomOnClickListener);
		
	}
	
	/**
	 * disable create room btn
	 */
	public void disableCreateRoom(){
		
		createRoomBtn.setVisibility(View.INVISIBLE);
		
	}
	
	/**
	 * enabled create room btn
	 */
	public void enableCreateRoom(){
		
		createRoomBtn.setVisibility(View.VISIBLE);
		
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
	

	@Override
	public void onBackPressed() {
		if (searchEt != null && searchEt.getVisibility() == View.VISIBLE) {
			closeSearchAnimation(searchBtn, sidebarBtn, closeSearchBtn, searchEt, screenTitle, screenWidth, speedSearchAnimation);
			return;
		}

		finish();
	}

}

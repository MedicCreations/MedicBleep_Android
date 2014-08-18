package com.clover.spika.enterprise.chat;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.clover.spika.enterprise.chat.animation.AnimUtils;
import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.extendables.CustomFragment;
import com.clover.spika.enterprise.chat.fragments.LobbyFragment;
import com.clover.spika.enterprise.chat.fragments.SidebarFragment;
import com.clover.spika.enterprise.chat.lazy.ImageLoader;
import com.clover.spika.enterprise.chat.listeners.OnSearchListener;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.PasscodeUtility;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu.OnClosedListener;

public class MainActivity extends BaseActivity {

	/* Menu/Header */
	SlidingMenu slidingMenu;
	ImageButton sidebarBtn;
	TextView screenTitle;

	/* Search bar */
	ImageButton searchBtn;
	EditText searchEt;
	boolean isOpenSearch = false;

	int screenWidth;
	int speedSearchAnimation = 300;// android.R.integer.config_shortAnimTime;
	OnSearchListener mSearchListener;

	/* Main ImageLoader */
	ImageLoader imageLoader;

	/* Fragment currently in use */
	CustomFragment mFragment;

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

		setContentView(R.layout.activity_base);
		getSupportFragmentManager().beginTransaction().replace(R.id.mainContent, mFragment).commit();
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

		sidebarBtn = (ImageButton) findViewById(R.id.sidebarBtn);
		sidebarBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				slidingMenu.toggle(true);
			}
		});

		slidingMenu.setOnClosedListener(new OnClosedListener() {

			@Override
			public void onClosed() {
				mFragment.onClosed();
			}
		});
		// end: set sliding menu options

		imageLoader = new ImageLoader(this);
		imageLoader.setDefaultImage(R.drawable.default_user_image);

		screenTitle = (TextView) findViewById(R.id.screenTitle);

		screenWidth = getResources().getDisplayMetrics().widthPixels;

		searchBtn = (ImageButton) findViewById(R.id.searchBtn);
		searchEt = (EditText) findViewById(R.id.searchEt);
	}

	public void setScreenTitle(String title) {
		screenTitle.setText(title);
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
	protected void onPause() {
		super.onPause();

		// passcode callback injected methods are important for tracking active
		// session
		PasscodeUtility.getInstance().onPause();
	}

	/**
	 * Call this to disable the side bar, it will still work on button click
	 */
	public void disableSidebar() {
		slidingMenu.setTouchModeBehind(SlidingMenu.TOUCHMODE_NONE);
		slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
	}

	public void setSearch(OnSearchListener listener) {

		searchBtn.setVisibility(View.VISIBLE);

		mSearchListener = listener;

		searchBtn.setOnClickListener(searchOnClickListener);

		searchEt.setOnEditorActionListener(editorActionListener);
		searchEt.setImeActionLabel("Search", EditorInfo.IME_ACTION_SEARCH);
	}

	public void disableSearch() {

		if (isOpenSearch) {
			closeSearchAnimation();
		}

		searchBtn.setVisibility(View.GONE);
		searchEt.setVisibility(View.GONE);
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
				isOpenSearch = true;
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
				isOpenSearch = false;
			}
		});
		AnimUtils.translationX(searchBtn, -(screenWidth - searchBtn.getWidth()), 0, speedSearchAnimation, null);
		AnimUtils.fadeAnim(sidebarBtn, 0, 1, speedSearchAnimation);
		AnimUtils.translationX(screenTitle, -screenWidth, 0, speedSearchAnimation, null);

	}

	@Override
	public void onBackPressed() {
		if (searchEt != null && searchEt.getVisibility() == View.VISIBLE) {
			closeSearchAnimation();
			return;
		}

		finish();
	}

}

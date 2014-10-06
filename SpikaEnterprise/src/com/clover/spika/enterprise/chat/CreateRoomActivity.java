package com.clover.spika.enterprise.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.fragments.CreateRoomFragment;
import com.clover.spika.enterprise.chat.lazy.ImageLoader;
import com.clover.spika.enterprise.chat.listeners.OnCreateRoomListener;
import com.clover.spika.enterprise.chat.listeners.OnSearchListener;
import com.clover.spika.enterprise.chat.utils.Const;

public class CreateRoomActivity extends BaseActivity {

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
	TextView screenTitle;
	
	public static void start(String categoryId, String categoryName, Context c){
		c.startActivity(new Intent(c, CreateRoomActivity.class)
					.putExtra(Const.CATEGORY_NAME, categoryName)
					.putExtra(Const.CATEGORY_ID, categoryId));
	}
	
	public static void start(Context c){
		start("0", "", c);
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_create_room);
		
		int mcategoryId = getIntent().getIntExtra(Const.CATEGORY_ID, 0);
		String categoryName = getIntent().getStringExtra(Const.CATEGORY_NAME);
		if(TextUtils.isEmpty(categoryName)){
			categoryName = getString(R.string.select_category);
		}
		
		CreateRoomFragment fragment = new CreateRoomFragment();
		Bundle bundle = new Bundle();
		bundle.putInt(Const.CATEGORY_ID, mcategoryId);
		bundle.putString(Const.CATEGORY_NAME, categoryName);
		fragment.setArguments(bundle);
		getSupportFragmentManager().beginTransaction().add(R.id.mainContent, fragment, CreateRoomFragment.class.getSimpleName()).commit();

		findViewById(R.id.goBack).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
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
		setScreenTitle(getString(R.string.create_room));

		closeSearchBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				closeSearchAnimation(searchBtn, (ImageButton) findViewById(R.id.goBack), closeSearchBtn, 
						searchEt, screenTitle, screenWidth, speedSearchAnimation);
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
		disableSearch(searchBtn, searchEt, (ImageButton) findViewById(R.id.goBack), 
				closeSearchBtn, screenTitle, screenWidth, speedSearchAnimation);
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
				openSearchAnimation(searchBtn, (ImageButton) findViewById(R.id.goBack), 
						closeSearchBtn, searchEt, screenTitle, screenWidth, speedSearchAnimation);
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
			closeSearchAnimation(searchBtn, (ImageButton) findViewById(R.id.goBack), 
					closeSearchBtn, searchEt, screenTitle, screenWidth, speedSearchAnimation);
			return;
		}

		finish();
	}
	
}

package com.clover.spika.enterprise.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.clover.spika.enterprise.chat.api.ApiCallback;
import com.clover.spika.enterprise.chat.api.ChatApi;
import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.fragments.ConfirmRoomFragment;
import com.clover.spika.enterprise.chat.fragments.CreateRoomFragment;
import com.clover.spika.enterprise.chat.lazy.ImageLoader;
import com.clover.spika.enterprise.chat.listeners.OnCreateRoomListener;
import com.clover.spika.enterprise.chat.listeners.OnNextStepRoomListener;
import com.clover.spika.enterprise.chat.listeners.OnSearchListener;
import com.clover.spika.enterprise.chat.models.Chat;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Helper;

public class CreateRoomActivity extends BaseActivity {

	/* Search bar */
	ImageButton searchBtn;
	EditText searchEt;
	ImageButton closeSearchBtn;

	/* create room */
	TextView createRoomBtn;
	TextView nextStepRoomBtn;
	
	int screenWidth;
	int speedSearchAnimation = 300;// android.R.integer.config_shortAnimTime;
	OnSearchListener mSearchListener;
	OnCreateRoomListener mCreateRoomListener;
	OnNextStepRoomListener mNextStepListener;

	/* Main ImageLoader */
	ImageLoader imageLoader;

	/* Fragment currently in use */
	TextView screenTitle;
	
	private String roomName = "";
	private String room_file_id = "";
	private String room_thumb_id = "";
	private String categoryId = "0";
	private String roomIsPrivate = "0";
	private String roomPassword = "";
	
	private boolean isConfirmActive = false;
	
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
		
		String mcategoryId = getIntent().getStringExtra(Const.CATEGORY_ID);
		String categoryName = getIntent().getStringExtra(Const.CATEGORY_NAME);
		if(TextUtils.isEmpty(categoryName)){
			categoryName = getString(R.string.select_category);
		}
		
		CreateRoomFragment fragment = new CreateRoomFragment();
		Bundle bundle = new Bundle();
		bundle.putString(Const.CATEGORY_ID, mcategoryId);
		bundle.putString(Const.CATEGORY_NAME, categoryName);
		fragment.setArguments(bundle);
		getSupportFragmentManager().beginTransaction().add(R.id.mainContent, fragment, CreateRoomFragment.class.getSimpleName()).commit();

		findViewById(R.id.goBack).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});

		imageLoader = ImageLoader.getInstance(this);
		imageLoader.setDefaultImage(R.drawable.default_user_image);

		screenWidth = getResources().getDisplayMetrics().widthPixels;

		searchBtn = (ImageButton) findViewById(R.id.searchBtn);
		nextStepRoomBtn = (TextView) findViewById(R.id.nextRoom);
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
	
	public void setConfirmScreen(String users_to_add, String group_to_add, String is_private, String password){
		
		createRoomBtn.setVisibility(View.VISIBLE);
		nextStepRoomBtn.setVisibility(View.INVISIBLE);
		
		roomIsPrivate = is_private;
		roomPassword = password;
		
		ConfirmRoomFragment fragment = new ConfirmRoomFragment();
		Bundle bundle = new Bundle();
		bundle.putString(Const.USER_IDS, users_to_add);
		bundle.putString(Const.GROUP_IDS, group_to_add);
		bundle.putString(Const.ROOM_THUMB_ID, room_thumb_id);
		bundle.putString(Const.NAME, roomName);
		fragment.setArguments(bundle);
		getSupportFragmentManager().beginTransaction().add(R.id.mainContent, fragment, ConfirmRoomFragment.class.getSimpleName()).commit();

		isConfirmActive = true;
	}
	
	public void setCategoryId(String categoryId){
		this.categoryId = categoryId;
	}
	
	public void setRoomName(String roomName) {
		this.roomName = roomName;
	}

	public void setRoom_file_id(String room_file_id) {
		this.room_file_id = room_file_id;
	}

	public void setRoom_thumb_id(String room_thumb_id) {
		this.room_thumb_id = room_thumb_id;
	}

	public void createRoomFinaly(String userIds){
		new ChatApi().createRoom(roomName, room_file_id, room_thumb_id, userIds, categoryId, roomIsPrivate, roomPassword,
				this, new ApiCallback<Chat>() {

			@Override
			public void onApiResponse(Result<Chat> result) {
				if (result.isSuccess()) {
					
					String chat_name = result.getResultData().getChat().getChat_name();
					String chat_id = result.getResultData().getChat().getChat_id();
					String chat_image = room_file_id;
					String chat_image_thumb = room_thumb_id;
					
					Intent intent = new Intent(CreateRoomActivity.this, ChatActivity.class);
					intent.putExtra(Const.TYPE, String.valueOf(Const.C_ROOM_ADMIN_ACTIVE));
					intent.putExtra(Const.CHAT_ID, chat_id);
					intent.putExtra(Const.CHAT_NAME, chat_name);
					intent.putExtra(Const.IMAGE, chat_image);
					intent.putExtra(Const.IMAGE_THUMB, chat_image_thumb);
					intent.putExtra(Const.IS_ACTIVE, 1);
					
					startActivity(intent);
					
					Helper.setRoomFileId(CreateRoomActivity.this, "");
					Helper.setRoomThumbId(CreateRoomActivity.this, "");
					
					finish();
				}
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
	public void setNext(OnNextStepRoomListener listener){
		
		nextStepRoomBtn.setVisibility(View.VISIBLE);
		mNextStepListener = listener;
		
		nextStepRoomBtn.setOnClickListener(nextStepOnClickListener);
		
	}
	
	public void setCreateRoom(OnCreateRoomListener listener){
		
		createRoomBtn.setVisibility(View.VISIBLE);
		mCreateRoomListener = listener;
		
		createRoomBtn.setOnClickListener(createRoomOnClickListener);
		
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
	
	private OnClickListener nextStepOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			
				if (mNextStepListener != null) {
					
					mNextStepListener.onNext();
				
			}
		}
	};
	

	@Override
	public void onBackPressed() {
		if(isConfirmActive){
			Fragment fragment = getSupportFragmentManager().findFragmentByTag(ConfirmRoomFragment.class.getSimpleName());
			getSupportFragmentManager().beginTransaction().remove(fragment).commit();
			createRoomBtn.setVisibility(View.INVISIBLE);
			nextStepRoomBtn.setVisibility(View.VISIBLE);
			isConfirmActive = false;
			return;
		}
		if (searchEt != null && searchEt.getVisibility() == View.VISIBLE) {
			closeSearchAnimation(searchBtn, (ImageButton) findViewById(R.id.goBack), 
					closeSearchBtn, searchEt, screenTitle, screenWidth, speedSearchAnimation);
			return;
		}

		finish();
	}
	
}

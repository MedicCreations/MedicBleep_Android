package com.clover.spika.enterprise.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.clover.spika.enterprise.chat.api.ApiCallback;
import com.clover.spika.enterprise.chat.api.UserApi;
import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.lazy.ImageLoader;
import com.clover.spika.enterprise.chat.listeners.OnImageDisplayFinishListener;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.models.UserWrapper;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.views.DetailsView;

public class ProfileOtherActivity extends BaseActivity {

	private ImageLoader imageLoader;

	private ImageView profileImage;
	private TextView profileName;
	private DetailsView mDetailScrollView;
	private ImageButton mOpenChat;
	
	private String mUserFirstName = "";
	private String mUserLastName = "";
	private String mUserId;

	public static void openOtherProfile(Context context, int userId, String imageFileId, String chatName) {

		Intent intent = new Intent(context, ProfileOtherActivity.class);

		intent.putExtra(Const.IMAGE, imageFileId);
		intent.putExtra(Const.CHAT_NAME, chatName);
		intent.putExtra(Const.USER_ID, String.valueOf(userId));
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		context.startActivity(intent);
	}
	
	public static void openOtherProfileFromList(Context context, int userId, String imageFileId, String chatName, String firstName, String lastName) {

		Intent intent = new Intent(context, ProfileOtherActivity.class);

		intent.putExtra(Const.IMAGE, imageFileId);
		intent.putExtra(Const.CHAT_NAME, chatName);
		intent.putExtra(Const.FIRSTNAME, firstName);
		intent.putExtra(Const.LASTNAME, lastName);
		intent.putExtra(Const.USER_ID, String.valueOf(userId));
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		context.startActivity(intent);
	}
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_other_profile);

		imageLoader = new ImageLoader(this);
		imageLoader.setDefaultImage(R.drawable.default_user_image);

		findViewById(R.id.goBack).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});

		profileImage = (ImageView) findViewById(R.id.profileImage);
		profileName = (TextView) findViewById(R.id.profileName);
		mDetailScrollView = (DetailsView) findViewById(R.id.scrollViewDetails);

		getIntentData(getIntent());

		findViewById(R.id.progressBarDetails).setVisibility(View.VISIBLE);

		new UserApi().getProfile(this, false, mUserId, new ApiCallback<UserWrapper>() {
			@Override
			public void onApiResponse(Result<UserWrapper> result) {
				if (result.isSuccess()) {
					findViewById(R.id.progressBarDetails).setVisibility(View.INVISIBLE);
					mDetailScrollView.createDetailsView(result.getResultData().getUser().getPublicDetails());
				} else {
					findViewById(R.id.progressBarDetails).setVisibility(View.INVISIBLE);
				}
			}
		});
		
		mOpenChat = (ImageButton) findViewById(R.id.openChat);
		boolean isFirstUserProfile = getResources().getBoolean(R.bool.first_user_profile);
		
		if (isFirstUserProfile){
			mOpenChat.setVisibility(View.VISIBLE);
		} else {
			mOpenChat.setVisibility(View.GONE);
		}
		mOpenChat.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				ChatActivity.startWithUserId(ProfileOtherActivity.this, String.valueOf(mUserId), false, mUserFirstName, mUserLastName);
				
			}
		});
		
	}

	private void getIntentData(Intent intent) {

		if (intent != null && intent.getExtras() != null) {

			imageLoader.displayImage(this, intent.getExtras().getString(Const.IMAGE), profileImage, new OnImageDisplayFinishListener() {

				@Override
				public void onFinish() {
					findViewById(R.id.loadingLayout).setVisibility(View.GONE);
				}
			});

			profileName.setText(intent.getExtras().getString(Const.CHAT_NAME));
			mUserId = getIntent().getExtras().getString(Const.USER_ID);
			mUserFirstName = getIntent().getExtras().getString(Const.FIRSTNAME);
			mUserLastName = getIntent().getExtras().getString(Const.LASTNAME);
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		getIntentData(intent);
	}

}

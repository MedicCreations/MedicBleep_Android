package com.clover.spika.enterprise.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.lazy.ImageLoaderSpice;
import com.clover.spika.enterprise.chat.listeners.OnImageDisplayFinishListener;
import com.clover.spika.enterprise.chat.models.User;
import com.clover.spika.enterprise.chat.utils.Const;

public class ProfileOtherActivity extends BaseActivity {

	private ImageView profileImage;
	private TextView profileName;

	private String mUserFirstName = "";
	private String mUserLastName = "";
	private String mUserId;

	private User activeUser = null;

	public static void openOtherProfile(Context context, int userId, String imageFileId, String chatName, User user) {

		Intent intent = new Intent(context, ProfileOtherActivity.class);

		intent.putExtra(Const.IMAGE, imageFileId);
		intent.putExtra(Const.CHAT_NAME, chatName);
		intent.putExtra(Const.USER_ID, String.valueOf(userId));
		intent.putExtra(Const.USER, user);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		context.startActivity(intent);
	}

	public static void openOtherProfileFromList(Context context, int userId, String imageFileId, String chatName, String firstName, String lastName, User user) {

		Intent intent = new Intent(context, ProfileOtherActivity.class);

		intent.putExtra(Const.IMAGE, imageFileId);
		intent.putExtra(Const.CHAT_NAME, chatName);
		intent.putExtra(Const.FIRSTNAME, firstName);
		intent.putExtra(Const.LASTNAME, lastName);
		intent.putExtra(Const.USER_ID, String.valueOf(userId));
		intent.putExtra(Const.USER, user);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		context.startActivity(intent);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_other_profile);

		findViewById(R.id.goBack).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});

		profileImage = (ImageView) findViewById(R.id.profileImage);
		profileName = (TextView) findViewById(R.id.profileName);

		if (!getResources().getBoolean(R.bool.enable_web_rtc)) {
			findViewById(R.id.callControls).setVisibility(View.INVISIBLE);
		}

		findViewById(R.id.btnVideoCall).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (activeUser != null) {
					showCallingPopup(activeUser, null, false, true);
				}
			}
		});

		findViewById(R.id.btnVoiceCall).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (activeUser != null) {
					showCallingPopup(activeUser, null, false, false);
				}
			}
		});

		getIntentData(getIntent());
		
		setActiveClass(ProfileOtherActivity.class.getName());

	}

	private void getIntentData(Intent intent) {

		if (intent != null && intent.getExtras() != null) {

			getImageLoader().displayImage(profileImage, intent.getExtras().getString(Const.IMAGE), ImageLoaderSpice.DEFAULT_USER_IMAGE, new OnImageDisplayFinishListener() {

				@Override
				public void onFinish() {
					findViewById(R.id.loadingLayout).setVisibility(View.GONE);
				}
			});

			profileName.setText(intent.getExtras().getString(Const.CHAT_NAME));
			mUserId = getIntent().getExtras().getString(Const.USER_ID);
			mUserFirstName = getIntent().getExtras().getString(Const.FIRSTNAME);
			mUserLastName = getIntent().getExtras().getString(Const.LASTNAME);
			activeUser = (User) getIntent().getSerializableExtra(Const.USER);

			final String imageId = intent.getExtras().getString(Const.IMAGE);
			final String chatName = getIntent().getExtras().getString(Const.CHAT_NAME);

			findViewById(R.id.showProfile).setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent sendIntent = new Intent(ProfileOtherActivity.this, ShowProfileActivity.class);
					sendIntent.putExtra(Const.USER_IMAGE_NAME, imageId);
					sendIntent.putExtra(Const.FIRSTNAME, mUserFirstName);
					sendIntent.putExtra(Const.LASTNAME, mUserLastName);
					sendIntent.putExtra(Const.CHAT_NAME, chatName);
					sendIntent.putExtra(Const.USER_ID, mUserId);
					startActivity(sendIntent);
				}
			});
		}

	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if(intent.getBooleanExtra(Const.IS_CALL_ACTIVE, false)){
			setViewWhenCallIsInBackground(R.id.wholeScreen, R.id.actionBarLayout, true);
		}else{
			getIntentData(intent);
		}
	}

}

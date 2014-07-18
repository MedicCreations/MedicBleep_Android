package com.clover.spika.enterprise.chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.lazy.ImageLoader;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Helper;

public class ProfileActivity extends BaseActivity implements OnClickListener {

	ImageView profileImage;
	ImageView addPhoto;
	TextView profileName;

	ImageLoader imageLoader;

	@Override
	public void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_profile);

		imageLoader = new ImageLoader(this);

		profileImage = (ImageView) findViewById(R.id.profileImage);
		
		int width = getResources().getDisplayMetrics().widthPixels;
		int padding = (int)(width / 10);
		
		profileImage.getLayoutParams().width = width-Helper.dpToPx(this, padding);
		profileImage.getLayoutParams().height = width-Helper.dpToPx(this, padding);
		
		profileName = (TextView) findViewById(R.id.profileName);
		
		addPhoto = (ImageView) findViewById(R.id.addPhoto);
		addPhoto.setOnClickListener(this);

		getIntentData(getIntent());
	}

	private void getIntentData(Intent intent) {
		if (intent != null && intent.getExtras() != null) {
			imageLoader.displayImage(this, intent.getExtras().getString(Const.USER_IMAGE_NAME), profileImage, true);
			profileName.setText(intent.getExtras().getString(Const.FIRSTNAME)+" "+intent.getExtras().getString(Const.LASTNAME));
		}
	}
	
	private void choosePhoto(){
		Intent intent = new Intent(this, CameraCropActivity.class);
		intent.putExtra(Const.INTENT_TYPE, Const.GALLERY_INTENT);
		intent.putExtra(Const.PROFILE_INTENT, true);
		startActivity(intent);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.addPhoto:
			choosePhoto();
			break;

		default:
			break;
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		getIntentData(intent);
	}
	
}
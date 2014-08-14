package com.clover.spika.enterprise.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.clover.spika.enterprise.chat.lazy.ImageLoader;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Helper;

public class ProfileOtherActivity extends MainActivity {

	ImageLoader imageLoader;
	ImageView profileImage;
	TextView profileName;

	public static void openOtherProfile(Context context, String fileId, String chatName) {

		Intent intent = new Intent(context, ProfileOtherActivity.class);

		intent.putExtra(Const.IMAGE, fileId);
		intent.putExtra(Const.CHAT_NAME, chatName);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		context.startActivity(intent);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_other_profile);

		imageLoader = new ImageLoader(this);

		findViewById(R.id.goBack).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});

		profileImage = (ImageView) findViewById(R.id.profileImage);

		int width = getResources().getDisplayMetrics().widthPixels;
		int padding = (int) (width / 9);

		profileImage.getLayoutParams().width = width - Helper.dpToPx(this, padding);
		profileImage.getLayoutParams().height = width - Helper.dpToPx(this, padding);

		profileName = (TextView) findViewById(R.id.profileName);

		getIntentData(getIntent());
	}

	private void getIntentData(Intent intent) {
		if (intent != null && intent.getExtras() != null) {
			imageLoader.displayImage(this, intent.getExtras().getString(Const.IMAGE), profileImage);
			profileName.setText(intent.getExtras().getString(Const.CHAT_NAME));
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		getIntentData(intent);
	}

}

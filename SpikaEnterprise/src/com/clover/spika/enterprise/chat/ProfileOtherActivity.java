package com.clover.spika.enterprise.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.clover.spika.enterprise.chat.api.ApiCallback;
import com.clover.spika.enterprise.chat.api.UserApi;
import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.lazy.ImageLoader;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.models.UserWrapper;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.views.DetailsScrollView;

public class ProfileOtherActivity extends BaseActivity {

	private ImageLoader imageLoader;

	private ImageView profileImage;
	private TextView profileName;
    private DetailsScrollView mDetailScrollView;

    private String mUserId;

	public static void openOtherProfile(Context context, String userId, String imageFileId, String chatName) {

		Intent intent = new Intent(context, ProfileOtherActivity.class);

		intent.putExtra(Const.IMAGE, imageFileId);
		intent.putExtra(Const.CHAT_NAME, chatName);
        intent.putExtra(Const.USER_ID, userId);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		context.startActivity(intent);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_other_profile);

		imageLoader = ImageLoader.getInstance();

		findViewById(R.id.goBack).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});

		profileImage = (ImageView) findViewById(R.id.profileImage);
		profileName = (TextView) findViewById(R.id.profileName);
        mDetailScrollView = (DetailsScrollView) findViewById(R.id.scrollViewDetails);

        getIntentData(getIntent());

        findViewById(R.id.progressBarDetails).setVisibility(View.VISIBLE);
        new UserApi().getProfile(this, mUserId, new ApiCallback<UserWrapper>() {
            @Override
            public void onApiResponse(Result<UserWrapper> result) {
                if (result.isSuccess()) {
                    findViewById(R.id.progressBarDetails).setVisibility(View.INVISIBLE);
                    mDetailScrollView.createDetailsView(result.getResultData().getUser().getPublicDetails());
                }
            }
        });
    }

	private void getIntentData(Intent intent) {
		if (intent != null && intent.getExtras() != null) {
			imageLoader.displayImage(this, intent.getExtras().getString(Const.IMAGE), profileImage);
			profileName.setText(intent.getExtras().getString(Const.CHAT_NAME));

            mUserId = getIntent().getExtras().getString(Const.USER_ID);
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		getIntentData(intent);
	}

}

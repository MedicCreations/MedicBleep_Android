package com.clover.spika.enterprise.chat;

import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.lazy.ImageLoader;
import com.clover.spika.enterprise.chat.utils.Const;

import com.clover.spika.enterprise.chat.R;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ProfileActivity extends BaseActivity implements OnClickListener {

    ImageView headerEditBack;

    ImageView profileImg;
    TextView nickname;

    LinearLayout btnOk;

    int radius = 0;

    ImageLoader imageLoader;

    @Override
    protected void onCreate(Bundle arg0) {
	super.onCreate(arg0);
	setContentView(R.layout.activity_profile);

	Bitmap bitmapBorder = BitmapFactory.decodeResource(this.getResources(), R.drawable.circle);
	radius = bitmapBorder.getWidth();
	bitmapBorder = null;

	imageLoader = new ImageLoader(this);

	headerEditBack = (ImageView) findViewById(R.id.headerEditBack);
	headerEditBack.setOnClickListener(this);

	profileImg = (ImageView) findViewById(R.id.profileImg);

	nickname = (TextView) findViewById(R.id.nickname);

	btnOk = (LinearLayout) findViewById(R.id.btnOk);
	btnOk.setOnClickListener(this);

	getIntentData(getIntent());
    }

    private void getIntentData(Intent intent) {
	if (intent != null && intent.getExtras() != null) {
	    imageLoader.displayImage(intent.getExtras().getString(Const.USER_IMAGE_NAME), profileImg, true);
	    nickname.setText(intent.getExtras().getString(Const.USER_NICKNAME));
	}
    }

    @Override
    public void onClick(View view) {

	int id = view.getId();
	if (id == R.id.headerEditBack || id == R.id.btnOk) {
	    finish();
	} else {
	}
    }

    @Override
    protected void onNewIntent(Intent intent) {
	super.onNewIntent(intent);
	getIntentData(intent);
    }

}
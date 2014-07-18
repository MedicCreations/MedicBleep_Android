package com.clover.spika.enterprise.chat;

import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.lazy.ImageLoader;
import com.clover.spika.enterprise.chat.utils.Const;

import com.clover.spika.enterprise.chat.R;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class PhotoActivity extends BaseActivity {

    RelativeLayout imageLayout;
    ImageView mImageView;

    String imageUrl;

    @Override
    public void onCreate(Bundle arg0) {
	super.onCreate(arg0);
	setContentView(R.layout.activity_photo);

	imageLayout = (RelativeLayout) findViewById(R.id.imageLayout);
	mImageView = (ImageView) findViewById(R.id.mImageView);

	if (getIntent().getExtras() != null && getIntent().getExtras().containsKey(Const.IMAGE)) {
	    imageUrl = getIntent().getExtras().getString(Const.IMAGE, "");

	    ImageLoader imageLoader = new ImageLoader(this);
	    imageLoader.displayImage(this, imageUrl, mImageView, false);
	}
    }
}
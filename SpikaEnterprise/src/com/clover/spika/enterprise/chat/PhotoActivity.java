package com.clover.spika.enterprise.chat;

import java.io.File;
import java.io.IOException;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.lazy.ImageLoader;
import com.clover.spika.enterprise.chat.listeners.OnImageDisplayFinishListener;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.views.TouchImageView;
import com.clover.spika.enterprise.chat.views.emoji.GifAnimationDrawable;

public class PhotoActivity extends BaseActivity {

	ImageButton goBack;
	RelativeLayout imageLayout;
	TouchImageView mImageView;
	ProgressBar pbLoading;

	String imageUrl;

	@Override
	public void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_photo);

		goBack = (ImageButton) findViewById(R.id.goBack);
		goBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});

		imageLayout = (RelativeLayout) findViewById(R.id.imageLayout);
		mImageView = (TouchImageView) findViewById(R.id.mImageView);
		pbLoading = (ProgressBar) findViewById(R.id.pbLoading);
		
		onNewIntent(getIntent());
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		if (intent.getExtras() != null && intent.getExtras().containsKey(Const.IMAGE)) {
			imageUrl = intent.getExtras().getString(Const.IMAGE, "");

			if(intent.hasExtra(Const.TYPE) && intent.getIntExtra(Const.TYPE, -1) == Const.MSG_TYPE_GIF){
				if(intent.getStringExtra(Const.FILE) == null){
					return;
				}
				try {
					GifAnimationDrawable gif = new GifAnimationDrawable(new File(intent.getStringExtra(Const.FILE)), this);
					gif.setOneShot(false);
					mImageView.setImageDrawable(gif);
					gif.setVisible(true, true);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				pbLoading.setVisibility(View.GONE);
				mImageView.setVisibility(View.VISIBLE);
			}else{
				mImageView.setVisibility(View.GONE);
				ImageLoader.getInstance(this).displayImage(this, imageUrl, mImageView, new OnImageDisplayFinishListener() {
					
					@Override
					public void onFinish() {
						pbLoading.setVisibility(View.GONE);
						mImageView.setVisibility(View.VISIBLE);
					}
				});
			}
		}
	}

}
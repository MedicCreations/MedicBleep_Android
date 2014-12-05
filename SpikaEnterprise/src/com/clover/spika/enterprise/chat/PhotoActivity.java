package com.clover.spika.enterprise.chat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.clover.spika.enterprise.chat.dialogs.AppDialog;
import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.extendables.BaseAsyncTask;
import com.clover.spika.enterprise.chat.lazy.ImageLoader;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.views.TouchImageView;

public class PhotoActivity extends BaseActivity {

	ImageButton goBack;
	RelativeLayout imageLayout;
	TouchImageView mImageView;

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

		onNewIntent(getIntent());
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		if (intent.getExtras() != null && intent.getExtras().containsKey(Const.IMAGE)) {
			imageUrl = intent.getExtras().getString(Const.IMAGE, "");

			new BaseAsyncTask<Void, Void, Bitmap>(this, true) {

				protected Bitmap doInBackground(Void... params) {
					return ImageLoader.getInstance(PhotoActivity.this).getBitmap(context, imageUrl);
				}

				@Override
				protected void onPostExecute(Bitmap result) {
					super.onPostExecute(result);
					if (result != null) {
						mImageView.setImageBitmap(result);
						imageLayout.setBackgroundColor(getResources().getColor(R.color.black));
					} else {
						AppDialog dialog = new AppDialog(context, true);
						dialog.setFailed(context.getResources().getString(R.string.e_error_downloading_file));
					}
				}

			}.execute();
		}
	}

}
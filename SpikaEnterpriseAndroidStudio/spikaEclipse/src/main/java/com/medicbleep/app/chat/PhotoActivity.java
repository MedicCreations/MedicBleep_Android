package com.medicbleep.app.chat;

import java.io.File;
import java.io.IOException;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.medicbleep.app.chat.extendables.BaseActivity;
import com.medicbleep.app.chat.lazy.ImageLoaderSpice;
import com.medicbleep.app.chat.listeners.OnImageDisplayFinishListener;
import com.medicbleep.app.chat.utils.Const;
import com.medicbleep.app.chat.utils.Utils;
import com.medicbleep.app.chat.views.TouchImageView;
import com.medicbleep.app.chat.views.emoji.GifAnimationDrawable;

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

		ImageButton share = (ImageButton) findViewById(R.id.sharePhoto);
		share.setVisibility(View.VISIBLE);

		onNewIntent(getIntent());
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		if (intent.getExtras() != null && intent.getExtras().containsKey(Const.IMAGE)) {
			imageUrl = intent.getExtras().getString(Const.IMAGE, "");

			if (intent.hasExtra(Const.TYPE) && intent.getIntExtra(Const.TYPE, -1) == Const.MSG_TYPE_GIF) {
				if (intent.getStringExtra(Const.FILE) == null) {
					return;
				}
				try {
					Log.d("Extras",intent.getExtras().toString());
					File slika = new File(intent.getStringExtra(Const.FILE));
					GifAnimationDrawable gif = new GifAnimationDrawable(new File(intent.getStringExtra(Const.FILE)), this);
					gif.setOneShot(false);
					mImageView.setImageDrawable(gif);
					gif.setVisible(true, true);
				} catch (IOException e) {
					e.printStackTrace();
				}
				pbLoading.setVisibility(View.GONE);
				mImageView.setVisibility(View.VISIBLE);

				final Uri uri = Uri.fromFile(new File(intent.getStringExtra(Const.FILE)));
				findViewById(R.id.sharePhoto).setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						if (uri != null) {
							Intent shareIntent = new Intent();
							shareIntent.setAction(Intent.ACTION_SEND);
							shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
							shareIntent.setType("image/*");
							startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.share_via)));
						}
					}
				});
			} else {
				mImageView.setVisibility(View.GONE);
				boolean isEncrypted = intent.getExtras().getBoolean(Const.IS_ENCRYPTED, true);
				mImageView.setTag(isEncrypted);
				getImageLoader().displayImage(mImageView, imageUrl, ImageLoaderSpice.NO_IMAGE, new OnImageDisplayFinishListener() {

					@Override
					public void onFinish() {
						pbLoading.setVisibility(View.GONE);
						mImageView.setVisibility(View.VISIBLE);

						findViewById(R.id.sharePhoto).setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								onShare();
							}

						});
					}
				});

			}
		}
	}

	private void onShare() {
		new GetUriFromImageView().execute();
	}

	class GetUriFromImageView extends AsyncTask<Void, Void, Uri> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pbLoading.setVisibility(View.VISIBLE);
		}

		@Override
		protected Uri doInBackground(Void... params) {
			Uri uri = Utils.getLocalBitmapUri(mImageView, PhotoActivity.this);
			return uri;
		}

		@Override
		protected void onPostExecute(Uri result) {
			super.onPostExecute(result);
			if (result != null) {
				Intent shareIntent = new Intent();
				shareIntent.setAction(Intent.ACTION_SEND);
				shareIntent.putExtra(Intent.EXTRA_STREAM, result);
				shareIntent.setType("image/*");
				startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.share_via)));
			}
			pbLoading.setVisibility(View.GONE);
		}
	}

}
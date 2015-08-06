package com.medicbleep.app.chat.share;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.medicbleep.app.chat.CameraFullPhotoActivity;
import com.medicbleep.app.chat.extendables.BaseActivity;
import com.medicbleep.app.chat.utils.Const;

public class ShareImageActivity extends BaseActivity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Uri uri = (Uri) getIntent().getExtras().get(Intent.EXTRA_STREAM);
		
		startActivity(new Intent(this, CameraFullPhotoActivity.class)
				.putExtra(Const.INTENT_TYPE, Const.SHARE_INTENT)
				.putExtra(Intent.EXTRA_STREAM, (Uri) getIntent().getExtras().get(Intent.EXTRA_STREAM))
				.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
		
		finish();
		
	}
	
}

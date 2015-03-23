package com.clover.spika.enterprise.chat.share;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.clover.spika.enterprise.chat.CameraFullPhotoActivity;
import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.utils.Const;

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

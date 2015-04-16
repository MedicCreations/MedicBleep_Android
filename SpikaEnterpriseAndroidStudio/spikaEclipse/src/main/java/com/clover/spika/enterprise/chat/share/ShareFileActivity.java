package com.clover.spika.enterprise.chat.share;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.clover.spika.enterprise.chat.extendables.BaseActivity;

public class ShareFileActivity extends BaseActivity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		ChooseLobbyActivity.start(this, (Uri) getIntent().getExtras().get(Intent.EXTRA_STREAM));

		finish();
		
	}
	
}

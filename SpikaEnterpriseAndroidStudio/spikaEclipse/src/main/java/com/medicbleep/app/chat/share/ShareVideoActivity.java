package com.medicbleep.app.chat.share;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.medicbleep.app.chat.RecordVideoActivity;
import com.medicbleep.app.chat.extendables.BaseActivity;
import com.medicbleep.app.chat.utils.Const;

public class ShareVideoActivity extends BaseActivity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		startActivity(new Intent(this, RecordVideoActivity.class)
				.putExtra(Const.INTENT_TYPE, Const.SHARE_INTENT_INT)
				.putExtra(Intent.EXTRA_STREAM, (Uri) getIntent().getExtras().get(Intent.EXTRA_STREAM))
				.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
		
		finish();
		
	}
	
}

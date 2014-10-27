package com.clover.spika.enterprise.chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ListView;

import com.clover.spika.enterprise.chat.adapters.UserDetailsAdapter;
import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.models.UserWrapper;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Logger;
import com.clover.spika.enterprise.chat.views.RobotoRegularTextView;

public class EditProfileActivity extends BaseActivity implements OnClickListener{
	
	private ListView listViewDetail;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_edit_profile);
		
		ImageButton btnBack = (ImageButton) findViewById(R.id.goBack);
		btnBack.setOnClickListener(this);
		
		RobotoRegularTextView saveProfile = (RobotoRegularTextView) findViewById(R.id.saveProfile);
		saveProfile.setOnClickListener(this);
		
		listViewDetail = (ListView) findViewById(R.id.listUserDetails);
		
		Intent i = getIntent();
		UserWrapper user = (UserWrapper)i.getSerializableExtra(Const.USER_WRAPPER);
		
		Logger.d("etoga " + user.getUser().getDetails().toString());
		
		UserDetailsAdapter adapter = new UserDetailsAdapter(this, user.getUserDetailList(), user.getUser().getDetails());
		
		listViewDetail.setAdapter(adapter);
		adapter.notifyDataSetChanged();
		
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.goBack:
			finish();
			break;

		default:
			break;
		}
		
	}

}

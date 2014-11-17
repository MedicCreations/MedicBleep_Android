package com.clover.spika.enterprise.chat;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ListView;

import com.clover.spika.enterprise.chat.adapters.UserDetailsAdapter;
import com.clover.spika.enterprise.chat.api.ApiCallback;
import com.clover.spika.enterprise.chat.api.UserApi;
import com.clover.spika.enterprise.chat.dialogs.AppProgressDialog;
import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.extendables.BaseModel;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.models.UserDetail;
import com.clover.spika.enterprise.chat.models.UserWrapper;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.views.RobotoRegularTextView;

public class EditProfileActivity extends BaseActivity implements OnClickListener {

	private ListView listViewDetail;
	private UserDetailsAdapter adapter;

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
		UserWrapper user = (UserWrapper) i.getSerializableExtra(Const.USER_WRAPPER);

		adapter = new UserDetailsAdapter(this, user.getUserDetailList(), user.getUser().getDetails());

		listViewDetail.setAdapter(adapter);
		adapter.notifyDataSetChanged();

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.goBack:
			finish();
			break;
		case R.id.saveProfile:

			new UserApi().updateUserDetails(adapter.getList(), this, new ApiCallback<BaseModel>() {

				@Override
				public void onApiResponse(Result<BaseModel> result) {
					finish();
				}
			});
		default:
			break;
		}

	}

}

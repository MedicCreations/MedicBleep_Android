package com.clover.spika.enterprise.chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ListView;

import com.clover.spika.enterprise.chat.adapters.UserDetailsAdapter;
import com.clover.spika.enterprise.chat.api.robospice.UserSpice;
import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.extendables.BaseModel;
import com.clover.spika.enterprise.chat.models.UserWrapper;
import com.clover.spika.enterprise.chat.services.robospice.CustomSpiceListener;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Helper;
import com.clover.spika.enterprise.chat.utils.Utils;
import com.clover.spika.enterprise.chat.views.RobotoRegularTextView;
import com.octo.android.robospice.persistence.exception.SpiceException;

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

		adapter = new UserDetailsAdapter(this, user.getUserDetailList(), user.getUser().getDetails(), false);

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

			handleProgress(true);

			UserSpice.UpdateUserDetails updateUserImage = new UserSpice.UpdateUserDetails(adapter.getList(), this);
			spiceManager.execute(updateUserImage, new CustomSpiceListener<BaseModel>() {

				@Override
				public void onRequestFailure(SpiceException arg0) {
					super.onRequestFailure(arg0);
					handleProgress(false);
					Utils.onFailedUniversal(null, EditProfileActivity.this);
				}

				@Override
				public void onRequestSuccess(BaseModel result) {
					super.onRequestSuccess(result);
					handleProgress(false);

					if (result.getCode() == Const.API_SUCCESS) {

						finish();

					} else {
						Utils.onFailedUniversal(Helper.errorDescriptions(EditProfileActivity.this, result.getCode()), EditProfileActivity.this);
					}
				}
			});

		default:
			break;
		}

	}

}

package com.clover.spika.enterprise.chat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.clover.spika.enterprise.chat.adapters.UserDetailsAdapter;
import com.clover.spika.enterprise.chat.api.robospice.UserSpice;
import com.clover.spika.enterprise.chat.caching.UserCaching.OnUserGetDetailsDBChanged;
import com.clover.spika.enterprise.chat.caching.robospice.UserCacheSpice;
import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.extendables.BaseModel;
import com.clover.spika.enterprise.chat.lazy.ImageLoaderSpice;
import com.clover.spika.enterprise.chat.models.UserWrapper;
import com.clover.spika.enterprise.chat.services.robospice.CustomSpiceListener;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Helper;
import com.clover.spika.enterprise.chat.utils.Utils;
import com.clover.spika.enterprise.chat.views.RobotoRegularTextView;
import com.clover.spika.enterprise.chat.views.RoundImageView;
import com.octo.android.robospice.persistence.exception.SpiceException;

public class ShowProfileActivity extends BaseActivity implements OnClickListener, OnUserGetDetailsDBChanged {

	private ListView listViewDetail;
	private View header;
	private UserDetailsAdapter adapter;
	private UserWrapper userData;

	private String mUserId = "";
	private boolean isMyProfile = true;

	private boolean isInEditMode = false;

	TextView btnCancel;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_profile);

		if (getIntent().hasExtra(Const.USER_ID)) {
			mUserId = getIntent().getStringExtra(Const.USER_ID);
			isMyProfile = false;
		} else {
			mUserId = Helper.getUserId(this);
		}

		ImageButton btnBack = (ImageButton) findViewById(R.id.goBack);
		btnBack.setOnClickListener(this);

		RobotoRegularTextView saveProfile = (RobotoRegularTextView) findViewById(R.id.saveProfile);
		saveProfile.setOnClickListener(this);

		btnCancel = (TextView) findViewById(R.id.cancelProfile);

		listViewDetail = (ListView) findViewById(R.id.listUserDetails);

		UserCacheSpice.GetProfile getProfileData = new UserCacheSpice.GetProfile(this, spiceManager, mUserId, true, this);
		spiceManager.execute(getProfileData, new CustomSpiceListener<UserWrapper>() {

			@Override
			public void onRequestSuccess(UserWrapper result) {
				super.onRequestSuccess(result);

				if (result != null && result.user != null && result.detail_values != null) {
					userData = result;
					setData(result);
					btnCancel.setOnClickListener(ShowProfileActivity.this);
				}
			}
		});

		saveProfile.setText(getString(R.string.edit));

		if (!isMyProfile)
			saveProfile.setVisibility(View.GONE);
	}

	private View fillHeader(LayoutInflater inflater, UserWrapper user) {
		View rootView = inflater.inflate(R.layout.header_in_profile_settings, null, false);

		String firstName = getIntent().getStringExtra(Const.FIRSTNAME);
		String lastName = getIntent().getStringExtra(Const.LASTNAME);
		String imageId = getIntent().getStringExtra(Const.USER_IMAGE_NAME);

		if (firstName == null) {
			String chatName = getIntent().getStringExtra(Const.CHAT_NAME);
			((TextView) rootView.findViewById(R.id.name)).setText(chatName);
		} else {
			((TextView) rootView.findViewById(R.id.name)).setText(firstName + " " + lastName);
		}

		if (user.getUser().getOrganization() != null) {
			((TextView) rootView.findViewById(R.id.company)).setText(user.getUser().getOrganization().name);
		}

		ImageView profileImage = (ImageView) rootView.findViewById(R.id.profileImage);

		((RoundImageView) profileImage).setBorderColor(getResources().getColor(R.color.light_light_gray));
		getImageLoader().displayImage(profileImage, imageId, ImageLoaderSpice.DEFAULT_USER_IMAGE);

		return rootView;
	}

	protected void setData(UserWrapper user) {
		listViewDetail.removeHeaderView(header);
		header = fillHeader(getLayoutInflater(), user);
		listViewDetail.addHeaderView(header);

		adapter = new UserDetailsAdapter(this, user.getUserDetailList(), user.getUser().getDetails(), true);
		adapter.setShowNotEdit(true);

		listViewDetail.setAdapter(adapter);
		adapter.notifyDataSetChanged();
	}

	private void setEditModeData() {
		((TextView) findViewById(R.id.saveProfile)).setText(getString(R.string.save));
		btnCancel.setVisibility(View.VISIBLE);
		isInEditMode = true;

		adapter.setNewData(userData.getUserDetailList(), userData.getUser().getDetails(), false);
		adapter.setShowNotEdit(false);
		adapter.notifyDataSetChanged();
	}

	private void backToShow(boolean withReload) {
		((TextView) findViewById(R.id.saveProfile)).setText(getString(R.string.edit));
		btnCancel.setVisibility(View.GONE);
		isInEditMode = false;

		if (withReload) {

			UserCacheSpice.GetProfile getProfileData = new UserCacheSpice.GetProfile(this, spiceManager, Helper.getUserId(this), true, this);
			spiceManager.execute(getProfileData, new CustomSpiceListener<UserWrapper>() {

				@Override
				public void onRequestSuccess(UserWrapper result) {
					super.onRequestSuccess(result);

					if (result != null && result.user != null && result.detail_values != null) {
						userData = result;
						adapter.setNewData(userData.getUserDetailList(), userData.getUser().getDetails(), true);
						adapter.setShowNotEdit(true);
						adapter.notifyDataSetChanged();
					}
				}
			});
		} else {
			adapter.setNewData(userData.getUserDetailList(), userData.getUser().getDetails(), true);
			adapter.setShowNotEdit(true);
			adapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {

		case R.id.goBack:
			finish();
			break;

		case R.id.saveProfile:
			if (!isInEditMode) {
				setEditModeData();
			} else {

				handleProgress(true);

				UserSpice.UpdateUserDetails updateUSerDetails = new UserSpice.UpdateUserDetails(adapter.getList(), this);
				spiceManager.execute(updateUSerDetails, new CustomSpiceListener<BaseModel>() {

					@Override
					public void onRequestFailure(SpiceException arg0) {
						super.onRequestFailure(arg0);
						handleProgress(false);
						Utils.onFailedUniversal(null, ShowProfileActivity.this);
					}

					@Override
					public void onRequestSuccess(BaseModel result) {
						super.onRequestSuccess(result);
						handleProgress(false);

						if (result.getCode() == Const.API_SUCCESS) {

							backToShow(true);

						} else {
							Utils.onFailedUniversal(Helper.errorDescriptions(ShowProfileActivity.this, result.getCode()), ShowProfileActivity.this);
						}
					}
				});
			}
			break;

		case R.id.cancelProfile:
			backToShow(false);
			break;

		default:
			break;
		}
	}

	@Override
	public void onUserGetDetailsDBChanged(UserWrapper userWrapper) {

		if (userWrapper != null && userWrapper.user != null && userWrapper.detail_values != null) {
			userData = userWrapper;
			setData(userWrapper);
		}
	}

}

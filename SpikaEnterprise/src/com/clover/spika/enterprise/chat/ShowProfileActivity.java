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
import com.clover.spika.enterprise.chat.api.ApiCallback;
import com.clover.spika.enterprise.chat.api.UserApi;
import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.extendables.BaseModel;
import com.clover.spika.enterprise.chat.lazy.ImageLoader;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.models.UserWrapper;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Helper;
import com.clover.spika.enterprise.chat.views.RobotoRegularTextView;

public class ShowProfileActivity extends BaseActivity implements OnClickListener {

	private ListView listViewDetail;
	private UserDetailsAdapter adapter;
	private UserWrapper userData;
	
	private String mUserId = "";
	private boolean isMyProfile = true;
	
	private boolean isInEditMode = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
		setContentView(R.layout.activity_edit_profile);
		
		if(getIntent().hasExtra(Const.USER_ID)){
			mUserId = getIntent().getStringExtra(Const.USER_ID);
			isMyProfile = false;
		}else{
			mUserId = Helper.getUserId(this);
		}

		ImageButton btnBack = (ImageButton) findViewById(R.id.goBack);
		btnBack.setOnClickListener(this);

		RobotoRegularTextView saveProfile = (RobotoRegularTextView) findViewById(R.id.saveProfile);
		saveProfile.setOnClickListener(this);

		listViewDetail = (ListView) findViewById(R.id.listUserDetails);

		new UserApi().getProfile(this, true, mUserId, new ApiCallback<UserWrapper>() {
			@Override
			public void onApiResponse(Result<UserWrapper> result) {
				if (result.isSuccess()) {
					
					userData = result.getResultData();
					setData(result.getResultData());

				} 
			}
		});
		
		saveProfile.setText(getString(R.string.edit));
		findViewById(R.id.cancelProfile).setOnClickListener(this);
		
		if(!isMyProfile) saveProfile.setVisibility(View.GONE);
		
	}
	
	private View fillHeader(LayoutInflater inflater, UserWrapper user){
		View rootView = inflater.inflate(R.layout.header_in_profile_settings, null, false);
		
		String firstName = getIntent().getStringExtra(Const.FIRSTNAME);
		String lastName = getIntent().getStringExtra(Const.LASTNAME);
		String imageId = getIntent().getStringExtra(Const.USER_IMAGE_NAME);
		
		if(firstName == null){
			String chatName = getIntent().getStringExtra(Const.CHAT_NAME);
			((TextView) rootView.findViewById(R.id.name)).setText(chatName);
		}else{
			((TextView) rootView.findViewById(R.id.name)).setText(firstName + " " + lastName);
		}
		((TextView) rootView.findViewById(R.id.company)).setText(user.getUser().getOrganization().getName());

		ImageView profileImage = (ImageView) rootView.findViewById(R.id.profileImage);
		ImageLoader.getInstance(this).displayImage(this, imageId, profileImage);
		
		return rootView;
	}

	protected void setData(UserWrapper user) {
		View header = fillHeader(getLayoutInflater(), user);
		listViewDetail.addHeaderView(header);
		
		adapter = new UserDetailsAdapter(this, user.getUserDetailList(), user.getUser().getDetails(), true);
		adapter.setShowNotEdit(true);

		listViewDetail.setAdapter(adapter);
		adapter.notifyDataSetChanged();
	}
	
	private void setEditModeData() {
		((TextView) findViewById(R.id.saveProfile)).setText(getString(R.string.save));
		findViewById(R.id.cancelProfile).setVisibility(View.VISIBLE);
		isInEditMode = true;
		
		adapter.setNewData(userData.getUserDetailList(), userData.getUser().getDetails(), false);
		adapter.setShowNotEdit(false);
		adapter.notifyDataSetChanged();
	}
	
	private void backToShow(boolean withReload) {
		((TextView) findViewById(R.id.saveProfile)).setText(getString(R.string.edit));
		findViewById(R.id.cancelProfile).setVisibility(View.GONE);
		isInEditMode = false;
		
		if(withReload){
			new UserApi().getProfile(this, true, Helper.getUserId(this), new ApiCallback<UserWrapper>() {
				@Override
				public void onApiResponse(Result<UserWrapper> result) {
					if (result.isSuccess()) {
						
						userData = result.getResultData();
						adapter.setNewData(userData.getUserDetailList(), userData.getUser().getDetails(), true);
						adapter.setShowNotEdit(true);
						adapter.notifyDataSetChanged();
					} 
				}
			});
		}else{
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
			if(!isInEditMode){
				setEditModeData();
			}else{
				new UserApi().updateUserDetails(adapter.getList(), this, new ApiCallback<BaseModel>() {

					@Override
					public void onApiResponse(Result<BaseModel> result) {
						backToShow(true);
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

}

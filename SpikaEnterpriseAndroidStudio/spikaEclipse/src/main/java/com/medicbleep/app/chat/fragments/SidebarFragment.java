package com.medicbleep.app.chat.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.medicbleep.app.chat.MainActivity;
import com.medicbleep.app.chat.R;
import com.medicbleep.app.chat.api.robospice.UserSpice;
import com.medicbleep.app.chat.dialogs.AppDialog;
import com.medicbleep.app.chat.extendables.BaseModel;
import com.medicbleep.app.chat.extendables.CustomFragment;
import com.medicbleep.app.chat.lazy.ImageLoaderSpice;
import com.medicbleep.app.chat.services.robospice.CustomSpiceListener;
import com.medicbleep.app.chat.utils.Helper;
import com.octo.android.robospice.exception.NoNetworkException;
import com.octo.android.robospice.persistence.exception.SpiceException;

public class SidebarFragment extends CustomFragment implements OnClickListener {

	ImageView userImage;
	TextView userName;

	Button profile;
	Button lobby;
	Button information;
	Button logout;

	String image;

	ProfileFragment profileFragment;
	HomeFragment lobbyFragment;
	InformationFragment informationFragment;
	
	private ImageLoaderSpice imageLoaderSpice;

	public ImageLoaderSpice getImageLoader() {
		return imageLoaderSpice;
	}

	public SidebarFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		imageLoaderSpice = ImageLoaderSpice.getInstance(getActivity());
		imageLoaderSpice.setSpiceManager(spiceManager);
		image = Helper.getUserImage();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.sidebar_layout, container, false);

		userImage = (ImageView) view.findViewById(R.id.userImage);
        userImage.setOnClickListener(this);

		setUserImage(image);

		userName = (TextView) view.findViewById(R.id.userName);
		userName.setText(Helper.getUserFirstName() + " " + Helper.getUserLastName());
        userName.setOnClickListener(this);

		profile = (Button) view.findViewById(R.id.profile);
		profile.setOnClickListener(this);

		lobby = (Button) view.findViewById(R.id.lobby);
		lobby.setOnClickListener(this);

		information = (Button) view.findViewById(R.id.information);
		information.setOnClickListener(this);

		logout = (Button) view.findViewById(R.id.logout);
		logout.setOnClickListener(this);

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!image.equals(Helper.getUserImage())) {
			image = Helper.getUserImage();
			setUserImage(image);
		}
	}

	public void setUserImage(String image) {
        userImage.setTag(false);
		getImageLoader().displayImage(userImage, image, ImageLoaderSpice.DEFAULT_USER_IMAGE);
	}

	@Override
	public void onClick(View view) {

		switch (view.getId()) {

		case R.id.lobby:

			if (lobbyFragment == null) {
				lobbyFragment = new HomeFragment();
			}

			((MainActivity) getActivity()).setScreenTitle(getActivity().getResources().getString(R.string.recent));
			switchFragment(lobbyFragment);

			break;

		case R.id.information:

			if (informationFragment == null) {
				informationFragment = new InformationFragment();
			}

			((MainActivity) getActivity()).setScreenTitle(getActivity().getResources().getString(R.string.information)); 
			switchFragment(informationFragment);

			break;

		case R.id.profile:
        case R.id.userImage:
        case R.id.userName:

			if (profileFragment == null) {
				profileFragment = ProfileFragment.newInstance(Helper.getUserImage(), Helper.getUserFirstName(), Helper.getUserLastName());
			}

			((MainActivity) getActivity()).setScreenTitle(getActivity().getResources().getString(R.string.profile));
			switchFragment(profileFragment);

			break;

		case R.id.logout:
			
			handleProgress(true);
			UserSpice.Logout logout = new UserSpice.Logout();
			spiceManager.execute(logout, new CustomSpiceListener<BaseModel>(){
				
				@Override
				public void onRequestFailure(SpiceException arg0) {
					super.onRequestFailure(arg0);
					handleProgress(false);
					if(arg0 instanceof NoNetworkException){
						new AppDialog(getActivity(), false).setFailed(getResources().getString(R.string.no_internet_connection_));
					}else{
						new AppDialog(getActivity(), false).setFailed(getResources().getString(R.string.e_error_while_logout));
					}
				}
				
				@Override
				public void onRequestSuccess(BaseModel arg0) {
					super.onRequestSuccess(arg0);
					handleProgress(false);
					Helper.logout(getActivity());
				}
			});

			break;

		default:
			break;
		}
	}

	private void switchFragment(Fragment fragment) {
		if (getActivity() == null) {
			return;
		}

		MainActivity base = (MainActivity) getActivity();
		base.switchContent(fragment);
	}

}

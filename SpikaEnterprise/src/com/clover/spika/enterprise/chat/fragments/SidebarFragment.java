package com.clover.spika.enterprise.chat.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.clover.spika.enterprise.chat.MainActivity;
import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.api.robospice.UserSpice;
import com.clover.spika.enterprise.chat.dialogs.AppDialog;
import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.extendables.BaseModel;
import com.clover.spika.enterprise.chat.extendables.CustomFragment;
import com.clover.spika.enterprise.chat.lazy.ImageLoaderSpice;
import com.clover.spika.enterprise.chat.services.robospice.CustomSpiceListener;
import com.clover.spika.enterprise.chat.services.robospice.OkHttpService;
import com.clover.spika.enterprise.chat.utils.Helper;
import com.clover.spika.enterprise.chat.views.RobotoRegularTextView;
import com.octo.android.robospice.SpiceManager;
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
	
	protected SpiceManager spiceManager = new SpiceManager(OkHttpService.class);
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
		image = Helper.getUserImage(getActivity());
	}
	
	@Override
	public void onStart() {
		super.onStart();
		spiceManager.start(getActivity());
	}

	@Override
	public void onStop() {
		if (spiceManager.isStarted()) {
			spiceManager.shouldStop();
		}
		super.onStop();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.sidebar_layout, container, false);

		RobotoRegularTextView tvAppVersion = (RobotoRegularTextView) view.findViewById(R.id.app_version);
		String version = Helper.getAppVersion();
		tvAppVersion.setText(tvAppVersion.getText() + version);

		userImage = (ImageView) view.findViewById(R.id.userImage);

		setUserImage();

		userName = (TextView) view.findViewById(R.id.userName);
		userName.setText(Helper.getUserFirstName(getActivity()) + "\n" + Helper.getUserLastName(getActivity()));

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
		if (!image.equals(Helper.getUserImage(getActivity()))) {
			image = Helper.getUserImage(getActivity());
			setUserImage();
		}
	}

	private void setUserImage() {
		getImageLoader().displayImage(userImage, image, ImageLoaderSpice.DEFAULT_USER_IMAGE);
	}

	@Override
	public void onClick(View view) {

		switch (view.getId()) {

		case R.id.lobby:

			if (lobbyFragment == null) {
				lobbyFragment = new HomeFragment();
			}

			((MainActivity) getActivity()).setScreenTitle(getActivity().getResources().getString(R.string.lobby));
			switchFragment(lobbyFragment);

			break;

		case R.id.information:

			if (informationFragment == null) {
				informationFragment = new InformationFragment();
			}

			((MainActivity) getActivity()).setScreenTitle(getActivity().getResources().getString(R.string.about)); 
			switchFragment(informationFragment);

			break;

		case R.id.profile:

			if (profileFragment == null) {
				profileFragment = ProfileFragment.newInstance(Helper.getUserImage(getActivity()), Helper.getUserFirstName(getActivity()), Helper.getUserLastName(getActivity()));
			}

			((MainActivity) getActivity()).setScreenTitle(getActivity().getResources().getString(R.string.profile));
			switchFragment(profileFragment);

			break;

		case R.id.logout:
			
			((BaseActivity)getActivity()).dropDatabase();
			
			handleProgress(true);
			UserSpice.Logout logout = new UserSpice.Logout(getActivity());
			spiceManager.execute(logout, new CustomSpiceListener<BaseModel>(){
				
				@Override
				public void onRequestFailure(SpiceException arg0) {
					super.onRequestFailure(arg0);
					handleProgress(false);
					new AppDialog(getActivity(), false).setFailed(getResources().getString(R.string.e_error_while_logout));
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

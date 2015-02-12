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
import com.clover.spika.enterprise.chat.api.ApiCallback;
import com.clover.spika.enterprise.chat.api.UserApi;
import com.clover.spika.enterprise.chat.dialogs.AppDialog;
import com.clover.spika.enterprise.chat.extendables.BaseModel;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.utils.Helper;
import com.clover.spika.enterprise.chat.views.RobotoRegularTextView;

public class SidebarFragment extends Fragment implements OnClickListener {

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

	public SidebarFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		image = Helper.getUserImage(getActivity());
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
		if (getActivity() instanceof MainActivity) {
			((MainActivity) getActivity()).getImageLoader().displayImage(getActivity(), image, userImage);
		}
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

			((MainActivity) getActivity()).setScreenTitle(getActivity().getResources().getString(R.string.information));
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

			new UserApi().logout(getActivity(), new ApiCallback<BaseModel>() {

				@Override
				public void onApiResponse(Result<BaseModel> result) {
					if (result.isSuccess()) {
						Helper.logout(getActivity());
					} else {
						new AppDialog(getActivity(), false).setFailed(getResources().getString(R.string.e_error_while_logout));
					}
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

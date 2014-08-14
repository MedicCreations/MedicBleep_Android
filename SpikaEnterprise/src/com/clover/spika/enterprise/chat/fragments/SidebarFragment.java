package com.clover.spika.enterprise.chat.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.clover.spika.enterprise.chat.MainActivity;
import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.api.ApiCallback;
import com.clover.spika.enterprise.chat.api.UserApi;
import com.clover.spika.enterprise.chat.dialogs.AppDialog;
import com.clover.spika.enterprise.chat.extendables.BaseModel;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Helper;
import com.clover.spika.enterprise.chat.views.RobotoThinButton;
import com.clover.spika.enterprise.chat.views.RobotoThinTextView;
import com.clover.spika.enterprise.chat.views.RoundImageView;

public class SidebarFragment extends Fragment implements OnClickListener {

	RoundImageView userImage;
	RobotoThinTextView userName;

	Button profile;
	RobotoThinButton lobby;
	RobotoThinButton users;
	RobotoThinButton groups;
	RobotoThinButton logout;

	String image;

	ProfileFragment profileFragment;
	LobbyFragment lobbyFragment;
	UsersFragment usersFragment;
	GroupsFragment groupsFragment;

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

		userImage = (RoundImageView) view.findViewById(R.id.userImage);

		setUserImage();

		userName = (RobotoThinTextView) view.findViewById(R.id.userName);
		userName.setText(Helper.getUserFirstName(getActivity()) + "\n" + Helper.getUserLastName(getActivity()));

		profile = (Button) view.findViewById(R.id.profile);
		profile.setOnClickListener(this);

		lobby = (RobotoThinButton) view.findViewById(R.id.lobby);
		lobby.setOnClickListener(this);

		users = (RobotoThinButton) view.findViewById(R.id.users);
		users.setOnClickListener(this);

		groups = (RobotoThinButton) view.findViewById(R.id.groups);
		groups.setOnClickListener(this);

		logout = (RobotoThinButton) view.findViewById(R.id.logout);
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

		Intent intent = new Intent();

		switch (view.getId()) {

		case R.id.profile:

			intent.putExtra(Const.USER_IMAGE_NAME, Helper.getUserImage(getActivity()));
			intent.putExtra(Const.FIRSTNAME, Helper.getUserFirstName(getActivity()));
			intent.putExtra(Const.LASTNAME, Helper.getUserLastName(getActivity()));

			if (profileFragment == null) {
				profileFragment = new ProfileFragment(intent);
			}

			((MainActivity) getActivity()).setScreenTitle(getActivity().getResources().getString(R.string.profile));
			switchFragment(profileFragment);

			break;

		case R.id.lobby:

			if (lobbyFragment == null) {
				lobbyFragment = new LobbyFragment();
			}

			((MainActivity) getActivity()).setScreenTitle(getActivity().getResources().getString(R.string.lobby));
			switchFragment(lobbyFragment);

			break;

		case R.id.users:

			if (usersFragment == null) {
				usersFragment = new UsersFragment();
			}

			((MainActivity) getActivity()).setScreenTitle(getActivity().getResources().getString(R.string.users));
			switchFragment(usersFragment);

			break;

		case R.id.groups:

			if (groupsFragment == null) {
				groupsFragment = new GroupsFragment();
			}

			((MainActivity) getActivity()).setScreenTitle(getActivity().getResources().getString(R.string.groups));
			switchFragment(groupsFragment);

			break;

		case R.id.logout:

			new UserApi().updateUserToken(getActivity(), new ApiCallback<BaseModel>() {

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

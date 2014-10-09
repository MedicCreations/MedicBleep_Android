package com.clover.spika.enterprise.chat.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.clover.spika.enterprise.chat.MainActivity;
import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.api.ApiCallback;
import com.clover.spika.enterprise.chat.api.UserApi;
import com.clover.spika.enterprise.chat.dialogs.AppDialog;
import com.clover.spika.enterprise.chat.extendables.BaseModel;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.utils.Helper;
import com.clover.spika.enterprise.chat.views.RobotoThinButton;
import com.clover.spika.enterprise.chat.views.RobotoThinTextView;

public class SidebarFragment extends Fragment implements OnClickListener {

	ImageView userImage;
	RobotoThinTextView userName;

	Button profile;
	RobotoThinButton lobby;
	RobotoThinButton users;
	RobotoThinButton groups;
	RobotoThinButton rooms;
	RobotoThinButton information;
	RobotoThinButton logout;

	String image;

	ProfileFragment profileFragment;
	LobbyFragment lobbyFragment;
	UsersFragment usersFragment;
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

		userImage = (ImageView) view.findViewById(R.id.userImage);

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
		
		rooms = (RobotoThinButton) view.findViewById(R.id.rooms);
		rooms.setOnClickListener(this);
		
		information = (RobotoThinButton) view.findViewById(R.id.information);
		information.setOnClickListener(this);

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

		switch (view.getId()) {

		case R.id.profile:

			if (profileFragment == null) {
				profileFragment = ProfileFragment.newInstance(Helper.getUserImage(getActivity()),
                        Helper.getUserFirstName(getActivity()),
                        Helper.getUserLastName(getActivity()));
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

        case R.id.rooms:
		case R.id.groups:

			CategoryFragment categoryFragment = CategoryFragment.newInstance(
                    view.getId() == R.id.rooms ? CategoryFragment.UseType.ROOM : CategoryFragment.UseType.GROUP
            );

			((MainActivity) getActivity()).setScreenTitle(getActivity().getResources().getString(R.string.pick_category));
			switchFragment(categoryFragment);

			break;

		case R.id.information:

			if (informationFragment == null) {
				informationFragment = new InformationFragment();
			}

			((MainActivity) getActivity()).setScreenTitle(getActivity().getResources().getString(R.string.information));
			switchFragment(informationFragment);

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

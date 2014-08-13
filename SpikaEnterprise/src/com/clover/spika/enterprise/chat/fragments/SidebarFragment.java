package com.clover.spika.enterprise.chat.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.api.ApiCallback;
import com.clover.spika.enterprise.chat.api.UserApi;
import com.clover.spika.enterprise.chat.dialogs.AppDialog;
import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.extendables.BaseModel;
import com.clover.spika.enterprise.chat.lazy.ImageLoader;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Helper;
import com.clover.spika.enterprise.chat.views.RobotoThinButton;
import com.clover.spika.enterprise.chat.views.RobotoThinTextView;
import com.clover.spika.enterprise.chat.views.RoundImageView;

public class SidebarFragment extends Fragment {

	RoundImageView userImage;
	RobotoThinTextView userName;

	Button profile;
	RobotoThinButton lobby;
	RobotoThinButton users;
	RobotoThinButton groups;
	RobotoThinButton logout;

	String image;
	ImageLoader imageLoader;

	public SidebarFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.sidebar_layout, container, false);

		userImage = (RoundImageView) view.findViewById(R.id.userImage);
		imageLoader = new ImageLoader(getActivity());
		imageLoader.setDefaultImage(R.drawable.default_user_image);
		image = Helper.getUserImage(getActivity());
		imageLoader.displayImage(getActivity(), image, userImage, false);

		userName = (RobotoThinTextView) view.findViewById(R.id.userName);
		userName.setText(Helper.getUserFirstName(getActivity()) + "\n" + Helper.getUserLastName(getActivity()));

		profile = (Button) view.findViewById(R.id.profile);
		profile.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// ProfileActivity.openProfile(getActivity(), null);
				// ((BaseActivity) getActivity()).slidingMenu.toggle(true);
				((BaseActivity) getActivity()).menuItemClick(Const.ITEM_PROFILE);
			}
		});

		lobby = (RobotoThinButton) view.findViewById(R.id.lobby);
		lobby.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// LobbyActivity.openLobby(getActivity());
				// ((BaseActivity) getActivity()).slidingMenu.toggle(true);
				((BaseActivity) getActivity()).menuItemClick(Const.ITEM_LOBBY);
			}
		});

		users = (RobotoThinButton) view.findViewById(R.id.users);
		users.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// UserListActivity.openUsers(getActivity());
				// ((BaseActivity) getActivity()).slidingMenu.toggle(true);
				((BaseActivity) getActivity()).menuItemClick(Const.ITEM_USERS);
			}
		});

		groups = (RobotoThinButton) view.findViewById(R.id.groups);
		groups.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// GroupListActivity.openGroups(getActivity());
				// ((BaseActivity) getActivity()).slidingMenu.toggle(true);
				((BaseActivity) getActivity()).menuItemClick(Const.ITEM_GROUPS);
			}
		});

		logout = (RobotoThinButton) view.findViewById(R.id.logout);
		logout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
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
			}
		});

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!image.equals(Helper.getUserImage(getActivity()))) {
			image = Helper.getUserImage(getActivity());
			imageLoader.displayImage(getActivity(), image, userImage, true);
		}
	}

}

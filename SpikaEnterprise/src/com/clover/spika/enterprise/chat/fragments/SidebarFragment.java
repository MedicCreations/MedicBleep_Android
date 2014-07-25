package com.clover.spika.enterprise.chat.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.clover.spika.enterprise.chat.GroupListActivity;
import com.clover.spika.enterprise.chat.LobbyActivity;
import com.clover.spika.enterprise.chat.ProfileActivity;
import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.UserListActivity;
import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.extendables.SpikaEnterpriseApp;
import com.clover.spika.enterprise.chat.lazy.ImageLoader;
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
		userName.setText(Helper.getUserFirstName(getActivity())+"\n"+Helper.getUserLastName(getActivity()));

		profile = (Button) view.findViewById(R.id.profile);
		profile.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ProfileActivity.openProfile(getActivity(), null);
                ((BaseActivity) getActivity()).slidingMenu.toggle(true);
			}
		});

		lobby = (RobotoThinButton) view.findViewById(R.id.lobby);
		lobby.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				LobbyActivity.openLobby(getActivity());
				((BaseActivity) getActivity()).slidingMenu.toggle(true);
			}
		});

		users = (RobotoThinButton) view.findViewById(R.id.users);
		users.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				UserListActivity.openUsers(getActivity());
				((BaseActivity) getActivity()).slidingMenu.toggle(true);
			}
		});

		groups = (RobotoThinButton) view.findViewById(R.id.groups);
		groups.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				GroupListActivity.openGroups(getActivity());
				((BaseActivity) getActivity()).slidingMenu.toggle(true);
			}
		});

		logout = (RobotoThinButton) view.findViewById(R.id.logout);
		logout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO
				SpikaEnterpriseApp.getSharedPreferences(getActivity()).clear();
				
				Helper.logout(getActivity());

			}
		});

		return view;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if(!image.equals(Helper.getUserImage(getActivity()))){
			image = Helper.getUserImage(getActivity());
			imageLoader.displayImage(getActivity(), image, userImage, true);
		}
	}

}

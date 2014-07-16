package com.clover.spika.enterprise.chat.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.views.RobotoThinButton;
import com.clover.spika.enterprise.chat.views.RobotoThinTextView;
import com.clover.spika.enterprise.chat.views.RoundImageView;

public class SidebarFragment extends Fragment {

	OnClickListener listener;

	RoundImageView userImage;
	RobotoThinTextView userName;

	Button profile;
	RobotoThinButton lobby;
	RobotoThinButton users;
	RobotoThinButton groups;
	RobotoThinButton logout;

	public SidebarFragment(OnClickListener listener) {
		this.listener = listener;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.sidebar_layout, container, false);

		Log.d("Vida", "onCreateView");

		userImage = (RoundImageView) view.findViewById(R.id.userImage);
		userName = (RobotoThinTextView) view.findViewById(R.id.userName);

		profile = (Button) view.findViewById(R.id.profile);
		profile.setOnClickListener(listener);

		lobby = (RobotoThinButton) view.findViewById(R.id.lobby);

		users = (RobotoThinButton) view.findViewById(R.id.users);

		groups = (RobotoThinButton) view.findViewById(R.id.groups);
		groups.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.d("Vida", "Click");
			}
		});

		logout = (RobotoThinButton) view.findViewById(R.id.logout);
		logout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
			}
		});

		return view;
	}

}

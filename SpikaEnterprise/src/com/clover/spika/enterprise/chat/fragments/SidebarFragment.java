package com.clover.spika.enterprise.chat.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.clover.spika.enterprise.chat.GroupListActivity;
import com.clover.spika.enterprise.chat.LoginActivity;
import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.UserListActivity;
import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.extendables.SpikaEnterpriseApp;
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

	public SidebarFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.sidebar_layout, container, false);

		userImage = (RoundImageView) view.findViewById(R.id.userImage);
		userName = (RobotoThinTextView) view.findViewById(R.id.userName);

		profile = (Button) view.findViewById(R.id.profile);
		profile.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				((BaseActivity)getActivity()).openProfile(null);
			}
		});

		lobby = (RobotoThinButton) view.findViewById(R.id.lobby);
		lobby.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
			}
		});

        users = (RobotoThinButton) view.findViewById(R.id.users);
        users.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ((BaseActivity)getActivity()).startActivity(new Intent(getActivity(), UserListActivity.class));
            }
        });

		groups = (RobotoThinButton) view.findViewById(R.id.groups);
		groups.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				((BaseActivity)getActivity()).startActivity(new Intent(getActivity(), GroupListActivity.class));
			}
		});

		logout = (RobotoThinButton) view.findViewById(R.id.logout);
		logout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
                SpikaEnterpriseApp.getSharedPreferences(getActivity()).clear();

                Intent logoutIntent = new Intent(getActivity(), LoginActivity.class);
                logoutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(logoutIntent);
                getActivity().finish();
			}
		});

		return view;
	}

}

package com.clover.spika.enterprise.chat.fragments;

import com.clover.spika.enterprise.chat.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class UserLobbyFragment extends Fragment {
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_lobby, container, false);
		TextView tv = (TextView) view.findViewById(R.id.tempText);
		tv.setText("USERS LOBBY");
		return view;
	}
}

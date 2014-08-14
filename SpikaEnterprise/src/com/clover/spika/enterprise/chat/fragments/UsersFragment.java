package com.clover.spika.enterprise.chat.fragments;

import com.clover.spika.enterprise.chat.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

// TODO implement users
public class UsersFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		int color = getResources().getColor(R.color.chat_menu_gray);
		// construct the RelativeLayout
		RelativeLayout v = new RelativeLayout(getActivity());
		v.setBackgroundColor(color);

		return v;
	}
}

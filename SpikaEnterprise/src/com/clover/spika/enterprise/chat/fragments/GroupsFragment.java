package com.clover.spika.enterprise.chat.fragments;

import com.clover.spika.enterprise.chat.R;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

// TODO implement groups
public class GroupsFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		int color = getResources().getColor(R.color.default_green_light);
		// construct the RelativeLayout
		RelativeLayout v = new RelativeLayout(getActivity());
		v.setBackgroundColor(color);

		return v;
	}
}

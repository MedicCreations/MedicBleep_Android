package com.clover.spika.enterprise.chat.fragments;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ToggleButton;

import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.api.ApiCallback;
import com.clover.spika.enterprise.chat.api.LobbyApi;
import com.clover.spika.enterprise.chat.extendables.CustomFragment;
import com.clover.spika.enterprise.chat.listeners.LobbyChangedListener;
import com.clover.spika.enterprise.chat.models.LobbyModel;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.utils.Const;

public class LobbyFragment extends CustomFragment implements OnPageChangeListener, OnClickListener {

	ViewPager viewPager;
	ToggleButton groupsTab;
	ToggleButton usersTab;

	LobbyModel model;
	List<LobbyChangedListener> lobbyChangedListener = new ArrayList<LobbyChangedListener>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_lobby, container, false);

		viewPager = (ViewPager) rootView.findViewById(R.id.viewPager);
		viewPager.setAdapter(new SampleFragmentPagerAdapter());
		viewPager.setOnPageChangeListener(this);

		groupsTab = (ToggleButton) rootView.findViewById(R.id.groupsTab);
		groupsTab.setOnClickListener(this);
		usersTab = (ToggleButton) rootView.findViewById(R.id.usersTab);
		usersTab.setOnClickListener(this);

		return rootView;
	}

	private void setLobbyChangedListener(LobbyChangedListener listener) {
		if (!this.lobbyChangedListener.contains(listener)) {
			this.lobbyChangedListener.add(listener);
		}
	}

	public void getLobby(LobbyChangedListener listener) {

		if (listener == null)
			return;

		setLobbyChangedListener(listener);
		if (this.model != null) {
			listener.onChangeAll(this.model);
		}

	}

	@Override
	public void onResume() {
		super.onResume();
		setTabsStates(viewPager.getCurrentItem());
		getAllLobby(true, 0);
	}

	private void getAllLobby(boolean showProgress, int page) {
		new LobbyApi().getLobbyByType(page, Const.ALL_TYPE, getActivity(), showProgress, new ApiCallback<LobbyModel>() {

			@Override
			public void onApiResponse(Result<LobbyModel> result) {
				if (result.isSuccess()) {
					model = result.getResultData();
					for (LobbyChangedListener listener : lobbyChangedListener) {
						listener.onChangeAll(result.getResultData());
					}
				}
			}
		});
	}

	public class SampleFragmentPagerAdapter extends FragmentPagerAdapter {

		final int PAGE_COUNT = 2;

		public SampleFragmentPagerAdapter() {
			super(getChildFragmentManager());
		}

		@Override
		public int getCount() {
			return PAGE_COUNT;
		}

		@Override
		public Fragment getItem(int position) {
			return position == 0 ? new LobbyUsersFragment() : new LobbyGroupsFragment();
		}
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
	}

	@Override
	public void onPageSelected(int position) {
		setTabsStates(position);
	}

	@Override
	public void onClick(View view) {
		if (view == groupsTab) {
			setTabsStates(0);
			viewPager.setCurrentItem(0, true);
		} else if (view == usersTab) {
			setTabsStates(1);
			viewPager.setCurrentItem(1, true);
		}
	}

	void setTabsStates(int position) {
		if (position == 0) {
			groupsTab.setChecked(true);
			usersTab.setChecked(false);
		} else {
			groupsTab.setChecked(false);
			usersTab.setChecked(true);
		}
	}

}

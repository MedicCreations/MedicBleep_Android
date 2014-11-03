package com.clover.spika.enterprise.chat.fragments;

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
import com.clover.spika.enterprise.chat.extendables.CustomFragment;
import com.clover.spika.enterprise.chat.models.LobbyModel;

public class LobbyFragment extends CustomFragment implements OnPageChangeListener, OnClickListener {

	ViewPager viewPager;
	ToggleButton groupsTab;
	ToggleButton usersTab;
	ToggleButton allTab;

	LobbyModel model;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_lobby, container, false);

		viewPager = (ViewPager) rootView.findViewById(R.id.viewPager);
		viewPager.setAdapter(new LobbyFragmentPagerAdapter());
		viewPager.setOffscreenPageLimit(2);
		viewPager.setOnPageChangeListener(this);

		allTab = (ToggleButton) rootView.findViewById(R.id.allTab);
		allTab.setOnClickListener(this);
		groupsTab = (ToggleButton) rootView.findViewById(R.id.groupsTab);
		groupsTab.setOnClickListener(this);
		usersTab = (ToggleButton) rootView.findViewById(R.id.usersTab);
		usersTab.setOnClickListener(this);

		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		setTabsStates(viewPager.getCurrentItem());

	}

    public class LobbyFragmentPagerAdapter extends FragmentPagerAdapter {

        final int PAGE_COUNT = 3;

        public LobbyFragmentPagerAdapter() {
            super(getChildFragmentManager());
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }

        @Override
        public Fragment getItem(int position) {
        	switch (position) {
			case 0:
				return new LobbyAllFragment();
				
			case 1:
				return new LobbyUsersFragment();
				
			case 2:
				return new LobbyGroupsFragment();

			default:
				return null;
			}
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
		if (view == allTab){
			setTabsStates(0);
			viewPager.setCurrentItem(0, true);
		} else if (view == usersTab) {
			setTabsStates(2);
			viewPager.setCurrentItem(2, true);
		} else if (view == groupsTab) {
			setTabsStates(1);
			viewPager.setCurrentItem(1, true);
		}
	}

	void setTabsStates(int position) {
		if (position == 0) {
			allTab.setChecked(true);
			groupsTab.setChecked(false);
			usersTab.setChecked(false);
		} else if (position == 1) {
			allTab.setChecked(false);
			groupsTab.setChecked(true);
			usersTab.setChecked(false);
		} else if (position == 2) {
			allTab.setChecked(false);
			groupsTab.setChecked(false);
			usersTab.setChecked(true);
		}
	}
}

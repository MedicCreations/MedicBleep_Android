package com.clover.spika.enterprise.chat.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import com.clover.spika.enterprise.chat.MainActivity;
import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.extendables.CustomFragment;

public class HomeFragment extends CustomFragment implements View.OnClickListener{
	
	private static final int RECENT_FRAGMENT = 1;
	private static final int PEOPLE_FRAGMENT = 2;
	private static final int GROUPS_FRAGMENT = 3;
	
	private Button btnRecent;
	private Button btnPeople;
	private Button btnGroups;
	
	private RecentFragment recentFragment;
	private PeopleFragment peopleFragment;
	private GroupsFragment groupsFragment;
	
	FrameLayout fragmentHolder;
	
	private int activeFragment = RECENT_FRAGMENT;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_home, container, false);
		
		recentFragment = new RecentFragment();
		peopleFragment = new PeopleFragment();
		groupsFragment = new GroupsFragment();
		
		initView(rootView);
		
		getFragmentManager().beginTransaction().add(fragmentHolder.getId(), recentFragment, recentFragment.getClass().toString()).commit();
		
		if(getActivity() instanceof MainActivity)((MainActivity)getActivity()).setScreenTitle(getString(R.string.recent));

		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void handlePushNotificationInFragment(String chatId) {
		if(activeFragment == RECENT_FRAGMENT){
			recentFragment.handlePushNotificationInFragment(chatId);
		}
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		
		case R.id.rlRecent:
			if(btnRecent.isSelected()) return;
			btnRecent.setSelected(true);
            ((ViewGroup)btnRecent.getParent()).setSelected(true);
			setSelected(btnRecent);
			setFragment(RECENT_FRAGMENT);
			break;
			
		case R.id.rlPeople:
			if(btnPeople.isSelected()) return;
			btnPeople.setSelected(true);
            ((ViewGroup)btnPeople.getParent()).setSelected(true);
			setSelected(btnPeople);
			setFragment(PEOPLE_FRAGMENT);
			break;
			
		case R.id.rlGroups:
			if(btnGroups.isSelected()) return;
			btnGroups.setSelected(true);
            ((ViewGroup)btnGroups.getParent()).setSelected(true);
			setSelected(btnGroups);
			setFragment(GROUPS_FRAGMENT);
			break;

		default:
			break;
		}
	}
	
	private void setFragment(int selectedFragment) {
		switch (selectedFragment) {
		
		case RECENT_FRAGMENT:
			if(getActivity() instanceof MainActivity)((MainActivity)getActivity()).setScreenTitle(getString(R.string.recent));
			getFragmentManager().beginTransaction().replace(fragmentHolder.getId(), recentFragment, recentFragment.getClass().toString()).commit();
			activeFragment = RECENT_FRAGMENT;
			break;
			
		case PEOPLE_FRAGMENT:
			if(getActivity() instanceof MainActivity)((MainActivity)getActivity()).setScreenTitle(getString(R.string.colleagues));
			getFragmentManager().beginTransaction().replace(fragmentHolder.getId(), peopleFragment, peopleFragment.getClass().toString()).commit();
			activeFragment = PEOPLE_FRAGMENT;
			break;
			
		case GROUPS_FRAGMENT:
			if(getActivity() instanceof MainActivity)((MainActivity)getActivity()).setScreenTitle(getString(R.string.groups));
			getFragmentManager().beginTransaction().replace(fragmentHolder.getId(), groupsFragment, groupsFragment.getClass().toString()).commit();
			activeFragment = GROUPS_FRAGMENT;
			break;

		default:
			break;
		}
	}

	private void setSelected(View view) {
		if(view.getId() != btnRecent.getId()) {
            btnRecent.setSelected(false);
            ((ViewGroup)btnRecent.getParent()).setSelected(false);
        }
        if(view.getId() != btnPeople.getId()) {
            btnPeople.setSelected(false);
            ((ViewGroup)btnPeople.getParent()).setSelected(false);
        }
        if(view.getId() != btnGroups.getId()) {
            btnGroups.setSelected(false);
            ((ViewGroup)btnGroups.getParent()).setSelected(false);
        }
	}

	private void initView(View view) {
		btnRecent = (Button) view.findViewById(R.id.btnRecent);
		btnPeople = (Button) view.findViewById(R.id.btnPeople);
		btnGroups = (Button) view.findViewById(R.id.btnGroups);
		btnRecent.setClickable(false);
		btnPeople.setClickable(false);
		btnGroups.setClickable(false);
		view.findViewById(R.id.rlRecent).setOnClickListener(this);
		view.findViewById(R.id.rlPeople).setOnClickListener(this);
		view.findViewById(R.id.rlGroups).setOnClickListener(this);
		
		btnRecent.setSelected(true);
        ((ViewGroup)btnRecent.getParent()).setSelected(true);
		
		fragmentHolder = (FrameLayout) view.findViewById(R.id.contentForFragments);
	}

    public int whoIsActive(){
        if (btnRecent.isSelected()) return 1;
        else if (btnPeople.isSelected()) return 2;
        else if (btnGroups.isSelected()) return 3;
        return 1;
    }
	
	public PeopleFragment getPeopleFragment(){
		return peopleFragment;
	}

}

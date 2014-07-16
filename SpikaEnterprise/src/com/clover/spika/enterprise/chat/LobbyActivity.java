package com.clover.spika.enterprise.chat;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ToggleButton;

import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.fragments.GroupLobbyFragment;
import com.clover.spika.enterprise.chat.fragments.UserLobbyFragment;

public class LobbyActivity extends BaseActivity implements OnPageChangeListener, OnClickListener {

	ViewPager viewPager;
	ToggleButton groupsTab;
	ToggleButton usersTab;
	
	@Override
	public void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		requestWindowFeature(Window.FEATURE_ACTION_BAR);
		setContentView(R.layout.activity_lobby);
		
		viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.setAdapter(new SampleFragmentPagerAdapter());	
        viewPager.setOnPageChangeListener(this);
        
        groupsTab = (ToggleButton) findViewById(R.id.groupsTab);
        groupsTab.setOnClickListener(this);
        usersTab = (ToggleButton) findViewById(R.id.usersTab);
        usersTab.setOnClickListener(this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		setTabsStates(viewPager.getCurrentItem());
	}

	public class SampleFragmentPagerAdapter extends FragmentPagerAdapter {
		
        final int PAGE_COUNT = 2;
        
        public SampleFragmentPagerAdapter() {
            super(getSupportFragmentManager());
        }
        
        @Override
        public int getCount() {
            return PAGE_COUNT;
        }
        
        @Override
        public Fragment getItem(int position) {
            return position == 0 ? new UserLobbyFragment() : new GroupLobbyFragment();
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
		Log.e("ping", "state");
		setTabsStates(position);
	}

	@Override
	public void onClick(View view) {
		if (view == groupsTab) {
			setTabsStates(0);
			viewPager.setCurrentItem(0, true);
		} 
		else if (view == usersTab) {
			setTabsStates(1);
			viewPager.setCurrentItem(1, true);
		}
	}
	
	void setTabsStates (int position) {
		if (position == 0) {
    		groupsTab.setChecked(true);
    		usersTab.setChecked(false);
    	}
    	else {
    		groupsTab.setChecked(false);
    		usersTab.setChecked(true);
    	}
	}
}
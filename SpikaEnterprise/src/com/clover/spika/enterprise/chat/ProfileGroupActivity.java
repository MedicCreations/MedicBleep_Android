package com.clover.spika.enterprise.chat;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ToggleButton;

import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.fragments.ProfileGroupFragment;
import com.clover.spika.enterprise.chat.utils.Const;

public class ProfileGroupActivity extends BaseActivity implements OnPageChangeListener, OnClickListener {

	ViewPager viewPager;
	ToggleButton profileTab;
	ToggleButton membersTab;
	
	public static void openOtherProfile(Context context, String fileId, String chatName, boolean isAdmin) {

		Intent intent = new Intent(context, ProfileGroupActivity.class);

		intent.putExtra(Const.IMAGE, fileId);
		intent.putExtra(Const.CHAT_NAME, chatName);
		intent.putExtra(Const.IS_ADMIN, isAdmin);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		context.startActivity(intent);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile_group);

		findViewById(R.id.goBack).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		viewPager = (ViewPager) findViewById(R.id.viewPager);
		viewPager.setAdapter(new SampleFragmentPagerAdapter());
		viewPager.setOnPageChangeListener(this);

		profileTab = (ToggleButton) findViewById(R.id.profileTab);
		profileTab.setOnClickListener(this);
		membersTab = (ToggleButton) findViewById(R.id.membersTab);
		membersTab.setOnClickListener(this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		setTabsStates(viewPager.getCurrentItem());
	}
	
	public class SampleFragmentPagerAdapter extends FragmentPagerAdapter {

		final int PAGE_COUNT = 2;

		public SampleFragmentPagerAdapter() {
			super(getFragmentManager());
		}

		@Override
		public int getCount() {
			return PAGE_COUNT;
		}

		@Override
		public Fragment getItem(int position) {
			return position == 0 ? new ProfileGroupFragment(getIntent()) : new Fragment();
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
		if (view == profileTab) {
			setTabsStates(0);
			viewPager.setCurrentItem(0, true);
		} else if (view == membersTab) {
			setTabsStates(1);
			viewPager.setCurrentItem(1, true);
		}
	}

	void setTabsStates(int position) {
		if (position == 0) {
			profileTab.setChecked(true);
			membersTab.setChecked(false);
		} else {
			profileTab.setChecked(false);
			membersTab.setChecked(true);
		}
	}
}

package com.clover.spika.enterprise.chat;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ToggleButton;

import com.clover.spika.enterprise.chat.api.ApiCallback;
import com.clover.spika.enterprise.chat.api.LobbyApi;
import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.fragments.GroupLobbyFragment;
import com.clover.spika.enterprise.chat.fragments.UserLobbyFragment;
import com.clover.spika.enterprise.chat.listeners.LobbyChangedListener;
import com.clover.spika.enterprise.chat.models.LobbyModel;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.utils.Const;

public class LobbyActivity extends BaseActivity implements OnPageChangeListener, OnClickListener {

	ViewPager viewPager;
	ToggleButton groupsTab;
	ToggleButton usersTab;

	private LobbyModel model;
	private List<LobbyChangedListener> lobbyChangedListener = new ArrayList<LobbyChangedListener>();
	
	public static void openLobby(Context context) {
        Intent intent = new Intent(context, LobbyActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

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
	protected void onResume() {
		super.onResume();
		setTabsStates(viewPager.getCurrentItem());
		getAllLobby(true, 0);
	}

	private void getAllLobby(boolean showProgress, int page) {
		new LobbyApi().getLobbyByType(page, Const.ALL_TYPE, this, showProgress, new ApiCallback<LobbyModel>() {

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
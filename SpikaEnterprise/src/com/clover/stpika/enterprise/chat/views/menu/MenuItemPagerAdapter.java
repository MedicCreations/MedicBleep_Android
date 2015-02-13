package com.clover.stpika.enterprise.chat.views.menu;

import java.util.ArrayList;
import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class MenuItemPagerAdapter extends FragmentPagerAdapter {

	private List<String> imagesList = new ArrayList<String>();
	private int count;
	private SelectImageListener listener;
	
	public MenuItemPagerAdapter(FragmentManager fm, int numberOfFragment, List<String> filesList, SelectImageListener lis) {
		super(fm);
		imagesList = filesList;
		count = numberOfFragment;
		listener = lis;
	}

	@Override
	public int getCount() {
		return count;
	}

	@Override
	public Fragment getItem(int position) {
		MenuItemFragment frag = MenuItemFragment.newInstance(imagesList, position);
		frag.setListener(listener);
		return frag;
	}

}

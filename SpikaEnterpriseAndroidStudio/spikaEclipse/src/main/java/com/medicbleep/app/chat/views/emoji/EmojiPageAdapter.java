package com.medicbleep.app.chat.views.emoji;

import java.util.ArrayList;
import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.medicbleep.app.chat.models.StickersHolder;

public class EmojiPageAdapter extends FragmentPagerAdapter {

	private List<Fragment> mFragmentList = new ArrayList<Fragment>();
	
	public EmojiPageAdapter(FragmentManager fm, int numberOfFragment, StickersHolder holder, SelectEmojiListener listener) {
		super(fm);
		for(int i = 0; i < numberOfFragment; i++){
			EmojiFragment frag = EmojiFragment.newInstance(holder, i);
			frag.setListener(listener);
			mFragmentList.add(frag);
		}
	}

	@Override
	public int getCount() {
		return mFragmentList.size();
	}

	@Override
	public Fragment getItem(int position) {
		return mFragmentList.get(position);
	}

}

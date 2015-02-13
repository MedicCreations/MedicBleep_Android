package com.clover.stpika.enterprise.chat.views.menu;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.utils.Helper;

public class FrameLayoutForMenuPager extends FrameLayout{
	
	private ViewPager viewPager;
	private Context c;
	
	public FrameLayoutForMenuPager(Context context) {
		super(context);
		createView(context);
	}
	
	public FrameLayoutForMenuPager(Context context, AttributeSet attrs) {
		super(context, attrs);
		createView(context);
	}
	
	public FrameLayoutForMenuPager(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		createView(context);
	}
	
	public void createView(Context c) {
		this.c = c;
		ViewGroup convertView = (ViewGroup) LayoutInflater.from(c).inflate(R.layout.layout_frame_for_menu_pager, this, false);

		viewPager = (ViewPager) convertView.findViewById(R.id.viewPagerMenu);

		this.addView(convertView);
	}
	
	public void setViews(SelectImageListener lis){

		List<String> imagesList = Helper.getAllShownImagesPath((Activity) c);
		
		int numberOfPager = (imagesList.size() / 4) + 1;
		
		MenuItemPagerAdapter adapter = new MenuItemPagerAdapter(((BaseActivity)c).getSupportFragmentManager(), numberOfPager, imagesList, lis);
		viewPager.setAdapter(adapter);
		
	}
	
	public void clearAdapters(){
		viewPager.setAdapter(null);
	}

}

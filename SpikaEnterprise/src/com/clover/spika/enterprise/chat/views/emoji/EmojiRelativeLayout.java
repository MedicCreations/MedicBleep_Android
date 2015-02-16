package com.clover.spika.enterprise.chat.views.emoji;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.models.Stickers;
import com.clover.spika.enterprise.chat.models.StickersHolder;
import com.clover.spika.enterprise.chat.utils.Utils;

public class EmojiRelativeLayout extends RelativeLayout {
	
	private List<Stickers> stickersList = new ArrayList<Stickers>();
	private ViewPager viewPager;
	private ProgressBar progressLoading;
	
	public EmojiRelativeLayout(Context context) {
		super(context);
		createView(context);
	}
	
	public EmojiRelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		createView(context);
	}
	
	public EmojiRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		createView(context);
	}

//	public EmojiRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
//		super(context, attrs, defStyleAttr, defStyleRes);
//	}
	
	public void setStickersList(List<Stickers> stickersList, Context c, SelectEmojiListener lis){
		this.stickersList.addAll(stickersList);
		StickersHolder holder = new StickersHolder();
		holder.stickers = stickersList;
		int number = 1 + stickersList.size() / 8;
		viewPager.setAdapter(new EmojiPageAdapter(((BaseActivity)c).getSupportFragmentManager(), number, holder, lis));
		
		final LinearLayout dotsLayout = (LinearLayout) findViewById(R.id.dotsLayout);
		
		for(int i = 0; i < number; i++){
			ImageView dot = new ImageView(c);
			dot.setImageResource(R.drawable.selector_dot);
			dotsLayout.addView(dot);
			int padd = Utils.getPxFromDp(5, getResources());
			dot.setPadding(padd, 0, padd, 0);
			if(i == 0) dot.setSelected(true);
		}
		
		viewPager.setOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int arg0) {
				for(int i = 0; i < dotsLayout.getChildCount(); i++){
					if(i == arg0) dotsLayout.getChildAt(i).setSelected(true);
					else dotsLayout.getChildAt(i).setSelected(false);
				}
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {}
		});
		
		progressLoading.setVisibility(View.GONE);
	}
	
	public void resetDotsIfNeed(){
		LinearLayout dotsLayout = (LinearLayout) findViewById(R.id.dotsLayout);
		
		int active = 0;
		if(viewPager != null){
			active = viewPager.getCurrentItem();
		}
		
		for(int i = 0; i < dotsLayout.getChildCount(); i++){
			if(i == active) dotsLayout.getChildAt(i).setSelected(true);
			else dotsLayout.getChildAt(i).setSelected(false);
		}
	}
	
	public void createView(Context c){
		 ViewGroup convertView = (ViewGroup) LayoutInflater.from(c).inflate(R.layout.layout_emoji_relative, this, false);
		 
		 viewPager =  (ViewPager) convertView.findViewById(R.id.viewPager);
		 progressLoading =  (ProgressBar) convertView.findViewById(R.id.progressBarLoading);
		 
		 this.addView(convertView);
	}

}

package com.clover.spika.enterprise.chat.views.emoji;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.extendables.CustomFragment;
import com.clover.spika.enterprise.chat.lazy.GifLoader;
import com.clover.spika.enterprise.chat.listeners.OnImageDisplayFinishListener;
import com.clover.spika.enterprise.chat.models.Stickers;
import com.clover.spika.enterprise.chat.models.StickersHolder;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Utils;

public class EmojiFragment extends CustomFragment {
	
	private SelectEmojiListener mListener;
	
	public static EmojiFragment newInstance(StickersHolder holder, int position) {
		EmojiFragment fragment = new EmojiFragment();
		Bundle arguments = new Bundle();
		arguments.putSerializable(Const.STICEKRS_HOLDER, holder);
		arguments.putSerializable(Const.POSITION, position);
		fragment.setArguments(arguments);
		return fragment;
	}
	
	public void setListener(SelectEmojiListener lis){
		mListener = lis;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		View superView = inflater.inflate(R.layout.layout_emoji_fragment, container, false);
		
		int position = getArguments().getInt(Const.POSITION, 0);
		List<Stickers> listAll = ((StickersHolder) getArguments().getSerializable(Const.STICEKRS_HOLDER)).getStickersList();
		
		List<Stickers> listLocal = new ArrayList<Stickers>();
		
		for(int i = 0; i < 8; i++){
			if((position * 8) + i > listAll.size() - 1) break;
			listLocal.add(listAll.get((position * 8) + i));
		}
		
		int widthScreen = getResources().getDisplayMetrics().widthPixels;
		int imageSize = ((widthScreen - Utils.getPxFromDp(40, getResources())) / 4) - Utils.getPxFromDp(10, getResources());
		
		for(int i = 0; i < listLocal.size(); i++){
			int idRes = 0;
			if(i < 4){
				idRes = getResources().getIdentifier("llFirstRow" + (i + 1), "id", getActivity().getPackageName());
			}else{
				idRes = getResources().getIdentifier("llSecondRow" + (i + 1 - 4), "id", getActivity().getPackageName());
			}
			
			LinearLayout ll = (LinearLayout) superView.findViewById(idRes);
			
			final ImageView iv = new ImageView(getActivity());
			iv.setScaleType(ScaleType.FIT_XY);
			iv.setVisibility(View.GONE);
			
			final ProgressBar pbLoading = new ProgressBar(getActivity());
			ll.addView(pbLoading);
			
			GifLoader gif = new GifLoader(getActivity());
//			gif.displayImage(getActivity(), listLocal.get(i).getUrl(), iv, new OnImageDisplayFinishListener() {
//				
//				@Override
//				public void onFinish() {
//					GifAnimationDrawable big;
//					try {
////						big = new GifAnimationDrawable((File) iv.getTag(), getActivity());
//						big = (GifAnimationDrawable) iv.getTag();
//						big.setOneShot(false);
//						iv.setImageDrawable(big);
//						big.setVisible(true, true);
//						
//						pbLoading.setVisibility(View.GONE);
//						iv.setVisibility(View.VISIBLE);
//					} catch (NullPointerException e) {
//						e.printStackTrace();
//					} 
//				}
//			});
			
			
			ll.addView(iv);
			
			iv.getLayoutParams().width = imageSize;
			iv.getLayoutParams().height = imageSize;
			
			final Stickers object = listLocal.get(i);
			iv.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if(mListener != null) mListener.onEmojiSelect(object);
				}
			});
			
		}
		
		return superView;
	}
	
}

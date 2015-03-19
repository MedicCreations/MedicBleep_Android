package com.clover.spika.enterprise.chat.views.emoji;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.LinearLayout;

import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.extendables.CustomFragment;
import com.clover.spika.enterprise.chat.extendables.SpikaEnterpriseApp;
import com.clover.spika.enterprise.chat.lazy.GifLoaderSpice;
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
		List<Stickers> listAll = ((StickersHolder) getArguments().getSerializable(Const.STICEKRS_HOLDER)).stickers;
		
		List<Stickers> listLocal = new ArrayList<Stickers>();
		
		for(int i = 0; i < 8; i++){
			if((position * 8) + i > listAll.size() - 1) break;
			listLocal.add(listAll.get((position * 8) + i));
		}
		
		int widthScreen = getResources().getDisplayMetrics().widthPixels;
		int imageSize = ((widthScreen - Utils.getPxFromDp(40, getResources())) / 4) - Utils.getPxFromDp(10, getResources());
		
		GifLoaderSpice spiceGifLoader = GifLoaderSpice.getInstance(getActivity());
		spiceGifLoader.setSpiceManager(spiceManager);
		
		for(int i = 0; i < listLocal.size(); i++){
			int idRes = 0;
			if(i < 4){
				idRes = getResources().getIdentifier("llFirstRow" + (i + 1), "id", getActivity().getPackageName());
			}else{
				idRes = getResources().getIdentifier("llSecondRow" + (i + 1 - 4), "id", getActivity().getPackageName());
			}
			
			LinearLayout ll = (LinearLayout) superView.findViewById(idRes);
			
			final ClickableWebView webView = new ClickableWebView(getActivity());
			webView.setVerticalScrollBarEnabled(false);
			webView.setHorizontalScrollBarEnabled(false);
			webView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
			ll.addView(webView);
			
			webView.getLayoutParams().width = imageSize;
			webView.getLayoutParams().height = imageSize;
			
			String style = "style=\"border: solid #eee 1px;border-radius: 10px;\"";
			spiceGifLoader.displayImage(getActivity(), listLocal.get(i).getUrl(), webView, style);
			
			final Stickers object = listLocal.get(i);
			webView.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					EmojiCounterHelper.increaseEmojiCounter(String.valueOf(object.getId()), SpikaEnterpriseApp.getSharedPreferences(getActivity()));
					if(mListener != null) mListener.onEmojiSelect(object);
				}
			});
			
		}
		
		return superView;
	}
	
}

package com.medicbleep.app.chat.fragments;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.medicbleep.app.chat.MainActivity;
import com.medicbleep.app.chat.R;
import com.medicbleep.app.chat.api.robospice.UserSpice;
import com.medicbleep.app.chat.extendables.CustomFragment;
import com.medicbleep.app.chat.models.Information;
import com.medicbleep.app.chat.services.robospice.CustomSpiceListener;
import com.medicbleep.app.chat.utils.Const;
import com.medicbleep.app.chat.utils.Helper;
import com.medicbleep.app.chat.utils.Utils;
import com.octo.android.robospice.persistence.exception.SpiceException;

public class InformationFragment extends CustomFragment {
	
	private WebView mWebView;
	private String mUrl;

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_information, container, false);

//		mWebView = (WebView) rootView.findViewById(R.id.webViewInformation);
//		mWebView.setWebViewClient(new CustomWebViewClient());
//		mWebView.setWebChromeClient(new WebChromeClient());
//		mWebView.getSettings().setJavaScriptEnabled(true);
		
		if (getActivity() instanceof MainActivity) {
			((MainActivity) getActivity()).disableCreateRoom();
		}
		
		return rootView;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
//		getInformation();
	}
	
	private void getInformation(){
		
		handleProgress(true);
		
		UserSpice.GetInformation getInformation = new UserSpice.GetInformation();
		spiceManager.execute(getInformation, new CustomSpiceListener<Information>(){
			
			@Override
			public void onRequestFailure(SpiceException arg0) {
				super.onRequestFailure(arg0);
				handleProgress(false);
				Utils.onFailedUniversal(null, getActivity());
			}
			
			@Override
			public void onRequestSuccess(Information result) {
				super.onRequestSuccess(result);
				handleProgress(false);
				
				if (result.getCode() == Const.API_SUCCESS) {
					mUrl = result.url;
					setUrl();
				}else{
					Utils.onFailedUniversal(Helper.errorDescriptions(getActivity(), result.getCode()), getActivity());
				}
			}
		});
	}
	
	private void setUrl(){
		mWebView.loadUrl(mUrl);
	}
	
	class CustomWebViewClient extends WebViewClient{
		
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			return super.shouldOverrideUrlLoading(view, url);
		}
		
		@Override
		public void onPageFinished(WebView view, String url) {
			if(getActivity() instanceof MainActivity) ((MainActivity)getActivity()).showSmallLoading(View.GONE);
			super.onPageFinished(view, url);
		}
		
		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			if(getActivity() instanceof MainActivity) ((MainActivity)getActivity()).showSmallLoading(View.VISIBLE);
			super.onPageStarted(view, url, favicon);
		}
		
	}
	
	@Override
	public void onDetach() {
		if(getActivity() instanceof MainActivity) ((MainActivity)getActivity()).showSmallLoading(View.GONE);
		super.onDetach();
	}

}

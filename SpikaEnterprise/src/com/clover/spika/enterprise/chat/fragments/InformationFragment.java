package com.clover.spika.enterprise.chat.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.extendables.CustomFragment;

public class InformationFragment extends CustomFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_information, container, false);
		
		WebView webView = (WebView) rootView.findViewById(R.id.webViewInformation);
		webView.loadUrl("http://www.google.ba");

		return rootView;
	}

}

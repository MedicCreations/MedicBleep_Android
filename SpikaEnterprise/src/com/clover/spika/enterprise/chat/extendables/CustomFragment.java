package com.clover.spika.enterprise.chat.extendables;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.clover.spika.enterprise.chat.dialogs.AppProgressDialog;
import com.clover.spika.enterprise.chat.lazy.ImageLoaderSpice;
import com.clover.spika.enterprise.chat.services.robospice.OkHttpService;
import com.octo.android.robospice.SpiceManager;

public class CustomFragment extends Fragment {

	protected SpiceManager spiceManager = new SpiceManager(OkHttpService.class);
	private ImageLoaderSpice imageLoaderSpice;

	public ImageLoaderSpice getImageLoader() {
		return imageLoaderSpice;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		imageLoaderSpice = ImageLoaderSpice.getInstance(getActivity());
		imageLoaderSpice.setSpiceManager(spiceManager);
	}

	@Override
	public void onStart() {
		super.onStart();
		spiceManager.start(getActivity());
	}

	@Override
	public void onStop() {
		if (spiceManager.isStarted()) {
			spiceManager.shouldStop();
		}
		super.onStop();
	}

	private AppProgressDialog progressBar;

	public void handleProgress(boolean showProgress) {

		try {

			if (showProgress) {

				if (progressBar != null && progressBar.isShowing()) {
					progressBar.dismiss();
					progressBar = null;
				}

				progressBar = new AppProgressDialog(getActivity());
				progressBar.show();

			} else {

				if (progressBar != null && progressBar.isShowing()) {
					progressBar.dismiss();
				}

				progressBar = null;
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void onClosed() {
	}

	public void handlePushNotificationInFragment(String chatId) {
	}

}

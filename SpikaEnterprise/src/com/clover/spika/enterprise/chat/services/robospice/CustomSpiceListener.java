package com.clover.spika.enterprise.chat.services.robospice;

import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

public class CustomSpiceListener<T> implements RequestListener<T> {

	@Override
	public void onRequestFailure(SpiceException arg0) {
	}

	@Override
	public void onRequestSuccess(T arg0) {
	}

}

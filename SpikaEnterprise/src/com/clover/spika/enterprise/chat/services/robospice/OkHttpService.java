package com.clover.spika.enterprise.chat.services.robospice;

import android.app.Application;
import com.octo.android.robospice.okhttp.OkHttpSpiceService;
import com.octo.android.robospice.persistence.CacheManager;
import com.octo.android.robospice.persistence.exception.CacheCreationException;

public class OkHttpService extends OkHttpSpiceService {

	@Override
	public CacheManager createCacheManager(Application arg0) throws CacheCreationException {
		return new CacheManager();
	}

}
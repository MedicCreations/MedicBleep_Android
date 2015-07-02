package com.medicbleep.app.chat.services.robospice;

import android.app.Application;

import com.octo.android.robospice.okhttp.OkHttpSpiceService;
import com.octo.android.robospice.persistence.CacheManager;
import com.octo.android.robospice.persistence.exception.CacheCreationException;

public class OkHttpService extends OkHttpSpiceService {

	@Override
	public CacheManager createCacheManager(Application application) throws CacheCreationException {
		
		CacheManager cacheManager = new CacheManager();
		return cacheManager;
	}
	
	@Override
	public int getThreadCount() {
		return 3;
	}

}

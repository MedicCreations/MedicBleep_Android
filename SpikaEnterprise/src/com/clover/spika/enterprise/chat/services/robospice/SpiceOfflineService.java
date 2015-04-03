package com.clover.spika.enterprise.chat.services.robospice;

import android.app.Application;
import android.content.Context;

import com.octo.android.robospice.SpiceService;
import com.octo.android.robospice.networkstate.NetworkStateChecker;
import com.octo.android.robospice.persistence.CacheManager;
import com.octo.android.robospice.persistence.exception.CacheCreationException;

public class SpiceOfflineService extends SpiceService {

	@Override
	public CacheManager createCacheManager(Application application) throws CacheCreationException {
		
		CacheManager cacheManager = new CacheManager();
		return cacheManager;
	}
	
	@Override
	public int getThreadCount() {
		return 3;
	}
	
	@Override
	protected NetworkStateChecker getNetworkStateChecker() {
		return new NetworkStateChecker() {
			
			@Override
			public boolean isNetworkAvailable(Context arg0) {
				return true;
			}
			
			@Override
			public void checkPermissions(Context arg0) {}
		};
	}

}

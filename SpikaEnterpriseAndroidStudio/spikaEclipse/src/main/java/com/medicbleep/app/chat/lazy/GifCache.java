package com.medicbleep.app.chat.lazy;

import java.io.File;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import android.util.Log;

public class GifCache {

	private static final String TAG = "GifCache";

	// Last argument true for LRU ordering
	private Map<String, File> cache = Collections.synchronizedMap(new LinkedHashMap<String, File>(10, 1.5f, true));

	// current allocated size
	// max memory cache folder used to download images in bytes
	private long limit = 1000000;

	public GifCache() {

		// use 25% of available heap size
		setLimit(Runtime.getRuntime().maxMemory() / 4);
	}

	public void setLimit(long new_limit) {

		limit = new_limit;
		Log.i(TAG, "MemoryCache will use up to " + limit / 1024. / 1024. + "MB");
	}
	
	long getSizeInBytes(File file) {
		if (file == null || !file.exists())
			return 0;
		return file.length();
	}

	public File get(String id) {
		try {
			if (!cache.containsKey(id))
				return null;
			
			return cache.get(id);

		} catch (NullPointerException ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public void put(String id, File is) {
		try {
			cache.put(id, is);
		} catch (Throwable th) {
			th.printStackTrace();
		}
	}

	public void clear() {
		try {
			// Clear cache
			cache.clear();
		} catch (NullPointerException ex) {
			ex.printStackTrace();
		}
	}

}

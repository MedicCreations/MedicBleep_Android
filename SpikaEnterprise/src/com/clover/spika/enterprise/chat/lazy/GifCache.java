package com.clover.spika.enterprise.chat.lazy;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import android.graphics.Bitmap;
import android.util.Log;

import com.clover.spika.enterprise.chat.utils.Logger;
import com.clover.spika.enterprise.chat.views.emoji.GifAnimationDrawable;

public class GifCache {

	private static final String TAG = "GifCache";

	// singleton usage
	private static GifCache sInstance;

	public static GifCache getInstance() {
		if (sInstance == null) {
			Logger.e("ImageLoader has to be initialized first before instance can be used. " + "Call init method before usage.");
			init();
		}
		return sInstance;
	}

	public static void init() {
		sInstance = new GifCache();
	}

	// Last argument true for LRU ordering
	private Map<String, GifAnimationDrawable> cache = Collections.synchronizedMap(new LinkedHashMap<String, GifAnimationDrawable>(10, 1.5f, true));

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
	
	long getSizeInBytes(Bitmap bitmap) {
		if (bitmap == null)
			return 0;
		return bitmap.getRowBytes() * bitmap.getHeight();
	}

	public GifAnimationDrawable get(String id) {
		Log.d("LOG", "get id; "+id);
		Log.w("LOG", cache.toString());
		try {
			if (!cache.containsKey(id))
				return null;
			
			return cache.get(id);

		} catch (NullPointerException ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public void put(String id, GifAnimationDrawable is) {
		Log.d("LOG", "put id; "+id);
		try {
			cache.put(id, is);
			Log.i("LOG", cache.toString());
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

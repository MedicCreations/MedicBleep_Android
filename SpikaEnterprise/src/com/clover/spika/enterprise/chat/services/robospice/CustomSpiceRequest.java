package com.clover.spika.enterprise.chat.services.robospice;

import java.security.MessageDigest;

import android.content.Context;
import android.text.TextUtils;

import com.clover.spika.enterprise.chat.extendables.SpikaEnterpriseApp;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Helper;
import com.octo.android.robospice.request.okhttp.OkHttpSpiceRequest;
import com.squareup.okhttp.Headers;

public abstract class CustomSpiceRequest<T> extends OkHttpSpiceRequest<T> {

	public CustomSpiceRequest(Class<T> clazz) {
		super(clazz);
	}

	@Override
	public T loadDataFromNetwork() throws Exception {
		return null;
	}

	/**
	 * This method generates a unique cache key for this request.
	 * @return
	 */
	public String createCacheKey(String cacheKey) {

		try {

			byte[] bytesOfKey = cacheKey.getBytes("UTF-8");
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] digest = md.digest(bytesOfKey);

			return new String(digest, "Cp1252");

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public Headers getPostHeaders(Context ctx) {

		Headers.Builder headersBuilder = new Headers.Builder()
		.add("Encoding", "UTF-8")
		.add(Const.APP_VERSION, Helper.getAppVersion())
		.add(Const.PLATFORM, "android")
		.add("User-Agent", Const.HTTP_USER_AGENT);

		String token = SpikaEnterpriseApp.getSharedPreferences(ctx).getToken();
		if (!TextUtils.isEmpty(token)) {
			headersBuilder.add("token", token);
		}

		return headersBuilder.build();
	}

	public Headers getGetHeaders(Context ctx) {
		
		Headers.Builder headersBuilder = new Headers.Builder()
		.add("Encoding", "UTF-8")
		.add(Const.APP_VERSION, Helper.getAppVersion())
		.add(Const.PLATFORM, "android")
		.add("User-Agent", Const.HTTP_USER_AGENT);

		String token = SpikaEnterpriseApp.getSharedPreferences(ctx).getToken();
		if (!TextUtils.isEmpty(token)) {
			headersBuilder.add("token", token);
		}
		
		return headersBuilder.build();
	}

}

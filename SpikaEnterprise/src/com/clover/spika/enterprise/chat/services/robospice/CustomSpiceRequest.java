package com.clover.spika.enterprise.chat.services.robospice;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import android.content.Context;
import android.text.TextUtils;

import com.clover.spika.enterprise.chat.extendables.SpikaEnterpriseApp;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Helper;
import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;

public abstract class CustomSpiceRequest<T> extends SpringAndroidSpiceRequest<T> {

	public CustomSpiceRequest(Class<T> clazz) {
		super(clazz);
	}

	@Override
	public T loadDataFromNetwork() throws Exception {
		return null;
	}

	/**
	 * This method generates a unique cache key for this request.
	 * 
	 * TODO
	 * 
	 * @return
	 */
	public String createCacheKey() {

		try {

			String cacheKeyString = "TestKey";
			byte[] bytesOfKey = cacheKeyString.getBytes("UTF-8");
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] digest = md.digest(bytesOfKey);

			return new String(digest, "Cp1252");

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public HttpHeaders getPostHeader(Context ctx) {

		List<MediaType> acceptableMediaTypes = new ArrayList<MediaType>();
		acceptableMediaTypes.add(MediaType.ALL);

		HttpHeaders headers = new HttpHeaders();

		headers.setAccept(acceptableMediaTypes);

		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		headers.set("Encoding", "UTF-8");
		headers.set(Const.APP_VERSION, Helper.getAppVersion());
		headers.set(Const.PLATFORM, "android");

		String token = SpikaEnterpriseApp.getSharedPreferences(ctx).getToken();
		if (!TextUtils.isEmpty(token)) {
			headers.set("token", token);
		}

		return headers;
	}

	public HttpEntity<?> getGetheaders(Context ctx) {
		HttpEntity<?> httpEntity = new HttpEntity<Object>(getPostHeader(ctx));
		return httpEntity;
	}

}

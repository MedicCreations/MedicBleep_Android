package com.clover.spika.enterprise.chat.lazy;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebView;

import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.listeners.OnImageDisplayFinishListener;
import com.clover.spika.enterprise.chat.services.robospice.CustomSpiceListener;
import com.clover.spika.enterprise.chat.services.robospice.CustomSpiceRequest;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Logger;
import com.clover.spika.enterprise.chat.utils.Utils;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

public class GifLoaderSpice {

	public static int DEFAULT_USER_IMAGE = R.drawable.default_user_image;
	public static int DEFAULT_GROUP_IMAGE = R.drawable.default_group_image;
	public static int NO_IMAGE = 0;

	private static GifLoaderSpice instance;

	private Context ctx;

	private FileCache fileCache;
	private GifCache gifCache;
	private Map<WebView, String> imageViews = Collections.synchronizedMap(new WeakHashMap<WebView, String>());

	private SpiceManager spiceManager;

	private OnImageDisplayFinishListener onFinishListener;

	public static GifLoaderSpice getInstance(Context ctx) {

		if (instance == null) {
			instance = new GifLoaderSpice(ctx);
		}

		return instance;
	}

	private GifLoaderSpice(Context ctx) {

		this.ctx = ctx;
		fileCache = new FileCache(ctx);
		gifCache = new GifCache();
	}

	public void displayImage(Context ctx, String url, WebView webView, String style, OnImageDisplayFinishListener onFinishListener) {
		this.onFinishListener = onFinishListener;
		displayImage(ctx, url, webView, style);
	}

	public void displayImage(Context ctx, String url, final WebView webView, final String style) {

		if (TextUtils.isEmpty(url) || url.equals(Const.DEFAULT_IMAGE_GROUP) || url.equals(Const.DEFAULT_IMAGE_USER)) {
			webView.loadUrl(null);
			if (onFinishListener != null)
				onFinishListener.onFinish();
			return;
		}

		imageViews.put(webView, url);

		// Get from memmory cache
		File fileMemory = gifCache.get(url);

		if (fileMemory != null) {
			webView.loadDataWithBaseURL("", Utils.generateGifHTML(fileMemory.getAbsolutePath(), style), "text/html","utf-8", ""); 
			webView.setTag(fileMemory.getAbsolutePath());
			
			if (onFinishListener != null) {
				onFinishListener.onFinish();
			}

			return;
		}

		// Get from file cache
		File file = fileCache.getFile(url);

		if (file != null && file.exists()) {
			
			webView.loadDataWithBaseURL("", Utils.generateGifHTML(file.getAbsolutePath(), style), "text/html","utf-8", ""); 
			webView.setTag(file.getAbsolutePath());

			if (onFinishListener != null) {
				onFinishListener.onFinish();
			}

			ImageSpiceMemory imageSpiceMemory = new ImageSpiceMemory(url, file);
			spiceManager.execute(imageSpiceMemory, null);

			return;
		}

		ImageSpiceWeb imageSpice = new ImageSpiceWeb(webView, url);
		spiceManager.execute(imageSpice, new CustomSpiceListener<File>() {

			@Override
			public void onRequestFailure(SpiceException ex) {
				super.onRequestFailure(ex);

				if (onFinishListener != null) {
					onFinishListener.onFinish();
				}
			}

			@Override
			public void onRequestSuccess(File fileWeb) {

				webView.loadDataWithBaseURL("", Utils.generateGifHTML(fileWeb.getAbsolutePath(), style), "text/html","utf-8", ""); 
				webView.setTag(fileWeb.getAbsolutePath());

				if (onFinishListener != null) {
					onFinishListener.onFinish();
				}

				super.onRequestSuccess(fileWeb);
			}
		});
	}

	public void setSpiceManager(SpiceManager spiceManager) {
		Log.d("LOG", "TRY TO SET");
		if(this.spiceManager != null) {
			Log.d("LOG", "NO NEED");
			return;
		}
		Log.d("LOG", "SETTING");
		this.spiceManager = spiceManager;
	}

	private class ImageSpiceWeb extends CustomSpiceRequest<File> {

		private WebView webView;
		private String url;

		public ImageSpiceWeb(WebView webView, String url) {
			super(File.class);

			this.webView = webView;
			this.url = url;
		}

		@Override
		public File loadDataFromNetwork() throws Exception {
			
			Logger.i("Downloading image: " + url);

			// Check if image already downloaded
			if (isImageViewReused(webView, url)) {
				return null;
			}

			// Get from web
			Request.Builder requestBuilder = new Request.Builder().headers(getGetHeaders(ctx)).url(url).get();

			Call connection = getOkHttpClient().newCall(requestBuilder.build());

			Response res = connection.execute();
			ResponseBody resBody = res.body();

			File file = fileCache.getFile(url);

			if (!file.exists()) {
				file.createNewFile();
			}

			FileOutputStream fos = new FileOutputStream(file);
			fos.write(resBody.bytes());
			fos.flush();
			fos.close();
			
			gifCache.put(url, file);
			
			// Check if image already downloaded
			if (isImageViewReused(webView, url)) {
				return null;
			}
			
			return file;

		}
	}

	private class ImageSpiceMemory extends CustomSpiceRequest<File> {

		private String url;
		private File file;

		public ImageSpiceMemory(String url, File file) {
			super(File.class);
			this.url = url;
			this.file = file;
		}

		@Override
		public File loadDataFromNetwork() throws Exception {

			if (file != null) {
				gifCache.put(url, file);
			}

			return null;
		}
	}
	
	boolean isImageViewReused(WebView webView, String url) {

		String tag = imageViews.get(webView);
		// Check url is already exist in imageViews MAP
		if (tag == null || !tag.equals(url)){
			return true;
		}
		return false;
	}

}

package com.medicbleep.app.chat.lazy;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.widget.ImageView;

import com.medicbleep.app.chat.R;
import com.medicbleep.app.chat.listeners.OnImageDisplayFinishListener;
import com.medicbleep.app.chat.security.JNAesCrypto;
import com.medicbleep.app.chat.services.robospice.CustomSpiceListener;
import com.medicbleep.app.chat.services.robospice.CustomSpiceRequest;
import com.medicbleep.app.chat.utils.Const;
import com.medicbleep.app.chat.utils.Logger;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

public class ImageLoaderSpice {

	public static int DEFAULT_USER_IMAGE = R.drawable.default_user_image;
	public static int DEFAULT_GROUP_IMAGE = R.drawable.default_group_image;
	public static int NO_IMAGE = 0;

	private static ImageLoaderSpice instance;

	private Context ctx;

	private FileCache fileCache;
	private MemoryCache memoryCache;
	private Map<ImageView, String> imageViews = Collections.synchronizedMap(new WeakHashMap<ImageView, String>());

	private SpiceManager spiceManager;

	private OnImageDisplayFinishListener onFinishListener;

	public static ImageLoaderSpice getInstance(Context ctx) {

		if (instance == null) {
			instance = new ImageLoaderSpice(ctx);
		}

		return instance;
	}

	private ImageLoaderSpice(Context ctx) {

		this.ctx = ctx;
		fileCache = new FileCache(ctx);
		memoryCache = new MemoryCache();
	}

	public void displayImage(final ImageView imageView, final String fileId, int defaultImage, OnImageDisplayFinishListener onFinishListener) {
		this.onFinishListener = onFinishListener;
		displayImage(imageView, fileId, defaultImage);
	}

	public void displayImage(final ImageView imageView, final String fileId, final int defaultImage) {

		if (TextUtils.isEmpty(fileId) || fileId.contains("default")) {
			if (defaultImage != NO_IMAGE) {
				imageView.setImageResource(defaultImage);

				if (onFinishListener != null) {
					onFinishListener.onFinish();
				}
			}
			
			return;
		}

		imageViews.put(imageView, fileId);

		// Get from memmory cache
		Bitmap bitmap = memoryCache.get(fileId);

		if (bitmap != null) {
			imageView.setImageBitmap(bitmap);

			if (onFinishListener != null) {
				onFinishListener.onFinish();
			}

			return;
		}

		// Get from file cache
		File file = fileCache.getFile(fileId);

		if (file != null && file.exists()) {

			bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

			if (bitmap != null) {

				imageView.setImageBitmap(bitmap);

				if (onFinishListener != null) {
					onFinishListener.onFinish();
				}

				ImageSpiceMemory imageSpiceMemory = new ImageSpiceMemory(fileId, bitmap);
				spiceManager.execute(imageSpiceMemory, null);

				return;
			}
		}

		if (defaultImage != NO_IMAGE) {
			imageView.setImageResource(defaultImage);
		}

		ImageSpiceWeb imageSpice = new ImageSpiceWeb(imageView, fileId);
		spiceManager.execute(imageSpice, new CustomSpiceListener<Bitmap>() {

			@Override
			public void onRequestFailure(SpiceException ex) {
				super.onRequestFailure(ex);

				if (onFinishListener != null) {
					onFinishListener.onFinish();
				}
			}

			@Override
			public void onRequestSuccess(Bitmap bitmap) {

				if (bitmap != null) {
					imageView.setImageBitmap(bitmap);
				}

				if (onFinishListener != null) {
					onFinishListener.onFinish();
				}

				super.onRequestSuccess(bitmap);
			}
		});
	}

	public void setSpiceManager(SpiceManager spiceManager) {
		this.spiceManager = spiceManager;
	}

	private class ImageSpiceWeb extends CustomSpiceRequest<Bitmap> {

		private ImageView imageView;
		private String fileId;

		public ImageSpiceWeb(ImageView imageView, String fileId) {
			super(Bitmap.class);

			this.imageView = imageView;
			this.fileId = fileId;
		}

		@Override
		public Bitmap loadDataFromNetwork() throws Exception {
			
			Logger.i("Downloading image: " + fileId);
			if(imageView.getTag() != null) {
				Logger.custom("w", "LOG", "ID: " + fileId);
				Logger.custom("i", "LOG", "Is image encrypted: " + imageView.getTag().toString());
			}

			// Check if image already downloaded
			if (isImageViewReused(imageView, fileId)) {
				return null;
			}

			// Get from web
			String url = Const.BASE_URL + Const.F_USER_GET_FILE + "?" + Const.FILE_ID + "=" + fileId;

			Request.Builder requestBuilder = new Request.Builder().headers(getGetHeaders()).url(url).get();

			Call connection = getOkHttpClient().newCall(requestBuilder.build());

			Response res = connection.execute();
			final ResponseBody resBody = res.body();

			final File file = fileCache.getFile(fileId);

			if (!file.exists()) {
				file.createNewFile();
			}

			Bitmap bitmap = null;
			
			if (fileId.startsWith("http")) {

				FileOutputStream fos = new FileOutputStream(file);
				fos.write(resBody.bytes());
				fos.flush();
				fos.close();

			} else {
				if(imageView.getTag() != null && !(Boolean)imageView.getTag()){
					FileOutputStream fos = new FileOutputStream(file);
					fos.write(resBody.bytes());
					fos.flush();
					fos.close();
				}else{
                    JNAesCrypto.decryptIsForLoader(resBody.byteStream(), file, ctx);
				}
			}

			bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

			if (bitmap != null) {
				if (bitmap != null) {
					memoryCache.put(fileId, bitmap);
				}
			} else {
				return null;
			}

			// Check if image already downloaded
			if (isImageViewReused(imageView, fileId)) {
				return null;
			}

			return bitmap;
		}
	}

	private class ImageSpiceMemory extends CustomSpiceRequest<Bitmap> {

		private String fileId;
		private Bitmap bitmap;

		public ImageSpiceMemory(String fileId, Bitmap bitmap) {
			super(Bitmap.class);
			this.fileId = fileId;
			this.bitmap = bitmap;
		}

		@Override
		public Bitmap loadDataFromNetwork() throws Exception {

			if (bitmap != null) {
				memoryCache.put(fileId, bitmap);
			}

			return null;
		}
	}

	private boolean isImageViewReused(ImageView imageView, String fileId) {

		String tag = imageViews.get(imageView);
		// Check url is already exist in imageViews MAP
		if (tag == null || !tag.equals(fileId)) {
			return true;
		}

		return false;
	}

}

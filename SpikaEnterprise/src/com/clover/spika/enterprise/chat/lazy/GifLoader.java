package com.clover.spika.enterprise.chat.lazy;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.util.ByteArrayBuffer;
import org.apache.http.util.TextUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.widget.ImageView;

import com.clover.spika.enterprise.chat.extendables.SpikaEnterpriseApp;
import com.clover.spika.enterprise.chat.listeners.OnImageDisplayFinishListener;
import com.clover.spika.enterprise.chat.networking.NetworkManagement;
import com.clover.spika.enterprise.chat.security.JNAesCrypto;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Logger;
import com.clover.spika.enterprise.chat.utils.Utils;

public class GifLoader {

	// singleton usage
	private static GifLoader sInstance;

	public static GifLoader getInstance(Context initActivityContext) {
		if (sInstance == null) {
			Logger.e("ImageLoader has to be initialized first before instance can be used. " + "Call init method before usage.");
			init(initActivityContext);
		}
		return sInstance;
	}

	public static void init(Context initActivityContext) {
		sInstance = new GifLoader(initActivityContext);
	}

	// Initialize MemoryCache
	GifCache gifCache = new GifCache();

	FileCache fileCache;

	// Create Map (collection) to store image and image url in key value pair
	private Map<ImageView, String> imageViews = Collections.synchronizedMap(new WeakHashMap<ImageView, String>());
	ExecutorService executorService;

	// handler to display images in UI thread
	Handler handler = new Handler();

	private int defaultImageId = -1;
	private OnImageDisplayFinishListener mListener;

	public GifLoader(Context context) {

		fileCache = new FileCache(context);

		// Creates a thread pool that reuses a fixed number of
		// threads operating off a shared unbounded queue.
		executorService = Executors.newFixedThreadPool(5);
	}

	public void displayImage(Context ctx, String url, ImageView imageView) {

		displayImage(ctx, url, imageView, null);
	}

	public void displayImage(Context ctx, String url, ImageView imageView, OnImageDisplayFinishListener lis) {

		mListener = lis;

		if (TextUtils.isEmpty(url) || url.equals(Const.DEFAULT_IMAGE_GROUP) || url.equals(Const.DEFAULT_IMAGE_USER)) {
			imageView.setImageResource(defaultImageId);
			if (mListener != null)
				mListener.onFinish();
			return;
		}
		// Store image and url in Map
		imageViews.put(imageView, url);

		// Check image is stored in MemoryCache Map or not (see
		// MemoryCache.java)
		File file = gifCache.get(url);

		if (file != null) {
			// if image is stored in MemoryCache Map then
			// Show image in listview row
//			imageView.setImageBitmap(bitmap); //TODO
			imageView.setTag(file);
			if (mListener != null)
				mListener.onFinish();
		} else {
			// queue Photo to download from url
			queuePhoto(ctx, url, imageView);

			// Before downloading image show default image
			if (defaultImageId != -1) {
				imageView.setImageResource(defaultImageId);
			}
		}
	}

	private void queuePhoto(Context ctx, String url, ImageView imageView) {
		// Store image and url in PhotoToLoad object
		PhotoToLoad p = new PhotoToLoad(url, imageView);

		// pass PhotoToLoad object to PhotosLoader runnable class
		// and submit PhotosLoader runnable to executers to run runnable
		// Submits a PhotosLoader runnable task for execution

		executorService.submit(new PhotosLoader(p, ctx));
	}

	// Task for the queue
	private class PhotoToLoad {
		public String url;
		public ImageView imageView;

		public PhotoToLoad(String url, ImageView imageView) {
			this.url = url;
			this.imageView = imageView;
		}
	}

	private class PhotosLoader implements Runnable {
		PhotoToLoad photoToLoad;
		Context context;

		PhotosLoader(PhotoToLoad photoToLoad, Context context) {
			this.photoToLoad = photoToLoad;
			this.context = context;
		}

		@Override
		public void run() {
			try {

				// Check if image already downloaded
				if (imageViewReused(photoToLoad))
					return;

				// download image from web url
				File file = getFile(context, photoToLoad.url);

				// set image data in Memory Cache
				gifCache.put(photoToLoad.url, file);

				if (imageViewReused(photoToLoad))
					return;

				// Get bitmap to display
				BitmapDisplayer bd = new BitmapDisplayer(file, photoToLoad);

				// Causes the Runnable bd (BitmapDisplayer) to be added to the
				// message queue.
				// The runnable will be run on the thread to which this handler
				// is attached.
				// BitmapDisplayer run method will call
				handler.post(bd);

			} catch (Throwable th) {
				th.printStackTrace();
			}
		}
	}

	/**
	 * This should be used in a seperate thread
	 * 
	 * @param context
	 * @param url
	 * @return
	 */
	public File getFile(Context context, String url) {

		File file = fileCache.getFile(url);

		// start: Get image from cache
		try {

			File localFile;
			localFile = file;
			
			if (localFile != null) {
				return localFile;
			}

		} catch (Exception e) {
			if (Const.DEBUG_CRYPTO)
				e.printStackTrace();
		}
		// end: Get image from cache

		// start: Download image
		try {
			File fileNew = null;

			HashMap<String, String> getParams = new HashMap<String, String>();
			getParams.put(Const.FILE_ID, url);

			InputStream is = NetworkManagement.httpGetGetFile(SpikaEnterpriseApp.getSharedPreferences(context), Const.F_USER_GET_FILE, getParams).getContent();

			BufferedInputStream bis = new BufferedInputStream(is);

			ByteArrayBuffer baf = new ByteArrayBuffer(20000);
			int current = 0;
			while ((current = bis.read()) != -1) {
				baf.append((byte) current);
			}

			/* Convert the Bytes read to a String. */
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(baf.toByteArray());
			fos.flush();
			fos.close();
			is.close();

			return fileNew;

		} catch (Throwable ex) {
			if (Const.DEBUG_CRYPTO)
				ex.printStackTrace();
			if (ex instanceof OutOfMemoryError) {
				clearCache();
			}
		}
		// end: Download image

		return null;
	}

	boolean imageViewReused(PhotoToLoad photoToLoad) {

		String tag = imageViews.get(photoToLoad.imageView);
		// Check url is already exist in imageViews MAP
		if (tag == null || !tag.equals(photoToLoad.url))
			return true;
		return false;
	}

	// Used to display bitmap in the UI thread
	private class BitmapDisplayer implements Runnable {
		File file;
		PhotoToLoad photoToLoad;

		public BitmapDisplayer(File file, PhotoToLoad photoLoad) {
			this.file = file;
			this.photoToLoad = photoLoad;
		}

		public void run() {

			// Show bitmap on UI
			if (file != null) {
//				photoToLoad.imageView.setImageBitmap(bitmap); //TODO
				photoToLoad.imageView.setTag(file);
				if (mListener != null)
					mListener.onFinish();
			}
		}
	}

	public void clearCache() {
		// Clear cache directory downloaded images and stored data in maps
		gifCache.clear();
		fileCache.clear();
	}

	public void setDefaultImage(int id) {
		this.defaultImageId = id;
	}

}

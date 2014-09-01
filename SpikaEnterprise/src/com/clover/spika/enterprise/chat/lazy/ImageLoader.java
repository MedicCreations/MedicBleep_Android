package com.clover.spika.enterprise.chat.lazy;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.widget.ImageView;

import com.clover.spika.enterprise.chat.extendables.SpikaEnterpriseApp;
import com.clover.spika.enterprise.chat.networking.NetworkManagement;
import com.clover.spika.enterprise.chat.security.JNAesCrypto;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Logger;
import com.clover.spika.enterprise.chat.utils.Utils;

import org.apache.http.util.ByteArrayBuffer;

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

public class ImageLoader {

	// singleton usage
	private static ImageLoader sInstance;

    public static ImageLoader getInstance() {
        if (sInstance == null) {
            Logger.e("ImageLoader has to be initialized first before instance can be used. " +
                    "Call init method before usage.");
        }
        return sInstance;
    }

	public static void init(Context initActivityContext) {
		sInstance = new ImageLoader(initActivityContext);
	}

	// Initialize MemoryCache
	MemoryCache memoryCache = new MemoryCache();

	FileCache fileCache;

	// Create Map (collection) to store image and image url in key value pair
	private Map<ImageView, String> imageViews = Collections.synchronizedMap(new WeakHashMap<ImageView, String>());
	ExecutorService executorService;

	// handler to display images in UI thread
	Handler handler = new Handler();

	private int defaultImageId = -1;

	private ImageLoader(Context context) {

		fileCache = new FileCache(context);

		// Creates a thread pool that reuses a fixed number of
		// threads operating off a shared unbounded queue.
		executorService = Executors.newFixedThreadPool(5);
	}

	public void displayImage(Context ctx, String url, ImageView imageView) {

		// Store image and url in Map
		imageViews.put(imageView, url);

		// Check image is stored in MemoryCache Map or not (see
		// MemoryCache.java)
		Bitmap bitmap = memoryCache.get(url);

		if (bitmap != null) {
			// if image is stored in MemoryCache Map then
			// Show image in listview row
			imageView.setImageBitmap(bitmap);
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
				Bitmap bmp = getBitmap(context, photoToLoad.url);

				// set image data in Memory Cache
				memoryCache.put(photoToLoad.url, bmp);

				if (imageViewReused(photoToLoad))
					return;

				// Get bitmap to display
				BitmapDisplayer bd = new BitmapDisplayer(bmp, photoToLoad);

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
	public Bitmap getBitmap(Context context, String url) {

		File file = fileCache.getFile(url);

		// start: Get image from cache
		try {

			String fileStr1 = Utils.getStringFromFile(file.getAbsolutePath());
			Bitmap localBitmap = JNAesCrypto.decryptBitmapJN(fileStr1, file.getAbsolutePath());

			if (localBitmap != null) {
				return localBitmap;
			}

		} catch (Exception e) {
			if(Const.DEBUG_CRYPTO) e.printStackTrace();
		}
		// end: Get image from cache

		// start: Download image
		try {
			Bitmap bitmap = null;

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

			String fileStr = Utils.getStringFromFile(file.getAbsolutePath());
			bitmap = JNAesCrypto.decryptBitmapJN(fileStr, file.getAbsolutePath());

			return bitmap;

		} catch (Throwable ex) {
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
		Bitmap bitmap;
		PhotoToLoad photoToLoad;

		public BitmapDisplayer(Bitmap bitmap, PhotoToLoad photoLoad) {
			this.bitmap = bitmap;
			this.photoToLoad = photoLoad;
		}

		public void run() {

			// Show bitmap on UI
			if (bitmap != null) {
				photoToLoad.imageView.setImageBitmap(bitmap);
			}
		}
	}

	public void clearCache() {
		// Clear cache directory downloaded images and stored data in maps
		memoryCache.clear();
		fileCache.clear();
	}

	public void setDefaultImage(int id) {
		this.defaultImageId = id;
	}

}

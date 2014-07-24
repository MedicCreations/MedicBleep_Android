package com.clover.spika.enterprise.chat.lazy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.widget.ImageView;

import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.extendables.BaseAsyncTask;
import com.clover.spika.enterprise.chat.networking.NetworkManagement;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Helper;
import com.clover.spika.enterprise.chat.views.RoundedCornersImage;

public class ImageLoader {

	// Initialize MemoryCache
	MemoryCache memoryCache = new MemoryCache();

	FileCache fileCache;

	// Create Map (collection) to store image and image url in key value pair
	private Map<ImageView, String> imageViews = Collections.synchronizedMap(new WeakHashMap<ImageView, String>());
	ExecutorService executorService;

	// handler to display images in UI thread
	Handler handler = new Handler();

	int radius = 0;
	int id = -1;

	public ImageLoader(Context context) {

		fileCache = new FileCache(context);

		// Creates a thread pool that reuses a fixed number of
		// threads operating off a shared unbounded queue.
		executorService = Executors.newFixedThreadPool(5);

		Bitmap bitmapBorder = BitmapFactory.decodeResource(context.getResources(), R.drawable.circle);
		radius = bitmapBorder.getWidth();
		bitmapBorder = null;
	}

	public void setDefaultImage(int id) {
		this.id = id;
	}

	public void displayImage(Context ctx, String url, ImageView imageView, boolean isRoundedLive) {
		// Store image and url in Map
		if (id != -1)
			imageView.setImageResource(id);
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
			queuePhoto(ctx, url, imageView, isRoundedLive);
		}
	}

	private void queuePhoto(Context ctx, String url, ImageView imageView, boolean isRoundedLive) {
		// Store image and url in PhotoToLoad object
		PhotoToLoad p = new PhotoToLoad(url, imageView, isRoundedLive);

		// pass PhotoToLoad object to PhotosLoader runnable class
		// and submit PhotosLoader runnable to executers to run runnable
		// Submits a PhotosLoader runnable task for execution

		executorService.submit(new PhotosLoader(p, ctx));
	}

	// Task for the queue
	private class PhotoToLoad {
		public String url;
		public ImageView imageView;
		public boolean isRoundedLive;

		public PhotoToLoad(String u, ImageView i, boolean r) {
			url = u;
			imageView = i;
			isRoundedLive = r;
		}
	}

	class PhotosLoader implements Runnable {
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
				Bitmap bmp = getBitmap(context, photoToLoad.url, photoToLoad.isRoundedLive);

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

	private Bitmap getBitmap(Context context, String url, boolean isRoundedLive) {
		File f = fileCache.getFile(url);

		// from SD cache
		// CHECK : if trying to decode file which not exist in cache return null
		Bitmap b = decodeFile(f);
		if (b != null) {
			if (isRoundedLive) {
				return Helper.getCroppedBitmap(b, null, radius);
			} else {
				return b;
			}
		}
		// Download image file from web
		try {

			Bitmap bitmap = null;

			HashMap<String, String> getParams = new HashMap<String, String>();
			getParams.put(Const.FILE_ID, url);

			InputStream is = NetworkManagement.httpGetGetFile(Const.F_USER_GET_FILE, getParams).getContent();

			// TODO this needs to be fixed
			// try {
			// InputStream isCheck = is;
			//
			// String resString = NetworkManagement.getString(isCheck);
			//
			// Logger.info("Image not downloaded : " + resString);
			//
			// JSONObject res = Helper.jObjectFromString(resString);
			//
			// isCheck = null;
			//
			// int code = res.getInt(Const.CODE);
			//
			// if (code != Const.E_SUCCESS) {
			// Logger.info("Image not downloaded : " + res.toString(2));
			// } else {
			// Logger.info("Image not downloaded : " + res.toString(2));
			// }
			// } catch (Exception ex) {
			// ex.printStackTrace();
			// // do nothing - image downloaded
			// }

			// Constructs a new FileOutputStream that writes to file
			// if file not exist then it will create file
			OutputStream os = new FileOutputStream(f);

			// See Utils class CopyStream method
			// It will each pixel from input stream and
			// write pixels to output stream (file)
			Helper.copyStream(is, os);

			is.close();
			os.close();
			// conn.disconnect();

			// Now file created and going to resize file with defined height
			// Decodes image and scales it to reduce memory consumption
			bitmap = decodeFile(f);

			if (isRoundedLive) {
				return Helper.getCroppedBitmap(bitmap, null, radius);
			} else {
				return bitmap;
			}
		} catch (Throwable ex) {
			ex.printStackTrace();
			if (ex instanceof OutOfMemoryError)
				memoryCache.clear();
			return null;
		}
	}

	public void getBitmapAsync(Context context, final String url, final ImageView imageView) {
		new BaseAsyncTask<Void, Void, Bitmap>(context, false) {

			File file = fileCache.getFile(url);;

			protected Bitmap doInBackground(Void... params) {

				// Download image file from web
				try {

					// from cache
					Bitmap localBitmap = decodeFile(file);
					if (localBitmap != null) {
						return localBitmap;
					}

					Bitmap bitmap = null;

					HashMap<String, String> getParams = new HashMap<String, String>();
					getParams.put(Const.FILE_ID, url);

					InputStream is = NetworkManagement.httpGetGetFile(Const.F_USER_GET_FILE, getParams).getContent();
					OutputStream os = new FileOutputStream(file);

					Helper.copyStream(is, os);

					is.close();
					os.close();

					bitmap = decodeFile(file);

					return bitmap;

				} catch (Throwable ex) {
					ex.printStackTrace();
					if (ex instanceof OutOfMemoryError) {
						memoryCache.clear();
					}
				}

				return null;
			};

			protected void onPostExecute(Bitmap result) {
				if (result != null) {
					RoundedCornersImage drawable = new RoundedCornersImage(result);
					imageView.setImageDrawable(drawable);
				}
			};
		}.execute();
	}

	// Decodes image and scales it to reduce memory consumption
	private Bitmap decodeFile(File f) {

		try {
			BitmapFactory.Options o2 = new BitmapFactory.Options();

			FileInputStream stream2 = new FileInputStream(f);
			Bitmap bitmap = BitmapFactory.decodeStream(stream2, null, o2);
			stream2.close();

			return bitmap;

		} catch (FileNotFoundException e) {
		} catch (IOException e) {
			e.printStackTrace();
		}
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
	class BitmapDisplayer implements Runnable {
		Bitmap bitmap;
		PhotoToLoad photoToLoad;

		public BitmapDisplayer(Bitmap b, PhotoToLoad p) {
			bitmap = b;
			photoToLoad = p;
		}

		public void run() {
			if (imageViewReused(photoToLoad))
				return;

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

}

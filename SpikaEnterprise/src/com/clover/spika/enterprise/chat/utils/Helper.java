package com.clover.spika.enterprise.chat.utils;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.annotation.DrawableRes;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;

import com.clover.spika.enterprise.chat.LoginActivity;
import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.extendables.SpikaEnterpriseApp;
import com.clover.spika.enterprise.chat.listeners.ProgressBarListeners;

public class Helper {

	public static String getTime(String createdString) {

		long created = Long.parseLong(createdString) * 1000;

		Date date = new Date(created);
		SimpleDateFormat dateFormat1 = new SimpleDateFormat("HH:mm", Locale.US);

		String time = dateFormat1.format(date);

		SimpleDateFormat dateFormat = new SimpleDateFormat("MM.dd - E", Locale.US);

		String rez = dateFormat.format(date);

		return rez + " " + time;
	}

	public static Bitmap scaleBitmap(Bitmap bm, float scalingFactor) {
		int scaleHeight = (int) (bm.getHeight() * scalingFactor);
		int scaleWidth = (int) (bm.getWidth() * scalingFactor);

		return Bitmap.createScaledBitmap(bm, scaleWidth, scaleHeight, true);
	}

	/**
	 * Get image path
	 * 
	 * @param cntx
	 * @param uri
	 * @return
	 */
	public static String getImagePath(Context cntx, Uri uri, boolean isOverJellyBeam) {
		
		if(isOverJellyBeam){
			try {
				ParcelFileDescriptor parcelFileDescriptor = cntx.getContentResolver().openFileDescriptor(uri, "r");
			    FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
//			    Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
			    copyStream(new FileInputStream(fileDescriptor), 
			    		new FileOutputStream(new File(cntx.getExternalCacheDir() + "/" + "image_profile")));
			    parcelFileDescriptor.close();
//			    saveBitmapToFile(image, cntx.getExternalCacheDir() + "/" + "image_profile");
			    return cntx.getExternalCacheDir() + "/" + "image_profile";
			} catch (Exception e) {
				e.printStackTrace();
				return "";
			}
			
		}else{

			String[] projection = { MediaStore.Images.Media.DATA };
			Cursor cursor = cntx.getContentResolver().query(uri, projection, null, null, null);
	
			int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
	
			return cursor.getString(column_index);
		}
	}

	/**
	 * Get and set current app version
	 */
	public static boolean isUpdated(Context cntx) {

		try {
			PackageInfo packageInfo = cntx.getPackageManager().getPackageInfo(cntx.getPackageName(), 0);

			int currentVersion = packageInfo.versionCode;

			if (SpikaEnterpriseApp.getSharedPreferences(cntx).getCustomInt(Const.CURRENT_APP_VERSION) == -1) {
				SpikaEnterpriseApp.getSharedPreferences(cntx).setCustomInt(Const.CURRENT_APP_VERSION, currentVersion);

				return false;
			} else if (SpikaEnterpriseApp.getSharedPreferences(cntx).getCustomInt(Const.CURRENT_APP_VERSION) < currentVersion) {
				return true;
			}
		} catch (NameNotFoundException e) {
			Logger.info("Could not get package name for app version checkup.");
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * Update app version
	 * 
	 * @param cntx
	 */
	public static void updateAppVersion(Context cntx) {
		try {
			PackageInfo packageInfo = cntx.getPackageManager().getPackageInfo(cntx.getPackageName(), 0);
			int currentVersion = packageInfo.versionCode;
			SpikaEnterpriseApp.getSharedPreferences(cntx).setCustomInt(Const.CURRENT_APP_VERSION, currentVersion);
		} catch (NameNotFoundException e) {
			Logger.info("Could not get package name for app version checkup.");
			e.printStackTrace();
		}
	}

	/**
	 * Check the type of the exception
	 * 
	 * @param receivedError
	 * @param constError
	 * @return
	 */
	public static boolean checkException(String receivedError, int constError) {

		int error = Integer.valueOf(receivedError);

		if (error != constError) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Return JSON object from string
	 * 
	 * @param result
	 * @return
	 */
	public static JSONObject jObjectFromString(String string) {
		try {
			JSONObject result = new JSONObject(string);
			Logger.custom("RawResponse", result.toString(2));

			try {
				if (result != null) {
					@SuppressWarnings("unused")
					int code = result.getInt(Const.CODE);
					// This was used when your account has been blocked, it can
					// be refactored to show something else depending on the
					// returnning code
					// if (code == Const.E_YOUR_ACC_IS_BLOCKED) {
					//
					// Intent intent = new Intent(BaseActivity.getInstance(),
					// AccountBlockedActivity.class);
					// intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					// BaseActivity.getInstance().startActivity(intent);
					//
					// return null;
					// }
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}

			return result;
		} catch (Exception e) {
			Logger.custom("RawResponse", string);
			return null;
		}
	}

	public static JSONObject jObjectRawFromString(String string) {
		try {
			JSONObject result = new JSONObject(string);
			Logger.custom("RawResponse", result.toString(2));

			return result;
		} catch (Exception e) {
			Logger.custom("RawResponse", string);
			return null;
		}
	}

	/**
	 * Set user settings
	 */
	public static void setUserProperties(Context ctx, String userId, String userImageName, String firstName, String lastName) {
		Preferences pref = SpikaEnterpriseApp.getSharedPreferences(ctx);
		pref.setCustomString(Const.USER_ID, userId);
		pref.setCustomString(Const.USER_IMAGE_NAME, userImageName);
		pref.setCustomString(Const.FIRSTNAME, firstName);
		pref.setCustomString(Const.LASTNAME, lastName);
	}

	public static void setUserImage(Context ctx, String image) {
		SpikaEnterpriseApp.getSharedPreferences(ctx).setCustomString(Const.USER_IMAGE_NAME, image);
	}

	public static String getUserFirstName(Context ctx) {
		return SpikaEnterpriseApp.getSharedPreferences(ctx).getCustomString(Const.FIRSTNAME);
	}

	public static String getUserLastName(Context ctx) {
		return SpikaEnterpriseApp.getSharedPreferences(ctx).getCustomString(Const.LASTNAME);
	}

	public static String getUserImage(Context ctx) {
		return SpikaEnterpriseApp.getSharedPreferences(ctx).getCustomString(Const.USER_IMAGE_NAME);
	}

	public static String getUserId(Context ctx) {
		return SpikaEnterpriseApp.getSharedPreferences(ctx).getCustomString(Const.USER_ID);
	}

	/**
	 * Return JSON array from string
	 * 
	 * @param result
	 * @return
	 */
	public static JSONArray jArrayFromString(String string) {

		try {
			return new JSONArray(string);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * get rounded bitmap
	 * 
	 * @param bmp
	 * @param path
	 * @param radius
	 * @return
	 */
	public static Bitmap getCroppedBitmap(Bitmap bmp, String path, int radius) {

		if (path != null) {
			bmp = BitmapFactory.decodeFile(path);

			if (bmp == null) {
				return null;
			}

			return getRoundedBitmap(bmp, radius);
		} else {
			if (bmp == null) {
				return null;
			}

			return getRoundedBitmap(bmp, radius);
		}
	}

	/**
	 * Set pagging animation
	 */
	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	public static void startPaggingAnimation(final Context cntx, final ImageView loading, final boolean isJellyBean) {
		AnimationDrawable animation = new AnimationDrawable();
		animation.addFrame(cntx.getResources().getDrawable(R.drawable.process_frame1), 500);
		animation.addFrame(cntx.getResources().getDrawable(R.drawable.process_frame2), 500);
		animation.addFrame(cntx.getResources().getDrawable(R.drawable.process_frame3), 500);
		animation.addFrame(cntx.getResources().getDrawable(R.drawable.process_frame4), 500);
		animation.setOneShot(false);

		if (isJellyBean) {
			loading.setBackgroundDrawable(animation);
		} else {
			loading.setBackground(animation);
		}

		animation.start();
	}

	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	public static void setViewBackgroundDrawable(View view, Drawable drawable) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			view.setBackground(drawable);
		} else {
			view.setBackgroundDrawable(drawable);
		}
	}

	public static void setViewBackgroundResource(View view, @DrawableRes int drawableId) {
		Drawable drawable = view.getResources().getDrawable(drawableId);
		setViewBackgroundDrawable(view, drawable);
	}

	/**
	 * method is used for checking valid email id format.
	 * 
	 * @param email
	 * @return boolean true for valid false for invalid
	 */
	public static boolean isEmailValid(String email) {
		boolean isValid = false;

		String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
		CharSequence inputStr = email;

		Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(inputStr);
		if (matcher.matches()) {
			isValid = true;
		}
		return isValid;
	}

	/**
	 * Copy input stream to output stream
	 * 
	 * @param is
	 * @param os
	 */
	public static void copyStream(InputStream is, OutputStream os) {
		copyStream(is, os, -1, null);
	}

	/**
	 * Copy input stream to output stream
	 * 
	 * @param is
	 * @param os
	 * @param length
	 *            of content
	 */
	public static void copyStream(InputStream is, OutputStream os, long length, ProgressBarListeners listener) {
		final int buffer_size = 1024;
		int totalLen = 0;
		try {

			byte[] bytes = new byte[buffer_size];
			while (true) {
				// Read byte from input stream

				int count = is.read(bytes, 0, buffer_size);
				if (count == -1) {
					listener.onFinish();
					break;
				}

				// Write byte from output stream
				if (length != -1 && listener != null) {
					totalLen = totalLen + count;
					listener.onSetMax(length);
					listener.onProgress(totalLen);
				}
				os.write(bytes, 0, count);
			}
		} catch (Exception ex) {
		}
	}

	/**
	 * Get error description
	 */
	public static String errorDescriptions(Context cntx, Integer code) {

		SparseArray<String> errors = new SparseArray<String>();

		errors.put(Const.E_INVALID_TOKEN, cntx.getResources().getString(R.string.e_invalid_token));
		errors.put(Const.E_EXPIRED_TOKEN, cntx.getResources().getString(R.string.e_expired_token));
		errors.put(Const.E_SOMETHING_WENT_WRONG, cntx.getResources().getString(R.string.e_something_went_wrong));

		for (int i = 0; i < errors.size(); i++) {
			if (code == errors.keyAt(i)) {
				return errors.valueAt(i);
			}
		}

		return "";
	}

	/**
	 * get aplication directory
	 * 
	 * @return
	 */
	public static File getAppPath() {
		File root = android.os.Environment.getExternalStorageDirectory();
		File dir = new File(root.getAbsolutePath() + "/" + Const.APP_FILES_DIRECTORY);
		if (dir.exists() == false) {
			dir.mkdirs();
		}
		return dir;
	}

	public static Bitmap BitmapFromFile(File f) {

		Bitmap mBitmap = null;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		options.inSampleSize = calculateInSampleSize(options, 640, 640);
		options.inJustDecodeBounds = false;
		mBitmap = BitmapFactory.decodeFile(f.getPath(), options);
		mBitmap = Bitmap.createScaledBitmap(mBitmap, 640, 640, true);

		return mBitmap;
	}

	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			// Calculate ratios of height and width to requested height and
			// width
			final int heightRatio = Math.round((float) height / (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);

			// Choose the smallest ratio as inSampleSize value, this will
			// guarantee
			// a final image with both dimensions larger than or equal to the
			// requested height and width.
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}

		return inSampleSize;
	}

	public static Bitmap getRoundedBitmap(Bitmap bmp, int radius) {

		Bitmap sbmp;
		int smaller = bmp.getWidth() < bmp.getHeight() ? bmp.getWidth() : bmp.getHeight();

		if (bmp.getWidth() != radius || bmp.getHeight() != radius) {
			double coefficient = (double) radius / (double) smaller;
			sbmp = Bitmap.createScaledBitmap(bmp, (int) (bmp.getWidth() * coefficient), (int) (bmp.getHeight() * coefficient), false);
		} else {
			sbmp = bmp;
		}

		Bitmap output = Bitmap.createBitmap(sbmp.getWidth(), sbmp.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, radius, radius);

		paint.setAntiAlias(true);
		paint.setFilterBitmap(true);
		paint.setDither(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(Color.parseColor("#FFFFFF"));
		canvas.drawCircle(radius / 2, radius / 2, radius / 2, paint);
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(sbmp, rect, rect, paint);

		sbmp = null;
		bmp = null;

		return output;
	}

	public static int dpToPx(Context ctx, int dp) {

		DisplayMetrics metrics = new DisplayMetrics();
		((Activity) ctx).getWindowManager().getDefaultDisplay().getMetrics(metrics);

		float logicalDensity = metrics.density;

		return (int) Math.ceil(dp * logicalDensity);
	}
	
	public static void logout(Context ac){
		Intent logoutIntent = new Intent(ac, LoginActivity.class);
		logoutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
		ac.startActivity(logoutIntent);
		((Activity) ac).finish();
	}

}

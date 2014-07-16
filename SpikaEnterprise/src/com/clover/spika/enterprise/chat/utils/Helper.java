package com.clover.spika.enterprise.chat.utils;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

import com.clover.spika.enterprise.chat.extendables.BaseActivity;

import com.clover.spika.enterprise.chat.R;
import android.content.Context;
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
import android.net.Uri;
import android.provider.MediaStore;
import android.util.SparseArray;
import android.widget.ImageView;

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
	public static String getImagePath(Context cntx, Uri uri) {

		String[] projection = { MediaStore.Images.Media.DATA };
		Cursor cursor = cntx.getContentResolver().query(uri, projection, null, null, null);

		int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();

		return cursor.getString(column_index);
	}

	/**
	 * Get and set current app version
	 */
	public static boolean isUpdated(Context cntx) {

		try {
			PackageInfo packageInfo = cntx.getPackageManager().getPackageInfo(cntx.getPackageName(), 0);

			int currentVersion = packageInfo.versionCode;

			if (BaseActivity.getPreferences().getCustomInt(Const.CURRENT_APP_VERSION) == -1) {
				BaseActivity.getPreferences().setCustomInt(Const.CURRENT_APP_VERSION, currentVersion);

				return false;
			} else if (BaseActivity.getPreferences().getCustomInt(Const.CURRENT_APP_VERSION) < currentVersion) {
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
			BaseActivity.getPreferences().setCustomInt(Const.CURRENT_APP_VERSION, currentVersion);
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

	/**
	 * Set user settings
	 */
	public static void setUserProperties(String userId, String userImageName, String userNickname) {
		BaseActivity.getPreferences().setCustomString(Const.USER_ID, userId);
		BaseActivity.getPreferences().setCustomString(Const.USER_IMAGE_NAME, userImageName);
		BaseActivity.getPreferences().setCustomString(Const.USER_NICKNAME, userNickname);
	}

	/**
	 * Clear user properties
	 */
	public static void clearUserProperties() {
		BaseActivity.getPreferences().setCustomString(Const.USER_ID, "");
		BaseActivity.getPreferences().setCustomString(Const.USER_IMAGE_NAME, "");
		BaseActivity.getPreferences().setCustomString(Const.USER_NICKNAME, "");
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
	public static void startPaggingAnimation(final Context cntx, final ImageView loading) {
		AnimationDrawable animation = new AnimationDrawable();
		animation.addFrame(cntx.getResources().getDrawable(R.drawable.gb_process_frame1), 500);
		animation.addFrame(cntx.getResources().getDrawable(R.drawable.gb_process_frame2), 500);
		animation.addFrame(cntx.getResources().getDrawable(R.drawable.gb_process_frame3), 500);
		animation.addFrame(cntx.getResources().getDrawable(R.drawable.gb_process_frame4), 500);
		animation.setOneShot(false);

		loading.setBackgroundDrawable(animation);

		animation.start();
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
		final int buffer_size = 1024;
		try {

			byte[] bytes = new byte[buffer_size];
			while (true) {
				// Read byte from input stream

				int count = is.read(bytes, 0, buffer_size);
				if (count == -1)
					break;

				// Write byte from output stream
				os.write(bytes, 0, count);
			}
		} catch (Exception ex) {
		}
	}

	/**
	 * Substring adapter
	 */
	public static String substringText(String text, int size) {
		try {
			if (text.length() < size) {
				return text;
			} else {
				return text.substring(0, size) + "...";
			}
		} catch (Exception e) {
			return "";
		}
	}

	/**
	 * Get error description
	 */
	public static String errorDescriptions(Context cntx, Integer code) {

		SparseArray<String> errors = new SparseArray<String>();

		errors.put(Const.E_INVALID_TOKEN, cntx.getResources().getString(R.string.e_invalid_token));
		errors.put(Const.E_EXPIRED_TOKEN, cntx.getResources().getString(R.string.e_expired_token));
		errors.put(Const.E_MESSAGE_ALLREADY_REPORTED, cntx.getResources().getString(R.string.e_allready_reported));
		errors.put(Const.E_SOMETHING_WENT_WRONG, cntx.getResources().getString(R.string.e_something_went_wrong));
		errors.put(Const.E_YOUR_ACC_IS_BLOCKED, cntx.getResources().getString(R.string.e_your_acc_has_been_blocked));
		errors.put(Const.E_GROUP_DELETED, cntx.getResources().getString(R.string.e_group_has_been_deleted));
		errors.put(Const.E_NG_WORD_DETECTED, cntx.getResources().getString(R.string.e_ng_word_detected));

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

		if (bmp.getWidth() != radius || bmp.getHeight() != radius) {
			sbmp = Bitmap.createScaledBitmap(bmp, radius, radius, false);
		} else {
			sbmp = bmp;
		}

		Bitmap output = Bitmap.createBitmap(sbmp.getWidth(), sbmp.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, sbmp.getWidth(), sbmp.getHeight());

		paint.setAntiAlias(true);
		paint.setFilterBitmap(true);
		paint.setDither(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(Color.parseColor("#FFFFFF"));
		canvas.drawCircle(sbmp.getWidth() / 2, sbmp.getHeight() / 2, sbmp.getWidth() / 2, paint);
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(sbmp, rect, rect, paint);

		sbmp = null;
		bmp = null;

		return output;
	}

}
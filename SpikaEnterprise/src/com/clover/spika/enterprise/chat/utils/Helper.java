package com.clover.spika.enterprise.chat.utils;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
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
import android.provider.MediaStore.MediaColumns;
import android.support.annotation.DrawableRes;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;

import com.clover.spika.enterprise.chat.LoginActivity;
import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.extendables.SpikaEnterpriseApp;
import com.clover.spika.enterprise.chat.listeners.ProgressBarListeners;
import com.clover.spika.enterprise.chat.models.User;

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

		if (isOverJellyBeam) {
			try {
				ParcelFileDescriptor parcelFileDescriptor = cntx.getContentResolver().openFileDescriptor(uri, "r");
				FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
				// Bitmap image =
				// BitmapFactory.decodeFileDescriptor(fileDescriptor);
				copyStream(new FileInputStream(fileDescriptor), new FileOutputStream(new File(cntx.getExternalCacheDir() + "/" + "image_profile")));
				parcelFileDescriptor.close();
				// saveBitmapToFile(image, cntx.getExternalCacheDir() + "/" +
				// "image_profile");
				return cntx.getExternalCacheDir() + "/" + "image_profile";
			} catch (Exception e) {
				e.printStackTrace();
				return "";
			}

		} else {

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
			Logger.i("Could not get package name for app version checkup.");
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * Get current App version
	 */
	public static String getAppVersion() {
		Context cntx = SpikaEnterpriseApp.getAppContext();
		try {
			PackageInfo packageInfo = cntx.getPackageManager().getPackageInfo(cntx.getPackageName(), 0);
			String currentVersion = packageInfo.versionName;
			return currentVersion;
		} catch (NameNotFoundException e) {
			Logger.i("Could not get package name for app version checkup.");
			e.printStackTrace();
			return null;
		}

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
			Logger.i("Could not get package name for app version checkup.");
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
					// This can be used to intercept the whole api response
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
	public static void setUserProperties(Context ctx, String userId, String userImageName, String userThumbImage, String firstName, String lastName, String token) {
		Preferences pref = SpikaEnterpriseApp.getSharedPreferences(ctx);
		pref.setCustomString(Const.USER_ID, userId);
		pref.setCustomString(Const.USER_IMAGE_NAME, userImageName);
		pref.setCustomString(Const.USER_THUMB_IMAGE_NAME, userThumbImage);
		pref.setCustomString(Const.FIRSTNAME, firstName);
		pref.setCustomString(Const.LASTNAME, lastName);
		pref.setCustomString(Const.TOKEN, token);
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
	
	public static String getUserThumbImage(Context ctx) {
		return SpikaEnterpriseApp.getSharedPreferences(ctx).getCustomString(Const.USER_THUMB_IMAGE_NAME);
	}

	public static String getUserId(Context ctx) {
		return SpikaEnterpriseApp.getSharedPreferences(ctx).getCustomString(Const.USER_ID);
	}

	public static void setRoomFileId(Context ctx, String fileId) {
		SpikaEnterpriseApp.getSharedPreferences(ctx).setCustomString(Const.ROOM_FILE_ID, fileId);
	}

	public static String getRoomFileId(Context ctx) {
		return SpikaEnterpriseApp.getSharedPreferences(ctx).getCustomString(Const.ROOM_FILE_ID);
	}

	public static void setRoomThumbId(Context ctx, String thumbId) {
		SpikaEnterpriseApp.getSharedPreferences(ctx).setCustomString(Const.ROOM_THUMB_ID, thumbId);
	}

	public static String getRoomThumbId(Context ctx) {
		return SpikaEnterpriseApp.getSharedPreferences(ctx).getCustomString(Const.ROOM_THUMB_ID);
	}
	
	public static User getUser(Context ctx){
		try {
			return new User(Integer.parseInt(getUserId(ctx)), getUserFirstName(ctx), getUserLastName(ctx), null, getUserImage(ctx), getUserThumbImage(ctx), false, null, false, null);
		} catch (Exception e) {
			return new User(-1, "", "", "", "", "", false, null, false, null);
		}
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
	 * Set pagging animation
	 */
	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	public static void startPaggingAnimation(final Context cntx, final ImageView loading) {
		AnimationDrawable animation = new AnimationDrawable();
		animation.addFrame(cntx.getResources().getDrawable(R.drawable.process_frame1), 500);
		animation.addFrame(cntx.getResources().getDrawable(R.drawable.process_frame2), 500);
		animation.addFrame(cntx.getResources().getDrawable(R.drawable.process_frame3), 500);
		animation.addFrame(cntx.getResources().getDrawable(R.drawable.process_frame4), 500);
		animation.setOneShot(false);

		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
			loading.setBackground(animation);
		} else {
			loading.setBackgroundDrawable(animation);
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

		errors.put(Const.E_INVALID_TOKEN, cntx.getString(R.string.e_invalid_token));
		errors.put(Const.E_EXPIRED_TOKEN, cntx.getString(R.string.e_expired_token));
		errors.put(Const.E_CHAT_INACTIVE, cntx.getString(R.string.e_chat_inactive));
		errors.put(Const.E_NOT_CHAT_MEMBER, cntx.getString(R.string.e_not_chat_member));
		errors.put(Const.E_EMAIL_MISSING, cntx.getString(R.string.e_email_missing));
		errors.put(Const.E_USERNAME_NOT_EXIST, cntx.getString(R.string.e_username_not_exist));
		errors.put(Const.E_SOMETHING_WENT_WRONG, cntx.getString(R.string.e_something_went_wrong));
		errors.put(Const.E_CHAT_DELETED, cntx.getString(R.string.chat_has_been_deleted_));
		errors.put(Const.E_PAGE_NOT_FOUND, cntx.getString(R.string.page_has_not_been_found_));
		errors.put(Const.E_NOT_GROUP_ADMIN, cntx.getString(R.string.you_are_not_a_group_admin_));
		errors.put(Const.E_TEMP_PASSWORD_NOT_VALID, cntx.getString(R.string.temp_password_is_not_valid_));
		errors.put(Const.E_LOGIN_WITH_TEMP_PASS, cntx.getString(R.string.login_with_temp_password_));
		errors.put(Const.E_FAILED, cntx.getString(R.string.e_something_went_wrong));
		errors.put(Const.E_DIR_NOT_WRITABLE, cntx.getString(R.string.dir_not_writable_on_the_server_));
		errors.put(Const.E_INVALID_LOGIN, cntx.getString(R.string.invalid_login_));
		errors.put(Const.E_NO_CHILD_MSGS, cntx.getString(R.string.no_child_messages_));
		errors.put(Const.E_INVALID_TEMP_PASSWORD, cntx.getString(R.string.invalid_temp_password_));
		errors.put(Const.E_PASSWORD_EXIST, cntx.getString(R.string.password_already_in_use_));

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

	public static void logout(Context ctx) {
		SpikaEnterpriseApp.getSharedPreferences(ctx).clear();
		Intent logoutIntent = new Intent(ctx, LoginActivity.class);
		logoutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
		SpikaEnterpriseApp.getInstance().stopSocket();
		ctx.startActivity(logoutIntent);
		((Activity) ctx).finish();
	}
	
	public static void saveMap(Context ctx, Map<String,String> inputMap){
		Preferences pref = SpikaEnterpriseApp.getSharedPreferences(ctx);
        if (pref != null){
            JSONObject jsonObject = new JSONObject(inputMap);
            String jsonString = jsonObject.toString();
            String userId = pref.getCustomString(Const.USER_ID);
            pref.setCustomString(userId, jsonString);
        }
    }
	
	public static Map<String,String> loadMap(Context ctx){
		
        Map<String, String> outputMap = new HashMap<String,String>();
        Preferences pref = SpikaEnterpriseApp.getSharedPreferences(ctx);
        String userId = pref.getCustomString(Const.USER_ID);
        try{
            if (pref != null){       
                String jsonString = pref.getCustomString(userId);
                if (!jsonString.equals("")){
                	JSONObject jsonObject = new JSONObject(jsonString);
                    Iterator<String> keysItr = jsonObject.keys();
                    while(keysItr.hasNext()) {
                        String key = keysItr.next();
                        String value = (String) jsonObject.get(key);
                        outputMap.put(key, value);
                    }
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return outputMap;
    }
	
	public static String getStoredChatPassword(Context ctx, String chatId){
		
		String password = "";
		Map<String, String> chatPasswords = loadMap(ctx);
		password = chatPasswords.get(chatId);
		Logger.d("ovo je pass: " + password);
		return password;
		
	}
	
	public static void storeChatPassword(Context ctx, String chatPassword, String chatId){
		
		Map<String, String> chatPasswords = loadMap(ctx);
		if (chatPasswords.containsKey(chatId)){
			chatPasswords.remove(chatId);
		} 
		chatPasswords.put(chatId, chatPassword);
		saveMap(ctx, chatPasswords);
		
	}
	
	public static ArrayList<String> getAllShownImagesPath(Activity activity) {
		Uri uri;
		Cursor cursor;
		int column_index_data, column_index_folder_name;
		ArrayList<String> listOfAllImages = new ArrayList<String>();
		String absolutePathOfImage = null;
		uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

		String[] projection = { MediaColumns.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME };

		cursor = activity.getContentResolver().query(uri, projection, null, null, null);

		column_index_data = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
		column_index_folder_name = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
		cursor.moveToLast();
		while (cursor.moveToPrevious()) {
			absolutePathOfImage = cursor.getString(column_index_data);

			listOfAllImages.add(absolutePathOfImage);
		}
		return listOfAllImages;
	}
	

}

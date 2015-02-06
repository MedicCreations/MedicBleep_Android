/*
 * The MIT License (MIT)
 * 
 * Copyright � 2013 Clover Studio Ltd. All rights reserved.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.clover.spika.enterprise.chat.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.Bitmap;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.clover.spika.enterprise.chat.LoginActivity;
import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.dialogs.AppDialog;
import com.clover.spika.enterprise.chat.extendables.LoginBaseActivity;
import com.clover.spika.enterprise.chat.security.JNAesCrypto;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.Random;

/**
 * Utils
 * 
 * Contains various methods used through the application.
 */

@SuppressLint("SimpleDateFormat")
public class Utils {

	/**
	 * Returns encoded file or null if exception is raised
	 * 
	 * @param filePath
	 * @param ctx
	 * @return
	 */
	public static String handleFileEncryption(String filePath, Context ctx) {
		try {
			if (JNAesCrypto.isEncryptionEnabled) {
				File tempOut = new File(Utils.getFileDir(ctx), Const.APP_SPEN_TEMP_FILE);
				tempOut.createNewFile();

				File out = new File(Utils.getFileDir(ctx), Const.APP_SPEN_FILE);
				out.createNewFile();

				JNAesCrypto.encryptWithFiles(new File(filePath), tempOut, out);

				return out.getAbsolutePath();
			}
		} catch (Exception e) {
			if (Const.DEBUG_CRYPTO)
				e.printStackTrace();
			return null;
		}

		return filePath;
	}

	/**
	 * Handle file decryption
	 * 
	 * @param filePath
	 * @param ctx
	 * @return
	 */
	public static String handleFileDecryption(String filePath, Context ctx) {
		try {
			if (JNAesCrypto.isEncryptionEnabled) {
				File tempOut = new File(Utils.getFileDir(ctx), Const.APP_SPEN_TEMP_FILE);
				tempOut.createNewFile();

				File out = new File(Utils.getFileDir(ctx), Const.APP_SPEN_FILE);
				out.createNewFile();

				JNAesCrypto.decryptJNFiles(new File(filePath), tempOut, out);

				return out.getAbsolutePath();
			}
		} catch (Exception e) {
			if (Const.DEBUG_CRYPTO)
				e.printStackTrace();
			return null;
		}

		return filePath;
	}

	public static void copyStream(InputStream is, OutputStream os) {
		final int buffer_size = 1024;
		try {
			byte[] bytes = new byte[buffer_size];
			for (;;) {
				int count = is.read(bytes, 0, buffer_size);
				if (count == -1)
					break;
				os.write(bytes, 0, count);
			}
		} catch (Exception ex) {
		}
	}

	/**
	 * Returns length of jsonArray
	 * 
	 * @param json
	 * @return length if parameter is JSON Array, 0 if something else
	 */
	public static int getJsonArrayLength(String json) {
		try {
			JSONArray jsonArray = new JSONArray(json);
			return jsonArray.length();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public static boolean isOsVersionHigherThenGingerbread() {
		if (android.os.Build.VERSION.RELEASE.startsWith("1.") || android.os.Build.VERSION.RELEASE.startsWith("2.0") || android.os.Build.VERSION.RELEASE.startsWith("2.1")
				|| android.os.Build.VERSION.RELEASE.startsWith("2.2") || android.os.Build.VERSION.RELEASE.startsWith("2.3")) {
			return false;
		} else {
			return true;
		}
	}

	public static String generateToken() {
		char[] chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
		StringBuilder sb = new StringBuilder();
		Random random = new Random();

		for (int i = 0; i < 40; i++) {
			char c = chars[random.nextInt(chars.length)];
			sb.append(c);
		}

		return sb.toString();
	}

	public static String getClassNameInStr(Object obj) {

		Class<?> enclosingClass = obj.getClass().getEnclosingClass();
		if (enclosingClass != null) {
			return enclosingClass.getName();
		} else {
			return obj.getClass().getName();
		}

	}

	public static boolean saveBitmapToFile(Bitmap bitmap, String path) {
		File file = new File(path);
		FileOutputStream fOut;
		try {
			fOut = new FileOutputStream(file);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 95, fOut);
			fOut.flush();
			fOut.close();
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;

	}

	// encrypt and save bitmap to txt
	public static boolean saveTxtToFileWithEncrypt(Bitmap bitmap, String path) {
		String filename = path;

		File file = new File(filename);
		FileOutputStream fos;
		String content = "";
		// try {
		// content=AESCrypto.encrypt(bitmap);
		//
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		try {
			content = JNAesCrypto.encryptJN(bitmap);
			// content=JNAesCrypto.encryptJNChunk(bitmap);

		} catch (Exception e) {
			if (Const.DEBUG_CRYPTO)
				e.printStackTrace();
		}
		byte[] data = content.getBytes();
		try {
			fos = new FileOutputStream(file);
			fos.write(data);
			fos.flush();
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return false;

	}

	// encrypt and save string to txt if to encrypt is true, else just save
	// string to txt
	public static boolean saveTxtToFileWithEncrypt(String dataInput, String path, boolean toEncrypt) {
		String filename = path;

		File file = new File(filename);
		FileOutputStream fos;
		String content = "";
		if (toEncrypt) {
			// try {
			// content=AESCrypto.encrypt(dataInput);
			//
			// } catch (Exception e) {
			// e.printStackTrace();
			// }
			try {
				content = JNAesCrypto.encryptJN(dataInput);

			} catch (Exception e) {
				if (Const.DEBUG_CRYPTO)
					e.printStackTrace();
			}
		} else {
			content = dataInput;
		}

		byte[] data = content.getBytes();
		try {
			fos = new FileOutputStream(file);
			fos.write(data);
			fos.flush();
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return false;

	}

	// encrypt and save byte array to txt
	public static boolean saveTxtToFileWithEncrypt(byte[] dataInput, String path, boolean toEncrypt) {
		String filename = path;

		File file = new File(filename);
		FileOutputStream fos;
		byte[] content = null;
		if (toEncrypt) {
			// try {
			// content=AESCrypto.encrypt(dataInput);
			//
			// } catch (Exception e) {
			// e.printStackTrace();
			// }
			try {
				content = JNAesCrypto.encryptJN(dataInput);
			} catch (Exception e) {
				if (Const.DEBUG_CRYPTO)
					e.printStackTrace();
			}
		} else {
			content = dataInput;
		}

		byte[] data = content;
		try {
			fos = new FileOutputStream(file);
			fos.write(data);
			fos.flush();
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return false;

	}

	// get bytearray from txt file
	public static byte[] getByteArrayFromFile(String filePath) throws Exception {
		File file = new File(filePath);
		int size = (int) file.length();
		byte[] bytes = new byte[size];
		try {
			BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
			buf.read(bytes, 0, bytes.length);
			buf.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bytes;
	}

	// get string from file
	public static String getStringFromFile(String filePath) throws Exception {
		File fl = new File(filePath);
		FileInputStream fin = new FileInputStream(fl);
		String ret = convertStreamToString(fin);
		// Make sure you close all streams.
		fin.close();
		return ret;
	}

	public static String convertStreamToString(InputStream is) throws Exception {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		String line = null;
		while ((line = reader.readLine()) != null) {
			sb.append(line).append("\n");
		}
		reader.close();
		return sb.toString();
	}

	public static Bitmap scaleBitmap(Bitmap originalBitmap, int toWidth, int toHeight) {

		int image_width = originalBitmap.getWidth();
		int image_height = originalBitmap.getHeight();

		float scale = (float) toWidth / (float) image_width;

		if (image_width < image_height) {
			scale = toHeight / image_height;
		}

		return Bitmap.createScaledBitmap(originalBitmap, (int) (image_width * scale), (int) (image_height * scale), true);

	}

	public static boolean isSmallHeap() {
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);
		df.setMinimumFractionDigits(2);
		if ((Double.valueOf(Runtime.getRuntime().maxMemory() / 1048576)) < 200.00) {
			return true;
		} else {
			return false;
		}
	}

	public static File getFileDir(Context context) {
		File cacheDir = null;

		if (Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
			cacheDir = new File(android.os.Environment.getExternalStorageDirectory(), Const.APP_FILES_DIRECTORY);
		} else {
			cacheDir = context.getCacheDir();
		}

		if (!cacheDir.exists()) {
			cacheDir.mkdirs();
		}

		return cacheDir;
	}

	public static File getTempFile(Context context, String name) {
		return new File(getFileDir(context), TextUtils.isEmpty(name) ? "temp.spika" : name);
	}

	public static void logHeap(String step) {
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);
		df.setMinimumFractionDigits(2);

		Log.d("MEMORY_HANDLING", "debug. =================================" + step);
		Log.d("MEMORY_HANDLING",
				"debug.memory: allocated: " + df.format(Double.valueOf(Runtime.getRuntime().totalMemory() / 1048576)) + "MB of "
						+ df.format(Double.valueOf(Runtime.getRuntime().maxMemory() / 1048576)) + "MB (" + df.format(Double.valueOf(Runtime.getRuntime().freeMemory() / 1048576))
						+ "MB free)");
		Log.d("MEMORY_HANDLING", "debug. =================================END");
	}

	public static String getHexString(String string) throws NoSuchAlgorithmException, UnsupportedEncodingException {

		byte[] arrayBytes = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));

		if (arrayBytes == null) {
			return null;
		}

		StringBuffer stringBuffer = new StringBuffer();
		for (int i = 0; i < arrayBytes.length; i++) {
			stringBuffer.append(Integer.toString((arrayBytes[i] & 0xff) + 0x100, 16).substring(1));
		}

		return stringBuffer.toString();
	}

	public static boolean isBuildOver(int version) {
		if (android.os.Build.VERSION.SDK_INT > version)
			return true;
		return false;
	}

	public static void onFailedUniversal(String message, final Context ctx) {

		if (TextUtils.isEmpty(message)) {
			message = ctx.getString(R.string.e_something_went_wrong);
		}

		AppDialog dialog = new AppDialog(ctx, false);
		dialog.setFailed(message);
		dialog.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {

				Intent intent = new Intent(ctx, LoginActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				ctx.startActivity(intent);
			}
		});
	}

}
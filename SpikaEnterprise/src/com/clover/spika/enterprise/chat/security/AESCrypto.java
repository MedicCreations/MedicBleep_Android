package com.clover.spika.enterprise.chat.security;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class AESCrypto {

	private static byte[] rawKey = null;// new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9,
										// 10, 11, 12, 13, 14, 15, 16};// =
										// "4157d730".getBytes();

	public static byte[] generateKeyNEW() {
		// SecretKey keyFactory = null;
		// SecretKey secret = null;
		// KeySpec spec = new PBEKeySpec(new char[]{'p', 'a', 's', 's', 'w',
		// 'd'}, new byte[]{1,2,3,4,5,6,7,8}, 65536, 128);
		// try {
		// keyFactory =
		// SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(spec);
		// secret = new SecretKeySpec(keyFactory.getEncoded(), "AES");
		// } catch (NoSuchAlgorithmException e) {
		// } catch (InvalidKeySpecException e) {
		// e.printStackTrace();
		// }

		// PKCS5S2ParametersGenerator generator = new
		// PKCS5S2ParametersGenerator(new SHA1Digest ());
		// generator.init(PBEParametersGenerator.PKCS5PasswordToUTF8Bytes(new
		// char[]{'p', 'a', 's', 's', 'w', 'd', 'a', 'a', 's', '3', 'z'}), new
		// byte[]{1,2,3,4,5,6,7,8}, 65536);
		// KeyParameter key =
		// (KeyParameter)generator.generateDerivedMacParameters(256);

		SecretKey key = null;
		try {
			final String utf8 = "utf-8";

			String password = "pass";
			byte[] keyBytes = Arrays.copyOf(password.getBytes(utf8), 32);
			key = new SecretKeySpec(keyBytes, "AES");

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return key.getEncoded();
	}

	// *******encrypt string and return string
	public static String encrypt(String cleartext) throws Exception {
		if (rawKey == null)
			// rawKey =
			// getRawKey(SpikaApp.getInstance().getSeedForCrypt().getBytes());
			rawKey = generateKeyNEW();
		byte[] result = encrypt(rawKey, cleartext.getBytes());
		return toHex(result);
		// return new String(Base64.encodeBase64(result));
	}

	// *******decrypt string and return string
	public static String decrypt(String encrypted) throws Exception {
		if (rawKey == null)
			// rawKey =
			// getRawKey(SpikaApp.getInstance().getSeedForCrypt().getBytes());
			rawKey = generateKeyNEW();
		byte[] enc = toByte(encrypted);
		// byte[] enc = Base64.decodeBase64(encrypted.getBytes());
		byte[] result = decrypt(rawKey, enc);
		return new String(result);
	}

	// *******encrypt bitmap and return string
	public static String encrypt(Bitmap bitmap) throws Exception {
		if (rawKey == null)
			rawKey = generateKeyNEW();
		// rawKey =
		// getRawKey(SpikaApp.getInstance().getSeedForCrypt().getBytes());
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
		byte[] result = encrypt(rawKey, stream.toByteArray());
		return toHex(result);
	}

	// *******decrypt string and return bitmap
	public static Bitmap decryptBitmap(String encrypted) throws Exception {
		if (rawKey == null)
			rawKey = generateKeyNEW();
		// rawKey =
		// getRawKey(SpikaApp.getInstance().getSeedForCrypt().getBytes());
		byte[] enc = toByte(encrypted);
		byte[] result = decrypt(rawKey, enc);
		return BitmapFactory.decodeByteArray(result, 0, result.length);
	}

	// *******encrypt byte array and return byte array
	public static byte[] encrypt(byte[] clearData) throws Exception {
		if (rawKey == null)
			rawKey = generateKeyNEW();
		// rawKey =
		// getRawKey(SpikaApp.getInstance().getSeedForCrypt().getBytes());
		byte[] result = encrypt(rawKey, clearData);
		return result;
	}

	// *******decrypt byte array and return byte array
	public static byte[] decrypt(byte[] encrypted) throws Exception {
		if (rawKey == null)
			rawKey = generateKeyNEW();
		// rawKey =
		// getRawKey(SpikaApp.getInstance().getSeedForCrypt().getBytes());
		byte[] enc = encrypted;
		byte[] result = decrypt(rawKey, enc);
		return result;
	}

	// *************************************************//////////////////////////

	// private static byte[] getRawKey(byte[] seed) throws Exception {
	// KeyGenerator kgen = KeyGenerator.getInstance("AES");
	// SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
	// sr.setSeed(seed);
	// kgen.init(128, sr); // 192 and 256 bits may not be available
	// SecretKey skey = kgen.generateKey();
	//
	// byte[] raw = skey.getEncoded();
	// return raw;
	// }

	private static byte[] encrypt(byte[] raw, byte[] clear) throws Exception {
		SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
		byte[] encrypted = cipher.doFinal(clear);
		return encrypted;
	}

	private static byte[] decrypt(byte[] raw, byte[] encrypted) throws Exception {
		SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.DECRYPT_MODE, skeySpec);
		byte[] decrypted = cipher.doFinal(encrypted);
		return decrypted;
	}

	public static String toHex(String txt) {
		return toHex(txt.getBytes());
	}

	public static String fromHex(String hex) {
		return new String(toByte(hex));
	}

	public static byte[] toByte(String hexString) {
		int len = hexString.length() / 2;
		byte[] result = new byte[len];
		for (int i = 0; i < len; i++)
			result[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2), 16).byteValue();
		return result;
	}

	public static String toHex(byte[] buf) {
		if (buf == null)
			return "";
		StringBuffer result = new StringBuffer(2 * buf.length);
		for (int i = 0; i < buf.length; i++) {
			appendHex(result, buf[i]);
		}
		return result.toString();
	}

	private final static String HEX = "0123456789ABCDEF";

	private static void appendHex(StringBuffer sb, byte b) {
		sb.append(HEX.charAt((b >> 4) & 0x0f)).append(HEX.charAt(b & 0x0f));
	}

}

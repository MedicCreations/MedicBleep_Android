package com.clover.spika.enterprise.chat.security;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.clover.spika.enterprise.chat.cryptor.AES256JNCryptor;
import com.clover.spika.enterprise.chat.cryptor.JNCryptor;

public class JNAesCrypto {

	private static JNCryptor cryptor = new AES256JNCryptor(SecureConst.ITERATIONS);

	// *******encrypt string and return string
	public static String encryptJN(String textToEncrypt) throws Exception {

		byte[] text = textToEncrypt.getBytes();

		byte[] cypterText = cryptor.encryptData(text, SecureConst.PASSWORD.toCharArray());

		String cypherString = toHex(cypterText);

		return cypherString;
	}

	// *******encrypt bitmap and return string
	public static String encryptJN(Bitmap bitmap) throws Exception {

		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);

		byte[] image = stream.toByteArray();

		byte[] cypterText = cryptor.encryptData(image, SecureConst.PASSWORD.toCharArray());

		String cypherString = toHex(cypterText);

		return cypherString;
	}

	// *******encrypt byte array and return byte array
	public static byte[] encryptJN(byte[] clearData) throws Exception {

		byte[] cypterText = cryptor.encryptData(clearData, SecureConst.PASSWORD.toCharArray());

		String hexText = toHex(cypterText);

		return hexText.getBytes();
	}

	public static void encryptWithFiles(File in, File tempOut, File out) throws Exception {
		cryptor.encryptData(SecureConst.PASSWORD.toCharArray(), in, tempOut);

		FileInputStream inputHex = new FileInputStream(tempOut);
		FileOutputStream ouputHex = new FileOutputStream(out);

		int size = 32 * 1024;
		byte[] chunk = new byte[size];
		int chunkLen = 0;

		while ((chunkLen = inputHex.read(chunk)) != -1) {
			byte[] temp = new byte[chunkLen];
			for (int i = 0; i < chunkLen; i++) {
				temp[i] = chunk[i];
			}
			byte[] toHex = toHex(temp).getBytes();
			ouputHex.write(toHex, 0, toHex.length);
		}

		inputHex.close();
		ouputHex.close();
		tempOut.delete();
	}

	// *******encrypt byte array and return String
	public static String encryptJNSTR(byte[] clearData) throws Exception {

		byte[] cypterText = cryptor.encryptData(clearData, SecureConst.PASSWORD.toCharArray());

		String cypherString = toHex(cypterText);

		return cypherString;
	}

	// *******decrypt string and return string
	public static String decryptJN(String encrypted) throws Exception {

		byte[] textText = toByte(encrypted);

		byte[] decipherText = cryptor.decryptData(textText, SecureConst.PASSWORD.toCharArray());

		String decypher = new String(decipherText);

		return decypher;
	}

	// *******decrypt string and return bitmap
	public static Bitmap decryptBitmapJN(String encrypted) throws Exception {

		byte[] textText = toByte(encrypted);

		byte[] decipherImage = cryptor.decryptData(textText, SecureConst.PASSWORD.toCharArray());

		return BitmapFactory.decodeByteArray(decipherImage, 0, decipherImage.length);
	}

	// *******decrypt byte array and return byte array
	public static byte[] decryptJN(byte[] encrypted) throws Exception {

		byte[] deHex = toByte(new String(encrypted));

		byte[] decipher = cryptor.decryptData(deHex, SecureConst.PASSWORD.toCharArray());

		return decipher;
	}

	// *******decrypt byte array and return byte array
	public static void decryptJNFiles(File in, File tempOut, File out) throws Exception {

		FileInputStream inputHex = new FileInputStream(in);
		FileOutputStream ouputHex = new FileOutputStream(tempOut);

		int size = 32 * 1024;
		byte[] chunk = new byte[size];
		int chunkLen = 0;

		while ((chunkLen = inputHex.read(chunk)) != -1) {
			byte[] temp = new byte[chunkLen];
			for (int i = 0; i < chunkLen; i++) {
				temp[i] = chunk[i];
			}
			byte[] deHex = toByte(new String(temp));
			ouputHex.write(deHex, 0, deHex.length);
		}

		inputHex.close();
		ouputHex.close();
		in.delete();

		cryptor.decryptData(SecureConst.PASSWORD.toCharArray(), tempOut, out);

	}

	// to hex methods

	public static String toHex(String txt) {
		return toHex(txt.getBytes());
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

	// from hex methods
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
}

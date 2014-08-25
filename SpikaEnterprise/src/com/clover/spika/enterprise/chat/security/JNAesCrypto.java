package com.clover.spika.enterprise.chat.security;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.clover.spika.enterprise.chat.cryptor.AES256JNCryptor;
import com.clover.spika.enterprise.chat.cryptor.JNCryptor;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Helper;
import com.clover.spika.enterprise.chat.utils.Utils;

public class JNAesCrypto {

	public static final boolean isEncrypted = true;
	private static JNCryptor cryptor = new AES256JNCryptor(Const.S_ITERATIONS);

	// *******encrypt string and return string
	public static String encryptJN(String textToEncrypt) throws Exception {

		if (!isEncrypted) {
			return textToEncrypt;
		}

		byte[] text = textToEncrypt.getBytes();
		byte[] cypterText = cryptor.encryptData(text, Const.getPassword());

		String cypherString = toHex(cypterText);

		return cypherString;
	}

	/**
	 * encrypt bitmap and return string
	 * 
	 * If result is null, encryption is disabled and you should use the original
	 * bitmap.
	 * 
	 * @param bitmap
	 * @return
	 * @throws Exception
	 */
	public static String encryptJN(Bitmap bitmap) throws Exception {

		if (!isEncrypted) {
			return null;
		}

		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);

		byte[] image = stream.toByteArray();

		byte[] cypterText = cryptor.encryptData(image, Const.getPassword());

		String cypherString = toHex(cypterText);

		return cypherString;
	}

	// *******encrypt byte array and return byte array
	public static byte[] encryptJN(byte[] clearData) throws Exception {

		if (!isEncrypted) {
			return clearData;
		}

		byte[] cypterText = cryptor.encryptData(clearData, Const.getPassword());

		String hexText = toHex(cypterText);

		return hexText.getBytes();
	}

	/**
	 * Encrypt a file
	 * 
	 * @param in
	 * @param tempOut
	 * @param out
	 * @throws Exception
	 */
	public static void encryptWithFiles(File in, File tempOut, File out) throws Exception {

		cryptor.encryptData(Const.getPassword(), in, tempOut);

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

	/**
	 * encrypt byte array and return String
	 * 
	 * If null has been returned you should use the original byte[]
	 * 
	 * @param clearData
	 * @return
	 * @throws Exception
	 */
	public static String encryptJNSTR(byte[] clearData) throws Exception {

		if (!isEncrypted) {
			return null;
		}

		byte[] cypterText = cryptor.encryptData(clearData, Const.getPassword());

		String cypherString = toHex(cypterText);

		return cypherString;
	}

	// *******decrypt string and return string
	public static String decryptJN(String encrypted) throws Exception {

		if (!isEncrypted) {
			return encrypted;
		}

		byte[] textText = toByte(encrypted);

		byte[] decipherText = cryptor.decryptData(textText, Const.getPassword());

		String decypher = new String(decipherText);

		return decypher;
	}

	// *******decrypt string and return bitmap
	public static Bitmap decryptBitmapJN(String encrypted, String filePath) throws Exception {

		if (!isEncrypted) {
			byte[] textText = Utils.getByteArrayFromFile(filePath);
			return BitmapFactory.decodeByteArray(textText, 0, textText.length);
		}

		byte[] textText = toByte(encrypted);
		byte[] decipherImage = cryptor.decryptData(textText, Const.getPassword());

		return BitmapFactory.decodeByteArray(decipherImage, 0, decipherImage.length);
	}

	// *******decrypt byte array and return byte array
	public static byte[] decryptJN(byte[] encrypted) throws Exception {

		if (!isEncrypted) {
			return encrypted;
		}

		byte[] deHex = toByte(new String(encrypted));

		byte[] decipher = cryptor.decryptData(deHex, Const.getPassword());

		return decipher;
	}

	/**
	 * decrypt byte array and return byte array
	 * 
	 * @param in
	 * @param tempOut
	 * @param out
	 * @return
	 * @throws Exception
	 */
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

		cryptor.decryptData(Const.getPassword(), tempOut, out);

		tempOut.delete();
	}

	// TODO
	public static void decryptIs(InputStream is, File out, Context ctx) throws Exception {

		File tempOut = new File(Utils.getFileDir(ctx), Const.APP_SPEN_TEMP_FILE);
		tempOut.createNewFile();

		File tempIn = new File(Utils.getFileDir(ctx), Const.APP_SPEN_FILE);
		tempIn.createNewFile();

		OutputStream os = new FileOutputStream(tempIn.getAbsolutePath());
		Helper.copyStream(is, os);
		os.close();
		is.close();

		FileOutputStream ouputHex = new FileOutputStream(tempOut);
		FileInputStream inputHex = new FileInputStream(tempIn);

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

		cryptor.decryptData(Const.getPassword(), tempOut, out);

		tempOut.delete();
		tempIn.delete();
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
			result[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2).trim(), 16).byteValue();
		return result;
	}
}

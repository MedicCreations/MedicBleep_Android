/*    Copyright 2014 Duncan Jones
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.clover.spika.enterprise.chat.cryptor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import com.clover.spika.enterprise.chat.utils.Utils;

import android.os.Environment;

/**
 * Base class for parsing and producing formatted ciphertext.
 */
abstract class AES256CiphertextFff {

	// Values are default protection to share with unit
	// tests
	static final int FLAG_PASSWORD = 0x01;
	static final int ENCRYPTION_SALT_LENGTH = 8;
	static final int HMAC_SALT_LENGTH = 8;
	static final int AES_BLOCK_SIZE = 16;
	static final int HMAC_SIZE = 32;
	static final int HEADER_SIZE = 2;

	static final int MINIMUM_LENGTH_WITH_PASSWORD = HEADER_SIZE + ENCRYPTION_SALT_LENGTH + HMAC_SALT_LENGTH + AES_BLOCK_SIZE + HMAC_SIZE;

	static final int MINIMUM_LENGTH_WITHOUT_PASSWORD = HEADER_SIZE + AES_BLOCK_SIZE + HMAC_SIZE;

	private final int version;
	private final byte options;
	private final byte[] encryptionSalt;
	private final byte[] hmacSalt;
	private final byte[] iv;
	private File dataFile;
	private File fileOut;
	private File fileOutWithoutHMAC;
	private byte[] hmac;

	private final boolean isPasswordBased;

	/**
	 * Constructs a {@code CryptorData} from its constituent parts. An
	 * {@code IllegalArgumentException} is thrown if any of the parameters are
	 * of the wrong length or invalid.
	 * <p>
	 * This constructor is used if the data was encrypted with a password.
	 * 
	 * @param encryptionSalt
	 *            the encryption salt
	 * @param hmacSalt
	 *            the HMAC salt
	 * @param iv
	 *            the initialisation value
	 * @param ciphertext
	 *            the encrypted data
	 */
	AES256CiphertextFff(byte[] encryptionSalt, byte[] hmacSalt, byte[] iv, File in, File out) {

		validateLength(encryptionSalt, "encryption salt", ENCRYPTION_SALT_LENGTH);
		validateLength(hmacSalt, "HMAC salt", HMAC_SALT_LENGTH);
		validateLength(iv, "IV", AES_BLOCK_SIZE);

		this.version = getVersionNumber();
		this.options = FLAG_PASSWORD;
		this.encryptionSalt = encryptionSalt;
		this.hmacSalt = hmacSalt;
		this.iv = iv;
		this.dataFile = in;
		this.fileOut = out;
		this.isPasswordBased = true;
		this.fileOutWithoutHMAC = new File(Environment.getExternalStorageDirectory() + "/tempA");

		// HMAC will be set later
		hmac = new byte[HMAC_SIZE];
	}

	/**
	 * Constructs a {@code CryptorData} from its constituent parts. An
	 * {@code IllegalArgumentException} is thrown if any of the parameters are
	 * of the wrong length or invalid.
	 * <p>
	 * This constructor is used if the data was encrypted with a key.
	 * 
	 * @param iv
	 *            the initialisation value
	 * @param ciphertext
	 *            the encrypted data
	 */
	AES256CiphertextFff(byte[] iv, byte[] ciphertext) {

		validateLength(iv, "IV", AES_BLOCK_SIZE);

		this.version = getVersionNumber();
		this.options = 0;
		this.iv = iv;

		this.encryptionSalt = null;
		this.hmacSalt = null;
		this.isPasswordBased = false;

		// HMAC will be set later
		hmac = new byte[HMAC_SIZE];
	}

	/**
	 * Checks the length of a byte array.
	 * 
	 * @param data
	 *            the data to check
	 * @param dataName
	 *            the name of the field (to include in the exception)
	 * @param expectedLength
	 *            the length the data should be
	 * @throws IllegalArgumentException
	 *             if the data is not of the correct length
	 */
	private static void validateLength(byte[] data, String dataName, int expectedLength) throws IllegalArgumentException {
		if (data.length != expectedLength) {
			throw new IllegalArgumentException(String.format("Invalid %s length. Expected %d bytes but found %d.", dataName, expectedLength, data.length));
		}
	}

	/**
	 * Returns the ciphertext, packaged as a byte array.
	 * 
	 * @return the byte array
	 * @throws IOException
	 */
	void getRawData() throws IOException {

		// Header: [Version | Options]
		byte[] header = new byte[] { (byte) getVersionNumber(), 0 };

		if (isPasswordBased) {
			header[1] |= FLAG_PASSWORD;
		}

		// Pack result
		int dataSize = 0;

		if (isPasswordBased) {
			dataSize = (int) (header.length + encryptionSalt.length + hmacSalt.length + iv.length + dataFile.length() + hmac.length);
		} else {
			// dataSize = header.length + iv.length + ciphertext.length +
			// hmac.length;
			dataSize = 5;
		}

		byte[] result = new byte[dataSize];

		if (isPasswordBased) {
			FileOutputStream os = new FileOutputStream(fileOut);
			FileOutputStream osWHMAC = new FileOutputStream(fileOutWithoutHMAC);
			os.write(header, 0, header.length);
			os.write(encryptionSalt, 0, encryptionSalt.length);
			os.write(hmacSalt, 0, hmacSalt.length);
			os.write(iv, 0, iv.length);

			osWHMAC.write(header, 0, header.length);
			osWHMAC.write(encryptionSalt, 0, encryptionSalt.length);
			osWHMAC.write(hmacSalt, 0, hmacSalt.length);
			osWHMAC.write(iv, 0, iv.length);

			FileInputStream input = new FileInputStream(dataFile);

			int size = 32 * 1024;
			byte[] chunk = new byte[size];
			int chunkLen = 0;

			while ((chunkLen = input.read(chunk)) != -1) {
				byte[] temp = new byte[chunkLen];
				for (int i = 0; i < chunkLen; i++) {
					temp[i] = chunk[i];
				}
				os.write(temp, 0, temp.length);
				osWHMAC.write(temp, 0, temp.length);
			}
			input.close();
			os.write(hmac, 0, hmac.length);

			os.close();
			osWHMAC.close();

			// System.arraycopy(src, srcPos, dst, dstPos, length)
			// System.arraycopy(encryptionSalt, 0, result, header.length,
			// encryptionSalt.length);
			// System.arraycopy(hmacSalt, 0, result, header.length +
			// encryptionSalt.length, hmacSalt.length);
			// System.arraycopy(iv, 0, result, header.length +
			// encryptionSalt.length + hmacSalt.length, iv.length);
			// System.arraycopy(ciphertext, 0, result,
			// header.length + encryptionSalt.length + hmacSalt.length +
			// iv.length, ciphertext.length);
			// System.arraycopy(hmac, 0, result, header.length +
			// encryptionSalt.length + hmacSalt.length + iv.length
			// + ciphertext.length, hmac.length);
		} else {
			// System.arraycopy(iv, 0, result, header.length, iv.length);
			// System.arraycopy(ciphertext, 0, result, header.length +
			// iv.length, ciphertext.length);
			// System.arraycopy(hmac, 0, result, header.length + iv.length +
			// ciphertext.length, hmac.length);
		}

	}

	/**
	 * @return the data to compute the HMAC over
	 * @throws Exception
	 */
	byte[] getDataToHMAC() throws Exception {
		// byte[] rawData = getRawData();
		// byte[] result = new byte[rawData.length - HMAC_SIZE];
		// System.arrarraycopy(rawData, 0, result, 0, result.length);
		return Utils.getByteArrayFromFile(fileOutWithoutHMAC.getPath());
	}

	/**
	 * @return the version
	 */
	int getVersion() {
		return version;
	}

	/**
	 * @return the options
	 */
	byte getOptions() {
		return options;
	}

	/**
	 * @return the encryptionSalt
	 */
	byte[] getEncryptionSalt() {
		return encryptionSalt;
	}

	/**
	 * @return the hmacSalt
	 */
	byte[] getHmacSalt() {
		return hmacSalt;
	}

	/**
	 * @return the iv
	 */
	byte[] getIv() {
		return iv;
	}

	/**
	 * @return the hmac
	 */
	byte[] getHmac() {
		return hmac;
	}

	/**
	 * Indicates if the ciphertext was created using a password. If so, then the
	 * salt values will be present in the ciphertext.
	 * 
	 * @return <code>true</code> if the ciphertext was created with a password
	 *         (not a key), <code>false</code> otherwise
	 */
	public boolean isPasswordBased() {
		return isPasswordBased;
	}

	/**
	 * @param hmac
	 *            the hmac to set
	 */
	void setHmac(byte[] hmac) {
		this.hmac = hmac;
	}

	/**
	 * @return the expected version number
	 */
	abstract int getVersionNumber();

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(encryptionSalt);
		result = prime * result + Arrays.hashCode(hmac);
		result = prime * result + Arrays.hashCode(hmacSalt);
		result = prime * result + (isPasswordBased ? 1231 : 1237);
		result = prime * result + Arrays.hashCode(iv);
		result = prime * result + options;
		result = prime * result + version;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		AES256CiphertextFff other = (AES256CiphertextFff) obj;
		if (!Arrays.equals(encryptionSalt, other.encryptionSalt)) {
			return false;
		}
		if (!Arrays.equals(hmac, other.hmac)) {
			return false;
		}
		if (!Arrays.equals(hmacSalt, other.hmacSalt)) {
			return false;
		}
		if (isPasswordBased != other.isPasswordBased) {
			return false;
		}
		if (!Arrays.equals(iv, other.iv)) {
			return false;
		}
		if (options != other.options) {
			return false;
		}
		if (version != other.version) {
			return false;
		}
		return true;
	}
}

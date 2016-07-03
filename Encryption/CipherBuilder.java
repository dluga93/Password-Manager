package PwdManager.Encryption;
import PwdManager.Logger;
import PwdManager.EncodedFileReader;
import PwdManager.Registration;

import javax.crypto.*;
import javax.crypto.spec.*;
import java.security.*;
import java.io.*;

public class CipherBuilder {
	private static final String cipherInitString = "AES/CBC/PKCS5Padding";
	private static final int pbeIterations = 1000;
	public static enum KeyTypes {
		AES("AES", 128),
		HMACSHA1("HmacSHA1", 128);
		private final String type;
		private final int sizeInBits;
		
		KeyTypes(String type, int sizeInBits) {
			this.type = type;
			this.sizeInBits = sizeInBits;
		}

		public String getType() {
			return type;
		}

		public int getSizeInBits() {
			return sizeInBits;
		}
	}

	/*public static StringCipher build(byte[] keyBytes) {
		Cipher cipher = createCipher();
		SecretKey secretKey = new SecretKeySpec(keyBytes, 0, keyBytes.length, KeyTypes.AES.getType());
		return new StringCipherImpl(cipher, secretKey);
	}*/

	// temporary. join with above and make the macBytes parameter non-optional
	public static StringCipher build(byte[] keyBytes, byte[] macBytes) throws Exception {
		Cipher cipher = createCipher();
		SecretKey secretKey = new SecretKeySpec(keyBytes, 0, keyBytes.length, KeyTypes.AES.getType());
		Hmac hmac = new Hmac(macBytes);
		return new StringCipherImpl(cipher, secretKey, hmac);
	}

	public static StringCipher build(String user, String password) throws Exception {
		Cipher cipher = createCipher();
		byte[] salt = readSaltFromFile(user);
		SecretKey secretKey = keyFromPasswordAndSalt(password, salt);
		return new StringCipherImpl(cipher, secretKey);
	}

	public static StringCipher build(String password, byte[] salt) {
		Cipher cipher = createCipher();
		SecretKey secretKey = keyFromPasswordAndSalt(password, salt);
		return new StringCipherImpl(cipher, secretKey);
	}

	private static Cipher createCipher() {
		try {
			return Cipher.getInstance(cipherInitString);
		} catch (Exception e) {
			Logger.logException("Unknown options for encryption algorithm.", e);
			System.exit(1);
		}
		return null;
	}

	private static byte[] readSaltFromFile(String user) throws Exception {
		try {
			EncodedFileReader fileReader = new EncodedFileReader(Registration.saltFilename(user));
			byte[] salt = fileReader.readData();
			fileReader.close();
			return salt;
		} catch (FileNotFoundException e) {
			throw new Exception("Can't find salt file.");
		} catch (Exception e) {
			throw new Exception("Problem reading salt file.", e);
		}
	}

	private static SecretKey keyFromPasswordAndSalt(String password, byte[] salt) {
		try {
			char[] chars = password.toCharArray();
			PBEKeySpec spec = new PBEKeySpec(chars, salt, pbeIterations, KeyTypes.AES.getSizeInBits());
			SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			byte[] secretKeyBytes = skf.generateSecret(spec).getEncoded();
			return new SecretKeySpec(secretKeyBytes, 0, secretKeyBytes.length, KeyTypes.AES.getType());
		} catch (Exception e) {
			Logger.logException("Problem with creating master key from password and salt.", e);
			System.exit(1);
			return null;
		}
	}

	public static byte[] generateKey(KeyTypes keyType) {
		try {
			KeyGenerator keyGenerator = KeyGenerator.getInstance(keyType.getType());
			keyGenerator.init(keyType.getSizeInBits());
			return keyGenerator.generateKey().getEncoded();
		} catch (NoSuchAlgorithmException e) {
			Logger.logException("Unknown algorithm for key generation.", e);
			System.exit(1);
			return null;
		}
	}

	public static byte[] randomData(int size) {
		byte[] generated = new byte[size];
		SecureRandom prng = new SecureRandom();
		prng.nextBytes(generated);
		return generated;
	}
}
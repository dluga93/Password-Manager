package PwdManager.Encryption;
import PwdManager.Logger;
import PwdManager.EncodedFileReader;

import javax.crypto.*;
import javax.crypto.spec.*;
import java.security.*;
import java.io.*;

public class CipherBuilder {
	private static final String cipherInitString = "AES/CBC/PKCS5Padding";
	private static final int pbeIterations = 1000;
	public static final int keySizeInBits = 128;
	public static enum KeyTypes {
		AES("AES"),
		HMACSHA1("HmacSHA1");
		private final String type;
		
		KeyTypes(String type) {
			this.type = type;
		}

		public String getType() {
			return type;
		}
	}

	public static StringCipher build(byte[] keyBytes) {
		Cipher cipher = createCipher();
		SecretKey secretKey = new SecretKeySpec(keyBytes, 0, keyBytes.length, "AES");
		return new StringCipherImpl(cipher, secretKey);
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
			EncodedFileReader fileReader = new EncodedFileReader(user + "_salt");
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
			PBEKeySpec spec = new PBEKeySpec(chars, salt, pbeIterations, keySizeInBits);
			SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			byte[] secretKeyBytes = skf.generateSecret(spec).getEncoded();
			return new SecretKeySpec(secretKeyBytes, 0, secretKeyBytes.length, "AES");
		} catch (Exception e) {
			Logger.logException("Problem with creating master key from password and salt.", e);
			System.exit(1);
			return null;
		}
	}

	public static byte[] generateKey() {
		try {
			KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
			keyGenerator.init(CipherBuilder.keySizeInBits);
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
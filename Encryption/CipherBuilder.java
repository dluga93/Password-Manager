package Encryption;
import PwdManager.Logger;
import PwdManager.EncodedFileReader;

import javax.crypto.*;
import javax.crypto.spec.*;
import java.security.*;

public class CipherBuilder {
	private static final String cipherInitString = "AES/CBC/PKCS5Padding";
	private static final int pbeIterations = 1000;
	public static final int keySizeInBits = 128;

	public static StringCipher build(byte[] keyBytes) {
		Cipher cipher = createCipher();
		SecretKey secretKey = new SecretKeySpec(keyBytes, 0, keyBytes.length, "AES");
		return new StringCipherImpl(cipher, secretKey);
	}

	public static StringCipher build(String user, String password) {
		try {
			Cipher cipher = createCipher();
			byte[] salt = readSaltFromFile(user);
			SecretKey secretKey = keyFromPasswordAndSalt(password, salt);
			return new StringCipherImpl(cipher, secretKey);
		} catch (Exception e) {
			Logger.logError("User does not exist.", e);
			System.exit(1);
		}
		return null;
	}

	public static StringCipher build(String password, byte[] salt) {
		try {
			Cipher cipher = createCipher();
			SecretKey secretKey = keyFromPasswordAndSalt(password, salt);
			return new StringCipherImpl(cipher, secretKey);
		} catch (Exception e) {
			Logger.logError("Couldn't create master key.", e);
			System.exit(1);
		}
		return null;
	}

	private static Cipher createCipher() {
		try {
			return Cipher.getInstance(cipherInitString);
		} catch (Exception e) {
			Logger.logError("Unknown options for encryption algorithm.", e);
			System.exit(1);
		}
		return null;
	}

	private static byte[] readSaltFromFile(String user)
	throws Exception {
		EncodedFileReader fileReader = new EncodedFileReader(user + "_salt");
		byte[] salt = fileReader.readData();
		fileReader.close();
		return salt;
	}

	private static SecretKey keyFromPasswordAndSalt(String password, byte[] salt)
	throws Exception {
		char[] chars = password.toCharArray();
		PBEKeySpec spec = new PBEKeySpec(chars, salt, pbeIterations, keySizeInBits);
		SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		byte[] secretKeyBytes = skf.generateSecret(spec).getEncoded();
		return new SecretKeySpec(secretKeyBytes, 0, secretKeyBytes.length, "AES");
	}

	public static byte[] generateKey() {
		try {
			KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
			keyGenerator.init(CipherBuilder.keySizeInBits);
			return keyGenerator.generateKey().getEncoded();
		} catch (NoSuchAlgorithmException e) {
			Logger.logError("Unknown algorithm for key generation.", e);
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
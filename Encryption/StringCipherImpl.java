package PwdManager.Encryption;
import PwdManager.Logger;

import java.nio.charset.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import java.security.*;
import java.util.*;

class StringCipherImpl implements StringCipher {
	private final Cipher cipher;
	private final SecretKey secretKey;

	public StringCipherImpl(Cipher cipher, SecretKey secretKey) {
		this.cipher = cipher;
		this.secretKey = secretKey;
	}

	// also prepends IV
	public byte[] tryEncrypt(String plaintext) {
		return tryEncrypt(plaintext.getBytes(StandardCharsets.UTF_8));
	}

	// also prepends IV
	public byte[] tryEncrypt(byte[] plaintext) {
		try {
			return ivAndEncrypt(plaintext);
		} catch (Exception e) {
			Logger.logException("Encountered problem with encryption algorithm.", e);
			System.exit(1);
		}
		return null;
	}

	private byte[] ivAndEncrypt(byte[] plaintext)
	throws InvalidKeyException, IllegalBlockSizeException,
	InvalidAlgorithmParameterException, BadPaddingException {
		byte[] ivBytes = CipherBuilder.randomData(CipherBuilder.keySizeInBits/8);
		IvParameterSpec iv = new IvParameterSpec(ivBytes);
		cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
		
		byte[] encrypted = cipher.doFinal(plaintext);
		byte[] ivAndEncrypted = Utility.concatByteArray(ivBytes, encrypted);
		return ivAndEncrypted;
	}

	public String tryDecryptString(byte[] encrypted) {
		byte[] decrypted = tryDecrypt(encrypted);
		return new String(decrypted, StandardCharsets.UTF_8);
	}

	public byte[] tryDecrypt(byte[] encrypted) {
		try {
			byte[] iv = Arrays.copyOfRange(encrypted,0,CipherBuilder.keySizeInBits/8);
			byte[] cipherText = Arrays.copyOfRange(encrypted,
												   CipherBuilder.keySizeInBits/8,
												   encrypted.length);
			return decrypt(iv, cipherText);
		} catch (Exception e) {
			Logger.logException("Encountered problem with decryption algorithm.", e);
		}
		return null;
	}

	private byte[] decrypt(byte[] iv, byte[] ciphertext)
	throws InvalidKeyException, IllegalBlockSizeException,
	InvalidAlgorithmParameterException, BadPaddingException {
		IvParameterSpec IV = new IvParameterSpec(iv);
		cipher.init(Cipher.DECRYPT_MODE, secretKey, IV);
		byte[] decryptedBytes = cipher.doFinal(ciphertext);
		return decryptedBytes;
	}
}
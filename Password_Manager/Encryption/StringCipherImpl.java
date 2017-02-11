package Password_Manager.Encryption;

import java.nio.charset.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import java.security.*;
import java.util.*;

/**
 * @brief An implementation of the StringCipher interface
 */
class StringCipherImpl implements StringCipher {
	private final Cipher cipher;
	private final SecretKey secretKey;
	private final Hmac hmac;

	/**
	 * Constructs a StringCipher object from a java Cipher
	 * (javax.crypto.Cipher) and a secret key, without using an HMAC.
	 *
	 * @param      cipher     The java cipher
	 * @param      secretKey  The secret key
	 */
	public StringCipherImpl(Cipher cipher, SecretKey secretKey) {
		this(cipher, secretKey, null);
	}

	/**
	 * Constructs a StringCipher object from a java Cipher
	 * (javax.crypto.Cipher) and a secret key, and an Hmac object.
	 *
	 * @param      cipher     The java cipher
	 * @param      secretKey  The secret key
	 * @param      hmac       The Hmac object
	 */
	public StringCipherImpl(Cipher cipher, SecretKey secretKey, Hmac hmac) {
		this.cipher = cipher;
		this.secretKey = secretKey;
		this.hmac = hmac;
	}

	/**
	 * @brief Encrypt and mac string
	 * 
	 * Converts a string to a byte array using the UTF8
	 * character set. Next, mac the byte array, generate an IV,
	 * encrypt the macced byte array using the IV and prepend the
	 * IV to the encrypted data.
	 *
	 * @param      plaintext  The plaintext string
	 *                        
	 * @return     The encrypted data
	 *
	 * @throws     Exception  If unknown options were given to the encryption algorithm
	 */
	public byte[] tryEncrypt(String plaintext) throws Exception {
		return tryEncrypt(plaintext.getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * @brief Encrypt and mac a byte array
	 * 
	 * Mac a byte array if the Cipher contains a mac.
	 * Generate an IV, encrypt the macced byte array using the IV and prepend
	 * the IV to the encrypted data.
	 *
	 * @param      plaintext  The plaintext byte array
	 *                        
	 * @return     The encrypted data
	 *
	 * @throws     Exception  If unknown options were given to the encryption algorithm
	 */
	public byte[] tryEncrypt(byte[] plaintext) throws Exception {
		if (hmac != null)
			plaintext = hmac.mac(plaintext);
		return ivAndEncrypt(plaintext);
	}

	/**
	 * @brief Encrypt a byte array and prepend IV
	 * 
	 * Generate IV, encrypt ```plaintext```, prepend IV to the encrypted
	 * data and return the result. 
	 *
	 * @param      plaintext  The plaintext
	 *
	 * @return     The encrypted data
	 *
	 * @throws     Exception  If unknown options were given to the encryption algorithm
	 */
	private byte[] ivAndEncrypt(byte[] plaintext) throws Exception {
		try {
			byte[] ivBytes = CipherBuilder.randomData(CipherBuilder.encryptionKeyType.sizeInBytes());
			IvParameterSpec iv = new IvParameterSpec(ivBytes);
			cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
			
			byte[] encrypted = cipher.doFinal(plaintext);
			byte[] ivAndEncrypted = Utility.concatByteArray(ivBytes, encrypted);
			return ivAndEncrypted;
		} catch (Exception e) {
			throw new Exception("Problem with encryption algorithm.", e);
		}
	}

	/**
	 * @brief Decrypt and unmac to string
	 * 
	 * Separates the IV, decrypts and unmacs some encrypted data back to the
	 * original unencrypted string.
	 * 
	 * @param      encrypted            The encrypted data
	 *
	 * @return     A string representation of the decrypted data
	 *
	 * @throws     BadPaddingException  If the data was corrupted
	 * @throws     Exception            If unknown options were given to the decryption or unmaccing algorithm.
	 */
	public String tryDecryptString(byte[] encrypted) throws BadPaddingException, Exception {
		byte[] decrypted = tryDecrypt(encrypted);
		return new String(decrypted, StandardCharsets.UTF_8);
	}

	/**
	 * @brief Decrypt and unmac a byte array
	 * 
	 * Separates the IV, decrypts and unmacs some encrypted data.
	 *
	 * @param      encrypted            The encrypted data
	 *
	 * @return     The decrypted data as a byte array.
	 *
	 * @throws     BadPaddingException  If the data was corrupted
	 * @throws     Exception            If unknown options were given to the decryption or unmaccing algorithm.
	 */
	public byte[] tryDecrypt(byte[] encrypted) throws BadPaddingException, Exception {
		byte[] iv = Arrays.copyOfRange(encrypted,0,CipherBuilder.encryptionKeyType.sizeInBytes());
		byte[] cipherText = Arrays.copyOfRange(encrypted,
											   CipherBuilder.encryptionKeyType.sizeInBytes(),
											   encrypted.length);
		try {
			return decrypt(iv, cipherText);
		} catch (InvalidAlgorithmParameterException | InvalidKeyException |
				IllegalBlockSizeException e) {
			throw new Exception("Problem decrypting. Data corrupted.", e);
		}
	}

	/**
	 * @brief Decrypt and unmac a byte array
	 *
	 * @param      iv                                  The extracted IV
	 * @param      ciphertext                          The ciphertext
	 *
	 * @return     The decrypted data
	 *
	 * @throws     InvalidAlgorithmParameterException  If unknown options were given to the algorithms
	 * @throws     InvalidKeyException                 If the used key is invalid for the algorithm
	 * @throws     IllegalBlockSizeException           If the data was corrupted
	 * @throws     BadPaddingException                 If the data was corrupted
	 * @throws     Exception                           If the data was corrupted
	 */
	private byte[] decrypt(byte[] iv, byte[] ciphertext)
	throws InvalidAlgorithmParameterException, InvalidKeyException,
	IllegalBlockSizeException, BadPaddingException, Exception {
		IvParameterSpec IV = new IvParameterSpec(iv);
		cipher.init(Cipher.DECRYPT_MODE, secretKey, IV);
		byte[] decryptedBytes = cipher.doFinal(ciphertext);
		if (hmac != null)
			decryptedBytes = hmac.unmac(decryptedBytes);
		return decryptedBytes;
	}
}

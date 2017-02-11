package Password_Manager.Encryption;
import Password_Manager.EncodedFileReader;
import Password_Manager.ByteArray;

import javax.crypto.*;
import javax.crypto.spec.*;
import java.security.*;
import java.io.*;
import java.util.*;

/**
 * @brief Class to create Cipher objects.
 * 
 * Cipher objects are what perform the encryption of data.
 * 
 */
public class CipherBuilder {
	private static final String cipherInitString = "AES/CBC/PKCS5Padding"; ///< String representing the type of cipher
	private static final int pbeIterations = 1000; ///< Iterations of PBEKeySpec
	public static final KeyTypes encryptionKeyType = KeyTypes.AES128; /// Type of encryption key (AES-128)

	/**
	 * @brief Build cipher from master and mac keys
	 * 
	 * Creates a cipher that uses ```keyBytes``` to encrypt and
	 * ```macBytes``` to mac data.
	 *
	 * @param      keyBytes   The master key as a byte array
	 * @param      macBytes   The mac key as a byte array
	 *
	 * @return     the newly created cipher
	 *
	 * @throws     Exception  If an error occurred creating the ciphers.
	 */
	public static StringCipher build(byte[] keyBytes, byte[] macBytes) throws Exception {
		Cipher cipher = createCipher();
		SecretKey secretKey = new SecretKeySpec(keyBytes, 0, keyBytes.length,
												encryptionKeyType.getType());
		Hmac hmac = new Hmac(macBytes);
		return new StringCipherImpl(cipher, secretKey, hmac);
	}

	/**
	 * @brief Build cipher from salt filename and password
	 * 
	 * Read the salt from ```saltFilename``` and create a cipher using
	 * the read salt and the password.
	 *
	 * @param      saltFilename  The name of the salt file
	 * @param      password      The password
	 *
	 * @return     The generated cipher
	 *
	 * @throws     Exception     If there was an error reading from the salt file or creating the cipher.
	 */
	public static StringCipher build(String saltFilename, String password) throws Exception {
		byte[] salt = readSaltFromFile(saltFilename);
		return build(password, salt);
	}

	/**
	 * @brief Build cipher from salt and password
	 * 
	 * Builds a cipher using a key generated from the ```salt``` byte array
	 * and a password.
	 *
	 * @param      password   The password
	 * @param      salt       The salt
	 *
	 * @return     { description_of_the_return_value }
	 *
	 * @throws     Exception  { exception_description }
	 */
	public static StringCipher build(String password, byte[] salt) throws Exception {
		Cipher cipher = createCipher();
		SecretKey secretKey = keyFromPasswordAndSalt(password, salt);
		return new StringCipherImpl(cipher, secretKey);
	}

	/**
	 * @brief Creates a Cipher
	 * 
	 * Creates an object of type javax.crypto.Cipher using ```cipherInitString```
	 * to specify the encryption algorithm. This is what the StringCipher derived
	 * classes wrap around.
	 *
	 * @return     A javax.crypto.Cipher object
	 *
	 * @throws     Exception  If the ```cipherInitString``` is invalid.
	 */
	private static Cipher createCipher() throws Exception {
		try {
			return Cipher.getInstance(cipherInitString);
		} catch (Exception e) {
			throw new Exception("Unknown options for encryption algorithm.", e);
		}
	}

	/**
	 * @brief Reads a salt from file.
	 *
	 * @param      saltFilename  The salt filename
	 *
	 * @return     The salt as a byte array
	 *
	 * @throws     Exception     If an error occurred when trying to read the salt file.
	 */
	private static byte[] readSaltFromFile(String saltFilename) throws Exception {
		try {
			EncodedFileReader fileReader = new EncodedFileReader(saltFilename);
			ArrayList<ByteArray> data = fileReader.readData();
			fileReader.close();

			return data.get(0).getRawBytes();
		} catch (FileNotFoundException e) {
			throw new Exception("Can't find salt file.");
		} catch (Exception e) {
			throw new Exception("Problem reading salt file.", e);
		}
	}

	/**
	 * @brief Create an encryption key from password and salt
	 * 
	 * Create an encryption key from the password and the salt using the
	 * PBKDF2 algorithm.
	 *
	 * @param      password   The user's password
	 * @param      salt       The salt as a byte array
	 *
	 * @return     The generated key
	 *
	 * @throws     Exception  If an error occurs when creating the key
	 */
	private static SecretKey keyFromPasswordAndSalt(String password, byte[] salt)
	throws Exception {
		try {
			char[] chars = password.toCharArray();
			PBEKeySpec spec = new PBEKeySpec(chars, salt, pbeIterations, encryptionKeyType.sizeInBits());
			SecretKeyFactory skf = SecretKeyFactory.getInstance(KeyTypes.PBKD_HMACSHA1.getType());
			byte[] secretKeyBytes = skf.generateSecret(spec).getEncoded();
			return new SecretKeySpec(secretKeyBytes, 0, secretKeyBytes.length, encryptionKeyType.getType());
		} catch (Exception e) {
			throw new Exception("Problem creating key from password and salt.", e);
		}
	}

	/**
	 * @brief generates a key of type ```keyType```
	 *
	 * @param      keyType    The key type
	 *
	 * @return     The generated key
	 *
	 * @throws     Exception  If an error occurred when generating the key
	 */
	public static byte[] generateKey(KeyTypes keyType) throws Exception {
		try {
			KeyGenerator keyGenerator = KeyGenerator.getInstance(keyType.getType());
			keyGenerator.init(keyType.sizeInBits());
			return keyGenerator.generateKey().getEncoded();
		} catch (NoSuchAlgorithmException e) {
			throw new Exception("Unknown algorithm for key generation.", e);
		}
	}

	/**
	 * @brief generate random data
	 * 
	 * Generate ```size``` bytes of random data.
	 *
	 * @param      size  The number of bytes of data to generate
	 *
	 * @return     The generated random data.
	 */
	public static byte[] randomData(int size) {
		byte[] generated = new byte[size];
		SecureRandom prng = new SecureRandom();
		prng.nextBytes(generated);
		return generated;
	}
}

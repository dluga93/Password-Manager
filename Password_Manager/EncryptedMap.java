package Password_Manager;

import java.io.*;
import java.util.*;
import javax.crypto.*;
import java.nio.file.*;
import Password_Manager.Encryption.StringCipher;
import Password_Manager.Encryption.CipherBuilder;
import Password_Manager.Encryption.Hmac;

/**
 * @brief Structure that keeps password entries in memory
 * 
 * A structure based on a HashMap, that stores the password entries
 * in memory, and handles communication with files in disk.
 */
public class EncryptedMap {
	private static final int macKeyIndexInFile = 0; ///< Index of the mac key in the file on disk
	private static final int masterKeyIndexInFile = 1; ///< Index of the master key in the file on disk
	private StringCipher cipher; ///< cipher used to encrypt/decrypt passwords
	private HashMap<String, String> passwordMap; ///< Map storing (website,password) as (key,value) pairs
	private final String user; ///< Username

	/**
	 * Constructs an EncryptedMap object for ```user``` with ```password```.
	 *
	 * @param      user      The username
	 * @param      password  The user's password
	 * 
	 * @throws     Exception If the structure can't be created because
	 * the user doesn't exist, the password is incorrect, or some of the files
	 * were corrupted.
	 */
	public EncryptedMap(String user, String password) throws Exception {
		this.user = user;
		passwordMap = new HashMap<String, String>();

		int masterKeySize = CipherBuilder.encryptionKeyType.sizeInBytes();
		ByteArray masterKey = new ByteArray(masterKeySize);

		int macKeySize = Hmac.keyType.sizeInBytes();
		ByteArray macKey = new ByteArray(macKeySize);

		getKeys(password, masterKey, macKey);

		cipher = CipherBuilder.build(masterKey.getRawBytes(), macKey.getRawBytes());
		tryGetPasswords(cipher);
	}

	/**
	 * @brief Gets the master and mac keys
	 * 
	 * Read the master and mac keys from the file, decrypt them,
	 * check their integrity is intact by unmaccing, and return them.
	 *
	 * @param      password   The of the user password
	 * @param      masterKey  The object in which the master key will be stored
	 * @param      macKey     The object in which the mac key will be stored
	 *
	 * @throws     Exception  If the files were corrupted or the password is incorrect.
	 */
	private void getKeys(String password, ByteArray masterKey, ByteArray macKey)
	throws Exception {
		tryReadKeys(password, masterKey, macKey);

		byte[] decryptedMasterKey =
			decryptKey(password, Naming.masterSaltFilename(user), masterKey);
		masterKey.setRawBytes(decryptedMasterKey);

		byte[] decryptedMacKey =
			decryptKey(password, Naming.macSaltFilename(user), macKey);
		macKey.setRawBytes(decryptedMacKey);

		// unmac keys
		macKey.setRawBytes(Hmac.unwrap(macKey.getRawBytes()));
		Hmac hmac = new Hmac(macKey.getRawBytes());
		masterKey.setRawBytes(hmac.unmac(masterKey.getRawBytes()));
	}

	/**
	 * @brief Decrypt an encrypted key
	 * 
	 * Decrypts stored key data using the user's password, the salt that
	 * was used to encrypt it, and the actual encrypted data.
	 *
	 * @param      password      The user's password
	 * @param      saltFilename  The name of the salt file
	 * @param      encryptedKey  The encrypted key data
	 *
	 * @return     the decrypted (but macced) key
	 *
	 * @throws     Exception     If one of the files was corrupted or the
	 * password is incorrect.
	 */
	private byte[] decryptKey(String password, String saltFilename,
		ByteArray encryptedKey) throws Exception {
		StringCipher keyDecrypter = CipherBuilder.build(saltFilename, password);
		return keyDecrypter.tryDecrypt(encryptedKey.getRawBytes());
	}

	/**
	 * @brief Exception filter for readKeys
	 *
	 * Handles the exceptions that the readKeys method might throw.
	 *
	 * @param      password   The user's password
	 * @param      masterKey  The object where the encrypted master key will be stored
	 * @param      macKey     The object where the encrypted mac key will be stored
	 *
	 * @throws     Exception  If a file was corrupted, or an error occurred while reading
	 * from a file.
	 */
	private void tryReadKeys(String password, ByteArray masterKey, ByteArray macKey) 
	throws Exception {
		String keyFilename = Naming.keyFileName(user);
		try {
			readKeys(password, masterKey, macKey);
		} catch (FileNotFoundException e) {
			throw new Exception("Key file " + keyFilename +
				" not found.");
		} catch (EOFException e) {
			throw new Exception("End of key file " + keyFilename +
				" reached prematurely. The file might be corrupted.");
		} catch (IOException e) {
			throw new Exception("Could not read from file " + keyFilename + ".");
		} catch (Hmac.IntegrityException e) {
			throw new Exception("Wrong password or key file " + keyFilename +
				" is corrupted.");
		}
	}

	/**
	 * @brief Read keys from file.
	 * 
	 * Reads the master and mac keys from file and returns them
	 * encrypted.
	 *
	 * @param      password               The user's password
	 * @param      masterKey              The object where the encrypted master key will be stored
	 * @param      macKey                 The object where the encrypted mac key will be stored
	 *
	 * @throws     FileNotFoundException  If the file key file can't be found
	 * @throws     EOFException           If the key file was corrupted
	 * @throws     IOException            If an error occurred when reading from file
	 * @throws     IntegrityException                   If the file was corrupted
	 */
	private void readKeys(String password, ByteArray masterKey, ByteArray macKey)
	throws FileNotFoundException, EOFException, IOException,
	Hmac.IntegrityException, Exception {
	    String keyFilename = Naming.keyFileName(user);
	    EncodedFileReader fileReader = new EncodedFileReader(keyFilename);

	    ArrayList<ByteArray> encryptedKeys = fileReader.readData();

	    fileReader.close();

	    macKey.setRawBytes(encryptedKeys.get(macKeyIndexInFile).getRawBytes());
	    masterKey.setRawBytes(encryptedKeys.get(masterKeyIndexInFile).getRawBytes());
	}

	/**
	 * @brief Exception filter for readPasswords method.
	 * 
	 * Handles exceptions thrown by the readPasswords method.
	 *
	 * @param      cipher     The cipher to decrypt the passwords
	 *
	 * @throws     Exception  If a file was corrupted or an error occurred when
	 * reading from a file.
	 */
	private void tryGetPasswords(StringCipher cipher) throws Exception {
		try {
			getPasswords(cipher);
		} catch (FileNotFoundException e) {
			throw new Exception("Can't find password directory/file.", e);
		} catch (IOException e) {	// also handles EOFException
			throw new Exception("Problem reading password files.", e);
		} catch (BadPaddingException e) {
			throw new Exception("Password file corrupted.", e);
		}
	}

	/**
	 * @brief Read passwords from file
	 * 
	 * Reads and returns all decrypted passwords in the user's
	 * directory.
	 *
	 * @param      cipher                 The cipher used to decrypt the password files
	 *
	 * @throws     FileNotFoundException  If a file or directory was not found.
	 * @throws     IOException            If an error occurred reading from a file.
	 * @throws     Exception              If a file has been corrupted.
	 */
	private void getPasswords(StringCipher cipher)
	throws FileNotFoundException, IOException, Exception {
		ArrayList<String> filenames = EncodedFileReader.getFilenames(user);

		for (String filename : filenames) {
			EncodedFileReader fileReader = new EncodedFileReader(filename);

			ArrayList<ByteArray> data = fileReader.readData();

			fileReader.close();

			String website = cipher.tryDecryptString(data.get(0).getRawBytes());
			String password = cipher.tryDecryptString(data.get(1).getRawBytes());
			passwordMap.put(website, password);
		}
	}

	/**
	 * @brief Changes the master password
	 * 
	 * Changes the master password from ```oldPass```  to ```newPass```. Only the
	 * key file is changed, the password files are not touched.
	 *
	 * @param      oldPass    The old password
	 * @param      newPass    The new password
	 *
	 * @throws     Exception  If an error occured writing to file.
	 */
	public void tryChangeMasterPassword(String oldPass, String newPass) throws Exception {
		ByteArray masterKey = new ByteArray(CipherBuilder.encryptionKeyType.sizeInBytes());
		ByteArray macKey = new ByteArray(Hmac.keyType.sizeInBytes());
		tryReadKeys(oldPass, masterKey, macKey);
		try {
			new Registration(user, newPass, masterKey.getRawBytes(), macKey.getRawBytes());
		} catch (FileAlreadyExistsException e) {
			// supposed to happen because password folder already exists. ignore
		}
	}

	/**
	 * @brief Adds a password entry.
	 *
	 * @param      website                The website for which the password is used
	 * @param      password               The password
	 *
	 * @throws     FileNotFoundException  If the password file can't be created.
	 * @throws     IOException            If an error occurs when writing to file.
	 */
	public void addEntry(String website, String password)
	throws FileNotFoundException, IOException, Exception {
		byte[] rawEncryptedWebsite = cipher.tryEncrypt(website);
		byte[] rawEncryptedPassword = cipher.tryEncrypt(password);
		String pathname = Naming.directoryName(user) + File.separator +
				Naming.entryFilename(user, website);

		ArrayList<ByteArray> encryptedData = new ArrayList<ByteArray>();
		ByteArray encryptedWebsite = new ByteArray(rawEncryptedWebsite);
		ByteArray encryptedPassword = new ByteArray(rawEncryptedPassword);

		encryptedData.add(encryptedWebsite);
		encryptedData.add(encryptedPassword);

		EncodedFileWriter fileWriter = new EncodedFileWriter(pathname);
		fileWriter.writeData(encryptedData);
		fileWriter.close();
		passwordMap.put(website, password);
	}

	/**
	 * @brief Removes a password entry.
	 *
	 * @param      website    The website for which the password is used.
	 *
	 * @throws     Exception  If an error occurs when deleting the password file.
	 */
	public void removeEntry(String website) throws Exception {
		String pathname = Naming.directoryName(user) + File.separator +
				Naming.entryFilename(user, website);
		EncodedFileWriter.deleteFile(pathname);
		passwordMap.remove(website);
	}

	/**
	 * @brief Get the stored websites
	 *
	 * @return     The websites.
	 */
	public Set<String> getWebsites() {
		return passwordMap.keySet();
	}

	/**
	 * @brief Gets the website password.
	 *
	 * @param      website  The website for which we want the password
	 *
	 * @return     The password.
	 */
	public String getWebsitePassword(String website) {
		return passwordMap.get(website);
	}

	/**
	 * @brief Delete a user account
	 * 
	 * Delete all files created by the user.
	 *
	 * @throws     Exception  If an error occurs when deleting a file.
	 */
	public void deleteAccount() throws Exception {
		EncodedFileWriter.deleteFile(Naming.directoryName(user));
		EncodedFileWriter.deleteFile(Naming.masterSaltFilename(user));
		EncodedFileWriter.deleteFile(Naming.macSaltFilename(user));
		EncodedFileWriter.deleteFile(Naming.masterKeyFilename(user));
		EncodedFileWriter.deleteFile(Naming.macKeyFilename(user));
	}
}

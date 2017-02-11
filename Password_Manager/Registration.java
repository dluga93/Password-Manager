package Password_Manager;
import Password_Manager.Encryption.StringCipher;
import Password_Manager.Encryption.CipherBuilder;
import Password_Manager.Encryption.Hmac;
import java.nio.file.*;
import java.io.*;
import java.util.*;

/**
 * @brief Class for registering a new user.
 */
public class Registration {
	private static final int SALT_LENGTH = 32; ///< Length of the salt in bytes
	private byte[] encryptedMasterKey; ///< Encrypted master key, ready to be written to file
	private byte[] encryptedMacKey; ///< Encrypted mac key, ready to be written to file
	private byte[] masterKey; ///< generated master key (unencrypted)
	private byte[] macKey; ///< generated mac key (unencrypted)
	private String user; ///< username

	/**
	 * @brief Registers a new user
	 * 
	 * Register a new user with username ```usr``` and password
	 * ```password```. This generates the master and mac keys, the salts 
	 * for each, and creates the necessary files and directories.
	 *
	 * @param      usr       The username
	 * @param      password  The user's password
	 */
	public Registration(String usr, String password)
	throws FileAlreadyExistsException, Exception {
		user = usr;
		assertValidPassword(password);
		createKeys();
		createPasswordDirectory();
		encryptAndStoreKeys(password);
	}

	/**
	 * @brief Registers a user with preset keys
	 * 
	 * Registers a user with preset master and mac keys. This function is called
	 * when the user wants to change the password, so it uses the existing keys
	 * and just re-encrypts them with a different password and salt.
	 *
	 * @param      usr        The username
	 * @param      password   The new password
	 * @param      masterKey  The master key
	 * @param      macKey     The mac key
	 */
	public Registration(String usr, String password, byte[] masterKey, byte[] macKey)
	throws FileAlreadyExistsException, Exception {
		user = usr;
		this.masterKey = masterKey;
		this.macKey = macKey;

		encryptAndStoreKeys(password);
	}

	/**
	 * @brief Check password validity
	 * 
	 * Checks the validity of ```password```. If the password is
	 * shorter than 8 characters, it throws an exception.
	 *
	 * @param      password   The password to be checked.
	 *
	 * @throws     Exception  If the password is too short.
	 */
	private void assertValidPassword(String password) throws Exception {
		if (password.length() < 8)
			throw new Exception("Your password must be at least 8 characters long.");
	}

	/**
	 * @brief Generate the keys
	 * 
	 * Generates the master key and the mac key that will
	 * be used to encrypt the website password entries.
	 */
	private void createKeys() {
		masterKey = CipherBuilder.generateKey(CipherBuilder.encryptionKeyType);
		macKey = CipherBuilder.generateKey(Hmac.keyType);
	}

	/**
	 * @brief Create password directory
	 * 
	 * Creates a directory based on the username to store the
	 * password entries of the user.
	 *
	 * @throws     FileAlreadyExistsException  If a directory with the same name already exists.
	 * @throws     Exception                   If an IO error occurred when creating the directory.
	 */
	private void createPasswordDirectory() throws FileAlreadyExistsException, Exception {
		String directoryName = Naming.directoryName(user);
		try {
			Files.createDirectory(Paths.get(directoryName));
		} catch (FileAlreadyExistsException e) {
			throw e;
		} catch (IOException e) {
			throw new Exception("Couldn't create password directory.", e);
		}
	}

	/**
	 * @brief Encrypts and stores the keys
	 * 
	 * This function encrypts the master and mac keys with a master
	 * password derived key and a randomly generated salt, and stores
	 * them to a file generated based on the username.
	 *
	 * @param      password   The master password
	 *
	 * @throws     Exception  If an error occurred writing storing the keys.
	 */
	private void encryptAndStoreKeys(String password) throws Exception {
		byte[] masterKeySalt = CipherBuilder.randomData(SALT_LENGTH);
		byte[] macKeySalt = CipherBuilder.randomData(SALT_LENGTH);

		Hmac hmac = new Hmac(macKey);
		masterKey = hmac.mac(masterKey);
		macKey = hmac.mac(macKey);

		StringCipher masterKeyCipher = CipherBuilder.build(password, masterKeySalt);
		encryptedMasterKey = masterKeyCipher.tryEncrypt(masterKey);
		StringCipher macKeyCipher = CipherBuilder.build(password, macKeySalt);
		encryptedMacKey = macKeyCipher.tryEncrypt(macKey);

		tryCreateFiles(masterKeySalt, macKeySalt);
	}

	/**
	 * @brief Exception filter for createFiles
	 * 
	 * Calls createFiles and handles the exceptions that it might throw.
	 *
	 * @param      masterKeySalt               The master key salt
	 * @param      macKeySalt                  The mac key salt
	 *
	 * @throws     FileAlreadyExistsException  If the salt or key files already exist.
	 */
	private void tryCreateFiles(byte[] masterKeySalt, byte[] macKeySalt)
	throws FileAlreadyExistsException {
		try {
			createFiles(masterKeySalt, macKeySalt);
		} catch (FileAlreadyExistsException e) {
			throw e;
		} catch (Exception e) {
			throw new Exception("Can't create files for signing up.", e);
		}
	}

	/**
	 * @brief Creates the salt and key files
	 * 
	 * Create files and store the two salts (unencrypted) and the
	 * two keys (macced and encrypted).
	 *
	 * @param      masterKeySalt               The master key salt
	 * @param      macKeySalt                  The mac key salt
	 *
	 * @throws     FileAlreadyExistsException  If one of the files already exists
	 * @throws     Exception                   If an error occurs when writing to file
	 */
	private void createFiles(byte[] masterKeySalt, byte[] macKeySalt)
	throws FileAlreadyExistsException, Exception {
		saveDataToFile(masterKeySalt, Naming.masterSaltFilename(user));
		saveDataToFile(macKeySalt, Naming.macSaltFilename(user));

		EncodedFileWriter fileWriter = new EncodedFileWriter(Naming.keyFileName(user));
		ArrayList<ByteArray> keys = new ArrayList<ByteArray>();
		keys.add(new ByteArray(encryptedMacKey));
		keys.add(new ByteArray(encryptedMasterKey));
		fileWriter.writeData(keys);
		fileWriter.close();
	}

	/**
	 * @brief Write a byte array to a file
	 *
	 * Writes the byte array ```data``` to the file ```filename```.
	 *
	 * @param      data       The data to write to file
	 * @param      filename   The filename
	 *
	 * @throws     Exception  If an error occurred when writing to the file.
	 */
	private void saveDataToFile(byte[] data, String filename) throws Exception {
		try {
			EncodedFileWriter fileWriter = new EncodedFileWriter(filename);

			ArrayList<ByteArray> dataToWrite = new ArrayList<ByteArray>();
			dataToWrite.add(new ByteArray(data));
			fileWriter.writeData(dataToWrite);

			fileWriter.close();
		} catch (IOException e) {
			throw new Exception("Couldn't write to " + filename + " file.", e);
		}
	}
}

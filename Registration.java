package PwdManager;
import PwdManager.Encryption.StringCipher;
import PwdManager.Encryption.CipherBuilder;
import java.nio.file.*;
import java.io.*;

public class Registration {
	private static String user;

	public static void registerUser(String usr, String password)
	throws Exception {
		user = usr;
		byte[] salt = CipherBuilder.randomData(CipherBuilder.keySizeInBits/8);
		byte[] masterKey = createEncryptedMasterKey(password, salt);
		tryCreateFiles(salt, masterKey);
	}

	// register with a preset master key
	public static void registerUser(String usr, String password, byte[] masterKey)
	throws Exception {
		user = usr;
		byte[] salt = CipherBuilder.randomData(CipherBuilder.keySizeInBits/8);
		StringCipher cipher = tryCreateCipher(password, salt);
		byte[] encryptedKey = cipher.tryEncrypt(masterKey);
		tryCreateFiles(salt, encryptedKey);
	}

	private static byte[] createEncryptedMasterKey(String password, byte[] salt) {
		byte[] key = CipherBuilder.generateKey();
		StringCipher cipher = tryCreateCipher(password, salt);
		byte[] encryptedKey = cipher.tryEncrypt(key);
		return encryptedKey;
	}

	private static StringCipher tryCreateCipher(String password, byte[] salt) {
		return CipherBuilder.build(password, salt);
	}

	private static void tryCreateFiles(byte[] salt, byte[] masterKey) {
		try {
			createFiles(salt, masterKey);
		} catch (Exception e) {
			Logger.logException("Can't create files for signing up.", e);
			System.exit(1);
		}
	}

	private static void createFiles(byte[] salt, byte[] masterKey) throws Exception {
		saveDataToFile(salt, user + "_salt");
		saveDataToFile(masterKey, user + "_key");
		createPasswordDirectory();
	}

	private static void saveDataToFile(byte[] data, String filename) throws Exception {
		try {
			EncodedFileWriter fileWriter = new EncodedFileWriter(filename);
			fileWriter.writeData(data);
			fileWriter.close();
		} catch (IOException e) {
			throw new Exception("Couldn't create " + filename + " file.", e);
		}
	}

	// TODO: BAD! Find a new way to change master password, not just register again
	// with a new password and same master key.
	private static void createPasswordDirectory() throws Exception {
		String directoryName = user + "_dir";
		try {
			Files.createDirectory(Paths.get(directoryName));
		} catch (FileAlreadyExistsException e) {
		} catch (IOException e) {
			throw new Exception("Couldn't create password directory.", e);
		}
	}
}
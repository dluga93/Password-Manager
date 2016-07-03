package PwdManager;
import PwdManager.Encryption.StringCipher;
import PwdManager.Encryption.CipherBuilder;
import java.nio.file.*;
import java.io.*;

public class Registration {
	private static final String masterKeyFileSuffix = "_key";
	private static final String macKeyFileSuffix = "_mackey";
	private static final String saltFileSuffix = "_salt";
	private static final String directorySuffix = "_dir";
	private static byte[] encryptedMasterKey;
	private static byte[] encryptedMacKey;
	private static String user;

	public static void registerUser(String usr, String password)
	throws Exception {
		user = usr;
		byte[] salt = CipherBuilder.randomData(CipherBuilder.keySizeInBits/8);
		createKeys(password, salt);
		tryCreateFiles(salt);
	}

	// register with a preset master and mac key
	public static void registerUser(String usr, String password, byte[] masterKey, byte[] macKey)
	throws Exception {
		user = usr;
		byte[] salt = CipherBuilder.randomData(CipherBuilder.keySizeInBits/8);
		StringCipher cipher = tryCreateCipher(password, salt);
		encryptedMasterKey = cipher.tryEncrypt(masterKey);
		encryptedMacKey = cipher.tryEncrypt(macKey);
		tryCreateFiles(salt);
	}

	private static void createKeys(String password, byte[] salt) {
		byte[] masterKey = CipherBuilder.generateKey(CipherBuilder.KeyTypes.AES);
		byte[] macKey = CipherBuilder.generateKey(CipherBuilder.KeyTypes.HMACSHA1);
		StringCipher cipher = tryCreateCipher(password, salt);
		encryptedMasterKey = cipher.tryEncrypt(masterKey);
		encryptedMacKey = cipher.tryEncrypt(macKey);
	}

	private static StringCipher tryCreateCipher(String password, byte[] salt) {
		return CipherBuilder.build(password, salt);
	}

	private static void tryCreateFiles(byte[] salt) {
		try {
			createFiles(salt);
		} catch (Exception e) {
			Logger.logException("Can't create files for signing up.", e);
			System.exit(1);
		}
	}

	private static void createFiles(byte[] salt) throws Exception {
		saveDataToFile(salt, user + saltFileSuffix);
		saveDataToFile(encryptedMasterKey, user + masterKeyFileSuffix);
		saveDataToFile(encryptedMacKey, user + macKeyFileSuffix);
		createPasswordDirectory();
	}

	private static void saveDataToFile(byte[] data, String filename) throws Exception {
		try {
			EncodedFileWriter fileWriter = new EncodedFileWriter(filename);
			fileWriter.writeData(data);
			fileWriter.close();
		} catch (IOException e) {
			throw new Exception("Couldn't write to " + filename + " file.", e);
		}
	}

	// TODO: BAD! Find a new way to change master password, not just register again
	// with a new password and same master key.
	private static void createPasswordDirectory() throws Exception {
		String directoryName = user + directorySuffix;
		try {
			Files.createDirectory(Paths.get(directoryName));
		} catch (FileAlreadyExistsException e) {
		} catch (IOException e) {
			throw new Exception("Couldn't create password directory.", e);
		}
	}

	public static String masterKeyFilename(String user) {
		return user + masterKeyFileSuffix;
	}

	public static String macKeyFilename(String user) {
		return user + macKeyFileSuffix;
	}
}
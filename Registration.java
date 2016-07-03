package PwdManager;
import PwdManager.Encryption.StringCipher;
import PwdManager.Encryption.CipherBuilder.KeyTypes;
import PwdManager.Encryption.CipherBuilder;
import PwdManager.Encryption.Hmac;
import java.nio.file.*;
import java.io.*;

public class Registration {
	private static byte[] encryptedMasterKey;
	private static byte[] encryptedMacKey;
	private static String user;

	public static void registerUser(String usr, String password)
	throws FileAlreadyExistsException, Exception {
		user = usr;
		byte[] salt = CipherBuilder.randomData(KeyTypes.AES.getSizeInBits()/8);
		createKeys(password, salt);
		tryCreateFiles(salt);
	}

	// register with a preset master and mac key
	public static void registerUser(String usr, String password, byte[] masterKey, byte[] macKey)
	throws FileAlreadyExistsException, Exception {
		user = usr;
		byte[] salt = CipherBuilder.randomData(KeyTypes.AES.getSizeInBits()/8);
		StringCipher cipher = CipherBuilder.build(password, salt);
		Hmac hmac = new Hmac(macKey);
		masterKey = hmac.mac(masterKey);
		macKey = hmac.mac(macKey);
		encryptedMasterKey = cipher.tryEncrypt(masterKey);
		encryptedMacKey = cipher.tryEncrypt(macKey);
		tryCreateFiles(salt);
	}

	private static void createKeys(String password, byte[] salt) throws Exception {
		byte[] masterKey = CipherBuilder.generateKey(CipherBuilder.KeyTypes.AES);
		byte[] macKey = CipherBuilder.generateKey(CipherBuilder.KeyTypes.HMACSHA1);

		Hmac hmac = new Hmac(macKey);
		masterKey = hmac.mac(masterKey);
		macKey = hmac.mac(macKey);
		
		StringCipher cipher = CipherBuilder.build(password, salt);
		encryptedMasterKey = cipher.tryEncrypt(masterKey);
		encryptedMacKey = cipher.tryEncrypt(macKey);
	}

	private static void tryCreateFiles(byte[] salt) throws FileAlreadyExistsException {
		try {
			createFiles(salt);
		} catch (FileAlreadyExistsException e) {
			throw e;
		} catch (Exception e) {
			Logger.logException("Can't create files for signing up.", e);
			System.exit(1);
		}
	}

	private static void createFiles(byte[] salt) throws FileAlreadyExistsException, Exception {
		saveDataToFile(salt, Naming.saltFilename(user));
		saveDataToFile(encryptedMasterKey, Naming.masterKeyFilename(user));
		saveDataToFile(encryptedMacKey, Naming.macKeyFilename(user));
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

	private static void createPasswordDirectory() throws FileAlreadyExistsException, Exception {
		String directoryName = Naming.directoryName(user);
		try {
			Files.createDirectory(Paths.get(directoryName));
		} catch (FileAlreadyExistsException e) {
			throw e;
		} catch (IOException e) {
			throw new Exception("Couldn't create password directory.", e);
		}
	}
}
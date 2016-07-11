package PwdManager;
import PwdManager.Encryption.StringCipher;
import PwdManager.Encryption.CipherBuilder;
import PwdManager.Encryption.Hmac;
import java.nio.file.*;
import java.io.*;

public class Registration {
	private static final int SALT_LENGTH = 32; // bytes
	private byte[] encryptedMasterKey;
	private byte[] encryptedMacKey;
	private byte[] masterKey;
	private byte[] macKey;
	private String user;

	public Registration(String usr, String password)
	throws FileAlreadyExistsException, Exception {
		user = usr;
		assertValidPassword(password);
		createKeys();
		createPasswordDirectory();
		encryptAndStoreKeys(password);
	}

	// register with a preset master and mac key
	public Registration(String usr, String password, byte[] masterKey, byte[] macKey)
	throws FileAlreadyExistsException, Exception {
		user = usr;
		this.masterKey = masterKey;
		this.macKey = macKey;

		encryptAndStoreKeys(password);
	}

	private void assertValidPassword(String password) throws Exception {
		if (password.length() < 8)
			throw new Exception("Your password must be at least 8 characters long.");
	}

	private void createKeys() {
		masterKey = CipherBuilder.generateKey(CipherBuilder.encryptionKeyType);
		macKey = CipherBuilder.generateKey(Hmac.keyType);
	}

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

	private void tryCreateFiles(byte[] masterKeySalt, byte[] macKeySalt)
	throws FileAlreadyExistsException {
		try {
			createFiles(masterKeySalt, macKeySalt);
		} catch (FileAlreadyExistsException e) {
			throw e;
		} catch (Exception e) {
			Logger.logException("Can't create files for signing up.", e);
			System.exit(1);
		}
	}

	private void createFiles(byte[] masterKeySalt, byte[] macKeySalt)
	throws FileAlreadyExistsException, Exception {
		saveDataToFile(masterKeySalt, Naming.masterSaltFilename(user));
		saveDataToFile(macKeySalt, Naming.macSaltFilename(user));
		saveDataToFile(encryptedMasterKey, Naming.masterKeyFilename(user));
		saveDataToFile(encryptedMacKey, Naming.macKeyFilename(user));
	}

	private void saveDataToFile(byte[] data, String filename) throws Exception {
		try {
			EncodedFileWriter fileWriter = new EncodedFileWriter(filename);
			fileWriter.writeData(data);
			fileWriter.close();
		} catch (IOException e) {
			throw new Exception("Couldn't write to " + filename + " file.", e);
		}
	}

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
}
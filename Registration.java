package PwdManager;
import PwdManager.Encryption.StringCipher;
import PwdManager.Encryption.CipherBuilder;
import java.nio.file.*;

public class Registration {
	private static String user;

	public static void registerUser(String user_, String password) {
		user = user_;
		byte[] salt = CipherBuilder.randomData(CipherBuilder.keySizeInBits/8);
		byte[] masterKey = createEncryptedMasterKey(password, salt);
		tryCreateFiles(salt, masterKey);
	}

	private static byte[] createEncryptedMasterKey(String password, byte[] salt) {
		byte[] key = CipherBuilder.generateKey();
		StringCipher cipher = tryCreateCipher(password, salt);
		byte[] encryptedKey = cipher.tryEncrypt(key);
		return encryptedKey;
	}

	private static StringCipher tryCreateCipher(String password, byte[] salt) {
		try {
			return CipherBuilder.build(password, salt);
		} catch (Exception e) {
			Logger.logException("Error creating cipher for encryption.", e);
			System.exit(1);
		}
		return null;
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
		createSaltFile(salt);
		createPasswordDirectory();
		createMasterKeyFile(masterKey);
	}

	private static void createSaltFile(byte[] salt) throws Exception {
		EncodedFileWriter fileWriter = new EncodedFileWriter(user + "_salt");
		fileWriter.writeData(salt);
		fileWriter.close();
	}

	private static void createPasswordDirectory() throws Exception {
		String directoryName = user + "_dir";
		Files.createDirectory(Paths.get(directoryName));
	}

	private static void createMasterKeyFile(byte[] masterKey) throws Exception {
		EncodedFileWriter fileWriter = new EncodedFileWriter(user + "_key");
		fileWriter.writeData(masterKey);
		fileWriter.close();
	}
}
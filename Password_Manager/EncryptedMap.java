package Password_Manager;

import java.io.*;
import java.util.*;
import javax.crypto.*;
import java.nio.file.*;
import Password_Manager.Encryption.StringCipher;
import Password_Manager.Encryption.CipherBuilder;
import Password_Manager.Encryption.Hmac;

public class EncryptedMap {
	private static final int macKeyIndexInFile = 0;
	private static final int masterKeyIndexInFile = 1;
	private StringCipher cipher;
	private HashMap<String, String> passwordMap;
	private EncodedFileReader fileReader;
	private final String user;

	public EncryptedMap(String user, String password) throws Exception {
		this.user = user;
		passwordMap = new HashMap<String, String>();

		int masterKeySize = CipherBuilder.encryptionKeyType.sizeInBytes();
		ByteArray masterKey = new ByteArray(masterKeySize);

		int macKeySize = Hmac.keyType.sizeInBytes();
		ByteArray macKey = new ByteArray(macKeySize);

		getKeys(password, masterKey, macKey);

		cipher = CipherBuilder.build(masterKey.getRawBytes(), macKey.getRawBytes());
		tryReadPasswords(cipher);
	}

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

	private byte[] decryptKey(String password, String saltFilename,
		ByteArray encryptedKey) throws Exception {
		StringCipher keyDecrypter = CipherBuilder.build(saltFilename, password);
		return keyDecrypter.tryDecrypt(encryptedKey.getRawBytes());
	}

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

	private void readKeys(String password, ByteArray masterKey, ByteArray macKey)
	throws FileNotFoundException, EOFException, IOException,
	Hmac.IntegrityException, Exception {
	    String keyFilename = Naming.keyFileName(user);
	    fileReader = new EncodedFileReader(keyFilename);

	    ArrayList<ByteArray> encryptedKeys = fileReader.readData();

	    fileReader.close();

	    macKey.setRawBytes(encryptedKeys.get(macKeyIndexInFile).getRawBytes());
	    masterKey.setRawBytes(encryptedKeys.get(masterKeyIndexInFile).getRawBytes());
	}

	private void tryReadPasswords(StringCipher cipher) throws Exception {
		try {
			readPasswords(cipher);
		} catch (FileNotFoundException e) {
			throw new Exception("Can't find password directory/file.", e);
		} catch (IOException e) {	// also handles EOFException
			throw new Exception("Problem reading password files.", e);
		} catch (BadPaddingException e) {
			throw new Exception("Password file corrupted.", e);
		}
	}

	private void readPasswords(StringCipher cipher)
	throws FileNotFoundException, IOException, EOFException,
	BadPaddingException, Exception {
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

	public void addEntry(String website, String password)
	throws FileNotFoundException, IOException {
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

	public void removeEntry(String website) throws Exception {
		String pathname = Naming.directoryName(user) + File.separator +
				Naming.entryFilename(user, website);
		EncodedFileWriter.deleteFile(pathname);
		passwordMap.remove(website);
	}

	public Set<String> getWebsites() {
		return passwordMap.keySet();
	}

	public String getWebsitePassword(String website) {
		return passwordMap.get(website);
	}

	public void deleteAccount() throws Exception {
		EncodedFileWriter.deleteFile(Naming.directoryName(user));
		EncodedFileWriter.deleteFile(Naming.masterSaltFilename(user));
		EncodedFileWriter.deleteFile(Naming.macSaltFilename(user));
		EncodedFileWriter.deleteFile(Naming.masterKeyFilename(user));
		EncodedFileWriter.deleteFile(Naming.macKeyFilename(user));
	}
}

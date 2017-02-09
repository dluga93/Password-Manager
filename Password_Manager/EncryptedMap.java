package Password_Manager;

import java.io.*;
import java.util.*;
import javax.crypto.*;
import java.nio.file.*;
import Password_Manager.Encryption.StringCipher;
import Password_Manager.Encryption.CipherBuilder;
import Password_Manager.Encryption.Hmac;

public class EncryptedMap {
	private StringCipher cipher;
	private HashMap<String, String> passwordMap;
	private EncodedFileReader fileReader;
	private final String user;

	public EncryptedMap(String user, String password) throws Exception {
		this.user = user;
		passwordMap = new HashMap<String, String>();

		int masterKeySize = CipherBuilder.encryptionKeyType.sizeInBytes();
		byte[] masterKey = new byte[masterKeySize];

		int macKeySize = Hmac.keyType.sizeInBytes();
		byte[] macKey = new byte[macKeySize];

		getKeys(password, masterKey, macKey);

		cipher = CipherBuilder.build(masterKey, macKey);
		tryReadPasswords(cipher);
	}

	private void getKeys(String password, byte[] masterKey, byte[] macKey)
	throws Exception {
		tryReadKeys(password, masterKey, macKey);
	}

	private void decryptKey(String password, String saltFilename,
		byte[] encryptedKey) throws Exception {
		StringCipher keyDecrypter = CipherBuilder.build(saltFilename, password);
		encryptedKey = keyDecrypter.tryDecrypt(encryptedKey);
	}

	private void tryReadKeys(String password, byte[] masterKey, byte[] macKey) 
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

	private void readKeys(String password, byte[] masterKey, byte[] macKey)
	throws Exception {
	    String keyFilename = Naming.keyFileName(user);
	    fileReader = new EncodedFileReader(keyFilename);

	    byte[] encryptedMacKey = fileReader.readData();
	    StringCipher keyDecrypter =
	    	CipherBuilder.build(Naming.macSaltFilename(user), password);
	    byte[] maccedMacKey = keyDecrypter.tryDecrypt(encryptedMacKey);
	    macKey = Hmac.unwrap(maccedMacKey);

	    Hmac hmac = new Hmac(macKey);
	    byte[] encryptedMasterKey = fileReader.readData();
	    keyDecrypter =
	    	CipherBuilder.build(Naming.masterSaltFilename(user), password);
	    byte[] maccedMasterKey = keyDecrypter.tryDecrypt(encryptedMasterKey);
	    masterKey = hmac.unmac(maccedMasterKey);

	    fileReader.close();
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
			String website = cipher.tryDecryptString(fileReader.readData());
			String password = cipher.tryDecryptString(fileReader.readData());
			fileReader.close();
			passwordMap.put(website, password);
		}
	}

	public void tryChangeMasterPassword(String oldPass, String newPass) throws Exception {
		byte[] masterKey = new byte[CipherBuilder.encryptionKeyType.sizeInBytes()];
		byte[] macKey = new byte[Hmac.keyType.sizeInBytes()];
		tryReadKeys(oldPass, masterKey, macKey);
		try {
			new Registration(user, newPass, masterKey, macKey);
		} catch (FileAlreadyExistsException e) {
			// supposed to happen because password folder already exists. ignore
		}
	}

	public void addEntry(String website, String password)
	throws FileNotFoundException, IOException {
		byte[] encryptedWebsite = cipher.tryEncrypt(website);
		byte[] encryptedPassword = cipher.tryEncrypt(password);
		String pathname = Naming.directoryName(user) + File.separator +
				Naming.entryFilename(user, website);

		EncodedFileWriter fileWriter = new EncodedFileWriter(pathname);
		fileWriter.writeData(encryptedWebsite);
		fileWriter.writeData(encryptedPassword);
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

// TODO: correct message for wrong password
// TODO: integrity checking for the saved files.

package PwdManager;

import java.io.*;
import java.util.*;
import Encryption.StringCipher;
import Encryption.CipherBuilder;

public class EncryptedMap {
	private StringCipher cipher;
	private HashMap<String, String> passwordMap;
	private final String user;

	public EncryptedMap(String user, String password) {
		this.user = user;
		passwordMap = new HashMap<String, String>();
		byte[] masterKey = tryGetMasterKey(user, password);
		cipher = CipherBuilder.build(masterKey);
		tryReadPasswords(user, cipher);
	}

	private byte[] tryGetMasterKey(String user, String password) {
		try {
			return getMasterKey(user, password);
		} catch (IOException e) {
			Logger.logError("Problem reading master key.", e);
			System.exit(1);
		} catch (Exception e) {
			Logger.logError("User does not exist.", e);
			System.exit(1);
		}
		return null;
	}

	private byte[] getMasterKey(String user, String password)
	throws FileNotFoundException, IOException, Exception {
		EncodedFileReader fileReader = new EncodedFileReader(user + "_key");
		byte[] encryptedMasterKey = fileReader.readData();
		fileReader.close();

		StringCipher masterKeyDecrypter = CipherBuilder.build(user, password);
		byte[] masterKey = masterKeyDecrypter.tryDecrypt(encryptedMasterKey);
		return masterKey;
	}

	private void tryReadPasswords(String user, StringCipher cipher) {
		try {
			readPasswords(user, cipher);
		} catch (FileNotFoundException e) {
			Logger.logError("Can't find password file.", e);
			System.exit(1);
		} catch (IOException e) {
			Logger.logError("Problem reading password file.", e);
			System.exit(1);
		} catch (EOFException e) {
			return;
		}
	}

	// TODO: fix. Currently, method only terminates with exception throw.
	private void readPasswords(String user, StringCipher cipher)
	throws FileNotFoundException, IOException, EOFException {
		EncodedFileReader fileReader = new EncodedFileReader(user + "_pass");
		
		while (true) {
			String website = cipher.tryDecryptString(fileReader.readData());
			String password = cipher.tryDecryptString(fileReader.readData());
			passwordMap.put(website, password);
		}
	}

	public void addEntry(String website, String password)
	throws FileNotFoundException, IOException {
		byte[] encryptedWebsite = cipher.tryEncrypt(website);
		byte[] encryptedPassword = cipher.tryEncrypt(password);
		
		EncodedFileWriter fileWriter = new EncodedFileWriter(user + "_pass");
		fileWriter.writeData(encryptedWebsite);
		fileWriter.writeData(encryptedPassword);
		fileWriter.close();
		passwordMap.put(website, password);
	}

	public Set<String> getWebsites() {
		return passwordMap.keySet();
	}

	public String getWebsitesPassword(String website) {
		return passwordMap.get(website);
	}
}
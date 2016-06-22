// TODO: correct message for wrong password
// TODO: integrity checking for the saved files.
// TODO: dialog boxes and no abortion for exceptions.

package PwdManager;

import java.io.*;
import java.util.*;
import PwdManager.Encryption.StringCipher;
import PwdManager.Encryption.CipherBuilder;

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
			Logger.logError("Can't find password directory/file.", e);
			System.exit(1);
		} catch (Exception e) {
			Logger.logError("Problem reading password files.", e);
			System.exit(1);
		}
	}

	// TODO: put each (website,password) pair in a separate file.
	private void readPasswords(String user, StringCipher cipher)
	throws FileNotFoundException, Exception {
		ArrayList<String> filenames = getFilenames(user);

		for (String filename : filenames) {
			EncodedFileReader fileReader = new EncodedFileReader(filename);
			String website = cipher.tryDecryptString(fileReader.readData());
			String password = cipher.tryDecryptString(fileReader.readData());
			fileReader.close();
			passwordMap.put(website, password);
		}
	}

	private ArrayList<String> getFilenames(String user)
	throws FileNotFoundException, Exception {
		File[] passwordFiles = new File(user + "_dir").listFiles();
		ArrayList<String> filenames = new ArrayList<String>();
		for (File file : passwordFiles) {
			String filename = user + "_dir" + File.separator + file.getName();
			filenames.add(filename);
		}
		return filenames;
	}

	public void addEntry(String website, String password)
	throws FileNotFoundException, IOException {
		byte[] encryptedWebsite = cipher.tryEncrypt(website);
		byte[] encryptedPassword = cipher.tryEncrypt(password);
		String pathname = user + "_dir" + File.separator + user + "_" + website;

		EncodedFileWriter fileWriter = new EncodedFileWriter(pathname);
		fileWriter.writeData(encryptedWebsite);
		fileWriter.writeData(encryptedPassword);
		fileWriter.close();
		passwordMap.put(website, password);
	}

	public void removeEntry(String website) {
		String pathname = user + "_dir" + File.separator + user + "_" + website;
		EncodedFileWriter.deleteFile(pathname);
		passwordMap.remove(website);
	}

	public Set<String> getWebsites() {
		return passwordMap.keySet();
	}

	public String getWebsitePassword(String website) {
		return passwordMap.get(website);
	}
}
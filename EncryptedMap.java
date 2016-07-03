// TODO: correct message for wrong password
// TODO: integrity checking for the saved files.

package PwdManager;

import java.io.*;
import java.util.*;
import PwdManager.Encryption.StringCipher;
import PwdManager.Encryption.CipherBuilder;

public class EncryptedMap {
	private StringCipher cipher;
	private HashMap<String, String> passwordMap;
	private final String user;

	public EncryptedMap(String user, String password) throws Exception {
		this.user = user;
		passwordMap = new HashMap<String, String>();
		byte[] masterKey = tryReadKey(password, Registration.masterKeyFilename(user));
		cipher = CipherBuilder.build(masterKey);
		tryReadPasswords(cipher);
	}

	private byte[] tryReadKey(String password, String filename) throws Exception {
		try {
			return readKey(password, filename);
		} catch (IOException e) {
			throw new Exception("Problem reading master key.", e);
		} catch (Exception e) {
			throw new Exception("User does not exist.", e);
		}
	}

	private byte[] readKey(String password, String filename)
	throws FileNotFoundException, IOException, Exception {
		EncodedFileReader fileReader = new EncodedFileReader(filename);
		byte[] encryptedKey = fileReader.readData();
		fileReader.close();

		StringCipher keyDecrypter = CipherBuilder.build(user, password);
		byte[] key = keyDecrypter.tryDecrypt(encryptedKey);
		return key;
	}

	private void tryReadPasswords(StringCipher cipher) throws Exception {
		try {
			readPasswords(cipher);
		} catch (FileNotFoundException e) {
			throw new Exception("Can't find password directory/file.", e);
		} catch (IOException e) {	// also handles EOFException
			throw new Exception("Problem reading password files.", e);
		}
	}

	private void readPasswords(StringCipher cipher)
	throws FileNotFoundException, IOException, EOFException, Exception {
		ArrayList<String> filenames = getFilenames(user);

		for (String filename : filenames) {
			EncodedFileReader fileReader = new EncodedFileReader(filename);
			String website = cipher.tryDecryptString(fileReader.readData());
			String password = cipher.tryDecryptString(fileReader.readData());
			fileReader.close();
			passwordMap.put(website, password);
		}
	}

	public void tryChangeMasterPassword(String oldPass, String newPass) throws Exception {
		byte[] masterKey = tryReadKey(oldPass, Registration.masterKeyFilename(user));
		byte[] macKey = tryReadKey(oldPass, Registration.macKeyFilename(user));
		Registration.registerUser(user, newPass, masterKey, macKey);
	}

	private ArrayList<String> getFilenames(String user)
	throws Exception {
		File directory = new File(user + "_dir");
		if (!directory.exists())
			throw new Exception("Password directory not found.");

		File[] passwordFiles = directory.listFiles();
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

	public void removeEntry(String website) throws Exception {
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

	public void deleteAccount() throws Exception {
		EncodedFileWriter.deleteFile(user + "_dir");
	}
}
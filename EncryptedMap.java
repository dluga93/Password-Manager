// TODO: correct message for wrong password

package PwdManager;

import java.io.*;
import java.util.*;
import javax.crypto.*;
import PwdManager.Encryption.StringCipher;
import PwdManager.Encryption.CipherBuilder;
import PwdManager.Encryption.Hmac;

public class EncryptedMap {
	private StringCipher cipher;
	private HashMap<String, String> passwordMap;
	private final String user;

	public EncryptedMap(String user, String password) throws Hmac.IntegrityException, Exception {
		this.user = user;
		passwordMap = new HashMap<String, String>();

		byte[] maccedMacKey = tryReadKey(password, Naming.macKeyFilename(user));
		byte[] macKey = Hmac.unwrap(maccedMacKey);
		
		Hmac hmac = new Hmac(macKey);
		byte[] maccedMasterKey = tryReadKey(password, Naming.masterKeyFilename(user));
		byte[] masterKey = hmac.unmac(maccedMasterKey);
		
		cipher = CipherBuilder.build(masterKey, macKey);
		tryReadPasswords(cipher);
	}

	private byte[] tryReadKey(String password, String filename) throws Exception {
		try {
			return readKey(password, filename);
		} catch (IOException e) {
			throw new Exception("User does not exist or key file corrupted.", e);
		} catch (BadPaddingException e) {
			throw new Exception("Wrong password or corrupted files.", e);
		}
	}

	private byte[] readKey(String password, String filename)
	throws FileNotFoundException, IOException, BadPaddingException, Exception {
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
		byte[] masterKey = tryReadKey(oldPass, Naming.masterKeyFilename(user));
		byte[] macKey = tryReadKey(oldPass, Naming.macKeyFilename(user));
		Registration.registerUser(user, newPass, masterKey, macKey);
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
	}
}
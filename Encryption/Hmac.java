package PwdManager.Encryption;

import javax.crypto.*;
import javax.crypto.spec.*;
import java.util.*;
import PwdManager.Logger;
import PwdManager.Encryption.CipherBuilder.KeyTypes;

public class Hmac {
	private final byte[] key;
	private final static KeyTypes keyType = KeyTypes.HMACSHA1;
	private final static int HASH_SIZE_IN_BYTES = 20;

	@SuppressWarnings("serial")
	public class IntegrityException extends Exception {
		public IntegrityException() {
			super();
		}

		public IntegrityException(String message) {
			super(message);
		}

		public IntegrityException(String message, Throwable cause) {
			super(message, cause);
		}
	}

	public Hmac(byte[] key) throws Exception {
		if (key.length != keyType.getSizeInBits()/8)
			throw new Exception("Wrong Key Size. " + keyType.getSizeInBits()
								+ " bits expected.");

		this.key = key;
	}

	public byte[] mac(byte[] message) {
		byte[] mac = getMac(message);
		return Utility.concatByteArray(message, mac);
	}

	public byte[] getMac(byte[] message) {
		try {
			Mac hmac = Mac.getInstance(keyType.getType());
			hmac.init(new SecretKeySpec(key, keyType.getType()));
			return hmac.doFinal(message);
		} catch (Exception e) {
			Logger.logException("Invalid parameters for MAC algorithm.", e);
			System.exit(1);
			return null;
		}
	}

	public byte[] unmac(byte[] maccedMessage) throws Exception {
		if (maccedMessage.length <= HASH_SIZE_IN_BYTES)
			throw new Exception("Corrupted data. Invalid length.");

		byte[] message = new byte[maccedMessage.length - HASH_SIZE_IN_BYTES];
		System.arraycopy(maccedMessage, 0, message, 0, message.length);

		byte[] mac = new byte[HASH_SIZE_IN_BYTES];
		System.arraycopy(maccedMessage, message.length, mac, 0, mac.length);

		if (!isMacCorrect(message, mac))
			throw new Exception("Corrupted data. Incorrect MAC.");

		return message;
	}

	private boolean isMacCorrect(byte[] message, byte[] actualMac) {
		byte[] exptectedMac = getMac(message);
		if (Arrays.equals(exptectedMac, actualMac))
			return true;
		return false;
	}

	public static byte[] unwrap(byte[] maccedKey) throws IntegrityException, Exception {
		byte[] key = new byte[maccedKey.length - HASH_SIZE_IN_BYTES];
		System.arraycopy(maccedKey, 0, key, 0, key.length);
		byte[] mac = new byte[HASH_SIZE_IN_BYTES];
		System.arraycopy(maccedKey, key.length, mac, 0, mac.length);
		Hmac hmac = new Hmac(key);
		if (hmac.isMacCorrect(key, mac))
			return key;
		else
			throw hmac.new IntegrityException();
	}
}
package PwdManager.Encryption;

import javax.crypto.*;
import javax.crypto.spec.*;
import PwdManager.Logger;

public class Hmac {
	private final byte[] key;
	private final static String HMAC_ALGORITHM = "HmacSHA1";
	public final static int KEY_SIZE_IN_BITS = 128;

	public Hmac(byte[] key) throws Exception {
		if (key.length != KEY_SIZE_IN_BITS/8)
			throw new Exception("Wrong Key Size. " + KEY_SIZE_IN_BITS + " bits expected.");

		this.key = key;
	}

	public byte[] getMac(byte[] message) {
		try {
			Mac hmac = Mac.getInstance(HMAC_ALGORITHM);
			hmac.init(new SecretKeySpec(key, HMAC_ALGORITHM));
			return hmac.doFinal(message);
		} catch (Exception e) {
			Logger.logException("Invalid parameters for MAC algorithm.", e);
			System.exit(1);
			return null;
		}
	}

	public byte[] mac(byte[] message) {
		byte[] mac = getMac(message);
		return Utility.concatByteArray(message, mac);
	}
}
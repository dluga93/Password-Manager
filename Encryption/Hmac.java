package PwdManager.Encryption;

import javax.crypto.*;
import javax.crypto.spec.*;
import PwdManager.Logger;

public class Hmac {
	private final byte[] key;
	private final static String HMAC_ALGORITHM = "HmacSHA1";
	private final static int keySizeInBits = 128;

	public Hmac(byte[] key) throws Exception {
		if (key.length != keySizeInBits/8)
			throw new Exception("Wrong Key Size. " + keySizeInBits + " bits expected.");

		this.key = key;
	}

	public byte[] getmac(byte[] message) {
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
}
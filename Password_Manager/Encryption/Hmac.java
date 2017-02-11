package Password_Manager.Encryption;

import javax.crypto.*;
import javax.crypto.spec.*;
import java.util.*;
import Password_Manager.Encryption.KeyTypes;

/**
 * @brief Class for handling message authentication codes
 */
public class Hmac {
	private final byte[] key; ///< HMAC key
	public final static KeyTypes keyType = KeyTypes.HMACSHA256; ///< type of key used

	/**
	 * @brief Exception for signaling integrity errors.
	 */
	@SuppressWarnings("serial")
	public class IntegrityException extends Exception {
	}

	/**
	 * @brief Construct an object from a byte array key
	 *
	 * @param      key   The HMAC key
	 * 
	 * @throws     Exception If key argument is of the wrong size
	 */
	public Hmac(byte[] key) throws Exception {
		if (key.length != keyType.sizeInBytes())
			throw new Exception("Wrong Key Size. " + keyType.sizeInBits()
								+ " bits expected.");

		this.key = key;
	}

	/**
	 * @brief Mac a message
	 * 
	 * Creates the MAC of a message byte array, appends it to the
	 * message, and returns the message.
	 *
	 * @param      message    The original message data
	 *
	 * @return     The macced message data
	 *
	 * @throws     Exception  If unknown options were used for the Mac algorithm
	 */
	public byte[] mac(byte[] message) throws Exception {
		byte[] mac = getMac(message);
		return Utility.concatByteArray(message, mac);
	}

	/**
	 * @brief Gets the mac of a message
	 * 
	 * Returns the mac of a message byte array
	 *
	 * @param      message    The original message
	 *
	 * @return     The mac of the message.
	 *
	 * @throws     Exception  If unknown options were used for the Mac algorithm
	 */
	public byte[] getMac(byte[] message) throws Exception {
		try {
			Mac hmac = Mac.getInstance(keyType.getType());
			hmac.init(new SecretKeySpec(key, keyType.getType()));
			return hmac.doFinal(message);
		} catch (Exception e) {
			throw new Exception("Invalid parameters for MAC algorithm.", e);
		}
	}

	/**
	 * @brief Unmac a macced message
	 * 
	 * Takes as input a message followed by its mac. Returns only the
	 * message.
	 *
	 * @param      maccedMessage  The macced message
	 *
	 * @return     The message without the mac
	 *
	 * @throws     IntegrityException,Exception If the mac doesn't match the message.
	 */
	public byte[] unmac(byte[] maccedMessage) throws Hmac.IntegrityException,
	Exception {
		if (maccedMessage.length <= keyType.sizeInBytes())
			throw new Exception("Corrupted data. Invalid length.");

		byte[] message = new byte[maccedMessage.length - keyType.sizeInBytes()];
		System.arraycopy(maccedMessage, 0, message, 0, message.length);

		byte[] mac = new byte[keyType.sizeInBytes()];
		System.arraycopy(maccedMessage, message.length, mac, 0, mac.length);

		if (!isMacCorrect(message, mac))
			throw new Hmac.IntegrityException();

		return message;
	}

	/**
	 * @brief Checks whether a message's mac is correct
	 * 
	 * Returns true if the mac of the message is the same as ```actualMac```,
	 * or false otherwise.
	 *
	 * @param      message    The message
	 * @param      actualMac  The message's mac
	 *
	 * @return     True if mac is correct, False otherwise.
	 *
	 * @throws     Exception  If unknown options were used for the mac algorithm
	 */
	private boolean isMacCorrect(byte[] message, byte[] actualMac) throws Exception {
		byte[] exptectedMac = getMac(message);
		if (Arrays.equals(exptectedMac, actualMac))
			return true;
		return false;
	}

	/**
	 * @brief Unmacs the mac key
	 * 
	 * The input ```maccedKey``` is the mac key macced using itself.
	 * This method basically removes the mac and returns only the key.
	 *
	 * @param      maccedKey           The macced key
	 *
	 * @return     only the key
	 *
	 * @throws     IntegrityException  If the maccedKey was corrupted and the mac doesn't match the key
	 * @throws     Exception           If unknown options were used for the mac algorithm
	 */
	public static byte[] unwrap(byte[] maccedKey) throws IntegrityException, Exception {
		byte[] key = new byte[maccedKey.length - keyType.sizeInBytes()];
		System.arraycopy(maccedKey, 0, key, 0, key.length);
		byte[] mac = new byte[keyType.sizeInBytes()];
		System.arraycopy(maccedKey, key.length, mac, 0, mac.length);
		Hmac hmac = new Hmac(key);
		if (hmac.isMacCorrect(key, mac))
			return key;
		else
			throw hmac.new IntegrityException();
	}
}

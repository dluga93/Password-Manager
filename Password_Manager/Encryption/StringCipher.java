package Password_Manager.Encryption;

/**
 * @brief StringCipher interface
 * 
 * Defines the methods that a Cipher of String objects must implement.
 */
public interface StringCipher {
    /**
     * @brief Encrypt a string
     *
     * @param      plaintext  The plaintext string
     *
     * @return     The encrypted string as a byte array
     *
     * @throws     Exception  If the encryption algorithm was given unknown options.
     */
	public byte[] tryEncrypt(String plaintext) throws Exception;

    /**
     * @brief Encrypt a byte array
     *
     * @param      plaintext  The plaintext as a byte array
     *
     * @return     The encrypted byte array
     *
     * @throws     Exception  If the encryption algorithm was given unknown options.
     */
	public byte[] tryEncrypt(byte[] plaintext) throws Exception;

    /**
     * @brief Decrypt a byte array to a string
     *
     * @param      encrypted  The encrypted data
     *
     * @return     The unencrypted data as a string
     *
     * @throws     Exception  If the encryption algorithm was given unknown options.
     */
	public String tryDecryptString(byte[] encrypted) throws Exception;

    /**
     * @brief Decrypt a byte array
     *
     * @param      encrypted  The encrypted data
     *
     * @return     The unencrypted data as a byte array
     *
     * @throws     Exception  If the encryption algorithm was given unknown options.
     */
	public byte[] tryDecrypt(byte[] encrypted) throws Exception;
}

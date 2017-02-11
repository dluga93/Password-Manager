package Password_Manager.Encryption;

/**
 * @brief Types of keys used
 * 
 * Enum listing the types of encryption and mac keys used. Currently
 * there are AES-128, Hmac-SHA1, HMAC-SHA-256, PBKDF2 with HMAC SHA1.
 */
public enum KeyTypes {
	AES128("AES", 128),
	HMACSHA1("HmacSHA1", 160),
	HMACSHA256("HMACSHA256", 256),
	PBKD_HMACSHA1("PBKDF2WithHmacSHA1", 0);

	private final String type;
	private final int keySizeInBits;
	
	/**
	 * @brief Constructs a key type
	 *
	 * @param      type        The type as a string
	 * @param      sizeInBits  The key size in bits
	 */
	KeyTypes(String type, int sizeInBits) {
		this.type = type;
		this.keySizeInBits = sizeInBits;
	}

	/**
	 * @brief Returns the string type of the key
	 *
	 * @return     The type.
	 */
	public String getType() {
		return type;
	}

	/**
	 * @brief Returns the size of the key in bits
	 *
	 * @return     The key size in bits
	 */
	public int sizeInBits() {
		return keySizeInBits;
	}

	/**
	 * Returns the size of the key in bytes
	 *
	 * @return     The key size in bytes
	 */
	public int sizeInBytes() {
		return keySizeInBits/8;
	}
}

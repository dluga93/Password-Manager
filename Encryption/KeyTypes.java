package PwdManager.Encryption;

public enum KeyTypes {
	AES128("AES", 128),
	HMACSHA1("HmacSHA1", 160),
	HMACSHA256("HMACSHA256", 256),
	PBKD_HMACSHA1("PBKDF2WithHmacSHA1", 0);
	private final String type;
	private final int keySizeInBits;
	
	KeyTypes(String type, int sizeInBits) {
		this.type = type;
		this.keySizeInBits = sizeInBits;
	}

	public String getType() {
		return type;
	}

	public int sizeInBits() {
		return keySizeInBits;
	}

	public int sizeInBytes() {
		return keySizeInBits/8;
	}
}
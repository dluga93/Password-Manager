package Password_Manager.Encryption;

public interface StringCipher {
	public byte[] tryEncrypt(String plaintext) throws Exception;
	public byte[] tryEncrypt(byte[] plaintext) throws Exception;
	public String tryDecryptString(byte[] encrypted) throws Exception;
	public byte[] tryDecrypt(byte[] encrypted) throws Exception;
}

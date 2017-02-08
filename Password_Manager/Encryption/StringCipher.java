package Password_Manager.Encryption;

public interface StringCipher {
	public byte[] tryEncrypt(String plaintext);
	public byte[] tryEncrypt(byte[] plaintext);
	public String tryDecryptString(byte[] encrypted) throws Exception;
	public byte[] tryDecrypt(byte[] encrypted) throws Exception;
}
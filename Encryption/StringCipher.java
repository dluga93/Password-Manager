package PwdManager.Encryption;

public interface StringCipher {
	public byte[] tryEncrypt(String plaintext);
	public byte[] tryEncrypt(byte[] plaintext);
	public String tryDecryptString(byte[] encrypted);
	public byte[] tryDecrypt(byte[] encrypted);
}
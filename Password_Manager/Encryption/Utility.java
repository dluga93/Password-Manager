package Password_Manager.Encryption;

/**
 * @brief Utility class for encryption and maccing
 */
public class Utility {
    /**
     * @brief Concatenate two byte arrys
     *
     * @return     An array containing the concatenated elements of the
     * two arguments.
     */
	public static byte[] concatByteArray(byte[] array1, byte[] array2) {
		byte[] concatenated = new byte[array1.length + array2.length];
		System.arraycopy(array1, 0, concatenated, 0, array1.length);
		System.arraycopy(array2, 0, concatenated, array1.length, array2.length);
		return concatenated;
	}
}

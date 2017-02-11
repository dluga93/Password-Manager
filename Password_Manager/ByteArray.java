package Password_Manager;

/**
 * \brief Wrapper class for byte[]
 */
public class ByteArray {
    private byte[] data;

    /**
     * Constructs an object and initializes it with the ```data``` argument.
     *
     * @param      data  The byte array for initialization.
     */
    public ByteArray(byte[] data) {
        this.data = data;
    }

    /**
     * Constructs an object as an empty array of size ```arraySize```
     *
     * @param      arraySize  The array size
     */
    public ByteArray(int arraySize) {
        this.data = new byte[arraySize];
    }

    /**
     * @return     The raw byte array
     */
    public byte[] getData() {
        return data;
    }

    /**
     * Sets the data in the byte array to the argument.
     *
     */
    public void setData(byte[] data) {
        this.data = data;
    }

    /**
     * @return     Number of elements in the array.
     */
    public int length() {
        return data.length;
    }
}
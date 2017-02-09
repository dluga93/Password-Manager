package Password_Manager;

public class ByteArray {
    private byte[] data;

    public ByteArray(byte[] data) {
        this.data = data;
    }

    public ByteArray(int arraySize) {
        this.data = new byte[arraySize];
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public int length() {
        return data.length;
    }
}
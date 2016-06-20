package PwdManager;

import java.io.*;

public class EncodedFileReader {
	private FileInputStream fileInStream;

	public EncodedFileReader(String filename) throws FileNotFoundException {
		fileInStream = new FileInputStream(filename);
	}

	public void close() throws IOException {
		fileInStream.close();
	}

	public byte[] readData() throws IOException, EOFException {
		return decodeBytes(fileInStream);
	}

	public int readInt() throws IOException, EOFException {
		return decodeInt(fileInStream);
	}

	private byte[] decodeBytes(FileInputStream file) throws IOException, EOFException {
		int length = decodeInt(file);
		byte[] data = new byte[length];
		if (file.read(data) == -1)
			throw new EOFException();
		return data;
	}

	private int decodeInt(FileInputStream file) throws IOException, EOFException {
		byte[] intBytes = new byte[4];
		if (file.read(intBytes) == -1)
			throw new EOFException();
		return decodeInt(intBytes);
	}

	private int decodeInt(byte[] encodedInt) {
		int decodedInt = 0;
		for (int i = 0; i < 4; ++i) {
			int decodedByte = encodedInt[i] << 8*i;
			decodedInt = decodedInt | decodedByte;
		}

		return decodedInt;
	}
}
package Password_Manager;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class EncodedFileWriter {
	private FileOutputStream fileOutStream;

	public EncodedFileWriter(String filename) throws FileNotFoundException {
		fileOutStream = new FileOutputStream(filename);
	}

	public EncodedFileWriter(String filename, boolean append) throws FileNotFoundException {
		fileOutStream = new FileOutputStream(filename, append);
	}

	public void close() throws IOException {
		fileOutStream.close();
	}

	public void writeData2(ArrayList<ByteArray> dataToWrite) throws IOException {
		for (ByteArray entry : dataToWrite) {
			byte[] encoded = encodeBytes(entry.getData());

			try {
				fileOutStream.write(encoded);
			} catch (IOException e) {
				fileOutStream.close();
				throw e;
			}
		}
	}

	public void writeData(byte[] data) throws IOException {
		byte[] encoded = encodeBytes(data);
		try {
			fileOutStream.write(encoded);
		} catch (IOException e) {
			fileOutStream.close();
			throw e;
		}
	}

	private byte[] encodeBytes(byte[] bytes) {
		int length = bytes.length;
		byte[] lengthBytes = encodeInt(length);

		byte[] encodedBytes = new byte[length+4];
		System.arraycopy(lengthBytes, 0, encodedBytes, 0, 4);
		System.arraycopy(bytes, 0, encodedBytes, 4, length);

		return encodedBytes;
	}

	private byte[] encodeInt(int toEncode) {
		byte[] encodedInt = new byte[4];

		for (int i = 0; i < 4; ++i) {
			encodedInt[i] = (byte)(toEncode & 0xFF);
			toEncode = toEncode >> 8;
		}

		return encodedInt;
	}

	public static void deleteFile(String pathname) throws Exception {
		File f = new File(pathname);
		if (f.isDirectory()) {
			File[] files = new File(pathname).listFiles();

			for (File file : files) {
				String newPathname = pathname + File.separator + file.getName();
				deleteFile(newPathname);
			}
		}

		try {
			Files.delete(Paths.get(pathname));
		} catch (IOException e) {
			throw new Exception("Couldn't delete file " + pathname + ". Ignoring.", e);
		}
	}
}

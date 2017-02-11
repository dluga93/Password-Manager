package Password_Manager;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * @brief Class for writing encoded data to files.
 * 
 * Data entries are written contiguously to a file.
 * Each entry is encoded using its length as an integer
 * represented by 4 bytes, followed by the data itself.
 */
public class EncodedFileWriter {
	private FileOutputStream fileOutStream; ///< The file we are writing to.

	/**
	 * Constructs an object to write to file ```filename```
	 * 
	 * This constructor tries to create a new file if it
	 * doesn't already exist, or replace the file if it exists.
	 *
	 * @param      filename  The file we want to write to.
	 * 
	 * @throws     FileNotFoundException If the file exists but is
	 * a directory, or if the file can't be created or opened.
	 */
	public EncodedFileWriter(String filename) throws FileNotFoundException {
		fileOutStream = new FileOutputStream(filename);
	}

	/**
	 * Constructs an object to write to file ```filename```
	 * 
	 * If ```append``` is true, the file should already exist, and
	 * any write operation will be performed at the end of the file.
	 * Otherwise, a new file will be created if possible.
	 *
	 * @param      filename  The filename to write to
	 * @param      append    If true, write operations write to the end
	 * of an existing file.
	 */
	public EncodedFileWriter(String filename, boolean append) throws FileNotFoundException {
		fileOutStream = new FileOutputStream(filename, append);
	}

	/**
	 * @brief Closes the file we're writing to.
	 *
	 * Closes the fileOutStream object. After the caller finished using the file,
	 * this function should always be called to release system resources.
	 *
	 * @throws     IOException  If an error occurred while closing the file stream.
	 */
	public void close() throws IOException {
		fileOutStream.close();
	}

	/**
	 * @brief Writes data to file
	 * 
	 * Writes a list of data entries to the file.
	 *
	 * @param      dataToWrite  The data to write as a list of ByteArray objects
	 *
	 * @throws     IOException  If an error occurred while writing to the file.
	 */
	public void writeData(ArrayList<ByteArray> dataToWrite) throws IOException {
		for (ByteArray entry : dataToWrite) {
			byte[] encoded = encodeBytes(entry.getRawBytes());

			try {
				fileOutStream.write(encoded);
			} catch (IOException e) {
				fileOutStream.close();
				throw e;
			}
		}
	}

	/**
	 * @brief Encodes bytes to be written to file
	 * 
	 * Encodes a byte[] to the format that will be written to file.
	 * First, 4 bytes representing the length of the byte
	 * array as an integer are written, followed by the byte array
	 * itself.
	 *
	 * @param      bytes  The bytes
	 *
	 * @return     encoded bytes to be written to the file.
	 */
	private byte[] encodeBytes(byte[] bytes) {
		int length = bytes.length;
		byte[] lengthBytes = encodeInt(length);

		byte[] encodedBytes = new byte[length+4];
		System.arraycopy(lengthBytes, 0, encodedBytes, 0, 4);
		System.arraycopy(bytes, 0, encodedBytes, 4, length);

		return encodedBytes;
	}

	/**
	 * @brief Encode an integer to a byte[]
	 *
	 * Encodes an integer to a byte array, starting from
	 * the rightmost byte. Mostly used to encode entry
	 * lengths when writing to a file.
	 *
	 * @param      toEncode  Integer to encode
	 *
	 * @return     The array of 4 bytes representing the encoded integer.
	 */
	private byte[] encodeInt(int toEncode) {
		byte[] encodedInt = new byte[4];

		for (int i = 0; i < 4; ++i) {
			encodedInt[i] = (byte)(toEncode & 0xFF);
			toEncode = toEncode >> 8;
		}

		return encodedInt;
	}

	/**
	 * @brief Delete a file
	 * 
	 * Deletes the file ```pathname```. The file can also be
	 * a directory, in which case it will be deleted recursively.
	 *
	 * @param      pathname   The path to the file to delete
	 *
	 * @throws     Exception  If the file couldn't be deleted.
	 */
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

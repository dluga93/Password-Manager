package Password_Manager;

import java.io.*;
import java.util.*;

/**
 * @brief Class for reading encoded data from files.
 * 
 * An encoded file contains contiguous data entries.
 * Each data entry is encoded as a four byte integer, showing the length
 * of the data field, followed by the data itself.
 */
public class EncodedFileReader {
	private FileInputStream fileInStream; ///< The file we are reading from.

	/**
	 * Constructs an object to read from the file ```filename```.
	 *
	 * @param      filename  The name of the file we want to read from.
	 * @throws     FileNotFoundException if the file does not exist.
	 */
	public EncodedFileReader(String filename) throws FileNotFoundException {
		fileInStream = new FileInputStream(filename);
	}

	/**
	 * @brief Closes the file we're reading.
	 * 
	 * Closes the fileInStream object.
	 *
	 * @throws     IOException  if an error occured while closing the file stream.
	 */
	public void close() throws IOException {
		fileInStream.close();
	}

	/**
	 * @brief Reads all the data from a file.
	 * 
	 * Returns all the data entries in the currently opened file.
	 *
	 * @return     A list of data entries as ```ByteArray``` objects.
	 *
	 * @throws     IOException  if a problem occured while reading from the file.
	 */
	public ArrayList<ByteArray> readData() throws IOException {
		ArrayList<ByteArray> dataEntries = new ArrayList<ByteArray>();
		try {
			while (true) { // breaks with EOFException
				byte[] rawBytes = decodeBytes();
				ByteArray entry = new ByteArray(rawBytes);
				dataEntries.add(entry);
			}
		} catch (EOFException e) {
			return dataEntries;
		}
	}

	/**
	 * @brief Returns the next decoded entry in the file
	 * 
	 * Decodes the next entry in ```fileInStream``` and returns it.
	 *
	 * @param      file          The file to read from
	 *
	 * @return     The data entry as a ```byte[]```
	 *
	 * @throws     IOException   If an error occured reading from the file
	 * @throws     EOFException  If we've reached the end of the file.
	 */
	private byte[] decodeBytes() throws IOException, EOFException {
		int length = decodeInt();
		byte[] data = new byte[length];
		if (fileInStream.read(data) == -1)
			throw new EOFException();
		return data;
	}

	private int decodeInt() throws IOException, EOFException {
		byte[] intBytes = new byte[4];
		if (fileInStream.read(intBytes) == -1)
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

	public static ArrayList<String> getFilenames(String user)
	throws Exception {
		File directory = new File(Naming.directoryName(user));
		if (!directory.exists())
			throw new Exception("Password directory not found.");

		File[] passwordFiles = directory.listFiles();
		ArrayList<String> filenames = new ArrayList<String>();
		for (File file : passwordFiles) {
			String filename = user + "_dir" + File.separator + file.getName();
			filenames.add(filename);
		}

		return filenames;
	}
}

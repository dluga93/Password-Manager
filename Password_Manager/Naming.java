package Password_Manager;

/**
 * @brief Class for generating file names
 */
public class Naming {
	// file suffixes
	private static final String masterKeyFileSuffix = "_key";
	private static final String macKeyFileSuffix = "_mackey";
	private static final String masterSaltFileSuffix = "_mastersalt";
	private static final String macSaltFileSuffix = "_macsalt";
	private static final String directorySuffix = "_dir";
	private static final String keyFileSuffix = "keys";

	/**
	 * @brief generate filename for master key salt
	 *
	 * @param      user  The username
	 *
	 * @return     Filename where salt will be stored
	 */
	public static String masterSaltFilename(String user) {
		return user + masterSaltFileSuffix;
	}

	/**
	 * @brief generate filename for mac key salt
	 *
	 * @param      user  The username
	 *
	 * @return     Filename where salt will be stored
	 */
	public static String macSaltFilename(String user) {
		return user + macSaltFileSuffix;
	}

	/**
	 * @brief generate name of password files directory
	 *
	 * @param      user  The username
	 *
	 * @return     Directory name
	 */
	public static String directoryName(String user) {
		return user + directorySuffix;
	}

	/**
	 * @brief generate name of password entry
	 *
	 * @param      user     The username
	 * @param      website  The entry's website
	 *
	 * @return     entry filename
	 */
	public static String entryFilename(String user, String website) {
		return user + "_" + website;
	}

	/**
	 * @brief generate name of file to store keys
	 *
	 * @param      user  The username
	 *
	 * @return     Name of file to store keys
	 */
	public static String keyFileName(String user) {
		return user + keyFileSuffix;
	}
}

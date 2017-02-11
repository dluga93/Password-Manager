package Password_Manager;

public class Naming {
	// file suffixes
	private static final String masterKeyFileSuffix = "_key";
	private static final String macKeyFileSuffix = "_mackey";
	private static final String masterSaltFileSuffix = "_mastersalt";
	private static final String macSaltFileSuffix = "_macsalt";
	private static final String directorySuffix = "_dir";
	private static final String keyFileSuffix = "keys";

	public static String masterSaltFilename(String user) {
		return user + masterSaltFileSuffix;
	}

	public static String macSaltFilename(String user) {
		return user + macSaltFileSuffix;
	}

	public static String directoryName(String user) {
		return user + directorySuffix;
	}

	public static String entryFilename(String user, String website) {
		return user + "_" + website;
	}

	public static String keyFileName(String user) {
		return user + keyFileSuffix;
	}
}

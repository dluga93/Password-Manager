package PwdManager;

public class Naming {
	private static final String masterKeyFileSuffix = "_key";
	private static final String macKeyFileSuffix = "_mackey";
	private static final String masterSaltFileSuffix = "_mastersalt";
	private static final String macSaltFileSuffix = "_macsalt";
	private static final String directorySuffix = "_dir";

	public static String masterKeyFilename(String user) {
		return user + masterKeyFileSuffix;
	}

	public static String macKeyFilename(String user) {
		return user + macKeyFileSuffix;
	}

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
}
package PwdManager;

public class Naming {
	private static final String masterKeyFileSuffix = "_key";
	private static final String macKeyFileSuffix = "_mackey";
	private static final String saltFileSuffix = "_salt";
	private static final String directorySuffix = "_dir";

	public static String masterKeyFilename(String user) {
		return user + masterKeyFileSuffix;
	}

	public static String macKeyFilename(String user) {
		return user + macKeyFileSuffix;
	}

	public static String saltFilename(String user) {
		return user + saltFileSuffix;
	}

	public static String directoryName(String user) {
		return user + directorySuffix;
	}

	public static String entryFilename(String user, String website) {
		return user + "_" + website;
	}
}
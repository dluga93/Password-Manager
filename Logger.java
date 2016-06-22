package PwdManager;

public class Logger {
	public static void logException(String message, Exception e) {
		e.printStackTrace();
		System.err.println(message);
	}

	public static void logError(String message) {
		System.err.println(message);
	}
}
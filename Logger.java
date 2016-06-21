package PwdManager;

public class Logger {
	public static void logError(String message, Exception e) {
		e.printStackTrace();
		System.err.println(message);
	}
}
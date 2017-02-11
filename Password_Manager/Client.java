/*
Part of the GUI code is based on a template given for an
assignment in my Security course.
*/

/**
 * \mainpage Password Manager
 * Simple password manager in Java
 */

package Password_Manager;
import Password_Manager.UI.MainUI;

/**
 * @brief The entry point of the program.
 */
public class Client {
	private static boolean restart = false;

	public static void main(String[] args) {
		do {
			restart = false;
			new MainUI();
		} while (restart);
	}//end main()

	/**
	 * @brief Sets the restart flag to true. 
	 * 
	 * @desc When the restart flag is true, the program will restart when the MainUI closes. 
	 * This is used for going back to the login screen when an account is deleted.
	 */
	public static void setRestart() {
		restart = true;
	}
}//end class

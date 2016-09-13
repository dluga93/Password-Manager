/*
Parts of the GUI code is based on a template given for an
assignment in my Security course.
*/
package Password_Manager;
import Password_Manager.UI.MainUI;

/**
 * The main client.
 */
public class Client {
	private static boolean restart = false;

	public static void main(String[] args) {
		do {
			restart = false;
			new MainUI();
		} while (restart);
	}//end main()

	public static void setRestart() {
		restart = true;
	}
}//end class

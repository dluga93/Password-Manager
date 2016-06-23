/*
Parts of the GUI code is based on a template given for an
assignment in my Security course.
*/
// TODO: don't allow any characters in website names,
// since they are used as filenames too.
// TODO: possibility to delete account
// TODO: warning if a website the user wants to add already exists
// TODO: password verification. check if password is correct.
package PwdManager;
import PwdManager.UI.MainUI;

/**
 * The main client.
 */
public class Client {
	public static void main(String[] args) {
		new MainUI();
	}//end main()
}//end class

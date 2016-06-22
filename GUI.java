/*
A lot of this class, (the GUI functionality) is based on
a template given for an assignment in my Security course.
*/
// TODO: don't allow any characters in website names, since they are used as filenames too.
// TODO: possibility to delete account
// TODO: warning if a website the user wants to add already exists
// TODO: password verification. check if password is correct.
package PwdManager;

/**
 * The main client.
 */
public class GUI {
	private static String password = null;
	private static String user = null;
	private static EncryptedMap passwords = null;
	
	public static void main(String[] args) {
		UI.welcomeScreen();
		// TODO: can I make welcomeScreen return boolean?
		if (UI.credentials.user == null)
			return;
		else if (UI.triedLogIn)
			passwords = new EncryptedMap(UI.credentials.user,
										 UI.credentials.password);
		else {
			Registration.registerUser(UI.credentials.user,
									  UI.credentials.password);
			passwords = new EncryptedMap(UI.credentials.user,
										 UI.credentials.password);
		}

		UI.setupWindow(passwords);
	}//end main()
}//end class

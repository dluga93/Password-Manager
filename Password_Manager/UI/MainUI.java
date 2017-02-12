package Password_Manager.UI;

import Password_Manager.EncryptedMap;
import Password_Manager.Client;
import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;

/**
 * @brief The main UI handler of the program
 * 
 * This class coordinates the general UI of the program,
 * from asking for authentication in the beginning, to showing
 * the main view with the menu and the password entries, to
 * showing the different dialog boxes appearing when the
 * user presses a menu item.
 */
public class MainUI {
	public static Display display; ///< The Display object the program uses to display the shells
	private Shell shell; ///< The main shell of the program
	private List list; ///< The list of password entries
	private PasswordsHandlerUI passwordHandler; ///< The UI used to handle most menu item presses

	/**
	 * @brief Starts the main UI
	 * 
	 * Starts the UI of the program, from asking the user to login or
	 * register, to showing the main view with the menu bar and the
	 * entry list, to displaying the Password handling dialog boxes
	 * when needed.
	 */
	public MainUI() {
		display = new Display();
		shell = UIUtility.createShell(new FormLayout(), "Password Manager");
		list = new List(shell, SWT.V_SCROLL);

		EncryptedMap passwords = new AuthenticationUI().start();
		if (passwords == null) {
			passwordHandler = null;
			return;
		}
		passwordHandler = new PasswordsHandlerUI(passwords);
		passwordHandler.initializeList(list);

		Menu bar = new Menu(shell, SWT.BAR);
		shell.setMenuBar(bar);

		makeFileMenu(bar);
		makeEditMenu(bar);

		FormData lay = new FormData();
		lay.left   = new FormAttachment(0,  0);
		lay.top    = new FormAttachment(0,  0);
		lay.bottom = new FormAttachment(100,0);
		lay.right  = new FormAttachment(100,0);
		list.setLayoutData(lay);

		UIUtility.startShell(shell, 300, 200);
		display.dispose ();
	}

	/**
	 * @brief Create the File menu
	 * 
	 * Creates the File menu and its items, and adds it to the menu bar ```bar```.
	 * You can create a new password, change the master password,
	 * quit the program or delete your account.
	 *
	 * @param      bar   The menu bar where this menu will be added
	 */
	private void makeFileMenu(Menu bar) {
		MenuItem menu = new MenuItem (bar, SWT.CASCADE);
		menu.setText ("&File");
		Menu submenu = new Menu (shell, SWT.DROP_DOWN);
		menu.setMenu (submenu);

		addMenuPushItem(submenu, "&New password\tCtrl+N", SWT.MOD1 + 'N',
			new Listener () {
				public void handleEvent (Event e) {
					passwordHandler.addPasswd(list);
			}});

		addMenuPushItem(submenu, "&Change Master Password", SWT.NONE,
			new Listener () {
				public void handleEvent (Event e) {
					passwordHandler.changeMasterPassword();
			}});

		addMenuPushItem(submenu, "&Quit\tCtrl+Q", SWT.MOD1 + 'Q',
			new Listener () {
				public void handleEvent (Event e) {
					shell.dispose();
			}});

		addMenuPushItem(submenu, "&Delete Account", SWT.NONE,
			new Listener () {
				public void handleEvent (Event e) {
					boolean success = tryDeleteAccount();
					if (success) {
						shell.dispose();
						Client.setRestart();
					}
			}});
	}

	/**
	 * @brief Create the Edit menu
	 * 
	 * Create the Edit menu and its items, and add it to the menu bar ```bar```.
	 * You can copy a password, delete a password or change a password.
	 *
	 * @param      bar   The menu bar where this menu will be added
	 */
	private void makeEditMenu(Menu bar) {
		MenuItem menu = new MenuItem (bar, SWT.CASCADE);
		menu.setText ("&Edit");
		Menu submenu = new Menu (shell, SWT.DROP_DOWN);
		menu.setMenu (submenu);

		addMenuPushItem(submenu, "&Copy\tCtrl+C", SWT.MOD1 + 'C',
			new Listener () {
				public void handleEvent (Event e) {
					String[] keys = list.getSelection();
					for (String key : keys) {
						String value = passwordHandler.getPassword(key);
						UIUtility.copyToClipboard(value);
					}
			}});

		addMenuPushItem(submenu, "&Delete\tDel", SWT.DEL,
			new Listener () {
				public void handleEvent (Event e) {
					if (!UIUtility.yesNoQuestion("Delete Entry", "Are you sure?"))
						return;
					String[] keys = list.getSelection();
					for (String key : keys) {
						boolean success = tryDeleteEntry(key);
						if (success)
							list.remove(key);
					}
			}});

		addMenuPushItem(submenu, "&Change\tEnter", SWT.LF,
			new Listener () {
				public void handleEvent (Event e) {
					String[] keys = list.getSelection();
					for (String key : keys) {
						passwordHandler.editEntry(key);
					}
			}});
	}

	/**
	 * @brief Adds a menu item
	 * 
	 * Adds an item to a menu.
	 *
	 * @param      menu         The menu where the item will be added
	 * @param      text         The text label of the item
	 * @param      accelerator  A bit mask showing the keyboard shortcut that
	 * can be used for this item.
	 * @param      listener     The listener describing what happens when
	 * the item is clicked/activated.
	 */
	private void addMenuPushItem(Menu menu, String text,
							int accelerator, Listener listener) {
		MenuItem item = new MenuItem(menu, SWT.PUSH);
		item.setText(text);
		item.setAccelerator(accelerator);
		item.addListener(SWT.Selection, listener);
	}

	/**
	 * @brief Delete a user account
	 *
	 * @return     True if successfule, false otherwise.
	 */
	private boolean tryDeleteAccount() {
		try {
			passwordHandler.deleteAccount();
			return true;
		} catch (Exception e) {
			UIUtility.errorMessage("Deleting Account", e.getMessage());
			return false;
		}
	}

	/**
	 * @brief Delete a password entry
	 *
	 * @param      key   The website name that is used as the key
	 * in the password map.
	 *
	 * @return     True if successful, false otherwise.
	 */
	private boolean tryDeleteEntry(String key) {
		try {
			passwordHandler.deletePassword(key);
			return true;
		} catch (Exception e) {
			UIUtility.errorMessage("Deleting Password", e.getMessage());
			return false;
		}
	}
}

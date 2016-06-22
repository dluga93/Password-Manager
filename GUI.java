/*
A lot of this class, (the GUI functionality) is based on
a template given for an assignment in my Security course.
*/
// TODO: don't allow any characters in website names, since they are used as filenames too.
// TODO: change master password
// TODO: ask "are you sure" when removing entry
// TODO: possibility to delete account
// TODO: warning if a website the user wants to add already exists
package PwdManager;

import org.eclipse.swt.*;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;

/**
 * The main client.
 */
public class GUI {
	private static String password = null;
	private static String user = null;
	private static boolean triedLogIn = false;
	private static EncryptedMap passwords = null;
	private static final Display display = new Display ();
	private static final Shell shell = new Shell(display);
	private static final List list = new List(shell, SWT.BORDER);
	private static final Clipboard cb = new Clipboard(display);

	public static Shell createShell(Layout layout) {
		Shell shell = new Shell(display);
		shell.setLayout(layout);
		return shell;
	}

	public static void startShell(Shell shell) {
		shell.pack();
		shell.open();

		while (!shell.isDisposed())
			if (!display.readAndDispatch())
				display.sleep();
	}

	public static void welcomeScreen() {
		final Shell shell = createShell(new FillLayout());
		shell.setText("Password Manager");

		Button login = new Button(shell, SWT.PUSH);
		login.setText("Log In\n");
		login.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				shell.dispose();
				inputCredentials();
				triedLogIn = true;
			}
		});

		Button register = new Button(shell, SWT.PUSH);
		register.setText("Register");
		register.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				shell.dispose();
				inputCredentials();
				triedLogIn = false;
			}
		});

		startShell(shell);
	}

	public static void inputCredentials() {
		final Shell shell = createShell(new FillLayout());
		shell.setText("Input Credentials");

		final Text tuser  = new Text(shell, SWT.BORDER);
		final Text tpass  = new Text(shell, SWT.BORDER | SWT.PASSWORD);

		Button submit = new Button(shell, SWT.PUSH);
		shell.setDefaultButton(submit);
		submit.setText("Submit");
		submit.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				password = tpass.getText();
				user = tuser.getText();
				shell.dispose();
			}
		});

		startShell(shell);
	}

	public static void editPassword(String website) {
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = true;
		final Shell shell = createShell(layout);
		shell.setText("Edit Password");

		final Label newPass = new Label(shell, SWT.LEFT);
		newPass.setText("New password");
		final Text newPassText = new Text(shell, SWT.BORDER | SWT.PASSWORD);
		final Label confirmPass = new Label(shell, SWT.LEFT);
		confirmPass.setText("Confirm password");
		final Text confirmPassText  = new Text(shell, SWT.BORDER | SWT.PASSWORD);

		Button submit = new Button(shell, SWT.PUSH);
		shell.setDefaultButton(submit);
		submit.setText("Submit");
		submit.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				String password1 = newPassText.getText();
				String password2 = confirmPassText.getText();
				if (!password1.equals(password2)) {
					Logger.logError("Passwords don't match.");
					return;
				}
				tryAddPassword(website, password1);
				shell.dispose();
			}
		});

		startShell(shell);
	}

	public static void addPasswd() {
		final Shell shell = createShell(new FillLayout());
		shell.setText("Add Password");

		final Text tsite  = new Text(shell, SWT.BORDER);
		final Text tpass  = new Text(shell, SWT.BORDER | SWT.PASSWORD);

		Button add = new Button(shell, SWT.PUSH);
		shell.setDefaultButton(add);
		add.setText("Add");
		add.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				tryAddPassword(tsite.getText(), tpass.getText());
				shell.dispose();
			}
		});

		startShell(shell);
	}

	public static void tryAddPassword(String website, String password) {
		try {
			passwords.addEntry(website, password);
			list.add(website);
		} catch (Exception exc) {
			Logger.logException("Can't add new password. File corrupted.", exc);
			System.exit(1);
		}
	}

	public static void addMenuPushItem(Menu menu, String text,
							int accelerator, Listener listener) {
		MenuItem item = new MenuItem(menu, SWT.PUSH);
		item.setText(text);
		item.setAccelerator(accelerator);
		item.addListener(SWT.Selection, listener);
	}

	public static void copyToClipboard(String string) {
		if (string != null) {
			TextTransfer textTransfer = TextTransfer.getInstance();
			cb.setContents(new Object[]{string},
						   new Transfer[]{textTransfer});
		}
	}
	
	public static void main(String[] args) {
		
		welcomeScreen();
		if (user == null)
			return;
		else if (triedLogIn)
			passwords = new EncryptedMap(user, password);
		else {
			Registration.registerUser(user, password);
			passwords = new EncryptedMap(user, password);
		}

		/* set up SWT */
		shell.setText("Password Manager");
		shell.setLayout(new FormLayout());
		
		/* make the menu bar */
		Menu bar = new Menu (shell, SWT.BAR);
		shell.setMenuBar (bar);

		{ /* file menu */
			MenuItem menu = new MenuItem (bar, SWT.CASCADE);
			menu.setText ("&File");
			Menu submenu = new Menu (shell, SWT.DROP_DOWN);
			menu.setMenu (submenu);

			addMenuPushItem(submenu, "&New password\tCtrl+N", SWT.MOD1 + 'N',
				new Listener () {
					public void handleEvent (Event e) {
						addPasswd();
				}});

			addMenuPushItem(submenu, "&Quit\tCtrl+Q", SWT.MOD1 + 'Q',
				new Listener () {
					public void handleEvent (Event e) {
						shell.dispose();
				}});
		}

		{ /* edit menu */
			MenuItem menu = new MenuItem (bar, SWT.CASCADE);
			menu.setText ("&Edit");
			Menu submenu = new Menu (shell, SWT.DROP_DOWN);
			menu.setMenu (submenu);

			addMenuPushItem(submenu, "&Copy\tCtrl+C", SWT.MOD1 + 'C',
				new Listener () {
					public void handleEvent (Event e) {
						String[] keys = list.getSelection();
						for (String key : keys) {
							String value = passwords.getWebsitePassword(key);
							copyToClipboard(value);
						}
				}});

			addMenuPushItem(submenu, "&Delete\tDel", SWT.DEL,
				new Listener () {
					public void handleEvent (Event e) {
						String[] keys = list.getSelection();
						for (String key : keys) {
							passwords.removeEntry(key);
							list.remove(key);
						}
				}});

			addMenuPushItem(submenu, "&Change\tEnter", SWT.LF,
				new Listener () {
					public void handleEvent (Event e) {
						String[] keys = list.getSelection();
						for (String key : keys) {
							editPassword(key);
						}
				}});
		}

		list.addListener(SWT.KeyDown, new Listener(){
 			public void handleEvent(Event e){
 				switch (e.character){
 					case SWT.DEL:
 					{
 						String[] keys = list.getSelection();
						for (String key : keys) {
							passwords.removeEntry(key);
							list.remove(key);
						}
 						break;
 					}
 				}
 			}});

		for (String entry : passwords.getWebsites()) {
			list.add(entry);
		}

		FormData lay = new FormData();
		lay.left   = new FormAttachment(0,  0);
		lay.top    = new FormAttachment(0,  0);
		lay.bottom = new FormAttachment(100,0);
		lay.right  = new FormAttachment(100,0);
		list.setLayoutData(lay);
		
		/* go */
		shell.setSize(200, 500);
		startShell(shell);
		
		display.dispose ();
	}//end main()
}//end class

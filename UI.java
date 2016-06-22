package PwdManager;

import org.eclipse.swt.*;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;

public class UI {
	public static boolean triedLogIn = false;
	private static final Display display = new Display ();
	private static final Shell shell = new Shell(display);
	private static final List list = new List(shell, SWT.BORDER);
	private static final Clipboard cb = new Clipboard(display);
	private static boolean answeredYes = false;
	public static Credentials credentials;

	public static void setupWindow(EncryptedMap passwords) {
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
						addPasswd(passwords);
				}});

			addMenuPushItem(submenu, "&Change Master Password", SWT.NONE,
				new Listener () {
					public void handleEvent (Event e) {
						changePassword(passwords);
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
						areYouSure("Delete Entry");
						if (!answeredYes)
							return;
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
							editPassword(passwords, key);
						}
				}});
		}

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
		startShell(shell);

		display.dispose ();
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
				String password = tpass.getText();
				String user = tuser.getText();
				credentials = new Credentials(user, password);
				shell.dispose();
			}
		});

		startShell(shell);
	}

	public static void editPassword(EncryptedMap passwords, String website) {
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
				tryAddPassword(passwords, website, password1);
				shell.dispose();
			}
		});

		startShell(shell);
	}

	public static void addPasswd(EncryptedMap passwords) {
		final Shell shell = createShell(new FillLayout());
		shell.setText("Add Password");

		final Text tsite  = new Text(shell, SWT.BORDER);
		final Text tpass  = new Text(shell, SWT.BORDER | SWT.PASSWORD);

		Button add = new Button(shell, SWT.PUSH);
		shell.setDefaultButton(add);
		add.setText("Add");
		add.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				tryAddPassword(passwords, tsite.getText(), tpass.getText());
				shell.dispose();
			}
		});

		startShell(shell);
	}

	public static void tryAddPassword(EncryptedMap passwords,
						String website, String password) {
		try {
			passwords.addEntry(website, password);
			list.add(website);
		} catch (Exception exc) {
			Logger.logException("Can't add new password. File corrupted.", exc);
			System.exit(1);
		}
	}

	public static void areYouSure(String title) {
		answeredYes = false;

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = true;
		final Shell shell = createShell(layout);
		shell.setText(title);

		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.CENTER;
		gridData.horizontalSpan = 2;

		Label label = new Label(shell, SWT.CENTER);
		label.setText("Are you sure?");
		label.setLayoutData(gridData);

		Button yes = new Button(shell, SWT.PUSH | SWT.CENTER);
		yes.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		shell.setDefaultButton(yes);
		yes.setText("Yes");
		yes.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				answeredYes = true;
				shell.dispose();
			}
		});

		Button no = new Button(shell, SWT.PUSH | SWT.CENTER);
		no.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		shell.setDefaultButton(no);
		no.setText("No");
		no.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				answeredYes = false;
				shell.dispose();
			}
		});

		startShell(shell);
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

	public static void changePassword(EncryptedMap passwords) {
		final Shell shell = createShell(new FillLayout());
		shell.setText("Change Password");

		final Text oldPass = new Text(shell, SWT.BORDER | SWT.PASSWORD);
		final Text newPass  = new Text(shell, SWT.BORDER | SWT.PASSWORD);

		Button change = new Button(shell, SWT.PUSH);
		shell.setDefaultButton(change);
		change.setText("Submit");
		change.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				String oldPassword = oldPass.getText();
				String newPassword = newPass.getText();
				passwords.tryChangeMasterPassword(oldPassword, newPassword);
				shell.dispose();
			}
		});

		startShell(shell);
	}

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
}
package PwdManager.UI;

import PwdManager.EncryptedMap;
import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;

public class MainUI {
	public static final Display display = new Display();
	private final Shell shell = new Shell(display);
	private final List list = new List(shell, SWT.BORDER);
	private final PasswordsHandlerUI passwordHandler;

	public MainUI() {
		EncryptedMap passwords = AuthenticationUI.start();
		passwordHandler = new PasswordsHandlerUI(passwords);
		passwordHandler.initializeList(list);

		/* set up SWT */
		shell.setText("Password Manager");
		shell.setLayout(new FormLayout());
		
		/* make the menu bar */
		Menu bar = new Menu (shell, SWT.BAR);
		shell.setMenuBar (bar);

		makeFileMenu(bar);
		makeEditMenu(bar);

		FormData lay = new FormData();
		lay.left   = new FormAttachment(0,  0);
		lay.top    = new FormAttachment(0,  0);
		lay.bottom = new FormAttachment(100,0);
		lay.right  = new FormAttachment(100,0);
		list.setLayoutData(lay);
		
		/* go */
		UIUtility.startShell(shell);

		display.dispose ();
	}

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
	}

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
					if (!UIUtility.areYouSure("Delete Entry"))
						return;
					String[] keys = list.getSelection();
					for (String key : keys) {
						passwordHandler.deletePassword(key);
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

	private void addMenuPushItem(Menu menu, String text,
							int accelerator, Listener listener) {
		MenuItem item = new MenuItem(menu, SWT.PUSH);
		item.setText(text);
		item.setAccelerator(accelerator);
		item.addListener(SWT.Selection, listener);
	}
}
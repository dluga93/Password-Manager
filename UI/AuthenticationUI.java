package PwdManager.UI;
import PwdManager.EncryptedMap;
import PwdManager.Registration;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;

public class AuthenticationUI {
	private static String user;
	private static String password;
	private static EncryptedMap passwords;

	public static EncryptedMap start() {
		final Shell shell = UIUtility.createShell(new FillLayout());
		shell.setText("Password Manager");

		Button login = new Button(shell, SWT.PUSH);
		login.setText("Log In\n");
		login.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				shell.dispose();
				inputCredentialsDialog();
				passwords = new EncryptedMap(user, password);
			}
		});

		Button register = new Button(shell, SWT.PUSH);
		register.setText("Register");
		register.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				shell.dispose();
				inputCredentialsDialog();
				Registration.registerUser(user, password);
				passwords = new EncryptedMap(user, password);
			}
		});

		UIUtility.startShell(shell);
		return passwords;
	}

	private static void inputCredentialsDialog() {
		final Shell shell = UIUtility.createShell(new FillLayout());
		shell.setText("Input Credentials");

		final Text tuser  = new Text(shell, SWT.BORDER);
		final Text tpass  = new Text(shell, SWT.BORDER | SWT.PASSWORD);

		Button submit = new Button(shell, SWT.PUSH);
		shell.setDefaultButton(submit);
		submit.setText("Submit");
		submit.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				user = tuser.getText();
				password = tpass.getText();
				shell.dispose();
			}
		});

		UIUtility.startShell(shell);
	}
}
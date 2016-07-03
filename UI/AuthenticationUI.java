package PwdManager.UI;
import PwdManager.EncryptedMap;
import PwdManager.Encryption.Hmac.IntegrityException;
import PwdManager.Registration;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;

public class AuthenticationUI {
	private static String user;
	private static String password;
	private static EncryptedMap passwords;
	private static boolean credentialsGiven;

	public static EncryptedMap start() {
		final Shell shell = UIUtility.createShell(new FillLayout());
		shell.setText("Password Manager");

		Button login = new Button(shell, SWT.PUSH);
		login.setText("Log In\n");
		login.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				inputCredentialsDialog();
				if (!credentialsGiven)
					return;
				boolean success = tryLogin(user, password);
				if (success)
					shell.dispose();
			}
		});

		Button register = new Button(shell, SWT.PUSH);
		register.setText("Register");
		register.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				inputCredentialsDialog();
				if (!credentialsGiven)
					return;
				boolean success = tryRegister(user, password);
				if (success)
					success = tryLogin(user, password);
				if (success)
					shell.dispose();
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
		credentialsGiven = false;

		Button submit = new Button(shell, SWT.PUSH);
		shell.setDefaultButton(submit);
		submit.setText("Submit");
		submit.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				user = tuser.getText();
				password = tpass.getText();
				credentialsGiven = true;
				shell.dispose();
			}
		});

		UIUtility.startShell(shell);
	}

	private static boolean tryLogin(String user, String password) {
		try {
			passwords = new EncryptedMap(user, password);
			return true;
		} catch (IntegrityException e) {
			UIUtility.errorMessage("Login", "Wrong Password or corrupted files.");
			return false;
		} catch (Exception e) {
			UIUtility.errorMessage("Login", e.getMessage());
			return false;
		}
	}

	private static boolean tryRegister(String user, String password) {
		try {
			Registration.registerUser(user, password);
			return true;
		} catch (Exception e) {
			UIUtility.errorMessage("Registration", e.getMessage());
			return false;
		}
	}
}
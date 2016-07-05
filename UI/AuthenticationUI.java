package PwdManager.UI;
import PwdManager.EncryptedMap;
import PwdManager.Encryption.Hmac.IntegrityException;
import PwdManager.Registration;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.custom.*;
import java.nio.file.*;

public class AuthenticationUI {
	private String user;
	private String password;
	private EncryptedMap passwords = null;
	private boolean credentialsGiven = false;

	private static final GridData textFieldData = new GridData(100, SWT.DEFAULT);

	public EncryptedMap start() {
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

	private void inputCredentialsDialog() {
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		final Shell shell = UIUtility.createShell(layout);
		shell.setText("Input Credentials");

		final CLabel userLabel = new CLabel(shell, SWT.CENTER);
		userLabel.setText("Username: ");
		final Text tuser  = new Text(shell, SWT.BORDER);
		tuser.setLayoutData(textFieldData);

		final CLabel passLabel = new CLabel(shell, SWT.CENTER);
		passLabel.setText("Password: ");
		final Text tpass  = new Text(shell, SWT.BORDER | SWT.PASSWORD);
		tpass.setLayoutData(textFieldData);

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

	private boolean tryLogin(String user, String password) {
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

	private boolean tryRegister(String user, String password) {
		try {
			Registration.registerUser(user, password);
			return true;
		} catch (FileAlreadyExistsException e) {
			UIUtility.errorMessage("Registration.", "User " + user + " already exists.");
			return false;
		} catch (Exception e) {
			UIUtility.errorMessage("Registration", e.getMessage());
			return false;
		}
	}
}
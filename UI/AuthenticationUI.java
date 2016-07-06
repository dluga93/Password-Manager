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

	public EncryptedMap start() {
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		layout.makeColumnsEqualWidth = true;
		GridData buttonGridData = new GridData(80, SWT.DEFAULT);

		final Shell shell = UIUtility.createShell(layout);
		shell.setText("Password Manager");

		Button login = new Button(shell, SWT.PUSH);
		login.setLayoutData(buttonGridData);
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
		register.setLayoutData(buttonGridData);
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

		Button exit = new Button(shell, SWT.PUSH);
		exit.setLayoutData(buttonGridData);
		exit.setText("Exit");
		exit.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
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
		tuser.setLayoutData(UIUtility.textFieldData);

		final CLabel passLabel = new CLabel(shell, SWT.CENTER);
		passLabel.setText("Password: ");
		final Text tpass  = new Text(shell, SWT.BORDER | SWT.PASSWORD);
		tpass.setLayoutData(UIUtility.textFieldData);

		UIUtility.addEmptyCell(shell);

		credentialsGiven = false;

		Button submit = new Button(shell, SWT.PUSH);
		submit.setLayoutData(UIUtility.textFieldData);
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
			new Registration(user, password);
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
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
	private String user, password;
	private EncryptedMap passwords;
	private boolean credentialsGiven;
	private Shell mainShell, inputShell;

	public EncryptedMap start() {
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		layout.makeColumnsEqualWidth = true;

		mainShell = UIUtility.createShell(layout, "Password Manager");

		makeButton(mainShell, "Log In", new Listener() {
			public void handleEvent(Event e) {
				inputCredentialsDialog();
				if (!credentialsGiven)
					return;
				boolean success = tryLogin(user, password);
				if (success)
					mainShell.dispose();
			}
		});

		makeButton(mainShell, "Register", new Listener() {
			public void handleEvent(Event e) {
				inputCredentialsDialog();
				if (!credentialsGiven)
					return;
				boolean success = tryRegister(user, password);
				if (success)
					success = tryLogin(user, password);
				if (success)
					mainShell.dispose();
			}
		});

		makeButton(mainShell, "Exit",  new Listener() {
			public void handleEvent(Event e) {
				mainShell.dispose();
			}
		});

		UIUtility.startShell(mainShell);
		return passwords;
	}

	// first button created for the shell will be the default button
	private Button makeButton(Shell shell, String text, Listener listener) {
		Button button = new Button(shell, SWT.PUSH);
		button.setLayoutData(UIUtility.buttonGridData);
		button.setText(text);
		button.addListener(SWT.Selection, listener);
		shell.setDefaultButton(button);
		return button;
	}

	private void inputCredentialsDialog() {
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		inputShell = UIUtility.createShell(layout, "Input Credentials");

		final Text tuser = labelAndText("Username: ", SWT.BORDER);
		final Text tpass = labelAndText("Password: ", SWT.BORDER | SWT.PASSWORD);

		UIUtility.addEmptyCell(inputShell);

		credentialsGiven = false;

		Button submit = makeButton(inputShell, "Submit", new Listener() {
			public void handleEvent(Event e) {
				user = tuser.getText();
				password = tpass.getText();
				credentialsGiven = true;
				inputShell.dispose();
			}
		});
		submit.setLayoutData(UIUtility.textFieldData);

		UIUtility.startShell(inputShell);
	}

	private Text labelAndText(String labelText, int textFlags) {
		final CLabel label = new CLabel(inputShell, SWT.CENTER);
		label.setText(labelText);
		final Text textField = new Text(inputShell, textFlags);
		textField.setLayoutData(UIUtility.textFieldData);
		return textField;
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
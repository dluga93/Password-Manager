package PwdManager.UI;

import PwdManager.EncryptedMap;
import PwdManager.Logger;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;

public class PasswordsHandlerUI {
	private EncryptedMap passwords;
	
	public PasswordsHandlerUI(EncryptedMap passwords) {
		this.passwords = passwords;
	}

	public void initializeList(List list) {
		for (String entry : passwords.getWebsites())
			list.add(entry);
	}

	public void addPasswd(List list) {
		final Shell shell = UIUtility.createShell(new FillLayout());
		shell.setText("Add Password");

		final Text tsite  = new Text(shell, SWT.BORDER);
		final Text tpass  = new Text(shell, SWT.BORDER | SWT.PASSWORD);

		Button add = new Button(shell, SWT.PUSH);
		shell.setDefaultButton(add);
		add.setText("Add");
		add.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				tryAddPassword(tsite.getText(), tpass.getText());
				list.add(tsite.getText());
				shell.dispose();
			}
		});

		UIUtility.startShell(shell);
	}

	private void tryAddPassword(String website, String password) {
		try {
			passwords.addEntry(website, password);
		} catch (Exception exc) {
			Logger.logException("Can't add new password. File corrupted.", exc);
			System.exit(1);
		}
	}

	public void editPassword(String website) {
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = true;
		final Shell shell = UIUtility.createShell(layout);
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

		UIUtility.startShell(shell);
	}

	public void changePassword() {
		final Shell shell = UIUtility.createShell(new FillLayout());
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

		UIUtility.startShell(shell);
	}

	public String getPassword(String website) {
		return passwords.getWebsitePassword(website);
	}

	public void deletePassword(String website) {
		passwords.removeEntry(website);
	}
}
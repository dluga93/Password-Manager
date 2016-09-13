package PwdManager.UI;

import PwdManager.EncryptedMap;
import PwdManager.Logger;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import java.util.regex.Pattern;
import java.io.*;

public class PasswordsHandlerUI {
	private EncryptedMap passwords;
	
	public PasswordsHandlerUI(EncryptedMap passwords) {
		this.passwords = passwords;
	}

	public void initializeList(List list) {
		for (String entry : passwords.getWebsites())
			list.add(entry, 0);
	}

	public void addPasswd(List list) {
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;

		final Shell shell = UIUtility.createShell(layout, "Add Password");

		final Text tsite = UIUtility.labelAndText(shell, "Website: ", SWT.BORDER);
		final Text tpass = UIUtility.labelAndText(shell, "Password: ", SWT.BORDER | SWT.PASSWORD);
		UIUtility.addEmptyCell(shell);

		Button add = new Button(shell, SWT.PUSH);
		shell.setDefaultButton(add);
		add.setText("Add");
		add.setLayoutData(UIUtility.textFieldData);
		add.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				if (list.indexOf(tsite.getText()) != -1) {
					String question = tsite.getText() + " already exists. Replace?";
					UIUtility.yesNoQuestion("Replace password", question);
					if (!UIUtility.answeredYes) {
						shell.dispose();
						return;
					}
				}
				boolean success = tryAddPassword(tsite.getText(), tpass.getText());
				if (success) {
					if (list.indexOf(tsite.getText()) == -1)
						list.add(tsite.getText());
					shell.dispose();
				}
			}
		});

		UIUtility.startShell(shell);
	}

	private boolean tryAddPassword(String website, String password) {
		try {
			isValid(website);
			passwords.addEntry(website, password);
			return true;
		} catch (IOException e) {
			Logger.logException("Can't add new entry.", e);
			System.exit(1);
			return false;
		} catch (Exception e) {
			UIUtility.errorMessage("Adding/Changing Entry", e.getMessage());
			return false;
		}
	}

	private void isValid(String website) throws Exception {
		Pattern websitePattern = Pattern.compile("[a-zA-Z0-9_.-]+");
		boolean match = websitePattern.matcher(website).matches();
		if (!match)
			throw new Exception("Invalid characters in website.");
	}

	public void editEntry(String website) {
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		final Shell shell = UIUtility.createShell(layout, "Edit Password");

		final Text newPassText = UIUtility.labelAndText(shell, "New Password: ", SWT.BORDER | SWT.PASSWORD);
		final Text confirmPassText = UIUtility.labelAndText(shell, "Confirm Password: ", SWT.BORDER | SWT.PASSWORD);
		UIUtility.addEmptyCell(shell);

		Button submit = new Button(shell, SWT.PUSH);
		shell.setDefaultButton(submit);
		submit.setText("Submit");
		submit.setLayoutData(UIUtility.textFieldData);
		submit.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				String password1 = newPassText.getText();
				String password2 = confirmPassText.getText();
				if (!password1.equals(password2)) {
					Logger.logError("Passwords don't match.");
					return;
				}
				boolean success = tryAddPassword(website, password1);
				if (success)
					shell.dispose();
			}
		});

		UIUtility.startShell(shell);
	}

	public void changeMasterPassword() {
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;

		final Shell shell = UIUtility.createShell(layout, "Change Password");

		// TODO: check what error we get if we input the wrong old password
		final Text oldPass = UIUtility.labelAndText(shell, "Old Password: ", SWT.BORDER | SWT.PASSWORD);
		final Text newPass = UIUtility.labelAndText(shell, "New Password: ", SWT.BORDER | SWT.PASSWORD);
		UIUtility.addEmptyCell(shell);

		Button change = new Button(shell, SWT.PUSH);
		shell.setDefaultButton(change);
		change.setLayoutData(UIUtility.textFieldData);
		change.setText("Submit");
		change.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				String oldPassword = oldPass.getText();
				String newPassword = newPass.getText();
				boolean success = tryChangeMasterPassword(oldPassword, newPassword);
				if (success)
					shell.dispose();
			}
		});

		UIUtility.startShell(shell);
	}

	private boolean tryChangeMasterPassword(String oldPassword, String newPassword) {
		try {
			passwords.tryChangeMasterPassword(oldPassword, newPassword);
			return true;
		} catch (Exception e) {
			UIUtility.errorMessage("Password Change Error", e.getMessage());
			return false;
		}
	}

	public String getPassword(String website) {
		return passwords.getWebsitePassword(website);
	}

	public void deletePassword(String website) throws Exception {
		passwords.removeEntry(website);
	}

	public void deleteAccount() throws Exception {
		passwords.deleteAccount();
	}
}

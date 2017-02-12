package Password_Manager.UI;

import Password_Manager.EncryptedMap;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import java.util.regex.Pattern;
import java.io.*;

/**
 * @brief UI class to handle password related activities
 * 
 * UI class to displaying dialog boxes needed to perform
 * activities like adding a password entry, changing a
 * password entry, changing the master password etc.
 */
public class PasswordsHandlerUI {
	private EncryptedMap passwords; ///< The set of password entries

	/**
	 * @brief Create the UI
	 *
	 * @param      passwords  The password entries
	 */
	public PasswordsHandlerUI(EncryptedMap passwords) {
		this.passwords = passwords;
	}

	/**
	 * @brief Create the list of entries in the UI
	 * 
	 * Creates the list of entries in the UI.
	 * 
	 * Each element
	 * shows the website name, but the password is not shown. The
	 * user can get the password by selecting the website whose
	 * password he wants and using the Copy button in the Edit
	 * menu. The password will be copied to the clipboard
	 * and will be ready to be pasted.
	 *
	 * @param      list  The list that will be initialized with the
	 * password entries.
	 */
	public void initializeList(List list) {
		for (String entry : passwords.getWebsites())
			list.add(entry, 0);
	}

	/**
	 * @brief UI to add a password entry
	 * 
	 * Shows the UI to add a new password entry. Asks to input
	 * the website and password for the entry.
	 *
	 * @param      list  The list where the entry will be added
	 */
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

	/**
	 * @brief Add a password entry
	 * 
	 * Checks validity of the password and website, and
	 * tries to add the password to the password map
	 *
	 * @param      website   The website
	 * @param      password  The password
	 *
	 * @return     True if successful, false otherwise
	 */
	private boolean tryAddPassword(String website, String password) {
		try {
			isValid(website);
			passwords.addEntry(website, password);
			return true;
		} catch (IOException e) {
			UIUtility.errorMessage("Adding/Changing Entry",
				"IO Error occurred when trying to add new entry.");
			return false;
		} catch (Exception e) {
			UIUtility.errorMessage("Adding/Changing Entry", e.getMessage());
			return false;
		}
	}

	/**
	 * @brief Checks validity of the website string in an entry
	 * 
	 * The only website names allowed are those containing english alphabet
	 * letters, numbers, dashes, underscores and dots.
	 *
	 * @param      website    The website
	 *
	 * @throws     Exception  If the website name is invalid.
	 */
	private void isValid(String website) throws Exception {
		Pattern websitePattern = Pattern.compile("[a-zA-Z0-9_.-]+");
		boolean match = websitePattern.matcher(website).matches();
		if (!match)
			throw new Exception("Invalid characters in website." +
				" Use only english letters, numbers, dot, dash or underscore.");
	}

	/**
	 * @brief Show UI to edit a password entry
	 *
	 * @param      website  The website of the entry
	 */
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
					UIUtility.errorMessage("Editing Password", "Passwords don't match.");
					return;
				}
				boolean success = tryAddPassword(website, password1);
				if (success)
					shell.dispose();
			}
		});

		UIUtility.startShell(shell);
	}

	/**
	 * @brief Show UI to change the master password
	 * 
	 * The use will be asked for his previous password as well as
	 * the new one.
	 */
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

	/**
	 * @brief Change the master password
	 * 
	 * Calls the backend method to change the master password
	 * and handle the files.
	 *
	 * @param      oldPassword  The old password
	 * @param      newPassword  The new password
	 *
	 * @return     True if successful, false otherwise.
	 */
	private boolean tryChangeMasterPassword(String oldPassword, String newPassword) {
		try {
			passwords.tryChangeMasterPassword(oldPassword, newPassword);
			return true;
		} catch (Exception e) {
			UIUtility.errorMessage("Password Change Error", e.getMessage());
			return false;
		}
	}

	/**
	 * @brief Get the password of an entry
	 *
	 * @param      website  The website of the entry
	 *
	 * @return     The password.
	 */
	public String getPassword(String website) {
		return passwords.getWebsitePassword(website);
	}

	/**
	 * @brief Delete a password entry
	 *
	 * @param      website    The website of the entry
	 *
	 * @throws     Exception  If an error occurred removing the password file.
	 */
	public void deletePassword(String website) throws Exception {
		passwords.removeEntry(website);
	}

	/**
	 * @brief Delete the account 
	 *
	 * @throws     Exception  If an error occurred trying to delete the user's files.
	 */
	public void deleteAccount() throws Exception {
		passwords.deleteAccount();
	}
}

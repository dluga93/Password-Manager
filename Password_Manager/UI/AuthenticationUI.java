package Password_Manager.UI;
import Password_Manager.EncryptedMap;
import Password_Manager.Encryption.*;
import Password_Manager.Registration;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import java.nio.file.*;

/**
 * @brief Initial Login/Register screen
 * 
 * This class handles the UI that shows up before a user has
 * logged in or created an account. It allows to login as an
 * existing user, register as a new user, or exit the program.
 */
public class AuthenticationUI {
    private String user, password;  ///< username and password of a user
    private EncryptedMap passwords; ///< password entries of the logged in user
    private boolean credentialsGiven; ///< boolean flag to check if submit or cancel was pressed

    /**
     * @brief Start the authentication UI
     * 
     * Shows the screen with the Login, Register and Exit
     * buttons.
     *
     * @return     the password entries of a logged in user
     */
    public EncryptedMap start() {
        GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        layout.makeColumnsEqualWidth = true;

        final Shell shell = UIUtility.createShell(layout, "Password Manager");

        makeButton(shell, "Log In", new Listener() {
            public void handleEvent(Event e) {
                inputCredentialsDialog();
                if (!credentialsGiven)
                    return;
                boolean success = tryLogin(user, password);
                if (success)
                    shell.dispose();
            }
        });

        makeButton(shell, "Register", new Listener() {
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

        makeButton(shell, "Exit",  new Listener() {
            public void handleEvent(Event e) {
                shell.dispose();
            }
        });

        UIUtility.startShell(shell);
        return passwords;
    }

    /**
     * @brief Add a button to the shell
     * 
     * Adds a button to the ```shell``` (window), with a label of ```text```
     * and a Listener ```listener``` describing what happens when the button
     * is pressed.
     * 
     * The first button added to a shell will be the default button. Meaning
     * that button will be pressed if the user presses the Enter key after
     * the UI initializes.
     *
     * @param      shell     The shell to which we add the button
     * @param      text      The label of the button
     * @param      listener  The listener describing what happens when button is pressed
     *
     * @return     The created button
     */
    private Button makeButton(Shell shell, String text, Listener listener) {
        Button button = new Button(shell, SWT.PUSH);
        button.setLayoutData(UIUtility.buttonGridData);
        button.setText(text);
        button.addListener(SWT.Selection, listener);
        shell.setDefaultButton(button);
        return button;
    }

    /**
     * @brief Ask for user credentials
     * 
     * Show the UI asking for the username and password of a user, whether
     * it's for a login or registration.
     */
    private void inputCredentialsDialog() {
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        final Shell inshell = UIUtility.createShell(layout, "Input Credentials");

        final Text tuser = UIUtility.labelAndText(inshell, "Username: ", SWT.BORDER);
        final Text tpass = UIUtility.labelAndText(inshell, "Password: ", SWT.BORDER | SWT.PASSWORD);

        UIUtility.addEmptyCell(inshell);

        credentialsGiven = false;

        Button submit = makeButton(inshell, "Submit", new Listener() {
            public void handleEvent(Event e) {
                user = tuser.getText();
                password = tpass.getText();
                credentialsGiven = true;
                inshell.dispose();
            }
        });
        submit.setLayoutData(UIUtility.textFieldData);

        UIUtility.startShell(inshell);
    }

    /**
     * @brief Try to login with the user credentials
     * 
     * Try to login with the username and password that the user
     * provided. If the login is successful, the password entries
     * are loaded.
     * 
     * Shows an error message if the login was unsuccessful.
     *
     * @param      user      The username
     * @param      password  The password
     *
     * @return     True if login was successful, false otherwise.
     */
    private boolean tryLogin(String user, String password) {
        try {
            passwords = new EncryptedMap(user, password);
            return true;
        } catch (Hmac.IntegrityException e) {
            UIUtility.errorMessage("Login", "Wrong Password or corrupted files.");
            return false;
        } catch (Exception e) {
            UIUtility.errorMessage("Login", e.getMessage());
            return false;
        }
    }

    /**
     * @brief Try to register a user
     * 
     * Try to register a user with username ```user``` and
     * password ```password```.
     * 
     * Shows an error message if the registration was unsuccessful.
     *
     * @param      user      The username
     * @param      password  The password
     *
     * @return     True if registration was successful, false otherwise.
     */
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

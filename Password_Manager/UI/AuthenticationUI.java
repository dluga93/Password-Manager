package Password_Manager.UI;
import Password_Manager.EncryptedMap;
import Password_Manager.Encryption.*;
import Password_Manager.Registration;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import java.nio.file.*;

public class AuthenticationUI {
    private String user, password;
    private EncryptedMap passwords;
    private boolean credentialsGiven;

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

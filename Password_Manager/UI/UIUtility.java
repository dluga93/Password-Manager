package Password_Manager.UI;

import org.eclipse.swt.*;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.custom.*;

/**
 * @brief Utility methods for UI classes
 */
class UIUtility {
	private static final Clipboard cb = new Clipboard(MainUI.display); ///< Clipboard to store the copied passwords
	public static final GridData textFieldData = new GridData(150, SWT.DEFAULT); ///< Layout for text fields
	public static final GridData buttonGridData = new GridData(80, SWT.DEFAULT); ///< Layout for buttons
	public static boolean answeredYes = false; ///< boolean storing whether the latest answer to a yes/no dialog was yes

	/**
	 * @brief Create a shell (window)
	 *
	 * @param      layout  The layout
	 * @param      title   The title
	 *
	 * @return     The created shell
	 */
	public static Shell createShell(Layout layout, String title) {
		Shell shell = new Shell(MainUI.display);
		shell.setLayout(layout);
		shell.setText(title);
		return shell;
	}

	/**
	 * @brief Start and display a shell
	 *
	 * @param      shell  The shell
	 */
	public static void startShell(Shell shell) {
		shell.pack();
		shell.open();

		while (!shell.isDisposed())
			if (!MainUI.display.readAndDispatch())
				MainUI.display.sleep();
	}
	
	/**
	 * @brief Start and display a shell with specified dimensions
	 *
	 * @param      shell   The shell
	 * @param      width   The width
	 * @param      height  The height
	 */
	public static void startShell(Shell shell, int width, int height) {
		shell.setSize(width, height);
		shell.open();

		while (!shell.isDisposed())
			if (!MainUI.display.readAndDispatch())
				MainUI.display.sleep();
	}

	/**
	 * @brief Copy a string to clipboard
	 *
	 * @param      string  The string
	 */
	public static void copyToClipboard(String string) {
		if (string != null) {
			TextTransfer textTransfer = TextTransfer.getInstance();
			cb.setContents(new Object[]{string},
						   new Transfer[]{textTransfer});
		}
	}

	/**
	 * @brief Dialog box for a yes/no question
	 *
	 * @param      title     The title of the window
	 * @param      question  The question text
	 *
	 * @return     True if answer was yes, false otherwise.
	 */
	public static boolean yesNoQuestion(String title, String question) {
		answeredYes = false;

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = true;
		final Shell shell = createShell(layout, title);

		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.CENTER;
		gridData.horizontalSpan = 2;

		Label label = new Label(shell, SWT.CENTER);
		label.setText(question);
		label.setLayoutData(gridData);

		Button yes = new Button(shell, SWT.PUSH | SWT.CENTER);
		yes.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		shell.setDefaultButton(yes);
		yes.setText("Yes");
		yes.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				answeredYes = true;
				shell.dispose();
			}
		});

		Button no = new Button(shell, SWT.PUSH | SWT.CENTER);
		no.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		shell.setDefaultButton(no);
		no.setText("No");
		no.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				answeredYes = false;
				shell.dispose();
			}
		});

		startShell(shell);
		return answeredYes;
	}

	/**
	 * @brief Display an error message
	 *
	 * @param      title    The title of the window
	 * @param      message  The error message to display
	 *
	 * @return     The ID of the button that was selected to dismiss the message box
	 */
	public static int errorMessage(String title, String message) {
		final Shell shell = UIUtility.createShell(new FillLayout(), "");
		MessageBox errorBox = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK | SWT.TITLE);
		errorBox.setText(title);
		errorBox.setMessage(message);
		return errorBox.open();
	}

	/**
	 * @brief Add an empty placeholder cell in a shell
	 *
	 * @param      shell  The shell
	 */
	public static void addEmptyCell(Shell shell) {
		@SuppressWarnings("unused")
		Label emptyCell = new Label(shell, SWT.NONE);
	}

	/**
	 * @brief Add a label and a textfield to a shell
	 *
	 * @param      shell      The shell
	 * @param      labelText  The label text
	 * @param      textFlags  The style of the textfield
	 *
	 * @return     The textfield
	 */
	public static Text labelAndText(Shell shell, String labelText, int textFlags) {
		final CLabel label = new CLabel(shell, SWT.CENTER);
		label.setText(labelText);
		final Text textField = new Text(shell, textFlags);
		textField.setLayoutData(textFieldData);
		return textField;
	}
}

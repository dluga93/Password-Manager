package PwdManager.UI;

import org.eclipse.swt.*;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.custom.*;

class UIUtility {
	private static final Clipboard cb = new Clipboard(MainUI.display);
	public static final GridData textFieldData = new GridData(100, SWT.DEFAULT);
	public static final GridData buttonGridData = new GridData(80, SWT.DEFAULT);
	public static boolean answeredYes = false;

	public static Shell createShell(Layout layout, String title) {
		Shell shell = new Shell(MainUI.display);
		shell.setLayout(layout);
		shell.setText(title);
		return shell;
	}

	public static void startShell(Shell shell) {
		shell.pack();
		shell.open();

		while (!shell.isDisposed())
			if (!MainUI.display.readAndDispatch())
				MainUI.display.sleep();
	}
	
	public static void startShell(Shell shell, int width, int height) {
		shell.setSize(width, height);
		shell.open();

		while (!shell.isDisposed())
			if (!MainUI.display.readAndDispatch())
				MainUI.display.sleep();
	}

	public static void copyToClipboard(String string) {
		if (string != null) {
			TextTransfer textTransfer = TextTransfer.getInstance();
			cb.setContents(new Object[]{string},
						   new Transfer[]{textTransfer});
		}
	}

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

	public static int errorMessage(String title, String message) {
		final Shell shell = UIUtility.createShell(new FillLayout(), "");
		MessageBox errorBox = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK | SWT.TITLE);
		errorBox.setText(title);
		errorBox.setMessage(message);
		return errorBox.open();
	}

	public static void addEmptyCell(Shell shell) {
		@SuppressWarnings("unused")
		Label emptyCell = new Label(shell, SWT.NONE);
	}

	public static Text labelAndText(Shell shell, String labelText, int textFlags) {
		final CLabel label = new CLabel(shell, SWT.CENTER);
		label.setText(labelText);
		final Text textField = new Text(shell, textFlags);
		textField.setLayoutData(UIUtility.textFieldData);
		return textField;
	}
}
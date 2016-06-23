package PwdManager.UI;

import org.eclipse.swt.*;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;

public class UIUtility {
	private static boolean answeredYes = false;
	private static final Clipboard cb = new Clipboard(MainUI.display);

	public static Shell createShell(Layout layout) {
		Shell shell = new Shell(MainUI.display);
		shell.setLayout(layout);
		return shell;
	}

	public static void startShell(Shell shell) {
		shell.pack();
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

	public static boolean areYouSure(String title) {
		answeredYes = false;

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = true;
		final Shell shell = createShell(layout);
		shell.setText(title);

		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.CENTER;
		gridData.horizontalSpan = 2;

		Label label = new Label(shell, SWT.CENTER);
		label.setText("Are you sure?");
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
}
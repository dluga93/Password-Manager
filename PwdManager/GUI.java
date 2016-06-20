/*
A lot of this class, (the GUI functionality) is based on
a template given for an assignment in my Security course.
*/
package PwdManager;

import org.eclipse.swt.*;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.graphics.Point;

import java.util.Map;

/**
 * The main client.
 */
public class GUI {
	private static String hack_string = null; /* HACK due to Java's poor closure support */
	private static String password = null;
	private static String user = null;
	private static boolean triedLogIn = false;
	private static EncryptedMap passwords = null;
	private static final Display display = new Display ();
	private static final Shell shell = new Shell(display);
	private static final List list = new List(shell, SWT.BORDER);

	public static void welcomeScreen() {
		final Shell shell = new Shell(display);
		shell.setLayout (new FillLayout());

		Button login = new Button(shell, SWT.PUSH);
		login.setText("Log In\n");
		login.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				shell.dispose();
				login();
				triedLogIn = true;
			}
		});

		Button register = new Button(shell, SWT.PUSH);
		register.setText("Register");
		register.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				shell.dispose();
				register();
				triedLogIn = false;
			}
		});

		shell.pack();
		shell.open();

		while (!shell.isDisposed())
			if (!display.readAndDispatch())
				display.sleep();
	}

	public static void register() {
		final Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());

		final Text tuser  = new Text(shell, SWT.BORDER);
		final Text tpass  = new Text(shell, SWT.BORDER | SWT.PASSWORD);

		Button reg = new Button(shell, SWT.PUSH);
		reg.setText("Register");
		reg.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				password = tpass.getText();
				user = tuser.getText();
				shell.dispose();
			}
		});

		shell.pack();
		shell.open();

		while (!shell.isDisposed())
			if (!display.readAndDispatch())
				display.sleep();
	}

	public static void login() {
		final Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());

		final Text tuser  = new Text(shell, SWT.BORDER);
		final Text tpass  = new Text(shell, SWT.BORDER | SWT.PASSWORD);

		Button log = new Button(shell, SWT.PUSH);
		log.setText("Login");
		log.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				password = tpass.getText();
				user = tuser.getText();
				shell.dispose();
			}
		});

		shell.pack();
		shell.open();

		while (!shell.isDisposed())
			if (!display.readAndDispatch())
				display.sleep();
	}

	public static void addPasswd() {
		final Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());

		final Text tsite  = new Text(shell, SWT.BORDER);
		final Text tpass  = new Text(shell, SWT.BORDER | SWT.PASSWORD);

		Button log = new Button(shell, SWT.PUSH);
		log.setText("Add");
		log.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				String website = tsite.getText();
				String password = tpass.getText();
				try {
					passwords.addEntry(website, password);
					list.add(website);
				} catch (Exception exc) {
					Logger.logError("Can't add new password. File corrupted.", exc);
					System.exit(1);
				}
				// TODO: update view with new password
				shell.dispose();
			}
		});

		shell.pack();
		shell.open();

		while (!shell.isDisposed())
			if (!display.readAndDispatch())
				display.sleep();
	}
	
	public static void askPasswd() {
		final Shell shell = new Shell(display);
		shell.setLayout (new FillLayout());
		
		Label tprompt = new Label(shell, 0);
		tprompt.setText("Log In");
		
		final Text tpass  = new Text(shell, SWT.BORDER | SWT.PASSWORD);
		final Text tuser  = new Text(shell, SWT.BORDER);
		
		Button bok = new Button(shell, SWT.PUSH);
		bok.setText("OK");
		bok.addListener(SWT.Selection, new Listener (){
			public void handleEvent (Event e) {
				password = tpass.getText();
				user = tuser.getText();
				shell.dispose();
			}});

		shell.setDefaultButton(bok);
		
		shell.pack ();
		shell.open();
		
		while(!shell.isDisposed ())
			if (!display.readAndDispatch ())
				display.sleep();
	}//end askPasswd()
	
	public static String editPasswd(final Map<String,String> m, final String name){
		final Shell shell = new Shell(display);
		shell.setText ("Edit password");
		shell.setLayout (new FormLayout());
		
		hack_string = null;
		
		Control tname;
		final Text tname_text;
		if(name == null) {
			tname = tname_text = new Text(shell, SWT.BORDER);
			tname_text.setText("name");
		}else{
			Label lab  = new Label(shell, 0);
			tname      = lab;
			tname_text = null;
			lab.setText(name);
		}
		
		FormData layname = new FormData();
		layname.left     = new FormAttachment(0,  0);
		//layname.top      = new FormAttachment(0,  0);
		layname.right    = new FormAttachment(100,0);
		tname.setLayoutData(layname);
		
		/* password field[s] and show button */
		final Text tpass  = new Text(shell, SWT.BORDER | SWT.PASSWORD);
		final Text tpass2 = new Text(shell, SWT.BORDER);
		
		if(name != null){
			String s = m.get(name);
			
			if(s != null){
				tpass.setText(s);
				tpass2.setText(s);
			}
		}
		
		final Button bshow = new Button(shell, SWT.CHECK);
		bshow.setText("show");
		FormData layshow   = new FormData();
		layshow.top        = new FormAttachment(tname, 0);
		layshow.right      = new FormAttachment(100,0);
		bshow.setLayoutData(layshow);
		
		FormData laypass  = new FormData();
		laypass.left      = new FormAttachment(0,  0);
		laypass.top       = new FormAttachment(tname, 0);
		laypass.right     = new FormAttachment(bshow,0);
		tpass.setLayoutData(laypass);
		tpass2.setLayoutData(laypass);
		tpass2.setVisible(false);
		
		bshow.addListener(SWT.Selection, new Listener(){
			boolean oldb = false;
			public void handleEvent (Event e){
				boolean b = bshow.getSelection();
				
				if(oldb != b){
					if(b)
						tpass2.setText(tpass.getText());
					else
						tpass.setText(tpass2.getText());
					
					tpass.setVisible(!b);
					tpass2.setVisible(b);
				}
				oldb = b;
			}});
		
		/* OK and Cancel buttons */
		Button bcanc = new Button(shell, SWT.PUSH);
		bcanc.setText("Cancel");
		FormData laycanc  = new FormData();
		laycanc.left      = new FormAttachment(0,     0);
		laycanc.top       = new FormAttachment(tpass, 0);
		laycanc.right     = new FormAttachment(50,    0);
		bcanc.setLayoutData(laycanc);
		bcanc.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event e){
				shell.dispose();
			}});
		
		Button bok = new Button(shell, SWT.PUSH);
		bok.setText("OK");
		FormData layok  = new FormData();
		layok.left      = new FormAttachment(50,    0);
		layok.top       = new FormAttachment(tpass, 0);
		layok.right     = new FormAttachment(100,   0);
		bok.setLayoutData(layok);
		bok.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event e){
				String key = hack_string = (name != null) ? name : tname_text.getText();
				String value = (bshow.getSelection() ? tpass2 : tpass).getText();
				m.put(key,value);
				shell.dispose();
			}});
		
		shell.setDefaultButton(bcanc);
		
		/* go */
		// shell.setSize(200, 100);
		shell.pack ();
		Point p = shell.getSize();
		p.x = 200;
		shell.setSize(p);
		
		shell.open();
		
		while(!shell.isDisposed())
			if (!display.readAndDispatch ())
				display.sleep ();
		
		return hack_string;
	}//end editPasswd()
	
	public static void main(String[] args) {
		
		//SecureBlobIO.ClientParams cp = null;
		//SecureBlobIO sbio_ = null;
		welcomeScreen();
		if (user == null)
			return;
		else if (triedLogIn)
			passwords = new EncryptedMap(user, password);
		else {
			Registration.registerUser(user, password);
			passwords = new EncryptedMap(user, password);
		}
		/*
		for(int i=0; i<3; i++){
			String passwd = askPasswd(display, "Enter your master password: ");
			
			Socket sock = new Socket("localhost", NetworkedMapServer.DEFAULT_PORT);
			
			try{
				cp = new SecureBlobIO.ClientParams(passwd);
				sbio_ = new SecureBlobIO(new IOBlobIO(sock), cp);
				
				break;
			}catch (CorruptMessageException e){
				sock.close();
				// try again
			}
		}//end for - i
		
		if (sbio_ == null)
			Debug.choke("three strikes and you're out");
		
		final SecureBlobIO sbio = sbio_;
		
		final StringMap m = new StringMap(new EncryptedMap(new NetworkedMap(sbio), cp.getMaster()));
		// final StringMap m = new StringMap(new FileMap("test"));
		
		/* set up SWT */
		final Clipboard cb = new Clipboard(display);
		shell.setText ("Password Manager");
		shell.setLayout (new FormLayout());
		
		/* make the menu bar */
		Menu bar = new Menu (shell, SWT.BAR);
		shell.setMenuBar (bar);

		//final TreeSet<String> names = m.keySet();
		
		{ /* file menu */
			MenuItem menu = new MenuItem (bar, SWT.CASCADE);
			menu.setText ("&File");
			Menu submenu = new Menu (shell, SWT.DROP_DOWN);
			menu.setMenu (submenu);
			
			MenuItem newItem = new MenuItem (submenu, SWT.PUSH);
			newItem.setText ("&New password\tCtrl+N");
			newItem.setAccelerator (SWT.MOD1 + 'N');
			newItem.addListener(SWT.Selection, new Listener () {
				public void handleEvent (Event e) {
					addPasswd();
					/*if (s != null && !names.contains(s)) {
						names.add(s);
						int index = names.headSet(s).size();
						list.add(s,index);
					}*/
				}
			});
		}
			/*
			MenuItem passItem = new MenuItem (submenu, SWT.PUSH);
			passItem.setText ("Change master &password");
			passItem.addListener(SWT.Selection, new Listener () {
				public void handleEvent (Event e) {
					try {
						sbio.setPassword(askPasswd(display,
												   "Enter a new master password: "),
										 false);
					} catch (IOException ee) {
						Debug.warn(ee);
					}
				}
			});
			
			MenuItem closeItem = new MenuItem (submenu, SWT.PUSH);
			closeItem.setText ("&Close\tCtrl+W");
			closeItem.setAccelerator (SWT.MOD1 + 'W');
			closeItem.addListener(SWT.Selection, new Listener () {
				public void handleEvent (Event e) {
					shell.dispose();
				}
			});
			
			MenuItem quitItem = new MenuItem (submenu, SWT.PUSH);
			quitItem.setText ("&Quit\tCtrl+Q");
			quitItem.setAccelerator (SWT.MOD1 + 'Q');
			quitItem.addListener(SWT.Selection, new Listener () {
				public void handleEvent (Event e) {
					shell.dispose();
				}});
		}*/
		
		{ /* edit menu */
			MenuItem menu = new MenuItem (bar, SWT.CASCADE);
			menu.setText ("&Edit");
			Menu submenu = new Menu (shell, SWT.DROP_DOWN);
			menu.setMenu (submenu);
			
			MenuItem copyItem = new MenuItem (submenu, SWT.PUSH);
			copyItem.setText ("&Copy\tCtrl+C");
			copyItem.setAccelerator (SWT.MOD1 + 'C');
			copyItem.addListener(SWT.Selection, new Listener () {
				public void handleEvent (Event e) {
					String[] keys = list.getSelection();
					for (String key : keys) {
						String value = passwords.getWebsitesPassword(key);
						if (value != null) {
							TextTransfer textTransfer = TextTransfer.getInstance();
							cb.setContents(new Object[]{value},
										   new Transfer[]{textTransfer});
						}
					}
				}});
		}
		
		/* create the list */
		/*
		for(String s : names)
			list.add(s);
		
		list.addListener(SWT.KeyDown, new Listener(){
			public void handleEvent(Event e){
				switch (e.character){
					case SWT.DEL:
					{
						String[] keys = list.getSelection();
						for (String key : keys) {
							m.remove(key);
							names.remove(key);
							list.remove(key);
						}
						break;
					}
						
					case SWT.LF:
					case SWT.CR:
					{
						String[] keys = list.getSelection();
						for (String key : keys) {
							editPasswd(display, m, key);
						}
						break;
					}
				}
			}
		});
		*/
		for (String entry : passwords.getWebsites()) {
			list.add(entry);
		}

		FormData lay = new FormData();
		lay.left   = new FormAttachment(0,  0);
		lay.top    = new FormAttachment(0,  0);
		lay.bottom = new FormAttachment(100,0);
		lay.right  = new FormAttachment(100,0);
		list.setLayoutData(lay);
		
		/* go */
		shell.setSize (200, 500);
		shell.pack ();
		shell.open ();
		
		while (!shell.isDisposed ())
            if (!display.readAndDispatch ())
                display.sleep ();
		
		display.dispose ();
	}//end main()
}//end class

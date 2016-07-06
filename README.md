# Password-Manager
A simple password manager in Java. You can use this application to store passwords of websites you use,
so you don't have to remember them.

## Usage

Create an account by clicking on **Register** and input a username and a master password. This master password will be used to login,
so this is the only password you need to remember.

After registering, you will be logged in, and you will be shown the list of your website passwords (which will be empty if you just registered). Now you can add website passwords. You can do this by clicking **File**->**New Password** in the menu, or just pressing *Ctrl+N* on your keyboard. A window opens, where you input the website name, and the password you want to associate with that website. You click **Add** and the website appears in the list.

In the list, you can only see the website names, not the actual passwords. To get the password, you click on a website, and press *Ctrl+C* or **Edit**->**Copy** in the menu. You can now paste that website's password in a textbox or wherever you want.

## Technical details

Passwords are stored locally, meaning you don't need an internet connection to use this application.
The passwords, of course, aren't stored in plaintext. Currently, 128-bit AES is used for encryption, and HMAC-SHA-256 for HMAC.

The application creates a directory and four files for each user account. If you ever want to backup your passwords, or transport them to another device, you just move these five items in the same directory as the application in the new device.

The directory contains a file for each website you have added in the application. These files contain the macced and encrypted (in that order) passwords.

The other four files contain:
<ul>
  <li>
    Master key salt
  </li>
  <li>
    Password-encrypted master key
  </li>
  <li>
    Hmac key salt
  </li>
  <li>
    Password-encrypted hmac key
  </li>
</ul>

The master and hmac keys are encrypted with a third key generated from the stored salt and the master password using <a href="https://en.wikipedia.org/wiki/PBKDF2">PBKDF2</a> with HMAC-SHA1. If the user wants to change the master password, the application only re-encrypts the master and hmac keys, and doesn't need to change the stored website passwords.

## Build

The application was developped using Java and SWT for the user interface. You can download the source code, put it into a *PwdManager* directory, and add the files in a project in the Eclipse IDE. You also need to download the <a href="http://www.eclipse.org/swt/">SWT</a> (I'm using version 4.5), import it in Eclipse, and add it to the PwdManager project's build path.

To add a project to the build path, right-click the project's name in the Package Explorer and go to **Properties**. Click Java Build Path on the left, go to the Projects tab, and click **Add**. In the window that opens, you select the SWT project and click OK.

That's it. Just make sure you got the SWT version that matches your JVM. They should both be 64-bit or 32-bit.

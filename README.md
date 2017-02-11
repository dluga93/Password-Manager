# Password-Manager
A simple password manager in Java. 

As you might know, for security reasons, it is not recommended to use the same password in multiple websites. But if you use a lot of websites, it can get hard to remember all your different passwords. That's where password managers help. With password managers, you only need to remember one password. The program remembers the rest of them for you.

## Build and Run

To build the program, follow these steps.

1. Download the source code and extract it.
2. Download <a href="http://www.eclipse.org/swt/">SWT</a>. Make sure it matches your JVM; they should both be 64-bit or 32-bit.
3. Put the SWT jar file in the classpath.
4. Run the following command:

```
javac -cp <swt_jar_path>:<code_path> <code_path>/Password_Manager/*.java <code_path>/Password_Manager/UI/*.java <code_path>/Password_Manager/Encryption/*.java
```
where ```<code_path>``` is the path to the source code and ```<swt_jar_path>``` is the path to the downloaded swt.jar file.

5. Now you can run the program with:

```
java -cp <swt_jar_path>:<code_path> <code_path>/Password_Manager.Client
```

## Usage

Create an account by clicking on **Register** and input a username and a master password. This master password is the only one you will need to remember.

After registering, you will be logged in, and you will be shown the list of your website passwords (which will be empty if you just registered). Now you can add website passwords. You can do this by clicking **File**->**New Password** in the menu, or just pressing *Ctrl+N* on your keyboard. You input the website name, and the password you want to associate with that website. You click **Add** and the entry is saved.

In the list, you can only see the website names, not the actual passwords. To get the password, you click on a website, and press *Ctrl+C* or **Edit**->**Copy** in the menu. The password is now in your clipboard and you can directly paste it in a textbox or wherever you want.

## Technical details

Passwords are stored locally, meaning you don't need an internet connection to use this application.
The passwords, of course, aren't stored in plaintext. Currently, 128-bit AES is used for encryption, and HMAC-SHA-256 for authentication.

The application creates a directory and three files for each user account. If you ever want to backup your passwords, or transport them to another device, you just move these five items in the same directory as the application in the new device.

The directory contains a file for each website entry you have added in the application. These files contain the macced and encrypted (in that order) passwords.

The other three files contain:

* Master key salt
* Hmac key salt
* Password-encrypted master key and hmac key

The master and hmac keys are encrypted with a third key generated from the stored salt and the master password using <a href="https://en.wikipedia.org/wiki/PBKDF2">PBKDF2</a> with HMAC-SHA1. If the user wants to change the master password, the application makes it easy by only re-encrypting the master and hmac keys. This means this operation won't take longer if you have a lot of entries saved.
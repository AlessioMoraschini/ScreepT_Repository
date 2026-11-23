/**
 *
 * =========================================================================================
 *  Copyright (C) 2019-2020
 *
 *  AM-Design-Development - (Alessio Moraschini) - All Rights Reserved
 * =========================================================================================
 *
 * You should have received a copy of the license with this file.
 * If not, please write to: info@am-design-development.com, or visit : https://www.am-design-development.com
 */
package gui.texteditor.commons.encrypted;

import java.io.File;
import java.io.IOException;

/**
 * Lets the file editor open and save encrypted text files (the Crypter format) as normal tabs: the file is decrypted in memory
 * when opened, every save encrypts the text again over the same file. The plain text never goes to disk. The editor knows nothing
 * about the cipher: the application gives an implementation through {@code ITabbedEditorOwner.getEncryptedFileSupport()}.
 */
public interface EncryptedFileSupport {

	/** Cheap check of the first bytes (no password needed): true if the file is an encrypted file this support can handle */
	boolean isEncrypted(File file);

	/**
	 * Asks the password and decrypts the file. Returns null if the user cancelled or the file cannot be edited as text (the user
	 * has already been told why). The editor never shows the encrypted bytes as text.
	 */
	Opened open(File file);

	/** Decrypted text of a file and the binding that saves it back */
	final class Opened {
		public final String text;
		public final Binding binding;

		public Opened(String text, Binding binding) {
			this.text = text;
			this.binding = binding;
		}
	}

	/** Keeps the secrets of an encrypted tab and writes its text back, encrypted */
	interface Binding {

		/** Encrypts the text and replaces the target (atomically when possible): the target is left untouched on failure */
		void write(String text, File target) throws IOException;

		/** One line for the user, like "AES-256-GCM / PBKDF2" */
		String describe();

		/** Wipes the secrets from memory (tab closed) */
		void close();
	}
}

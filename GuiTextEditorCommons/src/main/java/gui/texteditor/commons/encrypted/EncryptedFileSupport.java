/**
 *
 * =========================================================================================
 *  Copyright (C) 2019-2020
 *
 *  AM-Design-Development - (Alessio Moraschini)
 * =========================================================================================
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
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

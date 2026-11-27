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
package screept.gui.utils.texteditor;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.BoxLayout;

import org.apache.log4j.Logger;

import gui.texteditor.commons.encrypted.EncryptedFileSupport;
import screept.crypter.engine.CryptoEngine;
import screept.crypter.engine.CryptoException;
import screept.crypter.engine.CryptoKey;
import screept.crypter.engine.CryptoProfile;
import various.common.light.i18n.I18n;

/**
 * Encrypted text files in the file editor (Crypter format, password only): the file is decrypted in memory when opened and every
 * save encrypts the text again over the same file. Files that need a key image, are not text or are too big stay for the Crypter.
 */
public class EncryptedFilesScreepT implements EncryptedFileSupport {

	private static final Logger logger = Logger.getLogger(EncryptedFilesScreepT.class);

	/** Bigger files are not loaded as text (they go through the Crypter) */
	private static final long MAX_BYTES = 64L * 1024 * 1024;
	private static final int HEAD_BYTES = 256;

	private final Component parent;
	private final java.util.function.BooleanSupplier startupPhase;

	/** @param startupPhase true while the editor restores the previous session: no password is asked then, the file just stays closed */
	public EncryptedFilesScreepT(Component parent, java.util.function.BooleanSupplier startupPhase) {
		this.parent = parent;
		this.startupPhase = startupPhase;
	}

	@Override
	public boolean isEncrypted(File file) {
		try (InputStream in = Files.newInputStream(file.toPath())) {
			byte[] head = new byte[CryptoEngine.MAGIC.length];
			return in.read(head) == head.length && CryptoEngine.isEngineData(head);
		} catch (IOException e) {
			return false;
		}
	}

	@Override
	public Opened open(File file) {
		if (startupPhase.getAsBoolean()) {
			return null; // session restore: never a password prompt at launch
		}
		try {
			if (file.length() > MAX_BYTES) {
				info(I18n.t("enc.not_text"));
				return null;
			}
			byte[] encrypted = Files.readAllBytes(file.toPath());
			CryptoProfile profile = CryptoEngine.readProfile(Arrays.copyOf(encrypted, Math.min(HEAD_BYTES, encrypted.length)));
			if (profile.needsKeyImage()) {
				info(I18n.t("enc.key_image_needed"));
				return null;
			}
			while (true) {
				char[] password = askPassword(I18n.fmt("enc.unlock_prompt", file.getName()));
				if (password == null) {
					return null; // cancelled by user
				}
				CryptoKey key = CryptoKey.of(password);
				try {
					byte[] plain = withWaitCursor(parent, () -> CryptoEngine.decrypt(encrypted, key));
					String text = strictUtf8(plain);
					Arrays.fill(plain, (byte) 0);
					if (text == null) {
						key.clear();
						info(I18n.t("enc.not_text"));
						return null;
					}
					return new Opened(text, new CryptoBinding(key, profile, parent));
				} catch (CryptoException e) {
					key.clear();
					info(I18n.t("enc.wrong_password"));
				}
			}
		} catch (Exception e) {
			logger.error("Cannot open the encrypted file " + file, e);
			info(I18n.t("enc.wrong_password"));
			return null;
		}
	}

	/** Asks twice for a new password (with a warning if it is short); null if cancelled */
	public Binding newBinding(String fileName) {
		char[] first = askPassword(I18n.fmt("enc.set_password", fileName));
		if (first == null) {
			return null;
		}
		if (first.length == 0) {
			return null;
		}
		char[] second = askPassword(I18n.t("enc.retype"));
		if (second == null || !Arrays.equals(first, second)) {
			if (second != null) {
				info(I18n.t("enc.mismatch"));
			}
			Arrays.fill(first, '\0');
			if (second != null) {
				Arrays.fill(second, '\0');
			}
			return null;
		}
		Arrays.fill(second, '\0');
		if (first.length < 8 && JOptionPane.showConfirmDialog(parent, I18n.t("enc.weak"), I18n.t("enc.title"), JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
			Arrays.fill(first, '\0');
			return null;
		}
		return new CryptoBinding(CryptoKey.of(first), CryptoProfile.standard(), parent);
	}

	/**
	 * Key derivation takes about a second by design (600000 PBKDF2 rounds). The work runs on its own thread while a small modal
	 * window with an indeterminate progress bar is shown (the interface keeps repainting, so ScreepT does not look frozen);
	 * the window closes by itself when the work ends, whatever the outcome.
	 */
	static <T> T withWaitCursor(Component parent, java.util.concurrent.Callable<T> work) throws CryptoException {
		final java.awt.Window window = parent == null ? null : (parent instanceof java.awt.Window ? (java.awt.Window) parent : javax.swing.SwingUtilities.getWindowAncestor(parent));
		final Object[] box = new Object[2];
		if (!javax.swing.SwingUtilities.isEventDispatchThread()) {
			return run(work, box);
		}
		final javax.swing.JDialog dialog = new javax.swing.JDialog(window, I18n.t("enc.title"), java.awt.Dialog.ModalityType.APPLICATION_MODAL);
		dialog.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
		javax.swing.JProgressBar bar = new javax.swing.JProgressBar();
		bar.setIndeterminate(true);
		JPanel content = new JPanel(new java.awt.BorderLayout(0, 10));
		content.setBorder(javax.swing.BorderFactory.createEmptyBorder(16, 20, 16, 20));
		content.add(new JLabel(I18n.t("enc.working")), java.awt.BorderLayout.NORTH);
		content.add(bar, java.awt.BorderLayout.CENTER);
		dialog.setContentPane(content);
		dialog.setResizable(false);
		dialog.pack();
		dialog.setMinimumSize(new java.awt.Dimension(320, dialog.getHeight()));
		dialog.setLocationRelativeTo(window);
		java.awt.Cursor previous = window == null ? null : window.getCursor();
		if (window != null) {
			window.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
		}
		Thread worker = new Thread(() -> {
			try {
				box[0] = work.call();
			} catch (Throwable e) {
				box[1] = e;
			} finally {
				javax.swing.SwingUtilities.invokeLater(dialog::dispose);
			}
		}, "enc-key-derivation");
		worker.setDaemon(true);
		worker.start();
		try {
			dialog.setVisible(true); // returns when the worker has disposed it
			worker.join();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} finally {
			if (window != null) {
				window.setCursor(previous);
			}
		}
		return result(box);
	}

	private static <T> T run(java.util.concurrent.Callable<T> work, Object[] box) throws CryptoException {
		try {
			box[0] = work.call();
		} catch (Throwable e) {
			box[1] = e;
		}
		return result(box);
	}

	@SuppressWarnings("unchecked")
	private static <T> T result(Object[] box) throws CryptoException {
		if (box[1] instanceof CryptoException) {
			throw (CryptoException) box[1];
		}
		if (box[1] instanceof RuntimeException) {
			throw (RuntimeException) box[1];
		}
		if (box[1] != null) {
			throw new IllegalStateException((Throwable) box[1]);
		}
		return (T) box[0];
	}

	private char[] askPassword(String prompt) {
		JPasswordField field = new JPasswordField(24);
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(new JLabel(prompt));
		panel.add(field);
		int choice = JOptionPane.showConfirmDialog(parent, panel, I18n.t("enc.title"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		return choice == JOptionPane.OK_OPTION ? field.getPassword() : null;
	}

	private void info(String message) {
		JOptionPane.showMessageDialog(parent, message, I18n.t("enc.title"), JOptionPane.INFORMATION_MESSAGE);
	}

	/** Text of the bytes, or null if they are not valid UTF-8 (a lossy decode followed by a save would destroy the data) */
	private static String strictUtf8(byte[] bytes) {
		try {
			return StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
					.onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(bytes)).toString();
		} catch (CharacterCodingException e) {
			return null;
		}
	}

	/** Password and profile of an encrypted tab: every save is a new encryption (new salt) of the whole text */
	private static final class CryptoBinding implements Binding {

		private final CryptoKey key;
		private final CryptoProfile profile;
		private final Component parent;

		CryptoBinding(CryptoKey key, CryptoProfile profile, Component parent) {
			this.key = key;
			this.profile = profile;
			this.parent = parent;
		}

		@Override
		public void write(String text, File target) throws IOException {
			byte[] plain = text.getBytes(StandardCharsets.UTF_8);
			File temp = new File(target.getAbsoluteFile().getParentFile(), target.getName() + ".tmp~");
			try {
				byte[] encrypted = withWaitCursor(parent, () -> CryptoEngine.encrypt(plain, key, profile));
				Files.write(temp.toPath(), encrypted);
				try {
					Files.move(temp.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
				} catch (AtomicMoveNotSupportedException e) {
					Files.move(temp.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
				}
			} catch (CryptoException e) {
				throw new IOException("Encryption failed", e);
			} finally {
				Arrays.fill(plain, (byte) 0);
				Files.deleteIfExists(temp.toPath());
			}
		}

		@Override
		public String describe() {
			return profile.describe();
		}

		@Override
		public void close() {
			key.clear();
		}
	}
}

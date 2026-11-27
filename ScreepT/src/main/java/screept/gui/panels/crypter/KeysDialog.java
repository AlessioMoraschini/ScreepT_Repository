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
package screept.gui.panels.crypter;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import gui.commons.dialogutils.NativeFileChooser;
import net.miginfocom.swing.MigLayout;
import screept.crypter.engine.CryptoException;
import screept.crypter.engine.Identity;
import screept.crypter.impl.manager.CryptoMode;

/**
 * Public keys of the Crypter: the user's own key pair (private key in a password protected file) and the people
 * files are sent to. Everything lives in one folder ({@link #folder()}), easy to back up.
 */
public final class KeysDialog {

	/** Folder override for tests. */
	public static final String FOLDER_PROPERTY = "screept.keysDir";
	private static final String MY_PUBLIC = "my.public";
	private static final String KEY = "key.";
	private static final String USE = "use.";

	private KeysDialog() {
	}

	// ---------------------------------------------------------------- store

	public static File folder() {
		String p = System.getProperty(FOLDER_PROPERTY);
		return p != null ? new File(p) : new File(System.getProperty("user.home"), "ScreepT_keys");
	}

	public static File identityFile() {
		return new File(folder(), "my_private_key.scptkey");
	}

	static Properties loadStore() {
		Properties p = new Properties();
		File f = new File(folder(), "contacts.properties");
		if (f.isFile()) {
			try (InputStream in = new FileInputStream(f)) {
				p.load(in);
			} catch (IOException e) {
				// unreadable: treated as empty, nothing is overwritten until the user changes something
			}
		}
		return p;
	}

	static void saveStore(Properties p) throws IOException {
		File dir = folder();
		if (!dir.isDirectory() && !dir.mkdirs()) {
			throw new IOException("Cannot create " + dir);
		}
		File tmp = new File(dir, "contacts.properties.tmp");
		try (OutputStream out = new FileOutputStream(tmp)) {
			p.store(out, "ScreepT public keys (public, not secret)");
		}
		java.nio.file.Files.move(tmp.toPath(), new File(dir, "contacts.properties").toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
	}

	/** name -> public key text, sorted. */
	static Map<String, String> contacts(Properties p) {
		Map<String, String> m = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
		for (String k : p.stringPropertyNames()) {
			if (k.startsWith(KEY)) {
				m.put(k.substring(KEY.length()), p.getProperty(k));
			}
		}
		return m;
	}

	/**
	 * Public keys to encrypt for: the user's own (so that the sender can open the file too) and the selected people.
	 * Null, after telling why, if the user has no keys yet.
	 */
	public static List<byte[]> recipients(Component parent) {
		Properties p = loadStore();
		String mine = p.getProperty(MY_PUBLIC);
		if (mine == null || !identityFile().isFile()) {
			JOptionPane.showMessageDialog(parent, "Create your keys first (Keys... > Create my keys).", "Public keys", JOptionPane.WARNING_MESSAGE);
			return null;
		}
		List<byte[]> out = new ArrayList<byte[]>();
		try {
			out.add(Identity.parsePublicKey(mine));
			for (Map.Entry<String, String> c : contacts(p).entrySet()) {
				if (Boolean.parseBoolean(p.getProperty(USE + c.getKey(), "false"))) {
					out.add(Identity.parsePublicKey(c.getValue()));
				}
			}
		} catch (CryptoException e) {
			JOptionPane.showMessageDialog(parent, "A saved public key is damaged: " + e.getMessage(), "Public keys", JOptionPane.ERROR_MESSAGE);
			return null;
		}
		return out;
	}

	public static int selectedCount() {
		Properties p = loadStore();
		int n = 0;
		for (String c : contacts(p).keySet()) {
			n += Boolean.parseBoolean(p.getProperty(USE + c, "false")) ? 1 : 0;
		}
		return n;
	}

	// ---------------------------------------------------------------- other Crypter helpers

	/** The Legacy modes are kept only to open old files. @return true if the operation must stop */
	public static boolean refuseLegacyEncryption(Component parent, CryptoMode mode) {
		if (mode != null && mode.profile == null) {
			JOptionPane.showMessageDialog(parent,
					"<html><body style='width:360px'>The Legacy modes are kept only to <b>decrypt</b> old files: they have no tampering detection and "
							+ "use only 16 characters of the password.<br><br>To encrypt, choose a Secure mode (for example AES-256-GCM).</body></html>",
					"Legacy mode", JOptionPane.WARNING_MESSAGE);
			return true;
		}
		return false;
	}

	/** Asks for the cover picture of the steganography output; null if cancelled. */
	public static File chooseCover(Component parent) {
		JFileChooser fc = new NativeFileChooser();
		fc.setDialogTitle("Picture to hide the encrypted data in (a colour photo)");
		fc.setFileFilter(new FileNameExtensionFilter("Pictures (png, jpg, bmp)", "png", "jpg", "jpeg", "bmp"));
		return fc.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION ? fc.getSelectedFile() : null; // cancelled by user
	}

	// ---------------------------------------------------------------- dialog

	public static void show(Component parent) {
		final JPanel root = new JPanel(new MigLayout("insets 10, gapy 6, wrap 1, fillx", "[grow, fill]"));
		Runnable[] refresh = new Runnable[1];
		refresh[0] = () -> {
			root.removeAll();
			build(root, parent, refresh[0]);
			root.revalidate();
			Window w = SwingUtilities.getWindowAncestor(root);
			if (w != null) {
				w.pack();
			}
		};
		build(root, parent, refresh[0]);
		JOptionPane.showMessageDialog(parent, root, "Keys - share files without a password", JOptionPane.PLAIN_MESSAGE);
	}

	private static void build(JPanel root, Component parent, Runnable refresh) {
		Properties p = loadStore();
		String mine = p.getProperty(MY_PUBLIC);
		boolean haveKeys = mine != null && identityFile().isFile();

		root.add(section("My keys"));
		if (haveKeys) {
			root.add(hint("Your public key: give it to whoever wants to send you files. It is not secret."));
			JTextField pub = new JTextField(mine);
			pub.setEditable(false);
			JButton copy = new JButton("Copy");
			copy.addActionListener(e -> Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(mine), null));
			JPanel row = new JPanel(new MigLayout("insets 0", "[grow, fill][]"));
			row.add(pub, "wmin 360");
			row.add(copy);
			root.add(row);
		} else {
			root.add(hint("You have no keys yet. Create them to receive files encrypted for you, and to send files to others."));
		}
		JButton create = new JButton(haveKeys ? "Create new keys..." : "Create my keys...");
		create.addActionListener(e -> {
			if (createKeys(parent, haveKeys)) {
				refresh.run();
			}
		});
		root.add(create, "growx 0");

		root.add(section("People you send files to (ticked = the next files are encrypted for them too)"), "gaptop 10");
		Map<String, String> contacts = contacts(p);
		if (contacts.isEmpty()) {
			root.add(hint("Nobody yet: add the public key that a person gave you."));
		}
		for (Map.Entry<String, String> c : contacts.entrySet()) {
			final String name = c.getKey();
			JCheckBox use = new JCheckBox(name, Boolean.parseBoolean(p.getProperty(USE + name, "false")));
			use.setToolTipText(c.getValue());
			use.addActionListener(e -> update(parent, s -> s.setProperty(USE + name, String.valueOf(use.isSelected()))));
			JButton remove = new JButton("Remove");
			remove.addActionListener(e -> {
				if (JOptionPane.showConfirmDialog(parent, "Remove " + name + "?", "Keys", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
					update(parent, s -> {
						s.remove(KEY + name);
						s.remove(USE + name);
					});
					refresh.run();
				}
			});
			JPanel row = new JPanel(new MigLayout("insets 0", "[grow, fill][]"));
			row.add(use);
			row.add(remove);
			root.add(row);
		}
		JButton add = new JButton("Add a person...");
		add.addActionListener(e -> {
			if (addContact(parent)) {
				refresh.run();
			}
		});
		root.add(add, "growx 0");
		root.add(hint("Folder: " + folder().getAbsolutePath() + ". Back it up: without your private key the files sent to you cannot be opened."),
				"gaptop 10, wmin 420");
	}

	private interface Change {
		void apply(Properties p);
	}

	private static void update(Component parent, Change c) {
		Properties p = loadStore();
		c.apply(p);
		try {
			saveStore(p);
		} catch (IOException ex) {
			JOptionPane.showMessageDialog(parent, "Cannot save the keys: " + ex.getMessage(), "Keys", JOptionPane.ERROR_MESSAGE);
		}
	}

	private static boolean createKeys(Component parent, boolean replacing) {
		if (replacing && JOptionPane.showConfirmDialog(parent,
				"<html><body style='width:340px'>New keys replace the current ones. Files already sent to you need the old private key: "
						+ "it is kept in the same folder with the date in its name.</body></html>",
				"Keys", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.OK_OPTION) {
			return false; // cancelled by user
		}
		JPasswordField a = new JPasswordField(24), b = new JPasswordField(24);
		JPanel form = new JPanel(new MigLayout("insets 0, wrap 2", "[][grow, fill]"));
		form.add(new JLabel("Password of the private key"));
		form.add(a);
		form.add(new JLabel("Again"));
		form.add(b);
		form.add(hint("Use the password you use in the Crypter: it opens the files sent to you."), "span 2");
		char[] pa = null, pb = null;
		try {
			while (true) {
				if (JOptionPane.showConfirmDialog(parent, form, "Create my keys", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
					return false; // cancelled by user
				}
				pa = a.getPassword();
				pb = b.getPassword();
				if (pa.length >= 8 && Arrays.equals(pa, pb)) {
					break;
				}
				JOptionPane.showMessageDialog(parent, "The two passwords must be the same and at least 8 characters long.", "Keys", JOptionPane.WARNING_MESSAGE);
				Arrays.fill(pa, '\0');
				Arrays.fill(pb, '\0');
			}
			File id = identityFile();
			if (id.isFile()) {
				File backup = new File(folder(), "my_private_key_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".scptkey");
				if (!id.renameTo(backup)) {
					throw new IOException("Cannot keep a copy of the old private key");
				}
			}
			Identity identity = Identity.generate();
			try {
				identity.save(id, pa);
				String pub = identity.publicKeyText();
				update(parent, s -> s.setProperty(MY_PUBLIC, pub));
			} finally {
				identity.clear();
			}
			return true;
		} catch (CryptoException | IOException ex) {
			JOptionPane.showMessageDialog(parent, "Cannot create the keys: " + ex.getMessage(), "Keys", JOptionPane.ERROR_MESSAGE);
			return false;
		} finally {
			a.setText("");
			b.setText("");
			if (pa != null) {
				Arrays.fill(pa, '\0');
			}
			if (pb != null) {
				Arrays.fill(pb, '\0');
			}
		}
	}

	private static boolean addContact(Component parent) {
		JTextField name = new JTextField(20), key = new JTextField(40);
		JPanel form = new JPanel(new MigLayout("insets 0, wrap 2", "[][grow, fill]"));
		form.add(new JLabel("Name"));
		form.add(name);
		form.add(new JLabel("Public key"));
		form.add(key);
		while (true) {
			if (JOptionPane.showConfirmDialog(parent, form, "Add a person", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
				return false; // cancelled by user
			}
			String n = name.getText().trim();
			try {
				byte[] pub = Identity.parsePublicKey(key.getText());
				if (n.isEmpty()) {
					throw new CryptoException("Write a name");
				}
				String text = Identity.formatPublicKey(pub);
				update(parent, s -> {
					s.setProperty(KEY + n, text);
					s.setProperty(USE + n, "true");
				});
				return true;
			} catch (CryptoException ex) {
				JOptionPane.showMessageDialog(parent, ex.getMessage(), "Add a person", JOptionPane.WARNING_MESSAGE);
			}
		}
	}

	private static JLabel section(String text) {
		JLabel l = new JLabel(text);
		l.setFont(l.getFont().deriveFont(Font.BOLD));
		return l;
	}

	private static JLabel hint(String text) {
		JLabel l = new JLabel("<html><body style='width:400px'>" + text.replace("&", "&amp;").replace("<", "&lt;") + "</body></html>");
		l.setForeground(new Color(160, 168, 176));
		return l;
	}
}

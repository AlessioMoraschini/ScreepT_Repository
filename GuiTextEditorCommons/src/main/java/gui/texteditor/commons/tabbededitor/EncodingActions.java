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
package gui.texteditor.commons.tabbededitor;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;

import gui.texteditor.commons.om.TabTextEditorObject;
import various.common.light.files.EncodingDetector;
import various.common.light.i18n.I18n;
import various.common.light.utility.string.ListCharsets;

/**
 * Encoding of the file in front, for the right click menu and the footer: read the file again with another encoding, or convert it.
 * The choice belongs to the tab: the encoding of the preferences never changes.
 */
public final class EncodingActions {

	private static final String BOM = "\uFEFF";
	private static final String[] COMMON = { "UTF-8", "windows-1252", "ISO-8859-1", "ISO-8859-15", "UTF-16", "UTF-16LE", "UTF-16BE",
			"US-ASCII", "IBM850" };

	private EncodingActions() {
	}

	static void fill(CustomTabbedEditor editor, JComponent menu, TabTextEditorObject tab) {
		boolean text = tab != null && !tab.isEmbeddedViewer() && !tab.isEncrypted();
		boolean onDisk = text && tab.loadedFile != null && tab.loadedFile.isFile();
		String current = tab == null ? null : currentEncoding(tab);
		menu.add(list(editor, I18n.t("editor.reopen_with_encoding"), current, onDisk, name -> reopen(editor, tab, name)));
		menu.add(list(editor, I18n.t("editor.convert_to_encoding"), current, text, name -> convert(editor, tab, name)));
	}

	static String currentEncoding(TabTextEditorObject tab) {
		return tab.encoding != null ? tab.encoding : System.getProperty("file.encoding");
	}

	private static JMenu list(CustomTabbedEditor editor, String title, String current, boolean enabled, Consumer<String> action) {
		JMenu menu = new JMenu(title);
		menu.setEnabled(enabled);
		if (!enabled) {
			return menu;
		}
		List<String> names = new ArrayList<>();
		boolean currentListed = false;
		for (String name : COMMON) {
			if (Charset.isSupported(name)) {
				names.add(name);
				currentListed |= sameCharset(name, current);
			}
		}
		if (!currentListed && current != null) {
			names.add(current);
		}
		for (String name : names) {
			JMenuItem item = new JRadioButtonMenuItem(name, sameCharset(name, current));
			item.addActionListener(e -> action.accept(name));
			menu.add(item);
		}
		menu.addSeparator();
		JMenuItem other = new JMenuItem(I18n.t("editor.other_encoding"));
		other.addActionListener(e -> {
			Object chosen = JOptionPane.showInputDialog(editor.mainTabList, title, I18n.t("editor.encoding_menu"),
					JOptionPane.QUESTION_MESSAGE, null, ListCharsets.getCharsets().toArray(), current);
			if (chosen != null) {
				action.accept(chosen.toString());
			}
		});
		menu.add(other);
		return menu;
	}

	private static boolean sameCharset(String a, String b) {
		return a != null && b != null && Charset.isSupported(b) && Charset.forName(a).equals(Charset.forName(b));
	}

	/** Reads the file again with the given encoding: the text on screen is replaced, the file is not touched */
	static void reopen(CustomTabbedEditor editor, TabTextEditorObject tab, String encoding) {
		if (tab != editor.loadedTabObj) {
			return;
		}
		if (tab.unsaved && !editor.dialogHelper.yesOrNo(I18n.fmt("editor.reopen_discard_changes", encoding), I18n.t("editor.encoding_menu"))) {
			return;
		}
		tab.forcedEncoding = encoding;
		editor.reloadCurrentTab(false);
		tab.textArea.discardAllEdits();
	}

	/** The text on screen will be saved with the given encoding: the tab is left unsaved, the file changes when it is saved */
	static void convert(CustomTabbedEditor editor, TabTextEditorObject tab, String encoding) {
		if (tab != editor.loadedTabObj || sameCharset(encoding, currentEncoding(tab))) {
			return;
		}
		Charset charset = Charset.forName(encoding);
		if (!charset.canEncode()) {
			editor.dialogHelper.warn(I18n.fmt("editor.encoding_read_only", encoding), I18n.t("editor.encoding_menu"));
			return;
		}
		String text = tab.textArea.getText();
		boolean hasBom = text.startsWith(BOM);
		String body = hasBom ? text.substring(1) : text;
		if (!charset.newEncoder().canEncode(body)
				&& !editor.dialogHelper.yesOrNo(I18n.fmt("editor.encoding_lossy", encoding), I18n.t("editor.encoding_menu"))) {
			return;
		}
		// without a byte order mark, these two could not be recognised when the file is opened again
		boolean wantsBom = "UTF-16LE".equals(charset.name()) || "UTF-16BE".equals(charset.name());
		if (hasBom && !wantsBom) {
			tab.textArea.replaceRange("", 0, 1);
		} else if (!hasBom && wantsBom) {
			tab.textArea.insert(BOM, 0);
		}
		tab.encoding = charset.name();
		tab.encodingConverted = true;
		tab.unsaved = true;
		editor.updateSavedLabelNTab(null);

		String misread = EncodingDetector.misreadAs(tab.textArea.getText(), tab.encoding, editor.fixedEncoding(),
				editor.isEncodingAutoDetected());
		if (misread != null) {
			editor.dialogHelper.info(I18n.fmt("editor.encoding_reopen_notice", tab.encoding, misread), I18n.t("editor.encoding_menu"));
		}
	}
}

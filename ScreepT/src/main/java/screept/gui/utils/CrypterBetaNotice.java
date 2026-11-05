/**
 *
 * =========================================================================================
 *  Copyright (C) 2019-2026
 *
 *  AM-Design-Development - (Alessio Moraschini) - All Rights Reserved
 * =========================================================================================
 *
 * You should have received a copy of the license with this file.
 * If not, please write to: info@am-design-development.com, or visit : https://www.am-design-development.com
 */
package screept.gui.utils;

import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.BoxLayout;

import initializer.configs.impl.INItializer;

/**
 * Warning shown when the Crypter module is opened: the module is experimental (beta) and not extensively tested.
 * "Don't show this again" is saved in the configuration file (GENERIC_OPTIONS, crypterBetaNoticeHidden).
 */
public final class CrypterBetaNotice {

	private static final String KEY = "crypterBetaNoticeHidden";

	private CrypterBetaNotice() {
	}

	public static boolean isHidden(INItializer config) {
		try {
			return config != null && "true".equalsIgnoreCase(config.getIniFile().get(INItializer.GEN_OPT, KEY));
		} catch (RuntimeException e) {
			return false;
		}
	}

	/** Shows the warning unless the user asked not to see it again; the choice is saved before returning */
	public static void showIfNeeded(Component parent, INItializer config) {
		if (isHidden(config)) {
			return;
		}
		JLabel text = new JLabel("<html><body style='width:430px'>"
				+ "<b>The Crypter module is in beta.</b><br><br>"
				+ "It is experimental and has not been extensively tested yet. Its algorithms have not been reviewed by independent experts: "
				+ "do not rely on it as the only protection of important data, and keep an independent copy of your original files and of your keys. "
				+ "A bug or a lost key can make crypted files impossible to recover.<br><br>"
				+ "Updates over the coming weeks and months will improve the Crypter and its integration in ScreepT, and add new features."
				+ "</body></html>");
		JCheckBox dontShow = new JCheckBox("Don't show this again");
		JPanel content = new JPanel();
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		content.setOpaque(false);
		text.setAlignmentX(Component.LEFT_ALIGNMENT);
		dontShow.setAlignmentX(Component.LEFT_ALIGNMENT);
		content.add(text);
		content.add(javax.swing.Box.createVerticalStrut(14));
		content.add(dontShow);

		JOptionPane.showOptionDialog(parent, content, "Crypter - beta version", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null,
				new Object[] { "OK, I understand" }, "OK, I understand");

		if (dontShow.isSelected() && config != null) {
			config.writeSingleValue(INItializer.GEN_OPT, KEY, "true");
		}
	}
}

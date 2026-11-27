/**
 *
 * =========================================================================================
 *  Copyright (C) 2019-2026
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
package screept.gui.utils;

import various.common.light.i18n.I18n;
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
		JLabel text = new JLabel(I18n.t("crypter.the_crypter_module_is_in_beta"));
		JCheckBox dontShow = new JCheckBox(I18n.t("crypter.don_t_show_this_again"));
		JPanel content = new JPanel();
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		content.setOpaque(false);
		text.setAlignmentX(Component.LEFT_ALIGNMENT);
		dontShow.setAlignmentX(Component.LEFT_ALIGNMENT);
		content.add(text);
		content.add(javax.swing.Box.createVerticalStrut(14));
		content.add(dontShow);

		JOptionPane.showOptionDialog(parent, content, I18n.t("crypter.crypter_beta_version"), JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null,
				new Object[] { I18n.t("crypter.ok_i_understand") }, I18n.t("crypter.ok_i_understand"));

		if (dontShow.isSelected() && config != null) {
			config.writeSingleValue(INItializer.GEN_OPT, KEY, "true");
		}
	}
}

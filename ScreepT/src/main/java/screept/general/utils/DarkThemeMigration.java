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
package screept.general.utils;

import org.apache.log4j.Logger;

import gui.commons.guistyle.laf.ScreepTDarkLaf;
import initializer.configs.impl.INItializer;

/**
 * One-time migration: the first time a configuration is used by a version that includes it, the look and feel is set to ScreepT Dark
 * (the new default). The fact is remembered in the configuration (GUI_OPTIONS, {@value #KEY}), so the following updates never touch the
 * look and feel again: whatever the user chooses afterwards (Nimbus, light...) is kept.
 * <p>
 * To force a new look and feel in a future release, use a new key: never reset this one.
 */
public final class DarkThemeMigration {

	private static final Logger logger = Logger.getLogger(DarkThemeMigration.class);

	/** Key written in the GUI_OPTIONS section once the migration has run */
	public static final String KEY = "darkLafForced";

	private DarkThemeMigration() {
	}

	/** Runs the migration if it never ran for this configuration; call it right after the configuration is read */
	public static void applyOnce(INItializer config) {
		try {
			if (config == null || "true".equalsIgnoreCase(config.getIniFile().get(INItializer.GUI_OPT, KEY))) {
				return;
			}
			String style = config.getGuiOpt().getPreferredStyle();
			if (!ScreepTDarkLaf.isScreepTDark(style)) {
				logger.info("Look and feel changed from [" + style + "] to ScreepT Dark (one-time migration)");
				config.getGuiOpt().setPreferredStyle(ScreepTDarkLaf.CLASS_NAME);
				config.writeSingleValue(INItializer.GUI_OPT, "preferredStyle", ScreepTDarkLaf.CLASS_NAME);
			}
			config.writeSingleValue(INItializer.GUI_OPT, KEY, "true");
		} catch (RuntimeException e) {
			logger.warn("Dark theme migration skipped", e);
		}
	}
}

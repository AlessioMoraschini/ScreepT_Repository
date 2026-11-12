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
package screept.general.utils;

import java.io.File;
import java.io.IOException;

import org.ini4j.Wini;

import resources.GeneralConfig;
import screept.launcher.utils.IniFileManager;
import various.common.light.utility.log.SafeLogger;

/**
 * The options of the launcher configuration file (Launcher_Configuration.ini) that concern how ScreepT is started:
 * "Hide launcher at start" (the same property written by Generic Options) and the one-time request to show the
 * launcher at the next start, consumed by the launcher itself.
 */
public final class LauncherIniOptions {

	private static final SafeLogger logger = new SafeLogger(LauncherIniOptions.class);

	/** "Hide launcher at start": when true the launcher starts ScreepT directly, without showing itself */
	public static final String KEY_HIDE_LAUNCHER = "flagHideNStartDirect";

	/** Asks the launcher to show itself at the next start only, even if it is hidden by default */
	public static final String KEY_SHOW_LAUNCHER_ONCE = "flagShowLauncherOnce";

	private LauncherIniOptions() {
	}

	/** @return "Hide launcher at start", or null when the launcher configuration cannot be read */
	public static Boolean isLauncherHidden() {
		try {
			File iniFile = new File(GeneralConfig.DEFAULT_LAUNCHER_INI_PATH);
			if (!iniFile.isFile()) {
				return null;
			}
			String value = new Wini(iniFile).get(IniFileManager.INI_APP_OPT, KEY_HIDE_LAUNCHER);
			return value != null && value.equalsIgnoreCase("true");
		} catch (Exception e) {
			logger.error("Cannot read the launcher configuration", e);
			return null;
		}
	}

	/** Sets "Hide launcher at start" in the launcher configuration file, like Generic Options does */
	public static boolean setLauncherHidden(boolean hidden) {
		return write(KEY_HIDE_LAUNCHER, hidden);
	}

	/** The launcher will be shown at the next start only (the launcher clears the request by itself) */
	public static boolean setShowLauncherOnce(boolean once) {
		return write(KEY_SHOW_LAUNCHER_ONCE, once);
	}

	/** Memory options of the launcher (like "-Xmx2048m -Xms512m"), or null when the launcher configuration cannot be read */
	public static String getMemoryArgs() {
		try {
			File iniFile = new File(GeneralConfig.DEFAULT_LAUNCHER_INI_PATH);
			if (!iniFile.isFile()) {
				return null;
			}
			String value = new Wini(iniFile).get(IniFileManager.INI_JVM_OPT, "memoryArgs");
			return value == null ? "" : value.trim();
		} catch (Exception e) {
			logger.error("Cannot read the launcher configuration", e);
			return null;
		}
	}

	/** Saves the memory options of the launcher: they apply from the next start */
	public static boolean setMemoryArgs(String args) {
		try {
			File iniFile = new File(GeneralConfig.DEFAULT_LAUNCHER_INI_PATH);
			if (!iniFile.isFile()) {
				return false;
			}
			Wini ini = new Wini(iniFile);
			ini.put(IniFileManager.INI_JVM_OPT, "memoryArgs", args);
			ini.store();
			return true;
		} catch (IOException e) {
			logger.error("Exception happened writing launcher configuration!", e);
			return false;
		}
	}

	private static boolean write(String key, boolean value) {
		try {
			File iniFile = new File(GeneralConfig.DEFAULT_LAUNCHER_INI_PATH);
			if (!iniFile.isFile()) {
				return false;
			}
			Wini ini = new Wini(iniFile);
			ini.put(IniFileManager.INI_APP_OPT, key, value ? "true" : "false");
			ini.store();
			return true;
		} catch (IOException e) {
			logger.error("Exception happened writing launcher configuration!", e);
			return false;
		}
	}
}

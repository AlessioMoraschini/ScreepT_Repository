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
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Base64;

import org.apache.commons.io.FileUtils;

import os.commons.utils.SysUtils;
import resources.GeneralConfig;

/**
 * Start of ScreepT at login.
 * <p>
 * On Windows it is a real shortcut in the Startup folder of the <b>current user</b>: no administrator rights are
 * needed (the old code wrote a symbolic link in the folder of all users, which needs them) and it starts ScreepT.bat
 * through cmd.exe, because ScreepT.exe can be blocked on company PCs; the exe is used only when there is no bat.
 * On the other systems the previous behavior is kept.
 */
public final class AutostartShortcut {

	private AutostartShortcut() {
	}

	private static boolean windows() {
		return SysUtils.getOsInfoLazy().isWindows();
	}

	private static String shortcutName() {
		return GeneralConfig.APPLICATION_SHORT_NAME + ".lnk";
	}

	/** Startup folder of the current user */
	public static File userStartupFolder() {
		String appData = System.getenv("APPDATA");
		File base = appData != null && !appData.trim().isEmpty() ? new File(appData) : new File(System.getProperty("user.home"), "AppData/Roaming");
		return new File(base, "Microsoft/Windows/Start Menu/Programs/StartUp");
	}

	/** The shortcut written by the old versions, in the Startup folder of all users */
	private static File oldAllUsersEntry() {
		return new File(SysUtils.WIN_AUTOSTART_PATH, shortcutName());
	}

	/** True if ScreepT starts at login (new shortcut of the user, or the old one for all users) */
	public static boolean isPresent() {
		if (!windows()) {
			return SysUtils.isPresentInAutostart(GeneralConfig.MAIN_APP_OS_DEPENDENT_LAUNCHER);
		}
		return new File(userStartupFolder(), shortcutName()).exists() || oldAllUsersEntry().exists();
	}

	/** @return element 0: success message; element 1: error message (empty if success) */
	public static String[] add() throws IOException {
		if (!windows()) {
			return SysUtils.addToAutostart(GeneralConfig.MAIN_APP_OS_DEPENDENT_LAUNCHER);
		}
		File starter = AdminStarter.chooseStarter();
		if (starter == null) {
			return new String[] { "", "Neither ScreepT.bat nor ScreepT.exe found in the installation folder." };
		}
		return createShortcut(userStartupFolder(), starter);
	}

	/** @return true if ScreepT does not start at login anymore */
	public static boolean remove() throws IOException {
		if (!windows()) {
			return SysUtils.removeFromAutostart(GeneralConfig.MAIN_APP_OS_DEPENDENT_LAUNCHER);
		}
		FileUtils.deleteQuietly(new File(userStartupFolder(), shortcutName()));
		// entry of the old versions (all users): removing it needs administrator rights, failing is fine
		FileUtils.deleteQuietly(oldAllUsersEntry());
		return !isPresent();
	}

	/** Creates the shortcut in the given folder; package visible so that it can be tried in a temporary folder */
	static String[] createShortcut(File folder, File starter) throws IOException {
		String starterPath = starter.getCanonicalPath();
		String rootFolder = starter.getCanonicalFile().getParentFile().getPath();
		String icon = OpenWithContextMenu.findIconPath();
		boolean bat = starterPath.toLowerCase().endsWith(".bat");

		StringBuilder script = new StringBuilder("$ErrorActionPreference='Stop'\r\n")
				.append("New-Item -ItemType Directory -Force -Path '").append(q(folder.getPath())).append("' | Out-Null\r\n")
				.append("$link=(New-Object -ComObject WScript.Shell).CreateShortcut('").append(q(new File(folder, shortcutName()).getPath())).append("')\r\n");
		if (bat) {
			script.append("$link.TargetPath=$env:ComSpec\r\n")
					.append("$link.Arguments='/c \"\"").append(q(starterPath)).append("\"\"'\r\n");
		} else {
			script.append("$link.TargetPath='").append(q(starterPath)).append("'\r\n");
		}
		script.append("$link.WorkingDirectory='").append(q(rootFolder)).append("'\r\n")
				.append("$link.WindowStyle=7\r\n")
				.append("$link.Description='").append(q(GeneralConfig.APPLICATION_SHORT_NAME)).append("'\r\n");
		if (icon != null) {
			script.append("$link.IconLocation='").append(q(icon)).append("'\r\n");
		}
		script.append("$link.Save()\r\n");

		String encoded = Base64.getEncoder().encodeToString(script.toString().getBytes(Charset.forName("UTF-16LE")));
		Process process = new ProcessBuilder("powershell.exe", "-NoProfile", "-NonInteractive", "-EncodedCommand", encoded)
				.redirectErrorStream(true).start();
		StringBuilder output = new StringBuilder();
		try (InputStream in = process.getInputStream()) {
			byte[] buffer = new byte[1024];
			int read;
			while ((read = in.read(buffer)) > 0) {
				output.append(new String(buffer, 0, read, Charset.defaultCharset()));
			}
		}
		try {
			if (process.waitFor() != 0) {
				return new String[] { "", "Cannot create the shortcut: " + output.toString().trim() };
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return new String[] { "", "Interrupted while creating the shortcut" };
		}
		return new String[] { GeneralConfig.APPLICATION_SHORT_NAME + " will start at login (" + starter.getName() + ", for your user).", "" };
	}

	/** Inside a PowerShell single quoted string a quote is doubled */
	private static String q(String value) {
		return value.replace("'", "''");
	}
}

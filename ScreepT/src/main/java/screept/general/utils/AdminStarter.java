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
import java.nio.charset.Charset;
import java.util.Base64;

import resources.GeneralConfig;

/**
 * Starts ScreepT again with administrator rights (Windows). It prefers ScreepT.bat, run by cmd.exe, because
 * on company PCs ScreepT.exe can be blocked; ScreepT.exe is used only when there is no ScreepT.bat.
 */
public final class AdminStarter {

	private AdminStarter() {
	}

	/** @return ScreepT.bat if present, else ScreepT.exe if present, else null */
	public static File chooseStarter() {
		File bat = new File(GeneralConfig.MAIN_APP_PARENT_DIR, "ScreepT.bat");
		if (bat.isFile()) {
			return bat;
		}
		File exe = GeneralConfig.MAIN_APP_OS_DEPENDENT_LAUNCHER;
		return exe.isFile() ? exe : null;
	}

	/** Asks Windows (UAC prompt) to start the given starter as administrator, then returns without waiting for it */
	public static void startAsAdministrator(File starter) throws IOException {
		String script = buildScript(starter, true);
		// the script travels encoded, so quotes and spaces in paths cannot be mangled by the command line
		String encoded = Base64.getEncoder().encodeToString(script.getBytes(Charset.forName("UTF-16LE")));
		new ProcessBuilder("powershell.exe", "-NoProfile", "-NonInteractive", "-EncodedCommand", encoded).start();
	}

	/** The PowerShell script that starts the starter (bat through cmd.exe, exe directly), in its folder */
	public static String buildScript(File starter, boolean asAdministrator) throws IOException {
		String path = starter.getCanonicalPath();
		String folder = starter.getCanonicalFile().getParentFile().getPath();
		String verb = asAdministrator ? " -Verb RunAs" : "";

		if (path.toLowerCase().endsWith(".bat")) {
			// cmd /c ""C:\path with spaces\ScreepT.bat""
			return "$ErrorActionPreference='Stop'\r\n"
					+ "Start-Process -FilePath 'cmd.exe' -ArgumentList '/c \"\"" + quote(path) + "\"\"' -WorkingDirectory '" + quote(folder) + "'"
					+ verb + " -WindowStyle Minimized\r\n";
		}
		return "$ErrorActionPreference='Stop'\r\n"
				+ "Start-Process -FilePath '" + quote(path) + "' -WorkingDirectory '" + quote(folder) + "'" + verb + "\r\n";
	}

	/** Inside a PowerShell single quoted string a quote is doubled */
	private static String quote(String value) {
		return value.replace("'", "''");
	}
}

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
import java.nio.file.Files;

import resources.GeneralConfig;

/**
 * "Open With ScreepT" entry in the Windows right click menu of every file.
 * <p>
 * The entry is written for the current user (HKEY_CURRENT_USER\Software\Classes), so no administrator rights are needed,
 * directly by this application and not by relaunching ScreepT.exe: on company PCs the exe can be blocked while
 * ScreepT.bat works. The entry starts the starter chosen by the user: ScreepT.exe, or ScreepT.bat through cmd.exe.
 */
public final class OpenWithContextMenu {

	private static final String KEY_NAME = "Open With " + GeneralConfig.APPLICATION_SHORT_NAME;
	private static final String USER_KEY = "HKEY_CURRENT_USER\\Software\\Classes\\*\\shell\\" + KEY_NAME;
	/** The key written by the old versions (all users, needs administrator rights) */
	private static final String MACHINE_KEY = "HKEY_CLASSES_ROOT\\*\\shell\\" + KEY_NAME;

	private OpenWithContextMenu() {
	}

	/** Outcome of an operation: success flag and a message for the user */
	public static final class Result {
		public final boolean ok;
		public final String message;

		Result(boolean ok, String message) {
			this.ok = ok;
			this.message = message;
		}
	}

	/** The starter files that can be registered and exist in the installation folder, exe first */
	public static File[] availableStarters() {
		File exe = GeneralConfig.MAIN_APP_OS_DEPENDENT_LAUNCHER;
		File bat = new File(GeneralConfig.MAIN_APP_PARENT_DIR, "ScreepT.bat");
		if (exe.isFile() && bat.isFile()) {
			return new File[] { exe, bat };
		}
		return exe.isFile() ? new File[] { exe } : bat.isFile() ? new File[] { bat } : new File[0];
	}

	/** Registers the entry so that it starts the given starter (ScreepT.exe or ScreepT.bat) with the clicked file */
	public static Result register(File starter) {
		try {
			String starterPath = starter.getCanonicalPath();
			String iconPath = findIconPath();

			String command = starterPath.toLowerCase().endsWith(".bat")
					? "cmd.exe /c \"\"" + starterPath + "\" \"%1\"\""
					: "\"" + starterPath + "\" \"%1\"";

			String reg = "Windows Registry Editor Version 5.00\r\n\r\n"
					+ "[" + USER_KEY + "]\r\n"
					+ (iconPath == null ? "" : "\"Icon\"=\"" + escape(iconPath) + "\"\r\n") + "\r\n"
					+ "[" + USER_KEY + "\\command]\r\n"
					+ "@=\"" + escape(command) + "\"\r\n";
			Result imported = importReg(reg);
			if (!imported.ok) {
				return imported;
			}
			if (!exists(USER_KEY + "\\command")) {
				return new Result(false, "The registry did not accept the entry.");
			}
			return new Result(true, "Added for your user, it starts " + starter.getName() + ". It is in the right click menu of every file.");

		} catch (IOException | InterruptedException e) {
			return new Result(false, e.toString());
		}
	}

	/** Removes the entry of the current user and, if there are the rights, the one written by old versions for all users */
	public static Result unregister() {
		try {
			// old machine-wide entry first: it needs administrator rights, failing is fine
			run("reg", "delete", MACHINE_KEY, "/f");
			Result removed = importReg("Windows Registry Editor Version 5.00\r\n\r\n[-" + USER_KEY + "]\r\n");
			if (!removed.ok) {
				return removed;
			}
			if (exists(USER_KEY + "\\command") || exists(MACHINE_KEY + "\\command")) {
				return new Result(false, "An entry created by an old version for all users is still there: removing it needs administrator rights.");
			}
			return new Result(true, "Removed from the right click menu.");

		} catch (IOException | InterruptedException e) {
			return new Result(false, e.toString());
		}
	}

	// ---------------------------------------------------------------------------------------------

	/**
	 * The icon of the menu entry: the ScreepT.ico shipped with the application resources (always updated with it),
	 * else the one of the guide, else the icon inside ScreepT.exe. A .bat has no icon of its own, so it is never used.
	 *
	 * @return null if nothing is available
	 */
	static String findIconPath() throws IOException {
		File[] candidates = {
				new File(GeneralConfig.RESOURCES_DIR, "Icons/ScreepT.ico"),
				new File(GeneralConfig.MAIN_APP_PARENT_DIR, "Application/" + GeneralConfig.RESOURCES_DIR + "Icons/ScreepT.ico"),
				new File(GeneralConfig.MAIN_APP_PARENT_DIR, "Readme/Images/ScreepT.ico") };
		for (File candidate : candidates) {
			if (candidate.isFile()) {
				return candidate.getCanonicalPath();
			}
		}
		File exe = GeneralConfig.MAIN_APP_OS_DEPENDENT_LAUNCHER;
		return exe.isFile() ? exe.getCanonicalPath() + ",0" : null;
	}

	/** In a .reg file backslashes and quotes must be escaped */
	private static String escape(String value) {
		return value.replace("\\", "\\\\").replace("\"", "\\\"");
	}

	/** Applies a .reg content with "reg import" (UTF-16 so that any path is fine) */
	private static Result importReg(String content) throws IOException, InterruptedException {
		File file = File.createTempFile("screept-openwith-", ".reg");
		try {
			byte[] body = content.getBytes(Charset.forName("UTF-16LE"));
			byte[] bytes = new byte[body.length + 2];
			bytes[0] = (byte) 0xFF; // byte order mark
			bytes[1] = (byte) 0xFE;
			System.arraycopy(body, 0, bytes, 2, body.length);
			Files.write(file.toPath(), bytes);

			String output = run("reg", "import", file.getAbsolutePath());
			return output == null ? new Result(true, "") : new Result(false, "reg import failed: " + output);
		} finally {
			file.delete();
		}
	}

	private static boolean exists(String key) throws IOException, InterruptedException {
		return run("reg", "query", key) == null;
	}

	/** @return null if the command succeeded, else what it printed */
	private static String run(String... command) throws IOException, InterruptedException {
		Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
		StringBuilder out = new StringBuilder();
		try (InputStream in = process.getInputStream()) {
			byte[] buffer = new byte[1024];
			int read;
			while ((read = in.read(buffer)) > 0) {
				out.append(new String(buffer, 0, read, Charset.defaultCharset()));
			}
		}
		return process.waitFor() == 0 ? null : out.toString().trim();
	}
}

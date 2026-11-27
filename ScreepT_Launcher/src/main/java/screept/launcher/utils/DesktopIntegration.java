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
package screept.launcher.utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import os.commons.utils.SysUtils;
import resources.GeneralConfig;
import resources.ImagesPathConfigurator;
import various.common.light.i18n.I18n;

/**
 * Puts ScreepT in the desktop of the user, never with administrator rights: icon on the desktop, panel (Linux, where the
 * desktop environment allows it) or Start menu (Windows, which does not let a program pin itself to the taskbar) and, on
 * Linux, the "Open with" entries of the file managers.
 * <p>
 * Every method returns the message to show to the user.
 */
public final class DesktopIntegration {

	private static final Logger logger = Logger.getLogger(DesktopIntegration.class);

	static final String ENTRY_NAME = "screept.desktop";
	private static final String NEMO_ACTION = "screept.nemo_action";
	private static final String CINNAMON_APPLET = "grouped-window-list@cinnamon.org";
	/** Class of the window of ScreepT, so the panel keeps the running window on the same icon */
	private static final String WINDOW_CLASS = "maingUI-MainGuiGeneralAppStarter";
	private static final String MIME_TYPES = "text/plain;text/x-log;text/markdown;text/csv;text/html;text/css;text/xml;application/xml;"
			+ "application/json;application/javascript;application/x-yaml;application/toml;application/sql;text/x-sql;application/x-shellscript;"
			+ "text/x-python;text/x-java;text/x-csrc;text/x-chdr;text/x-c++src;application/x-php;image/png;image/jpeg;image/gif;image/bmp;";
	private static final int TIMEOUT_SECONDS = 10;

	private DesktopIntegration() {
	}

	/** What can be switched on and off on Linux */
	public enum Item {
		DESKTOP_ICON, PANEL, OPEN_WITH
	}

	/** Windows and Linux; macOS has its own way (the Dock) */
	public static boolean isSupported() {
		return SysUtils.getOsInfoLazy().isWindows() || SysUtils.getOsInfoLazy().isLinux();
	}

	////////// STATE (Linux) ////////////////

	/** @return the items already in place for this installation (reads files and may run desktop commands: not for the EDT) */
	public static Set<Item> activeItems() {
		Set<Item> active = EnumSet.noneOf(Item.class);
		try {
			String starter = starter().getAbsolutePath();
			if (startsWith(new File(desktopFolder(), ENTRY_NAME), desktopExec(starter))) {
				active.add(Item.DESKTOP_ICON);
			}
			if (isPinned(starter)) {
				active.add(Item.PANEL);
			}
			if (isOpenWithActive(dataHome(), starter)) {
				active.add(Item.OPEN_WITH);
			}
		} catch (IOException | RuntimeException e) {
			logger.warn("Cannot read the desktop integration", e);
		}
		return active;
	}

	/** @return the message to show to the user */
	public static String setActive(Item item, boolean active) throws IOException {
		switch (item) {
			case DESKTOP_ICON:
				return active ? createDesktopIcon() : removeDesktopIcon();
			case PANEL:
				return active ? addToPanel() : removeFromPanel();
			default:
				return active ? addOpenWith() : removeOpenWith();
		}
	}

	/** True when the file exists and has the line that starts this installation: one of another folder is not ours */
	static boolean startsWith(File file, String execLine) throws IOException {
		return file.isFile() && Arrays.asList(new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8).split("\r?\n"))
				.contains(execLine);
	}

	static boolean isOpenWithActive(File dataHome, String starterPath) throws IOException {
		return startsWith(new File(dataHome, "nemo/actions/" + NEMO_ACTION), nemoExec(starterPath));
	}

	private static boolean isPinned(String starterPath) throws IOException {
		if (!startsWith(new File(dataHome(), "applications/" + ENTRY_NAME), desktopExec(starterPath))) {
			return false;
		}
		if (isDesktop("cinnamon")) {
			return isPinnedInCinnamon(new File(System.getProperty("user.home")));
		}
		if (isDesktop("gnome") || isDesktop("unity")) {
			String current = run("gsettings", "get", "org.gnome.shell", "favorite-apps");
			return current != null && current.contains("'" + ENTRY_NAME + "'");
		}
		return false;
	}

	private static boolean isDesktop(String name) {
		return String.valueOf(System.getenv("XDG_CURRENT_DESKTOP")).toLowerCase(Locale.ROOT).contains(name);
	}

	////////// ACTIONS ////////////////

	public static String createDesktopIcon() throws IOException {
		File starter = starter();
		if (SysUtils.getOsInfoLazy().isWindows()) {
			windowsShortcut("Desktop", starter);
			return I18n.t("launcher.desktop_icon_created");
		}
		File icon = new File(desktopFolder(), ENTRY_NAME);
		write(icon, desktopEntry(starter.getAbsolutePath(), iconPath()));
		makeExecutable(icon);
		// some desktops ask for confirmation unless the file is marked as trusted: best effort
		run("gio", "set", icon.getAbsolutePath(), "metadata::trusted", "true");
		return I18n.t("launcher.desktop_icon_created");
	}

	/** Linux: pins ScreepT to the panel. Windows: adds it to the Start menu, from which the user can pin it */
	public static String addToPanel() throws IOException {
		File starter = starter();
		if (SysUtils.getOsInfoLazy().isWindows()) {
			windowsShortcut("Programs", starter);
			return I18n.t("launcher.added_to_start_menu");
		}
		// the file types are listed only if "Open with" was asked for
		writeMenuEntry(starter, isOpenWithActive(dataHome(), starter.getAbsolutePath()));
		if (isDesktop("cinnamon")) {
			return pinToCinnamon(new File(System.getProperty("user.home")));
		}
		if (isDesktop("gnome") || isDesktop("unity")) {
			return pinToGnome();
		}
		return I18n.t("launcher.added_to_menu_pin_by_hand");
	}

	/** Linux: "Open With" of every file manager, and a direct entry in the right click menu of Nemo (Linux Mint) */
	public static String addOpenWith() throws IOException {
		File starter = starter();
		writeMenuEntry(starter, true);
		File actions = new File(dataHome(), "nemo/actions");
		write(new File(actions, NEMO_ACTION), nemoAction(starter.getAbsolutePath()));
		return I18n.t("launcher.open_with_added");
	}

	public static String removeDesktopIcon() throws IOException {
		delete(new File(desktopFolder(), ENTRY_NAME));
		return I18n.t("launcher.desktop_icon_removed");
	}

	/** Linux: the menu entry goes too, unless "Open with" still needs it */
	public static String removeFromPanel() throws IOException {
		if (isDesktop("cinnamon")) {
			unpinFromCinnamon(new File(System.getProperty("user.home")));
		} else if (isDesktop("gnome") || isDesktop("unity")) {
			unpinFromGnome();
		}
		if (!isOpenWithActive(dataHome(), starter().getAbsolutePath())) {
			removeMenuEntry();
		}
		return I18n.t("launcher.removed_from_panel");
	}

	/** Linux: a pinned ScreepT keeps its menu entry, without the file types */
	public static String removeOpenWith() throws IOException {
		File starter = starter();
		boolean pinned = isPinned(starter.getAbsolutePath());
		delete(new File(dataHome(), "nemo/actions/" + NEMO_ACTION));
		if (pinned) {
			writeMenuEntry(starter, false);
		} else {
			removeMenuEntry();
		}
		return I18n.t("launcher.open_with_removed");
	}

	////////// FILES ////////////////

	private static File starter() throws IOException {
		File root = GeneralConfig.MAIN_APP_PARENT_DIR;
		String[] names = SysUtils.getOsInfoLazy().isWindows() ? new String[] { "ScreepT.bat", "ScreepT.exe" } : new String[] { "ScreepT.sh" };
		for (String name : names) {
			File file = new File(root, name);
			if (file.isFile()) {
				if (!SysUtils.getOsInfoLazy().isWindows()) {
					makeExecutable(file);
				}
				return file.getCanonicalFile();
			}
		}
		throw new IOException(I18n.fmt("launcher.starter_not_found", names[0], String.valueOf(root)));
	}

	private static String iconPath() {
		return new File(ImagesPathConfigurator.MAIN_APP_ICON).getAbsolutePath();
	}

	private static File dataHome() {
		String data = System.getenv("XDG_DATA_HOME");
		return data != null && data.startsWith("/") ? new File(data) : new File(System.getProperty("user.home"), ".local/share");
	}

	private static File desktopFolder() throws IOException {
		String answer = run("xdg-user-dir", "DESKTOP");
		File home = new File(System.getProperty("user.home"));
		File folder = answer == null || answer.trim().isEmpty() ? null : new File(answer.trim());
		if (folder == null || !folder.isDirectory() || folder.equals(home)) {
			folder = new File(home, "Desktop");
		}
		return folder;
	}

	private static void writeMenuEntry(File starter, boolean openWith) throws IOException {
		File applications = new File(dataHome(), "applications");
		File entry = new File(applications, ENTRY_NAME);
		String content = desktopEntry(starter.getAbsolutePath(), iconPath());
		write(entry, openWith ? content : withoutFileTypes(content));
		makeExecutable(entry);
		run("update-desktop-database", applications.getAbsolutePath());
	}

	private static void removeMenuEntry() throws IOException {
		File applications = new File(dataHome(), "applications");
		delete(new File(applications, ENTRY_NAME));
		run("update-desktop-database", applications.getAbsolutePath());
	}

	private static void delete(File file) throws IOException {
		if (Files.deleteIfExists(file.toPath())) {
			logger.info("Deleted " + file);
		}
	}

	private static void write(File file, String content) throws IOException {
		Files.createDirectories(file.getParentFile().toPath());
		Files.write(file.toPath(), content.getBytes(StandardCharsets.UTF_8));
		logger.info("Written " + file);
	}

	private static void makeExecutable(File file) {
		if (!file.setExecutable(true)) {
			logger.warn("Cannot make executable: " + file);
		}
	}

	/** Content of the .desktop file (freedesktop.org Desktop Entry) */
	static String desktopEntry(String starterPath, String iconPath) {
		String folder = starterPath.substring(0, Math.max(1, starterPath.lastIndexOf('/')));
		return "[Desktop Entry]\n"
				+ "Type=Application\n"
				+ "Version=1.0\n"
				+ "Name=ScreepT\n"
				+ "Comment=Text and script editor, crypter and tools\n"
				+ desktopExec(starterPath) + "\n"
				+ "Path=" + value(folder) + "\n"
				+ "Icon=" + value(iconPath) + "\n"
				+ "Terminal=false\n"
				+ "Categories=Utility;TextEditor;Development;\n"
				+ "StartupWMClass=" + WINDOW_CLASS + "\n"
				+ "MimeType=" + MIME_TYPES + "\n";
	}

	private static String desktopExec(String starterPath) {
		return "Exec=" + execArgument(starterPath) + " %F";
	}

	static String withoutFileTypes(String desktopEntry) {
		return desktopEntry.replaceFirst("(?m)^MimeType=.*\n", "");
	}

	/** A plain value of a .desktop file: backslashes are escapes */
	private static String value(String text) {
		return text.replace("\\", "\\\\");
	}

	/** A quoted argument of an Exec line: first the quoting rules of the command line, then those of the file */
	static String execArgument(String path) {
		StringBuilder quoted = new StringBuilder("\"");
		for (char c : path.toCharArray()) {
			if (c == '"' || c == '`' || c == '$' || c == '\\') {
				quoted.append('\\');
			}
			quoted.append(c);
			if (c == '%') {
				quoted.append('%');
			}
		}
		return value(quoted.append('"').toString());
	}

	static String nemoAction(String starterPath) {
		return "[Nemo Action]\n"
				+ "Active=true\n"
				+ "Name=Open with ScreepT\n"
				+ "Comment=Open the selected files with ScreepT\n"
				+ nemoExec(starterPath) + "\n"
				+ "Icon-Name=accessories-text-editor\n" // Nemo takes only names of the icon theme
				+ "Selection=notnone\n"
				+ "Extensions=nodirs;\n"
				+ "Quote=double\n";
	}

	private static String nemoExec(String starterPath) {
		return "Exec=\"" + starterPath.replace("\\", "\\\\").replace("\"", "\\\"").replace("%", "%%") + "\" %F";
	}

	////////// PANEL ////////////////

	/** Adds ScreepT to the pinned applications of every window list of the Cinnamon panel */
	static String pinToCinnamon(File home) throws IOException {
		boolean added = false, present = false;
		for (File file : cinnamonSettings(home)) {
			String json = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
			String changed = addPinned(json, ENTRY_NAME);
			if (changed == null) {
				continue; // not the expected content: left as it is
			}
			if (changed.equals(json)) {
				present = true;
				continue;
			}
			replaceSettings(file, changed);
			added = true;
		}
		if (added) {
			return I18n.t("launcher.pinned_to_panel");
		}
		return present ? I18n.t("launcher.already_in_panel") : I18n.t("launcher.added_to_menu_pin_by_hand");
	}

	/**
	 * @return the settings with id among the "value" of "pinned-apps" (the same text when it is already there), or null when the
	 *         settings do not have that list
	 */
	static String addPinned(String json, String id) {
		Matcher list = pinnedList(json);
		if (list == null) {
			return null;
		}
		String content = list.group(1);
		if (content.contains("\"" + id + "\"")) {
			return json;
		}
		String kept = content.replaceAll("\\s+$", "");
		String added = (kept.isEmpty() ? "" : kept + ", ") + "\"" + id + "\"" + content.substring(kept.length());
		return json.substring(0, list.start(1)) + added + json.substring(list.end(1));
	}

	/** Inverse of {@link #addPinned}: the same text when id is not there, null when the settings do not have the list */
	static String removePinned(String json, String id) {
		Matcher list = pinnedList(json);
		if (list == null) {
			return null;
		}
		return json.substring(0, list.start(1)) + withoutItem(list.group(1), "\"" + id + "\"") + json.substring(list.end(1));
	}

	/** @return a matcher whose group 1 is the content of the "value" list of "pinned-apps", or null */
	private static Matcher pinnedList(String json) {
		int key = json.indexOf("\"pinned-apps\"");
		if (key < 0) {
			return null;
		}
		// the list of the object of this key only: no other object may start or end in between
		Matcher list = Pattern.compile("\"pinned-apps\"\\s*:\\s*\\{(?:[^{}]*?)\"value\"\\s*:\\s*\\[([^\\[\\]]*)\\]").matcher(json);
		return list.find(key) ? list : null;
	}

	/** Removes a quoted item, with its comma, from the content of a list */
	private static String withoutItem(String content, String item) {
		String quoted = Pattern.quote(item);
		return content.replaceAll(",\\s*" + quoted, "").replaceAll(quoted + "\\s*,\\s*", "").replaceAll("\\s*" + quoted, "");
	}

	static boolean isPinnedInCinnamon(File home) throws IOException {
		for (File file : cinnamonSettings(home)) {
			Matcher list = pinnedList(new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8));
			if (list != null && list.group(1).contains("\"" + ENTRY_NAME + "\"")) {
				return true;
			}
		}
		return false;
	}

	static void unpinFromCinnamon(File home) throws IOException {
		for (File file : cinnamonSettings(home)) {
			String json = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
			String changed = removePinned(json, ENTRY_NAME);
			if (changed != null && !changed.equals(json)) {
				replaceSettings(file, changed);
			}
		}
	}

	private static List<File> cinnamonSettings(File home) {
		List<File> settings = new ArrayList<File>();
		for (String folder : new String[] { ".config/cinnamon/spices/" + CINNAMON_APPLET, ".cinnamon/configs/" + CINNAMON_APPLET }) {
			File[] files = new File(home, folder).listFiles((dir, name) -> name.endsWith(".json"));
			if (files != null) {
				settings.addAll(Arrays.asList(files));
			}
		}
		return settings;
	}

	/** Writes the settings of the panel, keeping a copy of the previous ones */
	private static void replaceSettings(File file, String content) throws IOException {
		Files.copy(file.toPath(), new File(file.getPath() + ".before-screept").toPath(), StandardCopyOption.REPLACE_EXISTING);
		write(file, content);
	}

	private static void unpinFromGnome() throws IOException {
		String current = run("gsettings", "get", "org.gnome.shell", "favorite-apps");
		String changed = removeFavorite(current, ENTRY_NAME);
		if (changed == null || !changed.equals(current.trim()) && run("gsettings", "set", "org.gnome.shell", "favorite-apps", changed) == null) {
			throw new IOException(I18n.t("launcher.cannot_remove_from_panel"));
		}
	}

	/** Same of {@link #removePinned} for the list printed by gsettings */
	static String removeFavorite(String current, String id) {
		String list = favoriteList(current);
		if (list == null) {
			return null;
		}
		return list.contains("'" + id + "'") ? withoutItem(list, "'" + id + "'") : current.trim();
	}

	private static String favoriteList(String current) {
		if (current == null) {
			return null;
		}
		String list = current.trim();
		list = list.startsWith("@as ") ? list.substring(4) : list;
		return list.startsWith("[") && list.endsWith("]") ? list : null;
	}

	private static String pinToGnome() throws IOException {
		String current = run("gsettings", "get", "org.gnome.shell", "favorite-apps");
		String changed = addFavorite(current, ENTRY_NAME);
		if (changed == null) {
			return I18n.t("launcher.added_to_menu_pin_by_hand");
		}
		if (changed.equals(current.trim())) {
			return I18n.t("launcher.already_in_panel");
		}
		return run("gsettings", "set", "org.gnome.shell", "favorite-apps", changed) != null ? I18n.t("launcher.pinned_to_panel")
				: I18n.t("launcher.added_to_menu_pin_by_hand");
	}

	/** Same of {@link #addPinned} for the list printed by gsettings, e.g. ['a.desktop', 'b.desktop'] */
	static String addFavorite(String current, String id) {
		String list = favoriteList(current);
		if (list == null) {
			return null;
		}
		if (list.contains("'" + id + "'")) {
			return current.trim();
		}
		String inside = list.substring(1, list.length() - 1).trim();
		return "[" + (inside.isEmpty() ? "" : inside + ", ") + "'" + id + "']";
	}

	////////// PROCESSES ////////////////

	/** Shortcut in a special folder of the current user ("Desktop" or "Programs", the Start menu) */
	private static void windowsShortcut(String specialFolder, File starter) throws IOException {
		File icon = new File("Resources_ScreepT/Icons/ScreepT.ico").getAbsoluteFile();
		boolean bat = starter.getName().toLowerCase(Locale.ROOT).endsWith(".bat");
		StringBuilder script = new StringBuilder("$ErrorActionPreference='Stop'\r\n")
				.append("$path=Join-Path ([Environment]::GetFolderPath('").append(specialFolder).append("')) 'ScreepT.lnk'\r\n")
				.append("$link=(New-Object -ComObject WScript.Shell).CreateShortcut($path)\r\n");
		if (bat) {
			// through cmd.exe: ScreepT.exe can be blocked on company PCs
			script.append("$link.TargetPath=$env:ComSpec\r\n")
					.append("$link.Arguments='/c \"\"").append(quote(starter.getPath())).append("\"\"'\r\n");
		} else {
			script.append("$link.TargetPath='").append(quote(starter.getPath())).append("'\r\n");
		}
		script.append("$link.WorkingDirectory='").append(quote(starter.getParent())).append("'\r\n")
				.append("$link.WindowStyle=7\r\n")
				.append("$link.Description='ScreepT'\r\n");
		if (icon.isFile()) {
			script.append("$link.IconLocation='").append(quote(icon.getPath())).append("'\r\n");
		}
		script.append("$link.Save()\r\n");
		String encoded = Base64.getEncoder().encodeToString(script.toString().getBytes(Charset.forName("UTF-16LE")));
		if (run("powershell.exe", "-NoProfile", "-NonInteractive", "-EncodedCommand", encoded) == null) {
			throw new IOException(I18n.t("launcher.cannot_create_shortcut"));
		}
	}

	/** Inside a PowerShell single quoted string a quote is doubled */
	private static String quote(String text) {
		return text.replace("'", "''");
	}

	/** @return what the command printed, or null if it is missing, failed or did not end in time */
	private static String run(String... command) {
		File output = null;
		try {
			output = File.createTempFile("screept-desktop", ".txt");
			Process process = new ProcessBuilder(command).redirectErrorStream(true).redirectOutput(output).start();
			process.getOutputStream().close();
			if (!process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
				process.destroyForcibly();
				logger.warn("Timeout: " + command[0]);
				return null;
			}
			String printed = new String(Files.readAllBytes(output.toPath()), Charset.defaultCharset());
			if (process.exitValue() != 0) {
				logger.warn(command[0] + " failed: " + printed.trim());
				return null;
			}
			return printed;
		} catch (IOException e) {
			logger.warn("Cannot run " + command[0], e);
			return null;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return null;
		} finally {
			if (output != null) {
				output.delete();
			}
		}
	}
}

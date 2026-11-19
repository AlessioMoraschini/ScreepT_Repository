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
package screept.gui.panels.quicknotes;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * Workspaces of the quick notes: separate notebooks, each one a Markdown file of its own in the notes folder.
 * The first one is the classic quick_notes.md, so the notes already written need no migration.
 * The list, the names and the current workspace are kept in quick_notes_workspaces.properties.
 * File names come from internal ids, never from what the user types: names are only labels.
 * Deleting a workspace moves its file to the "deleted" folder, it is never erased.
 */
public final class QuickNotesWorkspaces {

	private static final Logger logger = Logger.getLogger(QuickNotesWorkspaces.class);

	public static final String MAIN_ID = "main";
	private static final String MAIN_NAME = "Notes";
	private static final int NAME_MAX = 40;
	private static final String SETTINGS_FILE = "quick_notes_workspaces.properties";
	private static final String DELETED_DIR = "deleted";
	private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

	private final File dir;
	/** Id to name, in display order */
	private final Map<String, String> names = new LinkedHashMap<String, String>();
	private final Map<String, QuickNotesStore> stores = new HashMap<String, QuickNotesStore>();
	private String currentId = MAIN_ID;

	public QuickNotesWorkspaces(File dir) {
		this.dir = dir.getAbsoluteFile();
		load();
	}

	/** Ids in display order */
	public synchronized List<String> ids() {
		return new ArrayList<String>(names.keySet());
	}

	public synchronized String name(String id) {
		return names.get(id);
	}

	public synchronized String currentId() {
		return currentId;
	}

	/** Store of the current workspace */
	public synchronized QuickNotesStore current() {
		return store(currentId);
	}

	public synchronized QuickNotesStore store(String id) {
		QuickNotesStore store = stores.get(id);
		if (store == null) {
			store = new QuickNotesStore(fileOf(id));
			stores.put(id, store);
		}
		return store;
	}

	public synchronized void setCurrent(String id) {
		if (names.containsKey(id)) {
			currentId = id;
			save();
		}
	}

	/** Creates an empty workspace and makes it the current one; null if the name is empty or already used */
	public synchronized String create(String name) {
		String clean = cleanName(name);
		if (clean == null || nameTaken(clean, null)) {
			return null;
		}
		String id = "ws" + Long.toString(System.currentTimeMillis(), 36);
		while (names.containsKey(id)) {
			id = id + "x";
		}
		names.put(id, clean);
		currentId = id;
		save();
		return id;
	}

	/** Renames a workspace; false if the name is empty or already used by another one */
	public synchronized boolean rename(String id, String name) {
		String clean = cleanName(name);
		if (clean == null || !names.containsKey(id) || nameTaken(clean, id)) {
			return false;
		}
		names.put(id, clean);
		save();
		return true;
	}

	/** Deletes a workspace: its notes are moved to the "deleted" folder. The last workspace cannot be deleted. */
	public synchronized boolean delete(String id) throws IOException {
		if (names.size() <= 1 || !names.containsKey(id)) {
			return false;
		}
		QuickNotesStore store = store(id);
		store.flush();
		store.discard();
		stores.remove(id);
		File file = fileOf(id);
		if (file.exists()) {
			File deleted = new File(dir, DELETED_DIR);
			if (!deleted.exists() && !deleted.mkdirs()) {
				throw new IOException("Cannot create folder " + deleted);
			}
			String base = file.getName().replaceFirst("\\.md$", "");
			Files.move(file.toPath(), new File(deleted, base + "_" + LocalDateTime.now().format(STAMP) + ".md").toPath());
		}
		names.remove(id);
		if (currentId.equals(id)) {
			currentId = names.keySet().iterator().next();
		}
		save();
		return true;
	}

	/**
	 * Creates a workspace with the notes of a quick notes file (the file is only read) and makes it the current one.
	 * Returns null, and changes nothing, if the name is empty or already used.
	 */
	public synchronized String importAsNew(String name, File source) throws IOException {
		String id = create(name);
		if (id == null) {
			return null;
		}
		QuickNotesStore store = store(id);
		store.importFrom(source);
		store.flush();
		return id;
	}

	/** Writes the pending changes of every workspace (on closing, or before showing another one) */
	public synchronized void flushAll() {
		for (QuickNotesStore store : stores.values()) {
			store.flush();
		}
	}

	private File fileOf(String id) {
		return MAIN_ID.equals(id) ? new File(dir, QuickNotesStore.DEFAULT_FILE.getName()) : new File(dir, "quick_notes_" + id + ".md");
	}

	private static String cleanName(String name) {
		if (name == null) {
			return null;
		}
		String clean = name.trim().replaceAll("\\s+", " ");
		if (clean.isEmpty()) {
			return null;
		}
		return clean.length() > NAME_MAX ? clean.substring(0, NAME_MAX) : clean;
	}

	/** Another workspace (not exceptId) already has this name, ignoring case */
	private boolean nameTaken(String name, String exceptId) {
		for (Map.Entry<String, String> entry : names.entrySet()) {
			if (!entry.getKey().equals(exceptId) && entry.getValue().equalsIgnoreCase(name)) {
				return true;
			}
		}
		return false;
	}

	private void load() {
		Properties p = new Properties();
		File settings = new File(dir, SETTINGS_FILE);
		if (settings.exists()) {
			try (InputStream in = Files.newInputStream(settings.toPath())) {
				p.load(in);
			} catch (IOException e) {
				logger.warn("Cannot read quick notes workspaces " + settings, e);
			}
		}
		for (String id : p.getProperty("ids", "").split(",")) {
			String name = p.getProperty("name." + id.trim());
			if (!id.trim().isEmpty() && name != null && !names.containsKey(id.trim())) {
				names.put(id.trim(), name);
			}
		}
		if (names.isEmpty()) {
			names.put(MAIN_ID, MAIN_NAME);
		}
		String saved = p.getProperty("current", MAIN_ID);
		currentId = names.containsKey(saved) ? saved : names.keySet().iterator().next();
	}

	private void save() {
		Properties p = new Properties();
		p.setProperty("current", currentId);
		p.setProperty("ids", String.join(",", names.keySet()));
		for (Map.Entry<String, String> entry : names.entrySet()) {
			p.setProperty("name." + entry.getKey(), entry.getValue());
		}
		try {
			if (!dir.exists() && !dir.mkdirs()) {
				throw new IOException("Cannot create folder " + dir);
			}
			try (OutputStream out = Files.newOutputStream(new File(dir, SETTINGS_FILE).toPath())) {
				p.store(out, "ScreepT quick notes workspaces");
			}
		} catch (IOException e) {
			logger.warn("Cannot save quick notes workspaces", e);
		}
	}
}

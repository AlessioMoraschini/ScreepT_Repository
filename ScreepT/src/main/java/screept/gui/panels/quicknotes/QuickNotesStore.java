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
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Timer;

import org.apache.log4j.Logger;

import resources.GeneralConfig;

/**
 * Persists quick notes as a readable Markdown task list ("- [ ] text"), so the file can also be opened in the editor.
 * Every change is saved automatically after a short pause; writes are atomic and the previous version is kept as .bak.
 */
public class QuickNotesStore {

	private static final Logger logger = Logger.getLogger(QuickNotesStore.class);

	public static final File DEFAULT_FILE = new File(GeneralConfig.RESOURCES_DIR + "QuickNotes/quick_notes.md");

	private static final String HEADER = "# ScreepT quick notes";
	private static final String COLLAPSED_FLAG = "<!-- completed-collapsed -->";
	/** Line with the tags of the workspace, also the ones without notes: "<!-- tags: a,b -->" */
	private static final String TAGS_PREFIX = "<!-- tags:";
	private static final Pattern NOTE_LINE = Pattern.compile("^- \\[([ xX])\\] (.*?)(?:\\s*<!-- (\\S+)(?: tags=(\\S*))? -->)?$");
	private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
	private static final int SAVE_DELAY_MS = 400;

	/** Name of the file written in the backup folder (always the same: "update backup" overwrites it) */
	public static final String BACKUP_FILE_NAME = "quick_notes_backup.md";
	private static final String BACKUP_DIR_KEY = "backupFolder";

	private final File file;
	private final List<QuickNote> notes = new ArrayList<>();
	private final Set<String> knownTags = new TreeSet<>();
	private final Timer saveTimer;
	private volatile boolean dirty;
	private volatile boolean completedCollapsed;
	private volatile boolean discarded;

	public QuickNotesStore(File file) {
		this.file = file.getAbsoluteFile();
		saveTimer = new Timer(SAVE_DELAY_MS, e -> flush());
		saveTimer.setRepeats(false);
		load();
		// last edits typed right before closing the application must not be lost
		Runtime.getRuntime().addShutdownHook(new Thread(this::flush, "QuickNotes-flush"));
	}

	/** Tags of the workspace, also the ones that no note has (created from the tag bar) */
	public synchronized List<String> getKnownTags() {
		return new ArrayList<>(knownTags);
	}

	/** Adds a tag to the workspace; false when it was already there */
	public synchronized boolean addKnownTag(String tag) {
		if (tag == null || tag.isEmpty() || !knownTags.add(tag)) {
			return false;
		}
		changed();
		return true;
	}

	/** Snapshot of the notes, in saved order */
	public synchronized List<QuickNote> getNotes() {
		return new ArrayList<>(notes);
	}

	public synchronized boolean contains(QuickNote note) {
		return notes.contains(note);
	}

	/** Adds a note on top of the list */
	public synchronized QuickNote addFirst(String text) {
		QuickNote note = new QuickNote(text);
		notes.add(0, note);
		changed();
		return note;
	}

	/** Adds an empty note right after the given one */
	public synchronized QuickNote addAfter(QuickNote previous) {
		QuickNote note = new QuickNote("");
		int index = notes.indexOf(previous);
		notes.add(index < 0 ? 0 : index + 1, note);
		changed();
		return note;
	}

	public synchronized void remove(QuickNote note) {
		if (notes.remove(note)) {
			changed();
		}
	}

	/** Puts a note back at the given position (undo of a deletion) */
	public synchronized void insert(QuickNote note, int index) {
		notes.add(Math.max(0, Math.min(index, notes.size())), note);
		changed();
	}

	/** Removes every completed note of this workspace; returns how many */
	public synchronized int removeCompleted() {
		int before = notes.size();
		notes.removeIf(QuickNote::isDone);
		int removed = before - notes.size();
		if (removed > 0) {
			changed();
		}
		return removed;
	}

	/** Moves the note up (delta < 0) or down among the notes with the same done state */
	public synchronized boolean move(QuickNote note, int delta) {
		int index = notes.indexOf(note);
		for (int i = index + delta; index >= 0 && i >= 0 && i < notes.size(); i += delta) {
			if (notes.get(i).isDone() == note.isDone()) {
				notes.remove(index);
				notes.add(i, note);
				changed();
				return true;
			}
		}
		return false;
	}

	public boolean isCompletedCollapsed() {
		return completedCollapsed;
	}

	public void setCompletedCollapsed(boolean completedCollapsed) {
		this.completedCollapsed = completedCollapsed;
		changed();
	}

	/** Marks the notes as modified: they are saved after a short pause */
	public void changed() {
		dirty = true;
		saveTimer.restart();
	}

	/** Saves now if there are pending changes */
	public synchronized void flush() {
		if (!dirty || discarded) {
			return;
		}
		saveTimer.stop();
		try {
			write(serialize());
			dirty = false;
		} catch (Exception e) {
			logger.error("Cannot save quick notes to " + file, e);
		}
	}

	/** The workspace was deleted: nothing more is written for it */
	public synchronized void discard() {
		discarded = true;
		dirty = false;
		saveTimer.stop();
	}

	public File getFile() {
		return file;
	}

	private void write(String content) throws IOException {
		File dir = file.getParentFile();
		if (!dir.exists() && !dir.mkdirs()) {
			throw new IOException("Cannot create folder " + dir);
		}
		Path target = file.toPath();
		Path tmp = new File(dir, file.getName() + ".tmp").toPath();
		Files.write(tmp, content.getBytes(StandardCharsets.UTF_8));
		if (Files.exists(target)) {
			Files.copy(target, backupFile().toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
		try {
			Files.move(tmp, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
		} catch (AtomicMoveNotSupportedException e) {
			Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);
		}
	}

	private synchronized String serialize() {
		StringBuilder sb = new StringBuilder(HEADER).append("\r\n");
		if (completedCollapsed) {
			sb.append(COLLAPSED_FLAG).append("\r\n");
		}
		if (!knownTags.isEmpty()) {
			sb.append(TAGS_PREFIX).append(" ").append(String.join(",", knownTags)).append(" -->").append("\r\n");
		}
		sb.append("\r\n");
		for (QuickNote note : notes) {
			if (note.isBlank()) {
				continue;
			}
			sb.append("- [").append(note.isDone() ? 'x' : ' ').append("] ")
				.append(escape(note.getText()))
				.append("  <!-- ").append(STAMP.format(note.getCreated()));
			if (!note.getTags().isEmpty()) {
				sb.append(" tags=").append(String.join(",", note.getTags()));
			}
			sb.append(" -->\r\n");
		}
		return sb.toString();
	}

	private synchronized void load() {
		File source = file.exists() ? file : backupFile();
		if (!source.exists()) {
			return;
		}
		try {
			for (String line : Files.readAllLines(source.toPath(), StandardCharsets.UTF_8)) {
				if (COLLAPSED_FLAG.equals(line.trim())) {
					completedCollapsed = true;
					continue;
				}
				if (line.trim().startsWith(TAGS_PREFIX)) {
					knownTags.addAll(parseKnownTags(line));
					continue;
				}
				Matcher m = NOTE_LINE.matcher(line);
				if (m.matches()) {
					notes.add(parseLine(line));
				}
			}
			logger.debug("Quick notes loaded: " + notes.size() + " from " + source);
		} catch (Exception e) {
			logger.error("Cannot read quick notes from " + source, e);
		}
	}

	// ---- backup / import ----

	/** Folder chosen by the user for the backup, or null if none was chosen yet */
	public File getBackupFolder() {
		Properties p = new Properties();
		File settings = settingsFile();
		if (settings.exists()) {
			try (InputStream in = Files.newInputStream(settings.toPath())) {
				p.load(in);
			} catch (IOException e) {
				logger.warn("Cannot read quick notes settings " + settings, e);
			}
		}
		String path = p.getProperty(BACKUP_DIR_KEY);
		return path == null || path.isEmpty() ? null : new File(path);
	}

	public void setBackupFolder(File folder) {
		Properties p = new Properties();
		p.setProperty(BACKUP_DIR_KEY, folder.getAbsolutePath());
		File settings = settingsFile();
		try {
			settings.getParentFile().mkdirs();
			try (OutputStream out = Files.newOutputStream(settings.toPath())) {
				p.store(out, "ScreepT quick notes settings");
			}
		} catch (IOException e) {
			logger.warn("Cannot save quick notes settings " + settings, e);
		}
	}

	/** Writes (or overwrites) the backup file in the given folder, with the current notes; returns the file written */
	public synchronized File backupTo(File folder, String fileName) throws IOException {
		if (!folder.exists() && !folder.mkdirs()) {
			throw new IOException("Cannot create folder " + folder);
		}
		File target = new File(folder, fileName);
		Path tmp = new File(folder, fileName + ".tmp").toPath();
		Files.write(tmp, serialize().getBytes(StandardCharsets.UTF_8));
		try {
			Files.move(tmp, target.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
		} catch (AtomicMoveNotSupportedException e) {
			Files.move(tmp, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
		return target;
	}

	/** Number of notes a backup file contains, or -1 if it is not a quick notes file */
	public static int countNotesIn(File backup) {
		try {
			List<String> lines = Files.readAllLines(backup.toPath(), StandardCharsets.UTF_8);
			if (lines.isEmpty() || !HEADER.equals(lines.get(0).trim())) {
				return -1;
			}
			int count = 0;
			for (String line : lines) {
				if (NOTE_LINE.matcher(line).matches()) {
					count++;
				}
			}
			return count;
		} catch (Exception e) {
			return -1;
		}
	}

	/** Replaces ALL the current notes with the ones of the backup file (the caller already asked the user) */
	public synchronized void importFrom(File backup) throws IOException {
		List<QuickNote> imported = new ArrayList<>();
		boolean collapsed = false;
		Set<String> importedKnown = new TreeSet<>();
		for (String line : Files.readAllLines(backup.toPath(), StandardCharsets.UTF_8)) {
			if (COLLAPSED_FLAG.equals(line.trim())) {
				collapsed = true;
				continue;
			}
			if (line.trim().startsWith(TAGS_PREFIX)) {
				importedKnown.addAll(parseKnownTags(line));
				continue;
			}
			Matcher m = NOTE_LINE.matcher(line);
			if (m.matches()) {
				imported.add(parseLine(line));
			}
		}
		notes.clear();
		notes.addAll(imported);
		completedCollapsed = collapsed;
		knownTags.clear();
		knownTags.addAll(importedKnown);
		changed();
	}

	private File settingsFile() {
		return new File(file.getParentFile(), "quick_notes_settings.properties");
	}

	private File backupFile() {
		return new File(file.getParentFile(), file.getName() + ".bak");
	}

	/** The tags of a "<!-- tags: a,b -->" line */
	static List<String> parseKnownTags(String line) {
		String body = line.trim().substring(TAGS_PREFIX.length()).replace("-->", "");
		return QuickNote.parseTags(body);
	}

	/** One note from a line of the file, or null when the line is not a note */
	static QuickNote parseLine(String line) {
		Matcher m = NOTE_LINE.matcher(line);
		if (!m.matches()) {
			return null;
		}
		List<String> tags = m.group(4) == null ? Collections.<String>emptyList() : QuickNote.parseTags(m.group(4));
		return new QuickNote(unescape(m.group(2)), !" ".equals(m.group(1)), parseStamp(m.group(3)), tags);
	}

	private static LocalDateTime parseStamp(String stamp) {
		try {
			return stamp == null ? null : LocalDateTime.parse(stamp, STAMP);
		} catch (DateTimeParseException e) {
			return null;
		}
	}

	/** One note per line: backslashes and line breaks are escaped */
	static String escape(String text) {
		return text.replace("\\", "\\\\").replace("\r", "").replace("\n", "\\n");
	}

	static String unescape(String text) {
		StringBuilder sb = new StringBuilder(text.length());
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (c == '\\' && i + 1 < text.length()) {
				char next = text.charAt(++i);
				sb.append(next == 'n' ? '\n' : next);
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}
}

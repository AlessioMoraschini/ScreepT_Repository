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
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
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
	private static final Pattern NOTE_LINE = Pattern.compile("^- \\[([ xX])\\] (.*?)(?:\\s*<!-- (\\S+) -->)?$");
	private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
	private static final int SAVE_DELAY_MS = 400;

	private final File file;
	private final List<QuickNote> notes = new ArrayList<>();
	private final Timer saveTimer;
	private volatile boolean dirty;
	private volatile boolean completedCollapsed;

	public QuickNotesStore(File file) {
		this.file = file.getAbsoluteFile();
		saveTimer = new Timer(SAVE_DELAY_MS, e -> flush());
		saveTimer.setRepeats(false);
		load();
		// last edits typed right before closing the application must not be lost
		Runtime.getRuntime().addShutdownHook(new Thread(this::flush, "QuickNotes-flush"));
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
		if (!dirty) {
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
		sb.append("\r\n");
		for (QuickNote note : notes) {
			if (note.isBlank()) {
				continue;
			}
			sb.append("- [").append(note.isDone() ? 'x' : ' ').append("] ")
				.append(escape(note.getText()))
				.append("  <!-- ").append(STAMP.format(note.getCreated())).append(" -->\r\n");
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
				Matcher m = NOTE_LINE.matcher(line);
				if (m.matches()) {
					notes.add(new QuickNote(unescape(m.group(2)), !" ".equals(m.group(1)), parseStamp(m.group(3))));
				}
			}
			logger.debug("Quick notes loaded: " + notes.size() + " from " + source);
		} catch (Exception e) {
			logger.error("Cannot read quick notes from " + source, e);
		}
	}

	private File backupFile() {
		return new File(file.getParentFile(), file.getName() + ".bak");
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

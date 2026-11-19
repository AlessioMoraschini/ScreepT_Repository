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

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Single quick note: a to-do line with its checkbox state and creation time.
 */
public class QuickNote {

	private volatile String text;
	private volatile boolean done;
	private final LocalDateTime created;
	/** Tags: lower case, no duplicates (see parseTags) */
	private volatile List<String> tags = Collections.emptyList();

	public QuickNote(String text) {
		this(text, false, LocalDateTime.now());
	}

	public QuickNote(String text, boolean done, LocalDateTime created) {
		this(text, done, created, Collections.<String>emptyList());
	}

	public QuickNote(String text, boolean done, LocalDateTime created, List<String> tags) {
		this.text = text == null ? "" : text;
		this.done = done;
		this.created = (created == null ? LocalDateTime.now() : created).truncatedTo(ChronoUnit.MINUTES);
		setTags(tags);
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text == null ? "" : text;
	}

	public boolean isDone() {
		return done;
	}

	public void setDone(boolean done) {
		this.done = done;
	}

	public LocalDateTime getCreated() {
		return created;
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = Collections.unmodifiableList(new ArrayList<String>(new LinkedHashSet<String>(tags == null ? Collections.<String>emptyList() : tags)));
	}

	public boolean hasTag(String tag) {
		return tags.contains(tag);
	}

	/** Splits what the user typed ("Work, to do ; ideas") into clean tags: lower case, spaces become '-', only letters, digits, '-', '_' and '.', at most 20 characters, no duplicates */
	public static List<String> parseTags(String input) {
		Set<String> out = new LinkedHashSet<String>();
		if (input != null) {
			for (String part : input.split("[,;]")) {
				String tag = part.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", "-").replaceAll("[^\\p{L}\\p{N}_.\\-]", "");
				if (!tag.isEmpty()) {
					out.add(tag.length() > 20 ? tag.substring(0, 20) : tag);
				}
			}
		}
		return new ArrayList<String>(out);
	}

	public boolean isBlank() {
		return text.trim().isEmpty();
	}
}

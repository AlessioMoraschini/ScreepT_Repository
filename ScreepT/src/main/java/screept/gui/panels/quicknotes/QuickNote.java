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

/**
 * Single quick note: a to-do line with its checkbox state and creation time.
 */
public class QuickNote {

	private volatile String text;
	private volatile boolean done;
	private final LocalDateTime created;

	public QuickNote(String text) {
		this(text, false, LocalDateTime.now());
	}

	public QuickNote(String text, boolean done, LocalDateTime created) {
		this.text = text == null ? "" : text;
		this.done = done;
		this.created = (created == null ? LocalDateTime.now() : created).truncatedTo(ChronoUnit.MINUTES);
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

	public boolean isBlank() {
		return text.trim().isEmpty();
	}
}

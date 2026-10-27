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
package screept.gui.frames.releasenotes;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Curated release notes read from CHANGELOG.md, written for users (not the git log).
 *
 * Format:
 * <pre>
 * ## 0.9.60 - 2026-10-27
 * &gt; One line highlight
 * ### New | Improved | Fixed
 * - item, **bold** and `code` are shown as plain text
 *   indented lines continue the previous item
 * </pre>
 */
public final class ReleaseNotes {

	private static final Pattern VERSION_LINE = Pattern.compile("^##\\s+v?(\\S+)(?:\\s+[-\u2013\u2014]\\s+(.+))?\\s*$");

	private ReleaseNotes() {
	}

	/** One released version */
	public static final class Release {
		public final String version;
		public final String date;
		private String highlight = "";
		private final Map<String, List<String>> sections = new LinkedHashMap<>();

		Release(String version, String date) {
			this.version = version;
			this.date = date == null ? "" : date.trim();
		}

		public String getHighlight() {
			return highlight;
		}

		/** Section title (New, Improved, Fixed...) to its items, in file order */
		public Map<String, List<String>> getSections() {
			return Collections.unmodifiableMap(sections);
		}
	}

	public static List<Release> read(File file) throws IOException {
		return parse(Files.readAllLines(file.toPath(), StandardCharsets.UTF_8));
	}

	public static List<Release> parse(List<String> lines) {
		List<Release> releases = new ArrayList<>();
		Release current = null;
		List<String> items = null;
		for (String raw : lines) {
			String line = raw.replace("\uFEFF", "");
			String trimmed = line.trim();
			Matcher version = VERSION_LINE.matcher(trimmed);
			if (trimmed.startsWith("## ") && version.matches()) {
				current = new Release(version.group(1), version.group(2));
				releases.add(current);
				items = null;
			} else if (current == null || trimmed.isEmpty()) {
				continue;
			} else if (trimmed.startsWith("###")) {
				items = new ArrayList<>();
				current.sections.put(trimmed.replaceFirst("^#+", "").trim(), items);
			} else if (trimmed.startsWith(">")) {
				String text = plain(trimmed.substring(1).trim());
				current.highlight = current.highlight.isEmpty() ? text : current.highlight + " " + text;
			} else if (trimmed.startsWith("- ") || trimmed.startsWith("* ")) {
				if (items == null) {
					items = new ArrayList<>();
					current.sections.put("Changes", items);
				}
				items.add(plain(trimmed.substring(2).trim()));
			} else if (items != null && !items.isEmpty() && Character.isWhitespace(line.charAt(0))) {
				int last = items.size() - 1;
				items.set(last, items.get(last) + " " + plain(trimmed));
			}
		}
		return releases;
	}

	/** Markdown emphasis and code marks removed */
	private static String plain(String text) {
		return text.replace("**", "").replace("`", "");
	}
}

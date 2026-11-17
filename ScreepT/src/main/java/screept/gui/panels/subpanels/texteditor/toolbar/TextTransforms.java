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
package screept.gui.panels.subpanels.texteditor.toolbar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

/**
 * Line and case transformations of the "Text" menu of the file editor toolbar. Pure functions on strings: the end of
 * line style (LF or CRLF) and a final end of line are kept as they are.
 */
public final class TextTransforms {

	private TextTransforms() {
	}

	/** Lines sorted without caring about the case (ties keep the original order) */
	public static String sortLines(String text, boolean ascending) {
		return mapLines(text, lines -> {
			List<String> sorted = new ArrayList<String>(lines);
			Comparator<String> comparator = String.CASE_INSENSITIVE_ORDER;
			Collections.sort(sorted, ascending ? comparator : Collections.reverseOrder(comparator));
			return sorted;
		});
	}

	/** Every line only once, the first occurrence stays where it is */
	public static String removeDuplicateLines(String text) {
		return mapLines(text, lines -> new ArrayList<String>(new LinkedHashSet<String>(lines)));
	}

	/** Removes the empty lines and the ones with only spaces */
	public static String removeEmptyLines(String text) {
		return mapLines(text, lines -> {
			List<String> kept = new ArrayList<String>();
			for (String line : lines) {
				if (!line.trim().isEmpty()) {
					kept.add(line);
				}
			}
			return kept;
		});
	}

	/** Removes spaces and tabs at the end of every line */
	public static String trimTrailingSpaces(String text) {
		return mapLines(text, lines -> {
			List<String> trimmed = new ArrayList<String>();
			for (String line : lines) {
				int end = line.length();
				while (end > 0 && (line.charAt(end - 1) == ' ' || line.charAt(end - 1) == '\t')) {
					end--;
				}
				trimmed.add(line.substring(0, end));
			}
			return trimmed;
		});
	}

	public static String toUpperCase(String text) {
		return text.toUpperCase(Locale.ROOT);
	}

	public static String toLowerCase(String text) {
		return text.toLowerCase(Locale.ROOT);
	}

	/** First letter of every word in capitals, the others in small letters */
	public static String toTitleCase(String text) {
		StringBuilder result = new StringBuilder(text.length());
		boolean startOfWord = true;
		for (int i = 0; i < text.length(); i++) {
			char ch = text.charAt(i);
			if (Character.isLetter(ch)) {
				result.append(startOfWord ? Character.toUpperCase(ch) : Character.toLowerCase(ch));
				startOfWord = false;
			} else {
				result.append(ch);
				startOfWord = !(Character.isDigit(ch) || ch == '\'');
			}
		}
		return result.toString();
	}

	private interface LinesOperation {
		List<String> apply(List<String> lines);
	}

	private static String mapLines(String text, LinesOperation operation) {
		if (text == null || text.isEmpty()) {
			return text;
		}
		String eol = text.contains("\r\n") ? "\r\n" : "\n";
		boolean finalEol = text.endsWith("\n");
		String body = finalEol ? text.substring(0, text.length() - eol.length()) : text;
		List<String> lines = new ArrayList<String>();
		Collections.addAll(lines, body.split("\r?\n", -1));
		List<String> result = operation.apply(lines);
		StringBuilder out = new StringBuilder(text.length());
		for (int i = 0; i < result.size(); i++) {
			if (i > 0) {
				out.append(eol);
			}
			out.append(result.get(i));
		}
		if (finalEol && !result.isEmpty()) {
			out.append(eol);
		}
		return out.toString();
	}
}

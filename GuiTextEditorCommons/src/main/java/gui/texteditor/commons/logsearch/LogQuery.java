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
package gui.texteditor.commons.logsearch;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * What the user typed in the search field. As plain text it is a small search language made for logs: words separated by spaces
 * must all be in the line (in any order), "double quotes" keep a phrase together, and a minus in front of a word or phrase
 * excludes the lines that contain it ({@code error timeout -healthcheck "connection reset"}). As a regular expression the whole
 * text is the expression. Match case and whole word apply in both modes.
 *
 * @throws java.util.regex.PatternSyntaxException when the text is a regular expression that is not valid
 */
final class LogQuery {

	private final List<Pattern> required = new ArrayList<Pattern>();
	private final List<Pattern> excluded = new ArrayList<Pattern>();

	/** Union of the required parts, to tint the matching text of a line (null when there is nothing to tint) */
	final Pattern highlight;

	LogQuery(String text, boolean regex, boolean matchCase, boolean wholeWord) {
		int flags = matchCase ? 0 : Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
		if (regex) {
			if (!text.isEmpty()) {
				required.add(Pattern.compile(wrap(text, wholeWord), flags));
			}
		} else {
			for (String term : split(text)) {
				boolean exclude = term.length() > 1 && term.charAt(0) == '-';
				String literal = exclude ? term.substring(1) : term;
				Pattern pattern = Pattern.compile(wrap(Pattern.quote(literal), wholeWord), flags);
				(exclude ? excluded : required).add(pattern);
			}
		}
		if (required.isEmpty()) {
			highlight = null;
		} else {
			StringBuilder union = new StringBuilder();
			for (Pattern p : required) {
				union.append(union.length() > 0 ? "|" : "").append("(?:").append(p.pattern()).append(')');
			}
			highlight = Pattern.compile(union.toString(), flags);
		}
	}

	private static String wrap(String expression, boolean wholeWord) {
		return wholeWord ? "(?<!\\w)(?:" + expression + ")(?!\\w)" : expression;
	}

	/** Words and "phrases"; a leading minus stays attached to its term */
	private static List<String> split(String text) {
		List<String> terms = new ArrayList<String>();
		StringBuilder current = new StringBuilder();
		boolean quoted = false;
		boolean started = false;
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (c == '"') {
				quoted = !quoted;
				started = true;
			} else if (Character.isWhitespace(c) && !quoted) {
				if (started && current.length() > 0) {
					terms.add(current.toString());
				}
				current.setLength(0);
				started = false;
			} else {
				current.append(c);
				started = true;
			}
		}
		if (started && current.length() > 0) {
			terms.add(current.toString());
		}
		return terms;
	}

	boolean isEmpty() {
		return required.isEmpty() && excluded.isEmpty();
	}

	boolean test(CharSequence line) {
		Matcher matcher = null;
		for (Pattern p : required) {
			matcher = matcher == null ? p.matcher(line) : matcher.reset(line).usePattern(p);
			if (!matcher.find()) {
				return false;
			}
		}
		for (Pattern p : excluded) {
			matcher = matcher == null ? p.matcher(line) : matcher.reset(line).usePattern(p);
			if (matcher.find()) {
				return false;
			}
		}
		return true;
	}
}

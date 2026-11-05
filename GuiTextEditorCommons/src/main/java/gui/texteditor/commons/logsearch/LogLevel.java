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

import java.awt.Color;

/**
 * Severity of a log line, found while the file is indexed: the first level word (ERROR, WARN, INFO, DEBUG, TRACE and their usual
 * synonyms) in the first bytes of the line. The lines of a stack trace that follow an entry take the level of that entry.
 * A level is stored in one byte: the low nibble is the level, {@link #CONTINUATION} marks the inherited ones, {@link #NONE} no level.
 */
final class LogLevel {

	static final int TRACE = 0;
	static final int DEBUG = 1;
	static final int INFO = 2;
	static final int WARN = 3;
	static final int ERROR = 4;
	static final int COUNT = 5;
	static final int NONE = 15;
	static final int CONTINUATION = 0x10;

	static final String[] NAMES = { "TRACE", "DEBUG", "INFO", "WARN", "ERROR" };
	/** Same palette as the rest of the application (green, red, blue, orange, muted) */
	static final Color[] COLORS = { new Color(139, 148, 158), new Color(61, 155, 92), new Color(58, 124, 214), new Color(214, 140, 38), new Color(201, 72, 66) };

	private LogLevel() {
	}

	static int level(byte stored) {
		return stored & 0x0F;
	}

	static boolean isContinuation(byte stored) {
		return (stored & CONTINUATION) != 0;
	}

	/** Level word in the first bytes of a line, or -1 */
	static int detect(byte[] head, int length) {
		for (int i = 0; i < length;) {
			byte c = head[i];
			if (!isLetter(c) || (i > 0 && isAlphaNumeric(head[i - 1]))) {
				i++;
				continue;
			}
			int end = i;
			while (end < length && isLetter(head[end])) {
				end++;
			}
			int size = end - i;
			if (size >= 4 && size <= 8) {
				int level = word(head, i, size);
				if (level >= 0) {
					return level;
				}
			}
			i = end;
		}
		return -1;
	}

	/** Stack trace and wrapped lines: indented, "at ...", "Caused by", "..." */
	static boolean looksLikeContinuation(byte[] head, int length) {
		if (length == 0) {
			return false;
		}
		if (head[0] == ' ' || head[0] == '\t') {
			return true;
		}
		return startsWith(head, length, "at ") || startsWith(head, length, "Caused by") || startsWith(head, length, "...")
				|| startsWith(head, length, "Suppressed") || isExceptionHeader(head, length);
	}

	/** "java.lang.IllegalStateException: message", "com.acme.FooError" and the like */
	private static boolean isExceptionHeader(byte[] head, int length) {
		int end = 0;
		boolean dotted = false;
		while (end < length && head[end] != ' ' && head[end] != ':' && head[end] != '	') {
			if (head[end] == '.') {
				dotted = true;
			} else if (!isAlphaNumeric(head[end]) && head[end] != '$') {
				return false;
			}
			end++;
		}
		return dotted && (endsWith(head, end, "Exception") || endsWith(head, end, "Error") || endsWith(head, end, "Throwable"));
	}

	private static boolean endsWith(byte[] head, int end, String suffix) {
		if (end < suffix.length()) {
			return false;
		}
		for (int i = 0; i < suffix.length(); i++) {
			if (head[end - suffix.length() + i] != suffix.charAt(i)) {
				return false;
			}
		}
		return true;
	}

	private static boolean startsWith(byte[] head, int length, String prefix) {
		if (length < prefix.length()) {
			return false;
		}
		for (int i = 0; i < prefix.length(); i++) {
			if (head[i] != prefix.charAt(i)) {
				return false;
			}
		}
		return true;
	}

	private static int word(byte[] b, int from, int size) {
		switch (size) {
		case 4:
			if (eq(b, from, "INFO")) {
				return INFO;
			}
			return eq(b, from, "WARN") ? WARN : -1;
		case 5:
			if (eq(b, from, "ERROR") || eq(b, from, "FATAL")) {
				return ERROR;
			}
			if (eq(b, from, "DEBUG")) {
				return DEBUG;
			}
			return eq(b, from, "TRACE") ? TRACE : -1;
		case 6:
			return eq(b, from, "SEVERE") ? ERROR : -1;
		case 7:
			return eq(b, from, "WARNING") ? WARN : -1;
		case 8:
			return eq(b, from, "CRITICAL") ? ERROR : -1;
		default:
			return -1;
		}
	}

	private static boolean eq(byte[] b, int from, String upper) {
		for (int i = 0; i < upper.length(); i++) {
			int c = b[from + i];
			if (c >= 'a' && c <= 'z') {
				c -= 32;
			}
			if (c != upper.charAt(i)) {
				return false;
			}
		}
		return true;
	}

	private static boolean isLetter(byte c) {
		return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
	}

	private static boolean isAlphaNumeric(byte c) {
		return isLetter(c) || (c >= '0' && c <= '9') || c == '_';
	}
}

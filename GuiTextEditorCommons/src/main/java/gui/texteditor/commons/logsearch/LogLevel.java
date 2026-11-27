/**
 *
 * =========================================================================================
 *  Copyright (C) 2019-2026
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
	/** Bytes of a line looked at by the formats other than the standard words */
	private static final int OTHER_SCAN = 64;
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
		return detectOther(head, length);
	}

	/**
	 * The other log systems, looked for only on the lines where no standard level word was found (so the common case costs nothing
	 * more): .NET "fail: ...", Google glog "E0102 12:00:00", Android logcat "E/Tag(", Serilog "[INF] [WRN] [ERR]" and the words
	 * java.util.logging (FINE, FINER, FINEST) and syslog (NOTICE, ALERT, EMERG) use, written in capitals because they are also plain words.
	 */
	private static int detectOther(byte[] head, int length) {
		if (length == 0 || head[0] == ' ' || head[0] == '	') {
			return -1; // indented: a stack trace or a wrapped line, it has no level of its own
		}
		int special = lineStart(head, length);
		if (special >= 0) {
			return special;
		}
		length = Math.min(length, OTHER_SCAN); // the level of these formats is at the start of the line
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
			if (size == 3) {
				if (i > 0 && head[i - 1] == '[' && end < length && head[end] == ']') {
					int level = bracketCode(head, i);
					if (level >= 0) {
						return level;
					}
				}
			} else if (size >= 4 && size <= 7) {
				int level = wordOther(head, i, size);
				if (level >= 0) {
					return level;
				}
			}
			i = end;
		}
		return -1;
	}

	private static int wordOther(byte[] b, int from, int size) {
		switch (size) {
		case 4:
			return eqExact(b, from, "FINE") ? DEBUG : -1;
		case 5:
			if (eqExact(b, from, "FINER")) {
				return TRACE;
			}
			return eqExact(b, from, "ALERT") || eqExact(b, from, "EMERG") || eqExact(b, from, "PANIC") ? ERROR : -1;
		case 6:
			if (eqExact(b, from, "FINEST")) {
				return TRACE;
			}
			return eqExact(b, from, "NOTICE") ? INFO : -1;
		case 7:
			return eqExact(b, from, "VERBOSE") ? TRACE : -1;
		default:
			return -1;
		}
	}

	/** Forms that only count at the very start of the line: .NET "fail: ...", Google glog "E0102 12:00:00", Android logcat "E/Tag(" */
	private static int lineStart(byte[] b, int length) {
		if (length >= 6 && b[4] == ':' && isLetter(b[0]) && isLetter(b[3])) {
			if (eqExact(b, 0, "fail") || eqExact(b, 0, "crit")) {
				return ERROR;
			}
			if (eqExact(b, 0, "dbug")) {
				return DEBUG;
			}
			if (eqExact(b, 0, "trce")) {
				return TRACE;
			}
		}
		if (length >= 6 && b[5] == ' ' && isDigit(b[1]) && isDigit(b[2]) && isDigit(b[3]) && isDigit(b[4])) {
			switch (b[0]) {
			case 'I':
				return INFO;
			case 'W':
				return WARN;
			case 'E':
			case 'F':
				return ERROR;
			default:
				break;
			}
		}
		if (length >= 3 && b[1] == '/') {
			switch (b[0]) {
			case 'V':
				return TRACE;
			case 'D':
				return DEBUG;
			case 'I':
				return INFO;
			case 'W':
				return WARN;
			case 'E':
			case 'F':
				return ERROR;
			default:
				break;
			}
		}
		return -1;
	}

	/** The 3 letters of Serilog between square brackets */
	private static int bracketCode(byte[] b, int at) {
		if (eqExact(b, at, "VRB") || eqExact(b, at, "TRC")) {
			return TRACE;
		}
		if (eqExact(b, at, "DBG")) {
			return DEBUG;
		}
		if (eqExact(b, at, "INF")) {
			return INFO;
		}
		if (eqExact(b, at, "WRN")) {
			return WARN;
		}
		return eqExact(b, at, "ERR") || eqExact(b, at, "FTL") ? ERROR : -1;
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

	/** Same letters and capitals as given: for the words that are also common in plain text */
	private static boolean eqExact(byte[] b, int from, String word) {
		for (int i = 0; i < word.length(); i++) {
			if (b[from + i] != word.charAt(i)) {
				return false;
			}
		}
		return true;
	}

	private static boolean isDigit(byte c) {
		return c >= '0' && c <= '9';
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

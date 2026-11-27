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

	/** Plain-text terms as bytes, when every one is ASCII: such a query is matched on the raw bytes, without decoding the line */
	private ByteTerm[] requiredBytes;
	private ByteTerm[] excludedBytes;
	/** Fixed pieces that every line matching a regular expression must contain: cheap byte test run before the regex */
	private ByteTerm[] prefilter = new ByteTerm[0];

	/** Union of the required parts, to tint the matching text of a line (null when there is nothing to tint) */
	final Pattern highlight;

	/** What kind of search this is, for the speed statistics (see {@link SearchStats#KIND_TEXT}) */
	final int kind;

	LogQuery(String text, boolean regex, boolean matchCase, boolean wholeWord) {
		int flags = matchCase ? 0 : Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
		List<ByteTerm> reqB = new ArrayList<ByteTerm>();
		List<ByteTerm> exB = new ArrayList<ByteTerm>();
		boolean ascii = !regex;
		if (regex) {
			if (!text.isEmpty()) {
				required.add(Pattern.compile(wrap(text, wholeWord), flags));
				prefilter = prefilterOf(text, matchCase);
			}
		} else {
			for (String term : split(text)) {
				boolean exclude = term.length() > 1 && term.charAt(0) == '-';
				String literal = exclude ? term.substring(1) : term;
				Pattern pattern = Pattern.compile(wrap(Pattern.quote(literal), wholeWord), flags);
				(exclude ? excluded : required).add(pattern);
				ByteTerm bytes = ByteTerm.of(literal, matchCase, wholeWord);
				if (bytes == null) {
					ascii = false;
				} else {
					(exclude ? exB : reqB).add(bytes);
				}
			}
		}
		if (ascii) {
			requiredBytes = reqB.toArray(new ByteTerm[0]);
			excludedBytes = exB.toArray(new ByteTerm[0]);
		}
		int base = regex ? SearchStats.KIND_REGEX : (required.size() > 1 || !excluded.isEmpty()) ? SearchStats.KIND_MULTI : SearchStats.KIND_TEXT;
		kind = base | (matchCase ? SearchStats.KIND_CASE : 0) | (wholeWord ? SearchStats.KIND_WORD : 0);
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

	/** True when the regular expression has fixed pieces that can be looked for on the raw bytes first */
	boolean hasPrefilter() {
		return prefilter.length > 0;
	}

	/** False means the line cannot match the regular expression (true means: run it) */
	boolean passesPrefilter(byte[] data, int from, int to) {
		for (ByteTerm term : prefilter) {
			if (!term.occursIn(data, from, to)) {
				return false;
			}
		}
		return true;
	}

	private static ByteTerm[] prefilterOf(String regex, boolean matchCase) {
		List<String> literals = requiredLiterals(regex);
		List<ByteTerm> terms = new ArrayList<ByteTerm>();
		for (String literal : literals) {
			ByteTerm term = ByteTerm.of(literal, matchCase, false);
			if (term != null && terms.size() < 3) {
				terms.add(term);
			}
		}
		return terms.toArray(new ByteTerm[0]);
	}

	/**
	 * ASCII pieces (3+ characters, longest first) that any text matched by the regular expression must contain. Conservative: an
	 * alternation, a lookaround, a back reference, an optional group or a quoted block gives no pieces at all.
	 */
	static List<String> requiredLiterals(String re) {
		List<String> runs = new ArrayList<String>();
		StringBuilder run = new StringBuilder();
		int i = 0;
		int n = re.length();
		while (i < n) {
			char c = re.charAt(i);
			if (c == '|') {
				return new ArrayList<String>();
			}
			if (c == '\\') {
				if (i + 1 >= n) {
					return new ArrayList<String>();
				}
				char d = re.charAt(i + 1);
				if (Character.isDigit(d) || "QEkpPxuc".indexOf(d) >= 0) {
					return new ArrayList<String>();
				}
				if (Character.isLetter(d)) { // \d \w \s  ...: not a fixed character
					flush(runs, run);
					i = skipQuantifier(re, i + 2);
				} else {
					i = literal(re, i + 1, d, runs, run);
				}
			} else if (c == '[') {
				flush(runs, run);
				i++;
				while (i < n && re.charAt(i) != ']') {
					i += re.charAt(i) == '\\' ? 2 : 1;
				}
				i = skipQuantifier(re, i + 1);
			} else if (c == '(') {
				if (i + 1 < n && re.charAt(i + 1) == '?') {
					if (i + 2 < n && re.charAt(i + 2) == ':') {
						i += 3;
					} else {
						return new ArrayList<String>();
					}
				} else {
					i++;
				}
			} else if (c == ')') {
				i++;
				if (i < n && (re.charAt(i) == '?' || re.charAt(i) == '*' || re.charAt(i) == '{')) {
					return new ArrayList<String>();
				}
				flush(runs, run);
				i = skipQuantifier(re, i);
			} else if (c == '.' || c == '^' || c == '$') {
				flush(runs, run);
				i = skipQuantifier(re, i + 1);
			} else if (c == '*' || c == '?' || c == '{' || c == '+') {
				flush(runs, run);
				i = skipQuantifier(re, i);
			} else if (c >= 128) {
				flush(runs, run);
				i = skipQuantifier(re, i + 1);
			} else {
				i = literal(re, i, c, runs, run);
			}
		}
		flush(runs, run);
		java.util.Collections.sort(runs, new java.util.Comparator<String>() {
			@Override
			public int compare(String a, String b) {
				return b.length() - a.length();
			}
		});
		return runs;
	}

	/** A fixed character at index: kept when always present, ends the run when a quantifier makes it optional; returns the next index */
	private static int literal(String re, int index, char c, List<String> runs, StringBuilder run) {
		int next = index + 1;
		char q = next < re.length() ? re.charAt(next) : ' ';
		if (q == '*' || q == '?' || q == '{') {
			flush(runs, run);
			return skipQuantifier(re, next);
		}
		run.append(c);
		if (q == '+') {
			flush(runs, run);
			return skipQuantifier(re, next);
		}
		return next;
	}

	private static void flush(List<String> runs, StringBuilder run) {
		if (run.length() >= 3 && !runs.contains(run.toString())) {
			runs.add(run.toString());
		}
		run.setLength(0);
	}

	/** Skips a quantifier (and its lazy or possessive mark) that starts at index, if there is one */
	private static int skipQuantifier(String re, int index) {
		int i = index;
		int n = re.length();
		if (i < n && (re.charAt(i) == '*' || re.charAt(i) == '+' || re.charAt(i) == '?')) {
			i++;
		} else if (i < n && re.charAt(i) == '{') {
			int close = re.indexOf('}', i);
			i = close < 0 ? n : close + 1;
		} else {
			return i;
		}
		if (i < n && (re.charAt(i) == '?' || re.charAt(i) == '+')) {
			i++;
		}
		return i;
	}

	/** True when {@link #testBytes} can be used (plain text made of ASCII characters) */
	boolean bytesCapable() {
		return requiredBytes != null;
	}

	/** Same verdict as {@link #test}, on the raw bytes of the line (UTF-8 or ASCII); only when {@link #bytesCapable()} */
	boolean testBytes(byte[] data, int from, int to) {
		for (ByteTerm term : requiredBytes) {
			if (!term.occursIn(data, from, to)) {
				return false;
			}
		}
		for (ByteTerm term : excludedBytes) {
			if (term.occursIn(data, from, to)) {
				return false;
			}
		}
		return true;
	}

	/** Matchers that are reused from one line to the next (one instance per thread) */
	Tester tester() {
		return new Tester();
	}

	final class Tester {
		private final Matcher[] requiredMatchers = matchers(required);
		private final Matcher[] excludedMatchers = matchers(excluded);

		private Matcher[] matchers(List<Pattern> patterns) {
			Matcher[] result = new Matcher[patterns.size()];
			for (int i = 0; i < result.length; i++) {
				result[i] = patterns.get(i).matcher("");
			}
			return result;
		}

		boolean test(CharSequence line) {
			for (Matcher m : requiredMatchers) {
				if (!m.reset(line).find()) {
					return false;
				}
			}
			for (Matcher m : excludedMatchers) {
				if (m.reset(line).find()) {
					return false;
				}
			}
			return true;
		}
	}

	/** A literal searched in bytes with the Horspool algorithm; letters fold to lower case when the match is not case sensitive */
	private static final class ByteTerm {
		private final byte[] pattern;
		private final int[] skip = new int[256];
		/** Shift for the raw (not yet folded) byte under the last position of the pattern: one table read per step instead of two */
		private final int[] skipRaw = new int[256];
		private final byte[] fold = new byte[256];
		private final boolean wholeWord;

		private ByteTerm(byte[] pattern, byte[] fold, boolean wholeWord) {
			this.pattern = pattern;
			this.wholeWord = wholeWord;
			System.arraycopy(fold, 0, this.fold, 0, 256);
			int m = pattern.length;
			java.util.Arrays.fill(skip, m);
			for (int k = 0; k < m - 1; k++) {
				skip[pattern[k] & 0xFF] = m - 1 - k;
			}
			for (int b = 0; b < 256; b++) {
				skipRaw[b] = skip[this.fold[b] & 0xFF];
			}
		}

		/** Null when the literal is empty or has a non-ASCII character */
		static ByteTerm of(String literal, boolean matchCase, boolean wholeWord) {
			if (literal.isEmpty()) {
				return null;
			}
			byte[] fold = new byte[256];
			for (int i = 0; i < 256; i++) {
				fold[i] = (byte) (!matchCase && i >= 'A' && i <= 'Z' ? i + 32 : i);
			}
			byte[] bytes = new byte[literal.length()];
			for (int i = 0; i < bytes.length; i++) {
				char c = literal.charAt(i);
				if (c >= 128) {
					return null;
				}
				bytes[i] = fold[c];
			}
			return new ByteTerm(bytes, fold, wholeWord);
		}

		private static boolean isWord(byte b) {
			return (b >= 'a' && b <= 'z') || (b >= 'A' && b <= 'Z') || (b >= '0' && b <= '9') || b == '_';
		}

		boolean occursIn(byte[] data, int from, int to) {
			final byte[] p = pattern;
			final int m = p.length;
			final byte[] f = fold;
			final int[] shift = skipRaw;
			final byte last = p[m - 1];
			int i = from + m - 1;
			while (i < to) {
				while (i < to && f[data[i] & 0xFF] != last) { // not the last character of the pattern: jump
					i += shift[data[i] & 0xFF];
				}
				if (i >= to) {
					return false;
				}
				int k = m - 1;
				int j = i;
				while (k >= 0 && f[data[j] & 0xFF] == p[k]) {
					j--;
					k--;
				}
				if (k < 0) {
					int start = j + 1;
					int end = start + m;
					if (!wholeWord || ((start == from || !isWord(data[start - 1])) && (end == to || !isWord(data[end])))) {
						return true;
					}
					i++;
				} else {
					i += shift[data[i] & 0xFF];
				}
			}
			return false;
		}
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

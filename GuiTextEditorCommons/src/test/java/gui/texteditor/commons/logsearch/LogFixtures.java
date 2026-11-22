/**
 *
 * =========================================================================================
 *  Copyright (C) 2019-2020
 *
 *  AM-Design-Development - (Alessio Moraschini) - All Rights Reserved
 * =========================================================================================
 *
 * You should have received a copy of the license with this file.
 * If not, please write to: info@am-design-development.com, or visit : https://www.am-design-development.com
 */
package gui.texteditor.commons.logsearch;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Shared helpers of the LogSearch tests: temp log files with known content, a tiny chunk size (so that lines cross the
 * boundaries of the pieces read by the worker threads) and brute-force references.
 */
final class LogFixtures {

	static {
		// must be set before ChunkScan is loaded: tiny pieces exercise the lines that cross a boundary
		if (System.getProperty("screept.logsearch.chunk") == null) {
			System.setProperty("screept.logsearch.chunk", "1031");
		}
	}

	private LogFixtures() {
	}

	/** Forces the class initialization (the property above) from a {@code @BeforeClass} */
	static void init() {
	}

	static File write(File dir, String name, byte[] data) throws IOException {
		File file = new File(dir, name);
		Files.write(file.toPath(), data);
		return file;
	}

	static byte[] utf8(String text) {
		return text.getBytes(StandardCharsets.UTF_8);
	}

	/** Lines joined with an end of line; the last line is terminated only when {@code trailing} */
	static byte[] join(String eol, boolean trailing, List<String> lines) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < lines.size(); i++) {
			sb.append(lines.get(i));
			if (i < lines.size() - 1 || trailing) {
				sb.append(eol);
			}
		}
		return utf8(sb.toString());
	}

	/** Index of a file, built to the end (fails when it takes more than 30 s) */
	static LogFileIndex index(File file) throws IOException {
		LogFileIndex index = new LogFileIndex(file);
		index.startIndexing();
		waitComplete(index);
		return index;
	}

	static void waitComplete(LogFileIndex index) {
		long limit = System.currentTimeMillis() + 30_000;
		while (!index.isComplete()) {
			if (System.currentTimeMillis() > limit) {
				fail("indexing did not finish");
			}
			try {
				Thread.sleep(2);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				fail("interrupted");
			}
		}
	}

	/** Search run to the end; returns the numbers of the matching lines */
	static List<Integer> search(File file, LogQuery query) {
		LogSearcher searcher = new LogSearcher(file, query);
		searcher.start();
		waitFinished(searcher);
		return hits(searcher);
	}

	static void waitFinished(LogSearcher searcher) {
		long limit = System.currentTimeMillis() + 30_000;
		while (!searcher.isFinished()) {
			if (System.currentTimeMillis() > limit) {
				fail("search did not finish");
			}
			try {
				Thread.sleep(2);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				fail("interrupted");
			}
		}
	}

	static List<Integer> hits(LogSearcher searcher) {
		List<Integer> out = new ArrayList<Integer>();
		for (int i = 0; i < searcher.results.size(); i++) {
			out.add(searcher.results.get(i));
		}
		return out;
	}

	/** Lines of a file as the viewer must see them: split on LF, a CR before the LF dropped, no empty line after the last LF */
	static List<String> refLines(byte[] data) {
		String text = new String(data, StandardCharsets.UTF_8);
		List<String> lines = new ArrayList<String>();
		int start = 0;
		while (start < text.length()) {
			int end = text.indexOf('\n', start);
			int stop = end < 0 ? text.length() : end;
			int contentEnd = stop > start && text.charAt(stop - 1) == '\r' ? stop - 1 : stop;
			lines.add(text.substring(start, contentEnd));
			start = stop + 1;
		}
		return lines;
	}

	/** Brute-force plain-text query: all required terms in, no excluded term in (written without regular expressions) */
	static boolean refPlain(String line, List<String> required, List<String> excluded, boolean matchCase, boolean wholeWord) {
		for (String term : required) {
			if (!contains(line, term, matchCase, wholeWord)) {
				return false;
			}
		}
		for (String term : excluded) {
			if (contains(line, term, matchCase, wholeWord)) {
				return false;
			}
		}
		return true;
	}

	static boolean contains(String line, String term, boolean matchCase, boolean wholeWord) {
		for (int at = 0; at + term.length() <= line.length(); at++) {
			if (!line.regionMatches(!matchCase, at, term, 0, term.length())) {
				continue;
			}
			if (!wholeWord) {
				return true;
			}
			boolean before = at > 0 && isWord(line.charAt(at - 1));
			boolean after = at + term.length() < line.length() && isWord(line.charAt(at + term.length()));
			if (!before && !after) {
				return true;
			}
		}
		return false;
	}

	/** Same notion of word as the engine: ASCII letters, digits, underscore */
	static boolean isWord(char c) {
		return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '_';
	}

	private static final String[] LEVELS = { "TRACE", "DEBUG", "INFO", "WARN", "ERROR" };
	private static final String[] WORDS = { "connection reset by peer", "timeout", "healthcheck ok", "Timeout", "ERR500", "retry 3", "Foo", "foobar", "foo_bar",
			"foo bar", "café", "perché timeout", "user=alice", "user=bob", "disk full", "ERROR inside text", "healthcheck", "x", "connection", "reset" };

	/**
	 * A plausible log in chronological order ({@code days} days from 2026-11-10, ISO dates) with levels, stack traces,
	 * blank lines and some non-ASCII text. Deterministic for a seed.
	 */
	static List<String> randomLog(long seed, int lines, int days) {
		Random random = new Random(seed);
		List<String> out = new ArrayList<String>();
		while (out.size() < lines) {
			int day = 10 + out.size() * days / lines;
			int second = (out.size() * 7919) % 86400;
			String stamp = String.format("2026-11-%02d %02d:%02d:%02d.%03d", day, second / 3600, second / 60 % 60, second % 60, random.nextInt(1000));
			StringBuilder message = new StringBuilder();
			int n = 1 + random.nextInt(4);
			for (int i = 0; i < n; i++) {
				message.append(i == 0 ? "" : " ").append(WORDS[random.nextInt(WORDS.length)]);
			}
			String level = LEVELS[random.nextInt(LEVELS.length)];
			out.add(stamp + " " + level + " [main-" + random.nextInt(8) + "] c.a.Svc - " + message);
			if (level.equals("ERROR") && random.nextInt(3) == 0) {
				out.add("java.lang.IllegalStateException: " + message);
				int depth = 1 + random.nextInt(4);
				for (int d = 0; d < depth; d++) {
					out.add("\tat com.acme.Svc.method" + d + "(Svc.java:" + (10 + d) + ")");
				}
				out.add("Caused by: java.io.IOException: " + WORDS[random.nextInt(WORDS.length)]);
			}
			if (random.nextInt(50) == 0) {
				out.add("");
			}
		}
		return out;
	}
}

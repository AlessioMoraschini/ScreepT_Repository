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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** Searches over a log file, checked against brute-force references on the decoded lines */
public class LogSearcherTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	@BeforeClass
	public static void tinyChunks() {
		LogFixtures.init();
	}

	private List<String> lines;
	private File file;

	private void prepare(String eol, boolean trailing) throws IOException {
		lines = LogFixtures.randomLog(11L, 6000, 4);
		file = LogFixtures.write(tmp.getRoot(), "app.log", LogFixtures.join(eol, trailing, lines));
	}

	private List<Integer> expectedPlain(List<String> required, List<String> excluded, boolean matchCase, boolean wholeWord) {
		List<Integer> out = new ArrayList<Integer>();
		for (int i = 0; i < lines.size(); i++) {
			if (LogFixtures.refPlain(lines.get(i), required, excluded, matchCase, wholeWord)) {
				out.add(i);
			}
		}
		return out;
	}

	private List<Integer> expectedRegex(String regex, boolean matchCase, boolean wholeWord) {
		int flags = matchCase ? 0 : Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
		Pattern p = Pattern.compile(wholeWord ? "(?<!\\w)(?:" + regex + ")(?!\\w)" : regex, flags);
		List<Integer> out = new ArrayList<Integer>();
		for (int i = 0; i < lines.size(); i++) {
			if (p.matcher(lines.get(i)).find()) {
				out.add(i);
			}
		}
		return out;
	}

	private void checkPlain(String text, List<String> required, List<String> excluded) {
		for (boolean matchCase : new boolean[] { false, true }) {
			for (boolean wholeWord : new boolean[] { false, true }) {
				List<Integer> actual = LogFixtures.search(file, new LogQuery(text, false, matchCase, wholeWord));
				assertEquals("[" + text + "] case=" + matchCase + " word=" + wholeWord, expectedPlain(required, excluded, matchCase, wholeWord), actual);
			}
		}
	}

	@Test
	public void plainQueriesMatchTheReferenceOnLfFiles() throws IOException {
		prepare("\n", true);
		checkPlain("error timeout", Arrays.asList("error", "timeout"), Arrays.<String>asList());
		checkPlain("\"connection reset\"", Arrays.asList("connection reset"), Arrays.<String>asList());
		checkPlain("-healthcheck", Arrays.<String>asList(), Arrays.asList("healthcheck"));
		checkPlain("error -healthcheck", Arrays.asList("error"), Arrays.asList("healthcheck"));
		checkPlain("foo", Arrays.asList("foo"), Arrays.<String>asList());
		checkPlain("Foo -\"foo bar\"", Arrays.asList("Foo"), Arrays.asList("foo bar"));
		checkPlain("user=alice \"disk full\"", Arrays.asList("user=alice", "disk full"), Arrays.<String>asList());
		checkPlain("x", Arrays.asList("x"), Arrays.<String>asList());
		checkPlain("2026-11-12", Arrays.asList("2026-11-12"), Arrays.<String>asList());
		checkPlain("nomatchatall", Arrays.asList("nomatchatall"), Arrays.<String>asList());
	}

	@Test
	public void plainQueriesMatchTheReferenceOnCrlfFilesWithoutTrailingNewline() throws IOException {
		prepare("\r\n", false);
		checkPlain("error timeout", Arrays.asList("error", "timeout"), Arrays.<String>asList());
		checkPlain("ok", Arrays.asList("ok"), Arrays.<String>asList());
	}

	@Test
	public void nonAsciiTermsUseTheTextPathAndMatchTheReference() throws IOException {
		prepare("\n", true);
		for (boolean matchCase : new boolean[] { false, true }) {
			List<Integer> actual = LogFixtures.search(file, new LogQuery("café", false, matchCase, false));
			assertEquals(expectedPlain(Arrays.asList("café"), Arrays.<String>asList(), matchCase, false), actual);
			assertFalse(actual.isEmpty());
			actual = LogFixtures.search(file, new LogQuery("perché timeout", false, matchCase, false));
			assertEquals(expectedPlain(Arrays.asList("perché", "timeout"), Arrays.<String>asList(), matchCase, false), actual);
		}
	}

	@Test
	public void asciiTermNextToNonAsciiLetterIsAWholeWord() throws IOException {
		prepare("\n", true);
		// "perch" in "perché": the accented letter is not a word character for the engine (ASCII \w)
		List<Integer> actual = LogFixtures.search(file, new LogQuery("perch", false, false, true));
		assertEquals(expectedPlain(Arrays.asList("perch"), Arrays.<String>asList(), false, true), actual);
		assertFalse(actual.isEmpty());
	}

	@Test
	public void regexQueriesMatchTheReference() throws IOException {
		prepare("\n", true);
		String[] regexes = { "ERR\\d{3}", "conn\\w+ reset", "(foo|bar)", "^2026-11-1[01]", "retry \\d", "timeout$", "\\[main-[0-3]\\]", "Caused by: .*(disk|reset)",
				"user=(alice|bob)", "café", "x", "\\bfoo\\b", "ERROR.*ERROR" };
		for (String regex : regexes) {
			for (boolean matchCase : new boolean[] { false, true }) {
				for (boolean wholeWord : new boolean[] { false, true }) {
					List<Integer> actual = LogFixtures.search(file, new LogQuery(regex, true, matchCase, wholeWord));
					assertEquals(regex + " case=" + matchCase + " word=" + wholeWord, expectedRegex(regex, matchCase, wholeWord), actual);
				}
			}
		}
	}

	@Test
	public void searchStatisticsAndProgressAreConsistent() throws IOException {
		prepare("\n", true);
		LogSearcher searcher = new LogSearcher(file, new LogQuery("error", false, false, false));
		assertEquals(0, searcher.linesScanned());
		searcher.start();
		LogFixtures.waitFinished(searcher);
		assertEquals(lines.size(), searcher.linesScanned());
		assertEquals(1d, searcher.progress(), 0d);
		assertFalse(searcher.isCancelled());
		assertEquals(expectedPlain(Arrays.asList("error"), Arrays.<String>asList(), false, false).size(), searcher.results.size());
	}

	@Test
	public void emptyQueryMatchesEveryLine() throws IOException {
		prepare("\n", true);
		assertEquals(lines.size(), LogFixtures.search(file, new LogQuery("", false, false, false)).size());
	}

	@Test
	public void emptyFileHasNoMatchesAndFinishes() throws IOException {
		File empty = LogFixtures.write(tmp.getRoot(), "empty.log", new byte[0]);
		LogSearcher searcher = new LogSearcher(empty, new LogQuery("a", false, false, false));
		searcher.start();
		LogFixtures.waitFinished(searcher);
		assertEquals(0, searcher.results.size());
		assertEquals(1d, searcher.progress(), 0d);
	}

	@Test
	public void matchInTheLastLineWithoutNewlineIsFound() throws IOException {
		File f = LogFixtures.write(tmp.getRoot(), "last.log", LogFixtures.utf8("alpha\nbeta\nneedle"));
		assertEquals(Arrays.asList(2), LogFixtures.search(f, new LogQuery("needle", false, false, false)));
		assertEquals(Arrays.asList(2), LogFixtures.search(f, new LogQuery("nee.le", true, false, false)));
	}

	@Test
	public void lineLongerThanTheReadPiecesIsMatchedOnce() throws IOException {
		StringBuilder big = new StringBuilder();
		for (int i = 0; i < 150_000; i++) { // far over the chunk size and over the tail buffer
			big.append(i % 10 == 0 ? ' ' : 'a');
		}
		String longLine = big + " NEEDLE " + big;
		File f = LogFixtures.write(tmp.getRoot(), "long.log", LogFixtures.utf8("before\n" + longLine + "\nafter NEEDLE\ntail\n"));
		assertEquals(Arrays.asList(1, 2), LogFixtures.search(f, new LogQuery("needle", false, false, false)));
		assertEquals(Arrays.asList(1, 2), LogFixtures.search(f, new LogQuery("NEED[L]E", true, true, false)));
		assertEquals(Arrays.asList(0), LogFixtures.search(f, new LogQuery("before", false, false, false)));
		assertEquals(Arrays.asList(3), LogFixtures.search(f, new LogQuery("tail", false, false, false)));
	}

	@Test
	public void cancelStopsTheSearchAndKeepsOnlyRealMatches() throws IOException {
		lines = LogFixtures.randomLog(5L, 60000, 6);
		file = LogFixtures.write(tmp.getRoot(), "big.log", LogFixtures.join("\n", true, lines));
		List<Integer> full = LogFixtures.search(file, new LogQuery("error", false, false, false));
		LogSearcher searcher = new LogSearcher(file, new LogQuery("error", false, false, false));
		searcher.start();
		searcher.cancel();
		LogFixtures.waitFinished(searcher);
		assertTrue(searcher.isCancelled());
		List<Integer> partial = LogFixtures.hits(searcher);
		assertTrue(partial.size() <= full.size());
		assertTrue(full.containsAll(partial));
		// matches are handed back in file order and contiguous from the start of the file: a prefix of the full result
		assertEquals(full.subList(0, partial.size()), partial);
	}

	@Test
	public void cancelBeforeStartFindsNothing() throws IOException {
		prepare("\n", true);
		LogSearcher searcher = new LogSearcher(file, new LogQuery("error", false, false, false));
		searcher.cancel();
		searcher.start();
		LogFixtures.waitFinished(searcher);
		assertEquals(0, searcher.results.size());
	}

	@Test
	public void extendScansOnlyWhatTheFileGained() throws IOException {
		prepare("\n", true);
		LogQuery query = new LogQuery("timeout", false, false, false);
		LogFileIndex index = LogFixtures.index(file);
		LogSearcher searcher = new LogSearcher(file, query);
		searcher.start();
		LogFixtures.waitFinished(searcher);

		List<String> more = LogFixtures.randomLog(99L, 700, 1);
		Files.write(file.toPath(), LogFixtures.join("\n", true, more), StandardOpenOption.APPEND);
		assertTrue(index.extend());
		LogFixtures.waitComplete(index);
		searcher.extend(index.resumeLine(), index.resumeOffset());
		LogFixtures.waitFinished(searcher);

		List<String> all = new ArrayList<String>(lines);
		all.addAll(more);
		lines = all;
		assertEquals(expectedPlain(Arrays.asList("timeout"), Arrays.<String>asList(), false, false), LogFixtures.hits(searcher));
		assertEquals(all.size(), searcher.linesScanned());
		assertEquals(all.size(), index.lineCount());
	}

	@Test
	public void searchOfAFileWithBinaryGarbageDoesNotCrash() throws IOException {
		byte[] data = new byte[50_000];
		new java.util.Random(1).nextBytes(data);
		File f = LogFixtures.write(tmp.getRoot(), "garbage.log", data);
		for (LogQuery q : new LogQuery[] { new LogQuery("abc", false, false, false), new LogQuery("a.c", true, false, false), new LogQuery("café", false, false, false) }) {
			LogSearcher searcher = new LogSearcher(f, q);
			searcher.start();
			LogFixtures.waitFinished(searcher);
			assertTrue(searcher.results.size() <= searcher.linesScanned());
		}
	}
}

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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** Indexing of log files: line count, line text, levels, dates, change detection (against a brute-force reference) */
public class LogFileIndexTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	@BeforeClass
	public static void tinyChunks() {
		LogFixtures.init();
	}

	private LogFileIndex index(byte[] data) throws IOException {
		return LogFixtures.index(LogFixtures.write(tmp.getRoot(), "f" + System.nanoTime() + ".log", data));
	}

	private void assertSameLines(List<String> expected, LogFileIndex index) {
		assertEquals(expected.size(), index.lineCount());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals("line " + i, expected.get(i), index.line(i));
		}
	}

	@Test
	public void emptyFileHasNoLines() throws IOException {
		LogFileIndex index = index(new byte[0]);
		assertEquals(0, index.lineCount());
		assertTrue(index.isComplete());
		assertEquals(1d, index.progress(), 0d);
		assertEquals("", index.line(0));
		assertFalse(index.hasLevels());
		assertFalse(index.hasDates());
	}

	@Test
	public void newlinesOnlyAreEmptyLines() throws IOException {
		assertEquals(1, index(LogFixtures.utf8("\n")).lineCount());
		LogFileIndex three = index(LogFixtures.utf8("\n\n\n"));
		assertEquals(3, three.lineCount());
		assertEquals("", three.line(2));
	}

	@Test
	public void lastLineWithoutNewlineCounts() throws IOException {
		LogFileIndex index = index(LogFixtures.utf8("a\nb\nc"));
		assertEquals(3, index.lineCount());
		assertEquals("c", index.line(2));
		assertEquals("c", index.lineFull(2));
	}

	@Test
	public void trailingNewlineDoesNotAddALine() throws IOException {
		LogFileIndex index = index(LogFixtures.utf8("a\nb\nc\n"));
		assertEquals(3, index.lineCount());
		assertEquals("c", index.line(2));
		assertEquals("", index.line(3));
	}

	@Test
	public void singleLineNoNewline() throws IOException {
		LogFileIndex index = index(LogFixtures.utf8("only"));
		assertEquals(1, index.lineCount());
		assertEquals("only", index.line(0));
	}

	@Test
	public void crlfAndLfAndMixedGiveTheSameLines() throws IOException {
		List<String> lines = Arrays.asList("alpha", "", "beta gamma", "x", "last");
		for (String eol : new String[] { "\n", "\r\n" }) {
			for (boolean trailing : new boolean[] { true, false }) {
				assertSameLines(lines, index(LogFixtures.join(eol, trailing, lines)));
			}
		}
		LogFileIndex mixed = index(LogFixtures.utf8("alpha\r\n\nbeta gamma\r\nx\nlast\r\n"));
		assertSameLines(lines, mixed);
		for (int i = 0; i < lines.size(); i++) {
			assertEquals(lines.get(i), mixed.lineFull(i));
		}
	}

	@Test
	public void loneCarriageReturnInsideALineIsKept() throws IOException {
		LogFileIndex index = index(LogFixtures.utf8("a\rb\nc\n"));
		assertEquals(2, index.lineCount());
		assertEquals("a\rb", index.line(0));
	}

	@Test
	public void utf8BomDoesNotHideTheLevelOfTheFirstLine() throws IOException {
		byte[] body = LogFixtures.utf8("ERROR boom\nINFO ok\n");
		byte[] data = new byte[body.length + 3];
		data[0] = (byte) 0xEF;
		data[1] = (byte) 0xBB;
		data[2] = (byte) 0xBF;
		System.arraycopy(body, 0, data, 3, body.length);
		LogFileIndex index = index(data);
		assertEquals(2, index.lineCount());
		assertTrue(index.line(0).endsWith("ERROR boom"));
		assertEquals(LogLevel.ERROR, index.level(0));
		assertEquals(LogLevel.INFO, index.level(1));
	}

	@Test
	public void unicodeLinesComeBackIdenticalAcrossChunkBoundaries() throws IOException {
		List<String> lines = new ArrayList<String>();
		for (int i = 0; i < 600; i++) {
			lines.add("riga " + i + " perché € 日本語 😀 end");
		}
		LogFileIndex index = index(LogFixtures.join("\n", true, lines));
		assertSameLines(lines, index);
	}

	@Test
	public void veryLongLineIsShownCutButExportedWhole() throws IOException {
		StringBuilder longLine = new StringBuilder();
		for (int i = 0; i < 200_000; i++) {
			longLine.append((char) ('a' + i % 26));
		}
		List<String> lines = Arrays.asList("first", longLine.toString(), "last");
		LogFileIndex index = index(LogFixtures.join("\n", true, lines));
		assertEquals(3, index.lineCount());
		assertEquals("first", index.line(0));
		String shown = index.line(1);
		assertEquals(LogFileIndex.MAX_DISPLAY_BYTES + 4, shown.length());
		assertTrue(shown.endsWith(" ..."));
		assertTrue(longLine.toString().startsWith(shown.substring(0, LogFileIndex.MAX_DISPLAY_BYTES)));
		assertEquals(longLine.toString(), index.lineFull(1));
		assertEquals("last", index.line(2));
		assertEquals("last", index.lineFull(2));
	}

	@Test
	public void manyLinesAreAllReadable() throws IOException {
		// more than 65536 lines: the level bitmap and the block offsets must grow
		List<String> lines = new ArrayList<String>();
		for (int i = 0; i < 70_000; i++) {
			lines.add(i % 7 == 0 ? "ERROR e" + i : "i" + i);
		}
		LogFileIndex index = index(LogFixtures.join("\n", true, lines));
		assertSameLines(lines, index);
		assertEquals(10_000, index.levelCount(LogLevel.ERROR)); // lines 0, 7, ... 69993
		assertEquals(LogLevel.ERROR, index.level(69_999 - 69_999 % 7));
	}

	@Test
	public void outOfRangeAccessIsSafe() throws IOException {
		LogFileIndex index = index(LogFixtures.utf8("a\nb\n"));
		assertEquals("", index.line(-1));
		assertEquals("", index.line(2));
		assertEquals("", index.lineFull(-1));
		assertEquals("", index.lineFull(99));
		assertEquals(LogLevel.NONE, LogLevel.level(index.levelByte(-5)));
		assertEquals(LogLevel.NONE, LogLevel.level(index.levelByte(99)));
	}

	@Test
	public void stackTraceLinesInheritTheLevelOfTheirEntry() throws IOException {
		List<String> lines = Arrays.asList(
				"2026-11-16 10:00:00 INFO started",
				"2026-11-16 10:00:01 ERROR boom",
				"java.lang.IllegalStateException: boom",
				"\tat com.acme.Foo.bar(Foo.java:1)",
				"Caused by: java.io.IOException: disk",
				"\t... 5 more",
				"2026-11-16 10:00:02 WARN slow",
				"plain text with no level");
		LogFileIndex index = index(LogFixtures.join("\n", true, lines));
		assertTrue(index.hasLevels());
		int[] expected = { LogLevel.INFO, LogLevel.ERROR, LogLevel.ERROR, LogLevel.ERROR, LogLevel.ERROR, LogLevel.ERROR, LogLevel.WARN, LogLevel.NONE };
		for (int i = 0; i < expected.length; i++) {
			assertEquals("level of line " + i, expected[i], index.level(i));
		}
		assertFalse(LogLevel.isContinuation(index.levelByte(1)));
		for (int i = 2; i <= 5; i++) {
			assertTrue("continuation " + i, LogLevel.isContinuation(index.levelByte(i)));
		}
		// inherited lines are not counted as entries
		assertEquals(1, index.levelCount(LogLevel.ERROR));
		assertEquals(1, index.levelCount(LogLevel.INFO));
		assertEquals(1, index.levelCount(LogLevel.WARN));
		assertEquals(0, index.levelCount(LogLevel.DEBUG));
	}

	@Test
	public void leadingStackTraceWithoutEntryHasNoLevel() throws IOException {
		LogFileIndex index = index(LogFixtures.utf8("\tat a.B.c(B.java:1)\nINFO x\n"));
		assertEquals(LogLevel.NONE, index.level(0));
		assertEquals(LogLevel.INFO, index.level(1));
	}

	@Test
	public void levelsOfARandomLogMatchLineByLineDetection() throws IOException {
		List<String> lines = LogFixtures.randomLog(1L, 6000, 5);
		LogFileIndex index = index(LogFixtures.join("\n", true, lines));
		assertSameLines(lines, index);
		int[] counts = new int[LogLevel.COUNT];
		int previous = LogLevel.NONE;
		for (int i = 0; i < lines.size(); i++) {
			byte[] bytes = lines.get(i).getBytes(java.nio.charset.StandardCharsets.UTF_8);
			byte[] head = Arrays.copyOf(bytes, Math.min(bytes.length, 160));
			int level = LogLevel.detect(head, head.length);
			int expected;
			if (level >= 0) {
				expected = level;
				counts[level]++;
			} else if (LogLevel.looksLikeContinuation(head, head.length)) {
				expected = previous;
			} else {
				expected = LogLevel.NONE;
			}
			previous = expected;
			assertEquals("level of line " + i + ": " + lines.get(i), expected, index.level(i));
		}
		for (int l = 0; l < LogLevel.COUNT; l++) {
			assertEquals(counts[l], index.levelCount(l));
		}
	}

	// ---------------------------------------------------------------- dates

	/** 10 days (12th-13th missing... see below), 100 entries a day, long stack traces between some of them */
	private List<String> datedLog(List<Integer> firstLineOfDay) {
		List<String> lines = new ArrayList<String>();
		for (int day = 10; day <= 19; day++) {
			if (day == 13 || day == 14) {
				continue; // a gap of two days
			}
			firstLineOfDay.add(lines.size());
			for (int i = 0; i < 100; i++) {
				lines.add(String.format("2026-11-%02d %02d:%02d:00 INFO entry %d", day, i / 5, (i % 5) * 10, i));
				if (i == 99) {
					for (int t = 0; t < 150; t++) { // a trace longer than two blocks at the end of the day
						lines.add("\tat com.acme.Deep.m" + t + "(Deep.java:" + t + ")");
					}
				}
			}
		}
		return lines;
	}

	private int expectedStart(List<String> lines, int fromDay) {
		for (int i = 0; i < lines.size(); i++) {
			int day = dayOf(lines.get(i));
			if (day != 0 && day >= fromDay) {
				return i;
			}
		}
		return lines.size();
	}

	private int expectedEnd(List<String> lines, int toDay) {
		for (int i = 0; i < lines.size(); i++) {
			int day = dayOf(lines.get(i));
			if (day != 0 && day > toDay) {
				return i;
			}
		}
		return lines.size();
	}

	private int dayOf(String line) {
		if (!line.startsWith("2026-")) {
			return 0;
		}
		return LogDates.fromDate(LocalDate.of(2026, Integer.parseInt(line.substring(5, 7)), Integer.parseInt(line.substring(8, 10))));
	}

	@Test
	public void dateRangesMatchABruteForceScan() throws IOException {
		List<Integer> firsts = new ArrayList<Integer>();
		List<String> lines = datedLog(firsts);
		LogFileIndex index = index(LogFixtures.join("\r\n", true, lines));
		assertTrue(index.hasDates());
		assertEquals(LogDates.ISO, index.dateFormat());
		assertEquals(LogDates.fromDate(LocalDate.of(2026, 11, 10)), index.firstDay());
		assertEquals(LogDates.fromDate(LocalDate.of(2026, 11, 19)), index.lastDay());
		assertEquals(LogDates.fromDate(LocalDate.of(2026, 11, 12)), index.dayOf(expectedStart(lines, LogDates.fromDate(LocalDate.of(2026, 11, 12)))));

		for (int from = 8; from <= 21; from++) {
			for (int to = 8; to <= 21; to++) {
				int fromDay = LogDates.fromDate(LocalDate.of(2026, 11, from));
				int toDay = LogDates.fromDate(LocalDate.of(2026, 11, to));
				int[] range = index.lineRange(fromDay, toDay);
				int start = expectedStart(lines, fromDay);
				int end = Math.max(start, expectedEnd(lines, toDay));
				assertEquals("start of " + from + ".." + to, start, range[0]);
				assertEquals("end of " + from + ".." + to, end, range[1]);
			}
		}
	}

	@Test
	public void unboundedDateRangeIsTheWholeFile() throws IOException {
		List<String> lines = datedLog(new ArrayList<Integer>());
		LogFileIndex index = index(LogFixtures.join("\n", true, lines));
		assertEquals(Arrays.asList(0, lines.size()), Arrays.asList(index.lineRange(0, 0)[0], index.lineRange(0, 0)[1]));
		int day = LogDates.fromDate(LocalDate.of(2026, 11, 15));
		int[] open = index.lineRange(day, 0);
		assertEquals(expectedStart(lines, day), open[0]);
		assertEquals(lines.size(), open[1]);
		int[] upTo = index.lineRange(0, day);
		assertEquals(0, upTo[0]);
		assertEquals(expectedEnd(lines, day), upTo[1]);
	}

	@Test
	public void fileWithoutDatesHasNoDateFilter() throws IOException {
		LogFileIndex index = index(LogFixtures.utf8("alpha\nbeta\ngamma\ndelta\nepsilon\n"));
		assertFalse(index.hasDates());
		assertEquals(0, index.dayOf(0));
		assertEquals(0, index.firstDay());
		assertEquals(0, index.lastDay());
	}

	@Test
	public void timeBoundaryFindsTheFirstLineFromATime() throws IOException {
		List<String> lines = new ArrayList<String>();
		for (int i = 0; i < 300; i++) {
			// one entry every 10 s from 00:00:00; every 4th line is a stack trace line without time
			lines.add(i % 4 == 3 && i < 299 ? "\tat trace" : String.format("2026-11-16 %02d:%02d:%02d INFO e%d", i * 10 / 3600, i * 10 / 60 % 60, i * 10 % 60, i));
		}
		LogFileIndex index = index(LogFixtures.join("\n", true, lines));
		for (int second = 0; second <= 3000; second += 7) {
			int ms = second * 1000;
			int atLeast = index.timeBoundary(0, lines.size(), ms, false);
			int after = index.timeBoundary(0, lines.size(), ms, true);
			int expectedAtLeast = lines.size();
			int expectedAfter = lines.size();
			// a line without time takes the time of the next one that has it
			for (int i = lines.size() - 1; i >= 0; i--) {
				int time = timeWithFallback(lines, i);
				if (time >= ms) {
					expectedAtLeast = i;
				}
				if (time > ms) {
					expectedAfter = i;
				}
			}
			assertEquals("at least " + second, expectedAtLeast, atLeast);
			assertEquals("after " + second, expectedAfter, after);
		}
	}

	private int timeWithFallback(List<String> lines, int i) {
		for (int p = i; p < lines.size(); p++) {
			int t = LogDates.timeOfDay(lines.get(p));
			if (t >= 0) {
				return t;
			}
		}
		return Integer.MAX_VALUE;
	}

	// ---------------------------------------------------------------- changes on disk

	@Test
	public void inspectSeesAppendRewriteAndRemoval() throws IOException {
		File file = LogFixtures.write(tmp.getRoot(), "grow.log", LogFixtures.utf8("2026-11-16 10:00:00 INFO a\n2026-11-16 10:00:01 INFO b\n"));
		LogFileIndex index = LogFixtures.index(file);
		assertEquals(LogFileIndex.Change.UNCHANGED, index.inspect());

		Files.write(file.toPath(), LogFixtures.utf8("2026-11-16 10:00:02 ERROR c\n"), StandardOpenOption.APPEND);
		assertEquals(LogFileIndex.Change.APPENDED, index.inspect());
		assertTrue(index.extend());
		LogFixtures.waitComplete(index);
		assertEquals(3, index.lineCount());
		assertEquals("2026-11-16 10:00:02 ERROR c", index.line(2));
		assertEquals("2026-11-16 10:00:01 INFO b", index.line(1));
		assertEquals(1, index.levelCount(LogLevel.ERROR));
		assertEquals(2, index.levelCount(LogLevel.INFO));
		assertEquals(LogFileIndex.Change.UNCHANGED, index.inspect());

		// shorter: replaced by a new log after a rotation
		Files.write(file.toPath(), LogFixtures.utf8("new\n"));
		assertEquals(LogFileIndex.Change.REWRITTEN, index.inspect());

		assertTrue(file.delete());
		assertEquals(LogFileIndex.Change.MISSING, index.inspect());
	}

	@Test
	public void sameSizeEditIsARewrite() throws IOException {
		File file = LogFixtures.write(tmp.getRoot(), "edit.log", LogFixtures.utf8("aaaa\nbbbb\n"));
		LogFileIndex index = LogFixtures.index(file);
		Files.write(file.toPath(), LogFixtures.utf8("aaaa\nbbbX\n"));
		assertTrue(file.setLastModified(file.lastModified() + 5000));
		assertEquals(LogFileIndex.Change.REWRITTEN, index.inspect());
	}

	@Test
	public void extendKeepsTheOldLinesAndRecountsTheLastBlock() throws IOException {
		List<String> lines = new ArrayList<String>();
		for (int i = 0; i < 200; i++) {
			lines.add(i % 10 == 0 ? "ERROR e" + i : "line " + i);
		}
		File file = LogFixtures.write(tmp.getRoot(), "ext.log", LogFixtures.join("\n", true, lines));
		LogFileIndex index = LogFixtures.index(file);
		int errorsBefore = index.levelCount(LogLevel.ERROR);
		assertEquals(20, errorsBefore);
		List<String> more = new ArrayList<String>();
		for (int i = 200; i < 300; i++) {
			more.add(i % 10 == 0 ? "ERROR e" + i : "line " + i);
		}
		Files.write(file.toPath(), LogFixtures.join("\n", true, more), StandardOpenOption.APPEND);
		assertTrue(index.extend());
		LogFixtures.waitComplete(index);
		List<String> all = new ArrayList<String>(lines);
		all.addAll(more);
		assertSameLines(all, index);
		assertEquals(30, index.levelCount(LogLevel.ERROR));
		assertTrue(index.resumeLine() <= 200);
		assertEquals(0, index.resumeLine() % LogFileIndex.BLOCK);
	}

	@Test
	public void extendIsRefusedWhileNotComplete() throws IOException {
		File file = LogFixtures.write(tmp.getRoot(), "x.log", LogFixtures.utf8("a\n"));
		LogFileIndex index = new LogFileIndex(file); // not started: not complete
		assertFalse(index.extend());
		index.close();
		LogFileIndex done = LogFixtures.index(file);
		done.close();
		assertFalse(done.extend());
	}

	@Test(expected = FileNotFoundException.class)
	public void missingFileIsReportedByTheConstructor() throws IOException {
		new LogFileIndex(new File(tmp.getRoot(), "nope.log"));
	}

	@Test
	public void closeStopsAnIndexingInProgress() throws IOException {
		List<String> lines = LogFixtures.randomLog(3L, 20000, 3);
		File file = LogFixtures.write(tmp.getRoot(), "big.log", LogFixtures.join("\n", true, lines));
		LogFileIndex index = new LogFileIndex(file);
		index.startIndexing();
		index.close();
		LogFixtures.waitComplete(index); // finishes (stopped) without errors
		assertTrue(index.lineCount() <= lines.size());
		assertTrue(index.progress() <= 1d);
	}
}

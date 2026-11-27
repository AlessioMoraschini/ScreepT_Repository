/**
 *
 * =========================================================================================
 *  Copyright (C) 2019-2020
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;

import org.junit.Test;

/** Level words and continuation lines of the supported log formats */
public class LogLevelTest {

	private static int detect(String line) {
		byte[] bytes = line.getBytes(StandardCharsets.UTF_8);
		return LogLevel.detect(bytes, bytes.length);
	}

	private static boolean continuation(String line) {
		byte[] bytes = line.getBytes(StandardCharsets.UTF_8);
		return LogLevel.looksLikeContinuation(bytes, bytes.length);
	}

	@Test
	public void standardWordsAnyCase() {
		assertEquals(LogLevel.ERROR, detect("2026-11-16 12:00:00 ERROR something broke"));
		assertEquals(LogLevel.ERROR, detect("12:00 error: x"));
		assertEquals(LogLevel.ERROR, detect("FATAL out of memory"));
		assertEquals(LogLevel.ERROR, detect("SEVERE: boom"));
		assertEquals(LogLevel.ERROR, detect("CRITICAL disk"));
		assertEquals(LogLevel.WARN, detect("WARN slow"));
		assertEquals(LogLevel.WARN, detect("Warning: low space"));
		assertEquals(LogLevel.INFO, detect("[INFO] started"));
		assertEquals(LogLevel.INFO, detect("info about stuff"));
		assertEquals(LogLevel.DEBUG, detect("level=debug msg"));
		assertEquals(LogLevel.TRACE, detect("TRACE enter()"));
	}

	@Test
	public void wordsInsideOtherWordsAreNotLevels() {
		assertEquals(-1, detect("these are errors in a list"));
		assertEquals(-1, detect("terror happened"));
		assertEquals(-1, detect("xinfo and myerror"));
		assertEquals(-1, detect("Information about nothing"));
		assertEquals(-1, detect("no level here at all"));
		assertEquals(-1, detect(""));
	}

	@Test
	public void firstLevelWordWins() {
		assertEquals(LogLevel.WARN, detect("2026 WARN retry after ERROR"));
		assertEquals(LogLevel.ERROR, detect("ERROR while logging INFO"));
	}

	@Test
	public void otherLogSystems() {
		assertEquals(LogLevel.ERROR, detect("fail: Microsoft.Hosting.Lifetime[0]"));
		assertEquals(LogLevel.ERROR, detect("crit: x.y"));
		assertEquals(LogLevel.DEBUG, detect("dbug: x.y"));
		assertEquals(LogLevel.TRACE, detect("trce: x.y"));
		assertEquals(LogLevel.INFO, detect("I0102 12:00:00.123 main.go:12] hi"));
		assertEquals(LogLevel.WARN, detect("W0102 12:00:00.123 main.go:12] hi"));
		assertEquals(LogLevel.ERROR, detect("E0102 12:00:00.123 main.go:12] hi"));
		assertEquals(LogLevel.ERROR, detect("F0102 12:00:00.123 main.go:12] hi"));
		assertEquals(LogLevel.ERROR, detect("E/ActivityManager( 123): crash"));
		assertEquals(LogLevel.TRACE, detect("V/Tag(1): x"));
		assertEquals(LogLevel.DEBUG, detect("D/Tag(1): x"));
		assertEquals(LogLevel.INFO, detect("I/Tag(1): x"));
		assertEquals(LogLevel.WARN, detect("W/Tag(1): x"));
		assertEquals(LogLevel.WARN, detect("2026-01-01 [WRN] slow"));
		assertEquals(LogLevel.ERROR, detect("[ERR] x"));
		assertEquals(LogLevel.ERROR, detect("[FTL] x"));
		assertEquals(LogLevel.INFO, detect("[INF] x"));
		assertEquals(LogLevel.DEBUG, detect("[DBG] x"));
		assertEquals(LogLevel.TRACE, detect("[VRB] x"));
		assertEquals(LogLevel.TRACE, detect("[TRC] x"));
	}

	@Test
	public void julAndSyslogWordsNeedCapitals() {
		assertEquals(LogLevel.DEBUG, detect("FINE message"));
		assertEquals(LogLevel.TRACE, detect("FINER message"));
		assertEquals(LogLevel.TRACE, detect("FINEST message"));
		assertEquals(LogLevel.INFO, detect("NOTICE message"));
		assertEquals(LogLevel.ERROR, detect("ALERT message"));
		assertEquals(LogLevel.ERROR, detect("EMERG message"));
		assertEquals(LogLevel.ERROR, detect("PANIC message"));
		assertEquals(LogLevel.TRACE, detect("VERBOSE message"));
		// ordinary words
		assertEquals(-1, detect("Fine thanks, notice me"));
		assertEquals(-1, detect("a verbose panic"));
	}

	@Test
	public void indentedLinesHaveNoOtherFormatLevel() {
		assertEquals(-1, detect("  FINE message"));
		assertEquals(-1, detect("\tE/Tag( 1): x"));
	}

	@Test
	public void continuationLines() {
		assertTrue(continuation("\tat com.acme.Foo.bar(Foo.java:1)"));
		assertTrue(continuation("    wrapped text"));
		assertTrue(continuation("at com.acme.Foo.bar(Foo.java:1)"));
		assertTrue(continuation("Caused by: java.io.IOException: x"));
		assertTrue(continuation("... 12 more"));
		assertTrue(continuation("Suppressed: java.lang.Foo"));
		assertTrue(continuation("java.lang.IllegalStateException: boom"));
		assertTrue(continuation("com.acme.Outer$InnerError"));
		assertTrue(continuation("java.lang.Throwable"));
		assertFalse(continuation("Exception happened"));
		assertFalse(continuation("org.example.Thing"));
		assertFalse(continuation("atlas is a map"));
		assertFalse(continuation("hello"));
		assertFalse(continuation(""));
	}

	@Test
	public void storedByteHelpers() {
		byte stored = (byte) (LogLevel.WARN | LogLevel.CONTINUATION);
		assertEquals(LogLevel.WARN, LogLevel.level(stored));
		assertTrue(LogLevel.isContinuation(stored));
		assertFalse(LogLevel.isContinuation((byte) LogLevel.WARN));
		assertEquals(LogLevel.COUNT, LogLevel.NAMES.length);
		assertEquals(LogLevel.COUNT, LogLevel.COLORS.length);
		assertEquals("ERROR", LogLevel.NAMES[LogLevel.ERROR]);
	}
}

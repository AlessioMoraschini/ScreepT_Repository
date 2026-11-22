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

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import org.junit.Test;

/** Day numbers, formats, detection and time of day of the log dates */
public class LogDatesTest {

	private static int day(int y, int m, int d) {
		return LogDates.fromDate(LocalDate.of(y, m, d));
	}

	private static int parse(int format, String line) {
		return LogDates.parse(format, line, 2026);
	}

	private static int detect(String... lines) {
		StringBuilder sb = new StringBuilder();
		for (String line : lines) {
			sb.append(line).append('\n');
		}
		byte[] data = sb.toString().getBytes(StandardCharsets.UTF_8);
		return LogDates.detect(data, data.length, 2026);
	}

	@Test
	public void daysEqualsJavaTimeForEveryPlausibleDate() {
		for (LocalDate date = LocalDate.of(1971, 1, 1); date.getYear() <= 2200; date = date.plusDays(1)) {
			assertEquals(date.toString(), (int) date.toEpochDay(), LogDates.days(date.getYear(), date.getMonthValue(), date.getDayOfMonth()));
		}
	}

	@Test
	public void daysRejectsImplausibleDates() {
		assertEquals(0, LogDates.days(1970, 1, 1));
		assertEquals(0, LogDates.days(2201, 1, 1));
		assertEquals(0, LogDates.days(2026, 0, 1));
		assertEquals(0, LogDates.days(2026, 13, 1));
		assertEquals(0, LogDates.days(2026, 1, 0));
		assertEquals(0, LogDates.days(2026, 1, 32));
		assertEquals(365, LogDates.days(1971, 1, 1));
	}

	@Test
	public void toDateAndFromDateAreInverse() {
		LocalDate date = LocalDate.of(2026, 11, 16);
		assertEquals(date, LogDates.toDate(LogDates.fromDate(date)));
	}

	@Test
	public void isoDates() {
		int expected = day(2026, 11, 16);
		assertEquals(expected, parse(LogDates.ISO, "2026-11-16 12:00:00 INFO x"));
		assertEquals(expected, parse(LogDates.ISO, "2026/11/16 12:00:00"));
		assertEquals(expected, parse(LogDates.ISO, "2026-11-16T12:00:00Z msg"));
		assertEquals(expected, parse(LogDates.ISO, "2026-11-16"));
		assertEquals(expected, parse(LogDates.ISO, "{\"level\":\"info\",\"ts\":\"2026-11-16T12:00:00.123Z\",\"msg\":\"x\"}"));
		assertEquals(0, parse(LogDates.ISO, "12026-11-16 x"));
		assertEquals(0, parse(LogDates.ISO, "2026-11-161 x"));
		assertEquals(0, parse(LogDates.ISO, "2026-13-01 x"));
		assertEquals(0, parse(LogDates.ISO, "1970-01-01 x"));
		assertEquals(0, parse(LogDates.ISO, "2026-11/16 x"));
		assertEquals(0, parse(LogDates.ISO, "no date"));
		assertEquals(0, parse(LogDates.ISO, ""));
		assertEquals(0, parse(LogDates.ISO, "  2026-11-16 indented stack trace"));
		assertEquals(0, parse(LogDates.ISO, "\t2026-11-16 indented stack trace"));
	}

	@Test
	public void dayFirstAndMonthFirst() {
		assertEquals(day(2026, 11, 16), parse(LogDates.DAY_FIRST, "16/11/2026 12:00"));
		assertEquals(day(2026, 11, 16), parse(LogDates.DAY_FIRST, "16-11-2026 12:00"));
		assertEquals(day(2026, 11, 16), parse(LogDates.DAY_FIRST, "16.11.2026 12:00"));
		assertEquals(0, parse(LogDates.DAY_FIRST, "11/16/2026"));
		assertEquals(day(2026, 11, 16), parse(LogDates.MONTH_FIRST, "11/16/2026 12:00"));
		assertEquals(0, parse(LogDates.MONTH_FIRST, "16/11/2026"));
		assertEquals(day(2026, 6, 5), parse(LogDates.DAY_FIRST, "05/06/2026"));
		assertEquals(day(2026, 5, 6), parse(LogDates.MONTH_FIRST, "05/06/2026"));
		assertEquals(0, parse(LogDates.DAY_FIRST, "16/11-2026"));
	}

	@Test
	public void accessLogDates() {
		assertEquals(day(2026, 10, 10), parse(LogDates.ACCESS_LOG, "127.0.0.1 - - [10/Oct/2026:13:55:36 +0000] \"GET / HTTP/1.1\" 200"));
		assertEquals(day(2026, 2, 3), parse(LogDates.ACCESS_LOG, "[03/feb/2026:00:00:00 +0100]"));
		assertEquals(0, parse(LogDates.ACCESS_LOG, "[10/Xyz/2026:13:55:36 +0000]"));
	}

	@Test
	public void syslogDatesUseTheDefaultYear() {
		assertEquals(day(2026, 11, 16), parse(LogDates.SYSLOG, "Nov 16 12:00:00 host sshd[1]: x"));
		assertEquals(day(2026, 11, 6), parse(LogDates.SYSLOG, "Nov  6 12:00:00 host sshd[1]: x"));
		assertEquals(day(2024, 11, 6), LogDates.parse(LogDates.SYSLOG, "nov 6 12:00:00 host", 2024));
		assertEquals(0, parse(LogDates.SYSLOG, "Nov 16 host"));
		assertEquals(0, parse(LogDates.SYSLOG, "Xyz 16 12:00:00 host"));
		assertEquals(0, parse(LogDates.SYSLOG, "Nov"));
	}

	@Test
	public void javaUtilLoggingDates() {
		assertEquals(day(2026, 11, 16), parse(LogDates.JUL, "Nov 16, 2026 12:00:00 PM x"));
		assertEquals(day(2026, 11, 16), parse(LogDates.JUL, "November 16, 2026 12:00:00 PM x"));
		assertEquals(0, parse(LogDates.JUL, "Nov 16 2026 12:00:00"));
		assertEquals(0, parse(LogDates.JUL, "Xyz 16, 2026 12:00:00 PM"));
	}

	/** A short line that starts like a month ("No", "Ma", "Ju"...) is a normal line of a stack trace or a wrapped text: must not throw */
	@Test
	public void shortLinesStartingLikeAMonthAreNotDates() {
		for (String line : new String[] { "No", "Ma", "Ju", "Se", "Oc", "De", "Fe", "Ja", "Ap", "Au", "J", "N" }) {
			for (int format = LogDates.ISO; format <= LogDates.UNIX_TIME; format++) {
				assertEquals(line + " format " + format, 0, parse(format, line));
			}
		}
	}

	@Test
	public void unixTime() {
		// 2025-11-16 06:46:40 UTC
		assertEquals(1763275600 / 86400, parse(LogDates.UNIX_TIME, "1763275600 started"));
		assertEquals(1763275600 / 86400, parse(LogDates.UNIX_TIME, "1763275600123 started"));
		assertEquals(0, parse(LogDates.UNIX_TIME, "12345 started"));
		assertEquals(0, parse(LogDates.UNIX_TIME, "17632756001 started"));
		assertEquals(0, parse(LogDates.UNIX_TIME, "0000000001 started"));
		assertEquals(0, parse(LogDates.UNIX_TIME, "17632756001234 started"));
		assertEquals(0, parse(LogDates.UNIX_TIME, "abc"));
	}

	@Test
	public void unknownFormatGivesNothing() {
		assertEquals(0, parse(LogDates.NONE, "2026-11-16 12:00:00"));
		assertEquals(0, parse(99, "2026-11-16 12:00:00"));
	}

	@Test
	public void detectRecognizesEachFormat() {
		String[] iso = new String[10];
		String[] dayFirst = new String[10];
		String[] monthFirst = new String[10];
		String[] access = new String[10];
		String[] syslog = new String[10];
		String[] jul = new String[10];
		String[] unix = new String[10];
		for (int i = 0; i < 10; i++) {
			iso[i] = "2026-11-" + (10 + i) + " 12:00:00 INFO x";
			dayFirst[i] = (13 + i) + "/11/2026 12:00:00 INFO x";
			monthFirst[i] = "11/" + (13 + i) + "/2026 12:00:00 INFO x";
			access[i] = "10.0.0.1 - - [" + (10 + i) + "/Oct/2026:13:55:36 +0000] \"GET /\" 200";
			syslog[i] = "Nov " + (10 + i) + " 12:00:00 host app: x";
			jul[i] = "Nov " + (10 + i) + ", 2026 12:00:00 PM x";
			unix[i] = "176327560" + i + " msg";
		}
		assertEquals(LogDates.ISO, detect(iso));
		assertEquals(LogDates.DAY_FIRST, detect(dayFirst));
		assertEquals(LogDates.MONTH_FIRST, detect(monthFirst));
		assertEquals(LogDates.ACCESS_LOG, detect(access));
		assertEquals(LogDates.SYSLOG, detect(syslog));
		assertEquals(LogDates.JUL, detect(jul));
		assertEquals(LogDates.UNIX_TIME, detect(unix));
	}

	@Test
	public void ambiguousDatesAreDayFirst() {
		String[] lines = new String[10];
		for (int i = 0; i < lines.length; i++) {
			lines[i] = "0" + (1 + i % 9) + "/02/2026 12:00:00 x";
		}
		assertEquals(LogDates.DAY_FIRST, detect(lines));
	}

	@Test
	public void detectNeedsEnoughDatedLines() {
		assertEquals(LogDates.NONE, detect("2026-11-16 a", "2026-11-16 b"));
		assertEquals(LogDates.NONE, detect("alpha", "beta", "gamma", "delta", "epsilon"));
		assertEquals(LogDates.NONE, LogDates.detect(new byte[0], 0, 2026));
		// 3 dated lines out of 20 candidates: under a third of them
		String[] lines = new String[20];
		for (int i = 0; i < lines.length; i++) {
			lines[i] = i < 3 ? "2026-11-16 x" : "no date here " + i;
		}
		assertEquals(LogDates.NONE, detect(lines));
	}

	@Test
	public void indentedLinesDoNotCountAsCandidates() {
		String[] lines = new String[16];
		for (int i = 0; i < lines.length; i++) {
			lines[i] = i % 2 == 0 ? "2026-11-16 12:00:00 ERROR x" : "\tat com.acme.Foo.bar(Foo.java:" + i + ")";
		}
		assertEquals(LogDates.ISO, detect(lines));
	}

	@Test
	public void timeOfDayParsing() {
		assertEquals(((12 * 60 + 34) * 60 + 56) * 1000 + 789, LogDates.timeOfDay("2026-11-16 12:34:56.789 INFO"));
		assertEquals(((12 * 60 + 34) * 60 + 56) * 1000 + 789, LogDates.timeOfDay("2026-11-16 12:34:56.789123 INFO"));
		assertEquals(500, LogDates.timeOfDay("00:00:00,5"));
		assertEquals(0, LogDates.timeOfDay("00:00:00"));
		assertEquals(((23 * 60 + 59) * 60 + 59) * 1000, LogDates.timeOfDay("x 23:59:59"));
		assertEquals(((23 * 60 + 59) * 60 + 60) * 1000, LogDates.timeOfDay("23:59:60"));
		assertEquals(-1, LogDates.timeOfDay("24:00:00"));
		assertEquals(-1, LogDates.timeOfDay("12:60:00"));
		assertEquals(-1, LogDates.timeOfDay("12:34:5"));
		assertEquals(-1, LogDates.timeOfDay("112:34:56"));
		assertEquals(-1, LogDates.timeOfDay("no time at all"));
		assertEquals(-1, LogDates.timeOfDay(""));
		// the first valid time wins after an invalid one
		assertEquals(((10 * 60 + 5) * 60 + 7) * 1000, LogDates.timeOfDay("99:99:99 then 10:05:07"));
		// only the first 160 characters are looked at
		StringBuilder far = new StringBuilder();
		for (int i = 0; i < 170; i++) {
			far.append('.');
		}
		assertEquals(-1, LogDates.timeOfDay(far + "12:00:00"));
	}
}

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

import java.nio.charset.StandardCharsets;

/**
 * The date of a log line, as a day number (days since 1970-01-01), read straight from the bytes of the line without creating any
 * object. The format of a file is recognized once on its first lines; after that only one line in 32 is read while indexing
 * (the first line of every block of lines gets the date of the nearest sampled line), which costs almost nothing. Formats: ISO
 * (2026-11-16 12:00:00, 2026-11-16T12:00:00Z, also inside a JSON line), day first or month first (16/11/2026, 16-11-2026,
 * 16.11.2026), Apache / nginx access logs (10/Oct/2026:13:55:36), syslog (Nov 16 12:00:00, the year is the one of the file),
 * java.util.logging (Nov 16, 2026 12:00:00 PM), Unix time in seconds or milliseconds at the start of the line.
 */
final class LogDates {

	static final int NONE = 0;
	static final int ISO = 1;
	static final int DAY_FIRST = 2;
	static final int MONTH_FIRST = 3;
	static final int ACCESS_LOG = 4;
	static final int SYSLOG = 5;
	static final int JUL = 6;
	static final int UNIX_TIME = 7;
	private static final int FORMATS = 8;

	/** How far into the line the date is looked for (a JSON line may have it after a few keys) */
	private static final int SCAN = 48;
	private static final int SCAN_JSON = 120;

	private static final String MONTHS = "janfebmaraprmayjunjulaugsepoctnovdec";

	private LogDates() {
	}

	/** Day number of a calendar date, 0 when it is not a plausible one (years 1971 to 2200) */
	static int days(int year, int month, int day) {
		if (year < 1971 || year > 2200 || month < 1 || month > 12 || day < 1 || day > 31) {
			return 0;
		}
		int y = month <= 2 ? year - 1 : year;
		int era = y / 400;
		int yoe = y - era * 400;
		int doy = (153 * (month + (month > 2 ? -3 : 9)) + 2) / 5 + day - 1;
		int doe = yoe * 365 + yoe / 4 - yoe / 100 + doy;
		return era * 146097 + doe - 719468;
	}

	/**
	 * Time of day of a log line in milliseconds (hh:mm:ss with optional fraction, looked for in the first 160 characters, whatever the
	 * date format is), -1 when the line has none.
	 */
	static int timeOfDay(String line) {
		int limit = Math.min(line.length(), 160) - 8;
		for (int i = 0; i <= limit; i++) {
			char c = line.charAt(i);
			if (c < '0' || c > '9' || (i > 0 && Character.isDigit(line.charAt(i - 1)))) {
				continue;
			}
			if (!Character.isDigit(line.charAt(i + 1)) || line.charAt(i + 2) != ':' || !Character.isDigit(line.charAt(i + 3))
					|| !Character.isDigit(line.charAt(i + 4)) || line.charAt(i + 5) != ':' || !Character.isDigit(line.charAt(i + 6))
					|| !Character.isDigit(line.charAt(i + 7))) {
				continue;
			}
			int hour = (c - '0') * 10 + line.charAt(i + 1) - '0';
			int minute = (line.charAt(i + 3) - '0') * 10 + line.charAt(i + 4) - '0';
			int second = (line.charAt(i + 6) - '0') * 10 + line.charAt(i + 7) - '0';
			if (hour > 23 || minute > 59 || second > 60) {
				continue;
			}
			int millis = 0;
			int at = i + 8;
			if (at < line.length() && (line.charAt(at) == '.' || line.charAt(at) == ',')) {
				int scale = 100;
				for (at++; at < line.length() && scale > 0 && Character.isDigit(line.charAt(at)); at++, scale /= 10) {
					millis += (line.charAt(at) - '0') * scale;
				}
			}
			return ((hour * 60 + minute) * 60 + second) * 1000 + millis;
		}
		return -1;
	}

	static java.time.LocalDate toDate(int day) {
		return java.time.LocalDate.ofEpochDay(day);
	}

	static int fromDate(java.time.LocalDate date) {
		return (int) date.toEpochDay();
	}

	/**
	 * Recognizes the date format of a file from its first bytes: the format found on most of the lines that can have a date
	 * (not the indented lines of a stack trace); {@link #NONE} when no format is found on enough lines.
	 */
	static int detect(byte[] data, int length, int defaultYear) {
		int[] counts = new int[FORMATS];
		int dayFirstOk = 0;
		int monthFirstOk = 0;
		int candidates = 0;
		int start = 0;
		for (int lines = 0; start < length && lines < 400; lines++) {
			int end = start;
			while (end < length && data[end] != '\n') {
				end++;
			}
			if (end > start && data[start] != ' ' && data[start] != '\t') {
				candidates++;
				for (int format = ISO; format < FORMATS; format++) {
					if (format == MONTH_FIRST) {
						continue;
					}
					if (parse(format, data, start, end, defaultYear) != 0) {
						counts[format]++;
					}
				}
				if (parse(MONTH_FIRST, data, start, end, defaultYear) != 0) {
					monthFirstOk++;
				}
				if (parse(DAY_FIRST, data, start, end, defaultYear) != 0) {
					dayFirstOk++;
				}
			}
			start = end + 1;
		}
		if (candidates < 3) {
			return NONE;
		}
		counts[MONTH_FIRST] = monthFirstOk;
		int best = NONE;
		int bestCount = 0;
		for (int format = ISO; format < FORMATS; format++) {
			if (counts[format] > bestCount) {
				best = format;
				bestCount = counts[format];
			}
		}
		if (best == DAY_FIRST && monthFirstOk > dayFirstOk) {
			best = MONTH_FIRST; // a month can not be over 12: the 13+ in the first position say that is the day
		}
		if (best == MONTH_FIRST && dayFirstOk >= monthFirstOk) {
			best = DAY_FIRST;
		}
		return bestCount >= Math.max(3, candidates * 3 / 10) ? best : NONE;
	}

	/** Day number of the date at the start of the line bytes {@code data[from, to)}, 0 when the line has no date in that format */
	static int parse(int format, byte[] data, int from, int to, int defaultYear) {
		if (to <= from || data[from] == ' ' || data[from] == '\t') {
			return 0; // empty or indented: stack trace, wrapped text
		}
		int limit = Math.min(to, from + (data[from] == '{' ? SCAN_JSON : SCAN));
		switch (format) {
		case ISO:
			return iso(data, from, limit);
		case DAY_FIRST:
			return numeric(data, from, limit, true);
		case MONTH_FIRST:
			return numeric(data, from, limit, false);
		case ACCESS_LOG:
			return accessLog(data, from, limit);
		case SYSLOG:
			return syslog(data, from, limit, defaultYear);
		case JUL:
			return jul(data, from, limit);
		case UNIX_TIME:
			return unixTime(data, from, limit);
		default:
			return 0;
		}
	}

	static int parse(int format, String line, int defaultYear) {
		byte[] bytes = line.getBytes(StandardCharsets.ISO_8859_1);
		return parse(format, bytes, 0, bytes.length, defaultYear);
	}

	/** yyyy-MM-dd or yyyy/MM/dd, also followed by T */
	private static int iso(byte[] b, int from, int limit) {
		for (int i = from; i + 10 <= limit; i++) {
			if (isDigit(b[i]) && (i == from || !isDigit(b[i - 1])) && isDigit(b[i + 1]) && isDigit(b[i + 2]) && isDigit(b[i + 3])
					&& (b[i + 4] == '-' || b[i + 4] == '/') && isDigit(b[i + 5]) && isDigit(b[i + 6]) && b[i + 7] == b[i + 4]
					&& isDigit(b[i + 8]) && isDigit(b[i + 9]) && (i + 10 == limit || !isDigit(b[i + 10]))) {
				int day = days(num(b, i, 4), num(b, i + 5, 2), num(b, i + 8, 2));
				if (day != 0) {
					return day;
				}
			}
		}
		return 0;
	}

	/** dd/MM/yyyy, dd-MM-yyyy, dd.MM.yyyy (or the month first) */
	private static int numeric(byte[] b, int from, int limit, boolean dayFirst) {
		for (int i = from; i + 10 <= limit; i++) {
			if (isDigit(b[i]) && (i == from || !isDigit(b[i - 1])) && isDigit(b[i + 1]) && (b[i + 2] == '/' || b[i + 2] == '-' || b[i + 2] == '.')
					&& isDigit(b[i + 3]) && isDigit(b[i + 4]) && b[i + 5] == b[i + 2]
					&& isDigit(b[i + 6]) && isDigit(b[i + 7]) && isDigit(b[i + 8]) && isDigit(b[i + 9]) && (i + 10 == limit || !isDigit(b[i + 10]))) {
				int first = num(b, i, 2);
				int second = num(b, i + 3, 2);
				int day = dayFirst ? days(num(b, i + 6, 4), second, first) : days(num(b, i + 6, 4), first, second);
				if (day != 0) {
					return day;
				}
			}
		}
		return 0;
	}

	/** 10/Oct/2026:13:55:36 */
	private static int accessLog(byte[] b, int from, int limit) {
		for (int i = from; i + 11 <= limit; i++) {
			if (isDigit(b[i]) && (i == from || !isDigit(b[i - 1])) && isDigit(b[i + 1]) && b[i + 2] == '/' && b[i + 6] == '/'
					&& isDigit(b[i + 7]) && isDigit(b[i + 8]) && isDigit(b[i + 9]) && isDigit(b[i + 10])) {
				int month = month(b, i + 3);
				if (month > 0) {
					int day = days(num(b, i + 7, 4), month, num(b, i, 2));
					if (day != 0) {
						return day;
					}
				}
			}
		}
		return 0;
	}

	/** Nov 16 12:00:00 (no year: the one of the file) */
	private static int syslog(byte[] b, int from, int limit, int defaultYear) {
		if (limit - from < 8) {
			return 0;
		}
		int month = month(b, from);
		if (month == 0) {
			return 0;
		}
		int i = from + 3;
		while (i < limit && b[i] == ' ') {
			i++;
		}
		if (i >= limit || !isDigit(b[i])) {
			return 0;
		}
		int day = b[i] - '0';
		i++;
		if (i < limit && isDigit(b[i])) {
			day = day * 10 + (b[i] - '0');
			i++;
		}
		if (i + 3 >= limit || b[i] != ' ' || !isDigit(b[i + 1]) || !isDigit(b[i + 2]) || b[i + 3] != ':') {
			return 0;
		}
		return days(defaultYear, month, day);
	}

	/** Nov 16, 2026 12:00:00 PM */
	private static int jul(byte[] b, int from, int limit) {
		if (limit - from < 12) {
			return 0;
		}
		int month = month(b, from);
		if (month == 0) {
			return 0;
		}
		int i = from + 3;
		while (i < limit && b[i] != ' ') {
			i++; // the month may be written in full
		}
		while (i < limit && b[i] == ' ') {
			i++;
		}
		if (i >= limit || !isDigit(b[i])) {
			return 0;
		}
		int day = b[i] - '0';
		i++;
		if (i < limit && isDigit(b[i])) {
			day = day * 10 + (b[i] - '0');
			i++;
		}
		if (i + 6 > limit || b[i] != ',' || b[i + 1] != ' ' || !isDigit(b[i + 2]) || !isDigit(b[i + 3]) || !isDigit(b[i + 4]) || !isDigit(b[i + 5])) {
			return 0;
		}
		return days(num(b, i + 2, 4), month, day);
	}

	/** 1763290000 (seconds) or 1763290000123 (milliseconds) at the start of the line */
	private static int unixTime(byte[] b, int from, int limit) {
		int end = from;
		long value = 0;
		while (end < limit && end - from < 14 && isDigit(b[end])) {
			value = value * 10 + (b[end] - '0');
			end++;
		}
		int size = end - from;
		if (end < limit && isDigit(b[end])) {
			return 0;
		}
		long seconds;
		if (size == 10) {
			seconds = value;
		} else if (size == 13) {
			seconds = value / 1000;
		} else {
			return 0;
		}
		if (seconds < 31536000L * 1 || seconds > 4102444800L) {
			return 0;
		}
		return (int) (seconds / 86400);
	}

	/** 1..12 for an English month name of 3 letters at the position (any case), else 0 */
	private static int month(byte[] b, int at) {
		for (int m = 0; m < 12; m++) {
			boolean same = true;
			for (int k = 0; k < 3; k++) {
				int c = b[at + k];
				if (c >= 'A' && c <= 'Z') {
					c += 32;
				}
				if (c != MONTHS.charAt(m * 3 + k)) {
					same = false;
					break;
				}
			}
			if (same) {
				return m + 1;
			}
		}
		return 0;
	}

	private static int num(byte[] b, int at, int size) {
		int value = 0;
		for (int i = 0; i < size; i++) {
			value = value * 10 + (b[at + i] - '0');
		}
		return value;
	}

	private static boolean isDigit(byte c) {
		return c >= '0' && c <= '9';
	}
}

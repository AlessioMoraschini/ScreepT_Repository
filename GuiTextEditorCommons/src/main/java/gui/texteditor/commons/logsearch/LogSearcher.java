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

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * One search over the file, streamed from the disk: the file is cut in big pieces that several threads scan at once, and the
 * matches come back in file order. Plain-text queries are matched on the raw bytes (no decoding, no allocation per line);
 * regular expressions run on the decoded line. Memory is a few MB of buffers plus the match bitmap, whatever the file size.
 * Matching lines are added to {@link #results} while the search runs; {@link #cancel()} stops it.
 */
final class LogSearcher {

	/** Bytes of a line that are matched by a regular expression (the displayed text is cut a lot earlier anyway) */
	private static final int MAX_MATCHED_BYTES = 1 << 20;

	final LineSet results = new LineSet();
	final LogQuery query;

	private final File file;
	private volatile long length;
	private final AtomicBoolean cancelled = new AtomicBoolean();
	private volatile boolean finished;
	private volatile long bytesRead;
	private volatile int linesScanned;

	LogSearcher(File file, LogQuery query) {
		this.file = file;
		this.length = file.length();
		this.query = query;
	}

	void start() {
		Thread thread = new Thread(this::run, "LogSearch-search");
		thread.setDaemon(true);
		thread.start();
	}

	void cancel() {
		cancelled.set(true);
	}

	boolean isFinished() {
		return finished;
	}

	boolean isCancelled() {
		return cancelled.get();
	}

	double progress() {
		return length == 0 ? 1d : Math.min(1d, bytesRead / (double) length);
	}

	int linesScanned() {
		return linesScanned;
	}

	/** Result of one piece: the matching lines, numbered from the first line of the piece */
	private static final class Part {
		int lines;
		int[] matches = new int[64];
		int count;

		void add(int line) {
			if (count == matches.length) {
				matches = Arrays.copyOf(matches, count * 2);
			}
			matches[count++] = line;
		}
	}

	private void run() {
		runFrom(0L);
	}

	/**
	 * The file grew (append only): the part from the start of line {@code fromLine} (at byte {@code fromOffset}) is scanned again,
	 * the matches found before it stay. Call it when the search is finished.
	 */
	void extend(int fromLine, long fromOffset) {
		results.truncate(fromLine);
		linesScanned = fromLine;
		bytesRead = fromOffset;
		length = file.length();
		finished = false;
		Thread thread = new Thread(() -> runFrom(fromOffset), "LogSearch-search");
		thread.setDaemon(true);
		thread.start();
	}

	private void runFrom(long startOffset) {
		final long began = System.nanoTime();
		boolean completed = false;
		try {
			final boolean bytes = query.bytesCapable();
			final boolean prefiltered = query.hasPrefilter();
			ChunkScan.run(file, startOffset, () -> {
				final LogQuery.Tester tester = bytes ? null : query.tester();
				final AsciiChars ascii = new AsciiChars();
				return (data, len, offset, unterminated) -> scan(data, len, unterminated, bytes, prefiltered, tester, ascii);
			}, (partial, end) -> {
				Part part = (Part) partial;
				int base = linesScanned;
				results.addAll(base, part.matches, part.count);
				linesScanned = base + part.lines;
				bytesRead = end;
			}, cancelled);
			completed = true;
		} catch (IOException e) {
			// what was found so far stays valid
		} finally {
			if (!cancelled.get()) {
				bytesRead = length;
			}
			finished = true;
			// only whole searches that ran to the end tell something about the speed (not stopped ones, nor the growth of a followed file)
			if (completed && startOffset == 0 && !cancelled.get()) {
				try {
					SearchStats.record(new SearchStats.Sample(System.currentTimeMillis(), System.nanoTime() - began, length, linesScanned, results.size(),
							query.bytesCapable() ? SearchStats.MODE_BYTES : (query.hasPrefilter() ? SearchStats.MODE_PREFILTER : SearchStats.MODE_DECODE), ChunkScan.defaultWorkers(), query.kind));
				} catch (Throwable e) {
					// statistics must never disturb the search
				}
			}
		}
	}

	private Part scan(byte[] data, int len, boolean unterminated, boolean bytes, boolean prefiltered, LogQuery.Tester tester, AsciiChars ascii) {
		Part part = new Part();
		int line = 0;
		int start = 0;
		while (start < len) {
			int end = start;
			int seen = 0;
			while (end < len && data[end] != '\n') {
				seen |= data[end];
				end++;
			}
			int contentEnd = end > start && data[end - 1] == '\r' ? end - 1 : end;
			boolean hit;
			if (bytes) {
				hit = query.testBytes(data, start, contentEnd);
			} else {
				int size = Math.min(contentEnd - start, MAX_MATCHED_BYTES);
				if (prefiltered && !query.passesPrefilter(data, start, contentEnd)) {
					hit = false;
				} else if (seen >= 0) { // pure ASCII: no decoding, no allocation
					hit = tester.test(ascii.wrap(data, start, size));
				} else {
					hit = tester.test(new String(data, start, size, StandardCharsets.UTF_8));
				}
			}
			if (hit) {
				part.add(line);
			}
			line++;
			start = end + 1;
		}
		part.lines = line;
		return part;
	}

	/** The bytes of an ASCII line seen as characters, reused from one line to the next */
	private static final class AsciiChars implements CharSequence {
		private byte[] data;
		private int from;
		private int size;

		AsciiChars wrap(byte[] data, int from, int size) {
			this.data = data;
			this.from = from;
			this.size = size;
			return this;
		}

		@Override
		public int length() {
			return size;
		}

		@Override
		public char charAt(int index) {
			return (char) data[from + index];
		}

		@Override
		public CharSequence subSequence(int start, int end) {
			return new String(data, from + start, end - start, StandardCharsets.ISO_8859_1);
		}

		@Override
		public String toString() {
			return new String(data, from, size, StandardCharsets.ISO_8859_1);
		}
	}
}

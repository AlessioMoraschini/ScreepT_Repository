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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * One search over the file, streamed from the disk on a daemon thread: only the current line is in memory.
 * Matching lines are appended to {@link #results} while the search runs; {@link #cancel()} stops it.
 */
final class LogSearcher {

	/** Bytes of a line that are matched (the displayed text is cut a lot earlier anyway) */
	private static final int MAX_MATCHED_BYTES = 1 << 20;

	final IntList results = new IntList();
	final LogQuery query;

	private final File file;
	private final long length;
	private volatile boolean cancelled;
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
		cancelled = true;
	}

	boolean isFinished() {
		return finished;
	}

	boolean isCancelled() {
		return cancelled;
	}

	double progress() {
		return length == 0 ? 1d : Math.min(1d, bytesRead / (double) length);
	}

	int linesScanned() {
		return linesScanned;
	}

	private void run() {
		try (InputStream in = new FileInputStream(file)) {
			byte[] buffer = new byte[1 << 20];
			byte[] line = new byte[8192];
			int lineLength = 0;
			boolean pending = false;
			int lineNumber = 0;
			int read;
			while (!cancelled && (read = in.read(buffer)) > 0) {
				for (int i = 0; i < read; i++) {
					byte current = buffer[i];
					if (current == '\n') {
						scan(line, lineLength, lineNumber++);
						lineLength = 0;
						pending = false;
						continue;
					}
					pending = true;
					if (lineLength < MAX_MATCHED_BYTES) {
						if (lineLength == line.length) {
							line = Arrays.copyOf(line, line.length * 2);
						}
						line[lineLength++] = current;
					}
				}
				bytesRead += read;
				linesScanned = lineNumber;
			}
			if (!cancelled && pending) {
				scan(line, lineLength, lineNumber++);
			}
			linesScanned = lineNumber;
		} catch (IOException e) {
			// what was found so far stays valid
		} finally {
			finished = true;
		}
	}

	private void scan(byte[] line, int length, int lineNumber) {
		int end = length > 0 && line[length - 1] == '\r' ? length - 1 : length;
		if (query.test(new String(line, 0, end, StandardCharsets.UTF_8))) {
			results.add(lineNumber);
		}
	}
}

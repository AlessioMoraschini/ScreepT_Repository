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

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Read-only view of a (possibly huge) text file that never loads it: a background pass records the offset of one line
 * every {@link #BLOCK}, and a line is read on demand from its block (a small cache keeps the recent ones). Memory use
 * is the sparse index (8 bytes per 64 lines) plus the cache, whatever the file size. Lines end with LF, a trailing CR is dropped.
 */
final class LogFileIndex implements Closeable {

	static final int BLOCK = 64;
	/** Longer lines are shown cut (the file is untouched) */
	static final int MAX_DISPLAY_BYTES = 4000;
	private static final int MAX_BLOCK_BYTES = 16 * 1024 * 1024;
	private static final int CACHED_BLOCKS = 256;
	/** Bytes of a line looked at to find its level */
	private static final int HEAD = 160;
	/** Longest line returned in full (export) */
	private static final int MAX_FULL_BYTES = 8 * 1024 * 1024;

	private final File file;
	private final RandomAccessFile reader;
	private final long length;

	private final Object offsetsLock = new Object();
	private long[] blockOffsets = new long[1024];
	private int blockCount;

	private volatile int lineCount;
	private volatile byte[] levels = new byte[1 << 16];
	private final int[] levelCounts = new int[LogLevel.COUNT];
	private volatile boolean anyLevel;
	private volatile long indexedBytes;
	private volatile boolean complete;
	private volatile boolean closed;

	private final Map<Integer, String[]> cache = new LinkedHashMap<Integer, String[]>(64, 0.75f, true) {
		@Override
		protected boolean removeEldestEntry(Map.Entry<Integer, String[]> eldest) {
			return size() > CACHED_BLOCKS;
		}
	};

	LogFileIndex(File file) throws IOException {
		this.file = file;
		this.reader = new RandomAccessFile(file, "r");
		this.length = reader.length();
	}

	File file() {
		return file;
	}

	long length() {
		return length;
	}

	int lineCount() {
		return lineCount;
	}

	boolean isComplete() {
		return complete;
	}

	/** Stored level of the line (see {@link LogLevel}), {@link LogLevel#NONE} when unknown */
	byte levelByte(int line) {
		byte[] current = levels;
		return line >= 0 && line < current.length && line < lineCount ? current[line] : (byte) LogLevel.NONE;
	}

	int level(int line) {
		return LogLevel.level(levelByte(line));
	}

	/** Lines found so far for a level (the inherited ones of a stack trace are not counted) */
	int levelCount(int level) {
		return levelCounts[level];
	}

	/** True when at least one line with a level word was found */
	boolean hasLevels() {
		return anyLevel;
	}

	/** Fraction of the file already indexed, 0..1 */
	double progress() {
		return length == 0 ? 1d : Math.min(1d, indexedBytes / (double) length);
	}

	/** Starts the indexing pass on a daemon thread */
	void startIndexing() {
		Thread thread = new Thread(this::buildIndex, "LogSearch-index");
		thread.setDaemon(true);
		thread.start();
	}

	private void buildIndex() {
		try (InputStream in = new FileInputStream(file)) {
			byte[] buffer = new byte[1 << 20];
			byte[] head = new byte[HEAD];
			int headLength = 0;
			int previousLevel = LogLevel.NONE;
			long position = 0;
			long lines = 0;
			boolean lineStart = true;
			int read;
			while (!closed && lines < Integer.MAX_VALUE && (read = in.read(buffer)) > 0) {
				for (int i = 0; i < read; i++) {
					if (lineStart) {
						if (lines % BLOCK == 0) {
							addBlockOffset(position + i);
						}
						lines++;
						lineStart = false;
						headLength = 0;
					}
					byte current = buffer[i];
					if (current == '\n') {
						lineStart = true;
						previousLevel = finishLine(head, headLength, (int) (lines - 1), previousLevel);
					} else if (headLength < HEAD) {
						head[headLength++] = current;
					}
				}
				position += read;
				indexedBytes = position;
				lineCount = (int) Math.min(lines, Integer.MAX_VALUE);
			}
			if (!lineStart && lines > 0) {
				finishLine(head, headLength, (int) (lines - 1), previousLevel);
			}
		} catch (IOException e) {
			// a file that cannot be read further just stays as indexed so far
		} finally {
			complete = true;
			indexedBytes = length;
		}
	}

	/** Stores the level of the line that just ended; returns the level that its continuation lines will inherit */
	private int finishLine(byte[] head, int length, int line, int previousLevel) {
		int level = LogLevel.detect(head, length);
		byte stored;
		if (level >= 0) {
			stored = (byte) level;
			levelCounts[level]++;
			anyLevel = true;
		} else if (previousLevel != LogLevel.NONE && LogLevel.looksLikeContinuation(head, length)) {
			stored = (byte) (previousLevel | LogLevel.CONTINUATION);
		} else {
			stored = (byte) LogLevel.NONE;
		}
		byte[] current = levels;
		if (line >= current.length) {
			current = Arrays.copyOf(current, Math.max(current.length * 2, line + 1));
			levels = current;
		}
		current[line] = stored;
		return LogLevel.level(stored);
	}

	private void addBlockOffset(long offset) {
		synchronized (offsetsLock) {
			if (blockCount == blockOffsets.length) {
				blockOffsets = Arrays.copyOf(blockOffsets, blockCount * 2);
			}
			blockOffsets[blockCount++] = offset;
		}
	}

	/** Text of the line (0-based), empty when it is not indexed yet or the file cannot be read */
	String line(int number) {
		if (number < 0 || number >= lineCount) {
			return "";
		}
		String[] lines = block(number / BLOCK);
		int inBlock = number % BLOCK;
		return inBlock < lines.length ? lines[inBlock] : "";
	}

	/** The line as it is in the file (up to 8 MB), for export; the displayed text of a very long line is cut */
	String lineFull(int number) {
		if (number < 0 || number >= lineCount) {
			return "";
		}
		long start;
		long end;
		int block = number / BLOCK;
		synchronized (offsetsLock) {
			if (block >= blockCount) {
				return "";
			}
			start = blockOffsets[block];
			end = block + 1 >= blockCount ? Math.max(start, complete ? length : indexedBytes) : blockOffsets[block + 1];
		}
		try {
			byte[] data = new byte[(int) Math.min(end - start, MAX_BLOCK_BYTES)];
			synchronized (reader) {
				reader.seek(start);
				reader.readFully(data);
			}
			int wanted = number % BLOCK;
			int from = 0;
			for (int n = 0; n < wanted && from < data.length; n++) {
				while (from < data.length && data[from] != '\n') {
					from++;
				}
				from++;
			}
			int to = from;
			while (to < data.length && data[to] != '\n') {
				to++;
			}
			if (to > from && data[to - 1] == '\r') {
				to--;
			}
			return from >= data.length ? "" : new String(data, from, Math.min(to - from, MAX_FULL_BYTES), StandardCharsets.UTF_8);
		} catch (IOException | RuntimeException e) {
			return "";
		}
	}

	private String[] block(int block) {
		synchronized (cache) {
			String[] cached = cache.get(block);
			if (cached != null) {
				return cached;
			}
		}

		long start;
		long end;
		boolean lastKnown;
		synchronized (offsetsLock) {
			if (block >= blockCount) {
				return new String[0];
			}
			start = blockOffsets[block];
			lastKnown = block + 1 >= blockCount;
			end = lastKnown ? Math.max(start, complete ? length : indexedBytes) : blockOffsets[block + 1];
		}

		String[] lines;
		try {
			byte[] data = new byte[(int) Math.min(end - start, MAX_BLOCK_BYTES)];
			synchronized (reader) {
				reader.seek(start);
				reader.readFully(data);
			}
			lines = split(data);
		} catch (IOException | RuntimeException e) {
			return new String[] { "<cannot read this part of the file: " + e.getMessage() + ">" };
		}

		// the last block is still growing while the index is being built
		if (!lastKnown || complete) {
			synchronized (cache) {
				cache.put(block, lines);
			}
		}
		return lines;
	}

	private static String[] split(byte[] data) {
		String[] lines = new String[BLOCK];
		int count = 0;
		int start = 0;
		while (start < data.length && count < BLOCK) {
			int end = start;
			while (end < data.length && data[end] != '\n') {
				end++;
			}
			int contentEnd = end;
			if (contentEnd > start && data[contentEnd - 1] == '\r') {
				contentEnd--;
			}
			int shown = Math.min(contentEnd - start, MAX_DISPLAY_BYTES);
			String text = new String(data, start, shown, StandardCharsets.UTF_8);
			lines[count++] = shown < contentEnd - start ? text + " ..." : text;
			start = end + 1;
		}
		return count == BLOCK ? lines : Arrays.copyOf(lines, count);
	}

	@Override
	public void close() {
		closed = true;
		try {
			synchronized (reader) {
				reader.close();
			}
		} catch (IOException e) {
			// nothing left to release
		}
	}
}

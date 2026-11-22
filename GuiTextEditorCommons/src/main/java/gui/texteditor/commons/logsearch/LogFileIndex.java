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
import java.util.zip.CRC32;
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
	private volatile long length;

	private final Object offsetsLock = new Object();
	private long[] blockOffsets = new long[1024];
	private int blockCount;

	private volatile int lineCount;
	/** Two lines per byte (4 bits each): see {@link #encode} */
	private volatile byte[] levels = new byte[1 << 15];
	private final java.util.concurrent.atomic.AtomicBoolean stopIndexing = new java.util.concurrent.atomic.AtomicBoolean();
	private int indexedLines;
	private int previousLevelCommitted = LogLevel.NONE;
	private final int[] levelCounts = new int[LogLevel.COUNT];
	private volatile boolean anyLevel;
	private volatile long indexedBytes;
	private volatile boolean complete;
	private volatile boolean closed;

	/** Bytes used to recognize that the file is still the one that was indexed (start and end of the indexed part) */
	private static final int FINGERPRINT = 4096;
	private volatile long scannedEnd;
	private volatile long snapshotLength;
	private volatile long snapshotModified;
	private volatile long sampleFingerprint;
	private volatile boolean snapshotReady;
	/** Line and byte where the last extension restarted: the matches of a search are scanned again from there */
	private volatile int resumeLine;
	private volatile long resumeOffset;
	private volatile int extendFromBlock = -1;

	/** Date format of the file (see LogDates), found on its first lines before the indexing starts */
	private volatile int dateFormat = LogDates.NONE;
	private int defaultYear = java.time.LocalDate.now().getYear();

	/** What happened to the file on disk since it was indexed */
	enum Change {
		UNCHANGED, APPENDED, REWRITTEN, MISSING
	}

	private final Map<Integer, String[]> cache = new LinkedHashMap<Integer, String[]>(64, 0.75f, true) {
		@Override
		protected boolean removeEldestEntry(Map.Entry<Integer, String[]> eldest) {
			return size() > CACHED_BLOCKS;
		}
	};

	LogFileIndex(File file) throws IOException {
		this.file = file;
		// opened only to check that it can be read: no handle is kept (on Windows an open file cannot be deleted or renamed by the
		// program that writes it, so a log could not rotate while it is shown here)
		try (RandomAccessFile probe = new RandomAccessFile(file, "r")) {
			this.length = probe.length();
		}
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
		int slot = line >> 1;
		if (line < 0 || slot >= current.length || line >= lineCount) {
			return (byte) LogLevel.NONE;
		}
		return decode((current[slot] >> ((line & 1) << 2)) & 0x0F);
	}

	/** Nibble: 0-4 level, 5 no level, 6-10 level inherited by a continuation line */
	private static int encode(byte stored) {
		int level = LogLevel.level(stored);
		if (level == LogLevel.NONE) {
			return 5;
		}
		return LogLevel.isContinuation(stored) ? 6 + level : level;
	}

	private static byte decode(int nibble) {
		if (nibble == 5 || nibble > 10) {
			return (byte) LogLevel.NONE;
		}
		return nibble > 5 ? (byte) ((nibble - 6) | LogLevel.CONTINUATION) : (byte) nibble;
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
		detectDates();
		Thread thread = new Thread(this::buildIndex, "LogSearch-index");
		thread.setDaemon(true);
		thread.start();
	}

	/** Looks at the first 128 KB to find the date format; the year of syslog lines (they have none) is the one of the file */
	private void detectDates() {
		try (InputStream in = new FileInputStream(file)) {
			byte[] sample = new byte[128 * 1024];
			int read = 0;
			int n;
			while (read < sample.length && (n = in.read(sample, read, sample.length - read)) > 0) {
				read += n;
			}
			long modified = file.lastModified();
			if (modified > 0) {
				defaultYear = java.time.Instant.ofEpochMilli(modified).atZone(java.time.ZoneId.systemDefault()).getYear();
			}
			dateFormat = LogDates.detect(sample, read, defaultYear);
		} catch (IOException | RuntimeException e) {
			dateFormat = LogDates.NONE;
		}
	}

	/** True when the lines have dates in a known format: the date filter is possible */
	boolean hasDates() {
		return dateFormat != LogDates.NONE && lineCount > 0;
	}

	int dateFormat() {
		return dateFormat;
	}

	/** Day number of a line (read from its text), 0 when the line has no date */
	int dayOf(int line) {
		return dateFormat == LogDates.NONE ? 0 : LogDates.parse(dateFormat, line(line), defaultYear);
	}

	/** Day of the first dated line, 0 when unknown */
	int firstDay() {
		for (int l = 0; l < lineCount && l < 400; l++) {
			int day = dayOf(l);
			if (day != 0) {
				return day;
			}
		}
		return 0;
	}

	/** Day of the last dated line indexed so far, 0 when unknown */
	int lastDay() {
		for (int l = lineCount - 1, tries = 0; l >= 0 && tries < 400; l--, tries++) {
			int day = dayOf(l);
			if (day != 0) {
				return day;
			}
		}
		return 0;
	}

	/**
	 * Lines from a day (0 = no limit) to a day (included), as [start, end). Nothing is recorded while indexing: the limits are found when
	 * asked, with a binary search over the blocks of 64 lines that reads the first dated line of about 15 of them. It assumes the lines
	 * are in chronological order (the normal case of a log); a line out of order can fall on the wrong side of a limit.
	 */
	int[] lineRange(int fromDay, int toDay) {
		int start = 0;
		int end = lineCount;
		if (fromDay > 0) {
			start = boundary(fromDay, false);
		}
		if (toDay > 0) {
			end = boundary(toDay, true);
		}
		return new int[] {start, Math.max(start, end)};
	}

	/**
	 * First line of [from, to) (all of one day) whose time of day is >= ms (> ms when strictly): binary search that reads about
	 * 20 lines. A line without time (stack trace) takes the one of the next line that has it.
	 */
	int timeBoundary(int from, int to, int ms, boolean strictly) {
		int low = Math.max(0, from);
		int end = Math.min(to, lineCount);
		int high = end;
		while (low < high) {
			int mid = (low + high) >>> 1;
			int time = -1;
			// the look-ahead of a line without time is limited by the end of the range, not by the narrowing search window
			for (int probe = mid; time < 0 && probe < end && probe < mid + 64; probe++) {
				time = LogDates.timeOfDay(line(probe));
			}
			if (time >= 0 && (strictly ? time > ms : time >= ms)) {
				high = mid;
			} else {
				low = mid + 1;
			}
		}
		return low;
	}

	/** Day of the first dated line of a block, 0 when none of its lines has a date */
	private int blockFirstDay(int block) {
		int last = Math.min(lineCount, (block + 1) * BLOCK);
		for (int line = block * BLOCK; line < last; line++) {
			int day = dayOf(line);
			if (day != 0) {
				return day;
			}
		}
		return 0;
	}

	/** First line dated >= day (or > day when strictly) */
	private int boundary(int day, boolean strictly) {
		int blocks;
		synchronized (offsetsLock) {
			blocks = Math.min(blockCount, (lineCount + BLOCK - 1) / BLOCK);
		}
		int low = 0;
		int high = blocks;
		while (low < high) {
			int mid = (low + high) >>> 1;
			int value = blockFirstDay(mid);
			for (int probe = mid + 1; value == 0 && probe < blocks && probe < mid + 8; probe++) {
				value = blockFirstDay(probe); // a block of an only stack trace: the next dated one tells the same
			}
			if (value != 0 && (strictly ? value > day : value >= day)) {
				high = mid;
			} else {
				low = mid + 1;
			}
		}
		// the line is in the block before the first one that qualifies (the lines of the day before come first) or at its start;
		// a stack trace of more than a block has no dated line, so the scan goes on until the first dated line that qualifies
		int scanFrom = Math.max(0, (low - 1) * BLOCK);
		for (int line = scanFrom; line < lineCount; line++) {
			int value = dayOf(line);
			if (value != 0 && (strictly ? value > day : value >= day)) {
				return line;
			}
		}
		return lineCount;
	}

	/** Raw verdict of one line, before the inheritance of the stack trace levels is applied */
	private static final byte RAW_NONE = 0x30;
	private static final byte RAW_CONTINUATION = 0x20;

	/** One piece of the file: a verdict for each line and where each line starts */
	private static final class Part {
		int lines;
		byte[] raw = new byte[256];
		int[] starts = new int[256];
		long offset;
	}

	private void buildIndex() {
		runIndex(0L);
	}

	private void runIndex(long startOffset) {
		try {
			ChunkScan.run(file, startOffset, () -> new ChunkScan.Task() {
				private final byte[] head = new byte[HEAD];

				@Override
				public Object process(byte[] data, int len, long offset, boolean unterminated) {
					return analyse(data, len, offset, head);
				}
			}, (partial, end) -> commit((Part) partial, end), stopIndexing);
		} catch (IOException e) {
			// a file that cannot be read further just stays as indexed so far
		} finally {
			if (!closed && !stopIndexing.get()) {
				captureSnapshot();
			}
			int from = extendFromBlock;
			if (from >= 0) {
				// the last block grew: its cached text is old
				synchronized (cache) {
					cache.keySet().removeIf(block -> block >= from);
				}
				extendFromBlock = -1;
			}
			complete = true;
			indexedBytes = length;
		}
	}

	/** What the file looked like when the index was finished: its size, its date and the checksum of its start and of its end */
	private void captureSnapshot() {
		long end = scannedEnd;
		length = end;
		snapshotLength = end;
		snapshotModified = file.lastModified();
		try (RandomAccessFile probe = new RandomAccessFile(file, "r")) {
			sampleFingerprint = sample(probe, end);
			snapshotReady = true;
		} catch (IOException | RuntimeException e) {
			snapshotReady = false;
		}
	}

	/**
	 * Checksum of small windows of the first {@code size} bytes: the start, the end and 7 more spread along the file. Reading the
	 * whole file would cost too much on a big one; a change inside it is seen unless it falls between the windows and keeps the size.
	 */
	private static long sample(RandomAccessFile probe, long size) throws IOException {
		CRC32 crc = new CRC32();
		byte[] data = new byte[FINGERPRINT];
		for (int k = 0; k <= 8; k++) {
			long from = k == 8 ? Math.max(0, size - FINGERPRINT) : size * k / 8;
			int window = (int) Math.min(FINGERPRINT, size - from);
			if (window <= 0) {
				continue;
			}
			probe.seek(from);
			probe.readFully(data, 0, window);
			crc.update(data, 0, window);
		}
		return crc.getValue();
	}

	/**
	 * Compares the file on disk with what was indexed (size and date first, then the checksum of its start and of the end of the
	 * indexed part). APPENDED: the indexed part is still the same and the file is longer, so the indexed lines are still right.
	 * REWRITTEN: something else changed (shorter file, edited, replaced by a new log after a rotation): the index does not match
	 * it any more and must be built again. Only meaningful when the index is complete.
	 */
	Change inspect() {
		if (!snapshotReady || closed || !complete) {
			return Change.UNCHANGED;
		}
		if (!file.exists()) {
			return Change.MISSING;
		}
		long now = file.length();
		long modified = file.lastModified();
		if (now == snapshotLength && modified == snapshotModified) {
			return Change.UNCHANGED;
		}
		if (now < snapshotLength) {
			return Change.REWRITTEN;
		}
		try (RandomAccessFile probe = new RandomAccessFile(file, "r")) {
			if (sample(probe, snapshotLength) != sampleFingerprint) {
				return Change.REWRITTEN;
			}
		} catch (IOException | RuntimeException e) {
			return Change.UNCHANGED; // locked for a moment: it is looked at again later
		}
		// same size and a new date: edited in place (or touched); reading it again is the only way to be sure
		return now > snapshotLength ? Change.APPENDED : Change.REWRITTEN;
	}

	/**
	 * The file gained lines at its end: only the last block (64 lines) is read again from its start and the new part is indexed;
	 * the lines already indexed stay valid and readable meanwhile. False when the index is not in a state to be extended.
	 */
	synchronized boolean extend() {
		if (!complete || closed) {
			return false;
		}
		int firstLine;
		long startOffset;
		synchronized (offsetsLock) {
			if (blockCount == 0) {
				firstLine = 0;
				startOffset = 0;
			} else {
				firstLine = (blockCount - 1) * BLOCK;
				startOffset = blockOffsets[blockCount - 1];
			}
		}
		// the lines of that block are indexed again: their levels leave the counters first
		for (int line = firstLine; line < lineCount; line++) {
			byte stored = levelByte(line);
			int level = LogLevel.level(stored);
			if (level != LogLevel.NONE && !LogLevel.isContinuation(stored)) {
				levelCounts[level]--;
			}
		}
		previousLevelCommitted = firstLine == 0 ? LogLevel.NONE : LogLevel.level(levelByte(firstLine - 1));
		indexedLines = firstLine;
		resumeLine = firstLine;
		resumeOffset = startOffset;
		extendFromBlock = firstLine / BLOCK;
		length = file.length();
		snapshotReady = false;
		complete = false;
		Thread thread = new Thread(() -> runIndex(startOffset), "LogSearch-index");
		thread.setDaemon(true);
		thread.start();
		return true;
	}

	int resumeLine() {
		return resumeLine;
	}

	long resumeOffset() {
		return resumeOffset;
	}

	private static Part analyse(byte[] data, int len, long offset, byte[] head) {
		Part part = new Part();
		part.offset = offset;
		int line = 0;
		int start = 0;
		while (start < len) {
			int end = start;
			while (end < len && data[end] != '\n') {
				end++;
			}
			if (line == part.raw.length) {
				part.raw = Arrays.copyOf(part.raw, line * 2);
				part.starts = Arrays.copyOf(part.starts, line * 2);
			}
			part.starts[line] = start;
			int headLength = Math.min(end - start, HEAD);
			System.arraycopy(data, start, head, 0, headLength);
			int level = LogLevel.detect(head, headLength);
			part.raw[line++] = level >= 0 ? (byte) level : LogLevel.looksLikeContinuation(head, headLength) ? RAW_CONTINUATION : RAW_NONE;
			start = end + 1;
		}
		part.lines = line;
		return part;
	}

	/** Runs in file order, one piece at a time: applies the stack trace inheritance, stores levels and offsets */
	private void commit(Part part, long end) {
		int base = indexedLines;
		if ((long) base + part.lines >= Integer.MAX_VALUE) {
			stopIndexing.set(true);
			return;
		}
		byte[] current = levels;
		int needed = ((base + part.lines) >> 1) + 1;
		if (needed > current.length) {
			current = Arrays.copyOf(current, Math.max(current.length * 2, needed));
		}
		int previousLevel = previousLevelCommitted;
		for (int r = 0; r < part.lines; r++) {
			int line = base + r;
			if (line % BLOCK == 0) {
				addBlockOffset(line / BLOCK, part.offset + part.starts[r]);
			}
			byte raw = part.raw[r];
			byte stored;
			if (raw >= 0 && raw < LogLevel.COUNT) {
				stored = raw;
				levelCounts[raw]++;
				anyLevel = true;
			} else if (raw == RAW_CONTINUATION && previousLevel != LogLevel.NONE) {
				stored = (byte) (previousLevel | LogLevel.CONTINUATION);
			} else {
				stored = (byte) LogLevel.NONE;
			}
			previousLevel = LogLevel.level(stored);
			int shift = (line & 1) << 2;
			int slot = line >> 1;
			current[slot] = (byte) ((current[slot] & ~(0x0F << shift)) | (encode(stored) << shift));
		}
		previousLevelCommitted = previousLevel;
		indexedLines = base + part.lines;
		levels = current;
		indexedBytes = end;
		scannedEnd = end;
		lineCount = indexedLines;
	}

	private void addBlockOffset(int block, long offset) {
		synchronized (offsetsLock) {
			if (block < blockCount) {
				blockOffsets[block] = offset; // a block that is indexed again
				return;
			}
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
			byte[] data = readAt(start, (int) Math.min(end - start, MAX_BLOCK_BYTES));
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

	/** Reads a piece of the file, opening it just for this read */
	private byte[] readAt(long start, int size) throws IOException {
		byte[] data = new byte[size];
		try (RandomAccessFile reader = new RandomAccessFile(file, "r")) {
			reader.seek(start);
			reader.readFully(data);
		}
		return data;
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
			lines = split(readAt(start, (int) Math.min(end - start, MAX_BLOCK_BYTES)));
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
		stopIndexing.set(true);
	}
}

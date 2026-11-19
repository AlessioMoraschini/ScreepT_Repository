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
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * Reads a file once, front to back, in big pieces that always end on a line end, and lets several threads work on them at the
 * same time. Every worker reads its own piece with a positional read (no shared reader, so the disk and the page cache are used
 * by all the threads at once) and scans it; results are handed back strictly in file order, so the callers can count lines.
 * A piece owns the lines that <i>start</i> inside it, so a line is never split nor read twice. Memory is a few buffers per worker
 * (a few MB), whatever the file size. No memory mapping: on Windows it would lock the log against rotation.
 */
final class ChunkScan {

	/** Size of a piece (the property is only there for tests: tiny pieces exercise the lines that cross a boundary) */
	private static final int CHUNK = Math.max(16, Integer.getInteger("screept.logsearch.chunk", 2 * 1024 * 1024));
	/** Extra room after a piece for the end of its last line (grows for longer lines) */
	private static final int TAIL = 64 * 1024;
	/** How many pieces a worker may be ahead of the one still to be handed back (limits what waits in memory) */
	private static final int AHEAD = 16;

	/** Work on one piece; runs on several threads, one {@code Task} instance per thread */
	interface Task {
		/**
		 * @param data bytes of whole lines
		 * @param length valid bytes
		 * @param offset position of {@code data[0]} in the file
		 * @param unterminated true when the last line has no line end (end of the file)
		 */
		Object process(byte[] data, int length, long offset, boolean unterminated);
	}

	/** Receives the results of {@link Task} in file order, one at a time */
	interface Committer {
		void commit(Object partial, long endOffset);
	}

	private ChunkScan() {
	}

	static int defaultWorkers() {
		int cpus = Runtime.getRuntime().availableProcessors();
		return Integer.getInteger("screept.logsearch.workers", Math.max(1, Math.max(Math.min(4, cpus - 1), Math.min(8, cpus / 2))));
	}

	/** Runs on worker threads; returns when everything is done or cancelled */
	static void run(File file, Supplier<Task> tasks, Committer committer, AtomicBoolean cancelled) throws IOException {
		run(file, 0L, tasks, committer, cancelled);
	}

	/** Same, but starting at a byte offset that is the beginning of a line: only what a file gained is scanned */
	static void run(File file, final long startOffset, Supplier<Task> tasks, final Committer committer, final AtomicBoolean cancelled) throws IOException {
		final long length = file.length();
		if (startOffset >= length) {
			return;
		}
		final int workers = (int) Math.max(1, Math.min(defaultWorkers(), (length - startOffset + CHUNK - 1) / CHUNK));
		final AtomicLong claim = new AtomicLong();
		final Object commitLock = new Object();
		final TreeMap<Long, Object[]> pending = new TreeMap<Long, Object[]>();
		final long[] next = { 0 };
		final AtomicBoolean failed = new AtomicBoolean();
		final AtomicReference<IOException> ioError = new AtomicReference<IOException>();

		Thread[] threads = new Thread[workers];
		for (int w = 0; w < workers; w++) {
			final Task task = tasks.get();
			threads[w] = new Thread(new Runnable() {
				@Override
				public void run() {
					try (FileInputStream in = new FileInputStream(file); FileChannel channel = in.getChannel()) {
						byte[] data = new byte[CHUNK + TAIL];
						final byte[] probe = new byte[TAIL];
						while (!cancelled.get() && !failed.get()) {
							long sequence = claim.getAndIncrement();
							long position = startOffset + sequence * CHUNK;
							if (position >= length) {
								return;
							}
							// not too far ahead of the piece that is still to be handed back
							synchronized (commitLock) {
								while (sequence - next[0] >= AHEAD && !cancelled.get() && !failed.get()) {
									commitLock.wait(20);
								}
							}
							if (cancelled.get() || failed.get()) {
								return;
							}
							long first = position == startOffset ? position : firstLineStart(channel, probe, position, length);
							Object[] result = null; // null: no line starts in this piece
							if (first < position + CHUNK && first < length) {
								int want = (int) (Math.min(position + CHUNK, length) - first);
								int have = readFully(channel, data, 0, want, first);
								boolean unterminated = false;
								if (have > 0 && data[have - 1] != '\n') { // the last line goes on after the piece
									while (true) {
										if (data.length - have < TAIL) {
											data = Arrays.copyOf(data, data.length * 2);
										}
										int more = readFully(channel, data, have, Math.min(TAIL, data.length - have), first + have);
										int cut = -1;
										for (int i = have; i < have + more; i++) {
											if (data[i] == '\n') {
												cut = i + 1;
												break;
											}
										}
										if (cut >= 0) {
											have = cut;
											break;
										}
										have += more;
										if (more <= 0) { // end of the file inside the last line
											unterminated = true;
											break;
										}
									}
								}
								if (have > 0) {
									Object partial = task.process(data, have, first, unterminated);
									result = new Object[] { partial, first + have };
								}
								if (data.length > 4 * (CHUNK + TAIL)) { // a very long line: the big buffer is not kept
									data = new byte[CHUNK + TAIL];
								}
							}
							synchronized (commitLock) {
								pending.put(sequence, result == null ? new Object[0] : result);
								Object[] ready;
								while ((ready = pending.remove(next[0])) != null) {
									if (ready.length > 0) {
										committer.commit(ready[0], (Long) ready[1]);
									}
									next[0]++;
								}
								commitLock.notifyAll();
							}
						}
					} catch (IOException e) {
						ioError.compareAndSet(null, e);
						failed.set(true);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						failed.set(true);
					} catch (RuntimeException | Error t) {
						failed.set(true);
					}
				}
			}, "LogSearch-worker-" + w);
			threads[w].setDaemon(true);
			threads[w].start();
		}

		for (Thread t : threads) {
			try {
				t.join();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				cancelled.set(true);
			}
		}
		if (ioError.get() != null) {
			throw ioError.get();
		}
	}

	/** Reads until the range is full or the file ends; returns how many bytes were read */
	private static int readFully(FileChannel channel, byte[] data, int offset, int length, long position) throws IOException {
		ByteBuffer buffer = ByteBuffer.wrap(data, offset, length);
		while (buffer.hasRemaining()) {
			int read = channel.read(buffer, position + (buffer.position() - offset));
			if (read < 0) {
				break;
			}
		}
		return buffer.position() - offset;
	}

	/** Start of the first line that begins at or after {@code position} ({@link Long#MAX_VALUE}: none in this piece) */
	private static long firstLineStart(FileChannel channel, byte[] probe, long position, long length) throws IOException {
		long at = position - 1; // the line break just before the piece makes the piece start on a line
		long limit = Math.min(length, position + CHUNK);
		while (at < limit) {
			int read = readFully(channel, probe, 0, probe.length, at);
			if (read <= 0) {
				break;
			}
			for (int i = 0; i < read; i++) {
				if (probe[i] == '\n') {
					return at + i + 1;
				}
			}
			at += read;
		}
		return Long.MAX_VALUE;
	}
}

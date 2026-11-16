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
import java.util.Arrays;
import java.util.TreeMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * Reads a file once, front to back, in big pieces that always end on a line end, and lets several threads work on them at the
 * same time. Results are handed back strictly in file order, so the callers can count lines. Memory is a fixed pool of buffers
 * (a few MB), whatever the file size. Plain sequential reads are used (no memory mapping: on Windows it would lock the log
 * against rotation).
 */
final class ChunkScan {

	private static final int CHUNK = 2 * 1024 * 1024;

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

	private static final class Chunk {
		final byte[] data;
		final int length;
		final long offset;
		final long sequence;
		final boolean unterminated;

		Chunk(byte[] data, int length, long offset, long sequence, boolean unterminated) {
			this.data = data;
			this.length = length;
			this.offset = offset;
			this.sequence = sequence;
			this.unterminated = unterminated;
		}
	}

	private static final Chunk END = new Chunk(new byte[0], 0, 0, -1, false);

	private ChunkScan() {
	}

	static int defaultWorkers() {
		return Integer.getInteger("screept.logsearch.workers", Math.max(1, Math.min(4, Runtime.getRuntime().availableProcessors() - 1)));
	}

	/** Runs on the calling thread (which reads) plus worker threads; returns when everything is done or cancelled */
	static void run(File file, Supplier<Task> tasks, Committer committer, AtomicBoolean cancelled) throws IOException {
		run(file, 0L, tasks, committer, cancelled);
	}

	/** Same, but starting at a byte offset that is the beginning of a line: only what a file gained is scanned */
	static void run(File file, long startOffset, Supplier<Task> tasks, Committer committer, AtomicBoolean cancelled) throws IOException {
		final int workers = defaultWorkers();
		final BlockingQueue<byte[]> free = new ArrayBlockingQueue<byte[]>(workers + 3);
		for (int i = 0; i < workers + 3; i++) {
			free.add(new byte[CHUNK]);
		}
		final BlockingQueue<Chunk> full = new LinkedBlockingQueue<Chunk>();
		final Object commitLock = new Object();
		final TreeMap<Long, Object[]> pending = new TreeMap<Long, Object[]>();
		final long[] next = { 0 };
		final AtomicBoolean failed = new AtomicBoolean();

		Thread[] threads = new Thread[workers];
		for (int w = 0; w < workers; w++) {
			final Task task = tasks.get();
			threads[w] = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						while (true) {
							Chunk chunk = full.take();
							if (chunk == END) {
								return;
							}
							if (cancelled.get() || failed.get()) {
								recycle(free, chunk.data);
								continue;
							}
							try {
								Object partial = task.process(chunk.data, chunk.length, chunk.offset, chunk.unterminated);
								recycle(free, chunk.data);
								synchronized (commitLock) {
									pending.put(chunk.sequence, new Object[] { partial, chunk.offset + chunk.length });
									Object[] ready;
									while ((ready = pending.remove(next[0])) != null) {
										committer.commit(ready[0], (Long) ready[1]);
										next[0]++;
									}
								}
							} catch (RuntimeException | Error t) {
								failed.set(true); // keep draining so the reader never blocks
								recycle(free, chunk.data);
							}
						}
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}
			}, "LogSearch-worker-" + w);
			threads[w].setDaemon(true);
			threads[w].start();
		}

		try {
			read(file, startOffset, free, full, cancelled, failed);
		} finally {
			for (int w = 0; w < workers; w++) {
				full.add(END);
			}
			for (Thread t : threads) {
				try {
					t.join();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		}
	}

	private static void recycle(BlockingQueue<byte[]> free, byte[] data) {
		free.offer(data.length == CHUNK ? data : new byte[CHUNK]);
	}

	private static void read(File file, long startOffset, BlockingQueue<byte[]> free, BlockingQueue<Chunk> full, AtomicBoolean cancelled, AtomicBoolean failed)
			throws IOException {
		try (InputStream in = new FileInputStream(file)) {
			byte[] current = take(free);
			int have = 0;
			long skipped = 0;
			while (skipped < startOffset) {
				long step = in.skip(startOffset - skipped);
				if (step <= 0) {
					break;
				}
				skipped += step;
			}
			long offset = skipped;
			long sequence = 0;
			while (current != null && !cancelled.get() && !failed.get()) {
				if (have == current.length) {
					int cut = lastLineEnd(current, have);
					if (cut < 0) { // a line longer than the buffer: keep growing
						current = Arrays.copyOf(current, current.length * 2);
						continue;
					}
					full.add(new Chunk(current, cut, offset, sequence++, false));
					byte[] following = take(free);
					if (following == null) {
						return;
					}
					int rest = have - cut;
					if (rest > following.length) {
						following = new byte[Math.max(rest * 2, CHUNK)];
					}
					System.arraycopy(current, cut, following, 0, rest);
					offset += cut;
					have = rest;
					current = following;
					continue;
				}
				int read = in.read(current, have, current.length - have);
				if (read < 0) {
					if (have > 0) {
						full.add(new Chunk(current, have, offset, sequence++, current[have - 1] != '\n'));
					}
					return;
				}
				have += read;
			}
		}
	}

	private static byte[] take(BlockingQueue<byte[]> free) throws IOException {
		try {
			return free.take();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return null;
		}
	}

	/** Index after the last line end, or -1 */
	private static int lastLineEnd(byte[] data, int length) {
		for (int i = length - 1; i >= 0; i--) {
			if (data[i] == '\n') {
				return i + 1;
			}
		}
		return -1;
	}
}

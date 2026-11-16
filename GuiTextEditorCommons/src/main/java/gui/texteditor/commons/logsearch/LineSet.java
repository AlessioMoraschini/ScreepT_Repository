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

import java.util.Arrays;
import java.util.function.IntConsumer;

/**
 * Ascending set of line numbers kept as a bitmap (1 bit per line plus a running count every 64 lines): memory depends on the
 * file size (about 1.5 bytes per 8 lines), never on how many lines match. Lines must be added in ascending order; safe to
 * read while another thread appends.
 */
final class LineSet {

	private long[] words = new long[256];
	/** Elements stored before each word */
	private int[] before = new int[256];
	private int wordCount;
	private int size;

	synchronized void add(int line) {
		int w = line >>> 6;
		if (w >= words.length) {
			int capacity = Math.max(words.length * 2, w + 1);
			words = Arrays.copyOf(words, capacity);
			before = Arrays.copyOf(before, capacity);
		}
		for (int k = wordCount; k <= w; k++) {
			before[k] = size;
		}
		wordCount = Math.max(wordCount, w + 1);
		long bit = 1L << (line & 63);
		if ((words[w] & bit) == 0) {
			words[w] |= bit;
			size++;
		}
	}

	/** Removes the elements >= line (the matches of a part of the file that is scanned again) */
	synchronized void truncate(int line) {
		if (line <= 0) {
			clear();
			return;
		}
		int w = line >>> 6;
		if (w >= wordCount) {
			return;
		}
		words[w] &= (1L << (line & 63)) - 1;
		for (int k = w + 1; k < wordCount; k++) {
			words[k] = 0;
		}
		wordCount = w + 1;
		size = before[w] + Long.bitCount(words[w]);
	}

	synchronized int size() {
		return size;
	}

	synchronized void clear() {
		words = new long[256];
		before = new int[256];
		wordCount = 0;
		size = 0;
	}

	synchronized boolean contains(int line) {
		int w = line >>> 6;
		return line >= 0 && w < wordCount && (words[w] & (1L << (line & 63))) != 0;
	}

	/** Number of elements lower than the value, i.e. the position of the first element >= value (size when there is none) */
	synchronized int lowerBound(int line) {
		if (line <= 0) {
			return 0;
		}
		int w = line >>> 6;
		if (w >= wordCount) {
			return size;
		}
		return before[w] + Long.bitCount(words[w] & ((1L << (line & 63)) - 1));
	}

	/** The element at the given position (0-based) */
	synchronized int get(int index) {
		if (index < 0 || index >= size) {
			throw new IndexOutOfBoundsException(String.valueOf(index));
		}
		int low = 0;
		int high = wordCount - 1;
		while (low < high) { // last word whose running count is <= index
			int mid = (low + high + 1) >>> 1;
			if (before[mid] <= index) {
				low = mid;
			} else {
				high = mid - 1;
			}
		}
		long word = words[low];
		for (int skip = index - before[low]; skip > 0; skip--) {
			word &= word - 1;
		}
		return (low << 6) + Long.numberOfTrailingZeros(word);
	}

	/** Visits the elements in order (works on a snapshot of the words, so the caller never holds the lock) */
	void forEach(IntConsumer consumer) {
		long[] copy;
		int count;
		synchronized (this) {
			count = wordCount;
			copy = Arrays.copyOf(words, count);
		}
		for (int w = 0; w < count; w++) {
			long word = copy[w];
			while (word != 0) {
				consumer.accept((w << 6) + Long.numberOfTrailingZeros(word));
				word &= word - 1;
			}
		}
	}
}

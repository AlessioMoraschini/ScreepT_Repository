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

/** Growable list of ints, safe to read while another thread appends (used for the lines that match a search) */
final class IntList {

	private int[] data = new int[1024];
	private int size;

	synchronized void add(int value) {
		if (size == data.length) {
			data = Arrays.copyOf(data, size * 2);
		}
		data[size++] = value;
	}

	synchronized int get(int index) {
		return data[index];
	}

	synchronized int size() {
		return size;
	}

	/** Lines are appended in ascending order, so the list is sorted */
	synchronized boolean contains(int value) {
		return Arrays.binarySearch(data, 0, size, value) >= 0;
	}

	/** Position of the first element >= value (size when there is none) */
	synchronized int lowerBound(int value) {
		int low = 0;
		int high = size;
		while (low < high) {
			int mid = (low + high) >>> 1;
			if (data[mid] < value) {
				low = mid + 1;
			} else {
				high = mid;
			}
		}
		return low;
	}

	synchronized int[] snapshot() {
		return Arrays.copyOf(data, size);
	}
}

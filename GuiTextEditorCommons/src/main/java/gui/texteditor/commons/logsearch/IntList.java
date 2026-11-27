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

	synchronized void clear() {
		size = 0;
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

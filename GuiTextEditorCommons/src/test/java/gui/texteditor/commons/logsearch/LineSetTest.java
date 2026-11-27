/**
 *
 * =========================================================================================
 *  Copyright (C) 2019-2020
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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;

import org.junit.Test;

/** LineSet (bitmap of matching lines) and IntList against a TreeSet / ArrayList reference */
public class LineSetTest {

	private static TreeSet<Integer> fill(LineSet set, long seed, int max, int count) {
		Random random = new Random(seed);
		TreeSet<Integer> reference = new TreeSet<Integer>();
		while (reference.size() < count) {
			reference.add(random.nextInt(max));
		}
		for (int line : reference) {
			set.add(line);
		}
		return reference;
	}

	private static void assertSame(TreeSet<Integer> reference, LineSet set, int probeMax) {
		assertEquals(reference.size(), set.size());
		int index = 0;
		for (int line : reference) {
			assertEquals(line, set.get(index));
			assertTrue(set.contains(line));
			assertEquals(index, set.lowerBound(line));
			index++;
		}
		for (int probe = -2; probe < probeMax; probe += 3) {
			assertEquals("lowerBound " + probe, reference.headSet(probe).size(), set.lowerBound(probe));
			assertEquals("contains " + probe, reference.contains(probe), set.contains(probe));
		}
		final List<Integer> visited = new ArrayList<Integer>();
		set.forEach(visited::add);
		assertEquals(new ArrayList<Integer>(reference), visited);
	}

	@Test
	public void emptySet() {
		LineSet set = new LineSet();
		assertEquals(0, set.size());
		assertFalse(set.contains(0));
		assertEquals(0, set.lowerBound(10));
		set.forEach(l -> fail("empty"));
		try {
			set.get(0);
			fail();
		} catch (IndexOutOfBoundsException expected) {
			// ok
		}
	}

	@Test
	public void sparseAndDenseSetsBehaveLikeATreeSet() {
		LineSet sparse = new LineSet();
		assertSame(fill(sparse, 1, 1_000_000, 500), sparse, 1_000_100);
		LineSet dense = new LineSet();
		assertSame(fill(dense, 2, 5000, 3000), dense, 5100);
	}

	@Test
	public void wordBoundariesAndLargeNumbers() {
		LineSet set = new LineSet();
		TreeSet<Integer> reference = new TreeSet<Integer>();
		for (int line : new int[] { 0, 1, 62, 63, 64, 65, 127, 128, 129, 1_000_000, 3_000_000 }) {
			set.add(line);
			reference.add(line);
		}
		assertEquals(reference.size(), set.size());
		int i = 0;
		for (int line : reference) {
			assertEquals(line, set.get(i++));
		}
		assertTrue(set.contains(3_000_000));
		assertFalse(set.contains(3_000_001));
		assertEquals(10, set.lowerBound(3_000_000));
	}

	@Test
	public void addingTheSameLineTwiceCountsOnce() {
		LineSet set = new LineSet();
		set.add(5);
		set.add(5);
		assertEquals(1, set.size());
	}

	@Test
	public void addAllOffsetsTheLines() {
		LineSet set = new LineSet();
		set.addAll(100, new int[] { 0, 5, 7, 0, 0 }, 3);
		assertEquals(3, set.size());
		assertEquals(100, set.get(0));
		assertEquals(105, set.get(1));
		assertEquals(107, set.get(2));
		set.addAll(200, new int[0], 0);
		assertEquals(3, set.size());
	}

	@Test
	public void truncateRemovesTheTailOnly() {
		for (int cut : new int[] { 0, 1, 63, 64, 65, 500, 4999, 5000, 9999 }) {
			LineSet set = new LineSet();
			TreeSet<Integer> reference = fill(set, 3, 5000, 1500);
			set.truncate(cut);
			TreeSet<Integer> expected = new TreeSet<Integer>(reference.headSet(Math.max(cut, 0)));
			assertSame(expected, set, 5100);
			// the set accepts new lines after the cut (a part of the file scanned again)
			int next = Math.max(cut, 0) + 3;
			set.add(next);
			expected.add(next);
			assertSame(expected, set, 5100);
		}
	}

	@Test
	public void clearEmptiesTheSet() {
		LineSet set = new LineSet();
		fill(set, 4, 1000, 100);
		set.clear();
		assertEquals(0, set.size());
		set.add(3);
		assertEquals(1, set.size());
		assertEquals(3, set.get(0));
	}

	@Test
	public void readingWhileAnotherThreadAppendsNeverSeesGarbage() throws Exception {
		final LineSet set = new LineSet();
		Thread writer = new Thread(() -> {
			for (int i = 0; i < 100_000; i++) {
				set.add(i * 3);
			}
		});
		writer.start();
		while (writer.isAlive()) {
			int size = set.size();
			if (size > 0) {
				assertEquals((size - 1) * 3, set.get(size - 1));
			}
		}
		writer.join();
		assertEquals(100_000, set.size());
	}

	@Test
	public void intListBasics() {
		IntList list = new IntList();
		assertEquals(0, list.size());
		assertFalse(list.contains(0));
		assertEquals(0, list.lowerBound(5));
		for (int i = 0; i < 5000; i++) { // grows over the initial capacity
			list.add(i * 2);
		}
		assertEquals(5000, list.size());
		assertEquals(40, list.get(20));
		assertTrue(list.contains(9998));
		assertFalse(list.contains(9999));
		assertEquals(0, list.lowerBound(-5));
		assertEquals(10, list.lowerBound(20));
		assertEquals(11, list.lowerBound(21));
		assertEquals(5000, list.lowerBound(1_000_000));
		int[] snapshot = list.snapshot();
		assertEquals(5000, snapshot.length);
		assertEquals(9998, snapshot[4999]);
		list.clear();
		assertEquals(0, list.size());
		assertArrayEquals(new int[0], list.snapshot());
		assertFalse(list.contains(0));
	}
}

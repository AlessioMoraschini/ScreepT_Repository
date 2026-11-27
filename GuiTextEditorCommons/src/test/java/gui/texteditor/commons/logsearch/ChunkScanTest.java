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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** The chunked reader: every line once, in order, whatever the line layout, and cancel */
public class ChunkScanTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	@BeforeClass
	public static void tinyChunks() {
		LogFixtures.init();
	}

	/** Lines seen by the tasks (each piece: its text), concatenated by the committer in the order of the commits */
	private List<String> scan(File file, long startOffset, AtomicBoolean cancelled, List<Long> ends, List<Boolean> unterminatedFlags) throws IOException {
		final List<String> pieces = new ArrayList<String>();
		ChunkScan.run(file, startOffset, () -> (data, length, offset, unterminated) -> {
			synchronized (unterminatedFlags) {
				unterminatedFlags.add(unterminated);
			}
			return new String(data, 0, length, StandardCharsets.UTF_8);
		}, (partial, end) -> {
			pieces.add((String) partial);
			ends.add(end);
		}, cancelled);
		return pieces;
	}

	private static String join(List<String> pieces) {
		StringBuilder sb = new StringBuilder();
		for (String piece : pieces) {
			sb.append(piece);
		}
		return sb.toString();
	}

	@Test
	public void piecesRebuildTheFileInOrderAndEndOnLineEnds() throws IOException {
		List<String> lines = LogFixtures.randomLog(8L, 3000, 3);
		lines.add(3, "x" + new String(new char[5000]).replace('\0', 'y')); // a line longer than a piece
		byte[] data = LogFixtures.join("\n", true, lines);
		File file = LogFixtures.write(tmp.getRoot(), "a.log", data);
		List<Long> ends = new ArrayList<Long>();
		List<String> pieces = scan(file, 0, new AtomicBoolean(), ends, new ArrayList<Boolean>());
		assertEquals(new String(data, StandardCharsets.UTF_8), join(pieces));
		for (String piece : pieces) {
			assertTrue("piece ends on a line end", piece.endsWith("\n"));
		}
		long last = 0;
		for (long end : ends) {
			assertTrue(end > last);
			last = end;
		}
		assertEquals(data.length, last);
	}

	@Test
	public void unterminatedLastLineIsFlagged() throws IOException {
		byte[] data = LogFixtures.utf8("a\nb\nno newline at the end");
		File file = LogFixtures.write(tmp.getRoot(), "b.log", data);
		List<Boolean> flags = new ArrayList<Boolean>();
		List<String> pieces = scan(file, 0, new AtomicBoolean(), new ArrayList<Long>(), flags);
		assertEquals(new String(data, StandardCharsets.UTF_8), join(pieces));
		assertTrue(flags.contains(Boolean.TRUE));
	}

	@Test
	public void startOffsetSkipsTheBeginning() throws IOException {
		byte[] data = LogFixtures.utf8("first\nsecond\nthird\n");
		File file = LogFixtures.write(tmp.getRoot(), "c.log", data);
		List<String> pieces = scan(file, 6, new AtomicBoolean(), new ArrayList<Long>(), new ArrayList<Boolean>());
		assertEquals("second\nthird\n", join(pieces));
	}

	@Test
	public void startOffsetAtOrPastTheEndDoesNothing() throws IOException {
		byte[] data = LogFixtures.utf8("abc\n");
		File file = LogFixtures.write(tmp.getRoot(), "d.log", data);
		assertTrue(scan(file, data.length, new AtomicBoolean(), new ArrayList<Long>(), new ArrayList<Boolean>()).isEmpty());
		assertTrue(scan(file, data.length + 100, new AtomicBoolean(), new ArrayList<Long>(), new ArrayList<Boolean>()).isEmpty());
		File empty = LogFixtures.write(tmp.getRoot(), "e.log", new byte[0]);
		assertTrue(scan(empty, 0, new AtomicBoolean(), new ArrayList<Long>(), new ArrayList<Boolean>()).isEmpty());
	}

	@Test
	public void cancelledBeforeStartCommitsNothing() throws IOException {
		File file = LogFixtures.write(tmp.getRoot(), "f.log", LogFixtures.utf8("a\nb\nc\n"));
		assertTrue(scan(file, 0, new AtomicBoolean(true), new ArrayList<Long>(), new ArrayList<Boolean>()).isEmpty());
	}

	@Test
	public void manyEmptyLinesAndBlankFileAreCounted() throws IOException {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 5000; i++) {
			sb.append('\n');
		}
		File file = LogFixtures.write(tmp.getRoot(), "g.log", LogFixtures.utf8(sb.toString()));
		assertEquals(sb.toString(), join(scan(file, 0, new AtomicBoolean(), new ArrayList<Long>(), new ArrayList<Boolean>())));
	}

	@Test
	public void workersAreAtLeastOne() {
		assertTrue(ChunkScan.defaultWorkers() >= 1);
	}
}

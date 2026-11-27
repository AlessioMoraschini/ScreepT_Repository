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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

/** Speed tiers, summaries and formatting of the search statistics (pure functions) */
public class SearchStatsTest {

	private static final long MB = 1024L * 1024L;

	private static SearchStats.Sample sample(long bytes, long nanos, int kind) {
		return new SearchStats.Sample(0, nanos, bytes, 100, 5, SearchStats.MODE_BYTES, 2, kind);
	}

	@Test
	public void tiersFollowTheThresholds() {
		assertEquals(SearchStats.TIER_NONE, SearchStats.tierOf(-1));
		assertEquals(SearchStats.TIER_SLOW, SearchStats.tierOf(0));
		assertEquals(SearchStats.TIER_SLOW, SearchStats.tierOf(SearchStats.FAIR - 1));
		assertEquals(SearchStats.TIER_FAIR, SearchStats.tierOf(SearchStats.FAIR));
		assertEquals(SearchStats.TIER_GOOD, SearchStats.tierOf(SearchStats.GOOD));
		assertEquals(SearchStats.TIER_EXCELLENT, SearchStats.tierOf(SearchStats.EXCELLENT));
		assertEquals(SearchStats.TIER_EXCELLENT, SearchStats.tierOf(1e12));
	}

	@Test
	public void speedNeedsEnoughBytesAndTime() {
		assertEquals(-1, sample(MB - 1, 1_000_000_000L, 0).speed(), 0);
		assertEquals(-1, sample(10 * MB, 999_999L, 0).speed(), 0);
		assertEquals(10 * MB, sample(10 * MB, 1_000_000_000L, 0).speed(), 1);
		assertEquals(SearchStats.TIER_NONE, sample(1, 1, 0).tier());
		assertEquals(1d, sample(10 * MB, 1_000_000_000L, 0).seconds(), 1e-9);
	}

	@Test
	public void negativeInputsAreClamped() {
		SearchStats.Sample s = new SearchStats.Sample(0, -5, -5, -5, -5, 0, 1, -5);
		assertEquals(0, s.nanos);
		assertEquals(0, s.bytes);
		assertEquals(0, s.lines);
		assertEquals(0, s.matches);
		assertEquals(0, s.kind);
	}

	@Test
	public void summaryStatistics() {
		List<SearchStats.Sample> samples = Arrays.asList(
				sample(100 * MB, 1_000_000_000L, 0), // 100 MB/s
				sample(300 * MB, 1_000_000_000L, 0), // 300 MB/s
				sample(200 * MB, 1_000_000_000L, 0), // 200 MB/s
				sample(10, 10, 0)); // too short for a speed
		SearchStats.Summary summary = SearchStats.summarize(samples);
		assertEquals(4, summary.count);
		assertEquals(3, summary.measured);
		assertEquals(200 * MB, summary.average, 1);
		assertEquals(200 * MB, summary.median, 1);
		assertEquals(100 * MB, summary.worst, 1);
		assertEquals(300 * MB, summary.best, 1);
		assertEquals(1d, summary.averageSeconds, 1e-9);
		assertEquals(600 * MB + 10, summary.totalBytes);
	}

	@Test
	public void medianOfAnEvenNumberOfSamplesIsTheMiddlePair() {
		SearchStats.Summary summary = SearchStats.summarize(Arrays.asList(sample(100 * MB, 1_000_000_000L, 0), sample(300 * MB, 1_000_000_000L, 0)));
		assertEquals(200 * MB, summary.median, 1);
	}

	@Test
	public void summaryOfNothing() {
		assertEquals(0, SearchStats.summarize(null).count);
		SearchStats.Summary summary = SearchStats.summarize(new ArrayList<SearchStats.Sample>());
		assertEquals(0, summary.count);
		assertEquals(-1, summary.average, 0);
		assertEquals(-1, summary.median, 0);
	}

	@Test
	public void kindsAndFilter() {
		List<SearchStats.Sample> samples = Arrays.asList(sample(1, 1, 2), sample(1, 1, 0), sample(1, 1, 2), sample(1, 1, 5));
		assertEquals(Arrays.asList(0, 2, 5), SearchStats.kindsPresent(samples));
		assertEquals(2, SearchStats.filter(samples, 2).size());
		assertEquals(4, SearchStats.filter(samples, SearchStats.KIND_ALL).size());
		assertEquals(0, SearchStats.filter(samples, 7).size());
	}

	@Test
	public void kindNamesInEnglish() {
		assertEquals("All", SearchStats.kindName(SearchStats.KIND_ALL, true));
		assertEquals("Text", SearchStats.kindName(SearchStats.KIND_TEXT, true));
		assertEquals("Several terms", SearchStats.kindName(SearchStats.KIND_MULTI, true));
		assertEquals("Regex", SearchStats.kindName(SearchStats.KIND_REGEX, true));
		assertTrue(SearchStats.kindName(SearchStats.KIND_REGEX | SearchStats.KIND_CASE | SearchStats.KIND_WORD, true).startsWith("Regex"));
		assertTrue(SearchStats.kindName(SearchStats.KIND_TEXT | SearchStats.KIND_CASE, true).contains("Match case"));
		assertTrue(SearchStats.kindName(SearchStats.KIND_TEXT | SearchStats.KIND_WORD, true).contains("Whole word"));
	}

	@Test
	public void durationFormatting() {
		assertEquals("-", SearchStats.formatDuration(-1));
		assertEquals("-", SearchStats.formatDuration(Double.NaN));
		assertEquals("420 ms", SearchStats.formatDuration(0.42));
		assertEquals("0 ms", SearchStats.formatDuration(0));
		assertEquals("1.23 s", SearchStats.formatDuration(1.234));
		assertEquals("2 min 05 s", SearchStats.formatDuration(125));
		assertEquals("1 min 00 s", SearchStats.formatDuration(60));
	}

	@Test
	public void recordKeepsTheLastSamplesWithinTheCapacity() throws InterruptedException {
		Thread.sleep(150); // a search of an earlier test may still be recording its sample
		SearchStats.reset();
		try {
			SearchStats.record(null);
			assertEquals(0, SearchStats.snapshot().size());
			for (int i = 0; i < SearchStats.CAPACITY + 20; i++) {
				SearchStats.record(new SearchStats.Sample(i, 1, 1, 1, 0, 0, 1, 0));
			}
			List<SearchStats.Sample> all = SearchStats.snapshot();
			assertEquals(SearchStats.CAPACITY, all.size());
			assertEquals(20, all.get(0).time);
			assertEquals(SearchStats.CAPACITY + 19, SearchStats.last().time);
		} finally {
			SearchStats.reset();
		}
	}
}

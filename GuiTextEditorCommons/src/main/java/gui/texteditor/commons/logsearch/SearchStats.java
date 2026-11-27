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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.SwingUtilities;

/**
 * Speed of the last searches (up to {@value #CAPACITY}, the oldest drop out), shared by all the LogSearch windows of the
 * session. Only numbers are kept: never the searched text nor the file path, so what is shown and exported can be shared.
 * Every method is safe to call from any thread and never throws.
 */
final class SearchStats {

	static final int CAPACITY = 100;

	/** Under this size (bytes) a search is too short to say anything about the speed */
	static final long MIN_BYTES_FOR_SPEED = 1L << 20;

	/** Speed tiers (bytes per second): from this speed up. One place for the icon, the chip and the report */
	static final double EXCELLENT = 500d * 1024 * 1024;
	static final double GOOD = 150d * 1024 * 1024;
	static final double FAIR = 40d * 1024 * 1024;

	static final int TIER_NONE = -1, TIER_SLOW = 0, TIER_FAIR = 1, TIER_GOOD = 2, TIER_EXCELLENT = 3;

	/** How the lines were tested (more or less work per line) */
	static final int MODE_BYTES = 0, MODE_PREFILTER = 1, MODE_DECODE = 2;

	/** Kind of search: what was typed (bits 0-1) plus the options of the search bar */
	static final int KIND_TEXT = 0, KIND_MULTI = 1, KIND_REGEX = 2, KIND_BASE_MASK = 3, KIND_CASE = 4, KIND_WORD = 8;
	/** Filter value meaning every kind */
	static final int KIND_ALL = -1;

	/** One finished search */
	static final class Sample {
		final long time;
		final long nanos;
		final long bytes;
		final long lines;
		final int matches;
		final int mode;
		final int workers;
		final int kind;

		Sample(long time, long nanos, long bytes, long lines, int matches, int mode, int workers, int kind) {
			this.time = time;
			this.nanos = Math.max(0, nanos);
			this.bytes = Math.max(0, bytes);
			this.lines = Math.max(0, lines);
			this.matches = Math.max(0, matches);
			this.mode = mode;
			this.workers = workers;
			this.kind = Math.max(0, kind);
		}

		/** Bytes per second, or -1 when the search was too short (or too fast to be measured) to tell */
		double speed() {
			if (bytes < MIN_BYTES_FOR_SPEED || nanos < 1_000_000L) {
				return -1;
			}
			double speed = bytes / (nanos / 1e9);
			return Double.isNaN(speed) || Double.isInfinite(speed) ? -1 : speed;
		}

		double seconds() {
			return nanos / 1e9;
		}

		int tier() {
			return tierOf(speed());
		}
	}

	/** Aggregates over the samples that have a speed */
	static final class Summary {
		int count;
		int measured;
		double average = -1;
		/** Average duration (seconds) of the searches that have a speed: the time that goes with the average speed; -1 if none */
		double averageSeconds = -1;
		double median = -1;
		double best = -1;
		double worst = -1;
		long totalBytes;
		double totalSeconds;
	}

	private static final ArrayDeque<Sample> SAMPLES = new ArrayDeque<Sample>(CAPACITY + 1);
	private static final List<Runnable> LISTENERS = new CopyOnWriteArrayList<Runnable>();

	private SearchStats() {
	}

	static int tierOf(double speed) {
		if (speed < 0) {
			return TIER_NONE;
		}
		return speed >= EXCELLENT ? TIER_EXCELLENT : speed >= GOOD ? TIER_GOOD : speed >= FAIR ? TIER_FAIR : TIER_SLOW;
	}

	static void record(Sample sample) {
		if (sample == null) {
			return;
		}
		synchronized (SAMPLES) {
			SAMPLES.addLast(sample);
			while (SAMPLES.size() > CAPACITY) {
				SAMPLES.removeFirst();
			}
		}
		fire();
	}

	/** Copy of the samples, the oldest first */
	static List<Sample> snapshot() {
		synchronized (SAMPLES) {
			return new ArrayList<Sample>(SAMPLES);
		}
	}

	static Sample last() {
		synchronized (SAMPLES) {
			return SAMPLES.peekLast();
		}
	}

	static void reset() {
		synchronized (SAMPLES) {
			SAMPLES.clear();
		}
		fire();
	}

	/** The listener runs on the event dispatch thread every time the samples change */
	static void addListener(Runnable listener) {
		if (listener != null) {
			LISTENERS.add(listener);
		}
	}

	static void removeListener(Runnable listener) {
		LISTENERS.remove(listener);
	}

	private static void fire() {
		for (final Runnable listener : LISTENERS) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					try {
						listener.run();
					} catch (Throwable e) {
						// a broken listener must not stop the others nor the search
					}
				}
			});
		}
	}

	/** Kinds that appear in the samples, in a stable order */
	static List<Integer> kindsPresent(List<Sample> samples) {
		java.util.TreeSet<Integer> kinds = new java.util.TreeSet<Integer>();
		for (Sample sample : samples) {
			kinds.add(sample.kind);
		}
		return new ArrayList<Integer>(kinds);
	}

	/** The samples of one kind ({@link #KIND_ALL}: all of them) */
	static List<Sample> filter(List<Sample> samples, int kind) {
		if (kind == KIND_ALL) {
			return samples;
		}
		List<Sample> out = new ArrayList<Sample>();
		for (Sample sample : samples) {
			if (sample.kind == kind) {
				out.add(sample);
			}
		}
		return out;
	}

	/** "Text", "Regex - Match case", "Several terms - Whole word"... (English for the report, translated for the window) */
	static String kindName(int kind, boolean english) {
		if (kind == KIND_ALL) {
			return english ? "All" : various.common.light.i18n.I18n.t("log.perf_all");
		}
		String[] ids = { "log.perf_kind_text", "log.perf_kind_multi", "log.perf_kind_regex" };
		String[] en = { "Text", "Several terms", "Regex" };
		int base = Math.min(2, kind & KIND_BASE_MASK);
		StringBuilder name = new StringBuilder(english ? en[base] : various.common.light.i18n.I18n.t(ids[base]));
		if ((kind & KIND_CASE) != 0) {
			name.append(" · ").append(english ? "Match case" : various.common.light.i18n.I18n.t("log.perf_kind_case"));
		}
		if ((kind & KIND_WORD) != 0) {
			name.append(" · ").append(english ? "Whole word" : various.common.light.i18n.I18n.t("log.perf_kind_word"));
		}
		return name.toString();
	}

	static Summary summarize(List<Sample> samples) {
		Summary summary = new Summary();
		if (samples == null) {
			return summary;
		}
		summary.count = samples.size();
		List<Double> speeds = new ArrayList<Double>();
		double measuredSeconds = 0;
		for (Sample sample : samples) {
			summary.totalBytes += sample.bytes;
			summary.totalSeconds += sample.seconds();
			double speed = sample.speed();
			if (speed >= 0) {
				speeds.add(speed);
				measuredSeconds += sample.seconds();
			}
		}
		summary.measured = speeds.size();
		if (!speeds.isEmpty()) {
			double[] sorted = new double[speeds.size()];
			double sum = 0;
			for (int i = 0; i < sorted.length; i++) {
				sorted[i] = speeds.get(i);
				sum += sorted[i];
			}
			Arrays.sort(sorted);
			summary.average = sum / sorted.length;
			summary.averageSeconds = measuredSeconds / sorted.length;
			summary.worst = sorted[0];
			summary.best = sorted[sorted.length - 1];
			summary.median = sorted.length % 2 == 1 ? sorted[sorted.length / 2] : (sorted[sorted.length / 2 - 1] + sorted[sorted.length / 2]) / 2;
		}
		return summary;
	}

	// ------------------------------------------------------------------ formatting (shared by the chip, the dialog and the report)

	/** "1.8 GB/s", or "-" when there is no speed */
	static String formatSpeed(double speed) {
		return speed < 0 ? "-" : gui.commons.frameutils.frame.arch.ToolStyle.size(Math.round(speed)) + "/s";
	}

	/** "420 ms", "1.23 s", "2 min 05 s" */
	static String formatDuration(double seconds) {
		if (Double.isNaN(seconds) || seconds < 0) {
			return "-";
		}
		if (seconds < 1) {
			return Math.round(seconds * 1000) + " ms";
		}
		if (seconds < 60) {
			return String.format(java.util.Locale.ROOT, "%.2f s", seconds);
		}
		long total = Math.round(seconds);
		return (total / 60) + " min " + String.format(java.util.Locale.ROOT, "%02d", total % 60) + " s";
	}
}

/**
 *
 * =========================================================================================
 *  Copyright (C) 2019-2020
 *
 *  AM-Design-Development - (Alessio Moraschini) - All Rights Reserved
 * =========================================================================================
 *
 * You should have received a copy of the license with this file.
 * If not, please write to: info@am-design-development.com, or visit : https://www.am-design-development.com
 */
package gui.texteditor.commons.logsearch;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import gui.commons.frameutils.frame.arch.ToolStyle;

/**
 * What the computer looks like right now, for the performance window and report. Uses only what the JDK offers:
 * every value is read on its own and any failure (a JVM without the extended management beans, a missing
 * counter, a read-only file system...) shows "n/a" for that value only. Never throws, never includes user names or paths.
 */
final class SystemSnapshot {

	static final String NOT_AVAILABLE = "n/a";

	/** Labels of the rows of {@link #collect}, in order (to show the rows before the values are known) */
	static final String[] LABELS = { "System", "CPU cores", "Search workers", "CPU load", "Memory", "Java", "App memory", "Log disk" };

	private interface Probe {
		String get() throws Throwable;
	}

	private SystemSnapshot() {
	}

	/** Label and value pairs; {@code logFile} (can be null) only tells which disk to describe, its name is never reported */
	static List<String[]> collect(final File logFile) {
		final List<String[]> rows = new ArrayList<String[]>();
		add(rows, "System", new Probe() {
			public String get() {
				return System.getProperty("os.name") + " " + System.getProperty("os.version") + " (" + System.getProperty("os.arch") + ")";
			}
		});
		add(rows, "CPU cores", new Probe() {
			public String get() {
				return String.valueOf(Runtime.getRuntime().availableProcessors());
			}
		});
		add(rows, "Search workers", new Probe() {
			public String get() {
				return String.valueOf(ChunkScan.defaultWorkers());
			}
		});
		add(rows, "CPU load", new Probe() {
			public String get() {
				return Ext.cpuLoad();
			}
		});
		add(rows, "Memory", new Probe() {
			public String get() {
				return Ext.memory();
			}
		});
		add(rows, "Java", new Probe() {
			public String get() {
				String vm = String.valueOf(System.getProperty("java.vm.name")).replace("Java HotSpot(TM) ", "HotSpot ").replace(" Server VM", "").replace(" Client VM", "");
								return System.getProperty("java.version") + " (" + vm + ")";
			}
		});
		add(rows, "App memory", new Probe() {
			public String get() {
				Runtime runtime = Runtime.getRuntime();
				long used = runtime.totalMemory() - runtime.freeMemory();
				return ToolStyle.size(used) + " used of " + ToolStyle.size(runtime.maxMemory()) + " allowed";
			}
		});
		add(rows, "Log disk", new Probe() {
			public String get() throws Throwable {
				if (logFile == null) {
					return null;
				}
				FileStore store = Files.getFileStore(logFile.toPath());
				return store.type() + ", " + ToolStyle.size(store.getUsableSpace()) + " free of " + ToolStyle.size(store.getTotalSpace());
			}
		});
		return rows;
	}

	private static void add(List<String[]> rows, String label, Probe probe) {
		String value = null;
		try {
			value = probe.get();
		} catch (Throwable e) {
			// not supported here: shown as not available
		}
		if (value == null || value.trim().isEmpty()) {
			value = NOT_AVAILABLE;
		}
		rows.add(new String[] { label, value });
	}

	/** The extended (com.sun) bean lives in its own class: if the JVM has none only these probes fail, not the whole snapshot */
	private static final class Ext {

		static String cpuLoad() {
			OperatingSystemMXBean bean = ManagementFactory.getOperatingSystemMXBean();
			double load = -1;
			if (bean instanceof com.sun.management.OperatingSystemMXBean) {
				load = ((com.sun.management.OperatingSystemMXBean) bean).getSystemCpuLoad();
			}
			if (load < 0 || Double.isNaN(load) || Double.isInfinite(load)) {
				load = bean.getSystemLoadAverage() >= 0 ? bean.getSystemLoadAverage() / Math.max(1, bean.getAvailableProcessors()) : -1;
			}
			if (load < 0 || Double.isNaN(load) || Double.isInfinite(load)) {
				return null;
			}
			return String.format(Locale.ROOT, "%d %%", Math.round(Math.min(1d, load) * 100));
		}

		static String memory() {
			OperatingSystemMXBean bean = ManagementFactory.getOperatingSystemMXBean();
			if (bean instanceof com.sun.management.OperatingSystemMXBean) {
				com.sun.management.OperatingSystemMXBean ext = (com.sun.management.OperatingSystemMXBean) bean;
				long total = ext.getTotalPhysicalMemorySize();
				long free = ext.getFreePhysicalMemorySize();
				if (total > 0 && free >= 0) {
					return ToolStyle.size(free) + " free of " + ToolStyle.size(total);
				}
			}
			return null;
		}
	}
}

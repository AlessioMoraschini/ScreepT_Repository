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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import gui.texteditor.commons.logsearch.SearchStats.Sample;

/** The shareable performance page: self contained, readable with no samples, and never carrying what was searched or where */
public class PerformanceReportTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private static List<Sample> samples(int n) {
		List<Sample> list = new ArrayList<Sample>();
		for (int i = 0; i < n; i++) {
			// 200 MB in 0.4 s, 5 GB in 20 s, a tiny one, one without lines
			list.add(new Sample(1_700_000_000_000L + i * 1000L, 400_000_000L * (i % 3 + 1), 200_000_000L * (i % 5 + 1), 1_000_000L * (i + 1), i * 3, i % 3, 1 + i % 8, i % 4));
		}
		return list;
	}

	private static List<String[]> system() {
		List<String[]> rows = new ArrayList<String[]>();
		rows.add(new String[] { "CPU", "8 cores <b>unsafe</b>" });
		rows.add(new String[] { "Memory", "16 GB" });
		return rows;
	}

	@Test
	public void pageIsSelfContainedAndEscaped() {
		String html = PerformanceReport.build(samples(12), system(), "Screept & <Co>");
		assertTrue(html.startsWith("<!DOCTYPE html>"));
		assertTrue(html.contains("LogSearch performance"));
		assertFalse("no scripts", html.toLowerCase().contains("<script"));
		assertFalse("no external resources", html.contains("http://") || html.contains("https://"));
		assertFalse("system values are escaped", html.contains("<b>unsafe</b>"));
		assertTrue(html.contains("&lt;b&gt;unsafe&lt;/b&gt;"));
		assertTrue(html.contains("Screept &amp; &lt;Co&gt;"));
		assertTrue(html.contains("</html>"));
	}

	@Test
	public void emptyAndSingleSampleDoNotBreak() {
		assertNotNull(PerformanceReport.build(new ArrayList<Sample>(), system(), null));
		String one = PerformanceReport.build(samples(1), new ArrayList<String[]>(), "");
		assertTrue(one.contains("1 search"));
		assertTrue(PerformanceReport.build(samples(100), system(), "x").contains("</html>"));
	}

	@Test
	public void tooShortSearchesHaveNoSpeedButStillListed() {
		List<Sample> list = new ArrayList<Sample>();
		list.add(new Sample(1L, 10L, 100L, 1L, 0, 0, 1, 0)); // too small and too fast to tell
		String html = PerformanceReport.build(list, system(), "ScreepT");
		assertTrue(html.contains("1 search"));
	}

	@Test
	public void writeCreatesTheFile() throws Exception {
		File out = new File(tmp.newFolder(), "report.html");
		PerformanceReport.write(out, samples(5), system(), "ScreepT");
		assertTrue(out.length() > 500);
		String content = new String(Files.readAllBytes(out.toPath()), StandardCharsets.UTF_8);
		assertTrue(content.contains("Average"));
		assertTrue(content.contains("Median"));
	}

	@Test
	public void systemSnapshotReportsRowsWithoutThePath() throws Exception {
		File log = new File(tmp.newFolder(), "secret-customer-name.log");
		Files.write(log.toPath(), "line\n".getBytes(StandardCharsets.UTF_8));
		List<String[]> rows = SystemSnapshot.collect(log);
		assertNotNull(rows);
		assertFalse(rows.isEmpty());
		for (String[] row : rows) {
			assertTrue(row.length >= 2);
			assertFalse("no file name in the shared data: " + row[0] + "=" + row[1], row[1].contains("secret-customer-name"));
		}
	}
}

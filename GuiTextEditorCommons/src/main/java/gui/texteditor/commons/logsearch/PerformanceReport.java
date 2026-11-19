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
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import gui.texteditor.commons.logsearch.SearchStats.Sample;
import gui.texteditor.commons.logsearch.SearchStats.Summary;

/**
 * One self-contained HTML page (inline style and SVG, no scripts, no external resources) with the speed of the last
 * searches and the computer they ran on, to share the results. It contains only timings, sizes and system data:
 * never the searched text, file names or paths. Always in English (it is meant to be read by other people).
 */
final class PerformanceReport {

	private static final String[] TIER_CLASS = { "slow", "fair", "good", "excellent" };
	private static final String[] TIER_NAME = { "Slow", "Fair", "Good", "Excellent" };
	private static final String[] MODE_NAME = { "Bytes", "Pre-filter", "Decode" };

	private PerformanceReport() {
	}

	static void write(File target, List<Sample> samples, List<String[]> system, String appName) throws IOException {
		Files.write(target.toPath(), build(samples, system, appName).getBytes(StandardCharsets.UTF_8));
	}

	static String build(List<Sample> samples, List<String[]> system, String appName) {
		Summary summary = SearchStats.summarize(samples);
		StringBuilder html = new StringBuilder(16384);
		html.append("<!DOCTYPE html>\n<html lang=\"en\"><head><meta charset=\"utf-8\">\n")
				.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n")
				.append("<title>LogSearch performance</title>\n<style>").append(css()).append("</style></head><body><main>\n");

		html.append("<header><h1>LogSearch performance</h1><p class=\"muted\">")
				.append(esc(appName == null || appName.isEmpty() ? "ScreepT" : appName)).append(" &middot; ")
				.append(esc(new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ROOT).format(new Date())))
				.append(" &middot; last ").append(summary.count).append(summary.count == 1 ? " search" : " searches").append("</p></header>\n");

		html.append("<section class=\"cards\">");
		card(html, "Average", SearchStats.formatSpeed(summary.average), SearchStats.tierOf(summary.average));
		card(html, "Median", SearchStats.formatSpeed(summary.median), SearchStats.tierOf(summary.median));
		card(html, "Best", SearchStats.formatSpeed(summary.best), SearchStats.tierOf(summary.best));
		card(html, "Worst", SearchStats.formatSpeed(summary.worst), SearchStats.tierOf(summary.worst));
		html.append("</section>\n");

		html.append("<section><h2>By type of search</h2><div class=\"scroll\"><table><thead><tr><th>Type</th><th>Searches</th><th>Average</th><th>Avg time</th>")
				.append("<th>Median</th><th>Best</th><th>Worst</th></tr></thead><tbody>\n");
		groupRow(html, SearchStats.KIND_ALL, samples);
		for (int kind : SearchStats.kindsPresent(samples)) {
			groupRow(html, kind, SearchStats.filter(samples, kind));
		}
		html.append("</tbody></table></div></section>\n");

		html.append("<section><h2>Speed of each search</h2>");
		if (summary.measured == 0) {
			html.append("<p class=\"muted\">No search was big enough (at least 1 MB) to measure a speed.</p>");
		} else {
			chart(html, samples, summary);
		}
		html.append("</section>\n");

		html.append("<section><h2>Searches</h2><div class=\"scroll\"><table><thead><tr><th>#</th><th>Time</th><th>Type</th><th>Duration</th><th>Scanned</th>")
				.append("<th>Speed</th><th>Lines</th><th>Matches</th><th>Method</th></tr></thead><tbody>\n");
		SimpleDateFormat clock = new SimpleDateFormat("HH:mm:ss", Locale.ROOT);
		for (int i = samples.size() - 1; i >= 0; i--) {
			Sample s = samples.get(i);
			int tier = s.tier();
			html.append("<tr><td>").append(i + 1).append("</td><td>").append(esc(clock.format(new Date(s.time)))).append("</td><td>")
					.append(esc(SearchStats.kindName(s.kind, true))).append("</td><td>").append(esc(SearchStats.formatDuration(s.seconds()))).append("</td><td>").append(esc(gui.commons.frameutils.frame.arch.ToolStyle.size(s.bytes)))
					.append("</td><td class=\"").append(tier >= 0 ? TIER_CLASS[tier] : "muted").append("\">").append(esc(SearchStats.formatSpeed(s.speed())))
					.append("</td><td>").append(String.format(Locale.ROOT, "%,d", s.lines)).append("</td><td>").append(String.format(Locale.ROOT, "%,d", s.matches))
					.append("</td><td>").append(esc(modeName(s.mode))).append("</td></tr>\n");
		}
		html.append("</tbody></table></div></section>\n");

		html.append("<section><h2>This computer</h2><table class=\"kv\"><tbody>");
		if (system != null) {
			for (String[] row : system) {
				if (row != null && row.length >= 2) {
					html.append("<tr><th>").append(esc(row[0])).append("</th><td>").append(esc(row[1])).append("</td></tr>");
				}
			}
		}
		html.append("</tbody></table></section>\n");

		html.append("<footer class=\"muted\">Speed = bytes of the file scanned per second, only for searches of at least 1 MB. ")
				.append("Tiers: excellent &ge; ").append(esc(SearchStats.formatSpeed(SearchStats.EXCELLENT))).append(", good &ge; ")
				.append(esc(SearchStats.formatSpeed(SearchStats.GOOD))).append(", fair &ge; ").append(esc(SearchStats.formatSpeed(SearchStats.FAIR)))
				.append(". This page contains only timings, sizes and system data: no search text, file names or paths.</footer>\n")
				.append("</main></body></html>\n");
		return html.toString();
	}

	private static void groupRow(StringBuilder html, int kind, List<Sample> samples) {
		Summary s = SearchStats.summarize(samples);
		html.append("<tr><td>").append(esc(SearchStats.kindName(kind, true))).append("</td><td>").append(s.count).append("</td>");
		double[] values = { s.average, -3, s.median, s.best, s.worst };
		for (double value : values) {
			if (value == -3) {
				html.append("<td>").append(esc(s.averageSeconds >= 0 ? SearchStats.formatDuration(s.averageSeconds) : "-")).append("</td>");
			} else {
				int tier = SearchStats.tierOf(value);
				html.append("<td class=\"").append(tier >= 0 ? TIER_CLASS[tier] : "muted").append("\">").append(esc(SearchStats.formatSpeed(value))).append("</td>");
			}
		}
		html.append("</tr>\n");
	}

	private static void card(StringBuilder html, String label, String value, int tier) {
		html.append("<div class=\"card\"><div class=\"label\">").append(esc(label)).append("</div><div class=\"value ")
				.append(tier >= 0 ? TIER_CLASS[tier] : "muted").append("\">").append(esc(value)).append("</div>")
				.append("<div class=\"tier muted\">").append(tier >= 0 ? TIER_NAME[tier] : "&nbsp;").append("</div></div>");
	}

	/** Inline SVG bars, one per search (oldest on the left), height = speed; a tooltip on every bar */
	private static void chart(StringBuilder html, List<Sample> samples, Summary summary) {
		final int slot = 10, barWidth = 8, height = 160, top = 8;
		int width = Math.max(slot * samples.size() + 4, 120);
		double max = Math.max(summary.best, 1);
		html.append("<svg class=\"chart\" viewBox=\"0 0 ").append(width).append(' ').append(height + top + 4).append("\" role=\"img\" aria-label=\"Speed of each search\">");
		for (int i = 0; i < samples.size(); i++) {
			Sample s = samples.get(i);
			double speed = s.speed();
			int tier = s.tier();
			int h = speed < 0 ? 2 : Math.max(2, (int) Math.round(speed / max * height));
			html.append("<rect class=\"bar ").append(tier >= 0 ? TIER_CLASS[tier] : "none").append("\" x=\"").append(2 + i * slot).append("\" y=\"")
					.append(top + height - h).append("\" width=\"").append(barWidth).append("\" height=\"").append(h).append("\" rx=\"1.5\"><title>#")
					.append(i + 1).append(": ").append(esc(SearchStats.formatSpeed(speed))).append(", ")
					.append(esc(SearchStats.formatDuration(s.seconds()))).append("</title></rect>");
		}
		html.append("</svg><p class=\"muted small\">Tallest bar: ").append(esc(SearchStats.formatSpeed(max))).append("</p>");
	}

	private static String modeName(int mode) {
		return mode >= 0 && mode < MODE_NAME.length ? MODE_NAME[mode] : "-";
	}

	private static String css() {
		return ":root{--bg:#f6f7f9;--fg:#1d2430;--muted:#6b7686;--card:#ffffff;--line:#dfe3ea;--slow:#c94842;--fair:#d68c26;--good:#3a7cd6;--excellent:#3d9b5c}"
				+ "@media(prefers-color-scheme:dark){:root{--bg:#14171c;--fg:#e6e9ee;--muted:#8f9aab;--card:#1d2128;--line:#2e343e;--slow:#e0605a;--fair:#e3a040;--good:#5b9bec;--excellent:#52bb74}}"
				+ "*{box-sizing:border-box}body{margin:0;background:var(--bg);color:var(--fg);font:15px/1.5 system-ui,-apple-system,'Segoe UI',Roboto,sans-serif}"
				+ "main{max-width:880px;margin:0 auto;padding:32px 20px 48px}h1{margin:0;font-size:26px}h2{font-size:16px;margin:28px 0 10px}"
				+ ".muted{color:var(--muted)}.small{font-size:12px}.cards{display:grid;grid-template-columns:repeat(auto-fit,minmax(150px,1fr));gap:12px;margin-top:20px}"
				+ ".card{background:var(--card);border:1px solid var(--line);border-radius:14px;padding:14px 16px}.label{font-size:12px;color:var(--muted);text-transform:uppercase;letter-spacing:.06em}"
				+ ".value{font-size:24px;font-weight:650;font-variant-numeric:tabular-nums}.tier{font-size:12px}"
				+ ".slow{color:var(--slow)}.fair{color:var(--fair)}.good{color:var(--good)}.excellent{color:var(--excellent)}"
				+ ".chart{width:100%;height:auto;background:var(--card);border:1px solid var(--line);border-radius:14px;padding:8px}"
				+ ".bar.slow{fill:var(--slow)}.bar.fair{fill:var(--fair)}.bar.good{fill:var(--good)}.bar.excellent{fill:var(--excellent)}.bar.none{fill:var(--line)}"
				+ ".scroll{overflow-x:auto;background:var(--card);border:1px solid var(--line);border-radius:14px}table{border-collapse:collapse;width:100%;font-variant-numeric:tabular-nums}"
				+ "th,td{text-align:left;padding:7px 12px;border-bottom:1px solid var(--line);white-space:nowrap}thead th{font-size:12px;color:var(--muted);text-transform:uppercase;letter-spacing:.05em}"
				+ "tbody tr:last-child td,tbody tr:last-child th{border-bottom:0}table.kv{background:var(--card);border:1px solid var(--line);border-radius:14px;overflow:hidden}"
				+ "table.kv th{width:34%;color:var(--muted);font-weight:500}footer{margin-top:28px;font-size:12px}";
	}

	/** HTML-escapes text (null becomes empty) */
	static String esc(String text) {
		if (text == null) {
			return "";
		}
		StringBuilder out = new StringBuilder(text.length() + 8);
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			switch (c) {
			case '&': out.append("&amp;"); break;
			case '<': out.append("&lt;"); break;
			case '>': out.append("&gt;"); break;
			case '"': out.append("&quot;"); break;
			case '\'': out.append("&#39;"); break;
			default: out.append(c);
			}
		}
		return out.toString();
	}
}

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
 *	 http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package gui.texteditor.commons.filesearch.cli;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import gui.texteditor.commons.filesearch.content.FileContentSearch;
import gui.texteditor.commons.filesearch.content.SearchPerformance;
import various.common.light.files.om.FileContentSearchDTO;
import various.common.light.files.om.SearchResult;

/**
 * Search in files from the command line, with the same engine as the Search in files window but without any window:
 * folders are walked while their files are searched, every core works and each file is printed as soon as it is done.
 * Output like ripgrep: path:line:column:text. Exit code 0 = found, 1 = not found, 2 = error.
 */
public final class SearchCli {

	static final String USAGE = String.join(System.lineSeparator(),
			"Usage: screept-search [options] TEXT [PATH...]",
			"Searches TEXT in the files under each PATH (default: the current folder) with every core of the PC.",
			"",
			"  -i, --ignore-case          case insensitive (default: case sensitive)",
			"  -w, --word                 whole words only",
			"  -e, --regex                TEXT is a regular expression (Java syntax)",
			"  -g, --glob FILTER          only the files whose path matches FILTER (* any text, ? one character);",
			"                             start it with ! to exclude; repeatable",
			"      --binary               search binary files too",
			"  -j, --threads N            worker threads (default: every core)",
			"  -c, --count                number of matching lines of each file",
			"  -l, --files-with-matches   only the names of the files with matches",
			"      --sort                 output sorted by path (it waits for the end of the search)",
			"      --color                colours (path, line number, matches)",
			"      --stats                summary on the error stream: files, matches, time, speed",
			"  -h, --help                 this help",
			"",
			"Output: path:line:column:text. Exit code: 0 found, 1 not found, 2 error.",
			"");

	private static final String C_PATH = "\u001b[35m", C_LINE = "\u001b[32m", C_MATCH = "\u001b[1;31m", C_RESET = "\u001b[0m";

	private SearchCli() {
	}

	public static void main(String[] args) {
		System.exit(run(args, new FileOutputStream(FileDescriptor.out), System.err));
	}

	/** Options of a run, parsed from the arguments */
	static final class Options {
		String text;
		final List<String> paths = new ArrayList<>();
		final List<String> globs = new ArrayList<>();
		/** Filters given to the engine as they are (the ones of the Search in files window) */
		final List<String> engineFilters = new ArrayList<>();
		boolean ignoreCase, word, regex, binary, count, filesOnly, sort, color, stats, help;
		int threads;

		static Options parse(String[] args) {
			Options o = new Options();
			boolean onlyPositional = false;
			for (int i = 0; i < args.length; i++) {
				String a = args[i];
				if (!onlyPositional && a.equals("--")) {
					onlyPositional = true;
				} else if (!onlyPositional && a.startsWith("-") && a.length() > 1) {
					switch (a) {
					case "-i": case "--ignore-case": o.ignoreCase = true; break;
					case "-w": case "--word": o.word = true; break;
					case "-e": case "--regex": o.regex = true; break;
					case "--binary": o.binary = true; break;
					case "-c": case "--count": o.count = true; break;
					case "-l": case "--files-with-matches": o.filesOnly = true; break;
					case "--sort": o.sort = true; break;
					case "--color": o.color = true; break;
					case "--no-color": o.color = false; break;
					case "--stats": o.stats = true; break;
					case "-h": case "--help": o.help = true; break;
					case "-g": case "--glob":
						o.globs.add(value(args, ++i, a));
						break;
					case "--engine-filter":
						o.engineFilters.add(value(args, ++i, a));
						break;
					case "-j": case "--threads":
						try {
							o.threads = Integer.parseInt(value(args, ++i, a));
						} catch (NumberFormatException e) {
							throw new IllegalArgumentException("the number of threads must be a number");
						}
						if (o.threads < 1)
							throw new IllegalArgumentException("the number of threads must be at least 1");
						break;
					default:
						throw new IllegalArgumentException("unknown option " + a);
					}
				} else if (o.text == null) {
					o.text = a;
				} else {
					o.paths.add(a);
				}
			}
			if (!o.help && (o.text == null || o.text.isEmpty()))
				throw new IllegalArgumentException("the text to search is missing");
			if (o.paths.isEmpty())
				o.paths.add(".");
			return o;
		}

		private static String value(String[] args, int i, String option) {
			if (i >= args.length)
				throw new IllegalArgumentException(option + " needs a value");
			return args[i];
		}

		/** The engine's filters: wildcards on the whole path, exclusions marked by its negation prefix */
		String[] engineFilters() {
			List<String> filters = new ArrayList<>();
			for (String g : globs) {
				String filter = g.trim();
				boolean exclude = filter.startsWith("!");
				if (exclude)
					filter = filter.substring(1).trim();
				// a filter without folders applies to the file name, wherever the file is
				if (!filter.isEmpty() && !filter.startsWith("*") && filter.indexOf('/') < 0 && filter.indexOf('\\') < 0)
					filter = "*/" + filter;
				filters.add(exclude ? FileContentSearch.NEGATION_FILTER + filter : filter);
			}
			filters.addAll(engineFilters);
			return filters.toArray(new String[0]);
		}
	}

	/**
	 * Arguments that run, from the command line, the same search as the Search in files window (same results).
	 * @param threads worker threads, 0 = every core
	 */
	public static List<String> argsFor(FileContentSearchDTO dto, List<File> sources, int threads) {
		List<String> args = new ArrayList<>();
		if (!dto.matchCase)
			args.add("-i");
		if (dto.wholeWord)
			args.add("-w");
		if (dto.regex)
			args.add("-e");
		if (dto.binariesIncluded)
			args.add("--binary");
		if (threads > 0) {
			args.add("-j");
			args.add(String.valueOf(threads));
		}
		if (dto.fileFilters != null) {
			for (String filter : dto.fileFilters) {
				if (filter != null && !FileContentSearch.removeExclusionFlag(filter).trim().isEmpty()) {
					args.add("--engine-filter");
					args.add(filter);
				}
			}
		}
		args.add("--");
		args.add(dto.searchString);
		for (File source : sources)
			args.add(source.getAbsolutePath());
		return args;
	}

	static int run(String[] args, OutputStream rawOut, PrintStream err) {
		silenceLogging();
		Options o;
		try {
			o = Options.parse(args);
		} catch (IllegalArgumentException e) {
			err.println("screept-search: " + e.getMessage());
			err.println("Try screept-search --help");
			return 2;
		}

		Writer out = new BufferedWriter(new OutputStreamWriter(rawOut, StandardCharsets.UTF_8), 1 << 20);
		try {
			if (o.help) {
				out.write(USAGE);
				out.flush();
				return 0;
			}

			List<File> roots = new ArrayList<>();
			for (String p : o.paths) {
				File f = new File(p);
				if (!f.exists()) {
					err.println("screept-search: " + p + ": no such file or folder");
					return 2;
				}
				roots.add(f);
			}

			FileContentSearchDTO dto = new FileContentSearchDTO(o.text, !o.ignoreCase, o.word, o.regex, o.binary, o.engineFilters());
			FileContentSearch engine;
			try {
				engine = new FileContentSearch(dto, roots, null);
			} catch (java.util.regex.PatternSyntaxException e) {
				err.println("screept-search: invalid regular expression: " + e.getDescription());
				return 2;
			}
			engine.setPerformance(SearchPerformance.MAXIMUM);
			engine.setResultLimit(Long.MAX_VALUE);

			Printer printer = new Printer(out, o);
			long start = System.nanoTime();
			FileContentSearch.StreamStats stats = engine.searchStreaming(printer::accept, o.threads);
			printer.finish();
			out.flush();
			double seconds = (System.nanoTime() - start) / 1e9;

			if (o.stats) {
				err.println(String.format(java.util.Locale.ROOT,
						"%d matching lines (%d matches) in %d of %d files, %.1f MB in %.3f s (%.2f GB/s), %d threads",
						stats.matchingLines, stats.occurrences, stats.filesWithMatches, stats.filesScanned,
						stats.bytesScanned / 1e6, seconds, stats.bytesScanned / 1e9 / Math.max(seconds, 1e-9), stats.threads));
			}
			return stats.filesWithMatches > 0 ? 0 : 1;

		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return 2;
		} catch (IOException e) {
			// the output was closed (for example by a pipe into head): nothing else to do
			return 2;
		}
	}

	/** Writes each file as one block (files are completed by several threads at once) */
	static final class Printer {
		private final Writer out;
		private final Options o;
		private final List<SearchResult> kept;
		private volatile IOException failure;

		Printer(Writer out, Options o) {
			this.out = out;
			this.o = o;
			this.kept = o.sort ? Collections.synchronizedList(new ArrayList<>()) : null;
		}

		void accept(SearchResult result) {
			if (kept != null) {
				kept.add(result);
				return;
			}
			String block = format(result);
			synchronized (this) {
				if (failure != null)
					return;
				try {
					out.write(block);
				} catch (IOException e) {
					failure = e;
				}
			}
		}

		void finish() throws IOException {
			if (kept != null) {
				List<SearchResult> sorted = new ArrayList<>(kept);
				sorted.sort((a, b) -> a.ownerFile.getPath().compareTo(b.ownerFile.getPath()));
				for (SearchResult r : sorted)
					out.write(format(r));
			}
			if (failure != null)
				throw failure;
		}

		String format(SearchResult result) {
			String path = result.ownerFile.getPath();
			String nl = System.lineSeparator();
			StringBuilder sb = new StringBuilder(256);
			if (o.filesOnly) {
				path(sb, path).append(nl);
				return sb.toString();
			}
			if (o.count) {
				path(sb, path).append(':').append(result.resultLines.size()).append(nl);
				return sb.toString();
			}
			for (Map.Entry<Integer, ArrayList<SearchResult.Match>> line : result.resultLines.entrySet()) {
				List<SearchResult.Match> matches = line.getValue();
				String text = result.getLineMatchText(line.getKey());
				path(sb, path).append(':');
				if (o.color)
					sb.append(C_LINE).append(line.getKey()).append(C_RESET);
				else
					sb.append(line.getKey());
				sb.append(':').append(matches.get(0).start + 1).append(':');
				if (o.color)
					highlight(sb, text, matches);
				else
					sb.append(text);
				sb.append(nl);
			}
			return sb.toString();
		}

		private StringBuilder path(StringBuilder sb, String path) {
			return o.color ? sb.append(C_PATH).append(path).append(C_RESET) : sb.append(path);
		}

		/** Matches in red; overlapping ones are merged */
		private static void highlight(StringBuilder sb, String text, List<SearchResult.Match> matches) {
			int done = 0;
			for (SearchResult.Match m : matches) {
				int s = Math.max(Math.min(m.start, text.length()), done), e = Math.min(m.end, text.length());
				if (e <= s)
					continue;
				sb.append(text, done, s).append(C_MATCH).append(text, s, e).append(C_RESET);
				done = e;
			}
			sb.append(text, Math.min(done, text.length()), text.length());
		}
	}

	/** Logging goes nowhere: the output of a command line tool is only its results */
	private static void silenceLogging() {
		try {
			org.apache.log4j.Logger root = org.apache.log4j.Logger.getRootLogger();
			root.removeAllAppenders();
			root.addAppender(new org.apache.log4j.varia.NullAppender());
			root.setLevel(org.apache.log4j.Level.OFF);
		} catch (Throwable e) {
			// log4j not on the class path: nothing to silence
		}
	}
}

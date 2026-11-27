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
package gui.texteditor.commons.filesearch.content;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import various.common.light.files.om.FileContentSearchDTO;
import various.common.light.files.om.SearchResult;

/**
 * The search inside files (block reader with byte prefilters, no window): results compared with a naive per-line reference.
 * The files have no extension on purpose: the binary check by extension asks the operating system.
 */
public class FileContentSearchEngineTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private static final Charset CS = charset();

	private static Charset charset() {
		try {
			return Charset.forName(System.getProperty("file.encoding"));
		} catch (RuntimeException e) {
			return Charset.defaultCharset();
		}
	}

	private static FileContentSearchDTO dto(String text, boolean matchCase, boolean wholeWord, boolean regex, boolean binaries) {
		return new FileContentSearchDTO(text, matchCase, wholeWord, regex, binaries, null);
	}

	private static FileContentSearch engine(FileContentSearchDTO dto) {
		return new FileContentSearch(dto, new ArrayList<File>(), null);
	}

	/** The block search of one file, as the worker threads run it */
	private static SearchResult scan(FileContentSearch engine, File file) throws Exception {
		Method m = FileContentSearch.class.getDeclaredMethod("scanSource", File.class);
		m.setAccessible(true);
		try {
			return (SearchResult) m.invoke(engine, file);
		} catch (InvocationTargetException e) {
			throw (Exception) e.getCause();
		}
	}

	private File write(String name, String content) throws IOException {
		File f = new File(tmp.getRoot(), name);
		Files.write(f.toPath(), content.getBytes(CS));
		return f;
	}

	/** line number -> [start,end] pairs */
	private static Map<Integer, List<String>> asMap(SearchResult result) {
		Map<Integer, List<String>> out = new TreeMap<Integer, List<String>>();
		for (Map.Entry<Integer, ? extends List<?>> e : result.resultLines.entrySet()) {
			List<String> spans = new ArrayList<String>();
			for (Object match : e.getValue()) {
				SearchResult.Match m = (SearchResult.Match) match;
				spans.add(m.start + "-" + m.end);
			}
			out.put(e.getKey(), spans);
		}
		return out;
	}

	private static List<String> lines(String content) {
		List<String> lines = new ArrayList<String>();
		int start = 0;
		while (start < content.length()) {
			int end = content.indexOf('\n', start);
			int stop = end < 0 ? content.length() : end;
			int contentEnd = stop > start && content.charAt(stop - 1) == '\r' ? stop - 1 : stop;
			lines.add(content.substring(start, contentEnd));
			start = stop + 1;
		}
		return lines;
	}

	/** Naive reference: every (possibly overlapping) occurrence of a literal, or every regex match, line by line */
	private static Map<Integer, List<String>> reference(String content, String needle, boolean matchCase, boolean wholeWord, boolean regex) {
		Map<Integer, List<String>> out = new TreeMap<Integer, List<String>>();
		List<String> lines = lines(content);
		Pattern pattern = null;
		if (regex || wholeWord) {
			String source = regex ? (needle.indexOf('|') >= 0 ? "(?:" + needle + ")" : needle) : Pattern.quote(needle);
			pattern = Pattern.compile(wholeWord ? "\\b" + source + "\\b" : source, matchCase ? 0 : Pattern.CASE_INSENSITIVE);
		}
		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			List<String> spans = new ArrayList<String>();
			if (pattern != null) {
				Matcher m = pattern.matcher(line);
				while (m.find()) {
					spans.add(m.start() + "-" + m.end());
				}
			} else {
				String hay = matchCase ? line : line.toLowerCase();
				String n = matchCase ? needle : needle.toLowerCase();
				for (int at = hay.indexOf(n); at >= 0; at = hay.indexOf(n, at + 1)) {
					spans.add(at + "-" + (at + needle.length()));
				}
			}
			if (!spans.isEmpty()) {
				out.put(i + 1, spans);
			}
		}
		return out;
	}

	private static final String[] WORDS = { "error", "Error", "ERROR", "timeout", "aaaa", "foo", "foobar", "foo_bar", "café", "x", "2026-11-16", "12:30:45", "connection reset", "a.c", "abc", "perché" };

	private static String randomText(long seed, int approxBytes, String eol, boolean trailing) {
		Random random = new Random(seed);
		StringBuilder sb = new StringBuilder();
		while (sb.length() < approxBytes) {
			int n = random.nextInt(9);
			for (int i = 0; i < n; i++) {
				sb.append(i == 0 ? "" : random.nextInt(5) == 0 ? "" : " ").append(WORDS[random.nextInt(WORDS.length)]);
			}
			if (random.nextInt(200) == 0) { // a long line
				for (int i = 0; i < 3000; i++) {
					sb.append("long ");
				}
				sb.append("foo");
			}
			sb.append(eol);
		}
		String text = sb.toString();
		return trailing ? text : text.substring(0, text.length() - eol.length());
	}

	private void check(String content, FileContentSearchDTO dto) throws Exception {
		File file = write("f" + System.nanoTime(), content);
		Map<Integer, List<String>> expected = reference(content, dto.searchString, dto.matchCase, dto.wholeWord, dto.regex);
		SearchResult result = scan(engine(dto), file);
		assertNotNull(result);
		assertEquals("search " + dto, expected, asMap(result));
		// the slow line reader agrees too (it also splits on lone CR: not present in these files)
		FileContentSearch e = engine(dto);
		assertEquals("slow path " + dto, expected, asMap(e.searchIntoFile(file, dto, null)));
	}

	@Test
	public void plainSearchOverSeveralBlocksMatchesTheReference() throws Exception {
		String content = randomText(1, 1_600_000, "\n", true);
		for (String needle : new String[] { "error", "foo", "aaaa", "café", "connection reset", "12:30", "zzzz-not-there", "x" }) {
			for (boolean matchCase : new boolean[] { false, true }) {
				check(content, dto(needle, matchCase, false, false, false));
			}
		}
	}

	@Test
	public void crlfFileWithoutTrailingNewline() throws Exception {
		String content = randomText(2, 700_000, "\r\n", false);
		check(content, dto("error", false, false, false, false));
		check(content, dto("foo", true, false, false, false));
		check(content, dto("^foo", false, false, true, false));
	}

	@Test
	public void overlappingMatchesAreAllReported() throws Exception {
		check("aaaaaa\nxaaay\n", dto("aa", false, false, false, false));
		Map<Integer, List<String>> r = asMap(scan(engine(dto("aa", false, false, false, false)), write("ov", "aaaa\n")));
		assertEquals(Arrays.asList("0-2", "1-3", "2-4"), r.get(1));
	}

	@Test
	public void regexSearchMatchesTheReference() throws Exception {
		String content = randomText(3, 1_200_000, "\n", true);
		String[] regexes = { "err\\w+", "foo\\w*bar", "\\d{4}-\\d{2}-\\d{2}", "(foo|abc)\\b", "^error", "timeout$", "co[n]+ection", "a\\.c", "x" };
		for (String regex : regexes) {
			for (boolean matchCase : new boolean[] { false, true }) {
				check(content, dto(regex, matchCase, false, true, false));
			}
		}
	}

	@Test
	public void wholeWordSearchMatchesTheReference() throws Exception {
		String content = randomText(4, 800_000, "\n", true);
		for (String word : new String[] { "foo", "error", "abc", "x", "foo_bar", "café" }) {
			check(content, dto(word, false, true, false, false));
			check(content, dto(word, true, true, false, false));
		}
	}

	/** Whole word is meant for plain text: the characters of the text must not be taken as a regular expression */
	@Test
	public void wholeWordOfTextWithRegexCharactersIsLiteral() throws Exception {
		String content = "abc\na.c\nxa.c y\nsee (a.c) here\nc++ rocks\nfoo(\n";
		check(content, dto("a.c", false, true, false, false));
		check(content, dto("c++", false, true, false, false));
		check(content, dto("foo(", false, true, false, false));
		check(content, dto("a\\", false, true, false, false));
		check(content, dto("a.c", true, true, false, false));
	}

	@Test
	public void wholeWordRegexWithAlternativesIsGrouped() throws Exception {
		String content = "foo\nxfoo\nbar\nbarx\nfoobar\nzz bar\n";
		check(content, dto("foo|bar", false, true, true, false));
		Map<Integer, List<String>> r = asMap(scan(engine(dto("foo|bar", false, true, true, false)), write("alt", content)));
		assertEquals(Arrays.asList(1, 3, 6), new ArrayList<Integer>(r.keySet()));
	}

	@Test
	public void textWithRegexCharactersIsLiteralInPlainMode() throws Exception {
		String content = "abc\na.c\nxa.c y\nsee (a.c) here\nc++ rocks\nfoo(\n[x]\n";
		for (String needle : new String[] { "a.c", "c++", "foo(", "[x]", "(a.c)" }) {
			check(content, dto(needle, false, false, false, false));
		}
	}

	@Test(expected = java.util.regex.PatternSyntaxException.class)
	public void invalidRegexIsRejectedByTheConstructor() {
		engine(dto("(unclosed", false, false, true, false));
	}

	@Test
	public void nullDtoDoesNotCrashTheConstructor() {
		FileContentSearch e = new FileContentSearch(null, new ArrayList<File>(), null);
		assertEquals("", e.searchDTO.searchString);
	}

	@Test
	public void emptySearchStringFindsNothing() throws Exception {
		SearchResult r = scan(engine(dto("", false, false, false, false)), write("e1", "abc\ndef\n"));
		assertTrue(r.resultLines.isEmpty());
		SearchResult blank = scan(engine(dto("   ", false, false, false, false)), write("e2", "abc   def\n"));
		assertTrue("the text is trimmed to empty", blank.resultLines.isEmpty());
	}

	@Test
	public void searchTextWithLineBreakNeverMatches() throws Exception {
		SearchResult r = scan(engine(dto("abc\ndef", false, false, false, false)), write("nl", "abc\ndef\n"));
		assertTrue(r.resultLines.isEmpty());
	}

	@Test
	public void emptyFileMissingFileAndFolder() throws Exception {
		assertTrue(scan(engine(dto("a", false, false, false, false)), write("empty", "")).resultLines.isEmpty());
		assertNull("a missing file is reported as no result", scan(engine(dto("a", false, false, false, false)), new File(tmp.getRoot(), "missing")));
		File folder = tmp.newFolder("sub");
		assertTrue(scan(engine(dto("a", false, false, false, false)), folder).resultLines.isEmpty());
	}

	@Test
	public void binaryContentIsSkippedUnlessRequested() throws Exception {
		byte[] data = "needle here\u0000binary\nneedle again\n".getBytes(CS);
		File file = new File(tmp.getRoot(), "blob");
		Files.write(file.toPath(), data);
		assertTrue(scan(engine(dto("needle", false, false, false, false)), file).resultLines.isEmpty());
		SearchResult included = scan(engine(dto("needle", false, false, false, true)), file);
		assertEquals(Arrays.asList(1, 2), new ArrayList<Integer>(included.resultLines.keySet()));
	}

	@Test
	public void nulByteAfterTheSniffedPrefixDoesNotMakeTheFileBinary() throws Exception {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 9000; i++) {
			sb.append('a');
		}
		sb.append('\u0000').append(" needle\n");
		File file = write("late", sb.toString());
		assertEquals(1, scan(engine(dto("needle", false, false, false, false)), file).resultLines.size());
	}

	@Test
	public void resultsAreOrderedByLine() throws Exception {
		SearchResult r = scan(engine(dto("a", false, false, false, false)), write("ord", "a\nb\na a\nc\na\n"));
		assertEquals(Arrays.asList(1, 3, 5), new ArrayList<Integer>(r.resultLines.keySet()));
		assertEquals(2, r.getOccurrencesFoundAtLine(3));
		assertEquals(4, r.getOccurrencesFound());
		assertEquals("a a", r.getLineMatchText(3));
	}

	@Test
	public void lineTextOfAMatchHasNoCarriageReturn() throws Exception {
		SearchResult r = scan(engine(dto("beta", false, false, false, false)), write("cr", "alpha\r\nbeta gamma\r\n"));
		assertEquals("beta gamma", r.getLineMatchText(2));
	}

	@Test
	public void nonAsciiLinePositionsAreInCharacters() throws Exception {
		SearchResult r = scan(engine(dto("end", false, false, false, false)), write("uni", "café éé end\n"));
		assertEquals(Arrays.asList("8-11"), asMap(r).get(1));
	}

	@Test
	public void searchIntoLineFindsEveryOccurrence() {
		FileContentSearchDTO d = dto("ab", false, false, false, false);
		SearchResult r = new SearchResult(null);
		engine(d).searchIntoLine(7, "xAB ab abab", d, r);
		assertEquals(Arrays.asList("1-3", "4-6", "7-9", "9-11"), asMap(r).get(7));
		FileContentSearchDTO rx = dto("a+b", false, false, true, false);
		SearchResult r2 = new SearchResult(null);
		engine(rx).searchIntoLine(1, "aab b ab", rx, r2);
		assertEquals(Arrays.asList("0-3", "6-8"), asMap(r2).get(1));
	}

	// ---------------------------------------------------------------- literal extraction (regex prefilter)

	@Test
	public void extractLiteralFindsTheLongestPlainRun() {
		assertEquals("foo", FileContentSearch.extractLiteral("foo\\d+bar"));
		assertEquals("word", FileContentSearch.extractLiteral("\\bword\\b"));
		assertEquals("colo", FileContentSearch.extractLiteral("colou?r"));
		assertEquals(".*", FileContentSearch.extractLiteral("\\.\\*"));
		assertEquals("foo", FileContentSearch.extractLiteral("[abc]foo"));
		assertEquals("abcdef", FileContentSearch.extractLiteral("ab.cd+abcdef"));
	}

	@Test
	public void extractLiteralGivesUpOnComplexExpressions() {
		assertNull(FileContentSearch.extractLiteral(null));
		assertNull(FileContentSearch.extractLiteral(""));
		assertNull(FileContentSearch.extractLiteral("a|b"));
		assertNull(FileContentSearch.extractLiteral("(foo)bar"));
		assertNull(FileContentSearch.extractLiteral("foo\\"));
		assertNull(FileContentSearch.extractLiteral("foo{"));
		assertNull(FileContentSearch.extractLiteral("\\d+"));
	}

	// ---------------------------------------------------------------- file filters

	@Test
	public void exclusionFlagHelpers() {
		assertTrue(FileContentSearch.isExclusionFilter("<>*.log"));
		assertTrue(FileContentSearch.isExclusionFilter("  <>*.log"));
		assertFalse(FileContentSearch.isExclusionFilter("*.log"));
		assertFalse(FileContentSearch.isExclusionFilter(null));
		assertFalse(FileContentSearch.isExclusionFilter(""));
		assertEquals("*.log", FileContentSearch.removeExclusionFlag("<> *.log"));
		assertEquals("*.log", FileContentSearch.removeExclusionFlag("*.log"));
		assertEquals(Arrays.asList("*.java"), Arrays.asList(FileContentSearch.getInclusionFileFilters(new String[] { "*.java", "<>*.log" })));
		assertEquals(Arrays.asList("*.log"), Arrays.asList(FileContentSearch.getExclusionFileFilters(new String[] { "*.java", "<>*.log" })));
		assertEquals(0, FileContentSearch.getInclusionFileFilters(null).length);
		assertEquals(0, FileContentSearch.getExclusionFileFilters(new String[0]).length);
	}

	private static List<File> files(String... paths) {
		List<File> out = new ArrayList<File>();
		for (String p : paths) {
			out.add(new File(p));
		}
		return out;
	}

	private static List<String> names(List<File> files) {
		List<String> out = new ArrayList<String>();
		for (File f : files) {
			out.add(f.getPath().replace(File.separatorChar, '/'));
		}
		return out;
	}

	@Test
	public void filtersInclusionAndExclusion() {
		List<File> all = files("src/A.java", "src/B.xml", "src/test/CTest.java", "docs/readme.md", "build/out.log");
		assertEquals(Arrays.asList("src/A.java", "src/test/CTest.java"), names(FileContentSearch.applyFilters(all, filterDto("*.java"))));
		assertEquals(Arrays.asList("src/A.java", "src/B.xml", "docs/readme.md", "build/out.log"), names(FileContentSearch.applyFilters(all, filterDto("<>*/test/*"))));
		assertEquals(Arrays.asList("src/A.java"), names(FileContentSearch.applyFilters(all, filterDto("*.java", "<>*/test/*"))));
		assertEquals(Arrays.asList("src/A.java", "src/B.xml"), names(FileContentSearch.applyFilters(all, filterDto("*A.java", "*.xml"))));
		assertEquals(names(all), names(FileContentSearch.applyFilters(all, filterDto())));
	}

	@Test
	public void blankAndInvalidFiltersAreIgnored() {
		List<File> all = files("a.txt", "b.txt");
		assertEquals(names(all), names(FileContentSearch.applyFilters(all, filterDto("   "))));
		assertEquals(names(all), names(FileContentSearch.applyFilters(all, filterDto("("))));
		assertEquals(names(all), names(FileContentSearch.applyFilters(all, filterDto("<>("))));
		// a valid filter next to an invalid one still applies
		assertEquals(Arrays.asList("a.txt"), names(FileContentSearch.applyFilters(all, filterDto("(", "a*"))));
	}

	@Test
	public void questionMarkIsOneOptionalCharacter() {
		List<File> all = files("file.txt", "fileA.txt", "fileAB.txt");
		assertEquals(Arrays.asList("file.txt", "fileA.txt"), names(FileContentSearch.applyFilters(all, filterDto("file?.txt"))));
	}

	@Test
	public void filtersLeaveTheInputListUntouched() {
		List<File> all = files("a.java", "b.xml");
		FileContentSearch.applyFilters(all, filterDto("*.java"));
		assertEquals(2, all.size());
		List<File> copy = FileContentSearch.applyFilters(all, filterDto());
		assertEquals(2, copy.size());
		assertFalse(copy == all);
	}

	private static FileContentSearchDTO filterDto(String... filters) {
		return new FileContentSearchDTO("x", false, false, false, false, filters);
	}
}

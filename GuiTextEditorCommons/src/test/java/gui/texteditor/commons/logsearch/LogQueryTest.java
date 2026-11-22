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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.junit.Test;

/** The search language of the LogSearch field, the byte matching and the regex literal prefilter */
public class LogQueryTest {

	private static LogQuery plain(String text) {
		return new LogQuery(text, false, false, false);
	}

	@Test
	public void allWordsMustBeInTheLineInAnyOrder() {
		LogQuery q = plain("error timeout");
		assertTrue(q.test("12:00 TIMEOUT while reading, ERROR"));
		assertTrue(q.test("error then timeout"));
		assertFalse(q.test("only error here"));
		assertFalse(q.test("only timeout here"));
	}

	@Test
	public void quotedPhraseStaysTogether() {
		LogQuery q = plain("\"connection reset\"");
		assertTrue(q.test("Connection reset by peer"));
		assertFalse(q.test("connection was reset"));
		assertFalse(q.test("connection  reset")); // two spaces: not the same phrase
	}

	@Test
	public void minusExcludesWordsAndPhrases() {
		LogQuery q = plain("error -healthcheck -\"disk full\"");
		assertTrue(q.test("ERROR in service"));
		assertFalse(q.test("ERROR in healthcheck"));
		assertFalse(q.test("ERROR: Disk Full"));
		assertTrue(q.test("ERROR: disk almost full"));
	}

	@Test
	public void queryOnlyMadeOfExclusions() {
		LogQuery q = plain("-healthcheck");
		assertFalse(q.isEmpty());
		assertTrue(q.test("anything else"));
		assertFalse(q.test("a HEALTHCHECK line"));
		assertNull(q.highlight);
	}

	@Test
	public void loneMinusIsAnOrdinaryTerm() {
		LogQuery q = plain("-");
		assertTrue(q.test("a-b"));
		assertFalse(q.test("ab"));
	}

	@Test
	public void unbalancedQuoteTakesTheRestAsPhrase() {
		LogQuery q = plain("\"abc def");
		assertTrue(q.test("xx abc def yy"));
		assertFalse(q.test("abc"));
	}

	@Test
	public void emptyAndBlankQueries() {
		assertTrue(plain("").isEmpty());
		assertTrue(plain("   ").isEmpty());
		assertTrue(plain("").test("anything"));
		assertNull(plain("").highlight);
		assertTrue(new LogQuery("", true, false, false).isEmpty());
	}

	@Test
	public void matchCaseAndWholeWord() {
		assertFalse(new LogQuery("Error", false, true, false).test("an error"));
		assertTrue(new LogQuery("Error", false, true, false).test("an Error"));
		LogQuery word = new LogQuery("err", false, false, true);
		assertFalse(word.test("an error"));
		assertTrue(word.test("an err."));
		assertTrue(word.test("(ERR)"));
		assertFalse(word.test("err_x"));
		assertFalse(word.test("my_err"));
		assertTrue(word.test("err"));
		assertTrue(word.test("a-err-b"));
	}

	@Test
	public void plainTextIsLiteral() {
		assertTrue(plain("a.b").test("xa.by"));
		assertFalse(plain("a.b").test("axb"));
		assertTrue(plain("(x)").test("f(x)"));
		assertTrue(plain("c++").test("g++ and c++"));
		assertTrue(plain("[abc]").test("a [abc] b"));
		assertFalse(plain("[abc]").test("a b"));
		assertTrue(plain("a\\b").test("a\\b"));
		assertTrue(plain("^$").test("cost ^$ end"));
		assertTrue(plain("café").test("un CAFÉ fumante"));
	}

	@Test
	public void regexModeUsesTheWholeText() {
		LogQuery q = new LogQuery("ERR\\d{3} .*timeout", true, false, false);
		assertTrue(q.test("err500 request timeout"));
		assertFalse(q.test("err50 request timeout"));
		LogQuery spaced = new LogQuery("a b", true, false, false);
		assertTrue(spaced.test("xx a b yy"));
		assertFalse(spaced.test("b a"));
		assertTrue(new LogQuery("^2026", true, true, false).test("2026-11-16"));
		assertFalse(new LogQuery("^2026", true, true, false).test(" 2026"));
	}

	@Test
	public void regexWholeWordWrapsTheExpression() {
		LogQuery q = new LogQuery("foo|bar", true, false, true);
		assertTrue(q.test("a foo b"));
		assertTrue(q.test("bar"));
		assertFalse(q.test("foobar"));
		assertFalse(q.test("xbar"));
	}

	@Test(expected = PatternSyntaxException.class)
	public void invalidRegexIsReported() {
		new LogQuery("(unclosed", true, false, false);
	}

	@Test
	public void highlightCoversTheRequiredTerms() {
		LogQuery q = plain("error \"time out\" -skip");
		Matcher m = q.highlight.matcher("An ERROR and a TIME OUT, skip");
		List<String> found = new ArrayList<String>();
		while (m.find()) {
			found.add(m.group());
		}
		assertEquals(Arrays.asList("ERROR", "TIME OUT"), found);
	}

	@Test
	public void testerGivesTheSameVerdictAsTest() {
		LogQuery q = plain("error -healthcheck");
		LogQuery.Tester tester = q.tester();
		for (String line : new String[] { "error", "ERROR healthcheck", "nothing", "x error y", "" }) {
			assertEquals(line, q.test(line), tester.test(line));
		}
	}

	@Test
	public void kindDescribesTheSearch() {
		assertEquals(SearchStats.KIND_TEXT, plain("a").kind);
		assertEquals(SearchStats.KIND_MULTI, plain("a b").kind);
		assertEquals(SearchStats.KIND_MULTI, plain("a -b").kind);
		assertEquals(SearchStats.KIND_REGEX, new LogQuery("a", true, false, false).kind);
		assertEquals(SearchStats.KIND_TEXT | SearchStats.KIND_CASE | SearchStats.KIND_WORD, new LogQuery("a", false, true, true).kind);
	}

	@Test
	public void bytesCapableOnlyForAsciiPlainText() {
		assertTrue(plain("error timeout").bytesCapable());
		assertTrue(plain("").bytesCapable());
		assertFalse(plain("café").bytesCapable());
		assertFalse(plain("error café").bytesCapable());
		assertFalse(new LogQuery("error", true, false, false).bytesCapable());
	}

	/** The byte path must give the verdict of the regular expression path, also on a slice of a bigger buffer */
	@Test
	public void bytesAndTextVerdictsAgreeOnRandomInput() {
		Random random = new Random(42);
		String alphabet = "abAB_ -.:xy01";
		String[] terms = { "ab", "AB", "a", "b_", "x-y", "ab ab", "ba", "y0", "1", "_", "A", "-.", "aab", "bab" };
		for (int round = 0; round < 3000; round++) {
			StringBuilder query = new StringBuilder();
			int n = 1 + random.nextInt(3);
			for (int i = 0; i < n; i++) {
				String term = terms[random.nextInt(terms.length)];
				String piece = term.indexOf(' ') >= 0 ? "\"" + term + "\"" : term;
				query.append(i == 0 ? "" : " ").append(random.nextInt(4) == 0 ? "-" : "").append(piece);
			}
			boolean matchCase = random.nextBoolean();
			boolean wholeWord = random.nextBoolean();
			LogQuery q = new LogQuery(query.toString(), false, matchCase, wholeWord);
			assertTrue(q.bytesCapable());
			StringBuilder line = new StringBuilder();
			int length = random.nextInt(20);
			for (int i = 0; i < length; i++) {
				line.append(alphabet.charAt(random.nextInt(alphabet.length())));
			}
			byte[] bytes = ("zz" + line + "zz").getBytes(StandardCharsets.UTF_8);
			boolean expected = q.test(line.toString());
			assertEquals("query [" + query + "] case=" + matchCase + " word=" + wholeWord + " line [" + line + "]", expected, q.testBytes(bytes, 2, 2 + line.length()));
		}
	}

	@Test
	public void bytesPathOnNonAsciiLinesTreatsMultibyteAsNonWord() {
		LogQuery q = new LogQuery("perch", false, false, true);
		byte[] line = "perché a".getBytes(StandardCharsets.UTF_8);
		assertEquals(q.test("perché a"), q.testBytes(line, 0, line.length));
		assertTrue(q.testBytes(line, 0, line.length));
	}

	// ---------------------------------------------------------------- required literals of a regular expression

	@Test
	public void requiredLiteralsOfSimpleExpressions() {
		assertEquals(Arrays.asList("foo", "bar"), LogQuery.requiredLiterals("foo\\d+bar"));
		assertEquals(Arrays.asList("connection", "reset"), LogQuery.requiredLiterals("connection.*reset").subList(0, 2));
		assertEquals(Arrays.asList("abcd"), LogQuery.requiredLiterals("abcd"));
		assertEquals(Arrays.asList("foo.bar"), LogQuery.requiredLiterals("foo\\.bar"));
		assertTrue(LogQuery.requiredLiterals("ab").isEmpty()); // under 3 characters
	}

	@Test
	public void requiredLiteralsAreConservative() {
		assertTrue(LogQuery.requiredLiterals("foo|bar").isEmpty());
		assertTrue(LogQuery.requiredLiterals("(?i)foo").isEmpty());
		assertTrue(LogQuery.requiredLiterals("(?<=a)foo").isEmpty());
		assertTrue(LogQuery.requiredLiterals("(foo)?bar").isEmpty());
		assertTrue(LogQuery.requiredLiterals("\\Qfoo\\E").isEmpty());
		assertTrue(LogQuery.requiredLiterals("(a)\\1").isEmpty());
		assertTrue(LogQuery.requiredLiterals("trailing\\").isEmpty());
		// an optional character is not required
		for (String literal : LogQuery.requiredLiterals("colou?rfast")) {
			assertTrue(literal.equals("colo") || literal.equals("rfast"));
		}
	}

	@Test
	public void prefilterIsSoundForRandomLines() {
		String[] regexes = { "foo\\d+bar", "foo.*bar", "ab+c", "x{2,3}yz", "foo(bar)?baz", "foo(?:bar)+baz", "[a-c]+foo", "foo\\.bar", "ab*cdef", "^foo bar$", "a{0}foo" };
		String[] pieces = { "foo", "bar", "baz", "abc", "abbc", "xxyz", "xyz", "12", "7", " ", "foo.bar", "cdef", "acdef", "abbbcdef", "FOO", "Bar" };
		Random random = new Random(7);
		for (String regex : regexes) {
			for (boolean matchCase : new boolean[] { true, false }) {
				LogQuery q = new LogQuery(regex, true, matchCase, false);
				Pattern p = Pattern.compile(regex, matchCase ? 0 : Pattern.CASE_INSENSITIVE);
				for (int round = 0; round < 400; round++) {
					StringBuilder line = new StringBuilder();
					int n = random.nextInt(6);
					for (int i = 0; i < n; i++) {
						line.append(pieces[random.nextInt(pieces.length)]);
					}
					byte[] bytes = ("##" + line + "##").getBytes(StandardCharsets.UTF_8);
					if (p.matcher("##" + line + "##").find()) {
						// a prefilter that rejects a line the expression matches would lose results
						assertTrue("regex " + regex + " case=" + matchCase + " lost line [" + line + "]", q.passesPrefilter(bytes, 0, bytes.length));
					}
				}
			}
		}
	}

	@Test
	public void prefilterIsPresentOnlyWhenThereAreFixedPieces() {
		assertTrue(new LogQuery("timeout\\s+\\d+", true, false, false).hasPrefilter());
		assertFalse(new LogQuery("a|b", true, false, false).hasPrefilter());
		assertFalse(new LogQuery("\\d+", true, false, false).hasPrefilter());
		assertFalse(plain("timeout").hasPrefilter());
		LogQuery q = new LogQuery("timeout\\s+\\d+", true, false, false);
		byte[] hit = "a TIMEOUT 5".getBytes(StandardCharsets.UTF_8);
		byte[] miss = "a time out 5".getBytes(StandardCharsets.UTF_8);
		assertTrue(q.passesPrefilter(hit, 0, hit.length));
		assertFalse(q.passesPrefilter(miss, 0, miss.length));
		assertNotNull(q.highlight);
	}
}

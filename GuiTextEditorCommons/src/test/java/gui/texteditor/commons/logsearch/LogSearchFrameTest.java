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
import static org.junit.Assert.assertTrue;

import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.swing.JList;
import javax.swing.SwingUtilities;

import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * The log viewer window on a real log file (never shown): indexing, searching with its options, level filters, jumps,
 * bookmarks, zoom and following a file that grows. Dialogs (go to line, export, bookmarks menu) are not opened.
 */
public class LogSearchFrameTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private LogSearchFrame frame;
	private File log;
	private final List<Integer> errorLines = new ArrayList<Integer>();
	private int totalLines;

	private static String line(int i) {
		String level = i % 50 == 7 ? "ERROR" : i % 10 == 3 ? "WARN " : "INFO ";
		String msg = i % 50 == 7 ? "boom number " + i : i % 10 == 3 ? "disk almost full " + i : i % 25 == 0 ? "user alice logged in" : i % 33 == 0 ? "failure of job " + i : "step " + i + " fail safe";
		return "2026-01-0" + (1 + i / 400) + " 10:" + String.format("%02d", (i / 7) % 60) + ":" + String.format("%02d", i % 60) + "," + String.format("%03d", i % 1000) + " " + level + " " + msg;
	}

	@Before
	public void build() throws Exception {
		Assume.assumeFalse("needs a display server (windows are never shown)", GraphicsEnvironment.isHeadless());
		StringBuilder sb = new StringBuilder();
		int n = 0;
		for (int i = 0; i < 1000; i++) {
			if (i % 50 == 7)
				errorLines.add(n);
			sb.append(line(i)).append('\n');
			n++;
			if (i % 50 == 7) { // a stack trace continues the error
				sb.append("\tat some.Class.method(Class.java:").append(i).append(")\n");
				n++;
			}
		}
		totalLines = n;
		log = new File(tmp.getRoot(), "app.log");
		Files.write(log.toPath(), sb.toString().getBytes(StandardCharsets.UTF_8));
		frame = edt(new Callable<LogSearchFrame>() {
			@Override
			public LogSearchFrame call() throws Exception {
				Constructor<LogSearchFrame> c = LogSearchFrame.class.getDeclaredConstructor(Component.class, boolean.class);
				c.setAccessible(true);
				LogSearchFrame f = c.newInstance(null, true);
				Method m = LogSearchFrame.class.getDeclaredMethod("load", File.class);
				m.setAccessible(true);
				m.invoke(f, log);
				f.addNotify();
				f.setSize(1100, 800);
				f.validate();
				return f;
			}
		});
		waitIndexed(totalLines);
	}

	@After
	public void close() throws Exception {
		edt(new Callable<Void>() {
			@Override
			public Void call() {
				frame.dispose();
				return null;
			}
		});
	}

	// ------------------------------------------------------------------ plumbing

	private static <T> T edt(final Callable<T> c) throws Exception {
		if (SwingUtilities.isEventDispatchThread())
			return c.call();
		final Object[] box = new Object[2];
		SwingUtilities.invokeAndWait(new Runnable() {
			@Override
			public void run() {
				try {
					box[0] = c.call();
				} catch (Throwable t) {
					box[1] = t;
				}
			}
		});
		if (box[1] != null)
			throw new AssertionError("EDT: " + box[1], (Throwable) box[1]);
		@SuppressWarnings("unchecked")
		T r = (T) box[0];
		return r;
	}

	private static void flush() throws Exception {
		for (int i = 0; i < 3; i++)
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
				}
			});
	}

	@SuppressWarnings("unchecked")
	private <T> T field(String name) throws Exception {
		Field f = LogSearchFrame.class.getDeclaredField(name);
		f.setAccessible(true);
		return (T) f.get(frame);
	}

	private Object call(final String name, final Object... args) throws Exception {
		for (final Method m : LogSearchFrame.class.getDeclaredMethods()) {
			if (m.getName().equals(name) && m.getParameterTypes().length == args.length) {
				m.setAccessible(true);
				Object r = edt(new Callable<Object>() {
					@Override
					public Object call() throws Exception {
						return m.invoke(frame, args);
					}
				});
				flush();
				return r;
			}
		}
		throw new AssertionError("no method " + name);
	}

	private void waitIndexed(int lines) throws Exception {
		LogFileIndex index = field("index");
		long end = System.currentTimeMillis() + 15000;
		while (System.currentTimeMillis() < end && !(index.isComplete() && index.lineCount() == lines)) {
			Thread.sleep(25);
			flush();
		}
		assertEquals("indexed lines", lines, index.lineCount());
		// the list shows the lines once the window has refreshed itself
		JList<?> main = field("mainList");
		while (System.currentTimeMillis() < end && main.getModel().getSize() != lines) {
			call("refresh");
			Thread.sleep(50);
		}
	}

	/** Types the text, runs the search and waits for it; returns the number of matching lines. */
	private int searchText(final String text) throws Exception {
		final javax.swing.JTextField txt = field("txtSearch");
		edt(new Callable<Void>() {
			@Override
			public Void call() {
				txt.setText(text);
				return null;
			}
		});
		call("runSearch");
		LogSearcher s = field("search");
		if (s != null)
			LogFixtures.waitFinished(s);
		Thread.sleep(150);
		flush();
		Object grep = field("grepModel");
		Field lines = grep.getClass().getDeclaredField("matchLines");
		lines.setAccessible(true);
		return ((LineSet) lines.get(grep)).size();
	}

	private void option(String button, boolean on) throws Exception {
		Object b = field(button);
		Field sel = b.getClass().getDeclaredField("selected");
		sel.setAccessible(true);
		sel.set(b, on);
	}

	private int currentLine() throws Exception {
		return (Integer) call("currentLine");
	}

	// ------------------------------------------------------------------ tests

	@Test
	public void theFileIsIndexedAndDescribed() throws Exception {
		LogFileIndex index = field("index");
		assertTrue(index.isComplete());
		assertEquals(totalLines, index.lineCount());
		assertEquals("app.log", ((javax.swing.JLabel) field("lblName")).getText());
		assertTrue(((javax.swing.JLabel) field("lblPath")).getText().endsWith("app.log"));
		assertEquals(log, (File) field("loadedFile"));
		assertTrue(frame.getTitle().contains("app.log"));
		assertEquals("a continuation line has the level of the line it continues", LogLevel.ERROR, LogLevel.level(index.levelByte(errorLines.get(0) + 1)));
	}

	@Test
	public void searchFindsExactlyTheMatchingLines() throws Exception {
		assertEquals("every error", errorLines.size(), searchText("ERROR"));
		int boom = searchText("boom");
		assertEquals(errorLines.size(), boom);
		assertEquals("text that is nowhere", 0, searchText("zzz-not-there"));
		int alice = 0;
		for (int i = 0; i < 1000; i++)
			if (line(i).contains("alice"))
				alice++;
		assertEquals("a plain word", alice, searchText("alice"));
	}

	@Test
	public void searchOptionsCaseWordAndRegex() throws Exception {
		int insensitive = searchText("boom");
		option("btnCase", true);
		assertEquals("match case: Boom is not boom", 0, searchText("Boom"));
		assertEquals(insensitive, searchText("boom"));
		option("btnCase", false);
		int fail = searchText("fail");
		option("btnWord", true);
		int word = searchText("fail");
		assertTrue("whole word finds fewer lines than the substring (" + word + " < " + fail + ")", word < fail);
		option("btnWord", false);
		option("btnRegex", true);
		int regex = searchText("user (alice|bob) logged");
		assertTrue(regex > 0);
		// a broken expression is reported and nothing is searched
		assertEquals(0, searchText("(unclosed"));
		assertTrue(((javax.swing.JLabel) field("lblStatus")).getText().length() > 0);
		option("btnRegex", false);
	}

	@Test
	public void levelFiltersAndJumps() throws Exception {
		assertTrue((Boolean) call("passes", 0));
		call("toggleLevel", LogLevel.ERROR, true); // only the errors
		assertTrue("error line passes", (Boolean) call("passes", errorLines.get(0)));
		assertFalse("an info line does not", (Boolean) call("passes", 0));
		call("toggleLevel", LogLevel.ERROR, true); // again: everything back
		assertTrue((Boolean) call("passes", 0));
		call("jumpToLevel", LogLevel.ERROR, true);
		assertTrue("the first jump lands on an error: " + currentLine(), errorLines.contains(currentLine()));
		int first = currentLine();
		call("jumpToLevel", LogLevel.ERROR, true);
		assertTrue(errorLines.contains(currentLine()) && currentLine() != first);
		call("jumpToLevel", LogLevel.ERROR, false);
		assertEquals(first, currentLine());
	}

	@Test
	public void nextAndPreviousMatchWalkTheResults() throws Exception {
		assertEquals(errorLines.size(), searchText("boom"));
		call("showLine", 0, true);
		call("jumpToMatch", true);
		assertEquals(errorLines.get(0).intValue(), currentLine());
		call("jumpToMatch", true);
		assertEquals(errorLines.get(1).intValue(), currentLine());
		call("jumpToMatch", false);
		assertEquals(errorLines.get(0).intValue(), currentLine());
		call("jumpToMatch", false);
		assertEquals("wraps to the last match", errorLines.get(errorLines.size() - 1).intValue(), currentLine());
	}

	@Test
	public void bookmarksAreToggledAndVisited() throws Exception {
		call("toggleBookmark", 10);
		call("toggleBookmark", 40);
		java.util.TreeSet<?> marks = field("bookmarks");
		assertEquals(2, marks.size());
		call("showLine", 0, true);
		call("jumpToBookmark", true);
		assertEquals("marks " + marks + " model " + ((JList<?>) field("mainList")).getModel().getSize(), 10, currentLine());
		call("jumpToBookmark", true);
		assertEquals(40, currentLine());
		call("jumpToBookmark", false);
		assertEquals(10, currentLine());
		call("toggleBookmark", 10);
		assertEquals(1, marks.size());
		call("toggleBookmark", 100000); // outside the file: ignored
		assertEquals(1, marks.size());
	}

	@Test
	public void zoomChangesTheFontWithinLimits() throws Exception {
		int before = (Integer) field("fontSize");
		call("zoom", 1);
		int bigger = (Integer) field("fontSize");
		assertTrue(bigger > before);
		for (int i = 0; i < 60; i++)
			call("zoom", -1);
		int smallest = (Integer) field("fontSize");
		assertTrue("lower limit " + smallest, smallest > 0 && smallest < before);
		for (int i = 0; i < 100; i++)
			call("zoom", 1);
		assertTrue("upper limit " + field("fontSize"), (Integer) field("fontSize") < 100);
	}

	@Test
	public void contextLinesAroundMatches() throws Exception {
		assertEquals(errorLines.size(), searchText("boom"));
		Field ctx = LogSearchFrame.class.getDeclaredField("context");
		ctx.setAccessible(true);
		ctx.setInt(frame, 2);
		Object grep = field("grepModel");
		Method reset = grep.getClass().getDeclaredMethod("reset");
		reset.setAccessible(true);
		edt(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				return null;
			}
		});
		reset.invoke(grep);
		call("refresh");
		Thread.sleep(300);
		flush();
		JList<?> list = field("matchList");
		assertTrue("context adds lines to the result list: " + list.getModel().getSize(), list.getModel().getSize() > errorLines.size());
	}

	@Test
	public void followsAFileThatGrows() throws Exception {
		LogFileIndex before = field("index");
		int lines = before.lineCount();
		try (FileOutputStream out = new FileOutputStream(log, true)) {
			out.write("2026-01-05 12:00:00,000 ERROR late failure one\n2026-01-05 12:00:01,000 INFO done\n".getBytes(StandardCharsets.UTF_8));
		}
		long end = System.currentTimeMillis() + 10000;
		LogFileIndex index = before;
		while (System.currentTimeMillis() < end && index.lineCount() < lines + 2) {
			call("checkFileChanges");
			index = field("index");
			Thread.sleep(100);
		}
		assertEquals("the two new lines are followed", lines + 2, ((LogFileIndex) field("index")).lineCount());
		assertEquals("and found by a new search", 1, searchText("late failure"));
		call("reloadFromDisk", true);
		waitIndexed(lines + 2);
	}

	@Test
	public void tabContentOffersTheViewerToTheTabs() throws Exception {
		gui.commons.tabs.TabContent tab = edt(new Callable<gui.commons.tabs.TabContent>() {
			@Override
			public gui.commons.tabs.TabContent call() {
				return LogSearchFrame.createTabContent(log);
			}
		});
		assertNotNull(tab.getComponent());
		assertEquals(log, tab.getFile());
		assertFalse(tab.isModified());
		assertTrue(tab.getTitle().contains("app.log"));
		assertTrue(tab.confirmClose(null));
		tab.onSelected();
		tab.dispose();
	}
}

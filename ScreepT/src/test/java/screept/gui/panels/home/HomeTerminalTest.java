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
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package screept.gui.panels.home;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static screept.gui.GuiTestSupport.call;
import static screept.gui.GuiTestSupport.edt;
import static screept.gui.GuiTestSupport.get;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JPanel;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import screept.gui.panels.home.HomeTerminalSession.Kind;
import screept.gui.panels.home.HomeTerminalSession.Line;

/**
 * The console of the home page: what the user types (editing keys, history, paste limits), what is sent to the assistant, how the
 * answers are revealed and how the view reacts to the keys and paints the rows. The view is attached to a panel that is never shown,
 * its key handlers are called directly (the global key dispatcher only acts on an active window).
 */
public class HomeTerminalTest {

	private HomeTerminalSession session;
	private JPanel panel;
	private HomeTerminalView view;

	@Before
	public void prepare() throws Exception {
		session = HomeTerminalSession.get();
		edt(new Runnable() {
			@Override
			public void run() {
				session.completeReveal();
				session.clearInput();
				session.clearScreen();
			}
		});
	}

	@After
	public void release() throws Exception {
		if (view != null) {
			edt(new Runnable() {
				@Override
				public void run() {
					view.detach();
				}
			});
		}
	}

	private static String lines(HomeTerminalSession s) {
		StringBuilder sb = new StringBuilder();
		for (Line l : s.lines()) {
			sb.append(l.kind).append(':').append(l.text).append('\n');
		}
		return sb.toString();
	}

	/** Sends the question and waits (bounded) until the answer has been delivered on the EDT */
	private void ask(final String question) throws Exception {
		final CountDownLatch delivered = new CountDownLatch(1);
		final java.util.concurrent.atomic.AtomicBoolean submitted = new java.util.concurrent.atomic.AtomicBoolean();
		final Runnable listener = new Runnable() {
			@Override
			public void run() {
				// the answer is delivered later on the EDT: only then, with the question sent, pending is back to 0
				if (submitted.get() && session.pending() == 0) {
					delivered.countDown();
				}
			}
		};
		edt(new Runnable() {
			@Override
			public void run() {
				session.addListener(listener);
				session.insert(question);
				session.submit();
				submitted.set(true);
			}
		});
		assertTrue("answer to " + question, delivered.await(10, TimeUnit.SECONDS));
		edt(new Runnable() {
			@Override
			public void run() {
				session.removeListener(listener);
			}
		});
	}

	private void attachView() throws Exception {
		edt(new Runnable() {
			@Override
			public void run() {
				panel = new JPanel();
				panel.setSize(800, 500);
				view = new HomeTerminalView(panel);
				view.attach();
			}
		});
	}

	private static KeyEvent pressed(Component c, int code, int modifiers) {
		return new KeyEvent(c, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), modifiers, code, KeyEvent.CHAR_UNDEFINED);
	}

	private static KeyEvent typed(Component c, char ch) {
		return new KeyEvent(c, KeyEvent.KEY_TYPED, System.currentTimeMillis(), 0, KeyEvent.VK_UNDEFINED, ch);
	}

	private boolean press(int code, int modifiers) throws Exception {
		return (Boolean) call(view, "handlePressed", pressed(panel, code, modifiers));
	}

	// ------------------------------------------------------------------ session: the typed line

	@Test
	public void typedTextIsEditedAtTheCaretAndStaysWithinTheLimit() throws Exception {
		edt(new Runnable() {
			@Override
			public void run() {
				session.insert("helo");
				session.moveCaret(-1);
				session.insert("l");
				assertEquals("hello", session.input());
				assertEquals(4, session.caret());
				session.backspace();
				assertEquals("helo", session.input());
				session.delete();
				assertEquals("hel", session.input());
				session.caretToStart();
				session.backspace(); // nothing before the caret
				session.moveCaret(-5);
				assertEquals(0, session.caret());
				session.insert("a\r\nb\tc");
				assertEquals("pasted line breaks become spaces", "a b chel", session.input());
				session.caretToEnd();
				assertEquals(8, session.caret());
				session.moveCaret(10);
				assertEquals(8, session.caret());
				session.clearInput();
				assertFalse(session.hasInput());
				StringBuilder big = new StringBuilder();
				for (int i = 0; i < 400; i++) {
					big.append('x');
				}
				session.insert(big.toString());
				assertEquals("the line is capped", 300, session.input().length());
				session.insert("more");
				assertEquals(300, session.input().length());
				session.clearInput();
				assertTrue(session.userInteracted());
			}
		});
	}

	@Test
	public void repeatedLetterOfAnEasterEggKeyIsRemovedOnlyWhenItIsTheSameLetter() throws Exception {
		edt(new Runnable() {
			@Override
			public void run() {
				session.insert("abc");
				session.insert("zzz");
				session.undoRepeatedTyping(3);
				assertEquals("abc", session.input());
				session.undoRepeatedTyping(3); // abc: not repeated
				assertEquals("abc", session.input());
				session.undoRepeatedTyping(9); // longer than the line
				assertEquals("abc", session.input());
				session.clearInput();
			}
		});
	}

	@Test
	public void historyRecallsPreviousQuestionsAndKeepsOneCopyOfRepeats() throws Exception {
		ask("about");
		ask("about");
		ask("version");
		edt(new Runnable() {
			@Override
			public void run() {
				session.historyUp();
				assertEquals("version", session.input());
				session.historyUp();
				assertEquals("about", session.input());
				session.historyUp(); // already the oldest
				assertEquals("about", session.input());
				session.historyDown();
				assertEquals("version", session.input());
				session.historyDown();
				assertEquals("past the newest the line is empty", "", session.input());
				session.historyDown();
				assertEquals("", session.input());
				session.clearInput();
			}
		});
	}

	@Test
	public void questionsGetAnAnswerAndTheLinesAreRevealedLikeATypewriter() throws Exception {
		ask("about");
		String all = lines(session);
		assertTrue(all, all.contains("INPUT:" + HomeTerminalSession.PROMPT + "about"));
		assertTrue(all, all.contains("ScreepT assistant"));
		assertEquals(0, session.pending());
		// the answer lines start hidden and are shown a few characters at a time
		Line first = null;
		for (Line l : session.lines()) {
			if (l.kind != Kind.INPUT) {
				first = l;
				break;
			}
		}
		assertNotNull(first);
		assertTrue(session.hasUnrevealed() || first.isComplete());
		int guard = 0;
		while (session.hasUnrevealed() && guard++ < 5000) {
			assertTrue(session.advanceReveal(5));
		}
		assertFalse(session.hasUnrevealed());
		assertFalse("nothing left to reveal", session.advanceReveal(5));
		assertEquals(first.text, first.visibleText());
		assertEquals("", new Line(Kind.OUTPUT, "hello", true).visibleText());
		assertEquals("hel", partial("hello", 3));
	}

	private static String partial(String text, int chars) {
		Line l = new Line(Kind.OUTPUT, text, true);
		l.shown = chars;
		assertFalse(l.isComplete());
		return l.visibleText();
	}

	@Test
	public void clearCommandWipesTheScreenAndBlankInputIsIgnored() throws Exception {
		ask("about");
		assertFalse(session.lines().isEmpty());
		ask("clear");
		for (Line l : session.lines()) {
			assertFalse("only the answers after clear remain: " + l.text, l.text.contains("ScreepT assistant - offline"));
		}
		int before = session.lines().size();
		edt(new Runnable() {
			@Override
			public void run() {
				session.insert("   ");
				session.submit();
			}
		});
		assertEquals("a blank line adds nothing", before, session.lines().size());
		assertFalse(session.hasInput());
	}

	@Test
	public void tooManyQuestionsInARowAreRefused() throws Exception {
		edt(new Runnable() {
			@Override
			public void run() {
				for (int i = 0; i < 6; i++) {
					session.insert("about " + i);
					session.submit();
				}
				assertTrue(lines(session), lines(session).contains("Too many questions in a row"));
			}
		});
		waitIdle(); // let the accepted ones finish before the next test
	}

	private void waitIdle() throws Exception {
		for (int i = 0; i < 100 && session.pending() > 0; i++) {
			Thread.sleep(50);
			screept.gui.GuiTestSupport.flushEdt();
		}
	}

	@Test
	public void registeredActionsRunOnTheEdtAndUnknownOnesAreRefused() throws Exception {
		final CountDownLatch ran = new CountDownLatch(1);
		final AtomicInteger onEdt = new AtomicInteger();
		session.registerAction("test-action", new Runnable() {
			@Override
			public void run() {
				onEdt.set(javax.swing.SwingUtilities.isEventDispatchThread() ? 1 : 2);
				ran.countDown();
			}
		});
		assertTrue(session.launchAction("test-action"));
		assertTrue(ran.await(5, TimeUnit.SECONDS));
		assertEquals(1, onEdt.get());
		assertFalse(session.launchAction("no-such-action"));
	}

	// ------------------------------------------------------------------ view

	@Test
	public void viewTypesSubmitsAndEditsThroughItsKeyHandlers() throws Exception {
		attachView();
		assertFalse("ignored control characters", (Boolean) call(view, "handleTyped", typed(panel, '\n')));
		assertFalse("ctrl combinations are not text", (Boolean) call(view, "handleTyped",
				new KeyEvent(panel, KeyEvent.KEY_TYPED, 0, KeyEvent.CTRL_DOWN_MASK, KeyEvent.VK_UNDEFINED, 'a')));
		assertTrue((Boolean) call(view, "handleTyped", typed(panel, 'h')));
		assertTrue((Boolean) call(view, "handleTyped", typed(panel, 'x')));
		assertEquals("hx", session.input());
		assertTrue(press(KeyEvent.VK_BACK_SPACE, 0));
		assertTrue(press(KeyEvent.VK_LEFT, 0));
		assertTrue(press(KeyEvent.VK_RIGHT, 0));
		assertTrue(press(KeyEvent.VK_HOME, 0));
		assertTrue(press(KeyEvent.VK_DELETE, 0));
		assertEquals("", session.input());
		assertFalse("with nothing typed the editing keys are left to the application", press(KeyEvent.VK_BACK_SPACE, 0));
		assertFalse(press(KeyEvent.VK_ENTER, 0));
		assertFalse(press(KeyEvent.VK_ESCAPE, 0));
		for (char c : "about".toCharArray()) {
			call(view, "handleTyped", typed(panel, c));
		}
		assertTrue(press(KeyEvent.VK_END, 0));
		assertTrue(press(KeyEvent.VK_ESCAPE, 0));
		assertEquals("Escape clears the line", "", session.input());
		for (char c : "version".toCharArray()) {
			call(view, "handleTyped", typed(panel, c));
		}
		assertTrue(press(KeyEvent.VK_U, KeyEvent.CTRL_DOWN_MASK));
		assertEquals("Ctrl+U clears the line", "", session.input());
		assertFalse("alt and meta are not for the console", press(KeyEvent.VK_A, KeyEvent.ALT_DOWN_MASK));
		assertFalse(press(KeyEvent.VK_F1, 0));
		assertFalse(press(KeyEvent.VK_X, KeyEvent.CTRL_DOWN_MASK));
		// Enter sends the question
		for (char c : "about".toCharArray()) {
			call(view, "handleTyped", typed(panel, c));
		}
		assertTrue(press(KeyEvent.VK_ENTER, 0));
		assertTrue(lines(session).contains("INPUT:" + HomeTerminalSession.PROMPT + "about"));
		waitIdle();
		// Ctrl+L clears the screen
		assertTrue(press(KeyEvent.VK_L, KeyEvent.CTRL_DOWN_MASK));
		assertTrue(session.lines().isEmpty());
		// history keys are taken once the user interacted
		assertTrue(press(KeyEvent.VK_UP, 0));
		assertEquals("about", session.input());
		assertTrue(press(KeyEvent.VK_DOWN, 0));
		assertEquals("", session.input());
	}

	@Test
	public void viewFadesInWhileTypingScrollsAndPaintsTheRows() throws Exception {
		attachView();
		set(view, "overlay", 0f); // the session is shared: the console may have been left open by another test
		assertFalse("page keys are not taken while the console is hidden", press(KeyEvent.VK_PAGE_UP, 0));
		edt(new Runnable() {
			@Override
			public void run() {
				session.insert("typing");
				for (int i = 0; i < 12; i++) {
					view.tick();
				}
			}
		});
		assertEquals(1f, view.overlayAmount(), 0f);
		// a long answer: many wrapped rows so the console can scroll
		for (int i = 0; i < 60; i++) {
			final int n = i;
			edt(new Runnable() {
				@Override
				public void run() {
					session.insert("x");
					session.clearInput();
					call2(n);
				}

				private void call2(int n) {
					try {
						call(session, "append", Kind.OUTPUT, "line number " + n + " of a long answer that has to be wrapped " + "word ", false);
					} catch (Exception e) {
						throw new AssertionError(e);
					}
				}
			});
		}
		BufferedImage image = new BufferedImage(800, 500, BufferedImage.TYPE_INT_ARGB);
		java.awt.Graphics2D g = image.createGraphics();
		view.paint(g, 800, 500);
		assertNotNull("the console box is known after painting", view.promptBounds());
		assertTrue(press(KeyEvent.VK_PAGE_UP, 0));
		view.paint(g, 800, 500); // scrolled: shows the hint
		assertTrue(press(KeyEvent.VK_PAGE_DOWN, 0));
		view.onWheel(-2);
		view.onWheel(3);
		view.paint(g, 300, 200); // narrow window: very few columns
		g.dispose();
		// after a pause without typing the console fades out and the hint line returns
		edt(new Runnable() {
			@Override
			public void run() {
				session.clearInput();
				session.completeReveal();
			}
		});
		set(view, "lastInteraction", 0L);
		for (int i = 0; i < 60; i++) {
			view.tick();
		}
		assertEquals(0f, view.overlayAmount(), 0f);
		Graphics2DHolder.paintHint(view);
		assertNotNull(view.promptBounds());
	}

	private static void set(Object target, String name, Object value) throws Exception {
		screept.gui.GuiTestSupport.set(target, name, value);
	}

	/** Paints the faded console (hint only) */
	private static final class Graphics2DHolder {
		static void paintHint(HomeTerminalView view) {
			BufferedImage image = new BufferedImage(800, 500, BufferedImage.TYPE_INT_ARGB);
			java.awt.Graphics2D g = image.createGraphics();
			view.paint(g, 800, 500);
			g.dispose();
		}
	}

	@Test
	public void wrapKeepsHangingIndentAndCutsLongWords() throws Exception {
		attachView();
		List<Object> rows = new ArrayList<Object>();
		call(view, "wrap", rows, "  - alpha beta gamma delta epsilon zeta eta theta", Kind.OUTPUT, 20, 0);
		assertTrue(rows.size() >= 3);
		assertEquals("  - alpha beta gamma", get(rows.get(0), "text"));
		assertTrue("continuation rows are indented", ((String) get(rows.get(1), "text")).startsWith("    "));
		List<Object> longWord = new ArrayList<Object>();
		call(view, "wrap", longWord, "abcdefghijklmnopqrstuvwxyz0123456789", Kind.OUTPUT, 10, 0);
		assertEquals(4, longWord.size());
		assertEquals("abcdefghij", get(longWord.get(0), "text"));
		List<Object> prompt = new ArrayList<Object>();
		call(view, "wrap", prompt, HomeTerminalSession.PROMPT + "hi", Kind.INPUT, 80, HomeTerminalSession.PROMPT.length());
		assertEquals(HomeTerminalSession.PROMPT.length(), get(prompt.get(0), "promptChars"));
	}

	@Test
	public void attachAndDetachAreIdempotentAndRevealEverythingPending() throws Exception {
		attachView();
		edt(new Runnable() {
			@Override
			public void run() {
				view.attach(); // second attach changes nothing
				call3();
				view.detach();
				view.detach();
			}

			private void call3() {
				try {
					call(session, "append", Kind.OUTPUT, "being typed", true);
				} catch (Exception e) {
					throw new AssertionError(e);
				}
			}
		});
		assertFalse("leaving the page shows the pending answers at once", session.hasUnrevealed());
	}
}

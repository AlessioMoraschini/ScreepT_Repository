/**
 *
 * =========================================================================================
 *  Copyright (C) 2019-2026
 *
 *  AM-Design-Development - (Alessio Moraschini) - All Rights Reserved
 * =========================================================================================
 *
 * You should have received a copy of the license with this file.
 * If not, please write to: info@am-design-development.com, or visit : https://www.am-design-development.com
 */
package screept.gui.easteregg;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static screept.gui.GuiTestSupport.call;
import static screept.gui.GuiTestSupport.edt;
import static screept.gui.GuiTestSupport.get;
import static screept.gui.GuiTestSupport.set;

import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.concurrent.Callable;

import javax.swing.JFrame;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import various.common.light.easteregg.scores.EggScores;

/**
 * The game logic of two easter eggs, driven without their timers or a visible window: Bug Squasher (spawning, squashing, score,
 * round end, record) and Code Typist (lines, mistakes, backspace, accuracy, restart). The scores go to a test node, never to the
 * real leaderboard.
 */
public class EasterEggGamesTest {

	private JFrame frame;

	@BeforeClass
	public static void useTestScores() {
		Assume.assumeFalse(GraphicsEnvironment.isHeadless());
		EggScores.useNode("screept/test/eggs/" + System.nanoTime());
	}

	@AfterClass
	public static void restoreScores() {
		EggScores.useNode(null);
	}

	@Before
	public void clean() {
		EggScores.clear(EggScores.BUG_SQUASHER);
		EggScores.clear(EggScores.CODE_TYPIST);
	}

	@After
	public void close() throws Exception {
		if (frame != null) {
			edt(new Runnable() {
				@Override
				public void run() {
					frame.dispose();
				}
			});
		}
	}

	private JFrame open(final Class<? extends JFrame> type) throws Exception {
		frame = edt(new Callable<JFrame>() {
			@Override
			public JFrame call() throws Exception {
				Constructor<? extends JFrame> c = type.getDeclaredConstructor();
				c.setAccessible(true);
				return c.newInstance();
			}
		});
		return frame;
	}

	private static void paint(final Component c) throws Exception {
		edt(new Runnable() {
			@Override
			public void run() {
				c.setSize(c.getPreferredSize());
				c.paint(new BufferedImage(c.getWidth(), c.getHeight(), BufferedImage.TYPE_INT_ARGB).createGraphics());
			}
		});
	}

	// ------------------------------------------------------------------ Bug Squasher

	private static List<?> bugs(Object arena) throws Exception {
		return (List<?>) get(arena, "bugs");
	}

	private static int score(Object arena) throws Exception {
		return (Integer) get(arena, "score");
	}

	@Test
	public void bugSquasherScoresSquashedBugsAndPenalisesMisses() throws Exception {
		open(BugSquasherFrame.class);
		final Object arena = get(frame, "arena");
		paint((Component) arena); // title screen
		call(arena, "click", 10, 10); // the first click starts the round
		assertTrue((Boolean) get(arena, "running"));
		assertEquals(3, bugs(arena).size());
		paint((Component) arena);
		// a miss in the HUD corner costs a point, but never goes below zero
		call(arena, "click", 1, 1);
		assertEquals(0, score(arena));
		Object bug = bugs(arena).get(bugs(arena).size() - 1); // the last one is the first to be hit
		boolean golden = (Boolean) get(bug, "golden");
		call(arena, "click", (int) Math.round((Double) get(bug, "x")), (int) Math.round((Double) get(bug, "y")));
		assertEquals(golden ? 5 : 1, score(arena));
		assertEquals(2, bugs(arena).size());
		assertEquals(1, get(arena, "bugsSquashed"));
		call(arena, "click", 1, 1);
		assertEquals((golden ? 5 : 1) - 1, score(arena));
		// the splat of the dead bug fades away step by step
		assertEquals(1, ((List<?>) get(arena, "splats")).size());
		for (int i = 0; i < 100; i++) {
			call(arena, "step");
		}
		assertTrue(((List<?>) get(arena, "splats")).isEmpty());
	}

	@Test
	public void bugsSpawnUpToTheLimitAndBounceOffTheEdges() throws Exception {
		open(BugSquasherFrame.class);
		Object arena = get(frame, "arena");
		call(arena, "click", 10, 10);
		set(arena, "startMs", System.currentTimeMillis() - 5000);
		for (int i = 0; i < 40; i++) {
			set(arena, "lastSpawnMs", 0L);
			call(arena, "step");
		}
		assertEquals("never more than the limit", 16, bugs(arena).size());
		Object bug = bugs(arena).get(0);
		set(bug, "x", -50.0);
		set(bug, "y", 2000.0);
		call(arena, "step");
		assertTrue("pushed back inside", (Double) get(bug, "x") >= (Integer) get(bug, "radius") - 1.5);
		assertTrue((Double) get(bug, "y") <= 480);
		paint((Component) arena);
	}

	@Test
	public void bugSquasherRoundEndsWithRecordAndCanBeRestarted() throws Exception {
		open(BugSquasherFrame.class);
		Object arena = get(frame, "arena");
		call(arena, "click", 10, 10);
		for (int i = 0; i < 3 && !bugs(arena).isEmpty(); i++) {
			Object bug = bugs(arena).get(bugs(arena).size() - 1);
			call(arena, "click", (int) Math.round((Double) get(bug, "x")), (int) Math.round((Double) get(bug, "y")));
		}
		int played = score(arena);
		assertTrue(played >= 3);
		set(arena, "startMs", System.currentTimeMillis() - 30100); // 30 s are over
		call(arena, "step");
		assertFalse((Boolean) get(arena, "running"));
		assertTrue((Boolean) get(arena, "finished"));
		assertTrue(bugs(arena).isEmpty());
		assertTrue("first score of the test node is a record", (Boolean) get(arena, "newRecord"));
		assertEquals(played, EggScores.best(EggScores.BUG_SQUASHER));
		paint((Component) arena); // end screen
		call(arena, "click", 5, 5); // a click soon after the end does not restart: the player may still be clicking
		call(arena, "click", 5, 5);
		set(arena, "startMs", System.currentTimeMillis() - 31000);
		call(arena, "click", 5, 5);
		assertTrue((Boolean) get(arena, "running"));
		assertEquals(0, score(arena));
	}

	// ------------------------------------------------------------------ Code Typist

	private static void type(Object board, String text) throws Exception {
		for (char c : text.toCharArray()) {
			call(board, "handleTyped", c);
		}
	}

	@Test
	public void codeTypistCountsMistakesBackspaceAndFinishesWithAccuracy() throws Exception {
		open(CodeTypistFrame.class);
		Object board = get(frame, "board");
		paint((Component) board); // start screen
		type(board, "x"); // ignored until Enter or space
		assertFalse((Boolean) get(board, "running"));
		type(board, "\n");
		assertTrue((Boolean) get(board, "running"));
		List<?> lines = (List<?>) get(board, "lines");
		assertEquals(5, lines.size());
		paint((Component) board);

		String first = (String) lines.get(0);
		char wrong = first.charAt(0) == '~' ? '^' : '~';
		type(board, String.valueOf(wrong)); // a mistake...
		assertEquals(1, get(board, "mistakes"));
		type(board, "\b"); // ...fixed with backspace
		type(board, "\b"); // nothing left to delete
		type(board, "\u0001"); // control characters do nothing
		assertEquals(0, ((StringBuilder) get(board, "typed")).length());
		type(board, first);
		assertEquals("the line was completed", 1, get(board, "round"));
		for (int i = 1; i < 5; i++) {
			paint((Component) board);
			type(board, (String) lines.get(i));
		}
		assertFalse((Boolean) get(board, "running"));
		assertTrue((Boolean) get(board, "finished"));
		int keystrokes = (Integer) get(board, "keystrokes");
		int total = 0;
		for (Object l : lines) {
			total += ((String) l).length();
		}
		assertEquals(total + 1, keystrokes);
		assertEquals(total, get(board, "totalChars"));
		int accuracy = (Integer) call(board, "accuracy");
		assertEquals(Math.round(100.0 * (keystrokes - 1) / keystrokes), accuracy);
		assertTrue(accuracy < 100);
		assertTrue((Integer) call(board, "wpm") > 0);
		assertTrue("the run was saved as the first record", (Boolean) get(board, "record"));
		assertEquals(call(board, "wpm"), EggScores.best(EggScores.CODE_TYPIST));
		paint((Component) board); // result screen
		type(board, " "); // another round
		assertTrue((Boolean) get(board, "running"));
		assertEquals(0, get(board, "round"));
		assertEquals(0, get(board, "mistakes"));
		assertNotNull(get(board, "lines"));
	}

	@Test
	public void codeTypistIgnoresKeysBeyondTheEndOfTheLine() throws Exception {
		open(CodeTypistFrame.class);
		Object board = get(frame, "board");
		type(board, " ");
		String first = (String) ((List<?>) get(board, "lines")).get(0);
		type(board, first.substring(0, first.length() - 1));
		assertEquals(first.length() - 1, ((StringBuilder) get(board, "typed")).length());
		assertEquals(0, get(board, "round"));
		assertEquals(0, get(board, "mistakes"));
	}
}

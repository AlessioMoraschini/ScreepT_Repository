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

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/** Easter egg: typing speed test with five lines of programmer life, words per minute and accuracy at the end. */
public final class CodeTypistFrame extends EggFrame {

	private static final long serialVersionUID = 1L;

	private static final String GAME_ID = "codeTypist";
	private static final int ROUNDS = 5;
	private static final int AREA_W = 760;
	private static final int AREA_H = 300;

	public static void open() {
		if (focusIfOpen(CodeTypistFrame.class)) {
			return;
		}
		SwingUtilities.invokeLater(() -> new CodeTypistFrame().setVisible(true));
	}

	private final Board board = new Board();

	private CodeTypistFrame() {
		super("Code Typist - ScreepT easter egg", AREA_W, AREA_H);
		add(board);
		pack();
		board.requestFocusInWindow();
	}

	private final class Board extends JPanel {

		private static final long serialVersionUID = 1L;

		private final List<String> lines = new ArrayList<>();
		private final StringBuilder typed = new StringBuilder();
		private int round;
		private boolean running;
		private boolean finished;
		private long startMs;
		private long totalMs;
		private int keystrokes;
		private int mistakes;
		private int totalChars;
		private int best = bestScore(GAME_ID);
		private boolean record;

		Board() {
			setPreferredSize(new Dimension(AREA_W, AREA_H));
			setBackground(BACKGROUND);
			setFocusable(true);
			addKeyListener(new KeyAdapter() {
				@Override
				public void keyTyped(KeyEvent e) {
					handleTyped(e.getKeyChar());
				}
			});
		}

		private void begin() {
			lines.clear();
			List<String> pool = new ArrayList<>();
			Collections.addAll(pool, ScreepTQuotes.TYPING);
			Collections.shuffle(pool);
			lines.addAll(pool.subList(0, ROUNDS));
			round = 0;
			keystrokes = 0;
			mistakes = 0;
			totalChars = 0;
			totalMs = 0;
			finished = false;
			running = true;
			typed.setLength(0);
			startMs = 0;
		}

		private void handleTyped(char c) {
			if (!running) {
				if (c == '\n' || c == ' ') {
					begin();
					repaint();
				}
				return;
			}
			if (c == '\b') {
				if (typed.length() > 0) {
					typed.setLength(typed.length() - 1);
				}
				repaint();
				return;
			}
			if (c < 32) {
				return;
			}
			if (startMs == 0) {
				startMs = System.currentTimeMillis();
			}
			String target = lines.get(round);
			if (typed.length() < target.length()) {
				keystrokes++;
				if (target.charAt(typed.length()) != c) {
					mistakes++;
				}
				typed.append(c);
			}
			if (typed.toString().equals(target)) {
				totalMs += System.currentTimeMillis() - startMs;
				totalChars += target.length();
				round++;
				typed.setLength(0);
				startMs = 0;
				if (round >= ROUNDS) {
					finish();
				}
			}
			repaint();
		}

		private void finish() {
			running = false;
			finished = true;
			record = saveBestScore(GAME_ID, wpm());
			best = Math.max(best, wpm());
		}

		private int wpm() {
			double minutes = Math.max(0.01, totalMs / 60000.0);
			return (int) Math.round(totalChars / 5.0 / minutes);
		}

		private int accuracy() {
			return keystrokes == 0 ? 100 : (int) Math.round(100.0 * (keystrokes - mistakes) / keystrokes);
		}

		@Override
		protected void paintComponent(Graphics graphics) {
			super.paintComponent(graphics);
			Graphics2D g = (Graphics2D) graphics.create();
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

			g.setFont(mono(java.awt.Font.BOLD, 20));
			g.setColor(TEAL);
			g.drawString("CODE TYPIST", 28, 40);
			g.setFont(mono(java.awt.Font.PLAIN, 15));
			g.setColor(MUTED);
			g.drawString(running ? "Line " + (round + 1) + "/" + ROUNDS : "Best: " + best + " WPM", AREA_W - 150, 40);

			if (!running) {
				g.setFont(mono(java.awt.Font.BOLD, 28));
				g.setColor(finished ? AMBER : TEXT);
				String headline = finished ? wpm() + " WPM  -  " + accuracy() + "% accuracy" : "Type the lines exactly as shown";
				g.drawString(headline, (AREA_W - g.getFontMetrics().stringWidth(headline)) / 2, 130);
				g.setFont(mono(java.awt.Font.PLAIN, 16));
				g.setColor(MUTED);
				String sub = finished ? (record ? "New record! Press Enter to try again, ESC to leave" : "Press Enter to try again, ESC to leave")
						: "Five lines, the clock starts at your first key. Press Enter to begin.";
				g.drawString(sub, (AREA_W - g.getFontMetrics().stringWidth(sub)) / 2, 175);
				g.dispose();
				return;
			}

			String target = lines.get(round);
			g.setFont(mono(java.awt.Font.PLAIN, 24));
			FontMetrics fm = g.getFontMetrics();
			int x = (AREA_W - fm.stringWidth(target)) / 2;
			int y = 140;
			int cursorX = x;
			for (int i = 0; i < target.length(); i++) {
				String character = String.valueOf(target.charAt(i));
				if (i < typed.length()) {
					boolean right = typed.charAt(i) == target.charAt(i);
					g.setColor(right ? GREEN : RED);
					if (!right && target.charAt(i) == ' ') {
						g.fillRect(x, y - fm.getAscent() + 4, fm.charWidth(' '), fm.getHeight() - 4);
					}
				} else {
					g.setColor(MUTED);
				}
				g.drawString(character, x, y);
				x += fm.charWidth(target.charAt(i));
				if (i + 1 == typed.length()) {
					cursorX = x;
				}
			}
			if (typed.length() == 0) {
				cursorX = (AREA_W - fm.stringWidth(target)) / 2;
			}
			if ((System.currentTimeMillis() / 500) % 2 == 0) {
				g.setColor(TEXT);
				g.fillRect(cursorX, y - fm.getAscent() + 4, 2, fm.getHeight() - 4);
			}
			g.setFont(mono(java.awt.Font.PLAIN, 14));
			g.setColor(MUTED);
			g.drawString("Backspace fixes mistakes. Mistakes so far: " + mistakes, 28, AREA_H - 24);
			g.dispose();
			repaint(500);
		}
	}
}

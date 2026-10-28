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

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/** Easter egg: squash the bugs crawling over the screen for 30 seconds. Golden bugs are fast and worth five. */
public final class BugSquasherFrame extends EggFrame {

	private static final long serialVersionUID = 1L;

	private static final String GAME_ID = "bugSquasher";
	private static final int AREA_W = 720;
	private static final int AREA_H = 480;
	private static final long ROUND_MS = 30000;
	private static final int MAX_BUGS = 16;

	public static void open() {
		if (focusIfOpen(BugSquasherFrame.class)) {
			return;
		}
		SwingUtilities.invokeLater(() -> new BugSquasherFrame().setVisible(true));
	}

	private final Arena arena = new Arena();

	private BugSquasherFrame() {
		super("Bug Squasher - ScreepT easter egg", AREA_W, AREA_H);
		add(arena);
		pack();
	}

	@Override
	protected void onClosed() {
		arena.stop();
	}

	private static final class Bug {
		double x, y, angle, speed, phase;
		boolean golden;
		int radius = 15;
	}

	private static final class Splat {
		final double x, y;
		final boolean golden;
		float life = 1f;

		Splat(double x, double y, boolean golden) {
			this.x = x;
			this.y = y;
			this.golden = golden;
		}
	}

	private final class Arena extends JPanel {

		private static final long serialVersionUID = 1L;

		private final Random random = new Random();
		private final List<Bug> bugs = new ArrayList<>();
		private final List<Splat> splats = new ArrayList<>();
		private final Timer timer = new Timer(16, e -> step());

		private long startMs;
		private long lastSpawnMs;
		private int score;
		private int best = bestScore(GAME_ID);
		private boolean running;
		private boolean finished;
		private boolean newRecord;
		private int bugsSquashed;

		Arena() {
			setPreferredSize(new Dimension(AREA_W, AREA_H));
			setBackground(BACKGROUND);
			setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
			addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					click(e.getX(), e.getY());
				}
			});
			timer.start();
		}

		void stop() {
			timer.stop();
		}

		private void start() {
			bugs.clear();
			splats.clear();
			score = 0;
			bugsSquashed = 0;
			newRecord = false;
			finished = false;
			running = true;
			startMs = System.currentTimeMillis();
			lastSpawnMs = 0;
			for (int i = 0; i < 3; i++) {
				spawn();
			}
		}

		private void spawn() {
			Bug bug = new Bug();
			bug.golden = random.nextInt(12) == 0;
			bug.x = 30 + random.nextInt(AREA_W - 60);
			bug.y = 60 + random.nextInt(AREA_H - 100);
			bug.angle = random.nextDouble() * Math.PI * 2;
			bug.speed = (bug.golden ? 3.6 : 1.1) + random.nextDouble() * 1.2 + (System.currentTimeMillis() - startMs) / 30000.0;
			bug.phase = random.nextDouble() * 10;
			bugs.add(bug);
		}

		private void step() {
			if (running) {
				long elapsed = System.currentTimeMillis() - startMs;
				if (elapsed >= ROUND_MS) {
					running = false;
					finished = true;
					bugs.clear();
					newRecord = saveBestScore(GAME_ID, score);
					best = Math.max(best, score);
				} else {
					long interval = Math.max(260, 900 - elapsed / 45);
					if (elapsed - lastSpawnMs >= interval && bugs.size() < MAX_BUGS) {
						spawn();
						lastSpawnMs = elapsed;
					}
					for (Bug bug : bugs) {
						move(bug);
					}
				}
			}
			for (Iterator<Splat> it = splats.iterator(); it.hasNext();) {
				Splat splat = it.next();
				splat.life -= 0.012f;
				if (splat.life <= 0) {
					it.remove();
				}
			}
			repaint();
		}

		private void move(Bug bug) {
			bug.phase += 0.35;
			bug.angle += (random.nextDouble() - 0.5) * 0.25;
			bug.x += Math.cos(bug.angle) * bug.speed;
			bug.y += Math.sin(bug.angle) * bug.speed;
			if (bug.x < bug.radius || bug.x > AREA_W - bug.radius) {
				bug.angle = Math.PI - bug.angle;
				bug.x = Math.max(bug.radius, Math.min(AREA_W - bug.radius, bug.x));
			}
			if (bug.y < 44 + bug.radius || bug.y > AREA_H - bug.radius) {
				bug.angle = -bug.angle;
				bug.y = Math.max(44 + bug.radius, Math.min(AREA_H - bug.radius, bug.y));
			}
		}

		private void click(int x, int y) {
			if (!running) {
				if (!finished || System.currentTimeMillis() - startMs > ROUND_MS + 500 || startMs == 0) {
					start();
				}
				return;
			}
			for (int i = bugs.size() - 1; i >= 0; i--) {
				Bug bug = bugs.get(i);
				if (Math.hypot(bug.x - x, bug.y - y) <= bug.radius + 7) {
					bugs.remove(i);
					splats.add(new Splat(bug.x, bug.y, bug.golden));
					score += bug.golden ? 5 : 1;
					bugsSquashed++;
					return;
				}
			}
			score = Math.max(0, score - 1);
		}

		@Override
		protected void paintComponent(Graphics graphics) {
			super.paintComponent(graphics);
			Graphics2D g = (Graphics2D) graphics.create();
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

			// terminal grid
			g.setColor(new Color(0x1B2229));
			for (int x = 0; x < AREA_W; x += 40) {
				g.drawLine(x, 44, x, AREA_H);
			}
			for (int y = 44; y < AREA_H; y += 40) {
				g.drawLine(0, y, AREA_W, y);
			}

			for (Splat splat : splats) {
				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0f, splat.life * 0.7f)));
				g.setColor(splat.golden ? AMBER : GREEN);
				int r = (int) (14 + (1f - splat.life) * 16);
				g.fillOval((int) splat.x - r, (int) splat.y - r, r * 2, r * 2);
			}
			g.setComposite(AlphaComposite.SrcOver);
			for (Bug bug : bugs) {
				paintBug(g, bug);
			}
			paintHud(g);
			g.dispose();
		}

		private void paintBug(Graphics2D g0, Bug bug) {
			Graphics2D g = (Graphics2D) g0.create();
			AffineTransform transform = new AffineTransform();
			transform.translate(bug.x, bug.y);
			transform.rotate(bug.angle);
			g.transform(transform);
			Color body = bug.golden ? AMBER : new Color(0xE0605A);
			g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			g.setColor(body.darker());
			for (int i = -1; i <= 1; i++) {
				double wobble = Math.sin(bug.phase + i) * 4;
				g.drawLine(i * 6, -4, i * 6 - 3, (int) (-14 - wobble));
				g.drawLine(i * 6, 4, i * 6 - 3, (int) (14 + wobble));
			}
			g.setColor(body);
			g.fillOval(-14, -8, 24, 16);
			g.setColor(body.brighter());
			g.fillOval(8, -6, 12, 12);
			g.setColor(body.darker());
			g.drawLine(-2, -7, -2, 7);
			g.drawLine(17, -3, 24, -8);
			g.drawLine(17, 3, 24, 8);
			g.setColor(Color.BLACK);
			g.fillOval(15, -4, 3, 3);
			g.fillOval(15, 1, 3, 3);
			g.dispose();
		}

		private void paintHud(Graphics2D g) {
			g.setColor(PANEL);
			g.fillRect(0, 0, AREA_W, 44);
			g.setColor(TEAL);
			g.drawLine(0, 44, AREA_W, 44);
			g.setFont(mono(java.awt.Font.BOLD, 18));
			g.setColor(TEXT);
			g.drawString("Squashed: " + score, 16, 29);
			long left = running ? Math.max(0, ROUND_MS - (System.currentTimeMillis() - startMs)) : (finished ? 0 : ROUND_MS);
			g.setColor(left < 6000 && running ? RED : TEXT);
			g.drawString(String.format("Time: %02d", (left + 999) / 1000), AREA_W / 2 - 40, 29);
			g.setColor(AMBER);
			g.drawString("Best: " + best, AREA_W - 140, 29);

			if (!running) {
				g.setColor(new Color(0, 0, 0, 170));
				g.fillRect(0, 44, AREA_W, AREA_H - 44);
				g.setColor(TEAL);
				g.setFont(mono(java.awt.Font.BOLD, 34));
				String title = finished ? "Release candidate cleared!" : "BUG SQUASHER";
				g.drawString(title, (AREA_W - g.getFontMetrics().stringWidth(title)) / 2, 190);
				g.setFont(mono(java.awt.Font.PLAIN, 18));
				g.setColor(TEXT);
				String line1 = finished ? bugsSquashed + " bugs squashed, score " + score + (newRecord ? "  - NEW RECORD!" : "")
						: "Click the bugs before they reach production.";
				g.drawString(line1, (AREA_W - g.getFontMetrics().stringWidth(line1)) / 2, 235);
				g.setColor(MUTED);
				String line2 = finished ? "Click to squash again, ESC to leave" : "Golden bugs are fast and worth 5. Missing costs a point. Click to start.";
				g.drawString(line2, (AREA_W - g.getFontMetrics().stringWidth(line2)) / 2, 270);
			}
		}
	}
}

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
package screept.gui.frames.about;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JComponent;
import javax.swing.Timer;

import screept.gui.frames.about.CatSprites.Cat;

/**
 * A small egg that drops in, wobbles, cracks and opens: Nano and Feo pop out of it and stay there, blinking.
 * Pixel art on a fixed grid, painted only inside the component: it can never go out of the space it is given.
 */
public final class EggHatchAnimation extends JComponent {

	private static final long serialVersionUID = 1L;

	/** Size of the grid in pixel-art units */
	private static final int COLUMNS = 28;
	private static final int ROWS = 20;

	private static final int DROP_END = 8;
	private static final int WOBBLE_END = 28;
	private static final int CRACK_END = 40;
	private static final int BURST_END = 52;
	private static final int RISE_END = 84;

	private static final int EGG_CENTER_X = 14;
	private static final int EGG_TOP = 8;
	private static final int EGG_HEIGHT = 12;
	private static final int RIM = 13;

	private static final Color OUTLINE = new Color(0x3A, 0x2E, 0x24);
	private static final Color SHELL = new Color(0xF4, 0xEC, 0xD8);
	private static final Color SHELL_SHADE = new Color(0xD9, 0xCB, 0xA8);
	private static final Color SPECKLE = new Color(0xC9, 0xA8, 0x6B);
	private static final Color SPARK = new Color(0xF2, 0xC5, 0x5C);

	private final int unit;
	private final Timer timer;
	private int tick = -1;

	public EggHatchAnimation(int unit) {
		this.unit = unit;
		setPreferredSize(new Dimension(COLUMNS * unit, ROWS * unit));
		setOpaque(false);
		timer = new Timer(50, e -> step());
	}

	/** Starts the show from the beginning */
	public void play() {
		tick = 0;
		if (!timer.isRunning()) {
			timer.start();
		}
		repaint();
	}

	/** Stops the timer (when the window closes) */
	public void stop() {
		timer.stop();
	}

	private void step() {
		tick++;
		// after the show only the blinking needs repainting
		if (tick <= RISE_END || blinkingNow() != blinkingBefore()) {
			repaint();
		}
	}

	private boolean blinkingNow() {
		return tick > RISE_END && (tick % 70) < 3;
	}

	private boolean blinkingBefore() {
		return tick - 1 > RISE_END && ((tick - 1) % 70) < 3;
	}

	////////// SHAPE ////////////////

	private static boolean inEgg(int x, int y) {
		double dx = (x + 0.5 - EGG_CENTER_X) / 6.0;
		double dy = (y + 0.5 - (EGG_TOP + EGG_HEIGHT / 2.0)) / (EGG_HEIGHT / 2.0);
		// a bit narrower at the top, like an egg
		double narrow = y < EGG_TOP + 5 ? 1.0 + (EGG_TOP + 5 - y) * 0.08 : 1.0;
		return dx * dx * narrow * narrow + dy * dy <= 1.0;
	}

	private void paintEggPart(Graphics2D g, boolean cap, int offsetX, int offsetY) {
		for (int y = EGG_TOP; y < EGG_TOP + EGG_HEIGHT; y++) {
			for (int x = EGG_CENTER_X - 7; x <= EGG_CENTER_X + 7; x++) {
				if (!inEgg(x, y)) {
					continue;
				}
				// the two halves meet on a jagged line: even columns of the rim row go to the cap, the others to the base
				boolean belongs = cap ? (y < RIM || (y == RIM && x % 2 == 0)) : (y > RIM || (y == RIM && x % 2 != 0));
				if (!belongs) {
					continue;
				}
				boolean edge = !inEgg(x - 1, y) || !inEgg(x + 1, y) || !inEgg(x, y - 1) || !inEgg(x, y + 1);
				Color color = edge ? OUTLINE : speckled(x, y) ? SPECKLE : (x - EGG_CENTER_X) + (y - EGG_TOP) > 7 ? SHELL_SHADE : SHELL;
				cell(g, color, x + offsetX, y + offsetY);
			}
		}
	}

	private static boolean speckled(int x, int y) {
		return (x * 7 + y * 13) % 11 == 0;
	}

	private void cell(Graphics2D g, Color color, int x, int y) {
		g.setColor(color);
		g.fillRect(x * unit, y * unit, unit, unit);
	}

	////////// PAINT ////////////////

	@Override
	protected void paintComponent(Graphics graphics) {
		if (tick < 0) {
			return;
		}
		Graphics2D g = (Graphics2D) graphics.create();
		g.clipRect(0, 0, COLUMNS * unit, ROWS * unit);

		if (tick < BURST_END - 12) {
			paintWholeEgg(g);
		} else {
			paintOpenEgg(g);
		}
		g.dispose();
	}

	private void paintWholeEgg(Graphics2D g) {
		int dropY = tick < DROP_END ? -(DROP_END - tick) * 2 : 0;
		int wobbleX = 0;
		if (tick >= DROP_END && tick < CRACK_END) {
			int phase = (tick / (tick < WOBBLE_END ? 2 : 1)) % 4;
			wobbleX = phase == 0 ? -1 : phase == 2 ? 1 : 0;
		}
		paintEggPart(g, false, wobbleX, dropY);
		paintEggPart(g, true, wobbleX, dropY);
		if (tick >= WOBBLE_END) {
			paintCrack(g, wobbleX, (tick - WOBBLE_END) / (double) (CRACK_END - WOBBLE_END));
		}
	}

	private void paintCrack(Graphics2D g, int offsetX, double progress) {
		int length = (int) Math.min(13, Math.round(progress * 13) + 1);
		for (int i = 0; i < length; i++) {
			int x = EGG_CENTER_X - 6 + i;
			int y = RIM + (i % 2 == 0 ? 0 : 1);
			if (inEgg(x, y)) {
				cell(g, OUTLINE, x + offsetX, y);
			}
		}
	}

	private void paintOpenEgg(Graphics2D g) {
		int sinceBurst = tick - (BURST_END - 12);

		// cats first: they live inside the shell, only what is above the rim shows
		java.awt.Shape previous = g.getClip();
		g.clipRect(0, 0, COLUMNS * unit, (RIM + 1) * unit);
		double rise = Math.min(1.0, Math.max(0.0, (tick - (BURST_END - 8)) / (double) (RISE_END - (BURST_END - 8))));
		int lift = (int) Math.round(8 * easeOutBack(rise));
		boolean blink = blinkingNow();
		CatSprites.draw(g, Cat.FEO, 4 * unit, (RIM + 1 - lift) * unit, unit, blink);
		CatSprites.draw(g, Cat.NANO, 15 * unit, (RIM + 1 - lift + (rise < 1.0 ? 1 : 0)) * unit, unit, blink);
		g.setClip(previous);

		paintEggPart(g, false, 0, 0);
		if (sinceBurst < 12) {
			// the cap flies away up and to the right
			paintEggPart(g, true, sinceBurst / 3, -sinceBurst / 2 - sinceBurst);
		}
		if (sinceBurst < 16) {
			paintSparks(g, sinceBurst);
		}
	}

	private void paintSparks(Graphics2D g, int age) {
		int[][] directions = { { -3, -2 }, { 3, -2 }, { -2, -3 }, { 2, -3 }, { -4, 0 }, { 4, 0 }, { 0, -4 }, { -1, -3 } };
		for (int[] direction : directions) {
			int x = EGG_CENTER_X + direction[0] * age / 3;
			int y = RIM + direction[1] * age / 3;
			if (x >= 0 && x < COLUMNS && y >= 0 && y < ROWS) {
				cell(g, SPARK, x, y);
			}
		}
	}

	/** Goes a little over 1 before settling, like a bounce */
	private static double easeOutBack(double t) {
		double c1 = 1.2;
		double c3 = c1 + 1;
		return 1 + c3 * Math.pow(t - 1, 3) + c1 * Math.pow(t - 1, 2);
	}
}

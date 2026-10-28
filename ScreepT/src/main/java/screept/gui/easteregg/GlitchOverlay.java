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

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Random;

import javax.swing.JComponent;
import javax.swing.JRootPane;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import various.common.light.gui.ScreenMemory;

/**
 * Easter egg: the main window glitches (slices shift, color bars flicker, scanlines) and a fake
 * "ACCESS GRANTED" appears, followed by a reassuring line. Works on a snapshot of the window, nothing is touched.
 */
public final class GlitchOverlay extends JWindow {

	private static final long serialVersionUID = 1L;

	private static final int TICK_MS = 45;
	private static final long GLITCH_MS = 2300;
	private static final long TOTAL_MS = 4600;

	private static GlitchOverlay instance;

	/** Starts the glitch over the main window; ignored while one is running or when the window is not visible */
	public static void open(Window over) {
		SwingUtilities.invokeLater(() -> {
			if (instance != null || !(over instanceof javax.swing.RootPaneContainer) || !over.isShowing()) {
				return;
			}
			JRootPane root = ((javax.swing.RootPaneContainer) over).getRootPane();
			if (root.getWidth() <= 0 || root.getHeight() <= 0) {
				return;
			}
			BufferedImage snapshot = new BufferedImage(root.getWidth(), root.getHeight(), BufferedImage.TYPE_INT_RGB);
			Graphics2D g = snapshot.createGraphics();
			root.paint(g);
			g.dispose();
			Point where = root.getLocationOnScreen();
			instance = new GlitchOverlay(over, snapshot, new Rectangle(where.x, where.y, snapshot.getWidth(), snapshot.getHeight()));
			instance.setVisible(true);
			instance.timer.start();
		});
	}

	private final BufferedImage snapshot;
	private final Random random = new Random();
	private final long startMs = System.currentTimeMillis();
	private final Timer timer = new Timer(TICK_MS, e -> step());

	private GlitchOverlay(Window owner, BufferedImage snapshot, Rectangle bounds) {
		super(owner);
		this.snapshot = snapshot;
		setBounds(bounds.isEmpty() ? ScreenMemory.getLastScreenBounds() : bounds);
		setFocusableWindowState(false);
		setAlwaysOnTop(true);

		JComponent scene = new JComponent() {
			private static final long serialVersionUID = 1L;

			@Override
			protected void paintComponent(Graphics g) {
				paintScene((Graphics2D) g);
			}
		};
		scene.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				close();
			}
		});
		setContentPane(scene);
	}

	private void step() {
		if (System.currentTimeMillis() - startMs > TOTAL_MS) {
			close();
			return;
		}
		repaint();
	}

	private void close() {
		timer.stop();
		dispose();
		SwingUtilities.invokeLater(() -> instance = null);
	}

	private void paintScene(Graphics2D g) {
		int w = getWidth();
		int h = getHeight();
		long elapsed = System.currentTimeMillis() - startMs;
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.drawImage(snapshot, 0, 0, null);

		if (elapsed < GLITCH_MS) {
			double intensity = Math.min(1.0, elapsed / 1500.0);
			int slices = 3 + (int) (intensity * 16);
			for (int i = 0; i < slices; i++) {
				int sliceHeight = 4 + random.nextInt(10 + (int) (50 * intensity));
				int y = random.nextInt(Math.max(1, h - sliceHeight));
				int shift = (int) ((random.nextInt(2) == 0 ? -1 : 1) * (8 + random.nextInt(20 + (int) (160 * intensity))));
				g.drawImage(snapshot, shift, y, shift + w, y + sliceHeight, 0, y, w, y + sliceHeight, null);
			}
			for (int i = 0; i < 2 + (int) (6 * intensity); i++) {
				int rgb = random.nextBoolean() ? 0x00FFC8 : 0xFF2BD6;
				g.setColor(new Color((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF, 40 + random.nextInt(60)));
				g.fillRect(0, random.nextInt(h), w, 2 + random.nextInt(8));
			}
		} else {
			float dark = Math.min(0.88f, (elapsed - GLITCH_MS) / 400f);
			g.setColor(new Color(0, 0, 0, (int) (255 * dark)));
			g.fillRect(0, 0, w, h);
			boolean flash = (elapsed / 160) % 2 == 0 || elapsed > GLITCH_MS + 900;
			g.setFont(EggFrame.mono(java.awt.Font.BOLD, Math.max(26, Math.min(56, w / 16))));
			FontMetrics fm = g.getFontMetrics();
			String title = "ACCESS GRANTED";
			if (flash) {
				g.setColor(EggFrame.GREEN);
				g.drawString(title, (w - fm.stringWidth(title)) / 2, h / 2);
			}
			if (elapsed > GLITCH_MS + 1200) {
				g.setFont(EggFrame.mono(java.awt.Font.PLAIN, 18));
				fm = g.getFontMetrics();
				String joke = "...just kidding. Nobody was hacked. Feo checked twice.";
				g.setColor(EggFrame.TEXT);
				g.drawString(joke, (w - fm.stringWidth(joke)) / 2, h / 2 + 46);
			}
		}

		g.setColor(new Color(0, 0, 0, 45));
		for (int line = 0; line < h; line += 3) {
			g.drawLine(0, line, w, line);
		}
	}
}

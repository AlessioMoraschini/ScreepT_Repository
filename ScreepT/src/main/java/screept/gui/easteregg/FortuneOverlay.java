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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import various.common.light.gui.ScreenMemory;

/**
 * Easter egg: a fortune cookie typed on a CRT-style console over the main window, then it fades away.
 * A click closes it at once. Never takes the focus.
 */
public final class FortuneOverlay extends JWindow {

	private static final long serialVersionUID = 1L;

	private static final int TICK_MS = 30;
	private static final int MS_PER_CHAR = 38;
	private static final int HOLD_MS = 3800;
	private static final int FADE_MS = 700;

	private static FortuneOverlay instance;

	/** Opens the cookie over the given window (or on the last used monitor), ignored while another one is showing */
	public static void open(Window over) {
		SwingUtilities.invokeLater(() -> {
			if (instance != null) {
				return;
			}
			Rectangle area = over != null && over.isShowing() ? over.getBounds() : ScreenMemory.getLastScreenBounds();
			instance = new FortuneOverlay(over, area, ScreepTQuotes.randomQuote());
			instance.setVisible(true);
			instance.timer.start();
		});
	}

	private final String quote;
	private final long startMs = System.currentTimeMillis();
	private final Timer timer = new Timer(TICK_MS, e -> step());
	private final boolean fadeSupported;

	private FortuneOverlay(Window owner, Rectangle area, String quote) {
		super(owner);
		this.quote = quote;
		int width = Math.min(780, area.width - 80);
		int height = 210;
		setBounds(area.x + (area.width - width) / 2, area.y + (area.height - height) / 2, width, height);
		setFocusableWindowState(false);
		setAlwaysOnTop(true);
		fadeSupported = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
				.isWindowTranslucencySupported(java.awt.GraphicsDevice.WindowTranslucency.TRANSLUCENT);

		JComponent console = new JComponent() {
			private static final long serialVersionUID = 1L;

			@Override
			protected void paintComponent(Graphics g) {
				paintConsole((Graphics2D) g);
			}
		};
		console.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				close();
			}
		});
		setContentPane(console);
	}

	private long elapsed() {
		return System.currentTimeMillis() - startMs;
	}

	private void step() {
		long total = (long) quote.length() * MS_PER_CHAR + HOLD_MS;
		long elapsed = elapsed();
		if (elapsed > total + FADE_MS) {
			close();
			return;
		}
		if (fadeSupported && elapsed > total) {
			try {
				setOpacity(Math.max(0.02f, 1f - (elapsed - total) / (float) FADE_MS));
			} catch (RuntimeException e) {
				// no fade on this system: the window simply disappears
			}
		}
		repaint();
	}

	private void close() {
		timer.stop();
		dispose();
		SwingUtilities.invokeLater(() -> instance = null);
	}

	private void paintConsole(Graphics2D g) {
		int w = getWidth();
		int h = getHeight();
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		g.setColor(new Color(0x0A1012));
		g.fillRect(0, 0, w, h);
		g.setColor(EggFrame.TEAL);
		g.setStroke(new BasicStroke(2f));
		g.drawRect(1, 1, w - 3, h - 3);

		g.setFont(EggFrame.mono(java.awt.Font.PLAIN, 14));
		g.setColor(EggFrame.MUTED);
		g.drawString("fortune@screept:~$ ./cookie --offline", 22, 32);

		int shown = (int) Math.min(quote.length(), elapsed() / MS_PER_CHAR);
		String visible = quote.substring(0, shown);
		g.setFont(EggFrame.mono(java.awt.Font.BOLD, 24));
		FontMetrics fm = g.getFontMetrics();
		List<String> rows = wrap(visible, fm, w - 60);
		int y = 82;
		g.setColor(EggFrame.TEXT);
		for (String row : rows) {
			g.drawString(row, 30, y);
			y += fm.getHeight() + 2;
		}
		if ((elapsed() / 450) % 2 == 0 && shown < quote.length() + 1) {
			String last = rows.isEmpty() ? "" : rows.get(rows.size() - 1);
			g.setColor(EggFrame.AMBER);
			g.fillRect(30 + fm.stringWidth(last) + 3, y - (fm.getHeight() + 2) - fm.getAscent() + 6, 12, fm.getHeight() - 6);
		}
		if (shown >= quote.length()) {
			g.setFont(EggFrame.mono(java.awt.Font.PLAIN, 13));
			g.setColor(EggFrame.MUTED);
			g.drawString("- your ScreepT fortune cookie  (click to close)", 30, h - 18);
		}

		// CRT scanlines
		g.setColor(new Color(0, 0, 0, 55));
		for (int line = 0; line < h; line += 3) {
			g.drawLine(0, line, w, line);
		}
	}

	private static List<String> wrap(String text, FontMetrics fm, int maxWidth) {
		List<String> rows = new ArrayList<>();
		StringBuilder row = new StringBuilder();
		for (String word : text.split(" ", -1)) {
			String candidate = row.length() == 0 ? word : row + " " + word;
			if (fm.stringWidth(candidate) > maxWidth && row.length() > 0) {
				rows.add(row.toString());
				row = new StringBuilder(word);
			} else {
				row = new StringBuilder(candidate);
			}
		}
		rows.add(row.toString());
		return rows;
	}
}

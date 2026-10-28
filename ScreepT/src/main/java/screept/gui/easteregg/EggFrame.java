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
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.JFrame;

import various.common.light.gui.ScreenMemory;

/**
 * Window of an easter egg game: dark terminal look, opens on the monitor used last, one instance per game,
 * ESC closes it. Subclasses stop their timers in {@link #onClosed()}, so nothing keeps running after the window is gone.
 */
abstract class EggFrame extends JFrame {

	private static final long serialVersionUID = 1L;

	static final Color BACKGROUND = new Color(0x101418);
	static final Color PANEL = new Color(0x171C22);
	static final Color TEAL = new Color(0x2FB5A8);
	static final Color TEXT = new Color(0xDDF5F1);
	static final Color MUTED = new Color(0x6FA39C);
	static final Color AMBER = new Color(0xF2B84B);
	static final Color RED = new Color(0xFF6E6E);
	static final Color GREEN = new Color(0x5FD38D);

	private static final Map<Class<?>, EggFrame> OPEN = new HashMap<>();

	/** Brings the window of this game to the front when it is already open */
	static boolean focusIfOpen(Class<?> game) {
		EggFrame open = OPEN.get(game);
		if (open != null) {
			open.setState(JFrame.NORMAL);
			open.toFront();
			return true;
		}
		return false;
	}

	static Font mono(int style, int size) {
		Font consolas = new Font("Consolas", style, size);
		return "Consolas".equalsIgnoreCase(consolas.getFamily()) ? consolas : new Font(Font.MONOSPACED, style, size);
	}

	protected EggFrame(String title, int width, int height) {
		super(title);
		getContentPane().setBackground(BACKGROUND);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setResizable(false);
		getContentPane().setPreferredSize(new java.awt.Dimension(width, height));
		pack();
		ScreenMemory.centerOnLastScreen(this);

		getRootPane().registerKeyboardAction(e -> dispose(),
				javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0),
				javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW);

		OPEN.put(getClass(), this);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				OPEN.remove(EggFrame.this.getClass());
				onClosed();
			}
		});
	}

	/** Stop timers and release what the game holds */
	protected void onClosed() {
	}

	////////// BEST SCORES ////////////////

	static int bestScore(String game) {
		try {
			return Preferences.userRoot().node("screept/eggs").getInt(game, 0);
		} catch (RuntimeException e) {
			return 0;
		}
	}

	/** Saves the score when it beats the stored one, returns true on a new record */
	static boolean saveBestScore(String game, int score) {
		try {
			Preferences node = Preferences.userRoot().node("screept/eggs");
			if (score > node.getInt(game, 0)) {
				node.putInt(game, score);
				return true;
			}
		} catch (RuntimeException e) {
			// the record is only a nicety
		}
		return false;
	}
}

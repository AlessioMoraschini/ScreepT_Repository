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
package screept.gui.panels.quicknotes;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;

/**
 * Right-side drawer of the main frame hosting {@link QuickNotesPanel}.
 * Lives in the layered pane above the content, so the module panels (text editor, crypter...) are not touched.
 */
public final class QuickNotesDrawer extends JPanel {

	private static final long serialVersionUID = 6081513979326218420L;

	private static final int SHADOW = 10;
	private static final int ANIMATION_MS = 160;

	private static QuickNotesDrawer instance;

	private final JFrame frame;
	private final QuickNotesStore store;
	private final QuickNotesPanel notesPanel;
	private final Timer animation;
	private long animationStart;
	private boolean opening;
	private Component previousFocus;

	private QuickNotesDrawer(JFrame frame) {
		this.frame = frame;
		this.store = new QuickNotesStore(QuickNotesStore.DEFAULT_FILE);
		this.notesPanel = new QuickNotesPanel(store, this::close);

		setLayout(new BorderLayout());
		setOpaque(false);
		setBorder(javax.swing.BorderFactory.createEmptyBorder(0, SHADOW, 0, 0));
		add(notesPanel, BorderLayout.CENTER);
		setVisible(false);

		animation = new Timer(12, e -> animate());
		frame.getLayeredPane().add(this, JLayeredPane.PALETTE_LAYER);
		frame.getLayeredPane().addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				if (isVisible() && !animation.isRunning()) {
					setBounds(targetBounds(1d));
				}
			}
		});
	}

	public static QuickNotesDrawer getInstance(JFrame frame) {
		if (instance == null) {
			instance = new QuickNotesDrawer(frame);
		}
		return instance;
	}

	/** True when the keyboard focus is inside the notes, so other global shortcuts must not react */
	public static boolean ownsFocus() {
		Component owner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
		return instance != null && instance.isVisible() && owner != null && SwingUtilities.isDescendingFrom(owner, instance);
	}

	public void toggle() {
		if (isVisible() && opening) {
			close();
		} else {
			open();
		}
	}

	public void open() {
		previousFocus = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
		notesPanel.applyPalette();
		opening = true;
		setBounds(targetBounds(0d));
		setVisible(true);
		startAnimation();
		notesPanel.focusAddField();
	}

	public void close() {
		store.flush();
		opening = false;
		startAnimation();
		if (previousFocus != null && previousFocus.isShowing()) {
			previousFocus.requestFocusInWindow();
		}
	}

	private void startAnimation() {
		animationStart = System.currentTimeMillis();
		animation.restart();
	}

	private void animate() {
		double t = Math.min(1d, (System.currentTimeMillis() - animationStart) / (double) ANIMATION_MS);
		// ease-out cubic
		double eased = 1 - Math.pow(1 - t, 3);
		setBounds(targetBounds(opening ? eased : 1 - eased));
		if (t >= 1d) {
			animation.stop();
			if (!opening) {
				setVisible(false);
			}
		}
	}

	/** Bounds of the drawer below the menu bar, slid in by the given fraction (0 = hidden, 1 = open) */
	private Rectangle targetBounds(double shown) {
		Rectangle content = frame.getContentPane().getBounds();
		float scale = UIManager.getFont("Label.font") == null ? 1f : UIManager.getFont("Label.font").getSize2D() / 12f;
		int width = (int) Math.min(content.width * 0.9, Math.max(340 * scale, Math.min(460 * scale, content.width * 0.28))) + SHADOW;
		int x = content.x + content.width - (int) Math.round(width * shown);
		return new Rectangle(x, content.y, width, content.height);
	}

	@Override
	protected void paintComponent(Graphics g) {
		// soft shadow on the left edge
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setPaint(new GradientPaint(0, 0, new Color(0, 0, 0, 0), SHADOW, 0, new Color(0, 0, 0, 70)));
		g2.fillRect(0, 0, SHADOW, getHeight());
		g2.dispose();
	}
}

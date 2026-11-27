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
	/** Transparent strip on the right edge, grabbed to resize the width (the left one is the shadow strip) */
	private static final int GRIP = 6;
	private static final int MIN_WIDTH = 300;
	private static final String WIDTH_KEY = "quickNotesWidth";
	/** Total width chosen by the user by dragging an edge (0 = the default one) */
	private int userWidth;
	private static final int ANIMATION_MS = 160;

	private static QuickNotesDrawer instance;
	/** Notified with the new state whenever the drawer opens or closes (any trigger: shortcut, menu, button) */
	private static final java.util.List<java.util.function.Consumer<Boolean>> OPEN_LISTENERS = new java.util.concurrent.CopyOnWriteArrayList<>();

	private final JFrame frame;
	private final QuickNotesWorkspaces workspaces;
	private final QuickNotesPanel notesPanel;
	private final Timer animation;
	private long animationStart;
	private boolean opening;
	/** Distance in pixels between the right edge of the application and the drawer: moved by dragging the title, kept between sessions and resizes */
	private int rightGap;
	private static final String GAP_KEY = "quickNotesRightGap";
	private Component previousFocus;

	private QuickNotesDrawer(JFrame frame) {
		this.frame = frame;
		this.workspaces = new QuickNotesWorkspaces(QuickNotesStore.DEFAULT_FILE.getParentFile());
		this.notesPanel = new QuickNotesPanel(workspaces, this::close);
		loadRightGap();
		notesPanel.installDragHandle(new java.awt.event.MouseAdapter() {
			private int pressScreenX;
			private int pressGap;

			@Override
			public void mousePressed(java.awt.event.MouseEvent e) {
				pressScreenX = e.getXOnScreen();
				pressGap = rightGap;
			}

			@Override
			public void mouseDragged(java.awt.event.MouseEvent e) {
				if (animation.isRunning() || !isVisible()) {
					return;
				}
				rightGap = Math.max(0, pressGap - (e.getXOnScreen() - pressScreenX));
				setBounds(targetBounds(1d));
				revalidate();
				repaint();
			}

			@Override
			public void mouseReleased(java.awt.event.MouseEvent e) {
				saveRightGap();
			}
		});

		setLayout(new BorderLayout());
		setOpaque(false);
		setBorder(javax.swing.BorderFactory.createEmptyBorder(0, SHADOW, 0, GRIP));
		installEdgeResize();
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

	public static void addOpenListener(java.util.function.Consumer<Boolean> listener) {
		OPEN_LISTENERS.add(listener);
	}

	private static void fireOpenChanged(boolean open) {
		for (java.util.function.Consumer<Boolean> listener : OPEN_LISTENERS) {
			listener.accept(open);
		}
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
		fireOpenChanged(true);
	}

	public void close() {
		workspaces.flushAll();
		opening = false;
		startAnimation();
		fireOpenChanged(false);
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
		float scale = scale();
		int width = (int) Math.min(content.width * 0.9, Math.max(340 * scale, Math.min(460 * scale, content.width * 0.28))) + SHADOW + GRIP;
		if (userWidth > 0) {
			width = Math.max((int) (MIN_WIDTH * scale) + SHADOW + GRIP, Math.min(userWidth, content.width)); // chosen by the user, never wider than the window
		}
		rightGap = Math.max(0, Math.min(rightGap, content.width - width)); // never out of the window, also after a resize
		int x = content.x + content.width - (int) Math.round((width + rightGap) * shown);
		return new Rectangle(x, content.y, width, content.height);
	}

	/** Dragging the left or the right edge changes the width: the opposite side stays where it is, the distance from the right edge follows */
	private void installEdgeResize() {
		java.awt.event.MouseAdapter edges = new java.awt.event.MouseAdapter() {
			private int mode; // 0 none, 1 left edge, 2 right edge
			private int pressScreenX;
			private int startWidth;
			private int startGap;

			private int zoneAt(int x) {
				return x < SHADOW ? 1 : x >= getWidth() - GRIP ? 2 : 0;
			}

			@Override
			public void mouseMoved(java.awt.event.MouseEvent e) {
				setCursor(zoneAt(e.getX()) == 0 ? java.awt.Cursor.getDefaultCursor() : java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.E_RESIZE_CURSOR));
			}

			@Override
			public void mousePressed(java.awt.event.MouseEvent e) {
				mode = zoneAt(e.getX());
				pressScreenX = e.getXOnScreen();
				startWidth = getWidth();
				startGap = rightGap;
			}

			@Override
			public void mouseDragged(java.awt.event.MouseEvent e) {
				if (mode == 0 || animation.isRunning() || !isVisible()) {
					return;
				}
				int dx = e.getXOnScreen() - pressScreenX;
				int available = frame.getContentPane().getWidth();
				int minWidth = (int) (MIN_WIDTH * scale()) + SHADOW + GRIP;
				if (mode == 1) {
					userWidth = Math.max(minWidth, Math.min(startWidth - dx, available - rightGap));
				} else {
					rightGap = Math.max(0, startGap - dx);
					userWidth = Math.max(minWidth, Math.min(startWidth + (startGap - rightGap), available - rightGap));
				}
				setBounds(targetBounds(1d));
				revalidate();
				repaint();
			}

			@Override
			public void mouseReleased(java.awt.event.MouseEvent e) {
				if (mode != 0) {
					saveLayout();
				}
				mode = 0;
			}

			@Override
			public void mouseExited(java.awt.event.MouseEvent e) {
				if (mode == 0) {
					setCursor(java.awt.Cursor.getDefaultCursor());
				}
			}
		};
		addMouseListener(edges);
		addMouseMotionListener(edges);
	}

	private static float scale() {
		java.awt.Font font = UIManager.getFont("Label.font");
		return font == null ? 1f : font.getSize2D() / 12f;
	}

	private void loadRightGap() {
		try {
			String width = maingUI.MainGUI.currentConfiguration.readFromFile(initializer.configs.impl.INItializer.GEN_OPT, WIDTH_KEY);
			userWidth = width == null || width.trim().isEmpty() ? 0 : Math.max(0, Integer.parseInt(width.trim()));
		} catch (Exception e) {
			userWidth = 0;
		}
		try {
			String stored = maingUI.MainGUI.currentConfiguration.readFromFile(initializer.configs.impl.INItializer.GEN_OPT, GAP_KEY);
			rightGap = stored == null || stored.trim().isEmpty() ? 0 : Math.max(0, Integer.parseInt(stored.trim()));
		} catch (Exception e) {
			rightGap = 0;
		}
	}

	private void saveLayout() {
		saveRightGap();
		try {
			maingUI.MainGUI.currentConfiguration.writeSingleValue(initializer.configs.impl.INItializer.GEN_OPT, WIDTH_KEY, String.valueOf(userWidth));
		} catch (Exception e) {
			// not saved: it opens at the previous width next time
		}
	}

	private void saveRightGap() {
		try {
			maingUI.MainGUI.currentConfiguration.writeSingleValue(initializer.configs.impl.INItializer.GEN_OPT, GAP_KEY, String.valueOf(rightGap));
		} catch (Exception e) {
			// not saved: the drawer simply opens at the previous distance next time
		}
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

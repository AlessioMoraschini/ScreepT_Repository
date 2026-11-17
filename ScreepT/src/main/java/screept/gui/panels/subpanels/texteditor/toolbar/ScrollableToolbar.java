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
package screept.gui.panels.subpanels.texteditor.toolbar;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.plaf.basic.BasicScrollBarUI;

import com.formdev.flatlaf.util.UIScale;

import net.miginfocom.swing.MigLayout;

/**
 * A toolbar that never gets squeezed: when the window is narrower than the buttons it scrolls horizontally (thin
 * scroll bar under the buttons, mouse wheel, drag of the bar); when there is room the strip fills the width, so the
 * controls added after a glue stay on the right.
 */
public class ScrollableToolbar extends JScrollPane {
	private static final long serialVersionUID = 1L;

	/** Height of the scroll bar, always reserved so the toolbar does not change height when it starts to scroll */
	private static final int BAR_HEIGHT = 7;

	private final Strip strip = new Strip();

	public ScrollableToolbar() {
		super(VERTICAL_SCROLLBAR_NEVER, HORIZONTAL_SCROLLBAR_AS_NEEDED);
		setBorder(null);
		setViewportBorder(null);
		setOpaque(true);
		setBackground(ToolbarStyle.BACKGROUND);
		getViewport().setOpaque(true);
		getViewport().setBackground(ToolbarStyle.BACKGROUND);
		getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
		setHorizontalScrollBar(new ThinScrollBar());
		setViewportView(strip);
		setWheelScrollingEnabled(false);
		addMouseWheelListener(e -> {
			JScrollBar bar = getHorizontalScrollBar();
			if (bar.isVisible()) {
				bar.setValue(bar.getValue() + (int) Math.round(e.getPreciseWheelRotation() * UIScale.scale(56)));
				e.consume();
			}
		});
	}

	/** Panel that holds the buttons (MigLayout, one row) */
	public JPanel getStrip() {
		return strip;
	}

	/** Scrolls to show the given component, for instance after it has been enabled or focused */
	public void reveal(JComponent component) {
		Rectangle bounds = component.getBounds();
		strip.scrollRectToVisible(bounds);
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(UIScale.scale(112), UIScale.scale(ToolbarStyle.BUTTON_SIZE + 8 + BAR_HEIGHT));
	}

	@Override
	public Dimension getMinimumSize() {
		return new Dimension(0, UIScale.scale(ToolbarStyle.BUTTON_SIZE + 8 + BAR_HEIGHT));
	}

	/** Content of the viewport: follows the viewport width when everything fits, scrolls otherwise */
	private static final class Strip extends JPanel implements Scrollable {
		private static final long serialVersionUID = 1L;

		Strip() {
			super(new MigLayout("insets 4 6 " + BAR_HEIGHT + " 6, gapx 1, hidemode 3, aligny center", "", "[center]"));
			setOpaque(true);
			setBackground(ToolbarStyle.BACKGROUND);
		}

		@Override
		public Dimension getPreferredScrollableViewportSize() {
			return getPreferredSize();
		}

		@Override
		public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
			return UIScale.scale(32);
		}

		@Override
		public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
			return Math.max(UIScale.scale(32), visibleRect.width - UIScale.scale(48));
		}

		@Override
		public boolean getScrollableTracksViewportWidth() {
			return getParent() != null && getParent().getWidth() >= getPreferredSize().width;
		}

		@Override
		public boolean getScrollableTracksViewportHeight() {
			return true;
		}
	}

	/** Slim, rounded scroll bar without arrows */
	private static final class ThinScrollBar extends JScrollBar {
		private static final long serialVersionUID = 1L;

		ThinScrollBar() {
			super(HORIZONTAL);
			setOpaque(false);
		}

		@Override
		public void updateUI() {
			setUI(new ThinUI());
		}

		@Override
		public Dimension getPreferredSize() {
			return new Dimension(super.getPreferredSize().width, UIScale.scale(BAR_HEIGHT));
		}
	}

	private static final class ThinUI extends BasicScrollBarUI {

		@Override
		protected JButton createDecreaseButton(int orientation) {
			return emptyButton();
		}

		@Override
		protected JButton createIncreaseButton(int orientation) {
			return emptyButton();
		}

		private static JButton emptyButton() {
			JButton button = new JButton();
			button.setPreferredSize(new Dimension(0, 0));
			button.setMinimumSize(new Dimension(0, 0));
			button.setMaximumSize(new Dimension(0, 0));
			return button;
		}

		@Override
		protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
			// transparent track
		}

		@Override
		protected void paintThumb(Graphics g0, JComponent c, Rectangle thumbBounds) {
			if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) {
				return;
			}
			Graphics2D g = (Graphics2D) g0.create();
			try {
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				boolean strong = isDragging || isThumbRollover();
				g.setColor(new Color(255, 255, 255, strong ? 130 : 70));
				int arc = thumbBounds.height - UIScale.scale(2);
				g.fillRoundRect(thumbBounds.x, thumbBounds.y + UIScale.scale(1), thumbBounds.width, thumbBounds.height - UIScale.scale(2), arc, arc);
			} finally {
				g.dispose();
			}
		}
	}
}

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
package screept.gui.style;

import java.awt.Dimension;
import java.awt.Rectangle;

import javax.swing.JPanel;
import javax.swing.Scrollable;

import net.miginfocom.swing.MigLayout;

/**
 * Vertical list of cards that always fits the viewport width, so text wraps instead of scrolling horizontally.
 */
public class ScrollableListPanel extends JPanel implements Scrollable {

	private static final long serialVersionUID = -2280917417563150745L;

	public ScrollableListPanel(String insets, int gapY) {
		super(new MigLayout("wrap 1, fillx, insets " + insets + ", gapy " + gapY, "[grow,fill]"));
		setOpaque(false);
	}

	@Override
	public Dimension getPreferredScrollableViewportSize() {
		return getPreferredSize();
	}

	@Override
	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
		return 16;
	}

	@Override
	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
		return Math.max(visibleRect.height - 32, 16);
	}

	@Override
	public boolean getScrollableTracksViewportWidth() {
		return true;
	}

	@Override
	public boolean getScrollableTracksViewportHeight() {
		return false;
	}
}

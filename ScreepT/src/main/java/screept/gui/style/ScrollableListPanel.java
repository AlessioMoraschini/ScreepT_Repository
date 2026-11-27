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
package screept.gui.style;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;

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

	// ------------------------------------------------------------------ drag and drop overlay

	private Image dragGhost;
	private Point dragGhostAt;
	private Rectangle dropMark;
	private Color dragColor = Color.GRAY;

	/**
	 * What is painted over the cards while one is dragged: the ghost of the card under the pointer and the mark of the
	 * place where it would be dropped.
	 * @param ghost image of the dragged card
	 * @param ghostAt where the ghost is (top left, in the coordinates of this panel)
	 * @param mark rectangle of the drop mark (null: none)
	 */
	public void setDragOverlay(Image ghost, Point ghostAt, Rectangle mark, Color color) {
		Rectangle dirty = overlayBounds();
		this.dragGhost = ghost;
		this.dragGhostAt = ghostAt;
		this.dropMark = mark;
		this.dragColor = color;
		Rectangle now = overlayBounds();
		if (dirty != null) {
			now = now == null ? dirty : now.union(dirty);
		}
		if (now != null) {
			now.grow(8, 8);
			repaint(now);
		}
	}

	public void clearDragOverlay() {
		Rectangle dirty = overlayBounds();
		dragGhost = null;
		dragGhostAt = null;
		dropMark = null;
		if (dirty != null) {
			dirty.grow(8, 8);
			repaint(dirty);
		} else {
			repaint();
		}
	}

	public boolean isDragOverlayShown() {
		return dragGhost != null || dropMark != null;
	}

	public Rectangle getDropMark() {
		return dropMark == null ? null : new Rectangle(dropMark);
	}

	private Rectangle overlayBounds() {
		Rectangle r = null;
		if (dragGhost != null && dragGhostAt != null) {
			r = new Rectangle(dragGhostAt.x, dragGhostAt.y, dragGhost.getWidth(null) + 6, dragGhost.getHeight(null) + 6);
		}
		if (dropMark != null) {
			r = r == null ? new Rectangle(dropMark) : r.union(dropMark);
		}
		return r;
	}

	@Override
	protected void paintChildren(Graphics g) {
		super.paintChildren(g);
		if (dragGhost == null && dropMark == null) {
			return;
		}
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		if (dropMark != null) {
			// a bar with a round end on each side: "it goes here"
			g2.setColor(dragColor);
			g2.fillRoundRect(dropMark.x, dropMark.y, dropMark.width, dropMark.height, dropMark.height, dropMark.height);
			int d = dropMark.height + 4;
			g2.fillOval(dropMark.x - 2, dropMark.y + dropMark.height / 2 - d / 2, d, d);
			g2.fillOval(dropMark.x + dropMark.width - d + 2, dropMark.y + dropMark.height / 2 - d / 2, d, d);
		}
		if (dragGhost != null && dragGhostAt != null) {
			int w = dragGhost.getWidth(null), h = dragGhost.getHeight(null);
			g2.setComposite(AlphaComposite.SrcOver.derive(0.22f));
			g2.setColor(Color.BLACK);
			g2.fillRoundRect(dragGhostAt.x + 3, dragGhostAt.y + 5, w, h, 14, 14);
			g2.setComposite(AlphaComposite.SrcOver.derive(0.88f));
			g2.drawImage(dragGhost, dragGhostAt.x, dragGhostAt.y, null);
			g2.setComposite(AlphaComposite.SrcOver);
			g2.setColor(dragColor);
			g2.setStroke(new java.awt.BasicStroke(2f));
			g2.drawRoundRect(dragGhostAt.x, dragGhostAt.y, w - 1, h - 1, 14, 14);
		}
		g2.dispose();
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

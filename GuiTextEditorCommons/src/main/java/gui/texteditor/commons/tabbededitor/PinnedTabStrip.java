/**
 *
 * =========================================================================================
 *  Copyright (C) 2019-2020
 *
 *  AM-Design-Development - (Alessio Moraschini) - All Rights Reserved
 * =========================================================================================
 *
 * You should have received a copy of the license with this file.
 * If not, please write to: info@am-design-development.com, or visit : https://www.am-design-development.com
 */
package gui.texteditor.commons.tabbededitor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import gui.commons.tabbedpaneutils.CustomTabbedPaneUI_v1;
import gui.commons.tabbedpaneutils.TabHeadPanel_Full;
import gui.commons.treeutils.TreeUtils;
import resources.GeneralConfig;
import various.common.light.gui.ShortcutKeys;
import various.common.light.utility.string.StringWorker;

/**
 * Row above the tabs with the pinned ones: they stay visible and out of the way of the scrolling tabs below
 * (in the tabbed pane they have no width). Every cell mirrors the header of its real tab (icon, title, tooltip),
 * so the rest of the editor keeps working on the tabbed pane.
 */
class PinnedTabStrip extends JPanel {
	private static final long serialVersionUID = 1L;

	private static final int MIN_CELL_WIDTH = 70;
	private static final int MAX_CELL_WIDTH = 220;
	private static final int PIN_ZONE = 22;

	private final CustomTabbedEditor editor;
	private final List<Cell> cells = new ArrayList<Cell>();
	private boolean refreshScheduled = false;

	PinnedTabStrip(CustomTabbedEditor editor) {
		super(null);
		this.editor = editor;
		setVisible(false);
		editor.mainTabList.addChangeListener(e -> repaint());
		// the wheel scrolls through the tabs here too, as on the tabs
		final gui.commons.tabbedpaneutils.TabbedPaneMouseWheelListener wheel = new gui.commons.tabbedpaneutils.TabbedPaneMouseWheelListener();
		addMouseWheelListener(e -> wheel.mouseWheelMoved(new java.awt.event.MouseWheelEvent(editor.mainTabList, e.getID(), e.getWhen(), e.getModifiers(),
				0, 0, e.getClickCount(), false, e.getScrollType(), e.getScrollAmount(), e.getWheelRotation())));
		// tabs added or removed: the pinned ones may have changed
		editor.mainTabList.addContainerListener(new java.awt.event.ContainerAdapter() {
			@Override
			public void componentAdded(java.awt.event.ContainerEvent e) {
				scheduleRefresh();
			}

			@Override
			public void componentRemoved(java.awt.event.ContainerEvent e) {
				scheduleRefresh();
			}
		});
	}

	private void scheduleRefresh() {
		if (refreshScheduled) {
			return;
		}
		refreshScheduled = true;
		SwingUtilities.invokeLater(() -> {
			refreshScheduled = false;
			refresh();
		});
	}

	/** Rebuilds the cells from the pinned tabs (they are always the first ones) */
	void refresh() {
		int pinned = editor.getPinnedCount();
		boolean same = pinned == cells.size();
		for (int i = 0; same && i < pinned; i++) {
			same = cells.get(i).scroll == editor.loadedList.getList().get(i).currentScrollPane;
		}
		if (!same) {
			for (Cell cell : cells) {
				cell.dispose();
			}
			cells.clear();
			removeAll();
			for (int i = 0; i < pinned; i++) {
				JScrollPane scroll = editor.loadedList.getList().get(i).currentScrollPane;
				java.awt.Component header = editor.mainTabList.getTabComponentAt(i);
				if (scroll == null || !(header instanceof JPanel)) {
					continue;
				}
				Cell cell = new Cell(scroll, (JPanel) header);
				cells.add(cell);
				add(cell);
				editor.installStripCell(cell, scroll);
			}
		}
		setVisible(!cells.isEmpty());
		revalidate();
		repaint();
	}

	/** Index (among all the tabs, so among the pinned ones) of the cell nearest to a point of the strip, -1 if none */
	int nearestIndex(java.awt.Point p) {
		int best = -1;
		int bestDistance = Integer.MAX_VALUE;
		for (int i = 0; i < cells.size(); i++) {
			Rectangle r = cells.get(i).getBounds();
			int distance = p.x < r.x ? r.x - p.x : Math.max(0, p.x - (r.x + r.width));
			if (distance < bestDistance) {
				bestDistance = distance;
				best = i;
			}
		}
		return best;
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(super.getPreferredSize().width, cells.isEmpty() ? 0 : GeneralConfig.TAB_HEAD_HEIGHT + 2);
	}

	@Override
	public void doLayout() {
		if (cells.isEmpty()) {
			return;
		}
		int available = getWidth();
		int total = 0;
		for (Cell cell : cells) {
			total += cell.preferredWidth();
		}
		int x = 0;
		int h = getHeight();
		for (Cell cell : cells) {
			int w = cell.preferredWidth();
			if (total > available) {
				w = Math.max(MIN_CELL_WIDTH, w * available / Math.max(1, total));
			}
			cell.setBounds(x, 0, w, h);
			x += w;
		}
	}

	@Override
	protected void paintComponent(Graphics g) {
		g.setColor(editor.mainTabList.getBackground());
		g.fillRect(0, 0, getWidth(), getHeight());
	}

	@Override
	protected void paintChildren(Graphics g) {
		super.paintChildren(g);
		Color base = editor.mainTabList.getBackground();
		// thin line between the strip and the tabs below, like the one under the tabs
		g.setColor(TreeUtils.blend(base, TreeUtils.textOn(base), 0.16f));
		g.fillRect(0, getHeight() - 1, getWidth(), 1);
		// where the dragged pinned tab would be dropped
		int from = editor.dragFromIndex;
		int to = editor.dragDropIndex;
		if (from >= 0 && from < cells.size() && to >= 0 && to < cells.size() && to != from) {
			Rectangle r = cells.get(to).getBounds();
			int x = to > from ? r.x + r.width - 1 : r.x;
			g.setColor(CustomTabbedPaneUI_v1.accentColor());
			g.fillRect(x - 1, r.y + 2, 3, Math.max(4, r.height - 4));
		}
	}

	/** One pinned tab */
	final class Cell extends JComponent {
		private static final long serialVersionUID = 1L;

		final JScrollPane scroll;
		private final JLabel iconLabel;
		private final JLabel titleLabel;
		private final PropertyChangeListener mirror;
		private boolean hover = false;

		Cell(JScrollPane scroll, JPanel header) {
			this.scroll = scroll;
			this.iconLabel = (JLabel) header.getComponent(0);
			this.titleLabel = (JLabel) header.getComponent(1);
			// the title (unsaved mark, rename) and the icon of the real tab are mirrored here
			mirror = ev -> {
				setToolTipText(header.getToolTipText());
				PinnedTabStrip.this.revalidate();
				repaint();
			};
			titleLabel.addPropertyChangeListener("text", mirror);
			iconLabel.addPropertyChangeListener("icon", mirror);
			setToolTipText(header.getToolTipText());

			addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					if (!SwingUtilities.isLeftMouseButton(e) || e.isShiftDown() || ShortcutKeys.isCtrlDown(e)) {
						return;
					}
					SwingUtilities.invokeLater(() -> {
						int idx = editor.mainTabList.indexOfComponent(scroll);
						if (idx >= 0) {
							editor.mainTabList.setSelectedIndex(idx);
						}
					});
				}

				@Override
				public void mouseClicked(MouseEvent e) {
					// the pin on the right unpins the tab
					if (SwingUtilities.isLeftMouseButton(e) && !e.isShiftDown() && !ShortcutKeys.isCtrlDown(e) && e.getX() >= getWidth() - PIN_ZONE) {
						SwingUtilities.invokeLater(() -> editor.unpinTab(editor.mainTabList.indexOfComponent(scroll)));
					}
				}

				@Override
				public void mouseEntered(MouseEvent e) {
					hover = true;
					repaint();
				}

				@Override
				public void mouseExited(MouseEvent e) {
					hover = false;
					repaint();
				}
			});
		}

		void dispose() {
			titleLabel.removePropertyChangeListener("text", mirror);
			iconLabel.removePropertyChangeListener("icon", mirror);
		}

		private String title() {
			return StringWorker.trimToEmpty(titleLabel.getText());
		}

		int preferredWidth() {
			FontMetrics fm = getFontMetrics(titleLabel.getFont());
			int width = 6 + 16 + 5 + fm.stringWidth(title()) + 5 + PIN_ZONE;
			return Math.max(MIN_CELL_WIDTH, Math.min(MAX_CELL_WIDTH, width));
		}

		@Override
		protected void paintComponent(Graphics g) {
			boolean selected = editor.mainTabList.getSelectedComponent() == scroll;
			Color base = editor.mainTabList.getBackground();
			boolean dark = TreeUtils.isDark(base);
			Color strong = TreeUtils.textOn(base);
			int w = getWidth();
			int h = getHeight() - 1;

			// same look as the tabs: the selected one is lifted, the others sink into the strip
			g.setColor(selected
					? TreeUtils.blend(base, strong, dark ? 0.12f : 0.07f)
					: TreeUtils.blend(base, dark ? Color.BLACK : strong, dark ? 0.18f : 0.03f));
			g.fillRect(0, 0, w, h);
			if (selected) {
				g.setColor(CustomTabbedPaneUI_v1.accentColor());
				g.fillRect(0, 0, w, 2);
			} else {
				g.setColor(TreeUtils.blend(base, strong, 0.14f));
				g.fillRect(w - 1, 5, 1, Math.max(1, h - 10));
			}

			Graphics2D g2 = (Graphics2D) g.create();
			try {
				g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
				Icon icon = iconLabel.getIcon();
				int textX = 6;
				if (icon != null) {
					icon.paintIcon(this, g2, 6, (h - icon.getIconHeight()) / 2);
					textX += 16 + 5;
				}
				String title = title();
				boolean unsaved = title.contains("(*)");
				Color text = selected ? strong : TreeUtils.blend(strong, base, 0.4f);
				if (unsaved) {
					text = dark ? new Color(selected ? 240 : 205, selected ? 110 : 95, selected ? 100 : 90) : new Color(selected ? 176 : 150, 30, 30);
				}
				g2.setColor(text);
				g2.setFont(titleLabel.getFont());
				FontMetrics fm = g2.getFontMetrics();
				int room = w - textX - PIN_ZONE;
				String shown = title;
				while (shown.length() > 1 && fm.stringWidth(shown) > room) {
					shown = shown.substring(0, shown.length() - 1);
				}
				if (shown.length() < title.length() && shown.length() > 1) {
					shown = shown.substring(0, shown.length() - 1) + "…";
				}
				g2.drawString(shown, textX, (h - fm.getHeight()) / 2 + fm.getAscent());
				TabHeadPanel_Full.pinIcon(selected || hover).paintIcon(this, g2, w - PIN_ZONE + 5, (h - 12) / 2);
			} finally {
				g2.dispose();
			}
		}

		@Override
		public Insets getInsets() {
			return new Insets(0, 0, 0, 0);
		}
	}
}

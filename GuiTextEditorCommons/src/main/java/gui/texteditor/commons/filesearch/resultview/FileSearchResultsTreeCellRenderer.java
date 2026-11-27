/**
 *
 * =========================================================================================
 *  Copyright (C) 2019-2020
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
package gui.texteditor.commons.filesearch.resultview;

import java.awt.Color;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreeCellRenderer;

import gui.texteditor.commons.filesearch.resultview.FileSearchResultViewer.ResultViewDTO;
import resources.IconsPathConfigurator;

/**
 * One reused label for every row; icons loaded once, text already computed by the node.
 * Repaint/validate are no-ops as in DefaultTreeCellRenderer: a renderer is only a stamp.
 */
public class FileSearchResultsTreeCellRenderer extends JLabel implements TreeCellRenderer {
	private static final long serialVersionUID = 1L;

	public static Font normalFont = new Font("Segoe UI", Font.PLAIN, 14);
	public static Font boldFont = new Font("Segoe UI", Font.BOLD, 13);

	/** Slightly different background of the text searched, inside the lines */
	private static final Color MATCH_BACKGROUND = new Color(255, 205, 80, 60);
	private static final int[] NO_RANGES = new int[0];
	private int[] ranges = NO_RANGES;

	private final Icon rootIcon;
	private final Icon fileIcon;
	private final Icon lineIcon;

	public FileSearchResultsTreeCellRenderer(JTree treeRef) {
		rootIcon = new ImageIcon(IconsPathConfigurator.ICON_SEARCH);
		fileIcon = new ImageIcon(IconsPathConfigurator.iconFilePath);
		lineIcon = new ImageIcon(IconsPathConfigurator.ICON_NEXT);
		setOpaque(false);
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
			boolean leaf, int row, boolean hasFocus) {

		if (value instanceof ResultViewDTO) {
			ResultViewDTO node = (ResultViewDTO) value;
			setText(node.toString());
			setIcon(node.root ? rootIcon : node.fileHead ? fileIcon : lineIcon);
			setFont(node.root || node.fileHead ? boldFont : normalFont);
			ranges = node.matchRanges();
		} else {
			ranges = NO_RANGES;
		}

		setForeground(selected ? Color.WHITE : Color.LIGHT_GRAY);
		return this;
	}

	@Override
	protected void paintComponent(Graphics g) {
		if(ranges.length > 0)
			paintMatches(g);
		super.paintComponent(g);
	}

	private void paintMatches(Graphics g) {
		String text = getText();
		FontMetrics metrics = getFontMetrics(getFont());
		Insets in = getInsets();
		Rectangle view = new Rectangle(in.left, in.top, getWidth() - in.left - in.right, getHeight() - in.top - in.bottom);
		Rectangle iconRect = new Rectangle();
		Rectangle textRect = new Rectangle();
		SwingUtilities.layoutCompoundLabel(this, metrics, text, getIcon(), getVerticalAlignment(), getHorizontalAlignment(),
				getVerticalTextPosition(), getHorizontalTextPosition(), view, iconRect, textRect, getIconTextGap());
		g.setColor(MATCH_BACKGROUND);
		for(int i = 0; i + 1 < ranges.length; i += 2) {
			int start = Math.min(ranges[i], text.length());
			int end = Math.min(ranges[i + 1], text.length());
			if(end > start)
				g.fillRect(textRect.x + metrics.stringWidth(text.substring(0, start)), textRect.y, metrics.stringWidth(text.substring(start, end)), textRect.height);
		}
	}

	@Override public void validate() {}
	@Override public void invalidate() {}
	@Override public void revalidate() {}
	@Override public void repaint() {}
	@Override public void repaint(long tm, int x, int y, int width, int height) {}
	@Override public void repaint(Rectangle r) {}

}

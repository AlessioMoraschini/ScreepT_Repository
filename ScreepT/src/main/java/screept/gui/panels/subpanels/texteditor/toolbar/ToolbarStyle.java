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
package screept.gui.panels.subpanels.texteditor.toolbar;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

import javax.swing.AbstractButton;
import javax.swing.UIManager;

import com.formdev.flatlaf.util.UIScale;

/**
 * Colors, sizes and painting shared by the buttons of the file editor toolbar. The editor chrome is always dark, so the
 * toolbar has its own palette; only the accent follows the look and feel.
 */
public final class ToolbarStyle {

	/** Toolbar background (a little lighter than the editor header) */
	public static final Color BACKGROUND = new Color(40, 41, 44);
	/** Thin line under the toolbar and between groups */
	public static final Color LINE = new Color(255, 255, 255, 38);
	/** Normal icon and text color */
	public static final Color ICON = new Color(196, 200, 207);
	public static final Color ICON_HOVER = new Color(255, 255, 255);
	public static final Color MUTED = new Color(150, 154, 162);

	private static final Color DEFAULT_ACCENT = new Color(58, 124, 214);

	static final int BUTTON_SIZE = 28;
	public static final int ICON_SIZE = 17;

	private ToolbarStyle() {
	}

	/** Accent color of the look and feel (selected toggles, focus) */
	public static Color accent() {
		Color accent = UIManager.getColor("Component.accentColor");
		if (accent == null) {
			accent = UIManager.getColor("List.selectionBackground");
		}
		return accent != null ? accent : DEFAULT_ACCENT;
	}

	/** Selected icons are drawn with a lighter accent to stay readable on the dark chrome */
	static Color accentIcon() {
		Color accent = accent();
		return new Color(Math.min(255, accent.getRed() + 70), Math.min(255, accent.getGreen() + 70), Math.min(255, accent.getBlue() + 70));
	}

	static Color withAlpha(Color color, int alpha) {
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
	}

	/** Paints the rounded state background, the icon, the optional label and the optional drop down chevron of a toolbar button */
	static void paintButton(AbstractButton button, Graphics g0, String label, boolean chevron, boolean active) {
		Graphics2D g = (Graphics2D) g0.create();
		try {
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			boolean enabled = button.isEnabled();
			boolean pressed = enabled && button.getModel().isArmed() && button.getModel().isPressed();
			boolean hover = enabled && button.getModel().isRollover();
			boolean selected = enabled && (button.isSelected() || active);

			int w = button.getWidth();
			int h = button.getHeight();
			int inset = UIScale.scale(1);
			Color background = null;
			if (pressed) {
				background = new Color(255, 255, 255, 60);
			} else if (selected) {
				background = withAlpha(accent(), hover ? 120 : 90);
			} else if (hover) {
				background = new Color(255, 255, 255, 34);
			}
			if (background != null) {
				g.setColor(background);
				float arc = UIScale.scale(8);
				g.fill(new RoundRectangle2D.Float(inset, inset, w - inset * 2, h - inset * 2, arc, arc));
			}

			EditorIcons icon = button.getIcon() instanceof EditorIcons ? (EditorIcons) button.getIcon() : null;
			Color color = !enabled ? withAlpha(ICON, 70) : icon != null && icon.getFixedColor() != null ? icon.getFixedColor() : selected ? accentIcon() : hover || pressed ? ICON_HOVER : ICON;
			int iconSize = UIScale.scale(ICON_SIZE);
			int chevronSize = UIScale.scale(10);
			int gap = UIScale.scale(4);

			if (label == null && !chevron) {
				if (icon != null) {
					icon.paint(g, (w - iconSize) / 2, (h - iconSize) / 2, iconSize, color);
				} else if (button.getIcon() != null) {
					button.getIcon().paintIcon(button, g, (w - button.getIcon().getIconWidth()) / 2, (h - button.getIcon().getIconHeight()) / 2);
				}
				return;
			}

			FontMetrics metrics = g.getFontMetrics(button.getFont());
			int textWidth = label != null ? metrics.stringWidth(label) : 0;
			int total = (icon != null ? iconSize : 0) + (label != null ? gap + textWidth : 0) + (chevron ? gap - 1 + chevronSize : 0);
			int x = (w - total) / 2;
			if (icon != null) {
				icon.paint(g, x, (h - iconSize) / 2, iconSize, color);
				x += iconSize;
			}
			if (label != null) {
				g.setFont(button.getFont());
				g.setColor(color);
				g.drawString(label, x + gap, (h - metrics.getHeight()) / 2 + metrics.getAscent());
				x += gap + textWidth;
			}
			if (chevron) {
				EditorIcons.of(EditorIcons.Kind.CHEVRON, 10).paint(g, x + gap - 1, (h - chevronSize) / 2 + 1, chevronSize, enabled ? withAlpha(color, 190) : color);
			}
		} finally {
			g.dispose();
		}
	}

	/** Preferred width of a toolbar button with optional label and chevron */
	static int preferredWidth(AbstractButton button, String label, boolean chevron) {
		if (label == null && !chevron) {
			return UIScale.scale(BUTTON_SIZE);
		}
		FontMetrics metrics = button.getFontMetrics(button.getFont());
		int width = UIScale.scale(10) + UIScale.scale(ICON_SIZE);
		if (label != null) {
			width += UIScale.scale(4) + metrics.stringWidth(label);
		}
		if (chevron) {
			width += UIScale.scale(3) + UIScale.scale(10);
		}
		return width;
	}

	/** Common setup: no LAF painting, no focus stealing from the text area */
	static void setup(AbstractButton button) {
		button.setOpaque(false);
		button.setContentAreaFilled(false);
		button.setBorderPainted(false);
		button.setFocusPainted(false);
		button.setFocusable(false);
		button.setRolloverEnabled(true);
		button.setMargin(new java.awt.Insets(0, 0, 0, 0));
		button.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
	}

	static void drawSeparator(Graphics g0, int w, int h) {
		Graphics g = g0.create();
		try {
			g.setColor(LINE);
			int height = Math.min(h, UIScale.scale(20));
			g.fillRect(w / 2, (h - height) / 2, Math.max(1, UIScale.scale(1)), height);
		} finally {
			g.dispose();
		}
	}
}

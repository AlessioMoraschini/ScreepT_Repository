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
package screept.launcher.gui.utils;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.plaf.basic.BasicButtonUI;

/**
 * Pixel art look for the main buttons of the launcher: flat colors, a dark outline with notched corners, a light edge on
 * top and left and a shadow edge on the bottom and right (the classic 8 bit bevel), a hard drop shadow under the label,
 * a pixel arrow. Hover lights the button up, pressing sinks it by one pixel unit. Nothing is smoothed or blurred.
 */
final class ActionButtonUI extends BasicButtonUI {

	private static final Color OUTLINE = new Color(0x12, 0x14, 0x18);
	/** One "pixel" of the button in screen pixels */
	private static final int U = 2;

	private final Color base;

	ActionButtonUI(Color base) {
		this.base = base;
	}

	@Override
	public void installUI(JComponent c) {
		super.installUI(c);
		AbstractButton button = (AbstractButton) c;
		button.setOpaque(false);
		button.setContentAreaFilled(false);
		button.setBorderPainted(false);
		button.setFocusPainted(false);
		button.setRolloverEnabled(true);
		button.setForeground(Color.WHITE);
		button.addChangeListener(e -> button.repaint());
	}

	@Override
	public void paint(Graphics g0, JComponent c) {
		AbstractButton button = (AbstractButton) c;
		Graphics2D g = (Graphics2D) g0.create();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
		boolean enabled = button.isEnabled();
		boolean pressed = enabled && button.getModel().isPressed();
		boolean hover = enabled && button.getModel().isRollover();
		int w = c.getWidth();
		int h = c.getHeight();

		Color face = !enabled ? new Color(0x4A, 0x4D, 0x53) : hover ? mix(base, Color.WHITE, 0.16f) : base;
		Color light = mix(face, Color.WHITE, pressed ? 0.0f : 0.38f);
		Color dark = mix(face, Color.BLACK, pressed ? 0.0f : 0.38f);
		if (pressed) {
			face = mix(face, Color.BLACK, 0.12f);
		}

		// outline with notched corners
		g.setColor(OUTLINE);
		g.fillRect(2 * U, 0, w - 4 * U, h);
		g.fillRect(0, 2 * U, w, h - 4 * U);
		g.fillRect(U, U, w - 2 * U, h - 2 * U);
		// face
		g.setColor(face);
		g.fillRect(3 * U, U, w - 6 * U, h - 2 * U);
		g.fillRect(U, 3 * U, w - 2 * U, h - 6 * U);
		g.fillRect(2 * U, 2 * U, w - 4 * U, h - 4 * U);
		// bevel: light on top and left, shadow on the bottom and right
		g.setColor(light);
		g.fillRect(3 * U, U, w - 6 * U, U);
		g.fillRect(U, 3 * U, U, h - 6 * U);
		g.fillRect(2 * U, 2 * U, U, U);
		g.setColor(dark);
		g.fillRect(3 * U, h - 2 * U, w - 6 * U, U);
		g.fillRect(w - 2 * U, 3 * U, U, h - 6 * U);
		g.fillRect(w - 3 * U, h - 3 * U, U, U);
		if (hover) { // a row of lighter pixels along the top, like a screen scanline
			g.setColor(new Color(255, 255, 255, 40));
			g.fillRect(3 * U, 3 * U, w - 6 * U, U);
		}

		int sink = pressed ? U : 0;
		String text = button.getText() == null ? "" : button.getText().toUpperCase();
		Font font = new Font(Font.MONOSPACED, Font.BOLD, Math.max(11, button.getFont().getSize() - (button.getFont().getSize() >= 20 ? 3 : 0)));
		g.setFont(font);
		FontMetrics metrics = g.getFontMetrics();
		int textWidth = metrics.stringWidth(text);
		int arrow = h >= 44 ? Math.max(3, h / (4 * U)) : 0; // height of the pixel arrow, in pixel units
		int arrowWidth = arrow > 0 ? (arrow * U) : 0;
		int gap = arrow > 0 ? 3 * U : 0;
		int startX = (w - (arrowWidth + gap + textWidth)) / 2 + sink;
		int centerY = h / 2 + sink;
		Color ink = enabled ? Color.WHITE : new Color(0xA0, 0xA3, 0xA8);
		if (arrow > 0) {
			// arrow pointing right made of columns, from tall to short
			for (int i = 0; i < arrow; i++) {
				int half = (arrow - i) * U;
				g.setColor(OUTLINE);
				g.fillRect(startX + i * U + U, centerY - half + U, U, half * 2);
				g.setColor(ink);
				g.fillRect(startX + i * U, centerY - half, U, half * 2);
			}
		}
		int textX = startX + arrowWidth + gap;
		int textY = centerY + (metrics.getAscent() - metrics.getDescent()) / 2;
		g.setColor(OUTLINE);
		g.drawString(text, textX + U, textY + U);
		g.setColor(ink);
		g.drawString(text, textX, textY);
		g.dispose();
	}

	@Override
	public Dimension getPreferredSize(JComponent c) {
		AbstractButton button = (AbstractButton) c;
		FontMetrics metrics = c.getFontMetrics(button.getFont());
		int width = (button.getText() == null ? 0 : metrics.stringWidth(button.getText())) + 48;
		return new Dimension(width, Math.max(34, metrics.getHeight() + 16));
	}

	private static Color mix(Color a, Color b, float amount) {
		return new Color(Math.round(a.getRed() + (b.getRed() - a.getRed()) * amount),
				Math.round(a.getGreen() + (b.getGreen() - a.getGreen()) * amount),
				Math.round(a.getBlue() + (b.getBlue() - a.getBlue()) * amount));
	}
}

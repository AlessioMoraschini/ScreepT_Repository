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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.UIManager;

/**
 * Colors of the card based panels (quick notes, release notes), derived from the colors of the active look and feel
 * (panel, text and accent), so they match the theme selected in the graphic preferences.
 */
public final class CardPalette {

	public final boolean dark;
	public final Color background;
	public final Color card;
	public final Color cardHover;
	public final Color cardDone;
	public final Color border;
	public final Color text;
	public final Color muted;
	public final Color accent;
	public final Color onAccent;
	public final Color highlight;
	public final Color selection;
	public final Color danger;
	public final Color badgeNew;
	public final Color badgeImproved;
	public final Color badgeFixed;
	public final Color onBadge;

	private CardPalette(Color panel, Color foreground, Color accentColor, boolean dark) {
		this.dark = dark;
		// surfaces follow the selected look and feel: cards are the panel color nudged towards white (or black), so the
		// panel keeps the shape of the modern cards while staying in the colors of the theme
		background = panel;
		card = dark ? blend(panel, Color.WHITE, 0.06) : blend(panel, Color.WHITE, 0.70);
		cardHover = dark ? blend(panel, Color.WHITE, 0.11) : blend(panel, Color.WHITE, 0.88);
		cardDone = dark ? blend(panel, Color.BLACK, 0.12) : blend(panel, Color.BLACK, 0.04);
		border = blend(panel, foreground, dark ? 0.20 : 0.16);
		text = readable(foreground, card);
		muted = blend(text, card, 0.45);
		accent = accentColor;
		onAccent = contrast(Color.WHITE, accentColor) >= contrast(new Color(0x1C1F25), accentColor) ? Color.WHITE : new Color(0x1C1F25);
		highlight = dark ? new Color(0x806020) : new Color(0xFFE38A);
		selection = blend(accentColor, panel, dark ? 0.55 : 0.70);
		danger = dark ? new Color(0xE57373) : new Color(0xC0392B);
		badgeNew = dark ? new Color(0x4CAF7A) : new Color(0x2E8B57);
		badgeImproved = dark ? new Color(0x5B8DEF) : new Color(0x3A6FD8);
		badgeFixed = dark ? new Color(0xE0895A) : new Color(0xC8622D);
		onBadge = dark ? new Color(0x15171C) : Color.WHITE;
	}

	public static CardPalette current() {
		Color panel = firstColor("Panel.background", "control");
		if (panel == null) {
			panel = new Color(0xF0F0F0);
		}
		Color foreground = firstColor("Label.foreground", "textText");
		boolean dark = luminance(panel) < 0.45;
		if (foreground == null) {
			foreground = dark ? new Color(0xE4E6EB) : new Color(0x2A2A2A);
		}
		return new CardPalette(panel, foreground, themeAccent(dark), dark);
	}

	/** First saturated color among the accent/selection colors of the look and feel, amber when the theme has none */
	private static Color themeAccent(boolean dark) {
		String[] keys = {"Component.accentColor", "Button.default.background", "List.selectionBackground",
				"TextField.selectionBackground", "nimbusSelectionBackground", "nimbusFocus", "textHighlight"};
		for (String key : keys) {
			Color candidate = UIManager.getColor(key);
			if (candidate != null && saturation(candidate) >= 0.3f) {
				return new Color(candidate.getRGB());
			}
		}
		return dark ? new Color(0xF2B84B) : new Color(0xE0A100);
	}

	private static Color firstColor(String... keys) {
		for (String key : keys) {
			Color color = UIManager.getColor(key);
			if (color != null) {
				return new Color(color.getRGB());
			}
		}
		return null;
	}

	private static Color blend(Color from, Color to, double amount) {
		return new Color(
				(int) Math.round(from.getRed() + (to.getRed() - from.getRed()) * amount),
				(int) Math.round(from.getGreen() + (to.getGreen() - from.getGreen()) * amount),
				(int) Math.round(from.getBlue() + (to.getBlue() - from.getBlue()) * amount));
	}

	/** The color itself when it reads on the background, else black or white */
	private static Color readable(Color color, Color background) {
		if (contrast(color, background) >= 4.5) {
			return color;
		}
		return contrast(Color.WHITE, background) >= contrast(Color.BLACK, background) ? Color.WHITE : new Color(0x1C1F25);
	}

	private static float saturation(Color c) {
		return Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null)[1];
	}

	private static double contrast(Color first, Color second) {
		double l1 = relativeLuminance(first);
		double l2 = relativeLuminance(second);
		return (Math.max(l1, l2) + 0.05) / (Math.min(l1, l2) + 0.05);
	}

	/** WCAG relative luminance */
	private static double relativeLuminance(Color c) {
		return 0.2126 * linear(c.getRed()) + 0.7152 * linear(c.getGreen()) + 0.0722 * linear(c.getBlue());
	}

	private static double linear(int channel) {
		double value = channel / 255.0;
		return value <= 0.03928 ? value / 12.92 : Math.pow((value + 0.055) / 1.055, 2.4);
	}

	/**
	 * Nimbus paints its own white background under text components, ignoring setOpaque(false): the cards would show white
	 * boxes. The basic UI (which honors the transparency) is installed instead, only under Nimbus.
	 */
	public static void flatten(javax.swing.text.JTextComponent component) {
		if (!"Nimbus".equals(UIManager.getLookAndFeel().getName())) {
			return;
		}
		if (component instanceof javax.swing.JTextArea) {
			component.setUI(new javax.swing.plaf.basic.BasicTextAreaUI());
		} else if (component instanceof javax.swing.JTextField) {
			component.setUI(new javax.swing.plaf.basic.BasicTextFieldUI());
		}
		component.setOpaque(false);
	}

	public static void antialias(Graphics2D g2) {
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	}

	private static double luminance(Color c) {
		return (0.2126 * c.getRed() + 0.7152 * c.getGreen() + 0.0722 * c.getBlue()) / 255d;
	}
}

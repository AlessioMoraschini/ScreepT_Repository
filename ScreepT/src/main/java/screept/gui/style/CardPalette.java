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
 * Colors of the card based panels (quick notes, release notes), derived from the active look and feel:
 * warm paper on light themes, slate on dark ones.
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

	private CardPalette(boolean dark) {
		this.dark = dark;
		if (dark) {
			background = new Color(0x1C1F25);
			card = new Color(0x272B33);
			cardHover = new Color(0x2F343E);
			cardDone = new Color(0x22252C);
			border = new Color(0x363B46);
			text = new Color(0xE4E6EB);
			muted = new Color(0x8A93A3);
			accent = new Color(0xF2B84B);
			onAccent = new Color(0x1C1F25);
			highlight = new Color(0x806020);
			selection = new Color(0x3E4C66);
			danger = new Color(0xE57373);
			badgeNew = new Color(0x4CAF7A);
			badgeImproved = new Color(0x5B8DEF);
			badgeFixed = new Color(0xE0895A);
			onBadge = new Color(0x15171C);
		} else {
			background = new Color(0xF5F3EE);
			card = Color.WHITE;
			cardHover = new Color(0xFFFCF2);
			cardDone = new Color(0xEFEDE7);
			border = new Color(0xE0DCD2);
			text = new Color(0x2A2A2A);
			muted = new Color(0x8C877B);
			accent = new Color(0xE0A100);
			onAccent = Color.WHITE;
			highlight = new Color(0xFFE38A);
			selection = new Color(0xCFE0F7);
			danger = new Color(0xC0392B);
			badgeNew = new Color(0x2E8B57);
			badgeImproved = new Color(0x3A6FD8);
			badgeFixed = new Color(0xC8622D);
			onBadge = Color.WHITE;
		}
	}

	public static CardPalette current() {
		Color base = UIManager.getColor("Panel.background");
		return new CardPalette(base != null && luminance(base) < 0.45);
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

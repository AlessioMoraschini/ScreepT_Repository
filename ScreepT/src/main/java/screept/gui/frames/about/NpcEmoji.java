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
package screept.gui.frames.about;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.Icon;

/**
 * The faces of the assistant, drawn as vectors (no emoji font needed, same look on every system): an angry one, red, with
 * slanted brows and a frown, and a thinking one, with a raised eyebrow and a hand on the chin.
 */
final class NpcEmoji implements Icon {

	private final NpcMood mood;
	private final int size;

	NpcEmoji(NpcMood mood, int size) {
		this.mood = mood;
		this.size = size;
	}

	@Override
	public int getIconWidth() {
		return size;
	}

	@Override
	public int getIconHeight() {
		return size;
	}

	@Override
	public void paintIcon(Component c, Graphics graphics, int x, int y) {
		Graphics2D g = (Graphics2D) graphics.create();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.translate(x, y);
		if (mood == NpcMood.ANGRY) {
			paintAngry(g);
		} else {
			paintThinking(g);
		}
		g.dispose();
	}

	private void face(Graphics2D g, Color top, Color bottom, Color outline) {
		g.setPaint(new java.awt.GradientPaint(0, 0, top, 0, size, bottom));
		g.fill(new Ellipse2D.Double(1, 1, size - 2, size - 2));
		g.setColor(outline);
		g.setStroke(new BasicStroke(Math.max(1f, size / 28f)));
		g.draw(new Ellipse2D.Double(1, 1, size - 2, size - 2));
	}

	private void paintAngry(Graphics2D g) {
		face(g, new Color(0xFF, 0x6A, 0x4D), new Color(0xD8, 0x2B, 0x20), new Color(0x8E, 0x16, 0x10));
		float stroke = size / 9f;
		g.setColor(new Color(0x2A, 0x0A, 0x08));
		g.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		// brows slanting down to the middle
		g.draw(new Line2D.Double(size * 0.17, size * 0.27, size * 0.43, size * 0.40));
		g.draw(new Line2D.Double(size * 0.83, size * 0.27, size * 0.57, size * 0.40));
		// small furious eyes
		g.fill(new Ellipse2D.Double(size * 0.29, size * 0.45, size * 0.13, size * 0.15));
		g.fill(new Ellipse2D.Double(size * 0.58, size * 0.45, size * 0.13, size * 0.15));
		// frown
		g.setStroke(new BasicStroke(size / 12f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		g.draw(new Arc2D.Double(size * 0.28, size * 0.62, size * 0.44, size * 0.30, 25, 130, Arc2D.OPEN));
		// forehead vein
		g.setColor(new Color(0x5A, 0x0A, 0x08));
		g.setStroke(new BasicStroke(size / 22f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		g.draw(new Arc2D.Double(size * 0.64, size * 0.08, size * 0.16, size * 0.16, 90, 200, Arc2D.OPEN));
		g.draw(new Arc2D.Double(size * 0.72, size * 0.08, size * 0.16, size * 0.16, 270, 200, Arc2D.OPEN));
	}

	private void paintThinking(Graphics2D g) {
		face(g, new Color(0xFF, 0xDD, 0x67), new Color(0xF2, 0xB1, 0x2C), new Color(0xB9, 0x7A, 0x10));
		g.setStroke(new BasicStroke(size / 14f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		Color ink = new Color(0x4A, 0x2E, 0x08);
		// raised eyebrow on the left, flat on the right
		g.setColor(ink);
		g.draw(new Arc2D.Double(size * 0.16, size * 0.14, size * 0.30, size * 0.22, 20, 140, Arc2D.OPEN));
		g.draw(new Line2D.Double(size * 0.57, size * 0.30, size * 0.82, size * 0.30));
		// eyes looking up and to the side
		g.setColor(Color.WHITE);
		g.fill(new Ellipse2D.Double(size * 0.22, size * 0.36, size * 0.20, size * 0.24));
		g.fill(new Ellipse2D.Double(size * 0.58, size * 0.36, size * 0.20, size * 0.24));
		g.setColor(ink);
		g.fill(new Ellipse2D.Double(size * 0.30, size * 0.37, size * 0.10, size * 0.12));
		g.fill(new Ellipse2D.Double(size * 0.66, size * 0.37, size * 0.10, size * 0.12));
		// mouth, a bit crooked
		g.setStroke(new BasicStroke(size / 14f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		g.draw(new Line2D.Double(size * 0.24, size * 0.72, size * 0.52, size * 0.68));
		// hand on the chin, finger on the cheek
		Color skin = new Color(0xFF, 0xC9, 0x8A);
		Color skinLine = new Color(0xB9, 0x7A, 0x10);
		g.setColor(skin);
		g.fill(new RoundRectangle2D.Double(size * 0.55, size * 0.66, size * 0.34, size * 0.22, size * 0.14, size * 0.14));
		g.fill(new RoundRectangle2D.Double(size * 0.58, size * 0.50, size * 0.11, size * 0.26, size * 0.10, size * 0.10));
		g.setColor(skinLine);
		g.setStroke(new BasicStroke(size / 30f));
		g.draw(new RoundRectangle2D.Double(size * 0.55, size * 0.66, size * 0.34, size * 0.22, size * 0.14, size * 0.14));
		g.draw(new RoundRectangle2D.Double(size * 0.58, size * 0.50, size * 0.11, size * 0.26, size * 0.10, size * 0.10));
	}
}

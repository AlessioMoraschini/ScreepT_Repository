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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/** The answer of the assistant: its face on the left, and on the right its words, the quote, the fact, the heat and the tip */
public final class NpcBubble extends JPanel {

	private static final long serialVersionUID = 1L;

	/** Width of the text in pixels: the answer wraps here and never makes the window wider */
	private static final int TEXT_WIDTH = 330;
	private static final int FACE_SIZE = 38;

	private final JLabel face = new JLabel();
	private final JLabel text = new JLabel();
	private final Color textColor;
	private final Color quoteColor;
	private final Color mutedColor;
	private final Color factColor;
	private final Color heatColor;

	public NpcBubble(Font font, Color textColor, Color quoteColor, Color mutedColor, Color factColor, Color heatColor) {
		super(new BorderLayout(12, 0));
		this.textColor = textColor;
		this.quoteColor = quoteColor;
		this.mutedColor = mutedColor;
		this.factColor = factColor;
		this.heatColor = heatColor;
		setOpaque(false);
		setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
		face.setVerticalAlignment(SwingConstants.TOP);
		text.setVerticalAlignment(SwingConstants.TOP);
		text.setFont(font);
		add(face, BorderLayout.WEST);
		add(text, BorderLayout.CENTER);
		setVisible(false);
	}

	public void show(NpcReply reply) {
		face.setIcon(new NpcEmoji(reply.mood, FACE_SIZE));
		StringBuilder html = new StringBuilder("<html><div style='width:" + TEXT_WIDTH + "px'>");
		html.append(span(escape(reply.opener), textColor));
		if (reply.quote != null) {
			html.append("<br>").append("<i>").append(span("“" + escape(reply.quote.text) + "”", quoteColor)).append("</i> ")
					.append(span("— " + escape(reply.quote.movie), mutedColor));
		}
		if (reply.fact != null) {
			html.append("<br>").append(span(escape(reply.fact), factColor));
		}
		if (reply.heatLine != null) {
			html.append("<br><b>").append(span(escape(reply.heatLine), heatColor)).append("</b>");
		}
		if (reply.hint != null) {
			html.append("<br><i>").append(span(escape(reply.hint), mutedColor)).append("</i>");
		}
		html.append("</div></html>");
		text.setText(html.toString());
		setVisible(true);
		revalidate();
		repaint();
	}

	private static String span(String content, Color color) {
		return "<font color='" + String.format("#%06X", color.getRGB() & 0xFFFFFF) + "'>" + content + "</font>";
	}

	private static String escape(String content) {
		return content.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
	}
}

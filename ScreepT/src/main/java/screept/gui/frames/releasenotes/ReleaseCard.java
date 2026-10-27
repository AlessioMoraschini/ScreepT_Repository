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
package screept.gui.frames.releasenotes;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;

import net.miginfocom.swing.MigLayout;
import screept.gui.frames.releasenotes.ReleaseNotes.Release;
import screept.gui.style.CardPalette;

/**
 * Card of a single release: version, date, highlight and the New / Improved / Fixed items with colored badges.
 * Collapsed cards show only the header and the highlight; a click on the header toggles them.
 */
public class ReleaseCard extends JPanel {

	private static final long serialVersionUID = -6013296718409322187L;

	private static final int ARC = 16;

	private final CardPalette palette;
	private final boolean installed;

	/**
	 * @param terms     lower case search terms: only matching items are listed and highlighted (empty = all)
	 * @param onToggle  called when the header is clicked, null for a card that cannot collapse
	 */
	public ReleaseCard(Release release, boolean installed, boolean expanded, String[] terms, CardPalette palette, Runnable onToggle) {
		this.palette = palette;
		this.installed = installed;
		setOpaque(false);
		setLayout(new MigLayout("insets 14 18 14 18, fillx, wrap 1, gapy 4", "[grow,fill]"));

		Font base = UIManager.getFont("Label.font");

		JPanel header = new JPanel(new MigLayout("insets 0, fillx", "[][][]push[]", "[]"));
		header.setOpaque(false);
		JLabel versionLbl = new JLabel("ScreepT " + release.version);
		versionLbl.setFont(base.deriveFont(Font.BOLD, base.getSize2D() + 6f));
		versionLbl.setForeground(palette.text);
		JLabel dateLbl = new JLabel(release.date);
		dateLbl.setFont(base.deriveFont(base.getSize2D() - 0.5f));
		dateLbl.setForeground(palette.muted);
		header.add(versionLbl, "aligny baseline");
		header.add(dateLbl, "aligny baseline, gapleft 6");
		header.add(installed ? new Badge("INSTALLED", palette.accent, palette.onAccent) : new JLabel(), "aligny center, gapleft 6");
		if (onToggle != null) {
			JLabel arrow = new JLabel(expanded ? "\u25BE" : "\u25B8");
			arrow.setFont(base.deriveFont(base.getSize2D() + 4f));
			arrow.setForeground(palette.muted);
			header.add(arrow, "aligny center");
			header.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			header.setToolTipText(expanded ? "Collapse" : "Show details");
			header.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					onToggle.run();
				}
			});
		}
		add(header);

		if (!release.getHighlight().isEmpty()) {
			JTextArea highlight = text(release.getHighlight(), base.deriveFont(Font.ITALIC, base.getSize2D() + 1.5f), palette.text, terms);
			add(highlight, "gaptop 2");
		}

		if (expanded) {
			for (Map.Entry<String, List<String>> section : release.getSections().entrySet()) {
				boolean first = true;
				for (String item : section.getValue()) {
					if (!matchesAll(item, terms) && !matchesAll(release.getHighlight() + " " + release.version, terms)) {
						continue;
					}
					if (first) {
						add(new Badge(section.getKey().toUpperCase(Locale.ROOT), badgeColor(section.getKey()), palette.onBadge), "gaptop 10, growx 0");
						first = false;
					}
					add(itemRow(item, base, terms), "gaptop 2");
				}
			}
		}
	}

	private JComponent itemRow(String item, Font base, String[] terms) {
		JPanel row = new JPanel(new MigLayout("insets 0 4 0 0, fillx, gap 8 0", "[][grow,fill]", "[top]"));
		row.setOpaque(false);
		JLabel bullet = new JLabel("\u2022");
		bullet.setFont(base.deriveFont(Font.BOLD, base.getSize2D() + 2f));
		bullet.setForeground(palette.muted);
		row.add(bullet, "aligny top");
		row.add(text(item, base.deriveFont(base.getSize2D() + 1f), palette.text, terms), "wmin 10");
		return row;
	}

	private JTextArea text(String content, Font font, Color color, String[] terms) {
		JTextArea area = new JTextArea(content);
		area.setFont(font);
		area.setForeground(color);
		area.setLineWrap(true);
		area.setWrapStyleWord(true);
		area.setEditable(false);
		area.setOpaque(false);
		area.setBorder(BorderFactory.createEmptyBorder());
		area.setSelectionColor(palette.selection);
		area.setSelectedTextColor(palette.text);
		Highlighter.HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(palette.highlight);
		String lower = content.toLowerCase(Locale.ROOT);
		for (String term : terms) {
			for (int i = lower.indexOf(term); i >= 0; i = lower.indexOf(term, i + term.length())) {
				try {
					area.getHighlighter().addHighlight(i, i + term.length(), painter);
				} catch (BadLocationException e) {
					// highlight skipped
				}
			}
		}
		return area;
	}

	private Color badgeColor(String section) {
		String s = section.toLowerCase(Locale.ROOT);
		if (s.startsWith("new") || s.startsWith("add")) {
			return palette.badgeNew;
		}
		if (s.startsWith("fix") || s.startsWith("bug")) {
			return palette.badgeFixed;
		}
		return palette.badgeImproved;
	}

	static boolean matchesAll(String text, String[] terms) {
		String lower = text.toLowerCase(Locale.ROOT);
		for (String term : terms) {
			if (!lower.contains(term)) {
				return false;
			}
		}
		return true;
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g.create();
		CardPalette.antialias(g2);
		g2.setColor(palette.card);
		g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, ARC, ARC);
		g2.setColor(installed ? palette.accent : palette.border);
		g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, ARC, ARC);
		if (installed) {
			// accent strip on the left edge of the installed release
			g2.fillRoundRect(0, 0, 5, getHeight() - 1, 5, 5);
		}
		g2.dispose();
		super.paintComponent(g);
	}

	/** Small pill label */
	static final class Badge extends JLabel {
		private static final long serialVersionUID = 1L;
		private final Color fill;

		Badge(String text, Color fill, Color foreground) {
			super(text);
			this.fill = fill;
			Font base = UIManager.getFont("Label.font");
			setFont(base.deriveFont(Font.BOLD, base.getSize2D() - 2f));
			setForeground(foreground);
			setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
		}

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			CardPalette.antialias(g2);
			g2.setColor(fill);
			g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
			g2.dispose();
			super.paintComponent(g);
		}
	}
}

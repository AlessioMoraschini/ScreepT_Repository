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

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.log4j.Logger;

import net.miginfocom.swing.MigLayout;
import screept.gui.frames.releasenotes.ReleaseNotes.Release;
import screept.gui.style.CardPalette;
import screept.gui.style.RoundTextField;
import screept.gui.style.ScrollableListPanel;

/**
 * Release notes viewer: one card per version, the two latest expanded, live search on everything.
 * The raw git log (technical) is available collapsed at the bottom.
 */
public class ReleaseNotesPanel extends JPanel {

	private static final long serialVersionUID = 2311608954031713340L;

	private static final Logger logger = Logger.getLogger(ReleaseNotesPanel.class);

	private static final int EXPANDED_BY_DEFAULT = 2;

	private final List<Release> releases;
	private final String installedVersion;
	private final File technicalLog;
	private final Set<String> toggled = new HashSet<>();
	private boolean technicalLogExpanded;

	private CardPalette palette = CardPalette.current();
	private final JLabel titleLbl = new JLabel("What's new");
	private final JLabel subtitleLbl = new JLabel();
	private final RoundTextField searchField = new RoundTextField("Search changes", true, () -> palette);
	private final ScrollableListPanel listPanel = new ScrollableListPanel("6 18 18 18", 10);

	public ReleaseNotesPanel(List<Release> releases, String installedVersion, File technicalLog) {
		this.releases = releases;
		this.installedVersion = installedVersion;
		this.technicalLog = technicalLog;

		setLayout(new BorderLayout());
		JPanel header = new JPanel(new MigLayout("insets 16 18 8 18, fillx, gapy 4", "[grow][260:320:420]", "[][]"));
		header.setOpaque(false);
		header.add(titleLbl, "split 2, flowy");
		header.add(subtitleLbl);
		header.add(searchField, "growx, aligny bottom");
		add(header, BorderLayout.NORTH);

		JScrollPane scroll = new JScrollPane(listPanel);
		scroll.setBorder(BorderFactory.createEmptyBorder());
		scroll.setOpaque(false);
		scroll.getViewport().setOpaque(false);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scroll.getVerticalScrollBar().setUnitIncrement(18);
		add(scroll, BorderLayout.CENTER);

		searchField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				rebuild();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				rebuild();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				rebuild();
			}
		});
		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK), "rn-find");
		getActionMap().put("rn-find", new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				searchField.requestFocusInWindow();
				searchField.selectAll();
			}
		});

		applyPalette();
		SwingUtilities.invokeLater(() -> scroll.getVerticalScrollBar().setValue(0));
	}

	public void applyPalette() {
		palette = CardPalette.current();
		Font base = UIManager.getFont("Label.font");
		setBackground(palette.background);
		titleLbl.setFont(base.deriveFont(Font.BOLD, base.getSize2D() + 9f));
		titleLbl.setForeground(palette.text);
		subtitleLbl.setFont(base.deriveFont(base.getSize2D() + 0.5f));
		subtitleLbl.setForeground(palette.muted);
		subtitleLbl.setText("Installed version " + installedVersion + "  \u00B7  " + releases.size() + " releases");
		searchField.setFont(base.deriveFont(base.getSize2D() + 1f));
		searchField.applyPalette();
		rebuild();
	}

	private void rebuild() {
		String query = searchField.getText().trim().toLowerCase(Locale.ROOT);
		String[] terms = query.isEmpty() ? new String[0] : query.split("\\s+");

		listPanel.removeAll();
		int shown = 0;
		for (int i = 0; i < releases.size(); i++) {
			Release release = releases.get(i);
			if (terms.length > 0 && !matches(release, terms)) {
				continue;
			}
			boolean expanded = terms.length > 0 || (i < EXPANDED_BY_DEFAULT) != toggled.contains(release.version);
			Runnable toggle = terms.length > 0 ? null : () -> {
				if (!toggled.remove(release.version)) {
					toggled.add(release.version);
				}
				rebuild();
			};
			listPanel.add(new ReleaseCard(release, release.version.equals(installedVersion), expanded, terms, palette, toggle));
			shown++;
		}
		if (shown == 0) {
			JLabel empty = new JLabel(releases.isEmpty() ? "No release notes available." : "No changes match \"" + query + "\"", SwingConstants.CENTER);
			empty.setForeground(palette.muted);
			listPanel.add(empty, "gaptop 30");
		}
		if (terms.length == 0 && technicalLog != null && technicalLog.isFile()) {
			listPanel.add(technicalLogCard(), "gaptop 14");
		}
		listPanel.revalidate();
		listPanel.repaint();
	}

	private static boolean matches(Release release, String[] terms) {
		StringBuilder all = new StringBuilder(release.version).append(' ').append(release.date).append(' ').append(release.getHighlight());
		release.getSections().forEach((section, items) -> items.forEach(item -> all.append(' ').append(item)));
		if (ReleaseCard.matchesAll(all.toString(), terms)) {
			// at least one visible line must contain every term
			if (ReleaseCard.matchesAll(release.version + " " + release.getHighlight(), terms)) {
				return true;
			}
			for (List<String> items : release.getSections().values()) {
				for (String item : items) {
					if (ReleaseCard.matchesAll(item, terms)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/** Raw commit history, collapsed: kept for the technical details */
	private JComponent technicalLogCard() {
		JPanel card = new JPanel(new MigLayout("insets 10 18 12 18, fillx, wrap 1", "[grow,fill]")) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				CardPalette.antialias(g2);
				g2.setColor(palette.cardDone);
				g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
				g2.dispose();
				super.paintComponent(g);
			}
		};
		card.setOpaque(false);
		JLabel title = new JLabel((technicalLogExpanded ? "\u25BE  " : "\u25B8  ") + "Commit history (technical)");
		title.setFont(title.getFont().deriveFont(Font.BOLD));
		title.setForeground(palette.muted);
		title.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		title.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				technicalLogExpanded = !technicalLogExpanded;
				rebuild();
			}
		});
		card.add(title);
		if (technicalLogExpanded) {
			JTextArea log = new JTextArea();
			try {
				log.setText(new String(Files.readAllBytes(technicalLog.toPath()), StandardCharsets.UTF_8).replaceAll("(\r?\n){2,}", "\n").trim());
			} catch (Exception e) {
				logger.error("Cannot read " + technicalLog, e);
				log.setText("Cannot read " + technicalLog);
			}
			log.setFont(new Font(Font.MONOSPACED, Font.PLAIN, UIManager.getFont("Label.font").getSize()));
			log.setForeground(palette.muted);
			log.setEditable(false);
			log.setOpaque(false);
			log.setLineWrap(true);
			log.setWrapStyleWord(true);
			card.add(log, "wmin 10, gaptop 6");
		}
		return card;
	}
}

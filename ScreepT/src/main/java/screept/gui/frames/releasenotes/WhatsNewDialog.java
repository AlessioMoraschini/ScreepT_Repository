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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Window;
import java.io.File;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;

import org.apache.log4j.Logger;

import net.miginfocom.swing.MigLayout;
import screept.gui.frames.releasenotes.ReleaseNotes.Release;
import screept.gui.style.CardPalette;
import screept.gui.style.ScrollableListPanel;

/**
 * Shown once at the first start of a new version, with the notes of that version.
 * The last version shown is kept in the user preferences.
 */
public class WhatsNewDialog extends JDialog {

	private static final long serialVersionUID = -4693411806052370113L;

	private static final Logger logger = Logger.getLogger(WhatsNewDialog.class);

	private static final String PREFS_NODE = "screept/gui";
	private static final String KEY_LAST_SEEN = "lastSeenReleaseNotes";

	private WhatsNewDialog(Window owner, Release release, Runnable openFullChangelog) {
		super(owner, "What's new in ScreepT " + release.version, ModalityType.MODELESS);
		CardPalette palette = CardPalette.current();
		Font base = UIManager.getFont("Label.font");

		JPanel content = new JPanel(new BorderLayout());
		content.setBackground(palette.background);

		JLabel welcome = new JLabel("ScreepT has been updated");
		welcome.setFont(base.deriveFont(Font.BOLD, base.getSize2D() + 7f));
		welcome.setForeground(palette.text);
		JLabel subtitle = new JLabel("Here is what changed in this version");
		subtitle.setForeground(palette.muted);
		JPanel header = new JPanel(new MigLayout("insets 18 20 6 20, wrap 1, gapy 2"));
		header.setOpaque(false);
		header.add(welcome);
		header.add(subtitle);
		content.add(header, BorderLayout.NORTH);

		ScrollableListPanel list = new ScrollableListPanel("6 18 10 18", 8);
		list.add(new ReleaseCard(release, false, true, new String[0], palette, null));
		JScrollPane scroll = new JScrollPane(list);
		scroll.setBorder(BorderFactory.createEmptyBorder());
		scroll.setOpaque(false);
		scroll.getViewport().setOpaque(false);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scroll.getVerticalScrollBar().setUnitIncrement(18);
		content.add(scroll, BorderLayout.CENTER);

		JButton fullBtn = new JButton("Full changelog");
		fullBtn.addActionListener(e -> {
			dispose();
			openFullChangelog.run();
		});
		JButton okBtn = new JButton("Got it");
		okBtn.addActionListener(e -> dispose());
		JPanel footer = new JPanel(new MigLayout("insets 6 20 16 20, fillx", "[]push[]", "[]"));
		footer.setOpaque(false);
		footer.add(fullBtn);
		footer.add(okBtn, "w 100!");
		content.add(footer, BorderLayout.SOUTH);

		setContentPane(content);
		getRootPane().setDefaultButton(okBtn);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setMinimumSize(new Dimension(420, 320));

		float scale = base.getSize2D() / 12f;
		int width = (int) (640 * scale);
		Rectangle ownerBounds = owner.getBounds();
		int height = (int) Math.min(580 * scale, ownerBounds.height * 0.85);
		setBounds(ownerBounds.x + (ownerBounds.width - width) / 2, ownerBounds.y + (ownerBounds.height - height) / 2, width, height);
	}

	/**
	 * Shows the notes of the running version if not shown yet (once per version).
	 * Nothing happens when the notes do not describe the running version.
	 */
	public static void showIfNewVersion(Window owner, File releaseNotes, String version, Runnable openFullChangelog) {
		try {
			Preferences prefs = Preferences.userRoot().node(PREFS_NODE);
			if (version == null || version.equals(prefs.get(KEY_LAST_SEEN, null)) || releaseNotes == null || !releaseNotes.isFile()) {
				return;
			}
			List<Release> releases = ReleaseNotes.read(releaseNotes);
			for (Release release : releases) {
				if (release.version.equals(version)) {
					new WhatsNewDialog(owner, release, openFullChangelog).setVisible(true);
					break;
				}
			}
			prefs.put(KEY_LAST_SEEN, version);
		} catch (Exception e) {
			logger.warn("Cannot show what's new dialog", e);
		}
	}
}

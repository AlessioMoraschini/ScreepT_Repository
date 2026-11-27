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
package screept.gui.frames.releasenotes;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Window;
import java.io.File;
import java.util.List;
import java.util.prefs.Preferences;

import initializer.configs.impl.INItializer;
import maingUI.MainGUI;

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
			if (version == null || alreadySeen(version) || releaseNotes == null || !releaseNotes.isFile()) {
				return;
			}
			markSeen(version); // before showing: if the application ends abnormally it is not shown again
			List<Release> releases = ReleaseNotes.read(releaseNotes);
			for (Release release : releases) {
				if (release.version.equals(version)) {
					new WhatsNewDialog(owner, release, openFullChangelog).setVisible(true);
					break;
				}
			}
		} catch (Exception e) {
			logger.warn("Cannot show what's new dialog", e);
		}
	}

	/**
	 * The version is remembered in the user preferences and in the configuration file: the preferences alone can fail to be
	 * stored (registry not writable, other user) and the notes would come back at every start.
	 */
	public static boolean alreadySeen(String version) {
		try {
			if (version.equals(Preferences.userRoot().node(PREFS_NODE).get(KEY_LAST_SEEN, null))) {
				return true;
			}
		} catch (Exception e) {
			logger.debug("Cannot read the preferences: " + e.getMessage());
		}
		try {
			return version.equals(MainGUI.currentConfiguration.readFromFile(INItializer.GEN_OPT, KEY_LAST_SEEN));
		} catch (Exception e) {
			return false;
		}
	}

	public static void markSeen(String version) {
		try {
			Preferences prefs = Preferences.userRoot().node(PREFS_NODE);
			prefs.put(KEY_LAST_SEEN, version);
			prefs.flush();
		} catch (Exception e) {
			logger.debug("Cannot store the preferences: " + e.getMessage());
		}
		try {
			MainGUI.currentConfiguration.writeSingleValue(INItializer.GEN_OPT, KEY_LAST_SEEN, version);
		} catch (Exception e) {
			logger.warn("Cannot store the last seen release notes", e);
		}
	}

	/** True while a modal dialog (welcome question, license, name...) is on screen: the notes must wait, not take its focus */
	public static boolean otherModalDialogShowing() {
		for (Window w : Window.getWindows()) {
			if (w.isShowing() && w instanceof java.awt.Dialog && ((java.awt.Dialog) w).isModal()) {
				return true;
			}
		}
		return false;
	}
}

/**
 *
 * =========================================================================================
*  Copyright (C) 2019-2020
*
*  AM-Design-Development - (Alessio Moraschini) - All Rights Reserved
* =========================================================================================
*
* You should have received a copy of the license with this file.
* If not, please write to: info@am-design-development.com, or visit : https://www.am-design-development.com
*/
package screept.gui.frames;

import java.awt.Cursor;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

import net.miginfocom.swing.MigLayout;
import resources.GeneralConfig;
import resources.IconsPathConfigurator;

/**
 * Asked when ScreepT is restarted while "Hide launcher at start" is on: open the launcher this time, or restart
 * directly. "Keep as default" turns "Hide launcher at start" off for good. Esc cancels the restart.
 */
public final class RestartLauncherDialog extends JDialog {

	private static final long serialVersionUID = 6840413573210884351L;

	public enum Choice {
		/** do not restart */
		CANCEL,
		/** restart without the launcher, as always */
		DIRECT,
		/** restart showing the launcher */
		LAUNCHER
	}

	public static final class Result {
		public final Choice choice;
		/** only meaningful with Choice.LAUNCHER */
		public final boolean keepAsDefault;

		Result(Choice choice, boolean keepAsDefault) {
			this.choice = choice;
			this.keepAsDefault = keepAsDefault;
		}
	}

	private Choice choice = Choice.CANCEL;
	private final JCheckBox chckbxKeepDefault;

	public static Result ask(Window owner, boolean asAdmin) {
		RestartLauncherDialog dialog = new RestartLauncherDialog(owner, asAdmin);
		dialog.setVisible(true);
		return new Result(dialog.choice, dialog.chckbxKeepDefault.isSelected());
	}

	private RestartLauncherDialog(Window owner, boolean asAdmin) {
		super(owner, "Restart " + GeneralConfig.APPLICATION_SHORT_NAME, ModalityType.APPLICATION_MODAL);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		getContentPane().setLayout(new MigLayout("insets 18 22 14 22, gapy 10", "[]18[grow]", "[]6[]10[]20[]"));

		JLabel icon = new JLabel(new ImageIcon(IconsPathConfigurator.ICON_RESTART));
		icon.setVerticalAlignment(SwingConstants.TOP);
		getContentPane().add(icon, "cell 0 0 1 3, aligny top");

		JLabel title = new JLabel("Restart " + GeneralConfig.APPLICATION_SHORT_NAME + (asAdmin ? " as administrator" : "") + "?");
		title.setFont(title.getFont().deriveFont(Font.BOLD, title.getFont().getSize2D() + 4f));
		getContentPane().add(title, "cell 1 0");

		JLabel text = new JLabel("<html>The launcher is hidden at start (<i>Hide launcher at start</i> is on).<br>"
				+ "Do you want to open it on this restart, to check the Java version and the start options?</html>");
		getContentPane().add(text, "cell 1 1");

		chckbxKeepDefault = new JCheckBox("Keep as default: always show the launcher at start");
		chckbxKeepDefault.setToolTipText("<html>Turns off <i>Hide launcher at start</i> in the launcher configuration, like in Generic Options.<br>"
				+ "Only used with \"Open launcher\"; without it the launcher is shown just this time.</html>");
		chckbxKeepDefault.setFocusable(false);
		getContentPane().add(chckbxKeepDefault, "cell 1 2");

		JButton btnLauncher = newButton("Open launcher", "Restart and show the launcher (just this time, unless \"Keep as default\" is checked)", Choice.LAUNCHER);
		JButton btnDirect = newButton("Restart directly", "Restart without the launcher, as usual", Choice.DIRECT);
		JButton btnCancel = newButton("Cancel", "Do not restart (Esc)", Choice.CANCEL);
		getContentPane().add(btnLauncher, "cell 0 3 2 1, split 3, align right, gapx 8");
		getContentPane().add(btnDirect, "gapx 8");
		getContentPane().add(btnCancel);
		getRootPane().setDefaultButton(btnLauncher);

		// Esc = cancel
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel-restart");
		getRootPane().getActionMap().put("cancel-restart", new AbstractAction() {
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				choice = Choice.CANCEL;
				dispose();
			}
		});

		pack();
		setResizable(false);
		setLocationRelativeTo(owner);
		btnLauncher.requestFocusInWindow();
	}

	private JButton newButton(String text, String tooltip, final Choice result) {
		JButton button = new JButton(text);
		button.setToolTipText(tooltip);
		button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		button.addActionListener(e -> {
			choice = result;
			dispose();
		});
		return button;
	}
}

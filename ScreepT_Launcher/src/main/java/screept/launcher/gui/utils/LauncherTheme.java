package screept.launcher.gui.utils;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.RootPaneContainer;
import javax.swing.UIManager;
import javax.swing.text.JTextComponent;

import org.apache.log4j.Logger;

import gui.commons.guistyle.laf.ScreepTDarkLaf;

/**
 * Look of the launcher windows: the same ScreepT Dark look and feel of the application. Only graphics:
 * colors, shapes and fonts come from the look and feel instead of being set one by one on each component.
 */
public final class LauncherTheme {

	private static final Logger logger = Logger.getLogger(LauncherTheme.class);

	/** Main action (Start, Save, Install) */
	public static final Color GREEN = new Color(61, 155, 92);
	/** Discard / dangerous action */
	public static final Color RED = new Color(201, 72, 66);

	private static boolean applied = false;

	private LauncherTheme() {
	}

	/** Applies the ScreepT Dark look and feel (once). Returns false if it cannot be applied: the default look stays. */
	public static synchronized boolean apply() {
		if (applied) {
			return true;
		}
		try {
			ScreepTDarkLaf.apply();
			// rounder, calmer controls
			UIManager.put("Button.arc", 10);
			UIManager.put("Component.arc", 8);
			UIManager.put("ProgressBar.arc", 8);
			UIManager.put("TextComponent.arc", 6);
			applied = true;
			return true;
		} catch (Throwable e) {
			logger.warn("Cannot apply the ScreepT Dark look and feel, the default one is used", e);
			return false;
		}
	}

	/**
	 * Removes the colors that were set by hand on the components of a window (gray buttons, black text, white tables...):
	 * the look and feel draws them. Call it after the window has been built and before showing it.
	 */
	public static void polish(Container root) {
		if (root instanceof RootPaneContainer) {
			Container content = ((RootPaneContainer) root).getContentPane();
			content.setBackground(null);
			content.setForeground(null);
			installEscapeToClose((RootPaneContainer) root);
		}
		walk(root);
	}

	/** Esc closes the window like its close button (the window keeps its own close logic) */
	private static void installEscapeToClose(final RootPaneContainer window) {
		if (!(window instanceof java.awt.Window)) {
			return;
		}
		javax.swing.JRootPane rootPane = window.getRootPane();
		rootPane.getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW)
				.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0), "launcher.escapeClose");
		rootPane.getActionMap().put("launcher.escapeClose", new javax.swing.AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				java.awt.Window w = (java.awt.Window) window;
				w.dispatchEvent(new java.awt.event.WindowEvent(w, java.awt.event.WindowEvent.WINDOW_CLOSING));
			}
		});
	}

	/** Green main action button */
	public static void primary(AbstractButton button) {
		accent(button, GREEN);
	}

	/** Red discard / dangerous action button */
	public static void danger(AbstractButton button) {
		accent(button, RED);
	}

	private static void accent(AbstractButton button, Color background) {
		if (button == null) {
			return;
		}
		button.putClientProperty("JButton.buttonType", "roundRect");
		button.setBackground(background);
		button.setForeground(Color.WHITE);
		button.setFocusPainted(false);
	}

	private static void walk(Container container) {
		for (Component child : container.getComponents()) {
			if (child instanceof JComponent) {
				reset((JComponent) child);
			}
			if (child instanceof Container) {
				walk((Container) child);
			}
		}
	}

	private static void reset(JComponent c) {
		if (c instanceof JButton || c instanceof JCheckBox || c instanceof JRadioButton) {
			c.setBackground(null);
			c.setForeground(null);
		} else if (c instanceof JLabel) {
			c.setForeground(null);
			if (c.isOpaque()) {
				c.setBackground(null);
			}
		} else if (c instanceof JTable) {
			JTable table = (JTable) c;
			table.setBackground(null);
			table.setForeground(null);
			table.setBorder(null);
			Color grid = UIManager.getColor("Table.gridColor");
			if (grid != null) {
				table.setGridColor(grid);
			}
			Color selectionBackground = UIManager.getColor("Table.selectionBackground");
			Color selectionForeground = UIManager.getColor("Table.selectionForeground");
			if (selectionBackground != null) {
				table.setSelectionBackground(selectionBackground);
			}
			if (selectionForeground != null) {
				table.setSelectionForeground(selectionForeground);
			}
			if (table.getTableHeader() != null) {
				table.getTableHeader().setBackground(null);
				table.getTableHeader().setForeground(null);
			}
		} else if (c instanceof JScrollPane) {
			c.setBackground(null);
			((JScrollPane) c).getViewport().setBackground(null);
		} else if (c instanceof JSpinner || c instanceof JComboBox || c instanceof JTextComponent) {
			c.setBackground(null);
			c.setForeground(null);
		} else if (c instanceof JPanel) {
			c.setBackground(null);
		}
	}
}

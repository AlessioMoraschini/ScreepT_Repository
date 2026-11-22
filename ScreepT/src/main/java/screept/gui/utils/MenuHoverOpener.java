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
package screept.gui.utils;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPopupMenu;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;

/**
 * Makes every menu of a menu bar (the main ones and the ones nested in sub menus, also those added later, such as the
 * plugins) open as soon as the mouse is over it, without a click.
 */
public final class MenuHoverOpener {

	private static AWTEventListener installed;

	private MenuHoverOpener() {
	}

	/** Idempotent: a second call just moves the listener to the given bar */
	public static synchronized void install(final JMenuBar bar) {
		uninstall();
		installed = new AWTEventListener() {
			@Override
			public void eventDispatched(AWTEvent event) {
				if (event.getID() == MouseEvent.MOUSE_ENTERED && event.getSource() instanceof JMenu) {
					JMenu menu = (JMenu) event.getSource();
					if (belongsTo(bar, menu)) {
						open(menu);
					}
				}
			}
		};
		Toolkit.getDefaultToolkit().addAWTEventListener(installed, AWTEvent.MOUSE_EVENT_MASK);
	}

	public static synchronized void uninstall() {
		if (installed != null) {
			Toolkit.getDefaultToolkit().removeAWTEventListener(installed);
			installed = null;
		}
	}

	/** True if the component is the bar, or hangs from it through menus and popups */
	static boolean belongsTo(JMenuBar bar, Component c) {
		for (int guard = 0; c != null && guard < 50; guard++) {
			if (c == bar) {
				return true;
			}
			c = c instanceof JPopupMenu ? ((JPopupMenu) c).getInvoker() : c.getParent();
		}
		return false;
	}

	/** Selection path that shows the popup of the menu, null if it cannot (or need not) be opened now */
	static MenuElement[] pathToOpen(JMenu menu, MenuElement[] current) {
		if (!menu.isEnabled() || menu.getItemCount() == 0) {
			return null;
		}
		for (MenuElement e : current) {
			if (e == menu) {
				return null; // already selected (its popup is open or opening)
			}
		}
		List<MenuElement> path = new ArrayList<MenuElement>();
		Component parent = menu.getParent();
		if (parent instanceof JMenuBar) {
			path.add((MenuElement) parent);
		} else {
			// a nested menu opens only from an open popup: keep the path down to that popup
			int at = -1;
			for (int i = 0; i < current.length; i++) {
				if (current[i] == parent) {
					at = i;
				}
			}
			if (at < 0) {
				return null;
			}
			for (int i = 0; i <= at; i++) {
				path.add(current[i]);
			}
		}
		path.add(menu);
		path.add(menu.getPopupMenu());
		return path.toArray(new MenuElement[0]);
	}

	private static void open(JMenu menu) {
		MenuSelectionManager manager = MenuSelectionManager.defaultManager();
		MenuElement[] path = pathToOpen(menu, manager.getSelectedPath());
		if (path != null) {
			manager.setSelectedPath(path);
		}
	}
}

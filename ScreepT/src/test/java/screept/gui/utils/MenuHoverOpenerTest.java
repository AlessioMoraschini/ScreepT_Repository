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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.MenuElement;

import org.junit.Test;

/** The selection paths that open menus on hover (no window is created or shown) */
public class MenuHoverOpenerTest {

	private final JMenuBar bar = new JMenuBar();
	private final JMenu file = new JMenu("File");
	private final JMenu recent = new JMenu("Recent");
	private final JMenu deeper = new JMenu("Deeper");
	private final JMenu empty = new JMenu("Empty");

	public MenuHoverOpenerTest() {
		bar.add(file);
		bar.add(empty);
		file.add(new JMenuItem("Open"));
		file.add(recent);
		recent.add(new JMenuItem("a.txt"));
		recent.add(deeper);
		deeper.add(new JMenuItem("x"));
	}

	@Test
	public void menusBelongToTheirBarThroughNestedPopups() {
		assertTrue(MenuHoverOpener.belongsTo(bar, file));
		assertTrue(MenuHoverOpener.belongsTo(bar, recent));
		assertTrue(MenuHoverOpener.belongsTo(bar, deeper));
		assertFalse(MenuHoverOpener.belongsTo(bar, new JMenu("Foreign")));
		assertFalse(MenuHoverOpener.belongsTo(new JMenuBar(), file));
	}

	@Test
	public void aMainMenuOpensFromNothing() {
		MenuElement[] path = MenuHoverOpener.pathToOpen(file, new MenuElement[0]);
		assertEquals(3, path.length);
		assertSame(bar, path[0]);
		assertSame(file, path[1]);
		assertSame(file.getPopupMenu(), path[2]);
	}

	@Test
	public void aNestedMenuOpensOnlyFromItsOpenParentAndKeepsThePath() {
		MenuElement[] open = { bar, file, file.getPopupMenu() };
		MenuElement[] path = MenuHoverOpener.pathToOpen(recent, open);
		assertEquals(5, path.length);
		assertSame(file.getPopupMenu(), path[2]);
		assertSame(recent, path[3]);
		assertSame(recent.getPopupMenu(), path[4]);
		// a sub menu of a menu that is not open is left alone
		assertNull(MenuHoverOpener.pathToOpen(deeper, open));
		// third level
		MenuElement[] deepOpen = path;
		MenuElement[] deepPath = MenuHoverOpener.pathToOpen(deeper, deepOpen);
		assertEquals(7, deepPath.length);
		assertSame(deeper, deepPath[5]);
	}

	@Test
	public void anotherSiblingReplacesTheOpenOne() {
		MenuElement[] openFile = { bar, file, file.getPopupMenu() };
		MenuElement[] path = MenuHoverOpener.pathToOpen(empty, openFile);
		assertNull("an empty menu has nothing to show", path);
		JMenu second = new JMenu("Second");
		second.add(new JMenuItem("s"));
		bar.add(second);
		path = MenuHoverOpener.pathToOpen(second, openFile);
		assertSame(second, path[1]);
		assertEquals(3, path.length);
	}

	@Test
	public void alreadySelectedOrDisabledMenusAreNotReopened() {
		assertNull(MenuHoverOpener.pathToOpen(file, new MenuElement[] { bar, file, file.getPopupMenu() }));
		file.setEnabled(false);
		assertNull(MenuHoverOpener.pathToOpen(file, new MenuElement[0]));
	}
}

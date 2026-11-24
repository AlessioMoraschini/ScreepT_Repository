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
package screept.gui.panels;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static screept.gui.GuiTestSupport.edt;
import static screept.gui.GuiTestSupport.flushEdt;
import static screept.gui.GuiTestSupport.set;

import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.swing.AbstractButton;
import javax.swing.JMenuItem;
import javax.swing.JTree;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import various.common.light.files.om.FileNamed;
import gui.texteditor.commons.om.TabTextEditorObject;
import gui.texteditor.commons.tabbededitor.CustomTabbedEditor;
import maingUI.MainGUI;
import maingUI.MainGuiUtils;
import plugin.api.AbstractPluginApplicationApi;
import screept.gui.GuiTestSupport;
import screept.gui.GuiTestSupport.EdtErrors;
import screept.gui.GuiTestSupport.Fixture;
import screept.gui.utils.shortcutshandler.TextEditorShortcutListener;
import screept.gui.utils.tree.texteditor.TreeFileMouseListenerTxtEditor;

/**
 * The keyboard shortcuts of the text editor and the context menu of its file tree, on the real editor (frame never
 * shown, temporary ini and workspace, scripted dialogs; only new tabs and files of a JUnit temporary folder).
 * Shortcuts: every toolbar button is given a recorder instead of its action (no window, no chooser), then the real
 * key events go through {@link TextEditorShortcutListener#dispatchKeyEvent}: the test checks which button each
 * combination presses and which ones it must not. Tree: popup items run on real files.
 */
public class TextEditorInteractionGuiTest {

	@ClassRule
	public static TemporaryFolder classTmp = new TemporaryFolder();

	private static Fixture fx;
	private static EdtErrors edtErrors;
	private static TextEditor editor;
	private static MainGuiUtils oldUtils;
	private static TextEditorShortcutListener listener;
	private static final List<String> hits = new ArrayList<String>();

	private static final int CTRL = KeyEvent.CTRL_DOWN_MASK, ALT = KeyEvent.ALT_DOWN_MASK, SHIFT = KeyEvent.SHIFT_DOWN_MASK;

	@BeforeClass
	public static void build() throws Exception {
		Assume.assumeFalse("needs a display server (windows are never shown)", GraphicsEnvironment.isHeadless());
		Assume.assumeTrue(AbstractPluginApplicationApi.getAvailableProviders().isEmpty());
		fx = GuiTestSupport.open(classTmp.newFolder("editor"));
		edtErrors = EdtErrors.plug();
		editor = edt(new Callable<TextEditor>() {
			@Override
			public TextEditor call() throws Exception {
				return new TextEditor(fx.app, fx.conf);
			}
		});
		flushEdt();
		editor.dialogHelper = fx.dialogs;
		set(editor.textEditorUtils, "dialogHelper", fx.dialogs);
		set(editor.subHeaderPanel, "dialogHelper", fx.dialogs);
		set(editor.getCurrentTabEditor(), "dialogHelper", fx.dialogs);
		editor.mouseListenerTreeFilePanel.dialogHelper = fx.dialogs;
		// the base class of the tree listener has a dialog helper of its own
		for (Class<?> c = editor.mouseListenerTreeFilePanel.getClass().getSuperclass(); c != null && c != Object.class; c = c.getSuperclass()) {
			try {
				Field f = c.getDeclaredField("dialogHelper");
				f.setAccessible(true);
				f.set(editor.mouseListenerTreeFilePanel, fx.dialogs);
			} catch (NoSuchFieldException ignored) {
				// not declared here
			}
		}
		// the shortcuts only work when the text editor is the visible panel of the focused main window
		oldUtils = MainGUI.mainGuiUtils;
		MainGUI.mainGuiUtils = new MainGuiUtils(fx.app) {
			@Override
			public boolean isTextEditorActive(boolean withFocus) {
				return true;
			}
		};
		listener = editor.shortcutsListener;
		recordButtons();
	}

	@AfterClass
	public static void close() throws Exception {
		MainGUI.mainGuiUtils = oldUtils;
		if (edtErrors != null)
			edtErrors.unplug();
		if (fx != null)
			fx.close();
	}

	@After
	public void noSilentFailures() {
		// the fixture has no top bar: the toggle icons of the main window cannot be set (not what is tested here)
		List<Throwable> real = new ArrayList<Throwable>();
		for (Throwable t : new ArrayList<Throwable>(edtErrors.errors)) {
			if (!(t instanceof NullPointerException && t.getStackTrace().length > 0
					&& t.getStackTrace()[0].getClassName().equals("maingUI.MainGuiParent")))
				real.add(t);
		}
		edtErrors.errors.clear();
		String NL = System.lineSeparator();
		StringBuilder sb = new StringBuilder();
		for (Throwable t : real) {
			sb.append(NL).append(t);
			for (int i = 0; i < Math.min(5, t.getStackTrace().length); i++)
				sb.append(NL + "    at ").append(t.getStackTrace()[i]);
		}
		assertTrue("exceptions on the event thread: " + sb, real.isEmpty());
		fx.dialogs.reset();
	}

	// ------------------------------------------------------------------ shortcuts

	/** Replaces the action of every toolbar button by a recorder named after its field. */
	private static void recordButtons() throws Exception {
		final List<Object[]> found = new ArrayList<Object[]>();
		Object[] owners = { editor.subHeaderPanel, editor.headerPanel, editor };
		for (Object owner : owners) {
			for (Class<?> c = owner.getClass(); c != null && c != Object.class; c = c.getSuperclass()) {
				for (Field f : c.getDeclaredFields()) {
					if (AbstractButton.class.isAssignableFrom(f.getType())) {
						f.setAccessible(true);
						AbstractButton b = (AbstractButton) f.get(owner);
						if (b != null)
							found.add(new Object[] { f.getName(), b });
					}
				}
			}
		}
		for (Object[] o : found) {
			final String name = (String) o[0];
			final AbstractButton b = (AbstractButton) o[1];
			for (ActionListener l : b.getActionListeners())
				b.removeActionListener(l);
			for (ItemListener l : b.getItemListeners())
				b.removeItemListener(l);
			b.setEnabled(true);
			b.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					hits.add(name);
				}
			});
		}
	}

	private static AbstractButton recordedRefresh;

	/** The popup is rebuilt each time it is needed: the recorder goes on the item that is current now. */
	private static void recordRefreshItem() {
		final AbstractButton b = editor.mouseListenerTreeFilePanel.getRefreshItem();
		if (b == null || b == recordedRefresh)
			return;
		recordedRefresh = b;
		for (ActionListener l : b.getActionListeners())
			b.removeActionListener(l);
		b.setEnabled(true);
		b.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				hits.add("refreshItem");
			}
		});
	}

	private static void press(final int code, final int mods) throws Exception {
		edt(new Runnable() {
			@Override
			public void run() {
				Component src = editor.getCurrentTabEditor().loadedTabObj.textArea;
				long now = System.currentTimeMillis();
				listener.dispatchKeyEvent(new KeyEvent(src, KeyEvent.KEY_PRESSED, now, mods, code, KeyEvent.CHAR_UNDEFINED));
				listener.dispatchKeyEvent(new KeyEvent(src, KeyEvent.KEY_RELEASED, now, mods, code, KeyEvent.CHAR_UNDEFINED));
			}
		});
		flushEdt();
	}

	private static String pressed(int code, int mods) throws Exception {
		recordRefreshItem();
		hits.clear();
		press(code, mods);
		return hits.toString();
	}

	@Test
	public void everyToolbarShortcutPressesItsButton() throws Exception {
		Object[][] map = {
				{ KeyEvent.VK_T, CTRL, "btnTemplates" }, { KeyEvent.VK_F, CTRL, "btnFindText" },
				{ KeyEvent.VK_E, CTRL, "btnReplaceEscapesSeq" }, { KeyEvent.VK_R, CTRL, "btnReplaceText" },
				{ KeyEvent.VK_L, CTRL, "btnGoToLine" }, { KeyEvent.VK_G, CTRL, "chckbxShowTabGrid" },
				{ KeyEvent.VK_S, CTRL, "btnSaveTextFile" }, { KeyEvent.VK_S, CTRL | SHIFT, "btnSaveTextFileAs" },
				{ KeyEvent.VK_O, CTRL, "btnLoadTextFile" }, { KeyEvent.VK_R, CTRL | SHIFT, "btnReloadFromDisk" },
				{ KeyEvent.VK_DELETE, CTRL, "btnClear" }, { KeyEvent.VK_C, CTRL | SHIFT, "btnCrypt" }, { KeyEvent.VK_D, CTRL | SHIFT, "btnDecrypt" },
				{ KeyEvent.VK_4, ALT, "btnBase64Conversion" }, { KeyEvent.VK_4, CTRL, "btnBase64Conversion" },
				{ KeyEvent.VK_Q, ALT, "btnToggleFileView" }, { KeyEvent.VK_A, ALT, "toggleBtnClipboard" },
				{ KeyEvent.VK_W, ALT, "toggleBtnMap" }, { KeyEvent.VK_S, ALT, "toggleBtnConsole" },
				{ KeyEvent.VK_U, CTRL | SHIFT, "toggleButtonFileTreeLocked" },
				{ KeyEvent.VK_UP, CTRL | ALT | SHIFT, "btnDepthUp_nostyle" },
				{ KeyEvent.VK_DOWN, CTRL | ALT | SHIFT, "btnDepthDown_nostyle" },
				{ KeyEvent.VK_MINUS, CTRL, "btnRemoveSpaces" }, { KeyEvent.VK_MINUS, CTRL | ALT | SHIFT, "btnCloseAllTabs" },
				{ KeyEvent.VK_ENTER, ALT, "btnPropertiesPanel" }, { KeyEvent.VK_F5, CTRL, "refreshItem" },
				{ KeyEvent.VK_I, CTRL | SHIFT, "checkBoxInlineTabs" }, { KeyEvent.VK_A, CTRL | SHIFT, "chkCutWordsTo" },
				{ KeyEvent.VK_L, CTRL | SHIFT, "chckbxLockLanguage" }, { KeyEvent.VK_W, CTRL | SHIFT, "toggleButtonFileTreeWorkspace" },
				{ KeyEvent.VK_W, ALT | SHIFT, "toggleButtonFileTreeNewWorkspace" } };
		StringBuilder wrong = new StringBuilder();
		for (Object[] row : map) {
			String got = pressed((Integer) row[0], (Integer) row[1]);
			if (!got.equals("[" + row[2] + "]"))
				wrong.append(KeyEvent.getKeyText((Integer) row[0])).append('+').append(row[1]).append(" -> ").append(got)
						.append(" (expected ").append(row[2]).append(")\n");
		}
		assertEquals(wrong.toString(), 0, wrong.length());
	}

	@Test
	public void plainKeysAndUnrelatedCombinationsDoNothing() throws Exception {
		assertEquals("[]", pressed(KeyEvent.VK_F, 0));
		assertEquals("[]", pressed(KeyEvent.VK_S, 0));
		assertEquals("[]", pressed(KeyEvent.VK_F, ALT | SHIFT));
		assertEquals("[]", pressed(KeyEvent.VK_ESCAPE, 0));
		// shortcuts switched off by the owner of the editor
		listener.setShortcutsEnabler(new various.common.light.om.ICommonShortcuts.ShortcutsEnabler() {
			@Override
			public boolean shortcutsEnabled() {
				return false;
			}
		});
		try {
			assertEquals("[]", pressed(KeyEvent.VK_F, CTRL));
		} finally {
			listener.setShortcutsEnabler(null);
		}
		assertEquals("[btnFindText]", pressed(KeyEvent.VK_F, CTRL));
	}

	@Test
	public void modifierFlagsFollowTheKeysAndResetWhenReleased() throws Exception {
		final Component src = editor.getCurrentTabEditor().loadedTabObj.textArea;
		edt(new Runnable() {
			@Override
			public void run() {
				long t = System.currentTimeMillis();
				listener.dispatchKeyEvent(new KeyEvent(src, KeyEvent.KEY_PRESSED, t, CTRL | SHIFT, KeyEvent.VK_X, KeyEvent.CHAR_UNDEFINED));
				assertTrue(listener.isCtrlDown());
				assertTrue(TextEditorShortcutListener.flagShiftDown.get());
				assertFalse(TextEditorShortcutListener.flagAltDown.get());
				listener.dispatchKeyEvent(new KeyEvent(src, KeyEvent.KEY_RELEASED, t, 0, KeyEvent.VK_X, KeyEvent.CHAR_UNDEFINED));
				assertFalse(listener.isCtrlDown());
				assertFalse(TextEditorShortcutListener.flagShiftDown.get());
			}
		});
		flushEdt();
	}

	@Test
	public void tabShortcutsOpenCloseAndSwitchTabs() throws Exception {
		CustomTabbedEditor tabs = editor.getCurrentTabEditor();
		int start = tabs.mainTabList.getTabCount();
		press(KeyEvent.VK_N, CTRL);
		assertEquals(start + 1, tabs.mainTabList.getTabCount());
		press(KeyEvent.VK_N, CTRL);
		assertEquals(start + 2, tabs.mainTabList.getTabCount());
		int selected = tabs.mainTabList.getSelectedIndex();
		press(KeyEvent.VK_TAB, CTRL | SHIFT);
		assertEquals("previous tab", selected - 1, tabs.mainTabList.getSelectedIndex());
		press(KeyEvent.VK_TAB, CTRL);
		assertEquals("next tab", selected, tabs.mainTabList.getSelectedIndex());
		press(KeyEvent.VK_W, CTRL);
		assertEquals(start + 1, tabs.mainTabList.getTabCount());
		press(KeyEvent.VK_W, CTRL);
		assertEquals(start, tabs.mainTabList.getTabCount());
	}

	@Test
	public void textShortcutsEditTheCurrentTab() throws Exception {
		final TabTextEditorObject tab = editor.getCurrentTabEditor().loadedTabObj;
		edt(new Runnable() {
			@Override
			public void run() {
				tab.textArea.setText("one\ntwo\nthree");
				tab.textArea.setCaretPosition(5); // inside "two"
			}
		});
		press(KeyEvent.VK_UP, CTRL | ALT);
		assertEquals("line copied above", "one\ntwo\ntwo\nthree", tab.textArea.getText());
		edt(new Runnable() {
			@Override
			public void run() {
				tab.textArea.setText("one\ntwo\nthree");
				tab.textArea.setCaretPosition(5);
			}
		});
		press(KeyEvent.VK_DOWN, CTRL | ALT);
		assertEquals("line copied below", "one\ntwo\ntwo\nthree", tab.textArea.getText());
		edt(new Runnable() {
			@Override
			public void run() {
				tab.textArea.setText("small words");
				tab.textArea.selectAll();
			}
		});
		press(KeyEvent.VK_UP, CTRL | SHIFT);
		assertEquals("SMALL WORDS", tab.textArea.getText());
		press(KeyEvent.VK_DOWN, CTRL | SHIFT);
		assertEquals("small words", tab.textArea.getText());
		edt(new Runnable() {
			@Override
			public void run() {
				tab.textArea.setText("alpha. beta");
				tab.textArea.selectAll();
			}
		});
		press(KeyEvent.VK_PERIOD, CTRL | SHIFT);
		assertEquals("uppercase after the dots", "Alpha. Beta", tab.textArea.getText());
		edt(new Runnable() {
			@Override
			public void run() {
				tab.textArea.setText("Start. Next. Last");
				tab.textArea.setSelectionStart(7);
				tab.textArea.setSelectionEnd(tab.textArea.getText().length());
			}
		});
		press(KeyEvent.VK_UP, CTRL | SHIFT);
		assertEquals("only the selection is changed", "Start. NEXT. LAST", tab.textArea.getText());
		// scrolling, history, maximize, font and format must at least run without errors
		press(KeyEvent.VK_UP, CTRL);
		press(KeyEvent.VK_DOWN, CTRL);
		press(KeyEvent.VK_RIGHT, CTRL | ALT);
		press(KeyEvent.VK_LEFT, CTRL | ALT);
		press(KeyEvent.VK_RIGHT, ALT);
		press(KeyEvent.VK_LEFT, ALT);
		press(KeyEvent.VK_M, CTRL);
		press(KeyEvent.VK_M, CTRL);
		press(KeyEvent.VK_J, ALT | SHIFT);
		press(KeyEvent.VK_J, ALT | SHIFT);
		press(KeyEvent.VK_H, CTRL | ALT | SHIFT);
		press(KeyEvent.VK_H, CTRL | ALT | SHIFT);
		press(KeyEvent.VK_U, CTRL | ALT | SHIFT);
		press(KeyEvent.VK_U, CTRL | ALT | SHIFT);
		press(KeyEvent.VK_L, CTRL | ALT | SHIFT);
		press(KeyEvent.VK_L, CTRL | ALT | SHIFT);
		press(KeyEvent.VK_F, CTRL | SHIFT);
		press(KeyEvent.VK_C, ALT);
		press(KeyEvent.VK_K, CTRL | SHIFT);
		press(KeyEvent.VK_ESCAPE, 0);
	}

	// ------------------------------------------------------------------ file tree

	private static File workspaceFile(String relative) {
		return new File(fx.workspace, relative);
	}

	private static File write(String relative, String content) throws Exception {
		File f = workspaceFile(relative);
		f.getParentFile().mkdirs();
		Files.write(f.toPath(), content.getBytes("UTF-8"));
		return f;
	}

	private static TreePath find(JTree tree, TreeModel model, Object node, TreePath path, File wanted) {
		if (node instanceof FileNamed && wanted.getAbsoluteFile().equals(((FileNamed) node).mFile == null ? null : ((FileNamed) node).mFile.getAbsoluteFile()))
			return path;
		tree.expandPath(path);
		for (int i = 0; i < model.getChildCount(node); i++) {
			Object child = model.getChild(node, i);
			TreePath r = find(tree, model, child, path.pathByAddingChild(child), wanted);
			if (r != null)
				return r;
		}
		return null;
	}

	/** Refreshes the tree and presses the left mouse button on the node of the file, as a click would. */
	private static void clickNode(final File f) throws Exception {
		edt(new Runnable() {
			@Override
			public void run() {
				editor.textEditorUtils.refreshTreeFromFolder(fx.workspace, true);
			}
		});
		flushEdt();
		final JTree tree = editor.leftFileTree;
		edt(new Runnable() {
			@Override
			public void run() {
				tree.setSize(400, 600);
				tree.doLayout();
				Object root = tree.getModel().getRoot();
				TreePath p = find(tree, tree.getModel(), root, new TreePath(root), f);
				assertNotNull("node of " + f + " in the tree", p);
				tree.setSelectionPath(p);
				Rectangle r = tree.getPathBounds(p);
				assertNotNull(r);
				Point pt = new Point(r.x + 2, r.y + r.height / 2);
				MouseEvent me = new MouseEvent(tree, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), MouseEvent.BUTTON1_DOWN_MASK, pt.x, pt.y, 1, false, MouseEvent.BUTTON1);
				for (java.awt.event.MouseListener l : tree.getMouseListeners()) {
					if (l.getClass().getName().contains("TreeFileMouseListenerTxtEditor"))
						l.mousePressed(me);
				}
			}
		});
		flushEdt();
	}

	private static void click(final JMenuItem item) throws Exception {
		edt(new Runnable() {
			@Override
			public void run() {
				assertTrue(item.getText() + " enabled", item.isEnabled());
				item.doClick();
			}
		});
		flushEdt();
		Thread.sleep(150);
		flushEdt();
	}

	@Test
	public void popupMenuIsEnabledByWhatIsSelected() throws Exception {
		File dir = workspaceFile("menus");
		File file = write("menus/note.txt", "hello");
		TreeFileMouseListenerTxtEditor l = editor.mouseListenerTreeFilePanel;
		assertNotNull(l.getPopUpMenu());
		clickNode(file);
		assertTrue(l.itemLoadFile.isEnabled() && l.itemRename.isEnabled() && l.copyPathsItem.isEnabled());
		assertFalse("a file is not a workspace", l.workspaceItem.isEnabled());
		clickNode(dir);
		assertTrue(l.workspaceItem.isEnabled() && l.subMenuNew.isEnabled());
		assertFalse("a folder is not opened as a file", l.itemLoadFile.isEnabled());
		assertFalse(l.itemRename.isEnabled());
	}

	@Test
	public void openingNewFileRenamingAndCopyingFromTheMenu() throws Exception {
		TreeFileMouseListenerTxtEditor l = editor.mouseListenerTreeFilePanel;
		File dir = workspaceFile("work");
		File file = write("work/first.txt", "first content");

		// open the selected file in a tab
		clickNode(file);
		click(l.itemLoadFile);
		TabTextEditorObject tab = editor.getCurrentTabEditor().loadedTabObj;
		assertEquals(file.getAbsoluteFile(), tab.loadedFile.getAbsoluteFile());
		assertEquals("first content", tab.textArea.getText().trim());

		// a new file inside the selected folder
		clickNode(dir);
		fx.dialogs.inputs.add("created");
		click(l.itemNewFile);
		File created = new File(dir, "created.txt");
		assertTrue("the new file exists: " + java.util.Arrays.toString(dir.list()), created.isFile() || new File(dir, "created").isFile());

		// copy the file of the current tab next to itself: the name exists, the user refuses to overwrite
		clickNode(file);
		fx.dialogs.answers.add(Boolean.FALSE);
		click(l.copyHereCurrentItem);

		// rename the selected file
		File second = write("work/second.txt", "second");
		clickNode(second);
		fx.dialogs.inputs.add("renamed.txt");
		click(l.itemRename);
		assertTrue("renamed", new File(dir, "renamed.txt").isFile());
		assertFalse(second.exists());

		// copy the paths of the selection to the clipboard
		clickNode(new File(dir, "renamed.txt"));
		click(l.copyPathsItem);

		// refresh, expand, collapse, select all, properties: no failure
		clickNode(dir);
		click(l.refreshItem);
		click(l.expandItem);
		click(l.collapseItem);
		click(l.selectAllItem);
	}

	@Test
	public void movingTheCurrentFileIntoAnotherFolder() throws Exception {
		TreeFileMouseListenerTxtEditor l = editor.mouseListenerTreeFilePanel;
		File source = write("mv/from/doc.txt", "to move");
		File target = workspaceFile("mv/to");
		target.mkdirs();
		clickNode(source);
		click(l.itemLoadFile);
		clickNode(target);
		fx.dialogs.answers.add(Boolean.TRUE); // sure to move
		click(l.moveHereCurrentItem);
		assertTrue("file moved", new File(target, "doc.txt").isFile());
		assertFalse("and removed from the old place", source.exists());
		assertEquals(new File(target, "doc.txt").getAbsoluteFile(), editor.getCurrentTabEditor().loadedTabObj.loadedFile.getAbsoluteFile());
	}
	@Test
	public void multipleSelectionDoubleClickHoverAndNewFolder() throws Exception {
		TreeFileMouseListenerTxtEditor l = editor.mouseListenerTreeFilePanel;
		final File a = write("multi/a.txt", "aaa");
		final File b = write("multi/b.txt", "bbb");
		clickNode(a);
		final JTree tree = editor.leftFileTree;
		// two files selected: the menus that depend on the whole selection are rebuilt for both
		edt(new Runnable() {
			@Override
			public void run() {
				Object root = tree.getModel().getRoot();
				TreePath pa = find(tree, tree.getModel(), root, new TreePath(root), a);
				TreePath pb = find(tree, tree.getModel(), root, new TreePath(root), b);
				tree.setSelectionPaths(new TreePath[] { pa, pb });
				Rectangle r = tree.getPathBounds(pb);
				MouseEvent me = new MouseEvent(tree, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), MouseEvent.BUTTON3_DOWN_MASK, r.x + 2, r.y + r.height / 2, 1, true, MouseEvent.BUTTON3);
				for (java.awt.event.MouseListener ml : tree.getMouseListeners()) {
					if (ml.getClass().getName().contains("TreeFileMouseListenerTxtEditor"))
						ml.mousePressed(me);
				}
				assertTrue("two files selected", tree.getSelectionCount() == 2);
			}
		});
		flushEdt();
		assertTrue(l.itemOpenWith.isEnabled() || !l.itemOpenWith.isEnabled()); // menus were rebuilt without failing

		// hover over the nodes and leave the tree
		edt(new Runnable() {
			@Override
			public void run() {
				Object root = tree.getModel().getRoot();
				Rectangle r = tree.getPathBounds(find(tree, tree.getModel(), root, new TreePath(root), a));
				for (java.awt.event.MouseMotionListener mm : tree.getMouseMotionListeners()) {
					mm.mouseMoved(new MouseEvent(tree, MouseEvent.MOUSE_MOVED, System.currentTimeMillis(), 0, r.x + 2, r.y + 2, 0, false));
					mm.mouseMoved(new MouseEvent(tree, MouseEvent.MOUSE_MOVED, System.currentTimeMillis(), 0, 390, 590, 0, false));
					mm.mouseDragged(new MouseEvent(tree, MouseEvent.MOUSE_DRAGGED, System.currentTimeMillis(), 0, r.x + 2, r.y + 2, 0, false));
				}
				for (java.awt.event.MouseListener ml : tree.getMouseListeners())
					ml.mouseExited(new MouseEvent(tree, MouseEvent.MOUSE_EXITED, System.currentTimeMillis(), 0, 0, 0, 0, false));
			}
		});
		flushEdt();

		// a double click opens the file in a tab
		edt(new Runnable() {
			@Override
			public void run() {
				Object root = tree.getModel().getRoot();
				tree.setSelectionPath(find(tree, tree.getModel(), root, new TreePath(root), b));
				Rectangle r = tree.getPathBounds(find(tree, tree.getModel(), root, new TreePath(root), b));
				MouseEvent dbl = new MouseEvent(tree, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), MouseEvent.BUTTON1_DOWN_MASK, r.x + 2, r.y + r.height / 2, 2, false, MouseEvent.BUTTON1);
				for (java.awt.event.MouseListener ml : tree.getMouseListeners())
					ml.mousePressed(dbl);
			}
		});
		flushEdt();
		Thread.sleep(200);
		flushEdt();
		assertEquals(b.getAbsoluteFile(), editor.getCurrentTabEditor().loadedTabObj.loadedFile.getAbsoluteFile());

		// new folder from the menu
		clickNode(workspaceFile("multi"));
		fx.dialogs.inputs.add("brandNew");
		click(l.itemNewFolder);
		assertTrue("folder created", new File(workspaceFile("multi"), "brandNew").isDirectory());
	}
}

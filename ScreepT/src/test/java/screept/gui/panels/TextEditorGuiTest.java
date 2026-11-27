/**
 *
 * =========================================================================================
 *  Copyright (C) 2019-2020
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
package screept.gui.panels;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static screept.gui.GuiTestSupport.edt;
import static screept.gui.GuiTestSupport.flushEdt;
import static screept.gui.GuiTestSupport.get;
import static screept.gui.GuiTestSupport.set;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.KeyboardFocusManager;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import javax.swing.JButton;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.ini4j.Wini;

import gui.texteditor.commons.om.TabTextEditorObject;
import gui.texteditor.commons.tabbededitor.CustomTabbedEditor;
import initializer.configs.impl.INItializer;
import maingUI.MainGuiParent;
import plugin.api.AbstractPluginApplicationApi;
import screept.gui.GuiTestSupport;
import screept.gui.GuiTestSupport.EdtErrors;
import screept.gui.GuiTestSupport.Fixture;

/**
 * The real text editor, built on a frame that is never shown with a configuration bound to a temporary ini (no session
 * of the user can reopen) and a workspace in a temporary folder. Only new empty tabs and files of a JUnit temporary
 * folder are ever opened or saved. Dialogs are scripted. Not driven (they open windows or choosers): save as, open
 * file, find / replace / go to line dialogs, font chooser.
 */
public class TextEditorGuiTest {

	@ClassRule
	public static TemporaryFolder classTmp = new TemporaryFolder();

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private static Fixture fx;
	private static TextEditor editor;
	private static EdtErrors edtErrors;

	// ------------------------------------------------------------------ plumbing

	private static CustomTabbedEditor tabs() {
		return editor.getCurrentTabEditor();
	}

	private static TabTextEditorObject tab() {
		return tabs().loadedTabObj;
	}

	private static int tabCount() {
		return tabs().mainTabList.getTabCount();
	}

	private static void click(final JButton b) throws Exception {
		edt(new Runnable() {
			@Override
			public void run() {
				b.doClick();
			}
		});
		flushEdt();
	}

	private static void append(final TabTextEditorObject t, final String s) throws Exception {
		edt(new Runnable() {
			@Override
			public void run() {
				t.textArea.append(s);
			}
		});
		flushEdt();
	}

	private static void setText(final TabTextEditorObject t, final String s) throws Exception {
		edt(new Runnable() {
			@Override
			public void run() {
				t.textArea.setText(s);
			}
		});
		flushEdt();
	}

	private static void select(final CustomTabbedEditor ed, final int index) throws Exception {
		edt(new Runnable() {
			@Override
			public void run() {
				ed.mainTabList.setSelectedIndex(index);
			}
		});
		flushEdt();
	}

	private static File newFile(File dir, String name, String content) throws Exception {
		File f = new File(dir, name);
		Files.write(f.toPath(), content.getBytes("UTF-8"));
		return f;
	}

	private static int indexOf(CustomTabbedEditor ed, File f) {
		List<TabTextEditorObject> l = ed.loadedList.getList();
		for (int i = 0; i < l.size(); i++) {
			if (l.get(i).loadedFile != null && l.get(i).loadedFile.getAbsoluteFile().equals(f.getAbsoluteFile())) {
				return i;
			}
		}
		return -1;
	}

	private static void open(File f) throws Exception {
		final File file = f;
		edt(new Runnable() {
			@Override
			public void run() {
				tabs().loadFile(file);
			}
		});
		flushEdt();
		assertTrue("opened in a tab: " + f, indexOf(tabs(), f) >= 0);
	}

	/** Text of a tab with the line breaks normalised: the editor keeps the EOL of the platform */
	private static String txt(TabTextEditorObject t) {
		return t.textArea.getText().replace("\r\n", "\n");
	}

	private static String read(File f) throws Exception {
		return new String(Files.readAllBytes(f.toPath()), "UTF-8");
	}

	private static boolean removeTab(final int index) throws Exception {
		boolean removed = edt(new Callable<Boolean>() {
			@Override
			public Boolean call() {
				return tabs().removeTab(index);
			}
		});
		flushEdt();
		return removed;
	}

	/** Back to what a fresh editor shows: one empty tab, nothing pinned, no second panel */
	private static void cleanSlate() throws Exception {
		edt(new Runnable() {
			@Override
			public void run() {
				if (editor.tabSplit.hasSplit()) {
					editor.tabSplit.onLastTabClosed(editor.tabbedEditorsList.get(1));
				}
				editor.currentTabbedEditorSelected = 0;
			}
		});
		edt(new Runnable() {
			@Override
			public void run() {
				for (CustomTabbedEditor ed : new ArrayList<CustomTabbedEditor>(editor.tabbedEditorsList)) {
					for (TabTextEditorObject t : ed.loadedList.getList()) {
						t.unsaved = false;
						t.pinned = false;
					}
				}
			}
		});
		for (int guard = 0; guard < 60 && (tabCount() > 1 || tab().loadedFile != null || !txt(tab()).isEmpty()); guard++) {
			removeTab(tabCount() - 1);
		}
		edt(new Runnable() {
			@Override
			public void run() {
				tab().unsaved = false;
			}
		});
		flushEdt();
	}

	// ------------------------------------------------------------------ lifecycle

	@BeforeClass
	public static void build() throws Exception {
		Assume.assumeFalse("needs a display server (windows are never shown)", GraphicsEnvironment.isHeadless());
		// the editor starts a cleaner of unused temp files that is harmless only while no application provider is registered
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
		set(tabs(), "dialogHelper", fx.dialogs);
	}

	@AfterClass
	public static void dispose() throws Exception {
		if (editor != null) {
			KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(editor.shortcutsListener);
		}
		if (edtErrors != null) {
			edtErrors.unplug();
		}
		if (fx != null) {
			fx.close();
		}
	}

	@Before
	public void reset() throws Exception {
		Assume.assumeNotNull(editor);
		cleanSlate();
		fx.dialogs.reset();
		edtErrors.errors.clear();
	}

	@After
	public void noWindowWasShown() {
		if (editor != null) {
			assertFalse("the test frame must stay hidden", MainGuiParent.mainFrameScreept.isVisible());
		}
	}

	// ------------------------------------------------------------------ tests

	@Test
	public void editorStartsWithOneEmptyTabAndNothingOfTheUser() throws Exception {
		assertEquals(1, tabCount());
		assertNull("a new tab has no file", tab().loadedFile);
		assertEquals("", txt(tab()));
		assertFalse(tab().unsaved);
		assertEquals(1, editor.tabbedEditorsList.size());
		assertFalse("no second panel", editor.tabSplit.hasSplit());
		assertEquals("the workspace is the temporary one", fx.workspace.getAbsolutePath(),
				fx.conf.getTextEditorOpt().defaultWorkspacePath);
		assertEquals(0, fx.dialogs.errors());
	}

	@Test
	public void newTabButtonAddsAndSelectsEmptyTabs() throws Exception {
		click(editor.subHeaderPanel.btnNewTab);
		click(editor.subHeaderPanel.btnNewTab);
		assertEquals(3, tabCount());
		assertEquals("the new tab is in front", 2, tabs().mainTabList.getSelectedIndex());
		assertSame(tabs().loadedList.getList().get(2), tab());
		assertNotSame(tabs().loadedList.getList().get(0).textArea, tab().textArea);
		assertEquals("", txt(tab()));
		assertNull(tab().loadedFile);
	}

	@Test
	public void typingMarksTheTabUnsavedAndUndoRedoWalkTheText() throws Exception {
		File f = newFile(tmp.getRoot(), "note.txt", "one\n");
		open(f);
		TabTextEditorObject t = tab();
		assertFalse("just loaded: saved", t.unsaved);
		assertEquals("one\n", txt(t));
		append(t, "two");
		assertTrue("edited: unsaved", t.unsaved);
		assertEquals("one\ntwo", txt(t));
		click(editor.subHeaderPanel.btnUndo);
		assertEquals("one\n", txt(t));
		assertFalse("back to the content of the file: saved again", t.unsaved);
		click(editor.subHeaderPanel.btnRedo);
		assertEquals("one\ntwo", txt(t));
		assertTrue(t.unsaved);
		assertEquals("the file was never touched", "one\n", read(f));
	}

	@Test
	public void saveButtonWritesTheFileAndClearsTheUnsavedState() throws Exception {
		File f = newFile(tmp.getRoot(), "save.txt", "alpha\n");
		open(f);
		append(tab(), "beta");
		assertTrue(tab().unsaved);
		click(editor.subHeaderPanel.btnSaveTextFile);
		assertFalse(tab().unsaved);
		String onDisk = read(f);
		assertEquals("alpha", onDisk.split("\r?\n")[0]);
		assertEquals("beta", onDisk.split("\r?\n")[1]);
		assertFalse("one kind of line break only", onDisk.contains("\r\n") && onDisk.replace("\r\n", "").contains("\n"));
		assertEquals(0, fx.dialogs.errors());
	}

	@Test
	public void reloadFromDiskShowsWhatAnotherProgramWrote() throws Exception {
		File f = newFile(tmp.getRoot(), "reload.txt", "first\n");
		open(f);
		append(tab(), " local edit");
		Files.write(f.toPath(), "changed outside\n".getBytes("UTF-8"));
		fx.dialogs.answers.add(Boolean.TRUE); // "reload?"
		edt(new Runnable() {
			@Override
			public void run() {
				tabs().reloadCurrentTab(true);
			}
		});
		flushEdt();
		assertEquals("changed outside\n", txt(tab()));
		assertFalse(tab().unsaved);
		// declining keeps the local text
		append(tab(), "!");
		fx.dialogs.answers.add(Boolean.FALSE);
		edt(new Runnable() {
			@Override
			public void run() {
				tabs().reloadCurrentTab(true);
			}
		});
		assertEquals("changed outside\n!", txt(tab()));
	}

	@Test
	public void reloadOfATabWithoutFileIsReportedNotSilentlyIgnored() throws Exception {
		edt(new Runnable() {
			@Override
			public void run() {
				tabs().reloadCurrentTab(false);
			}
		});
		assertEquals(1, fx.dialogs.errors());
		assertEquals("", txt(tab()));
	}

	@Test
	public void closingAnUnsavedTabAsksAndHonoursTheChoice() throws Exception {
		File f = newFile(tmp.getRoot(), "close.txt", "keep\n");
		open(f);
		int index = indexOf(tabs(), f);
		append(tab(), "+ edit");
		assertTrue(tab().unsaved);
		int before = tabCount();

		fx.dialogs.answers.add(null); // cancel
		assertFalse("cancel keeps the tab", removeTab(index));
		assertEquals(before, tabCount());
		assertEquals("keep\n+ edit", txt(tab()));

		fx.dialogs.answers.add(Boolean.FALSE); // discard
		assertTrue(removeTab(index));
		assertEquals(before - 1, tabCount());
		assertEquals("the file is untouched", "keep\n", read(f));

		open(f);
		index = indexOf(tabs(), f);
		append(tab(), "+ saved edit");
		fx.dialogs.answers.add(Boolean.TRUE); // save
		assertTrue(removeTab(index));
		assertEquals(Arrays.asList("keep", "+ saved edit"), Arrays.asList(read(f).split("\r?\n")));
		assertEquals("no leftover questions", 0, fx.dialogs.answers.size());
	}

	@Test
	public void closingASavedTabNeverAsks() throws Exception {
		File f = newFile(tmp.getRoot(), "plain.txt", "x");
		open(f);
		assertTrue(removeTab(indexOf(tabs(), f)));
		assertEquals(0, fx.dialogs.questions.size());
	}

	@Test
	public void closingTheLastTabLeavesAnEmptyTab() throws Exception {
		File f = newFile(tmp.getRoot(), "last.txt", "x");
		open(f);
		// leave only this tab
		while (tabCount() > 1) {
			int other = indexOf(tabs(), f) == 0 ? 1 : 0;
			assertTrue(removeTab(other));
		}
		assertTrue(removeTab(0));
		assertEquals(1, tabCount());
		assertNull(tab().loadedFile);
		assertEquals("", txt(tab()));
		assertFalse(tab().unsaved);
		assertEquals("the file itself is untouched", "x", read(f));
	}

	@Test
	public void pinnedTabsGoFirstAndStayAmongThemselves() throws Exception {
		File a = newFile(tmp.getRoot(), "a.txt", "a");
		File b = newFile(tmp.getRoot(), "b.txt", "b");
		File c = newFile(tmp.getRoot(), "c.txt", "c");
		open(a);
		open(b);
		open(c);
		final CustomTabbedEditor ed = tabs();
		int emptyTabs = tabCount() - 3;
		assertEquals(0, ed.getPinnedCount());

		edt(new Runnable() {
			@Override
			public void run() {
				ed.pinTab(ed.loadedList.getSize() - 1);
			}
		});
		assertEquals(1, ed.getPinnedCount());
		assertEquals("the pinned tab moved to the front", 0, indexOf(ed, c));
		assertTrue(ed.isPinned(0));
		assertEquals("the others did not lose their tab", emptyTabs + 3, tabCount());

		edt(new Runnable() {
			@Override
			public void run() {
				ed.pinTab(indexOf(ed, a));
			}
		});
		assertEquals("a pinned after c", Arrays.asList(c, a), pinnedFiles(ed));

		// an unpinned tab cannot be dragged in front of the pinned ones
		final int bIndex = indexOf(ed, b);
		edt(new Runnable() {
			@Override
			public void run() {
				ed.moveTab(bIndex, 0);
			}
		});
		assertEquals(2, indexOf(ed, b));
		// and a pinned one cannot leave the pinned area
		edt(new Runnable() {
			@Override
			public void run() {
				ed.moveTab(0, ed.loadedList.getSize() - 1);
			}
		});
		assertTrue(ed.isPinned(indexOf(ed, c)));
		assertTrue(indexOf(ed, c) <= 1);

		// the pinned files are remembered in the configuration
		String saved = new Wini(fx.iniFile).get(INItializer.TEXT_EDITOR_OPT, "pinnedFilesAtStart");
		assertTrue(saved.contains(a.getAbsolutePath()));
		assertTrue(saved.contains(c.getAbsolutePath()));
		assertFalse(saved.contains(b.getAbsolutePath()));

		edt(new Runnable() {
			@Override
			public void run() {
				ed.togglePin(indexOf(ed, c));
			}
		});
		assertEquals(Arrays.asList(a), pinnedFiles(ed));
		assertFalse(new Wini(fx.iniFile).get(INItializer.TEXT_EDITOR_OPT, "pinnedFilesAtStart").contains(c.getAbsolutePath()));
		// out of bounds requests are ignored
		edt(new Runnable() {
			@Override
			public void run() {
				ed.pinTab(-1);
				ed.pinTab(999);
				ed.unpinTab(999);
			}
		});
		assertEquals(1, ed.getPinnedCount());
	}

	private static List<File> pinnedFiles(CustomTabbedEditor ed) {
		List<File> l = new ArrayList<File>();
		for (TabTextEditorObject t : ed.loadedList.getList()) {
			if (t.pinned) {
				l.add(t.loadedFile.getAbsoluteFile());
			}
		}
		return l;
	}

	@Test
	public void secondPanelTabsFollowFontAndTheme() throws Exception {
		File f = newFile(tmp.getRoot(), "second.txt", "in the second panel\n");
		final File g = newFile(tmp.getRoot(), "second2.txt", "also");
		edt(new Runnable() {
			@Override
			public void run() {
				editor.tabSplit.restoreSecondPanel(Arrays.asList(f.getAbsolutePath(), g.getAbsolutePath()));
			}
		});
		flushEdt();
		assertTrue("second panel open", editor.tabSplit.hasSplit());
		assertEquals(2, editor.tabbedEditorsList.size());
		CustomTabbedEditor second = editor.tabbedEditorsList.get(1);
		assertEquals(2, second.mainTabList.getTabCount());
		// the tabs behind the front one load when they are first shown (lazy loading): show the first one
		select(second, 0);
		for (int i = 0; i < 100 && txt(second.loadedList.getList().get(0)).isEmpty(); i++) {
			Thread.sleep(50);
			flushEdt();
		}
		assertEquals("in the second panel\n", txt(second.loadedList.getList().get(0)));

		Font font = new Font("Monospaced", Font.PLAIN, 23);
		edt(new Runnable() {
			@Override
			public void run() {
				tab().textArea.setFont(new Font("Monospaced", Font.PLAIN, 23));
				editor.applyFontToOtherPanels(tab().textArea.getFont());
			}
		});
		for (TabTextEditorObject t : second.loadedList.getList()) {
			assertEquals("every tab of the other panel follows the font", font, t.textArea.getFont());
		}

		// the saved state of the panel's tabs survives a theme change
		final TabTextEditorObject s0 = second.loadedList.getList().get(0);
		edt(new Runnable() {
			@Override
			public void run() {
				editor.textEditorUtils.applySelectedTheme(true);
			}
		});
		flushEdt();
		assertFalse("applying a theme is not an edit", s0.unsaved);
		assertEquals(0, edtErrors.errors.size());
	}

	@Test
	public void renameMovesTheFileAndRefusesNonsense() throws Exception {
		File f = newFile(tmp.getRoot(), "old.txt", "content");
		assertTrue(editor.textEditorUtils.renameFile(f, "new.txt", false));
		flushEdt();
		assertFalse(f.exists());
		assertEquals("content", read(new File(tmp.getRoot(), "new.txt")));
		assertEquals("renaming a file that is not open must not fail later in the tree refresh: " + edtErrors.errors, 0, edtErrors.errors.size());

		File moved = new File(tmp.getRoot(), "new.txt");
		assertFalse("same name: nothing to do", editor.textEditorUtils.renameFile(moved, "new.txt", false));
		assertFalse("empty name", editor.textEditorUtils.renameFile(moved, "  ", false));
		assertTrue(moved.exists());
		assertFalse("missing source is reported", editor.textEditorUtils.renameFile(new File(tmp.getRoot(), "ghost.txt"), "x.txt", false));
		assertEquals(1, fx.dialogs.errors());
	}

	@Test
	public void renameOntoAnExistingFileKeepsBothUnlessOverwriteIsChosen() throws Exception {
		File a = newFile(tmp.getRoot(), "a.txt", "AAA");
		File b = newFile(tmp.getRoot(), "b.txt", "BBB");
		fx.dialogs.answers.add(Boolean.FALSE); // do not overwrite
		assertTrue(editor.textEditorUtils.renameFile(a, "b.txt", false));
		flushEdt();
		assertEquals("the existing file is intact", "BBB", read(b));
		assertFalse(a.exists());
		String[] names = tmp.getRoot().list();
		Arrays.sort(names);
		assertEquals("the renamed file got a unique name", 2, names.length);

		File c = newFile(tmp.getRoot(), "c.txt", "CCC");
		fx.dialogs.answers.add(Boolean.TRUE); // overwrite
		assertTrue(editor.textEditorUtils.renameFile(c, "b.txt", false));
		flushEdt();
		assertEquals("CCC", read(b));
		assertFalse(c.exists());
	}

	@Test
	public void renameOfAnOpenUnsavedFileAsksFirst() throws Exception {
		File f = newFile(tmp.getRoot(), "open.txt", "v1\n");
		open(f);
		append(tab(), "v2");
		fx.dialogs.answers.add(null); // cancel
		assertFalse(editor.textEditorUtils.renameFile(f, "renamed.txt", false));
		assertTrue(f.exists());
		fx.dialogs.answers.add(Boolean.TRUE); // save, then rename
		assertTrue(editor.textEditorUtils.renameFile(f, "renamed.txt", false));
		flushEdt();
		File renamed = new File(tmp.getRoot(), "renamed.txt");
		assertEquals(Arrays.asList("v1", "v2"), Arrays.asList(read(renamed).split("\r?\n")));
		assertEquals("the open tab follows the file", renamed.getAbsoluteFile(), tab().loadedFile.getAbsoluteFile());
	}

	@Test
	public void extensionIsAddedOnlyWhenMissingOrInvalid() throws Exception {
		String ext = editor.CURRENT_EXT_OUT;
		assertEquals("plain" + ext, editor.textEditorUtils.adaptFileExt(new File(tmp.getRoot(), "plain")).getName());
		assertEquals("notes.md", editor.textEditorUtils.adaptFileExt(new File(tmp.getRoot(), "notes.md")).getName());
		assertEquals("an absurdly long extension is not one", "archive" + ext,
				editor.textEditorUtils.adaptFileExt(new File(tmp.getRoot(), "archive.extremelylong")).getName());
	}

	@Test
	public void footerDirectoryLabelIsShortenedToItsLimit() throws Exception {
		int limit = resources.GeneralConfig.FOOTER_MAX_DIR_LABEL_LENGTH;
		assertTrue(limit >= 10);
		String atLimit = new String(new char[limit]).replace(' ', 'x');
		assertEquals("exactly at the limit: unchanged", atLimit, editor.textEditorUtils.getFooterPanelAdaptedDirString(atLimit));
		StringBuilder longPath = new StringBuilder();
		for (int i = 0; i < limit * 2; i++) {
			longPath.append((char) ('a' + i % 26));
		}
		String shown = editor.textEditorUtils.getFooterPanelAdaptedDirString(longPath.toString());
		assertEquals("never longer than the limit: " + shown, limit, shown.length());
		assertTrue("keeps the beginning", shown.startsWith(longPath.substring(0, 5)));
		assertTrue("and the end, where the folder name is", shown.endsWith(longPath.substring(longPath.length() - 8)));
		assertTrue(shown.contains(".."));
	}
}

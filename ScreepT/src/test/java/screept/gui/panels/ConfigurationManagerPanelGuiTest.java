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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static screept.gui.GuiTestSupport.edt;
import static screept.gui.GuiTestSupport.flushEdt;
import static screept.gui.GuiTestSupport.get;
import static screept.gui.GuiTestSupport.set;

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Files;
import java.util.Calendar;
import java.util.concurrent.Callable;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.ini4j.Wini;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import initializer.configs.impl.INItializer;
import maingUI.MainGuiParent;
import resources.GeneralConfig;
import screept.gui.GuiTestSupport;
import screept.gui.GuiTestSupport.Fixture;
import screept.gui.utils.filedialog.INIfileChoosER;
import screept.gui.utils.tree.iniconfmanager.ConfigPool;
import screept.gui.utils.tree.iniconfmanager.TreeFileMouseListenerIni;
import various.common.light.i18n.I18n;

/**
 * The real Config Manager panel, driven through its buttons and its tree on a frame that is never shown. Every ini it
 * reads or writes is a temporary one (the fixture redirects the default/backup/folder paths too) and every dialog is
 * scripted. Buttons that open a file chooser (load custom, export, save with its own dialog) are not driven: their
 * engine (INIfileChoosER) is tested directly instead.
 */
public class ConfigurationManagerPanelGuiTest {

	@ClassRule
	public static TemporaryFolder classTmp = new TemporaryFolder();

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private static Fixture fx;
	private static ConfigurationManagerPanel panel;
	private static File seedIni;
	private static File poolFolder;
	private static Object oldStaticDialogs;

	// ------------------------------------------------------------------ plumbing

	private static void click(final JButton b) throws Exception {
		edt(new Runnable() {
			@Override
			public void run() {
				b.doClick();
			}
		});
		flushEdt();
	}

	private static JButton button(String field) throws Exception {
		return (JButton) get(panel, field);
	}

	private static JTree tree() throws Exception {
		return (JTree) get(panel, "tree");
	}

	private static boolean flagOnDisk(File ini) throws Exception {
		return Boolean.parseBoolean(new Wini(ini).get(INItializer.AES_OPT, "flagCodificaAes"));
	}

	private static void writeIni(File target, boolean aesFlag) throws Exception {
		Files.copy(seedIni.toPath(), target.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
		Wini w = new Wini(target);
		w.put(INItializer.AES_OPT, "flagCodificaAes", aesFlag);
		w.store();
	}

	private static String withoutWindowPosition(String dump) {
		return dump.replaceAll("LAST POS [XY]: [^\r\n]*", "");
	}

	private static String text() throws Exception {
		flushEdt();
		return panel.textArea.getText();
	}

	private static JMenuItem popupItem(String i18nKey) throws Exception {
		JPopupMenu menu = tree().getComponentPopupMenu();
		String label = I18n.t(i18nKey);
		for (java.awt.Component c : menu.getComponents()) {
			if (c instanceof JMenuItem && label.equals(((JMenuItem) c).getText())) {
				return (JMenuItem) c;
			}
		}
		throw new AssertionError("no popup item " + i18nKey);
	}

	private static void fire(final JMenuItem item) throws Exception {
		edt(new Runnable() {
			@Override
			public void run() {
				for (ActionListener l : item.getActionListeners()) {
					l.actionPerformed(new ActionEvent(item, ActionEvent.ACTION_PERFORMED, ""));
				}
			}
		});
		flushEdt();
	}

	private static Object root() throws Exception {
		return tree().getModel().getRoot();
	}

	/** The node of a file of the pool folder, as the tree model lists it */
	private static TreePath poolPath(File file) throws Exception {
		TreeModel model = tree().getModel();
		Object folder = model.getChild(root(), 1);
		for (int i = 0; i < model.getChildCount(folder); i++) {
			Object n = model.getChild(folder, i);
			if (((File) n).getName().equals(file.getName())) {
				return new TreePath(new Object[] { root(), folder, n });
			}
		}
		throw new AssertionError("not in the tree: " + file);
	}

	private static void select(final TreePath path) throws Exception {
		edt(new Runnable() {
			@Override
			public void run() {
				try {
					tree().setSelectionPath(path);
				} catch (Exception e) {
					throw new AssertionError(e);
				}
			}
		});
	}

	private static void deselect() throws Exception {
		edt(new Runnable() {
			@Override
			public void run() {
				try {
					tree().clearSelection();
				} catch (Exception e) {
					throw new AssertionError(e);
				}
			}
		});
	}

	// ------------------------------------------------------------------ lifecycle

	@BeforeClass
	public static void build() throws Exception {
		Assume.assumeFalse("needs a display server (windows are never shown)", GraphicsEnvironment.isHeadless());
		fx = GuiTestSupport.open(classTmp.newFolder("cfg"));
		seedIni = new File(GeneralConfig.BACKUP_INI);
		poolFolder = new File(GeneralConfig.DEFAULT_INI_FOLDER);
		oldStaticDialogs = TreeFileMouseListenerIni.dialogHelper;
		panel = edt(new Callable<ConfigurationManagerPanel>() {
			@Override
			public ConfigurationManagerPanel call() throws Exception {
				ConfigurationManagerPanel p = new ConfigurationManagerPanel(fx.conf, fx.app);
				set(p, "dialogHelper", fx.dialogs);
				return p;
			}
		});
		// the tree menu reports through a static helper that its constructor made: it must not open a dialog either
		TreeFileMouseListenerIni.dialogHelper = fx.dialogs;
		flushEdt();
	}

	@AfterClass
	public static void dispose() throws Exception {
		if (fx != null) {
			TreeFileMouseListenerIni.dialogHelper = (gui.commons.dialogutils.JOptionHelperExtended) oldStaticDialogs;
			fx.close();
		}
	}

	@Before
	public void reset() throws Exception {
		Assume.assumeNotNull(panel);
		// back to the pristine state: temp ini = seed, memory loaded from it, no pool files, nothing unsaved
		Files.copy(seedIni.toPath(), fx.iniFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
		assertTrue(INIfileChoosER.loadIniIntoTarget(fx.conf, fx.iniFile));
		File[] pool = poolFolder.listFiles();
		if (pool != null) {
			for (File f : pool) {
				f.delete();
			}
		}
		new File(GeneralConfig.DEFAULT_INI).delete();
		edt(new Runnable() {
			@Override
			public void run() {
				panel.markSaved();
				panel.menuMouseListener.refreshTree();
			}
		});
		fx.dialogs.reset();
	}

	@After
	public void noWindowWasShown() {
		if (panel != null) {
			assertFalse("the test frame must stay hidden", MainGuiParent.mainFrameScreept.isVisible());
		}
	}

	// ------------------------------------------------------------------ tests

	@Test
	public void panelShowsTheConfigurationInUseAndItsFile() throws Exception {
		assertEquals(fx.iniFile.getPath(), ((JLabel) get(panel, "config_PATH")).getText());
		assertFalse("a configuration just loaded has no unsaved changes", panel.hasUnsavedChanges());
		String shown = text();
		assertTrue("the ini text is on screen", shown.contains("[AES_OPTIONS]"));
		assertFalse("the view is read only", panel.textArea.isEditable());
		// the configuration in use comes first, even when it lives outside the folder of the saved ones
		TreeModel model = tree().getModel();
		Object first = model.getChild(root(), 0);
		assertTrue(first instanceof ConfigPool.CurrentConfigFile);
		assertEquals(fx.iniFile.getAbsolutePath(), ((File) first).getPath());
		assertEquals(poolFolder.getAbsolutePath(), ((File) model.getChild(root(), 1)).getAbsolutePath());
		assertEquals(2, model.getChildCount(root()));
	}

	@Test
	public void unsavedChangesAreTheDifferenceFromTheLastSavedState() throws Exception {
		assertEquals("the snapshot is stable (no timestamps inside)", fx.conf.toStringa(), fx.conf.toStringa());
		boolean before = fx.conf.getAesOpt().isFlagAes();
		fx.conf.getAesOpt().setFlagAes(!before);
		assertTrue(panel.hasUnsavedChanges());
		fx.conf.getAesOpt().setFlagAes(before);
		assertFalse("undoing the change makes it clean again", panel.hasUnsavedChanges());
		fx.conf.getAesOpt().setFlagAes(!before);
		panel.markSaved();
		assertFalse("marking it saved takes the new state as reference", panel.hasUnsavedChanges());
	}

	@Test
	public void leavingWithoutChangesNeverAsks() throws Exception {
		assertTrue(panel.confirmLoadingOtherConfig());
		assertEquals(0, fx.dialogs.questions.size());
	}

	@Test
	public void unsavedChangesChoiceYesSavesThenProceeds() throws Exception {
		assertTrue(flagOnDisk(fx.iniFile));
		fx.conf.getAesOpt().setFlagAes(false);
		fx.dialogs.answers.add(Boolean.TRUE);
		assertTrue(panel.confirmLoadingOtherConfig());
		assertFalse("written to the file in use", flagOnDisk(fx.iniFile));
		assertFalse("and now clean", panel.hasUnsavedChanges());
		assertEquals(1, fx.dialogs.questions.size());
	}

	@Test
	public void unsavedChangesChoiceNoProceedsWithoutSaving() throws Exception {
		fx.conf.getAesOpt().setFlagAes(false);
		fx.dialogs.answers.add(Boolean.FALSE);
		assertTrue("discarding lets the caller load the other configuration", panel.confirmLoadingOtherConfig());
		assertTrue("the file is untouched", flagOnDisk(fx.iniFile));
		assertTrue("nothing was saved", panel.hasUnsavedChanges());
	}

	@Test
	public void unsavedChangesChoiceCancelStopsEverything() throws Exception {
		fx.conf.getAesOpt().setFlagAes(false);
		fx.dialogs.answers.add(null);
		assertFalse(panel.confirmLoadingOtherConfig());
		assertTrue(flagOnDisk(fx.iniFile));
		assertTrue(panel.hasUnsavedChanges());
	}

	@Test
	public void refreshButtonShowsVolatileConfigAndTimestampWhenClean() throws Exception {
		JLabel status = (JLabel) get(panel, "lblAvviso");
		click(button("btnCurrentRefresh"));
		assertEquals(0, fx.dialogs.questions.size());
		assertEquals(I18n.t("cfg.status_refreshed"), status.getText());
		String shown = text();
		assertTrue("the memory configuration is listed", shown.contains(fx.conf.toStringa().substring(0, 20)));
		assertTrue("with a time header", shown.contains("####"));
		assertEquals(fx.iniFile.getPath(), ((JLabel) get(panel, "config_PATH")).getText());
	}

	@Test
	public void refreshButtonCancelledByUserChangesNothing() throws Exception {
		JLabel status = (JLabel) get(panel, "lblAvviso");
		String statusBefore = status.getText();
		String textBefore = text();
		fx.conf.getAesOpt().setFlagAes(false);
		fx.dialogs.answers.add(null);
		click(button("btnCurrentRefresh"));
		assertEquals(statusBefore, status.getText());
		assertEquals(textBefore, text());
		assertTrue("still unsaved", panel.hasUnsavedChanges());
		assertTrue(flagOnDisk(fx.iniFile));
	}

	@Test
	public void refreshButtonCanSaveFirst() throws Exception {
		fx.conf.getAesOpt().setFlagAes(false);
		fx.dialogs.answers.add(Boolean.TRUE);
		click(button("btnCurrentRefresh"));
		assertFalse(flagOnDisk(fx.iniFile));
		assertFalse(panel.hasUnsavedChanges());
	}

	@Test
	public void viewCurrentFileShowsTheFileOnDisk() throws Exception {
		click(button("btnViewCurrentFile"));
		String shown = text();
		assertTrue(shown.contains("[AES_OPTIONS]"));
		assertEquals(I18n.t("cfg.status_current_file"), ((JLabel) get(panel, "lblAvviso")).getText());
	}

	@Test
	public void timestampOfTheRefreshViewUses24HourClock() throws Exception {
		Calendar c = Calendar.getInstance();
		c.set(2026, Calendar.NOVEMBER, 22, 15, 30, 45);
		String formatted = ConfigurationManagerPanel.sdf.format(c.getTime());
		assertTrue("15:30 must not be shown as 03:30: " + formatted, formatted.endsWith("_15:30:45"));
	}

	@Test
	public void loadSelectedConfigurationReplacesTheOneInUse() throws Exception {
		File other = new File(poolFolder, "other.ini");
		writeIni(other, false);
		edt(new Runnable() {
			@Override
			public void run() {
				panel.menuMouseListener.refreshTree();
			}
		});
		select(poolPath(other));
		fire(popupItem("app.load_selected_configuration"));
		assertEquals(other.getAbsoluteFile(), fx.conf.getIniFile().getFile().getAbsoluteFile());
		assertFalse("the memory follows the loaded file", fx.conf.getAesOpt().isFlagAes());
		assertEquals(other.getPath(), ((JLabel) get(panel, "config_PATH")).getText());
		assertEquals(other.getAbsolutePath(), ((File) tree().getModel().getChild(root(), 0)).getPath());
		assertFalse(panel.hasUnsavedChanges());
		assertTrue(text().contains("NEW INI CONFIGURATION LOADED"));
	}

	@Test
	public void loadSelectedConfigurationIgnoresWhatIsNotAnIni() throws Exception {
		File notIni = new File(poolFolder, "notes.txt");
		Files.write(notIni.toPath(), "hello".getBytes("UTF-8"));
		edt(new Runnable() {
			@Override
			public void run() {
				panel.menuMouseListener.refreshTree();
			}
		});
		select(poolPath(notIni));
		fire(popupItem("app.load_selected_configuration"));
		assertEquals(fx.iniFile.getAbsoluteFile(), fx.conf.getIniFile().getFile().getAbsoluteFile());
		deselect();
		fire(popupItem("app.load_selected_configuration")); // nothing selected: silent
		assertEquals(fx.iniFile.getAbsoluteFile(), fx.conf.getIniFile().getFile().getAbsoluteFile());
		assertEquals(0, fx.dialogs.errors());
	}

	@Test
	public void loadSelectedConfigurationStopsWhenUserCancelsTheSavePrompt() throws Exception {
		File other = new File(poolFolder, "other.ini");
		writeIni(other, false);
		edt(new Runnable() {
			@Override
			public void run() {
				panel.menuMouseListener.refreshTree();
			}
		});
		fx.conf.getAesOpt().setFlagAes(true);
		fx.conf.getAesOpt().setHashKey(!fx.conf.getAesOpt().isHashKey());
		select(poolPath(other));
		fx.dialogs.answers.add(null);
		fire(popupItem("app.load_selected_configuration"));
		assertEquals("cancelled: the configuration in use stays", fx.iniFile.getAbsoluteFile(), fx.conf.getIniFile().getFile().getAbsoluteFile());
		assertTrue(panel.hasUnsavedChanges());
	}

	@Test
	public void theConfigurationInUseCannotBeDeletedFromTheTree() throws Exception {
		TreePath current = new TreePath(new Object[] { root(), tree().getModel().getChild(root(), 0) });
		select(current);
		fire(popupItem("common.delete_selected_configuration_files"));
		assertTrue("the file in use is still there", fx.iniFile.isFile());
		assertEquals("the user is told why", 1, fx.dialogs.infos());
		assertEquals(0, fx.dialogs.questions.size());
	}

	@Test
	public void previewShowsTheChosenFileWithoutLoadingIt() throws Exception {
		File other = new File(poolFolder, "other.ini");
		writeIni(other, false);
		edt(new Runnable() {
			@Override
			public void run() {
				panel.menuMouseListener.refreshTree();
			}
		});
		set(panel.menuMouseListener, "selectedNode", other);
		fire(popupItem("app.preview_selected_configuration"));
		String shown = text();
		assertTrue(shown.contains(other.getAbsolutePath()));
		assertTrue("the file content follows", shown.contains("flagCodificaAes"));
		assertEquals("only a preview", fx.iniFile.getAbsoluteFile(), fx.conf.getIniFile().getFile().getAbsoluteFile());
	}

	@Test
	public void previewWithNothingSelectedDoesNothingInsteadOfFailing() throws Exception {
		// a right click on the empty area of the tree leaves no selected node, the menu item must then be harmless
		set(panel.menuMouseListener, "selectedNode", null);
		String before = text();
		fire(popupItem("app.preview_selected_configuration"));
		assertEquals(before, text());
	}

	@Test
	public void resetToOriginalReloadsTheBackupConfiguration() throws Exception {
		fx.conf.getAesOpt().setFlagAes(false);
		fx.conf.writeCurrentConfigIniFile();
		assertFalse(flagOnDisk(fx.iniFile));
		fx.dialogs.answers.add(Boolean.TRUE);
		click(button("btnResetOriginalConfig"));
		File def = new File(GeneralConfig.DEFAULT_INI);
		assertTrue("the backup was restored as default ini", def.isFile());
		assertEquals(def.getAbsoluteFile(), fx.conf.getIniFile().getFile().getAbsoluteFile());
		assertTrue("memory reloaded from the original values", fx.conf.getAesOpt().isFlagAes());
		assertEquals(def.getPath(), ((JLabel) get(panel, "config_PATH")).getText());
		assertFalse(panel.hasUnsavedChanges());
		assertEquals(I18n.t("cfg.status_reset"), ((JLabel) get(panel, "lblAvviso")).getText());
		assertEquals("one confirmation question", 1, fx.dialogs.questions.size());
		assertEquals(1, fx.dialogs.infos());
	}

	@Test
	public void resetToOriginalDeclinedChangesNothing() throws Exception {
		fx.conf.getAesOpt().setFlagAes(false);
		fx.dialogs.answers.add(Boolean.FALSE);
		click(button("btnResetOriginalConfig"));
		assertFalse("no default ini created", new File(GeneralConfig.DEFAULT_INI).exists());
		assertEquals(fx.iniFile.getAbsoluteFile(), fx.conf.getIniFile().getFile().getAbsoluteFile());
		assertFalse("memory kept", fx.conf.getAesOpt().isFlagAes());
		assertEquals(1, fx.dialogs.warnings());
	}

	@Test
	public void saveAsDefaultWritesTheDefaultIniAndKeepsTheOneInUse() throws Exception {
		File def = new File(GeneralConfig.DEFAULT_INI);
		Files.copy(seedIni.toPath(), def.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
		assertTrue(flagOnDisk(def));
		fx.conf.getAesOpt().setFlagAes(false);
		fx.dialogs.answers.add(Boolean.TRUE);
		click(button("btnSaveCurrentAsDefault"));
		assertFalse("memory written to the default ini", flagOnDisk(def));
		assertTrue("the file in use is not overwritten", flagOnDisk(fx.iniFile));
		assertEquals("and stays the file in use", fx.iniFile.getAbsoluteFile(), fx.conf.getIniFile().getFile().getAbsoluteFile());
		assertEquals("the label tells which file is in use", fx.conf.getIniFile().getFile().getPath(),
				((JLabel) get(panel, "config_PATH")).getText());
		// the reader that stays behind must read like the application does: Windows paths keep their backslashes
		String raw = new Wini(fx.iniFile).get(INItializer.TEXT_EDITOR_OPT, "defaultWorspaceLocation");
		assertEquals(raw, fx.conf.getIniReadFile().get(INItializer.TEXT_EDITOR_OPT, "defaultWorspaceLocation"));
	}

	@Test
	public void saveAsDefaultDeclinedWritesNothing() throws Exception {
		File def = new File(GeneralConfig.DEFAULT_INI);
		Files.copy(seedIni.toPath(), def.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
		fx.conf.getAesOpt().setFlagAes(false);
		fx.dialogs.answers.add(Boolean.FALSE);
		click(button("btnSaveCurrentAsDefault"));
		assertTrue(flagOnDisk(def));
		assertTrue(flagOnDisk(fx.iniFile));
		assertEquals(fx.iniFile.getAbsoluteFile(), fx.conf.getIniFile().getFile().getAbsoluteFile());
	}

	// ------------------------------------------------------------------ engine (INIfileChoosER, INItializer)

	@Test
	public void loadIniIntoTargetLoadsOrLeavesTheTargetAlone() throws Exception {
		File other = tmp.newFile("other.ini");
		writeIni(other, false);
		INItializer target = new INItializer();
		assertTrue(INIfileChoosER.loadIniIntoTarget(target, other));
		assertEquals(other.getAbsoluteFile(), target.getIniFile().getFile().getAbsoluteFile());
		assertFalse(target.getAesOpt().isFlagAes());
		File missing = new File(tmp.getRoot(), "missing.ini");
		assertFalse("a missing file is refused", INIfileChoosER.loadIniIntoTarget(target, missing));
		assertEquals("and the previous one stays", other.getAbsoluteFile(), target.getIniFile().getFile().getAbsoluteFile());
	}

	@Test
	public void loadIniAsDefaultAndCopyReplaceTheCurrentConfiguration() throws Exception {
		File other = tmp.newFile("other.ini");
		writeIni(other, false);
		assertTrue(INIfileChoosER.loadIniAsDefault(other));
		assertEquals(other.getAbsoluteFile(), fx.conf.getIniFile().getFile().getAbsoluteFile());
		assertFalse(fx.conf.getAesOpt().isFlagAes());
		assertFalse(INIfileChoosER.loadIniAsDefault(new File(tmp.getRoot(), "missing.ini")));

		// copy: the content of the file in use is replaced, the file in use stays the same
		INIfileChoosER.loadIniIntoTarget(fx.conf, fx.iniFile);
		assertTrue(flagOnDisk(fx.iniFile));
		assertTrue(INIfileChoosER.copyNloadIniAsDefault(other));
		assertFalse("content copied over the file in use", flagOnDisk(fx.iniFile));
		assertEquals(fx.iniFile.getAbsoluteFile(), fx.conf.getIniFile().getFile().getAbsoluteFile());
		assertFalse(fx.conf.getAesOpt().isFlagAes());
		assertFalse("a missing source is refused", INIfileChoosER.copyNloadIniAsDefault(new File(tmp.getRoot(), "missing.ini")));
	}

	@Test
	public void writtenConfigurationReadsBackIdentically() throws Exception {
		fx.conf.getAesOpt().setFlagAes(false);
		fx.conf.getAesOpt().setHashKey(!fx.conf.getAesOpt().isHashKey());
		fx.conf.getTextEditorOpt().setTabSize(7);
		fx.conf.writeCurrentConfigIniFile();
		INItializer reread = new INItializer();
		assertTrue(INIfileChoosER.loadIniIntoTarget(reread, fx.iniFile));
		assertEquals(fx.conf.getAesOpt().isHashKey(), reread.getAesOpt().isHashKey());
		assertFalse(reread.getAesOpt().isFlagAes());
		assertEquals(7, reread.getTextEditorOpt().getTabSize());
		// the window position is moved by +10 at every load on purpose (cascade of windows): the rest must be identical
		assertEquals("everything the application keeps survives a save and a load", withoutWindowPosition(fx.conf.toStringa()),
				withoutWindowPosition(reread.toStringa()));
		assertNotNull(reread.getGuiOpt());
		assertNotEquals("sanity: the text carries real data", 0, reread.toStringa().length());
	}
}

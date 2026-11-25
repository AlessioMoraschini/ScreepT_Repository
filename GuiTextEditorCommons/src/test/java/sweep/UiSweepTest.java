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
package sweep;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import test.util.gui.UiSweep;

/** Builds, lays out, paints and disposes every window and panel of the module (never shown): see {@link UiSweep} */
public class UiSweepTest {

	private String panelFiles;

	/** The clipboard panel creates its Clipboard.txt in the clipboards folder of the application: here that is a folder of target, not of the project */
	@Before
	public void redirectPanelFiles() {
		panelFiles = resources.GeneralConfig.CLIBPOARDS_PANEL_SRC_FOLDER;
		resources.GeneralConfig.CLIBPOARDS_PANEL_SRC_FOLDER = new java.io.File("target", "sweep-panel-files").getAbsolutePath() + java.io.File.separator;
	}

	@After
	public void restorePanelFiles() {
		resources.GeneralConfig.CLIBPOARDS_PANEL_SRC_FOLDER = panelFiles;
	}

	@Test
	public void everyWindowAndPanelThatCanBeBuiltRenders() throws Exception {
		UiSweep.Result r = UiSweep.run(gui.texteditor.commons.tabbededitor.CustomTabbedEditor.class, null);
		UiSweep.runMethods(gui.texteditor.commons.tabbededitor.CustomTabbedEditor.class, null);
		java.nio.file.Files.write(java.nio.file.Paths.get("target", "uisweep.txt"), (r + " BUILT " + r.builtNames + " FAILED " + r.failedNames).getBytes("UTF-8"));
		assertTrue("the interface thread got stuck on " + r.hung, r.hung.isEmpty());
	}
}

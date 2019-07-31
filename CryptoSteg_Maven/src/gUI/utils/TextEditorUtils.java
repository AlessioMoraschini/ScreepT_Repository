package gUI.utils;

import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import gUI.panels.TextEditor;
import guistyle.GUIStyelSettings;
import tabbedpaneutils.CustomTabbedPaneUI_v1;
import tabbedpaneutils.TabUtils;
import treeutils.TreeUtils;

public class TextEditorUtils {
	
	// TextFileWriter logger creation
	static Logger logger = Logger.getLogger(TextEditorUtils.class);

	public static void updateGuiLafWindows(TextEditor editor) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				GUIStyelSettings.applyNimbusSize(editor.currentConfiguration.getGuiOpt().nimbusSize);
				GUIStyelSettings.updateAllWindowsLaf();
				TextEditor.mainTabList.setUI(new CustomTabbedPaneUI_v1(null, null, null, TextEditor.mainTabList, TabUtils.TAB_BUTTON_CLOSE_ORDER));
				TextEditor.resetDividerColor();
				TextEditor.applyStyleToTabs();
				try {
					TreeUtils.selectFileInNamedFileTree(TextEditor.loadedTabObj.loadedFile, TextEditor.leftSplitTreeFiles);
				} catch (Exception e) {
					logger.debug("Error during current file highlighting in file tree - textEditor");
				}
			}
		});
	}
}

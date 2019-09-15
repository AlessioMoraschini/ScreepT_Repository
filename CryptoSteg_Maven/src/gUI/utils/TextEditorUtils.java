package gUI.utils;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPasswordField;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import amfileutils.FileStuff;
import gUI.panels.TextEditor;
import gUI.panels.TextEditor.*;
import gUI.utils.tree.texteditor.FileNameCellRenderer;
import gUI.utils.tree.texteditor.FileTreeNamedModel;
import guistyle.GUIStyelSettings;
import maingUI.mainGUI;
import reflection.ReflectionUtils;
import splitpanel.FileNamed;
import splitpanel.SplitPaneUtils;
import tabbedpaneutils.CustomTabbedPaneUI_v1;
import tabbedpaneutils.TabUtils;
import treeutils.TreeUtils;

public class TextEditorUtils {
	
	public static File workspaceRoot;
	
	// TextFileWriter logger creation
	static Logger logger = Logger.getLogger(TextEditorUtils.class);
	
	public static boolean checkWorkspaceExistNsel() {
		File workspace = new File (TextEditor.currentConfiguration.getTextEditorOpt().defaultWorkspacePath);
		return(TextEditor.toggleButtonFileTreeWorkspace.isSelected() && workspace.exists());
	}
	
	public static void refreshTree(File currentFile, boolean forceRefresh) {
		workspaceRoot = new File(TextEditor.currentConfiguration.getTextEditorOpt().defaultWorkspacePath);
		try {
			workspaceRoot = workspaceRoot.getCanonicalFile();
		} catch (IOException e) {
			logger.error(e);
		}
		
		// refresh only if tree is not locked
		if (!TextEditor.currentConfiguration.getTextEditorOpt().treeLocked) {
			// TREE UNPINNED
			if(TextEditor.toggleButtonFileTreeWorkspace.isSelected()) {
				// SELECTED WORKSPACE
				if (workspaceRoot != null && workspaceRoot.exists()) {
					// EXISTING WORKSPACE
					refreshFromWorkspace();
				}else {
					// NOT EXISTING WORKSPACE
					refresh(currentFile);
				}
			}else {
				// UNSELECTED WORKSPACE
				refresh(currentFile);
			}
		}else if(forceRefresh) {
			refreshFromWorkspace();
		}
		
		
		TextEditor.leftSplitTreeFiles.setCellRenderer(new FileNameCellRenderer());
		SplitPaneUtils.setDividerColors(null, Color.BLACK, TextEditor.splitPaneMain);
	}
	
	private static void refresh(File currentFile) {
		
		FileNamed root = (FileNamed)TextEditor.leftSplitTreeFiles.getModel().getRoot();
		File ancestor = new File("");
		try {
			ancestor = FileStuff.getNlevelsAncestor(currentFile.getCanonicalFile(), TextEditor.fileTreeAncestorLevel);
			ancestor = ancestor.getCanonicalFile();
		} catch (IOException e1) {
			logger.error(e1);
		}
		
		if (currentFile != null && currentFile.exists() && TextEditor.btnToggleFileView.isSelected()) {

			TextEditor.splitPaneMain.getLeftComponent().setVisible(true);
			TextEditor.splitPaneMain.setDividerLocation(TextEditor.currentConfiguration.getTextEditorOpt().dividerFileTreePosX);
			
			TextEditor.leftSplitTreeFiles.setModel(new FileTreeNamedModel(new FileNamed(ancestor)));
			
			TreeUtils.selectFileInNamedFileTree(currentFile, TextEditor.leftSplitTreeFiles, true);
			TextEditor.dynLblCurrentDir.setText(FileStuff.getNlevelsPathString(FileStuff.getNlevelsAncestor(currentFile, TextEditor.fileTreeAncestorLevel), 2));
			try {
				TextEditor.dynLblCurrentDir.setToolTipText(currentFile.getCanonicalPath());
			} catch (IOException e) {
				logger.error(e);
			}
			logger.debug("Tree refreshed from file: "+currentFile.getAbsolutePath());
		} else {
			
			if(!currentFile.exists()) {
				TextEditor.loadedTabObj.unsaved = true;
				TextEditor.updateSavedLabelNTab(null);
				TextEditor.dynLblCurrentDir.setText(FileStuff.getNlevelsPathString(ancestor, 2));
				try {
					TextEditor.dynLblCurrentDir.setToolTipText(ancestor.getCanonicalPath());
				} catch (IOException e) {
					logger.error(e);
				}
				logger.warn("Tree refresh skipped, file is does not exist or it's null");

			}
			TextEditor.splitPaneMain.setDividerLocation(0.0);
			TextEditor.dynLblCurrentDir.setText(" - ");
			logger.warn("Tree refresh skipped, file is null");
		} 
	}
	
	private static void refreshFromWorkspace() {
		
		// WORKSPACE VALIDATION
		if(workspaceRoot == null || workspaceRoot.getName().equals("") || !workspaceRoot.exists()) {
			TextEditor.toggleButtonFileTreeNewWorkspace.doClick();
			logger.info("Tree refresh from workspace skipped, workspace is null or it doese not exist. Redirecting to new workspace selection");
			return;
		}
		
		if (TextEditor.btnToggleFileView.isSelected()) {
			TextEditor.splitPaneMain.getLeftComponent().setVisible(true);
			TextEditor.splitPaneMain.setDividerLocation(TextEditor.currentConfiguration.getTextEditorOpt().dividerFileTreePosX);
			
			TextEditor.leftSplitTreeFiles.setModel(new FileTreeNamedModel(new FileNamed(workspaceRoot)));
			TreeUtils.selectFileInNamedFileTree(TextEditor.loadedTabObj.loadedFile, TextEditor.leftSplitTreeFiles, true);
			
			TextEditor.dynLblCurrentDir.setText(FileStuff.getNlevelsPathString(workspaceRoot, 2));
			try {
				TextEditor.dynLblCurrentDir.setToolTipText(workspaceRoot.getCanonicalPath());
				logger.debug("Tree refreshed from workspace: "+workspaceRoot.getCanonicalPath());
			} catch (IOException e) {
				logger.error(e);
			}
			
			if(!TextEditor.loadedTabObj.loadedFile.exists()) {
				TextEditor.loadedTabObj.unsaved = true;
				TextEditor.updateSavedLabelNTab(null);
			}
			
		}else {
			logger.warn("\"btnToggleFileView\" is not selected, refresh skipped!");
		}
		
	}
	
	public static void resizeFrameToDefault(mainGUI mainApplication) {
		Rectangle currSize = mainApplication.getMainFrame().getBounds();
		Dimension high = new Dimension(currSize.width - 60, currSize.height - 260);
		Dimension max = new Dimension(currSize.width - 45, currSize.height - 200);
		TextEditor.mainTabList.setPreferredSize(high);
		TextEditor.mainTabList.setMaximumSize(max);
		// mainTabList.setMinimumSize(max);
		TextEditor.loadedTabObj.currentScrollPane.setPreferredSize(high);
		// loadedTabObj.currentScrollPane.setMinimumSize(high);
		Dimension med = new Dimension(currSize.width - TextEditor.loadedTabObj.currentScrollPane.getGutter().getWidth(),
				currSize.height - 300);
		TextEditor.loadedTabObj.textArea.setMaximumSize(med);
		if (!TextEditor.btnToggleFileView.isSelected()) {
			TextEditor.splitPaneMain.setDividerLocation(0);
		}
		// loadedTabObj.textArea.setMinimumSize(high);
		// currentTextEditor.revalidate();
		TextEditor.currentTextEditor.repaint();
	}
	
	public static void updateGuiLafWindows(TextEditor editor) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				GUIStyelSettings.applyNimbusSize(editor.currentConfiguration.getGuiOpt().nimbusSize);
				GUIStyelSettings.updateAllWindowsLaf();
				TextEditor.mainTabList.setUI(new CustomTabbedPaneUI_v1(null, null, null, TextEditor.mainTabList, TabUtils.TAB_BUTTON_CLOSE_ORDER));
				TextEditor.resetDividerColor();
				TextEditor.applyStyleToTabs();
				try {
					TreeUtils.selectFileInNamedFileTree(TextEditor.loadedTabObj.loadedFile, TextEditor.leftSplitTreeFiles, true);
				} catch (Exception e) {
					logger.debug("Error during current file highlighting in file tree - textEditor");
				}
			}
		});
	}
	
	/**
	 * Toggle visibility of text editor header panel,  inverting visible flag at each invocation
	 */
	public static void hideOrShowHeaderPanelLevel1() {
		ArrayList<Component> headerList = getTxtEditorHeaderLevel1();
		for(Component element : headerList) {
			// invert visible flag 
			element.setVisible(!element.isVisible());
		}
		TextEditor.currentTextEditor.headerPanel.setVisible(!TextEditor.currentTextEditor.headerPanel.isVisible());
	}
	
	/**
	 * Toggle visibility of text editor header panel,  inverting visible flag at each invocation
	 */
	public static void hideOrShowFooterPanel() {
		TextEditor.currentTextEditor.footerPanel.setVisible(!TextEditor.currentTextEditor.footerPanel.isVisible());
	}
	
	/**
	 * Retrieve all elements of type JButton or JComboBox contained in TextEditor
	 * 
	 * @return
	 */
	public static ArrayList<Component> getTxtEditorHeaderComponentList() {
		
		ArrayList<Component> headerList = new ArrayList<Component>();
		
		try {
			// BUTTON
			List<JButton> buttons = ReflectionUtils.retrieveFieldsByType(TextEditor.currentTextEditor, JButton.class);
			headerList.addAll(buttons);
			
			// COMBO
			List<JComboBox> combos = ReflectionUtils.retrieveFieldsByType(TextEditor.currentTextEditor, JComboBox.class);
			headerList.addAll(combos);
			
			// CHECKBOX
			List<JCheckBox> checkboxes = ReflectionUtils.retrieveFieldsByType(TextEditor.currentTextEditor, JCheckBox.class);
			headerList.addAll(checkboxes);
			
			// PASSORD
			List<JPasswordField> passwords = ReflectionUtils.retrieveFieldsByType(TextEditor.currentTextEditor, JPasswordField.class);
			headerList.addAll(passwords);
			
			// SPINNER
			List<JSpinner> jspinners = ReflectionUtils.retrieveFieldsByType(TextEditor.currentTextEditor, JSpinner.class);
			headerList.addAll(jspinners);
			
			// SLIDERS
			List<JSlider> jsliders = ReflectionUtils.retrieveFieldsByType(TextEditor.currentTextEditor, JSlider.class);
			headerList.addAll(jsliders);
			
			// JSEPARATORS
			List<JSeparator> jseparators = ReflectionUtils.retrieveFieldsByType(TextEditor.currentTextEditor, JSeparator.class);
			headerList.addAll(jseparators);
			
			// AD-HOC COMPONENTS
			List<Component> adHocs = getSpecificHeaderHideableObjs();
			headerList.addAll(adHocs);
			
		} catch (IllegalArgumentException | IllegalAccessException e) {
			logger.error("Exception during retrieve of elements", e);
		}
		
		return headerList;
	}
	
	/**
	 * Ad-hoc elements to hide
	 * @return List of components to hide/show with header main function
	 */
	private static List<Component> getSpecificHeaderHideableObjs(){
		List<Component> hideableList = new ArrayList<Component>();
		TextEditor txtEditor = TextEditor.currentTextEditor;
		
		hideableList.add(txtEditor.lblMargin);
		hideableList.add(txtEditor.lblFontSize);
		hideableList.add(txtEditor.labelTabSize);
		hideableList.add(txtEditor.lBLMargin);
		hideableList.add(txtEditor.lblSIZE);
		hideableList.add(txtEditor.btnToggleFileView);
		
		return hideableList;
	}
	
	/**
	 * Retrieve all elements of type JButton or JComboBox contained in TextEditor
	 * 
	 * @return
	 */
	public static ArrayList<Component> getTxtEditorHeaderLevel1() {
		
		ArrayList<Component> headerList = new ArrayList<Component>();
		
		try {
						
			// JSEPARATORS
			List<JSeparator> jseparators = ReflectionUtils.retrieveFieldsByType(TextEditor.currentTextEditor, JSeparator.class);
			headerList.addAll(jseparators);
			
		} catch (IllegalArgumentException | IllegalAccessException e) {
			logger.error("Exception during retrieve of elements", e);
		}
		
		return headerList;
	}
	
}

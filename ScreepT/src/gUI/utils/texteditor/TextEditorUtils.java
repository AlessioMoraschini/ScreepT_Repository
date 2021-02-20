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
package gUI.utils.texteditor;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.Gutter;

import dialogutils.JOptionHelperExtended;
import files.FileWorker;
import files.FileVarious;
import files.om.FileNamed;
import frameutils.frame.panels.ShellReadWritePanel;
import frameutils.utils.GuiUtilsExtended;
import gUI.panels.CrypterPanel;
import gUI.panels.TextEditor;
import gUI.panels.subpanels.texteditor.TextEditorFooterPanel;
import gUI.panels.subpanels.texteditor.TextEditorSubHeadButtonsPanel;
import gui.TextEditTooltips;
import gui.dialogs.FindDialogFork;
import gui.dialogs.ReplaceDialogFork;
import gui.dialogs.msg.JOptionHelper;
import guistyle.GUIStyelSettingsExtended;
import impl.om.TextEditorOption;
import maingUI.MainGUI;
import om.SupportedFileTextEditor;
import om.TabTextEditorObject;
import om.themes.Themes;
import om.themes.ThemesWrapper;
import resources.GeneralConfig;
import resources.IconsPathConfigurator;
import resources.SoundsConfigurator;
import resources.SoundsManagerExtended;
import splitpanel.SplitPaneUtils;
import tabbededitor.CustomTabbedEditor;
import treeutils.TreeUtils;
import treeutils.om.FileNameCellRenderer;
import treeutils.om.FileTreeNamedModel;
import utility.reflection.ReflectionUtils;
import utility.string.StringWorker;
import utils.ImageWorkerExtended;
import utils.RsyntaxUtils;
import utils.dialog.ITextAreaDialogs;
import utils.dialog.TextAreaDialogs;
import utils.panels.ClipboardPanel;
import utils.panels.MapTextAreaPanel;
import utils.panels.arch.PanelType;
import utils.panels.arch.ParentPanel;

public class TextEditorUtils {
	
	// TextFileWriter logger creation
	static Logger logger = Logger.getLogger(TextEditorUtils.class);

	public static File workspaceRoot;
	
	// reference to main text editor
	public TextEditor currentTextEditor;
	
	private SupportedFileTextEditor chosenType; // temp variable used in findRightStyleFromComboNSelect()
	
	public TextAreaDialogs textAreaDialogs;
	
	static final int LEVEL_ANCESTOR_LABEL_DIR = 1;
	
	public static final String NO_HIDE_ELEMENT = "NO_HIDE";
	
	JOptionHelperExtended dialogHelper;
	
	public File lastValidTreeSelRefreshed;
	
	private Timer adviceTimer;
	
	public TextEditorUtils(TextEditor currentTextEditor) {
		this.currentTextEditor = currentTextEditor;
		this.textAreaDialogs = new TextAreaDialogs(getDialogManager());
		this.dialogHelper = new JOptionHelperExtended(currentTextEditor);
	}
	
	////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Return the interface with custom method to retrieve target text area in real time for dialogs
	 * (search/replace/gotoline)
	 * 
	 * @return
	 */
	public ITextAreaDialogs getDialogManager() {
		return getDialogManager(true);
	}
	
	public ITextAreaDialogs getDialogManager(boolean loadFileSyntax) {
		return new ITextAreaDialogs() {
			@Override
			public RSyntaxTextArea getTargetTextArea() {
				RSyntaxTextArea result = new RSyntaxTextArea();
				try {
					if (currentTextEditor != null && currentTextEditor.getCurrentTabEditor() != null && currentTextEditor.getCurrentTabEditor().loadedTabObj != null) {
						result = currentTextEditor.getCurrentTabEditor().loadedTabObj.textArea;
					}
				}catch(Exception e) {
					logger.error("ITextAreaDialogs() embedded in TextEditorutils: Error while retrieving textArea.", e);
				}
				return result;
			}

			@Override
			public void loadThemeIntoTarget(RSyntaxTextArea txtArea) {
				TextEditorOption txtOpt = currentTextEditor.currentConfiguration.getTextEditorOpt();
				loadTheme(txtArea, (Gutter) null, txtOpt, false);
				if (!currentTextEditor.isPreloading && loadFileSyntax) {
					applyFileFormatToTextArea(txtArea, false);
				}
			}

			@Override
			public String getCurrentClipboard() {
				return GeneralConfig.CLIPBOARD_PANEL_DEFAULT;
			}

			@Override
			public Dialog getSearchDialog() {
				return currentTextEditor.subHeaderPanel.searchBox;
			}

			@Override
			public Dialog getReplaceDialog() {
				return currentTextEditor.subHeaderPanel.replaceDialog;
			}
		};
	}
	
	/**
	 * If something is selected start last searched with current selection
	 */
	public void addCurrentSelectionToLastSearched() {
		String selectedText = currentTextEditor.getCurrentTabEditor().loadedTabObj.textArea.getSelectedText();
		if(selectedText != null) {
			currentTextEditor.currentConfiguration.getTextEditorOpt().lastSearchTerms.addUniqueToTop(selectedText);
		}
	}

	/**
	 * If something is selected updated last replaced with current selection
	 */
	public void addCurrentSelectionToLastReplaced() {
		String selectedText = currentTextEditor.getCurrentTabEditor().loadedTabObj.textArea.getSelectedText();
		if(selectedText != null) {
			currentTextEditor.currentConfiguration.getTextEditorOpt().lastReplaceTerms.addUniqueToTop(selectedText);
		}
	}
	
	public void applyStyleToButtons(TextEditor currentTextEditor) throws IllegalArgumentException, IllegalAccessException {
		// retrieve all buttons except Containing depth in name
		
		Insets margin = new Insets(1, 6, 0, 6);
		
		List<JButton> textEditorButtons = ReflectionUtils.retrieveFieldsByTypeContaininingFilter(currentTextEditor.subHeaderPanel, JButton.class, "nostyle", true, false);
		for(JButton button : textEditorButtons) {
			button.setCursor(GuiUtilsExtended.CURSOR_HAND);
			button.setBackground(GeneralConfig.BUTTON_BCKG_COLOR_TXT_EDITOR);
			button.setFont(GeneralConfig.BUTTON_DEFAULT_FONT);
			button.setMinimumSize(GeneralConfig.MINIMUM_BUTTON_SIZE);
			button.setMargin(margin);
		}

		List<JToggleButton> textEditorToggleButtons = ReflectionUtils.retrieveFieldsByTypeContaininingFilter(currentTextEditor.subHeaderPanel, JToggleButton.class, "nostyle", true, false);
		for(JToggleButton toggleButton : textEditorToggleButtons) {
			toggleButton.setCursor(GuiUtilsExtended.CURSOR_HAND);
			toggleButton.setBackground(GeneralConfig.BUTTON_BCKG_COLOR_TXT_EDITOR);
			toggleButton.setMinimumSize(GeneralConfig.MINIMUM_BUTTON_SIZE);
			toggleButton.setMargin(margin);
		}
		
	}
	
	/**
	 * @param source the source file
	 * @param autosync if this is true, syntax will be auto synched with current file type
	 */
	public void findRightSyntaxFromComboNSelect(File source, boolean autosync) {

		chosenType = (SupportedFileTextEditor)currentTextEditor.subHeaderPanel.comboFileFormat.getSelectedItem();
		final TabTextEditorObject loadedTabObj = currentTextEditor.getCurrentTabEditor().loadedTabObj;

		if (autosync) {
			if (source == null) {
				chosenType = SupportedFileTextEditor.TXT;

			} else {
				// IF AUTOSYNC FIND RIGHT TYPE BY FILE EXTENSION AND SELECT
				for (SupportedFileTextEditor current : SupportedFileTextEditor.values()) {
					if (FilenameUtils.getExtension(source.getName()).toLowerCase().trim().equals(current.senzapunto.toLowerCase())) {
						// match found
						chosenType = current;
					}
				}
			} 
			
			// APPLY SELECTED
			currentTextEditor.subHeaderPanel.comboFileFormat.setSelectedItem(chosenType);
			if(!loadedTabObj.textArea.getSyntaxEditingStyle().equals(chosenType.syntax)) {
				logger.info(chosenType+" -> File format color scheme applied, it was different from current applied");
				applyFileFormatFromCombo(false);
			}else {
				logger.info(chosenType+" -> File format color scheme not applied, it was the same of current applied");
			}
		
		} else {
			logger.info("Autosync was false, File format color scheme not applied.");
		}
	}
	
	public void setRightPanelContent(PanelType wantedType) {
		if(wantedType.equals(PanelType.MAP_TXTAREA)) {
			currentTextEditor.rightModulePanel = MapTextAreaPanel.getSingletonLazy(getDialogManager());
			currentTextEditor.rightModulePanel.setVisible(true);
			currentTextEditor.rightModuleScrollPane = new JScrollPane(currentTextEditor.rightModulePanel);
			currentTextEditor.rightModuleScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
			currentTextEditor.rightModuleScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			currentTextEditor.splitPaneTreetabsAndRightToggle.setRightComponent(currentTextEditor.rightModuleScrollPane);
			((MapTextAreaPanel)currentTextEditor.rightModulePanel).sync();
			
		}else if(wantedType.equals(PanelType.CLIPBOARD_PANEL)) {
			currentTextEditor.rightModulePanel = ClipboardPanel.getSingletonLazy(getDialogManager(), currentTextEditor.currentConfiguration);
			currentTextEditor.rightModulePanel.setVisible(true);
			currentTextEditor.rightModuleScrollPane = new JScrollPane(currentTextEditor.rightModulePanel);
			currentTextEditor.rightModuleScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
			currentTextEditor.rightModuleScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			currentTextEditor.splitPaneTreetabsAndRightToggle.setRightComponent(currentTextEditor.rightModuleScrollPane);
			((ClipboardPanel)currentTextEditor.rightModulePanel).sync();
			((ClipboardPanel)currentTextEditor.rightModulePanel)
				.selectFromComboClipboards(currentTextEditor.currentConfiguration.getTextEditorOpt().lastFileOpenedClipboard);
		}
	}

	public ParentPanel getTypedIRightPanel() {
		JPanel rightContainer = currentTextEditor.rightModulePanel;
		if (rightContainer != null) {
			if (rightContainer instanceof MapTextAreaPanel) {
				return (MapTextAreaPanel) rightContainer;
			} else if (rightContainer instanceof ClipboardPanel) {
				return (ClipboardPanel) rightContainer;
			} 
		}			
		// default is map
		return MapTextAreaPanel.getSingletonLazy(getDialogManager());
	}
	
	public PanelType getTypeOfRightPanel() {
		JPanel rightContainer = currentTextEditor.rightModulePanel;
		if (rightContainer != null) {
			if (rightContainer instanceof MapTextAreaPanel) {
				return PanelType.MAP_TXTAREA;
			} else if (rightContainer instanceof ClipboardPanel) {
				return PanelType.CLIPBOARD_PANEL;
			} 
		}			
		
		// default is NONE
		return PanelType.NONE;
	}
	
	public boolean isMiniTextAreaActive() {
		return currentTextEditor.currentConfiguration.getTextEditorOpt().mapViewActive && 
				getTypeOfRightPanel().equals(PanelType.MAP_TXTAREA);
	}
	public boolean isClipboardPanelActive() {
		return currentTextEditor.currentConfiguration.getTextEditorOpt().clipboardViewActive && 
				getTypeOfRightPanel().equals(PanelType.CLIPBOARD_PANEL);
	}
	
	public void syncMiniTextAreaContent() {
		if (isMiniTextAreaActive()) {
			MapTextAreaPanel miniMap = (MapTextAreaPanel) getTypedIRightPanel();
			miniMap.syncLight();
		}
	}
	
	public void openCurrentFileInScreept() {
		TabTextEditorObject loadedTabObj = currentTextEditor.getCurrentTabEditor().loadedTabObj;

		// current file not saved
		if (loadedTabObj.unsaved) {
			String currFileName = (loadedTabObj.loadedFile != null) ? loadedTabObj.loadedFile.getName()
					: currentTextEditor.footerPanel.lblCurrentFile.getText();
			int choice = dialogHelper.yesNoOrCanc( currFileName + " is not saved, do you want to save it before to crypt?", null);
			if (choice == JOptionHelperExtended.OK) {
				currentTextEditor.subHeaderPanel.btnSaveTextFile.doClick();
			} else if (choice == JOptionHelperExtended.CANC) {
				return;
			}
		}

		openFileInScreept(loadedTabObj.loadedFile);
	}
	
	public void openFileInScreept(File toOpenInScreept) {
		
		CrypterPanel cipherPanel = MainGUI.mainAPP.getPanelCrypter();
		
		// current file not exists or null
		if (toOpenInScreept == null || !toOpenInScreept.exists()) {
			return;
		}
		
		if (cipherPanel != null) {
			
			int openedFilesCrypter = cipherPanel.getPanelVDragNDropPane().getTreeNodes().length;
			if (openedFilesCrypter != 0) {
				String msg = "There are " + openedFilesCrypter
						+ " loaded files in ScreepT: do you want to remove from loaded list?";
				Integer choice = dialogHelper.yesNoOrCanc(msg, "Empty current loaded files?");
				if (choice == JOptionHelperExtended.OK) {
					cipherPanel.getPanelVDragNDropPane().removeAll();
				} else {
					return;
				} 
			}
		}
		
		MainGUI.mainGuiUtils.startCrypterPanel(false);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					CrypterPanel cipherPanel = null;
					while (cipherPanel == null) {
						Thread.sleep(250);
						cipherPanel = MainGUI.mainAPP.getPanelCrypter();
					}
					cipherPanel.addNewSourceFile(toOpenInScreept);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public void openFileInScreeptWithReturnAction(File toOpen, Runnable returnAction) {
		openFileInScreept(toOpen);
		
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				CrypterPanel cipherPanel = MainGUI.mainAPP.getPanelCrypter();
				if(cipherPanel != null) {
					cipherPanel.setAfterCompleteRunnable(returnAction);
				}
			}
		});
	}
	
	public boolean checkWorkspaceExistNsel() {
		File workspace = new File (currentTextEditor.currentConfiguration.getTextEditorOpt().defaultWorkspacePath);
		return(currentTextEditor.toggleButtonFileTreeWorkspace.isSelected() && workspace.exists());
	}
	
	public void refreshTree(File currentFile, boolean forceRefresh) {
		
		TextEditorOption options = currentTextEditor.currentConfiguration.getTextEditorOpt();
		
		if((currentFile == null || !currentFile.exists()) && !currentTextEditor.currentConfiguration.getTextEditorOpt().workspaceViewSelected) {
			if (!options.treeLocked) {
				try {
					FileNamed oldDir = (FileNamed)currentTextEditor.leftFileTree.getModel().getRoot();
					if (oldDir != null) {
						refreshTreeFromWorkspace(oldDir.getFile());
					}
				} catch (Exception e) {
				}
			}
			return;
		}
		
		lastValidTreeSelRefreshed = currentFile;
		
		// Avoid useless refreshing during preload
		if (!currentTextEditor.isPreloading) {
			workspaceRoot = new File(options.defaultWorkspacePath);
			// refresh only if tree is not locked
			if (!options.treeLocked) {
				// TREE UNPINNED
				if (options.workspaceViewSelected) {
					// SELECTED WORKSPACE
					if (workspaceRoot != null && workspaceRoot.exists()) {
						// EXISTING WORKSPACE
						refreshTreeFromWorkspace(workspaceRoot);
					} else {
						// NOT EXISTING WORKSPACE
						refresh(currentFile);
					}
				} else {
					// UNSELECTED WORKSPACE
					refresh(currentFile);
				}
			} else {
				if (forceRefresh) {
					if (options.workspaceViewSelected) {
						refreshTreeFromWorkspace(workspaceRoot);
					} else {
						refresh(currentFile);
					}
				} else {
					refreshTreeFromLastValid();
				}
			}
			refreshInfoAfterTreeRefresh();
		}
	}
	
	public void refreshTreeFromLastValid() {
		
		FileNamed lastSelectedNode = (FileNamed)currentTextEditor.leftFileTree.getLastSelectedPathComponent();
		File lastSelected = lastSelectedNode != null ? lastSelectedNode.getFile() : lastValidTreeSelRefreshed;
		
		if (lastSelected != null && lastSelected.exists()) {
			File ancestor = FileVarious.getNlevelsAncestor(lastSelected, GeneralConfig.DEFAULT_FILE_TREE_ANCESTOR_LEVEL);
			changeTreeDepth(ancestor, lastSelected);
			refreshInfoAfterTreeRefresh();
		}
	}
	
	public void refreshTreeFromFolder(File folder, boolean forceRefresh) {
		TextEditorOption options = currentTextEditor.currentConfiguration.getTextEditorOpt();
		folder = (folder.isDirectory())? folder : folder.getParentFile();
		File workspaceRoot = folder;
		
		// refresh only if tree is not locked
		if (!options.treeLocked) {
			// TREE UNPINNED
			if(options.workspaceViewSelected) {
				// SELECTED WORKSPACE
				if (workspaceRoot != null && workspaceRoot.exists()) {
					// EXISTING WORKSPACE
					refreshTreeFromWorkspace(workspaceRoot);
				}
			}
		}else if(forceRefresh) {
			refreshTreeFromWorkspace(workspaceRoot);
		}
		
		currentTextEditor.splitPaneTreeAndVerticalSplit.setDividerLocation(getMinimumLeftPaneSize()+10);
		refreshInfoAfterTreeRefresh();
	}
	
	public void refreshInfoAfterTreeRefresh() {
		// Validate dir string length
		currentTextEditor.dynLblCurrentDir.setText(getFooterPanelAdaptedDirString(currentTextEditor.dynLblCurrentDir.getText()));
		currentTextEditor.leftFileTree.setCellRenderer(new FileNameCellRenderer());
		resetAllDIviderColors();
	}
	
	public void resetAllDIviderColors() {
		// Reset all divider colors
		try {
			for(JSplitPane i : ReflectionUtils.retrieveFieldsByType(currentTextEditor, JSplitPane.class)) {
				SplitPaneUtils.setDividerColors(new Color(25,25,25), Color.BLACK, i);
			}
			SplitPaneUtils.setDividerColors(Color.DARK_GRAY.darker(), Color.DARK_GRAY.brighter(), currentTextEditor.bottomModulePanel.splitPane);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			logger.error("Error resetting divider colors: ", e);
		}
	}
	
	public String getFooterPanelAdaptedDirString(String rawText) {
		
		rawText = StringWorker.fitToMaxLentgh(rawText, GeneralConfig.FOOTER_MAX_DIR_LABEL_LENGTH);
		return rawText;
	}
	
	private int getMinimumLeftPaneSize() {
		return (currentTextEditor.dynLblCurrentDir.getWidth() + currentTextEditor.lblCurrentDir.getWidth()); 
	}
	
	/**
	 * Close each single node from bottom to top
	 */
	public void collapseTree() {
		try {
			for (int i = currentTextEditor.leftFileTree.getRowCount(); i >= 0; i--) {
				try {
					currentTextEditor.leftFileTree.collapseRow(i);
				} catch (Exception e) {
				}
			}
		} catch (Exception e) {
			logger.error("Error collapsing tree into textEditor", e);
		}
	}
	
	public boolean renameCurrentFile(String newName, boolean ensureValidExtension) throws IOException {
		return renameFile(currentTextEditor.getCurrentTabEditor().loadedTabObj.loadedFile, newName, ensureValidExtension);
	}
	
	public boolean renameFile(File toRename, String newName, boolean ensureValidExtension) throws IOException {
		if(validateFile(toRename, false) && 
				!StringWorker.trimToEmpty(newName).equals("") &&
				!StringWorker.trimToEmpty(newName).equals(toRename.getName())) {
			
			logger.info("Renaming file: " + toRename);
			
			TabTextEditorObject tabObj = currentTextEditor.getCurrentTabEditor().getAlreadyLoaded(toRename);
			if(tabObj != null && tabObj.unsaved) {
				int choice = dialogHelper.yesNoOrCanc("File is not saved, do you want to save it?", "File changed");
				if (choice == JOptionHelper.OK) {
					currentTextEditor.subHeaderPanel.btnSaveTextFile.doClick();
				} else if(choice == JOptionHelper.CANC) {
					return false;
				}
			}
			
			newName = ensureValidExtension 
					? FileVarious.ensureValidExtension(newName, null)
					: newName;
			File newFile = new File(toRename.getParentFile().getCanonicalPath() + File.separator + newName);
					
			if (newFile.exists()) {
				logger.info("File already exists: " + toRename);
				if(!dialogHelper.askOverwriteFile(newFile)) {
					newFile = FileVarious.uniqueJavaObjFile(newFile);
					logger.info("User don't want to overwrite: " + toRename);
				} else {
					FileUtils.deleteQuietly(newFile);
					logger.info("User has chosen to overwrite: " + toRename);
				}
			}
			
			if(toRename.renameTo(newFile)) {
				FileUtils.deleteQuietly(toRename);
				if (tabObj != null) {
					tabObj.loadedFile = newFile;
					SwingUtilities.invokeLater(() -> {
						currentTextEditor.getCurrentTabEditor().changeMonManager.removePendingFiredChanges(tabObj.loadedFile);
					});
				}
				logger.info("File renamed to: " + newFile);
			} else {
				dialogHelper.warn("Cannot rename!");
				return false;
			}
			
			SwingUtilities.invokeLater(()->{
				currentTextEditor.getCurrentTabEditor().tabRefreshAfterUpdate();
				refreshTree(tabObj.loadedFile, true);
			});
			return true;
		} else 
			return false;
	}
	
	
	public boolean validateFile(File toValidate, boolean acceptDirectories) {
		if (toValidate == null || !toValidate.exists()) {
			String msg = "File does not exist";
			dialogHelper.error(msg, msg);
			return false;
		} else if(!acceptDirectories && toValidate.isDirectory()) {
			String msg = "Directories are not supported for this operation";
			dialogHelper.error(msg, "directories not supported");
			return false;
		}
		
		return true;
	}
	
	private void refresh(File currentFile) {
		
		CustomTabbedEditor currentTabEditor = currentTextEditor.getCurrentTabEditor();
		
		File ancestor = FileVarious.getNlevelsAncestor(currentFile, GeneralConfig.DEFAULT_FILE_TREE_ANCESTOR_LEVEL);

		// ANCESTOR CHECK
		if(ancestor == null || !ancestor.exists()) {
			currentTextEditor.lblCurrentDir.setText(TextEditor.ROOT_STANDARD_LBL);
			refreshTreeFromWorkspace(workspaceRoot);
			return;
		}
		
		// CURRENT FILE CHECK
		if (currentFile != null && currentFile.exists() && currentTextEditor.subHeaderPanel.btnToggleFileView.isSelected()) {

			currentTextEditor.splitPaneTreeAndVerticalSplit.getLeftComponent().setVisible(true);
			currentTextEditor.splitPaneTreeAndVerticalSplit.setDividerLocation(currentTextEditor.currentConfiguration.getTextEditorOpt().dividerFileTreePosX);
			
			currentTextEditor.rightPaneShowOrHide(false);
			
			changeTreeDepth(ancestor, currentFile);
			
			currentTextEditor.dynLblCurrentDir.setText(FileVarious.getNlevelsPathString(ancestor, LEVEL_ANCESTOR_LABEL_DIR));
			try {
				currentTextEditor.dynLblCurrentDir.setToolTipText(currentFile.getCanonicalPath());
			} catch (IOException e) {
				logger.error(e);
			}
			logger.debug("Tree refreshed from file: "+currentFile.getAbsolutePath());
			
		} else if(currentFile != null && !currentFile.exists()){
			
			currentTabEditor.loadedTabObj.unsaved = true;
			currentTabEditor.updateSavedLabelNTab(null);
			currentTextEditor.dynLblCurrentDir.setText(FileVarious.getNlevelsPathString(ancestor, LEVEL_ANCESTOR_LABEL_DIR));
			try {
				currentTextEditor.dynLblCurrentDir.setToolTipText(ancestor.getCanonicalPath());
			} catch (IOException e) {
				logger.error(e);
			}
			logger.warn("Tree refresh skipped, file is does not exist or it's null");
			
			if (!currentTextEditor.currentConfiguration.getTextEditorOpt().treeLocked) {
				currentTextEditor.splitPaneTreeAndVerticalSplit.setDividerLocation(0.0);
			}
			currentTextEditor.dynLblCurrentDir.setText(" - ");
			logger.warn("Tree refresh skipped, file does not exist");
			
			refreshTreeFromWorkspace(ancestor);
		} else {
			if (!currentTextEditor.currentConfiguration.getTextEditorOpt().treeLocked) {
				currentTextEditor.splitPaneTreeAndVerticalSplit.setDividerLocation(0.0);
			}
			currentTextEditor.dynLblCurrentDir.setText(" - ");
			logger.warn("Tree refresh skipped, file is null");
		}
		
		currentTextEditor.lblCurrentDir.setText(TextEditor.ROOT_STANDARD_LBL);
	}
	
	public void refreshTreeFromWorkspace(File workspaceRoot) {
		
		CustomTabbedEditor currentTabEditor = currentTextEditor.getCurrentTabEditor();
		
		// WORKSPACE VALIDATION
		if(workspaceRoot == null || workspaceRoot.getName().equals("") || !workspaceRoot.exists()) {
			currentTextEditor.toggleButtonFileTreeNewWorkspace.doClick();
			logger.info("Tree refresh from workspace skipped, workspace is null or it doese not exist. Redirecting to new workspace selection");
			return;
		}
		
		if (currentTextEditor.currentConfiguration.getTextEditorOpt().fileTreeActive) {
			
			// loadedTabObj VALIDATION
			if(currentTabEditor.loadedTabObj == null || currentTabEditor.loadedTabObj.loadedFile == null) {
				logger.warn("Cannot select current file... Null data for TextEditorUtils.refreshFromWorkspace -> loadedTabObj or loaded file is NULL - index is " + currentTabEditor.mainTabList.getSelectedIndex());
				changeTreeDepth(workspaceRoot, null);
			}else {
				changeTreeDepth(workspaceRoot, currentTabEditor.loadedTabObj.loadedFile);
			}
			
			currentTextEditor.dynLblCurrentDir.setText(FileVarious.getNlevelsPathString(workspaceRoot, LEVEL_ANCESTOR_LABEL_DIR));
			try {
				currentTextEditor.dynLblCurrentDir.setToolTipText(workspaceRoot.getCanonicalPath());
				logger.debug("Tree refreshed from workspace: "+workspaceRoot.getCanonicalPath());
			} catch (IOException e) {
				logger.error(e);
			}
			
			if(currentTabEditor.loadedTabObj != null && currentTabEditor.loadedTabObj.loadedFile != null && !currentTabEditor.loadedTabObj.loadedFile.exists()) {
				currentTabEditor.loadedTabObj.unsaved = true;
				currentTabEditor.updateSavedLabelNTab(null);
			}
			
		}else {
			logger.warn("\"btnToggleFileView\" is not selected, refresh skipped!");
		}
		
		currentTextEditor.lblCurrentDir.setText(TextEditor.ROOT_WORKSPACE_LBL);
	}
	
	public void changeTreeDepth(File newRoot, File toSelect) {
		String[] expansionStBack = TreeUtils.getExpansionStates(currentTextEditor.leftFileTree);
		currentTextEditor.leftFileTree.setModel(new FileTreeNamedModel(new FileNamed(newRoot)));
		TreeUtils.restoreExpansionStates(currentTextEditor.leftFileTree, expansionStBack);
		
		if(toSelect != null) {
			try {
				TreeUtils.selectFileInNamedFileTree(toSelect, currentTextEditor.leftFileTree, true);
			} catch (IOException e1) {
				logger.warn("Some problems occurred in file tree, during search of file into current root", e1);
			}
		}
	}
	
	public void resizeFrameToDefault(MainGUI mainApplication) {
		final CustomTabbedEditor currentTabEditor = currentTextEditor.getCurrentTabEditor();

		final Rectangle currSize = mainApplication.getMainFrame().getBounds();
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				Dimension med = new Dimension(currSize.width - 20, currSize.height - 100);
				Dimension high = new Dimension(currSize.width - 20, currSize.height - 90);
				Dimension max = new Dimension(currSize.width - 20, currSize.height - 85);
				currentTabEditor.mainTabList.setPreferredSize(max);
				currentTabEditor.mainTabList.setMaximumSize(max);
				currentTabEditor.loadedTabObj.currentScrollPane.setPreferredSize(high);
				currentTabEditor.loadedTabObj.textArea.setMaximumSize(med);
				if (high.getWidth() > currentTextEditor.mainScrollPane.getMinimumSize().width) {
					if (high.getHeight() > currentTextEditor.mainScrollPane.getMinimumSize().height) {
						currentTextEditor.mainScrollPane.setPreferredSize(high);
					}
				}
				if (high.getWidth() > currentTextEditor.mainPanel.getMinimumSize().width) {
					if (high.getHeight() > currentTextEditor.mainPanel.getMinimumSize().height) {
						currentTextEditor.mainPanel.setPreferredSize(high);
					}
				}
				if (!currentTextEditor.subHeaderPanel.btnToggleFileView.isSelected() && !currentTextEditor.currentConfiguration.getTextEditorOpt().treeLocked) {
					currentTextEditor.splitPaneTreeAndVerticalSplit.setDividerLocation(0);
				}
				if (currentTextEditor.isRightPaneOn()) {
					currentTextEditor.rightPaneResizeOnly();
				}
				
				adaptToSizeResponsive(MainGUI.mainAPP.getMainFrame().getBounds());
				
				updateBottomDividerLocation();

				currentTextEditor.repaint();
			}
		});
	}
	
	/////////////////////////////////////////////////////////////
	//// BOTTOM PANEL METHODS - START /////
	
	public frameutils.frame.panels.arch.ParentPanel getBottomPane() {
		return currentTextEditor.bottomModulePanel;
	}
	
	public ShellReadWritePanel getBottomShellPanel() {
		if(currentTextEditor.bottomModulePanel instanceof ShellReadWritePanel)
			return (ShellReadWritePanel)getBottomPane();
		else return null;
	}
	
	public void saveBottomDividerLocation() {
		Double ratio = SplitPaneUtils.getDividerRatio(currentTextEditor.splitPaneVerticalAllAndBottomToggle);
		if(ratio < 0.95D)
			currentTextEditor.currentConfiguration.getTextEditorOpt().setDividerBottomPosRatio(ratio);
	}
	
	public void setBottomDividerActive(boolean active, boolean saveConfig) {
		if(active) {
			currentTextEditor.splitPaneVerticalAllAndBottomToggle.getBottomComponent().setVisible(true);
			currentTextEditor.subHeaderPanel.toggleBtnConsole.setBackground(GeneralConfig.BUTTON_BCKG_COLOR_TXT_EDITOR_DARKER);
			resizeBottomPane();
		} else {
			currentTextEditor.subHeaderPanel.toggleBtnConsole.setBackground(GeneralConfig.BUTTON_BCKG_COLOR_TXT_EDITOR);
			currentTextEditor.splitPaneVerticalAllAndBottomToggle.getBottomComponent().setVisible(false);
			currentTextEditor.splitPaneVerticalAllAndBottomToggle.setDividerLocation(1.0D);
		}
		
		currentTextEditor.subHeaderPanel.toggleBtnConsole.setSelected(active);
		
		if(saveConfig)
			currentTextEditor.currentConfiguration.getTextEditorOpt().setBottomViewActive(active);
	}
	
	public void checkRightVisibleIfEnabled() {
		SwingUtilities.invokeLater(() -> {
			if(!isRightPaneVisible())
				currentTextEditor.splitPaneTreetabsAndRightToggle.setDividerLocation(
						MainGUI.mainAPP.getMainFrame().getWidth() -
						currentTextEditor.splitPaneTreeAndVerticalSplit.getDividerLocation() - 
						(getTypeOfRightPanel().equals(PanelType.CLIPBOARD_PANEL)? 350 : 150)
						);
		});
	}
	
	public boolean isRightPaneVisible() {
		return SplitPaneUtils.getDividerRatio(currentTextEditor.splitPaneTreetabsAndRightToggle) < 0.95D;
	}
	
	public void resizeBottomPane() {
		currentTextEditor.splitPaneVerticalAllAndBottomToggle.setDividerLocation(currentTextEditor.currentConfiguration.getTextEditorOpt().dividerBottomPosRatio);
		resizeShellInnerDivider();
	}
	
	public void resizeShellInnerDivider() {
		if(getBottomShellPanel() != null) {
			currentTextEditor.bottomModulePanel.splitPane.setDividerLocation(ShellReadWritePanel.defaultDividerLocationRatio);
		}
	}
	
	public boolean isBottomDividerActive() {
		return SplitPaneUtils.getDividerRatio(currentTextEditor.splitPaneVerticalAllAndBottomToggle) < 0.95D;
	}
	
	public void updateBottomDividerLocation() {
		SwingUtilities.invokeLater(() -> {
			setBottomDividerActive(currentTextEditor.currentConfiguration.getTextEditorOpt().isBottomViewActive(), true);
		});
	}
	
	/////////////////////////////////////////////////////////////
	//// BOTTOM PANEL METHODS - END /////
	
	public void adaptToSizeResponsive(Rectangle mainFrameBounds) {
		if(mainFrameBounds.width > 950 && mainFrameBounds.width < 1200) {
			setSubHeaderHideablesVisible1(false);
			setSubHeaderHideablesVisible2(true);
		} else if(mainFrameBounds.width <= 950) {
			setSubHeaderHideablesVisible2(false);
		} else {
			setSubHeaderHideablesVisible1(true);
			setSubHeaderHideablesVisible2(true);
		}
	}
	
	public void setSubHeaderHideablesVisible1(boolean visible) {
		TextEditorSubHeadButtonsPanel subHeader = currentTextEditor.subHeaderPanel;
		subHeader.slider_margin.setVisible(visible);
		subHeader.sliderFontSize.setVisible(visible);
		subHeader.lblSIZE.setVisible(visible);
		subHeader.lblMargin.setVisible(visible);
		subHeader.lBLMargin.setVisible(visible);
		subHeader.lblFontSize.setVisible(visible);
		
		subHeader.revalidate();
		subHeader.repaint(50);
	}
	
	public void setSubHeaderHideablesVisible2(boolean visible) {
		TextEditorSubHeadButtonsPanel subHeader = currentTextEditor.subHeaderPanel;
		TextEditorFooterPanel footer = currentTextEditor.footerPanel;
		subHeader.labelTabSize.setVisible(visible);
		subHeader.tabSizeSpinner.setVisible(visible);
		subHeader.chckbxShowTabGrid.setVisible(visible);
		
		footer.lblAdvice.setVisible(visible);
		
		footer.revalidate();
		subHeader.revalidate();
		footer.repaint(50);
		subHeader.repaint(50);
	}
	
	public void updateMargin() {
		TabTextEditorObject loadedTabObj = currentTextEditor.getCurrentTabEditor().loadedTabObj;

		int textMargin = currentTextEditor.subHeaderPanel.slider_margin.getValue();
		currentTextEditor.subHeaderPanel.lBLMargin.setText("" + textMargin);
		loadedTabObj.textArea.setMargin(new Insets(textMargin / 2, textMargin, textMargin / 2, textMargin));
		loadedTabObj.textArea.revalidate();
		loadedTabObj.currentScrollPane.revalidate();
		loadedTabObj.textArea.repaint(100);
		loadedTabObj.currentScrollPane.repaint(100);
		currentTextEditor.currentConfiguration.getTextEditorOpt().setMargin(textMargin);
	}
	
	public void updateGuiLafWindows(TextEditor editor) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				CustomTabbedEditor currentTabEditor = currentTextEditor.getCurrentTabEditor();
				GUIStyelSettingsExtended.applyNimbusSize(null, editor.currentConfiguration.getGuiOpt().nimbusSize);
				GUIStyelSettingsExtended.updateAllWindowsLaf();
				resetDividerColor();
				currentTabEditor.applyStyleToTabs();
				try {
					TreeUtils.selectFileInNamedFileTree(currentTabEditor.loadedTabObj.loadedFile, currentTextEditor.leftFileTree, true);
				} catch (Exception e) {
					logger.debug("Error during current file highlighting in file tree - textEditor");
				}
			}
		});
	}
	
	// TOGGLE GUI-BLOCKS VISIBILITY METHODS //
	
	/**
	 * Toggle visibility of text editor panel just under header,  inverting visible flag at each invocation
	 */
	public void toggleImmersiveMode() {
		
		MainGUI mainApp = currentTextEditor.mainApplication;
		
		boolean hideAll = currentTextEditor.headerPanel.isVisible() || currentTextEditor.subHeaderPanel.isVisible() || currentTextEditor.footerPanel.isVisible();
		boolean enableImmersive = hideAll || !mainApp.isFullScreen.get();
		
		setImmersiveMode(enableImmersive);
	}
	
	public void setImmersiveMode (boolean enableImmersive) {
		MainGUI mainApp = currentTextEditor.mainApplication;
		
		mainApp.setFullScreenEnabled(enableImmersive);
		
		TextEditorOption txteditorOpt = currentTextEditor.currentConfiguration.getTextEditorOpt();
		
		if (enableImmersive) {
			
			setFooterVisibile(false);
			setHeaderVisibileLevel1(false);
			setHeaderVisibileLevel2(false);
			
			txteditorOpt.setFooterVisible(txteditorOpt.footerVisible);
			txteditorOpt.setHeaderVisible(txteditorOpt.headerVisible);
			txteditorOpt.setSubHeaderVisible(txteditorOpt.subHeaderVisible);
		} else {
			setFooterVisibile(txteditorOpt.footerVisible);
			setHeaderVisibileLevel1(txteditorOpt.headerVisible);
			setHeaderVisibileLevel2(txteditorOpt.subHeaderVisible);
		}
		
		mainApp.menuBar.setVisible(!enableImmersive);
		mainApp.isImmersiveModeOn.set(enableImmersive);

		GuiUtilsExtended.scrollJScrollPane(currentTextEditor.mainScrollPane, true);
		resizeFrameToDefault(mainApp);
	}
	
	/**
	 * Toggle visibility of text editor header panel,  inverting visible flag at each invocation
	 */
	public void hideOrShowHeaderPanelLevel1() {
		ArrayList<Component> headerList = getTxtEditorHeaderLevel1();
		boolean newVisible = false;
		for(Component element : headerList) {
			// invert visible flag 
			newVisible = !element.isVisible();
			element.setVisible(newVisible);
		}
		
		currentTextEditor.currentConfiguration.getTextEditorOpt().setHeaderVisible(newVisible);
		GuiUtilsExtended.scrollJScrollPane(currentTextEditor.mainScrollPane, true);
		resizeFrameToDefault(currentTextEditor.mainApplication);
		updateTogglesIconsFromConfig();
	}

	/**
	 * Toggle visibility of text editor header panel,  inverting visible flag at each invocation
	 */
	public void setHeaderVisibileLevel1(boolean visibility) {
		ArrayList<Component> headerList = getTxtEditorHeaderLevel1();
		for(Component element : headerList) {
			// invert visible flag 
			element.setVisible(visibility);
		}
		
		currentTextEditor.currentConfiguration.getTextEditorOpt().setHeaderVisible(visibility);
		GuiUtilsExtended.scrollJScrollPane(currentTextEditor.mainScrollPane, true);
		resizeFrameToDefault(currentTextEditor.mainApplication);
		updateTogglesIconsFromConfig();
	}
	
	/**
	 * Toggle visibility of text editor panel just under header,  inverting visible flag at each invocation
	 */
	public void hideOrShowHeaderPanelLevel2() {
		ArrayList<Component> headerList = getTxtEditorHeaderLevel2();
		boolean newVisible = false;
		for(Component element : headerList) {
			// invert visible flag 
			newVisible = !element.isVisible();
			element.setVisible(newVisible);
		}
		
		currentTextEditor.currentConfiguration.getTextEditorOpt().setSubHeaderVisible(newVisible);
		GuiUtilsExtended.scrollJScrollPane(currentTextEditor.mainScrollPane, true);
		resizeFrameToDefault(currentTextEditor.mainApplication);
		updateTogglesIconsFromConfig();
	}

	/**
	 * Toggle visibility of text editor panel just under header,  inverting visible flag at each invocation
	 */
	public void setHeaderVisibileLevel2(boolean visibility) {
		ArrayList<Component> headerList = getTxtEditorHeaderLevel2();
		for(Component element : headerList)
			element.setVisible(visibility);
		
		currentTextEditor.currentConfiguration.getTextEditorOpt().setSubHeaderVisible(visibility);
		GuiUtilsExtended.scrollJScrollPane(currentTextEditor.mainScrollPane, true);
		resizeFrameToDefault(currentTextEditor.mainApplication);
		updateTogglesIconsFromConfig();
	}
	
	/**
	 * Toggle visibility of text editor footer panel,  inverting visible flag at each invocation
	 */
	public void hideOrShowFooterPanel() {
		boolean newVisible = !currentTextEditor.footerPanel.isVisible();
		currentTextEditor.footerPanel.setVisible(newVisible);
		currentTextEditor.currentConfiguration.getTextEditorOpt().setFooterVisible(newVisible);
		GuiUtilsExtended.scrollJScrollPane(currentTextEditor.mainScrollPane, true);
		resizeFrameToDefault(currentTextEditor.mainApplication);
		updateTogglesIconsFromConfig();
	}

	/**
	 * Toggle visibility of text editor footer panel,  inverting visible flag at each invocation
	 */
	public void setFooterVisibile(boolean visibility) {
		currentTextEditor.footerPanel.setVisible(visibility);
		currentTextEditor.currentConfiguration.getTextEditorOpt().setFooterVisible(visibility);
		GuiUtilsExtended.scrollJScrollPane(currentTextEditor.mainScrollPane, true);
		resizeFrameToDefault(currentTextEditor.mainApplication);
		updateTogglesIconsFromConfig();
	}
	
	// START ADVICE TEMPORIZATION AND VISUALIZTION METHODS
		
	public void refreshMessageAndTabTooltip(String message) {
		logger.debug("Starting currentTextEditor.refreshMessageAndTabTooltip() with message: "+message);
		successAdvice(message);
		updateTabTooltip(currentTextEditor.getCurrentTabEditor().mainTabList.getSelectedIndex());
		currentTextEditor.footerPanel.lblCurrentFile.setForeground(ImageWorkerExtended.randomColorRGB(0.1f));
		logger.debug("currentTextEditor.refreshMessageAndTabTooltip() completed - Exiting");
	}

	public void errorAdvice(String msg) {
		SoundsManagerExtended.playSound(SoundsConfigurator.ERROR, null);
		currentTextEditor.footerPanel.dynLblSearchEsit.setForeground(GeneralConfig.ADVICE_FOOTER_KO_COLOR);
		currentTextEditor.footerPanel.dynLblSearchEsit.setText(msg);
		timeoutLblAdvice(GeneralConfig.ADVICE_FOOTER_TIMEOUT);
	}

	public void successAdvice(String msg) {
		currentTextEditor.footerPanel.dynLblSearchEsit.setForeground(GeneralConfig.ADVICE_FOOTER_OK_COLOR);
		currentTextEditor.footerPanel.dynLblSearchEsit.setText(msg);
		timeoutLblAdvice(GeneralConfig.ADVICE_FOOTER_TIMEOUT);
	}

	public void infoAdvice(String msg) {
		currentTextEditor.footerPanel.lblAdvice.setForeground(GeneralConfig.ADVICE_FOOTER_INFO_COLOR);
		currentTextEditor.footerPanel.lblAdvice.setText(msg);
		timeoutLblAdvice(GeneralConfig.ADVICE_FOOTER_TIMEOUT);
	}

	public void updateTabTooltip(int index) {
		CustomTabbedEditor currentTabEditor = currentTextEditor.getCurrentTabEditor();
		
		JPanel tabPanel = currentTabEditor.getPanelFromTabHeader(index);
		if (currentTabEditor.loadedTabObj.loadedFile != null && currentTabEditor.loadedTabObj.loadedFile.exists()) {
			tabPanel.setToolTipText(currentTabEditor.loadedTabObj.loadedFile.getAbsolutePath());
		} else {
			tabPanel.setToolTipText(CustomTabbedEditor.DEFAULT_NEW_FILE_NAME);
		}
	}
	
	public void updateCurrentUnsavedFromFileRead() {
		try {
			CustomTabbedEditor tabbedEditor = currentTextEditor.getCurrentTabEditor();
			boolean fileUnsaved = checkCurrentFileIsUnsaved();
			tabbedEditor.loadedTabObj.unsaved = fileUnsaved;

			currentTextEditor.getCurrentTabEditor().updateSavedLabelNTab(null);
		} catch (Exception e) {
			logger.error("Cannot update unsaved option for current file and current textArea", e);
		}
	}
	
	/**
	 * Reads the content from the current tabObject file, and compares it to the current JTextArea loaded content
	 * If a match is found, returns true, else false is returned.
	 * @return
	 * @throws IOException 
	 */
	public boolean checkCurrentFileIsUnsaved() throws IOException {
		TabTextEditorObject loadedTabObj = currentTextEditor.getCurrentTabEditor().loadedTabObj;
//		String text = loadedTabObj.textArea.getText();
//		boolean normalized = !text.contains("\r\n") && !text.contains("\r");
		return !FileWorker.isTextAreaEqualToFileContent(loadedTabObj.textArea, loadedTabObj.loadedFile);
	}
	
	public void timeoutLblAdvice(int millisec) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				if (adviceTimer != null) {
					adviceTimer.stop();
				}
				adviceTimer = new Timer(millisec, (e) -> {
					currentTextEditor.footerPanel.dynLblSearchEsit.setText("");
				});
				adviceTimer.start();
			}
		}).start();
	}

	// END ADVICE TEMPORIZATION AND VISUALIZTION METHODS
	
	/**
	 * Retrieve header elements to hide contained in TextEditor
	 * 
	 * @return
	 */
	public ArrayList<Component> getTxtEditorHeaderLevel1() {
		
		ArrayList<Component> headerList = new ArrayList<Component>();
		
		try {
						
			// JSEPARATORS
			List<JSeparator> jseparators = ReflectionUtils.retrieveFieldsByTypeContaininingFilter(currentTextEditor , JSeparator.class, NO_HIDE_ELEMENT, true, false);
			headerList.addAll(jseparators);
			headerList.add(currentTextEditor.headerPanel);
			
		} catch (IllegalArgumentException | IllegalAccessException e) {
			SoundsManagerExtended.playSound(SoundsConfigurator.ERROR, null);
			logger.error("Exception during retrieve of elements", e);
		}
		
		return headerList;
	}
	
	/**
	 * Retrieve all elements of type JButton or JComboBox contained in TextEditor
	 * 
	 * @return
	 */
	public ArrayList<Component> getTxtEditorHeaderLevel2() {
		
		ArrayList<Component> headerList = new ArrayList<Component>();
		
		try {
			// JSEPARATORS
			List<JSeparator> jseparators = ReflectionUtils.retrieveFieldsByTypeContaininingFilter(currentTextEditor , JSeparator.class, NO_HIDE_ELEMENT, true, false);
			headerList.addAll(jseparators);
			
			// AD-HOC COMPONENTS
			headerList.add(currentTextEditor.subHeaderPanel);
			
		} catch (IllegalArgumentException | IllegalAccessException e) {
			logger.error("Exception during retrieve of elements", e);
		}
		
		return headerList;
	}
	
	/**
	 * Ad-hoc elements to hide
	 * @return List of components to hide/show with header main function
	 */
	public List<Component> getSpecificHeaderHideableObjs(){
		List<Component> hideableList = new ArrayList<Component>();
		TextEditor txtEditor = currentTextEditor;
		
		hideableList.add(txtEditor.subHeaderPanel.lblMargin);
		hideableList.add(txtEditor.subHeaderPanel.lblFontSize);
		hideableList.add(txtEditor.subHeaderPanel.labelTabSize);
		hideableList.add(txtEditor.subHeaderPanel.lBLMargin);
		hideableList.add(txtEditor.subHeaderPanel.lblSIZE);
		hideableList.add(txtEditor.subHeaderPanel.btnToggleFileView);
		
		return hideableList;
	}

	/**
	 * Retrieve all clickable elements in TextEditor
	 * 
	 * @return
	 */
	public ArrayList<Component> getTxtEditorClickablesList() {
		
		ArrayList<Component> clickableList = new ArrayList<Component>();
		
		clickableList.addAll(getTxtEditorHeaderLevel1());
		clickableList.addAll(getTxtEditorHeaderLevel2());
		clickableList.add(currentTextEditor.footerPanel);
		
		return clickableList;
	}
	
	public void resetDividerColor() {
		if (currentTextEditor.splitPaneTreeAndVerticalSplit != null) {
			resetAllDIviderColors();
		}
	}
	
	public void applySelectedTheme(boolean forceThemeReload) {
		
		TabTextEditorObject loadedTabObj = currentTextEditor.getCurrentTabEditor().loadedTabObj;
		TextEditorOption txtOpt = currentTextEditor.currentConfiguration.getTextEditorOpt();
		// backup unsaved to avoid changing saved info with theme change
		boolean unsaved = loadedTabObj.unsaved;

		loadTheme(loadedTabObj.textArea, loadedTabObj.currentScrollPane.getGutter(), txtOpt, forceThemeReload);
		
		// retore unsaved info backup
		loadedTabObj.unsaved = unsaved;
		
		// Retrieve current pane type and call standard method to sync position and content
		getTypedIRightPanel().sync();
		
		Color textAreaBackground = currentTextEditor.leftFileTree.getBackground().darker();
		currentTextEditor.setBackground(textAreaBackground);
		currentTextEditor.getCurrentTabEditor().mainTabList.setBackground(textAreaBackground);
		currentTextEditor.mainPanel.setBackground(textAreaBackground);
		currentTextEditor.getCurrentTabEditor().mainTabList.setBackground(textAreaBackground);
		currentTextEditor.rightModuleScrollPane.setBackground(textAreaBackground);
	}
	
	/**
	 * nullable gutter
	 * @param gutter
	 * @param txtOpt
	 */
	public void loadTheme(RSyntaxTextArea txtArea, Gutter gutter, TextEditorOption txtOpt, boolean forceFileRefresh) {
		ThemesWrapper theme = (ThemesWrapper) currentTextEditor.subHeaderPanel.comboStylesScheme.getSelectedItem();
		File pathFile = new File(theme.path);
		
		// If none selected or style definition not found on file system -> use default
		if (theme.path.equals(Themes.NONE.defaultPath) || !pathFile.exists()) {
			txtArea.setHighlightCurrentLine(false);
			if (gutter != null) {
				gutter.setBackground(txtOpt.backCol.darker());
			}
			RsyntaxUtils.setBackground(txtArea, txtOpt.backCol);
			RsyntaxUtils.setForeground(txtArea, txtOpt.foreCol);
			txtArea.setForeground(txtOpt.foreCol);
			txtOpt.setSelectedTheme(theme.path);
		} else {
			txtArea.setHighlightCurrentLine(true);
			txtOpt.setSelectedTheme(theme.path);
			
			boolean usePreloadedStringXml = 
					!forceFileRefresh &&
					GeneralConfig.XML_THEMES_CONTENT_MAP != null && 
					GeneralConfig.XML_THEMES_CONTENT_MAP.containsKey(theme.name);
			
			if (usePreloadedStringXml) {
				// If available use lazy initialized in memory
				RsyntaxUtils.changeStyleViaThemeXmlString(txtArea, GeneralConfig.XML_THEMES_CONTENT_MAP.get(theme.name));
			}else {
				// if not available read from file
				RsyntaxUtils.changeStyleViaThemeXml(txtArea, theme.path);
			}
		}

		txtArea.repaint();
	}

	public File adaptFileExt(File selectedFile) {
		String givenExtension = FilenameUtils.getExtension(selectedFile.getName());

		// if not specified extension, invalid or equal to checkbox, then change
		// extension to current in global var
		if (("." + givenExtension).equals(currentTextEditor.CURRENT_EXT_OUT) || givenExtension.equals("")
				|| (("." + givenExtension).length() > 5)) {
			selectedFile = FileVarious.changeExtension(selectedFile, currentTextEditor.CURRENT_EXT_OUT);
		} else {
			selectedFile = FileVarious.changeExtension(selectedFile, "." + givenExtension);
		}

		return selectedFile;
	}
	
	public void applyFileFormatToTextArea(RSyntaxTextArea textArea, boolean repaint) {
		SupportedFileTextEditor selected = (SupportedFileTextEditor) currentTextEditor.subHeaderPanel.comboFileFormat.getSelectedItem();
		textArea.setSyntaxEditingStyle(selected.syntax);
		textArea.revalidate();
		if(repaint)
			textArea.repaint();
	}
	
	/**
	 * If enabled, sync current file format to minimap
	 * @param repaint repaint or not miniMap
	 */
	public void applyFileFormatToMiniMap(boolean repaint) {
		if(isMiniTextAreaActive()) {
			applyFileFormatToTextArea(((MapTextAreaPanel)getTypedIRightPanel()).getTxtAreaMiniMap(), repaint);
		}
	}
	
	public void applyFileFormatFromCombo(boolean forceReload) {
		TabTextEditorObject loadedTabObj = currentTextEditor.getCurrentTabEditor().loadedTabObj;
		boolean unsaved = loadedTabObj.unsaved;
		
		SupportedFileTextEditor selected = (SupportedFileTextEditor) currentTextEditor.subHeaderPanel.comboFileFormat.getSelectedItem();
		String originalStyle = loadedTabObj.textArea.getSyntaxEditingStyle();
		
		logger.info("applyFileFormatFromCombo - START - " + selected.syntax);
		

		// Update text area autocomplete only if selected is different from current one
		if (forceReload || !originalStyle.equals(selected.syntax)) {
			loadedTabObj.textArea.setSyntaxEditingStyle(selected.syntax);
			RsyntaxUtils.addAutoCompletion(loadedTabObj.textArea, selected.languageWordsFile);
			logger.info("\n [APPLIED] => applyFileFormatFromCombo - addAutoCompletion and Style reapplied cause wanted is different from current textarea editing style"
					+ "\nCurrent textarea Style: " + loadedTabObj.textArea.getSyntaxEditingStyle() 
					+ "\nWanted Style: " + selected.syntax + "\n\n"
					+ "\nForced reload=true");
		}else {
			logger.info("\n [SKIPPED] => applyFileFormatFromCombo - addAutoCompletion skipped cause wanted is equal to current textarea editing style"
					+ "\nCurrent textarea Style: " + loadedTabObj.textArea.getSyntaxEditingStyle() 
					+ "\nWanted Style: " + selected.syntax + "\n\n");
		}
		
		// Update gui
		loadedTabObj.textArea.revalidate();
		currentTextEditor.CURRENT_EXT_OUT = selected.extension;
		currentTextEditor.footerPanel.lblAdvice.setText("[Type: " + currentTextEditor.CURRENT_EXT_OUT + "]");
		currentTextEditor.subHeaderPanel.btnSaveTextFileAs.setToolTipText(TextEditTooltips.tooltipSaveTextFileAs + " as " + currentTextEditor.CURRENT_EXT_OUT);

		loadedTabObj.unsaved = unsaved;
		
		applyFileFormatToMiniMap(true);
		
		logger.info("applyFileFormatFromCombo - FINISH - " + selected.syntax);
	}

	/** updates textArea reference in search/replace panels and other that must be in sync with current text area **/
	public void refreshTxtAreaReference() {

		if (currentTextEditor.subHeaderPanel.escapeReplacerPanel != null) {
			currentTextEditor.subHeaderPanel.escapeReplacerPanel.outputTextArea = getDialogManager().getTargetTextArea();
		}

		if (currentTextEditor.subHeaderPanel.templatesPanel != null) {
			currentTextEditor.subHeaderPanel.templatesPanel.outputTextArea = getDialogManager().getTargetTextArea();
		}
		
		FindDialogFork searchBox = currentTextEditor.subHeaderPanel.searchBox;
		ReplaceDialogFork replaceBox = currentTextEditor.subHeaderPanel.replaceDialog;
		if (searchBox != null && replaceBox != null) {
			TextAreaDialogs.allineateFindToReplaceDialog(searchBox, replaceBox);
			TextAreaDialogs.allineateReplaceToFindDialog(searchBox, replaceBox);
		}
		
		if (currentTextEditor.subHeaderPanel.pdfParserBox != null) {
			currentTextEditor.subHeaderPanel.pdfParserBox.outputTEXTarea = getDialogManager().getTargetTextArea();
		}
		
		if (currentTextEditor.subHeaderPanel.imageParserBox != null) {
			currentTextEditor.subHeaderPanel.imageParserBox.outputTEXTarea = getDialogManager().getTargetTextArea();
		}
	}

	public void applyCurrentGraphicsTextArea(RSyntaxTextArea textArea, boolean refreshStyle) {
		
		currentTextEditor.initGuiFromConfig(false, refreshStyle);
		textArea.revalidate();
		textArea.repaint();
	}
	
	public void updateDefaultTxtConfig(Font DEFAULT, Color DEFAULT_BCKG_COL, Color DEFAULT_FORE_COL, int margin) {
		TextEditorOption txtOpt = currentTextEditor.currentConfiguration.getTextEditorOpt();
		txtOpt.setFont(DEFAULT);
		txtOpt.setBackCol(DEFAULT_BCKG_COL);
		txtOpt.setForeCol(DEFAULT_FORE_COL);
		txtOpt.setMargin(margin);
	}
	
	public void setupTogglesOnMainMenu(boolean enabled) {
		MainGUI.mainAPP.setTopRightTogglesEnabled(enabled);
		
		MainGUI.mainAPP.setTopRightToggleTooltip(TextEditTooltips.tooltipToggleFooter, 0);
		MainGUI.mainAPP.setTopRightToggleTooltip(TextEditTooltips.tooltipToggleSubHeader, 1);
		MainGUI.mainAPP.setTopRightToggleTooltip(TextEditTooltips.tooltipToggleHeader, 2);
		
		if (enabled) {
			updateTogglesIconsFromConfig();
			
			MainGUI.toggleTopRightActions.set(0, () -> {
				hideOrShowFooterPanel();
			});
			MainGUI.toggleTopRightActions.set(1, () -> {
				hideOrShowHeaderPanelLevel2();
			});
			MainGUI.toggleTopRightActions.set(2, () -> {
				hideOrShowHeaderPanelLevel1();
			});
		}
	}
	
	public void updateTogglesIconsFromConfig() {
		try {
			TextEditorOption options = currentTextEditor.currentConfiguration.getTextEditorOpt();
			
			MainGUI.mainAPP.setTopRightToggleIcon(options.footerVisible 
					? IconsPathConfigurator.IMG_ICON_FOOTER_VISIBLE 
					: IconsPathConfigurator.IMG_ICON_FOOTER_INVISIBLE, 0);
			
			MainGUI.mainAPP.setTopRightToggleIcon(options.subHeaderVisible 
					? IconsPathConfigurator.IMG_ICON_SUBHEAD_VISIBLE 
					: IconsPathConfigurator.IMG_ICON_SUBHEAD_INVISIBLE, 1);

			MainGUI.mainAPP.setTopRightToggleIcon(options.headerVisible 
					? IconsPathConfigurator.IMG_ICON_HEAD_VISIBLE 
					: IconsPathConfigurator.IMG_ICON_HEAD_INVISIBLE, 2);
			
		} catch (Exception e) {
			logger.error("Cannot update main menu toggles icons from text editor utils.", e);
		}
	}

}

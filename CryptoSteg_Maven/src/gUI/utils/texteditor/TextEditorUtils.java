package gUI.utils.texteditor;

import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
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
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.fife.rsta.ui.search.FindDialog;
import org.fife.rsta.ui.search.ReplaceDialog;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.Gutter;

import amfileutils.FileStuff;
import amfileutils.om.FileNamed;
import dialogutils.JOptionHelper;
import frameutils.utils.GuiUtils;
import gUI.panels.TextEditor;
import gUI.utils.tooltip.TextEditorTooltips;
import guistyle.GUIStyelSettings;
import maingUI.MainGUI;
import media.ImageGeneralWorker;
import media.Sounds;
import om.SupportedFileTextEditor;
import om.TabTextEditorObj;
import om.TextEditorOption;
import om.themes.Themes;
import om.themes.ThemesWrapper;
import reflection.ReflectionUtils;
import resources.GeneralConfig;
import resources.SoundsConfigurator;
import splitpanel.SplitPaneUtils;
import tabbededitor.CustomTabbedEditor;
import treeutils.TreeUtils;
import treeutils.om.FileNameCellRenderer;
import treeutils.om.FileTreeNamedModel;
import utils.RsyntaxUtils;
import utils.dialog.ITextAreaDialogs;
import utils.dialog.TextAreaDialogs;
import utils.panels.ClipboardPanel;
import utils.panels.MapTextAreaPanel;
import utils.panels.arch.PanelType;
import utils.panels.arch.ParentPanel;
import variousutils.HandleString;

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
	
	JOptionHelper dialogHelper;
	
	public TextEditorUtils(TextEditor currentTextEditor) {
		this.currentTextEditor = currentTextEditor;
		this.textAreaDialogs = new TextAreaDialogs(getDialogManager());
		this.dialogHelper = new JOptionHelper(currentTextEditor);
	}
	
	////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Return the interface with custom method to retrieve target text area in real time for dialogs
	 * (search/replace/gotoline)
	 * 
	 * @return
	 */
	public ITextAreaDialogs getDialogManager() {
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
				loadTheme(txtArea, (Gutter) null,currentTextEditor.currentConfiguration.getTextEditorOpt());
			}

			@Override
			public String getCurrentClipboard() {
				return GeneralConfig.CLIPBOARD_PANEL_DEFAULT;
			}
		};
	}
	
	public void applyStyleToButtons(TextEditor currentTextEditor) throws IllegalArgumentException, IllegalAccessException {
		// retrieve all buttons except Containing depth in name
		List<JButton> textEditorButtons = ReflectionUtils.retrieveFieldsByTypeContaininingFilter(currentTextEditor.subHeaderPanel, JButton.class, "nostyle", true, false);
		for(JButton button : textEditorButtons) {
			button.setCursor(GuiUtils.CURSOR_HAND);
			button.setBackground(GeneralConfig.BUTTON_BCKG_COLOR_TXT_EDITOR);
			button.setFont(GeneralConfig.BUTTON_DEFAULT_FONT);
			button.setMinimumSize(GeneralConfig.MINIMUM_BUTTON_SIZE);
		}
		
		currentTextEditor.subHeaderPanel.btnTt.setMinimumSize(new Dimension(65, GeneralConfig.MINIMUM_BUTTON_SIZE.height));
		currentTextEditor.subHeaderPanel.btnLowerAfterDots.setMinimumSize(new Dimension(45, GeneralConfig.MINIMUM_BUTTON_SIZE.height));
	}
	
	/**
	 * @param source the source file
	 * @param autosync if this is true, syntax will be auto synched with current file type
	 */
	public void findRightStyleFromComboNSelect(File source, boolean autosync) {

		chosenType = (SupportedFileTextEditor)currentTextEditor.subHeaderPanel.comboFileFormat.getSelectedItem();
		final TabTextEditorObj loadedTabObj = currentTextEditor.getCurrentTabEditor().loadedTabObj;

		if (source == null) {
			chosenType = SupportedFileTextEditor.TXT;
		
		}else {
			if (autosync) {
				// IF AUTOSYNC FIND RIGHT TYPE BY FILE EXTENSION AND SELECT
				for (SupportedFileTextEditor current : SupportedFileTextEditor.values()) {
					if (FilenameUtils.getExtension(source.getName()).toLowerCase().endsWith(current.senzapunto.toLowerCase())) {
						// match found
						chosenType = current;
					}
				}
			}
		}
		
		// APPLY SELECTED
		currentTextEditor.subHeaderPanel.comboFileFormat.setSelectedItem(chosenType);
		if(!currentTextEditor.subHeaderPanel.chckbxLockLanguage.isSelected() && !loadedTabObj.textArea.getSyntaxEditingStyle().equals(chosenType.syntax)) {
			logger.info(chosenType+" -> Style applied, it was different from current applied");
			applyFileFormatFromCombo();
		}else {
			logger.info(chosenType+" -> Style not applied, it was the same of current applied");
		}
		
	}
	
	public void setRightPanelContent(PanelType wantedType) {
		if(wantedType.equals(PanelType.MAP_TXTAREA)) {
			currentTextEditor.rightSplitContainer = MapTextAreaPanel.getSingletonLazy(getDialogManager());
			currentTextEditor.rightSplitContainer.setVisible(true);
			currentTextEditor.rightSplitPane = new JScrollPane(currentTextEditor.rightSplitContainer);
			currentTextEditor.rightSplitPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
			currentTextEditor.rightSplitPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			currentTextEditor.splitPaneMainContainer.setRightComponent(currentTextEditor.rightSplitPane);
			((MapTextAreaPanel)currentTextEditor.rightSplitContainer).sync();
			
		}else if(wantedType.equals(PanelType.CLIPBOARD_PANEL)) {
			currentTextEditor.rightSplitContainer = ClipboardPanel.getSingletonLazy(getDialogManager(), currentTextEditor.currentConfiguration);
			currentTextEditor.rightSplitContainer.setVisible(true);
			currentTextEditor.rightSplitPane = new JScrollPane(currentTextEditor.rightSplitContainer);
			currentTextEditor.rightSplitPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
			currentTextEditor.rightSplitPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			currentTextEditor.splitPaneMainContainer.setRightComponent(currentTextEditor.rightSplitPane);
			((ClipboardPanel)currentTextEditor.rightSplitContainer).sync();
			((ClipboardPanel)currentTextEditor.rightSplitContainer)
				.selectFromComboClipboards(currentTextEditor.currentConfiguration.getTextEditorOpt().lastFileOpenedClipboard);
		}
	}

	public ParentPanel getTypedIRightPanel() {
		JPanel rightContainer = currentTextEditor.rightSplitContainer;
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
		JPanel rightContainer = currentTextEditor.rightSplitContainer;
		if (rightContainer != null) {
			if (rightContainer instanceof MapTextAreaPanel) {
				return PanelType.MAP_TXTAREA;
			} else if (rightContainer instanceof ClipboardPanel) {
				return PanelType.CLIPBOARD_PANEL;
			} 
		}			
		// default is map
		return PanelType.MAP_TXTAREA;
	}
	
	public boolean checkWorkspaceExistNsel() {
		File workspace = new File (currentTextEditor.currentConfiguration.getTextEditorOpt().defaultWorkspacePath);
		return(currentTextEditor.toggleButtonFileTreeWorkspace.isSelected() && workspace.exists());
	}
	
	public void refreshTree(File currentFile, boolean forceRefresh) {
		
		if(currentFile == null) {
			return;
		}
		
		// Avoid useless refreshing during preload
		if (!currentTextEditor.isPreloading) {
			TextEditorOption options = currentTextEditor.currentConfiguration.getTextEditorOpt();
			workspaceRoot = new File(options.defaultWorkspacePath);
			// refresh only if tree is not locked
			if (!options.treeLocked) {
				// TREE UNPINNED
				if (options.workspaceViewSelected) {
					// SELECTED WORKSPACE
					if (workspaceRoot != null && workspaceRoot.exists()) {
						// EXISTING WORKSPACE
						refreshFromWorkspace(workspaceRoot);
					} else {
						// NOT EXISTING WORKSPACE
						refresh(currentFile);
					}
				} else {
					// UNSELECTED WORKSPACE
					refresh(currentFile);
				}
			} else if (forceRefresh) {
				if (options.workspaceViewSelected) {
					refreshFromWorkspace(workspaceRoot);
				} else {
					refresh(currentFile);
				}
			}
			// Validate dir string length
			currentTextEditor.dynLblCurrentDir.setText(getFooterPanelAdaptedDirString(currentTextEditor.dynLblCurrentDir.getText()));
			currentTextEditor.leftSplitTreeFiles.setCellRenderer(new FileNameCellRenderer());
			resetAllDIviderColors();
			currentTextEditor.fileTreeShowOrHide(options);
			currentTextEditor.rightPaneShowOrHide(false);
			currentTextEditor.leftSplitTreeFiles.scrollRowToVisible(currentTextEditor.leftSplitTreeFiles.getLeadSelectionRow());
		}
	}
	
	public void refreshTreeFromFolder(File folder, File currentFile, boolean forceRefresh) {
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
					refreshFromWorkspace(workspaceRoot);
				}
			}
		}else if(forceRefresh) {
			refreshFromWorkspace(workspaceRoot);
		}
		
		// Validate dir string length
		currentTextEditor.dynLblCurrentDir.setText(getFooterPanelAdaptedDirString(currentTextEditor.dynLblCurrentDir.getText()));
		currentTextEditor.splitPaneMain.setDividerLocation(getMinimumLeftPaneSize()+10);
		
		currentTextEditor.leftSplitTreeFiles.setCellRenderer(new FileNameCellRenderer());
		resetAllDIviderColors();
//		SplitPaneUtils.setDividerColors(null, Color.BLACK, currentTextEditor.splitPaneMain);
	}
	
	public void resetAllDIviderColors() {
		// Reset all divider colors
		try {
			for(JSplitPane i : ReflectionUtils.retrieveFieldsByType(currentTextEditor, JSplitPane.class)) {
				SplitPaneUtils.setDividerColors(new Color(25,25,25), Color.BLACK, i);
			}
		} catch (IllegalArgumentException | IllegalAccessException e) {
			logger.error("Error resetting divider colors: ", e);
		}
	}
	
	public String getFooterPanelAdaptedDirString(String rawText) {
		
		rawText = HandleString.fitToMaxLentgh(rawText, GeneralConfig.FOOTER_MAX_DIR_LABEL_LENGTH);
		return rawText;
	}
	
	private int getMinimumLeftPaneSize() {
		return (currentTextEditor.dynLblCurrentDir.getWidth() + currentTextEditor.lblCurrentDir.getWidth()); 
	}
	
	public void collapseTree() {
		try {
			for (int i = 0; i < currentTextEditor.leftSplitTreeFiles.getRowCount(); i++) {
				try {
					currentTextEditor.leftSplitTreeFiles.collapseRow(i);
				} catch (Exception e) {
				}
			}
		} catch (Exception e) {
			logger.error("Error collapsing tree into textEditor", e);
		}
	}
	
	private void refresh(File currentFile) {
		
		CustomTabbedEditor currentTabEditor = currentTextEditor.getCurrentTabEditor();
		
		File ancestor = new File("");
		ancestor = FileStuff.getNlevelsAncestor(currentFile, GeneralConfig.DEFAULT_FILE_TREE_ANCESTOR_LEVEL);

		// ANCESTOR CHECK
		if(ancestor == null || !ancestor.exists()) {
			currentTextEditor.lblCurrentDir.setText(TextEditor.ROOT_STANDARD_LBL);
			refreshFromWorkspace(workspaceRoot);
			return;
		}
		
		// CURRENT FILE CHECK
		if (currentFile != null && currentFile.exists() && currentTextEditor.subHeaderPanel.btnToggleFileView.isSelected()) {

			currentTextEditor.splitPaneMain.getLeftComponent().setVisible(true);
			currentTextEditor.splitPaneMain.setDividerLocation(currentTextEditor.currentConfiguration.getTextEditorOpt().dividerFileTreePosX);
			
			currentTextEditor.subHeaderPanel.toggleBtnMap.setSelected(currentTextEditor.currentConfiguration.getTextEditorOpt().mapViewActive);
			currentTextEditor.subHeaderPanel.toggleBtnClipboard.setSelected(currentTextEditor.currentConfiguration.getTextEditorOpt().clipboardViewActive);

			currentTextEditor.rightPaneShowOrHide(false);
			
			String[] expansionStBack = TreeUtils.getExpansionStates(currentTextEditor.leftSplitTreeFiles);
			currentTextEditor.leftSplitTreeFiles.setModel(new FileTreeNamedModel(new FileNamed(ancestor)));
			TreeUtils.restoreExpansionStates(currentTextEditor.leftSplitTreeFiles, expansionStBack);
			try {
				TreeUtils.selectFileInNamedFileTree(currentFile, currentTextEditor.leftSplitTreeFiles, true);
			} catch (IOException e1) {
				logger.warn("Some problems occurred in file tree, during search of file into current root", e1);
			}
			currentTextEditor.dynLblCurrentDir.setText(FileStuff.getNlevelsPathString(ancestor, LEVEL_ANCESTOR_LABEL_DIR));
			try {
				currentTextEditor.dynLblCurrentDir.setToolTipText(currentFile.getCanonicalPath());
			} catch (IOException e) {
				logger.error(e);
			}
			logger.debug("Tree refreshed from file: "+currentFile.getAbsolutePath());
			
		} else if(currentFile != null && !currentFile.exists()){
			
			currentTabEditor.loadedTabObj.unsaved = true;
			currentTabEditor.updateSavedLabelNTab(null);
			currentTextEditor.dynLblCurrentDir.setText(FileStuff.getNlevelsPathString(ancestor, LEVEL_ANCESTOR_LABEL_DIR));
			try {
				currentTextEditor.dynLblCurrentDir.setToolTipText(ancestor.getCanonicalPath());
			} catch (IOException e) {
				logger.error(e);
			}
			logger.warn("Tree refresh skipped, file is does not exist or it's null");
			
			if (!currentTextEditor.currentConfiguration.getTextEditorOpt().treeLocked) {
				currentTextEditor.splitPaneMain.setDividerLocation(0.0);
			}
			currentTextEditor.dynLblCurrentDir.setText(" - ");
			logger.warn("Tree refresh skipped, file does not exist");
			
			refreshFromWorkspace(ancestor);
		} else {
			if (!currentTextEditor.currentConfiguration.getTextEditorOpt().treeLocked) {
				currentTextEditor.splitPaneMain.setDividerLocation(0.0);
			}
			currentTextEditor.dynLblCurrentDir.setText(" - ");
			logger.warn("Tree refresh skipped, file is null");
		}
		
		currentTextEditor.lblCurrentDir.setText(TextEditor.ROOT_STANDARD_LBL);
	}
	
	private void refreshFromWorkspace(File workspaceRoot) {
		
		CustomTabbedEditor currentTabEditor = currentTextEditor.getCurrentTabEditor();
		
		// WORKSPACE VALIDATION
		if(workspaceRoot == null || workspaceRoot.getName().equals("") || !workspaceRoot.exists()) {
			currentTextEditor.toggleButtonFileTreeNewWorkspace.doClick();
			logger.info("Tree refresh from workspace skipped, workspace is null or it doese not exist. Redirecting to new workspace selection");
			return;
		}
		
		if (currentTextEditor.currentConfiguration.getTextEditorOpt().fileTreeActive) {
//			currentTextEditor.splitPaneMain.getLeftComponent().setVisible(true);
//			currentTextEditor.splitPaneMain.setDividerLocation(currentTextEditor.currentConfiguration.getTextEditorOpt().dividerFileTreePosX);
			
			String[] expansionStBack = TreeUtils.getExpansionStates(currentTextEditor.leftSplitTreeFiles);
			currentTextEditor.leftSplitTreeFiles.setModel(new FileTreeNamedModel(new FileNamed(workspaceRoot)));
			TreeUtils.restoreExpansionStates(currentTextEditor.leftSplitTreeFiles, expansionStBack);
			try {
				// loadedTabObj VALIDATION
				if(currentTabEditor.loadedTabObj == null || currentTabEditor.loadedTabObj.loadedFile == null) {
					logger.warn("Cannot select current file... Null data for TextEditorUtils.refreshFromWorkspace -> loadedTabObj or loaded file is NULL - index is " + currentTabEditor.mainTabList.getSelectedIndex());
				}else {
					TreeUtils.selectFileInNamedFileTree(currentTabEditor.loadedTabObj.loadedFile, currentTextEditor.leftSplitTreeFiles, true);
				}
			} catch (IOException e1) {
				logger.warn("Some problems occurred in file tree, during search of file into current root", e1);
			}
			
			currentTextEditor.dynLblCurrentDir.setText(FileStuff.getNlevelsPathString(workspaceRoot, LEVEL_ANCESTOR_LABEL_DIR));
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
	
	public void resizeFrameToDefault(MainGUI mainApplication) {
		final CustomTabbedEditor currentTabEditor = currentTextEditor.getCurrentTabEditor();

		final Rectangle currSize = mainApplication.getMainFrame().getBounds();
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				Dimension med = new Dimension(currSize.width - 20, currSize.height - 100);
				Dimension high = new Dimension(currSize.width - 20, currSize.height - 90);
				Dimension max = new Dimension(currSize.width - 20, currSize.height - 85);
				currentTabEditor.mainTabList.setPreferredSize(high);
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
					currentTextEditor.splitPaneMain.setDividerLocation(0);
				}
				if (currentTextEditor.isRightPaneOn()) {
					currentTextEditor.rightPaneResizeOnly();
				}
				currentTextEditor.repaint();
			}
		});
	}
	
	public void updateMargin() {
		TabTextEditorObj loadedTabObj = currentTextEditor.getCurrentTabEditor().loadedTabObj;

		int textMargin = currentTextEditor.subHeaderPanel.slider_margin.getValue();
		currentTextEditor.subHeaderPanel.lBLMargin.setText("" + textMargin);
		loadedTabObj.textArea.setMargin(new Insets(textMargin / 2, textMargin, textMargin / 2, textMargin));
		loadedTabObj.textArea.revalidate();
		loadedTabObj.textArea.repaint();
		currentTextEditor.currentConfiguration.getTextEditorOpt().setMargin(textMargin);
	}
	
	public void updateGuiLafWindows(TextEditor editor) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				CustomTabbedEditor currentTabEditor = currentTextEditor.getCurrentTabEditor();
				GUIStyelSettings.applyNimbusSize(editor.currentConfiguration.getGuiOpt().nimbusSize);
				GUIStyelSettings.updateAllWindowsLaf();
				resetDividerColor();
				currentTabEditor.applyStyleToTabs();
				try {
//					TreeUtils.selectFileInNamedFileTree(currentTabEditor.loadedTabObj.loadedFile, currentTextEditor.leftSplitTreeFiles, true);
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
	public void toggleImersiveMode() {
		
		MainGUI mainApp = currentTextEditor.mainApplication;
		
		ArrayList<Component> clickableList = getTxtEditorClickablesList();
		boolean showAll = clickableList.size()>0 && !clickableList.get(0).isVisible();

		if((!mainApp.isFullScreen.get() && !showAll) || (mainApp.isFullScreen.get() && showAll)) {
			mainApp.mntmFullScreenMode.doClick();
		}
		
		for(Component element : clickableList) {
			// invert visible flag 
			element.setVisible(showAll);
		}
		mainApp.menuBar.setVisible(showAll);
		mainApp.isImmersiveModeOn.set(!showAll);

		GuiUtils.scrollJScrollPane(currentTextEditor.mainScrollPane, true);
		resizeFrameToDefault(mainApp);
	}
	
	/**
	 * Toggle visibility of text editor header panel,  inverting visible flag at each invocation
	 */
	public void hideOrShowHeaderPanelLevel1() {
		ArrayList<Component> headerList = getTxtEditorHeaderLevel1();
		for(Component element : headerList) {
			// invert visible flag 
			element.setVisible(!element.isVisible());
		}
		GuiUtils.scrollJScrollPane(currentTextEditor.mainScrollPane, true);
		resizeFrameToDefault(currentTextEditor.mainApplication);
	}
	
	/**
	 * Toggle visibility of text editor panel just under header,  inverting visible flag at each invocation
	 */
	public void hideOrShowHeaderPanelLevel2() {
		ArrayList<Component> headerList = getTxtEditorHeaderLevel2();
		for(Component element : headerList) {
			// invert visible flag 
			element.setVisible(!element.isVisible());
		}
		GuiUtils.scrollJScrollPane(currentTextEditor.mainScrollPane, true);
		resizeFrameToDefault(currentTextEditor.mainApplication);
	}
	
	/**
	 * Toggle visibility of text editor footer panel,  inverting visible flag at each invocation
	 */
	public void hideOrShowFooterPanel() {
		currentTextEditor.footerPanel.setVisible(!currentTextEditor.footerPanel.isVisible());
		GuiUtils.scrollJScrollPane(currentTextEditor.mainScrollPane, true);
		resizeFrameToDefault(currentTextEditor.mainApplication);
	}
	
	// START ADVICE TEMPORIZATION AND VISUALIZTION METHODS
		
	public void refreshMessageAndTabTooltip(String message) {
		logger.debug("Starting currentTextEditor.refreshMessageAndTabTooltip() with message: "+message);
		successAdvice(message);
		updateTabTooltip(currentTextEditor.getCurrentTabEditor().mainTabList.getSelectedIndex());
		currentTextEditor.footerPanel.lblCurrentFile.setForeground(ImageGeneralWorker.randomColorRGB(0.1f));
		logger.debug("currentTextEditor.refreshMessageAndTabTooltip() completed - Exiting");
	}

	public void errorAdvice(String msg) {
		Sounds.playSound(SoundsConfigurator.ERROR, null);
		currentTextEditor.footerPanel.dynLblSearchEsit.setForeground(new Color(232, 102, 46));
		currentTextEditor.footerPanel.dynLblSearchEsit.setText(msg);
		timeoutLblAdvice(GeneralConfig.ADVICE_FOOTER_TIMEOUT);
	}

	public void successAdvice(String msg) {
		currentTextEditor.footerPanel.dynLblSearchEsit.setForeground(Color.GREEN);
		currentTextEditor.footerPanel.dynLblSearchEsit.setText(msg);
		timeoutLblAdvice(GeneralConfig.ADVICE_FOOTER_TIMEOUT);
	}

	public void infoAdvice(String msg) {
		currentTextEditor.footerPanel.lblAdvice.setForeground(Color.BLUE);
		currentTextEditor.footerPanel.lblAdvice.setText(msg);
		timeoutLblAdvice(GeneralConfig.ADVICE_FOOTER_TIMEOUT);
	}

	public void updateTabTooltip(int index) {
		CustomTabbedEditor currentTabEditor = currentTextEditor.getCurrentTabEditor();
		
		JPanel tabPanel = currentTabEditor.getPanelFromTabHeader(index);
		if (currentTabEditor.loadedTabObj.loadedFile != null && currentTabEditor.loadedTabObj.loadedFile.exists()) {
			tabPanel.setToolTipText(currentTabEditor.loadedTabObj.loadedFile.getAbsolutePath()
					+ TextEditorTooltips.tooltipScrollTabsWithMouseWheel);
		} else {
			tabPanel.setToolTipText(CustomTabbedEditor.DEFAULT_NEW_FILE_NAME
					+ TextEditorTooltips.tooltipScrollTabsWithMouseWheel);
		}
	}
	
	public void timeoutLblAdvice(int millisec) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					currentTextEditor.renewedAdviceTimeout = true;
					Thread.sleep(millisec);
					currentTextEditor.renewedAdviceTimeout = false;
					if(!currentTextEditor.renewedAdviceTimeout) {
						currentTextEditor.footerPanel.dynLblSearchEsit.setText("");
					}
				} catch (InterruptedException e) {
					logger.error("Exception happened!", e);
				}
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
			Sounds.playSound(SoundsConfigurator.ERROR, null);
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
		if (currentTextEditor.splitPaneMain != null) {
			resetAllDIviderColors();
//			SplitPaneUtils.setDividerColors(null, Color.DARK_GRAY, currentTextEditor.splitPaneMain);
		}
	}
	
	public void applySelectedTheme() {
		
		TabTextEditorObj loadedTabObj = currentTextEditor.getCurrentTabEditor().loadedTabObj;
		TextEditorOption txtOpt = currentTextEditor.currentConfiguration.getTextEditorOpt();
		// backup unsaved to avoid changing saved info with theme change
		boolean unsaved = loadedTabObj.unsaved;

		loadTheme(loadedTabObj.textArea, loadedTabObj.currentScrollPane.getGutter(), txtOpt);

		// retore unsaved info backup
		loadedTabObj.unsaved = unsaved;
		
		// Retrieve current pane type and call standard method to sync position and content
		getTypedIRightPanel().sync();
	}
	
	/**
	 * nullable gutter
	 * @param gutter
	 * @param txtOpt
	 */
	public void loadTheme(RSyntaxTextArea txtArea, Gutter gutter, TextEditorOption txtOpt) {
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
			
			boolean usePreloadedStringXml = GeneralConfig.XML_THEMES_CONTENT_MAP != null && GeneralConfig.XML_THEMES_CONTENT_MAP.containsKey(theme.name);
			if (usePreloadedStringXml) {
				// If available use lazy initialized in memory
				RsyntaxUtils.changeStyleViaThemeXmlString(txtArea, GeneralConfig.XML_THEMES_CONTENT_MAP.get(theme.name));
			}else {
				// if not available read from file
				RsyntaxUtils.changeStyleViaThemeXml(txtArea, theme.path);
			}
		}
		// loadedTabObj.textArea.revalidate();
		txtArea.repaint();
	}

	public File adaptFileExt(File selectedFile) {
		String givenExtension = FilenameUtils.getExtension(selectedFile.getName());

		// if not specified extension, invalid or equal to checkbox, then change
		// extension to current in global var
		if (("." + givenExtension).equals(currentTextEditor.CURRENT_EXT_OUT) || givenExtension.equals("")
				|| (("." + givenExtension).length() > 5)) {
			selectedFile = FileStuff.changeExtension(selectedFile, currentTextEditor.CURRENT_EXT_OUT);
		} else {
			selectedFile = FileStuff.changeExtension(selectedFile, "." + givenExtension);
		}

		return selectedFile;
	}
	
	public void applyFileFormatFromCombo() {
		TabTextEditorObj loadedTabObj = currentTextEditor.getCurrentTabEditor().loadedTabObj;
		boolean unsaved = loadedTabObj.unsaved;
		
		SupportedFileTextEditor selected = (SupportedFileTextEditor) currentTextEditor.subHeaderPanel.comboFileFormat.getSelectedItem();
		String originalStyle = loadedTabObj.textArea.getSyntaxEditingStyle();
		
		logger.info("applyFileFormatFromCombo - START - " + selected.syntax);
		

		// Update text area autocomplete only if selected is different from current one
		if (!originalStyle.equals(selected.syntax)) {
			loadedTabObj.textArea.setSyntaxEditingStyle(selected.syntax);
			RsyntaxUtils.addAutoCompletion(loadedTabObj.textArea, selected.languageWordsFile);
			logger.info("\n [APPLIED] => applyFileFormatFromCombo - addAutoCompletion and Style reapplied cause wanted is different from current textarea editing style"
					+ "\nCurrent textarea Style: " + loadedTabObj.textArea.getSyntaxEditingStyle() 
					+ "\nWanted Style: " + selected.syntax + "\n\n");
		}else {
			logger.info("\n [SKIPPED] => applyFileFormatFromCombo - addAutoCompletion skipped cause wanted is equal to current textarea editing style"
					+ "\nCurrent textarea Style: " + loadedTabObj.textArea.getSyntaxEditingStyle() 
					+ "\nWanted Style: " + selected.syntax + "\n\n");
		}
		
		// Update gui
		loadedTabObj.textArea.revalidate();
		currentTextEditor.CURRENT_EXT_OUT = selected.nomeStringa;
		currentTextEditor.footerPanel.lblAdvice.setText("[Current output: " + currentTextEditor.CURRENT_EXT_OUT + "]");
		currentTextEditor.subHeaderPanel.btnSaveTextFileAs.setToolTipText("Choose name and folder, save " + currentTextEditor.CURRENT_EXT_OUT + " file!");

		loadedTabObj.unsaved = unsaved;
		
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
		
		FindDialog searchBox = currentTextEditor.subHeaderPanel.searchBox;
		ReplaceDialog replaceBox = currentTextEditor.subHeaderPanel.replaceDialog;
		if (searchBox != null && replaceBox != null) {
			currentTextEditor.textEditorUtils.textAreaDialogs.allineateFindToReplaceDialog(searchBox, replaceBox);
			currentTextEditor.textEditorUtils.textAreaDialogs.allineateReplaceToFindDialog(searchBox, replaceBox);
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
	
	public void defaultTxtConfig(Font DEFAULT, Color DEFAULT_BCKG_COL, Color DEFAULT_FORE_COL, int margin) {
		TextEditorOption txtOpt = currentTextEditor.currentConfiguration.getTextEditorOpt();
		txtOpt.setFont(DEFAULT);
		txtOpt.setBackCol(DEFAULT_BCKG_COL);
		txtOpt.setForeCol(DEFAULT_FORE_COL);
		txtOpt.setMargin(margin);
	}
	
}

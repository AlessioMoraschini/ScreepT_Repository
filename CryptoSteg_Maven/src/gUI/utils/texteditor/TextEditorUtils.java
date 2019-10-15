package gUI.utils.texteditor;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.Gutter;

import amfileutils.FileStuff;
import dialogutils.JOptionHelper;
import enums.Themes;
import enums.ThemesWrapper;
import frameutils.utils.GuiUtils;
import gUI.panels.TextEditor;
import gUI.utils.tooltip.TextEditorTooltips;
import gUI.utils.tree.texteditor.FileNameCellRenderer;
import gUI.utils.tree.texteditor.FileTreeNamedModel;
import guistyle.GUIStyelSettings;
import maingUI.MainGUI;
import media.ImageGeneralWorker;
import om.SupportedFileTextEditor;
import om.TabTextEditorObj;
import om.TextEditorOption;
import reflection.ReflectionUtils;
import resources.GeneralConfig;
import splitpanel.SplitPaneUtils;
import tabbededitor.CustomTabbedEditor;
import treeutils.TreeUtils;
import treeutils.om.FileNamed;
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
					result = currentTextEditor.getCurrentTabEditor().loadedTabObj.textArea;
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
				return GeneralConfig.CLIPBOARD_PANEL_PREFIX+0;
			}
		};
	}
	
	public static void applyStyleToButtons(TextEditor currentTextEditor) throws IllegalArgumentException, IllegalAccessException {
		List<JButton> textEditorButtons = ReflectionUtils.retrieveFieldsByType(currentTextEditor, JButton.class);
		for(JButton button : textEditorButtons) {
			button.setCursor(GuiUtils.CURSOR_HAND);
			button.setBackground(GeneralConfig.BUTTON_BCKG_COLOR_TXT_EDITOR);
			button.setFont(GeneralConfig.BUTTON_DEFAULT_FONT);
			button.setMinimumSize(GeneralConfig.MINIMUM_BUTTON_SIZE);
		}
		
		currentTextEditor.btnTt.setMinimumSize(new Dimension(65, GeneralConfig.MINIMUM_BUTTON_SIZE.height));
		currentTextEditor.btnLowerAfterDots.setMinimumSize(new Dimension(45, GeneralConfig.MINIMUM_BUTTON_SIZE.height));
	}
	
	/**
	 * @param source the source file
	 * @param autosync if this is true, syntax will be auto synched with current file type
	 */
	public void findRightStyleFromComboNSelect(File source, boolean autosync) {

		if (source == null) {
			return;
		}
		
		// default target current selected as default
		SupportedFileTextEditor chosenType = (SupportedFileTextEditor)currentTextEditor.comboFileFormat.getSelectedItem();
		
		if (autosync) {
			// IF AUTOSYNC FIND RIGHT TYPE BY FILE EXTENSION AND SELECT
			for (SupportedFileTextEditor current : SupportedFileTextEditor.values()) {
				if (FilenameUtils.getExtension(source.getName()).toLowerCase().endsWith(current.senzapunto.toLowerCase())) {
					// match found
					chosenType = current;
				}
			}
		}
		// APPLY SELECTED
		currentTextEditor.comboFileFormat.setSelectedItem(chosenType);
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
			currentTextEditor.rightPaneShowOrHide();
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
	
	private void refresh(File currentFile) {
		
		CustomTabbedEditor currentTabEditor = currentTextEditor.getCurrentTabEditor();
		
		File ancestor = new File("");
		ancestor = FileStuff.getNlevelsAncestor(currentFile, currentTextEditor.fileTreeAncestorLevel);

		// ANCESTOR CHECK
		if(ancestor == null || !ancestor.exists()) {
			currentTextEditor.lblCurrentDir.setText(TextEditor.ROOT_STANDARD_LBL);
			refreshFromWorkspace(workspaceRoot);
			return;
		}
		
		// CURRENT FILE CHECK
		if (currentFile != null && currentFile.exists() && currentTextEditor.btnToggleFileView.isSelected()) {

			currentTextEditor.splitPaneMain.getLeftComponent().setVisible(true);
			currentTextEditor.splitPaneMain.setDividerLocation(currentTextEditor.currentConfiguration.getTextEditorOpt().dividerFileTreePosX);
			
			currentTextEditor.toggleBtnMap.setSelected(currentTextEditor.currentConfiguration.getTextEditorOpt().mapViewActive);
			currentTextEditor.toggleBtnClipboard.setSelected(currentTextEditor.currentConfiguration.getTextEditorOpt().clipboardViewActive);

			currentTextEditor.rightPaneShowOrHide();
			
			currentTextEditor.leftSplitTreeFiles.setModel(new FileTreeNamedModel(new FileNamed(ancestor)));
			
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
			
			currentTextEditor.leftSplitTreeFiles.setModel(new FileTreeNamedModel(new FileNamed(workspaceRoot)));
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
		
		currentTextEditor.lblCurrentDir.setText(currentTextEditor.ROOT_WORKSPACE_LBL);
		
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
				if (!currentTextEditor.btnToggleFileView.isSelected() && !currentTextEditor.currentConfiguration.getTextEditorOpt().treeLocked) {
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

		int textMargin = currentTextEditor.slider_margin.getValue();
		currentTextEditor.lBLMargin.setText("" + textMargin);
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
					TreeUtils.selectFileInNamedFileTree(currentTabEditor.loadedTabObj.loadedFile, currentTextEditor.leftSplitTreeFiles, true);
				} catch (Exception e) {
					logger.debug("Error during current file highlighting in file tree - textEditor");
				}
			}
		});
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
		currentTextEditor.headerPanel.setVisible(!currentTextEditor.headerPanel.isVisible());
		GuiUtils.scrollJScrollPane(currentTextEditor.mainScrollPane, true);
		resizeFrameToDefault(currentTextEditor.mainApplication);
	}
	
	/**
	 * Toggle visibility of text editor header panel,  inverting visible flag at each invocation
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
		currentTextEditor.footerPanel.dynLblSearchEsit.setForeground(new Color(232, 102, 46));
		currentTextEditor.footerPanel.dynLblSearchEsit.setText(msg);
		timeoutLblAdvice(TextEditor.timeoutAdvice);
	}

	public void successAdvice(String msg) {
		currentTextEditor.footerPanel.dynLblSearchEsit.setForeground(Color.GREEN);
		currentTextEditor.footerPanel.dynLblSearchEsit.setText(msg);
		timeoutLblAdvice(TextEditor.timeoutAdvice);
	}

	public void infoAdvice(String msg) {
		currentTextEditor.footerPanel.lblAdvice.setForeground(Color.BLUE);
		currentTextEditor.footerPanel.lblAdvice.setText(msg);
		timeoutLblAdvice(TextEditor.timeoutAdvice);
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
	 * Retrieve all elements of type JButton or JComboBox contained in TextEditor
	 * 
	 * @return
	 */
	public ArrayList<Component> getTxtEditorHeaderComponentList() {
		
		ArrayList<Component> headerList = new ArrayList<Component>();
		
		try {
			// BUTTON
			List<JButton> buttons = ReflectionUtils.retrieveFieldsByType(currentTextEditor, JButton.class);
			headerList.addAll(buttons);
			
			// COMBO
			List<JComboBox> combos = ReflectionUtils.retrieveFieldsByType(currentTextEditor, JComboBox.class);
			headerList.addAll(combos);
			
			// CHECKBOX
			List<JCheckBox> checkboxes = ReflectionUtils.retrieveFieldsByType(currentTextEditor, JCheckBox.class);
			headerList.addAll(checkboxes);
			
			// PASSORD
			List<JPasswordField> passwords = ReflectionUtils.retrieveFieldsByType(currentTextEditor, JPasswordField.class);
			headerList.addAll(passwords);
			
			// SPINNER
			List<JSpinner> jspinners = ReflectionUtils.retrieveFieldsByType(currentTextEditor, JSpinner.class);
			headerList.addAll(jspinners);
			
			// SLIDERS
			List<JSlider> jsliders = ReflectionUtils.retrieveFieldsByType(currentTextEditor, JSlider.class);
			headerList.addAll(jsliders);
			
			// JSEPARATORS
			List<JSeparator> jseparators = ReflectionUtils.retrieveFieldsByTypeContaininingFilter(currentTextEditor , JSeparator.class, NO_HIDE_ELEMENT, true, false);
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
	private List<Component> getSpecificHeaderHideableObjs(){
		List<Component> hideableList = new ArrayList<Component>();
		TextEditor txtEditor = currentTextEditor;
		
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
	public ArrayList<Component> getTxtEditorHeaderLevel1() {
		
		ArrayList<Component> headerList = new ArrayList<Component>();
		
		try {
						
			// JSEPARATORS
			List<JSeparator> jseparators = ReflectionUtils.retrieveFieldsByTypeContaininingFilter(currentTextEditor , JSeparator.class, NO_HIDE_ELEMENT, true, false);
			headerList.addAll(jseparators);
			
		} catch (IllegalArgumentException | IllegalAccessException e) {
			logger.error("Exception during retrieve of elements", e);
		}
		
		return headerList;
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
		ThemesWrapper theme = (ThemesWrapper) currentTextEditor.comboStylesScheme.getSelectedItem();
		File pathFile = new File(theme.path);
		
		// If none selected or style definition not found on file system -> use default
		if (theme.path.equals(Themes.NONE.defaultPath) || !pathFile.exists()) {
			txtArea.setHighlightCurrentLine(false);
			if (gutter != null) {
				gutter.setBackground(txtOpt.backCol.darker());
			}
			RsyntaxUtils.setBackground(txtArea, txtOpt.backCol);
			RsyntaxUtils.setForeground(txtArea, txtOpt.foreCol);
			txtOpt.setSelectedTheme(theme.path);
		} else {
			txtArea.setHighlightCurrentLine(true);
			txtOpt.setSelectedTheme(theme.path);
			RsyntaxUtils.changeStyleViaThemeXml(txtArea, theme.path);
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
		logger.debug("applyFileFormatFromCombo - START");

		SupportedFileTextEditor selected = (SupportedFileTextEditor) currentTextEditor.comboFileFormat.getSelectedItem();
		loadedTabObj.textArea.setSyntaxEditingStyle(selected.syntax);
		SupportedFileTextEditor selExtension = (SupportedFileTextEditor) currentTextEditor.comboFileFormat.getSelectedItem();
		RsyntaxUtils.addAutoCompletion(loadedTabObj.textArea, selExtension.languageWordsFile);
		loadedTabObj.textArea.revalidate();
		currentTextEditor.CURRENT_EXT_OUT = selected.nomeStringa;
		currentTextEditor.footerPanel.lblAdvice.setText("[Current output: " + currentTextEditor.CURRENT_EXT_OUT + "]");
		currentTextEditor.btnSaveTextFileAs.setToolTipText("Choose name and folder, save " + currentTextEditor.CURRENT_EXT_OUT + " file!");

		loadedTabObj.unsaved = unsaved;
		logger.debug("applyFileFormatFromCombo - FINISH");
	}

	/** updates textArea reference in search/replace panels **/
	public void refreshTxtAreaReference() {

		if (currentTextEditor.escapeReplacerPanel != null) {
			currentTextEditor.escapeReplacerPanel.outputTextArea = currentTextEditor.getCurrentTabEditor().loadedTabObj.textArea;
		}
		if (currentTextEditor.pdfParserBox != null) {
			currentTextEditor.pdfParserBox.outputTEXTarea = currentTextEditor.getCurrentTabEditor().loadedTabObj.textArea;
		}
		if (currentTextEditor.imageParserBox != null) {
			currentTextEditor.imageParserBox.outputTEXTarea = currentTextEditor.getCurrentTabEditor().loadedTabObj.textArea;
		}
	}

	public void applyCurrentGraphicsTextArea(RSyntaxTextArea textArea) {
		
		currentTextEditor.initGuiFromConfig(false);
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
	
	public void formatText() {
		boolean error = false;
		String source = new String(currentTextEditor.getCurrentTabEditor().loadedTabObj.textArea.getText());
		String old = new String(source);

		// XML format handler (if xml formatter works then is a xml recognized file)
		if (currentTextEditor.CURRENT_EXT_OUT.equals(".xml")) {
			String formatted = HandleString.xmlPrettyFormat(source, 4);
			if (!formatted.equals(HandleString.ERROR)) {
				source = HandleString.xmlPrettyFormat(source, 4);
				if (source.equals(old)) {
					// already formatted
					source = HandleString.removeLines(source);
				}
			} else {
				error = true;
				JOptionPane.showMessageDialog(
						currentTextEditor.parentFrame,
						"Error reading xml format, maybe it's not a valid xml String?", "XML format Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}

		// apply formatted to textArea
		if (!error && !source.equals(old)) {
			currentTextEditor.getCurrentTabEditor().loadedTabObj.textArea.setText(source);
		}
	}
	
}

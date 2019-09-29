package gUI.utils.texteditor;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SwingUtilities;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import amfileutils.FileStuff;
import dialogutils.MyFileView;
import frameutils.GuiUtils;
import gUI.panels.TextEditor;
import gUI.utils.shortcutshandler.TextEditorMouseWheelListener;
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
import resources.IconsPathConfigurator;
import splitpanel.FileNamed;
import splitpanel.SplitPaneUtils;
import tabbedpaneutils.CustomTabbedPaneUI_v1;
import tabbedpaneutils.TabUtils;
import treeutils.TreeUtils;
import variousutils.HandleString;

public class TextEditorUtils {
	
	public static File workspaceRoot;
	public static volatile boolean tabJustSwitched = false;
	public static int lastIndexTabbedPane = 0;
	public static volatile boolean isRemovingTab = false;
	public static volatile boolean isAddingTab = false;
	public static volatile boolean isFirstTabAdded = false;
	public static volatile boolean alreadyrefreshing = false;
	
	// reference to main text editor
	public static TextEditor currentTextEditor;
	
	// TextFileWriter logger creation
	static Logger logger = Logger.getLogger(TextEditorUtils.class);
	
	static final int LEVEL_ANCESTOR_LABEL_DIR = 1;
	
	public static final String NO_HIDE_ELEMENT = "NO_HIDE";
	
	////////////////////////////////////////////////////////////////////////////////////
	
	public static void applyStyleToButtons(TextEditor currentTextEditor) throws IllegalArgumentException, IllegalAccessException {
		List<JButton> textEditorButtons = ReflectionUtils.retrieveFieldsByType(currentTextEditor, JButton.class);
		for(JButton button : textEditorButtons) {
			button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			button.setBackground(new Color(150,150,150));
			button.setFont(GeneralConfig.BUTTON_DEFAULT_FONT);
			button.setMinimumSize(GeneralConfig.MINIMUM_BUTTON_SIZE);
		}
	}
	
	/**
	 * @param source the source file
	 * @param autosync if this is true, syntax will be auto synched with current file type
	 */
	public static void findRightStyleFromComboNSelect(File source, boolean autosync) {

		if (source == null) {
			return;
		}
		
		// default target current selected as default
		SupportedFileTextEditor chosenType = (SupportedFileTextEditor)TextEditor.comboFileFormat.getSelectedItem();
		
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
		TextEditor.comboFileFormat.setSelectedItem(chosenType);
	}
	
	public static boolean checkWorkspaceExistNsel() {
		File workspace = new File (TextEditor.currentConfiguration.getTextEditorOpt().defaultWorkspacePath);
		return(TextEditor.toggleButtonFileTreeWorkspace.isSelected() && workspace.exists());
	}
	
	public static void refreshTree(File currentFile, boolean forceRefresh) {
		TextEditorOption options = TextEditor.currentConfiguration.getTextEditorOpt();
		workspaceRoot = new File(options.defaultWorkspacePath);
		
		// refresh only if tree is not locked
		if (!options.treeLocked) {
			// TREE UNPINNED
			if(options.workspaceViewSelected) {
				// SELECTED WORKSPACE
				if (workspaceRoot != null && workspaceRoot.exists()) {
					// EXISTING WORKSPACE
					refreshFromWorkspace(workspaceRoot);
				}else {
					// NOT EXISTING WORKSPACE
					refresh(currentFile);
				}
			}else {
				// UNSELECTED WORKSPACE
				refresh(currentFile);
			}
		}else if(forceRefresh) {
			if (options.workspaceViewSelected) {
				refreshFromWorkspace(workspaceRoot);
			}else {
				refresh(currentFile);
			}
		}
		
		// Validate dir string length
		TextEditor.dynLblCurrentDir.setText(getFooterPanelAdaptedDirString(TextEditor.dynLblCurrentDir.getText()));
		
		TextEditor.leftSplitTreeFiles.setCellRenderer(new FileNameCellRenderer());
		SplitPaneUtils.setDividerColors(null, Color.BLACK, TextEditor.splitPaneMain);
	}
	
	public static void refreshTreeFromFolder(File folder, File currentFile, boolean forceRefresh) {
		TextEditorOption options = TextEditor.currentConfiguration.getTextEditorOpt();
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
		TextEditor.dynLblCurrentDir.setText(getFooterPanelAdaptedDirString(TextEditor.dynLblCurrentDir.getText()));
		TextEditor.splitPaneMain.setDividerLocation(getMinimumLeftPaneSize()+10);
		
		TextEditor.leftSplitTreeFiles.setCellRenderer(new FileNameCellRenderer());
		SplitPaneUtils.setDividerColors(null, Color.BLACK, TextEditor.splitPaneMain);
	}
	
	public static String getFooterPanelAdaptedDirString(String rawText) {
		
		rawText = HandleString.fitToMaxLentgh(rawText, GeneralConfig.FOOTER_MAX_DIR_LABEL_LENGTH);
		return rawText;
	}
	
	private static int getMinimumLeftPaneSize() {
		return (TextEditor.dynLblCurrentDir.getWidth() + TextEditor.lblCurrentDir.getWidth()); 
	}
	
	private static void refresh(File currentFile) {
		
		File ancestor = new File("");
		ancestor = FileStuff.getNlevelsAncestor(currentFile, TextEditor.fileTreeAncestorLevel);

		// ANCESTOR CHECK
		if(ancestor == null || !ancestor.exists()) {
			TextEditor.lblCurrentDir.setText(TextEditor.ROOT_STANDARD_LBL);
			return;
		}
		
		// CURRENT FILE CHECK
		if (currentFile != null && currentFile.exists() && TextEditor.btnToggleFileView.isSelected()) {

			TextEditor.splitPaneMain.getLeftComponent().setVisible(true);
			TextEditor.splitPaneMain.setDividerLocation(TextEditor.currentConfiguration.getTextEditorOpt().dividerFileTreePosX);
			
			TextEditor.leftSplitTreeFiles.setModel(new FileTreeNamedModel(new FileNamed(ancestor)));
			
			try {
				TreeUtils.selectFileInNamedFileTree(currentFile, TextEditor.leftSplitTreeFiles, true);
			} catch (IOException e1) {
				logger.warn("Some problems occurred in file tree, during search of file into current root", e1);
			}
			TextEditor.dynLblCurrentDir.setText(FileStuff.getNlevelsPathString(ancestor, LEVEL_ANCESTOR_LABEL_DIR));
			try {
				TextEditor.dynLblCurrentDir.setToolTipText(currentFile.getCanonicalPath());
			} catch (IOException e) {
				logger.error(e);
			}
			logger.debug("Tree refreshed from file: "+currentFile.getAbsolutePath());
			
		} else if(currentFile != null && !currentFile.exists()){
			
			TextEditor.loadedTabObj.unsaved = true;
			TextEditor.updateSavedLabelNTab(null);
			TextEditor.dynLblCurrentDir.setText(FileStuff.getNlevelsPathString(ancestor, LEVEL_ANCESTOR_LABEL_DIR));
			try {
				TextEditor.dynLblCurrentDir.setToolTipText(ancestor.getCanonicalPath());
			} catch (IOException e) {
				logger.error(e);
			}
			logger.warn("Tree refresh skipped, file is does not exist or it's null");
			
			TextEditor.splitPaneMain.setDividerLocation(0.0);
			TextEditor.dynLblCurrentDir.setText(" - ");
			logger.warn("Tree refresh skipped, file is null");
			
		} else {
			TextEditor.splitPaneMain.setDividerLocation(0.0);
			TextEditor.dynLblCurrentDir.setText(" - ");
			logger.warn("Tree refresh skipped, file is null");
		}
		
		TextEditor.lblCurrentDir.setText(TextEditor.ROOT_STANDARD_LBL);
	}
	
	private static void refreshFromWorkspace(File workspaceRoot) {
		
		// WORKSPACE VALIDATION
		if(workspaceRoot == null || workspaceRoot.getName().equals("") || !workspaceRoot.exists()) {
			TextEditor.toggleButtonFileTreeNewWorkspace.doClick();
			logger.info("Tree refresh from workspace skipped, workspace is null or it doese not exist. Redirecting to new workspace selection");
			return;
		}
		
		// loadedTabObj VALIDATION
		if(TextEditor.loadedTabObj == null || TextEditor.loadedTabObj.loadedFile == null) {
			logger.error("Invalid data for TextEditorUtils.refreshFromWorkspace -> loadedTabObj: " + TextEditor.loadedTabObj);
			return;
		}
		
		if (TextEditor.btnToggleFileView.isSelected()) {
			TextEditor.splitPaneMain.getLeftComponent().setVisible(true);
			TextEditor.splitPaneMain.setDividerLocation(TextEditor.currentConfiguration.getTextEditorOpt().dividerFileTreePosX);
			
			TextEditor.leftSplitTreeFiles.setModel(new FileTreeNamedModel(new FileNamed(workspaceRoot)));
			try {
				TreeUtils.selectFileInNamedFileTree(TextEditor.loadedTabObj.loadedFile, TextEditor.leftSplitTreeFiles, true);
			} catch (IOException e1) {
				logger.warn("Some problems occurred in file tree, during search of file into current root", e1);
			}
			
			TextEditor.dynLblCurrentDir.setText(FileStuff.getNlevelsPathString(workspaceRoot, LEVEL_ANCESTOR_LABEL_DIR));
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
		
		TextEditor.lblCurrentDir.setText(TextEditor.ROOT_WORKSPACE_LBL);
		
	}
	
	public static void resizeFrameToDefault(MainGUI mainApplication) {
		Rectangle currSize = mainApplication.getMainFrame().getBounds();
		Dimension med = new Dimension
				(currSize.width-20,currSize.height - 90);
		Dimension high = new Dimension(currSize.width -20, currSize.height - 80);
		Dimension max = new Dimension(currSize.width - 20, currSize.height - 70);
		
		TextEditor.mainTabList.setPreferredSize(high);
		TextEditor.mainTabList.setMaximumSize(max);
		TextEditor.loadedTabObj.currentScrollPane.setPreferredSize(high);
		TextEditor.loadedTabObj.textArea.setMaximumSize(med);
		
		if(high.getWidth() > currentTextEditor.mainScrollPane.getMinimumSize().width) {
			if(high.getHeight() > currentTextEditor.mainScrollPane.getMinimumSize().height) {
				currentTextEditor.mainScrollPane.setPreferredSize(high);
			}
		}
		if(high.getWidth() > currentTextEditor.mainPanel.getMinimumSize().width) {
			if(high.getHeight() > currentTextEditor.mainPanel.getMinimumSize().height) {
				currentTextEditor.mainPanel.setPreferredSize(high);
			}
		}
		
		if (!TextEditor.btnToggleFileView.isSelected()) {
			TextEditor.splitPaneMain.setDividerLocation(0);
		}
		
		currentTextEditor.repaint();
	}
	
	public static void updateGuiLafWindows(TextEditor editor) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				GUIStyelSettings.applyNimbusSize(editor.currentConfiguration.getGuiOpt().nimbusSize);
				GUIStyelSettings.updateAllWindowsLaf();
				TextEditor.mainTabList.setUI(new CustomTabbedPaneUI_v1(null, null, null, TextEditor.mainTabList, TabUtils.TAB_BUTTON_CLOSE_ORDER));
				resetDividerColor();
				applyStyleToTabs();
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
		currentTextEditor.headerPanel.setVisible(!currentTextEditor.headerPanel.isVisible());
		GuiUtils.scrollJScrollPane(currentTextEditor.mainScrollPane, true);
		resizeFrameToDefault(currentTextEditor.mainApplication);
	}
	
	/**
	 * Toggle visibility of text editor header panel,  inverting visible flag at each invocation
	 */
	public static void hideOrShowFooterPanel() {
		currentTextEditor.footerPanel.setVisible(!currentTextEditor.footerPanel.isVisible());
		GuiUtils.scrollJScrollPane(currentTextEditor.mainScrollPane, true);
		resizeFrameToDefault(currentTextEditor.mainApplication);
	}
	
	// START ADVICE TEMPORIZATION AND VISUALIZTION METHODS
		
	public static void refreshMessageAndTabTooltip(String message) {
		logger.debug("Starting TextEditor.refreshMessageAndTabTooltip() with message: "+message);
		successAdvice(message);
		updateTabTooltip(TextEditor.mainTabList.getSelectedIndex());
		TextEditor.lblCurrentFile.setForeground(ImageGeneralWorker.randomColorRGB(0.1f));
		logger.debug("TextEditor.refreshMessageAndTabTooltip() completed - Exiting");
	}

	public static void errorAdvice(String msg) {
		TextEditor.dynLblSearchEsit.setForeground(Color.RED);
		TextEditor.dynLblSearchEsit.setText(msg);
		timeoutLblAdvice(TextEditor.timeoutAdvice);
	}

	public static void successAdvice(String msg) {
		TextEditor.dynLblSearchEsit.setForeground(Color.GREEN);
		TextEditor.dynLblSearchEsit.setText(msg);
		timeoutLblAdvice(TextEditor.timeoutAdvice);
	}

	public static void infoAdvice(String msg) {
		TextEditor.lblAdvice.setForeground(Color.BLUE);
		TextEditor.lblAdvice.setText(msg);
		timeoutLblAdvice(TextEditor.timeoutAdvice);
	}

	public static void updateTabTooltip(int index) {
		JPanel tabPanel = getPanelFromTabHeader(index);
		if (TextEditor.loadedTabObj.loadedFile != null && TextEditor.loadedTabObj.loadedFile.exists()) {
			tabPanel.setToolTipText(TextEditor.loadedTabObj.loadedFile.getAbsolutePath()+TextEditorTooltips.tooltipScrollTabsWithMouseWheel);
		} else {
			tabPanel.setToolTipText(TextEditor.DEFAULT_NEW_FILE_NAME+TextEditorTooltips.tooltipScrollTabsWithMouseWheel);
		}
	}
	
	public static void timeoutLblAdvice(int millisec) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					TextEditor.renewedAdviceTimeout = true;
					Thread.sleep(millisec);
					TextEditor.renewedAdviceTimeout = false;
					if(!TextEditor.renewedAdviceTimeout) {
						TextEditor.dynLblSearchEsit.setText("");
					}
				} catch (InterruptedException e) {
					logger.error("Exception happened!", e);
				}
			}
		}).start();
	}

	public static JPanel getPanelFromTabHeader(int index) {
		JPanel tabPanel = null;
		try {
			tabPanel = (JPanel) TextEditor.mainTabList.getTabComponentAt(index);
		} catch (Exception e) {
			logger.error("Error retrieving panel from header tabbed pane panel", e);
		}
		return tabPanel;
	}
		
	// END ADVICE TEMPORIZATION AND VISUALIZTION METHODS
	
	/**
	 * Retrieve all elements of type JButton or JComboBox contained in TextEditor
	 * 
	 * @return
	 */
	public static ArrayList<Component> getTxtEditorHeaderComponentList() {
		
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
	private static List<Component> getSpecificHeaderHideableObjs(){
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
	public static ArrayList<Component> getTxtEditorHeaderLevel1() {
		
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
	
	// TABBED PAIN - RELATED FUNCTIONS
	
	public static boolean setIconToTab(String iconPath, int index) {
		try {
			SupportedFileTextEditor fileType = (SupportedFileTextEditor) TextEditor.comboFileFormat.getSelectedItem();
			JLabel tabHeadIcon = (JLabel) (TextEditorUtils.getPanelFromTabHeader(index)).getComponent(TabUtils.TAB_ICON_ORDER);
			if (TextEditor.loadedTabObj.loadedFile == null || TextEditor.loadedTabObj.unsaved) {
				tabHeadIcon.setIcon(new ImageIcon(iconPath));
			} else if (fileType.equals(SupportedFileTextEditor.TXT)) {
				tabHeadIcon.setIcon(new ImageIcon(iconPath));
			} else {
				MyFileView fileView = new MyFileView();
				tabHeadIcon.setIcon(fileView.getIcon(TextEditor.loadedTabObj.loadedFile));
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static JPanel getCustomClosableTab(String title, int index) {
		String tooltip = "";
		JScrollPane i_component = TextEditor.loadedList.get(index).currentScrollPane;

		if (TextEditor.loadedTabObj.loadedFile != null && TextEditor.loadedTabObj.loadedFile.exists()) {
			tooltip = TextEditor.loadedTabObj.loadedFile.getAbsolutePath() + TextEditorTooltips.tooltipScrollTabsWithMouseWheel;
		} else {
			tooltip = title + TextEditorTooltips.tooltipScrollTabsWithMouseWheel;
		}

		String icon = "";
		if (TextEditor.DEFAULT_NEW_FILE_NAME.equals(title)) {
			icon = IconsPathConfigurator.ICON_DOC_EMPTY;
		} else {
			icon = IconsPathConfigurator.ICON_DOC_SAVED;
		}
		JPanel customTab = TabUtils.getCustomClosableTab(title, tooltip, icon, getTabCloseListener(index));
		customTab.setMinimumSize(new Dimension(150, 18));
		customTab.setMaximumSize(new Dimension(1000, 18)); // "FACCIAMO... MILLE!" (cit.)
		customTab.addMouseListener(new MouseListener() {
			public void mouseReleased(MouseEvent e) {
				TextEditor.mainTabList.setSelectedComponent(i_component);
			}

			public void mousePressed(MouseEvent e) {
			}

			public void mouseExited(MouseEvent e) {
			}

			public void mouseEntered(MouseEvent e) {
			}

			public void mouseClicked(MouseEvent e) {
			}
		});

		return customTab;
	}
	

	public static ActionListener getTabCloseListener(int index) {
		return new ActionListener() {
			JScrollPane i_component = TextEditor.loadedList.get(index).currentScrollPane;

			@Override
			public void actionPerformed(ActionEvent e) {
				TextEditor.mainTabList.setSelectedComponent(i_component);
				removeTab(TextEditor.mainTabList.getSelectedIndex());
			}
		};
	}
	
	public static boolean saveAllUnsavedExisting() throws IOException {
		boolean done = true;
		for (TabTextEditorObj element : TextEditor.loadedList) {
			if (element.loadedFile != null && element.loadedFile.exists()) {
				if (!FileStuff.writeStringToFile(TextEditor.loadedTabObj.textArea.getText(), TextEditor.loadedTabObj.loadedFile, true)) {
					done = false;
				}
			}
		}
		return done;
	}

	public static void applyStyleToTabs() {
		if (TextEditor.mainTabList != null) {
			TextEditor.mainTabList.setUI(new CustomTabbedPaneUI_v1(null, null, null, TextEditor.mainTabList,
					TabUtils.TAB_BUTTON_CLOSE_ORDER));
		}
		resetDividerColor();
	}

	public static void resetDividerColor() {
		if (TextEditor.splitPaneMain != null) {
			SplitPaneUtils.setDividerColors(null, Color.DARK_GRAY, TextEditor.splitPaneMain);
		}
	}
	
	// ADD / REMOVE TAB
	
	public static void addTab(File file, boolean first) {
		isAddingTab = true;
		backupCurrentBeforeSwitch();
		String name = (file != null) ? file.getName() : TextEditor.DEFAULT_NEW_FILE_NAME;
		int newIndex = TextEditor.mainTabList.getTabCount();

		TabTextEditorObj newTabNode = new TabTextEditorObj(file, "", currentTextEditor.getChangeListener(),
		TextEditor.currentConfiguration.getTextEditorOpt().txtAreaDetailLevel);

		TextEditor.loadedList.add(newTabNode);
		TextEditor.mainTabList.addTab(name, newTabNode.currentScrollPane);
		TextEditor.loadedTabObj = newTabNode;
		FileStuff.appendFileTxtArea(file, TextEditor.loadedTabObj.textArea, true);
		TextEditor.mainTabList.setTabComponentAt(newIndex, getCustomClosableTab(name, newIndex));
		TextEditor.mainTabList.setSelectedIndex(newIndex);
		TextEditor.loadedTabObj.textArea.discardAllEdits();

		TextEditor.loadedTabObj.textArea.addMouseWheelListener(new TextEditorMouseWheelListener(currentTextEditor));
		TextEditor.loadedTabObj.textArea.setFont(TextEditor.currentConfiguration.getTextEditorOpt().font);
		TextEditor.loadedTabObj.unsaved = false;
		isAddingTab = false;
	}
	
	public static void removeTab(int index) {

		if (currentTextEditor.askSave(index)) {
			isRemovingTab = true;
			if (index > 0 && TextEditor.mainTabList.getTabCount() > 1) {
				TextEditor.loadedList.remove(index);
				TextEditor.mainTabList.remove(index);
				if (TextEditor.mainTabList.getTabCount() > index) {
					TextEditor.mainTabList.setSelectedIndex(index);
				} else {
					TextEditor.mainTabList.setSelectedIndex(index - 1);
					TextEditor.updateSavedLabelNTab(index - 1);
				}
				tabRefreshAfterUpdate();

			} else if (index == 0 && TextEditor.mainTabList.getTabCount() > 1) {
				TextEditor.loadedList.remove(0);
				TextEditor.mainTabList.remove(0);
				TextEditor.mainTabList.setSelectedComponent(TextEditor.loadedList.get(0).currentScrollPane);
				tabRefreshAfterUpdate();

			} else if (index == 0) {
				addTab(null, true);
				TextEditor.loadedList.remove(0);
				TextEditor.mainTabList.remove(0);
				isRemovingTab = false;
			}
			isRemovingTab = false;
		}
		TextEditor.loadedTabObj.textArea.requestFocus();
	}

	public static void goToPreviousTab() {
		try {
			int index = TextEditor.mainTabList.getSelectedIndex() - 1;
			if (index < 0) {
				index = TextEditor.mainTabList.getTabCount() - 1;
			}
			TextEditor.mainTabList.setSelectedIndex(index);
		} catch (Exception e) {
			logger.error("Cannot go to previous tab!", e);
		}
	}

	public static void goToNextTab() {
		try {
			int index = TextEditor.mainTabList.getSelectedIndex() + 1;
			if (index >= TextEditor.mainTabList.getTabCount()) {
				index = 0;
			}
			TextEditor.mainTabList.setSelectedIndex(index);
		} catch (Exception e) {
			logger.error("Cannot go to next tab!", e);
		}
	}

	public static void closeCurrentTab() {
		int index = TextEditor.mainTabList.getSelectedIndex();
		try {
			logger.debug("closeCurrentTab() - closing tab: "+TextEditor.mainTabList.getTitleAt(index));
			JButton closeTabButton = (JButton) (TextEditorUtils.getPanelFromTabHeader(index))
					.getComponent(TabUtils.TAB_BUTTON_CLOSE_ORDER);
			closeTabButton.doClick();
		} catch (Exception e) {
			logger.error("Error closing current tab: " + index, e);
		}
	}
	
	public static void tabRefreshAfterUpdate() {
		tabJustSwitched = true;
		TextEditor.loadedTabObj.textArea.setCursor(GuiUtils.CURSOR_WAIT);

		if (!isAddingTab && !isRemovingTab) {
			backupCurrentBeforeSwitch();
		}
		TextEditor.loadedTabObj = TextEditor.loadedList.get(TextEditor.mainTabList.getSelectedIndex());
		TextEditor.updateSavedLabelNTab(null);
		TextEditor.loadedTabObj.textArea.setCursor(GuiUtils.CURSOR_TEXT);
		tabJustSwitched = false;
		TextEditor.loadedTabObj.textArea.requestFocus();
		currentTextEditor.applyCurrentGraphicsTextArea(TextEditor.loadedTabObj.textArea);

		// update load target textArea
		currentTextEditor.refreshTxtAreaReference();
		TextEditorUtils.resizeFrameToDefault(currentTextEditor.mainApplication);

		TextEditorUtils.refreshTree(TextEditor.loadedTabObj.loadedFile, false);

		// update eventually opened info frames
		if (currentTextEditor.propertiesFrame != null) {
			if (TextEditor.loadedTabObj != null && TextEditor.loadedTabObj.loadedFile != null && TextEditor.loadedTabObj.loadedFile.exists()) {
				currentTextEditor.propertiesFrame.refreshValues(TextEditor.loadedTabObj.loadedFile);
			}
		}
	}
	
	private static void backupCurrentBeforeSwitch() {
		
		if (isFirstTabAdded && GeneralConfig.TEXT_EDITOR_BACKUP_BEFORE_SWITCH_ENABLED) {
			try {
				logger.debug("backupCurrentBeforeSwitch() - starting");
				TabTextEditorObj toSaveObj = TextEditor.loadedList.get(TextEditorUtils.lastIndexTabbedPane);
				toSaveObj.loadedFile = (TextEditor.loadedTabObj.loadedFile != null) ? new File(TextEditor.loadedTabObj.loadedFile.getAbsolutePath()) : null;
				toSaveObj.unsaved = TextEditor.loadedTabObj.unsaved;
				logger.debug("backupCurrentBeforeSwitch() - completed");
			} catch (Exception e) {
				logger.error("Error during backup before switch :(", e);
			} 
		}
	}
	
	public static int checkFilePresence(File match) {
		int i = 0;
		for (TabTextEditorObj iterator : TextEditor.loadedList) {
			if (iterator.loadedFile != null && iterator.loadedFile.equals(match)) {
				TextEditor.mainTabList.setSelectedComponent(iterator.currentScrollPane);
				i++;
				return i;
			}
		}

		return -1;
	}
	
}

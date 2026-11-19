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
package screept.gui.utils.tree.texteditor;

import various.common.light.i18n.I18n;
import various.common.light.gui.ShortcutKeys;
import static java.awt.Event.ALT_MASK;
import static java.awt.Event.SHIFT_MASK;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import gui.commons.dialogutils.JOptionHelperExtended;
import gui.commons.frameutils.frame.HashCheckerFrame;
import gui.commons.frameutils.utils.GuiUtilsExtended;
import gui.commons.mouseutils.TreeFileMouseListenerBasicFileNamed;
import gui.commons.treeutils.TreeUtils;
import gui.commons.treeutils.om.FileNameCellRenderer;
import gui.texteditor.commons.tabbededitor.CustomTabbedEditor;
import gui.texteditor.commons.utils.dialog.ColorDialogUtils;
import initializer.configs.impl.INItializer;
import maingUI.MainGuiGeneralAppStarter;
import plugin.api.AbstractPluginApplicationApi.PluginType;
import plugin.external.arch.IPlugin;
import plugin.external.arch.IPluginParentExecutor.FunctionType;
import resources.GeneralConfig;
import resources.IconsPathConfigurator;
import screept.gui.panels.TextEditor;
import screept.plugins.ScreepTApplicationApi;
import various.common.light.files.FileVarious;
import various.common.light.files.FileWorker;
import various.common.light.files.om.FileNamed;
import various.common.light.gui.GuiUtils;
import various.common.light.gui.MenuSetter;
import various.common.light.utility.manipulation.ConversionUtils;

public class TreeFileMouseListenerTxtEditor extends TreeFileMouseListenerBasicFileNamed {

	static Logger logger = Logger.getLogger(TreeFileMouseListenerTxtEditor.class);

	// gui elements
	private int selectedRow;
	private TextEditor textEditor;
	private CustomTabbedEditor currentTabEditor;

	public JMenu subMenuNew;
	/** "Open with": the file editor or an integrated viewer, only for this opening */
	public JMenu itemOpenWith;
	/** "Open in a window": the embedded tools of the Various menu that open a file (LogSearch, Image Studio, Hash checker) */
	public JMenu itemOpenInWindow;
		public JMenuItem itemNewFile;
		public JMenuItem itemNewFolder;
	public JMenuItem itemRename;
	public JMenuItem copyItem;
	public JMenuItem copyPathsItem;
	public JMenuItem pasteItem;
	public JMenuItem copyHereCurrentItem;
	public JMenuItem moveHereCurrentItem;
	public JMenuItem sysExplorerItem;
	public JMenuItem searchContentItem;
	public JMenu pluginsMenuItem;
	public JMenuItem refreshItem;
	public JMenuItem selectAllItem;
	public JMenuItem collapseItem;
	public JMenuItem expandItem;
	public JMenuItem hashingItem;
	public JMenuItem workspaceItem;
	public JMenuItem propertiesItem;
	public JMenuItem changeBckgItem;

	public JOptionHelperExtended dialogHelper;

	// constructor
	public TreeFileMouseListenerTxtEditor(JTree tree, TextEditor textEdit){
		this(tree, textEdit, true);
	}

	public TreeFileMouseListenerTxtEditor(JTree tree, TextEditor textEdit, boolean autoAddListener){
		super(tree);
		parent = textEdit;
		this.tree = tree;
		textEditor = textEdit;
		currentTabEditor = textEditor.getCurrentTabEditor();
		dialogHelper = new JOptionHelperExtended(textEditor);
	}

	@Override
	public MouseListener getMouseListener() {
	    return new MouseAdapter() {

	        @Override
	        public void mousePressed(MouseEvent arg0) {

	        	selectedRow = tree.getRowForLocation(arg0.getX(), arg0.getY());

	        	// Right click
            	updateEnabledOrDisabled();

                TreePath pathForLocation = tree.getPathForLocation(arg0.getPoint().x, arg0.getPoint().y);
                TreePath[] pathsSelection = tree.getSelectionPaths();
                updateSelectedNodes(pathsSelection);
                if(pathForLocation != null){
                	tree.setSelectionPaths(pathsSelection);
                    selectedNode = (FileNamed) pathForLocation.getLastPathComponent();
                } else{
                    selectedNode = null;
                }

                if(SwingUtilities.isRightMouseButton(arg0)) {
            		updateAvailablePlugins(ScreepTApplicationApi.getAvailablePlugins(PluginType.TEXT_EDITOR, false, true));
                }

	            super.mousePressed(arg0);
	        }

			@Override
			public void mouseExited(MouseEvent e) {
				super.mouseExited(e);
			}

	    };
	}

	@Override
	public Vector<FileNamed> updateSelectedNodes(TreePath[] selectionPaths) {
		return super.updateSelectedNodes(selectionPaths);
	}

	public  MouseAdapter getDoubleCLickListener() {
		return new MouseAdapter() {
		    @Override
			public void mousePressed(MouseEvent e) {
		        int selRow = tree.getRowForLocation(e.getX(), e.getY());
		        TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
		        if(selRow != -1 && e.getClickCount() == 2) {
		        	if(selPath != null){
	                    selectedNode = (FileNamed) selPath.getLastPathComponent();
	                    if (selectedNode.mFile != null && selectedNode.mFile.exists() && selectedNode.mFile.isFile()) {
	                    	//	                myDoubleClick(selRow, selPath);
	                    	currentTabEditor.loadFile(selectedNode.mFile);
	                    }
	                } else{
	                    selectedNode = null;
	                }
		        }
		    }
		};
	}

	private void updateEnabledOrDisabled() {

		JMenuItem[] ifFile = new JMenuItem[] {itemLoadFile, itemOpenWith, itemOpenInWindow, itemRename};
		JMenuItem[] ifDir = new JMenuItem[] {workspaceItem};
		JMenuItem[] ifCurrentTabFileExists = new JMenuItem[] {copyHereCurrentItem, moveHereCurrentItem};
		JMenuItem[] ifExists = new JMenuItem[] {
				copyHereCurrentItem, moveHereCurrentItem, itemRename, sysExplorerItem, searchContentItem,
				copyPathsItem, hashingItem, deleteItem, copyItem, pasteItem, itemLoadFile, itemOpenWith, itemOpenInWindow,
				workspaceItem, propertiesItem, pluginsMenuItem};

		if(selectedNode == null || selectedNode.mFile == null || !selectedNode.mFile.exists()) {
			subMenuNew.setEnabled(false);
	    	for(JMenuItem jNull : ifExists)
	    		jNull.setEnabled(false);

		} else {
			subMenuNew.setEnabled(true);
			for(JMenuItem jNull : ifExists)
	    		jNull.setEnabled(true);

			if(selectedNode.mFile.isDirectory()){
				for(JMenuItem jFile : ifFile)
					jFile.setEnabled(false);
				for(JMenuItem jDir : ifDir)
					jDir.setEnabled(true);

			} else {
		    	for(JMenuItem jFile : ifFile)
		    		jFile.setEnabled(true);
				for(JMenuItem jDir : ifDir)
		    		jDir.setEnabled(false);
		    }
		}


		File currentTabFile = currentTabEditor.loadedTabObj.loadedFile;
		if(currentTabFile == null || !currentTabFile.exists()) {
			for(JMenuItem jCurr : ifCurrentTabFileExists)
				jCurr.setEnabled(false);

		} else {
			for(JMenuItem jCurr : ifCurrentTabFileExists)
				jCurr.setEnabled(true);
		}

		rebuildOpenWithMenu();
		rebuildOpenInWindowMenu();
	}

	/**
	 * "Open in a window" for the selected file(s): only the tools that can read that type of file are listed. They are the windows of the
	 * Various menu (LogSearch for any file, Image Studio for the images, File Hash checker for any file).
	 */
	private void rebuildOpenInWindowMenu() {
		if (itemOpenInWindow == null) {
			return;
		}
		itemOpenInWindow.removeAll();
		final java.util.List<File> files = new java.util.ArrayList<File>();
		for (File selected : getSelectedFiles()) {
			if (selected != null && selected.isFile()) {
				files.add(selected);
			}
		}
		if (files.isEmpty() && selectedNode != null && selectedNode.mFile != null && selectedNode.mFile.isFile()) {
			files.add(selectedNode.mFile);
		}
		if (files.isEmpty()) {
			itemOpenInWindow.setEnabled(false);
			return;
		}

		Font font = GeneralConfig.DEFAULT_POP_UP_MENUS_FONT;
		final File first = files.get(0);

		boolean textFiles = allText(files);
		JMenuItem logSearch = new JMenuItem(I18n.t("common.logsearch"));
		logSearch.setFont(font);
		logSearch.addActionListener((e) -> gui.texteditor.commons.logsearch.LogSearchFrame.open(textEditor.mainApplication.getMainFrame(), first));
		if (textFiles) {
			itemOpenInWindow.add(logSearch);
		}

		gui.commons.tabs.TabImplementor image = gui.commons.tabs.TabImplementors.byId(gui.commons.imagestudio.ImageStudioTabImplementor.ID);
		if (image != null && allCanOpen(image, files)) {
			JMenuItem imageStudio = new JMenuItem(I18n.t("common.image_studio"));
			imageStudio.setFont(font);
			imageStudio.addActionListener((e) -> {
				for (File file : files) {
					gui.commons.frameutils.frame.ImageViewerFrame.loadIntoLastExistingInstance(file);
				}
			});
			itemOpenInWindow.add(imageStudio);
		}

		JMenuItem hashChecker = new JMenuItem(I18n.t("app.file_hash_checker"));
		hashChecker.setFont(font);
		hashChecker.addActionListener((e) -> textEditor.mainApplication.windowCreator.newHashChecker(first));
		itemOpenInWindow.add(hashChecker);
		itemOpenInWindow.setEnabled(true);
	}

	/**
	 * "Open with" for the selected file(s): the file editor and every integrated viewer (image studio, LogSearch, HTML...). The choice is for
	 * this opening only: the associations of Graphic preferences > File editor > Viewers do not change. The default one is marked.
	 */
	private void rebuildOpenWithMenu() {
		if (itemOpenWith == null) {
			return;
		}
		itemOpenWith.removeAll();
		final List<File> files = new java.util.ArrayList<File>();
		for (File selected : getSelectedFiles()) {
			if (selected != null && selected.isFile()) {
				files.add(selected);
			}
		}
		if (files.isEmpty() && selectedNode != null && selectedNode.mFile != null && selectedNode.mFile.isFile()) {
			files.add(selectedNode.mFile);
		}
		if (files.isEmpty()) {
			itemOpenWith.setEnabled(false);
			return;
		}

		String defaultId = gui.commons.tabs.TabImplementors.effectiveIdFor(gui.commons.tabs.TabImplementors.extensionOf(files.get(0)));
		String suffix = " " + I18n.t("app.open_with_default");
		Font font = GeneralConfig.DEFAULT_POP_UP_MENUS_FONT;

		JMenuItem asText = new JMenuItem(I18n.t("common.text_editor") + (gui.commons.tabs.TabImplementors.TEXT_ID.equals(defaultId) ? suffix : ""));
		asText.setFont(font);
		asText.addActionListener((e) -> {
			for (File file : files) {
				currentTabEditor.loadFile(file, false);
			}
		});
		if (allText(files)) {
			itemOpenWith.add(asText);
		}

		for (final gui.commons.tabs.TabImplementor implementor : gui.commons.tabs.TabImplementors.all()) {
			if (!allCanOpen(implementor, files)) {
				continue;
			}
			JMenuItem item = new JMenuItem(implementor.getDisplayName() + (implementor.getId().equals(defaultId) ? suffix : ""));
			item.setFont(font);
			item.addActionListener((e) -> {
				for (File file : files) {
					if (!currentTabEditor.goToTabByFile(file)) {
						currentTabEditor.openInViewerTab(file, implementor);
					}
				}
			});
			itemOpenWith.add(item);
		}
		if (itemOpenWith.getMenuComponentCount() == 0) {
			JMenuItem none = new JMenuItem(I18n.t("app.no_compatible_editor"));
			none.setFont(font);
			none.setEnabled(false);
			itemOpenWith.add(none);
		}
		itemOpenWith.setEnabled(true);
	}

	/** True when every file is text: only then the text editor and the text tools are offered for them */
	private static boolean allText(List<File> files) {
		for (File file : files) {
			if (!gui.commons.tabs.TabImplementors.isTextFile(file)) {
				return false;
			}
		}
		return true;
	}

	/** True when the viewer can open every one of the files */
	private static boolean allCanOpen(gui.commons.tabs.TabImplementor implementor, List<File> files) {
		for (File file : files) {
			if (!implementor.canOpen(file)) {
				return false;
			}
		}
		return true;
	}

	private JMenuItem addMenuItem(JMenu menu, String label, Font font, boolean separatorBefore, boolean separatorAfter) {
		if (separatorBefore)
			menu.add(new JSeparator());

		JMenuItem item = new JMenuItem(label);
	    item.setFont(GeneralConfig.DEFAULT_POP_UP_MENUS_FONT);
	    menu.add(item);

	    if (separatorAfter)
			menu.add(new JSeparator());

	    return item;
	}

	@Override
	public JPopupMenu getPopUpMenu() {

		super.getPopUpMenu();

	    JPopupMenu menu = new JPopupMenu();
	    Font font = GeneralConfig.DEFAULT_POP_UP_MENUS_FONT;

	    subMenuNew = new JMenu(I18n.t("app.new"));
	    subMenuNew.setFont(GeneralConfig.DEFAULT_POP_UP_MENUS_FONT);
	    menu.add(subMenuNew);
	    menu.add(new JSeparator());

	    itemNewFolder = addMenuItem(subMenuNew, I18n.t("app.add_new_folder"), font, false, false);
	    itemNewFolder.addActionListener(getNewFolderActionListener());

	    itemNewFile = addMenuItem(subMenuNew, I18n.t("app.add_new_file"), font, false, false);
	    itemNewFile.addActionListener(getNewFileActionListener());

	    itemLoadFile = addMenuItem(menu, I18n.t("common.open_file"), font, false, false);
	    itemLoadFile.addActionListener(getLoadActionListener());

	    itemOpenWith = new JMenu(I18n.t("app.open_with"));
	    itemOpenWith.setFont(font);
	    menu.add(itemOpenWith);

	    itemOpenInWindow = new JMenu(I18n.t("app.open_in_window"));
	    itemOpenInWindow.setFont(font);
	    menu.add(itemOpenInWindow);

	    itemRename = addMenuItem(menu, I18n.t("app.rename_selected_files_only"), font, false, false);
	    itemRename.addActionListener(getRenameActionListener());

	    workspaceItem = addMenuItem(menu, I18n.t("app.use_as_workspace"), font, false, false);
	    workspaceItem.addActionListener((e) -> {
	    	List<File> selected = getSelectedFiles();
	    	if(!selected.isEmpty())
	    		textEditor.mainApplication.setFolderAsWorkspace(selected.get(selected.size() - 1));
	    });


	    refreshItem = addMenuItem(menu, I18n.t("common.refresh_tree"), font, true, false);
	    refreshItem.addActionListener(refreshListener());

	    selectAllItem = addMenuItem(menu, I18n.t("app.select_all_nodes"), font, false, false);
	    selectAllItem.addActionListener(selectAllListener());

	    collapseItem = addMenuItem(menu, I18n.t("app.collapse_children_nodes"), font, false, false);
	    collapseItem.addActionListener(collapseChildren());

	    expandItem = addMenuItem(menu, I18n.t("app.expand_children_nodes"), font, false, false);
	    expandItem.addActionListener(expandChildren());

	    copyPathsItem = addMenuItem(menu, I18n.t("app.copy_selected_file_paths"), font, true, false);
	    copyPathsItem.addActionListener((e) -> {
	    	writeSelectedFilesPathIntoClipboard();
	    });

	    copyItem = addMenuItem(menu, I18n.t("common.copy"), font, false, false);
	    copyItem.addActionListener((e) -> {
	    	copySelectedFilesToClipboard();
	    });

	    pasteItem = addMenuItem(menu, I18n.t("common.paste"), font, false, false);
	    pasteItem.addActionListener((e) -> {
	    	try {
				pasteFromClipboard();
			} catch (IOException e1) {
				logger.error("Cannote paste files from clipboard", e1);
			}
	    });

	    copyHereCurrentItem = addMenuItem(menu, I18n.t("app.copy_current_file_here"), font, false, false);
	    copyHereCurrentItem.addActionListener(getCopyHereCurrentFileIfExists());

	    moveHereCurrentItem = addMenuItem(menu, I18n.t("app.move_current_file_here"), font, false, false);
	    moveHereCurrentItem.addActionListener(getMoveHereCurrentFileIfExists());

	    deleteItem = addMenuItem(menu, I18n.t("common.delete_selected_files"), font, false, false);
	    deleteItem.addActionListener(getDeleteActionListener());

	    menu.add(new JSeparator());
	    pluginsMenuItem = new JMenu(I18n.t("common.plugins"));
	    pluginsMenuItem.setFont(GeneralConfig.DEFAULT_POP_UP_MENUS_FONT);
	    menu.add(pluginsMenuItem);

	    searchContentItem = addMenuItem(menu, I18n.t("app.search_for_text_in_selected_files"), font, true, false);
	    searchContentItem.addActionListener(searchContentInSelection());

	    sysExplorerItem = addMenuItem(menu, I18n.t("common.view_in_system_explorer"), font, true, false);
	    sysExplorerItem.addActionListener(openInSystemExplorer());

	    hashingItem = addMenuItem(menu, I18n.t("app.check_file_hash"), font, false, false);
	    hashingItem.addActionListener(hashingListener());

	    propertiesItem = addMenuItem(menu, I18n.t("common.file_properties"), font, false, false);
	    propertiesItem.addActionListener(propertiesListener());

	    // DEACTIVATED, see javadoc
//	    changeBckgItem = addMenuItem(menu, I18n.t("app.change_file_panel_background"), font, false, false);
	    changeBckgItem = new JMenuItem();
	    changeBckgItem.addActionListener(ChangeBckgNewFileActionListener());

	    // set menu icons
	    MenuSetter.setIconTextGap(menu, 12);

	    sysExplorerItem.setIcon(new ImageIcon(IconsPathConfigurator.iconFolderPath));
	    itemLoadFile.setIcon(new ImageIcon(IconsPathConfigurator.ICON_LOAD));
	    copyHereCurrentItem.setIcon(new ImageIcon(IconsPathConfigurator.ICON_COPY));
	    moveHereCurrentItem.setIcon(new ImageIcon(IconsPathConfigurator.ICON_ARROW_CURVED_RIGHT));
	    deleteItem.setIcon(new ImageIcon(IconsPathConfigurator.ICON_DELETE));
	    refreshItem.setIcon(new ImageIcon(IconsPathConfigurator.ICON_RELOAD_CONF));
	    selectAllItem.setIcon(new ImageIcon(IconsPathConfigurator.ICON_CLIPBOARD_SELECTION));
	    collapseItem.setIcon(new ImageIcon(IconsPathConfigurator.ICON_LESS));
	    expandItem.setIcon(new ImageIcon(IconsPathConfigurator.ICON_MORE_SMALL));
	    hashingItem.setIcon(new ImageIcon(IconsPathConfigurator.ICON_HASHING));
	    workspaceItem.setIcon(new ImageIcon(IconsPathConfigurator.ICON_JOB_ON));
	    searchContentItem.setIcon(new ImageIcon(IconsPathConfigurator.ICON_FILE_SEARCH));
	    propertiesItem.setIcon(new ImageIcon(IconsPathConfigurator.ICON_ABOUT));
	    subMenuNew.setIcon(new ImageIcon(IconsPathConfigurator.ICON_MORE));
	    itemNewFile.setIcon(new ImageIcon(IconsPathConfigurator.ICON_FILE_LOAD));
	    itemNewFolder.setIcon(new ImageIcon(IconsPathConfigurator.ICON_CREATE_FOLDER));
	    changeBckgItem.setIcon(new ImageIcon(IconsPathConfigurator.ICON_TEXT_BG_COLOR));

	    // Add shortcuts hints
	    updateShortcutStrings();

	    return menu;
	}

	@Override
	public ActionListener refreshListener() {
		return (e) -> {
			refreshTree();
		};
	}

	private ActionListener expandChildren() {
		return (e) -> {
			GuiUtils.launchThreadSafeSwing(() -> {
				try {
					TreePath parent = tree.getSelectionPath();
					TreeUtils.expandAllChildNodes(tree, parent);
				} catch (Exception e1) {
					logger.error("Error expanding tree row N°"+selectedRow, e1);
				}
			});
		};
	}

	private ActionListener collapseChildren() {
		return (e) -> {
			try {
//					tree.collapseRow(selectedRow);
				TreeUtils.collapseTreeSelectionChildren(tree, selectedRow);
			} catch (Exception e1) {
				logger.error("Error collapsing tree row N°"+selectedRow, e1);
			}
		};
	}

	private ActionListener hashingListener() {
		return (e) -> {
			try {
				hashCheckerFrame = HashCheckerFrame.getInstance(selectedNode.getFile(), textEditor.parentFrame, textEditor.currentConfiguration.getTextEditorOpt());
//				hashCheckerFrame.updateHash();
				hashCheckerFrame.setVisible(true);
				hashCheckerFrame.setAlwaysOnTop(true);
				hashCheckerFrame.toFront();
			} catch (Exception e1) {
				logger.error("An error occurred while opening hashing functions manager frame", e1);
			}
		};
	}

	private ActionListener getLoadActionListener() {
		return (e) -> {
			currentTabEditor.loadedTabObj.textArea.setCursor(GuiUtilsExtended.CURSOR_WAIT);
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					try {
						// the node comes from the current selection of the tree: selectedNode is updated only by the mouse, so with the keyboard (arrows + Enter) it would be missing or old
						FileNamed nodeToOpen = selectedNode;
						if (tree.getSelectionCount() == 1 && tree.getSelectionPath() != null && tree.getSelectionPath().getLastPathComponent() instanceof FileNamed) {
							nodeToOpen = (FileNamed) tree.getSelectionPath().getLastPathComponent();
						}
						if (nodeToOpen != null && nodeToOpen.mFile != null && nodeToOpen.mFile.isFile() && !currentTabEditor.alreadyLoading) {
							selectedNode = nodeToOpen;
							currentTabEditor.loadFile(nodeToOpen.mFile);
							currentTabEditor.loadedTabObj.textArea.setCursor(GuiUtilsExtended.CURSOR_TEXT);
						}
					} catch (Exception e) {
						logger.error(e);
					}
				}
			});
		};
	}

	public ActionListener searchContentInSelection(){
		return (e) -> {
			textEditor.mainApplication.windowCreator.fileContentSearch(getSelectedFiles());
		};
	}

	/**
	 *  Deprecated BECAUSE BUGGY AND UGLY :(
	 * @return
	 */
	@Deprecated
	public ActionListener ChangeBckgNewFileActionListener() {
		return (e) -> {
			Color esit = new ColorDialogUtils().askForColor(textEditor.leftFileTree, false);
			if (esit != null) {
				textEditor.currentConfiguration.getTextEditorOpt().setFilePanelBackCol(esit);
				textEditor.currentConfiguration.writeSingleValue(INItializer.TEXT_EDITOR_OPT, "filePanelBackgroundCol", ConversionUtils.getRgbString(esit));
				textEditor.leftScrollPaneContainer.setBackground(esit);
				textEditor.leftFileTree.setBackground(esit);
				logger.debug("File panel Background color changed to : " + ConversionUtils.getRgbString(esit));
			}
		};
	}

	private ActionListener getMoveHereCurrentFileIfExists() {
		return (e) -> {
			if(!checkCurrentAndSelected()) {
				return;
			}

			File targetParentDir = (selectedNode.mFile.isDirectory())? selectedNode.mFile : selectedNode.mFile.getParentFile();
			if(!dialogHelper.yesOrNo("Are you sure to move current file into [" + targetParentDir.getAbsolutePath()+"] ?", I18n.t("app.move_current_file"))) {
				return;
			}

			currentTabEditor.loadedTabObj.textArea.setCursor(GuiUtilsExtended.CURSOR_WAIT);

			File outputFilePrevision = new File(targetParentDir.getAbsolutePath() + File.separator + currentTabEditor.loadedTabObj.loadedFile.getName());
			boolean overwrite = (outputFilePrevision.exists())? dialogHelper.yesOrNo(I18n.t("app.file_exists_wanna_overwrite"), I18n.t("common.file_exists")) : true;
			File result = FileWorker.copyFileIntoDir(currentTabEditor.loadedTabObj.loadedFile, targetParentDir, overwrite, GeneralConfig.TEMP_DIRECTORY);
			logger.debug("Copy of "+currentTabEditor.loadedTabObj.loadedFile+" created into target directory: " + targetParentDir.getAbsolutePath());

			if(result != null) {
				FileUtils.deleteQuietly(currentTabEditor.loadedTabObj.loadedFile);
				logger.debug("Original deleted."+currentTabEditor.loadedTabObj.loadedFile);

				currentTabEditor.loadedTabObj.loadedFile = result;
				currentTabEditor.updateSavedLabelNTab(null);

				textEditor.textEditorUtils.refreshMessageAndTabTooltip("File Moved");
			}else {
				textEditor.textEditorUtils.errorAdvice("Cannot Move!");
				dialogHelper.error(I18n.t("app.an_error_occurred_moving_file_so"), I18n.t("app.cannot_move"));
			}

			refreshItem.doClick();
			currentTabEditor.loadedTabObj.textArea.setCursor(GuiUtilsExtended.CURSOR_TEXT);
		};
	}

	private boolean checkCurrentAndSelected() {
		if(currentTabEditor == null
				|| currentTabEditor.loadedTabObj == null
				|| currentTabEditor.loadedTabObj.textArea == null
				|| currentTabEditor.loadedTabObj.loadedFile == null
				|| !currentTabEditor.loadedTabObj.loadedFile.exists()
				|| selectedNode.mFile == null
				|| !selectedNode.mFile.exists())
		{
			if (currentTabEditor.loadedTabObj.loadedFile == null
				|| !currentTabEditor.loadedTabObj.loadedFile.exists()) {
				String msg = "File does not exist";
				dialogHelper.error(msg, msg);
			}
			return false;
		} else {
			return true;
		}
	}

	private ActionListener getCopyHereCurrentFileIfExists() {
		return new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				if(!checkCurrentAndSelected()) {
					return;
				}

				currentTabEditor.loadedTabObj.textArea.setCursor(GuiUtilsExtended.CURSOR_WAIT);

				File targetParentDir = (selectedNode.mFile.isDirectory())? selectedNode.mFile : selectedNode.mFile.getParentFile();
				File outputFilePrevision = new File(targetParentDir.getAbsolutePath() + File.separator + currentTabEditor.loadedTabObj.loadedFile.getName());
				boolean overwrite = (outputFilePrevision.exists())? dialogHelper.yesOrNo(I18n.t("app.file_exists_wanna_overwrite"), I18n.t("common.file_exists")) : true;
				File result = FileWorker.copyFileIntoDir(currentTabEditor.loadedTabObj.loadedFile, targetParentDir, overwrite, GeneralConfig.TEMP_DIRECTORY);

				if(result != null) {
					textEditor.textEditorUtils.successAdvice("Copied");
				}else {
					textEditor.textEditorUtils.errorAdvice("Cannot Copy!");
					dialogHelper.error(I18n.t("app.an_error_occurred_copying_file_so"), I18n.t("app.cannot_copy"));
				}

				refreshItem.doClick();

				currentTabEditor.loadedTabObj.textArea.setCursor(GuiUtilsExtended.CURSOR_TEXT);
			}
		};
	}

	private ActionListener getRenameActionListener() {
		return (e) -> {
			try {
				TreePath[] treeSelArr = tree.getSelectionPaths();
				for(TreePath tp : treeSelArr) {

					FileNamed node = (FileNamed)tp.getLastPathComponent();
					File selectedFile = node.mFile;

					if(selectedFile != null && selectedFile.isFile()) {
						String newName = dialogHelper.askForStringNullable("Specify new name for " + selectedFile.getName(), I18n.t("app.specify_new_name"), selectedFile.getName());
						if(newName != null) {
							textEditor.textEditorUtils.renameFile(selectedFile, newName, false);
						} else {
							return;
						}
					}
				}
			} catch (IOException e1) {
				logger.error("Cannot rename current file", e1);
			}
		};
	}

	private ActionListener getNewFileActionListener() {
		return new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				currentTabEditor.loadedTabObj.textArea.setCursor(GuiUtilsExtended.CURSOR_WAIT);

				try {
					File destination = getCurrentDestinationDynamic(selectedNode);
					if(destination == null || selectedNode.mFile == null || currentTabEditor.alreadyLoading) {
						return;
					}

					String newName = dialogHelper.askForStringNullable("Plese specify a name for the file to save in "+destination.getParent(), "Create New file in " + destination.getName());
					if(newName == null) {
						return;
					}
					newName = FileVarious.ensureValidExtension(newName, textEditor.subHeaderPanel.comboFileFormat.getSelectedItem().toString());
					String separator = File.separator;
					// If file use parent, if directory use selected as root to insert into
					destination = (destination.isDirectory()) ?
							new File(destination.getAbsolutePath() + separator + newName) : new File(destination.getParentFile().getAbsolutePath() + separator + newName);
					destination = FileVarious.uniqueJavaObjFile(destination);

						// create and load new item
						if (!destination.createNewFile()) {
							String msg = "File not created";
							logger.warn(msg);
							dialogHelper.warn(msg, msg);
							return;
						}
						currentTabEditor.loadFile(destination);

					// REFRESH
					SwingUtilities.invokeLater(() -> {
						if (currentTabEditor.loadedTabObj != null) {
							textEditor.textEditorUtils.refreshTree(currentTabEditor.loadedTabObj.loadedFile, false);
						}
					});

				} catch (Exception e1) {
					String msg = "Error creating new file";
					logger.error(msg, e1);
					dialogHelper.error(msg, msg);
				} finally {
					currentTabEditor.loadedTabObj.textArea.setCursor(GuiUtilsExtended.CURSOR_TEXT);
				}
			}
		};
	}

	@Override
	public void refreshTree() {
		if (currentTabEditor.loadedTabObj != null) {
			textEditor.textEditorUtils.refreshTree(currentTabEditor.loadedTabObj.loadedFile, true);
		}else{
			FileNamed root = (FileNamed)tree.getModel().getRoot();
			textEditor.textEditorUtils.refreshTree(root.mFile, true);
		}
	}

	public MouseMotionListener getHoverOnNodesListener() {
		return new MouseMotionListener() {

			@Override
			public void mouseDragged(MouseEvent e) {
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				JTree source = (JTree) e.getSource();
				TreePath treePath = source.getPathForLocation(e.getX(), e.getY());
				int row = source.getRowForLocation(e.getX(), e.getY());
				FileNameCellRenderer renderer = (FileNameCellRenderer)source.getCellRenderer();

				if (treePath != null) {
					renderer.hoveringRow = row;
					source.setCursor(GuiUtilsExtended.CURSOR_HAND);
				} else {
					renderer.hoveringRow = -1;
					source.setCursor(GuiUtilsExtended.CURSOR_DEFAULT);
				}

				source.repaint(tree.getVisibleRect());
			}

		};
	}

	@Override
	public void keyReleased(KeyEvent event) {
		// Enter on a file of the tree opens it in a tab: the focus must then come back to the tree, on the same node
		if (tree.hasFocus() && !event.isAltDown() && !ShortcutKeys.isCtrlDown(event) && !event.isShiftDown()
				&& event.getKeyCode() == KeyEvent.VK_ENTER && tree.getSelectionCount() == 1) {
			openSelectedFromKeyboard();
			return;
		}
		super.keyReleased(event);
		if (tree.hasFocus()) {
			if (!ShortcutKeys.isCtrlDown(event) && event.isAltDown() && !event.isShiftDown()  && event.getKeyCode() == KeyEvent.VK_F2) {
				GuiUtils.fireActionListener(getRenameActionListener(), null);

			} else if (ShortcutKeys.isCtrlDown(event) && !event.isAltDown() && !event.isShiftDown() &&  event.getKeyCode() == KeyEvent.VK_N) {
				if(event.isShiftDown()) {
					GuiUtils.fireActionListener(getNewFolderActionListener(), null);

				} else {
					GuiUtils.fireActionListener(getNewFileActionListener(), null);

				}
			} else if (event.isAltDown() && ShortcutKeys.isCtrlDown(event) && !event.isShiftDown() && event.getKeyCode() == KeyEvent.VK_F5) {
				refreshItem.doClick();

			} else if (event.isAltDown() && ShortcutKeys.isCtrlDown(event) && !event.isShiftDown() && event.getKeyCode() == KeyEvent.VK_U) {
				hashingItem.doClick();

			} else if (!event.isAltDown() && ShortcutKeys.isCtrlDown(event) && !event.isShiftDown() && event.getKeyCode() == KeyEvent.VK_ENTER) {
				propertiesItem.doClick();
			}

		}
	}

	/**
	 * Enter on the selected file: same as the double click (text in a tab, or the integrated viewer chosen for the type: image studio,
	 * LogSearch, HTML...). It does not go through the "Open file" menu item: that one stays disabled after a right click on a folder,
	 * and the editor flag of a loading in progress must not block it.
	 */
	private void openSelectedFromKeyboard() {
		TreePath selection = tree.getSelectionPath();
		if (selection == null || !(selection.getLastPathComponent() instanceof FileNamed)) {
			return;
		}
		FileNamed node = (FileNamed) selection.getLastPathComponent();
		if (node.mFile == null || !node.mFile.isFile()) {
			return; // a folder: the tree expands or collapses it
		}
		selectedNode = node;
		try {
			currentTabEditor.loadFile(node.mFile);
		} catch (Exception e) {
			logger.error("Cannot open " + node.mFile + " from the file tree", e);
		}
		returnFocusToTree();
	}

	/** The tab that opens takes the focus after the loading: a moment later the tree gets it back, with the selected node still in view */
	private void returnFocusToTree() {
		javax.swing.Timer timer = new javax.swing.Timer(350, e -> {
			if (tree.isShowing() && !tree.hasFocus()) {
				tree.requestFocusInWindow();
				TreePath selection = tree.getSelectionPath();
				if (selection != null) {
					tree.scrollPathToVisible(selection);
				}
			}
		});
		timer.setRepeats(false);
		timer.start();
	}

	private void updateShortcutStrings() {
		itemLoadFile.setAccelerator(KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_ENTER, KeyEvent.KEY_LOCATION_UNKNOWN));
		itemNewFile.setAccelerator(KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_N, ShortcutKeys.MENU_MASK));
		itemNewFolder.setAccelerator(KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_N, ShortcutKeys.MENU_MASK | SHIFT_MASK));
		selectAllItem.setAccelerator(KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_A, ShortcutKeys.MENU_MASK));
		hashingItem.setAccelerator(KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_U, ShortcutKeys.MENU_MASK | ALT_MASK));
		copyItem.setAccelerator(KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_C, ShortcutKeys.MENU_MASK));
		pasteItem.setAccelerator(KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_V, ShortcutKeys.MENU_MASK));
		propertiesItem.setAccelerator(KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_ENTER, ShortcutKeys.MENU_MASK));
		itemRename.setAccelerator(KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_F2, ALT_MASK));
		deleteItem.setAccelerator(KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_CANCEL, KeyEvent.KEY_LOCATION_UNKNOWN));
		refreshItem.setAccelerator(KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_F5, ShortcutKeys.MENU_MASK | ALT_MASK));
	}

	public JMenuItem getItemLoadFile() {
		return itemLoadFile;
	}

	public void setItemLoadFile(JMenuItem itemLoadFile) {
		this.itemLoadFile = itemLoadFile;
	}

	public JMenuItem getItemNewFile() {
		return itemNewFile;
	}

	public void setItemNewFile(JMenuItem itemNewFile) {
		this.itemNewFile = itemNewFile;
	}

	public JMenuItem getSysExplorerItem() {
		return sysExplorerItem;
	}

	public void setSysExplorerItem(JMenuItem sysExplorerItem) {
		this.sysExplorerItem = sysExplorerItem;
	}

	public JMenuItem getRefreshItem() {
		return refreshItem;
	}

	public void setRefreshItem(JMenuItem refreshItem) {
		this.refreshItem = refreshItem;
	}

	public JMenuItem getPropertiesItem() {
		return propertiesItem;
	}

	public void setPropertiesItem(JMenuItem propertiesItem) {
		this.propertiesItem = propertiesItem;
	}

	// GETTERS AND SETTERS FOR MENU CLICKABLE


	public void updateAvailablePlugins(Set<IPlugin> plugins) {

		try {

			pluginsMenuItem.removeAll();

			if(!MainGuiGeneralAppStarter.tesseractAvailable) {
				for(IPlugin plugin : plugins) {
					if("PDFReaderPlugin".equals(plugin.getClass().getSimpleName()))
						plugins.remove(plugin);
				}
			}

			Iterator<IPlugin> iterator = plugins.iterator();
			while (iterator.hasNext()) {
				IPlugin plugin = iterator.next();
				if (plugin != null) {

					JMenu menu = ScreepTApplicationApi.getPluginsMenu(
									FunctionType.FILE_TREE_RIGHT_CLICK_MENU,
									()->{return getSelectedFiles();},
									plugin);

					if(menu != null && menu.getItemCount() > 0)
						pluginsMenuItem.add(menu);

				}
			}
		} catch (Exception e) {
			logger.error("Error refreshing last opened files", e);
		}
	};
}

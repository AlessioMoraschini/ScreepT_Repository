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
package gUI.utils.tree.texteditor;

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
import java.util.List;
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

import dialogutils.JOptionHelperExtended;
import files.FileVarious;
import files.FileWorker;
import files.om.FileNamed;
import frameutils.frame.HashCheckerFrame;
import frameutils.utils.GuiUtilsExtended;
import gUI.panels.TextEditor;
import gui.GuiUtils;
import gui.MenuSetter;
import impl.INItializer;
import mouseutils.TreeFileMouseListenerBasicFileNamed;
import resources.GeneralConfig;
import resources.IconsPathConfigurator;
import tabbededitor.CustomTabbedEditor;
import treeutils.TreeUtils;
import treeutils.om.FileNameCellRenderer;
import utility.manipulation.ConversionUtils;
import utils.dialog.ColorDialogUtils;

public class TreeFileMouseListenerTxtEditor extends TreeFileMouseListenerBasicFileNamed {
	
	static Logger logger = Logger.getLogger(TreeFileMouseListenerTxtEditor.class); 
	
	// gui elements
	private int selectedRow;
	private TextEditor textEditor;
	private CustomTabbedEditor currentTabEditor;
	
	public JMenu subMenuNew;
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
		super(tree);
		parent = textEdit;
		this.tree = tree;
		textEditor = textEdit;
		currentTabEditor = textEditor.getCurrentTabEditor();
		dialogHelper = new JOptionHelperExtended(textEditor);
	}
	
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
		
		JMenuItem[] ifFile = new JMenuItem[] {itemLoadFile, itemRename};
		JMenuItem[] ifDir = new JMenuItem[] {workspaceItem};
		JMenuItem[] ifCurrentTabFileExists = new JMenuItem[] {copyHereCurrentItem, moveHereCurrentItem};
		JMenuItem[] ifExists = new JMenuItem[] {
				copyHereCurrentItem, moveHereCurrentItem, itemRename, sysExplorerItem, searchContentItem,
				copyPathsItem, hashingItem, deleteItem, copyItem, pasteItem, itemLoadFile,
				workspaceItem, propertiesItem};
		
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

	public JPopupMenu getPopUpMenu() {
		
		super.getPopUpMenu();
		
	    JPopupMenu menu = new JPopupMenu();
	    Font font = GeneralConfig.DEFAULT_POP_UP_MENUS_FONT;
	    
	    subMenuNew = new JMenu("New...");
	    subMenuNew.setFont(GeneralConfig.DEFAULT_POP_UP_MENUS_FONT);
	    menu.add(subMenuNew);
	    menu.add(new JSeparator());

	    itemNewFolder = addMenuItem(subMenuNew, "Add new folder", font, false, false);
	    itemNewFolder.addActionListener(getNewFolderActionListener());

	    itemNewFile = addMenuItem(subMenuNew, "Add new file", font, false, false);
	    itemNewFile.addActionListener(getNewFileActionListener());
	    
	    itemLoadFile = addMenuItem(menu, "Open File", font, false, false);
	    itemLoadFile.addActionListener(getLoadActionListener());
	    
	    itemRename = addMenuItem(menu, "Rename selected (files only)", font, false, false);
	    itemRename.addActionListener(getRenameActionListener());
	    
	    workspaceItem = addMenuItem(menu, "Use as workspace", font, false, false);
	    workspaceItem.addActionListener((e) -> {
	    	List<File> selected = getSelectedFiles();
	    	if(!selected.isEmpty())
	    		textEditor.mainApplication.setFolderAsWorkspace(selected.get(selected.size() - 1));
	    });

	    
	    refreshItem = addMenuItem(menu, "Refresh tree", font, true, false);
	    refreshItem.addActionListener(refreshListener());
	    
	    selectAllItem = addMenuItem(menu, "Select all nodes", font, false, false);
	    selectAllItem.addActionListener(selectAllListener());
	   
	    collapseItem = addMenuItem(menu, "Collapse children nodes", font, false, false);
	    collapseItem.addActionListener(collapseChildren());

	    expandItem = addMenuItem(menu, "Expand children nodes", font, false, false);
	    expandItem.addActionListener(expandChildren());

	    copyPathsItem = addMenuItem(menu, "Copy selected file paths", font, true, false);
	    copyPathsItem.addActionListener((e) -> {
	    	writeSelectedFilesPathIntoClipboard();
	    });

	    copyItem = addMenuItem(menu, "Copy", font, false, false);
	    copyItem.addActionListener((e) -> {
	    	copySelectedFilesToClipboard();
	    });

	    pasteItem = addMenuItem(menu, "Paste", font, false, false);
	    pasteItem.addActionListener((e) -> {
	    	try {
				pasteFromClipboard();
			} catch (IOException e1) {
				logger.error("Cannote paste files from clipboard", e1);
			}
	    });
	    
	    copyHereCurrentItem = addMenuItem(menu, "Copy current file here", font, false, false);
	    copyHereCurrentItem.addActionListener(getCopyHereCurrentFileIfExists());

	    moveHereCurrentItem = addMenuItem(menu, "Move current file here", font, false, false);
	    moveHereCurrentItem.addActionListener(getMoveHereCurrentFileIfExists());

	    deleteItem = addMenuItem(menu, "Delete selected files", font, false, false);
	    deleteItem.addActionListener(getDeleteActionListener());
	    
	    searchContentItem = addMenuItem(menu, "Search for text in selected files content", font, true, false);
	    searchContentItem.addActionListener(searchContentInSelection());

	    sysExplorerItem = addMenuItem(menu, "View in system explorer", font, true, false);
	    sysExplorerItem.addActionListener(openInSystemExplorer());
	    
	    hashingItem = addMenuItem(menu, "Check File hash...", font, false, false);
	    hashingItem.addActionListener(hashingListener());

	    propertiesItem = addMenuItem(menu, "File Properties", font, false, false);
	    propertiesItem.addActionListener(propertiesListener());
	    
	    // DEACTIVATED, see javadoc
//	    changeBckgItem = addMenuItem(menu, "Change file Panel background", font, false, false);
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
				hashCheckerFrame.updateHash();
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
				public void run() {
					try {
						if (selectedNode != null && selectedNode.mFile != null && !currentTabEditor.alreadyLoading) {
							currentTabEditor.loadFile(selectedNode.mFile);
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
			Color esit = new ColorDialogUtils().askForColor(textEditor.leftSplitTreeFiles, false);
			if (esit != null) {
				textEditor.currentConfiguration.getTextEditorOpt().setFilePanelBackCol(esit);
				textEditor.currentConfiguration.writeSingleValue(INItializer.TEXT_EDITOR_OPT, "filePanelBackgroundCol", ConversionUtils.getRgbString(esit));
				textEditor.leftSplitContainer.setBackground(esit);
				textEditor.leftSplitTreeFiles.setBackground(esit);
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
			if(!dialogHelper.yesOrNo("Are you sure to move current file into [" + targetParentDir.getAbsolutePath()+"] ?", "Move current file?")) {
				return;
			}
			
			currentTabEditor.loadedTabObj.textArea.setCursor(GuiUtilsExtended.CURSOR_WAIT);

			File outputFilePrevision = new File(targetParentDir.getAbsolutePath() + File.separator + currentTabEditor.loadedTabObj.loadedFile.getName());
			boolean overwrite = (outputFilePrevision.exists())? dialogHelper.yesOrNo("File exists, wanna overwrite?", "File exists") : true;
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
				dialogHelper.error("An error occurred moving file, so file is still to its original position", "Cannot Move!");
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
				boolean overwrite = (outputFilePrevision.exists())? dialogHelper.yesOrNo("File exists, wanna overwrite?", "File exists") : true;
				File result = FileWorker.copyFileIntoDir(currentTabEditor.loadedTabObj.loadedFile, targetParentDir, overwrite, GeneralConfig.TEMP_DIRECTORY);

				if(result != null) {
					textEditor.textEditorUtils.successAdvice("Copied");
				}else {
					textEditor.textEditorUtils.errorAdvice("Cannot Copy!");
					dialogHelper.error("An error occurred copying file, so file is still to its original position", "Cannot Copy!");
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
						String newName = dialogHelper.askForStringNullable("Specify new name for " + selectedFile.getName(), "Specify new name", selectedFile.getName());
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
		super.keyReleased(event);
		if (tree.hasFocus()) {
			if (!event.isControlDown() && event.isAltDown() && !event.isShiftDown()  && event.getKeyCode() == KeyEvent.VK_F2) {
				GuiUtils.fireActionListener(getRenameActionListener(), null);
			
			} else if (event.isControlDown() && !event.isAltDown() && !event.isShiftDown() &&  event.getKeyCode() == KeyEvent.VK_N) {
				if(event.isShiftDown()) {
					GuiUtils.fireActionListener(getNewFolderActionListener(), null);
					
				} else {
					GuiUtils.fireActionListener(getNewFileActionListener(), null);
					
				}
			} else if (event.isAltDown() && event.isControlDown() && !event.isShiftDown() && event.getKeyCode() == KeyEvent.VK_F5) {
				refreshItem.doClick();
			
			} else if (event.isAltDown() && event.isControlDown() && !event.isShiftDown() && event.getKeyCode() == KeyEvent.VK_U) {
				hashingItem.doClick();
			
			} else if (!event.isAltDown() && event.isControlDown() && !event.isShiftDown() && event.getKeyCode() == KeyEvent.VK_ENTER) {
				propertiesItem.doClick();
			}
			
		}
	}
	
	private void updateShortcutStrings() {
		itemLoadFile.setAccelerator(KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_ENTER, KeyEvent.KEY_LOCATION_UNKNOWN));
		itemNewFile.setAccelerator(KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_N, KeyEvent.CTRL_MASK));
		itemNewFolder.setAccelerator(KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_N, KeyEvent.CTRL_MASK | SHIFT_MASK));
		selectAllItem.setAccelerator(KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_A, KeyEvent.CTRL_MASK));
		hashingItem.setAccelerator(KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_U, KeyEvent.CTRL_MASK | ALT_MASK));
		copyItem.setAccelerator(KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_C, KeyEvent.CTRL_MASK));
		pasteItem.setAccelerator(KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_V, KeyEvent.CTRL_MASK));
		propertiesItem.setAccelerator(KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_ENTER, KeyEvent.CTRL_MASK));
		itemRename.setAccelerator(KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_F2, ALT_MASK));
		deleteItem.setAccelerator(KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_CANCEL, KeyEvent.KEY_LOCATION_UNKNOWN));
		refreshItem.setAccelerator(KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_F5, KeyEvent.CTRL_MASK | ALT_MASK));
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
	
	
}

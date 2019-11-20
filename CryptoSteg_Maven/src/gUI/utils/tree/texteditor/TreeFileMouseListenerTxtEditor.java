package gUI.utils.tree.texteditor;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import amfileutils.FileStuff;
import amfileutils.om.FileNamed;
import dialogutils.JOptionHelper;
import frameutils.utils.GuiUtils;
import gUI.panels.TextEditor;
import mainobj.INItializer;
import menusettings.MenuSetter;
import mouseutils.TreeFileMouseListenerBasicFileNamed;
import resources.GeneralConfig;
import resources.IconsPathConfigurator;
import tabbededitor.CustomTabbedEditor;
import treeutils.TreeUtils;
import utils.dialog.ColorDialogUtils;
import variousutils.HandleString;

public class TreeFileMouseListenerTxtEditor extends TreeFileMouseListenerBasicFileNamed{
	
	// KeyGenPanel logger
	static Logger logger = Logger.getLogger(TreeFileMouseListenerTxtEditor.class); 
	
	// gui elements
	private JTree tree;
	private int selectedRow;
	private TextEditor textEditor;
	private CustomTabbedEditor currentTabEditor;
	
	public JMenuItem itemLoadFile;
	public JMenu subMenuNew;
		public JMenuItem itemNewFile;
		public JMenuItem itemNewFolder;
	public JMenuItem deleteItem;
	public JMenuItem copyHereCurrentItem;
	public JMenuItem moveHereCurrentItem;
	public JMenuItem sysExplorerItem;
	public JMenuItem refreshItem;
	public JMenuItem collapseItem;
	public JMenuItem expandItem;
	public JMenuItem propertiesItem;
	public JMenuItem changeBckgItem;
	
	public JOptionHelper dialogHelper;
	
	// constructor
	public TreeFileMouseListenerTxtEditor(JTree tree, TextEditor textEdit){
//		super(tree);
		this.tree = tree;
		textEditor = textEdit;
		currentTabEditor = textEditor.getCurrentTabEditor();
		dialogHelper = new JOptionHelper(textEditor);
	}
	
	public MouseListener getMouseListener() {
	    return new MouseAdapter() {

	        @Override
	        public void mousePressed(MouseEvent arg0) {
	        	
	        	selectedRow = tree.getRowForLocation(arg0.getX(), arg0.getY());
	        	// Right click
	            if(true){ 
	            	
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
	            }
	            super.mousePressed(arg0);
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
		if(selectedNode == null || selectedNode.mFile == null || !selectedNode.mFile.isFile()) {
	    	itemLoadFile.setEnabled(false);
	    }else {
	    	itemLoadFile.setEnabled(true);
	    }
	}

	public JPopupMenu getPopUpMenu() {
	    JPopupMenu menu = new JPopupMenu();
	    
	    subMenuNew = new JMenu("New...");
	    subMenuNew.setFont(GeneralConfig.DEFAULT_POP_UP_MENUS_FONT);
	    menu.add(subMenuNew);
	    
	    itemNewFolder = new JMenuItem("Add new folder");
	    itemNewFolder.setFont(GeneralConfig.DEFAULT_POP_UP_MENUS_FONT);
	    itemNewFolder.addActionListener(getNewFolderActionListener());
	    subMenuNew.add(itemNewFolder);

	    itemNewFile = new JMenuItem("Add new file");
	    itemNewFile.setFont(GeneralConfig.DEFAULT_POP_UP_MENUS_FONT);
	    itemNewFile.addActionListener(getNewFileActionListener());
	    subMenuNew.add(itemNewFile);
	    
	    itemLoadFile = new JMenuItem("Open File");
	    itemLoadFile.addActionListener(getLoadActionListener());
	    itemLoadFile.setFont(GeneralConfig.DEFAULT_POP_UP_MENUS_FONT);
	    menu.add(itemLoadFile);
	    
	    menu.add(new JSeparator());
	    
	    collapseItem = new JMenuItem("Collapse children nodes");
	    collapseItem.setFont(GeneralConfig.DEFAULT_POP_UP_MENUS_FONT);
	    collapseItem.addActionListener(collapseChildren());
	    menu.add(collapseItem);

	    expandItem = new JMenuItem("Expand children nodes");
	    expandItem.setFont(GeneralConfig.DEFAULT_POP_UP_MENUS_FONT);
	    expandItem.addActionListener(expandChildren());
	    menu.add(expandItem);

	    menu.add(new JSeparator());

	    copyHereCurrentItem = new JMenuItem("Copy current file here");
	    copyHereCurrentItem.setFont(GeneralConfig.DEFAULT_POP_UP_MENUS_FONT);
	    copyHereCurrentItem.addActionListener(getCopyHereCurrentFileIfExists());
	    menu.add(copyHereCurrentItem);

	    moveHereCurrentItem = new JMenuItem("Move current file here");
	    moveHereCurrentItem.setFont(GeneralConfig.DEFAULT_POP_UP_MENUS_FONT);
	    moveHereCurrentItem.addActionListener(getMoveHereCurrentFileIfExists());
	    menu.add(moveHereCurrentItem);

	    deleteItem = new JMenuItem("Delete selected files");
	    deleteItem.setFont(GeneralConfig.DEFAULT_POP_UP_MENUS_FONT);
	    deleteItem.addActionListener(getDeleteActionListener());
	    menu.add(deleteItem);
	    
	    menu.add(new JSeparator());
	    
	    sysExplorerItem = new JMenuItem("View in system explorer");
	    sysExplorerItem.setFont(GeneralConfig.DEFAULT_POP_UP_MENUS_FONT);
	    sysExplorerItem.addActionListener(openInSystemExplorer());
	    menu.add(sysExplorerItem);
	    
	    refreshItem = new JMenuItem("Refresh tree");
	    refreshItem.setFont(GeneralConfig.DEFAULT_POP_UP_MENUS_FONT);
	    refreshItem.addActionListener(refreshListener());
	    menu.add(refreshItem);
	    
	    propertiesItem = new JMenuItem("File Properties");
	    propertiesItem.setFont(GeneralConfig.DEFAULT_POP_UP_MENUS_FONT);
	    propertiesItem.addActionListener(propertiesListener());
	    menu.add(propertiesItem);
	    
	    changeBckgItem = new JMenuItem("Change file Panel background");
	    changeBckgItem.setFont(GeneralConfig.DEFAULT_POP_UP_MENUS_FONT);
	    changeBckgItem.addActionListener(ChangeBckgNewFileActionListener());
//	    menu.add(changeBckgItem); DEACTIVATED, see javadoc
	    
	    // set menu icons
	    MenuSetter.setIconTextGap(menu, 12);
	    
	    sysExplorerItem.setIcon(new ImageIcon(IconsPathConfigurator.iconFolderPath));
	    itemLoadFile.setIcon(new ImageIcon(IconsPathConfigurator.ICON_LOAD));
	    copyHereCurrentItem.setIcon(new ImageIcon(IconsPathConfigurator.ICON_COPY));
	    moveHereCurrentItem.setIcon(new ImageIcon(IconsPathConfigurator.ICON_ARROW_CURVED_RIGHT));
	    deleteItem.setIcon(new ImageIcon(IconsPathConfigurator.ICON_DELETE));
	    refreshItem.setIcon(new ImageIcon(IconsPathConfigurator.ICON_RELOAD_CONF));
	    collapseItem.setIcon(new ImageIcon(IconsPathConfigurator.ICON_LESS));
	    expandItem.setIcon(new ImageIcon(IconsPathConfigurator.ICON_MORE_SMALL));
	    propertiesItem.setIcon(new ImageIcon(IconsPathConfigurator.ICON_ABOUT));
	    subMenuNew.setIcon(new ImageIcon(IconsPathConfigurator.ICON_MORE));
	    itemNewFile.setIcon(new ImageIcon(IconsPathConfigurator.ICON_FILE_LOAD));
	    itemNewFolder.setIcon(new ImageIcon(IconsPathConfigurator.ICON_CREATE_FOLDER));
	    changeBckgItem.setIcon(new ImageIcon(IconsPathConfigurator.ICON_TEXT_BG_COLOR));

	    return menu;
	}
	
	@Override
	public ActionListener refreshListener() {
		return new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				refreshTree();
			}
		};
	}
	
	private ActionListener expandChildren() {
		return new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					TreePath parent = tree.getSelectionPath();
					TreeUtils.expandAllChildNodes(tree, parent);
				} catch (Exception e1) {
					logger.error("Error expanding tree row N°"+selectedRow, e1);
				}
			}
		};
	}
	
	
	private ActionListener collapseChildren() {
		return new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
//					tree.collapseRow(selectedRow);
					TreeUtils.collapseTree(tree, selectedRow);
				} catch (Exception e1) {
					logger.error("Error collapsing tree row N°"+selectedRow, e1);
				}
			}
		};
	}
	
	private ActionListener getLoadActionListener() {
		return new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				currentTabEditor.loadedTabObj.textArea.setCursor(GuiUtils.CURSOR_WAIT);
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						try {
							if (selectedNode != null && selectedNode.mFile != null && !currentTabEditor.alreadyLoading) {
								currentTabEditor.loadFile(selectedNode.mFile);
								currentTabEditor.loadedTabObj.textArea.setCursor(GuiUtils.CURSOR_TEXT);
							}
						} catch (Exception e) {
							logger.error(e);
						}
					}
				});
			}
		};
	}
	
	/**
	 *  Deprecated BECAUSE BUGGY AND UGLY :(
	 * @return
	 */
	@Deprecated
	public ActionListener ChangeBckgNewFileActionListener() {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Color esit = new ColorDialogUtils().askForColor(textEditor.leftSplitTreeFiles, false);
				if (esit != null) {
					textEditor.currentConfiguration.getTextEditorOpt().setFilePanelBackCol(esit);
					textEditor.currentConfiguration.writeSingleValue(INItializer.TEXT_EDITOR_OPT, "filePanelBackgroundCol", HandleString.getRgbString(esit));
					textEditor.leftSplitContainer.setBackground(esit);
					textEditor.leftSplitTreeFiles.setBackground(esit);
					logger.debug("File panel Background color changed to : " + HandleString.getRgbString(esit));
				}
			}
		};
	}
	
	private ActionListener getMoveHereCurrentFileIfExists() {
		return new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(!checkCurrentAndSelected()) {
					return;
				}
				
				File targetParentDir = (selectedNode.mFile.isDirectory())? selectedNode.mFile : selectedNode.mFile.getParentFile();
				if(!dialogHelper.yesOrNo("Are you sure to move current file into [" + targetParentDir.getAbsolutePath()+"] ?", "Move current file?")) {
					return;
				}
				
				currentTabEditor.loadedTabObj.textArea.setCursor(GuiUtils.CURSOR_WAIT);

				File outputFilePrevision = new File(targetParentDir.getAbsolutePath() + File.separator + currentTabEditor.loadedTabObj.loadedFile.getName());
				boolean overwrite = (outputFilePrevision.exists())? dialogHelper.yesOrNo("File exists, wanna overwrite?", "File exists") : true;
				File result = FileStuff.copyFileIntoDir(currentTabEditor.loadedTabObj.loadedFile, targetParentDir, overwrite);
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
				currentTabEditor.loadedTabObj.textArea.setCursor(GuiUtils.CURSOR_TEXT);
			}
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
				
				currentTabEditor.loadedTabObj.textArea.setCursor(GuiUtils.CURSOR_WAIT);
				
				File targetParentDir = (selectedNode.mFile.isDirectory())? selectedNode.mFile : selectedNode.mFile.getParentFile();
				File outputFilePrevision = new File(targetParentDir.getAbsolutePath() + File.separator + currentTabEditor.loadedTabObj.loadedFile.getName());
				boolean overwrite = (outputFilePrevision.exists())? dialogHelper.yesOrNo("File exists, wanna overwrite?", "File exists") : true;
				File result = FileStuff.copyFileIntoDir(currentTabEditor.loadedTabObj.loadedFile, targetParentDir, overwrite);

				if(result != null) {
					textEditor.textEditorUtils.successAdvice("Copied");
				}else {
					textEditor.textEditorUtils.errorAdvice("Cannot Copy!");
					dialogHelper.error("An error occurred copying file, so file is still to its original position", "Cannot Copy!");
				}
				
				refreshItem.doClick();
				
				currentTabEditor.loadedTabObj.textArea.setCursor(GuiUtils.CURSOR_TEXT);
			}
		};
	}
	
	private ActionListener getNewFileActionListener() {
		return new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				currentTabEditor.loadedTabObj.textArea.setCursor(GuiUtils.CURSOR_WAIT);
				File selected = new File(""); 
				if(selectedNode != null && selectedNode.mFile != null && !currentTabEditor.alreadyLoading) {
					selected = selectedNode.mFile;
				}else if(currentTabEditor.loadedTabObj.loadedFile != null) {
					selected = currentTabEditor.loadedTabObj.loadedFile;
				}else {
					currentTabEditor.loadedTabObj.textArea.setCursor(GuiUtils.CURSOR_TEXT);
					return;
				}
				
				selected = FileStuff.renameJavaObjFile(selected);
				String newName = JOptionPane.showInputDialog("Plese specify a name for the file to save in "+selected.getParent(), selected.getName());
				if(newName == null) {
					currentTabEditor.loadedTabObj.textArea.setCursor(GuiUtils.CURSOR_TEXT);
					return;
				}
				newName = (!newName.equals(""))? newName : "NewFile.txt";
				selected = new File(selected.getParentFile().getAbsolutePath() + "/" + newName);
				selected = FileStuff.renameJavaObjFile(selected);
				
				try {
					// create, load, and refresh tree
					if (!selected.createNewFile()) {
						String msg = "File not created";
						logger.warn(msg);
						JOptionPane.showMessageDialog(null, msg, msg, JOptionPane.WARNING_MESSAGE);
						currentTabEditor.loadedTabObj.textArea.setCursor(GuiUtils.CURSOR_TEXT);
						return;
					}
					currentTabEditor.loadFile(selected);
					if (currentTabEditor.loadedTabObj != null) {
						textEditor.textEditorUtils.refreshTree(currentTabEditor.loadedTabObj.loadedFile, false);
					}
				} catch (IOException e1) {
					String msg = "Error creating new file";
					logger.error(msg, e1);
					JOptionPane.showMessageDialog(null, msg, msg, JOptionPane.ERROR_MESSAGE);
				}
				
				currentTabEditor.loadedTabObj.textArea.setCursor(GuiUtils.CURSOR_TEXT);
						
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

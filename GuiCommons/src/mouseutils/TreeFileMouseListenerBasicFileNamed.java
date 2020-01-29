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
package mouseutils;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import dialogutils.JOptionHelperExtended;
import files.FileWorker;
import files.FileVarious;
import files.om.FileNamed;
import frameutils.frame.FilePropertiesFrame;
import frameutils.frame.HashCheckerFrame;
import frameutils.utils.GuiUtilsExtended;
import gui.GuiUtils;
import gui.MenuSetter;
import resources.GeneralConfig;
import resources.IconsPathConfigurator;
import utility.SystemClipboardUtils;
import utility.string.StringWorker;

/**
 * This represents a basic extensible component, when each node is a File Object
 * @author Alessio Moraschini
 *
 */
public class TreeFileMouseListenerBasicFileNamed {
	
	// KeyGenPanel logger
	static Logger logger = Logger.getLogger(TreeFileMouseListenerBasicFileNamed.class); 
	
	// gui elements
	public JTree tree;
	public FileNamed selectedNode;
	public Vector<FileNamed> selectedNodes;
	
	public HashCheckerFrame hashCheckerFrame;
	
	public JFrame propertiesFrame = null; 
	public Component parent;
	public JOptionHelperExtended dialogHelper;
	
	public JMenuItem itemLoadFile;
	public JMenuItem deleteItem;

	public TreeFileMouseListenerBasicFileNamed() {
		selectedNodes = new Vector<>();
		dialogHelper = new JOptionHelperExtended(null);
	}
	// constructor
	public TreeFileMouseListenerBasicFileNamed(Component parent) {
		selectedNodes = new Vector<>();
		this.parent = parent;
		dialogHelper = new JOptionHelperExtended(parent);
	}
	
	public TreeFileMouseListenerBasicFileNamed(JTree treeIn) {
		selectedNodes = new Vector<>();
		tree = treeIn;
		tree.setComponentPopupMenu(getPopUpMenu());
		tree.addMouseListener(getMouseListener());
		dialogHelper = new JOptionHelperExtended(tree);
	}
	
	public MouseListener getMouseListener() {
	    return new MouseAdapter() {

	        @Override
	        public void mousePressed(MouseEvent arg0) {
	            if(arg0.getButton() == MouseEvent.BUTTON3){
	            
		            TreePath pathForLocation = tree.getPathForLocation(arg0.getPoint().x, arg0.getPoint().y);
	                TreePath[] pathsSelection = tree.getSelectionPaths();
	                updateSelectedNodes(pathsSelection);
	                tree.setSelectionPaths(pathsSelection);
	                
		            if(pathForLocation != null){
		            	selectedNode = (FileNamed) pathForLocation.getLastPathComponent();
		            } else{
		            	selectedNode = null;
		            }
		           
		            super.mousePressed(arg0);
	            }
	        }
	    };
	}
	
	public Vector<FileNamed> updateSelectedNodes(TreePath[] selectionPaths) {
		selectedNodes = new Vector<>();
		try {
			for(TreePath curr : selectionPaths) {
				selectedNodes.add((FileNamed)curr.getLastPathComponent());
			}
		} catch (Exception e) {
		}
		
		return selectedNodes;
	}

	public JPopupMenu getPopUpMenu() {
	    JPopupMenu menu = new JPopupMenu();
	    
	    JMenuItem sysExplorerItem = new JMenuItem("View in system explorer");
	    sysExplorerItem.setFont(GeneralConfig.DEFAULT_POP_UP_MENUS_FONT);
	    sysExplorerItem.addActionListener(openInSystemExplorer());
	    menu.add(sysExplorerItem);
	    
	    itemLoadFile = new JMenuItem("Open File");
	    itemLoadFile.setFont(GeneralConfig.DEFAULT_POP_UP_MENUS_FONT);
	    menu.add(itemLoadFile);
	    
	    deleteItem = new JMenuItem("Delete selected files");
	    deleteItem.setFont(GeneralConfig.DEFAULT_POP_UP_MENUS_FONT);
	    deleteItem.addActionListener(getDeleteActionListener());
	    menu.add(deleteItem);

	    JMenuItem refreshItem = new JMenuItem("Refresh tree");
	    refreshItem.setFont(GeneralConfig.DEFAULT_POP_UP_MENUS_FONT);
	    refreshItem.addActionListener(refreshListener());
	    menu.add(refreshItem);
	    
	    JMenuItem propertiesItem = new JMenuItem("File Properties");
	    propertiesItem.setFont(GeneralConfig.DEFAULT_POP_UP_MENUS_FONT);
	    propertiesItem.addActionListener(propertiesListener());
	    menu.add(propertiesItem);

	    // set menu icons
	    MenuSetter.setIconTextGap(menu, 12);
	    
	    sysExplorerItem.setIcon(new ImageIcon(IconsPathConfigurator.iconFolderPath));
	    deleteItem.setIcon(new ImageIcon(IconsPathConfigurator.ICON_DELETE));
	    refreshItem.setIcon(new ImageIcon(IconsPathConfigurator.ICON_RELOAD_CONF));
	    propertiesItem.setIcon(new ImageIcon(IconsPathConfigurator.ICON_ABOUT));
	    
	    return menu;
	}
	
	public ActionListener openInSystemExplorer() {
		return new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				GuiUtilsExtended.openInFileSystem(selectedNode.mFile);
			}
		};
	}
	
	public ActionListener getNewFolderActionListener() {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				FileNamed node = (FileNamed)tree.getSelectionPath().getLastPathComponent();
				if(node == null || node.mFile == null || !node.mFile.exists()) {
					return;
				}
				logger.info("getNewFolderActionListener() -> asking for creating new folder...");
				File targetDir = node.mFile.isDirectory()? node.mFile : node.mFile.getParentFile();
				File unexistant = new File(targetDir.getAbsolutePath() + File.separator + "new_folder");
				unexistant = FileVarious.renameJavaObjFile(unexistant);
				String newName = dialogHelper.askForStringNullable("Specify the name of the folder to create:", "Specify folder name", unexistant.getName());
				
				if(newName == null) {
					return;
				}else if(!StringWorker.trimToEmpty(newName).equals("")){
					unexistant = FileVarious.renameJavaObjFile(new File(targetDir.getAbsolutePath() + File.separator + newName));
				}
				
				boolean result = unexistant.mkdirs();
				logger.info("getNewFolderActionListener() -> " + unexistant + " creation process had result: "+result);
			}
		};
	}
	
	public List<File> getSelectedFiles() {
		 TreePath[] treeSelArr = tree.getSelectionPaths();
		 ArrayList<File> files = new ArrayList<>();
		 for(TreePath path : treeSelArr) {
			 FileNamed node = (FileNamed)path.getLastPathComponent();
			 if (node != null) {
				try {
					files.add(node.mFile.getCanonicalFile());
				} catch (IOException e) {
					logger.error("Cannot retrieve file " + node, e);
				}
			}
		 }
		 
		 return files;
	}

	public List<String> getSelectedFilesPaths() {
		TreePath[] treeSelArr = tree.getSelectionPaths();
		ArrayList<String> files = new ArrayList<>();
		for(TreePath path : treeSelArr) {
			FileNamed node = (FileNamed)path.getLastPathComponent();
			if (node != null) {
				try {
					files.add(node.mFile.getCanonicalPath());
				} catch (IOException e) {
					logger.error("Cannot retrieve file " + node, e);
				}
			}
		}
		
		return files;
	}

	public ActionListener getDeleteActionListener() {
	    return new ActionListener() {

	        @Override
	        public void actionPerformed(ActionEvent arg0) {

	            TreePath[] treeSelArr = tree.getSelectionPaths();
	            String currFilesString = (treeSelArr.length == 1)? ((FileNamed)treeSelArr[0].getLastPathComponent()).toString() : "selected files" ;
            	if(treeSelArr.length!= 0 && new JOptionHelperExtended(null).yesOrNo("Are you sure to delete "+currFilesString+" from disk(cannot be undone)? "
            			+ "All eventual selected folders will be deleted with all the file contained in them!", null)){	        		
		            for(TreePath tp : treeSelArr) {
		            	
		            	FileNamed node = (FileNamed)tp.getLastPathComponent();
		            	File selectedFile = node.mFile;
		            	if(selectedFile.isFile()) {	
		            		logger.info("getDeleteActionListener()"+node.mFile+"File -> Deleted: "+selectedFile.delete());	
		            	}else {
		            		logger.info("getDeleteActionListener()"+node.mFile + "File -> Deleting folder content recursively...");
		            		FileWorker.deleteDirContentInclRoot(node.mFile);
		            		logger.info(node.mFile + "Emptied and Deleted!");
		            	}
			        }

	            }else {
	            	String msg = "Skipped after user request!";
			    	logger.debug(msg);
			    	dialogHelper.info(msg, "Delete Skipped!");
	            }
            	
            	refreshTree();
            	logger.info("getDeleteActionListener() -> Completed!");
	        }
	    };
	}
	
	public ActionListener refreshListener() {
		return new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				refreshTree();
			}
		};
	}

	public ActionListener selectAllListener() {
		return new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					tree.addSelectionInterval(0, tree.getRowCount());
				} catch (Exception e2) {
					logger.error("An error occurred while selecting all nodes!");
				}
			}
		};
	}
	
	public ActionListener hashingListener(File initialFile) {
		return new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					if (hashCheckerFrame == null || !hashCheckerFrame.isActive()) {
						hashCheckerFrame = new HashCheckerFrame(initialFile, null, null);
						hashCheckerFrame.setVisible(true);
						hashCheckerFrame.setAlwaysOnTop(true);
					} else {
						hashCheckerFrame.toFront();
						hashCheckerFrame.setVisible(true);
						hashCheckerFrame.setAlwaysOnTop(true);
					}
				} catch (Exception e1) {
					logger.error("An error occurred while opening hashing functions manager frame", e1);
				}
			}
		};
	}
	
	public ActionListener propertiesListener() {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (propertiesFrame == null || !propertiesFrame.isVisible()) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							try {
								if (selectedNode != null && selectedNode.mFile != null && selectedNode.mFile.exists()) {
								propertiesFrame = new FilePropertiesFrame(selectedNode.mFile, true);
								propertiesFrame.setLocationRelativeTo(parent);
								propertiesFrame.setTitle(selectedNode.mFile.getAbsolutePath());
								propertiesFrame.setVisible(true);
								}
							} catch (Exception e) {
								logger.error(e);
							}
						}
					});
				}else {
					propertiesFrame.toFront();
					propertiesFrame.setVisible(true);
				}
			}
		};
	}
	
	/**
	 * Retrieve parent if a file is selected, or current selected if file itself is a directory
	 * @return
	 */
	protected File getCurrentDestinationDynamic(FileNamed selectedNode) {
		if(selectedNode != null && selectedNode.mFile != null) {
			if(selectedNode.mFile.isDirectory()) {
				return selectedNode.mFile;
			} else {
				return selectedNode.mFile.getParentFile();
			}
		} else {
			return null;
		}
	}
	
	public KeyListener getKeyListener() {
		return new KeyAdapter() {
			public void keyReleased(KeyEvent event) {
				if(tree.hasFocus()) try {
					tree.setCursor(GuiUtils.CURSOR_WAIT);
					
					if (event.getKeyCode() == KeyEvent.VK_ENTER && tree.getSelectionCount() == 1) {
						if(itemLoadFile != null) {
							itemLoadFile.doClick();
						}
					} else if (event.getKeyCode() == 127 && tree.getSelectionCount() != 0) {
						if(deleteItem != null) {
							deleteItem.doClick();
						}
					} else if (event.isControlDown() && event.getKeyCode() == KeyEvent.VK_V && tree.getSelectionCount() == 1) {
						File destination = getCurrentDestinationDynamic(selectedNode);
						List<File> clipboardFiles = SystemClipboardUtils.getClipboardFiles(null);
						if(!clipboardFiles.isEmpty() && dialogHelper.yesOrNo("Paste " + clipboardFiles.size() + " files into " + destination.getName() + " ?")) {
							// TODO subfolders
							for(File file : clipboardFiles) {
								boolean copy = true;
								if(destination.listFiles((dir, name) -> {
									boolean sameType = (dir.isFile() && file.isFile()) || (dir.isDirectory() && file.isDirectory());
									return sameType && file.getName().equals(name);
								}).length > 0) {
									copy = dialogHelper.yesOrNo(destination + " already contains " + file.getName() + ", are you sure to overwrite?", "Overwrite?");
								}
								if (copy) {
									FileUtils.copyToDirectory(file, destination);
								}
							}
							
							refreshTree();
						}
					} else if(event.isControlDown() && !event.isShiftDown() && !event.isAltDown() && event.getKeyCode() == KeyEvent.VK_C && tree.getSelectionCount() != 0) {
						SystemClipboardUtils.setClipobardFiles(getSelectedFiles());
					}
				} catch (Exception e) {
					logger.error("An error occurred while processing file tree key event", e);
				} finally {
					tree.setCursor(GuiUtils.CURSOR_DEFAULT);
				}
			}
		};
	}
	
	
	// utility methods //
	
	public void refreshTree() {
		tree.setModel(tree.getModel());
		tree.revalidate();
		tree.repaint();
	}
}

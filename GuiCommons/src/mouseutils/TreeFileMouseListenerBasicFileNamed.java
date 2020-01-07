package mouseutils;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import dialogutils.JOptionHelperExtended;
import files.FileWorker;
import files.FileVarious;
import files.om.FileNamed;
import frameutils.frame.FilePropertiesFrame;
import frameutils.frame.HashCheckerFrame;
import frameutils.utils.GuiUtils;
import menusettings.MenuSetter;
import resources.GeneralConfig;
import resources.IconsPathConfigurator;
import utility.ConversionUtils;

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
	    
	    JMenuItem deleteItem = new JMenuItem("Delete selected files");
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
				GuiUtils.openInFileSystem(selectedNode.mFile);
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
				}else if(!ConversionUtils.trimToEmpty(newName).equals("")){
					unexistant = FileVarious.renameJavaObjFile(new File(targetDir.getAbsolutePath() + File.separator + newName));
				}
				
				boolean result = unexistant.mkdirs();
				logger.info("getNewFolderActionListener() -> " + unexistant + " creation process had result: "+result);
			}
		};
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
	
	private ActionListener hashingListener(File initialFile) {
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
	
	
	// utility methods //
	
	public void refreshTree() {
		tree.setModel(tree.getModel());
		tree.revalidate();
		tree.repaint();
	}
	
}

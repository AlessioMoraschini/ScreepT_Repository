package gUI.utils.tree.dragndrop;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import gUI.panels.CrypterPanel;
import gUI.utils.tree.dragndrop.om.CrypterDragFileModel;

public class TreeDragNDropListenerCrypter {
	
	// KeyGenPanel logger
	static Logger logger = Logger.getLogger(TreeDragNDropListenerCrypter.class); 
	
	// gui elements
	private JTree fileTree ;
	private DefaultMutableTreeNode selectedNode;
	private CrypterPanel mainCrypterFrame;
	
	// constructor
	public TreeDragNDropListenerCrypter(JTree tree){
		fileTree = tree;
		fileTree.setComponentPopupMenu(getPopUpMenu());
		fileTree.addMouseListener(getMouseListener());
	}
	
	private MouseListener getMouseListener() {
	    return new MouseAdapter() {

	        @Override
	        public void mousePressed(MouseEvent arg0) {
	            if(arg0.getButton() == MouseEvent.BUTTON3){
	                TreePath pathForLocation = fileTree .getPathForLocation(arg0.getPoint().x, arg0.getPoint().y);
	                if(pathForLocation != null){
	                    selectedNode = (DefaultMutableTreeNode) pathForLocation.getLastPathComponent();
	                } else{
	                    selectedNode = null;
	                }
	            }
	            super.mousePressed(arg0);
	        }
	    };
	}
	
	private MouseListener getTooltipListener() {
		return new MouseAdapter() {
			
		};
	}

	private JPopupMenu getPopUpMenu() {
	    	    
		JPopupMenu menu = new JPopupMenu();
		 
	    
	    JMenuItem item1 = new JMenuItem("Refresh");
	    item1.addActionListener(getRefreshListener());
	    menu.add(item1);

	    JMenuItem item2 = new JMenuItem("Remove selected files");
	    item2.addActionListener(getRemoveSelectedFromViewListener());
	    menu.add(item2);
	    
	    JMenuItem item3 = new JMenuItem("Remove all");
	    item3.addActionListener(getRemoveAllFromViewListener());
	    menu.add(item3);
	    
	    JMenuItem item4 = new JMenuItem("Open parent folder in File System view");
	    item4.addActionListener(getFileSystemViewListener());
	    menu.add(item4);

	    return menu;
	}
	
	// ACTION LISTENERS RELATED TO POP-UP MENU CHOICES //
	
	/**
	 * REFRESH VIEW (CHECK FILE EXISTANCE)
	 * @return Action Listener for Pop-up Menu
	 */
	private ActionListener getRefreshListener() {
	    return new ActionListener() {

	        @Override
	        public void actionPerformed(ActionEvent arg0) {
	        	for(DefaultMutableTreeNode currentSel : getAllNodes()) {
	        		if (currentSel != null && currentSel.getParent() != null) {
	        			File currentFile = ((CrypterDragFileModel)currentSel.getUserObject()).realFile;
	        			if(!currentFile.exists()) {
	        				((DefaultTreeModel)fileTree.getModel()).removeNodeFromParent(currentSel);
	        			}
	        		}
        		}
	        }
	    };
	}
	
	/**
	 * REMOVE SELECTED
	 * @return Action Listener for Pop-up Menu
	 */
	private ActionListener getRemoveSelectedFromViewListener() {
	    return new ActionListener() {

	        @Override
	        public void actionPerformed(ActionEvent arg0) {
	        	for(DefaultMutableTreeNode currentSel : getSelectedNodes()) {
	        		if (currentSel != null && currentSel.getParent() != null) {
	        			((DefaultTreeModel)fileTree.getModel()).removeNodeFromParent(currentSel);
	        		}
        		}
	        }
	    };
	}
	
	/**
	 * REMOVE ALL
	 * @return Action Listener for Pop-up Menu
	 */
	private ActionListener getRemoveAllFromViewListener() {
		return new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				for(DefaultMutableTreeNode currentScan : getAllNodes()) {
	        		if (currentScan != null && currentScan.getParent() != null) {
	        			((DefaultTreeModel)fileTree.getModel()).removeNodeFromParent(currentScan);
	        		}
        		}
			}
		};
	}
	
	/**
	 * OPEN IN SYSTEM EXPLRER (MAX N)
	 * @return Action Listener for Pop-up Menu
	 */
	private ActionListener getFileSystemViewListener() {
	    return new ActionListener() {

	        @Override
	        public void actionPerformed(ActionEvent arg0) {
	        	try {
	        		int i = 0;
	        		if (getNumFirstLevelNodes() != 0) {
						for (DefaultMutableTreeNode currentSel : getSelectedNodes()) {
							if (currentSel != null && currentSel.getParent() != null) {
								File currentFile = ((CrypterDragFileModel) currentSel.getUserObject()).realFile;
								if (currentFile.exists() && i < 5) {
									Desktop.getDesktop().open(currentFile.getParentFile());
								}
							}
							i++; // open maximum 5 windows
						} 
					}
					
				} catch (IOException e) {
					logger.error("Error opening folder in system explorer !", e);
				}	

	        }
	    };
	}
	
	// SOME USEFUL PUBLIC METHOD //
	
	public DefaultMutableTreeNode[] getSelectedNodes() {
		TreePath[] treeSelPaths = fileTree.getSelectionPaths();
		DefaultMutableTreeNode[] selectedNodesModel = new DefaultMutableTreeNode[treeSelPaths.length];
		int i = 0;
		for(TreePath currentNodePath : treeSelPaths) {
			selectedNodesModel[i] = (DefaultMutableTreeNode)currentNodePath.getLastPathComponent();
			i++;
		}
		return selectedNodesModel;
	}
	
	public DefaultMutableTreeNode[] getAllNodes() {
		TreeNode root = (TreeNode)fileTree.getModel().getRoot();
		int Nfiles = fileTree.getModel().getChildCount(root);
		DefaultMutableTreeNode[] nodeArray= new DefaultMutableTreeNode[Nfiles];
				
		for(int i=0; i<Nfiles; i++) {
			nodeArray[i] = (DefaultMutableTreeNode)fileTree.getModel().getChild(root, i);
		}
		return nodeArray;
	}
	
	public int getNumFirstLevelNodes() {
    	TreeNode root = (TreeNode)fileTree.getModel().getRoot();
		return fileTree.getModel().getChildCount(root);
    }
}

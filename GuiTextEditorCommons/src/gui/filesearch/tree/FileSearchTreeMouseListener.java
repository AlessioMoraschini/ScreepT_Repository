package gui.filesearch.tree;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import gui.filesearch.FileSearchActions;
import gui.filesearch.FileSearchResultViewer.ResultViewDTO;

public class FileSearchTreeMouseListener {

	static Logger logger = Logger.getLogger(FileSearchTreeMouseListener.class); 
	
	// gui elements
	private JTree fileTree ;
	public DefaultMutableTreeNode selectedNode;
	public FileSearchActions searchActions;
	
	public FileSearchTreeMouseListener(JTree fileTree, FileSearchActions searchActions) {
		this.fileTree = fileTree;
		this.searchActions = searchActions;
		fileTree.addMouseListener(getMouseListener());
	}
	
	private MouseListener getMouseListener() {
	    return new MouseAdapter() {

	        @Override
	        public void mousePressed(MouseEvent e) {
	        	int selRow = fileTree.getRowForLocation(e.getX(), e.getY());
		        TreePath selPath = fileTree.getPathForLocation(e.getX(), e.getY());
		        if(selRow != -1 && e.getClickCount() == 2) {
		        	if(selPath != null){
	                    selectedNode = (DefaultMutableTreeNode) selPath.getLastPathComponent();
	                    ResultViewDTO elementObj = (ResultViewDTO)selectedNode.getUserObject();
	                    if (!elementObj.root && elementObj != null) {
	                    	if(!elementObj.fileHead) {
	                    		searchActions.resultView(elementObj.result, elementObj.line);
	                    	}
	                    }
	                } else{
	                    selectedNode = null;
	                }
		        }
	            super.mousePressed(e);
	        }
	    };
	}
}

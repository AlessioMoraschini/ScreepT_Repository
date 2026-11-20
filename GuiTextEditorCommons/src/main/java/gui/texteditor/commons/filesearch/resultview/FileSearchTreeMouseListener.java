package gui.texteditor.commons.filesearch.resultview;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JTree;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import gui.texteditor.commons.filesearch.FileSearchActions;
import gui.texteditor.commons.filesearch.resultview.FileSearchResultViewer.ResultViewDTO;

public class FileSearchTreeMouseListener {

	static Logger logger = Logger.getLogger(FileSearchTreeMouseListener.class);

	// gui elements
	private JTree fileTree ;
	public ResultViewDTO selectedNode;
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
	                    selectedNode = (ResultViewDTO) selPath.getLastPathComponent();
	                    if (!selectedNode.root && !selectedNode.fileHead) {
	                    	searchActions.resultView(selectedNode.result, selectedNode.line);
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

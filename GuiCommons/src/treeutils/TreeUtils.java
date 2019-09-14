package treeutils;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JTree;
import javax.swing.text.Position;
import javax.swing.text.Position.Bias;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import splitpanel.FileNamed;

public class TreeUtils {
	
	public static TreePath getPath(TreeNode treeNode) {
		List<Object> nodes = new ArrayList<Object>();
		if (treeNode != null) {
			nodes.add(treeNode);
			treeNode = treeNode.getParent();
			while (treeNode != null) {
				nodes.add(0, treeNode);
				treeNode = treeNode.getParent();
			}
		}

		return nodes.isEmpty() ? null : new TreePath(nodes.toArray());
	}
	
	public static TreePath getTreePathFromFile(FileNamed file, int levels) {
		File[] result = new File[levels];
		for (int i = levels-1; i >= 0; i--) {
			if (file != null) {
				result[i] = file.mFile;
				file = new FileNamed(file.mFile.getParentFile());
			}else {
				break;
			}
		}
		return new TreePath(result);
	}
	
	/**
	 * Use this method to retrieve and select a node contained in the root of given tree.
	 * 
	 * This method is meant for jtree models that use only name of file as label for the node.
	 * @param fileToSelect
	 * @param root
	 * @param tree
	 */
	public static void selectFileInNamedFileTree(File fileToSelect, JTree tree, boolean checkExistance){
		
		boolean error = fileToSelect == null || tree == null || (checkExistance && !fileToSelect.exists());
		
		if(error) {
			return;
		}
		
		File root = ((FileNamed)tree.getModel().getRoot()).mFile;
		LinkedList<File> nodes = new LinkedList<File>();
		File current = new File(fileToSelect.getAbsolutePath());
		
		// find all nodes till root
		while((current != null && current.exists()) && !current.getName().equals(root.getName())){
			nodes.add(current);
			current = current.getParentFile();
		}
		nodes.add(new File(root.getAbsolutePath()));
		
		// start selection from root node
		while(nodes.size() > 0){
			File currentSelection = nodes.getLast();
			TreePath nodePath = tree.getNextMatch(currentSelection.getName(), 0, Bias.Forward);
			tree.setExpandsSelectedPaths(true);
			tree.setSelectionPath(nodePath);
			tree.expandPath(nodePath);
			nodes.removeLast();
		}		
	}
}

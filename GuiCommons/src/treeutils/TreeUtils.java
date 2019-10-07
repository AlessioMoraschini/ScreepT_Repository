package treeutils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JTree;
import javax.swing.text.Position.Bias;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import generic.LimitedConcurrentList;
import treeutils.om.FileNamed;

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
	 * @throws IOException 
	 */
	public static void selectFileInNamedFileTree(File fileToSelect, JTree tree, boolean checkExistance) throws IOException{
		
		boolean error = fileToSelect == null || tree == null || (checkExistance && !fileToSelect.exists());
		if(error) {
			return;
		}
		
		File root = ((FileNamed)tree.getModel().getRoot()).mFile.getAbsoluteFile();
		LimitedConcurrentList<File> nodes = new LimitedConcurrentList<File>(Integer.MAX_VALUE);
		File current = new File(fileToSelect.getCanonicalPath());
		
		// find all nodes till root
		while((current != null && current.exists()) && !current.getName().equals(root.getName())){
			nodes.add(current);
			current = current.getParentFile();
		}
		nodes.add(new File(root.getCanonicalPath()));
		
		// start selection from root node
		while(nodes.getList().size() > 0){
			File currentSelection = nodes.getLast();
			TreePath nodePath = tree.getNextMatch(currentSelection.getName(), 0, Bias.Forward);
			tree.setExpandsSelectedPaths(true);
			tree.setSelectionPath(nodePath);
			tree.expandPath(nodePath);
			nodes.removeLast();
		}		
	}

	public static void setTreeExpandedState(JTree tree, boolean expanded) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getModel().getRoot();
		setNodeExpandedState(tree, node, expanded);
	}

	public static void setNodeExpandedState(JTree tree, DefaultMutableTreeNode node, boolean expanded) {
		ArrayList<DefaultMutableTreeNode> list = Collections.list(node.children());
		for (DefaultMutableTreeNode treeNode : list) {
			setNodeExpandedState(tree, treeNode, expanded);
		}
		if (!expanded && node.isRoot()) {
			return;
		}
		TreePath path = new TreePath(node.getPath());
		if (expanded) {
			tree.expandPath(path);
		} else {
			tree.collapsePath(path);
		}
	}
}

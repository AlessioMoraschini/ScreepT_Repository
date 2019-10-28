package treeutils;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.JTree;
import javax.swing.text.Position.Bias;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import amfileutils.om.FileNamed;
import frameutils.utils.GuiUtils;
import generic.LimitedConcurrentList;
import treeutils.om.FileNameCellRenderer;

public class TreeUtils {
	
	public static final Color DEF_BACK_SELECTION = new Color(10, 102, 153, 40);
	public static final Color DEF_BACK_HOVER = new Color(0, 102, 153, 20);
	
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
	
	public static MouseMotionListener getHoverOnNodesListener() {
		return new MouseMotionListener() {

			@Override
			public void mouseDragged(MouseEvent e) {
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				JTree source = (JTree) e.getSource();
				TreePath tp = source.getPathForLocation(e.getX(), e.getY());
				
				if (tp != null) {
					source.setCursor(GuiUtils.CURSOR_HAND);
					((FileNameCellRenderer)source.getCellRenderer()).setHoverBackgroundEffect(true);
				} else {
					source.setCursor(GuiUtils.CURSOR_DEFAULT);
					((FileNameCellRenderer)source.getCellRenderer()).setHoverBackgroundEffect(false);
				}
			}
		};
	}
	
	public static void expandNodeChildren(JTree tree, TreePath parent) {
	    try {
			if (tree != null && parent != null) {
				TreeNode node = (TreeNode) parent.getLastPathComponent();
				if (node.getChildCount() >= 0) {
					for (Enumeration e = node.children(); e.hasMoreElements();) {
						TreeNode n = (TreeNode) e.nextElement();
						TreePath path = parent.pathByAddingChild(n);
						expandNodeChildren(tree, path);
					}
				}
				tree.expandPath(parent);
			} 
		} catch (Exception e) {
		}
	}
	
	/**
	 *  Recursively expand the given path and its child paths in the given tree
	 * @param tree
	 * @param treePath
	 */
    public static void expandAllRecursively(JTree tree, TreePath treePath)
    {
        TreeModel model = tree.getModel();
        Object lastPathComponent = treePath.getLastPathComponent();
        int childCount = model.getChildCount(lastPathComponent);
        if (childCount == 0)
        {
            return;
        }
        tree.setExpandsSelectedPaths(true);
        tree.setSelectionPath(treePath);
        tree.expandPath(treePath);
        for (int i=0; i<childCount; i++)
        {
            Object child = model.getChild(lastPathComponent, i);
            int grandChildCount = model.getChildCount(child);
            if (grandChildCount > 0)
            {
                class LocalTreePath extends TreePath
                {
                    private static final long serialVersionUID = 0;
                    public LocalTreePath(
                        TreePath parent, Object lastPathComponent)
                    {
                        super(parent, lastPathComponent);
                    }
                }
                TreePath nextTreePath = new LocalTreePath(treePath, child);
                expandAllRecursively(tree, nextTreePath);
            }
        }
    }

	public static void collapseNodeChildren(JTree tree, TreePath parent) {
		try {
			if (tree != null && parent != null) {
				TreeNode node = (TreeNode) parent.getLastPathComponent();
				if (node.getChildCount() >= 0) {
					for (Enumeration e = node.children(); e.hasMoreElements();) {
						TreeNode n = (TreeNode) e.nextElement();
						TreePath path = parent.pathByAddingChild(n);
						collapseNodeChildren(tree, path);
					}
				}
				tree.collapsePath(parent);
			} 
		} catch (Exception e) {
		}
		
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
	public static TreePath selectFileInNamedFileTree(File fileToSelect, JTree tree, boolean checkExistance) throws IOException{
		
		TreePath nodePath = null;
		boolean error = fileToSelect == null 
				|| tree == null 
				|| tree.getModel() == null 
				|| tree.getModel().getRoot() == null 
				|| (checkExistance && !fileToSelect.exists());
		
		if(error) {
			return nodePath;
		}
		
		File root = ((FileNamed)tree.getModel().getRoot()).mFile;
		if(root != null) {
			root = root.getAbsoluteFile();
		}
		
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
			nodePath = tree.getNextMatch(currentSelection.getName(), 0, Bias.Forward);
			tree.setExpandsSelectedPaths(true);
			tree.setSelectionPath(nodePath);
			tree.expandPath(nodePath);
			nodes.removeLast();
		}
		
		return nodePath;
	}

    public static boolean isDescendant(TreePath path1, TreePath path2){
        int count1 = path1.getPathCount();
        int count2 = path2.getPathCount();
        if(count1<=count2)
            return false;
        while(count1!=count2){
            path1 = path1.getParentPath();
            count1--;
        }
        return path1.equals(path2);
    }
    
    public static String[] getExpansionStates(JTree tree) {
    	if(tree == null) {return new String[0];}
    	
    	String[] rowsSelectionBack = new String[tree.getRowCount()];
    	for(int i = 0; i< tree.getRowCount(); i++) {
    		rowsSelectionBack[i] = getExpansionState(tree, i);
    	}
    	return rowsSelectionBack;
    }
    
    public static void restoreExpansionStates(JTree tree, String[] selectionBackup) {
    	if(selectionBackup == null || tree == null) {return;}
    	
    	int sourcesNum = selectionBackup.length;
    	for(int i = 0; i< sourcesNum; i++) {
    		restoreExpanstionState(tree, i, selectionBackup[i]);
    	}
    }
 
    public static String getExpansionState(JTree tree, int row){
        TreePath rowPath = tree.getPathForRow(row);
        StringBuffer buf = new StringBuffer();
        int rowCount = tree.getRowCount();
        for(int i=row; i<rowCount; i++){
            TreePath path = tree.getPathForRow(i);
            if(i==row || isDescendant(path, rowPath)){
                if(tree.isExpanded(path))
                    buf.append(","+String.valueOf(i-row));
            }else
                break;
        }
        return buf.toString();
    }
 
    public static void restoreExpanstionState(JTree tree, int row, String expansionState){
        StringTokenizer stok = new StringTokenizer(expansionState, ",");
        while(stok.hasMoreTokens()){
            int token = row + Integer.parseInt(stok.nextToken());
            tree.expandRow(token);
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

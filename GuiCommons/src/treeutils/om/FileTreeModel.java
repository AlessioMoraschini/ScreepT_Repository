package treeutils.om;

import java.io.File;
import java.util.Arrays;

import javax.swing.tree.TreeModel;

public class FileTreeModel implements TreeModel {

	    private File root;

	    public FileTreeModel(File root) {
	        this.root = root;
	    }

	    @Override
	    public void addTreeModelListener(javax.swing.event.TreeModelListener l) {
	        //do nothing
	    }

	    @Override
	    public Object getChild(Object parent, int index) {
	        File f = (File) parent;
		    File[] sons = f.listFiles();
			return sons[index];
			
	    }

	    @Override
	    public int getChildCount(Object parent) {
	        File f = (File) parent;
	        File[] sons = f.listFiles();
	        if (!f.isDirectory()) {
	            return 0;
	        } else {
		        if (sons.length!=0) {
		        	return sons.length;
		        }else return 0;  
	        }
	    }

	    @Override
	    public int getIndexOfChild(Object parent, Object child) {
	        File par = (File) parent;
	        File ch = (File) child;
	        if(par!=null) {
		        return Arrays.asList(par.listFiles()).indexOf(ch);
	        }else return 0;
	    }

	    @Override
	    public Object getRoot() {
	        return root;
	    }

	    @Override
	    public boolean isLeaf(Object node) {
	        File f = (File) node;
	        return !f.isDirectory();
	    }

	    @Override
	    public void removeTreeModelListener(javax.swing.event.TreeModelListener l) {
	        //do nothing
	    }

	    @Override
	    public void valueForPathChanged(javax.swing.tree.TreePath path, Object newValue) {
	        //do nothing
	    }

	}

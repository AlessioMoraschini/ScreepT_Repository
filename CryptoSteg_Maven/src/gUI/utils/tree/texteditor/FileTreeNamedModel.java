package gUI.utils.tree.texteditor;

import java.util.Arrays;

import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import splitpanel.FileNamed;

public class FileTreeNamedModel implements TreeModel {

	    private final FileNamed mFile;


	    public FileTreeNamedModel(final FileNamed root) {
	        mFile = root;
	    }

	    @Override
	    public void addTreeModelListener(javax.swing.event.TreeModelListener l) {
	        //do nothing
	    }

	    @Override
	    public Object getChild(Object parent, int index) {
	    	FileNamed f = (FileNamed) parent;
		    FileNamed[] sons = f.listFiles();
			return sons[index];
			
	    }

	    @Override
	    public int getChildCount(Object parent) {
	    	FileNamed f = (FileNamed) parent;
	    	FileNamed[] sons = f.listFiles();
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
	    	FileNamed par = (FileNamed) parent;
	    	FileNamed ch = (FileNamed) child;
	        if(par!=null) {
		        return Arrays.asList(par.listFiles()).indexOf(ch);
	        }else return 0;
	    }

	    @Override
	    public Object getRoot() {
	        return this.mFile;
	    }

	    @Override
	    public boolean isLeaf(Object node) {
	        FileNamed f = (FileNamed) node;
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

	    public static TreePath getPath(FileNamed file) {
	    	return new TreePath(file.getFile().getName());
	    }

	}

package treeutils;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;

import javax.swing.JTree;
import javax.swing.tree.TreePath;

public class TreeHighlighterAdapter extends MouseMotionAdapter implements MouseListener{
	TreePath lastSelected;
	TreePath originalSelection;
	
	public TreeHighlighterAdapter() {
	}
	
    public void mouseMoved(MouseEvent e)
    {
        JTree tree = (JTree)e.getSource();
        TreePath selRow = tree.getPathForLocation(e.getX(), e.getY());
        
    }

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		 JTree tree = (JTree)e.getSource();
		 TreePath treeSel = tree.getSelectionPath();
		 if(treeSel != null) {
			 originalSelection = treeSel;
		 }
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}
}

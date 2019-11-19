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
    	super.mouseMoved(e);
    }
    
    @Override
    public void mouseDragged(MouseEvent e) {
    	// TODO implement move file
    }

	@Override
	public void mouseEntered(MouseEvent e) {
		try {
			JTree tree = (JTree) e.getSource();
			TreePath treeSel = tree.getSelectionPath();
			if (treeSel != null) {
				originalSelection = treeSel;
			}
		} catch (Exception e1) {
		}
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
}

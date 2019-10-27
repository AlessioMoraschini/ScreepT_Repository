package treeutils;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;

import javax.swing.JTree;

public class TreeHighlighterAdapter extends MouseMotionAdapter implements MouseListener{
	int lastSelected;
	int originalSelected;
	
	public TreeHighlighterAdapter() {
	}
	
    public void mouseMoved(MouseEvent e)
    {
        JTree tree = (JTree)e.getSource();
        int selRow = tree.getRowForLocation(e.getX(), e.getY());
        
        if(selRow == -1){
            lastSelected = originalSelected;
        }
        
        else if(selRow != lastSelected){
            tree.setSelectionRow(selRow);
            lastSelected = selRow;
        }
    }

	@Override
	public void mouseClicked(MouseEvent e) {
		JTree tree = (JTree)e.getSource();
		int clicked = tree.getRowForLocation(e.getX(), e.getY());
		if(clicked == -1){
            lastSelected = originalSelected;
        }else if(e.getClickCount() > 1){
        	originalSelected = clicked;
        }
		lastSelected = originalSelected;
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		JTree tree = (JTree)e.getSource();
		originalSelected = tree.getRowForLocation(e.getX(), e.getY());
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
}

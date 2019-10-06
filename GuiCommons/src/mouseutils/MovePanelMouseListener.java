package mouseutils;

import java.awt.Frame;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 * Use This applied to a Frame to make it draggable even if undecorated
 * @author xx
 *
 */
public class MovePanelMouseListener implements MouseListener, MouseMotionListener {

	private Frame theFrame;
	
	public MovePanelMouseListener(Frame theFrame){
	this.theFrame = theFrame;
	
	}
	
		private Point startClick;
	
		public void mouseDragged(MouseEvent e) {
			int deltaX = 0;
			int deltaY = 0;
			if (e != null) {
				deltaX = e.getX() - startClick.x;
				deltaY = e.getY() - startClick.y;
			}
			
			theFrame.setBounds(theFrame.getLocation().x + deltaX, theFrame.getLocation().y + deltaY, theFrame.getWidth(), theFrame.getHeight());
		}
	
		public void mousePressed(MouseEvent e) {
			startClick = e.getPoint();
		}
	
		public void mouseMoved(MouseEvent e) {
		}
	
		@Override
		public void mouseClicked(MouseEvent e) {
	
		}
	
		@Override
		public void mouseEntered(MouseEvent e) {
		}
	
		@Override
		public void mouseExited(MouseEvent e) {
		}
	
	@Override
	public void mouseReleased(MouseEvent e) {
	}
	
	public static void applyMoveListener(Frame frame) {
		MovePanelMouseListener IMove = new MovePanelMouseListener(frame);      
		frame.addMouseListener(IMove);
		frame.addMouseMotionListener(IMove);
	}

	public static void removeMoveListener(Frame frame, MovePanelMouseListener IMove) {
		frame.removeMouseListener(IMove);
		frame.removeMouseMotionListener(IMove);
	}
	
	
	public static MovePanelMouseListener applyMoveListenerR(Frame frame) {
		MovePanelMouseListener IMove = new MovePanelMouseListener(frame);      
		frame.addMouseListener(IMove);
		frame.addMouseMotionListener(IMove);
		
		return IMove;
	}
}

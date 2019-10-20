

import java.awt.KeyEventDispatcher;
import java.awt.event.KeyEvent;

public class ShortcutTester implements KeyEventDispatcher{
	
	private static char lastPressed = 'è';
	
	public ShortcutTester() {
	}
	
	@Override
	  public boolean dispatchKeyEvent(final KeyEvent e) {
	    
//////////////// LISTENER FOR KEY PRESSED EVENT - ONLY WHEN IS CORRECT TO CONTINUE FIRE EVENT WHILE PRESSISNG  //////////////////////////
		
		if (e.getID() == KeyEvent.KEY_PRESSED) {
			if (e.getKeyChar() != lastPressed) {
				System.out.println(" => PRESSED: " + "KeyCodeString: "+ e.getKeyText(e.getKeyCode()) + " - KeyChar: " + e.getKeyChar() + " - KeyCode: " + e.getKeyCode()
						+ " - ALT: " + e.isAltDown() + " - CTRL: " + e.isControlDown() + " - SHIFT: "
						+ e.isShiftDown());
			}
			lastPressed = e.getKeyChar();
			
		}
		
////////////////LISTENER FOR KEY RELEASED EVENT - WHEN NEED TO TRIGGER ONLY ONE TIME EVEN WHILE KEEPING PRESSED  //////////////////////////
		
		if (e.getID() == KeyEvent.KEY_RELEASED) {
	      
	  
	    }
	    
	// Pass the KeyEvent to the next KeyEventDispatcher in the chain
    return false;
	}
	
}

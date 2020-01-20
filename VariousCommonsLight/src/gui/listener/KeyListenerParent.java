package gui.listener;

import java.awt.event.KeyEvent;
import java.util.Vector;

import utility.SafeLogger;

public class KeyListenerParent {
	
	// TextFileWriter logger creation
	protected static SafeLogger logger = new SafeLogger(KeyListenerParent.class);
	
	protected Vector<Integer> currentPressed = new Vector<>();
	
	public KeyListenerParent() {
		currentPressed = new Vector<>();
	}

	protected boolean isShortcutKey(KeyEvent e) {
		return (e.isAltDown() || e.isControlDown() || e.isShiftDown());
	}

	protected void updateKeyCode(KeyEvent keyEvent) {
		if (keyEvent.getID() == KeyEvent.KEY_PRESSED && !currentPressed.contains(keyEvent.getKeyCode())) {
			currentPressed.add(keyEvent.getKeyCode());
		} else if (keyEvent.getID() == KeyEvent.KEY_RELEASED && currentPressed.contains(keyEvent.getKeyCode())){
			currentPressed.remove(Integer.valueOf(keyEvent.getKeyCode()));
		}
	}
	
	protected boolean isOtherPressed(int keyCode) {
		return currentPressed.contains(keyCode);
	}
	
	protected boolean isArrowLR(int keyCode) {
		return keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_KP_LEFT || keyCode == KeyEvent.VK_RIGHT || keyCode == KeyEvent.VK_KP_RIGHT;
	}

}

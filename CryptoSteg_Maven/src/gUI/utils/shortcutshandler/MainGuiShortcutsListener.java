package gUI.utils.shortcutshandler;

import java.awt.KeyEventDispatcher;
import java.awt.event.KeyEvent;

import org.apache.log4j.Logger;

import maingUI.mainGUI;

public class MainGuiShortcutsListener implements KeyEventDispatcher{
	
	// TextFileWriter logger creation
	static Logger logger = Logger.getLogger(MainGuiShortcutsListener.class);
	
	mainGUI currentGui;
	
	public MainGuiShortcutsListener(mainGUI currentMainGui) {
		logger.debug(this.getClass().getName() + " Started!");
		currentGui = currentMainGui;
	}
	
	@Override
	  public boolean dispatchKeyEvent(final KeyEvent e) {
	    
//////////////// LISTENER FOR KEY PRESSED EVENT - ONLY WHEN IS CORRECT TO CONTINUE FIRE EVENT WHILE PRESSISNG  //////////////////////////
		
		if (e.getID() == KeyEvent.KEY_PRESSED) {
			

		}
		
////////////////LISTENER FOR KEY RELEASED EVENT - WHEN NEED TO TRIGGER ONLY ONE TIME EVEN WHILE KEEPING PRESSED  //////////////////////////
		
		if (e.getID() == KeyEvent.KEY_RELEASED) {
	      
			if (isShortcutKey(e)) {
				/**
				 *  OPEN CRYPTER : ALT+SHIFT+S
				 */
				if (e.getKeyCode() == 83 && e.isAltDown() && !e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmCrypter().doClick();
				}
				/**
				 *  OPEN TEXT-EDITOR : ALT+SHIFT+T
				 */
				if (e.getKeyCode() == 84 && e.isAltDown() && !e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmTextEditor().doClick();
				}
				/**
				 *  OPEN IMAGE-CONVERTER : ALT+SHIFT+I
				 */
				if (e.getKeyCode() == 73 && e.isAltDown() && !e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmImageConverter().doClick();
				}
				/**
				 *  OPEN SYS-INFO : ALT+SHIFT+0
				 */
				if (e.getKeyCode() == 48 && e.isAltDown() && !e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmhwInfo().doClick();
				}
				/**
				 *  OPEN NET-INFO : ALT+SHIFT+9
				 */
				if (e.getKeyCode() == 57 && e.isAltDown() && !e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmnetInfo().doClick();
				}
				/**
				 *  OPEN ABOUT INFO : ALT+SHIFT+.
				 */
				if (e.getKeyCode() == 46 && e.isAltDown() && !e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmAboutScreept().doClick();
				}
				/**
				 *  OPEN CONFIG-MANAGER : ALT+SHIFT+1
				 */
				if (e.getKeyCode() == 49 && e.isAltDown() && !e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmInspectCurrentConfiguration().doClick();
				}
				/**
				 *  OPEN GENERAL OPTIONS : ALT+SHIFT+2
				 */
				if (e.getKeyCode() == 50 && e.isAltDown() && !e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmGenOpt().doClick();
				}
				/**
				 *  OPEN GUI OPTIONS : ALT+SHIFT+3
				 */
				if (e.getKeyCode() == 51 && e.isAltDown() && !e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmCustomizeUIView().doClick();
				}
				/**
				 *  OPEN KEY-GEN : ALT+SHIFT+K
				 */
				if (e.getKeyCode() == 75 && e.isAltDown() && !e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmgenerateKeyImageSub().doClick();
				}
				/**
				 *  OPEN MANUAL : ALT+SHIFT+?
				 */
				if (e.getKeyCode() == 222 && e.isAltDown() && !e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmUserDocumentation().doClick();
				}
				/**
				 *  RETURN TO HOME PAGE : ALT+SHIFT+H
				 */
				if (e.getKeyCode() == 72 && e.isAltDown() && !e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmReturnToWelcome().doClick();
				} 
			}
		}
	    
	// Pass the KeyEvent to the next KeyEventDispatcher in the chain
    return false;
	}
	
	private boolean isShortcutKey(KeyEvent e) {
		return (e.isAltDown() || e.isControlDown() || e.isShiftDown());
	}
}

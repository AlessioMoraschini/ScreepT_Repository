package gUI.utils.shortcutshandler;

import java.awt.KeyEventDispatcher;
import java.awt.event.KeyEvent;

import org.apache.log4j.Logger;

import gUI.panels.CrypterPanel;
import maingUI.MainGUI;

public class CrypterPanelShortcutsListener implements KeyEventDispatcher{
	
	static Logger logger = Logger.getLogger(CrypterPanelShortcutsListener.class);
	
	CrypterPanel currentGui;
	
	public CrypterPanelShortcutsListener(CrypterPanel currentCrypter) {
		logger.debug(this.getClass().getName() + " Started!");
		currentGui = currentCrypter;
	}
	
	@Override
	  public boolean dispatchKeyEvent(final KeyEvent e) {
	    
//////////////// LISTENER FOR KEY PRESSED EVENT - ONLY WHEN IS CORRECT TO CONTINUE FIRE EVENT WHILE PRESSISNG  //////////////////////////
		
		if (e.getID() == KeyEvent.KEY_PRESSED && MainGUI.mainGuiUtils.isCrypterActive()) {
			
			

		}
		
////////////////LISTENER FOR KEY RELEASED EVENT - WHEN NEED TO TRIGGER ONLY ONE TIME EVEN WHILE KEEPING PRESSED  //////////////////////////
		
		if (e.getID() == KeyEvent.KEY_RELEASED && MainGUI.mainGuiUtils.isCrypterActive()) {
	      
			// HERE SHORTCUTS KEY WITH CTR|ALT|SHIFT
			if (isShortcutKey(e)) {
				
				/*
				 *  Creept FILE -> CTRL+S
				 */
				if (e.getKeyCode() == 83 && !e.isAltDown() && e.isControlDown()	&& !e.isShiftDown()) {
					currentGui.btnCreept.doClick();
				}
				
				/*
				 *  Decreept FILE -> CTRL+D
				 */
				if (e.getKeyCode() == 68 && !e.isAltDown() && e.isControlDown()	&& !e.isShiftDown()) {
					currentGui.btnDeCreept.doClick();
				}

				/*
				 *  LOAD FILE -> CTRL+L
				 */
				if (e.getKeyCode() == 76 && !e.isAltDown() && e.isControlDown()	&& !e.isShiftDown()) {
					currentGui.btnAddSourceFile.doClick();
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

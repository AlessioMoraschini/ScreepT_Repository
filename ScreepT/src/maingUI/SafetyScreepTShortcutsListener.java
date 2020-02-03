/**
 *
 * =========================================================================================
 *  Copyright (C) 2019-2020
 *
 *  AM-Design-Development - (Alessio Moraschini) - All Rights Reserved
 * =========================================================================================
 *
 * You should have received a copy of the license with this file.
 * If not, please write to: info@am-design-development.com, or visit : https://www.am-design-development.com
 */
package maingUI;

import java.awt.KeyEventDispatcher;
import java.awt.event.KeyEvent;

import org.apache.log4j.Logger;

import dialogutils.JOptionHelperExtended;
import gui.listener.KeyListenerParent;
import maingUI.MainGUI;

public class SafetyScreepTShortcutsListener extends KeyListenerParent implements KeyEventDispatcher{
	
	static Logger logger = Logger.getLogger(SafetyScreepTShortcutsListener.class);
	
	private static final String killAdvice = "Really want to kill application and loose your unsaved changes (use this only if application is not responding)?";
	
	public SafetyScreepTShortcutsListener() {
		logger.debug(this.getClass().getName() + " Started!");
	}
	
	@Override
	public boolean dispatchKeyEvent(final KeyEvent e) {
		
//////////////// LISTENER FOR KEY PRESSED EVENT - ONLY WHEN IS CORRECT TO CONTINUE FIRE EVENT WHILE PRESSISNG  //////////////////////////
		
		if (e.getID() == KeyEvent.KEY_PRESSED) {
		}
		
////////////////LISTENER FOR KEY RELEASED EVENT - WHEN NEED TO TRIGGER ONLY ONE TIME EVEN WHILE KEEPING PRESSED  //////////////////////////
		
		if (e.getID() == KeyEvent.KEY_RELEASED) {
			
			super.dispatchKeyEvent(e);
			
			// HERE SHORTCUTS KEY WITH CTR|ALT|SHIFT
			if (isShortcutKey(e) || e.getKeyCode() == 27) {
				
				/*
				 *  Safe app shutdown -> alt + f4 X 3 times (must be in a row and with max interval between)
				 */
				if (e.getKeyCode() == KeyEvent.VK_F4 && repeatedClicksInLimitedTime(e, 4, false, true, false, MAX_INTERVAL_MS)) {
					if (askKill()) {

						logger.info(this.getClass().getName() + " emergency kill procedure started.");
			    		MainGuiManager.killApplication(0);
			    	}
				}

				/*
				 *  Safe app shutdown -> esc X 3 times (must be in a row and with max interval between)
				 */
				if (e.getKeyCode() == 27 && repeatedClicksInLimitedTime(e, 4, false, false, false, MAX_INTERVAL_MS)) {
					if (askKill()) {
						
						logger.info(this.getClass().getName() + " emergency kill procedure started.");
						MainGuiManager.killApplication(0);
					}
				}

				/*
				 *  Safe app restart : alt + F5
				 */
				if (e.getKeyCode() == KeyEvent.VK_F5 && e.isAltDown() && !e.isControlDown() && !e.isShiftDown()) {
					MainGuiManager.restartApplication();
				}
			}
		}
	    
		// Pass the KeyEvent to the next KeyEventDispatcher in the chain
	    return false;
	}
	
	private boolean askKill() {
		JOptionHelperExtended dialogHelper = MainGUI.mainAPP != null && MainGUI.dialogHelper != null ? MainGUI.dialogHelper : new JOptionHelperExtended(null);
		return dialogHelper.showYNDialogWarn(killAdvice, "Kill Application?", JOptionHelperExtended.YES_OR_NO);
	}
	
}

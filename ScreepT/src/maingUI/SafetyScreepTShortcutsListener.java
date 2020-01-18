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
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import dialogutils.JOptionHelperExtended;
import maingUI.MainGUI;
import om.LimitedConcurrentList;

public class SafetyScreepTShortcutsListener implements KeyEventDispatcher{
	
	static Logger logger = Logger.getLogger(SafetyScreepTShortcutsListener.class);
	
	private static final String killAdvice = "Really want to kill application and loose your unsaved changes (use this only if application is not responding)?";
	private static long MAX_INTERVAL_MS = 600;
	
	LimitedConcurrentList<ClickUpdate> lastFiveClicks = new LimitedConcurrentList<>(5);
	
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
			
			lastFiveClicks.add(new ClickUpdate(e.getKeyCode(), e.isControlDown(), e.isAltDown(), e.isShiftDown()));
	      
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
	
	private boolean isShortcutKey(KeyEvent e) {
		return (e.isAltDown() || e.isControlDown() || e.isShiftDown());
	}
	
	private boolean repeatedClicksInLimitedTime(KeyEvent e, int repeats, boolean ctrl, boolean alt, boolean shift, long maxInterval) {
		if(!lastFiveClicks.isFull()) {
			return false;
		}
		
		boolean combinationFound = false;
		
		if(e.isControlDown() == ctrl && e.isAltDown() == alt && e.isShiftDown() == shift) {
			int nLastClicks = lastFiveClicks.getSize();
			int limit = nLastClicks-1-repeats >= 0 ? nLastClicks-1-repeats : 0;
			
			ClickUpdate lastClickData = lastFiveClicks.getLast();
			long lastTime = lastClickData.timestamp;
			boolean inARowMatched = false;
			
			for(int i = nLastClicks-1; i > limit; i--) {
				inARowMatched = lastTime - lastClickData.timestamp < maxInterval;
				lastTime = lastClickData.timestamp;
				inARowMatched = inARowMatched 
						&& lastClickData.ctrlPressed.get() == ctrl
						&& lastClickData.altPressed.get() == alt
						&& lastClickData.shiftPressed.get() == shift
						&& lastClickData.keyCode == e.getKeyCode();
				
				if(!inARowMatched) {
					inARowMatched = false;
					break;
				}
				
				if (i-1 >= 0) {
					lastClickData = lastFiveClicks.getList().get(i - 1);
				}
			}
			
			combinationFound = inARowMatched;
		}
		
		if(combinationFound) {
			for (int i = 0; i < repeats; i++) {
				lastFiveClicks.removeLast();
			}
		}
		
		return combinationFound;
	}
	
	private class ClickUpdate {
		long timestamp = 0;
		int keyCode = KeyEvent.VK_UNDEFINED;
		
		AtomicBoolean ctrlPressed;
		AtomicBoolean altPressed;
		AtomicBoolean shiftPressed;
		
		private ClickUpdate(int keyCode, boolean ctrl, boolean alt, boolean shift) {
			ctrlPressed = new AtomicBoolean(); 
			altPressed = new AtomicBoolean();  
			shiftPressed = new AtomicBoolean();
			
			this.keyCode = keyCode;
			ctrlPressed.set(ctrl);
			altPressed.set(alt);
			shiftPressed.set(shift);
			timestamp = System.currentTimeMillis();
		}
	}
}

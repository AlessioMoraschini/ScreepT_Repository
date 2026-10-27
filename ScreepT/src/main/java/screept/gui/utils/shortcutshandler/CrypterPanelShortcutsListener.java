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
package screept.gui.utils.shortcutshandler;

import java.awt.KeyEventDispatcher;
import java.awt.event.KeyEvent;

import org.apache.log4j.Logger;

import maingUI.MainGUI;
import screept.gui.panels.CrypterPanel;
import screept.gui.panels.quicknotes.QuickNotesDrawer;

public class CrypterPanelShortcutsListener implements KeyEventDispatcher{

	static Logger logger = Logger.getLogger(CrypterPanelShortcutsListener.class);

	CrypterPanel currentGui;

	public CrypterPanelShortcutsListener(CrypterPanel currentCrypter) {
		logger.debug(this.getClass().getName() + " Started!");
		currentGui = currentCrypter;
	}

	@Override
	  public boolean dispatchKeyEvent(final KeyEvent e) {

		// typing in the quick notes drawer must not trigger crypter shortcuts
		if (QuickNotesDrawer.ownsFocus()) {
			return false;
		}

//////////////// LISTENER FOR KEY PRESSED EVENT - ONLY WHEN IS CORRECT TO CONTINUE FIRE EVENT WHILE PRESSISNG  //////////////////////////

		if (e.getID() == KeyEvent.KEY_PRESSED && MainGUI.mainGuiUtils.isCrypterActive(true)) {



		}

////////////////LISTENER FOR KEY RELEASED EVENT - WHEN NEED TO TRIGGER ONLY ONE TIME EVEN WHILE KEEPING PRESSED  //////////////////////////

		if (e.getID() == KeyEvent.KEY_RELEASED && MainGUI.mainGuiUtils.isCrypterActive(true)) {

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

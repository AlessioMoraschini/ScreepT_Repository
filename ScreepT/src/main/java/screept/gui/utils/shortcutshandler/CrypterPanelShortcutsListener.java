/**
 *
 * =========================================================================================
 *  Copyright (C) 2019-2020
 *
 *  AM-Design-Development - (Alessio Moraschini)
 * =========================================================================================
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package screept.gui.utils.shortcutshandler;

import various.common.light.gui.ShortcutKeys;
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
				if (e.getKeyCode() == 83 && !e.isAltDown() && ShortcutKeys.isCtrlDown(e)	&& !e.isShiftDown()) {
					currentGui.btnCreept.doClick();
				}

				/*
				 *  Decreept FILE -> CTRL+D
				 */
				if (e.getKeyCode() == 68 && !e.isAltDown() && ShortcutKeys.isCtrlDown(e)	&& !e.isShiftDown()) {
					currentGui.btnDeCreept.doClick();
				}

				/*
				 *  LOAD FILE -> CTRL+L
				 */
				if (e.getKeyCode() == 76 && !e.isAltDown() && ShortcutKeys.isCtrlDown(e)	&& !e.isShiftDown()) {
					currentGui.btnAddSourceFile.doClick();
				}

			}
		}

		// Pass the KeyEvent to the next KeyEventDispatcher in the chain
	    return false;
	}

	private boolean isShortcutKey(KeyEvent e) {
		return (e.isAltDown() || ShortcutKeys.isCtrlDown(e) || e.isShiftDown());
	}
}

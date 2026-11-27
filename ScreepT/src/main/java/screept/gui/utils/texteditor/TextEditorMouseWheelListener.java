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
package screept.gui.utils.texteditor;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.SwingUtilities;

import gui.texteditor.commons.tabbededitor.CustomTabbedEditor;
import maingUI.MainGUI;
import resources.GeneralConfig;
import screept.gui.panels.TextEditor;
import screept.gui.utils.shortcutshandler.TextEditorShortcutListener;

public class TextEditorMouseWheelListener implements MouseWheelListener{

	private TextEditor currentEditor;
	/** Panel under the pointer for the wheel being handled, null = the one that has the commands */
	private CustomTabbedEditor wheelEditor;

	private CustomTabbedEditor currentTabEditor() {
		return wheelEditor != null ? wheelEditor : currentEditor.getCurrentTabEditor();
	}

	public TextEditorMouseWheelListener(TextEditor currentEditor) {
		super();
		GeneralConfig.initLazyGeneralConfig();
		this.currentEditor = currentEditor;
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		// the wheel acts on the panel under the pointer, which is not always the one that has the commands
		wheelEditor = null;
		for (CustomTabbedEditor panel : currentEditor.tabbedEditorsList) {
			if (e.getComponent() != null && javax.swing.SwingUtilities.isDescendingFrom(e.getComponent(), panel.getTabsComponent())) {
				wheelEditor = panel;
			}
		}
		try {
			handleWheel(e);
		} finally {
			wheelEditor = null;
		}
	}

	private void handleWheel(MouseWheelEvent e) {
		if(!MainGUI.mainGuiUtils.isTextEditorActive(true))
			return;

		int rotation = e.getWheelRotation();

		if (TextEditorShortcutListener.flagCtrlDown.get() && TextEditorShortcutListener.flagShiftDown.get() && !TextEditorShortcutListener.flagAltDown.get()) {
			// CASE CTRL+SHIFT
			adjustFontSize(rotation);
		} else if (((TextEditorShortcutListener.flagAltDown.get() && TextEditorShortcutListener.flagShiftDown.get()) || TextEditorShortcutListener.flagShiftDown.get()) && ! TextEditorShortcutListener.flagCtrlDown.get()){
			// CASE ALT OR SHIFT
			adjustHorizontalScroll(e);

		} else {

			adjustScroll(e);
			syncMiniMapScroll(rotation);
		}
	}

	private void syncMiniMapScroll(int rotation) {
//		try {
//			if (currentEditor.textEditorUtils.isMiniTextAreaActive()) {
//				MapTextAreaPanel miniMap = (MapTextAreaPanel) currentEditor.textEditorUtils.getTypedIRightPanel();
//				Rectangle pos = miniMap.currentScrollPane.getViewport().getViewRect();
//				float txtSize = currentEditor.getCurrentTabEditor().getCurrentTextArea().getFont().getSize();
//				float txtMiniSize = GeneralConfig.TXT_EDITOR_MINIMAP_FONT.getSize();
//				pos.y += getVerticalScrollAmount() * (float)rotation * (txtMiniSize/txtSize) * 3;
//				miniMap.getTxtAreaMiniMap().scrollRectToVisible(pos);
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}

	private int getVerticalScrollAmount() {
		if (TextEditorShortcutListener.flagCtrlDown.get() && TextEditorShortcutListener.flagAltDown.get()
				&& !TextEditorShortcutListener.flagShiftDown.get()) {
			return GeneralConfig.ULTRA_FAST_WHEEL_SPEED;
		} else if (TextEditorShortcutListener.flagCtrlDown.get() && !TextEditorShortcutListener.flagAltDown.get()
				&& !TextEditorShortcutListener.flagShiftDown.get()) {
			return GeneralConfig.FAST_WHEEL_SPEED;
		} else {
			return GeneralConfig.NORMAL_WHEEL_SPEED;
		}
	}

	private void adjustScroll(MouseWheelEvent e) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				currentTabEditor().loadedTabObj.currentScrollPane.getVerticalScrollBar().setUnitIncrement(getVerticalScrollAmount());
				// trigger scroll event on target
				currentTabEditor().loadedTabObj.currentScrollPane.getListeners(MouseWheelListener.class)[0].mouseWheelMoved(e);
			}
		});
	}

	private void adjustHorizontalScroll(MouseWheelEvent e) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (TextEditorShortcutListener.flagAltDown.get() && TextEditorShortcutListener.flagShiftDown.get()
						&& !TextEditorShortcutListener.flagCtrlDown.get()) {
					// alt+shift-> more speed
					currentTabEditor().loadedTabObj.currentScrollPane.getHorizontalScrollBar().setUnitIncrement(GeneralConfig.FAST_WHEEL_SPEED);
				} else if (!TextEditorShortcutListener.flagAltDown.get() && TextEditorShortcutListener.flagShiftDown.get()
						&& !TextEditorShortcutListener.flagCtrlDown.get()) {
					// only shift normal
					currentTabEditor().loadedTabObj.currentScrollPane.getHorizontalScrollBar().setUnitIncrement(GeneralConfig.NORMAL_WHEEL_SPEED);
				}
				// trigger scroll event on target
				currentTabEditor().loadedTabObj.currentScrollPane.getListeners(MouseWheelListener.class)[0].mouseWheelMoved(e);
			}
		});
	}

	private void adjustFontSize(int delta) {
		if(currentTabEditor().loadedTabObj != null) {
			currentEditor.subHeaderPanel.sliderFontSize.setValue(currentEditor.subHeaderPanel.sliderFontSize.getValue() - (delta*2));
		}
	}
}

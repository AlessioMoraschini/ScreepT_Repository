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
package gUI.utils.texteditor;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.SwingUtilities;

import gUI.panels.TextEditor;
import gUI.utils.shortcutshandler.TextEditorShortcutListener;
import maingUI.MainGUI;
import resources.GeneralConfig;

import tabbededitor.CustomTabbedEditor;

public class TextEditorMouseWheelListener implements MouseWheelListener{
	
	private TextEditor currentEditor;
	private CustomTabbedEditor currentTabEditor;
	
	public TextEditorMouseWheelListener(TextEditor currentEditor) {
		super();
		GeneralConfig.initLazyGeneralConfig();
		this.currentEditor = currentEditor;
		this.currentTabEditor = currentEditor.getCurrentTabEditor();
	}
	
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		if(!MainGUI.mainGuiUtils.isTextEditorActive())
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
			public void run() {
				currentTabEditor.loadedTabObj.currentScrollPane.getVerticalScrollBar().setUnitIncrement(getVerticalScrollAmount());
				// trigger scroll event on target
				currentTabEditor.loadedTabObj.currentScrollPane.getListeners(MouseWheelListener.class)[0].mouseWheelMoved(e);
			}
		});
	}
	
	private void adjustHorizontalScroll(MouseWheelEvent e) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (TextEditorShortcutListener.flagAltDown.get() && TextEditorShortcutListener.flagShiftDown.get()
						&& !TextEditorShortcutListener.flagCtrlDown.get()) {
					// alt+shift-> more speed
					currentTabEditor.loadedTabObj.currentScrollPane.getHorizontalScrollBar().setUnitIncrement(GeneralConfig.FAST_WHEEL_SPEED);
				} else if (!TextEditorShortcutListener.flagAltDown.get() && TextEditorShortcutListener.flagShiftDown.get()
						&& !TextEditorShortcutListener.flagCtrlDown.get()) {
					// only shift normal
					currentTabEditor.loadedTabObj.currentScrollPane.getHorizontalScrollBar().setUnitIncrement(GeneralConfig.NORMAL_WHEEL_SPEED);
				}
				// trigger scroll event on target
				currentTabEditor.loadedTabObj.currentScrollPane.getListeners(MouseWheelListener.class)[0].mouseWheelMoved(e);
			}
		});
	}
	
	private void adjustFontSize(int delta) {
		if(currentTabEditor.loadedTabObj != null) {
			currentEditor.subHeaderPanel.sliderFontSize.setValue(currentEditor.subHeaderPanel.sliderFontSize.getValue() - (delta*2));
		}
	}
}

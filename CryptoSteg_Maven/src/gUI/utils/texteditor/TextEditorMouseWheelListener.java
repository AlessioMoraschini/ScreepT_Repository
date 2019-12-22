package gUI.utils.texteditor;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.SwingUtilities;

import gUI.panels.TextEditor;
import gUI.utils.shortcutshandler.TextEditorShortcutListener;
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
		int rotation = e.getWheelRotation();
		
		if (TextEditorShortcutListener.flagCtrlDown.get() && TextEditorShortcutListener.flagShiftDown.get() && !TextEditorShortcutListener.flagAltDown.get()) {
			// CASE CTRL+SHIFT
			adjustFontSize(rotation);
		}else if(((TextEditorShortcutListener.flagAltDown.get() && TextEditorShortcutListener.flagShiftDown.get()) || TextEditorShortcutListener.flagShiftDown.get()) && ! TextEditorShortcutListener.flagCtrlDown.get()){
			// CASE ALT OR SHIFT
			adjustHorizontalScroll(e);
		}else {
			// CASE CTRL (in method)
			adjustScroll(e);
			
		}
		
	}
	
	private void adjustScroll(MouseWheelEvent e) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (TextEditorShortcutListener.flagCtrlDown.get() && TextEditorShortcutListener.flagAltDown.get()
						&& !TextEditorShortcutListener.flagShiftDown.get()) {
					currentTabEditor.loadedTabObj.currentScrollPane.getVerticalScrollBar().setUnitIncrement(GeneralConfig.ULTRA_FAST_WHEEL_SPEED);
				} else if (TextEditorShortcutListener.flagCtrlDown.get() && !TextEditorShortcutListener.flagAltDown.get()
						&& !TextEditorShortcutListener.flagShiftDown.get()) {
					currentTabEditor.loadedTabObj.currentScrollPane.getVerticalScrollBar().setUnitIncrement(GeneralConfig.FAST_WHEEL_SPEED);
				} else {
					currentTabEditor.loadedTabObj.currentScrollPane.getVerticalScrollBar().setUnitIncrement(GeneralConfig.NORMAL_WHEEL_SPEED);
				}
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
			currentEditor.subHeaderPanel.sliderFontSize.setValue(currentEditor.subHeaderPanel.sliderFontSize.getValue() - (delta*4));
		}
	}
}

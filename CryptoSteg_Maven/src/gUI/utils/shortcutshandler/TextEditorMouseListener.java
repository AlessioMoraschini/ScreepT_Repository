package gUI.utils.shortcutshandler;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import gUI.panels.TextEditor;

public class TextEditorMouseListener implements MouseWheelListener{
	
	public static final int normalSpeed = 15;
	public static final int fastSpeed = 60;
	private TextEditor currentEditor;
	
	public TextEditorMouseListener(TextEditor currentEditor) {
		super();
		this.currentEditor = currentEditor;
	}
	
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		int rotation = e.getWheelRotation();
		
		// CASE CTRL+SHIFT
		if (TextEditorShortcutListener.flagCtrlPressed && TextEditorShortcutListener.flagShiftPressed) {
			adjustFontSize(rotation);
		}else {
			
			// CASE CTRL (in method)
			adjustScroll();
			// trigger scroll event on target
			TextEditor.loadedTabObj.currentScrollPane.getListeners(MouseWheelListener.class)[0].mouseWheelMoved(e);
		}
		
		
	}
	
	private void adjustScroll() {
		if (TextEditorShortcutListener.flagCtrlPressed) {
			TextEditor.loadedTabObj.currentScrollPane.getVerticalScrollBar().setUnitIncrement(fastSpeed);
		}else {
			TextEditor.loadedTabObj.currentScrollPane.getVerticalScrollBar().setUnitIncrement(normalSpeed);
		}
	}
	
	private void adjustFontSize(int delta) {
		if(TextEditor.loadedTabObj != null) {
			currentEditor.sliderFontSize.setValue(currentEditor.sliderFontSize.getValue() - (delta*3));
		}
	}
}

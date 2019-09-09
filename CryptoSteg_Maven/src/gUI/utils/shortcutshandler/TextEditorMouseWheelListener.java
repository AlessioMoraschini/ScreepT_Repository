package gUI.utils.shortcutshandler;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import gUI.panels.TextEditor;

public class TextEditorMouseWheelListener implements MouseWheelListener{
	
	public static final int normalSpeed = 15;
	public static final int fastSpeed = 100;
	public static final int ultraFastSpeed = 350;
	private TextEditor currentEditor;
	
	public TextEditorMouseWheelListener(TextEditor currentEditor) {
		super();
		this.currentEditor = currentEditor;
	}
	
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		int rotation = e.getWheelRotation();
		
		if (TextEditorShortcutListener.flagCtrlPressed && TextEditorShortcutListener.flagShiftPressed && !TextEditorShortcutListener.flagAltPressed) {
			// CASE CTRL+SHIFT
			adjustFontSize(rotation);
		}else if((TextEditorShortcutListener.flagAltPressed || TextEditorShortcutListener.flagShiftPressed) && ! TextEditorShortcutListener.flagCtrlPressed){
			// CASE ALT OR SHIFT
			adjustHorizontalScroll();
			// trigger scroll event on target
			TextEditor.loadedTabObj.currentScrollPane.getListeners(MouseWheelListener.class)[0].mouseWheelMoved(e);
		}else {
			// CASE CTRL (in method)
			adjustScroll();
			// trigger scroll event on target
			TextEditor.loadedTabObj.currentScrollPane.getListeners(MouseWheelListener.class)[0].mouseWheelMoved(e);
			
		}
		
	}
	
	private void adjustScroll() {
		if (TextEditorShortcutListener.flagCtrlPressed && TextEditorShortcutListener.flagAltPressed && !TextEditorShortcutListener.flagShiftPressed) {
			TextEditor.loadedTabObj.currentScrollPane.getVerticalScrollBar().setUnitIncrement(ultraFastSpeed);
		}else if (TextEditorShortcutListener.flagCtrlPressed) {
			TextEditor.loadedTabObj.currentScrollPane.getVerticalScrollBar().setUnitIncrement(fastSpeed);
		}else {
			TextEditor.loadedTabObj.currentScrollPane.getVerticalScrollBar().setUnitIncrement(normalSpeed);
		}
	}
	
	private void adjustHorizontalScroll() {
		if (TextEditorShortcutListener.flagAltPressed && TextEditorShortcutListener.flagShiftPressed && !TextEditorShortcutListener.flagCtrlPressed) {
			// alt+shift-> more speed
			TextEditor.loadedTabObj.currentScrollPane.getHorizontalScrollBar().setUnitIncrement(fastSpeed);
		}else if (!TextEditorShortcutListener.flagAltPressed && TextEditorShortcutListener.flagShiftPressed && !TextEditorShortcutListener.flagCtrlPressed){
			// only shift normal
			TextEditor.loadedTabObj.currentScrollPane.getHorizontalScrollBar().setUnitIncrement(normalSpeed);
		}
	}
	
	private void adjustFontSize(int delta) {
		if(TextEditor.loadedTabObj != null) {
			currentEditor.sliderFontSize.setValue(currentEditor.sliderFontSize.getValue() - (delta*3));
		}
	}
}

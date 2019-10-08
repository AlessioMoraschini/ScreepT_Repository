package gUI.utils.shortcutshandler;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import gUI.panels.TextEditor;
import tabbededitor.CustomTabbedEditor;

public class TextEditorMouseWheelListener implements MouseWheelListener{
	
	public static final int normalSpeed = 14;
	public static final int fastSpeed = 150;
	public static final int ultraFastSpeed = 300;
	private TextEditor currentEditor;
	private CustomTabbedEditor currentTabEditor;
	
	public TextEditorMouseWheelListener(TextEditor currentEditor) {
		super();
		this.currentEditor = currentEditor;
		this.currentTabEditor = currentEditor.getCurrentTabEditor();
	}
	
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		int rotation = e.getWheelRotation();
		
		if (TextEditorShortcutListener.flagCtrlPressed.get() && TextEditorShortcutListener.flagShiftPressed.get() && !TextEditorShortcutListener.flagAltPressed.get()) {
			// CASE CTRL+SHIFT
			adjustFontSize(rotation);
		}else if((TextEditorShortcutListener.flagAltPressed.get() || TextEditorShortcutListener.flagShiftPressed.get()) && ! TextEditorShortcutListener.flagCtrlPressed.get()){
			// CASE ALT OR SHIFT
			adjustHorizontalScroll();
			// trigger scroll event on target
			currentTabEditor.loadedTabObj.currentScrollPane.getListeners(MouseWheelListener.class)[0].mouseWheelMoved(e);
		}else {
			// CASE CTRL (in method)
			adjustScroll();
			// trigger scroll event on target
			currentTabEditor.loadedTabObj.currentScrollPane.getListeners(MouseWheelListener.class)[0].mouseWheelMoved(e);
			
		}
		
	}
	
	private void adjustScroll() {
		if (TextEditorShortcutListener.flagCtrlPressed.get() && TextEditorShortcutListener.flagAltPressed.get() && !TextEditorShortcutListener.flagShiftPressed.get()) {
			currentTabEditor.loadedTabObj.currentScrollPane.getVerticalScrollBar().setUnitIncrement(ultraFastSpeed);
		}else if (TextEditorShortcutListener.flagCtrlPressed.get()) {
			currentTabEditor.loadedTabObj.currentScrollPane.getVerticalScrollBar().setUnitIncrement(fastSpeed);
		}else {
			currentTabEditor.loadedTabObj.currentScrollPane.getVerticalScrollBar().setUnitIncrement(normalSpeed);
		}
	}
	
	private void adjustHorizontalScroll() {
		if (TextEditorShortcutListener.flagAltPressed.get() && TextEditorShortcutListener.flagShiftPressed.get() && !TextEditorShortcutListener.flagCtrlPressed.get()) {
			// alt+shift-> more speed
			currentTabEditor.loadedTabObj.currentScrollPane.getHorizontalScrollBar().setUnitIncrement(fastSpeed);
		}else if (!TextEditorShortcutListener.flagAltPressed.get() && TextEditorShortcutListener.flagShiftPressed.get() && !TextEditorShortcutListener.flagCtrlPressed.get()){
			// only shift normal
			currentTabEditor.loadedTabObj.currentScrollPane.getHorizontalScrollBar().setUnitIncrement(normalSpeed);
		}
	}
	
	private void adjustFontSize(int delta) {
		if(currentTabEditor.loadedTabObj != null) {
			currentEditor.sliderFontSize.setValue(currentEditor.sliderFontSize.getValue() - (delta*4));
		}
	}
}

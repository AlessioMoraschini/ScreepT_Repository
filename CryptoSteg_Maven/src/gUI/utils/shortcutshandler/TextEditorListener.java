package gUI.utils.shortcutshandler;

import java.awt.KeyEventDispatcher;
import java.awt.event.KeyEvent;

import javax.swing.JTextArea;

import org.apache.log4j.Logger;

import gUI.panels.TextEditor;

public class TextEditorListener implements KeyEventDispatcher{
	
	// TextFileWriter logger creation
	static Logger logger = Logger.getLogger(TextEditorListener.class);
	
	TextEditor currentTextEditor;
	
	public TextEditorListener(TextEditor currentTextEdit) {
		currentTextEditor = currentTextEdit;
	}
	
	@Override
	  public boolean dispatchKeyEvent(final KeyEvent e) {
	    
//////////////// LISTENER FOR KEY PRESSED EVENT - ONLY WHEN IS CORRECT TO CONTINUE FIRE EVENT WHILE PRESSISNG  //////////////////////////
		
		if (e.getID() == KeyEvent.KEY_PRESSED && currentTextEditor.parentFrame.getContentPane().getComponent(0) instanceof TextEditor) {
			
			/**
			 *  UNDO
			 */
			// case CTRL+Z -> undo
			if (e.getKeyCode()==90 && e.isControlDown() && !e.isAltDown() && !e.isShiftDown()) {
				currentTextEditor.btnUndo.doClick();
			}
			
			//case Ctrl+shift+Z -> undo5Steps
			if (e.getKeyCode()==90 && !e.isAltDown() && e.isControlDown() && e.isShiftDown())
			{
				for(int i = 0; i<5; i++) {
					currentTextEditor.btnUndo.doClick();
				}
			}
			
			/**
			 *  REDO
			 */
			//case Ctrl+alt+Z -> redo
			if (e.getKeyCode()==90 && e.isControlDown() && e.isAltDown() && !e.isShiftDown())
			{
				currentTextEditor.btnRedo.doClick();
			}
			
			//case Ctrl+shift+Alt+z -> redo5steps
			if (e.getKeyCode()==90 && e.isAltDown() && e.isControlDown() && e.isShiftDown())
			{
				for(int i = 0; i<5; i++) {
					currentTextEditor.btnRedo.doClick();
				}
			}
			
			/**
			 *  ADJUST FONT-SIZE
			 */
			//case Ctrl+[numpad +] -> increase
			if (e.getKeyCode()==107 && e.isControlDown() && !e.isAltDown() && !e.isShiftDown())
			{
				int val = currentTextEditor.sliderFontSize.getValue();
				currentTextEditor.sliderFontSize.setValue(val+1);
			}
			
			//case Ctrl+[numpad -] -> decrease
			if (e.getKeyCode()==109 && e.isControlDown() && !e.isAltDown() && !e.isShiftDown())
			{
				int val = currentTextEditor.sliderFontSize.getValue();
				currentTextEditor.sliderFontSize.setValue(val-1);
			}
		}
		
////////////////LISTENER FOR KEY RELEASED EVENT - WHEN NEED TO TRIGGER ONLY ONE TIME EVEN WHILE KEEPING PRESSED  //////////////////////////
		
		if (e.getID() == KeyEvent.KEY_RELEASED  && currentTextEditor.parentFrame.getContentPane().getComponent(0) instanceof TextEditor) {
	      
			/**
			 *  SAVE FILE AS NEW
			 */
			//case Ctrl+shift+s -> Save file as new
			if ( !currentTextEditor.alreadySaving && e.getKeyCode()==83 && !e.isAltDown() && e.isControlDown() && e.isShiftDown())
			{
				currentTextEditor.btnSaveTextFileAs.doClick();
			}
			
			/**
			 *  SAVE FILE
			 */
			//case Ctrl+s -> Save file as new
			if ( !currentTextEditor.alreadySaving && e.getKeyCode()==83 && !e.isAltDown() && e.isControlDown() && !e.isShiftDown())
			{
				currentTextEditor.btnSaveTextFile.doClick();
			}
			
			/**
			 *  LOAD FILE
			 */
			//case Ctrl+o -> Load file
			if (!currentTextEditor.alreadyLoading && e.getKeyCode()==79 && !e.isAltDown() && e.isControlDown() && !e.isShiftDown())
			{
				currentTextEditor.btnLoadTextFile.doClick();
			}
			
			/**
			 * 	READ TEXT WITH SPEAKER / STOP PLAYNG  objSpeaker
			 */
			//case Ctrl+Space -> listen/stop playing
			if (e.getKeyCode()==32 && !e.isAltDown() && e.isControlDown() && !e.isShiftDown())
			{
				currentTextEditor.btnSpeak.doClick();
			}
			
			/**
			 * 	CLEAN TEXT
			 */
			// case ctrl+canc
			if (e.getKeyCode()==127 && !e.isAltDown() && e.isControlDown() && !e.isShiftDown())
			{
				// DELETE TEXT AREA CONTENT
				currentTextEditor.btnClear.doClick();
			}
			
			/**
			 * 	CRYPT-DECRYPT
			 */
			// case ctrl+shift+C => CRYPT
			if (e.getKeyCode()==67 && !e.isAltDown() && e.isControlDown() && e.isShiftDown())
			{
				currentTextEditor.btnCrypt.doClick();
			}
			
			// case ctrl+shift+D => DECRYPT
			if (e.getKeyCode()==68 && !e.isAltDown() && e.isControlDown() && e.isShiftDown())
			{
				currentTextEditor.btnDecrypt.doClick();
			}
			
			/**
			 * 	DARK/LIGHT VIEW
			 */
			// case ctrl+B => DARK/LIGHT
			if (e.getKeyCode()==66 && !e.isAltDown() && e.isControlDown() && !e.isShiftDown())
			{
				currentTextEditor.btnDark.doClick();
			}
			
			/**
			 * 	UPPERCASE AFTER DOTS-SEPARATOR
			 */
			// case ctrl+shift+. => UPPERCASE AFTER DOTS
			if (e.getKeyCode()==46 && !e.isAltDown() && e.isControlDown() && e.isShiftDown())
			{
				currentTextEditor.btnUpperPostDots.doClick();
			}
			
			/**
			 * 	UPPERCASE SELECTED TEXT
			 */
			// case ctrl+shift+UP => UPPERCASE SELECTED
			if (e.getKeyCode()==38 && !e.isAltDown() && e.isControlDown() && e.isShiftDown())
			{
				currentTextEditor.btnTt.doClick();
			}
			
			/**
			 * 	FIND  TEXT
			 */
			// case ctrl+f=> FIND
			if (e.getKeyCode()==70 && !e.isAltDown() && e.isControlDown() && !e.isShiftDown())
			{
				currentTextEditor.btnFindText.doClick();
			}
			
//			/**
//			 * 	SELECT TEXT
//			 */
//			// case ctrl+A => SELECT/DESELECT ALL TEXT
//			if (e.getKeyCode()==65 && !e.isAltDown() && e.isControlDown() && !e.isShiftDown())
//			{
//				JTextArea textA = currentTextEditor.textArea_1;
//				int textLength = textA.getText().length();
//				
//				if (textA.getSelectedText() == "" || textA.getSelectedText() == null) {
//					textA.requestFocus();
//					textA.setSelectionStart(0);
//					textA.setSelectionEnd(textLength);
//				}else {
//					textA.setSelectionStart(0);
//					textA.setSelectionEnd(0);
//					textA.transferFocus();
//				}
//			}
	  
	    }
	    
	// Pass the KeyEvent to the next KeyEventDispatcher in the chain
    return false;
	}
}

package gUI.utils.shortcutshandler;

import java.awt.KeyEventDispatcher;
import java.awt.event.KeyEvent;

import org.apache.log4j.Logger;

import gUI.panels.TextFileWriter;

public class TextEditorListener implements KeyEventDispatcher{
	
	// TextFileWriter logger creation
	static Logger logger = Logger.getLogger(TextEditorListener.class);
	
	TextFileWriter currentTextEditor;
	
	public TextEditorListener(TextFileWriter currentTextEdit) {
		currentTextEditor = currentTextEdit;
	}
	
	@Override
	  public boolean dispatchKeyEvent(final KeyEvent e) {
	    if (e.getID() == KeyEvent.KEY_RELEASED) {
	      
	    	logger.debug("!!! Catched key input: "+"KeyChar: "+e.getKeyChar()+" - KeyCode: "+e.getKeyCode()+" - ALT: "+e.isAltDown()+" - CTRL: "+e.isControlDown()+" - SHIFT: "+e.isShiftDown());
		
		/**
		 *  UNDO
		 */
		// case CTRL-Z -> undo
		if (e.getKeyCode()==90 && e.isControlDown() && !e.isShiftDown() && !e.isAltDown()) {
			if(currentTextEditor.manager.canUndo()) {
				currentTextEditor.manager.undo();
				
			}
		}
		
		//case Ctrl-shift-Z -> undo5Steps
		if (e.getKeyCode()==90 && !e.isAltDown() && e.isControlDown() && e.isShiftDown())
		{
			for(int i = 0; i<5; i++) {
				if(currentTextEditor.manager.canUndo()) {
					currentTextEditor.manager.undo();
				}
			}
		}
		
		/**
		 *  REDO
		 */
		//case Ctrl-alt-Z -> redo
		if (e.getKeyCode()==90 && e.isControlDown() && !e.isShiftDown() && e.isAltDown())
		{
			if(currentTextEditor.manager.canRedo()) {
				currentTextEditor.manager.redo();
			}
		}
		
		//case Ctrl-shift-Alt-z -> redo5steps
		if (e.getKeyCode()==90 && e.isAltDown() && e.isControlDown() && e.isShiftDown())
		{
			for(int i = 0; i<5; i++) {
				if (currentTextEditor.manager.canRedo()) {
					currentTextEditor.manager.redo();
				}
			}
		}
		
		/**
		 *  SAVE FILE
		 */
		//case Ctrl-s -> Save file
		if ( !currentTextEditor.alreadySaving && e.getKeyCode()==83 && !e.isAltDown() && e.isControlDown() && !e.isShiftDown())
		{
			// simulates click on save button
			currentTextEditor.btnSaveTextFile.doClick();
			
		}
		
		/**
		 *  LOAD FILE
		 */
		//case Ctrl-o -> Load file
		if (!currentTextEditor.alreadyLoading && e.getKeyCode()==79 && !e.isAltDown() && e.isControlDown() && !e.isShiftDown())
		{
			// simulates click on load button
			currentTextEditor.btnLoadTextFile.doClick();
		}
		
		/**
		 * READ TEXT WITH SPEAKER / STOP PLAYNG  objSpeaker
		 */
		//case Ctrl-Space -> listen/stop playing
		if (!currentTextEditor.alreadyLoading && e.getKeyCode()==32 && !e.isAltDown() && e.isControlDown() && !e.isShiftDown())
		{
			// simulates click on load button
			currentTextEditor.btnSpeak.doClick();
		}
		
		/**
		 * CLEAN TEXT
		 */
		// case ctrl-canc
		if (!currentTextEditor.alreadyLoading && e.getKeyCode()==127 && !e.isAltDown() && e.isControlDown() && !e.isShiftDown())
		{
			// DELETE TEXT AREA CONTENT
			currentTextEditor.textArea_1.setText("");
		}
	  
	}
	// Pass the KeyEvent to the next KeyEventDispatcher in the chain
	    return false;
	  }
}

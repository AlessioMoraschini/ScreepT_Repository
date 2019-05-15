package gUI.utils.shortcutshandler;

import java.awt.KeyEventDispatcher;
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import org.apache.log4j.Logger;

import gUI.panels.TextEditor;
import resources.IconsPathConfigurator;

public class TextEditorListener implements KeyEventDispatcher{
	
	// TextFileWriter logger creation
	static Logger logger = Logger.getLogger(TextEditorListener.class);
	
	public static volatile boolean flagCtrlPressed = false;
	
	public volatile boolean flagHoverBtnViewExpCmd = false;
	public volatile boolean flagHoverBtnRemoveSpaces = false;
	
	TextEditor currentTextEditor;
	
	public TextEditorListener(TextEditor currentTextEdit) {
		currentTextEditor = currentTextEdit;
	}
	
	private void undo(UndoManager undoManager) {
		try {
			if (undoManager.canUndo()) {
				undoManager.undo();
			}
		} catch (CannotUndoException e) {
			logger.error(e);
		}
	}
	
	private void redo(UndoManager undoManager) {
		try {
			if (undoManager.canRedo()) {
				undoManager.redo();
			}
		} catch (CannotRedoException e) {
			logger.error(e);
		}
	}
	
	@Override
	  public boolean dispatchKeyEvent(final KeyEvent e) {
	    
//////////////// LISTENER FOR KEY PRESSED EVENT - ONLY WHEN IS CORRECT TO CONTINUE FIRE EVENT WHILE PRESSISNG  //////////////////////////
		
		if (e.getID() == KeyEvent.KEY_PRESSED && currentTextEditor.parentFrame.getContentPane().getComponent(0) instanceof TextEditor) {

			/**
			 * ONLY CTRL PRESSED FLAG
			 */
			if (e.isControlDown()) {
				flagCtrlPressed = true;
				doControlActions();
			}
			
			if (isShortcutKey(e)) {
				
				
				/**
				 *  UNDO
				 */
				// case CTRL+Z -> undo
				if (e.getKeyCode()==90 && e.isControlDown() && !e.isAltDown() && !e.isShiftDown()) {
					undo(currentTextEditor.loadedTabObj.undoManager);
				}
				
				//case Ctrl+shift+Z -> undo5Steps
				if (e.getKeyCode()==90 && !e.isAltDown() && e.isControlDown() && e.isShiftDown())
				{
					for(int i = 0; i<5; i++) {
						undo(currentTextEditor.loadedTabObj.undoManager);
					}
				}
				
				/**
				 *  REDO
				 */
				//case Ctrl+alt+Z -> redo
				if (e.getKeyCode()==90 && e.isControlDown() && e.isAltDown() && !e.isShiftDown())
				{
					redo(currentTextEditor.loadedTabObj.undoManager);
				}
				
				//case Ctrl+shift+Alt+z -> redo5steps
				if (e.getKeyCode()==90 && e.isAltDown() && e.isControlDown() && e.isShiftDown())
				{
					for(int i = 0; i<5; i++) {
						redo(currentTextEditor.loadedTabObj.undoManager);
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
		}
		
////////////////LISTENER FOR KEY RELEASED EVENT - WHEN NEED TO TRIGGER ONLY ONE TIME EVEN WHILE KEEPING PRESSED  //////////////////////////
		
		if (e.getID() == KeyEvent.KEY_RELEASED  && currentTextEditor.parentFrame.getContentPane().getComponent(0) instanceof TextEditor) {
	      
			
			if(!e.isControlDown()){
				flagCtrlPressed = false;
				doControlActions();
			}
			
			if (isShortcutKey(e)) {
				
				/**
				 *  RELOAD FROM DISK -> CTRL+Shift+R
				 */
				//case Ctrl+shift+R -> reload current file from Disk
				if (!currentTextEditor.alreadySaving && e.getKeyCode() == 82 && !e.isAltDown() && e.isControlDown()
						&& e.isShiftDown()) {
					currentTextEditor.btnReloadFromDisk.doClick();
				}
				/**
				 *  SAVE FILE AS NEW -> CTRL+Shift+S
				 */
				if (!currentTextEditor.alreadySaving && e.getKeyCode() == 83 && !e.isAltDown() && e.isControlDown()
						&& e.isShiftDown()) {
					currentTextEditor.btnSaveTextFileAs.doClick();
				}
				/**
				 *  SAVE FILE -> CTRL+S
				 */
				if (!currentTextEditor.alreadySaving && e.getKeyCode() == 83 && !e.isAltDown() && e.isControlDown()
						&& !e.isShiftDown()) {
					currentTextEditor.btnSaveTextFile.doClick();
				}
				/**
				 *  LOAD FILE -> CTRL+O
				 */
				if (!currentTextEditor.alreadyLoading && e.getKeyCode() == 79 && !e.isAltDown() && e.isControlDown()
						&& !e.isShiftDown()) {
					currentTextEditor.btnLoadTextFile.doClick();
				}
				/**
				 * 	READ TEXT WITH SPEAKER / STOP PLAYNG  objSpeaker -> CTRL+Space
				 */
				if (e.getKeyCode() == 32 && !e.isAltDown() && e.isControlDown() && !e.isShiftDown()) {
					currentTextEditor.btnSpeak.doClick();
				}
				/**
				 * 	CLEAN TEXT -> CTRL+Canc
				 */
				if (e.getKeyCode() == 127 && !e.isAltDown() && e.isControlDown() && !e.isShiftDown()) {
					currentTextEditor.btnClear.doClick();
				}
				/**
				 * 	CRYPT-DECRYPT -> CTRL+Shift+C  ## CTRL+Shift+D
				 */
				if (e.getKeyCode() == 67 && !e.isAltDown() && e.isControlDown() && e.isShiftDown()) {
					currentTextEditor.btnCrypt.doClick();
				}
				if (e.getKeyCode() == 68 && !e.isAltDown() && e.isControlDown() && e.isShiftDown()) {
					currentTextEditor.btnDecrypt.doClick();
				}
				/**
				 * 	DARK/LIGHT VIEW -> CTRL+B
				 */
				if (e.getKeyCode() == 66 && !e.isAltDown() && e.isControlDown() && !e.isShiftDown()) {
					currentTextEditor.btnDark.doClick();
				}
				/**
				 * 	UPPERCASE AFTER DOTS-SEPARATOR -> CTRL+Shift+[.]
				 */
				if (e.getKeyCode() == 46 && !e.isAltDown() && e.isControlDown() && e.isShiftDown()) {
					currentTextEditor.btnUpperPostDots.doClick();
				}
				/**
				 * 	UPPERCASE SELECTED TEXT -> CTRL+Shift+UP_Arrow
				 */
				if (e.getKeyCode() == 38 && !e.isAltDown() && e.isControlDown() && e.isShiftDown()) {
					currentTextEditor.btnTt.doClick();
				}
				/**
				 * 	OPEN TEMPLATES PANEL -> CTRL+Shift+T
				 */
				if (e.getKeyCode() == 84 && !e.isAltDown() && e.isControlDown() && !e.isShiftDown()) {
					currentTextEditor.btnTemplates.doClick();
				}
				
				/**
				 * 	FIND  TEXT -> CTRL+F
				 */
				if (e.getKeyCode() == 70 && !e.isAltDown() && e.isControlDown() && !e.isShiftDown()) {
					if(currentTextEditor.searchBox != null && currentTextEditor.searchBox.active){
						String selected = currentTextEditor.textArea_1.getSelectedText();
						if(selected != null && !selected.equals("")) {
							currentTextEditor.searchBox.searchTextField.setText(selected);
							currentTextEditor.searchBox.searchTextField.setSelectionStart(0);
							currentTextEditor.searchBox.searchTextField.setSelectionEnd(selected.length());
						}
					}
					currentTextEditor.btnFindText.doClick();
				}
				
				/**
				 * 	REPLACE SPECIALS/ESCAPES -> CTRL+R
				 */
				if (e.getKeyCode() == 82 && !e.isAltDown() && e.isControlDown() && !e.isShiftDown()) {
					currentTextEditor.btnReplaceEscapesSeq.doClick();
				}
				
				/**
				 * 	REMOVE SPACES/WORD -> CTRL+[-]
				 */
				if (e.getKeyCode() == 45 && !e.isAltDown() && e.isControlDown() && !e.isShiftDown()) {
					currentTextEditor.btnRemoveSpaces.doClick();
				}
				
				/**
				 * 	NEXT TAB -> CTRL+TAB
				 */
				if (e.getKeyCode() == 9 && !e.isAltDown() && e.isControlDown() && !e.isShiftDown()) {
					currentTextEditor.goToNextTab();
				}
				
				/**
				 * 	PREVIOUS TAB -> CTRL+Shift+TAB
				 */
				if (e.getKeyCode() == 9 && !e.isAltDown() && e.isControlDown() && e.isShiftDown()) {
					currentTextEditor.goToPreviousTab();
				}
				/**
				 * 	NEW TAB -> CTRL+N
				 */
				if (e.getKeyCode() == 78 && !e.isAltDown() && e.isControlDown() && !e.isShiftDown()) {
					currentTextEditor.addTab(null, false);
				}
				/**
				 * 	CLOSE CURRENT TAB -> CTRL+W
				 */
				if (e.getKeyCode() == 87 && !e.isAltDown() && e.isControlDown() && !e.isShiftDown()) {
					currentTextEditor.closeCurrentTab();
				} 
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
	    
    return false;
	}
	
	private boolean isShortcutKey(KeyEvent e) {
		return (e.isAltDown() || e.isControlDown() || e.isShiftDown());
	}
	
	public void doControlActions() {
		
		if (flagCtrlPressed) {
			if (flagHoverBtnRemoveSpaces) {
				currentTextEditor.btnRemoveSpaces.setToolTipText(TextEditor.tooltipRemoveContent);
				currentTextEditor.btnRemoveSpaces
						.setIcon(new ImageIcon(IconsPathConfigurator.ICON_REMOVE_CONTENT));
			}
			if (flagHoverBtnViewExpCmd) {
				currentTextEditor.btnViewInExplorer.setToolTipText(TextEditor.tooltipViewInCommandLine);
				currentTextEditor.btnViewInExplorer.setIcon(new ImageIcon(IconsPathConfigurator.ICON_CONSOLE));
			}
		}

		if(!flagCtrlPressed || !flagHoverBtnRemoveSpaces){
			currentTextEditor.btnRemoveSpaces.setToolTipText(TextEditor.tooltipRemoveSpaces);
			currentTextEditor.btnRemoveSpaces.setIcon(new ImageIcon(IconsPathConfigurator.ICON_REMOVE_WHITE_LINE));
		}
		if(!flagCtrlPressed || !flagHoverBtnViewExpCmd){
			currentTextEditor.btnViewInExplorer.setToolTipText(TextEditor.tooltipViewInSysExplorer);
			currentTextEditor.btnViewInExplorer.setIcon(new ImageIcon(IconsPathConfigurator.ICON_COPY_DIR_STRUCTURE));
		}
		
	}
}

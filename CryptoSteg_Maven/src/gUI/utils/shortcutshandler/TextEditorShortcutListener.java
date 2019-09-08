package gUI.utils.shortcutshandler;

import java.awt.KeyEventDispatcher;
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;

import org.apache.log4j.Logger;

import gUI.panels.TextEditor;
import resources.IconsPathConfigurator;

public class TextEditorShortcutListener implements KeyEventDispatcher{
	
	// TextFileWriter logger creation
	static Logger logger = Logger.getLogger(TextEditorShortcutListener.class);
	
	public static volatile boolean flagCtrlPressed = false;
	public static volatile boolean flagShiftPressed = false;
	
	public volatile boolean flagHoverBtnViewExpCmd = false;
	public volatile boolean flagHoverBtnRemoveSpaces = false;
	
	TextEditor currentTextEditor;
	
	public TextEditorShortcutListener(TextEditor currentTextEdit) {
		currentTextEditor = currentTextEdit;
	}
	
	@Override
	  public boolean dispatchKeyEvent(final KeyEvent e) {
	    
//////////////// LISTENER FOR KEY PRESSED EVENT - ONLY WHEN IS CORRECT TO CONTINUE FIRE EVENT WHILE PRESSISNG  //////////////////////////
		
		if (e.getID() == KeyEvent.KEY_PRESSED && currentTextEditor.parentFrame.getContentPane().getComponent(0) instanceof TextEditor) {

			/*
			 * CTRL PRESSED FLAG
			 */
			if (e.isControlDown()) {
				flagCtrlPressed = true;
				doControlActions();
			}
			
			/*
			 * SHIFT PRESSED FLAG
			 */
			if (e.isShiftDown()) {
				flagShiftPressed = true;
			}
			
			if (isShortcutKey(e)) {
				
				/*
				 * UNDO x5 -  Ctrl+Alt+Z
				 */
				if (e.getKeyCode()==90 && e.isAltDown() && e.isControlDown() && !e.isShiftDown())
				{
					try {
						for(int i = 0; i<5; i++) {
							if (TextEditor.loadedTabObj.textArea.canUndo()) {
								TextEditor.loadedTabObj.textArea.undoLastAction();
							}
						}
					} catch (Throwable e2) {
							logger.warn(e2);
					}
				}
				
				/*
				 *  REDO x5 - Ctrl+Alt+Y
				 */
				if (e.getKeyCode()==89 && e.isAltDown() && e.isControlDown() && !e.isShiftDown())
				{
					try {
						for (int i = 0; i < 5; i++) {
							if (TextEditor.loadedTabObj.textArea.canRedo()) {
								TextEditor.loadedTabObj.textArea.redoLastAction();
							} 
						}
					} catch (Throwable e2) {
							logger.warn(e2);
					}
				}
				
				/*
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
	      
			/*
			 * CTRL RELEASED FLAG
			 */
			if(!e.isControlDown()){
				flagCtrlPressed = false;
				doControlActions();
			}
			/*
			 * SHIFT RELEASED FLAG
			 */
			if (!e.isShiftDown()) {
				flagShiftPressed = false;
			}
			
			if (isShortcutKey(e)) {
				
								
				/*
				 *  RELOAD FROM DISK -> CTRL+Shift+R
				 */
				//case Ctrl+shift+R -> reload current file from Disk
				if (!currentTextEditor.alreadySaving && e.getKeyCode() == 82 && !e.isAltDown() && e.isControlDown()
						&& e.isShiftDown()) {
					currentTextEditor.btnReloadFromDisk.doClick();
				}
				/*
				 *  SAVE FILE AS NEW -> CTRL+Shift+S
				 */
				if (!currentTextEditor.alreadySaving && e.getKeyCode() == 83 && !e.isAltDown() && e.isControlDown()
						&& e.isShiftDown()) {
					currentTextEditor.btnSaveTextFileAs.doClick();
				}
				/*
				 *  SAVE FILE -> CTRL+S
				 */
				if (!currentTextEditor.alreadySaving && e.getKeyCode() == 83 && !e.isAltDown() && e.isControlDown()
						&& !e.isShiftDown()) {
					currentTextEditor.btnSaveTextFile.doClick();
				}
				/*
				 *  LOAD FILE -> CTRL+O
				 */
				if (!TextEditor.alreadyLoading && e.getKeyCode() == 79 && !e.isAltDown() && e.isControlDown()
						&& !e.isShiftDown()) {
					currentTextEditor.btnLoadTextFile.doClick();
				}
				/*
				 * 	READ TEXT WITH SPEAKER / STOP PLAYNG  objSpeaker -> CTRL+Alt+Space
				 */
				if (e.getKeyCode() == 32 && e.isAltDown() && e.isControlDown() && !e.isShiftDown()) {
					currentTextEditor.btnSpeak.doClick();
				}
				/*
				 * 	CLEAN TEXT -> CTRL+Canc
				 */
				if (e.getKeyCode() == 127 && !e.isAltDown() && e.isControlDown() && !e.isShiftDown()) {
					currentTextEditor.btnClear.doClick();
				}
				/*
				 * 	CRYPT-DECRYPT -> CTRL+Shift+C  ## CTRL+Shift+D
				 */
				if (e.getKeyCode() == 67 && !e.isAltDown() && e.isControlDown() && e.isShiftDown()) {
					currentTextEditor.btnCrypt.doClick();
				}
				if (e.getKeyCode() == 68 && !e.isAltDown() && e.isControlDown() && e.isShiftDown()) {
					currentTextEditor.btnDecrypt.doClick();
				}
				/*
				 * 	DARK/LIGHT VIEW -> CTRL+B
				 */
				if (e.getKeyCode() == 66 && !e.isAltDown() && e.isControlDown() && !e.isShiftDown()) {
					currentTextEditor.btnDark.doClick();
				}
				/*
				 * 	UPPERCASE AFTER DOTS-SEPARATOR -> CTRL+Shift+[.]
				 */
				if (e.getKeyCode() == 46 && !e.isAltDown() && e.isControlDown() && e.isShiftDown()) {
					currentTextEditor.btnUpperPostDots.doClick();
				}
				/*
				 * 	UPPERCASE SELECTED TEXT -> CTRL+Shift+UP_Arrow
				 */
				if (e.getKeyCode() == 38 && !e.isAltDown() && e.isControlDown() && e.isShiftDown()) {
					currentTextEditor.btnTt.doClick();
				}
				/*
				 * 	OPEN TEMPLATES PANEL -> CTRL+Shift+T
				 */
				if (e.getKeyCode() == 84 && !e.isAltDown() && e.isControlDown() && !e.isShiftDown()) {
					currentTextEditor.btnTemplates.doClick();
				}
				
				/*
				 * 	FIND  TEXT -> CTRL+F
				 */
				if (e.getKeyCode() == 70 && !e.isAltDown() && e.isControlDown() && !e.isShiftDown()) {
					currentTextEditor.btnFindText.doClick();
//					if(currentTextEditor.searchBox != null && currentTextEditor.searchBox.active){
//						String selected = TextEditor.loadedTabObj.textArea.getSelectedText();
//						if(selected != null && !selected.equals("")) {
//							currentTextEditor.searchBox.searchTextField.setText(selected);
//							currentTextEditor.searchBox.searchTextField.setSelectionStart(0);
//							currentTextEditor.searchBox.searchTextField.setSelectionEnd(selected.length());
//						}
//					}
				}
				
				/*
				 * 	REPLACE SPECIALS/ESCAPES -> CTRL+E
				 */
				if (e.getKeyCode() == 69 && !e.isAltDown() && e.isControlDown() && !e.isShiftDown()) {
					currentTextEditor.btnReplaceEscapesSeq.doClick();
				}
				
				/*
				 * 	REPLACE TEXT -> CTRL+R
				 */
				if (e.getKeyCode() == 82 && !e.isAltDown() && e.isControlDown() && !e.isShiftDown()) {
					currentTextEditor.btnReplaceText.doClick();
				}
				
				/*
				 * 	GOTO LINE NUMBER -> CTRL+L
				 */
				if (e.getKeyCode() == 76 && !e.isAltDown() && e.isControlDown() && !e.isShiftDown()) {
					currentTextEditor.btnGoToLine.doClick();
				}
				
				/*
				 * 	OPEN/CLOSE FILE TREE PANEL -> ALT+Q
				 */
				if (e.getKeyCode() == 81 && e.isAltDown() && !e.isControlDown() && !e.isShiftDown()) {
					currentTextEditor.btnToggleFileView.doClick();
				}
				
				/*
				 *  REFRESH TREE -> Ctrl+F5
				 */
				if(e.getKeyCode() == 116 && !e.isAltDown() && e.isControlDown() && !e.isShiftDown()) {
					currentTextEditor.mouseListenerTreeFilePanel.getRefreshItem().doClick();
				}
				
				/*
				 * 	REMOVE SPACES/WORD -> CTRL+[-]
				 */
				if (e.getKeyCode() == 45 && !e.isAltDown() && e.isControlDown() && !e.isShiftDown()) {
					currentTextEditor.btnRemoveSpaces.doClick();
				}
				
				/*
				 * OPEN CURRENT FILE PROPERTIES -> ALT+ENTER
				 */
				if (e.getKeyCode() == 10 && e.isAltDown() && !e.isControlDown() && !e.isShiftDown()) {
					currentTextEditor.btnPropertiesPanel.doClick();
				}
				
				/*
				 * 	NEXT TAB -> CTRL+TAB
				 */
				if (e.getKeyCode() == 9 && !e.isAltDown() && e.isControlDown() && !e.isShiftDown()) {
					currentTextEditor.goToNextTab();
				}
				
				/*
				 * 	PREVIOUS TAB -> CTRL+Shift+TAB
				 */
				if (e.getKeyCode() == 9 && !e.isAltDown() && e.isControlDown() && e.isShiftDown()) {
					currentTextEditor.goToPreviousTab();
				}
				/*
				 * 	NEW TAB -> CTRL+N
				 */
				if (e.getKeyCode() == 78 && !e.isAltDown() && e.isControlDown() && !e.isShiftDown()) {
					currentTextEditor.addTab(null, false);
				}
				/*
				 * 	CLOSE CURRENT TAB -> CTRL+W
				 */
				if (e.getKeyCode() == 87 && !e.isAltDown() && e.isControlDown() && !e.isShiftDown()) {
					currentTextEditor.closeCurrentTab();
				} 
				
				/*
				 *  SWITCH INLINE TABS ON/OFF -> Ctrl+shift+I
				 */
				if(e.getKeyCode() == 73 && !e.isAltDown() && e.isControlDown() && e.isShiftDown()) {
					currentTextEditor.checkBoxInlineTabs.doClick();
				}
				
			}
			
	    }
	    
    return false;
	}
	
	/**
	 * This functions is to optimize performance, since controls will be fired only if ctrl/shift/alt is pressed
	 * @param e
	 * @return
	 */
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

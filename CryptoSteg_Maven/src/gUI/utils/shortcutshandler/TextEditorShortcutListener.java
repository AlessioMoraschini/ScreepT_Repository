package gUI.utils.shortcutshandler;

import java.awt.KeyEventDispatcher;
import java.awt.event.KeyEvent;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;

import org.apache.log4j.Logger;
import org.fife.ui.rtextarea.RTextScrollPane;

import frameutils.utils.GuiUtils;
import gUI.panels.TextEditor;
import gUI.utils.tooltip.TextEditorTooltips;
import resources.IconsPathConfigurator;
import tabbededitor.CustomTabbedEditor;

public class TextEditorShortcutListener implements KeyEventDispatcher{
	
	// TextFileWriter logger creation
	static Logger logger = Logger.getLogger(TextEditorShortcutListener.class);
	
	public static AtomicBoolean flagCtrlPressed = new AtomicBoolean(false);
	public static AtomicBoolean flagShiftPressed = new AtomicBoolean(false);
	public static AtomicBoolean flagAltPressed = new AtomicBoolean(false);
	
	public AtomicBoolean flagHoverBtnViewExpCmd = new AtomicBoolean(false);
	public AtomicBoolean flagHoverBtnRemoveSpaces = new AtomicBoolean(false);
	
	private TextEditor currentTextEditor;
	private CustomTabbedEditor currentTabEditor;
	
	public TextEditorShortcutListener(TextEditor currentTextEdit) {
		this.currentTextEditor = currentTextEdit;
		this.currentTabEditor = currentTextEditor.getCurrentTabEditor();
	}
	
	@Override
	  public boolean dispatchKeyEvent(final KeyEvent e) {
	    
//////////////// LISTENER FOR KEY PRESSED EVENT - ONLY WHEN IS CORRECT TO CONTINUE FIRE EVENT WHILE PRESSISNG  //////////////////////////
		
		if (e.getID() == KeyEvent.KEY_PRESSED && currentTextEditor.parentFrame.getContentPane().getComponent(0) instanceof TextEditor) {

			updateCtrlAltShift(e);
			
			if (isShortcutKey(e)) {
				
				/*
				 * UNDO x5 -  Ctrl+Alt+Z
				 */
				if (e.getKeyCode()==90 && e.isAltDown() && e.isControlDown() && !e.isShiftDown())
				{
					try {
						for(int i = 0; i<5; i++) {
							if (currentTabEditor.loadedTabObj.textArea.canUndo()) {
								currentTabEditor.loadedTabObj.textArea.undoLastAction();
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
							if (currentTabEditor.loadedTabObj.textArea.canRedo()) {
								currentTabEditor.loadedTabObj.textArea.redoLastAction();
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
	      
			updateCtrlAltShift(e);
			
			if (isShortcutKey(e)) {
				
								
				/*
				 *  RELOAD FROM DISK -> CTRL+Shift+R
				 */
				//case Ctrl+shift+R -> reload current file from Disk
				if (!currentTabEditor.alreadySaving && e.getKeyCode() == 82 && !e.isAltDown() && e.isControlDown()
						&& e.isShiftDown()) {
					currentTextEditor.btnReloadFromDisk.doClick();
				}
				/*
				 *  SAVE FILE AS NEW -> CTRL+Shift+S
				 */
				if (!currentTabEditor.alreadySaving && e.getKeyCode() == 83 && !e.isAltDown() && e.isControlDown()
						&& e.isShiftDown()) {
					currentTextEditor.btnSaveTextFileAs.doClick();
				}
				/*
				 *  SAVE FILE -> CTRL+S
				 */
				if (!currentTabEditor.alreadySaving && e.getKeyCode() == 83 && !e.isAltDown() && e.isControlDown()
						&& !e.isShiftDown()) {
					currentTextEditor.btnSaveTextFile.doClick();
				}
				/*
				 *  LOAD FILE -> CTRL+O
				 */
				if (!currentTabEditor.alreadyLoading && e.getKeyCode() == 79 && !e.isAltDown() && e.isControlDown()
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
				 * 	SHOW TABS GRID -> CTRL+Shift+G
				 */
				if (e.getKeyCode() == 71 && !e.isAltDown() && e.isControlDown() && !e.isShiftDown()) {
					currentTextEditor.chckbxShowTabGrid.doClick();
				}
				
				/*
				 * 	OPEN/CLOSE FILE TREE PANEL -> ALT+Q
				 */
				if (e.getKeyCode() == 81 && e.isAltDown() && !e.isControlDown() && !e.isShiftDown()) {
					currentTextEditor.btnToggleFileView.doClick();
				}
				
				/*
				 * 	OPEN/CLOSE CLIPBOARD PANEL -> ALT+A
				 */
				if (e.getKeyCode() == 65 && e.isAltDown() && !e.isControlDown() && !e.isShiftDown()) {
					currentTextEditor.toggleBtnClipboard.doClick();
				}

				/*
				 * 	OPEN/CLOSE MAP PANEL -> ALT+W
				 */
				if (e.getKeyCode() == 87 && e.isAltDown() && !e.isControlDown() && !e.isShiftDown()) {
					currentTextEditor.toggleBtnMap.doClick();
				}
				
				/*
				 *  FILE TREE AUTO UPDATE on CURRENT FILE ON/OFF -> Ctrl+shift+U
				 */
				if(e.getKeyCode() == 85 && !e.isAltDown() && e.isControlDown() && e.isShiftDown()) {
					currentTextEditor.toggleButtonFileTreeLocked.doClick();
				}
				
				/*
				 *  HIDE/SHOW current workspace -> Ctrl+Shift+W
				 */
				if(e.getKeyCode() == 87 && !e.isAltDown() && e.isControlDown() && e.isShiftDown()) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							currentTextEditor.toggleButtonFileTreeWorkspace.doClick();
						}
					});
				}
				
				/*
				 *  SELECT NEW workspace -> Ctrl+Alt+Shift+W
				 */
				if(e.getKeyCode() == 87 && e.isAltDown() && e.isControlDown() && e.isShiftDown()) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							currentTextEditor.toggleButtonFileTreeNewWorkspace.doClick();
						}
					});
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
				 * OPEN CURRENT FILE'S PROPERTIES -> ALT+ENTER
				 */
				if (e.getKeyCode() == 10 && e.isAltDown() && !e.isControlDown() && !e.isShiftDown()) {
					currentTextEditor.btnPropertiesPanel.doClick();
				}
				
				/*
				 * 	NEXT TAB -> CTRL+TAB
				 */
				if (e.getKeyCode() == 9 && !e.isAltDown() && e.isControlDown() && !e.isShiftDown()) {
					currentTabEditor.goToNextTab();
				}
				
				/*
				 * 	PREVIOUS TAB -> CTRL+Shift+TAB
				 */
				if (e.getKeyCode() == 9 && !e.isAltDown() && e.isControlDown() && e.isShiftDown()) {
					currentTabEditor.goToPreviousTab();
				}
				/*
				 * 	NEW TAB -> CTRL+N
				 */
				if (e.getKeyCode() == 78 && !e.isAltDown() && e.isControlDown() && !e.isShiftDown()) {
					currentTabEditor.addTab(null, false);
				}
				/*
				 * 	CLOSE CURRENT TAB -> CTRL+W
				 */
				if (e.getKeyCode() == 87 && !e.isAltDown() && e.isControlDown() && !e.isShiftDown()) {
					currentTabEditor.closeCurrentTab();
				} 
				
				/*
				 *  NEXT TAB IN HYSTORY -> Ctrl+Alt+ ->
				 */
				if(e.getKeyCode() == 39 && e.isAltDown() && e.isControlDown() && !e.isShiftDown()) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							currentTextEditor.getCurrentTabEditor().goToNextInHistory();
						}
					});
				}

				/*
				 *  NEXT TAB IN HYSTORY -> Ctrl+Alt+ <-
				 */
				if(e.getKeyCode() == 37 && e.isAltDown() && e.isControlDown() && !e.isShiftDown()) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							currentTextEditor.getCurrentTabEditor().goToPreviousInHistory();
						}
					});
				}
				
				/*
				 *  SWITCH INLINE TABS ON/OFF -> Ctrl+shift+I
				 */
				if(e.getKeyCode() == 73 && !e.isAltDown() && e.isControlDown() && e.isShiftDown()) {
					currentTextEditor.headerPanel.checkBoxInlineTabs.doClick();
				}
				
				/*
				 *  ADAPT WIDTH TO SCREEN SIZE -> Ctrl+shift+A
				 */
				if(e.getKeyCode() == 65 && !e.isAltDown() && e.isControlDown() && e.isShiftDown()) {
					currentTextEditor.headerPanel.chkCutWordsTo.doClick();
				}
				
				/*
				 *  HIDE/SHOW header panel -> Ctrl+shift+Alt+H
				 */
				if(e.getKeyCode() == 72 && e.isAltDown() && e.isControlDown() && e.isShiftDown()) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							currentTextEditor.textEditorUtils.hideOrShowHeaderPanelLevel1();
						}
					});
				}
				
				/*
				 *  HIDE/SHOW footer panel -> Ctrl+shift+Alt+L
				 */
				if(e.getKeyCode() == 76 && e.isAltDown() && e.isControlDown() && e.isShiftDown()) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							currentTextEditor.textEditorUtils.hideOrShowFooterPanel();
						}
					});
				}
				
				/*
				 *  ON/OFF autoSync language from file type -> Ctrl+shift+L
				 */
				if(e.getKeyCode() == 76 && !e.isAltDown() && e.isControlDown() && e.isShiftDown()) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							currentTextEditor.chckbxLockLanguage.doClick();
						}
					});
				}
				
				
				/*
				 *  OPEN CALCULATOR -> Ctrl+Alt+C
				 */
				if(e.getKeyCode() == 67 && e.isAltDown() && e.isControlDown() && !e.isShiftDown()) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							currentTextEditor.btnCalculator.doClick();
						}
					});
				}

				/*
				 *  OPEN COLOR SAMPLER -> Ctrl+Alt+Shift+P
				 */
				if(e.getKeyCode() == 80 && e.isAltDown() && e.isControlDown() && e.isShiftDown()) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							currentTextEditor.btnColorSampler.doClick();
						}
					});
				}
				
				
				/*********************************************
				 * Text Editor  -  TEXT SPECIFIC SHORTCUTS
				 *********************************************/
				
				/*
				 *  CLONE CURRENT LINE UP -> Ctrl+Alt+UP arrow
				 */
				if(e.getKeyCode() == 38 && e.isAltDown() && e.isControlDown() && !e.isShiftDown()) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							try {
								boolean result = GuiUtils.duplicateTextAreaCurrentLine(
										currentTextEditor.getCurrentTabEditor().loadedTabObj.textArea, true);
								if(!result) {
									logger.warn("Error duplicating current text Line !");
								}
							} catch (BadLocationException e) {
								logger.error("Error duplicating current text Line", e);
							}
						}
					});
				}

				
				/*
				 *  CLONE CURRENT LINE DOWN -> Ctrl+Alt+DOWN arrow
				 */
				if(e.getKeyCode() == 40 && e.isAltDown() && e.isControlDown() && !e.isShiftDown()) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							try {
								boolean result = GuiUtils.duplicateTextAreaCurrentLine(
										currentTextEditor.getCurrentTabEditor().loadedTabObj.textArea, false);
								if(!result) {
									logger.warn("Error duplicating current text Line !");
								}
							} catch (BadLocationException e) {
								logger.error("Error duplicating current text Line", e);
							}
						}
					});
				}
				
				
				
//				/*
//				 *  TEST MOVE TABS : Ctrl + shift + Alt + [->|<-]
//				 */
//				if(e.getKeyCode() == 37 && e.isAltDown() && e.isControlDown() && e.isShiftDown()) {
//					currentTextEditor.moveCurrentTab(true);
//				}
//				
//				/*
//				 *  TEST MOVE TABS : Ctrl + shift + Alt + [->|<-]
//				 */
//				if(e.getKeyCode() == 39 && e.isAltDown() && e.isControlDown() && e.isShiftDown()) {
//					currentTextEditor.moveCurrentTab(false);
//				}
				
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
	
	public synchronized void doControlActions() {
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (flagCtrlPressed.get()) {
					if (flagHoverBtnRemoveSpaces.get()) {
						currentTextEditor.btnRemoveSpaces.setToolTipText(TextEditorTooltips.tooltipRemoveContent);
						currentTextEditor.btnRemoveSpaces.setIcon(new ImageIcon(IconsPathConfigurator.ICON_REMOVE_CONTENT));
					}
					if (flagHoverBtnViewExpCmd.get()) {
						currentTextEditor.btnViewInExplorer.setToolTipText(TextEditorTooltips.tooltipViewInCommandLine);
						currentTextEditor.btnViewInExplorer.setIcon(new ImageIcon(IconsPathConfigurator.ICON_CONSOLE));
					}
				}
				if (!flagCtrlPressed.get() || !flagHoverBtnRemoveSpaces.get()) {
					currentTextEditor.btnRemoveSpaces.setToolTipText(TextEditorTooltips.tooltipRemoveSpaces);
					currentTextEditor.btnRemoveSpaces.setIcon(new ImageIcon(IconsPathConfigurator.ICON_REMOVE_WHITE_LINE));
				}
				if (!flagCtrlPressed.get() || !flagHoverBtnViewExpCmd.get()) {
					currentTextEditor.btnViewInExplorer.setToolTipText(TextEditorTooltips.tooltipViewInSysExplorer);
					currentTextEditor.btnViewInExplorer.setIcon(new ImageIcon(IconsPathConfigurator.ICON_COPY_DIR_STRUCTURE));
				}
			}
		});
		
	}
	
	private void updateCtrlAltShift(final KeyEvent e) {
		
		/*
		 * CTRL PRESSED FLAG
		 */
		if (e.isControlDown()) {
			flagCtrlPressed.set(true);
			doControlActions();
		}else {
			flagCtrlPressed.set(false);
		}
		
		/*
		 * ALT PRESSED FLAG
		 */
		if (e.isAltDown()) {
			flagAltPressed.set(true);
		}else {
			flagAltPressed.set(false);
		}
		
		/*
		 * SHIFT PRESSED FLAG
		 */
		if (e.isShiftDown()) {
			flagShiftPressed.set(true);
		}else {
			flagShiftPressed.set(false);
		}
	}
}

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
package gUI.utils.shortcutshandler;

import java.awt.KeyEventDispatcher;
import java.awt.event.KeyEvent;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;

import org.apache.log4j.Logger;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;

import dialogutils.GenericFileChooserDialog;
import files.FileVarious;
import frameutils.utils.GuiUtilsExtended;
import gUI.panels.TextEditor;
import gUI.panels.subpanels.texteditor.TextEditorSubHeadButtonsPanel;
import gui.GuiUtils;
import gui.TextEditTooltips;
import maingUI.MainGUI;
import om.ICommonShortcuts;
import resources.GeneralConfig;
import resources.IconsPathConfigurator;
import tabbededitor.CustomTabbedEditor;
import utility.KeyListenerUtils;
import utility.string.StringWorker.EOL;
import utils.SysUtils;

public class TextEditorShortcutListener implements KeyEventDispatcher, ICommonShortcuts{
	
	// TextFileWriter logger creation
	static Logger logger = Logger.getLogger(TextEditorShortcutListener.class);
	
	public static AtomicBoolean flagCtrlDown = new AtomicBoolean(false);
	public static AtomicBoolean flagShiftDown = new AtomicBoolean(false);
	public static AtomicBoolean flagAltDown = new AtomicBoolean(false);
	
	public AtomicBoolean flagHoverBtnBase64 = new AtomicBoolean(false);
	public AtomicBoolean flagHoverBtnViewExpCmd = new AtomicBoolean(false);
	public AtomicBoolean flagHoverBtnRemoveSpaces = new AtomicBoolean(false);
	
	private TextEditor currentTextEditor;
	private CustomTabbedEditor currentTabEditor;
	private TextEditorSubHeadButtonsPanel subHeaderPanel;
	
	public TextEditorShortcutListener(TextEditor currentTextEdit) {
		this.currentTextEditor = currentTextEdit;
		this.subHeaderPanel = currentTextEditor.subHeaderPanel;
		this.currentTabEditor = currentTextEditor.getCurrentTabEditor();
	}
	
	@Override
	  public boolean dispatchKeyEvent(final KeyEvent e) {
		
		if(GenericFileChooserDialog.blockEventsModal.get()) {
			return false;
		}
	    
//////////////// LISTENER FOR KEY PRESSED EVENT - ONLY WHEN IS CORRECT TO CONTINUE FIRE EVENT WHILE PRESSISNG  //////////////////////////
		
		if (e.getID() == KeyEvent.KEY_PRESSED && MainGUI.mainGuiUtils.isTextEditorActive()) {

			updateCtrlAltShiftPressed(e);
			
			// ESC + search panel or replace visible: remove highlights
			if(e.getKeyCode() == 27) {
				searchOrReplaceActive();
			}
			
			if (isShortcutKey(e)) {
				
				// SCROLL UP -> Ctrl + arrow up
				if (e.getKeyCode()==38 && !e.isAltDown() && e.isControlDown() && !e.isShiftDown()) {
					GuiUtils.scrollJscrollPane(currentTabEditor.loadedTabObj.currentScrollPane, - GeneralConfig.KEY_SCROLL_SPEED, true);
				}
				// SCROLL DOWN -> Ctrl + arrow down
				if (e.getKeyCode()==40 && !e.isAltDown() && e.isControlDown() && !e.isShiftDown()) {
					GuiUtils.scrollJscrollPane(currentTabEditor.loadedTabObj.currentScrollPane, GeneralConfig.KEY_SCROLL_SPEED, true);
				}
				
				// SCROLL RIGHT -> Ctrl + Alt + arrow right
				if (e.getKeyCode()==39 && e.isAltDown() && e.isControlDown() && !e.isShiftDown()) {
					GuiUtils.scrollJscrollPane(currentTabEditor.loadedTabObj.currentScrollPane, 2*GeneralConfig.KEY_SCROLL_SPEED, false);
				}
				// SCROLL LEFT -> Ctrl + Alt + arrow left
				if (e.getKeyCode()==37 && e.isAltDown() && e.isControlDown() && !e.isShiftDown()) {
					GuiUtils.scrollJscrollPane(currentTabEditor.loadedTabObj.currentScrollPane, - 2*GeneralConfig.KEY_SCROLL_SPEED, false);
				}
				
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
						currentTextEditor.textEditorUtils.updateCurrentUnsavedFromFileRead();
					} catch (Throwable e2) {
						logger.warn(e2);
					}
				}
				
				/*
				 *  REDO x5 - Ctrl+Alt+Y
				 */
				else if (e.getKeyCode()==89 && e.isAltDown() && e.isControlDown() && !e.isShiftDown())
				{
					try {
						for (int i = 0; i < 5; i++) {
							if (currentTabEditor.loadedTabObj.textArea.canRedo()) {
								currentTabEditor.loadedTabObj.textArea.redoLastAction();
							} 
						}
						currentTextEditor.textEditorUtils.updateCurrentUnsavedFromFileRead();
					} catch (Throwable e2) {
							logger.warn(e2);
					}
				}
				
				/*
				 *  ADJUST FONT-SIZE
				 */
				//case Ctrl+Shift[numpad +] -> increase
				else if (e.getKeyCode() == KeyEvent.VK_PLUS && e.isControlDown() && !e.isAltDown() && e.isShiftDown())
				{
					int val = subHeaderPanel.sliderFontSize.getValue();
					subHeaderPanel.sliderFontSize.setValue(val+1);
				}
				
				//case Ctrl+Shift[numpad -] -> decrease
				else if (e.getKeyCode() == KeyEvent.VK_MINUS && e.isControlDown() && !e.isAltDown() && e.isShiftDown())
				{
					int val = subHeaderPanel.sliderFontSize.getValue();
					subHeaderPanel.sliderFontSize.setValue(val-1);
				}
			}
		}
		
////////////////LISTENER FOR KEY RELEASED EVENT - WHEN NEED TO TRIGGER ONLY ONE TIME EVEN WHILE KEEPING PRESSED  //////////////////////////
		
		if (e.getID() == KeyEvent.KEY_RELEASED  && MainGUI.mainGuiUtils.isTextEditorActive()) {
	      
			updateCtrlAltShiftReleased(e);
			
			if (isShortcutKey(e)) {
				
				/*
				 * UNDO -  Ctrl+Z : check and update unsaved variable only
				 */
				if (e.getKeyCode()==90 && !e.isAltDown() && e.isControlDown() && !e.isShiftDown())
				{
					try {
						currentTextEditor.textEditorUtils.updateCurrentUnsavedFromFileRead();
					} catch (Throwable e2) {
						logger.warn(e2);
					}
				}
				
				else if (e.getKeyCode() == KeyEvent.VK_K && !e.isAltDown() && e.isControlDown() && e.isShiftDown())
				{
					try {
						revalidateUnsaved();
					} catch (Throwable e2) {
						logger.warn(e2);
					}
				}
				/*
				 * REDO -  Ctrl+Y : check and update unsaved variable only
				 */
				else if (e.getKeyCode()==89 && !e.isAltDown() && e.isControlDown() && !e.isShiftDown())
				{
					try {
						currentTextEditor.textEditorUtils.updateCurrentUnsavedFromFileRead();
					} catch (Throwable e2) {
						logger.warn(e2);
					}
				}
								
				/*
				 *  RELOAD FROM DISK -> CTRL+Shift+R
				 */
				//case Ctrl+shift+R -> reload current file from Disk
				else if (!currentTabEditor.alreadySaving && e.getKeyCode() == 82 && !e.isAltDown() && e.isControlDown()
						&& e.isShiftDown()) {
					subHeaderPanel.btnReloadFromDisk.doClick();
				}
				/*
				 *  SAVE FILE AS NEW -> CTRL+Shift+S
				 */
				else if (!currentTabEditor.alreadySaving && e.getKeyCode() == 83 && !e.isAltDown() && e.isControlDown()
						&& e.isShiftDown()) {
					subHeaderPanel.btnSaveTextFileAs.doClick();
				}
				/*
				 *  SAVE FILE -> CTRL+S
				 */
				else if (!currentTabEditor.alreadySaving && e.getKeyCode() == 83 && !e.isAltDown() && e.isControlDown()
						&& !e.isShiftDown()) {
					subHeaderPanel.btnSaveTextFile.doClick();
				}
				/*
				 *  LOAD FILE -> CTRL+O
				 */
				else if (!currentTabEditor.alreadyLoading && e.getKeyCode() == 79 && !e.isAltDown() && e.isControlDown()
						&& !e.isShiftDown()) {
					subHeaderPanel.btnLoadTextFile.doClick();
				}
				/*
				 * 	READ TEXT WITH SPEAKER / STOP PLAYNG  objSpeaker -> Ctrl+Alt+Space
				 */
				else if (e.getKeyCode() == 32 && e.isAltDown() && e.isControlDown() && !e.isShiftDown()) {
					subHeaderPanel.btnSpeak.doClick();
				}
				/*
				 * 	CLEAN TEXT -> CTRL+Canc
				 */
				else if (e.getKeyCode() == 127 && !e.isAltDown() && e.isControlDown() && !e.isShiftDown()) {
					subHeaderPanel.btnClear.doClick();
				}
				/*
				 * 	CRYPT-DECRYPT -> CTRL+Shift+C  ## CTRL+Shift+D
				 */
				else if (e.getKeyCode() == 67 && !e.isAltDown() && e.isControlDown() && e.isShiftDown()) {
					currentTextEditor.headerPanel.btnCrypt.doClick();
				}
				else if (e.getKeyCode() == 68 && !e.isAltDown() && e.isControlDown() && e.isShiftDown()) {
					currentTextEditor.headerPanel.btnDecrypt.doClick();
				}
				/*
				 * Code/Decode using base 64, selected or all text: 
				 * - Alt + 4 -> encode
				 * - Ctrl + 4 -> decode
				 */
				else if (e.getKeyCode() == 52 && e.isAltDown() && !e.isControlDown() && !e.isShiftDown()) {
					subHeaderPanel.btnBase64Conversion.doClick();
				}
				else if (e.getKeyCode() == 52 && !e.isAltDown() && e.isControlDown() && !e.isShiftDown()) {
					subHeaderPanel.btnBase64Conversion.doClick();
				}
				
				/*
				 * 	DARK/LIGHT VIEW -> CTRL+B
				 */
				else if (e.getKeyCode() == 66 && !e.isAltDown() && e.isControlDown() && !e.isShiftDown()) {
					subHeaderPanel.btnDark.doClick();
				}
				/*
				 * 	UPPERCASE AFTER DOTS-SEPARATOR -> CTRL+Shift+[.]
				 */
				else if (e.getKeyCode() == 46 && !e.isAltDown() && e.isControlDown() && e.isShiftDown()) {
					try {
						GuiUtils.fireActionListener(currentTextEditor.getCurrentTabEditor()
								.loadedTabObj.getPostDotsUppercaseListener(), null);
					} catch (Exception e1) {
						logger.error(e1);
					}
				}
				/*
				 * 	UPPERCASE SELECTED TEXT -> CTRL+Shift+UP_Arrow
				 */
				else if (KeyListenerUtils.keyUp(e) && !e.isAltDown() && e.isControlDown() && e.isShiftDown()) {
					try {
						GuiUtils.fireActionListener(currentTextEditor.getCurrentTabEditor()
								.loadedTabObj.getUppercaseListener(), null);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
				/*
				 * 	LOWERCASE SELECTED TEXT -> CTRL+Shift+DOWN_Arrow
				 */
				else if (KeyListenerUtils.keyDown(e) && !e.isAltDown() && e.isControlDown() && e.isShiftDown()) {
					try {
						GuiUtils.fireActionListener(currentTextEditor.getCurrentTabEditor()
								.loadedTabObj.getLowercaseListener(), null);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
				/*
				 * 	OPEN TEMPLATES PANEL -> CTRL+Shift+T
				 */
				else if (e.getKeyCode() == 84 && !e.isAltDown() && e.isControlDown() && !e.isShiftDown()) {
					subHeaderPanel.btnTemplates.doClick();
				}
				
				/*
				 * 	FIND  TEXT -> CTRL+F
				 */
				else if (e.getKeyCode() == 70 && !e.isAltDown() && e.isControlDown() && !e.isShiftDown()) {
					subHeaderPanel.btnFindText.doClick();
				}
				
				/*
				 * 	REPLACE SPECIALS/ESCAPES -> CTRL+E
				 */
				else if (e.getKeyCode() == 69 && !e.isAltDown() && e.isControlDown() && !e.isShiftDown()) {
					subHeaderPanel.btnReplaceEscapesSeq.doClick();
				}
				
				/*
				 * 	REPLACE TEXT -> CTRL+R
				 */
				else if (e.getKeyCode() == 82 && !e.isAltDown() && e.isControlDown() && !e.isShiftDown()) {
					subHeaderPanel.btnReplaceText.doClick();
				}

				/*
				 * 	ENCAPSULATE IN TAG -> CTRL+ALT+T
				 */
				else if (e.getKeyCode() == KeyEvent.VK_T && e.isAltDown() && e.isControlDown() && !e.isShiftDown()) {
					try {
						GuiUtils.fireActionListener(currentTextEditor.getCurrentTabEditor()
								.loadedTabObj.getEncapsulateTagListener(), null);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
				
				/*
				 * 	GOTO LINE NUMBER -> CTRL+L
				 */
				else if (e.getKeyCode() == 76 && !e.isAltDown() && e.isControlDown() && !e.isShiftDown()) {
					subHeaderPanel.btnGoToLine.doClick();
				}
				
				/*
				 * 	SHOW TABS GRID -> CTRL+Shift+G
				 */
				else if (e.getKeyCode() == 71 && !e.isAltDown() && e.isControlDown() && !e.isShiftDown()) {
					subHeaderPanel.chckbxShowTabGrid.doClick();
				}
				
				/*
				 * 	OPEN/CLOSE FILE TREE PANEL -> ALT+Q
				 */
				else if (e.getKeyCode() == 81 && e.isAltDown() && !e.isControlDown() && !e.isShiftDown()) {
					subHeaderPanel.btnToggleFileView.doClick();
				}
				
				/*
				 * 	OPEN/CLOSE CLIPBOARD PANEL -> ALT+A
				 */
				else if (e.getKeyCode() == 65 && e.isAltDown() && !e.isControlDown() && !e.isShiftDown()) {
					subHeaderPanel.toggleBtnClipboard.doClick();
				}

				/*
				 * 	OPEN/CLOSE MAP PANEL -> ALT+W
				 */
				else if (e.getKeyCode() == 87 && e.isAltDown() && !e.isControlDown() && !e.isShiftDown()) {
					subHeaderPanel.toggleBtnMap.doClick();
				}
				
				/*
				 *  FILE TREE AUTO UPDATE on CURRENT FILE ON/OFF -> Ctrl+shift+U
				 */
				else if (e.getKeyCode() == 85 && !e.isAltDown() && e.isControlDown() && e.isShiftDown()) {
					currentTextEditor.toggleButtonFileTreeLocked.doClick();
				}

				/*
				 *  FILE TREE DEPTH  +1 -> Ctrl+Alt+shift+Arrow up
				 */
				else if (e.getKeyCode() == 38 && e.isAltDown() && e.isControlDown() && e.isShiftDown()) {
					currentTextEditor.btnDepthUp_nostyle.doClick();
				}
				
				/*
				 *  FILE TREE DEPTH  -1 -> Ctrl+Alt+shift+Arrow down
				 */
				else if (e.getKeyCode() == 40 && e.isAltDown() && e.isControlDown() && e.isShiftDown()) {
					currentTextEditor.btnDepthDown_nostyle.doClick();
				}
				
				/**
				 * TOGGLE MAXIMIZE TEXT AREA
				 */
				else if (e.getKeyCode() == 77 && !e.isAltDown() && e.isControlDown() && !e.isShiftDown()) {
					currentTextEditor.toggleMaximizedTextArea();
				}
				
				
				/*
				 *  HIDE/SHOW current workspace -> Ctrl+Shift+W
				 */
				else if (e.getKeyCode() == 87 && !e.isAltDown() && e.isControlDown() && e.isShiftDown()) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							currentTextEditor.toggleButtonFileTreeWorkspace.doClick();
						}
					});
				}
				
				/*
				 *  SELECT NEW workspace -> Alt+Shift+W
				 */
				else if (e.getKeyCode() == 87 && e.isAltDown() && !e.isControlDown() && e.isShiftDown()) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							currentTextEditor.toggleButtonFileTreeNewWorkspace.doClick();
						}
					});
				}
				
				/*
				 *  REFRESH TREE -> Ctrl+F5
				 */
				else if (e.getKeyCode() == 116 && !e.isAltDown() && e.isControlDown() && !e.isShiftDown()) {
					currentTextEditor.mouseListenerTreeFilePanel.getRefreshItem().doClick();
				}
				
				/*
				 * 	REMOVE SPACES/WORD -> CTRL+[-]
				 */
				else if (e.getKeyCode() == 45 && !e.isAltDown() && e.isControlDown() && !e.isShiftDown()) {
					subHeaderPanel.btnRemoveSpaces.doClick();
				}
				
				/*
				 * OPEN CURRENT FILE'S PROPERTIES -> ALT+ENTER
				 */
				else if (e.getKeyCode() == 10 && e.isAltDown() && !e.isControlDown() && !e.isShiftDown()) {
					subHeaderPanel.btnPropertiesPanel.doClick();
				}
				/*
				 * OPEN CURRENT FILE into FyleSystem-> Ctrl+Shift+ENTER
				 */
				else if (e.getKeyCode() == 10 && !e.isAltDown() && e.isControlDown() && e.isShiftDown()) {
					try {
						GuiUtilsExtended.openInFileSystem(FileVarious.getParent(currentTextEditor.getCurrentTabEditor().loadedTabObj.loadedFile));
					} catch (Exception e2) {
					}
				}
				/*
				 * OPEN CURRENT FILE into CommandLine -> Ctrl+Alt+ENTER
				 */
				else if (e.getKeyCode() == 10 && e.isAltDown() && e.isControlDown() && !e.isShiftDown()) {
					try {
						SysUtils.cdIntoCommandLine(FileVarious.getParent(currentTextEditor.getCurrentTabEditor().loadedTabObj.loadedFile));
					} catch (Exception e2) {
					}
				}
				
				/*
				 * 	NEXT TAB -> CTRL+TAB
				 */
				else if (e.getKeyCode() == 9 && !e.isAltDown() && e.isControlDown() && !e.isShiftDown()) {
					currentTabEditor.goToNextTab();
				}
				
				/*
				 * 	PREVIOUS TAB -> CTRL+Shift+TAB
				 */
				else if (e.getKeyCode() == 9 && !e.isAltDown() && e.isControlDown() && e.isShiftDown()) {
					currentTabEditor.goToPreviousTab();
				}
				/*
				 * 	NEW TAB -> CTRL+N
				 */
				else if (e.getKeyCode() == 78 && !e.isAltDown() && e.isControlDown() && !e.isShiftDown()) {
					if (!currentTextEditor.leftSplitTreeFiles.hasFocus()) {
						currentTabEditor.addTab(null);
					}
				}
				/*
				 * 	CLOSE CURRENT TAB -> CTRL+W
				 */
				else if (e.getKeyCode() == 87 && !e.isAltDown() && e.isControlDown() && !e.isShiftDown()) {
					currentTabEditor.closeCurrentTab();
				} 
				/*
				 * 	CLOSE ALL TABS -> CTRL+ALT+SHIFT+ [-]
				 */
				else if (e.getKeyCode() == 45 && e.isAltDown() && e.isControlDown() && e.isShiftDown()) {
					subHeaderPanel.btnCloseAllTabs.doClick();
				} 
				
				/*
				 *  NEXT TAB IN HYSTORY -> Alt+ ->
				 */
				else if (e.getKeyCode() == 39 && e.isAltDown() && !e.isControlDown() && !e.isShiftDown()) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							currentTextEditor.getCurrentTabEditor().goToNextInHistory();
						}
					});
				}

				/*
				 *  NEXT TAB IN HYSTORY -> Alt+ <-
				 */
				else if (e.getKeyCode() == 37 && e.isAltDown() && !e.isControlDown() && !e.isShiftDown()) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							currentTextEditor.getCurrentTabEditor().goToPreviousInHistory();
						}
					});
				}
				
				/*
				 *  SWITCH INLINE TABS ON/OFF -> Ctrl+shift+I
				 */
				else if (e.getKeyCode() == 73 && !e.isAltDown() && e.isControlDown() && e.isShiftDown()) {
					currentTextEditor.headerPanel.checkBoxInlineTabs.doClick();
				}
				
				/*
				 *  ADAPT WIDTH TO SCREEN SIZE -> Ctrl+shift+A
				 */
				else if (e.getKeyCode() == 65 && !e.isAltDown() && e.isControlDown() && e.isShiftDown()) {
					currentTextEditor.headerPanel.chkCutWordsTo.doClick();
				}
				
			/** VISUALIZATION SWITCHER **/
				
				/*
				 *  HIDE/SHOW header panel -> Ctrl+shift+Alt+H
				 */
				else if (e.getKeyCode() == 72 && e.isAltDown() && e.isControlDown() && e.isShiftDown()) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							currentTextEditor.textEditorUtils.hideOrShowHeaderPanelLevel1();
						}
					});
				}

				/*
				 *  HIDE/SHOW subHeader panel -> Ctrl+shift+Alt+U
				 */
				else if (e.getKeyCode() == 85 && e.isAltDown() && e.isControlDown() && e.isShiftDown()) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							currentTextEditor.textEditorUtils.hideOrShowHeaderPanelLevel2();
						}
					});
				}
				
				/*
				 *  HIDE/SHOW footer panel -> Ctrl+shift+Alt+L
				 */
				else if (e.getKeyCode() == 76 && e.isAltDown() && e.isControlDown() && e.isShiftDown()) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							currentTextEditor.textEditorUtils.hideOrShowFooterPanel();
						}
					});
				}

			/** END VISUALIZATION SWITCHER **/
				
				/*
				 *  ON/OFF autoSync language from file type -> Ctrl+shift+L
				 */
				else if (e.getKeyCode() == 76 && !e.isAltDown() && e.isControlDown() && e.isShiftDown()) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							subHeaderPanel.chckbxLockLanguage.doClick();
						}
					});
				}
				
				
				/*
				 *  OPEN CALCULATOR -> Ctrl+Alt+C
				 */
				else if (e.getKeyCode() == 67 && e.isAltDown() && e.isControlDown() && !e.isShiftDown()) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							subHeaderPanel.btnCalculator.doClick();
						}
					});
				}

				/*********************************************
				 * Text Editor  -  TEXT SPECIFIC SHORTCUTS
				 *********************************************/
				
				/*
				 *  CLONE CURRENT LINE UP -> Ctrl+Alt+UP arrow
				 */
				else if (e.getKeyCode() == 38 && e.isAltDown() && e.isControlDown() && !e.isShiftDown()) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							try {
								boolean result = GuiUtilsExtended.duplicateTextAreaCurrentLine(
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
				else if (e.getKeyCode() == 40 && e.isAltDown() && e.isControlDown() && !e.isShiftDown()) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							try {
								boolean result = GuiUtilsExtended.duplicateTextAreaCurrentLine(
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

				/*
				 * 	FORMAT SUPPORTED CODE -> CTRL+SHIFT+F
				 */
				else if (e.getKeyCode() == 70 && !e.isAltDown() && e.isControlDown() && e.isShiftDown()) {
					GuiUtilsExtended.formatTextLater(currentTextEditor.getCurrentTabEditor().loadedTabObj.textArea, 
							currentTextEditor.CURRENT_EXT_OUT, 
							currentTextEditor.currentConfiguration.getTextEditorOpt().tabSize
					);
				}
				
				/*
				 *  Copy current with syntax style -> Alt+C
				 */
				else if (e.getKeyCode() == 67 && e.isAltDown() && !e.isControlDown() && !e.isShiftDown()) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							currentTextEditor.getCurrentTabEditor().loadedTabObj.textArea.copyAsStyledText();
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
				if (flagCtrlDown.get()) {
					if (flagHoverBtnRemoveSpaces.get()) {
						subHeaderPanel.btnRemoveSpaces.setToolTipText(TextEditTooltips.tooltipRemoveContent);
						subHeaderPanel.btnRemoveSpaces.setIcon(new ImageIcon(IconsPathConfigurator.ICON_REMOVE_CONTENT));
					}
					if (flagHoverBtnViewExpCmd.get()) {
						subHeaderPanel.btnViewInExplorer.setToolTipText(TextEditTooltips.tooltipViewInCommandLine);
						subHeaderPanel.btnViewInExplorer.setIcon(new ImageIcon(IconsPathConfigurator.ICON_CONSOLE));
					}
					
					if(flagHoverBtnBase64.get()) {
						subHeaderPanel.btnBase64Conversion.setToolTipText(TextEditTooltips.tooltipDecryptBase64);
						subHeaderPanel.btnBase64Conversion.setIcon(new ImageIcon(IconsPathConfigurator.ICON_64_RED));
					}
				}
				
				if (!flagCtrlDown.get() || !flagHoverBtnBase64.get()) {
					subHeaderPanel.btnBase64Conversion.setToolTipText(TextEditTooltips.tooltipCryptBase64);
					subHeaderPanel.btnBase64Conversion.setIcon(new ImageIcon(IconsPathConfigurator.ICON_64_PURPLE));
				}

				if (!flagCtrlDown.get() || !flagHoverBtnRemoveSpaces.get()) {
					subHeaderPanel.btnRemoveSpaces.setToolTipText(TextEditTooltips.tooltipRemoveSpaces);
					subHeaderPanel.btnRemoveSpaces.setIcon(new ImageIcon(IconsPathConfigurator.ICON_REMOVE_WHITE_LINE));
				}
				if (!flagCtrlDown.get() || !flagHoverBtnViewExpCmd.get()) {
					subHeaderPanel.btnViewInExplorer.setToolTipText(TextEditTooltips.tooltipViewInSysExplorer);
					subHeaderPanel.btnViewInExplorer.setIcon(new ImageIcon(IconsPathConfigurator.ICON_COPY_DIR_STRUCTURE));
				}
			}
		});
		
	}
	
	private void updateCtrlAltShiftPressed(final KeyEvent e) {
		
		/*
		 * CTRL PRESSED FLAG
		 */
		if (e.isControlDown()) {
			flagCtrlDown.set(true);
		}else {
			flagCtrlDown.set(false);
		}
		
		/*
		 * ALT PRESSED FLAG
		 */
		if (e.isAltDown()) {
			flagAltDown.set(true);
			if (!e.isControlDown() && !e.isShiftDown()) {
				currentTabEditor.loadedTabObj.textArea.setCursor(GuiUtilsExtended.CURSOR_CROSS);
			}else {
				currentTabEditor.loadedTabObj.textArea.setCursor(GuiUtilsExtended.CURSOR_TEXT);
			}
		}else {
			flagAltDown.set(false);
		}
		
		/*
		 * SHIFT PRESSED FLAG
		 */
		if (e.isShiftDown()) {
			flagShiftDown.set(true);
		}else {
			flagShiftDown.set(false);
		}

		doControlActions();
	}
	
	private void updateCtrlAltShiftReleased(final KeyEvent e) {
		
		/*
		 * CTRL PRESSED FLAG
		 */
		if (e.isControlDown()) {
			flagCtrlDown.set(true);
		}else {
			flagCtrlDown.set(false);
		}
		
		/*
		 * ALT PRESSED FLAG
		 */
		if (e.isAltDown()) {
			flagAltDown.set(true);
		}else {
			flagAltDown.set(false);
			currentTabEditor.loadedTabObj.textArea.setCursor(GuiUtilsExtended.CURSOR_TEXT);
		}
		
		/*
		 * SHIFT PRESSED FLAG
		 */
		if (e.isShiftDown()) {
			flagShiftDown.set(true);
		}else {
			flagShiftDown.set(false);
		}

		doControlActions();
	}

	@Override
	public boolean isCtrlDown() {
		return flagCtrlDown.get();
	}

	@Override
	public boolean isShiftDown() {
		return flagShiftDown.get();
	}

	@Override
	public boolean isAltDown() {
		return flagAltDown.get();
	}

	@Override
	public void revalidateUnsaved() {
		currentTextEditor.textEditorUtils.updateCurrentUnsavedFromFileRead();
	}

	@Override
	public void saveAsShortcut() {
		subHeaderPanel.saveCurrentFileAs(null);
	}
	
	@Override
	public void saveAsShortcut(EOL eol) {
		subHeaderPanel.saveCurrentFileAs(eol);
	}
	
	private void searchOrReplaceActive() {
		if(subHeaderPanel.searchBox != null || subHeaderPanel.replaceDialog != null) {
			String searchFor = UUID.randomUUID().toString();
	    	// RANDOM STRING TO DESELECT ALL
	    	SearchEngine.markAll(currentTextEditor.getCurrentTabEditor().getCurrentTextArea(), new SearchContext(searchFor));
		}
	}
}

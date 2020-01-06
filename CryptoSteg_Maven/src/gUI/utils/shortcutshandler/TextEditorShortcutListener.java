package gUI.utils.shortcutshandler;

import java.awt.KeyEventDispatcher;
import java.awt.event.KeyEvent;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;

import org.apache.log4j.Logger;

import frameutils.utils.GuiUtils;
import gUI.panels.TextEditor;
import gUI.panels.subpanels.texteditor.TextEditorSubHeadButtonsPanel;
import gUI.utils.tooltip.TextEditorTooltips;
import maingUI.MainGUI;
import resources.IconsPathConfigurator;
import shortcuts.ICommonShortcuts;
import tabbededitor.CustomTabbedEditor;
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
	    
//////////////// LISTENER FOR KEY PRESSED EVENT - ONLY WHEN IS CORRECT TO CONTINUE FIRE EVENT WHILE PRESSISNG  //////////////////////////
		
		if (e.getID() == KeyEvent.KEY_PRESSED && MainGUI.mainGuiUtils.isTextEditorActive()) {

			updateCtrlAltShiftPressed(e);
			
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
						currentTextEditor.textEditorUtils.updateCurrentUnsavedFromFileRead();
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
						currentTextEditor.textEditorUtils.updateCurrentUnsavedFromFileRead();
					} catch (Throwable e2) {
							logger.warn(e2);
					}
				}
				
				/*
				 *  ADJUST FONT-SIZE
				 */
				//case Ctrl+Shift[numpad +] -> increase
				if (e.getKeyCode() == KeyEvent.VK_PLUS && e.isControlDown() && !e.isAltDown() && e.isShiftDown())
				{
					int val = subHeaderPanel.sliderFontSize.getValue();
					subHeaderPanel.sliderFontSize.setValue(val+1);
				}
				
				//case Ctrl+Shift[numpad -] -> decrease
				if (e.getKeyCode() == KeyEvent.VK_MINUS && e.isControlDown() && !e.isAltDown() && e.isShiftDown())
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
				/*
				 * REDO -  Ctrl+Y : check and update unsaved variable only
				 */
				if (e.getKeyCode()==89 && !e.isAltDown() && e.isControlDown() && !e.isShiftDown())
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
				if (!currentTabEditor.alreadySaving && e.getKeyCode() == 82 && !e.isAltDown() && e.isControlDown()
						&& e.isShiftDown()) {
					subHeaderPanel.btnReloadFromDisk.doClick();
				}
				/*
				 *  SAVE FILE AS NEW -> CTRL+Shift+S
				 */
				if (!currentTabEditor.alreadySaving && e.getKeyCode() == 83 && !e.isAltDown() && e.isControlDown()
						&& e.isShiftDown()) {
					subHeaderPanel.btnSaveTextFileAs.doClick();
				}
				/*
				 *  SAVE FILE -> CTRL+S
				 */
				if (!currentTabEditor.alreadySaving && e.getKeyCode() == 83 && !e.isAltDown() && e.isControlDown()
						&& !e.isShiftDown()) {
					subHeaderPanel.btnSaveTextFile.doClick();
				}
				/*
				 *  LOAD FILE -> CTRL+O
				 */
				if (!currentTabEditor.alreadyLoading && e.getKeyCode() == 79 && !e.isAltDown() && e.isControlDown()
						&& !e.isShiftDown()) {
					subHeaderPanel.btnLoadTextFile.doClick();
				}
				/*
				 * 	READ TEXT WITH SPEAKER / STOP PLAYNG  objSpeaker -> Ctrl+Alt+Space
				 */
				if (e.getKeyCode() == 32 && e.isAltDown() && e.isControlDown() && !e.isShiftDown()) {
					subHeaderPanel.btnSpeak.doClick();
				}
				/*
				 * 	CLEAN TEXT -> CTRL+Canc
				 */
				if (e.getKeyCode() == 127 && !e.isAltDown() && e.isControlDown() && !e.isShiftDown()) {
					subHeaderPanel.btnClear.doClick();
				}
				/*
				 * 	CRYPT-DECRYPT -> CTRL+Shift+C  ## CTRL+Shift+D
				 */
				if (e.getKeyCode() == 67 && !e.isAltDown() && e.isControlDown() && e.isShiftDown()) {
					currentTextEditor.headerPanel.btnCrypt.doClick();
				}
				if (e.getKeyCode() == 68 && !e.isAltDown() && e.isControlDown() && e.isShiftDown()) {
					currentTextEditor.headerPanel.btnDecrypt.doClick();
				}
				/*
				 * Code/Decode using base 64, selected or all text: 
				 * - Alt + 4 -> encode
				 * - Ctrl + 4 -> decode
				 */
				if (e.getKeyCode() == 52 && e.isAltDown() && !e.isControlDown() && !e.isShiftDown()) {
					subHeaderPanel.btnBase64Conversion.doClick();
				}
				if (e.getKeyCode() == 52 && !e.isAltDown() && e.isControlDown() && !e.isShiftDown()) {
					subHeaderPanel.btnBase64Conversion.doClick();
				}
				
				/*
				 * 	DARK/LIGHT VIEW -> CTRL+B
				 */
				if (e.getKeyCode() == 66 && !e.isAltDown() && e.isControlDown() && !e.isShiftDown()) {
					subHeaderPanel.btnDark.doClick();
				}
				/*
				 * 	UPPERCASE AFTER DOTS-SEPARATOR -> CTRL+Shift+[.]
				 */
				if (e.getKeyCode() == 46 && !e.isAltDown() && e.isControlDown() && e.isShiftDown()) {
					subHeaderPanel.btnUpperPostDots.doClick();
				}
				/*
				 * 	UPPERCASE SELECTED TEXT -> CTRL+Shift+UP_Arrow
				 */
				if (e.getKeyCode() == 38 && !e.isAltDown() && e.isControlDown() && e.isShiftDown()) {
					subHeaderPanel.btnTt.doClick();
				}
				/*
				 * 	OPEN TEMPLATES PANEL -> CTRL+Shift+T
				 */
				if (e.getKeyCode() == 84 && !e.isAltDown() && e.isControlDown() && !e.isShiftDown()) {
					subHeaderPanel.btnTemplates.doClick();
				}
				
				/*
				 * 	FIND  TEXT -> CTRL+F
				 */
				if (e.getKeyCode() == 70 && !e.isAltDown() && e.isControlDown() && !e.isShiftDown()) {
					subHeaderPanel.btnFindText.doClick();
				}
				
				/*
				 * 	REPLACE SPECIALS/ESCAPES -> CTRL+E
				 */
				if (e.getKeyCode() == 69 && !e.isAltDown() && e.isControlDown() && !e.isShiftDown()) {
					subHeaderPanel.btnReplaceEscapesSeq.doClick();
				}
				
				/*
				 * 	REPLACE TEXT -> CTRL+R
				 */
				if (e.getKeyCode() == 82 && !e.isAltDown() && e.isControlDown() && !e.isShiftDown()) {
					subHeaderPanel.btnReplaceText.doClick();
				}
				
				/*
				 * 	GOTO LINE NUMBER -> CTRL+L
				 */
				if (e.getKeyCode() == 76 && !e.isAltDown() && e.isControlDown() && !e.isShiftDown()) {
					subHeaderPanel.btnGoToLine.doClick();
				}
				
				/*
				 * 	SHOW TABS GRID -> CTRL+Shift+G
				 */
				if (e.getKeyCode() == 71 && !e.isAltDown() && e.isControlDown() && !e.isShiftDown()) {
					subHeaderPanel.chckbxShowTabGrid.doClick();
				}
				
				/*
				 * 	OPEN/CLOSE FILE TREE PANEL -> ALT+Q
				 */
				if (e.getKeyCode() == 81 && e.isAltDown() && !e.isControlDown() && !e.isShiftDown()) {
					subHeaderPanel.btnToggleFileView.doClick();
				}
				
				/*
				 * 	OPEN/CLOSE CLIPBOARD PANEL -> ALT+A
				 */
				if (e.getKeyCode() == 65 && e.isAltDown() && !e.isControlDown() && !e.isShiftDown()) {
					subHeaderPanel.toggleBtnClipboard.doClick();
				}

				/*
				 * 	OPEN/CLOSE MAP PANEL -> ALT+W
				 */
				if (e.getKeyCode() == 87 && e.isAltDown() && !e.isControlDown() && !e.isShiftDown()) {
					subHeaderPanel.toggleBtnMap.doClick();
				}
				
				/*
				 *  FILE TREE AUTO UPDATE on CURRENT FILE ON/OFF -> Ctrl+shift+U
				 */
				if(e.getKeyCode() == 85 && !e.isAltDown() && e.isControlDown() && e.isShiftDown()) {
					currentTextEditor.toggleButtonFileTreeLocked.doClick();
				}

				/*
				 *  FILE TREE DEPTH  +1 -> Ctrl+Alt+shift+Arrow up
				 */
				if(e.getKeyCode() == 38 && e.isAltDown() && e.isControlDown() && e.isShiftDown()) {
					currentTextEditor.btnDepthUp_nostyle.doClick();
				}
				
				/*
				 *  FILE TREE DEPTH  -1 -> Ctrl+Alt+shift+Arrow down
				 */
				if(e.getKeyCode() == 40 && e.isAltDown() && e.isControlDown() && e.isShiftDown()) {
					currentTextEditor.btnDepthDown_nostyle.doClick();
				}
				
				/**
				 * TOGGLE MAXIMIZE TEXT AREA
				 */
				if(e.getKeyCode() == 77 && !e.isAltDown() && e.isControlDown() && !e.isShiftDown()) {
					currentTextEditor.toggleMaximizedTextArea();
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
				 *  SELECT NEW workspace -> Alt+Shift+W
				 */
				if(e.getKeyCode() == 87 && e.isAltDown() && !e.isControlDown() && e.isShiftDown()) {
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
					subHeaderPanel.btnRemoveSpaces.doClick();
				}
				
				/*
				 * OPEN CURRENT FILE'S PROPERTIES -> ALT+ENTER
				 */
				if (e.getKeyCode() == 10 && e.isAltDown() && !e.isControlDown() && !e.isShiftDown()) {
					subHeaderPanel.btnPropertiesPanel.doClick();
				}
				/*
				 * OPEN CURRENT FILE into FyleSystem-> Ctrl+ENTER
				 */
				if (e.getKeyCode() == 10 && !e.isAltDown() && e.isControlDown() && !e.isShiftDown()) {
					try {
						GuiUtils.openInFileSystem(currentTextEditor.getCurrentTabEditor().loadedTabObj.loadedFile.getParentFile());
					} catch (Exception e2) {
					}
				}
				/*
				 * OPEN CURRENT FILE into CommandLine -> Ctrl+Shift+ENTER
				 */
				if (e.getKeyCode() == 10 && !e.isAltDown() && e.isControlDown() && e.isShiftDown()) {
					try {
						SysUtils.cdIntoCommandLine(currentTextEditor.getCurrentTabEditor().loadedTabObj.loadedFile.getParentFile());
					} catch (Exception e2) {
					}
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
					currentTabEditor.addTab(null);
				}
				/*
				 * 	CLOSE CURRENT TAB -> CTRL+W
				 */
				if (e.getKeyCode() == 87 && !e.isAltDown() && e.isControlDown() && !e.isShiftDown()) {
					currentTabEditor.closeCurrentTab();
				} 
				/*
				 * 	CLOSE ALL TABS -> CTRL+ALT+SHIFT+ [-]
				 */
				if (e.getKeyCode() == 45 && e.isAltDown() && e.isControlDown() && e.isShiftDown()) {
					subHeaderPanel.btnCloseAllTabs.doClick();
				} 
				
				/*
				 *  NEXT TAB IN HYSTORY -> Alt+ ->
				 */
				if(e.getKeyCode() == 39 && e.isAltDown() && !e.isControlDown() && !e.isShiftDown()) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							currentTextEditor.getCurrentTabEditor().goToNextInHistory();
						}
					});
				}

				/*
				 *  NEXT TAB IN HYSTORY -> Alt+ <-
				 */
				if(e.getKeyCode() == 37 && e.isAltDown() && !e.isControlDown() && !e.isShiftDown()) {
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
				
			/** VISUALIZATION SWITCHER **/
				
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
				 *  HIDE/SHOW subHeader panel -> Ctrl+shift+Alt+U
				 */
				if(e.getKeyCode() == 85 && e.isAltDown() && e.isControlDown() && e.isShiftDown()) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							currentTextEditor.textEditorUtils.hideOrShowHeaderPanelLevel2();
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

			/** END VISUALIZATION SWITCHER **/
				
				/*
				 *  ON/OFF autoSync language from file type -> Ctrl+shift+L
				 */
				if(e.getKeyCode() == 76 && !e.isAltDown() && e.isControlDown() && e.isShiftDown()) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							subHeaderPanel.chckbxLockLanguage.doClick();
						}
					});
				}
				
				
				/*
				 *  OPEN CALCULATOR -> Ctrl+Alt+C
				 */
				if(e.getKeyCode() == 67 && e.isAltDown() && e.isControlDown() && !e.isShiftDown()) {
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

				/*
				 * 	FORMAT SUPPORTED CODE -> CTRL+SHIFT+F
				 */
				if (e.getKeyCode() == 70 && !e.isAltDown() && e.isControlDown() && e.isShiftDown()) {
					GuiUtils.formatTextLater(currentTextEditor.getCurrentTabEditor().loadedTabObj.textArea, 
							currentTextEditor.CURRENT_EXT_OUT, 
							currentTextEditor.currentConfiguration.getTextEditorOpt().tabSize
					);
				}
				
				/*
				 *  Copy current with syntax style -> Alt+C
				 */
				if(e.getKeyCode() == 67 && e.isAltDown() && !e.isControlDown() && !e.isShiftDown()) {
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
						subHeaderPanel.btnRemoveSpaces.setToolTipText(TextEditorTooltips.tooltipRemoveContent);
						subHeaderPanel.btnRemoveSpaces.setIcon(new ImageIcon(IconsPathConfigurator.ICON_REMOVE_CONTENT));
					}
					if (flagHoverBtnViewExpCmd.get()) {
						subHeaderPanel.btnViewInExplorer.setToolTipText(TextEditorTooltips.tooltipViewInCommandLine);
						subHeaderPanel.btnViewInExplorer.setIcon(new ImageIcon(IconsPathConfigurator.ICON_CONSOLE));
					}
					
					if(flagHoverBtnBase64.get()) {
						subHeaderPanel.btnBase64Conversion.setToolTipText(TextEditorTooltips.tooltipDecryptBase64);
						subHeaderPanel.btnBase64Conversion.setIcon(new ImageIcon(IconsPathConfigurator.ICON_64_RED));
					}
				}
				
				if (!flagCtrlDown.get() || !flagHoverBtnBase64.get()) {
					subHeaderPanel.btnBase64Conversion.setToolTipText(TextEditorTooltips.tooltipCryptBase64);
					subHeaderPanel.btnBase64Conversion.setIcon(new ImageIcon(IconsPathConfigurator.ICON_64_PURPLE));
				}

				if (!flagCtrlDown.get() || !flagHoverBtnRemoveSpaces.get()) {
					subHeaderPanel.btnRemoveSpaces.setToolTipText(TextEditorTooltips.tooltipRemoveSpaces);
					subHeaderPanel.btnRemoveSpaces.setIcon(new ImageIcon(IconsPathConfigurator.ICON_REMOVE_WHITE_LINE));
				}
				if (!flagCtrlDown.get() || !flagHoverBtnViewExpCmd.get()) {
					subHeaderPanel.btnViewInExplorer.setToolTipText(TextEditorTooltips.tooltipViewInSysExplorer);
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
				currentTabEditor.loadedTabObj.textArea.setCursor(GuiUtils.CURSOR_CROSS);
			}else {
				currentTabEditor.loadedTabObj.textArea.setCursor(GuiUtils.CURSOR_TEXT);
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
			currentTabEditor.loadedTabObj.textArea.setCursor(GuiUtils.CURSOR_TEXT);
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
}

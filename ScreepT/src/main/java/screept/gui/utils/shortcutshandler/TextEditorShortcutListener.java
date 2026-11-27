/**
 *
 * =========================================================================================
 *  Copyright (C) 2019-2020
 *
 *  AM-Design-Development - (Alessio Moraschini)
 * =========================================================================================
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package screept.gui.utils.shortcutshandler;

import various.common.light.gui.ShortcutKeys;
import java.awt.KeyEventDispatcher;
import java.awt.event.KeyEvent;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;

import org.apache.log4j.Logger;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;

import gui.commons.dialogutils.GenericFileChooserDialog;
import gui.commons.frameutils.utils.GuiUtilsExtended;
import gui.texteditor.commons.TextEditTooltips;
import gui.texteditor.commons.tabbededitor.CustomTabbedEditor;
import gui.texteditor.commons.om.TabTextEditorObject;
import maingUI.MainGUI;
import os.commons.utils.SysUtils;
import resources.GeneralConfig;
import screept.gui.panels.TextEditor;
import screept.gui.panels.quicknotes.QuickNotesDrawer;
import screept.gui.panels.subpanels.texteditor.TextEditorSubHeadButtonsPanel;
import screept.gui.panels.subpanels.texteditor.toolbar.EditorIcons;
import screept.gui.panels.subpanels.texteditor.toolbar.ToolbarStyle;
import various.common.light.files.FileVarious;
import various.common.light.gui.GuiUtils;
import various.common.light.om.ICommonShortcuts;
import various.common.light.utility.KeyListenerUtils;
import various.common.light.utility.string.StringWorker.EOL;

public class TextEditorShortcutListener implements KeyEventDispatcher, ICommonShortcuts{

	// TextFileWriter logger creation
	static Logger logger = Logger.getLogger(TextEditorShortcutListener.class);

	public static AtomicBoolean flagCtrlDown = new AtomicBoolean(false);
	public static AtomicBoolean flagShiftDown = new AtomicBoolean(false);
	public static AtomicBoolean flagAltDown = new AtomicBoolean(false);

	public AtomicBoolean flagHoverBtnBase64 = new AtomicBoolean(false);

	private TextEditor currentTextEditor;
	/** The panel of the editor that has the commands (a second panel can be open beside the first) */
	private CustomTabbedEditor currentTabEditor() {
		return currentTextEditor.getCurrentTabEditor();
	}
	private TextEditorSubHeadButtonsPanel subHeaderPanel;

	private ShortcutsEnabler enabler;

	public void setShortcutsEnabler(ShortcutsEnabler enabler) {
		this.enabler = enabler;
	}

	public TextEditorShortcutListener(TextEditor currentTextEditor, ShortcutsEnabler enabler) {
		this(currentTextEditor);
		this.enabler = enabler;
	}

	public TextEditorShortcutListener(TextEditor currentTextEdit) {
		this.currentTextEditor = currentTextEdit;
		this.subHeaderPanel = currentTextEditor.subHeaderPanel;
		// the window lost the focus (Alt+Tab...): the release of the keys is never seen, nothing must stay "pressed"
		java.awt.KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener("activeWindow", ev -> {
			flagCtrlDown.set(false);
			flagAltDown.set(false);
			flagShiftDown.set(false);
			resetAltCursor();
		});
	}

	/** Ctrl+Z, Ctrl+Y and Ctrl+Shift+Z while the focus is on a viewer tab that keeps its own history */
	private boolean viewerHistoryKey(KeyEvent e) {
		boolean z = e.getKeyCode() == KeyEvent.VK_Z;
		if (e.getID() != KeyEvent.KEY_PRESSED || !(z || e.getKeyCode() == KeyEvent.VK_Y) || !ShortcutKeys.isCtrlDown(e)
				|| e.isAltDown() || !areShortcutsEnabled(enabler)) {
			return false;
		}
		try {
			java.awt.Component focus = java.awt.KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
			if (focus == null || focus instanceof javax.swing.text.JTextComponent || !MainGUI.mainGuiUtils.isTextEditorActive(true)) {
				return false;
			}
			gui.texteditor.commons.om.TabTextEditorObject tab = currentTabEditor().loadedTabObj;
			if (tab == null || !tab.isEmbeddedViewer() || !tab.viewer.supportsEditHistory()
					|| !javax.swing.SwingUtilities.isDescendingFrom(focus, currentTabEditor().mainTabList)) {
				return false;
			}
			if (z && !e.isShiftDown()) {
				tab.viewer.undo();
			} else {
				tab.viewer.redo();
			}
			return true;
		} catch (Exception e1) {
			logger.warn("Cannot apply undo/redo to the viewer", e1);
			return false;
		}
	}

	@Override
	  public boolean dispatchKeyEvent(final KeyEvent e) {

		// whatever the state of the shortcuts, a released key must never leave the editor in a "pressed" mode (cross cursor, scroll...)
		if (e.getID() == KeyEvent.KEY_RELEASED) {
			flagCtrlDown.set(ShortcutKeys.isCtrlDown(e));
			flagAltDown.set(e.isAltDown());
			flagShiftDown.set(e.isShiftDown());
			if (!e.isAltDown()) {
				resetAltCursor();
			}
		}

		// a terminal with the focus owns the keyboard: no shortcut of the other panels
		if(ShortcutKeys.isFocusOwnerCapturingKeys()) {
			return false;
		}

		if(GenericFileChooserDialog.blockEventsModal.get()) {
			return false;
		}

		// typing in the quick notes drawer must not trigger editor shortcuts (undo, find...)
		if (QuickNotesDrawer.ownsFocus()) {
			return false;
		}

		// a viewer tab with its own history (PDF editing) takes undo and redo; text fields keep theirs
		if (viewerHistoryKey(e)) {
			return true;
		}

//////////////// LISTENER FOR KEY PRESSED EVENT - ONLY WHEN IS CORRECT TO CONTINUE FIRE EVENT WHILE PRESSISNG  //////////////////////////

		if (e.getID() == KeyEvent.KEY_PRESSED && areShortcutsEnabled(enabler)) {

			updateCtrlAltShiftPressed(e);

			// ESC + search panel or replace visible: remove highlights
			if(e.getKeyCode() == 27) {
				searchOrReplaceActive();
			}

			if (isShortcutKey(e)&& MainGUI.mainGuiUtils.isTextEditorActive(true)) {

				// SCROLL UP -> Ctrl + arrow up
				if (e.getKeyCode()==38 && !e.isAltDown() && ShortcutKeys.isCtrlDown(e) && !e.isShiftDown()) {
					GuiUtils.scrollJscrollPane(currentTabEditor().loadedTabObj.currentScrollPane, - GeneralConfig.KEY_SCROLL_SPEED, true);
				}
				// SCROLL DOWN -> Ctrl + arrow down
				if (e.getKeyCode()==40 && !e.isAltDown() && ShortcutKeys.isCtrlDown(e) && !e.isShiftDown()) {
					GuiUtils.scrollJscrollPane(currentTabEditor().loadedTabObj.currentScrollPane, GeneralConfig.KEY_SCROLL_SPEED, true);
				}

				// SCROLL RIGHT -> Ctrl + Alt + arrow right
				if (e.getKeyCode()==39 && e.isAltDown() && ShortcutKeys.isCtrlDown(e) && !e.isShiftDown()) {
					GuiUtils.scrollJscrollPane(currentTabEditor().loadedTabObj.currentScrollPane, 2*GeneralConfig.KEY_SCROLL_SPEED, false);
				}
				// SCROLL LEFT -> Ctrl + Alt + arrow left
				if (e.getKeyCode()==37 && e.isAltDown() && ShortcutKeys.isCtrlDown(e) && !e.isShiftDown()) {
					GuiUtils.scrollJscrollPane(currentTabEditor().loadedTabObj.currentScrollPane, - 2*GeneralConfig.KEY_SCROLL_SPEED, false);
				}

				/*
				 * UNDO x5 -  Ctrl+Alt+Z
				 */
				if (e.getKeyCode()==90 && ShortcutKeys.isCtrlDown(e) && !e.isShiftDown())
				{
					try {

						if (e.isAltDown()) {
							for (int i = 0; i < 5; i++) {
								if (currentTabEditor().loadedTabObj.textArea.canUndo()) {
									currentTabEditor().loadedTabObj.textArea.undoLastAction();
								}
							}
						} else {
							// Ctrl-Z already implemented in text area - only dto backup
						}
					} catch (Throwable e2) {
						logger.warn(e2);
					}
				}

				/*
				 *  REDO x5 - Ctrl+Alt+Y
				 */
				else if (e.getKeyCode()==89 && ShortcutKeys.isCtrlDown(e) && !e.isShiftDown())
				{
					try {
						if (e.isAltDown()) {
							for (int i = 0; i < 5; i++) {
								if (currentTabEditor().loadedTabObj.textArea.canRedo()) {
									currentTabEditor().loadedTabObj.textArea.redoLastAction();
								}
							}
						} else {
							// Ctrl-Y already implemented in text area - only dto backup
						}
					} catch (Throwable e2) {
						logger.warn(e2);
					}
				}

				/*
				 *  ADJUST FONT-SIZE
				 */
				//case Ctrl+Shift[numpad +] -> increase
				else if (e.getKeyCode() == KeyEvent.VK_PLUS && ShortcutKeys.isCtrlDown(e) && !e.isAltDown() && e.isShiftDown())
				{
					int val = subHeaderPanel.sliderFontSize.getValue();
					subHeaderPanel.sliderFontSize.setValue(val+1);
				}

				//case Ctrl+Shift[numpad -] -> decrease
				else if (e.getKeyCode() == KeyEvent.VK_MINUS && ShortcutKeys.isCtrlDown(e) && !e.isAltDown() && e.isShiftDown())
				{
					int val = subHeaderPanel.sliderFontSize.getValue();
					subHeaderPanel.sliderFontSize.setValue(val-1);
				}
			}
		}

////////////////LISTENER FOR KEY RELEASED EVENT - WHEN NEED TO TRIGGER ONLY ONE TIME EVEN WHILE KEEPING PRESSED  //////////////////////////

		if (e.getID() == KeyEvent.KEY_RELEASED && areShortcutsEnabled(enabler)) {

			updateCtrlAltShiftReleased(e);

			if (isShortcutKey(e)) {

				/*
				 * UNDO -  Ctrl+Z : check and update unsaved variable only
				 */
				if (e.getKeyCode() == KeyEvent.VK_Z && ShortcutKeys.isCtrlDown(e) && !e.isShiftDown())
				{
					SwingUtilities.invokeLater(()->{
						try {
							currentTextEditor.textEditorUtils.updateCurrentUnsavedFromFileRead();
						} catch (Throwable e2) {
							logger.warn(e2);
						}
					});
				}
				/*
				 * REDO -  Ctrl+Y : check and update unsaved variable only
				 */
				else if (e.getKeyCode()== KeyEvent.VK_Y && ShortcutKeys.isCtrlDown(e) && !e.isShiftDown())
				{
					SwingUtilities.invokeLater(()->{
						try {
							currentTextEditor.textEditorUtils.updateCurrentUnsavedFromFileRead();
						} catch (Throwable e2) {
							logger.warn(e2);
						}
					});
				}

				else if (e.getKeyCode() == KeyEvent.VK_K && !e.isAltDown() && ShortcutKeys.isCtrlDown(e) && e.isShiftDown())
				{
					try {
						revalidateUnsaved();
					} catch (Throwable e2) {
						logger.warn(e2);
					}
				}

				/*
				 *  RELOAD FROM DISK -> CTRL+Shift+R
				 */
				//case Ctrl+shift+R -> reload current file from Disk
				else if (!currentTabEditor().alreadySaving && e.getKeyCode() == 82 && !e.isAltDown() && ShortcutKeys.isCtrlDown(e)
						&& e.isShiftDown()) {
					subHeaderPanel.btnReloadFromDisk.doClick();
				}
				/*
				 *  SAVE FILE AS NEW -> CTRL+Shift+S
				 */
				else if (!currentTabEditor().alreadySaving && e.getKeyCode() == 83 && !e.isAltDown() && ShortcutKeys.isCtrlDown(e)
						&& e.isShiftDown()) {
					subHeaderPanel.btnSaveTextFileAs.doClick();
				}
				/*
				 *  SAVE FILE -> CTRL+S
				 */
				else if (!currentTabEditor().alreadySaving && e.getKeyCode() == 83 && !e.isAltDown() && ShortcutKeys.isCtrlDown(e)
						&& !e.isShiftDown()) {
					subHeaderPanel.btnSaveTextFile.doClick();
				}
				/*
				 *  LOAD FILE -> CTRL+O
				 */
				else if (!currentTabEditor().alreadyLoading && e.getKeyCode() == 79 && !e.isAltDown() && ShortcutKeys.isCtrlDown(e)
						&& !e.isShiftDown()) {
					subHeaderPanel.btnLoadTextFile.doClick();
				}
				/*
				 * 	READ TEXT WITH SPEAKER / STOP PLAYNG  objSpeaker -> Ctrl+Alt+Space
				 */
				else if (e.getKeyCode() == 32 && e.isAltDown() && ShortcutKeys.isCtrlDown(e) && !e.isShiftDown()) {
					subHeaderPanel.btnSpeak.doClick();
				}
				/*
				 * 	CLEAN TEXT -> CTRL+Canc
				 */
				else if (e.getKeyCode() == 127 && !e.isAltDown() && ShortcutKeys.isCtrlDown(e) && !e.isShiftDown()) {
					subHeaderPanel.btnClear.doClick();
				}
				/*
				 * 	CRYPT-DECRYPT -> CTRL+Shift+C  ## CTRL+Shift+D
				 */
				else if (e.getKeyCode() == 67 && !e.isAltDown() && ShortcutKeys.isCtrlDown(e) && e.isShiftDown()) {
					currentTextEditor.headerPanel.btnCrypt.doClick();
				}
				else if (e.getKeyCode() == 68 && !e.isAltDown() && ShortcutKeys.isCtrlDown(e) && e.isShiftDown()) {
					currentTextEditor.headerPanel.btnDecrypt.doClick();
				}
				/*
				 * Code/Decode using base 64, selected or all text:
				 * - Alt + 4 -> encode
				 * - Ctrl + 4 -> decode
				 */
				else if (e.getKeyCode() == 52 && e.isAltDown() && !ShortcutKeys.isCtrlDown(e) && !e.isShiftDown()) {
					subHeaderPanel.btnBase64Conversion.doClick();
				}
				else if (e.getKeyCode() == 52 && !e.isAltDown() && ShortcutKeys.isCtrlDown(e) && !e.isShiftDown()) {
					subHeaderPanel.btnBase64Conversion.doClick();
				}


				/*
				 * 	UPPERCASE AFTER DOTS-SEPARATOR -> CTRL+Shift+[.]
				 */
				else if (e.getKeyCode() == 46 && !e.isAltDown() && ShortcutKeys.isCtrlDown(e) && e.isShiftDown()) {
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
				else if (KeyListenerUtils.keyUp(e) && !e.isAltDown() && ShortcutKeys.isCtrlDown(e) && e.isShiftDown()) {
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
				else if (KeyListenerUtils.keyDown(e) && !e.isAltDown() && ShortcutKeys.isCtrlDown(e) && e.isShiftDown()) {
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
				else if (e.getKeyCode() == 84 && !e.isAltDown() && ShortcutKeys.isCtrlDown(e) && !e.isShiftDown()) {
					subHeaderPanel.btnTemplates.doClick();
				}

				/*
				 * 	FIND  TEXT -> CTRL+F
				 */
				else if (e.getKeyCode() == 70 && !e.isAltDown() && ShortcutKeys.isCtrlDown(e) && !e.isShiftDown()) {
					subHeaderPanel.btnFindText.doClick();
				}

				/*
				 * 	REPLACE SPECIALS/ESCAPES -> CTRL+E
				 */
				else if (e.getKeyCode() == 69 && !e.isAltDown() && ShortcutKeys.isCtrlDown(e) && !e.isShiftDown()) {
					subHeaderPanel.btnReplaceEscapesSeq.doClick();
				}

				/*
				 * 	REPLACE TEXT -> CTRL+R
				 */
				else if (e.getKeyCode() == 82 && !e.isAltDown() && ShortcutKeys.isCtrlDown(e) && !e.isShiftDown()) {
					subHeaderPanel.btnReplaceText.doClick();
				}

				/*
				 * 	ENCAPSULATE IN TAG -> CTRL+ALT+T
				 */
				else if (e.getKeyCode() == KeyEvent.VK_T && e.isAltDown() && ShortcutKeys.isCtrlDown(e) && !e.isShiftDown()) {
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
				else if (e.getKeyCode() == 76 && !e.isAltDown() && ShortcutKeys.isCtrlDown(e) && !e.isShiftDown()) {
					subHeaderPanel.btnGoToLine.doClick();
				}

				/*
				 * 	SHOW TABS GRID -> CTRL+Shift+G
				 */
				else if (e.getKeyCode() == 71 && !e.isAltDown() && ShortcutKeys.isCtrlDown(e) && !e.isShiftDown()) {
					subHeaderPanel.chckbxShowTabGrid.doClick();
				}

				/*
				 * 	OPEN/CLOSE FILE TREE PANEL -> ALT+Q
				 */
				else if (e.getKeyCode() == KeyEvent.VK_Q && e.isAltDown() && !ShortcutKeys.isCtrlDown(e) && !e.isShiftDown()) {
					subHeaderPanel.btnToggleFileView.doClick();
				}

				/*
				 * 	PIN / UNPIN CURRENT TAB -> ALT+SHIFT+J (ALT+SHIFT+Q is the documentation)
				 */
				else if (e.getKeyCode() == KeyEvent.VK_J && e.isAltDown() && !ShortcutKeys.isCtrlDown(e) && e.isShiftDown()) {
					gui.texteditor.commons.tabbededitor.CustomTabbedEditor tabs = currentTextEditor.getCurrentTabEditor();
					tabs.togglePin(tabs.mainTabList.getSelectedIndex());
				}

				/*
				 * 	OPEN/CLOSE CLIPBOARD PANEL -> ALT+A
				 */
				else if (e.getKeyCode() == KeyEvent.VK_A && e.isAltDown() && !ShortcutKeys.isCtrlDown(e) && !e.isShiftDown()) {
					subHeaderPanel.toggleBtnClipboard.doClick();
				}

				/*
				 * 	OPEN/CLOSE MAP PANEL -> ALT+W
				 */
				else if (e.getKeyCode() == KeyEvent.VK_W && e.isAltDown() && !ShortcutKeys.isCtrlDown(e) && !e.isShiftDown()) {
					subHeaderPanel.toggleBtnMap.doClick();
				}

				/*
				 * 	OPEN/CLOSE CONSOLE PANEL -> ALT+S
				 */
				else if (e.getKeyCode() == KeyEvent.VK_S && e.isAltDown() && !ShortcutKeys.isCtrlDown(e) && !e.isShiftDown()) {
					subHeaderPanel.toggleBtnConsole.doClick();
				}

				/*
				 *  FILE TREE AUTO UPDATE on CURRENT FILE ON/OFF -> Ctrl+shift+U
				 */
				else if (e.getKeyCode() == KeyEvent.VK_U && !e.isAltDown() && ShortcutKeys.isCtrlDown(e) && e.isShiftDown()) {
					currentTextEditor.toggleButtonFileTreeLocked.doClick();
				}

				/*
				 *  FILE TREE DEPTH  +1 -> Ctrl+Alt+shift+Arrow up
				 */
				else if (e.getKeyCode() == 38 && e.isAltDown() && ShortcutKeys.isCtrlDown(e) && e.isShiftDown()) {
					currentTextEditor.btnDepthUp_nostyle.doClick();
				}

				/*
				 *  FILE TREE DEPTH  -1 -> Ctrl+Alt+shift+Arrow down
				 */
				else if (e.getKeyCode() == 40 && e.isAltDown() && ShortcutKeys.isCtrlDown(e) && e.isShiftDown()) {
					currentTextEditor.btnDepthDown_nostyle.doClick();
				}

				/**
				 * TOGGLE MAXIMIZE TEXT AREA
				 */
				else if (e.getKeyCode() == KeyEvent.VK_M && !e.isAltDown() && ShortcutKeys.isCtrlDown(e) && !e.isShiftDown()) {
					currentTextEditor.toggleMaximizedTextArea();
				}


				/*
				 *  HIDE/SHOW current workspace -> Ctrl+Shift+W
				 */
				else if (e.getKeyCode() == 87 && !e.isAltDown() && ShortcutKeys.isCtrlDown(e) && e.isShiftDown()) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							currentTextEditor.toggleButtonFileTreeWorkspace.doClick();
						}
					});
				}

				/*
				 *  SELECT NEW workspace -> Alt+Shift+W
				 */
				else if (e.getKeyCode() == 87 && e.isAltDown() && !ShortcutKeys.isCtrlDown(e) && e.isShiftDown()) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							currentTextEditor.toggleButtonFileTreeNewWorkspace.doClick();
						}
					});
				}

				/*
				 *  REFRESH TREE -> Ctrl+F5
				 */
				else if (e.getKeyCode() == 116 && !e.isAltDown() && ShortcutKeys.isCtrlDown(e) && !e.isShiftDown()) {
					currentTextEditor.mouseListenerTreeFilePanel.getRefreshItem().doClick();
				}

				/*
				 * 	REMOVE SPACES/WORD -> CTRL+[-]
				 */
				else if (e.getKeyCode() == 45 && !e.isAltDown() && ShortcutKeys.isCtrlDown(e) && !e.isShiftDown()) {
					subHeaderPanel.btnRemoveSpaces.doClick();
				}

				/*
				 * OPEN CURRENT FILE'S PROPERTIES -> ALT+ENTER
				 */
				else if (e.getKeyCode() == 10 && e.isAltDown() && !ShortcutKeys.isCtrlDown(e) && !e.isShiftDown()) {
					subHeaderPanel.btnPropertiesPanel.doClick();
				}
				/*
				 * OPEN CURRENT FILE into FyleSystem-> Ctrl+Shift+ENTER
				 */
				else if (e.getKeyCode() == 10 && !e.isAltDown() && ShortcutKeys.isCtrlDown(e) && e.isShiftDown()) {
					try {
						GuiUtilsExtended.openInFileSystem(FileVarious.getParent(currentTextEditor.getCurrentTabEditor().loadedTabObj.loadedFile));
					} catch (Exception e2) {
					}
				}
				/*
				 * OPEN CURRENT FILE into CommandLine -> Ctrl+Alt+ENTER
				 */
				else if (e.getKeyCode() == 10 && e.isAltDown() && ShortcutKeys.isCtrlDown(e) && !e.isShiftDown()) {
					try {
						SysUtils.cdIntoCommandLine(FileVarious.getParent(currentTextEditor.getCurrentTabEditor().loadedTabObj.loadedFile));
					} catch (Exception e2) {
					}
				}

				/*
				 * 	NEXT TAB -> CTRL+TAB
				 */
				else if (e.getKeyCode() == 9 && !e.isAltDown() && ShortcutKeys.isCtrlDown(e) && !e.isShiftDown()) {
					currentTabEditor().goToNextTab();
				}

				/*
				 * 	PREVIOUS TAB -> CTRL+Shift+TAB
				 */
				else if (e.getKeyCode() == 9 && !e.isAltDown() && ShortcutKeys.isCtrlDown(e) && e.isShiftDown()) {
					currentTabEditor().goToPreviousTab();
				}
				/*
				 * 	NEW TAB -> CTRL+N
				 */
				else if (e.getKeyCode() == 78 && !e.isAltDown() && ShortcutKeys.isCtrlDown(e) && !e.isShiftDown()) {
					if (!currentTextEditor.leftFileTree.hasFocus()) {
						currentTabEditor().addTab(null);
					}
				}
				/*
				 * 	CLOSE CURRENT TAB -> CTRL+W
				 */
				else if (e.getKeyCode() == 87 && !e.isAltDown() && ShortcutKeys.isCtrlDown(e) && !e.isShiftDown()) {
					currentTabEditor().closeCurrentTab();
				}
				/*
				 * 	CLOSE ALL TABS -> CTRL+ALT+SHIFT+ [-]
				 */
				else if (e.getKeyCode() == 45 && e.isAltDown() && ShortcutKeys.isCtrlDown(e) && e.isShiftDown()) {
					subHeaderPanel.btnCloseAllTabs.doClick();
				}

				/*
				 *  NEXT TAB IN HYSTORY -> Alt+ ->
				 */
				else if (e.getKeyCode() == 39 && e.isAltDown() && !ShortcutKeys.isCtrlDown(e) && !e.isShiftDown()) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							currentTextEditor.getCurrentTabEditor().goToNextInHistory();
						}
					});
				}

				/*
				 *  NEXT TAB IN HYSTORY -> Alt+ <-
				 */
				else if (e.getKeyCode() == 37 && e.isAltDown() && !ShortcutKeys.isCtrlDown(e) && !e.isShiftDown()) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							currentTextEditor.getCurrentTabEditor().goToPreviousInHistory();
						}
					});
				}

				/*
				 *  SWITCH INLINE TABS ON/OFF -> Ctrl+shift+I
				 */
				else if (e.getKeyCode() == 73 && !e.isAltDown() && ShortcutKeys.isCtrlDown(e) && e.isShiftDown()) {
					currentTextEditor.headerPanel.checkBoxInlineTabs.doClick();
				}

				/*
				 *  ADAPT WIDTH TO SCREEN SIZE -> Ctrl+shift+A
				 */
				else if (e.getKeyCode() == 65 && !e.isAltDown() && ShortcutKeys.isCtrlDown(e) && e.isShiftDown()) {
					currentTextEditor.headerPanel.chkCutWordsTo.doClick();
				}

			/** VISUALIZATION SWITCHER **/

				/*
				 *  HIDE/SHOW header panel -> Ctrl+shift+Alt+H
				 */
				else if (e.getKeyCode() == 72 && e.isAltDown() && ShortcutKeys.isCtrlDown(e) && e.isShiftDown()) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							currentTextEditor.textEditorUtils.hideOrShowHeaderPanelLevel1();
						}
					});
				}

				/*
				 *  HIDE/SHOW subHeader panel -> Ctrl+shift+Alt+U
				 */
				else if (e.getKeyCode() == 85 && e.isAltDown() && ShortcutKeys.isCtrlDown(e) && e.isShiftDown()) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							currentTextEditor.textEditorUtils.hideOrShowHeaderPanelLevel2();
						}
					});
				}

				/*
				 *  HIDE/SHOW footer panel -> Ctrl+shift+Alt+L
				 */
				else if (e.getKeyCode() == 76 && e.isAltDown() && ShortcutKeys.isCtrlDown(e) && e.isShiftDown()) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							currentTextEditor.textEditorUtils.hideOrShowFooterPanel();
						}
					});
				}

			/** END VISUALIZATION SWITCHER **/

				/*
				 *  ON/OFF autoSync language from file type -> Ctrl+shift+L
				 */
				else if (e.getKeyCode() == 76 && !e.isAltDown() && ShortcutKeys.isCtrlDown(e) && e.isShiftDown()) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							subHeaderPanel.chckbxLockLanguage.doClick();
						}
					});
				}


				/*********************************************
				 * Text Editor  -  TEXT SPECIFIC SHORTCUTS
				 *********************************************/

				/*
				 *  CLONE CURRENT LINE UP -> Ctrl+Alt+UP arrow
				 */
				else if (e.getKeyCode() == 38 && e.isAltDown() && ShortcutKeys.isCtrlDown(e) && !e.isShiftDown()) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
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
				else if (e.getKeyCode() == 40 && e.isAltDown() && ShortcutKeys.isCtrlDown(e) && !e.isShiftDown()) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
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
				else if (e.getKeyCode() == 70 && !e.isAltDown() && ShortcutKeys.isCtrlDown(e) && e.isShiftDown()) {
					GuiUtilsExtended.formatTextLater(currentTextEditor.getCurrentTabEditor().loadedTabObj.textArea,
							currentTextEditor.CURRENT_EXT_OUT,
							currentTextEditor.currentConfiguration.getTextEditorOpt().tabSize
					);
				}

				/*
				 *  Copy current with syntax style -> Alt+C
				 */
				else if (e.getKeyCode() == 67 && e.isAltDown() && !ShortcutKeys.isCtrlDown(e) && !e.isShiftDown()) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							currentTextEditor.getCurrentTabEditor().loadedTabObj.textArea.copyAsStyledText();
						}
					});
				}


//				/*
//				 *  TEST MOVE TABS : Ctrl + shift + Alt + [->|<-]
//				 */
//				if(e.getKeyCode() == 37 && e.isAltDown() && ShortcutKeys.isCtrlDown(e) && e.isShiftDown()) {
//					currentTextEditor.moveCurrentTab(true);
//				}
//
//				/*
//				 *  TEST MOVE TABS : Ctrl + shift + Alt + [->|<-]
//				 */
//				if(e.getKeyCode() == 39 && e.isAltDown() && ShortcutKeys.isCtrlDown(e) && e.isShiftDown()) {
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
		return (e.isAltDown() || ShortcutKeys.isCtrlDown(e) || e.isShiftDown());
	}

	public synchronized void doControlActions() {

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (flagCtrlDown.get()) {
					if(flagHoverBtnBase64.get()) {
						subHeaderPanel.btnBase64Conversion.setToolTipText(TextEditTooltips.tooltipDecryptBase64);
						subHeaderPanel.btnBase64Conversion.setIcon(EditorIcons.of(EditorIcons.Kind.BASE64, ToolbarStyle.ICON_SIZE, new java.awt.Color(235, 96, 84)));
					}
				}

				if (!flagCtrlDown.get() || !flagHoverBtnBase64.get()) {
					subHeaderPanel.btnBase64Conversion.setToolTipText(TextEditTooltips.tooltipCryptBase64);
					subHeaderPanel.btnBase64Conversion.setIcon(EditorIcons.of(EditorIcons.Kind.BASE64, ToolbarStyle.ICON_SIZE));
				}

			}
		});

	}

	/** Gives back the text cursor to every text area left with the cross of the rectangular selection (tab changed or window left while Alt was down) */
	private void resetAltCursor() {
		try {
			for (CustomTabbedEditor panel : currentTextEditor.tabbedEditorsList) {
				for (TabTextEditorObject tab : panel.loadedList.getList()) {
					if (tab.textArea != null && tab.textArea.getCursor().getType() == java.awt.Cursor.CROSSHAIR_CURSOR) {
						tab.textArea.setCursor(GuiUtilsExtended.CURSOR_TEXT);
					}
				}
			}
		} catch (Exception e) {
			logger.debug("Cannot reset the cursor of the text areas", e);
		}
	}

	private void updateCtrlAltShiftPressed(final KeyEvent e) {

		/*
		 * CTRL PRESSED FLAG
		 */
		if (ShortcutKeys.isCtrlDown(e)) {
			flagCtrlDown.set(true);
		}else {
			flagCtrlDown.set(false);
		}

		/*
		 * ALT PRESSED FLAG
		 */
		if (e.isAltDown()) {
			flagAltDown.set(true);
			if (!ShortcutKeys.isCtrlDown(e) && !e.isShiftDown()) {
				currentTabEditor().loadedTabObj.textArea.setCursor(GuiUtilsExtended.CURSOR_CROSS);
			}else {
				currentTabEditor().loadedTabObj.textArea.setCursor(GuiUtilsExtended.CURSOR_TEXT);
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
		if (ShortcutKeys.isCtrlDown(e)) {
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
			currentTabEditor().loadedTabObj.textArea.setCursor(GuiUtilsExtended.CURSOR_TEXT);
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

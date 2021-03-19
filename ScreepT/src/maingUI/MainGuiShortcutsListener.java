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
package maingUI;

import java.awt.KeyEventDispatcher;
import java.awt.event.KeyEvent;
import javax.swing.SwingUtilities;

import dialogutils.GenericFileChooserDialog;
import gui.listener.KeyListenerParent;

public class MainGuiShortcutsListener extends KeyListenerParent implements KeyEventDispatcher {
	
	MainGUI currentGui;
	
	public MainGuiShortcutsListener(MainGUI currentMainGui) {
		super();
		logger.debug(this.getClass().getName() + " Started!");
		currentGui = currentMainGui;
	}
	
	@Override
	  public boolean dispatchKeyEvent(final KeyEvent e) {
		
		if(GenericFileChooserDialog.blockEventsModal.get()) {
			return false;
		}
		
		updateKeyCode(e);
	    
//////////////// LISTENER FOR KEY PRESSED EVENT - ONLY WHEN IS CORRECT TO CONTINUE FIRE EVENT WHILE PRESSISNG  //////////////////////////
		
		if (e.getID() == KeyEvent.KEY_PRESSED) {
			
			// WIN + ARROW Pressed: trigger resize
//			if(isOtherPressed(KeyEvent.VK_WINDOWS) && isArrowLR(e.getKeyCode())) {
//				currentGui.resizeMainFrameDefaults();
//				return true;
//			}
			
			/*
			 *  EXIT TOGGLE MODE -> ESC and TOGGLE MODE ACTIVE
			 */
			if(e.getKeyCode() == 27 && currentGui.isImmersiveModeOn.get()) {
				try {
					if (MainGUI.mainFrameScreept.isFocused()) {
						if (MainGUI.mainGuiUtils.isTextEditorActive()) {
							MainGUI.textEditorPanel.textEditorUtils.setImmersiveMode(false);
						} else {
							MainGUI.mainGuiUtils.immersiveModeToggle();
						}
						logger.debug("Immersive mode exit triggered with \"esc\": current mainFrame is active!");
					} else {
						logger.warn("Immersive mode esc skipped: current mainFrame is not active!");
					}
				} catch (Exception e2) {
					logger.error("Immersive mode esc skipped: an error occurred!", e2);
				}
			}
			
			/*
			 *  EXIT - FULL SCREEN WITHOUT IMMERSIVE MODE
			 */
			if(e.getKeyCode() == 27 && currentGui.isFullScreen.get() && !currentGui.isImmersiveModeOn.get()) {
				try {
					if (MainGUI.mainFrameScreept.isFocused()) {
						MainGUI.mainAPP.mntmFullScreenMode.doClick();
						logger.debug("FullScreenMode exit triggered with \"esc\": current mainFrame is active!");
					} else {
						logger.warn("FullScreenMode esc skipped: current mainFrame is not active!");
					}
				} catch (Exception e2) {
					logger.error("Immersive mode esc skipped: an error occurred!", e2);
				}
			}

		}
		
////////////////LISTENER FOR KEY RELEASED EVENT - WHEN NEED TO TRIGGER ONLY ONE TIME EVEN WHILE KEEPING PRESSED  //////////////////////////
		
		if (e.getID() == KeyEvent.KEY_RELEASED) {
			
			// HERE SHORTCUTS KEY WITH CTR|ALT|SHIFT
			if (isShortcutKey(e)) {
				
				/**
				 *  TOGGLE FULL SCREEN : Alt+Shift+Enter
				 */
				if (e.getKeyCode() == 10 && e.isAltDown() && !e.isControlDown() && e.isShiftDown()) {
					currentGui.mntmFullScreenMode.doClick();
				}
				
				/*
				 *  IMMERSIVE MODE ON/OFF -> Ctrl+shift+Alt+M
				 */
				else if(e.getKeyCode() == 77 && e.isAltDown() && !e.isControlDown() && e.isShiftDown()) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							MainGUI.mainGuiUtils.immersiveModeToggle();
						}
					});
				}

				
			// FIRST MENU SECTION - FILE //
				
				/**
				 *  OPEN CONFIG-MANAGER : ALT+SHIFT+1
				 */
				else if (e.getKeyCode() == 49 && e.isAltDown() && !e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmInspectCurrentConfiguration().doClick();
				}
				
				/**
				 *  EXPORT CURRENT CONFIGURATION : Ctrl+Alt+Shift+E
				 */
				else if (e.getKeyCode() == 69 && e.isAltDown() && e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmExportPrjSub().doClick();
				}
				
				/**
				 *  RELOAD CURRENT CONFIGURATION : Ctrl+Alt+Shift+R
				 */
				else if (e.getKeyCode() == 82 && e.isAltDown() && e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmReloadPrjSub().doClick();
				}

				/**
				 *  LOAD CUSTOM CONFIGURATION : Ctrl+Alt+Shift+O
				 */
				else if (e.getKeyCode() == 79 && e.isAltDown() && e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmloadPrjSub().doClick();
				}
				
				/**
				 *  SAVE CURRENT CONFIGURATION : Ctrl+Alt+Shift+S
				 */
				else if (e.getKeyCode() == 83 && e.isAltDown() && e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmSaveCurrent().doClick();
				}

				/**
				 *  SAVE CURRENT CONFIGURATION AS: Ctrl+Alt+Shift+W
				 */
				else if (e.getKeyCode() == 87 && e.isAltDown() && e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmSaveCurrentAs().doClick();
				}
				
				/**
				 *  LOAD BUILT-IN DEFAULT CONFIGURATION: Ctrl+Alt+Shift+D
				 */
				else if (e.getKeyCode() == 68 && e.isAltDown() && e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmloadSrcrYptDefaultConf().doClick();
				}
				
			// SECOND MENU SECTION - OPTIONS //
				
				/**
				 *  OPEN GENERAL OPTIONS : ALT+SHIFT+2
				 */
				else if (e.getKeyCode() == 50 && e.isAltDown() && !e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmGenOpt().doClick();
				}
				/**
				 *  OPEN GUI OPTIONS : ALT+SHIFT+3
				 */
				else if (e.getKeyCode() == 51 && e.isAltDown() && !e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmCustomizeUIView().doClick();
				}
				
			// THIRD MENU SECTION - SCREEPT //	
				
				/**
				 *  OPEN CRYPTER : ALT+SHIFT+S
				 */
				else if (e.getKeyCode() == 83 && e.isAltDown() && !e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmCrypter().doClick();
				}
				
				/**
				 *  OPEN KEY-GEN : ALT+SHIFT+K
				 */
				else if (e.getKeyCode() == 75 && e.isAltDown() && !e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmgenerateKeyImageSub().doClick();
				}
				
				/**
				 *  OPEN PSW-KEY-GEN : ALT+SHIFT+K
				 */
				else if (e.getKeyCode() == 75 && e.isAltDown() && e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmgenerateKeyPswSub().doClick();
				}

				/**
				 *  OPEN KEY-DICIONARY : ALT+SHIFT+D
				 */
				else if (e.getKeyCode() == 68 && e.isAltDown() && !e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmViewKeyDictionary().doClick();
				}
				
			// FOURTH MENU SECTION - VARIOUS //
				
				/**
				 *  OPEN TEXT-EDITOR : ALT+SHIFT+T
				 */
				else if (e.getKeyCode() == 84 && e.isAltDown() && !e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmTextEditor().doClick();
				}
				
				/*
				 * 	Fast file search -> CTRL+ALT+R
				 */
				else if (e.getKeyCode() == KeyEvent.VK_R && e.isAltDown() && e.isControlDown() && !e.isShiftDown()) {
					MainGUI.mainGuiUtils.openQuickSearchIntoWorkspace();
				}
				
				/**
				 * OPEN FILE-CONTENT-SEARCH - Ctrl+Alt+F
				 */
				else if(e.getKeyCode() == KeyEvent.VK_F && e.isAltDown() && e.isControlDown() && !e.isShiftDown()) {
					currentGui.windowCreator.fileContentSearch();
				}

				/**
				 * OPEN SHELL FRAME - Ctrl+Alt+S
				 */
				else if(e.getKeyCode() == KeyEvent.VK_S && e.isAltDown() && e.isControlDown() && !e.isShiftDown()) {
					currentGui.windowCreator.newShellFrame(true);
				}
				
				/*
				 *  OPEN CALCULATOR -> Ctrl+Alt+C
				 */
				else if (e.getKeyCode() == 67 && e.isAltDown() && e.isControlDown() && !e.isShiftDown()) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							currentGui.mntmCalculator.doClick();
						}
					});
				}
				
				/**
				 *  OPEN IMPORT/EXPORT crypted workspace: TEXT-EDITOR : CTRL+ALT+SHIFT+I
				 */
				else if (e.getKeyCode() == 73 && e.isAltDown() && e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmTxtEditorImpExpCryptedWkspc().doClick();
				}

				/**
				 *  OPEN SYS-INFO : ALT+SHIFT+Y
				 */
				else if (e.getKeyCode() == 89 && e.isAltDown() && !e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmhwInfo().doClick();
				}
				/**
				 *  OPEN NET-INFO : ALT+SHIFT+N
				 */
				else if (e.getKeyCode() == 78 && e.isAltDown() && !e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmnetInfo().doClick();
				}

				/**
				 *  OPEN HASH CHECKER PANEL : ALT+SHIFT+U
				 */
				else if (e.getKeyCode() == 85 && e.isAltDown() && !e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmHashChecker().doClick();
				}
				
				/*
				 *  OPEN COLOR SAMPLER -> Ctrl+Alt+Shift+P
				 */
				else if(e.getKeyCode() == 80 && e.isAltDown() && e.isControlDown() && e.isShiftDown()) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							currentGui.getMntmColorPicker().doClick();
						}
					});
				}

				/*
				 *  CLIPBOARDS StANDALONE PANEL -> Alt+Shift+P
				 */
				else if(e.getKeyCode() == 80 && e.isAltDown() && !e.isControlDown() && e.isShiftDown()) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							currentGui.getMntmClipboardStandalone().doClick();
						}
					});
				}
				
				// MEDIA - CONVERTERS //
				/**
				 *  OPEN IMAGE-VIEWER : ALT+SHIFT+O
				 */
				else if (e.getKeyCode() == KeyEvent.VK_O && e.isAltDown() && !e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmImageViewer().doClick();
				}

				/**
				 *  OPEN IMAGE-CONVERTER : ALT+SHIFT+I
				 */
				else if (e.getKeyCode() == 73 && e.isAltDown() && !e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmImageConverter().doClick();
				}
				
				// FILE & FOLDER ACTIONS
				/**
				 *  UPPERCASE DIRECTORY : ALT+SHIFT+0
				 */
				else if (e.getKeyCode() == 48 && e.isAltDown() && !e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmTouppercaseDirectory().doClick();
				}

				/**
				 *  MAKE DIRS FROM PATH : CTRL+ALT+SHIFT+9
				 */
				else if (e.getKeyCode() == 57 && e.isAltDown() && !e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmMakeDirsFromPath().doClick();
				}
				
				/**
				 *  COPY DIR STRUCTURE : CTRL+ALT+SHIFT+8
				 */
				else if (e.getKeyCode() == 56 && e.isAltDown() && !e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmCopyDirStructure().doClick();
				}
				
				/**
				 *  DELETE DIR CONTENT BY EXTENSION : CTRL+ALT+SHIFT+7
				 */
				else if (e.getKeyCode() == 55 && e.isAltDown() && !e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmDeleteDirectoryContentByExtension().doClick();
				}
				
				/**
				 *  DELETE ALL DIR CONTENT : CTRL+ALT+SHIFT+6
				 */
				else if (e.getKeyCode() == 54 && e.isAltDown() && !e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmDeleteDirectoryAllContentRecursive().doClick();
				}
				
				/**
				 *  EMPTY FOLDERS CONTENT : CTRL+ALT+SHIFT+5
				 */
				else if (e.getKeyCode() == 53 && e.isAltDown() && !e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmEmptyDirectoryRecursive().doClick();
				}
				
			// FIFTH MENU SECTION - HELP //
				
				/**
				 *  OPEN ABOUT INFO : ALT+SHIFT+.
				 */
				else if (e.getKeyCode() == 46 && e.isAltDown() && !e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmAboutScreept().doClick();
				}
				
				/**
				 *  EXPORT LOGS : ALT+SHIFT+E
				 */
				else if (e.getKeyCode() == 69 && e.isAltDown() && !e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmExportLog().doClick();
				}
				
				/**
				 *  ERASE LOGS : ALT+SHIFT+CANC
				 */
				else if (e.getKeyCode() == 127 && e.isAltDown() && !e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmEraseLogs().doClick();
				}
				
				/**
				 *  RETURN TO HOME PAGE : ALT+SHIFT+H
				 */
				else if (e.getKeyCode() == 72 && e.isAltDown() && !e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmReturnToWelcome().doClick();
				} 
				
				/**
				 *  VIEW WEBSITE : CTRL+ALT+SHIFT+4
				 */
				else if (e.getKeyCode() == 52 && e.isAltDown() && e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmViewWebsite().doClick();
				} 
				
				/**
				 *  LOOK FOR UPDATES : CTRL+ALT+SHIFT+5
				 */
				else if (e.getKeyCode() == 53 && e.isAltDown() && e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmLookForUpdates().doClick();
				} 

				/**
				 *  LOOK FOR PLUGINS : CTRL+ALT+SHIFT+6
				 */
				else if (e.getKeyCode() == 54 && e.isAltDown() && e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmLookForPlugins().doClick();
				} 
				
				/**
				 *  OPEN MANUAL : ALT+SHIFT+Q
				 */
				else if (e.getKeyCode() == 81 && e.isAltDown() && !e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmUserDocumentation().doClick();
				}
				
				// RESET PANELS SHORTCUTS // 
				
				
				/**
				 *  RESET ALL : ALT+SHIFT+0
				 */
				else if (e.getKeyCode() == 48 && e.isAltDown() && e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmResetAllPanels().doClick();
				}
				
				/**
				 *  RESET TXT EDITOR : ALT+SHIFT+9
				 */
				else if (e.getKeyCode() == 57 && e.isAltDown() && e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmResetTextEditor().doClick();
				}
				
				/**
				 *  RESET CRYPTER : ALT+SHIFT+8
				 */
				else if (e.getKeyCode() == 56 && e.isAltDown() && e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmResetCrypterPanel().doClick();
				}
				
				/**
				 *  RESET CONFIG MANAGER : ALT+SHIFT+7
				 */
				else if (e.getKeyCode() == 55 && e.isAltDown() && e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmResetConfigManager().doClick();
				}
			}
		}
	    
		// Pass the KeyEvent to the next KeyEventDispatcher in the chain
	    return false;
	}
	
}

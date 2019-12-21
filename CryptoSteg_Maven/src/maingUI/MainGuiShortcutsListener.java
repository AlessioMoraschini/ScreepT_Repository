package maingUI;

import java.awt.KeyEventDispatcher;
import java.awt.event.KeyEvent;

import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

public class MainGuiShortcutsListener implements KeyEventDispatcher{
	
	// TextFileWriter logger creation
	static Logger logger = Logger.getLogger(MainGuiShortcutsListener.class);
	
	MainGUI currentGui;
	
	public MainGuiShortcutsListener(MainGUI currentMainGui) {
		logger.debug(this.getClass().getName() + " Started!");
		currentGui = currentMainGui;
	}
	
	@Override
	  public boolean dispatchKeyEvent(final KeyEvent e) {
	    
//////////////// LISTENER FOR KEY PRESSED EVENT - ONLY WHEN IS CORRECT TO CONTINUE FIRE EVENT WHILE PRESSISNG  //////////////////////////
		
		if (e.getID() == KeyEvent.KEY_PRESSED) {
			
			/*
			 *  EXIT TOGGLE MODE -> ESC and TOGGLE MODE ACTIVE
			 */
			if(e.getKeyCode() == 27 && currentGui.isImmersiveModeOn.get()) {
				try {
					if (MainGUI.frmScryptV.isFocused()) {
						if (MainGUI.mainGuiUtils.isTextEditorActive()) {
							MainGUI.initTxtWriter.textEditorUtils.setImmersiveMode(false);
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
					if (MainGUI.frmScryptV.isFocused()) {
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
				if(e.getKeyCode() == 77 && e.isAltDown() && e.isControlDown() && e.isShiftDown()) {
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
				if (e.getKeyCode() == 49 && e.isAltDown() && !e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmInspectCurrentConfiguration().doClick();
				}
				
				/**
				 *  EXPORT CURRENT CONFIGURATION : Ctrl+Alt+Shift+E
				 */
				if (e.getKeyCode() == 69 && e.isAltDown() && e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmExportPrjSub().doClick();
				}
				
				/**
				 *  RELOAD CURRENT CONFIGURATION : Ctrl+Alt+Shift+R
				 */
				if (e.getKeyCode() == 82 && e.isAltDown() && e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmReloadPrjSub().doClick();
				}

				/**
				 *  LOAD CUSTOM CONFIGURATION : Ctrl+Alt+Shift+O
				 */
				if (e.getKeyCode() == 79 && e.isAltDown() && e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmloadPrjSub().doClick();
				}
				
				/**
				 *  SAVE CURRENT CONFIGURATION : Ctrl+Alt+Shift+S
				 */
				if (e.getKeyCode() == 83 && e.isAltDown() && e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmSaveCurrent().doClick();
				}

				/**
				 *  SAVE CURRENT CONFIGURATION AS: Ctrl+Alt+Shift+W
				 */
				if (e.getKeyCode() == 87 && e.isAltDown() && e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmSaveCurrentAs().doClick();
				}
				
				/**
				 *  LOAD BUILT-IN DEFAULT CONFIGURATION: Ctrl+Alt+Shift+D
				 */
				if (e.getKeyCode() == 68 && e.isAltDown() && e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmloadSrcrYptDefaultConf().doClick();
				}
				
			// SECOND MENU SECTION - OPTIONS //
				
				/**
				 *  OPEN GENERAL OPTIONS : ALT+SHIFT+2
				 */
				if (e.getKeyCode() == 50 && e.isAltDown() && !e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmGenOpt().doClick();
				}
				/**
				 *  OPEN GUI OPTIONS : ALT+SHIFT+3
				 */
				if (e.getKeyCode() == 51 && e.isAltDown() && !e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmCustomizeUIView().doClick();
				}
				
			// THIRD MENU SECTION - SCREEPT //	
				
				/**
				 *  OPEN CRYPTER : ALT+SHIFT+S
				 */
				if (e.getKeyCode() == 83 && e.isAltDown() && !e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmCrypter().doClick();
				}
				
				/**
				 *  OPEN KEY-GEN : ALT+SHIFT+K
				 */
				if (e.getKeyCode() == 75 && e.isAltDown() && !e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmgenerateKeyImageSub().doClick();
				}
				
				/**
				 *  OPEN PSW-KEY-GEN : ALT+SHIFT+W
				 */
				if (e.getKeyCode() == 87 && e.isAltDown() && !e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmgenerateKeyPswSub().doClick();
				}

				/**
				 *  OPEN KEY-DICIONARY : ALT+SHIFT+D
				 */
				if (e.getKeyCode() == 68 && e.isAltDown() && !e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmViewKeyDictionary().doClick();
				}
				
			// FOURTH MENU SECTION - VARIOUS //
				
				/**
				 *  OPEN TEXT-EDITOR : ALT+SHIFT+T
				 */
				if (e.getKeyCode() == 84 && e.isAltDown() && !e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmTextEditor().doClick();
				}

				/**
				 *  OPEN SYS-INFO : ALT+SHIFT+Y
				 */
				if (e.getKeyCode() == 89 && e.isAltDown() && !e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmhwInfo().doClick();
				}
				/**
				 *  OPEN NET-INFO : ALT+SHIFT+N
				 */
				if (e.getKeyCode() == 78 && e.isAltDown() && !e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmnetInfo().doClick();
				}

				/**
				 *  OPEN HASH CHECKER PANEL : ALT+SHIFT+U
				 */
				if (e.getKeyCode() == 85 && e.isAltDown() && !e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmHashChecker().doClick();
				}
				
				/*
				 *  OPEN COLOR SAMPLER -> Ctrl+Alt+Shift+P
				 */
				if(e.getKeyCode() == 80 && e.isAltDown() && e.isControlDown() && e.isShiftDown()) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							currentGui.getMntmColorPicker().doClick();
						}
					});
				}

				/*
				 *  CLIPBOARDS StANDALONE PANEL -> Alt+Shift+P
				 */
				if(e.getKeyCode() == 80 && e.isAltDown() && !e.isControlDown() && e.isShiftDown()) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							currentGui.getMntmClipboardStandalone().doClick();
						}
					});
				}
				
				// MEDIA - CONVERTERS //
				/**
				 *  OPEN IMAGE-CONVERTER : ALT+SHIFT+I
				 */
				if (e.getKeyCode() == 73 && e.isAltDown() && !e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmImageConverter().doClick();
				}
				
				// FILE & FOLDER ACTIONS
				/**
				 *  UPPERCASE DIRECTORY : ALT+SHIFT+0
				 */
				if (e.getKeyCode() == 48 && e.isAltDown() && !e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmTouppercaseDirectory().doClick();
				}

				/**
				 *  MAKE DIRS FROM PATH : CTRL+ALT+SHIFT+9
				 */
				if (e.getKeyCode() == 57 && e.isAltDown() && !e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmMakeDirsFromPath().doClick();
				}
				
				/**
				 *  COPY DIR STRUCTURE : CTRL+ALT+SHIFT+8
				 */
				if (e.getKeyCode() == 56 && e.isAltDown() && !e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmCopyDirStructure().doClick();
				}
				
				/**
				 *  DELETE DIR CONTENT BY EXTENSION : CTRL+ALT+SHIFT+7
				 */
				if (e.getKeyCode() == 55 && e.isAltDown() && !e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmDeleteDirectoryContentByExtension().doClick();
				}
				
				/**
				 *  DELETE ALL DIR CONTENT : CTRL+ALT+SHIFT+6
				 */
				if (e.getKeyCode() == 54 && e.isAltDown() && !e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmDeleteDirectoryAllContentRecursive().doClick();
				}
				
				/**
				 *  EMPTY FOLDERS CONTENT : CTRL+ALT+SHIFT+5
				 */
				if (e.getKeyCode() == 53 && e.isAltDown() && !e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmEmptyDirectoryRecursive().doClick();
				}
				
			// FIFTH MENU SECTION - HELP //
				
				/**
				 *  OPEN ABOUT INFO : ALT+SHIFT+.
				 */
				if (e.getKeyCode() == 46 && e.isAltDown() && !e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmAboutScreept().doClick();
				}
				
				/**
				 *  EXPORT LOGS : ALT+SHIFT+E
				 */
				if (e.getKeyCode() == 69 && e.isAltDown() && !e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmExportLog().doClick();
				}
				
				/**
				 *  ERASE LOGS : ALT+SHIFT+CANC
				 */
				if (e.getKeyCode() == 127 && e.isAltDown() && !e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmEraseLogs().doClick();
				}
				
				/**
				 *  RETURN TO HOME PAGE : ALT+SHIFT+H
				 */
				if (e.getKeyCode() == 72 && e.isAltDown() && !e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmReturnToWelcome().doClick();
				} 
				
				/**
				 *  VIEW WEBSITE : CTRL+ALT+SHIFT+4
				 */
				if (e.getKeyCode() == 52 && e.isAltDown() && e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmViewWebsite().doClick();
				} 
				
				/**
				 *  LOOK FOR UPDATES : CTRL+ALT+SHIFT+5
				 */
				if (e.getKeyCode() == 53 && e.isAltDown() && e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmLookForUpdates().doClick();
				} 

				/**
				 *  LOOK FOR PLUGINS : CTRL+ALT+SHIFT+6
				 */
				if (e.getKeyCode() == 54 && e.isAltDown() && e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmLookForPlugins().doClick();
				} 
				
				/**
				 *  OPEN MANUAL : ALT+SHIFT+Q
				 */
				if (e.getKeyCode() == 81 && e.isAltDown() && !e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmUserDocumentation().doClick();
				}
				
				// RESET PANELS SHORTCUTS // 
				
				
				/**
				 *  RESET ALL : ALT+SHIFT+0
				 */
				if (e.getKeyCode() == 48 && e.isAltDown() && e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmResetAllPanels().doClick();
				}
				
				/**
				 *  RESET TXT EDITOR : ALT+SHIFT+9
				 */
				if (e.getKeyCode() == 57 && e.isAltDown() && e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmResetTextEditor().doClick();
				}
				
				/**
				 *  RESET CRYPTER : ALT+SHIFT+8
				 */
				if (e.getKeyCode() == 56 && e.isAltDown() && e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmResetCrypterPanel().doClick();
				}
				
				/**
				 *  RESET CONFIG MANAGER : ALT+SHIFT+7
				 */
				if (e.getKeyCode() == 55 && e.isAltDown() && e.isControlDown() && e.isShiftDown()) {
					currentGui.getMntmResetConfigManager().doClick();
				}
			}
		}
	    
		// Pass the KeyEvent to the next KeyEventDispatcher in the chain
	    return false;
	}
	
	private boolean isShortcutKey(KeyEvent e) {
		return (e.isAltDown() || e.isControlDown() || e.isShiftDown());
	}
}

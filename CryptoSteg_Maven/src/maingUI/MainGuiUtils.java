package maingUI;

import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.event.ComponentListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import amfileutils.FileStuff;
import frameutils.utils.GuiUtils;
import gUI.panels.ChargeIniLog;
import gUI.panels.CrypterPanel;
import gUI.panels.TextEditor;
import mainobj.INItializer;
import media.Sounds;
import resources.GeneralConfig;
import resources.SoundsConfigurator;

public class MainGuiUtils {
	
	// MainGuiUtils logger
	static Logger logger = Logger.getLogger(MainGuiUtils.class);

	public MainGUI mainAppReference;
	
	public MainGuiUtils(MainGUI mainApp) {
		mainAppReference = mainApp;
	}
	
	public <T> void immersiveModeToggle() {
		JFrame target = MainGUI.frmScryptV;
		
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				// start correct panel
				if(ChargeIniLog.class.equals(target.getContentPane().getComponent(0).getClass())) {
					mainAppReference.mntmFullScreenMode.doClick();
				
				}else if(TextEditor.class.equals(target.getContentPane().getComponent(0).getClass()) && MainGUI.initTxtWriter != null) {
					MainGUI.initTxtWriter.textEditorUtils.toggleImersiveMode();
				
				}else if(CrypterPanel.class.equals(target.getContentPane().getComponent(0).getClass())) {
					mainAppReference.mntmFullScreenMode.doClick();
				
				}else {
					mainAppReference.mntmFullScreenMode.doClick();
				}
			}
		});
	}
	
	/**
	 * Use this function to reset panel to initial state
	 * @param <T>
	 * @param target if null all will be resetted, else use this to specify class to reset
	 */
	public <T> void resetPanel(Class<T>target, JFrame instance) {
		final boolean txtEditor, crypter, configuration, home;
		
		String targetStr = (target != null)? target.getName() : "All Panels";
		logger.info(" Starting reset -> " +targetStr);
		
		txtEditor = (instance.getContentPane().getComponent(0).equals(MainGUI.initTxtWriter))? true : false;
		crypter = (instance.getContentPane().getComponent(0).equals(MainGUI.panelCrypter))? true : false;
		configuration = (instance.getContentPane().getComponent(0).equals(mainAppReference.configManagerPanel))? true : false;
		home = (!txtEditor && !crypter && !configuration)? true : false;
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				
				// start correct panel
				if(ChargeIniLog.class.equals(target)) {
					startConfigManagerPanel(true);
				}else if(TextEditor.class.equals(target)) {
					startTextEditor(true);
				}else if(CrypterPanel.class.equals(target)) {
					startCrypterPanel(true);
				}else if(target == null) {
					startConfigManagerPanel(true);
					startCrypterPanel(true);
					startTextEditor(true);
				}
		
				// Now select last selected before refresh
				if (txtEditor) {
					mainAppReference.getMntmTextEditor().doClick();
				} else if (crypter) {
					mainAppReference.getMntmCrypter().doClick();
				} else if (configuration) {
					mainAppReference.getMntmInspectCurrentConfiguration().doClick();
				} else if (home) {
					mainAppReference.getMntmReturnToWelcome().doClick();
				}

				mainAppReference.menuBar.revalidate();
				mainAppReference.menuBar.repaint();
			}
		});
		
		
		logger.info("Reset finished [" +targetStr + "]");
	}
	
	public void startConfigManagerPanel(boolean forceStart) {
		
		mainAppReference.getMainFrame().setCursor(GuiUtils.CURSOR_WAIT);
		
		mainAppReference.isOnHomePage.set(false);
		
		JPanel contentPane = (JPanel) mainAppReference.getMainFrame().getContentPane();

		// reset all listeners
		if (forceStart) {
			removeAllListeners(mainAppReference.configManagerPanel);
		}
		
		if (mainAppReference.configManagerPanel == null || forceStart) {
			Sounds.playSound(SoundsConfigurator.SOUND_POOL_SHOT, null);
			mainAppReference.configManagerPanel = new ChargeIniLog(MainGUI.currentConfiguration, mainAppReference);
		}
		
		mainAppReference.configManagerPanel.splitPane.setDividerLocation(300);
		mainAppReference.configManagerPanel.textArea.revalidate();
		mainAppReference.configManagerPanel.textArea.repaint();
		
		contentPane.removeAll();
    	contentPane.add(mainAppReference.configManagerPanel);
    	contentPane.revalidate(); 
    	contentPane.repaint();
    	
    	MainGUI.currentConfiguration.getGenOpt().setLastPanelOpened(ChargeIniLog.class);
    	MainGUI.currentConfiguration.writeSingleValue(
		INItializer.GEN_OPT, "lastPanelOpened", ChargeIniLog.class.getCanonicalName());
    	
    	resetMainAppCursor();
	}
	
	public void startCrypterPanel(final boolean forceStart) {
		
		mainAppReference.isOnHomePage.set(false);
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				mainAppReference.getMainFrame().setCursor(GuiUtils.CURSOR_WAIT);
				final JPanel contentPane = (JPanel) mainAppReference.getMainFrame().getContentPane();
				// reset all listeners
				if (forceStart) {
					removeAllListeners(MainGUI.panelCrypter);
				}
				if (MainGUI.panelCrypter == null || forceStart) {
					Sounds.playSound(SoundsConfigurator.SOUND_POOL_SHOT, null);
					MainGUI.panelCrypter = new CrypterPanel(MainGUI.currentConfiguration, mainAppReference);
				}
				
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						contentPane.removeAll();
						contentPane.add(MainGUI.panelCrypter);
						contentPane.revalidate();
						contentPane.repaint();
						MainGUI.currentConfiguration.getGenOpt().setLastPanelOpened(CrypterPanel.class);
						MainGUI.currentConfiguration.writeSingleValue(INItializer.GEN_OPT, "lastPanelOpened", CrypterPanel.class.getCanonicalName());
					}
				});
				
				resetMainAppCursor();
			}
		});
	}
	
	public void startTextEditor(final boolean forceRestart) {
		
		mainAppReference.isOnHomePage.set(false);
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				mainAppReference.getMainFrame().setCursor(GuiUtils.CURSOR_WAIT);
				final JPanel contentPane = (JPanel) mainAppReference.getMainFrame().getContentPane();
				// reset shortcut listener
				if (forceRestart && MainGUI.initTxtWriter != null && MainGUI.initTxtWriter.shortcutsListener != null) {
					KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(MainGUI.initTxtWriter.shortcutsListener);
					removeAllListeners(MainGUI.panelCrypter);
				}
				if (MainGUI.initTxtWriter == null || forceRestart) {
					Sounds.playSound(SoundsConfigurator.SOUND_POOL_SHOT, null);
					MainGUI.initTxtWriter = new TextEditor(mainAppReference, MainGUI.currentConfiguration);
				}
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						contentPane.removeAll();
						contentPane.add(MainGUI.initTxtWriter);
						contentPane.revalidate();
						contentPane.repaint();
						MainGUI.updateGuiFromCurrConfig();
					}
				});
				
				resetMainAppCursor();
				MainGUI.currentConfiguration.getGenOpt().setLastPanelOpened(TextEditor.class);
				MainGUI.currentConfiguration.writeSingleValue(INItializer.GEN_OPT, "lastPanelOpened", TextEditor.class.getCanonicalName());
				
			}
		});
	}
	
	public Runnable tempDecryptedEraser() {
		return new Runnable() {
			
			@Override
			public void run() {
				File[] decryptedList = GeneralConfig.TEMP_DECRIPTED_ROOT.listFiles();
				if (decryptedList!= null && decryptedList.length!=0) {
					// while there are temp decripted delete them
					for (File temp : decryptedList) {
						FileUtils.deleteQuietly(temp);
					} 
				}	
				// now delete temp folder, and retry if something has not been deleted
				FileStuff.deleteDirContentInclRoot(GeneralConfig.TEMP_DECRIPTED_ROOT);
			}
		};
	}

	public Runnable tempFilesEraser() {
		return new Runnable() {
			
			@Override
			public void run() {
				File[] decryptedList = GeneralConfig.TEMP_DIRECTORY_FILE.listFiles();
				if (decryptedList!= null && decryptedList.length!=0) {
					// while there are temp decripted delete them
					for (File temp : decryptedList) {
						FileUtils.deleteQuietly(temp);
					} 
				}	
				// now delete temp folder, and retry if something has not been deleted
				FileStuff.deleteDirContentRecursive(GeneralConfig.TEMP_DIRECTORY_FILE);
			}
		};
	}
	
	/**
	 * Use this to retrieve all panels thare are restartable using the method
	 * ResetPanel(String className) -> just use a class name from the given arrayList or ALL
	 * the reset all panels
	 * @return
	 */
	@Deprecated
	public static ArrayList<String> getResettablePanels() {
		ArrayList<String> resettablePanels = new ArrayList<String>();
		resettablePanels.add(TextEditor.class.getSimpleName());
		resettablePanels.add(CrypterPanel.class.getSimpleName());
		resettablePanels.add(ChargeIniLog.class.getSimpleName());
		return resettablePanels;
	}
	
	public void removeAllListeners(Component target) {
		if(target == null) {
			return;
		}
		ComponentListener[] listeners = target.getComponentListeners();
		for(ComponentListener listener : listeners) {
			target.removeComponentListener(listener);
		}
		
	}
	
	public void resetMainAppCursor() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				mainAppReference.getMainFrame().setCursor(GuiUtils.CURSOR_DEFAULT);
			}
		});
	}
	
	public void removeKeyListeners(Component target) {
		if(target == null) {
			return;
		}
		KeyListener[] listeners = target.getKeyListeners();
		for(KeyListener listener : listeners) {
			target.removeKeyListener(listener);
		}
	}

	public void removeMouseListeners(Component target) {
		if(target == null) {
			return;
		}
		MouseListener[] listeners = target.getMouseListeners();
		for(MouseListener listener : listeners) {
			target.removeMouseListener(listener);
		}
	}

	public void removeMouseWheelListeners(Component target) {
		if(target == null) {
			return;
		}
		MouseWheelListener[] listeners = target.getMouseWheelListeners();
		for(MouseWheelListener listener : listeners) {
			target.removeMouseWheelListener(listener);
		}
	}

	public static void welcomeMethodIfFirstTime() {
		// ASK FOR USERNAME IF NULL OR FIRST TIME
		INItializer currentConfiguration = MainGUI.getCurrentConfInterface().getInitializer();
		if(currentConfiguration.getGenOpt().getUserName().equals("") || currentConfiguration.getGenOpt().getUserName().length()>20) {
			logger.info("Current user input initialization: void user or lenght>20 detected!");
			currentConfiguration.getGenOpt().setUserName(askUsername());
			currentConfiguration.writeSingleValue(INItializer.GEN_OPT, "userName", currentConfiguration.getGenOpt().getUserName());
			logger.info("Current userName: " + currentConfiguration.getGenOpt().getUserName());
		}
	}
	
	public static String askUsername() {
		INItializer currentConfiguration = MainGUI.getCurrentConfInterface().getInitializer();
		boolean end=false;
		String input="";
		do {
			input = MainGUI.dialogHelper.askForStringUnsafe("Please insert your name:", "Hi, what's your name? :)", "U-sir");
			
			if (input==null || input.equals("") || input.length() > 20) {
				JOptionPane.showMessageDialog(null, "Username must be between 1-20 characters!",
						"UserName lenght Error", JOptionPane.WARNING_MESSAGE);
				logger.error("Error in user input, redo.");
			} else {
				currentConfiguration.writeSingleValue(INItializer.GEN_OPT, "userName", input);
				currentConfiguration.getGenOpt().setUserName(input);
				end = true;
			} 
		} while (!end);
		logger.info("Name selected and saved username: "+input);
		return input;
	}
	
}

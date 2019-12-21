package maingUI;

import static maingUI.MainGuiGeneralAppStarter.*;
import java.awt.Component;
import java.awt.Frame;
import java.awt.KeyboardFocusManager;
import java.awt.event.ComponentListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import amfileutils.FileStuff;
import amfileutils.filechangewatcher.ChangeDTO;
import amfileutils.filechangewatcher.FileChangeMonitor;
import dialogutils.JOptionHelper;
import exception.ProgressBarInterruptedException;
import frameutils.utils.GuiUtils;
import gUI.panels.ChargeIniLog;
import gUI.panels.CrypterPanel;
import gUI.panels.TextEditor;
import guistyle.GUIStyelSettings;
import mainobj.INItializer;
import media.Sounds;
import om.GenericOption;
import om.SupportedFileTextEditor;
import resources.GeneralConfig;
import resources.PropertiesManager;
import resources.SoundsConfigurator;

public class MainGuiUtils {
	
	// MainGuiUtils logger
	static Logger logger = Logger.getLogger(MainGuiUtils.class);

	public MainGUI mainAppReference;

	public MainGuiUtils(MainGUI mainApp) {
		mainAppReference = mainApp;
	}
	
	public static Thread lookForUpdates(final boolean silent) {
		return new Thread(new Runnable() {
			
			@Override
			public void run() {
				Frame activeWindowBeforeUpdates = GuiUtils.getTopFrame();
				
				if (!MainGUI.mainAPP.isUpdating.get()) {
					dialogHelper = new JOptionHelper(MainGUI.frmScryptV);
					MainGUI.mainAPP.isUpdating.set(true);
					MainGUI.mainAPP.getMntmLookForUpdates().setEnabled(false);

					try {
						// Loof for new versions...
						String newVersion = MainGuiGeneralAppStarter.updatesManager.lookForUpdates(silent);
						if (newVersion != null) {
							// If found ask to user what to do
							Sounds.playSound(SoundsConfigurator.SOUND_PLING, null);
							boolean downloadUpdates = dialogHelper.yesOrNo(
									"New version [" + newVersion
											+ "] found, do you want to download and install it? (selecting 'no' will cancel all pending updates)",
									"Update found: " + newVersion);
							if (downloadUpdates) {
								try {
									MainGUI.updatesDownloaded = MainGuiGeneralAppStarter.updatesManager.downloadAndExtractUpdates();
									MainGuiGeneralAppStarter.updatesManager.updatesReadyToInstall.set(true);
									boolean restartNow = dialogHelper.yesOrNo(
											"ScrYpT must be restarted to begin update procedure: do you want to restart now?", "Restart required");
									if (restartNow) {
										MainGUI.closeApplication(0);
									} else {
										dialogHelper.info("Updates will be installed when you'll close current session", "Update will start at close");
									}

								} catch (Exception e1) { 
									// Error during download/extraction or user killed process
									if (!(e1 instanceof ProgressBarInterruptedException)) {
										String msg = "Cannot download updates, an error occurred";
										logger.error(msg, e1);
										dialogHelper.errorScroll(msg, "Error during download", e1);
									
									} else if(e1 instanceof TimeoutException) {
										String message = "Server is unreachable!";
										dialogHelper.error(message, "An error occurred");
										logger.error(message, e1);
										
									} else {
										dialogHelper.info("Process killed by user", "Process killed by user");
										logger.info("User has killed process" + e1.toString());
									}
								}
							} else {
								// User skipped updates pressing no or cancel
								dialogHelper.info("As you want, update will be skipped, return here when you'll want to update software ;)",
										"Update skipped");
								logger.info("Update skipped, as requested from user.");
								MainGuiGeneralAppStarter.updatesManager.updatesReadyToInstall.set(false);
								MainGUI.frmScryptV.toFront();
							}
						} else {
							// If version == null means that sw is already updated
							if (MainGuiGeneralAppStarter.updatesManager.udpdatesReachable()) {
								logger.info("Nothing to download, current version is up to date :)");
								if (!silent) {
									dialogHelper.info("Nothing to download, current version is up to date :)", "Already up to date");
								}
							}else {
								logger.error("Cannot retrieve remote update data, server is unreachable.");
								if (!silent) {
									dialogHelper.error("Cannot retrieve remote update data, server is unreachable.", "Connection error");
								}
							}
						}
					} catch (Exception e2) {
						// " main types of error: user killed process or general error during process
						if (!(e2 instanceof ProgressBarInterruptedException)) {
							dialogHelper.error("Cannot read updates, an error occurred :(", "An error occurred!");
							logger.error("Cannot read updates, an error occurred :(", e2);
						
						} else if(e2 instanceof TimeoutException) {
							String message = "Server is unreachable!";
							dialogHelper.error(message, "An error occurred");
							logger.error(message, e2);
						
						} else {
							dialogHelper.info("Process killed by user", "Process killed by user");
							logger.info("User has killed process" + e2.toString());
						}
					}

					// Reset application graphics
					MainGUI.frmScryptV.setEnabled(true);
					MainGUI.mainAPP.getMntmLookForUpdates().setEnabled(true);
					MainGUI.mainAPP.isUpdating.set(false);
					
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							activeWindowBeforeUpdates.setAutoRequestFocus(true);
							activeWindowBeforeUpdates.toFront();
						}
					});
				}
			}
		});
	}
	
	public <T> void immersiveModeToggle() {

		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				// start correct panel
				if(isConfManagerActive()) {
					mainAppReference.mntmFullScreenMode.doClick();
					fullScreenToggleMenuBar();
				
				}else if(isTextEditorActive()) {
					MainGUI.initTxtWriter.textEditorUtils.toggleImmersiveMode();
				
				}else if(isCrypterActive()) {
					mainAppReference.mntmFullScreenMode.doClick();
					fullScreenToggleMenuBar();
				
				}else {
					mainAppReference.mntmFullScreenMode.doClick();
					fullScreenToggleMenuBar();
				}
				
			}
		});
	}
	
	public void fullScreenToggleMenuBar() {
		// Ensure menu bar visible when not full screen
		if (mainAppReference.isFullScreen.get() || !mainAppReference.menuBar.isVisible()) {
			mainAppReference.menuBar.setVisible(true);
			mainAppReference.isImmersiveModeOn.set(false);
		} else {
			mainAppReference.menuBar.setVisible(false);
			mainAppReference.isImmersiveModeOn.set(true);
		}
	}
	
	/**
	 * Start thread with iterative check for externally signaled files to be opened in current instance
	 * (For now only textEditor is supported)
	 */
	public static void startSignalListener() {
		try {
			GuiUtils.launchThreadSafeSwing(new Runnable() {
				
				@Override
				public void run() {
					ArrayList<File> initialMonitoredList = new ArrayList<File>();
					initialMonitoredList.add(GeneralConfig.SCRYPT_ACTIVE_SIGNAL_FILE);
					FileChangeMonitor signalFileChangeMonitor = new FileChangeMonitor(GeneralConfig.SCRYPT_ACTIVE_SIGNAL_FILE.getParentFile(), initialMonitoredList);

					logger.info("startSignalListener Thread created and started!");
					
					// Only text editor managed for now - missing logic to distinguish file opener type
					while(MainGUI.mainAppRunning) {
						try {
							ChangeDTO changeDTO = signalFileChangeMonitor.checkForFile(GeneralConfig.SCRYPT_ACTIVE_SIGNAL_FILE);
							if(changeDTO.isChanged(GeneralConfig.SCRYPT_ACTIVE_SIGNAL_FILE)) {
								logger.info("Detected new files to load: loading into TextEditor...");
								loadFilesFromSignal(TextEditor.class);
								logger.info("Detected new files LOADED into TextEditor!");
							}
							Thread.sleep(GeneralConfig.SIGNAL_LISTENER_PERIOD);
						} catch (Exception e) {
							logger.error("Cannot wait timeout to check for signal changes", e);
						}
					}
				}
			});
		} catch (Exception e) {
			logger.error("Cannot load files from signal, an error occurred :(", e);
		}
	}
	
	public static void loadFilesFromSignal(Class<?> targetFrameClass) {
		PropertiesManager signalManager = new PropertiesManager(GeneralConfig.SCRYPT_ACTIVE_SIGNAL_PATH);
		boolean breakLoop = false;
		int i = 0;
		while(!breakLoop) {
			String target = signalManager.getProperty(GeneralConfig.SIGNAL_FILE_TO_LAUNCH_PROP_PREFIX + i);
			logger.info("Loading " + target + " into TextEditor...");

			breakLoop = target == null || "".equals(target.trim());
			
			File targetFile = new File(target);
			loadFile(targetFrameClass, targetFile);
			
			i++;
		}
		
		GuiUtils.changeFrameStateLaterUnchecked(MainGUI.frmScryptV, MainGUI.frmScryptV, JFrame.MAXIMIZED_BOTH);
		MainGUI.frmScryptV.requestFocus();
		MainGUI.frmScryptV.toFront();
	}
	
	public static void loadFile(Class<?> targetFrameClass, File toLoad) {
		
		// TEXT EDITOR LOADING
		if(targetFrameClass.equals(TextEditor.class)) {
			logger.info("Loading " + toLoad + " into Text Editor...");
			
			MainGUI.mainGuiUtils.startTextEditor(false);
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					if (toLoad != null && toLoad.exists() && SupportedFileTextEditor.isSupported(toLoad)) {
						try {
							MainGUI.initTxtWriter.getCurrentTabEditor().loadFile(toLoad);
						} catch (Exception e) {
							logger.error("Cannot load file into text Editor: " + toLoad, e);
						}
					} else {
						logger.error("FILE not existing or null: Cannot load into text Editor: " + toLoad);
					}
				}
			});
		}
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
		
		replaceMainPanel(mainAppReference.configManagerPanel);
    	
    	MainGUI.currentConfiguration.getGenOpt().setLastPanelOpened(ChargeIniLog.class);
    	MainGUI.currentConfiguration.writeSingleValue(
		INItializer.GEN_OPT, "lastPanelOpened", ChargeIniLog.class.getCanonicalName());
    	
    	refreshGUIlaf();
    	
    	resetMainAppCursor();
    	
    	mainAppReference.getMainFrame().setCursor(GuiUtils.CURSOR_DEFAULT);
	}
	
	public boolean isTextEditorActive() {
		return mainAppReference.getMainFrame().getContentPane().getComponent(0) instanceof TextEditor;
	}
	public boolean isCrypterActive() {
		return mainAppReference.getMainFrame().getContentPane().getComponent(0) instanceof CrypterPanel;
	}
	public boolean isConfManagerActive() {
		return mainAppReference.getMainFrame().getContentPane().getComponent(0) instanceof ChargeIniLog;
	}
	
	public void updateTitleRightPart(String newSuffix) {
		String[] titleComponents = mainAppReference.getMainFrame().getTitle().split("-");
		String newTitle = "";
		int i = 0;
		for (i = 0; i<2; i++) {
			newTitle += titleComponents[i] + " - ";
		}
		newTitle += newSuffix;
		mainAppReference.getMainFrame().setTitle(newTitle);
	}
	
	public void startCrypterPanel(final boolean forceStart) {
		
		mainAppReference.isOnHomePage.set(false);
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				mainAppReference.getMainFrame().setCursor(GuiUtils.CURSOR_WAIT);
				
				// reset all listeners
				if (forceStart) {
					removeAllListeners(MainGUI.panelCrypter);
				}
				
				// reset shortcut listener
				if (forceStart && MainGUI.panelCrypter != null && MainGUI.panelCrypter.shortcutsListener != null) {
					KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(MainGUI.panelCrypter.shortcutsListener);
					removeAllListeners(MainGUI.panelCrypter);
				}
				
				if (MainGUI.panelCrypter == null || forceStart) {
					Sounds.playSound(SoundsConfigurator.SOUND_POOL_SHOT, null);
					MainGUI.panelCrypter = new CrypterPanel(MainGUI.currentConfiguration, mainAppReference);
				}
				
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						replaceMainPanel(MainGUI.panelCrypter);
						MainGUI.currentConfiguration.getGenOpt().setLastPanelOpened(CrypterPanel.class);
						MainGUI.currentConfiguration.writeSingleValue(INItializer.GEN_OPT, "lastPanelOpened", CrypterPanel.class.getCanonicalName());
					}
				});
				
				refreshGUIlaf();
				
				resetMainAppCursor();
			}
		});
	}
	
	public void startTextEditor(final boolean forceRestart) {
		
		mainAppReference.isOnHomePage.set(false);
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				mainAppReference.getMainFrame().setCursor(GuiUtils.CURSOR_WAIT);

				// reset shortcut listener
				if (forceRestart && MainGUI.initTxtWriter != null && MainGUI.initTxtWriter.shortcutsListener != null) {
					KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(MainGUI.initTxtWriter.shortcutsListener);
					removeAllListeners(MainGUI.initTxtWriter);
				}
				if (MainGUI.initTxtWriter == null || forceRestart) {
					Sounds.playSound(SoundsConfigurator.SOUND_POOL_SHOT, null);
					MainGUI.initTxtWriter = new TextEditor(mainAppReference, MainGUI.currentConfiguration);
				}
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						replaceMainPanel(MainGUI.initTxtWriter);
						MainGUI.updateGuiFromCurrConfig();
					}
				});
				
				mainAppReference.getMainFrame().setCursor(GuiUtils.CURSOR_DEFAULT);
				MainGUI.currentConfiguration.getGenOpt().setLastPanelOpened(TextEditor.class);
				MainGUI.currentConfiguration.writeSingleValue(INItializer.GEN_OPT, "lastPanelOpened", TextEditor.class.getCanonicalName());
				
			}
		});
	}
	
	public void replaceMainPanel(JPanel newMainPanel) {
		JPanel contentPane = (JPanel) mainAppReference.getMainFrame().getContentPane();
		contentPane.removeAll();
    	contentPane.add(newMainPanel);
    	contentPane.revalidate(); 
    	contentPane.repaint();
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

	public void refreshGUIlaf() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				boolean isNimbus = GUIStyelSettings.isNimbus(MainGUI.currentConfiguration.getGuiOpt());
				boolean isDarkNimbus = MainGUI.currentConfiguration.getGuiOpt().isDarkGuiNimbus();
				
				if(isNimbus) {
					if (isDarkNimbus) {
						GUIStyelSettings.setDarkNimbus(MainGUI.currentConfiguration.getGuiOpt().nimbusSize);
					}else {
						GUIStyelSettings.setLightNimbus(MainGUI.currentConfiguration.getGuiOpt().nimbusSize);
					}
				}else {
					GUIStyelSettings.setStyle(MainGUI.currentConfiguration.getGuiOpt().getPreferredStyle(), MainGUI.currentConfiguration.getGuiOpt().nimbusSize);
				}
				
				GUIStyelSettings.updateAllWindowsLaf();
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
	
	public static void askNameAtStartIfNotDefined() {
		INItializer currentConfiguration = MainGUI.getCurrentConfInterface().getInitializer();
		GenericOption genOpt = currentConfiguration.getGenOpt();
		if (genOpt.getUserName().equals("") || genOpt.getUserName().length() > 20) {
			logger.info("Current user input initialization: void user or lenght>20 detected!");
			genOpt.setUserName(askUsername());
			currentConfiguration.writeSingleValue(INItializer.GEN_OPT, "userName", genOpt.getUserName());
			logger.info("Current userName: " + genOpt.getUserName());
		}
	}

	public static void welcomeMethodFirstTime() {
		
		GuiUtils.launchThreadSafeSwing(new Runnable() {
			public void run() {
				// ASK FOR USERNAME IF NULL OR FIRST TIME
				INItializer currentConfiguration = MainGUI.getCurrentConfInterface().getInitializer();
				GenericOption genOpt = currentConfiguration.getGenOpt();
				if (genOpt.isFirstAccess()) {
					String msg = "<html>Hi " + genOpt.getUserName() + ", and welcome to " + GeneralConfig.APPLICATION_NAME
							+ "!<br/>I suggest you to take a look to documentation before to start, do you want to open it now?</html>";
					if (dialogHelper.showInfoYNDialogOnTop(msg, "Welcome to " + GeneralConfig.APPLICATION_NAME, JOptionHelper.QUESTION)) {
						MainGUI.mainAPP.getMntmUserDocumentation().doClick();
					}
				}
				currentConfiguration.writeSingleValue(INItializer.GEN_OPT, "firstAccess", false);
			}
		});
	}
	
	public static String askUsername() {
		INItializer currentConfiguration = MainGUI.getCurrentConfInterface().getInitializer();
		boolean end=false;
		String input="";
		do {
			input = MainGUI.dialogHelper.askForStringUnsafeOnTop("Please insert your name:", "Hi, what's your name? :)", "U-sir", new Object[]{"Confirm"});
			
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

package maingUI;

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
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import amfileutils.FileStuff;
import dialogutils.JOptionHelperExtended;
import exception.UpdatesUnavailableException;
import files.watcher.ChangeDTO;
import files.watcher.FileChangeMonitor;
import frameutils.utils.GuiUtils;
import gUI.panels.ConfigurationManagerPanel;
import gUI.panels.CrypterPanel;
import gUI.panels.TextEditor;
import guistyle.GUIStyelSettings;
import mainobj.INItializer;
import media.Sounds;
import om.GenericOption;
import om.SupportedFileTextEditor;
import om.TabTextEditorObj;
import om.exception.ProgressBarInterruptedException;
import resources.GeneralConfig;
import resources.PropertiesManager;
import resources.SoundsConfigurator;
import tabbededitor.CustomTabbedEditor;

public class MainGuiUtils {
	
	// MainGuiUtils logger
	static Logger logger = Logger.getLogger(MainGuiUtils.class);

	public MainGUI mainAppReference;
	
	private static JOptionHelperExtended dialogHelper = new JOptionHelperExtended(null);

	public MainGuiUtils(MainGUI mainApp) {
		mainAppReference = mainApp;
		dialogHelper = new JOptionHelperExtended(mainApp != null ? mainApp.getMainFrame() : null);
	}
	
	public static Thread lookForUpdates(final boolean silent) {
		dialogHelper.setAdvicesEnabled(!silent);
		
		return new Thread(new Runnable() {
			
			@Override
			public void run() {
				Frame activeWindowBeforeUpdates = GuiUtils.getTopFrame();
				
				if (!MainGUI.mainAPP.isUpdating.get()) {
					MainGUI.mainAPP.isUpdating.set(true);
					MainGUI.mainAPP.getMntmLookForUpdates().setEnabled(false);

					try {
						// Look for new versions...
						String newVersion = MainGuiGeneralAppStarter.updatesManager.lookForUpdates(silent);
						if (newVersion != null) {
							// If found ask to user what to do
							Sounds.playSound(SoundsConfigurator.SOUND_PLING, null);
							boolean downloadUpdates = dialogHelper.yesOrNo(
									"<html>New version [" + newVersion + "] found, do you want to download and install it?",
									"Update found: " + newVersion +
									"</br>(you can disable automatic check at start going to preferences -> generalOptions)</html>");
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
										dialogHelper.errorScroll(msg, "Error during download or unzipping", e1);
									
									} else if(e1 instanceof TimeoutException) {
										String message = "Server is unreachable!";
										dialogHelper.error(message, "An error occurred");
										logger.error(message, e1);
										
									} else if(e1 instanceof UpdatesUnavailableException) {
										dialogHelper.warn(e1.getMessage(), "An error occurred");
										logger.error(e1.getMessage(), e1);
										
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
									dialogHelper.info("Nothing to download, current version is up to date :)", "Already up to date");
							}else {
								logger.error("Cannot retrieve remote update data, server is unreachable.");
								dialogHelper.error("Cannot retrieve remote update data, server is unreachable.", "Connection error");
							}
						}
					} catch (Exception e2) {
						// " main types of error: user killed process or general error during process
						if (e2 instanceof ProgressBarInterruptedException) {
							dialogHelper.info("Process killed by user", "Process killed by user");
							logger.info("User has killed process" + e2.toString());
						
						} else if(e2 instanceof TimeoutException) {
							String message = "Server is unreachable!";
							dialogHelper.error(message, "An error occurred");
							logger.error(message, e2);
						
						} else if(e2 instanceof UpdatesUnavailableException) {
							dialogHelper.warn(e2.getMessage(), "An error occurred");
							logger.error(e2.getMessage(), e2);
							
						} else {
							dialogHelper.error("Cannot read updates, an error occurred :(", "An error occurred!");
							logger.error("Cannot read updates, an error occurred :(", e2);
						}
					} finally {
						dialogHelper.setAdvicesEnabled(true);
					}
					
					// Reset application graphics
					MainGUI.frmScryptV.setEnabled(true);
					MainGUI.mainAPP.getMntmLookForUpdates().setEnabled(true);
					MainGUI.mainAPP.isUpdating.set(false);
				}
				
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						activeWindowBeforeUpdates.setAutoRequestFocus(true);
						activeWindowBeforeUpdates.toFront();
					}
				});
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
		
		GuiUtils.extendFramesAndMoveToTop(MainGUI.frmScryptV, MainGUI.frmScryptV, JFrame.MAXIMIZED_BOTH);
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
		} else {
			dialogHelper.info("The file file-type-opener target is not yet supported.", "File type opener not supported");
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
		configuration = (instance.getContentPane().getComponent(0).equals(MainGUI.configManagerPanel))? true : false;
		home = (!txtEditor && !crypter && !configuration)? true : false;
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				
				// start correct panel
				if(ConfigurationManagerPanel.class.equals(target)) {
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
			removeAllListeners(MainGUI.configManagerPanel);
		}
		
		if (MainGUI.configManagerPanel == null || forceStart) {
			Sounds.playSound(SoundsConfigurator.SOUND_POOL_SHOT, null);
			MainGUI.configManagerPanel = new ConfigurationManagerPanel(MainGUI.currentConfiguration, mainAppReference);
		}
		
		MainGUI.configManagerPanel.splitPane.setDividerLocation(300);
		MainGUI.configManagerPanel.textArea.revalidate();
		MainGUI.configManagerPanel.textArea.repaint();
		
		replaceMainPanel(MainGUI.configManagerPanel);
    	
    	MainGUI.currentConfiguration.getGenOpt().setLastPanelOpened(ConfigurationManagerPanel.class);
    	MainGUI.currentConfiguration.writeSingleValue(INItializer.GEN_OPT, "lastPanelOpened", ConfigurationManagerPanel.class.getCanonicalName());
    	
    	MainGUI.configManagerPanel.initGuiFromConfig();
    	
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
		return mainAppReference.getMainFrame().getContentPane().getComponent(0) instanceof ConfigurationManagerPanel;
	}
	
	public void updateTitleRightPart(String newSuffix) {
		String[] titleComponents = mainAppReference.getMainFrame().getTitle().split("-");
		String newTitle = "";
		int i = 0;
		for (i = 0; i < 2; i++) {
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
				
				replaceMainPanel(MainGUI.panelCrypter);
				MainGUI.currentConfiguration.getGenOpt().setLastPanelOpened(CrypterPanel.class);
				MainGUI.currentConfiguration.writeSingleValue(INItializer.GEN_OPT, "lastPanelOpened", CrypterPanel.class.getCanonicalName());
				MainGUI.panelCrypter.initGuiFromConfig();
				
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
						MainGUI.initTxtWriter.initGuiFromConfig(true, true);
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
    	
    	if(newMainPanel instanceof TextEditor) {
    		((TextEditor)newMainPanel).textEditorUtils.setupTogglesOnMainMenu(true);
    		mainAppReference.resizeMainFrameDefaults();
    	} else if(newMainPanel instanceof CrypterPanel) {
    		mainAppReference.setTogglesEnabled(false);
    		mainAppReference.resizeMainFrameDefaults();
    	} else {
    		mainAppReference.setTogglesEnabled(false);
    	}
	}
	
	public static Runnable tempDecryptedEraser() {
		return new Runnable() {
			
			@Override
			public void run() {
				File[] decryptedList = GeneralConfig.TEMP_DECRIPTED_ROOT.listFiles();
				if (decryptedList!= null && decryptedList.length!=0) {
					// while there are temp decripted delete them
					for (File temp : decryptedList) {
						logger.info("Deleting current temp file: " + temp);
						FileUtils.deleteQuietly(temp);
					} 
				} else {
					logger.info("Nothing to delete!");
				}
				// now delete temp folder, and retry if something has not been deleted
				FileStuff.deleteDirContentInclRoot(GeneralConfig.TEMP_DECRIPTED_ROOT);
			}
		};
	}

	public static Runnable tempFilesEraser() {
		return new Runnable() {
			
			@Override
			public void run() {
				File[] tempFilesList = GeneralConfig.TEMP_DIRECTORY_FILE.listFiles();
				if (tempFilesList!= null && tempFilesList.length!=0) {
					// while there are temp decripted delete them
					for (File temp : tempFilesList) {
						logger.info("Deleting current temp file: " + temp);
						FileUtils.deleteQuietly(temp);
					} 
				} else {
					logger.info("Nothing to delete!");
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
		resettablePanels.add(ConfigurationManagerPanel.class.getSimpleName());
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
					if (dialogHelper.showYNDialog(msg, "Welcome to " + GeneralConfig.APPLICATION_NAME, JOptionHelperExtended.QUESTION)) {
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
			input = MainGUI.dialogHelper.askForStringNullable("Please insert your name:", "Hi, what's your name? :)", "U-sir", new Object[]{"Confirm"});
			
			if (input==null || input.equals("") || input.length() > 20) {
				MainGUI.dialogHelper.warn("Username must be between 1-20 characters!", "UserName length Error");
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
	
	public boolean isAllSavedInTextEditor() {
		if (MainGUI.initTxtWriter == null || MainGUI.initTxtWriter.getCurrentTabEditor() == null) {
			return true;
		}
		
		CustomTabbedEditor currentTabEditor = MainGUI.initTxtWriter.getCurrentTabEditor();

		boolean thereAreUnsaved;
		boolean atLeastOneTemp;
		
		thereAreUnsaved = false;
		atLeastOneTemp = false;
		for (TabTextEditorObj tabElement : currentTabEditor.loadedList.getList()) {
			if (tabElement.unsaved) {
				thereAreUnsaved = true;
			}
			if (tabElement.loadedFile == null) {
				atLeastOneTemp = true;
			}
		} 
		
		return (thereAreUnsaved || atLeastOneTemp);
	}
	
	/**
	 * Check if panels that are currently loaded (!= null) can be de-referenced (if nothing to save is found, or no file to crypt currently loaded)
	 */
	public void deReferenceUnusedPanels() {
		
		if(MainGUI.configManagerPanel != null && !isConfManagerActive()) {
			MainGUI.configManagerPanel = null;
		}
		
		if(MainGUI.panelCrypter != null && !isCrypterActive() && MainGUI.panelCrypter.panelVDragNDropPane.getAllNodes().length == 0) {
			MainGUI.panelCrypter = null;
		}

		if(MainGUI.initTxtWriter != null && !isTextEditorActive() && isAllSavedInTextEditor()) {
			MainGUI.initTxtWriter = null;
		}
	}
}

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

import java.awt.Component;
import java.awt.Frame;
import java.awt.KeyboardFocusManager;
import java.awt.event.ComponentListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.commons.io.FileUtils;

import gui.commons.dialogutils.JOptionHelperExtended;
import gui.commons.frameutils.utils.GuiUtilsExtended;
import gui.commons.guistyle.GUIStyelSettingsExtended;
import gui.texteditor.commons.filesearch.name.FileNameSearchDialog;
import gui.texteditor.commons.om.SupportedFileTextEditor;
import gui.texteditor.commons.om.TabTextEditorObject;
import gui.texteditor.commons.tabbededitor.CustomTabbedEditor;
import initializer.configs.impl.INItializer;
import initializer.configs.impl.om.GenericOption;
import initializer.configs.impl.om.TextEditorOption;
import resources.GeneralConfig;
import resources.SoundsConfigurator;
import resources.SoundsManagerExtended;
import screept.gui.panels.ConfigurationManagerPanel;
import screept.gui.panels.CrypterPanel;
import screept.gui.panels.TextEditor;
import updater.module.exception.UpdatesUnavailableException;
import various.common.light.files.FileVarious;
import various.common.light.files.FileWorker;
import various.common.light.files.watcher.ChangeDTO;
import various.common.light.files.watcher.FileChangeMonitor;
import various.common.light.gui.GuiUtils;
import various.common.light.gui.LightLicenseViewer;
import various.common.light.gui.LightLicenseViewer.UserLicenseChoice;
import various.common.light.network.utils.DownloadUtils;
import various.common.light.om.exception.ProgressBarInterruptedException;
import various.common.light.utility.hash.ChecksumHasher;
import various.common.light.utility.hash.HashingFunction;
import various.common.light.utility.log.SafeLogger;
import various.common.light.utility.properties.PropertiesManager;
import various.common.light.utility.string.StringWorker;
import various.common.light.utility.string.StringWorker.EOL;

public class MainGuiUtils {

	// MainGuiUtils logger
	static SafeLogger logger = new SafeLogger(MainGuiUtils.class);

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
							SoundsManagerExtended.playSound(SoundsConfigurator.SOUND_PLING, null);
							boolean downloadUpdates = dialogHelper.yesOrNo(
									"<html>New version [" + newVersion + "] found, do you want to download and install it? </br>(you can disable automatic check at start going to preferences -> generalOptions)</html>",
									"Update found: " + newVersion);
							if (downloadUpdates && checkNewLicense()) {
								try {
									MainGUI.updatesDownloaded = MainGuiGeneralAppStarter.updatesManager.downloadAndExtractUpdates();
									MainGuiGeneralAppStarter.updatesManager.updatesReadyToInstall.set(true);
									boolean restartNow = dialogHelper.yesOrNo(
											"ScreepT must be restarted to begin update procedure: do you want to restart now?", "Restart required");

									if (restartNow) {
										MainGuiManager.closeApplication(0);
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
										dialogHelper.warn(e1.getMessage(), "Updates not found. It's possibile that they're being upgraded, retry again later.");
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
								MainGUI.mainFrameScreept.toFront();
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
					MainGUI.mainFrameScreept.setEnabled(true);
					MainGUI.mainAPP.getMntmLookForUpdates().setEnabled(true);
					MainGUI.mainAPP.isUpdating.set(false);
				}

				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						activeWindowBeforeUpdates.setAutoRequestFocus(true);
						GuiUtils.giveFocusToComponent(activeWindowBeforeUpdates);
					}
				});
			}
		});
	}

	public static boolean checkNewLicense() {

		try {
			File newLicense = DownloadUtils.downloadFile(false, new URL(GeneralConfig.WEB_UPDATES_LICENSE_URL), GeneralConfig.TEMP_DIRECTORY+"tempNewLicense.html", null);

			if (!ChecksumHasher.equalsFilesChecksum(newLicense, GeneralConfig.LICENSE_FILE, HashingFunction.SHA512)) {
				LightLicenseViewer licenseViewer = MainGUI.mainAPP.windowCreator.newLicenseFrame(newLicense, true, false, new boolean[] {true, false, false});
				return UserLicenseChoice.ACCEPTED.equals(licenseViewer.userChoice);
			} else {
				return true;
			}
		} catch (Throwable e) {
			logger.error("An error occurred while comparing license!", e);
			return true;
		}
	}

	public <T> void immersiveModeToggle() {

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				// start correct panel
				if(isTextEditorActive(true)) {
					mainAppReference.mntmFullScreenMode.doClick();
					fullScreenToggleMenuBar();

				}else if(isTextEditorActive(true)) {
					MainGUI.textEditorPanel.textEditorUtils.toggleImmersiveMode();

				}else if(isCrypterActive(true)) {
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
			GuiUtilsExtended.launchThreadSafeSwing(new Runnable() {

				@Override
				public void run() {
					ArrayList<File> initialMonitoredList = new ArrayList<File>();
					initialMonitoredList.add(GeneralConfig.SCREEPT_ACTIVE_SIGNAL_FILE);
					FileChangeMonitor signalFileChangeMonitor = new FileChangeMonitor(GeneralConfig.SCREEPT_ACTIVE_SIGNAL_FILE.getParentFile(), initialMonitoredList);

					logger.info("startSignalListener Thread created and started!");

					// Only text editor managed for now - missing logic to distinguish file opener type
					while(MainGUI.mainAppRunning) {
						try {
							ChangeDTO changeDTO = signalFileChangeMonitor.checkForFile(GeneralConfig.SCREEPT_ACTIVE_SIGNAL_FILE);
							if(changeDTO.isChanged(GeneralConfig.SCREEPT_ACTIVE_SIGNAL_FILE)) {
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
		PropertiesManager signalManager = new PropertiesManager(GeneralConfig.SCREEPT_ACTIVE_SIGNAL_PATH);
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

		GuiUtilsExtended.extendFramesAndMoveToTop(MainGUI.mainFrameScreept, MainGUI.mainFrameScreept, JFrame.MAXIMIZED_BOTH);
		MainGUI.mainFrameScreept.requestFocus();
		MainGUI.mainFrameScreept.toFront();
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
							MainGUI.textEditorPanel.getCurrentTabEditor().loadFile(toLoad);
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

		txtEditor = (instance.getContentPane().getComponent(0).equals(MainGUI.textEditorPanel))? true : false;
		crypter = (instance.getContentPane().getComponent(0).equals(MainGUI.crypterPanel))? true : false;
		configuration = (instance.getContentPane().getComponent(0).equals(MainGUI.configManagerPanel))? true : false;
		home = (!txtEditor && !crypter && !configuration)? true : false;

		SwingUtilities.invokeLater(new Runnable() {
			@Override
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

		mainAppReference.getMainFrame().setCursor(GuiUtilsExtended.CURSOR_WAIT);

		mainAppReference.isOnHomePage.set(false);

		// reset all listeners
		if (forceStart) {
			removeAllListeners(MainGUI.configManagerPanel);
		}

		if (MainGUI.configManagerPanel == null || forceStart) {
			SoundsManagerExtended.playSound(SoundsConfigurator.SOUND_POOL_SHOT, null);
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

    	mainAppReference.getMainFrame().setCursor(GuiUtilsExtended.CURSOR_DEFAULT);
	}

	public boolean isOnHomePage(boolean withFocus) {
		return mainAppReference.isOnHomePage.get() && (!withFocus || isMainFrameFocused());
	}

	public boolean isTextEditorActive(boolean withFocus) {
		return getMainPanel().getComponent(0) instanceof TextEditor && (!withFocus || isMainFrameFocused());
	}
	public boolean isCrypterActive(boolean withFocus) {
		return getMainPanel().getComponent(0) instanceof CrypterPanel && (!withFocus || isMainFrameFocused());
	}
	public boolean isConfManagerActive(boolean withFocus) {
		return getMainPanel().getComponent(0) instanceof ConfigurationManagerPanel && (!withFocus || isMainFrameFocused());
	}

	public boolean isMainFrameFocused() {
		return mainAppReference.getMainFrame().getFocusOwner() != null &&
				mainAppReference.getMainFrame().isFocusableWindow();
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
			@Override
			public void run() {
				mainAppReference.getMainFrame().setCursor(GuiUtilsExtended.CURSOR_WAIT);

				// reset all listeners
				if (forceStart) {
					removeAllListeners(MainGUI.crypterPanel);
				}

				// reset shortcut listener
				if (forceStart && MainGUI.crypterPanel != null && MainGUI.crypterPanel.shortcutsListener != null) {
					KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(MainGUI.crypterPanel.shortcutsListener);
					removeAllListeners(MainGUI.crypterPanel);
				}

				if (MainGUI.crypterPanel == null || forceStart) {
					SoundsManagerExtended.playSound(SoundsConfigurator.SOUND_POOL_SHOT, null);
					MainGUI.crypterPanel = new CrypterPanel(MainGUI.currentConfiguration, mainAppReference);
				}

				replaceMainPanel(MainGUI.crypterPanel);
				MainGUI.currentConfiguration.getGenOpt().setLastPanelOpened(CrypterPanel.class);
				MainGUI.currentConfiguration.writeSingleValue(INItializer.GEN_OPT, "lastPanelOpened", CrypterPanel.class.getCanonicalName());
				MainGUI.crypterPanel.initGuiFromConfig();

				resetMainAppCursor();
			}
		});
	}

	public void startTextEditor(final boolean forceRestart) {

		mainAppReference.isOnHomePage.set(false);

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				mainAppReference.getMainFrame().setCursor(GuiUtilsExtended.CURSOR_WAIT);

				// reset shortcut listener
				if (forceRestart && MainGUI.textEditorPanel != null && MainGUI.textEditorPanel.shortcutsListener != null) {
					KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(MainGUI.textEditorPanel.shortcutsListener);
					removeAllListeners(MainGUI.textEditorPanel);
				}
				if (MainGUI.textEditorPanel == null || forceRestart) {
					SoundsManagerExtended.playSound(SoundsConfigurator.SOUND_POOL_SHOT, null);
					MainGUI.textEditorPanel = new TextEditor(mainAppReference, MainGUI.currentConfiguration);
				}
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						replaceMainPanel(MainGUI.textEditorPanel);
						MainGUI.textEditorPanel.initGuiFromConfig(true, true);
					}
				});

				mainAppReference.getMainFrame().setCursor(GuiUtilsExtended.CURSOR_DEFAULT);
				MainGUI.currentConfiguration.getGenOpt().setLastPanelOpened(TextEditor.class);
				MainGUI.currentConfiguration.writeSingleValue(INItializer.GEN_OPT, "lastPanelOpened", TextEditor.class.getCanonicalName());
			}
		});
	}

	public synchronized void openFilesInTextEditor(File[] filesToLoad, Runnable postOpenAction) {

		if(!MainGUI.mainGuiUtils.isTextEditorActive(true)) {
			MainGUI.mainGuiUtils.startTextEditor(MainGUI.textEditorPanel == null);
		}

		SwingUtilities.invokeLater(()->{
			while (MainGUI.textEditorPanel == null) {
				try {
					Thread.sleep(250);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			MainGUI.textEditorPanel.getCurrentTabEditor().loadFiles(filesToLoad);

			SwingUtilities.invokeLater(()->{
				Runnable action = postOpenAction != null ? postOpenAction : () -> {};
				action.run();
			});
		});
	}

	public void openQuickSearchIntoWorkspace() {

		SwingUtilities.invokeLater(()->{
//			File workspace = new File(MainGUI.textEditorPanel.currentConfiguration.getTextEditorOpt().defaultWorkspacePath);
			FileNameSearchDialog dialog = new FileNameSearchDialog(
					MainGUI.mainAPP.getMainFrame(),
					MainGUI.textEditorPanel.currentConfiguration);
			dialog.setTitle("Search files by name - " + GeneralConfig.APPLICATION_NAME);

			dialog.setDoubleClickItemAction((file) ->{
				MainGUI.mainGuiUtils.openFilesInTextEditor(new File[] {file}, null);
			});
			dialog.setLocationRelativeTo(mainAppReference.getMainFrame());
			dialog.setVisible(true);
		});
	}

	public JPanel getMainPanel() {
		return (JPanel) mainAppReference.getMainFrame().getContentPane();
	}

	public void replaceMainPanel(JPanel newMainPanel) {
		JPanel contentPane = getMainPanel();
		contentPane.removeAll();
    	contentPane.add(newMainPanel);
    	contentPane.revalidate();
    	contentPane.repaint();

    	// ENABLE/DISABLE TOP RIGH TOGGLES AND RESIZE CUSTOMIZED FOR PANEL
    	if(newMainPanel instanceof TextEditor) {
    		((TextEditor)newMainPanel).textEditorUtils.setupTogglesOnMainMenu(true);
    		mainAppReference.resizeMainFrameDefaults();

    	} else if(newMainPanel instanceof CrypterPanel) {
    		mainAppReference.setTopRightTogglesEnabled(false);
    		mainAppReference.resizeMainFrameDefaults();

    	} else {
    		mainAppReference.setTopRightTogglesEnabled(false);
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
				FileWorker.deleteDirContentInclRoot(GeneralConfig.TEMP_DECRIPTED_ROOT);
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
				FileWorker.deleteDirContentRecursive(GeneralConfig.TEMP_DIRECTORY_FILE);
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
			@Override
			public void run() {
				mainAppReference.getMainFrame().setCursor(GuiUtilsExtended.CURSOR_DEFAULT);
			}
		});
	}

	public void refreshGUIlaf() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				boolean isNimbus = GUIStyelSettingsExtended.isNimbus(MainGUI.currentConfiguration.getGuiOpt().getPreferredStyle());
				boolean isDarkNimbus = MainGUI.currentConfiguration.getGuiOpt().isDarkGuiNimbus();

				if(isNimbus) {
					if (isDarkNimbus) {
						GUIStyelSettingsExtended.setDarkNimbus(MainGUI.currentConfiguration.getGuiOpt().nimbusSize);
					}else {
						GUIStyelSettingsExtended.setLightNimbus(MainGUI.currentConfiguration.getGuiOpt().nimbusSize);
					}
				}else {
					GUIStyelSettingsExtended.setStyle(MainGUI.currentConfiguration.getGuiOpt().getPreferredStyle(), MainGUI.currentConfiguration.getGuiOpt().nimbusSize);
				}

				GUIStyelSettingsExtended.updateAllWindowsLaf();
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

	public static void askNameAtStartIfNotDefined(JFrame parent) {
		INItializer currentConfiguration = MainGUI.getCurrentConfInterface().getInitializer();
		GenericOption genOpt = currentConfiguration.getGenOpt();
		if (genOpt.getUserName().equals("") || genOpt.getUserName().length() > 20) {
			logger.info("Current user input initialization: void user or lenght>20 detected!");
			genOpt.setUserName(askUsername(parent));
			currentConfiguration.writeSingleValue(INItializer.GEN_OPT, "userName", genOpt.getUserName());
			logger.info("Current userName: " + genOpt.getUserName());
		}
	}

	public static void welcomeMethodFirstTime() {

		EOL.defaultEol = EOL.getOsBasedEOL();
		MainGUI.currentConfiguration.getTextEditorOpt().setEol(EOL.defaultEol);

		GuiUtilsExtended.launchThreadSafeSwing(new Runnable() {
			@Override
			public void run() {

				// ASK FOR USERNAME IF NULL OR FIRST TIME
				INItializer currentConfiguration = MainGUI.getCurrentConfInterface().getInitializer();
				GenericOption genOpt = currentConfiguration.getGenOpt();
				if (genOpt.isFirstAccess()) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					String msg = "<html>Hi " + genOpt.getUserName() + ", and welcome to " + GeneralConfig.APPLICATION_NAME
							+ "!<br/>I suggest you to take a look to documentation before to start, do you want to open it now?</html>";
					if (dialogHelper.showYNDialog(msg, "Welcome to " + GeneralConfig.APPLICATION_NAME, JOptionHelperExtended.QUESTION)) {
						MainGUI.mainAPP.windowCreator.newTutorialFrame(true);
					}
				}
				currentConfiguration.writeSingleValue(INItializer.GEN_OPT, "firstAccess", false);

			}
		});
	}

	public static String askUsername(JFrame parent) {
		INItializer currentConfiguration = MainGUI.getCurrentConfInterface().getInitializer();
		boolean end=false;
		String input="";
		do {
			input = new JOptionHelperExtended(parent).askForStringNullable("Please insert your name:", "Hi, what's your name? :)", "U-sir", new Object[]{"Confirm"});

			if (input != null && (input.equals("") || input.length() > 26)) {
				MainGUI.dialogHelper.warn("Username must be between 1-26 characters!", "UserName length Error");
				logger.error("Error in user input, redo.");
			} else {
				input = (input == null) ? "U-Sir" : input;
				currentConfiguration.writeSingleValue(INItializer.GEN_OPT, "userName", input);
				currentConfiguration.getGenOpt().setUserName(input);
				end = true;
			}
		} while (!end);
		logger.info("Name selected and saved username: "+input);
		return input;
	}

	public boolean isAllSavedInTextEditor() {
		if (MainGUI.textEditorPanel == null || MainGUI.textEditorPanel.getCurrentTabEditor() == null) {
			return true;
		}

		CustomTabbedEditor currentTabEditor = MainGUI.textEditorPanel.getCurrentTabEditor();

		boolean thereAreUnsaved;
		boolean atLeastOneTemp;

		thereAreUnsaved = false;
		atLeastOneTemp = false;
		for (TabTextEditorObject tabElement : currentTabEditor.loadedList.getList()) {
			if (tabElement.unsaved) {
				thereAreUnsaved = true;
			}
			if (tabElement.loadedFile == null) {
				atLeastOneTemp = true;
			}
		}

		return !(thereAreUnsaved || atLeastOneTemp);
	}

	/**
	 * Check if panels that are currently loaded (!= null) can be de-referenced (if nothing to save is found, or no file to crypt currently loaded)
	 */
	public Boolean deReferenceUnusedPanels() {

		if(!MainGuiUtils.askUnsavedTextFiles())
			// If user presses cancel button do nothing
			return null;

		Boolean somethingCleaned = Boolean.FALSE;

		if(MainGUI.configManagerPanel != null && !isConfManagerActive(false)) {
			MainGUI.configManagerPanel = null;
			somethingCleaned = Boolean.TRUE;
		}

		if(MainGUI.crypterPanel != null && !isCrypterActive(false) && MainGUI.crypterPanel.panelVDragNDropPane.getAllNodes().length == 0) {
			MainGUI.crypterPanel = null;
			somethingCleaned = Boolean.TRUE;
		}

		if(MainGUI.textEditorPanel != null && !isTextEditorActive(false) && isAllSavedInTextEditor()) {
			MainGUI.textEditorPanel = null;
			somethingCleaned = Boolean.TRUE;
		}

		return somethingCleaned;
	}

	public static boolean checkJavaVersionMaxCompatibility() {
		MainGUI.currentJvmVersion = System.getProperty("java.version");

		String tempCompareJvmCurrent = StringWorker.keepOnlyDotsAndDigits(MainGUI.currentJvmVersion);
		int compareRes = StringWorker.compareVersions(tempCompareJvmCurrent, GeneralConfig.MAX_JVM_COMPLETE_COMPATIBILITY);

		return compareRes != 1;
	}

	/**
	 * return false if not exit, true if has to exit;
	 * @return
	 */
	public static boolean askUnsavedTextFiles() {

		TextEditor initTxtWriter = MainGUI.textEditorPanel;
		if (initTxtWriter == null || initTxtWriter.getCurrentTabEditor() == null) {
			return true;
		}

		TextEditorOption txtOpt = MainGUI.currentConfiguration.getTextEditorOpt();

		CustomTabbedEditor currentTabEditor = initTxtWriter.getCurrentTabEditor();
		boolean askCondition = false;
		boolean atLeastOneTemp = false;
		List<File> tempFiles = new ArrayList<>();

		for (TabTextEditorObject tabElement : currentTabEditor.loadedList.getList()) {
			if (tabElement.unsaved) {
				askCondition = askCondition || tabElement.loadedFile != null || (
						tabElement.tempFile != null && !txtOpt.isAutosaveTempFiles());
			}
			if (tabElement.loadedFile == null) {
				atLeastOneTemp = true;
				try {
					if(txtOpt.autosaveTempFiles && tabElement.unsaved) {
						File temp = tabElement.saveTempFile();
						tempFiles.add(temp);
						logger.debug("Temp file saved: " + temp);
					}
				} catch (IOException e) {
					logger.error("can't add to tempFiles : " + tabElement.tempFile, e);
				}
			}
		}

		Boolean choiceY = dialogHelper.askYorNPrecond(
				"Files unsaved in textEditor",
				"There are some unsaved files in text Editor, do you want to save them?",
				MainGUI.mainAPP.getMainFrame(),
				askCondition
		);

		if(choiceY == null) {
			// cancel -> don't exit
			return false;

		}else if(choiceY) {
			try {
				currentTabEditor.saveAllUnsavedExisting();
			} catch (IOException e) {
				logger.error("", e);
				if(!MainGUI.dialogHelper.yesOrNo("Oooops... an error occurred. Continue anyway (you'll lose all unsaved changes)?")) {
					MainGUI.dialogHelper.error("Procedure aborted", "Procedure aborted");
					MainGUI.mntmTextEditor.doClick();
					return false; // no -> don't exit after error
				}
			}
			if(atLeastOneTemp) {
				MainGUI.mntmTextEditor.doClick();
				for(TabTextEditorObject tabElement : currentTabEditor.loadedList.getList()) {
					if(tabElement.unsaved && tabElement.loadedFile == null) {
						currentTabEditor.mainTabList.setSelectedComponent(tabElement.currentScrollPane);
						MainGUI.textEditorPanel.subHeaderPanel.btnSaveTextFile.requestFocus();
						return false; // don't exit
					}
				}
			}
			return true;

		}else {

			MainGUI.currentConfiguration.writeSingleValue(INItializer.TEXT_EDITOR_OPT, "loadedTempFilesAtStart",
					MainGUI.currentConfiguration.listToString(FileVarious.filesToPathList(tempFiles)));

			return true;
		}
	}
}

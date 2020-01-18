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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.KeyboardFocusManager;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.UIManager;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.PropertyConfigurator;
import org.ini4j.Wini;

import dialogutils.GenericFileChooserDialog;
import dialogutils.JOptionHelperExtended;
import files.FileWorker;
import files.FileVarious;
import frameutils.utils.GuiUtilsExtended;
import generic.ParametricExceptionThread;
import gui.GuiUtils;
import gui.JOptionHelper;
import gui.LightLicenseViewer;
import gui.LightLicenseViewer.UserLicenseChoice;
import gui.utils.LauncherLoaderFrame;
import handlers.AWTExceptionHandler;
import launcherutils.IniFileManager;
import logger.LoggerUtils;
import mainobj.INItializer;
import plugins.PluginManager;
import resources.GeneralConfig;
import resources.ImagesPathConfigurator;
import resources.SoundsConfigurator;
import resources.SoundsManagerExtended;
import uTILS.dependencycheck.LibChecker;
import updater.UpdatesManager;
import utility.SafeLogger;
import utility.properties.PropertiesManager;

public class MainGuiGeneralAppStarter {
	
	public static final String MAIN_SIGNAL_PROPERTY = "MAIN_APP_IS_ACTIVE";

    public static Level LOG_LEVEL_AT_START;
    private static boolean isLoggerReady = false;
	
	// mainGUI logger
	static SafeLogger logger = new SafeLogger(MainGuiGeneralAppStarter.class);
	
	// LOADER
	public static LauncherLoaderFrame loaderGraphics;
	public static final Dimension screenSize = GuiUtilsExtended.getScreenSize();
	public static final Dimension minimumSize = new Dimension(880,500);
	public static final Dimension minimumSizeCrypter = new Dimension(1230,750);
		
	// Main configuration reference for all sub-modules
	private static INItializer currentConfiguration;
	// and another one for launcher modding from main app
	protected static IniFileManager iniFileManager;
	
	public static String[] ARGS;
	public static File[] argumentsFiles;
	
	public static AWTExceptionHandler mainTrheadAwtErrorHandler;
	
	public static JOptionHelperExtended dialogHelper;
	public static GenericFileChooserDialog fileDialogHelper;
	
	public static UpdatesManager updatesManager;
	public static PluginManager pluginManager;
	
	// LIbs availability
	public static boolean tesseractAvailable;
	public static boolean voiceSpeakerAvailable;
	public static boolean jnetPcapAvailable;
	public static boolean hwInfoAvailable;
	
	public static void main(String[] args) {
		
		long startTime = System.currentTimeMillis();
		
		checkEULAstatus();
		
		loaderGraphics = new LauncherLoaderFrame("Loading Launcher...", ImagesPathConfigurator.LAUNCHER_BACK_IMAGE_PATH, null);
		loaderGraphics.indeterminate(true, "Loading configuration");
		
		try {
			try {
				String initPropsMsg = "Preloading configuration";
				loaderGraphics.update(initPropsMsg, 1);
				loaderGraphics.indeterminate(true, initPropsMsg);
				
				// Load Internal configuration and dialog helper
				GeneralConfig.initLazyGeneralConfig();

				dialogHelper = new JOptionHelperExtended(null);
				fileDialogHelper = new GenericFileChooserDialog(currentConfiguration);
				
				GuiUtilsExtended.init();
				JOptionHelperExtended.init();
				AWTExceptionHandler.init();
				
				try {
					UIManager.put("Button.defaultButtonFollowsFocus", Boolean.TRUE);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			} catch (Exception e) {
				SoundsManagerExtended.playSound(SoundsConfigurator.ERROR, null);
				dialogHelper.errorScroll("Cannot load internal configuration File: " + AWTExceptionHandler.ERROR_FILE_REASONS + "resourcesFilePathsMappings.properties", "Cannot load configuration", e);
				throw e;
			}
			
			loaderGraphics.indeterminate(false);
			printElapsed("dialogHelper and GeneralConfig initialization finished", startTime);
			
			try {
				// Initialize logger properties
				PropertyConfigurator.configure(GeneralConfig.LOG_PROPERTIES_PATH);
				logger.info(LoggerUtils.getStartString(GeneralConfig.APPLICATION_NAME, true));
				
				isLoggerReady = true;
				logger.info("Logger is READY to log everything ;)");
				
			} catch (Exception e) {
				System.out.println("There was a critical error when trying to load Logger! Application cannot start.");
				SoundsManagerExtended.playSound(SoundsConfigurator.ERROR, null);
				dialogHelper.errorScroll("Critical error: Cannot load internal logging properties File: " + AWTExceptionHandler.ERROR_FILE_REASONS 
						+GeneralConfig.LOG_PROPERTIES_PATH, "Cannot load configuration", e);
				throw e;
			}
			
			try {
				// Load updates manager module
				loaderGraphics.update("Starting updates manager...", 5);
				updatesManager = new UpdatesManager(new URL(GeneralConfig.WEB_UPDATES_MANIFEST_URL), GeneralConfig.APP_VERSION);
				updatesManager.emptyUpdatesDir();
				logger.info("Updates manager started!");
			} catch (Exception e) {
				logger.error("Cannot initialize Updates Manager :(", e);
			}
			
			try {
				// Load plugin manager module
				loaderGraphics.update("Starting plugin manager...", 6);
				pluginManager = new PluginManager();
				logger.info("Plugin manager started!");
			} catch (Exception e) {
				logger.error("Cannot initialize Plugin Manager :(", e);
			}
			
			try {
				// Check hard coded plugins presence
				tesseractAvailable = LibChecker.checkTesseract();
				voiceSpeakerAvailable = LibChecker.checkVoiceSpeaker();
				jnetPcapAvailable = LibChecker.checkJnetPcap();
				hwInfoAvailable = LibChecker.checkHardwareInfoDependencies();
				
				updateInstalledPlugins();
			} catch (Exception e) {
				logger.warn("Cannot check avalaibility of plugins :(");
			}
			
			loaderGraphics.update("Starting Exception handler...", 8);
			
			// CATCH ALL EXCEPTION UNCATCHED DURING SW EXECUTION AND ANALYZE IF THEY ARE Errors or Exceptions
			mainTrheadAwtErrorHandler = new AWTExceptionHandler(new ParametricExceptionThread(new Runnable() {
				@Override
				public void run() {
					try {
						// FOrce exit, because AWT already does a check and ask to user, this will be launched when user will choose to kill application
						doStuffBeforeErrorClose(true, new Throwable("\n\n  ----  MAIN APPLICATION ERROR DETECTED!   ----\n\n"));
					} catch (Exception e) {
					}
				}
			}), MainGUI.getCurrentConfInterface());

			printElapsed("AWT error handler loaded", startTime);
			
			try {
				
				// SETUP TOOLTIPS PROPERTIES
				Font tooltipFontFromProps = (GeneralConfig.TOOLTIP_FONT == null)? new Font("Segoe UI", Font.PLAIN, 14) : GeneralConfig.TOOLTIP_FONT;
				GuiUtils.setupTooltips(GeneralConfig.TOOLTIP_DELAY, GeneralConfig.TOOLTIP_TIMEOUT, GeneralConfig.TOOLTIP_RESHOW_DELAY, tooltipFontFromProps);
			
			} catch (Exception e) {
				SoundsManagerExtended.playSound(SoundsConfigurator.ERROR, null);
				dialogHelper.errorScroll("Cannot setup Tooltips general graphics, an error occurred", "Cannot load Tooltips props", e);
			}
			
			printElapsed("DialogHelper, General config and tooltips setup finished", startTime);
			
			printElapsed("Log properties loaded", startTime);
			
			writeSignalFileForExternalJars();
			argumentsFiles = FileVarious.getFileArrayFromStrings(args);
			
			// INITIALIZE .INI CONFIGURATION (ScreepT + Launcher) FROM CONFIG DEFAULT FILES
			try {
				loaderGraphics.update("Initializing user configuration", 10);
				
				iniFileManager = new IniFileManager(GeneralConfig.DEFAULT_LAUNCHER_INI_PATH);
				iniFileManager.initFromINIFile();
				LOG_LEVEL_AT_START = iniFileManager.logLevel;
				logger.info("Chosen log level: " + LOG_LEVEL_AT_START);
				
				if(LOG_LEVEL_AT_START != null && LOG_LEVEL_AT_START.equals(Level.OFF)) {
					logger.warn("Logger disabled by user choice: nothing will be logged in this session.");
				}
				
				LogManager.getRootLogger().setLevel(LOG_LEVEL_AT_START);

				currentConfiguration = new INItializer(new Wini(new File(GeneralConfig.DEFAULT_INI)));
				currentConfiguration.readConfigIniFile();
				System.setProperty("file.encoding", currentConfiguration.getGenOpt().getCharset());
				
			} catch (IOException e) {
				logger.error("Exception happened during loading of file [ "+ GeneralConfig.RESOURCES_DIR  + "CONFIG.ini]!", e);
				throw e;
			}

			SoundsConfigurator.DEFAULT_EFFECTS_VOLUME = currentConfiguration.getGenOpt().getGeneralVolume();
			fileDialogHelper = new GenericFileChooserDialog(currentConfiguration);
			
			printElapsed("Initial configuration completed", startTime);
			loaderGraphics.update("Initializing main application...", 15);
			
			startSafeKeyBoardThreadListener();
			
			/*************************
			 * LAUNCH MAIN APPLICATION
			 *************************/
			boolean mainAppRunning = MainGUI.mainApplicationGeneral(currentConfiguration);
			GuiUtils.forceToFront(MainGUI.mainAPP.getMainFrame());
			/** MainApp: LAUNCHED :)**/
			
			if(!mainAppRunning) {
				throw new Exception("Something has gone wrong during application start-up, please check the logs");
			}
			
			printElapsed("Launch completed", startTime);
			
		// CATCH ALL ERRORS DURING INITIALIZATION, ADVICE USER AND ASK TO EXIT
		} catch (Throwable e) {
			printElapsed("Launch finished with errors" + Arrays.deepToString(e.getStackTrace()), startTime);
			loaderGraphics.setProgBarColor(Color.RED);
			try { Thread.sleep(200); } catch (InterruptedException e1) {}
			loaderGraphics.update("An error occurred during load :(", 100);
			doStuffBeforeErrorClose(false, e);
		}
	}
	
	/**
	 * Main error handler method - this will be launched when user has chosen to continue closing with forceClose = true
	 * 
	 * This method is still present cause it can be used everytime you need to check an error in the application and choose what to do
	 * 
	 * @param forceCloseWithoutAsk
	 * @param t
	 */
	private static void doStuffBeforeErrorClose(boolean forceCloseWithoutAsk, Throwable t) {
		
		try {
			Object res;
			
			if (forceCloseWithoutAsk || ParametricExceptionThread.isCriticalError(t)) {
				logger.error(AWTExceptionHandler.CRITICAL_ERROR_MSG, t);
				SoundsManagerExtended.playSound(SoundsConfigurator.ERROR, null);
				
				if (!forceCloseWithoutAsk) {
					res = dialogHelper.errorScroll(AWTExceptionHandler.CRITICAL_ERROR_MSG + AWTExceptionHandler.DIALOG_USER_ERR_INFO + t.getClass().getCanonicalName(), "Critical Error", t, AWTExceptionHandler.options, JOptionHelperExtended.YES_OR_NO);
					// IF not kill then return back
					if (!AWTExceptionHandler.options[0].equals(res)) {
						// CASE CONTINUE
						return;
					} 
				}
				
				try {
					logger.info(LoggerUtils.getGeneralErrorExitMsg(GeneralConfig.APPLICATION_NAME, true));
				} catch (Exception e) {
					System.out.println("Cannot write to LOGS, maybe there is an error in LOG_properties file (or it does not exist).");
				}

				try {
					MainGuiManager.closeApplication(1);
				} catch (Exception e) {
					System.exit(1);
				}
				
			} else {
				logger.error("An Exception occurred: ", t);
				
				if (currentConfiguration.getGenOpt().isExceptionAdvicesEnabled()) {
					SoundsManagerExtended.playSound(SoundsConfigurator.SOUND_PLING, null);
					res = new JOptionHelperExtended(null).warnScroll(
							"An Exception occurred, but I'still here ;) \n\n"
									+ "(if you don't want to see this message you can disable it going to preferences->general options and deselecting the related checkbox)\n\n => PRESS \"Kill Application\" IF YOU WANT TO CLOSE, TRYING TO SAVE CURRENT WORK\n\n =>"
									+ AWTExceptionHandler.DIALOG_USER_ERR_INFO + t.getClass().getCanonicalName(),
							"An Exception Occurred", t, AWTExceptionHandler.options, JOptionHelperExtended.YES_OR_NO);
				}else {
					// Default Continue
					res = AWTExceptionHandler.options[1];
				}
				if (AWTExceptionHandler.options[0].equals(res)) {
					// CASE KILL APPLICATION
					try {
						MainGuiManager.closeApplication(1);
					} catch (Exception e) {
						System.exit(1);
					}
				} 
			} 
		} catch (Throwable e) {
			System.exit(1);
		}
		
	}
	
	/**
	 * This file will be used as flag to signal other threads to close when it exists no more 
	 * (each external Jar must provide thread must check for this in order to close correctly).
	 * @throws IOException 
	 */
	private static void writeSignalFileForExternalJars(){
		try {
			GeneralConfig.mkDirUserDirScreepT();
			GeneralConfig.SCREEPT_ACTIVE_SIGNAL_FILE.createNewFile();
			FileWorker.writeStringToFile(MAIN_SIGNAL_PROPERTY, GeneralConfig.SCREEPT_ACTIVE_SIGNAL_FILE, true);
		} catch (Exception e) {
			logger.error("Error writing signal file ["+GeneralConfig.SCREEPT_ACTIVE_SIGNAL_FILE.getAbsolutePath()+"]");
		}
	}
	
	/**
	 * This method will delete general signal file to signal that main Application is closed
	 */
	public static void deleteSignalFileForExternalJars() {
		FileUtils.deleteQuietly(GeneralConfig.SCREEPT_ACTIVE_SIGNAL_FILE);
	}
	
	private static long printElapsed(String msg, long startTime) {
		long endTime = System.currentTimeMillis();
		String totMsg = "\n\n [MAIN LOAD PROGRESS] => " + msg + " after " + (endTime - startTime) + " ms (Total time from first executed line)\n";
		if (isLoggerReady && !Level.OFF.equals(LOG_LEVEL_AT_START) && !Level.ERROR.equals(LOG_LEVEL_AT_START) && !Level.FATAL.equals(LOG_LEVEL_AT_START)) {
			logger.info(totMsg);
		}else {
			System.out.println(totMsg);
		}
			
		return endTime;
	}

	/**
	 * Check if plugins are available and update installed list
	 */
	public static void updateInstalledPlugins() {
		ArrayList<String> installedPlugins = new ArrayList<>();
		
		if(MainGuiGeneralAppStarter.tesseractAvailable) {
			installedPlugins.add("Tesseract.zip");
		}
		
		if(MainGuiGeneralAppStarter.jnetPcapAvailable) {
			installedPlugins.add("JnetPcap.zip");
		}

		if(MainGuiGeneralAppStarter.voiceSpeakerAvailable) {
			installedPlugins.add("TextToSpeech.zip");
		}
		
		if(MainGuiGeneralAppStarter.hwInfoAvailable) {
			installedPlugins.add("HardwareInfo.zip");
		}
		
		PluginManager.installedPluginProperties.saveCommentedProperty(
				PluginManager.installedPluginPropsListKey, PropertiesManager.listToString(installedPlugins, PluginManager.installedPluginPropsListSeparator));
	}
	
	/**
	 * This is the separated thread with key listening for emergency operations like quitting application in case of stuck
	 */
	public static void startSafeKeyBoardThreadListener() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new SafetyScreepTShortcutsListener());
			}
		}).start();
	}
	
	public static void checkEULAstatus() {
		// SHOW LICENSE IF MARKED AS UNACCEPTED
		if(!GeneralConfig.LICENSE_ACCEPTED) {
			
			LightLicenseViewer licenseViewer;
			UserLicenseChoice choice;
			do{
				licenseViewer = new LightLicenseViewer(GeneralConfig.LICENSE_FILE, "You must accept this EULA before to start using ScreepT", "License acceptance");
				choice = licenseViewer.userChoice;
				if(choice.equals(UserLicenseChoice.REJECTED)) {
					if (new JOptionHelper(null).yesOrNo("You cannot use ScreepT without accepting License, wanna exit launch procedure?")) {
						System.exit(0);
					}
				}else {
					GeneralConfig.licenseProperties.saveCommentedProperty("LICENSE_ACCEPTED", String.valueOf(true));
				}
				
			} while(choice.equals(UserLicenseChoice.REJECTED));
		}
	}
}
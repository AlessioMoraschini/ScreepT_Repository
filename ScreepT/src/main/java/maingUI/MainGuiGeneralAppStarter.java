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
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.KeyboardFocusManager;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.PropertyConfigurator;
import org.ini4j.Wini;

import gui.commons.dialogutils.GenericFileChooserDialog;
import gui.commons.dialogutils.JOptionHelperExtended;
import gui.commons.frameutils.utils.GuiUtilsExtended;
import gui.commons.handlers.AWTExceptionHandler;
import initializer.configs.impl.INItializer;
import plugin.api.AbstractPluginApplicationApi;
import plugin.api.AbstractPluginApplicationApi.PluginType;
import plugin.external.arch.IPlugin;
import resources.GeneralConfig;
import resources.ImagesPathConfigurator;
import resources.SoundsConfigurator;
import resources.SoundsManagerExtended;
import screept.general.utils.SafetyScreepTShortcutsListener;
import screept.general.utils.ScreepTLoggerUtils;
import screept.gui.utils.dependencycheck.LibChecker;
import screept.launcher.gui.utils.LauncherLoaderFrame;
import screept.launcher.utils.IniFileManager;
import screept.plugins.ScreepTApplicationApi;
import updater.UpdatesManager;
import updater.module.gui.PluginManagerGUI;
import updater.module.plugins.PluginManager;
import various.common.light.files.FileVarious;
import various.common.light.files.FileWorker;
import various.common.light.gui.GuiUtils;
import various.common.light.gui.LightLicenseViewer;
import various.common.light.gui.LightLicenseViewer.UserLicenseChoice;
import various.common.light.gui.dialogs.msg.JOptionHelper;
import various.common.light.network.proxy.ProxySettingsGUI;
import various.common.light.runtime.ParametricExceptionThread;
import various.common.light.utility.log.SafeLogger;
import various.common.light.utility.properties.PropertiesManager;
import various.common.light.utility.time.Benchmarker;

public class MainGuiGeneralAppStarter {

	public static final String MAIN_SIGNAL_PROPERTY = "MAIN_APP_IS_ACTIVE";

	public static final Benchmarker SCREEPT_BENCHMARKER = new Benchmarker(true);
	static {
		SCREEPT_BENCHMARKER.start();
		SCREEPT_BENCHMARKER.registerEvent("Starting Application initializer [MainGuiGeneralAppStarter]...");
	}

	public static AbstractPluginApplicationApi externalPluginAppProvider;

	public static Level LOG_LEVEL_AT_START;

	// mainGUI logger
	static File preInitializationLogFile = new File("./pre_initialization.log");
	static SafeLogger logger = new SafeLogger(MainGuiGeneralAppStarter.class);

	// LOADER
	public static LauncherLoaderFrame loaderGraphics;
	public static final Dimension screenSize = GuiUtilsExtended.getScreenSize();
	public static final Dimension minimumSize = new Dimension(880,500);
	public static final Dimension minimumSizeTextEditor = new Dimension(1100,600);
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

	public static EasterEggsInitializer easterEggInitializer;

	public static Thread getLibcheckerThread() {
		return new Thread(()-> {

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
		});
	}

	public static Thread getConfigInitializerThread() {
		return new Thread(()-> {
			// INITIALIZE .INI CONFIGURATION (ScreepT + Launcher) FROM CONFIG DEFAULT FILES
			try {
				loaderGraphics.update("Initializing user configuration", 1);

				currentConfiguration = new INItializer(new Wini(new File(GeneralConfig.DEFAULT_INI)));
				currentConfiguration.readConfigIniFile();
				System.setProperty("file.encoding", currentConfiguration.getGenOpt().getCharset());

			} catch (IOException e) {
				logger.error("Exception happened during loading of file [ "+ GeneralConfig.RESOURCES_DIR  + "CONFIG.ini]!", e);
				throw new RuntimeException(e);
			}
		});
	}

	public static Thread getJsCodeFormatterInitializerThread() {
		return new Thread(()-> {
			// INITIALIZE .INI CONFIGURATION (ScreepT + Launcher) FROM CONFIG DEFAULT FILES
			try {
				GuiUtilsExtended.init();

			} catch (Exception e) {
				logger.error("Exception happened during loading of file [ "+ GeneralConfig.RESOURCES_DIR  + "CONFIG.ini]!", e);
				throw new RuntimeException(e);
			}
		});
	}

	public static void startAsyncDynaThreads(Thread... threads) {
		String syncAsyncMode = GeneralConfig.ASYNC_MODULES_LOAD ? "asyncronous" : "syncronous";

		for(Thread t : threads) {
			SCREEPT_BENCHMARKER.registerEvent("Starting " + syncAsyncMode + " module...");
			t.start();
			if(!GeneralConfig.ASYNC_MODULES_LOAD)
				try {
					t.join();
				} catch (InterruptedException e) {
					logger.error("Interrupted thread!", e);
				}
		}
	}


	public static void main(String[] args) {
		System.setProperty("sun.java2d.dpiaware", "true");
		long startTime = System.currentTimeMillis();
		logger.setLoggerAvailable(false);

		logger.info("Starting MainGuiGeneralAppStarter.main(" + Arrays.deepToString(args) + ")");
		SCREEPT_BENCHMARKER.registerEvent("Main function entered");

		SCREEPT_BENCHMARKER.registerEvent("Start EULA check");
		checkEULAstatus();

		SCREEPT_BENCHMARKER.registerEvent("Loader graphics initialization");
		loaderGraphics = new LauncherLoaderFrame("Loading Launcher...", ImagesPathConfigurator.LAUNCHER_BACK_IMAGE_PATH, null);
		loaderGraphics.setIconImage(new ImageIcon(ImagesPathConfigurator.MAIN_APP_ICON).getImage());
		loaderGraphics.indeterminate(true, "Loading configuration");

		try {
			Thread libCheckerThread = getLibcheckerThread();
			Thread configInitializerThread = getConfigInitializerThread();
			Thread jsCodeFormatterInitializerThread = getJsCodeFormatterInitializerThread();

			try {

				// Load Internal configuration and dialog helper
				printElapsed("Main Properties initialization started...", startTime);
				SCREEPT_BENCHMARKER.registerEvent("General config initialization started");

				// Load main properties file
				GeneralConfig.initLazyGeneralConfig();

				SCREEPT_BENCHMARKER.registerEvent("General config initialization finished");
				printElapsed("Main Properties initialized!", startTime);

				startAsyncDynaThreads(libCheckerThread, configInitializerThread, jsCodeFormatterInitializerThread);

				SCREEPT_BENCHMARKER.registerEvent("Dialog helpers initialization started");
				dialogHelper = new JOptionHelperExtended(null);
				SCREEPT_BENCHMARKER.registerEvent("JOption helper initialization completed");
				fileDialogHelper = new GenericFileChooserDialog(currentConfiguration);
				SCREEPT_BENCHMARKER.registerEvent("File chooser initialization completed");

				SCREEPT_BENCHMARKER.registerEvent("Static helpers vars initialization started");
				JOptionHelperExtended.init();
				AWTExceptionHandler.init();
				SCREEPT_BENCHMARKER.registerEvent("Static helpers vars initialization completed");

				try {
					UIManager.put("Button.defaultButtonFollowsFocus", Boolean.TRUE);
				} catch (Exception e) {
					e.printStackTrace();
				}

				try {
					// Preload network configuration
					SCREEPT_BENCHMARKER.registerEvent("network config loading started");
					loaderGraphics.update("Preloading Network configuration", 1);
					new ProxySettingsGUI(ModalityType.TOOLKIT_MODAL, true, GeneralConfig.DEF_PROXY_CONFIG_STRING);
					SCREEPT_BENCHMARKER.registerEvent("network config loading finished");
					logger.info("Network configuration applied");
				} catch (Exception e) {
					logger.info("An error occurred while preloading application's network settings");
				}

			} catch (Exception e) {
				SoundsManagerExtended.playSound(SoundsConfigurator.ERROR, null);
				dialogHelper.errorScroll("Cannot load internal configuration File: " + AWTExceptionHandler.ERROR_FILE_REASONS + "resourcesFilePathsMappings.properties", "Cannot load configuration", e);
				throw e;
			}

			loaderGraphics.indeterminate(false);
			printElapsed("dialogHelper and GeneralConfig initialization finished", startTime);

			try {

				// READ LOGLEVEL FROM LAUNCHER .INI CONFIGURATION
				iniFileManager = new IniFileManager(GeneralConfig.DEFAULT_LAUNCHER_INI_PATH);
				iniFileManager.initFromINIFile();
				LOG_LEVEL_AT_START = iniFileManager.logLevel;
				logger.info("Chosen log level: " + LOG_LEVEL_AT_START);

				if(LOG_LEVEL_AT_START != null && LOG_LEVEL_AT_START.equals(Level.OFF)) {
					logger.warn("Logger disabled by user choice: nothing will be logged in this session.");
				}

				// APPLY LOG LEVEL TO LOG4J properties file
				PropertiesManager loggerProps = new PropertiesManager(GeneralConfig.LOG_PROPERTIES_PATH);
				loggerProps.saveCommentedProperty("log4j.rootLogger", LOG_LEVEL_AT_START + ", FILE, console");

				// Initialize logger properties
				SCREEPT_BENCHMARKER.registerEvent("Loading logger properties");
				PropertyConfigurator.configure(GeneralConfig.LOG_PROPERTIES_PATH);
				logger.setLoggerAvailable(true);

				logger.info(ScreepTLoggerUtils.getStartString(GeneralConfig.APPLICATION_NAME, true));

				// Apply log level
				LogManager.getRootLogger().setLevel(LOG_LEVEL_AT_START);

				printElapsed("Log properties loaded", startTime);

			} catch (Exception e) {
				logger.error("There was a critical error when trying to load Logger! Application cannot start.");
				SoundsManagerExtended.playSound(SoundsConfigurator.ERROR, null);
				dialogHelper.errorScroll("Critical error: Cannot load internal logging properties File: " + AWTExceptionHandler.ERROR_FILE_REASONS
						+GeneralConfig.LOG_PROPERTIES_PATH, "Cannot load configuration", e);
				throw e;
			}

			try {
				// Load updates manager module
				SCREEPT_BENCHMARKER.registerEvent("Loading updates manager");
				loaderGraphics.update("Starting updates manager...", 5);
				updatesManager = new UpdatesManager(new URL(GeneralConfig.WEB_UPDATES_MANIFEST_URL), GeneralConfig.APP_VERSION);
				updatesManager.emptyUpdatesDir();
				logger.info("Updates manager started!");
			} catch (Exception e) {
				logger.error("Cannot initialize Updates Manager :(", e);
			}

			try {
				// Load plugin manager module
				SCREEPT_BENCHMARKER.registerEvent("Starting plugin manager");
				loaderGraphics.update("Starting plugin manager...", 6);
				pluginManager = new PluginManager();
				logger.info("Plugin manager started!");
			} catch (Exception e) {
				logger.error("Cannot initialize Plugin Manager :(", e);
			}

			if(GeneralConfig.ASYNC_MODULES_LOAD) {
				SCREEPT_BENCHMARKER.registerEvent("Waiting async configuration loading...");
				configInitializerThread.join();
			}

			SCREEPT_BENCHMARKER.registerEvent("Starting Main AWT Exception handler");
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

			SCREEPT_BENCHMARKER.registerEvent("Writing signal file for other instances");
			writeSignalFileForExternalJars();
			argumentsFiles = FileVarious.getFileArrayFromStrings(args);

			SoundsConfigurator.DEFAULT_EFFECTS_VOLUME = currentConfiguration.getGenOpt().getGeneralVolume();
			fileDialogHelper = new GenericFileChooserDialog(currentConfiguration);

			printElapsed("Initial configuration completed", startTime);
			loaderGraphics.update("Initializing main application...", 15);

			SCREEPT_BENCHMARKER.registerEvent("Starting key listener");
			startSafeKeyBoardThreadListener();

			if(GeneralConfig.ASYNC_MODULES_LOAD) {
				SCREEPT_BENCHMARKER.registerEvent("Waiting async plugin checker...");
				libCheckerThread.join();
			}

			/*************************
			 * LAUNCH MAIN APPLICATION
			 *************************/
			SCREEPT_BENCHMARKER.registerEvent("Configurations loaded: Starting main Application");
			boolean mainAppRunning = MainGUI.mainApplicationGeneral(currentConfiguration);
			SCREEPT_BENCHMARKER.registerEvent("Application started");
			GuiUtils.forceToFront(MainGUI.mainAPP.getMainFrame());
			/** MainApp: LAUNCHED :)**/

			externalPluginAppProvider = AbstractPluginApplicationApi.registerProvider(new ScreepTApplicationApi());

			if(GeneralConfig.ASYNC_MODULES_LOAD) {
				SCREEPT_BENCHMARKER.registerEvent("Waiting async jsCodeFormatterInitializerThread...");
				jsCodeFormatterInitializerThread.join();
			}

			if(!mainAppRunning) {
				throw new Exception("Something has gone wrong during application start-up, please check the logs");
			}

			try {
				// SETUP TOOLTIPS PROPERTIES
				SwingUtilities.invokeLater(() -> {
					Font tooltipFontFromProps = (GeneralConfig.TOOLTIP_FONT == null)? new Font("Segoe UI", Font.PLAIN, 14) : GeneralConfig.TOOLTIP_FONT;
					GuiUtils.setupTooltips(GeneralConfig.TOOLTIP_DELAY, GeneralConfig.TOOLTIP_TIMEOUT, GeneralConfig.TOOLTIP_RESHOW_DELAY, tooltipFontFromProps);
				});
			} catch (Exception e) {
				SoundsManagerExtended.playSound(SoundsConfigurator.ERROR, null);
				dialogHelper.errorScroll("Cannot setup Tooltips general graphics, an error occurred", "Cannot load Tooltips props", e);
			}

			easterEggInitializer = new EasterEggsInitializer(MainGUI.mainGuiUtils);

			PluginManagerGUI.dialogHelper = dialogHelper;

			SCREEPT_BENCHMARKER.registerEvent("Application launched correctly!");
			SCREEPT_BENCHMARKER.finalize();
			printElapsed("Launch completed", startTime);

			logger.debug("\n\n(ASYNC_MODULES_LOAD: " + GeneralConfig.ASYNC_MODULES_LOAD + ")\n\n############################\nSCREEPT_BENCHMARKER Analysys:\n\n" + SCREEPT_BENCHMARKER.getSortedReportString()
			+ "\n\n############################\n\n");

			GuiUtils.giveFocusToComponent(MainGUI.mainFrameScreept);
			// CATCH ALL ERRORS DURING INITIALIZATION, ADVICE USER AND ASK TO EXIT
		} catch (Throwable e) {
			SCREEPT_BENCHMARKER.registerEvent("Error launching application");
			SCREEPT_BENCHMARKER.finalize();
			logger.debug("\n\n(ASYNC_MODULES_LOAD: " + GeneralConfig.ASYNC_MODULES_LOAD + ")\n\n############################\nSCREEPT_BENCHMARKER Analysys:\n\n" + SCREEPT_BENCHMARKER.getSortedReportString()
			+ "\n\n############################\n\n");

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

		Object userChoice;
		try {
			if (forceCloseWithoutAsk || ParametricExceptionThread.isCriticalError(t)) {

				logger.error(AWTExceptionHandler.CRITICAL_ERROR_MSG, t);
				SoundsManagerExtended.playSound(SoundsConfigurator.ERROR, null);

				if (!forceCloseWithoutAsk) {
					userChoice = dialogHelper.errorScroll(AWTExceptionHandler.CRITICAL_ERROR_MSG + AWTExceptionHandler.DIALOG_USER_ERR_INFO + t.getClass().getCanonicalName(), "Critical Error", t, AWTExceptionHandler.options, JOptionHelperExtended.YES_OR_NO);
					// IF not kill then return back
					if (!AWTExceptionHandler.options[0].equals(userChoice)) {
						// CASE CONTINUE
						return;
					}
				}

				try {
					logger.info(ScreepTLoggerUtils.getGeneralErrorExitMsg(GeneralConfig.APPLICATION_NAME, true));
				} catch (Exception e) {
					System.out.println("Cannot write to LOGS, maybe there is an error in LOG_properties file (or it does not exist).");
				}

				try {
					MainGuiManager.closeApplication(1);
				} catch (Exception e) {
					System.exit(1);
				}

			} else {
				// Case EXCEPTION

				logger.error("An Exception occurred: ", t);

				if (currentConfiguration.getGenOpt().isExceptionAdvicesEnabled()) {
					SoundsManagerExtended.playSound(SoundsConfigurator.SOUND_PLING, null);
					userChoice = new JOptionHelperExtended(null).warnScroll(
							"An Exception occurred, but I'still here ;) \n\n"
									+ "(if you don't want to see this message you can disable it going to preferences->general options and deselecting the related checkbox)\n\n => PRESS \"Kill Application\" IF YOU WANT TO CLOSE, TRYING TO SAVE CURRENT WORK\n\n =>"
									+ AWTExceptionHandler.DIALOG_USER_ERR_INFO + t.getClass().getCanonicalName(),
									"An Exception Occurred", t, AWTExceptionHandler.options, JOptionHelperExtended.YES_OR_NO);
				}else {
					// Default Continue
					userChoice = AWTExceptionHandler.options[1];
				}
				if (AWTExceptionHandler.options[0].equals(userChoice)) {
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
		logger.info(totMsg);

		return endTime;
	}

	/**
	 * Check if plugins are available and update installed list
	 */
	public static void updateInstalledPlugins() {
		ArrayList<String> installedPlugins = new ArrayList<>();

		ArrayList<String> toRemove = new ArrayList<>();
		ArrayList<String> toAdd = new ArrayList<>();

		if(MainGuiGeneralAppStarter.tesseractAvailable) {
			toAdd.add("Tesseract.zip");
		} else {
			toRemove.add("Tesseract.zip");
		}

		if(MainGuiGeneralAppStarter.jnetPcapAvailable) {
			toAdd.add("JnetPcap.zip");
		} else {
			toRemove.add("JnetPcap.zip");
		}

		if(MainGuiGeneralAppStarter.voiceSpeakerAvailable) {
			toAdd.add("TextToSpeech.zip");
		} else {
			toRemove.add("TextToSpeech.zip");
		}

		if(MainGuiGeneralAppStarter.hwInfoAvailable) {
			toAdd.add("HardwareInfo.zip");
		} else {
			toRemove.add("HardwareInfo.zip");
		}

		for(String rm : toRemove)
			installedPlugins.remove(rm);

		for(String add : toAdd)
			installedPlugins.add(add);

		Set<IPlugin> plugins = ScreepTApplicationApi.getAvailablePlugins(PluginType.ROOT, false, false);
		if(plugins != null && !plugins.isEmpty()) {
			for(IPlugin plugin : plugins) {
				if(!plugin.isTestPlugin() && !toRemove.contains(plugin.getPluginZipName()))
					installedPlugins.add(plugin.getPluginZipName());
			}
		}

		PluginManager.installedPluginProperties.savePropertyChecked(
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
				licenseViewer = new LightLicenseViewer(
						GeneralConfig.LICENSE_FILE,
						"You must accept this EULA before to start using ScreepT",
						"License acceptance",
						new boolean[] {true, false, false}
						);
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
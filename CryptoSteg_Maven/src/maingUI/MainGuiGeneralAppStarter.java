package maingUI;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.ini4j.Wini;

import amfileutils.FileStuff;
import amfileutils.filechangewatcher.FileChangeMonitor;
import dialogutils.GenericFileChooserDialog;
import dialogutils.JOptionHelper;
import frameutils.utils.GuiUtils;
import generic.ParametricExceptionThread;
import gui.utils.LauncherLoaderFrame;
import handlers.AWTExceptionHandler;
import launcherutils.IniFileManager;
import logger.LoggerUtils;
import mainobj.INItializer;
import media.Sounds;
import resources.GeneralConfig;
import resources.ImagesPathConfigurator;
import resources.SoundsConfigurator;
import uTILS.dependencycheck.LibChecker;
import updater.PluginManager;
import updater.UpdatesManager;
import variousutils.HandleString;

public class MainGuiGeneralAppStarter {
	
	public static final String MAIN_SIGNAL_PROPERTY = "MAIN_APP_IS_ACTIVE";

    public static Level LOG_LEVEL_AT_START;
    private static boolean isLoggerReady = false;
	
	// mainGUI logger
	static Logger logger = Logger.getLogger(MainGuiGeneralAppStarter.class);
	
	// LOADER
	public static LauncherLoaderFrame loaderGraphics;
	public static final Dimension screenSize = GuiUtils.getScreenSize();
	public static final Dimension minimumSize = new Dimension(1400,650);
		
	// Main configuration reference for all sub-modules
	private static INItializer currentConfiguration;
	// and another one for launcher modding from main app
	private static Wini launcherConfig;
	
	public static String[] ARGS;
	public static File[] argumentsFiles;
	
	public static JOptionHelper dialogHelper;
	public static AWTExceptionHandler mainTrheadAwtErrorHandler;
	
	public static GenericFileChooserDialog fileDialogHelper;
	
	public static UpdatesManager updatesManager;
	public static PluginManager pluginManager;
	
	// LIbs availability
	public static boolean tesseractAvailable;
	public static boolean voiceSpeakerAvailable;
	
	public static void main(String[] args) {
		
		long startTime = System.currentTimeMillis();
		
		loaderGraphics = new LauncherLoaderFrame("Loading Launcher...", ImagesPathConfigurator.LAUNCHER_BACK_IMAGE_PATH, null);
		loaderGraphics.indeterminate(true, "Loading configuration");
		
		try {
			try {
				String initPropsMsg = "Preloading configuration";
				loaderGraphics.update(initPropsMsg, 1);
				loaderGraphics.indeterminate(true, initPropsMsg);
				
				// Load Internal configuration and dialog helper
				GeneralConfig.initLazyGeneralConfig();

				dialogHelper = new JOptionHelper(null);
				fileDialogHelper = new GenericFileChooserDialog(currentConfiguration);
				
				GuiUtils.init();
				JOptionHelper.init();
				AWTExceptionHandler.init();
				
				try {
					UIManager.put("Button.defaultButtonFollowsFocus", Boolean.TRUE);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			} catch (Exception e) {
				Sounds.playSound(SoundsConfigurator.ERROR, null);
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
				Sounds.playSound(SoundsConfigurator.ERROR, null);
				dialogHelper.errorScroll("Critical error: Cannot load internal logging properties File: " + AWTExceptionHandler.ERROR_FILE_REASONS 
						+GeneralConfig.LOG_PROPERTIES_PATH, "Cannot load configuration", e);
				throw e;
			}
			
			try {
				// Load updates manager module
				loaderGraphics.update("Starting updates manager...", 5);
				updatesManager = new UpdatesManager(new URL(GeneralConfig.WEB_UPDATES_MANIFEST_URL), GeneralConfig.APP_VERSION);
				updatesManager.emtpyUpdatesDir();
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
				Font tooltipFontFromProps = ("".equals(GeneralConfig.TOOLTIP_FONT))? new Font("Segoe UI", Font.PLAIN, 14) : HandleString.getFontFromString(GeneralConfig.TOOLTIP_FONT);
				GuiUtils.setupTooltips(GeneralConfig.TOOLTIP_DELAY, GeneralConfig.TOOLTIP_TIMEOUT, GeneralConfig.TOOLTIP_RESHOW_DELAY, tooltipFontFromProps);
			
			} catch (Exception e) {
				Sounds.playSound(SoundsConfigurator.ERROR, null);
				dialogHelper.errorScroll("Cannot setup Tooltips general graphics, an error occurred", "Cannot load Tooltips props", e);
			}
			
			printElapsed("DialogHelper, General config and tooltips setup finished", startTime);
			
			printElapsed("Log properties loaded", startTime);
			
			writeSignalFileForExternalJars();
			argumentsFiles = FileStuff.getFileArrayFromStrings(args);
			
			// INITIALIZE .INI CONFIGURATION (ScrYpT + Launcher) FROM CONFIG DEFAULT FILES
			try {
				loaderGraphics.update("Initializing user configuration", 10);
				
				launcherConfig = new Wini(new File(GeneralConfig.DEFAULT_LAUNCHER_INI_PATH));
				LOG_LEVEL_AT_START = LoggerUtils.getLogLevelFromString(launcherConfig.get(IniFileManager.INI_APP_OPT, "logLevelSetting"));
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
			
			/*************************
			 * LAUNCH MAIN APPLICATION
			 *************************/
			boolean mainAppRunning = MainGUI.mainApplicationGeneral(currentConfiguration);
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
				Sounds.playSound(SoundsConfigurator.ERROR, null);
				
				if (!forceCloseWithoutAsk) {
					res = dialogHelper.errorScroll(AWTExceptionHandler.CRITICAL_ERROR_MSG + AWTExceptionHandler.DIALOG_USER_ERR_INFO + t.getClass().getCanonicalName(), "Critical Error", t, AWTExceptionHandler.options, JOptionHelper.YES_OR_NO);
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
					MainGUI.closeApplication(1);
				} catch (Exception e) {
					System.exit(1);
				}
				
			} else {
				logger.error("An Exception occurred: ", t);
				
				if (currentConfiguration.getGenOpt().isExceptionAdvicesEnabled()) {
					Sounds.playSound(SoundsConfigurator.SOUND_PLING, null);
					res = new JOptionHelper(null).warnScroll(
							"An Exception occurred, but I'still here ;) \n\n"
									+ "(if you don't want to see this message you can disable it going to preferences->general options and deselecting the related checkbox)\n\n => PRESS \"Kill Application\" IF YOU WANT TO CLOSE, TRYING TO SAVE CURRENT WORK\n\n =>"
									+ AWTExceptionHandler.DIALOG_USER_ERR_INFO + t.getClass().getCanonicalName(),
							"An Exception Occurred", t, AWTExceptionHandler.options, JOptionHelper.YES_OR_NO);
				}else {
					// Default Continue
					res = AWTExceptionHandler.options[1];
				}
				if (AWTExceptionHandler.options[0].equals(res)) {
					// CASE KILL APPLICATION
					try {
						MainGUI.closeApplication(1);
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
			GeneralConfig.mkDirUserDirScrYpT();
			GeneralConfig.SCRYPT_ACTIVE_SIGNAL_FILE.createNewFile();
			FileStuff.writeStringToFile(MAIN_SIGNAL_PROPERTY, GeneralConfig.SCRYPT_ACTIVE_SIGNAL_FILE, true);
		} catch (Exception e) {
			logger.error("Error writing signal file ["+GeneralConfig.SCRYPT_ACTIVE_SIGNAL_FILE.getAbsolutePath()+"]");
		}
	}
	
	/**
	 * This method will delete general signal file to signal that main Application is closed
	 */
	public static void deleteSignalFileForExternalJars() {
		FileUtils.deleteQuietly(GeneralConfig.SCRYPT_ACTIVE_SIGNAL_FILE);
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
}
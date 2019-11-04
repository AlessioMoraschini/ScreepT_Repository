package maingUI;

import java.awt.Font;
import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.ini4j.Wini;

import amfileutils.FileStuff;
import dialogutils.JOptionHelper;
import frameutils.utils.GuiUtils;
import generic.ParametricExceptionThread;
import handlers.AWTExceptionHandler;
import launcherutils.IniFileManager;
import logger.LoggerUtils;
import mainlauncher.ScrYpTLauncher;
import mainobj.INItializer;
import media.Sounds;
import resources.GeneralConfig;
import resources.SoundsConfigurator;
import variousutils.HandleString;

public class MainGuiGeneralAppStarter {

    public static Level LOG_LEVEL_AT_START;
	
	// mainGUI logger
	static Logger logger = Logger.getLogger(MainGuiGeneralAppStarter.class);
		
	// used with the only aim to force GeneralConfig properties read
	private static GeneralConfig configStarter;
	
	// Main configuration reference for all sub-modules
	private static INItializer currentConfiguration;
	// and another one for launcher modding from main app
	private static Wini launcherConfig;
	
	public static String[] ARGS;
	public static File[] argumentsFiles;
	
	public static JOptionHelper dialogHelper;
	public static AWTExceptionHandler mainTrheadAwtErrorHandler;
	
	public static void main(String[] args) {
		try {
			
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
			
			try {
				// start Properties
				dialogHelper = new JOptionHelper(null);
				configStarter = new GeneralConfig();
				
				// SETUP TOOLTIPS PROPERTIES
				Font tooltipFontFromProps = ("".equals(GeneralConfig.TOOLTIP_FONT))? new Font("Segoe UI", Font.PLAIN, 14) : HandleString.getFontFromString(GeneralConfig.TOOLTIP_FONT);
				GuiUtils.setupTooltips(GeneralConfig.TOOLTIP_DELAY, GeneralConfig.TOOLTIP_TIMEOUT, GeneralConfig.TOOLTIP_RESHOW_DELAY, tooltipFontFromProps);
			
			} catch (Exception e) {
				Sounds.playSound(SoundsConfigurator.ERROR, 0.8f);
				dialogHelper.errorScroll("Cannot load internal configuration File: " + AWTExceptionHandler.ERROR_FILE_REASONS + "resourcesFilePathsMappings.properties", "Cannot load configuration", e);
				throw e;
			}
			
			try {
				// Initialize logger properties
				PropertyConfigurator.configure(GeneralConfig.LOG_PROPERTIES_PATH);
				logger.info(LoggerUtils.getStartString(GeneralConfig.APPLICATION_NAME, true));
				
			} catch (Exception e) {
				Sounds.playSound(SoundsConfigurator.ERROR, 0.8f);
				dialogHelper.errorScroll("Cannot load internal logging properties File: " + AWTExceptionHandler.ERROR_FILE_REASONS 
						+GeneralConfig.LOG_PROPERTIES_PATH, "Cannot load configuration", e);
				throw e;
			}
			
			writeSignalFileForExternalJars();
			argumentsFiles = FileStuff.getFileArrayFromStrings(args);
			
			// INITIALIZE .INI CONFIGURATION (ScrYpT + Launcher) FROM CONFIG DEFAULT FILES
			try {
				launcherConfig = new Wini(new File(ScrYpTLauncher.DEFAULT_INI_PATH));
				LOG_LEVEL_AT_START = LoggerUtils.getLogLevelFromString(launcherConfig.get(IniFileManager.INI_APP_OPT, "logLevelSetting"));
				logger.info("Chosen log level: " + LOG_LEVEL_AT_START);
				if(LOG_LEVEL_AT_START != null && LOG_LEVEL_AT_START.equals(Level.OFF)) {
					logger.warn("Logger disabled by user choice: nothing will be logged in this session.");
				}
				LogManager.getRootLogger().setLevel(LOG_LEVEL_AT_START);

				currentConfiguration = new INItializer(new Wini(new File(GeneralConfig.DEFAULT_INI)));
				currentConfiguration.readConfigIniFile();
				
				SoundsConfigurator.DEFAULT_EFFECTS_VOLUME = currentConfiguration.getGenOpt().getGeneralVolume();

			} catch (IOException e) {
				logger.error("Exception happened during loading of file [ "+ GeneralConfig.RESOURCES_DIR  + "CONFIG.ini]!", e);
			}
			
			/*************************
			 * LAUNCH MAIN APPLICATION
			 *************************/
			MainGUI.mainApplicationGeneral(currentConfiguration);
			/** MainApp: LAUNCHED :)**/
			MainGUI.mainAppRunning = true;
			
		// CATCH ALL ERRORS DURING INITIALIZATION, ADVICE USER AND ASK TO EXIT
		} catch (Throwable e) {
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
				Sounds.playSound(SoundsConfigurator.ERROR, 0.6f);
				
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
					Sounds.playSound(SoundsConfigurator.SOUND_PLING, 0.5f);
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
			FileStuff.writeStringToFile("MAIN_APP_IS_ACTIVE", GeneralConfig.SCRYPT_ACTIVE_SIGNAL_FILE, true);
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
}
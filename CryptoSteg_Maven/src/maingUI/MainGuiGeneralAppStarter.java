package maingUI;

import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import amfileutils.FileStuff;
import frameutils.utils.GuiUtils;
import logger.LoggerUtils;
import resources.GeneralConfig;
import resources.IconsPathConfigurator;
import variousutils.HandleString;

public class MainGuiGeneralAppStarter {

	// use this to test throwable launched
	private static final boolean TEST_ERROR_FLAG = false;
	
	// mainGUI logger
	static Logger logger = Logger.getLogger(MainGuiGeneralAppStarter.class);
	// log_trace location
		
	public static final String CRITICAL_ERROR_MSG = "An unexpected problem caused a critical error: ";
	private static Date now;
	private static GeneralConfig configStarter;
	
	public static String[] ARGS;
	public static File[] argumentsFiles;
	
	public static void main(String[] args) {
		try {
			
			now = new Date();
			
			configStarter = new GeneralConfig();

			// Initialize logger properties
			PropertyConfigurator.configure(GeneralConfig.LOG_PROPERTIES_PATH);
			
			writeSignalFileForExternalJars();
			
			logger.info(LoggerUtils.getStartString(GeneralConfig.APPLICATION_NAME, true));

			argumentsFiles = FileStuff.getFileArrayFromStrings(args);
			
			// SETUP TOOLTIPS PROPERTIES
			Font tooltipFontFromProps = (GeneralConfig.TOOLTIP_FONT.equals(""))? new Font("Segoe UI", Font.PLAIN, 14) : HandleString.getFontFromString(GeneralConfig.TOOLTIP_FONT);
			GuiUtils.setupTooltips(GeneralConfig.TOOLTIP_DELAY, GeneralConfig.TOOLTIP_TIMEOUT, tooltipFontFromProps);
			
			// LAUNCH MAIN APPLICATION
			MainGUI.mainApplicationGeneral();
			
			while(MainGUI.mainAppRunning) {
				try {
					Thread.sleep(20000);
				} catch (InterruptedException e) {
					logger.warn("\n\n\n THE ERROR WAITER MODULE HAS BEEN INTERRUPTED! (if not followed by exit log this could mean there was some problems) \n\n\n");
				}
			}
			
		} catch (Throwable e) {
			logger.error(CRITICAL_ERROR_MSG, e);
			JOptionPane.showMessageDialog(null, CRITICAL_ERROR_MSG+e.getClass().getName(), "Critical Error",
					JOptionPane.ERROR_MESSAGE, new ImageIcon(IconsPathConfigurator.ICON_BIG_GEN_ERROR));
			doStuffBeforeErrorClose();
		}
	}
	
	private static void doStuffBeforeErrorClose() {
		now = new Date();
		
		logger.info(LoggerUtils.getGeneralErrorExitMsg(GeneralConfig.APPLICATION_NAME, true));
		
		deleteSignalFileForExternalJars();
		
		System.exit(1);
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

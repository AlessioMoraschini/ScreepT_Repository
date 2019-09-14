package maingUI;

import java.io.File;
import java.util.Date;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import amfileutils.FileStuff;
import resources.GeneralConfig;
import resources.IconsPathConfigurator;

public class MainGuiGeneralAppStarter {

	// use this to test throwable launched
	private static final boolean TEST_ERROR_FLAG = false;
	
	// mainGUI logger
	static Logger logger = Logger.getLogger(MainGuiGeneralAppStarter.class);
	// log_trace location
		
	public static final String CRITICAL_ERROR_MSG = "An unexpected problem caused a critical error: ";
	private static Date now;
	private GeneralConfig configStarter;
	
	public static String[] ARGS;
	public static File[] argumentsFiles;
	
	public static void main(String[] args) {
		try {
			
			now = new Date();

			// Initialize logger properties
			PropertyConfigurator.configure(GeneralConfig.LOG_PROPERTIES_PATH);
			
			logger.info("\n\n##################################################\n\n"+now.toString()+"\n\n"
					+ mainGUI.APPLICATION_NAME+" Started now!\n\n"
					+ "##################################################\n\n");

			argumentsFiles = FileStuff.getFileArrayFromStrings(args);
			
			{
				mainGUI.mainApplicationGeneral();
			}
			
			// TODO test life cycle
			if(TEST_ERROR_FLAG) {
				Thread.sleep(2000);
				throw new Error();
			}
		
		} catch (Throwable e) {
			logger.error(CRITICAL_ERROR_MSG, e);
			JOptionPane.showMessageDialog(null, CRITICAL_ERROR_MSG+e.getClass().getName(), "Critical Error",
					JOptionPane.ERROR_MESSAGE, new ImageIcon(IconsPathConfigurator.ICON_BIG_GEN_ERROR));
			doStuffBeforeClose();
		}
	}
	
	private static void doStuffBeforeClose() {
		now = new Date();
		
		logger.info("\n\n##################################################\n\n"+now.toString()+"\n\n"
				+ mainGUI.APPLICATION_NAME+" HAD AN UNEXPECTED ERROR - FORCING CLOSE\n\n"
				+ "##################################################\n\n");
		
		System.exit(0);
	}
}

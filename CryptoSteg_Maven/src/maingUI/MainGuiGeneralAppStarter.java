package maingUI;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class MainGuiGeneralAppStarter {

	// use this to test throwable launched
	static boolean test = false;
	
	// mainGUI logger
	static Logger logger = Logger.getLogger(MainGuiGeneralAppStarter.class);
	// log_trace location
	private static final String LOG_PROPERTIES_PATH = mainGUI.RESOURCES_DIR + "log4j.properties";
		
	public static final String CRITICAL_ERROR_MSG = "An unexpected problem caused a critical error: ";
	private static Date now;
	
	public static void main(String[] args) {
		try {
			now = new Date();
			
			// Initialize logger properties
			PropertyConfigurator.configure(LOG_PROPERTIES_PATH);
			
			logger.info("\n\n##################################################\n\n"+now.toString()+"\n\n"
					+ mainGUI.APPLICATION_NAME+" Started now!\n\n"
					+ "##################################################\n\n");
			
			if(test) {
				throw new Error();
			}else {
				mainGUI.mainApplicationGeneral();
			}
			
		
		} catch (Throwable e) {
			logger.error(CRITICAL_ERROR_MSG, e);
			JOptionPane.showMessageDialog(null, CRITICAL_ERROR_MSG+e.getClass().getName(), "Critical Error", JOptionPane.ERROR_MESSAGE);
			doBeforeClose();
		}
	}
	
	private static void doBeforeClose() {
		now = new Date();
		
		logger.info("\n\n##################################################\n\n"+now.toString()+"\n\n"
				+ mainGUI.APPLICATION_NAME+" HAD AN UNEXPECTED ERROR - CLOSING\n\n"
				+ "##################################################\n\n");
		
		System.exit(0);
	}
}

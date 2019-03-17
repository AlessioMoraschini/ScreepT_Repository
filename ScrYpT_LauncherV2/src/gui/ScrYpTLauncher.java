package gui;

import java.awt.EventQueue;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFrame;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import gui.panels.StartPanel;
import launcherutils.FileUtils;
import launcherutils.SysUtils;
import launcherutils.om.OSenum;
import launcherutils.om.OSinfo;

/**
 * 
 * @author Alessio Moraschini
 * 
 * This class represents a custom installer/launcher written in java, that runs specific 
 * OS commands to set dependencies, JVM version, path, and other options before to start a jar containing main APP.
 * 
 * To use this place the exported jar in same folder as main-jar application to run, including the given "LAUNCHER/"
 * folder with sub-folders and files keeping names and structure.
 * 
 * => to run the application just customize MAIN_JAR_FILE with the name of main application jar.
 * 
 * => to customize launcher graphics just change the class of launcherMainFrame extending JFrame class with your own.
 * 
 * ENJOY :)
 *
 */
public class ScrYpTLauncher {

	public static final String LAUNCHER_CONTAINER_PATH = "LAUNCHER/";
	public static final String APPLICATION_DIR = "Application/";
	
	private static final String LOG_PROPERTIES_PATH = LAUNCHER_CONTAINER_PATH+"Config/log4j.properties";

	// KeyGenPanel logger
	static Logger logger = Logger.getLogger(ScrYpTLauncher.class); 
		
	// FIELDS
	
	public static final String MAIN_JAR_FILE = "SCreepT_V0.3.1_X.jar";
	public static final String JVM_ARGS = "-Dfile.encoding=UTF8 -Djava.library.path=\"./Resources_Scrypt/lib\"";
	
	// jvm search fields
	public static final String MIN_JRE_VERSION = "1.6"; // included MIN
	public static final String MAX_JRE_VERSION = "9.0"; // excluded MAX
	public static final String NOT_FOUND_JVM = "";
	public static OSinfo currentOS;
	
	public static LinkedList<String> execCommand;
	public static StartPanel launcherMainFrame;
	public static String javaPath="";
	public static ScrYpTLauncher LAUNCHER;
	
	// MAIN START POINT
	public static void main(String[] args) {
		
		// Initialize logger properties
		PropertyConfigurator.configure(LOG_PROPERTIES_PATH);
		appLogStart();
		
		start();

	}// end main
	
	public static void start() {
		try {
			EventQueue.invokeLater(new Runnable() {

				@Override
				public void run() {

					// init launcher and GUI
					LAUNCHER = new ScrYpTLauncher();

				} // end run method
			}); // end invokeLater
		} catch (RuntimeException e) {
			logger.error("   =>  Exception catched during launch!\n\n", e);
		}
	}
	
	// CONSTRUCTOR
	public ScrYpTLauncher() {
		
		execCommand = new LinkedList<String>();	
		analyzeBeforeLauncher();
		
		// GUI launcher initialization
		launcherMainFrame = new StartPanel("ScrYpt Launcher", this);
		launcherMainFrame.setVisible(true);
		launcherMainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		launcherMainFrame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				appLogEnd();
				System.exit(0);
			}
		});
	}
	
	public void analyzeBeforeLauncher() {
		currentOS = SysUtils.retrieveOS();
		
		javaPath = retrieveCorrectJVMS();
		// static for test => comment to use found one
		//javaPath="C:/Program Files/Java/jdk1.8.0_162/bin;";
				
		addPathCommand(javaPath);
		
		addInstallDependenciesCommand();
		
		addLaunchScrYpTCommand();
	}
	
	/**
	 * retrieve list of commands, write to file and run it in current OS
	 */
	public void launchCommands() {
		String combinedCommands = getCombinedCommandList();
		// TODO use initpath to call exec
		
		try {
			String initPath = writeInitBatShAndGetPath(combinedCommands);
			Runtime.getRuntime().exec(initPath);
		} catch (IOException e) {
			logger.error("Error launching application!\n\n", e);
		}
	}
	
	/**
	 * Writes the commands added to commandList in a batch/bash file, according to current OS.
	 * 
	 * @param commandList
	 * @return the path of written file
	 * @throws IOException
	 */
	private static String writeInitBatShAndGetPath(String commandList) throws IOException {

		String initFilePath = (!currentOS.isWindows())? "LauncherBash.sh" : "LauncherBatch.bat";
		
		// write commands to build bash/batch starter file
		FileUtils.writeStringToFile(commandList, new File(initFilePath), true);
		
		return initFilePath;
	}	
	
	// SOME USEFUL LAUNCH-RELATED METHOD
	
	/**
	 * Find all versions of JVM installed on this machine (According to OS), and find if there is one in the 
	 * admitted range, and set its path to javaPath field in this class.
	 * 
	 * @return If found return true, else return false
	 */
	private String retrieveCorrectJVMS() {

		ArrayList<String> foundJvmHomeList = SysUtils.findJvmVersionsInstalled(currentOS);
		String foundCorrect = compareWithAdmittedJVMS(foundJvmHomeList);
		foundCorrect = (!foundCorrect.equals(""))? foundCorrect+"/bin": "";
		return foundCorrect;
	}
	
	/**
	 * Add command to read path (according to currentOS) to the command list
	 * @param pathEntry
	 */
	private void addPathCommand(String pathEntry) {

		if(currentOS.isWindows()) {
			execCommand.add("set JDK_PATH=" + pathEntry + ";");
			execCommand.add("set PATH=%JDK_PATH%%PATH%");
			execCommand.add("echo %PATH%");
		
		}else if(currentOS.isLinux()) {
		
			execCommand.add("PATH=" + pathEntry + ":$PATH");
		
		}else if(currentOS.isMac()) {
			
			execCommand.add("PATH=" + pathEntry + ":$PATH");

		}		
	}
	
	/**
	 * run commands to check  if dependencies software is installed, and to eventually install it.
	 */
	private void addInstallDependenciesCommand() {
		// TODO
	}
	
	/**
	 * add command to start main application jar with given JVM args
	 */
	private void addLaunchScrYpTCommand() {
		
		execCommand.add(SysUtils.startJarCommand(JVM_ARGS, MAIN_JAR_FILE)+" -Xmx2048m -Xms512");
	}
	
	/**
	 * extract a String containing all added commands separed by 2 newlines (to write after on a bash/batch file).
	 * @return
	 */
	private static String getCombinedCommandList() {
		StringBuilder builder = new StringBuilder();
		builder.ensureCapacity(execCommand.size());
		
		int i = 1;
		for(String currentCommand : execCommand) {
			builder.append(currentCommand);
			if(i < execCommand.size()) {
				builder.append(" \n\n");
			}
			i++;
		}
		return builder.toString();
	}
	
	/**
	 * Compare a list of JVM versions with the admitted one
	 * @param foundJVMS
	 * @return the path of the current JVM
	 */
	private String compareWithAdmittedJVMS(List<String> foundJVMS) {
		
		for(String currentPath : foundJVMS) {
			
			// find name from path
			String versionDirName = new File(currentPath).getName();
			// change  underscores with dots
			String dotsOnly = versionDirName.replaceAll("_",".");
			dotsOnly = dotsOnly.replaceAll("-","");
			// remove non dots chars
			String noLettersDotsOnly = dotsOnly.replaceAll("[a-zA-Z!#$%&'*+=?^`{|}+]|[_]\\-","");
			
			try {
				boolean lessThanMax = SysUtils.compareVersions(noLettersDotsOnly, this.MAX_JRE_VERSION) < 0; // true if digits < max
				boolean moreThanMin = SysUtils.compareVersions(noLettersDotsOnly,this.MIN_JRE_VERSION) >= 0; // true if digits >= min
				
				if(lessThanMax && moreThanMin) {
					return currentPath;
				}
			} catch (IllegalArgumentException e) {
				logger.error("Error comparing java version for " + currentPath, e);
			}
		}
		
		// if not found rech this point and return 
		return NOT_FOUND_JVM;
	}
	
	private static void appLogStart() {
		Date now = new Date();
		logger.debug("\n\n##################################################\n\n"+now.toString()+"\n\n"
				+ "Launcher Started now!\n\n"
				+ "##################################################\n\n");
	}
	
	private static void appLogEnd() {
		Date now = new Date();
		logger.debug("\n\n##################################################\n\n"+now.toString()+"\n\n"
				+ "Launcher Closed now!\n\n"
				+ "##################################################\n\n");
	}
}

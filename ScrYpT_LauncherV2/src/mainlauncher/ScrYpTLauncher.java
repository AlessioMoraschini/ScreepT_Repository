package mainlauncher;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFrame;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.ini4j.InvalidFileFormatException;
import org.ini4j.Wini;

import gui.panels.MissingDependenciesPanel;
import gui.panels.StartPanel;
import gui.utils.LauncherLoaderFrame;
import launcherutils.FileUtils;
import launcherutils.om.DependencyLocalPath;
import launcherutils.om.DependencyModel;
import om.OSinfo;
import utils.SysUtils;

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

	// KeyGenPanel logger
	static Logger logger = Logger.getLogger(ScrYpTLauncher.class); 
		
	public static final String LAUNCHER_BACK_IMAGE_PATH = "LAUNCHER/launcher_icon.jpg";
	public static final String LAUNCHER_CONTAINER_PATH = "LAUNCHER/";
	public static final String APPLICATION_DIR = "Application/";
	
	private static final String LOG_PROPERTIES_PATH = LAUNCHER_CONTAINER_PATH+"Config/log4j.properties";
	
	// main application path default
	public static String MAIN_JAR_FILE = "SCreepT_V0.3.1_X.jar";
	
	// jvm search fields default
	public static String MIN_JRE_VERSION = "1.6"; // included MIN
	public static String MAX_JRE_VERSION = "9.0"; // excluded MAX
	public static String JVM_MEMORY_OPTIONS = "-Xmx2048m -Xms512m"; // default memory options
	public static String JVM_STACK_SIZE = "-Xss128m"; // default stack size
	public static final String JVM_URL_INSTALL = "https://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html";
	public static final String NOT_FOUND_JVM = "";
	public static final String JVM_OK = "JVMOK";
	public static boolean ignoreWarnings = false;
	public static final String JVM_ARGS = "-Dsun.java2d.uiScale=1 -Dfile.encoding=UTF8 -Djava.library.path=\"./Resources_Scrypt/lib\"";
	
	// DEFAULT INI
	public static final String DEFAULT_INI_PATH = LAUNCHER_CONTAINER_PATH + "Launcher_Configuration.ini"; 
	public final static Level DEFAULT_LOG_LEVEL = Level.ERROR;
	public static final String INI_JVM_OPT = "JVM_OPTIONS";
	public static final String INI_APP_OPT = "APPLICATION_OPTIONS";
	public static boolean viewShellAfterStart = false;
	
	// use this as flag to signal that something is not ok with dependencies
	public static  boolean criticalDependenciesOK = false;
	public static ArrayList<DependencyModel> dependeciesMissing;
	public static Wini iniConfigFile;
	public static boolean showInstallOptions = true;
	public static boolean hideNStartDirect = false;
	
	// USER MANUALLY LOCATED DEPENDENCY PATHS
	public String manualPath_Jvm = NOT_FOUND_JVM;
	public static String javaPath=NOT_FOUND_JVM;
	private final static String arch32OR64 = String.valueOf(SysUtils.findIf32or64());
	
	// ACTION LISTENERS - JVM
	public ActionListener actionLocManually_Jvm;
	public ActionListener actionDownNInstall_Jvm;
	
////////////////////////////////////// FIELDS //////////////////////////////////////

	public static OSinfo currentOS;
	
	public static LinkedList<String> execCommand;
	public static StartPanel launcherMainFrame;
	public static ScrYpTLauncher LAUNCHER;
	public static LauncherLoaderFrame loaderGraphics;
	
	// MAIN START POINT
	public static void main(String[] args) {
		
		loaderGraphics = new LauncherLoaderFrame("Loading Launcher...", "LAUNCHER/launcher_icon.jpg", 250);
		for (int i = 0; i < 25; i++) {
			String dots = new String(new char[i+1]).replace("\0", ".");
			String msg = "Initializing launcher" + dots;
			loaderGraphics.update(msg, i/2);
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				logger.error("Error waiting before starting launcher", e);
			} 
		}
		// Initialize logger properties
		PropertyConfigurator.configure(LOG_PROPERTIES_PATH);
		appLogStart();
		
		loaderGraphics.update("Reading Configuration File "+DEFAULT_INI_PATH, 15);
		initFromINIFile();
		
		loaderGraphics.update("Starting Launcher...", 25);
		start();
		
		loaderGraphics.update("Launcher Ready!", 100, 200);

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
		
		currentOS = SysUtils.retrieveOS();
		
		defineActionListeners();
		
		analyzeBeforeLauncher();
		
		// if direct launch configured launch directly commands
		if(hideNStartDirect) {
			appLogEnd();
			loaderGraphics.update("Ready", 100);
			launchCommands();
			System.exit(0);
		}
		
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
		//  IF PROBLEMS WITH DEPENDENCIES LIST CONTAINS SOME ELEMENTS
		if(dependeciesMissing.size() != 0 && (showInstallOptions || !criticalDependenciesOK)) {
			launcherMainFrame.missingDependenciesPanel = new MissingDependenciesPanel("There are dependencies not found!", launcherMainFrame);
			launcherMainFrame.missingDependenciesPanel.setVisible(true);
			launcherMainFrame.missingDependenciesPanel.requestFocus();
			logger.info("Missing dependencies detected, opening installer panel");
		}
		logger.info(this.getClass().getName() + " Loaded");
	}
	
	public void analyzeBeforeLauncher() {
		
		logger.info("Started to analyze before launch");
		
		execCommand = new LinkedList<String>();	
		dependeciesMissing = new ArrayList<DependencyModel>();
		
		javaPath = retrieveCorrectJVMS();
		
		if(javaPath.equals(NOT_FOUND_JVM)) {
			addToMissingDependenciesJVM();
		}
		// static for test => comment to use found one
		//javaPath="C:/Program Files/Java/jdk1.8.0_162/bin;";
				
		addPathCommand(javaPath);
		
		addInstallDependenciesCommand();
		
		addLaunchScrYpTCommand();
		
		logger.info("Launch command generated:\n\n" + getCombinedCommandList() +"\n\n");
	}
	
	public void addToMissingDependenciesJVM() {
		String localPathWinInstaller = DependencyModel.INSTALLERS_PATH + arch32OR64 + "/JRE-1.8-installer/jre-1.8u201.exe";
		String localPathLinInstaller = "";
		String localPathMacInstaller = "";
		DependencyLocalPath pathsLocalInstaller = new DependencyLocalPath(localPathWinInstaller, localPathLinInstaller, localPathMacInstaller);
		
		DependencyModel javaDependency = new DependencyModel("Compatible JAVA JVM not found", "Download/Install/Locate", false, JVM_URL_INSTALL, pathsLocalInstaller, actionDownNInstall_Jvm, actionLocManually_Jvm);
		dependeciesMissing.add(javaDependency);
	}
	
	/**
	 * retrieve list of commands, write to file and run it in current OS
	 */
	public void launchCommands() {
		
		logger.info("Launching commands on currentOS: " + currentOS.getOsName() + currentOS.getOsArch());
		
		String combinedCommands = getCombinedCommandList();
		
		Runtime runtime = Runtime.getRuntime();
		
		try {
			String initPath = writeInitBatShAndGetPath(combinedCommands);
			if (currentOS.isWindows()) {
				if (viewShellAfterStart) {
					runtime.exec("cmd /c start \"\" " + initPath);
				}else {
					runtime.exec(initPath);
				}
			}else {
				runtime.exec(initPath);
			}
			logger.info("Commands launched without exceptions :)"); 
		} catch (IOException e) {
			logger.error(" !!! Error launching generated commands file !!!\n\n", e);
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
		logger.info("Creating launcher file : " + initFilePath);
		
		// write commands to build bash/batch starter file
		boolean written = FileUtils.writeStringToFile(commandList, new File(initFilePath), true);
		
		String msg = (written)? "Write completed correctly :)" : "Write not succeded for some error :(";
		logger.info(msg);
		
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
		
		logger.info("Started java installed versions check!");
		String currJvmVersion = SysUtils.getCurrentJVMversion();
		
		// if current is correct return it and exit method
		if(checkSingleJavaVersionOK(currJvmVersion)) {
			logger.info("Current JVM("+currJvmVersion+") is OK -> selected and skipping search");
			return JVM_OK;
			// adding ; to path does not modify current retrieved and escapes NOT_FOUND_JVM controls as path won't be ""
		}
		
		if(ignoreWarnings) {
			// if user selected to ignore given warnings
			logger.info("User have chosen to ignore warnings: Current JVM("+currJvmVersion+") will be used.");
			return JVM_OK;
		}
		
		ArrayList<String> foundJvmHomeList = SysUtils.findJvmVersionsInstalled(currentOS);
		String foundCorrect = compareWithAdmittedJVMS(foundJvmHomeList);
		foundCorrect = (!foundCorrect.equals(NOT_FOUND_JVM))? foundCorrect+"/bin": NOT_FOUND_JVM;
		logger.info("Versions found: " + foundJvmHomeList);
		logger.info("Found correct one: " + foundCorrect);
		
		return foundCorrect;
	}
	
	/**
	 * Add command to read path (according to currentOS) to the command list
	 * @param pathEntry
	 */
	private void addPathCommand(String pathEntry) {

		if (!javaPath.equals(JVM_OK)) {
			if (currentOS.isWindows()) {
				execCommand.add("set JDK_PATH=" + pathEntry + ";");
				execCommand.add("set PATH=%JDK_PATH%%PATH%");
				execCommand.add("echo %PATH%");

			} else if (currentOS.isLinux()) {

				execCommand.add("PATH=" + pathEntry + ":$PATH");

			} else if (currentOS.isMac()) {

				execCommand.add("PATH=" + pathEntry + ":$PATH");

			} 
		}		
	}
	
	/**
	 * run commands to check  if dependencies software is installed, and to eventually install it.
	 */
	private void addInstallDependenciesCommand() {
		// TODO
		criticalDependenciesOK = true;
	}
	
	/**
	 * add command to start main application jar with given JVM args
	 */
	private void addLaunchScrYpTCommand() {
		
		String complete_ARGS = JVM_MEMORY_OPTIONS + JVM_STACK_SIZE + " -XshowSettings:vm " + JVM_ARGS;
		execCommand.add(SysUtils.startJarCommand(complete_ARGS, MAIN_JAR_FILE));
	}
	
	/**
	 * extract a String containing all added commands separed by 2 newlines (to write after on a bash/batch file).
	 * @return
	 */
	private static String getCombinedCommandList() {
		StringBuilder builder = new StringBuilder();
		builder.ensureCapacity(execCommand.size());
		
		for(String currentCommand : execCommand) {
			builder.append(currentCommand);
			builder.append(" \n\n");
		}
		// add exit command to end of batch/bash
		builder.append("exit 0");
		
		return builder.toString();
	}
	
	/**
	 * Compare a list of JVM versions with the admitted one
	 * @param foundJVMS
	 * @return the path of the current JVM
	 */
	private String compareWithAdmittedJVMS(List<String> foundJVMS) {
		
		// first check used located JVM if it exist
		if(!manualPath_Jvm.equals(NOT_FOUND_JVM) && isOKManualJVM()) {
			File binPath = new File(manualPath_Jvm);
			return binPath.getParentFile().getAbsolutePath();
		}
		
		// if unspecified manual search automatically
		for(String currentPath : foundJVMS) {
			
			// find name from path
			String versionDirName = new File(currentPath).getName();
			
			try {
					
				if(checkSingleJavaVersionOK(versionDirName)) {
					return currentPath;
				}
			} catch (IllegalArgumentException e) {
				logger.error("Error comparing java version for " + currentPath, e);
			}
			
		}
		
		// if not found rech this point and return 
		return NOT_FOUND_JVM;
	}

	/**
	 * check if a given java version (which can contain letters or underscores) respect
	 * min and max values : if less than max and more(or equal) to min then is OK.
	 * @param version
	 * @return
	 */
	private static boolean checkSingleJavaVersionOK(String version) {
		// remove non dots chars and transform undescores to dots
		String noLettersDotsOnly = SysUtils.keepOnlyDotsAndDigits(version);
		
		boolean lessThanMax;
		boolean moreThanMin;
		try {
			lessThanMax = SysUtils.compareVersions(noLettersDotsOnly, MAX_JRE_VERSION) < 0;
			moreThanMin = SysUtils.compareVersions(noLettersDotsOnly, MIN_JRE_VERSION) >= 0;
		} catch (IllegalArgumentException e) {
			logger.error("Error comparing version: illegal format of ["+version+"]", e);
			return false;
		}
	
		return (lessThanMax && moreThanMin);
	}
	
	private boolean isOKManualJVM() { // TODO improve and make multiOS
		File manualJvmBin = new File(manualPath_Jvm);
		boolean errorCondition1 = !manualJvmBin.exists();
		String version ="";
		try {
			version = SysUtils.getJavaVersionFromBin(manualPath_Jvm);
		} catch (IOException e) {
			logger.error("Error checkin java version from release file in /bin directory", e);
			return false;
		}
		boolean errorCondition2 = !checkSingleJavaVersionOK(version) ;
		if(errorCondition1 || errorCondition2) {
			return false;
		}else {
			return true;
		}
	}
	
	public void defineActionListeners() {
		actionLocManually_Jvm = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String msg = "Specify /bin folder of a supported java installation ("+MIN_JRE_VERSION+" <= VERSION < "+MAX_JRE_VERSION+")";
				manualPath_Jvm = FileUtils.directoRead(launcherMainFrame, msg).getAbsolutePath();
			}
		};
	}
	
	// read configuration from ini file with null-value check
	public static void initFromINIFile(){
		try {
			logger.info("Loading INI launcher_configuration file...");
			
			iniConfigFile = new Wini(new File(DEFAULT_INI_PATH));
			
			// JVM OPTIONS WITH NULL CHECK
			String MAX_JVERSION_INI = iniConfigFile.get(INI_JVM_OPT, "maxJvmVersion");
			MAX_JRE_VERSION = (MAX_JVERSION_INI != null)? MAX_JVERSION_INI : MAX_JRE_VERSION;
			String MIN_JVERSION_INI = iniConfigFile.get(INI_JVM_OPT, "minJvmVersion");
			MIN_JRE_VERSION = (MIN_JVERSION_INI != null)? MIN_JVERSION_INI : MIN_JRE_VERSION;
			String MEM_JOPTIONS_INI = iniConfigFile.get(INI_JVM_OPT, "memoryArgs");
			JVM_MEMORY_OPTIONS = (MEM_JOPTIONS_INI != null)? MEM_JOPTIONS_INI : JVM_MEMORY_OPTIONS;
			String MEM_STACK_INI = " -Xss" + iniConfigFile.get(INI_JVM_OPT, "stackSizeArgs")+"m";
			JVM_STACK_SIZE = (MEM_STACK_INI != null)? MEM_STACK_INI : JVM_STACK_SIZE;
			
			// APPLICATION OPTIONS WITH NULL CHECK
			String viewShell = iniConfigFile.get(INI_APP_OPT, "viewCmdConsole");
			viewShellAfterStart = (!"".equals(viewShell) && Boolean.parseBoolean(viewShell))? true : false;
			String MAIN_JAR_INI = iniConfigFile.get(INI_APP_OPT, "jarToLaunch");
			MAIN_JAR_FILE = (MAIN_JAR_INI != null)? MAIN_JAR_INI : MAIN_JAR_FILE;
			String autoInstallerOpt = iniConfigFile.get(INI_APP_OPT, "autoShowInstallOpt");
			showInstallOptions = (autoInstallerOpt!=null && autoInstallerOpt.equalsIgnoreCase("false"))? false : true;
			String hideNStart = iniConfigFile.get(INI_APP_OPT, "flagHideNStartDirect");
			hideNStartDirect = (hideNStart!=null && hideNStart.equalsIgnoreCase("true"))? true : false;
			
			logger.info("Launcher_configuration file loaded!");
		} catch (InvalidFileFormatException e) {
			logger.error("Invalid format detected while trying to read " + DEFAULT_INI_PATH, e);
		} catch (IOException e) {
			logger.error("IO error while trying to read " + DEFAULT_INI_PATH, e);
		}
	}
	
	/**
	 * use this function to write a single value to launcher configuration file
	 * @param OPT_SECTION
	 * @param OPT_VAR
	 * @param OPT_VALUE
	 * @return
	 */
	public synchronized static boolean writeValueToIniConfiguration(String OPT_SECTION, String OPT_VAR, String OPT_VALUE ) {
		boolean OK = false;
		
		if (iniConfigFile != null && iniConfigFile.getFile().exists()) {
			try {
				iniConfigFile.put(OPT_SECTION, OPT_VAR, OPT_VALUE);
				iniConfigFile.store();
				OK = true;
			} catch (IOException e) {
				OK = false;
				logger.error("Exception happened writing value to LAUNCHER ini configuration file!\n\n", e);
			} 
		}
		return OK;
	}
	
	public static Level getLogLevelFromString(String stringLevel) {
		if(stringLevel == null) {
			return DEFAULT_LOG_LEVEL;
		}else if(stringLevel.equals("OFF")) {
			return Level.OFF;
		}else if(stringLevel.equals("FATAL")) {
			return Level.FATAL;
		}else if(stringLevel.equals("ERROR")) {
			return Level.ERROR;
		}else if(stringLevel.equals("WARN")) {
			return Level.WARN;
		}else if(stringLevel.equals("DEBUG")) {
			return Level.DEBUG;
		}else if(stringLevel.equals("INFO")) {
			return Level.INFO;
		}else if(stringLevel.equals("ALL")) {
			return Level.ALL;
		}else {
			return DEFAULT_LOG_LEVEL;
		}
	}
	
	public static String[] getLogLevelsList() {
		String[] levelArray = {"ALL","INFO","DEBUG","WARN","ERROR","FATAL","OFF"};
		return levelArray;
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

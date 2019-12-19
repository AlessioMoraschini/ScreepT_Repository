package mainlauncher;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import gui.panels.CustomizeParametersPanel;
import gui.panels.MissingDependenciesPanel;
import gui.panels.StartPanel;
import gui.utils.GuiUtils;
import gui.utils.LauncherLoaderFrame;
import gui.utils.LoggerUtils;
import launcherutils.CommandsAssembler;
import launcherutils.FileUtils;
import launcherutils.IniFileManager;
import launcherutils.JvmHelper;
import launcherutils.om.DependencyLocalPath;
import launcherutils.om.DependencyModel;
import launcherutils.om.JvmArgumentsDTO;
import om.OSinfo;
import resources.GeneralConfig;
import resources.ImagesPathConfigurator;
import resources.PropertiesManager;
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
	
	// ScrYpTLauncher logger
	static Logger logger = Logger.getLogger(ScrYpTLauncher.class); 
	
	public static final String BATCH_LAUNCHER_NAME = "LauncherBatch.bat";
	public static final String BASH_LAUNCHER_NAME = "LauncherBash.sh";
	
	public static String DEFAULT_INI_PATH; 
	public static String APPLICATION_DIR;
	
	public static final String installedPluginPropsFileName = "Installed_plugins.properties";
	public static final String installedPluginPendingDeleteListKey = "PLUGINS_PENDING_DELETE_AT_START";
	public static PropertiesManager installedPluginProperties = new PropertiesManager(installedPluginPropsFileName);
	
	
	// jvm search fields default
	public static String JVM_ARGS;
	
	public static boolean ignoreWarnings = false;
	
	public static final String END_ARGS_SECTION = "###_END_SECTION";
	private final static String arch32OR64 = String.valueOf(SysUtils.findIf32or64());
	public static final Dimension screenSize = GuiUtils.getScreenSize();
	
////////////////////////////////////// FIELDS //////////////////////////////////////

	public static OSinfo currentOS;
	public static CommandsAssembler commandsAssembler;
	public static JvmHelper jvmHelper;
	
	public static StartPanel launcherMainFrame;
	public static ScrYpTLauncher LAUNCHER;
	public static LauncherLoaderFrame loaderGraphics;
	
	public static String[] ARGS;
	public static File[] argumentsFiles;
	
	public static IniFileManager iniFileManager;

	// ACTION LISTENERS - JVM
	public ActionListener actionLocManually_Jvm;
	public ActionListener actionDownNInstall_Jvm;
	
	// use this as flag to signal that something is not ok with dependencies
	public static  boolean criticalDependenciesOK = false;
	public static ArrayList<DependencyModel> dependeciesMissing;
	
	/** MAIN START POINT **/
	public static void main(String[] args) {
		
		// TEST error launch if first argument is err_test
		if(args.length > 0 && args[0].equals("err_test")) {
			System.exit(1);
		}
		
		ARGS = args;
		argumentsFiles = getFileArrayFromStrings(args);

		checkAlreadyRunning();
		
		loaderGraphics = new LauncherLoaderFrame("Loading Launcher...", ImagesPathConfigurator.LAUNCHER_BACK_IMAGE_PATH, null);

		GeneralConfig.initLazyGeneralConfig();
		
		startPendingDeletesCleanerThread();
		
		JVM_ARGS = " -Djava.library.path=\"./" + GeneralConfig.RESOURCES_DIR  + "lib\"";
		DEFAULT_INI_PATH = GeneralConfig.DEFAULT_LAUNCHER_INI_PATH; 
		APPLICATION_DIR = GeneralConfig.MAIN_APP_ROOT_DIR;  
		
		// Initialize logger properties -> FIRST THING TO DO
		PropertyConfigurator.configure(GeneralConfig.LAUNCHER_LOG_PROPS_PATH);
		appLogStart();

		iniFileManager = new IniFileManager(DEFAULT_INI_PATH);
		
		for (int i = 0; i < 20; i++) {
			String dots = new String(new char[i+1]).replace("\0", ".");
			String msg = "Initializing launcher" + dots;
			loaderGraphics.update(msg, i/2);
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				logger.error("Error waiting before starting launcher", e);
			} 
		}
		
		loaderGraphics.update("Reading Configuration File "+DEFAULT_INI_PATH, 15);
		iniFileManager.initFromINIFile();
		
		loaderGraphics.update("Starting Launcher...", 25);
		start();
		
		loaderGraphics.update("Launcher Ready!", 100, 500);

	}// end main
	
	public static File[] getFileArrayFromStrings(String[] filePaths){
		logger.debug("ARGUMENT FILES:" + Arrays.toString(filePaths));
		
		File[] argumentsFiles = new File[filePaths.length];
		int j = 0;
		for(String s : filePaths) {
			if (!END_ARGS_SECTION.equals(s)) {
				argumentsFiles[j] = new File(s);
			}else {
				break;
			}
			j++;
		}
		return argumentsFiles;
	}
	
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
		
		jvmHelper = new JvmHelper(iniFileManager.MAX_JRE_VERSION, iniFileManager.MIN_JRE_VERSION);
		commandsAssembler = new CommandsAssembler(jvmHelper);
		
		currentOS = SysUtils.getOsInfoLazy();
		
	loaderGraphics.update("Defining listeners...", 35);
	
		defineActionListeners();
	
	loaderGraphics.update("Analyzing System...", 45);
		analyzeBeforeLauncher();
		
		// if there aren't blocking problems and direct launch configured -> launch directly
		// NB incompatible jvm is potential blocking cause for medium user, so by default when jvm is "warning" launcher panel will be prompted
		if(!JvmHelper.warningJVM && criticalDependenciesOK && (iniFileManager.hideNStartDirect || ARGS.length != 0)) {
			loaderGraphics.update("Ready", 600);
			appLogEnd();
			launchCommands();
			try {
				Thread.sleep(400);
			} catch (InterruptedException e1) {
			}
			System.exit(0);
		}
		
		// GUI launcher initialization
		launcherMainFrame = new StartPanel("ScrYpt Launcher", this);

		if(JvmHelper.warningJVM) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					launcherMainFrame.customizerPanel = new CustomizeParametersPanel("Customize start options", launcherMainFrame.LAUNCHER_GUI);
					launcherMainFrame.customizerPanel.setVisible(true);
					launcherMainFrame.customizerPanel.setAlwaysOnTop(true);
					launcherMainFrame.customizerPanel.requestFocus();
					launcherMainFrame.customizerPanel.toFront();
				}
			});
		}
		
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
		if(dependeciesMissing.size() != 0 && (iniFileManager.showInstallOptions || !criticalDependenciesOK)) {
			launcherMainFrame.missingDependenciesPanel = new MissingDependenciesPanel("There are dependencies not found!", launcherMainFrame);
			launcherMainFrame.missingDependenciesPanel.setVisible(true);
			launcherMainFrame.missingDependenciesPanel.requestFocus();
			logger.info("Missing dependencies detected, opening installer panel");
		}
		logger.info(this.getClass().getName() + " Loaded");
		
	loaderGraphics.update("Preparing launch...", 85);
	}
	
	/**
	 * Analyze JVM and dependencies, preparing arguments and options to create the executable file (bat/sh)
	 * to launch the main java application.
	 */
	public void analyzeBeforeLauncher() {
		logger.info("Started to analyze before launch");
		
		// ENSURE OLD COMMANDS ARE DELETED
		commandsAssembler.emptyCommandsList();
		dependeciesMissing = new ArrayList<DependencyModel>();
		
		// RETRIEVE BEST MACTHING JVM ON CURRENT OS
		String javaPath = jvmHelper.retrieveCorrectJVMS(ignoreWarnings);
		if(javaPath.equals(JvmHelper.NOT_FOUND_JVM)) {
			addToMissingDependenciesJVM();
		}
				
		// ADD PATH UPDATE COMMAND FOR RIGHT TIPE SH/BAT
		commandsAssembler.addPathCommand(javaPath);
		
		// ADD COMMANDS TO AUTO INSTALL BUNDLED DEPENDENCIES
		addInstallDependenciesCommand();
		
		JvmArgumentsDTO dto = new JvmArgumentsDTO(
				iniFileManager.JVM_MEMORY_OPTIONS,
				iniFileManager.JVM_STACK_SIZE,
				iniFileManager.MAIN_JAR_FILE,
				iniFileManager.GUI_SCALE_ARG_VALUE,
				JVM_ARGS,
				ARGS);
		
		// ADD LAUNCH COMMAND WITH GIVEN ARGUMENTS FROM CONFIGURATION (parsed .ini)
		commandsAssembler.addLaunchCommand(dto, argumentsFiles);
		
		logger.info("Launch command generated:\n\n" + commandsAssembler.getCombinedCommandList() +"\n\n");
	}
	
	/**
	 * This method adds to missing dependencies JVM with references to download/install it
	 */
	public void addToMissingDependenciesJVM() {
		String localPathWinInstaller = DependencyModel.INSTALLERS_PATH + arch32OR64 + "/JRE-1.8-installer/jre-1.8u201.exe";
		String localPathLinInstaller = "";
		String localPathMacInstaller = "";
		DependencyLocalPath pathsLocalInstaller = new DependencyLocalPath(localPathWinInstaller, localPathLinInstaller, localPathMacInstaller);
		
		DependencyModel javaDependency = new DependencyModel("Compatible JAVA JVM not found", "Download/Install/Locate", false, JvmHelper.JVM_URL_INSTALL, pathsLocalInstaller, actionDownNInstall_Jvm, actionLocManually_Jvm);
		dependeciesMissing.add(javaDependency);
	}
	
	/**
	 * retrieve list of commands, write to file and run it in current OS
	 */
	public void launchCommands() {
		
		logger.info("Launching commands on currentOS: " + currentOS.getOsName() + currentOS.getOsArch());
		
		String combinedCommands = commandsAssembler.getCombinedCommandList();
		
		Runtime runtime = Runtime.getRuntime();
		
		try {
			String initPath = writeInitBatShAndGetPath(combinedCommands);
			
			// CASE WINDOWS
			if (currentOS.isWindows()) {
				if (iniFileManager.viewShellAfterStart) {
					runtime.exec("cmd /c start \"\" " + initPath);
				}else {
					runtime.exec(initPath);
				}
			
			}else {
				// TODO LINUX/MAC SHELL show on/off
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

		String initFilePath = (!currentOS.isWindows())? BASH_LAUNCHER_NAME : BATCH_LAUNCHER_NAME;
		logger.info("Creating launcher file : " + initFilePath);
		
		// write commands to build bash/batch starter file
		boolean written = FileUtils.writeStringToFile(commandList, new File(initFilePath), true);
		
		String msg = (written)? "Write completed correctly :)" : "Write not succeded for some error :(";
		logger.info(msg);
		
		return initFilePath;
	}	
	
	/**
	 * run commands to check  if dependencies software is installed, and to eventually install it.
	 */
	private void addInstallDependenciesCommand() {
		// TODO
		criticalDependenciesOK = true;
	}
	
	public void defineActionListeners() {
		actionLocManually_Jvm = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String msg = "Specify /bin folder of a supported java installation ("
						+iniFileManager.MIN_JRE_VERSION 
						+ " <= VERSION < "
						+iniFileManager.MAX_JRE_VERSION+")";
				
				jvmHelper.manualPath_Jvm = FileUtils.directoRead(launcherMainFrame, msg).getAbsolutePath();
				ScrYpTLauncher.iniFileManager.manualPathJvm = jvmHelper.manualPath_Jvm;
				if (jvmHelper.checkSingleJavaVersionOK(jvmHelper.manualPath_Jvm.replace("\bin", ""))) {
					ScrYpTLauncher.iniFileManager.preferredJvm = jvmHelper.manualPath_Jvm;
				}
			}
		};
	}
	
	private static void appLogStart() {
		logger.debug(LoggerUtils.getStartString("ScrYpT - Launcher", true));
	}
	
	private static void appLogEnd() {
		logger.debug(LoggerUtils.getExitString("ScrYpT - Launcher", true));
	}
	
	public static void startPendingDeletesCleanerThread(){
		new Thread(new Runnable() {
			public void run() {
				logger.info("Starting removal of pending files to delete from last execution...");
				ArrayList<File> pendingDeletes = installedPluginProperties.getFileList(installedPluginPendingDeleteListKey, ";", true);
				if (pendingDeletes.isEmpty()) {
					logger.info("startPendingDeletesCleanerThread: nothing to delete :)");
				} else {
					for (File pending : pendingDeletes) {
						try {
							logger.info("Deleting pending file to be deleted: " + pending);
							pending.delete();
							logger.info(pending + " deleted succesfully!");
						} catch (Exception e) {
							logger.error("An error occurred, cannot delete current pending file to delete: " + pending);
						}
					}
				}
				
				installedPluginProperties.removeProperty(installedPluginPendingDeleteListKey);

				try {
					FileUtils.deleteDirContentRecursive(new File(GeneralConfig.PLUGINS_DIR));
					logger.info("PLUGINS folder cleaned");
				} catch (Exception e) {
					logger.error("Cannot clean PLUGINS folder");
				}
			}
		}).start();
	}
	
	/**
	 * Check if there is already an instance running, and in that case show a message to inform user or open the file into runnig instance
	 */
	public static void checkAlreadyRunning() {
		boolean alreadyRunning = GeneralConfig.SCRYPT_ACTIVE_SIGNAL_FILE != null && GeneralConfig.SCRYPT_ACTIVE_SIGNAL_FILE.exists();
		if(alreadyRunning) {
			
			boolean runNewInstance = false;
			
			if(argumentsFiles != null && argumentsFiles.length > 0) {
				try {
					int i = 0;
					PropertiesManager propertyManager = new PropertiesManager(GeneralConfig.SCRYPT_ACTIVE_SIGNAL_FILE);
					for (File currentToOpen : argumentsFiles) {
						propertyManager.dynamicFileBindingsDef.setProperty(GeneralConfig.SIGNAL_FILE_TO_LAUNCH_PROP_PREFIX + i, currentToOpen.getCanonicalPath());
						i++;
					}
					propertyManager.saveAllProperties();
				} catch (IOException e) {
					logger.error("{}", e);
				}
			} else {
				String advice = "<html>It seems there is already a running instance of " +GeneralConfig.APPLICATION_NAME 
						+ "<br/> -> If this message is not due to a previous application crash, it's strongly recommended to avoid multiple instances." 
						+ "<br/> Are you sure to start another instance?</html>";
				int choice = JOptionPane.showConfirmDialog(null, advice);
				runNewInstance = choice == JOptionPane.YES_OPTION;
			}
			
			if (!runNewInstance) {
				System.exit(0);
			}
		}
	}
}

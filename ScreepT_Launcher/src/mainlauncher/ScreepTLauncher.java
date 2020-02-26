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
package mainlauncher;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.apache.log4j.PropertyConfigurator;

import files.FileVarious;
import files.FileWorker;
import gui.GuiUtils;
import gui.LightLicenseViewer;
import gui.LightLicenseViewer.UserLicenseChoice;
import gui.dialogs.msg.JOptionHelper;
import gui.panels.CustomizeParametersPanel;
import gui.panels.DependenciesManagerPanel;
import gui.panels.LauncherMainPanel;
import gui.utils.LauncherLoaderFrame;
import gui.utils.LoggerUtils;
import launcherutils.CommandsAssembler;
import launcherutils.IniFileManager;
import launcherutils.JvmHelper;
import launcherutils.om.DependencyLocalPath;
import launcherutils.om.DependencyModel;
import launcherutils.om.JvmArgumentsDTO;
import om.OSinfo;
import resources.GeneralConfig;
import resources.ImagesPathConfigurator;
import utility.SafeLogger;
import utility.properties.PropertiesManager;
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
	 * => to run the application just customize GeneralConfig properties file with the name of main application jar.
	 * 
	 * => to customize launcher graphics just change the class of launcherMainFrame extending JFrame class with your own.
	 * 
	 * ENJOY :)
	 *
	 */
	public class ScreepTLauncher {
		
		// ScreepTLauncher logger
		static SafeLogger logger = new SafeLogger(ScreepTLauncher.class); 
		
		public static final String BATCH_LAUNCHER_NAME = "LauncherBatch.bat";
		public static final String BASH_LAUNCHER_NAME = "./LauncherBash.sh";
		
		public static String initPath = new OSinfo().isWindows() ? BATCH_LAUNCHER_NAME : BASH_LAUNCHER_NAME;
		
		public static String DEFAULT_INI_PATH; 
		
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
		
		public static LauncherMainPanel launcherMainFrame;
		public static ScreepTLauncher LAUNCHER;
		public static LauncherLoaderFrame loaderGraphics;
		
		public static String[] ARGS;
		public static File[] argumentsFiles;
		
		public static IniFileManager iniFileManager;
	
		// ACTION LISTENERS - JVM
		public ActionListener actionLocManually_Jvm;
		public ActionListener actionDownNInstall_Jvm;
		
		// use this as flag to signal that something is not ok with dependencies
		public static  boolean criticalDependenciesOK = false;
		public static  boolean criticalJVMnotFound = false;
		public static ArrayList<DependencyModel> missingDependenciesList;
		
		/** MAIN START POINT **/
		public static void main(String[] args) {
			
			// TEST error launch if first argument is err_test
			if(args.length > 0 && args[0].equals("err_test")) {
				System.exit(1);
			}
			
			checkAlreadyRunning();
			
			loaderGraphics = new LauncherLoaderFrame("Loading Launcher...", ImagesPathConfigurator.LAUNCHER_BACK_IMAGE_PATH, null);
			loaderGraphics.update("Reading configuration... ", 5);
			loaderGraphics.indeterminate(true);
			
			GeneralConfig.initLazyGeneralConfig();
			
			GuiUtils.setupTooltips(500, 30000, new Font("Segoe UI", Font.PLAIN, 17));
			
			ARGS = args;
			argumentsFiles = getFileArrayFromStrings(args);
			
			loaderGraphics.update("Checking license status... ", 10);
			loaderGraphics.indeterminate(true);
			
			// SHOW LICENSE IF MARKED AS UNACCEPTED
			if(!GeneralConfig.LICENSE_ACCEPTED) {
				
				LightLicenseViewer licenseViewer;
				UserLicenseChoice choice;
				do{
					loaderGraphics.setVisible(false);
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
	
			loaderGraphics.setVisible(true);
			startPendingDeletesCleanerThread();
			
			JVM_ARGS = " -Djava.library.path=\"./" + GeneralConfig.RESOURCES_DIR  + "lib\"";
			DEFAULT_INI_PATH = GeneralConfig.DEFAULT_LAUNCHER_INI_PATH; 
			
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
						LAUNCHER = new ScreepTLauncher();
	
					} // end run method
				}); // end invokeLater
			} catch (RuntimeException e) {
				logger.error("   =>  Exception catched during launch!\n\n", e);
			}
		}
		
		// CONSTRUCTOR
		public ScreepTLauncher() {
			
			jvmHelper = new JvmHelper(IniFileManager.MAX_JRE_VERSION, IniFileManager.MIN_JRE_VERSION);
			commandsAssembler = new CommandsAssembler(jvmHelper);
			
			currentOS = SysUtils.getOsInfoLazy();
			
		loaderGraphics.update("Defining listeners...", 35);
		
			defineActionListeners();
		
		loaderGraphics.update("Analyzing System...", 45);
			analyzeBeforeLauncher();
			
			// if there aren't blocking problems and direct launch configured or args are present -> launch directly
			// NB incompatible jvm is potential blocking cause for medium user, so by default when jvm is "warning" launcher panel will be prompted
			if(!JvmHelper.warningJVM && criticalDependenciesOK && (iniFileManager.hideNStartDirect || ARGS.length != 0)) {
				
				appLogEnd();
				
				launchCommands(initPath, iniFileManager.viewShellAfterStart);
				
				loaderGraphics.update("Ready", 101);
				
				try {
					Thread.sleep(800);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				System.exit(0);
			}
			
			// GUI launcher initialization
			launcherMainFrame = new LauncherMainPanel("ScreepT Launcher", this);
	
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
			if(missingDependenciesList.size() != 0 && (iniFileManager.showInstallOptions && !criticalDependenciesOK)) {
				launcherMainFrame.missingDependenciesPanel = new DependenciesManagerPanel("Check missing dependencies", launcherMainFrame);
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
			missingDependenciesList = new ArrayList<DependencyModel>();
			
			// RETRIEVE BEST MACTHING JVM ON CURRENT OS
			String javaPath = jvmHelper.retrieveCorrectJVMS(ignoreWarnings);
			if(javaPath.equals(JvmHelper.NOT_FOUND_JVM)) {
				addToMissingDependenciesJVM(true);
			} else {
				addToMissingDependenciesJVM(false);
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
			commandsAssembler.addLaunchCommand(dto, argumentsFiles, iniFileManager.viewShellAfterStart);
			
			try {
				String combinedCommands = commandsAssembler.getCombinedCommandList();
				initPath = writeInitBatShAndGetPath(combinedCommands);
			} catch (Throwable e) {
				logger.error("Error detected while launching application", e);
			}
			
			logger.info("Launch command generated:\n\n" + commandsAssembler.getCombinedCommandList() +"\n\n");
		}
		
		/**
		 * This method adds to missing dependencies JVM with references to download/install it
		 */
		public void addToMissingDependenciesJVM(boolean critical) {
			String localPathWinInstaller = DependencyModel.INSTALLERS_PATH + arch32OR64 + "/JRE-1.8-installer/jre-1.8u201.exe";
			String localPathLinInstaller = "";
			String localPathMacInstaller = "";
			DependencyLocalPath pathsLocalInstaller = new DependencyLocalPath(localPathWinInstaller, localPathLinInstaller, localPathMacInstaller);
			
			String javaMsg = critical? "Compatible JAVA JVM not found" : "Java interpreter";
			DependencyModel javaDependency = new DependencyModel(
					javaMsg, "Download/Install/Locate", critical, JvmHelper.JVM_URL_INSTALL, pathsLocalInstaller, actionDownNInstall_Jvm, actionLocManually_Jvm);
			missingDependenciesList.add(javaDependency);
			
			criticalJVMnotFound = critical;
		}
		
		/**
		 * retrieve list of commands, write to file and run it in current OS
		 */
		public static void launchCommands(String nativeLauncherPath, boolean shellVisible) {
			
			OSinfo OS = SysUtils.getOsInfoLazy();
			logger.info("Launching commands on currentOS: " + OS.getOsName() + OS.getOsArch());
			
			Runtime runtime = Runtime.getRuntime();
			
			try {
				try {
					new File(nativeLauncherPath).setExecutable(true);
					Thread.sleep(100);
				} catch (Exception e1) {}
				
				// CASE WINDOWS
				if (OS.isWindows()) {
					if (shellVisible) {
						runtime.exec("cmd /c start /min \"ScreepT-Shell\" \"" + nativeLauncherPath + "\"");
					}else {
						runtime.exec(nativeLauncherPath);
//						runtime.exec("cmd /c \"start /b " + nativeLauncherPath + "\"");
					}
				
				}else {
					
					// TODO LINUX/MAC SHELL show on/off
					logger.info("Executing process [pwd]");
					SysUtils.logProcessOutput(runtime.exec("pwd"));
					logger.info("Executing process [chmod u+x" + nativeLauncherPath + "]");
					Process executableMaker = runtime.exec("chmod u+x " + nativeLauncherPath);
					SysUtils.logProcessOutput(executableMaker);
					executableMaker.waitFor();
					
					runtime.exec(nativeLauncherPath);
				}
				logger.info("Commands launched without exceptions :)"); 
			} catch (Exception e) {
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
	
			String initFilePath = (currentOS.isWindows())? BATCH_LAUNCHER_NAME : BASH_LAUNCHER_NAME;
			logger.info("Creating launcher file : " + initFilePath);
			
			// write commands to build bash/batch starter file
			boolean written = FileWorker.writeStringToFile(commandList, new File(initFilePath), true);
			
			String msg = (written)? "Write completed correctly :)" : "Write not succeded for some error :(";
			logger.info(msg);
			
			return initFilePath;
		}	
		
		/**
		 * run commands to check  if dependencies software is installed, and to eventually install it.
		 */
		private void addInstallDependenciesCommand() {
			// TODO
			criticalDependenciesOK = !criticalJVMnotFound;
		}
		
		public void defineActionListeners() {
			actionLocManually_Jvm = new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					String msg = "Specify /bin folder of a supported java installation ("
							+ IniFileManager.MIN_JRE_VERSION 
							+ " <= VERSION < "
							+ IniFileManager.MAX_JRE_VERSION+")";
					
					File tempManualJvm = FileVarious.directoRead(launcherMainFrame, msg, new File(GeneralConfig.USER_PERSONAL_DIR));
					
					if (tempManualJvm != null && tempManualJvm.exists()) {
						jvmHelper.manualPath_Jvm = tempManualJvm.getAbsolutePath();
						ScreepTLauncher.iniFileManager.manualPathJvm = jvmHelper.manualPath_Jvm;
						if (jvmHelper.checkSingleJavaVersionOK(jvmHelper.manualPath_Jvm.replace("\bin", ""))) {
							ScreepTLauncher.iniFileManager.preferredJvm = jvmHelper.manualPath_Jvm;
						} 
					}
				}
			};
		}
		
		private static void appLogStart() {
			logger.debug(LoggerUtils.getStartString("ScreepT - Launcher", true));
		}
		
		private static void appLogEnd() {
			logger.debug(LoggerUtils.getExitString("ScreepT - Launcher", true));
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
	
					// delete updater.jar if present in application directory
					File oldUpdater = new File("updater.jar");
					if(oldUpdater.exists()) {
						try {
							logger.info("Found old updater.jar: it will be deleted now...");
							oldUpdater.delete();
							logger.info("updater.jar -> deleted succesfully.");
							
						} catch (Exception e) {
							logger.error("Cannot delete old updater.jar file");
						}
					}
					
					try {
						FileWorker.deleteDirContentRecursive(new File(GeneralConfig.PLUGINS_DIR));
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
			boolean alreadyRunning = GeneralConfig.SCREEPT_ACTIVE_SIGNAL_FILE != null && GeneralConfig.SCREEPT_ACTIVE_SIGNAL_FILE.exists();
			if(alreadyRunning) {
				
				boolean runNewInstance = false;
				
				if(argumentsFiles != null && argumentsFiles.length > 0) {
					try {
						int i = 0;
						PropertiesManager propertyManager = new PropertiesManager(GeneralConfig.SCREEPT_ACTIVE_SIGNAL_FILE);
						for (File currentToOpen : argumentsFiles) {
							propertyManager.dynamicFileBindingsDef.setProperty(GeneralConfig.SIGNAL_FILE_TO_LAUNCH_PROP_PREFIX + i, currentToOpen.getCanonicalPath());
							i++;
						}
						propertyManager.saveAllProperties();
					} catch (IOException e) {
						logger.error("{}", e);
					}
				} else {
					String advice = "<html>It seems there is already a running instance of " + GeneralConfig.APPLICATION_NAME 
							+ "<br/> -> If this message is not due to a previous application crash, it's strongly recommended to avoid multiple instances." 
							+ "<br/> Are you sure to start another instance?</html>";
					runNewInstance = new JOptionHelper(null).yesOrNo(advice, "ScreepT already running");
				}
				
				if (!runNewInstance) {
					System.exit(0);
				}
			}
		}
	}

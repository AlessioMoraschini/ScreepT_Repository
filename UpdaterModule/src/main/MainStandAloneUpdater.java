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
package main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.ini4j.Wini;

import files.FileWorker;
import files.FileVarious;
import files.ZipWorker;
import gui.UpdateProgressPanel;
import gui.dialogs.msg.JOptionHelper;
import main.om.PropertiesDTO;
import network.ftp.FtpDumper;
import network.progress.UploadAction;
import utility.hash.ChecksumHasher;
import utility.hash.HashingFunction;
import utility.log.SafeLogger;
import utility.properties.PropertiesManager;
import utility.string.StringWorker;

public class MainStandAloneUpdater {
	
	public static SafeLogger logger = new SafeLogger(MainStandAloneUpdater.class);
	
	public static final File CURRENT_DIR = new File(System.getProperty("user.dir"));
	public static final String THIS_JAR_PATH = "updater.jar";

	// Main application update static named files
	public static final String MAIN_LAUNCHER_JAR = "ScreepT_Launcher.jar";
	public static final String MAIN_APP_PROPERTIES_PATH = "resourcesFilePathsMappings.properties";
	
	// Other functionalities
	public static final String FTP_PROPERTIES_FILE = "FTP.properties";
	public static String UPDATES_PROPERTIES_FILE = "manifest.properties";
	
	// Properties file keys
	public final static String KEY_DEFAULT_LAUNCHER_INI_PATH = "DEFAULT_LAUNCHER_INI_PATH";
	private static final String KEY_ROOT_FILES_UPDATES_DIR = "ROOT_DIR_APP_FILES";
	private static final String KEY_FOLDER_ZIP_TO_ELABORATE = "RELEASE_FOLDER-OR-ZIP_TO_ELABORATE";
	
	
	private static final JOptionHelper dialogHelper = new JOptionHelper(null);
	public static final HashingFunction downloadsHashFunc = HashingFunction.SHA512;
	/**
	 * This is main application updater module, runnable as main class of updater.jar
	 * @param args -> args[0]= mainApplication jar ; args[1]=  updates extracted folder
	 */
	public static void main(String[] args) {
		
		if(args[0].trim().contains("-generateHashes")) {
			
			ArrayList<File> toUpload = new ArrayList<>();
			
			if (args[0].trim().contains("-mainRelease")) {
				logger.info("\n\n******************************************************" 
						+ "\n* MainRelease detected: -generateHashes-mainRelease  *"
						+ "\n******************************************************/\n");
				/** Usage -> generateHashes(mainRelease) function: java -jar updater.jar -generateHashes-mainRelease true manifest.properties**/
				toUpload = mainGenerateReleaseHashed(args);
				
			} else {
				logger.info("\n\n*******************************************" 
						+ "\n* Hash updates detected: -generateHashes  *"
						+ "\n*******************************************/\n");
				/******************************************************************************************************** 
				 * 	Usage -> generateHashes function: java -jar updater.jar -generateHashes true manifest.properties
				 ********************************************************************************************************/
				toUpload = mainGenerateUpdatesHashed(args);
			}
			
			if(args[0].trim().contains("-uploadFtpUpdates")) {
				
				logger.info("\n\n*******************************************" 
						+ "\n* Upload flag detected: -uploadFtpUpdates *"
						+ "\n*******************************************/\n");
				/******************************************************************************************************** 
				 * Function A.1: "-generateHashes-uploadFtpUpdates" (reads ftp properties from FTP_PROPERTIES file)
				 * 	Usage -> generateHashes function: 
				 * 
				 * java -jar updater.jar -generateHashes-uploadFtpUpdates true manifest.properties
				 * java -jar updater.jar -generateHashes-uploadFtpUpdates-mainRelease true manifest.properties
				 ********************************************************************************************************/
				mainUploadUpdatesViaFTP(toUpload);
			}
			
			signalOKAndExit0();
		
		} else {

			logger.info("\n\n***************************************************" 
					+ "\n* App embedded Update detected: -uploadFtpUpdates *"
					+ "\n***************************************************/\n");
			/**********************************************************************************************************		
			 * Function B: update ScreepT 
			 * Usage -> update ScreepT function: java -jar updater.jar updater.jar UPDATES/updates ScreepT_0.9.5.jar
			 **********************************************************************************************************/
			mainUpdateScreepT(args);			
		}
	}
	
	private static void mainUpdateScreepT(String[] args) {
		
		PropertiesManager resourcesFilePathsMappings = new PropertiesManager(MAIN_APP_PROPERTIES_PATH);
		
		final File updaterJar = new File(args[0]).getAbsoluteFile();
		final File updatesFolder = new File(args[1]).getAbsoluteFile();
		final File newJar = new File(args[2]).getAbsoluteFile();
		
		String manifestPath = updatesFolder.getAbsolutePath() + File.separator + UPDATES_PROPERTIES_FILE;
		PropertiesManager manifestProps = new PropertiesManager(manifestPath);
		
		// ScreepTVx.x/application/
		File generalApplicationFolder = new File("").getAbsoluteFile();
		// ScreepTVx.x/
		File mainRootDirectory = generalApplicationFolder.getParentFile();
		
		UpdateProgressPanel updateProgressMonitor = null;
		try {
			if(!validateUpdatesFolder(updaterJar, updatesFolder, manifestProps)) {
				launchJar(MAIN_LAUNCHER_JAR);
			}
			
			File oldJar = getOldJar(resourcesFilePathsMappings);
			
			if(updaterJar.exists() && updaterJar.isFile() && updatesFolder.exists() && updatesFolder.isDirectory()){

				// Current files backup - ask to user
				boolean proceed = false;
				do {
					Integer choice = dialogHelper.yesNoOrCanc(
							"ScreepT will be updated, do you want to backup current installation before to update?" +
						    " (update process will not touch your personal files, but if you prefer you can backup all ScreepT folder before to update)",
						    "Backup current Version files?");
					
					if (JOptionHelper.OK == choice) {
						boolean validDirectory = true;
						do {
							File destBackupDir = FileVarious.directoRead(null, "Select a backup destination, or proceed without backup. A unique \"ScreepT_updater_backup\" folder will be created in the specified destination.", new File(System.getProperty("user.home")));
							if (destBackupDir != null && destBackupDir.exists()) {
								destBackupDir = new File(destBackupDir.getCanonicalPath() + File.separator + "ScreepT_updater_backup");
								destBackupDir = FileWorker.renameJavaObjFile(destBackupDir);
								FileWorker.copyDirectoryContents(mainRootDirectory, destBackupDir);
							} else {
								validDirectory = !dialogHelper.yesOrNo("Destination (" + destBackupDir + ") is not valid: wanna choose another one?", "Invalid Backup destination");
							} 
						} while (!validDirectory);
					}

					if (JOptionHelper.CANC == choice) {
						if(dialogHelper.yesOrNo("Are you sure to exit update Procedure?", "Exit procedure?")) {
							launchJar(MAIN_LAUNCHER_JAR);
						}
					} else {
						proceed = true;
					}
					
				} while(!proceed);
				
				// Proceed with update
				updateProgressMonitor = new UpdateProgressPanel(true, true); 
				updateProgressMonitor.update(30, "Copying new files...");
				
				PropertiesDTO backupProperties = getPropertiesToKeep(resourcesFilePathsMappings);
				
				// replace existing files matching with updates (don't touch user personal files)
				logger.info("Copying content of " + updatesFolder + " into " + generalApplicationFolder);
				FileWorker.copyDirectoryContents(updatesFolder, generalApplicationFolder);
				
				// Check if there is Root files update to copy one level up
				File rootDirUpdates = new File(KEY_ROOT_FILES_UPDATES_DIR);
				if(rootDirUpdates.exists()) {
					try {
						updateProgressMonitor.update(70, "Updating licenses and launcher...", true);
						FileWorker.copyDirectoryContents(rootDirUpdates, mainRootDirectory);
						FileWorker.deleteDirContentInclRoot(rootDirUpdates);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
				updateProgressMonitor.update(85, 100, "Updating configuration...");
				restorePropertiesToKeep(backupProperties, MAIN_APP_PROPERTIES_PATH);
				saveNewJarRef(resourcesFilePathsMappings, newJar);
				
				FileUtils.deleteQuietly(oldJar);
				
				updateProgressMonitor.update(100, "Completed! Restarting ScreepT...", true);
				
			} else {
				String fileData = "UpdaterJar => "+updaterJar+"; updates folder => "+updatesFolder;
				dialogHelper.error("Updates data or updater jar not found, exiting updated procedure." + fileData, " (Updates not found)");
			}
			
			launchJar(MAIN_LAUNCHER_JAR);
			
		} catch (Throwable e) {
			
			dialogHelper.errorScroll("Cannot update, an error occurred: " + e.getClass(), e.getClass().getCanonicalName(), e);
			logger.error(e.getClass().getCanonicalName(), e);
			
			try {
				launchJar(MAIN_LAUNCHER_JAR);
			} catch (IOException e1) {
				logger.error(e.getClass().getCanonicalName(), e);
			}
			
		} finally {
			if (updateProgressMonitor != null) {
				updateProgressMonitor.forceClose();
			}
			System.exit(0);
		}
	}

	private static ArrayList<File> mainGenerateUpdatesHashed(String[] args) {
		
		Boolean createUpdatesZip = Boolean.valueOf(args[1]);
		UPDATES_PROPERTIES_FILE = args[2];
		PropertiesManager manifestProps = new PropertiesManager(UPDATES_PROPERTIES_FILE);

		File updatesDir = new File("updates");
		// Only for updates folder -> zip folder into current one
		if(createUpdatesZip && updatesDir.isDirectory() && updatesDir.exists()) {
			try {
				ZipWorker.zipFolder(updatesDir.getAbsolutePath(), CURRENT_DIR, true, true);
			} catch (Exception e) {
				signalErrorAndExit1(e);
			}
		}

		ArrayList<File> toUpload = new ArrayList<>();
		toUpload.add(new File(UPDATES_PROPERTIES_FILE));
		for(File curr : CURRENT_DIR.listFiles()) {
			
			if (curr.isFile() 
					&& !curr.getName().endsWith(".bat") 
					&& !curr.getName().equals(UPDATES_PROPERTIES_FILE)
					&& !curr.getName().equals(FTP_PROPERTIES_FILE)) {
				
				try {
					String hash = ChecksumHasher.getFileHashBuffered(curr.getName(), downloadsHashFunc, true);
					manifestProps.saveCommentedProperty(curr.getName(), hash);
					manifestProps.readFromFile();
					if(!manifestProps.getProperty(curr.getName(), "").equals(hash)) {
						new JOptionHelper(null).warn("Hash control not passed: check properties for " + curr.getName(), "Hash not matching");
					} else {
						toUpload.add(curr);
					}
				} catch (Throwable e) {
					signalErrorAndExit1(e);
				}
			}
			
		}
		
		return toUpload;
	}

	private static ArrayList<File> mainGenerateReleaseHashed(String[] args) {

		Boolean createUpdatesZip = Boolean.valueOf(args[1]);
		UPDATES_PROPERTIES_FILE = args[2];

		File releaseFolderOrZip = new File(new PropertiesManager(FTP_PROPERTIES_FILE).getProperty(KEY_FOLDER_ZIP_TO_ELABORATE));
		logger.info("Src: " + releaseFolderOrZip.getAbsolutePath());
		
		// Only for folder to be zipped -> zip folder into current one
		if(createUpdatesZip && releaseFolderOrZip.isDirectory() && releaseFolderOrZip.exists()) {
			try {
				// zip the wanted folder and place the zipped file into current directory
				logger.info("Zipping Src folder: " + releaseFolderOrZip.getAbsolutePath());
				releaseFolderOrZip = ZipWorker.zipFolder(releaseFolderOrZip.getAbsolutePath(), CURRENT_DIR, true, true);
				logger.info("Src folder zipped! Result file is: " + releaseFolderOrZip.getAbsolutePath());
			} catch (Exception e) {
				signalErrorAndExit1(e);
			}
		}

		ArrayList<File> toUpload = new ArrayList<>();
		toUpload.add(new File(UPDATES_PROPERTIES_FILE));
		
		File updaterJarFile = new File(THIS_JAR_PATH);
		if (updaterJarFile.exists()) {
			toUpload.add(updaterJarFile);
		}
		
		if(releaseFolderOrZip != null && releaseFolderOrZip.isFile() && StringWorker.trimToEmpty(releaseFolderOrZip.getName()).endsWith(".zip")) {
			
			try {
				PropertiesManager manifestProps = new PropertiesManager(UPDATES_PROPERTIES_FILE);

				if (updaterJarFile.exists()) {
					String updaterHash = ChecksumHasher.getFileHashBuffered(THIS_JAR_PATH, downloadsHashFunc, true);
					manifestProps.saveCommentedProperty(THIS_JAR_PATH, updaterHash);
				}
				String hash = ChecksumHasher.getFileHashBuffered(releaseFolderOrZip, downloadsHashFunc, true);
				manifestProps.saveCommentedProperty(releaseFolderOrZip.getName(), hash);
				manifestProps.saveAllProperties();
				manifestProps.readFromFile();
				if (!manifestProps.getProperty(releaseFolderOrZip.getName(), "").equals(hash)) {
					new JOptionHelper(null).warn(
							"Hash control not passed: check properties for " + releaseFolderOrZip.getName(), "Hash not matching");
				} else {
					toUpload.add(releaseFolderOrZip);
				}
			} catch (Throwable e) {
				signalErrorAndExit1(e);
			}
		}
		
		return toUpload;
	}
	

	////////////////////// JAR METHODS //////////////////////////
	
	private static File getOldJar(PropertiesManager resourcesFilePathsMappings) {
		try {
			String DEFAULT_LAUNCHER_INI_PATH = resourcesFilePathsMappings.getProperty(MainStandAloneUpdater.KEY_DEFAULT_LAUNCHER_INI_PATH, "LAUNCHER/Launcher_Configuration.ini");
			Wini iniLauncherConfig = new Wini(new File(DEFAULT_LAUNCHER_INI_PATH));
			String oldJarName = iniLauncherConfig.get("APPLICATION_OPTIONS", "jarToLaunch");
			return (oldJarName != null) ? new File(oldJarName) : null;
		} catch (Exception e) {
			logger.error("Cannot retrieve old jar", e);
			return null;
		}
	}
	
	private static void saveNewJarRef(PropertiesManager resourcesFilePathsMappings, File newJar) {
		try {
			String DEFAULT_LAUNCHER_INI_PATH = resourcesFilePathsMappings.getProperty(MainStandAloneUpdater.KEY_DEFAULT_LAUNCHER_INI_PATH, "LAUNCHER/Launcher_Configuration.ini");
			Wini iniLauncherConfig = new Wini(new File(DEFAULT_LAUNCHER_INI_PATH));
			
			iniLauncherConfig.put("APPLICATION_OPTIONS", "jarToLaunch", newJar.getName());
			iniLauncherConfig.store();
		} catch (Exception e) {
			logger.error("Cannot save new jar reference", e);
		}
	}
	
	private static void launchJar(String launcherPath) throws IOException {
		Runtime runtime = Runtime.getRuntime();
		runtime.exec("java -jar " + launcherPath);
		
		System.exit(0);
	}
	
	/**
	 * Updates Hashes vaildation
	 */
	private static boolean validateUpdatesFolder(File updaterJar, File updatesFolder, PropertiesManager manifestProps) {
		// .... /UPDATES/updates.zip  ::  (updatesFolder = .... /UPDATES/updates/)
		File zipUpdates = new File(updatesFolder.getParentFile() + File.separator + "updates.zip");
		boolean proceed = true;
		try {
			proceed = (!ChecksumHasher.checkHashFromProperties(updaterJar, manifestProps, downloadsHashFunc)) ? false : proceed;
			proceed = (!ChecksumHasher.checkHashFromProperties(zipUpdates, manifestProps, downloadsHashFunc)) ? false : proceed;
		} catch (Throwable e) {
			proceed = false;
		}
		
		if(!proceed) {
			if(dialogHelper.yesOrNo("It was not possible to verify integrity of downloaded updates, install anyway?")) {
				proceed = true;
			} else {
				proceed = false;
			}
		}
		
		return proceed;
	}
	
	// BACKUP-RESTORE PROPERTIES
	public static PropertiesDTO getPropertiesToKeep(PropertiesManager resourcesFilePathsMappings) {
		PropertiesDTO propertiesDTO = new PropertiesDTO();
		propertiesDTO.proxyConfString = resourcesFilePathsMappings.getProperty(PropertiesDTO.DEF_PROXY_CONFIG_KEY);
		return propertiesDTO;
	}
	public static void restorePropertiesToKeep(PropertiesDTO backupProperties, String propertiesFilePath) {
		PropertiesManager resourcesFilePathsMappings = new PropertiesManager(propertiesFilePath);
		resourcesFilePathsMappings.saveCommentedProperty(PropertiesDTO.DEF_PROXY_CONFIG_KEY, backupProperties.proxyConfString);
	}
	
	/**
	 *  SIGNAL KO BEFORE EXIT
	 */
	private static void signalErrorAndExit1(Throwable e) {
		logger.error("\n\n*******************" 
				+ "\n* PROCESS FAILED! *"
				+ "\n*******************/\n");
		logger.error("An error occurred: ",e);
		try {
			Thread.sleep(10000);
			System.exit(1);
		} catch (InterruptedException e1) {
		}
	}

	/**
	 *  SIGNAL OK BEFORE EXIT
	 */
	private static void signalOKAndExit0() {
		logger.info("\n\n*********************" 
				+ "\n* COMPLETE SUCCESS! *"
				+ "\n*********************/\n");
		logger.info("Finished! Updater will close after 5 seconds from now...");
		try {
			Thread.sleep(5000);
			System.exit(0);
		} catch (InterruptedException e1) {
		}
	}
	
	/**
	 * FTP update mode - Developer usage only
	 */
	private static void mainUploadUpdatesViaFTP(ArrayList<File> toUploadList) {
		
		PropertiesManager ftpProperties;
		try {
			ftpProperties = new PropertiesManager(FTP_PROPERTIES_FILE);
		} catch (Exception e) {
			logger.error("Cannot read properties file: " + FTP_PROPERTIES_FILE, e);
			throw e;
		}
		
		String HOST = ftpProperties.getProperty("HOST", "ftp.am-design-development.com");
		String USER = ftpProperties.getProperty("USER", "");
		String PSW = ftpProperties.getProperty("PSW", "");
		int PORT = ftpProperties.getIntVarFromProps("PORT", 22);
		String FOLDER_ON_SERVER = ftpProperties.getProperty("FOLDER_ON_SERVER", "/www.am-design-development.com/ScreepT/Updates");
		
		if(StringWorker.isEmpty(USER)) {
			USER = new JOptionHelper(null).askForString("Specify USERNAME");
		}
		
		if (PSW.equals("")) {
			char[] psw = new JOptionHelper(null).askForPasswordNullable("Insert FTPS password", "Insert FTPS password", 64, "Upload");
			if(psw != null) {
				PSW = new String(psw);
			}
		}
		
		FtpDumper dumper = new FtpDumper(HOST, USER, PSW, PORT);
		
		UpdateProgressPanel progPanel = new UpdateProgressPanel(true);
		
		// Remove remote manifest before to start upload, and upload separately after other files:
		// -> this will avoid users to start updates during the update process.
		File manifestFound = findManifestAndMoveAtTheEnd(toUploadList);
		if(manifestFound != null) {
			try {
				logger.info("Deleting remote manifest: " + UPDATES_PROPERTIES_FILE);
				boolean deleted = dumper.deleteFile(FOLDER_ON_SERVER, manifestFound.getName());
				logger.info("Remote manifest deleted? -> " + deleted);
			} catch (Exception e) {
				logger.warn("Cannot delete remote " + UPDATES_PROPERTIES_FILE + " before to upload!", e);
			}
		}
		
		try {
			int i = 1;
			for (File currentToUpload : toUploadList) {
				logger.info("Uploading: " + currentToUpload + " ...");
				
				dumper.setUploadAction(getUploadAction(progPanel, currentToUpload.length(), currentToUpload.getName()));
				int generalProgress = (int)(i*100 / toUploadList.size());
				generalProgress = generalProgress < 100 ? generalProgress : 99;
				progPanel.update(generalProgress, currentToUpload + " (" + String.valueOf(i) + "/" + toUploadList.size() + ")", true);
				progPanel.adaptWidthToCurrentText();
				
				dumper.uploadFile(currentToUpload.getAbsolutePath(), currentToUpload.getName(), FOLDER_ON_SERVER);
				logger.info(currentToUpload + " -> Uploaded");
				
				i++;
			}
		
		} catch (Throwable e) {
			e.printStackTrace();
		
		} finally {
			progPanel.forceClose();
			dumper.disconnect();
			
			new JOptionHelper(null).info("FTP upload completed!", "Upload completed");
		}
	}

	private static UploadAction getUploadAction(UpdateProgressPanel progPanel, long fileSize, String fileName) {
		return new UploadAction() {
			@Override
			public void doAfterUploadProgress(int percent, String currentFile) {
				progPanel.update(percent, null, false);
			}
		};
	}
	
	private static File findManifestAndMoveAtTheEnd(List<File> readyToDownload) {
		for(File current : readyToDownload) {
			if(UPDATES_PROPERTIES_FILE.equals(current.getName())) {
				readyToDownload.remove(current);
				readyToDownload.add(current); // Moves the file at the end of the list
				
				return current;
			}
		}
		
		return null;
	}

}

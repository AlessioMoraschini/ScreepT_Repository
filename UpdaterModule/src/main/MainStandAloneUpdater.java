package main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ini4j.Wini;

import files.Various;
import files.ZipWorker;
import gui.JOptionHelper;
import gui.UpdateProgressPanel;
import network.ftp.FtpDumper;
import network.progress.UploadAction;
import utility.PropertiesManagerLight;
import utility.SafeLogger;
import utility.hash.ChecksumHasher;
import utility.hash.HashingFunction;

public class MainStandAloneUpdater {
	
	public static SafeLogger logger = new SafeLogger(MainStandAloneUpdater.class);

	public static final String UPDATES_MANIFEST = "manifest.properties";
	public static final String FTP_PROPERTIES = "FTP.properties";
	public static final String resourcesMappingDefPath = "resourcesFilePathsMappings.properties";
	private static final String ROOT_FILES_UPDATES_DIR = "ROOT_DIR_APP_FILES";
	
	private static final JOptionHelper dialogHelper = new JOptionHelper(null);
	
	public static final HashingFunction downloadsHashFunc = HashingFunction.SHA512;
	/**
	 * This is main application updater module, runnable as main class of updater.jar
	 * @param args -> args[0]= mainApplication jar ; args[1]=  updates extracted folder
	 */
	public static void main(String[] args) {
		
		PropertiesManagerLight resourcesFilePathsMappings = new PropertiesManagerLight(resourcesMappingDefPath);
		String DEFAULT_LAUNCHER_INI_PATH = resourcesFilePathsMappings.getProperty("DEFAULT_LAUNCHER_INI_PATH", "LAUNCHER/Launcher_Configuration.ini");
		
		File currentDir = new File(System.getProperty("user.dir"));
		File updatesDir = new File("updates");

		if(args[0].trim().contains("-generateHashes")) {
			
			/******************************************************************************************************** 
			 * Function A: "generateHashes"
			 * 	Usage -> generateHashes function: java -jar updater.jar -generateHashes true manifest.properties
			 ********************************************************************************************************/
			
			Boolean createUpdatesZip = Boolean.valueOf(args[1]);
			String updatesProperties = args[2];
			
			PropertiesManagerLight manifestProps = new PropertiesManagerLight(updatesProperties);

			// Only for updates folder -> zip folder into current one
			if(createUpdatesZip && updatesDir.isDirectory() && updatesDir.exists()) {
				try {
					ZipWorker.zipFolder(updatesDir.getAbsolutePath(), currentDir, true, true);
				} catch (Exception e) {
					signalErrorAndExit1(e);
				}
			}

			ArrayList<File> toUpload = new ArrayList<>();
			toUpload.add(new File(updatesProperties));
			for(File curr : currentDir.listFiles()) {
				
				if (curr.isFile() 
						&& !curr.getName().endsWith(".bat") 
						&& !curr.getName().equals(updatesProperties)
						&& !curr.getName().equals(FTP_PROPERTIES)) {
					
					try {
						manifestProps.saveProperty(curr.getName(), ChecksumHasher.getFileHash(curr, downloadsHashFunc, true));
						manifestProps.readFromFile();
						if(!manifestProps.getProperty(curr.getName(), "").equals(ChecksumHasher.getFileHash(curr, downloadsHashFunc, true))) {
							new JOptionHelper(null).warn("Hash control not passed: check properties for " + curr.getName(), "Hash not matching");
						} else {
							toUpload.add(curr);
						}
					} catch (Throwable e) {
						signalErrorAndExit1(e);
					}
				}
				
			}
			
			/******************************************************************************************************** 
			 * Function A.1: "uploadFtpUpdates" (reads ftp properties from FTP_PROPERTIES file)
			 * 	Usage -> generateHashes function: 
			 * 
			 * java -jar updater.jar -generateHashes-uploadFtpUpdates true manifest.properties
			 ********************************************************************************************************/
			if(args[0].trim().contains("-uploadFtpUpdates")) {
				uploadUpdatesViaFTP(toUpload);
			}
			
			signalOKAndExit0();
		
		}else {

			/**********************************************************************************************************		
			 * Function B: update ScreepT 
			 * Usage -> update ScreepT function: java -jar updater.jar updater.jar UPDATES/updates ScreepT_0.9.5.jar
			 **********************************************************************************************************/
			// This is normal updater function //
			
			final File updaterJar = new File(args[0]).getAbsoluteFile();
			final File updatesFolder = new File(args[1]).getAbsoluteFile();
			final File newJar = new File(args[2]).getAbsoluteFile();
			
			String manifestPath = updatesFolder.getAbsolutePath() + File.separator + UPDATES_MANIFEST;
			PropertiesManagerLight manifestProps = new PropertiesManagerLight(manifestPath);
			
			final String mainApplicationLauncher = "ScreepT_Launcher.jar";
			
			// ScreepTVx.x/application/
			File generalApplicationFolder = new File("").getAbsoluteFile();
			// ScreepTVx.x/
			File mainRootDirectory = generalApplicationFolder.getParentFile();
			
			UpdateProgressPanel updateProgressMonitor = null;
			try {
				
				// Validate downloaded files
				if(!validateUpdatesFolder(updaterJar, updatesFolder, manifestProps)) {
					launchJar(mainApplicationLauncher);
				}
				
				Wini iniLauncherConfig = new Wini(new File(DEFAULT_LAUNCHER_INI_PATH));
				String oldJarName = iniLauncherConfig.get("APPLICATION_OPTIONS", "jarToLaunch");
				File oldJar = (oldJarName != null) ? new File(oldJarName) : null;
				
				if(updaterJar.exists() && updaterJar.isFile() && updatesFolder.exists() && updatesFolder.isDirectory()){

					// Current files backup - ask to user
					boolean proceed = false;
					do {
						Integer choice = dialogHelper.yesNoOrCanc(
								"ScreepT will be updated, do you want to backup current installation before to update?"
										+ " (update process will not touch your personal files, but if you prefer you can backup all ScreepT folder before to update)", "Backup current Version files?");
						if (JOptionHelper.OK == choice) {
							File destBackupDir = Various.directoRead(null,
									"Select backup destination, or proceed without backup. A unique \"ScreepT_updater_backup\" folder will be created in the specified destination.",
									new File(System.getProperty("user.home")));
							if (destBackupDir != null) {
								destBackupDir = new File(destBackupDir.getCanonicalPath() + File.separator + "ScreepT_updater_backup");
								destBackupDir = Various.renameJavaObjFile(destBackupDir);
								Various.copyDirectoryContents(mainRootDirectory, destBackupDir);
							}
						}

						if (JOptionHelper.CANC == choice) {
							if(dialogHelper.yesOrNo("Are you sure to exit update Procedure?", "Exit procedure?")) {
								launchJar(mainApplicationLauncher);
							} else {
								proceed = true;
							}
						} else {
							proceed = true;
						}
						
					} while(!proceed);
					
					// Proceed with update
					updateProgressMonitor = new UpdateProgressPanel(true); 
					updateProgressMonitor.setUndefinedDuration();
					updateProgressMonitor.update(30, "Copying new files...", true);
					updateProgressMonitor.update(30, "Copying new files...", false);
					
					// replace existing files matching with updates (don't touch user personal files)
					System.out.print("Copying content of " + updatesFolder + " into ");
					
					System.out.println(generalApplicationFolder.getAbsolutePath());
					Various.copyDirectoryContents(updatesFolder, generalApplicationFolder);
					
					// Check if there is Root files update to copy one level up
					File rootDirUpdates = new File(ROOT_FILES_UPDATES_DIR);
					if(rootDirUpdates.exists()) {
						try {
							updateProgressMonitor.update(70, "Updating licenses and launcher...", true);
							Various.copyDirectoryContents(rootDirUpdates, mainRootDirectory);
							Various.deleteDirContentInclRoot(rootDirUpdates);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					
					updateProgressMonitor.update(85, "Updating configuration...", true);
					updateProgressMonitor.update(100, "Updating configuration...", false);
					
					// update main application reference in .ini settings for launcher
					iniLauncherConfig.put("APPLICATION_OPTIONS", "jarToLaunch", newJar.getName());
					iniLauncherConfig.store();
					
					if (oldJar != null && oldJar.exists()) {
						oldJar.deleteOnExit();
					}
					
					updateProgressMonitor.update(100, "Completed! Restarting ScreepT...", true);
					updateProgressMonitor.forceClose();
					
				} else {
					String fileData = "UpdaterJar => "+updaterJar+"; updates folder => "+updatesFolder;
					dialogHelper.error("Updates data or updater jar not found, exiting updated procedure." + fileData, "Updates not found");
				}
				
				// LAUNCH ScreepT_Launcher.jar
				launchJar(mainApplicationLauncher);
				
			} catch (Exception e) {
				if (updateProgressMonitor != null) {
					
					updateProgressMonitor.update(100, "Completed! Restarting ScreepT...", true);
					updateProgressMonitor.forceClose();
				}
				
				dialogHelper.error("Cannot update, an error occurred: " + e.getClass());
				System.out.println(e.getClass() + Arrays.deepToString(e.getStackTrace()));
				
				// LAUNCH ScreepT_Launcher.jar
				Runtime runtime = Runtime.getRuntime();
				try {
					runtime.exec("java -jar " + mainApplicationLauncher);
				} catch (IOException e1) {
				}
				
				System.exit(0);
			}
		}
	}
	
	private static void launchJar(String launcherPath) throws IOException {
		Runtime runtime = Runtime.getRuntime();
		runtime.exec("java -jar " + launcherPath);
		
		System.exit(0);
	}
	
	private static boolean validateUpdatesFolder(File updaterJar, File updatesFolder, PropertiesManagerLight manifestProps) {
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
	
	private static void signalErrorAndExit1(Throwable e) {
		logger.error("An error occurred: ",e);
		try {
			Thread.sleep(10000);
			System.exit(1);
		} catch (InterruptedException e1) {
		}
	}

	private static void signalOKAndExit0() {
		logger.info("Finished! Updater will close after 5 seconds from now...");
		try {
			Thread.sleep(5000);
			System.exit(0);
		} catch (InterruptedException e1) {
		}
	}
	
	private static void uploadUpdatesViaFTP(ArrayList<File> toUploadList) {
		
		PropertiesManagerLight ftpProperties = new PropertiesManagerLight(FTP_PROPERTIES);
		String HOST = ftpProperties.getProperty("HOST", "ftp.am-design-development.com");
		String USER = ftpProperties.getProperty("USER", "");
		String PSW = ftpProperties.getProperty("PSW", "");
		int PORT = ftpProperties.getIntVarFromProps("PORT", 22);
		String FOLDER_ON_SERVER = ftpProperties.getProperty("FOLDER_ON_SERVER", "/www.am-design-development.com/ScreepT/Updates");
		
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
				logger.info("Deleting remote manifest: " + UPDATES_MANIFEST);
				boolean deleted = dumper.deleteFile(FOLDER_ON_SERVER, manifestFound.getName());
				logger.info("Remote manifest deleted? -> " + deleted);
			} catch (Exception e) {
				logger.warn("Cannot delete remote " + UPDATES_MANIFEST + " before to upload!", e);
			}
		}
		
		try {
			int i = 1;
			for (File currentToUpload : toUploadList) {
				System.out.println("Uploading: " + currentToUpload + " ...");
				
				dumper.setUploadAction(getUploadAction(progPanel, currentToUpload.length(), currentToUpload.getName()));
				int generalProgress = (int)(i*100 / toUploadList.size());
				generalProgress = generalProgress < 100 ? generalProgress : 99;
				progPanel.update(generalProgress, currentToUpload + " (" + String.valueOf(i) + "/" + toUploadList.size() + ")", true);
				progPanel.adaptWidthToCurrentText();
				
				dumper.uploadFile(currentToUpload.getAbsolutePath(), currentToUpload.getName(), FOLDER_ON_SERVER);
				System.out.println(currentToUpload + " -> Uploaded");
				
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
	
	private static File findManifestAndMoveAtTheEnd(List<File> readyToDownload) {
		for(File current : readyToDownload) {
			if(UPDATES_MANIFEST.equals(current.getName())) {
				readyToDownload.remove(current);
				readyToDownload.add(current); // Moves the file at the end of the list
				
				return current;
			}
		}
		
		return null;
	}
	
	private static UploadAction getUploadAction(UpdateProgressPanel progPanel, long fileSize, String fileName) {
		return new UploadAction() {

			@Override
			public void doAfterUploadProgress(int percent, String currentFile) {
				progPanel.update(percent, null, false);
			}
		};
	}
}

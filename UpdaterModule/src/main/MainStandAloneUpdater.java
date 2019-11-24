package main;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.swing.JOptionPane;

import org.ini4j.Wini;

import forks.merged.PropertiesManagerFork;
import forks.merged.Various;
import gui.UpdateProgressPanelMin;
import updater.DownloadUtils;
import updater.HashingFunction;

public class MainStandAloneUpdater {

	public static final String resourcesMappingDefPath = "resourcesFilePathsMappings.properties";
	
	public static final HashingFunction downloadsHashFunc = HashingFunction.MD5;
	/**
	 * This is main application updater module, runnable as main class of updater.jar
	 * @param args -> args[0]= mainApplication jar ; args[1]=  updates extracted folder
	 */
	public static void main(String[] args) {
		
		PropertiesManagerFork resourcesFilePathsMappings = new PropertiesManagerFork(resourcesMappingDefPath);
		String RESOURCES_DIR = resourcesFilePathsMappings.getProperty("RESOURCES_DIR", "Resources_Scrypt/");  
		String DEFAULT_LAUNCHER_INI_PATH = resourcesFilePathsMappings.getProperty("DEFAULT_LAUNCHER_INI_PATH", "LAUNCHER/Launcher_Configuration.ini");
		
		File currentDir = new File(System.getProperty("user.dir"));
		File updatesDir = new File("updates");
		
		// Usage -> generateHashes function: java -jar updater.jar -generateHashes true manifest.properties
		if(args[0].trim().equals("-generateHashes")) {
			// this is updater "update hashes" function //
			
			Boolean createUpdatesZip = Boolean.valueOf(args[1]);
			PropertiesManagerFork manifestProps = new PropertiesManagerFork(args[2]);

			// Only for updates folder -> zip folder into current one
			if(createUpdatesZip && updatesDir.isDirectory() && updatesDir.exists()) {
				try {
					DownloadUtils.zipFolder(updatesDir.getAbsolutePath(), currentDir, true);
				} catch (Exception e) {
					signalErrorAndExit1(e);
				}
			}

			for(File curr : currentDir.listFiles()) {
				
				if (curr.isFile() && !curr.getName().endsWith(".bat") && !curr.getName().equals("manifest.properties")) {
					try {
						manifestProps.saveProperty(curr.getName(), DownloadUtils.getFileHash(curr, downloadsHashFunc));
					} catch (Throwable e) {
						signalErrorAndExit1(e);
					}
				}
				
			}
			signalOKAndExit0();
			
		// Usage -> update scrypt function: java -jar updater.jar updater.jar UPDATES/updates ScrYpT_0.9.5.jar
		}else {
			// This is normal updater function //
			
			final File updaterJar = new File(args[0]).getAbsoluteFile();
			final File updatesFolder = new File(args[1]).getAbsoluteFile();
			final File newJar = new File(args[2]).getAbsoluteFile();
			
			String manifestPath = updatesFolder.getAbsolutePath() + File.separator + "manifest.properties";
			PropertiesManagerFork manifestProps = new PropertiesManagerFork(manifestPath);
			
			final String mainApplicationLauncher = "ScrYpT_Launcher.jar";
			
			// main external folder -> application -> Resources_Scrypt
			File mainDirectory = new File(RESOURCES_DIR).getAbsoluteFile().getParentFile().getParentFile();
			UpdateProgressPanelMin updateProgressMonitor = null;
			try {
				
				// Validate downloaded files
				if(!validateUpdatesFolder(updaterJar, updatesFolder, manifestProps)) {
					launchJar(mainApplicationLauncher);
				}
				
				Wini iniLauncherConfig = new Wini(new File(DEFAULT_LAUNCHER_INI_PATH));
				String oldJarName = iniLauncherConfig.get("APPLICATION_OPTIONS", "jarToLaunch");
				File oldJar = (oldJarName != null) ? new File(oldJarName) : null;
				
				if(updaterJar.exists() && updaterJar.isFile() && updatesFolder.exists() && updatesFolder.isDirectory()){
					Integer choice = JOptionPane.showConfirmDialog(null, "ScrYpT will be updated, do you want to backup current installation before to update? (update process will not touch your personal files, but if you prefer you can backup all scrypt folder before to update)");
					if(JOptionPane.YES_OPTION == choice)
					{
						File destBackupDir = Various.directoRead(null, "Select backup destination, or press cancel to continue update procedure without backup", new File(System.getProperty("user.home")));
						if (destBackupDir != null) {
							destBackupDir = Various.renameJavaObjFile(destBackupDir);
							Various.copyDirectoryContents(mainDirectory, destBackupDir);
						} 
					}
					updateProgressMonitor = new UpdateProgressPanelMin(true); 
					updateProgressMonitor.setUndefinedDuration();
					updateProgressMonitor.update(30, "Copying new files...", true);
					updateProgressMonitor.update(30, "Copying new files...", false);
					
					// replace existing files matching with updates (don't touch user personal files)
					System.out.print("Copying content of " + updatesFolder + " into ");
					File generalApplicationFolder = new File("").getAbsoluteFile();
					System.out.println(generalApplicationFolder.getAbsolutePath());
					Various.copyDirectoryContents(updatesFolder, generalApplicationFolder);
					
					updateProgressMonitor.update(70, "Updating configuration...", true);
					updateProgressMonitor.update(100, "Updating configuration...", false);
					
					// update main application reference in .ini settings for launcher
					iniLauncherConfig.put("APPLICATION_OPTIONS", "jarToLaunch", newJar.getName());
					iniLauncherConfig.store();
					
					if (oldJar != null && oldJar.exists()) {
						oldJar.deleteOnExit();
					}
					
					updateProgressMonitor.update(100, "Completed! Restarting ScrYpT...", true);
					updateProgressMonitor.forceClose();
					
				} else {
					String fileData = "UpdaterJar => "+updaterJar+"; updates folder => "+updatesFolder;
					JOptionPane.showMessageDialog(null, "Updates data or updater jar not found, exiting updated procedure."+fileData, "Updates not found", JOptionPane.ERROR_MESSAGE);
				}
				
				// LAUNCH ScrYpT_Launcher.jar
				launchJar(mainApplicationLauncher);
				
			} catch (Exception e) {
				if (updateProgressMonitor != null) {
					
					updateProgressMonitor.update(100, "Completed! Restarting ScrYpT...", true);
					updateProgressMonitor.forceClose();
				}
				JOptionPane.showMessageDialog(null, "Cannot update, an error occurred: " + e.getClass());
				System.out.println(e.getClass() + Arrays.deepToString(e.getStackTrace()));
				
				// LAUNCH ScrYpT_Launcher.jar
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
	
	private static boolean validateUpdatesFolder(File updaterJar, File updatesFolder, PropertiesManagerFork manifestProps) {
		File zipUpdates = new File(updatesFolder.getParentFile() + File.separator + "updates.zip");
		boolean esit = true;
		try {
			esit = (!checkHash(updaterJar, manifestProps)) ? false : esit;
			esit = (!checkHash(zipUpdates, manifestProps)) ? false : esit;
		} catch (Throwable e) {
			esit = false;
		}
		
		if(!esit) {
			Integer choice = JOptionPane.showConfirmDialog(null, "It was not possible to verify integrity of downloaded updates, install anyway?");
			if(!(JOptionPane.YES_OPTION == choice)) {
				esit = false;
			} else {
				esit = true;
			}
		}
		
		return esit;
	}
	
	private static boolean checkHash(File toCheck, PropertiesManagerFork propsManifest) throws Throwable {
		if(propsManifest == null || toCheck == null || !toCheck.exists()) {
			return false;
		} else {
			String checksum = propsManifest.getProperty(toCheck.getName());
			if (checksum != null && !"".equals(checksum.trim())) {
				// If checksum defined then check current file for it
				return DownloadUtils.checkFileHash(toCheck, checksum, downloadsHashFunc);
			} else {
				// If reference checksum not defined continue
				return true;
			}
		}
	}
	
	private static void signalErrorAndExit1(Throwable e) {
		System.out.println("An error occurred: " + Arrays.deepToString(e.getStackTrace()));
		try {
			Thread.sleep(10000);
			System.exit(1);
		} catch (InterruptedException e1) {
		}
	}

	private static void signalOKAndExit0() {
		System.out.println("Finished! Updater will close after 5 seconds from now...");
		try {
			Thread.sleep(5000);
			System.exit(0);
		} catch (InterruptedException e1) {
		}
	}
}

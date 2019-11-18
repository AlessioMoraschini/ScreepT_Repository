package main;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.swing.JOptionPane;

import org.ini4j.Wini;

import gui.UpdateProgressPanelMin;
import resources.GeneralConfig;
import utils.Various;

public class MainStandAloneUpdater {

	/**
	 * This is main application updater module, runnable as main class of updater.jar
	 * @param args -> args[0]= mainApplication jar ; args[1]=  updates extracted folder
	 */
	public static void main(String[] args) {
		
		final File updaterJar = new File(args[0]).getAbsoluteFile();
		final File updatesFolder = new File(args[1]).getAbsoluteFile();
		final File newJar = new File(args[2]).getAbsoluteFile();
		
		final String mainApplicationLauncher = "ScrYpT_Launcher.jar";
		
		// main external folder -> application -> Resources_Scrypt
		File mainDirectory = new File(GeneralConfig.RESOURCES_DIR).getAbsoluteFile().getParentFile().getParentFile();
		UpdateProgressPanelMin updateProgressMonitor = null;
		try {
			Wini iniLauncherConfig = new Wini(new File(GeneralConfig.DEFAULT_LAUNCHER_INI_PATH));
			String oldJarName = iniLauncherConfig.get("APPLICATION_OPTIONS", "jarToLaunch");
			File oldJar = (oldJarName != null) ? new File(oldJarName) : null;
			
			if(updaterJar.exists() && updaterJar.isFile() && updatesFolder.exists() && updatesFolder.isDirectory()){
				if(JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(null, "ScrYpT will be updated, do you want to backup current installation before to update? (update process will not touch your personal files, but if you prefer you can backup all scrypt folder before to update)"))
				{
					File destBackupDir = Various.directoRead(null, "Select backup destination, or press cancel to continue update procedure without backup", new File(GeneralConfig.USER_DESKTOP));
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
			Runtime runtime = Runtime.getRuntime();
			runtime.exec("java -jar " + mainApplicationLauncher);
			
			System.exit(0);
			
		} catch (IOException e) {
			if (updateProgressMonitor != null) {
				updateProgressMonitor.update(100, "Completed! Restarting ScrYpT...", true);
				updateProgressMonitor.forceClose();
			}
			JOptionPane.showMessageDialog(null, "Cannot update, an error occurred: " + e.getClass());
			System.out.println(e.getClass() + Arrays.deepToString(e.getStackTrace()));
		}
	}
	
}

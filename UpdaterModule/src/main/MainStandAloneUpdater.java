package main;

import java.io.File;
import java.io.IOException;

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
		
		final File updaterJar = new File(args[0]);
		final File updatesFolder = new File(args[1]);
		final File newJar = new File(args[2]);
		
		final String mainApplicationLauncher = "ScrYpT_Launcher.jar";
		
		// main external folder -> application -> Resources_Scrypt
		File mainDirectory = new File(GeneralConfig.RESOURCES_DIR).getParentFile().getParentFile();
		
		try {
			Wini iniLauncherConfig = new Wini(new File(GeneralConfig.DEFAULT_LAUNCHER_INI_PATH));
			String oldJarName = iniLauncherConfig.get("APPLICATION_OPTIONS", "jarToLaunch");
			File oldJar = (oldJarName != null) ? new File(oldJarName) : null;
			
			UpdateProgressPanelMin updateProgressMonitor = new UpdateProgressPanelMin(true); 
			updateProgressMonitor.setUndefinedDuration();
			
			if(updaterJar.exists() && updaterJar.isFile() && updatesFolder.exists() && updatesFolder.isDirectory()){
				if(JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(null, "ScrYpT will be updated, do you want to backup current installation before to update? (update process will not touch your personal files, but if you prefer you can backup all scrypt folder before to update)"))
				{
					File destBackupDir = Various.directoRead(null, "Select backup destination, or press cancel to continue update procedure without backup", new File(GeneralConfig.USER_DESKTOP));
					if (destBackupDir != null) {
						destBackupDir = Various.renameJavaObjFile(destBackupDir);
						Various.copyDirectoryContents(mainDirectory, destBackupDir);
					} 
				}
				
				// replace existing files matching with updates (don't touch user personal files)
				Various.copyDirectoryContents(updatesFolder, new File(GeneralConfig.RESOURCES_DIR).getParentFile());
				
				// update main application reference in .ini settings for launcher
				iniLauncherConfig.put("APPLICATION_OPTIONS", "jarToLaunch", newJar.getName());
				iniLauncherConfig.store();
				
				if (oldJar != null && oldJar.exists()) {
					oldJar.deleteOnExit();
				}
			} else {
				JOptionPane.showMessageDialog(null, "Updates data or updater jar not found, exiting updated procedure.", "Updates not found", JOptionPane.ERROR_MESSAGE);
			}
			
			// LAUNCH ScrYpT_Launcher.jar
			Runtime runtime = Runtime.getRuntime();
			runtime.exec("java -jar " + mainApplicationLauncher);
			
			System.exit(0);
			
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Cannot update, an error occurred :(");
		}
	}
	
}

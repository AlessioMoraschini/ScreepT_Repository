package maingUI;

import java.awt.Window;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JFrame;

import org.apache.log4j.Level;

import dialogutils.JOptionHelperExtended;
import files.CustomFileFilters;
import files.FileVarious;
import files.FileWorker;
import files.ZipWorker;
import files.watcher.ChangeDTO;
import gUI.panels.CrypterPanel;
import gUI.panels.TextEditor;
import general.utils.ScreepTLoggerUtils;
import guiVersion.HwInfoFrame;
import guiVersion.NetInfoFrame;
import impl.INItializer;
import main.MainStandAloneUpdater;
import mainlauncher.ScreepTLauncher;
import om.OSinfo;
import om.TabTextEditorObject;
import plugins.PluginManager;
import resources.GeneralConfig;
import resources.SoundsConfigurator;
import resources.SoundsManagerExtended;
import tabbededitor.CustomTabbedEditor;
import utility.log.SafeLogger;
import utils.SysUtils;
import utils.panels.ClipboardPanel;
import utils.panels.arch.PanelType;

public class MainGuiManager {

	// MainGuiUtils logger
	static SafeLogger logger = new SafeLogger(MainGuiUtils.class);

	private static String nativeLauncherPath = new OSinfo().isWindows()
			? ScreepTLauncher.BATCH_LAUNCHER_NAME
			: ScreepTLauncher.BASH_LAUNCHER_NAME;

	MainGuiUtils mainGuiUtils;

	public MainGuiManager(MainGuiUtils mainGuiUtils) {
		this.mainGuiUtils = mainGuiUtils;
	}

	public static void restartApplication(boolean asAdmin) {

		String adminFlag = asAdmin ? " as administrator" : "";
		if(!MainGUI.dialogHelper.yesOrNo("Restart " + GeneralConfig.APPLICATION_SHORT_NAME + adminFlag + " ?")) {
			return;
		}

		try {
			File launcherNativeFile = new File(nativeLauncherPath);
			if (launcherNativeFile.exists()) {

				MainGuiUtils.logger.info("Restart requested: restarting application...");

				closeApplication(999);

				// If there are updates to install then restart has already been triggered
				if(MainGuiGeneralAppStarter.updatesManager.updatesReadyToInstall.get() && MainGUI.updatesDownloaded != null)
					System.exit(0);

				if(asAdmin) {
					String path = FileVarious.getCanonicalPathSafe(GeneralConfig.MAIN_APP_OS_DEPENDENT_LAUNCHER);
					SysUtils.launchExeAsAdmin(path, path, (String[])null);
				} else {
					SysUtils.launchBatchOrBash(nativeLauncherPath, false);
				}

				MainGuiUtils.logger.info("\n [RESTART REQUESTED - adminMode="+asAdmin+"] \n\n" + ScreepTLoggerUtils.getExitString(GeneralConfig.APPLICATION_NAME, true));

				System.exit(0);

			} else {
				String errorMsg = "Cannot restart " + GeneralConfig.APPLICATION_NAME + " : native launcher not found";
				MainGUI.dialogHelper.error(errorMsg);
				logger.error(errorMsg);
			}
		} catch (Exception e) {
			MainGuiUtils.logger.error("Cannot restart application: an error occurred", e);
		}
	}

	public void exportLogs() {
		logger.debug(" => User started logs exporting procedure.");
		boolean choice = true;
		if(MainGuiGeneralAppStarter.LOG_LEVEL_AT_START.equals(Level.OFF)) {
			String msg =  "Logger is disabled, you won't have any information from curent session. Export anyway? (To enable use ScreepT_launcher's customize options at start)";
			choice = MainGUI.dialogHelper.yesOrNo(msg, "Export logs even if disabled?");
		}

		if (choice) {

			// show input dialog to select the file(gives null in case of no file selected)
			File selected = MainGUI.fileLogWriteDialog(MainGUI.mainFrameScreept);
			if (selected != null) {
				logger.info("\n\n##################################################\n\n" + new Date().toString()
				 		+ "\n\nBuild N°: " + GeneralConfig.BUILD_INCREMENTAL_NUMBER + "\n\n"
						+ "Build HASH: " + GeneralConfig.BUILD_COMMIT_HASH + "\n\n"
						+ "\n\n" + GeneralConfig.APPLICATION_NAME + " EXPORT!\n\n"
						+ "##################################################\n\n");

				// Create unique temporary folder
				File tempFolder = FileVarious.uniqueJavaObjFile(new File(GeneralConfig.TEMP_DIRECTORY + "LOG_EXPORT_TEMP"));
				tempFolder.mkdir();

				// Copy logs into it
				File copiedMainLog = FileWorker.copyFileIntoDir(new File(GeneralConfig.LOG_PATH), tempFolder, true, GeneralConfig.TEMP_DIRECTORY);
				File copiedLauncherLog = FileWorker.copyFileIntoDir(new File(GeneralConfig.LOG_LAUNCHER_PATH), tempFolder, false, GeneralConfig.TEMP_DIRECTORY);
				for(File f : getUpdaterLogs())
					FileWorker.copyFileIntoDir(f, tempFolder, false, GeneralConfig.TEMP_DIRECTORY);

				// Create a zip of temp folder and put it into selected destination
				File zipped = ZipWorker.zipFolderToFile(tempFolder.getAbsolutePath(), selected, true, true);
				boolean copied = copiedMainLog != null && copiedLauncherLog != null;
				if (copied && zipped != null && zipped.exists()) {
					MainGUI.dialogHelper.info("File correctly saved in " + zipped, "Log File Exported :)");
					logger.info("File :" + GeneralConfig.LOG_PATH + " Exported, exportFile: " + zipped);
				} else {
					MainGUI.dialogHelper.error("File not saved [" + zipped +"], Some error occurred :(", (String)null);
					logger.error("File not saved, some error occurred :( ");
				}

				// Delete temporary folder
				FileWorker.deleteDirContentInclRoot(tempFolder);
			}
		}
	}

	public List<File> getUpdaterLogs() {
		File mainUpdaterLogs = new File(GeneralConfig.CURRENT_EXECUTION_DIR);
		File[] logs = mainUpdaterLogs.listFiles(CustomFileFilters.customTypeFilter(CustomFileFilters.FILE_ONLY, ".log", true));
		List<File> filtered = new ArrayList<>();
		if(logs != null) {
			for(File log : logs) {
				if(log.getName().contains(MainStandAloneUpdater.UPDATER_LOG_FILE_NAME))
					filtered.add(log);
			}
		}

		return filtered;
	}

	/**
	 * Close application doing a check and backup of all unsaved data.
	 * If exit code is 999 then all will be done without effectively exit the program, delegating the exit call to the invoker
	 * @param exitCode
	 */
	public static void closeApplication(int exitCode) {

		MainGuiUtils.logger.info("\n\n => Starting exit procedure...\n\n");

		INItializer currentConfiguration = MainGUI.currentConfiguration;
		JFrame mainFrameScreept = MainGUI.mainAPP.getMainFrame();

		// Remember splitPanel dividers position - TEXT Editor
		currentConfiguration.writeSingleValue(INItializer.TEXT_EDITOR_OPT, "dividerRightPanePosX", currentConfiguration.getTextEditorOpt().getDividerRightPanePosX());
		currentConfiguration.writeSingleValue(INItializer.TEXT_EDITOR_OPT, "dividerFileTreePosX", currentConfiguration.getTextEditorOpt().getDividerFileTreePosX());
		currentConfiguration.writeSingleValue(INItializer.TEXT_EDITOR_OPT, "dividerBottomPosRatio", String.valueOf(currentConfiguration.getTextEditorOpt().getDividerBottomPosRatio()));

//		DEFAULT_FILE_TREE_ANCESTOR_LEVEL
		GeneralConfig.resourcesFilePathsMappings.saveCommentedProperty("DEFAULT_FILE_TREE_ANCESTOR_LEVEL", String.valueOf(GeneralConfig.DEFAULT_FILE_TREE_ANCESTOR_LEVEL));

		// CHECK UNSAVED TEXT EDITOR FILES
		if(!MainGuiUtils.askUnsavedTextFiles()) {
			mainFrameScreept.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			mainFrameScreept.setAlwaysOnTop(false);
			return;
		}

		if(MainGuiManager.doIfTempDecryptedChanged(mainFrameScreept)) {
			mainFrameScreept.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			return;
		}

		// if controls are passed then start real application closing routine
		mainFrameScreept.setAlwaysOnTop(true);

		MainGuiManager.closeOpenBlockingFrames();

		MainGUI.now = new Date();

	    // if autosave is on save before quit
	    if(currentConfiguration.getFileOpt().isFlagAutosave()) {
			try {
				currentConfiguration.writeCurrentConfigIniFile();
				MainGuiUtils.logger.info(" => AUTOSAVE ON: file saved before exiting ["
						+ currentConfiguration.getIniFile().getFile().getAbsolutePath() + "]");
			} catch (Exception e) {
				MainGuiUtils.logger.error("Exception happened!", e);
			}
		}else {
			MainGuiUtils.logger.info("\n => Autosave OFF -> configuration saving skipped");
		}

	  	// SAVE CURRENTLY OPENED FILES reference only (in text-editor)
	  	MainGuiManager.backupOpenedFilesInConfig();

	  	MainGuiManager.runTempFilesErasers();

		MainGUI.mainAppRunning = false;

		MainGuiUtils.logger.info(ScreepTLoggerUtils.getExitString(GeneralConfig.APPLICATION_NAME, true));

		SoundsManagerExtended.playSound(SoundsConfigurator.SOUND_PLING, null);

		MainGuiGeneralAppStarter.deleteSignalFileForExternalJars();

		// Check and launch eventual pending updates
		MainGuiManager.checkUpdatesWaitingForInstall();

		boolean hasPendingPluginsToDelete = hasPendingPluginsToDelete();

		MainGuiUtils.logger.info("Exit procedure completed succesfully, now software will shut down");

		if(hasPendingPluginsToDelete) {
			MainGuiUtils.logger.info("Pending plugins to delete found, restarting from launcher...");
			restartFromLauncher();
		}

		if (hasPendingPluginsToDelete || exitCode != 999) {
			System.exit(exitCode);
		}
	}

	public static void killApplication(int exitCode) {
		MainGuiUtils.logger.warn("\n\n => Starting kill procedure...\n\n");

		JFrame mainFrameScreept = MainGUI.mainAPP.getMainFrame();

		if (mainFrameScreept != null) {
			// if controls are passed then start real application closing routine
			mainFrameScreept.setAlwaysOnTop(false);
		}

		MainGuiManager.closeOpenBlockingFrames();

		runTempFilesErasers();

		MainGUI.mainAppRunning = false;

		MainGuiUtils.logger.info(ScreepTLoggerUtils.getExitString(GeneralConfig.APPLICATION_NAME, true));

		SoundsManagerExtended.playSound(SoundsConfigurator.SOUND_PLING, null);

		MainGuiGeneralAppStarter.deleteSignalFileForExternalJars();

		MainGuiUtils.logger.info("Kill procedure completed succesfully, now software will shut down");

		System.exit(exitCode);
	}

	public static void runTempFilesErasers() {
	 	// START ERASING TEMP DECRYPTED IF THERE ARE SOME
	  	Thread tempEraser = new Thread(MainGuiUtils.tempFilesEraser());
	  	tempEraser.start();

	  	// START ERASING CREEPT TEMP DECRYPTED IF THERE ARE SOME
	  	Thread tempCreeptEraser = new Thread(MainGuiUtils.tempDecryptedEraser());
	  	tempCreeptEraser.start();

		String msg = "Deleting temp files and folders...";

		try {
			MainGuiUtils.logger.info(msg + "please wait!");
			tempEraser.join();
			tempCreeptEraser.join();
			MainGuiUtils.logger.info(msg + "COMPLETED :)");

		} catch (InterruptedException e) {
			MainGuiUtils.logger.error("Some error occurred deleting temporary Decrypted files");
		}
	}

	static void backupOpenedFilesInConfig() {

		TextEditor initTxtWriter = MainGUI.textEditorPanel;
		INItializer currentConfiguration = MainGUI.currentConfiguration;

		if (initTxtWriter != null) {
			CustomTabbedEditor currentTabEditor = initTxtWriter.getCurrentTabEditor();

			// save list of non null and existing files in tabs
			currentConfiguration.getTextEditorOpt().getOpenedFileList().clear();
			for (TabTextEditorObject currentObj : currentTabEditor.loadedList.getList()) {
				if (currentObj.loadedFile != null && currentObj.loadedFile.exists()) {
					currentConfiguration.getTextEditorOpt().getOpenedFileList()
							.add(currentObj.loadedFile.getAbsolutePath());
				}
			}
			currentConfiguration.writeSingleValue(INItializer.TEXT_EDITOR_OPT, "loadedFilesAtStart",
					currentConfiguration.listToString(currentConfiguration.getTextEditorOpt().getOpenedFileList()));

			initTxtWriter.updateOpenedFilesCaretPositionMap(true);

			// save current opened if it exists
			if(currentTabEditor.loadedTabObj != null
					&& currentTabEditor.loadedTabObj.loadedFile!= null
					&& currentConfiguration.getTextEditorOpt().openedFileList.contains(currentTabEditor.loadedTabObj.loadedFile.getAbsolutePath()))
			{
				currentConfiguration.writeSingleValue(INItializer.TEXT_EDITOR_OPT, "selectedFileAtStart", currentTabEditor.loadedTabObj.loadedFile.getAbsolutePath());
			}

			// update clipboard if exists
			if(initTxtWriter != null && initTxtWriter.textEditorUtils.getTypeOfRightPanel().equals(PanelType.CLIPBOARD_PANEL)) {
				// save only current cause others are already saved once user selected items
				((ClipboardPanel)initTxtWriter.rightModulePanel).saveCurrClipboardSilent();
			}
		}

		logger.info("\n => Opened files in textEditor added to configuration");
	}

	public static boolean doIfTempDecryptedChanged(JFrame parent) {
		logger.debug("Starting check on fileMonitor...");
		CrypterPanel panelCrypter = MainGUI.crypterPanel;
		boolean result = false;

		try {
			if (panelCrypter != null &&
				panelCrypter.fileMonitor != null &&
				panelCrypter.fileMonitor.rootMonitorDir.exists()) {

				ChangeDTO changesTempDecrypted = panelCrypter.fileMonitor.checkForEverithing();
				if (changesTempDecrypted.changedFiles.size() != 0) {
					if(MainGUI.dialogHelper.yesOrNo("There are Files changed after decrypt.. do you want to return to ScreepT view? "+changesTempDecrypted.changedFiles)) {
						result = false;
						MainGUI.mntmCreepter.doClick();
						// add unsaved as source for crypterPanel
						panelCrypter.panelVDragNDropPane.removeAll();
						for(File unsaved : panelCrypter.fileMonitor.rootMonitorDir.listFiles()) {
							panelCrypter.panelVDragNDropPane.addFileToTree(unsaved);
						}
					}else {
						result = false;
					}
				}
			}

		} catch (Exception e) {
			logger.error("Error getting changes on tempFolder decrypted", e);
		}
		return result;
	}

	public static void closeOpenBlockingFrames(){
		logger.info("\n => Closing blocking frames");

		if(MainGuiGeneralAppStarter.hwInfoAvailable && HwInfoFrame.singletonInstance != null) {
			HwInfoFrame.singletonInstance.dispose();
		}

		if(NetInfoFrame.singletonInstance != null) {
			NetInfoFrame.singletonInstance.dispose();
		}

		if(MainGUI.textEditorPanel != null && MainGUI.textEditorPanel.subHeaderPanel != null) {
			if(MainGUI.textEditorPanel.subHeaderPanel.propertiesFrame != null) {
				MainGUI.textEditorPanel.subHeaderPanel.propertiesFrame.dispose();
			}
		}

		logger.info("\n => Blocking frames closed");
	}

	public static void askForExit() {

		if(MainGUI.alreadyAskingForClose) {
			return;
		}

		MainGUI.alreadyAskingForClose = true;

		Window activeWindow = javax.swing.FocusManager.getCurrentManager().getActiveWindow();

		if (MainGUI.dialogHelper.showYNDialog("Really want to exit?", "Exit Program", JOptionHelperExtended.YES_OR_NO)) {

			MainGUI.mainAPP.getMainFrame().setAlwaysOnTop(true);
			closeApplication(0);

		}else {

			MainGUI.mainAPP.getMainFrame().setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			activeWindow.toFront();
			activeWindow.requestFocus();
		}

		MainGUI.alreadyAskingForClose = false;
	}

	public static boolean hasPendingPluginsToDelete() {
		ArrayList<String> strPluginFilesList = PluginManager.installedPluginProperties.getStringList(PluginManager.installedPluginPropsListSeparator, ";");
		return strPluginFilesList != null && !strPluginFilesList.isEmpty();
	}

	public static void restartFromLauncher() {
		try {
			String command = "java -Dsun.java2d.ddscale=false -Dsun.java2d.noddraw=true -Dsun.java2d.uiScale=1 -Dfile.encoding=utf8 -Xmx256m -Xms128m -jar ScreepT_Launcher.jar";
			MainGuiUtils.logger.info("Launching ScreepT Launcher jar application ...");
			Runtime.getRuntime().exec(command);
			MainGuiUtils.logger.info("ScreepT Launcher jar application launched!");
		} catch (Exception e) {
			MainGuiUtils.logger.error("Cannot start ScreepT Launcher jar application, an error occurred!", e);
		}
	}

	public static void checkUpdatesWaitingForInstall() {
		try {
			if (MainGuiGeneralAppStarter.updatesManager.updatesReadyToInstall.get()) {
				MainGuiUtils.logger.info("Updates detected, starting updater jar application...");
				if(MainGUI.updatesDownloaded != null) {
					String command = "java -jar " + MainGUI.updatesDownloaded[0].getName()
						+ " " + MainGUI.updatesDownloaded[0].getName()
						+ " " + MainGUI.updatesDownloaded[1].getParentFile().getName() + File.separator + MainGUI.updatesDownloaded[1].getName()
						+ " " + MainGUI.updatesDownloaded[2].getName();
					MainGuiUtils.logger.info("Launching java command to update with downloaded files: \n\n" + command + "\n\n");
					Runtime.getRuntime().exec(command);
					MainGuiUtils.logger.info("Updater jar application launched!");
				}else {
					MainGuiUtils.logger.error("MainGUI.updatesDownloaded is null!");
				}
			}
		} catch (Exception e) {
			MainGuiUtils.logger.error("Cannot start update procedure, an error occurred!", e);
		}
	}

}

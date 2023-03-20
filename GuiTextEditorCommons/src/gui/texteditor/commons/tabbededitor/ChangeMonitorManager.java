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
package gui.texteditor.commons.tabbededitor;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import gui.texteditor.commons.om.TabTextEditorObject;
import resources.GeneralConfig;
import various.common.light.files.FileVarious;
import various.common.light.files.watcher.ChangeDTO;
import various.common.light.files.watcher.FileChangeMonitor;
import various.common.light.gui.GuiUtils;

public class ChangeMonitorManager implements IChangeMonManager{

	// ChangeMonitorManager logger creation
	static Logger logger = Logger.getLogger(ChangeMonitorManager.class);

	CustomTabbedEditor mainEditorRef;
	public AtomicBoolean isAlreadyChecking = new AtomicBoolean(false);

	public AtomicBoolean finalized;


	public ChangeMonitorManager(CustomTabbedEditor mainEditorRef) {
		this.mainEditorRef = mainEditorRef;
		logger.debug("CustomTabbedEditor initialized");
		finalized = new AtomicBoolean();
		startTailCheckerThread();
	}

	/**
	 * Check changes asking to user for what to do in each case, according to configuration value
	 */
	@Override
	public void checkChanges() {
		try {
			if (!isAlreadyChecking.get()) {
				// FILE MONITOR CHECK IF ENABLED IN CONFIG
				checkAndAdviceIfCurrentChanged(false, true);
			}
		} catch (Exception e1) {
			logger.error("An error occurred when trying to check current file for changes: ", e1);
		}
	}

	/**
	 * Check changes asking to user for what to do in each case, according to configuration value
	 */
	@Override
	public Runnable getRunnableChanges() {
		return new Runnable() {
			@Override
			public void run() {
				if (!isAlreadyChecking.get()) {
					try {
						// FILE MONITOR CHECK IF ENABLED IN CONFIG
						checkAndAdviceIfCurrentChanged(false, true);
					} catch (Exception e1) {
						logger.error("An error occurred when trying to check current file for changes: ", e1);
					}
				}
			}
		};
	}

	public void removeAllPendingChanges() throws Exception {
		for (int i = 0; i < mainEditorRef.loadedList.getSize(); i++) {
			ChangeDTO change = mainEditorRef.loadedTabObj.fileMonitorManager.checkForFile(mainEditorRef.loadedList.getList().get(i).loadedFile);
			logger.debug("Pending change removed from file change monitor: " + change);
		}
	}


	/** useful when user watns to reset monitor just after he has made a change to a file
	 */
	@Override
	public void removePendingFiredChanges(File target) {
		ChangeDTO changes;
		TabTextEditorObject objTOClear = mainEditorRef.getTabObjByFile(target);
		try {
			logger.debug("Cleaning changes from tabbedPane history");
			changes = objTOClear.fileMonitorManager.checkForFile(objTOClear.loadedFile);
			// removes references of current file from change history
			changes = FileChangeMonitor.cleanDtoFromFile(changes, objTOClear.loadedFile);
			// now add all except them with all eventuals other changes different from given file
			logger.debug("Resetting changes from others files monitored (except cleaned)...");
			mainEditorRef.changesHistory.update(changes);

			logger.debug("Cleaned changes from tabbedPane history for file: " + objTOClear.loadedFile);

		} catch (Exception e) {
			logger.warn("There were some problems when trying to remove pending fired changes from file change monitor: " + objTOClear.loadedFile);
		}
	}

	public void startTailCheckerThread() {
		GuiUtils.launchRunnable(()->{
			while(!finalized.get()) {
				try {
					Thread.sleep(GeneralConfig.TAIL_CHECKER_REFRESH_PERIOD);
					if(mainEditorRef.loadedTabObj.getTailMode().get()) {
						checkChanges();
					}
				} catch (InterruptedException e) {
					logger.error("Can't wait end of cycle of tail checker", e);
				} catch (Exception e1) {
					logger.error(e1.getClass().getCanonicalName() + e1.getStackTrace()[0].getClassName() + ": Line -> " + e1.getStackTrace()[0].getLineNumber());
				}
			}
		}, true, false, true);
	}

	/**
	 * checks if current file is changed, in case of force check or autoMonitorCheck enabled (from configuration).
	 * Then, if ask-option is enabled it asks user what to do. (in case of removal it will be asked every time)
	 * [CLOSE|RELOAD|DO_NOTHING]
	 * @throws Exception
	 */
	@Override
	public void checkAndAdviceIfCurrentChanged(boolean forceCheck, boolean ask) throws Exception {

		// only if forced check or if auto check is enabled
		if(mainEditorRef.loadedTabObj.getTailMode().get() || forceCheck || mainEditorRef.tabEditorManager.isEnabledAutoFileMonitor()) {

			try {
				isAlreadyChecking.set(true);
				ChangeDTO changes = new ChangeDTO();
				if (mainEditorRef.loadedTabObj.loadedFile != null && mainEditorRef.loadedTabObj.fileMonitorManager != null) {
					// CHECK change for curent file
					changes = mainEditorRef.loadedTabObj.fileMonitorManager.checkForFile(mainEditorRef.loadedTabObj.loadedFile);
					mainEditorRef.changesHistory.update(changes);
				} else {
					if (mainEditorRef.loadedTabObj.fileMonitorManager == null) {
						logger.info("fileMonitorManager is NULL, nothing to check.");
					} else {
						logger.info("file is NULL, nothing to check.");
					}
					return;
				}
				logger.debug("\n\n ########################  EVENT SEARCH... ###############################\n\n");
				boolean isChanged = mainEditorRef.changesHistory.changedFiles.contains(mainEditorRef.loadedTabObj.loadedFile);
				boolean isDeleted = mainEditorRef.changesHistory.removedFiles.contains(mainEditorRef.loadedTabObj.loadedFile);
				boolean isCreated = mainEditorRef.changesHistory.addedFiles.contains(mainEditorRef.loadedTabObj.loadedFile);
				if (isDeleted && !isCreated) {
					logger.info("\n\n ########################  DELETE EVENT REGISTERED! ###############################\n\n");

					File removed = mainEditorRef.changesHistory.findRemovedFile(mainEditorRef.loadedTabObj.loadedFile);
					if (removed == null) {
						return;
					}

					boolean resp = false;
					ask = true;
					logger.debug("\n => FILE EXTERNALLY REMOVED: " + removed.getAbsolutePath());

					if (ask) {
						// continue or close tab?
						String msg = "It seems that you or a software in your machine has deleted current file, wanna keep this file in editor?";
						resp = mainEditorRef.dialogHelper.yesOrNo(msg, "File deleted by external");
					}

					if (!resp) {
						logger.debug("\n => User has chosen to close tab with file: " + removed.getAbsolutePath());
						mainEditorRef.closeCurrentTab();
					} else {
						mainEditorRef.loadedTabObj.unsaved = true;
						mainEditorRef.updateSavedLabelNTab(null);
					}

					cleanFromHistory(removed);

				} else if (isChanged || isCreated) {
					logger.info("\n\n ########################  NEW/MOD EVENT REGISTERED! ###############################\n\n");

					File changed = mainEditorRef.changesHistory.findChangedFile(mainEditorRef.loadedTabObj.loadedFile);
					if (changed == null) {
						changed = mainEditorRef.changesHistory.findAddeddFile(mainEditorRef.loadedTabObj.loadedFile);
						if (changed == null) {
							return;
						}
					}
					boolean resp = true;
					logger.debug("\n => FILE EXTERNALLY CHANGED: " + changed.getAbsolutePath());

					if (!mainEditorRef.loadedTabObj.getTailMode().get() && ask) {
						// continue or reload file?
						String msg = "It seems that you or a software in your machine has modified current file, wanna reload this file? (every change could be lost)";
						resp = mainEditorRef.dialogHelper.yesOrNo(msg, "File changed by external");
					}

					if (resp) {
						logger.debug("\n => User has chosen to reload: " + changed.getAbsolutePath());
						mainEditorRef.reloadCurrentTab(false);

						SwingUtilities.invokeLater(()->{
							if(mainEditorRef.loadedTabObj.getTailMode().get())
								mainEditorRef.loadedTabObj.textArea.setCaretPosition(
										mainEditorRef.loadedTabObj.textArea.getDocument().getLength()-1);
						});

					} else {
						mainEditorRef.loadedTabObj.unsaved = true;
						mainEditorRef.updateSavedLabelNTab(null);
					}

					cleanFromHistory(changed);
				}
			} catch (Exception e) {

			} finally {
				isAlreadyChecking.set(false);
			}
		}
	}

	/**
	 * CLEAN history from current changed file occurrences
	 */
	@Override
	public void cleanFromHistory(File target) {
		mainEditorRef.changesHistory.removedFiles.removeAll(Collections.singletonList(target));
		mainEditorRef.changesHistory.changedFiles.removeAll(Collections.singletonList(target));
		mainEditorRef.changesHistory.addedFiles.removeAll(Collections.singletonList(target));
	}

	/**
	 * Adds or initialize to a monitor with given file.
	 * If file is already present, nothing will be done
	 * @param file to add to monitor for changes
	 */
	@Override
	public void updateCurrMonitor(File file){
		if (file != null) {
			boolean nullManager = mainEditorRef.loadedTabObj.fileMonitorManager == null;
			boolean differentRoot = !nullManager && !FileVarious.isFileSonOfDir(file, mainEditorRef.loadedTabObj.fileMonitorManager.rootMonitorDir);
			boolean mustUpdateRoot = nullManager || differentRoot;

			if (mustUpdateRoot) {
				logger.debug(file + " [CREATING MONITOR]: added to change monitor list. [Reloading for different root: "+differentRoot+"]");
				ArrayList<File> init = new ArrayList<File>();
				init.add(file);
				mainEditorRef.loadedTabObj.fileMonitorManager = new FileChangeMonitor(file.getParentFile(), init);
			} else {
				if (!mainEditorRef.loadedTabObj.fileMonitorManager.monitoredFileList.contains(file)) {
					mainEditorRef.loadedTabObj.fileMonitorManager.addFileToMonitorList(file);
					logger.debug(file + " [OK]: added to change monitor list.");
				} else {
					logger.debug(file + " [skipped]: already present in change monitor list.");
				}
			}
		}else {
			logger.warn("[skipped]: file is NULL, it's not possible to add it to change monitor list.");
		}
	}

	@Override
	public boolean isAlreadyChecking() {
		return isAlreadyChecking.get();
	}

	@Override
	public boolean setAlreadyChecking(boolean alreadyCheking) {
		this.isAlreadyChecking.set(alreadyCheking);
		return isAlreadyChecking.get();
	}

	@Override
	public void finalize() throws Throwable {
		finalized.set(true);
		super.finalize();
	}

}

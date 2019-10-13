package tabbededitor;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import amfileutils.FileStuff;
import amfileutils.filechangewatcher.ChangeDTO;
import amfileutils.filechangewatcher.FileChangeMonitor;

public class ChangeMonitorManager implements IChangeMonManager{
	
	// ChangeMonitorManager logger creation
	static Logger logger = Logger.getLogger(ChangeMonitorManager.class);
	
	CustomTabbedEditor mainEditorRef;
	public AtomicBoolean isAlreadyChecking = new AtomicBoolean(false);
	
	
	public ChangeMonitorManager(CustomTabbedEditor mainEditorRef) {
		this.mainEditorRef = mainEditorRef;
	}

	/**
	 * Check changes asking to user for what to do in each case, according to configuration value
	 */
	public void checkChanges() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					// FILE MONITOR CHECK IF ENABLED IN CONFIG
					checkAndAdviceIfCurrentChanged(false, true);
				} catch (Exception e1) {
					logger.error("An error occurred when trying to check current file for changes: ", e1);
				}
			}
		});
	}

	/**
	 * Check changes asking to user for what to do in each case, according to configuration value
	 */
	public Runnable getRunnableChanges() {
		return new Runnable() {
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
	
	/**
	 * checks if current file is changed, in case of force check or autoMonitorCheck enabled (from configuration).
	 * Then, if ask-option is enabled it asks user what to do. (in case of removal it will be asked every time) 
	 * [CLOSE|RELOAD|DO_NOTHING]
	 * @throws Exception 
	 */
	public void checkAndAdviceIfCurrentChanged(boolean forceCheck, boolean ask) throws Exception {
		
		
		// only if forced check or if auto check is enabled
		if(!isAlreadyChecking.get() && (forceCheck || mainEditorRef.tabEditorManager.isEnabledAutoFileMonitor())) {
			
			isAlreadyChecking.set(true);
			
			ChangeDTO changes = new ChangeDTO();
			
			if(mainEditorRef.loadedTabObj.loadedFile != null && mainEditorRef.loadedTabObj.fileMonitorManager != null) {
				// CHECK change for curent file
				changes = mainEditorRef.loadedTabObj.fileMonitorManager.checkForFile(mainEditorRef.loadedTabObj.loadedFile);
				mainEditorRef.changesHistory.update(changes);
			}else {
				if (mainEditorRef.loadedTabObj.fileMonitorManager == null) {
					logger.info("fileMonitorManager is NULL, nothing to check.");
				}else {
					logger.info("file is NULL, nothing to check.");
				}
				return;
			}
			
			logger.debug("\n\n ########################  EVENT SEARCH... ###############################\n\n");
			boolean isChanged = mainEditorRef.changesHistory.changedFiles.contains(mainEditorRef.loadedTabObj.loadedFile);
			boolean isDeleted = false;/* mainEditorRef.changesHistory.removedFiles.contains(mainEditorRef.loadedTabObj.loadedFile);*/
			boolean isCreated = mainEditorRef.changesHistory.addedFiles.contains(mainEditorRef.loadedTabObj.loadedFile);

			if (isDeleted) {
				logger.debug("\n\n ########################  DELETE EVENT REGISTERED! ###############################\n\n");

				File removed = mainEditorRef.changesHistory.findRemovedFile(mainEditorRef.loadedTabObj.loadedFile);
				if(removed == null) {return;}
				
				boolean resp = false;
				ask = true;
				logger.debug("\n => FILE EXTERNALLY REMOVED: " + removed.getAbsolutePath());
				
				if (ask) {
					// continue or close tab?
					String msg = "It seems that you or a software in your machine has deleted current file, wanna keep this file in editor?";
					resp = mainEditorRef.dialogHelper.yesOrNo(msg, "File deleted by external");
				}
				
				if(!resp) {
					logger.debug("\n => User has chosen to close tab with file: " + removed.getAbsolutePath());
					mainEditorRef.closeCurrentTab();
				}else {
					mainEditorRef.loadedTabObj.unsaved = true;
					mainEditorRef.updateSavedLabelNTab(null);
				}
				
				cleanFromHistory(removed);
				
			}else if (isChanged || isCreated) {
				logger.debug("\n\n ########################  NEW/MOD EVENT REGISTERED! ###############################\n\n");

				File changed = mainEditorRef.changesHistory.findChangedFile(mainEditorRef.loadedTabObj.loadedFile);
				if (changed == null) {
					changed = mainEditorRef.changesHistory.findAddeddFile(mainEditorRef.loadedTabObj.loadedFile);
					if (changed == null) {
						return;
					} 
				}
				boolean resp = true;
				logger.debug("\n => FILE EXTERNALLY CHANGED: " + changed.getAbsolutePath());

				if (ask) {
					// continue or reload file?
					String msg = "It seems that you or a software in your machine has modified current file, wanna reload this file? (every change could be lost)";
					resp = mainEditorRef.dialogHelper.yesOrNo(msg, "File changed by external");
				}
				
				if(resp) {
					logger.debug("\n => User has chosen to reload: " + changed.getAbsolutePath());
					mainEditorRef.reloadCurrentTab(false);
					
				}else {
					mainEditorRef.loadedTabObj.unsaved = true;
					mainEditorRef.updateSavedLabelNTab(null);
				}
				
				cleanFromHistory(changed);
			}			
		}
		isAlreadyChecking.set(false);
	}
	
	/**
	 * CLEAN history from current changed file occurrences
	 */
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
	public void updateCurrMonitor(File file){
		if (file != null) {
			boolean mustUpdateRoot = mainEditorRef.loadedTabObj.fileMonitorManager == null || !FileStuff.isFileSonOfDir(file, mainEditorRef.loadedTabObj.fileMonitorManager.rootMonitorDir);
			if (mustUpdateRoot) {
				logger.debug(file + " [CREATING MONITOR]: added to change monitor list.");
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
	
	public boolean isAlreadyChecking() {
		return isAlreadyChecking.get();
	}

	@Override
	public boolean setAlreadyChecking(boolean alreadyCheking) {
		this.isAlreadyChecking.set(alreadyCheking);
		return isAlreadyChecking.get();
	}
}

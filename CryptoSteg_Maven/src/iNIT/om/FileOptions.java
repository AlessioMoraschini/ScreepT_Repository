package iNIT.om;

import java.io.File;

public class FileOptions {
	
	// DEFAULT VALUES (usfeul to keep valid object during iniLoad, in case of null read or exception)
	public final boolean DEFAULT_FLAG_AUTOSAVE = true;
	public final boolean DEFAULT_FLAG_OVERWRITE = false;
	public final int DEFAULT_AUTOSAVE_INTERVAL = 5;
	public final String DEFAULT_LAST_SRC_FOLDER_PATH = "";
	public final String DEFAULT_LAST_DEST_FOLDER_PATH = "";
	
	// FIELDS
	private boolean flagAutosave;
	private boolean flagOverwrite;
	public int autosaveInterval;
	private String lastSrcFolderPath;
	private String lastDstFolderPath;
	
	public FileOptions() {
		
	}
	
	public FileOptions(boolean autosave, boolean overwrite, String lastSrcPath, String lastDstPath, int autosaveInt) {
		flagAutosave = autosave;
		flagOverwrite = overwrite;
		lastSrcFolderPath = lastDstPath;
		lastDstFolderPath = lastDstPath;
		autosaveInterval = autosaveInt;
	}

	public boolean isFlagAutosave() {
		return flagAutosave;
	}

	public int getAutosaveInterval() {
		return autosaveInterval;
	}

	public void setAutosaveInterval(int autosaveInterval) {
		this.autosaveInterval = autosaveInterval;
	}

	public void setFlagAutosave(boolean flagAutosave) {
		this.flagAutosave = flagAutosave;
	}

	public boolean isFlagOverwrite() {
		return flagOverwrite;
	}

	public void setFlagOverwrite(boolean flagOverwrite) {
		this.flagOverwrite = flagOverwrite;
	}

	public String getLastSrcFolderPath() {
		return lastSrcFolderPath;
	}

	public void setLastSrcFolderPath(String lastSrcFolderPath) {
		this.lastSrcFolderPath = lastSrcFolderPath;
	}

	public String getLastDstFolderPath() {
		return lastDstFolderPath;
	}

	public void setLastDstFolderPath(String lastDstFolderPath) {
		this.lastDstFolderPath = lastDstFolderPath;
	}
	
	

}

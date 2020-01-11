package tabbededitor;

import java.io.File;

import om.ColumnTextArea;

public interface ITabsManager {

	public ColumnTextArea getCurrentTextArea();
	
	public void loadFiles(final File[] fileToLoad);
}

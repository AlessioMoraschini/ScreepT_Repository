package enums;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Vector;

import resources.GeneralConfig;

public enum Themes {DARK(GeneralConfig.RESOURCES_DIR + "themes/dark.xml"),
					DEFAULT(GeneralConfig.RESOURCES_DIR + "themes/default.xml"),
					DEFAULT_V2(GeneralConfig.RESOURCES_DIR + "themes/default-alt.xml"),
					ECLIPSE(GeneralConfig.RESOURCES_DIR + "themes/eclipse.xml"),
					IDEA(GeneralConfig.RESOURCES_DIR + "themes/idea.xml"),
					MONOKAI(GeneralConfig.RESOURCES_DIR + "themes/monokai.xml"),
					THEMEVS(GeneralConfig.RESOURCES_DIR + "themes/vs.xml"),
					NONE("");
	
	public String defaultPath;
	
	private Themes(String path) {
		this.defaultPath = path;
	}
	
	/**
	 * Retrieve all custom themes in a source directory (only .xml will be considered) and add them to a list with default ones
	 * 
	 * @param customThemesFolder
	 * @return
	 * @throws IOException 
	 */
	public Vector<String>getAllThemesPlusCustom(File customThemesFolder) throws IOException {
		
		Vector<String> allThemes = new Vector<String>();

		// FOLDER validation
		if(customThemesFolder == null || !customThemesFolder.exists()) {
			return allThemes;
		}
		
		FileFilter filter = new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				if(pathname.isFile() && pathname.getName().toLowerCase().endsWith(".xml")) {
					// XML files only
					return true;
				}else {
					// directories and other files
					return false;
				}
			}
		};
		
		// add all xml files from custom folder
		for(File current : customThemesFolder.listFiles(filter)){
			allThemes.add(current.getCanonicalPath());
		};
		
		// add all defaults
		for(Themes  curr : Themes.values()) {
			allThemes.add(curr.defaultPath);
		}
		
		return allThemes;
	}

}

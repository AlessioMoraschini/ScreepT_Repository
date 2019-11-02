package om.themes;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Vector;

import resources.GeneralConfig;

public enum Themes {DARK(GeneralConfig.THEMES_FOLDER_DEFAULT + "dark.xml"),
					DEFAULT(GeneralConfig.THEMES_FOLDER_DEFAULT + "default.xml"),
					DEFAULT_V2(GeneralConfig.THEMES_FOLDER_DEFAULT + "default-alt.xml"),
					ECLIPSE(GeneralConfig.THEMES_FOLDER_DEFAULT + "eclipse.xml"),
					IDEA(GeneralConfig.THEMES_FOLDER_DEFAULT + "idea.xml"),
					MONOKAI(GeneralConfig.THEMES_FOLDER_DEFAULT + "monokai.xml"),
					THEMEVS(GeneralConfig.THEMES_FOLDER_DEFAULT + "vs.xml"),
					NONE("NO_STYLE");
	
	public String defaultPath;
	
	private Themes(String path) {
		this.defaultPath = path;
	}
	
	public static Vector<String> getStringValuesPath() {
		Vector<String> vector = new Vector<String>();
		for(Themes i : Themes.values()) {
			vector.add(i.defaultPath);
		}
		return vector;
	}
	
	/**
	 * Retrieve all paths for custom themes in a source directory (only .xml will be considered) and add them to a list with default ones
	 * 
	 * @param customThemesFolder
	 * @return
	 * @throws IOException 
	 */
	public static Vector<String> getAllXmlThemesPlusCustom(File customThemesFolder) throws IOException {
		
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

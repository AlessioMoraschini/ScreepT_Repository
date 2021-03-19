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
package om.themes;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Vector;

import resources.GeneralConfig;

public enum Themes {DARK(GeneralConfig.THEMES_FOLDER_DEFAULT + "dark.xml", "dark.xml", "/org/fife/ui/rsyntaxtextarea/themes/dark.xml"),
					DEFAULT(GeneralConfig.THEMES_FOLDER_DEFAULT + "default.xml", "default.xml", "/org/fife/ui/rsyntaxtextarea/themes/default.xml"),
					DEFAULT_V2(GeneralConfig.THEMES_FOLDER_DEFAULT + "default-alt.xml", "default-alt.xml", "/org/fife/ui/rsyntaxtextarea/themes/default-alt.xml"),
					ECLIPSE(GeneralConfig.THEMES_FOLDER_DEFAULT + "eclipse.xml", "eclipse.xml", "/org/fife/ui/rsyntaxtextarea/themes/eclipse.xml"),
					IDEA(GeneralConfig.THEMES_FOLDER_DEFAULT + "idea.xml", "idea.xml", "/org/fife/ui/rsyntaxtextarea/themes/idea.xml"),
					DRUID(GeneralConfig.THEMES_FOLDER_DEFAULT + "druid.xml", "druid.xml", "/org/fife/ui/rsyntaxtextarea/themes/druid.xml"),
					MONOKAI(GeneralConfig.THEMES_FOLDER_DEFAULT + "monokai.xml", "monokai.xml", "/org/fife/ui/rsyntaxtextarea/themes/monokai.xml"),
					THEMEVS(GeneralConfig.THEMES_FOLDER_DEFAULT + "vs.xml", "vs.xml", "/org/fife/ui/rsyntaxtextarea/themes/vs.xml"),
					NONE("NO_STYLE", "NO_STYLE", "");
	
	public String defaultPath;
	public String defaultClassPath;
	public String fileName;
	
	private Themes(String path, String fileName, String classPath) {
		this.defaultPath = path;
		this.fileName = fileName;
		this.defaultClassPath = classPath;
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
	
	public static ThemesWrapper getThemesWrapperFromName(String themeName, Vector<ThemesWrapper> wrappers) {
		for(ThemesWrapper wrapper : wrappers) {
			if(wrapper.name.equals(themeName) || wrapper.name.contains(themeName) || themeName.contains(wrapper.name)) {
				return wrapper;
			}
		}
		
		if (wrappers != null && wrappers.size() > 0) {
			return wrappers.get(0);
		} else {
			return new ThemesWrapper(DARK.defaultPath);
		}
	}

}

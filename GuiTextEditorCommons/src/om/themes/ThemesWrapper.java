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
import java.io.IOException;
import java.util.Vector;

import resources.GeneralConfig;

public class ThemesWrapper {

	public static ThemesWrapper DEFAULT = new ThemesWrapper(Themes.DEFAULT);
			
	public String name;
	public String path;
	public Themes theme = Themes.DEFAULT;
	
	public ThemesWrapper(Themes theme) {
		this.path = theme.defaultPath;
		this.name = getNameFromPath(path);
		this.theme = theme;
	}

	public ThemesWrapper(String path) {
		this.path = path;
		this.name = getNameFromPath(path);
	}
	
	/**
	 * Get name from path - E.g. folder/folderB/foldec/file.xml => file.xml
	 */
	public static String getNameFromPath(String path) {
		File pathFile = new File(path);
		return pathFile.getName();
	}
	
	public static Vector<ThemesWrapper> getDefaultWrappers() {
		Vector<ThemesWrapper> vector = new Vector<ThemesWrapper>();
		for(String i : Themes.getStringValuesPath()) {
			vector.add(new ThemesWrapper(i));
		}
		
		return vector;
	}
	
	public static Vector<ThemesWrapper> getDefaultAndCustomWrappers() throws IOException {
		Vector<ThemesWrapper> vector = new Vector<ThemesWrapper>();
		for(String i : Themes.getAllXmlThemesPlusCustom(new File(GeneralConfig.THEMES_FOLDER_CUSTOM))) {
			vector.add(new ThemesWrapper(i));
		}
		
		return vector;
	}
	
	public static ThemesWrapper getFromListByPath(String targetPath, Vector<ThemesWrapper> wrappers) {
		for(ThemesWrapper i : wrappers) {
			if(i.path.equals(targetPath)) {
				return i;
			}
		}
		
		return new ThemesWrapper(Themes.NONE);
	}

	public static ThemesWrapper getFromListByName(String targetName, Vector<ThemesWrapper> wrappers) {
		for(ThemesWrapper i : wrappers) {
			if(i.name.equals(targetName)) {
				return i;
			}
		}
		
		return new ThemesWrapper(Themes.NONE);
	}
	
	@Override
	public String toString() {
		return name;
	}

}

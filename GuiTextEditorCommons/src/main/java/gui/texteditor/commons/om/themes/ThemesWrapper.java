/**
 *
 * =========================================================================================
 *  Copyright (C) 2019-2020
 *
 *  AM-Design-Development - (Alessio Moraschini)
 * =========================================================================================
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package gui.texteditor.commons.om.themes;

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

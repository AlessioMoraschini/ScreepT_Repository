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
package tabbededitor;

import java.io.File;

import om.ColumnTextArea;
import om.TabTextEditorObject;

public interface ITabsManager {

	public ColumnTextArea getCurrentTextArea();
	
	public void openColorPicker();
	
	public void loadFiles(final File[] fileToLoad);
	
	public void addToHystory(TabTextEditorObject loadedTabObj);
}

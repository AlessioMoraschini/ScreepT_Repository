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

import gui.texteditor.commons.om.ColumnTextArea;
import gui.texteditor.commons.om.TabTextEditorObject;

public interface ITabsManager {

	public ColumnTextArea getCurrentTextArea();
	
	public void openColorPicker();
	
	public void loadFiles(final File[] fileToLoad);
	
	public void addToHystory(TabTextEditorObject loadedTabObj);

	/** Adds to a menu the encoding actions of the tab */
	public default void fillEncodingMenu(javax.swing.JComponent menu, TabTextEditorObject tab) {
	}
}

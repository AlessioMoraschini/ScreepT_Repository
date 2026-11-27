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
package gui.texteditor.commons.tabbededitor;

import java.awt.event.MouseWheelListener;

import javax.swing.JTextArea;
import javax.swing.text.JTextComponent;

import gui.texteditor.commons.om.SupportedFileTextEditor;
import various.common.light.om.ICommonShortcuts;

public interface ITabbedEditorOwner {
	
	public void openColorPicker();
	
	public void forceUnrecognizedFileType();
	
	public void resyncAddedPanelLight();
	
	/**
	* Method to call after file load
	*/
	public void doAfterFileLoad();
	
	/**
	 * Called at once when a tab is selected (or its kind changes, e.g. a viewer is closed): the owner can enable or disable
	 * the functions the tab supports. Unlike {@link #doAfterTabRefresh()} it is not deferred.
	 */
	default void onTabSelected(gui.texteditor.commons.om.TabTextEditorObject tab) {
	}

	/** Opening and saving of encrypted text files as normal tabs; null if the application has none */
	default gui.texteditor.commons.encrypted.EncryptedFileSupport getEncryptedFileSupport() {
		return null;
	}

	/** All the tab editors of the window (a second panel can be open beside the first): null = only this one */
	default java.util.List<CustomTabbedEditor> getAllEditors() {
		return null;
	}

	/**
	 * A tab is being dragged and the pointer is over the content area (not the tab strip): the owner can show where the tab
	 * would land. Returns true if a drop here would be handled by the owner (then the editor shows no reorder mark).
	 */
	default boolean onTabDrag(CustomTabbedEditor source, int tabIndex, java.awt.Point screenPoint) {
		return false;
	}

	/** The tab was released over the content area: true if the owner moved it elsewhere */
	default boolean onTabDrop(CustomTabbedEditor source, int tabIndex, java.awt.Point screenPoint) {
		return false;
	}

	/** The drag of a tab ended (dropped, released or cancelled): the owner clears what it showed */
	default void onTabDragEnd() {
	}

	/** The last tab of the editor was closed (it stays as an empty tab): the owner may close the panel */
	default void onLastTabClosed(CustomTabbedEditor editor) {
	}

	/**
	 * Added method after refresh of tab (switching)
	 */
	public void doAfterTabRefresh();
	
	/**
	 * Optional added methods to do when a new style is applied to tabs (Ex. style another element according to)
	 */
	public void doAfterApplyingStyleToTabs();
	
	/**
	 * Method to call when having to ask user if he wants to save file before closing tab.
	 * 
	 * <b>Note that file save has to be done by implementation, this is only the interface and does not implies that</b>
	 * 
	 * @return true if user wants to remove, false in other case
	 */
	public boolean askBeforeRemove(int i);
	
	/**
	 * @return the element to write the name on. If null it will be created
	 */
	public JTextComponent getNewFileStringTarget();
	
	/**
	 * @return if you have any additional checks use them to return true to continue.
	 * 
	 * <b>(if none just return true)</b>
	 * 
	 * This is used when a change is detected on current document, if checks are true current tab will be marked as unsaved
	 */
	public boolean getAdditionalConditionBeforeUpdateOnDocumentEvent();
	
	/**
	 * Use this to do all you want to do after the document has changed (when user has done some action on the textarea, such as write delete, modify, ecc...)
	 * If you don't need this, leave the method body empty [ <b>public void doAdditionalStuffOnDucChange(){} </b>]
	 */
	public void doAdditionalStuffOnDocMouseReleased(JTextArea target);
	public void doAdditionalStuffOnDocMousePressed(JTextArea target);
	
	/**
	 * If this is true tabs will be refreshed using current settings when swtiching (useful to enable after initialization)
	 * @return
	 */
	public boolean isEnabledRefresh();

	/**
	 * @return when this returns true tabEditor will check for current file existance automatically when a change in current file is detected
	 */
	public boolean isEnabledAutoFileMonitor();
	
	/**
	 * @return use this to return the right type of file to sync (o choose always the same from SupportedFileTextEditor enum)
	 */
	public SupportedFileTextEditor getCurrentSelectedFileType();
	
	/**
	 * This method will be used (if != null) to apply mouse listener to mainTab panel
	 * 
	 */
	public MouseWheelListener getMouseWheelTabbedAreaListener();
	
	/**
	 * Implementation must return a shorctut manager
	 * @return
	 */
	public ICommonShortcuts getShortcutManager();
	
}

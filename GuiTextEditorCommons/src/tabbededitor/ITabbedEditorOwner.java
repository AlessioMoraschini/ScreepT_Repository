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

import java.awt.event.MouseWheelListener;

import javax.swing.JTextArea;
import javax.swing.text.JTextComponent;

import om.ICommonShortcuts;
import om.SupportedFileTextEditor;

public interface ITabbedEditorOwner {
	
	public void openColorPicker();
	
	public void forceUnrecognizedFileType();
	
	public void resyncAddedPanelLight();
	
	/**
	* Method to call after file load
	*/
	public void doAfterFileLoad();
	
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

package tabbededitor;

import java.awt.event.MouseWheelListener;

import javax.swing.text.JTextComponent;

import om.SupportedFileTextEditor;

public interface ITabbedEditorOwner {
	
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
	 * <b>Note that file save has to be done by you!</b>
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
	 * If this is true tabs will be refreshed using current settings when swtiching (useful to enable after initialization)
	 * @return
	 */
	public boolean isEnabledRefresh();
	
	/**
	 * @return use this to return the right type of file to sync (o choose always the same from SupportedFileTextEditor enum)
	 */
	public SupportedFileTextEditor getCurrentSelectedFileType();
	
	/**
	 * This method will be used (if != null) to apply mouse listener to mainTab panel
	 * 
	 */
	public MouseWheelListener getMouseWheelTabbedAreaListener();

}

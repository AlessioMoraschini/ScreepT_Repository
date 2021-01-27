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
package gUI.utils.texteditor;

import java.awt.event.MouseWheelListener;

import javax.swing.JTextArea;
import javax.swing.text.JTextComponent;

import org.apache.log4j.Logger;

import gUI.panels.TextEditor;
import impl.om.TextEditorOption;
import maingUI.MainGUI;
import om.ICommonShortcuts;
import om.SupportedFileTextEditor;
import om.TabTextEditorObj;
import tabbededitor.ITabbedEditorOwner;
import utils.panels.MapTextAreaPanel;
import utils.panels.arch.PanelType;

public class TabbedEditorOwnerScreepT implements ITabbedEditorOwner{
	
	// TabbedEditorOwnerScreepT logger creation
	static Logger logger = Logger.getLogger(TabbedEditorOwnerScreepT.class);

	public TextEditor textEditor;
	public TextEditorUtils txtUtils;
	
	public TabbedEditorOwnerScreepT(TextEditor textEditorReference) {
		textEditor = textEditorReference;
		txtUtils = textEditor.textEditorUtils;
	}
	
	@Override
	public void doAfterFileLoad() {
//		txtUtils.applySelectedTheme();
		txtUtils.refreshMessageAndTabTooltip("[File loaded]");
	}

	@Override
	public void doAfterApplyingStyleToTabs() {
		txtUtils.resetDividerColor();
	}

	@Override
	public JTextComponent getNewFileStringTarget() {
		if(textEditor != null && textEditor.footerPanel != null && textEditor.footerPanel.lblCurrentFile != null) {
			return textEditor.footerPanel.lblCurrentFile;
		}else {
			logger.warn("\n\n => Cannot getStringTarget JText component cause something in (textEditor.footerPanel.lblCurrentFile) is null: \n\n");
			return new JTextComponent() {private static final long serialVersionUID = 4758186686168361742L;};
		}
	}

	@Override
	public void doAfterTabRefresh() {
		// create lazy refs for common used objects during current check
		TextEditorOption txtOpt = textEditor.currentConfiguration.getTextEditorOpt();
		TabTextEditorObj loadedTabObj = textEditor.getCurrentTabEditor().loadedTabObj;
		
		if (!textEditor.isPreloading) {
			
			// Most heavy operation, reloads current textarea based on current settings
			txtUtils.applyCurrentGraphicsTextArea(loadedTabObj.textArea, false);
			
			// Update load target textArea into search/replace/gotoline dialogs eventually opened
			txtUtils.refreshTxtAreaReference();
			
			// Resize container to fit well into actual size
			txtUtils.resizeFrameToDefault(textEditor.mainApplication);
			
			// Refresh tree if needed based on current options selected
			if (txtOpt.fileTreeActive) {
				txtUtils.refreshTree(textEditor.getCurrentTabEditor().loadedTabObj.loadedFile, false);
			}
			
			// Update eventually opened file properties frames
			if (textEditor.subHeaderPanel.propertiesFrame != null) {
				if (loadedTabObj != null && loadedTabObj.loadedFile != null && loadedTabObj.loadedFile.exists()) {
					textEditor.subHeaderPanel.propertiesFrame.refreshValues(loadedTabObj.loadedFile);
				}
			}
			
			// Sync map if it's active
			if (txtOpt.mapViewActive && txtUtils != null && txtUtils.getTypeOfRightPanel().equals(PanelType.MAP_TXTAREA)) {
//				txtUtils.getTypedIRightPanel();
				((MapTextAreaPanel) textEditor.rightSplitContainer).sync();
			}
			
			// Refresh current style from combo only if different from current actually applied to loaded text area tab
			txtUtils.findRightSyntaxFromComboNSelect(loadedTabObj.loadedFile, textEditor.currentConfiguration.getTextEditorOpt().defaultAutoSyntaxSync);
			
			txtUtils.applySelectedTheme(false);
		}
		
		
		try {
			// Update last opened files to have current on top of last opened
			textEditor.currentConfiguration.getTextEditorOpt().addFileToTopLimitedList(textEditor.getCurrentTabEditor().loadedTabObj.loadedFile, textEditor.currentConfiguration.getTextEditorOpt().lastOpenedFiles, "lastOpenedFiles");
		} catch (Exception e) {
			logger.warn("Cannot select current file between last opened ones");
		}
	}

	/**
	 * If no or OK continue, if cancel turn back
	 * @param index
	 * @return
	 */
	@Override
	public boolean askBeforeRemove(int i) {
		return textEditor.askSave(i);
	}

	@Override
	public boolean getAdditionalConditionBeforeUpdateOnDocumentEvent() {
		return !textEditor.headerPanel.isCrypterOn;
	}

	@Override
	public SupportedFileTextEditor getCurrentSelectedFileType() {
		SupportedFileTextEditor currentRef = (SupportedFileTextEditor) textEditor.subHeaderPanel.comboFileFormat.getSelectedItem();
		currentRef = (currentRef != null)? currentRef : SupportedFileTextEditor.TXT;
		return currentRef;
	}

	@Override
	public MouseWheelListener getMouseWheelTabbedAreaListener() {
		return new TextEditorMouseWheelListener(textEditor);
	}

	@Override
	public boolean isEnabledRefresh() {
		return !textEditor.isPreloading;
	}

	@Override
	public boolean isEnabledAutoFileMonitor() {
		return textEditor.currentConfiguration.getGenOpt().isTxtEditorFileMonitorActive();
	}

	@Override
	public void doAdditionalStuffOnDocMouseReleased(JTextArea target) {
		try {
			txtUtils.syncMiniTextAreaContent();
		} catch (Exception e) {
			logger.error("Error trying to sync text area minimap caret", e);
		}
	}

	@Override
	public void doAdditionalStuffOnDocMousePressed(JTextArea target) {
		// FOR NOW DO NOTHING
	}

	@Override
	public ICommonShortcuts getShortcutManager() {
		return textEditor.shortcutsListener;
	}

	@Override
	public void resyncAddedPanelLight() {
		if (txtUtils.getTypeOfRightPanel().equals(PanelType.MAP_TXTAREA) && textEditor.currentConfiguration.getTextEditorOpt().mapViewActive) {
			((MapTextAreaPanel) textEditor.rightSplitContainer).syncLight();
		} 
	}

	@Override
	public void forceUnrecognizedFileType() {
		textEditor.subHeaderPanel.comboFileFormat.setSelectedItem(SupportedFileTextEditor.TXT);
//		textEditor.subHeaderPanel.comboStylesScheme.setSelectedIndex(textEditor.subHeaderPanel.comboStylesScheme.getItemCount()-1);
	}

	@Override
	public void openColorPicker() {
		MainGUI.mainAPP.getMntmColorPicker().doClick();
	}

}

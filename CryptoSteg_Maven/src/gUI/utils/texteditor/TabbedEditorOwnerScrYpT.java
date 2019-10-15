package gUI.utils.texteditor;

import java.awt.event.MouseWheelListener;

import javax.swing.JTextArea;
import javax.swing.text.JTextComponent;

import org.apache.log4j.Logger;

import gUI.panels.TextEditor;
import gUI.utils.shortcutshandler.TextEditorMouseWheelListener;
import om.SupportedFileTextEditor;
import om.TabTextEditorObj;
import om.TextEditorOption;
import tabbededitor.ITabbedEditorOwner;
import utils.panels.MapTextAreaPanel;
import utils.panels.arch.PanelType;

public class TabbedEditorOwnerScrYpT implements ITabbedEditorOwner{
	
	// TabbedEditorOwnerScrYpT logger creation
	static Logger logger = Logger.getLogger(TabbedEditorOwnerScrYpT.class);

	public TextEditor textEditor;
	public TextEditorUtils txtUtils;
	
	public TabbedEditorOwnerScrYpT(TextEditor textEditorReference) {
		textEditor = textEditorReference;
		txtUtils = textEditor.textEditorUtils;
	}
	
	@Override
	public void doAfterFileLoad() {
		txtUtils.applySelectedTheme();
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
			return new JTextComponent() {};
		}
	}

	@Override
	public void doAfterTabRefresh() {
		if (!textEditor.isPreloading) {
			TextEditorOption txtOpt = textEditor.currentConfiguration.getTextEditorOpt();
			TabTextEditorObj loadedTabObj = textEditor.getCurrentTabEditor().loadedTabObj;
			txtUtils.applyCurrentGraphicsTextArea(loadedTabObj.textArea);
			// update load target textArea
			txtUtils.refreshTxtAreaReference();
			txtUtils.resizeFrameToDefault(textEditor.mainApplication);
			txtUtils.refreshTree(textEditor.getCurrentTabEditor().loadedTabObj.loadedFile, false);
			// update eventually opened info frames
			if (textEditor.propertiesFrame != null) {
				if (loadedTabObj != null && loadedTabObj.loadedFile != null && loadedTabObj.loadedFile.exists()) {
					textEditor.propertiesFrame.refreshValues(loadedTabObj.loadedFile);
				}
			}
			if (txtOpt.mapViewActive && txtUtils != null && txtUtils.getTypeOfRightPanel().equals(PanelType.MAP_TXTAREA)) {
				// syncronyze map if active
				txtUtils.getTypedIRightPanel();
				((MapTextAreaPanel) textEditor.rightSplitContainer).sync();
			}
			txtUtils.findRightStyleFromComboNSelect(loadedTabObj.loadedFile, !textEditor.chckbxLockLanguage.isSelected());
		}
	}

	@Override
	public boolean askBeforeRemove(int i) {
		return textEditor.askSave(i);
	}

	@Override
	public boolean getAdditionalConditionBeforeUpdateOnDocumentEvent() {
		return !textEditor.isCrypterOn;
	}

	@Override
	public SupportedFileTextEditor getCurrentSelectedFileType() {
		SupportedFileTextEditor currentRef = (SupportedFileTextEditor) textEditor.comboFileFormat.getSelectedItem();
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
	public void doAdditionalStuffOnDocMouseClick(JTextArea target) {
		if(txtUtils.getTypeOfRightPanel().equals(PanelType.MAP_TXTAREA)) {
			((MapTextAreaPanel)textEditor.rightSplitContainer).safeSyncCaretFromTxtReference();
		}
	}

}

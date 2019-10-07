package gUI.utils.texteditor;

import java.awt.event.MouseWheelListener;

import javax.swing.JLabel;
import javax.swing.text.JTextComponent;

import org.apache.log4j.Logger;

import gUI.panels.TextEditor;
import gUI.utils.shortcutshandler.TextEditorMouseWheelListener;
import om.SupportedFileTextEditor;
import om.TabTextEditorObj;
import tabbededitor.CustomTabbedEditor;
import tabbededitor.ITabbedEditorOwner;

public class TabbedEditorOwnerScrYpT implements ITabbedEditorOwner{
	
	// TabbedEditorOwnerScrYpT logger creation
	static Logger logger = Logger.getLogger(TabbedEditorOwnerScrYpT.class);

	public TextEditor textEditor;
	
	public TabbedEditorOwnerScrYpT(TextEditor textEditorReference) {
		textEditor = textEditorReference;
	}
	
	@Override
	public void doAfterFileLoad() {
		textEditor.textEditorUtils.applySelectedTheme();
		textEditor.textEditorUtils.refreshMessageAndTabTooltip("[File loaded]");
	}

	@Override
	public void doAfterApplyingStyleToTabs() {
		textEditor.textEditorUtils.resetDividerColor();
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
		
		TabTextEditorObj loadedTabObj = textEditor.getCurrentTabEditor().loadedTabObj;
		
		textEditor.textEditorUtils.applyCurrentGraphicsTextArea(loadedTabObj.textArea);

		// update load target textArea
		textEditor.textEditorUtils.refreshTxtAreaReference();
		textEditor.textEditorUtils.resizeFrameToDefault(textEditor.mainApplication);

		textEditor.textEditorUtils.refreshTree(textEditor.getCurrentTabEditor().loadedTabObj.loadedFile, false);

		// update eventually opened info frames
		if (textEditor.propertiesFrame != null) {
			if (loadedTabObj != null && loadedTabObj.loadedFile != null && loadedTabObj.loadedFile.exists()) {
				textEditor.propertiesFrame.refreshValues(loadedTabObj.loadedFile);
			}
		}
		
		textEditor.textEditorUtils.findRightStyleFromComboNSelect(loadedTabObj.loadedFile, !textEditor.chckbxLockLanguage.isSelected());
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
}

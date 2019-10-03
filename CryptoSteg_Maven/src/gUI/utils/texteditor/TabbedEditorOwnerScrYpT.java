package gUI.utils.texteditor;

import java.awt.event.MouseWheelListener;

import javax.swing.text.JTextComponent;

import gUI.panels.TextEditor;
import gUI.utils.shortcutshandler.TextEditorMouseWheelListener;
import om.SupportedFileTextEditor;
import om.TabTextEditorObj;
import tabbededitor.ITabbedEditorOwner;

public class TabbedEditorOwnerScrYpT implements ITabbedEditorOwner{

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
		TextEditorUtils.resetDividerColor();
	}

	@Override
	public JTextComponent getNewFileStringTarget() {
		return TextEditor.lblCurrentFile;
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

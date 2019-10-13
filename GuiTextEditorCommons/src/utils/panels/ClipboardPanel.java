package utils.panels;

import javax.swing.JPanel;

import org.apache.log4j.Logger;

import utils.dialog.ITextAreaDialogs;

public class ClipboardPanel  extends ParentPanel{

	// ClipboardPanel logger creation
	static Logger logger = Logger.getLogger(ClipboardPanel.class);
	
	private ITextAreaDialogs dialogManager;
	
	public ClipboardPanel(ITextAreaDialogs dialogManager) {
		this.dialogManager = dialogManager;
	}

	public ITextAreaDialogs getTxtEditor() {
		return dialogManager;
	}

	@Override
	public void sync() {
	}
}

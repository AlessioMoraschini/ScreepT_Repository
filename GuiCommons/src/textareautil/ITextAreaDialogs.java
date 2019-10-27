package textareautil;

import javax.swing.JTextArea;

public interface ITextAreaDialogs {

	public JTextArea getTargetTextArea();
	public void loadThemeIntoTarget(JTextArea txtArea);
	public String getCurrentClipboard();
}

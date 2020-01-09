package om;

import javax.swing.JTextArea;

import utility.string.StringWorker;

public class SelectionDtoFull {

	public String prefix = "";
	public String text = "";
	public String selection = "";
	public String end = "";
	public int first = 0;
	public int last = 0;
	public int length = 0;
	public boolean selected;
	public JTextArea textArea;
	
	public SelectionDtoFull(JTextArea textArea) {
		this.textArea = textArea;
		first = textArea.getSelectionStart();
		last = textArea.getSelectionEnd();
		text = textArea.getText();
		length = text.length();
		selection = StringWorker.nullToEmpty(textArea.getSelectedText());
		end = text.substring(last, text.length());
		prefix = text.substring(0, first);
		if(first == last){
			selected = false;
		}else {
			selected = true;
		}
	}
	
	public String getCompleteString() {
		return text.concat(selection).concat(end);
	}
}

package textareautil;

import javax.swing.JTextArea;

public class SelectionDto {

	public String prefix = "";
	public String selection = "";
	public String end = "";
	public int first = 0;
	public int last = 0;
	public int length = 0;
	public boolean selected;
	
	public SelectionDto(JTextArea textArea) {
		first = textArea.getSelectionStart();
		last = textArea.getSelectionEnd();
		prefix = textArea.getText();
		length = prefix.length();
		selection = prefix.substring(first, last);
		end = prefix.substring(last, prefix.length());
		prefix = prefix.substring(0, first);
		if(first == last){
			selected = false;
		}else {
			selected = true;
		}
	}
	
	public String getCompleteString() {
		return prefix.concat(selection).concat(end);
	}
}

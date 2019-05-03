package iNIT.om;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;

public class TextEditorOption {

	// DEFAULT VALUES (usfeul to keep valid object during iniLoad, in case of null read or exception)
	public static final int DEFAULT_MARGIN = 18;
	public static final int DEFAULT_VOICE_RATE = 3;
	public static final Font DEFAULT_FONT = new Font("Segoe UI", Font.PLAIN, 16);
	public static final Color DEFAULT_BACKGROUND_COL = new Color(250,250,250);
	public static final Color DEFAULT_FOREGROUND_COL = Color.BLACK;
	public static final String DEFAULT_SELECTED_FILE_AT_START = "";
	
	// FIELDS	
	public int margin;
	public int voiceRate;
	public Font font;
	public Color backCol;
	public Color foreCol;
	public ArrayList<String> openedFileList;
	public String selectedAtStart;
	
	// CONSTRUCTOR
	public TextEditorOption() {
		openedFileList = new ArrayList<String>();
	}

	// GETTERS AND SETTERS
	
	public ArrayList<String> getOpenedFileList() {
		return openedFileList;
	}
	
	public void setOpenedFileList(ArrayList<String> openedList) {
		this.openedFileList = openedList;
	}
	
	public String getSelectedAtStart() {
		return selectedAtStart;
	}

	public void setSelectedAtStart(String selectedAtStart) {
		this.selectedAtStart = selectedAtStart;
	}

	public int getMargin() {
		return margin;
	}

	public void setMargin(int margin) {
		this.margin = margin;
	}

	public Font getFont() {
		return font;
	}

	public void setFont(Font font) {
		this.font = font;
	}

	public Color getBackCol() {
		return backCol;
	}

	public void setBackCol(Color backCol) {
		this.backCol = backCol;
	}

	public Color getForeCol() {
		return foreCol;
	}

	public void setForeCol(Color foreCol) {
		this.foreCol = foreCol;
	}

	public int getVoiceRate() {
		return voiceRate;
	}

	public void setVoiceRate(int voiceRate) {
		this.voiceRate = voiceRate;
	}
	
}

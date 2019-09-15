package om;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;

import enums.Themes;


public class TextEditorOption {

	// DEFAULT VALUES (usfeul to keep valid object during iniLoad, in case of null read or exception)
	public static final int DEFAULT_MARGIN = 18;
	public static final int DEFAULT_VOICE_RATE = 3;
	public static final boolean DEFAULT_TAB_INLINE = true;
	public static final Font DEFAULT_FONT = new Font("Segoe UI", Font.PLAIN, 16);
	public static final Color DEFAULT_BACKGROUND_COL = new Color(250,250,250);
	public static final Color DEFAULT_FILE_PANEL_BACKGROUND_COL = new Color(41, 49, 52);
	public static final Color DEFAULT_FOREGROUND_COL = Color.BLACK;
	public static final String DEFAULT_SELECTED_FILE_AT_START = "";
	public static final Themes DEFAULT_SELECTED_THEME = Themes.DARK;
	public static final boolean DEFAULT_FTREE_ACTIVE = true;
	public static final int DEFAULT_DIVIDER_POS_X = 230;
	public static final int DEFAULT_TXT_AREA_DETAIL = 2;
	public static final int DEFAULT_TAB_SIZE = 2;
	public static final boolean DEFAULT_TAB_LINE_VISIBLE = false;
	public static final boolean DEFAULT_ANTIALIASED_TXT_AREA = false;
	public static final boolean DEFAULT_BREAK_LINE = false;
	public static final boolean DEFAULT_TREE_LOCKED = false;
	public static final String DEFAULT_WORKSPACE_LOCATION = System.getProperty("user.home");
	
	// FIELDS	
	public int margin;
	public int voiceRate;
	public boolean inlinedTabs;
	public Font font;
	public Color filePanelBackCol;
	public Color backCol;
	public Color foreCol;
	public ArrayList<String> openedFileList;
	public String selectedAtStart;
	public String defaultWorkspacePath;
	public Themes selectedTheme;
	public boolean fileTreeActive;
	public int dividerFileTreePosX;
	public int tabSize = 2;
	public boolean tabLineVisible;
	
	public boolean breaklineActive;
	public boolean antialiasedTxtArea;
	public boolean treeLocked;
	public int txtAreaDetailLevel;
	
	
	// CONSTRUCTOR
	public TextEditorOption() {
		openedFileList = new ArrayList<String>();
	}

	
	// GETTERS AND SETTERS
	
	public boolean isTabLineVisible() {
		return tabLineVisible;
	}
	
	public boolean isTreeLocked() {
		return treeLocked;
	}

	public void setTreeLocked(boolean treeLocked) {
		this.treeLocked = treeLocked;
	}


	public void setTabLineVisible(boolean tabLineVisible) {
		this.tabLineVisible = tabLineVisible;
	}
	
	public ArrayList<String> getOpenedFileList() {
		return openedFileList;
	}
	
	public boolean isBreaklineActive() {
		return breaklineActive;
	}

	public int getTabSize() {
		return tabSize;
	}

	public void setTabSize(int tabSize) {
		this.tabSize = tabSize;
	}

	public void setBreaklineActive(boolean breaklineActive) {
		this.breaklineActive = breaklineActive;
	}

	public boolean isAntialiasedTxtArea() {
		return antialiasedTxtArea;
	}

	public void setAntialiasedTxtArea(boolean antialiasedTxtArea) {
		this.antialiasedTxtArea = antialiasedTxtArea;
	}

	public int getTxtAreaDetailLevel() {
		return txtAreaDetailLevel;
	}

	public void setTxtAreaDetailLevel(int txtAreaDetailLevel) {
		this.txtAreaDetailLevel = txtAreaDetailLevel;
	}

	public boolean isFileTreeActive() {
		return fileTreeActive;
	}

	public void setFileTreeActive(boolean fileTreeActive) {
		this.fileTreeActive = fileTreeActive;
	}

	public int getDividerFileTreePosX() {
		return dividerFileTreePosX;
	}

	public void setDividerFileTreePosX(int dividerFileTreePosX) {
		this.dividerFileTreePosX = dividerFileTreePosX;
	}

	public void setOpenedFileList(ArrayList<String> openedList) {
		this.openedFileList = openedList;
	}
	
	public void setInlinedTabs(boolean inlinedTabs) {
		this.inlinedTabs = inlinedTabs;
	}
	
	public boolean isInlinedTabs() {
		return inlinedTabs;
	}
	
	public Themes getSelectedTheme() {
		return selectedTheme;
	}

	public void setSelectedTheme(Themes selectedTheme) {
		this.selectedTheme = selectedTheme;
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
	
	public Color getFilePanelBackCol() {
		return filePanelBackCol;
	}

	public void setFilePanelBackCol(Color filePanelBackCol) {
		this.filePanelBackCol = filePanelBackCol;
	}

	public int getVoiceRate() {
		return voiceRate;
	}

	public void setVoiceRate(int voiceRate) {
		this.voiceRate = voiceRate;
	}


	public String getDefaultWorkspacePath() {
		return defaultWorkspacePath;
	}


	public void setDefaultWorkspacePath(String defaultWorkspacePath) {
		this.defaultWorkspacePath = defaultWorkspacePath;
	}
	
	
	
}

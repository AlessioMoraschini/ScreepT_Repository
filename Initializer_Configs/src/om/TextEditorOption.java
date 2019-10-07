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
	
	//TEXT AREA
	public static final Themes DEFAULT_SELECTED_THEME = Themes.DARK;
	public static final boolean DEFAULT_FTREE_ACTIVE = true;
	public static final int DEFAULT_DIVIDER_POS_X = 230;
	public static final int DEFAULT_TAB_SIZE = 2;
	public static final boolean DEFAULT_TAB_LINE_VISIBLE = false;
	public static final boolean DEFAULT_BREAK_LINE = false;
	public static final boolean DEFAULT_TREE_LOCKED = false;
	public static final boolean DEFAULT_WORKSPACE_VIEW_SELECTED = false;
	public static final boolean DFAULT_AUTO_SYNTAX_SYN_FLAG = true;
	public static final String DEFAULT_WORKSPACE_LOCATION = System.getProperty("user.home");
	
	// TEXT AREA DETAIL
	public static final boolean DEF_codeFoldingEnabled = true;
	public static final boolean DEF_autoIndentEnabled = true;
	public static final boolean DEF_markOccurrences = true;
	public static final boolean DEF_closeMarkupTags = true;
	public static final boolean DEF_closeCurlyBraces = true;
	public static final boolean DEF_antialiasedTxtArea = true;
	public static final boolean DEF_bracketMatchingEnabled = true;
	public static final boolean DEF_animateBracketMatching = true;
	public static final boolean DEF_fadeCurrentLineHighlight = true;
	public static final boolean DEF_useFocusableTips = true;
	public static final boolean DEF_roundedSelectionEdges = true;
	public static final boolean DEF_paintMarkOccurrencesBorder = true;
	public static final boolean DEF_clearWhitespaceLinesEnabled = true;
	public static final boolean DEF_highlightSecondaryLanguages = true;
	public static final boolean DEF_fractionalFontMetricsEnabled = true;
	public static final boolean DEF_hyperLinksEnabled = false;
	public static final boolean DEF_tabsSpacesEmulated = false;
	public static final boolean DEF_markEndOfLine = false;
	public static final boolean DEF_showBracketsPopup = false;
	
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
	
	// TEXT AREA
	public String defaultWorkspacePath;
	public Themes selectedTheme;
	public int dividerFileTreePosX;
	public int tabSize = 2;
	public boolean tabLineVisible;
	public boolean fileTreeActive;
	public boolean treeLocked;
	public boolean workspaceViewSelected;
	public boolean defaultAutoSyntaxSync;
	public boolean breaklineActive;
	
	public boolean animateBracketMatching;
	public boolean antialiasedTxtArea;
	public boolean autoIndentEnabled;
	public boolean bracketMatchingEnabled;
	public boolean clearWhitespaceLinesEnabled;
	public boolean closeCurlyBraces;
	public boolean closeMarkupTags;
	public boolean codeFoldingEnabled;
	public boolean fadeCurrentLineHighlight;
	public boolean fractionalFontMetricsEnabled;
	public boolean highlightSecondaryLanguages;
	public boolean hyperLinksEnabled;
	public boolean markOccurrences;
	public boolean paintMarkOccurrencesBorder;
	public boolean roundedSelectionEdges;
	public boolean useFocusableTips;
	public boolean tabSpacesEmulated;
	public boolean markEndOfLine;
	public boolean showBracketsPopup;
	
	
	// CONSTRUCTOR
	public TextEditorOption() {
		openedFileList = new ArrayList<String>();
		margin = DEFAULT_MARGIN;
		voiceRate = DEFAULT_VOICE_RATE;
		inlinedTabs = DEFAULT_TAB_INLINE;
		font = DEFAULT_FONT;
		filePanelBackCol = DEFAULT_FILE_PANEL_BACKGROUND_COL;
		backCol = DEFAULT_BACKGROUND_COL;
		foreCol = DEFAULT_FOREGROUND_COL;
		selectedAtStart = DEFAULT_SELECTED_FILE_AT_START;
		defaultWorkspacePath = DEFAULT_WORKSPACE_LOCATION;
		selectedTheme = DEFAULT_SELECTED_THEME;
		dividerFileTreePosX = DEFAULT_DIVIDER_POS_X;
		tabSize = DEFAULT_TAB_SIZE;
		tabLineVisible = DEFAULT_TAB_LINE_VISIBLE;
		fileTreeActive = DEFAULT_FTREE_ACTIVE;
		treeLocked = DEFAULT_TREE_LOCKED;
		workspaceViewSelected = DEFAULT_WORKSPACE_VIEW_SELECTED;
		defaultAutoSyntaxSync = DFAULT_AUTO_SYNTAX_SYN_FLAG;
		
		breaklineActive = DEFAULT_BREAK_LINE;
		antialiasedTxtArea = DEF_antialiasedTxtArea;
		codeFoldingEnabled = DEF_codeFoldingEnabled;
		autoIndentEnabled = DEF_autoIndentEnabled;
		markOccurrences = DEF_markOccurrences;
		closeMarkupTags = DEF_closeMarkupTags;
		closeCurlyBraces = DEF_closeCurlyBraces;
		bracketMatchingEnabled = DEF_bracketMatchingEnabled;
		animateBracketMatching = DEF_animateBracketMatching;
		fadeCurrentLineHighlight = DEF_fadeCurrentLineHighlight;
		useFocusableTips = DEF_useFocusableTips;
		roundedSelectionEdges = DEF_roundedSelectionEdges;
		paintMarkOccurrencesBorder = DEF_paintMarkOccurrencesBorder;
		clearWhitespaceLinesEnabled = DEF_clearWhitespaceLinesEnabled;
		highlightSecondaryLanguages = DEF_highlightSecondaryLanguages;
		fractionalFontMetricsEnabled = DEF_fractionalFontMetricsEnabled;
		hyperLinksEnabled = DEF_fractionalFontMetricsEnabled;
		tabSpacesEmulated = DEF_tabsSpacesEmulated;
		markEndOfLine = DEF_markEndOfLine;
		showBracketsPopup = DEF_showBracketsPopup;
	}

	
	// GETTERS AND SETTERS
	
	public boolean isTabLineVisible() {
		return tabLineVisible;
	}
	
	public boolean isDefaultAutoSyntaxSync() {
		return defaultAutoSyntaxSync;
	}


	public void setDefaultAutoSyntaxSync(boolean defaultAutoSyntax) {
		this.defaultAutoSyntaxSync = defaultAutoSyntax;
	}


	public boolean isWorkspaceViewSelected() {
		return workspaceViewSelected;
	}

	public void setWorkspaceViewSelected(boolean workspaceViewSelected) {
		this.workspaceViewSelected = workspaceViewSelected;
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


	public boolean isCodeFoldingEnabled() {
		return codeFoldingEnabled;
	}


	public void setCodeFoldingEnabled(boolean codeFoldingEnabled) {
		this.codeFoldingEnabled = codeFoldingEnabled;
	}


	public boolean isAutoIndentEnabled() {
		return autoIndentEnabled;
	}


	public void setAutoIndentEnabled(boolean autoIndentEnabled) {
		this.autoIndentEnabled = autoIndentEnabled;
	}


	public boolean isMarkOccurrences() {
		return markOccurrences;
	}


	public void setMarkOccurrences(boolean markOccurrences) {
		this.markOccurrences = markOccurrences;
	}


	public boolean isCloseMarkupTags() {
		return closeMarkupTags;
	}


	public void setCloseMarkupTags(boolean closeMarkupTags) {
		this.closeMarkupTags = closeMarkupTags;
	}


	public boolean isCloseCurlyBraces() {
		return closeCurlyBraces;
	}


	public void setCloseCurlyBraces(boolean closeCurlyBraces) {
		this.closeCurlyBraces = closeCurlyBraces;
	}


	public boolean isBracketMatchingEnabled() {
		return bracketMatchingEnabled;
	}


	public void setBracketMatchingEnabled(boolean bracketMatchingEnabled) {
		this.bracketMatchingEnabled = bracketMatchingEnabled;
	}


	public boolean isAnimateBracketMatching() {
		return animateBracketMatching;
	}


	public void setAnimateBracketMatching(boolean animateBracketMatching) {
		this.animateBracketMatching = animateBracketMatching;
	}


	public boolean isFadeCurrentLineHighlight() {
		return fadeCurrentLineHighlight;
	}


	public void setFadeCurrentLineHighlight(boolean fadeCurrentLineHighlight) {
		this.fadeCurrentLineHighlight = fadeCurrentLineHighlight;
	}


	public boolean isUseFocusableTips() {
		return useFocusableTips;
	}


	public void setUseFocusableTips(boolean useFocusableTips) {
		this.useFocusableTips = useFocusableTips;
	}


	public boolean isRoundedSelectionEdges() {
		return roundedSelectionEdges;
	}


	public void setRoundedSelectionEdges(boolean roundedSelectionEdges) {
		this.roundedSelectionEdges = roundedSelectionEdges;
	}


	public boolean isPaintMarkOccurrencesBorder() {
		return paintMarkOccurrencesBorder;
	}


	public void setPaintMarkOccurrencesBorder(boolean paintMarkOccurrencesBorder) {
		this.paintMarkOccurrencesBorder = paintMarkOccurrencesBorder;
	}


	public boolean isClearWhitespaceLinesEnabled() {
		return clearWhitespaceLinesEnabled;
	}


	public void setClearWhitespaceLinesEnabled(boolean clearWhitespaceLinesEnabled) {
		this.clearWhitespaceLinesEnabled = clearWhitespaceLinesEnabled;
	}


	public boolean isHighlightSecondaryLanguages() {
		return highlightSecondaryLanguages;
	}


	public void setHighlightSecondaryLanguages(boolean highlightSecondaryLanguages) {
		this.highlightSecondaryLanguages = highlightSecondaryLanguages;
	}


	public boolean isFractionalFontMetricsEnabled() {
		return fractionalFontMetricsEnabled;
	}


	public void setFractionalFontMetricsEnabled(boolean fractionalFontMetricsEnabled) {
		this.fractionalFontMetricsEnabled = fractionalFontMetricsEnabled;
	}


	public boolean isHyperLinksEnabled() {
		return hyperLinksEnabled;
	}


	public void setHyperLinksEnabled(boolean hyperLinksEnabled) {
		this.hyperLinksEnabled = hyperLinksEnabled;
	}


	public boolean isTabSpacesEmulated() {
		return tabSpacesEmulated;
	}


	public void setTabSpacesEmulated(boolean tabSpacesEmulated) {
		this.tabSpacesEmulated = tabSpacesEmulated;
	}


	public boolean isMarkEndOfLine() {
		return markEndOfLine;
	}


	public void setMarkEndOfLine(boolean markEndOfLine) {
		this.markEndOfLine = markEndOfLine;
	}


	public boolean isShowBracketsPopup() {
		return showBracketsPopup;
	}


	public void setShowBracketsPopup(boolean showBracketsPopup) {
		this.showBracketsPopup = showBracketsPopup;
	}
	
}

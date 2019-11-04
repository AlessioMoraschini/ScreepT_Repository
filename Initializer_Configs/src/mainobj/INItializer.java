package mainobj;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.ini4j.Wini;

import amfileutils.om.FileNamed;
import enums.ScalingAlgorithm;
import om.AesOption;
import om.FileOptions;
import om.GenericOption;
import om.GuiOption;
import om.ImgConverterOption;
import om.SteganOption;
import om.TextEditorOption;
import resources.GeneralConfig;
import variousutils.HandleString;

public class INItializer extends INItializerParent implements INItializerInterface{

	// INItializer logger creation		
	static Logger logger = Logger.getLogger(INItializer.class); 
	
	// DEFAULT INI
	public final static String DEFAULT_INI_PATH = GeneralConfig.RESOURCES_DIR  + "CONFIG.ini"; 
	
	// options om
	private GenericOption genOpt;
	private FileOptions fileOpt;
	private SteganOption steganOpt;
	private AesOption aesOpt;
	private GuiOption guiOpt;
	private TextEditorOption textEditorOpt;
	private ImgConverterOption imageConvertOption;
	
	public final static String AES_OPT = "AES_OPTIONS";
	public final static String STEGA_OPT = "STEGANO_OPTIONS";
	public final static String FILE_OPT = "FILE_OPTIONS";
	public final static String GEN_OPT = "GENERIC_OPTIONS";
	public final static String GUI_OPT = "GUI_OPTIONS";
	public final static String TEXT_EDITOR_OPT = "TEXT_EDITOR";
	public final static String IMAGE_CONVERTER_OPT = "IMAGE_CONVERTER";
	
	public INItializer() {
		genOpt = new GenericOption();
		fileOpt = new FileOptions();
		steganOpt = new SteganOption();
		aesOpt = new AesOption();
		guiOpt = new GuiOption();
		textEditorOpt = new TextEditorOption(this);
		imageConvertOption = new ImgConverterOption();
		
		try {
			iniReadFile = new Wini(new File(DEFAULT_INI_PATH));
			iniFile = new Wini(new File(DEFAULT_INI_PATH));
		} catch (IOException e) {
			logger.error("INItializer() -> Exception happened initializing configuration!", e);
		}
	}
	
	public INItializer(Wini configFile) {
		// initialize both read and write ini variable, to keep them synchronyzed
		if(configFile!=null) {
			iniReadFile = configFile;
			iniFile = configFile;
		} else try {
			iniReadFile = new Wini(new File(DEFAULT_INI_PATH));
			iniFile = new Wini(new File(DEFAULT_INI_PATH));
		} catch (IOException e) {
			logger.error("INItializer(Wini configFile) -> Exception happened initializing configuration!", e);
		}
		genOpt = new GenericOption();
		fileOpt = new FileOptions();
		steganOpt = new SteganOption();
		aesOpt = new AesOption();
		guiOpt = new GuiOption();
		textEditorOpt = new TextEditorOption();
		imageConvertOption = new ImgConverterOption();
	}
	
	// READ & FILL FIELDS METHODS
	
	/**
	 * Method that fills the pojo object mapped from the file given as ini file with iniReadFile setter method, 
	 * or also with constructor "INItializer(Wini configFile)". if some value is null it sets default values.
	 */
	@Override
	public void readConfigIniFile() {
		
		// LOAD AES OPTIONS => disabled key for security
		aesOpt.setFlagAes(Boolean.parseBoolean(iniReadFile.get(AES_OPT, "flagCodificaAes")));
		
		loadFileOptions();
		loadGeneralOptions();
		loadGuiOptions();
		loadSteganoOptions();
		loadTxtEditorOptions();
		loadImageConverterOptions();
		
		logger.info("Loaded ini file: "+ this.toStringa());
	}
	
	
	// WRITE & FILL FIELDS METHODS
	
	/**
	 * Method that write on the iniFile (notice that it's different from iniReadFile which is user to read values from file)
	 * the values setted on pojo's of this class. if some value is null it writes default values of pojo's
	 */
	@Override
	public synchronized void writeCurrentConfigIniFile() {
		
		synchronized(iniFile) {
			// set INIFILE-AES OPTIONS
			iniFile.put(AES_OPT, "flagCodificaAes", aesOpt.isFlagAes());
			
			writeFileOptions();
			writeGeneralOptions();
			writeGuiOptions();
			writeImgConverterOptions();
			writeTxtEditOptions();
			writeSteganoOptions();
			
			logger.info("Configuration written to: "+iniFile.getFile().getAbsolutePath() + this.toStringa());
			
			// WRITE ALL VALUES TO FILE
			try {
				iniFile.store();
			} catch (IOException e) {
				logger.error("Exception happened!", e);
			}
		}
	}
	
	// READ PROPERTIES
	
	/**
	 * Load file properties from file into volatile memory
	 */
	public void loadFileOptions() {
		// LOAD FILE OPTIONS
		
		// boolean
		fileOpt.setFlagAutosave(Boolean.parseBoolean(iniReadFile.get(FILE_OPT, "flagAutosave")));
		fileOpt.setFlagOverwrite(Boolean.parseBoolean(iniReadFile.get(FILE_OPT, "flagOverwrite")));
		fileOpt.setLastDstFolderPath(iniReadFile.get(FILE_OPT, "lastDstFolder"));
		// String
		if(fileOpt.getLastDstFolderPath().equals("")) {
			fileOpt.setLastDstFolderPath(FileOptions.DEFAULT_LAST_DEST_FOLDER_PATH);
		}
		fileOpt.setLastSrcFolderPath(iniReadFile.get(FILE_OPT, "lastSrcFolder"));
		if(fileOpt.getLastSrcFolderPath().equals("")) {
			fileOpt.setLastSrcFolderPath(FileOptions.DEFAULT_LAST_SRC_FOLDER_PATH);
		}
		// integer
		try {
			// minimum value 1 minutes
			if(Integer.valueOf(iniReadFile.get(FILE_OPT, "autosaveInterval"))<FileOptions.DEFAULT_MIN_AUTOSAVE_INTERVAL) {
				fileOpt.setAutosaveInterval(FileOptions.DEFAULT_MIN_AUTOSAVE_INTERVAL);
			}else {
				fileOpt.setAutosaveInterval(Integer.valueOf(iniReadFile.get(FILE_OPT, "autosaveInterval")));
			}
			
		} catch (NumberFormatException e) {
			fileOpt.setAutosaveInterval(FileOptions.DEFAULT_AUTOSAVE_INTERVAL);
			logger.error("Exception happened!", e);
		}
	}
	
	/**
	 * Load GUI properties from file into volatile memory
	 */
	public void loadGuiOptions() {
		
		// LOAD GUI OPTIONS
		
		// int values
		try {
			guiOpt.setMenuFontSize(Integer.valueOf(iniReadFile.get(GUI_OPT, "menuFontSize")));
			guiOpt.setLogFontSize(Integer.valueOf(iniReadFile.get(GUI_OPT, "logFontSize")));
		} catch (NumberFormatException e) {
			guiOpt.setLogFontSize(GuiOption.DEFAULT_LOG_FONT_SIZE);
			guiOpt.setMenuFontSize(GuiOption.DEFAULT_MENU_FONT_SIZE);
		}
		try {
			guiOpt.setNimbusSize(Integer.valueOf(iniReadFile.get(GUI_OPT, "nimbusSize")));
		} catch (NumberFormatException e) {
			guiOpt.setNimbusSize(GuiOption.DEFAULT_NIMBUS_SIZE);
		}
		
		// string values
		guiOpt.setPreferredStyle(iniReadFile.get(GUI_OPT, "preferredStyle"));
		if(guiOpt.getPreferredStyle().equals("")||guiOpt.getPreferredStyle()==null) {
			guiOpt.setPreferredStyle(GuiOption.DEFAULT_GUI_STYLE);
		}
		// boolean
		guiOpt.setDarkGuiNimbus(Boolean.parseBoolean(iniReadFile.get(GUI_OPT, "darkGuiNimbus")));
				
	}
	
	/**
	 * Load Stegano crypter properties from file into volatile memory
	 */
	public void loadSteganoOptions() {
		// LOAD STEGANO OPTIONS
		steganOpt.setUltraSecure(Boolean.parseBoolean(iniReadFile.get(STEGA_OPT, "flagUltraSecure")));
		steganOpt.setFlagSteg(Boolean.parseBoolean(iniReadFile.get(STEGA_OPT, "flagCodificaSteg")));
		try {
			steganOpt.setDefaultWidth(Integer.valueOf(iniReadFile.get(STEGA_OPT, "defaultWidth")));
			steganOpt.setDefaultHeight(Integer.valueOf(iniReadFile.get(STEGA_OPT, "defaultHeight")));
		} catch (NumberFormatException e) {
			steganOpt.setDefaultHeight(SteganOption.DEFAULT_HEIGHT);
			steganOpt.setDefaultWidth(SteganOption.DEFAULT_WIDTH);
			logger.error("Exception happened!", e);
		}
		String pathsListString;
		try {
			pathsListString = iniReadFile.get(STEGA_OPT, "imgKeyRecentPath");
			StringTokenizer tokenizer = new StringTokenizer(pathsListString, ",");
			steganOpt.getImgRecentPath().clear();
			while(tokenizer.hasMoreTokens()) {
				steganOpt.getImgRecentPath().add(tokenizer.nextToken());
			}
		} catch (Exception e1) {
			pathsListString = SteganOption.DEFAULT_IMG_RECENT_PATH;
		}
		
	}
	
	/**
	 * Load Generic properties from file into volatile memory
	 */
	public void loadGeneralOptions() {
		// LOAD GENERAL OPTIONS
		genOpt.setUserName(iniReadFile.get(GEN_OPT, "userName"));
		if(genOpt.getUserName().equals("")) {
			genOpt.setUserName(GenericOption.DEFAULT_USERNAME);
		}
		genOpt.setCharset(iniReadFile.get(GEN_OPT, "charset"));
		if(genOpt.getCharset().equals("")) {
			genOpt.setCharset(GenericOption.DEFAULT_CHARSET);
		}
		
		try {
			genOpt.setLastPanelOpened(Class.forName(iniReadFile.get(GEN_OPT, "lastPanelOpened")));
		} catch (Exception e) {
			genOpt.setLastPanelOpened(GenericOption.DEFAULT_PANEL_CLASS_AT_START);
		}
		try {
			genOpt.setGeneralVolume(Float.parseFloat(iniReadFile.get(GEN_OPT, "generalVolume")));
		} catch (Exception e1) {
			genOpt.setGeneralVolume(GenericOption.DEFAULT_VOLUME);
		}
		
		genOpt.setExceptionAdvicesEnabled(getBooleanFromString(iniReadFile.get(GEN_OPT, "exceptionAdvicesEnabled"), GenericOption.DEF_exceptionAdvicesEnabled));
		
		genOpt.setTxtEditorFileMonitorActive(getBooleanFromString(iniReadFile.get(GEN_OPT, "txtEditorFileMonitorActive"), GenericOption.DEF_TxtEditorFileMonitorActive));
	
	}
	
	/**
	 * Load TextEditor properties from file into volatile memory
	 */
	public void loadTxtEditorOptions() {
		// TEXT EDITOR OPTIONS
		
		// INI read
		String fontStr = iniReadFile.get(TEXT_EDITOR_OPT, "font");
		String FilePanelbackColStr = iniReadFile.get(TEXT_EDITOR_OPT, "filePanelBackgroundCol");
		String backColStr = iniReadFile.get(TEXT_EDITOR_OPT, "backgroundCol");
		String foreColStr = iniReadFile.get(TEXT_EDITOR_OPT, "foregroundCol");
		String selectedAtStartStr = iniReadFile.get(TEXT_EDITOR_OPT, "selectedFileAtStart");
		String defaultWorspaceLocationStr = iniReadFile.get(TEXT_EDITOR_OPT, "defaultWorspaceLocation");
		String lastEscapeLanguageStr = iniReadFile.get(TEXT_EDITOR_OPT, "lastEscapeLanguage");

		// validation
		Font font = (!"".equals(fontStr) && fontStr!= null)? HandleString.getFontFromString(fontStr) : TextEditorOption.DEFAULT_FONT;
		Color backCol = (!"".equals(backColStr) && backColStr!= null)? HandleString.getRgbColorFromString(backColStr) : TextEditorOption.DEFAULT_BACKGROUND_COL;
		Color filePanelBackCol = (!"".equals(FilePanelbackColStr) && FilePanelbackColStr!= null)? HandleString.getRgbColorFromString(FilePanelbackColStr) : TextEditorOption.DEFAULT_FILE_PANEL_BACKGROUND_COL;
		Color foreCol = (!"".equals(foreColStr) && foreColStr!= null)? HandleString.getRgbColorFromString(foreColStr) : TextEditorOption.DEFAULT_FOREGROUND_COL;
		String selectedFileAtStart = (!"".equals(selectedAtStartStr) && selectedAtStartStr!= null)? selectedAtStartStr : TextEditorOption.DEFAULT_SELECTED_FILE_AT_START;
		String defaultWorspaceLocation = (!"".equals(defaultWorspaceLocationStr) && defaultWorspaceLocationStr!= null)? defaultWorspaceLocationStr : TextEditorOption.DEFAULT_WORKSPACE_LOCATION;
		String lastEscapeLanguage = (!"".equals(lastEscapeLanguageStr) && lastEscapeLanguageStr!= null)? lastEscapeLanguageStr : TextEditorOption.DEF_LastEscapeLanguage;
		
		int tabSize = 2;
		try {
			tabSize = Integer.valueOf(iniReadFile.get(TEXT_EDITOR_OPT, "tabSize"));
		} catch (Exception e) {
			logger.error("Exception happened!", e);
		}
		
		// update Object
		textEditorOpt.setFont(font);
		textEditorOpt.setTabSize(tabSize);
		textEditorOpt.setFilePanelBackCol(filePanelBackCol);
		textEditorOpt.setBackCol(backCol);
		textEditorOpt.setForeCol(foreCol);
		textEditorOpt.setSelectedAtStart(selectedFileAtStart);
		textEditorOpt.setInlinedTabs(Boolean.parseBoolean(iniReadFile.get(TEXT_EDITOR_OPT, "inlineTabs")));
		textEditorOpt.setDefaultWorkspacePath(defaultWorspaceLocation);
		textEditorOpt.setLastEscapeLanguage(lastEscapeLanguage);
		textEditorOpt.setLastFileOpenedClipboard(getSafeStringConf(TEXT_EDITOR_OPT, "lastFileOpenedClipboard", TextEditorOption.DEF_lastFileOpenedClipboard));
		textEditorOpt.setClipboardSyncStyle(Boolean.parseBoolean(iniReadFile.get(TEXT_EDITOR_OPT, "clipboardSyncStyle")));
		
		// CONVERTED/PARSED TYPES
		// margin
		try {
			textEditorOpt.setMargin(Integer.valueOf(iniReadFile.get(TEXT_EDITOR_OPT, "margin")));
		} catch (NumberFormatException e) {
			textEditorOpt.setMargin(TextEditorOption.DEFAULT_MARGIN);
		}
		// voice rate
		try {
			textEditorOpt.setVoiceRate(Integer.valueOf(iniReadFile.get(TEXT_EDITOR_OPT, "voiceRate")));
		} catch (NumberFormatException e) {
			textEditorOpt.setVoiceRate(TextEditorOption.DEFAULT_VOICE_RATE);
		}
		
		textEditorOpt.setDividerFileTreePosX(getIntVarFromConf(iniReadFile.get(TEXT_EDITOR_OPT, "dividerFileTreePosX"), TextEditorOption.DEFAULT_DIVIDER_POS_X));
		textEditorOpt.setDividerRightPanePosX(getIntVarFromConf(iniReadFile.get(TEXT_EDITOR_OPT, "dividerRightPanePosX"), TextEditorOption.DEF_dividerRightPanePosX));
		textEditorOpt.setDividerRightPanePosXClipboard(getIntVarFromConf(iniReadFile.get(TEXT_EDITOR_OPT, "dividerRightPanePosXClipboard"), TextEditorOption.DEF_dividerRightPanePosXClipboard));
		
		// automatic syntax selection file-extension-based
		textEditorOpt.setDefaultAutoSyntaxSync(Boolean.valueOf(iniReadFile.get(TEXT_EDITOR_OPT, "defaultAutoSyntaxSync")));
		
		// workspace view selected
		textEditorOpt.setWorkspaceViewSelected(Boolean.valueOf(iniReadFile.get(TEXT_EDITOR_OPT, "workspaceViewSelected")));
		
		// txtArea detail
		textEditorOpt.setTreeLocked(getBooleanFromString(iniReadFile.get(TEXT_EDITOR_OPT, "isTreeLocked"), TextEditorOption.DEFAULT_TREE_LOCKED));
		textEditorOpt.setTabLineVisible(getBooleanFromString(iniReadFile.get(TEXT_EDITOR_OPT, "tabLineVisibleFlag"), TextEditorOption.DEFAULT_TAB_LINE_VISIBLE));
		textEditorOpt.setFileTreeActive(getBooleanFromString(iniReadFile.get(TEXT_EDITOR_OPT, "fileTreeActive"), TextEditorOption.DEFAULT_FTREE_ACTIVE));
		textEditorOpt.setMapViewActive(getBooleanFromString(iniReadFile.get(TEXT_EDITOR_OPT, "mapViewActive"), TextEditorOption.DEF_mapViewActive));
		textEditorOpt.setClipboardViewActive(getBooleanFromString(iniReadFile.get(TEXT_EDITOR_OPT, "clipboardViewActive"), TextEditorOption.DEF_clipboardViewActive));
		textEditorOpt.setBreaklineActive(getBooleanFromString(iniReadFile.get(TEXT_EDITOR_OPT, "breakLineActive"), TextEditorOption.DEFAULT_BREAK_LINE));
		textEditorOpt.setAntialiasedTxtArea(getBooleanFromString(iniReadFile.get(TEXT_EDITOR_OPT, "antialiasedTxtArea"), TextEditorOption.DEF_antialiasedTxtArea));
		textEditorOpt.setCodeFoldingEnabled(getBooleanFromString(iniReadFile.get(TEXT_EDITOR_OPT, "codeFoldingEnabled"), TextEditorOption.DEF_codeFoldingEnabled));
		textEditorOpt.setAutoIndentEnabled(getBooleanFromString(iniReadFile.get(TEXT_EDITOR_OPT, "autoIndentEnabled"), TextEditorOption.DEF_autoIndentEnabled));
		textEditorOpt.setMarkOccurrences(getBooleanFromString(iniReadFile.get(TEXT_EDITOR_OPT, "markOccurrences"), TextEditorOption.DEF_markOccurrences));
		textEditorOpt.setCloseMarkupTags(getBooleanFromString(iniReadFile.get(TEXT_EDITOR_OPT, "closeMarkupTags"), TextEditorOption.DEF_closeMarkupTags));
		textEditorOpt.setCloseCurlyBraces(getBooleanFromString(iniReadFile.get(TEXT_EDITOR_OPT, "closeCurlyBraces"), TextEditorOption.DEF_closeCurlyBraces));
		textEditorOpt.setBracketMatchingEnabled(getBooleanFromString(iniReadFile.get(TEXT_EDITOR_OPT, "bracketMatchingEnabled"), TextEditorOption.DEF_bracketMatchingEnabled));
		textEditorOpt.setAnimateBracketMatching(getBooleanFromString(iniReadFile.get(TEXT_EDITOR_OPT, "animateBracketMatching"), TextEditorOption.DEF_animateBracketMatching));
		textEditorOpt.setFadeCurrentLineHighlight(getBooleanFromString(iniReadFile.get(TEXT_EDITOR_OPT, "fadeCurrentLineHighlight"), TextEditorOption.DEF_fadeCurrentLineHighlight));
		textEditorOpt.setUseFocusableTips(getBooleanFromString(iniReadFile.get(TEXT_EDITOR_OPT, "useFocusableTips"), TextEditorOption.DEF_useFocusableTips));
		textEditorOpt.setRoundedSelectionEdges(getBooleanFromString(iniReadFile.get(TEXT_EDITOR_OPT, "roundedSelectionEdges"), TextEditorOption.DEF_roundedSelectionEdges));
		textEditorOpt.setPaintMarkOccurrencesBorder(getBooleanFromString(iniReadFile.get(TEXT_EDITOR_OPT, "paintMarkOccurrencesBorder"), TextEditorOption.DEF_paintMarkOccurrencesBorder));
		textEditorOpt.setClearWhitespaceLinesEnabled(getBooleanFromString(iniReadFile.get(TEXT_EDITOR_OPT, "clearWhitespaceLinesEnabled"), TextEditorOption.DEF_clearWhitespaceLinesEnabled));
		textEditorOpt.setHighlightSecondaryLanguages(getBooleanFromString(iniReadFile.get(TEXT_EDITOR_OPT, "highlightSecondaryLanguages"), TextEditorOption.DEF_highlightSecondaryLanguages));
		textEditorOpt.setFractionalFontMetricsEnabled(getBooleanFromString(iniReadFile.get(TEXT_EDITOR_OPT, "fractionalFontMetricsEnabled"), TextEditorOption.DEF_fractionalFontMetricsEnabled));
		textEditorOpt.setHyperLinksEnabled(getBooleanFromString(iniReadFile.get(TEXT_EDITOR_OPT, "hyperLinksEnabled"), TextEditorOption.DEF_hyperLinksEnabled));
		textEditorOpt.setTabSpacesEmulated(getBooleanFromString(iniReadFile.get(TEXT_EDITOR_OPT, "tabSpacesEmulated"), TextEditorOption.DEF_tabsSpacesEmulated));
		textEditorOpt.setMarkEndOfLine(getBooleanFromString(iniReadFile.get(TEXT_EDITOR_OPT, "markEndOfLine"), TextEditorOption.DEF_markEndOfLine));
		textEditorOpt.setShowBracketsPopup(getBooleanFromString(iniReadFile.get(TEXT_EDITOR_OPT, "showBracketsPopup"), TextEditorOption.DEF_showBracketsPopup));
		textEditorOpt.setSpellChecker(getBooleanFromString(iniReadFile.get(TEXT_EDITOR_OPT, "spellChecker"), TextEditorOption.DEF_spellChecker));
		textEditorOpt.setPrefDictspellChecker(getSafeStringConf(TEXT_EDITOR_OPT, "prefDictspellChecker", TextEditorOption.DEF_prefDictspellChecker));
		
		// file list
		String pathsOpenFilesString;
		try {
			pathsOpenFilesString = iniReadFile.get(TEXT_EDITOR_OPT, "loadedFilesAtStart");
			StringTokenizer tokenizerOpenedFiles = new StringTokenizer(pathsOpenFilesString, ",");
			textEditorOpt.getOpenedFileList().clear();		
			while(tokenizerOpenedFiles.hasMoreTokens()) {
				textEditorOpt.getOpenedFileList().add(tokenizerOpenedFiles.nextToken());
			}
		} catch (Exception e1) {
			pathsOpenFilesString = "";
		}
		
		String lastOpenedFiles;
		try {
			lastOpenedFiles = iniReadFile.get(TEXT_EDITOR_OPT, "lastOpenedFiles");
			StringTokenizer tokenizerOpenedFiles = new StringTokenizer(lastOpenedFiles, ",");
			textEditorOpt.getLastOpenedFiles().getList().clear();		
			while(tokenizerOpenedFiles.hasMoreTokens()) {
				textEditorOpt.getLastOpenedFiles().addLast(new FileNamed(tokenizerOpenedFiles.nextToken()));
			}
		} catch (Exception e1) {
			lastOpenedFiles = "";
		}
		
		String lastSelectedWorkspaces;
		try {
			lastSelectedWorkspaces = iniReadFile.get(TEXT_EDITOR_OPT, "lastSelectedWorkspaces");
			StringTokenizer tokenizerSelectedWorkspaces = new StringTokenizer(lastSelectedWorkspaces, ",");
			textEditorOpt.lastSelectedWorkspaces.getList().clear();		
			while(tokenizerSelectedWorkspaces.hasMoreTokens()) {
				textEditorOpt.lastSelectedWorkspaces.addLast(new FileNamed(tokenizerSelectedWorkspaces.nextToken()));
			}
		} catch (Exception e1) {
			lastSelectedWorkspaces = "";
		}
		
		// selected theme
		String themeRead = iniReadFile.get(TEXT_EDITOR_OPT, "preferredTheme");
		String theme = (themeRead != null && !themeRead.equals(""))? themeRead : TextEditorOption.DEFAULT_SELECTED_THEME;
		textEditorOpt.setSelectedTheme(theme);
	}
	
	/**
	 * Load image converter module properties from file into volatile memory
	 */
	public void loadImageConverterOptions() {
		// IMAGE CONVERTER OPTIONS
		imageConvertOption.setSameSize(Boolean.parseBoolean(iniReadFile.get(IMAGE_CONVERTER_OPT, "sameSize")));
		imageConvertOption.setSameExtension(Boolean.parseBoolean(iniReadFile.get(IMAGE_CONVERTER_OPT, "sameExtension")));
		try {
			imageConvertOption.setQuality(Integer.valueOf(iniReadFile.get(IMAGE_CONVERTER_OPT, "quality")));
		} catch (NumberFormatException e) {
			imageConvertOption.setQuality(ImgConverterOption.DEFAULT_QUALITY);
		}
		String algo = iniReadFile.get(IMAGE_CONVERTER_OPT, "algorithm");
		algo = (ScalingAlgorithm.isValidString(algo))? algo : ImgConverterOption.DEFAULT_ALGORITHM.toString();
		imageConvertOption.setAlgorithm(ScalingAlgorithm.valueOf(algo));
	}
	
	// WRITE PROPERTIES
	
	public void writeFileOptions() {
		// set INIFILE-FILE OPTIONS
		
		// boolean
		iniFile.put(FILE_OPT, "flagAutosave", fileOpt.isFlagAutosave());
		iniFile.put(FILE_OPT, "flagOverwrite", fileOpt.isFlagOverwrite());
		// string values
		if(fileOpt.getLastDstFolderPath().equals("")) {
			fileOpt.setLastDstFolderPath(FileOptions.DEFAULT_LAST_DEST_FOLDER_PATH);
		}
		iniFile.put(FILE_OPT, "lastDstFolder", fileOpt.getLastDstFolderPath());
		if(fileOpt.getLastSrcFolderPath().equals("")) {
			fileOpt.setLastSrcFolderPath(FileOptions.DEFAULT_LAST_SRC_FOLDER_PATH);
		}
		iniFile.put(FILE_OPT, "lastSrcFolder", fileOpt.getLastSrcFolderPath());
		// integer values
		iniFile.put(FILE_OPT, "autosaveInterval", fileOpt.getAutosaveInterval());
		
	}
	
	public void writeGuiOptions() {
		// set INIFILE-GUI OPTIONS
		if(guiOpt.getPreferredStyle().equals("")) {
			guiOpt.setPreferredStyle(GuiOption.DEFAULT_GUI_STYLE);
		}
		iniFile.put(GUI_OPT, "preferredStyle", guiOpt.getPreferredStyle());
		iniFile.put(GUI_OPT, "menuFontSize", guiOpt.getMenuFontSize());
		iniFile.put(GUI_OPT, "logFontSize", guiOpt.getLogFontSize());
		iniFile.put(GUI_OPT, "darkGuiNimbus", guiOpt.isDarkGuiNimbus());
		iniFile.put(GUI_OPT, "nimbusSize", guiOpt.getNimbusSize());
	}
	
	public void writeSteganoOptions() {
		// set INIFILE-STEGANO OPTIONS
		iniFile.put(STEGA_OPT, "flagCodificaSteg", steganOpt.isFlagSteg());
		iniFile.put(STEGA_OPT, "flagUltraSecure", steganOpt.isUltraSecure());
		iniFile.put(STEGA_OPT, "defaultWidth", steganOpt.getDefaultWidth());
		iniFile.put(STEGA_OPT, "defaultHeight", steganOpt.getDefaultHeight());
		// creo variabili temporanee per il ricalcolo della stringa_lista
		String accumulator="";
		Vector<String> lista = steganOpt.getImgRecentPath();
		// concateno gli elementi della lista e, tranne l'ultimo, aggiungo una virgola
		for(String currentPath : lista) {
			accumulator=accumulator+currentPath;
			if(!(lista.get(lista.size()-1).equals(currentPath))) {
				accumulator=accumulator+",";
			}
		}
		if(accumulator.equals("")) {
			accumulator = SteganOption.DEFAULT_IMG_RECENT_PATH;
		}
		iniFile.put(STEGA_OPT, "imgKeyRecentPath", accumulator );
		
	}
	
	public void writeGeneralOptions() {
		// set INIFILE-GENERAL OPTIONS
		if(genOpt.getUserName().equals("")) {
			genOpt.setUserName(GenericOption.DEFAULT_USERNAME);
		}
		iniFile.put(GEN_OPT, "userName", genOpt.getUserName());
		iniFile.put(GEN_OPT, "charset", genOpt.getCharset());
		iniFile.put(GEN_OPT, "generalVolume", genOpt.getGeneralVolume());
		iniFile.put(GEN_OPT, "lastPanelOpened", genOpt.getLastPanelOpened().getName());
		iniFile.put(GEN_OPT, "txtEditorFileMonitorActive", genOpt.isTxtEditorFileMonitorActive());
		iniFile.put(GEN_OPT, "exceptionAdvicesEnabled", genOpt.isExceptionAdvicesEnabled());
	}
	
	public void writeImgConverterOptions() {
		// IMAGE CONVERTER OPTIONS
		iniFile.put(IMAGE_CONVERTER_OPT, "sameSize", imageConvertOption.sameSize);
		iniFile.put(IMAGE_CONVERTER_OPT, "sameExtension", imageConvertOption.sameExtension);
		iniFile.put(IMAGE_CONVERTER_OPT, "quality", imageConvertOption.quality);
		iniFile.put(IMAGE_CONVERTER_OPT, "algorithm", imageConvertOption.algorithm);
	}
	
	public void writeTxtEditOptions() {
		// TEXT EDITOR OPTIONS
		iniFile.put(TEXT_EDITOR_OPT, "inlineTabs", textEditorOpt.isInlinedTabs());
		iniFile.put(TEXT_EDITOR_OPT, "font", HandleString.getFontString(textEditorOpt.font));
		iniFile.put(TEXT_EDITOR_OPT, "filePanelBackgroundCol", HandleString.getRgbString(textEditorOpt.filePanelBackCol));
		iniFile.put(TEXT_EDITOR_OPT, "backgroundCol", HandleString.getRgbString(textEditorOpt.backCol));
		iniFile.put(TEXT_EDITOR_OPT, "foregroundCol", HandleString.getRgbString(textEditorOpt.foreCol));
		iniFile.put(TEXT_EDITOR_OPT, "margin", textEditorOpt.margin);
		iniFile.put(TEXT_EDITOR_OPT, "tabSize", textEditorOpt.tabSize);
		iniFile.put(TEXT_EDITOR_OPT, "tabLineVisibleFlag", textEditorOpt.tabLineVisible);
		iniFile.put(TEXT_EDITOR_OPT, "lastEscapeLanguage", textEditorOpt.lastEscapeLanguage);
		iniFile.put(TEXT_EDITOR_OPT, "voiceRate", textEditorOpt.voiceRate);
		iniFile.put(TEXT_EDITOR_OPT, "preferredTheme", textEditorOpt.selectedTheme);
		iniFile.put(TEXT_EDITOR_OPT, "fileTreeActive", textEditorOpt.fileTreeActive);
		iniFile.put(TEXT_EDITOR_OPT, "clipboardViewActive", textEditorOpt.clipboardViewActive);
		iniFile.put(TEXT_EDITOR_OPT, "mapViewActive", textEditorOpt.mapViewActive);
		iniFile.put(TEXT_EDITOR_OPT, "dividerFileTreePosX", textEditorOpt.dividerFileTreePosX);
		iniFile.put(TEXT_EDITOR_OPT, "dividerRightPanePosX", textEditorOpt.dividerRightPanePosX);
		iniFile.put(TEXT_EDITOR_OPT, "dividerRightPanePosXClipboard", textEditorOpt.dividerRightPanePosXClipboard);
		iniFile.put(TEXT_EDITOR_OPT, "lastFileOpenedClipboard", textEditorOpt.lastFileOpenedClipboard);
		iniFile.put(TEXT_EDITOR_OPT, "clipboardSyncStyle", textEditorOpt.clipboardSyncStyle);
		iniFile.put(TEXT_EDITOR_OPT, "antialiasedTxtArea", textEditorOpt.antialiasedTxtArea);
		iniFile.put(TEXT_EDITOR_OPT, "breakLineActive", textEditorOpt.breaklineActive);
		iniFile.put(TEXT_EDITOR_OPT, "isTreeLocked", textEditorOpt.treeLocked);
		iniFile.put(TEXT_EDITOR_OPT, "defaultWorspaceLocation", textEditorOpt.defaultWorkspacePath);
		iniFile.put(TEXT_EDITOR_OPT, "workspaceViewSelected", textEditorOpt.workspaceViewSelected);
		iniFile.put(TEXT_EDITOR_OPT, "defaultAutoSyntaxSync", textEditorOpt.defaultAutoSyntaxSync);
		
		// DETAIL
		iniFile.put(TEXT_EDITOR_OPT, "codeFoldingEnabled", textEditorOpt.defaultAutoSyntaxSync);
		iniFile.put(TEXT_EDITOR_OPT, "autoIndentEnabled", textEditorOpt.defaultAutoSyntaxSync);
		iniFile.put(TEXT_EDITOR_OPT, "markOccurrences", textEditorOpt.defaultAutoSyntaxSync);
		iniFile.put(TEXT_EDITOR_OPT, "closeMarkupTags", textEditorOpt.defaultAutoSyntaxSync);
		iniFile.put(TEXT_EDITOR_OPT, "closeCurlyBraces", textEditorOpt.defaultAutoSyntaxSync);
		iniFile.put(TEXT_EDITOR_OPT, "bracketMatchingEnabled", textEditorOpt.defaultAutoSyntaxSync);
		iniFile.put(TEXT_EDITOR_OPT, "animateBracketMatching", textEditorOpt.animateBracketMatching);
		iniFile.put(TEXT_EDITOR_OPT, "fadeCurrentLineHighlight", textEditorOpt.fadeCurrentLineHighlight);
		iniFile.put(TEXT_EDITOR_OPT, "useFocusableTips", textEditorOpt.useFocusableTips);
		iniFile.put(TEXT_EDITOR_OPT, "roundedSelectionEdges", textEditorOpt.roundedSelectionEdges);
		iniFile.put(TEXT_EDITOR_OPT, "paintMarkOccurrencesBorder", textEditorOpt.paintMarkOccurrencesBorder);
		iniFile.put(TEXT_EDITOR_OPT, "clearWhitespaceLinesEnabled", textEditorOpt.clearWhitespaceLinesEnabled);
		iniFile.put(TEXT_EDITOR_OPT, "highlightSecondaryLanguages", textEditorOpt.highlightSecondaryLanguages);
		iniFile.put(TEXT_EDITOR_OPT, "fractionalFontMetricsEnabled", textEditorOpt.fractionalFontMetricsEnabled);
		iniFile.put(TEXT_EDITOR_OPT, "hyperLinksEnabled", textEditorOpt.hyperLinksEnabled);
		iniFile.put(TEXT_EDITOR_OPT, "tabSpacesEmulated", textEditorOpt.tabSpacesEmulated);
		iniFile.put(TEXT_EDITOR_OPT, "markEndOfLine", textEditorOpt.markEndOfLine);
		iniFile.put(TEXT_EDITOR_OPT, "showBracketsPopup", textEditorOpt.showBracketsPopup);
		iniFile.put(TEXT_EDITOR_OPT, "spellChecker", textEditorOpt.spellChecker);
		iniFile.put(TEXT_EDITOR_OPT, "prefDictspellChecker", textEditorOpt.prefDictspellChecker);
		iniFile.put(TEXT_EDITOR_OPT, "lastOpenedFiles", textEditorOpt.lastOpenedFiles.toCharSeparatedString(","));
		iniFile.put(TEXT_EDITOR_OPT, "lastSelectedWorkspaces", textEditorOpt.lastSelectedWorkspaces.toCharSeparatedString(","));

//		String openedFiles = listToString(textEditorOpt.getOpenedFileList()); => this and selected at start are setted at software closing
//		iniFile.put(TEXT_EDITOR_OPT, "loadedFilesAtStart", openedFiles ); => this and selected at start are setted at software closing
	}
	
	// TO_STRING AND RELATED METHODS
	
	@Override
	public String toStringa() {
		String result = "";
		// DEBUG ReadWrite Structure
//		result="\n[Debug-Read] String INIFILE -> "+ iniReadFile.getFile().getAbsolutePath();
//		builder.append"\n[Debug-Write] String INIFILE -> "+ iniFile.getFile().getAbsolutePath()+"\n\n  -----------------\n\n ";
		StringBuilder builder = new StringBuilder();
		
		builder.append("\n\n### Config file: "+this.getIniFile().getFile().getAbsolutePath());
		// concat AES OPTIONS
		builder.append("\n\n ---- AES OPTIONS -----\n\n")
			   .append("\nFLAGAES: ").append(aesOpt.isFlagAes());
		
		// concat GENERAL OPTIONS
		builder.append("\n\n ---- GENERAL OPTIONS -----\n\n")
			   .append("USERNAME: ").append(genOpt.getUserName())
			   .append("\nGENERAL VOLUME: ").append(genOpt.getVolumeString())
			   .append("\nLAST PANEL OPENED: ").append(genOpt.getLastPanelOpened().getName())
			   .append("\nTxt Editor: Listen for changes in current file -> ").append(genOpt.isTxtEditorFileMonitorActive())
			   .append("\nTxt Editor: Listen for uncatched exceptions and show them -> ").append(genOpt.isExceptionAdvicesEnabled())
			   .append("\nCHARSET: ").append(genOpt.getCharset());
		
		// concat GUI_OPTION
		builder.append("\n\n ---- GUI OPTIONS -----\n\n")
			   .append("PREFERRED_STYLE: ").append(guiOpt.getPreferredStyle())
			   .append("\nMENU_FONT_SIZE: ").append(guiOpt.getMenuFontSize())
			   .append("\nLOG_FONT_SIZE: ").append(guiOpt.getLogFontSize())
			   .append("\nNIMBUS_SIZE_VAR: ").append(guiOpt.getNimbusSize())
			   .append("\nDARK_GUI_NUMBUS: ").append(guiOpt.isDarkGuiNimbus());
		
		// concat FILE OPTIONS
		builder.append("\n\n ---- FILE OPTIONS -----\n\n")
			   .append("LAST_DST_FOLDERPATH: ").append(fileOpt.getLastDstFolderPath())
			   .append("\nLAST_SRC_FOLDERPATH: ").append(fileOpt.getLastSrcFolderPath())
			   .append("\nFLAG_AUTOSAVE: ").append(fileOpt.isFlagAutosave())
			   .append("\nAUTOSAVE_INTERVAL: ").append(fileOpt.getAutosaveInterval())
			   .append("\nFLAGOVERWRITE: ").append(fileOpt.isFlagOverwrite());
		
		// concat STEGANO OPTIONS
		builder.append("\n\n ---- STEGANO OPTIONS -----\n\n")
			   .append("DEFAULT_HEIGHT: ").append(steganOpt.getDefaultHeight())
		  	   .append("\nULTRA-SECURE: ").append(steganOpt.isUltraSecure())
			   .append("\nFLAGSTEG: ").append(steganOpt.isFlagSteg())
		       .append("\nDEFAULT_WIDTH: ").append(steganOpt.getDefaultWidth())
		       .append("\nIMG_RECENT_PATH: ").append(steganOpt.getImgRecentPath());

		// concat TEXT EDITOR OPTIONS
		builder.append("\n\n ---- TEXT EDITOR -----\n\n")
			   .append("INLINE TABS MODE: ").append(textEditorOpt.inlinedTabs)
			   .append("\nMARGIN: ").append(textEditorOpt.margin)
			   .append("\nPREFERRED_THEME: ").append(textEditorOpt.selectedTheme)
			   .append("\nFILE_PANEL_BACKGROUND_COLOR: ").append(textEditorOpt.filePanelBackCol)
			   .append("\nBACKGROUND_COLOR: ").append(textEditorOpt.backCol)
			   .append("\nFOREGROUND_COLOR: ").append(textEditorOpt.foreCol)
			   .append("\nFONT: ").append(textEditorOpt.font)
			   .append("\nVOICE_RATE: ").append(textEditorOpt.voiceRate)
			   .append("\nPREFERRED DIVIDER FILETREE POSITION: ").append(textEditorOpt.dividerFileTreePosX)
			   .append("\nPREFERRED DIVIDER RIGHT PANE POSITION: ").append(textEditorOpt.dividerRightPanePosX)
			   .append("\nPREFERRED DIVIDER RIGHT PANE POSITION (Clipboard): ").append(textEditorOpt.dividerRightPanePosXClipboard)
			   .append("\nLAST FILE OPENED IN CLIPBOARD: ").append(textEditorOpt.lastFileOpenedClipboard)
			   .append("\nSYNC CLIPBOARD STYLE WITH CURRENT TEXT AREA: ").append(textEditorOpt.clipboardSyncStyle)
			   .append("\nOPENED FILES AT START: ").append(textEditorOpt.getOpenedFileList())
			   .append("\nLAST OPENED FILES: ").append(textEditorOpt.lastOpenedFiles.toCharSeparatedString(","))
			   .append("\nLAST SELECTED WORKSPACES: ").append(textEditorOpt.lastSelectedWorkspaces.toCharSeparatedString(","))
			   .append("\nDEFAULT OPENED FILE AT START: ").append(textEditorOpt.selectedAtStart)
			   .append("\nLAST CODE ESCAPE LANGUAGE: ").append(textEditorOpt.lastEscapeLanguage)

			   .append("\n\n TEXT AREA DETAIL VALUES: \n\n")
			   
			   .append("\nFILE TREE VIEW ACTIVE: ").append(textEditorOpt.fileTreeActive)
			   .append("\nMAP VIEW ACTIVE: ").append(textEditorOpt.mapViewActive)
			   .append("\nCLIPBOARD VIEW ACTIVE: ").append(textEditorOpt.clipboardViewActive)
			   .append("\nFILE TREE LOCKED: ").append(textEditorOpt.treeLocked)
			   .append("\nWORKSPACE VIEW SELECTED: ").append(textEditorOpt.workspaceViewSelected)
			   .append("\nDEFAULT WORKSPACE LOCATION: ").append(textEditorOpt.defaultWorkspacePath)
			   .append("\nDEFAULT AUTOMATIC SYNTAX SYNC: ").append(textEditorOpt.defaultAutoSyntaxSync)
			   .append("\nANTIALIASED TEXT AREA: ").append(textEditorOpt.isAntialiasedTxtArea())
			   .append("\nTAB SIZE: ").append(textEditorOpt.tabSize)
			   .append("\nTAB LINE VISIBLE: ").append(textEditorOpt.tabLineVisible)
			   .append("\nBRACKET MATCHING: ").append(textEditorOpt.bracketMatchingEnabled)
			   .append("\nANIMATED BRACKETS MATCH: ").append(textEditorOpt.animateBracketMatching)
			   .append("\nAUTO INDENT: ").append(textEditorOpt.autoIndentEnabled)
			   .append("\nAUTO-BREAKLINE ACTIVE: ").append(textEditorOpt.breaklineActive)
			   .append("\nCLEAR WHITESPACES LINES: ").append(textEditorOpt.clearWhitespaceLinesEnabled)
			   .append("\nCLOSE CURLY BRACES: ").append(textEditorOpt.closeCurlyBraces)
			   .append("\nCLOSE MARKUP TAGS: ").append(textEditorOpt.closeMarkupTags)
			   .append("\nCODE FOLDING ENABLED: ").append(textEditorOpt.codeFoldingEnabled)
			   .append("\nFADE CURRENT LINE HIGHLIGHT: ").append(textEditorOpt.fadeCurrentLineHighlight)
			   .append("\nFRACTIONAL FONT METRICS: ").append(textEditorOpt.fractionalFontMetricsEnabled)
			   .append("\nHIGHLIGHT SECONDARY LANGUAGES: ").append(textEditorOpt.highlightSecondaryLanguages)
			   .append("\nHYPERLINKS ENABLED: ").append(textEditorOpt.hyperLinksEnabled)
			   .append("\nMARK OCCURRENCES: ").append(textEditorOpt.markOccurrences)
			   .append("\nPAINT OCCURRENCES MARKES BORDER: ").append(textEditorOpt.paintMarkOccurrencesBorder)
			   .append("\nROUND SELECTION EDGES: ").append(textEditorOpt.roundedSelectionEdges)
			   .append("\nTAB SPACES EMULATED: ").append(textEditorOpt.roundedSelectionEdges)
			   .append("\nSHOW END OF LINE: ").append(textEditorOpt.markEndOfLine)
			   .append("\nSHOW BRACKETS MATCH POPUP: ").append(textEditorOpt.showBracketsPopup)
			   .append("\nSELL CHECKER ACTIVE: ").append(textEditorOpt.spellChecker)
			   .append("\nPREFERRED SPELLCHECK DICTIONARY: ").append(textEditorOpt.prefDictspellChecker)
			   .append("\nFOCUSABLE TIPS FLAG: ").append(textEditorOpt.useFocusableTips);
		
		// concat TEXT EDITOR OPTIONS
		builder.append("\n\n ---- IMAGE CONVERTER -----\n\n")
			   .append("SAME_SIZE: ").append(imageConvertOption.sameSize)
			   .append("\nSAME EXTENSION: ").append(imageConvertOption.sameExtension)
			   .append("\nQUALITY: ").append(imageConvertOption.quality)
			   .append("\nALGORITHM: ").append(imageConvertOption.algorithm);
		
		// create and return result string
		result = builder.toString();
		return result;
	}
	
	public GuiOption getGuiOpt() {
		return guiOpt;
	}

	public void setGuiOpt(GuiOption guiOpt) {
		this.guiOpt = guiOpt;
	}

	public GenericOption getGenOpt() {
		return genOpt;
	}

	public void setGenOpt(GenericOption genOpt) {
		this.genOpt = genOpt;
	}

	public FileOptions getFileOpt() {
		return fileOpt;
	}

	public void setFileOpt(FileOptions fileOpt) {
		this.fileOpt = fileOpt;
	}

	public SteganOption getSteganOpt() {
		return steganOpt;
	}

	public void setSteganOpt(SteganOption steganOpt) {
		this.steganOpt = steganOpt;
	}

	public AesOption getAesOpt() {
		return aesOpt;
	}

	public void setAesOpt(AesOption aesOpt) {
		this.aesOpt = aesOpt;
	}

	public TextEditorOption getTextEditorOpt() {
		return this.textEditorOpt;
	}
	
	public ImgConverterOption getImageConvertionOpt() {
		return this.imageConvertOption;
	}
}

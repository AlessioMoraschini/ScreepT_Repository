package mainobj;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.ini4j.Ini;
import org.ini4j.Wini;

import enums.ScalingAlgorithm;
import enums.Themes;
import om.AesOption;
import om.FileOptions;
import om.GenericOption;
import om.GuiOption;
import om.ImgConverterOption;
import om.SteganOption;
import om.TextEditorOption;
import resources.GeneralConfig;
import variousutils.HandleString;

public class INItializer {

	// INItializer logger creation		
	static Logger logger = Logger.getLogger(INItializer.class); 
		
	
	// DEFAULT INI
	public final static String DEFAULT_INI_PATH = GeneralConfig.RESOURCES_DIR  + "CONFIG.ini"; 
	
	// FILEDS
	private Ini iniReadFile;
	private Wini iniFile;
	
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
		textEditorOpt = new TextEditorOption();
		imageConvertOption = new ImgConverterOption();
	}
	
//	public INItializer(Ini ini) {
//		iniReadFile = ini;
//		genOpt = new GenericOption();
//		fileOpt = new FileOptions();
//		steganOpt = new SteganOption();
//		aesOpt = new AesOption();
//	}
	
	public INItializer(Wini configFile) {
		// initialize both read and write ini variable, to keep them synchronyzed
		if(configFile!=null) {
			iniReadFile = configFile;
			iniFile = configFile;
		} else try {
			iniReadFile = new Wini(new File(DEFAULT_INI_PATH));
			iniFile = new Wini(new File(DEFAULT_INI_PATH));
		} catch (IOException e) {
			logger.error("Exception happened!", e);
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
			genOpt.setGeneralVolume(Float.parseFloat(iniReadFile.get(GEN_OPT, "generalVolume")));
		} catch (Exception e1) {
			genOpt.setGeneralVolume(GenericOption.DEFAULT_VOLUME);
		}
	}
	
	public void loadTxtEditorOptions() {
		// TEXT EDITOR OPTIONS
		
		// INI read
		String fontStr = iniReadFile.get(TEXT_EDITOR_OPT, "font");
		String FilePanelbackColStr = iniReadFile.get(TEXT_EDITOR_OPT, "filePanelBackgroundCol");
		String backColStr = iniReadFile.get(TEXT_EDITOR_OPT, "backgroundCol");
		String foreColStr = iniReadFile.get(TEXT_EDITOR_OPT, "foregroundCol");
		String selectedAtStartStr = iniReadFile.get(TEXT_EDITOR_OPT, "selectedFileAtStart");
		String defaultWorspaceLocationStr = iniReadFile.get(TEXT_EDITOR_OPT, "defaultWorspaceLocation");
		
		// validation
		Font font = (!"".equals(fontStr) && fontStr!= null)? HandleString.getFontFromString(fontStr) : TextEditorOption.DEFAULT_FONT;
		Color backCol = (!"".equals(backColStr) && backColStr!= null)? HandleString.getRgbColorFromString(backColStr) : TextEditorOption.DEFAULT_BACKGROUND_COL;
		Color filePanelBackCol = (!"".equals(FilePanelbackColStr) && FilePanelbackColStr!= null)? HandleString.getRgbColorFromString(FilePanelbackColStr) : TextEditorOption.DEFAULT_FILE_PANEL_BACKGROUND_COL;
		Color foreCol = (!"".equals(foreColStr) && foreColStr!= null)? HandleString.getRgbColorFromString(foreColStr) : TextEditorOption.DEFAULT_FOREGROUND_COL;
		String selectedFileAtStart = (!"".equals(selectedAtStartStr) && selectedAtStartStr!= null)? selectedAtStartStr : TextEditorOption.DEFAULT_SELECTED_FILE_AT_START;
		String defaultWorspaceLocation = (!"".equals(defaultWorspaceLocationStr) && defaultWorspaceLocationStr!= null)? defaultWorspaceLocationStr : TextEditorOption.DEFAULT_WORKSPACE_LOCATION;
		
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
		
		// CONVERTED/PARSED TYPES
		
		// automatic syntax selection file-extension-based
		textEditorOpt.setDefaultAutoSyntaxSync(Boolean.valueOf(iniReadFile.get(TEXT_EDITOR_OPT, "defaultAutoSyntaxSync")));
		
		// workspace view selected
		textEditorOpt.setWorkspaceViewSelected(Boolean.valueOf(iniReadFile.get(TEXT_EDITOR_OPT, "workspaceViewSelected")));
		
		// file tree locked
		try {
			textEditorOpt.setTreeLocked(Boolean.valueOf(iniReadFile.get(TEXT_EDITOR_OPT, "isTreeLocked")));
		} catch (NumberFormatException e) {
			textEditorOpt.setTreeLocked(TextEditorOption.DEFAULT_TREE_LOCKED);
		}
		
		// tabLineVisible
		try {
			textEditorOpt.setTabLineVisible(Boolean.valueOf(iniReadFile.get(TEXT_EDITOR_OPT, "tabLineVisibleFlag")));
		} catch (NumberFormatException e) {
			textEditorOpt.setTabLineVisible(TextEditorOption.DEFAULT_TAB_LINE_VISIBLE);
		}
				
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
		// divider size
		try {
			textEditorOpt.setDividerFileTreePosX(Integer.valueOf(iniReadFile.get(TEXT_EDITOR_OPT, "dividerFileTreePosX")));
		} catch (NumberFormatException e) {
			textEditorOpt.setVoiceRate(TextEditorOption.DEFAULT_DIVIDER_POS_X);
		}
		// txtArea detail
		try {
			textEditorOpt.setTxtAreaDetailLevel(Integer.valueOf(iniReadFile.get(TEXT_EDITOR_OPT, "txtAreaDetailLevel")));
		} catch (NumberFormatException e) {
			textEditorOpt.setTxtAreaDetailLevel(TextEditorOption.DEFAULT_TXT_AREA_DETAIL);
		}
		// file visibility
		try {
			textEditorOpt.setFileTreeActive(Boolean.valueOf(iniReadFile.get(TEXT_EDITOR_OPT, "fileTreeActive")));
		} catch (NumberFormatException e) {
			textEditorOpt.setFileTreeActive(TextEditorOption.DEFAULT_FTREE_ACTIVE);
		}
		// linebreak active
		try {
			textEditorOpt.setBreaklineActive(Boolean.valueOf(iniReadFile.get(TEXT_EDITOR_OPT, "breakLineActive")));
		} catch (NumberFormatException e) {
			textEditorOpt.setBreaklineActive(TextEditorOption.DEFAULT_BREAK_LINE);
		}
		// antialiasing textarea active
		try {
			textEditorOpt.setAntialiasedTxtArea(Boolean.valueOf(iniReadFile.get(TEXT_EDITOR_OPT, "antialiasedTxtArea")));
		} catch (NumberFormatException e) {
			textEditorOpt.setAntialiasedTxtArea(TextEditorOption.DEFAULT_ANTIALIASED_TXT_AREA);
		}
		
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
		// selected theme
		String themeRead = iniReadFile.get(TEXT_EDITOR_OPT, "preferredTheme");
		Themes theme = (themeRead != null && !themeRead.equals(""))? Themes.valueOf(themeRead): TextEditorOption.DEFAULT_SELECTED_THEME;
		textEditorOpt.setSelectedTheme(theme);
	}
	
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
		iniFile.put(TEXT_EDITOR_OPT, "voiceRate", textEditorOpt.voiceRate);
		iniFile.put(TEXT_EDITOR_OPT, "preferredTheme", textEditorOpt.selectedTheme);
		iniFile.put(TEXT_EDITOR_OPT, "fileTreeActive", textEditorOpt.fileTreeActive);
		iniFile.put(TEXT_EDITOR_OPT, "dividerFileTreePosX", textEditorOpt.dividerFileTreePosX);
		iniFile.put(TEXT_EDITOR_OPT, "antialiasedTxtArea", textEditorOpt.antialiasedTxtArea);
		iniFile.put(TEXT_EDITOR_OPT, "breakLineActive", textEditorOpt.breaklineActive);
		iniFile.put(TEXT_EDITOR_OPT, "txtAreaDetailLevel", textEditorOpt.txtAreaDetailLevel);
		iniFile.put(TEXT_EDITOR_OPT, "isTreeLocked", textEditorOpt.treeLocked);
		iniFile.put(TEXT_EDITOR_OPT, "defaultWorspaceLocation", textEditorOpt.defaultWorkspacePath);
		iniFile.put(TEXT_EDITOR_OPT, "workspaceViewSelected", textEditorOpt.workspaceViewSelected);
		iniFile.put(TEXT_EDITOR_OPT, "defaultAutoSyntaxSync", textEditorOpt.defaultAutoSyntaxSync);
		
//		String openedFiles = listToString(textEditorOpt.getOpenedFileList()); => this and selected at start are setted at software closing
//		iniFile.put(TEXT_EDITOR_OPT, "loadedFilesAtStart", openedFiles ); => this and selected at start are setted at software closing
	}
	
	/**
	 * This method is used to write on file single opt values in a precise section
	 * @return true if no exception is throwed
	 */
	public synchronized boolean writeSingleValue(String OPT_SECTION, String OPT_VAR, String OPT_VALUE ) {
		boolean OK = false;
		
		synchronized (iniFile) {
			try {
				iniFile.put(OPT_SECTION, OPT_VAR, OPT_VALUE);
				iniFile.store();
				OK = true;
				logger.info(iniFile.getFile().getCanonicalPath()+" - Single option saved -> "+OPT_SECTION+" - "+OPT_VAR+" : "+OPT_VALUE);
			} catch (IOException e) {
				OK = false;
				logger.error("Exception happened!", e);
			}
		}
		return OK;
	}
	
	/**
	 * This method is used to write on file single opt values in a precise section
	 * @return true if no exception is throwed
	 */
	public synchronized boolean writeSingleValue(String OPT_SECTION, String OPT_VAR, float OPT_VALUE ) {
		boolean OK = false;
		
		synchronized (iniFile) {
			try {
				iniFile.put(OPT_SECTION, OPT_VAR, OPT_VALUE);
				iniFile.store();
				OK = true;
				logger.info(iniFile.getFile().getCanonicalPath()+" - Single option saved -> "+OPT_SECTION+" - "+OPT_VAR+" : "+OPT_VALUE);
			} catch (IOException e) {
				OK = false;
				logger.error("Exception happened!", e);
			}
		}
		return OK;
	}
	
	/**
	 * This method is used to write on file single opt values in a precise section (INT value)
	 * @return true if no exception is throwed
	 */
	public synchronized boolean writeSingleValue(String OPT_SECTION, String OPT_VAR, int OPT_VALUE ) {
		boolean OK = false;
		
		synchronized (iniFile) {
			try {
				iniFile.put(OPT_SECTION, OPT_VAR, OPT_VALUE);
				iniFile.store();
				OK = true;
				logger.info(iniFile.getFile().getCanonicalPath()+" - Single option saved -> "+OPT_SECTION+" - "+OPT_VAR+" : "+OPT_VALUE);
			} catch (IOException e) {
				OK = false;
				logger.error("Exception happened!", e);
			}
		}
		return OK;
	}
	
	/**
	 * This method is used to write on file single opt values in a precise section (INT value)
	 * @return true if no exception is throwed
	 */
	public synchronized boolean writeSingleValue(String OPT_SECTION, String OPT_VAR, boolean OPT_VALUE ) {
		boolean OK = false;
		
		synchronized (iniFile) {
			try {
				iniFile.put(OPT_SECTION, OPT_VAR, OPT_VALUE);
				iniFile.store();
				OK = true;
				logger.info(iniFile.getFile().getCanonicalPath()+" - Single option saved -> "+OPT_SECTION+" - "+OPT_VAR+" : "+OPT_VALUE);
			} catch (IOException e) {
				OK = false;
				logger.error("Exception happened!", e);
			}
		}
		return OK;
	}

	// TO_STRING AND RELATED METHODS
	
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
			   .append("\nTAB_SIZE: ").append(textEditorOpt.tabSize)
			   .append("\nTAB_LINE_VISIBLE: ").append(textEditorOpt.tabLineVisible)
			   .append("\nFILE TREE VIEW ACTIVE: ").append(textEditorOpt.fileTreeActive)
			   .append("\nFILE TREE LOCKED: ").append(textEditorOpt.treeLocked)
			   .append("\nWORKSPACE VIEW SELECTED: ").append(textEditorOpt.workspaceViewSelected)
			   .append("\nDEFAULT WORKSPACE LOCATION: ").append(textEditorOpt.defaultWorkspacePath)
			   .append("\nDEFAULT AUTOMATIC SYNTAX SYNC: ").append(textEditorOpt.defaultAutoSyntaxSync)
			   .append("\nPREFERRED_THEME: ").append(textEditorOpt.selectedTheme)
			   .append("\nFILE_PANEL_BACKGROUND_COLOR: ").append(textEditorOpt.filePanelBackCol)
			   .append("\nBACKGROUND_COLOR: ").append(textEditorOpt.backCol)
			   .append("\nFOREGROUND_COLOR: ").append(textEditorOpt.foreCol)
			   .append("\nFONT: ").append(textEditorOpt.font)
			   .append("\nPREFERRED DIVIDER FILETREE POSITION: ").append(textEditorOpt.dividerFileTreePosX)
			   .append("\nOPENED FILES AT START: ").append(textEditorOpt.getOpenedFileList())
			   .append("\nDEFAULT OPENED FILE AT START: ").append(textEditorOpt.selectedAtStart)
			   .append("\nAUTO-BREAKLINE ACTIVE: ").append(textEditorOpt.isBreaklineActive())
			   .append("\nANTIALIASED TEXT AREA: ").append(textEditorOpt.isAntialiasedTxtArea())
			   .append("\nTEXT-AREA DETAIL LEVEL (0-3): ").append(textEditorOpt.getTxtAreaDetailLevel())
			   .append("\nVOICE_RATE: ").append(textEditorOpt.voiceRate);
		
		// concat TEXT EDITOR OPTIONS
		builder.append("\n\n ---- IMAGE CONVERTER -----\n\n")
			   .append("SAME_SIZE: ").append(imageConvertOption.sameSize)
			   .append("\nSAME_EXTENSION: ").append(imageConvertOption.sameExtension)
			   .append("\nQUALITY: ").append(imageConvertOption.quality)
			   .append("\nALGORITHM: ").append(imageConvertOption.algorithm);
		
		// create and return result string
		result = builder.toString();
		return result;
	}
	
	public static String listToString(ArrayList<String> loadedFiles) {
		String openedFiles = "";
		// concateno gli elementi della lista e, tranne l'ultimo, aggiungo una virgola
		for(String currentPath : loadedFiles) {
			openedFiles += currentPath+",";
		}
		if(openedFiles.length() > 0) {
			openedFiles = openedFiles.substring(0, openedFiles.length()-1);
		}
		return openedFiles;
	}
	
	public GuiOption getGuiOpt() {
		return guiOpt;
	}

	public void setGuiOpt(GuiOption guiOpt) {
		this.guiOpt = guiOpt;
	}

	public Wini getIniFile() {
		return iniFile;
	}

	public Ini getIniReadFile() {
		return iniReadFile;
	}

	public void setIniReadFile(Ini iniReadFile) {
		this.iniReadFile = iniReadFile;
	}

	public void setIniFile(Wini iniFile) {
		this.iniFile = iniFile;
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

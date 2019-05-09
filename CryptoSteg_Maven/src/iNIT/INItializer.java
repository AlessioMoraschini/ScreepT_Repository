package iNIT;

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

import gUI.om.ScalingAlgorithm;
import iNIT.om.AesOption;
import iNIT.om.FileOptions;
import iNIT.om.GenericOption;
import iNIT.om.GuiOption;
import iNIT.om.ImgConverterOption;
import iNIT.om.SteganOption;
import iNIT.om.TextEditorOption;
import local.HandleString;
import maingUI.mainGUI;

public class INItializer {

	// INItializer logger creation		
	static Logger logger = Logger.getLogger(INItializer.class); 
		
	
	// DEFAULT INI
	public final static String DEFAULT_INI_PATH = "Resources_Scrypt/CONFIG.ini"; 
	
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
		
		// LOAD GUI OPTIONS
		
		// int values
		try {
			guiOpt.setMenuFontSize(Integer.valueOf(iniReadFile.get(GUI_OPT, "menuFontSize")));
			guiOpt.setLogFontSize(Integer.valueOf(iniReadFile.get(GUI_OPT, "logFontSize")));
		} catch (NumberFormatException e) {
			guiOpt.setLogFontSize(GuiOption.DEFAULT_LOG_FONT_SIZE);
			guiOpt.setMenuFontSize(GuiOption.DEFAULT_MENU_FONT_SIZE);
		}
		// string values
		guiOpt.setPreferredStyle(iniReadFile.get(GUI_OPT, "preferredStyle"));
		if(guiOpt.getPreferredStyle().equals("")||guiOpt.getPreferredStyle()==null) {
			guiOpt.setPreferredStyle(GuiOption.DEFAULT_GUI_STYLE);
		}
		// boolean
		guiOpt.setDarkGuiNimbus(Boolean.parseBoolean(iniReadFile.get(GUI_OPT, "darkGuiNimbus")));
				
		
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
		
		// TEXT EDITOR OPTIONS
		String fontStr = iniReadFile.get(TEXT_EDITOR_OPT, "font");
		String backColStr = iniReadFile.get(TEXT_EDITOR_OPT, "backgroundCol");
		String foreColStr = iniReadFile.get(TEXT_EDITOR_OPT, "foregroundCol");
		String selectedAtStartStr = iniReadFile.get(TEXT_EDITOR_OPT, "selectedFileAtStart");
		Font font = (!"".equals(fontStr))? HandleString.getFontFromString(fontStr) : TextEditorOption.DEFAULT_FONT;
		Color backCol = (!"".equals(backColStr))? HandleString.getRgbColorFromString(backColStr) : TextEditorOption.DEFAULT_BACKGROUND_COL;
		Color foreCol = (!"".equals(foreColStr))? HandleString.getRgbColorFromString(foreColStr) : TextEditorOption.DEFAULT_FOREGROUND_COL;
		String selectedFileAtStart = (!"".equals(selectedAtStartStr))? selectedAtStartStr : TextEditorOption.DEFAULT_SELECTED_FILE_AT_START;
		textEditorOpt.setFont(font);
		textEditorOpt.setBackCol(backCol);
		textEditorOpt.setForeCol(foreCol);
		textEditorOpt.setSelectedAtStart(selectedFileAtStart);
		textEditorOpt.setInlinedTabs(Boolean.parseBoolean(iniReadFile.get(TEXT_EDITOR_OPT, "inlineTabs")));
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
		
		logger.info("Loaded ini file: "+ this.toStringa());
	}
	
	
	// WRITE & FILL FIELDS METHODS
	
	/**
	 * Method that write on the iniFile (notice that it's different from iniReadFile which is user to read values from file)
	 * the values setted on pojo's of this class. if some value is null it writes default values of pojo's
	 */
	public synchronized void writeCurrentConfigIniFile() {
		
		// set INIFILE-AES OPTIONS
		iniFile.put(AES_OPT, "flagCodificaAes", aesOpt.isFlagAes());
		
		// set INIFILE-GENERAL OPTIONS
		if(genOpt.getUserName().equals("")) {
			genOpt.setUserName(GenericOption.DEFAULT_USERNAME);
		}
		iniFile.put(GEN_OPT, "userName", genOpt.getUserName());
		iniFile.put(GEN_OPT, "charset", genOpt.getCharset());
		iniFile.put(GEN_OPT, "generalVolume", genOpt.getGeneralVolume());
		
		// set INIFILE-GUI OPTIONS
		if(guiOpt.getPreferredStyle().equals("")) {
			guiOpt.setPreferredStyle(GuiOption.DEFAULT_GUI_STYLE);
		}
		iniFile.put(GUI_OPT, "preferredStyle", guiOpt.getPreferredStyle());
		iniFile.put(GUI_OPT, "menuFontSize", guiOpt.getMenuFontSize());
		iniFile.put(GUI_OPT, "logFontSize", guiOpt.getLogFontSize());
		iniFile.put(GUI_OPT, "darkGuiNimbus", guiOpt.isDarkGuiNimbus());

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
		
		// TEXT EDITOR OPTIONS
		iniFile.put(TEXT_EDITOR_OPT, "inlineTabs", textEditorOpt.isInlinedTabs());
		iniFile.put(TEXT_EDITOR_OPT, "font", HandleString.getFontString(textEditorOpt.font));
		iniFile.put(TEXT_EDITOR_OPT, "backgroundCol", HandleString.getRgbString(textEditorOpt.backCol));
		iniFile.put(TEXT_EDITOR_OPT, "foregroundCol", HandleString.getRgbString(textEditorOpt.foreCol));
		iniFile.put(TEXT_EDITOR_OPT, "margin", textEditorOpt.margin);
		iniFile.put(TEXT_EDITOR_OPT, "voiceRate", textEditorOpt.voiceRate);
//		String openedFiles = listToString(textEditorOpt.getOpenedFileList()); => this and selected at start are setted at software closing
//		iniFile.put(TEXT_EDITOR_OPT, "loadedFilesAtStart", openedFiles );
		
		// IMAGE CONVERTER OPTIONS
		iniFile.put(IMAGE_CONVERTER_OPT, "sameSize", imageConvertOption.sameSize);
		iniFile.put(IMAGE_CONVERTER_OPT, "sameExtension", imageConvertOption.sameExtension);
		iniFile.put(IMAGE_CONVERTER_OPT, "quality", imageConvertOption.quality);
		iniFile.put(IMAGE_CONVERTER_OPT, "algorithm", imageConvertOption.algorithm);

	logger.info("Configuration written to: "+iniFile.getFile().getAbsolutePath() + this.toStringa());
		
		// WRITE ALL VALUES TO FILE
		try {
			iniFile.store();
		} catch (IOException e) {
			logger.error("Exception happened!", e);
		}
	}
	
	/**
	 * This method is used to write on file single opt values in a precise section
	 * @return true if no exception is throwed
	 */
	public synchronized boolean writeSingleValue(String OPT_SECTION, String OPT_VAR, String OPT_VALUE ) {
		boolean OK = false;
		
		try {
			iniFile.put(OPT_SECTION, OPT_VAR, OPT_VALUE );
			iniFile.store();
			OK = true;
		} catch (IOException e) {
			OK = false;
			logger.error("Exception happened!", e);
		}
		return OK;
	}
	
	/**
	 * This method is used to write on file single opt values in a precise section
	 * @return true if no exception is throwed
	 */
	public synchronized boolean writeSingleValue(String OPT_SECTION, String OPT_VAR, float OPT_VALUE ) {
		boolean OK = false;
		
		try {
			iniFile.put(OPT_SECTION, OPT_VAR, OPT_VALUE );
			iniFile.store();
			OK = true;
		} catch (IOException e) {
			OK = false;
			logger.error("Exception happened!", e);
		}
		return OK;
	}
	
	/**
	 * This method is used to write on file single opt values in a precise section (INT value)
	 * @return true if no exception is throwed
	 */
	public synchronized boolean writeSingleValue(String OPT_SECTION, String OPT_VAR, int OPT_VALUE ) {
		boolean OK = false;
		
		try {
			iniFile.put(OPT_SECTION, OPT_VAR, OPT_VALUE );
			iniFile.store();
			OK = true;
		} catch (IOException e) {
			OK = false;
			logger.error("Exception happened!", e);
		}
		return OK;
	}
	
	/**
	 * This method is used to write on file single opt values in a precise section (INT value)
	 * @return true if no exception is throwed
	 */
	public synchronized boolean writeSingleValue(String OPT_SECTION, String OPT_VAR, boolean OPT_VALUE ) {
		boolean OK = false;
		
		try {
			iniFile.put(OPT_SECTION, OPT_VAR, OPT_VALUE );
			iniFile.store();
			OK = true;
		} catch (IOException e) {
			OK = false;
			logger.error("Exception happened!", e);
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
			   .append("\nSELECTED LOG LEVEL: ").append(mainGUI.LOG_LEVEL_AT_START)
			   .append("\nCHARSET: ").append(genOpt.getCharset());
		
		// concat GUI_OPTION
		builder.append("\n\n ---- GUI OPTIONS -----\n\n")
			   .append("PREFERRED_STYLE: ").append(guiOpt.getPreferredStyle())
			   .append("\nMENU_FONT_SIZE: ").append(guiOpt.getMenuFontSize())
			   .append("\nLOG_FONT_SIZE: ").append(guiOpt.getLogFontSize())
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
			   .append("FONT: ").append(textEditorOpt.font)
			   .append("\nINLINE TABS MODE: ").append(textEditorOpt.inlinedTabs)
			   .append("\nMARGIN: ").append(textEditorOpt.margin)
			   .append("\nBACKGROUND_COLOR: ").append(textEditorOpt.backCol)
			   .append("\nFOREGROUND_COLOR: ").append(textEditorOpt.foreCol)
			   .append("\nOPENED FILES AT START: ").append(textEditorOpt.getOpenedFileList())
			   .append("\n DEFAULT OPENED FILE AT START: ").append(textEditorOpt.selectedAtStart)
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

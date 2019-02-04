package iNIT;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.ini4j.Ini;
import org.ini4j.Wini;

import iNIT.om.AesOption;
import iNIT.om.FileOptions;
import iNIT.om.GenericOption;
import iNIT.om.GuiOption;
import iNIT.om.SteganOption;

public class INItializer {

	// INItializer logger creation		
	static Logger logger = Logger.getLogger(INItializer.class); 
		
	
	// DEFAULT INI
	public final String DEFAULT_INI_PATH = "Resources_Scrypt/CONFIG.ini"; 
	public final static int DEFAULT_MIN_AUTOSAVE_INTERVAL = 60000;
	// FILEDS
	private Ini iniReadFile;
	private Wini iniFile;
	private GenericOption genOpt;
	private FileOptions fileOpt;
	private SteganOption steganOpt;
	private AesOption aesOpt;
	private GuiOption guiOpt;
	
	public final String AES_OPT = "AES_OPTIONS";
	public final String STEGA_OPT = "STEGANO_OPTIONS";
	public final String FILE_OPT = "FILE_OPTIONS";
	public final String GEN_OPT = "GENERIC_OPTIONS";
	public final String GUI_OPT = "GUI_OPTIONS";

	
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
	}
	
	// READ & FILL FIELDS METHODS
	
	/**
	 * Method that fills the pojo object mapped from the file given as ini file with iniReadFile setter method, 
	 * or also with constructor "INItializer(Wini configFile)". if some value is null it sets default values.
	 */
	public void readConfigIniFile() {
		
		// LOAD AES OPTIONS
		aesOpt.setAesKey(iniReadFile.get(AES_OPT, "aesKey"));
		if(aesOpt.getAesKey().equals("")||aesOpt.getAesKey()==null) {
			aesOpt.setAesKey(aesOpt.DEFAULT_AES_KEY);
		}
		
		// LOAD GENERAL OPTIONS
		genOpt.setFlagAes(Boolean.parseBoolean(iniReadFile.get(GEN_OPT, "flagCodificaAes")));
		genOpt.setFlagSteg(Boolean.parseBoolean(iniReadFile.get(GEN_OPT, "flagCodificaSteg")));
		genOpt.setUserName(iniReadFile.get(GEN_OPT, "userName"));
		if(genOpt.getUserName().equals("")) {
			genOpt.setUserName(genOpt.DEFAULT_USERNAME);
		}
		genOpt.setCharset(iniReadFile.get(GEN_OPT, "charset"));
		if(genOpt.getCharset().equals("")) {
			genOpt.setCharset(genOpt.DEFAULT_CHARSET);
		}
		
		// LOAD GUI OPTIONS
		
		// int values
		try {
			guiOpt.setMenuFontSize(Integer.valueOf(iniReadFile.get(GUI_OPT, "menuFontSize")));
			guiOpt.setLogFontSize(Integer.valueOf(iniReadFile.get(GUI_OPT, "logFontSize")));
		} catch (NumberFormatException e) {
			guiOpt.setLogFontSize(guiOpt.DEFAULT_LOG_FONT_SIZE);
			guiOpt.setMenuFontSize(guiOpt.DEFAULT_MENU_FONT_SIZE);
			logger.error("Exception happened!", e);
		}
		// string values
		guiOpt.setPreferredStyle(iniReadFile.get(GUI_OPT, "preferredStyle"));
		if(guiOpt.getPreferredStyle().equals("")||guiOpt.getPreferredStyle()==null) {
			guiOpt.setPreferredStyle(guiOpt.DEFAULT_GUI_STYLE);
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
			fileOpt.setLastDstFolderPath(fileOpt.DEFAULT_LAST_DEST_FOLDER_PATH);
		}
		fileOpt.setLastSrcFolderPath(iniReadFile.get(FILE_OPT, "lastSrcFolder"));
		if(fileOpt.getLastSrcFolderPath().equals("")) {
			fileOpt.setLastSrcFolderPath(fileOpt.DEFAULT_LAST_SRC_FOLDER_PATH);
		}
		// integer
		try {
			// minimum value 1 minutes
			if(Integer.valueOf(iniReadFile.get(FILE_OPT, "autosaveInterval"))<DEFAULT_MIN_AUTOSAVE_INTERVAL) {
				fileOpt.setAutosaveInterval(DEFAULT_MIN_AUTOSAVE_INTERVAL);
			}else {
				fileOpt.setAutosaveInterval(Integer.valueOf(iniReadFile.get(FILE_OPT, "autosaveInterval")));
			}
			
		} catch (NumberFormatException e) {
			fileOpt.setAutosaveInterval(fileOpt.DEFAULT_AUTOSAVE_INTERVAL);
			logger.error("Exception happened!", e);
		}
		
		// LOAD STEGANO OPTIONS
		try {
			steganOpt.setDefaultHeight(Integer.valueOf(iniReadFile.get(STEGA_OPT, "defaultWidth")));
			steganOpt.setDefaultWidth(Integer.valueOf(iniReadFile.get(STEGA_OPT, "defaultHeight")));
		} catch (NumberFormatException e) {
			steganOpt.setDefaultHeight(steganOpt.DEFAULT_HEIGHT);
			steganOpt.setDefaultWidth(steganOpt.DEFAULT_WIDTH);
			logger.error("Exception happened!", e);
		}
		String pathsListString = iniReadFile.get(STEGA_OPT, "imgKeyRecentPath");
		if(pathsListString.equals("")) {
			pathsListString = steganOpt.DEFAULT_IMG_RECENT_PATH;
		}
		StringTokenizer tokenizer = new StringTokenizer(pathsListString, ",");
		steganOpt.getImgRecentPath().clear();
		
		while(tokenizer.hasMoreTokens()) {
			steganOpt.getImgRecentPath().add(tokenizer.nextToken());
		}
		logger.debug("Loaded ini file: "+ this.toStringa());
	}
	
	
	// WRITE & FILL FIELDS METHODS
	
	/**
	 * Method that write on the iniFile (notice that it's different from iniReadFile which is user to read values from file)
	 * the values setted on pojo's of this class. if some value is null it writes default values of pojo's
	 */
	public synchronized void writeCurrentConfigIniFile() {
		
		// set INIFILE-AES OPTIONS
		if(aesOpt.getAesKey().equals("")) {
			aesOpt.setAesKey(aesOpt.DEFAULT_AES_KEY);
		}
		iniFile.put(AES_OPT, "aesKey", aesOpt.getAesKey());
		
		// set INIFILE-GENERAL OPTIONS
		if(genOpt.getUserName().equals("")) {
			genOpt.setUserName(genOpt.DEFAULT_USERNAME);
		}
		iniFile.put(GEN_OPT, "userName", genOpt.getUserName());
		iniFile.put(GEN_OPT, "charset", genOpt.getCharset());
		iniFile.put(GEN_OPT, "flagCodificaAes", genOpt.isFlagAes());
		iniFile.put(GEN_OPT, "flagCodificaSteg", genOpt.isFlagSteg());
		
		// set INIFILE-GUI OPTIONS
		if(guiOpt.getPreferredStyle().equals("")) {
			guiOpt.setPreferredStyle(guiOpt.DEFAULT_GUI_STYLE);
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
			fileOpt.setLastDstFolderPath(fileOpt.DEFAULT_LAST_DEST_FOLDER_PATH);
		}
		iniFile.put(FILE_OPT, "lastDstFolder", fileOpt.getLastDstFolderPath());
		if(fileOpt.getLastSrcFolderPath().equals("")) {
			fileOpt.setLastSrcFolderPath(fileOpt.DEFAULT_LAST_SRC_FOLDER_PATH);
		}
		iniFile.put(FILE_OPT, "lastSrcFolder", fileOpt.getLastSrcFolderPath());
		// integer values
		iniFile.put(FILE_OPT, "autosaveInterval", fileOpt.getAutosaveInterval());
		
		
		// set INIFILE-STEGANO OPTIONS
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
			accumulator = steganOpt.DEFAULT_IMG_RECENT_PATH;
		}
		iniFile.put(STEGA_OPT, "imgKeyRecentPath", accumulator );
		
		// log the operation
		logger.debug("Configuration written to: "+iniFile.getFile().getAbsolutePath() + this.toStringa());
		
		// write the put values on the File
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
		//result="\n[Debug-Read] String INIFILE -> "+ iniReadFile.getFile().getAbsolutePath();
		//result += "\n[Debug-Write] String INIFILE -> "+ iniFile.getFile().getAbsolutePath()+"\n\n  -----------------\n\n ";
		
		result+= ("\n\n### Config file: "+this.getIniFile().getFile().getAbsolutePath()+"\n\n");
		// concat AES OPTIONS
		result += ("\n ---- AES OPTIONS -----\n\n " + "AESKEY: " + aesOpt.getAesKey()+"\n ");
		// concat GENERAL OPTIONS
		result += (" \n ---- GENERAL OPTIONS -----\n\n " + "USERNAME: " + genOpt.getUserName()+"\n ");
		result += ("CHARSET: " + genOpt.getCharset()+"\n ");
		result += ("FLAGAES: " + genOpt.isFlagAes()+"\n ");
		result += ("FLAGSTEG: " + genOpt.isFlagSteg()+"\n ");
		// concat GUI_OPTION
		result += (" \n ---- GUI OPTIONS -----\n\n " + "PREFERRED_STYLE: " + guiOpt.getPreferredStyle()+"\n ");
		result += ("MENU_FONT_SIZE: " + guiOpt.getMenuFontSize()+"\n ");
		result += ("LOG_FONT_SIZE: " +guiOpt.getLogFontSize()+"\n ");
		result += ("DARK_GUI_NUMBUS: " +guiOpt.isDarkGuiNimbus()+"\n ");
		// concat FILE OPTIONS
		result += (" \n ---- FILE OPTIONS -----\n\n " + "LAST_DST_FOLDERPATH: " + fileOpt.getLastDstFolderPath()+"\n ");
		result += ("LAST_SRC_FOLDERPATH: " +fileOpt.getLastSrcFolderPath()+"\n ");
		result += ("FLAG_AUTOSAVE: " + fileOpt.isFlagAutosave()+"\n ");
		result += ("AUTOSAVE_INTERVAL: " + fileOpt.getAutosaveInterval()+"\n ");
		result += ("FLAGOVERWRITE: " + fileOpt.isFlagOverwrite()+" \n ");
		// concat STEGANO OPTIONS
		result += (" \n ---- STEGANO OPTIONS -----\n\n " + "DEFAULT_HEIGHT: " + steganOpt.getDefaultHeight()+"\n ");
		result += ("DEFAULT_WIDTH: " + steganOpt.getDefaultWidth()+"\n ");
		result += ("IMG_RECENT_PATH: " +steganOpt.getImgRecentPath()+"\n ");

		
		return result;
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
	
	
	
}

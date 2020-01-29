/**
 *
 * =========================================================================================
 *  Copyright (C) 2019-2020
 *
 *  AM-Design-Development - (Alessio Moraschini) - All Rights Reserved
 * =========================================================================================
 *
 * You should have received a copy of the license with this file.
 * If not, please write to: info@am-design-development.com, or visit : https://www.am-design-development.com
 */
package mainobj;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;
import org.ini4j.Wini;

import utility.ConversionUtils;
import utility.SafeLogger;
import utility.reflection.ReflectionUtils;
import utility.string.StringWorker;

public abstract class INItializerParent implements INItializerInterface{

	static SafeLogger logger = new SafeLogger(INItializerParent.class); 
	
	public static final String USER_PERSONAL_DIR = System.getProperty("user.home")+"/";
	
	// FIELDS
	Ini iniReadFile;
	Wini iniFile;
	
	public void readConfigIniFile() {};
	
	public synchronized void writeCurrentConfigIniFile() {};
	
	
	/**
	 * Use this to store all pending changes to file (options must have been pre-written with <b>writeXxxOptions()</b>)
	 */
	public void store() {
		// WRITE ALL VALUES TO FILE
		try {
			iniFile.store();
		} catch (IOException e) {
			logger.error("Exception happened!", e);
		}
	}
	
	/**
	 * Read a value from a given INI file in String format : nullSafe
	 * @param iniToReadPath
	 * @param OPT_SECTION
	 * @param OPT_VAR
	 * @return
	 * @throws InvalidFileFormatException
	 * @throws IOException
	 */
	public static String readFromFile(String iniToReadPath, String OPT_SECTION, String OPT_VAR) throws InvalidFileFormatException, IOException {
		String valueRead = "";
		if (iniToReadPath != null && new File(iniToReadPath).exists()) {
			Ini tempIniFile = new Wini(new File(iniToReadPath));
			valueRead = StringWorker.trimToEmpty(tempIniFile.get(OPT_SECTION, OPT_VAR));
		}
		
		return valueRead;
	}
	
	/**
	 * Read a value from loaded INI file in String format : nullSafe
	 * @param OPT_SECTION
	 * @param OPT_VAR
	 * @return
	 * @throws InvalidFileFormatException
	 * @throws IOException
	 */
	public String readFromFile(String OPT_SECTION, String OPT_VAR) throws InvalidFileFormatException, IOException {
		return readFromFile(iniReadFile.getFile().getCanonicalPath(), OPT_SECTION, OPT_VAR);
	}
	
	/**
	 * Writes a value inside a given file, returning true if value is effectively written
	 * @param iniToReadPath
	 * @param OPT_SECTION
	 * @param OPT_VAR
	 * @param OPT_VALUE
	 * @return
	 * @throws InvalidFileFormatException
	 * @throws IOException
	 */
	public static boolean writeToFile(String iniToReadPath, String OPT_SECTION, String OPT_VAR, String OPT_VALUE) throws InvalidFileFormatException, IOException {
		boolean valueWritten = false;
		if (iniToReadPath != null && new File(iniToReadPath).exists()) {
			Wini iniTempFile = new Wini(new File(iniToReadPath));
			iniTempFile.load();
			
			iniTempFile.put(OPT_SECTION, OPT_VAR, OPT_VALUE);
			iniTempFile.store();
			String result = readFromFile(iniToReadPath, OPT_SECTION, OPT_VAR);
			if(result != null && result.equals(OPT_VALUE)) {
				valueWritten = true;
			}
		}
		
		return valueWritten;
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
				logger.info(iniFile.getFile().getCanonicalPath()+" \n\n Single option saved -> "+OPT_SECTION+" - "+OPT_VAR+" : "+OPT_VALUE);
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
				logger.info(iniFile.getFile().getCanonicalPath()+" \n\n Single option saved -> "+OPT_SECTION+" - "+OPT_VAR+" : "+OPT_VALUE);
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
				logger.info(iniFile.getFile().getCanonicalPath()+" \n\n Single option saved -> "+OPT_SECTION+" - "+OPT_VAR+" : "+OPT_VALUE);
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
				logger.info(iniFile.getFile().getCanonicalPath()+" \n\n Single option saved -> "+OPT_SECTION+" - "+OPT_VAR+" : "+OPT_VALUE);
			} catch (IOException e) {
				OK = false;
				logger.error("Exception happened!", e);
			}
		}
		return OK;
	}
	
	public String getSafeStringConf(String section, String variable, String defIfBlank) {
		String result = iniReadFile.get(section, variable);
		result = (StringWorker.isEmpty(result))? defIfBlank : result;
		
		return result;
	}

	public File getSafeFileConf(String section, String variable, String defPathIfBlank) {
		File result = new File(getSafeStringConf(section, variable, defPathIfBlank));
		result = (result == null || !result.exists() || !result.isFile()) ? new File(defPathIfBlank) : result;
		
		return result;
	}
	
	public boolean getBooleanFromString(String value, boolean defIfBlank) {
		boolean result = false;
		if(value == null || "".equals(value)) {
			result = defIfBlank;
		}else {
			result = Boolean.valueOf(value);
		}
		
		return result;
	}
	
	public static int getIntVarFromString(String val, int defaultVal) {
		try {
			return Integer.valueOf(val);
		}catch(Exception e){
			return defaultVal;
		}
	}
	
	public String listToString(ArrayList<String> loadedFiles) {
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
	
	@Override
	public String toStringa() {
		List<Field> list = ReflectionUtils.retrieveAllFields(getClass());
		StringBuilder builder = new StringBuilder();
		for (Field i : list) {
			builder.append(i.toGenericString());
		}
		
		return builder.toString();
	};
	
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
	
	// Utility methods //
	
	 /**
     * parse font from a representing string
     */
    public static Font getFontFromString(String stringFont) {
    	return ConversionUtils.getFontFromString(stringFont);
    }
    
    /**
     * parse font from a representing string
     */
    public static Font getFontFromString(String stringFont, Font defaultFont) {
    	return ConversionUtils.getFontFromString(stringFont, defaultFont);
    }
    
    /**
     * return font in string format
     * @return
     */
    public static String getFontString(Font srcFont) {
    	return ConversionUtils.getFontString(srcFont);
    }
    
    /**
     * create color from String RGB separed with "-" representation (red-green-blue)
     */
    public static Color getRgbColorFromString(String color) {
    	return ConversionUtils.getRgbColorFromString(color);
    }
    
    /**
     * Convert color to string RGB
     */
    public static String getRgbString(Color sourceColor) {
    	return ConversionUtils.getRgbString(sourceColor, "-");
    }
}

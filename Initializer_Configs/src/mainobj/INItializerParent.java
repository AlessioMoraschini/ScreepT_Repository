package mainobj;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.ini4j.Ini;
import org.ini4j.Wini;

import utility.reflection.ReflectionUtils;

public abstract class INItializerParent implements INItializerInterface{

	static Logger logger = Logger.getLogger(INItializerParent.class); 
	
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
		result = (result == null || "".equals(result))? defIfBlank : result;
		
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
	
	public static int getIntVarFromConf(String val, int defaultVal) {
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
    	String[] font = stringFont.split("-");
    	String type;
    	int style;
    	int size;
    	try {
			if (font.length != 3) {
				return new Font("Segoe UI", Font.PLAIN, 18);
			}
			type = font[0];
			style = Integer.parseInt(font[1]);
			size = Integer.parseInt(font[2]);
		} catch (Exception e) {
			return new Font("Segoe UI", Font.PLAIN, 18);
		}
    	
    	return new Font(type, style, size);
    }
    
    /**
     * parse font from a representing string
     */
    public static Font getFontFromString(String stringFont, Font defaultFont) {
    	String[] font = stringFont.split("-");
    	String type;
    	int style;
    	int size;
		try {
			if (font.length != 3) {
				return defaultFont;
			}
			type = font[0];
			style = Integer.parseInt(font[1]);
			size = Integer.parseInt(font[2]);
		} catch (Exception e) {
			return defaultFont;
		}
    	
    	return new Font(type, style, size);
    }
    
    /**
     * return font in string format
     * @return
     */
    public static String getFontString(Font srcFont) {
    	String stringFont = srcFont.getFontName()+"-"+srcFont.getStyle()+"-"+srcFont.getSize();
    	return stringFont;
    }
    
    /**
     * create color from String RGB separed with "-" representation (red-green-blue)
     */
    public static Color getRgbColorFromString(String color) {
    	String[] rgb = color.split("-");
    	if(rgb.length != 3) {
    		return new Color(0,0,0);
    	}
    	int red = Integer.parseInt(rgb[0]);
    	int green = Integer.parseInt(rgb[1]);
    	int blue = Integer.parseInt(rgb[2]);
    	
    	return new Color(red, green, blue);
    }
    
    /**
     * Convert color to string RGB
     */
    public static String getRgbString(Color sourceColor) {
    	StringBuilder builder = new StringBuilder();
    	builder.append(sourceColor.getRed()).append("-")
    		   .append(sourceColor.getGreen()).append("-")
    		   .append(sourceColor.getBlue());
    	String result = builder.toString();
    	return result;
    }
}

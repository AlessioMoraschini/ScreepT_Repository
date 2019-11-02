package mainobj;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.ini4j.Ini;
import org.ini4j.Wini;

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
}

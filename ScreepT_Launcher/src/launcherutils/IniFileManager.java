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
package launcherutils;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.ini4j.InvalidFileFormatException;
import org.ini4j.Wini;

import gui.utils.LoggerUtils;
import utility.string.StringWorker;

public class IniFileManager {
	
	// ConfigurationManager logger
	static Logger logger = Logger.getLogger(IniFileManager.class); 
	
	// INI SECTIONS
	public static final String INI_JVM_OPT = "JVM_OPTIONS";
	public static final String INI_APP_OPT = "APPLICATION_OPTIONS";
	
	// INI FIELDS
	public boolean viewShellAfterStart = false;
	public String DEFAULT_INI_PATH = "Launcher_Configuration.ini"; 
	
	public String preferredJvmVersionIfNotSpecified = "1.8"; // default selected if no user preferred is specified
	public String preferredJvm = "";
	public String manualPathJvm = "";
	public static String MIN_JRE_VERSION = "1.8"; // default included MIN
	public static String MAX_JRE_VERSION = "1.9"; // default excluded MAX
	public static String MAX_JRE_VERSION_FULL_COMPATIBILITY = "1.9"; // default excluded MAX
	public String GUI_SCALE_ARG_VALUE = "1";
	public String JVM_MEMORY_OPTIONS = "-Xmx2048m -Xms512m"; // default memory options
	public String JVM_STACK_SIZE = "-Xss128m"; // default stack size
	// main application path default
	public String MAIN_JAR_FILE = "MainAppToLaunch.jar";
	
	public Level logLevel = Level.INFO;
	
	public boolean showInstallOptions = true;
	public boolean hideNStartDirect = false;
	
	public Wini iniConfigFile;
	
	public IniFileManager(String INI_PATH) {
		// INIt for null safe
		iniConfigFile = new Wini();
		DEFAULT_INI_PATH = INI_PATH;
	}

	// read configuration from ini file with null-value check
	public void initFromINIFile(){
		try {
			logger.info("Loading INI launcher_configuration file...");
			
			iniConfigFile = new Wini(new File(DEFAULT_INI_PATH));
			
			// JVM OPTIONS WITH NULL CHECK
			String MAX_JVERSION_INI = iniConfigFile.get(INI_JVM_OPT, "maxJvmVersion");
			MAX_JRE_VERSION = (MAX_JVERSION_INI != null)? MAX_JVERSION_INI : MAX_JRE_VERSION;
			String MAX_JVERSION_FULL_COMP_INI = iniConfigFile.get(INI_JVM_OPT, "maxJvmVersionFullCompatibility");
			MAX_JRE_VERSION_FULL_COMPATIBILITY = (MAX_JVERSION_FULL_COMP_INI != null)? MAX_JVERSION_FULL_COMP_INI : MAX_JRE_VERSION_FULL_COMPATIBILITY;
			String MIN_JVERSION_INI = iniConfigFile.get(INI_JVM_OPT, "minJvmVersion");
			MIN_JRE_VERSION = (MIN_JVERSION_INI != null)? MIN_JVERSION_INI : MIN_JRE_VERSION;
			String MEM_JOPTIONS_INI = iniConfigFile.get(INI_JVM_OPT, "memoryArgs");
			JVM_MEMORY_OPTIONS = (MEM_JOPTIONS_INI != null)? MEM_JOPTIONS_INI : JVM_MEMORY_OPTIONS;
			String MEM_STACK_INI = " -Xss" + iniConfigFile.get(INI_JVM_OPT, "stackSizeArgs")+"m";
			JVM_STACK_SIZE = (MEM_STACK_INI != null)? MEM_STACK_INI : JVM_STACK_SIZE;
			
			// APPLICATION OPTIONS WITH NULL CHECK
			String GUI_SCALE = iniConfigFile.get(INI_APP_OPT, "scaleGuiPerc").replace(",", ".");
			GUI_SCALE_ARG_VALUE = (GUI_SCALE == null || GUI_SCALE.equals(""))? GUI_SCALE_ARG_VALUE : GUI_SCALE;
			String viewShell = iniConfigFile.get(INI_APP_OPT, "viewCmdConsole");
			viewShellAfterStart = (!"".equals(viewShell) && Boolean.parseBoolean(viewShell))? true : false;
			String MAIN_JAR_INI = iniConfigFile.get(INI_APP_OPT, "jarToLaunch");
			MAIN_JAR_FILE = (MAIN_JAR_INI != null)? MAIN_JAR_INI : MAIN_JAR_FILE;
			String autoInstallerOpt = iniConfigFile.get(INI_APP_OPT, "autoShowInstallOpt");
			showInstallOptions = (autoInstallerOpt!=null && autoInstallerOpt.equalsIgnoreCase("false"))? false : true;
			String hideNStart = iniConfigFile.get(INI_APP_OPT, "flagHideNStartDirect");
			hideNStartDirect = (hideNStart!=null && hideNStart.equalsIgnoreCase("true"))? true : false;
			preferredJvm = StringWorker.trimToEmpty(iniConfigFile.get(IniFileManager.INI_JVM_OPT, "preferredJvm"));
			preferredJvmVersionIfNotSpecified = StringWorker.trimToEmpty(iniConfigFile.get(IniFileManager.INI_JVM_OPT, "preferredJvmVersionIfNotSpecified"));
			manualPathJvm = StringWorker.trimToEmpty(iniConfigFile.get(IniFileManager.INI_JVM_OPT, "manualPathJvm"));
			
			String logLevelStr = iniConfigFile.get(IniFileManager.INI_APP_OPT, "logLevelSetting");
			logLevel = LoggerUtils.getLogLevelFromString(logLevelStr != null ? logLevelStr : "INFO");
			
			logger.info("Launcher_configuration file loaded!");
		} catch (InvalidFileFormatException e) {
			logger.error("Invalid format detected while trying to read " + DEFAULT_INI_PATH, e);
		} catch (IOException e) {
			logger.error("IO error while trying to read " + DEFAULT_INI_PATH, e);
		}
	}
	
	/**
	 * use this function to write a single value to launcher configuration file
	 * @param OPT_SECTION
	 * @param OPT_VAR
	 * @param OPT_VALUE
	 * @return
	 */
	public synchronized boolean writeValueToIniConfiguration(String OPT_SECTION, String OPT_VAR, String OPT_VALUE ) {
		boolean OK = false;
		
		if (iniConfigFile != null && iniConfigFile.getFile().exists()) {
			try {
				iniConfigFile.put(OPT_SECTION, OPT_VAR, OPT_VALUE);
				iniConfigFile.store();
				OK = true;
			} catch (IOException e) {
				OK = false;
				logger.error("Exception happened writing value to LAUNCHER ini configuration file!\n\n", e);
			} 
		}
		return OK;
	}
	
	public static String[] getLogLevelsList() {
		String[] levelArray = {"ALL","INFO","DEBUG","WARN","ERROR","FATAL","OFF"};
		return levelArray;
	}
}

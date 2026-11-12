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
package screept.launcher.utils;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.ini4j.InvalidFileFormatException;
import org.ini4j.Wini;

import screept.launcher.gui.utils.LauncherLoggerUtils;
import various.common.light.utility.string.StringWorker;

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
	/** Newest Java (excluded) this ScreepT supports: an older limit written in a launcher ini by previous versions (12) is ignored */
	public static final String SUPPORTED_MAX_JRE_VERSION = "22";
	public static String MAX_JRE_VERSION = "22"; // default excluded MAX
	public static String MAX_JRE_VERSION_FULL_COMPATIBILITY = "22"; // default excluded MAX
	public String GUI_SCALE_ARG_VALUE = "1";
	/** Default maximum memory of ScreepT, in MB (1.5 GB) */
	public static final long DEFAULT_MAX_MEMORY_MB = 1536;
	public String JVM_MEMORY_OPTIONS = "-Xmx" + DEFAULT_MAX_MEMORY_MB + "m -Xms512m"; // default memory options
	public String JVM_STACK_SIZE = "-Xss128m"; // default stack size
	// main application path default
	public String MAIN_JAR_FILE = "MainAppToLaunch.jar";
	
	public Level logLevel = Level.INFO;
	
	public boolean showInstallOptions = true;
	public boolean hideNStartDirect = false;
	/** Set (once per run) when the application asked to see the launcher at this start only: see flagShowLauncherOnce */
	public static boolean showLauncherOnce = false;
	
	public Wini iniConfigFile;
	
	public IniFileManager(String INI_PATH) {
		// INIt for null safe
		iniConfigFile = new Wini();
		DEFAULT_INI_PATH = INI_PATH;
	}

	private static String atLeastSupportedMax(String fromIni) {
		try {
			return StringWorker.compareVersions(fromIni, SUPPORTED_MAX_JRE_VERSION) < 0 ? SUPPORTED_MAX_JRE_VERSION : fromIni;
		} catch (RuntimeException e) {
			return SUPPORTED_MAX_JRE_VERSION;
		}
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
			MAX_JRE_VERSION = atLeastSupportedMax(MAX_JRE_VERSION);
			MAX_JRE_VERSION_FULL_COMPATIBILITY = atLeastSupportedMax(MAX_JRE_VERSION_FULL_COMPATIBILITY);
			String MIN_JVERSION_INI = iniConfigFile.get(INI_JVM_OPT, "minJvmVersion");
			MIN_JRE_VERSION = (MIN_JVERSION_INI != null)? MIN_JVERSION_INI : MIN_JRE_VERSION;
			String MEM_JOPTIONS_INI = iniConfigFile.get(INI_JVM_OPT, "memoryArgs");
			JVM_MEMORY_OPTIONS = (MEM_JOPTIONS_INI != null)? MEM_JOPTIONS_INI : JVM_MEMORY_OPTIONS;
			migrateMemoryDefault();
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
			// one-time request written by the application (restart with launcher): consumed here, so it applies to this start only
			String showOnce = iniConfigFile.get(INI_APP_OPT, "flagShowLauncherOnce");
			if (showOnce != null && showOnce.equalsIgnoreCase("true")) {
				showLauncherOnce = true;
				iniConfigFile.put(INI_APP_OPT, "flagShowLauncherOnce", "false");
				iniConfigFile.store();
			}
			preferredJvm = StringWorker.trimToEmpty(iniConfigFile.get(IniFileManager.INI_JVM_OPT, "preferredJvm"));
			preferredJvmVersionIfNotSpecified = StringWorker.trimToEmpty(iniConfigFile.get(IniFileManager.INI_JVM_OPT, "preferredJvmVersionIfNotSpecified"));
			manualPathJvm = StringWorker.trimToEmpty(iniConfigFile.get(IniFileManager.INI_JVM_OPT, "manualPathJvm"));
			
			String logLevelStr = iniConfigFile.get(IniFileManager.INI_APP_OPT, "logLevelSetting");
			logLevel = LauncherLoggerUtils.getLogLevelFromString(logLevelStr != null ? logLevelStr : "INFO");
			
			logger.info("Launcher_configuration file loaded!");
		} catch (InvalidFileFormatException e) {
			logger.error("Invalid format detected while trying to read " + DEFAULT_INI_PATH, e);
		} catch (IOException e) {
			logger.error("IO error while trying to read " + DEFAULT_INI_PATH, e);
		}
	}
	
	/**
	 * One time only, at the first start after the update that made 1.5 GB the default: the maximum memory becomes 1.5 GB, or
	 * the most the PC can give (80% of its memory) if that is less. A marker in the launcher settings makes sure the following
	 * updates never reset it again, so a value chosen afterwards (for instance with the pencil in the menu bar) is kept.
	 */
	private void migrateMemoryDefault() {
		try {
			String done = iniConfigFile.get(INI_JVM_OPT, "memoryDefaultMigrated");
			if (done != null && done.trim().equalsIgnoreCase("true")) {
				return;
			}
			long targetMb = DEFAULT_MAX_MEMORY_MB;
			java.lang.management.OperatingSystemMXBean os = java.lang.management.ManagementFactory.getOperatingSystemMXBean();
			if (os instanceof com.sun.management.OperatingSystemMXBean) {
				long totalMb = ((com.sun.management.OperatingSystemMXBean) os).getTotalPhysicalMemorySize() / (1024L * 1024L);
				if (totalMb > 0) {
					targetMb = Math.min(targetMb, Math.max(256, totalMb * 80 / 100));
				}
			}
			String args = JVM_MEMORY_OPTIONS == null ? "" : JVM_MEMORY_OPTIONS;
			java.util.regex.Matcher max = java.util.regex.Pattern.compile("-Xmx\\d+[mMgG]").matcher(args);
			args = max.find() ? max.replaceFirst("-Xmx" + targetMb + "m") : ("-Xmx" + targetMb + "m " + args).trim();
			java.util.regex.Matcher initial = java.util.regex.Pattern.compile("-Xms(\\d+)([mMgG])").matcher(args);
			if (initial.find()) {
				long initialMb = Long.parseLong(initial.group(1)) * (initial.group(2).equalsIgnoreCase("g") ? 1024 : 1);
				if (initialMb > targetMb) {
					args = initial.replaceFirst("-Xms" + targetMb + "m");
				}
			}
			JVM_MEMORY_OPTIONS = args;
			iniConfigFile.put(INI_JVM_OPT, "memoryArgs", args);
			iniConfigFile.put(INI_JVM_OPT, "memoryDefaultMigrated", "true");
			iniConfigFile.store();
			logger.info("Maximum memory set once to " + targetMb + " MB: " + args);
		} catch (Exception e) {
			logger.error("Cannot apply the default memory", e);
		}
	}

	/** True when the launcher must not be shown at this start: hidden by configuration and not asked once by the application */
	public boolean isHiddenForThisStart() {
		return hideNStartDirect && !showLauncherOnce;
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

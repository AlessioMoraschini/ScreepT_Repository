package resources;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Hashtable;
import java.util.Properties;

import javax.swing.ImageIcon;

import org.apache.commons.lang.StringUtils;

public class GeneralConfig {
	
	// general properties file containing mappings (this way you can change them without rebuild)
	public static final String resourcesMappingDefPath = "resourcesFilePathsMappings.properties";
	public static PropertiesManager resourcesFilePathsMappings = new PropertiesManager(resourcesMappingDefPath);
	public static Properties linkedMappings = resourcesFilePathsMappings.getDynamicFileBindingsDef();
	
	public static final SimpleDateFormat GENERIC_FORMATTER = new SimpleDateFormat("yyyy/MM/dd - hh:mm:ss");
	

	// MAIN PROPERTIES
	public static final String APP_VERSION = linkedMappings.getProperty("APP_VERSION");
	public static final String WEBSITE = "www.am_design_developement.com";
	public static final String APPLICATION_NAME = "ScrYpT - V" + APP_VERSION;
	public static final String AUTHOR = "A.M. Design and Developement";
	public static final String BUILD_TS_DATE = linkedMappings.getProperty("BUILD_TIMESTAMP")
										.replaceAll("T", " at ")
										.replaceAll("Z", "")
										.replaceAll("-", "/");
	
	// General Folders
	public static final String RESOURCES_DIR = linkedMappings.getProperty("RESOURCES_DIR");
	public static final String DEFAULT_IMAGE_FOLDER = linkedMappings.getProperty("DEFAULT_IMAGE_FOLDER");
	public static final String SOUNDS_FOLDER = linkedMappings.getProperty("SOUNDS_FOLDER");
	public static final String WORDLIST_FOLDER = linkedMappings.getProperty("WORDLIST_FOLDER");
	public static final String DEFAULT_INI_FOLDER = linkedMappings.getProperty("DEFAULT_INI_FOLDER");
	public static final String THEMES_FOLDER_DEFAULT = linkedMappings.getProperty("THEMES_FOLDER_DEFAULT");
	public static final String THEMES_FOLDER_CUSTOM = linkedMappings.getProperty("THEMES_FOLDER_CUSTOM");
	
	// DEBUG GUI FLAG
	public static String debugPreRead = linkedMappings.getProperty("FLAG_DEBUG_GRAPHICS");
	private static final boolean FLAG_DEBUG_GRAPHICS = (debugPreRead!= null && debugPreRead.equalsIgnoreCase("true"))? true : false;
	public static String DEBUG_GRAPHICS = (FLAG_DEBUG_GRAPHICS)? "debug," : "";
	
	// Temp Folders and Files
	public static final String TEMP_DECRIPTED_ROOT_PATH = System.getProperty("user.home")+"/Desktop/ScrYpT_decrypted_tempFiles";
	public static final File TEMP_DECRIPTED_ROOT = new File(TEMP_DECRIPTED_ROOT_PATH);
	public static final String TEMP_DIRECTORY = RESOURCES_DIR + "temp/";
	public static final File TEMP_DIRECTORY_FILE = new File(TEMP_DIRECTORY);
	
	// Ini files configuration
	public static final String DEFAULT_INI= linkedMappings.getProperty("DEFAULT_INI");
	public static final String BACKUP_INI= linkedMappings.getProperty("BACKUP_INI");
	
	// Readme File
	public static final String SCRYPT_TUTORIAL_HTML_PATH = RESOURCES_DIR + "Readme/ReadMe.html";
	public static final String SCRYPT_TUTORIAL_HTML_URL = "File:./" + SCRYPT_TUTORIAL_HTML_PATH;

	// Logs configuration
	public static final String LOG_PATH = linkedMappings.getProperty("LOG_PATH");
	public static final String LOG_PROPERTIES_PATH = linkedMappings.getProperty("LOG_PROPERTIES_PATH");
	public static final String LOG_LAUNCHER_PATH = linkedMappings.getProperty("LOG_LAUNCHER_PATH");
	
	// JVM and Launcher
	public static final String MAX_JVM_COMPLETE_COMPATIBILITY = linkedMappings.getProperty("MAX_JVM_COMPLETE_COMPATIBILITY");
	public static final String JVM_CUSTOM_CUSTOM_SETTINGS = linkedMappings.getProperty("JVM_CUSTOM_CUSTOM_SETTINGS");
	
	/**
	 * Used in launcher to choose right type of launch command for system
	 */
	public static final boolean FLAG_LAUNCH_JAR_COMMAND = Boolean.valueOf(linkedMappings.getProperty("FLAG_LAUNCH_JAR_COMMAND"));
	public static final String MAIN_GENERAL_CLASSPATH = "./*;./Resources_Scrypt/lib/* ";
	public static final String MAIN_GENERAL_STARTER_PACKAGE = "maingUI.MainGuiGeneralAppStarter";

	// NETWORK OPTIONS
	public static final int NETWORK_UTILS_TIMEOUT  = getIntVarFromProps("NETWORK_UTILS_TIMEOUT", 5000);
	public static final String testAddress = linkedMappings.getProperty("testAddress");
	
	// Tooltips
	public static int TOOLTIP_DELAY;
	public static int TOOLTIP_TIMEOUT;
	public static String TOOLTIP_FONT = linkedMappings.getProperty("TOOLTIP_FONT");
	
	static {
		try {
			TOOLTIP_DELAY = Integer.parseInt(linkedMappings.getProperty("TOOLTIP_DELAY"));// 0,4 seconds
			TOOLTIP_TIMEOUT = Integer.parseInt(linkedMappings.getProperty("TOOLTIP_TIMEOUT")); // 30 seconds
		}catch(Exception e) {
			TOOLTIP_DELAY = 400;// 0,4 seconds
			TOOLTIP_TIMEOUT = 30000; // 30 seconds
			TOOLTIP_FONT = "";
		}
	}
	
	// TEXT EDITOR DEFAULTS
	public static final boolean TEXT_EDITOR_BACKUP_BEFORE_SWITCH_ENABLED = Boolean.valueOf(linkedMappings.getProperty("TEXT_EDITOR_BACKUP_BEFORE_SWITCH_ENABLED"));
	
	public static final Dimension MINIMUM_BUTTON_SIZE = new Dimension(18, 18);
	public static final Font BUTTON_DEFAULT_FONT =  new Font("Segoe UI", Font.BOLD, 12);
	public static final Font BUTTON_TAB_HEAD_FONT =  new Font("Segoe UI", Font.PLAIN, 14);
	public static final Font DEFAULT_FONT = new Font("Segoe UI", Font.PLAIN, 16);
	public static final Color BUTTON_BCKG_COLOR_TXT_EDITOR = new Color(120, 125, 140);
	public static final Color BUTTON_BCKG_COLOR_TXT_EDITOR_DARKER = new Color(71, 76, 84);
	public static final int TEXT_EDITOR_FOOTER_REFRESH_PERIOD_MS = getIntVarFromProps("TEXT_EDITOR_FOOTER_REFRESH_PERIOD_MS", 300);
	
	public static int FOOTER_MAX_DIR_LABEL_LENGTH;
	static {
		try {
			FOOTER_MAX_DIR_LABEL_LENGTH = Integer.parseInt(linkedMappings.getProperty("FOOTER_MAX_DIR_LABEL_LENGTH"));
		}catch(Exception e) {
			FOOTER_MAX_DIR_LABEL_LENGTH = 30;
		}
	}
	public static int DEFAULT_FILE_TREE_ANCESTOR_LEVEL;
	static {
		try {
			DEFAULT_FILE_TREE_ANCESTOR_LEVEL = Integer.parseInt(linkedMappings.getProperty("DEFAULT_FILE_TREE_ANCESTOR_LEVEL"));
		}catch(Exception e) {
			DEFAULT_FILE_TREE_ANCESTOR_LEVEL = 2;
		}
	}
	
	///////////////////////////////////////////////// METHODS ///////////////////////////////////////////////
	
	/**
	 * Properties reloader
	 */
	public static void reloadProperties () {
		resourcesFilePathsMappings = new PropertiesManager(resourcesMappingDefPath);
		linkedMappings = resourcesFilePathsMappings.getDynamicFileBindingsDef();
	}
	
	public static String[] getPubIpProvList() {
		String list = linkedMappings.getProperty("PUB_IP_PROV_LIST");
		String[] providers = list.split(",");
		
		return providers;
	}
	
	private static int getIntVarFromProps(String key, int defaultVal) {
		String val = linkedMappings.getProperty(key);
		try {
			return Integer.valueOf(val);
		}catch(Exception e){
			return defaultVal;
		}
	}
	
}

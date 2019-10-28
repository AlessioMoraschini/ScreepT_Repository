package resources;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import resources.om.FilePojo;

public class GeneralConfig {
	
	// general properties file containing mappings (this way you can change them without rebuild)
	public static final String resourcesMappingDefPath = "resourcesFilePathsMappings.properties";
	public static PropertiesManager resourcesFilePathsMappings = new PropertiesManager(resourcesMappingDefPath);
	public static Properties linkedMappings = resourcesFilePathsMappings.getDynamicFileBindingsDef();
	
	public static final SimpleDateFormat GENERIC_FORMATTER = new SimpleDateFormat("yyyy/MM/dd - hh:mm:ss");
	
	// TEST ERROR CATCH
	public static final boolean FLAG_THROW_ERROR_TEST_ON = Boolean.valueOf(resourcesFilePathsMappings.getProperty("FLAG_THROW_ERROR_TEST_ON"));
	public static final int MAX_N_OF_ERR_DIALOG_OPENED = resourcesFilePathsMappings.getIntVarFromProps("MAX_N_OF_ERR_DIALOG_OPENED", 2);
	public static final int AWT_EVENT_CHECKER_DECREASE_LIMIT_PERIOD = resourcesFilePathsMappings.getIntVarFromProps("AWT_EVENT_CHECKER_DECREASE_LIMIT_PERIOD", 5000);
	
	// MAIN PROPERTIES
	public static final String APP_VERSION = resourcesFilePathsMappings.getProperty("APP_VERSION");
	public static final String WEBSITE = "www.am_design_development.com";
	public static final String APPLICATION_NAME = "ScrYpT - V" + APP_VERSION;
	public static final String AUTHOR = "A.M. Design and Development";
	public static final String BUILD_TS_DATE = resourcesFilePathsMappings.getProperty("BUILD_TIMESTAMP")
										.replaceAll("T", " at ")
										.replaceAll("Z", "")
										.replaceAll("-", "/");
	// General Folders
	public static final String RESOURCES_DIR = resourcesFilePathsMappings.getProperty("RESOURCES_DIR");
	public static final String DEFAULT_IMAGE_FOLDER = resourcesFilePathsMappings.getProperty("DEFAULT_IMAGE_FOLDER");
	public static final String SOUNDS_FOLDER = resourcesFilePathsMappings.getProperty("SOUNDS_FOLDER");
	public static final String WORDLIST_FOLDER = resourcesFilePathsMappings.getProperty("WORDLIST_FOLDER");
	public static final String DEFAULT_INI_FOLDER = resourcesFilePathsMappings.getProperty("DEFAULT_INI_FOLDER");
	public static final String THEMES_FOLDER_DEFAULT = resourcesFilePathsMappings.getProperty("THEMES_FOLDER_DEFAULT");
	public static final String THEMES_FOLDER_CUSTOM = resourcesFilePathsMappings.getProperty("THEMES_FOLDER_CUSTOM");
	public static final String PANEL_FILES_FOLDER = resourcesFilePathsMappings.getProperty("PANEL_TEMP_FOLDER");
	public static final String TEMPLATES_FOLDER = resourcesFilePathsMappings.getProperty("TEMPLATES_FOLDER");
	public static final String CLIBPOARDS_PANEL_SRC_FOLDER = resourcesFilePathsMappings.getProperty("CLIBPOARDS_PANEL_SRC_FOLDER");
	public static final String JS_LIB_FOLDER = resourcesFilePathsMappings.getProperty("JS_LIB_FOLDER") != null? resourcesFilePathsMappings.getProperty("JS_LIB_FOLDER") : "/Resources_Scrypt/lib/js/";
	// Temp Folders and Files
	public static final String TEMP_DIRECTORY = RESOURCES_DIR + "temp/";
	public static final File TEMP_DIRECTORY_FILE = new File(TEMP_DIRECTORY);
	
	// IMPORTANT FOLDERS
	public static final String USER_PERSONAL_DIR = System.getProperty("user.home")+"/";
	public static final String USER_PERSONAL_DIR_SCRYPT_FOLDER = USER_PERSONAL_DIR + APPLICATION_NAME + File.separatorChar + APP_VERSION;
	public static final File F_USER_PERSONAL_DIR_SCRYPT_FOLDER = new File(USER_PERSONAL_DIR_SCRYPT_FOLDER);
	public static final String USER_DESKTOP = USER_PERSONAL_DIR+"Desktop/";
	public static final String TEMP_DECRIPTED_ROOT_PATH = USER_DESKTOP + "ScrYpT_decrypted_tempFiles";
	public static final File TEMP_DECRIPTED_ROOT = new File(TEMP_DECRIPTED_ROOT_PATH);
	/**
	 * This file is created at SW start, and deleted on close to signal other external programs
	 */
	public static final File SCRYPT_ACTIVE_SIGNAL_FILE = new File(USER_PERSONAL_DIR_SCRYPT_FOLDER + File.separatorChar + "mainApplicationSignalFile.scrypt");
	
	// Ini files configuration
	public static final String DEFAULT_INI= resourcesFilePathsMappings.getProperty("DEFAULT_INI");
	public static final String BACKUP_INI= resourcesFilePathsMappings.getProperty("BACKUP_INI");
	
	// Readme File
	public static final String SCRYPT_TUTORIAL_HTML_PATH = RESOURCES_DIR + "Readme/ReadMe.html";
	public static final String SCRYPT_TUTORIAL_HTML_URL = "File:./" + SCRYPT_TUTORIAL_HTML_PATH;

	// Logs configuration
	public static final String LOG_PATH = resourcesFilePathsMappings.getProperty("LOG_PATH");
	public static final String LOG_PROPERTIES_PATH = resourcesFilePathsMappings.getProperty("LOG_PROPERTIES_PATH");
	public static final String LOG_LAUNCHER_PATH = resourcesFilePathsMappings.getProperty("LOG_LAUNCHER_PATH");
	
	// JVM and LAUNCHER
	public static final String MAX_JVM_COMPLETE_COMPATIBILITY = resourcesFilePathsMappings.getProperty("MAX_JVM_COMPLETE_COMPATIBILITY");
	public static final String JVM_CUSTOM_CUSTOM_SETTINGS = resourcesFilePathsMappings.getProperty("JVM_CUSTOM_CUSTOM_SETTINGS");
	
	/**
	 * Used in launcher to choose right type of launch command for system
	 */
	public static final boolean FLAG_LAUNCH_JAR_COMMAND = Boolean.valueOf(resourcesFilePathsMappings.getProperty("FLAG_LAUNCH_JAR_COMMAND"));
	public static final String MAIN_GENERAL_CLASSPATH = "./*;./Resources_Scrypt/lib/* ";
	public static final String MAIN_GENERAL_STARTER_PACKAGE = "maingUI.MainGuiGeneralAppStarter";
	
	// LAUNCHER DEFAULTS
	public static final int DEFAULT_JVM_XMX  = resourcesFilePathsMappings.getIntVarFromProps("DEFAULT_JVM_XMX", 768);
	public static final int DEFAULT_JVM_XMS  = resourcesFilePathsMappings.getIntVarFromProps("DEFAULT_JVM_XMS", 348);
	public static final int DEFAULT_JVM_XSS  = resourcesFilePathsMappings.getIntVarFromProps("DEFAULT_JVM_XSS", 100);
	public static final float DEFAULT_JVM_GUI  = resourcesFilePathsMappings.getFloatVarFromProps("DEFAULT_JVM_GUI", 1.0f);
	public static final String DEFAULT_JVM_VERS_MAX = resourcesFilePathsMappings.getProperty("DEFAULT_JVM_VERS_MAX");
	public static final String DEFAULT_JVM_VERS_MIN = resourcesFilePathsMappings.getProperty("DEFAULT_JVM_VERS_MIN");
	public static final String DEFAULT_LOG_LEVEL = resourcesFilePathsMappings.getProperty("DEFAULT_LOG_LEVEL");
	public static final boolean DEFAULT_JVM_JAR_MODE = Boolean.valueOf(resourcesFilePathsMappings.getProperty("DEFAULT_JVM_JAR_MODE"));
	public static final boolean DEFAULT_JVM_SHELL_ON = Boolean.valueOf(resourcesFilePathsMappings.getProperty("DEFAULT_JVM_SHELL_ON"));
	
	// NETWORK OPTIONS
	public static final int NETWORK_UTILS_TIMEOUT  = resourcesFilePathsMappings.getIntVarFromProps("NETWORK_UTILS_TIMEOUT", 5000);
	public static final String testAddress = resourcesFilePathsMappings.getProperty("testAddress");
	
	/**	GUI PROPERTIES **/
	
	// MENU CUSTOM FONTS
	public static final Font MENU_LASTFILES_DEFAULT_FONT =  new Font("Segoe UI", Font.PLAIN, 15);
	public static final Font MENU_LASTFILES_FONT =  resourcesFilePathsMappings.getFontFromString("MENU_LASTFILES_FONT", MENU_LASTFILES_DEFAULT_FONT);
	
	
	// DEBUG GUI FLAG
	public static String debugPreRead = resourcesFilePathsMappings.getProperty("FLAG_DEBUG_GRAPHICS");
	private static final boolean FLAG_DEBUG_GRAPHICS = (debugPreRead!= null && debugPreRead.equalsIgnoreCase("true"))? true : false;
	public static String DEBUG_GRAPHICS = (FLAG_DEBUG_GRAPHICS)? "debug," : "";
	
	// Tooltips
	public static int TOOLTIP_DELAY = resourcesFilePathsMappings.getIntVarFromProps("TOOLTIP_DELAY", 400);
	public static int TOOLTIP_TIMEOUT = resourcesFilePathsMappings.getIntVarFromProps("TOOLTIP_TIMEOUT", 30000);
	public static String TOOLTIP_FONT = resourcesFilePathsMappings.getProperty("TOOLTIP_FONT");
	
	// FILE CHOOSER DIALOG
	public static final Dimension DEFAULT_FILE_CHOOSER_SIZE = new Dimension(800, 600);
	public static final Dimension FILE_CHOOSER_SIZE = resourcesFilePathsMappings.getDimensionFromString("-", "FILE_CHOOSER_SIZE_STR", DEFAULT_FILE_CHOOSER_SIZE);
	
	// TEXT EDITOR DEFAULTS
	public static final int DEFAULT_TREE_ROW_HEIGHT = resourcesFilePathsMappings.getIntVarFromProps("DEFAULT_TREE_ROW_HEIGHT", 14);
	public static final int MAX_LAST_OPENED_LIST_SIZE = resourcesFilePathsMappings.getIntVarFromProps("MAX_LAST_OPENED_LIST_SIZE", 12);
	public static final int MAX_HISTORY_SIZE = resourcesFilePathsMappings.getIntVarFromProps("MAX_HISTORY_SIZE", 40);
	public static final boolean RECTANGULAR_TXT_SELECTION_ALLOWED = Boolean.valueOf(resourcesFilePathsMappings.getProperty("RECTANGULAR_TXT_SELECTION_ALLOWED"));
	public static final boolean PRELOAD_TXT_EDITOR_FLAG = Boolean.valueOf(resourcesFilePathsMappings.getProperty("PRELOAD_TXT_EDITOR_FLAG"));
	public static final Font TXT_EDITOR_MINIMAP_FONT_DEFAULT =  new Font("Segoe UI", Font.PLAIN, 5);
	public static Font TXT_EDITOR_MINIMAP_FONT = resourcesFilePathsMappings.getFontFromString("TXT_EDITOR_MINIMAP_FONT", TXT_EDITOR_MINIMAP_FONT_DEFAULT);
	
	public static final Font TXT_EDITOR_CLIPBOARD_FONT_DEFAULT =  new Font("Segoe UI", Font.PLAIN, 12);
	public static Font TXT_EDITOR_CLIPBOARD_FONT =  resourcesFilePathsMappings.getFontFromString("TXT_EDITOR_CLIPBOARD_FONT", TXT_EDITOR_CLIPBOARD_FONT_DEFAULT);
	
	public static final boolean TEXT_EDITOR_BACKUP_BEFORE_SWITCH_ENABLED = Boolean.valueOf(resourcesFilePathsMappings.getProperty("TEXT_EDITOR_BACKUP_BEFORE_SWITCH_ENABLED"));
	@Deprecated
	public static final String CLIPBOARD_PANEL_PREFIX = "Clipboard ";
	public static final String CLIPBOARD_PANEL_DEFAULT = "Clipboard.txt";
	public static final TreeMap<String, File> CLIPBOARD_PANEL_MAP = getRefreshedClipBoardsMap();
	
	public static final Dimension MINIMUM_BUTTON_SIZE = new Dimension(18, 18);
	public static final Font BUTTON_DEFAULT_FONT =  new Font("Segoe UI", Font.BOLD, 12);
	public static final Font BUTTON_TAB_HEAD_FONT =  new Font("Segoe UI", Font.PLAIN, 14);
	public static final Font DEFAULT_FONT = new Font("Segoe UI", Font.PLAIN, 16);
	public static final Color BUTTON_BCKG_COLOR_TXT_EDITOR = new Color(120, 125, 140);
	public static final Color BUTTON_BCKG_COLOR_TXT_EDITOR_DARKER = new Color(71, 76, 84);
	public static final int TEXT_EDITOR_FOOTER_REFRESH_PERIOD_MS = resourcesFilePathsMappings.getIntVarFromProps("TEXT_EDITOR_FOOTER_REFRESH_PERIOD_MS", 300);
	public static final int NORMAL_WHEEL_SPEED = resourcesFilePathsMappings.getIntVarFromProps("NORMAL_WHEEL_SPEED", 14);
	public static final int FAST_WHEEL_SPEED = resourcesFilePathsMappings.getIntVarFromProps("FAST_WHEEL_SPEED", 150);
	public static final int ULTRA_FAST_WHEEL_SPEED = resourcesFilePathsMappings.getIntVarFromProps("ULTRA_FAST_WHEEL_SPEED", 300);
	
	public static int FOOTER_MAX_DIR_LABEL_LENGTH = resourcesFilePathsMappings.getIntVarFromProps("FOOTER_MAX_DIR_LABEL_LENGTH", 30);
	public static int DEFAULT_FILE_TREE_ANCESTOR_LEVEL = resourcesFilePathsMappings.getIntVarFromProps("DEFAULT_FILE_TREE_ANCESTOR_LEVEL", 2);
	
	//// CONSTRUCTOR ////
	public GeneralConfig() {
		File propertiesSrc = new File(resourcesMappingDefPath);
		if(!propertiesSrc.exists()) {
			try {
				propertiesSrc.createNewFile();
			} catch (IOException e) {
			}
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
		String list = resourcesFilePathsMappings.getProperty("PUB_IP_PROV_LIST");
		String[] providers = list.split(",");
		
		return providers;
	}
	
    public static File[] getClipboardsFiles(){
    	File[] clipboards = new File(CLIBPOARDS_PANEL_SRC_FOLDER).listFiles(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				
				// Accept only files
				if(dir.isDirectory()) {
					return false;
				}else {
					return true;
				}
			}
		});
    	
    	return clipboards;
    }
    
    @Deprecated
    public static TreeMap<String, File>  getMapFromCommaSeparatedFiles(String src) {
    	TreeMap<String, File> map = new TreeMap<String, File>();
    	src = StringUtils.trimToEmpty(src);
    	String[] commaSep = src.split(",");
    	int i = 0;
    	for(String curr : commaSep) {
    		curr = PANEL_FILES_FOLDER + curr;
    		map.put(CLIPBOARD_PANEL_PREFIX+i, new File(curr));
    		++i;
    	}
    	return map;
    }
    
    
    public static TreeMap<String, File> getRefreshedClipBoardsMap(){
    	return FilePojo.mapFilesFromDir(new File(CLIBPOARDS_PANEL_SRC_FOLDER));
    }
    
    public static boolean presentInClipboardMap(String key) {
    	if(CLIPBOARD_PANEL_MAP.containsKey(key)) {
    		return true;
    	}
    	return false;
    }
    
    /**
     * This method creates directory in user system folder to put shared scrypt's files
     * @return
     */
    public static boolean mkDirUserDirScrYpT() {
    	try {
    		File userDirScryptFolder = new File(USER_PERSONAL_DIR_SCRYPT_FOLDER); 
			if (!userDirScryptFolder.exists()) {
				userDirScryptFolder.mkdirs();
			}
			return true;
		} catch (Exception e) {
			return false;
		}
    }
}

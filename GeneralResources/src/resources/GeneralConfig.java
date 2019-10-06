package resources;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Hashtable;
import java.util.Properties;

import javax.swing.ImageIcon;

public class GeneralConfig {
	
	// general properties file containing mappings (this way you can change them without rebuild)
	public static final String resourcesMappingDefPath = "resourcesFilePathsMappings.properties";
	public static PropertiesManager resourcesFilePathsMappings = new PropertiesManager(resourcesMappingDefPath);
	public static Properties linkedMappings = resourcesFilePathsMappings.getDynamicFileBindingsDef();
	
	public static final SimpleDateFormat GENERIC_FORMATTER = new SimpleDateFormat("yyyy/MM/dd - hh:mm:ss");
	
	/**
	 * Used in launcher to choose right type of launch command for system
	 */
	public static final boolean FLAG_LAUNCH_JAR_COMMAND = Boolean.valueOf(linkedMappings.getProperty("FLAG_LAUNCH_JAR_COMMAND"));
	
	public static final String MAIN_GENERAL_STARTER_PACKAGE = "maingUI.MainGuiGeneralAppStarter";
	public static final String MAIN_GENERAL_CLASSPATH = "./*;./Resources_Scrypt/lib/* ";

	public static final String APP_VERSION = linkedMappings.getProperty("APP_VERSION");
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
	
	public static String debugPreRead = linkedMappings.getProperty("FLAG_DEBUG_GRAPHICS");
	private static final boolean FLAG_DEBUG_GRAPHICS = (debugPreRead!= null && debugPreRead.equalsIgnoreCase("true"))? true : false;
	public static String DEBUG_GRAPHICS = (FLAG_DEBUG_GRAPHICS)? "debug," : "";
	
	// Temp decryptedFolder
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
	
	// JVM
	public static final String MAX_JVM_COMPLETE_COMPATIBILITY = linkedMappings.getProperty("MAX_JVM_COMPLETE_COMPATIBILITY");
	public static final String JVM_CUSTOM_CUSTOM_SETTINGS = linkedMappings.getProperty("JVM_CUSTOM_CUSTOM_SETTINGS");
	// file icons defaults 
	public static final Hashtable<String,ImageIcon> FILE_TYPE_ICONS_MAP = defaultFileIcons();
	
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
	public static final Font DEFAULT_FONT = new Font("Segoe UI", Font.PLAIN, 16);
	public static final Color BUTTON_BCKG_COLOR_TXT_EDITOR = new Color(120, 125, 140);
	public static final Color BUTTON_BCKG_COLOR_TXT_EDITOR_DARKER = new Color(71, 76, 84);
	
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
	
	
	/** Icons <-> File Image Mappings - (Used in Text Editor tabs)
	 * 
	 * @return hashtable of extensions linked to related icons
	 */
	public static Hashtable<String,ImageIcon> defaultFileIcons() {
    	
    	Hashtable<String,ImageIcon> table=new Hashtable<>();

	    // Add extensions and icons
	    table.put(".jpg",new ImageIcon(IconsPathConfigurator.F_ICON_JPG));
	    table.put(".jpeg",new ImageIcon(IconsPathConfigurator.F_ICON_JPG));
	    table.put(".png",new ImageIcon(IconsPathConfigurator.F_ICON_PNG));
	    table.put(".txt",new ImageIcon(IconsPathConfigurator.F_ICON_TXT));
	    table.put(".pdf",new ImageIcon(IconsPathConfigurator.F_ICON_PDF));
	    table.put(".html",new ImageIcon(IconsPathConfigurator.F_ICON_HTML));
	    table.put(".class",new ImageIcon(IconsPathConfigurator.F_ICON_EXE));
	    table.put(".java",new ImageIcon(IconsPathConfigurator.F_ICON_JAVA));
	    table.put(".jar",new ImageIcon(IconsPathConfigurator.F_ICON_JAR));
	    table.put(".js",new ImageIcon(IconsPathConfigurator.F_ICON_JS));
	    table.put(".json",new ImageIcon(IconsPathConfigurator.F_ICON_JSON));
	    table.put(".css",new ImageIcon(IconsPathConfigurator.F_ICON_CSS));
	    table.put(".exe",new ImageIcon(IconsPathConfigurator.F_ICON_EXE));
	    table.put(".bat",new ImageIcon(IconsPathConfigurator.ICON_CONSOLE));
	    table.put(".sh",new ImageIcon(IconsPathConfigurator.ICON_CONSOLE));
	    table.put(".php",new ImageIcon(IconsPathConfigurator.F_ICON_PHP));
	    table.put(".xml",new ImageIcon(IconsPathConfigurator.F_ICON_HTML));
	    table.put(".pom",new ImageIcon(IconsPathConfigurator.F_ICON_HTML));
	    table.put(".xsd",new ImageIcon(IconsPathConfigurator.F_ICON_HTML));
	    table.put(".wsdl",new ImageIcon(IconsPathConfigurator.F_ICON_HTML));
	    table.put(".zip",new ImageIcon(IconsPathConfigurator.F_ICON_ZIP));
	    table.put(".rar",new ImageIcon(IconsPathConfigurator.F_ICON_ZIP));
	    table.put(".sql",new ImageIcon(IconsPathConfigurator.F_ICON_DATABASE));
	    table.put(".db",new ImageIcon(IconsPathConfigurator.F_ICON_DATABASE));
	    table.put(".log",new ImageIcon(IconsPathConfigurator.ICON_LOOK_INFO));
	    table.put(".lnk",new ImageIcon(IconsPathConfigurator.F_ICON_LINK));
	    table.put(".properties",new ImageIcon(IconsPathConfigurator.F_ICON_PROPERTIES));
	    table.put(".config",new ImageIcon(IconsPathConfigurator.F_ICON_PROPERTIES));
	    table.put(".ini",new ImageIcon(IconsPathConfigurator.F_ICON_PROPERTIES));
	    table.put(".yml",new ImageIcon(IconsPathConfigurator.F_ICON_PROPERTIES));
	    
	    // generics video
	    table.put(".mov",new ImageIcon(IconsPathConfigurator.F_ICON_VIDEO));
	    table.put(".mpeg",new ImageIcon(IconsPathConfigurator.F_ICON_VIDEO));
	    table.put(".mpg",new ImageIcon(IconsPathConfigurator.F_ICON_VIDEO));
	    table.put(".flv",new ImageIcon(IconsPathConfigurator.F_ICON_VIDEO));
	    table.put(".mp4",new ImageIcon(IconsPathConfigurator.F_ICON_VIDEO));
	    table.put(".gif",new ImageIcon(IconsPathConfigurator.F_ICON_VIDEO));
	    
	    // generics audio
	    table.put(".mp3",new ImageIcon(IconsPathConfigurator.F_ICON_AUDIO));
	    table.put(".wav",new ImageIcon(IconsPathConfigurator.F_ICON_WAV));
	    
	    // generics image
	    table.put(".tiff",new ImageIcon(IconsPathConfigurator.F_ICON_IMAGE));
	    table.put(".gif",new ImageIcon(IconsPathConfigurator.F_ICON_IMAGE));
	    
	    return table;
    }
	
}

package resources;

import java.io.File;
import java.util.Hashtable;

import javax.swing.ImageIcon;

public class GeneralConfig {
	
	// General Folders
	public static final String RESOURCES_DIR = "Resources_Scrypt/";
	public static final String DEFAULT_IMAGE_FOLDER = RESOURCES_DIR + "Images/";
	public static final String SOUNDS_FOLDER = "Resources_Scrypt/sounds/";
	public static final String WORDLIST_FOLDER = RESOURCES_DIR + "ProgrammingLanguagesWords/";
	public static final String DEFAULT_INI_FOLDER= "INI_FILES/";
	
	// Temp decryptedFolder
	public static final String TEMP_DECRIPTED_ROOT_PATH = System.getProperty("user.home")+"/Desktop/ScrYpT_decrypted_tempFiles";
	public static final File TEMP_DECRIPTED_ROOT = new File(TEMP_DECRIPTED_ROOT_PATH);

	// Ini files configuration
	public static final String DEFAULT_INI= RESOURCES_DIR + "CONFIG.ini";
	public static final String BACKUP_INI= RESOURCES_DIR + "Backup/BACKUP_CONFIG.ini";
	
	// Readme File
	public static final String SCRYPT_TUTORIAL_HTML_PATH = "Resources_Scrypt/Readme/ReadMe.html";
	public static final String SCRYPT_TUTORIAL_HTML_URL = "File:./" + SCRYPT_TUTORIAL_HTML_PATH;
	
	// Logs configuration
	public static final String LOG_PATH = "LOG/Execution_Log_Trace.log";
	public static final String LOG_PROPERTIES_PATH = RESOURCES_DIR + "log4j.properties";
	public static final String LOG_LAUNCHER_PATH = "LAUNCHER/LOG/Launcher_Log_Trace.log";
	
	// JVM
	public static final String MAX_JVM_COMPLETE_COMPATIBILITY = "1.9.0";
	
	// Tooltips
	public static final int TOOLTIP_DELAY = 400; // 0,4 seconds
	public static final int TOOLTIP_TIMEOUT = 30000; // 30 seconds
	
	public static final Hashtable<String,ImageIcon> FILE_TYPE_ICONS_MAP = defaultFileIcons();
	
	// Icons <-> File Image Mappings - (Used in Text Editor tabs)
	private static Hashtable<String,ImageIcon> defaultFileIcons() {
    	
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
	    table.put(".zip",new ImageIcon(IconsPathConfigurator.F_ICON_ZIP));
	    table.put(".sql",new ImageIcon(IconsPathConfigurator.F_ICON_DATABASE));
	    table.put(".db",new ImageIcon(IconsPathConfigurator.F_ICON_DATABASE));
	    table.put(".log",new ImageIcon(IconsPathConfigurator.ICON_LOOK_INFO));
	    table.put(".lnk",new ImageIcon(IconsPathConfigurator.F_ICON_LINK));
	    table.put(".properties",new ImageIcon(IconsPathConfigurator.F_ICON_PROPERTIES));
	    
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

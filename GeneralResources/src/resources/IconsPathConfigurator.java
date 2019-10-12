package resources;

import java.util.Hashtable;

import javax.swing.ImageIcon;

public class IconsPathConfigurator {
	
	///////////////// MENU ICONS /////////////////
	
	// file extension types
	public static final String F_ICON_PDF = GeneralConfig.RESOURCES_DIR + "Icons/File_Extensions/pdf.png";
	public static final String F_ICON_PNG = GeneralConfig.RESOURCES_DIR + "Icons/File_Extensions/F_png.png";
	public static final String F_ICON_JPG = GeneralConfig.RESOURCES_DIR + "Icons/File_Extensions/F_jpg.png";
	public static final String F_ICON_TXT = GeneralConfig.RESOURCES_DIR + "Icons/File_Extensions/F_txt.png";
	public static final String F_ICON_HTML = GeneralConfig.RESOURCES_DIR + "Icons/File_Extensions/F_html.png";
	public static final String F_ICON_CSS = GeneralConfig.RESOURCES_DIR + "Icons/File_Extensions/F_css.png";
	public static final String F_ICON_PHP = GeneralConfig.RESOURCES_DIR + "Icons/File_Extensions/F_php.png";
	public static final String F_ICON_C_PLUS_PLUS = GeneralConfig.RESOURCES_DIR + "Icons/File_Extensions/F_cpp.png";
	public static final String F_ICON_C_SHARP = GeneralConfig.RESOURCES_DIR + "Icons/File_Extensions/F_Csharp.png";
	public static final String F_ICON_JAVA = GeneralConfig.RESOURCES_DIR + "Icons/File_Extensions/F_Java.png";
	public static final String F_ICON_PYTHON = GeneralConfig.RESOURCES_DIR + "Icons/File_Extensions/F_python.png";
	public static final String F_ICON_JS = GeneralConfig.RESOURCES_DIR + "Icons/File_Extensions/F_javascript.png";
	public static final String F_ICON_JSON = GeneralConfig.RESOURCES_DIR + "Icons/File_Extensions/F_json.png";
	public static final String F_ICON_TYPESCRIPT = GeneralConfig.RESOURCES_DIR + "Icons/File_Extensions/F_typescript.png";
	public static final String F_ICON_AUDIO = GeneralConfig.RESOURCES_DIR + "Icons/File_Extensions/F_audio.png";
	public static final String F_ICON_VIDEO = GeneralConfig.RESOURCES_DIR + "Icons/File_Extensions/F_movie.png";
	public static final String F_ICON_IMAGE = GeneralConfig.RESOURCES_DIR + "Icons/File_Extensions/F_image.png";
	public static final String F_ICON_EXE = GeneralConfig.RESOURCES_DIR + "Icons/File_Extensions/F_Exe.png";
	public static final String F_ICON_GENERIC = GeneralConfig.RESOURCES_DIR + "Icons/File_Extensions/F_genericFile.png";
	public static final String F_ICON_JAR = GeneralConfig.RESOURCES_DIR + "Icons/File_Extensions/F_Jar.png";
	public static final String F_ICON_ZIP = GeneralConfig.RESOURCES_DIR + "Icons/File_Extensions/F_Zip.png";
	public static final String F_ICON_DATABASE = GeneralConfig.RESOURCES_DIR + "Icons/File_Extensions/F_Database.png";
	public static final String F_ICON_LINK = GeneralConfig.RESOURCES_DIR + "Icons/File_Extensions/F_Link.png";
	public static final String F_ICON_PROPERTIES = GeneralConfig.RESOURCES_DIR + "Icons/File_Extensions/F_Properties.png";
	public static final String F_ICON_CODE = GeneralConfig.RESOURCES_DIR + "Icons/File_Extensions/F_code.png";
	public static final String F_ICON_RUNNABLE = GeneralConfig.RESOURCES_DIR + "Icons/File_Extensions/F_runnable.png";
	public static final String F_ICON_WAV = GeneralConfig.RESOURCES_DIR + "Icons/File_Extensions/F_wav.png";
	
		// file submenu
	public static final String ICON_CONF_MANAGER = GeneralConfig.RESOURCES_DIR + "Icons/Menu_Icons/manager.png";
	public static final String ICON_LOAD_CUSTOM_CONF = GeneralConfig.RESOURCES_DIR + "Icons/Menu_Icons/loadConfig.png";
	public static final String ICON_EXPORT_CONF = GeneralConfig.RESOURCES_DIR + "Icons/Menu_Icons/export.png";
	public static final String ICON_RELOAD_CONF = GeneralConfig.RESOURCES_DIR + "Icons/Menu_Icons/reload.png";
	public static final String ICON_SAVE_CONF = GeneralConfig.RESOURCES_DIR + "Icons/Menu_Icons/save.png";
	public static final String ICON_SAVE_AS_DEFAULT_CONF = GeneralConfig.RESOURCES_DIR + "Icons/Menu_Icons/saveAsDefault.png";
	public static final String ICON_RESET_CONF = GeneralConfig.RESOURCES_DIR + "Icons/Menu_Icons/reset.png";
	public static final String ICON_EXIT = GeneralConfig.RESOURCES_DIR + "Icons/Menu_Icons/turnOff.png";
	
		// preferences submenu
	public static final String ICON_GUI_OPT = GeneralConfig.RESOURCES_DIR + "Icons/Menu_Icons/graphics.png";
	public static final String ICON_GEN_OPT = GeneralConfig.RESOURCES_DIR + "Icons/Menu_Icons/options.png";
	
		// Screept submenu
	public static final String ICON_FILE_CRYPTER = GeneralConfig.RESOURCES_DIR + "Icons/Menu_Icons/crypter.png";
	public static final String ICON_KEYGEN = GeneralConfig.RESOURCES_DIR + "Icons/Menu_Icons/imageWriter.png";
	public static final String ICON_KEY_DICT = GeneralConfig.RESOURCES_DIR + "Icons/Menu_Icons/keyDictionary.png";
	
		// Various submenu
	public static final String ICON_TEXT_EDITOR = GeneralConfig.RESOURCES_DIR + "Icons/Menu_Icons/textEditor.png";
	public static final String ICON_NET_INFO = GeneralConfig.RESOURCES_DIR + "Icons/Menu_Icons/network.png";
	public static final String ICON_HW_INFO = GeneralConfig.RESOURCES_DIR + "Icons/Menu_Icons/hardware.png";
	public static final String ICON_CONVERSION = GeneralConfig.RESOURCES_DIR + "Icons/Menu_Icons/conversion.png";
	public static final String ICON_FILE_N_DIR = GeneralConfig.RESOURCES_DIR + "Icons/Menu_Icons/folderActions.png";
	
	public static final String ICON_IMAGE_CONVERTER = GeneralConfig.RESOURCES_DIR + "Icons/Menu_Icons/imageConverter.png";
	
	public static final String ICON_UPPERCASE_FIRST = GeneralConfig.RESOURCES_DIR + "Icons/Menu_Icons/uppercase.png";
	public static final String ICON_CREATE_FOLDER = GeneralConfig.RESOURCES_DIR + "Icons/Menu_Icons/createFolder.png";
	public static final String ICON_DELETE_TARGETED = GeneralConfig.RESOURCES_DIR + "Icons/Menu_Icons/targetedDelete.png";
	public static final String ICON_DELETE_FOLDER = GeneralConfig.RESOURCES_DIR + "Icons/Menu_Icons/deleteFolder.png";
	public static final String ICON_DELETE_DIRS_ONLY = GeneralConfig.RESOURCES_DIR + "Icons/Menu_Icons/emptyDirectory.png";
	public static final String ICON_COPY_DIR_STRUCTURE = GeneralConfig.RESOURCES_DIR + "Icons/Menu_Icons/cloneDirectory.png";
	public static final String ICON_FOLDER_WEB = GeneralConfig.RESOURCES_DIR + "Icons/Menu_Icons/webFolder.png";
	
		// help submenu
	public static final String ICON_ABOUT = GeneralConfig.RESOURCES_DIR + "Icons/Menu_Icons/about.png";
	public static final String ICON_EXPORT_LOG = GeneralConfig.RESOURCES_DIR + "Icons/Menu_Icons/logExport.png";
	public static final String ICON_USER_GUIDE = GeneralConfig.RESOURCES_DIR + "Icons/Menu_Icons/userGuide.png";
	public static final String ICON_HOME_PAGE = GeneralConfig.RESOURCES_DIR + "Icons/Menu_Icons/homePage.png";
		
	/////////////////// END MENU ICONS //////////////////
	
	///////////////// POP-UP MENU ICONS /////////////////
	public static final String ICON_IMPORT = GeneralConfig.RESOURCES_DIR + "Icons/Menu_Icons/import.png";
	public static final String ICON_LOAD = GeneralConfig.RESOURCES_DIR + "Icons/Menu_Icons/load.png";
	public static final String ICON_DELETE = GeneralConfig.RESOURCES_DIR + "Icons/Menu_Icons/delete.png";
	public static final String ICON_REMOVECROSS = GeneralConfig.RESOURCES_DIR + "Icons/Menu_Icons/removeCross.png";
	public static final String ICON_LOOK_INFO = GeneralConfig.RESOURCES_DIR + "Icons/Menu_Icons/look.png";
	public static final String ICON_CHECKMARK = GeneralConfig.RESOURCES_DIR + "Icons/Menu_Icons/checkmark.png";
	public static final String ICON_ADD = GeneralConfig.RESOURCES_DIR + "Icons/Menu_Icons/add.png";
	
	///////////////// TEXT EDITOR ICONS /////////////////
	/* First shared with Menu Icons */
	public static final String ICON_NEW_FILE = GeneralConfig.RESOURCES_DIR + "Icons/Menu_Icons/loadConfig.png";
	public static final String ICON_DOC_EMPTY = GeneralConfig.RESOURCES_DIR + "Icons/Menu_Icons/emptyDoc.png";
	public static final String ICON_DOC_VOID = GeneralConfig.RESOURCES_DIR + "Icons/Menu_Icons/voidDoc.png";
	public static final String ICON_DOC_SAVED = GeneralConfig.RESOURCES_DIR + "Icons/Menu_Icons/docSaved.png";
	public static final String ICON_DOC_UNSAVED = GeneralConfig.RESOURCES_DIR + "Icons/Menu_Icons/starDoc.png";
	
	/* Then custom only for Text Editor */
	public static final String ICON_LOCKED= GeneralConfig.RESOURCES_DIR + "Icons/Text_Editor/Crypt_3.png";
	public static final String ICON_UNLOCKED = GeneralConfig.RESOURCES_DIR + "Icons/Text_Editor/Decrypt_3.png";
	public static final String ICON_TEXT_CLEAR = GeneralConfig.RESOURCES_DIR + "Icons/Text_Editor/Clear.png";
	public static final String ICON_TEXT_BOLD = GeneralConfig.RESOURCES_DIR + "Icons/Text_Editor/Bold.png";
	public static final String ICON_TEXT_ITALIC = GeneralConfig.RESOURCES_DIR + "Icons/Text_Editor/Italic.png";
	public static final String ICON_REMOVE_CONTENT = GeneralConfig.RESOURCES_DIR + "Icons/Text_Editor/deleteContent.png";
	public static final String ICON_REMOVE_WHITE_LINE = GeneralConfig.RESOURCES_DIR + "Icons/Text_Editor/removeWhiteLine.png";
	public static final String ICON_TEXT_COLOR = GeneralConfig.RESOURCES_DIR + "Icons/Text_Editor/TextColor_2.png";
	public static final String ICON_TEXT_BG_COLOR = GeneralConfig.RESOURCES_DIR + "Icons/Text_Editor/BackgroundColor.png";
	public static final String ICON_TEXT_FONT = GeneralConfig.RESOURCES_DIR + "Icons/Text_Editor/Font_2.png";
	public static final String ICON_FILE_SAVE_AS = GeneralConfig.RESOURCES_DIR + "Icons/Text_Editor/saveFileAs.png";
	public static final String ICON_FILE_SAVE = GeneralConfig.RESOURCES_DIR + "Icons/Text_Editor/saveFile.png";
	public static final String ICON_FILE_LOAD = GeneralConfig.RESOURCES_DIR + "Icons/Text_Editor/LoadFile.png";
	public static final String ICON_MAP = GeneralConfig.RESOURCES_DIR + "Icons/Text_Editor/map.png";
	public static final String ICON_CLIPBOARD = GeneralConfig.RESOURCES_DIR + "Icons/Text_Editor/clipboard.png";
	public static final String ICON_REDO = GeneralConfig.RESOURCES_DIR + "Icons/Text_Editor/Redo.png";
	public static final String ICON_UNDO = GeneralConfig.RESOURCES_DIR + "Icons/Text_Editor/Undo.png";
	public static final String ICON_CRYPT = GeneralConfig.RESOURCES_DIR + "Icons/Text_Editor/Crypt.png";
	public static final String ICON_DECRYPT = GeneralConfig.RESOURCES_DIR + "Icons/Text_Editor/Decrypt.png";
	public static final String ICON_SPEAKER = GeneralConfig.RESOURCES_DIR + "Icons/Text_Editor/Speaker.png";
	public static final String ICON_SEARCH = GeneralConfig.RESOURCES_DIR + "Icons/Text_Editor/Search.png";
	public static final String ICON_CONVERT_OCR_IMAGE = GeneralConfig.RESOURCES_DIR + "Icons/Text_Editor/OCR.png";
	public static final String ICON_CONVERT_ESCAPES = GeneralConfig.RESOURCES_DIR + "Icons/Text_Editor/convertEscapes.png";
	public static final String ICON_REPLACE_TXT = GeneralConfig.RESOURCES_DIR + "Icons/Text_Editor/replace.png";
	public static final String ICON_GOTO_LINE = GeneralConfig.RESOURCES_DIR + "Icons/Text_Editor/goToLine.png";
	public static final String ICON_NUMBERED_LINES = GeneralConfig.RESOURCES_DIR + "Icons/Text_Editor/numberedLines.png";
	public static final String ICON_PIN_GREEN = GeneralConfig.RESOURCES_DIR + "Icons/Text_Editor/pin.png";
	public static final String ICON_PIN_RED = GeneralConfig.RESOURCES_DIR + "Icons/Text_Editor/pinRed.png";
	public static final String ICON_UNPIN = GeneralConfig.RESOURCES_DIR + "Icons/Text_Editor/unpin.png";
	public static final String ICON_COLOR_SAMPLER = GeneralConfig.RESOURCES_DIR + "Icons/Text_Editor/colorDropper.png";
	
	///////////////// TREE RENDERER ICONS /////////////////
	public static final String iconFolderPath = GeneralConfig.RESOURCES_DIR + "Icons/General/Folder.png";
	public static final String iconFilePath = GeneralConfig.RESOURCES_DIR + "Icons/General/File.png";
	public static final String iconConfigFilePath = GeneralConfig.RESOURCES_DIR + "Icons/General/configFile.png";
	public static final String iconRootPath = GeneralConfig.RESOURCES_DIR + "Icons/General/Root.png";
	
	/////////////////  GENERAL PURPOSE  ///////////////////
	public static final String ICON_GEN_INFO = GeneralConfig.RESOURCES_DIR + "Icons/General/info.png";
	public static final String ICON_GEN_QUESTION = GeneralConfig.RESOURCES_DIR + "Icons/General/question.png";
	public static final String ICON_GEN_WARNING = GeneralConfig.RESOURCES_DIR + "Icons/General/Warning.png";
	public static final String ICON_BIG_GEN_WARNING = GeneralConfig.RESOURCES_DIR + "Icons/General/Warning_Big.png";
	public static final String ICON_GEN_ERROR = GeneralConfig.RESOURCES_DIR + "Icons/General/Error.png";
	public static final String ICON_BIG_GEN_ERROR = GeneralConfig.RESOURCES_DIR + "Icons/General/Error_Big.png";
	public static final String ICON_LIGHT_ON = GeneralConfig.RESOURCES_DIR + "Icons/General/lightON.png";
	public static final String ICON_LIGHT_OFF = GeneralConfig.RESOURCES_DIR + "Icons/General/lightOFF.png";
	public static final String ICON_CONSOLE = GeneralConfig.RESOURCES_DIR + "Icons/General/console.png";
	public static final String ICON_SCREEPT = GeneralConfig.RESOURCES_DIR + "Icons/General/ScreepT.png";
	public static final String ICON_RELOAD = GeneralConfig.RESOURCES_DIR + "Icons/General/reload.png";
	public static final String ICON_LESS = GeneralConfig.RESOURCES_DIR + "Icons/General/less.png";
	public static final String ICON_MORE = GeneralConfig.RESOURCES_DIR + "Icons/General/more.png";
	public static final String ICON_JOB_ON = GeneralConfig.RESOURCES_DIR + "Icons/General/job_ON.png";
	public static final String ICON_JOB_OFF = GeneralConfig.RESOURCES_DIR + "Icons/General/job_OFF.png";
	public static final String ICON_JOB_SEARCH = GeneralConfig.RESOURCES_DIR + "Icons/General/job_search.png";
	public static final String ICON_CALCULATOR = GeneralConfig.RESOURCES_DIR + "Icons/General/calculator.png";
	public static final String ICON_PREV = GeneralConfig.RESOURCES_DIR + "Icons/General/prev.png";
	public static final String ICON_NEXT = GeneralConfig.RESOURCES_DIR + "Icons/General/next.png";
	
	////////////////    INSTANTIATED DEFAULT STATIC ICONS   ///////////////////
	public static final ImageIcon IMG_ICON_LIGHT_ON = new ImageIcon(ICON_LIGHT_ON);
	public static final ImageIcon IMG_ICON_LIGHT_OFF = new ImageIcon(ICON_LIGHT_OFF);
	
	/**
	 * Same as defaultFileIcons(), but this one is already lazy initialized
	 */
	public static final Hashtable<String,ImageIcon> FILE_TYPE_ICONS_MAP = defaultFileIcons();
	
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
	    table.put(".class",new ImageIcon(IconsPathConfigurator.F_ICON_EXE));
	    table.put(".java",new ImageIcon(IconsPathConfigurator.F_ICON_JAVA));
	    table.put(".cpp",new ImageIcon(IconsPathConfigurator.F_ICON_C_PLUS_PLUS));
	    table.put(".c",new ImageIcon(IconsPathConfigurator.F_ICON_CODE));
	    table.put(".cs",new ImageIcon(IconsPathConfigurator.F_ICON_C_SHARP));
	    table.put(".py",new ImageIcon(IconsPathConfigurator.F_ICON_PYTHON));
	    table.put(".jar",new ImageIcon(IconsPathConfigurator.F_ICON_JAR));
	    table.put(".js",new ImageIcon(IconsPathConfigurator.F_ICON_JS));
	    table.put(".ts",new ImageIcon(IconsPathConfigurator.F_ICON_TYPESCRIPT));
	    table.put(".json",new ImageIcon(IconsPathConfigurator.F_ICON_JSON));
	    table.put(".css",new ImageIcon(IconsPathConfigurator.F_ICON_CSS));
	    table.put(".exe",new ImageIcon(IconsPathConfigurator.F_ICON_EXE));
	    table.put(".bat",new ImageIcon(IconsPathConfigurator.F_ICON_RUNNABLE));
	    table.put(".sh",new ImageIcon(IconsPathConfigurator.F_ICON_RUNNABLE));
	    table.put(".bin",new ImageIcon(IconsPathConfigurator.F_ICON_RUNNABLE));
	    table.put(".php",new ImageIcon(IconsPathConfigurator.F_ICON_PHP));
	    table.put(".html",new ImageIcon(IconsPathConfigurator.F_ICON_HTML));
	    table.put(".xsl",new ImageIcon(IconsPathConfigurator.F_ICON_HTML));
	    table.put(".vm",new ImageIcon(IconsPathConfigurator.F_ICON_HTML));
	    table.put(".pom",new ImageIcon(IconsPathConfigurator.F_ICON_CODE));
	    table.put(".xml",new ImageIcon(IconsPathConfigurator.F_ICON_CODE));
	    table.put(".pom",new ImageIcon(IconsPathConfigurator.F_ICON_CODE));
	    table.put(".xsd",new ImageIcon(IconsPathConfigurator.F_ICON_CODE));
	    table.put(".wsdl",new ImageIcon(IconsPathConfigurator.F_ICON_CODE));
	    table.put(".zip",new ImageIcon(IconsPathConfigurator.F_ICON_ZIP));
	    table.put(".7z",new ImageIcon(IconsPathConfigurator.F_ICON_ZIP));
	    table.put(".rar",new ImageIcon(IconsPathConfigurator.F_ICON_ZIP));
	    table.put(".lz",new ImageIcon(IconsPathConfigurator.F_ICON_ZIP));
	    table.put(".sql",new ImageIcon(IconsPathConfigurator.F_ICON_DATABASE));
	    table.put(".db",new ImageIcon(IconsPathConfigurator.F_ICON_DATABASE));
	    table.put(".log",new ImageIcon(IconsPathConfigurator.ICON_LOOK_INFO));
	    table.put(".lnk",new ImageIcon(IconsPathConfigurator.F_ICON_LINK));
	    table.put(".desktop",new ImageIcon(IconsPathConfigurator.F_ICON_LINK));
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
	
	// MAIN RESOURCES DIR SETTER & GETTER //
	public static String getRESOURCES_DIR() {
		return GeneralConfig.RESOURCES_DIR;
	}
	
}

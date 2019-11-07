package resources;

public class ImagesPathConfigurator {

	public static String IMAGE_LOGO_AM_MAIN;
	public static String IMAGE_BCKG_ABOUT;
	public static String IMAGE_BCKG_DRAGNDROP;
	public static String MAIN_APP_ICON;
	public static String LAUNCHER_BACK_IMAGE_PATH = "LAUNCHER/launcher_icon.jpg";
	
	public static void init() {
		IMAGE_LOGO_AM_MAIN = GeneralConfig.DEFAULT_IMAGE_FOLDER + GeneralConfig.resourcesFilePathsMappings.getProperty("AM_IMAGE_MAIN_FILE");   
		IMAGE_BCKG_ABOUT = GeneralConfig.DEFAULT_IMAGE_FOLDER + GeneralConfig.resourcesFilePathsMappings.getProperty("BCKG_IMAGE_ABOUT_FILE");  
		IMAGE_BCKG_DRAGNDROP = GeneralConfig.DEFAULT_IMAGE_FOLDER + GeneralConfig.resourcesFilePathsMappings.getProperty("DRAGNDROP_BCKG_FILE");
		MAIN_APP_ICON = GeneralConfig.DEFAULT_IMAGE_FOLDER + GeneralConfig.resourcesFilePathsMappings.getProperty("MAIN_ICON_FILE");            
		LAUNCHER_BACK_IMAGE_PATH = GeneralConfig.resourcesFilePathsMappings.getProperty("LAUNCHER_BACK_IMAGE_PATH");                            
		                                                                                                                                        
	}
}

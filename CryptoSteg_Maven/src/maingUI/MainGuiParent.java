package maingUI;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.util.Date;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.ini4j.Wini;

import frameutils.GuiUtils;
import gUI.utils.mainGui.MainGuiUtils;
import gui.utils.LauncherLoaderFrame;
import mainobj.INItializer;
import menusettings.MenuSetter;
import resources.IconsPathConfigurator;

public class MainGuiParent {
	
	// mainGUI logger
	static Logger logger = Logger.getLogger(MainGUI.class);
	// date used for log operations
	public static Date now;
    public static Level LOG_LEVEL_AT_START;
	
	// the main frame for placing the content
	static public JFrame frmScryptV;
	
	// this file is the abstraction of the current settings used by various sub-programs and application of this main_GUI.
	// if you need to get or set current configuration in volatile memory, then use pojo's mapped in this object, please.	
	public static INItializer currentConfiguration;
	
	// and another one for launcher modding from main app
	public static Wini launcherConfig;

	//////////////////////GENERAL STATIC ELEMENTS /////////////////////////
	////////////////////////////////////////////////////////////////////////
	
	static final long AUTOSAVE_MANAGER_PERIOD = 10000; // autosave manager constant check refresh period in millisecond
	static Thread autosaveModule; // AUTOSAVE MODULE - only if specified in fileOptions
	
	// AUTHOR AND GENERAL INFORMATIONS
	public static final String APP_VERSION = "0.7.3bis";
	public static final String APP_VERSION_DATE = "01/10/2019";
	public static final String WEBSITE = "www.am_design_developement.com";
	public static final String APPLICATION_NAME = "ScrYpT - V" + APP_VERSION;
	public static final String AUTHOR = "A.M. Design and Developement";
	
	// STATIC MAIN IMAGES REFERENCES
	public static Image APPLICATION_MAIN_BCKG_IMAGE;
	public static Image SCREEPT_BACKGROUND_DRAGNDROP_IMAGE;
	public static Image SW_ICON;
	
	// JVM VERSION INIT 
	public static String currentJvmVersion = "";
	
	// RUNNING FLAG
	volatile public static boolean mainAppRunning = false;
	
	public static MainGuiUtils mainGuiUtils;
	
	// LOADER
	public static LauncherLoaderFrame loaderGraphics;
	public static final float launcherRatio = 0.25f;
	public static final Dimension screenSize = GuiUtils.getScreenSize();
	public static final Dimension minimumSize = new Dimension(1100,650);
	
	// MENU ITEMS
	int menuSize;
	int subMenuSize;
	
	JMenuBar menuBar;
	
	JMenu mnFile;
	JMenu mnPreferences;
	JMenu mnCryptoSteg; 
	JMenu mnVarious;
	JMenu mnConversionsAndOther;
	JMenu mnFilesAndDirectories;
	JMenu mnHelp;
	JMenu mnrestartPanel;
	
	JMenuItem mntmInspectCurrentConfiguration;
	JMenuItem mntmloadPrjSub;
	JMenuItem mntmExportPrjSub;
	JMenuItem mntmReloadPrjSub;
	JMenuItem mntmSaveCurrent;
	JMenuItem mntmSaveCurrentAs;
	JMenuItem mntmloadSrcrYptDefaultConf;
	JMenuItem mntmExit;
	JMenuItem mntmUserDocumentation;
	JMenuItem mntmGenOpt;
	JMenuItem mntmCustomizeUIView;
	public JMenuItem mntmFullScreenMode;
	static JMenuItem mntmCrypter;
	JMenuItem mntmgenerateKeyImageSub;
	JMenuItem mntmViewKeyDictionary;
	static JMenuItem mntmTextEditor;
	JMenuItem mntmnetInfo;
	JMenuItem mntmhwInfo;
	JMenuItem mntmImageConverter;
	JMenuItem mntmReturnToWelcome;
	JMenuItem mntmAboutScreept;
	JMenuItem mntmExportLog; 
	JMenuItem mntmEraseLogs;
	JMenuItem mntmTouppercaseDirectory;
	JMenuItem mntmDeleteDirectoryContentByExtension;
	JMenuItem mntmDeleteDirectoryContentRecursive;
	JMenuItem mntmDeleteDirectoryContentDirsOnlyRecursive;
	JMenuItem mntmMakeDirsFromPath;
	JMenuItem mntmCopyDirStructure;
	JMenuItem mntmResetAllPanels;
	
	/******************  BEGIN UTILITY METHODS  *****************************/
	
	/**
	 *  Updates the title displayed on top of main app window
	 */
	public void startTitleUpdater() {
		// TITLE UPDATER - Thread that refreshes every second the username of the title
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (mainAppRunning) {
					frmScryptV.setTitle(currentConfiguration.getGenOpt().getUserName()+" is using " + APPLICATION_NAME);				
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						frmScryptV.setTitle(APPLICATION_NAME+" - A.M. developement");	
					}
				}
			}
		}).start();
	}

	
	/******************  BEGIN MAIN MENU-BAR SETUP  *****************************/
	
	void structureMenuBar() {
		
		// INITIALIZE FONTS BY TYPE
		Font menuBarFont = new Font("Segoe UI", Font.BOLD, menuSize);
		Font subMenuFont = new Font("Gadugi", Font.BOLD, subMenuSize);
		Font menuItemFont = new Font("Segoe UI", Font.PLAIN, subMenuSize);
		
		// CREATE MENU-BAR STRUCTURE
		menuBar.add(mnFile);
		menuBar.add(mnPreferences);
		menuBar.add(mnCryptoSteg);
		menuBar.add(mnVarious);
		menuBar.add(mnHelp);
		
			// SUB-MENU
			mnFile.add(mntmInspectCurrentConfiguration);
			mnFile.add(new JSeparator());
			mnFile.add(mntmReloadPrjSub);
			mnFile.add(mntmloadPrjSub);
			mnFile.add(mntmExportPrjSub);
			mnFile.add(new JSeparator());
			mnFile.add(mntmSaveCurrent);
			mnFile.add(mntmSaveCurrentAs);
			mnFile.add(new JSeparator());
			mnFile.add(mntmloadSrcrYptDefaultConf);
			mnFile.add(new JSeparator());
			mnFile.add(mntmExit);
			
			mnPreferences.add(mntmCustomizeUIView);
			mnPreferences.add(mntmGenOpt);
			mnPreferences.add(mntmFullScreenMode);
			
			mnCryptoSteg.add(mntmCrypter);
			mnCryptoSteg.add(new JSeparator());
			mnCryptoSteg.add(mntmgenerateKeyImageSub);
			mnCryptoSteg.add(mntmViewKeyDictionary);
	
			mnVarious.add(mntmTextEditor);
			mnVarious.add(new JSeparator());
			mnVarious.add(mntmnetInfo);
			mnVarious.add(mntmhwInfo);
			mnVarious.add(mnConversionsAndOther);
			mnVarious.add(mnFilesAndDirectories);
			
			mnHelp.add(mntmAboutScreept);		
			mnHelp.add(new JSeparator());
			mnHelp.add(mntmExportLog);
			mnHelp.add(mntmEraseLogs);
			mnHelp.add(mntmUserDocumentation);
			mnHelp.add(mnrestartPanel);
				mnrestartPanel.add(mntmResetAllPanels);
			mnHelp.add(new JSeparator());
			mnHelp.add(mntmReturnToWelcome);
			
				// UNDER-SUB-MENU
				mnConversionsAndOther.add(mntmImageConverter);
				
				mnFilesAndDirectories.add(mntmTouppercaseDirectory);
				mnFilesAndDirectories.add(mntmMakeDirsFromPath);
				mnFilesAndDirectories.add(mntmCopyDirStructure);
				mnFilesAndDirectories.add(mntmDeleteDirectoryContentByExtension);
				mnFilesAndDirectories.add(mntmDeleteDirectoryContentRecursive);
				mnFilesAndDirectories.add(mntmDeleteDirectoryContentDirsOnlyRecursive);
			
		updateMenuFonts(menuBarFont, subMenuFont, menuItemFont);
		
		addMenuIcons();
	}
	
	/**
	 * Method to update the font of main menu
	 *  
	 * @param menuBarFont Font for main menuBar
	 * @param subMenuFont Font for sub menu containers
	 * @param menuItemFont Font for clickable items
	 */
	void updateMenuFonts(Font menuBarFont, Font subMenuFont, Font menuItemFont) {

		menuBar.setFont(menuBarFont);
		
		// MAIN MENU ENTRIES
		mnFile.setFont(menuBarFont);
		mnPreferences.setFont(menuBarFont);
		mnCryptoSteg.setFont(menuBarFont);
		mnVarious.setFont(menuBarFont);
		mnHelp.setFont(menuBarFont);

		// SUBDIRECTORIES
		mnFilesAndDirectories.setFont(subMenuFont);
		mnConversionsAndOther.setFont(subMenuFont);
		
		// FILE SECTION
		mntmExit.setFont(menuItemFont);
		mntmInspectCurrentConfiguration.setFont(menuItemFont);
		mntmloadPrjSub.setFont(menuItemFont);
		mntmExportPrjSub.setFont(menuItemFont);
		mntmReloadPrjSub.setFont(menuItemFont);
		mntmSaveCurrent.setFont(menuItemFont);
		mntmSaveCurrentAs.setFont(menuItemFont);
		mntmloadSrcrYptDefaultConf.setFont(menuItemFont);
		
		// PREFERENCES SECTION
		mntmCustomizeUIView.setFont(menuItemFont);
		mntmGenOpt.setFont(menuItemFont);
		mntmFullScreenMode.setFont(menuItemFont);
		
		// SCREEPT SECTION
		mntmCrypter.setFont(menuItemFont);
		mntmgenerateKeyImageSub.setFont(menuItemFont);
		mntmViewKeyDictionary.setFont(menuItemFont);

		// VARIOUS SECTION
		mntmnetInfo.setFont(menuItemFont);
		mntmhwInfo.setFont(menuItemFont);
		mntmTextEditor.setFont(menuItemFont);
		
		// SUB -> CONVERSION UTILS SECTION
		mntmImageConverter.setFont(menuItemFont);
		
		// SUB-> FILES AND DIRECTORIES SECTION
		mntmTouppercaseDirectory.setFont(menuItemFont);
		mntmMakeDirsFromPath.setFont(menuItemFont);
		mntmDeleteDirectoryContentByExtension.setFont(menuItemFont);
		mntmDeleteDirectoryContentRecursive.setFont(menuItemFont);
		mntmDeleteDirectoryContentDirsOnlyRecursive.setFont(menuItemFont);
		mntmCopyDirStructure.setFont(menuItemFont);
		
		// HELP SECTION
		mntmReturnToWelcome.setFont(menuItemFont);
		mntmAboutScreept.setFont(menuItemFont);
		mntmExportLog.setFont(menuItemFont);
		mntmEraseLogs.setFont(menuItemFont);
		mntmUserDocumentation.setFont(menuItemFont);
		mnrestartPanel.setFont(subMenuFont);
		mntmResetAllPanels.setFont(menuItemFont);
	}
	
	void addMenuIcons() {
		
		MenuSetter.setIconTextGap(menuBar, 15);
		
		// file submenu
		mntmInspectCurrentConfiguration.setIcon(new ImageIcon(IconsPathConfigurator.ICON_CONF_MANAGER));
		mntmloadPrjSub.setIcon(new ImageIcon(IconsPathConfigurator.ICON_LOAD_CUSTOM_CONF));
		mntmExportPrjSub.setIcon(new ImageIcon(IconsPathConfigurator.ICON_EXPORT_CONF));
		mntmReloadPrjSub.setIcon(new ImageIcon(IconsPathConfigurator.ICON_RELOAD_CONF));
		mntmSaveCurrent.setIcon(new ImageIcon(IconsPathConfigurator.ICON_SAVE_CONF));
		mntmSaveCurrentAs.setIcon(new ImageIcon(IconsPathConfigurator.ICON_SAVE_AS_DEFAULT_CONF));
		mntmloadSrcrYptDefaultConf.setIcon(new ImageIcon(IconsPathConfigurator.ICON_RESET_CONF));
		mntmExit.setIcon(new ImageIcon(IconsPathConfigurator.ICON_EXIT));
		
		// preferences submenu
		mntmCustomizeUIView.setIcon(new ImageIcon(IconsPathConfigurator.ICON_GUI_OPT));
		mntmGenOpt.setIcon(new ImageIcon(IconsPathConfigurator.ICON_GEN_OPT));
		
		// ScreepT submenu
		mntmCrypter.setIcon(new ImageIcon(IconsPathConfigurator.ICON_SCREEPT));
		mntmgenerateKeyImageSub.setIcon(new ImageIcon(IconsPathConfigurator.ICON_KEYGEN));
		mntmViewKeyDictionary.setIcon(new ImageIcon(IconsPathConfigurator.ICON_KEY_DICT));
		
		// Various submenu
		mntmTextEditor.setIcon(new ImageIcon(IconsPathConfigurator.ICON_TEXT_EDITOR));
		mntmnetInfo.setIcon(new ImageIcon(IconsPathConfigurator.ICON_NET_INFO));
		mntmhwInfo.setIcon(new ImageIcon(IconsPathConfigurator.ICON_HW_INFO));
		mnConversionsAndOther.setIcon(new ImageIcon(IconsPathConfigurator.ICON_CONVERSION));
		mnFilesAndDirectories.setIcon(new ImageIcon(IconsPathConfigurator.ICON_FILE_N_DIR));
		
		mntmImageConverter.setIcon(new ImageIcon(IconsPathConfigurator.ICON_IMAGE_CONVERTER));
		
		mntmTouppercaseDirectory.setIcon(new ImageIcon(IconsPathConfigurator.ICON_UPPERCASE_FIRST));
		mntmMakeDirsFromPath.setIcon(new ImageIcon(IconsPathConfigurator.ICON_CREATE_FOLDER));
		mntmDeleteDirectoryContentByExtension.setIcon(new ImageIcon(IconsPathConfigurator.ICON_DELETE_TARGETED));
		mntmDeleteDirectoryContentRecursive.setIcon(new ImageIcon(IconsPathConfigurator.ICON_DELETE_FOLDER));
		mntmDeleteDirectoryContentDirsOnlyRecursive.setIcon(new ImageIcon(IconsPathConfigurator.ICON_DELETE_DIRS_ONLY));
		mntmCopyDirStructure.setIcon(new ImageIcon(IconsPathConfigurator.ICON_COPY_DIR_STRUCTURE));

		// Help submenu
		mntmAboutScreept.setIcon(new ImageIcon(IconsPathConfigurator.ICON_ABOUT));
		mntmExportLog.setIcon(new ImageIcon(IconsPathConfigurator.ICON_EXPORT_LOG));
		mntmEraseLogs.setIcon(new ImageIcon(IconsPathConfigurator.ICON_DELETE));
		mntmUserDocumentation.setIcon(new ImageIcon(IconsPathConfigurator.ICON_USER_GUIDE));
		mntmReturnToWelcome.setIcon(new ImageIcon(IconsPathConfigurator.ICON_HOME_PAGE));
		
		MenuSetter.setIconTextGap(mnrestartPanel, 20);
		mnrestartPanel.setIcon(new ImageIcon(IconsPathConfigurator.ICON_RELOAD_CONF));
		mntmResetAllPanels.setIcon(new ImageIcon(IconsPathConfigurator.ICON_RELOAD_CONF));
	}
	
	void updateMenuAndLogFontSize(int fontSize){

		// use the same font as main gui but change font size
		UIManager.put("JMenuBar.font", new Font("Segoe UI", Font.BOLD, fontSize ));
		UIManager.put("JMenu.font", new Font("Segoe UI", Font.BOLD, fontSize ));
		UIManager.put("JMenuItem.font", new Font("Segoe UI", Font.BOLD, fontSize-2 ));
	    SwingUtilities.updateComponentTreeUI(frmScryptV);
	}
	
	/******************  END MAIN MENU-BAR SETUP *****************************/
	
	// GETTERS //
	
	public JFrame getMainFrame() {
		return frmScryptV;
	}
	
	public JMenuItem getMntmInspectCurrentConfiguration() {
		return mntmInspectCurrentConfiguration;
	}

	public JMenuItem getMntmloadPrjSub() {
		return mntmloadPrjSub;
	}

	public JMenuItem getMntmExportPrjSub() {
		return mntmExportPrjSub;
	}

	public JMenuItem getMntmReloadPrjSub() {
		return mntmReloadPrjSub;
	}

	public JMenuItem getMntmSaveCurrentAs() {
		return mntmSaveCurrentAs;
	}

	public JMenuItem getMntmSaveCurrent() {
		return mntmSaveCurrent;
	}

	public void setMntmSaveCurrent(JMenuItem mntmSaveCurrent) {
		this.mntmSaveCurrent = mntmSaveCurrent;
	}
	
	public JMenuItem getMntmloadSrcrYptDefaultConf() {
		return mntmloadSrcrYptDefaultConf;
	}

	public JMenuItem getMntmExit() {
		return mntmExit;
	}

	public JMenuItem getMntmUserDocumentation() {
		return mntmUserDocumentation;
	}

	public JMenuItem getMntmGenOpt() {
		return mntmGenOpt;
	}

	public JMenuItem getMntmCustomizeUIView() {
		return mntmCustomizeUIView;
	}

	public JMenuItem getMntmCrypter() {
		return mntmCrypter;
	}

	public JMenuItem getMntmgenerateKeyImageSub() {
		return mntmgenerateKeyImageSub;
	}

	public JMenuItem getMntmViewKeyDictionary() {
		return mntmViewKeyDictionary;
	}

	public JMenuItem getMntmTextEditor() {
		return mntmTextEditor;
	}

	public JMenuItem getMntmnetInfo() {
		return mntmnetInfo;
	}

	public JMenuItem getMntmhwInfo() {
		return mntmhwInfo;
	}

	public JMenuItem getMntmImageConverter() {
		return mntmImageConverter;
	}

	public JMenuItem getMntmReturnToWelcome() {
		return mntmReturnToWelcome;
	}

	public JMenuItem getMntmAboutScreept() {
		return mntmAboutScreept;
	}

	public JMenuItem getMntmExportLog() {
		return mntmExportLog;
	}

	public JMenuItem getMntmTouppercaseDirectory() {
		return mntmTouppercaseDirectory;
	}

	public JMenuItem getMntmDeleteDirectoryContentRecursive() {
		return mntmDeleteDirectoryContentRecursive;
	}

	public JMenuItem getMntmMakeDirsFromPath() {
		return mntmMakeDirsFromPath;
	}

	public JMenuItem getMntmDeleteDirectoryContentByExtension() {
		return mntmDeleteDirectoryContentByExtension;
	}


	public JMenuItem getMntmResetAllPanels() {
		return mntmResetAllPanels;
	}


	public void setMntmResetAllPanels(JMenuItem mntmResetAllPanels) {
		this.mntmResetAllPanels = mntmResetAllPanels;
	}


	public void setMntmReturnToWelcome(JMenuItem mntmReturnToWelcome) {
		this.mntmReturnToWelcome = mntmReturnToWelcome;
	}
	
}

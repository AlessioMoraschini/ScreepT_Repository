package maingUI;

import static java.awt.Event.CTRL_MASK;
import static java.awt.Event.ALT_MASK;
import static java.awt.Event.SHIFT_MASK;

import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.log4j.Logger;

import amfileutils.om.FileNamed;
import frameutils.utils.GuiUtils;
import gUI.panels.ChargeIniLog;
import gUI.panels.CrypterPanel;
import gUI.panels.TextEditor;
import generic.LimitedConcurrentList;
import jmenu.CustomJmenuItem;
import mainobj.INItializer;
import menusettings.MenuSetter;
import resources.GeneralConfig;
import resources.IconsPathConfigurator;

public class MainGuiParent {
	
	// mainGUI logger
	static Logger logger = Logger.getLogger(MainGUI.class);
	
	// date used for log operations
	public static Date now;
	
	// the main frame for placing the content
	static public JFrame frmScryptV;
	
	// this file is the abstraction of the current settings used by various sub-programs and application of this main_GUI.
	// if you need to get or set current configuration in volatile memory, then use pojo's mapped in this object, please.	
	public static INItializer currentConfiguration;
	
	//////////////////////GENERAL STATIC ELEMENTS /////////////////////////
	////////////////////////////////////////////////////////////////////////
	
	static final long AUTOSAVE_MANAGER_PERIOD = 10000; // autosave manager constant check refresh period in millisecond
	static Thread autosaveModule; // AUTOSAVE MODULE - only if specified in fileOptions
	
	// INITIALIZE FONTS BY TYPE
	Font menuBarFont;
	Font subMenuFont;
	Font menuItemFont;
	
	// AUTHOR AND GENERAL INFORMATIONS

	// STATIC MAIN IMAGES REFERENCES
	public static Image APPLICATION_MAIN_BCKG_IMAGE;
	public static Image SCREEPT_BACKGROUND_DRAGNDROP_IMAGE;
	public static Image SW_ICON;
	
	public static String BASE_TITLE;
	
	// JVM VERSION INIT 
	public static String currentJvmVersion = "";
	
	// RUNNING FLAG
	volatile public static boolean mainAppRunning = false;
	
	// MENU ITEMS
	int menuSize;
	int subMenuSize;
	
	public JMenuBar menuBar;
	
	JMenu mnFile;
	JMenu mnPreferences;
	JMenu mnCryptoSteg; 
	JMenu mnVarious;
		JMenu mnLastOpenedFile;
		JMenu mnLastWorkspaces;
		JMenu mnConversionsAndOther;
	JMenu mnFilesAndDirectories;
	JMenu mnHelp;
		JMenu mnrestartPanel;
	JMenuItem mntmpadding = new JMenuItem();
	JMenuItem mntmPinApplicationOnTop;
	
	static CustomJmenuItem mntmInspectCurrentConfiguration;
	CustomJmenuItem mntmloadPrjSub;
	CustomJmenuItem mntmExportPrjSub;
	CustomJmenuItem mntmReloadPrjSub;
	CustomJmenuItem mntmSaveCurrent;
	CustomJmenuItem mntmSaveCurrentAs;
	CustomJmenuItem mntmloadSrcrYptDefaultConf;
	CustomJmenuItem mntmExit;
	CustomJmenuItem mntmUserDocumentation;
	CustomJmenuItem mntmGenOpt;
	CustomJmenuItem mntmCustomizeUIView;
	public CustomJmenuItem mntmFullScreenMode;
	public CustomJmenuItem mntmImmersiveMode;
	static CustomJmenuItem mntmCrypter;
	CustomJmenuItem mntmgenerateKeyImageSub;
	CustomJmenuItem mntmViewKeyDictionary;
	static CustomJmenuItem mntmTextEditor;
	CustomJmenuItem mntmnetInfo;
	CustomJmenuItem mntmhwInfo;
	CustomJmenuItem mntmHashChecker;
	CustomJmenuItem mntmColorPicker;
	CustomJmenuItem mntmClipboardStandalone;
	CustomJmenuItem mntmImageConverter;
	CustomJmenuItem mntmTouppercaseDirectory;
	CustomJmenuItem mntmDeleteDirectoryContentByExtension;
	CustomJmenuItem mntmDeleteDirectoryAllContentRecursive;
	CustomJmenuItem mntmEmptyDirectoryRecursive;
	CustomJmenuItem mntmMakeDirsFromPath;
	CustomJmenuItem mntmCopyDirStructure;
	CustomJmenuItem mntmReturnToWelcome;
	CustomJmenuItem mntmAboutScreept;
	CustomJmenuItem mntmExportLog; 
	CustomJmenuItem mntmEraseLogs;
	CustomJmenuItem mntmViewWebsite;
	CustomJmenuItem mntmLookForUpdates;
	CustomJmenuItem mntmResetAllPanels;
	CustomJmenuItem mntmResetTextEditor;
	CustomJmenuItem mntmResetConfigManager;
	CustomJmenuItem mntmResetCrypterPanel;
	
	// PANELS THAT OPEN IN THE MAIN FRAME
	public static TextEditor initTxtWriter;
	public static CrypterPanel panelCrypter;
	public ChargeIniLog configManagerPanel;
	
	public static MainGuiUtils mainGuiUtils;
	
	public MainGuiParent() {
		// INIT MENU and MAIN-BAR SUMBMENUS
        menuBar = new JMenuBar();
		menuBar.setOpaque(true);
		
		mnFile = new JMenu("File");
		mnPreferences = new JMenu("Preferences");
		mnCryptoSteg = new JMenu("ScreepT");
		mnVarious = new JMenu("Various");
			mnLastWorkspaces = new JMenu("TextEditor: Last selected workspaces");
			mnLastOpenedFile = new JMenu("TextEditor: Last opened files");
			mnConversionsAndOther = new JMenu("Conversions and Media");
			mnFilesAndDirectories = new JMenu("Files And Directories");
		mnHelp = new JMenu("Help");
			mnrestartPanel = new JMenu("Reset panels...");
			
		BASE_TITLE = currentConfiguration.getGenOpt().getUserName() + " is using " + GeneralConfig.APPLICATION_NAME;
		
		// JMenuItems are initialized inside MainGUI Constructor, where they are linked to actions
	}
	
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
					
					if(mainGuiUtils.isTextEditorActive()) {
						frmScryptV.setTitle(BASE_TITLE + " - Text Editor");
					
					} else if(mainGuiUtils.isCrypterActive()) {
						frmScryptV.setTitle(BASE_TITLE + " - Crypter");
					
					} else if(mainGuiUtils.isConfManagerActive()) {
						frmScryptV.setTitle(BASE_TITLE + " - Configuration Manager");
						
					} else {
						frmScryptV.setTitle(BASE_TITLE);
					}
					try {
						Thread.sleep(GeneralConfig.TITLE_REFRESH_TIME);
					} catch (InterruptedException e) {
						frmScryptV.setTitle(GeneralConfig.APPLICATION_NAME+" - A.M. development");	
					}
				}
			}
		}).start();
	}

	/******************  BEGIN MAIN MENU-BAR SETUP  *****************************/

	void structureMenuBar() {
		
		// CREATE MENU-BAR STRUCTURE
		Insets mainMargin = new Insets(0, 1, 1, 1);
		menuBar.setMargin(mainMargin);
		// prepare padding to right for pinApplication
		mntmpadding.setMaximumSize(new Dimension(5000, 20));
		mntmpadding.setMinimumSize(new Dimension(400, 20));
		mntmPinApplicationOnTop.setMaximumSize(new Dimension(30, 20));
		mntmPinApplicationOnTop.setPreferredSize(new Dimension(30, 20));
 		mntmpadding.setEnabled(false);
 		
		Insets menuEntryMargin = new Insets(0, 0, 0, 6);
		menuBar.add(mnFile);
		mnFile.setMargin(menuEntryMargin);
		menuBar.add(mnPreferences);
		mnPreferences.setMargin(menuEntryMargin);
		menuBar.add(mnCryptoSteg);
		mnCryptoSteg.setMargin(menuEntryMargin);
		menuBar.add(mnVarious);
		mnVarious.setMargin(menuEntryMargin);
		menuBar.add(mnHelp);
		mnHelp.setMargin(menuEntryMargin);
		
		menuBar.add(mntmpadding);
		menuBar.add(mntmPinApplicationOnTop);
		mntmPinApplicationOnTop.setMargin(menuEntryMargin);
		
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
			mnPreferences.add(mntmImmersiveMode);
			
			mnCryptoSteg.add(mntmCrypter);
			mnCryptoSteg.add(new JSeparator());
			mnCryptoSteg.add(mntmgenerateKeyImageSub);
			mnCryptoSteg.add(mntmViewKeyDictionary);
	
			mnVarious.add(mntmTextEditor);
				mnVarious.add(mnLastWorkspaces);
				mnVarious.add(mnLastOpenedFile);
			mnVarious.add(new JSeparator());
			mnVarious.add(mntmnetInfo);
			mnVarious.add(mntmhwInfo);
			mnVarious.add(mntmHashChecker);
			mnVarious.add(mntmColorPicker);
			mnVarious.add(mntmClipboardStandalone);
			mnVarious.add(mnConversionsAndOther);
				// SUB-MENU
				mnConversionsAndOther.add(mntmImageConverter);
				mnVarious.add(mnFilesAndDirectories);
				// SUB-MENU
				mnFilesAndDirectories.add(mntmTouppercaseDirectory);
				mnFilesAndDirectories.add(mntmMakeDirsFromPath);
				mnFilesAndDirectories.add(mntmCopyDirStructure);
				mnFilesAndDirectories.add(mntmDeleteDirectoryContentByExtension);
				mnFilesAndDirectories.add(mntmDeleteDirectoryAllContentRecursive);
				mnFilesAndDirectories.add(mntmEmptyDirectoryRecursive);
			
			mnHelp.add(mntmAboutScreept);		
			mnHelp.add(mntmViewWebsite);
			mnHelp.add(mntmLookForUpdates);
			mnHelp.add(new JSeparator());
			mnHelp.add(mntmExportLog);
			mnHelp.add(mntmEraseLogs);
			mnHelp.add(mntmUserDocumentation);
			mnHelp.add(new JSeparator());
			mnHelp.add(mntmReturnToWelcome);
			mnHelp.add(mnrestartPanel);
				// SUB-MENU
				mnrestartPanel.add(mntmResetAllPanels);
				mnrestartPanel.add(mntmResetTextEditor);
				mnrestartPanel.add(mntmResetCrypterPanel);
				mnrestartPanel.add(mntmResetConfigManager);
			
			if (GeneralConfig.FLAG_THROW_ERROR_TEST_ON) {
				JMenuItem throwError = new JMenuItem("throw Error");
				throwError.setFont(new Font("Gadugi", Font.BOLD, subMenuSize));
				throwError.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						throw new Error("FLAG_THROW_ERROR_TEST_ON = true, Error thrown.");
					}
				});
				JMenuItem throwException = new JMenuItem("throw Exception");
				throwException.setFont(new Font("Gadugi", Font.BOLD, subMenuSize));
				throwException.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						"".substring(2);
					}
				});
				
				mnHelp.add(throwError);
				mnHelp.add(throwException);
			}
			
		updateMenuFonts(menuBarFont, subMenuFont, menuItemFont);
		
		addMenuIcons();
		
		addMenuShorctutsTips();
		
		addExtraHandlers();
		
		updateLastOpenedFiles(currentConfiguration.getTextEditorOpt().getLastOpenedFiles());
		updateLastOpenedWorkspaces(currentConfiguration.getTextEditorOpt().lastSelectedWorkspaces);
		
		menuBar.repaint();
	}
	
	/**
	 * Method to update the font of main menu
	 *  
	 * @param menuBarFont Font for main menuBar
	 * @param subMenuFont Font for sub menu containers
	 * @param menuItemFont Font for clickable items
	 */
	void updateMenuFonts(Font menuBarFont, Font subMenuFont, Font menuItemFont) {

		menuBarFont = new Font("Segoe UI", Font.BOLD, menuSize);
		subMenuFont = new Font("Gadugi", Font.BOLD, subMenuSize);
		menuItemFont = new Font("Segoe UI", Font.PLAIN, subMenuSize);
		
		menuBar.setFont(menuBarFont);
		
		// MAIN MENU ENTRIES
		mnFile.setFont(menuBarFont);
		mnPreferences.setFont(menuBarFont);
		mnCryptoSteg.setFont(menuBarFont);
		mnVarious.setFont(menuBarFont);
		mnHelp.setFont(menuBarFont);
		
		// ICON ON RIGHT OF BAR
		mntmPinApplicationOnTop.setFont(menuBarFont);
		mntmPinApplicationOnTop.setAlignmentX(1);
		mntmpadding.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT); // ONLY TO PAD RIGHT Pin On Top
		mntmPinApplicationOnTop.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

		// SUBDIRECTORIES
		mnFilesAndDirectories.setFont(subMenuFont);
		mnConversionsAndOther.setFont(subMenuFont);
		
		// FILE SECTION
		mntmExit.setFont(menuItemFont);
		mntmInspectCurrentConfiguration.setFont(menuItemFont);
		mnLastWorkspaces.setFont(menuItemFont);
		mnLastOpenedFile.setFont(menuItemFont);
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
		mntmImmersiveMode.setFont(menuItemFont);
		
		// SCREEPT SECTION
		mntmCrypter.setFont(menuItemFont);
		mntmgenerateKeyImageSub.setFont(menuItemFont);
		mntmViewKeyDictionary.setFont(menuItemFont);

		// VARIOUS SECTION
		mntmnetInfo.setFont(menuItemFont);
		mntmhwInfo.setFont(menuItemFont);
		mntmHashChecker.setFont(menuItemFont);
		mntmColorPicker.setFont(menuItemFont);
		mntmClipboardStandalone.setFont(menuItemFont);
		mntmTextEditor.setFont(menuItemFont);
		
		// SUB -> CONVERSION UTILS SECTION
		mntmImageConverter.setFont(menuItemFont);
		
		// SUB-> FILES AND DIRECTORIES SECTION
		mntmTouppercaseDirectory.setFont(menuItemFont);
		mntmMakeDirsFromPath.setFont(menuItemFont);
		mntmCopyDirStructure.setFont(menuItemFont);
		mntmDeleteDirectoryContentByExtension.setFont(menuItemFont);
		mntmDeleteDirectoryAllContentRecursive.setFont(menuItemFont);
		mntmEmptyDirectoryRecursive.setFont(menuItemFont);
		
		// HELP SECTION
		mntmReturnToWelcome.setFont(menuItemFont);
		mntmViewWebsite.setFont(menuItemFont);
		mntmLookForUpdates.setFont(menuItemFont);
		mntmAboutScreept.setFont(menuItemFont);
		mntmExportLog.setFont(menuItemFont);
		mntmEraseLogs.setFont(menuItemFont);
		mntmUserDocumentation.setFont(menuItemFont);
		mnrestartPanel.setFont(subMenuFont);
		mntmResetAllPanels.setFont(menuItemFont);
		mntmResetTextEditor.setFont(menuItemFont);
		mntmResetCrypterPanel.setFont(menuItemFont);
		mntmResetConfigManager.setFont(menuItemFont);
	}
	
	void addMenuIcons() {
		
		MenuSetter.setIconTextGap(menuBar, 15);
		mnLastOpenedFile.setIconTextGap(15);
		mnLastWorkspaces.setIconTextGap(15);
		
		mntmPinApplicationOnTop.setIcon(new ImageIcon(IconsPathConfigurator.ICON_PIN_RED));
		mntmPinApplicationOnTop.setCursor(GuiUtils.CURSOR_HAND);
		mntmPinApplicationOnTop.setToolTipText("Enable \"always on top\" over other windows (if supported)");
		
		// file submenu
		mntmInspectCurrentConfiguration.setIcon(new ImageIcon(IconsPathConfigurator.ICON_CONF_MANAGER));
		mnLastWorkspaces.setIcon(new ImageIcon(IconsPathConfigurator.iconFolderPath));
		mnLastOpenedFile.setIcon(new ImageIcon(IconsPathConfigurator.ICON_CLOCK));
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
		mntmFullScreenMode.setIcon(new ImageIcon(IconsPathConfigurator.ICON_EXPAND));
		mntmImmersiveMode.setIcon(new ImageIcon(IconsPathConfigurator.ICON_IMMERSIVE));

		// ScreepT submenu
		mntmCrypter.setIcon(new ImageIcon(IconsPathConfigurator.ICON_SCREEPT));
		mntmgenerateKeyImageSub.setIcon(new ImageIcon(IconsPathConfigurator.ICON_KEYGEN));
		mntmViewKeyDictionary.setIcon(new ImageIcon(IconsPathConfigurator.ICON_KEY_DICT));
		
		// Various submenu
		mntmTextEditor.setIcon(new ImageIcon(IconsPathConfigurator.ICON_TEXT_EDITOR));
		mntmnetInfo.setIcon(new ImageIcon(IconsPathConfigurator.ICON_NET_INFO));
		mntmhwInfo.setIcon(new ImageIcon(IconsPathConfigurator.ICON_HW_INFO));
		mntmHashChecker.setIcon(new ImageIcon(IconsPathConfigurator.ICON_HASHING));
		mntmColorPicker.setIcon(new ImageIcon(IconsPathConfigurator.ICON_COLOR_SAMPLER));
		mntmClipboardStandalone.setIcon(new ImageIcon(IconsPathConfigurator.ICON_CLIPBOARD));
		mnConversionsAndOther.setIcon(new ImageIcon(IconsPathConfigurator.ICON_CONVERSION));
		mnFilesAndDirectories.setIcon(new ImageIcon(IconsPathConfigurator.ICON_FILE_N_DIR));
		
		mntmImageConverter.setIcon(new ImageIcon(IconsPathConfigurator.ICON_IMAGE_CONVERTER));
		
		mntmTouppercaseDirectory.setIcon(new ImageIcon(IconsPathConfigurator.ICON_UPPERCASE_FIRST));
		mntmMakeDirsFromPath.setIcon(new ImageIcon(IconsPathConfigurator.ICON_CREATE_FOLDER));
		mntmDeleteDirectoryContentByExtension.setIcon(new ImageIcon(IconsPathConfigurator.ICON_DELETE_TARGETED));
		mntmDeleteDirectoryAllContentRecursive.setIcon(new ImageIcon(IconsPathConfigurator.ICON_DELETE_FOLDER));
		mntmEmptyDirectoryRecursive.setIcon(new ImageIcon(IconsPathConfigurator.ICON_DELETE_DIRS_ONLY));
		mntmCopyDirStructure.setIcon(new ImageIcon(IconsPathConfigurator.ICON_COPY_DIR_STRUCTURE));

		// Help submenu
		mntmAboutScreept.setIcon(new ImageIcon(IconsPathConfigurator.ICON_ABOUT));
		mntmExportLog.setIcon(new ImageIcon(IconsPathConfigurator.ICON_EXPORT_LOG));
		mntmEraseLogs.setIcon(new ImageIcon(IconsPathConfigurator.ICON_DELETE));
		mntmUserDocumentation.setIcon(new ImageIcon(IconsPathConfigurator.ICON_USER_GUIDE));
		mntmReturnToWelcome.setIcon(new ImageIcon(IconsPathConfigurator.ICON_HOME_PAGE));
		mntmViewWebsite.setIcon(new ImageIcon(IconsPathConfigurator.ICON_WEBSITE));
		mntmLookForUpdates.setIcon(new ImageIcon(IconsPathConfigurator.ICON_FOLDER_WEB));
		
		MenuSetter.setIconTextGap(mnrestartPanel, 20);
		mnrestartPanel.setIcon(new ImageIcon(IconsPathConfigurator.ICON_RELOAD_CONF));
		mntmResetAllPanels.setIcon(new ImageIcon(IconsPathConfigurator.ICON_RELOAD_CONF));
		mntmResetTextEditor.setIcon(new ImageIcon(IconsPathConfigurator.ICON_TEXT_EDITOR));
		mntmResetCrypterPanel.setIcon(new ImageIcon(IconsPathConfigurator.ICON_CRYPT));
		mntmResetConfigManager.setIcon(new ImageIcon(IconsPathConfigurator.ICON_CONF_MANAGER));
	}
	
	/**
	 * This method adds fake keyListeners (see customJmenuItem class in the overridden method setAccelerator)
	 * 
	 * The real ones resides in the class "MainGuiShortcutsListener": those are the shortcuts used in scrypt
	 */
	void addMenuShorctutsTips() {
		
		// file submenu
		mntmInspectCurrentConfiguration.setAccelerator(KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_1, ALT_MASK | SHIFT_MASK));
		mntmExportPrjSub.setAccelerator(KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_E, CTRL_MASK | ALT_MASK | SHIFT_MASK));
		mntmloadPrjSub.setAccelerator(KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_O, CTRL_MASK | ALT_MASK | SHIFT_MASK));
		mntmReloadPrjSub.setAccelerator(KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_R, CTRL_MASK | ALT_MASK | SHIFT_MASK));
		mntmSaveCurrent.setAccelerator(KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_S, CTRL_MASK | ALT_MASK | SHIFT_MASK));
		mntmSaveCurrentAs.setAccelerator(KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_W, CTRL_MASK | ALT_MASK | SHIFT_MASK));
		mntmloadSrcrYptDefaultConf.setAccelerator(KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_D, CTRL_MASK | ALT_MASK | SHIFT_MASK));
		mntmExit.setAccelerator(KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_F4, ALT_MASK ));
		
		// preferences submenu
		mntmGenOpt.setAccelerator(KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_2, ALT_MASK | SHIFT_MASK));
		mntmCustomizeUIView.setAccelerator(KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_3, ALT_MASK | SHIFT_MASK));
		mntmFullScreenMode.setAccelerator(KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_ENTER, ALT_MASK | SHIFT_MASK));
		mntmImmersiveMode.setAccelerator(KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_M, CTRL_MASK | ALT_MASK | SHIFT_MASK));
		
		// ScreepT submenu
		mntmCrypter.setAccelerator(KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_S, ALT_MASK | SHIFT_MASK));
		mntmgenerateKeyImageSub.setAccelerator(KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_K, ALT_MASK | SHIFT_MASK));
		mntmViewKeyDictionary.setAccelerator(KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_D, ALT_MASK | SHIFT_MASK));
		
		// Various submenu
		mntmTextEditor.setAccelerator(KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_T, ALT_MASK | SHIFT_MASK));
		mntmnetInfo.setAccelerator(KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_N, ALT_MASK | SHIFT_MASK));
		mntmhwInfo.setAccelerator(KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_Y, ALT_MASK | SHIFT_MASK));
		mntmHashChecker.setAccelerator(KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_U, ALT_MASK | SHIFT_MASK));
		mntmColorPicker.setAccelerator(KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_P,CTRL_MASK | ALT_MASK | SHIFT_MASK));
		mntmClipboardStandalone.setAccelerator(KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_P, ALT_MASK | SHIFT_MASK));
		mntmImageConverter.setAccelerator(KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_I, ALT_MASK | SHIFT_MASK));
		
			// Folder and files functions
			mntmTouppercaseDirectory.setAccelerator(KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_0, ALT_MASK | SHIFT_MASK));
			mntmMakeDirsFromPath.setAccelerator(KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_9, ALT_MASK | SHIFT_MASK));
			mntmCopyDirStructure.setAccelerator(KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_8, ALT_MASK | SHIFT_MASK));
			mntmDeleteDirectoryContentByExtension.setAccelerator(KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_7, ALT_MASK | SHIFT_MASK));
			mntmDeleteDirectoryAllContentRecursive.setAccelerator(KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_6, ALT_MASK | SHIFT_MASK));
			mntmEmptyDirectoryRecursive.setAccelerator(KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_5, ALT_MASK | SHIFT_MASK));
		
		// Help submenu
		mntmAboutScreept.setAccelerator(KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_PERIOD, ALT_MASK | SHIFT_MASK));
		mntmExportLog.setAccelerator(KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_E, ALT_MASK | SHIFT_MASK));
		mntmEraseLogs.setAccelerator(KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_CANCEL, ALT_MASK | SHIFT_MASK));
		mntmReturnToWelcome.setAccelerator(KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_H, ALT_MASK | SHIFT_MASK));
		mntmViewWebsite.setAccelerator(KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_4, CTRL_MASK | ALT_MASK | SHIFT_MASK));
		mntmLookForUpdates.setAccelerator(KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_5, CTRL_MASK | ALT_MASK | SHIFT_MASK));
		mntmUserDocumentation.setAccelerator(KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_Q, ALT_MASK | SHIFT_MASK));
		
			// Reset functions 
			mntmResetAllPanels.setAccelerator(KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_0, CTRL_MASK | ALT_MASK | SHIFT_MASK));
			mntmResetTextEditor.setAccelerator(KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_9, CTRL_MASK | ALT_MASK | SHIFT_MASK));
			mntmResetCrypterPanel.setAccelerator(KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_8, CTRL_MASK | ALT_MASK | SHIFT_MASK));
			mntmResetConfigManager.setAccelerator(KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_7, CTRL_MASK | ALT_MASK | SHIFT_MASK));
		
	}
	
	public void addExtraHandlers() {
		mnLastOpenedFile.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
			}
			@Override
			public void mousePressed(MouseEvent e) {
			}
			@Override
			public void mouseExited(MouseEvent e) {
			}
			@Override
			public void mouseEntered(MouseEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						updateLastOpenedFiles(currentConfiguration.getTextEditorOpt().lastOpenedFiles);
					}
				});
			}
			@Override
			public void mouseClicked(MouseEvent e) {
			}
		});

		mnLastWorkspaces.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
			}
			@Override
			public void mousePressed(MouseEvent e) {
			}
			@Override
			public void mouseExited(MouseEvent e) {
			}
			@Override
			public void mouseEntered(MouseEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						updateLastOpenedWorkspaces(currentConfiguration.getTextEditorOpt().lastSelectedWorkspaces);
					}
				});
			}
			@Override
			public void mouseClicked(MouseEvent e) {
			}
		});
	}
	
	public void updateLastOpenedFiles(LimitedConcurrentList<FileNamed> lastOpenedLlist) {

		try {
			@SuppressWarnings("unchecked")
			Vector<FileNamed> valueCopy = (Vector<FileNamed>) lastOpenedLlist.getList().clone();
			Collections.reverse(valueCopy);
			mnLastOpenedFile.removeAll();
			for (FileNamed file : valueCopy) {
				if (file.mFile != null && file.mFile.exists()) {
					JMenuItem newOccurrence = new JMenuItem(file.toString());
					newOccurrence.setFont(GeneralConfig.MENU_LASTFILES_FONT);
					try {
						newOccurrence.setToolTipText(file.getCanonicalAbsPath());
					} catch (Exception e) {
					}
					mnLastOpenedFile.add(newOccurrence);
					addListenerToJMenuItemLastOpenedTxtEditor(newOccurrence);
				}else {
					currentConfiguration.getTextEditorOpt().lastOpenedFiles.removeAllOccurrencesByEquals(file);
				}
			} 
			
			currentConfiguration.writeSingleValue(INItializer.TEXT_EDITOR_OPT, "lastOpenedFiles", lastOpenedLlist.toCharSeparatedString(","));

		} catch (Exception e) {
			logger.error("Error refreshing last opened files", e);
		}
	};

	public void updateLastOpenedWorkspaces(LimitedConcurrentList<FileNamed> lastWorskspacesLlist) {
		
		try {
			@SuppressWarnings("unchecked")
			Vector<FileNamed> valueCopy = (Vector<FileNamed>) lastWorskspacesLlist.getList().clone();
			Collections.reverse(valueCopy);
			mnLastWorkspaces.removeAll();
			for (FileNamed file : valueCopy) {
				if (file.mFile != null && file.mFile.exists()) {
					JMenuItem newOccurrence = new JMenuItem(file.toString());
					newOccurrence.setFont(GeneralConfig.MENU_LASTFILES_FONT);
					try {
						newOccurrence.setToolTipText(file.getCanonicalAbsPath());
					} catch (Exception e) {
					}
					mnLastWorkspaces.add(newOccurrence);
					addListenerToJMenuItemLastWorkspaces(newOccurrence);
				}else if(file.mFile != null){
					currentConfiguration.getTextEditorOpt().lastSelectedWorkspaces.removeAllOccurrencesByEquals(file);
				}
			} 
			
			currentConfiguration.writeSingleValue(INItializer.TEXT_EDITOR_OPT, "lastSelectedWorkspaces", lastWorskspacesLlist.toCharSeparatedString(","));
			
		} catch (Exception e) {
			logger.error("Error refreshing last opened files", e);
		}
	};
	
	public void addListenerToJMenuItemLastOpenedTxtEditor(final JMenuItem target) {
		target.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(target != null && !target.getToolTipText().equals("")) {
					
					// IF text editor not opened open it
					if(initTxtWriter == null || !initTxtWriter.isVisible()) {
						mntmTextEditor.doClick();
					}
					
					// now load file from history
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							File toLoad = new File(target.getToolTipText());
							if (toLoad.exists()) {
								initTxtWriter.getCurrentTabEditor().loadFile(toLoad);
							} else {
								initTxtWriter.dialogHelper.error("File does not exist anymore!", "File does not exist");
								removeFromLastOpened(toLoad);
							}
						}
					});
				}
			}
		});
	}

	public void addListenerToJMenuItemLastWorkspaces(final JMenuItem target) {
		target.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(target != null && !target.getToolTipText().equals("")) {
					
					// IF text editor not opened open it
					if(initTxtWriter == null || !initTxtWriter.isVisible()) {
						mntmTextEditor.doClick();
					}
					
					// now load file from history
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							File toLoad = new File(target.getToolTipText());
							if (toLoad.exists() && toLoad.isDirectory()) {
								initTxtWriter.setWorkspace(toLoad);
							} else {
								initTxtWriter.dialogHelper.error("File does not exist anymore!", "File does not exist");
								removeFromLastWorkspaces(toLoad);
							}
						}
					});
				}
			}
		});
	}
	
	public void emptyLastOpenedSubMenuItems() {
		mnLastOpenedFile.removeAll();
	}

	public void emptyLastWorkspacesSubMenuItems() {
		mnLastWorkspaces.removeAll();
	}
	
	public JMenuItem isFileContained(File searchElem) {
		for(Component currentComponent : mnLastOpenedFile.getComponents()) {
			if(currentComponent instanceof JMenuItem) {
				try {
					if(searchElem.getCanonicalPath().equals(((JMenuItem) currentComponent).getToolTipText())){
						return (JMenuItem) currentComponent;
					}
				} catch (IOException e) {
				}
			}
		}
		
		return new JMenuItem();
	}

	public JMenuItem removeFromLastOpened(File searchElem) {
		for(Component currentComponent : mnLastOpenedFile.getComponents()) {
			if(currentComponent instanceof JMenuItem) {
				try {
					if(searchElem.getCanonicalPath().equals(((JMenuItem) currentComponent).getToolTipText())){
						mnLastOpenedFile.remove((JMenuItem)currentComponent);
						return (JMenuItem) currentComponent;
					}
				} catch (IOException e) {
				}
			}
		}
		
		return new JMenuItem();
	}

	public JMenuItem removeFromLastWorkspaces(File searchElem) {
		for(Component currentComponent : mnLastWorkspaces.getComponents()) {
			if(currentComponent instanceof JMenuItem) {
				try {
					if(searchElem.getCanonicalPath().equals(((JMenuItem) currentComponent).getToolTipText())){
						mnLastWorkspaces.remove((JMenuItem)currentComponent);
						return (JMenuItem) currentComponent;
					}
				} catch (IOException e) {
				}
			}
		}
		
		return new JMenuItem();
	}

	public boolean isFileContainedBool(File searchElem) {
		for(Component currentComponent : mnLastOpenedFile.getComponents()) {
			if(currentComponent instanceof JMenuItem) {
				try {
					if(searchElem.getCanonicalPath().equals(((JMenuItem) currentComponent).getToolTipText())){
						return true;
					}
				} catch (IOException e) {
				}
			}
		}
		
		return false;
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

	public void setMntmSaveCurrent(CustomJmenuItem mntmSaveCurrent) {
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
	
	public CustomJmenuItem getMntmColorPicker() {
		return mntmColorPicker;
	}

	public void setMntmColorPicker(CustomJmenuItem mntmColorPicker) {
		this.mntmColorPicker = mntmColorPicker;
	}

	public CustomJmenuItem getMntmClipboardStandalone() {
		return mntmClipboardStandalone;
	}

	public void setMntmClipboardStandalone(CustomJmenuItem mntmClipboardStandalone) {
		this.mntmClipboardStandalone = mntmClipboardStandalone;
	}

	public CustomJmenuItem getMntmHashChecker() {
		return mntmHashChecker;
	}

	public void setMntmHashChecker(CustomJmenuItem mntmHashChecker) {
		this.mntmHashChecker = mntmHashChecker;
	}

	public JMenuItem getMntmImageConverter() {
		return mntmImageConverter;
	}
	
	public CustomJmenuItem getMntmViewWebsite() {
		return mntmViewWebsite;
	}

	public CustomJmenuItem getMntmLookForUpdates() {
		return mntmLookForUpdates;
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

	public JMenuItem getMntmDeleteDirectoryAllContentRecursive() {
		return mntmDeleteDirectoryAllContentRecursive;
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

	public void setMntmResetAllPanels(CustomJmenuItem mntmResetAllPanels) {
		this.mntmResetAllPanels = mntmResetAllPanels;
	}

	public void setMntmReturnToWelcome(CustomJmenuItem mntmReturnToWelcome) {
		this.mntmReturnToWelcome = mntmReturnToWelcome;
	}

	public JMenuItem getMntmResetTextEditor() {
		return mntmResetTextEditor;
	}

	public JMenuItem getMntmResetConfigManager() {
		return mntmResetConfigManager;
	}

	public JMenuItem getMntmResetCrypterPanel() {
		return mntmResetCrypterPanel;
	}

	public CustomJmenuItem getMntmFullScreenMode() {
		return mntmFullScreenMode;
	}

	public CustomJmenuItem getMntmEraseLogs() {
		return mntmEraseLogs;
	}

	public CustomJmenuItem getMntmEmptyDirectoryRecursive() {
		return mntmEmptyDirectoryRecursive;
	}

	public CustomJmenuItem getMntmCopyDirStructure() {
		return mntmCopyDirStructure;
	}
	
}

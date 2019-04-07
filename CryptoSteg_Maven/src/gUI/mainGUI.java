package gUI;

import javax.swing.JFrame;
import javax.swing.JLabel;

import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;

import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JMenu;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import java.awt.Font;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;

import javax.swing.JFileChooser;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.ini4j.Ini;
import org.ini4j.Wini;

import amfileutils.FileStuff;
import gUI.panels.AboutSectionPanel;
import gUI.panels.ChargeIniLog;
import gUI.panels.CrypterPanel;
import gUI.panels.GeneralOptionsFrame;
import gUI.panels.KeyGenPanel;
import gUI.panels.NetInfoPanel;
import gUI.panels.TextEditor;
import gUI.utils.filedialog.DirectoryManagementGUI;
import gUI.utils.filedialog.INIfileChoosER;
import gUI.utils.progressDialog.TestUpdateProgress;
import gui.utils.LauncherLoaderFrame;
import gUI.panels.GraphicPreferencesFrame;
import gUI.panels.HwInfoPanel;
import gUI.panels.ImageConverterFrame;
import gUI.panels.KeyDictionaryPanel;
import iNIT.INItializer;
import menusettings.MenuSetter;

import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.awt.SystemColor;
import java.awt.Window;

import net.miginfocom.swing.MigLayout;
import utils.SysUtils;

import java.awt.Dialog.ModalExclusionType;
import java.awt.Color;

import javax.swing.JSeparator;
import javax.swing.SwingUtilities;

public class mainGUI {

	// mainGUI logger
	static Logger logger = Logger.getLogger(mainGUI.class);

	private static Date now; // date used for log operations

////////////////////// GENERAL STATIC ELEMENTS /////////////////////////
////////////////////////////////////////////////////////////////////////
	
	private static final long AUTOSAVE_MANAGER_PERIOD=8000; // autosave manager constant check refresh period in millisecond
	private static Thread autosaveModule; // AUTOSAVE MODULE - only if specified in fileOptions
		
	// general application variables
	public static final String APP_VERSION = "0.5";
	public static final String APPLICATION_NAME = "ScrYpT - V" + APP_VERSION;
	public static final String AUTHOR = "A.M. Design and Developement";
	public static final String APP_VERSION_DATE = "4/4/2019";
	public static final String WEBSITE = "www.am_design_developement.com";
	
	// constant files & directories prefix
	public static final String RESOURCES_DIR= "Resources_Scrypt/";
	public static final String DEFAULT_INI= RESOURCES_DIR + "CONFIG.ini";
	public static final String BACKUP_INI= RESOURCES_DIR + "Backup/BACKUP_CONFIG.ini";
	private static final String DEFAULT_INI_FOLDER= "INI_FILES";
	
	// icons and images
	public static final String APPLICATION_MAIN_BCKG = "Logo-AM_v2.jpg";
	public static Image APPLICATION_MAIN_BCKG_IMAGE;
	public static final String ICON_FILE_NAME = "icon.jpg";
	public static Image SW_ICON;
	public static final String LAUNCHER_BACK_IMAGE_PATH = "LAUNCHER/launcher_icon.jpg";
	
	// MENU ICONS //
	
		// file submenu
	public static final String ICON_CONF_MANAGER = RESOURCES_DIR + "Icons/Menu_Icons/manager.png";
	public static final String ICON_LOAD_CUSTOM_CONF = RESOURCES_DIR + "Icons/Menu_Icons/loadConfig.png";
	public static final String ICON_EXPORT_CONF = RESOURCES_DIR + "Icons/Menu_Icons/export.png";
	public static final String ICON_RELOAD_CONF = RESOURCES_DIR + "Icons/Menu_Icons/reload.png";
	public static final String ICON_SAVE_CONF = RESOURCES_DIR + "Icons/Menu_Icons/save.png";
	public static final String ICON_SAVE_AS_DEFAULT_CONF = RESOURCES_DIR + "Icons/Menu_Icons/saveAsDefault.png";
	public static final String ICON_RESET_CONF = RESOURCES_DIR + "Icons/Menu_Icons/reset.png";
	public static final String ICON_EXIT = RESOURCES_DIR + "Icons/Menu_Icons/turnOff.png";
	
		// preferences submenu
	public static final String ICON_GUI_OPT = RESOURCES_DIR + "Icons/Menu_Icons/graphics.png";
	public static final String ICON_GEN_OPT = RESOURCES_DIR + "Icons/Menu_Icons/options.png";
	
		// Screept submenu
	public static final String ICON_FILE_CRYPTER = RESOURCES_DIR + "Icons/Menu_Icons/crypter.png";
	public static final String ICON_KEYGEN = RESOURCES_DIR + "Icons/Menu_Icons/imageWriter.png";
	public static final String ICON_KEY_DICT = RESOURCES_DIR + "Icons/Menu_Icons/keyDictionary.png";
	
		// Various submenu
	public static final String ICON_TEXT_EDITOR = RESOURCES_DIR + "Icons/Menu_Icons/textEditor.png";
	public static final String ICON_NET_INFO = RESOURCES_DIR + "Icons/Menu_Icons/network.png";
	public static final String ICON_HW_INFO = RESOURCES_DIR + "Icons/Menu_Icons/hardware.png";
	public static final String ICON_CONVERSION = RESOURCES_DIR + "Icons/Menu_Icons/conversion.png";
	public static final String ICON_FILE_N_DIR = RESOURCES_DIR + "Icons/Menu_Icons/folderActions.png";
	
	public static final String ICON_IMAGE_CONVERTER = RESOURCES_DIR + "Icons/Menu_Icons/imageConverter.png";
	
	public static final String ICON_UPPERCASE_FIRST = RESOURCES_DIR + "Icons/Menu_Icons/uppercase.png";
	public static final String ICON_CREATE_FOLDER = RESOURCES_DIR + "Icons/Menu_Icons/createFolder.png";
	public static final String ICON_DELETE_FOLDER = RESOURCES_DIR + "Icons/Menu_Icons/deleteFolder.png";
	
		// help submenu
	public static final String ICON_ABOUT = RESOURCES_DIR + "Icons/Menu_Icons/about.png";
	public static final String ICON_EXPORT_LOG = RESOURCES_DIR + "Icons/Menu_Icons/logExport.png";
	public static final String ICON_USER_GUIDE = RESOURCES_DIR + "Icons/Menu_Icons/userGuide.png";
	public static final String ICON_HOME_PAGE = RESOURCES_DIR + "Icons/Menu_Icons/homePage.png";
		
	// END MENU ICONS//
	
	// log_trace location
	private static final String LOG_PROPERTIES_PATH = RESOURCES_DIR + "log4j.properties";
	private static final String LOG_PATH = "LOG/Execution_Log_Trace.log";
	
	// temp decryptedFolder
	public static final String TEMP_DECRIPTED_ROOT_PATH = System.getProperty("user.home")+"/Desktop/ScrYpT_decrypted_tempFiles";
	public static final File TEMP_DECRIPTED_ROOT = new File(TEMP_DECRIPTED_ROOT_PATH);
	
	public static String currentJvmVersion = "";
	public static final String MAX_JVM_COMPLETE_COMPATIBILITY = "1.9.0";
	
//////////////////////////// GUI ELEMENTS //////////////////////////////
////////////////////////////////////////////////////////////////////////
	
	// background image
	private JLabel labelBACKground;
	
	// the main frame for placing the content
	volatile private JFrame frmScryptV;
	
	// FRAMES THAT OPEN IN A NEW DIALOG
	private static GraphicPreferencesFrame panelGuiOptions;
	private static KeyGenPanel panelKeyGen;
	private static KeyDictionaryPanel panelKeyDict;
	private static GeneralOptionsFrame panelGeneralOptions;
	private static ImageConverterFrame panelImgConv;
	private static NetInfoPanel panelNetInfo;
	private static HwInfoPanel panelHwInfo;
	private static AboutSectionPanel panelAbout;
	
	// PANELS THAT OPEN IN THE MAIN FRAME
	private TextEditor initTxtWriter;
	private JPanel panelCrypter;
	private ChargeIniLog configManagerPanel;
	
	// MENU ITEMS
	private JMenuBar menuBar;
	
	private JMenu mnFile;
	private JMenu mnPreferences;
	private JMenu mnCryptoSteg; 
	private JMenu mnVarious;
	private JMenu mnConversionsAndOther;
	private JMenu mnFilesAndDirectories;
	private JMenu mnHelp;
	
	private JMenuItem mntmInspectCurrentConfiguration;
	private JMenuItem mntmloadPrjSub;
	private JMenuItem mntmExportPrjSub;
	private JMenuItem mntmReloadPrjSub;
	private JMenuItem mntmSaveCurrentAs;
	private JMenuItem mntmloadSrcrYptDefaultConf;
	private JMenuItem mntmExit;
	private JMenuItem mntmUserDocumentation;
	private JMenuItem mntmGenOpt;
	private JMenuItem mntmCustomizeUIView;
	private JMenuItem mntmCrypter;
	private JMenuItem mntmgenerateKeyImageSub;
	private JMenuItem mntmViewKeyDictionary;
	private JMenuItem mntmTextEditor;
	private JMenuItem mntmnetInfo;
	private JMenuItem mntmhwInfo;
	private JMenuItem mntmImageConverter;
	private JMenuItem mntmReturnToWelcome;
	private JMenuItem mntmAboutScreept;
	private JMenuItem mntmExportLog; 
	private JMenuItem mntmTouppercaseDirectory;
	private JMenuItem mntmDeleteDirectoryContentRecursive;
	private JMenuItem mntmMakeDirsFromPath;
	
////////////////////////// MAIN GUI ELEMENTS ///////////////////////////
////////////////////////////////////////////////////////////////////////
	
	// this file is the abstraction of the current settings used by various sub-programs and application of this main_GUI.
	// if you need to get or set current configuration in volatile memory, then use pojo's mapped in this object, please.	
	public static INItializer currentConfiguration;
	
	// Filed for uppercaseFirstLetter "various' function"
	private String last_dir_used; 
	
	// global values representing the screen size
	public static int screenWidth;
	public static int screenHeight;

	// some used lookAndFeel
	public static String NimbusLookAndFeel = "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel";
	public static String WindowsLookAndFeel = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
	public static String preferredStyle = NimbusLookAndFeel; // preferredStyle (initialized to fully avoid null values)
	public static boolean DARK_THEME; // true if dark theme is selected
	
	private int menuSize;
	private int subMenuSize;
	
	// FLAGS TO SIGNAL THAT A MODULE IS OPENED (JDIALOG) 
	// for jpanels they refresh only the first time opened or if manually refreshed 
	// (-> TODO manual refresh)
	private static boolean flagFrameKeyGen = false;
	private static boolean flagFrameGenOptions = false;
	private static boolean flagFrameGuiOptions = false;
	private static boolean flagFrameKeyDict = false;	
	private static boolean flagImgConv = false;
	private static boolean flagNetInfoPanel = false;
	private static boolean flagHwInfoPanel = false;
	private static boolean flagAboutPanel = false;
	
	// flags for no-window operations
	private static boolean FLAG_mntmExit = false;
	private static boolean FLAG_loadPrjSub = false;
	private static boolean FLAG_savePrjSub = false;
	private static boolean FLAG_loadDefaultPrjSub = false;
	private static boolean FLAG_mntmSaveCurrentAs = false;
	private static boolean FLAG_loadSrcrYptDefaultConf = false;
	private static boolean FLAG_mntmExportLog = false;
	private static boolean FLAG_mntmTouppercaseDirectory = false;
	private static boolean FLAG_mntmDeleteDirectoryContentRecursive = false;
	private static boolean FLAG_mntmMakeDirsFromPath = false;
	
	// flag to close all suspended threads when exitings Main App
	volatile private static boolean mainAppRunning = false;
	
	// main app object reference
	private mainGUI mainAPP = this;
	
	// loader to visualize while loading application
	public static LauncherLoaderFrame loaderGraphics;
	
	/**
	 * MAIN METHOD
	 * 
	 * Calls contructor, initialize & Launch the application.
	 */
	public static void main(String[] args) {
		
		// OPEN LOADING PANEL
		loaderGraphics = new LauncherLoaderFrame("Loading Launcher...", LAUNCHER_BACK_IMAGE_PATH, 280);
		for (int i = 0; i < 10; i++) {
			String dots = new String(new char[i+1]).replace("\0", ".");
			String msg = "Initializing "+ APPLICATION_NAME + dots;
			loaderGraphics.update(msg, i/2);
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				logger.error("Error waiting before starting launcher", e);
			} 
		}
		
		new Thread(new Runnable() {
			public void run() {
				try {
					
					now = new Date();
					mainAppRunning = true;

					// empty old log file
					// useful for test phase
//					PrintWriter writer;
//					try {
//						writer = new PrintWriter(LOG_PATH);
//						writer.print("");
//						writer.close();
//					} catch (FileNotFoundException e) {
//						e.printStackTrace();
//					}
					
					// Initialize logger properties
					PropertyConfigurator.configure(LOG_PROPERTIES_PATH);
					logger.debug("\n\n##################################################\n\n"+now.toString()+"\n\n"
							+ APPLICATION_NAME+" Started now!\n\n"
							+ "##################################################\n\n");
					
					// INITIALIZE CONFIGURATION FROM CONFIG DEFAULT FILE
					try {
						currentConfiguration = new INItializer(new Wini(new File(DEFAULT_INI)));
						currentConfiguration.readConfigIniFile();
					} catch (IOException e) {
						logger.error("Exception happened during loading of file [Resources_Scrypt/CONFIG.ini]!", e);
					}
					
		loaderGraphics.update("Initializing graphics... ", 15);
					
					// START MAIN WINDOW CONSTRUCTOR
					logger.debug("Initializing main GUI-start");
					mainGUI window = new mainGUI();
					logger.debug("Main GUI initialized correctly.");
					
		loaderGraphics.update("Starting autosave modules... ", 85);
					
					// ACTIVE MODULES FLAG UPDATER THREAD
					new Thread(new Runnable() {
						
						@Override
						public void run() {
							logger.debug("## MODULES FLAG UPDATER started");
							while(mainAppRunning) {
								try {
									// sleep small time to adapt
									Thread.sleep(600);
									if(!GraphicPreferencesFrame.active) {
										flagFrameGuiOptions=false;
									}
									if(!KeyGenPanel.active){
										flagFrameKeyGen=false;
									}
									if(!KeyDictionaryPanel.active) {
										flagFrameKeyDict=false;
									}
									if(!GeneralOptionsFrame.active) {
										flagFrameGenOptions=false;
									}
									if(!ImageConverterFrame.active) {
										flagImgConv=false;
									}
									if(!NetInfoPanel.active) {
										flagNetInfoPanel=false;
									}
									if(!HwInfoPanel.active) {
										flagHwInfoPanel=false;
									}
									if(!AboutSectionPanel.active) {
										flagAboutPanel=false;
									}
								} catch (InterruptedException e) {
									logger.error("Exception happened!", e);
								}
							}
						}
					}).start();
					
					
					// AUTOSAVE MODULE - only if specified in fileOptions
					autosaveModule = new Thread(new Runnable() {
						@Override
						public void run() {
							logger.debug("## AUTOSAVE module started, interval: "+currentConfiguration.getFileOpt().getAutosaveInterval() + " milliseconds");
							while (mainAppRunning) {
												
								try {
									// sleep time is the interval configured by user
									Thread.sleep(currentConfiguration.getFileOpt().getAutosaveInterval());
								} catch (InterruptedException e) {
							        logger.warn("## AUTOSAVE MODULE interrupted.");
								}
								if(currentConfiguration.getFileOpt().isFlagAutosave()) {
									try {
										currentConfiguration.writeCurrentConfigIniFile();
										logger.debug("AUTOSAVE ON: file saved ["
												+ currentConfiguration.getIniFile().getFile().getAbsolutePath() + "]");
									} catch (Exception e) {
										logger.error("Exception happened!", e);
									}
								}
							}
							logger.debug("## AUTOSAVE module Stopped (Closing ScrYpt)");
						}
					});
					
					// AUTOSAVE MANAGER (refreshes every 4 second)
					Thread autosaveManager = new Thread(new Runnable() {
						
						@Override
						public void run() {
							logger.debug("## AUTOSAVE-MANAGER module started - FlagAutosave from config.ini: "+currentConfiguration.getFileOpt().isFlagAutosave());
							while(mainAppRunning) {
								try {
									// every period specified within the fields
									Thread.sleep(AUTOSAVE_MANAGER_PERIOD);
									
									if(currentConfiguration.getFileOpt().isFlagAutosave() && !autosaveModule.isAlive()) {
										// if thread autosave is not started and autosave is releved as ON -> start autosave thread
										logger.warn("## AUTOSAVE-MANAGER: detected flagAutosave=true & Autosave is not running yet -> Starting autosave module...");
										autosaveModule.start();
									}else if(!currentConfiguration.getFileOpt().isFlagAutosave() && autosaveModule.isAlive()) {
										// if thread autosave is started and autosave is releved as OFF -> kill autosave thread
										logger.warn("## AUTOSAVE-MANAGER: detected flagAutosave=false & Autosave is alive ");
										autosaveModule.interrupt();
										logger.warn("## AUTOSAVE-MANAGER: [AUTOSAVE OFF] autosave module interrupted.");
									}else {
										logger.debug("## AUTOSAVE-MANAGER: [AUTOSAVE "+currentConfiguration.getFileOpt().isFlagAutosave()+"] -> autosave Module running["+autosaveModule.isAlive()+"] - Interval: "+currentConfiguration.getFileOpt().getAutosaveInterval()/1000+"s");
									}
									
								} catch (InterruptedException e) {
						        	logger.warn("## AUTOSAVE-MANAGER interrupted.");
								}
							}
							logger.warn("## AUTOSAVE-MANAGER Stopped (Closing ScrYpt)");
						}
					});
					
					// start autosave manager module
					autosaveManager.start();
					
					// WINDOW CLOSE LISTENER
					window.frmScryptV.addWindowListener(new java.awt.event.WindowAdapter() {
					    @Override
					    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
					    	
							Window activeWindow = javax.swing.FocusManager.getCurrentManager().getActiveWindow();
					    	window.frmScryptV.setAlwaysOnTop(true);
					    	
					    	if (JOptionPane.showConfirmDialog(window.frmScryptV, "Really want to exit? :(", "Exit Program",
									JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION) {
					    		
					    		// perform closing operation and exit
					    		closeApplication();
					    	}else {
					    		
					    		window.frmScryptV.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
					    		window.frmScryptV.setAlwaysOnTop(false);
					    		activeWindow.toFront();
					    		activeWindow.requestFocus();
					    	}
					  	}
					});
					
					loaderGraphics.update("ScrYpT is Ready! :)", 100);
					
					initGUI();
					
					window.frmScryptV.setVisible(true);
					window.frmScryptV.toFront();
					window.frmScryptV.requestFocus();
					
					// ASK FOR USERNAME IF NULL OR FIRST TIME
					if(currentConfiguration.getGenOpt().getUserName().equals("") || currentConfiguration.getGenOpt().getUserName().length()>20) {
						currentConfiguration.getGenOpt().setUserName(window.askUsername());
					}
					window.startTitleUpdater();
					
					//window.frmScryptV.getContentPane().setPreferredSize( new Dimension(screenWidth, screenHeight));
				} catch (Exception e) {
					logger.error("Exception happened!", e);
				}
			}
		}).start();
	}
	
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

	/**
	 * Constructor: it creates the application, and initialize components 
	 * of main window using the function initialize()
	 */
	public mainGUI() {
		
		// initialize value for function uppercaseDirectories ()
		last_dir_used=currentConfiguration.getFileOpt().getLastSrcFolderPath();
		
		// INITIALIZE MAIN_GUI (See the initialize() function below this constructor)
		initialize();
		logger.debug("## GUI components started");
		
		// SW window icon
		try {
			SW_ICON = ImageIO.read(new File(RESOURCES_DIR + ICON_FILE_NAME));
			frmScryptV.setIconImage(SW_ICON);
			logger.debug("icon image setted to main window");
		} catch (IOException e1) {
			logger.error("Exception happened!", e1);
		}
		
		logger.debug("## TITLE UPDATER started.");
	}
	// END CONSTRUCTOR
	
	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {

 loaderGraphics.update("Initializing screen properties...", 30);
 
		// INITIALIZE SCREEN PROPOERTIES
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		screenWidth = gd.getDisplayMode().getWidth();
		screenHeight = gd.getDisplayMode().getHeight();
				
		menuSize = currentConfiguration.getGuiOpt().getMenuFontSize();
		subMenuSize = menuSize-2;
		
		// INITIALIZE MAIN FRAME
		frmScryptV = new JFrame();
		frmScryptV.setModalExclusionType(ModalExclusionType.TOOLKIT_EXCLUDE);
		frmScryptV.getContentPane().setBackground(SystemColor.windowText);
		frmScryptV.setBounds(0, 0, 1600, 900 );
		
		// FULL SCREEN SETTING
	    frmScryptV.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frmScryptV.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frmScryptV.setVisible(true);
        
        setBackgroundImage();
        
        // INIT MENU and SUMBMENUS
        menuBar = new JMenuBar();
		menuBar.setOpaque(true);
		frmScryptV.setJMenuBar(menuBar);
		
		mnFile = new JMenu("File");
		mnPreferences = new JMenu("Preferences");
		mnCryptoSteg = new JMenu("ScreepT");
		mnVarious = new JMenu("Various");
		mnConversionsAndOther = new JMenu("Conversions and Media");
		mnFilesAndDirectories = new JMenu("Files And Directories");
		mnHelp = new JMenu("Help");
	
 loaderGraphics.update("Defining handlers...", 40);
		
		//////////////////////////// DEFINE HANDLERS  //////////////////////////////////
		
		// NOT OPENING VIEW - EXIT AND CLOSE SCRYPT
		mntmExit = new JMenuItem(new AbstractAction("Exit") {
			public void actionPerformed(ActionEvent e) {
				if (!FLAG_mntmExit) {
					FLAG_mntmExit = true;
					
					// backup current active window
					Window activeWindow = javax.swing.FocusManager.getCurrentManager().getActiveWindow();
					frmScryptV.setAlwaysOnTop(true);
					
					if (JOptionPane.showConfirmDialog(frmScryptV, "Really want to exit? :(", "Exit Program",
							JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION) {
						
						// perform closing actions and exit application
						closeApplication();
					}
					
					frmScryptV.setAlwaysOnTop(false);
					activeWindow.toFront();
		    		activeWindow.requestFocus();
					FLAG_mntmExit = false;
				}
			}
		});

		// NOT OPENING VIEW - LOAD CUSTOM CONFIGURATION
		mntmloadPrjSub = new JMenuItem(new AbstractAction("Load Custom Configuration") {
		    public void actionPerformed(ActionEvent ae) {
		   
		    		if (!FLAG_loadPrjSub) {
		    			FLAG_loadPrjSub = true;
						JPanel contentPane = (JPanel) frmScryptV.getContentPane();
						INIfileChoosER panel = new INIfileChoosER(currentConfiguration, DEFAULT_INI_FOLDER);
						panel.setBounds(20, 20, frmScryptV.getWidth(), frmScryptV.getHeight() / 2);
						FLAG_loadPrjSub = false;
					}
		    }
		});

		// NOT OPENING VIEW - SAVE CUSTOM CONFIGURATION
		mntmExportPrjSub = new JMenuItem(new AbstractAction("Export Current Configuration") {
		    public void actionPerformed(ActionEvent ae) {
		    	if (!FLAG_savePrjSub) {
		    		FLAG_savePrjSub = true;
					JPanel contentPane = (JPanel) frmScryptV.getContentPane();
					INIfileChoosER panel = new INIfileChoosER(currentConfiguration, (JLabel)null);
					panel.setBounds(20, 20, frmScryptV.getWidth(), frmScryptV.getHeight() / 2);
					FLAG_savePrjSub = false;
				}  	
		    }
		});

		// NOT OPENING VIEW - RELOAD CONFIGURATION
		mntmReloadPrjSub = new JMenuItem(new AbstractAction("Reload Configuration") {
		    public void actionPerformed(ActionEvent ae) {

		    	if (!FLAG_loadDefaultPrjSub) {
		    		FLAG_loadDefaultPrjSub = true;
					if (JOptionPane.showConfirmDialog(frmScryptV,
							"Really want to load default config(unsaved changes will be lost)? ",
							"Warning - Reload INI default config?",
							JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION) {
						try {
							currentConfiguration.setIniReadFile(new Ini(new File(DEFAULT_INI)));
							currentConfiguration.setIniFile(new Wini(new File(DEFAULT_INI)));
							JOptionPane.showMessageDialog(frmScryptV, "Configuration reloaded!");
						} catch (IOException e) {
							JOptionPane.showMessageDialog(frmScryptV,
									"Error opening: " + DEFAULT_INI + " -> maybe it does not exist?");
							logger.error("Exception happened!", e);
						}
						currentConfiguration.readConfigIniFile();
						JOptionPane.showMessageDialog(frmScryptV,
								"File '" + currentConfiguration.getIniFile().getFile().getName() + "' loaded!");
					} else {
						JOptionPane.showMessageDialog(frmScryptV,
								"Configuration not loaded, current CONFIG mantained!");
					} 
					FLAG_loadDefaultPrjSub = false;
				}
		    }
		});

		// NOT OPENING VIEW - SAVE DEFAULT CONFIGURATION
		mntmSaveCurrentAs = new JMenuItem(new AbstractAction("Save Current as Default") {
			public void actionPerformed(ActionEvent e) {
				if (!FLAG_mntmSaveCurrentAs) {
					FLAG_mntmSaveCurrentAs = true;
					try {
						currentConfiguration.setIniFile(new Wini(new File(DEFAULT_INI)));
						currentConfiguration.setIniReadFile(new Ini(new File(DEFAULT_INI)));

					} catch (IOException e1) {
						logger.error("Exception happened!", e1);
					}
					if (JOptionPane.showConfirmDialog(frmScryptV, "Really want to overwrite default configuration?",
							"Warning - Overwrite default configuration?",
							JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION) {
						currentConfiguration.writeCurrentConfigIniFile();
						JOptionPane.showMessageDialog(frmScryptV,
								"File " + currentConfiguration.getIniFile().getFile().getAbsolutePath() + " saved!");
					} else {
						JOptionPane.showMessageDialog(frmScryptV, "Configuration not saved!");
					}
					FLAG_mntmSaveCurrentAs = false;
				}
			}
		});

		// NOT OPENING VIEW - RELOAD STANDARD SCRYPT CONFIGURATION
		mntmloadSrcrYptDefaultConf = new JMenuItem(new AbstractAction("Reset Original") {
		    public void actionPerformed(ActionEvent ae) {

		    	if (!FLAG_loadSrcrYptDefaultConf) {
		    		FLAG_loadSrcrYptDefaultConf = true;
					if (JOptionPane.showConfirmDialog(frmScryptV,
							"Really want to Reset standard ScrypT configuration? Unsaved changes will be lost! ",
							"Warning - Reset INI standard config?",
							JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION) {
						try {
							FileUtils.copyFile(new File(BACKUP_INI), new File(DEFAULT_INI));
							currentConfiguration.setIniReadFile(new Wini(new File(DEFAULT_INI)));
							currentConfiguration.setIniFile(new Wini(new File(DEFAULT_INI)));
						} catch (IOException e) {
							JOptionPane.showMessageDialog(frmScryptV,
									"Error resetting standard configuration: it does not exist or IO error occurred, retry later!");
							logger.error("Exception happened!", e);
						}
						String name = askUsername();
						JOptionPane.showMessageDialog(frmScryptV,
								"File '" + currentConfiguration.getIniFile().getFile().getName() + "' loaded!");
						currentConfiguration.readConfigIniFile();
						currentConfiguration.getGenOpt().setUserName(name);
					} else {
						JOptionPane.showMessageDialog(frmScryptV,
								"Configuration not loaded, current CONFIG mantained!");
					} 
					FLAG_loadSrcrYptDefaultConf = false;
				}
		    }
		});

		// NOT OPENING VIEW - LOG EXPORT
		mntmExportLog = new JMenuItem(new AbstractAction("Export Event Log") {
			public void actionPerformed(ActionEvent e) {
				if (!FLAG_mntmExportLog) {
					FLAG_mntmExportLog = true;
					// show input dialog to select the file(gives null in case of no file selected)
					File selected = fileLogWriteDialog(frmScryptV);
					if (selected != null) {
						logger.debug("\n\n##################################################\n\n" + now.toString()
								+ "\n\n" + APPLICATION_NAME + " EXPORT!\n\n"
								+ "##################################################\n\n");

						// give true to overwrite because fileWriteDialog already set the value to be unique
						if (FileStuff.copyFile(new File(LOG_PATH), selected, true)) {
							JOptionPane.showMessageDialog(frmScryptV, "File correctly saved in " + selected,
									"Log File Exported :)", JOptionPane.INFORMATION_MESSAGE);
							logger.debug("File :" + LOG_PATH + " Exported, exportFile: " + selected);
						} else {
							JOptionPane.showMessageDialog(frmScryptV, "File not saved [" + selected,
									"]  Some error occurred :(", JOptionPane.ERROR_MESSAGE);
							logger.error("File not saved, some error occurred :( ");
						}
					}
					FLAG_mntmExportLog = false;
				}
			}
		});

		// NOT OPENING VIEW - UPPERCASE DIRECTORY'S NAMES FIRST LETTER
		mntmTouppercaseDirectory = new JMenuItem(new AbstractAction("Uppercase First letter - Files&Directories") {
			public void actionPerformed(ActionEvent e) {
				if (!FLAG_mntmTouppercaseDirectory) {
					FLAG_mntmTouppercaseDirectory = true;
					last_dir_used = DirectoryManagementGUI.renameFirstLetterDirNames(frmScryptV, last_dir_used);
					FLAG_mntmTouppercaseDirectory = false;
				}
			}
		});
		
		// NOT OPENING VIEW - DELETE RECURSIVELY DIRECTORY'S CONTENT
		mntmDeleteDirectoryContentRecursive = new JMenuItem(new AbstractAction("Delete Directory's content (Recursive)") {
			public void actionPerformed(ActionEvent e) {
				if (!FLAG_mntmDeleteDirectoryContentRecursive) {
					FLAG_mntmDeleteDirectoryContentRecursive = true;
					DirectoryManagementGUI.deleteContentOfDir(frmScryptV, currentConfiguration);
					FLAG_mntmDeleteDirectoryContentRecursive = false;
				}
			}
		});

		// NOT OPENING VIEW - MAKE DIRS FROM STRING PATH
		mntmMakeDirsFromPath = new JMenuItem(new AbstractAction("Create directories from String path") {
			public void actionPerformed(ActionEvent e) {
				if (!FLAG_mntmMakeDirsFromPath) {
					FLAG_mntmMakeDirsFromPath = true;
					DirectoryManagementGUI.makeDirsFromPath(frmScryptV, currentConfiguration);
					FLAG_mntmMakeDirsFromPath = false;
				}
			}
		});
		
	///////////////// HANDLERS THAT OPEN A NEW WINDOW  ////////////////////////
		
		// open in a new window - CUSTOMIZE GRAPHICS
		mntmCustomizeUIView = new JMenuItem(new AbstractAction("Customize Graphics") {
			public void actionPerformed(ActionEvent e) {
				if (!flagFrameGuiOptions) {
					flagFrameGuiOptions = true;
					panelGuiOptions = new GraphicPreferencesFrame(currentConfiguration);
					panelGuiOptions.setBounds(500, 300, 950, 330);
					panelGuiOptions.setVisible(true);
					panelGuiOptions.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					panelGuiOptions.setTitle("Apply/Save Preferences [Customize - Apply - Save]");
				}else {
					panelGuiOptions.setState(java.awt.Frame.NORMAL);
					panelGuiOptions.requestFocus();
				}
			}
		});
		
		// open in a new window - GENERAL OPTIONS
		mntmGenOpt = new JMenuItem(new AbstractAction("General Options") {
			public void actionPerformed(ActionEvent e) {
				if (!flagFrameGenOptions) {
					flagFrameGenOptions=true;
					panelGeneralOptions = new GeneralOptionsFrame(currentConfiguration);
					panelGeneralOptions.setBounds(400, 300, 700, 250);
					panelGeneralOptions.setVisible(true);
					panelGeneralOptions.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					panelGeneralOptions.setTitle("Apply/Save General Options [Customize - Apply - Save]");
				}else {
					panelGeneralOptions.setState(java.awt.Frame.NORMAL);
					panelGeneralOptions.requestFocus();
				}
			}
		});
		
		// open in a new window - KEYGEN
		mntmgenerateKeyImageSub = new JMenuItem(new AbstractAction("Generate Key Image") {
		    public void actionPerformed(ActionEvent ae) {
		    	if (!flagFrameKeyGen) {
		    		flagFrameKeyGen = true;
		    		panelKeyGen = new KeyGenPanel(currentConfiguration);
					panelKeyGen.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					panelKeyGen.setBounds(150, 150, 1100, 800);
					panelKeyGen.setVisible(true);
					panelKeyGen.setTitle("IMG Generator (V1.1) - A.M.2018");
				}else {
					panelKeyGen.setState(java.awt.Frame.NORMAL);
					panelKeyGen.requestFocus();
				}
		    }
		});
		
		// open in a new window - NET INFO
		mntmnetInfo = new JMenuItem(new AbstractAction("Network Info") {
		    public void actionPerformed(ActionEvent ae) {
		    	if (!flagNetInfoPanel) {
		    		flagNetInfoPanel = true;
		    		new Thread(new Runnable() {
						
						@Override
						public void run() {
							TestUpdateProgress prog = new TestUpdateProgress();
							prog.getPb().setIndeterminate(true);
							prog.getLabel().setText("Loading Network informations...");
							prog.getPb().setString("Loading...");
							panelNetInfo = new NetInfoPanel(currentConfiguration);	
							panelNetInfo.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
							panelNetInfo.setAlwaysOnTop(true);
							panelNetInfo.setBounds(150, 150, 950, 700);
							panelNetInfo.setVisible(true);
							panelNetInfo.setTitle("Network info - ScrYpT");
				    		prog.getPb().setValue(100);
						}
					}).start();
		    		
				}else {
					panelNetInfo.setState(java.awt.Frame.NORMAL);
					panelNetInfo.requestFocus();
				}
		    }
		});
		
		// open in a new window - NET INFO
		mntmhwInfo = new JMenuItem(new AbstractAction("System Info") {
		    public void actionPerformed(ActionEvent ae) {
		    	if (!flagHwInfoPanel) {
		    		flagHwInfoPanel = true;
		    		new Thread(new Runnable() {
						
						@Override
						public void run() {
							TestUpdateProgress prog = new TestUpdateProgress();
							prog.getPb().setIndeterminate(true);
							prog.getLabel().setText("Loading System informations...");
							prog.getPb().setString("Loading...");
							panelHwInfo = new HwInfoPanel(currentConfiguration);	
							panelHwInfo.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
							panelHwInfo.setBounds(250, 200, 1000, 750);
							panelHwInfo.setAlwaysOnTop(true);
							panelHwInfo.setResizable(false);
							panelHwInfo.setVisible(true);
							panelHwInfo.setTitle("Hardware info - ScrYpT");
				    		prog.getPb().setValue(100);
						}
					}).start();
		    		
				}else {
					panelHwInfo.setState(java.awt.Frame.NORMAL);
					panelHwInfo.requestFocus();
				}
		    }
		});
		
		// open in a new window - DICTIONARY (KEY IMG)
		mntmViewKeyDictionary = new JMenuItem(new AbstractAction("View Key Dictionary") {
		    public void actionPerformed(ActionEvent e) {
		    	if (!flagFrameKeyDict) {
		    		flagFrameKeyDict = true;
		    		panelKeyDict = new KeyDictionaryPanel(currentConfiguration);
		    		panelKeyDict.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		    		panelKeyDict.setBounds(300, 100, 800, 560);
		    		panelKeyDict.setVisible(true);
		    		panelKeyDict.setTitle("Key Dictionary Manager");
				}else {
					panelKeyDict.setState(java.awt.Frame.NORMAL);
					panelKeyDict.requestFocus();
				}
			}
		});
		
		// open in a new window - IMAGE CONVERTER
		mntmImageConverter = new JMenuItem(new AbstractAction("Image Converter") {
			public void actionPerformed(ActionEvent e) {
		    	if (!flagImgConv) {
		    		flagImgConv = true;
		    		panelImgConv = new ImageConverterFrame(currentConfiguration);
		    		panelImgConv.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		    		panelImgConv.setBounds(700, 200, 650, 500);
		    		panelImgConv.setResizable(false);
		    		panelImgConv.setVisible(true);
		    		panelImgConv.setTitle("IMG Generator (V1.1) - A.M.2018");
				}else {
					panelImgConv.setState(java.awt.Frame.NORMAL);
					panelImgConv.requestFocus();
				}
			}
		});
		
		// open in a new window - ABOUT SCRYPT
		mntmAboutScreept = new JMenuItem(new AbstractAction("About - ScrYpt") {
			public void actionPerformed(ActionEvent e) {
				if (!flagAboutPanel) {
					flagAboutPanel = true;
		    		panelAbout = new AboutSectionPanel(mainAPP);
		    		panelAbout.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		    		panelAbout.setBounds(700, 200, 400, 345);
		    		panelAbout.setResizable(false);
		    		panelAbout.setVisible(true);
		    		panelAbout.setTitle(APPLICATION_NAME);
				}else {
					panelAbout.setState(java.awt.Frame.NORMAL);
					panelAbout.requestFocus();
				}
			}
		});
		
		// open in a new window - MANUAL
		mntmUserDocumentation = new JMenuItem(new AbstractAction("User Guide") {
			public void actionPerformed(ActionEvent e) {
				// TODO
			}
		});
		
		// temp tooltip
		mntmUserDocumentation.setEnabled(false);
		mntmUserDocumentation.setToolTipText("Cooming soon!");
		
	////////////////  NOW HANDLERS THAT OPEN IN THE MAIN FRAME  ////////////////////////
		
		// open in main frame - FILE CRYPTER
		mntmCrypter = new JMenuItem(new AbstractAction("ScreepT - Crypt and Decrypt files") {
		    public void actionPerformed(ActionEvent ae) {
		    	JPanel contentPane = (JPanel) frmScryptV.getContentPane();
		    	if(panelCrypter == null) {
			    	panelCrypter = new CrypterPanel(currentConfiguration, frmScryptV);
		    	}
		    	contentPane.removeAll();
		    	contentPane.add(panelCrypter);
		    	contentPane.revalidate(); 
		    	contentPane.repaint();
		    }
		});
		
		// open in main frame - CONFIGURATION MANAGER
		mntmInspectCurrentConfiguration = new JMenuItem(new AbstractAction("Configuration Manager") {
		    public void actionPerformed(ActionEvent ae) {
				   
	    		JPanel contentPane = (JPanel) frmScryptV.getContentPane();
	    		if (configManagerPanel == null) {
					configManagerPanel = new ChargeIniLog(currentConfiguration, mainAPP);
				}
				contentPane.removeAll();
		    	contentPane.add(configManagerPanel);
		    	contentPane.revalidate(); 
		    	contentPane.repaint();
		    }
		});
		
		// open in main frame - TEXT EDITOR
		mntmTextEditor = new JMenuItem(new AbstractAction("Text Editor") {
			public void actionPerformed(ActionEvent e) {
			
				JPanel contentPane = (JPanel) frmScryptV.getContentPane();
				if(initTxtWriter == null) {
					initTxtWriter = new TextEditor(frmScryptV, currentConfiguration);
				}		    	
		    	contentPane.removeAll();
		    	contentPane.add(initTxtWriter);
		    	contentPane.revalidate(); 
		    	contentPane.repaint();
			
			}
		});
		
		// open in main frame - RETURN TO WELCOME PAGE
		mntmReturnToWelcome = new JMenuItem(new AbstractAction("Return to Welcome Page") {
			public void actionPerformed(ActionEvent e) {
				setBackgroundImage();
			}
		});

		// BACKGROUND RESIZER //
		frmScryptV.addComponentListener(new ComponentListener() {
			public void componentShown(ComponentEvent e) {
			}
			public void componentResized(ComponentEvent e) {
				labelBACKground.setSize(frmScryptV.getContentPane().getSize());
			}
			public void componentMoved(ComponentEvent e) {
			}
			public void componentHidden(ComponentEvent e) {
			}
		});
	////////// END OF HANDLERS DECLARATION  ////////////////
	
 loaderGraphics.update("Structuring menu...", 60);

 		addHandlers();
 
		// once each component is declared add it to the menu bar
		structureMenuBar();
		
		// check non compatible options with current JVM
		checkVersionAndDisableUnsupportedOpt();
		
		frmScryptV.requestFocus();
	}
	
	
//////////	NOW SOME USEFUL METHOD  ////////////////
	
	private static void initGUI() {
		// initialize style
		preferredStyle = currentConfiguration.getGuiOpt().getPreferredStyle();
		
		// initialize GUI default Style from ini
		try {
			UIManager.setLookAndFeel(preferredStyle);
			for(Window window: Window.getWindows()) {
    		    SwingUtilities.updateComponentTreeUI(window);
    		}
			logger.debug(preferredStyle+" initialized correctly.");
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e3) {
			logger.error("Exception happened!", e3);
		}
		
		// try to set preferred charset
		try {
			System.setProperty("file.encoding", currentConfiguration.getGenOpt().getCharset());
			logger.info("Charset setted: "+ System.getProperty("file.encoding"));
		} catch (Exception e) {
			logger.error("Exception happened: Erroring setting charset :(", e);		
		}
		
		DARK_THEME = currentConfiguration.getGuiOpt().isDarkGuiNimbus();
		// adjust general look and feel opt
		if(DARK_THEME) {
			updateUIDarkNimbus();
		}else {
			updateUILightNimbus();
		}
	}
	
	private void structureMenuBar() {
		
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
			mnFile.add(mntmloadPrjSub);
			mnFile.add(mntmExportPrjSub);
			mnFile.add(new JSeparator());
			mnFile.add(mntmReloadPrjSub);
			mnFile.add(mntmSaveCurrentAs);
			mnFile.add(new JSeparator());
			mnFile.add(mntmloadSrcrYptDefaultConf);
			mnFile.add(new JSeparator());
			mnFile.add(mntmExit);
			
			mnPreferences.add(mntmCustomizeUIView);
			mnPreferences.add(mntmGenOpt);
			
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
			mnHelp.add(mntmUserDocumentation);
			mnHelp.add(new JSeparator());
			mnHelp.add(mntmReturnToWelcome);
			
				// UNDER-SUB-MENU
				mnConversionsAndOther.add(mntmImageConverter);
				
				mnFilesAndDirectories.add(mntmTouppercaseDirectory);
				mnFilesAndDirectories.add(mntmMakeDirsFromPath);
				mnFilesAndDirectories.add(mntmDeleteDirectoryContentRecursive);
			
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
	public void updateMenuFonts(Font menuBarFont, Font subMenuFont, Font menuItemFont) {

		menuBar.setFont(menuBarFont);
		
		mnFile.setFont(menuBarFont);
		mnHelp.setFont(menuBarFont);
		mnPreferences.setFont(menuBarFont);
		mnCryptoSteg.setFont(menuBarFont);
		mnVarious.setFont(menuBarFont);

		mnFilesAndDirectories.setFont(subMenuFont);
		mnConversionsAndOther.setFont(subMenuFont);

		mntmExit.setFont(menuItemFont);
		mntmInspectCurrentConfiguration.setFont(menuItemFont);
		mntmloadPrjSub.setFont(menuItemFont);
		mntmExportPrjSub.setFont(menuItemFont);
		mntmReloadPrjSub.setFont(menuItemFont);
		mntmSaveCurrentAs.setFont(menuItemFont);
		mntmloadSrcrYptDefaultConf.setFont(menuItemFont);
		mntmCustomizeUIView.setFont(menuItemFont);
		mntmGenOpt.setFont(menuItemFont);
		mntmCrypter.setFont(menuItemFont);
		mntmgenerateKeyImageSub.setFont(menuItemFont);
		mntmnetInfo.setFont(menuItemFont);
		mntmhwInfo.setFont(menuItemFont);
		mntmViewKeyDictionary.setFont(menuItemFont);
		mntmTextEditor.setFont(menuItemFont);
		mntmImageConverter.setFont(menuItemFont);
		mntmTouppercaseDirectory.setFont(menuItemFont);
		mntmMakeDirsFromPath.setFont(menuItemFont);
		mntmDeleteDirectoryContentRecursive.setFont(menuItemFont);
		mntmReturnToWelcome.setFont(menuItemFont);
		mntmAboutScreept.setFont(menuItemFont);
		mntmExportLog.setFont(menuItemFont);
		mntmUserDocumentation.setFont(menuItemFont);
	}
	
	public void addHandlers() {
		
	}
	
	public void addMenuIcons() {
		
		MenuSetter.setIconTextGap(menuBar, 10);
		
		// file submenu
		mntmInspectCurrentConfiguration.setIcon(new ImageIcon(ICON_CONF_MANAGER));
		mntmloadPrjSub.setIcon(new ImageIcon(ICON_LOAD_CUSTOM_CONF));
		mntmExportPrjSub.setIcon(new ImageIcon(ICON_EXPORT_CONF));
		mntmReloadPrjSub.setIcon(new ImageIcon(ICON_RELOAD_CONF));
		mntmSaveCurrentAs.setIcon(new ImageIcon(ICON_SAVE_AS_DEFAULT_CONF));
		mntmloadSrcrYptDefaultConf.setIcon(new ImageIcon(ICON_RESET_CONF));
		mntmExit.setIcon(new ImageIcon(ICON_EXIT));
		
		// preferences submenu
		mntmCustomizeUIView.setIcon(new ImageIcon(ICON_GUI_OPT));
		mntmGenOpt.setIcon(new ImageIcon(ICON_GEN_OPT));
		
		// ScreepT submenu
		mntmCrypter.setIcon(new ImageIcon(ICON_FILE_CRYPTER));
		mntmgenerateKeyImageSub.setIcon(new ImageIcon(ICON_KEYGEN));
		mntmViewKeyDictionary.setIcon(new ImageIcon(ICON_KEY_DICT));
		
		// Various submenu
		mntmTextEditor.setIcon(new ImageIcon(ICON_TEXT_EDITOR));
		mntmnetInfo.setIcon(new ImageIcon(ICON_NET_INFO));
		mntmhwInfo.setIcon(new ImageIcon(ICON_HW_INFO));
		mnConversionsAndOther.setIcon(new ImageIcon(ICON_CONVERSION));
		mnFilesAndDirectories.setIcon(new ImageIcon(ICON_FILE_N_DIR));
		
		mntmImageConverter.setIcon(new ImageIcon(ICON_IMAGE_CONVERTER));
		
		mntmTouppercaseDirectory.setIcon(new ImageIcon(ICON_UPPERCASE_FIRST));
		mntmMakeDirsFromPath.setIcon(new ImageIcon(ICON_CREATE_FOLDER));
		mntmDeleteDirectoryContentRecursive.setIcon(new ImageIcon(ICON_DELETE_FOLDER));
		
		// Help submenu
		mntmAboutScreept.setIcon(new ImageIcon(ICON_ABOUT));
		mntmExportLog.setIcon(new ImageIcon(ICON_EXPORT_LOG));
		mntmUserDocumentation.setIcon(new ImageIcon(ICON_USER_GUIDE));
		mntmReturnToWelcome.setIcon(new ImageIcon(ICON_HOME_PAGE));
	}
	
	public void updateMenuAndLogFontSize(int fontSize){

		// use the same font as main gui but change font size
		UIManager.put("JMenuBar.font", new Font("Segoe UI", Font.BOLD, fontSize ));
		UIManager.put("JMenu.font", new Font("Segoe UI", Font.BOLD, fontSize ));
		UIManager.put("JMenuItem.font", new Font("Segoe UI", Font.BOLD, fontSize-2 ));
	    SwingUtilities.updateComponentTreeUI(frmScryptV);
	}
	
	public static void updateUIDarkNimbus() {
//		UIManager.put( "control", new Color( 128, 128, 128) );
//		  UIManager.put( "info", new Color(128,128,128) );
		  UIManager.put( "nimbusBase", new Color( 18, 30, 49) );
		  UIManager.put( "MenuBar.foreground", Color.white );
//		  UIManager.put( "nimbusAlertYellow", new Color( 248, 187, 0) );
//		  UIManager.put( "nimbusDisabledText", new Color( 128, 128, 128) );
//		  UIManager.put( "nimbusFocus", new Color(115,164,209) );
//		  UIManager.put( "nimbusGreen", new Color(176,179,50) );
//		  UIManager.put( "nimbusInfoBlue", new Color( 66, 139, 221) );
		  UIManager.put( "nimbusLightBackground", new Color( 250, 250, 250) );
//		  UIManager.put( "nimbusOrange", new Color(191,98,4) );
//		  UIManager.put( "nimbusRed", new Color(169,46,34) );
//		  UIManager.put( "nimbusSelectedText", new Color( 255, 255, 255) );
		  UIManager.put( "nimbusSelectionBackground", new Color( 150, 150, 150) );
		 // UIManager.put( "text", new Color( 230, 230, 230) );
		  try {
		    for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
		      if ("Nimbus".equals(info.getName())) {
		          javax.swing.UIManager.setLookAndFeel(info.getClassName());
		          break;
		      }
		    }
		    DARK_THEME=true;
		  } catch (ClassNotFoundException e) {
			  logger.error("Exception happened!", e);
		  } catch (InstantiationException e) {
			  logger.error("Exception happened!", e);
		  } catch (IllegalAccessException e) {
			  logger.error("Exception happened!", e);
		  } catch (javax.swing.UnsupportedLookAndFeelException e) {
			  logger.error("Exception happened!", e);
		  } catch (Exception e) {
			  logger.error("Exception happened!", e);
		  }
		  // Show your JFrame
	}
	
	public static void updateUILightNimbus() {
		
		UIManager.put("nimbusOrange", Color.GREEN);
//		UIManager.put( "control", new Color( 128, 128, 128) );
//		  UIManager.put( "info", new Color(128,128,128) );
		  UIManager.put( "nimbusBase", new Color(51,98,140) );
		  UIManager.put( "MenuBar.foreground", Color.black );
//		  UIManager.put( "nimbusAlertYellow", new Color( 248, 187, 0) );
//		  UIManager.put( "nimbusDisabledText", new Color( 128, 128, 128) );
//		  UIManager.put( "nimbusFocus", new Color(115,164,209) );
//		  UIManager.put( "nimbusGreen", new Color(176,179,50) );
//		  UIManager.put( "nimbusInfoBlue", new Color( 66, 139, 221) );
//		  UIManager.put( "Tree.selectionBackground", Color.BLACK );
		  UIManager.put( "nimbusLightBackground", new Color( 240, 240, 240) );
//		  UIManager.put( "nimbusOrange", new Color(191,98,4) );
//		  UIManager.put( "nimbusRed", new Color(169,46,34) );
//		  UIManager.put( "nimbusSelectedText", new Color( 255, 255, 255) );
		  UIManager.put( "nimbusSelectionBackground", new Color(57,105,138));
		 // UIManager.put( "text", new Color( 230, 230, 230) );
		  try {
		    for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
		      if ("Nimbus".equals(info.getName())) {
		          javax.swing.UIManager.setLookAndFeel(info.getClassName());
		          break;
		      }
		    }
			DARK_THEME=false;
		  } catch (ClassNotFoundException e) {
			  logger.error("Exception happened!", e);
		  } catch (InstantiationException e) {
			  logger.error("Exception happened!", e);
		  } catch (IllegalAccessException e) {
			  logger.error("Exception happened!", e);
		  } catch (javax.swing.UnsupportedLookAndFeelException e) {
			  logger.error("Exception happened!", e);
		  } catch (Exception e) {
			  logger.error("Exception happened!", e);
		  }
		  // Show your JFrame
	}
	
	public static String askUsername() {
		boolean end=false;
		String input="";
		logger.debug("Current user input initialization: void user or lenght>20 detected!");
		do {
			input = JOptionPane.showInputDialog("Please insert your name:", "U-sir");
			if (input==null || input.equals("") || input.length() > 20) {
				JOptionPane.showMessageDialog(null, "Username must be between 1-20 characters!",
						"UserName lenght Error", JOptionPane.WARNING_MESSAGE);
				logger.error("Error in user input, redo.");
			} else {
				currentConfiguration.writeSingleValue(INItializer.GEN_OPT, "userName", input);
				currentConfiguration.getGenOpt().setUserName(input);
				end = true;
			} 
		} while (!end);
		logger.debug("Name selected and saved username: "+input);
		return input;
	}
	
	public void readBackgroundImage() throws IOException {
		BufferedImage image = ImageIO.read(new File(RESOURCES_DIR + APPLICATION_MAIN_BCKG));
        APPLICATION_MAIN_BCKG_IMAGE = image.getScaledInstance(screenWidth-100, screenHeight-100,Image.SCALE_SMOOTH);
	}
	
	public void setBackgroundImage() {
		// setto lo sfondo iniziale dell'app
		try {
			JPanel MAIN_PANEL = new JPanel();
			MAIN_PANEL.setBackground(SystemColor.infoText);
            frmScryptV.getContentPane().add(MAIN_PANEL);
            MAIN_PANEL.setLayout(new MigLayout("", "[90%]", "[1.00][100.52%,baseline]"));
            
            if(APPLICATION_MAIN_BCKG_IMAGE == null) {
            	readBackgroundImage();
            }
            
            labelBACKground = new JLabel(new ImageIcon(APPLICATION_MAIN_BCKG_IMAGE));
		   	JPanel contentPane = (JPanel) frmScryptV.getContentPane();
		   	contentPane.removeAll();
	    	contentPane.add(labelBACKground);
	    	contentPane.revalidate(); 
	    	contentPane.repaint();
			logger.debug("Background of mainGUI setted: "+RESOURCES_DIR + APPLICATION_MAIN_BCKG);

        } catch (IOException e2) {
			logger.error("Exception happened!", e2);
        }
	}
	
	/**
	 * this method select a file from the user, if nothing is selected returns null
	 */
	public static File fileLogWriteDialog(JFrame parent) {
		
		now = new Date();
		SimpleDateFormat df = new SimpleDateFormat("yyyy_MM_dd_@_HH-mm-ss");
		String defName = "ScreepT_Execution_Log_" + df.format(now) + ".log";
		
		JFileChooser fileChooser = new JFileChooser();		
		fileChooser.setDialogTitle("Export Log File");
		fileChooser.setSelectedFile(new File(defName));
		fileChooser.setCurrentDirectory(new File(currentConfiguration.getFileOpt().getLastDstFolderPath()));		
		File correctFile = null;
		
		int returnVal = fileChooser.showSaveDialog(parent);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            
        	File selectedFile = fileChooser.getSelectedFile();
        	selectedFile = FileStuff.changeExtension(selectedFile, ".log");	
        	selectedFile = FileStuff.renameJavaObjFile(selectedFile);
        	String givenExtension = FilenameUtils.getExtension(selectedFile.getName());
        	
        	if(selectedFile==null||!givenExtension.equalsIgnoreCase("log")) {
        		JOptionPane.showMessageDialog(parent, "Please specify a valid 'log' file!", "Wrong File format", JOptionPane.ERROR_MESSAGE);
        	}else {
        		correctFile = selectedFile;
        		currentConfiguration.getFileOpt().setLastDstFolderPath(correctFile.getAbsolutePath());

        	}
        }else {
        	JOptionPane.showMessageDialog(parent, "Skipped after user request.");
        }
        return correctFile;
	}
	
	public static Runnable tempDecryptedEraser() {
		return new Runnable() {
			
			@Override
			public void run() {
				File[] decryptedList = TEMP_DECRIPTED_ROOT.listFiles();
				if (decryptedList!= null && decryptedList.length!=0) {
					// while there are temp decripted delete them
					for (File temp : decryptedList) {
						FileUtils.deleteQuietly(temp);
					} 
				}	
				// now delete temp folder, and retry if something has not been deleted
				FileStuff.deleteDirContentInclRoot(TEMP_DECRIPTED_ROOT);
			}
		};
	}
	
	private void checkVersionAndDisableUnsupportedOpt() {
		currentJvmVersion = System.getProperty("java.version");
		String tempCompareJvmCurrent = SysUtils.keepOnlyDotsAndDigits(currentJvmVersion);
		int compareRes = SysUtils.compareVersions(tempCompareJvmCurrent, MAX_JVM_COMPLETE_COMPATIBILITY);
		if(compareRes == 1) {
			String tooltipMSG = "This option is unsupported with current JVM version (" + currentJvmVersion + "). Install version under "+MAX_JVM_COMPLETE_COMPATIBILITY;
			// Disable non compatible options
			mntmhwInfo.setEnabled(false);
			mntmnetInfo.setEnabled(false);
			// set tooltip text to menu item
			mntmhwInfo.setToolTipText(tooltipMSG);
			mntmnetInfo.setToolTipText(tooltipMSG);
		}
	}
	
	public static void closeApplication() {
		
		closeOpenBlockingFrames();

		now=new Date();
		
        // if autosave is on save before quit
        if(currentConfiguration.getFileOpt().isFlagAutosave()) {
			try {
				currentConfiguration.writeCurrentConfigIniFile();
				logger.debug(" => AUTOSAVE ON: file saved before exiting ["
						+ currentConfiguration.getIniFile().getFile().getAbsolutePath() + "]");
			} catch (Exception e) {
				logger.error("Exception happened!", e);
			}
		}
        
      	logger.debug(" ## CLOSING APPLICATION:"+"\n\n##################################################\n\n"+now.toString()+"\n\n"
						+ APPLICATION_NAME+" CLOSED!\n\n"
						+ "##################################################\n\n");
      	
      	// START ERASING TEMP DECRYPTED IF THERE ARE SOME
      	Thread tempEraser = new Thread(tempDecryptedEraser());
      	tempEraser.start();	
      	
		String msg = "Deleting temp files and folders... please wait!";
		
		new Thread(new Runnable() {
					
			@Override
			public void run() {
				JOptionPane.showMessageDialog(null, msg, msg, JOptionPane.INFORMATION_MESSAGE);
			}
		}).start();
		
		try {
			tempEraser.join();
		} catch (InterruptedException e) {
			logger.error("Some error occurred deleting temporary Decrypted files");
		}
      	
		mainAppRunning = false;
		
		System.exit(0);
	}
	
	public static void closeOpenBlockingFrames(){
		if(panelHwInfo != null) {
			panelHwInfo.dispose();
		}
		
		if(panelNetInfo != null) {
			panelNetInfo.dispose();
		}
	}
	
	// GETTERS //
	
	public JFrame getMainFrame() {
		return frmScryptV;
	}
	
}

package maingUI;

import javax.swing.JFrame;
import javax.swing.JLabel;

import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;

import javax.swing.JOptionPane;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;

import javax.swing.JFileChooser;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Level;
import org.ini4j.Ini;
import org.ini4j.Wini;

import amfileutils.FileStuff;
import amfileutils.filechangewatcher.ChangeDTO;
import dialogutils.GenericFileChooserDialog;
import dialogutils.JOptionHelper;
import dialogutils.UpdateProgressPanel;
import frameutils.frame.ClipboardFrame;
import frameutils.frame.ColorPickerFrame;
import frameutils.frame.HashCheckerFrame;
import frameutils.utils.ComponentResizer;
import frameutils.utils.GuiUtils;
import gUI.frames.AboutSectionFrame;
import gUI.frames.GeneralOptionsFrame;
import gUI.frames.GraphicPreferencesFrame;
import gUI.frames.ImageConverterFrame;
import gUI.frames.KeyDictionaryPanel;
import gUI.frames.KeyGenFrame;
import gUI.frames.TutorialFrame;
import gUI.panels.ChargeIniLog;
import gUI.panels.CrypterPanel;
import gUI.panels.TextEditor;
import gUI.utils.filedialog.DirectoryManagementGUI;
import gUI.utils.filedialog.INIfileChoosER;
import gui.PluginManagerGUI;
import guiVersion.HwInfoFrame;
import guiVersion.NetInfoFrame;
import guistyle.GUIStyelSettings;
import jmenu.CustomJmenuItem;
import logger.LoggerUtils;
import mainobj.INItializer;
import mainobj.InitOwner;
import media.Sounds;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import java.awt.SystemColor;
import java.awt.Window;
import java.awt.Dialog.ModalExclusionType;

import net.miginfocom.swing.MigLayout;
import om.TabTextEditorObj;
import resources.GeneralConfig;
import resources.IconsPathConfigurator;
import resources.ImagesPathConfigurator;
import resources.SoundsConfigurator;
import tabbededitor.CustomTabbedEditor;
import uTILS.dependencycheck.LibChecker;
import utils.SysUtils;
import utils.panels.ClipboardPanel;
import utils.panels.arch.PanelType;

import javax.swing.MenuSelectionManager;
import javax.swing.SwingUtilities;

public class MainGUI extends MainGuiParent{

	
//////////////////////////// GUI ELEMENTS //////////////////////////////
////////////////////////////////////////////////////////////////////////
	
	// background image
	private JLabel labelBACKground;
	
	// FRAMES THAT OPEN IN A NEW DIALOG
	private static GraphicPreferencesFrame panelGuiOptions;
	private static GeneralOptionsFrame panelGeneralOptions;
	private static KeyGenFrame panelKeyGen;
	private static KeyDictionaryPanel panelKeyDict;
	private static ImageConverterFrame panelImgConv;
	private static NetInfoFrame panelNetInfo;
	private static HwInfoFrame panelHwInfo;
	private static HashCheckerFrame hashCheckerFrame;
	private static ClipboardFrame clipboardFrame;
	private static ColorPickerFrame colorPickerFrame;
	private static AboutSectionFrame panelAbout;
	private static TutorialFrame tutorialPanel;
	private static PluginManagerGUI pluginManagerGUI;
	
////////////////////////// MAIN GUI ELEMENTS ///////////////////////////
////////////////////////////////////////////////////////////////////////
	
	// Filed for uppercaseFirstLetter "various' function"
	private String last_dir_used; 
	
//	private MovePanelMouseListener movePanelMouseListener;
	
	// global values representing the screen size
	public static int screenWidth;
	public static int screenHeight;

	// some used lookAndFeel
	public static String preferredStyle = GUIStyelSettings.NimbusLookAndFeel; // preferredStyle (initialized to fully avoid null values)
	public static boolean DARK_THEME; // true if dark theme is selected
	
	// flags for no-window operations
	private static boolean FLAG_mntmExit = false;
	private static boolean FLAG_loadPrjSub = false;
	private static boolean FLAG_savePrjSub = false;
	private static boolean FLAG_savePrjSubAs = false;
	private static boolean FLAG_loadDefaultPrjSub = false;
	private static boolean FLAG_mntmSaveCurrentAs = false;
	private static boolean FLAG_loadSrcrYptDefaultConf = false;
	private static boolean FLAG_mntmExportLog = false;
	private static boolean FLAG_mntmEraseLogs = false;
	private static boolean FLAG_mntmTouppercaseDirectory = false;
	private static boolean FLAG_mntmDeleteDirectoryContentByExtension = false;
	private static boolean FLAG_mntmDeleteDirectoryContentRecursive = false;
	private static boolean FLAG_mntmDeleteDirectoryContentDirsOnlyRecursive = false;
	private static boolean FLAG_mntmMakeDirsFromPath = false;
	private static boolean FLAG_mntmCopyDirStructure = false; 
	
	// main app object reference
	public static MainGUI mainAPP;
	public static Dimension preFullScreenSize = null;
	
	public static JOptionHelper dialogHelper;
	
	public AtomicBoolean isOnHomePage = new AtomicBoolean(true);
	public AtomicBoolean isUpdating = new AtomicBoolean(false);
	
	// Full screen+custom modes varying on loaded panel
	public AtomicBoolean isImmersiveModeOn = new AtomicBoolean();
	public AtomicBoolean isFullScreen = new AtomicBoolean(false);
	
	public static String newUpdatedJar = "";
	static File[] updatesDownloaded = new File[3];
	
	 /************ BEGIN MAIN APPLICATION GENERAL METHOD  ****************************
	 * 
	 * 			Calls contructor, initialize & Launch the application.
	 * 
	 * 				 (Called from MainGuiGeneralAppStarter())
	 * 
	 ************* BEGIN MAIN APPLICATION GENERAL METHOD  ****************************/
	
	public static boolean mainApplicationGeneral(INItializer currentConf) {
		
		if(mainAppRunning) {
			return true;
		}
		
		currentConfiguration = currentConf;
		dialogHelper = new JOptionHelper(null);
		
		MainGuiUtils.welcomeMethodIfFirstTime();
		
		// START LOADING PANEL THREAD
		new Thread(new Runnable() {
			public void run() {
				for (int i = 0; i < 10; i++) {
					String dots = new String(new char[i + 1]).replace("\0", ".");
					String msg = "Initializing " + GeneralConfig.APPLICATION_NAME + dots;
					MainGuiGeneralAppStarter.loaderGraphics.update(msg, i / 2);
					try {
						Thread.sleep(60);
					} catch (InterruptedException e) {
						logger.error("Error waiting before starting launcher", e);
					}
				}
			}
		}).start();
		
		try {
			now = new Date();
			mainAppRunning = true;
			
MainGuiGeneralAppStarter.loaderGraphics.update("Initializing graphics... ", 15);
			
			// START MAIN WINDOW CONSTRUCTOR
			logger.info("Initializing main GUI-start");
			MainGUI window = new MainGUI();
			frmScryptV.setVisible(true);
			frmScryptV.setEnabled(true);
			mainGuiUtils = new MainGuiUtils(window);
			logger.info("Main GUI initialized correctly.");
			
MainGuiGeneralAppStarter.loaderGraphics.update("Starting autosave modules... ", 65);
						
			// AUTOSAVE MODULE - only if specified in fileOptions
			autosaveModule = new Thread(new Runnable() {
				@Override
				public void run() {
					logger.info("## AUTOSAVE module started, interval: "+currentConfiguration.getFileOpt().getAutosaveInterval() + " milliseconds");
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
								logger.info("AUTOSAVE ON: file saved ["
										+ currentConfiguration.getIniFile().getFile().getAbsolutePath() + "]");
							} catch (Exception e) {
								logger.error("Exception happened!", e);
							}
						}
					}
					logger.info("## AUTOSAVE module Stopped (Closing ScrYpt)");
				}
			});
			
			// AUTOSAVE MANAGER (refreshes every 4 second)
			Thread autosaveManager = new Thread(new Runnable() {
				
				@Override
				public void run() {
					logger.info("## AUTOSAVE-MANAGER module started - FlagAutosave from config.ini: "+currentConfiguration.getFileOpt().isFlagAutosave());
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
								logger.debug("## AUTOSAVE-MANAGER: running -> [AUTOSAVE "+currentConfiguration.getFileOpt().isFlagAutosave()+"] -> autosave Module running["+autosaveModule.isAlive()+"] - Interval: "+currentConfiguration.getFileOpt().getAutosaveInterval()/1000+"s");
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
			
			// MAIN WINDOW RESIZE LISTENER
			frmScryptV.addComponentListener(new ComponentAdapter() {
				@Override
				public void componentResized(ComponentEvent componentEvent) {
					MainGUI.mainAPP.resizeMainFrameDefaults();
			    }
			});
			// MAIN WINDOW CLOSE LISTENER
			frmScryptV.addWindowListener(new java.awt.event.WindowAdapter() {
			    @Override
			    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
			    	askForExit();
			  	}
			});
			
			Sounds.playSound(SoundsConfigurator.SOUND_POOL_SHOT, null);

			frmScryptV.requestFocus();
			frmScryptV.toFront();
			
			SwingUtilities.invokeLater(new Runnable() {
				
				@Override
				public void run() {
					// REFRESH GUI GENERAL
					MainGuiGeneralAppStarter.loaderGraphics.update("Updating GUI style...", 75, 350);
					if (GUIStyelSettings.isNimbus(currentConfiguration.getGuiOpt())) {
						if (GUIStyelSettings.isDarkNimbus(currentConfiguration.getGuiOpt())) {
							
							GUIStyelSettings.setDarkNimbus(currentConfiguration.getGuiOpt().nimbusSize);
						} else {
							
							GUIStyelSettings.setLightNimbus(currentConfiguration.getGuiOpt().nimbusSize);
						}
					}else {
						GUIStyelSettings.setStyle(currentConfiguration.getGuiOpt().getPreferredStyle(), currentConfiguration.getGuiOpt().nimbusSize);
					}
					
				}
			});

			MainGuiGeneralAppStarter.loaderGraphics.update("Resetting last viewed panel: " + currentConfiguration.getGenOpt().getLastPanelOpened() + " ...", 80, 350);

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					
					// RUN ARGUMENTS IF THERE ARE
					if (MainGuiGeneralAppStarter.argumentsFiles != null && MainGuiGeneralAppStarter.argumentsFiles.length != 0) {
						mainGuiUtils.startTextEditor(true);
					} else {
						Class<?> lastOpened = currentConfiguration.getGenOpt().getLastPanelOpened();
						if (lastOpened.equals(TextEditor.class)) {
							mainGuiUtils.startTextEditor(true);
						} else if (lastOpened.equals(CrypterPanel.class)) {
							mainGuiUtils.startCrypterPanel(true);
						} else if (lastOpened.equals(ChargeIniLog.class)) {
							mainGuiUtils.startConfigManagerPanel(true);
						}
					}
					MainGuiGeneralAppStarter.loaderGraphics.update("Restoring Gui Elements From configuration...", 99, 200);
					updateGuiFromCurrConfig();
					
					// Close loader
					GuiUtils.launchThreadSafeSwing(new Runnable() {
						public void run() {
							
							MainGuiGeneralAppStarter.loaderGraphics.update("Welcome to ScrYpT", 100, 200);
							
							window.startTitleUpdater();
							
							if (currentConfiguration.getGenOpt().isAutomaticUpdateOn()) {
								try {
									Thread.sleep(GeneralConfig.UPDATES_AUTOCHECK_DELAY);
								} catch (Exception e) {
									logger.warn("Cannot wait " + GeneralConfig.UPDATES_AUTOCHECK_DELAY + " ms to check for updates", e);
								}
								MainGuiUtils.lookForUpdates(true).start();
							}
						}
					});
				}
			});
			
			//window.frmScryptV.getContentPane().setPreferredSize( new Dimension(screenWidth, screenHeight));
		} catch (Exception e) {
			logger.error("Exception happened!", e);
		}
		
		return mainAppRunning;
		
	} /************  END MAIN APPLICATION GENERAL STATIC METHOD  ****************************/

	
	/****************************************************************************************************
	 * 										=> CONSTRUCTOR
	 * 
	 * it creates the application, initializing components of main window using the function initialize().
	 * Called from MainGUI.mainApplicationGeneral()
	 *****************************************************************************************************/
	public MainGUI() {
		super();
		
		mainAPP = this;
		
		// initialize value for function uppercaseDirectories ()
		last_dir_used = currentConfiguration.getFileOpt().getLastSrcFolderPath();
		
		// INITIALIZE MAIN_GUI (See the initialize() function below this constructor)
		initialize();
		
		logger.info("## GUI components started");
		
		// SW window icon
		try {
			SW_ICON = ImageIO.read(new File(ImagesPathConfigurator.MAIN_APP_ICON));
			frmScryptV.setIconImage(SW_ICON);
			logger.info("icon image setted to main window");
		} catch (IOException e1) {
			logger.error("Exception happened!", e1);
		}

		logger.info("## TITLE UPDATER started.");
		
	}
	// END CONSTRUCTOR
	
	/**
	 * Initialize the contents of the frame.
	 */
	@SuppressWarnings("serial")
	private void initialize() {

 MainGuiGeneralAppStarter.loaderGraphics.update("Initializing screen properties...", 30);
 
 		// This will be used to print out various types of dialogs in this class
 		dialogHelper = new JOptionHelper(frmScryptV);
 
		// INITIALIZE SCREEN PROPOERTIES
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		screenWidth = gd.getDisplayMode().getWidth();
		screenHeight = gd.getDisplayMode().getHeight();
				
		menuSize = currentConfiguration.getGuiOpt().getMenuFontSize();
		subMenuSize = menuSize-2;
		
		// INITIALIZE MAIN FRAME
		frmScryptV = new JFrame();
		frmScryptV.setEnabled(false);
		frmScryptV.setModalExclusionType(ModalExclusionType.TOOLKIT_EXCLUDE);
		frmScryptV.getContentPane().setBackground(SystemColor.windowText);
		frmScryptV.setBounds(0, 0, 1500, 850 );
		frmScryptV.setMinimumSize(MainGuiGeneralAppStarter.minimumSize);
		frmScryptV.addMouseListener(new ComponentResizer());
		frmScryptV.setJMenuBar(menuBar);
		
		// FULL SCREEN SETTING
	    frmScryptV.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frmScryptV.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        setBackgroundImage();
        
 MainGuiGeneralAppStarter.loaderGraphics.update("Defining handlers...", 40);
		
		//////////////////////////// DEFINE HANDLERS  //////////////////////////////////
		
 		mntmPinApplicationOnTop = new CustomJmenuItem(new AbstractAction("") {
			public void actionPerformed(ActionEvent e) {
				if (!mainAPP.getMainFrame().isAlwaysOnTop()) {
					
					if(!mainAPP.getMainFrame().isAlwaysOnTopSupported()) {
						logger.warn("User tried to set Always on top, but it seems it's not supported in this device!");
						dialogHelper.warn("It seems that \"Always on top\" is not supported on this system :(", "\"Always on top\" not supported");
					}

					getMainFrame().setAlwaysOnTop(true);
					mntmPinApplicationOnTop.setIcon(new ImageIcon(IconsPathConfigurator.ICON_PIN_GREEN));
					mntmPinApplicationOnTop.setToolTipText("Disable \"always on top\" over other windows");
					
				}else{
					getMainFrame().setAlwaysOnTop(false);
					mntmPinApplicationOnTop.setIcon(new ImageIcon(IconsPathConfigurator.ICON_PIN_RED));
					mntmPinApplicationOnTop.setToolTipText("Enable \"always on top\" over other windows (if supported)");
				}
			}
 		});
 
		// NOT OPENING VIEW - EXIT AND CLOSE SCRYPT
		mntmExit = new CustomJmenuItem(new AbstractAction("Exit") {
			public void actionPerformed(ActionEvent e) {
				if (!FLAG_mntmExit) {
					FLAG_mntmExit = true;
					
					askForExit();
					
					FLAG_mntmExit = false;
				}
			}
		});

		// NOT OPENING VIEW - LOAD CUSTOM CONFIGURATION
		mntmloadPrjSub = new CustomJmenuItem(new AbstractAction("Load Custom Configuration") {
		    public void actionPerformed(ActionEvent ae) {
		   
		    		if (!FLAG_loadPrjSub) {
		    			Sounds.playSound(SoundsConfigurator.CLICK_2, null);
		    			FLAG_loadPrjSub = true;
						INIfileChoosER panel = new INIfileChoosER(currentConfiguration, GeneralConfig.DEFAULT_INI_FOLDER);
						panel.setBounds(20, 20, frmScryptV.getWidth(), frmScryptV.getHeight() / 2);
						FLAG_loadPrjSub = false;
						updateGuiFromCurrConfig();
					}
		    }
		});
		
		String msg1 = "Are you sure to reset ";
		String msg2 = "? All unsaved work will be lost.";
		
		// NOT OPENING VIEW - RESET ALL PANELS
		mntmResetAllPanels = new CustomJmenuItem(new AbstractAction("Reset all Panels") {
		    public void actionPerformed(ActionEvent ae) {
		    	if(dialogHelper.yesOrNo(msg1 + "all panels" + msg2, "Reset all panels?")) {
		    		Sounds.playSound(SoundsConfigurator.CLICK_2, null);
		    		mainGuiUtils.resetPanel(null, MainGUI.frmScryptV);
		    	}
			}
	    });
		
		// NOT OPENING VIEW - RESET TEXT EDITOR
		mntmResetTextEditor = new CustomJmenuItem(new AbstractAction("Reset Text Editor") {
			public void actionPerformed(ActionEvent ae) {
				if(dialogHelper.yesOrNo(msg1 + "Text Editor" + msg2, "Reset TextEditor?")) {
					Sounds.playSound(SoundsConfigurator.CLICK_2, null);
					mainGuiUtils.resetPanel(TextEditor.class, MainGUI.frmScryptV);
		    	}
			}
		});
		
		// NOT OPENING VIEW - RESET CRYPTER
		mntmResetCrypterPanel = new CustomJmenuItem(new AbstractAction("Reset ScreepT") {
			public void actionPerformed(ActionEvent ae) {
				if(dialogHelper.yesOrNo(msg1 + "Crypter" + msg2, "Reset Crypter?")) {
					Sounds.playSound(SoundsConfigurator.CLICK_2, null);
					mainGuiUtils.resetPanel(CrypterPanel.class, MainGUI.frmScryptV);
		    	}
			}
		});
		
		// NOT OPENING VIEW - RESET CONFIG MANAGER
		mntmResetConfigManager = new CustomJmenuItem(new AbstractAction("Reset Configuration manager") {
			public void actionPerformed(ActionEvent ae) {
				if(dialogHelper.yesOrNo(msg1 + "Configuration Manager" + msg2, "Reset Conf. Manager?")) {
					Sounds.playSound(SoundsConfigurator.CLICK_2, null);
					mainGuiUtils.resetPanel(ChargeIniLog.class, MainGUI.frmScryptV);
		    	}
			}
		});
		
		// NOT OPENING VIEW - SAVE CUSTOM CONFIGURATION
		mntmSaveCurrent = new CustomJmenuItem(new AbstractAction("Save Current Configuration") {
		    public void actionPerformed(ActionEvent ae) {
		    	if (!FLAG_savePrjSub) {
		    		Sounds.playSound(SoundsConfigurator.CLICK_2, null);
		    		FLAG_savePrjSub = true;
		    		ChargeIniLog.saveCurrentConf(currentConfiguration, frmScryptV);
		    		FLAG_savePrjSub = false;
				}  	
		    }
		});

		// NOT OPENING VIEW - SAVE CUSTOM CONFIGURATION
		mntmExportPrjSub = new CustomJmenuItem(new AbstractAction("Export Current Configuration") {
		    public void actionPerformed(ActionEvent ae) {
		    	if (!FLAG_savePrjSubAs) {
		    		Sounds.playSound(SoundsConfigurator.CLICK_2, null);
		    		FLAG_savePrjSubAs = true;
					INIfileChoosER panel = new INIfileChoosER(currentConfiguration, (JLabel)null);
					panel.setBounds(20, 20, frmScryptV.getWidth(), frmScryptV.getHeight() / 2);
					FLAG_savePrjSubAs = false;
				}  	
		    }
		});

		// NOT OPENING VIEW - RELOAD CONFIGURATION
		mntmReloadPrjSub = new CustomJmenuItem(new AbstractAction("Reload Configuration") {
		    public void actionPerformed(ActionEvent ae) {

		    	if (!FLAG_loadDefaultPrjSub) {
		    		Sounds.playSound(SoundsConfigurator.CLICK_2, null);
		    		FLAG_loadDefaultPrjSub = true;
					if (JOptionPane.showConfirmDialog(frmScryptV,
							"Really want to load default config(unsaved changes will be lost)? ",
							"Warning - Reload INI default config?",
							JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION) {
						try {
							currentConfiguration.setIniReadFile(new Ini(new File(GeneralConfig.DEFAULT_INI)));
							currentConfiguration.setIniFile(new Wini(new File(GeneralConfig.DEFAULT_INI)));
							JOptionPane.showMessageDialog(frmScryptV, "Configuration reloaded!");
						} catch (IOException e) {
							JOptionPane.showMessageDialog(frmScryptV,
									"Error opening: " + GeneralConfig.DEFAULT_INI + " -> maybe it does not exist?");
							logger.error("Exception happened!", e);
						}
						currentConfiguration.readConfigIniFile();
						updateGuiFromCurrConfig();
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
		mntmSaveCurrentAs = new CustomJmenuItem(new AbstractAction("Save Current as Default") {
			public void actionPerformed(ActionEvent e) {
				if (!FLAG_mntmSaveCurrentAs) {
					Sounds.playSound(SoundsConfigurator.CLICK_2, null);
					FLAG_mntmSaveCurrentAs = true;
					try {
						currentConfiguration.setIniFile(new Wini(new File(GeneralConfig.DEFAULT_INI)));
						currentConfiguration.setIniReadFile(new Ini(new File(GeneralConfig.DEFAULT_INI)));

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
		mntmloadSrcrYptDefaultConf = new CustomJmenuItem(new AbstractAction("Reset Original") {
		    public void actionPerformed(ActionEvent ae) {

		    	if (!FLAG_loadSrcrYptDefaultConf) {
		    		Sounds.playSound(SoundsConfigurator.CLICK_2, null);
		    		FLAG_loadSrcrYptDefaultConf = true;
					if (JOptionPane.showConfirmDialog(frmScryptV,
							"Really want to Reset standard ScrypT configuration? Unsaved changes will be lost! ",
							"Warning - Reset INI standard config?",
							JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION) {
						try {
							FileUtils.copyFile(new File(GeneralConfig.BACKUP_INI), new File(GeneralConfig.DEFAULT_INI));
							currentConfiguration.setIniReadFile(new Wini(new File(GeneralConfig.DEFAULT_INI)));
							currentConfiguration.setIniFile(new Wini(new File(GeneralConfig.DEFAULT_INI)));
						} catch (IOException e) {
							JOptionPane.showMessageDialog(frmScryptV,
									"Error resetting standard configuration: it does not exist or IO error occurred, retry later!");
							logger.error("Exception happened!", e);
						}
						MainGuiUtils.welcomeMethodIfFirstTime();
						JOptionPane.showMessageDialog(frmScryptV,
								"File '" + currentConfiguration.getIniFile().getFile().getName() + "' loaded!");
						currentConfiguration.readConfigIniFile();
						updateGuiFromCurrConfig();
					} else {
						JOptionPane.showMessageDialog(frmScryptV,
								"Configuration not loaded, current CONFIG mantained!");
					} 
					FLAG_loadSrcrYptDefaultConf = false;
				}
		    }
		});

		// NOT OPENING VIEW - LOG EXPORT
		mntmExportLog = new CustomJmenuItem(new AbstractAction("Export Event Log") {
			public void actionPerformed(ActionEvent e) {
				if (!FLAG_mntmExportLog) {
					logger.debug(" => User started logs exporting procedure.");
					Sounds.playSound(SoundsConfigurator.CLICK_2, null);
					FLAG_mntmExportLog = true;
					
					String msg =  "Logger is disabled, you won't have any information from curent session. Export anyway? (To enable use ScrYpT_launcher's customize options at start)";
					int choice = JOptionPane.YES_OPTION;
					if(MainGuiGeneralAppStarter.LOG_LEVEL_AT_START.equals(Level.OFF)) {
						choice = JOptionPane.showConfirmDialog(frmScryptV, msg, "Logger turned OFF", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
					}
					
					if (choice == JOptionPane.YES_OPTION) {

						// show input dialog to select the file(gives null in case of no file selected)
						File selected = fileLogWriteDialog(frmScryptV);
						if (selected != null) {
							logger.info("\n\n##################################################\n\n" + now.toString()
									+ "\n\n" + GeneralConfig.APPLICATION_NAME + " EXPORT!\n\n"
									+ "##################################################\n\n");

							// give true to overwrite because fileWriteDialog already set the value to be unique
							if (FileStuff.copyFile(new File(GeneralConfig.LOG_PATH), selected, true) && FileStuff.copyFile(new File(GeneralConfig.LOG_LAUNCHER_PATH), selected, false)) {
								JOptionPane.showMessageDialog(frmScryptV, "File correctly saved in " + selected,
										"Log File Exported :)", JOptionPane.INFORMATION_MESSAGE);
								logger.info("File :" + GeneralConfig.LOG_PATH + " Exported, exportFile: " + selected);
							} else {
								JOptionPane.showMessageDialog(frmScryptV, "File not saved [" + selected,
										"]  Some error occurred :(", JOptionPane.ERROR_MESSAGE);
								logger.error("File not saved, some error occurred :( ");
							}
						} 
					}
					FLAG_mntmExportLog = false;
					logger.debug(" => Logs exporting procedure ended.");
				}
			}
		});
		
		// NOT OPENING VIEW - LOG ERASE
		mntmEraseLogs = new CustomJmenuItem(new AbstractAction("Erase Logs") {
			public void actionPerformed(ActionEvent e) {
				logger.debug(" => User started logs erasing procedure.");

				if (!FLAG_mntmEraseLogs) {
					Sounds.playSound(SoundsConfigurator.SOUND_CLOCK, null);
					FLAG_mntmEraseLogs = true;
					File LOG_MAIN_FILE = new File(GeneralConfig.LOG_PATH);
					String coolFileSize = FileStuff.coolFileSize(LOG_MAIN_FILE.length());
					int choice = JOptionPane.NO_OPTION;
					String msg =  " Are you sure you to want to erase your logs? \n\n => If you want you can click 'no' option and backup them, go under 'help submenu' [ Total size: " + coolFileSize + "]";
					choice = JOptionPane.showConfirmDialog(frmScryptV, msg, "Erase Logs content?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
					if(choice == JOptionPane.YES_OPTION) {
						if (FileStuff.clearFileContent(GeneralConfig.LOG_PATH) && FileStuff.clearFileContent(GeneralConfig.LOG_LAUNCHER_PATH)) {
							logger.debug("Files deleted");
							JOptionPane.showMessageDialog(frmScryptV, "Logs deleted - as you asked ;)", "Logs deleted", JOptionPane.INFORMATION_MESSAGE);
						}else {
							logger.error("Files not deleted :: An error occurred :(");
							JOptionPane.showMessageDialog(frmScryptV, "Something went wrong :( Some files may still contain stuff.", "An error occurred :(", JOptionPane.ERROR_MESSAGE);
						}
					} else {
						logger.debug("Logs not deleted :: skipped by user.");
						JOptionPane.showMessageDialog(frmScryptV, "Logs not deleted - as you asked ;)", "Logs not deleted", JOptionPane.INFORMATION_MESSAGE);
					}
				}
				
				FLAG_mntmEraseLogs = false;
				logger.debug(" => Logs erasing procedure ended. User exited esit advice");

			}
		});

		// NOT OPENING VIEW - UPPERCASE DIRECTORY'S NAMES FIRST LETTER
		mntmTouppercaseDirectory = new CustomJmenuItem(new AbstractAction("Uppercase First letter - Files&Directories") {
			public void actionPerformed(ActionEvent e) {
				if (!FLAG_mntmTouppercaseDirectory) {
					Sounds.playSound(SoundsConfigurator.CLICK_2, null);
					FLAG_mntmTouppercaseDirectory = true;
					last_dir_used = DirectoryManagementGUI.renameFirstLetterDirNames(frmScryptV, last_dir_used);
					FLAG_mntmTouppercaseDirectory = false;
				}
			}
		});
		
		// NOT OPENING VIEW - DELETE RECURSIVELY DIRECTORY'S CONTENT BY EXTENSION
		mntmDeleteDirectoryContentByExtension = new CustomJmenuItem(new AbstractAction("Delete Directory's content by extension") {
			public void actionPerformed(ActionEvent e) {
				if (!FLAG_mntmDeleteDirectoryContentByExtension) {
					Sounds.playSound(SoundsConfigurator.CLICK_2, null);
					FLAG_mntmDeleteDirectoryContentByExtension = true;
					DirectoryManagementGUI.deleteContentOfDirByExt(frmScryptV, currentConfiguration);
					FLAG_mntmDeleteDirectoryContentByExtension = false;
				}
			}
		});
		
		// NOT OPENING VIEW - DELETE RECURSIVELY DIRECTORY'S CONTENT
		mntmDeleteDirectoryAllContentRecursive = new CustomJmenuItem(new AbstractAction("Delete Directory's content (All except selected)") {
			public void actionPerformed(ActionEvent e) {
				if (!FLAG_mntmDeleteDirectoryContentRecursive) {
					Sounds.playSound(SoundsConfigurator.CLICK_2, null);
					FLAG_mntmDeleteDirectoryContentRecursive = true;
					DirectoryManagementGUI.deleteContentOfDir(frmScryptV, currentConfiguration);
					FLAG_mntmDeleteDirectoryContentRecursive = false;
				}
			}
		});
		
		// NOT OPENING VIEW - DELETE DIRECTORY'S CONTENT
		mntmEmptyDirectoryRecursive = new CustomJmenuItem(new AbstractAction("Empty all content (doesn't delete folders)") {
			public void actionPerformed(ActionEvent e) {
				if(!FLAG_mntmDeleteDirectoryContentDirsOnlyRecursive) {
					Sounds.playSound(SoundsConfigurator.CLICK_2, null);
					FLAG_mntmDeleteDirectoryContentDirsOnlyRecursive = true;
					DirectoryManagementGUI.deleteDirContentFiles(frmScryptV, currentConfiguration);
					FLAG_mntmDeleteDirectoryContentDirsOnlyRecursive = false;
				}
			}
		});

		// NOT OPENING VIEW - MAKE DIRS FROM STRING PATH
		mntmMakeDirsFromPath = new CustomJmenuItem(new AbstractAction("Create directory from String path") {
			public void actionPerformed(ActionEvent e) {
				if (!FLAG_mntmMakeDirsFromPath) {
					Sounds.playSound(SoundsConfigurator.CLICK_2, null);
					FLAG_mntmMakeDirsFromPath = true;
					DirectoryManagementGUI.makeDirsFromPath(frmScryptV, currentConfiguration);
					FLAG_mntmMakeDirsFromPath = false;
				}
			}
		});
		
		// NOT OPENING VIEW - COPY DIRECTORY STRUCTURE
		mntmCopyDirStructure = new CustomJmenuItem(new AbstractAction("Copy directory structure (Copy folders, not files)") {
			public void actionPerformed(ActionEvent e) {
				if(!FLAG_mntmCopyDirStructure) {
					Sounds.playSound(SoundsConfigurator.CLICK_2, null);
					FLAG_mntmCopyDirStructure = true;
					DirectoryManagementGUI.copyDirStructure(frmScryptV, currentConfiguration);
					FLAG_mntmCopyDirStructure = false;
				}
			}
		});
		
	///////////////// HANDLERS THAT OPEN A NEW WINDOW  ////////////////////////
		
		// open in a new window - CUSTOMIZE GRAPHICS
		mntmCustomizeUIView = new CustomJmenuItem(new AbstractAction("Customize Graphics") {
			public void actionPerformed(ActionEvent e) {
				if (panelGuiOptions == null || !panelGuiOptions.isVisible()) {
					Sounds.playSound(SoundsConfigurator.CLICK_2, null);
					panelGuiOptions = new GraphicPreferencesFrame(currentConfiguration);
					panelGuiOptions.setLocationRelativeTo(mainAPP.getMainFrame());
					panelGuiOptions.setVisible(true);
					panelGuiOptions.setAlwaysOnTop(true);
					panelGuiOptions.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					panelGuiOptions.setTitle("Graphics preferences - "+GeneralConfig.APPLICATION_NAME);
				}else {
					try {
						Sounds.playSound(SoundsConfigurator.CLICK_1, null);
						panelGuiOptions.setState(java.awt.Frame.NORMAL);
						panelGuiOptions.setVisible(true);
						panelGuiOptions.setAlwaysOnTop(true);
						panelGuiOptions.requestFocus();
					} catch (Exception e2) {
						logger.warn("There was an error opening " + GraphicPreferencesFrame.class, e2);
					}
				}
				GuiUtils.changeFrameStateLaterUnchecked(frmScryptV, panelGuiOptions, JFrame.MAXIMIZED_BOTH);
			}
		});
		
		// fullScreen mode toggle
		mntmFullScreenMode = new CustomJmenuItem(new AbstractAction("Full Screen Mode") {
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						if (frmScryptV.isUndecorated()) {
//							MovePanelMouseListener.removeMoveListener(frmScryptV, movePanelMouseListener); // USE TO REMOVE LISTENER DRAG PANEL
							if(preFullScreenSize == null) {
								preFullScreenSize = GuiUtils.getScreenSize(frmScryptV);
							}
							frmScryptV.dispose();
							frmScryptV.setUndecorated(false);
							frmScryptV.setLocationRelativeTo(frmScryptV);
							frmScryptV.setMinimumSize(MainGuiGeneralAppStarter.minimumSize);
							frmScryptV.setSize(preFullScreenSize);
							frmScryptV.setVisible(true);
							isFullScreen.set(false);
							mntmFullScreenMode.setIcon(new ImageIcon(IconsPathConfigurator.ICON_EXPAND));
						} else {
							frmScryptV.dispose();
							preFullScreenSize = GuiUtils.makeFullScreen(frmScryptV);
							frmScryptV.setSize(preFullScreenSize);
							frmScryptV.setMinimumSize(GuiUtils.getScreenSize(frmScryptV));
							frmScryptV.setLocationRelativeTo(frmScryptV);
							frmScryptV.setVisible(true);
							isFullScreen.set(true);
							mntmFullScreenMode.setIcon(new ImageIcon(IconsPathConfigurator.ICON_LESS));
//							movePanelMouseListener = MovePanelMouseListener.applyMoveListenerR(frmScryptV); // USE TO ADD LISTENER DRAG PANEL
						}
						
						// Update txtArea to adapt new size
						if(initTxtWriter != null && initTxtWriter.textEditorUtils != null) {
							initTxtWriter.initGuiFromConfig(false, true);
						}
						
						// RESYNC JSCROLL BUG WORKAROUND
						if (MainGUI.initTxtWriter != null && MainGUI.initTxtWriter instanceof TextEditor) {
							JScrollPane target = ((TextEditor)MainGUI.initTxtWriter).mainScrollPane;
							GuiUtils.scrollJScrollPane(target, true);
							initTxtWriter.textEditorUtils.resizeFrameToDefault(mainAPP);
						}
					}
				});
			}
		});
		
		
		// IMMERSIVE MODE ON/OFF
		mntmImmersiveMode = new CustomJmenuItem(new AbstractAction("Switch Immersive Mode ON/OFF (Press ESC to exit)") {
			public void actionPerformed(ActionEvent e) {
				mainGuiUtils.immersiveModeToggle();
			}
		});
		
		
		// open in a new window - GENERAL OPTIONS
		mntmGenOpt = new CustomJmenuItem(new AbstractAction("General Options") {
			public void actionPerformed(ActionEvent e) {
				if (panelGeneralOptions == null || !panelGeneralOptions.isVisible()) {
					Sounds.playSound(SoundsConfigurator.CLICK_2, null);
					panelGeneralOptions = new GeneralOptionsFrame(currentConfiguration);
					panelGeneralOptions.setLocationRelativeTo(mainAPP.getMainFrame());
					panelGeneralOptions.setMinimumSize(new Dimension(800, 245));
					GuiUtils.centerComponent(panelGeneralOptions);
					panelGeneralOptions.setAlwaysOnTop(true);
					panelGeneralOptions.setVisible(true);
					panelGeneralOptions.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					panelGeneralOptions.setTitle("Generic Options - "+GeneralConfig.APPLICATION_NAME);
				}else {
					try {
						Sounds.playSound(SoundsConfigurator.CLICK_1, null);
						panelGeneralOptions.setState(java.awt.Frame.NORMAL);
						panelGeneralOptions.setAlwaysOnTop(true);
						panelGeneralOptions.setVisible(true);
						panelGeneralOptions.requestFocus();
					} catch (Exception e2) {
						logger.warn("There was an error opening " + GeneralOptionsFrame.class, e2);
					}
				}
				
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
//	 					GuiUtils.changeFrameStateLaterUnchecked(frmScryptV, panelGeneralOptions, JFrame.MAXIMIZED_BOTH);
						panelGeneralOptions.toFront();
					}
				});
			}
		});
		
		// open in a new window - KEYGEN
		mntmgenerateKeyImageSub = new CustomJmenuItem(new AbstractAction("Generate Key Image") {
		    public void actionPerformed(ActionEvent ae) {
		    	if (panelKeyGen == null) {
		    		Sounds.playSound(SoundsConfigurator.CLICK_2, null);
		    		panelKeyGen = new KeyGenFrame(currentConfiguration);
					panelKeyGen.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					panelKeyGen.setLocationRelativeTo(mainAPP.getMainFrame());
					panelKeyGen.setBounds(150, 150, 1100, 800);
					panelKeyGen.setVisible(true);
					panelKeyGen.setTitle("IMG Generator - "+GeneralConfig.APPLICATION_NAME);
				}else {
					try {
						Sounds.playSound(SoundsConfigurator.CLICK_1, null);
						panelKeyGen.setState(java.awt.Frame.NORMAL);
						panelKeyGen.setVisible(true);
						panelKeyGen.requestFocus();
					} catch (Exception e2) {
						logger.warn("There was an error opening " + KeyGenFrame.class, e2);
					}
				}
		    	GuiUtils.changeFrameStateLaterUnchecked(frmScryptV, panelKeyGen,JFrame.MAXIMIZED_BOTH);
		    }
		});
		
		// open in a new window - NET INFO
		mntmnetInfo = new CustomJmenuItem(new AbstractAction("Network Info") {
		    public void actionPerformed(ActionEvent ae) {
		    	if (panelNetInfo == null || !panelNetInfo.isVisible()) {
		    		Sounds.playSound(SoundsConfigurator.CLICK_2, null);
		    		SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							new Thread(new Runnable() {

								@Override
								public void run() {
									UpdateProgressPanel prog = new UpdateProgressPanel(currentConfiguration, false);
									prog.getPb().setIndeterminate(true);
									prog.getLabel().setText("Loading Network informations...");
									prog.getPb().setString("Loading...");
									panelNetInfo = new NetInfoFrame();
									panelNetInfo.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
									panelNetInfo.setLocationRelativeTo(mainAPP.getMainFrame());
									panelNetInfo.setAlwaysOnTop(true);
									panelNetInfo.setBounds(150, 150, 1000, 650);
									panelNetInfo.setResizable(false);
									panelNetInfo.setVisible(true);
									panelNetInfo.setTitle("Network info - "+GeneralConfig.APPLICATION_NAME);
									prog.getPb().setValue(100);
								}
							}).start();
						}
					});
		    		
				}else {
					try {
						Sounds.playSound(SoundsConfigurator.CLICK_1, null);
						panelNetInfo.setState(java.awt.Frame.NORMAL);
						panelNetInfo.setVisible(true);
						panelNetInfo.requestFocus();
					} catch (Exception e2) {
						logger.warn("There was an error opening " + NetInfoFrame.class, e2);
					}
				}
		    	GuiUtils.changeFrameStateLaterUnchecked(frmScryptV, panelNetInfo, JFrame.MAXIMIZED_BOTH);
		    }
		});
		
		// open in a new window - NET INFO
		mntmhwInfo = new CustomJmenuItem(new AbstractAction("System Info") {
		    public void actionPerformed(ActionEvent ae) {
		    	if (panelHwInfo == null|| !panelHwInfo.isVisible()) {
		    		Sounds.playSound(SoundsConfigurator.CLICK_2, null);
		    		GuiUtils.launchThreadSafeSwing(new Runnable() {

						@Override
						public void run() {
							UpdateProgressPanel prog = new UpdateProgressPanel(currentConfiguration, false);
							prog.getPb().setIndeterminate(true);
							prog.getLabel().setText("Loading System informations...");
							prog.getPb().setString("Loading...");
							panelHwInfo = new HwInfoFrame();
							panelHwInfo.setLocationRelativeTo(mainAPP.getMainFrame());
							panelHwInfo.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
							panelHwInfo.setBounds(250, 200, 1000, 750);
							panelHwInfo.setAlwaysOnTop(true);
							panelHwInfo.setResizable(false);
							panelHwInfo.setVisible(true);
							panelHwInfo.setTitle("Hardware info - "+GeneralConfig.APPLICATION_NAME);
							prog.getPb().setValue(100);
						}
					});
		    		
				}else {
					try {
						Sounds.playSound(SoundsConfigurator.CLICK_1, null);
						panelHwInfo.setState(java.awt.Frame.NORMAL);
						panelHwInfo.setVisible(true);
						panelHwInfo.requestFocus();
						panelHwInfo.setLocationRelativeTo(frmScryptV);
					} catch (Exception e2) {
						logger.warn("There was an error opening " + HwInfoFrame.class, e2);
					}
				}
		    	GuiUtils.changeFrameStateLaterUnchecked(frmScryptV, panelHwInfo, JFrame.MAXIMIZED_BOTH);
		    }
		});
		
		// open in a new window - File Hash checker
		mntmHashChecker = new CustomJmenuItem(new AbstractAction("File Hash checker") {
			public void actionPerformed(ActionEvent e) {
				try {
					if (hashCheckerFrame == null || !hashCheckerFrame.isActive()) {
						File targetFile = (initTxtWriter != null) ? initTxtWriter.getCurrentTabEditor().loadedTabObj.loadedFile : null;
						hashCheckerFrame = new HashCheckerFrame(targetFile, frmScryptV);
						hashCheckerFrame.setVisible(true);
						hashCheckerFrame.setAlwaysOnTop(true);
					} else {
						hashCheckerFrame.toFront();
						hashCheckerFrame.setVisible(true);
						hashCheckerFrame.setAlwaysOnTop(true);
					}
				} catch (Exception e1) {
					logger.error("An error occurred while opening hashing functions manager frame", e1);
				}
			}
		});
		
		// open in a new window - File Hash checker
		mntmClipboardStandalone = new CustomJmenuItem(new AbstractAction("Clipboard tool") {
			public void actionPerformed(ActionEvent e) {
				try {
					if (clipboardFrame == null || !clipboardFrame.isActive()) {
						clipboardFrame = new ClipboardFrame(frmScryptV);
						clipboardFrame.setVisible(true);
						clipboardFrame.setAlwaysOnTop(true);
					} else {
						clipboardFrame.toFront();
						clipboardFrame.setVisible(true);
						clipboardFrame.setAlwaysOnTop(true);
					}
					
					clipboardFrame.setLocationRelativeTo(frmScryptV);
				} catch (Exception e1) {
					logger.error("An error occurred while opening cilpboard standalone frame", e1);
				}
			}
		});
		
		// open in a new window - File Hash checker
		mntmColorPicker = new CustomJmenuItem(new AbstractAction("Color picker") {
			public void actionPerformed(ActionEvent e) {
				try {
					if (colorPickerFrame == null || !colorPickerFrame.isActive()) {
						colorPickerFrame = new ColorPickerFrame(frmScryptV);
						colorPickerFrame.setVisible(true);
						colorPickerFrame.setAlwaysOnTop(true);
					} else {
						colorPickerFrame.toFront();
						colorPickerFrame.setVisible(true);
						colorPickerFrame.setAlwaysOnTop(true);
					}
					
					colorPickerFrame.setLocationRelativeTo(frmScryptV);
				} catch (Exception e1) {
					logger.error("An error occurred while opening color picker frame", e1);
				}
			}
		});
		
		// open in a new window - DICTIONARY (KEY IMG)
		mntmViewKeyDictionary = new CustomJmenuItem(new AbstractAction("View Key Dictionary") {
		    public void actionPerformed(ActionEvent e) {
		    	if (panelKeyDict == null) {
		    		Sounds.playSound(SoundsConfigurator.CLICK_2, null);
		    		panelKeyDict = new KeyDictionaryPanel(currentConfiguration);
		    		panelKeyDict.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		    		panelKeyDict.setBounds(300, 100, 800, 560);
		    		panelKeyDict.setVisible(true);
		    		panelKeyDict.setTitle("Key Dictionary - "+GeneralConfig.APPLICATION_NAME);
		    		panelKeyDict.setLocationRelativeTo(mainAPP.getMainFrame());
				}else {
					try {
						Sounds.playSound(SoundsConfigurator.CLICK_1, null);
						panelKeyDict.setState(java.awt.Frame.NORMAL);
						panelKeyDict.setVisible(true);
						panelKeyDict.requestFocus();
					} catch (Exception e2) {
						logger.warn("There was an error opening " + KeyDictionaryPanel.class, e2);
					}
				}
		    	GuiUtils.changeFrameStateLaterUnchecked(frmScryptV, panelKeyDict,JFrame.MAXIMIZED_BOTH);
			}
		});
		
		// open in a new window - IMAGE CONVERTER
		mntmImageConverter = new CustomJmenuItem(new AbstractAction("Image Converter") {
			public void actionPerformed(ActionEvent e) {
		    	if (panelImgConv == null) {
		    		Sounds.playSound(SoundsConfigurator.CLICK_2, null);
		    		panelImgConv = new ImageConverterFrame(currentConfiguration);
		    		panelImgConv.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		    		panelImgConv.setBounds(700, 200, 650, 500);
		    		panelImgConv.setResizable(false);
		    		panelImgConv.setVisible(true);
		    		panelImgConv.setTitle("IMG Generator - "+GeneralConfig.APPLICATION_NAME);
		    		panelImgConv.setLocationRelativeTo(mainAPP.getMainFrame());
				}else {
					try {
						Sounds.playSound(SoundsConfigurator.CLICK_1, null);
						panelImgConv.setState(java.awt.Frame.NORMAL);
						panelImgConv.setVisible(true);
						panelImgConv.requestFocus();
					} catch (Exception e2) {
						logger.warn("There was an error opening " + ImageConverterFrame.class, e2);
					}
				}
		    	GuiUtils.changeFrameStateLaterUnchecked(frmScryptV, panelImgConv,JFrame.MAXIMIZED_BOTH);
			}
		});
		
		// open in a new window - ABOUT SCRYPT
		mntmAboutScreept = new CustomJmenuItem(new AbstractAction("About - ScrYpt") {
			public void actionPerformed(ActionEvent e) {
				if (panelAbout == null) {
					Sounds.playSound(SoundsConfigurator.CLICK_2, null);
		    		panelAbout = new AboutSectionFrame(mainAPP);
		    		panelAbout.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		    		panelAbout.setBounds(700, 200, 400, 345);
		    		panelAbout.setResizable(false);
		    		panelAbout.setVisible(true);
		    		panelAbout.setTitle("About - "+GeneralConfig.APPLICATION_NAME);
		    		panelAbout.setLocationRelativeTo(mainAPP.getMainFrame());
				}else {
					try {
						Sounds.playSound(SoundsConfigurator.CLICK_1, null);
						panelAbout.setState(java.awt.Frame.NORMAL);
						panelAbout.setVisible(true);
						panelAbout.requestFocus();
					} catch (Exception e2) {
						logger.warn("There was an error opening " + AboutSectionFrame.class, e2);
					}
				}
				GuiUtils.changeFrameStateLaterUnchecked(frmScryptV, panelAbout, JFrame.MAXIMIZED_BOTH);
			}
		});
		
		// ASYNC HTML TUTORIAL PANE
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					// open in a new window - MANUAL - preloading after manGui is loaded to improve performance
					tutorialPanel = new TutorialFrame();
				} catch (Throwable e) {
					logger.error("Error loading HTML panel", e);
				}
			}
		});
		
		mntmUserDocumentation = new CustomJmenuItem(new AbstractAction("User Guide") {
			public void actionPerformed(ActionEvent e) {
				try {
					if (tutorialPanel == null) {
						// open in a new window - MANUAL - preloading after manGui is loaded to improve performance
						tutorialPanel = new TutorialFrame();
						
						Sounds.playSound(SoundsConfigurator.CLICK_2, null);
						tutorialPanel.setResizable(true);
			    		tutorialPanel.setVisible(true);
			    		tutorialPanel.setTitle("Tutorial - "+GeneralConfig.APPLICATION_NAME);
			    		tutorialPanel.setLocationRelativeTo(mainAPP.getMainFrame());
					}else {
						Sounds.playSound(SoundsConfigurator.CLICK_1, null);
						tutorialPanel.setVisible(true);
						tutorialPanel.toFront();
						tutorialPanel.setState(java.awt.Frame.NORMAL);
						tutorialPanel.requestFocus();
					}
				} catch (Throwable ex) {
					logger.error("Error loading HTML panel"  + TutorialFrame.class, ex);
				}
			}
		});
		
		// View Website
		mntmViewWebsite = new CustomJmenuItem(new AbstractAction("Visit Website") {
			public void actionPerformed(ActionEvent e) {
				
				SysUtils.openUrlInBrowser("https://"+GeneralConfig.WEBSITE);
				Sounds.playSound(SoundsConfigurator.SOUND_PLING, null);
			}
		});

		// Check and download/install updates
		mntmLookForUpdates = new CustomJmenuItem(new AbstractAction("Check for software updates...") {
			public void actionPerformed(ActionEvent e) {
				Thread updatesTHread = MainGuiUtils.lookForUpdates(false);
				updatesTHread.start();
			}
		});

		// Check and download/install updates
		mntmLookForPlugins = new CustomJmenuItem(new AbstractAction("Install plugins...") {
			public void actionPerformed(ActionEvent e) {
				new Thread(new Runnable() {
					public void run() {
						
						try {
							MainGuiGeneralAppStarter.pluginManager.discoverLatestPlugins();
							
							if (pluginManagerGUI != null || !PluginManagerGUI.active) {
								Runnable libCheckNRefreshGUI = new Runnable() {
									@Override
									public void run() {
										try {
											// Check hard coded plugins presence
											MainGuiGeneralAppStarter.tesseractAvailable = LibChecker.checkTesseract();
											MainGuiGeneralAppStarter.voiceSpeakerAvailable = LibChecker.checkVoiceSpeaker();
											if(initTxtWriter != null) {
												initTxtWriter.subHeaderPanel.enableDependenciesFunctions();
											}
										} catch (Throwable e) {
											logger.warn("Cannot check avalaibility of plugins :(");
										}
									}
								};

								pluginManagerGUI = new PluginManagerGUI(null, MainGuiGeneralAppStarter.pluginManager, libCheckNRefreshGUI);
								GuiUtils.setFrameIcon(IconsPathConfigurator.ICON_PLUGINS, pluginManagerGUI);
							} else {
								pluginManagerGUI.toFront();
							}
							pluginManagerGUI.setVisible(true);
						} catch (Throwable e1) {
							String errorMsg = "Cannot load Plugin Manager, an error occurred :(";
							dialogHelper.error(errorMsg, "An error occurred");
							logger.error(errorMsg, e1);
						}
					}
				}).start();
			}
		});
		
	////////////////  NOW HANDLERS THAT OPEN IN THE MAIN FRAME  ////////////////////////
		
		// open in main frame - FILE CRYPTER
		mntmCrypter = new CustomJmenuItem(new AbstractAction("ScreepT - Crypt and Decrypt files") {
		    public void actionPerformed(ActionEvent ae) {
		    	mainGuiUtils.startCrypterPanel(false);
		    }
		});
		
		// open in main frame - CONFIGURATION MANAGER
		mntmInspectCurrentConfiguration = new CustomJmenuItem(new AbstractAction("Configuration Manager") {
		    public void actionPerformed(ActionEvent ae) {
	    		mainGuiUtils.startConfigManagerPanel(false);
		    }
		});
		
		// open in main frame - TEXT EDITOR
		mntmTextEditor = new CustomJmenuItem(new AbstractAction("Text Editor") {
			public void actionPerformed(ActionEvent e) {
				mainGuiUtils.startTextEditor(false);
			}
		});
		
		// open in main frame - RETURN TO WELCOME PAGE
		mntmReturnToWelcome = new CustomJmenuItem(new AbstractAction("Return to Welcome Page") {
			public void actionPerformed(ActionEvent e) {
				isOnHomePage.set(true);
				Sounds.playSound(SoundsConfigurator.SOUND_PLING, null);
				setBackgroundImage();
			}
		});

		// BACKGROUND RESIZER //
		frmScryptV.addComponentListener(new ComponentListener() {
			public void componentShown(ComponentEvent e) {
			}
			public void componentResized(ComponentEvent e) {
				resizeMainFrameDefaults();
			}
			public void componentMoved(ComponentEvent e) {
			}
			public void componentHidden(ComponentEvent e) {
			}
		});
		
	////////// END OF MENU MOUSE CLICK - HANDLERS DECLARATION  ////////////////
	
 MainGuiGeneralAppStarter.loaderGraphics.update("Adding ActionHandlers...", 50);
 		addHandlers();
 		
 		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(getKeyEventHandler());
 
		// once each component is declared add it to the menu bar
 MainGuiGeneralAppStarter.loaderGraphics.update("Structuring menu...", 60);
		structureMenuBar();
		
		// check non compatible options with current JVM
		checkVersionAndDisableUnsupportedOpt();
		
		frmScryptV.requestFocus();
	}
	
	
//////////	NOW SOME USEFUL METHOD  ////////////////
	
	public void resizeMainFrameDefaults() {

		// resize background - Only on home page to improve performance
		if (isOnHomePage.get()) {
			labelBACKground.setSize(frmScryptV.getContentPane().getSize());
		}

		// resize textEditor if present
		if (initTxtWriter != null && initTxtWriter.getCurrentTabEditor().mainTabList != null) {
			initTxtWriter.textEditorUtils.resizeFrameToDefault(mainAPP);
		}

		// resize textEditor if present
		if (panelCrypter != null) {
			panelCrypter.resizeFrameToDefault(mainAPP);
		}
	}
	
	public void addHandlers() {
		
		// DEACTIVATE OPEN MENU ON CLICK OUTSIDE IT WHEN OPEN
		frmScryptV.addMouseListener(new MouseListener() {
			
			public void mouseReleased(MouseEvent e) {
				MenuSelectionManager.defaultManager().clearSelectedPath();
			}
			public void mousePressed(MouseEvent e) {
			}
			public void mouseExited(MouseEvent e) {
			}
			public void mouseEntered(MouseEvent e) {
			}
			public void mouseClicked(MouseEvent e) {
			}
		});
	}
	
	public void readBackgroundImage() throws IOException {
		BufferedImage image = ImageIO.read(new File(ImagesPathConfigurator.IMAGE_LOGO_AM_MAIN));
        APPLICATION_MAIN_BCKG_IMAGE = image.getScaledInstance(screenWidth-100, screenHeight-100,Image.SCALE_SMOOTH);
        
        BufferedImage screeptImageDnD = ImageIO.read(new File(ImagesPathConfigurator.IMAGE_BCKG_DRAGNDROP));
        SCREEPT_BACKGROUND_DRAGNDROP_IMAGE = screeptImageDnD.getScaledInstance(screeptImageDnD.getWidth(), screeptImageDnD.getHeight(), Image.SCALE_SMOOTH);
	}
	
	public void setBackgroundImage() {
		// setto lo sfondo iniziale dell'app
		try {
			JPanel MAIN_PANEL = new JPanel();
			MAIN_PANEL.setBackground(SystemColor.infoText);
            frmScryptV.getContentPane().add(MAIN_PANEL);
            MAIN_PANEL.setLayout(new MigLayout(GeneralConfig.DEBUG_GRAPHICS+"", "[90%]", "[1.00][100.52%,baseline]"));
            
            if(APPLICATION_MAIN_BCKG_IMAGE == null) {
            	readBackgroundImage();
            }
            
            labelBACKground = new JLabel(new ImageIcon(APPLICATION_MAIN_BCKG_IMAGE));
		   	JPanel contentPane = (JPanel) frmScryptV.getContentPane();
		   	contentPane.removeAll();
	    	contentPane.add(labelBACKground);
	    	contentPane.revalidate(); 
	    	contentPane.repaint();
			logger.info("Background of mainGUI setted: "+GeneralConfig.RESOURCES_DIR + ImagesPathConfigurator.IMAGE_LOGO_AM_MAIN);

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
	
	private void checkVersionAndDisableUnsupportedOpt() {
		currentJvmVersion = System.getProperty("java.version");
		String tempCompareJvmCurrent = SysUtils.keepOnlyDotsAndDigits(currentJvmVersion);
		int compareRes = SysUtils.compareVersions(tempCompareJvmCurrent, GeneralConfig.MAX_JVM_COMPLETE_COMPATIBILITY);
		if(compareRes == 1) {
			String tooltipMSG = "This option is unsupported with current JVM version (" + currentJvmVersion 
					+ "). Run with version under Max JVM version supported to use this functionality (Max allowed: "
					+GeneralConfig.MAX_JVM_COMPLETE_COMPATIBILITY + ")";
			
			// Disable non compatible options
			mntmhwInfo.setEnabled(false);
			mntmnetInfo.setEnabled(false);
//			mntmCrypter.setEnabled(false);
//			mntmCrypter.setToolTipText(tooltipMSG);
			// set tooltip text to menu item
			mntmhwInfo.setToolTipText(tooltipMSG);
			mntmnetInfo.setToolTipText(tooltipMSG);
		}
	}
	
	public static void askForExit() {
		Window activeWindow = javax.swing.FocusManager.getCurrentManager().getActiveWindow();
    	
		
    	if (new JOptionHelper(frmScryptV).yesOrNo("Really want to exit?", "Exit Program")) {

    		frmScryptV.setAlwaysOnTop(true);
    		closeApplication(0);
    		
    	}else {
    		
    		frmScryptV.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    		activeWindow.toFront();
    		activeWindow.requestFocus();
    	}
    	
	}
	
	public static void closeApplication(int exitCode) {
		
		logger.info("\n\n => Starting exit procedure...\n\n");
		
		// Remember splitPanel dividers position - TEXT Editor
		currentConfiguration.writeSingleValue(INItializer.TEXT_EDITOR_OPT, "dividerRightPanePosX", currentConfiguration.getTextEditorOpt().getDividerRightPanePosX());
		currentConfiguration.writeSingleValue(INItializer.TEXT_EDITOR_OPT, "dividerFileTreePosX", currentConfiguration.getTextEditorOpt().getDividerFileTreePosX());
		
		// CHECK UNSAVED TEXT EDITOR FILES
		if(!askUnsavedTextFiles()) {
			frmScryptV.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			return;
		}
		
		if(doIfTempDecryptedChanged(frmScryptV)) {
			frmScryptV.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			return;
		}
		
		// if controls are passed then start real application closing routine
		frmScryptV.setAlwaysOnTop(true);
		
		closeOpenBlockingFrames();
		
		now=new Date();
		
        // if autosave is on save before quit
        if(currentConfiguration.getFileOpt().isFlagAutosave()) {
			try {
				currentConfiguration.writeCurrentConfigIniFile();
				logger.info(" => AUTOSAVE ON: file saved before exiting ["
						+ currentConfiguration.getIniFile().getFile().getAbsolutePath() + "]");
			} catch (Exception e) {
				logger.error("Exception happened!", e);
			}
		}else {
			logger.info("\n => Autosave OFF -> configuration saving skipped");
		}
        
      	// SAVE CURRENTLY OPENED FILES reference only (in text-editor)
      	backupOpenedFilesInConfig();
      	
      	// START ERASING TEMP DECRYPTED IF THERE ARE SOME
      	Thread tempEraser = new Thread(mainGuiUtils.tempFilesEraser());
      	tempEraser.start();	
      	
      	// START ERASING CREEPT TEMP DECRYPTED IF THERE ARE SOME
      	Thread tempCreeptEraser = new Thread(mainGuiUtils.tempDecryptedEraser());
      	tempCreeptEraser.start();	
      	
		String msg = "Deleting temp files and folders...";
		
		try {
			logger.info(msg+ "please wait!");
			tempEraser.join();
			tempCreeptEraser.join();
			logger.info(msg+ "COMPLETED :)");
			
		} catch (InterruptedException e) {
			logger.error("Some error occurred deleting temporary Decrypted files");
		}
      	
		mainAppRunning = false;
		
		logger.info(LoggerUtils.getExitString(GeneralConfig.APPLICATION_NAME, true));
	
		Sounds.playSound(SoundsConfigurator.SOUND_PLING, null);
		
		MainGuiGeneralAppStarter.deleteSignalFileForExternalJars();
		
		// Check and launch eventual pending updates
		try {
			if (MainGuiGeneralAppStarter.updatesManager.updatesReadyToInstall.get()) {
				logger.error("Updates detected, starting updater jar application...");
				if(updatesDownloaded != null) {
					String command = "java -jar " + updatesDownloaded[0].getName() 
						+ " " + updatesDownloaded[0].getName() 
						+ " " + updatesDownloaded[1].getParentFile().getName() + "\\" + updatesDownloaded[1].getName()
						+ " " + updatesDownloaded[2].getName();
					Runtime runtime = Runtime.getRuntime();
					logger.info("Launching java command to update with downloaded files: \n\n" + command + "\n\n");
					runtime.exec(command);
					logger.info("Updater jar application launched!");
				}else {
					logger.error("updatesDownloaded is null!");
				}
			}
		} catch (IOException e) {
			logger.error("Cannot start update procedure, an error occurred!", e);
		}
		
		logger.info("Exit procedure completed succesfully, now software will shut down");
		
		System.exit(exitCode);
	}
	
	public static void closeOpenBlockingFrames(){
		logger.info("\n => Closing blocking frames");
		
		if(panelHwInfo != null) {
			panelHwInfo.dispose();
		}
		
		if(panelNetInfo != null) {
			panelNetInfo.dispose();
		}
		
		if(initTxtWriter != null) {
			if(initTxtWriter.subHeaderPanel.propertiesFrame != null) {
				initTxtWriter.subHeaderPanel.propertiesFrame.dispose();
			}
		}
		
		logger.info("\n => Blocking frames closed");
	}
	
	public static boolean doIfTempDecryptedChanged(JFrame parent) {
		logger.debug("Starting check on fileMonitor...");
		boolean result = false;
		try {
			if (CrypterPanel.fileMonitor != null && CrypterPanel.fileMonitor.rootMonitorDir.exists()) {
				ChangeDTO changesTempDecrypted = CrypterPanel.fileMonitor.checkForEverithing();
				if (changesTempDecrypted.changedFiles.size() != 0) {
					int choice = JOptionPane.showConfirmDialog(parent, "There are Files changed after decrypt.. do you want to return to ScreepT view? "+changesTempDecrypted.changedFiles);
					if(choice == JOptionPane.YES_OPTION) {
						result = false;
						mntmCrypter.doClick();
						// add unsaved as source for crypterPanel
						CrypterPanel.panelVDragNDropPane.removeAll();
						for(File unsaved : CrypterPanel.fileMonitor.rootMonitorDir.listFiles()) {
							CrypterPanel.panelVDragNDropPane.addFileToTree(unsaved);
						}
					}else {
						result = false;
					}
				}
			}
			
		} catch (Exception e) {
			logger.error("Error getting changes on tempFolder decrypted", e);
		}
		return result;
	}
	
	/**
	 * return false if not exit, true if has to exit;
	 * @return
	 */
	public static boolean askUnsavedTextFiles() {
		
		if (initTxtWriter == null || initTxtWriter.getCurrentTabEditor() == null) {
			return true;
		}
		
		CustomTabbedEditor currentTabEditor = initTxtWriter.getCurrentTabEditor();

		boolean askCondition;
		boolean atLeastOneTemp;
		
		askCondition = false;
		atLeastOneTemp = false;
		for (TabTextEditorObj tabElement : currentTabEditor.loadedList.getList()) {
			if (tabElement.unsaved) {
				askCondition = true;
			}
			if (tabElement.loadedFile == null) {
				atLeastOneTemp = true;
			}
		} 
		
		Boolean choiceY = GenericFileChooserDialog.askYorN("Files unsaved in textEditor", "There are some unsaved files in text Editor, do you want to save them?", frmScryptV, askCondition);
		if(choiceY == null) {
			// cancel -> don't exit
			return false;
		}else if(choiceY) {
			try {
				currentTabEditor.saveAllUnsavedExisting();
			} catch (IOException e) {
				logger.error(e);
				int choiceError = JOptionPane.showConfirmDialog(null, "Oooops... an error occurred. Continue anyway (you'll lose all unsaved changes)?");
				if(!(choiceError == JOptionPane.OK_OPTION)) {
					dialogHelper.error("Exit procedure arrested", "Close procedure stopped");
					mntmTextEditor.doClick();
					return false; // no -> don't exit after error
				}
			}
			if(atLeastOneTemp) {
				mntmTextEditor.doClick();
				for(TabTextEditorObj tabElement : currentTabEditor.loadedList.getList()) {
					if(tabElement.unsaved && tabElement.loadedFile == null) {
						currentTabEditor.mainTabList.setSelectedComponent(tabElement.currentScrollPane);
						return false; // don't exit
					}
				}
			}
			return true;
		}else {
			return true;
		}
	}
	
	private static void backupOpenedFilesInConfig() {
		if (initTxtWriter != null) {
			CustomTabbedEditor currentTabEditor = initTxtWriter.getCurrentTabEditor();
			
			// save list of non null and existing files in tabs
			currentConfiguration.getTextEditorOpt().getOpenedFileList().clear();
			for (TabTextEditorObj currentObj : currentTabEditor.loadedList.getList()) {
				if (currentObj.loadedFile != null && currentObj.loadedFile.exists()) {
					currentConfiguration.getTextEditorOpt().getOpenedFileList()
							.add(currentObj.loadedFile.getAbsolutePath());
				}
			}
			currentConfiguration.writeSingleValue(INItializer.TEXT_EDITOR_OPT, "loadedFilesAtStart",
					currentConfiguration.listToString(currentConfiguration.getTextEditorOpt().getOpenedFileList()));

			// save current opened if it exists
			if(currentTabEditor.loadedTabObj != null 
					&& currentTabEditor.loadedTabObj.loadedFile!= null 
					&& currentConfiguration.getTextEditorOpt().openedFileList.contains(currentTabEditor.loadedTabObj.loadedFile.getAbsolutePath()))
			{
				currentConfiguration.writeSingleValue(INItializer.TEXT_EDITOR_OPT, "selectedFileAtStart", currentTabEditor.loadedTabObj.loadedFile.getAbsolutePath());
			}
			
			// update clipboard if exists
			if(initTxtWriter != null && initTxtWriter.textEditorUtils.getTypeOfRightPanel().equals(PanelType.CLIPBOARD_PANEL)) {
				// save only current cause others are already saved once user selected items
				((ClipboardPanel)initTxtWriter.rightSplitContainer).saveCurrClipboardSilent();
			}
		}
		
		logger.info("\n => Opened files in textEditor added to configuration");
	}
	
	/**
	 *  KEYBOARD SHORTCUTS - use custom listener
	 */
	private KeyEventDispatcher getKeyEventHandler() {
		return new MainGuiShortcutsListener(mainAPP); 
	}
	
	/**
	 * General update from .ini configuration
	 */
	public static void updateGuiFromCurrConfig() {
		// TEXT EDITOR REFRESH
		if(initTxtWriter != null) {
			initTxtWriter.initGuiFromConfig(true, true);
		}
		
		// CRYPTER REFRESH
		if(panelCrypter != null) {
			panelCrypter.initGuiFromConfig();
		}
		
		// PANEL GRAPHICS PREFERENCES
		if(panelGuiOptions!= null && panelGuiOptions.isVisible()) {
			panelGuiOptions.initGuiFromConfig();
		}
		
		// PANEL GENERAL PREFERENCES
		if(panelGeneralOptions != null && panelGeneralOptions.isVisible()) {
			panelGeneralOptions.initGuiFromConfig();
		}
		
		if(GUIStyelSettings.isNimbus(currentConfiguration.getGuiOpt())) {
			if (GUIStyelSettings.isDarkNimbus(currentConfiguration.getGuiOpt())) {
				
				GUIStyelSettings.setDarkNimbus(currentConfiguration.getGuiOpt().nimbusSize);
			} else {
				GUIStyelSettings.setLightNimbus(currentConfiguration.getGuiOpt().nimbusSize);
			}
		}else {
			GUIStyelSettings.setStyle(currentConfiguration.getGuiOpt().getPreferredStyle(), currentConfiguration.getGuiOpt().nimbusSize);
		}
		
		if(initTxtWriter != null) {
			initTxtWriter.getCurrentTabEditor().applyStyleToTabs();
		}

		GUIStyelSettings.applyNimbusSize(currentConfiguration.getGuiOpt().nimbusSize);
	}
	
	// GETTERS //

	public CrypterPanel getPanelCrypter() {
		return panelCrypter;
	}

	public static void setPanelCrypter(CrypterPanel panelCrypteR) {
		panelCrypter = panelCrypteR;
	}
	
	public static InitOwner getCurrentConfInterface() {
		return new InitOwner() {
			
			@Override
			public INItializer getInitializer() {
				try {
					return (currentConfiguration != null)? currentConfiguration : new INItializer(new Wini(new File(GeneralConfig.DEFAULT_INI)));
				} catch (IOException e) {
					return new INItializer();
				}
			}
		};
	}
}

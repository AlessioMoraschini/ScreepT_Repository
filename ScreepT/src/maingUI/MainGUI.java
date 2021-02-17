/**
 *
 * =========================================================================================
 *  Copyright (C) 2019-2020
 *
 *  AM-Design-Development - (Alessio Moraschini) - All Rights Reserved
 * =========================================================================================
 *
 * You should have received a copy of the license with this file.
 * If not, please write to: info@am-design-development.com, or visit : https://www.am-design-development.com
 */
package maingUI;

import javax.swing.JFrame;
import javax.swing.JLabel;

import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;

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
import org.ini4j.Ini;
import org.ini4j.Wini;

import com.minhasKamal.ultimateCalculator.CalculatorMainApp;

import arch.InitOwner;
import dialogutils.JOptionHelperExtended;
import files.FileVarious;
import frameutils.frame.ImageViewerFrame;
import frameutils.utils.GuiUtilsExtended;
import gUI.frames.GeneralOptionsFrame;
import gUI.frames.GraphicPreferencesFrame;
import gUI.frames.ImageConverterFrame;
import gUI.panels.ConfigurationManagerPanel;
import gUI.panels.CrypterPanel;
import gUI.panels.TextEditor;
import gUI.utils.filedialog.FilesAndDirectoriesManagerGUI;
import gUI.utils.filedialog.INIfileChoosER;
import gui.ComponentResizer;
import gui.PluginManagerGUI;
import guistyle.GUIStyelSettingsExtended;
import impl.INItializer;
import jmenu.CustomJmenuItem;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.awt.SystemColor;
import java.awt.Dialog.ModalExclusionType;

import net.miginfocom.swing.MigLayout;
import resources.GeneralConfig;
import resources.IconsPathConfigurator;
import resources.ImagesPathConfigurator;
import resources.SoundsConfigurator;
import resources.SoundsManagerExtended;
import utility.manipulation.ConversionUtils;
import utils.SysUtils;

import javax.swing.MenuSelectionManager;
import javax.swing.SwingUtilities;

public class MainGUI extends MainGuiParent{

	
//////////////////////////// GUI ELEMENTS //////////////////////////////
////////////////////////////////////////////////////////////////////////
	
	// background image
	private JLabel labelBACKground;
	
	// FRAMES THAT OPEN IN A NEW DIALOG
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
	public static String preferredStyle = GUIStyelSettingsExtended.NimbusLookAndFeel; // preferredStyle (initialized to fully avoid null values)
	public static boolean DARK_THEME; // true if dark theme is selected
	
	// flags for no-window operations
	private static boolean FLAG_mntmRestart = false;
	private static boolean FLAG_mntmExit = false;
	private static boolean FLAG_loadPrjSub = false;
	private static boolean FLAG_importFromOtherPrjSub = false;
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
	
	public static JOptionHelperExtended dialogHelper;
	
	public AtomicBoolean isOnHomePage = new AtomicBoolean(true);
	public AtomicBoolean isUpdating = new AtomicBoolean(false);
	
	// Full screen+custom modes varying on loaded panel
	public AtomicBoolean isImmersiveModeOn = new AtomicBoolean();
	public AtomicBoolean isFullScreen = new AtomicBoolean(false);
	
	public static String newUpdatedJar = "";
	static File[] updatesDownloaded = new File[3];
	
	public WindowCreator windowCreator;
	
	public static boolean alreadyAskingForClose = false;
	
	 /************* BEGIN MAIN APPLICATION GENERAL METHOD  ***************
	 * 
	 * 		Calls contructor, initialize & Launch the application.
	 * 
	 * 			 (Called from MainGuiGeneralAppStarter())
	 * 
	 ************* BEGIN MAIN APPLICATION GENERAL METHOD  ****************/
	
	public static boolean mainApplicationGeneral(INItializer currentConf) {
		
		if(mainAppRunning) {
			return true;
		}
		
		currentConfiguration = currentConf;
		dialogHelper = new JOptionHelperExtended(MainGuiGeneralAppStarter.loaderGraphics);
		
		MainGuiGeneralAppStarter.loaderGraphics.setVisible(false);
		MainGuiUtils.askNameAtStartIfNotDefined();
		MainGuiGeneralAppStarter.loaderGraphics.setVisible(true);
		MainGuiGeneralAppStarter.loaderGraphics.toFront();
		
		try {
			now = new Date();
			mainAppRunning = true;
			
			MainGuiGeneralAppStarter.loaderGraphics.update("Initializing graphics... ", 18);
			
			// START MAIN WINDOW CONSTRUCTOR
			logger.info("Initializing main GUI-start");
			MainGUI window = new MainGUI();
			mainFrameScreept.setVisible(true);
			mainFrameScreept.setEnabled(true);
			mainGuiUtils = new MainGuiUtils(window);
			mainGuiManager = new MainGuiManager(mainGuiUtils);
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
					logger.info("## AUTOSAVE module Stopped (Closing ScreepT)");
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
								logger.warn("## AUTOSAVE-MANAGER: detected flagAutosave=false & Autosave is alive -> interrupting");
								autosaveModule.interrupt();
								logger.warn("## AUTOSAVE-MANAGER: [AUTOSAVE OFF] autosave module interrupted.");
							}else {
								logger.debug("## AUTOSAVE-MANAGER: running -> [AUTOSAVE "+currentConfiguration.getFileOpt().isFlagAutosave()+"] -> autosave Module running["+autosaveModule.isAlive()+"] - Interval: "+currentConfiguration.getFileOpt().getAutosaveInterval()/1000+"s");
							}
							
						} catch (InterruptedException e) {
				        	logger.warn("## AUTOSAVE-MANAGER interrupted.");
						}
					}
					logger.warn("## AUTOSAVE-MANAGER Stopped (Closing ScreepT)");
				}
			});
			
			// start autosave manager module
			autosaveManager.start();
			
			// MAIN WINDOW RESIZE LISTENER
//			mainFrameScreept.addComponentListener(new ComponentAdapter() {
//				@Override
//				public void componentResized(ComponentEvent componentEvent) {
//					SwingUtilities.invokeLater(new Runnable() {
//						public void run() {
//							MainGUI.mainAPP.resizeMainFrameDefaults();
//						}
//					});
//			    }
//			});
			
			// MAIN WINDOW CLOSE LISTENER
			mainFrameScreept.addWindowListener(new java.awt.event.WindowAdapter() {
			    @Override
			    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
			    	MainGuiManager.askForExit();
			  	}
			});
			
			SoundsManagerExtended.playSound(SoundsConfigurator.SOUND_POOL_SHOT, null);

			SwingUtilities.invokeLater(new Runnable() {
				
				@Override
				public void run() {
					// REFRESH GUI GENERAL
					MainGuiGeneralAppStarter.loaderGraphics.update("Updating GUI style...", 75, 350);
					GUIStyelSettingsExtended.updateThemeByName(currentConfiguration.getGuiOpt().getPreferredThemeName());
					if (GUIStyelSettingsExtended.isNimbus(currentConfiguration.getGuiOpt().getPreferredStyle())) {
						if (GUIStyelSettingsExtended.isDarkNimbus(currentConfiguration.getGuiOpt())) {
							
							GUIStyelSettingsExtended.setDarkNimbus(currentConfiguration.getGuiOpt().nimbusSize);
						} else {
							
							GUIStyelSettingsExtended.setLightNimbus(currentConfiguration.getGuiOpt().nimbusSize);
						}
					}else {
						GUIStyelSettingsExtended.setStyle(currentConfiguration.getGuiOpt().getPreferredStyle(), currentConfiguration.getGuiOpt().nimbusSize);
					}
					
					mainFrameScreept.toFront();
					mainFrameScreept.requestFocus();
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
						} else if (lastOpened.equals(ConfigurationManagerPanel.class)) {
							mainGuiUtils.startConfigManagerPanel(true);
						}
					}
					
					MainGuiGeneralAppStarter.loaderGraphics.update("Restoring Gui Elements From configuration...", 99, 200);
					updateGuiFromCurrConfig();
					
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							MainGuiUtils.startSignalListener();
						}
					});
					
					// Close loader
					GuiUtilsExtended.launchThreadSafeSwing(new Runnable() {
						public void run() {
							
							MainGuiGeneralAppStarter.loaderGraphics.update("Welcome to ScreepT", 100, 200);
							
							window.startTitleUpdater();
							
							if(currentConfiguration.getGenOpt().isFirstAccess()) {
								MainGuiUtils.welcomeMethodFirstTime();
							} 
							
							if (currentConfiguration.getGenOpt().isAutomaticUpdateOn()) {
								try {
									Thread.sleep(GeneralConfig.UPDATES_AUTOCHECK_DELAY);
									Thread updater = MainGuiUtils.lookForUpdates(true);
									updater.start();
									updater.join();
								} catch (Exception e) {
									logger.warn("Cannot wait " + GeneralConfig.UPDATES_AUTOCHECK_DELAY + " ms to check for updates", e);
								}
								currentConfiguration.getGenOpt().setFirstAccess(false);
								currentConfiguration.writeSingleValue(INItializer.GEN_OPT, "firstAccess", false);
								
								mainFrameScreept.toFront();
								mainFrameScreept.requestFocus();
							}
						}
					}); // End title updater and Updates check starters
				}
			});
			
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
		
		windowCreator = new WindowCreator(this);
		
		mainAPP = this;
		
		// initialize value for function uppercaseDirectories ()
		last_dir_used = currentConfiguration.getFileOpt().getLastSrcFolderPath();
		
		// INITIALIZE MAIN_GUI (See the initialize() function below this constructor)
		initialize();
		
		logger.info("## GUI components started");
		
		// SW window icon
		try {
			SW_ICON = ImageIO.read(new File(ImagesPathConfigurator.MAIN_APP_ICON));
			mainFrameScreept.setIconImage(SW_ICON);
			logger.info("icon image setted to main window");
		} catch (IOException e1) {
			logger.error("Exception happened!", e1);
		}

	}
	// END CONSTRUCTOR
	
	/**
	 * Initialize the contents of the frame.
	 */
	@SuppressWarnings("serial")
	private void initialize() {

		MainGuiGeneralAppStarter.loaderGraphics.update("Initializing screen properties...", 30);
 
		// INITIALIZE SCREEN PROPOERTIES
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		screenWidth = gd.getDisplayMode().getWidth();
		screenHeight = gd.getDisplayMode().getHeight();
				
		menuSize = currentConfiguration.getGuiOpt().getMenuFontSize();
		subMenuSize = menuSize-2;
		
		// INITIALIZE MAIN FRAME AND MAIN MENU
		mainFrameScreept = new JFrame();
		mainFrameScreept.setEnabled(false);
		mainFrameScreept.setModalExclusionType(ModalExclusionType.TOOLKIT_EXCLUDE);
		mainFrameScreept.getContentPane().setBackground(SystemColor.windowText);
		mainFrameScreept.setBounds(0, 0, 1500, 850 );
		mainFrameScreept.setMinimumSize(MainGuiGeneralAppStarter.minimumSize);
		mainFrameScreept.addMouseListener(new ComponentResizer());
		mainFrameScreept.setJMenuBar(menuBar);
		
 		// This will be used to print out various types of dialogs in this class
 		dialogHelper = new JOptionHelperExtended(mainFrameScreept.getContentPane());
		
		// FULL SCREEN SETTING
	    mainFrameScreept.setExtendedState(JFrame.MAXIMIZED_BOTH);
		mainFrameScreept.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
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
 		
 		// TOP-RIGHT TOGGLES : from right to left
 		for (int i = 0; i < 3; i++) {
 			toggleTopRightActions.add(() -> {});
 			final int fi = i;
			mntmTopRightToggles.add(new CustomJmenuItem(new AbstractAction("") {
				public void actionPerformed(ActionEvent e) {
					toggleTopRightActions.get(fi).doOnClick();
				}
			}));
		}
 		
 		// NOT OPENING VIEW - UNUSED RESOURCES OPTIMIZATION
 		mntmOptimizeResourcesUsage = new CustomJmenuItem(new AbstractAction("") {
 			public void actionPerformed(ActionEvent e) {
 				mntmOptimizeResourcesUsage.setEnabled(false);
 				SwingUtilities.invokeLater(() -> {
 					try {
 	 					Boolean cleanResult = mainGuiUtils.deReferenceUnusedPanels();
 	 					Thread.sleep(800);
 	 					String msg = "Cleaning skipped as requested.";
 	 					if(cleanResult != null) {
 	 						System.gc();
 	 						System.runFinalization();
 	 						if(cleanResult)
 	 							msg = "ScreepT's Resources usage optimized.";
 	 						else
 	 							msg = "Resources usage was already optimized, but I've done my best to improve it ;)";
 	 					}
 	 					
 	 					dialogHelper.info(msg);
 	 				} catch(Exception e1) {
 	 					String errorMessage = "Cannot optimize ScreepT's resources usage, something has gone wrong!";
 	 					logger.error(errorMessage, e1);
 	 					dialogHelper.warn(errorMessage, "Cannot clean resources");
 	 				} finally {
 	 					mntmOptimizeResourcesUsage.setEnabled(true);
 	 				}
 				});
 			}
 		});
 
		// NOT OPENING VIEW - RESTART SCREEPT
 		mntmRestart = new CustomJmenuItem(new AbstractAction("Restart") {
			public void actionPerformed(ActionEvent e) {
				if (!FLAG_mntmRestart) {
					FLAG_mntmRestart = true;
					
					MainGuiManager.restartApplication();
					
					FLAG_mntmRestart = false;
				}
			}
		});

		// NOT OPENING VIEW - EXIT AND CLOSE SCREEPT
		mntmExit = new CustomJmenuItem(new AbstractAction("Exit") {
			public void actionPerformed(ActionEvent e) {
				if (!FLAG_mntmExit) {
					FLAG_mntmExit = true;
					
					MainGuiManager.askForExit();
					
					FLAG_mntmExit = false;
				}
			}
		});

		// NOT OPENING VIEW - LOAD CUSTOM CONFIGURATION
		mntmloadPrjSub = new CustomJmenuItem(new AbstractAction("Load Custom Configuration") {
		    public void actionPerformed(ActionEvent ae) {
		   
	    		if (!FLAG_loadPrjSub) {
	    			SoundsManagerExtended.playSound(SoundsConfigurator.CLICK_2, null);
	    			FLAG_loadPrjSub = true;
					INIfileChoosER.readNLoadIniFile(currentConfiguration, GeneralConfig.DEFAULT_INI_FOLDER);
					FLAG_loadPrjSub = false;
					updateGuiFromCurrConfig();
				}
		    }
		});

		// NOT OPENING VIEW - IMPORT CONFIGURATION FROM OTHER SCREEPT
		mntmImportFromOtherScreepTPrjSub = new CustomJmenuItem(new AbstractAction("Load configuration from other installation") {
			public void actionPerformed(ActionEvent ae) {
				
				if (!FLAG_importFromOtherPrjSub) {
					SoundsManagerExtended.playSound(SoundsConfigurator.CLICK_2, null);
					FLAG_importFromOtherPrjSub = true;
					INIfileChoosER.loadDefaultsFromOtherInstallation(false);
					FLAG_importFromOtherPrjSub = false;
				}
			}
		});

		String msg1 = "Are you sure to reset ";
		String msg2 = "? All unsaved work will be lost.";
		
		// NOT OPENING VIEW - RESET ALL PANELS
		mntmResetAllPanels = new CustomJmenuItem(new AbstractAction("Reset all Panels") {
		    public void actionPerformed(ActionEvent ae) {
		    	if(dialogHelper.yesOrNo(msg1 + "all panels" + msg2, "Reset all panels?")) {
		    		SoundsManagerExtended.playSound(SoundsConfigurator.CLICK_2, null);
		    		mainGuiUtils.resetPanel(null, MainGUI.mainFrameScreept);
		    	}
			}
	    });
		
		// NOT OPENING VIEW - RESET TEXT EDITOR
		mntmResetTextEditor = new CustomJmenuItem(new AbstractAction("Reset Text Editor") {
			public void actionPerformed(ActionEvent ae) {
				if(dialogHelper.yesOrNo(msg1 + "Text Editor" + msg2, "Reset TextEditor?")) {
					SoundsManagerExtended.playSound(SoundsConfigurator.CLICK_2, null);
					mainGuiUtils.resetPanel(TextEditor.class, MainGUI.mainFrameScreept);
		    	}
			}
		});
		
		// NOT OPENING VIEW - RESET CRYPTER
		mntmResetCrypterPanel = new CustomJmenuItem(new AbstractAction("Reset CreepT") {
			public void actionPerformed(ActionEvent ae) {
				if(dialogHelper.yesOrNo(msg1 + "Crypter" + msg2, "Reset Crypter?")) {
					SoundsManagerExtended.playSound(SoundsConfigurator.CLICK_2, null);
					mainGuiUtils.resetPanel(CrypterPanel.class, MainGUI.mainFrameScreept);
		    	}
			}
		});
		
		// NOT OPENING VIEW - RESET CONFIG MANAGER
		mntmResetConfigManager = new CustomJmenuItem(new AbstractAction("Reset Configuration manager") {
			public void actionPerformed(ActionEvent ae) {
				if(dialogHelper.yesOrNo(msg1 + "Configuration Manager" + msg2, "Reset Conf. Manager?")) {
					SoundsManagerExtended.playSound(SoundsConfigurator.CLICK_2, null);
					mainGuiUtils.resetPanel(ConfigurationManagerPanel.class, MainGUI.mainFrameScreept);
		    	}
			}
		});
		
		// NOT OPENING VIEW - SAVE CUSTOM CONFIGURATION
		mntmSaveCurrent = new CustomJmenuItem(new AbstractAction("Save Current Configuration") {
		    public void actionPerformed(ActionEvent ae) {
		    	if (!FLAG_savePrjSub) {
		    		SoundsManagerExtended.playSound(SoundsConfigurator.CLICK_2, null);
		    		FLAG_savePrjSub = true;
		    		INIfileChoosER.saveCurrentConf(currentConfiguration, mainFrameScreept);
		    		FLAG_savePrjSub = false;
				}  	
		    }
		});

		// NOT OPENING VIEW - SAVE CUSTOM CONFIGURATION
		mntmExportPrjSub = new CustomJmenuItem(new AbstractAction("Export Current Configuration") {
		    public void actionPerformed(ActionEvent ae) {
		    	if (!FLAG_savePrjSubAs) {
		    		SoundsManagerExtended.playSound(SoundsConfigurator.CLICK_2, null);
		    		FLAG_savePrjSubAs = true;
					INIfileChoosER.exportIniFile(currentConfiguration, (JLabel)null);
					FLAG_savePrjSubAs = false;
				}  	
		    }
		});

		// NOT OPENING VIEW - RELOAD CONFIGURATION
		mntmReloadPrjSub = new CustomJmenuItem(new AbstractAction("Reload Configuration") {
		    public void actionPerformed(ActionEvent ae) {

		    	if (!FLAG_loadDefaultPrjSub) {
		    		SoundsManagerExtended.playSound(SoundsConfigurator.CLICK_2, null);
		    		FLAG_loadDefaultPrjSub = true;
					if (dialogHelper.yesOrNo(
							"Really want to load default config(unsaved changes will be lost)? ",
							"Warning - Reload INI default config?")) {
						try {
							currentConfiguration.setIniReadFile(new Ini(new File(GeneralConfig.DEFAULT_INI)));
							currentConfiguration.setIniFile(new Wini(new File(GeneralConfig.DEFAULT_INI)));
							dialogHelper.info("Configuration reloaded!", "Configuration reloaded!");
						} catch (IOException e) {
							dialogHelper.error("Error opening: " + GeneralConfig.DEFAULT_INI + " -> maybe it does not exist?", "An error occurred");
							logger.error("Exception happened!", e);
						}
						currentConfiguration.readConfigIniFile();
						updateGuiFromCurrConfig();
						dialogHelper.info("File '" + currentConfiguration.getIniFile().getFile().getName() + "' loaded!", "File loaded");
					} else {
						dialogHelper.warn("Configuration not loaded, current CONFIG mantained!", "Loading skipped");
					} 
					FLAG_loadDefaultPrjSub = false;
				}
		    }
		});

		// NOT OPENING VIEW - SAVE DEFAULT CONFIGURATION
		mntmSaveCurrentAs = new CustomJmenuItem(new AbstractAction("Save Current Configuration as Default") {
			public void actionPerformed(ActionEvent e) {
				if (!FLAG_mntmSaveCurrentAs) {
					SoundsManagerExtended.playSound(SoundsConfigurator.CLICK_2, null);
					FLAG_mntmSaveCurrentAs = true;
					try {
						currentConfiguration.setIniFile(new Wini(new File(GeneralConfig.DEFAULT_INI)));
						currentConfiguration.setIniReadFile(new Ini(new File(GeneralConfig.DEFAULT_INI)));

					} catch (IOException e1) {
						logger.error("Exception happened!", e1);
					}
					if (dialogHelper.yesOrNo("Really want to overwrite default configuration?", "Warning - Overwrite default configuration?")) {
						currentConfiguration.writeCurrentConfigIniFile();
						dialogHelper.info("File " + currentConfiguration.getIniFile().getFile().getAbsolutePath() + " saved!", "File saved");
					} else {
						dialogHelper.warn("Configuration not saved!", "Skipped!");
					}
					FLAG_mntmSaveCurrentAs = false;
				}
			}
		});

		// NOT OPENING VIEW - RELOAD STANDARD SCREEPT CONFIGURATION
		mntmloadSrcrYptDefaultConf = new CustomJmenuItem(new AbstractAction("Reset Original Configuration") {
		    public void actionPerformed(ActionEvent ae) {

		    	if (!FLAG_loadSrcrYptDefaultConf) {
		    		SoundsManagerExtended.playSound(SoundsConfigurator.CLICK_2, null);
		    		FLAG_loadSrcrYptDefaultConf = true;
					if (dialogHelper.yesOrNo(
							"Really want to Reset standard ScreepT configuration? Unsaved changes will be lost! ",
							"Warning - Reset INI standard config?")) {
						try {
							FileUtils.copyFile(new File(GeneralConfig.BACKUP_INI), new File(GeneralConfig.DEFAULT_INI));
							currentConfiguration.setIniReadFile(new Wini(new File(GeneralConfig.DEFAULT_INI)));
							currentConfiguration.setIniFile(new Wini(new File(GeneralConfig.DEFAULT_INI)));
						} catch (IOException e) {
							dialogHelper.error(
									"Error resetting standard configuration: it does not exist or IO error occurred, retry later!", (String)null);
							logger.error("Exception happened!", e);
						}
						MainGuiUtils.welcomeMethodFirstTime();
						dialogHelper.info("File '" + currentConfiguration.getIniFile().getFile().getName() + "' loaded!", "File loaded");
						currentConfiguration.readConfigIniFile();
						updateGuiFromCurrConfig();
					} else {
						dialogHelper.info("Configuration not loaded, current CONFIG mantained!", "Loading skipped");
					} 
					FLAG_loadSrcrYptDefaultConf = false;
				}
		    }
		});

		// NOT OPENING VIEW - LOG EXPORT
		mntmExportLog = new CustomJmenuItem(new AbstractAction("Export Event Log") {
			public void actionPerformed(ActionEvent e) {
				if (!FLAG_mntmExportLog) {
					SoundsManagerExtended.playSound(SoundsConfigurator.CLICK_2, null);
					FLAG_mntmExportLog = true;
					
					mainGuiManager.exportLogs();
					
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
					SoundsManagerExtended.playSound(SoundsConfigurator.SOUND_CLOCK, null);
					FLAG_mntmEraseLogs = true;
					File LOG_MAIN_FILE = new File(GeneralConfig.LOG_PATH);
					String coolFileSize = ConversionUtils.coolFileSize(LOG_MAIN_FILE.length());
					String msg =  "<html>Are you sure you to want to erase your logs? <br/> If you want you can click 'no' option and backup them, go under 'help submenu' [ Total size: " + coolFileSize + "]</html>";
					if(dialogHelper.yesOrNo(msg, "Erase Logs content?")) {
						if (FileVarious.clearFileContent(GeneralConfig.LOG_PATH) && FileVarious.clearFileContent(GeneralConfig.LOG_LAUNCHER_PATH)) {
							logger.debug("Files deleted");
							dialogHelper.info("Logs deleted - as you asked ;)", "Logs deleted");
						}else {
							logger.error("Files not deleted :: An error occurred :(");
							dialogHelper.error("Something went wrong :( Some files may still contain stuff.", (String)null);
						}
					} else {
						logger.debug("Logs not deleted :: skipped by user.");
						dialogHelper.info("Logs not deleted - as you asked ;)", "Logs not deleted");
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
					SoundsManagerExtended.playSound(SoundsConfigurator.CLICK_2, null);
					FLAG_mntmTouppercaseDirectory = true;
					last_dir_used = FilesAndDirectoriesManagerGUI.renameFirstLetterDirNames(mainFrameScreept, last_dir_used);
					FLAG_mntmTouppercaseDirectory = false;
				}
			}
		});
		
		// NOT OPENING VIEW - DELETE RECURSIVELY DIRECTORY'S CONTENT BY EXTENSION
		mntmDeleteDirectoryContentByExtension = new CustomJmenuItem(new AbstractAction("Delete Directory's content by extension") {
			public void actionPerformed(ActionEvent e) {
				if (!FLAG_mntmDeleteDirectoryContentByExtension) {
					SoundsManagerExtended.playSound(SoundsConfigurator.CLICK_2, null);
					FLAG_mntmDeleteDirectoryContentByExtension = true;
					FilesAndDirectoriesManagerGUI.deleteContentByExtension(mainFrameScreept, currentConfiguration);
					FLAG_mntmDeleteDirectoryContentByExtension = false;
				}
			}
		});
		
		// NOT OPENING VIEW - DELETE RECURSIVELY DIRECTORY'S CONTENT
		mntmDeleteDirectoryAllContentRecursive = new CustomJmenuItem(new AbstractAction("Delete Directory's content (All except selected)") {
			public void actionPerformed(ActionEvent e) {
				if (!FLAG_mntmDeleteDirectoryContentRecursive) {
					SoundsManagerExtended.playSound(SoundsConfigurator.CLICK_2, null);
					FLAG_mntmDeleteDirectoryContentRecursive = true;
					FilesAndDirectoriesManagerGUI.deleteAllContent(mainFrameScreept, currentConfiguration);
					FLAG_mntmDeleteDirectoryContentRecursive = false;
				}
			}
		});
		
		// NOT OPENING VIEW - DELETE DIRECTORY'S CONTENT
		mntmEmptyDirectoryRecursive = new CustomJmenuItem(new AbstractAction("Empty all content (doesn't delete folders)") {
			public void actionPerformed(ActionEvent e) {
				if(!FLAG_mntmDeleteDirectoryContentDirsOnlyRecursive) {
					SoundsManagerExtended.playSound(SoundsConfigurator.CLICK_2, null);
					FLAG_mntmDeleteDirectoryContentDirsOnlyRecursive = true;
					FilesAndDirectoriesManagerGUI.deleteOnlyFilesInFolder(mainFrameScreept, currentConfiguration);
					FLAG_mntmDeleteDirectoryContentDirsOnlyRecursive = false;
				}
			}
		});

		// NOT OPENING VIEW - MAKE DIRS FROM STRING PATH
		mntmMakeDirsFromPath = new CustomJmenuItem(new AbstractAction("Create directory from String path") {
			public void actionPerformed(ActionEvent e) {
				if (!FLAG_mntmMakeDirsFromPath) {
					SoundsManagerExtended.playSound(SoundsConfigurator.CLICK_2, null);
					FLAG_mntmMakeDirsFromPath = true;
					FilesAndDirectoriesManagerGUI.makeDirsFromPath(mainFrameScreept, currentConfiguration);
					FLAG_mntmMakeDirsFromPath = false;
				}
			}
		});
		
		// NOT OPENING VIEW - COPY DIRECTORY STRUCTURE
		mntmCopyDirStructure = new CustomJmenuItem(new AbstractAction("Copy directory structure (Copy folders, not files)") {
			public void actionPerformed(ActionEvent e) {
				if(!FLAG_mntmCopyDirStructure) {
					SoundsManagerExtended.playSound(SoundsConfigurator.CLICK_2, null);
					FLAG_mntmCopyDirStructure = true;
					FilesAndDirectoriesManagerGUI.copyDirStructure(mainFrameScreept, currentConfiguration);
					FLAG_mntmCopyDirStructure = false;
				}
			}
		});
		
	///////////////// HANDLERS THAT OPEN A NEW WINDOW  ////////////////////////
		
		// open in a new window - CUSTOMIZE GRAPHICS
		mntmCustomizeUIView = new CustomJmenuItem(new AbstractAction("Customize Graphics") {
			public void actionPerformed(ActionEvent e) {
				
				windowCreator.newGraphicPreferencesFrame();
			}
		});
		
		// fullScreen mode toggle
		mntmFullScreenMode = new CustomJmenuItem(new AbstractAction("Full Screen Mode") {
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						if (mainFrameScreept.isUndecorated()) {
						} else {
						}
						
						setFullScreenEnabled(!mainFrameScreept.isUndecorated());
						
						// Update txtArea to adapt new size
						if(textEditorPanel != null && textEditorPanel.textEditorUtils != null) {
							textEditorPanel.initGuiFromConfig(false, true);
						}
						
						// RESYNC JSCROLL BUG WORKAROUND
						if (MainGUI.textEditorPanel != null && MainGUI.textEditorPanel instanceof TextEditor) {
							JScrollPane target = ((TextEditor)MainGUI.textEditorPanel).mainScrollPane;
							GuiUtilsExtended.scrollJScrollPane(target, true);
							textEditorPanel.textEditorUtils.resizeFrameToDefault(mainAPP);
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
				
				windowCreator.newGeneralOptionsFrame();
			}
		});
		
		// open in a new window - KEYGEN
		mntmgenerateKeyImageSub = new CustomJmenuItem(new AbstractAction("Generate Key Image") {
		    public void actionPerformed(ActionEvent ae) {
	    		
		    	windowCreator.newKeyGenFrame();
		    }
		});
		
		// open in a new window - PSW_KEYGEN
		mntmgenerateKeyPswSub = new CustomJmenuItem(new AbstractAction("Generate Password") {
			public void actionPerformed(ActionEvent ae) {
				
				windowCreator.newGeneratePassordFrame(null);
			}
		});

		// open in a new window - IMPORT/EXPORT WORKSPACE - Text Editor
		mntmFileSearch = new CustomJmenuItem(new AbstractAction("Search text into files") {
			public void actionPerformed(ActionEvent ae) {
				
				windowCreator.fileContentSearch();
			}
		});

		// open in a new window - IMPORT/EXPORT WORKSPACE - Text Editor
		mntmTxtEditorImpExpCryptedWkspc = new CustomJmenuItem(new AbstractAction("Import/Export crypted workspace") {
			public void actionPerformed(ActionEvent ae) {
				
				windowCreator.newImportExportCryptWorkspace();
			}
		});
		
		// open in a new window - Theme Editor
		mntmThemeEditor = new CustomJmenuItem(new AbstractAction("Color Theme editor") {
			public void actionPerformed(ActionEvent e) {
				windowCreator.newSchemeManager();
			}
		});
		
		
		// open in a new window - NET INFO
		mntmnetInfo = new CustomJmenuItem(new AbstractAction("Network Info") {
		    public void actionPerformed(ActionEvent ae) {
		    	
	    		windowCreator.newNetInfoFrame();
		    }
		});
		
		// open in a new window - NET INFO
		mntmhwInfo = new CustomJmenuItem(new AbstractAction("System Info") {
		    public void actionPerformed(ActionEvent ae) {
	    		
		    	windowCreator.newHwInfoFrame();
		    }
		});
		
		// open in a new window - File Hash checker
		mntmHashChecker = new CustomJmenuItem(new AbstractAction("File Hash checker") {
			public void actionPerformed(ActionEvent e) {
				
				windowCreator.newHashChecker();
			}
		});
		
		// open in a new window - File Hash checker
		mntmClipboardStandalone = new CustomJmenuItem(new AbstractAction("Clipboard tool") {
			public void actionPerformed(ActionEvent e) {
				
				windowCreator.newClipboardFrame();
			}
		});

		// open in a new window - Built-in Calculator
		mntmCalculator = new CustomJmenuItem(new AbstractAction("Calculator") {
			public void actionPerformed(ActionEvent e) {
				
				CalculatorMainApp.getInstance(getMainFrame());
			}
		});
		
		// open in a new window - Built-in Shell
		mntmShellStandalone = new CustomJmenuItem(new AbstractAction("System Console") {
			public void actionPerformed(ActionEvent e) {
				
				windowCreator.newShellFrame();
			}
		});

		// open in a new window - Built-in Shell
		mntmFileContentSearch = new CustomJmenuItem(new AbstractAction("File content search") {
			public void actionPerformed(ActionEvent e) {
				
				windowCreator.fileContentSearch();
			}
		});
		
		// open in a new window - File Hash checker
		mntmColorPicker = new CustomJmenuItem(new AbstractAction("Color picker") {
			public void actionPerformed(ActionEvent e) {
				windowCreator.newColorPickerFrame();
			}
		});
		
		// open in a new window - DICTIONARY (KEY IMG)
		mntmViewKeyDictionary = new CustomJmenuItem(new AbstractAction("View Key Dictionary") {
		    public void actionPerformed(ActionEvent e) {
	    		
		    	windowCreator.newKeyDIctionaryFrame();
			}
		});
		
		// open in a new window - IMAGE CONVERTER
		mntmImageConverter = new CustomJmenuItem(new AbstractAction("Image Converter") {
			public void actionPerformed(ActionEvent e) {
				
	    		windowCreator.newImageConverterFrame();
			}
		});

		ImageViewerFrame.imageConverterOpener = () -> {
			ImageConverterFrame converter = windowCreator.newImageConverterFrame();
			SwingUtilities.invokeLater(() -> {
				converter.loadImage(ImageViewerFrame.getViewerInstanceList().get(0).imageFile);
			});
		};
		
		// open in a new window - IMAGE CONVERTER
		mntmImageViewer = new CustomJmenuItem(new AbstractAction("Image Viewer") {
			public void actionPerformed(ActionEvent e) {
				if (ImageViewerFrame.history.isEmpty()) {
					ImageViewerFrame.loadIntoLastExistingInstance(new File(ImagesPathConfigurator.IMAGE_LOGO_AM_MAIN));
				} else {
					ImageViewerFrame.loadIntoLastExistingInstance(ImageViewerFrame.history.getLast());
				}
			}
		});
		
		// open in a new window - ABOUT ScreepT
		mntmAboutScreept = new CustomJmenuItem(new AbstractAction("About - ScreepT") {
			public void actionPerformed(ActionEvent e) {
				
				windowCreator.newAboutSectionFrame();
			}
		});

		// open in a new window - ABOUT ScreepT
		mntmLicenseScreept = new CustomJmenuItem(new AbstractAction("License - ScreepT") {
			public void actionPerformed(ActionEvent e) {
				
				windowCreator.newLicenseFrame(false, false);
			}
		});
		
		// open in a new window - ChangeLog view
		mntmChangeLog = new CustomJmenuItem(new AbstractAction("View changeLog") {
			public void actionPerformed(ActionEvent e) {
				
				windowCreator.newChangeLogFrame();
			}
		});
		
		// ASYNC HTML TUTORIAL PANE
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				windowCreator.newTutorialFrame(false);
			}
		});
		
		mntmUserDocumentation = new CustomJmenuItem(new AbstractAction("User Guide") {
			public void actionPerformed(ActionEvent e) {
				windowCreator.newTutorialFrame(true);
			}
		});
		
		// View Website
		mntmViewWebsite = new CustomJmenuItem(new AbstractAction("Visit Website") {
			public void actionPerformed(ActionEvent e) {
				
				SysUtils.openUrlInBrowser("https://"+GeneralConfig.WEBSITE);
				SoundsManagerExtended.playSound(SoundsConfigurator.SOUND_PLING, null);
			}
		});

		// Check and download/install updates
		mntmLookForUpdates = new CustomJmenuItem(new AbstractAction("Check for software updates...") {
			public void actionPerformed(ActionEvent e) {
				Thread updatesTHread = MainGuiUtils.lookForUpdates(false);
				updatesTHread.start();
			}
		});

		// Check and download/install Plugins
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
									}
								};

								pluginManagerGUI = new PluginManagerGUI(null, MainGuiGeneralAppStarter.pluginManager, libCheckNRefreshGUI);
								GuiUtilsExtended.setFrameIcon(IconsPathConfigurator.ICON_PLUGINS, pluginManagerGUI);
							} else {
								pluginManagerGUI.toFront();
							}
							pluginManagerGUI.setVisible(true);
						} catch (Throwable e1) {
							String errorMsg = "Cannot load Plugin Manager: ";
							if(e1 instanceof TimeoutException) {
								errorMsg += "server is unreachable!";
							} else {
								errorMsg += "an error occurred :(";
							}
							dialogHelper.error(errorMsg, "An error occurred");
							logger.error(errorMsg, e1);
						}
					}
				}).start();
			}
		});
		
	////////////////  NOW HANDLERS THAT OPEN IN THE MAIN FRAME  ////////////////////////
		
		// open in main frame - FILE CRYPTER
		mntmCreepter = new CustomJmenuItem(new AbstractAction("Creepter Module - Crypt and Decrypt files") {
		    public void actionPerformed(ActionEvent ae) {
		    	mainGuiUtils.startCrypterPanel(false);
		    }
		});
		
		// open in main frame - CONFIGURATION MANAGER
		mntmInspectCurrentConfiguration = new CustomJmenuItem(new AbstractAction("Configuration Manager Module") {
		    public void actionPerformed(ActionEvent ae) {
	    		mainGuiUtils.startConfigManagerPanel(false);
		    }
		});
		
		// open in main frame - TEXT EDITOR
		mntmTextEditor = new CustomJmenuItem(new AbstractAction("Text Editor Module") {
			public void actionPerformed(ActionEvent e) {
				mainGuiUtils.startTextEditor(false);
			}
		});

		// open in main frame - RETURN TO WELCOME PAGE
		mntmReturnToWelcome = new CustomJmenuItem(new AbstractAction("Return to Welcome Page") {
			public void actionPerformed(ActionEvent e) {
				isOnHomePage.set(true);
				SoundsManagerExtended.playSound(SoundsConfigurator.SOUND_PLING, null);
				setBackgroundImage();
				
				setTopRightTogglesEnabled(false);
				
				MainGUI.currentConfiguration.getGenOpt().setLastPanelOpened(MainGUI.class);
		    	MainGUI.currentConfiguration.writeSingleValue(INItializer.GEN_OPT, "lastPanelOpened", MainGUI.class.getCanonicalName());
			}
		});

		// BACKGROUND RESIZER //
		mainFrameScreept.addComponentListener(new ComponentListener() {
			public void componentShown(ComponentEvent e) {
				SwingUtilities.invokeLater(() -> {resizeMainFrameDefaults();});
			}
			public void componentResized(ComponentEvent e) {
				SwingUtilities.invokeLater(() -> {resizeMainFrameDefaults();});
			}
			public void componentMoved(ComponentEvent e) {
				SwingUtilities.invokeLater(() -> {resizeMainFrameDefaults();});
			}
			public void componentHidden(ComponentEvent e) {
				SwingUtilities.invokeLater(() -> {resizeMainFrameDefaults();});
			}
		});
		
	////////// END OF MENU MOUSE CLICK - HANDLERS DECLARATION  ////////////////
	
		MainGuiGeneralAppStarter.loaderGraphics.update("Adding ActionHandlers...", 50);
		logger.debug("Adding ActionHandlers...");
 		addHandlers();
 		
 		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(getKeyEventHandler());
 
		// once each component is declared add it to the menu bar
 		logger.debug("Initializing main menuBar...");
 		MainGuiGeneralAppStarter.loaderGraphics.update("Structuring menu...", 60);
		structureMenuBar();
		
		// check non compatible options with current JVM
		checkVersionAndDisableUnsupportedOpt();
		
		mainFrameScreept.requestFocus();
		
		logger.debug("MainGui instance initialized");
	}
	
	
//////////	NOW SOME USEFUL METHOD  ////////////////
	
	public void setFullScreenEnabled(boolean enable) {
		
		if(enable) {
			mainFrameScreept.dispose();
			preFullScreenSize = GuiUtilsExtended.makeFullScreen(mainFrameScreept);
			mainFrameScreept.setSize(preFullScreenSize);
			mainFrameScreept.setMinimumSize(GuiUtilsExtended.getScreenSize(mainFrameScreept));
			mainFrameScreept.setLocationRelativeTo(mainFrameScreept);
			mainFrameScreept.setVisible(true);
			isFullScreen.set(true);
			mntmFullScreenMode.setIcon(new ImageIcon(IconsPathConfigurator.ICON_LESS));
//			movePanelMouseListener = MovePanelMouseListener.applyMoveListenerR(frmScryptV); // USE TO ADD LISTENER DRAG PANEL
		
		} else {
//			MovePanelMouseListener.removeMoveListener(frmScryptV, movePanelMouseListener); // USE TO REMOVE LISTENER DRAG PANEL
			if(preFullScreenSize == null) {
				preFullScreenSize = GuiUtilsExtended.getScreenSize(mainFrameScreept);
			}
			mainFrameScreept.dispose();
			mainFrameScreept.setUndecorated(false);
			mainFrameScreept.setLocationRelativeTo(mainFrameScreept);
			mainFrameScreept.setSize(preFullScreenSize);
			mainFrameScreept.setVisible(true);
			isFullScreen.set(false);
			menuBar.setVisible(true);
			mntmFullScreenMode.setIcon(new ImageIcon(IconsPathConfigurator.ICON_EXPAND));
		}
		
		resizeMainFrameDefaults();
	}
	
	public void resizeMainFrameDefaults() {

		mainFrameScreept.setMinimumSize(MainGuiGeneralAppStarter.minimumSize);
		
		// resize background - Only on home page to improve performance
		if (isOnHomePage.get()) {
			labelBACKground.setSize(mainFrameScreept.getContentPane().getSize());
		}

		// resize textEditor if present
		if (mainFrameScreept.getContentPane().getComponent(0) instanceof TextEditor) {
			textEditorPanel.textEditorUtils.resizeFrameToDefault(mainAPP);
		}

		// resize textEditor if present
		if (mainFrameScreept.getContentPane().getComponent(0) instanceof CrypterPanel) {
			mainFrameScreept.setMinimumSize(MainGuiGeneralAppStarter.minimumSizeCrypter);
			crypterPanel.resizeFrameToDefault(mainAPP);
		}
	}
	
	public void addHandlers() {
		
		// DEACTIVATE OPEN MENU ON CLICK OUTSIDE IT WHEN OPEN
		mainFrameScreept.addMouseListener(new MouseListener() {
			
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
            mainFrameScreept.getContentPane().add(MAIN_PANEL);
            MAIN_PANEL.setLayout(new MigLayout(GeneralConfig.DEBUG_GRAPHICS+"", "[90%]", "[1.00][100.52%,baseline]"));
            
            if(APPLICATION_MAIN_BCKG_IMAGE == null) {
            	readBackgroundImage();
            }
            
            labelBACKground = new JLabel(new ImageIcon(APPLICATION_MAIN_BCKG_IMAGE));
		   	JPanel contentPane = (JPanel) mainFrameScreept.getContentPane();
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
		String defName = "ScreepT_Execution_Log_" + df.format(now) + ".zip";
		
		JFileChooser fileChooser = new JFileChooser();		
		fileChooser.setDialogTitle("Export Log File");
		fileChooser.setSelectedFile(new File(defName));
		fileChooser.setCurrentDirectory(new File(currentConfiguration.getFileOpt().getLastDstFolderPath()));		
		File correctFile = null;
		
		int returnVal = fileChooser.showSaveDialog(parent);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            
        	File selectedFile = fileChooser.getSelectedFile();
        	selectedFile = FileVarious.changeExtension(selectedFile, ".zip");	
        	selectedFile = FileVarious.uniqueJavaObjFile(selectedFile);
        	String givenExtension = FilenameUtils.getExtension(selectedFile.getName());
        	
        	if(selectedFile==null||!givenExtension.equalsIgnoreCase("zip")) {
        		dialogHelper.error("Please specify a valid 'zip' file!", "Wrong File format");
        	}else {
        		correctFile = selectedFile;
        		currentConfiguration.getFileOpt().setLastDstFolderPath(correctFile.getAbsolutePath());

        	}
        }else {
        	dialogHelper.info("Skipped after user request.");
        }
        return correctFile;
	}
	
	private void checkVersionAndDisableUnsupportedOpt() {
		
		if(!MainGuiUtils.checkJavaVersionMaxCompatibility()) {
			String tooltipMSG = "This option is unsupported with current JVM version (" + currentJvmVersion 
					+ "). Run with version under Max JVM version supported to use this functionality (Max allowed: "
					+ GeneralConfig.MAX_JVM_COMPLETE_COMPATIBILITY + ")";
			
			// Disable non compatible options
			mntmhwInfo.setEnabled(false);
			
			mntmhwInfo.setToolTipText(tooltipMSG);
		
		} else {
			if(!MainGuiGeneralAppStarter.hwInfoAvailable) {
				mntmhwInfo.setEnabled(false);
				mntmhwInfo.setToolTipText("Hardware info tool not found in this installation, download plugin and restart in order to use it");
			} 
			
		}
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
		if(textEditorPanel != null && mainGuiUtils.isTextEditorActive()) {
			textEditorPanel.initGuiFromConfig(true, true);
		}
		
		// CRYPTER REFRESH
		if(crypterPanel != null && mainGuiUtils.isCrypterActive()) {
			crypterPanel.initGuiFromConfig();
		}
		
		// CONFIGURATOR REFRESH
		if(configManagerPanel != null && mainGuiUtils.isConfManagerActive()) {
			configManagerPanel.initGuiFromConfig();
		}
		
		// PANEL GRAPHICS PREFERENCES
		if(GraphicPreferencesFrame.singletonInstance!= null && GraphicPreferencesFrame.singletonInstance.isVisible()) {
			GraphicPreferencesFrame.singletonInstance.initGuiFromConfig();
		}
		
		// PANEL GENERAL PREFERENCES
		if(GeneralOptionsFrame.singletonInstance != null && GeneralOptionsFrame.singletonInstance.isVisible()) {
			GeneralOptionsFrame.singletonInstance.initGuiFromConfig();
		}
		
		if(textEditorPanel != null) {
			textEditorPanel.getCurrentTabEditor().applyStyleToTabs();
		}

		if(GUIStyelSettingsExtended.isNimbus(currentConfiguration.getGuiOpt().getPreferredStyle())) {
			if (GUIStyelSettingsExtended.isDarkNimbus(currentConfiguration.getGuiOpt())) {
				
				GUIStyelSettingsExtended.setDarkNimbus(currentConfiguration.getGuiOpt().nimbusSize);
			} else {
				GUIStyelSettingsExtended.setLightNimbus(currentConfiguration.getGuiOpt().nimbusSize);
			}
		}else {
			GUIStyelSettingsExtended.setStyle(currentConfiguration.getGuiOpt().getPreferredStyle(), currentConfiguration.getGuiOpt().nimbusSize);
		}
		GUIStyelSettingsExtended.applyNimbusSize(null, currentConfiguration.getGuiOpt().nimbusSize);
	}
	
	// GETTERS //

	public CrypterPanel getPanelCrypter() {
		return crypterPanel;
	}

	public static void setPanelCrypter(CrypterPanel panelCrypteR) {
		crypterPanel = panelCrypteR;
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

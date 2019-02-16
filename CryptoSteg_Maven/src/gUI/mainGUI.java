package gUI;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

import gUI.panels.ChargeIniLog;
import gUI.panels.CrypterPanel;
import gUI.panels.GeneralOptionsFrame;
import gUI.panels.KeyGenPanel;
import gUI.panels.NetInfoPanel;
import gUI.panels.TextFileWriter;
import gUI.utils.filedialog.DirectoryManagementGUI;
import gUI.utils.filedialog.INIfileChoosER;
import gUI.panels.GraphicPreferencesFrame;
import gUI.panels.ImageConverterFrame;
import gUI.panels.KeyDictionaryPanel;
import iNIT.INItializer;

import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.awt.SystemColor;

import net.miginfocom.swing.MigLayout;
import uTILS.FileStuff;

import java.awt.Dialog.ModalExclusionType;
import java.awt.Color;
import javax.swing.Action;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;

public class mainGUI {

	// mainGUI logger
	static Logger logger = Logger.getLogger(mainGUI.class);

	// autosave constant refresh period in millisecond
	private static final long AUTOSAVE_MANAGER_PERIOD=20000;
	
	// AUTOSAVE MODULE - only if specified in fileOptions
	private static Thread autosaveModule;
	
	// the name of the application used elsewhere on the sw
	private static final String APPLICATION_NAME = "ScrYpt_V1.0";
	private static final String APPLICATION_MAIN_BCKG = "Logo-AM_v2.jpg";
	
	// constant files & directories prefix
	private static final String RESOURCES_DIR="Resources_Scrypt/";
	private static final String DEFAULT_INI="Resources_Scrypt/CONFIG.ini";
	private static final String BACKUP_SW_INI="Resources_Scrypt/Backup/BACKUP_CONFIG.ini";
	private static final String DEFAULT_INI_FOLDER="INI_FILES";
	
	// log_trace location
	private static final String LOG_PROPERTIES_PATH = RESOURCES_DIR + "log4j.properties";
	private static final String LOG_PATH = "LOG/Execution_Log_Trace.log";
	// some used lookAndFeel
	public static String NimbusLookAndFeel = "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel";
	public static String WindowsLookAndFeel = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
	
	// Filed for uppercaseFirstLetter "various' function"
	private String last_dir_used; 
	
	// GUI ELEMENTS
	
	// the main frame for placing the content
	volatile private JFrame frmScryptV;
	
	// FRAMES THAT OPEN IN A NEW DIALOG
	private static GraphicPreferencesFrame panelGuiOptions;
	private static KeyGenPanel panelKeyGen;
	private static KeyDictionaryPanel panelKeyDict;
	private static GeneralOptionsFrame panelGeneralOptions;
	private static ImageConverterFrame panelImgConv;
	private static NetInfoPanel netInfoPanel;
	
	// PANELS THAT OPEN IN THE MAIN FRAME
	private TextFileWriter initTxtWriter;
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
	
	private JMenuItem mntmExit;
	private JMenuItem loadPrjSub;
	private JMenuItem savePrjSub;
	private JMenuItem loadDefaultPrjSub;
	private JMenuItem mntmUserDocumentation;
	private JMenuItem mntmSaveCurrentAs;
	private JMenuItem mntmGenOpt;
	private JMenuItem mntmCustomizeUIView;
	private JMenuItem mntmInspectCurrentConfiguration;
	private JMenuItem mntmCrypter;
	private JMenuItem generateKeyImageSub;
	private JMenuItem mntmViewKeyDictionary;
	private JMenuItem mntmNewTextFile;
	private JMenuItem netInfo;
	private JMenuItem mntmImageConverter;
	private JMenuItem mntmTouppercaseDirectory;
	private JMenuItem mntmReturnToWelcome;
	private JMenuItem mntmAboutScreept;
	private JMenuItem mntmExportLog; 
	private JMenuItem loadSrcrYptDefaultConf;
	
	// END GUI ELEMENTS
	
	// this file is the abstraction of the current settings used by various sub-programs and application of this main_GUI.
	// if you need to get or set current configuration in volatile memory, then use pojo's mapped in this object, please.	
	public static INItializer currentConfiguration;
	
	// VALUES FROM INI
	
	// global values representing the screen size
	public static int screenWidth;
	public static int screenHeight;

	// true if dark theme is selected
	public static boolean DARK_THEME;
	
	// preferredStyle (initialized to fully avoid null values)
	public static String preferredStyle = WindowsLookAndFeel;
	
	private int menuSize;
	private int subMenuSize;
	
	// date used for log operations
	private static Date now;
	
	// FLAGS TO SIGNAL THAT A MODULE IS OPENED (JDIALOG) 
	// for jpanels they refresh only the first time opened or if manually refreshed (TODO)
	private static boolean flagFrameKeyGen=false;
	private static boolean flagFrameGenOptions=false;
	private static boolean flagFrameGuiOptions=false;
	private static boolean flagFrameKeyDict=false;	
	private static boolean flagImgConv=false;
	private static boolean flagInfoPanel=false;
	
	volatile private static boolean running = false;
	
	/**
	 * MAINT METHOD
	 * 
	 * Calls contructor, initialize & Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					now = new Date();
					
					// empty old log file
					// useful for test phase
//					
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
					
					// initialize style
					preferredStyle = currentConfiguration.getGuiOpt().getPreferredStyle();
					
					// initialize GUI default Style from ini
					try {
						UIManager.setLookAndFeel(preferredStyle);
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
					
					// menu List
//					UIManager.getLookAndFeelDefaults().put("MenuBar[Enabled].backgroundPainter", Color.RED);
					
					// menu item
//					UIManager.getLookAndFeelDefaults().put("MenuItem[Enabled].textForeground", Color.WHITE);
//					UIManager.getLookAndFeelDefaults().put("MenuItem[Enabled].backgroundPainter", Color.black);
					
					// start window using constructor (see below)
					logger.debug("Initializing main GUI-start");
					mainGUI window = new mainGUI();
					window.frmScryptV.setVisible(true);
					logger.debug("Main GUI initialized correctly.");
					

					// ACTIVE MODULES FLAG UPDATER THREAD
					new Thread(new Runnable() {
						
						@Override
						public void run() {
							logger.debug("## MODULES FLAG UPDATER started");
							while(true) {
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
										flagInfoPanel=false;
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
							running = true;
							logger.debug("## AUTOSAVE module started, interval: "+currentConfiguration.getFileOpt().getAutosaveInterval() + " milliseconds");
							while (running) {
												
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
							while(true) {
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
						}
					});
					
					// start autosave manager module
					autosaveManager.start();
					
					// add listener for window closing
					window.frmScryptV.addWindowListener(new java.awt.event.WindowAdapter() {
					    @Override
					    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
					        now=new Date();
					        if(autosaveManager.isAlive()) {
					        	autosaveManager.interrupt();
					        	logger.warn("## AUTOSAVE-MANAGER interrupted.");
					        }
					        if(autosaveModule.isAlive()) {
						        autosaveModule.interrupt();
						        logger.warn("## AUTOSAVE MODULE interrupted.");
					        }
					        
					        // if autosave is on save before quit
					        if(currentConfiguration.getFileOpt().isFlagAutosave()) {
								try {
									currentConfiguration.writeCurrentConfigIniFile();
									logger.debug("AUTOSAVE ON: file saved before exiting ["
											+ currentConfiguration.getIniFile().getFile().getAbsolutePath() + "]");
								} catch (Exception e) {
									logger.error("Exception happened!", e);
								}
							}
					        
					      	logger.debug("\n\n##################################################\n\n"+now.toString()+"\n\n"
									+ APPLICATION_NAME+" CLOSED!\n\n"
									+ "##################################################\n\n");
					      	
					      	running = false;
					  	}
					});
					
					//window.frmScryptV.getContentPane().setPreferredSize( new Dimension(screenWidth, screenHeight));
				} catch (Exception e) {
					logger.error("Exception happened!", e);
				}
			}
		});
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
			frmScryptV.setIconImage(ImageIO.read(new File(RESOURCES_DIR + "icon.jpg")));
			logger.debug("icon image setted to main window");
		} catch (IOException e1) {
			logger.error("Exception happened!", e1);
		}
		
		// TITLE UPDATER - Thread that refreshes every second the username of the title
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					frmScryptV.setTitle(currentConfiguration.getGenOpt().getUserName()+" is using " + APPLICATION_NAME);				
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						frmScryptV.setTitle("Scrypt - V1.0 - A.M. developement");	
					}
				}
			}
		}).start();
		logger.debug("## TITLE UPDATER started.");
	}
	// END CONSTRUCTOR
	
	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {

		// ASK FOR USERNAME IF NULL OR FIRST TIME
		if(currentConfiguration.getGenOpt().getUserName().equals("") || currentConfiguration.getGenOpt().getUserName().length()>20) {
			askUsername();
		}
		
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
		frmScryptV.setBounds(0, 0, 1200, 860 );
		
		// FULL SCREEN SETTING
	    frmScryptV.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frmScryptV.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frmScryptV.setVisible(true);
        
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
		
//////////////////////////// DEFINE HANDLERS  //////////////////////////////////
		
		// NOT OPENING VIEW - EXIT AND CLOSE SCRYPT
		mntmExit = new JMenuItem(new AbstractAction("Exit") {
			public void actionPerformed(ActionEvent e) {
				if(JOptionPane.showConfirmDialog(frmScryptV, "Really want to exit? :(","Exit Program",JOptionPane.YES_NO_OPTION)==JOptionPane.OK_OPTION) {
					now=new Date();
			      	logger.debug("\n\n##################################################\n\n"+now.toString()+"\n\n"
							+ APPLICATION_NAME+" CLOSED!\n\n"
							+ "##################################################\n\n");
			      	
			      	running = false;
			      	
					frmScryptV.dispose();
				}
			}
		});
		
		// NOT OPENING VIEW - LOAD CUSTOM CONFIGURATION
		loadPrjSub = new JMenuItem(new AbstractAction("Load Custom Configuration") {
		    public void actionPerformed(ActionEvent ae) {
		   
		    		JPanel contentPane = (JPanel) frmScryptV.getContentPane();
		    		INIfileChoosER panel = new INIfileChoosER(currentConfiguration, DEFAULT_INI_FOLDER);
			    	panel.setBounds(20,20,frmScryptV.getWidth(),frmScryptV.getHeight()/2);
		    }
		});

		// NOT OPENING VIEW - SAVE CUSTOM CONFIGURATION
		savePrjSub = new JMenuItem(new AbstractAction("Export Current Configuration") {
		    public void actionPerformed(ActionEvent ae) {
		    	JPanel contentPane = (JPanel) frmScryptV.getContentPane();
		    	INIfileChoosER panel = new INIfileChoosER(currentConfiguration);
		    	panel.setBounds(20,20,frmScryptV.getWidth(),frmScryptV.getHeight()/2);  	
		    }
		});
		
		// NOT OPENING VIEW - RELOAD CONFIGURATION
		loadDefaultPrjSub = new JMenuItem(new AbstractAction("Reload Configuration") {
		    public void actionPerformed(ActionEvent ae) {

		    	if(JOptionPane.showConfirmDialog(frmScryptV, "Really want to load default config(unsaved changes will be lost)? ","Warning - Reload INI default config?",JOptionPane.YES_NO_OPTION)==JOptionPane.OK_OPTION) {
			    	try {
						currentConfiguration.setIniReadFile(new Ini(new File(DEFAULT_INI)));
						currentConfiguration.setIniFile(new Wini(new File(DEFAULT_INI)));
						JOptionPane.showMessageDialog(frmScryptV,"Configuration reloaded!");
					} catch (IOException e) {
						JOptionPane.showMessageDialog(frmScryptV,"Error opening: "+DEFAULT_INI+" -> maybe it does not exist?");
						logger.error("Exception happened!", e);
					}
			    	currentConfiguration.readConfigIniFile();
			    	JOptionPane.showMessageDialog(frmScryptV,"File '"+ currentConfiguration.getIniFile().getFile().getName()+"' loaded!");
			    }else {
					JOptionPane.showMessageDialog(frmScryptV, "Configuration not loaded, current CONFIG mantained!");
				}
		    }
		});
		
		// NOT OPENING VIEW - SAVE DEFAULT CONFIGURATION
		mntmSaveCurrentAs = new JMenuItem(new AbstractAction("Save Current as Default") {
			public void actionPerformed(ActionEvent e) {
				try {	
					currentConfiguration.setIniFile(new Wini(new File(DEFAULT_INI)));
					currentConfiguration.setIniReadFile(new Ini(new File(DEFAULT_INI)));

				} catch (IOException e1) {
					logger.error("Exception happened!", e1);
				}
				if(JOptionPane.showConfirmDialog(frmScryptV, "Really want to overwrite default configuration?","Warning - Overwrite default configuration?",JOptionPane.YES_NO_OPTION)==JOptionPane.OK_OPTION) {
					currentConfiguration.writeCurrentConfigIniFile();
					JOptionPane.showMessageDialog(frmScryptV, "File "+currentConfiguration.getIniFile().getFile().getAbsolutePath()+" saved!");
				}else {
					JOptionPane.showMessageDialog(frmScryptV, "Configuration not saved!");
				}
			}
		});
		
		// NOT OPENING VIEW - RELOAD STANDARD SCRYPT CONFIGURATION
		loadSrcrYptDefaultConf = new JMenuItem(new AbstractAction("Reset Original") {
		    public void actionPerformed(ActionEvent ae) {

		    	if(JOptionPane.showConfirmDialog(frmScryptV, "Really want to Reset standard ScrypT configuration? Unsaved changes will be lost! ","Warning - Reset INI standard config?",JOptionPane.YES_NO_OPTION)==JOptionPane.OK_OPTION) {
			    	try {
			    		FileUtils.copyFile(new File(BACKUP_SW_INI), new File(DEFAULT_INI));
						currentConfiguration.setIniReadFile(new Ini(new File(DEFAULT_INI)));
						currentConfiguration.setIniFile(new Wini(new File(DEFAULT_INI)));
						askUsername();
						
					} catch (IOException e) {
						JOptionPane.showMessageDialog(frmScryptV,"Error resetting standard configuration: it does not exist or IO error occurred, retry later!");
						logger.error("Exception happened!", e);
					}
			    	currentConfiguration.readConfigIniFile();
			    	JOptionPane.showMessageDialog(frmScryptV,"File '"+ currentConfiguration.getIniFile().getFile().getName()+"' loaded!");
			    }else {
					JOptionPane.showMessageDialog(frmScryptV, "Configuration not loaded, current CONFIG mantained!");
				}
		    }
		});
		
		// NOT OPENING VIEW - LOG EXPORT
		mntmExportLog = new JMenuItem(new AbstractAction("Export Event Log") {
			public void actionPerformed(ActionEvent e) {
				// show input dialog to select the file(gives null in case of no file selected)
				File selected = fileLogWriteDialog(frmScryptV);		
				if (selected!=null) {
				   	logger.debug("\n\n##################################################\n\n"+now.toString()+"\n\n"
							+ APPLICATION_NAME+" EXPORT!\n\n"
							+ "##################################################\n\n");
			  	
					// give true to overwrite because fileWriteDialog already set the value to be unique
					if (FileStuff.copyFile(new File(LOG_PATH), selected ,true)) {
						JOptionPane.showMessageDialog(frmScryptV, "File correctly saved in " + selected,
								"Log File Exported :)", JOptionPane.INFORMATION_MESSAGE);
						logger.debug("File :" + LOG_PATH + " Exported, exportFile: " + selected);
					} else {
						JOptionPane.showMessageDialog(frmScryptV, "File not saved [" + selected,
								"]  Some error occurred :(", JOptionPane.ERROR_MESSAGE);
						logger.error("File not saved, some error occurred :( ");
					}
				}
			}
		});
		
		// NOT OPENING VIEW - UPPERCASE DIRECTORY'S NAMES FIRST LETTER
		mntmTouppercaseDirectory = new JMenuItem(new AbstractAction("Uppercase First letter - Files&Directories") {
			public void actionPerformed(ActionEvent e) {
				last_dir_used = DirectoryManagementGUI.renameFirstLetterDirNames(frmScryptV, last_dir_used);
			}
		});
		
///////////////// HANDLERS THAT OPEN A NEW WINDOW  ////////////////////////
		
		// open in a new window - CONFIGURATION MANAGER
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
					panelGeneralOptions.requestFocus();
				}
			}
		});
		
		// open in a new window - KEYGEN
		generateKeyImageSub = new JMenuItem(new AbstractAction("Generate Key Image") {
		    public void actionPerformed(ActionEvent ae) {
		    	if (!flagFrameKeyGen) {
		    		flagFrameKeyGen = true;
		    		panelKeyGen = new KeyGenPanel(currentConfiguration);
					panelKeyGen.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					panelKeyGen.setBounds(150, 150, 1100, 800);
					panelKeyGen.setVisible(true);
					panelKeyGen.setTitle("IMG Generator (V1.1) - A.M.2018");
				}else {
					panelKeyGen.requestFocus();
				}
		    }
		});
		
		// open in a new window - NET INFO
		netInfo = new JMenuItem(new AbstractAction("Net Info") {
		    public void actionPerformed(ActionEvent ae) {
		    	if (!flagInfoPanel) {
		    		flagInfoPanel = true;
		    		netInfoPanel = new NetInfoPanel(currentConfiguration);
		    		netInfoPanel.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		    		netInfoPanel.setBounds(150, 150, 1100, 800);
		    		netInfoPanel.setVisible(true);
		    		netInfoPanel.setTitle("Network info - ScrYpT");
				}else {
					netInfoPanel.requestFocus();
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
		    		panelImgConv.setVisible(true);
		    		panelImgConv.setTitle("IMG Generator (V1.1) - A.M.2018");
				}else {
					panelImgConv.requestFocus();
				}
			}
		});
		
		// open in a new window - ABOUT SCRYPT
		mntmAboutScreept = new JMenuItem(new AbstractAction("About - ScrYpt") {
			public void actionPerformed(ActionEvent e) {
				// TODO
			}
		});
		
		// open in a new window - MANUAL
		mntmUserDocumentation = new JMenuItem(new AbstractAction("User Guide") {
			public void actionPerformed(ActionEvent e) {
				// TODO
			}
		});
		
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
					configManagerPanel = new ChargeIniLog(currentConfiguration, frmScryptV);
				}
				contentPane.removeAll();
		    	contentPane.add(configManagerPanel);
		    	contentPane.revalidate(); 
		    	contentPane.repaint();
		    }
		});
		
		// open in main frame - TEXT EDITOR
		mntmNewTextFile = new JMenuItem(new AbstractAction("Text Editor") {
			public void actionPerformed(ActionEvent e) {
			
				JPanel contentPane = (JPanel) frmScryptV.getContentPane();
				if(initTxtWriter == null) {
					initTxtWriter = new TextFileWriter(frmScryptV, currentConfiguration);
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

		
////////// END OF HANDLERS DECLARATION  ////////////////
		
		// once each component is declared add it to the menu bar
		structureMenuBar();
	}
	
	private void structureMenuBar() {
		
		// INITIALIZE FONTS BY TYPE
		Font menuBarFont = new Font("Segoe UI", Font.BOLD, menuSize);
		Font subMenuFont = new Font("Gadugi", Font.BOLD, menuSize);
		Font menuItemFont = new Font("Segoe UI", Font.PLAIN, subMenuSize);
		
		// CREATE MENU STRUCTURE
		menuBar.add(mnFile);
		menuBar.add(mnPreferences);
		menuBar.add(mnCryptoSteg);
		menuBar.add(mnVarious);
		menuBar.add(mnHelp);
		
		mnFile.add(mntmInspectCurrentConfiguration);
		mnFile.add(new JSeparator());
		mnFile.add(loadPrjSub);
		mnFile.add(savePrjSub);
		mnFile.add(new JSeparator());
		mnFile.add(loadDefaultPrjSub);
		mnFile.add(mntmSaveCurrentAs);
		mnFile.add(new JSeparator());
		mnFile.add(loadSrcrYptDefaultConf);
		mnFile.add(new JSeparator());
		mnFile.add(mntmExit);
		
		mnPreferences.add(mntmCustomizeUIView);
		mnPreferences.add(mntmGenOpt);
		
		mnCryptoSteg.add(mntmCrypter);
		mnCryptoSteg.add(new JSeparator());
		mnCryptoSteg.add(generateKeyImageSub);
		mnCryptoSteg.add(mntmViewKeyDictionary);

		mnVarious.add(mntmNewTextFile);
		mnVarious.add(new JSeparator());
		mnVarious.add(netInfo);
		mnVarious.add(mnConversionsAndOther);
		mnVarious.add(mnFilesAndDirectories);

		mnConversionsAndOther.add(mntmImageConverter);
		mnFilesAndDirectories.add(mntmTouppercaseDirectory);
		
		mnHelp.add(mntmAboutScreept);		
		mnHelp.add(new JSeparator());
		mnHelp.add(mntmExportLog);
		mnHelp.add(mntmUserDocumentation);
		mnHelp.add(new JSeparator());
		mnHelp.add(mntmReturnToWelcome);
		
		// SET FONTS
		menuBar.setFont(menuBarFont);
		
		mnFile.setFont(subMenuFont);
		mnHelp.setFont(subMenuFont);
		mnPreferences.setFont(subMenuFont);
		mnCryptoSteg.setFont(subMenuFont);
		mnVarious.setFont(subMenuFont);

		mnFilesAndDirectories.setFont(menuItemFont);
		mnConversionsAndOther.setFont(menuItemFont);

		mntmExit.setFont(menuItemFont);
		mntmInspectCurrentConfiguration.setFont(menuItemFont);
		loadPrjSub.setFont(menuItemFont);
		savePrjSub.setFont(menuItemFont);
		loadDefaultPrjSub.setFont(menuItemFont);
		mntmSaveCurrentAs.setFont(menuItemFont);
		loadSrcrYptDefaultConf.setFont(menuItemFont);
		mntmCustomizeUIView.setFont(menuItemFont);
		mntmGenOpt.setFont(menuItemFont);
		mntmCrypter.setFont(menuItemFont);
		generateKeyImageSub.setFont(menuItemFont);
		netInfo.setFont(menuItemFont);
		mntmViewKeyDictionary.setFont(menuItemFont);
		mntmNewTextFile.setFont(menuItemFont);
		mntmImageConverter.setFont(menuItemFont);
		mntmTouppercaseDirectory.setFont(menuItemFont);
		mntmReturnToWelcome.setFont(menuItemFont);
		mntmAboutScreept.setFont(menuItemFont);
		mntmExportLog.setFont(menuItemFont);
		mntmUserDocumentation.setFont(menuItemFont);
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
	
	public void askUsername() {
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
				currentConfiguration.getGenOpt().setUserName(input);
				currentConfiguration.writeSingleValue(currentConfiguration.GEN_OPT, "userName", input);
				end = true;
			} 
		} while (!end);
		logger.debug("Name selected and saved username: "+input);
	}
	
	public void setBackgroundImage() {
		// setto lo sfondo iniziale dell'app
		try {
			JPanel MAIN_PANEL = new JPanel();
			MAIN_PANEL.setBackground(SystemColor.infoText);
            frmScryptV.getContentPane().add(MAIN_PANEL);
            MAIN_PANEL.setLayout(new MigLayout("", "[90%]", "[1.00][100.52%,baseline]"));
            BufferedImage image = ImageIO.read(new File(RESOURCES_DIR + APPLICATION_MAIN_BCKG));
            Image scaledImage = image.getScaledInstance(screenWidth, screenHeight,Image.SCALE_REPLICATE);
            JLabel label = new JLabel(new ImageIcon(scaledImage));
            
		   	JPanel contentPane = (JPanel) frmScryptV.getContentPane();
		   	contentPane.removeAll();
	    	contentPane.add(label);
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
		SimpleDateFormat df = new SimpleDateFormat("yyyy_MM_dd_@_HH-MM-ss");
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
	
	//TODO function to update graphic and current conf usage values on the fly after load
	public static void updateMainGUI() {
		// TODO
	}
}

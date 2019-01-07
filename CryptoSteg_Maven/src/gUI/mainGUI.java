package gUI;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.FlatteningPathIterator;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;

import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JMenu;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;

import javax.swing.JFileChooser;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.ini4j.Ini;
import org.ini4j.Wini;

import gUI.panels.ChargeIniLog;
import gUI.panels.CrypterPanel;
import gUI.panels.CryptoStegTest_Gui_V2_0;
import gUI.panels.GeneralOptionsFrame;
import gUI.panels.KeyGenPanel;
import gUI.panels.TextFileWriter;
import gUI.utils.filedialog.INIfileChoosER;
import gUI.panels.GraphicPreferencesFrame;
import gUI.panels.ImageConverterFrame;
import gUI.panels.KeyDictionaryPanel;
import iNIT.INItializer;

import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
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
import javax.swing.UIDefaults;

public class mainGUI {

	// mainGUI logger
	static Logger logger = Logger.getLogger(mainGUI.class);

	// autosave constant refresh period in millisecond
	private static final long AUTOSAVE_MANAGER_PERIOD=20000;
	
	// AUTOSAVE MODULE - only if specified in fileOptions
	private static Thread autosaveModule;
	
	// the name of the application used elsewhere on the sw
	private static final String APPLICATION_NAME = "ScrYpt_V1.0";
	
	// constant files & directories prefix
	private static final String RESOURCES_DIR="Resources_Scrypt/";
	private static final String DEFAULT_INI="Resources_Scrypt/CONFIG.ini";
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
	// frames that open in new dialog
	private static GraphicPreferencesFrame panelGuiOptions;
	private static KeyGenPanel panelKeyGen;
	private static KeyDictionaryPanel panelKeyDict;
	private static GeneralOptionsFrame panelGeneralOptions;
	private static ImageConverterFrame panelImgConv;
	
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
	
	// panel representing text-editor
	private TextFileWriter initTxtWriter;
	
	// panel representing the file Crypter
	private JPanel panelCrypter;
	
	// FLAGS TO SIGNAL THAT A MODULE IS OPENED (JDIALOG) 
	// for jpanels they refresh only the first time opened or if manually refreshed (TODO)
	private static boolean flagFrameKeyGen=false;
	private static boolean flagFrameGenOptions=false;
	private static boolean flagFrameGuiOptions=false;
	private static boolean flagFrameKeyDict=false;	
	private static boolean flagImgConv=false;
	
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
						logger.error("Error during loading of file [Resources_Scrypt/CONFIG.ini]");
					}
					
					// initialize style
					preferredStyle = currentConfiguration.getGuiOpt().getPreferredStyle();
					
					// initialize GUI default Style from ini
					try {
						UIManager.setLookAndFeel(preferredStyle);
						logger.debug(preferredStyle+" initialized correctly.");
					} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
							| UnsupportedLookAndFeelException e3) {
						logger.warn(preferredStyle+" not loaded, some error occurred.");
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
								} catch (InterruptedException e) {
							        logger.warn("## ## MODULES FLAG UPDATER interrupted.");
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
										logger.error("An error occurred during the saving :(");
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
									logger.error("An error occurred during the saving :(");
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
					logger.error("Error initializing GUI: "+e.getMessage());
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
		last_dir_used="INI_FILES";
		
		// INITIALIZE MAIN_GUI (See the initialize() function below this constructor)
		initialize();
		logger.debug("## GUI components started");
		
		// SW window icon
		try {
			frmScryptV.setIconImage(ImageIO.read(new File(RESOURCES_DIR + "icon.jpg")));
			logger.debug("icon image setted to main window");
		} catch (IOException e1) {
			logger.debug("Error setting main window icon image :(");
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

		// if not specified name in config then prompt
		if(currentConfiguration.getGenOpt().getUserName().equals("") || currentConfiguration.getGenOpt().getUserName().length()>20) {
			boolean end=false;
			String input="";
			logger.debug("Current user input initialization: void user or lenght>20 detected!");
			do {
				input = JOptionPane.showInputDialog("Please insert your name:", "U-sir");
				if (input.length() > 20) {
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
		
		// inizialize values with screen width
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		screenWidth = gd.getDisplayMode().getWidth();
		screenHeight = gd.getDisplayMode().getHeight();
				
		menuSize = currentConfiguration.getGuiOpt().getMenuFontSize();
		subMenuSize = menuSize-2;
		
		frmScryptV = new JFrame();
		frmScryptV.setModalExclusionType(ModalExclusionType.TOOLKIT_EXCLUDE);
		frmScryptV.getContentPane().setBackground(SystemColor.windowText);
		frmScryptV.setBounds(0, 0, 1200, 860 );
		
		//Istruzione per schermo intero
	    frmScryptV.setExtendedState(JFrame.MAXIMIZED_BOTH);
		
		frmScryptV.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JMenuBar menuBar = new JMenuBar();
//		menuBar.setBackground(Color.DARK_GRAY);
		menuBar.setFont(new Font("Segoe UI", Font.BOLD, menuSize));
		menuBar.setOpaque(true);
		frmScryptV.setJMenuBar(menuBar);
		
		// setto lo sfondo iniziale dell'app
		try {
			JPanel MAIN_PANEL = new JPanel();
			MAIN_PANEL.setBackground(SystemColor.infoText);
            frmScryptV.getContentPane().add(MAIN_PANEL);
            MAIN_PANEL.setLayout(new MigLayout("", "[90%]", "[1.00][100.52%,baseline]"));
            BufferedImage image = ImageIO.read(new File(RESOURCES_DIR + "Logo-AM_v2.jpg"));
            //Image scaledImage = image.getScaledInstance(screenWidth, screenHeight,Image.SCALE_FAST);
            JLabel label = new JLabel(new ImageIcon(image));
            //MAIN_PANEL.add(label, "cell 0 1,grow");
            
		   	JPanel contentPane = (JPanel) frmScryptV.getContentPane();
		   	contentPane.removeAll();
	    	contentPane.add(label);
	    	contentPane.revalidate(); 
	    	contentPane.repaint();
			logger.debug("Background of mainGUI setted: "+RESOURCES_DIR + "Logo-AM_v2.jpg");

        } catch (IOException e2) {
            logger.error("Background - mainGUI: error : "+e2.toString());
        }

        frmScryptV.setVisible(true);
		
		JMenu mnFile = new JMenu("File");
		mnFile.setBackground(SystemColor.menu);
		//mnFile.setForeground(Color.BLACK);
		mnFile.setFont(new Font("Gadugi", Font.BOLD, menuSize));
		menuBar.add(mnFile);
		//UIManager.put("PopupMenu.border", new LineBorder(Color.gray));
		
		JMenuItem mntmExit = new JMenuItem("Exit");
		mntmExit.setFont(new Font("Segoe UI", Font.PLAIN, subMenuSize));
		mntmExit.addActionListener(new ActionListener() {
			
			@Override
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
		
		JMenuItem mntmInspectCurrentConfiguration = new JMenuItem(new AbstractAction("Current Configuration") {
		    public void actionPerformed(ActionEvent ae) {
				   
	    		JPanel contentPane = (JPanel) frmScryptV.getContentPane();
		    	ChargeIniLog init = new ChargeIniLog(currentConfiguration, frmScryptV);
		    	contentPane.removeAll();
		    	contentPane.add(init);
		    	contentPane.revalidate(); 
		    	contentPane.repaint();
//		    	currentConfiguration.readConfigIniFile();	-> cause of a bug because of cuncurrency	    	
	    }
	});
		mntmInspectCurrentConfiguration.setText("Configuration Manager");
		mntmInspectCurrentConfiguration.setFont(new Font("Segoe UI", Font.PLAIN, subMenuSize));
		mnFile.add(mntmInspectCurrentConfiguration);
		
		JMenuItem loadPrjSub = new JMenuItem(new AbstractAction("Load Configuration") {
		    public void actionPerformed(ActionEvent ae) {
		   
		    		JPanel contentPane = (JPanel) frmScryptV.getContentPane();
		    		INIfileChoosER panel = new INIfileChoosER(currentConfiguration, DEFAULT_INI_FOLDER);
			    	panel.setBounds(20,20,frmScryptV.getWidth(),frmScryptV.getHeight()/2);
//			    	currentConfiguration.readConfigIniFile();	-> cause of a bug because of cuncurrency	    	
		    }
		});
		mnFile.add(loadPrjSub);
		loadPrjSub.setFont(new Font("Segoe UI", Font.PLAIN, subMenuSize));
		
		JMenuItem savePrjSub = new JMenuItem(new AbstractAction("Save Configuration") {
		    public void actionPerformed(ActionEvent ae) {
		    	JPanel contentPane = (JPanel) frmScryptV.getContentPane();
		    	INIfileChoosER panel = new INIfileChoosER(currentConfiguration);
		    	panel.setBounds(20,20,frmScryptV.getWidth(),frmScryptV.getHeight()/2);  	
				//config_PATH.setText(currentConfiguration.getIniFile().getFile().getAbsolutePath());
		    }
		});
		mnFile.add(savePrjSub);
		savePrjSub.setFont(new Font("Segoe UI", Font.PLAIN, subMenuSize));
		
		JSeparator separator_1 = new JSeparator();
		mnFile.add(separator_1);
		
		JMenuItem loadDefaultPrjSub = new JMenuItem(new AbstractAction("Load Default Configuration") {
		    public void actionPerformed(ActionEvent ae) {

		    	if(JOptionPane.showConfirmDialog(frmScryptV, "Really want to load default config(unsaved changes will be lost)? ","Warning - Load INI standard config?",JOptionPane.YES_NO_OPTION)==JOptionPane.OK_OPTION) {
			    	try {
						currentConfiguration.setIniReadFile(new Ini(new File(DEFAULT_INI)));
						currentConfiguration.setIniFile(new Wini(new File(DEFAULT_INI)));
					} catch (IOException e) {
						JOptionPane.showMessageDialog(frmScryptV,"Error opening: "+DEFAULT_INI+" -> maybe it does not exist?");
					}
			    	currentConfiguration.readConfigIniFile();
			    	JOptionPane.showMessageDialog(frmScryptV,"File '"+ currentConfiguration.getIniFile().getFile().getName()+"' loaded!");
			    }else {
					JOptionPane.showMessageDialog(frmScryptV, "File not loaded, current CONFIG mantained!");
				}
		    }
		});
		mnFile.add(loadDefaultPrjSub);
		loadDefaultPrjSub.setText("Load Default Configuration");
		loadDefaultPrjSub.setFont(new Font("Segoe UI", Font.PLAIN, subMenuSize));
		
		JMenuItem mntmSaveCurrentAs = new JMenuItem("Save Current as Default");
		mnFile.add(mntmSaveCurrentAs);
		mntmSaveCurrentAs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {	
					currentConfiguration.setIniFile(new Wini(new File(DEFAULT_INI)));
					currentConfiguration.setIniReadFile(new Ini(new File(DEFAULT_INI)));

				} catch (IOException e1) {
					e1.printStackTrace();
				}
				if(JOptionPane.showConfirmDialog(frmScryptV, "Really want to overwrite default config(standard CONFIG.ini will be lost)? ","Warning - Overwrite INI standard config?",JOptionPane.YES_NO_OPTION)==JOptionPane.OK_OPTION) {
					currentConfiguration.writeCurrentConfigIniFile();
					JOptionPane.showMessageDialog(frmScryptV, "File "+currentConfiguration.getIniFile().getFile().getAbsolutePath()+" saved!");
				}else {
					JOptionPane.showMessageDialog(frmScryptV, "File not saved!");
				}
			}
		});
		mntmSaveCurrentAs.setFont(new Font("Segoe UI", Font.PLAIN, subMenuSize));
		
		JSeparator separator_2 = new JSeparator();
		mnFile.add(separator_2);
		mnFile.add(mntmExit);
	
		
		JMenu mnPreferences = new JMenu("Preferences");
		mnPreferences.setBackground(SystemColor.menu);
		//mnPreferences.setForeground(Color.BLACK);
		mnPreferences.setFont(new Font("Gadugi", Font.BOLD, menuSize));
		menuBar.add(mnPreferences);
		
		JMenuItem mntmCustomizeUIView = new JMenuItem("Customize UI View");
		mntmCustomizeUIView.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!flagFrameGuiOptions) {
					flagFrameGuiOptions = true;
					panelGuiOptions = new GraphicPreferencesFrame(currentConfiguration);
					panelGuiOptions.setBounds(500, 300, 950, 330);
					panelGuiOptions.setVisible(true);
//					panelGuiOptions.setAlwaysOnTop(true);
					panelGuiOptions.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					panelGuiOptions.setTitle("Apply/Save Preferences [Customize - Apply - Save]");
				}else {
					panelGuiOptions.requestFocus();
				}
			}
		});
		mntmCustomizeUIView.setFont(new Font("Segoe UI", Font.PLAIN, subMenuSize));
		mnPreferences.add(mntmCustomizeUIView);
		
		JMenuItem mntmCrypto = new JMenuItem("General Options");
		mntmCrypto.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!flagFrameGenOptions) {
					flagFrameGenOptions=true;
					panelGeneralOptions = new GeneralOptionsFrame(currentConfiguration);
					//bounds setted in constructor GraphicPreferencesFrame();
					panelGeneralOptions.setBounds(400, 300, 700, 250);
					panelGeneralOptions.setVisible(true);
//					panelGeneralOptions.setAlwaysOnTop(true);
					panelGeneralOptions.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					panelGeneralOptions.setTitle("Apply/Save General Options [Customize - Apply - Save]");
				}else {
					panelGeneralOptions.requestFocus();
				}
			}
		});
		mntmCrypto.setFont(new Font("Segoe UI", Font.PLAIN, subMenuSize));
		mnPreferences.add(mntmCrypto);
		
		JMenu mnCryptoSteg = new JMenu("ScreepT");
		mnCryptoSteg.setBackground(SystemColor.menu);
		//mnCryptoSteg.setForeground(Color.BLACK);
		mnCryptoSteg.setFont(new Font("Gadugi", Font.BOLD, menuSize));
		menuBar.add(mnCryptoSteg);
		
		JMenuItem mntmCrypter = new JMenuItem(new AbstractAction("File Crypter") {
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
		mntmCrypter.setText("ScreepT - Crypt and Decrypt files");
		mntmCrypter.setFont(new Font("Segoe UI", Font.PLAIN, subMenuSize));
		mnCryptoSteg.add(mntmCrypter);
		
		JSeparator separator_3 = new JSeparator();
		mnCryptoSteg.add(separator_3);
		
//		// esempio per embed in finestra principale
//		
//		JMenuItem loadKeyImageSub = new JMenuItem(new AbstractAction("Load Key Image") {
//		    public void actionPerformed(ActionEvent ae) {
//		    	
//		    }
//		});
//		mnCryptoSteg.add(loadKeyImageSub);
//		loadKeyImageSub.setFont(new Font("Segoe UI", Font.PLAIN, subMenuSize));
//		
		// open in a new window
		JMenuItem generateKeyImageSub = new JMenuItem(new AbstractAction("Generate Key Image") {
		    public void actionPerformed(ActionEvent ae) {
		    	if (!flagFrameKeyGen) {
		    		flagFrameKeyGen = true;
		    		panelKeyGen = new KeyGenPanel(currentConfiguration);
//					panelKeyGen.setAlwaysOnTop(true);
					panelKeyGen.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					panelKeyGen.setBounds(150, 150, 1100, 800);
					panelKeyGen.setVisible(true);
					panelKeyGen.setTitle("IMG Generator (V1.1) - A.M.2018");
					//panel.setAlwaysOnTop(true);
					//panel.setSize(frmScryptV.getWidth(),frmScryptV.getHeight());
				}else {
					panelKeyGen.requestFocus();
				}
		    }
		});
		mnCryptoSteg.add(generateKeyImageSub);
		generateKeyImageSub.setFont(new Font("Segoe UI", Font.PLAIN, subMenuSize));
		
		// open in a new window - DICTIONARY (KEY IMG)
		JMenuItem mntmViewKeyDictionary = new JMenuItem(new AbstractAction("View Key Dictionary") {
		    public void actionPerformed(ActionEvent e) {
		    	if (!flagFrameKeyDict) {
		    		flagFrameKeyDict = true;
		    		panelKeyDict = new KeyDictionaryPanel(currentConfiguration);
		    		panelKeyDict.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		    		panelKeyDict.setBounds(300, 100, 800, 560);
//		    		panelKeyDict.setAlwaysOnTop(true);
		    		panelKeyDict.setVisible(true);
		    		panelKeyDict.setTitle("Key Dictionary Manager");
				}else {
					panelKeyDict.requestFocus();
				}
			}
		});
		mnCryptoSteg.add(mntmViewKeyDictionary);
		mntmViewKeyDictionary.setFont(new Font("Segoe UI", Font.PLAIN, subMenuSize));
		
		JMenu mnVarious = new JMenu("Various");
		mnVarious.setBackground(SystemColor.menu);
		//mnVarious.setForeground(Color.BLACK);
		mnVarious.setFont(new Font("Gadugi", Font.BOLD, menuSize));
		menuBar.add(mnVarious);
		
		JMenuItem mntmNewTextFile = new JMenuItem("Text Editor");
		mnVarious.add(mntmNewTextFile);
		mntmNewTextFile.addActionListener(new ActionListener() {
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
		mntmNewTextFile.setFont(new Font("Segoe UI", Font.PLAIN, subMenuSize));
		
		JSeparator separator_4 = new JSeparator();
		mnVarious.add(separator_4);
		
		JMenu mnConversionsAndOther = new JMenu("Conversions and Media");
		mnConversionsAndOther.setFont(new Font("Segoe UI", Font.PLAIN, subMenuSize));
		mnVarious.add(mnConversionsAndOther);
		
		JMenuItem mntmSimpleCryptdecriptaes = new JMenuItem("Simple Text Crypt/Decript (AES)");
		mnConversionsAndOther.add(mntmSimpleCryptdecriptaes);
		mntmSimpleCryptdecriptaes.setFont(new Font("Segoe UI", Font.PLAIN, subMenuSize));
		mntmSimpleCryptdecriptaes.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CryptoStegTest_Gui_V2_0 runner = new CryptoStegTest_Gui_V2_0();
		    	runner.getFrame().setVisible(true);
//		    	runner.setAlwaysOnTop(true);
		    	runner.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			}
		});
		
		JMenuItem mntmImageConverter = new JMenuItem("Image Converter");
		mntmImageConverter.setFont(new Font("Segoe UI", Font.PLAIN, subMenuSize));
		mnConversionsAndOther.add(mntmImageConverter);
		mntmImageConverter.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
		    	if (!flagImgConv) {
		    		flagImgConv = true;
		    		panelImgConv = new ImageConverterFrame(currentConfiguration);
//		    		panelImgConv.setAlwaysOnTop(true);
		    		panelImgConv.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		    		panelImgConv.setBounds(700, 200, 650, 500);
		    		panelImgConv.setVisible(true);
		    		panelImgConv.setTitle("IMG Generator (V1.1) - A.M.2018");
					//panel.setAlwaysOnTop(true);
					//panel.setSize(frmScryptV.getWidth(),frmScryptV.getHeight());
				}else {
					panelImgConv.requestFocus();
				}
			}
		});
		
		JMenu mnFilesAndDirectories = new JMenu("Files And Directories");
		mnFilesAndDirectories.setFont(new Font("Segoe UI", Font.PLAIN, subMenuSize));
		mnVarious.add(mnFilesAndDirectories);
		
		// funzionalità per applicare la prima lettera magiuscola a directory e al suo contenuto
		JMenuItem mntmTouppercaseDirectory = new JMenuItem("Uppercase First letter - Files&Directories");
		mnFilesAndDirectories.add(mntmTouppercaseDirectory);
		mntmTouppercaseDirectory.setFont(new Font("Segoe UI", Font.PLAIN, subMenuSize));
		mntmTouppercaseDirectory.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int errors=0;
				File selectedFile;
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setDialogTitle("Select source dir/file to make uppercase");
				fileChooser.setCurrentDirectory(new File(last_dir_used));
				fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				int returnVal = fileChooser.showOpenDialog(frmScryptV);
				if (returnVal == JFileChooser.APPROVE_OPTION && fileChooser.getSelectedFile()!=null) {
		            try {
		            	selectedFile = fileChooser.getSelectedFile();
		            	if(selectedFile.isFile()) {
		            		if(JOptionPane.showConfirmDialog(fileChooser, "Are you sure to rename file: '"+selectedFile.getName()+
			            			"'?")==JOptionPane.OK_OPTION) {
		            			last_dir_used = selectedFile.getParentFile().getAbsolutePath();
				            	errors=FileStuff.upperCaseSubFolder(selectedFile);
				            	JOptionPane.showMessageDialog(frmScryptV, "Renaming completed! \n\nCompleted with: "+errors+" Errors.");
		            		}
		            	}//END FILE
		            	else if(selectedFile.isDirectory()) {
		            		if(JOptionPane.showConfirmDialog(fileChooser, "Are you sure to rename folder: '"+selectedFile.getName()+
			            			"' and all files/directory in it?")==JOptionPane.OK_OPTION) {
		            			last_dir_used = selectedFile.getAbsolutePath();
				            	errors=FileStuff.upperCaseSubFolder(selectedFile);
				            	JOptionPane.showMessageDialog(frmScryptV, "Renaming completed! \n\nCompleted with: "+errors+" Errors.");
		            		}	
		            	}// END DIRECTORY
		            	if(!selectedFile.exists()) {
			            	JOptionPane.showMessageDialog(frmScryptV, "File does not exist! \nChoose an existing File or Directory ;)", "Error Executin' requested Action", JOptionPane.WARNING_MESSAGE);
		            	}
		            }catch(Exception exc) {
						JOptionPane.showMessageDialog(frmScryptV, "Renaming cancelled! \n An error occurred.", "Error Executin' requested Action", JOptionPane.ERROR_MESSAGE);
		            }
				}else {
					JOptionPane.showMessageDialog(frmScryptV, "Renaming cancelled! ", "Skipped by user", JOptionPane.INFORMATION_MESSAGE);
				}
			}
		});
		
		JMenu mnNetworkUtils = new JMenu("Network Utils");
		mnNetworkUtils.setFont(new Font("Segoe UI", Font.PLAIN, subMenuSize));
		mnVarious.add(mnNetworkUtils);
		
		JMenu mnHelp = new JMenu("Help");
		mnHelp.setBackground(SystemColor.menu);
		//mnHelp.setForeground(Color.BLACK);
		mnHelp.setFont(new Font("Gadugi", Font.BOLD, menuSize));
		menuBar.add(mnHelp);
		
		JMenuItem mntmReturnToWelcome = new JMenuItem("Return to Welcome Page");
		mntmReturnToWelcome.setFont(new Font("Segoe UI", Font.PLAIN, subMenuSize));
		mntmReturnToWelcome.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				// setto lo sfondo iniziale dell'app
				try {
					JPanel MAIN_PANEL = new JPanel();
					MAIN_PANEL.setBackground(SystemColor.infoText);
		            frmScryptV.getContentPane().add(MAIN_PANEL);
		            MAIN_PANEL.setLayout(new MigLayout("", "[90%]", "[60.00][100.52%,baseline]"));
		            JLabel label = new JLabel(new ImageIcon(ImageIO.read(new File(RESOURCES_DIR + "Logo-AM_v2.jpg"))));
		            MAIN_PANEL.add(label, "cell 0 1,grow");
		            
				   	JPanel contentPane = (JPanel) frmScryptV.getContentPane();
				   	contentPane.removeAll();
			    	contentPane.add(label);
			    	contentPane.revalidate(); 
			    	contentPane.repaint();
		            
		        } catch (IOException e2) {
		            e2.printStackTrace();
		        }
		        //frmScryptV.pack();
			}
		});
		
		JMenuItem mntmAboutScreept = new JMenuItem((Action) null);
		mntmAboutScreept.setText("About - ScrYpt");
		mntmAboutScreept.setFont(new Font("Segoe UI", Font.PLAIN, subMenuSize));
		mnHelp.add(mntmAboutScreept);
		
		JSeparator separator = new JSeparator();
		mnHelp.add(separator);
		
		JMenuItem mntmExportLog = new JMenuItem("Export Event Log");
		mntmExportLog.setFont(new Font("Segoe UI", Font.PLAIN, subMenuSize));
		mnHelp.add(mntmExportLog);
		mnHelp.add(mntmReturnToWelcome);
		mntmExportLog.addActionListener(new ActionListener() {
			
			@Override
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
		
		JMenuItem mntmUserDocumentation = new JMenuItem((Action) null);
		mntmUserDocumentation.setText("User Guide");
		mntmUserDocumentation.setFont(new Font("Segoe UI", Font.PLAIN, subMenuSize));
		mnHelp.add(mntmUserDocumentation);
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
		    e.printStackTrace();
		  } catch (InstantiationException e) {
		    e.printStackTrace();
		  } catch (IllegalAccessException e) {
		    e.printStackTrace();
		  } catch (javax.swing.UnsupportedLookAndFeelException e) {
		    e.printStackTrace();
		  } catch (Exception e) {
		    e.printStackTrace();
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
		    e.printStackTrace();
		  } catch (InstantiationException e) {
		    e.printStackTrace();
		  } catch (IllegalAccessException e) {
		    e.printStackTrace();
		  } catch (javax.swing.UnsupportedLookAndFeelException e) {
		    e.printStackTrace();
		  } catch (Exception e) {
		    e.printStackTrace();
		  }
		  // Show your JFrame
	}
	
	public void updateMenuAndLogFontSize(int fontSize){
		

		// use the same font as main gui but change font size
		UIManager.put("JMenuBar.font", new Font("Segoe UI", Font.BOLD, fontSize ));
		UIManager.put("JMenu.font", new Font("Segoe UI", Font.BOLD, fontSize ));
		UIManager.put("JMenuItem.font", new Font("Segoe UI", Font.BOLD, fontSize-2 ));

	//			Component[] panelElem = window.getComponents();
	//			for (Component c : panelElem) {
	//				if(c instanceof JMenu) {
	//					c.setFont(new Font("Segoe UI", Font.BOLD, MENU_FONT_SIZE ));
	//					c.revalidate();
	//					c.repaint();
	//				}
	//			}
	    SwingUtilities.updateComponentTreeUI(frmScryptV);
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

package gUI.panels;

import javax.swing.JFrame;
import net.miginfocom.swing.MigLayout;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JOptionPane;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.SystemColor;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.log4j.Logger;

import gUI.mainGUI;
import iNIT.INItializer;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import javax.imageio.ImageIO;
import javax.security.auth.callback.NameCallback;
import javax.swing.JButton;
import javax.swing.JSlider;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.JTextField;
import javax.swing.JCheckBox;

public class GraphicPreferencesFrame extends JFrame{
	
	// initialize Logger
	// GraphicPreferencesFrame logger
	static Logger logger = Logger.getLogger(GraphicPreferencesFrame.class); 
	
	// GUI elements
	private final JFrame thisFrame = this;
	private JTextField textFieldUname;
	private JSlider sliderMenuSize;
	private JSlider sliderLogSize;
	private JComboBox comboStyleLaF;
	private JCheckBox chckbxDarklight;
	
	// actual values
	private static String CURRENT_USERNAME;
	private static String PREFERRED_STYLE;
	private static int MENU_FONT_SIZE;
	private static int LOG_PANEL_FONT_SIZE;
	private static boolean DARK_THEME;
	
	// default values
	private static String BACKUP_CURRENT_USERNAME;
	private static String BACKUP_PREFERRED_STYLE;
	private static int BACKUP_MENU_FONT_SIZE;
	private static int BACKUP_LOG_PANEL_FONT_SIZE;
	private static boolean BACKUP_DARK_THEME;
	
	private mainGUI mainAPP;
	// flag to signal active
	public static boolean active = false;

	public GraphicPreferencesFrame(INItializer currentConf, mainGUI rootApplication) {
		
		active = true;
		
		// SW window icon
		try {
			this.setIconImage(ImageIO.read(new File(mainGUI.RESOURCES_DIR + mainGUI.ICON_FILE_NAME)));
			logger.debug("icon image setted to main window");
		} catch (IOException e1) {
			logger.error("Exception happened!", e1);
		}
		
		mainAPP = rootApplication;
		
		JFrame thisFrame = this;
		
		initValues(currentConf);
		
		logger.debug("Initialized.");
		
		//TODO initialize size (add to ini fields before)
		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		setBackground(new Color(0, 0, 0));
		getContentPane().setBackground(new Color(112, 128, 144));
		getContentPane().setLayout(new MigLayout("", "[:10px:100px,grow][:140px:180px][:80:80,grow][70][90][:10px:100px,grow][:100px:170px][:100px:100px][:75px:75px][:75px:75px][:10px:100px,grow]", "[:50:70px][5][][15][30][10][30][30][:5px:10px,grow][10%,grow,baseline]"));
		
		JPanel panel = new JPanel();
		panel.setForeground(SystemColor.controlText);
		panel.setBorder(new LineBorder(null, 4));
		panel.setBackground(new Color(211, 211, 211));
		getContentPane().add(panel, "cell 1 0 9 1,grow");
		panel.setLayout(new MigLayout("", "[][][][][][][][][][][][][][][][][][][grow 1000][][][][][]", "[100]"));
		
		chckbxDarklight = new JCheckBox("Nimbus Dark theme");
		chckbxDarklight.setFont(new Font("SansSerif", Font.BOLD, 13));
		panel.add(chckbxDarklight, "cell 0 0,growx");
		chckbxDarklight.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(!chckbxDarklight.isSelected()) {
					mainGUI.updateUILightNimbus();
					currentConf.getGuiOpt().setDarkGuiNimbus(false);
					DARK_THEME=false;
				}else {
					mainGUI.updateUIDarkNimbus();
					currentConf.getGuiOpt().setDarkGuiNimbus(true);
					DARK_THEME=true;
				}
				for(Window window: Window.getWindows()) {
	    		    SwingUtilities.updateComponentTreeUI(window);
	    		}
			}
		});
		if(DARK_THEME) {
			chckbxDarklight.setSelected(true);
		}
		
		JLabel lblGraphicInterfaceSettings = new JLabel("GUI   Preferences");
		panel.add(lblGraphicInterfaceSettings, "cell 21 0 3 1,alignx right");
		lblGraphicInterfaceSettings.setHorizontalAlignment(SwingConstants.RIGHT);
		lblGraphicInterfaceSettings.setForeground(new Color(0, 0, 0));
		lblGraphicInterfaceSettings.setFont(new Font("Lithos Pro Regular", Font.BOLD, 22));
		lblGraphicInterfaceSettings.setBackground(Color.LIGHT_GRAY);
		
		JSeparator separator = new JSeparator();
		getContentPane().add(separator, "cell 1 2 9 1,growx");
		
		JLabel lblGuiType = new JLabel("Select GUI type");
		lblGuiType.setFont(new Font("Tahoma", Font.BOLD, 13));
		getContentPane().add(lblGuiType, "cell 1 4,alignx center");
		
		comboStyleLaF = new JComboBox(UIManager.getInstalledLookAndFeels());
		comboStyleLaF.setMaximumRowCount(10);
		comboStyleLaF.setSelectedItem(PREFERRED_STYLE);
		getContentPane().add(comboStyleLaF, "cell 2 4 8 1,grow");
		
		// Initialize ComboBox values,
		comboStyleLaF.addActionListener (new ActionListener () {
		    public void actionPerformed(ActionEvent e) {
		    	try {
		    		LookAndFeelInfo laf = (LookAndFeelInfo)comboStyleLaF.getSelectedItem();		    		
		    		PREFERRED_STYLE = laf.getClassName();
		    		currentConf.getGuiOpt().setPreferredStyle(PREFERRED_STYLE); 
		    		UIManager.setLookAndFeel(laf.getClassName());
		    		for(Window window: Window.getWindows()) {
		    		    SwingUtilities.updateComponentTreeUI(window);
		    		}
//					thisFrame.getContentPane().revalidate();
//					thisFrame.getContentPane().repaint();
				} catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException e1) {
				JOptionPane.showMessageDialog(thisFrame, "Sorry, something went wrong :(", e1.getClass().getName(), JOptionPane.ERROR_MESSAGE);
				logger.error("Exception happened!", e1);	
				PREFERRED_STYLE = BACKUP_PREFERRED_STYLE;
				}
		    }
		});
		
		JLabel lblMenuFontSize = new JLabel("Menu font size");
		lblMenuFontSize.setFont(new Font("Tahoma", Font.BOLD, 13));
		getContentPane().add(lblMenuFontSize, "cell 1 6,alignx center");
		
		JSpinner spinnerMenuSize = new JSpinner();
		spinnerMenuSize.setModel(new SpinnerNumberModel(12, 5, 24, 1));
		getContentPane().add(spinnerMenuSize, "cell 2 6,growx");
		spinnerMenuSize.setValue(MENU_FONT_SIZE);
		
		sliderMenuSize = new JSlider();
		sliderMenuSize.setValue(MENU_FONT_SIZE);
		sliderMenuSize.setMaximum(24);
		sliderMenuSize.setMinimum(5);
		getContentPane().add(sliderMenuSize, "cell 3 6 7 1,growx");
		
		JLabel lblFontSize = new JLabel("Log Font size");
		lblFontSize.setFont(new Font("Tahoma", Font.BOLD, 13));
		getContentPane().add(lblFontSize, "cell 1 7,alignx center");
		
		JSpinner spinnerLogSize = new JSpinner();
		spinnerLogSize.setModel(new SpinnerNumberModel(12, 5, 30, 1));
		spinnerLogSize.setValue(LOG_PANEL_FONT_SIZE);
		getContentPane().add(spinnerLogSize, "cell 2 7,growx");
		
		sliderLogSize = new JSlider();
		sliderLogSize.setMaximum(30);
		sliderLogSize.setMinimum(5);
		sliderLogSize.setValue(LOG_PANEL_FONT_SIZE);
		getContentPane().add(sliderLogSize, "cell 3 7 7 1,growx");
		
		JSeparator separator_1 = new JSeparator();
		getContentPane().add(separator_1, "cell 1 8 9 1,growx,aligny center");
		
		JButton btnResetValues = new JButton("Reset initial values");
		btnResetValues.setFont(new Font("SansSerif", Font.BOLD, 12));
		btnResetValues.setBackground(new Color(240, 230, 140));
		getContentPane().add(btnResetValues, "cell 1 9 2 1,growx,aligny center");
		btnResetValues.addActionListener(new ActionListener() {
			
			// action fired when user reset initial values saved in backup values
			@Override
			public void actionPerformed(ActionEvent e) {
				
				logger.debug("Starting reset of GUI_Preferences");
				
				// reset current values
				resetToBackup();
				
				// reset INItializer reference currentConf too
				resetConfigValues(currentConf);
				
				// reset gui (sliders changement will trigger jtextfield, see action listener)
				updateGUI();
				
				logger.debug("Configuration resetted: \n"+currentConf.toStringa());
			}
		});
		
		JLabel lblUsername = new JLabel("Username");
		lblUsername.setFont(new Font("Tahoma", Font.BOLD, 13));
		getContentPane().add(lblUsername, "cell 3 9,alignx right");
		
		textFieldUname = new JTextField();
		textFieldUname.setBackground(new Color(224, 255, 255));
		textFieldUname.setHorizontalAlignment(SwingConstants.CENTER);
		textFieldUname.setFont(new Font("SansSerif", Font.PLAIN, 18));
		getContentPane().add(textFieldUname, "cell 4 9 3 1,growx,aligny center");
		textFieldUname.setColumns(10);
		textFieldUname.setBorder(new LineBorder(Color.DARK_GRAY, 3));
		textFieldUname.setText(CURRENT_USERNAME);
		textFieldUname.getDocument().addDocumentListener(new DocumentListener() {
			  public void changedUpdate(DocumentEvent e) {
			  	  resetCurrentIni();
			  }
			  public void removeUpdate(DocumentEvent e) {
				  resetCurrentIni();
			  }
			  public void insertUpdate(DocumentEvent e) {
				  resetCurrentIni();
			  }

			  public void resetCurrentIni() {
				  CURRENT_USERNAME = textFieldUname.getText();
				  currentConf.getGenOpt().setUserName(CURRENT_USERNAME);
			  }
		});
		
		JButton btnSaveConf = new JButton("Save Values");
		btnSaveConf.setForeground(new Color(0, 0, 0));
		btnSaveConf.setFont(new Font("SansSerif", Font.BOLD, 12));
		btnSaveConf.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int chosenOption = JOptionPane.showConfirmDialog(thisFrame,"Are you sure you want to overwrite "+currentConf.getIniFile().getFile().getPath()+"(ONLY GUI_OPTIONS)?", 
		        		"overwrite configuration", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
				if (chosenOption == JOptionPane.YES_OPTION) {
					
					// write single values to file ini associated to currentConf
					writeValuesToConfFile(currentConf);
					
					// update BACKUP_VALUES as the new ones
					updateBackup();

					JOptionPane.showMessageDialog(thisFrame, "Graphic Interface settings Saved :)\n -> Changes will be visible after next restart.", "Saved!", JOptionPane.INFORMATION_MESSAGE);

				}else {
					JOptionPane.showMessageDialog(thisFrame, "Ok, I wont save theese settings ;)", "Not saved!", JOptionPane.WARNING_MESSAGE);
				}
				
				
			}
		});
		btnSaveConf.setBackground(new Color(169, 169, 169));
		getContentPane().add(btnSaveConf, "cell 7 9,growx,aligny center");
		
		JButton btnUseSettings = new JButton("Use");
		btnUseSettings.setForeground(Color.WHITE);
		btnUseSettings.setFont(new Font("SansSerif", Font.BOLD, 12));
		btnUseSettings.setBackground(Color.DARK_GRAY);
		getContentPane().add(btnUseSettings, "cell 8 9,growx");
		
		JButton btnDiscard = new JButton("Discard");
		btnDiscard.setForeground(new Color(0, 0, 0));
		btnDiscard.setFont(new Font("SansSerif", Font.BOLD, 12));
		btnDiscard.setBackground(new Color(205, 92, 92));
		getContentPane().add(btnDiscard, "cell 9 9,growx");
		btnDiscard.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				logger.debug("DISCARD => Closing GUI_Preferences");
				
				// reset current values
				resetToBackup();
				
				// reset INItializer reference currentConf too
				resetConfigValues(currentConf);
				
				// reset gui (sliders changement will trigger jtextfield, see action listener)
				updateGUI();
				
				logger.debug("Configuration resetted: \n"+currentConf.toStringa());
	        	
	        	//dispose();
				dispatchEvent(new WindowEvent(thisFrame, WindowEvent.WINDOW_CLOSING));
			}
		});
		
		btnUseSettings.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// update (locally)BACKUP_VALUES as the new ones
				updateBackup();
				
//				// advisor (changes won't take place until restart or reload of modules)
//				JOptionPane.showMessageDialog(thisFrame, "Some Changes will not be visible until next restart.", "Not all changes visible!", JOptionPane.INFORMATION_MESSAGE);

				// close and apply new values without saving
				//thisFrame.dispose();
				dispatchEvent(new WindowEvent(thisFrame, WindowEvent.WINDOW_CLOSING));
			}
		});
		
		// event handlers
		spinnerMenuSize.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				MENU_FONT_SIZE = (int)spinnerMenuSize.getValue();
				currentConf.getGuiOpt().setMenuFontSize(MENU_FONT_SIZE);
				sliderMenuSize.setValue(MENU_FONT_SIZE);
			}
		});
		
		spinnerLogSize.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				LOG_PANEL_FONT_SIZE = (int)spinnerLogSize.getValue();
				currentConf.getGuiOpt().setLogFontSize(LOG_PANEL_FONT_SIZE);
				sliderLogSize.setValue(LOG_PANEL_FONT_SIZE);
			}
		});
		
		sliderLogSize.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				LOG_PANEL_FONT_SIZE = (int)sliderLogSize.getValue();
				spinnerLogSize.setValue(LOG_PANEL_FONT_SIZE);
			}
		});
		
		sliderMenuSize.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				MENU_FONT_SIZE = (int)sliderMenuSize.getValue();
				spinnerMenuSize.setValue(MENU_FONT_SIZE);
			}
		});
		
		// default close operation(same as reset) 
		addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		            
		        	logger.debug("Closing GUI_Preferences");
					
					// reset current values
					resetToBackup();
					
					// reset INItializer reference currentConf too
					resetConfigValues(currentConf);
					
					// update gui (sliders changement will trigger jtextfield, see action listener)
					
					
					logger.debug("Closing "+getClass().getName()+" - Configuration resetted: \n"+currentConf.toStringa());
		        	
					active = false;
					
		        	dispose();
		    }
		});
	} 
	
	private void initValues(INItializer currentConf) {
		// initialize values from currentConfig
		CURRENT_USERNAME = currentConf.getGenOpt().getUserName();
		MENU_FONT_SIZE = currentConf.getGuiOpt().getMenuFontSize();
		LOG_PANEL_FONT_SIZE = currentConf.getGuiOpt().getLogFontSize();
		PREFERRED_STYLE = currentConf.getGuiOpt().getPreferredStyle();
		DARK_THEME = currentConf.getGuiOpt().isDarkGuiNimbus();
		
		// initialize default values (by value)
		BACKUP_CURRENT_USERNAME = new String(CURRENT_USERNAME);
		BACKUP_MENU_FONT_SIZE = MENU_FONT_SIZE;
		BACKUP_LOG_PANEL_FONT_SIZE = LOG_PANEL_FONT_SIZE;
		BACKUP_PREFERRED_STYLE = new String(PREFERRED_STYLE);
		BACKUP_DARK_THEME=DARK_THEME;
	}
	
	private void resetConfigValues(INItializer currentConf) {
		currentConf.getGenOpt().setUserName(CURRENT_USERNAME);
		currentConf.getGuiOpt().setPreferredStyle(PREFERRED_STYLE);
		currentConf.getGuiOpt().setMenuFontSize(MENU_FONT_SIZE);
		currentConf.getGuiOpt().setLogFontSize(LOG_PANEL_FONT_SIZE);
		currentConf.getGuiOpt().setDarkGuiNimbus(DARK_THEME);
	}
	
	private boolean writeValuesToConfFile(INItializer currentConf) {
		currentConf.writeSingleValue(currentConf.GEN_OPT, "userName", CURRENT_USERNAME);
		currentConf.writeSingleValue(currentConf.GUI_OPT, "preferredStyle", PREFERRED_STYLE);
		currentConf.writeSingleValue(currentConf.GUI_OPT, "menuFontSize", MENU_FONT_SIZE);
		currentConf.writeSingleValue(currentConf.GUI_OPT, "logFontSize", LOG_PANEL_FONT_SIZE);
		currentConf.writeSingleValue(currentConf.GUI_OPT, "darkGuiNimbus", DARK_THEME);
		
		return true;
	}
	
	private void updateGUI() {
		sliderMenuSize.setValue(MENU_FONT_SIZE);
		sliderLogSize.setValue(LOG_PANEL_FONT_SIZE);
		textFieldUname.setText(CURRENT_USERNAME);
		comboStyleLaF.setSelectedItem(PREFERRED_STYLE);
		chckbxDarklight.setSelected(DARK_THEME);
		
		try {
			UIManager.setLookAndFeel(PREFERRED_STYLE);
			for(Window window: Window.getWindows()) {
    		    SwingUtilities.updateComponentTreeUI(window);
    		}
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e1) {
			logger.error("Exception happened!", e1);
		}
		
		if(!DARK_THEME) {
			mainGUI.updateUILightNimbus();
		}else {
			mainGUI.updateUIDarkNimbus();
		}
		for(Window window: Window.getWindows()) {
		    SwingUtilities.updateComponentTreeUI(window);
		}
	}
	
	private void updateBackup(){
		// update backup values to current
		BACKUP_CURRENT_USERNAME = CURRENT_USERNAME;
		BACKUP_PREFERRED_STYLE = PREFERRED_STYLE;
		BACKUP_MENU_FONT_SIZE = MENU_FONT_SIZE;
		BACKUP_LOG_PANEL_FONT_SIZE = LOG_PANEL_FONT_SIZE;
		BACKUP_DARK_THEME = DARK_THEME;
	}
	
	private void resetToBackup(){
		// reset current values
		CURRENT_USERNAME = BACKUP_CURRENT_USERNAME;
		PREFERRED_STYLE = BACKUP_PREFERRED_STYLE;
		MENU_FONT_SIZE = BACKUP_MENU_FONT_SIZE;
		LOG_PANEL_FONT_SIZE = BACKUP_LOG_PANEL_FONT_SIZE;
		DARK_THEME = BACKUP_DARK_THEME;
	}

	private static void updateMenuAndLogFontSize(JFrame jframe){
		
		// use the same font as main gui but change font size
		UIManager.put("MenuBar.font", new Font("Segoe UI", Font.BOLD, MENU_FONT_SIZE ));
		UIManager.put("Menu.font", new Font("Segoe UI", Font.BOLD, MENU_FONT_SIZE ));
	    SwingUtilities.updateComponentTreeUI(jframe);
		
	}
}

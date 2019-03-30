package gUI.panels;

import javax.swing.JFrame;

import org.apache.log4j.Logger;
import org.ini4j.InvalidFileFormatException;
import org.ini4j.Wini;

import gUI.mainGUI;
import gui.ScrYpTLauncher;
import iNIT.INItializer;
import net.miginfocom.swing.MigLayout;
import uTILS.local.ListCharsets;

import javax.swing.JPanel;
import java.awt.Color;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.Font;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.JCheckBox;
import java.awt.SystemColor;
import java.awt.Window;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JTextField;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.awt.event.ActionEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.JSeparator;
import javax.swing.JComboBox;

public class GeneralOptionsFrame extends JFrame{

	// initialize Logger
	// GraphicPreferencesFrame logger
	static Logger logger = Logger.getLogger(GeneralOptionsFrame.class); 
	
	private final JFrame thisFrame;
	
	// gui elements as fields
	private JTextField textFieldUname;
	private JSpinner spinnerInterval;
	private JCheckBox chckbxAutosave;
	private JComboBox<String> comboCharsets;
	
	// actual values
	private static int AUTOSAVE_INTERVAL;
	private static boolean AUTOSAVE_FLAG;
	private static String CURRENT_USERNAME;
	private static String CHARSET;
	
	// default values
	private static int BACKUP_AUTOSAVE_INTERVAL;
	private static boolean BACKUP_AUTOSAVE_FLAG;
	private static String BACKUP_CURRENT_USERNAME;
	private static String BACKUP_CHARSET;
	
	// reference to configuration
	private INItializer currentConfiguration;
	private Wini launcherConfiguration;
	
	// flag to signal active
	public static boolean active=false;
	private JCheckBox chckbxHideLauncherAt;
	
	public GeneralOptionsFrame(INItializer currentConf) {
		
		active = true;
		
		// SW window icon
		try {
			this.setIconImage(ImageIO.read(new File(mainGUI.RESOURCES_DIR + mainGUI.ICON_FILE_NAME)));
			logger.debug("icon image setted to main window");
		} catch (IOException e1) {
			logger.error("Exception happened!", e1);
		}
		
		// initialize config
		currentConfiguration = currentConf;
		try {
			launcherConfiguration = new Wini(new File(ScrYpTLauncher.DEFAULT_INI_PATH));
		} catch (InvalidFileFormatException e1) {
			logger.error("Invlid launcher configuration format!\n", e1);
		} catch (IOException e1) {
			logger.error("IO error setting launcher configuration file", e1);
		}
		
		// assign this frame to variable
		thisFrame = this;
		
		initializeValues();		
		
		logger.debug("Initialized.");
		
		getContentPane().setBackground(new Color(119, 136, 153));
		getContentPane().setLayout(new MigLayout("", "[:10px:100px,grow][:110px:110px][:80:180,grow][130,grow][110,grow][:10px:100px,grow][:100px:170px][:100px:100px][:75px:75px][:75px:75px][:10px:100px,grow]", "[:50:70px][5][][:5:5px][30][10][][:5:5px][10%,grow,baseline]"));
		
		JPanel panel = new JPanel();
		panel.setForeground(Color.BLACK);
		panel.setBorder(new LineBorder(Color.BLACK, 4));
		panel.setBackground(Color.DARK_GRAY);
		getContentPane().add(panel, "cell 1 0 9 1,grow");
		panel.setLayout(new MigLayout("", "[10,grow][10,grow][200px,grow]", "[]"));
		
		chckbxAutosave = new JCheckBox("Autosave");
		chckbxAutosave.setFont(new Font("SansSerif", Font.BOLD, 14));
		chckbxAutosave.setForeground(Color.WHITE);
		panel.add(chckbxAutosave, "cell 0 0");
		chckbxAutosave.setHorizontalAlignment(SwingConstants.CENTER);
		chckbxAutosave.setBackground(new Color(119, 136, 153));
		chckbxAutosave.setSelected(AUTOSAVE_FLAG);
		chckbxAutosave.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				AUTOSAVE_FLAG = chckbxAutosave.isSelected();
				currentConfiguration.getFileOpt().setFlagAutosave(AUTOSAVE_FLAG);
				if(chckbxAutosave.isSelected()) {
					spinnerInterval.setEnabled(true);
				}else {
					spinnerInterval.setEnabled(false);
				}
			}
		});
		
//		 calculate minutes: interval in seconds / 60
		JLabel lblIntMinutes = new JLabel(AUTOSAVE_INTERVAL/60 +" Minutes + "+ AUTOSAVE_INTERVAL%60+" Seconds");
		lblIntMinutes.setBackground(new Color(240, 248, 255));
		lblIntMinutes.setForeground(new Color(255, 255, 255));
		lblIntMinutes.setFont(new Font("SansSerif", Font.BOLD, 13));
		getContentPane().add(lblIntMinutes, "cell 6 2 3 1,alignx center,aligny center");
		
		chckbxHideLauncherAt = new JCheckBox("Hide Launcher at start");
		panel.add(chckbxHideLauncherAt, "cell 1 0");
		chckbxHideLauncherAt.setSelected(readLauncherHidingOption());
		chckbxHideLauncherAt.setHorizontalAlignment(SwingConstants.CENTER);
		chckbxHideLauncherAt.setForeground(Color.WHITE);
		chckbxHideLauncherAt.setFont(new Font("SansSerif", Font.BOLD, 14));
		chckbxHideLauncherAt.setBackground(new Color(119, 136, 153));
		
		
		JLabel lblGeneralOptions = new JLabel("General Options");
		lblGeneralOptions.setHorizontalAlignment(SwingConstants.RIGHT);
		lblGeneralOptions.setForeground(new Color(230, 230, 250));
		lblGeneralOptions.setFont(new Font("Lithos Pro Regular", Font.BOLD, 22));
		lblGeneralOptions.setBackground(Color.LIGHT_GRAY);
		panel.add(lblGeneralOptions, "cell 2 0,alignx right");
		
		JLabel lblIntervalInSeconds = new JLabel("Interval in Seconds");
		lblIntervalInSeconds.setFont(new Font("SansSerif", Font.BOLD, 12));
		getContentPane().add(lblIntervalInSeconds, "cell 1 2,alignx center,aligny center");
		
		
		// update only local variable fields, INItializer will be updated on exit/save/use/discard
		spinnerInterval = new JSpinner();
		spinnerInterval.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				AUTOSAVE_INTERVAL = (int)spinnerInterval.getValue();
				currentConfiguration.getFileOpt().setAutosaveInterval(AUTOSAVE_INTERVAL*1000);
				lblIntMinutes.setText(AUTOSAVE_INTERVAL/60 +" Minutes + "+ AUTOSAVE_INTERVAL%60+" Seconds");
			}
		});
		spinnerInterval.setModel(new SpinnerNumberModel(AUTOSAVE_INTERVAL, INItializer.DEFAULT_MIN_AUTOSAVE_INTERVAL/1000, 3600, 1));
		getContentPane().add(spinnerInterval, "cell 2 2 2 1,growx,aligny center");
		spinnerInterval.setValue(AUTOSAVE_INTERVAL);
		if(AUTOSAVE_FLAG) {
			spinnerInterval.setEnabled(true);
		}else {
			spinnerInterval.setEnabled(false);
		}
		
		JLabel label_1 = new JLabel("=");
		label_1.setFont(new Font("SansSerif", Font.BOLD, 21));
		getContentPane().add(label_1, "cell 4 2,alignx center");
		
		JLabel lblDefaultCharset = new JLabel("Default Charset");
		lblDefaultCharset.setFont(new Font("SansSerif", Font.BOLD, 13));
		getContentPane().add(lblDefaultCharset, "cell 1 4,alignx center");
		
		comboCharsets = new JComboBox<String>(ListCharsets.getCharsets());
		getContentPane().add(comboCharsets, "cell 2 4 2 1,growx");
		comboCharsets.setSelectedItem(CHARSET);
		comboCharsets.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				CHARSET = (String)comboCharsets.getSelectedItem();
				logger.info("Default charset: " + CHARSET);
			}
		});
		
		JLabel label = new JLabel("Username");
		label.setFont(new Font("Tahoma", Font.BOLD, 13));
		getContentPane().add(label, "cell 4 4,alignx center");
		
		textFieldUname = new JTextField();
		textFieldUname.setText(CURRENT_USERNAME);
		textFieldUname.setHorizontalAlignment(SwingConstants.CENTER);
		textFieldUname.setFont(new Font("SansSerif", Font.PLAIN, 18));
		textFieldUname.setColumns(10);
		textFieldUname.setBorder(new LineBorder(Color.DARK_GRAY, 3));
		textFieldUname.setBackground(new Color(224, 255, 255));
		getContentPane().add(textFieldUname, "cell 6 4 4 1,growx");
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
		
		JButton btnResetValues = new JButton("Reset initial values");
		btnResetValues.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				logger.debug("Starting reset of General Options");
				
				// reset values to default
	        	resetValuesAndAdd();
	        	
	        	// update gui
	        	updateGUIComponents();		        	
				
	        	// reset check box workaround
	        	if(chckbxAutosave.isSelected()) {
					spinnerInterval.setEnabled(true);
				}else {
					spinnerInterval.setEnabled(false);
				}
	        	
				logger.debug("Configuration resetted.\n");
	        	
			}
		});
		
		JSeparator separator = new JSeparator();
		getContentPane().add(separator, "cell 1 6 9 1,growx");
		btnResetValues.setFont(new Font("SansSerif", Font.BOLD, 12));
		btnResetValues.setBackground(new Color(240, 230, 140));
		getContentPane().add(btnResetValues, "cell 1 8 2 1");
		
		JButton btnDiscard = new JButton("Discard");
		btnDiscard.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				logger.debug("DISCARD => Closing General Options");
				
				// reset values to default
	        	resetValuesAndAdd();
	        	
	        	// update gui
	        	updateGUIComponents();		        	
				
				logger.debug("Closing "+getClass().getName()+" - Configuration resetted.\n");
	        	
				//thisFrame.dispose();
				dispatchEvent(new WindowEvent(thisFrame, WindowEvent.WINDOW_CLOSING));
			}
		});
		
		JButton btnUseSettings = new JButton("Use");
		btnUseSettings.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// update fields' values
				setValuesAndAdd();
				
				// update gui
	        	updateGUIComponents();	
	        	
	        	// write launcher configuration
	    		writeLauncherHidingOption(chckbxHideLauncherAt.isSelected());
				
				// close and apply new values without saving () windowclose listener will use updated values
				//thisFrame.dispose();
	        	dispatchEvent(new WindowEvent(thisFrame, WindowEvent.WINDOW_CLOSING));
			}
		});
		
		JButton btnSaveConf = new JButton("Save Values");
		btnSaveConf.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (JOptionPane.showConfirmDialog(thisFrame,"Are you sure you want to overwrite "+currentConf.getIniFile().getFile().getPath()+"(ONLY GENERAL OPTIONS)?", 
		        		"overwrite configuration", JOptionPane.YES_NO_OPTION,
		        		JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
					
					writeValues();
					
					// update backup values to current
					setValuesAndAdd();

					logger.debug("SAVE => General Options saved.");
					JOptionPane.showMessageDialog(thisFrame, "General Options Saved :)", "Saved!", JOptionPane.INFORMATION_MESSAGE);
					
					dispatchEvent(new WindowEvent(thisFrame, WindowEvent.WINDOW_CLOSING));
					
				}else {
					JOptionPane.showMessageDialog(thisFrame, "Ok, I wont save theese settings ;)", "Not saved!", JOptionPane.WARNING_MESSAGE);
				}
			}
		});
		btnSaveConf.setForeground(Color.BLACK);
		btnSaveConf.setFont(new Font("SansSerif", Font.BOLD, 12));
		btnSaveConf.setBackground(new Color(169, 169, 169));
		getContentPane().add(btnSaveConf, "cell 7 8");
		btnUseSettings.setForeground(Color.WHITE);
		btnUseSettings.setFont(new Font("SansSerif", Font.BOLD, 12));
		btnUseSettings.setBackground(Color.DARK_GRAY);
		getContentPane().add(btnUseSettings, "cell 8 8,growx");
		btnDiscard.setForeground(new Color(0, 0, 0));
		btnDiscard.setFont(new Font("SansSerif", Font.BOLD, 12));
		btnDiscard.setBackground(new Color(205, 92, 92));
		getContentPane().add(btnDiscard, "cell 9 8,growx");
		
		// default close operation(same as reset) 
		addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		            
	        	logger.debug("Closing GUI_Preferences");
				
	        	// reset values to default adn update configuration
	        	resetValuesAndAdd();
	        	
	        	// update gui
	        	updateGUIComponents();		        	
				
				logger.debug("Closing "+getClass().getName()+" - Configuration resetted.\n");
	        	
				active = false;
				
	        	dispose();
		    }
		});

	}
	
	private void initializeValues() {
		// initialize values from currentConfig
		AUTOSAVE_INTERVAL = currentConfiguration.getFileOpt().getAutosaveInterval()/1000;
		AUTOSAVE_FLAG = currentConfiguration.getFileOpt().isFlagAutosave();
		CURRENT_USERNAME = currentConfiguration.getGenOpt().getUserName();
		CHARSET = System.getProperty("file.encoding");
		
		// initialize default values (by value)
		BACKUP_AUTOSAVE_INTERVAL = AUTOSAVE_INTERVAL;
		BACKUP_AUTOSAVE_FLAG = AUTOSAVE_FLAG;
		BACKUP_CURRENT_USERNAME = new String(CURRENT_USERNAME);
		BACKUP_CHARSET = new String(CHARSET); 
	}
	
	/**
	 * updates gui with current used values
	 */
	private void updateGUIComponents() {
    	textFieldUname.setText(CURRENT_USERNAME);
    	spinnerInterval.setValue(AUTOSAVE_INTERVAL);
    	chckbxAutosave.setSelected(AUTOSAVE_FLAG);
    	comboCharsets.setSelectedItem(CHARSET);
    	chckbxHideLauncherAt.setSelected(readLauncherHidingOption());
	}
		
	/**
	 * resets values to backup ones
	 */
	private void resetValuesAndAdd() {
		// reset current values
		AUTOSAVE_INTERVAL = BACKUP_AUTOSAVE_INTERVAL;
		AUTOSAVE_FLAG = BACKUP_AUTOSAVE_FLAG;
		CURRENT_USERNAME = BACKUP_CURRENT_USERNAME;
		CHARSET = BACKUP_CHARSET;
		
		// reset INItializer reference currentConfiguration too
		currentConfiguration.getFileOpt().setAutosaveInterval(AUTOSAVE_INTERVAL*1000);
		currentConfiguration.getFileOpt().setFlagAutosave(AUTOSAVE_FLAG);
		currentConfiguration.getGenOpt().setUserName(CURRENT_USERNAME);
		currentConfiguration.getGenOpt().setCharset(CHARSET);
		System.setProperty("file.encoding", CHARSET);

	}
	
	/**
	 * sets values to backup ones and adds them to configuration
	 */
	private void setValuesAndAdd() {
		// set current values
		BACKUP_AUTOSAVE_INTERVAL = AUTOSAVE_INTERVAL;
		BACKUP_AUTOSAVE_FLAG = AUTOSAVE_FLAG;
		BACKUP_CURRENT_USERNAME = CURRENT_USERNAME;
		BACKUP_CHARSET = CHARSET;
		
		// set INItializer reference currentConfiguration too
		currentConfiguration.getFileOpt().setAutosaveInterval(AUTOSAVE_INTERVAL*1000);
		currentConfiguration.getFileOpt().setFlagAutosave(AUTOSAVE_FLAG);
		currentConfiguration.getGenOpt().setUserName(CURRENT_USERNAME);
		currentConfiguration.getGenOpt().setCharset(CHARSET);
		System.setProperty("file.encoding", CHARSET);

	}
	
	private void writeValues() {
		// write single values to file ini associated to currentConf
		currentConfiguration.writeSingleValue(currentConfiguration.GEN_OPT, "userName", CURRENT_USERNAME);
		currentConfiguration.writeSingleValue(currentConfiguration.GEN_OPT, "charset", CHARSET);
		currentConfiguration.writeSingleValue(currentConfiguration.FILE_OPT, "flagAutosave", AUTOSAVE_FLAG);
		currentConfiguration.writeSingleValue(currentConfiguration.FILE_OPT, "autosaveInterval", AUTOSAVE_INTERVAL*1000);
		
		// write launcher configuration
		writeLauncherHidingOption(chckbxHideLauncherAt.isSelected());
	}
	
	private void writeLauncherHidingOption(boolean hideLauncher) {
		try {
			String valueHidden = (hideLauncher)? "true" : "false";
			launcherConfiguration.put(ScrYpTLauncher.INI_APP_OPT, "flagHideNStartDirect", valueHidden );
			launcherConfiguration.store();
		} catch (IOException e) {
			logger.error("Exception happened writing launcher configuration!", e);
		}
	}
	
	private boolean readLauncherHidingOption() {
		String startDirectString = launcherConfiguration.get(ScrYpTLauncher.INI_APP_OPT, "flagHideNStartDirect");
		boolean startDirect = (startDirectString.equalsIgnoreCase("true"))? true : false;
		return startDirect;
	}
}

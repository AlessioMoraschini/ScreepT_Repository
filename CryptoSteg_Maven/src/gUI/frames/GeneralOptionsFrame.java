package gUI.frames;

import javax.swing.JFrame;

import org.apache.log4j.Logger;
import org.ini4j.InvalidFileFormatException;
import org.ini4j.Wini;

import dialogutils.JOptionHelperExtended;
import frameutils.frame.arch.ParentFrame;
import frameutils.utils.GuiUtils;
import launcherutils.IniFileManager;
import maingUI.MainGUI;
import maingUI.MainGuiGeneralAppStarter;
import mainobj.INItializer;
import net.miginfocom.swing.MigLayout;
import om.FileOptions;
import resources.GeneralConfig;
import resources.IconsPathConfigurator;
import resources.SoundsConfigurator;
import utility.ListCharsets;

import javax.swing.JPanel;
import java.awt.Color;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.JLabel;

import java.awt.Font;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.JCheckBox;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.JButton;
import javax.swing.JTextField;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.awt.event.ActionEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.JSeparator;
import javax.swing.JComboBox;

import java.awt.Cursor;
import javax.swing.JSlider;
import java.awt.Dimension;

public class GeneralOptionsFrame extends ParentFrame{
	private static final long serialVersionUID = 7396680036861927288L;

	// initialize Logger
	// GraphicPreferencesFrame logger
	static Logger logger = Logger.getLogger(GeneralOptionsFrame.class);
	
	public static GeneralOptionsFrame singletonInstance = null;
	
	// gui elements as fields
	private final JFrame thisFrame;
	private JTextField textFieldUname;
	private JSpinner spinnerInterval;
	private JCheckBox chckbxAutosave;
	private JComboBox<String> comboCharsets;
	private JCheckBox chckbxHideLauncherAt;	
	private JCheckBox chckbxExceptionAdvicesEnabled;
	private JLabel lblIntMinutes;
	private JButton btnResetValues;
	private JButton btnDiscard;
	private JButton btnUseSettings;
	private JButton btnSaveConf;
	
	// default values
	private int BACKUP_AUTOSAVE_INTERVAL;
	private boolean BACKUP_AUTOSAVE_FLAG;
	private boolean BACKUP_EXCEPTION_ADVICES;
	private boolean BACKUP_AUTOCHECK_UPDATES;
	private String BACKUP_CURRENT_USERNAME;
	private String BACKUP_CHARSET;
	private float BACKUP_GENVOLUME;
	
	// reference to configuration
	private INItializer currentConfiguration;
	private Wini launcherConfiguration;
	
	private JLabel lblVolume;
	private JLabel lblDynVolume;
	private JSlider sliderVolume;
	private JLabel lblLogLevelChosen;
	
	JOptionHelperExtended dialogHelper;
	private JCheckBox chckbxAutomaticallyCheckFor;
	
	public GeneralOptionsFrame(INItializer currentConf) {
		
		isActive.set(true);
		
		logger.info(this.getClass().getName() + " Starting...");
		
		dialogHelper = new JOptionHelperExtended(this);
		
		// assign this frame to variable
		thisFrame = this;
		singletonInstance = this;
		
		// initialize launcher config
		currentConfiguration = currentConf;
		
		// SW window icon
		GuiUtils.setFrameIcon(IconsPathConfigurator.ICON_GEN_OPT, this);
		
		GuiUtils.showOnSameScreen(this, MainGUI.frmScryptV);
		
		try {
			launcherConfiguration = new Wini(new File(GeneralConfig.DEFAULT_LAUNCHER_INI_PATH));
		} catch (InvalidFileFormatException e1) {
			logger.error("Invlid launcher configuration format!\n", e1);
		} catch (IOException e1) {
			logger.error("IO error setting launcher configuration file", e1);
		}
		
		initializeValues();		
		
		getContentPane().setBackground(Color.DARK_GRAY);
		getContentPane().setLayout(new MigLayout("", "[:10px:100px,grow][:110px:110px][:80:180][100px:120px,grow][80px:100,grow][:20px:100px][grow][:40px:50px][][:100px:100px][:100px:100px,grow][:75px:75px][:75px:95px][:10px:100px,grow]", "[:30:40px][5][][:5:5px][30px:40][grow,bottom][:30px:35px,baseline]"));
		
		JPanel panel = new JPanel();
		panel.setForeground(Color.BLACK);
		panel.setBorder(new LineBorder(new Color(0, 0, 0), 2));
		panel.setBackground(Color.GRAY);
		getContentPane().add(panel, "cell 0 0 14 1,grow");
		panel.setLayout(new MigLayout("", "[10][][][200px,grow][10][][10][][][][10][100px:110px,grow]", "[grow]"));
		
		JLabel lblGeneralOptions = new JLabel("General Options");
		lblGeneralOptions.setHorizontalAlignment(SwingConstants.RIGHT);
		lblGeneralOptions.setForeground(new Color(255, 255, 240));
		lblGeneralOptions.setFont(new Font("Lithos Pro Regular", Font.BOLD, 16));
		lblGeneralOptions.setBackground(Color.LIGHT_GRAY);
		panel.add(lblGeneralOptions, "cell 0 0 4 1,alignx left");
		
		lblVolume = new JLabel("Volume:");
		panel.add(lblVolume, "cell 7 0");
		lblVolume.setForeground(new Color(255, 255, 255));
		lblVolume.setFont(new Font("SansSerif", Font.BOLD, 13));
		
		lblDynVolume = new JLabel("");
		panel.add(lblDynVolume, "cell 8 0");
		lblDynVolume.setForeground(new Color(220, 239, 248));
		lblDynVolume.setFont(new Font("SansSerif", Font.BOLD, 13));
		
		sliderVolume = new JSlider(0, 100);
		panel.add(sliderVolume, "cell 9 0,growx");
		
		lblLogLevelChosen = new JLabel("LogLevel: " + MainGuiGeneralAppStarter.LOG_LEVEL_AT_START);
		panel.add(lblLogLevelChosen, "cell 11 0,alignx center");
		lblLogLevelChosen.setForeground(new Color(255, 255, 255));
		lblLogLevelChosen.setFont(new Font("Tahoma", Font.BOLD, 13));
		
		JLabel lblDefaultCharset = new JLabel("Encoding:");
		lblDefaultCharset.setForeground(Color.WHITE);
		lblDefaultCharset.setFont(new Font("SansSerif", Font.BOLD, 13));
		getContentPane().add(lblDefaultCharset, "cell 1 2,alignx center");
		
		comboCharsets = new JComboBox<String>(ListCharsets.getCharsets());
		comboCharsets.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		comboCharsets.setToolTipText("Choose general application Charset to use");
		getContentPane().add(comboCharsets, "cell 2 2 2 1,grow");
		
		chckbxAutosave = new JCheckBox("Autosave configuration");
		getContentPane().add(chckbxAutosave, "cell 4 2 2 1,alignx right,aligny center");
		chckbxAutosave.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		chckbxAutosave.setToolTipText("Enable/Disable periodic autosave (also when exiting application) current configuration will be automatically saved");
		chckbxAutosave.setFont(new Font("SansSerif", Font.BOLD, 14));
		chckbxAutosave.setForeground(Color.LIGHT_GRAY);
		chckbxAutosave.setHorizontalAlignment(SwingConstants.CENTER);
		chckbxAutosave.setBackground(new Color(119, 136, 153));
		chckbxAutosave.setOpaque(false);
		
		JLabel lblIntervalInSeconds = new JLabel("Timeout :");
		lblIntervalInSeconds.setForeground(Color.WHITE);
		lblIntervalInSeconds.setFont(new Font("SansSerif", Font.BOLD, 14));
		getContentPane().add(lblIntervalInSeconds, "cell 6 2 2 1,alignx right,aligny center");
		
		// update only local variable fields, INItializer will be updated on exit/save/use/discard
		spinnerInterval = new JSpinner();
		spinnerInterval.setMinimumSize(new Dimension(90, 28));
		spinnerInterval.setPreferredSize(new Dimension(70, 28));
		spinnerInterval.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		spinnerInterval.setModel(new SpinnerNumberModel(100, FileOptions.DEFAULT_MIN_AUTOSAVE_INTERVAL/1000, 3600000, 1));
		getContentPane().add(spinnerInterval, "cell 8 2 2 1,growx,aligny center");
		
//		 calculate minutes: interval in seconds / 60
		lblIntMinutes = new JLabel("(0 Minutes + 0 Seconds)");
		lblIntMinutes.setBackground(new Color(240, 248, 255));
		lblIntMinutes.setForeground(Color.LIGHT_GRAY);
		lblIntMinutes.setFont(new Font("SansSerif", Font.BOLD, 13));
		getContentPane().add(lblIntMinutes, "cell 10 2 3 1,alignx center,aligny center");
		
		JSeparator separator = new JSeparator();
		
		chckbxExceptionAdvicesEnabled = new JCheckBox("Exception Advices enabled");
		chckbxExceptionAdvicesEnabled.setOpaque(false);
		chckbxExceptionAdvicesEnabled.setForeground(Color.LIGHT_GRAY);
		chckbxExceptionAdvicesEnabled.setToolTipText("Enable/Disable error dialog in case of general warnings detected by the application");
		chckbxExceptionAdvicesEnabled.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		chckbxExceptionAdvicesEnabled.setFont(new Font("SansSerif", Font.BOLD, 14));
		getContentPane().add(chckbxExceptionAdvicesEnabled, "cell 1 4 3 1,alignx center,aligny center");
		
		chckbxAutomaticallyCheckFor = new JCheckBox("Automatically check for updates");
		chckbxAutomaticallyCheckFor.setOpaque(false);
		chckbxAutomaticallyCheckFor.setForeground(Color.LIGHT_GRAY);
		chckbxAutomaticallyCheckFor.setToolTipText("Enable/Disable automatic check for updates at start");
		chckbxAutomaticallyCheckFor.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		chckbxAutomaticallyCheckFor.setFont(new Font("SansSerif", Font.BOLD, 15));
		getContentPane().add(chckbxAutomaticallyCheckFor, "cell 4 4 6 1,alignx center,aligny center");
		
		chckbxHideLauncherAt = new JCheckBox("Hide Launcher at start");
		chckbxHideLauncherAt.setToolTipText("When checked, launcher will not be shown on startup");
		getContentPane().add(chckbxHideLauncherAt, "cell 10 4 3 1,alignx center,aligny center");
		chckbxHideLauncherAt.setOpaque(false);
		chckbxHideLauncherAt.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		chckbxHideLauncherAt.setSelected(readLauncherHidingOption());
		chckbxHideLauncherAt.setHorizontalAlignment(SwingConstants.CENTER);
		chckbxHideLauncherAt.setForeground(Color.LIGHT_GRAY);
		chckbxHideLauncherAt.setFont(new Font("SansSerif", Font.BOLD, 14));
		chckbxHideLauncherAt.setBackground(new Color(119, 136, 153));
		getContentPane().add(separator, "cell 1 5 12 1,growx");
		
		JLabel lblUsername = new JLabel("Username");
		lblUsername.setForeground(Color.WHITE);
		lblUsername.setFont(new Font("Tahoma", Font.BOLD, 13));
		getContentPane().add(lblUsername, "cell 1 6,alignx center");
		
		textFieldUname = new JTextField();
		textFieldUname.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		textFieldUname.setToolTipText("Choose name used to customize messages and various dialogs and advices");
		textFieldUname.setHorizontalAlignment(SwingConstants.CENTER);
		textFieldUname.setFont(new Font("SansSerif", Font.PLAIN, 16));
		textFieldUname.setColumns(10);
		textFieldUname.setBorder(new LineBorder(Color.DARK_GRAY, 3));
		textFieldUname.setBackground(new Color(255, 255, 255));
		getContentPane().add(textFieldUname, "cell 2 6 3 1,grow");
		btnResetValues = new JButton("Reset initial values");
		btnResetValues.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnResetValues.setFont(new Font("SansSerif", Font.BOLD, 12));
		btnResetValues.setBackground(new Color(240, 230, 140));
		getContentPane().add(btnResetValues, "cell 5 6 4 1,growx");
		
		btnSaveConf = new JButton("Save Values");
		btnSaveConf.setToolTipText("Close this dialog and use current settings with explicit save on phisical file");
		btnSaveConf.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnSaveConf.setForeground(Color.BLACK);
		btnSaveConf.setFont(new Font("SansSerif", Font.BOLD, 12));
		btnSaveConf.setBackground(new Color(169, 169, 169));
		getContentPane().add(btnSaveConf, "cell 9 6 2 1,growx");

		btnUseSettings = new JButton("Use");
		btnUseSettings.setToolTipText("Close this dialog and use current settings without explicit save on phisical file");
		btnUseSettings.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnUseSettings.setForeground(Color.WHITE);
		btnUseSettings.setFont(new Font("SansSerif", Font.BOLD, 12));
		btnUseSettings.setBackground(Color.DARK_GRAY);
		getContentPane().add(btnUseSettings, "cell 11 6,growx");

		btnDiscard = new JButton("Discard");
		btnDiscard.setToolTipText("Close this dialog and discard every change");
		btnDiscard.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnDiscard.setForeground(new Color(0, 0, 0));
		btnDiscard.setFont(new Font("SansSerif", Font.BOLD, 12));
		btnDiscard.setBackground(new Color(205, 92, 92));
		getContentPane().add(btnDiscard, "cell 12 6,growx");
		
		addHandlers();
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				updateGUIComponents();
			}
		});
		
		logger.info(this.getClass().getName() + " Started");
	}
	
	private void addHandlers() {
		
		// DEFAULT CLOSE ACTIONS 
		addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		            
	        	logger.debug("Closing GUI_Preferences");
				
	        	// reset values to default adn update configuration
	        	resetValuesAndAdd();
	        	
	        	// update gui
	        	updateGUIComponents();		        	

	        	isActive.set(false);
	        	
				logger.debug("Closing "+getClass().getName()+" - Configuration resetted.\n");
				
				singletonInstance = null;
				
	        	dispose();
		    }
		});
		
		// VOLUME SLIDER
		sliderVolume.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				lblDynVolume.setText(sliderVolume.getValue() + "%");
				float GENVOLUME = (sliderVolume.getValue()/100.0f);
				currentConfiguration.getGenOpt().setGeneralVolume(GENVOLUME);
			}
		});
		
		// AUTOSAVE CHECKBOX HANDLER
		chckbxAutosave.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean AUTOSAVE_FLAG = chckbxAutosave.isSelected();
				currentConfiguration.getFileOpt().setFlagAutosave(AUTOSAVE_FLAG);
				refreshCheckBoxForeground(chckbxAutosave);
				if(AUTOSAVE_FLAG) {
					spinnerInterval.setEnabled(true);
					lblIntMinutes.setForeground(Color.WHITE);
				}else {
					spinnerInterval.setEnabled(false);
					lblIntMinutes.setForeground(Color.LIGHT_GRAY);
				}
			}
		});
		
		// EXCFEPTION ADVICE ON/OFF HANDLER
		chckbxExceptionAdvicesEnabled.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean exceptionAdvices = chckbxExceptionAdvicesEnabled.isSelected();
				currentConfiguration.getGenOpt().setExceptionAdvicesEnabled(exceptionAdvices);
				refreshCheckBoxForeground(chckbxExceptionAdvicesEnabled);
			}
		});

		// AUTO UPDATES ON/OFF 
		chckbxAutomaticallyCheckFor.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean autoUpdates = chckbxAutomaticallyCheckFor.isSelected();
				currentConfiguration.getGenOpt().setAutomaticUpdateOn(autoUpdates);
				refreshCheckBoxForeground(chckbxAutomaticallyCheckFor);
			}
		});

		// HIDE LAUNCHER ON/OFF HANDLER - ONLY COLOR FONT, the rest is handled in ad-hoc method when saving-resetting
		chckbxHideLauncherAt.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				refreshCheckBoxForeground(chckbxHideLauncherAt);
			}
		});
		
		// AUTOSAVE SPINNER HANDLER
		spinnerInterval.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				int AUTOSAVE_INTERVAL = (int)spinnerInterval.getValue();
				currentConfiguration.getFileOpt().setAutosaveInterval(AUTOSAVE_INTERVAL*1000);
				lblIntMinutes.setText("("+AUTOSAVE_INTERVAL/60 +" Minutes + "+ AUTOSAVE_INTERVAL%60+" Seconds)");
			}
		});
		
		// CHARSETS SELECTOR HANDLER
		comboCharsets.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				String CHARSET = (String)comboCharsets.getSelectedItem();
				currentConfiguration.getGenOpt().setCharset(CHARSET);
				logger.info("Default charset: " + CHARSET);
			}
		});
		
		// USERNAME HANDLER
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
				  String CURRENT_USERNAME = textFieldUname.getText();
				  currentConfiguration.getGenOpt().setUserName(CURRENT_USERNAME);
			  }
		});
		
		// RESET BUTTON HANDLER
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
		
		// DISCARD BUTTON HANDLER
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
		
		// SAVE CONF BUTTON HANDLER
		btnSaveConf.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (dialogHelper.yesOrNo("Are you sure you want to overwrite "+currentConfiguration.getIniFile().getFile().getPath()+"(ONLY GENERAL OPTIONS)?", 
		        		"overwrite configuration")) {
					
					writeValues();
					
					// update backup values to current
					setValuesAndAdd();

					logger.debug("SAVE => General Options saved.");
					dialogHelper.info("General Options Saved :)", "Saved!");
					
					dispatchEvent(new WindowEvent(thisFrame, WindowEvent.WINDOW_CLOSING));
					
				}else {
					dialogHelper.warn("Ok, I wont save theese settings ;)", "Not saved!");
				}
			}
		});
		
		// USE SETTINGS BUTTON HANDLER
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
	}
	
	public void refreshCheckBoxForeground() {
		refreshCheckBoxForeground(chckbxAutomaticallyCheckFor);
		refreshCheckBoxForeground(chckbxAutosave);
		refreshCheckBoxForeground(chckbxExceptionAdvicesEnabled);
		refreshCheckBoxForeground(chckbxHideLauncherAt);
	}
	
	public void refreshCheckBoxForeground(JCheckBox target) {
		boolean autocheck = target.isSelected();
		if(autocheck) {
			target.setForeground(Color.WHITE);
		}else {
			target.setForeground(Color.LIGHT_GRAY);
		}
	}
	
	private void initializeValues() {
		
		// initialize default values (by value)
		BACKUP_AUTOSAVE_INTERVAL = currentConfiguration.getFileOpt().getAutosaveInterval()/1000;
		BACKUP_AUTOSAVE_FLAG = currentConfiguration.getFileOpt().isFlagAutosave();
		BACKUP_CURRENT_USERNAME = new String(currentConfiguration.getGenOpt().getUserName());
		BACKUP_CHARSET = currentConfiguration.getGenOpt().getCharset();
		BACKUP_GENVOLUME = currentConfiguration.getGenOpt().getGeneralVolume();
		BACKUP_EXCEPTION_ADVICES = currentConfiguration.getGenOpt().isExceptionAdvicesEnabled();
		BACKUP_AUTOCHECK_UPDATES = currentConfiguration.getGenOpt().isAutomaticUpdateOn();
	}
	
	/**
	 * updates gui with current used values
	 */
	private void updateGUIComponents() {
    	textFieldUname.setText(currentConfiguration.getGenOpt().getUserName());
    	spinnerInterval.setValue(currentConfiguration.getFileOpt().getAutosaveInterval()/1000);
    	sliderVolume.setValue((int)((currentConfiguration.getGenOpt().getGeneralVolume())*100));
    	lblDynVolume.setText(currentConfiguration.getGenOpt().getVolumeString());
    	SoundsConfigurator.DEFAULT_EFFECTS_VOLUME = currentConfiguration.getGenOpt().getGeneralVolume();
    	chckbxAutosave.setSelected(currentConfiguration.getFileOpt().isFlagAutosave());
    	chckbxExceptionAdvicesEnabled.setSelected(currentConfiguration.getGenOpt().isExceptionAdvicesEnabled());
    	chckbxAutomaticallyCheckFor.setSelected(currentConfiguration.getGenOpt().isAutomaticUpdateOn());
    	updateComboCharsetSelection(currentConfiguration.getGenOpt().getCharset());
    	System.setProperty("file.encoding", currentConfiguration.getGenOpt().getCharset());
    	chckbxHideLauncherAt.setSelected(readLauncherHidingOption());
    	
    	refreshCheckBoxForeground();
	}
	
	public void updateComboCharsetSelection(String charset) {
		int tot = comboCharsets.getItemCount();
		for(int i = 0; i < tot; i++) {
			String value = comboCharsets.getItemAt(i);
			if(value.equalsIgnoreCase(charset)) {
				comboCharsets.setSelectedIndex(i);
				break;
			}
		}
		
		comboCharsets.revalidate();
		comboCharsets.repaint();
	}
		
	/**
	 * resets values to backup ones
	 */
	private void resetValuesAndAdd() {
		// reset current values
		currentConfiguration.getFileOpt().autosaveInterval = BACKUP_AUTOSAVE_INTERVAL*1000;
		currentConfiguration.getFileOpt().setFlagAutosave(BACKUP_AUTOSAVE_FLAG);
		currentConfiguration.getGenOpt().setUserName(BACKUP_CURRENT_USERNAME);
		currentConfiguration.getGenOpt().setCharset(BACKUP_CHARSET);
		currentConfiguration.getGenOpt().setGeneralVolume(BACKUP_GENVOLUME);
		currentConfiguration.getGenOpt().setExceptionAdvicesEnabled(BACKUP_EXCEPTION_ADVICES);
		currentConfiguration.getGenOpt().setAutomaticUpdateOn(BACKUP_AUTOCHECK_UPDATES);
		
		// reset INItializer reference currentConfiguration too
		System.setProperty("file.encoding", BACKUP_CHARSET);

	}
	
	/**
	 * sets values to backup ones and adds them to configuration
	 */
	private void setValuesAndAdd() {
		// set current values
		BACKUP_AUTOSAVE_INTERVAL = currentConfiguration.getFileOpt().getAutosaveInterval()/1000;
		BACKUP_AUTOSAVE_FLAG = currentConfiguration.getFileOpt().isFlagAutosave();
		BACKUP_CURRENT_USERNAME = currentConfiguration.getGenOpt().getUserName();
		BACKUP_CHARSET = currentConfiguration.getGenOpt().getCharset();
		BACKUP_GENVOLUME = currentConfiguration.getGenOpt().getGeneralVolume();
		BACKUP_EXCEPTION_ADVICES = currentConfiguration.getGenOpt().isExceptionAdvicesEnabled();
		BACKUP_AUTOCHECK_UPDATES = currentConfiguration.getGenOpt().isAutomaticUpdateOn();
		
		// write launcher configuration
		writeLauncherHidingOption(chckbxHideLauncherAt.isSelected());
		
		System.setProperty("file.encoding", currentConfiguration.getGenOpt().getCharset());
	}
	
	private void writeValues() {
		// write single values to file ini associated to currentConf
		currentConfiguration.writeSingleValue(INItializer.GEN_OPT, "userName", currentConfiguration.getGenOpt().getUserName());
		currentConfiguration.writeSingleValue(INItializer.GEN_OPT, "charset", currentConfiguration.getGenOpt().getCharset());
		currentConfiguration.writeSingleValue(INItializer.GEN_OPT, "generalVolume", currentConfiguration.getGenOpt().getGeneralVolume());
		currentConfiguration.writeSingleValue(INItializer.GEN_OPT, "exceptionAdvicesEnabled", currentConfiguration.getGenOpt().isExceptionAdvicesEnabled());
		currentConfiguration.writeSingleValue(INItializer.FILE_OPT, "flagAutosave", currentConfiguration.getFileOpt().isFlagAutosave());
		currentConfiguration.writeSingleValue(INItializer.FILE_OPT, "autosaveInterval", currentConfiguration.getFileOpt().autosaveInterval);
		
		// write launcher configuration
		writeLauncherHidingOption(chckbxHideLauncherAt.isSelected());
	}
	
	private void writeLauncherHidingOption(boolean hideLauncher) {
		try {
			String valueHidden = (hideLauncher)? "true" : "false";
			launcherConfiguration.put(IniFileManager.INI_APP_OPT, "flagHideNStartDirect", valueHidden );
			launcherConfiguration.store();
		} catch (IOException e) {
			logger.error("Exception happened writing launcher configuration!", e);
		}
	}
	
	private boolean readLauncherHidingOption() {
		String startDirectString = launcherConfiguration.get(IniFileManager.INI_APP_OPT, "flagHideNStartDirect");
		boolean startDirect = (startDirectString.equalsIgnoreCase("true"))? true : false;
		return startDirect;
	}
	
	public void initGuiFromConfig() {
		initializeValues();
		updateGUIComponents();
	}
	
	@Override
	public Dimension getDefaultDimension() {
		return new Dimension(870, 245);
	}
	
	public static GeneralOptionsFrame getInstance(INItializer currentConf) {
		return singletonInstance == null ? new GeneralOptionsFrame(currentConf) : singletonInstance;
	}
}

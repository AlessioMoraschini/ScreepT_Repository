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
package screept.gui.frames;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.Locale;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.log4j.Logger;
import org.ini4j.InvalidFileFormatException;
import org.ini4j.Wini;

import gui.commons.dialogutils.JOptionHelperExtended;
import gui.commons.frameutils.frame.arch.ParentFrame;
import gui.commons.frameutils.utils.GuiUtilsExtended;
import initializer.configs.impl.INItializer;
import initializer.configs.impl.om.FileOptions;
import maingUI.MainGUI;
import maingUI.MainGuiGeneralAppStarter;
import maingUI.MainGuiManager;
import net.miginfocom.swing.MigLayout;
import os.commons.utils.SysUtils;
import resources.GeneralConfig;
import resources.IconsPathConfigurator;
import resources.SoundsConfigurator;
import screept.launcher.utils.IniFileManager;
import various.common.light.gui.GUIStyelSettings;
import various.common.light.gui.GuiUtils;
import various.common.light.network.proxy.ProxySettingsGUI;
import various.common.light.om.SecurityLevel;
import various.common.light.utility.string.ListCharsets;
import various.common.light.utility.string.StringWorker.EOL;

public class GeneralOptionsFrame extends ParentFrame{
	private static final long serialVersionUID = 7396680036861927288L;

	// initialize Logger
	// GraphicPreferencesFrame logger
	static Logger logger = Logger.getLogger(GeneralOptionsFrame.class);

	public static GeneralOptionsFrame singletonInstance = null;

	// gui elements as fields
	private final JFrame thisFrame;
	private JTextField textFieldUname;
	private JCheckBox chckbxAutosave;
	private JComboBox<String> comboCharsets;
	private JComboBox<EOL> comboBoxDefEOL;
	private JComboBox<SecurityLevel> comboSecurityLevels;
	private JComboBox<Locale> comboLocale;

	private JCheckBox chckbxHideLauncherAt;
	private JCheckBox chckbxExceptionAdvicesEnabled;
	private JCheckBox chckbxAutomaticallyCheckFor;

	private JLabel lblVolume;
	private JLabel lblDynVolume;
	private JLabel lblLogLevelChosen;

	private JSpinner spinnerInterval;

	private JSlider sliderVolume;

	private JLabel lblPasswordSecurityCheck;
	private JLabel lblIntMinutes;
	private JButton btnResetValues;
	private JButton btnDiscard;
	private JButton btnUseSettings;
	private JButton btnSaveConf;

	// default values
	private Locale BACKUP_LOCALE;
	private int BACKUP_AUTOSAVE_INTERVAL;
	private boolean BACKUP_AUTOSAVE_FLAG;
	private boolean BACKUP_EXCEPTION_ADVICES;
	private boolean BACKUP_AUTOCHECK_UPDATES;
	private String BACKUP_CURRENT_USERNAME;
	private String BACKUP_CHARSET;
	private SecurityLevel BACKUP_SECURITY_LEVEL;
	private EOL BACKUP_DEFAULT_EOL;
	private float BACKUP_GENVOLUME;

	JOptionHelperExtended dialogHelper;

	// reference to configuration
	private INItializer currentConfiguration;
	private Wini launcherConfiguration;
	private JSeparator separator_1;
	private JLabel lblDefaultEOL;
	private JButton btnNetworkConfig;
	private JLabel lblLocale;
	private JCheckBox chckbxAutostart;
	private JButton buttonOpenAutostartDir;

	public GeneralOptionsFrame(INItializer currentConf) {
		super();

		isActive.set(true);

		logger.info(this.getClass().getName() + " Starting...");

		dialogHelper = new JOptionHelperExtended(this);

		// assign this frame to variable
		thisFrame = this;
		singletonInstance = this;

		// initialize launcher config
		currentConfiguration = currentConf;

		// SW window icon
		GuiUtilsExtended.setFrameIcon(IconsPathConfigurator.ICON_GEN_OPT, this);

		GuiUtilsExtended.showOnSameScreen(this, MainGUI.mainFrameScreept);

		try {
			launcherConfiguration = new Wini(new File(GeneralConfig.DEFAULT_LAUNCHER_INI_PATH));
		} catch (InvalidFileFormatException e1) {
			logger.error("Invlid launcher configuration format!\n", e1);
		} catch (IOException e1) {
			logger.error("IO error setting launcher configuration file", e1);
		}

		initializeValues();

		getContentPane().setBackground(Color.DARK_GRAY);
		getContentPane().setLayout(new MigLayout("", "[10px][10px][:110px:190px,grow][::90px][100px,grow][80px:n][100][100][110px:n]", "[:30:40px][10px:n][grow][][grow][grow][grow,bottom][:30px:35px,baseline]"));

		JPanel panel = new JPanel();
		panel.setForeground(Color.BLACK);
		panel.setBorder(new LineBorder(new Color(0, 0, 0), 2));
		panel.setBackground(Color.GRAY);
		getContentPane().add(panel, "cell 0 0 9 1,grow");
		panel.setLayout(new MigLayout("", "[220px:n][][90px:n][grow][100px:110px:160px,grow][][100px:110px:160px,grow]", "[grow]"));


		chckbxAutosave = new JCheckBox("Autosave configuration");
		panel.add(chckbxAutosave, "cell 0 0,alignx center");
		chckbxAutosave.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		chckbxAutosave.setToolTipText("Enable/Disable periodic autosave (also when exiting application) current configuration will be automatically saved");
		chckbxAutosave.setFont(new Font("SansSerif", Font.BOLD, 14));
		chckbxAutosave.setForeground(Color.LIGHT_GRAY);
		chckbxAutosave.setHorizontalAlignment(SwingConstants.CENTER);
		chckbxAutosave.setBackground(new Color(119, 136, 153));
		chckbxAutosave.setOpaque(false);

		JLabel lblIntervalInSeconds = new JLabel("Timeout :");
		panel.add(lblIntervalInSeconds, "cell 1 0");
		lblIntervalInSeconds.setForeground(Color.WHITE);
		lblIntervalInSeconds.setFont(new Font("SansSerif", Font.BOLD, 14));

		// update only local variable fields, INItializer will be updated on exit/save/use/discard
		spinnerInterval = new JSpinner();
		panel.add(spinnerInterval, "cell 2 0");
		spinnerInterval.setMinimumSize(new Dimension(80, 28));
		spinnerInterval.setFont(new Font("Segoe UI", Font.PLAIN, 17));
		spinnerInterval.setPreferredSize(new Dimension(70, 28));
		spinnerInterval.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		spinnerInterval.setModel(new SpinnerNumberModel(100, FileOptions.DEFAULT_MIN_AUTOSAVE_INTERVAL/1000, 3600000, 1));

//		 calculate minutes: interval in seconds / 60
		lblIntMinutes = new JLabel("(0 Minutes + 0 Seconds)");
		panel.add(lblIntMinutes, "cell 3 0");
		lblIntMinutes.setBackground(new Color(240, 248, 255));
		lblIntMinutes.setForeground(Color.LIGHT_GRAY);
		lblIntMinutes.setFont(new Font("SansSerif", Font.BOLD, 13));

		chckbxAutostart = new JCheckBox("Autostart");
		chckbxAutostart.setForeground(Color.LIGHT_GRAY);
		chckbxAutostart.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		chckbxAutostart.setToolTipText("Enable/Disable ScreepT to be automatically launched at System startup.");
		chckbxAutostart.setFont(new Font("SansSerif", Font.BOLD, 14));
		chckbxAutostart.setOpaque(false);
		panel.add(chckbxAutostart, "cell 4 0,alignx center");

		buttonOpenAutostartDir = new JButton("");
		buttonOpenAutostartDir.setIcon(new ImageIcon(IconsPathConfigurator.iconFolderPath));
		buttonOpenAutostartDir.setBackground(Color.DARK_GRAY);
		buttonOpenAutostartDir.setToolTipText("Open autostart folder in File System");
		buttonOpenAutostartDir.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		panel.add(buttonOpenAutostartDir, "cell 5 0, alignx left");

		lblLogLevelChosen = new JLabel("LogLevel: " + MainGuiGeneralAppStarter.LOG_LEVEL_AT_START);
		panel.add(lblLogLevelChosen, "cell 6 0,alignx right");
		lblLogLevelChosen.setForeground(new Color(255, 255, 255));
		lblLogLevelChosen.setFont(new Font("Tahoma", Font.BOLD, 13));

		JSeparator separator = new JSeparator();

		lblVolume = new JLabel("Volume:");
		getContentPane().add(lblVolume, "cell 0 2,alignx center");
		lblVolume.setForeground(new Color(255, 255, 255));
		lblVolume.setFont(new Font("SansSerif", Font.BOLD, 13));

		lblDynVolume = new JLabel("");
		getContentPane().add(lblDynVolume, "cell 1 2");
		lblDynVolume.setForeground(new Color(220, 239, 248));
		lblDynVolume.setFont(new Font("SansSerif", Font.BOLD, 13));

		sliderVolume = new JSlider(0, 100);
		getContentPane().add(sliderVolume, "cell 2 2 3 1,growx");

		JLabel lblUsername = new JLabel("Username");
		lblUsername.setForeground(Color.WHITE);
		lblUsername.setFont(new Font("Tahoma", Font.BOLD, 13));
		getContentPane().add(lblUsername, "cell 5 2,alignx right");

		textFieldUname = new JTextField();
		textFieldUname.setMinimumSize(new Dimension(12, 30));
		textFieldUname.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		textFieldUname.setToolTipText("Choose name used to customize messages and various dialogs and advices");
		textFieldUname.setHorizontalAlignment(SwingConstants.CENTER);
		textFieldUname.setFont(new Font("SansSerif", Font.PLAIN, 16));
		textFieldUname.setColumns(10);
		textFieldUname.setBorder(new LineBorder(Color.DARK_GRAY, 3));
		textFieldUname.setBackground(new Color(255, 255, 255));
		getContentPane().add(textFieldUname, "cell 6 2 3 1,growx,aligny center");

		separator_1 = new JSeparator();
		getContentPane().add(separator_1, "cell 0 3 9 1,growx");

		lblPasswordSecurityCheck = new JLabel("Password security:");
		lblPasswordSecurityCheck.setForeground(Color.WHITE);
		lblPasswordSecurityCheck.setFont(new Font("SansSerif", Font.BOLD, 13));
		getContentPane().add(lblPasswordSecurityCheck, "cell 0 4 2 1,alignx right,aligny center");

		lblLocale = new JLabel("Locale:");
		lblLocale.setForeground(Color.WHITE);
		lblLocale.setFont(new Font("SansSerif", Font.BOLD, 13));
		getContentPane().add(lblLocale, "cell 3 4,alignx trailing");

		JLabel lblDefaultCharset = new JLabel("Encoding:");
		lblDefaultCharset.setForeground(Color.WHITE);
		lblDefaultCharset.setFont(new Font("SansSerif", Font.BOLD, 13));
		getContentPane().add(lblDefaultCharset, "cell 5 4,alignx right");

		comboCharsets = new JComboBox<String>(ListCharsets.getCharsets());
		comboCharsets.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		comboCharsets.setMinimumSize(new Dimension(20, 30));
		comboCharsets.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		comboCharsets.setToolTipText("Choose general application Charset to use");
		getContentPane().add(comboCharsets, "cell 6 4 3 1,growx,aligny center");

		lblDefaultEOL = new JLabel("Default EOL:");
		lblDefaultEOL.setForeground(Color.WHITE);
		lblDefaultEOL.setFont(new Font("SansSerif", Font.BOLD, 13));
		getContentPane().add(lblDefaultEOL, "cell 0 5 2 1,alignx right");


//		comboLocale = new JComboBox<Locale>();
		comboLocale = new JComboBox<Locale>(getSupportedLocales());
		comboLocale.setFont(GeneralConfig.BUTTON_DEFAULT_FONT);
		comboLocale.setCursor(GuiUtilsExtended.CURSOR_HAND);
		comboLocale.setToolTipText("Choose general language used (not yet implemented)");
		comboLocale.setMinimumSize(new Dimension(20, 30));
		getContentPane().add(comboLocale, "cell 4 4,growx");

//		comboSecurityLevels = new JComboBox<SecurityLevel>();
		comboSecurityLevels = new JComboBox<SecurityLevel>(SecurityLevel.values());
		comboSecurityLevels.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		comboSecurityLevels.setMinimumSize(new Dimension(20, 30));
		comboSecurityLevels.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		comboSecurityLevels.setToolTipText("Choose security level used for passwords check");
		getContentPane().add(comboSecurityLevels, "cell 2 4,growx");

//		comboBoxDefEOL = new JComboBox<EOL>();
		comboBoxDefEOL = new JComboBox<EOL>(EOL.values());
		comboBoxDefEOL.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		comboBoxDefEOL.setMinimumSize(new Dimension(20, 30));
		comboBoxDefEOL.setToolTipText("Choose security level used for passwords check");
		comboBoxDefEOL.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		getContentPane().add(comboBoxDefEOL, "cell 2 5,growx");

		btnNetworkConfig = new JButton("");
		btnNetworkConfig.setToolTipText("Open network options configuration panel");
		btnNetworkConfig.setMinimumSize(new Dimension(35, 35));
		btnNetworkConfig.setIcon(new ImageIcon(IconsPathConfigurator.ICON_NETWORK));
		btnNetworkConfig.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		getContentPane().add(btnNetworkConfig, "cell 3 5,alignx center,aligny center");

		chckbxExceptionAdvicesEnabled = new JCheckBox("Exception Advices");
		getContentPane().add(chckbxExceptionAdvicesEnabled, "cell 4 5,alignx center,aligny center");
		chckbxExceptionAdvicesEnabled.setOpaque(false);
		chckbxExceptionAdvicesEnabled.setForeground(Color.LIGHT_GRAY);
		chckbxExceptionAdvicesEnabled.setToolTipText("Enable/Disable error dialog in case of general warnings detected by the application");
		chckbxExceptionAdvicesEnabled.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		chckbxExceptionAdvicesEnabled.setFont(new Font("SansSerif", Font.BOLD, 14));

		chckbxHideLauncherAt = new JCheckBox("Hide Launcher at start");
		getContentPane().add(chckbxHideLauncherAt, "cell 5 5 2 1,alignx center,aligny center");
		chckbxHideLauncherAt.setToolTipText("When checked, launcher will not be shown on startup");
		chckbxHideLauncherAt.setOpaque(false);
		chckbxHideLauncherAt.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		chckbxHideLauncherAt.setSelected(readLauncherHidingOption());
		chckbxHideLauncherAt.setHorizontalAlignment(SwingConstants.CENTER);
		chckbxHideLauncherAt.setForeground(Color.LIGHT_GRAY);
		chckbxHideLauncherAt.setFont(new Font("SansSerif", Font.BOLD, 14));
		chckbxHideLauncherAt.setBackground(new Color(119, 136, 153));

		chckbxAutomaticallyCheckFor = new JCheckBox("Auto-check updates");
		getContentPane().add(chckbxAutomaticallyCheckFor, "cell 7 5 2 1,alignx center,aligny center");
		chckbxAutomaticallyCheckFor.setOpaque(false);
		chckbxAutomaticallyCheckFor.setForeground(Color.LIGHT_GRAY);
		chckbxAutomaticallyCheckFor.setToolTipText("Enable/Disable automatic check for updates at start");
		chckbxAutomaticallyCheckFor.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		chckbxAutomaticallyCheckFor.setFont(new Font("SansSerif", Font.BOLD, 15));
		getContentPane().add(separator, "cell 0 6 9 1,growx");
		btnResetValues = new JButton("Reset initial values");
		btnResetValues.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnResetValues.setFont(new Font("SansSerif", Font.BOLD, 12));
		btnResetValues.setBackground(new Color(240, 230, 140));
		getContentPane().add(btnResetValues, "cell 0 7 3 1,growx");

		btnSaveConf = new JButton("Save Values");
		btnSaveConf.setToolTipText("Close this dialog and use current settings with explicit save on phisical file");
		btnSaveConf.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnSaveConf.setForeground(Color.BLACK);
		btnSaveConf.setFont(new Font("SansSerif", Font.BOLD, 12));
		btnSaveConf.setBackground(new Color(169, 169, 169));
		getContentPane().add(btnSaveConf, "cell 6 7,growx");

		btnUseSettings = new JButton("Use");
		btnUseSettings.setToolTipText("Close this dialog and use current settings without explicit save on phisical file");
		btnUseSettings.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnUseSettings.setForeground(Color.BLACK);
		btnUseSettings.setFont(new Font("SansSerif", Font.BOLD, 12));
		btnUseSettings.setBackground(new Color(169, 169, 169));
		getContentPane().add(btnUseSettings, "cell 7 7,growx");

		btnDiscard = new JButton("Discard");
		btnDiscard.setToolTipText("Close this dialog and discard every change");
		btnDiscard.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnDiscard.setForeground(new Color(0, 0, 0));
		btnDiscard.setFont(new Font("SansSerif", Font.BOLD, 12));
		btnDiscard.setBackground(new Color(205, 92, 92));
		getContentPane().add(btnDiscard, "cell 8 7,growx");

		addHandlers();

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				updateGUIComponents();
			}
		});

		logger.info(this.getClass().getName() + " Started");

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				GUIStyelSettings.applyNimbusSize(thisFrame, currentConfiguration.getGuiOpt().nimbusSize);
			}
		});
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

		// Autostart checbox handler
		chckbxAutostart.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				addOrRemoveFromAutostart(chckbxAutostart.isSelected(), true);
				refreshCheckBoxForeground(chckbxAutostart);
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

		btnNetworkConfig.addActionListener((e) -> {
			GeneralConfig.reloadProperties();
			ProxySettingsGUI.frameIcon = new ImageIcon(IconsPathConfigurator.ICON_NETWORK);
			new ProxySettingsGUI(ModalityType.TOOLKIT_MODAL, false, GeneralConfig.DEF_PROXY_CONFIG_STRING);
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
			@Override
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

		// SECURITY LEVEL SELECTOR HANDLER
		comboSecurityLevels.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SecurityLevel security = (SecurityLevel)comboSecurityLevels.getSelectedItem();
				currentConfiguration.getAesOpt().setSecurityLevel(security);
				logger.info("Security level changed: " + security);
			}
		});

		// DEFAULT EOL SELECTOR HANDLER
		comboBoxDefEOL.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				EOL defaultEol = (EOL)comboBoxDefEOL.getSelectedItem();
				currentConfiguration.getTextEditorOpt().setEol(defaultEol);
				logger.info("Default EOL changed: " + defaultEol);
			}
		});

		// COMBO LOCALES HANDLER
		comboLocale.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					Locale locale = (Locale) comboLocale.getSelectedItem();
					currentConfiguration.getGenOpt().setLocale(locale);
				} catch (Exception e1) {
					logger.error("Error setting locale", e1);
				}
			}
		});

		// USERNAME HANDLER
		textFieldUname.getDocument().addDocumentListener(new DocumentListener() {
			  @Override
			public void changedUpdate(DocumentEvent e) {
			  	  resetCurrentIni();
			  }
			  @Override
			public void removeUpdate(DocumentEvent e) {
				  resetCurrentIni();
			  }
			  @Override
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
			@Override
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

		// OPEN AUTOSTART FOLDER BUTTON HANDLER
		buttonOpenAutostartDir.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				SysUtils.viewAutostartFolder(false);
			}
		});

		// DISCARD BUTTON HANDLER
		btnDiscard.addActionListener(new ActionListener() {
			@Override
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
			@Override
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
			@Override
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
		refreshCheckBoxForeground(chckbxAutostart);
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
		BACKUP_SECURITY_LEVEL = currentConfiguration.getAesOpt().getSecurityLevel();
		BACKUP_DEFAULT_EOL = currentConfiguration.getTextEditorOpt().getEol();
		BACKUP_GENVOLUME = currentConfiguration.getGenOpt().getGeneralVolume();
		BACKUP_EXCEPTION_ADVICES = currentConfiguration.getGenOpt().isExceptionAdvicesEnabled();
		BACKUP_LOCALE = currentConfiguration.getGenOpt().getLocale();
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
    	comboSecurityLevels.setSelectedItem(currentConfiguration.getAesOpt().getSecurityLevel());
    	comboBoxDefEOL.setSelectedItem(currentConfiguration.getTextEditorOpt().getEol());
    	comboLocale.setSelectedItem(currentConfiguration.getGenOpt().getLocale());
    	updateComboLocale();
    	Locale.setDefault(currentConfiguration.getGenOpt().getLocale());
    	chckbxHideLauncherAt.setSelected(readLauncherHidingOption());
		chckbxAutostart.setSelected(SysUtils.isPresentInAutostart(GeneralConfig.MAIN_APP_OS_DEPENDENT_LAUNCHER));

    	refreshCheckBoxForeground();
	}

	public static Locale[] getSupportedLocales(){
		Locale[] supportedLocales = new Locale[2];
		supportedLocales[0] = Locale.ENGLISH;
		supportedLocales[1] = Locale.ITALIAN;

		return supportedLocales;
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

	private void updateComboLocale() {
		for (int i = 0; i < comboLocale.getItemCount(); i++) {
			Locale currentScanLocale = (comboLocale.getItemAt(i));
			if (currentScanLocale.equals(currentConfiguration.getGenOpt().getLocale())) {
				comboLocale.setSelectedIndex(i);
				break;
			}
		}
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
		currentConfiguration.getAesOpt().setSecurityLevel(BACKUP_SECURITY_LEVEL);
		currentConfiguration.getTextEditorOpt().setEol(BACKUP_DEFAULT_EOL);
		currentConfiguration.getGenOpt().setGeneralVolume(BACKUP_GENVOLUME);
		currentConfiguration.getGenOpt().setExceptionAdvicesEnabled(BACKUP_EXCEPTION_ADVICES);
		currentConfiguration.getGenOpt().setAutomaticUpdateOn(BACKUP_AUTOCHECK_UPDATES);
		currentConfiguration.getGenOpt().setLocale(BACKUP_LOCALE);

		// reset INItializer reference currentConfiguration too
		System.setProperty("file.encoding", BACKUP_CHARSET);
	}

	private void addOrRemoveFromAutostart(boolean flagAutostartActive, boolean showDialogs) {
		boolean advicesFlagBackup = dialogHelper.enabledAdvices;
		dialogHelper.enabledAdvices = showDialogs;
		try {
			if(flagAutostartActive) {
				String[] outcome = SysUtils.addToAutostart(GeneralConfig.MAIN_APP_OS_DEPENDENT_LAUNCHER);
				if(outcome[1].isEmpty()) {
					dialogHelper.info(outcome[0]);
					chckbxAutostart.setSelected(true);
				} else {
					dialogHelper.error(GuiUtils.encapsulateInHtml(outcome[1] + "<br><br> Try to restart application with Administration rights."), "Error");
					chckbxAutostart.setSelected(false);
				}
			} else {
				if(!SysUtils.removeFromAutostart(GeneralConfig.MAIN_APP_OS_DEPENDENT_LAUNCHER)) {
					chckbxAutostart.setSelected(true);
					boolean tryAsAdmin = dialogHelper.yesOrNo(GuiUtils.encapsulateInHtml("Cannot remove from autostart menu <br><br> Try to restart application with Administration rights?"), "Error");
					if(tryAsAdmin)
						MainGuiManager.restartApplication(tryAsAdmin);
				};
			}
		} catch (IOException e) {
			String msg = "Exception happened while " +
					(flagAutostartActive ? "adding " : "removing ") +
					GeneralConfig.MAIN_APP_OS_DEPENDENT_LAUNCHER +
					" to autostart!";
			logger.error(msg, e);
			dialogHelper.warn(msg, "Cannot " +
					(flagAutostartActive? "add to" : "remove from") +
					" autostart");
		} finally {
			dialogHelper.enabledAdvices = advicesFlagBackup;
		}
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
		BACKUP_SECURITY_LEVEL = currentConfiguration.getAesOpt().getSecurityLevel();
		BACKUP_DEFAULT_EOL = currentConfiguration.getTextEditorOpt().getEol();
		BACKUP_GENVOLUME = currentConfiguration.getGenOpt().getGeneralVolume();
		BACKUP_EXCEPTION_ADVICES = currentConfiguration.getGenOpt().isExceptionAdvicesEnabled();
		BACKUP_AUTOCHECK_UPDATES = currentConfiguration.getGenOpt().isAutomaticUpdateOn();
		BACKUP_LOCALE = currentConfiguration.getGenOpt().getLocale();

		// write launcher configuration
		writeLauncherHidingOption(chckbxHideLauncherAt.isSelected());

		System.setProperty("file.encoding", currentConfiguration.getGenOpt().getCharset());
	}

	private void writeValues() {
		// write single values to file ini associated to currentConf
		currentConfiguration.writeSingleValue(INItializer.GEN_OPT, "locale", currentConfiguration.getGenOpt().getLocale().toString());
		currentConfiguration.writeSingleValue(INItializer.GEN_OPT, "userName", currentConfiguration.getGenOpt().getUserName());
		currentConfiguration.writeSingleValue(INItializer.GEN_OPT, "charset", currentConfiguration.getGenOpt().getCharset());
		currentConfiguration.writeSingleValue(INItializer.GEN_OPT, "generalVolume", currentConfiguration.getGenOpt().getGeneralVolume());
		currentConfiguration.writeSingleValue(INItializer.GEN_OPT, "exceptionAdvicesEnabled", currentConfiguration.getGenOpt().isExceptionAdvicesEnabled());
		currentConfiguration.writeSingleValue(INItializer.GEN_OPT, "automaticUpdateOn", currentConfiguration.getGenOpt().isAutomaticUpdateOn());
		currentConfiguration.writeSingleValue(INItializer.FILE_OPT, "flagAutosave", currentConfiguration.getFileOpt().isFlagAutosave());
		currentConfiguration.writeSingleValue(INItializer.FILE_OPT, "autosaveInterval", currentConfiguration.getFileOpt().autosaveInterval);
		currentConfiguration.writeSingleValue(INItializer.AES_OPT, "securityLevel", currentConfiguration.getAesOpt().getSecurityLevel().name());
		currentConfiguration.writeSingleValue(INItializer.TEXT_EDITOR_OPT, "EOL", currentConfiguration.getTextEditorOpt().getEol().name());

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
		return new Dimension(1020, 270);
	}

	public static GeneralOptionsFrame getInstance(INItializer currentConf) {
		return singletonInstance == null ? new GeneralOptionsFrame(currentConf) : singletonInstance;
	}
}

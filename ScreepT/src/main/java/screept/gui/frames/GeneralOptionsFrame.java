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
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.log4j.Logger;
import org.ini4j.InvalidFileFormatException;
import org.ini4j.Wini;

import gui.commons.frameutils.frame.arch.ParentFrame;
import gui.commons.frameutils.utils.GuiUtilsExtended;
import gui.commons.handlers.AlwaysOnTopManager;
import gui.commons.panel.options.OptionsHeader;
import gui.commons.panel.options.OptionsPanel;
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

/**
 * Generic options: searchable, collapsible sections (same structure as {@link GraphicPreferencesFrame}).
 * Changes are live on the current configuration; the BACKUP_* fields restore them on discard or close,
 * Use keeps them (updating the backups), Save also writes them in the INI file.
 */
public class GeneralOptionsFrame extends ParentFrame{
	private static final long serialVersionUID = 7396680036861927288L;

	// initialize Logger
	// GraphicPreferencesFrame logger
	static Logger logger = Logger.getLogger(GeneralOptionsFrame.class);

	private static final int MAX_SCREEN_PERCENT = 90;

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
	private JCheckBox chckbxAutostart;

	private JLabel lblDynVolume;
	private JLabel lblLogLevelChosen;
	private JLabel lblIntMinutes;

	private JSpinner spinnerInterval;

	private JSlider sliderVolume;

	private JButton btnNetworkConfig;
	private JButton buttonOpenAutostartDir;
	private JButton btnResetValues;
	private JButton btnDiscard;
	private JButton btnUseSettings;
	private JButton btnSaveConf;
	private JPanel footerPanel;
	private OptionsPanel optionsPanel;

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

	// reference to configuration
	private INItializer currentConfiguration;
	private Wini launcherConfiguration;

	public GeneralOptionsFrame(INItializer currentConf) {
		super();

		isActive.set(true);

		logger.info(this.getClass().getName() + " Starting...");

		// assign this frame to variable
		thisFrame = this;
		singletonInstance = this;

		// initialize launcher config
		currentConfiguration = currentConf;

		// SW window icon
		GuiUtilsExtended.setFrameIcon(IconsPathConfigurator.ICON_GEN_OPT, this);

		GuiUtilsExtended.showOnSameScreen(this, MainGUI.mainFrameScreept);
		setResizable(true);
		AlwaysOnTopManager.keepAboveApplication(this);

		try {
			launcherConfiguration = new Wini(new File(GeneralConfig.DEFAULT_LAUNCHER_INI_PATH));
		} catch (InvalidFileFormatException e1) {
			logger.error("Invlid launcher configuration format!\n", e1);
		} catch (IOException e1) {
			logger.error("IO error setting launcher configuration file", e1);
		}

		initializeValues();

		createControls();
		createFooter();
		buildLayout();

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

	/*
	 * VIEW CONSTRUCTION: option controls (models and listeners are bound in addHandlers/updateGUIComponents),
	 * footer buttons, then the presentation (sections, search). The data model is never touched here.
	 */

	private void createControls() {

		textFieldUname = new JTextField(16);
		textFieldUname.setToolTipText("Choose name used to customize messages and various dialogs and advices");

		comboLocale = new JComboBox<Locale>(getSupportedLocales());
		comboLocale.setToolTipText("Choose general language used (not yet implemented)");

		sliderVolume = new JSlider(0, 100);
		sliderVolume.setOpaque(false);
		sliderVolume.setToolTipText("Volume of the application sounds");
		lblDynVolume = new JLabel("");

		chckbxExceptionAdvicesEnabled = newCheckBox("Exception advices",
				"Enable/Disable error dialog in case of general warnings detected by the application");

		comboCharsets = new JComboBox<String>(ListCharsets.getCharsets());
		comboCharsets.setMaximumRowCount(20);
		comboCharsets.setToolTipText("Choose general application Charset to use");

		comboBoxDefEOL = new JComboBox<EOL>(EOL.values());
		comboBoxDefEOL.setToolTipText("Line ending used for new files");

		chckbxAutosave = newCheckBox("every",
				"Enable/Disable periodic autosave (also when exiting application) current configuration will be automatically saved");
		spinnerInterval = new JSpinner(new SpinnerNumberModel(100, FileOptions.DEFAULT_MIN_AUTOSAVE_INTERVAL/1000, 3600000, 1));
		spinnerInterval.setToolTipText("Autosave interval, in seconds");
		lblIntMinutes = new JLabel("(0 Minutes + 0 Seconds)");

		comboSecurityLevels = new JComboBox<SecurityLevel>(SecurityLevel.values());
		comboSecurityLevels.setToolTipText("Choose security level used for passwords check");

		btnNetworkConfig = new JButton("Proxy settings...");
		btnNetworkConfig.setIcon(new ImageIcon(IconsPathConfigurator.ICON_NETWORK));
		btnNetworkConfig.setToolTipText("Open network options configuration panel");

		chckbxAutostart = newCheckBox("Launch at system startup", "Enable/Disable ScreepT to be automatically launched at System startup.");
		buttonOpenAutostartDir = new JButton("Open folder");
		buttonOpenAutostartDir.setIcon(new ImageIcon(IconsPathConfigurator.iconFolderPath));
		buttonOpenAutostartDir.setToolTipText("Open autostart folder in File System");
		chckbxHideLauncherAt = newCheckBox("Hide launcher at start", "When checked, launcher will not be shown on startup");
		chckbxHideLauncherAt.setSelected(readLauncherHidingOption());
		chckbxAutomaticallyCheckFor = newCheckBox("Auto-check updates", "Enable/Disable automatic check for updates at start");

		lblLogLevelChosen = new JLabel(String.valueOf(MainGuiGeneralAppStarter.LOG_LEVEL_AT_START));
		lblLogLevelChosen.setToolTipText("Log level chosen at application start");

		for (JComponent control : new JComponent[] { comboLocale, sliderVolume, comboCharsets, comboBoxDefEOL, spinnerInterval,
				comboSecurityLevels, btnNetworkConfig, buttonOpenAutostartDir }) {
			control.setCursor(GuiUtilsExtended.CURSOR_HAND);
		}
	}

	private JCheckBox newCheckBox(String text, String toolTip) {
		JCheckBox checkBox = new JCheckBox(text);
		checkBox.setOpaque(false);
		checkBox.setCursor(GuiUtilsExtended.CURSOR_HAND);
		checkBox.setToolTipText(toolTip);
		return checkBox;
	}

	private void createFooter() {
		btnResetValues = newFooterButton("Reset initial values", null, new Color(240, 230, 140));
		btnSaveConf = newFooterButton("Save Values", "Close this dialog and use current settings with explicit save on phisical file", new Color(169, 169, 169));
		btnUseSettings = newFooterButton("Use", "Close this dialog and use current settings without explicit save on phisical file", new Color(169, 169, 169));
		btnDiscard = newFooterButton("Discard", "Close this dialog and discard every change", new Color(205, 92, 92));

		footerPanel = new JPanel(new MigLayout("insets 10 18 12 18", "[][grow][][][]"));
		footerPanel.add(btnResetValues, "cell 0 0");
		footerPanel.add(btnSaveConf, "cell 2 0");
		footerPanel.add(btnUseSettings, "cell 3 0");
		footerPanel.add(btnDiscard, "cell 4 0");
	}

	private JButton newFooterButton(String text, String toolTip, Color background) {
		JButton button = new JButton(text);
		button.setToolTipText(toolTip);
		button.setMinimumSize(new Dimension(30, 35));
		button.setCursor(GuiUtilsExtended.CURSOR_HAND);
		button.setFont(button.getFont().deriveFont(Font.BOLD, button.getFont().getSize2D() + 2f));
		button.setForeground(Color.BLACK);
		button.setBackground(background);
		return button;
	}

	/** Presentation only: searchable sections around the option controls */
	private void buildLayout() {

		optionsPanel = new OptionsPanel();
		optionsPanel.addSection("General", "User name, language and sounds")
				.addRow("User name", textFieldUname)
				.addRow("Language", comboLocale)
				.addRow("Volume", sliderVolume, lblDynVolume)
				.addCheckBoxes(chckbxExceptionAdvicesEnabled);
		optionsPanel.addSection("Files", "Encoding, line endings and configuration autosave")
				.addRow("Encoding", comboCharsets)
				.addRow("Default EOL", comboBoxDefEOL)
				.addRow("Configuration autosave", chckbxAutosave, spinnerInterval, new JLabel("seconds"), lblIntMinutes);
		optionsPanel.addSection("Security and network", "Password checks and proxy")
				.addRow("Password security", comboSecurityLevels)
				.addRow("Network", btnNetworkConfig);
		optionsPanel.addSection("Startup and updates", "Autostart, launcher and updates check")
				.addRow("Autostart", chckbxAutostart, buttonOpenAutostartDir)
				.addRow("Log level", lblLogLevelChosen)
				.addCheckBoxes(chckbxHideLauncherAt, chckbxAutomaticallyCheckFor);

		JScrollPane scroller = new JScrollPane(optionsPanel);
		scroller.setBorder(BorderFactory.createEmptyBorder());
		scroller.getVerticalScrollBar().setUnitIncrement(16);

		getContentPane().setLayout(new MigLayout("fill, insets 0, gap 0", "[grow,fill]", "[][grow,fill][]"));
		getContentPane().add(new OptionsHeader("Generic options", optionsPanel, this), "growx, wrap");
		getContentPane().add(scroller, "grow, wrap");
		getContentPane().add(footerPanel, "growx");
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
			}
		});

		// AUTOSAVE CHECKBOX HANDLER
		chckbxAutosave.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				boolean AUTOSAVE_FLAG = chckbxAutosave.isSelected();
				currentConfiguration.getFileOpt().setFlagAutosave(AUTOSAVE_FLAG);
				refreshAutosaveControls();
			}
		});

		// EXCFEPTION ADVICE ON/OFF HANDLER
		chckbxExceptionAdvicesEnabled.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean exceptionAdvices = chckbxExceptionAdvicesEnabled.isSelected();
				currentConfiguration.getGenOpt().setExceptionAdvicesEnabled(exceptionAdvices);
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
			}
		});

		// HIDE LAUNCHER ON/OFF: handled in ad-hoc method when saving-resetting

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
					// cancelled by user: no message needed
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

	/** The interval is editable only with autosave enabled */
	private void refreshAutosaveControls() {
		spinnerInterval.setEnabled(chckbxAutosave.isSelected());
		lblIntMinutes.setEnabled(chckbxAutosave.isSelected());
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

		refreshAutosaveControls();
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
	/** Fits all the options without scrolling, up to 90% of the main window monitor (maximize stays available) */
	public Dimension getDefaultDimension() {
		return OptionsPanel.fitWindowSize(this, getMinimumDimension(), MAX_SCREEN_PERCENT,
				MainGUI.mainFrameScreept != null ? MainGUI.mainFrameScreept : this);
	}

	@Override
	public Dimension getMinimumDimension() {
		return new Dimension(620, 420);
	}

	public static GeneralOptionsFrame getInstance(INItializer currentConf) {
		return singletonInstance == null ? new GeneralOptionsFrame(currentConf) : singletonInstance;
	}
}

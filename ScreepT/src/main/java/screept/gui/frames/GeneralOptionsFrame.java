/**
 *
 * =========================================================================================
 *  Copyright (C) 2019-2020
 *
 *  AM-Design-Development - (Alessio Moraschini)
 * =========================================================================================
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package screept.gui.frames;

import various.common.light.i18n.I18n;
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
import gui.commons.panel.options.OptionHelp;
import gui.commons.panel.options.OptionsPanel;
import initializer.configs.impl.INItializer;
import initializer.configs.impl.om.FileOptions;
import maingUI.MainGUI;
import maingUI.MainGuiGeneralAppStarter;
import maingUI.MainGuiManager;
import screept.general.utils.AutostartShortcut;
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
	private JComboBox<EOL> comboBoxDefEOL;
	private JCheckBox chckbxKeepEol;
	private boolean BACKUP_KEEP_EOL;
	private JComboBox<SecurityLevel> comboSecurityLevels;
	private JComboBox<Locale> comboLocale;

	private JCheckBox chckbxHideLauncherAt;
	private JCheckBox chckbxExceptionAdvicesEnabled;
	private JCheckBox chckbxAutomaticallyCheckFor;
	private JCheckBox chckbxAutomaticallyCheckPlugins;
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
	private String BACKUP_LANGUAGE = "en";
	private int BACKUP_AUTOSAVE_INTERVAL;
	private boolean BACKUP_AUTOSAVE_FLAG;
	private boolean BACKUP_EXCEPTION_ADVICES;
	private boolean BACKUP_AUTOCHECK_UPDATES;
	private boolean BACKUP_AUTOCHECK_PLUGIN_UPDATES;
	private String BACKUP_CURRENT_USERNAME;
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
		textFieldUname.setToolTipText(I18n.t("app.choose_name_used_to_customize_messages"));

		comboLocale = new JComboBox<Locale>(getSupportedLocales());
		comboLocale.setToolTipText(I18n.t("app.choose_the_language_of_the_application"));
		comboLocale.setRenderer(new javax.swing.DefaultListCellRenderer() {
			private static final long serialVersionUID = 1L;

			@Override
			public java.awt.Component getListCellRendererComponent(javax.swing.JList<?> list, Object value, int index, boolean selected, boolean focus) {
				Object shown = value instanceof Locale ? various.common.light.i18n.I18n.getLanguageInfo(((Locale) value).getLanguage()).nativeName : value;
				return super.getListCellRendererComponent(list, shown, index, selected, focus);
			}
		});

		sliderVolume = new JSlider(0, 100);
		sliderVolume.setOpaque(false);
		sliderVolume.setToolTipText(I18n.t("app.volume_of_the_application_sounds"));
		lblDynVolume = new JLabel("");

		chckbxExceptionAdvicesEnabled = newCheckBox(I18n.t("app.exception_advices"),
				I18n.t("app.enable_disable_error_dialog_in_case"));

		comboBoxDefEOL = new JComboBox<EOL>(EOL.values());
		comboBoxDefEOL.setToolTipText(I18n.t("app.line_ending_used_for_new_files"));
		chckbxKeepEol = newCheckBox(I18n.t("app.keep_original_eol"), I18n.t("app.keep_original_eol_tip"));

		chckbxAutosave = newCheckBox(I18n.t("app.every"),
				I18n.t("app.enable_disable_periodic_autosave_also_when"));
		spinnerInterval = new JSpinner(new SpinnerNumberModel(100, FileOptions.DEFAULT_MIN_AUTOSAVE_INTERVAL/1000, 3600000, 1));
		spinnerInterval.setToolTipText(I18n.t("app.autosave_interval_in_seconds"));
		lblIntMinutes = new JLabel("(0 Minutes + 0 Seconds)");

		comboSecurityLevels = new JComboBox<SecurityLevel>(SecurityLevel.values());
		comboSecurityLevels.setToolTipText(I18n.t("app.choose_security_level_used_for_passwords"));

		btnNetworkConfig = new JButton(I18n.t("app.proxy_settings"));
		btnNetworkConfig.setIcon(new ImageIcon(IconsPathConfigurator.ICON_NETWORK));
		btnNetworkConfig.setToolTipText(I18n.t("app.open_network_options_configuration_panel"));

		chckbxAutostart = newCheckBox(I18n.t("app.launch_at_system_startup"), I18n.t("app.enable_disable_screept_to_be_automatically"));
		buttonOpenAutostartDir = new JButton(I18n.t("app.open_folder"));
		buttonOpenAutostartDir.setIcon(new ImageIcon(IconsPathConfigurator.iconFolderPath));
		buttonOpenAutostartDir.setToolTipText(I18n.t("app.open_autostart_folder_in_file_system"));
		chckbxHideLauncherAt = newCheckBox(I18n.t("common.hide_launcher_at_start"), I18n.t("app.when_checked_launcher_will_not_be"));
		chckbxHideLauncherAt.setSelected(readLauncherHidingOption());
		chckbxAutomaticallyCheckFor = newCheckBox(I18n.t("app.auto_check_updates"), I18n.t("app.enable_disable_automatic_check_for_updates"));
		chckbxAutomaticallyCheckPlugins = newCheckBox(I18n.t("app.auto_check_plugin_updates"), I18n.t("app.auto_check_plugin_updates_tip"));

		lblLogLevelChosen = new JLabel(String.valueOf(MainGuiGeneralAppStarter.LOG_LEVEL_AT_START));
		lblLogLevelChosen.setToolTipText(I18n.t("app.log_level_chosen_at_application_start"));

		for (JComponent control : new JComponent[] { comboLocale, sliderVolume, comboBoxDefEOL, spinnerInterval,
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
		btnResetValues = newFooterButton(I18n.t("app.reset_initial_values"), null, new Color(240, 230, 140));
		btnSaveConf = newFooterButton(I18n.t("app.save_values"), I18n.t("app.close_this_dialog_and_use_current"), new Color(169, 169, 169));
		btnUseSettings = newFooterButton("Use", I18n.t("app.close_this_dialog_and_use_current_2"), new Color(169, 169, 169));
		btnDiscard = newFooterButton(I18n.t("common.discard"), I18n.t("app.close_this_dialog_and_discard_every"), new Color(205, 92, 92));

		footerPanel = new JPanel(new MigLayout("insets 10 18 12 18", "[][grow][][][]"));
		footerPanel.add(btnResetValues, "cell 0 0");
		footerPanel.add(btnSaveConf, "cell 2 0");
		footerPanel.add(btnUseSettings, "cell 3 0");
		footerPanel.add(btnDiscard, "cell 4 0");

		OptionHelp.attach(I18n.t("app.reset_initial_values"), I18n.t("app.brings_every_option_of_this_window"), btnResetValues);
		OptionHelp.attach(I18n.t("app.save_values"), I18n.t("app.closes_the_window_keeps_the_current"), btnSaveConf);
		OptionHelp.attach("Use", I18n.t("app.closes_the_window_and_uses_the"), btnUseSettings);
		OptionHelp.attach(I18n.t("common.discard"), I18n.t("app.closes_the_window_and_drops_every"), btnDiscard);
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
		optionsPanel.setHelpTexts(OptionDescriptions.general());
		optionsPanel.addSection(I18n.t("app.general"), I18n.t("app.user_name_language_and_sounds"))
				.addRow(I18n.t("app.user_name"), textFieldUname)
				.addRow(I18n.t("app.language"), comboLocale)
				.addRow(I18n.t("app.volume"), sliderVolume, lblDynVolume)
				.addCheckBoxes(chckbxExceptionAdvicesEnabled);
		optionsPanel.addSection(I18n.t("app.files"), I18n.t("app.line_endings_and_configuration_autosave"))
				.addRow(I18n.t("app.default_eol"), comboBoxDefEOL)
				.addCheckBoxes(chckbxKeepEol)
				.addRow(I18n.t("app.configuration_autosave"), chckbxAutosave, spinnerInterval, new JLabel(I18n.t("app.seconds")), lblIntMinutes);
		optionsPanel.addSection(I18n.t("app.security_and_network"), I18n.t("app.password_checks_and_proxy"))
				.addRow(I18n.t("app.password_security"), comboSecurityLevels)
				.addRow(I18n.t("app.network"), btnNetworkConfig);
		optionsPanel.addSection(I18n.t("app.startup_and_updates"), I18n.t("app.autostart_launcher_and_updates_check"))
				.addRow(I18n.t("app.autostart"), chckbxAutostart, buttonOpenAutostartDir)
				.addRow(I18n.t("app.log_level"), lblLogLevelChosen)
				.addCheckBoxes(chckbxHideLauncherAt, chckbxAutomaticallyCheckFor, chckbxAutomaticallyCheckPlugins);

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

		// AUTO PLUGIN UPDATES ON/OFF
		chckbxAutomaticallyCheckPlugins.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				currentConfiguration.getGenOpt().setAutomaticPluginUpdateOn(chckbxAutomaticallyCheckPlugins.isSelected());
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

		chckbxKeepEol.addActionListener(e -> currentConfiguration.getTextEditorOpt().keepOriginalEol = chckbxKeepEol.isSelected());

		// COMBO LOCALES HANDLER
		comboLocale.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					Locale locale = (Locale) comboLocale.getSelectedItem();
					if (locale.getLanguage().equals(various.common.light.i18n.I18n.normalize(currentConfiguration.getGenOpt().getLanguage()))) {
						return; // the combo was only refreshed from the configuration: nothing to remember (and nothing to write to the file)
					}
					currentConfiguration.getGenOpt().setLanguage(locale.getLanguage());
					various.common.light.i18n.I18n.setLanguage(locale.getLanguage());
					currentConfiguration.writeCurrentConfigIniFile(); // the language is remembered at once
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
					dialogHelper.info(I18n.t("app.general_options_saved"), "Saved!");

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
		BACKUP_SECURITY_LEVEL = currentConfiguration.getAesOpt().getSecurityLevel();
		BACKUP_DEFAULT_EOL = currentConfiguration.getTextEditorOpt().getEol();
		BACKUP_KEEP_EOL = currentConfiguration.getTextEditorOpt().keepOriginalEol;
		BACKUP_GENVOLUME = currentConfiguration.getGenOpt().getGeneralVolume();
		BACKUP_EXCEPTION_ADVICES = currentConfiguration.getGenOpt().isExceptionAdvicesEnabled();
		BACKUP_LOCALE = currentConfiguration.getGenOpt().getLocale();
		BACKUP_LANGUAGE = currentConfiguration.getGenOpt().getLanguage();
		BACKUP_AUTOCHECK_UPDATES = currentConfiguration.getGenOpt().isAutomaticUpdateOn();
		BACKUP_AUTOCHECK_PLUGIN_UPDATES = currentConfiguration.getGenOpt().isAutomaticPluginUpdateOn();
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
    	chckbxAutomaticallyCheckPlugins.setSelected(currentConfiguration.getGenOpt().isAutomaticPluginUpdateOn());
    	comboSecurityLevels.setSelectedItem(currentConfiguration.getAesOpt().getSecurityLevel());
    	comboBoxDefEOL.setSelectedItem(currentConfiguration.getTextEditorOpt().getEol());
		chckbxKeepEol.setSelected(currentConfiguration.getTextEditorOpt().keepOriginalEol);
comboLocale.setSelectedItem(new Locale(various.common.light.i18n.I18n.normalize(currentConfiguration.getGenOpt().getLanguage())));
    	updateComboLocale();
    	Locale.setDefault(currentConfiguration.getGenOpt().getLocale());
    	chckbxHideLauncherAt.setSelected(readLauncherHidingOption());
		chckbxAutostart.setSelected(AutostartShortcut.isPresent());

		refreshAutosaveControls();
	}

	public static Locale[] getSupportedLocales(){
		Locale[] supportedLocales = new Locale[various.common.light.i18n.I18n.LANGUAGES.size()];
		for (int i = 0; i < supportedLocales.length; i++) {
			supportedLocales[i] = new Locale(various.common.light.i18n.I18n.LANGUAGES.get(i).code);
		}

		return supportedLocales;
	}

	private void updateComboLocale() {
		for (int i = 0; i < comboLocale.getItemCount(); i++) {
			Locale currentScanLocale = (comboLocale.getItemAt(i));
			if (currentScanLocale.getLanguage().equals(various.common.light.i18n.I18n.normalize(currentConfiguration.getGenOpt().getLanguage()))) {
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
		currentConfiguration.getAesOpt().setSecurityLevel(BACKUP_SECURITY_LEVEL);
		currentConfiguration.getTextEditorOpt().setEol(BACKUP_DEFAULT_EOL);
		currentConfiguration.getTextEditorOpt().keepOriginalEol = BACKUP_KEEP_EOL;
		currentConfiguration.getGenOpt().setGeneralVolume(BACKUP_GENVOLUME);
		currentConfiguration.getGenOpt().setExceptionAdvicesEnabled(BACKUP_EXCEPTION_ADVICES);
		currentConfiguration.getGenOpt().setAutomaticUpdateOn(BACKUP_AUTOCHECK_UPDATES);
		currentConfiguration.getGenOpt().setAutomaticPluginUpdateOn(BACKUP_AUTOCHECK_PLUGIN_UPDATES);
		currentConfiguration.getGenOpt().setLocale(BACKUP_LOCALE);
		currentConfiguration.getGenOpt().setLanguage(BACKUP_LANGUAGE);
		various.common.light.i18n.I18n.setLanguage(BACKUP_LANGUAGE);

		// reset INItializer reference currentConfiguration too
	}

	private void addOrRemoveFromAutostart(boolean flagAutostartActive, boolean showDialogs) {
		boolean advicesFlagBackup = dialogHelper.enabledAdvices;
		dialogHelper.enabledAdvices = showDialogs;
		try {
			if(flagAutostartActive) {
				String[] outcome = AutostartShortcut.add();
				if(outcome[1].isEmpty()) {
					dialogHelper.info(outcome[0]);
					chckbxAutostart.setSelected(true);
				} else {
					dialogHelper.error(GuiUtils.encapsulateInHtml(outcome[1] + (SysUtils.getOsInfoLazy().isWindows() ? "" : "<br><br> Try to restart application with Administration rights.")), I18n.t("common.error"));
					chckbxAutostart.setSelected(false);
				}
			} else {
				if(!AutostartShortcut.remove()) {
					chckbxAutostart.setSelected(true);
					boolean tryAsAdmin = dialogHelper.yesOrNo(GuiUtils.encapsulateInHtml("Cannot remove from autostart menu <br><br> Try to restart application with Administration rights?"), I18n.t("common.error"));
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
		BACKUP_SECURITY_LEVEL = currentConfiguration.getAesOpt().getSecurityLevel();
		BACKUP_DEFAULT_EOL = currentConfiguration.getTextEditorOpt().getEol();
		BACKUP_KEEP_EOL = currentConfiguration.getTextEditorOpt().keepOriginalEol;
		BACKUP_GENVOLUME = currentConfiguration.getGenOpt().getGeneralVolume();
		BACKUP_EXCEPTION_ADVICES = currentConfiguration.getGenOpt().isExceptionAdvicesEnabled();
		BACKUP_AUTOCHECK_UPDATES = currentConfiguration.getGenOpt().isAutomaticUpdateOn();
		BACKUP_AUTOCHECK_PLUGIN_UPDATES = currentConfiguration.getGenOpt().isAutomaticPluginUpdateOn();
		BACKUP_LOCALE = currentConfiguration.getGenOpt().getLocale();
		BACKUP_LANGUAGE = currentConfiguration.getGenOpt().getLanguage();

		// write launcher configuration
		writeLauncherHidingOption(chckbxHideLauncherAt.isSelected());
	}

	private void writeValues() {
		// write single values to file ini associated to currentConf
		currentConfiguration.writeSingleValue(INItializer.GEN_OPT, "locale", currentConfiguration.getGenOpt().getLocale().toString());
		currentConfiguration.writeSingleValue(INItializer.GEN_OPT, "language", currentConfiguration.getGenOpt().getLanguage());
		currentConfiguration.writeSingleValue(INItializer.GEN_OPT, "userName", currentConfiguration.getGenOpt().getUserName());
		currentConfiguration.writeSingleValue(INItializer.GEN_OPT, "generalVolume", currentConfiguration.getGenOpt().getGeneralVolume());
		currentConfiguration.writeSingleValue(INItializer.GEN_OPT, "exceptionAdvicesEnabled", currentConfiguration.getGenOpt().isExceptionAdvicesEnabled());
		currentConfiguration.writeSingleValue(INItializer.GEN_OPT, "automaticUpdateOn", currentConfiguration.getGenOpt().isAutomaticUpdateOn());
		currentConfiguration.writeSingleValue(INItializer.GEN_OPT, "automaticPluginUpdateOn", currentConfiguration.getGenOpt().isAutomaticPluginUpdateOn());
		currentConfiguration.writeSingleValue(INItializer.FILE_OPT, "flagAutosave", currentConfiguration.getFileOpt().isFlagAutosave());
		currentConfiguration.writeSingleValue(INItializer.FILE_OPT, "autosaveInterval", currentConfiguration.getFileOpt().autosaveInterval);
		currentConfiguration.writeSingleValue(INItializer.AES_OPT, "securityLevel", currentConfiguration.getAesOpt().getSecurityLevel().name());
		currentConfiguration.writeSingleValue(INItializer.TEXT_EDITOR_OPT, "EOL", currentConfiguration.getTextEditorOpt().getEol().name());
		currentConfiguration.writeSingleValue(INItializer.TEXT_EDITOR_OPT, "keepOriginalEol", currentConfiguration.getTextEditorOpt().keepOriginalEol);

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

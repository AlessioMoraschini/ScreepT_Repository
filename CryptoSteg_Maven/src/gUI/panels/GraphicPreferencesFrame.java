package gUI.panels;

import resources.GeneralConfig;
import javax.swing.JFrame;
import net.miginfocom.swing.MigLayout;
import resources.IconsPathConfigurator;
import utils.RsyntaxUtils;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.Color;
import java.awt.Font;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

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

import frameutils.GuiUtils;
import gUI.utils.texteditor.TextEditorUtils;
import guistyle.GUIStyelSettings;
import maingUI.MainGUI;
import mainobj.INItializer;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.JButton;
import javax.swing.JSlider;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.JTextField;
import javax.swing.JCheckBox;
import java.awt.Cursor;
import java.awt.Dimension;

public class GraphicPreferencesFrame extends JFrame {

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
	private JCheckBox chckBoxAntiAliasingON;

	private JButton btnDiscard;
	private JButton btnUseSettings;
	private JButton btnSaveConf;
	private JButton btnResetValues;
	private JLabel lblUsername;
	private JLabel lblNimbusSize;
	private JLabel lblTextAreaDetail;
	private JSpinner spinnerMenuSize;
	private JSpinner spinnerLogSize;
	private JSpinner spinnerNimbusSize;
	private JSpinner spinnerDetailTxtArea;

	// actual values
	private static String CURRENT_USERNAME;
	private static String PREFERRED_STYLE;
	private static int MENU_FONT_SIZE;
	private static int LOG_PANEL_FONT_SIZE;
	private static boolean DARK_THEME;
	private static int NIMBUS_SIZE;
	private static int TXTAREA_DETAIL;
	private static boolean TXTAREA_ANTIALIASED;

	// default values
	private static String BACKUP_CURRENT_USERNAME;
	private static String BACKUP_PREFERRED_STYLE;
	private static int BACKUP_MENU_FONT_SIZE;
	private static int BACKUP_LOG_PANEL_FONT_SIZE;
	private static boolean BACKUP_DARK_THEME;
	private static int BACKUP_NIMBUS_SIZE;
	private static int BACKUP_TXTAREA_DETAIL;
	private static boolean BACKUP_TXTAREA_ANTIALIASED;

	// flag to signal active
	public static boolean active = false;

	private INItializer currentConfiguration;
	
	private Font initialTxtEditorFont; 

	public GraphicPreferencesFrame(INItializer currentConf) {

		logger.info(this.getClass().getName() + " Starting...");

		currentConfiguration = currentConf;
		active = true;
		initialTxtEditorFont = currentConfiguration.getTextEditorOpt().getFont();

		GuiUtils.setFrameIcon(IconsPathConfigurator.ICON_GUI_OPT, this);
		GuiUtils.showOnSameScreen(MainGUI.frmScryptV);
		
		initValues(currentConfiguration);

		logger.debug("Initialized.");

		// TODO initialize size (add to ini fields before)

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		setBackground(new Color(0, 0, 0));
		getContentPane().setBackground(new Color(112, 128, 144));
		getContentPane().setLayout(new MigLayout(GeneralConfig.DEBUG_GRAPHICS+"", "[:140px:180px][:80:80,grow][70][90][:10px:100px,grow][:100px:100px][:75px:75px][:75px:75px]", "[:50:70px][5][][15][30][10][30][30][50][30][:5px:10px,grow][10%,grow,baseline]"));

		JPanel panel = new JPanel();
		panel.setForeground(SystemColor.controlText);
		panel.setBorder(new LineBorder(null, 4));
		panel.setBackground(new Color(211, 211, 211));
		getContentPane().add(panel, "cell 0 0 8 1,grow");
		panel.setLayout(new MigLayout(GeneralConfig.DEBUG_GRAPHICS+"", "[100px][][70px][][][][][][][][][][][][][][][][][grow 1000][][][][][100px]", "[100]"));
		
		JLabel lblGraphicInterfaceSettings = new JLabel("GUI   Preferences");
		panel.add(lblGraphicInterfaceSettings, "cell 2 0 22 1,alignx center");
		lblGraphicInterfaceSettings.setHorizontalAlignment(SwingConstants.RIGHT);
		lblGraphicInterfaceSettings.setForeground(new Color(0, 0, 0));
		lblGraphicInterfaceSettings.setFont(new Font("Lithos Pro Regular", Font.BOLD, 22));
		lblGraphicInterfaceSettings.setBackground(Color.LIGHT_GRAY);

		JSeparator separator = new JSeparator();
		getContentPane().add(separator, "cell 0 2 8 1,growx");

		JLabel lblGuiType = new JLabel("Select GUI type");
		lblGuiType.setFont(new Font("Tahoma", Font.BOLD, 13));
		getContentPane().add(lblGuiType, "cell 0 4,alignx center");

		comboStyleLaF = new JComboBox(GUIStyelSettings.getLookAndFeelList());
		comboStyleLaF.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		comboStyleLaF.setMaximumRowCount(10);
		comboStyleLaF.setMaximumSize(new Dimension(600, 50));
		updateComboLaFGUI(PREFERRED_STYLE);
		getContentPane().add(comboStyleLaF, "cell 1 4 7 1,grow");

		JLabel lblMenuFontSize = new JLabel("Menu font size");
		lblMenuFontSize.setFont(new Font("Tahoma", Font.BOLD, 13));
		getContentPane().add(lblMenuFontSize, "cell 0 6,alignx center");

		spinnerMenuSize = new JSpinner();
		spinnerMenuSize.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		spinnerMenuSize.setModel(new SpinnerNumberModel(12, 5, 24, 1));
		getContentPane().add(spinnerMenuSize, "cell 1 6,growx");
		spinnerMenuSize.setValue(MENU_FONT_SIZE);

		sliderMenuSize = new JSlider();
		sliderMenuSize.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		sliderMenuSize.setValue(MENU_FONT_SIZE);
		sliderMenuSize.setMaximum(24);
		sliderMenuSize.setMinimum(5);
		getContentPane().add(sliderMenuSize, "cell 2 6 6 1,growx");

		JLabel lblFontSize = new JLabel("Log Font size");
		lblFontSize.setFont(new Font("Tahoma", Font.BOLD, 13));
		getContentPane().add(lblFontSize, "cell 0 7,alignx center");

		spinnerLogSize = new JSpinner();
		spinnerLogSize.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		spinnerLogSize.setModel(new SpinnerNumberModel(12, 5, 30, 1));
		spinnerLogSize.setValue(LOG_PANEL_FONT_SIZE);
		getContentPane().add(spinnerLogSize, "cell 1 7,growx");

		sliderLogSize = new JSlider();
		sliderLogSize.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		sliderLogSize.setMaximum(30);
		sliderLogSize.setMinimum(5);
		sliderLogSize.setValue(LOG_PANEL_FONT_SIZE);
		getContentPane().add(sliderLogSize, "cell 2 7 6 1,growx");

		lblNimbusSize = new JLabel("Nimbus size variant");
		getContentPane().add(lblNimbusSize, "cell 0 8,alignx center");
		lblNimbusSize.setFont(new Font("Segoe UI", Font.PLAIN, 13));

		spinnerNimbusSize = new JSpinner();
		getContentPane().add(spinnerNimbusSize, "cell 1 8,growx");
		spinnerNimbusSize.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		spinnerNimbusSize.setModel(new SpinnerNumberModel(0, 0, 2, 1));
		spinnerNimbusSize.setValue(NIMBUS_SIZE);
						
		lblUsername = new JLabel("Username");
		lblUsername.setFont(new Font("Tahoma", Font.BOLD, 13));
		getContentPane().add(lblUsername, "cell 2 8 2 1,alignx center");
				
		textFieldUname = new JTextField();
		textFieldUname.setBackground(new Color(224, 255, 255));
		textFieldUname.setHorizontalAlignment(SwingConstants.CENTER);
		textFieldUname.setFont(new Font("SansSerif", Font.PLAIN, 18));
		getContentPane().add(textFieldUname, "cell 4 8 4 1,growx,aligny center");
		textFieldUname.setColumns(10);
		textFieldUname.setBorder(new LineBorder(Color.DARK_GRAY, 3));
		textFieldUname.setText(CURRENT_USERNAME);
		
		lblTextAreaDetail = new JLabel("Text Area Detail");
		lblTextAreaDetail.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		getContentPane().add(lblTextAreaDetail, "cell 0 9,alignx center");
		
		spinnerDetailTxtArea = new JSpinner();
		spinnerDetailTxtArea.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		spinnerDetailTxtArea.setModel(new SpinnerNumberModel(0, 0, 3, 1));
		getContentPane().add(spinnerDetailTxtArea, "cell 1 9,growx");
		
		chckBoxAntiAliasingON = new JCheckBox("Antialiasing enabled (text area)");
		chckBoxAntiAliasingON.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		chckBoxAntiAliasingON.setFont(new Font("SansSerif", Font.BOLD, 13));
		getContentPane().add(chckBoxAntiAliasingON, "cell 2 9 3 1,alignx center");
		
		chckbxDarklight = new JCheckBox("Nimbus Dark theme");
		getContentPane().add(chckbxDarklight, "cell 5 9 3 1,alignx center");
		chckbxDarklight.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		chckbxDarklight.setFont(new Font("SansSerif", Font.BOLD, 13));

		JSeparator separator_1 = new JSeparator();
		getContentPane().add(separator_1, "cell 0 10 8 1,growx,aligny center");

		btnResetValues = new JButton("Reset initial values");
		btnResetValues.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnResetValues.setFont(new Font("SansSerif", Font.BOLD, 12));
		btnResetValues.setBackground(new Color(240, 230, 140));
		getContentPane().add(btnResetValues, "cell 0 11 2 1,growx,aligny center");

		btnSaveConf = new JButton("Save Values");
		btnSaveConf.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnSaveConf.setForeground(new Color(0, 0, 0));
		btnSaveConf.setFont(new Font("SansSerif", Font.BOLD, 12));
		btnSaveConf.setBackground(new Color(169, 169, 169));
		getContentPane().add(btnSaveConf, "cell 5 11,growx,aligny center");

		btnUseSettings = new JButton("Use");
		btnUseSettings.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnUseSettings.setForeground(Color.WHITE);
		btnUseSettings.setFont(new Font("SansSerif", Font.BOLD, 12));
		btnUseSettings.setBackground(Color.DARK_GRAY);
		getContentPane().add(btnUseSettings, "cell 6 11,growx");

		btnDiscard = new JButton("Discard");
		btnDiscard.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnDiscard.setForeground(new Color(0, 0, 0));
		btnDiscard.setFont(new Font("SansSerif", Font.BOLD, 12));
		btnDiscard.setBackground(new Color(205, 92, 92));
		getContentPane().add(btnDiscard, "cell 7 11,growx");

		// default close operation(same as reset)
		addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {

				logger.debug("Closing GUI_Preferences");

				// reset current values
				resetToBackup();

				// reset INItializer reference currentConf too
				resetConfigValues(currentConf);
//				GUIStyelSettings.updateAllWindowsLaf();

				logger.debug(
						"Closing " + getClass().getName() + " - Configuration resetted: \n" + currentConf.toStringa());

				active = false;

				dispose();
			}
		});

		initGuiFromConfig();
		
		addHandlers();
		
//		if (TextEditor.loadedTabObj != null) {
//			TextEditor.loadedTabObj.textArea.setFont(initialTxtEditorFont);
//			RsyntaxUtils.setFont(TextEditor.loadedTabObj.textArea, initialTxtEditorFont);
//		}
	}

	private void addHandlers() {

		// DARK GUI HANDLER
		chckbxDarklight.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!chckbxDarklight.isSelected()) {
					DARK_THEME = !GUIStyelSettings.setLightNimbus(currentConfiguration.getGuiOpt().nimbusSize);
					currentConfiguration.getGuiOpt().setDarkGuiNimbus(false);
				} else {
					DARK_THEME = GUIStyelSettings.setDarkNimbus(currentConfiguration.getGuiOpt().nimbusSize);
					currentConfiguration.getGuiOpt().setDarkGuiNimbus(true);
				}
				GUIStyelSettings.updateAllWindowsLaf();
				TextEditorUtils.applyStyleToTabs();
			}
		});
		
		// ANTIALIASING SELECTOR HANDLER
		chckBoxAntiAliasingON.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean antialiasing = chckBoxAntiAliasingON.isSelected();
				TXTAREA_ANTIALIASED = antialiasing;
				currentConfiguration.getTextEditorOpt().antialiasedTxtArea = antialiasing;
				antialiasingApply(antialiasing);
			}
		});

		// COMBO STYLE HANDLER
		comboStyleLaF.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						try {
							LookAndFeelInfo laf = (LookAndFeelInfo) comboStyleLaF.getSelectedItem();
							PREFERRED_STYLE = laf.getClassName();
							currentConfiguration.getGuiOpt().setPreferredStyle(PREFERRED_STYLE);
							GUIStyelSettings.setStyle(laf.getClassName(), NIMBUS_SIZE);
						} catch (Exception e1) {
							JOptionPane.showMessageDialog(thisFrame, "Sorry, something went wrong :(",
									e1.getClass().getName(), JOptionPane.ERROR_MESSAGE);
							logger.error("Exception happened!", e1);
							PREFERRED_STYLE = BACKUP_PREFERRED_STYLE;
						} finally {
							GUIStyelSettings.updateAllWindowsLaf();
							TextEditorUtils.applyStyleToTabs();
							TextEditorUtils.resetDividerColor();
						}
					}
				});
			}
		});

		// RESET BUTTON HANDLER
		btnResetValues.addActionListener(new ActionListener() {

			// action fired when user reset initial values saved in backup values
			@Override
			public void actionPerformed(ActionEvent e) {

				logger.debug("Starting reset of GUI_Preferences");

				// reset current values
				resetToBackup();

				// reset INItializer reference currentConf too
				resetConfigValues(currentConfiguration);

				// reset gui (sliders changement will trigger jtextfield, see action listener)
				updateGUI();

//				GUIStyelSettings.updateAllWindowsLaf();
				TextEditorUtils.applyStyleToTabs();

				logger.debug("Configuration resetted: \n" + currentConfiguration.toStringa());
			}
		});

		// USERNAME TEXTFIELD HANDLER
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
				currentConfiguration.getGenOpt().setUserName(CURRENT_USERNAME);
			}
		});

		// SAVE CONFIG BUTTON HANDLER
		btnSaveConf.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int chosenOption = JOptionPane.showConfirmDialog(thisFrame,
						"Are you sure you want to overwrite " + currentConfiguration.getIniFile().getFile().getPath()
								+ "(ONLY GUI_OPTIONS)?",
						"overwrite configuration", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
				if (chosenOption == JOptionPane.YES_OPTION) {

					// write single values to file ini associated to currentConf
					writeValuesToConfFile(currentConfiguration);
					
					// update BACKUP_VALUES as the new ones
					updateBackup();

					antialiasingApply(TXTAREA_ANTIALIASED);
					
					JOptionPane.showMessageDialog(thisFrame,
							"Graphic Interface settings Saved :)\n -> Changes will be visible after next restart.",
							"Saved!", JOptionPane.INFORMATION_MESSAGE);
					TextEditorUtils.resetDividerColor();
					TextEditorUtils.applyStyleToTabs();
					logger.debug("Configuration saved: \n" + currentConfiguration.toStringa());

					// dispose();
					dispatchEvent(new WindowEvent(thisFrame, WindowEvent.WINDOW_CLOSING));
					

				} else {
					JOptionPane.showMessageDialog(thisFrame, "Ok, I wont save theese settings ;)", "Not saved!",
							JOptionPane.WARNING_MESSAGE);
					TextEditorUtils.resetDividerColor();
					TextEditorUtils.applyStyleToTabs();
				}
				
			}
		});

		// DISCARD BUTTON HANDLER
		btnDiscard.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				logger.debug("DISCARD => Closing GUI_Preferences");
				
				// reset current values
				resetToBackup();

				antialiasingApply(TXTAREA_ANTIALIASED);

				// reset INItializer reference currentConf too
				resetConfigValues(currentConfiguration);

				// reset gui (sliders changement will trigger jtextfield, see action listener)
				updateGUI();
//				GUIStyelSettings.updateAllWindowsLaf();
				TextEditorUtils.applyStyleToTabs();
				TextEditorUtils.resetDividerColor();

				logger.debug("Configuration resetted: \n" + currentConfiguration.toStringa());

				// dispose();
				dispatchEvent(new WindowEvent(thisFrame, WindowEvent.WINDOW_CLOSING));
			}
		});

		// USE SETTINGS BUTTON HANDLER
		btnUseSettings.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// update (locally)BACKUP_VALUES as the new ones
				updateBackup();
				TextEditorUtils.resetDividerColor();
//				GUIStyelSettings.updateAllWindowsLaf();
				TextEditorUtils.applyStyleToTabs();

				// close and apply new values without saving
				dispatchEvent(new WindowEvent(thisFrame, WindowEvent.WINDOW_CLOSING));
			}
		});

		/** SPINNER HANDLERS **/

		spinnerDetailTxtArea.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				TXTAREA_DETAIL = (int)spinnerDetailTxtArea.getValue();
				currentConfiguration.getTextEditorOpt().setTxtAreaDetailLevel(TXTAREA_DETAIL);
			}
		});
		
		// MENU SIZE HANDLER
		spinnerMenuSize.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				MENU_FONT_SIZE = (int) spinnerMenuSize.getValue();
				currentConfiguration.getGuiOpt().setMenuFontSize(MENU_FONT_SIZE);
				sliderMenuSize.setValue(MENU_FONT_SIZE);
			}
		});

		spinnerLogSize.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				LOG_PANEL_FONT_SIZE = (int) spinnerLogSize.getValue();
				currentConfiguration.getGuiOpt().setLogFontSize(LOG_PANEL_FONT_SIZE);
				sliderLogSize.setValue(LOG_PANEL_FONT_SIZE);
			}
		});

		spinnerNimbusSize.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				NIMBUS_SIZE = (int) spinnerNimbusSize.getValue();
				GUIStyelSettings.applyNimbusSize(NIMBUS_SIZE);
				GUIStyelSettings.updateAllWindowsLaf();
				TextEditorUtils.resetDividerColor();
				TextEditorUtils.applyStyleToTabs();
			}
		});

		sliderLogSize.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				LOG_PANEL_FONT_SIZE = (int) sliderLogSize.getValue();
				spinnerLogSize.setValue(LOG_PANEL_FONT_SIZE);
			}
		});

		sliderMenuSize.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				MENU_FONT_SIZE = (int) sliderMenuSize.getValue();
				spinnerMenuSize.setValue(MENU_FONT_SIZE);
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
		NIMBUS_SIZE = currentConf.getGuiOpt().getNimbusSize();
		TXTAREA_ANTIALIASED = currentConf.getTextEditorOpt().isAntialiasedTxtArea();
		TXTAREA_DETAIL = currentConf.getTextEditorOpt().getTxtAreaDetailLevel();

		// initialize default values (by value)
		BACKUP_CURRENT_USERNAME = new String(CURRENT_USERNAME);
		BACKUP_MENU_FONT_SIZE = MENU_FONT_SIZE;
		BACKUP_LOG_PANEL_FONT_SIZE = LOG_PANEL_FONT_SIZE;
		BACKUP_PREFERRED_STYLE = new String(PREFERRED_STYLE);
		BACKUP_DARK_THEME = DARK_THEME;
		BACKUP_NIMBUS_SIZE = NIMBUS_SIZE;
		BACKUP_TXTAREA_ANTIALIASED = TXTAREA_ANTIALIASED;
		BACKUP_TXTAREA_DETAIL = TXTAREA_DETAIL;
	}

	private void resetConfigValues(INItializer currentConf) {
		currentConf.getGenOpt().setUserName(CURRENT_USERNAME);
		currentConf.getGuiOpt().setPreferredStyle(PREFERRED_STYLE);
		currentConf.getGuiOpt().setMenuFontSize(MENU_FONT_SIZE);
		currentConf.getGuiOpt().setLogFontSize(LOG_PANEL_FONT_SIZE);
		currentConf.getGuiOpt().setDarkGuiNimbus(DARK_THEME);
		currentConf.getGuiOpt().setNimbusSize(NIMBUS_SIZE);
		
		currentConf.getTextEditorOpt().setAntialiasedTxtArea(TXTAREA_ANTIALIASED);
		currentConf.getTextEditorOpt().setTxtAreaDetailLevel(TXTAREA_DETAIL);
	}

	private boolean writeValuesToConfFile(INItializer currentConf) {
		currentConf.writeSingleValue(INItializer.GEN_OPT, "userName", CURRENT_USERNAME);
		currentConf.writeSingleValue(INItializer.GUI_OPT, "preferredStyle", PREFERRED_STYLE);
		currentConf.writeSingleValue(INItializer.GUI_OPT, "menuFontSize", MENU_FONT_SIZE);
		currentConf.writeSingleValue(INItializer.GUI_OPT, "logFontSize", LOG_PANEL_FONT_SIZE);
		currentConf.writeSingleValue(INItializer.GUI_OPT, "darkGuiNimbus", DARK_THEME);
		currentConf.writeSingleValue(INItializer.GUI_OPT, "nimbusSize", NIMBUS_SIZE);
		
		currentConf.writeSingleValue(INItializer.TEXT_EDITOR_OPT, "txtAreaDetailLevel", TXTAREA_DETAIL);
		currentConf.writeSingleValue(INItializer.TEXT_EDITOR_OPT, "antialiasedTxtArea", TXTAREA_ANTIALIASED);

		return true;
	}

	private void updateGUI() {
		sliderMenuSize.setValue(MENU_FONT_SIZE);
		sliderLogSize.setValue(LOG_PANEL_FONT_SIZE);
		textFieldUname.setText(CURRENT_USERNAME);
		updateComboLaFGUI(PREFERRED_STYLE);
		chckbxDarklight.setSelected(DARK_THEME);
		chckBoxAntiAliasingON.setSelected(TXTAREA_ANTIALIASED);
		spinnerDetailTxtArea.setValue(TXTAREA_DETAIL);

		antialiasingApply(TXTAREA_ANTIALIASED);

		if (DARK_THEME) {
			GUIStyelSettings.setDarkNimbus(NIMBUS_SIZE);
		} 
//		else {
//			GUIStyelSettings.setLightNimbus(NIMBUS_SIZE);
//		}

		spinnerNimbusSize.setValue(NIMBUS_SIZE);

		GUIStyelSettings.setStyle(PREFERRED_STYLE, NIMBUS_SIZE);
		TextEditorUtils.resetDividerColor();
		TextEditorUtils.applyStyleToTabs();
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (TextEditor.loadedTabObj != null) {
					TextEditor.loadedTabObj.textArea.setFont(currentConfiguration.getTextEditorOpt().getFont());
					RsyntaxUtils.setFont(TextEditor.loadedTabObj.textArea, currentConfiguration.getTextEditorOpt().getFont());
				}
			}
		});
	}

	private void updateBackup() {
		// update backup values to current
		BACKUP_CURRENT_USERNAME = CURRENT_USERNAME;
		BACKUP_PREFERRED_STYLE = PREFERRED_STYLE;
		BACKUP_MENU_FONT_SIZE = MENU_FONT_SIZE;
		BACKUP_LOG_PANEL_FONT_SIZE = LOG_PANEL_FONT_SIZE;
		BACKUP_DARK_THEME = DARK_THEME;
		BACKUP_NIMBUS_SIZE = NIMBUS_SIZE;
		BACKUP_TXTAREA_ANTIALIASED = TXTAREA_ANTIALIASED;
		BACKUP_TXTAREA_DETAIL = TXTAREA_DETAIL;
	}

	private void resetToBackup() {
		// reset current values
		CURRENT_USERNAME = BACKUP_CURRENT_USERNAME;
		PREFERRED_STYLE = BACKUP_PREFERRED_STYLE;
		MENU_FONT_SIZE = BACKUP_MENU_FONT_SIZE;
		LOG_PANEL_FONT_SIZE = BACKUP_LOG_PANEL_FONT_SIZE;
		DARK_THEME = BACKUP_DARK_THEME;
		NIMBUS_SIZE = BACKUP_NIMBUS_SIZE;
		TXTAREA_ANTIALIASED = BACKUP_TXTAREA_ANTIALIASED;
		TXTAREA_DETAIL = BACKUP_TXTAREA_DETAIL;
	}

	private static void updateMenuAndLogFontSize(JFrame jframe) {

		// use the same font as main gui but change font size
		UIManager.put("MenuBar.font", new Font("Segoe UI", Font.BOLD, MENU_FONT_SIZE));
		UIManager.put("Menu.font", new Font("Segoe UI", Font.BOLD, MENU_FONT_SIZE));
//		GUIStyelSettings.updateAllWindowsLaf();

	}

	private void updateComboLaFGUI(String value) {

		for (int i = 0; i < comboStyleLaF.getItemCount(); i++) {
			String currentScanString = ((LookAndFeelInfo) comboStyleLaF.getItemAt(i)).getClassName();
			if (currentScanString.indexOf(value) > -1) {
				comboStyleLaF.setSelectedIndex(i);
				break;
			}
		}
	}

	public void initGuiFromConfig() {
		initValues(currentConfiguration);
		updateGUI();
	}
	
	private void antialiasingApply(boolean antialiasing) {
		try {
			TextEditor.loadedTabObj.textArea.setAntiAliasingEnabled(antialiasing);
		} catch (Exception e1) {
			logger.warn("Error while preiewing antialising effect on textarea", e1);
		}
	}
}

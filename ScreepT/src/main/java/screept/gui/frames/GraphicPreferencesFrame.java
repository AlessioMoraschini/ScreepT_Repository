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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.ListCellRenderer;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;

import com.jgoodies.looks.plastic.PlasticTheme;

import gui.commons.dialogutils.JOptionHelperExtended;
import gui.commons.frameutils.frame.arch.ParentFrame;
import gui.commons.frameutils.utils.GuiUtilsExtended;
import gui.commons.handlers.AlwaysOnTopManager;
import gui.commons.guistyle.GUIStyelSettingsExtended;
import gui.commons.guistyle.LookAndFeelInfoCustom;
import gui.commons.guistyle.scale.UiScaleCustomDialog;
import gui.commons.guistyle.scale.UiScaleManager;
import gui.commons.panel.options.OptionSection;
import gui.commons.panel.options.OptionsHeader;
import gui.commons.panel.options.OptionsPanel;
import gui.texteditor.commons.om.TabTextEditorObject;
import gui.texteditor.commons.utils.RsyntaxUtils;
import gui.texteditor.commons.utils.panels.ClipboardPanel;
import gui.texteditor.commons.utils.panels.arch.TogglePanelTextArea;
import initializer.configs.impl.INItializer;
import initializer.configs.impl.om.GuiOption;
import initializer.configs.impl.om.TextEditorOption;
import initializer.configs.impl.om.UiSizePreset;
import maingUI.MainGUI;
import net.miginfocom.swing.MigLayout;
import resources.GeneralConfig;
import resources.IconsPathConfigurator;
import screept.gui.panels.TextEditor;
import various.common.light.files.om.FileNamed;
import various.common.light.gui.GuiUtils;

public class GraphicPreferencesFrame extends ParentFrame {
	private static final long serialVersionUID = 6658781789560743187L;

	public static GraphicPreferencesFrame singletonInstance = null;

	// initialize Logger
	// GraphicPreferencesFrame logger
	static Logger logger = Logger.getLogger(GraphicPreferencesFrame.class);

	public static Font CHECKBOX_FONT = new Font("SansSerif", Font.PLAIN, 15);

	private static final int MAX_SCREEN_PERCENT = 90;

	// default values
	private String BACKUP_PREFERRED_STYLE;
	private String BACKUP_PREFERRED_THEME_NAME;
	private int BACKUP_MENU_FONT_SIZE;
	private int BACKUP_LOG_PANEL_FONT_SIZE;
	private boolean BACKUP_DARK_THEME;
	private int BACKUP_NIMBUS_SIZE;
	private final GuiOption BACKUP_UI_SIZE = new GuiOption();

	// DETAIL
	private boolean BACKUP_TXTAREA_ANTIALIASED;
	private boolean BACKUP_TXTAREA_AUTO_INDENT;
	private boolean BACKUP_TXTAREA_ANIMATE_BRACKETS;
	private boolean BACKUP_TXTAREA_HIGHLIGHT_BRACKET_PAIRS;
	private boolean BACKUP_TXTAREA_BRACKET_MATCHING;
	private boolean BACKUP_TXTAREA_SHOW_WHITESPACES;
	private boolean BACKUP_TXTAREA_CLEAR_WHITESP_LINES;
	private boolean BACKUP_TXTAREA_CLOSE_CURLY_BRACE;
	private boolean BACKUP_TXTAREA_CLOSE_MARKUP_TAG;
	private boolean BACKUP_TXTAREA_CODE_FOLDING;
	private boolean BACKUP_TXTAREA_CURR_LINE_HIGHLIGHT;
	private boolean BACKUP_TXTAREA_FRACT_FONT_METRICS;
	private boolean BACKUP_TXTAREA_HIGLIGHT_2ND_LANG;
	private boolean BACKUP_TXTAREA_HYPERLINKS;
	private boolean BACKUP_TXTAREA_MARK_OCCURRENCES;
	private boolean BACKUP_TXTAREA_PAINT_MARK_OCCURRENCES;
	private boolean BACKUP_TXTAREA_ROUND_SEL_EDGES;
	private boolean BACKUP_TXTAREA_FOCUSABLE_TIPS;
	private boolean BACKUP_TXTAREA_TABSPACES_EMULATED;
	private boolean BACKUP_TXTAREA_SHOW_BRACKETS_POPUP;
	private boolean BACKUP_TXTAREA_SHOW_EOL_DELIM;
	private boolean BACKUP_TXTAREA_AUTOCHECK_CHANGES;
	private boolean BACKUP_SYNC_CLIPBOARD_STYLE;
	private boolean BACKUP_AUTOSAVE_TEMP_FILES;
	private String BACKUP_TXTAREA_SPECCLCHECKER_DICT;
	private int BACKUP_INTERLINE;

	private INItializer currentConfiguration;

	private TextEditor currentEditor;

	// GUI elements
	private final JFrame thisFrame;
	private OptionsPanel optionsPanel;

	private JSlider sliderMenuSize;
	private JSlider sliderLogSize;
	private ArrayList<PlasticTheme> themes;
	private JComboBox<PlasticTheme> comboTheme;
	private JComboBox<LookAndFeelInfoCustom> comboStyleLaF;
	private JComboBox<FileNamed> comboSpellcheckerDictionary;

	private JButton btnDiscard;
	private JButton btnUseSettings;
	private JButton btnSaveConf;
	private JButton btnResetValues;
	private JSpinner spinnerMenuSize;
	private JSpinner spinnerLogSize;
	private JSpinner spinnerNimbusSize;
	private JComboBox<UiSizePreset> comboUiSize;
	private JButton btnUiSizeCustomize;
	private boolean updatingUiSizeCombo;

	private JCheckBox chckbxDarklight;

	private JCheckBox chckBoxAntiAliasingON;
	private JCheckBox chckbxAutoindentation;
	private JCheckBox chckbxAnimateBrackets;
	private JCheckBox chckbxBracketsMatching;
	private JCheckBox chckbxHightlightBracketPairs;
	private JCheckBox chckbxShowWhitespaces;
	private JCheckBox chckbxClearWhitespaceLines;
	private JCheckBox chckbxCloseCurlybraces;
	private JCheckBox chckbxCloseMarkuptags;
	private JCheckBox chckbxFocusableTips;
	private JCheckBox chckbxCodeFolding;
	private JCheckBox chckbxFadeHighlight;
	private JCheckBox chckbxAllowFractionalFonts;
	private JCheckBox chckbxHighlightndLanguages;
	private JCheckBox chckbxHyperlinksEnabled;
	private JCheckBox chckbxMarkOccurrencesPaintBorder;
	private JCheckBox checkBoxMarkOccurrences;
	private JCheckBox chckbxRoundSelectionEdges;
	private JCheckBox chckbxEmulatedTabsWith;
	private JCheckBox chckbxMarkEOL;
	private JCheckBox chckbxShowBracket;
	private JCheckBox chckbxAutoCheckFiles;
	private JCheckBox chckbxSyncClipboardStyle;

	private JComboBox<String> comboEditorFontFamily;
	private JSpinner spinnerEditorFontSize;
	private boolean updatingEditorFont;
	private Font BACKUP_EDITOR_FONT;

	private JSpinner spinnerInterline;
	private JCheckBox checkBoxAutosaveTempFiles;
	private JPanel footerPanel;

	public GraphicPreferencesFrame(INItializer currentConf) {

		isActive.set(true);

		thisFrame = this;

		singletonInstance = this;

		currentEditor = MainGUI.textEditorPanel;

		logger.info(this.getClass().getName() + " Starting...");

		currentConfiguration = currentConf;

		AlwaysOnTopManager.keepAboveApplication(this);
		this.setResizable(true);
		this.setMinimumSize(new Dimension(680, 460));

		GuiUtilsExtended.setFrameIcon(IconsPathConfigurator.ICON_GUI_OPT, this);
		setLocationRelativeTo(MainGUI.mainFrameScreept);

		updateBackup(currentConfiguration);

		logger.debug("Backup Initialized.");

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		createControls();
		createFooter();
		buildLayout();

		addHandlers();

		styleBasedThemesSelectorToggle((LookAndFeelInfoCustom) comboStyleLaF.getSelectedItem());
		styleBasedDarkNimbusToggle((LookAndFeelInfoCustom) comboStyleLaF.getSelectedItem());
		styleBasedSizeNimbusToggle((LookAndFeelInfoCustom) comboStyleLaF.getSelectedItem());

		initGuiFromConfig();

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				MainGUI.updateGuiFromCurrConfig();
				GuiUtils.launchRunnable(()->{
					GuiUtils.giveFocusToComponent(thisFrame);
				}, true, true, true);
			}
		});
	}

	/*
	 * VIEW CONSTRUCTION: option controls (models and listeners are bound in addHandlers/updateGUI),
	 * footer buttons, then the presentation (sections, search). The data model is never touched here.
	 */

	private void createControls() {

		comboStyleLaF = new JComboBox<LookAndFeelInfoCustom>(GUIStyelSettingsExtended.getLookAndFeelList());
		comboStyleLaF.setMaximumRowCount(10);

		themes = GUIStyelSettingsExtended.getPlasticThemes();
		comboTheme = new JComboBox<PlasticTheme>(themes.toArray(new PlasticTheme[themes.size()]));
		comboTheme.setRenderer(new ThemeCellRenderer());
		comboTheme.setMaximumRowCount(10);

		comboUiSize = new JComboBox<UiSizePreset>(UiSizePreset.values());
		comboUiSize.setToolTipText("Fonts and graphic elements size; Custom keeps your own values");
		btnUiSizeCustomize = new JButton("Customize...");
		btnUiSizeCustomize.setToolTipText("Show or set font family and sizes of general text, menus, toolbars and graphic elements");

		spinnerMenuSize = new JSpinner(new SpinnerNumberModel(12, 5, 24, 1));
		sliderMenuSize = new JSlider(5, 24);
		spinnerLogSize = new JSpinner(new SpinnerNumberModel(12, 5, 30, 1));
		sliderLogSize = new JSlider(5, 30);

		spinnerNimbusSize = new JSpinner(new SpinnerNumberModel(0, 0, 2, 1));
		spinnerNimbusSize.setValue(currentConfiguration.getGuiOpt().nimbusSize);
		chckbxDarklight = newCheckBox("Nimbus dark theme");

		comboEditorFontFamily = new JComboBox<String>(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
		comboEditorFontFamily.setMaximumRowCount(20);
		comboEditorFontFamily.setToolTipText("Text editor font: a monospaced one (e.g. Consolas) keeps rectangular selections aligned");
		spinnerEditorFontSize = new JSpinner(new SpinnerNumberModel(16, 4, 100, 1));

		spinnerInterline = new JSpinner(new SpinnerNumberModel(0, 0, 20, 1));
		comboSpellcheckerDictionary = new JComboBox<FileNamed>(RsyntaxUtils.getSpellCheckerDIctionaries());

		chckbxAutoindentation = newCheckBox("Auto-indentation");
		chckbxCloseCurlybraces = newCheckBox("Close curly braces");
		chckbxCloseMarkuptags = newCheckBox("Close markup tags");
		chckbxEmulatedTabsWith = newCheckBox("Emulated tabs with spaces");
		chckbxClearWhitespaceLines = newCheckBox("Clear whitespace lines");
		checkBoxAutosaveTempFiles = newCheckBox("Autosave temp files");
		chckbxAutoCheckFiles = newCheckBox("Auto check for file changes");
		chckbxSyncClipboardStyle = newCheckBox("Sync clipboard style");

		chckBoxAntiAliasingON = newCheckBox("Antialiasing enabled");
		chckbxAllowFractionalFonts = newCheckBox("Allow fractional fonts");
		chckbxCodeFolding = newCheckBox("Code folding");
		chckbxFadeHighlight = newCheckBox("Fade highlight");
		chckbxHighlightndLanguages = newCheckBox("Highlight 2nd languages");
		chckbxShowWhitespaces = newCheckBox("Show whitespaces and empty lines");
		chckbxMarkEOL = newCheckBox("Mark end of line");
		chckbxRoundSelectionEdges = newCheckBox("Round selection edges");
		chckbxHyperlinksEnabled = newCheckBox("Hyperlinks enabled");
		chckbxFocusableTips = newCheckBox("Focusable tips");

		chckbxBracketsMatching = newCheckBox("Brackets matching");
		chckbxAnimateBrackets = newCheckBox("Animate brackets");
		chckbxHightlightBracketPairs = newCheckBox("Paint matched bracket pairs");
		chckbxShowBracket = newCheckBox("Show bracket popup");
		checkBoxMarkOccurrences = newCheckBox("Mark occurrences");
		chckbxMarkOccurrencesPaintBorder = newCheckBox("Paint occurrences border");

		for (JComponent control : new JComponent[] { comboStyleLaF, comboTheme, comboUiSize, btnUiSizeCustomize,
				comboEditorFontFamily, comboSpellcheckerDictionary, sliderMenuSize, sliderLogSize }) {
			control.setCursor(GuiUtilsExtended.CURSOR_HAND);
		}
	}

	private JCheckBox newCheckBox(String text) {
		JCheckBox checkBox = new JCheckBox(text);
		checkBox.setOpaque(false);
		checkBox.setCursor(GuiUtilsExtended.CURSOR_HAND);
		return checkBox;
	}

	private void createFooter() {
		btnResetValues = newFooterButton("Reset initial values", new Color(240, 230, 140));
		btnSaveConf = newFooterButton("Save Values", new Color(169, 169, 169));
		btnUseSettings = newFooterButton("Use", new Color(169, 169, 169));
		btnDiscard = newFooterButton("Discard", new Color(205, 92, 92));

		footerPanel = new JPanel(new MigLayout("insets 10 18 12 18", "[][grow][][][]"));
		footerPanel.add(btnResetValues, "cell 0 0");
		footerPanel.add(btnSaveConf, "cell 2 0");
		footerPanel.add(btnUseSettings, "cell 3 0");
		footerPanel.add(btnDiscard, "cell 4 0");
	}

	private JButton newFooterButton(String text, Color background) {
		JButton button = new JButton(text);
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
		optionsPanel.addSection("Appearance", "Look and feel, theme and interface size")
				.addRow("GUI style", comboStyleLaF)
				.addRow("Theme", comboTheme)
				.addRow("Interface size", comboUiSize, btnUiSizeCustomize)
				.addRow("Menu font size", spinnerMenuSize, sliderMenuSize)
				.addRow("Log font size", spinnerLogSize, sliderLogSize)
				.addRow("Nimbus size variant", spinnerNimbusSize)
				.addCheckBoxes(chckbxDarklight);
		OptionSection textEditor = optionsPanel.addSection("Text editor", "Font, line spacing and spellchecker")
				.addRow("Font", comboEditorFontFamily, spinnerEditorFontSize)
				.addRow("Interline", spinnerInterline)
				.addRow("Spellchecker dictionary", comboSpellcheckerDictionary);
		textEditor.addSubsection("Editing", "Typing helpers, tabs and files")
				.addCheckBoxes(chckbxAutoindentation, chckbxCloseCurlybraces, chckbxCloseMarkuptags, chckbxEmulatedTabsWith,
						chckbxClearWhitespaceLines, checkBoxAutosaveTempFiles, chckbxAutoCheckFiles, chckbxSyncClipboardStyle);
		textEditor.addSubsection("Display", "How text is rendered and highlighted")
				.addCheckBoxes(chckBoxAntiAliasingON, chckbxAllowFractionalFonts, chckbxCodeFolding, chckbxFadeHighlight,
						chckbxHighlightndLanguages, chckbxShowWhitespaces, chckbxMarkEOL, chckbxRoundSelectionEdges,
						chckbxHyperlinksEnabled, chckbxFocusableTips);
		textEditor.addSubsection("Brackets and occurrences", null)
				.addCheckBoxes(chckbxBracketsMatching, chckbxAnimateBrackets, chckbxHightlightBracketPairs, chckbxShowBracket,
						checkBoxMarkOccurrences, chckbxMarkOccurrencesPaintBorder);

		JScrollPane scroller = new JScrollPane(optionsPanel);
		scroller.setBorder(BorderFactory.createEmptyBorder());
		scroller.getVerticalScrollBar().setUnitIncrement(16);

		getContentPane().setLayout(new MigLayout("fill, insets 0, gap 0", "[grow,fill]", "[][grow,fill][]"));
		getContentPane().add(createHeader(), "growx, wrap");
		getContentPane().add(scroller, "grow, wrap");
		getContentPane().add(footerPanel, "growx");
	}

	private JPanel createHeader() {
		return new OptionsHeader("Graphic preferences", optionsPanel, this);
	}

	/** Builds the editor font from the controls and applies it live (backup/restore handle discard) */
	private void applyEditorFont() {
		Font current = currentConfiguration.getTextEditorOpt().getFont();
		Font font = new Font((String) comboEditorFontFamily.getSelectedItem(), current.getStyle(), (Integer) spinnerEditorFontSize.getValue());
		currentConfiguration.getTextEditorOpt().setFont(font);
		refreshEditorFont(font);
	}

	/** Applies the font to every open tab and to the editor toolbar size slider */
	private void refreshEditorFont(Font font) {
		if (currentEditor == null) {
			return;
		}
		try {
			for (TabTextEditorObject tab : currentEditor.getCurrentTabEditor().loadedList.getList()) {
				tab.textArea.setFont(font);
			}
			currentEditor.subHeaderPanel.sliderFontSize.setValue(font.getSize());
		} catch (Exception e) {
			logger.warn("Cannot refresh text editor font", e);
		}
	}

	private void addHandlers() {

		// default close operation(same as reset)
		addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {

				logger.debug("Closing GUI_Preferences");

				// reset INItializer reference currentConf too
				resetConfigValues(currentConfiguration);

//				antialiasingApply(currentConfiguration.getTextEditorOpt().antialiasedTxtArea);
//				if (currentEditor != null) {
//					currentEditor.initGuiFromConfig(true, true);
//				}
				logger.debug("Closing " + getClass().getName() + " - Configuration resetted: \n" + currentConfiguration.toStringa());

				isActive.set(false);

				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						try {
							PlasticTheme themeFound = GUIStyelSettingsExtended.getPlasticThemeByName(currentConfiguration.getGuiOpt().getPreferredThemeName(), themes);
							if (themeFound != null) {
								GUIStyelSettingsExtended.currentTheme = themeFound;
							}
							GUIStyelSettingsExtended.setStyle(currentConfiguration.getGuiOpt().getPreferredStyle(), currentConfiguration.getGuiOpt().nimbusSize);
						} catch (Exception e1) {

							PlasticTheme themeFound = GUIStyelSettingsExtended.getPlasticThemeByName(BACKUP_PREFERRED_THEME_NAME, themes);
							if (themeFound != null) {
								GUIStyelSettingsExtended.currentTheme = themeFound;
							}
							GUIStyelSettingsExtended.setStyle(BACKUP_PREFERRED_STYLE, currentConfiguration.getGuiOpt().nimbusSize);
							currentConfiguration.getGuiOpt().setPreferredStyle(BACKUP_PREFERRED_STYLE);
							currentConfiguration.getGuiOpt().setPreferredThemeName(BACKUP_PREFERRED_THEME_NAME);
							dialogHelper.error("Sorry, something went wrong :(", e1.getClass().getName());
							logger.error("Exception happened!", e1);
						} finally {
							GUIStyelSettingsExtended.updateAllWindowsLaf();
							if (currentEditor != null) {
								currentEditor.initGuiFromConfig(true, true);
							}
							UiScaleManager.apply(currentConfiguration.getGuiOpt());
						refreshEditorFont(currentConfiguration.getTextEditorOpt().getFont());

							singletonInstance = null;
						}

						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								MainGUI.updateGuiFromCurrConfig();
							}
						});
					}
				});

				dispose();
			}
		});

		// DARK GUI HANDLER
		chckbxDarklight.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!chckbxDarklight.isSelected()) {
					boolean res = !GUIStyelSettingsExtended.setLightNimbus(currentConfiguration.getGuiOpt().nimbusSize);
					currentConfiguration.getGuiOpt().setDarkGuiNimbus(res);
				} else {
					boolean res = GUIStyelSettingsExtended.setDarkNimbus(currentConfiguration.getGuiOpt().nimbusSize);
					currentConfiguration.getGuiOpt().setDarkGuiNimbus(res);
				}
				SwingUtilities.invokeLater(styleChangedRunnable());
			}
		});

		// ANTIALIASING SELECTOR HANDLER
		chckBoxAntiAliasingON.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean antialiasing = chckBoxAntiAliasingON.isSelected();
				currentConfiguration.getTextEditorOpt().antialiasedTxtArea = antialiasing;
				antialiasingApply(antialiasing);
			}
		});

	//////////////// TEXT AREA DETAIL ///////////////////////

		// TXTAREA_AUTO_INDENT SELECTOR HANDLER
		chckbxAutoindentation.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				currentConfiguration.getTextEditorOpt().autoIndentEnabled = chckbxAutoindentation.isSelected();
			}
		});

		// TXTAREA_ANIMATE_BRACKETS SELECTOR HANDLER
		chckbxAnimateBrackets.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				currentConfiguration.getTextEditorOpt().animateBracketMatching = chckbxAnimateBrackets.isSelected();
			}
		});

		// TXTAREA_PRINT MATHED PAIRS OF BRACKETS SELECTOR HANDLER
		chckbxHightlightBracketPairs.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				currentConfiguration.getTextEditorOpt().paintMatchedBracketPair = chckbxHightlightBracketPairs.isSelected();
			}
		});

		// TXTAREA_SHOW WHITESPACES SELECTOR HANDLER
		chckbxShowWhitespaces.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				currentConfiguration.getTextEditorOpt().whiteSpaceVisible = chckbxShowWhitespaces.isSelected();
			}
		});

		// TXTAREA_BRACKET_MATCHING SELECTOR HANDLER
		chckbxBracketsMatching.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				currentConfiguration.getTextEditorOpt().bracketMatchingEnabled = chckbxBracketsMatching.isSelected();
			}
		});

		// clearWspaces SELECTOR HANDLER
		chckbxClearWhitespaceLines.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				currentConfiguration.getTextEditorOpt().clearWhitespaceLinesEnabled = chckbxClearWhitespaceLines.isSelected();
			}
		});

		// closeCurlyBraces SELECTOR HANDLER
		chckbxCloseCurlybraces.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				currentConfiguration.getTextEditorOpt().closeCurlyBraces = chckbxCloseCurlybraces.isSelected();
			}
		});

		// closeCurlyBraces SELECTOR HANDLER
		chckbxCloseMarkuptags.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				currentConfiguration.getTextEditorOpt().closeMarkupTags = chckbxCloseMarkuptags.isSelected();
			}
		});

		// codeFoldingEnabled SELECTOR HANDLER
		chckbxCodeFolding.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				currentConfiguration.getTextEditorOpt().codeFoldingEnabled = chckbxCodeFolding.isSelected();
			}
		});

		// fadeCurrentLineHighlight SELECTOR HANDLER
		chckbxFadeHighlight.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				currentConfiguration.getTextEditorOpt().fadeCurrentLineHighlight = chckbxFadeHighlight.isSelected();
			}
		});

		// fractionalFontMetricsEnabled SELECTOR HANDLER
		chckbxAllowFractionalFonts.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				currentConfiguration.getTextEditorOpt().fractionalFontMetricsEnabled = chckbxAllowFractionalFonts.isSelected();
			}
		});

		// highlightSecondaryLanguages SELECTOR HANDLER
		chckbxHighlightndLanguages.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				currentConfiguration.getTextEditorOpt().highlightSecondaryLanguages = chckbxHighlightndLanguages.isSelected();
			}
		});

		// hyperLinksEnabled SELECTOR HANDLER
		chckbxHyperlinksEnabled.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				currentConfiguration.getTextEditorOpt().hyperLinksEnabled = chckbxHyperlinksEnabled.isSelected();
			}
		});

		// markOccurrences SELECTOR HANDLER
		checkBoxMarkOccurrences.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				currentConfiguration.getTextEditorOpt().markOccurrences = checkBoxMarkOccurrences.isSelected();
			}
		});

		// paint border markOccurrences SELECTOR HANDLER
		chckbxMarkOccurrencesPaintBorder.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				currentConfiguration.getTextEditorOpt().paintMarkOccurrencesBorder = chckbxMarkOccurrencesPaintBorder.isSelected();
			}
		});

		// roundedSelectionEdges SELECTOR HANDLER
		chckbxRoundSelectionEdges.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				currentConfiguration.getTextEditorOpt().roundedSelectionEdges = chckbxRoundSelectionEdges.isSelected();
			}
		});

		// useFocusableTips SELECTOR HANDLER
		chckbxFocusableTips.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				currentConfiguration.getTextEditorOpt().useFocusableTips = chckbxFocusableTips.isSelected();
			}
		});

		// tabSpacesEmulated SELECTOR HANDLER
		chckbxEmulatedTabsWith.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				currentConfiguration.getTextEditorOpt().tabSpacesEmulated = chckbxEmulatedTabsWith.isSelected();
			}
		});

		// showBracketsPopup SELECTOR HANDLER
		chckbxShowBracket.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				currentConfiguration.getTextEditorOpt().showBracketsPopup = chckbxShowBracket.isSelected();
			}
		});

		// markEndOfLine SELECTOR HANDLER
		chckbxMarkEOL.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				currentConfiguration.getTextEditorOpt().markEndOfLine = chckbxMarkEOL.isSelected();
			}
		});

		// chckbxAutoCheckFiles for modifications/deletes SELECTOR HANDLER
		chckbxAutoCheckFiles.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				currentConfiguration.getGenOpt().setTxtEditorFileMonitorActive(chckbxAutoCheckFiles.isSelected());
			}
		});

		// chckbxSyncClipboardStyle for sync cliboard style with current SELECTOR HANDLER
		chckbxSyncClipboardStyle.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				currentConfiguration.getTextEditorOpt().setClipboardSyncStyle(chckbxSyncClipboardStyle.isSelected());
				syncRightPaneCustomOptions();
			}
		});

		checkBoxAutosaveTempFiles.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				currentConfiguration.getTextEditorOpt().setAutosaveTempFiles(checkBoxAutosaveTempFiles.isSelected());
			}
		});

		// END TXT AREA DETAIL //

		// COMBO STYLE HANDLER
		comboStyleLaF.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(styleChangedRunnable());
			}
		});

		comboTheme.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					GUIStyelSettingsExtended.currentTheme = (PlasticTheme)comboTheme.getSelectedItem();
				} catch (Exception e1) {
					logger.warn("Canot update current selected theme", e1);
				}
				SwingUtilities.invokeLater(styleChangedRunnable());
			}
		});

		// Preferred spellcheckker dictionary
		comboSpellcheckerDictionary.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					FileNamed selected = (FileNamed) comboSpellcheckerDictionary.getSelectedItem();
					currentConfiguration.getTextEditorOpt().setPrefDictspellChecker(GeneralConfig.SPELLCHECKER_DICT_FOLDER+selected.mFile.getName());
				} catch (Exception e2) {
					logger.error("Combo preferred spellcheck dictionary clicked: Error saving current preferred in: "+currentConfiguration.getIniFile().getFile().getAbsolutePath());
				}
			}
		});

		spinnerInterline.addChangeListener((e) -> {
			currentConfiguration.getTextEditorOpt().setInterline((int)spinnerInterline.getValue());
		});

		// RESET BUTTON HANDLER
		btnResetValues.addActionListener(new ActionListener() {

			// action fired when user reset initial values saved in backup values
			@Override
			public void actionPerformed(ActionEvent e) {

				currentEditor = MainGUI.textEditorPanel;

				logger.debug("Starting reset of GUI_Preferences");

				// reset INItializer reference currentConf too
				resetConfigValues(currentConfiguration);

				// reset gui (sliders changement will trigger jtextfield, see action listener)
				updateGUI(true);

//				GUIStyelSettings.updateAllWindowsLaf();

				if (currentEditor != null) {
					currentEditor.getCurrentTabEditor().applyStyleToTabs();
				}

				logger.debug("Configuration resetted: \n" + currentConfiguration.toStringa());
			}
		});

		// SAVE CONFIG BUTTON HANDLER
		btnSaveConf.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				boolean chosenOption = dialogHelper.yesOrNo(
						"Are you sure you want to overwrite " + currentConfiguration.getIniFile().getFile().getPath()
								+ "(ONLY GUI_OPTIONS)?",
						"overwrite configuration");
				if (chosenOption) {

					// write single values to file ini associated to currentConf
					writeValuesToConfFile(currentConfiguration);

					// update BACKUP_VALUES as the new ones
					updateBackup(currentConfiguration);

					updateGUI(true);

					dialogHelper.info( "Graphic Interface settings Saved :)\n -> Changes will be visible after next restart.", "Saved!");
					if (currentEditor != null) {
						currentEditor.textEditorUtils.resetDividerColor();
						currentEditor.getCurrentTabEditor().applyStyleToTabs();
					}
					logger.debug("Configuration saved: \n" + currentConfiguration.toStringa());

					// dispose();
					dispatchEvent(new WindowEvent(thisFrame, WindowEvent.WINDOW_CLOSING));


				} else {
					// cancelled by user: no message needed
					if (currentEditor != null) {
						currentEditor.textEditorUtils.resetDividerColor();
						currentEditor.getCurrentTabEditor().applyStyleToTabs();
					}
				}

			}
		});

		// DISCARD BUTTON HANDLER
		btnDiscard.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				logger.debug("DISCARD => Closing GUI_Preferences");

				// reset current values
				resetConfigValues(currentConfiguration);

				// reset gui (sliders changement will trigger jtextfield, see action listener)
				updateGUI(true);

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
				updateBackup(currentConfiguration);

				updateGUI(true);

				// close and apply new values without saving
				dispatchEvent(new WindowEvent(thisFrame, WindowEvent.WINDOW_CLOSING));
			}
		});

		// TEXT EDITOR FONT HANDLERS (applied live, like the other options)
		comboEditorFontFamily.addActionListener(e -> {
			if (!updatingEditorFont) {
				applyEditorFont();
			}
		});
		spinnerEditorFontSize.addChangeListener(e -> {
			if (!updatingEditorFont) {
				applyEditorFont();
			}
		});

		// INTERFACE SIZE HANDLERS: preset sets its default values, Custom keeps current ones (preview applied live)
		comboUiSize.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (updatingUiSizeCombo) {
					return;
				}
				UiSizePreset preset = (UiSizePreset) comboUiSize.getSelectedItem();
				if (preset == UiSizePreset.CUSTOM) {
					openUiSizeCustomDialog();
				} else {
					currentConfiguration.getGuiOpt().applyUiSizePreset(preset);
					UiScaleManager.apply(currentConfiguration.getGuiOpt());
				}
			}
		});

		btnUiSizeCustomize.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openUiSizeCustomDialog();
			}
		});

		/** SPINNER HANDLERS **/

		// MENU SIZE HANDLER
		spinnerMenuSize.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				currentConfiguration.getGuiOpt().setMenuFontSize((int) spinnerMenuSize.getValue());
				sliderMenuSize.setValue((int) spinnerMenuSize.getValue());
			}
		});

		spinnerLogSize.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				currentConfiguration.getGuiOpt().setLogFontSize((int) spinnerLogSize.getValue());
				sliderLogSize.setValue((int) spinnerLogSize.getValue());
			}
		});

		spinnerNimbusSize.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				currentConfiguration.getGuiOpt().setNimbusSize((int) spinnerNimbusSize.getValue());
				GUIStyelSettingsExtended.applyNimbusSize(null, (int) spinnerNimbusSize.getValue());
//				currentEditor.initGuiFromConfig(true, true);
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						MainGUI.updateGuiFromCurrConfig();
					}
				});
			}
		});

		sliderLogSize.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				spinnerLogSize.setValue(sliderLogSize.getValue());
			}
		});

		sliderMenuSize.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				spinnerMenuSize.setValue(sliderMenuSize.getValue());
			}
		});
	}

	private void openUiSizeCustomDialog() {
		GuiOption guiOpt = currentConfiguration.getGuiOpt();
		new UiScaleCustomDialog(thisFrame, guiOpt, () -> {
			selectUiSizePreset(guiOpt.getUiSizePreset());
			UiScaleManager.apply(guiOpt);
		}).setVisible(true);
		selectUiSizePreset(guiOpt.getUiSizePreset());
	}

	private void selectUiSizePreset(UiSizePreset preset) {
		updatingUiSizeCombo = true;
		comboUiSize.setSelectedItem(preset);
		updatingUiSizeCombo = false;
	}

	private boolean writeValuesToConfFile(INItializer currentConf) {

		currentConf.writeGuiOptions();
		currentConf.writeTxtEditOptions();
		currentConf.store();
		currentConf.writeSingleValue(INItializer.GUI_OPT, "darkGuiNimbus", currentConfiguration.getGuiOpt().isDarkGuiNimbus());

		return true;
	}

	/**
	 * Update this panel elements, and also general graphics according to config
	 **/
	private void updateGUI(boolean forceGuiRefresh) {
		TextEditorOption txtOpt = currentConfiguration.getTextEditorOpt();
		GuiOption guiOpt = currentConfiguration.getGuiOpt();

		spinnerNimbusSize.setValue(guiOpt.nimbusSize);
		sliderMenuSize.setValue(guiOpt.getMenuFontSize());
		spinnerMenuSize.setValue(guiOpt.getMenuFontSize());
		sliderLogSize.setValue(guiOpt.getLogFontSize());
		spinnerLogSize.setValue(guiOpt.getLogFontSize());
		updateComboLaFGUI(guiOpt.getPreferredStyle());
		updateComboLaFGUITheme(guiOpt.getPreferredThemeName());
		chckbxDarklight.setSelected(guiOpt.isDarkGuiNimbus());
		selectUiSizePreset(guiOpt.getUiSizePreset());
		UiScaleManager.apply(guiOpt);

		updatingEditorFont = true;
		comboEditorFontFamily.setSelectedItem(txtOpt.getFont().getFamily());
		spinnerEditorFontSize.setValue(txtOpt.getFont().getSize());
		updatingEditorFont = false;

		// TEXT AREA
		chckBoxAntiAliasingON.setSelected(txtOpt.antialiasedTxtArea);
		chckbxAllowFractionalFonts.setSelected(txtOpt.fractionalFontMetricsEnabled);
		chckbxAnimateBrackets.setSelected(txtOpt.animateBracketMatching);
		chckbxHightlightBracketPairs.setSelected(txtOpt.paintMatchedBracketPair);
		chckbxAutoindentation.setSelected(txtOpt.autoIndentEnabled);
		chckbxBracketsMatching.setSelected(txtOpt.bracketMatchingEnabled);
		chckbxClearWhitespaceLines.setSelected(txtOpt.clearWhitespaceLinesEnabled);
		chckbxShowWhitespaces.setSelected(txtOpt.whiteSpaceVisible);
		chckbxCloseCurlybraces.setSelected(txtOpt.closeCurlyBraces);
		chckbxCloseMarkuptags.setSelected(txtOpt.closeMarkupTags);
		chckbxCodeFolding.setSelected(txtOpt.codeFoldingEnabled);
		chckbxFadeHighlight.setSelected(txtOpt.fadeCurrentLineHighlight);
		chckbxFocusableTips.setSelected(txtOpt.useFocusableTips);
		chckbxHighlightndLanguages.setSelected(txtOpt.highlightSecondaryLanguages);
		chckbxHyperlinksEnabled.setSelected(txtOpt.hyperLinksEnabled);
		checkBoxMarkOccurrences.setSelected(txtOpt.markOccurrences);
		chckbxMarkOccurrencesPaintBorder.setSelected(txtOpt.paintMarkOccurrencesBorder);
		chckbxRoundSelectionEdges.setSelected(txtOpt.roundedSelectionEdges);
		chckbxEmulatedTabsWith.setSelected(txtOpt.tabSpacesEmulated);
		chckbxMarkEOL.setSelected(txtOpt.markEndOfLine);
		chckbxShowBracket.setSelected(txtOpt.showBracketsPopup);
		chckbxAutoCheckFiles.setSelected(currentConfiguration.getGenOpt().isTxtEditorFileMonitorActive());
		chckbxSyncClipboardStyle.setSelected(txtOpt.clipboardSyncStyle);
		checkBoxAutosaveTempFiles.setSelected(currentConfiguration.getTextEditorOpt().isAutosaveTempFiles());
		spinnerInterline.setValue(txtOpt.getInterline());
		refreshTextEditorArea();
		syncRightPaneCustomOptions();


		for (int i = 0; i < comboSpellcheckerDictionary.getItemCount(); i++) {
			FileNamed currentEl = comboSpellcheckerDictionary.getItemAt(i);

			if (currentEl.mFile.getAbsoluteFile().equals(new File(txtOpt.prefDictspellChecker).getAbsoluteFile())) {
				comboSpellcheckerDictionary.setSelectedItem(currentEl);
				break;
			}
		}

		GUIStyelSettingsExtended.applyNimbusSize(null, (int) spinnerNimbusSize.getValue());

		antialiasingApply(currentConfiguration.getTextEditorOpt().antialiasedTxtArea);
	}

	private void refreshTextEditorArea() {
		try {
			currentEditor.getCurrentTabEditor().loadedTabObj.textArea.repaint();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void syncRightPaneCustomOptions() {
		if (currentEditor != null) {
			TogglePanelTextArea rightPane = currentEditor.textEditorUtils.getTypedIRightPanel();
			if (rightPane != null && rightPane instanceof ClipboardPanel) {
				((ClipboardPanel) rightPane).sync();
			}
		}
	}

	/**
	 * Refresh backups with current values from config
	 */
	private void updateBackup(INItializer currentConf) {

		TextEditorOption txtOpt = currentConf.getTextEditorOpt();
		GuiOption guiOpt = currentConf.getGuiOpt();

		// initialize default values (by value)
		BACKUP_MENU_FONT_SIZE = guiOpt.getMenuFontSize();
		BACKUP_LOG_PANEL_FONT_SIZE = guiOpt.getLogFontSize();
		BACKUP_PREFERRED_STYLE = new String(guiOpt.getPreferredStyle());
		BACKUP_PREFERRED_THEME_NAME = guiOpt.getPreferredThemeName();
		BACKUP_DARK_THEME = guiOpt.isDarkGuiNimbus();
		BACKUP_NIMBUS_SIZE = guiOpt.getNimbusSize();
		BACKUP_UI_SIZE.copyUiSizeFrom(guiOpt);
		BACKUP_EDITOR_FONT = txtOpt.getFont();

		// detail backup
		BACKUP_TXTAREA_ANTIALIASED = txtOpt.isAntialiasedTxtArea();
		BACKUP_TXTAREA_ANIMATE_BRACKETS = txtOpt.animateBracketMatching;
		BACKUP_TXTAREA_AUTO_INDENT = txtOpt.autoIndentEnabled;
		BACKUP_TXTAREA_HIGHLIGHT_BRACKET_PAIRS = txtOpt.paintMatchedBracketPair;
		BACKUP_TXTAREA_BRACKET_MATCHING = txtOpt.bracketMatchingEnabled;
		BACKUP_TXTAREA_SHOW_WHITESPACES = txtOpt.whiteSpaceVisible;
		BACKUP_TXTAREA_CLEAR_WHITESP_LINES = txtOpt.clearWhitespaceLinesEnabled;
		BACKUP_TXTAREA_CLOSE_CURLY_BRACE = txtOpt.closeCurlyBraces;
		BACKUP_TXTAREA_CLOSE_MARKUP_TAG = txtOpt.closeMarkupTags;
		BACKUP_TXTAREA_CODE_FOLDING = txtOpt.codeFoldingEnabled;
		BACKUP_TXTAREA_CURR_LINE_HIGHLIGHT = txtOpt.fadeCurrentLineHighlight;
		BACKUP_TXTAREA_FRACT_FONT_METRICS = txtOpt.fractionalFontMetricsEnabled;
		BACKUP_TXTAREA_HIGLIGHT_2ND_LANG = txtOpt.highlightSecondaryLanguages;
		BACKUP_TXTAREA_HYPERLINKS = txtOpt.hyperLinksEnabled;
		BACKUP_TXTAREA_MARK_OCCURRENCES = txtOpt.markOccurrences;
		BACKUP_TXTAREA_PAINT_MARK_OCCURRENCES = txtOpt.paintMarkOccurrencesBorder;
		BACKUP_TXTAREA_ROUND_SEL_EDGES = txtOpt.roundedSelectionEdges;
		BACKUP_TXTAREA_FOCUSABLE_TIPS = txtOpt.useFocusableTips;
		BACKUP_TXTAREA_TABSPACES_EMULATED = txtOpt.tabSpacesEmulated;
		BACKUP_TXTAREA_SHOW_BRACKETS_POPUP = txtOpt.showBracketsPopup;
		BACKUP_TXTAREA_SHOW_EOL_DELIM = txtOpt.markEndOfLine;
		BACKUP_TXTAREA_SPECCLCHECKER_DICT = txtOpt.getPrefDictspellChecker();
		BACKUP_INTERLINE = txtOpt.getInterline();
		BACKUP_SYNC_CLIPBOARD_STYLE = txtOpt.clipboardSyncStyle;
		BACKUP_AUTOSAVE_TEMP_FILES = txtOpt.autosaveTempFiles;
		BACKUP_TXTAREA_AUTOCHECK_CHANGES = currentConf.getGenOpt().isTxtEditorFileMonitorActive();
	}

	/**
	 * Reset current configuration from backup values
	 * @param currentConf
	 */
	private void resetConfigValues(INItializer currentConf) {
		TextEditorOption txtOpt = currentConf.getTextEditorOpt();
		GuiOption guiOpt = currentConf.getGuiOpt();

		guiOpt.setPreferredStyle(BACKUP_PREFERRED_STYLE);
		guiOpt.setPreferredThemeName(BACKUP_PREFERRED_THEME_NAME);
		guiOpt.setMenuFontSize(BACKUP_MENU_FONT_SIZE);
		guiOpt.setLogFontSize(BACKUP_LOG_PANEL_FONT_SIZE);
		guiOpt.setDarkGuiNimbus(BACKUP_DARK_THEME);
		guiOpt.setNimbusSize(BACKUP_NIMBUS_SIZE);
		guiOpt.copyUiSizeFrom(BACKUP_UI_SIZE);
		txtOpt.setFont(BACKUP_EDITOR_FONT);

		txtOpt.setAntialiasedTxtArea(BACKUP_TXTAREA_ANTIALIASED);
		txtOpt.setAnimateBracketMatching(BACKUP_TXTAREA_ANIMATE_BRACKETS);
		txtOpt.setPaintMatchedBracketPair(BACKUP_TXTAREA_HIGHLIGHT_BRACKET_PAIRS);
		txtOpt.setAutoIndentEnabled(BACKUP_TXTAREA_AUTO_INDENT);
		txtOpt.setBracketMatchingEnabled(BACKUP_TXTAREA_BRACKET_MATCHING);
		txtOpt.setClearWhitespaceLinesEnabled(BACKUP_TXTAREA_CLEAR_WHITESP_LINES);
		txtOpt.setWhiteSpaceVisible(BACKUP_TXTAREA_SHOW_WHITESPACES);
		txtOpt.setCloseCurlyBraces(BACKUP_TXTAREA_CLOSE_CURLY_BRACE);
		txtOpt.setCloseMarkupTags(BACKUP_TXTAREA_CLOSE_MARKUP_TAG);
		txtOpt.setCodeFoldingEnabled(BACKUP_TXTAREA_CODE_FOLDING);
		txtOpt.setFadeCurrentLineHighlight(BACKUP_TXTAREA_CURR_LINE_HIGHLIGHT);
		txtOpt.setFractionalFontMetricsEnabled(BACKUP_TXTAREA_FRACT_FONT_METRICS);
		txtOpt.setHighlightSecondaryLanguages(BACKUP_TXTAREA_HIGLIGHT_2ND_LANG);
		txtOpt.setHyperLinksEnabled(BACKUP_TXTAREA_HYPERLINKS);
		txtOpt.setMarkOccurrences(BACKUP_TXTAREA_MARK_OCCURRENCES);
		txtOpt.setPaintMarkOccurrencesBorder(BACKUP_TXTAREA_PAINT_MARK_OCCURRENCES);
		txtOpt.setRoundedSelectionEdges(BACKUP_TXTAREA_ROUND_SEL_EDGES);
		txtOpt.setUseFocusableTips(BACKUP_TXTAREA_FOCUSABLE_TIPS);
		txtOpt.setTabSpacesEmulated(BACKUP_TXTAREA_TABSPACES_EMULATED);
		txtOpt.setMarkEndOfLine(BACKUP_TXTAREA_SHOW_EOL_DELIM);
		txtOpt.setShowBracketsPopup(BACKUP_TXTAREA_SHOW_BRACKETS_POPUP);
		txtOpt.setPrefDictspellChecker(BACKUP_TXTAREA_SPECCLCHECKER_DICT);
		txtOpt.setInterline(BACKUP_INTERLINE);
		txtOpt.setClipboardSyncStyle(BACKUP_SYNC_CLIPBOARD_STYLE);
		txtOpt.setAutosaveTempFiles(BACKUP_AUTOSAVE_TEMP_FILES);
		currentConf.getGenOpt().setTxtEditorFileMonitorActive(BACKUP_TXTAREA_AUTOCHECK_CHANGES);
	}


	public void updateMenuAndLogFontSize(JFrame jframe) {
		GuiOption guiOpt = currentConfiguration.getGuiOpt();

		// use the same font as main gui but change font size
		UIManager.put("MenuBar.font", new Font("Segoe UI", Font.BOLD, guiOpt.getMenuFontSize()));
		UIManager.put("Menu.font", new Font("Segoe UI", Font.BOLD, guiOpt.getMenuFontSize()));
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

	private void updateComboLaFGUITheme(String themeName) {

		for (int i = 0; i < comboTheme.getItemCount(); i++) {
			try {
				PlasticTheme currentTheme = (comboTheme.getItemAt(i));
				if (currentTheme.getName().equalsIgnoreCase(themeName)) {
					comboTheme.setSelectedIndex(i);
					break;
				}
			} catch (Exception e) {
				logger.warn("Canot update current selected theme", e);
			}
		}
	}

	public void initGuiFromConfig() {
		updateGUI(false);
	}

	private void antialiasingApply(boolean antialiasing) {
		try {
			if (currentEditor != null) {
				currentEditor.getCurrentTabEditor().loadedTabObj.textArea.setAntiAliasingEnabled(antialiasing);
			}
		} catch (Exception e1) {
			logger.warn("Error while preiewing antialising effect on textarea", e1);
		}
	}

	private Runnable styleChangedRunnable() {
		return new Runnable() {

			@Override
			public void run() {
				try {
					LookAndFeelInfoCustom laf = (LookAndFeelInfoCustom) comboStyleLaF.getSelectedItem();
					styleBasedThemesSelectorToggle(laf);
					styleBasedDarkNimbusToggle(laf);
					styleBasedSizeNimbusToggle(laf);
					currentConfiguration.getGuiOpt().setPreferredStyle(laf.getClassName());
					try {
						currentConfiguration.getGuiOpt().setPreferredThemeName(((PlasticTheme)comboTheme.getSelectedItem()).getName());
					} catch (Exception e) {
						logger.warn("Canot update current selected theme", e);
					}
					GUIStyelSettingsExtended.updateThemeByName(currentConfiguration.getGuiOpt().getPreferredThemeName());
					GUIStyelSettingsExtended.setStyle(laf.getClassName(), currentConfiguration.getGuiOpt().nimbusSize);
					GUIStyelSettingsExtended.applyNimbusSize(null, currentConfiguration.getGuiOpt().nimbusSize);

				} catch (Exception e1) {

					GUIStyelSettingsExtended.updateThemeByName(BACKUP_PREFERRED_THEME_NAME);
					GUIStyelSettingsExtended.setStyle(BACKUP_PREFERRED_STYLE, currentConfiguration.getGuiOpt().nimbusSize);
					currentConfiguration.getGuiOpt().setPreferredStyle(BACKUP_PREFERRED_STYLE);
					currentConfiguration.getGuiOpt().setPreferredThemeName(BACKUP_PREFERRED_THEME_NAME);
					dialogHelper.error("Sorry, something went wrong :(", e1.getClass().getName());
					logger.error("Exception happened!", e1);
				} finally {
					GUIStyelSettingsExtended.updateAllWindowsLaf();
					if (currentEditor != null) {
						currentEditor.initGuiFromConfig(true, true);
					}
					// the LAF switch can push this window behind the main one
					SwingUtilities.invokeLater(thisFrame::toFront);
				}
			}
		};
	}

	private void styleBasedThemesSelectorToggle(LookAndFeelInfoCustom laf) {
		if(laf.isJGoodiesThemed() || laf.isMetal()) {
			comboTheme.setEnabled(true);
		} else {
			comboTheme.setEnabled(false);
		}
	}

	private void styleBasedDarkNimbusToggle(LookAndFeelInfoCustom laf) {
		if(laf.isNimbus()) {
			chckbxDarklight.setEnabled(true);
		} else {
			chckbxDarklight.setEnabled(false);
		}
	}

	private void styleBasedSizeNimbusToggle(LookAndFeelInfoCustom laf) {
		if(laf.isNimbus()) {
			spinnerNimbusSize.setEnabled(true);
		} else {
			spinnerNimbusSize.setEnabled(false);
		}
	}

	@Override
	/** Fits all the options without scrolling, up to 90% of the main window monitor (maximize stays available) */
	public Dimension getDefaultDimension() {

		return OptionsPanel.fitWindowSize(this, getMinimumDimension(), MAX_SCREEN_PERCENT,
				MainGUI.mainFrameScreept != null ? MainGUI.mainFrameScreept : this);
	}

	@Override
	public Dimension getMinimumDimension() {
		return new Dimension(680, 460);
	}

	public static GraphicPreferencesFrame getInstance(INItializer currentConf) {
		return singletonInstance == null ? new GraphicPreferencesFrame(currentConf) : singletonInstance;
	}

}

class ThemeCellRenderer implements ListCellRenderer<Object> {
	protected DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

	static Logger logger = Logger.getLogger(ThemeCellRenderer.class);

	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		JLabel renderer = (JLabel) defaultRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		if (value instanceof PlasticTheme) {
			try {
				PlasticTheme plasticValue = (PlasticTheme)value;
				renderer.setText(plasticValue.getName());
			} catch (Exception e) {
				logger.warn("Canot update current selected theme", e);
			}
			renderer.setFont(GeneralConfig.BUTTON_DEFAULT_FONT);
		}
		return renderer;
	}
}

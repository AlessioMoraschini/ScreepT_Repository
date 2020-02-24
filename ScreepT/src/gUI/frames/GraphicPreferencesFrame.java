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
package gUI.frames;

import javax.swing.JFrame;
import net.miginfocom.swing.MigLayout;
import om.GuiOption;
import om.TextEditorOption;
import resources.GeneralConfig;
import resources.IconsPathConfigurator;
import utils.RsyntaxUtils;
import utils.panels.ClipboardPanel;
import utils.panels.arch.ParentPanel;

import javax.swing.JLabel;
import javax.swing.JList;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;

import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;

import com.jgoodies.looks.plastic.PlasticTheme;

import dialogutils.JOptionHelperExtended;
import files.om.FileNamed;
import frameutils.frame.arch.ParentFrame;
import frameutils.utils.GuiUtilsExtended;
import gUI.panels.TextEditor;
import guistyle.GUIStyelSettingsExtended;
import guistyle.LookAndFeelInfoCustom;
import maingUI.MainGUI;
import mainobj.INItializer;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JSlider;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.ListCellRenderer;
import javax.swing.SpinnerNumberModel;
import javax.swing.JCheckBox;
import java.awt.Dimension;
import java.awt.ComponentOrientation;

public class GraphicPreferencesFrame extends ParentFrame {
	private static final long serialVersionUID = 6658781789560743187L;
	
	public static GraphicPreferencesFrame singletonInstance = null;

	// initialize Logger
	// GraphicPreferencesFrame logger
	static Logger logger = Logger.getLogger(GraphicPreferencesFrame.class);
	
	// default values
	private String BACKUP_PREFERRED_STYLE;
	private String BACKUP_PREFERRED_THEME_NAME;
	private int BACKUP_MENU_FONT_SIZE;
	private int BACKUP_LOG_PANEL_FONT_SIZE;
	private boolean BACKUP_DARK_THEME;
	private int BACKUP_NIMBUS_SIZE;

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
	private String BACKUP_TXTAREA_SPECCLCHECKER_DICT;
	private int BACKUP_INTERLINE;

	private INItializer currentConfiguration;
	
	private TextEditor currentEditor;
	
	// GUI elements
	private JPanel mainPanel;
	private JScrollPane mainPanelContainer;
	private final JFrame thisFrame;
	
	private JSlider sliderMenuSize;
	private JSlider sliderLogSize;
	private JLabel lblTheme;
	private ArrayList<PlasticTheme> themes;
	private JComboBox<PlasticTheme> comboTheme;
	private JComboBox<LookAndFeelInfoCustom> comboStyleLaF;
	private JComboBox<FileNamed> comboSpellcheckerDictionary;

	private JButton btnDiscard;
	private JButton btnUseSettings;
	private JButton btnSaveConf;
	private JButton btnResetValues;
	private JLabel lblNimbusSize;
	private JSpinner spinnerMenuSize;
	private JSpinner spinnerLogSize;
	private JSpinner spinnerNimbusSize;
	
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
	private JLabel lblTextAreaOptions;
	private JLabel lblGenericGraphicalOptions;
	private JLabel lblSpellcheckerDictionarySelected;
	private JSeparator separator_2;
	private JSeparator separator_3;
	private JSeparator separator_4;
	private JSeparator separator_5;
	
	JOptionHelperExtended dialogHelper;
	private JSpinner spinnerInterline;
	private JLabel lblInterline;
	
	public GraphicPreferencesFrame(INItializer currentConf) {
		
		isActive.set(true);
		
		thisFrame = this;
		
		singletonInstance = this;
		
		currentEditor = MainGUI.textEditorPanel;
		
		logger.info(this.getClass().getName() + " Starting...");

		currentConfiguration = currentConf;
		
		dialogHelper = new JOptionHelperExtended(this);
		
		this.setAlwaysOnTop(true);
		this.setResizable(false);

		GuiUtilsExtended.setFrameIcon(IconsPathConfigurator.ICON_GUI_OPT, this);
		setLocationRelativeTo(MainGUI.mainFrameScreept);
		
		updateBackup(currentConfiguration);

		logger.debug("Backup Initialized.");

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		mainPanel = new JPanel();
		mainPanel.setBackground(new Color(0, 0, 0));
		mainPanel.setBackground(Color.LIGHT_GRAY);
		mainPanel.setLayout(new MigLayout("", "[:30px:140px][:60px:140px][:30px:90px,grow][:10px:30px,grow][25px,grow][70px:70px][10][:100px:10px,grow][10px,grow][35px,grow][:60px:70px][:30px:40px][:50px:50px]", "[:50:70px][25][10][30][20][30][30][30][30][30][30][][][:5px:10px,grow][:15px:100px,grow,baseline]"));
		mainPanelContainer = new JScrollPane(mainPanel);
		getContentPane().add(mainPanelContainer);

		JPanel panel = new JPanel();
		panel.setForeground(SystemColor.controlText);
		panel.setBorder(new LineBorder(null, 4));
		panel.setBackground(Color.DARK_GRAY);
		mainPanel.add(panel, "cell 0 0 13 1,grow");
		panel.setLayout(new MigLayout("", "[100px][][70px][grow][][][][]", "[100]"));
		
		chckbxDarklight = new JCheckBox("Nimbus Dark theme");
		chckbxDarklight.setOpaque(false);
		chckbxDarklight.setForeground(Color.WHITE);
		panel.add(chckbxDarklight, "cell 0 0 2 1");
		chckbxDarklight.setCursor(GuiUtilsExtended.CURSOR_HAND);
		chckbxDarklight.setFont(new Font("SansSerif", Font.BOLD, 11));
				
		JLabel lblGraphicInterfaceSettings = new JLabel("GUI   Preferences");
		panel.add(lblGraphicInterfaceSettings, "cell 3 0,alignx center");
		lblGraphicInterfaceSettings.setHorizontalAlignment(SwingConstants.RIGHT);
		lblGraphicInterfaceSettings.setForeground(Color.WHITE);
		lblGraphicInterfaceSettings.setFont(new Font("Lithos Pro Regular", Font.BOLD, 22));
		lblGraphicInterfaceSettings.setBackground(Color.LIGHT_GRAY);
				
		lblNimbusSize = new JLabel("Nimbus size variant");
		lblNimbusSize.setForeground(Color.WHITE);
		panel.add(lblNimbusSize, "cell 6 0");
		lblNimbusSize.setFont(new Font("SansSerif", Font.BOLD, 11));

		spinnerNimbusSize = new JSpinner();
		spinnerNimbusSize.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		spinnerNimbusSize.setCursor(GuiUtilsExtended.CURSOR_HAND);
		spinnerNimbusSize.setModel(new SpinnerNumberModel(0, 0, 2, 1));
		spinnerNimbusSize.setValue(currentConf.getGuiOpt().nimbusSize);
		spinnerNimbusSize.setFont(GeneralConfig.BUTTON_DEFAULT_FONT);
		panel.add(spinnerNimbusSize, "cell 7 0,alignx right");
						
		separator_4 = new JSeparator();
		separator_4.setPreferredSize(new Dimension(0, 1));
		separator_4.setMinimumSize(new Dimension(1, 1));
		separator_4.setOpaque(true);
		separator_4.setForeground(Color.DARK_GRAY);
		separator_4.setBackground(Color.GRAY);
		mainPanel.add(separator_4, "cell 0 1,growx");
		
		lblGenericGraphicalOptions = new JLabel("Generic Graphical Options");
		lblGenericGraphicalOptions.setForeground(Color.GRAY);
		lblGenericGraphicalOptions.setFont(new Font("Tahoma", Font.BOLD, 13));
		mainPanel.add(lblGenericGraphicalOptions, "cell 1 1 3 1,alignx left");
		
		separator_2 = new JSeparator();
		separator_2.setPreferredSize(new Dimension(0, 1));
		separator_2.setMinimumSize(new Dimension(1, 1));
		separator_2.setOpaque(true);
		separator_2.setForeground(Color.DARK_GRAY);
		separator_2.setBackground(Color.GRAY);
		mainPanel.add(separator_2, "cell 4 1 9 1,growx");

		JLabel lblGuiType = new JLabel("GUI Style");
		lblGuiType.setFont(new Font("Tahoma", Font.BOLD, 13));
		mainPanel.add(lblGuiType, "cell 0 2,alignx center");
		
		lblTheme = new JLabel("Theme");
		lblTheme.setFont(new Font("Tahoma", Font.BOLD, 13));
		mainPanel.add(lblTheme, "cell 5 2,alignx center");
		
		spinnerInterline = new JSpinner();
		spinnerInterline.setModel(new SpinnerNumberModel(0, 0, 20, 1));
		spinnerInterline.setCursor(GuiUtilsExtended.CURSOR_HAND);
		mainPanel.add(spinnerInterline, "cell 9 10,growx");
		
		lblInterline = new JLabel("Interline");
		lblInterline.setFont(new Font("Tahoma", Font.BOLD, 13));
		mainPanel.add(lblInterline, "cell 10 10 3 1");
		
		lblSpellcheckerDictionarySelected = new JLabel("Spellchecker dictionary:");
		lblSpellcheckerDictionarySelected.setFont(new Font("Tahoma", Font.BOLD, 13));
		mainPanel.add(lblSpellcheckerDictionarySelected, "cell 0 12 2 1,alignx center");

//		comboStyleLaF = new JComboBox<LookAndFeelInfoCustom>();
		comboStyleLaF = new JComboBox<LookAndFeelInfoCustom>(GUIStyelSettingsExtended.getLookAndFeelList());
		comboStyleLaF.setPreferredSize(new Dimension(200, 26));
		comboStyleLaF.setMinimumSize(new Dimension(200, 26));
		comboStyleLaF.setFont(GeneralConfig.BUTTON_DEFAULT_FONT);
		comboStyleLaF.setCursor(GuiUtilsExtended.CURSOR_HAND);
		comboStyleLaF.setMaximumRowCount(10);
		comboStyleLaF.setMaximumSize(new Dimension(2000, 50));
		mainPanel.add(comboStyleLaF, "cell 1 2 4 1,grow");
		
//		comboTheme = new JComboBox();
		themes = GUIStyelSettingsExtended.getPlasticThemes();
		comboTheme = new JComboBox<PlasticTheme>(themes.toArray(new PlasticTheme[themes.size()]));
		comboTheme.setRenderer(new ThemeCellRenderer());
		comboTheme.setCursor(GuiUtilsExtended.CURSOR_HAND);
		comboTheme.setPreferredSize(new Dimension(200, 26));
		comboTheme.setFont(GeneralConfig.BUTTON_DEFAULT_FONT);
		comboTheme.setMinimumSize(new Dimension(200, 26));
		comboTheme.setMaximumSize(new Dimension(2000, 50));
		comboTheme.setMaximumRowCount(10);
		comboTheme.setFont(null);
		mainPanel.add(comboTheme, "cell 6 2 7 1,growx");

//		comboSpellcheckerDictionary = new JComboBox<FileNamed>();
		comboSpellcheckerDictionary = new JComboBox<FileNamed>(RsyntaxUtils.getSpellCheckerDIctionaries());
		comboSpellcheckerDictionary.setCursor(GuiUtilsExtended.CURSOR_HAND);
		comboSpellcheckerDictionary.setFont(GeneralConfig.BUTTON_DEFAULT_FONT);
		mainPanel.add(comboSpellcheckerDictionary, "cell 2 12 4 1,growx");

		JLabel lblMenuFontSize = new JLabel("Menu font size");
		lblMenuFontSize.setFont(new Font("Tahoma", Font.BOLD, 13));
		mainPanel.add(lblMenuFontSize, "cell 0 3,alignx right");

		spinnerMenuSize = new JSpinner();
		spinnerMenuSize.setCursor(GuiUtilsExtended.CURSOR_HAND);
		spinnerMenuSize.setModel(new SpinnerNumberModel(12, 5, 24, 1));
		spinnerMenuSize.setFont(GeneralConfig.BUTTON_DEFAULT_FONT);
		mainPanel.add(spinnerMenuSize, "cell 1 3,growx");

		sliderMenuSize = new JSlider();
		sliderMenuSize.setCursor(GuiUtilsExtended.CURSOR_HAND);
		sliderMenuSize.setMaximum(24);
		sliderMenuSize.setMinimum(5);
		mainPanel.add(sliderMenuSize, "cell 2 3 4 1,growx");
		
		JLabel lblFontSize = new JLabel("  Log Font size");
		lblFontSize.setFont(new Font("Tahoma", Font.BOLD, 13));
		mainPanel.add(lblFontSize, "cell 6 3,alignx center");

		spinnerLogSize = new JSpinner();
		spinnerLogSize.setCursor(GuiUtilsExtended.CURSOR_HAND);
		spinnerLogSize.setFont(GeneralConfig.BUTTON_DEFAULT_FONT);
		spinnerLogSize.setModel(new SpinnerNumberModel(12, 5, 30, 1));
		mainPanel.add(spinnerLogSize, "cell 7 3 2 1,growx");
		
		sliderLogSize = new JSlider();
		sliderLogSize.setCursor(GuiUtilsExtended.CURSOR_HAND);
		sliderLogSize.setMaximum(30);
		sliderLogSize.setMinimum(5);
		mainPanel.add(sliderLogSize, "cell 9 3 4 1,growx");
		
		separator_3 = new JSeparator();
		separator_3.setPreferredSize(new Dimension(0, 1));
		separator_3.setMinimumSize(new Dimension(1, 1));
		separator_3.setOpaque(true);
		separator_3.setForeground(Color.DARK_GRAY);
		separator_3.setBackground(Color.GRAY);
		mainPanel.add(separator_3, "cell 0 4,growx");
		
		lblTextAreaOptions = new JLabel("Text Eitor Options");
		lblTextAreaOptions.setForeground(Color.GRAY);
		lblTextAreaOptions.setFont(new Font("Tahoma", Font.BOLD, 13));
		mainPanel.add(lblTextAreaOptions, "cell 1 4 2 1,alignx left");

		JSeparator separator = new JSeparator();
		separator.setPreferredSize(new Dimension(0, 1));
		separator.setMinimumSize(new Dimension(1, 1));
		separator.setOpaque(true);
		separator.setForeground(Color.DARK_GRAY);
		separator.setBackground(Color.GRAY);
		mainPanel.add(separator, "cell 3 4 10 1,growx");
		
		chckBoxAntiAliasingON = new JCheckBox("Antialiasing enabled");
		chckBoxAntiAliasingON.setOpaque(false);
		chckBoxAntiAliasingON.setCursor(GuiUtilsExtended.CURSOR_HAND);
		chckBoxAntiAliasingON.setFont(new Font("SansSerif", Font.PLAIN, 13));
		mainPanel.add(chckBoxAntiAliasingON, "cell 0 5 4 1,alignx left");
		
		chckbxAutoindentation = new JCheckBox("Auto-Indentation");
		chckbxAutoindentation.setOpaque(false);
		chckbxAutoindentation.setCursor(GuiUtilsExtended.CURSOR_HAND);
		chckbxAutoindentation.setSelected(false);
		chckbxAutoindentation.setFont(new Font("SansSerif", Font.PLAIN, 13));
		mainPanel.add(chckbxAutoindentation, "cell 4 5 2 1,alignx left");
		
		chckbxAnimateBrackets = new JCheckBox("Animate Brackets");
		chckbxAnimateBrackets.setOpaque(false);
		chckbxAnimateBrackets.setCursor(GuiUtilsExtended.CURSOR_HAND);
		chckbxAnimateBrackets.setSelected(false);
		chckbxAnimateBrackets.setFont(new Font("SansSerif", Font.PLAIN, 13));
		mainPanel.add(chckbxAnimateBrackets, "cell 6 5,alignx left");
		
		chckbxBracketsMatching = new JCheckBox("Brackets Matching");
		chckbxBracketsMatching.setOpaque(false);
		chckbxBracketsMatching.setCursor(GuiUtilsExtended.CURSOR_HAND);
		chckbxBracketsMatching.setSelected(false);
		chckbxBracketsMatching.setFont(new Font("SansSerif", Font.PLAIN, 13));
		mainPanel.add(chckbxBracketsMatching, "cell 9 5 4 1,alignx left");
		
		chckbxHightlightBracketPairs = new JCheckBox("Print matched bracket pairs");
		chckbxHightlightBracketPairs.setOpaque(false);
		chckbxHightlightBracketPairs.setCursor(GuiUtilsExtended.CURSOR_HAND);
		chckbxHightlightBracketPairs.setSelected(false);
		chckbxHightlightBracketPairs.setFont(new Font("SansSerif", Font.PLAIN, 13));
		mainPanel.add(chckbxHightlightBracketPairs, "cell 0 6 4 1");
		
		chckbxCloseCurlybraces = new JCheckBox("Close Curly-Braces");
		chckbxCloseCurlybraces.setOpaque(false);
		chckbxCloseCurlybraces.setCursor(GuiUtilsExtended.CURSOR_HAND);
		chckbxCloseCurlybraces.setSelected(false);
		chckbxCloseCurlybraces.setFont(new Font("SansSerif", Font.PLAIN, 13));
		mainPanel.add(chckbxCloseCurlybraces, "cell 4 6 2 1,alignx left");
		
		chckbxCloseMarkuptags = new JCheckBox("Close Markup-Tags");
		chckbxCloseMarkuptags.setOpaque(false);
		chckbxCloseMarkuptags.setCursor(GuiUtilsExtended.CURSOR_HAND);
		chckbxCloseMarkuptags.setSelected(false);
		chckbxCloseMarkuptags.setFont(new Font("SansSerif", Font.PLAIN, 13));
		mainPanel.add(chckbxCloseMarkuptags, "cell 6 6,alignx left");
		
		chckbxFocusableTips = new JCheckBox("Focusable Tips:");
		chckbxFocusableTips.setOpaque(false);
		chckbxFocusableTips.setCursor(GuiUtilsExtended.CURSOR_HAND);
		chckbxFocusableTips.setSelected(false);
		chckbxFocusableTips.setFont(new Font("SansSerif", Font.PLAIN, 13));
		mainPanel.add(chckbxFocusableTips, "cell 9 6 4 1,alignx left");
		
		chckbxCodeFolding = new JCheckBox("Code Folding");
		chckbxCodeFolding.setOpaque(false);
		chckbxCodeFolding.setCursor(GuiUtilsExtended.CURSOR_HAND);
		chckbxCodeFolding.setSelected(false);
		chckbxCodeFolding.setFont(new Font("SansSerif", Font.PLAIN, 13));
		mainPanel.add(chckbxCodeFolding, "cell 0 7 4 1,alignx left");
		
		chckbxAllowFractionalFonts = new JCheckBox("Allow Fractional Fonts");
		chckbxAllowFractionalFonts.setOpaque(false);
		chckbxAllowFractionalFonts.setCursor(GuiUtilsExtended.CURSOR_HAND);
		chckbxAllowFractionalFonts.setSelected(false);
		chckbxAllowFractionalFonts.setFont(new Font("SansSerif", Font.PLAIN, 13));
		mainPanel.add(chckbxAllowFractionalFonts, "cell 4 7 2 1,alignx left");
		
		chckbxFadeHighlight = new JCheckBox("Fade Highlight");
		chckbxFadeHighlight.setOpaque(false);
		chckbxFadeHighlight.setCursor(GuiUtilsExtended.CURSOR_HAND);
		chckbxFadeHighlight.setSelected(false);
		chckbxFadeHighlight.setFont(new Font("SansSerif", Font.PLAIN, 13));
		mainPanel.add(chckbxFadeHighlight, "cell 6 7,alignx left");
		
		chckbxHighlightndLanguages = new JCheckBox("Highlight 2nd Languages");
		chckbxHighlightndLanguages.setOpaque(false);
		chckbxHighlightndLanguages.setCursor(GuiUtilsExtended.CURSOR_HAND);
		chckbxHighlightndLanguages.setSelected(false);
		chckbxHighlightndLanguages.setFont(new Font("SansSerif", Font.PLAIN, 13));
		mainPanel.add(chckbxHighlightndLanguages, "cell 9 7 4 1,alignx left");
		
		chckbxEmulatedTabsWith = new JCheckBox("Emulated tabs with spaces");
		chckbxEmulatedTabsWith.setOpaque(false);
		chckbxEmulatedTabsWith.setCursor(GuiUtilsExtended.CURSOR_HAND);
		chckbxEmulatedTabsWith.setSelected(false);
		chckbxEmulatedTabsWith.setFont(new Font("SansSerif", Font.PLAIN, 13));
		mainPanel.add(chckbxEmulatedTabsWith, "cell 0 8 4 1");
		
		chckbxMarkOccurrencesPaintBorder = new JCheckBox("Paint Occurrences Border");
		chckbxMarkOccurrencesPaintBorder.setOpaque(false);
		chckbxMarkOccurrencesPaintBorder.setCursor(GuiUtilsExtended.CURSOR_HAND);
		chckbxMarkOccurrencesPaintBorder.setSelected(false);
		chckbxMarkOccurrencesPaintBorder.setFont(new Font("SansSerif", Font.PLAIN, 13));
		mainPanel.add(chckbxMarkOccurrencesPaintBorder, "cell 4 8 2 1");
		
		checkBoxMarkOccurrences = new JCheckBox("Mark Occurrences");
		checkBoxMarkOccurrences.setOpaque(false);
		checkBoxMarkOccurrences.setCursor(GuiUtilsExtended.CURSOR_HAND);
		checkBoxMarkOccurrences.setSelected(false);
		checkBoxMarkOccurrences.setFont(new Font("SansSerif", Font.PLAIN, 13));
		mainPanel.add(checkBoxMarkOccurrences, "cell 6 8");
		
		chckbxRoundSelectionEdges = new JCheckBox("Round Selection Edges");
		chckbxRoundSelectionEdges.setOpaque(false);
		chckbxRoundSelectionEdges.setCursor(GuiUtilsExtended.CURSOR_HAND);
		chckbxRoundSelectionEdges.setSelected(false);
		chckbxRoundSelectionEdges.setFont(new Font("SansSerif", Font.PLAIN, 13));
		mainPanel.add(chckbxRoundSelectionEdges, "cell 9 8 4 1");
		
		chckbxMarkEOL = new JCheckBox("Mark end of line");
		chckbxMarkEOL.setOpaque(false);
		chckbxMarkEOL.setCursor(GuiUtilsExtended.CURSOR_HAND);
		chckbxMarkEOL.setSelected(false);
		chckbxMarkEOL.setFont(new Font("SansSerif", Font.PLAIN, 13));
		mainPanel.add(chckbxMarkEOL, "cell 0 9 4 1");
		
		chckbxHyperlinksEnabled = new JCheckBox("Hyperlinks Enabled");
		chckbxHyperlinksEnabled.setOpaque(false);
		chckbxHyperlinksEnabled.setCursor(GuiUtilsExtended.CURSOR_HAND);
		chckbxHyperlinksEnabled.setSelected(false);
		chckbxHyperlinksEnabled.setFont(new Font("SansSerif", Font.PLAIN, 13));
		mainPanel.add(chckbxHyperlinksEnabled, "cell 4 9 2 1");
		
		chckbxShowBracket = new JCheckBox("Show bracket popup");
		chckbxShowBracket.setOpaque(false);
		chckbxShowBracket.setCursor(GuiUtilsExtended.CURSOR_HAND);
		chckbxShowBracket.setSelected(false);
		chckbxShowBracket.setFont(new Font("SansSerif", Font.PLAIN, 13));
		mainPanel.add(chckbxShowBracket, "cell 6 9");
		
		chckbxSyncClipboardStyle = new JCheckBox("Sync clipboard style");
		chckbxSyncClipboardStyle.setOpaque(false);
		chckbxSyncClipboardStyle.setCursor(GuiUtilsExtended.CURSOR_HAND);
		chckbxSyncClipboardStyle.setSelected(false);
		chckbxSyncClipboardStyle.setFont(new Font("SansSerif", Font.BOLD, 13));
		mainPanel.add(chckbxSyncClipboardStyle, "cell 9 9 4 1");
		
		chckbxShowWhitespaces = new JCheckBox("Show whitespaces and empty lines");
		chckbxShowWhitespaces.setOpaque(false);
		chckbxShowWhitespaces.setCursor(GuiUtilsExtended.CURSOR_HAND);
		chckbxShowWhitespaces.setSelected(false);
		chckbxShowWhitespaces.setFont(new Font("SansSerif", Font.PLAIN, 13));
		mainPanel.add(chckbxShowWhitespaces, "cell 0 10 4 1");
		
		chckbxClearWhitespaceLines = new JCheckBox("Clear whitespace lines");
		chckbxClearWhitespaceLines.setOpaque(false);
		chckbxClearWhitespaceLines.setCursor(GuiUtilsExtended.CURSOR_HAND);
		chckbxClearWhitespaceLines.setSelected(false);
		chckbxClearWhitespaceLines.setFont(new Font("SansSerif", Font.PLAIN, 13));
		mainPanel.add(chckbxClearWhitespaceLines, "cell 4 10 2 1,alignx left");
		
		chckbxAutoCheckFiles = new JCheckBox("Auto check for file changes");
		chckbxAutoCheckFiles.setOpaque(false);
		chckbxAutoCheckFiles.setSelected(false);
		chckbxAutoCheckFiles.setFont(new Font("SansSerif", Font.BOLD, 13));
		mainPanel.add(chckbxAutoCheckFiles, "cell 6 10 3 1");
		
		separator_5 = new JSeparator();
		separator_5.setOpaque(true);
		separator_5.setBackground(Color.GRAY);
		mainPanel.add(separator_5, "cell 0 11 13 1,growx");
		
		JSeparator separator_1 = new JSeparator();
		separator_1.setBackground(Color.GRAY);
		separator_1.setOpaque(true);
		mainPanel.add(separator_1, "cell 0 13 13 1,growx,aligny center");

		btnResetValues = new JButton("Reset initial values");
		btnResetValues.setMinimumSize(new Dimension(30, 35));
		btnResetValues.setCursor(GuiUtilsExtended.CURSOR_HAND);
		btnResetValues.setFont(new Font("SansSerif", Font.BOLD, 12));
		btnResetValues.setBackground(new Color(240, 230, 140));
		mainPanel.add(btnResetValues, "cell 0 14 5 1,growx,aligny center");
								
		btnSaveConf = new JButton("Save Values");
		btnSaveConf.setMinimumSize(new Dimension(30, 35));
		btnSaveConf.setCursor(GuiUtilsExtended.CURSOR_HAND);
		btnSaveConf.setForeground(new Color(0, 0, 0));
		btnSaveConf.setFont(new Font("SansSerif", Font.BOLD, 12));
		btnSaveConf.setBackground(new Color(169, 169, 169));
		mainPanel.add(btnSaveConf, "cell 7 14 3 1,growx,aligny center");

		btnUseSettings = new JButton("Use");
		btnUseSettings.setMinimumSize(new Dimension(30, 35));
		btnUseSettings.setCursor(GuiUtilsExtended.CURSOR_HAND);
		btnUseSettings.setForeground(Color.BLACK);
		btnUseSettings.setFont(new Font("SansSerif", Font.BOLD, 12));
		btnUseSettings.setBackground(new Color(169, 169, 169));
		mainPanel.add(btnUseSettings, "cell 10 14,growx,aligny center");

		btnDiscard = new JButton("Discard");
		btnDiscard.setMinimumSize(new Dimension(30, 35));
		btnDiscard.setCursor(GuiUtilsExtended.CURSOR_HAND);
		btnDiscard.setForeground(new Color(0, 0, 0));
		btnDiscard.setFont(new Font("SansSerif", Font.BOLD, 12));
		btnDiscard.setBackground(new Color(205, 92, 92));
		mainPanel.add(btnDiscard, "cell 11 14 2 1,growx,aligny center");

		addHandlers();
		
		GuiUtilsExtended.launchThreadSafeSwing(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(300);
				} catch (InterruptedException e) {
				}
				thisFrame.toFront();
				thisFrame.requestFocus();
			}
		});
		
		styleBasedThemesSelectorToggle((LookAndFeelInfoCustom) comboStyleLaF.getSelectedItem());
		styleBasedDarkNimbusToggle((LookAndFeelInfoCustom) comboStyleLaF.getSelectedItem());
		styleBasedSizeNimbusToggle((LookAndFeelInfoCustom) comboStyleLaF.getSelectedItem());
		
		initGuiFromConfig();
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				MainGUI.updateGuiFromCurrConfig();
			}
		});
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
							
							singletonInstance = null;
						}
						
						SwingUtilities.invokeLater(new Runnable() {
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
		
		// END TXT AREA DETAIL //

		// COMBO STYLE HANDLER
		comboStyleLaF.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(styleChangedRunnable());
			}
		});

		comboTheme.addActionListener(new ActionListener() {
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
					dialogHelper.warn( "Ok, I wont save theese settings ;)", "Not saved!");
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
					public void run() {
						MainGUI.updateGuiFromCurrConfig();
					}
				});
			}
		});

		sliderLogSize.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				spinnerLogSize.setValue((int) sliderLogSize.getValue());
			}
		});

		sliderMenuSize.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				spinnerMenuSize.setValue((int) sliderMenuSize.getValue());
			}
		});
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
			ParentPanel rightPane = currentEditor.textEditorUtils.getTypedIRightPanel();
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
				PlasticTheme currentTheme = ((PlasticTheme) comboTheme.getItemAt(i));
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
					comboTheme.setFont(GeneralConfig.BUTTON_DEFAULT_FONT);
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
	public Dimension getDefaultDimension() {
		return new Dimension(960, 530);
	}
	
	public static GraphicPreferencesFrame getInstance(INItializer currentConf) {
		return singletonInstance == null ? new GraphicPreferencesFrame(currentConf) : singletonInstance;
	}
	
}

class ThemeCellRenderer implements ListCellRenderer<Object> {
	protected DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();
	
	static Logger logger = Logger.getLogger(ThemeCellRenderer.class);

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

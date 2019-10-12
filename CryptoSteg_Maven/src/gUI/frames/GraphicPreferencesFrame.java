package gUI.frames;

import javax.swing.JFrame;
import net.miginfocom.swing.MigLayout;
import om.GuiOption;
import om.TextEditorOption;
import resources.IconsPathConfigurator;
import tabbededitor.CustomTabbedEditor;
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
import javax.swing.JScrollPane;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;

import dialogutils.JOptionHelper;
import frameutils.utils.GuiUtils;
import gUI.panels.TextEditor;
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
import javax.swing.JCheckBox;
import java.awt.Dimension;
import java.awt.ComponentOrientation;

public class GraphicPreferencesFrame extends JFrame {

	// initialize Logger
	// GraphicPreferencesFrame logger
	static Logger logger = Logger.getLogger(GraphicPreferencesFrame.class);

	// GUI elements
	private final JFrame thisFrame = this;
	private JSlider sliderMenuSize;
	private JSlider sliderLogSize;
	private JComboBox comboStyleLaF;
	private JCheckBox chckbxDarklight;
	private JCheckBox chckBoxAntiAliasingON;

	private JButton btnDiscard;
	private JButton btnUseSettings;
	private JButton btnSaveConf;
	private JButton btnResetValues;
	private JLabel lblNimbusSize;
	private JSpinner spinnerMenuSize;
	private JSpinner spinnerLogSize;
	private JSpinner spinnerNimbusSize;
	
	// default values
	private static String BACKUP_PREFERRED_STYLE;
	private static int BACKUP_MENU_FONT_SIZE;
	private static int BACKUP_LOG_PANEL_FONT_SIZE;
	private static boolean BACKUP_DARK_THEME;
	private static int BACKUP_NIMBUS_SIZE;
	// DETAIL
	private static boolean BACKUP_TXTAREA_ANTIALIASED;
	private static boolean BACKUP_TXTAREA_AUTO_INDENT;
	private static boolean BACKUP_TXTAREA_ANIMATE_BRACKETS;
	private static boolean BACKUP_TXTAREA_BRACKET_MATCHING;
	private static boolean BACKUP_TXTAREA_CLEAR_WHITESP_LINES;
	private static boolean BACKUP_TXTAREA_CLOSE_CURLY_BRACE;
	private static boolean BACKUP_TXTAREA_CLOSE_MARKUP_TAG;
	private static boolean BACKUP_TXTAREA_CODE_FOLDING;
	private static boolean BACKUP_TXTAREA_CURR_LINE_HIGHLIGHT;
	private static boolean BACKUP_TXTAREA_FRACT_FONT_METRICS;
	private static boolean BACKUP_TXTAREA_HIGLIGHT_2ND_LANG;
	private static boolean BACKUP_TXTAREA_HYPERLINKS;
	private static boolean BACKUP_TXTAREA_MARK_OCCURRENCES;
	private static boolean BACKUP_TXTAREA_PAINT_MARK_OCCURRENCES;
	private static boolean BACKUP_TXTAREA_ROUND_SEL_EDGES;
	private static boolean BACKUP_TXTAREA_FOCUSABLE_TIPS;
	private static boolean BACKUP_TXTAREA_TABSPACES_EMULATED;
	private static boolean BACKUP_TXTAREA_SHOW_BRACKETS_POPUP;
	private static boolean BACKUP_TXTAREA_SHOW_EOL_DELIM;
	private static boolean BACKUP_TXTAREA_AUTOCHECK_CHANGES;

	// flag to signal active
	public static boolean active = false;

	private INItializer currentConfiguration;
	
	private TextEditor currentEditor;
	private CustomTabbedEditor currentTabEditor;
	
	private JPanel mainPanel;
	private JScrollPane mainPanelContainer;
	
	private JCheckBox chckbxAutoindentation;
	private JCheckBox chckbxAnimateBrackets;
	private JCheckBox chckbxBracketsMatching;
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
	
	JOptionHelper dialogHelper;
	private JCheckBox chckbxMarkEOL;
	private JCheckBox chckbxShowBracket;
	private JCheckBox chckbxAutoCheckFiles;
	private JLabel lblTextAreaOptions;
	private JLabel lblGenericGraphicalOptions;
	private JSeparator separator_2;
	private JSeparator separator_3;
	private JSeparator separator_4;
	
	public GraphicPreferencesFrame(INItializer currentConf) {
		
		currentEditor = MainGUI.initTxtWriter;
		currentTabEditor = MainGUI.initTxtWriter.getCurrentTabEditor();
		
		logger.info(this.getClass().getName() + " Starting...");

		currentConfiguration = currentConf;
		active = true;
		
		dialogHelper = new JOptionHelper(this);
		
		this.setAlwaysOnTop(true);
		this.setResizable(false);

		GuiUtils.setFrameIcon(IconsPathConfigurator.ICON_GUI_OPT, this);
		setLocationRelativeTo(MainGUI.frmScryptV);
		
		updateBackup(currentConfiguration);

		logger.debug("Backup Initialized.");

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		mainPanel = new JPanel();
		mainPanel.setBackground(new Color(0, 0, 0));
		mainPanel.setBackground(Color.LIGHT_GRAY);
		mainPanel.setLayout(new MigLayout("", "[:130px:140px][40px,grow][40,grow][10,grow][90][10,grow][100px:100px][:40px:60px,grow][:75px:75px][75px,grow]", "[:50:70px][25][10][30][15][30][30][30][30][30][:5px:10px,grow][15px,grow,baseline]"));
		mainPanelContainer = new JScrollPane(mainPanel);
		getContentPane().add(mainPanelContainer);

		JPanel panel = new JPanel();
		panel.setForeground(SystemColor.controlText);
		panel.setBorder(new LineBorder(null, 4));
		panel.setBackground(Color.DARK_GRAY);
		mainPanel.add(panel, "cell 0 0 10 1,grow");
		panel.setLayout(new MigLayout("", "[100px][][70px][grow][][][][]", "[100]"));
		
		chckbxDarklight = new JCheckBox("Nimbus Dark theme");
		chckbxDarklight.setForeground(Color.WHITE);
		panel.add(chckbxDarklight, "cell 0 0 2 1");
		chckbxDarklight.setCursor(GuiUtils.CURSOR_HAND);
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
		panel.add(spinnerNimbusSize, "cell 7 0,alignx right");
		spinnerNimbusSize.setCursor(GuiUtils.CURSOR_HAND);
		spinnerNimbusSize.setModel(new SpinnerNumberModel(0, 0, 2, 1));
		spinnerNimbusSize.setValue(currentConf.getGuiOpt().nimbusSize);
						
		separator_4 = new JSeparator();
		separator_4.setOpaque(true);
		separator_4.setForeground(Color.DARK_GRAY);
		separator_4.setBackground(Color.GRAY);
		mainPanel.add(separator_4, "cell 0 1 3 1,growx");
		
		lblGenericGraphicalOptions = new JLabel("Generic Graphical Options");
		lblGenericGraphicalOptions.setForeground(Color.GRAY);
		lblGenericGraphicalOptions.setFont(new Font("Tahoma", Font.BOLD, 13));
		mainPanel.add(lblGenericGraphicalOptions, "cell 3 1 3 1");
		
		separator_2 = new JSeparator();
		separator_2.setOpaque(true);
		separator_2.setForeground(Color.DARK_GRAY);
		separator_2.setBackground(Color.GRAY);
		mainPanel.add(separator_2, "cell 6 1 4 1,growx");

		JLabel lblGuiType = new JLabel("Select GUI type");
		lblGuiType.setFont(new Font("Tahoma", Font.BOLD, 13));
		mainPanel.add(lblGuiType, "cell 0 2,alignx right");

		comboStyleLaF = new JComboBox(GUIStyelSettings.getLookAndFeelList());
		comboStyleLaF.setPreferredSize(new Dimension(620, 26));
		comboStyleLaF.setMinimumSize(new Dimension(600, 26));
		comboStyleLaF.setCursor(GuiUtils.CURSOR_HAND);
		comboStyleLaF.setMaximumRowCount(10);
		comboStyleLaF.setMaximumSize(new Dimension(2000, 50));
		mainPanel.add(comboStyleLaF, "cell 1 2 9 1,grow");

		JLabel lblMenuFontSize = new JLabel("Menu font size");
		lblMenuFontSize.setFont(new Font("Tahoma", Font.BOLD, 13));
		mainPanel.add(lblMenuFontSize, "cell 0 3,alignx right");

		spinnerMenuSize = new JSpinner();
		spinnerMenuSize.setCursor(GuiUtils.CURSOR_HAND);
		spinnerMenuSize.setModel(new SpinnerNumberModel(12, 5, 24, 1));
		mainPanel.add(spinnerMenuSize, "cell 1 3,growx");

		sliderMenuSize = new JSlider();
		sliderMenuSize.setCursor(GuiUtils.CURSOR_HAND);
		sliderMenuSize.setMaximum(24);
		sliderMenuSize.setMinimum(5);
		mainPanel.add(sliderMenuSize, "cell 2 3 4 1,growx");
		
		JLabel lblFontSize = new JLabel("Log Font size");
		lblFontSize.setFont(new Font("Tahoma", Font.BOLD, 13));
		mainPanel.add(lblFontSize, "cell 6 3,alignx right");

		spinnerLogSize = new JSpinner();
		spinnerLogSize.setCursor(GuiUtils.CURSOR_HAND);
		spinnerLogSize.setModel(new SpinnerNumberModel(12, 5, 30, 1));
		mainPanel.add(spinnerLogSize, "cell 7 3,growx");
		
		sliderLogSize = new JSlider();
		sliderLogSize.setCursor(GuiUtils.CURSOR_HAND);
		sliderLogSize.setMaximum(30);
		sliderLogSize.setMinimum(5);
		mainPanel.add(sliderLogSize, "cell 8 3 2 1,growx");
		
		separator_3 = new JSeparator();
		separator_3.setOpaque(true);
		separator_3.setForeground(Color.DARK_GRAY);
		separator_3.setBackground(Color.GRAY);
		mainPanel.add(separator_3, "cell 0 4 4 1,growx");
		
		lblTextAreaOptions = new JLabel("Text Area Options");
		lblTextAreaOptions.setForeground(Color.GRAY);
		lblTextAreaOptions.setFont(new Font("Tahoma", Font.BOLD, 13));
		mainPanel.add(lblTextAreaOptions, "cell 4 4,alignx center");

		JSeparator separator = new JSeparator();
		separator.setOpaque(true);
		separator.setForeground(Color.DARK_GRAY);
		separator.setBackground(Color.GRAY);
		mainPanel.add(separator, "cell 5 4 5 1,growx");
		
		chckBoxAntiAliasingON = new JCheckBox("Antialiasing enabled");
		chckBoxAntiAliasingON.setCursor(GuiUtils.CURSOR_HAND);
		chckBoxAntiAliasingON.setFont(new Font("SansSerif", Font.PLAIN, 13));
		mainPanel.add(chckBoxAntiAliasingON, "cell 0 5 2 1,alignx left");
		
		chckbxAutoindentation = new JCheckBox("Auto-Indentation");
		chckbxAutoindentation.setCursor(GuiUtils.CURSOR_HAND);
		chckbxAutoindentation.setSelected(false);
		chckbxAutoindentation.setFont(new Font("SansSerif", Font.PLAIN, 13));
		mainPanel.add(chckbxAutoindentation, "cell 2 5 4 1,alignx left");
		
		chckbxAnimateBrackets = new JCheckBox("Animate Brackets");
		chckbxAnimateBrackets.setCursor(GuiUtils.CURSOR_HAND);
		chckbxAnimateBrackets.setSelected(false);
		chckbxAnimateBrackets.setFont(new Font("SansSerif", Font.PLAIN, 13));
		mainPanel.add(chckbxAnimateBrackets, "cell 6 5 2 1,alignx left");
		
		chckbxBracketsMatching = new JCheckBox("Brackets Matching");
		chckbxBracketsMatching.setCursor(GuiUtils.CURSOR_HAND);
		chckbxBracketsMatching.setSelected(false);
		chckbxBracketsMatching.setFont(new Font("SansSerif", Font.PLAIN, 13));
		mainPanel.add(chckbxBracketsMatching, "cell 8 5 2 1,alignx left");
		
		chckbxClearWhitespaceLines = new JCheckBox("Clear whitespace lines");
		chckbxClearWhitespaceLines.setCursor(GuiUtils.CURSOR_HAND);
		chckbxClearWhitespaceLines.setSelected(false);
		chckbxClearWhitespaceLines.setFont(new Font("SansSerif", Font.PLAIN, 13));
		mainPanel.add(chckbxClearWhitespaceLines, "cell 0 6 2 1,alignx left");
		
		chckbxCloseCurlybraces = new JCheckBox("Close Curly-Braces");
		chckbxCloseCurlybraces.setCursor(GuiUtils.CURSOR_HAND);
		chckbxCloseCurlybraces.setSelected(false);
		chckbxCloseCurlybraces.setFont(new Font("SansSerif", Font.PLAIN, 13));
		mainPanel.add(chckbxCloseCurlybraces, "cell 2 6 4 1,alignx left");
		
		chckbxCloseMarkuptags = new JCheckBox("Close Markup-Tags");
		chckbxCloseMarkuptags.setCursor(GuiUtils.CURSOR_HAND);
		chckbxCloseMarkuptags.setSelected(false);
		chckbxCloseMarkuptags.setFont(new Font("SansSerif", Font.PLAIN, 13));
		mainPanel.add(chckbxCloseMarkuptags, "cell 6 6 2 1,alignx left");
		
		chckbxFocusableTips = new JCheckBox("Focusable Tips:");
		chckbxFocusableTips.setCursor(GuiUtils.CURSOR_HAND);
		chckbxFocusableTips.setSelected(false);
		chckbxFocusableTips.setFont(new Font("SansSerif", Font.PLAIN, 13));
		mainPanel.add(chckbxFocusableTips, "cell 8 6 2 1,alignx left");
		
		chckbxCodeFolding = new JCheckBox("Code Folding");
		chckbxCodeFolding.setCursor(GuiUtils.CURSOR_HAND);
		chckbxCodeFolding.setSelected(false);
		chckbxCodeFolding.setFont(new Font("SansSerif", Font.PLAIN, 13));
		mainPanel.add(chckbxCodeFolding, "cell 0 7 2 1,alignx left");
		
		chckbxAllowFractionalFonts = new JCheckBox("Allow Fractional Fonts");
		chckbxAllowFractionalFonts.setCursor(GuiUtils.CURSOR_HAND);
		chckbxAllowFractionalFonts.setSelected(false);
		chckbxAllowFractionalFonts.setFont(new Font("SansSerif", Font.PLAIN, 13));
		mainPanel.add(chckbxAllowFractionalFonts, "cell 2 7 4 1,alignx left");
		
		chckbxFadeHighlight = new JCheckBox("Fade Highlight");
		chckbxFadeHighlight.setCursor(GuiUtils.CURSOR_HAND);
		chckbxFadeHighlight.setSelected(false);
		chckbxFadeHighlight.setFont(new Font("SansSerif", Font.PLAIN, 13));
		mainPanel.add(chckbxFadeHighlight, "cell 6 7 2 1,alignx left");
		
		chckbxHighlightndLanguages = new JCheckBox("Highlight 2nd Languages");
		chckbxHighlightndLanguages.setCursor(GuiUtils.CURSOR_HAND);
		chckbxHighlightndLanguages.setSelected(false);
		chckbxHighlightndLanguages.setFont(new Font("SansSerif", Font.PLAIN, 13));
		mainPanel.add(chckbxHighlightndLanguages, "cell 8 7 2 1,alignx left");
		
		chckbxHyperlinksEnabled = new JCheckBox("Hyperlinks Enabled");
		chckbxHyperlinksEnabled.setCursor(GuiUtils.CURSOR_HAND);
		chckbxHyperlinksEnabled.setSelected(false);
		chckbxHyperlinksEnabled.setFont(new Font("SansSerif", Font.PLAIN, 13));
		mainPanel.add(chckbxHyperlinksEnabled, "cell 0 8 2 1");
		
		chckbxMarkOccurrencesPaintBorder = new JCheckBox("Paint Occurrences Border");
		chckbxMarkOccurrencesPaintBorder.setCursor(GuiUtils.CURSOR_HAND);
		chckbxMarkOccurrencesPaintBorder.setSelected(false);
		chckbxMarkOccurrencesPaintBorder.setFont(new Font("SansSerif", Font.PLAIN, 13));
		mainPanel.add(chckbxMarkOccurrencesPaintBorder, "cell 2 8 4 1");
		
		checkBoxMarkOccurrences = new JCheckBox("Mark Occurrences");
		checkBoxMarkOccurrences.setCursor(GuiUtils.CURSOR_HAND);
		checkBoxMarkOccurrences.setSelected(false);
		checkBoxMarkOccurrences.setFont(new Font("SansSerif", Font.PLAIN, 13));
		mainPanel.add(checkBoxMarkOccurrences, "cell 6 8 2 1");
		
		chckbxRoundSelectionEdges = new JCheckBox("Round Selection Edges");
		chckbxRoundSelectionEdges.setCursor(GuiUtils.CURSOR_HAND);
		chckbxRoundSelectionEdges.setSelected(false);
		chckbxRoundSelectionEdges.setFont(new Font("SansSerif", Font.PLAIN, 13));
		mainPanel.add(chckbxRoundSelectionEdges, "cell 8 8 2 1");
		
		chckbxMarkEOL = new JCheckBox("Mark end of line");
		chckbxMarkEOL.setCursor(GuiUtils.CURSOR_HAND);
		chckbxMarkEOL.setSelected(false);
		chckbxMarkEOL.setFont(new Font("SansSerif", Font.PLAIN, 13));
		mainPanel.add(chckbxMarkEOL, "cell 0 9");
		
		chckbxEmulatedTabsWith = new JCheckBox("Emulated tabs with spaces");
		chckbxEmulatedTabsWith.setCursor(GuiUtils.CURSOR_HAND);
		chckbxEmulatedTabsWith.setSelected(false);
		chckbxEmulatedTabsWith.setFont(new Font("SansSerif", Font.PLAIN, 13));
		mainPanel.add(chckbxEmulatedTabsWith, "cell 2 9 4 1");
		
		chckbxShowBracket = new JCheckBox("Show bracket popup");
		chckbxShowBracket.setCursor(GuiUtils.CURSOR_HAND);
		chckbxShowBracket.setSelected(false);
		chckbxShowBracket.setFont(new Font("SansSerif", Font.PLAIN, 13));
		mainPanel.add(chckbxShowBracket, "cell 6 9 2 1");
		
		chckbxAutoCheckFiles = new JCheckBox("Auto check for file changes");
		chckbxAutoCheckFiles.setSelected(false);
		chckbxAutoCheckFiles.setFont(new Font("SansSerif", Font.BOLD, 13));
		mainPanel.add(chckbxAutoCheckFiles, "cell 8 9 2 1");
						
		JSeparator separator_1 = new JSeparator();
		separator_1.setBackground(Color.GRAY);
		separator_1.setOpaque(true);
		mainPanel.add(separator_1, "cell 0 10 10 1,growx,aligny center");

		btnResetValues = new JButton("Reset initial values");
		btnResetValues.setCursor(GuiUtils.CURSOR_HAND);
		btnResetValues.setFont(new Font("SansSerif", Font.BOLD, 12));
		btnResetValues.setBackground(new Color(240, 230, 140));
		mainPanel.add(btnResetValues, "cell 0 11 2 1,growx,aligny center");
		
		btnSaveConf = new JButton("Save Values");
		btnSaveConf.setCursor(GuiUtils.CURSOR_HAND);
		btnSaveConf.setForeground(new Color(0, 0, 0));
		btnSaveConf.setFont(new Font("SansSerif", Font.BOLD, 12));
		btnSaveConf.setBackground(new Color(169, 169, 169));
		mainPanel.add(btnSaveConf, "cell 6 11 2 1,growx,aligny center");

		btnUseSettings = new JButton("Use");
		btnUseSettings.setCursor(GuiUtils.CURSOR_HAND);
		btnUseSettings.setForeground(Color.WHITE);
		btnUseSettings.setFont(new Font("SansSerif", Font.BOLD, 12));
		btnUseSettings.setBackground(Color.DARK_GRAY);
		mainPanel.add(btnUseSettings, "cell 8 11,growx");

		btnDiscard = new JButton("Discard");
		btnDiscard.setCursor(GuiUtils.CURSOR_HAND);
		btnDiscard.setForeground(new Color(0, 0, 0));
		btnDiscard.setFont(new Font("SansSerif", Font.BOLD, 12));
		btnDiscard.setBackground(new Color(205, 92, 92));
		mainPanel.add(btnDiscard, "cell 9 11,growx");

		// default close operation(same as reset)
		addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {

				logger.debug("Closing GUI_Preferences");

				// reset INItializer reference currentConf too
				resetConfigValues(currentConf);
				
				antialiasingApply(currentConfiguration.getTextEditorOpt().antialiasedTxtArea);
				currentEditor.initGuiFromConfig(true);

				logger.debug(
						"Closing " + getClass().getName() + " - Configuration resetted: \n" + currentConf.toStringa());

				active = false;

				dispose();
			}
		});

		initGuiFromConfig();
		
		addHandlers();
		
		MainGUI.initTxtWriter.getCurrentTabEditor().applyStyleToTabs();
		
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
					boolean res = !GUIStyelSettings.setLightNimbus(currentConfiguration.getGuiOpt().nimbusSize);
					currentConfiguration.getGuiOpt().setDarkGuiNimbus(res);
				} else {
					boolean res = GUIStyelSettings.setDarkNimbus(currentConfiguration.getGuiOpt().nimbusSize);
					currentConfiguration.getGuiOpt().setDarkGuiNimbus(res);
				}
				SwingUtilities.invokeLater(styleChangedRunnable());
//				if (MainGUI.initTxtWriter != null) {
//					MainGUI.initTxtWriter.getCurrentTabEditor().applyStyleToTabs();
//				}
//				currentEditor.initGuiFromConfig(true);
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
		
		// END TXT AREA DETAIL //

		// COMBO STYLE HANDLER
		comboStyleLaF.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(styleChangedRunnable());
			}
		});

		// RESET BUTTON HANDLER
		btnResetValues.addActionListener(new ActionListener() {

			// action fired when user reset initial values saved in backup values
			@Override
			public void actionPerformed(ActionEvent e) {

				logger.debug("Starting reset of GUI_Preferences");

				// reset INItializer reference currentConf too
				resetConfigValues(currentConfiguration);

				// reset gui (sliders changement will trigger jtextfield, see action listener)
				updateGUI(true);
				
//				GUIStyelSettings.updateAllWindowsLaf();
				currentTabEditor.applyStyleToTabs();

				logger.debug("Configuration resetted: \n" + currentConfiguration.toStringa());
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
					updateBackup(currentConfiguration);

					updateGUI(true);
					
					dialogHelper.info( "Graphic Interface settings Saved :)\n -> Changes will be visible after next restart.", "Saved!");
					currentEditor.textEditorUtils.resetDividerColor();
					currentTabEditor.applyStyleToTabs();
					logger.debug("Configuration saved: \n" + currentConfiguration.toStringa());

					// dispose();
					dispatchEvent(new WindowEvent(thisFrame, WindowEvent.WINDOW_CLOSING));
					

				} else {
					dialogHelper.warn( "Ok, I wont save theese settings ;)", "Not saved!");
					currentEditor.textEditorUtils.resetDividerColor();
					currentTabEditor.applyStyleToTabs();
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
				GUIStyelSettings.applyNimbusSize((int) spinnerNimbusSize.getValue());
				currentEditor.initGuiFromConfig(true);
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
		sliderLogSize.setValue(guiOpt.getLogFontSize());
		updateComboLaFGUI(guiOpt.getPreferredStyle());
		chckbxDarklight.setSelected(guiOpt.isDarkGuiNimbus());
		
		// TEXT AREA
		chckBoxAntiAliasingON.setSelected(txtOpt.antialiasedTxtArea);
		chckbxAllowFractionalFonts.setSelected(txtOpt.fractionalFontMetricsEnabled);
		chckbxAnimateBrackets.setSelected(txtOpt.animateBracketMatching);
		chckbxAutoindentation.setSelected(txtOpt.autoIndentEnabled);
		chckbxBracketsMatching.setSelected(txtOpt.bracketMatchingEnabled);
		chckbxClearWhitespaceLines.setSelected(txtOpt.clearWhitespaceLinesEnabled);
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
		
		// REFRESH GUI GENERAL
		if (guiOpt.isDarkGuiNimbus()) {
			GUIStyelSettings.setDarkNimbus(guiOpt.nimbusSize);
		}else {
			GUIStyelSettings.setLightNimbus(guiOpt.nimbusSize);
		}
		GUIStyelSettings.setStyle(guiOpt.getPreferredStyle(), guiOpt.nimbusSize);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (currentTabEditor.loadedTabObj != null) {
					currentTabEditor.loadedTabObj.textArea.setFont(currentConfiguration.getTextEditorOpt().getFont());
					RsyntaxUtils.setFont(currentTabEditor.loadedTabObj.textArea, currentConfiguration.getTextEditorOpt().getFont());
				}
			}
		});
		
		antialiasingApply(currentConfiguration.getTextEditorOpt().antialiasedTxtArea);
		currentEditor.initGuiFromConfig(forceGuiRefresh);
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
		BACKUP_DARK_THEME = guiOpt.isDarkGuiNimbus();
		BACKUP_NIMBUS_SIZE = guiOpt.getNimbusSize();
		
		// detail backup
		BACKUP_TXTAREA_ANTIALIASED = txtOpt.isAntialiasedTxtArea();
		BACKUP_TXTAREA_ANIMATE_BRACKETS = txtOpt.animateBracketMatching;
		BACKUP_TXTAREA_AUTO_INDENT = txtOpt.autoIndentEnabled;
		BACKUP_TXTAREA_BRACKET_MATCHING = txtOpt.bracketMatchingEnabled;
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
		guiOpt.setMenuFontSize(BACKUP_MENU_FONT_SIZE);
		guiOpt.setLogFontSize(BACKUP_LOG_PANEL_FONT_SIZE);
		guiOpt.setDarkGuiNimbus(BACKUP_DARK_THEME);
		guiOpt.setNimbusSize(BACKUP_NIMBUS_SIZE);
		
		txtOpt.setAntialiasedTxtArea(BACKUP_TXTAREA_ANTIALIASED);
		txtOpt.setAnimateBracketMatching(BACKUP_TXTAREA_ANIMATE_BRACKETS);
		txtOpt.setAutoIndentEnabled(BACKUP_TXTAREA_AUTO_INDENT);
		txtOpt.setBracketMatchingEnabled(BACKUP_TXTAREA_BRACKET_MATCHING);
		txtOpt.setClearWhitespaceLinesEnabled(BACKUP_TXTAREA_CLEAR_WHITESP_LINES);
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
		currentConf.getGenOpt().setTxtEditorFileMonitorActive(BACKUP_TXTAREA_AUTOCHECK_CHANGES);
	}

	
	private void updateMenuAndLogFontSize(JFrame jframe) {
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

	public void initGuiFromConfig() {
		updateBackup(currentConfiguration);
		updateGUI(false);
	}
	
	private void antialiasingApply(boolean antialiasing) {
		try {
			currentTabEditor.loadedTabObj.textArea.setAntiAliasingEnabled(antialiasing);
		} catch (Exception e1) {
			logger.warn("Error while preiewing antialising effect on textarea", e1);
		}
	}
	
	private Runnable styleChangedRunnable() {
		return new Runnable() {
			
			@Override
			public void run() {
				try {
					LookAndFeelInfo laf = (LookAndFeelInfo) comboStyleLaF.getSelectedItem();
					currentConfiguration.getGuiOpt().setPreferredStyle(laf.getClassName());
					GUIStyelSettings.setStyle(laf.getClassName(), currentConfiguration.getGuiOpt().nimbusSize);
				} catch (Exception e1) {
					GUIStyelSettings.setStyle(BACKUP_PREFERRED_STYLE, currentConfiguration.getGuiOpt().nimbusSize);
					currentConfiguration.getGuiOpt().setPreferredStyle(BACKUP_PREFERRED_STYLE);
					dialogHelper.error("Sorry, something went wrong :(", e1.getClass().getName());
					logger.error("Exception happened!", e1);
				} finally {
					GUIStyelSettings.updateAllWindowsLaf();
					currentEditor.initGuiFromConfig(true);
				}
			}
		};
	}
}

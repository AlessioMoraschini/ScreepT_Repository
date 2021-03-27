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
package screept.gui.panels.subpanels.texteditor;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;

import org.apache.log4j.Logger;
import org.fife.rsta.ui.GoToDialog;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import com.minhasKamal.ultimateCalculator.CalculatorMainApp;

import gui.commons.dialogutils.JOptionHelperExtended;
import gui.commons.frameutils.frame.EscapeReplacerFrame;
import gui.commons.frameutils.frame.FilePropertiesFrame;
import gui.commons.frameutils.utils.GuiUtilsExtended;
import gui.commons.textarea.om.TextLineNumber;
import gui.texteditor.commons.TextEditTooltips;
import gui.texteditor.commons.dialogs.FindDialogFork;
import gui.texteditor.commons.dialogs.ReplaceDialogFork;
import gui.texteditor.commons.om.SupportedFileTextEditor;
import gui.texteditor.commons.om.TabTextEditorObject;
import gui.texteditor.commons.om.themes.Themes;
import gui.texteditor.commons.om.themes.ThemesWrapper;
import gui.texteditor.commons.utils.RsyntaxUtils;
import gui.texteditor.commons.utils.dialog.ColorDialogUtils;
import gui.texteditor.commons.utils.panels.arch.PanelType;
import initializer.configs.impl.INItializer;
import initializer.configs.impl.om.TextEditorOption;
import maingUI.MainGUI;
import maingUI.MainGuiGeneralAppStarter;
import multimedia.conversion.Text2Speech;
import multimedia.conversion.gui.ImageSelector2TextFrame;
import multimedia.conversion.gui.PdfSelector2TextFrame;
import net.miginfocom.swing.MigLayout;
import os.commons.utils.SysUtils;
import resources.GeneralConfig;
import resources.IconsPathConfigurator;
import screept.gui.panels.TextEditor;
import screept.gui.utils.filedialog.TemplatesChooserFrame;
import screept.gui.utils.shortcutshandler.TextEditorShortcutListener;
import screept.gui.utils.texteditor.TextEditorUtils;
import various.common.light.files.FileVarious;
import various.common.light.files.FileWorker;
import various.common.light.gui.GuiUtils;
import various.common.light.gui.RXTextUtilities;
import various.common.light.gui.dialogs.font.JFontChooserExtended;
import various.common.light.gui.selection.AbstractSelector;
import various.common.light.utility.manipulation.EncodingUtils;
import various.common.light.utility.string.StringWorker;
import various.common.light.utility.string.StringWorker.EOL;

/**
 * This panel will contain all buttons to do actions into textEditor, in order to have small and separated module to use into GUI
 * @author Alessio Moraschini
 *
 */
public class TextEditorSubHeadButtonsPanel extends JPanel{
	private static final long serialVersionUID = 5481362170538925466L;
	
	// TextEditorSubHeadButtonsPanel logger creation
	static Logger logger = Logger.getLogger(TextEditorSubHeadButtonsPanel.class);
	
	// COLORS DEFAULT DEFINITIONS
	final Color DEFAULT_BCKG_COL;
	final Color DEFAULT_FORE_COL;
	
	// GENERAL FIELDS //
	public TextEditor parent;
	public TextEditorUtils textEditorUtils;
	public INItializer currentConfiguration;
	public MainGUI mainApplication;
	
	// SUB-HEADER COMPONENTS

	public JCheckBox chckbxShowTabGrid;
	public JCheckBox chckbxLockLanguage;
	
	public JLabel lblSIZE;
	public JLabel lblMargin;
	public JLabel lBLMargin;
	public JLabel lblFontSize;
	public JLabel labelTabSize;

	public JButton btnFindText;
	public JButton btnReplaceText;
	public JButton btnReplaceEscapesSeq;
	public JButton btnGoToLine;
	public JButton btntTextFormat;
	public JButton btnLoadTextFile;
	public JButton btnSaveTextFileAs;
	public JButton btnSaveTextFile;
	public JButton btnCloseAllTabs;
	public JButton btnReloadFromDisk;
	public JButton btnSpeak;
	public JButton btnOpenInScreepT;
	public JButton btnDark;
	public JButton btnBackgroundCol;
	public JButton btnParseImageText;
	public JButton btnParsePdfText;
	public JButton btnFontChooser;
	public JButton btnTemplates;
	private JButton btnNextHistory;
	private JButton btnPrevHistory;
//	public JButton btnDefaultOpt;
	public JButton btnUndo;
	public JButton btnClear;
	public JButton btnRedo;
	public JButton btnBold;
	public JButton btnQuickFileSearch;
	public JButton btnForegroundCol;
	public JButton btnNewTab;
	public JButton btnRemoveSpaces;
	public JButton btnViewInExplorer;
	public JButton btnPropertiesPanel;
	public JButton btnCalculator;
	public JButton btnColorSampler;
	private JButton btnThemeEditor;
	public JButton btnBase64Conversion;
	
	public JToggleButton btnToggleFileView;
	public JToggleButton toggleBtnMap;
	public JToggleButton toggleBtnClipboard;
	public JToggleButton toggleBtnConsole;
	public JToggleButton toggleButtonSpellCheck;

	public JSpinner tabSizeSpinner;

	public JComboBox<SupportedFileTextEditor> comboFileFormat;
	public JComboBox<ThemesWrapper> comboStylesScheme;
	public Vector<ThemesWrapper> themeList = new Vector<ThemesWrapper>();
	
	public JSlider sliderFontSize;
	public JSlider slider_margin;
	
	// DIALOGS AND JFRAMES
	public FindDialogFork searchBox;
	public ReplaceDialogFork replaceDialog;
	public GoToDialog goToLineDialog;
	public TemplatesChooserFrame templatesPanel;
	public EscapeReplacerFrame escapeReplacerPanel;
	public ImageSelector2TextFrame imageParserBox;
	public PdfSelector2TextFrame pdfParserBox;
	public TextLineNumber lineNumberPanel;
	public FilePropertiesFrame propertiesFrame;
	public CalculatorMainApp calculatorFrame;
	public Thread calculatorMainThread;
	
	public Text2Speech objSpeaker; // field containing the current object used to speak text

	public JOptionHelperExtended dialogHelper;
	
	TextEditorSubHeadButtonsPanel thisHeaderRef;
	private JButton btnFileContentSearch;
	
	/************************
	*	Main subHeader CONSTRUCTOR
	************************/
	public TextEditorSubHeadButtonsPanel(TextEditor parent) {
		logger.info(this.getClass().getName() + " Starting...");
		
		setBackground(Color.DARK_GRAY);
		
		thisHeaderRef = this;
		this.parent = parent;
		this.textEditorUtils = parent.textEditorUtils;
		this.currentConfiguration = parent.currentConfiguration;
		this.mainApplication = parent.mainApplication;
		dialogHelper = new JOptionHelperExtended(this.parent);
		
		DEFAULT_BCKG_COL = currentConfiguration.getTextEditorOpt().backCol;
		DEFAULT_FORE_COL = currentConfiguration.getTextEditorOpt().foreCol;
		
		setBorder(new EmptyBorder(0, 0, 0, 0));
		setLayout(new MigLayout(
				"insets 3, hidemode 3", 
				"[27px:40px:45px][27px:40px:45px][27px:40px:45px][27px:40px:45px][27px:40px:45px][27px:40px:45px][27px:40px:45px][27px:40px:45px][65px:n:70px][27px:n][50px:65px:100px][40px:40px:60px][27px:40px:45px][27px:40px:45px][27px:40px:45px][27px:40px:45px][27px:40px:45px][27px:40px:45px][27px:40px:45px][27px:40px:45px][27px:40px:45px][27px:40px:45px][27px:40px:45px][27px:40px:45px][27px:40px:45px][0px:5px:5px][60px][27px][][][][grow][]",
				"[:35px:35px,center][:50px:50px,center]"));
		
		btnCloseAllTabs = new JButton("");
		btnCloseAllTabs.setForeground(Color.BLACK);
		btnCloseAllTabs.setToolTipText(TextEditTooltips.tooltipRemoveAllDocs);
		btnCloseAllTabs.setCursor(GuiUtilsExtended.CURSOR_HAND);
		btnCloseAllTabs.setIcon(new ImageIcon(IconsPathConfigurator.ICON_REMOVE_ALL_DOCS));
		add(btnCloseAllTabs, "cell 4 0,grow");
		
		btntTextFormat = new JButton("Format");
		btntTextFormat.setMinimumSize(new Dimension(67, 12));
		add(btntTextFormat, "cell 8 0 2 1,grow");
		
				btnTemplates = new JButton("Templates");
				btnTemplates.setMinimumSize(new Dimension(87, 12));
				btnTemplates.setToolTipText(TextEditTooltips.tooltipTemplatesPanel);
				add(btnTemplates, "cell 10 0 2 1,grow");
		
		toggleBtnMap = new JToggleButton("");
		toggleBtnMap.setMinimumSize(new Dimension(25, 0));
		toggleBtnMap.setToolTipText(TextEditTooltips.tooltipMapViewBtn);
		toggleBtnMap.setBackground(GeneralConfig.BUTTON_BCKG_COLOR_TXT_EDITOR.darker());
		toggleBtnMap.setCursor(GuiUtilsExtended.CURSOR_HAND);
		toggleBtnMap.setIcon(new ImageIcon(IconsPathConfigurator.ICON_MAP));
		add(toggleBtnMap, "cell 22 0,alignx center,grow");
		
		toggleBtnClipboard = new JToggleButton("");
		toggleBtnClipboard.setMinimumSize(new Dimension(25, 0));
		toggleBtnClipboard.setBackground(GeneralConfig.BUTTON_BCKG_COLOR_TXT_EDITOR.darker());
		toggleBtnClipboard.setToolTipText(TextEditTooltips.tooltipClipboardViewBtn);
		toggleBtnClipboard.setCursor(GuiUtilsExtended.CURSOR_HAND);
		toggleBtnClipboard.setIcon(new ImageIcon(IconsPathConfigurator.ICON_CLIPBOARD));
		add(toggleBtnClipboard, "cell 23 0,alignx center,grow");
		
		toggleBtnConsole = new JToggleButton("");
		toggleBtnConsole.setMinimumSize(new Dimension(25, 0));
		toggleBtnConsole.setToolTipText(TextEditTooltips.tooltipConsoleView);
		toggleBtnConsole.setCursor(GuiUtilsExtended.CURSOR_HAND);
		toggleBtnConsole.setIcon(new ImageIcon(IconsPathConfigurator.ICON_CONSOLE));
		toggleBtnConsole.setSelected(currentConfiguration.getTextEditorOpt().bottomViewActive);
		add(toggleBtnConsole, "cell 24 0,alignx center,grow");

		toggleButtonSpellCheck = new JToggleButton("");
		toggleButtonSpellCheck.setMinimumSize(new Dimension(25, 0));
		toggleButtonSpellCheck.setToolTipText(TextEditTooltips.tooltipSpellChecker);
		toggleButtonSpellCheck.setCursor(GuiUtilsExtended.CURSOR_HAND);
		toggleButtonSpellCheck.setIcon(new ImageIcon(IconsPathConfigurator.ICON_SPELLCHECKER));
		add(toggleButtonSpellCheck, "cell 24 1,alignx center,grow");
		
		btnThemeEditor = new JButton("");
		btnThemeEditor.setToolTipText(TextEditTooltips.tooltipThemeEditor);
		btnThemeEditor.setCursor(GuiUtilsExtended.CURSOR_HAND);
		btnThemeEditor.setIcon(new ImageIcon(IconsPathConfigurator.ICON_PAINT_PALETTE));
		add(btnThemeEditor, "cell 12 1,grow");
		
		comboFileFormat = new JComboBox<SupportedFileTextEditor>();
		comboFileFormat.setMinimumSize(new Dimension(75, 12));
		comboFileFormat.setToolTipText(TextEditTooltips.tooltipComboFileFormat);
		comboFileFormat.setCursor(GuiUtilsExtended.CURSOR_HAND);
		comboFileFormat.setAlignmentX(CENTER_ALIGNMENT);
		comboFileFormat.setModel(new DefaultComboBoxModel<SupportedFileTextEditor>(SupportedFileTextEditor.values()));
		comboFileFormat.setFont(GeneralConfig.BUTTON_DEFAULT_FONT.deriveFont(10).deriveFont(Font.PLAIN));
		add(comboFileFormat, "cell 6 1 2 1,growx,aligny center");
		
		btnNewTab = new JButton("");
		btnNewTab.setToolTipText(TextEditTooltips.tooltipNewEmptyTab);
		btnNewTab.setForeground(Color.BLACK);
		btnNewTab.setIcon(new ImageIcon(IconsPathConfigurator.ICON_DOC_EMPTY));
		add(btnNewTab, "cell 1 0,grow");

		btnSaveTextFile = new JButton("");
		btnSaveTextFile.setToolTipText(TextEditTooltips.tooltipSaveTextFile);
		btnSaveTextFile.setForeground(Color.BLACK);
		btnSaveTextFile.setIcon(new ImageIcon(IconsPathConfigurator.ICON_FILE_SAVE));
		add(btnSaveTextFile, "cell 2 0,grow");

		btnSaveTextFileAs = new JButton("");
		btnSaveTextFileAs.setToolTipText(TextEditTooltips.tooltipSaveTextFileAs);
		btnSaveTextFileAs.setForeground(SystemColor.menuText);
		btnSaveTextFileAs.setIcon(new ImageIcon(IconsPathConfigurator.ICON_FILE_SAVE_AS));
		add(btnSaveTextFileAs, "cell 3 0,grow");

		btnReloadFromDisk = new JButton("");
		btnReloadFromDisk.setToolTipText(TextEditTooltips.tooltipReloadFromDisk);
		btnReloadFromDisk.setIcon(new ImageIcon(IconsPathConfigurator.ICON_RELOAD_CONF));
		btnReloadFromDisk.setForeground(Color.BLACK);
		add(btnReloadFromDisk, "cell 5 0,grow");

		btnViewInExplorer = new JButton("");
		btnViewInExplorer.setToolTipText(TextEditTooltips.tooltipViewInSysExplorer);
		btnViewInExplorer.setIcon(new ImageIcon(IconsPathConfigurator.ICON_COPY_DIR_STRUCTURE));
		add(btnViewInExplorer, "cell 6 0,grow");

		btnPropertiesPanel = new JButton("");
		btnPropertiesPanel.setToolTipText(TextEditTooltips.tooltipViewPropertiesCurrFile);
		btnPropertiesPanel.setIcon(new ImageIcon(IconsPathConfigurator.ICON_ABOUT));
		add(btnPropertiesPanel, "cell 7 0,grow");

		btnClear = new JButton("");
		btnClear.setToolTipText(TextEditTooltips.tooltipClearText);
		btnClear.setIcon(new ImageIcon(IconsPathConfigurator.ICON_DELETE));
		add(btnClear, "cell 12 0,alignx center,grow");

		btnUndo = new JButton("");
		btnUndo.setToolTipText(TextEditTooltips.tooltipUndo);
		btnUndo.setIcon(new ImageIcon(IconsPathConfigurator.ICON_UNDO));
		add(btnUndo, "cell 13 0,alignx center,grow");

		btnRedo = new JButton("");
		btnRedo.setToolTipText(TextEditTooltips.tooltipRedo);
		add(btnRedo, "cell 14 0,alignx center,grow");
		btnRedo.setIcon(new ImageIcon(IconsPathConfigurator.ICON_REDO));

		btnBold = new JButton("");
		btnBold.setToolTipText(TextEditTooltips.tooltipBoldText);
		btnBold.setIcon(new ImageIcon(IconsPathConfigurator.ICON_TEXT_BOLD));
		add(btnBold, "cell 15 0,alignx center,grow");

		btnParseImageText = new JButton("");
		btnParseImageText.setCursor(GuiUtilsExtended.CURSOR_HAND);
		btnParseImageText.setIcon(new ImageIcon(IconsPathConfigurator.ICON_CONVERT_OCR_IMAGE));
		btnParseImageText.setToolTipText(TextEditTooltips.tooltipParseImage);
		add(btnParseImageText, "cell 17 0,alignx center,grow");

		btnParsePdfText = new JButton("");
		btnParsePdfText.setToolTipText(TextEditTooltips.tooltipParsePdfTxt);
		btnParsePdfText.setCursor(GuiUtilsExtended.CURSOR_HAND);
		btnParsePdfText.setIcon(new ImageIcon(IconsPathConfigurator.F_ICON_PDF));
		add(btnParsePdfText, "cell 18 0,alignx center,grow");

		btnOpenInScreepT = new JButton("");
		btnOpenInScreepT.setToolTipText(TextEditTooltips.tooltipOpenInScreept);
		btnOpenInScreepT.setIcon(new ImageIcon(IconsPathConfigurator.ICON_SCREEPT));
		add(btnOpenInScreepT, "cell 19 0,alignx center,grow");
		
		
		btnCalculator = new JButton("");
		btnCalculator.setToolTipText(TextEditTooltips.tooltipBtnCalculator);
		btnCalculator.setIcon(new ImageIcon(IconsPathConfigurator.ICON_CALCULATOR));
		add(btnCalculator, "cell 21 0,grow");
		
		labelTabSize = new JLabel("Tab size:");
		labelTabSize.setMinimumSize(new Dimension(50, 12));
		labelTabSize.setHorizontalAlignment(SwingConstants.RIGHT);
		labelTabSize.setForeground(SystemColor.menu);
		labelTabSize.setFont(GeneralConfig.BUTTON_DEFAULT_FONT);
		add(labelTabSize, "cell 26 0,alignx right");
		
		tabSizeSpinner = new JSpinner();
		tabSizeSpinner.setMinimumSize(new Dimension(30, GeneralConfig.MINIMUM_BUTTON_SIZE.height));
		tabSizeSpinner.setToolTipText(TextEditTooltips.tooltipTabSize);
		tabSizeSpinner.setCursor(GuiUtilsExtended.CURSOR_HAND);
		tabSizeSpinner.setFont(GeneralConfig.BUTTON_DEFAULT_FONT);
		add(tabSizeSpinner, "cell 27 0,growx");
		tabSizeSpinner.setModel(new SpinnerNumberModel(currentConfiguration.getTextEditorOpt().getTabSize(), 1, 9, 1));

		lblMargin = new JLabel("Margin: ");
		lblMargin.setMinimumSize(new Dimension(65, 12));
		lblMargin.setHorizontalAlignment(SwingConstants.RIGHT);
		lblMargin.setForeground(Color.LIGHT_GRAY);
		lblMargin.setFont(new Font("SansSerif", Font.BOLD, 13));
		lblMargin.setBackground(UIManager.getColor("CheckBox.darkShadow"));
		add(lblMargin, "cell 28 0,alignx right,aligny center");

		lBLMargin = new JLabel("12");
		lBLMargin.setMinimumSize(new Dimension(14, 12));
		lBLMargin.setForeground(Color.WHITE);
		lBLMargin.setFont(new Font("SansSerif", Font.BOLD, 13));
		add(lBLMargin, "cell 29 0,alignx center,aligny center");

		btnToggleFileView = new JToggleButton("");
		btnToggleFileView.setMinimumSize(GeneralConfig.MINIMUM_BUTTON_SIZE);
		btnToggleFileView.setToolTipText(TextEditTooltips.tooltipFileViewBtn);
		btnToggleFileView.setCursor(GuiUtilsExtended.CURSOR_HAND);
		btnToggleFileView.setIcon(new ImageIcon(IconsPathConfigurator.ICON_FILE_N_DIR));
		add(btnToggleFileView, "cell 0 1,grow");

		btnDark = new JButton("");
		btnDark.setIcon(new ImageIcon(IconsPathConfigurator.ICON_LIGHT_ON));
		btnDark.setMargin(new Insets(0, 0, 0, 0));
		btnDark.setToolTipText(TextEditTooltips.tooltipDarkSwitch);
		add(btnDark, "cell 1 1,grow");

		btnPrevHistory = new JButton("");
		btnPrevHistory.setCursor(GuiUtilsExtended.CURSOR_HAND);
		btnPrevHistory.setIcon(new ImageIcon(IconsPathConfigurator.ICON_PREV));
		btnPrevHistory.setToolTipText(TextEditTooltips.tooltipHystoryPrev);
		GuiUtilsExtended.makeTransparentJButton(btnPrevHistory);
		add(btnPrevHistory, "cell 2 1 1 1,alignx right,growy");
		
		btnNextHistory = new JButton("");
		btnNextHistory.setCursor(GuiUtilsExtended.CURSOR_HAND);
		btnNextHistory.setIcon(new ImageIcon(IconsPathConfigurator.ICON_NEXT));
		btnNextHistory.setToolTipText(TextEditTooltips.tooltipHystoryNext);
		GuiUtilsExtended.makeTransparentJButton(btnNextHistory);
		add(btnNextHistory, "cell 3 1 1 1,alignx left,growy");

		chckbxLockLanguage = new JCheckBox("Lock");
		chckbxLockLanguage.setOpaque(false);
		chckbxLockLanguage.setToolTipText(TextEditTooltips.tooltipChckbxLockLanguage);
		chckbxLockLanguage.setCursor(GuiUtilsExtended.CURSOR_HAND);
		chckbxLockLanguage.setIconTextGap(1);
		chckbxLockLanguage.setForeground(Color.WHITE);
		chckbxLockLanguage.setFont(new Font("SansSerif", Font.BOLD, 13));
		add(chckbxLockLanguage, "cell 4 1 2 1,alignx center,aligny center");

		comboStylesScheme = new JComboBox<ThemesWrapper>();
		comboStylesScheme.setMinimumSize(new Dimension(33, 12));
		comboStylesScheme.setMaximumSize(new Dimension(210, 32767));
		initComboStyleScheme();
		comboStylesScheme.setToolTipText(TextEditTooltips.tooltipComboStyleScheme);
		comboStylesScheme.setCursor(GuiUtilsExtended.CURSOR_HAND);
		comboStylesScheme.setAlignmentX(CENTER_ALIGNMENT);
		comboStylesScheme.setFont(GeneralConfig.BUTTON_DEFAULT_FONT.deriveFont(10).deriveFont(Font.PLAIN));
		add(comboStylesScheme, "cell 8 1 4 1,growx,aligny center");

		btnRemoveSpaces = new JButton("");
		btnRemoveSpaces.setToolTipText(TextEditTooltips.tooltipRemoveSpaces);
		btnRemoveSpaces.setIcon(new ImageIcon(IconsPathConfigurator.ICON_REMOVE_WHITE_LINE));
		add(btnRemoveSpaces, "cell 13 1,alignx center,grow");

		btnBackgroundCol = new JButton("");
		btnBackgroundCol.setToolTipText(TextEditTooltips.tooltipBackgroundColor);
		btnBackgroundCol.setIcon(new ImageIcon(IconsPathConfigurator.ICON_TEXT_BG_COLOR));
		add(btnBackgroundCol, "cell 14 1,alignx center,grow");

		btnForegroundCol = new JButton("");
		btnForegroundCol.setToolTipText(TextEditTooltips.tooltipForegroundTxt);
		btnForegroundCol.setIcon(new ImageIcon(IconsPathConfigurator.ICON_TEXT_COLOR));
		add(btnForegroundCol, "cell 15 1,alignx center,grow");

		btnFontChooser = new JButton("");
		btnFontChooser.setToolTipText(TextEditTooltips.tooltipFontChooser);
		btnFontChooser.setIcon(new ImageIcon(IconsPathConfigurator.ICON_TEXT_FONT));
		add(btnFontChooser, "cell 16 1,alignx center,grow");

		btnSpeak = new JButton("");
		btnSpeak.setToolTipText(TextEditTooltips.tooltipSpeak);
		btnSpeak.setIcon(new ImageIcon(IconsPathConfigurator.ICON_SPEAKER));
		add(btnSpeak, "cell 16 0,alignx center,grow");

		btnReplaceEscapesSeq = new JButton("");
		btnReplaceEscapesSeq.setToolTipText(TextEditTooltips.tooltipReplaceEscapes);
		btnReplaceEscapesSeq.setIcon(new ImageIcon(IconsPathConfigurator.ICON_CONVERT_ESCAPES));
		add(btnReplaceEscapesSeq, "cell 17 1,alignx center,grow");
		
		btnQuickFileSearch = new JButton("");
		btnQuickFileSearch.setToolTipText(TextEditTooltips.tooltipQuickFileSearch);
		btnQuickFileSearch.setIcon(new ImageIcon(IconsPathConfigurator.ICON_SEARCH_TEXT));
		add(btnQuickFileSearch, "cell 18 1,alignx center,grow");

		btnColorSampler = new JButton("");
		btnColorSampler.setToolTipText(TextEditTooltips.tooltipBtnColorSampler);
		btnColorSampler.setIcon(new ImageIcon(IconsPathConfigurator.ICON_COLOR_SAMPLER));
		add(btnColorSampler, "cell 20 0,grow");
						
		btnFileContentSearch = new JButton("");
		btnFileContentSearch.setToolTipText(TextEditTooltips.tooltipFileContentSearch);
		btnFileContentSearch.setIcon(new ImageIcon(IconsPathConfigurator.ICON_FILE_SEARCH));
		add(btnFileContentSearch, "cell 19 1,grow");

		btnFindText = new JButton("");
		btnFindText.setToolTipText(TextEditTooltips.tooltipFindText);
		btnFindText.setIcon(new ImageIcon(IconsPathConfigurator.ICON_SEARCH));
		add(btnFindText, "cell 20 1,alignx center,grow");
		
		btnReplaceText = new JButton("");
		btnReplaceText.setToolTipText(TextEditTooltips.tooltipReplaceText);
		btnReplaceText.setIcon(new ImageIcon(IconsPathConfigurator.ICON_REPLACE_TXT));
		add(btnReplaceText, "cell 21 1,grow");

		btnGoToLine = new JButton("");
		btnGoToLine.setToolTipText(TextEditTooltips.tooltipGoToLine);
		btnGoToLine.setIcon(new ImageIcon(IconsPathConfigurator.ICON_GOTO_LINE));
		add(btnGoToLine, "cell 22 1,grow");
		
		btnBase64Conversion = new JButton("");
		btnBase64Conversion.setMinimumSize(new Dimension(25, 0));
		btnBase64Conversion.setIcon(new ImageIcon(IconsPathConfigurator.ICON_64_PURPLE));
		btnBase64Conversion.setToolTipText("Encode selected/all text to base 64 (press ctrl to decode base 64)");
		add(btnBase64Conversion, "cell 23 1,alignx center,grow");
						
		chckbxShowTabGrid = new JCheckBox("Show Tabs");
		chckbxShowTabGrid.setOpaque(false);
		chckbxShowTabGrid.setFont(new Font("SansSerif", Font.BOLD, 13));
		chckbxShowTabGrid.setHorizontalAlignment(SwingConstants.LEFT);
		chckbxShowTabGrid.setCursor(GuiUtilsExtended.CURSOR_HAND);
		chckbxShowTabGrid.setToolTipText(TextEditTooltips.tooltipShowTabGrid);
		chckbxShowTabGrid.setSelected(currentConfiguration.getTextEditorOpt().isTabLineVisible());
		chckbxShowTabGrid.setForeground(SystemColor.menu);
		chckbxShowTabGrid.setBackground(Color.DARK_GRAY);
		add(chckbxShowTabGrid, "cell 26 1 2 1,alignx center");
				
		lblFontSize = new JLabel("Size: ");
		lblFontSize.setToolTipText(TextEditTooltips.tooltipIncreaseTextSize);
		lblFontSize.setFont(new Font("SansSerif", Font.BOLD, 13));
		lblFontSize.setHorizontalAlignment(SwingConstants.CENTER);
		lblFontSize.setForeground(Color.LIGHT_GRAY);
		add(lblFontSize, "cell 28 1,alignx right,aligny center");
		
		lblSIZE = new JLabel("");
		lblSIZE.setToolTipText(TextEditTooltips.tooltipIncreaseTextSize);
		lblSIZE.setFont(new Font("SansSerif", Font.BOLD, 13));
		lblSIZE.setForeground(SystemColor.window);
		add(lblSIZE, "cell 29 1,alignx center,aligny center");
		lblSIZE.setText(String.valueOf(GeneralConfig.DEFAULT_FONT.getSize()) + " Px");
		
		sliderFontSize = new JSlider(4, 100);
		sliderFontSize.setToolTipText(TextEditTooltips.tooltipIncreaseTextSize);
		sliderFontSize.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
		sliderFontSize.setForeground(SystemColor.activeCaptionBorder);
		sliderFontSize.setBackground(UIManager.getColor("Button.shadow"));
		sliderFontSize.setValue(currentConfiguration.getTextEditorOpt().getFont().getSize());
		add(sliderFontSize, "cell 30 1 3 1,growx");
		
		slider_margin = new JSlider(0, 100, 0);
		slider_margin.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
		slider_margin.setBackground(UIManager.getColor("Button.shadow"));
		add(slider_margin, "cell 30 0 3 1,growx");
		
		btnLoadTextFile = new JButton("");
		btnLoadTextFile.setToolTipText(TextEditTooltips.tooltipLoadTextFile);
		btnLoadTextFile.setForeground(SystemColor.menuText);
		btnLoadTextFile.setIcon(new ImageIcon(IconsPathConfigurator.ICON_FILE_LOAD));
		add(btnLoadTextFile, "cell 0 0,grow");
		
		addHandlers();
		
		if(currentConfiguration.getTextEditorOpt().spellChecker) {
			// Initially not selected
			boolean last = toggleButtonSpellCheck.isSelected();
			toggleButtonSpellCheck.doClick();
			toggleButtonSpellCheck.setSelected(!last);
		}

		logger.info(this.getClass().getName() + ": ready!");
	}
	
	// Add action handlers (listeners) to gui elements (buttons, combo, spinners, textboxes, ecc...)
	private void addHandlers() {
		
		// TAB SIZE SELECTOR
		tabSizeSpinner.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				parent.getCurrentTabEditor().loadedTabObj.textArea.setTabSize((int)tabSizeSpinner.getValue());
				currentConfiguration.getTextEditorOpt().setTabSize((int)tabSizeSpinner.getValue());
			}
		});

		// PROPERTIES - CURRENT FILE
		btnPropertiesPanel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
				TabTextEditorObject loadedTabObj = parent.getCurrentTabEditor().loadedTabObj;
				
				if (propertiesFrame == null) {
					loadedTabObj.textArea.setCursor(GuiUtilsExtended.CURSOR_WAIT);
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							try {
								if (loadedTabObj != null && loadedTabObj.loadedFile != null
										&& loadedTabObj.loadedFile.exists()) {
									propertiesFrame = new FilePropertiesFrame(loadedTabObj.loadedFile, true);
									propertiesFrame.setLocationRelativeTo(parent);
									propertiesFrame.setAlwaysOnTop(true);
									propertiesFrame.setTitle(loadedTabObj.loadedFile.getName());
									propertiesFrame.setVisible(true);
								}
							} catch (Exception e) {
								logger.error(e);
							}
							loadedTabObj.textArea.setCursor(GuiUtilsExtended.CURSOR_TEXT);
						}
					});
				} else {
					if (!propertiesFrame.isActive()) {
						if (loadedTabObj.loadedFile != null && loadedTabObj.loadedFile.exists()) {
							propertiesFrame.refreshValues(loadedTabObj.loadedFile);
						}
						propertiesFrame.toFront();
						propertiesFrame.setVisible(true);
					} else {
						propertiesFrame.dispose();
						propertiesFrame = null;
					}
				}
			}
		});
		
		// FILE VIEW
		btnToggleFileView.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String oldRightValStr = "";
				try {
					oldRightValStr = currentConfiguration.readFromFile(INItializer.TEXT_EDITOR_OPT, "dividerFileTreePosX");
				} catch (IOException e1) {
					logger.error("OOOOps", e1);
				}
				int oldRightVal = StringWorker.isEmpty(oldRightValStr)
						? currentConfiguration.getTextEditorOpt().dividerFileTreePosX
						: Integer.valueOf(oldRightValStr);
				
				if (btnToggleFileView.isSelected()) {
					btnToggleFileView.setBackground(GeneralConfig.BUTTON_BCKG_COLOR_TXT_EDITOR_DARKER);
					currentConfiguration.getTextEditorOpt().fileTreeActive = true;
				} else {
					btnToggleFileView.setBackground(GeneralConfig.BUTTON_BCKG_COLOR_TXT_EDITOR);
					currentConfiguration.getTextEditorOpt().fileTreeActive = false;
				}
				parent.fileTreeShowOrHide(currentConfiguration.getTextEditorOpt());
				textEditorUtils.refreshTree(parent.getCurrentTabEditor().loadedTabObj.loadedFile, false);
				
				if(!currentConfiguration.getTextEditorOpt().fileTreeActive) {
					SwingUtilities.invokeLater(() -> {
						parent.applyDeltaToRightToggle(parent.splitPaneTreeAndVerticalSplit.getDividerLocation() - oldRightVal);
					});
				} else {
					SwingUtilities.invokeLater(() -> {
						parent.applyDeltaToRightToggle(parent.splitPaneTreeAndVerticalSplit.getDividerLocation());
					});
				}
			}
		});
		
		// COPY TO CLIPBOARD KEEPING STYLE
		btnThemeEditor.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				MainGUI.mainAPP.windowCreator.newSchemeManager();
			}
		});

		// MAP VIEW
		toggleBtnMap.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						if (!currentConfiguration.getTextEditorOpt().mapViewActive) {
							toggleBtnMap.setBackground(GeneralConfig.BUTTON_BCKG_COLOR_TXT_EDITOR_DARKER);
							currentConfiguration.getTextEditorOpt().mapViewActive = true;
							textEditorUtils.setRightPanelContent(PanelType.MAP_TXTAREA);

						} else {
							toggleBtnMap.setBackground(GeneralConfig.BUTTON_BCKG_COLOR_TXT_EDITOR.darker());
							currentConfiguration.getTextEditorOpt().mapViewActive = false;
						}
						currentConfiguration.getTextEditorOpt().clipboardViewActive = false;
						parent.rightPaneShowOrHide(true);
						
						if(currentConfiguration.getTextEditorOpt().mapViewActive)
							textEditorUtils.checkRightVisibleIfEnabled();
					}
				});
			}
		});

		// MAP VIEW
		toggleBtnConsole.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						if(!toggleBtnConsole.isSelected())
							textEditorUtils.saveBottomDividerLocation();
						
						textEditorUtils.setBottomDividerActive(toggleBtnConsole.isSelected(), true);
					}
				});
			}
		});

		// SPELLCHECKER ON/OFF
		toggleButtonSpellCheck.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						TabTextEditorObject loadedTabObj = parent.getCurrentTabEditor().loadedTabObj;
						
						if (toggleButtonSpellCheck.isSelected()) {
							loadedTabObj.updateSpellCheckerParser(currentConfiguration.getTextEditorOpt().prefDictspellChecker, true);
							currentConfiguration.getTextEditorOpt().spellChecker = true;
							currentConfiguration.writeSingleValue(INItializer.TEXT_EDITOR_OPT, "spellChecker", true);
							toggleButtonSpellCheck.setBackground(GeneralConfig.BUTTON_BCKG_COLOR_TXT_EDITOR_DARKER);
						
						} else {
							RsyntaxUtils.removeAllParsers(loadedTabObj.textArea);
							currentConfiguration.getTextEditorOpt().spellChecker = false;
							currentConfiguration.writeSingleValue(INItializer.TEXT_EDITOR_OPT, "spellChecker", false);
							toggleButtonSpellCheck.setBackground(GeneralConfig.BUTTON_BCKG_COLOR_TXT_EDITOR);
						}
					}
				});
			}
		});

		// CLIPBOARD VIEW
		toggleBtnClipboard.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						if (!currentConfiguration.getTextEditorOpt().clipboardViewActive) {
							toggleBtnClipboard.setBackground(GeneralConfig.BUTTON_BCKG_COLOR_TXT_EDITOR_DARKER);
							currentConfiguration.getTextEditorOpt().clipboardViewActive = true;
							textEditorUtils.setRightPanelContent(PanelType.CLIPBOARD_PANEL);
						} else {
							toggleBtnClipboard.setBackground(GeneralConfig.BUTTON_BCKG_COLOR_TXT_EDITOR.darker());
							currentConfiguration.getTextEditorOpt().clipboardViewActive = false;
						}
						
						currentConfiguration.getTextEditorOpt().mapViewActive = false;
						parent.rightPaneShowOrHide(true);
						if(currentConfiguration.getTextEditorOpt().clipboardViewActive)
							textEditorUtils.checkRightVisibleIfEnabled();
					}
				});
			}
		});

		// OPEN IN SCREEPT CRYPTER
		btnOpenInScreepT.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				if (parent.getCurrentTabEditor().loadedTabObj.loadedFile != null && parent.getCurrentTabEditor().loadedTabObj.loadedFile.exists()) {
					parent.textEditorUtils.openCurrentFileInScreept();
				} else {
					dialogHelper.error("Cannot open current file in screept because it does not exist.", "Cannot open in screept");
				}
				
			}
		});

		// OPEN CURRENT IN SYSTEM EXPLORER
		btnViewInExplorer.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new Thread(new Runnable() {
					public void run() {
						File loadedFileParent = FileVarious.getParent(parent.getCurrentTabEditor().loadedTabObj.loadedFile);
						if (TextEditorShortcutListener.flagCtrlDown.get()) {
							SysUtils.cdIntoCommandLine(loadedFileParent);
						} else {
							GuiUtilsExtended.openInFileSystem(loadedFileParent);
						}
					}
				}).start();
			}
		});

		btnViewInExplorer.addMouseListener(new MouseListener() {
			public void mouseReleased(MouseEvent e) {
			}

			public void mousePressed(MouseEvent e) {
			}

			public void mouseExited(MouseEvent e) {
				parent.shortcutsListener.flagHoverBtnViewExpCmd.set(false);
				parent.shortcutsListener.doControlActions();
			}

			public void mouseEntered(MouseEvent e) {
				parent.shortcutsListener.flagHoverBtnViewExpCmd.set(true);
				parent.shortcutsListener.doControlActions();
			}

			public void mouseClicked(MouseEvent e) {
			}
		});
		
		Runnable escapeLastaUpdater = new Runnable() {
			@Override
			public void run() {
				currentConfiguration.getTextEditorOpt().setLastEscapeLanguage(escapeReplacerPanel.getCurrentComboLang());
			}
		};

		// OPEN TEMPLATES PANEL
		btnReplaceEscapesSeq.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						if (!EscapeReplacerFrame.active) {
							
							escapeReplacerPanel = new EscapeReplacerFrame
							(
								mainApplication.getMainFrame(),
								parent.getCurrentTabEditor().loadedTabObj.textArea,
								btnReplaceEscapesSeq, escapeLastaUpdater
							);
							
							GuiUtilsExtended.centerComponent(escapeReplacerPanel, escapeReplacerPanel.getDefaultDimension());
							escapeReplacerPanel.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
							escapeReplacerPanel.initComboLang(currentConfiguration.getTextEditorOpt().getLastEscapeLanguage());
							escapeReplacerPanel.setAlwaysOnTop(true);
							escapeReplacerPanel.setVisible(true);
							escapeReplacerPanel.setTitle("Escape<->Special converter - " + GeneralConfig.APPLICATION_NAME);
							btnReplaceEscapesSeq.setEnabled(false);
						} else {
							escapeReplacerPanel.dispatchEvent(new WindowEvent(escapeReplacerPanel, WindowEvent.WINDOW_CLOSING));
						}
						escapeReplacerPanel.setLocationRelativeTo(parent.mainPanel);
					}
				});
			}
		});
		
		// CALCULATOR OPENER
		btnCalculator.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						if (calculatorFrame == null || !CalculatorMainApp.active) {
							calculatorMainThread = new CalculatorMainApp(MainGUI.mainAPP.getMainFrame()); 
						}
					}
				});
			}
		});
		
		// COLOR SAMPLER
		btnColorSampler.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						mainApplication.windowCreator.newColorPickerFrame();
					}
				});
			}
		});

		// IMAGE PARSE INJECTOR
		btnParseImageText.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						if (imageParserBox == null || !ImageSelector2TextFrame.active) {
							imageParserBox = new ImageSelector2TextFrame(parent.parentFrame, parent.getCurrentTabEditor().loadedTabObj.textArea,
									GeneralConfig.APPLICATION_NAME + " - Image2Text");
							imageParserBox.setVisible(true);
						} else {
							imageParserBox.dispatchEvent(new WindowEvent(imageParserBox, WindowEvent.WINDOW_CLOSING));
						}
						imageParserBox.setLocationRelativeTo(mainApplication.getMainFrame());
						GuiUtilsExtended.extendFramesAndMoveToTop(mainApplication.getMainFrame(), imageParserBox, JFrame.MAXIMIZED_BOTH);
					}
				});
			}
		});

		// PDF PARSER INJECTOR
		btnParsePdfText.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						if (pdfParserBox == null || !PdfSelector2TextFrame.active) {
							pdfParserBox = new PdfSelector2TextFrame(parent.parentFrame, parent.getCurrentTabEditor().loadedTabObj.textArea,
									GeneralConfig.APPLICATION_NAME + " - Pdf2Text");
							pdfParserBox.setVisible(true);
						} else {
							pdfParserBox.dispatchEvent(new WindowEvent(pdfParserBox, WindowEvent.WINDOW_CLOSING));
						}
						pdfParserBox.setLocationRelativeTo(mainApplication.getMainFrame());
						GuiUtilsExtended.extendFramesAndMoveToTop(mainApplication.getMainFrame(), pdfParserBox, JFrame.MAXIMIZED_BOTH);
					}
				});
			}
		});

		// SEARCH INTO FILE
		btnFileContentSearch.addActionListener((e)-> {
			mainApplication.windowCreator.fileContentSearch();
		});
		
		// SEARCH TEXT
		btnFindText.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						textEditorUtils.addCurrentSelectionToLastSearched();
						if (searchBox == null) {
							searchBox = textEditorUtils.textAreaDialogs.getSearchDialog(parent.parentFrame,  null, textEditorUtils,"successAdvice", "errorAdvice");
							GuiUtilsExtended.setFrameIcon(IconsPathConfigurator.ICON_SEARCH, searchBox);
							try {
								textEditorUtils.textAreaDialogs.setComboItems(
										searchBox, currentConfiguration.getTextEditorOpt().lastSearchTerms.getList(), true);
							} catch (IllegalArgumentException | IllegalAccessException e) {
								logger.error("Cannot update search dialog combobox");
							}
						} else {
							searchBox.setVisible(true);
							searchBox.toFront();
							searchBox.requestFocus();
						}
						
						searchBox.addWindowListener(new java.awt.event.WindowAdapter() {
							@Override
							public void windowClosing(java.awt.event.WindowEvent windowEvent) {
								currentConfiguration.getTextEditorOpt().lastSearchTerms.addUniquesToTop(textEditorUtils.textAreaDialogs.searchList.getList());
								currentConfiguration.writeSingleValue(INItializer.TEXT_EDITOR_OPT, "lastSearchTerms", currentConfiguration.getTextEditorOpt().lastSearchTerms.toCharSeparatedString(","));
								
								searchBox.dispose();
							}
						});
						
						searchBox.setLocationRelativeTo(mainApplication.getMainFrame());
					}
				});
			}
		});
		
		// REPLACE TEXT
		btnReplaceText.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						textEditorUtils.addCurrentSelectionToLastSearched();
						
						if (replaceDialog == null) {
							replaceDialog = textEditorUtils.textAreaDialogs.getReplaceDialog(
									parent.parentFrame, null, textEditorUtils,"successAdvice", "errorAdvice");
							GuiUtilsExtended.setFrameIcon(IconsPathConfigurator.ICON_REPLACE_TXT, replaceDialog);
							try {
								textEditorUtils.textAreaDialogs.setComboItems(
										replaceDialog, currentConfiguration.getTextEditorOpt().lastReplaceTerms.getList(), false);
								textEditorUtils.textAreaDialogs.setComboItems(
										replaceDialog, currentConfiguration.getTextEditorOpt().lastSearchTerms.getList(), true);
							} catch (IllegalArgumentException | IllegalAccessException e) {
								logger.error("Cannot update replace dialog combobox");
							}
						} else {
							replaceDialog.setVisible(true);
							replaceDialog.toFront();
							replaceDialog.requestFocus();
						}
						
						replaceDialog.addWindowListener(new java.awt.event.WindowAdapter() {
							@Override
							public void windowClosing(java.awt.event.WindowEvent windowEvent) {
								// Backup replaced
								currentConfiguration.getTextEditorOpt().lastReplaceTerms.addUniquesToTop(textEditorUtils.textAreaDialogs.replaceList.getList());
								currentConfiguration.writeSingleValue(INItializer.TEXT_EDITOR_OPT, "lastReplaceTerms", currentConfiguration.getTextEditorOpt().lastReplaceTerms.toCharSeparatedString(","));
								
								// Backup searched
								currentConfiguration.getTextEditorOpt().lastSearchTerms.addUniquesToTop(textEditorUtils.textAreaDialogs.searchList.getList());
								currentConfiguration.writeSingleValue(INItializer.TEXT_EDITOR_OPT, "lastSearchTerms", currentConfiguration.getTextEditorOpt().lastSearchTerms.toCharSeparatedString(","));
								
								replaceDialog.dispose();
							}
						});

						replaceDialog.setLocationRelativeTo(mainApplication.getMainFrame());
					}
				});
			}
		});

		// GOTO LINE
		btnGoToLine.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						if (goToLineDialog == null) {
							goToLineDialog = textEditorUtils.textAreaDialogs.getGoToLineDialog(parent.parentFrame);
						} else {
							goToLineDialog.dispatchEvent(new WindowEvent(mainApplication.getMainFrame(), WindowEvent.WINDOW_CLOSING));
							goToLineDialog = textEditorUtils.textAreaDialogs.getGoToLineDialog(parent.parentFrame);
						}
						goToLineDialog.setLocationRelativeTo(mainApplication.getMainFrame());
					}
				});
			}
		});
		
		// CONVERT TO/FROM base 64
		btnBase64Conversion.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
				EncodingUtils encoding64 = new EncodingUtils();
				EncodingUtils.stringEncoding = currentConfiguration.getGenOpt().getCharset();
				
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						RSyntaxTextArea textArea = parent.getCurrentTabEditor().loadedTabObj.textArea;
						textArea.setCursor(GuiUtilsExtended.CURSOR_WAIT);
						
						final int startSel = textArea.getSelectionStart();
						int endSel = textArea.getSelectionEnd();
						String currEncoding = SysUtils.getCurrentSystemEncoding();
						String sourceText = GuiUtilsExtended.getSelectedOrAllText(textArea);
						boolean isSomethingSelected = textArea.getSelectedText() != null;
						try {
							boolean controlDown = parent.shortcutsListener.isCtrlDown();
							String converted = "";

							if (!controlDown) {
								// Convert only selection an inject in the place of current selection
								converted = encoding64.encodeBase64(sourceText.getBytes(currEncoding));
							} else {
								converted = encoding64.decodeBase64(sourceText);
							}

							if (isSomethingSelected) {
								String prefix = textArea.getText(0, startSel);
								String postFix = textArea.getText(endSel, textArea.getText().length() - endSel);
								textArea.setText(prefix + converted + postFix);
								endSel = startSel + converted.length();

							} else {
								textArea.setText(converted);
							}

						} catch (UnsupportedEncodingException | BadLocationException e1) {
							dialogHelper.error("Something has gone wrong :(", "An Error Occurred");
							logger.error("An error occurred tryin' to convert using base64 (SelectedText = " + isSomethingSelected);
						} finally {
							try {
								if (startSel != endSel) {
									textArea.requestFocus();
									textArea.setSelectionStart(startSel);
									textArea.setSelectionEnd(endSel);
								} else {
									textArea.setCaretPosition(endSel);
								}
							} catch (Exception e) {
								logger.error("Error with textarea selection reapply after base 64 conversion", e);
							}
							
							textEditorUtils.updateCurrentUnsavedFromFileRead();
							textArea.setCursor(GuiUtilsExtended.CURSOR_DEFAULT);
							
							parent.getCurrentTabEditor().revalidateFoldingPainted(textArea);
						}
					}
				});
			}
		});
		
		btnBase64Conversion.addMouseListener(new MouseListener() {
			public void mouseReleased(MouseEvent e) {
			}

			public void mousePressed(MouseEvent e) {
			}

			public void mouseExited(MouseEvent e) {
				parent.shortcutsListener.flagHoverBtnBase64.set(false);
				parent.shortcutsListener.doControlActions();
			}

			public void mouseEntered(MouseEvent e) {
				parent.shortcutsListener.flagHoverBtnBase64.set(true);
				parent.shortcutsListener.doControlActions();
			}

			public void mouseClicked(MouseEvent e) {
			}
		});

		// VIEW/HIDE tab grids
		chckbxShowTabGrid.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (chckbxShowTabGrid.isSelected()) {
					currentConfiguration.getTextEditorOpt().setTabLineVisible(true);
					parent.getCurrentTabEditor().loadedTabObj.textArea.setPaintTabLines(true);
				} else {
					currentConfiguration.getTextEditorOpt().setTabLineVisible(false);
					parent.getCurrentTabEditor().loadedTabObj.textArea.setPaintTabLines(false);
				}
			}
		});

		// PREV IN HISTORY
		btnPrevHistory.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				parent.getCurrentTabEditor().goToPreviousInHistory();
			}
		});

		// NEXT IN HISTORY
		btnNextHistory.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				parent.getCurrentTabEditor().goToNextInHistory();
			}
		});

		// BACKGROUND TEXT COLOR
		btnBackgroundCol.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnBackgroundCol.setEnabled(false);
				Color esit = new ColorDialogUtils().askForColor(parent.getCurrentTabEditor().loadedTabObj.textArea, false);
				if (esit != null) {
					currentConfiguration.getTextEditorOpt().setBackCol(esit);
					RsyntaxUtils.setBackground(parent.getCurrentTabEditor().loadedTabObj.textArea, esit);
				}
				btnBackgroundCol.setEnabled(true);
			}
		});

		// FOREGROUND TEXT COLOR
		btnForegroundCol.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				btnForegroundCol.setEnabled(false);
				Color esit = new ColorDialogUtils().askForColor(parent.getCurrentTabEditor().loadedTabObj.textArea, true);
				if (esit != null) {
					currentConfiguration.getTextEditorOpt().setForeCol(esit);
					RsyntaxUtils.setForeground(parent.getCurrentTabEditor().loadedTabObj.textArea, esit);
					parent.getCurrentTabEditor().loadedTabObj.textArea.setForeground(esit);
				}
				btnForegroundCol.setEnabled(true);
			}
		});

		// FONT CHOOSER PANEL
		btnFontChooser.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				TabTextEditorObject loadedTabObj = parent.getCurrentTabEditor().loadedTabObj;

				btnFontChooser.setEnabled(false);

				Font chosenFont = currentConfiguration.getTextEditorOpt().getFont();
				JFontChooserExtended fontChooser = new JFontChooserExtended();
				fontChooser.setSelectedFont(chosenFont);
				int choice = fontChooser.showDialog(MainGUI.mainAPP.getMainFrame());
				
				if (choice == JFontChooserExtended.OK_OPTION) {
					chosenFont = fontChooser.getSelectedFont();
					currentConfiguration.getTextEditorOpt().setFont(chosenFont);
					logger.info("Selected Font : " + chosenFont.toString());
					RsyntaxUtils.setFont(loadedTabObj.textArea, loadedTabObj.textArea.getFont());
					loadedTabObj.textArea.setFont(chosenFont);
					sliderFontSize.setValue(loadedTabObj.textArea.getFont().getSize());
					lblSIZE.setText(String.valueOf(loadedTabObj.textArea.getFont().getSize()) + " Px");
				}
				
				btnFontChooser.setEnabled(true);
			}
		});

		// SWITCH BETWEEN DARK/LIGHT COLORS FOR TEXT AREA
		btnDark.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				comboStylesScheme.setSelectedIndex(comboStylesScheme.getItemCount()-1);
				setComboFileFormatByIndex(SupportedFileTextEditor.TXT);
				
				TabTextEditorObject loadedTabObj = parent.getCurrentTabEditor().loadedTabObj;
				
				Color newBackGroundDark = new Color(30,30,30);

				if (btnDark.getBackground().equals(SystemColor.controlHighlight)) {
					RsyntaxUtils.setBackground(loadedTabObj.textArea, newBackGroundDark);
					loadedTabObj.textArea.setBackground(newBackGroundDark);
					RsyntaxUtils.setForeground(loadedTabObj.textArea, new Color(235, 235, 235));
					loadedTabObj.textArea.setForeground(new Color(235, 235, 235));
					btnDark.setIcon(IconsPathConfigurator.IMG_ICON_LIGHT_ON);
					btnDark.setForeground(SystemColor.controlHighlight);
					btnDark.setBackground(Color.BLACK);
				} else {
					RsyntaxUtils.setBackground(loadedTabObj.textArea, SystemColor.controlHighlight);
					loadedTabObj.textArea.setBackground(SystemColor.controlHighlight);
					RsyntaxUtils.setForeground(loadedTabObj.textArea, Color.BLACK);
					loadedTabObj.textArea.setForeground(Color.BLACK);
					btnDark.setIcon(IconsPathConfigurator.IMG_ICON_LIGHT_OFF);
					btnDark.setForeground(Color.BLACK);
					btnDark.setBackground(SystemColor.controlHighlight);
				}
			}
		});

		// TEXT AREA FORMATTER
		btntTextFormat.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				GuiUtilsExtended.formatTextLater(parent.getCurrentTabEditor().loadedTabObj.textArea, parent.CURRENT_EXT_OUT, currentConfiguration.getTextEditorOpt().tabSize);
			}
		});

		// SLIDER FONT CHANGED
		sliderFontSize.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				
				final TabTextEditorObject loadedTabObj = parent.getCurrentTabEditor().loadedTabObj;
				
				loadedTabObj.textArea.setHighlightCurrentLine(true);

				SwingUtilities.invokeLater(() -> {
						
					Font font = loadedTabObj.textArea.getFont();
					font = new Font(font.getName(), font.getStyle(), sliderFontSize.getValue());
					lblSIZE.setText(String.valueOf(loadedTabObj.textArea.getFont().getSize()) + " Px");
					currentConfiguration.getTextEditorOpt().setFont(font);
					loadedTabObj.textArea.setFont(font);
					
					Font gutterFont = font.getSize() < 20 ? font.deriveFont(font.getSize()-2) : font.deriveFont(20F);
					loadedTabObj.currentScrollPane.getGutter().setLineNumberFont(gutterFont);
					
					RXTextUtilities.centerLineFullInScrollPane(loadedTabObj.textArea);
					
					GuiUtils.launchThreadSafeSwing(new Runnable() {
						public void run() {
							loadedTabObj.textArea.revalidate();
							loadedTabObj.textArea.setHighlightCurrentLine(true);
							loadedTabObj.textArea.repaint(500);
						}
					});
				});
			}
		});

		// SPEAK TEXT
		btnSpeak.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				new Thread(new Runnable() {

					@Override
					public void run() {
						TabTextEditorObject loadedTabObj = parent.getCurrentTabEditor().loadedTabObj;
						
						String selected = (loadedTabObj.textArea.getSelectedText() != null && loadedTabObj.textArea.getSelectedText().length() != 0)? 
								loadedTabObj.textArea.getSelectedText() : loadedTabObj.textArea.getText();
						
						if (objSpeaker != null && objSpeaker.getVoice() != null) {
							objSpeaker.getVoice().deallocate();
							btnSpeak.setBackground(new Color(176, 196, 222));
						} else {
							// else speak
							objSpeaker = new Text2Speech();
							btnSpeak.setBackground(Color.RED);
//							float volume = currentConfiguration.getGenOpt().getGeneralVolume();
							float volume = 1.0F;
							objSpeaker.dospeak(selected , "kevin16", (parent.headerPanel.sliderSpokenSpeed.getValue() / 100.0f), volume);
							objSpeaker = null;
							btnSpeak.setBackground(new Color(176, 196, 222));
						}
					}
				}).start();
			}
		});

		// UNDO LAST OPERATION ON TEXT
		btnUndo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (parent.getCurrentTabEditor().loadedTabObj.textArea.canUndo()) {
					parent.getCurrentTabEditor().loadedTabObj.textArea.undoLastAction();
					textEditorUtils.updateCurrentUnsavedFromFileRead();
				}
			}
		});

		// REDO LAST OPERATION ON TEXT
		btnRedo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				if (parent.getCurrentTabEditor().loadedTabObj.textArea.canRedo()) {
					parent.getCurrentTabEditor().loadedTabObj.textArea.redoLastAction();
					textEditorUtils.updateCurrentUnsavedFromFileRead();
				}

			}
		});

		// CLEAR TEXT AREA
		btnClear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				if (dialogHelper.yesOrNo("Clear Text?")) {
					parent.getCurrentTabEditor().loadedTabObj.textArea.setText("");
				}
			}
		});

		// FILE FORMAT SELECTOR
		comboFileFormat.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				applyFileFormatSyntaxFull();
				currentConfiguration.getTextEditorOpt().setPreferredSyntaxIfNotSync(getCurrentFileType().toString());
				currentConfiguration.writeSingleValue(INItializer.TEXT_EDITOR_OPT, "preferredSyntaxIfNotSync", currentConfiguration.getTextEditorOpt().preferredSyntaxIfNotSync);
				GuiUtils.giveFocusToComponent(comboFileFormat);
				checkFormatterEnabled();
			}
		});
		checkFormatterEnabled();
		
		//  LOCK/UNLOCK auto recognizer of file type and syntax
		chckbxLockLanguage.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				textEditorUtils.findRightSyntaxFromComboNSelect(parent.getCurrentTabEditor().loadedTabObj.loadedFile, !chckbxLockLanguage.isSelected());
				currentConfiguration.getTextEditorOpt().setDefaultAutoSyntaxSync(!chckbxLockLanguage.isSelected());
				currentConfiguration.getTextEditorOpt().setPreferredSyntaxIfNotSync(getCurrentFileType().toString());
				currentConfiguration.writeSingleValue(INItializer.TEXT_EDITOR_OPT, "preferredSyntaxIfNotSync", currentConfiguration.getTextEditorOpt().preferredSyntaxIfNotSync);
			}
		});

		// style graphics
		comboStylesScheme.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textEditorUtils.applySelectedTheme(false);
				GuiUtils.giveFocusToComponent(comboStylesScheme);
			}
		});

		// OPEN TEMPLATES PANEL
		btnTemplates.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!TemplatesChooserFrame.active) {
					templatesPanel = new TemplatesChooserFrame(mainApplication.getMainFrame(), parent.getCurrentTabEditor().loadedTabObj.textArea, btnTemplates);
					templatesPanel.setiSelector(new AbstractSelector<String>() {
						
						@Override
						public String getLastSelected() {
							return currentConfiguration.getTextEditorOpt().lastSelectedTemplate;
						}
						
						@Override
						public String setLastSelected(String last) {
							currentConfiguration.getTextEditorOpt().setLastSelectedTemplate(last);
							return last;
						}
						
					}, true);
					
					templatesPanel.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					templatesPanel.setAlwaysOnTop(true);
					templatesPanel.setVisible(true);
					templatesPanel.setTitle("Templates manager - " + GeneralConfig.APPLICATION_NAME);
					btnTemplates.setEnabled(false);
				}
				templatesPanel.setLocationRelativeTo(parent.mainPanel);
				GuiUtils.centerComponent(templatesPanel, templatesPanel.getDefaultDimension());
			}
		});

		// MARGIN TEXT AREA SELECTOR
		slider_margin.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				textEditorUtils.updateMargin();
			}
		});

		// REMOVE WHITE SPACES OR CHARACTER (CTRL PRESSED)
		btnRemoveSpaces.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TabTextEditorObject loadedTabObj = parent.getCurrentTabEditor().loadedTabObj;

				boolean isSomethingSelected = loadedTabObj.textArea.getSelectedText() != null;
				int start = isSomethingSelected ? loadedTabObj.textArea.getSelectionStart() : 0;
				int end = loadedTabObj.textArea.getSelectionEnd();
				int newEnd = 0;
				if (TextEditorShortcutListener.flagCtrlDown.get()) {
					// remove character - open panel ad hoc
					String selection = dialogHelper.askForString("Specify the character/string you want to remove");
					if (selection != null && !selection.equals("")) {
						newEnd = StringWorker.removeCharacterFromTxtArea(selection, loadedTabObj.textArea, start, end);
					}
					TextEditorShortcutListener.flagCtrlDown.set(false);
				} else {
					// remove whitespaces
					newEnd = StringWorker.removeCharacterFromTxtArea(" ", loadedTabObj.textArea, start, end);
				}
				loadedTabObj.textArea.requestFocus();
				loadedTabObj.textArea.setSelectionStart(start);
				loadedTabObj.textArea.setSelectionEnd(newEnd);
			}
		});

		btnRemoveSpaces.addMouseListener(new MouseListener() {
			public void mouseReleased(MouseEvent e) {
			}

			public void mousePressed(MouseEvent e) {
			}

			public void mouseExited(MouseEvent e) {
				parent.shortcutsListener.flagHoverBtnRemoveSpaces.set(false);
				parent.shortcutsListener.doControlActions();
			}

			public void mouseEntered(MouseEvent e) {
				parent.shortcutsListener.flagHoverBtnRemoveSpaces.set(true);
				parent.shortcutsListener.doControlActions();
			}

			public void mouseClicked(MouseEvent e) {
			}
		});

		// SET TEXT STYLE TO ITALICS - OLD
//		btnQuickFileSearch.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				TabTextEditorObject loadedTabObj = parent.getCurrentTabEditor().loadedTabObj;
//
//				Font txtFont = loadedTabObj.textArea.getFont();
//				if (!txtFont.isItalic()) {
//					loadedTabObj.textArea.setFont(txtFont.deriveFont(Font.ITALIC, txtFont.getSize()));
//				} else {
//					loadedTabObj.textArea.setFont(txtFont.deriveFont(Font.PLAIN, txtFont.getSize()));
//				}
//				currentConfiguration.getTextEditorOpt().setFont(txtFont);
//			}
//		});
		
		// QUICK FILE SEARCH
		btnQuickFileSearch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MainGUI.mainGuiUtils.openQuickSearchIntoWorkspace();
			}
		});

		// MAKE TEXT BOLD
		btnBold.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TabTextEditorObject loadedTabObj = parent.getCurrentTabEditor().loadedTabObj;

				Font txtFont = loadedTabObj.textArea.getFont();
				if (!txtFont.isBold()) {
					loadedTabObj.textArea.setFont(txtFont.deriveFont(Font.BOLD, txtFont.getSize()));
				} else {
					loadedTabObj.textArea.setFont(txtFont.deriveFont(Font.PLAIN, txtFont.getSize()));
				}

			}
		});

		// SAVE TEXT FILE
		btnSaveTextFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				final TabTextEditorObject loadedTabObj = parent.getCurrentTabEditor().loadedTabObj;

				boolean done = false;
				if (loadedTabObj.loadedFile == null || !loadedTabObj.loadedFile.exists()) {
					btnSaveTextFileAs.doClick();
					return;
				} else {
					try {
						String outString = StringWorker.normalizeStringToEol(loadedTabObj.textArea.getText(), EOL.defaultEol);

						done = FileWorker.writeStringToFile(outString, loadedTabObj.loadedFile, true);
					} catch (IOException e1) {
						dialogHelper.error("error Saving the selected file :(");
						logger.error("Exception happened!", e1);
					}
					
					if (done) {
						loadedTabObj.unsaved = false;
						parent.getCurrentTabEditor().updateSavedLabelNTab(null);
						textEditorUtils.refreshMessageAndTabTooltip("[File saved]");
						
						SwingUtilities.invokeLater(() -> {
							// Clean memorized changes triggered by save
							logger.debug("Cleaning changes from tabbedPane history");
							parent.getCurrentTabEditor().changeMonManager.removePendingFiredChanges(loadedTabObj.loadedFile);
							logger.debug("Cleaned changes from tabbedPane history");
						});
						
					} else {
						textEditorUtils.errorAdvice("[Error - not saved]");
						parent.footerPanel.lblCurrentFile.setText("(*)" + loadedTabObj.loadedFile.getName());
						parent.footerPanel.lblCurrentFile.setForeground(Color.RED);
					}
					try {
						parent.footerPanel.lblCurrentFile.setToolTipText(loadedTabObj.loadedFile.getCanonicalPath());
					} catch (IOException e1) {
						logger.error(e);
					}
				}
			}
		});

		// SAVE TEXT FILE AS
		btnSaveTextFileAs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveCurrentFileAs(null);
			}
		});
		
		// REMOVE ALL LOADED TABS
		btnCloseAllTabs.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				startCloseAllTabsProcedure();
			}
		});

		// RELOAD INTO TEXT AREA FROM DISK
		btnReloadFromDisk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						parent.getCurrentTabEditor().reloadCurrentTab(true);
						textEditorUtils.refreshMessageAndTabTooltip("[File Reloaded]");
						textEditorUtils.syncMiniTextAreaContent();
					}
				});
			}
		});

		// LOAD TEXT FILE
		btnLoadTextFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				if (!parent.getCurrentTabEditor().alreadyLoading) {

					File source = MainGuiGeneralAppStarter.fileDialogHelper.fileRead(parent, SupportedFileTextEditor.getMultipleFileFilter());

					parent.getCurrentTabEditor().loadFile(source);
				}
			}
		});

		// ADD NEW TAB
		btnNewTab.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				parent.getCurrentTabEditor().addTab(null);
			}
		});
		
		enableDependenciesFunctions();
	}
	
	private void checkFormatterEnabled() {
		if(GuiUtilsExtended.supportedTypesList.contains(getCurrentFileType().senzapunto.toLowerCase())) {
			btntTextFormat.setEnabled(true);
			btntTextFormat.setToolTipText(TextEditTooltips.tooltipFormatCode);
		} else {
			btntTextFormat.setEnabled(false);
			btntTextFormat.setToolTipText(TextEditTooltips.tooltipFormatCodeDisabled);
		}
	}
	
	public void saveCurrentFileAs(EOL lineSeparator) {
		TabTextEditorObject loadedTabObj = parent.getCurrentTabEditor().loadedTabObj;
		boolean skipRefresh = true;

		if (!parent.getCurrentTabEditor().alreadySaving) {
			// update signal value
			parent.getCurrentTabEditor().alreadySaving = true;
			boolean done = false;
			String exportNamePreset = (loadedTabObj.loadedFile != null && !loadedTabObj.loadedFile.getName().equals(""))?
					loadedTabObj.loadedFile.getName() : "NewFile" + getCurrentFileType().extension;
			exportNamePreset = FileVarious.ensureValidExtension(exportNamePreset, getCurrentFileType().extension);

			try {
				File selectedFile = MainGuiGeneralAppStarter.fileDialogHelper.fileWrite(
						parent.parentFrame, exportNamePreset, FileVarious.getParent(loadedTabObj.loadedFile), SupportedFileTextEditor.getMultipleFileFilter());

				if (selectedFile != null) {
					skipRefresh = false;
					selectedFile = textEditorUtils.adaptFileExt(selectedFile);
					
					String outString = StringWorker.normalizeStringToEol(loadedTabObj.textArea.getText(), lineSeparator == null ? EOL.defaultEol : lineSeparator);
					
					if (selectedFile.exists()) {
						if (dialogHelper.yesOrNo("File " + selectedFile.getName()
								+ " exists, overwrite it?", "Overwrite?")) {
							done = FileWorker.writeStringToFile(outString, selectedFile, true);
						} 
					} else {
						done = selectedFile.createNewFile();
						PrintWriter out = new PrintWriter(selectedFile.getAbsolutePath());
						out.write(outString);
						out.close();
					}
					if (done) {
						String msgOk = "File " + selectedFile.getName() + " succesfully saved!";
						dialogHelper.info(msgOk, "File Saved!");

						if (loadedTabObj.loadedFile == null) {
							// Set as current tabObject's file
							loadedTabObj.loadedFile = selectedFile;
							if (parent.getCurrentTabEditor().loadedTabObj.fileMonitorManager == null) {
								if (loadedTabObj.loadedFile != null && loadedTabObj.loadedFile.exists()) {
									parent.getCurrentTabEditor().changeMonManager.updateCurrMonitor(loadedTabObj.loadedFile);
								}
							} else {
								parent.getCurrentTabEditor().changeMonManager.removePendingFiredChanges(loadedTabObj.loadedFile);
							}
							loadedTabObj.unsaved = false;
						}
						parent.getCurrentTabEditor().updateSavedLabelNTab(null);
						
						parent.footerPanel.lblCurrentFile.setText(loadedTabObj.loadedFile.getName());
						parent.footerPanel.lblCurrentFile.setToolTipText(loadedTabObj.loadedFile.getCanonicalPath());
						textEditorUtils.refreshMessageAndTabTooltip("[File copy saved]");
						currentConfiguration.getFileOpt().setLastDstFolderPath(FileVarious.getParentPath(loadedTabObj.loadedFile));
					} else {
						skipRefresh = true;
						textEditorUtils.errorAdvice("[Error while saving a copy]");
					}
				}
				
			} catch (IOException e1) {
				skipRefresh = true;
				textEditorUtils.errorAdvice("[Error Saving selected file]");
				dialogHelper.error("error Saving the selected file :(");
				logger.error("Exception happened!", e1);
			} finally {
				
				// update signal value
				parent.getCurrentTabEditor().alreadySaving = false;
			}
		}
		if (!skipRefresh) {
			textEditorUtils.refreshTree(loadedTabObj.loadedFile, true);
		}
	}
	
	public void applyFileFormatSyntaxFull() {
		TabTextEditorObject loadedTabObj = parent.getCurrentTabEditor().loadedTabObj;
		
		// backup unsaved to avoid changing saved info with theme change
		textEditorUtils.applyFileFormatFromCombo(false);
		
		if (currentConfiguration.getTextEditorOpt().spellChecker) {
			RsyntaxUtils.removeAllParsers(loadedTabObj.textArea);
			loadedTabObj.updateSpellCheckerParser(currentConfiguration.getTextEditorOpt().prefDictspellChecker, true);
		}else {
			RsyntaxUtils.removeAllParsers(loadedTabObj.textArea);
		}
		
		parent.getCurrentTabEditor().updateSavedLabelNTab(null);
		
		if (!currentConfiguration.getTextEditorOpt().clipboardSyncStyle) {
			currentConfiguration.writeSingleValue(INItializer.TEXT_EDITOR_OPT, "preferredSyntaxIfNotSync",
					currentConfiguration.getTextEditorOpt().preferredSyntaxIfNotSync);
			currentConfiguration.getTextEditorOpt().setPreferredSyntaxIfNotSync(((SupportedFileTextEditor) comboFileFormat.getSelectedItem()).toString());
		}
	}
	
	/**
	 * Read default value from currentConfiguration file and apply read theme using it: NB current configuration in memory will be updated with it
	 */
	public void applyDefaultStyle() {
		try {
			String prefThemeFromFile = currentConfiguration.readFromFile(INItializer.TEXT_EDITOR_OPT, "preferredTheme");
			if (!StringWorker.trimToEmpty(prefThemeFromFile).equals("")) {
				comboStylesScheme.setSelectedItem(ThemesWrapper.getFromListByPath(prefThemeFromFile, themeList));
			}
		} catch (Exception e) {
			logger.error("Cannot set default theme from configuration file", e);
		}
	}
	
	/**
	 * Read default value from currentConfiguration file and apply syntax using it: NB current configuration in memory will be updated with it
	 */
	public void applyDefaultFileFormatSyntax() {
		try {
			String prefSyntaxFromConfigFile = currentConfiguration.readFromFile(INItializer.TEXT_EDITOR_OPT, "preferredTheme");
			if (!StringWorker.trimToEmpty(prefSyntaxFromConfigFile).equals("")) {
				applyFileFormatSyntaxFull();
			}
		} catch (Exception e) {
			logger.error("Cannot set default style from configuration file", e);
		}
	}
	
	public void enableDependenciesFunctions() {
		String koMsg = " not found on this installation: update your libraries plugin to use this function!"; 
		enableDependency(btnSpeak, MainGuiGeneralAppStarter.voiceSpeakerAvailable, TextEditTooltips.tooltipSpeak, "TextToSpeech" + koMsg);
		enableDependency(btnParseImageText, MainGuiGeneralAppStarter.tesseractAvailable, TextEditTooltips.tooltipParseImage, "Tesseract" + koMsg);
		enableDependency(btnParsePdfText, MainGuiGeneralAppStarter.tesseractAvailable, TextEditTooltips.tooltipParsePdfTxt, "Tesseract" + koMsg);
	}
	
	private static void enableDependency(JButton target, boolean enable, String tooltipY, String tooltipN) {
			target.setEnabled(enable);
			target.setToolTipText(enable ? tooltipY : tooltipN);
	}
	
	/**
	 * Close all tabs asking to user dinamically, if cancel is pressed false will be returned
	 * @return
	 */
	public boolean startCloseAllTabsProcedure() {
		int askIfUnsaved = dialogHelper.yesNoOrCanc("Do you want to be asked if any file is changed or unsaved?", "Close All Tabs");
		
		if(askIfUnsaved == JOptionHelperExtended.CANC)
			return false;
		
		if(askIfUnsaved == JOptionHelperExtended.OK) {
			parent.getCurrentTabEditor().removeAllTabs(true);
		
		}else if(askIfUnsaved == JOptionHelperExtended.NO){
			if (dialogHelper.yesOrNo("Are you sure to remove all tabs?")) {
				parent.getCurrentTabEditor().removeAllTabs(false);
			}
		}
		
		return true;
	}
	
	public Vector<ThemesWrapper> initComboStyleScheme() {
		Vector<ThemesWrapper> wrappers = new Vector<ThemesWrapper>();
		try {
			wrappers = ThemesWrapper.getDefaultAndCustomWrappers();
			logger.info("\n\n !! Custom themes initialized from folder: " + GeneralConfig.THEMES_FOLDER_CUSTOM + "\n");
		} catch (IOException e1) {
			wrappers = ThemesWrapper.getDefaultWrappers();
			logger.error("Error retrieving themes, using defaults from "+GeneralConfig.THEMES_FOLDER_DEFAULT
					+"... custom folder with errors was: " + GeneralConfig.THEMES_FOLDER_CUSTOM);
		}
		comboStylesScheme.setModel(new DefaultComboBoxModel<ThemesWrapper>(wrappers));
		
		comboStylesScheme.setSelectedItem(Themes.getThemesWrapperFromName(currentConfiguration.getTextEditorOpt().selectedTheme, wrappers));
		comboStylesScheme.revalidate();
		comboStylesScheme.repaint();
		
		themeList = wrappers;
		
		return wrappers;
	}
	
	public void setComboFileFormatByIndex(SupportedFileTextEditor fileType) {
		for(int i = 0; i < comboFileFormat.getItemCount(); i++) {
			if(((SupportedFileTextEditor)comboFileFormat.getItemAt(i)).equals(fileType)) {
				comboFileFormat.setSelectedIndex(i);
			}
		}
	}
	
	public void initGuiFromConfig(boolean refreshTheme) {
		final TextEditorOption txtOptions = currentConfiguration.getTextEditorOpt();
		
		SwingUtilities.invokeLater(() -> {
			
			slider_margin.setValue(txtOptions.getMargin());
			sliderFontSize.setValue(txtOptions.getFont().getSize());
			// AUTO LANGUAGE SYNC
			chckbxLockLanguage.setSelected(!txtOptions.defaultAutoSyntaxSync);
			
			if (refreshTheme) {
				comboStylesScheme.setSelectedItem(ThemesWrapper.getFromListByPath(txtOptions.selectedTheme, themeList));
			}
			
			if(chckbxLockLanguage.isSelected()) {
				comboFileFormat.setSelectedItem(SupportedFileTextEditor.valueOf(currentConfiguration.getTextEditorOpt().preferredSyntaxIfNotSync));
			}
			
			textEditorUtils.updateMargin(); // SetValue does not trigger action itself
			
			parent.revalidate();
			parent.repaint();
			
			textEditorUtils.setFooterVisibile(currentConfiguration.getTextEditorOpt().footerVisible);
			textEditorUtils.setHeaderVisibileLevel1(currentConfiguration.getTextEditorOpt().headerVisible);
			textEditorUtils.setHeaderVisibileLevel2(currentConfiguration.getTextEditorOpt().subHeaderVisible);
		
		});
	}
	
	public SupportedFileTextEditor getCurrentFileType() {
		return (SupportedFileTextEditor)comboFileFormat.getSelectedItem();
	}
	
}

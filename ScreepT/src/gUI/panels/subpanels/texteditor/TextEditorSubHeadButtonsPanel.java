package gUI.panels.subpanels.texteditor;

import java.awt.Color;
import java.awt.Component;
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
import javax.swing.JTextArea;
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
import org.drjekyll.fontchooser.FontDialog;
import org.fife.rsta.ui.GoToDialog;
import org.fife.rsta.ui.search.FindDialog;
import org.fife.rsta.ui.search.ReplaceDialog;

import com.minhasKamal.ultimateCalculator.CalculatorMainApp;

import conversion.Text2Speech;
import dialogutils.JOptionHelperExtended;
import files.FileWorker;
import files.FileVarious;
import frameutils.frame.ColorPickerFrame;
import frameutils.frame.EscapeReplacerFrame;
import frameutils.frame.FilePropertiesFrame;
import frameutils.utils.GuiUtils;
import gUI.panels.TextEditor;
import gUI.utils.filedialog.TemplatesChooserFrame;
import gUI.utils.shortcutshandler.TextEditorShortcutListener;
import gUI.utils.texteditor.TextEditorUtils;
import gUI.utils.tooltip.TextEditorTooltips;
import gui.ImageSelector2TextFrame;
import gui.PdfSelector2TextFrame;
import maingUI.MainGUI;
import maingUI.MainGuiGeneralAppStarter;
import mainobj.INItializer;
import net.miginfocom.swing.MigLayout;
import om.SupportedFileTextEditor;
import om.TabTextEditorObj;
import om.TextEditorOption;
import om.themes.Themes;
import om.themes.ThemesWrapper;
import resources.GeneralConfig;
import resources.IconsPathConfigurator;
import textareautil.SelectionDtoFull;
import textareautil.TextLineNumber;
import utility.ConversionUtils;
import utility.EncodingUtils;
import utils.RsyntaxUtils;
import utils.SysUtils;
import utils.dialog.ColorDialogUtils;
import utils.panels.arch.PanelType;
import variousutils.HandleString;

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
	
	// RESOURCE FILES //
	private static String LOREM_IPSUM;

	// GENERAL FIELDS //
	private boolean uppercase;
	private String backupText;
	
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
	public JButton btnUpperPostDots;
	public JButton btnLowerAfterDots;
	public JButton btnTt;
	public JButton btnTtTt;
	public JButton btnBackgroundCol;
	public JButton btnParseImageText;
	public JButton btnParsePdfText;
	public JButton btnLoremIpsum;
	public JButton btnFontChooser;
	public JButton btnTemplates;
	public JButton btnDefaultOpt;
	public JButton btnUndo;
	public JButton btnClear;
	public JButton btnRedo;
	public JButton btnBold;
	public JButton btnItaliC;
	public JButton btnForegroundCol;
	public JButton btnNewTab;
	public JButton btnRemoveSpaces;
	public JButton btnViewInExplorer;
	public JButton btnPropertiesPanel;
	public JButton btnCalculator;
	public JButton btnColorSampler;
	private JButton btnStyledCopy;
	public JButton btnBase64Conversion;
	
	public JToggleButton btnToggleFileView;
	public JToggleButton toggleBtnMap;
	public JToggleButton toggleBtnClipboard;
	public JToggleButton toggleButtonSpellCheck;

	public JSpinner tabSizeSpinner;

	public JComboBox<SupportedFileTextEditor> comboFileFormat;
	public JComboBox<ThemesWrapper> comboStylesScheme;
	public Vector<ThemesWrapper> themeList = new Vector<ThemesWrapper>();
	
	public JSlider sliderFontSize;
	public JSlider slider_margin;
	
	// DIALOGS AND JFRAMES
	public FindDialog searchBox;
	public ReplaceDialog replaceDialog;
	public GoToDialog goToLineDialog;
	public TemplatesChooserFrame templatesPanel;
	public EscapeReplacerFrame escapeReplacerPanel;
	public ImageSelector2TextFrame imageParserBox;
	public PdfSelector2TextFrame pdfParserBox;
	public TextLineNumber lineNumberPanel;
	public FilePropertiesFrame propertiesFrame;
	public CalculatorMainApp calculatorFrame;
	public Thread calculatorMainThread;
	public ColorPickerFrame colorPickerFrame;
	
	public Text2Speech objSpeaker; // field containing the current object used to speak text

	public JOptionHelperExtended dialogHelper;
	
	TextEditorSubHeadButtonsPanel thisHeaderRef;
	
	/************************
	*	Main subHeader CONSTRUCTOR
	************************/
	public TextEditorSubHeadButtonsPanel(TextEditor parent) {
		logger.info(this.getClass().getName() + " Starting...");
		
		setBackground(Color.DARK_GRAY);
		LOREM_IPSUM = GeneralConfig.RESOURCES_DIR  + "Templates/LoremIpsum.txt";
		
		thisHeaderRef = this;
		this.uppercase = false;
		this.parent = parent;
		this.textEditorUtils = parent.textEditorUtils;
		this.currentConfiguration = parent.currentConfiguration;
		this.mainApplication = parent.mainApplication;
		dialogHelper = new JOptionHelperExtended(this.parent);
		
		DEFAULT_BCKG_COL = currentConfiguration.getTextEditorOpt().backCol;
		DEFAULT_FORE_COL = currentConfiguration.getTextEditorOpt().foreCol;
		
		setBorder(new EmptyBorder(0, 0, 0, 0));
		setLayout(new MigLayout("insets 2, fill, hidemode 3", "[25px:2%:3%][25px:2%:3%][25px:2%:3%][25px:2%:3%][25px:2%:3%][25px:2%:3%][25px:2%:3%][25px:2%:3%][55px:n:55px,left][25px:n][50px:65px:80px][40px:40px:50px][45px:55px:60px][25px:2%:3%][25px:2%:3%][25px:2%:3%][25px:2%:3%][25px:2%:3%][105px:n][25px:2%:3%][25px:2%:3%][25px:2%:3%][25px:2%:3%][25px:2%:3%][25px:2%:3%][25px:2%:3%][:5px:5px][55px][35px][][][][][]", "[:35px:35px,center][:50px:50px,center]"));
		
		btnCloseAllTabs = new JButton("");
		btnCloseAllTabs.setForeground(Color.BLACK);
		btnCloseAllTabs.setToolTipText(TextEditorTooltips.tooltipRemoveAllDocs);
		btnCloseAllTabs.setCursor(GuiUtils.CURSOR_HAND);
		btnCloseAllTabs.setIcon(new ImageIcon(IconsPathConfigurator.ICON_REMOVE_ALL_DOCS));
		add(btnCloseAllTabs, "cell 4 0,grow");
		
		btntTextFormat = new JButton("Format");
		btntTextFormat.setToolTipText(TextEditorTooltips.tooltipFormatCode);
		btntTextFormat.setMinimumSize(new Dimension(67, 12));
		add(btntTextFormat, "cell 8 0 2 1,grow");

		btnTtTt = new JButton("Tt Tt");
		btnTtTt.setMinimumSize(new Dimension(51, 12));
		btnTtTt.setToolTipText(TextEditorTooltips.tooltipTtTt);
		add(btnTtTt, "cell 10 0,grow");

		btnUpperPostDots = new JButton(".Tt");
		btnUpperPostDots.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnUpperPostDots.setMinimumSize(new Dimension(44, 28));
		btnUpperPostDots.setToolTipText(TextEditorTooltips.tooltipUpperPostDots);
		add(btnUpperPostDots, "cell 12 0,grow");
		
		btnLowerAfterDots = new JButton(".tT");
		btnLowerAfterDots.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnLowerAfterDots.setMinimumSize(new Dimension(44, 28));
		btnLowerAfterDots.setToolTipText(TextEditorTooltips.tooltipLowerPostDots);
		add(btnLowerAfterDots, "cell 12 1,grow");
		
		toggleBtnMap = new JToggleButton("");
		toggleBtnMap.setMinimumSize(new Dimension(25, 0));
		toggleBtnMap.setToolTipText(TextEditorTooltips.tooltipMapViewBtn);
		toggleBtnMap.setBackground(GeneralConfig.BUTTON_BCKG_COLOR_TXT_EDITOR.darker());
		toggleBtnMap.setCursor(GuiUtils.CURSOR_HAND);
		toggleBtnMap.setIcon(new ImageIcon(IconsPathConfigurator.ICON_MAP));
		add(toggleBtnMap, "cell 24 0,alignx center,grow");
		
		toggleBtnClipboard = new JToggleButton("");
		toggleBtnClipboard.setMinimumSize(new Dimension(25, 0));
		toggleBtnClipboard.setBackground(GeneralConfig.BUTTON_BCKG_COLOR_TXT_EDITOR.darker());
		toggleBtnClipboard.setToolTipText(TextEditorTooltips.tooltipClipboardViewBtn);
		toggleBtnClipboard.setCursor(GuiUtils.CURSOR_HAND);
		toggleBtnClipboard.setIcon(new ImageIcon(IconsPathConfigurator.ICON_CLIPBOARD));
		add(toggleBtnClipboard, "cell 25 0,alignx center,grow");
		
		btnStyledCopy = new JButton("");
		btnStyledCopy.setToolTipText(TextEditorTooltips.tooltipStyledCopy);
		btnStyledCopy.setCursor(GuiUtils.CURSOR_HAND);
		btnStyledCopy.setIcon(new ImageIcon(IconsPathConfigurator.ICON_PAINT_PALETTE));
		add(btnStyledCopy, "cell 13 1,grow");
		
		comboFileFormat = new JComboBox<SupportedFileTextEditor>();
		comboFileFormat.setMinimumSize(new Dimension(75, 12));
		comboFileFormat.setToolTipText(TextEditorTooltips.tooltipComboFileFormat);
		comboFileFormat.setCursor(GuiUtils.CURSOR_HAND);
		comboFileFormat.setAlignmentX(CENTER_ALIGNMENT);
		comboFileFormat.setModel(new DefaultComboBoxModel<SupportedFileTextEditor>(SupportedFileTextEditor.values()));
		comboFileFormat.setFont(GeneralConfig.BUTTON_DEFAULT_FONT.deriveFont(10).deriveFont(Font.PLAIN));
		add(comboFileFormat, "cell 5 1 3 1,growx,aligny center");
		
		btnNewTab = new JButton("");
		btnNewTab.setToolTipText(TextEditorTooltips.tooltipNewEmptyTab);
		btnNewTab.setForeground(Color.BLACK);
		btnNewTab.setIcon(new ImageIcon(IconsPathConfigurator.ICON_DOC_EMPTY));
		add(btnNewTab, "cell 1 0,grow");

		btnSaveTextFile = new JButton("");
		btnSaveTextFile.setToolTipText(TextEditorTooltips.tooltipSaveTextFile);
		btnSaveTextFile.setForeground(Color.BLACK);
		btnSaveTextFile.setIcon(new ImageIcon(IconsPathConfigurator.ICON_FILE_SAVE));
		add(btnSaveTextFile, "cell 2 0,grow");

		btnSaveTextFileAs = new JButton("");
		btnSaveTextFileAs.setToolTipText(TextEditorTooltips.tooltipSaveTextFileAs);
		btnSaveTextFileAs.setForeground(SystemColor.menuText);
		btnSaveTextFileAs.setIcon(new ImageIcon(IconsPathConfigurator.ICON_FILE_SAVE_AS));
		add(btnSaveTextFileAs, "cell 3 0,grow");

		btnReloadFromDisk = new JButton("");
		btnReloadFromDisk.setToolTipText(TextEditorTooltips.tooltipReloadFromDisk);
		btnReloadFromDisk.setIcon(new ImageIcon(IconsPathConfigurator.ICON_RELOAD_CONF));
		btnReloadFromDisk.setForeground(Color.BLACK);
		add(btnReloadFromDisk, "cell 5 0,grow");

		btnViewInExplorer = new JButton("");
		btnViewInExplorer.setToolTipText(TextEditorTooltips.tooltipViewInSysExplorer);
		btnViewInExplorer.setIcon(new ImageIcon(IconsPathConfigurator.ICON_COPY_DIR_STRUCTURE));
		add(btnViewInExplorer, "cell 6 0,grow");

		btnPropertiesPanel = new JButton("");
		btnPropertiesPanel.setToolTipText(TextEditorTooltips.tooltipViewPropertiesCurrFile);
		btnPropertiesPanel.setIcon(new ImageIcon(IconsPathConfigurator.ICON_ABOUT));
		add(btnPropertiesPanel, "cell 7 0,grow");

		btnTt = new JButton("TT");
		btnTt.setPreferredSize(new Dimension(25, 20));
		btnTt.setMaximumSize(new Dimension(50, GeneralConfig.MINIMUM_BUTTON_SIZE.height));
		btnTt.setToolTipText(TextEditorTooltips.tooltipTt);
		add(btnTt, "cell 11 0,grow");

		btnClear = new JButton("");
		btnClear.setToolTipText(TextEditorTooltips.tooltipClearText);
		btnClear.setIcon(new ImageIcon(IconsPathConfigurator.ICON_DELETE));
		add(btnClear, "cell 13 0,alignx center,grow");

		btnUndo = new JButton("");
		btnUndo.setToolTipText(TextEditorTooltips.tooltipUndo);
		btnUndo.setIcon(new ImageIcon(IconsPathConfigurator.ICON_UNDO));
		add(btnUndo, "cell 14 0,alignx center,grow");

		btnRedo = new JButton("");
		btnRedo.setToolTipText(TextEditorTooltips.tooltipRedo);
		add(btnRedo, "cell 15 0,alignx center,grow");
		btnRedo.setIcon(new ImageIcon(IconsPathConfigurator.ICON_REDO));

		btnBold = new JButton("");
		btnBold.setToolTipText(TextEditorTooltips.tooltipBoldText);
		btnBold.setIcon(new ImageIcon(IconsPathConfigurator.ICON_TEXT_BOLD));
		add(btnBold, "cell 16 0,alignx center,grow");

		btnItaliC = new JButton("");
		btnItaliC.setToolTipText(TextEditorTooltips.tooltipItalic);
		btnItaliC.setIcon(new ImageIcon(IconsPathConfigurator.ICON_TEXT_ITALIC));
		add(btnItaliC, "cell 17 0,alignx center,grow");

		btnLoremIpsum = new JButton("Lorem Ipsum");
		btnLoremIpsum.setMinimumSize(new Dimension(102, 12));
		btnLoremIpsum.setToolTipText(TextEditorTooltips.tooltipLoremIpsum);
		add(btnLoremIpsum, "cell 18 0,growx");

		btnParseImageText = new JButton("");
		btnParseImageText.setCursor(GuiUtils.CURSOR_HAND);
		btnParseImageText.setIcon(new ImageIcon(IconsPathConfigurator.ICON_CONVERT_OCR_IMAGE));
		btnParseImageText.setToolTipText(TextEditorTooltips.tooltipParseImage);
		add(btnParseImageText, "cell 19 0,alignx center,grow");

		btnParsePdfText = new JButton("");
		btnParsePdfText.setToolTipText(TextEditorTooltips.tooltipParsePdfTxt);
		btnParsePdfText.setCursor(GuiUtils.CURSOR_HAND);
		btnParsePdfText.setIcon(new ImageIcon(IconsPathConfigurator.F_ICON_PDF));
		add(btnParsePdfText, "cell 20 0,alignx center,grow");

		btnOpenInScreepT = new JButton("");
		btnOpenInScreepT.setToolTipText(TextEditorTooltips.tooltipOpenInScreept);
		btnOpenInScreepT.setIcon(new ImageIcon(IconsPathConfigurator.ICON_SCREEPT));
		add(btnOpenInScreepT, "cell 21 0,alignx center,grow");
		
		
		btnCalculator = new JButton("");
		btnCalculator.setToolTipText(TextEditorTooltips.tooltipBtnCalculator);
		btnCalculator.setIcon(new ImageIcon(IconsPathConfigurator.ICON_CALCULATOR));
		add(btnCalculator, "cell 23 0,grow");

		btnColorSampler = new JButton("");
		btnColorSampler.setToolTipText(TextEditorTooltips.tooltipBtnColorSampler);
		btnColorSampler.setIcon(new ImageIcon(IconsPathConfigurator.ICON_COLOR_SAMPLER));
		add(btnColorSampler, "cell 23 1,grow");
		
		labelTabSize = new JLabel("Tab size:");
		labelTabSize.setMinimumSize(new Dimension(50, 12));
		labelTabSize.setHorizontalAlignment(SwingConstants.RIGHT);
		labelTabSize.setForeground(SystemColor.menu);
		labelTabSize.setFont(GeneralConfig.BUTTON_DEFAULT_FONT);
		add(labelTabSize, "cell 27 0,alignx right");
		
		tabSizeSpinner = new JSpinner();
		tabSizeSpinner.setMinimumSize(new Dimension(30, GeneralConfig.MINIMUM_BUTTON_SIZE.height));
		tabSizeSpinner.setToolTipText(TextEditorTooltips.tooltipTabSize);
		tabSizeSpinner.setCursor(GuiUtils.CURSOR_HAND);
		tabSizeSpinner.setFont(GeneralConfig.BUTTON_DEFAULT_FONT);
		add(tabSizeSpinner, "cell 28 0,growx");
		tabSizeSpinner.setModel(new SpinnerNumberModel(currentConfiguration.getTextEditorOpt().getTabSize(), 1, 9, 1));

		lblMargin = new JLabel("Margin: ");
		lblMargin.setMinimumSize(new Dimension(65, 12));
		lblMargin.setHorizontalAlignment(SwingConstants.RIGHT);
		lblMargin.setForeground(Color.LIGHT_GRAY);
		lblMargin.setFont(new Font("SansSerif", Font.BOLD, 13));
		lblMargin.setBackground(UIManager.getColor("CheckBox.darkShadow"));
		add(lblMargin, "cell 29 0,alignx right,aligny center");

		lBLMargin = new JLabel("12");
		lBLMargin.setMinimumSize(new Dimension(14, 12));
		lBLMargin.setForeground(Color.WHITE);
		lBLMargin.setFont(new Font("SansSerif", Font.BOLD, 13));
		add(lBLMargin, "cell 30 0,alignx center,aligny center");

		btnToggleFileView = new JToggleButton("");
		btnToggleFileView.setMinimumSize(GeneralConfig.MINIMUM_BUTTON_SIZE);
		btnToggleFileView.setToolTipText(TextEditorTooltips.tooltipFileViewBtn);
		btnToggleFileView.setCursor(GuiUtils.CURSOR_HAND);
		btnToggleFileView.setIcon(new ImageIcon(IconsPathConfigurator.ICON_FILE_N_DIR));
		add(btnToggleFileView, "cell 0 1,grow");

		btnDark = new JButton("");
		btnDark.setIcon(new ImageIcon(IconsPathConfigurator.ICON_LIGHT_ON));
		btnDark.setMargin(new Insets(0, 0, 0, 0));
		btnDark.setToolTipText(TextEditorTooltips.tooltipDarkSwitch);
		add(btnDark, "cell 1 1,grow");

		btnDefaultOpt = new JButton("Default");
		btnDefaultOpt.setPreferredSize(new Dimension(55, 28));
		btnDefaultOpt.setFont(new Font("SansSerif", Font.PLAIN, 11));
		btnDefaultOpt.setMinimumSize(new Dimension(62, 12));
		btnDefaultOpt.setToolTipText(TextEditorTooltips.tooltipDefaultOpt);
		btnDefaultOpt.setForeground(SystemColor.menuText);
		add(btnDefaultOpt, "cell 2 1 3 1,grow");

		chckbxLockLanguage = new JCheckBox("Lock");
		chckbxLockLanguage.setOpaque(false);
		chckbxLockLanguage.setToolTipText(TextEditorTooltips.tooltipChckbxLockLanguage);
		chckbxLockLanguage.setCursor(GuiUtils.CURSOR_HAND);
		chckbxLockLanguage.setIconTextGap(1);
		chckbxLockLanguage.setForeground(Color.WHITE);
		chckbxLockLanguage.setFont(new Font("SansSerif", Font.BOLD, 13));
		add(chckbxLockLanguage, "cell 8 1,alignx center,aligny center");

		comboStylesScheme = new JComboBox<ThemesWrapper>();
		comboStylesScheme.setMinimumSize(new Dimension(33, 12));
		comboStylesScheme.setMaximumSize(new Dimension(170, 32767));
		themeList = initComboStyleScheme();
		comboStylesScheme.setToolTipText(TextEditorTooltips.tooltipComboStyleScheme);
		comboStylesScheme.setCursor(GuiUtils.CURSOR_HAND);
		comboStylesScheme.setAlignmentX(CENTER_ALIGNMENT);
		comboStylesScheme.setFont(GeneralConfig.BUTTON_DEFAULT_FONT.deriveFont(10).deriveFont(Font.PLAIN));
		add(comboStylesScheme, "cell 9 1 3 1,growx,aligny center");

		btnRemoveSpaces = new JButton("");
		btnRemoveSpaces.setToolTipText(TextEditorTooltips.tooltipRemoveSpaces);
		btnRemoveSpaces.setIcon(new ImageIcon(IconsPathConfigurator.ICON_REMOVE_WHITE_LINE));
		add(btnRemoveSpaces, "cell 14 1,alignx center,grow");

		btnBackgroundCol = new JButton("");
		btnBackgroundCol.setToolTipText(TextEditorTooltips.tooltipBackgroundColor);
		btnBackgroundCol.setIcon(new ImageIcon(IconsPathConfigurator.ICON_TEXT_BG_COLOR));
		add(btnBackgroundCol, "cell 15 1,alignx center,grow");

		btnForegroundCol = new JButton("");
		btnForegroundCol.setToolTipText(TextEditorTooltips.tooltipForegroundTxt);
		btnForegroundCol.setIcon(new ImageIcon(IconsPathConfigurator.ICON_TEXT_COLOR));
		add(btnForegroundCol, "cell 16 1,alignx center,grow");

		btnFontChooser = new JButton("");
		btnFontChooser.setToolTipText(TextEditorTooltips.tooltipFontChooser);
		btnFontChooser.setIcon(new ImageIcon(IconsPathConfigurator.ICON_TEXT_FONT));
		add(btnFontChooser, "cell 17 1,alignx center,grow");

		btnTemplates = new JButton("Templates");
		btnTemplates.setMinimumSize(new Dimension(87, 12));
		btnTemplates.setToolTipText(TextEditorTooltips.tooltipTemplatesPanel);
		add(btnTemplates, "cell 18 1,grow");

		btnSpeak = new JButton("");
		btnSpeak.setToolTipText(TextEditorTooltips.tooltipSpeak);
		btnSpeak.setIcon(new ImageIcon(IconsPathConfigurator.ICON_SPEAKER));
		add(btnSpeak, "cell 19 1,alignx center,grow");

		btnReplaceEscapesSeq = new JButton("");
		btnReplaceEscapesSeq.setToolTipText(TextEditorTooltips.tooltipReplaceEscapes);
		btnReplaceEscapesSeq.setIcon(new ImageIcon(IconsPathConfigurator.ICON_CONVERT_ESCAPES));
		add(btnReplaceEscapesSeq, "cell 20 1,alignx center,grow");

		btnFindText = new JButton("");
		btnFindText.setToolTipText(TextEditorTooltips.tooltipFindText);
		btnFindText.setIcon(new ImageIcon(IconsPathConfigurator.ICON_SEARCH));
		add(btnFindText, "cell 21 1,alignx center,grow");

		btnReplaceText = new JButton("");
		btnReplaceText.setToolTipText(TextEditorTooltips.tooltipReplaceText);
		btnReplaceText.setIcon(new ImageIcon(IconsPathConfigurator.ICON_REPLACE_TXT));
		add(btnReplaceText, "cell 22 1,grow");

		btnGoToLine = new JButton("");
		btnGoToLine.setToolTipText(TextEditorTooltips.tooltipGoToLine);
		btnGoToLine.setIcon(new ImageIcon(IconsPathConfigurator.ICON_GOTO_LINE));
		add(btnGoToLine, "cell 22 0,grow");
		
		btnBase64Conversion = new JButton("");
		btnBase64Conversion.setMinimumSize(new Dimension(25, 0));
		btnBase64Conversion.setIcon(new ImageIcon(IconsPathConfigurator.ICON_64_PURPLE));
		btnBase64Conversion.setToolTipText("Encode selected/all text to base 64 (press ctrl to decode base 64)");
		add(btnBase64Conversion, "cell 24 1,alignx center,grow");
		
		toggleButtonSpellCheck = new JToggleButton("");
		toggleButtonSpellCheck.setMinimumSize(new Dimension(25, 0));
		toggleButtonSpellCheck.setToolTipText(TextEditorTooltips.tooltipSpellChecker);
		toggleButtonSpellCheck.setCursor(GuiUtils.CURSOR_HAND);
		toggleButtonSpellCheck.setIcon(new ImageIcon(IconsPathConfigurator.ICON_SPELLCHECKER));
		add(toggleButtonSpellCheck, "cell 25 1,alignx center,grow");
						
		chckbxShowTabGrid = new JCheckBox("Show Tabs");
		chckbxShowTabGrid.setOpaque(false);
		chckbxShowTabGrid.setFont(new Font("SansSerif", Font.BOLD, 13));
		chckbxShowTabGrid.setHorizontalAlignment(SwingConstants.LEFT);
		chckbxShowTabGrid.setCursor(GuiUtils.CURSOR_HAND);
		chckbxShowTabGrid.setToolTipText(TextEditorTooltips.tooltipShowTabGrid);
		chckbxShowTabGrid.setSelected(currentConfiguration.getTextEditorOpt().isTabLineVisible());
		chckbxShowTabGrid.setForeground(SystemColor.menu);
		chckbxShowTabGrid.setBackground(Color.DARK_GRAY);
		add(chckbxShowTabGrid, "cell 27 1 2 1,alignx center");
				
		lblFontSize = new JLabel("Size: ");
		lblFontSize.setToolTipText(TextEditorTooltips.tooltipIncreaseTextSize);
		lblFontSize.setFont(new Font("SansSerif", Font.BOLD, 13));
		lblFontSize.setHorizontalAlignment(SwingConstants.CENTER);
		lblFontSize.setForeground(Color.LIGHT_GRAY);
		add(lblFontSize, "cell 29 1,alignx right,aligny center");
		
		lblSIZE = new JLabel("");
		lblSIZE.setToolTipText(TextEditorTooltips.tooltipIncreaseTextSize);
		lblSIZE.setFont(new Font("SansSerif", Font.BOLD, 13));
		lblSIZE.setForeground(SystemColor.window);
		add(lblSIZE, "cell 30 1,alignx center,aligny center");
		lblSIZE.setText(String.valueOf(GeneralConfig.DEFAULT_FONT.getSize()) + " Px");
		
		sliderFontSize = new JSlider(4, 100);
		sliderFontSize.setToolTipText(TextEditorTooltips.tooltipIncreaseTextSize);
		sliderFontSize.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
		sliderFontSize.setForeground(SystemColor.activeCaptionBorder);
		sliderFontSize.setBackground(UIManager.getColor("Button.shadow"));
		sliderFontSize.setValue(currentConfiguration.getTextEditorOpt().getFont().getSize());
		add(sliderFontSize, "cell 31 1 3 1,growx");
		
		slider_margin = new JSlider(0, 100, 0);
		slider_margin.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
		slider_margin.setBackground(UIManager.getColor("Button.shadow"));
		add(slider_margin, "cell 31 0 3 1,growx");
		
		btnLoadTextFile = new JButton("");
		btnLoadTextFile.setToolTipText(TextEditorTooltips.tooltipLoadTextFile);
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
				
				TabTextEditorObj loadedTabObj = parent.getCurrentTabEditor().loadedTabObj;
				
				if (propertiesFrame == null) {
					loadedTabObj.textArea.setCursor(GuiUtils.CURSOR_WAIT);
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
							loadedTabObj.textArea.setCursor(GuiUtils.CURSOR_TEXT);
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
				if (btnToggleFileView.isSelected()) {
					btnToggleFileView.setBackground(GeneralConfig.BUTTON_BCKG_COLOR_TXT_EDITOR_DARKER);
					currentConfiguration.getTextEditorOpt().fileTreeActive = true;
				} else {
					btnToggleFileView.setBackground(GeneralConfig.BUTTON_BCKG_COLOR_TXT_EDITOR);
					currentConfiguration.getTextEditorOpt().fileTreeActive = false;
				}
				parent.fileTreeShowOrHide(currentConfiguration.getTextEditorOpt());
				textEditorUtils.refreshTree(parent.getCurrentTabEditor().loadedTabObj.loadedFile, false);
			}
		});
		
		// COPY TO CLIPBOARD KEEPING STYLE
		btnStyledCopy.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				parent.getCurrentTabEditor().loadedTabObj.textArea.copyAsStyledText();
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
						TabTextEditorObj loadedTabObj = parent.getCurrentTabEditor().loadedTabObj;
						
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
						if (TextEditorShortcutListener.flagCtrlDown.get()) {
							SysUtils.cdIntoCommandLine(parent.getCurrentTabEditor().loadedTabObj.loadedFile.getParentFile());
						} else {
							GuiUtils.openInFileSystem(parent.getCurrentTabEditor().loadedTabObj.loadedFile.getParentFile());
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
								btnTemplates, escapeLastaUpdater
							);
							
							GuiUtils.centerComponent(escapeReplacerPanel);
							escapeReplacerPanel.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
							escapeReplacerPanel.initComboLang(currentConfiguration.getTextEditorOpt().getLastEscapeLanguage());
							escapeReplacerPanel.setAlwaysOnTop(true);
							escapeReplacerPanel.setVisible(true);
							escapeReplacerPanel.setTitle("Escape<->Special converter - " + GeneralConfig.APPLICATION_NAME);
							btnTemplates.setEnabled(false);
						} else {
							escapeReplacerPanel.dispatchEvent(new WindowEvent(escapeReplacerPanel, WindowEvent.WINDOW_CLOSING));
						}
						escapeReplacerPanel.setLocationRelativeTo(parent.mainPanel);
//								GuiUtils.changeFrameStateLaterUnchecked(mainApplication.getMainFrame(), escapeReplacerPanel, JFrame.MAXIMIZED_BOTH);
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
							calculatorMainThread = new CalculatorMainApp(); 
							// STarts automatically
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
						if (colorPickerFrame == null || !colorPickerFrame.active) {
							colorPickerFrame = ColorPickerFrame.getInstance(parent.parentFrame);
						}else {
							colorPickerFrame.dispatchEvent(new WindowEvent(colorPickerFrame, WindowEvent.WINDOW_CLOSING));
						}
						colorPickerFrame.setLocationRelativeTo(mainApplication.getMainFrame());
						GuiUtils.extendFramesAndMoveToTop(mainApplication.getMainFrame(), colorPickerFrame, JFrame.MAXIMIZED_BOTH);
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
						GuiUtils.extendFramesAndMoveToTop(mainApplication.getMainFrame(), imageParserBox, JFrame.MAXIMIZED_BOTH);
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
						GuiUtils.extendFramesAndMoveToTop(mainApplication.getMainFrame(), pdfParserBox, JFrame.MAXIMIZED_BOTH);
					}
				});
			}
		});

		// SEARCH TEXT
		btnFindText.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						if (searchBox == null) {
							searchBox = textEditorUtils.textAreaDialogs.getSearchDialog(parent.parentFrame,  null, textEditorUtils,"successAdvice", "errorAdvice");
							GuiUtils.setFrameIcon(IconsPathConfigurator.ICON_SEARCH, searchBox);
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
								currentConfiguration.getTextEditorOpt().lastSearchTerms.addNreplaceToTopIfPresent(textEditorUtils.textAreaDialogs.searchList.getList());
								currentConfiguration.writeSingleValue(INItializer.TEXT_EDITOR_OPT, "lastSearchTerms", currentConfiguration.getTextEditorOpt().lastSearchTerms.toCharSeparatedString(","));
								
								searchBox.dispose();
							}
						});
						
						searchBox.setLocationRelativeTo(mainApplication.getMainFrame());
						GuiUtils.changeFrameStateLaterUnchecked(mainApplication.getMainFrame(), searchBox, JFrame.MAXIMIZED_BOTH);
					}
				});
			}
		});
		
		// REPLACE TEXT
		btnReplaceText.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						if (replaceDialog == null) {
							replaceDialog = textEditorUtils.textAreaDialogs.getReplaceDialog(
									parent.parentFrame, null, textEditorUtils,"successAdvice", "errorAdvice");
							GuiUtils.setFrameIcon(IconsPathConfigurator.ICON_REPLACE_TXT, replaceDialog);
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
								currentConfiguration.getTextEditorOpt().lastReplaceTerms.addNreplaceToTopIfPresent(textEditorUtils.textAreaDialogs.replaceList.getList());
								currentConfiguration.writeSingleValue(INItializer.TEXT_EDITOR_OPT, "lastReplaceTerms", currentConfiguration.getTextEditorOpt().lastReplaceTerms.toCharSeparatedString(","));
								
								// Backup searched
								currentConfiguration.getTextEditorOpt().lastSearchTerms.addNreplaceToTopIfPresent(textEditorUtils.textAreaDialogs.searchList.getList());
								currentConfiguration.writeSingleValue(INItializer.TEXT_EDITOR_OPT, "lastSearchTerms", currentConfiguration.getTextEditorOpt().lastSearchTerms.toCharSeparatedString(","));
								
								replaceDialog.dispose();
							}
						});

						replaceDialog.setLocationRelativeTo(mainApplication.getMainFrame());
						GuiUtils.changeFrameStateLaterUnchecked(mainApplication.getMainFrame(), replaceDialog, JFrame.MAXIMIZED_BOTH);
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
						GuiUtils.changeFrameStateLaterUnchecked(mainApplication.getMainFrame(), goToLineDialog, JFrame.MAXIMIZED_BOTH);
					}
				});
			}
		});
		
		// CONVERT TO/FROM base 64
		btnBase64Conversion.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final JTextArea textArea = parent.getCurrentTabEditor().loadedTabObj.textArea;
				textArea.setCursor(GuiUtils.CURSOR_WAIT);
				
				EncodingUtils encoding64 = new EncodingUtils();
				EncodingUtils.stringEncoding = currentConfiguration.getGenOpt().getCharset();
				
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						final int startSel = textArea.getSelectionStart();
						int endSel = textArea.getSelectionEnd();
						String currEncoding = SysUtils.getCurrentSystemEncoding();
						String sourceText = GuiUtils.getSelectedOrAllText(textArea);
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
							textArea.setCursor(GuiUtils.CURSOR_DEFAULT);
							
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

		// SET DEFAULT TEXT AREA VALUES
		btnDefaultOpt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				logger.debug("Applying default values from .ini current configuration file...");
				
				TabTextEditorObj loadedTabObj = parent.getCurrentTabEditor().loadedTabObj;

				// UPDATE GUI
				RsyntaxUtils.setBackground(loadedTabObj.textArea, DEFAULT_BCKG_COL);
				loadedTabObj.textArea.setBackground(DEFAULT_BCKG_COL);
				RsyntaxUtils.setForeground(loadedTabObj.textArea, DEFAULT_FORE_COL);
				loadedTabObj.textArea.setForeground(DEFAULT_FORE_COL);

				try {
					String marginString = currentConfiguration.readFromFile(INItializer.TEXT_EDITOR_OPT, "margin");
					slider_margin.setValue(INItializer.getIntVarFromString(marginString, 10));
					
					String fontString = currentConfiguration.readFromFile(INItializer.TEXT_EDITOR_OPT, "font");
					Font defaultFont = INItializer.getFontFromString(fontString, currentConfiguration.getTextEditorOpt().getFont());
					
					loadedTabObj.textArea.setFont(defaultFont);
					
					sliderFontSize.setValue(defaultFont.getSize());
					lblSIZE.setText(String.valueOf(defaultFont.getSize()) + " Px");
				} catch (IOException e1) {
					logger.warn("Cannot update default margin/Font: an error occurred", e1);
				}
				
				applyDefaultStyle();
				applyFileFormatSyntaxFull();

				// UPDATE CONFIGURATION (in memory only)
				textEditorUtils.updateDefaultTxtConfig(GeneralConfig.DEFAULT_FONT, DEFAULT_BCKG_COL, DEFAULT_FORE_COL, 10);

				logger.debug("Default values applying from .ini current configuration file Completed!");
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
				
				TabTextEditorObj loadedTabObj = parent.getCurrentTabEditor().loadedTabObj;

				btnFontChooser.setEnabled(false);

				FontDialog.setDefaultLookAndFeelDecorated(false);
				FontDialog.showDialog(loadedTabObj.textArea);
				sliderFontSize.setValue(loadedTabObj.textArea.getFont().getSize());
				lblSIZE.setText(String.valueOf(loadedTabObj.textArea.getFont().getSize()) + " Px");
				RsyntaxUtils.setFont(loadedTabObj.textArea, loadedTabObj.textArea.getFont());
				currentConfiguration.getTextEditorOpt().setFont(loadedTabObj.textArea.getFont());

				btnFontChooser.setEnabled(true);

			}
		});

		// SWITCH BETWEEN DARK/LIGHT COLORS FOR TEXT AREA
		btnDark.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				comboStylesScheme.setSelectedIndex(comboStylesScheme.getItemCount()-1);
				setComboFileFormatByIndex(SupportedFileTextEditor.TXT);
				
				TabTextEditorObj loadedTabObj = parent.getCurrentTabEditor().loadedTabObj;
				
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
		
		// INSERT LOREM IPSUM
		btnLoremIpsum.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				FileWorker.appendFileTxtArea(new File(LOREM_IPSUM), parent.getCurrentTabEditor().loadedTabObj.textArea, false);
			}
		});

		// TEXT AREA FORMATTER
		btntTextFormat.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				GuiUtils.formatTextLater(parent.getCurrentTabEditor().loadedTabObj.textArea, parent.CURRENT_EXT_OUT, currentConfiguration.getTextEditorOpt().tabSize);
			}
		});

		// SLIDER FONT CHANGED
		sliderFontSize.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				TabTextEditorObj loadedTabObj = parent.getCurrentTabEditor().loadedTabObj;
				int oldCaretPLinePos = loadedTabObj.textArea.getCaretPosition();
				
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						Font font = loadedTabObj.textArea.getFont();
						font = new Font(font.getName(), font.getStyle(), sliderFontSize.getValue());
						lblSIZE.setText(String.valueOf(loadedTabObj.textArea.getFont().getSize()) + " Px");
						currentConfiguration.getTextEditorOpt().setFont(font);
						loadedTabObj.textArea.setFont(font);
						
						try {
							
							if(oldCaretPLinePos > 0 ) {
								loadedTabObj.textArea.setCaretPosition(oldCaretPLinePos - 1);
								loadedTabObj.textArea.setCaretPosition(oldCaretPLinePos + 1);
							} else {
								loadedTabObj.textArea.setCaretPosition(oldCaretPLinePos + 1);
								loadedTabObj.textArea.setCaretPosition(oldCaretPLinePos - 1);
							}
						
						}catch(Exception e) {
							logger.warn("Error adjusting font size");
						}
						
						Font gutterFont = font.getSize() < 20 ? font.deriveFont(font.getSize()-2) : font.deriveFont(20F);
						loadedTabObj.currentScrollPane.getGutter().setLineNumberFont(gutterFont);
						
						loadedTabObj.textArea.revalidate();
						loadedTabObj.textArea.repaint();
					}
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
						TabTextEditorObj loadedTabObj = parent.getCurrentTabEditor().loadedTabObj;
						
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

		// MAKE LOWERCASE AFTER LINE ENDERS
		btnLowerAfterDots.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				TabTextEditorObj loadedTabObj = parent.getCurrentTabEditor().loadedTabObj;

				SelectionDtoFull txtAreaDto = new SelectionDtoFull(loadedTabObj.textArea);
				String result = "";

				if (txtAreaDto.selected) {
					result = txtAreaDto.prefix
							.concat(HandleString.lowerAfterDots(txtAreaDto.selection, '.').concat(txtAreaDto.end));
				} else {
					result = HandleString.lowerAfterDots(txtAreaDto.getCompleteString(), '.');
				}

				loadedTabObj.textArea.setText(result);
				loadedTabObj.textArea.requestFocus();
				loadedTabObj.textArea.setSelectionStart(txtAreaDto.first);
				loadedTabObj.textArea.setSelectionEnd(txtAreaDto.last);
			}
		});

		// MAKE UPPER CASE AFTER LINE ENDERS
		btnUpperPostDots.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TabTextEditorObj loadedTabObj = parent.getCurrentTabEditor().loadedTabObj;

				SelectionDtoFull txtAreaDto = new SelectionDtoFull(loadedTabObj.textArea);
				String result = "";

				if (txtAreaDto.selected) {
					result = txtAreaDto.prefix
							.concat(HandleString.upperAfterDots(txtAreaDto.selection, '.').concat(txtAreaDto.end));
				} else {
					result = HandleString.upperAfterDots(txtAreaDto.getCompleteString(), '.');
				}

				loadedTabObj.textArea.setText(result);
				loadedTabObj.textArea.requestFocus();
				loadedTabObj.textArea.setSelectionStart(txtAreaDto.first);
				loadedTabObj.textArea.setSelectionEnd(txtAreaDto.last);
			}
		});

		// FILE FORMAT SELECTOR
		comboFileFormat.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				applyFileFormatSyntaxFull();
			}
		});
		
		//  LOCK/UNLOCK auto recognizer of file type and syntax
		chckbxLockLanguage.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				textEditorUtils.findRightSyntaxFromComboNSelect(parent.getCurrentTabEditor().loadedTabObj.loadedFile, !chckbxLockLanguage.isSelected());
				currentConfiguration.getTextEditorOpt().setDefaultAutoSyntaxSync(!chckbxLockLanguage.isSelected());
				currentConfiguration.getTextEditorOpt().setPreferredSyntaxIfNotSync(((SupportedFileTextEditor)comboFileFormat.getSelectedItem()).toString());
				currentConfiguration.writeSingleValue(INItializer.TEXT_EDITOR_OPT, "preferredSyntaxIfNotSync", currentConfiguration.getTextEditorOpt().preferredSyntaxIfNotSync);
			}
		});

		// style graphics
		comboStylesScheme.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textEditorUtils.applySelectedTheme();
			}
		});

		// OPEN TEMPLATES PANEL
		btnTemplates.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!TemplatesChooserFrame.active) {
					templatesPanel = new TemplatesChooserFrame(mainApplication.getMainFrame(), parent.getCurrentTabEditor().loadedTabObj.textArea, btnTemplates);
					templatesPanel.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					templatesPanel.setAlwaysOnTop(true);
					templatesPanel.setVisible(true);
					templatesPanel.setTitle("TEMPLATES manager - " + GeneralConfig.APPLICATION_NAME);
					btnTemplates.setEnabled(false);
				}
				templatesPanel.setLocationRelativeTo(parent.mainPanel);
				GuiUtils.extendFramesAndMoveToTop(mainApplication.getMainFrame(), templatesPanel, JFrame.MAXIMIZED_BOTH);
			}
		});

		// MARGIN TEXT AREA SELECTOR
		slider_margin.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				textEditorUtils.updateMargin();
			}
		});

		// REMOVE WHITE SPACES OR CHARACHTER (CTRL PRESSED)
		btnRemoveSpaces.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TabTextEditorObj loadedTabObj = parent.getCurrentTabEditor().loadedTabObj;

				int start = loadedTabObj.textArea.getSelectionStart();
				int end = loadedTabObj.textArea.getSelectionEnd();
				int newEnd = 0;
				if (TextEditorShortcutListener.flagCtrlDown.get()) {
					// remove character - open panel ad hoc
					String selection = dialogHelper.askForString("Specify the charachter/string you want to remove");
					if (selection != null && !selection.equals("")) {
						newEnd = HandleString.removeCharachterFromTxtArea(selection, loadedTabObj.textArea, start, end);
					}
					TextEditorShortcutListener.flagCtrlDown.set(false);
				} else {
					// remove whitespaces
					newEnd = HandleString.removeCharachterFromTxtArea(" ", loadedTabObj.textArea, start, end);
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

		// SET TEXT STYLE TO ITALICS
		btnItaliC.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TabTextEditorObj loadedTabObj = parent.getCurrentTabEditor().loadedTabObj;

				Font txtFont = loadedTabObj.textArea.getFont();
				if (!txtFont.isItalic()) {
					loadedTabObj.textArea.setFont(txtFont.deriveFont(Font.ITALIC, txtFont.getSize()));
				} else {
					loadedTabObj.textArea.setFont(txtFont.deriveFont(Font.PLAIN, txtFont.getSize()));
				}
				currentConfiguration.getTextEditorOpt().setFont(txtFont);
			}
		});

		// MAKE TEXT BOLD
		btnBold.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TabTextEditorObj loadedTabObj = parent.getCurrentTabEditor().loadedTabObj;

				Font txtFont = loadedTabObj.textArea.getFont();
				if (!txtFont.isBold()) {
					loadedTabObj.textArea.setFont(txtFont.deriveFont(Font.BOLD, txtFont.getSize()));
				} else {
					loadedTabObj.textArea.setFont(txtFont.deriveFont(Font.PLAIN, txtFont.getSize()));
				}

			}
		});

		// MAKE UPPERCASE AFTER LINE ENDS
		btnTt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TabTextEditorObj loadedTabObj = parent.getCurrentTabEditor().loadedTabObj;

				String text = loadedTabObj.textArea.getText();
				String textBackup = new String(text);
				String selected = loadedTabObj.textArea.getSelectedText();
				String start = "";
				String end = "";
				Boolean none = false;
				int strtN = 0;
				int endN = text.length();
				if (selected == null) {
					// select ALL if no selection
					none = true;
					selected = new String(text);
				} else {
					strtN = loadedTabObj.textArea.getSelectionStart();
					endN = loadedTabObj.textArea.getSelectionEnd();
					start = textBackup.substring(0, strtN);
					end = textBackup.substring(endN, textBackup.length());
				}
				backupText = new String(selected);// create backup copy by value

				if (!uppercase) {
					selected = selected.toUpperCase();
					uppercase = true;
				} else {
					selected = selected.toLowerCase();
					uppercase = false;
				}

				selected = start + selected + end;
				loadedTabObj.textArea.setText(selected);
				if (!none) {
					loadedTabObj.textArea.requestFocus();
					loadedTabObj.textArea.setSelectionStart(strtN);
					loadedTabObj.textArea.setSelectionEnd(endN);
				}
				logger.debug("TT function start selection: " + strtN);
				logger.debug("TT function start selection: " + endN);
			}
		});

		// MAKE EVERY WORD STARTING WITH UPPERCASE LETTER
		btnTtTt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TabTextEditorObj loadedTabObj = parent.getCurrentTabEditor().loadedTabObj;

				String text = loadedTabObj.textArea.getText();
				String textBackup = new String(text);
				String selected = loadedTabObj.textArea.getSelectedText();
				String start = "";
				String end = "";
				int strtN = 0;
				int endN = text.length();
				if (selected == null) {
					// select ALL if no selection
					selected = new String(text);
				} else {
					strtN = loadedTabObj.textArea.getSelectionStart();
					endN = loadedTabObj.textArea.getSelectionEnd();
					start = textBackup.substring(0, strtN);
					end = textBackup.substring(endN, textBackup.length());
				}
				backupText = new String(selected);// create backup copy by value

				selected = HandleString.uppercaseEveryFirstLetterNoSpace(selected);
				if (selected.equals(backupText)) {
					selected = HandleString.lowerCaseEveryFirstLetterNoSpace(selected);
					// btnTtTt.setBackground(SystemColor.activeCaptionBorder);
				} else {
					// btnTtTt.setBackground(Color.gray);
				}
				selected = start + selected + end;
				loadedTabObj.textArea.setText(selected);
				loadedTabObj.textArea.requestFocus();
				loadedTabObj.textArea.setSelectionStart(strtN);
				loadedTabObj.textArea.setSelectionEnd(endN);
				logger.debug("Tt Tt function start selection: " + strtN);
				logger.debug("Tt Tt function start selection: " + endN);

			}
		});

		// SAVE TEXT FILE
		btnSaveTextFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				final TabTextEditorObj loadedTabObj = parent.getCurrentTabEditor().loadedTabObj;

				boolean done = false;
				if (loadedTabObj.loadedFile == null || !loadedTabObj.loadedFile.exists()) {
					btnSaveTextFileAs.doClick();
					return;
				} else {
					try {
						done = FileWorker.writeStringToFile(loadedTabObj.textArea.getText(), loadedTabObj.loadedFile, true);
					} catch (IOException e1) {
						dialogHelper.error("error Saving the selected file :(");
						logger.error("Exception happened!", e1);
					}
					
					if (done) {
						loadedTabObj.unsaved = false;
						parent.getCurrentTabEditor().updateSavedLabelNTab(null);
						textEditorUtils.refreshMessageAndTabTooltip("[File saved]");
						
						// Clean memorized changes triggered by save
						logger.debug("Cleaning changes from tabbedPane history");
						parent.getCurrentTabEditor().changeMonManager.removePendingFiredChanges(loadedTabObj.loadedFile);
						logger.debug("Cleaned changes from tabbedPane history");
						
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
				TabTextEditorObj loadedTabObj = parent.getCurrentTabEditor().loadedTabObj;
				boolean skipRefresh = true;

				if (!parent.getCurrentTabEditor().alreadySaving) {
					// update signal value
					parent.getCurrentTabEditor().alreadySaving = true;
					boolean done = false;
					String exportNamePreset = (loadedTabObj.loadedFile != null && !loadedTabObj.loadedFile.getName().equals(""))?
							loadedTabObj.loadedFile.getName() : "NewFile."+comboFileFormat.getSelectedItem().toString().toLowerCase();
					exportNamePreset = FileVarious.ensureValidExtension(exportNamePreset, comboFileFormat.getSelectedItem().toString());

					try {
						File selectedFile = MainGuiGeneralAppStarter.fileDialogHelper.fileWrite(parent.parentFrame, exportNamePreset);

						if (selectedFile != null) {
							skipRefresh = false;
							selectedFile = textEditorUtils.adaptFileExt(selectedFile);
							if (selectedFile.exists()) {
								if (dialogHelper.yesOrNo("File " + selectedFile.getName()
										+ " exists, overwrite it?", "Overwrite?")) {
									done = FileWorker.writeStringToFile(loadedTabObj.textArea.getText(), selectedFile,
											true);
								} else {
									done = FileWorker.writeStringToFile(loadedTabObj.textArea.getText(), selectedFile,
											false);
								}
							} else {
								done = selectedFile.createNewFile();
								PrintWriter out = new PrintWriter(selectedFile.getAbsolutePath());
								out.write(loadedTabObj.textArea.getText());
								out.close();
							}
							if (done) {
								dialogHelper.info("File " + selectedFile.getName() + " succesfully saved!", "File Saved!");

								// Add as current file
								loadedTabObj.loadedFile = selectedFile;
								
								if (parent.getCurrentTabEditor().loadedTabObj.fileMonitorManager == null) {
									if (loadedTabObj.loadedFile != null && loadedTabObj.loadedFile.exists()) {
										parent.getCurrentTabEditor().changeMonManager.updateCurrMonitor(loadedTabObj.loadedFile);
									}
								}else {
									parent.getCurrentTabEditor().changeMonManager.removePendingFiredChanges(loadedTabObj.loadedFile);
								}
								
								loadedTabObj.unsaved = false;
								parent.getCurrentTabEditor().updateSavedLabelNTab(null);
								
								parent.footerPanel.lblCurrentFile.setText(loadedTabObj.loadedFile.getName());
								parent.footerPanel.lblCurrentFile.setToolTipText(loadedTabObj.loadedFile.getCanonicalPath());
								textEditorUtils.refreshMessageAndTabTooltip("[File saved]");
								currentConfiguration.getFileOpt().setLastDstFolderPath(loadedTabObj.loadedFile.getParentFile().getAbsolutePath());
							} else {
								skipRefresh = true;
								textEditorUtils.errorAdvice("[Error Saving selected file]");
							}
						}
						
					} catch (IOException e1) {
						skipRefresh = true;
						textEditorUtils.errorAdvice("[Error Saving selected file]");
						dialogHelper.error("error Saving the selected file :(");
						logger.error("Exception happened!", e1);
					}
					// update signal value
					parent.getCurrentTabEditor().alreadySaving = false;
				}
				if (!skipRefresh) {
					textEditorUtils.refreshTree(loadedTabObj.loadedFile, true);
				}
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
					}
				});
			}
		});

		// LOAD TEXT FILE
		btnLoadTextFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				if (!parent.getCurrentTabEditor().alreadyLoading) {

					File source = MainGuiGeneralAppStarter.fileDialogHelper.fileRead(parent);

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
	
	public void applyFileFormatSyntaxFull() {
		TabTextEditorObj loadedTabObj = parent.getCurrentTabEditor().loadedTabObj;
		
		// backup unsaved to avoid changing saved info with theme change
		textEditorUtils.applyFileFormatFromCombo();
		
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
			if (!ConversionUtils.trimToEmpty(prefThemeFromFile).equals("")) {
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
			if (!ConversionUtils.trimToEmpty(prefSyntaxFromConfigFile).equals("")) {
				applyFileFormatSyntaxFull();
			}
		} catch (Exception e) {
			logger.error("Cannot set default style from configuration file", e);
		}
	}
	
	public void enableDependenciesFunctions() {
		String koMsg = "Not found on this installation: update your libraries plugin to use this function!"; 
		enableDependency(btnSpeak, MainGuiGeneralAppStarter.voiceSpeakerAvailable, TextEditorTooltips.tooltipSpeak, koMsg);
		enableDependency(btnParseImageText, MainGuiGeneralAppStarter.tesseractAvailable, TextEditorTooltips.tooltipParseImage, koMsg);
		enableDependency(btnParsePdfText, MainGuiGeneralAppStarter.tesseractAvailable, TextEditorTooltips.tooltipParsePdfTxt, koMsg);
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
	
	private Vector<ThemesWrapper> initComboStyleScheme() {
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
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
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
			}
		});
	}
	
}

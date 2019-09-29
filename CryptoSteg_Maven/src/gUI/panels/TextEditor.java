package gUI.panels;

import static gUI.utils.texteditor.TextEditorUtils.*;
import java.awt.Color;
import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.Insets;
import java.awt.KeyboardFocusManager;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import net.miginfocom.swing.MigLayout;
import om.SupportedFileTextEditor;
import om.TabTextEditorObj;
import om.TextEditorOption;
import resources.GeneralConfig;
import resources.IconsPathConfigurator;
import tabbedpaneutils.CustomTabbedPaneUI_v1;
import tabbedpaneutils.TabUtils;
import tabbedpaneutils.TabbedPaneMouseWheelListener;
import textareautil.SelectionDto;
import textareautil.TextLineNumber;
import utils.ColorDialogUtils;
import utils.RsyntaxUtils;
import utils.SysUtils;
import utils.TextAreaDialogs;
import variousutils.HandleString;

import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.drjekyll.fontchooser.FontDialog;
import org.fife.rsta.ui.GoToDialog;
import org.fife.rsta.ui.search.FindDialog;
import org.fife.rsta.ui.search.ReplaceDialog;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RUndoManager;

import com.minhasKamal.ultimateCalculator.CalculatorMainApp;

import amfileutils.FileStuff;
import conversion.Text2Speech;
import crypt.aes.AesCipher;
import dialogutils.GenericFileChooser;
import enums.Themes;
import frameutils.EscapeReplacerPanel;
import frameutils.FileDragNDropManager;
import frameutils.FilePropertiesFrame;
import frameutils.GuiUtils;
import maingUI.MainGuiGeneralAppStarter;
import maingUI.MainGUI;
import mainobj.INItializer;

import java.awt.SystemColor;

import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.LinkedList;
import java.awt.event.ActionEvent;
import javax.swing.JScrollPane;
import javax.swing.ImageIcon;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.event.ChangeEvent;
import javax.swing.UIManager;
import javax.swing.JCheckBox;
import javax.swing.JSeparator;
import javax.swing.border.LineBorder;
import javax.swing.JComboBox;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;

import gUI.utils.filedialog.GenericFileChooserDialog;
import gUI.utils.filedialog.TemplatesChoosePanel;
import gUI.utils.shortcutshandler.TextEditorShortcutListener;
import gUI.utils.texteditor.TextEditorUtils;
import gUI.utils.tooltip.TextEditorTooltips;
import gUI.utils.tree.FileTreeModel;
import gUI.utils.tree.TreeCellRendererDark;
import gUI.utils.tree.texteditor.TreeFileMouseListenerTxtEditor;
import gui.ImageSelector2TextFrame;
import gui.PdfSelector2TextFrame;

import javax.swing.JPasswordField;
import java.awt.Cursor;
import java.awt.Dimension;
import javax.swing.JRadioButton;
import javax.swing.JToggleButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.ScrollPaneConstants;

public class TextEditor extends JPanel {
	
	// TextFileWriter logger creation
	static Logger logger = Logger.getLogger(TextEditor.class);
	
	public static final int timeoutAdvice = 9000;
	public static volatile boolean renewedAdviceTimeout = false;
	
	public static int fileTreeAncestorLevel = GeneralConfig.DEFAULT_FILE_TREE_ANCESTOR_LEVEL;

	public static final String AES_KEY_PLACEHOLDER = "#### (AES KEY) ####";
	public static final char DEFAULT_PSW_CHAR = '\u25CF';
	public static final String DEFAULT_NEW_FILE_NAME = "New.txt";
	
	// this is the color of the left panel file navigator
	public static Color fileNavColorBckg = TextEditorOption.DEFAULT_FILE_PANEL_BACKGROUND_COL;

	// RESOURCE FILES //
	private final static String LOREM_IPSUM = GeneralConfig.RESOURCES_DIR  + "Templates/LoremIpsum.txt";

	// STATIC phrases
	public static final String ROOT_WORKSPACE_LBL = "Workspace: ";
	public static final String ROOT_STANDARD_LBL = "Root: ";
	
	// GUI Objects //
	public JCheckBox checkBoxInlineTabs;
	public JCheckBox chkFitmantainwords;
	public JCheckBox chkCutWordsTo;
	public JCheckBox chckbxAesShowKey;
	public JCheckBox chckbxShowTabGrid;
	public static JCheckBox chckbxLockLanguage;

	public static JTextField lblCurrentFile;
	private JLabel lblTextEditor;
	private JLabel lblCurrentFileLBL;
	private JLabel lblVoiceSpeed;
	private JLabel labelTotLines;
	private JLabel labelTotalChars;
	private JLabel labelSelectionLength;
	private JLabel dynLblTotLines;
	private JLabel dynLblTotChars;
	private JLabel dynLblSelLength;
	private JLabel labelCharset;
	private JLabel dynLabelCharset;
	public JLabel lblSIZE;
	public JLabel lblMargin;
	public JLabel lBLMargin;
	public JLabel lblFontSize;
	public JLabel labelTabSize;
	public static JLabel lblAdvice;

	// footer
	public static JLabel lblColumn;
	public static JLabel dynLblColumn;
	public static JLabel lblLine;
	public static JLabel dynLblCurrLine;
	public static JLabel dynLblSearchEsit;
	
	private JLabel lblLockNavigator;
	public static JLabel lblCurrentDir;
	public static volatile JLabel dynLblCurrentDir;

	public JButton btnFindText;
	public JButton btnReplaceText;
	public JButton btnReplaceEscapesSeq;
	public JButton btnGoToLine;
	public JButton btntTextFormat;
	public JButton btnLoadTextFile;
	public static JButton btnSaveTextFileAs;
	public JButton btnSaveTextFile;
	public JButton btnReloadFromDisk;
	public JButton btnSpeak;
	public JButton btnDecrypt;
	public JButton btnCrypt;
	private JButton btnOpenInScreepT;
	public JButton btnDark;
	public JButton btnUpperPostDots;
	private JButton btnLowerAfterDots;
	public JButton btnTt;
	public JButton btnTtTt;
	public JButton btnBackgroundCol;
	public JButton btnParseImageText;
	private JButton btnParsePdfText;
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
	public static JButton btnCalculator;

	public static JButton toggleButtonFileTreeNewWorkspace;
	public static JButton toggleButtonFileTreeWorkspace;
	public static JButton toggleButtonFileTreeLocked;
	public static JToggleButton btnToggleFileView;
	private JRadioButton rdbtnShowCompletePath;
	
	private JSpinner tabSizeSpinner;


	private JSeparator separator_NO_HIDE;
	private JSeparator separator_NO_HIDE_2;
	public JSeparator separator_1;
	public JSeparator separator_2;

	public static JComboBox comboFileFormat;
	private JComboBox comboStylesScheme;

	public JSlider sliderSpokenSpeed;
	public JSlider sliderFontSize;
	public JSlider slider_margin;

	public static LinkedList<TabTextEditorObj> loadedList;
	public static volatile TabTextEditorObj loadedTabObj;
	public static FileDragNDropManager mainTabList;
	public static JSplitPane splitPaneMain;
	public static JScrollPane leftSplitPane;
	public static JPanel leftSplitContainer; 
	public static JTree leftSplitTreeFiles;
	private JPasswordField txtAesKey; // field containing fast crypt text user key, sinchronized from ini file as

	// DIALOGS AND JFRAMES
	public FindDialog searchBox;
	public ReplaceDialog replaceDialog;
	public GoToDialog goToLineDialog;
	public EscapeReplacerPanel escapeReplacerPanel;
	private ImageSelector2TextFrame imageParserBox;
	private PdfSelector2TextFrame pdfParserBox;
	public TextLineNumber lineNumberPanel;
	public FilePropertiesFrame propertiesFrame;
	public CalculatorMainApp calculatorFrame;
	public Thread calculatorMainThread;

	public static boolean isCrypterOn = false;

	public JFrame parentFrame; // the frame that instantiate this object

	public JScrollPane mainScrollPane;
	public JPanel mainPanel;
	public JPanel headerPanel;
	public JPanel footerPanel;

	// GENERAL FIELDS //
	private boolean uppercase;
	private String backupText;

	// value used for margins of text
	private int textMargin;

	// flag to signal if function is already running
	public boolean alreadySaving = false;
	public static boolean alreadyLoading = false;
	public boolean askSaveBeforeClose = true;

	public static String CURRENT_EXT_OUT = ".txt";
	public Color LBL_ADVICE_COLOR;

	public RUndoManager manager;
	public static INItializer currentConfiguration;
	public TextEditor currentTextEditor; // current object
	private Text2Speech objSpeaker; // field containing the current object used to speak text

	public MainGUI mainApplication;

	private TextEditorShortcutListener shortcutsListener;
	
	public TreeFileMouseListenerTxtEditor mouseListenerTreeFilePanel;
	
	//////////////// CONSTRUCTOR /////////////////////

	public TextEditor(MainGUI mainGui, INItializer currentConf) {
		
		// Initialize text editor Utils reference
		TextEditorUtils.currentTextEditor = this;

		logger.info(this.getClass().getName() + " Starting...");

		loadedList = new LinkedList<TabTextEditorObj>();
		
		setBorder(BorderFactory.createEmptyBorder());
		setBackground(Color.BLACK);
		Dimension maxSize = GuiUtils.getScreenSize();
		setLayout(new MigLayout(GeneralConfig.DEBUG_GRAPHICS+"fill, insets 1, hidemode 2","[300px:"+MainGUI.screenSize.getWidth()+"px:"+MainGUI.screenSize.getWidth()+"px,grow]",
										"[300px:"+MainGUI.screenSize.getHeight()+"px:"+MainGUI.screenSize.getHeight()+"px,grow]"));

		// initialize values used in gui //
		currentConfiguration = currentConf;
		parentFrame = mainGui.getMainFrame();
		mainApplication = mainGui;
		currentTextEditor = this;
		LBL_ADVICE_COLOR = new Color(124, 252, 0);
		textMargin = 14;
		uppercase = false;
		
		fileNavColorBckg = currentConfiguration.getTextEditorOpt().filePanelBackCol;

		mainPanel = new JPanel();
		mainPanel.setMinimumSize(new Dimension(20, 0));
		mainPanel.setBackground(Color.DARK_GRAY);
		mainPanel.setMinimumSize(new Dimension(800, 300));
		mainPanel.setMaximumSize(GuiUtils.getScreenSizePerc(98, parentFrame));
		mainPanel.setLayout(new MigLayout("fill, insets 1, hidemode 2", "[25px:2%:2.5%][25px:2%:2.5%][25px:2%:2.5%][25px:2%:2.5%][25px:2%:2.5%][25px:2%:2.5%][25px:2%:2.5%][20px:n:60px][30px:30px:30px][:3%:4%][:60px:70px][40px:2%:50px][25px:2%:2.5%][25px:2%:2.5%][25px:2%:2.5%][25px:2%:2.5%][25px:2%:2.5%][50px:2%:3.5%][:50px:50px][20][30][][25px:2%:2.5%][25px:2%:2.5%][25px:2%:2.5%][25px:2%:2.5%][25px:2%:2.5%][5px:5px:5px][25px:50px:60px][25px:50px:60px][55px:55px:55px][40px:40px:40px][45px:45px:45px][30px:35px:35px][10px:30px,grow]", "[:0px:35px][0px:5px][:0px:25px][:0px:25px][0px:4px][0px:4px][300px,grow 300,fill][:0px:28px]"));
		mainScrollPane = new JScrollPane();
		mainScrollPane.setMinimumSize(new Dimension(1100, 300));
		mainScrollPane.setBorder(BorderFactory.createEmptyBorder());
		mainScrollPane.setBackground(Color.DARK_GRAY);
		mainScrollPane.setViewportView(mainPanel);
		add(mainScrollPane, "cell 0 0,grow");

		btnNewTab = new JButton("");
		btnNewTab.setToolTipText(TextEditorTooltips.tooltipNewEmptyTab);
		btnNewTab.setForeground(Color.BLACK);
		btnNewTab.setIcon(new ImageIcon(IconsPathConfigurator.ICON_DOC_EMPTY));
		mainPanel.add(btnNewTab, "cell 1 2,grow");

		btnSaveTextFile = new JButton("");
		btnSaveTextFile.setToolTipText(TextEditorTooltips.tooltipSaveTextFile);
		btnSaveTextFile.setForeground(Color.BLACK);
		btnSaveTextFile.setIcon(new ImageIcon(IconsPathConfigurator.ICON_FILE_SAVE));
		mainPanel.add(btnSaveTextFile, "cell 2 2,grow");

		btnSaveTextFileAs = new JButton("");
		btnSaveTextFileAs.setToolTipText(TextEditorTooltips.tooltipSaveTextFileAs);
		btnSaveTextFileAs.setForeground(SystemColor.menuText);
		btnSaveTextFileAs.setIcon(new ImageIcon(IconsPathConfigurator.ICON_FILE_SAVE_AS));
		mainPanel.add(btnSaveTextFileAs, "cell 3 2,grow");

		btnReloadFromDisk = new JButton("");
		btnReloadFromDisk.setToolTipText(TextEditorTooltips.tooltipReloadFromDisk);
		btnReloadFromDisk.setIcon(new ImageIcon(IconsPathConfigurator.ICON_RELOAD_CONF));
		btnReloadFromDisk.setForeground(Color.BLACK);
		mainPanel.add(btnReloadFromDisk, "cell 4 2,grow");

		btnViewInExplorer = new JButton("");
		btnViewInExplorer.setToolTipText(TextEditorTooltips.tooltipViewInSysExplorer);
		btnViewInExplorer.setIcon(new ImageIcon(IconsPathConfigurator.ICON_COPY_DIR_STRUCTURE));
		mainPanel.add(btnViewInExplorer, "cell 5 2,grow");

		btnPropertiesPanel = new JButton("");
		btnPropertiesPanel.setToolTipText(TextEditorTooltips.tooltipViewPropertiesCurrFile);
		btnPropertiesPanel.setIcon(new ImageIcon(IconsPathConfigurator.ICON_ABOUT));
		mainPanel.add(btnPropertiesPanel, "cell 6 2,grow");

		btnTtTt = new JButton("Tt Tt");
		btnTtTt.setToolTipText(TextEditorTooltips.tooltipTtTt);
		mainPanel.add(btnTtTt, "cell 7 2 2 1,growx");

		btnTt = new JButton("TT");
		btnTt.setToolTipText(TextEditorTooltips.tooltipTt);
		mainPanel.add(btnTt, "cell 9 2,growx");

		btnUpperPostDots = new JButton(".Tt");
		btnUpperPostDots.setToolTipText(TextEditorTooltips.tooltipUpperPostDots);
		mainPanel.add(btnUpperPostDots, "cell 10 2,growx");

		btnLowerAfterDots = new JButton(".tT");
		btnLowerAfterDots.setToolTipText(TextEditorTooltips.tooltipLowerPostDots);
		mainPanel.add(btnLowerAfterDots, "cell 11 2");

		btnClear = new JButton("");
		btnClear.setToolTipText(TextEditorTooltips.tooltipClearText);
		btnClear.setIcon(new ImageIcon(IconsPathConfigurator.ICON_DELETE));
		mainPanel.add(btnClear, "cell 12 2,alignx center,aligny center");

		btnUndo = new JButton("");
		btnUndo.setToolTipText(TextEditorTooltips.tooltipUndo);
		btnUndo.setIcon(new ImageIcon(IconsPathConfigurator.ICON_UNDO));
		mainPanel.add(btnUndo, "cell 13 2,alignx center");

		btnRedo = new JButton("");
		btnRedo.setToolTipText(TextEditorTooltips.tooltipRedo);
		mainPanel.add(btnRedo, "cell 14 2,alignx center");
		btnRedo.setIcon(new ImageIcon(IconsPathConfigurator.ICON_REDO));

		btnBold = new JButton("");
		btnBold.setToolTipText(TextEditorTooltips.tooltipBoldText);
		btnBold.setIcon(new ImageIcon(IconsPathConfigurator.ICON_TEXT_BOLD));
		mainPanel.add(btnBold, "cell 15 2,alignx center");

		btnItaliC = new JButton("");
		btnItaliC.setToolTipText(TextEditorTooltips.tooltipItalic);
		btnItaliC.setIcon(new ImageIcon(IconsPathConfigurator.ICON_TEXT_ITALIC));
		mainPanel.add(btnItaliC, "cell 16 2,alignx center");

		btnLoremIpsum = new JButton("Lorem Ipsum");
		btnLoremIpsum.setToolTipText(TextEditorTooltips.tooltipLoremIpsum);
		mainPanel.add(btnLoremIpsum, "cell 21 2,growx");

		btnParseImageText = new JButton("");
		btnParseImageText.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnParseImageText.setIcon(new ImageIcon(IconsPathConfigurator.ICON_CONVERT_OCR_IMAGE));
		btnParseImageText.setToolTipText(TextEditorTooltips.tooltipParseImage);
		mainPanel.add(btnParseImageText, "cell 22 2,alignx center");

		btnParsePdfText = new JButton("");
		btnParsePdfText.setToolTipText(TextEditorTooltips.tooltipParsePdfTxt);
		btnParsePdfText.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnParsePdfText.setIcon(new ImageIcon(IconsPathConfigurator.F_ICON_PDF));
		mainPanel.add(btnParsePdfText, "cell 23 2,alignx center");

		btnOpenInScreepT = new JButton("");
		btnOpenInScreepT.setToolTipText(TextEditorTooltips.tooltipOpenInScreept);
		btnOpenInScreepT.setIcon(new ImageIcon(IconsPathConfigurator.ICON_SCREEPT));
		mainPanel.add(btnOpenInScreepT, "cell 24 2,alignx center");
		
		btnCalculator = new JButton("");
		btnCalculator.setToolTipText(TextEditorTooltips.tooltipBtnCalculator);
		btnCalculator.setIcon(new ImageIcon(IconsPathConfigurator.ICON_CALCULATOR));
		mainPanel.add(btnCalculator, "cell 26 2");
		
		labelTabSize = new JLabel("Tab Size:");
		mainPanel.add(labelTabSize, "cell 28 2");
		labelTabSize.setHorizontalAlignment(SwingConstants.RIGHT);
		labelTabSize.setForeground(SystemColor.menu);
		
		tabSizeSpinner = new JSpinner();
		tabSizeSpinner.setToolTipText(TextEditorTooltips.tooltipTabSize);
		tabSizeSpinner.setCursor(GuiUtils.CURSOR_HAND);
		mainPanel.add(tabSizeSpinner, "cell 29 2,growx");
		tabSizeSpinner.setModel(new SpinnerNumberModel(currentConf.getTextEditorOpt().getTabSize(), 1, 9, 1));

		lblMargin = new JLabel("Margin:");
		lblMargin.setHorizontalAlignment(SwingConstants.CENTER);
		lblMargin.setForeground(SystemColor.menu);
		lblMargin.setBackground(UIManager.getColor("CheckBox.darkShadow"));
		mainPanel.add(lblMargin, "cell 30 2,alignx center");

		lBLMargin = new JLabel("12");
		lBLMargin.setForeground(Color.WHITE);
		mainPanel.add(lBLMargin, "cell 31 2,alignx center");

		btnToggleFileView = new JToggleButton("");
		btnToggleFileView.setMinimumSize(new Dimension(16, 0));
		btnToggleFileView.setToolTipText(TextEditorTooltips.tooltipFileViewBtn);
		btnToggleFileView.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnToggleFileView.setIcon(new ImageIcon(IconsPathConfigurator.ICON_FILE_N_DIR));
		mainPanel.add(btnToggleFileView, "cell 0 3,grow");

		btnDark = new JButton("");
		btnDark.setMargin(new Insets(0, 0, 0, 0));
		btnDark.setToolTipText(TextEditorTooltips.tooltipDarkSwitch);
		mainPanel.add(btnDark, "cell 1 3,grow");

		btnDefaultOpt = new JButton("Default");
		btnDefaultOpt.setToolTipText(TextEditorTooltips.tooltipDefaultOpt);
		btnDefaultOpt.setForeground(SystemColor.menuText);
		mainPanel.add(btnDefaultOpt, "cell 2 3 2 1,growx");

		comboFileFormat = new JComboBox();
		comboFileFormat.setToolTipText(TextEditorTooltips.tooltipComboFileFormat);
		comboFileFormat.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		comboFileFormat.setBackground(new Color(100, 149, 237));
		comboFileFormat.setModel(new DefaultComboBoxModel(SupportedFileTextEditor.values()));
		mainPanel.add(comboFileFormat, "cell 4 3 3 1,growx");
		
		chckbxLockLanguage = new JCheckBox("Lock");
		chckbxLockLanguage.setToolTipText(TextEditorTooltips.tooltipChckbxLockLanguage);
		chckbxLockLanguage.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		chckbxLockLanguage.setIconTextGap(1);
		chckbxLockLanguage.setForeground(Color.WHITE);
		chckbxLockLanguage.setFont(new Font("SansSerif", Font.BOLD, 12));
		mainPanel.add(chckbxLockLanguage, "cell 7 3 2 1");

		comboStylesScheme = new JComboBox();
		comboStylesScheme.setToolTipText(TextEditorTooltips.tooltipComboStyleScheme);
		comboStylesScheme.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		comboStylesScheme.setModel(new DefaultComboBoxModel<>(Themes.values()));
		comboStylesScheme.setSelectedIndex(0);
		comboStylesScheme.setBackground(new Color(100, 149, 237));
		mainPanel.add(comboStylesScheme, "cell 9 3 2 1,growx");

		btntTextFormat = new JButton("Format");
		mainPanel.add(btntTextFormat, "cell 11 3 2 1,grow");

		btnRemoveSpaces = new JButton("");
		btnRemoveSpaces.setToolTipText(TextEditorTooltips.tooltipRemoveSpaces);
		btnRemoveSpaces.setIcon(new ImageIcon(IconsPathConfigurator.ICON_REMOVE_WHITE_LINE));
		mainPanel.add(btnRemoveSpaces, "cell 13 3,alignx center");

		btnBackgroundCol = new JButton("");
		btnBackgroundCol.setToolTipText(TextEditorTooltips.tooltipBackgroundColor);
		btnBackgroundCol.setIcon(new ImageIcon(IconsPathConfigurator.ICON_TEXT_BG_COLOR));
		mainPanel.add(btnBackgroundCol, "cell 14 3,alignx center");

		btnForegroundCol = new JButton("");
		btnForegroundCol.setToolTipText(TextEditorTooltips.tooltipForegroundTxt);
		btnForegroundCol.setIcon(new ImageIcon(IconsPathConfigurator.ICON_TEXT_COLOR));
		mainPanel.add(btnForegroundCol, "cell 15 3,alignx center");

		btnFontChooser = new JButton("");
		btnFontChooser.setToolTipText(TextEditorTooltips.tooltipFontChooser);
		btnFontChooser.setIcon(new ImageIcon(IconsPathConfigurator.ICON_TEXT_FONT));
		mainPanel.add(btnFontChooser, "cell 16 3,alignx center");

		btnCrypt = new JButton();
		btnCrypt.setHorizontalAlignment(SwingConstants.RIGHT);
		// btnCrypt.setHorizontalTextPosition(SwingConstants.RIGHT);
		btnCrypt.setIcon(new ImageIcon(IconsPathConfigurator.ICON_CRYPT));
		btnCrypt.setToolTipText(TextEditorTooltips.tooltipCrypt);
		btnCrypt.setForeground(SystemColor.inactiveCaption);
		mainPanel.add(btnCrypt, "cell 17 3,alignx center");

		btnDecrypt = new JButton("");
		btnDecrypt.setHorizontalTextPosition(SwingConstants.RIGHT);
		btnDecrypt.setIcon(new ImageIcon(IconsPathConfigurator.ICON_DECRYPT));
		btnDecrypt.setToolTipText(TextEditorTooltips.tooltipDecrypt);
		btnDecrypt.setForeground(SystemColor.inactiveCaption);
		mainPanel.add(btnDecrypt, "cell 18 3,alignx center");

		chckbxAesShowKey = new JCheckBox("Show key");
		chckbxAesShowKey.setForeground(Color.WHITE);
		mainPanel.add(chckbxAesShowKey, "cell 19 3 2 1,alignx center");

		btnTemplates = new JButton("Templates");
		btnTemplates.setToolTipText(TextEditorTooltips.tooltipTemplates);
		mainPanel.add(btnTemplates, "cell 21 3,growx");

		btnSpeak = new JButton("");
		btnSpeak.setToolTipText(TextEditorTooltips.tooltipSpeak);
		btnSpeak.setIcon(new ImageIcon(IconsPathConfigurator.ICON_SPEAKER));
		mainPanel.add(btnSpeak, "cell 22 3,alignx center");

		headerPanel = new JPanel();
		headerPanel.setForeground(SystemColor.menu);
		headerPanel.setBorder(new LineBorder(new Color(0, 0, 0), 2));
		headerPanel.setBackground(new Color(30,30,30));
		mainPanel.add(headerPanel, "cell 0 0 35 1,grow");
		headerPanel.setLayout(
				new MigLayout(GeneralConfig.DEBUG_GRAPHICS+" hidemode 2", "[160px,left][][][][][][][160px][][][50px:100px:120px][grow][][][][]", "[grow]"));

		lblVoiceSpeed = new JLabel("Voice rate");
		lblVoiceSpeed.setHorizontalAlignment(SwingConstants.RIGHT);
		headerPanel.add(lblVoiceSpeed, "cell 2 0,alignx right");
		lblVoiceSpeed.setForeground(SystemColor.control);

		sliderSpokenSpeed = new JSlider();
		headerPanel.add(sliderSpokenSpeed, "cell 3 0,growy");
		sliderSpokenSpeed.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
		sliderSpokenSpeed.setValue(currentConfiguration.getTextEditorOpt().getVoiceRate());
		sliderSpokenSpeed.setMaximum(400);

		checkBoxInlineTabs = new JCheckBox("Inline Tabs  ");
		checkBoxInlineTabs.setSelected(true);
		checkBoxInlineTabs.setToolTipText(TextEditorTooltips.tooltipInlineTabs);
		checkBoxInlineTabs.setCursor(GuiUtils.CURSOR_HAND);
		checkBoxInlineTabs.setForeground(SystemColor.control);
		checkBoxInlineTabs.setBackground(Color.DARK_GRAY);
		headerPanel.add(checkBoxInlineTabs, "cell 13 0,alignx center,aligny center");

		chkCutWordsTo = new JCheckBox("Adapt Text Width  ");
		chkCutWordsTo.setCursor(GuiUtils.CURSOR_HAND);
		headerPanel.add(chkCutWordsTo, "cell 14 0,alignx center,aligny center");
		chkCutWordsTo.setToolTipText(TextEditorTooltips.tooltipCutWordsTo);
		chkCutWordsTo.setSelected(false);
		chkCutWordsTo.setForeground(SystemColor.control);
		chkCutWordsTo.setBackground(Color.DARK_GRAY);

		chkFitmantainwords = new JCheckBox("Don't cut words");
		chkFitmantainwords.setCursor(GuiUtils.CURSOR_HAND);
		headerPanel.add(chkFitmantainwords, "cell 15 0,alignx right,aligny center");
		chkFitmantainwords.setToolTipText(TextEditorTooltips.tooltipFitMaintainWords);
		chkFitmantainwords.setForeground(SystemColor.control);
		chkFitmantainwords.setBackground(Color.DARK_GRAY);
		chkFitmantainwords.setEnabled(false);

		// DRAG 'N DROP IMPLEMENTATION FOR TABS		
		Runnable fileDropLoader = new Runnable() {
			@Override
			public void run() {

				// for each dragged file on tabPane load it in a new tab
				if (!alreadyLoading) {

					for (File draggedCurrent : mainTabList.loadedFilesList) {
						loadFile(draggedCurrent);
					}
				} // end if !already loading

				// at the end remove all temp files dragged reference
				mainTabList.emptyLoadedList();
			}
		};

		mainTabList = new FileDragNDropManager(fileDropLoader, null);
		mainTabList.addMouseWheelListener(new TabbedPaneMouseWheelListener());
		mainTabList.setBorder(new LineBorder(Color.BLACK, 2));
		mainTabList.setBackground(new Color(30, 30, 30));
		// mainTabList.setOpaque(true);

		btnReplaceEscapesSeq = new JButton("");
		btnReplaceEscapesSeq.setToolTipText(TextEditorTooltips.tooltipReplaceEscapes);
		btnReplaceEscapesSeq.setIcon(new ImageIcon(IconsPathConfigurator.ICON_CONVERT_ESCAPES));
		mainPanel.add(btnReplaceEscapesSeq, "cell 23 3,alignx center");

		btnFindText = new JButton("");
		btnFindText.setToolTipText(TextEditorTooltips.tooltipFindText);
		btnFindText.setIcon(new ImageIcon(IconsPathConfigurator.ICON_SEARCH));
		mainPanel.add(btnFindText, "cell 24 3,alignx center");

		btnReplaceText = new JButton("");
		btnReplaceText.setToolTipText(TextEditorTooltips.tooltipReplaceText);
		btnReplaceText.setIcon(new ImageIcon(IconsPathConfigurator.ICON_REPLACE_TXT));
		mainPanel.add(btnReplaceText, "cell 25 3");

		btnGoToLine = new JButton("");
		btnGoToLine.setToolTipText(TextEditorTooltips.tooltipGoToLine);
		btnGoToLine.setIcon(new ImageIcon(IconsPathConfigurator.ICON_GOTO_LINE));
		mainPanel.add(btnGoToLine, "cell 25 2");
						
		chckbxShowTabGrid = new JCheckBox("Show tab grid");
		chckbxShowTabGrid.setHorizontalAlignment(SwingConstants.LEFT);
		chckbxShowTabGrid.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		chckbxShowTabGrid.setToolTipText(TextEditorTooltips.tooltipShowTabGrid);
		chckbxShowTabGrid.setSelected(currentConfiguration.getTextEditorOpt().isTabLineVisible());
		chckbxShowTabGrid.setForeground(SystemColor.menu);
		chckbxShowTabGrid.setBackground(Color.DARK_GRAY);
		mainPanel.add(chckbxShowTabGrid, "cell 28 3 2 1");
				
		lblFontSize = new JLabel("Font size: ");
		lblFontSize.setHorizontalAlignment(SwingConstants.CENTER);
		lblFontSize.setForeground(SystemColor.menu);
		mainPanel.add(lblFontSize, "cell 30 3,alignx center");
		
		lblSIZE = new JLabel("");
		lblSIZE.setFont(new Font("SansSerif", Font.PLAIN, 10));
		lblSIZE.setForeground(SystemColor.window);
		mainPanel.add(lblSIZE, "cell 31 3,alignx center");
		lblSIZE.setText(String.valueOf(GeneralConfig.DEFAULT_FONT.getSize()) + " Px");

		mainTabList.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

		splitPaneMain = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPaneMain.setBackground(mainTabList.getBackground());
		leftSplitTreeFiles = new JTree(new FileTreeModel(null));
		mouseListenerTreeFilePanel = new TreeFileMouseListenerTxtEditor(leftSplitTreeFiles, this);
		leftSplitTreeFiles.setCellRenderer(new TreeCellRendererDark());
		leftSplitTreeFiles.setBackground(fileNavColorBckg);
		leftSplitTreeFiles.setForeground(Color.white);
		leftSplitPane = new JScrollPane(leftSplitTreeFiles);
		leftSplitPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		leftSplitPane.setBorder(new EmptyBorder(0, 0, 0, 0));
		leftSplitPane.setPreferredSize(new Dimension(200, 500));
		leftSplitContainer = new JPanel();
		leftSplitContainer.setBackground(fileNavColorBckg);
		leftSplitContainer.setForeground(Color.GRAY);
		leftSplitContainer.setLayout(new MigLayout("", "[30px:40px][40px][2px:4px,grow][18px:20px:20px][18px:20px:20px][18px:20px:20px]", "[:25px:25px][2px:2px:2px][90%,grow][2px:2px:2px][:20px:20px]"));
		 
		lblLockNavigator = new JLabel("File Navigator");
		lblLockNavigator.setFont(new Font("SansSerif", Font.BOLD, 12));
		lblLockNavigator.setHorizontalAlignment(SwingConstants.RIGHT);
		lblLockNavigator.setForeground(SystemColor.menu);
		leftSplitContainer.add(lblLockNavigator, "cell 0 0 2 1,alignx left");
		
		toggleButtonFileTreeNewWorkspace = new JButton("");
		GuiUtils.makeTransparentJButton(toggleButtonFileTreeNewWorkspace);
		toggleButtonFileTreeNewWorkspace.setMaximumSize(new Dimension(20, 12));
		toggleButtonFileTreeNewWorkspace.setToolTipText(TextEditorTooltips.tooltipWorkspaceNew);
		toggleButtonFileTreeNewWorkspace.setIcon(new ImageIcon(IconsPathConfigurator.ICON_JOB_SEARCH));
		leftSplitContainer.add(toggleButtonFileTreeNewWorkspace, "cell 3 0");
		
		toggleButtonFileTreeWorkspace = new JButton("");
		GuiUtils.makeTransparentJButton(toggleButtonFileTreeWorkspace);
		File workspace = new File(currentConfiguration.getTextEditorOpt().defaultWorkspacePath);
		toggleButtonFileTreeWorkspace.setEnabled(workspace != null && !workspace.getName().equals("") && workspace.exists());
		toggleButtonFileTreeWorkspace.setMaximumSize(new Dimension(20, 12));
		toggleButtonFileTreeWorkspace.setToolTipText(TextEditorTooltips.tooltipWorkspaceToggle);
		toggleButtonFileTreeWorkspace.setIcon(new ImageIcon(IconsPathConfigurator.ICON_JOB_ON));
		leftSplitContainer.add(toggleButtonFileTreeWorkspace, "cell 4 0");
		
		toggleButtonFileTreeLocked = new JButton("");
		GuiUtils.makeTransparentJButton(toggleButtonFileTreeLocked);
		toggleButtonFileTreeLocked.setMaximumSize(new Dimension(20, 12));
		toggleButtonFileTreeLocked.setToolTipText(TextEditorTooltips.tooltipFileViewLock);
		toggleButtonFileTreeLocked.setIcon(new ImageIcon(IconsPathConfigurator.ICON_UNPIN));
		leftSplitContainer.add(toggleButtonFileTreeLocked, "cell 5 0,alignx center,growy");
		
		separator_NO_HIDE = new JSeparator();
		separator_NO_HIDE.setSize(new Dimension(50, 2));
		separator_NO_HIDE.setMinimumSize(new Dimension(50, 2));
		separator_NO_HIDE.setBackground(Color.GRAY);
		separator_NO_HIDE.setOpaque(true);
		separator_NO_HIDE.setForeground(Color.LIGHT_GRAY);
		leftSplitContainer.add(separator_NO_HIDE, "cell 0 1 6 1,growx");
		leftSplitContainer.add(leftSplitPane, "cell 0 2 6 1,grow");

		mainPanel.add(splitPaneMain, "cell 0 5 35 2,grow");
		splitPaneMain.setRightComponent(mainTabList);
		splitPaneMain.setLeftComponent(leftSplitContainer);
		
		separator_NO_HIDE_2 = new JSeparator();
		separator_NO_HIDE_2.setSize(new Dimension(50, 2));
		separator_NO_HIDE_2.setPreferredSize(new Dimension(50, 2));
		separator_NO_HIDE_2.setOpaque(true);
		separator_NO_HIDE_2.setForeground(Color.GRAY);
		separator_NO_HIDE_2.setBackground(Color.GRAY);
		leftSplitContainer.add(separator_NO_HIDE_2, "cell 0 3 6 1,growx");
		
		lblCurrentDir = new JLabel(ROOT_STANDARD_LBL);
		lblCurrentDir.setHorizontalTextPosition(SwingConstants.LEFT);
		lblCurrentDir.setMaximumSize(new Dimension(100, 16));
		lblCurrentDir.setMinimumSize(new Dimension(75, 16));
		lblCurrentDir.setHorizontalAlignment(SwingConstants.LEFT);
		lblCurrentDir.setForeground(SystemColor.menu);
		lblCurrentDir.setFont(new Font("SansSerif", Font.PLAIN, 12));
		leftSplitContainer.add(lblCurrentDir, "cell 0 4 2 1,alignx left");
		
		dynLblCurrentDir = new JLabel("");
		dynLblCurrentDir.setHorizontalTextPosition(SwingConstants.RIGHT);
		dynLblCurrentDir.setHorizontalAlignment(SwingConstants.RIGHT);
		dynLblCurrentDir.setForeground(SystemColor.menu);
		dynLblCurrentDir.setFont(new Font("SansSerif", Font.BOLD, 12));
		leftSplitContainer.add(dynLblCurrentDir, "cell 2 4 4 1,alignx right");
		splitPaneMain.setContinuousLayout(true);

		separator_2 = new JSeparator();
		separator_2.setBackground(Color.GRAY);
		mainPanel.add(separator_2, "cell 0 4 35 1,growx");

		TextEditorUtils.addTab(null, true); // comment for editing in windowuilder
		TextEditorUtils.isFirstTabAdded = true;

		sliderFontSize = new JSlider(4, 100);
		sliderFontSize.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
		sliderFontSize.setForeground(SystemColor.activeCaptionBorder);
		sliderFontSize.setBackground(UIManager.getColor("Button.shadow"));
		sliderFontSize.setValue(currentConfiguration.getTextEditorOpt().getFont().getSize());
		mainPanel.add(sliderFontSize, "cell 32 3 3 1,growx");

		separator_1 = new JSeparator();
		mainPanel.add(separator_1, "cell 0 1 35 1,growx");

		txtAesKey = new JPasswordField();
		txtAesKey.setHorizontalAlignment(SwingConstants.CENTER);
		txtAesKey.setEchoChar((char) 0);
		txtAesKey.setText(AES_KEY_PLACEHOLDER);
		mainPanel.add(txtAesKey, "cell 17 2 4 1,growx");
		txtAesKey.setColumns(10);
		txtAesKey.setFont(GeneralConfig.BUTTON_DEFAULT_FONT);
		txtAesKey.setEchoChar(DEFAULT_PSW_CHAR);
		AesCipher.check(txtAesKey);

		lblTextEditor = new JLabel("Text Editor");
		headerPanel.add(lblTextEditor, "cell 0 0,alignx left");
		lblTextEditor.setHorizontalAlignment(SwingConstants.CENTER);
		lblTextEditor.setForeground(SystemColor.text);
		lblTextEditor.setFont(new Font("Segoe UI", Font.BOLD, 15));
		lblTextEditor.setBackground(SystemColor.activeCaptionBorder);

		slider_margin = new JSlider(0, 100, 0);
		slider_margin.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
		slider_margin.setBackground(UIManager.getColor("Button.shadow"));
		mainPanel.add(slider_margin, "cell 32 2 3 1,growx");

		btnLoadTextFile = new JButton("");
		btnLoadTextFile.setToolTipText(TextEditorTooltips.tooltipLoadTextFile);
		btnLoadTextFile.setForeground(SystemColor.menuText);
		btnLoadTextFile.setIcon(new ImageIcon(IconsPathConfigurator.ICON_FILE_LOAD));
		mainPanel.add(btnLoadTextFile, "cell 0 2,grow");

		footerPanel = new JPanel();
		footerPanel.setBorder(new LineBorder(new Color(0, 0, 0), 2));
		footerPanel.setBackground(Color.GRAY);
		mainPanel.add(footerPanel, "cell 0 7 35 1,grow");
		footerPanel.setLayout(new MigLayout(GeneralConfig.DEBUG_GRAPHICS + "fill,insets 3", "[][200px,grow][grow][][:60px:120px][][:60px:120px][][:60px:120px][][:20px:120px][][:20px:120px][][:60px:120px][grow][]", "[fill]"));

		lblCurrentFileLBL = new JLabel("   Current file:");
		footerPanel.add(lblCurrentFileLBL, "cell 0 0,growy");
		lblCurrentFileLBL.setForeground(SystemColor.controlHighlight);
		lblCurrentFileLBL.setFont(GeneralConfig.BUTTON_DEFAULT_FONT);

		lblCurrentFile = new JTextField("\\*.txt");
		lblCurrentFile.setMinimumSize(new Dimension(280, 22));
		lblCurrentFile.setBackground(Color.DARK_GRAY);
		lblCurrentFile.setOpaque(true);
		footerPanel.add(lblCurrentFile, "cell 1 0,growx");
		lblCurrentFile.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblCurrentFile.setForeground(SystemColor.activeCaption);

		rdbtnShowCompletePath = new JRadioButton("Complete Path");
		rdbtnShowCompletePath.setFont(new Font("SansSerif", Font.PLAIN, 11));
		footerPanel.add(rdbtnShowCompletePath, "cell 2 0");
		rdbtnShowCompletePath.setSelected(true);

		labelCharset = new JLabel("Charset:");
		labelCharset.setForeground(SystemColor.controlHighlight);
		labelCharset.setFont(new Font("Segoe UI", Font.BOLD, 10));
		footerPanel.add(labelCharset, "cell 3 0");

		dynLabelCharset = new JLabel("");
		dynLabelCharset.setForeground(SystemColor.inactiveCaptionBorder);
		dynLabelCharset.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		footerPanel.add(dynLabelCharset, "cell 4 0");

		labelTotLines = new JLabel("Tot Lines:");
		labelTotLines.setForeground(SystemColor.controlHighlight);
		labelTotLines.setFont(new Font("Segoe UI", Font.BOLD, 10));
		footerPanel.add(labelTotLines, "cell 5 0,alignx right,aligny center");

		dynLblTotLines = new JLabel("");
		dynLblTotLines.setForeground(SystemColor.inactiveCaptionBorder);
		dynLblTotLines.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		footerPanel.add(dynLblTotLines, "cell 6 0");

		labelTotalChars = new JLabel("Tot. Chars:");
		labelTotalChars.setForeground(SystemColor.controlHighlight);
		labelTotalChars.setFont(new Font("Segoe UI", Font.BOLD, 10));
		footerPanel.add(labelTotalChars, "cell 7 0,alignx right");

		dynLblTotChars = new JLabel("");
		dynLblTotChars.setForeground(SystemColor.inactiveCaptionBorder);
		dynLblTotChars.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		footerPanel.add(dynLblTotChars, "cell 8 0");

		lblLine = new JLabel("Sel. Line:");
		lblLine.setForeground(SystemColor.controlHighlight);
		lblLine.setFont(new Font("Segoe UI", Font.BOLD, 10));
		footerPanel.add(lblLine, "cell 9 0");

		dynLblCurrLine = new JLabel("");
		dynLblCurrLine.setForeground(SystemColor.inactiveCaptionBorder);
		dynLblCurrLine.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		footerPanel.add(dynLblCurrLine, "cell 10 0");

		lblColumn = new JLabel("Sel. Column:");
		lblColumn.setForeground(SystemColor.controlHighlight);
		lblColumn.setFont(new Font("Segoe UI", Font.BOLD, 10));
		footerPanel.add(lblColumn, "cell 11 0");

		dynLblColumn = new JLabel("");
		dynLblColumn.setForeground(SystemColor.inactiveCaptionBorder);
		dynLblColumn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		footerPanel.add(dynLblColumn, "cell 12 0");

		labelSelectionLength = new JLabel("Sel. length:");
		labelSelectionLength.setForeground(SystemColor.controlHighlight);
		labelSelectionLength.setFont(new Font("Segoe UI", Font.BOLD, 10));
		footerPanel.add(labelSelectionLength, "cell 13 0,alignx right");

		dynLblSelLength = new JLabel("");
		dynLblSelLength.setForeground(SystemColor.inactiveCaptionBorder);
		dynLblSelLength.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		footerPanel.add(dynLblSelLength, "cell 14 0");

		dynLblSearchEsit = new JLabel("");
		dynLblSearchEsit.setHorizontalAlignment(SwingConstants.CENTER);
		dynLblSearchEsit.setForeground(new Color(255, 222, 173));
		dynLblSearchEsit.setFont(new Font("Segoe UI", Font.BOLD, 13));
		dynLblSearchEsit.setBackground(Color.WHITE);
		footerPanel.add(dynLblSearchEsit, "cell 15 0,alignx center");

		lblAdvice = new JLabel("");
		footerPanel.add(lblAdvice, "cell 16 0,alignx right,aligny center");
		lblAdvice.setBackground(SystemColor.info);
		lblAdvice.setFont(new Font("Segoe UI", Font.BOLD, 12));
		lblAdvice.setForeground(new Color(176, 224, 230));
		lblAdvice.setHorizontalAlignment(SwingConstants.CENTER);
		lblAdvice.setText(" [Current output: .txt]");

		addHandlers();

		// add custom keyboard listener
		shortcutsListener = getKeyEventHandler();
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(shortcutsListener);

		startAesKeyCheckThread();

		try {
			TextEditorUtils.applyStyleToButtons(currentTextEditor);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			logger.warn("Buttons styling gone wrong: ", e);
		}
		
		initGuiFromConfig();
		
		loadOpenedFilesAtStart();

		TextEditorUtils.resizeFrameToDefault(mainApplication);

		TextEditorUtils.refreshTree(loadedTabObj.loadedFile, false);
		
		loadedTabObj.textArea.requestFocusInWindow();
		
		mainTabList.setUI(new CustomTabbedPaneUI_v1(null, null, null, mainTabList, TabUtils.TAB_BUTTON_CLOSE_ORDER));

		// set default placeholder visible
		txtAesKey.setEchoChar((char) 0);

		startRefreshLabelCurrFIleThread();
		
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				loadedTabObj.textArea.setFont(currentConfiguration.getTextEditorOpt().getFont());
				loadedTabObj.textArea.revalidate();
				loadedTabObj.textArea.repaint();
			}
		});

		logger.debug("### TEXT WRITER module started");
	}

	private void startAesKeyCheckThread() {
		// this thread will check every 0.2s the value contained in the textfield
		new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					AesCipher.check(txtAesKey);
					try {
						Thread.sleep(400);
					} catch (InterruptedException e) {
						logger.error("ERROR in AES checker!", e);
					}
				}
			}
		}).start();
	}

	/**
	 * This method adds handlers to action-related Components (Use this after
	 * initialization)
	 */
	private void addHandlers() {

		// COLORS DEFAULT DEFINITIONS
		final Font DEFAULT = GeneralConfig.DEFAULT_FONT;
		final Color DEFAULT_BCKG_COL = currentConfiguration.getTextEditorOpt().backCol;
		final Color DEFAULT_FORE_COL = currentConfiguration.getTextEditorOpt().foreCol;
		
		// TAB SIZE SELECTOR
		tabSizeSpinner.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				loadedTabObj.textArea.setTabSize((int)tabSizeSpinner.getValue());
				currentConfiguration.getTextEditorOpt().setTabSize((int)tabSizeSpinner.getValue());
			}
		});
		
		// FILE TREE - LOCK/UNLOCK TREE SYNC
		toggleButtonFileTreeLocked.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!currentConfiguration.getTextEditorOpt().treeLocked) {
					toggleButtonFileTreeLocked.setIcon(new ImageIcon(IconsPathConfigurator.ICON_PIN));
					toggleButtonFileTreeLocked.setBackground(new Color(30,30,30));
					currentConfiguration.getTextEditorOpt().setTreeLocked(true);
					// SYNC config file
					currentConfiguration.writeSingleValue(INItializer.TEXT_EDITOR_OPT, "isTreeLocked", true);
				}else {
					toggleButtonFileTreeLocked.setIcon(new ImageIcon(IconsPathConfigurator.ICON_UNPIN));
					toggleButtonFileTreeLocked.setBackground(new Color(30,30,30));
					TextEditorUtils.refreshTree(loadedTabObj.loadedFile, true);
					currentConfiguration.getTextEditorOpt().setTreeLocked(false);
					// SYNC config file
					currentConfiguration.writeSingleValue(INItializer.TEXT_EDITOR_OPT, "isTreeLocked", false);
				}
			}
		});
		
		// FILE TREE - WORKSPACE ON/OFF
		toggleButtonFileTreeWorkspace.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// pre disable file tree lock
				boolean toggleTreeLockToReset = false;
				if(currentConfiguration.getTextEditorOpt().treeLocked) {
					toggleButtonFileTreeLocked.doClick();
					toggleTreeLockToReset = true;
				}
				
				// INVERT CURRENT TEXT EDITOR VALUES ON CLICK - AND ICONS ACCORING TO
				if (!currentConfiguration.getTextEditorOpt().workspaceViewSelected) {
					toggleButtonFileTreeWorkspace.setIcon(new ImageIcon(IconsPathConfigurator.ICON_JOB_ON));
					currentConfiguration.getTextEditorOpt().setWorkspaceViewSelected(true);
					// SYNC config file
					currentConfiguration.writeSingleValue(INItializer.TEXT_EDITOR_OPT, "workspaceViewSelected", true);

				}else {
					toggleButtonFileTreeWorkspace.setIcon(new ImageIcon(IconsPathConfigurator.ICON_JOB_OFF));
					currentConfiguration.getTextEditorOpt().setWorkspaceViewSelected(false);
					// SYNC config file
					currentConfiguration.writeSingleValue(INItializer.TEXT_EDITOR_OPT, "workspaceViewSelected", false);
				}
				
				// FILE TREE LOCK SYNC
				TextEditorUtils.refreshTree(loadedTabObj.loadedFile, false);
				
				if(toggleTreeLockToReset) {
					toggleButtonFileTreeLocked.doClick();
				}
				
			}
		});
		
		// FILE TREE - WORKSPACE ON/OFF
		toggleButtonFileTreeNewWorkspace.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {

				File directory = GenericFileChooser.directoRead(mainApplication.getMainFrame(), "Choose a workspace folder to load");
				if(directory != null && directory.exists()) {
					
					try {
						currentConfiguration.getTextEditorOpt().defaultWorkspacePath = directory.getCanonicalPath();
						currentConfiguration.writeSingleValue(INItializer.TEXT_EDITOR_OPT, "defaultWorspaceLocation", directory.getCanonicalPath());
						logger.info("Loaded new workspace: "+directory.getAbsolutePath());
						
						// to enable first time if it was disabled by default protection dir null
						toggleButtonFileTreeWorkspace.setEnabled(true);
						
					} catch (IOException e1) {
						currentConfiguration.getTextEditorOpt().defaultWorkspacePath = directory.getAbsolutePath();
						logger.warn("Loaded new workspace [Error parsing canonical path, using asolute]: "+directory.getAbsolutePath(), e1);
					}
					// FILE TREE LOCK SYNC
					TextEditorUtils.refreshTree(loadedTabObj.loadedFile, false);
				}
			}
		});

		// PROPERTIES - CURRENT FILE
		btnPropertiesPanel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (propertiesFrame == null) {
					loadedTabObj.textArea.setCursor(GuiUtils.CURSOR_WAIT);
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							try {
								if (loadedTabObj != null && loadedTabObj.loadedFile != null
										&& loadedTabObj.loadedFile.exists()) {
									propertiesFrame = new FilePropertiesFrame(loadedTabObj.loadedFile, true);
									propertiesFrame.setAlwaysOnTop(true);
									propertiesFrame.setTitle(loadedTabObj.loadedFile.getAbsolutePath());
									propertiesFrame.setVisible(true);
									GuiUtils.centerComponent(propertiesFrame, 650, 240);
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

		// divisor resize
		splitPaneMain.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (btnToggleFileView.isSelected() && loadedTabObj.loadedFile != null && loadedTabObj.loadedFile.exists()) {
					currentConfiguration.getTextEditorOpt().setDividerFileTreePosX(splitPaneMain.getDividerLocation());
				}
			}
		});

		// FILE VIEW
		btnToggleFileView.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				TextEditorUtils.refreshTree(loadedTabObj.loadedFile, false);
				if (btnToggleFileView.isSelected()) {
					btnToggleFileView.setBackground(new Color(120, 0, 0));
					splitPaneMain.setDividerLocation(currentConfiguration.getTextEditorOpt().dividerFileTreePosX);
					currentConfiguration.getTextEditorOpt().fileTreeActive = true;
				} else {
					splitPaneMain.setDividerLocation(0);
					btnToggleFileView.setBackground(SystemColor.inactiveCaption);
					currentConfiguration.getTextEditorOpt().fileTreeActive = false;
				}
			}
		});

		// FOOTER UI RADIOBOX COMPLETE PATH (current file)
		rdbtnShowCompletePath.setForeground(Color.LIGHT_GRAY);
		rdbtnShowCompletePath.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						if (rdbtnShowCompletePath.isSelected()) {
							rdbtnShowCompletePath.setForeground(Color.WHITE);
						} else {
							rdbtnShowCompletePath.setForeground(Color.LIGHT_GRAY);
						}
					}
				});
			}
		});

		// PLACEHOLDER AES KEY MANAGER
		txtAesKey.addMouseListener(new MouseListener() {
			public void mouseReleased(MouseEvent e) {
			}

			public void mousePressed(MouseEvent e) {
				if (txtAesKey.getPassword().length == AES_KEY_PLACEHOLDER.length()) {
					String digits = Arrays.toString(txtAesKey.getPassword());
					digits = digits.replace(", ", "");
					if (digits.substring(1, digits.length() - 1).equals(AES_KEY_PLACEHOLDER)) {
						txtAesKey.setText("");
						if (!chckbxAesShowKey.isSelected()) {
							txtAesKey.setEchoChar(DEFAULT_PSW_CHAR);
						}
					}
				}
			}

			public void mouseExited(MouseEvent e) {
			}

			public void mouseEntered(MouseEvent e) {
			}

			public void mouseClicked(MouseEvent e) {
			}
		});

		// resizeListener tabListContainer
		parentFrame.addComponentListener(new ComponentListener() {
			public void componentShown(ComponentEvent e) {
				TextEditorUtils.resizeFrameToDefault(mainApplication);
			}

			public void componentResized(ComponentEvent e) {
				TextEditorUtils.resizeFrameToDefault(mainApplication);
			}

			public void componentMoved(ComponentEvent e) {
				TextEditorUtils.resizeFrameToDefault(mainApplication);
			}

			public void componentHidden(ComponentEvent e) {
			}
		});

		// OPEN IN SCREEPT CRYPTER
		btnOpenInScreepT.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				CrypterPanel cipherPanel = mainApplication.getPanelCrypter();

				// current file not saved
				if (loadedTabObj.unsaved) {
					String currFileName = (loadedTabObj.loadedFile != null) ? loadedTabObj.loadedFile.getName()
							: lblCurrentFile.getText();
					int choice = JOptionPane.showConfirmDialog(currentTextEditor,
							currFileName + " is not saved, do you want to save it before to crypt?");
					if (choice == JOptionPane.OK_OPTION) {
						btnSaveTextFile.doClick();
					} else if (choice == JOptionPane.CANCEL_OPTION) {
						return;
					}
				}

				// current file not exists or null
				if (loadedTabObj.loadedFile == null || !loadedTabObj.loadedFile.exists()) {
					return;
				}

				if (cipherPanel != null) {
					int openedFilesCrypter = cipherPanel.getPanelVDragNDropPane().getTreeNodes().length;
					if (openedFilesCrypter != 0) {
						String msg = "There are " + openedFilesCrypter
								+ " loaded files in ScreepT: do you want to remove from loaded list?";
						int choice = JOptionPane.showConfirmDialog(currentTextEditor, msg);
						if (choice == JOptionPane.OK_OPTION) {
							cipherPanel.getPanelVDragNDropPane().removeAll();
						} else if (choice == JOptionPane.CANCEL_OPTION) {
							return;
						}
					}
				}

				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						mainApplication.getMntmCrypter().doClick();
						CrypterPanel cipherPanel = mainApplication.getPanelCrypter();
						cipherPanel.addNewSourceFile(loadedTabObj.loadedFile);
					}
				});
			}
		});

		// OPEN CURRENT IN SYSTEM EXPLORER
		btnViewInExplorer.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new Thread(new Runnable() {
					public void run() {
						if (TextEditorShortcutListener.flagCtrlPressed) {
							SysUtils.cdIntoCommandLine(loadedTabObj.loadedFile.getParentFile());
						} else {
							GuiUtils.openInFileSystem(loadedTabObj.loadedFile.getParentFile());
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
				shortcutsListener.flagHoverBtnViewExpCmd = false;
				shortcutsListener.doControlActions();
			}

			public void mouseEntered(MouseEvent e) {
				shortcutsListener.flagHoverBtnViewExpCmd = true;
				shortcutsListener.doControlActions();
			}

			public void mouseClicked(MouseEvent e) {
			}
		});

		// OPEN TEMPLATES PANEL
		btnReplaceEscapesSeq.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!EscapeReplacerPanel.active) {
					escapeReplacerPanel = new EscapeReplacerPanel(mainApplication.getMainFrame(), loadedTabObj.textArea, btnTemplates);
					escapeReplacerPanel.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					escapeReplacerPanel.setBounds(750, 140, 500, 210);
					escapeReplacerPanel.setAlwaysOnTop(true);
					escapeReplacerPanel.setVisible(true);
					escapeReplacerPanel.setTitle("Escape<->Special converter - " + MainGUI.APPLICATION_NAME);
					btnTemplates.setEnabled(false);
					GuiUtils.changeFrameStateLaterUnchecked(mainApplication.getMainFrame(), escapeReplacerPanel, JFrame.MAXIMIZED_BOTH);
				}else{
					escapeReplacerPanel.dispatchEvent(new WindowEvent(escapeReplacerPanel, WindowEvent.WINDOW_CLOSING));
				}
			}
		});
		
		// CALCULATOR OPENER
		btnCalculator.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (calculatorFrame == null || !CalculatorMainApp.active) {
					calculatorMainThread = new Thread (new Runnable() {
						public void run() {
							calculatorFrame = new CalculatorMainApp();
							CalculatorMainApp.getCalculatorGuiSingleton().gui.setAlwaysOnTop(true);
							
						}
					});
					calculatorMainThread.start();
				}
			}
		});

		// IMAGE PARSE INJECTOR
		btnParseImageText.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				if (imageParserBox == null || !ImageSelector2TextFrame.active) {
					imageParserBox = new ImageSelector2TextFrame(parentFrame, loadedTabObj.textArea,
							MainGUI.APPLICATION_NAME + " - Image2Text");
					imageParserBox.setVisible(true);
					GuiUtils.changeFrameStateLaterUnchecked(mainApplication.getMainFrame(), imageParserBox, JFrame.MAXIMIZED_BOTH);
				} else {
					imageParserBox.dispatchEvent(new WindowEvent(imageParserBox, WindowEvent.WINDOW_CLOSING));
				}
			}
		});

		// PDF PARSER INJECTOR
		btnParsePdfText.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (pdfParserBox == null || !PdfSelector2TextFrame.active) {
					pdfParserBox = new PdfSelector2TextFrame(parentFrame, loadedTabObj.textArea,
							MainGUI.APPLICATION_NAME + " - Pdf2Text");
					pdfParserBox.setVisible(true);
					GuiUtils.changeFrameStateLaterUnchecked(mainApplication.getMainFrame(), pdfParserBox, JFrame.MAXIMIZED_BOTH);
				} else {
					pdfParserBox.dispatchEvent(new WindowEvent(pdfParserBox, WindowEvent.WINDOW_CLOSING));
				}
			}
		});

		// SEARCH TEXT
		btnFindText.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (searchBox == null || !searchBox.isVisible()) {
					searchBox = TextAreaDialogs.getSearchDialog(parentFrame, loadedTabObj.textArea, null, currentTextEditor,  "successAdvice", "errorAdvice");
					GuiUtils.setFrameIcon(IconsPathConfigurator.ICON_SEARCH, searchBox);
					GuiUtils.changeFrameStateLaterUnchecked(mainApplication.getMainFrame(), searchBox, JFrame.MAXIMIZED_BOTH);
				} else {
					searchBox.dispatchEvent(new WindowEvent(searchBox, WindowEvent.WINDOW_CLOSING));
				}
			}
		});

		// GOTO LINE
		btnGoToLine.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (goToLineDialog == null || !goToLineDialog.isActive()) {
					goToLineDialog = TextAreaDialogs.getGoToLineDialog(parentFrame, loadedTabObj.textArea);
				} else {
					goToLineDialog.toFront();
					goToLineDialog.requestFocus();
					goToLineDialog.dispatchEvent(new WindowEvent(goToLineDialog, WindowEvent.WINDOW_CLOSING));
				}
				GuiUtils.changeFrameStateLaterUnchecked(mainApplication.getMainFrame(), goToLineDialog, JFrame.MAXIMIZED_BOTH);
			}
		});

		// REPLACE TEXT
		btnReplaceText.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (replaceDialog == null || !replaceDialog.isVisible()) {
					replaceDialog = TextAreaDialogs.getReplaceDialog(parentFrame, loadedTabObj.textArea, null, currentTextEditor,  "successAdvice", "errorAdvice");
					GuiUtils.setFrameIcon(IconsPathConfigurator.ICON_REPLACE_TXT, replaceDialog);
					GuiUtils.changeFrameStateLaterUnchecked(mainApplication.getMainFrame(), replaceDialog, JFrame.MAXIMIZED_BOTH);
				} else {
					replaceDialog.toFront();
					replaceDialog.requestFocus();
					replaceDialog.dispatchEvent(new WindowEvent(replaceDialog, WindowEvent.WINDOW_CLOSING));
				}
			}
		});

		// SHOW/HIDE PASSWORD DISPLAYED
		chckbxAesShowKey.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (chckbxAesShowKey.isSelected()) {
					txtAesKey.setEchoChar((char) 0);
				} else {
					txtAesKey.setEchoChar(DEFAULT_PSW_CHAR);
				}
			}
		});

		// CHANGE TAB VISUALIZATION MODE
		checkBoxInlineTabs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (checkBoxInlineTabs.isSelected()) {
					currentConfiguration.getTextEditorOpt().inlinedTabs = true;
					mainTabList.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
				} else {
					currentConfiguration.getTextEditorOpt().inlinedTabs = false;
					mainTabList.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
				}
			}
		});
		
		// VIEW/HIDE tab grids
		chckbxShowTabGrid.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (chckbxShowTabGrid.isSelected()) {
					currentConfiguration.getTextEditorOpt().setTabLineVisible(true);
					loadedTabObj.textArea.setPaintTabLines(true);
				} else {
					currentConfiguration.getTextEditorOpt().setTabLineVisible(false);
					loadedTabObj.textArea.setPaintTabLines(false);
				}
			}
		});

		// CRYPT TEXT
		ActionListener cryptListener = new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				if (!isCrypterOn) {
					loadedTabObj.textArea.setCursor(GuiUtils.CURSOR_WAIT);
					btnCrypt.setCursor(GuiUtils.CURSOR_WAIT);
					isCrypterOn = true;
					AesCipher.check(txtAesKey);
					String encoded = "";
					if (txtAesKey.getPassword().length != 0) {
						try {
							// loadedTabObj.textArea.getDocument().removeDocumentListener(textAreaChangedListener);
							AesCipher crypter = new AesCipher(txtAesKey.getPassword());
							String displayedSource = loadedTabObj.textArea.getText();
							encoded = crypter.encryptLines(displayedSource);
							// if lentgh more than admitted suggest to use screept
							loadedTabObj.textArea.setText("");
							loadedTabObj.textArea.append(encoded);

						} catch (Throwable e) {
							JOptionPane.showMessageDialog(null, "Wrong key, retry! :(", "Error",
									JOptionPane.ERROR_MESSAGE);
							logger.error("Exception happened!", e);
						} finally {
							// textArea_1.getDocument().addDocumentListener(textAreaChangedListener);
							loadedTabObj.unsaved = true;
							updateSavedLabelNTab(null);
						}
					} else {
						JOptionPane.showMessageDialog(currentTextEditor, "Please enter a password, empty key text!",
								"Empty password", JOptionPane.WARNING_MESSAGE);
					}
					isCrypterOn = false;
					loadedTabObj.textArea.setCursor(GuiUtils.CURSOR_TEXT);
					btnCrypt.setCursor(GuiUtils.CURSOR_HAND);
				}
			}
		};
		btnCrypt.addActionListener(cryptListener);

		// DECRIPT TEXT
		btnDecrypt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				if (!isCrypterOn) {
					loadedTabObj.textArea.setCursor(GuiUtils.CURSOR_WAIT);
					btnDecrypt.setCursor(GuiUtils.CURSOR_WAIT);
					isCrypterOn = true;
					AesCipher.check(txtAesKey);
					String decoded = "";
					if (txtAesKey.getPassword().length != 0) {
						try {
							AesCipher decrypter = new AesCipher(txtAesKey.getPassword());
							String displayedSource = loadedTabObj.textArea.getText();
							decoded = decrypter.decryptLines(displayedSource);
							loadedTabObj.textArea.setText("");
						} catch (Exception e) {
							JOptionPane.showMessageDialog(null, "Wrong key, retry! :(", "Error",
									JOptionPane.ERROR_MESSAGE);
							logger.error("Exception happened!", e);
						} finally {
							// textArea_1.getDocument().removeDocumentListener(textAreaChangedListener);
							loadedTabObj.textArea.append(decoded);
							// textArea_1.getDocument().addDocumentListener(textAreaChangedListener);
						}
					} else {
						JOptionPane.showMessageDialog(currentTextEditor, "Please enter a password, empty key text!",
								"Empty password", JOptionPane.WARNING_MESSAGE);
					}
					isCrypterOn = false;
					loadedTabObj.textArea.setCursor(GuiUtils.CURSOR_TEXT);
					btnDecrypt.setCursor(GuiUtils.CURSOR_HAND);
				}
			}
		});

		// SET DEFAULT TEXT AREA VALUES
		btnDefaultOpt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loadedTabObj.textArea.setFont(DEFAULT);

				RsyntaxUtils.setBackground(loadedTabObj.textArea, DEFAULT_BCKG_COL);
				loadedTabObj.textArea.setBackground(DEFAULT_BCKG_COL);
				RsyntaxUtils.setForeground(loadedTabObj.textArea, DEFAULT_FORE_COL);
				loadedTabObj.textArea.setForeground(DEFAULT_FORE_COL);

				slider_margin.setValue(10);
				sliderFontSize.setValue(loadedTabObj.textArea.getFont().getSize());
				lblSIZE.setText(String.valueOf(loadedTabObj.textArea.getFont().getSize()) + " Px");

				defaultTxtConfig(DEFAULT, DEFAULT_BCKG_COL, DEFAULT_FORE_COL, 10);
			}
		});

		// BACKGROUND TEXT COLOR
		btnBackgroundCol.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				btnBackgroundCol.setEnabled(false);
				Color esit = new ColorDialogUtils().askForColor(loadedTabObj.textArea, false);
				if (esit != null) {
					currentConfiguration.getTextEditorOpt().setBackCol(esit);
					RsyntaxUtils.setBackground(loadedTabObj.textArea, esit);
				}
				btnBackgroundCol.setEnabled(true);
			}
		});

		// FOREGROUND TEXT COLOR
		btnForegroundCol.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				btnForegroundCol.setEnabled(false);
				Color esit = new ColorDialogUtils().askForColor(loadedTabObj.textArea, true);
				if (esit != null) {
					currentConfiguration.getTextEditorOpt().setForeCol(esit);
					RsyntaxUtils.setForeground(loadedTabObj.textArea, esit);
					loadedTabObj.textArea.setForeground(esit);
				}
				btnForegroundCol.setEnabled(true);
			}
		});

		// FONT CHOOSER PANEL
		btnFontChooser.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

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
				if (loadedTabObj.textArea.getForeground() == Color.BLACK
						|| loadedTabObj.textArea.getBackground() == DEFAULT_BCKG_COL
						|| loadedTabObj.textArea.getBackground() == Color.WHITE) {
					RsyntaxUtils.setBackground(loadedTabObj.textArea, Color.BLACK);
					loadedTabObj.textArea.setBackground(Color.BLACK);
					RsyntaxUtils.setForeground(loadedTabObj.textArea, new Color(235, 235, 235));
					loadedTabObj.textArea.setForeground(new Color(235, 235, 235));
					btnDark.setIcon(IconsPathConfigurator.IMG_ICON_LIGHT_ON);
				} else {
					RsyntaxUtils.setBackground(loadedTabObj.textArea, SystemColor.controlHighlight);
					loadedTabObj.textArea.setBackground(SystemColor.controlHighlight);
					RsyntaxUtils.setForeground(loadedTabObj.textArea, Color.BLACK);
					loadedTabObj.textArea.setForeground(Color.BLACK);
					btnDark.setIcon(IconsPathConfigurator.IMG_ICON_LIGHT_OFF);
				}
				btnDark.setForeground(loadedTabObj.textArea.getForeground());
				btnDark.setBackground(loadedTabObj.textArea.getBackground());
				currentConfiguration.getTextEditorOpt().setBackCol(loadedTabObj.textArea.getBackground());
				currentConfiguration.getTextEditorOpt().setForeCol(loadedTabObj.textArea.getForeground());
			}
		});
		btnDark.doClick();

		// INSERT LOREM IPSUM
		btnLoremIpsum.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				FileStuff.appendFileTxtArea(new File(LOREM_IPSUM), loadedTabObj.textArea, false);
			}
		});

		// TEXT AREA FORMATTER
		btntTextFormat.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				formatText();
			}
		});

		// SLIDER FONT CHANGED
		sliderFontSize.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Font font = loadedTabObj.textArea.getFont();
				int oldCaretPLinePos = loadedTabObj.textArea.getCaretPosition();
				font = new Font(font.getName(), font.getStyle(), sliderFontSize.getValue());
				loadedTabObj.textArea.setFont(font);
				loadedTabObj.textArea.setCaretPosition(oldCaretPLinePos);
				lblSIZE.setText(String.valueOf(loadedTabObj.textArea.getFont().getSize()) + " Px");
				currentConfiguration.getTextEditorOpt().setFont(loadedTabObj.textArea.getFont());
			}
		});

		// SET SPEAKER'S VOICE SPEED
		sliderSpokenSpeed.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				int rate = sliderSpokenSpeed.getValue();
				currentConfiguration.getTextEditorOpt().setVoiceRate(rate);
				if (objSpeaker != null) {
					objSpeaker.voice.setDurationStretch((rate));
				}
			}
		});

		// SPEAK TEXT
		btnSpeak.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				new Thread(new Runnable() {

					@Override
					public void run() {
						
						String selected = (loadedTabObj.textArea.getSelectedText() != null && loadedTabObj.textArea.getSelectedText().length() != 0)? 
								loadedTabObj.textArea.getSelectedText() : loadedTabObj.textArea.getText();
						
						if (objSpeaker != null && objSpeaker.getVoice() != null) {
							objSpeaker.getVoice().deallocate();
							btnSpeak.setBackground(new Color(176, 196, 222));
						} else {
							// else speak
							objSpeaker = new Text2Speech();
							btnSpeak.setBackground(Color.RED);
							objSpeaker.dospeak(selected , "kevin16", (sliderSpokenSpeed.getValue() / 100.0f));
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
				if (loadedTabObj.textArea.canUndo()) {
					loadedTabObj.textArea.undoLastAction();
				}
			}
		});

		// REDO LAST OPERATION ON TEXT
		btnRedo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				if (loadedTabObj.textArea.canRedo()) {
					loadedTabObj.textArea.redoLastAction();
				}

			}
		});

		// CLEAR TEXT AREA
		btnClear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				if (JOptionPane.showConfirmDialog(parentFrame, "Clear Text?") == JOptionPane.OK_OPTION) {
					loadedTabObj.textArea.setText("");
				}
			}
		});

		// MAKE LOWERCASE AFTER LINE ENDERS
		btnLowerAfterDots.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				SelectionDto txtAreaDto = new SelectionDto(loadedTabObj.textArea);
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
				SelectionDto txtAreaDto = new SelectionDto(loadedTabObj.textArea);
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

				// backup unsaved to avoid changing saved info with theme change
				applyFileFormatFromCombo();
				updateSavedLabelNTab(null);
			}
		});
		
		//  LOCK/UNLOCK auto recognizer of file type and syntax
		chckbxLockLanguage.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				TextEditorUtils.findRightStyleFromComboNSelect(loadedTabObj.loadedFile, !chckbxLockLanguage.isSelected());
				currentConfiguration.getTextEditorOpt().setDefaultAutoSyntaxSync(!chckbxLockLanguage.isSelected());
			}
		});

		// style graphics
		comboStylesScheme.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				applySelectedTheme();
			}
		});

		// OPEN TEMPLATES PANEL
		btnTemplates.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!TemplatesChoosePanel.active) {
					JFrame panel = new TemplatesChoosePanel(mainApplication.getMainFrame(), loadedTabObj.textArea,
							comboFileFormat, btnTemplates);
					panel.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					panel.setBounds(750, 140, 500, 170);
					panel.setAlwaysOnTop(true);
					panel.setVisible(true);
					panel.setTitle("TEMPLATES manager - " + MainGUI.APPLICATION_NAME);
					btnTemplates.setEnabled(false);
					GuiUtils.changeFrameStateLaterUnchecked(mainApplication.getMainFrame(), panel, JFrame.MAXIMIZED_BOTH);
				}
			}
		});

		// ADAPT TO WIDTH MAINTAINING WORDS
		chkFitmantainwords.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (chkFitmantainwords.isSelected()) {
					loadedTabObj.textArea.setWrapStyleWord(true);
				} else {
					loadedTabObj.textArea.setWrapStyleWord(false);
				}
			}
		});

		// ADAPT TO WIDTH
		if (chkCutWordsTo.isSelected()) {
			loadedTabObj.textArea.setLineWrap(true);
			chkFitmantainwords.setEnabled(true);
		}
		chkCutWordsTo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (chkCutWordsTo.isSelected()) {
					loadedTabObj.textArea.setLineWrap(true);
					chkFitmantainwords.setEnabled(true);
					currentConfiguration.getTextEditorOpt().breaklineActive = true;
				} else {
					loadedTabObj.textArea.setLineWrap(false);
					loadedTabObj.textArea.setWrapStyleWord(false);
					chkFitmantainwords.setEnabled(false);
					currentConfiguration.getTextEditorOpt().breaklineActive = false;
				}
			}
		});

		// MARGIN TEXT AREA SELECTOR
		slider_margin.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				textMargin = slider_margin.getValue();
				lBLMargin.setText("" + textMargin);
				loadedTabObj.textArea.setMargin(new Insets(textMargin / 2, textMargin, textMargin / 2, textMargin));
				loadedTabObj.textArea.revalidate();
				loadedTabObj.textArea.repaint();
				currentConfiguration.getTextEditorOpt().setMargin(textMargin);
			}
		});

		// REMOVE WHITE SPACES OR CHARACHTER (CTRL PRESSED)
		btnRemoveSpaces.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int start = loadedTabObj.textArea.getSelectionStart();
				int end = loadedTabObj.textArea.getSelectionEnd();
				int newEnd = 0;
				if (TextEditorShortcutListener.flagCtrlPressed) {
					// remove character - open panel ad hoc
					String selection = JOptionPane.showInputDialog("Specify the charachter/string you want to remove");
					if (selection != null && !selection.equals("")) {
						newEnd = HandleString.removeCharachterFromTxtArea(selection, loadedTabObj.textArea, start, end);
					}
					TextEditorShortcutListener.flagCtrlPressed = false;
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
				shortcutsListener.flagHoverBtnRemoveSpaces = false;
				shortcutsListener.doControlActions();
			}

			public void mouseEntered(MouseEvent e) {
				shortcutsListener.flagHoverBtnRemoveSpaces = true;
				shortcutsListener.doControlActions();
			}

			public void mouseClicked(MouseEvent e) {
			}
		});

		// SET TEXT STYLE TO ITALICS
		btnItaliC.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
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
				boolean done = false;
				if (loadedTabObj.loadedFile == null || !loadedTabObj.loadedFile.exists()) {
					btnSaveTextFileAs.doClick();
					return;
				} else {
					try {
						done = FileStuff.writeStringToFile(loadedTabObj.textArea.getText(), loadedTabObj.loadedFile,
								true);
					} catch (IOException e1) {
						JOptionPane.showMessageDialog(currentTextEditor, "error Saving the selected file :(");
						logger.error("Exception happened!", e1);
					}
					if (done) {
						loadedTabObj.unsaved = false;
						updateSavedLabelNTab(null);
						TextEditorUtils.refreshMessageAndTabTooltip("[File saved]");
					} else {
						TextEditorUtils.errorAdvice("[Error - not saved]");
						lblCurrentFile.setText("(*)" + loadedTabObj.loadedFile.getName());
						lblCurrentFile.setForeground(Color.RED);
					}
					try {
						lblCurrentFile.setToolTipText(loadedTabObj.loadedFile.getCanonicalPath());
					} catch (IOException e1) {
						logger.error(e);
					}
				}
			}
		});

		// SAVE TEXT FILE AS
		btnSaveTextFileAs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				if (!alreadySaving) {
					// update signal value
					alreadySaving = true;
					boolean done = false;
					String exportNamePreset = (loadedTabObj.loadedFile != null && !loadedTabObj.loadedFile.getName().equals(""))? loadedTabObj.loadedFile.getName() : "New."+comboFileFormat.getSelectedItem().toString().toLowerCase();
					exportNamePreset = ("".equals(FilenameUtils.getExtension(exportNamePreset)))? exportNamePreset.concat(comboFileFormat.getSelectedItem().toString().toLowerCase()): exportNamePreset;
					try {
						File selectedFile = GenericFileChooserDialog.fileWrite(parentFrame, currentConfiguration, exportNamePreset);

						if (selectedFile != null) {
							selectedFile = adaptFileExt(selectedFile);
							if (selectedFile.exists()) {
								if (JOptionPane.showConfirmDialog(parentFrame, "File " + selectedFile.getName()
										+ " exists, overwrite it?") == JOptionPane.OK_OPTION) {
									done = FileStuff.writeStringToFile(loadedTabObj.textArea.getText(), selectedFile,
											true);
								} else {
									done = FileStuff.writeStringToFile(loadedTabObj.textArea.getText(), selectedFile,
											false);
								}
							} else {
								done = selectedFile.createNewFile();
								PrintWriter out = new PrintWriter(selectedFile.getAbsolutePath());
								out.write(loadedTabObj.textArea.getText());
								out.close();
							}
							if (done) {
								JOptionPane.showMessageDialog(parentFrame,
										"File " + selectedFile.getName() + " succesfully saved!");
								loadedTabObj.loadedFile = selectedFile;
								loadedTabObj.unsaved = false;
								updateSavedLabelNTab(null);
								lblCurrentFile.setText(loadedTabObj.loadedFile.getName());
								lblCurrentFile.setToolTipText(loadedTabObj.loadedFile.getCanonicalPath());
								TextEditorUtils.refreshMessageAndTabTooltip("[File saved]");
								currentConfiguration.getFileOpt().setLastDstFolderPath(
										loadedTabObj.loadedFile.getParentFile().getAbsolutePath());
							} else {
								TextEditorUtils.errorAdvice("[Error Saving selected file]");
							}
						}
					} catch (IOException e1) {
						TextEditorUtils.errorAdvice("[Error Saving selected file]");
						JOptionPane.showMessageDialog(currentTextEditor, "error Saving the selected file :(");
						logger.error("Exception happened!", e1);
					}
				}
				// update signal value
				alreadySaving = false;
				TextEditorUtils.refreshTree(loadedTabObj.loadedFile, true);
			}
		});

		// RELOAD INTO TEXT AREA FROM DISK
		btnReloadFromDisk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				loadedTabObj.textArea.setCursor(GuiUtils.CURSOR_WAIT);
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						if (!alreadyLoading && loadedTabObj.loadedFile != null && loadedTabObj.loadedFile.exists()) {
							// if unsaved ask to reload
							if (!(JOptionPane.showConfirmDialog(currentTextEditor,
									"If document is different or changed by another application, you will lose all unsaved changes. Continue?") == JOptionPane.YES_OPTION)) {
								loadedTabObj.textArea.setCursor(GuiUtils.CURSOR_TEXT);
								alreadyLoading = false;
								return;
							}
							alreadyLoading = true;
							loadedTabObj.textArea.setText("");
							FileStuff.appendFileTxtArea(new File(loadedTabObj.loadedFile.getAbsolutePath()),
									loadedTabObj.textArea, false);
							loadedTabObj.unsaved = false;
							updateSavedLabelNTab(null);
							TextEditorUtils.refreshMessageAndTabTooltip("[File Reloaded]");

							alreadyLoading = false;

						} else if (loadedTabObj.loadedFile == null || !loadedTabObj.loadedFile.exists()) {
							String msg = "File does not exist";
							JOptionPane.showMessageDialog(currentTextEditor, msg, msg, JOptionPane.ERROR_MESSAGE);
						}
						loadedTabObj.textArea.setCursor(GuiUtils.CURSOR_TEXT);
					}
				});
			}
		});

		// LOAD TEXT FILE
		btnLoadTextFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				if (!alreadyLoading) {

					File source = GenericFileChooser.fileRead(currentTextEditor);

					loadFile(source);

				} // end if !already loading
			}
		});

		// ADD NEW TAB
		btnNewTab.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				TextEditorUtils.addTab(null, false);
			}
		});

		// TAB CHANGE LISTENER
		mainTabList.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				
				if (!alreadyrefreshing && TextEditorUtils.lastIndexTabbedPane != mainTabList.getSelectedIndex()) {
					alreadyrefreshing = true;
					logger.debug("\n\n=>Triggered mainTabList action listener! Current obj:\n" + loadedTabObj);

					TextEditorUtils.tabRefreshAfterUpdate();
					TextEditorUtils.lastIndexTabbedPane = mainTabList.getSelectedIndex();
					TextEditorUtils.findRightStyleFromComboNSelect(loadedTabObj.loadedFile, !chckbxLockLanguage.isSelected());
					alreadyrefreshing = false;
				}
			}
		});
	}

	public static void fileChangedLblUpdate() {

		int indexChange = mainTabList.getSelectedIndex();
		try {
			if (!TextEditorUtils.tabJustSwitched && indexChange >= 0 && !isCrypterOn && !alreadyLoading) {
				loadedTabObj.unsaved = true;
				updateSavedLabelNTab(null);
			}
		} catch (Exception e) {
			logger.error("Error during label update!", e);
		}
	}

	/**
	 * specify NULL sourceIndex to use current selected tab, or a custom value > 0
	 * to specify the index of tab to take unsaved value
	 */
	public static void updateSavedLabelNTab(Integer sourceIndex) {
		int indexChange = (sourceIndex == null) ? mainTabList.getSelectedIndex() : sourceIndex;
		try {
			// reference to tabHeader title
			JLabel tabHeadLbl = (JLabel) (TextEditorUtils.getPanelFromTabHeader(indexChange)).getComponent(TabUtils.TAB_TITLE_ORDER);
			// IF ALREADY SAVED
			if (indexChange >= 0 && !loadedTabObj.unsaved) {
				// IF FILE IS DEFINED
				if (loadedTabObj.loadedFile != null) {
					lblCurrentFile.setText(loadedTabObj.loadedFile.getName());
					lblCurrentFile.setToolTipText(loadedTabObj.loadedFile.getCanonicalPath()+TextEditorTooltips.tooltipScrollTabsWithMouseWheel);
					tabHeadLbl.setText(HandleString.fitToMaxLentgh(loadedTabObj.loadedFile.getName(),
							TabUtils.MAX_TAB_LABELENGHT));
				} else {
					// IF FILE IS NOT DEFINED
					if (tabHeadLbl.getText().startsWith("(*)")) {
						tabHeadLbl.setText(HandleString.fitToMaxLentgh(
								tabHeadLbl.getText().substring(3, tabHeadLbl.getText().length()),
								TabUtils.MAX_TAB_LABELENGHT));
						if (lblCurrentFile.getText().startsWith("(*)")) {
							lblCurrentFile.setText("No File");
						}
					} else {
						tabHeadLbl.setText(
								HandleString.fitToMaxLentgh(DEFAULT_NEW_FILE_NAME, TabUtils.MAX_TAB_LABELENGHT));
						lblCurrentFile.setText("No File");
					}
				}
				// UPDATE ICON
				if (tabHeadLbl.getText().equals(DEFAULT_NEW_FILE_NAME)
						&& ("".equals(loadedTabObj.textArea.getText()) || alreadyLoading)) {
					TextEditorUtils.setIconToTab(IconsPathConfigurator.ICON_DOC_EMPTY, indexChange);
				} else {
					TextEditorUtils.setIconToTab(IconsPathConfigurator.ICON_DOC_SAVED, indexChange);
				}
				// ELSE IF NOT YET SAVED
			} else if (indexChange >= 0 && loadedTabObj.unsaved) {
				// IF FILE IS DEFINED
				if (loadedTabObj.loadedFile != null) {
					tabHeadLbl.setText(HandleString.fitToMaxLentgh("(*)" + loadedTabObj.loadedFile.getName(),
							TabUtils.MAX_TAB_LABELENGHT));
					lblCurrentFile.setText("(*)" + loadedTabObj.loadedFile.getName());
					lblCurrentFile.setToolTipText(loadedTabObj.loadedFile.getCanonicalPath()+TextEditorTooltips.tooltipScrollTabsWithMouseWheel);
				} else {
					// IF FILE IS NOT DEFINED
					lblCurrentFile.setText(tabHeadLbl.getText());
					if (!tabHeadLbl.getText().startsWith("(*)")) {
						tabHeadLbl.setText(
								HandleString.fitToMaxLentgh("(*)" + tabHeadLbl.getText(), TabUtils.MAX_TAB_LABELENGHT));
					}
					if (!lblCurrentFile.getText().startsWith("(*)")) {
						lblCurrentFile.setText(tabHeadLbl.getText());
					}
				}
				// UPDATE ICON
				TextEditorUtils.setIconToTab(IconsPathConfigurator.ICON_DOC_UNSAVED, indexChange);
			}
		} catch (Exception e) {
			logger.error("Error refreshing informations abount unsaved obj", e);
		}
	}

	public boolean askSave(int index) {
		int askSave = JOptionPane.CANCEL_OPTION;
		TabTextEditorObj selected = loadedList.get(index);
		if (selected.unsaved) {
			askSave = JOptionPane.showConfirmDialog(currentTextEditor, "Do you want to save current file?",
					"Save current file?", JOptionPane.YES_NO_CANCEL_OPTION);
		} else {
			return true;
		}
		if (askSave == JOptionPane.OK_OPTION) {

			if (loadedTabObj.loadedFile != null) {
				currentTextEditor.btnSaveTextFile.doClick();
			} else {
				currentTextEditor.btnSaveTextFileAs.doClick();
			}
			return !loadedTabObj.unsaved;
		} else if (askSave == JOptionPane.NO_OPTION) {
			return true;
		}
		return false;
	}

	public void initGuiFromConfig() {
		TextEditorOption txtOptions = currentConfiguration.getTextEditorOpt();

		// TEXT AREA
		checkBoxInlineTabs.setSelected(txtOptions.isInlinedTabs());
		chkCutWordsTo.setSelected(txtOptions.isBreaklineActive());
		loadedTabObj.textArea.setAntiAliasingEnabled(txtOptions.antialiasedTxtArea);
		loadedTabObj.setPreferences(txtOptions.txtAreaDetailLevel);
		slider_margin.setValue(txtOptions.getMargin());
		sliderFontSize.setValue(txtOptions.getFont().getSize());
		sliderSpokenSpeed.setValue(txtOptions.getVoiceRate());
		loadedTabObj.textArea.setBackground(txtOptions.getBackCol());
		RsyntaxUtils.setBackground(loadedTabObj.textArea, txtOptions.backCol);
		loadedTabObj.textArea.setForeground(txtOptions.getForeCol());
		RsyntaxUtils.setForeground(loadedTabObj.textArea, txtOptions.foreCol);
		loadedTabObj.textArea.setFont(txtOptions.getFont());
		loadedTabObj.textArea.setPaintTabLines(txtOptions.isTabLineVisible());
		comboStylesScheme.setSelectedItem(txtOptions.selectedTheme);
		
		// TREE LOCKED
		if (txtOptions.treeLocked) {
			toggleButtonFileTreeLocked.setIcon(new ImageIcon(IconsPathConfigurator.ICON_PIN));
		}else {
			toggleButtonFileTreeLocked.setIcon(new ImageIcon(IconsPathConfigurator.ICON_UNPIN));
		}
		
		// WORKSPACE VIEW ACTIVE ON/OFF
		if (txtOptions.workspaceViewSelected) {
			toggleButtonFileTreeWorkspace.setIcon(new ImageIcon(IconsPathConfigurator.ICON_JOB_ON));
		}else {
			toggleButtonFileTreeWorkspace.setIcon(new ImageIcon(IconsPathConfigurator.ICON_JOB_OFF));
		}
		
		// AUTO LANGUAGE SYNC
		chckbxLockLanguage.setSelected(!txtOptions.defaultAutoSyntaxSync);
		
		// FILE TREE PANEL ON/OFF
		btnToggleFileView.setSelected(txtOptions.fileTreeActive);
		if (txtOptions.fileTreeActive) {
			if (loadedTabObj.loadedFile == null) {
				splitPaneMain.setDividerLocation(0);
			}
			btnToggleFileView.setBackground(new Color(120, 0, 0));
		} else {
			btnToggleFileView.setBackground(SystemColor.inactiveCaption);
		}

		splitPaneMain.setDividerLocation(250);

		TextEditorUtils.updateGuiLafWindows(currentTextEditor);
	}

	/**
	 * KEYBOARD SHORTCUTS - use custom listener
	 */
	private TextEditorShortcutListener getKeyEventHandler() {
		return new TextEditorShortcutListener(currentTextEditor);
	}

	private void defaultTxtConfig(Font DEFAULT, Color DEFAULT_BCKG_COL, Color DEFAULT_FORE_COL, int margin) {
		TextEditorOption txtOpt = currentConfiguration.getTextEditorOpt();
		txtOpt.setFont(DEFAULT);
		txtOpt.setBackCol(DEFAULT_BCKG_COL);
		txtOpt.setForeCol(DEFAULT_FORE_COL);
		txtOpt.setMargin(margin);
	}

	private File adaptFileExt(File selectedFile) {
		String givenExtension = FilenameUtils.getExtension(selectedFile.getName());

		// if not specified extension, invalid or equal to checkbox, then change
		// extension to current in global var
		if (("." + givenExtension).equals(CURRENT_EXT_OUT) || givenExtension.equals("")
				|| (("." + givenExtension).length() > 5)) {
			selectedFile = FileStuff.changeExtension(selectedFile, CURRENT_EXT_OUT);
		} else {
			selectedFile = FileStuff.changeExtension(selectedFile, "." + givenExtension);
		}

		return selectedFile;
	}

	private void loadOpenedFilesAtStart() {
		loadedTabObj.textArea.setCursor(GuiUtils.CURSOR_WAIT);

		boolean loadedAlmostOne = false;
		alreadyLoading = true;
		if (currentConfiguration.getTextEditorOpt().getOpenedFileList() != null
				&& currentConfiguration.getTextEditorOpt().getOpenedFileList().size() != 0) {
			
			for (String pathToOpen : currentConfiguration.getTextEditorOpt().getOpenedFileList()) {
				File toLoadFile = new File(pathToOpen);
				if (toLoadFile.exists()) {
					TextEditorUtils.addTab(toLoadFile, !loadedAlmostOne);
					if (!loadedAlmostOne)
						loadedAlmostOne = true;
				}
			}
		}

		// ALSO ARGUMENTS IF THERE ARE
		if (MainGuiGeneralAppStarter.argumentsFiles != null && MainGuiGeneralAppStarter.argumentsFiles.length != 0) {
			logger.debug("ARGUMENTS FILES:" + Arrays.toString(MainGuiGeneralAppStarter.argumentsFiles));
			for (File file : MainGuiGeneralAppStarter.argumentsFiles) {
				loadFile(file);
				logger.debug(file.getAbsolutePath() + " -> Loaded");
			}
		}

		if (loadedAlmostOne) {
			// first remove first tab if at least one file is loaded
			TextEditorUtils.removeTab(0);

			// now try to select the old opened one
			String toOpenAtStart = currentConfiguration.getTextEditorOpt().selectedAtStart;
			try {
				int i = 0;
				for (TabTextEditorObj iteratorObj : loadedList) {
					if (iteratorObj.loadedFile != null
							&& toOpenAtStart.equals(iteratorObj.loadedFile.getAbsolutePath())) {
						mainTabList.setSelectedIndex(i);
						break;
					}
					i++;
				}
			} catch (Exception e) {
				logger.warn("Cannot select default file " + toOpenAtStart, e);
			}
		}
		alreadyLoading = false;
		loadedTabObj.textArea.setCursor(GuiUtils.CURSOR_TEXT);
		TextEditorUtils.refreshTree(loadedTabObj.loadedFile, true);
	}

	public static void applyFileFormatFromCombo() {
		boolean unsaved = loadedTabObj.unsaved;
		logger.debug("applyFileFormatFromCombo - START");

		SupportedFileTextEditor selected = (SupportedFileTextEditor) comboFileFormat.getSelectedItem();
		loadedTabObj.textArea.setSyntaxEditingStyle(selected.syntax);
		SupportedFileTextEditor selExtension = (SupportedFileTextEditor) comboFileFormat.getSelectedItem();
		RsyntaxUtils.addAutoCompletion(loadedTabObj.textArea, selExtension.languageWordsFile);
		loadedTabObj.textArea.revalidate();
		CURRENT_EXT_OUT = selected.nomeStringa;
		lblAdvice.setText("[Current output: " + CURRENT_EXT_OUT + "]");
		btnSaveTextFileAs.setToolTipText("Choose name and folder, save " + CURRENT_EXT_OUT + " file!");

		loadedTabObj.unsaved = unsaved;
		logger.debug("applyFileFormatFromCombo - FINISH");
	}

	// TABBED EDITOR FUNCTIONS //

	public void loadFile(final File fileToLoad) {
		
		logger.debug("Started file loading into new tab: "+fileToLoad.getAbsolutePath()+"\n\n");
		loadedTabObj.textArea.setCursor(GuiUtils.CURSOR_WAIT);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				alreadyLoading = true;
				boolean fileAlreadyPresent = TextEditorUtils.checkFilePresence(fileToLoad) >= 0;
				if (fileToLoad == null || !fileToLoad.exists() || fileAlreadyPresent) {
					alreadyLoading = false;
					loadedTabObj.textArea.setCursor(GuiUtils.CURSOR_TEXT);

				} else {

					// add new tab with selected file
					try {
						TextEditorUtils.addTab(fileToLoad, false);
						logger.debug(fileToLoad.getAbsolutePath()+" -> Loaded!");
					} catch (Throwable e1) {
						String msg = "Error occurred loading file into new tab: "+fileToLoad.getAbsolutePath()+"\n\n";
						logger.error(msg);
						JOptionPane.showMessageDialog(null, msg, msg, JOptionPane.ERROR_MESSAGE);
					}

					loadedTabObj.loadedFile = fileToLoad;
					lblCurrentFile.setText(loadedTabObj.loadedFile.getName());
					try {
						lblCurrentFile.setToolTipText(loadedTabObj.loadedFile.getCanonicalPath());
					} catch (IOException e) {
						logger.error(e);
					}
//					findRightStyleFromComboNSelect(fileToLoad);
					applySelectedTheme();
					loadedTabObj.unsaved = false;
					updateSavedLabelNTab(null);
					TextEditorUtils.refreshMessageAndTabTooltip("[File loaded]");
					currentConfiguration.getFileOpt().setLastSrcFolderPath(fileToLoad.getParentFile().getAbsolutePath());

					alreadyLoading = false;
					loadedTabObj.textArea.setCursor(GuiUtils.CURSOR_TEXT);
					logger.debug("TextEditor.loadFile() completed - Exiting");
				}
			}
		});

	}

	/** updates textArea reference in panels **/
	public void refreshTxtAreaReference() {

		if (searchBox != null && searchBox.isVisible()) {
			searchBox = TextAreaDialogs.getSearchDialog(parentFrame, loadedTabObj.textArea, null, currentTextEditor,  "successAdvice", "errorAdvice");
		}

		if (escapeReplacerPanel != null && EscapeReplacerPanel.active) {
			escapeReplacerPanel.outputTextArea = loadedTabObj.textArea;
		}
	}

	public void applyCurrentGraphicsTextArea(RSyntaxTextArea textArea) {
		TextEditorOption txtOptions = currentConfiguration.getTextEditorOpt();

		textArea.setBackground(txtOptions.backCol);
		textArea.setForeground(txtOptions.foreCol);
		chkCutWordsTo.setSelected(txtOptions.isBreaklineActive());
		textArea.setAntiAliasingEnabled(txtOptions.antialiasedTxtArea);
		loadedTabObj.setPreferences(txtOptions.txtAreaDetailLevel);
		textArea.setFont(txtOptions.font);
		textArea.setLineWrap(chkCutWordsTo.isSelected());
		textArea.setWrapStyleWord(chkFitmantainwords.isSelected());

		int margin = slider_margin.getValue();
		textArea.setMargin(new Insets(margin, margin, margin, margin));
		textArea.setTabSize(currentConfiguration.getTextEditorOpt().tabSize);
		textArea.setPaintTabLines(txtOptions.isTabLineVisible());
		applySelectedTheme();
		SupportedFileTextEditor selExtension = (SupportedFileTextEditor) comboFileFormat.getSelectedItem();
		RsyntaxUtils.addAutoCompletion(textArea, selExtension.languageWordsFile);

		textArea.revalidate();
		textArea.repaint();
	}

	private void applySelectedTheme() {
		// backup unsaved to avoid changing saved info with theme change
		boolean unsaved = loadedTabObj.unsaved;

		Themes theme = (Themes) comboStylesScheme.getSelectedItem();
		if (theme.equals(Themes.NONE)) {
			loadedTabObj.textArea.setHighlightCurrentLine(false);
			RsyntaxUtils.setBackground(loadedTabObj.textArea, currentConfiguration.getTextEditorOpt().backCol);
			RsyntaxUtils.setForeground(loadedTabObj.textArea, currentConfiguration.getTextEditorOpt().foreCol);
		} else {
			loadedTabObj.textArea.setHighlightCurrentLine(true);
			currentConfiguration.getTextEditorOpt().setSelectedTheme(theme);
			// loadedTabObj.currentScrollPane.getGutter().setBackground(loadedTabObj.textArea.getBackground());
			// loadedTabObj.currentScrollPane.getGutter().setForeground(ImageGeneralWorker.getComplimentColor(loadedTabObj.textArea.getBackground()));
			RsyntaxUtils.changeStyleViaThemeXml(loadedTabObj.textArea, theme.defaultPath);
		}
		// loadedTabObj.textArea.revalidate();
		loadedTabObj.textArea.repaint();

		// retore unsaved info backup
		loadedTabObj.unsaved = unsaved;
	}

	public DocumentListener getChangeListener() {
		return new DocumentListener() {

			public void removeUpdate(DocumentEvent e) {
				fileChangedLblUpdate();
			}

			public void insertUpdate(DocumentEvent e) {
				fileChangedLblUpdate();
			}

			public void changedUpdate(DocumentEvent e) {
				fileChangedLblUpdate();
			}
		};
	}

	private void formatText() {
		boolean error = false;
		String source = new String(loadedTabObj.textArea.getText());
		String old = new String(source);

		// XML format handler (if xml formatter works then is a xml recognized file)
		if (CURRENT_EXT_OUT.equals(".xml")) {
			String formatted = HandleString.xmlPrettyFormat(source, 4);
			if (!formatted.equals(HandleString.ERROR)) {
				source = HandleString.xmlPrettyFormat(source, 4);
				if (source.equals(old)) {
					// already formatted
					source = HandleString.removeLines(source);
				}
			} else {
				error = true;
				JOptionPane.showMessageDialog(parentFrame,
						"Error reading xml format, maybe it's not a valid xml String?", "XML format Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}

		// apply formatted to textArea
		if (!error && !source.equals(old)) {
			loadedTabObj.textArea.setText(source);
		}
	}

	public void startRefreshLabelCurrFIleThread() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				while (MainGUI.mainAppRunning) {
					if (loadedTabObj != null && loadedTabObj.textArea != null) {
						int selectionLength = loadedTabObj.textArea.getSelectionEnd()
								- loadedTabObj.textArea.getSelectionStart();
						int charTotNum = loadedTabObj.textArea.getText().length();
						int textLines = loadedTabObj.textArea.getLineCount();
						int column = loadedTabObj.textArea.getCaretOffsetFromLineStart();
						try {
							int line = loadedTabObj.textArea.getLineOfOffset(loadedTabObj.textArea.getCaretPosition());
							dynLblCurrLine.setText(String.valueOf(line));
						} catch (BadLocationException e1) {
							logger.error("Error refreshing current line in footer", e1);
						}
						dynLblSelLength.setText(String.valueOf(selectionLength));
						dynLblTotChars.setText(String.valueOf(charTotNum));
						dynLblTotLines.setText(String.valueOf(textLines));
						dynLblColumn.setText(String.valueOf(column));
						dynLabelCharset.setText(System.getProperty("file.encoding"));
						File currFile = loadedTabObj.loadedFile;
						if (loadedTabObj.loadedFile != null) {
							String oldCurrFile = lblCurrentFile.getText();
							boolean updateA = !oldCurrFile.equals(currFile.getAbsolutePath());
							boolean updateB = !oldCurrFile.equals(currFile.getName());
							if (rdbtnShowCompletePath.isSelected() && updateA) {
								try {
									lblCurrentFile.setText(loadedTabObj.loadedFile.getCanonicalPath());
								} catch (IOException e) {
									logger.error(e);								}
							} else if (!rdbtnShowCompletePath.isSelected() && updateB) {
								lblCurrentFile.setText(loadedTabObj.loadedFile.getName());
							}
						}
					}
					try {
						Thread.sleep(300);
					} catch (InterruptedException e) {
					}
				}
			}
		}).start();
	}
}

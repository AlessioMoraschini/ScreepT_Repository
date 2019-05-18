package gUI.panels;

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
import resources.IconsPathConfigurator;
import tabbedpaneutils.CustomTabbedPaneUI_v1;
import tabbedpaneutils.TabUtils;
import textareautil.SelectionDto;
import textareautil.TextLineNumber;
import utils.ColorDialogUtils;
import utils.RsyntaxUtils;
import utils.SysUtils;
import utils.Themes;
import variousutils.HandleString;

import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.drjekyll.fontchooser.FontDialog;
import org.fife.ui.rtextarea.RUndoManager;

import amfileutils.FileStuff;
import conversion.Text2Speech;
import crypt.aes.AesCipher;
import dialogutils.GenericFileChooser;
import frameutils.EscapeReplacerPanel;
import frameutils.GuiUtils;
import frameutils.SearchFrame;
import iNIT.INItializer;
import iNIT.om.TextEditorOption;
import maingUI.mainGUI;
import media.ImageGeneralWorker;

import java.awt.SystemColor;

import javax.swing.JTextArea;

import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.awt.event.ActionEvent;
import javax.swing.JScrollPane;
import javax.swing.ImageIcon;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.UIManager;
import javax.swing.JCheckBox;
import javax.swing.JSeparator;
import javax.swing.border.LineBorder;
import javax.swing.JComboBox;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;

import gUI.om.SupportedFileTextEditor;
import gUI.om.TabTextEditorObj;
import gUI.utils.filedialog.GenericFileChooserDialog;
import gUI.utils.filedialog.TemplatesChoosePanel;
import gUI.utils.shortcutshandler.TextEditorListener;
import gui.ImageSelector2TextFrame;
import gui.PdfSelector2TextFrame;

import javax.swing.JPasswordField;
import java.awt.Cursor;
import java.awt.Dimension;

public class TextEditor extends JPanel {

	// TextFileWriter logger creation
	static Logger logger = Logger.getLogger(TextEditor.class);
	
	public static final char DEFAULT_PSW_CHAR = '\u25CF';
	public static final String DEFAULT_NEW_FILE_NAME  = "New.txt";
	
	public static final int TEXT_AREA_TAB_SIZE = 2;
	
	public static final int MAX_TAB_LABELENGHT = 14;
	
	public static final Font DEFAULT_FONT = new Font("Segoe UI", Font.PLAIN, 16);
	
	//  RESOURCE FILES //
	private final static String LOREM_IPSUM = "Resources_Scrypt/Templates/LoremIpsum.txt";
	
	// TOOLTIPS //
	public static final String tooltipSaveTextFile = "[Ctrl+S] Save current file";
	public static final String tooltipSaveTextFileAs = "[Ctrl+Shift+S] Choose name and folder, save current file";
	public static final String tooltipReloadFromDisk = "[Ctrl+Shift+R] Reload current file from Disk"; 
	public static final String tooltipLoadTextFile = "[Ctrl+O] Load source File";
	public static final String tooltipNewEmptyTab = "[Ctrl+N] Create a new empty tab";
	public static final String tooltipSpeak = "[Ctrl+spacebar] Speak the text written in the text area (only english). Use the upper slider to change speed before to play, press again to disable";
	public static final String tooltipDecrypt = "[Ctrl+shift+D] Decrypt text with given key";
	public static final String tooltipCrypt = "[Ctrl+shift+C] Crypt text with given key";
	public static final String tooltipOpenInScreept = "Open current file in ScreepT";
	public static final String tooltipBackgroundColor = "Change background color (won't be saved in .txt)";
	public static final String tooltipForegroundTxt = "Change text color (won't be saved in .txt)";
	public static final String tooltipDarkSwitch = "[Ctrl+B] Switch upon dark and light text combination";
	public static final String tooltipUpperPostDots = "Makes text uppercase after dots, or a significat line brokers";
	public static final String tooltipLowerPostDots = "Makes text lowerCase after dots, or a significat line brokers";
	public static final String tooltipTt = "[Ctrl+Shift+UP] Makes selected/all text uppercase";
	public static final String tooltipTtTt = "[Ctrl+shift+dot] Makes selected/all initial characters uppercase";
	public static final String tooltipLoremIpsum = "Insert classic test text \"Lorem Ipsum\"";
	public static final String tooltipParseImage = "Read Image and parse text";
	public static final String tooltipParsePdfTxt = "Read Pdf and parse text";
	public static final String tooltipFindText = "[Ctrl+F] Search text into current editor";
	public static final String tooltipFontChooser = "Change font and text-properties";
	public static final String tooltipTemplates = "Choose between many different patterns of code to insert in you text";
	public static final String tooltipDefaultOpt = "Reset default text-graphics values";
	public static final String tooltipRemoveSpaces = "Remove spaces from selected/all text (press Ctrl to choose a character to remove)";
	public static final String tooltipRemoveContent = "Remove specific words from selected/all text";
	public static final String tooltipUndo = "[Ctrl+Z] Undo Last";
	public static final String tooltipClearText = "[Ctrl+canc] Erase the content of the current opened file displayed (without save)";
	public static final String tooltipRedo = "[Alt+Z] Redo Last";
	public static final String tooltipBoldText = "Set bold style to all text";
	public static final String tooltipItalic = "Set italics style to all text"; 
	public static final String tooltipViewInSysExplorer = "View in System Explorer (press Ctrl to open in Command Line)";
	public static final String tooltipViewInCommandLine = "View in Command line";
	public static final String tooltipReplaceEscapes = "Open Escape sequences / special characters replacement panel - Selected/all text";

	public static final String tooltipCutWordsTo = "When selected, the text will be cropped to adapt the size of the text area";
	public static final String tooltipFitMaintainWords = "When selected, the text will be cropped to adapt the size of the text area (without breaking words into fragments)";
	public static final String tooltipInlineTabs = "If selected tabs will be o one line, else they will be always visible organized in rows";
	
	public static final String tooltipComboFileFormat = "Choose file extension";
	public static final String tooltipComboStyleScheme = "Choose preferred style scheme";
			
	// GUI Objects //
	private JCheckBox checkBoxInlineTabs;
	public JCheckBox chkFitmantainwords;
	public JCheckBox chkCutWordsTo;
	
	private JLabel lblSIZE;
	private JLabel lblNewLabel;
	private JLabel lblTextEditor;
	private JLabel lblMargin;
	private JLabel lBLMargin;
	private JLabel lblFontSize;
	private JLabel lblCurrentFileLBL;
	private static JLabel lblCurrentFile;
	private JLabel lblVoiceSpeed;
	public JLabel lblAdvice;
	
	public JButton btntTextFormat;
	public JButton btnLoadTextFile;
	public JButton btnSaveTextFileAs;
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
	public JButton btnFindText;
	public JButton btnReplaceEscapesSeq;
	public JButton btnNewTab;
	public JButton btnRemoveSpaces;
	public JButton btnViewInExplorer;
	
	private JSeparator separator_2;
	
	public JComboBox comboFileFormat;
	private JComboBox comboStylesScheme;
	
	public JSlider sliderSpokenSpeed;
	public JSlider sliderFontSize;
	public JSlider slider_margin;
	
	public static LinkedList<TabTextEditorObj> loadedList;
	public static TabTextEditorObj loadedTabObj;
//	private JScrollPane textScrollPaneArea;
	public static JTabbedPane mainTabList;
//	public static RSyntaxTextArea textArea_1;
	private JPasswordField txtAesKey; // field containing fast crypt text user key, sinchronized from ini file as default
	public SearchFrame searchBox;
	private ImageSelector2TextFrame imageParserBox;
	private PdfSelector2TextFrame pdfParserBox;
	public TextLineNumber lineNumberPanel;
	
	private int lastIndexTabbedPane=0;
	public boolean isRemovingTab = false;
	public boolean isAddingTab = false;
	private static boolean tabJustSwitched = false;
	public static boolean isCrypterOn = false;
	
	private JScrollPane mainScrollPane;
	private JPanel mainPanel;
	
	public JFrame parentFrame; // the frame that instantiate this object

	// GENERAL FIELDS //
	private boolean uppercase;
	private String backupText;
	
	// value used for margins of text
	private int textMargin;
	
	// flag to signal if function is already running
	public boolean alreadySaving = false;
	public static boolean alreadyLoading = false;
	public boolean askSaveBeforeClose = true;
	
	public String CURRENT_EXT_OUT = ".txt";
	public Color LBL_ADVICE_COLOR;
	
	public RUndoManager manager;
	private INItializer currentConfiguration;
	private TextEditor currentTextEditor; // current object
	private Text2Speech objSpeaker; // field containing the current object used to speak text
	private JCheckBox chckbxAesShowKey;
	
	private mainGUI mainApplication;
	
	private TextEditorListener shortcutsListener;
	
//////////////// CONSTRUCTOR /////////////////////
	
	public TextEditor(mainGUI mainGUI, INItializer currentConf) {

		logger.info(this.getClass().getName() + " Starting...");

		loadedList = new LinkedList<TabTextEditorObj>();
		
		setBorder(BorderFactory.createEmptyBorder());
		setBackground(Color.BLACK);
		Dimension maxSize = GuiUtils.getScreenSize();
		setLayout(new MigLayout("", "[:1624px:1920,grow]", "[:711px:1080,grow]"));
	
		// initialize values used in gui //
		currentConfiguration = currentConf;		
		parentFrame = mainGUI.getMainFrame();
		mainApplication = mainGUI;
		currentTextEditor=this;
		LBL_ADVICE_COLOR = new Color(124, 252, 0);
		textMargin = 14;
		uppercase = false;
		
//		textArea_1 = new RSyntaxTextArea();
//		textArea_1.setCodeFoldingEnabled(true);
//		textArea_1.setAutoIndentEnabled(true);
////		RsyntaxUtils.adaptForegrToBackground(textArea_1);
//		textArea_1.setTabSize(TEXT_AREA_TAB_SIZE);
//		textArea_1.setFont(currentConfiguration.getTextEditorOpt().getFont());
//		textArea_1.setMargin(new Insets(textMargin/2, textMargin, textMargin/2, textMargin));
		
		mainPanel = new JPanel();
		mainPanel.setBackground(Color.DARK_GRAY);
		mainPanel.setMaximumSize(GuiUtils.getScreenSizePerc(95));
		mainPanel.setLayout(new MigLayout("", "[20][:16:16px][40][40px][40px][40px][40px][40px][][][:40:40][:40:40][:40:40][:40:40][:40px:40px][:40px:40px][:50px:50px][:50px:50px][20][30][40.00][40,fill][:40px:40px][:40px:40px][:40px:40px][35][35][70,grow]", "[:20:50px,grow][][20][][25][25][][15px][200px,grow]"));
		mainScrollPane = new JScrollPane();
		mainScrollPane.setBorder(BorderFactory.createEmptyBorder());
		mainScrollPane.setBackground(Color.DARK_GRAY);
		mainScrollPane.setViewportView(mainPanel);
		add(mainScrollPane, "cell 0 0,grow");
		
		lblCurrentFileLBL = new JLabel("   Current file:");
		lblCurrentFileLBL.setForeground(SystemColor.controlHighlight);
		lblCurrentFileLBL.setFont(new Font("Tahoma", Font.BOLD, 18));
		mainPanel.add(lblCurrentFileLBL, "cell 0 2 5 1,alignx center");
		
		lblCurrentFile = new JLabel("\\*.txt");
		lblCurrentFile.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblCurrentFile.setForeground(SystemColor.activeCaption);
		mainPanel.add(lblCurrentFile, "cell 5 2 7 1,alignx left");
		
		lblVoiceSpeed = new JLabel("Voice rate");
		lblVoiceSpeed.setForeground(new Color(211, 211, 211));
		mainPanel.add(lblVoiceSpeed, "cell 20 2,alignx center");
		
		sliderSpokenSpeed = new JSlider();
		sliderSpokenSpeed.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
		sliderSpokenSpeed.setValue(currentConfiguration.getTextEditorOpt().getVoiceRate());
		sliderSpokenSpeed.setMaximum(400);
		mainPanel.add(sliderSpokenSpeed, "cell 21 2 4 1,alignx center");
		
		lblAdvice = new JLabel("");
		lblAdvice.setBackground(SystemColor.info);
		lblAdvice.setFont(new Font("Tahoma", Font.BOLD, 13));
		lblAdvice.setForeground(new Color(176, 224, 230));
		lblAdvice.setHorizontalAlignment(SwingConstants.CENTER);
		mainPanel.add(lblAdvice, "cell 25 2 3 1,alignx center,aligny center");
		lblAdvice.setText(" [Current output: .txt]");
		
		btnNewTab = new JButton("");
		btnNewTab.setToolTipText(tooltipNewEmptyTab);
		btnNewTab.setForeground(Color.BLACK);
		btnNewTab.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnNewTab.setIcon(new ImageIcon(IconsPathConfigurator.ICON_DOC_EMPTY));
		btnNewTab.setBackground(SystemColor.inactiveCaption);
		mainPanel.add(btnNewTab, "cell 2 4,grow");
		
		btnSaveTextFile = new JButton("");
		btnSaveTextFile.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnSaveTextFile.setToolTipText(tooltipSaveTextFile);
		btnSaveTextFile.setForeground(Color.BLACK);
		btnSaveTextFile.setIcon(new ImageIcon(IconsPathConfigurator.ICON_FILE_SAVE));
		btnSaveTextFile.setBackground(SystemColor.inactiveCaption);
		mainPanel.add(btnSaveTextFile, "cell 3 4,grow");
		
		btnSaveTextFileAs = new JButton("");
		btnSaveTextFileAs.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnSaveTextFileAs.setToolTipText(tooltipSaveTextFileAs);
		btnSaveTextFileAs.setForeground(SystemColor.menuText);
		btnSaveTextFileAs.setIcon(new ImageIcon(IconsPathConfigurator.ICON_FILE_SAVE_AS));
		btnSaveTextFileAs.setBackground(SystemColor.inactiveCaption);
		mainPanel.add(btnSaveTextFileAs, "cell 4 4,grow");
		
		btnReloadFromDisk = new JButton("");
		btnReloadFromDisk.setToolTipText(tooltipReloadFromDisk);
		btnReloadFromDisk.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnReloadFromDisk.setIcon(new ImageIcon(IconsPathConfigurator.ICON_RELOAD_CONF));
		btnReloadFromDisk.setForeground(Color.BLACK);
		btnReloadFromDisk.setBackground(SystemColor.inactiveCaption);
		mainPanel.add(btnReloadFromDisk, "cell 5 4,grow");
		
		btnViewInExplorer = new JButton("");
		btnViewInExplorer.setToolTipText(tooltipViewInSysExplorer);
		btnViewInExplorer.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnViewInExplorer.setIcon(new ImageIcon(IconsPathConfigurator.ICON_COPY_DIR_STRUCTURE));
		btnViewInExplorer.setBackground(new Color(176, 196, 222));
		mainPanel.add(btnViewInExplorer, "cell 6 4,grow");
		
		btnTtTt = new JButton("Tt Tt");
		btnTtTt.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnTtTt.setFont(new Font("SansSerif", Font.BOLD, 12));
		btnTtTt.setBackground(new Color(176, 196, 222));
		btnTtTt.setToolTipText(tooltipTtTt);
		mainPanel.add(btnTtTt, "cell 7 4,growx");
		
		btnTt = new JButton("TT");
		btnTt.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnTt.setFont(new Font("SansSerif", Font.BOLD, 12));
		btnTt.setBackground(new Color(176, 196, 222));
		btnTt.setToolTipText(tooltipTt);
		mainPanel.add(btnTt, "cell 8 4,growx");
		
		btnUpperPostDots = new JButton(".Tt");
		btnUpperPostDots.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnUpperPostDots.setToolTipText(tooltipUpperPostDots);
		btnUpperPostDots.setFont(new Font("SansSerif", Font.BOLD, 12));
		btnUpperPostDots.setBackground(new Color(176, 196, 222));
		mainPanel.add(btnUpperPostDots, "cell 9 4,growx");
		
		btnLowerAfterDots = new JButton(".tT");
		btnLowerAfterDots.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnLowerAfterDots.setToolTipText(tooltipLowerPostDots);
		btnLowerAfterDots.setToolTipText("Makes text uppercase after dots, or a significat line brokers");
		btnLowerAfterDots.setFont(new Font("SansSerif", Font.BOLD, 12));
		btnLowerAfterDots.setBackground(new Color(176, 196, 222));
		mainPanel.add(btnLowerAfterDots, "cell 10 4");
		
		btnClear = new JButton("");
		btnClear.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnClear.setToolTipText(tooltipClearText);
		btnClear.setBackground(new Color(176, 196, 222));
		btnClear.setIcon(new ImageIcon(IconsPathConfigurator.ICON_DELETE));
		btnClear.setFont(new Font("Wide Latin", Font.BOLD, 14));
		mainPanel.add(btnClear, "cell 11 4,alignx center,aligny center");
		
		btnUndo = new JButton("");
		btnUndo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnUndo.setToolTipText(tooltipUndo);
		btnUndo.setIcon(new ImageIcon(IconsPathConfigurator.ICON_UNDO));
		btnUndo.setBackground(new Color(176, 196, 222));
		mainPanel.add(btnUndo, "cell 12 4,alignx center");
		
		btnRedo = new JButton("");
		btnRedo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnRedo.setToolTipText(tooltipRedo);
		btnRedo.setBackground(new Color(176, 196, 222));
		mainPanel.add(btnRedo, "cell 13 4,alignx center");
		btnRedo.setIcon(new ImageIcon(IconsPathConfigurator.ICON_REDO));
		
		btnBold = new JButton("");
		btnBold.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnBold.setToolTipText(tooltipBoldText);
		btnBold.setBackground(new Color(176, 196, 222));
		btnBold.setIcon(new ImageIcon(IconsPathConfigurator.ICON_TEXT_BOLD));
		mainPanel.add(btnBold, "cell 14 4,alignx center");
		
		btnItaliC = new JButton("");
		btnItaliC.setToolTipText(tooltipItalic);
		btnItaliC.setBackground(new Color(176, 196, 222));
		btnItaliC.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnItaliC.setIcon(new ImageIcon(IconsPathConfigurator.ICON_TEXT_ITALIC));
		mainPanel.add(btnItaliC, "cell 15 4,alignx center");
		
		btnLoremIpsum = new JButton("Lorem Ipsum");
		btnLoremIpsum.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnLoremIpsum.setToolTipText(tooltipLoremIpsum);
		btnLoremIpsum.setFont(new Font("SansSerif", Font.BOLD, 12));
		btnLoremIpsum.setBackground(new Color(176, 196, 222));
		mainPanel.add(btnLoremIpsum, "cell 20 4,growx");
		
		btnSpeak = new JButton("");
		btnSpeak.setCursor(GuiUtils.CURSOR_HAND);
		btnSpeak.setToolTipText(tooltipSpeak);
		btnSpeak.setIcon(new ImageIcon(IconsPathConfigurator.ICON_SPEAKER));
		btnSpeak.setFont(new Font("SansSerif", Font.BOLD, 12));
		btnSpeak.setBackground(new Color(176, 196, 222));
		mainPanel.add(btnSpeak, "cell 21 4,alignx center");
		
		btnParseImageText = new JButton("");
		btnParseImageText.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnParseImageText.setIcon(new ImageIcon(IconsPathConfigurator.ICON_CONVERT_OCR_IMAGE));
		btnParseImageText.setToolTipText(tooltipParseImage);
		btnParseImageText.setFont(new Font("SansSerif", Font.BOLD, 12));
		btnParseImageText.setBackground(new Color(176, 196, 222));
		mainPanel.add(btnParseImageText, "cell 22 4,alignx center");
		
		btnParsePdfText = new JButton("");
		btnParsePdfText.setToolTipText(tooltipParsePdfTxt);
		btnParsePdfText.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnParsePdfText.setIcon(new ImageIcon(IconsPathConfigurator.F_ICON_PDF));
		btnParsePdfText.setFont(new Font("SansSerif", Font.BOLD, 12));
		btnParsePdfText.setBackground(new Color(176, 196, 222));
		mainPanel.add(btnParsePdfText, "cell 23 4,alignx center");
		
		lblMargin = new JLabel("Margin:");
		lblMargin.setHorizontalAlignment(SwingConstants.CENTER);
		lblMargin.setForeground(SystemColor.menu);
		lblMargin.setBackground(UIManager.getColor("CheckBox.darkShadow"));
		mainPanel.add(lblMargin, "cell 25 4,alignx center");
		
		lBLMargin = new JLabel("12");
		lBLMargin.setForeground(Color.WHITE);
		mainPanel.add(lBLMargin, "cell 26 4,alignx center");
		
		btnDefaultOpt = new JButton("Default Opt");
		btnDefaultOpt.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnDefaultOpt.setFont(new Font("Tahoma", Font.BOLD, 13));
		btnDefaultOpt.setToolTipText(tooltipDefaultOpt);
		btnDefaultOpt.setForeground(SystemColor.menuText);
		btnDefaultOpt.setBackground(new Color(176, 196, 222));
		mainPanel.add(btnDefaultOpt, "cell 2 5 3 1,growx");
		
		comboFileFormat = new JComboBox();
		comboFileFormat.setToolTipText(tooltipComboFileFormat);
		comboFileFormat.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		comboFileFormat.setBackground(Color.gray);
		comboFileFormat.setModel(new DefaultComboBoxModel(SupportedFileTextEditor.values()));
		mainPanel.add(comboFileFormat, "cell 5 5 3 1,growx");
		
		comboStylesScheme = new JComboBox();
		comboStylesScheme.setToolTipText(tooltipComboStyleScheme);
		comboStylesScheme.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		comboStylesScheme.setModel(new DefaultComboBoxModel<>(Themes.values()));
		comboStylesScheme.setSelectedIndex(0);
		comboStylesScheme.setBackground(Color.GRAY);
		mainPanel.add(comboStylesScheme, "cell 8 5 2 1,growx");
		
		btntTextFormat = new JButton("Format");
		btntTextFormat.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btntTextFormat.setFont(new Font("Tahoma", Font.BOLD, 13));
		btntTextFormat.setBackground(new Color(176, 196, 222));
		mainPanel.add(btntTextFormat, "cell 10 5 2 1,growx");
		
		btnRemoveSpaces = new JButton("");
		btnRemoveSpaces.setToolTipText(tooltipRemoveSpaces);
		btnRemoveSpaces.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnRemoveSpaces.setIcon(new ImageIcon(IconsPathConfigurator.ICON_REMOVE_WHITE_LINE));
		btnRemoveSpaces.setBackground(new Color(176, 196, 222));
		mainPanel.add(btnRemoveSpaces, "cell 12 5,alignx center");
		
		btnBackgroundCol = new JButton("");
		btnBackgroundCol.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnBackgroundCol.setToolTipText(tooltipBackgroundColor);
		btnBackgroundCol.setBackground(new Color(176, 196, 222));
		btnBackgroundCol.setIcon(new ImageIcon(IconsPathConfigurator.ICON_TEXT_BG_COLOR));
		mainPanel.add(btnBackgroundCol, "cell 13 5,alignx center");
		
		btnForegroundCol = new JButton("");
		btnForegroundCol.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnForegroundCol.setToolTipText(tooltipForegroundTxt);
		btnForegroundCol.setBackground(new Color(176, 196, 222));
		btnForegroundCol.setIcon(new ImageIcon(IconsPathConfigurator.ICON_TEXT_COLOR));
		mainPanel.add(btnForegroundCol, "cell 14 5,alignx center");
		
		btnFontChooser = new JButton("");
		btnFontChooser.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnFontChooser.setToolTipText(tooltipFontChooser);
		
		btnFontChooser.setBackground(new Color(176, 196, 222));
		btnFontChooser.setIcon(new ImageIcon(IconsPathConfigurator.ICON_TEXT_FONT));
		mainPanel.add(btnFontChooser, "cell 15 5,alignx center"); 
		
		btnCrypt = new JButton("");
		btnCrypt.setHorizontalAlignment(SwingConstants.RIGHT);
		btnCrypt.setHorizontalTextPosition(SwingConstants.RIGHT);
		btnCrypt.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnCrypt.setIcon(new ImageIcon(IconsPathConfigurator.ICON_CRYPT));
		btnCrypt.setToolTipText(tooltipCrypt);
		btnCrypt.setFont(new Font("Tahoma", Font.BOLD, 12));
		btnCrypt.setForeground(SystemColor.inactiveCaption);
		mainPanel.add(btnCrypt, "cell 16 5,alignx right");
		
		btnDecrypt = new JButton("");
		btnDecrypt.setHorizontalTextPosition(SwingConstants.CENTER);
		btnDecrypt.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnDecrypt.setIcon(new ImageIcon(IconsPathConfigurator.ICON_DECRYPT));
		btnDecrypt.setToolTipText(tooltipDecrypt);
		btnDecrypt.setForeground(SystemColor.inactiveCaption);
		btnDecrypt.setFont(new Font("Tahoma", Font.BOLD, 12));
		mainPanel.add(btnDecrypt, "cell 17 5,alignx center");
		
		chckbxAesShowKey = new JCheckBox("Show key");
		chckbxAesShowKey.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		chckbxAesShowKey.setForeground(Color.WHITE);
		chckbxAesShowKey.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		mainPanel.add(chckbxAesShowKey, "cell 18 5 2 1,alignx center");
		
		btnTemplates = new JButton("Templates");
		btnTemplates.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnTemplates.setToolTipText(tooltipTemplates);
		btnTemplates.setFont(new Font("SansSerif", Font.BOLD, 12));
		btnTemplates.setBackground(new Color(176, 196, 222));
		mainPanel.add(btnTemplates, "cell 20 5,growx");
		
		btnFindText = new JButton("");
		btnFindText.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnFindText.setToolTipText(tooltipFindText);
		btnFindText.setIcon(new ImageIcon(IconsPathConfigurator.ICON_SEARCH));
		btnFindText.setBackground(SystemColor.activeCaption);
		btnFindText.setFont(new Font("SansSerif", Font.BOLD, 12));
		btnFindText.setBackground(new Color(176, 196, 222));
		mainPanel.add(btnFindText, "cell 21 5,alignx center");
		
		btnOpenInScreepT = new JButton("");
		btnOpenInScreepT.setToolTipText(tooltipOpenInScreept);
		btnOpenInScreepT.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnOpenInScreepT.setIcon(new ImageIcon(IconsPathConfigurator.ICON_SCREEPT));
		btnOpenInScreepT.setFont(new Font("SansSerif", Font.BOLD, 12));
		btnOpenInScreepT.setBackground(new Color(176, 196, 222));
		mainPanel.add(btnOpenInScreepT, "cell 22 5,alignx center");
		
		btnReplaceEscapesSeq = new JButton("");
		btnReplaceEscapesSeq.setToolTipText(tooltipReplaceEscapes);
		btnReplaceEscapesSeq.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnReplaceEscapesSeq.setIcon(new ImageIcon(IconsPathConfigurator.ICON_CONVERT_ESCAPES));
		btnReplaceEscapesSeq.setFont(new Font("SansSerif", Font.BOLD, 12));
		btnReplaceEscapesSeq.setBackground(new Color(176, 196, 222));
		mainPanel.add(btnReplaceEscapesSeq, "cell 23 5,alignx center");
		
//		textScrollPaneArea = new JScrollPane();
//		textScrollPaneArea.setViewportBorder(new EmptyBorder(0, 0, 0, 0));
//		textScrollPaneArea.setBackground(new Color(112, 128, 144));
//		textScrollPaneArea.setBorder(new EmptyBorder(0, 0, 0, 0));
//		textScrollPaneArea.setViewportView(textArea_1);
//		textScrollPaneArea.setFont(new Font("DejaVu Sans", Font.PLAIN, 18));
		
		JPanel panel = new JPanel();
		panel.setForeground(SystemColor.menu);
		panel.setBorder(new LineBorder(new Color(0, 0, 0), 4));
		panel.setBackground(new Color(100, 149, 237));
		mainPanel.add(panel, "cell 0 0 28 1,growx,aligny center");
		panel.setLayout(new MigLayout("",
				"[100px,grow 10000,left][][][][][][][][][][][][][][][][][][][][][][grow][grow][]", "[]"));
		
		checkBoxInlineTabs = new JCheckBox("Inline Tabs");
		checkBoxInlineTabs.setSelected(true);
		checkBoxInlineTabs.setToolTipText(tooltipInlineTabs);
		checkBoxInlineTabs.setCursor(GuiUtils.CURSOR_HAND);
		checkBoxInlineTabs.setForeground(SystemColor.menu);
		checkBoxInlineTabs.setBackground(Color.DARK_GRAY);
		panel.add(checkBoxInlineTabs, "cell 22 0");

		chkCutWordsTo = new JCheckBox("Adapt Text Width");
		chkCutWordsTo.setCursor(GuiUtils.CURSOR_HAND);
		panel.add(chkCutWordsTo, "cell 23 0");
		chkCutWordsTo.setToolTipText(tooltipCutWordsTo);
		chkCutWordsTo.setSelected(true);
		chkCutWordsTo.setForeground(SystemColor.control);
		chkCutWordsTo.setBackground(Color.DARK_GRAY);

		chkFitmantainwords = new JCheckBox("Don't cut words");
		chkFitmantainwords.setCursor(GuiUtils.CURSOR_HAND);
		panel.add(chkFitmantainwords, "cell 24 0");
		chkFitmantainwords.setToolTipText(tooltipFitMaintainWords);
		chkFitmantainwords.setForeground(SystemColor.control);
		chkFitmantainwords.setBackground(Color.DARK_GRAY);
		chkFitmantainwords.setEnabled(false);

		mainTabList = new JTabbedPane(JTabbedPane.TOP);
		mainTabList.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
		mainTabList.setBackground(Color.DARK_GRAY);
		mainTabList.setMaximumSize(new Dimension(parentFrame.getWidth()-50, parentFrame.getHeight()-280));	
		mainTabList.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
				
		lblFontSize = new JLabel("Font size: ");
		lblFontSize.setHorizontalAlignment(SwingConstants.CENTER);
		lblFontSize.setForeground(SystemColor.menu);
		mainPanel.add(lblFontSize, "cell 25 5,alignx center");

		lblSIZE = new JLabel("");
		lblSIZE.setForeground(SystemColor.window);
		mainPanel.add(lblSIZE, "cell 26 5,alignx center");
		lblSIZE.setText(String.valueOf(DEFAULT_FONT +" Px"));
		
		separator_2 = new JSeparator();
		separator_2.setBackground(Color.GRAY);
		mainPanel.add(separator_2, "cell 0 6 28 1,growx");
		
//		lineNumberPanel = new TextLineNumber(textArea_1, 3);
//		lineNumberPanel.setCurrentLineForeground(new Color(255, 80, 80));
//		lineNumberPanel.setAutoscrolls(true);
//		lineNumberPanel.setDigitAlignment(0.5f);
//		lineNumberPanel.setBorderGap(5);
//		lineNumberPanel.setCurrentLineForeground(Color.orange);
//		lineNumberPanel.setFont(new Font("SansSerif", Font.PLAIN, 16));
//		lineNumberPanel.setUpdateFont(false);
//		lineNumberPanel.setBorder(new LineBorder(Color.GRAY,2));
//		lineNumberPanel.setForeground(Color.gray);
//		lineNumberPanel.setBackground(new Color(50, 50, 50));
		
		mainPanel.add(mainTabList, "cell 0 7 28 2,grow");
		addTab(null, true); // comment for editing in windowuilder
		
		sliderFontSize = new JSlider(4,100);
		sliderFontSize.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
		sliderFontSize.setForeground(SystemColor.activeCaptionBorder);
		sliderFontSize.setBackground(UIManager.getColor("Button.shadow"));
		sliderFontSize.setValue(DEFAULT_FONT.getSize());
		mainPanel.add(sliderFontSize, "cell 27 5,growx");
		
		lblNewLabel = new JLabel("\u2193  (AES KEY)  \u2193");
		lblNewLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
		lblNewLabel.setForeground(new Color(211, 211, 211));
		mainPanel.add(lblNewLabel, "cell 16 2 4 1,alignx center");
		
		JSeparator separator_1 = new JSeparator();
		mainPanel.add(separator_1, "cell 0 3 28 1,growx");
		
		txtAesKey = new JPasswordField();
		txtAesKey.setHorizontalAlignment(SwingConstants.CENTER);
		txtAesKey.setText("");
		mainPanel.add(txtAesKey, "cell 16 4 4 1,growx");
		txtAesKey.setColumns(10);
		txtAesKey.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		txtAesKey.setEchoChar(DEFAULT_PSW_CHAR);
		AesCipher.check(txtAesKey);
		
		lblTextEditor = new JLabel("Text Editor");
		panel.add(lblTextEditor, "cell 0 0,alignx left");
		lblTextEditor.setHorizontalAlignment(SwingConstants.CENTER);
		lblTextEditor.setForeground(Color.BLACK);
		lblTextEditor.setFont(new Font("Lithos Pro Regular", Font.BOLD, 19));
		lblTextEditor.setBackground(SystemColor.activeCaptionBorder);
		
		slider_margin = new JSlider(0,100,0);
		slider_margin.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
		slider_margin.setBackground(UIManager.getColor("Button.shadow"));
		mainPanel.add(slider_margin, "cell 27 4,growx");
		
		JSeparator separator = new JSeparator();
		mainPanel.add(separator, "cell 0 1 28 1,growx");
		
		btnLoadTextFile = new JButton("");
		btnLoadTextFile.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnLoadTextFile.setToolTipText(tooltipLoadTextFile);
		btnLoadTextFile.setForeground(SystemColor.menuText);
		btnLoadTextFile.setBackground(SystemColor.inactiveCaption);
		btnLoadTextFile.setIcon(new ImageIcon(IconsPathConfigurator.ICON_FILE_LOAD));
		mainPanel.add(btnLoadTextFile, "cell 0 4 2 1,grow");
		
		btnDark = new JButton("");
		btnDark.setMargin(new Insets(0, 0, 0, 0));
		btnDark.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnDark.setToolTipText(tooltipDarkSwitch);
		btnDark.setFont(new Font("Tahoma", Font.BOLD, 13));
		mainPanel.add(btnDark, "cell 0 5 2 1,grow");
		
		addHandlers();
		
		// add custom keyboard listener
		shortcutsListener = getKeyEventHandler();
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(shortcutsListener);
		
		startAesKeyCheckThread();
		
		initGuiFromConfig();
		
		loadOpenedFilesAtStart();
		
		loadedTabObj.textArea.requestFocusInWindow();
		
		mainTabList.setUI(new CustomTabbedPaneUI_v1(null, null, null, mainTabList, TabUtils.TAB_BUTTON_CLOSE_ORDER));
		
		logger.debug("### TEXT WRITER module started");

	}
	
	private void startAesKeyCheckThread() {
		// this thread will check every 0.2s the value contained in the textfield
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				while(true) {
					AesCipher.check(txtAesKey);				
					try {
						Thread.sleep(400);
					} catch (InterruptedException e) {
						logger.error(e);
					}
				}
			}
		}).start();
	}
	
	/**
	 * This method adds handlers to action-related Components
	 * (Use this after initialization)
	 */
	private void addHandlers() {
		
		// COLORS DEFAULT DEFINITIONS
		final Font DEFAULT = DEFAULT_FONT;
		final Color DEFAULT_BCKG_COL = currentConfiguration.getTextEditorOpt().backCol; 
		final Color DEFAULT_FORE_COL = currentConfiguration.getTextEditorOpt().foreCol;
		
		// resizeListener tabListContainer
		parentFrame.addComponentListener(new ComponentListener() {
			public void componentShown(ComponentEvent e) {
			}
			public void componentResized(ComponentEvent e) {
				mainTabList.setMaximumSize(new Dimension(parentFrame.getWidth(), parentFrame.getHeight()-280));	
			}
			public void componentMoved(ComponentEvent e) {
			}
			public void componentHidden(ComponentEvent e) {
			}
		});
		
		// OPEN IN SCREEPT CRYPTER
		btnOpenInScreepT.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				CrypterPanel cipherPanel = mainApplication.getPanelCrypter();
				
				// current file not saved
				if(loadedTabObj.unsaved) {
					String currFileName = (loadedTabObj.loadedFile != null)? loadedTabObj.loadedFile.getName() : lblCurrentFile.getText();
					int choice = JOptionPane.showConfirmDialog(currentTextEditor, currFileName + " is not saved, do you want to save it before to crypt?");
					if (choice == JOptionPane.OK_OPTION) {
						btnSaveTextFile.doClick();
					}else if (choice == JOptionPane.CANCEL_OPTION) {
						return;
					}
				}
				
				// current file not exists or null
				if(loadedTabObj.loadedFile == null || !loadedTabObj.loadedFile.exists()) {
					return;
				}
				
				if(cipherPanel != null) {
					int openedFilesCrypter = cipherPanel.getPanelVDragNDropPane().getTreeNodes().length;
					if (openedFilesCrypter != 0) {
						String msg = "There are "+openedFilesCrypter+" loaded files in ScreepT: do you want to remove from loaded list?";
						int choice = JOptionPane.showConfirmDialog(currentTextEditor, msg);
						if(choice == JOptionPane.OK_OPTION) {
							cipherPanel.getPanelVDragNDropPane().removeAll();
						}else if(choice == JOptionPane.CANCEL_OPTION) {
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
						if (TextEditorListener.flagCtrlPressed) {
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
					JFrame panel = new EscapeReplacerPanel(loadedTabObj.textArea, btnTemplates);
					panel.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					panel.setBounds(750, 140, 650, 230);
					panel.setAlwaysOnTop(true);
					panel.setVisible(true);
					panel.setTitle("TEMPLATES manager - " + mainGUI.APPLICATION_NAME);
					btnTemplates.setEnabled(false);
				}
			}
		});
		
		// IMAGE PARSE INJECTOR
		btnParseImageText.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				if(imageParserBox == null || !ImageSelector2TextFrame.active) {
					imageParserBox = new ImageSelector2TextFrame(parentFrame, loadedTabObj.textArea, mainGUI.APPLICATION_NAME+" - Image2Text");
					imageParserBox.setVisible(true);
				}else {
					imageParserBox.toFront();
					imageParserBox.requestFocus();
				}
			}
		});
		
		// PDF PARSER INJECTOR
		btnParsePdfText.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(pdfParserBox == null || !PdfSelector2TextFrame.active) {
					pdfParserBox = new PdfSelector2TextFrame(parentFrame, loadedTabObj.textArea, mainGUI.APPLICATION_NAME+" - Pdf2Text");
					pdfParserBox.setVisible(true);
				}else {
					pdfParserBox.toFront();
					pdfParserBox.requestFocus();
				}
			}
		});
		
		// SEARCH TEXT
		btnFindText.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(searchBox == null || !searchBox.active) {
					searchBox = new SearchFrame(true, loadedTabObj.textArea);
					GuiUtils.setFrameIcon(IconsPathConfigurator.ICON_SEARCH, searchBox);
					searchBox.setVisible(true);
					GuiUtils.centerComponent(searchBox, 450, 145);
					searchBox.active = true;
				}else {
					searchBox.active = true;
					searchBox.toFront();
					searchBox.requestFocus();
				}
				
			}
		});
		
		// SHOW/HIDE PASSWORD DISPLAYED
		chckbxAesShowKey.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (chckbxAesShowKey.isSelected()) {
					txtAesKey.setEchoChar((char) 0);
				}else {
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
				}else {
					currentConfiguration.getTextEditorOpt().inlinedTabs = false;
					mainTabList.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
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
//							loadedTabObj.textArea.getDocument().removeDocumentListener(textAreaChangedListener);
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
//							textArea_1.getDocument().addDocumentListener(textAreaChangedListener);
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
//							textArea_1.getDocument().removeDocumentListener(textAreaChangedListener);
							loadedTabObj.textArea.append(decoded);
//							textArea_1.getDocument().addDocumentListener(textAreaChangedListener);
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
				lblSIZE.setText(String.valueOf(loadedTabObj.textArea.getFont().getSize())+" Px");

				updateTxtConfig(DEFAULT, DEFAULT_BCKG_COL, DEFAULT_FORE_COL, 10);
			}
		});
		
		//BACKGROUND TEXT COLOR
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
		

		//FOREGROUND TEXT COLOR
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
				lblSIZE.setText(String.valueOf(loadedTabObj.textArea.getFont().getSize())+" Px");	
				RsyntaxUtils.setFont(loadedTabObj.textArea, loadedTabObj.textArea.getFont());
				currentConfiguration.getTextEditorOpt().setFont(loadedTabObj.textArea.getFont());

				btnFontChooser.setEnabled(true);
				
			}
		});
				
		// SWITCH BETWEEN DARK/LIGHT COLORS FOR TEXT AREA
		btnDark.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(loadedTabObj.textArea.getForeground()==Color.BLACK || loadedTabObj.textArea.getBackground()==DEFAULT_BCKG_COL || loadedTabObj.textArea.getBackground()==Color.WHITE) {
					loadedTabObj.textArea.setForeground(new Color(235,235,235));
					RsyntaxUtils.setForeground(loadedTabObj.textArea, new Color(235,235,235));
					loadedTabObj.textArea.setBackground(Color.BLACK);
					RsyntaxUtils.setBackground(loadedTabObj.textArea,Color.BLACK);
					btnDark.setIcon(IconsPathConfigurator.IMG_ICON_LIGHT_ON);
				 }else {
					loadedTabObj.textArea.setForeground(Color.BLACK);
					RsyntaxUtils.setForeground(loadedTabObj.textArea, Color.BLACK);
					loadedTabObj.textArea.setBackground(SystemColor.controlHighlight);	
					RsyntaxUtils.setBackground(loadedTabObj.textArea,SystemColor.controlHighlight);
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
				font = new Font(font.getName(), font.getStyle(), sliderFontSize.getValue() );
				loadedTabObj.textArea.setFont(font);
				lblSIZE.setText(String.valueOf(loadedTabObj.textArea.getFont().getSize())+" Px");			
				currentConfiguration.getTextEditorOpt().setFont(loadedTabObj.textArea.getFont());
			}
		});
		
		// SET SPEAKER'S VOICE SPEED
		sliderSpokenSpeed.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				int rate = sliderSpokenSpeed.getValue();
				currentConfiguration.getTextEditorOpt().setVoiceRate(rate);
				if(objSpeaker!=null) {
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
						if(objSpeaker!=null) {
							objSpeaker.getVoice().deallocate();
							btnSpeak.setBackground(new Color(176, 196, 222));
						}else {
							// else speak
							objSpeaker=new Text2Speech(); 
							btnSpeak.setBackground(Color.RED);
							objSpeaker.dospeak(loadedTabObj.textArea.getText(),"kevin16", (sliderSpokenSpeed.getValue()/100.0f)); 
							objSpeaker=null;
							btnSpeak.setBackground(new Color(176, 196, 222));
						}						
					}
				}).start();
			}
		});
		
		// UNDO LAST OPERATION ON TEXT
		btnUndo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(loadedTabObj.textArea.canUndo()) {
					loadedTabObj.textArea.undoLastAction();
				}
			}
		});
		
		// REDO LAST OPERATION ON TEXT
		btnRedo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				if(loadedTabObj.textArea.canRedo()) {
					loadedTabObj.textArea.redoLastAction();
				}
				
			}
		});
		
		// CLEAR TEXT AREA
		btnClear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				if(JOptionPane.showConfirmDialog(parentFrame, "Clear Text?")==JOptionPane.OK_OPTION) {
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

				if(txtAreaDto.selected) {
					result = txtAreaDto.prefix.concat(HandleString.lowerAfterDots(txtAreaDto.selection, '.').concat(txtAreaDto.end));
				}else {
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
				
				if(txtAreaDto.selected) {
					result = txtAreaDto.prefix.concat(HandleString.upperAfterDots(txtAreaDto.selection, '.').concat(txtAreaDto.end));
				}else {
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
				boolean unsaved = loadedTabObj.unsaved;
				
				SupportedFileTextEditor selected = (SupportedFileTextEditor) comboFileFormat.getSelectedItem();
				loadedTabObj.textArea.setSyntaxEditingStyle(selected.syntax);
				loadedTabObj.textArea.revalidate();
				CURRENT_EXT_OUT = selected.nomeStringa;
				infoAdvice("[Current output: "+CURRENT_EXT_OUT+"]");
				btnSaveTextFileAs.setToolTipText("Choose name and folder, save "+CURRENT_EXT_OUT+" file!");
				
				loadedTabObj.unsaved = unsaved;
				updateSavedLabelNTab(null);
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
					JFrame panel = new TemplatesChoosePanel(loadedTabObj.textArea, comboFileFormat, btnTemplates);
					panel.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					panel.setBounds(750, 140, 500, 170);
					panel.setAlwaysOnTop(true);
					panel.setVisible(true);
					panel.setTitle("TEMPLATES manager - " + mainGUI.APPLICATION_NAME);
					btnTemplates.setEnabled(false);
				}
			}
		});
		
		// ADAPT TO WIDTH MAINTAINING WORDS
		chkFitmantainwords.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(chkFitmantainwords.isSelected()) {
					loadedTabObj.textArea.setWrapStyleWord(true);
				}else {
					loadedTabObj.textArea.setWrapStyleWord(false);
				}
			}
		});		
		
		// ADAPT TO WIDTH
		chkCutWordsTo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(chkCutWordsTo.isSelected()) {
					loadedTabObj.textArea.setLineWrap(true);
					chkFitmantainwords.setEnabled(true);
				}else {
					loadedTabObj.textArea.setLineWrap(false);
					loadedTabObj.textArea.setWrapStyleWord(false);
					chkFitmantainwords.setEnabled(false);
				}
			}
		});
		
		// MARGIN TEXT AREA SELECTOR
		slider_margin.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
					textMargin=slider_margin.getValue();
					lBLMargin.setText(""+textMargin);
					loadedTabObj.textArea.setMargin(new Insets(textMargin/2, textMargin, textMargin/2, textMargin));
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
				if(TextEditorListener.flagCtrlPressed) {
					// remove character - open panel ad hoc
					String selection = JOptionPane.showInputDialog("Specify the charachter/string you want to remove");
					if (selection != null && !selection.equals("")) {
						newEnd = HandleString.removeCharachterFromTxtArea(selection, loadedTabObj.textArea, start, end);
					}
					TextEditorListener.flagCtrlPressed = false;
				}else {
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
				 if(!txtFont.isItalic()) {
					 loadedTabObj.textArea.setFont(txtFont.deriveFont(Font.ITALIC, txtFont.getSize()));
				 }else {
					 loadedTabObj.textArea.setFont(txtFont.deriveFont(Font.PLAIN, txtFont.getSize()));
				 }
				 currentConfiguration.getTextEditorOpt().setFont(txtFont);
			}
		});
		
		// MAKE TEXT BOLD
		btnBold.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Font txtFont = loadedTabObj.textArea.getFont();
				 if(!txtFont.isBold()) {
					 loadedTabObj.textArea.setFont(txtFont.deriveFont(Font.BOLD, txtFont.getSize()));
				 }else {
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
				String start="";
				String end = "";
				Boolean none = false;
				int strtN=0;
				int endN=text.length();
				if(selected==null) {
					//select ALL if no selection
					none=true;
					selected = new String(text);
				}else {
					strtN=loadedTabObj.textArea.getSelectionStart();
					endN=loadedTabObj.textArea.getSelectionEnd();
					start = textBackup.substring(0, strtN);
					end = textBackup.substring(endN, textBackup.length());
				}				
				backupText = new String(selected);// create backup copy by value
				
				if(!uppercase) {
					selected = selected.toUpperCase();
					uppercase=true;
				}
				else {
					selected = selected.toLowerCase();
					uppercase=false;
				}
				
				selected = start + selected + end;
				loadedTabObj.textArea.setText(selected);
				if(!none) {
					loadedTabObj.textArea.requestFocus();
					loadedTabObj.textArea.setSelectionStart(strtN);
					loadedTabObj.textArea.setSelectionEnd(endN);
				}
				logger.debug("TT function start selection: "+strtN);
				logger.debug("TT function start selection: "+endN);
			}
		});
		
		// MAKE EVERY WORD STARTING WITH UPPERCASE LETTER
		btnTtTt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				String text = loadedTabObj.textArea.getText();
				String textBackup = new String(text);
				String selected = loadedTabObj.textArea.getSelectedText();
				String start="";
				String end = "";
				int strtN=0;
				int endN=text.length();
				if(selected==null) {
					//select ALL if no selection
					selected = new String(text);
				}else {
					strtN=loadedTabObj.textArea.getSelectionStart();
					endN=loadedTabObj.textArea.getSelectionEnd();
					start = textBackup.substring(0, strtN);
					end = textBackup.substring(endN, textBackup.length());
				}				
				backupText = new String(selected);// create backup copy by value
				
				selected = HandleString.uppercaseEveryFirstLetterNoSpace(selected);
				if (selected.equals(backupText)) {
					selected = HandleString.lowerCaseEveryFirstLetterNoSpace(selected);
//							btnTtTt.setBackground(SystemColor.activeCaptionBorder);
				}else {
//							btnTtTt.setBackground(Color.gray);
				}
				selected = start + selected + end;
				loadedTabObj.textArea.setText(selected);
				loadedTabObj.textArea.requestFocus();
				loadedTabObj.textArea.setSelectionStart(strtN);
				loadedTabObj.textArea.setSelectionEnd(endN);
				logger.debug("Tt Tt function start selection: "+strtN);
				logger.debug("Tt Tt function start selection: "+endN);
				
			}
		});
		
		// SAVE TEXT FILE
		btnSaveTextFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean done = false;
				if(loadedTabObj.loadedFile == null || !loadedTabObj.loadedFile.exists()) {
					btnSaveTextFileAs.doClick();
					return;
				}else {
					try {
						done = FileStuff.writeStringToFile(loadedTabObj.textArea.getText(), loadedTabObj.loadedFile, true);
					} catch (IOException e1) {
						JOptionPane.showMessageDialog(currentTextEditor, "error Saving the selected file :(");
						logger.error("Exception happened!", e1);
					}
					if(done) {
						loadedTabObj.unsaved = false;
						updateSavedLabelNTab(null);
						refreshMessageAndTabTooltip("[File saved]");
					}else {
						errorAdvice("[Error - not saved]");
						lblCurrentFile.setText("(*)"+loadedTabObj.loadedFile.getName());
						lblCurrentFile.setForeground(Color.RED);
					}
					lblCurrentFile.setToolTipText(loadedTabObj.loadedFile.getAbsolutePath());
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
					try {
						File selectedFile = GenericFileChooserDialog.fileWrite(parentFrame, currentConfiguration, "Save " + CURRENT_EXT_OUT + " File");
						
						if (selectedFile != null) {
							selectedFile = adaptFileExt(selectedFile);
							if (selectedFile.exists()) {
								if (JOptionPane.showConfirmDialog(parentFrame, "File " + selectedFile.getName()
										+ " exists, overwrite it?") == JOptionPane.OK_OPTION) {
									done = FileStuff.writeStringToFile(loadedTabObj.textArea.getText(), selectedFile, true);
								} else {
									done = FileStuff.writeStringToFile(loadedTabObj.textArea.getText(), selectedFile, false);
								}
							} else {
								done = selectedFile.createNewFile();
								PrintWriter out = new PrintWriter(selectedFile.getAbsolutePath());
								out.write(loadedTabObj.textArea.getText());
								out.close();
							}
							if (done) {
								JOptionPane.showMessageDialog(parentFrame,"File " + selectedFile.getName() + " succesfully saved!");
								loadedTabObj.loadedFile = selectedFile;
								loadedTabObj.unsaved = false;
								updateSavedLabelNTab(null);
								lblCurrentFile.setText(loadedTabObj.loadedFile.getName());
								lblCurrentFile.setToolTipText(loadedTabObj.loadedFile.getAbsolutePath());
								refreshMessageAndTabTooltip("[File saved]");
								currentConfiguration.getFileOpt()
										.setLastDstFolderPath(loadedTabObj.loadedFile.getParentFile().getAbsolutePath());
							} else {
								errorAdvice("[Error Saving selected file]");
							} 
						}
					} catch (IOException e1) {
						errorAdvice("[Error Saving selected file]");
						JOptionPane.showMessageDialog(currentTextEditor, "error Saving the selected file :(");
						logger.error("Exception happened!", e1);
					}
				}
				// update signal value
				alreadySaving = false;
			}
		});
		
		// RELOAD INTO TEXT AREA FROM DISK
		btnReloadFromDisk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				loadedTabObj.textArea.setCursor(GuiUtils.CURSOR_WAIT);
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						if (!alreadyLoading && loadedTabObj.loadedFile != null && loadedTabObj.loadedFile.exists()) {
							if (loadedTabObj.unsaved && JOptionPane.showConfirmDialog(currentTextEditor, "This document is not saved, you will lose all unsaved changes. Continue?") == JOptionPane.YES_OPTION) {
								alreadyLoading = true;
								loadedTabObj.textArea.setText("");
								FileStuff.appendFileTxtArea(loadedTabObj.loadedFile, loadedTabObj.textArea, true);
								loadedTabObj.unsaved = false;
								updateSavedLabelNTab(null);
								refreshMessageAndTabTooltip("[File Reloaded]");
								alreadyLoading = false;
							}
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
//					textArea_1.getDocument().removeUndoableEditListener(loadedTabObj.undoManager);
					alreadyLoading = true;
					
					File source = GenericFileChooser.fileRead(currentTextEditor);
					if (source == null || !source.exists()) {

					} else {
						
						// add new tab with selected file
						try {
							addTab(source, false);
						} catch (Throwable e1) {
							e1.printStackTrace();
						}

						loadedTabObj.loadedFile = source;
						lblCurrentFile.setText(loadedTabObj.loadedFile.getName());
						lblCurrentFile.setToolTipText(loadedTabObj.loadedFile.getAbsolutePath());
						updateSavedLabelNTab(null);
						refreshMessageAndTabTooltip("[File loaded]");
						currentConfiguration.getFileOpt().setLastSrcFolderPath(source.getParentFile().getAbsolutePath());
					}
					
					alreadyLoading = false;
				}// end if !already loading
			}
		});
		
		
		// ADD NEW TAB
		btnNewTab.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				addTab(null, false);
			}
		});
		
		// TAB CHANGE LISTENER 
		mainTabList.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
//				textArea_1.getDocument().removeUndoableEditListener(loadedTabObj.undoManager);
				if (lastIndexTabbedPane != mainTabList.getSelectedIndex()) {
					tabRefreshAfterUpdate();
					lastIndexTabbedPane = mainTabList.getSelectedIndex();
				}
			}
		});
	}
	
	public static void fileChangedLblUpdate() {
		
		int indexChange = mainTabList.getSelectedIndex();
		try {
			if (!tabJustSwitched && indexChange >= 0 && !isCrypterOn && !alreadyLoading) {
				loadedTabObj.unsaved = true;
				updateSavedLabelNTab(null);
			} 
		} catch (Exception e) {
			logger.error(e);
		}
	}
		
	private void refreshMessageAndTabTooltip(String message) {
		successAdvice(message);
		updateTabTooltip(mainTabList.getSelectedIndex());
		lblCurrentFile.setForeground(ImageGeneralWorker.randomColorRGB(0.2f));
	}
	
	/** specify NULL sourceIndex to use current selected tab, or a custom value > 0 
	 * to specify the index of tab to take unsaved value
	 */
	private static void updateSavedLabelNTab(Integer sourceIndex) {
		int indexChange = (sourceIndex == null)? mainTabList.getSelectedIndex() : sourceIndex;
		try {
			// reference to tabHeader title
			JLabel tabHeadLbl = (JLabel)(getPanelFromTabHeader(indexChange)).getComponent(TabUtils.TAB_TITLE_ORDER);
			// IF ALREADY SAVED
			if (indexChange >= 0 && !loadedTabObj.unsaved) {
				// IF FILE IS DEFINED
				if (loadedTabObj.loadedFile != null ) {
					lblCurrentFile.setText(loadedTabObj.loadedFile.getName());
					lblCurrentFile.setToolTipText(loadedTabObj.loadedFile.getAbsolutePath());
					tabHeadLbl.setText(HandleString.fitToMaxLentgh(loadedTabObj.loadedFile.getName(), MAX_TAB_LABELENGHT));
				}else {
				// IF FILE IS NOT DEFINED
					if(tabHeadLbl.getText().startsWith("(*)")) {
						tabHeadLbl.setText(HandleString.fitToMaxLentgh(tabHeadLbl.getText().substring(3, tabHeadLbl.getText().length()), MAX_TAB_LABELENGHT));
						if(lblCurrentFile.getText().startsWith("(*)")){
							lblCurrentFile.setText(lblCurrentFile.getText().substring(3, lblCurrentFile.getText().length()));
						}
					}else {
						tabHeadLbl.setText(HandleString.fitToMaxLentgh(tabHeadLbl.getText(), MAX_TAB_LABELENGHT));
						lblCurrentFile.setText(tabHeadLbl.getText());
					}
				}
				// UPDATE ICON
				if (tabHeadLbl.getText().equals(DEFAULT_NEW_FILE_NAME) && ("".equals(loadedTabObj.textArea.getText()) || alreadyLoading)) {
					setIconToTab(IconsPathConfigurator.ICON_DOC_EMPTY, indexChange);
				}else {
					setIconToTab(IconsPathConfigurator.ICON_DOC_SAVED, indexChange);
				}
			// ELSE IF NOT YET SAVED
			}else if (indexChange >= 0 && loadedTabObj.unsaved) {
				// IF FILE IS DEFINED
				if (loadedTabObj.loadedFile != null ) {
					tabHeadLbl.setText(HandleString.fitToMaxLentgh("(*)"+loadedTabObj.loadedFile.getName(), MAX_TAB_LABELENGHT));
					lblCurrentFile.setText("(*)"+loadedTabObj.loadedFile.getName());
					lblCurrentFile.setToolTipText(loadedTabObj.loadedFile.getAbsolutePath());
				}else {
				// IF FILE IS NOT DEFINED
					lblCurrentFile.setText(tabHeadLbl.getText());
					if(!tabHeadLbl.getText().startsWith("(*)")) {
						tabHeadLbl.setText(HandleString.fitToMaxLentgh("(*)"+tabHeadLbl.getText(), MAX_TAB_LABELENGHT));
					}
					if(!lblCurrentFile.getText().startsWith("(*)")){
						lblCurrentFile.setText(tabHeadLbl.getText());
					}
				}
				// UPDATE ICON
				setIconToTab(IconsPathConfigurator.ICON_DOC_UNSAVED, indexChange);
			}
		} catch (Exception e) {
			logger.error(e);
		}
	}
	
	private boolean askSave(int index) {
		int askSave = JOptionPane.CANCEL_OPTION;
		TabTextEditorObj selected = loadedList.get(index);
		if (selected.unsaved) {
			askSave = JOptionPane.showConfirmDialog(currentTextEditor, "Do you want to save current file?",
					"Save current file?", JOptionPane.YES_NO_CANCEL_OPTION);
		}else {
			return true;
		}
		if(askSave == JOptionPane.OK_OPTION){

			if(loadedTabObj.loadedFile != null) {
				currentTextEditor.btnSaveTextFile.doClick();
			}else {
				currentTextEditor.btnSaveTextFileAs.doClick();
			}
			return !loadedTabObj.unsaved;
		}else if(askSave == JOptionPane.NO_OPTION) {
			return true;
		}
		return false;
	}
	
	public void initGuiFromConfig(){
		TextEditorOption txtOptions = currentConfiguration.getTextEditorOpt();
		
		checkBoxInlineTabs.setSelected(txtOptions.isInlinedTabs());
		slider_margin.setValue(txtOptions.getMargin());
		sliderFontSize.setValue(txtOptions.getFont().getSize());
		sliderSpokenSpeed.setValue(txtOptions.getVoiceRate());
		loadedTabObj.textArea.setBackground(txtOptions.getBackCol());
		RsyntaxUtils.setBackground(loadedTabObj.textArea, txtOptions.backCol);
		loadedTabObj.textArea.setForeground(txtOptions.getForeCol());
		RsyntaxUtils.setForeground(loadedTabObj.textArea, txtOptions.foreCol);
		loadedTabObj.textArea.setFont(txtOptions.getFont());
		comboStylesScheme.setSelectedItem(currentConfiguration.getTextEditorOpt().selectedTheme);
	}
	

	/**
	 *  KEYBOARD SHORTCUTS - use custom listener
	 */
	private TextEditorListener getKeyEventHandler() {
		return new TextEditorListener(currentTextEditor); 
	}
	
	private void updateTxtConfig(Font DEFAULT, Color DEFAULT_BCKG_COL, Color DEFAULT_FORE_COL, int margin) {
		TextEditorOption txtOpt = currentConfiguration.getTextEditorOpt();
		txtOpt.setFont(DEFAULT);
		txtOpt.setBackCol(DEFAULT_BCKG_COL);
		txtOpt.setForeCol(DEFAULT_FORE_COL);
		txtOpt.setMargin(margin);
	}
	
	private File adaptFileExt(File selectedFile) {
		String givenExtension = FilenameUtils.getExtension(selectedFile.getName());

		// if not specified extension, invalid or equal to checkbox, then change extension to current in global var
		if (("." + givenExtension).equals(CURRENT_EXT_OUT) || givenExtension.equals("") || (("." + givenExtension).length() > 5)) {
			selectedFile = FileStuff.changeExtension(selectedFile, CURRENT_EXT_OUT);
		} else {
			selectedFile = FileStuff.changeExtension(selectedFile, "." + givenExtension);
		}
		
		return selectedFile;
	}
	
	private void timeoutLblAdvice(int millisec) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Thread.sleep(millisec);
					lblAdvice.setText("");
				} catch (InterruptedException e) {
					logger.error("Exception happened!", e);
				}
			}
		}).start();
	}
	
	private void errorAdvice(String msg) {
		lblAdvice.setForeground(Color.RED);
		lblAdvice.setText(msg);
		timeoutLblAdvice(3000);
	}
	
	private void successAdvice(String msg) {
		lblAdvice.setForeground(Color.GREEN);
		lblAdvice.setText(msg);
		timeoutLblAdvice(3000);
	}
	
	private void infoAdvice(String msg) {
		lblAdvice.setForeground(Color.BLUE);
		lblAdvice.setText(msg);
		timeoutLblAdvice(3000);
	}
	
	private void loadOpenedFilesAtStart() {
		loadedTabObj.textArea.setCursor(GuiUtils.CURSOR_WAIT);
		
		boolean loadedAlmostOne = false;
		if (currentConfiguration.getTextEditorOpt().getOpenedFileList().size() != 0) {
			for (String pathToOpen : currentConfiguration.getTextEditorOpt().getOpenedFileList()) {
				File toLoadFile = new File(pathToOpen);
				if(toLoadFile.exists()) {
					addTab(toLoadFile, !loadedAlmostOne);
					if(!loadedAlmostOne) loadedAlmostOne = true;
				}
			} 
		}
		
		if(loadedAlmostOne){
			// first remove first tab if at least one file is loaded
			removeTab(0);
			
			// now try to select the old opened one
			String toOpenAtStart = currentConfiguration.getTextEditorOpt().selectedAtStart;
			try {
				int i = 0;
				for (TabTextEditorObj iteratorObj : loadedList) {
					if (iteratorObj.loadedFile != null && toOpenAtStart.equals(iteratorObj.loadedFile.getAbsolutePath())) {
						mainTabList.setSelectedIndex(i);
						break;
					}
					i++;
				}
			} catch (Exception e) {
				logger.warn("Cannot select default file "+toOpenAtStart, e);
			}
		}
		
		loadedTabObj.textArea.setCursor(GuiUtils.CURSOR_TEXT);
	}
	
	// TABBED EDITOR FUNCTIONS //
	
	public void addTab(File file, boolean first) {
		isAddingTab = true;
		backupCurrentBeforeSwitch();
		String name = (file != null)? file.getName() : DEFAULT_NEW_FILE_NAME;
		int newIndex = mainTabList.getTabCount();

		TabTextEditorObj newTabNode = new TabTextEditorObj(file, "");
		loadedList.add(newTabNode);
		mainTabList.addTab(name, newTabNode.currentScrollPane);
		loadedTabObj = newTabNode;
		FileStuff.appendFileTxtArea(file, loadedTabObj.textArea, true);
		mainTabList.setTabComponentAt(newIndex, getCustomClosableTab(name, newIndex));
		mainTabList.setSelectedIndex(newIndex);
		loadedTabObj.textArea.discardAllEdits();
		loadedTabObj.textArea.setCursor(GuiUtils.CURSOR_TEXT);
		loadedTabObj.unsaved = false;
		isAddingTab = false;
	}
	
	private void removeTab(int index) {
		
		if (askSave(index)) {
			isRemovingTab = true;
			if (index > 0 && mainTabList.getTabCount()>1) {
				loadedList.remove(index);
				mainTabList.remove(index);
				mainTabList.setSelectedIndex(index-1);
			
			}else if(index == 0 && mainTabList.getTabCount()>1) {
				loadedList.remove(0);
				mainTabList.remove(0);
				mainTabList.setSelectedComponent(loadedList.get(0).currentScrollPane);
				tabRefreshAfterUpdate();
				
			}else if(index == 0){
				addTab(null, true);
				loadedList.remove(0);
				mainTabList.remove(0);
				isRemovingTab = false;
			}
			isRemovingTab = false;
		}
		loadedTabObj.textArea.requestFocus();
	}
	
	public void goToPreviousTab() {
		try {
			int index = mainTabList.getSelectedIndex()-1;
			if (index < 0) {
				index = mainTabList.getTabCount()-1;
			}
			mainTabList.setSelectedIndex(index);
		} catch (Exception e) {
			logger.error("Cannot go to previous tab!", e);
		}
	}
	
	public void goToNextTab() {
		try {
			int index = mainTabList.getSelectedIndex()+1;
			if(index >= mainTabList.getTabCount()) {
				index = 0;
			}
			mainTabList.setSelectedIndex(index);
		} catch (Exception e) {
			logger.error("Cannot go to next tab!", e);
		}
	}
	
	public void closeCurrentTab() {
		try {
			JButton closeTabButton = (JButton)(getPanelFromTabHeader(mainTabList.getSelectedIndex())).getComponent(TabUtils.TAB_BUTTON_CLOSE_ORDER);
			closeTabButton.doClick();
		} catch (Exception e) {
			logger.error(e);
		}
	}
	
	private void backupCurrentBeforeSwitch() {
		try {
			TabTextEditorObj toSaveObj = loadedList.get(lastIndexTabbedPane);
			toSaveObj.loadedFile = (loadedTabObj.loadedFile != null)? new File(loadedTabObj.loadedFile.getAbsolutePath()) : null;
			toSaveObj.unsaved = loadedTabObj.unsaved;
		} catch (Exception e) {
			logger.error(e);
		}
	}
	
	private void tabRefreshAfterUpdate() {
		tabJustSwitched = true;
		loadedTabObj.textArea.setCursor(GuiUtils.CURSOR_WAIT);
		
		if (!isAddingTab && !isRemovingTab) {
			backupCurrentBeforeSwitch();
		}
		loadedTabObj = loadedList.get(mainTabList.getSelectedIndex());
		updateSavedLabelNTab(null);
		applyCurrentGraphicsTextArea(loadedTabObj.textArea);
		loadedTabObj.textArea.setCursor(GuiUtils.CURSOR_TEXT);
		tabJustSwitched = false;
		loadedTabObj.textArea.requestFocus();
	}

	private void applyCurrentGraphicsTextArea(JTextArea textArea) {
		textArea.setBackground(currentConfiguration.getTextEditorOpt().backCol);
		textArea.setForeground(currentConfiguration.getTextEditorOpt().foreCol);
		textArea.setFont(currentConfiguration.getTextEditorOpt().font);
		textArea.setLineWrap(chkCutWordsTo.isSelected());
		textArea.setWrapStyleWord(chkFitmantainwords.isSelected());
		int margin = currentConfiguration.getTextEditorOpt().margin;
		textArea.setMargin(new Insets(margin, margin, margin, margin));
		textArea.setTabSize(TEXT_AREA_TAB_SIZE);
		applySelectedTheme();
		
		textArea.revalidate();
		textArea.repaint();
	}
	
	private void applySelectedTheme() {
		// backup unsaved to avoid changing saved info with theme change
		boolean unsaved = loadedTabObj.unsaved;
		
		Themes theme = (Themes)comboStylesScheme.getSelectedItem();
		if(theme.equals(Themes.NONE)) {
			RsyntaxUtils.setForeground(loadedTabObj.textArea, currentConfiguration.getTextEditorOpt().foreCol);
			RsyntaxUtils.setBackground(loadedTabObj.textArea, currentConfiguration.getTextEditorOpt().backCol);
			loadedTabObj.textArea.setHighlightCurrentLine(false);
			loadedTabObj.currentScrollPane.getGutter().setBackground(new Color(30,30,30));
			loadedTabObj.currentScrollPane.getGutter().setForeground(Color.LIGHT_GRAY);
			loadedTabObj.currentScrollPane.getGutter().setFont(DEFAULT_FONT);
		}else {
			loadedTabObj.textArea.setHighlightCurrentLine(true);
			RsyntaxUtils.changeStyleViaThemeXml(loadedTabObj.textArea, theme.defaultPath);
		}
		loadedTabObj.textArea.revalidate();
		loadedTabObj.textArea.repaint();
		
		currentConfiguration.getTextEditorOpt().setSelectedTheme(theme);
		// retore unsaved info backup
		loadedTabObj.unsaved = unsaved;
	}
	
	private static JPanel getPanelFromTabHeader(int index) {
		JPanel tabPanel = null;
		try {
			tabPanel = (JPanel) mainTabList.getTabComponentAt(index);
		} catch (Exception e) {
			logger.error(e);
		}
		return tabPanel;
	}
	
	private static boolean setIconToTab(String iconPath, int index) {
		try {
			JLabel tabHeadIcon = (JLabel)(getPanelFromTabHeader(index)).getComponent(TabUtils.TAB_ICON_ORDER);
			tabHeadIcon.setIcon(new ImageIcon(iconPath));
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	private JPanel getCustomClosableTab(String title, int index) {
		String tooltip = "";
		JScrollPane i_component = loadedList.get(index).currentScrollPane;

		if (loadedTabObj.loadedFile != null && loadedTabObj.loadedFile.exists()) {
			tooltip = loadedTabObj.loadedFile.getAbsolutePath();
		}else {
			tooltip = title;
		}
		
		String icon = "";
		if (DEFAULT_NEW_FILE_NAME.equals(title)) {
			icon = IconsPathConfigurator.ICON_DOC_EMPTY;
		}else {
			icon = IconsPathConfigurator.ICON_DOC_SAVED;
		}
		JPanel customTab = TabUtils.getCustomClosableTab(title, tooltip, icon, getTabCloseListener(index));
		customTab.addMouseListener(new MouseListener() {
			public void mouseReleased(MouseEvent e) {
				mainTabList.setSelectedComponent(i_component);
			}
			public void mousePressed(MouseEvent e) {
			}
			public void mouseExited(MouseEvent e) {
			}
			public void mouseEntered(MouseEvent e) {
			}
			public void mouseClicked(MouseEvent e) {
			}
		});
		
		return customTab;
	}
	
	private void updateTabTooltip(int index) {
		JPanel tabPanel = getPanelFromTabHeader(index);
		if (loadedTabObj.loadedFile != null && loadedTabObj.loadedFile.exists()) {
			tabPanel.setToolTipText(loadedTabObj.loadedFile.getAbsolutePath());
		}else {
			tabPanel.setToolTipText(DEFAULT_NEW_FILE_NAME);
		}
	}
	
	private ActionListener getTabCloseListener(int index) {
		return new ActionListener() {
			JScrollPane i_component = loadedList.get(index).currentScrollPane;
			
			@Override
			public void actionPerformed(ActionEvent e) {
				mainTabList.setSelectedComponent(i_component);
				removeTab(mainTabList.getSelectedIndex());			
			}
		};
	}
	
	private void formatText() {
		boolean error = false;
		String source = new String(loadedTabObj.textArea.getText());
		String old = new String(source);
		
		// XML format handler (if xml formatter works then is a xml recognized file)
		if(CURRENT_EXT_OUT.equals(".xml")) {
			String formatted = HandleString.xmlPrettyFormat(source,4);
			if (!formatted.equals(HandleString.ERROR)) {
				source = HandleString.xmlPrettyFormat(source, 4);
				if (source.equals(old)) {
					// already formatted
					source = HandleString.removeLines(source);
				}
			}else {
				error = true;
				JOptionPane.showMessageDialog(parentFrame, "Error reading xml format, maybe it's not a valid xml String?", "XML format Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		
		// apply formatted to textArea
		if(!error && !source.equals(old)) {
			loadedTabObj.textArea.setText(source);
		}
	}
	
	public static boolean saveAllUnsavedExisting() throws IOException {
		boolean done = true;
		for(TabTextEditorObj element : loadedList) {
			if(element.loadedFile != null && element.loadedFile.exists()) {
				if(!FileStuff.writeStringToFile(loadedTabObj.textArea.getText(), loadedTabObj.loadedFile, true)) {
					done = false;
				}
			}
		}
		return done;
	}
}

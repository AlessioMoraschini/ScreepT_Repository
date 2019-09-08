package gUI.panels;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import net.miginfocom.swing.MigLayout;
import om.SupportedFileTextEditor;
import om.TabTextEditorObj;
import om.TextEditorOption;
import resources.IconsPathConfigurator;
import splitpanel.FileNamed;
import splitpanel.SplitPaneUtils;
import tabbedpaneutils.CustomTabbedPaneUI_v1;
import tabbedpaneutils.TabUtils;
import textareautil.SelectionDto;
import textareautil.TextLineNumber;
import treeutils.TreeUtils;
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

import amfileutils.FileStuff;
import conversion.Text2Speech;
import crypt.aes.AesCipher;
import dialogutils.GenericFileChooser;
import dialogutils.MyFileView;
import enums.Themes;
import frameutils.EscapeReplacerPanel;
import frameutils.FileDragNDropManager;
import frameutils.FilePropertiesFrame;
import frameutils.GuiUtils;
import maingUI.MainGuiGeneralAppStarter;
import maingUI.mainGUI;
import mainobj.INItializer;
import media.ImageGeneralWorker;

import java.awt.SystemColor;

import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
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

import gUI.utils.TextEditorUtils;
import gUI.utils.filedialog.GenericFileChooserDialog;
import gUI.utils.filedialog.TemplatesChoosePanel;
import gUI.utils.shortcutshandler.TextEditorMouseWheelListener;
import gUI.utils.shortcutshandler.TextEditorShortcutListener;
import gUI.utils.tree.FileTreeModel;
import gUI.utils.tree.TreeCellRendererDark;
import gUI.utils.tree.texteditor.FileNameCellRenderer;
import gUI.utils.tree.texteditor.FileTreeNamedModel;
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

public class TextEditor extends JPanel {

	// TextFileWriter logger creation
	static Logger logger = Logger.getLogger(TextEditor.class);

	public static final String AES_KEY_PLACEHOLDER = "#### (AES KEY) ####";
	public static final char DEFAULT_PSW_CHAR = '\u25CF';
	public static final String DEFAULT_NEW_FILE_NAME = "New.txt";

	// this is the color of the left panel file navigator
	public static Color fileNavColorBckg = TextEditorOption.DEFAULT_FILE_PANEL_BACKGROUND_COL;

	// RESOURCE FILES //
	private final static String LOREM_IPSUM = "Resources_Scrypt/Templates/LoremIpsum.txt";

	// TOOLTIPS //
	public static final String tooltipFindText = "[Ctrl+F] Search text into current editor";
	public static final String tooltipReplaceEscapes = "[Ctrl+E] Open Escape sequences / special characters replacement panel - Selected/all text";
	public static final String tooltipGoToLine = "[Ctrl+L] Go to Line number";
	public static final String tooltipReplaceText = "[Ctrl+R] Replace text";
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
	public static final String tooltipFileViewBtn = "[Alt+Q] Show file explorer panel";
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
	public static final String tooltipViewPropertiesCurrFile = "[Alt+Enter] View the properties of current file.";

	public static final String tooltipCutWordsTo = "When selected, the text will be cropped to adapt the size of the text area";
	public static final String tooltipFitMaintainWords = "When selected, the text will be cropped to adapt the size of the text area (without breaking words into fragments)";
	public static final String tooltipInlineTabs = "If selected tabs will be o one line, else they will be always visible organized in rows";

	public static final String tooltipComboFileFormat = "Choose file extension";
	public static final String tooltipComboStyleScheme = "Choose preferred style scheme";

	// GUI Objects //
	public JCheckBox checkBoxInlineTabs;
	public JCheckBox chkFitmantainwords;
	public JCheckBox chkCutWordsTo;

	private JLabel lblSIZE;
	private JLabel lblTextEditor;
	private JLabel lblMargin;
	private JLabel lBLMargin;
	private JLabel lblFontSize;
	private JLabel lblCurrentFileLBL;
	private static JTextField lblCurrentFile;
	private JLabel lblVoiceSpeed;
	public JLabel lblAdvice;
	private JLabel labelTotLines;
	private JLabel labelTotalChars;
	private JLabel labelSelectionLength;
	private JLabel dynLblTotLines;
	private JLabel dynLblTotChars;
	private JLabel dynLblSelLength;
	private JLabel labelCharset;
	private JLabel dynLabelCharset;
	private JLabel labelTabSize;

	// footer
	private JLabel lblColumn;
	private JLabel dynLblColumn;
	private JLabel lblLine;
	private JLabel dynLblCurrLine;
	private JLabel dynLblSearchEsit;

	public JButton btnFindText;
	public JButton btnReplaceText;
	public JButton btnReplaceEscapesSeq;
	public JButton btnGoToLine;
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
	public JButton btnNewTab;
	public JButton btnRemoveSpaces;
	public JButton btnViewInExplorer;
	public JButton btnPropertiesPanel;
	private JSpinner tabSizeSpinner;

	public JToggleButton btnToggleFileView;
	private JRadioButton rdbtnShowCompletePath;

	private JSeparator separator_2;

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

	private int lastIndexTabbedPane = 0;
	public boolean isRemovingTab = false;
	public boolean isAddingTab = false;
	private static boolean tabJustSwitched = false;
	public static boolean isCrypterOn = false;

	public JFrame parentFrame; // the frame that instantiate this object

	private JScrollPane mainScrollPane;
	private JPanel mainPanel;
	private JPanel footerPanel;

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
	public INItializer currentConfiguration;
	private TextEditor currentTextEditor; // current object
	private Text2Speech objSpeaker; // field containing the current object used to speak text
	private JCheckBox chckbxAesShowKey;

	public mainGUI mainApplication;

	private TextEditorShortcutListener shortcutsListener;
	
	public TreeFileMouseListenerTxtEditor mouseListenerTreeFilePanel;

	//////////////// CONSTRUCTOR /////////////////////

	public TextEditor(mainGUI mainGui, INItializer currentConf) {

		logger.info(this.getClass().getName() + " Starting...");

		loadedList = new LinkedList<TabTextEditorObj>();

		setBorder(BorderFactory.createEmptyBorder());
		setBackground(Color.BLACK);
		Dimension maxSize = GuiUtils.getScreenSize();
		setLayout(new MigLayout("", "[300px:1624px:1920,grow]", "[300px:711px:1080,grow]"));

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
		mainPanel.setMaximumSize(GuiUtils.getScreenSizePerc(98, parentFrame));
		mainPanel.setLayout(new MigLayout("", "[25px:2%:2.5%][25px:2%:2.5%][25px:2%:2.5%][25px:2%:2.5%][25px:2%:2.5%][25px:2%:2.5%][25px:2%:2.5%][20px:n:60px][40px:3%:3%][:3%:4%][:2%:3%][40px:2%:50px][25px:2%:2.5%][25px:2%:2.5%][25px:2%:2.5%][25px:2%:2.5%][25px:2%:2.5%][50px:2%:3.5%][:50px:50px][20][30][][25px:2%:2.5%][25px:2%:2.5%][25px:2%:2.5%][25px:2%:2.5%][25px:2%:2.5%][25px:50px:60px][25px:50px:60px][45px:45px:45px][30px:35px:35px][10px:30px,grow]", "[25px:40px:40px][][20px:25px][20px:25px][][7px][200px,grow][32px:32px:34px]"));
		mainScrollPane = new JScrollPane();
		mainScrollPane.setBorder(BorderFactory.createEmptyBorder());
		mainScrollPane.setBackground(Color.DARK_GRAY);
		mainScrollPane.setViewportView(mainPanel);
		add(mainScrollPane, "cell 0 0,grow");

		btnNewTab = new JButton("");
		btnNewTab.setMinimumSize(mainGUI.MINIMUM_BUTTON_SIZE);
		btnNewTab.setToolTipText(tooltipNewEmptyTab);
		btnNewTab.setForeground(Color.BLACK);
		btnNewTab.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnNewTab.setIcon(new ImageIcon(IconsPathConfigurator.ICON_DOC_EMPTY));
		btnNewTab.setBackground(SystemColor.inactiveCaption);
		mainPanel.add(btnNewTab, "cell 1 2,grow");

		btnSaveTextFile = new JButton("");
		btnSaveTextFile.setMinimumSize(mainGUI.MINIMUM_BUTTON_SIZE);
		btnSaveTextFile.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnSaveTextFile.setToolTipText(tooltipSaveTextFile);
		btnSaveTextFile.setForeground(Color.BLACK);
		btnSaveTextFile.setIcon(new ImageIcon(IconsPathConfigurator.ICON_FILE_SAVE));
		btnSaveTextFile.setBackground(SystemColor.inactiveCaption);
		mainPanel.add(btnSaveTextFile, "cell 2 2,grow");

		btnSaveTextFileAs = new JButton("");
		btnSaveTextFileAs.setMinimumSize(mainGUI.MINIMUM_BUTTON_SIZE);
		btnSaveTextFileAs.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnSaveTextFileAs.setToolTipText(tooltipSaveTextFileAs);
		btnSaveTextFileAs.setForeground(SystemColor.menuText);
		btnSaveTextFileAs.setIcon(new ImageIcon(IconsPathConfigurator.ICON_FILE_SAVE_AS));
		btnSaveTextFileAs.setBackground(SystemColor.inactiveCaption);
		mainPanel.add(btnSaveTextFileAs, "cell 3 2,grow");

		btnReloadFromDisk = new JButton("");
		btnReloadFromDisk.setMinimumSize(mainGUI.MINIMUM_BUTTON_SIZE);
		btnReloadFromDisk.setToolTipText(tooltipReloadFromDisk);
		btnReloadFromDisk.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnReloadFromDisk.setIcon(new ImageIcon(IconsPathConfigurator.ICON_RELOAD_CONF));
		btnReloadFromDisk.setForeground(Color.BLACK);
		btnReloadFromDisk.setBackground(SystemColor.inactiveCaption);
		mainPanel.add(btnReloadFromDisk, "cell 4 2,grow");

		btnViewInExplorer = new JButton("");
		btnViewInExplorer.setMinimumSize(mainGUI.MINIMUM_BUTTON_SIZE);
		btnViewInExplorer.setToolTipText(tooltipViewInSysExplorer);
		btnViewInExplorer.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnViewInExplorer.setIcon(new ImageIcon(IconsPathConfigurator.ICON_COPY_DIR_STRUCTURE));
		btnViewInExplorer.setBackground(new Color(176, 196, 222));
		mainPanel.add(btnViewInExplorer, "cell 5 2,grow");

		btnPropertiesPanel = new JButton("");
		btnPropertiesPanel.setMinimumSize(mainGUI.MINIMUM_BUTTON_SIZE);
		btnPropertiesPanel.setToolTipText(tooltipViewPropertiesCurrFile);
		btnPropertiesPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnPropertiesPanel.setIcon(new ImageIcon(IconsPathConfigurator.ICON_ABOUT));
		btnPropertiesPanel.setFont(mainGUI.BUTTON_DEFAULT_FONT);
		btnPropertiesPanel.setMinimumSize(new Dimension(16, 12));
		mainPanel.add(btnPropertiesPanel, "cell 6 2,grow");

		btnTtTt = new JButton("Tt Tt");
		btnTtTt.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnTtTt.setFont(mainGUI.BUTTON_DEFAULT_FONT);
		btnTtTt.setBackground(new Color(176, 196, 222));
		btnTtTt.setToolTipText(tooltipTtTt);
		mainPanel.add(btnTtTt, "cell 7 2 2 1,growx");

		btnTt = new JButton("TT");
		btnTt.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnTt.setFont(mainGUI.BUTTON_DEFAULT_FONT);
		btnTt.setBackground(new Color(176, 196, 222));
		btnTt.setToolTipText(tooltipTt);
		mainPanel.add(btnTt, "cell 9 2,growx");

		btnUpperPostDots = new JButton(".Tt");
		btnUpperPostDots.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnUpperPostDots.setToolTipText(tooltipUpperPostDots);
		btnUpperPostDots.setFont(mainGUI.BUTTON_DEFAULT_FONT);
		btnUpperPostDots.setBackground(new Color(176, 196, 222));
		mainPanel.add(btnUpperPostDots, "cell 10 2,growx");

		btnLowerAfterDots = new JButton(".tT");
		btnLowerAfterDots.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnLowerAfterDots.setToolTipText(tooltipLowerPostDots);
		btnLowerAfterDots.setToolTipText("Makes text uppercase after dots, or a significat line brokers");
		btnLowerAfterDots.setFont(mainGUI.BUTTON_DEFAULT_FONT);
		btnLowerAfterDots.setBackground(new Color(176, 196, 222));
		mainPanel.add(btnLowerAfterDots, "cell 11 2");

		btnClear = new JButton("");
		btnClear.setMinimumSize(new Dimension(20, 0));
		btnClear.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnClear.setToolTipText(tooltipClearText);
		btnClear.setBackground(new Color(176, 196, 222));
		btnClear.setIcon(new ImageIcon(IconsPathConfigurator.ICON_DELETE));
		btnClear.setFont(mainGUI.BUTTON_DEFAULT_FONT);
		mainPanel.add(btnClear, "cell 12 2,alignx center,aligny center");

		btnUndo = new JButton("");
		btnUndo.setMinimumSize(new Dimension(20, 0));
		btnUndo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnUndo.setToolTipText(tooltipUndo);
		btnUndo.setIcon(new ImageIcon(IconsPathConfigurator.ICON_UNDO));
		btnUndo.setBackground(new Color(176, 196, 222));
		mainPanel.add(btnUndo, "cell 13 2,alignx center");

		btnRedo = new JButton("");
		btnRedo.setMinimumSize(new Dimension(20, 0));
		btnRedo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnRedo.setToolTipText(tooltipRedo);
		btnRedo.setBackground(new Color(176, 196, 222));
		mainPanel.add(btnRedo, "cell 14 2,alignx center");
		btnRedo.setIcon(new ImageIcon(IconsPathConfigurator.ICON_REDO));

		btnBold = new JButton("");
		btnBold.setMinimumSize(new Dimension(20, 0));
		btnBold.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnBold.setToolTipText(tooltipBoldText);
		btnBold.setBackground(new Color(176, 196, 222));
		btnBold.setIcon(new ImageIcon(IconsPathConfigurator.ICON_TEXT_BOLD));
		mainPanel.add(btnBold, "cell 15 2,alignx center");

		btnItaliC = new JButton("");
		btnItaliC.setMinimumSize(new Dimension(20, 0));
		btnItaliC.setToolTipText(tooltipItalic);
		btnItaliC.setBackground(new Color(176, 196, 222));
		btnItaliC.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnItaliC.setIcon(new ImageIcon(IconsPathConfigurator.ICON_TEXT_ITALIC));
		mainPanel.add(btnItaliC, "cell 16 2,alignx center");

		btnLoremIpsum = new JButton("Lorem Ipsum");
		btnLoremIpsum.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnLoremIpsum.setToolTipText(tooltipLoremIpsum);
		btnLoremIpsum.setFont(mainGUI.BUTTON_DEFAULT_FONT);
		btnLoremIpsum.setBackground(new Color(176, 196, 222));
		mainPanel.add(btnLoremIpsum, "cell 21 2,growx");

		btnParseImageText = new JButton("");
		btnParseImageText.setMinimumSize(new Dimension(20, 0));
		btnParseImageText.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnParseImageText.setIcon(new ImageIcon(IconsPathConfigurator.ICON_CONVERT_OCR_IMAGE));
		btnParseImageText.setToolTipText(tooltipParseImage);
		btnParseImageText.setFont(mainGUI.BUTTON_DEFAULT_FONT);
		btnParseImageText.setBackground(new Color(176, 196, 222));
		mainPanel.add(btnParseImageText, "cell 22 2,alignx center");

		btnParsePdfText = new JButton("");
		btnParsePdfText.setMinimumSize(new Dimension(20, 0));
		btnParsePdfText.setToolTipText(tooltipParsePdfTxt);
		btnParsePdfText.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnParsePdfText.setIcon(new ImageIcon(IconsPathConfigurator.F_ICON_PDF));
		btnParsePdfText.setFont(mainGUI.BUTTON_DEFAULT_FONT);
		btnParsePdfText.setBackground(new Color(176, 196, 222));
		mainPanel.add(btnParsePdfText, "cell 23 2,alignx center");

		btnOpenInScreepT = new JButton("");
		btnOpenInScreepT.setMinimumSize(new Dimension(20, 0));
		btnOpenInScreepT.setToolTipText(tooltipOpenInScreept);
		btnOpenInScreepT.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnOpenInScreepT.setIcon(new ImageIcon(IconsPathConfigurator.ICON_SCREEPT));
		btnOpenInScreepT.setFont(mainGUI.BUTTON_DEFAULT_FONT);
		btnOpenInScreepT.setBackground(new Color(176, 196, 222));
		mainPanel.add(btnOpenInScreepT, "cell 24 2,alignx center");
		
		labelTabSize = new JLabel("Tab Size:");
		mainPanel.add(labelTabSize, "cell 27 2");
		labelTabSize.setHorizontalAlignment(SwingConstants.RIGHT);
		labelTabSize.setForeground(SystemColor.menu);
		
		tabSizeSpinner = new JSpinner();
		mainPanel.add(tabSizeSpinner, "cell 28 2");
		tabSizeSpinner.setModel(new SpinnerNumberModel(currentConf.getTextEditorOpt().getTabSize(), 1, 9, 1));
		tabSizeSpinner.setCursor(GuiUtils.CURSOR_HAND);

		lblMargin = new JLabel("Margin:");
		lblMargin.setHorizontalAlignment(SwingConstants.CENTER);
		lblMargin.setForeground(SystemColor.menu);
		lblMargin.setBackground(UIManager.getColor("CheckBox.darkShadow"));
		mainPanel.add(lblMargin, "cell 29 2,alignx center");

		lBLMargin = new JLabel("12");
		lBLMargin.setForeground(Color.WHITE);
		mainPanel.add(lBLMargin, "cell 30 2,alignx center");

		btnToggleFileView = new JToggleButton("");
		btnToggleFileView.setMinimumSize(new Dimension(16, 12));
		btnToggleFileView.setToolTipText(tooltipFileViewBtn);
		btnToggleFileView.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnToggleFileView.setIcon(new ImageIcon(IconsPathConfigurator.ICON_FILE_N_DIR));
		mainPanel.add(btnToggleFileView, "cell 0 3,grow");

		btnDark = new JButton("");
		btnDark.setMinimumSize(mainGUI.MINIMUM_BUTTON_SIZE);
		btnDark.setMargin(new Insets(0, 0, 0, 0));
		btnDark.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnDark.setToolTipText(tooltipDarkSwitch);
		btnDark.setFont(mainGUI.BUTTON_DEFAULT_FONT);
		mainPanel.add(btnDark, "cell 1 3,grow");

		btnDefaultOpt = new JButton("Default");
		btnDefaultOpt.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnDefaultOpt.setFont(new Font("SansSerif", Font.BOLD, 10));
		btnDefaultOpt.setToolTipText(tooltipDefaultOpt);
		btnDefaultOpt.setForeground(SystemColor.menuText);
		btnDefaultOpt.setBackground(new Color(176, 196, 222));
		mainPanel.add(btnDefaultOpt, "cell 2 3 2 1,growx");

		comboFileFormat = new JComboBox();
		comboFileFormat.setToolTipText(tooltipComboFileFormat);
		comboFileFormat.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		comboFileFormat.setBackground(new Color(100, 149, 237));
		comboFileFormat.setModel(new DefaultComboBoxModel(SupportedFileTextEditor.values()));
		mainPanel.add(comboFileFormat, "cell 4 3 4 1,growx");

		comboStylesScheme = new JComboBox();
		comboStylesScheme.setToolTipText(tooltipComboStyleScheme);
		comboStylesScheme.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		comboStylesScheme.setModel(new DefaultComboBoxModel<>(Themes.values()));
		comboStylesScheme.setSelectedIndex(0);
		comboStylesScheme.setBackground(new Color(100, 149, 237));
		mainPanel.add(comboStylesScheme, "cell 8 3 3 1,growx");

		btntTextFormat = new JButton("Format");
		btntTextFormat.setMinimumSize(new Dimension(40, 24));
		btntTextFormat.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btntTextFormat.setFont(new Font("SansSerif", Font.BOLD, 11));
		btntTextFormat.setBackground(new Color(176, 196, 222));
		mainPanel.add(btntTextFormat, "cell 11 3 2 1,grow");

		btnRemoveSpaces = new JButton("");
		btnRemoveSpaces.setMinimumSize(new Dimension(20, 0));
		btnRemoveSpaces.setToolTipText(tooltipRemoveSpaces);
		btnRemoveSpaces.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnRemoveSpaces.setIcon(new ImageIcon(IconsPathConfigurator.ICON_REMOVE_WHITE_LINE));
		btnRemoveSpaces.setBackground(new Color(176, 196, 222));
		mainPanel.add(btnRemoveSpaces, "cell 13 3,alignx center");

		btnBackgroundCol = new JButton("");
		btnBackgroundCol.setMinimumSize(new Dimension(20, 0));
		btnBackgroundCol.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnBackgroundCol.setToolTipText(tooltipBackgroundColor);
		btnBackgroundCol.setBackground(new Color(176, 196, 222));
		btnBackgroundCol.setIcon(new ImageIcon(IconsPathConfigurator.ICON_TEXT_BG_COLOR));
		mainPanel.add(btnBackgroundCol, "cell 14 3,alignx center");

		btnForegroundCol = new JButton("");
		btnForegroundCol.setMinimumSize(new Dimension(20, 0));
		btnForegroundCol.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnForegroundCol.setToolTipText(tooltipForegroundTxt);
		btnForegroundCol.setBackground(new Color(176, 196, 222));
		btnForegroundCol.setIcon(new ImageIcon(IconsPathConfigurator.ICON_TEXT_COLOR));
		mainPanel.add(btnForegroundCol, "cell 15 3,alignx center");

		btnFontChooser = new JButton("");
		btnFontChooser.setMinimumSize(new Dimension(20, 0));
		btnFontChooser.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnFontChooser.setToolTipText(tooltipFontChooser);

		btnFontChooser.setBackground(new Color(176, 196, 222));
		btnFontChooser.setIcon(new ImageIcon(IconsPathConfigurator.ICON_TEXT_FONT));
		mainPanel.add(btnFontChooser, "cell 16 3,alignx center");

		btnCrypt = new JButton();
		btnCrypt.setBackground(new Color(112, 128, 144));
		btnCrypt.setMinimumSize(new Dimension(28, 12));
		btnCrypt.setHorizontalAlignment(SwingConstants.RIGHT);
		// btnCrypt.setHorizontalTextPosition(SwingConstants.RIGHT);
		btnCrypt.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnCrypt.setIcon(new ImageIcon(IconsPathConfigurator.ICON_CRYPT));
		btnCrypt.setToolTipText(tooltipCrypt);
		btnCrypt.setFont(mainGUI.BUTTON_DEFAULT_FONT);
		btnCrypt.setForeground(SystemColor.inactiveCaption);
		mainPanel.add(btnCrypt, "cell 17 3,alignx center");

		btnDecrypt = new JButton("");
		btnDecrypt.setBackground(new Color(112, 128, 144));
		btnDecrypt.setMinimumSize(new Dimension(28, 12));
		btnDecrypt.setHorizontalTextPosition(SwingConstants.RIGHT);
		btnDecrypt.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnDecrypt.setIcon(new ImageIcon(IconsPathConfigurator.ICON_DECRYPT));
		btnDecrypt.setToolTipText(tooltipDecrypt);
		btnDecrypt.setForeground(SystemColor.inactiveCaption);
		btnDecrypt.setFont(mainGUI.BUTTON_DEFAULT_FONT);
		mainPanel.add(btnDecrypt, "cell 18 3,alignx center");

		chckbxAesShowKey = new JCheckBox("Show key");
		chckbxAesShowKey.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		chckbxAesShowKey.setForeground(Color.WHITE);
		chckbxAesShowKey.setFont(mainGUI.BUTTON_DEFAULT_FONT);
		mainPanel.add(chckbxAesShowKey, "cell 19 3 2 1,alignx center");

		btnTemplates = new JButton("Templates");
		btnTemplates.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnTemplates.setToolTipText(tooltipTemplates);
		btnTemplates.setFont(mainGUI.BUTTON_DEFAULT_FONT);
		btnTemplates.setBackground(new Color(176, 196, 222));
		mainPanel.add(btnTemplates, "cell 21 3,growx");

		btnSpeak = new JButton("");
		btnSpeak.setMinimumSize(new Dimension(20, 0));
		btnSpeak.setCursor(GuiUtils.CURSOR_HAND);
		btnSpeak.setToolTipText(tooltipSpeak);
		btnSpeak.setIcon(new ImageIcon(IconsPathConfigurator.ICON_SPEAKER));
		btnSpeak.setFont(mainGUI.BUTTON_DEFAULT_FONT);
		btnSpeak.setBackground(new Color(176, 196, 222));
		mainPanel.add(btnSpeak, "cell 22 3,alignx center");

		JPanel panel = new JPanel();
		panel.setForeground(SystemColor.menu);
		panel.setBorder(new LineBorder(new Color(0, 0, 0), 2));
		panel.setBackground(new Color(169, 169, 169));
		mainPanel.add(panel, "cell 0 0 32 1,growx,aligny top");
		panel.setLayout(
				new MigLayout("", "[160px,left][][][][][][][160px][][][50px:100px:120px][grow][][][][]", "[grow]"));

		lblVoiceSpeed = new JLabel("Voice rate");
		lblVoiceSpeed.setHorizontalAlignment(SwingConstants.RIGHT);
		panel.add(lblVoiceSpeed, "cell 2 0,alignx right");
		lblVoiceSpeed.setForeground(new Color(0, 0, 0));

		sliderSpokenSpeed = new JSlider();
		panel.add(sliderSpokenSpeed, "cell 3 0,growy");
		sliderSpokenSpeed.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
		sliderSpokenSpeed.setValue(currentConfiguration.getTextEditorOpt().getVoiceRate());
		sliderSpokenSpeed.setMaximum(400);

		checkBoxInlineTabs = new JCheckBox("Inline Tabs  ");
		checkBoxInlineTabs.setSelected(true);
		checkBoxInlineTabs.setToolTipText(tooltipInlineTabs);
		checkBoxInlineTabs.setCursor(GuiUtils.CURSOR_HAND);
		checkBoxInlineTabs.setForeground(Color.DARK_GRAY);
		checkBoxInlineTabs.setBackground(Color.DARK_GRAY);
		panel.add(checkBoxInlineTabs, "cell 13 0,alignx center,aligny center");

		chkCutWordsTo = new JCheckBox("Adapt Text Width  ");
		chkCutWordsTo.setCursor(GuiUtils.CURSOR_HAND);
		panel.add(chkCutWordsTo, "cell 14 0,alignx center,aligny center");
		chkCutWordsTo.setToolTipText(tooltipCutWordsTo);
		chkCutWordsTo.setSelected(false);
		chkCutWordsTo.setForeground(Color.DARK_GRAY);
		chkCutWordsTo.setBackground(Color.DARK_GRAY);

		chkFitmantainwords = new JCheckBox("Don't cut words");
		chkFitmantainwords.setCursor(GuiUtils.CURSOR_HAND);
		panel.add(chkFitmantainwords, "cell 15 0,alignx right,aligny center");
		chkFitmantainwords.setToolTipText(tooltipFitMaintainWords);
		chkFitmantainwords.setForeground(Color.DARK_GRAY);
		chkFitmantainwords.setBackground(Color.DARK_GRAY);
		chkFitmantainwords.setEnabled(false);

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
		mainTabList.setBorder(new LineBorder(Color.BLACK, 2));
		mainTabList.setBackground(new Color(30, 30, 30));
		// mainTabList.setOpaque(true);

		btnReplaceEscapesSeq = new JButton("");
		btnReplaceEscapesSeq.setMinimumSize(new Dimension(20, 0));
		btnReplaceEscapesSeq.setToolTipText(tooltipReplaceEscapes);
		btnReplaceEscapesSeq.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnReplaceEscapesSeq.setIcon(new ImageIcon(IconsPathConfigurator.ICON_CONVERT_ESCAPES));
		btnReplaceEscapesSeq.setFont(mainGUI.BUTTON_DEFAULT_FONT);
		btnReplaceEscapesSeq.setBackground(new Color(176, 196, 222));
		mainPanel.add(btnReplaceEscapesSeq, "cell 23 3,alignx center");

		btnFindText = new JButton("");
		btnFindText.setMinimumSize(new Dimension(20, 0));
		btnFindText.setToolTipText(tooltipFindText);
		btnFindText.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnFindText.setIcon(new ImageIcon(IconsPathConfigurator.ICON_SEARCH));
		btnFindText.setBackground(SystemColor.activeCaption);
		btnFindText.setFont(mainGUI.BUTTON_DEFAULT_FONT);
		btnFindText.setBackground(new Color(176, 196, 222));
		mainPanel.add(btnFindText, "cell 24 3,alignx center");

		btnReplaceText = new JButton("");
		btnReplaceText.setToolTipText(tooltipReplaceText);
		btnReplaceText.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnReplaceText.setIcon(new ImageIcon(IconsPathConfigurator.ICON_REPLACE_TXT));
		btnReplaceText.setMinimumSize(new Dimension(20, 0));
		btnReplaceText.setFont(new Font("SansSerif", Font.BOLD, 12));
		btnReplaceText.setBackground(SystemColor.inactiveCaption);
		mainPanel.add(btnReplaceText, "cell 25 3");

		btnGoToLine = new JButton("");
		btnGoToLine.setMinimumSize(new Dimension(20, 0));
		btnGoToLine.setToolTipText(tooltipGoToLine);
		btnGoToLine.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnGoToLine.setIcon(new ImageIcon(IconsPathConfigurator.ICON_GOTO_LINE));
		btnGoToLine.setFont(new Font("SansSerif", Font.BOLD, 12));
		btnGoToLine.setBackground(SystemColor.inactiveCaption);
		mainPanel.add(btnGoToLine, "cell 25 2");

		mainTabList.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

		splitPaneMain = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPaneMain.setBackground(mainTabList.getBackground());
		leftSplitTreeFiles = new JTree(new FileTreeModel(null));
		mouseListenerTreeFilePanel = new TreeFileMouseListenerTxtEditor(leftSplitTreeFiles, this);
		leftSplitTreeFiles.setCellRenderer(new TreeCellRendererDark());
		leftSplitTreeFiles.setBackground(fileNavColorBckg);
		leftSplitTreeFiles.setForeground(Color.white);
		leftSplitPane = new JScrollPane(leftSplitTreeFiles);
		leftSplitPane.setPreferredSize(new Dimension(200, 500));

		mainPanel.add(splitPaneMain, "cell 0 5 32 2,grow");
		splitPaneMain.setRightComponent(mainTabList);
		splitPaneMain.setLeftComponent(leftSplitPane);
		splitPaneMain.setContinuousLayout(true);

		lblFontSize = new JLabel("Font size: ");
		lblFontSize.setHorizontalAlignment(SwingConstants.CENTER);
		lblFontSize.setForeground(SystemColor.menu);
		mainPanel.add(lblFontSize, "cell 27 3,alignx center");

		lblSIZE = new JLabel("");
		lblSIZE.setFont(new Font("SansSerif", Font.PLAIN, 10));
		lblSIZE.setForeground(SystemColor.window);
		mainPanel.add(lblSIZE, "cell 28 3,alignx center");
		lblSIZE.setText(String.valueOf(mainGUI.DEFAULT_FONT.getSize()) + " Px");

		separator_2 = new JSeparator();
		separator_2.setBackground(Color.GRAY);
		mainPanel.add(separator_2, "cell 0 4 32 1,growx");

		addTab(null, true); // comment for editing in windowuilder

		sliderFontSize = new JSlider(4, 100);
		sliderFontSize.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
		sliderFontSize.setForeground(SystemColor.activeCaptionBorder);
		sliderFontSize.setBackground(UIManager.getColor("Button.shadow"));
		sliderFontSize.setValue(currentConfiguration.getTextEditorOpt().getFont().getSize());
		mainPanel.add(sliderFontSize, "cell 29 3 3 1,growx");

		JSeparator separator_1 = new JSeparator();
		mainPanel.add(separator_1, "cell 0 1 32 1,growx");

		txtAesKey = new JPasswordField();
		txtAesKey.setHorizontalAlignment(SwingConstants.CENTER);
		txtAesKey.setEchoChar((char) 0);
		txtAesKey.setText(AES_KEY_PLACEHOLDER);
		mainPanel.add(txtAesKey, "cell 17 2 4 1,growx");
		txtAesKey.setColumns(10);
		txtAesKey.setFont(mainGUI.BUTTON_DEFAULT_FONT);
		txtAesKey.setEchoChar(DEFAULT_PSW_CHAR);
		AesCipher.check(txtAesKey);

		lblTextEditor = new JLabel("Text Editor");
		panel.add(lblTextEditor, "cell 0 0,alignx left");
		lblTextEditor.setHorizontalAlignment(SwingConstants.CENTER);
		lblTextEditor.setForeground(Color.BLACK);
		lblTextEditor.setFont(new Font("Segoe UI", Font.BOLD, 15));
		lblTextEditor.setBackground(SystemColor.activeCaptionBorder);

		slider_margin = new JSlider(0, 100, 0);
		slider_margin.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
		slider_margin.setBackground(UIManager.getColor("Button.shadow"));
		mainPanel.add(slider_margin, "cell 31 2,growx");

		btnLoadTextFile = new JButton("");
		btnLoadTextFile.setMinimumSize(mainGUI.MINIMUM_BUTTON_SIZE);
		btnLoadTextFile.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnLoadTextFile.setToolTipText(tooltipLoadTextFile);
		btnLoadTextFile.setForeground(SystemColor.menuText);
		btnLoadTextFile.setBackground(SystemColor.inactiveCaption);
		btnLoadTextFile.setIcon(new ImageIcon(IconsPathConfigurator.ICON_FILE_LOAD));
		mainPanel.add(btnLoadTextFile, "cell 0 2,grow");

		footerPanel = new JPanel();
		footerPanel.setBorder(new LineBorder(new Color(0, 0, 0), 2));
		footerPanel.setBackground(Color.GRAY);
		mainPanel.add(footerPanel, "cell 0 7 32 1,grow");
		footerPanel.setLayout(new MigLayout("",
				"[][160px][grow][][:60px:120px][][:60px:120px][][:60px:120px][][:20px:120px][][:20px:120px][][:60px:120px][grow][]",
				"[fill]"));

		lblCurrentFileLBL = new JLabel("   Current file:");
		footerPanel.add(lblCurrentFileLBL, "cell 0 0,growy");
		lblCurrentFileLBL.setForeground(SystemColor.controlHighlight);
		lblCurrentFileLBL.setFont(mainGUI.BUTTON_DEFAULT_FONT);

		lblCurrentFile = new JTextField("\\*.txt");
		lblCurrentFile.setMinimumSize(new Dimension(280, 28));
		lblCurrentFile.setBackground(Color.DARK_GRAY);
		lblCurrentFile.setOpaque(true);
		footerPanel.add(lblCurrentFile, "cell 1 0");
		lblCurrentFile.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblCurrentFile.setForeground(SystemColor.activeCaption);

		rdbtnShowCompletePath = new JRadioButton("Complete Path");
		rdbtnShowCompletePath.setFont(new Font("SansSerif", Font.PLAIN, 11));
		footerPanel.add(rdbtnShowCompletePath, "cell 2 0");
		rdbtnShowCompletePath.setSelected(false);

		labelCharset = new JLabel("Charset:");
		labelCharset.setForeground(SystemColor.controlHighlight);
		labelCharset.setFont(new Font("Segoe UI", Font.BOLD, 10));
		footerPanel.add(labelCharset, "cell 3 0");

		dynLabelCharset = new JLabel("");
		dynLabelCharset.setForeground(new Color(230, 230, 250));
		dynLabelCharset.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		footerPanel.add(dynLabelCharset, "cell 4 0");

		labelTotLines = new JLabel("Tot Lines:");
		labelTotLines.setForeground(SystemColor.controlHighlight);
		labelTotLines.setFont(new Font("Segoe UI", Font.BOLD, 10));
		footerPanel.add(labelTotLines, "cell 5 0,alignx right,aligny center");

		dynLblTotLines = new JLabel("");
		dynLblTotLines.setForeground(new Color(230, 230, 250));
		dynLblTotLines.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		footerPanel.add(dynLblTotLines, "cell 6 0");

		labelTotalChars = new JLabel("Tot. Chars:");
		labelTotalChars.setForeground(SystemColor.controlHighlight);
		labelTotalChars.setFont(new Font("Segoe UI", Font.BOLD, 10));
		footerPanel.add(labelTotalChars, "cell 7 0,alignx right");

		dynLblTotChars = new JLabel("");
		dynLblTotChars.setForeground(new Color(230, 230, 250));
		dynLblTotChars.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		footerPanel.add(dynLblTotChars, "cell 8 0");

		lblLine = new JLabel("Sel. Line:");
		lblLine.setForeground(SystemColor.controlHighlight);
		lblLine.setFont(new Font("Segoe UI", Font.BOLD, 10));
		footerPanel.add(lblLine, "cell 9 0");

		dynLblCurrLine = new JLabel("");
		dynLblCurrLine.setForeground(new Color(230, 230, 250));
		dynLblCurrLine.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		footerPanel.add(dynLblCurrLine, "cell 10 0");

		lblColumn = new JLabel("Sel. Column:");
		lblColumn.setForeground(SystemColor.controlHighlight);
		lblColumn.setFont(new Font("Segoe UI", Font.BOLD, 10));
		footerPanel.add(lblColumn, "cell 11 0");

		dynLblColumn = new JLabel("");
		dynLblColumn.setForeground(new Color(230, 230, 250));
		dynLblColumn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		footerPanel.add(dynLblColumn, "cell 12 0");

		labelSelectionLength = new JLabel("Sel. lenght:");
		labelSelectionLength.setForeground(SystemColor.controlHighlight);
		labelSelectionLength.setFont(new Font("Segoe UI", Font.BOLD, 10));
		footerPanel.add(labelSelectionLength, "cell 13 0,alignx right");

		dynLblSelLength = new JLabel("");
		dynLblSelLength.setForeground(new Color(230, 230, 250));
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
		lblAdvice.setFont(new Font("Segoe UI", Font.BOLD, 13));
		lblAdvice.setForeground(new Color(176, 224, 230));
		lblAdvice.setHorizontalAlignment(SwingConstants.CENTER);
		lblAdvice.setText(" [Current output: .txt]");

		addHandlers();

		// add custom keyboard listener
		shortcutsListener = getKeyEventHandler();
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(shortcutsListener);

		startAesKeyCheckThread();

		initGuiFromConfig();

		loadOpenedFilesAtStart();

		resizeFrameToDefault();

		refreshTree();
		
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
		final Font DEFAULT = mainGUI.DEFAULT_FONT;
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
				refreshTree();
				if (btnToggleFileView.isSelected()) {
					btnToggleFileView.setBackground(new Color(120, 0, 0));
					currentConfiguration.getTextEditorOpt().fileTreeActive = true;
				} else {
					btnToggleFileView.setBackground(SystemColor.inactiveCaption);
					currentConfiguration.getTextEditorOpt().fileTreeActive = false;
				}
			}
		});

		// FOOTER UI RADIOBOX COMPLETE PATH (current file)
		rdbtnShowCompletePath.setForeground(Color.LIGHT_GRAY);
		rdbtnShowCompletePath.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (rdbtnShowCompletePath.isSelected()) {
					rdbtnShowCompletePath.setForeground(Color.WHITE);
				} else {
					rdbtnShowCompletePath.setForeground(Color.LIGHT_GRAY);
				}
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
				resizeFrameToDefault();
			}

			public void componentResized(ComponentEvent e) {
				resizeFrameToDefault();
			}

			public void componentMoved(ComponentEvent e) {
				resizeFrameToDefault();
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
					escapeReplacerPanel = new EscapeReplacerPanel(mainApplication.getMainFrame(), loadedTabObj.textArea,
							btnTemplates);
					escapeReplacerPanel.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					escapeReplacerPanel.setBounds(750, 140, 500, 210);
					escapeReplacerPanel.setAlwaysOnTop(true);
					escapeReplacerPanel.setVisible(true);
					escapeReplacerPanel.setTitle("Escape<->Special converter - " + mainGUI.APPLICATION_NAME);
					btnTemplates.setEnabled(false);
				}
			}
		});

		// IMAGE PARSE INJECTOR
		btnParseImageText.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				if (imageParserBox == null || !ImageSelector2TextFrame.active) {
					imageParserBox = new ImageSelector2TextFrame(parentFrame, loadedTabObj.textArea,
							mainGUI.APPLICATION_NAME + " - Image2Text");
					imageParserBox.setVisible(true);
				} else {
					imageParserBox.toFront();
					imageParserBox.requestFocus();
				}
			}
		});

		// PDF PARSER INJECTOR
		btnParsePdfText.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (pdfParserBox == null || !PdfSelector2TextFrame.active) {
					pdfParserBox = new PdfSelector2TextFrame(parentFrame, loadedTabObj.textArea,
							mainGUI.APPLICATION_NAME + " - Pdf2Text");
					pdfParserBox.setVisible(true);
				} else {
					pdfParserBox.toFront();
					pdfParserBox.requestFocus();
				}
			}
		});

		// SEARCH TEXT
		btnFindText.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (searchBox == null || !searchBox.isVisible()) {
					TextAreaDialogs.getSearchDialog(parentFrame, loadedTabObj.textArea, dynLblSearchEsit);
					GuiUtils.setFrameIcon(IconsPathConfigurator.ICON_SEARCH, searchBox);
				} else {
					searchBox.toFront();
					searchBox.requestFocus();
				}
			}
		});

		// GOTO LINE
		btnGoToLine.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (goToLineDialog == null || !goToLineDialog.isVisible()) {
					goToLineDialog = TextAreaDialogs.getGoToLineDialog(parentFrame, loadedTabObj.textArea);
				} else {
					goToLineDialog.toFront();
					goToLineDialog.requestFocus();
				}
			}
		});

		// REPLACE TEXT
		btnReplaceText.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (replaceDialog == null || !replaceDialog.isVisible()) {
					replaceDialog = TextAreaDialogs.getReplaceDialog(parentFrame, loadedTabObj.textArea,
							dynLblSearchEsit);
					GuiUtils.setFrameIcon(IconsPathConfigurator.ICON_REPLACE_TXT, replaceDialog);
				} else {
					replaceDialog.toFront();
					replaceDialog.requestFocus();
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
				font = new Font(font.getName(), font.getStyle(), sliderFontSize.getValue());
				loadedTabObj.textArea.setFont(font);
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
					panel.setTitle("TEMPLATES manager - " + mainGUI.APPLICATION_NAME);
					btnTemplates.setEnabled(false);
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
						refreshMessageAndTabTooltip("[File saved]");
					} else {
						errorAdvice("[Error - not saved]");
						lblCurrentFile.setText("(*)" + loadedTabObj.loadedFile.getName());
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
					String exportNamePreset = (loadedTabObj.loadedFile != null && !loadedTabObj.loadedFile.getName().equals(""))? loadedTabObj.loadedFile.getName() : "New."+comboFileFormat.getSelectedItem();
					try {
						File selectedFile = GenericFileChooserDialog.fileWrite(parentFrame, currentConfiguration, exportNamePreset + CURRENT_EXT_OUT);

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
								lblCurrentFile.setToolTipText(loadedTabObj.loadedFile.getAbsolutePath());
								refreshMessageAndTabTooltip("[File saved]");
								currentConfiguration.getFileOpt().setLastDstFolderPath(
										loadedTabObj.loadedFile.getParentFile().getAbsolutePath());
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
							refreshMessageAndTabTooltip("[File Reloaded]");

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
				addTab(null, false);
			}
		});

		// TAB CHANGE LISTENER
		mainTabList.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				if (lastIndexTabbedPane != mainTabList.getSelectedIndex()) {
					tabRefreshAfterUpdate();
					lastIndexTabbedPane = mainTabList.getSelectedIndex();
				}
				findRightStyleFromComboNSelect(loadedTabObj.loadedFile);
			}
		});
	}

	public void resizeFrameToDefault() {
		Rectangle currSize = mainApplication.getMainFrame().getBounds();
		Dimension high = new Dimension(currSize.width - 60, currSize.height - 300);
		Dimension max = new Dimension(currSize.width - 45, currSize.height - 240);
		mainTabList.setPreferredSize(high);
		mainTabList.setMaximumSize(max);
		// mainTabList.setMinimumSize(max);
		loadedTabObj.currentScrollPane.setPreferredSize(high);
		// loadedTabObj.currentScrollPane.setMinimumSize(high);
		Dimension med = new Dimension(currSize.width - loadedTabObj.currentScrollPane.getGutter().getWidth(),
				currSize.height - 300);
		loadedTabObj.textArea.setMaximumSize(med);
		if (!btnToggleFileView.isSelected()) {
			splitPaneMain.setDividerLocation(0);
		}
		// loadedTabObj.textArea.setMinimumSize(high);
		// currentTextEditor.revalidate();
		currentTextEditor.repaint();
	}

	public static void fileChangedLblUpdate() {

		int indexChange = mainTabList.getSelectedIndex();
		try {
			if (!tabJustSwitched && indexChange >= 0 && !isCrypterOn && !alreadyLoading) {
				loadedTabObj.unsaved = true;
				updateSavedLabelNTab(null);
			}
		} catch (Exception e) {
			logger.error("Error during label update!", e);
		}
	}

	private void refreshMessageAndTabTooltip(String message) {
		logger.debug("Starting TextEditor.refreshMessageAndTabTooltip() with message: "+message);
		successAdvice(message);
		updateTabTooltip(mainTabList.getSelectedIndex());
		lblCurrentFile.setForeground(ImageGeneralWorker.randomColorRGB(0.1f));
		logger.debug("TextEditor.refreshMessageAndTabTooltip() completed - Exiting");
	}

	/**
	 * specify NULL sourceIndex to use current selected tab, or a custom value > 0
	 * to specify the index of tab to take unsaved value
	 */
	public static void updateSavedLabelNTab(Integer sourceIndex) {
		int indexChange = (sourceIndex == null) ? mainTabList.getSelectedIndex() : sourceIndex;
		try {
			// reference to tabHeader title
			JLabel tabHeadLbl = (JLabel) (getPanelFromTabHeader(indexChange)).getComponent(TabUtils.TAB_TITLE_ORDER);
			// IF ALREADY SAVED
			if (indexChange >= 0 && !loadedTabObj.unsaved) {
				// IF FILE IS DEFINED
				if (loadedTabObj.loadedFile != null) {
					lblCurrentFile.setText(loadedTabObj.loadedFile.getName());
					lblCurrentFile.setToolTipText(loadedTabObj.loadedFile.getAbsolutePath());
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
					setIconToTab(IconsPathConfigurator.ICON_DOC_EMPTY, indexChange);
				} else {
					setIconToTab(IconsPathConfigurator.ICON_DOC_SAVED, indexChange);
				}
				// ELSE IF NOT YET SAVED
			} else if (indexChange >= 0 && loadedTabObj.unsaved) {
				// IF FILE IS DEFINED
				if (loadedTabObj.loadedFile != null) {
					tabHeadLbl.setText(HandleString.fitToMaxLentgh("(*)" + loadedTabObj.loadedFile.getName(),
							TabUtils.MAX_TAB_LABELENGHT));
					lblCurrentFile.setText("(*)" + loadedTabObj.loadedFile.getName());
					lblCurrentFile.setToolTipText(loadedTabObj.loadedFile.getAbsolutePath());
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
				setIconToTab(IconsPathConfigurator.ICON_DOC_UNSAVED, indexChange);
			}
		} catch (Exception e) {
			logger.error("Error refreshing informations abount unsaved obj", e);
		}
	}

	private boolean askSave(int index) {
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
		comboStylesScheme.setSelectedItem(txtOptions.selectedTheme);
		btnToggleFileView.setSelected(txtOptions.fileTreeActive);

		splitPaneMain.setDividerLocation(txtOptions.dividerFileTreePosX);

		if (txtOptions.fileTreeActive) {
			if (loadedTabObj.loadedFile == null) {
				splitPaneMain.setDividerLocation(0);
			}
			btnToggleFileView.setBackground(new Color(120, 0, 0));
		} else {
			btnToggleFileView.setBackground(SystemColor.inactiveCaption);
		}

		// SwingUtilities.invokeLater(new Runnable() {
		// public void run() {
		// GUIStyelSettings.applyNimbusSize(currentConfiguration.getGuiOpt().nimbusSize);
		// GUIStyelSettings.updateAllWindowsLaf();
		// mainTabList.setUI(new CustomTabbedPaneUI_v1(null, null, null, mainTabList,
		// TabUtils.TAB_BUTTON_CLOSE_ORDER));
		// TextEditor.resetDividerColor();
		// applyStyleToTabs();
		// }
		// });

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
		alreadyLoading = true;
		if (currentConfiguration.getTextEditorOpt().getOpenedFileList().size() != 0) {
			for (String pathToOpen : currentConfiguration.getTextEditorOpt().getOpenedFileList()) {
				File toLoadFile = new File(pathToOpen);
				if (toLoadFile.exists()) {
					addTab(toLoadFile, !loadedAlmostOne);
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
			removeTab(0);

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
	}

	public void findRightStyleFromComboNSelect(File source) {

		if (source == null) {
			return;
		}

		for (SupportedFileTextEditor current : SupportedFileTextEditor.values()) {
			if (FilenameUtils.getExtension(source.getName()).toLowerCase().endsWith(current.senzapunto.toLowerCase())) {
				comboFileFormat.setSelectedItem(current);
				applyFileFormatFromCombo();
				return;
			}
		}

		// default unrecognized
		comboFileFormat.setSelectedItem(SupportedFileTextEditor.TXT);
		applyFileFormatFromCombo();

	}

	public void applyFileFormatFromCombo() {
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

	public boolean checkFilePresence(File match) {
		for (TabTextEditorObj iterator : loadedList) {
			if (iterator.loadedFile != null && iterator.loadedFile.equals(match)) {
				mainTabList.setSelectedComponent(iterator.currentScrollPane);
				return true;
			}
		}

		return false;
	}

	// TABBED EDITOR FUNCTIONS //

	public void loadFile(final File fileToLoad) {
		
		logger.debug("Started file loading into new tab: "+fileToLoad.getAbsolutePath()+"\n\n");
		loadedTabObj.textArea.setCursor(GuiUtils.CURSOR_WAIT);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				alreadyLoading = true;
				boolean fileAlreadyPresent = checkFilePresence(fileToLoad);
				if (fileToLoad == null || !fileToLoad.exists() || fileAlreadyPresent) {
					alreadyLoading = false;
					loadedTabObj.textArea.setCursor(GuiUtils.CURSOR_TEXT);

				} else {

					// add new tab with selected file
					try {
						addTab(fileToLoad, false);
						logger.debug(fileToLoad.getAbsolutePath()+" -> Loaded!");
					} catch (Throwable e1) {
						String msg = "Error occurred loading file into new tab: "+fileToLoad.getAbsolutePath()+"\n\n";
						logger.error(msg);
						JOptionPane.showMessageDialog(null, msg, msg, JOptionPane.ERROR_MESSAGE);
					}

					loadedTabObj.loadedFile = fileToLoad;
					lblCurrentFile.setText(loadedTabObj.loadedFile.getName());
					lblCurrentFile.setToolTipText(loadedTabObj.loadedFile.getAbsolutePath());
					findRightStyleFromComboNSelect(fileToLoad);
					applySelectedTheme();
					loadedTabObj.unsaved = false;
					updateSavedLabelNTab(null);
					refreshMessageAndTabTooltip("[File loaded]");
					currentConfiguration.getFileOpt().setLastSrcFolderPath(fileToLoad.getParentFile().getAbsolutePath());

					alreadyLoading = false;
					loadedTabObj.textArea.setCursor(GuiUtils.CURSOR_TEXT);
					logger.debug("TextEditor.loadFile() completed - Exiting");
				}
			}
		});

	}

	public void addTab(File file, boolean first) {
		isAddingTab = true;
		backupCurrentBeforeSwitch();
		String name = (file != null) ? file.getName() : DEFAULT_NEW_FILE_NAME;
		int newIndex = mainTabList.getTabCount();

		TabTextEditorObj newTabNode = new TabTextEditorObj(file, "", getChangeListener(),
				currentConfiguration.getTextEditorOpt().txtAreaDetailLevel);

		loadedList.add(newTabNode);
		mainTabList.addTab(name, newTabNode.currentScrollPane);
		loadedTabObj = newTabNode;
		FileStuff.appendFileTxtArea(file, loadedTabObj.textArea, true);
		mainTabList.setTabComponentAt(newIndex, getCustomClosableTab(name, newIndex));
		mainTabList.setSelectedIndex(newIndex);
		loadedTabObj.textArea.discardAllEdits();

		loadedTabObj.textArea.addMouseWheelListener(new TextEditorMouseWheelListener(currentTextEditor));
		loadedTabObj.textArea.setFont(currentConfiguration.getTextEditorOpt().font);
		loadedTabObj.unsaved = false;
		isAddingTab = false;
	}

	private void removeTab(int index) {

		if (askSave(index)) {
			isRemovingTab = true;
			if (index > 0 && mainTabList.getTabCount() > 1) {
				loadedList.remove(index);
				mainTabList.remove(index);
				if (mainTabList.getTabCount() > index) {
					mainTabList.setSelectedIndex(index);
				} else {
					mainTabList.setSelectedIndex(index - 1);
					updateSavedLabelNTab(index - 1);
				}
				tabRefreshAfterUpdate();

			} else if (index == 0 && mainTabList.getTabCount() > 1) {
				loadedList.remove(0);
				mainTabList.remove(0);
				mainTabList.setSelectedComponent(loadedList.get(0).currentScrollPane);
				tabRefreshAfterUpdate();

			} else if (index == 0) {
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
			int index = mainTabList.getSelectedIndex() - 1;
			if (index < 0) {
				index = mainTabList.getTabCount() - 1;
			}
			mainTabList.setSelectedIndex(index);
		} catch (Exception e) {
			logger.error("Cannot go to previous tab!", e);
		}
	}

	public void goToNextTab() {
		try {
			int index = mainTabList.getSelectedIndex() + 1;
			if (index >= mainTabList.getTabCount()) {
				index = 0;
			}
			mainTabList.setSelectedIndex(index);
		} catch (Exception e) {
			logger.error("Cannot go to next tab!", e);
		}
	}

	public void closeCurrentTab() {
		int index = mainTabList.getSelectedIndex();
		try {
			JButton closeTabButton = (JButton) (getPanelFromTabHeader(index))
					.getComponent(TabUtils.TAB_BUTTON_CLOSE_ORDER);
			closeTabButton.doClick();
		} catch (Exception e) {
			logger.error("Error closing current tab: " + index, e);
		}
	}

	private void backupCurrentBeforeSwitch() {
		try {
			TabTextEditorObj toSaveObj = loadedList.get(lastIndexTabbedPane);
			toSaveObj.loadedFile = (loadedTabObj.loadedFile != null)
					? new File(loadedTabObj.loadedFile.getAbsolutePath())
					: null;
			toSaveObj.unsaved = loadedTabObj.unsaved;
		} catch (Exception e) {
			logger.error("Error during backup before switch :(", e);
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
		loadedTabObj.textArea.setCursor(GuiUtils.CURSOR_TEXT);
		tabJustSwitched = false;
		loadedTabObj.textArea.requestFocus();
		applyCurrentGraphicsTextArea(loadedTabObj.textArea);

		// update load target textArea
		refreshTxtAreaReference();
		resizeFrameToDefault();

		refreshTree();

		// update eventually opened info frames
		if (propertiesFrame != null) {
			if (loadedTabObj != null && loadedTabObj.loadedFile != null && loadedTabObj.loadedFile.exists()) {
				propertiesFrame.refreshValues(loadedTabObj.loadedFile);
			}
		}
	}

	public void refreshTree() {
		// leftSplitTreeFiles.setBackground(loadedTabObj.textArea.getBackground());
		// leftSplitTreeFiles.setForeground(loadedTabObj.textArea.getForeground());
		//
		if (loadedTabObj.loadedFile != null && loadedTabObj.loadedFile.exists() && btnToggleFileView.isSelected()) {
			splitPaneMain.getLeftComponent().setVisible(true);
			splitPaneMain.setDividerLocation(currentConfiguration.getTextEditorOpt().dividerFileTreePosX);
			leftSplitTreeFiles.setModel(
					new FileTreeNamedModel(new FileNamed(loadedTabObj.loadedFile.getParentFile().getParentFile())));
			TreeUtils.selectFileInNamedFileTree(loadedTabObj.loadedFile, leftSplitTreeFiles);
		} else {
			splitPaneMain.setDividerLocation(0.0);
		}

		leftSplitTreeFiles.setCellRenderer(new FileNameCellRenderer());
		SplitPaneUtils.setDividerColors(null, Color.BLACK, splitPaneMain);
	}

	/** updates textArea reference in panels **/
	public void refreshTxtAreaReference() {

		if (searchBox != null && searchBox.isVisible()) {
			searchBox = TextAreaDialogs.getSearchDialog(parentFrame, loadedTabObj.textArea, dynLblSearchEsit);
		}

		if (escapeReplacerPanel != null && EscapeReplacerPanel.active) {
			escapeReplacerPanel.outputTextArea = loadedTabObj.textArea;
		}
	}

	private void applyCurrentGraphicsTextArea(RSyntaxTextArea textArea) {
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

	private static JPanel getPanelFromTabHeader(int index) {
		JPanel tabPanel = null;
		try {
			tabPanel = (JPanel) mainTabList.getTabComponentAt(index);
		} catch (Exception e) {
			logger.error("Error retrieving panel from header tabbed pane panel", e);
		}
		return tabPanel;
	}

	private static boolean setIconToTab(String iconPath, int index) {
		try {
			SupportedFileTextEditor fileType = (SupportedFileTextEditor) comboFileFormat.getSelectedItem();
			JLabel tabHeadIcon = (JLabel) (getPanelFromTabHeader(index)).getComponent(TabUtils.TAB_ICON_ORDER);
			if (loadedTabObj.loadedFile == null || loadedTabObj.unsaved) {
				tabHeadIcon.setIcon(new ImageIcon(iconPath));
			} else if (fileType.equals(SupportedFileTextEditor.TXT)) {
				tabHeadIcon.setIcon(new ImageIcon(iconPath));
			} else {
				MyFileView fileView = new MyFileView();
				tabHeadIcon.setIcon(fileView.getIcon(loadedTabObj.loadedFile));
			}
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
		} else {
			tooltip = title;
		}

		String icon = "";
		if (DEFAULT_NEW_FILE_NAME.equals(title)) {
			icon = IconsPathConfigurator.ICON_DOC_EMPTY;
		} else {
			icon = IconsPathConfigurator.ICON_DOC_SAVED;
		}
		JPanel customTab = TabUtils.getCustomClosableTab(title, tooltip, icon, getTabCloseListener(index));
		customTab.setMinimumSize(new Dimension(150, 18));
		customTab.setMaximumSize(new Dimension(1000, 18)); // "FACCIAMO... MILLE!" (cit.)
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
		} else {
			tabPanel.setToolTipText(DEFAULT_NEW_FILE_NAME);
		}
	}
	
	/**
	 * Use this to move a tab left or right by one position
	 * @param leftDirection
	 * @deprecated
	 */
	public void moveCurrentTab(boolean leftDirection) {
		int currentIndexSelected = mainTabList.getSelectedIndex();
		int newIndex;
		if(leftDirection) {
			// LEFT
			newIndex = currentIndexSelected-1;
			if(isExtremeLeftOrRightCurrentTab(leftDirection)) {
				newIndex = mainTabList.getTabCount()-1;
			}
		}else {
			// RIGHT
			newIndex = currentIndexSelected+1;
			if(isExtremeLeftOrRightCurrentTab(leftDirection)) {
				newIndex = 0;
			}
		}
		
		switchTwoTabs(currentIndexSelected, newIndex);
	}
	
	private boolean switchTwoTabs(int indexA, int indexB) {
		int maxIndex = mainTabList.getTabCount()-1;
		boolean error1 = indexA >= maxIndex || indexB >= maxIndex;
		boolean error2 = indexA < 0 || indexB < 0;
		if(error1 || error2) {
			return false;
		}
		
		moveTabs(indexA, indexB);
		
		return true;	
	}
	
	private void moveTabs(int indexA, int indexB) {
		Component comp = mainTabList.getComponentAt(indexA);
        String title = mainTabList.getTitleAt(indexA);
        mainTabList.removeTabAt(indexA);
        mainTabList.insertTab(title, null, comp, null, indexB);
        
        TabTextEditorObj back = loadedList.get(indexA);
        loadedList.remove(indexA);
        loadedList.add(indexB, back);
	}
	
	private boolean isExtremeLeftOrRightCurrentTab(boolean leftDirection) {
				
		int nTabs = mainTabList.getTabCount();
		int currentTabIndex = mainTabList.getSelectedIndex();
		
		if (leftDirection) {
			if ((currentTabIndex == 0)) {
				return true;
			} else {
				return false;
			} 
		}else {
			if ((currentTabIndex == nTabs - 1)) {
				return true;
			} else {
				return false;
			} 
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

	private DocumentListener getChangeListener() {
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

	public static boolean saveAllUnsavedExisting() throws IOException {
		boolean done = true;
		for (TabTextEditorObj element : loadedList) {
			if (element.loadedFile != null && element.loadedFile.exists()) {
				if (!FileStuff.writeStringToFile(loadedTabObj.textArea.getText(), loadedTabObj.loadedFile, true)) {
					done = false;
				}
			}
		}
		return done;
	}

	public static void applyStyleToTabs() {
		if (mainTabList != null) {
			mainTabList.setUI(new CustomTabbedPaneUI_v1(null, null, null, TextEditor.mainTabList,
					TabUtils.TAB_BUTTON_CLOSE_ORDER));
		}
		resetDividerColor();
	}

	public static void resetDividerColor() {
		if (TextEditor.splitPaneMain != null) {
			SplitPaneUtils.setDividerColors(null, Color.DARK_GRAY, TextEditor.splitPaneMain);
		}
	}

	public void startRefreshLabelCurrFIleThread() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				while (mainGUI.mainAppRunning) {
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
								lblCurrentFile.setText(loadedTabObj.loadedFile.getAbsolutePath());
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

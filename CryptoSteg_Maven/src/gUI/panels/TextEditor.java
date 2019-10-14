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
import om.SupportedFileTextEditor;
import om.TabTextEditorObj;
import om.TextEditorOption;
import resources.GeneralConfig;
import resources.IconsPathConfigurator;
import tabbededitor.CustomTabbedEditor;
import tabbedpaneutils.CustomTabbedPaneUI_v1;
import tabbedpaneutils.TabUtils;
import textareautil.SelectionDto;
import textareautil.TextLineNumber;
import utils.RsyntaxUtils;
import utils.SysUtils;
import utils.dialog.ColorDialogUtils;
import utils.panels.ClipboardPanel;
import utils.panels.MapTextAreaPanel;
import utils.panels.arch.PanelType;
import utils.panels.arch.ParentPanel;
import variousutils.HandleString;

import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.drjekyll.fontchooser.FontDialog;
import org.fife.rsta.ui.GoToDialog;
import org.fife.rsta.ui.search.FindDialog;
import org.fife.rsta.ui.search.ReplaceDialog;
import org.fife.ui.rtextarea.RUndoManager;

import com.minhasKamal.ultimateCalculator.CalculatorMainApp;

import amfileutils.FileStuff;
import conversion.Text2Speech;
import crypt.aes.AesCipher;
import dialogutils.GenericFileChooser;
import enums.Themes;
import enums.ThemesWrapper;
import frameutils.frame.ColorPickerFrame;
import frameutils.frame.EscapeReplacerFrame;
import frameutils.frame.FilePropertiesFrame;
import frameutils.utils.GuiUtils;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;
import java.awt.event.ActionEvent;
import javax.swing.JScrollPane;
import javax.swing.ImageIcon;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.event.ChangeEvent;
import javax.swing.UIManager;
import javax.swing.JCheckBox;
import javax.swing.JSeparator;
import javax.swing.JComboBox;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;

import gUI.panels.subpanels.texteditor.TextEditorFooterPanel;
import gUI.panels.subpanels.texteditor.TextEditorHeaderPanel;
import gUI.utils.filedialog.GenericFileChooserDialog;
import gUI.utils.filedialog.TemplatesChoosePanel;
import gUI.utils.shortcutshandler.TextEditorShortcutListener;
import gUI.utils.texteditor.TabbedEditorOwnerScrYpT;
import gUI.utils.texteditor.TextEditorUtils;
import gUI.utils.tooltip.TextEditorTooltips;
import gUI.utils.tree.FileTreeModel;
import gUI.utils.tree.TreeCellRendererDark;
import gUI.utils.tree.texteditor.TreeFileMouseListenerTxtEditor;
import gui.ImageSelector2TextFrame;
import gui.PdfSelector2TextFrame;
import guistyle.GUIStyelSettings;

import javax.swing.JPasswordField;
import java.awt.Cursor;
import java.awt.Dimension;
import javax.swing.JToggleButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;

public class TextEditor extends JPanel {
	
	// TextFileWriter logger creation
	static Logger logger = Logger.getLogger(TextEditor.class);
	
	public static final int timeoutAdvice = 9000;
	public volatile boolean renewedAdviceTimeout = false;
	
	public int fileTreeAncestorLevel = GeneralConfig.DEFAULT_FILE_TREE_ANCESTOR_LEVEL;

	public static final String AES_KEY_PLACEHOLDER = "#### (AES KEY) ####";
	public static final char DEFAULT_PSW_CHAR = '\u25CF';
	
	// this is the color of the left panel file navigator
	public Color fileNavColorBckg = TextEditorOption.DEFAULT_FILE_PANEL_BACKGROUND_COL;

	// RESOURCE FILES //
	private final static String LOREM_IPSUM = GeneralConfig.RESOURCES_DIR  + "Templates/LoremIpsum.txt";

	// STATIC phrases
	public static final String ROOT_WORKSPACE_LBL = "Workspace: ";
	public static final String ROOT_STANDARD_LBL = "Root: ";
	
	// GUI Objects //
	public JCheckBox chckbxAesShowKey;
	public JCheckBox chckbxShowTabGrid;
	public JCheckBox chckbxLockLanguage;

	public JLabel lblSIZE;
	public JLabel lblMargin;
	public JLabel lBLMargin;
	public JLabel lblFontSize;
	public JLabel labelTabSize;

	private JLabel lblLockNavigator;
	public JLabel lblCurrentDir;
	public volatile JLabel dynLblCurrentDir;

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
	public JButton btnCalculator;
	public JButton btnColorSampler;
	// TODO add reload themes folder and open themes folder

	public JButton toggleButtonFileTreeNewWorkspace;
	public JButton toggleButtonFileTreeWorkspace;
	public JButton toggleButtonFileTreeLocked;
	
	public JToggleButton btnToggleFileView;
	public JToggleButton toggleBtnMap;
	public JToggleButton toggleBtnClipboard;
	
	private JSpinner tabSizeSpinner;

	private JSeparator separator_NO_HIDE;
	private JSeparator separator_NO_HIDE_2;
	public JSeparator separator_1;
	public JSeparator separator_2;

	public JComboBox<SupportedFileTextEditor> comboFileFormat;
	public JComboBox<ThemesWrapper> comboStylesScheme;
	public Vector<ThemesWrapper> themeList = new Vector<ThemesWrapper>();

	public JSlider sliderFontSize;
	public JSlider slider_margin;

	public JSplitPane splitPaneMain;
	public JSplitPane splitPaneMainContainer;
	public JScrollPane leftSplitPane;
	public JScrollPane rightSplitPane;
	public JPanel leftSplitContainer; 
	public ParentPanel rightSplitContainer; 
	public JTree leftSplitTreeFiles;
	private JPasswordField txtAesKey; // field containing fast crypt text user key, sinchronized from ini file as

	// DIALOGS AND JFRAMES
	public FindDialog searchBox;
	public ReplaceDialog replaceDialog;
	public GoToDialog goToLineDialog;
	public EscapeReplacerFrame escapeReplacerPanel;
	public ImageSelector2TextFrame imageParserBox;
	public PdfSelector2TextFrame pdfParserBox;
	public TextLineNumber lineNumberPanel;
	public FilePropertiesFrame propertiesFrame;
	public CalculatorMainApp calculatorFrame;
	public Thread calculatorMainThread;
	public ColorPickerFrame colorPickerFrame;

	public boolean isCrypterOn = false;

	public JFrame parentFrame; // the frame that instantiate this object

	public JScrollPane mainScrollPane;
	public JPanel mainPanel;
	public TextEditorHeaderPanel headerPanel;
	public TextEditorFooterPanel footerPanel;

	// GENERAL FIELDS //
	private boolean uppercase;
	private String backupText;

	// flag to signal if function is already running
	public boolean askSaveBeforeClose = true;

	public String CURRENT_EXT_OUT = ".txt";
	public Color LBL_ADVICE_COLOR;

	public RUndoManager manager;
	public INItializer currentConfiguration;
	public TextEditor currentTextEditor; // current object
	public Text2Speech objSpeaker; // field containing the current object used to speak text

	public MainGUI mainApplication;
	public TextEditorUtils textEditorUtils;

	public TextEditorShortcutListener shortcutsListener;
	public TreeFileMouseListenerTxtEditor mouseListenerTreeFilePanel;
	
	public ArrayList<CustomTabbedEditor> tabbedEditorsList;
	public int currentTabbedEditorSelected = 0;
	
	public boolean isPreloading = false;
	
	//////////////// CONSTRUCTOR /////////////////////

	public TextEditor(MainGUI mainGui, INItializer currentConf) {
		
		// skip refresh during preload in case of settings to false
		isPreloading = !GeneralConfig.PRELOAD_TXT_EDITOR_FLAG;
		
		// Initialize text editor Utils reference
		textEditorUtils = new TextEditorUtils(this);

		logger.info(this.getClass().getName() + " Starting...");

		setBorder(BorderFactory.createEmptyBorder());
		setBackground(Color.BLACK);
		setLayout(new MigLayout("fill, insets 1, hidemode 2","[300px:"+MainGUI.screenSize.getWidth()+"px:"+MainGUI.screenSize.getWidth()+"px,grow]",
				"[300px:"+MainGUI.screenSize.getHeight()+"px:"+MainGUI.screenSize.getHeight()+"px,grow]"));

		// initialize OBJECTS used in GUI //
		currentConfiguration = currentConf;
		parentFrame = mainGui.getMainFrame();
		mainApplication = mainGui;
		currentTextEditor = this;
		LBL_ADVICE_COLOR = new Color(124, 252, 0);
		uppercase = false;
		fileNavColorBckg = currentConfiguration.getTextEditorOpt().filePanelBackCol;

		// MAIN PANEL
		mainPanel = new JPanel();
		mainPanel.setBackground(Color.DARK_GRAY);
		mainPanel.setMinimumSize(new Dimension(800, 300));
		mainPanel.setMaximumSize(GuiUtils.getScreenSizePerc(98, parentFrame));
		mainPanel.setLayout(new MigLayout("fill, insets 1, hidemode 2", "[25px:2%:2.5%][25px:2%:2.5%][25px:2%:2.5%][25px:2%:2.5%][25px:2%:2.5%][25px:2%:2.5%][25px:2%:2.5%][20px:n:60px][30px:30px:30px][:3%:4%][:60px:70px][40px:2%:50px][25px:2%:2.5%][25px:2%:2.5%][25px:2%:2.5%][25px:2%:2.5%][25px:2%:2.5%][50px:2%:3.5%][:50px:50px][20][30][][25px:2%:2.5%][25px:2%:2.5%][25px:2%:2.5%][25px:2%:2.5%][25px:2%:2.5%][25px:2%:2.5%][5px:5px:5px][25px:50px:60px][25px:50px:60px][55px:55px:55px][40px:40px:40px][45px:45px:45px][30px:35px:35px][10px:30px,grow]", "[:0px:35px][0px:5px][:0px:25px][:0px:25px][0px:4px][0px:4px][300px,grow 300,fill][:0px:30px]"));
		
		mainScrollPane = new JScrollPane();
		mainScrollPane.setMinimumSize(new Dimension(1100, 300));
		mainScrollPane.setBorder(BorderFactory.createEmptyBorder());
		mainScrollPane.setBackground(Color.DARK_GRAY);
		mainScrollPane.setViewportView(mainPanel);
		add(mainScrollPane, "cell 0 0,grow");
		
		// HEADER
		headerPanel = new TextEditorHeaderPanel(this);
		mainPanel.add(headerPanel, "cell 0 0 36 1,grow");
		
		toggleBtnMap = new JToggleButton("");
		toggleBtnMap.setMinimumSize(GeneralConfig.MINIMUM_BUTTON_SIZE);
		toggleBtnMap.setToolTipText(TextEditorTooltips.tooltipMapViewBtn);
		toggleBtnMap.setCursor(GuiUtils.CURSOR_HAND);
		toggleBtnMap.setIcon(new ImageIcon(IconsPathConfigurator.ICON_MAP));
		mainPanel.add(toggleBtnMap, "cell 27 2");
		
		toggleBtnClipboard = new JToggleButton("");
		toggleBtnClipboard.setMinimumSize(GeneralConfig.MINIMUM_BUTTON_SIZE);
		toggleBtnClipboard.setToolTipText(TextEditorTooltips.tooltipClipboardViewBtn);
		toggleBtnClipboard.setCursor(GuiUtils.CURSOR_HAND);
		toggleBtnClipboard.setIcon(new ImageIcon(IconsPathConfigurator.ICON_CLIPBOARD));
		mainPanel.add(toggleBtnClipboard, "cell 27 3");
		
		// FOOTER
		footerPanel = new TextEditorFooterPanel(this);
		mainPanel.add(footerPanel, "cell 0 7 36 1,grow");
		
		comboFileFormat = new JComboBox<SupportedFileTextEditor>();
		comboFileFormat.setFont(GeneralConfig.BUTTON_TAB_HEAD_FONT);
		comboFileFormat.setMinimumSize(new Dimension(75, 26));
		comboFileFormat.setToolTipText(TextEditorTooltips.tooltipComboFileFormat);
		comboFileFormat.setCursor(GuiUtils.CURSOR_HAND);
		comboFileFormat.setBackground(new Color(100, 149, 237));
		comboFileFormat.setModel(new DefaultComboBoxModel<SupportedFileTextEditor>(SupportedFileTextEditor.values()));
		mainPanel.add(comboFileFormat, "cell 4 3 3 1,growx");
		
		// ADD ONLY AFTER COMBO INITIALIZATION
		tabbedEditorsList = new ArrayList<CustomTabbedEditor>();
		CustomTabbedEditor initialTabbedEditor = new CustomTabbedEditor(currentConfiguration, new TabbedEditorOwnerScrYpT(this));
		tabbedEditorsList.add(initialTabbedEditor);
		
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
		btnParseImageText.setCursor(GuiUtils.CURSOR_HAND);
		btnParseImageText.setIcon(new ImageIcon(IconsPathConfigurator.ICON_CONVERT_OCR_IMAGE));
		btnParseImageText.setToolTipText(TextEditorTooltips.tooltipParseImage);
		mainPanel.add(btnParseImageText, "cell 22 2,alignx center");

		btnParsePdfText = new JButton("");
		btnParsePdfText.setToolTipText(TextEditorTooltips.tooltipParsePdfTxt);
		btnParsePdfText.setCursor(GuiUtils.CURSOR_HAND);
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

		btnColorSampler = new JButton("");
		btnColorSampler.setToolTipText(TextEditorTooltips.tooltipBtnColorSampler);
		btnColorSampler.setIcon(new ImageIcon(IconsPathConfigurator.ICON_COLOR_SAMPLER));
		mainPanel.add(btnColorSampler, "cell 26 3");
		
		labelTabSize = new JLabel("Tab Size:");
		mainPanel.add(labelTabSize, "cell 29 2");
		labelTabSize.setHorizontalAlignment(SwingConstants.RIGHT);
		labelTabSize.setForeground(SystemColor.menu);
		
		tabSizeSpinner = new JSpinner();
		tabSizeSpinner.setToolTipText(TextEditorTooltips.tooltipTabSize);
		tabSizeSpinner.setCursor(GuiUtils.CURSOR_HAND);
		mainPanel.add(tabSizeSpinner, "cell 30 2,growx");
		tabSizeSpinner.setModel(new SpinnerNumberModel(currentConf.getTextEditorOpt().getTabSize(), 1, 9, 1));

		lblMargin = new JLabel("Margin:");
		lblMargin.setHorizontalAlignment(SwingConstants.CENTER);
		lblMargin.setForeground(SystemColor.menu);
		lblMargin.setBackground(UIManager.getColor("CheckBox.darkShadow"));
		mainPanel.add(lblMargin, "cell 31 2,alignx center");

		lBLMargin = new JLabel("12");
		lBLMargin.setForeground(Color.WHITE);
		mainPanel.add(lBLMargin, "cell 32 2,alignx center");

		btnToggleFileView = new JToggleButton("");
		btnToggleFileView.setMinimumSize(GeneralConfig.MINIMUM_BUTTON_SIZE);
		btnToggleFileView.setToolTipText(TextEditorTooltips.tooltipFileViewBtn);
		btnToggleFileView.setCursor(GuiUtils.CURSOR_HAND);
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

		chckbxLockLanguage = new JCheckBox("Lock");
		chckbxLockLanguage.setToolTipText(TextEditorTooltips.tooltipChckbxLockLanguage);
		chckbxLockLanguage.setCursor(GuiUtils.CURSOR_HAND);
		chckbxLockLanguage.setIconTextGap(1);
		chckbxLockLanguage.setForeground(Color.WHITE);
		chckbxLockLanguage.setFont(new Font("SansSerif", Font.BOLD, 12));
		mainPanel.add(chckbxLockLanguage, "cell 7 3 2 1");

		comboStylesScheme = new JComboBox<ThemesWrapper>();
		comboStylesScheme.setMaximumSize(new Dimension(125, 32767));
		comboStylesScheme.setFont(GeneralConfig.BUTTON_TAB_HEAD_FONT);
		themeList = initComboStyleScheme();
		comboStylesScheme.setToolTipText(TextEditorTooltips.tooltipComboStyleScheme);
		comboStylesScheme.setCursor(GuiUtils.CURSOR_HAND);
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
		chckbxShowTabGrid.setCursor(GuiUtils.CURSOR_HAND);
		chckbxShowTabGrid.setToolTipText(TextEditorTooltips.tooltipShowTabGrid);
		chckbxShowTabGrid.setSelected(currentConfiguration.getTextEditorOpt().isTabLineVisible());
		chckbxShowTabGrid.setForeground(SystemColor.menu);
		chckbxShowTabGrid.setBackground(Color.DARK_GRAY);
		mainPanel.add(chckbxShowTabGrid, "cell 29 3 2 1");
				
		lblFontSize = new JLabel("Font size: ");
		lblFontSize.setHorizontalAlignment(SwingConstants.CENTER);
		lblFontSize.setForeground(SystemColor.menu);
		mainPanel.add(lblFontSize, "cell 31 3,alignx center");
		
		lblSIZE = new JLabel("");
		lblSIZE.setFont(new Font("SansSerif", Font.PLAIN, 10));
		lblSIZE.setForeground(SystemColor.window);
		mainPanel.add(lblSIZE, "cell 32 3,alignx center");
		lblSIZE.setText(String.valueOf(GeneralConfig.DEFAULT_FONT.getSize()) + " Px");

		getCurrentTabEditor().mainTabList.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

		// PARENT CONTAINER (main lv.2, right)
		splitPaneMainContainer = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPaneMainContainer.setBackground(currentConfiguration.getTextEditorOpt().getFilePanelBackCol());

		// 2ND LEVEL CONTAINER SPLIT PANE (left, center)
		splitPaneMain = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPaneMain.setBackground(currentConfiguration.getTextEditorOpt().getFilePanelBackCol());
		
		leftSplitTreeFiles = new JTree(new FileTreeModel(null));
		mouseListenerTreeFilePanel = new TreeFileMouseListenerTxtEditor(leftSplitTreeFiles, this);
		leftSplitTreeFiles.setCellRenderer(new TreeCellRendererDark());
		leftSplitTreeFiles.setBackground(fileNavColorBckg);
		leftSplitTreeFiles.setForeground(Color.white);
		leftSplitPane = new JScrollPane(leftSplitTreeFiles);
		leftSplitPane.setBorder(new EmptyBorder(0, 0, 0, 0));
		leftSplitPane.setPreferredSize(new Dimension(200, 500));
		leftSplitContainer = new JPanel();
		leftSplitContainer.setBackground(fileNavColorBckg);
		leftSplitContainer.setForeground(Color.GRAY);
		leftSplitContainer.setLayout(new MigLayout("", "[30px:40px][40px][2px:4px,grow][18px:20px:20px][18px:20px:20px][18px:20px:20px]", "[:25px:25px][2px:2px:2px][90%,grow][2px:2px:2px][:20px:20px]"));
		 
		initRightPane();
		rightSplitContainer.setBackground(fileNavColorBckg);
		rightSplitContainer.setForeground(Color.GRAY);
		rightSplitPane = new JScrollPane(rightSplitContainer);
		rightSplitPane.setBorder(new EmptyBorder(0, 0, 0, 0));
		rightSplitPane.setPreferredSize(new Dimension(200, 500));
		
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

		mainPanel.add(splitPaneMainContainer, "cell 0 5 36 2,grow");
		
		// Parent container split components
		splitPaneMainContainer.setRightComponent(rightSplitPane);
		splitPaneMainContainer.setLeftComponent(splitPaneMain);
		splitPaneMainContainer.setDividerLocation(parentFrame.getWidth()-100);
		
		// Main split init components
		splitPaneMain.setRightComponent(getCurrentTabEditor().mainTabList);
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
		mainPanel.add(separator_2, "cell 0 4 36 1,growx");

		getCurrentTabEditor().addTab(null, true); // comment for editing in windowuilder
		getCurrentTabEditor().isFirstTabAdded = true;

		sliderFontSize = new JSlider(4, 100);
		sliderFontSize.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
		sliderFontSize.setForeground(SystemColor.activeCaptionBorder);
		sliderFontSize.setBackground(UIManager.getColor("Button.shadow"));
		sliderFontSize.setValue(currentConfiguration.getTextEditorOpt().getFont().getSize());
		mainPanel.add(sliderFontSize, "cell 33 3 3 1,growx");

		separator_1 = new JSeparator();
		mainPanel.add(separator_1, "cell 0 1 36 1,growx");

		txtAesKey = new JPasswordField();
		txtAesKey.setHorizontalAlignment(SwingConstants.CENTER);
		txtAesKey.setEchoChar((char) 0);
		txtAesKey.setText(AES_KEY_PLACEHOLDER);
		mainPanel.add(txtAesKey, "cell 17 2 4 1,growx");
		txtAesKey.setColumns(10);
		txtAesKey.setFont(GeneralConfig.BUTTON_DEFAULT_FONT);
		txtAesKey.setEchoChar(DEFAULT_PSW_CHAR);
		AesCipher.check(txtAesKey);

		slider_margin = new JSlider(0, 100, 0);
		slider_margin.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
		slider_margin.setBackground(UIManager.getColor("Button.shadow"));
		mainPanel.add(slider_margin, "cell 33 2 3 1,growx");

		btnLoadTextFile = new JButton("");
		btnLoadTextFile.setToolTipText(TextEditorTooltips.tooltipLoadTextFile);
		btnLoadTextFile.setForeground(SystemColor.menuText);
		btnLoadTextFile.setIcon(new ImageIcon(IconsPathConfigurator.ICON_FILE_LOAD));
		mainPanel.add(btnLoadTextFile, "cell 0 2,grow");

		addHandlers();

		/*
		 * KEYBOARD SHORTCUTS - use custom listener
		 */
		shortcutsListener = new TextEditorShortcutListener(currentTextEditor);
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(shortcutsListener);

		startAesKeyCheckThread();

		try {
			textEditorUtils.applyStyleToButtons(currentTextEditor);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			logger.warn("Buttons styling gone wrong: ", e);
		}
		
		initGuiFromConfig(true);
		
		loadOpenedFilesAtStart();

		textEditorUtils.resizeFrameToDefault(mainApplication);

		textEditorUtils.refreshTree(getCurrentTabEditor().loadedTabObj.loadedFile, false);
		
		getCurrentTabEditor().loadedTabObj.textArea.requestFocusInWindow();
		
		// TODO REMOVE THIS?
		getCurrentTabEditor().mainTabList.setUI(new CustomTabbedPaneUI_v1(
				null, null, null, getCurrentTabEditor().mainTabList, TabUtils.TAB_BUTTON_CLOSE_ORDER));

		// set default placeholder visible
		txtAesKey.setEchoChar((char) 0);

		startRefreshFooterThread();
		
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				getCurrentTabEditor().loadedTabObj.textArea.setFont(currentConfiguration.getTextEditorOpt().getFont());
				getCurrentTabEditor().loadedTabObj.textArea.revalidate();
				getCurrentTabEditor().loadedTabObj.textArea.repaint();
			}
		});
		
		logger.debug("### TEXT WRITER module started");
	}

	private void initRightPane() {
		if (currentConfiguration.getTextEditorOpt().mapViewActive) {
			rightSplitContainer = MapTextAreaPanel.getSingletonLazy(textEditorUtils.getDialogManager());
		} else if (currentConfiguration.getTextEditorOpt().clipboardViewActive) {
			rightSplitContainer = ClipboardPanel.getSingletonLazy(textEditorUtils.getDialogManager(), currentConfiguration);
			((ClipboardPanel)rightSplitContainer).selectFromComboClipboards(currentConfiguration.getTextEditorOpt().lastFileOpenedClipboard);
		} else {
			// Default
			rightSplitContainer = MapTextAreaPanel.getSingletonLazy(textEditorUtils.getDialogManager());
		}
		rightSplitPane = new JScrollPane(rightSplitContainer);
		
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
		
		CustomTabbedEditor currentTabEditor = getCurrentTabEditor();

		// COLORS DEFAULT DEFINITIONS
		final Font DEFAULT = GeneralConfig.DEFAULT_FONT;
		final Color DEFAULT_BCKG_COL = currentConfiguration.getTextEditorOpt().backCol;
		final Color DEFAULT_FORE_COL = currentConfiguration.getTextEditorOpt().foreCol;
		
		// TAB SIZE SELECTOR
		tabSizeSpinner.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				getCurrentTabEditor().loadedTabObj.textArea.setTabSize((int)tabSizeSpinner.getValue());
				currentConfiguration.getTextEditorOpt().setTabSize((int)tabSizeSpinner.getValue());
			}
		});
		
		// FILE TREE - LOCK/UNLOCK TREE SYNC
		toggleButtonFileTreeLocked.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!currentConfiguration.getTextEditorOpt().treeLocked) {
					toggleButtonFileTreeLocked.setIcon(new ImageIcon(IconsPathConfigurator.ICON_PIN_GREEN));
					toggleButtonFileTreeLocked.setBackground(new Color(30,30,30));
					currentConfiguration.getTextEditorOpt().setTreeLocked(true);
					// SYNC config file
					currentConfiguration.writeSingleValue(INItializer.TEXT_EDITOR_OPT, "isTreeLocked", true);
				}else {
					toggleButtonFileTreeLocked.setIcon(new ImageIcon(IconsPathConfigurator.ICON_UNPIN));
					toggleButtonFileTreeLocked.setBackground(new Color(30,30,30));
					textEditorUtils.refreshTree(getCurrentTabEditor().loadedTabObj.loadedFile, true);
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
				textEditorUtils.refreshTree(getCurrentTabEditor().loadedTabObj.loadedFile, false);
				
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
					
					// REFRESH
					textEditorUtils.refreshTree(getCurrentTabEditor().loadedTabObj.loadedFile, true);
				}
			}
		});

		// PROPERTIES - CURRENT FILE
		btnPropertiesPanel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
				TabTextEditorObj loadedTabObj = getCurrentTabEditor().loadedTabObj;
				
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
									GuiUtils.centerComponent(propertiesFrame, 700, 220);
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

		// left divisor resize
		splitPaneMain.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (btnToggleFileView.isSelected() && getCurrentTabEditor().loadedTabObj.loadedFile != null && getCurrentTabEditor().loadedTabObj.loadedFile.exists()) {
					splitPaneMain.getLeftComponent().setVisible(true);
					currentConfiguration.getTextEditorOpt().setDividerFileTreePosX(splitPaneMain.getDividerLocation());
					currentConfiguration.writeSingleValue(INItializer.TEXT_EDITOR_OPT, "dividerFileTreePosX", currentConfiguration.getTextEditorOpt().getDividerFileTreePosX());
				}
			}
		});

		// right divisor resize
		splitPaneMainContainer.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				TextEditorOption opt = currentConfiguration.getTextEditorOpt();
				if ((opt.mapViewActive || opt.clipboardViewActive) && getCurrentTabEditor().loadedTabObj.textArea != null) {
					splitPaneMainContainer.getRightComponent().setVisible(true);
					
					int newRightPaneValue = parentFrame.getWidth()-splitPaneMainContainer.getDividerLocation();
					
					if (textEditorUtils.getTypeOfRightPanel().equals(PanelType.CLIPBOARD_PANEL)) {
						// In case of clipboard keep memory in ad-hoc option
						currentConfiguration.getTextEditorOpt().setDividerRightPanePosXClipboard(newRightPaneValue);
						currentConfiguration.writeSingleValue(INItializer.TEXT_EDITOR_OPT, "dividerRightPanePosXClipboard", newRightPaneValue);
					}else {
						currentConfiguration.getTextEditorOpt().setDividerRightPanePosX(newRightPaneValue);
						currentConfiguration.writeSingleValue(INItializer.TEXT_EDITOR_OPT, "dividerRightPanePosX", newRightPaneValue);
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
				fileTreeShowOrHide(currentConfiguration.getTextEditorOpt());
				textEditorUtils.refreshTree(getCurrentTabEditor().loadedTabObj.loadedFile, false);
			}
		});

		// MAP VIEW
		toggleBtnMap.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						if (toggleBtnMap.isSelected()) {
							toggleBtnMap.setBackground(GeneralConfig.BUTTON_BCKG_COLOR_TXT_EDITOR_DARKER);
							currentConfiguration.getTextEditorOpt().mapViewActive = true;
							textEditorUtils.setRightPanelContent(PanelType.MAP_TXTAREA);

						} else {
							toggleBtnMap.setBackground(GeneralConfig.BUTTON_BCKG_COLOR_TXT_EDITOR);
							currentConfiguration.getTextEditorOpt().mapViewActive = false;
						}
						currentConfiguration.getTextEditorOpt().clipboardViewActive = false;
						rightPaneShowOrHide(currentConfiguration.getTextEditorOpt());
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
						if (toggleBtnClipboard.isSelected()) {
							toggleBtnClipboard.setBackground(GeneralConfig.BUTTON_BCKG_COLOR_TXT_EDITOR_DARKER);
							currentConfiguration.getTextEditorOpt().clipboardViewActive = true;
							textEditorUtils.setRightPanelContent(PanelType.CLIPBOARD_PANEL);
						} else {
							toggleBtnClipboard.setBackground(GeneralConfig.BUTTON_BCKG_COLOR_TXT_EDITOR);
							currentConfiguration.getTextEditorOpt().clipboardViewActive = false;
						}
						currentConfiguration.getTextEditorOpt().mapViewActive = false;
						rightPaneShowOrHide(currentConfiguration.getTextEditorOpt());
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
				textEditorUtils.resizeFrameToDefault(mainApplication);
			}

			public void componentResized(ComponentEvent e) {
				textEditorUtils.resizeFrameToDefault(mainApplication);
			}

			public void componentMoved(ComponentEvent e) {
				textEditorUtils.resizeFrameToDefault(mainApplication);
			}

			public void componentHidden(ComponentEvent e) {
			}
		});

		// OPEN IN SCREEPT CRYPTER
		btnOpenInScreepT.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				TabTextEditorObj loadedTabObj = getCurrentTabEditor().loadedTabObj;

				CrypterPanel cipherPanel = mainApplication.getPanelCrypter();

				// current file not saved
				if (loadedTabObj.unsaved) {
					String currFileName = (loadedTabObj.loadedFile != null) ? loadedTabObj.loadedFile.getName()
							: footerPanel.lblCurrentFile.getText();
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
						if (TextEditorShortcutListener.flagCtrlPressed.get()) {
							SysUtils.cdIntoCommandLine(getCurrentTabEditor().loadedTabObj.loadedFile.getParentFile());
						} else {
							GuiUtils.openInFileSystem(getCurrentTabEditor().loadedTabObj.loadedFile.getParentFile());
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
				shortcutsListener.flagHoverBtnViewExpCmd.set(false);
				shortcutsListener.doControlActions();
			}

			public void mouseEntered(MouseEvent e) {
				shortcutsListener.flagHoverBtnViewExpCmd.set(true);
				shortcutsListener.doControlActions();
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
								getCurrentTabEditor().loadedTabObj.textArea,
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
						escapeReplacerPanel.setLocationRelativeTo(mainPanel);
//						GuiUtils.changeFrameStateLaterUnchecked(mainApplication.getMainFrame(), escapeReplacerPanel, JFrame.MAXIMIZED_BOTH);
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
						if (colorPickerFrame == null || !ColorPickerFrame.active) {
							colorPickerFrame = new ColorPickerFrame();
						}else {
							colorPickerFrame.dispatchEvent(new WindowEvent(colorPickerFrame, WindowEvent.WINDOW_CLOSING));
						}
						colorPickerFrame.setLocationRelativeTo(mainApplication.getMainFrame());
						GuiUtils.changeFrameStateLaterUnchecked(mainApplication.getMainFrame(), colorPickerFrame, JFrame.MAXIMIZED_BOTH);
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
							imageParserBox = new ImageSelector2TextFrame(parentFrame, getCurrentTabEditor().loadedTabObj.textArea,
									GeneralConfig.APPLICATION_NAME + " - Image2Text");
							imageParserBox.setVisible(true);
						} else {
							imageParserBox.dispatchEvent(new WindowEvent(imageParserBox, WindowEvent.WINDOW_CLOSING));
						}
						imageParserBox.setLocationRelativeTo(mainApplication.getMainFrame());
						GuiUtils.changeFrameStateLaterUnchecked(mainApplication.getMainFrame(), imageParserBox, JFrame.MAXIMIZED_BOTH);
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
							pdfParserBox = new PdfSelector2TextFrame(parentFrame, getCurrentTabEditor().loadedTabObj.textArea,
									GeneralConfig.APPLICATION_NAME + " - Pdf2Text");
							pdfParserBox.setVisible(true);
						} else {
							pdfParserBox.dispatchEvent(new WindowEvent(pdfParserBox, WindowEvent.WINDOW_CLOSING));
						}
						pdfParserBox.setLocationRelativeTo(mainApplication.getMainFrame());
						GuiUtils.changeFrameStateLaterUnchecked(mainApplication.getMainFrame(), pdfParserBox, JFrame.MAXIMIZED_BOTH);
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
							searchBox = textEditorUtils.textAreaDialogs.getSearchDialog(parentFrame,  null, textEditorUtils,"successAdvice", "errorAdvice");
							GuiUtils.setFrameIcon(IconsPathConfigurator.ICON_SEARCH, searchBox);
						} else {
							searchBox.setVisible(true);
							searchBox.toFront();
							searchBox.requestFocus();
						}
						searchBox.setLocationRelativeTo(mainApplication.getMainFrame());
						GuiUtils.changeFrameStateLaterUnchecked(mainApplication.getMainFrame(), searchBox, JFrame.MAXIMIZED_BOTH);
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
							goToLineDialog = textEditorUtils.textAreaDialogs.getGoToLineDialog(parentFrame);
						} else {
							goToLineDialog.dispatchEvent(new WindowEvent(mainApplication.getMainFrame(), WindowEvent.WINDOW_CLOSING));
							goToLineDialog = textEditorUtils.textAreaDialogs.getGoToLineDialog(parentFrame);
						}
						goToLineDialog.setLocationRelativeTo(mainApplication.getMainFrame());
						GuiUtils.changeFrameStateLaterUnchecked(mainApplication.getMainFrame(), goToLineDialog, JFrame.MAXIMIZED_BOTH);
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
									parentFrame, null, textEditorUtils,"successAdvice", "errorAdvice");
							GuiUtils.setFrameIcon(IconsPathConfigurator.ICON_REPLACE_TXT, replaceDialog);
						} else {
							replaceDialog.setVisible(true);
							replaceDialog.toFront();
							replaceDialog.requestFocus();
						}
						replaceDialog.setLocationRelativeTo(mainApplication.getMainFrame());
						GuiUtils.changeFrameStateLaterUnchecked(mainApplication.getMainFrame(), replaceDialog, JFrame.MAXIMIZED_BOTH);
					}
				});
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

		// VIEW/HIDE tab grids
		chckbxShowTabGrid.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (chckbxShowTabGrid.isSelected()) {
					currentConfiguration.getTextEditorOpt().setTabLineVisible(true);
					getCurrentTabEditor().loadedTabObj.textArea.setPaintTabLines(true);
				} else {
					currentConfiguration.getTextEditorOpt().setTabLineVisible(false);
					getCurrentTabEditor().loadedTabObj.textArea.setPaintTabLines(false);
				}
			}
		});

		// CRYPT TEXT
		ActionListener cryptListener = new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				TabTextEditorObj loadedTabObj = getCurrentTabEditor().loadedTabObj;

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
							getCurrentTabEditor().updateSavedLabelNTab(null);
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
				
				TabTextEditorObj loadedTabObj = getCurrentTabEditor().loadedTabObj;

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
				
				TabTextEditorObj loadedTabObj = getCurrentTabEditor().loadedTabObj;

				loadedTabObj.textArea.setFont(DEFAULT);

				RsyntaxUtils.setBackground(loadedTabObj.textArea, DEFAULT_BCKG_COL);
				loadedTabObj.textArea.setBackground(DEFAULT_BCKG_COL);
				RsyntaxUtils.setForeground(loadedTabObj.textArea, DEFAULT_FORE_COL);
				loadedTabObj.textArea.setForeground(DEFAULT_FORE_COL);

				slider_margin.setValue(10);
				sliderFontSize.setValue(loadedTabObj.textArea.getFont().getSize());
				lblSIZE.setText(String.valueOf(loadedTabObj.textArea.getFont().getSize()) + " Px");

				textEditorUtils.defaultTxtConfig(DEFAULT, DEFAULT_BCKG_COL, DEFAULT_FORE_COL, 10);
			}
		});

		// BACKGROUND TEXT COLOR
		btnBackgroundCol.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnBackgroundCol.setEnabled(false);
				Color esit = new ColorDialogUtils().askForColor(getCurrentTabEditor().loadedTabObj.textArea, false);
				if (esit != null) {
					currentConfiguration.getTextEditorOpt().setBackCol(esit);
					RsyntaxUtils.setBackground(getCurrentTabEditor().loadedTabObj.textArea, esit);
				}
				btnBackgroundCol.setEnabled(true);
			}
		});

		// FOREGROUND TEXT COLOR
		btnForegroundCol.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				btnForegroundCol.setEnabled(false);
				Color esit = new ColorDialogUtils().askForColor(getCurrentTabEditor().loadedTabObj.textArea, true);
				if (esit != null) {
					currentConfiguration.getTextEditorOpt().setForeCol(esit);
					RsyntaxUtils.setForeground(getCurrentTabEditor().loadedTabObj.textArea, esit);
					getCurrentTabEditor().loadedTabObj.textArea.setForeground(esit);
				}
				btnForegroundCol.setEnabled(true);
			}
		});

		// FONT CHOOSER PANEL
		btnFontChooser.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				TabTextEditorObj loadedTabObj = getCurrentTabEditor().loadedTabObj;

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
				TabTextEditorObj loadedTabObj = getCurrentTabEditor().loadedTabObj;

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

				FileStuff.appendFileTxtArea(new File(LOREM_IPSUM), getCurrentTabEditor().loadedTabObj.textArea, false);
			}
		});

		// TEXT AREA FORMATTER
		btntTextFormat.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textEditorUtils.formatText();
			}
		});

		// SLIDER FONT CHANGED
		sliderFontSize.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				TabTextEditorObj loadedTabObj = getCurrentTabEditor().loadedTabObj;
				int oldCaretPLinePos = loadedTabObj.textArea.getCaretPosition();
				
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						Font font = loadedTabObj.textArea.getFont();
						font = new Font(font.getName(), font.getStyle(), sliderFontSize.getValue());
						lblSIZE.setText(String.valueOf(loadedTabObj.textArea.getFont().getSize()) + " Px");
						currentConfiguration.getTextEditorOpt().setFont(loadedTabObj.textArea.getFont());
						loadedTabObj.textArea.setFont(font);
						
						if(oldCaretPLinePos > 0 ) {
							loadedTabObj.textArea.setCaretPosition(oldCaretPLinePos - 1);
							loadedTabObj.textArea.setCaretPosition(oldCaretPLinePos + 1);
						} else try {
							loadedTabObj.textArea.setCaretPosition(oldCaretPLinePos + 1);
							loadedTabObj.textArea.setCaretPosition(oldCaretPLinePos - 1);
						}catch(Exception e) {
						}
						
						loadedTabObj.textArea.revalidate();
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
						TabTextEditorObj loadedTabObj = getCurrentTabEditor().loadedTabObj;
						
						String selected = (loadedTabObj.textArea.getSelectedText() != null && loadedTabObj.textArea.getSelectedText().length() != 0)? 
								loadedTabObj.textArea.getSelectedText() : loadedTabObj.textArea.getText();
						
						if (objSpeaker != null && objSpeaker.getVoice() != null) {
							objSpeaker.getVoice().deallocate();
							btnSpeak.setBackground(new Color(176, 196, 222));
						} else {
							// else speak
							objSpeaker = new Text2Speech();
							btnSpeak.setBackground(Color.RED);
							objSpeaker.dospeak(selected , "kevin16", (headerPanel.sliderSpokenSpeed.getValue() / 100.0f));
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
				if (getCurrentTabEditor().loadedTabObj.textArea.canUndo()) {
					getCurrentTabEditor().loadedTabObj.textArea.undoLastAction();
				}
			}
		});

		// REDO LAST OPERATION ON TEXT
		btnRedo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				if (getCurrentTabEditor().loadedTabObj.textArea.canRedo()) {
					getCurrentTabEditor().loadedTabObj.textArea.redoLastAction();
				}

			}
		});

		// CLEAR TEXT AREA
		btnClear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				if (JOptionPane.showConfirmDialog(parentFrame, "Clear Text?") == JOptionPane.OK_OPTION) {
					getCurrentTabEditor().loadedTabObj.textArea.setText("");
				}
			}
		});

		// MAKE LOWERCASE AFTER LINE ENDERS
		btnLowerAfterDots.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				TabTextEditorObj loadedTabObj = getCurrentTabEditor().loadedTabObj;

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
				TabTextEditorObj loadedTabObj = getCurrentTabEditor().loadedTabObj;

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
				textEditorUtils.applyFileFormatFromCombo();
				getCurrentTabEditor().updateSavedLabelNTab(null);
			}
		});
		
		//  LOCK/UNLOCK auto recognizer of file type and syntax
		chckbxLockLanguage.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				textEditorUtils.findRightStyleFromComboNSelect(getCurrentTabEditor().loadedTabObj.loadedFile, !chckbxLockLanguage.isSelected());
				currentConfiguration.getTextEditorOpt().setDefaultAutoSyntaxSync(!chckbxLockLanguage.isSelected());
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
				JFrame panel = new JFrame();
				if (!TemplatesChoosePanel.active) {
					panel = new TemplatesChoosePanel(mainApplication.getMainFrame(), getCurrentTabEditor().loadedTabObj.textArea,
							comboFileFormat, btnTemplates);
					panel.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					panel.setBounds(750, 140, 500, 170);
					panel.setAlwaysOnTop(true);
					panel.setVisible(true);
					panel.setTitle("TEMPLATES manager - " + GeneralConfig.APPLICATION_NAME);
					btnTemplates.setEnabled(false);
				}
				panel.setLocationRelativeTo(mainPanel);
				GuiUtils.changeFrameStateLaterUnchecked(mainApplication.getMainFrame(), panel, JFrame.MAXIMIZED_BOTH);
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
				TabTextEditorObj loadedTabObj = getCurrentTabEditor().loadedTabObj;

				int start = loadedTabObj.textArea.getSelectionStart();
				int end = loadedTabObj.textArea.getSelectionEnd();
				int newEnd = 0;
				if (TextEditorShortcutListener.flagCtrlPressed.get()) {
					// remove character - open panel ad hoc
					String selection = JOptionPane.showInputDialog("Specify the charachter/string you want to remove");
					if (selection != null && !selection.equals("")) {
						newEnd = HandleString.removeCharachterFromTxtArea(selection, loadedTabObj.textArea, start, end);
					}
					TextEditorShortcutListener.flagCtrlPressed.set(false);
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
				shortcutsListener.flagHoverBtnRemoveSpaces.set(false);
				shortcutsListener.doControlActions();
			}

			public void mouseEntered(MouseEvent e) {
				shortcutsListener.flagHoverBtnRemoveSpaces.set(true);
				shortcutsListener.doControlActions();
			}

			public void mouseClicked(MouseEvent e) {
			}
		});

		// SET TEXT STYLE TO ITALICS
		btnItaliC.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TabTextEditorObj loadedTabObj = getCurrentTabEditor().loadedTabObj;

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
				TabTextEditorObj loadedTabObj = getCurrentTabEditor().loadedTabObj;

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
				TabTextEditorObj loadedTabObj = getCurrentTabEditor().loadedTabObj;

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
				TabTextEditorObj loadedTabObj = getCurrentTabEditor().loadedTabObj;

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
				TabTextEditorObj loadedTabObj = getCurrentTabEditor().loadedTabObj;

				boolean done = false;
				if (loadedTabObj.loadedFile == null || !loadedTabObj.loadedFile.exists()) {
					btnSaveTextFileAs.doClick();
					return;
				} else {
					try {
						done = FileStuff.writeStringToFile(loadedTabObj.textArea.getText(), loadedTabObj.loadedFile, true);
					} catch (IOException e1) {
						JOptionPane.showMessageDialog(currentTextEditor, "error Saving the selected file :(");
						logger.error("Exception happened!", e1);
					}finally {
						getCurrentTabEditor().changeMonManager.cleanFromHistory(loadedTabObj.loadedFile);
					}
					
					if (done) {
						loadedTabObj.unsaved = false;
						getCurrentTabEditor().updateSavedLabelNTab(null);
						textEditorUtils.refreshMessageAndTabTooltip("[File saved]");
					} else {
						textEditorUtils.errorAdvice("[Error - not saved]");
						footerPanel.lblCurrentFile.setText("(*)" + loadedTabObj.loadedFile.getName());
						footerPanel.lblCurrentFile.setForeground(Color.RED);
					}
					try {
						footerPanel.lblCurrentFile.setToolTipText(loadedTabObj.loadedFile.getCanonicalPath());
					} catch (IOException e1) {
						logger.error(e);
					}
				}
			}
		});

		// SAVE TEXT FILE AS
		btnSaveTextFileAs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TabTextEditorObj loadedTabObj = getCurrentTabEditor().loadedTabObj;
				boolean skipRefresh = true;

				if (!getCurrentTabEditor().alreadySaving) {
					// update signal value
					getCurrentTabEditor().alreadySaving = true;
					boolean done = false;
					String exportNamePreset = (loadedTabObj.loadedFile != null && !loadedTabObj.loadedFile.getName().equals(""))? loadedTabObj.loadedFile.getName() : "New."+comboFileFormat.getSelectedItem().toString().toLowerCase();
					exportNamePreset = ("".equals(FilenameUtils.getExtension(exportNamePreset)))? exportNamePreset.concat(comboFileFormat.getSelectedItem().toString().toLowerCase()): exportNamePreset;
					try {
						File selectedFile = GenericFileChooserDialog.fileWrite(parentFrame, currentConfiguration, exportNamePreset);

						if (selectedFile != null) {
							skipRefresh = false;
							selectedFile = textEditorUtils.adaptFileExt(selectedFile);
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
								
								getCurrentTabEditor().loadedTabObj.fileMonitorManager.removeFromListener(loadedTabObj.loadedFile);
								loadedTabObj.loadedFile = selectedFile;
								getCurrentTabEditor().loadedTabObj.fileMonitorManager.addFileToMonitorList(loadedTabObj.loadedFile);
								
								loadedTabObj.unsaved = false;
								getCurrentTabEditor().updateSavedLabelNTab(null);
								
								footerPanel.lblCurrentFile.setText(loadedTabObj.loadedFile.getName());
								footerPanel.lblCurrentFile.setToolTipText(loadedTabObj.loadedFile.getCanonicalPath());
								textEditorUtils.refreshMessageAndTabTooltip("[File saved]");
								currentConfiguration.getFileOpt().setLastDstFolderPath(
										loadedTabObj.loadedFile.getParentFile().getAbsolutePath());
								getCurrentTabEditor().changeMonManager.updateCurrMonitor(loadedTabObj.loadedFile);
							} else {
								skipRefresh = true;
								textEditorUtils.errorAdvice("[Error Saving selected file]");
							}
						}
						
					} catch (IOException e1) {
						skipRefresh = true;
						textEditorUtils.errorAdvice("[Error Saving selected file]");
						JOptionPane.showMessageDialog(currentTextEditor, "error Saving the selected file :(");
						logger.error("Exception happened!", e1);
					}
				}
				// update signal value
				getCurrentTabEditor().alreadySaving = false;
				if (!skipRefresh) {
					textEditorUtils.refreshTree(loadedTabObj.loadedFile, true);
				}
			}
		});

		// RELOAD INTO TEXT AREA FROM DISK
		btnReloadFromDisk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						getCurrentTabEditor().reloadCurrentTab(true);
						textEditorUtils.refreshMessageAndTabTooltip("[File Reloaded]");
					}
				});
			}
		});

		// LOAD TEXT FILE
		btnLoadTextFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				if (!getCurrentTabEditor().alreadyLoading) {

					File source = GenericFileChooser.fileRead(currentTextEditor);

					getCurrentTabEditor().loadFile(source);
				}
			}
		});

		// ADD NEW TAB
		btnNewTab.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				getCurrentTabEditor().addTab(null, false);
			}
		});

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
		
		return wrappers;
	}

	public boolean askSave(int index) {
		int askSave = JOptionPane.CANCEL_OPTION;
		TabTextEditorObj selected = getCurrentTabEditor().loadedList.getList().get(index);
		if (selected.unsaved) {
			askSave = JOptionPane.showConfirmDialog(currentTextEditor, "Do you want to save current file?",
					"Save current file?", JOptionPane.YES_NO_CANCEL_OPTION);
		} else {
			return true;
		}
		if (askSave == JOptionPane.OK_OPTION) {

			if (getCurrentTabEditor().loadedTabObj.loadedFile != null) {
				currentTextEditor.btnSaveTextFile.doClick();
			} else {
				currentTextEditor.btnSaveTextFileAs.doClick();
			}
			return !getCurrentTabEditor().loadedTabObj.unsaved;
		} else if (askSave == JOptionPane.NO_OPTION) {
			return true;
		}
		return false;
	}

	public void initGuiFromConfig(boolean refreshGuiStyle) {
		final TabTextEditorObj loadedTabObj = getCurrentTabEditor().loadedTabObj;
		final TextEditorOption txtOptions = currentConfiguration.getTextEditorOpt();


		// TEXT AREA
		headerPanel.checkBoxInlineTabs.setSelected(txtOptions.isInlinedTabs());
		headerPanel.doInlinedCheck();
		headerPanel.chkCutWordsTo.setSelected(txtOptions.isBreaklineActive());
		leftSplitContainer.setBackground(txtOptions.filePanelBackCol);
		leftSplitTreeFiles.setBackground(txtOptions.filePanelBackCol);
		headerPanel.sliderSpokenSpeed.setValue(txtOptions.getVoiceRate());
		
		// UPDATE DETAIL
		loadedTabObj.updateTextAreaPreferences(txtOptions);

		// TREE LOCKED
		if (txtOptions.treeLocked) {
			toggleButtonFileTreeLocked.setIcon(new ImageIcon(IconsPathConfigurator.ICON_PIN_GREEN));
		}else {
			toggleButtonFileTreeLocked.setIcon(new ImageIcon(IconsPathConfigurator.ICON_UNPIN));
		}
		
		// WORKSPACE VIEW ACTIVE ON/OFF
		if (txtOptions.workspaceViewSelected) {
			toggleButtonFileTreeWorkspace.setIcon(new ImageIcon(IconsPathConfigurator.ICON_JOB_ON));
		}else {
			toggleButtonFileTreeWorkspace.setIcon(new ImageIcon(IconsPathConfigurator.ICON_JOB_OFF));
		}
		
//		getCurrentTabEditor().mainTabList.setUI(new CustomTabbedPaneUI_v1(
//				null, null, null, getCurrentTabEditor().mainTabList, TabUtils.TAB_BUTTON_CLOSE_ORDER));
		
		if (refreshGuiStyle) {
			textEditorUtils.updateGuiLafWindows(currentTextEditor);// was causing temporization bug cause of invokelater;
		}
		
		comboStylesScheme.setSelectedItem(ThemesWrapper.getFromListByPath(txtOptions.selectedTheme, themeList));
		textEditorUtils.applySelectedTheme(); // SetSelected does not trigger action itself
		slider_margin.setValue(txtOptions.getMargin());
		textEditorUtils.updateMargin(); // SetValue does not trigger action itself
		currentTextEditor.revalidate();
		currentTextEditor.repaint();
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				sliderFontSize.setValue(txtOptions.getFont().getSize());
				loadedTabObj.textArea.setFont(txtOptions.getFont());
				if (txtOptions.selectedTheme.equals(Themes.NONE)) {
					loadedTabObj.textArea.setBackground(txtOptions.getBackCol());
					RsyntaxUtils.setBackground(loadedTabObj.textArea, txtOptions.backCol);
					loadedTabObj.textArea.setForeground(txtOptions.getForeCol());
					RsyntaxUtils.setForeground(loadedTabObj.textArea, txtOptions.foreCol);
				}
				
				// AUTO LANGUAGE SYNC
				chckbxLockLanguage.setSelected(!txtOptions.defaultAutoSyntaxSync);
				
				// FILE TREE PANEL ON/OFF
				fileTreeShowOrHide(txtOptions);
				
				// RIGHT TREE PANEL ON/OFF
				rightPaneShowOrHide(txtOptions);
			}
		});
	}
	
	public void fileTreeShowOrHide(TextEditorOption txtOptions) {
		btnToggleFileView.setSelected(txtOptions.fileTreeActive);
		if (txtOptions.fileTreeActive) {
			if (getCurrentTabEditor().loadedTabObj.loadedFile == null && !txtOptions.treeLocked) {
				splitPaneMain.getLeftComponent().setVisible(false);
				splitPaneMain.setDividerLocation(1);
			}
			splitPaneMain.getLeftComponent().setVisible(true);
			splitPaneMain.setDividerLocation(currentConfiguration.getTextEditorOpt().dividerFileTreePosX);
			btnToggleFileView.setBackground(GeneralConfig.BUTTON_BCKG_COLOR_TXT_EDITOR_DARKER);
		} else {
			splitPaneMain.getLeftComponent().setVisible(false);
			splitPaneMain.setDividerLocation(1);
			btnToggleFileView.setBackground(GeneralConfig.BUTTON_BCKG_COLOR_TXT_EDITOR);
		}
		
		textEditorUtils.resetDividerColor();
		GUIStyelSettings.applyNimbusSize(0);
	}

	public void rightPaneShowOrHide(TextEditorOption txtOptions) {
		
		// avoid useless operations at start
		if(isPreloading) {return;}
		
		if (txtOptions.clipboardViewActive || txtOptions.mapViewActive) {
			
			// Refresh divider location
			splitPaneMainContainer.getRightComponent().setVisible(true);
			if (textEditorUtils.getTypeOfRightPanel().equals(PanelType.CLIPBOARD_PANEL)) {
				// In case of clipboard use ad-hoc option
				splitPaneMainContainer.setDividerLocation(parentFrame.getWidth() - txtOptions.dividerRightPanePosXClipboard);
			}else {
				splitPaneMainContainer.setDividerLocation(parentFrame.getWidth() - txtOptions.dividerRightPanePosX);
			}
			
			// Update background
			toggleBtnClipboard.setBackground(GeneralConfig.BUTTON_BCKG_COLOR_TXT_EDITOR_DARKER);
			toggleBtnMap.setBackground(GeneralConfig.BUTTON_BCKG_COLOR_TXT_EDITOR_DARKER);
		
		} else {
			splitPaneMainContainer.getRightComponent().setVisible(false);
			splitPaneMainContainer.setDividerLocation(parentFrame.getWidth());
			toggleBtnClipboard.setBackground(GeneralConfig.BUTTON_BCKG_COLOR_TXT_EDITOR);
			toggleBtnMap.setBackground(GeneralConfig.BUTTON_BCKG_COLOR_TXT_EDITOR);
		}
		
		textEditorUtils.resetDividerColor();
		
		currentConfiguration.writeSingleValue(INItializer.TEXT_EDITOR_OPT, "mapViewActive", txtOptions.mapViewActive);
		currentConfiguration.writeSingleValue(INItializer.TEXT_EDITOR_OPT, "clipboardViewActive", txtOptions.clipboardViewActive);
		
		toggleBtnMap.setSelected(txtOptions.mapViewActive);
		toggleBtnClipboard.setSelected(txtOptions.clipboardViewActive);
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				currentTextEditor.rightSplitPane.setBackground(currentConfiguration.getTextEditorOpt().getFilePanelBackCol());
				currentTextEditor.splitPaneMainContainer.setBackground(currentConfiguration.getTextEditorOpt().getFilePanelBackCol());
				currentTextEditor.splitPaneMainContainer.getRightComponent().setBackground(currentConfiguration.getTextEditorOpt().getFilePanelBackCol());
				GUIStyelSettings.applyNimbusSize(0);
				currentTextEditor.rightSplitPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
				currentTextEditor.rightSplitPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			}
		});
	}

	private void loadOpenedFilesAtStart() {
		
		final CustomTabbedEditor currentTabbedEditor = getCurrentTabEditor();
		
		currentTabbedEditor.loadedTabObj.textArea.setCursor(GuiUtils.CURSOR_WAIT);

		boolean loadedAlmostOne = false;
		currentTabbedEditor.alreadyLoading = true;
		if (currentConfiguration.getTextEditorOpt().getOpenedFileList() != null
				&& currentConfiguration.getTextEditorOpt().getOpenedFileList().size() != 0) {
			
			for (String pathToOpen : currentConfiguration.getTextEditorOpt().getOpenedFileList()) {
				File toLoadFile = new File(pathToOpen);
				if (toLoadFile.exists()) {
					currentTabbedEditor.addTab(toLoadFile, !loadedAlmostOne);
					if (!loadedAlmostOne)
						loadedAlmostOne = true;
				}
			}
		}

		// ALSO ARGUMENTS IF THERE ARE
		if (MainGuiGeneralAppStarter.argumentsFiles != null && MainGuiGeneralAppStarter.argumentsFiles.length != 0) {
			logger.debug("ARGUMENTS FILES:" + Arrays.toString(MainGuiGeneralAppStarter.argumentsFiles));
			for (File file : MainGuiGeneralAppStarter.argumentsFiles) {
				currentTabbedEditor.loadFile(file);
				logger.debug(file.getAbsolutePath() + " -> Loaded");
			}
		}

		if (loadedAlmostOne) {
			// first remove first tab if at least one file is loaded
			currentTabbedEditor.removeTab(0);

			// now try to select the old opened one
			String toOpenAtStart = currentConfiguration.getTextEditorOpt().selectedAtStart;
			try {
				int i = 0;
				for (TabTextEditorObj iteratorObj : currentTabbedEditor.loadedList.getList()) {
					if (iteratorObj.loadedFile != null
							&& toOpenAtStart.equals(iteratorObj.loadedFile.getAbsolutePath())) {
						currentTabbedEditor.mainTabList.setSelectedIndex(i);
						break;
					}
					i++;
				}
			} catch (Exception e) {
				logger.warn("Cannot select default file " + toOpenAtStart, e);
			}
		}else {
			// trigger tab style refresh
			currentTabbedEditor.mainTabList.setSelectedIndex(0);
		}
		
		// From now document listener will be active
		currentTabbedEditor.alreadyLoading = false;
		currentTabbedEditor.loadedTabObj.textArea.setCursor(GuiUtils.CURSOR_TEXT);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				// RE-Enable refreshing
				isPreloading = false;
				getCurrentTabEditor().tabRefreshAfterUpdate();
				textEditorUtils.refreshTree(currentTabbedEditor.loadedTabObj.loadedFile, true);
			}
		});
	}

	public void startRefreshFooterThread() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (MainGUI.mainAppRunning) {
					TabTextEditorObj loadedTabObj = getCurrentTabEditor().loadedTabObj;
					
					if (loadedTabObj != null && loadedTabObj.textArea != null) {
						
						int selectionLength = loadedTabObj.textArea.getSelectionEnd() - loadedTabObj.textArea.getSelectionStart();
						int charTotNum = loadedTabObj.textArea.getText().length();
						int textLines = loadedTabObj.textArea.getLineCount();
						int column = loadedTabObj.textArea.getCaretOffsetFromLineStart();
						
						try {
							int line = loadedTabObj.textArea.getLineOfOffset(loadedTabObj.textArea.getCaretPosition());
							footerPanel.dynLblCurrLine.setText(String.valueOf(line));
						} catch (BadLocationException e1) {
							logger.error("Error refreshing current line in footer", e1);
						}
						
						footerPanel.dynLblSelLength.setText(String.valueOf(selectionLength));
						footerPanel.dynLblTotChars.setText(String.valueOf(charTotNum));
						footerPanel.dynLblTotLines.setText(String.valueOf(textLines));
						footerPanel.dynLblColumn.setText(String.valueOf(column));
						footerPanel.dynLabelCharset.setText(System.getProperty("file.encoding"));
						
						File currFile = loadedTabObj.loadedFile;
						if (loadedTabObj.loadedFile != null) {
							String oldCurrFile = footerPanel.lblCurrentFile.getText();
							boolean updateA = !oldCurrFile.equals(currFile.getAbsolutePath());
							boolean updateB = !oldCurrFile.equals(currFile.getName());
							if (footerPanel.rdbtnShowCompletePath.isSelected() && updateA) {
								try {
									footerPanel.lblCurrentFile.setText(loadedTabObj.loadedFile.getCanonicalPath());
								} catch (IOException e) {
									logger.error(e);								}
							} else if (!footerPanel.rdbtnShowCompletePath.isSelected() && updateB) {
								footerPanel.lblCurrentFile.setText(loadedTabObj.loadedFile.getName());
							}
						}
					}
					
					try {
						Thread.sleep(GeneralConfig.TEXT_EDITOR_FOOTER_REFRESH_PERIOD_MS);
					} catch (InterruptedException e) {
					}
				}
			}
		}).start();
	}
	
	public CustomTabbedEditor getCurrentTabEditor() {
		return currentTextEditor.tabbedEditorsList.get(currentTextEditor.currentTabbedEditorSelected);
	}
}

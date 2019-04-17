package gUI.panels;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollBar;

import java.awt.Insets;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import net.miginfocom.swing.MigLayout;
import resources.IconsPathConfigurator;
import uTILS.local.ImageWorker;

import javax.swing.SwingConstants;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.drjekyll.fontchooser.FontDialog;

import amfileutils.FileStuff;
import crypt.aes.AesCipher;
import dialogutils.ColorDialogUtils;
import frameutils.GuiUtils;
import frameutils.SearchFrame;
import iNIT.INItializer;
import iNIT.om.TextEditorOption;
import local.HandleString;
import local.Text2Speech;

import java.awt.SystemColor;

import javax.swing.JTextArea;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.awt.event.ActionEvent;
import javax.swing.JScrollPane;
import javax.swing.ImageIcon;
import javax.swing.JSlider;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.undo.UndoManager;
import javax.swing.event.ChangeEvent;
import javax.swing.UIManager;
import javax.swing.JCheckBox;
import javax.swing.JSeparator;
import javax.swing.border.LineBorder;
import javax.swing.JComboBox;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;

import gUI.mainGUI;
import gUI.om.SupportedFileTextEditor;
import gUI.utils.filedialog.TemplatesChoosePanel;
import gUI.utils.shortcutshandler.TextEditorListener;
import javax.swing.JPasswordField;
import java.awt.Cursor;

public class TextEditor extends JPanel {

	// TextFileWriter logger creation
	static Logger logger = Logger.getLogger(TextEditor.class);
	
	public static final char DEFAULT_PSW_CHAR = '\u25CF';
	
	//  RESOURCE FILES //
	private final String LOREM_IPSUM = "Resources_Scrypt/Templates/LoremIpsum.txt";
	
	// TOOLTIPS //
	private static final String tooltipSaveTextFile = "[Ctrl+S] Save current file";
	private static final String tooltipSaveTextFileAs = "[Ctrl+Shift+S] Choose name and folder, save current file";
	private static final String tooltipLoadTextFile = "[Ctrl+O] Load source File";
	private static final String tooltipSpeak = "[Ctrl+spacebar] Speak the text written in the text area (only english). Use the upper slider to change speed before to play, press again to disable";
	private static final String tooltipDecrypt = "[Ctrl+shift+D] Decrypt displayed text with given key";
	private static final String tooltipCrypt = "[Ctrl+shift+C] Crypt displayed text with given key";
	private static final String tooltipBackgroundColor = "Change background color (won't be saved in .txt)";
	private static final String tooltipForegroundTxt = "Change text color (won't be saved in .txt)";
	private static final String tooltipDarkSwitch = "[Ctrl+B] Switch upon dark and light text combination";
	private static final String tooltipUpperPostDots = "Makes text uppercase after dots a significat line brokers";
	private static final String tooltipTt = "[Ctrl+Shift+UP] Makes selected text uppercase";
	private static final String tooltipTtTt = "[Ctrl+shift+dot] Makes al initial characters uppercase";
	private static final String tooltipLoremIpsum = "Insert classic test text \"Lorem Ipsum\"";
	private static final String tooltipFindText = "[Ctrl+F] Search text into current editor";
	private static final String tooltipFontChooser = "Change font and text-properties";
	private static final String tooltipComboFileFormat = "Choose file extension";
	private static final String tooltipTemplates = "Choose between many different patterns of code to insert in you text";
	private static final String tooltipCutWordsTo = "When selected, the text will be cropped to adapt the size of the text area";
	private static final String tooltipFitMaintainWords = "When selected, the text will be cropped to adapt the size of the text area (without breaking words into fragments)";
	private static final String tooltipDefaultOpt = "Reset default text-graphics values";
	private static final String tooltipUndo = "[Ctrl+Z] Undo Last";
	private static final String tooltipClearText = "[Ctrl+canc] Erase the content of the current opened file displayed (without save)";
	private static final String tooltipRedo = "[Alt+Z] Redo Last";
	private static final String tooltipBoldText = "Set bold style to all text";
	private static final String tooltipItalic = "Set italics style to all text"; 
	
	// GUI Objects //
	public JCheckBox chkFitmantainwords;
	public JCheckBox chkCutWordsTo;
	
	private JLabel lblSIZE;
	private JLabel lblNewLabel;
	private JLabel lblTextEditor;
	private JLabel lblMargin;
	private JLabel lblFontSize;
	private JLabel lblCurrentFileLBL;
	private JLabel lblCurrentFile;
	private JLabel lblVoiceSpeed;
	public JLabel lblAdvice;
	
	public JButton btntTextFormat;
	public JButton btnLoadTextFile;
	public JButton btnSaveTextFileAs;
	public JButton btnSaveTextFile;
	public JButton btnSpeak;
	public JButton btnDecrypt;
	public JButton btnCrypt;
	public JButton btnDark;
	public JButton btnUpperPostDots;
	public JButton btnTt;
	public JButton btnTtTt;
	public JButton btnBackgroundCol; 
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
	private JButton button_1;
	
	public JComboBox comboFileFormat;
	
	public JSlider sliderSpokenSpeed;
	public JSlider sliderFontSize;
	public JSlider slider_margin;
	
	public JTextArea textArea_1;
	private JPasswordField txtAesKey; // field containing fast crypt text user key, sinchronized from ini file as default
	
	private JScrollPane mainScrollPane;
	private JPanel mainPanel;
	
	public JFrame parentFrame; // the frame that instantiate this object

	// GENERAL FIELDS //
	// values used in editor functions Tt Tt and .Tt
	private boolean uppercaseDots;
	private boolean uppercase;
	private String backupText;
	
	// value used for margins of text
	private int textMargin;
	
	// flag to signal if function is already running
	public boolean alreadySaving = false;
	public boolean alreadyLoading = false;
	
	public String CURRENT_EXT_OUT = ".txt";
	public Color LBL_ADVICE_COLOR;
	
	public UndoManager manager;
	private INItializer currentConfiguration;
	private TextEditor currentTextEditor; // current object
	private Text2Speech objSpeaker; // field containing the current object used to speak text
	private JCheckBox chckbxAesShowKey;
	private SearchFrame searchBox;
	
	private File loadedFile;
	
//////////////// CONSTRUCTOR /////////////////////
	
	public TextEditor(JFrame parent, INItializer currentConf) {

		setBorder(BorderFactory.createEmptyBorder());
		setBackground(Color.BLACK);
		setLayout(new MigLayout("", "[1624px,grow]", "[711px,grow]"));
		
		logger.info(this.getClass().getName() + " Starting...");
		
		// initialize values used in gui //
		currentConfiguration = currentConf;		
		parentFrame = parent;		
		currentTextEditor=this;
		LBL_ADVICE_COLOR = new Color(124, 252, 0);
		textMargin = 14;
		uppercase = false;
		uppercaseDots=true;
		
		mainPanel = new JPanel();
		mainPanel.setBackground(Color.DARK_GRAY);
		mainPanel.setLayout(new MigLayout("", "[40][40][40px][50px][60px][][][][:40:40][:40:40][:40:40][:40:40][:40px:40px][:50px:50px][:50px:50px][20][20][30][40.00][40,fill][:40px:40px][70][35px:35][35,grow][35,grow][70]", "[:20:50px,grow][][20][][25][25][52.33%,grow]"));
		mainScrollPane = new JScrollPane();
		mainScrollPane.setBorder(BorderFactory.createEmptyBorder());
		mainScrollPane.setBackground(Color.DARK_GRAY);
		mainScrollPane.setViewportView(mainPanel);
		add(mainScrollPane, "cell 0 0,grow");
		
		lblCurrentFileLBL = new JLabel("   Current file:");
		lblCurrentFileLBL.setForeground(SystemColor.controlHighlight);
		lblCurrentFileLBL.setFont(new Font("Tahoma", Font.BOLD, 18));
		mainPanel.add(lblCurrentFileLBL, "cell 0 2 4 1,alignx center");
		
		lblCurrentFile = new JLabel("\\*.txt");
		lblCurrentFile.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblCurrentFile.setForeground(SystemColor.activeCaption);
		mainPanel.add(lblCurrentFile, "cell 4 2 6 1,alignx left");
		
		lblVoiceSpeed = new JLabel("Voice rate");
		lblVoiceSpeed.setForeground(new Color(211, 211, 211));
		mainPanel.add(lblVoiceSpeed, "cell 10 2 2 1,alignx center");
		
		sliderSpokenSpeed = new JSlider();
		sliderSpokenSpeed.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
		sliderSpokenSpeed.setValue(currentConfiguration.getTextEditorOpt().getVoiceRate());
		sliderSpokenSpeed.setMaximum(400);
		mainPanel.add(sliderSpokenSpeed, "cell 13 2 2 1,alignx center");
		
		lblAdvice = new JLabel("");
		lblAdvice.setBackground(SystemColor.info);
		lblAdvice.setFont(new Font("Tahoma", Font.BOLD, 13));
		lblAdvice.setForeground(new Color(176, 224, 230));
		lblAdvice.setHorizontalAlignment(SwingConstants.CENTER);
		mainPanel.add(lblAdvice, "cell 23 2 3 1,alignx center,aligny center");
		lblAdvice.setText(" [Current output: .txt]");
		
		btnSaveTextFile = new JButton("");
		btnSaveTextFile.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnSaveTextFile.setToolTipText(tooltipSaveTextFile);
		btnSaveTextFile.setForeground(Color.BLACK);
		btnSaveTextFile.setIcon(new ImageIcon(IconsPathConfigurator.ICON_FILE_SAVE));
		btnSaveTextFile.setBackground(SystemColor.inactiveCaption);
		mainPanel.add(btnSaveTextFile, "cell 1 4,grow");
		
		btnSaveTextFileAs = new JButton("");
		btnSaveTextFileAs.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnSaveTextFileAs.setToolTipText(tooltipSaveTextFileAs);
		btnSaveTextFileAs.setForeground(SystemColor.menuText);
		btnSaveTextFileAs.setIcon(new ImageIcon(IconsPathConfigurator.ICON_FILE_SAVE_AS));
		btnSaveTextFileAs.setBackground(SystemColor.inactiveCaption);
		mainPanel.add(btnSaveTextFileAs, "cell 2 4,grow");
		
		btnFindText = new JButton("");
		btnFindText.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnFindText.setToolTipText(tooltipFindText);
		btnFindText.setIcon(new ImageIcon(IconsPathConfigurator.ICON_SEARCH));
		btnFindText.setBackground(SystemColor.activeCaption);
		btnFindText.setFont(new Font("SansSerif", Font.BOLD, 12));
		btnFindText.setBackground(new Color(176, 196, 222));
		mainPanel.add(btnFindText, "cell 12 4,growx");
		
		btnLoremIpsum = new JButton("Lorem Ipsum");
		btnLoremIpsum.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnLoremIpsum.setToolTipText(tooltipLoremIpsum);
		btnLoremIpsum.setFont(new Font("SansSerif", Font.BOLD, 12));
		btnLoremIpsum.setBackground(new Color(176, 196, 222));
		mainPanel.add(btnLoremIpsum, "cell 18 4 2 1,growx");
		
		lblMargin = new JLabel("Margin:");
		lblMargin.setHorizontalAlignment(SwingConstants.CENTER);
		lblMargin.setForeground(SystemColor.menu);
		lblMargin.setBackground(UIManager.getColor("CheckBox.darkShadow"));
		mainPanel.add(lblMargin, "cell 21 4,alignx center");
		
		button_1 = new JButton("");
		button_1.setToolTipText("[Ctrl+spacebar] Speak the text written in the text area (only english). Use the upper slider to change speed before to play, press again to disable");
		button_1.setFont(new Font("SansSerif", Font.BOLD, 12));
		button_1.setBackground(new Color(176, 196, 222));
		mainPanel.add(button_1, "cell 12 5,growx");
		
		btnCrypt = new JButton("");
		btnCrypt.setHorizontalAlignment(SwingConstants.RIGHT);
		btnCrypt.setHorizontalTextPosition(SwingConstants.RIGHT);
		btnCrypt.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnCrypt.setIcon(new ImageIcon(IconsPathConfigurator.ICON_CRYPT));
		btnCrypt.setToolTipText(tooltipCrypt);
		btnCrypt.setFont(new Font("Tahoma", Font.BOLD, 12));
		btnCrypt.setForeground(SystemColor.inactiveCaption);
		mainPanel.add(btnCrypt, "cell 13 5,alignx right");
		
		btnDecrypt = new JButton("");
		btnDecrypt.setHorizontalTextPosition(SwingConstants.CENTER);
		btnDecrypt.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnDecrypt.setIcon(new ImageIcon(IconsPathConfigurator.ICON_DECRYPT));
		btnDecrypt.setToolTipText(tooltipDecrypt);
		btnDecrypt.setForeground(SystemColor.inactiveCaption);
		btnDecrypt.setFont(new Font("Tahoma", Font.BOLD, 12));
		mainPanel.add(btnDecrypt, "cell 14 5,alignx center");
		
		chckbxAesShowKey = new JCheckBox("Show key");
		chckbxAesShowKey.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		chckbxAesShowKey.setForeground(Color.WHITE);
		chckbxAesShowKey.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		mainPanel.add(chckbxAesShowKey, "cell 15 5 3 1,alignx center");
		
		btnTemplates = new JButton("Templates");
		btnTemplates.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnTemplates.setToolTipText(tooltipTemplates);
		btnTemplates.setFont(new Font("SansSerif", Font.BOLD, 12));
		btnTemplates.setBackground(new Color(176, 196, 222));
		mainPanel.add(btnTemplates, "cell 18 5 2 1,growx");
		
		lblFontSize = new JLabel("Font size: ");
		lblFontSize.setHorizontalAlignment(SwingConstants.CENTER);
		lblFontSize.setForeground(SystemColor.menu);
		mainPanel.add(lblFontSize, "cell 21 5,alignx center");
		
		JScrollPane textScrollPaneArea = new JScrollPane();
		mainPanel.add(textScrollPaneArea, "cell 0 6 26 1,grow");
		textScrollPaneArea.setFont(new Font("DejaVu Sans", Font.PLAIN, 18));
		
		textArea_1 = new JTextArea();
		textArea_1.setFont(currentConfiguration.getTextEditorOpt().getFont());
		textScrollPaneArea.setViewportView(textArea_1);
		manager = new UndoManager();
		manager.setLimit(1000);
		textArea_1.getDocument().addUndoableEditListener(manager);
		
		btnBackgroundCol = new JButton("");
		btnBackgroundCol.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnBackgroundCol.setToolTipText(tooltipBackgroundColor);
		btnBackgroundCol.setBackground(new Color(176, 196, 222));
		btnBackgroundCol.setIcon(new ImageIcon(IconsPathConfigurator.ICON_TEXT_BG_COLOR));
		mainPanel.add(btnBackgroundCol, "cell 10 4,alignx center");
		
		sliderFontSize = new JSlider(4,100);
		sliderFontSize.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
		sliderFontSize.setForeground(SystemColor.activeCaptionBorder);
		sliderFontSize.setBackground(UIManager.getColor("Button.shadow"));
		sliderFontSize.setValue(textArea_1.getFont().getSize());
		mainPanel.add(sliderFontSize, "cell 23 5 3 1,growx");
		
		btnFontChooser = new JButton("");
		btnFontChooser.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnFontChooser.setToolTipText(tooltipFontChooser);

		lblSIZE = new JLabel("");
		lblSIZE.setForeground(SystemColor.window);
		mainPanel.add(lblSIZE, "cell 22 5,alignx center");
		lblSIZE.setText(String.valueOf(textArea_1.getFont().getSize())+" Px");
		
		btnSpeak = new JButton("");
		btnSpeak.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnSpeak.setToolTipText(tooltipSpeak);
		btnSpeak.setIcon(new ImageIcon(IconsPathConfigurator.ICON_SPEAKER));
		btnSpeak.setFont(new Font("SansSerif", Font.BOLD, 12));
		btnSpeak.setBackground(new Color(176, 196, 222));
		mainPanel.add(btnSpeak, "cell 11 4,alignx center");

		btnFontChooser.setBackground(new Color(176, 196, 222));
		btnFontChooser.setIcon(new ImageIcon(IconsPathConfigurator.ICON_TEXT_FONT));
		mainPanel.add(btnFontChooser, "cell 11 5,alignx center"); 
		
		lblNewLabel = new JLabel("\u2193  (AES KEY)  \u2193");
		lblNewLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
		lblNewLabel.setForeground(new Color(211, 211, 211));
		mainPanel.add(lblNewLabel, "cell 15 2 3 1,alignx center");
		
		JSeparator separator_1 = new JSeparator();
		mainPanel.add(separator_1, "cell 0 3 26 1,growx");
		
		comboFileFormat = new JComboBox();
		comboFileFormat.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		comboFileFormat.setToolTipText(tooltipComboFileFormat);
		comboFileFormat.setBackground(SystemColor.textHighlight);
		comboFileFormat.setModel(new DefaultComboBoxModel(SupportedFileTextEditor.values()));
		mainPanel.add(comboFileFormat, "cell 3 4,growx");
		
		txtAesKey = new JPasswordField();
		txtAesKey.setHorizontalAlignment(SwingConstants.CENTER);
		txtAesKey.setText("");
		mainPanel.add(txtAesKey, "cell 13 4 5 1,growx");
		txtAesKey.setColumns(10);
		txtAesKey.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		txtAesKey.setEchoChar(DEFAULT_PSW_CHAR);
		AesCipher.check(txtAesKey);
				
		JPanel panel = new JPanel();
		panel.setForeground(SystemColor.menu);
		panel.setBorder(new LineBorder(new Color(0, 0, 0), 4));
		panel.setBackground(new Color(100, 149, 237));
		mainPanel.add(panel, "cell 0 0 26 1,growx,aligny center");
		panel.setLayout(new MigLayout("", "[100px,grow 10000,left][][][][][][][][][][][][][][][][][][][][][][grow][grow][]", "[]"));
		
		lblTextEditor = new JLabel("Text Editor");
		panel.add(lblTextEditor, "cell 0 0,alignx left");
		lblTextEditor.setHorizontalAlignment(SwingConstants.CENTER);
		lblTextEditor.setForeground(Color.BLACK);
		lblTextEditor.setFont(new Font("Lithos Pro Regular", Font.BOLD, 19));
		lblTextEditor.setBackground(SystemColor.activeCaptionBorder);
		
		chkCutWordsTo = new JCheckBox("Adapt Text Width");
		chkCutWordsTo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		panel.add(chkCutWordsTo, "cell 23 0");
		chkCutWordsTo.setToolTipText(tooltipCutWordsTo);
		chkCutWordsTo.setSelected(true);
		chkCutWordsTo.setForeground(SystemColor.control);
		chkCutWordsTo.setBackground(Color.DARK_GRAY);
		
		chkFitmantainwords = new JCheckBox("and...Mantain Words!");
		chkFitmantainwords.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		panel.add(chkFitmantainwords, "cell 24 0");
		chkFitmantainwords.setToolTipText(tooltipFitMaintainWords);
		chkFitmantainwords.setForeground(SystemColor.control);
		chkFitmantainwords.setBackground(Color.DARK_GRAY);
		chkFitmantainwords.setEnabled(false);
		
		slider_margin = new JSlider(0,100,0);
		slider_margin.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
		slider_margin.setBackground(UIManager.getColor("Button.shadow"));
		mainPanel.add(slider_margin, "cell 22 4 4 1,growx");
		
		btntTextFormat = new JButton("Format");
		btntTextFormat.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btntTextFormat.setFont(new Font("Tahoma", Font.BOLD, 13));
		btntTextFormat.setBackground(new Color(176, 196, 222));
		mainPanel.add(btntTextFormat, "cell 4 4,growx");
		
		JSeparator separator = new JSeparator();
		mainPanel.add(separator, "cell 0 1 26 1,growx");
		
		btnLoadTextFile = new JButton("");
		btnLoadTextFile.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnLoadTextFile.setToolTipText(tooltipLoadTextFile);
		btnLoadTextFile.setForeground(SystemColor.menuText);
		btnLoadTextFile.setBackground(SystemColor.inactiveCaption);
		btnLoadTextFile.setIcon(new ImageIcon(IconsPathConfigurator.ICON_FILE_LOAD));
		mainPanel.add(btnLoadTextFile, "cell 0 4,grow");
		
		btnDefaultOpt = new JButton("Default Opt");
		btnDefaultOpt.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnDefaultOpt.setFont(new Font("Tahoma", Font.BOLD, 13));
		btnDefaultOpt.setToolTipText(tooltipDefaultOpt);
		btnDefaultOpt.setForeground(SystemColor.menuText);
		btnDefaultOpt.setBackground(new Color(176, 196, 222));
		mainPanel.add(btnDefaultOpt, "cell 5 4 2 1,growx");
		
		btnUndo = new JButton("");
		btnUndo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnUndo.setToolTipText(tooltipUndo);
		btnUndo.setBackground(new Color(176, 196, 222));
		mainPanel.add(btnUndo, "cell 8 4,alignx center");
		btnUndo.setIcon(new ImageIcon(IconsPathConfigurator.ICON_UNDO));
		
		btnRedo = new JButton("");
		btnRedo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnRedo.setToolTipText(tooltipRedo);
		btnRedo.setBackground(new Color(176, 196, 222));
		mainPanel.add(btnRedo, "cell 9 4,alignx center");
		btnRedo.setIcon(new ImageIcon(IconsPathConfigurator.ICON_REDO));
		
		btnClear = new JButton("");
		btnClear.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnClear.setToolTipText(tooltipClearText);
		btnClear.setBackground(new Color(176, 196, 222));
		btnClear.setIcon(new ImageIcon(IconsPathConfigurator.ICON_DELETE));
		btnClear.setFont(new Font("Wide Latin", Font.BOLD, 14));
		mainPanel.add(btnClear, "cell 7 4,growx,aligny center");
		
		btnUpperPostDots = new JButton(".Tt");
		btnUpperPostDots.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnUpperPostDots.setToolTipText(tooltipUpperPostDots);
		btnUpperPostDots.setFont(new Font("SansSerif", Font.BOLD, 12));
		btnUpperPostDots.setBackground(new Color(176, 196, 222));
		mainPanel.add(btnUpperPostDots, "cell 7 5,growx");
		textArea_1.setMargin(new Insets(textMargin/2, textMargin, textMargin/2, textMargin));
		
		btnBold = new JButton("");
		btnBold.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnBold.setToolTipText(tooltipBoldText);
		btnBold.setBackground(new Color(176, 196, 222));
		btnBold.setIcon(new ImageIcon(IconsPathConfigurator.ICON_TEXT_BOLD));
		mainPanel.add(btnBold, "cell 9 5,alignx center");
		
		btnDark = new JButton("Dark/Light");
		btnDark.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnDark.setToolTipText(tooltipDarkSwitch);
		btnDark.setFont(new Font("Tahoma", Font.BOLD, 13));
		btnDark.setForeground(SystemColor.menuText);
		btnDark.setBackground(SystemColor.controlShadow);
		btnDark.setIcon(null);
		btnDark.setPreferredSize(btnBold.size());
		mainPanel.add(btnDark, "cell 0 5 5 1,growx,aligny center");
		btnDark.setForeground(Color.LIGHT_GRAY);
		btnDark.setBackground(Color.DARK_GRAY);
		
		btnTt = new JButton("TT");
		btnTt.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnTt.setFont(new Font("SansSerif", Font.BOLD, 12));
		btnTt.setBackground(new Color(176, 196, 222));
		btnTt.setToolTipText(tooltipTt);
		mainPanel.add(btnTt, "cell 5 5,growx");
		
		btnTtTt = new JButton("Tt Tt");
		btnTtTt.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnTtTt.setFont(new Font("SansSerif", Font.BOLD, 12));
		btnTtTt.setBackground(new Color(176, 196, 222));
		btnTtTt.setToolTipText(tooltipTtTt);
		mainPanel.add(btnTtTt, "cell 6 5,growx");
		
		if(chkCutWordsTo.isSelected()) {
			textArea_1.setLineWrap(true);
			chkFitmantainwords.setEnabled(true);
		}
		
		btnItaliC = new JButton("");
		btnItaliC.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnItaliC.setToolTipText(tooltipItalic);
		btnItaliC.setBackground(new Color(176, 196, 222));
		btnItaliC.setIcon(new ImageIcon(IconsPathConfigurator.ICON_TEXT_ITALIC));
		mainPanel.add(btnItaliC, "cell 8 5,alignx center");
		
		btnForegroundCol = new JButton("");
		btnForegroundCol.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnForegroundCol.setToolTipText(tooltipForegroundTxt);
		btnForegroundCol.setBackground(new Color(176, 196, 222));
		btnForegroundCol.setIcon(new ImageIcon(IconsPathConfigurator.ICON_TEXT_COLOR));
		mainPanel.add(btnForegroundCol, "cell 10 5,alignx center");
		
		addHandlers();
		
		// add custom keyboard listener
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(getKeyEventHandler());
		
		startAesKeyCheckThread();
		
		initGuiFromConfig();

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
						logger.error(e.getMessage());
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
		final Font DEFAULT = textArea_1.getFont();
		final Color DEFAULT_BCKG_COL = textArea_1.getBackground(); 
		final Color DEFAULT_FORE_COL = textArea_1.getForeground();
		
		// SEARCH TEXT
		btnFindText.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(searchBox == null || !searchBox.isActive()) {
					searchBox = new SearchFrame(false, true, textArea_1);
					searchBox.setVisible(true);
					GuiUtils.centerComponent(searchBox, 450, 110);
				}else {
					searchBox.toFront();
					searchBox.requestFocus();
				}
				
			}
		});
		
		// SHOW/HIDE PASSORD DISPLAYED
		chckbxAesShowKey.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (chckbxAesShowKey.isSelected()) {
					txtAesKey.setEchoChar((char) 0);
				}else {
					txtAesKey.setEchoChar(DEFAULT_PSW_CHAR);
				}
			}
		});
		
		// CRYPT TEXT
		btnCrypt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// this function is used to change color and tooltip text according to key lenght
				AesCipher.check(txtAesKey);
				
				if (txtAesKey.getPassword().length != 0) {
					try {
						AesCipher crypter = new AesCipher(txtAesKey.getPassword());
						textArea_1.setText(crypter.encrypt(textArea_1.getText()));
					} catch (Exception e) {
						JOptionPane.showMessageDialog(null, "Wrong key, retry! :(", "Error", JOptionPane.ERROR_MESSAGE);
						logger.error("Exception happened!", e);
					} 
				}else {
					JOptionPane.showMessageDialog(currentTextEditor, "Please enter a password, empty key text!", "Empty password", JOptionPane.WARNING_MESSAGE);
				}
			}
		});
		
		// DECRIPT TEXT
		btnDecrypt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {	
				// this function is used to update bg color of textfield according to given key's lenght
				AesCipher.check(txtAesKey);
				
				if (txtAesKey.getPassword().length != 0) {
					try {
						AesCipher decrypter = new AesCipher(txtAesKey.getPassword());
						textArea_1.setText(decrypter.decrypt(textArea_1.getText()));
					} catch (Exception e) {
						JOptionPane.showMessageDialog(null, "Wrong key, retry! :(", "Error", JOptionPane.ERROR_MESSAGE);
						logger.error("Exception happened!", e);
					} 
				}else {
					JOptionPane.showMessageDialog(currentTextEditor, "Please enter a password, empty key text!", "Empty password", JOptionPane.WARNING_MESSAGE);
				}
				
			}
		});
		
		// SET DEFAULT TEXT AREA COLORS
		btnDefaultOpt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textArea_1.setFont(DEFAULT);
				textArea_1.setBackground(DEFAULT_BCKG_COL);
				textArea_1.setForeground(DEFAULT_FORE_COL);
				slider_margin.setValue(10);
				sliderFontSize.setValue(textArea_1.getFont().getSize());
				lblSIZE.setText(String.valueOf(textArea_1.getFont().getSize())+" Px");

				updateTxtConfig(DEFAULT, DEFAULT_BCKG_COL, DEFAULT_FORE_COL, 10);
			}
		});
		
		//BACKGROUND TEXT COLOR
		btnBackgroundCol.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				btnBackgroundCol.setEnabled(false);
				Color esit = new ColorDialogUtils().askForColor(textArea_1, false);
				if (esit != null) {
					currentConfiguration.getTextEditorOpt().setBackCol(esit);
				}
				btnBackgroundCol.setEnabled(true);
			}
		});
		

		//FOREGROUND TEXT COLOR
		btnForegroundCol.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {      
			    
				btnForegroundCol.setEnabled(false);
				Color esit = new ColorDialogUtils().askForColor(textArea_1, true);
				if (esit != null) {
					currentConfiguration.getTextEditorOpt().setForeCol(esit);
				}
				btnForegroundCol.setEnabled(true);
			}
		});
		
		// FONT CHOOSER PANEL
		btnFontChooser.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				btnFontChooser.setEnabled(false);

				FontDialog.setDefaultLookAndFeelDecorated(false);
				FontDialog.showDialog(textArea_1);
				sliderFontSize.setValue(textArea_1.getFont().getSize());
				lblSIZE.setText(String.valueOf(textArea_1.getFont().getSize())+" Px");	
				currentConfiguration.getTextEditorOpt().setFont(textArea_1.getFont());

				btnFontChooser.setEnabled(true);
				
			}
		});
				
		// SWITCH BETWEEN DARK/LIGHT COLORS FOR TEXT AREA
		btnDark.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(textArea_1.getForeground()==Color.BLACK || textArea_1.getBackground()==DEFAULT_BCKG_COL || textArea_1.getBackground()==Color.WHITE) {
					textArea_1.setForeground(SystemColor.LIGHT_GRAY);
					textArea_1.setBackground(Color.BLACK);
					btnDark.setForeground(Color.DARK_GRAY);
					btnDark.setBackground(SystemColor.controlHighlight);	
				 }else {
					textArea_1.setForeground(Color.BLACK);
					textArea_1.setBackground(SystemColor.controlHighlight);	
					btnDark.setForeground(SystemColor.LIGHT_GRAY);
					btnDark.setBackground(Color.DARK_GRAY);
				 }
				currentConfiguration.getTextEditorOpt().setBackCol(textArea_1.getBackground());
				currentConfiguration.getTextEditorOpt().setForeCol(textArea_1.getForeground());				
			}
		});
		
		// INSERT LOREM IPSUM
		btnLoremIpsum.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
							
				FileStuff.appendFileTxtArea(new File(LOREM_IPSUM), textArea_1);
			}
		});
		
		// TEXT AREA FORMATTER
		btntTextFormat.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String source = new String(textArea_1.getText());
				String old = new String(source);
				// XML format handler (if xml formatter works then is a xml recognized file)
				if(CURRENT_EXT_OUT.equals(".xml")&&!HandleString.xmlPrettyFormat(source,4).equals("ERROR :(")) {
					source=HandleString.xmlPrettyFormat(source, 4);
					if(source.equals(old)) {
						// already formatted
						textArea_1.setText(HandleString.removeLines(source));
					}else{
						// not yet formatted
						textArea_1.setText(source);
					}
				}
				//if error :(
				else {
					if(CURRENT_EXT_OUT.equals(".xml")) {
						JOptionPane.showMessageDialog(parentFrame, "Error reading xml format, maybe it's not a valid xml String?", "XML format Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});
		
		// SLIDER FONT CHANGED 
		sliderFontSize.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Font font=textArea_1.getFont();
				font = new Font(font.getName(), font.getStyle(), sliderFontSize.getValue() );
				textArea_1.setFont(font);
				lblSIZE.setText(String.valueOf(textArea_1.getFont().getSize())+" Px");			
				currentConfiguration.getTextEditorOpt().setFont(textArea_1.getFont());
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
							objSpeaker.dospeak(textArea_1.getText(),"kevin16", (sliderSpokenSpeed.getValue()/100.0f)); 
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
				if(manager.canUndo()) {
					manager.undo();
				}
			}
		});
		
		// REDO LAST OPERATION ON TEXT
		btnRedo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				if(manager.canRedo()) {
					manager.redo();
				}
				
			}
		});
		
		// CLEAR TEXT AREA
		btnClear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				if(JOptionPane.showConfirmDialog(parentFrame, "Clear Text?")==JOptionPane.OK_OPTION) {
					textArea_1.setText("");
				}
			}
		});
		
		// MAKE UPPER CASE AFTER LINE ENDERS
		btnUpperPostDots.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (uppercaseDots) {
					String result = HandleString.upperAfterDots(textArea_1.getText(), '.');
					textArea_1.setText(result);
					uppercaseDots=false;
				}else {
					String result = HandleString.lowerAfterDots(textArea_1.getText(), '.');
					textArea_1.setText(result);
					uppercaseDots=true;
				}
			}
		});
		
		// FILE FORMAT SELECTOR
		comboFileFormat.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
					SupportedFileTextEditor selected = (SupportedFileTextEditor) comboFileFormat.getSelectedItem();
					CURRENT_EXT_OUT = selected.nomeStringa;
					infoAdvice("[Current output: "+CURRENT_EXT_OUT+"]");
					btnSaveTextFileAs.setToolTipText("Choose name and folder, save "+CURRENT_EXT_OUT+" file!");
				}
			}
		);
		
		// OPEN TEMPLATES PANEL
		btnTemplates.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!TemplatesChoosePanel.active) {
					JFrame panel = new TemplatesChoosePanel(textArea_1, comboFileFormat, btnTemplates);
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
					textArea_1.setWrapStyleWord(true);
				}else {
					textArea_1.setWrapStyleWord(false);
				}
			}
		});		
		
		// ADAPT TO WIDTH
		chkCutWordsTo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(chkCutWordsTo.isSelected()) {
					textArea_1.setLineWrap(true);
					chkFitmantainwords.setEnabled(true);
				}else {
					textArea_1.setLineWrap(false);
					textArea_1.setWrapStyleWord(false);
					chkFitmantainwords.setEnabled(false);
				}
			}
		});
		
		// MARGIN TEXT AREA SELECTOR
		slider_margin.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
					textMargin=slider_margin.getValue();
					textArea_1.setMargin(new Insets(textMargin/2, textMargin, textMargin/2, textMargin));
					textArea_1.revalidate();
					textArea_1.repaint();
					currentConfiguration.getTextEditorOpt().setMargin(textMargin);
			}
		});
		
		// SET TEXT STYLE TO ITALICS
		btnItaliC.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Font txtFont = textArea_1.getFont();
				 if(!txtFont.isItalic()) {
					textArea_1.setFont(txtFont.deriveFont(Font.ITALIC, txtFont.getSize()));
				 }else {
					textArea_1.setFont(txtFont.deriveFont(Font.PLAIN, txtFont.getSize()));
				 }
				 currentConfiguration.getTextEditorOpt().setFont(txtFont);
			}
		});
		
		// MAKE TEXT BOLD
		btnBold.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Font txtFont = textArea_1.getFont();
				 if(!txtFont.isBold()) {
					textArea_1.setFont(txtFont.deriveFont(Font.BOLD, txtFont.getSize()));
				 }else {
					textArea_1.setFont(txtFont.deriveFont(Font.PLAIN, txtFont.getSize()));
				 }
				
			}
		});
		
		// MAKE UPPERCASE AFTER LINE ENDS
		btnTt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String text = textArea_1.getText();
				String textBackup = new String(text);
				String selected = textArea_1.getSelectedText();
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
					strtN=textArea_1.getSelectionStart();
					endN=textArea_1.getSelectionEnd();
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
				textArea_1.setText(selected);
				if(!none) {
					textArea_1.requestFocus();
					textArea_1.setSelectionStart(strtN);
					textArea_1.setSelectionEnd(endN);
				}
				logger.debug("TT function start selection: "+strtN);
				logger.debug("TT function start selection: "+endN);
			}
		});
		
		// MAKE EVERY WORD STARTING WITH UPPERCASE LETTER
		btnTtTt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				String text = textArea_1.getText();
				String textBackup = new String(text);
				String selected = textArea_1.getSelectedText();
				String start="";
				String end = "";
				int strtN=0;
				int endN=text.length();
				if(selected==null) {
					//select ALL if no selection
					selected = new String(text);
				}else {
					strtN=textArea_1.getSelectionStart();
					endN=textArea_1.getSelectionEnd();
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
				textArea_1.setText(selected);
				textArea_1.requestFocus();
				textArea_1.setSelectionStart(strtN);
				textArea_1.setSelectionEnd(endN);
				logger.debug("Tt Tt function start selection: "+strtN);
				logger.debug("Tt Tt function start selection: "+endN);
				
			}
		});
		
		// SAVE TEXT FILE
		btnSaveTextFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean done = false;
				if(loadedFile == null) {
					btnSaveTextFileAs.doClick();
					return;
				}else {
					try {
						done = FileStuff.writeStringToFile(textArea_1.getText(), loadedFile, true);
					} catch (IOException e1) {
						JOptionPane.showMessageDialog(currentTextEditor, "error Saving the selected file :(");
						logger.error("Exception happened!", e1);
					}
					if(done) {
						successAdvice("[File saved]");
						lblCurrentFile.setText(loadedFile.getName() + "[saved]");
						lblCurrentFile.setForeground(ImageWorker.randomColorRGB(1));
					}else {
						errorAdvice("[Error - not saved]");
						lblCurrentFile.setText(loadedFile.getName() + "(*)");
						lblCurrentFile.setForeground(Color.RED);
					}
					lblCurrentFile.setToolTipText(loadedFile.getAbsolutePath());
				}
			}
		});
		
		// SAVE TEXT FILE AS
		btnSaveTextFileAs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			
				if (!alreadySaving) {
					// update signal value
					alreadySaving = true;
					JFileChooser fileChooser = new JFileChooser();
					fileChooser.setCurrentDirectory(new File(currentConfiguration.getFileOpt().getLastDstFolderPath()));
					fileChooser.setDialogTitle("Save " + CURRENT_EXT_OUT + " File");
					int returnVal = fileChooser.showSaveDialog(parentFrame);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						boolean done = false;
						try {
							File selectedFile = fileChooser.getSelectedFile();
							selectedFile = adaptFileExt(selectedFile);
							
							if (selectedFile.exists()) {
								if (JOptionPane.showConfirmDialog(parentFrame, "File " + selectedFile.getName()
										+ " exists, overwrite it?") == JOptionPane.OK_OPTION) {
									done = FileStuff.writeStringToFile(textArea_1.getText(), selectedFile, true);
								} else {
									done = FileStuff.writeStringToFile(textArea_1.getText(), selectedFile, false);
								}
							} else {
								done = selectedFile.createNewFile();
								PrintWriter out = new PrintWriter(selectedFile.getAbsolutePath());
								out.write(textArea_1.getText());
								out.close();
							}

							if (done) {
								JOptionPane.showMessageDialog(parentFrame,"File " + selectedFile.getName() + " succesfully saved!");
								loadedFile = selectedFile;
								successAdvice("[File saved]");
								lblCurrentFile.setText(loadedFile.getName());
								lblCurrentFile.setToolTipText(loadedFile.getAbsolutePath());
								currentConfiguration.getFileOpt().setLastDstFolderPath(loadedFile.getParentFile().getAbsolutePath());
							}else {
								errorAdvice("[Error Saving selected file]");
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
			}
		});
		
		// LOAD TEXT FILE
		btnLoadTextFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				if (!alreadyLoading) {
					
					askSave();
					
					alreadyLoading = true;
					JFileChooser fileChooser = new JFileChooser();
					fileChooser.setDialogTitle("Load File");
					fileChooser.setCurrentDirectory(new File(currentConfiguration.getFileOpt().getLastSrcFolderPath()));
					int returnVal = fileChooser.showOpenDialog(null);
					if (returnVal == JFileChooser.APPROVE_OPTION) {

						File source = fileChooser.getSelectedFile();
						if (source == null || !source.exists()) {
							JOptionPane.showMessageDialog(null, "Please select a valid file!");
						} else {
							BufferedReader reader;
							textArea_1.setText("");

							try {
								//reader = new BufferedReader(new FileReader(source.getAbsolutePath()));
								reader = new BufferedReader(new InputStreamReader(new FileInputStream(source), System.getProperty("file.encoding")));
								String line = reader.readLine();
								while (line != null) {
									textArea_1.append(line);
									textArea_1.append("\n");
									line = reader.readLine();
								}
								infoAdvice("[New file loaded]");
								reader.close();
								
								loadedFile = source;
								lblCurrentFile.setText(loadedFile.getName());
								lblCurrentFile.setToolTipText(loadedFile.getAbsolutePath());
								currentConfiguration.getFileOpt().setLastSrcFolderPath(source.getParentFile().getAbsolutePath());

							} catch (Exception e1) {
								JOptionPane.showMessageDialog(null, "[Error loading selected file]");
								errorAdvice("[Error loading selected file]");
							}
						}
					} 
					alreadyLoading = false;
				}// end if !already loading
			}
		});
		
		// TEXT AREA LISTENER
		textArea_1.getDocument().addDocumentListener(new DocumentListener() {
			
			public void removeUpdate(DocumentEvent e) {
				fileChangedLblUpdate();
			}
			public void insertUpdate(DocumentEvent e) {
				fileChangedLblUpdate();
			}
			public void changedUpdate(DocumentEvent e) {
				fileChangedLblUpdate();
			}
		});
	}
	
	private void fileChangedLblUpdate() {
		if (loadedFile != null ) {
			lblCurrentFile.setText(loadedFile.getName()+" (*)");
			lblCurrentFile.setToolTipText(loadedFile.getAbsolutePath());
		}
	}
	
	private boolean askSave() {
		int askSave = JOptionPane.NO_OPTION;
		if (loadedFile == null && !textArea_1.getText().equals("")) {
			askSave = JOptionPane.showConfirmDialog(currentTextEditor, "Do you want to save current file before opening?",
					"Save current file?", JOptionPane.YES_NO_OPTION);
		}
		if(askSave == JOptionPane.OK_OPTION){
			if(loadedFile != null) {
				currentTextEditor.btnSaveTextFile.doClick();
			}else {
				currentTextEditor.btnSaveTextFileAs.doClick();
			}
			return true;
		}else if(askSave == JOptionPane.NO_OPTION) {
			return false;
		}else return false;

	}
	
	public void initGuiFromConfig(){
		TextEditorOption txtOptions = currentConfiguration.getTextEditorOpt();
		slider_margin.setValue(txtOptions.getMargin());
		sliderFontSize.setValue(txtOptions.getFont().getSize());
		sliderSpokenSpeed.setValue(txtOptions.getVoiceRate());
		textArea_1.setBackground(txtOptions.getBackCol());
		textArea_1.setForeground(txtOptions.getForeCol());
		textArea_1.setFont(txtOptions.getFont());
	}
	

	/**
	 *  KEYBOARD SHORTCUTS - use custom listener
	 */
	private KeyEventDispatcher getKeyEventHandler() {
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
}

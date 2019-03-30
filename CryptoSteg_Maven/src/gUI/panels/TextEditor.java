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
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import net.miginfocom.swing.MigLayout;
import uTILS.local.FileStuff;
import uTILS.local.HandleString;
import uTILS.local.Text2Speech;

import javax.swing.SwingConstants;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.drjekyll.fontchooser.FontDialog;

import crypt.aes.AesCipher;
import iNIT.INItializer;

import java.awt.SystemColor;
import javax.swing.JTextArea;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.awt.event.ActionEvent;
import javax.swing.JScrollPane;
import javax.swing.ImageIcon;
import javax.swing.JSlider;
import javax.swing.event.ChangeListener;
import javax.swing.undo.UndoManager;
import javax.swing.event.ChangeEvent;
import javax.swing.UIManager;
import javax.swing.JCheckBox;
import javax.swing.JTextField;
import javax.swing.JSeparator;
import javax.swing.border.LineBorder;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import gUI.om.SupportedFileTextEditor;
import gUI.utils.filedialog.TemplatesChoosePanel;
import gUI.utils.shortcutshandler.TextEditorListener;

public class TextEditor extends JPanel {

	// TextFileWriter logger creation
	static Logger logger = Logger.getLogger(TextEditor.class);
	
	//  RESOURCE FILES //
	private final String LOREM_IPSUM = "Resources_Scrypt/LoremIpsum.txt";
	
	// ICONS //
	public final String ICON_TEXT_CLEAR = "Resources_Scrypt/Icons/Clear.png";
	public final String ICON_TEXT_BOLD = "Resources_Scrypt/Icons/Bold.png";
	public final String ICON_TEXT_ITALIC = "Resources_Scrypt/Icons/Italic.png";
	public final String ICON_TEXT_COLOR = "Resources_Scrypt/Icons/TextColor_2.png";
	public final String ICON_TEXT_BG_COLOR = "Resources_Scrypt/Icons/BackgroundColor.png";
	public final String ICON_TEXT_FONT = "Resources_Scrypt/Icons/Font_2.png";
	public final String ICON_FILE_SAVE = "Resources_Scrypt/Icons/SaveFile.png";
	public final String ICON_FILE_LOAD = "Resources_Scrypt/Icons/LoadFile.png";
	public final String ICON_REDO = "Resources_Scrypt/Icons/Redo.png";
	public final String ICON_UNDO = "Resources_Scrypt/Icons/Undo.png";
	public final String ICON_CRYPT = "Resources_Scrypt/Icons/Crypt.png";
	public final String ICON_DECRYPT = "Resources_Scrypt/Icons/Decrypt.png";
	public final String ICON_SPEAKER = "Resources_Scrypt/Icons/Speaker.png";
	
	// TOOLTIPS //
	private static final String tooltipSaveTextFile = "[Ctrl+S] Choose name and folder, save current file";
	private static final String tooltipLoadTextFile = "[Ctrl+O] Load source File";
	private static final String tooltipSpeak = "[Ctrl+spacebar] Speak the text written in the text area (only english). Use the upper slider to change speed before to play, press again to disable";
	private static final String tooltipDecrypt = "[Ctrl+shift+D]Decrypt displayed text with given key";
	private static final String tooltipCrypt = "[Ctrl+shift+C] Crypt displayed text with given key";
	private static final String tooltipBackgroundColor = "Change background color (won't be saved in .txt)";
	private static final String tooltipForegroundTxt = "Change text color (won't be saved in .txt)";
	private static final String tooltipDarkSwitch = "Switch upon dark and light text combination";
	private static final String tooltipUpperPostDots = "Makes text uppercase after dots a significat line brokers";
	private static final String tooltipTt = "Makes textArea content uppercase";
	private static final String tooltipTtTt = "Makes al initial characters uppercase";
	private static final String tooltipLoremIpsum = "Insert classic test text \"Lorem Ipsum\"";
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
	public JButton btnSaveTextFile;
	public JButton btnLoadTextFile;
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
	
	public JComboBox comboFileFormat;
	
	public JSlider sliderSpokenSpeed;
	public JSlider sliderFontSize;
	public JSlider slider_margin;
	
	public JTextArea textArea_1;
	private JTextField txtAesKey; // field containing fast crypt text user key, sinchronized from ini file as default
	
	private JFrame parentFrame; // the frame that instantiate this object

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

	
//////////////// CONSTRUCTOR /////////////////////
	
	public TextEditor(JFrame parent, INItializer currentConf) {

		logger.info(this.getClass().getName() + " Starting...");
		
		// initialize values used in gui //
		currentConfiguration = currentConf;		
		parentFrame = parent;		
		currentTextEditor=this;
		LBL_ADVICE_COLOR = new Color(124, 252, 0);
		textMargin = 14;
		uppercase = false;
		uppercaseDots=true;
		
		setBackground(Color.DARK_GRAY);
		setLayout(new MigLayout("", "[40][40][70][4%][][][][:40:40][:40:40][:40:40][:40:40][:30:30][:40:80px][20][20][30][40.00][40,fill][70][35][35,grow][70]", "[:20:50px,grow][][20][][25][25][52.33%,grow]"));
		
		lblCurrentFileLBL = new JLabel("   Current file:");
		lblCurrentFileLBL.setForeground(SystemColor.controlHighlight);
		lblCurrentFileLBL.setFont(new Font("Tahoma", Font.BOLD, 18));
		add(lblCurrentFileLBL, "cell 0 2 3 1,alignx center");
		
		lblCurrentFile = new JLabel("\\*.txt");
		lblCurrentFile.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblCurrentFile.setForeground(SystemColor.activeCaption);
		add(lblCurrentFile, "cell 3 2 6 1,alignx left");
		
		lblVoiceSpeed = new JLabel("Voice rate");
		lblVoiceSpeed.setForeground(new Color(211, 211, 211));
		add(lblVoiceSpeed, "cell 9 2 2 1,alignx center");
		
		sliderSpokenSpeed = new JSlider();
		sliderSpokenSpeed.setValue(90);
		sliderSpokenSpeed.setMaximum(400);
		add(sliderSpokenSpeed, "cell 11 2 2 1,alignx center");
		
		lblAdvice = new JLabel("");
		lblAdvice.setBackground(SystemColor.info);
		lblAdvice.setFont(new Font("Tahoma", Font.BOLD, 13));
		lblAdvice.setForeground(new Color(176, 224, 230));
		lblAdvice.setHorizontalAlignment(SwingConstants.CENTER);
		add(lblAdvice, "cell 20 2 2 1,alignx center,aligny center");
		lblAdvice.setText(" [Current output: .txt]");
		
		btnCrypt = new JButton("");
		btnCrypt.setBackground(SystemColor.activeCaption);
		btnCrypt.setIcon(new ImageIcon(ICON_CRYPT));
		btnCrypt.setToolTipText(tooltipCrypt);
		btnCrypt.setFont(new Font("Tahoma", Font.BOLD, 12));
		btnCrypt.setForeground(SystemColor.inactiveCaption);
		add(btnCrypt, "cell 16 4,growx");
		
		btnDecrypt = new JButton("");
		btnDecrypt.setBackground(SystemColor.activeCaption);
		btnDecrypt.setIcon(new ImageIcon(ICON_DECRYPT));
		btnDecrypt.setToolTipText(tooltipDecrypt);
		btnDecrypt.setForeground(SystemColor.inactiveCaption);
		btnDecrypt.setFont(new Font("Tahoma", Font.BOLD, 12));
		add(btnDecrypt, "cell 16 5,growx");
		
		JScrollPane textScrollPaneArea = new JScrollPane();
		add(textScrollPaneArea, "cell 0 6 22 1,grow");
		textScrollPaneArea.setFont(new Font("DejaVu Sans", Font.PLAIN, 18));
		
		textArea_1 = new JTextArea();
		textArea_1.setFont(new Font("Dialog", Font.PLAIN, 20));
		textScrollPaneArea.setViewportView(textArea_1);
		manager = new UndoManager();
		manager.setLimit(1000);
		textArea_1.getDocument().addUndoableEditListener(manager);
		
		lblSIZE = new JLabel("");
		lblSIZE.setForeground(SystemColor.window);
		add(lblSIZE, "cell 19 5");
		lblSIZE.setText(String.valueOf(textArea_1.getFont().getSize())+" Px");
		
		btnBackgroundCol = new JButton("");
		btnBackgroundCol.setToolTipText(tooltipBackgroundColor);
		btnBackgroundCol.setBackground(new Color(176, 196, 222));
		btnBackgroundCol.setIcon(new ImageIcon(ICON_TEXT_BG_COLOR));
		add(btnBackgroundCol, "cell 9 4,alignx center");
		
		sliderFontSize = new JSlider(4,100);
		sliderFontSize.setForeground(SystemColor.activeCaptionBorder);
		sliderFontSize.setBackground(UIManager.getColor("Button.shadow"));
		sliderFontSize.setValue(textArea_1.getFont().getSize());
		add(sliderFontSize, "cell 20 5 2 1,growx");
		
		btnLoremIpsum = new JButton("Lorem Ipsum");
		btnLoremIpsum.setToolTipText(tooltipLoremIpsum);
		btnLoremIpsum.setFont(new Font("SansSerif", Font.BOLD, 12));
		btnLoremIpsum.setBackground(new Color(176, 196, 222));
		add(btnLoremIpsum, "cell 13 5 3 1,growx");
		
		btnFontChooser = new JButton("");
		btnFontChooser.setToolTipText(tooltipFontChooser);
		
		btnSpeak = new JButton("");
		btnSpeak.setToolTipText(tooltipSpeak);
		btnSpeak.setIcon(new ImageIcon(ICON_SPEAKER));
		btnSpeak.setFont(new Font("SansSerif", Font.BOLD, 12));
		btnSpeak.setBackground(new Color(176, 196, 222));
		add(btnSpeak, "cell 10 4,alignx center");

		btnFontChooser.setBackground(new Color(176, 196, 222));
		btnFontChooser.setIcon(new ImageIcon(ICON_TEXT_FONT));
		add(btnFontChooser, "cell 10 5,alignx center"); 
		
		lblNewLabel = new JLabel("\u2193  (AES KEY)  \u2193");
		lblNewLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
		lblNewLabel.setForeground(new Color(211, 211, 211));
		add(lblNewLabel, "cell 13 2 3 1,alignx center");
		
		JSeparator separator_1 = new JSeparator();
		add(separator_1, "cell 0 3 22 1,growx");
		
		btnSaveTextFile = new JButton("");
		btnSaveTextFile.setToolTipText(tooltipSaveTextFile);
		btnSaveTextFile.setForeground(SystemColor.menuText);
		btnSaveTextFile.setIcon(new ImageIcon(ICON_FILE_SAVE));
		btnSaveTextFile.setBackground(SystemColor.inactiveCaption);
		add(btnSaveTextFile, "cell 1 4,grow");
		
		comboFileFormat = new JComboBox();
		comboFileFormat.setToolTipText(tooltipComboFileFormat);
		comboFileFormat.setBackground(SystemColor.textHighlight);
		comboFileFormat.setModel(new DefaultComboBoxModel(SupportedFileTextEditor.values()));
		add(comboFileFormat, "cell 2 4,growx");
		
		btnTemplates = new JButton("Templates");
		btnTemplates.setToolTipText(tooltipTemplates);
		btnTemplates.setFont(new Font("SansSerif", Font.BOLD, 12));
		btnTemplates.setBackground(new Color(176, 196, 222));
		add(btnTemplates, "cell 11 5 2 1,growx");
		
		txtAesKey = new JTextField();
		txtAesKey.setHorizontalAlignment(SwingConstants.CENTER);
		txtAesKey.setText(currentConfiguration.getAesOpt().getAesKey());
		add(txtAesKey, "cell 11 4 5 1,growx");
		txtAesKey.setColumns(10);
		AesCipher.check(txtAesKey);
				
		JPanel panel = new JPanel();
		panel.setForeground(SystemColor.menu);
		panel.setBorder(new LineBorder(new Color(0, 0, 0), 4));
		panel.setBackground(new Color(100, 149, 237));
		add(panel, "cell 0 0 22 1,growx,aligny center");
		panel.setLayout(new MigLayout("", "[100px,grow 10000,left][][][][][][][][][][][][][][][][][][][][][][grow][grow][]", "[]"));
		
		lblTextEditor = new JLabel("Text Editor");
		panel.add(lblTextEditor, "cell 0 0,alignx left");
		lblTextEditor.setHorizontalAlignment(SwingConstants.CENTER);
		lblTextEditor.setForeground(Color.BLACK);
		lblTextEditor.setFont(new Font("Lithos Pro Regular", Font.BOLD, 19));
		lblTextEditor.setBackground(SystemColor.activeCaptionBorder);
		
		chkCutWordsTo = new JCheckBox("Adapt Text Width");
		panel.add(chkCutWordsTo, "cell 23 0");
		chkCutWordsTo.setToolTipText(tooltipCutWordsTo);
		chkCutWordsTo.setSelected(true);
		chkCutWordsTo.setForeground(SystemColor.control);
		chkCutWordsTo.setBackground(Color.DARK_GRAY);
		
		chkFitmantainwords = new JCheckBox("and...Mantain Words!");
		panel.add(chkFitmantainwords, "cell 24 0");
		chkFitmantainwords.setToolTipText(tooltipFitMaintainWords);
		chkFitmantainwords.setForeground(SystemColor.control);
		chkFitmantainwords.setBackground(Color.DARK_GRAY);
		chkFitmantainwords.setEnabled(false);
		
		lblMargin = new JLabel("Margin:");
		lblMargin.setHorizontalAlignment(SwingConstants.CENTER);
		lblMargin.setForeground(SystemColor.menu);
		lblMargin.setBackground(UIManager.getColor("CheckBox.darkShadow"));
		add(lblMargin, "cell 17 4 2 1,alignx center");
		
		slider_margin = new JSlider(0,100,0);
		slider_margin.setBackground(UIManager.getColor("Button.shadow"));
		add(slider_margin, "cell 19 4 3 1,growx");
		
		lblFontSize = new JLabel("Font size: ");
		lblFontSize.setHorizontalAlignment(SwingConstants.CENTER);
		lblFontSize.setForeground(SystemColor.menu);
		add(lblFontSize, "cell 17 5 2 1,alignx center");
		
		btntTextFormat = new JButton("Format");
		btntTextFormat.setFont(new Font("Tahoma", Font.BOLD, 13));
		btntTextFormat.setBackground(new Color(176, 196, 222));
		add(btntTextFormat, "cell 3 4,growx");
		
		JSeparator separator = new JSeparator();
		add(separator, "cell 0 1 22 1,growx");
		
		btnLoadTextFile = new JButton("");
		btnLoadTextFile.setToolTipText(tooltipLoadTextFile);
		btnLoadTextFile.setForeground(SystemColor.menuText);
		btnLoadTextFile.setBackground(SystemColor.inactiveCaption);
		btnLoadTextFile.setIcon(new ImageIcon(ICON_FILE_LOAD));
		add(btnLoadTextFile, "cell 0 4,grow");
		
		btnDefaultOpt = new JButton("Default Opt");
		btnDefaultOpt.setFont(new Font("Tahoma", Font.BOLD, 13));
		btnDefaultOpt.setToolTipText(tooltipDefaultOpt);
		btnDefaultOpt.setForeground(SystemColor.menuText);
		btnDefaultOpt.setBackground(new Color(176, 196, 222));
		add(btnDefaultOpt, "cell 4 4 2 1,growx");
		
		btnUndo = new JButton("");
		btnUndo.setToolTipText(tooltipUndo);
		btnUndo.setBackground(new Color(176, 196, 222));
		add(btnUndo, "cell 7 4,alignx center");
		btnUndo.setIcon(new ImageIcon(ICON_UNDO));
		
		btnRedo = new JButton("");
		btnRedo.setToolTipText(tooltipRedo);
		btnRedo.setBackground(new Color(176, 196, 222));
		add(btnRedo, "cell 8 4,alignx center");
		btnRedo.setIcon(new ImageIcon(ICON_REDO));
		
		btnClear = new JButton("");
		btnClear.setToolTipText(tooltipClearText);
		btnClear.setBackground(new Color(176, 196, 222));
		btnClear.setIcon(new ImageIcon(ICON_TEXT_CLEAR));
		btnClear.setFont(new Font("Wide Latin", Font.BOLD, 14));
		add(btnClear, "cell 6 4,growx,aligny center");
		
		btnUpperPostDots = new JButton(".Tt");
		btnUpperPostDots.setToolTipText(tooltipUpperPostDots);
		btnUpperPostDots.setFont(new Font("SansSerif", Font.BOLD, 12));
		btnUpperPostDots.setBackground(new Color(176, 196, 222));
		add(btnUpperPostDots, "cell 6 5,growx");
		textArea_1.setMargin(new Insets(textMargin/2, textMargin, textMargin/2, textMargin));
		
		btnBold = new JButton("");
		btnBold.setToolTipText(tooltipBoldText);
		btnBold.setBackground(new Color(176, 196, 222));
		btnBold.setIcon(new ImageIcon(ICON_TEXT_BOLD));
		add(btnBold, "cell 8 5,alignx center");
		
		btnDark = new JButton("Dark/Light");
		btnDark.setToolTipText(tooltipDarkSwitch);
		btnDark.setFont(new Font("Tahoma", Font.BOLD, 13));
		btnDark.setForeground(SystemColor.menuText);
		btnDark.setBackground(SystemColor.controlShadow);
		btnDark.setIcon(null);
		btnDark.setPreferredSize(btnBold.size());
		add(btnDark, "cell 0 5 4 1,growx,aligny center");
		btnDark.setForeground(Color.LIGHT_GRAY);
		btnDark.setBackground(Color.DARK_GRAY);
		
		btnTt = new JButton("TT");
		btnTt.setFont(new Font("SansSerif", Font.BOLD, 12));
		btnTt.setBackground(new Color(176, 196, 222));
		btnTt.setToolTipText(tooltipTt);
		add(btnTt, "cell 4 5,growx");
		
		btnTtTt = new JButton("Tt Tt");
		btnTtTt.setFont(new Font("SansSerif", Font.BOLD, 12));
		btnTtTt.setBackground(new Color(176, 196, 222));
		btnTtTt.setToolTipText(tooltipTtTt);
		add(btnTtTt, "cell 5 5,growx");
		
		if(chkCutWordsTo.isSelected()) {
			textArea_1.setLineWrap(true);
			chkFitmantainwords.setEnabled(true);
		}
		
		btnItaliC = new JButton("");
		btnItaliC.setToolTipText(tooltipItalic);
		btnItaliC.setBackground(new Color(176, 196, 222));
		btnItaliC.setIcon(new ImageIcon(ICON_TEXT_ITALIC));
		add(btnItaliC, "cell 7 5,alignx center");
		
		btnForegroundCol = new JButton("");
		btnForegroundCol.setToolTipText(tooltipForegroundTxt);
		btnForegroundCol.setBackground(new Color(176, 196, 222));
		btnForegroundCol.setIcon(new ImageIcon(ICON_TEXT_COLOR));
		add(btnForegroundCol, "cell 9 5,alignx center");
		
		JScrollBar scrollBar = new JScrollBar();
		
		parentFrame.getContentPane().add(scrollBar);
		
		addHandlers();
		
		// add custom keyboard listener
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(getKeyEventHandler());
		
		startAesKeyCheckThread();

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
		
		// CRYPT TEXT
		btnCrypt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// this function is used to change color and tooltip text according to key lenght
				AesCipher.check(txtAesKey);
				
				if (txtAesKey.getText().length() != 0) {
					try {
						AesCipher crypter = new AesCipher(txtAesKey.getText());
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
				
				if (txtAesKey.getText().length() != 0) {
					try {
						AesCipher decrypter = new AesCipher(txtAesKey.getText());
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
				txtAesKey.setText(currentConfiguration.getAesOpt().getAesKey());
				slider_margin.setValue(10);
				sliderFontSize.setValue(textArea_1.getFont().getSize());
				lblSIZE.setText(String.valueOf(textArea_1.getFont().getSize())+" Px");
			}
		});
		
		//BACKGROUND TEXT COLOR
		btnBackgroundCol.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				JColorChooser chooser = new JColorChooser();
			    JDialog dialog = chooser.createDialog(parentFrame,"Choose Background color",true,chooser, new ActionListener() {
					
					@Override // OK
					public void actionPerformed(ActionEvent e) {
						textArea_1.setBackground(chooser.getColor());					
					}
				}, new ActionListener() {
					
					@Override // NO
					public void actionPerformed(ActionEvent e) {	
					}
				});
				dialog.setAlwaysOnTop(true);
				dialog.setVisible(true);
			}
		});
		
		//FOREGROUND TEXT COLOR
		btnForegroundCol.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {      
			      
				JColorChooser chooser = new JColorChooser();
			    JDialog dialog = chooser.createDialog(parentFrame,"Choose Foreground color",true,chooser, new ActionListener() {
					
					@Override // OK
					public void actionPerformed(ActionEvent e) {
						Color c = chooser.getColor();
						if (c != null) {
					        textArea_1.setForeground(c);
				      }			
					}
				}, new ActionListener() {
					@Override // NO
					public void actionPerformed(ActionEvent e) {	
					}
				});
				dialog.setAlwaysOnTop(true);
				dialog.setVisible(true);
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
			}
		});
		
		// INSERT LOREM IPSUM
		btnLoremIpsum.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
							
				FileStuff.appendFileTxtArea(new File(LOREM_IPSUM), textArea_1);
			}
		});
		
		// FONT CHOOSER PANEL
		btnFontChooser.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FontDialog.showDialog(textArea_1);
				sliderFontSize.setValue(textArea_1.getFont().getSize());
				lblSIZE.setText(String.valueOf(textArea_1.getFont().getSize())+" Px");			}
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
				lblSIZE.setText(String.valueOf(textArea_1.getFont().getSize())+" Px");			}
		});
		
		// SET SPEAKER'S VOICE SPEED
		sliderSpokenSpeed.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if(objSpeaker!=null) {
					objSpeaker.voice.setDurationStretch((sliderSpokenSpeed.getValue()));
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
					lblAdvice.setText("[Current output: "+CURRENT_EXT_OUT+"]");
					btnSaveTextFile.setToolTipText("Choose name and folder, save "+CURRENT_EXT_OUT+" file!");
				}
			}
		);
		
		// OPEN TEMPLATES PANEL
		btnTemplates.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFrame panel = new TemplatesChoosePanel(textArea_1, comboFileFormat);
		    	panel.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		    	panel.setBounds(750,20,500,170);
		    	panel.setAlwaysOnTop(true);
		    	panel.setVisible(true);
		    	panel.setTitle("IMG Generator (V1.1) - A.M.2018");
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
			}
		});
		
		// SET TEXT STYLE TO ITALICS
		btnItaliC.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				 if(!textArea_1.getFont().isItalic()) {
					textArea_1.setFont(textArea_1.getFont().deriveFont(Font.ITALIC, textArea_1.getFont().getSize()));
				 }else {
					textArea_1.setFont(textArea_1.getFont().deriveFont(Font.PLAIN, textArea_1.getFont().getSize()));
				 }		
			}
		});
		
		// MAKE TEXT BOLD
		btnBold.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				 if(!textArea_1.getFont().isBold()) {
					textArea_1.setFont(textArea_1.getFont().deriveFont(Font.BOLD, textArea_1.getFont().getSize()));
				 }else {
					textArea_1.setFont(textArea_1.getFont().deriveFont(Font.PLAIN, textArea_1.getFont().getSize()));
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
							String givenExtension = FilenameUtils.getExtension(selectedFile.getName());

							// if not specified extension, invalid or equal to checkbox, then change extension to current in global var
							if (("." + givenExtension).equals(CURRENT_EXT_OUT) || givenExtension.equals("") || (("." + givenExtension).length() > 5)) {
								selectedFile = FileStuff.changeExtension(selectedFile, CURRENT_EXT_OUT);
							} else {
								selectedFile = FileStuff.changeExtension(selectedFile, "." + givenExtension);
							}
							if (selectedFile.exists()) {
								if (JOptionPane.showConfirmDialog(parentFrame, "File " + selectedFile.getName()
										+ " exists, overwrite it?") == JOptionPane.OK_OPTION) {
									selectedFile.delete();
									done = selectedFile.createNewFile();
								    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(selectedFile), System.getProperty("file.encoding"));
									PrintWriter out = new PrintWriter(outputStreamWriter);
									out.write(textArea_1.getText());
									out.close();
									outputStreamWriter.close();
								} else {
									JOptionPane.showMessageDialog(parentFrame, "Operation cancelled");
								}
							} else {
								done = selectedFile.createNewFile();
								PrintWriter out = new PrintWriter(selectedFile.getAbsolutePath());
								out.write(textArea_1.getText());
								out.close();
							}

							if (done) {
								JOptionPane.showMessageDialog(parentFrame,
										"File " + selectedFile.getName() + " succesfully saved!");
								lblCurrentFile.setText(selectedFile.getName());
								currentConfiguration.getFileOpt()
										.setLastDstFolderPath(selectedFile.getParentFile().getAbsolutePath());
							}
						} catch (IOException e1) {
							JOptionPane.showMessageDialog(null, "error Saving the selected file :(");
							logger.error("Exception happened!", e1);
						}
						new Thread(new Runnable() {

							@Override
							public void run() {
								try {
									Thread.sleep(4000);
									lblAdvice.setText("");
								} catch (InterruptedException e) {
									logger.error("Exception happened!", e);
								}
							}
						}).start();
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
					alreadyLoading = true;
					JFileChooser fileChooser = new JFileChooser();
					fileChooser.setDialogTitle("Load File");
					fileChooser.setCurrentDirectory(new File(currentConfiguration.getFileOpt().getLastSrcFolderPath()));
					int returnVal = fileChooser.showOpenDialog(null);
					if (returnVal == JFileChooser.APPROVE_OPTION) {

						File source = fileChooser.getSelectedFile();
						if (source == null) {
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
								lblAdvice.setText("[New file loaded]");
								reader.close();
								lblCurrentFile.setText(source.getParentFile().getName() + "\\"
										+ fileChooser.getSelectedFile().getName());
								currentConfiguration.getFileOpt()
										.setLastSrcFolderPath(source.getParentFile().getAbsolutePath());

							} catch (Exception e1) {
								JOptionPane.showMessageDialog(null, "[Error loading the selected file]");
							}
							new Thread(new Runnable() {
								// new thread to show advice of loaded file in label without block GUI
								@Override
								public void run() {
									try {
										Thread.sleep(4000);
										lblAdvice.setText("");
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
								}
							}).start();
						}
					} 
					alreadyLoading = false;
				}// end if !already loading
			}
		});
	}
	

	// initialize listener
	/**
	 *  KEYBOARD SHORTCUTS - use custom listener
	 */
	private KeyEventDispatcher getKeyEventHandler() {
		return new TextEditorListener(currentTextEditor); 
	}
	
}

package gUI.panels;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;

import javax.swing.JTree;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import amfileutils.FileStuff;
import amfileutils.filechangewatcher.FileChangeMonitor;
import crypt.aes.AesCipher;
import crypt.manager.CryptoMode;
import crypt.manager.ScreeptManagerFileOrFolder;
import dialogutils.JOptionHelper;
import enums.BufferedImageType;
import frameutils.frame.arch.ModulePanel;
import frameutils.utils.GuiUtils;
import gUI.frames.KeyGenFrame;
import gUI.utils.shortcutshandler.CrypterPanelShortcutsListener;
import gUI.utils.tree.crypter.TreeFileMouseListenerCrypter;
import gUI.utils.tree.crypter.dragndrop.VirtualFileDropPane;
import guistyle.GUIStyelSettings;
import maingUI.MainGUI;
import maingUI.MainGuiGeneralAppStarter;
import mainobj.INItializer;
import media.ImageGeneralWorker;
import net.miginfocom.swing.MigLayout;
import om.CrypterResultDTO;
import resources.GeneralConfig;
import resources.IconsPathConfigurator;
import splitpanel.SplitPaneUtils;
import treeutils.om.FileTreeModel;
import util.UpdateProgressPanelCrypter;
import variousutils.HandleString;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.Highlighter;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import java.awt.SystemColor;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTabbedPane;
import javax.swing.JComboBox;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.ScrollPaneConstants;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Dimension;
import java.awt.Rectangle;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.awt.event.ActionEvent;
import javax.swing.JCheckBox;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;

import java.awt.Cursor;
import javax.swing.JPasswordField;
import javax.swing.border.EmptyBorder;

public class CrypterPanel extends ModulePanel {
	private static final long serialVersionUID = 2683201298719353959L;

	static Logger logger = Logger.getLogger(CrypterPanel.class);

	// dateFormatter
	private static SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd @ HH-mm-ss");
	
	private static final Dimension screenSize = GuiUtils.getScreenSize();
	private static final Dimension screenSizeLess = new Dimension(screenSize.width - 5, screenSize.height - 10);
	private static final Dimension maxBodySize = new Dimension(screenSize.width - 5, screenSize.height - 40);
	private static final Dimension maxSplitPaneSize = new Dimension(screenSize.width - 5, screenSize.height - 170);
	
	public static final int labelLengthLimit = 40;
	public static final char DEFAULT_PSW_CHAR = '\u25CF';
	
	// static path of keyDictionary
	public static final String KEY_DICTIONARY = KeyGenFrame.KEY_DICTIONARY;
		
	// file representing dictionary
	public static final File KEY_DICTIONARY_FILE = new File(KEY_DICTIONARY);
	
///////////////////////////////////////////////
		//  GUI-RELATED-OBJECTS   //
		////////////////////////////
	
	private static final String unSelectedTooltip = "<b>Currently deactivated</b> - Activate to improve security (only if supported for current algorithm)<br/><br/>"; 

	private static final String shuffleTooltip = 
			"When this mode is active, an additional phase will reorder bytes based on given key values, in order to increase algorithm security and maximize entrophy diffusion. "
			+ "<br/> <b>NB: </b> <u>When decrypting there's no need to choose the right option</u>, this will be recognized automatically from encoded image file name.";
	
	private static final String hashedTooltip = 
			"When this mode is active, the given password will be hashed to avoid original password decription even if the data is compromised. "
			+ "<br/> <b>NB:</b> <u>When decrypting there's no need to choose the right option</u>, this will be recognized automatically from encoded file.";
	
	private static final String onlyAesTooltip = 
			"<u>ONLY_AES mode</u> uses AES algorithm (128 bits) to encrypt/decrypt a copy of your data."
			+ "<br/> To use this <u>you only have to specify a string password</u> (or generate a random new one.. just be sure to remember it). The best length is 16 charachters."
			+ "<br/><br/> <b>NB:</b> With this mode you can choose to hash password and/or shuffle original bytes to maximize security, but with a slower process.";
	
	private static final String onlyStegTooltip = 
			"<u>ONLY_STEG mode</u> uses brand new STEG algorithm to encrypt/decrypt a copy of your data. "
			+ "<br/>To use this you only have to specify a key image with PNG extension that will be your password."
			+ "<br/> You can load a one-use only image, generate a new random image, or either choose one from your dictionary."
			+ "<br/> <u>Without key image you won't be able to decrypt</u> the crypted copy of your data once it's crypted: keep it in a safe place! (I suggest you to use a usb Stick)"
			+ "<br/><br/> <b>NB:</b> With this mode you can choose to shuffle original bytes to maximize security, with a slower process, while key hashing is not supported.";
	
	private static final String completeModeTooltip = 
			"<u>COMPLETE mode</u> is the maximum security mode, that combines AES 128 bits and STEG algorithm, in order to encrypt/decrypt a copy of your data. "
			+ "<br/> To use this you have to specify both AES password and key image with PNG extension that will be the added-password."
			+ "<br/> You can load a one-use only image, generate a new random image, or either choose one from your dictionary: be careful and keep it in a safe place."
			+ "<br/> <u>Without key password and image you won't be able to decrypt</u> the crypted copy of your data once it's crypted! (I suggest you to use a usb Stick)"
			+ "<br/><br/> <b>NB:</b> With this mode you can choose to hash password and/or shuffle original bytes to maximize security, but with a slower process.";
	

	// text Areas
	private JTextArea textAreaMain;
	private JTextArea textAreaLog;
	
	// password field
	private JCheckBox chckbxShowAesKey;

	//Jtree used to represent dictionary
	private JTree tree;

	// listener used to refresh file options displayed
	private TreeSelectionListener treeListener;
	
	// tree model to specify root
	private TreeModel treeModel;
	
	// Jlabels
	private JLabel lblSelectedModeStatic;
	private JLabel lblAesKeyStatic;
	private JLabel lblSelectedInputStatic;
	private JLabel lblKeyImageSelected;
	private JLabel labelFlagCodeReady;
	private JLabel lblDisplayedKey;
	private JLabel lblSourceFileStatic;
	private JLabel lblSrcFileSel;
	private JLabel lblScreeptHeader;
	private JLabel iconInfoMode;

	// frame of parent GUI-object (main-GUI)
	private JFrame parentFrame;
	private MainGUI mainApplication;
	
	// panels
	private JPanel leftSplitTopPanel;
	private JPanel panelMainContainer;
	private JPanel panelContainerKey;
	private JPanel panelStatus;	
	private JPanel panelKeyImgDisp;
	private JPanel panelHeader;
	
	// scroll panel for jtree
	private JScrollPane scrollPaneTree;
	
	// other scroll-panels
	private JScrollPane mainScrollPane;
	private JScrollPane scrollPaneText;
	private JScrollPane scrollPaneLog;
	private JScrollPane scrollPanelTabImagePreview;

	// info tabbed pane
	private JTabbedPane tabbedPane;
	
	private JSplitPane leftSplitPaneVertical; // Left panel divided in two vertically (tree keys + infoTabbedPanel)
	private JSplitPane mainSplitTextAreaMain; // leftSplitPaneVertical<->textArea
	private JSplitPane mainSplitTextAreaRight; // textArea<->fileDragDropperTree
	
	// Buttons
	public JButton btnDeCreept;
	public JButton btnCreept;
	public JButton btnSelectSourceFile;
	public JButton btnDispoKeyImg;
	public JButton btnGenerateKeyImg;
	public JButton btnGenerateKeyPsw;

	// public field representing current Panel
	public CrypterPanel thisPanel; 
	
	// AES password gui-container
	private JPasswordField AESKEYtext;
	
	// combobox to select conversion mode
	private JComboBox<CryptoMode> comboModeCrypto;

	
///////////////////////////////////////////////
		// CODING-RELATED-OBJECTS //
		////////////////////////////
	
	// Global reference to current configuration
	private INItializer currentConf;
	
	private Runnable afterCompleteRunnable = null;
	private File completedDirectoryImportExport = null;
		
	// field representing the source file loaded by user, this is the file to crypt/decrypt
	private File srcFile;
	private File[] srcFiles; 
	
	// fied representing the img key file selected by user
	private File srcKeyImageFile;
	
	// CODERS objects - shared by methods
	private boolean flagCreateNewMonitor = false;
	public FileChangeMonitor fileMonitor;
	private ScreeptManagerFileOrFolder screeptManagerFoF;
	private CryptoMode mode;
	
	private JCheckBox chckbxShuffle;
	private JCheckBox chckbxHashKey;
	private JScrollPane scrollPaneDragNDropSource;
	private JPanel panelBackgroundDragSources;
	private JTree treeSourcesDragAndDrop;
	private JLabel lblSelectedFilesDragTitle;
	private JSeparator separatorDragNDropTitle;
	public VirtualFileDropPane panelVDragNDropPane;
	
	public UpdateProgressPanelCrypter generalProgress;
	public JOptionHelper dialogHelper;
	
	int leftVerticalDivHeight;
	int leftWidth = 370;
	int rightWidth = 480;
	int rightWidthMin = 465;
	
	public CrypterPanelShortcutsListener shortcutsListener;

	/**
	 * Create the application.
	 */
	public CrypterPanel(INItializer currentConfiguration, MainGUI parent) {
		setBorder(new EmptyBorder(5, 5, 5, 5));

		
/////////////////////////////////////////////////////////////////////////////////////
///////////////					INITIALIZATION    				/////////////////////
/////////////////////////////////////////////////////////////////////////////////////
		logger.info(this.getClass().getName() + " Starting...");
		
		thisPanel = this;
		dialogHelper = new JOptionHelper(this, true);
		screeptManagerFoF = new ScreeptManagerFileOrFolder(generalProgress, thisPanel, false);
		parentFrame=parent.getMainFrame();
		mainApplication = parent;
		leftVerticalDivHeight = mainApplication.getMainFrame().getHeight() / 2;
		
		// set the background of the entire panel
		setBackground(SystemColor.desktop);
		
		setBorder(BorderFactory.createEmptyBorder());
		
		// start with layout & header
		setLayout(new MigLayout("fill, insets 0", "[1024px,grow]", "[711px,grow]"));
		
		panelMainContainer = new JPanel();
		panelMainContainer.setBorder(new EmptyBorder(0, 0, 0, 0));
		panelMainContainer.setMaximumSize(screenSizeLess);
		panelMainContainer.setBackground(Color.BLACK);
		panelMainContainer.setLayout(new MigLayout(
				"fill,insets 0", 
				"[100:200:200][100:205:205][30,grow][100px:250px:250px][100px:120px:120px]",
				"[40px:40px:40px,top][:30px:30px][30px:30px:34px][100px:150px][100px:200px:200px][100px:200px][:1px:1px]"));
		mainScrollPane = new JScrollPane();
		mainScrollPane.setMaximumSize(maxBodySize);
		mainScrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
		mainScrollPane.setViewportView(panelMainContainer);
		add(mainScrollPane, "cell 0 0,grow");
		
		panelHeader = new JPanel();
		panelHeader.setMinimumSize(new Dimension(10, 40));
		panelHeader.setMaximumSize(new Dimension(screenSize.width, 45));
		panelHeader.setForeground(SystemColor.menu);
		panelHeader.setBorder(BorderFactory.createEmptyBorder());
		panelHeader.setBackground(new Color(10,10,10));
		panelMainContainer.add(panelHeader, "cell 0 0 5 1,grow");
		panelHeader.setLayout(new MigLayout("fill,insets 0",
				"[85px:n][][100px][][100px:150px,grow][110px][130px][10px]",
				"[40px:40px]"));
		
		lblSourceFileStatic = new JLabel("Source File:");
		lblSourceFileStatic.setBackground(Color.DARK_GRAY);
		panelHeader.add(lblSourceFileStatic, "cell 0 0,alignx right");
		lblSourceFileStatic.setForeground(new Color(255, 239, 213));
		lblSourceFileStatic.setFont(new Font("Segoe UI", Font.BOLD, 14));
		
		lblSrcFileSel = new JLabel("No file selected");
		lblSrcFileSel.setHorizontalTextPosition(SwingConstants.LEADING);
		lblSrcFileSel.setBackground(Color.DARK_GRAY);
		panelHeader.add(lblSrcFileSel, "cell 1 0,alignx right");
		lblSrcFileSel.setMaximumSize(new Dimension(180, 16));
		lblSrcFileSel.setForeground(new Color(135, 206, 235));
		lblSrcFileSel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		
		lblSelectedInputStatic = new JLabel(" Key Image:");
		lblSelectedInputStatic.setHorizontalTextPosition(SwingConstants.RIGHT);
		lblSelectedInputStatic.setHorizontalAlignment(SwingConstants.RIGHT);
		panelHeader.add(lblSelectedInputStatic, "cell 2 0,alignx right");
		lblSelectedInputStatic.setForeground(new Color(255, 239, 213));
		lblSelectedInputStatic.setFont(new Font("Segoe UI", Font.BOLD, 14));
		
		lblKeyImageSelected = new JLabel("No file selected");
		panelHeader.add(lblKeyImageSelected, "cell 3 0");
		lblKeyImageSelected.setMaximumSize(new Dimension(180, 16));
		lblKeyImageSelected.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		lblKeyImageSelected.setForeground(new Color(135, 206, 235));
		
		lblScreeptHeader = new JLabel("ScreepT - File Crypt & Decrypt");
		lblScreeptHeader.setHorizontalAlignment(SwingConstants.CENTER);
		lblScreeptHeader.setForeground(Color.WHITE);
		lblScreeptHeader.setFont(new Font("Lithos Pro Regular", Font.BOLD, 16));
		lblScreeptHeader.setBackground(new Color(112, 128, 144));
		panelHeader.add(lblScreeptHeader, "cell 4 0,growx,aligny center");
		
		btnCreept = new JButton("Creept");
		btnCreept.setHorizontalTextPosition(SwingConstants.RIGHT);
		btnCreept.setMinimumSize(new Dimension(120, 28));
		btnCreept.setIconTextGap(10);
		btnCreept.setIcon(new ImageIcon(IconsPathConfigurator.ICON_CRYPT));
		panelHeader.add(btnCreept, "cell 5 0,alignx right,aligny center");
		btnCreept.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnCreept.setRequestFocusEnabled(false);
		btnCreept.setForeground(Color.BLACK);
		btnCreept.setBorder(new LineBorder(Color.DARK_GRAY, 3, true));
		btnCreept.setFont(new Font("Lithos Pro Regular", Font.BOLD, 14));
		btnCreept.setBackground(GeneralConfig.ADVICE_FOOTER_OK_COLOR);
		
		btnDeCreept = new JButton("DeCreept");
		btnDeCreept.setHorizontalTextPosition(SwingConstants.RIGHT);
		btnDeCreept.setMinimumSize(new Dimension(120, 28));
		btnDeCreept.setIconTextGap(10);
		btnDeCreept.setIcon(new ImageIcon(IconsPathConfigurator.ICON_DECRYPT));
		panelHeader.add(btnDeCreept, "cell 6 0,alignx right,aligny center");
		btnDeCreept.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnDeCreept.setMargin(new Insets(0, 0, 0, 20));
		btnDeCreept.setRequestFocusEnabled(false);
		btnDeCreept.setForeground(Color.BLACK);
		btnDeCreept.setBorder(new LineBorder(Color.DARK_GRAY, 3, true));
		btnDeCreept.setBackground(GeneralConfig.ADVICE_FOOTER_KO_COLOR);
		btnDeCreept.setFont(new Font("Lithos Pro Regular", Font.BOLD, 14));
		
		panelStatus = new JPanel();
		panelStatus.setMaximumSize(new Dimension(screenSize.width, 30));
		panelStatus.setPreferredSize(new Dimension(10, 20));
		panelStatus.setBorder(new LineBorder(new Color(105, 105, 105), 2));
		panelStatus.setBackground(new Color(112, 128, 144));
		panelMainContainer.add(panelStatus, "cell 0 1 5 1,grow");
		GridBagLayout gbl_panelStatus = new GridBagLayout();
		gbl_panelStatus.columnWidths = new int[]{90, 180, 40, 30, 70, 140, 140, 10, 10, 0};
		gbl_panelStatus.rowHeights = new int[]{32, 0};
		gbl_panelStatus.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_panelStatus.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panelStatus.setLayout(gbl_panelStatus);
		
		lblAesKeyStatic = new JLabel("Password:");
		lblAesKeyStatic.setForeground(new Color(255, 239, 213));
		GridBagConstraints gbc_lblAesKeyStatic = new GridBagConstraints();
		gbc_lblAesKeyStatic.insets = new Insets(0, 0, 0, 5);
		gbc_lblAesKeyStatic.gridx = 0;
		gbc_lblAesKeyStatic.gridy = 0;
		panelStatus.add(lblAesKeyStatic, gbc_lblAesKeyStatic);
		lblAesKeyStatic.setFont(new Font("Segoe UI", Font.BOLD, 14));
		
		AESKEYtext = new JPasswordField();
		AESKEYtext.setPreferredSize(new Dimension(12, 25));
		AESKEYtext.setMinimumSize(new Dimension(12, 25));
		AESKEYtext.setHorizontalAlignment(SwingConstants.CENTER);
		AESKEYtext.setEchoChar(DEFAULT_PSW_CHAR);
		AESKEYtext.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		GridBagConstraints gbc_AESKEYtext = new GridBagConstraints();
		gbc_AESKEYtext.fill = GridBagConstraints.HORIZONTAL;
		gbc_AESKEYtext.insets = new Insets(0, 0, 0, 5);
		gbc_AESKEYtext.gridx = 1;
		gbc_AESKEYtext.gridy = 0;
		panelStatus.add(AESKEYtext, gbc_AESKEYtext);
		AESKEYtext.setColumns(10);
		
		chckbxShowAesKey = new JCheckBox("");
		chckbxShowAesKey.setIcon(new ImageIcon(IconsPathConfigurator.ICON_INVISIBLE));
		chckbxShowAesKey.setSelectedIcon(new ImageIcon(IconsPathConfigurator.ICON_VISIBLE));
		chckbxShowAesKey.setRolloverIcon(chckbxShowAesKey.getIcon());
		chckbxShowAesKey.setOpaque(false);
		chckbxShowAesKey.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		chckbxShowAesKey.setForeground(Color.WHITE);
		chckbxShowAesKey.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		GridBagConstraints gbc_chckbxShowAesKey = new GridBagConstraints();
		gbc_chckbxShowAesKey.anchor = GridBagConstraints.WEST;
		gbc_chckbxShowAesKey.insets = new Insets(0, 0, 0, 5);
		gbc_chckbxShowAesKey.gridx = 2;
		gbc_chckbxShowAesKey.gridy = 0;
		panelStatus.add(chckbxShowAesKey, gbc_chckbxShowAesKey);
		
		iconInfoMode = new JLabel("Mode:");
		iconInfoMode.setIcon(new ImageIcon(IconsPathConfigurator.ICON_GEN_INFO));
		GridBagConstraints gbc_lblInfoMode= new GridBagConstraints();
		gbc_lblInfoMode.insets = new Insets(0, 0, 0, 5);
		gbc_lblInfoMode.gridx = 3;
		gbc_lblInfoMode.gridy = 0;
		panelStatus.add(iconInfoMode, gbc_lblInfoMode);

		lblSelectedModeStatic = new JLabel("Mode:");
		lblSelectedModeStatic.setForeground(new Color(255, 239, 213));
		GridBagConstraints gbc_lblSelectedModeStatic = new GridBagConstraints();
		gbc_lblSelectedModeStatic.insets = new Insets(0, 0, 0, 5);
		gbc_lblSelectedModeStatic.gridx = 4;
		gbc_lblSelectedModeStatic.gridy = 0;
		panelStatus.add(lblSelectedModeStatic, gbc_lblSelectedModeStatic);
		lblSelectedModeStatic.setFont(new Font("Segoe UI", Font.BOLD, 14));
		
		comboModeCrypto = new JComboBox<CryptoMode>();
		comboModeCrypto.setPreferredSize(new Dimension(33, 24));
		comboModeCrypto.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		GridBagConstraints gbc_comboModeCrypto = new GridBagConstraints();
		gbc_comboModeCrypto.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboModeCrypto.insets = new Insets(0, 0, 0, 5);
		gbc_comboModeCrypto.gridx = 5;
		gbc_comboModeCrypto.gridy = 0;
		panelStatus.add(comboModeCrypto, gbc_comboModeCrypto);
		comboModeCrypto.setModel(new DefaultComboBoxModel<CryptoMode>(CryptoMode.values()));
			
		chckbxShuffle = new JCheckBox("Shuffle Mode");
		chckbxShuffle.setIcon(IconsPathConfigurator.IMG_ICON_LIGHT_OFF);
		chckbxShuffle.setDisabledIcon(new ImageIcon(IconsPathConfigurator.ICON_DELETE));
		chckbxShuffle.setSelectedIcon(IconsPathConfigurator.IMG_ICON_LIGHT_ON);
		chckbxShuffle.setRolloverIcon(chckbxShuffle.getIcon());
		chckbxShuffle.setToolTipText(encapsulateInHtml(shuffleTooltip));
		chckbxShuffle.setOpaque(false);
		chckbxShuffle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		chckbxShuffle.setForeground(Color.WHITE);
		
		GridBagConstraints gbc_chckbxShuffle = new GridBagConstraints();
		gbc_chckbxShuffle.insets = new Insets(0, 0, 0, 5);
		gbc_chckbxShuffle.gridx = 6;
		gbc_chckbxShuffle.gridy = 0;
		panelStatus.add(chckbxShuffle, gbc_chckbxShuffle);

		chckbxHashKey = new JCheckBox("Hash key");
		chckbxHashKey.setIcon(IconsPathConfigurator.IMG_ICON_LIGHT_OFF);
		chckbxHashKey.setDisabledIcon(new ImageIcon(IconsPathConfigurator.ICON_DELETE));
		chckbxHashKey.setSelectedIcon(IconsPathConfigurator.IMG_ICON_LIGHT_ON);
		chckbxHashKey.setDisabledSelectedIcon(new ImageIcon(IconsPathConfigurator.ICON_DELETE));
		chckbxHashKey.setRolloverIcon(chckbxHashKey.getIcon());
		chckbxHashKey.setToolTipText(encapsulateInHtml(hashedTooltip));
		chckbxHashKey.setOpaque(false);
		chckbxHashKey.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		chckbxHashKey.setForeground(Color.WHITE);
		
		GridBagConstraints gbc_chckbxHashKey = new GridBagConstraints();
		gbc_chckbxHashKey.insets = new Insets(0, 0, 0, 5);
		gbc_chckbxHashKey.gridx = 7;
		gbc_chckbxHashKey.gridy = 0;
		panelStatus.add(chckbxHashKey, gbc_chckbxHashKey);
		
		labelFlagCodeReady = new JLabel("");
		labelFlagCodeReady.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc_labelFlagCodeReady = new GridBagConstraints();
		gbc_labelFlagCodeReady.fill = GridBagConstraints.VERTICAL;
		gbc_labelFlagCodeReady.gridx = 8;
		gbc_labelFlagCodeReady.gridy = 0;
		panelStatus.add(labelFlagCodeReady, gbc_labelFlagCodeReady);
		labelFlagCodeReady.setBorder(null);
		labelFlagCodeReady.setFont(new Font("SansSerif", Font.BOLD, 14));

		scrollPanelTabImagePreview = new JScrollPane();
		scrollPanelTabImagePreview.setBorder(new LineBorder(new Color(192, 192, 192), 0));
		scrollPanelTabImagePreview.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPanelTabImagePreview.getViewport().setBackground(Color.DARK_GRAY);
		
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBackground(Color.DARK_GRAY);
		tabbedPane.setForeground(Color.LIGHT_GRAY);
		tabbedPane.setOpaque(true);
		tabbedPane.setBorder(new LineBorder(new Color(128, 128, 128)));
		tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);	
		
		leftSplitTopPanel = new JPanel();
		leftSplitTopPanel.setBackground(new Color(40, 40, 40));
		leftSplitTopPanel.setLayout(new MigLayout(
				"fill,insets 0",
				"[5px][100px][100px][100px][grow]",
				"[30px:30px:30px] [100px,grow]"));
		
		// Left panel divided in two vertically (tree keys + infoTabbedPanel) - top component added later
		leftSplitPaneVertical = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		leftSplitPaneVertical.setDividerLocation(leftVerticalDivHeight);
		leftSplitPaneVertical.setBottomComponent(tabbedPane);
		leftSplitPaneVertical.setTopComponent(leftSplitTopPanel);
		leftSplitPaneVertical.setContinuousLayout(true);
		leftSplitPaneVertical.setMaximumSize(maxSplitPaneSize);
		
		// leftSplitPaneVertical<->mainSplitTextAreaNDragger splitPanel
		mainSplitTextAreaMain = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		mainSplitTextAreaMain.setDividerLocation(leftWidth);
		mainSplitTextAreaMain.setLeftComponent(leftSplitPaneVertical);
		mainSplitTextAreaMain.setContinuousLayout(true);
		mainSplitTextAreaMain.setMaximumSize(maxSplitPaneSize);
		
		// textArea<->fileDragDropperTree
		mainSplitTextAreaRight = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		mainSplitTextAreaRight.setDividerLocation(parent.getMainFrame().getWidth()-rightWidth);
		mainSplitTextAreaRight.setLeftComponent(mainSplitTextAreaMain);
		mainSplitTextAreaRight.setContinuousLayout(true);
		mainSplitTextAreaRight.setMaximumSize(maxSplitPaneSize);
		panelMainContainer.add(mainSplitTextAreaRight, "cell 0 2 5 4,grow");
		
		// initialize text areas		
		initializeTextAreas();
		
		// initialize tree and panned events
		configureNstartTree();		

		/** TREE FOR KEY IMAGES **/
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				UIDefaults paneDefaults = new UIDefaults();
				paneDefaults.put("Tree.selectionBackground", Color.BLACK);
				/** TREE TO DRAG AND DROP SOURCE FILES **/
				treeSourcesDragAndDrop.putClientProperty("Nimbus.Overrides", paneDefaults);
				treeSourcesDragAndDrop.putClientProperty("Nimbus.Overrides.InheritDefaults", false);
				tree.setVisibleRowCount(10);
				// set custom graphic renderer to jtree
				// tree.setCellRenderer(new TreeCellRendererDark());
				tree.setBackground(new Color(80,80,80));
				tree.putClientProperty("Nimbus.Overrides", paneDefaults);
				tree.putClientProperty("Nimbus.Overrides.InheritDefaults", false);
				scrollPaneTree.setViewportView(tree);
				tree.addTreeSelectionListener(treeListener);
				new TreeFileMouseListenerCrypter(currentConf, thisPanel);
			}
		});
		
		// function to initialize values
		initialize(currentConfiguration);
				
		// ADD ACTION LISTENERS TO LINK ACTIONS // 
		addHandlers();
		
		// new thread that updates jtree periodically
		GuiUtils.launchThreadSafeSwing(new Runnable() {
			
			@Override
			public void run() {
				while (true) {
					String backupText = textAreaLog.getText();
					Highlighter backH = textAreaLog.getHighlighter();
					textAreaLog.setHighlighter(null);
					TreePath[] backup = tree.getSelectionPaths();
					tree = new JTree(treeModel);
					// refresh tree gui
					refreshJTree();
					tree.setSelectionPaths(backup);
					textAreaLog.setText(backupText);
					try {
						Thread.sleep(1500);
					} catch (InterruptedException e) {
						logger.error(e.toString());
					}
					textAreaLog.setHighlighter(backH);
				}
			}
		});
		
		// new thread keeps updated aeskey check and ReadyToCode label.
		// this thread will check (every second) the value contained in the textfield
		GuiUtils.launchThreadSafeSwing(new Runnable() {
			
			@Override
			public void run() {
				while(MainGUI.mainAppRunning) {
					
					// check the aes key and set jtextField color
					AesCipher.check(AESKEYtext);
					// set the label text according to selected mode
					if(checkIfReadyToCode() && !isCoderUsed()) {
						enableCoderButtons();
					}else {
						disableCoderButtons();
					}
					
					if(!isCoderUsed()) {
						enableSelectionButtons();
					}else {
						disableSelectionButtons();
					}
					
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						logger.error(e.getMessage());
					}

				}
			}
		});
		
		logger.info(this.getClass().getName() + " Started");
		
		GUIStyelSettings.setStyle(currentConfiguration.getGuiOpt().getPreferredStyle(), currentConfiguration.getGuiOpt().nimbusSize);
		SplitPaneUtils.resetAllDIviderColors(this, new Color(25, 25, 25));
		
		/*
		 * KEYBOARD SHORTCUTS - use custom listener
		 */
		shortcutsListener = new CrypterPanelShortcutsListener(thisPanel);
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(shortcutsListener);
		
		refreshRightSplitSize();
	}
	
	public void resizeRightSplitter() {
		leftWidth = mainSplitTextAreaMain.getDividerLocation();
		final int max = mainApplication.getMainFrame().getWidth();
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				rightWidth = (rightWidth < rightWidthMin) ? rightWidthMin : rightWidth;
				mainSplitTextAreaRight.setDividerLocation(max - rightWidth);
				refreshRightSplitSize();
			}
		});
	}
	
	private void addHandlers(){
		
		tabbedPane.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				tabbedPane.setForeground(Color.LIGHT_GRAY);
				tabbedPane.setForegroundAt(tabbedPane.getSelectedIndex(), (Color.WHITE));
			}
		});
		
		mainSplitTextAreaMain.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				resizeRightSplitter();
			}
		});

		mainSplitTextAreaRight.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				refreshRightSplitSize();
			}
		});
		

		// CREEPT ACTION LISTENER
		btnCreept.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				
				try {
					screeptManagerFoF.setHashed(chckbxHashKey.isSelected());
					new Thread(cripterRunner()).start();
				}
				catch(Throwable ex) {
					if(screeptManagerFoF.screepTManager!=null) {
						screeptManagerFoF.screepTManager.disableProgressUpdater();
					}
					dialogHelper.showTypedOKDialog("OOOps, an error occurred, see log for details :(", "Generic Error", JOptionPane.ERROR_MESSAGE);
					logger.error("Exception happened during creept", ex);
				}
				
			}
		});
		
		
		// DECREEPT ACTION LISTENER
		btnDeCreept.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				try {
					screeptManagerFoF.setHashed(chckbxHashKey.isSelected());
					new Thread(decripterRunner()).start();
				}catch(Throwable ex) {
					if(screeptManagerFoF.screepTManager!=null) {
						screeptManagerFoF.screepTManager.disableProgressUpdater();
					}
					dialogHelper.showTypedOKDialog("OOOps, an error occurred, see log for details :(", "Generic Error", JOptionPane.ERROR_MESSAGE);
					logger.error("Exception happened during decreept!", ex);
				}
								
			}
		});
		
		// CHANGED MODE LISTENER
		comboModeCrypto.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				CryptoMode modality = (CryptoMode)comboModeCrypto.getSelectedItem();
				
				if (modality.isAES()){
					chckbxHashKey.setEnabled(true);
					chckbxHashKey.setForeground(Color.white);
				} else {
					chckbxHashKey.setEnabled(false);
					chckbxHashKey.setForeground(Color.lightGray);
				}
				
				// update configuration reference
				currentConf.getAesOpt().setFlagAes(modality.AES);
				currentConf.getSteganOpt().setFlagSteg(modality.STEG);
				mode = modality;
				updateModeInfoTooltip();
			}
		});
		comboModeCrypto.setSelectedItem(CryptoMode.ONLY_AES);
		
		// ULTRA-SECURE - CHECKBOX LISTENER
		chckbxShuffle.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				currentConf.getSteganOpt().setUltraSecure(chckbxShuffle.isSelected() && chckbxShuffle.isEnabled());
				updateShuffleTooltip();				
			}
		});

		// HASH KEY - CHECKBOX LISTENER
		chckbxHashKey.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				currentConf.getAesOpt().setHashKey(chckbxHashKey.isSelected() && chckbxHashKey.isEnabled());
				updateHashedTooltip();				
			}
		});
		
		// SHOW/HIDE PASSWORD
		chckbxShowAesKey.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (chckbxShowAesKey.isSelected()) {
					AESKEYtext.setEchoChar((char) 0);
				}else {
					AESKEYtext.setEchoChar(DEFAULT_PSW_CHAR);
				}				
			}
		});
		
		// SELECT SINGLE FILE LISTENER
		btnSelectSourceFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				// select input file or directory with fileChooser
				File selected = MainGuiGeneralAppStarter.fileDialogHelper.fileAndDirRead(thisPanel);
				addNewSourceFile(selected);
			}
		});
		
		// SELECT DISPOSABLE KEY IMAGE LISTENER
		btnDispoKeyImg.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				File dispoImgFile;
				try {
					dispoImgFile = MainGuiGeneralAppStarter.fileDialogHelper.fileImgRead(thisPanel, "png");
					if (dispoImgFile != null) {
						useAsKeyImage(dispoImgFile);
						loadInfo(dispoImgFile);
					}
				} catch (Exception e1) {
					showMessageImageNotSupported();
				}
			}
		});

		// CREATE NEW KEY IMAGE LISTENER
		btnGenerateKeyImg.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				mainApplication.getMntmgenerateKeyImageSub().doClick();
			}
		});
		
		// CREATE NEW PASSWORD LISTENER
		btnGenerateKeyPsw.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				mainApplication.windowCreator.newGeneratePassordFrame(AesCipher.limit);
			}
		});
	}
	
	private void refreshRightSplitSize() {
		int max = mainApplication.getMainFrame().getWidth();
		rightWidth = max - mainSplitTextAreaRight.getDividerLocation();
	}
	
	
////	/////////////////////////////////
		// USEFUL METHODS //
		////////////////////
	
	public boolean isFileAlreadyLoadedInRoot(File toCheck) {
		return panelVDragNDropPane.isFileAlreadyPresent(toCheck);
	}
	
	private Runnable decripterRunner() {
		return new Runnable() {

			@Override
			public void run() {

				File destinationDir = null;
				// retrieve source file array from dragNDrop panel
				srcFiles = panelVDragNDropPane.getTreeNodes();

				// ask user if he wants to delete decripted after closing ScrYpT
				int deleteAfterClose = JOptionPane.showConfirmDialog(parentFrame,
						"Delete decripted file(s) after exiting ScrypT? (TEMP folder will be created on Desktop)", "Delete decripted when SW will be closed",
						JOptionPane.YES_NO_OPTION);

				if (deleteAfterClose == JOptionPane.YES_OPTION) {
					textAreaMain.append("Decripted files will be exported to temporary folder on user desktop folder\n");
					// if temporary mode create temp folder ScrYpT on desktop
					destinationDir = new File(GeneralConfig.TEMP_DECRIPTED_ROOT_PATH);

					if (fileMonitor == null) {
						flagCreateNewMonitor = true;
					} else {
						flagCreateNewMonitor = false;
					}
				} else {
					textAreaMain.append("Waiting input from user...\n");
					// get output asking to user
					destinationDir = MainGuiGeneralAppStarter.fileDialogHelper.directoWrite(parentFrame);
					textAreaMain.append("Chosen input: " + destinationDir + "\n\n");
				}

				CrypterResultDTO result = new CrypterResultDTO();

				// first recognize selected combo Mode selected
				mode = (CryptoMode) comboModeCrypto.getSelectedItem();
				generalProgress = null;

				if (destinationDir != null) {

					generalProgress = new UpdateProgressPanelCrypter(true);
					generalProgress.setLocationRelativeTo(parentFrame);
					screeptManagerFoF.setGeneralProgress(generalProgress);
					generalProgress.toFront();

					Date now = new Date();
					String date = dateFormatter.format(now);
					destinationDir = new File(destinationDir.getAbsolutePath() + "/" + date);

					if (!destinationDir.exists()) {
						destinationDir.mkdir();
					}
				}

				StringBuilder builder = new StringBuilder();
				int nFilesSource = srcFiles.length;
				int nProgrFile = 0;

				for (File currentFile : srcFiles) {

					if (currentFile != null && currentFile.exists() && destinationDir != null) {

						builder.append("Starting conversion of file ").append(nProgrFile + 1).append("/").append(nFilesSource).append("[")
								.append(currentFile.getName()).append("]");
						int progressPerc = nProgrFile * 100 / nFilesSource;

						if (nProgrFile == 0)
							progressPerc = 1;

						if (srcFiles.length > 1) {
							generalProgress.update(progressPerc - 1, builder.toString(), true);
						} else {
							generalProgress.update(50, builder.toString(), true);
							generalProgress.getGeneralBar().setIndeterminate(true);
						}

						textAreaLog.append(builder.toString());
						nProgrFile++;

						boolean shuffle = chckbxShuffle.isSelected() && chckbxShuffle.isEnabled();
						try {
							result = screeptManagerFoF.decrYpt(mode, currentFile, destinationDir, AESKEYtext.getPassword(), srcKeyImageFile, shuffle);
						} catch (Exception e) {
							result.resultOK = false;
						}
					}

					textAreaMain.append(screeptManagerFoF.getOutputStats());
				}

				if (generalProgress != null) {
					generalProgress.update(100, null, true);
				}
				// INFORM USER ABOUT ESIT OF DECRYPT OPERATION
				if (result.resultOK) {
					String msg = "Operation completed succesfully!\nDo you want to empty source file pool?";
					if (dialogHelper.yesOrNo(msg, "SUCCESS!")) {
						removeTreeFiles();
					}
					
					GuiUtils.openInFileSystem(destinationDir);

					textAreaLog.append("\n" + msg);
					srcFile = null;

				} else {
					if(result.interrupted) {
						String msg = "Process interrupted.";
						dialogHelper.info(msg, null);
						textAreaLog.append("\n" + msg);
						
						askUserIfWantsToDelete(destinationDir);
						
					} else if (destinationDir != null) {
						String msg = "Something went wrong :(";
						dialogHelper.error(msg, null);
						textAreaLog.append("\n" + msg);
						
						askUserIfWantsToDelete(destinationDir);
					} else {
						dialogHelper.warn("No directory selected, export aborted!", "No directory selected");
					}

				}

				textAreaMain.append(screeptManagerFoF.getOutputStats());
				tabbedPane.setSelectedIndex(0);

				if (flagCreateNewMonitor && fileMonitor == null) {
					fileMonitor = new FileChangeMonitor(destinationDir, null);
				}

				textAreaMain.append("\n\n => Completed !\n\n #####################################################");

				runRunnableImpExp(result.resultOK, destinationDir);
			}
		};
	}
	
	private Runnable cripterRunner() {
		return new Runnable() {
			
			@Override
			public void run() {
				textAreaMain.setText("Waiting input from user...\n");
				
				// get output asking to user
				File destinationDir = MainGuiGeneralAppStarter.fileDialogHelper.directoWrite(parentFrame);
				
				textAreaMain.append("Chosen input: " + destinationDir + "\n\n");
				
				// retrieve source file array from dragNDrop panel
				srcFiles = panelVDragNDropPane.getTreeNodes();
				
				CrypterResultDTO result = new CrypterResultDTO();
				
				// first recognize selected combo Mode selected
				mode = (CryptoMode)comboModeCrypto.getSelectedItem();
				
				generalProgress = null;
				
				if (destinationDir!= null) {
					
					generalProgress = new UpdateProgressPanelCrypter(true);
					generalProgress.setLocationRelativeTo(parentFrame);
					screeptManagerFoF.setGeneralProgress(generalProgress);
					generalProgress.toFront();

					Date now = new Date();
					String date = dateFormatter.format(now);
					destinationDir = new File(destinationDir.getAbsolutePath() + "/" + date);
					
					if (!destinationDir.exists()) {
						destinationDir.mkdir();
					}
				}
				
				StringBuilder builder = new StringBuilder();
				int nFilesSource = srcFiles.length;
				int nProgrFile = 0;
				
				for (File currentFile : srcFiles) {
					
					if (currentFile != null && currentFile.exists() && destinationDir != null) {
					
						builder.append("Starting conversion of file ").append(nProgrFile+1).append("/").append(nFilesSource)
						.append("[").append(currentFile.getName()).append("]");
						int progressPerc = nProgrFile*100/nFilesSource;
						if(nProgrFile == 0)
							progressPerc = 1;

						if (srcFiles.length > 1) {
							generalProgress.update(progressPerc - 1, builder.toString(), true);
						} else {
							generalProgress.update(50, builder.toString(), true);
							generalProgress.getGeneralBar().setIndeterminate(true);
						}
						
						textAreaLog.append(builder.toString());
						nProgrFile ++;
						
						boolean shuffle = chckbxShuffle.isSelected() && chckbxShuffle.isEnabled();
						try {
							result = screeptManagerFoF.crYpt(mode, currentFile, destinationDir, AESKEYtext.getPassword(), srcKeyImageFile, shuffle);
						} catch (Exception e) {
							result.resultOK = false;
						}
					} 
					textAreaMain.append(screeptManagerFoF.getOutputStats());
				}
				
				if (generalProgress != null) {
					generalProgress.update(100, null, true);
				}
				// INFORM USER ABOUT ESIT OF CRYPT OPERATION
				if(result.resultOK) {
					String msg = "Operation completed succesfully!\nDo you want to empty source file pool?";
					if(JOptionPane.showConfirmDialog(thisPanel, msg,"SUCCESS!", JOptionPane.INFORMATION_MESSAGE)==JOptionPane.OK_OPTION) {
						removeTreeFiles();
					}
					
					GuiUtils.openInFileSystem(destinationDir);
					
					logger.info(msg);
					textAreaLog.append("\n" + msg);
					
					srcFile = null;
					
				}else {
					if(result.interrupted) {
						String msg = "Process interrupted.";
						dialogHelper.info(msg, "Process Interrupted");
						textAreaLog.append("\n" + msg);
						
						askUserIfWantsToDelete(destinationDir);
						
					} else if(destinationDir != null) {
						String msg = "Something went wrong :(";
						dialogHelper.error(msg,"An error occurred");
						logger.error(msg);
						textAreaLog.append("\n" + msg);
						
						askUserIfWantsToDelete(destinationDir);
					}
					else{
						dialogHelper.warn("No directory selected, export aborted!", "No directory selected");
					}
				}
				
				textAreaMain.append(screeptManagerFoF.getOutputStats());
				tabbedPane.setSelectedIndex(0);
				
				textAreaMain.append("\n\n => Completed !\n\n #####################################################");
				
				runRunnableImpExp(result.resultOK, destinationDir);
			}
		};
	}
	
	private void askUserIfWantsToDelete(File resultDirectory) {
		if(resultDirectory != null && resultDirectory.isDirectory() && resultDirectory.exists()) {
			logger.debug("Asking to user if he wants to delete directory: " + resultDirectory);
			if(dialogHelper.showYNDialog("Do you want to delete " + resultDirectory + "'s content?", "Delete content?", JOptionPane.INFORMATION_MESSAGE)) {
				boolean result = FileStuff.deleteDirContentRecursive(resultDirectory);
				
				if(result) {
					dialogHelper.showTypedOKDialog(resultDirectory + "'s content deleted.", "Directory cleaned", JOptionPane.INFORMATION_MESSAGE);
				} else {
					dialogHelper.showTypedOKDialog("Cannot delete all content in " + resultDirectory, "Cannot clean directory.", JOptionPane.WARNING_MESSAGE);
					GuiUtils.openInFileSystem(resultDirectory);
				}
			}
		}
	}
	
	private void runRunnableImpExp(boolean resultOK, File destinationDir) {
		if(resultOK) {
			setCompletedDirectoryImportExport(destinationDir);
			if(getCompletedDirectoryImportExport() != null && getAfterCompleteRunnable() != null) {
				SwingUtilities.invokeLater(getAfterCompleteRunnable());
			}
		}
	}

	private void initializeTextAreas() {
		scrollPaneLog = new JScrollPane();
		scrollPaneLog.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		tabbedPane.addTab("Screept Log", null, scrollPaneLog, null);
		tabbedPane.setBackgroundAt(0, new Color(216, 191, 216));
		scrollPaneLog.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		
		textAreaLog = new JTextArea();
		textAreaLog.setMinimumSize(new Dimension(50, 28));
		textAreaLog.setBorder(new LineBorder(new Color(0, 0, 0)));
		textAreaLog.setWrapStyleWord(true);
		textAreaLog.setRequestFocusEnabled(false);
		textAreaLog.setLineWrap(true);
		textAreaLog.setEditable(false);
		textAreaLog.setBackground(new Color(20,20,20));
		textAreaLog.setForeground(new Color(210,210,210));
		scrollPaneLog.setViewportView(textAreaLog);
		textAreaLog.setMargin(new Insets(10, 10, 10, 10));
		textAreaLog.revalidate();
		textAreaLog.repaint();
		textAreaLog.setText("### WELCOME TO ScreepT :) ###\r\n\r\n Are you ready to crypt/decrypt your files?\r\n\r\n Nobody knows how to crypt them better than me ;)"); 
		
		panelContainerKey = new JPanel();
		panelContainerKey.setBackground(Color.DARK_GRAY);
		panelContainerKey.setBorder(new LineBorder(new Color(0, 0, 0), 0));
		scrollPanelTabImagePreview.setViewportView(panelContainerKey);
		tabbedPane.addTab("Img Preview", null, scrollPanelTabImagePreview, null);
		panelContainerKey.setLayout(new MigLayout(
				"", 
				"[100,center,grow]", 
				"[:20:20][200px,grow,top]"));
		
		lblDisplayedKey = new JLabel("to see image: Right click on img -> Load Image and Details");
		lblDisplayedKey.setForeground(Color.YELLOW);
		lblDisplayedKey.setHorizontalAlignment(SwingConstants.RIGHT);
		lblDisplayedKey.setFont(new Font("SansSerif", Font.BOLD, 12));
		panelContainerKey.add(lblDisplayedKey, "cell 0 0,alignx center,aligny top");

		panelKeyImgDisp = new JPanel();
		panelKeyImgDisp.setBorder(new LineBorder(Color.LIGHT_GRAY));
		panelKeyImgDisp.setMinimumSize(new Dimension(100, 200));
		panelKeyImgDisp.setBounds(new Rectangle(0, 0, 400, 0));
		panelKeyImgDisp.setBackground(Color.BLACK);
		panelContainerKey.add(panelKeyImgDisp, "cell 0 1,grow");
		panelKeyImgDisp.setLayout(new GridLayout(1, 0, 0, 0));
		tabbedPane.setBackgroundAt(1, new Color(169, 169, 169));
		
		textAreaMain = new JTextArea();
		textAreaMain.setWrapStyleWord(true);
		textAreaMain.setBackground(new Color(30,30,30));
		textAreaMain.setForeground(new Color(230,230,230));
		textAreaMain.setRequestFocusEnabled(true);
		textAreaMain.setEditable(true);
		textAreaMain.setEnabled(true);
		textAreaMain.setMargin(new Insets(10, 15, 10, 10));
		textAreaMain.setMaximumSize(maxBodySize);

		scrollPaneText = new JScrollPane();
		scrollPaneText.setViewportView(textAreaMain);
		scrollPaneText.setMaximumSize(maxBodySize);
		scrollPaneText.setWheelScrollingEnabled(true);
		scrollPaneText.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPaneText.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPaneText.setBorder(new LineBorder(new Color(128, 128, 128), 2));
		mainSplitTextAreaMain.setRightComponent(scrollPaneText);
//		panelMainContainer.add(scrollPaneText, "cell 2 2 1 4,grow");
		
		textAreaMain.setText("   ###  WELCOME TO ScreepT :) ###\r\n\r\n Are you ready to crypt/decrypt your files? I will be your best friend... and attackers worst nightmare ;)");
		StringBuilder builder = new StringBuilder();
		builder.append("\n\n 1) load source files or drop them on right panel with file tree")
			   .append("\n\n 2) choose a modality between only aes, only steg or complete using the selector on top-left bar")
			   .append("\n\n 2a) if complete or steg mode is selected, you have to import a pre-loaded Image fom dictionary or a disposable, or either generate a new one.")
		 	   .append("\n\n 3) enter a password in the given field")
		 	   .append("\n\n 4) press creept or drecreept to start process :)")
		       .append("\n\n PS: Tooltips are your friends, just hover on buttons to know more about them ;)");
		
		textAreaMain.append("\n\n" + builder.toString());
		textAreaMain.revalidate();
		textAreaMain.repaint();

	}
	
	public void configureNstartTree() {
		
		/** TREE FOR KEY IMAGES **/
		
		treeModel = new FileTreeModel(KEY_DICTIONARY_FILE);
		tree= new JTree(treeModel);
		tree.setBorder(null);
		tree.setRequestFocusEnabled(false);
		tree.setFont(new Font("SansSerif", Font.BOLD, 14));
		treeListener = new TreeSelectionListener() {
			
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				try {
					String msg;
					File thisFile = (File)tree.getLastSelectedPathComponent();
					if (thisFile != null) {
						if (thisFile.isDirectory()) {
							msg = "\n### Directory selected ### \n\n => " + thisFile.getName();
							textAreaLog.append("\n" + msg);
						} else {
							if (FilenameUtils.getExtension(thisFile.getAbsolutePath()).equals("png")) {
								msg = " .png Key recognized ->" + thisFile.getName() + "\n\n ## SIZE: " + FileStuff.coolFileSize(thisFile) + "\n\n ## Path: "
										+ thisFile.getAbsolutePath();
								textAreaLog.append("\n" + msg);
							} else {
								msg = "File not recognized as Key: \n\n => " + thisFile.getName();
								textAreaLog.append("\n" + msg);
							}
						} 
					}
				} catch (Exception e1) {
					logger.warn("Error in value changed listener: ", e1);
				}
				new TreeFileMouseListenerCrypter(currentConf, thisPanel);			
			}
		};
		
		btnDispoKeyImg = new JButton("One-Use");
		btnDispoKeyImg.setToolTipText("Select a disposable image that will be forgotten after encryption/decryption");
		btnDispoKeyImg.setOpaque(false);
		btnDispoKeyImg.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnDispoKeyImg.setIcon(new ImageIcon(IconsPathConfigurator.ICON_KEY_DICT));
		btnDispoKeyImg.setRequestFocusEnabled(false);
		btnDispoKeyImg.setBorder(null);
		btnDispoKeyImg.setForeground(new Color(240,240,240));
		btnDispoKeyImg.setFont(new Font("Segoe UI", Font.BOLD, 12));
		btnDispoKeyImg.setBackground(GeneralConfig.BUTTON_BCKG_COLOR_TXT_EDITOR_DARKER);
		leftSplitTopPanel.add(btnDispoKeyImg, "cell 1 0 1 1");

		btnGenerateKeyImg = new JButton("Img Keygen");
		btnGenerateKeyImg.setToolTipText("Generate a new random key image");
		btnGenerateKeyImg.setOpaque(false);
		btnGenerateKeyImg.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnGenerateKeyImg.setIcon(new ImageIcon(IconsPathConfigurator.ICON_ADD));
		btnGenerateKeyImg.setRequestFocusEnabled(false);
		btnGenerateKeyImg.setBorder(null);
		btnGenerateKeyImg.setForeground(new Color(240,240,240));
		btnGenerateKeyImg.setFont(new Font("Segoe UI", Font.BOLD, 12));
		btnGenerateKeyImg.setBackground(GeneralConfig.BUTTON_BCKG_COLOR_TXT_EDITOR_DARKER);
		leftSplitTopPanel.add(btnGenerateKeyImg, "cell 2 0 1 1");

		btnGenerateKeyPsw = new JButton("Psw Keygen");
		btnGenerateKeyPsw.setToolTipText("Generate a new random password with fixed length");
		btnGenerateKeyPsw.setOpaque(false);
		btnGenerateKeyPsw.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnGenerateKeyPsw.setIcon(new ImageIcon(IconsPathConfigurator.ICON_ADD));
		btnGenerateKeyPsw.setRequestFocusEnabled(false);
		btnGenerateKeyPsw.setBorder(null);
		btnGenerateKeyPsw.setForeground(new Color(240,240,240));
		btnGenerateKeyPsw.setFont(new Font("Segoe UI", Font.BOLD, 12));
		btnGenerateKeyPsw.setBackground(GeneralConfig.BUTTON_BCKG_COLOR_TXT_EDITOR_DARKER);
		leftSplitTopPanel.add(btnGenerateKeyPsw, "cell 3 0 1 1");
		
		// initialize tree 
		scrollPaneTree = new JScrollPane();
		scrollPaneTree.setBackground(new Color(80,80,80));
		scrollPaneTree.setBorder(new LineBorder(new Color(153, 153, 153)));
		leftSplitTopPanel.add(scrollPaneTree, "cell 0 1 5 1,grow");
		
		/** TREE TO DRAG AND DROP SOURCE FILES **/
		
		scrollPaneDragNDropSource = new JScrollPane();
		scrollPaneDragNDropSource.setBackground(Color.LIGHT_GRAY);
		scrollPaneDragNDropSource.setBorder(new LineBorder(new Color(119, 136, 153), 3, true));
		scrollPaneDragNDropSource.setMaximumSize(maxBodySize);
		mainSplitTextAreaRight.setRightComponent(scrollPaneDragNDropSource);
//		panelMainContainer.add(scrollPaneDragNDropSource, "cell 3 2 2 4,grow");
		
		panelBackgroundDragSources = new JPanel();
		panelBackgroundDragSources.setBackground(new Color(220, 220, 220));
		panelBackgroundDragSources.setMaximumSize(maxBodySize);
		scrollPaneDragNDropSource.setViewportView(panelBackgroundDragSources);
		panelBackgroundDragSources.setLayout(new MigLayout(
				"fill, insets 2",
				"[grow][40px:50px:350px,grow]", 
				"[50px][][30px][300px,grow][150px,grow]"));
		
		lblSelectedFilesDragTitle = new JLabel("Selected Files Pool");
		lblSelectedFilesDragTitle.setHorizontalTextPosition(SwingConstants.CENTER);
		lblSelectedFilesDragTitle.setBorder(null);
		lblSelectedFilesDragTitle.setHorizontalAlignment(SwingConstants.CENTER);
		lblSelectedFilesDragTitle.setForeground(Color.BLACK);
		lblSelectedFilesDragTitle.setFont(new Font("Lithos Pro Regular", Font.BOLD, 22));
		lblSelectedFilesDragTitle.setBackground(new Color(112, 128, 144));
		panelBackgroundDragSources.add(lblSelectedFilesDragTitle, "cell 0 0 2 1,growx");
		
		separatorDragNDropTitle = new JSeparator();
		panelBackgroundDragSources.add(separatorDragNDropTitle, "cell 0 1 2 1,growx");
		
		btnSelectSourceFile = new JButton("Add Source File or Folder");
		btnSelectSourceFile.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		panelBackgroundDragSources.add(btnSelectSourceFile, "cell 0 2 2 1,grow");
		btnSelectSourceFile.setRequestFocusEnabled(false);
		btnSelectSourceFile.setBorder(new LineBorder(new Color(0, 0, 0), 3));
		btnSelectSourceFile.setForeground(SystemColor.text);
		btnSelectSourceFile.setFont(new Font("Lithos Pro Regular", Font.PLAIN, 15));
		btnSelectSourceFile.setBackground(Color.DARK_GRAY);
		
		treeSourcesDragAndDrop = new JTree();
		treeSourcesDragAndDrop.setBackground(new Color(220, 220, 220));
		treeSourcesDragAndDrop.setBorder(BorderFactory.createEmptyBorder());
		JScrollPane treeContainer = new JScrollPane();
		treeContainer.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));
		treeContainer.setViewportView(treeSourcesDragAndDrop);
		panelBackgroundDragSources.add(treeContainer, "cell 0 3 2 1,grow");
		
		panelVDragNDropPane = new VirtualFileDropPane(treeSourcesDragAndDrop, scrollPaneDragNDropSource);
		panelVDragNDropPane.setPreferredSize(new Dimension(250, 250));
		panelVDragNDropPane.setMaximumSize(new Dimension(maxBodySize.width, 350));
		panelVDragNDropPane.message.setText("Drag your files here!");
		panelVDragNDropPane.message.setFont(new Font("SansSerif", Font.BOLD, 16));
		panelVDragNDropPane.setBorder(new LineBorder(new Color(0, 0, 0), 2, true));
		panelVDragNDropPane.setBackground(Color.BLACK);
		panelBackgroundDragSources.add(panelVDragNDropPane, "cell 0 4 2 1,grow");
	}
	
	private void initialize(INItializer config) {
		currentConf = config;		
		AESKEYtext.setText("");
		CryptoMode mode = CryptoMode.getModeFromBoolean(currentConf.getAesOpt().isFlagAes(), currentConf.getSteganOpt().isFlagSteg());
		comboModeCrypto.setSelectedItem(mode);
		chckbxShuffle.setSelected(currentConf.getSteganOpt().isUltraSecure());
		chckbxHashKey.setSelected(currentConf.getAesOpt().isHashKey());
		
		updateHashedTooltip();
		updateShuffleTooltip();
		
		initGuiFromConfig();
	}
	
	private synchronized boolean checkIfReadyToCode() {
		CryptoMode selectedMode = (CryptoMode)comboModeCrypto.getSelectedItem();
		
		// check the operation requested according to combo box value     
		//(see enum custom type)
		boolean ready = true;
		String checkEsit = "";
		int selectedFilesN = panelVDragNDropPane.getNumFirstLevelNodes(); 
		
		if(selectedFilesN == 0) {
			// MISSING source file
			lblSrcFileSel.setText("No file selected");
			labelFlagCodeReady.setForeground(new Color(111, 127, 143));
			checkEsit += " - Source File missing - ";
			ready = false;
		}else {
			lblSrcFileSel.setText(selectedFilesN + " selected");
		}
		
		if(srcKeyImageFile==null && selectedMode.isSTEG()) {
			// INVALID FILE FORMAT
			checkEsit += " - Missing IMGkey - ";
			ready = false;
		}
		
		if(AESKEYtext.getPassword().length == 0 && selectedMode.isAES()) {
			// MISSING AES KEY
			checkEsit += " - missing AES key - ";
			ready = false;
		}
		
		if(ready) {
			labelFlagCodeReady.setForeground(Color.GREEN);
			checkEsit += " - ScreepT is Ready to Crypt/Decrypt :) - ";
		}else {
			labelFlagCodeReady.setForeground(Color.ORANGE);
		}
		
		// update label on GUI
		
		// REFRESH ESIT

		labelFlagCodeReady.setText(checkEsit);
		if(!labelFlagCodeReady.isVisible()) {
			labelFlagCodeReady.setVisible(true);
		}
		
		labelFlagCodeReady.revalidate();
		labelFlagCodeReady.repaint();
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				btnCreept.setToolTipText(btnCreept.isEnabled()? "Ready to encrypt files!" : labelFlagCodeReady.getText());				btnDeCreept.setToolTipText(btnDeCreept.isEnabled()? "Ready to decrypt files!" : labelFlagCodeReady.getText());			}
		});
		return ready;
	}
	
	private void refreshJTree() {

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				UIDefaults paneDefaults = new UIDefaults();
				paneDefaults.put("Tree.selectionBackground", Color.BLACK);
				tree.putClientProperty("Nimbus.Overrides", paneDefaults);
				tree.putClientProperty("Nimbus.Overrides.InheritDefaults", false);
				scrollPaneTree.setViewportView(tree);
				tree.setBackground(Color.GRAY);
				tree.setVisibleRowCount(10);
				tree.addTreeSelectionListener(treeListener);
				tree.revalidate();
				tree.repaint();
			}
		});
	}
	
	private void removeTreeFiles() {
		panelVDragNDropPane.removeAll();
		srcFiles = null; // will be reinitialized at each round of cript/decript
	}

///////////////////////////////////////////////////////// GUI-FILE SELECTOR METHODS /////////////////////////////////////////////////////////	
	private void useAsKeyImage(File thisFile) {
		
    	// if a file is selected
		if (thisFile.exists()) {
			try {
				if(FilenameUtils.getExtension(thisFile.getAbsolutePath()).equalsIgnoreCase("png") && ImageIO.read(thisFile)!=null) {
					textAreaLog.append("\n" + "");
					String msg = " .png Key recognized ->" + thisFile.getName()+"\n\n ## SIZE: "+FileStuff.coolFileSize(thisFile)+"\n\n ## Path: "+thisFile.getAbsolutePath();
					logger.info(msg);
					textAreaLog.append("\n" + msg);	
					
					String newdisplayed = HandleString.fitToMaxLentgh(thisFile.getName(), labelLengthLimit);
					
					lblKeyImageSelected.setForeground(SystemColor.GREEN);
					lblKeyImageSelected.setText(newdisplayed);
					
					srcKeyImageFile = thisFile;
					JOptionPane.showMessageDialog(thisPanel, "Key Image file correctly loaded and selected :)","Key Image Selected :)", JOptionPane.INFORMATION_MESSAGE);
				}else {
					String msg = " File not recognized as Key: \n\n => "+thisFile.getName();
					textAreaLog.append("\n" + msg);
					JOptionPane.showMessageDialog(thisPanel, "Key Image file not recognized :(","Key Image not recognized :(", JOptionPane.ERROR_MESSAGE);
				}
			} catch (IOException e) {
				String msg = "Some Error Occurred :(";
				textAreaLog.append("\n" + msg);
				JOptionPane.showMessageDialog(thisPanel, msg, msg, JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	private void loadInfo(File thisFile) {

		try {
			BufferedImage img = ImageIO.read(thisFile);
			// check if file is an image or not
			if(img == null ) {
				throw new IOException();
			}
			
			// read image from file
			BufferedImage image = ImageIO.read(thisFile);
			Image scaledImage = ImageGeneralWorker.scaleImage(image, image.getType(), panelKeyImgDisp.getWidth(), panelKeyImgDisp.getHeight());
		
			refreshImage(scaledImage);
			
			if (BufferedImageType.checkIfAdmittedType(image.getType())) {
				
				String msg = "\n\n### Image values ###\n\n Width: " + img.getWidth() + " Px\n Height: "
						+ img.getHeight() + " Px\n\n=> File Size: " + FileStuff.coolFileSize(thisFile)
						+ "\n\n => ImgType: " + img.toString();
				textAreaLog.append("\n" + msg);
				logger.info(msg);
				
				String newdisplayed = HandleString.fitToMaxLentgh(thisFile.getName(), labelLengthLimit);

				lblDisplayedKey.setText("Displayed: " + newdisplayed);
			}else {
				
				String msg = "Image type not supported by cripter: ";
				textAreaLog.append("\n" + msg);
				logger.info(msg);
				
				String newdisplayed = HandleString.fitToMaxLentgh(thisFile.getName(), labelLengthLimit);

				lblDisplayedKey.setText("Displayed: " + newdisplayed);
				srcKeyImageFile = null;
				lblKeyImageSelected.setForeground(SystemColor.RED);
				lblKeyImageSelected.setText("Not supported: choose another or convert");
				JOptionPane.showMessageDialog(thisPanel, "Image not supported. Please convert it with converter png uncompressed, or generate one!", "UNSUPPORTED IMAGE", JOptionPane.ERROR_MESSAGE);
			}
			
			
			
		} catch (IOException e1) {
			String msg = "Error reading the image, something went wrong :(";
			logger.debug(msg);
			textAreaLog.append("\n" + msg);
			if (this.srcKeyImageFile == null) {
				lblKeyImageSelected.setText(msg);
				panelKeyImgDisp.removeAll();
				JLabel errorMsg = new JLabel("Error opening file :(");
				errorMsg.setForeground(Color.white);
				errorMsg.setHorizontalAlignment(JLabel.CENTER);
				errorMsg.setVerticalAlignment(JLabel.CENTER);
				panelKeyImgDisp.add(errorMsg);
				panelKeyImgDisp.revalidate();
				panelKeyImgDisp.repaint();
			}
			JOptionPane.showMessageDialog(thisPanel, "OOOps, IO Error occurred :(", "IO error", JOptionPane.ERROR_MESSAGE);
		}
    }

	public void addNewSourceFile(File selected) {
		logger.debug("Adding file to selection...");
		if (selected!=null && selected.exists() && !isFileAlreadyLoadedInRoot(selected)) {
			srcFile = selected;
			panelVDragNDropPane.addFileToTree(selected);
			String newdisplayed = HandleString.fitToMaxLentgh(selected.getName(), labelLengthLimit);
			// update gui
			panelVDragNDropPane.message.setText(newdisplayed + " added to list!");
			
			appendMessageTxtAreas("Source File Loaded: "+srcFile.getAbsolutePath());
			dialogHelper.showTypedOKDialog("Source file correctly loaded", "File Loaded :)", JOptionPane.INFORMATION_MESSAGE);
			
			logger.debug("File :" + selected.getName() + " correctly loaded");
		} else {
			
			dialogHelper.showTypedOKDialog("File was already loaded in source pool", "File already Loaded :)", JOptionPane.INFORMATION_MESSAGE);
			logger.debug("File load skipped: already loaded!");
		}
	}
	
	/**
	 * This method is used only locally to refresh image with the given one
	 */
	private void refreshImage(Image image) {
		
		JLabel picLabel = new JLabel(new ImageIcon(image));
		panelKeyImgDisp.removeAll();
		panelKeyImgDisp.add(picLabel);
		picLabel.setHorizontalAlignment(JLabel.CENTER);
		picLabel.setVerticalAlignment(JLabel.CENTER);
		panelKeyImgDisp.revalidate(); 
		panelKeyImgDisp.repaint();	
	}
	
	private void disableCoderButtons() {
		btnCreept.setEnabled(false);
		btnDeCreept.setEnabled(false);
	}
	
	private void enableCoderButtons() {
		btnCreept.setEnabled(true);
		btnDeCreept.setEnabled(true);
	}
	
	private void disableSelectionButtons() {
		btnDispoKeyImg.setEnabled(false);
		btnGenerateKeyImg.setEnabled(false);
		btnSelectSourceFile.setEnabled(false);
	}
	
	private void enableSelectionButtons() {
		btnDispoKeyImg.setEnabled(true);
		btnGenerateKeyImg.setEnabled(true);
		btnSelectSourceFile.setEnabled(true);
	}
	
	private void appendMessageTxtAreas(String msg) {
		String out = "\n\n## "+msg+" ##\n\n";
		textAreaLog.setSelectionStart(textAreaLog.getText().length()-1);
		textAreaMain.setSelectionStart(textAreaMain.getText().length()-1);
		
		textAreaLog.append(out);
		textAreaMain.append(out);
		tabbedPane.setSelectedIndex(0);
	}
	
	public void initGuiFromConfig() {
		GUIStyelSettings.applyNimbusSize(currentConf.getGuiOpt().nimbusSize);
		GUIStyelSettings.updateAllWindowsLaf();
		
		SplitPaneUtils.resetAllDIviderColors(this, new Color(25, 25, 25));
		
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				chckbxShuffle.setSelected(currentConf.getSteganOpt().isUltraSecure());
				Font logFont = textAreaLog.getFont();
				textAreaLog.setFont(new Font(logFont.getName(), logFont.getStyle(), currentConf.getGuiOpt().getLogFontSize()-1));
				textAreaMain.setFont(new Font("Segoe UI", Font.PLAIN, 17));
			}
		});
	}
	
	public void showMessageImageNotSupported() {
		int choice = JOptionPane.showConfirmDialog(mainApplication.getMainFrame(), "Image type not supported! Do you want to open image converter tool to convert it?", "Image not Supported!", JOptionPane.YES_NO_OPTION);
		if(choice == JOptionPane.YES_OPTION) {
			mainApplication.getMntmImageConverter().doClick();
		}
	}
	
	public boolean isCoderUsed() {
		return (screeptManagerFoF != null && screeptManagerFoF.coderUsed);
	}
	
	public void resizeFrameToDefault(MainGUI mainApp) {

		final Rectangle currSize = mainApplication.getMainFrame().getBounds();
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				Dimension med = new Dimension(currSize.width - 50, currSize.height - 115);
				Dimension high = new Dimension(currSize.width - 45, currSize.height - 110);
				
				getPanelMainContainer().setPreferredSize(med);
				getPanelMainContainer().setMaximumSize(high);
				if (high.getWidth() > getPanelMainContainer().getMinimumSize().width) {
					if (high.getHeight() > getPanelMainContainer().getMinimumSize().height) {
						getPanelMainContainer().setPreferredSize(high);
					}
				}
				
				textAreaMain.revalidate();
				textAreaMain.repaint();
				
				panelVDragNDropPane.revalidate();
				panelVDragNDropPane.repaint();
				
				resizeRightSplitter();
				
				revalidate();
				repaint();
			}
		});
	}
	
	// getters and setters
	
	public JTextArea getTextAreaMain() {
		return textAreaMain;
	}

	public void setTextAreaMain(RSyntaxTextArea textAreaMain) {
		this.textAreaMain = textAreaMain;
	}

	public JTextArea getTextAreaLog() {
		return textAreaLog;
	}

	public void setTextAreaLog(JTextArea textAreaLog) {
		this.textAreaLog = textAreaLog;
	}

	public JTree getTree() {
		return tree;
	}

	public void setTree(JTree tree) {
		this.tree = tree;
	}

	public JFrame getParentFrame() {
		return parentFrame;
	}

	public void setParentFrame(JFrame parentFrame) {
		this.parentFrame = parentFrame;
	}

	public JLabel getLblDisplayedKey() {
		return lblDisplayedKey;
	}

	public void setLblDisplayedKey(JLabel lblDisplayed) {
		this.lblDisplayedKey = lblDisplayed;
	}

	public JPanel getPanelKeyImgDisp() {
		return panelKeyImgDisp;
	}

	public void setPanelKeyImgDisp(JPanel panelKeyImgDisp) {
		this.panelKeyImgDisp = panelKeyImgDisp;
	}
	
	public File getSrcKeyImageFile() {
		return srcKeyImageFile;
	}

	public void setSrcKeyImageFile(File srcKeyImageFile) {
		this.srcKeyImageFile = srcKeyImageFile;
	}
	
	public JLabel getLblKeyImageSelected() {
		return lblKeyImageSelected;
	}

	public void setLblKeyImageSelected(JLabel lblKeyImageSelected) {
		this.lblKeyImageSelected = lblKeyImageSelected;
	}

	public JTabbedPane getTabbedPane() {
		return tabbedPane;
	}

	public VirtualFileDropPane getPanelVDragNDropPane() {
		return panelVDragNDropPane;
	}

	public void setPanelVDragNDropPane(VirtualFileDropPane panelVDragNDropPane) {
		this.panelVDragNDropPane = panelVDragNDropPane;
	}

	public JPanel getPanelMainContainer() {
		return panelMainContainer;
	}
	
	private String encapsulateInHtml(String original) {
		return "<html>" + original + "</html>";
	}
	
	public Runnable getAfterCompleteRunnable() {
		return afterCompleteRunnable;
	}

	public void setAfterCompleteRunnable(Runnable afterCompleteRunnable) {
		this.afterCompleteRunnable = afterCompleteRunnable;
	}

	public File getCompletedDirectoryImportExport() {
		return completedDirectoryImportExport;
	}

	public void setCompletedDirectoryImportExport(File completedDirectoryImportExport) {
		this.completedDirectoryImportExport = completedDirectoryImportExport;
	}

	private void updateShuffleTooltip() {
		if (chckbxShuffle.isSelected()) {
			chckbxShuffle.setToolTipText(encapsulateInHtml(shuffleTooltip));
		} else {
			chckbxShuffle.setToolTipText(encapsulateInHtml(unSelectedTooltip + shuffleTooltip));
		}
	}

	private void updateHashedTooltip() {
		if (chckbxHashKey.isSelected()) {
			chckbxHashKey.setToolTipText(encapsulateInHtml(hashedTooltip));
		} else {
			chckbxHashKey.setToolTipText(encapsulateInHtml(unSelectedTooltip + hashedTooltip));
		}
	}
	
	private void updateModeInfoTooltip() {
		String tooltip = "No Info available";
		
		if (mode.equals(CryptoMode.ONLY_AES)) {
			tooltip = encapsulateInHtml(onlyAesTooltip);
			
		} else if (mode.equals(CryptoMode.ONLY_STEG)) {
			tooltip = encapsulateInHtml(onlyStegTooltip);
			
		} else if (mode.equals(CryptoMode.COMPLETE)){
			tooltip = encapsulateInHtml(completeModeTooltip);
		}
		
		iconInfoMode.setToolTipText(tooltip);
	}
	
}

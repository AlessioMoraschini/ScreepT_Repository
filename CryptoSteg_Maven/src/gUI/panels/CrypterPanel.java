package gUI.panels;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;

import javax.swing.JTree;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import amfileutils.FileStuff;
import crypt.aes.AESException;
import crypt.aes.AesCipher;
import crypt.manager.CryptoMode;
import crypt.manager.ScreeptManager;
import crypt.steg.StegCipher;
import crypt.steg.STEGException;
import gUI.mainGUI;
import gUI.om.BufferedImageType;
import gUI.utils.IconsPathConfigurator;
import gUI.utils.filedialog.GenericFileChooserDialog;
import gUI.utils.tree.FileTreeModel;
import gUI.utils.tree.crypter.TreeFileMouseListenerCrypter;
import gUI.utils.tree.crypter.dragndrop.VirtualFileDropPane;
import iNIT.INItializer;
import local.HandleString;
import net.miginfocom.swing.MigLayout;
import uTILS.local.ImageWorker;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.border.LineBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.Highlighter;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import java.awt.SystemColor;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.SwingConstants;
import javax.swing.UIDefaults;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTabbedPane;
import javax.swing.JComboBox;
import javax.crypto.NoSuchPaddingException;
import javax.imageio.ImageIO;
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
import java.awt.event.ActionEvent;
import javax.swing.JCheckBox;
import javax.swing.JSeparator;
import java.awt.Cursor;
import javax.swing.JPasswordField;

public class CrypterPanel extends JPanel {

	// TextFileWriter logger creation
	static Logger logger = Logger.getLogger(CrypterPanel.class);
	
	// Global reference to current configuration
	private INItializer currentConf;
	
	// dateFormatter
	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd @ HH-mm-ss");
	
///////////////////////////////////////////////
		//  GUI-RELATED-OBJECTS   //
		////////////////////////////

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

	// frame of parent GUI-object (main-GUI)
	private JFrame parentFrame;
	
	// panels
	private JPanel panelContainerKey;
	private JPanel panelStatus;	
	private JPanel panelKeyImgDisp;
	private JPanel panelHeader;
	
	// scroll panel for jtree
	private JScrollPane scrollPaneTree;
	
	// other scroll-panels
	private JScrollPane scrollPaneText;
	private JScrollPane scrollPaneLog;
	private JScrollPane scrollPanelTab;

	// info tabbed pane
	private JTabbedPane tabbedPane;
	
	// Buttons
	private JButton btnDeCreept;
	private JButton btnCreept;
	private JButton btnSelectSourceFile;
	private JButton btnDispoKeyImg;

	// public field representing current Panel
	public static CrypterPanel thisPanel; 
	
	// AES password gui-container
	private JPasswordField AESKEYtext;
	public static final char DEFAULT_PSW_CHAR = '\u25CF';
	
	// combobox to select conversion mode
	private JComboBox comboModeCrypto;

	
///////////////////////////////////////////////
		// CODING-RELATED-OBJECTS //
		////////////////////////////
	
	// static path of keyDictionary
	public static final String KEY_DICTIONARY = KeyGenPanel.KEY_DICTIONARY;
		
	// file representing dictionary
	public static final File KEY_DICTIONARY_FILE = new File(KEY_DICTIONARY);
		
	// TEMP DIRECTORY
	private final File TEMP_DIR = new File("Resources_Scrypt/temp/");
	
	// field representing the source file loaded by user, this is the file to crypt/decrypt
	private File srcFile;
	private File[] srcFiles; 
	
	// fied representing the img key file selected by user
	private File srcKeyImageFile;
	
	// CODERS objects - shared by methods
	private AesCipher AEScoder;
	private StegCipher STEGcoder;	
	private ScreeptManager screepTManager;
	private CryptoMode mode;
	
	// volatile value representing if coders are used
	public volatile boolean coderUsed = false;
	
	public static final int labelLengthLimit = 40;
	private JCheckBox chckbxShuffle;
	private JScrollPane scrollPaneDragNDropSource;
	private JPanel panelBackgroundDragSources;
	private JTree treeSourcesDragAndDrop;
	private JLabel lblSelectedFilesDragTitle;
	private JSeparator separatorDragNDropTitle;
	private VirtualFileDropPane panelVDragNDropPane;

	/**
	 * Create the application.
	 */
	public CrypterPanel(INItializer currentConfiguration, JFrame parent) {
		
/////////////////////////////////////////////////////////////////////////////////////
///////////////					INITIALIZATION    				/////////////////////
/////////////////////////////////////////////////////////////////////////////////////
		logger.info(this.getClass().getName() + " Starting...");
		
		thisPanel = this;
		
		parentFrame=parent;
		// set the background of the entire panel
		setBackground(SystemColor.desktop);
		
		// start with layout & header
		initializeHeader();		
		
		// initialize text areas		
		initializeTextAreas();
		
		// initialize tree and panned events
		configureNstartTree();		
		initializeTree();
		
		// function to initialize values
		initialize(currentConfiguration);
				
		// ADD ACTION LISTENERS TO LINK ACTIONS // 
		addHandlers();
		
		// add Icons
		addIcons();
		
		// new thread that updates jtree periodically
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				while (true) {
					String backupText = textAreaLog.getText();
					Highlighter backH = textAreaLog.getHighlighter();
					textAreaLog.setHighlighter(null);
					TreePath[] backup = tree.getSelectionPaths();
					tree = new JTree(treeModel);
					// refresh tree gui
					initializeTree();
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
		}).start();
		
		// new thread keeps updated aeskey check and ReadyToCode label.
		// this thread will check (every second) the value contained in the textfield
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				while(true) {
					
					// check the aes key and set jtextField color
					AesCipher.check(AESKEYtext);
					// set the label text according to selected mode
					if(checkIfReadyToCode() && !coderUsed) {
						enableCoderButtons();
					}else {
						disableCoderButtons();
					}
					
					if(!coderUsed) {
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
		}).start();
		
		logger.info(this.getClass().getName() + " Started");
	}
	
	/**
	 * THIS METHOD INITIALIZE GUI ELEMENTS AND LISTENERS
	 */
	private void initializeHeader() {
		setLayout(new MigLayout("", "[200:200:200][200:200:200][:10px:10px][130,grow][:80:130,grow][140][120,grow][350px:350px:350px][100px:100px:100px]", "[:40:50px,top][4px:4px:4px][35][4px][100,grow][140,grow][:30px:30px][280,grow][:1px:1px]"));
		
		panelHeader = new JPanel();
		panelHeader.setForeground(SystemColor.menu);
		panelHeader.setBorder(new LineBorder(new Color(211, 211, 211), 2, true));
		panelHeader.setBackground(new Color(205, 92, 92));
		add(panelHeader, "cell 0 0 9 1,grow");
		panelHeader.setLayout(new MigLayout("", "[400px,grow]", "[40]"));
		
		lblScreeptHeader = new JLabel("ScreepT - File Crypt & Decrypt");
		lblScreeptHeader.setHorizontalAlignment(SwingConstants.CENTER);
		lblScreeptHeader.setForeground(new Color(0, 0, 0));
		lblScreeptHeader.setFont(new Font("Lithos Pro Regular", Font.BOLD, 22));
		lblScreeptHeader.setBackground(new Color(112, 128, 144));
		panelHeader.add(lblScreeptHeader, "cell 0 0,growx");
		
		scrollPanelTab = new JScrollPane();
		scrollPanelTab.setBorder(new LineBorder(new Color(119, 136, 153), 3, true));
		scrollPanelTab.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPanelTab.getViewport().setBackground(Color.DARK_GRAY);
		
		panelStatus = new JPanel();
		panelStatus.setBorder(new LineBorder(new Color(0, 0, 0), 2));
		panelStatus.setBackground(Color.BLACK);
		add(panelStatus, "cell 0 2 9 1,grow");
		GridBagLayout gbl_panelStatus = new GridBagLayout();
		gbl_panelStatus.columnWidths = new int[]{100, 0, 70, 60, 70, 150, 0, 70, 100, 80, 300, 120, 135, 10, 0};
		gbl_panelStatus.rowHeights = new int[]{32, 0};
		gbl_panelStatus.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_panelStatus.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panelStatus.setLayout(gbl_panelStatus);
		
		lblSourceFileStatic = new JLabel("Source File:");
		lblSourceFileStatic.setForeground(new Color(255, 239, 213));
		lblSourceFileStatic.setFont(new Font("Segoe UI", Font.BOLD, 14));
		GridBagConstraints gbc_lblSourceFileStatic = new GridBagConstraints();
		gbc_lblSourceFileStatic.insets = new Insets(0, 0, 0, 5);
		gbc_lblSourceFileStatic.gridx = 0;
		gbc_lblSourceFileStatic.gridy = 0;
		panelStatus.add(lblSourceFileStatic, gbc_lblSourceFileStatic);
		
		lblSrcFileSel = new JLabel("No file selected");
		lblSrcFileSel.setMaximumSize(new Dimension(180, 16));
		lblSrcFileSel.setForeground(new Color(135, 206, 235));
		lblSrcFileSel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		GridBagConstraints gbc_lblSrcFileSel = new GridBagConstraints();
		gbc_lblSrcFileSel.insets = new Insets(0, 0, 0, 5);
		gbc_lblSrcFileSel.gridx = 1;
		gbc_lblSrcFileSel.gridy = 0;
		panelStatus.add(lblSrcFileSel, gbc_lblSrcFileSel);
		
		lblSelectedInputStatic = new JLabel(" Key Image:");
		lblSelectedInputStatic.setForeground(new Color(255, 239, 213));
		GridBagConstraints gbc_lblSelectedInputStatic = new GridBagConstraints();
		gbc_lblSelectedInputStatic.insets = new Insets(0, 0, 0, 5);
		gbc_lblSelectedInputStatic.gridx = 2;
		gbc_lblSelectedInputStatic.gridy = 0;
		panelStatus.add(lblSelectedInputStatic, gbc_lblSelectedInputStatic);
		lblSelectedInputStatic.setFont(new Font("Segoe UI", Font.BOLD, 14));
		
		lblKeyImageSelected = new JLabel("No file selected");
		lblKeyImageSelected.setMaximumSize(new Dimension(180, 16));
		GridBagConstraints gbc_lblKeyImageSelected = new GridBagConstraints();
		gbc_lblKeyImageSelected.insets = new Insets(0, 0, 0, 5);
		gbc_lblKeyImageSelected.gridx = 3;
		gbc_lblKeyImageSelected.gridy = 0;
		panelStatus.add(lblKeyImageSelected, gbc_lblKeyImageSelected);
		lblKeyImageSelected.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		lblKeyImageSelected.setForeground(new Color(135, 206, 235));
		
		lblAesKeyStatic = new JLabel("AES key:");
		lblAesKeyStatic.setForeground(new Color(255, 239, 213));
		GridBagConstraints gbc_lblAesKeyStatic = new GridBagConstraints();
		gbc_lblAesKeyStatic.insets = new Insets(0, 0, 0, 5);
		gbc_lblAesKeyStatic.gridx = 4;
		gbc_lblAesKeyStatic.gridy = 0;
		panelStatus.add(lblAesKeyStatic, gbc_lblAesKeyStatic);
		lblAesKeyStatic.setFont(new Font("Segoe UI", Font.BOLD, 14));
		
		AESKEYtext = new JPasswordField();
		AESKEYtext.setHorizontalAlignment(SwingConstants.CENTER);
		AESKEYtext.setEchoChar(DEFAULT_PSW_CHAR);
		AESKEYtext.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		GridBagConstraints gbc_AESKEYtext = new GridBagConstraints();
		gbc_AESKEYtext.fill = GridBagConstraints.HORIZONTAL;
		gbc_AESKEYtext.insets = new Insets(0, 0, 0, 5);
		gbc_AESKEYtext.gridx = 5;
		gbc_AESKEYtext.gridy = 0;
		panelStatus.add(AESKEYtext, gbc_AESKEYtext);
		AESKEYtext.setColumns(10);
		
		chckbxShowAesKey = new JCheckBox("Show");
		chckbxShowAesKey.setForeground(Color.WHITE);
		chckbxShowAesKey.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		GridBagConstraints gbc_chckbxShowAesKey = new GridBagConstraints();
		gbc_chckbxShowAesKey.anchor = GridBagConstraints.WEST;
		gbc_chckbxShowAesKey.insets = new Insets(0, 0, 0, 5);
		gbc_chckbxShowAesKey.gridx = 6;
		gbc_chckbxShowAesKey.gridy = 0;
		panelStatus.add(chckbxShowAesKey, gbc_chckbxShowAesKey);
		
		lblSelectedModeStatic = new JLabel("Mode:");
		lblSelectedModeStatic.setForeground(new Color(255, 239, 213));
		GridBagConstraints gbc_lblSelectedModeStatic = new GridBagConstraints();
		gbc_lblSelectedModeStatic.insets = new Insets(0, 0, 0, 5);
		gbc_lblSelectedModeStatic.gridx = 7;
		gbc_lblSelectedModeStatic.gridy = 0;
		panelStatus.add(lblSelectedModeStatic, gbc_lblSelectedModeStatic);
		lblSelectedModeStatic.setFont(new Font("Segoe UI", Font.BOLD, 14));
		
		comboModeCrypto = new JComboBox();
		GridBagConstraints gbc_comboModeCrypto = new GridBagConstraints();
		gbc_comboModeCrypto.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboModeCrypto.insets = new Insets(0, 0, 0, 5);
		gbc_comboModeCrypto.gridx = 8;
		gbc_comboModeCrypto.gridy = 0;
		panelStatus.add(comboModeCrypto, gbc_comboModeCrypto);
		comboModeCrypto.setModel(new DefaultComboBoxModel(CryptoMode.values()));
			
		chckbxShuffle = new JCheckBox("Ultra Secure");
		chckbxShuffle.setForeground(Color.WHITE);
		
		GridBagConstraints gbc_chckbxShuffle = new GridBagConstraints();
		gbc_chckbxShuffle.insets = new Insets(0, 0, 0, 5);
		gbc_chckbxShuffle.gridx = 9;
		gbc_chckbxShuffle.gridy = 0;
		panelStatus.add(chckbxShuffle, gbc_chckbxShuffle);
		
		labelFlagCodeReady = new JLabel("");
		labelFlagCodeReady.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc_labelFlagCodeReady = new GridBagConstraints();
		gbc_labelFlagCodeReady.insets = new Insets(0, 0, 0, 5);
		gbc_labelFlagCodeReady.fill = GridBagConstraints.VERTICAL;
		gbc_labelFlagCodeReady.gridx = 10;
		gbc_labelFlagCodeReady.gridy = 0;
		panelStatus.add(labelFlagCodeReady, gbc_labelFlagCodeReady);
		labelFlagCodeReady.setBorder(null);
		labelFlagCodeReady.setFont(new Font("SansSerif", Font.BOLD, 14));
		
		btnCreept = new JButton("Creept");
		btnCreept.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		GridBagConstraints gbc_btnCreept = new GridBagConstraints();
		gbc_btnCreept.fill = GridBagConstraints.BOTH;
		gbc_btnCreept.insets = new Insets(0, 0, 0, 5);
		gbc_btnCreept.gridx = 11;
		gbc_btnCreept.gridy = 0;
		panelStatus.add(btnCreept, gbc_btnCreept);
		btnCreept.setRequestFocusEnabled(false);
		btnCreept.setForeground(Color.BLACK);
		btnCreept.setBorder(null);
		btnCreept.setFont(new Font("Lithos Pro Regular", Font.BOLD, 16));
		btnCreept.setBackground(new Color(127, 255, 0));
		
		btnDeCreept = new JButton("DeCreept");
		btnDeCreept.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnDeCreept.setMargin(new Insets(0, 0, 0, 20));
		GridBagConstraints gbc_btnDeCreept = new GridBagConstraints();
		gbc_btnDeCreept.insets = new Insets(0, 0, 0, 5);
		gbc_btnDeCreept.fill = GridBagConstraints.BOTH;
		gbc_btnDeCreept.gridx = 12;
		gbc_btnDeCreept.gridy = 0;
		panelStatus.add(btnDeCreept, gbc_btnDeCreept);
		btnDeCreept.setRequestFocusEnabled(false);
		btnDeCreept.setForeground(Color.BLACK);
		btnDeCreept.setBorder(null);
		btnDeCreept.setBackground(new Color(255, 0, 0));
		btnDeCreept.setFont(new Font("Lithos Pro Regular", Font.BOLD, 16));
		
		add(scrollPanelTab, "cell 0 4 2 2,grow");
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBorder(null);
		tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		scrollPanelTab.setViewportView(tabbedPane);
		
	}
	
	private void addIcons() {
		btnCreept.setIconTextGap(10);
		btnDeCreept.setIconTextGap(10);
		btnCreept.setIcon(new ImageIcon(IconsPathConfigurator.ICON_CRYPT));
		btnDeCreept.setIcon(new ImageIcon(IconsPathConfigurator.ICON_DECRYPT));
	}
	
	private void addHandlers(){
		

		// CREEPT ACTION LISTENER
		btnCreept.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				
				try {
					new Thread(cripterRunner()).start();
				}
				catch(Exception ex) {
					coderUsed = false;
					if(screepTManager!=null) {
						screepTManager.disableProgressUpdater();
					}
					JOptionPane.showMessageDialog(thisPanel,"OOOps, an error occurred, see log for details :(", "Generic Error", JOptionPane.ERROR_MESSAGE);
					logger.error("Exception happened during creept", ex);
				}
				
			}
		});
		
		
		// DECREEPT ACTION LISTENER
		btnDeCreept.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				try {
					new Thread(decripterRunner()).start();
				}catch(Exception ex) {
					coderUsed = false;
					if(screepTManager!=null) {
						screepTManager.disableProgressUpdater();
					}
					JOptionPane.showMessageDialog(thisPanel,"OOOps, an error occurred, see log for details :(", "Generic Error", JOptionPane.ERROR_MESSAGE);
					logger.error("Exception happened during decreept!", ex);
				}
								
			}
		});
		
		// CHANGED MODE LISTENER
		comboModeCrypto.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				CryptoMode modality = (CryptoMode)comboModeCrypto.getSelectedItem();
				if(modality.equals(CryptoMode.ONLY_AES)) {
					chckbxShuffle.setEnabled(false);
					chckbxShuffle.setForeground(Color.gray);
				}else {
					chckbxShuffle.setEnabled(true);
					chckbxShuffle.setForeground(Color.white);
				}
				
				// update configuration reference
				currentConf.getAesOpt().setFlagAes(modality.AES);
				currentConf.getSteganOpt().setFlagSteg(modality.STEG);
			}
		});
		
		// ULTRA-SECURE CHECKBOX LISTENER
		chckbxShuffle.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				currentConf.getSteganOpt().setUltraSecure(chckbxShuffle.isSelected() && chckbxShuffle.isEnabled());
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
				File selected = GenericFileChooserDialog.fileAndDirRead(thisPanel, currentConf);
				
				if (selected!=null && selected.exists()) {
					srcFile = selected;
					panelVDragNDropPane.addFileToTree(selected);
					String newdisplayed = HandleString.fitToMaxLentgh(selected.getName(), labelLengthLimit);
					// update gui
					panelVDragNDropPane.message.setText(newdisplayed + " added to list!");
					
					appendMessageTxtAreas("Source File Loaded: "+srcFile.getAbsolutePath());
					
					JOptionPane.showMessageDialog(thisPanel, "Source file correctly loaded", "File Loaded :)", JOptionPane.INFORMATION_MESSAGE);
					logger.debug("File :" + selected.getName() + " correctly loaded");
				}
			}
		});
		
		// SELECT DISPOSABLE KEY IMAGE LISTENER
		btnDispoKeyImg.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				File dispoImgFile = GenericFileChooserDialog.fileImgRead(thisPanel, currentConf, "png");
				if (dispoImgFile != null) {
					useAsKeyImage(dispoImgFile);
					loadInfo(dispoImgFile);
				}
			}
		});
	}
	
/////////////////////////////////////
		// USEFUL METHODS //
		////////////////////
	
	private Runnable decripterRunner() {
		return new Runnable() {
			
			@Override
			public void run() {
				
				File destinationDir = null;
				// retrieve source file array from dragNDrop panel
				srcFiles = panelVDragNDropPane.getTreeNodes();
				
				// ask user if he wants to delete decripted after closing ScrYpT
				int deleteAfterClose = JOptionPane.showConfirmDialog(parentFrame, "Delete decripted file(s) after exiting ScrypT? (TEMP folder will be created on Desktop)",
						"Delete decripted when SW will be closed", JOptionPane.YES_NO_OPTION);
				
				if(deleteAfterClose == JOptionPane.YES_OPTION) {
					// if temporary mode create temp folder ScrYpT on desktop
					destinationDir = new File(mainGUI.TEMP_DECRIPTED_ROOT_PATH);
				}else {
					// get output asking to user
					destinationDir = GenericFileChooserDialog.directoWrite(parentFrame, currentConf);
				}
				
				// notify that method is running
				coderUsed=true;
				
				// boolean value representing if everything went good
				boolean resultOK = false;
				
				// retrieve keyAES from GUI text-field
				String aesKey = AESKEYtext.getText();
				
				// first recognize selected combo Mode selected
				mode = (CryptoMode)comboModeCrypto.getSelectedItem();
				
				if (destinationDir!= null) {
					
					Date now = new Date();
					String date = dateFormatter.format(now);
					destinationDir = new File(destinationDir.getAbsolutePath() + "/" + date);
					
					if (!destinationDir.exists()) {
						destinationDir.mkdir();
					}
				}
				
				for (File currentFile : srcFiles) {
					if (currentFile != null && currentFile.exists() && destinationDir != null) {

						// ONLY AES DECRYPT
						if (mode.AES && !mode.STEG) {
							try {
								screepTManager = initAES(aesKey);

								// if only one file
								if (currentFile.isFile()) {
									resultOK = deCryptCurrentFile(currentFile, destinationDir);

									// if directory selected
								} else if (currentFile.isDirectory()) {
									resultOK = deCryptDirAES(currentFile, destinationDir);
								}

							} catch (UnsupportedEncodingException e1) {
								resultOK = resetAfterException("Encoding Exception happened!", e1);
							} catch (NoSuchPaddingException e1) {
								resultOK = resetAfterException("Padding Exception happened!", e1);
							} catch (NoSuchAlgorithmException e1) {
								resultOK = resetAfterException("Algorithm Exception happened!", e1);
							} catch (IOException e1) {
								resultOK = resetAfterException("Exception occurred moving the result file :(", e1);
							} catch (Exception e1) {
								resultOK = resetAfterException("Generic Exception occurred :(", e1);
							}

							// ONLY STEG DECRYPT
						} else if (!mode.AES && mode.STEG) {
							try {
								screepTManager = initSTEG(srcKeyImageFile);

								if (currentFile.isDirectory()) {
									resultOK = deCryptDirSTEG(currentFile, destinationDir);
								}

							} catch (IOException e1) {
								resultOK = resetAfterException("IO Exception happened!", e1);
							} catch (STEGException e1) {
								resultOK = resetAfterException("STEG Exception happened!", e1);
							} catch (Exception e1) {
								resultOK = resetAfterException("Generic Exception occurred :(", e1);
							}

							// COMPLETE DECRYPT
						} else if (mode.AES && mode.STEG) {
							try {
								screepTManager = initCOMPLETE(aesKey, srcKeyImageFile);

								if (currentFile.isDirectory()) {
									resultOK = deCryptDirSTEG(currentFile, destinationDir);
								}

							} catch (UnsupportedEncodingException e1) {
								resultOK = resetAfterException("Encoding Exception happened!", e1);
							} catch (NoSuchPaddingException e1) {
								resultOK = resetAfterException("Padding Exception happened!", e1);
							} catch (NoSuchAlgorithmException e1) {
								resultOK = resetAfterException("Algorithm Exception happened!", e1);
							} catch (IOException e1) {
								resultOK = resetAfterException("IO Exception happened!", e1);
							} catch (STEGException e1) {
								resultOK = resetAfterException("STEG Exception happened!", e1);
							} catch (Exception e1) {
								resultOK = resetAfterException("Generic Exception occurred :(", e1);
							}
						}
					} 
				}
				coderUsed=false;
				
				if(screepTManager!=null) {
					screepTManager.disableProgressUpdater();
				}
				
				// INFORM USER ABOUT ESIT OF DECRYPT OPERATION
				if(resultOK) {
					String msg = "Operation completed succesfully :)";
					JOptionPane.showMessageDialog(thisPanel, msg,"SUCCESS!", JOptionPane.INFORMATION_MESSAGE);
					logger.info(msg);
					textAreaLog.setText(msg);
					textAreaMain.setText(screepTManager.getOutputStats());
					tabbedPane.setSelectedIndex(0);
					srcFile = null;

				}else {					
					if (destinationDir != null) {
						String msg = "Something went wrong :(";
						JOptionPane.showMessageDialog(thisPanel, msg, "SUCCESS!", JOptionPane.ERROR_MESSAGE);
						logger.error(msg);
						textAreaLog.setText(msg);
						textAreaMain.setText(screepTManager.getOutputStats());
						tabbedPane.setSelectedIndex(0);
					}else{
						JOptionPane.showMessageDialog(thisPanel, "No directory selected, export aborted!", "No directory selected", JOptionPane.WARNING_MESSAGE);
					}
				}
				
				coderUsed=false;
				
			}
		};
	}
	
	private Runnable cripterRunner() {
		return new Runnable() {
			
			@Override
			public void run() {
				
				// notify that method is running
				coderUsed=true;
				
				// get output asking to user
				File destinationDir = GenericFileChooserDialog.directoWrite(parentFrame, currentConf);
				
				// retrieve source file array from dragNDrop panel
				srcFiles = panelVDragNDropPane.getTreeNodes();
				
				// boolean value representing if everything went good
				boolean resultOK = false;
				
				// retrieve keyAES from GUI text-field
				String aesKey = AESKEYtext.getText();
				
				// first recognize selected combo Mode selected
				mode = (CryptoMode)comboModeCrypto.getSelectedItem();
				
				if (destinationDir!= null) {
					
					Date now = new Date();
					String date = dateFormatter.format(now);
					destinationDir = new File(destinationDir.getAbsolutePath() + "/" + date);
					
					if (!destinationDir.exists()) {
						destinationDir.mkdir();
					}
				}
				
				for (File currentFile : srcFiles) {
					if (currentFile != null && currentFile.exists() && destinationDir != null) {
						
						// ONLY AES CRYPT
						if (mode.AES && !mode.STEG) {
							try {
								// initialize coder
								screepTManager = initAES(aesKey);

								// if only one file
								if (currentFile.isFile()) {
									resultOK = cryptCurrentFile(currentFile, destinationDir);

									// if directory selected
								} else if (currentFile.isDirectory()) {
									resultOK = cryptDir(currentFile, destinationDir);
								}

							} catch (UnsupportedEncodingException e1) {
								resultOK = resetAfterException("Encoding Exception happened!", e1);
							} catch (NoSuchPaddingException e1) {
								resultOK = resetAfterException("Padding Exception happened!", e1);
							} catch (NoSuchAlgorithmException e1) {
								resultOK = resetAfterException("Algorithm Exception happened!", e1);
							} catch (IOException e1) {
								resultOK = resetAfterException("Exception occurred moving the result file :(", e1);
							} catch (Exception e1) {
								resultOK = resetAfterException("Generic Exception occurred :(", e1);
							}

							// ONLY STEG CRYPT
						} else if (!mode.AES && mode.STEG) {
							try {

								screepTManager = initSTEG(srcKeyImageFile);

								// if only one file
								if (currentFile.isFile()) {
									resultOK = cryptCurrentFile(currentFile, destinationDir);

									// if directory selected
								} else if (currentFile.isDirectory()) {
									resultOK = cryptDir(currentFile, destinationDir);
								}

							} catch (IOException e1) {
								resultOK = resetAfterException("IO Exception happened!", e1);
							} catch (STEGException e1) {
								resultOK = resetAfterException("STEG Exception happened!", e1);
							} catch (Exception e1) {
								resultOK = resetAfterException("Generic Exception occurred :(", e1);
							}

							// COMPLETE CRYPT
						} else if (mode.AES && mode.STEG) {
							try {

								screepTManager = initCOMPLETE(aesKey, srcKeyImageFile);

								// if only one file
								if (currentFile.isFile()) {
									resultOK = cryptCurrentFile(currentFile, destinationDir);

									// if directory selected
								} else if (currentFile.isDirectory()) {
									resultOK = cryptDir(currentFile, destinationDir);
								}

							} catch (UnsupportedEncodingException e1) {
								resultOK = resetAfterException("Encoding Exception happened!", e1);
							} catch (NoSuchPaddingException e1) {
								resultOK = resetAfterException("Padding Exception happened!", e1);
							} catch (NoSuchAlgorithmException e1) {
								resultOK = resetAfterException("Algorithm Exception happened!", e1);
							} catch (IOException e1) {
								resultOK = resetAfterException("IO Exception happened!", e1);
							} catch (STEGException e1) {
								resultOK = resetAfterException("STEG Exception happened!", e1);
							} catch (AESException e1) {
								resultOK = resetAfterException("AES Exception happened!", e1);
							} catch (Exception e1) {
								resultOK = resetAfterException("Generic Exception occurred :(", e1);
							}
						}
					} 
				}
				coderUsed=false;
				
				if(screepTManager!=null) {
					screepTManager.disableProgressUpdater();
				}
		
				// INFORM USER ABOUT ESIT OF CRYPT OPERATION
				if(resultOK) {
					String msg = "Operation completed succesfully :)";
					JOptionPane.showMessageDialog(thisPanel, msg,"SUCCESS!", JOptionPane.INFORMATION_MESSAGE);
					logger.info(msg);
					textAreaLog.setText(msg);
					textAreaMain.setText(screepTManager.getOutputStats());
					tabbedPane.setSelectedIndex(0);
					srcFile = null;

				}else {
					if(destinationDir != null) {
						String msg = "Something went wrong :(";
						JOptionPane.showMessageDialog(thisPanel, msg,"SUCCESS!", JOptionPane.ERROR_MESSAGE);
						logger.error(msg);
						textAreaLog.setText(msg);
						textAreaMain.setText(screepTManager.getOutputStats());
						tabbedPane.setSelectedIndex(0);
					}
					else{
						JOptionPane.showMessageDialog(thisPanel, "No directory selected, export aborted!", "No directory selected", JOptionPane.WARNING_MESSAGE);
					}
				}
				
				coderUsed=false;
			}
		};
	}

	private void initializeTextAreas() {
		scrollPaneLog = new JScrollPane();
		scrollPaneLog.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		tabbedPane.addTab("Screept Log", null, scrollPaneLog, null);
		tabbedPane.setBackgroundAt(0, new Color(216, 191, 216));
		scrollPaneLog.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		
		textAreaLog = new JTextArea();
		textAreaLog.setBorder(null);
		textAreaLog.setWrapStyleWord(true);
		textAreaLog.setRequestFocusEnabled(false);
		textAreaLog.setLineWrap(true);
		textAreaLog.setMargin(new Insets(10, 10, 10, 10));
		textAreaLog.setEditable(false);
		textAreaLog.setBackground(new Color(255, 250, 205));
		scrollPaneLog.setViewportView(textAreaLog);
		textAreaLog.setText("### WELCOME TO ScreepT :) ###\r\n\r\n Are you ready to crypt/decrypt your files?\r\n\r\n Nobody knows how to crypt them better than me ;)"); 
		
		panelContainerKey = new JPanel();
		panelContainerKey.setBackground(Color.DARK_GRAY);
		panelContainerKey.setBorder(new LineBorder(new Color(0, 0, 0), 0));
		tabbedPane.addTab("Img Preview", null, panelContainerKey, null);
		panelContainerKey.setLayout(new MigLayout("", "[385:385:385,center]", "[:20:20][200px,grow,top]"));
		
		lblDisplayedKey = new JLabel("to see image: Right click on img -> Load Image and Details");
		lblDisplayedKey.setForeground(new Color(240, 230, 140));
		lblDisplayedKey.setHorizontalAlignment(SwingConstants.RIGHT);
		lblDisplayedKey.setFont(new Font("SansSerif", Font.BOLD, 12));
		panelContainerKey.add(lblDisplayedKey, "cell 0 0,alignx center,aligny top");
		
		panelKeyImgDisp = new JPanel();
		panelKeyImgDisp.setMinimumSize(new Dimension(385, 200));
		panelKeyImgDisp.setBounds(new Rectangle(0, 0, 400, 0));
		panelKeyImgDisp.setPreferredSize(new Dimension(0, 10));
		panelKeyImgDisp.setBackground(Color.BLACK);
		panelContainerKey.add(panelKeyImgDisp, "cell 0 1,grow");
		panelKeyImgDisp.setLayout(new GridLayout(1, 0, 0, 0));
		tabbedPane.setBackgroundAt(1, new Color(169, 169, 169));
		
		scrollPaneText = new JScrollPane();
		scrollPaneText.setBorder(new LineBorder(Color.GRAY, 4));
		add(scrollPaneText, "cell 2 4 5 4,grow");
		
		textAreaMain = new JTextArea();
		textAreaMain.setWrapStyleWord(true);
		textAreaMain.setRequestFocusEnabled(false);
		textAreaMain.setFont(new Font("SansSerif", Font.PLAIN, 15));
		textAreaMain.setBackground(SystemColor.menu);
		textAreaMain.setEditable(false);
		textAreaMain.setMargin(new Insets(10, 35, 10, 35));
		scrollPaneText.setViewportView(textAreaMain);
		textAreaMain.setText("   ###  WELCOME TO ScreepT :) ###\r\n\r\n Are you ready to crypt/decrypt your files? I will be your best friend... and attackers worst nightmare ;)");
		textAreaMain.append("\n\n#####################################################################################\n");
		textAreaMain.append(    "###############################__I'M READY__#########################################");
		textAreaMain.append("  \n#####################################################################################\n");

	}
	
	public void configureNstartTree() {
		
		/** TREE FOR KEY IMAGES **/
		
		treeModel = new FileTreeModel(KEY_DICTIONARY_FILE);
		treeListener = new TreeSelectionListener() {
			
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				String msg;
				File thisFile = (File)tree.getLastSelectedPathComponent();
				if (thisFile.isDirectory()) {
					msg = "\n### Directory selected ### \n\n => " + thisFile.getName();
					textAreaLog.setText(msg);	
				}else {
					if(FilenameUtils.getExtension(thisFile.getAbsolutePath()).equals("png")) {
						msg = " .png Key recognized ->" + thisFile.getName()+"\n\n ## SIZE: "+FileStuff.coolFileSize(thisFile)+"\n\n ## Path: "+thisFile.getAbsolutePath();
						textAreaLog.setText(msg);						
					}else {
						msg = "File not recognized as Key: \n\n => "+thisFile.getName();
						textAreaLog.setText(msg);
					}
				}
				TreeFileMouseListenerCrypter menuListener = new TreeFileMouseListenerCrypter( currentConf, thisPanel);			
			}
		};
		
		btnDispoKeyImg = new JButton("One-use Key Image");
		btnDispoKeyImg.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		add(btnDispoKeyImg, "cell 0 6 2 1,growx");
		btnDispoKeyImg.setMargin(new Insets(4, 4, 4, 4));
		btnDispoKeyImg.setRequestFocusEnabled(false);
		btnDispoKeyImg.setBorder(new LineBorder(new Color(119, 136, 153), 3, true));
		btnDispoKeyImg.setForeground(SystemColor.text);
		btnDispoKeyImg.setFont(new Font("Lithos Pro Regular", Font.PLAIN, 16));
		btnDispoKeyImg.setBackground(Color.DARK_GRAY);
		
		// initialize tree 
		
		scrollPaneTree = new JScrollPane();
		scrollPaneTree.setBackground(Color.DARK_GRAY);
		scrollPaneTree.setBorder(new LineBorder(new Color(119, 136, 153), 3, true));
		add(scrollPaneTree, "cell 0 7 2 1,grow");
		tree= new JTree(treeModel);
		tree.setBorder(null);
		tree.setRequestFocusEnabled(false);
		tree.setFont(new Font("SansSerif", Font.BOLD, 14));
		
		/** TREE TO DRAG AND DROP SOURCE FILES **/
		
		scrollPaneDragNDropSource = new JScrollPane();
		scrollPaneDragNDropSource.setBackground(Color.LIGHT_GRAY);
		scrollPaneDragNDropSource.setBorder(new LineBorder(new Color(119, 136, 153), 3, true));
		add(scrollPaneDragNDropSource, "cell 7 4 2 4,grow");
		
		panelBackgroundDragSources = new JPanel();
		panelBackgroundDragSources.setBackground(new Color(220, 220, 220));
		scrollPaneDragNDropSource.setViewportView(panelBackgroundDragSources);
		panelBackgroundDragSources.setLayout(new MigLayout("", "[][grow]", "[50px][][30px][300px,grow][150px,grow]"));
		
		lblSelectedFilesDragTitle = new JLabel("Selected Files Pool");
		lblSelectedFilesDragTitle.setHorizontalTextPosition(SwingConstants.CENTER);
		lblSelectedFilesDragTitle.setBorder(null);
		lblSelectedFilesDragTitle.setHorizontalAlignment(SwingConstants.CENTER);
		lblSelectedFilesDragTitle.setForeground(Color.BLACK);
		lblSelectedFilesDragTitle.setFont(new Font("Lithos Pro Regular", Font.BOLD, 22));
		lblSelectedFilesDragTitle.setBackground(new Color(112, 128, 144));
		panelBackgroundDragSources.add(lblSelectedFilesDragTitle, "cell 0 0 2 1,growx");
		
		separatorDragNDropTitle = new JSeparator();
		panelBackgroundDragSources.add(separatorDragNDropTitle, "cell 1 1,growx");
		
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
		panelBackgroundDragSources.add(treeSourcesDragAndDrop, "cell 0 3 2 1,grow");
		
		panelVDragNDropPane = new VirtualFileDropPane(treeSourcesDragAndDrop, scrollPaneDragNDropSource);
		panelVDragNDropPane.message.setText("Drag your files here!");
		panelVDragNDropPane.message.setFont(new Font("SansSerif", Font.BOLD, 16));
		panelVDragNDropPane.setBorder(new LineBorder(new Color(0, 0, 0), 2, true));
		panelVDragNDropPane.setBackground(Color.LIGHT_GRAY);
		panelBackgroundDragSources.add(panelVDragNDropPane, "cell 0 4 2 1,grow");
	}
	
	public void initializeTree() {
		
		UIDefaults paneDefaults = new UIDefaults();
	    paneDefaults.put("Tree.selectionBackground",Color.BLACK);
	    
		/** TREE TO DRAG AND DROP SOURCE FILES **/
//		treeSourcesDragAndDrop.setCellRenderer(new TreeCellRendererDark());
	    treeSourcesDragAndDrop.putClientProperty("Nimbus.Overrides",paneDefaults);
	    treeSourcesDragAndDrop.putClientProperty("Nimbus.Overrides.InheritDefaults",false);
		
		/** TREE FOR KEY IMAGES **/
		
		tree.setVisibleRowCount(10);		
		// set custom graphic renderer to jtree
	    // tree.setCellRenderer(new TreeCellRendererDark());
		tree.setBackground(Color.DARK_GRAY);
		tree.putClientProperty("Nimbus.Overrides",paneDefaults);
		tree.putClientProperty("Nimbus.Overrides.InheritDefaults",false);
		scrollPaneTree.setViewportView(tree);
		tree.addTreeSelectionListener(treeListener);
		TreeFileMouseListenerCrypter menuListener = new TreeFileMouseListenerCrypter(currentConf, thisPanel);
	}
	
	private void initialize(INItializer config) {
		currentConf = config;		
		AESKEYtext.setText("");
		CryptoMode mode = CryptoMode.getModeFromBoolean(currentConf.getAesOpt().isFlagAes(), currentConf.getSteganOpt().isFlagSteg());
		comboModeCrypto.setSelectedItem(mode);
		chckbxShuffle.setSelected(currentConf.getSteganOpt().isUltraSecure());
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
			lblSrcFileSel.setForeground(Color.YELLOW);
			labelFlagCodeReady.setForeground(Color.RED);
			checkEsit += " - Source File missing - ";
			ready = false;
		}else {
			lblSrcFileSel.setForeground(Color.GREEN);
			lblSrcFileSel.setText(selectedFilesN + " selected");
		}
		
		if(srcKeyImageFile==null && selectedMode.isSTEG()) {
			// INVALID FILE FORMAT
			labelFlagCodeReady.setForeground(Color.red);
			checkEsit += " - Missing IMGkey - ";
			ready = false;
		}
		
		if(AESKEYtext.getText().equals("") && selectedMode.isAES()) {
			// MISSING AES KEY
			labelFlagCodeReady.setForeground(Color.red);
			checkEsit += " - missing AES key - ";
			ready = false;
		}
		
		if(ready) {
			labelFlagCodeReady.setForeground(Color.green);
			checkEsit += " - ScreepT is Ready to Crypt/Decrypt :) - ";
		}		
		
		// update label on GUI
		
		// REFRESH ESIT

		labelFlagCodeReady.setText(checkEsit);
		
		
		return ready;
	}
	
	private void refreshJTree() {

		UIDefaults paneDefaults = new UIDefaults();
	    paneDefaults.put("Tree.selectionBackground",Color.BLACK);
		tree.putClientProperty("Nimbus.Overrides",paneDefaults);
		tree.putClientProperty("Nimbus.Overrides.InheritDefaults",false);
		scrollPaneTree.setViewportView(tree);
		tree.setBackground(Color.GRAY);
		tree.setVisibleRowCount(10);
		tree.addTreeSelectionListener(treeListener);
		tree.revalidate();
		tree.repaint();
	}

///////////////////////////////////////////////////////// CRYPT INITIALIZERS /////////////////////////////////////////////////////////
	private ScreeptManager initAES(String AES_KEY) throws UnsupportedEncodingException, NoSuchPaddingException, NoSuchAlgorithmException {
		AEScoder = new AesCipher(AES_KEY);
		screepTManager = new ScreeptManager(AEScoder);
		return screepTManager;
	}
	
	private ScreeptManager initSTEG(File imageFile) throws IOException, STEGException {
		if(imageFile == null) {
			throw new STEGException();
		}
		STEGcoder = new StegCipher(imageFile);
		screepTManager = new ScreeptManager(STEGcoder);
		screepTManager.setShuffleResult(chckbxShuffle.isSelected() && chckbxShuffle.isEnabled());
		return screepTManager;
	}
	
	private ScreeptManager initCOMPLETE(String AES_KEY, File imageFile) throws NoSuchPaddingException, NoSuchAlgorithmException, IOException, STEGException, AESException {
		if(imageFile == null) {
			throw new STEGException();
		}
		if(AES_KEY.length() == 0) {
			throw new AESException();
		}
		AEScoder = new AesCipher(AES_KEY);
		STEGcoder = new StegCipher(imageFile);
		screepTManager = new ScreeptManager(STEGcoder, AEScoder);
		screepTManager.setShuffleResult(chckbxShuffle.isSelected() && chckbxShuffle.isEnabled());
		return screepTManager;
	}
	
///////////////////////////////////////////////////////// CRYPT METHODS /////////////////////////////////////////////////////////
	private boolean cryptCurrentFile(File src, File destinationDir) throws IOException{
		boolean resultOK = false;
		if (destinationDir!=null) {
			
			if(!destinationDir.exists()) {
				destinationDir.mkdirs();
			}
			
			screepTManager.setOutPath(destinationDir.getCanonicalPath());
			resultOK = screepTManager.crypt(src);
			
		}

		return resultOK;
	}
	
	private boolean cryptDir(File src, File destinationDir) throws IOException {
		boolean resultOK = false;
		
		if (src.isFile()) {
			
			resultOK = cryptCurrentFile(src, destinationDir);

		}else if (src.isDirectory()) {
			// create new folder in destination
			File destSonDir = new File(destinationDir.getCanonicalPath()+"/"+src.getName());
			// if dir to create inside destination exists create unique name
			if(destSonDir.exists()) {
				destSonDir = FileStuff.renameJavaObjFile(destSonDir);
			}
			destSonDir.mkdir();
			
			// scan source folder content
			for(File iFile : src.listFiles()) {
				if(iFile.isDirectory()) {
					resultOK = cryptDir(iFile, destSonDir);
				}else if (iFile.isFile()) {
					resultOK = cryptCurrentFile(iFile, destSonDir);
				}
			}
			
		}
		
		return resultOK;
	}
	
///////////////////////////////////////////////////////// DECRYPT METHODS /////////////////////////////////////////////////////////
	private boolean deCryptCurrentFile(File src, File destinationDir) throws IOException{
		boolean resultOK = false;
		if (destinationDir!=null) {
			
			if(!destinationDir.exists()) {
				destinationDir.mkdirs();
			}
			
			screepTManager.setOutPath(destinationDir.getCanonicalPath());
			resultOK = screepTManager.deCrypt(src);
			
		}
		return resultOK;
	}
	
	private boolean deCryptDirAES(File src, File destinationDir) throws IOException {
		boolean resultOK = false;
		
		if (src.isFile()) {
			
			resultOK = deCryptCurrentFile(src, destinationDir);

		}else if (src.isDirectory()) {

			// create new folder in destination
			File destSonDir = new File(destinationDir.getCanonicalPath()+"/"+src.getName());
			// if dir to create inside destination exists create unique name
			destSonDir = FileStuff.renameJavaObjFile(destSonDir);
			destSonDir.mkdir();
			
			// scan source folder content
			for(File iFile : src.listFiles()) {
				if(iFile.isDirectory()) {
					resultOK = deCryptDirAES(iFile, destSonDir);
					if(!resultOK) {
						FileUtils.deleteQuietly(iFile);
					}
				}else if (iFile.isFile()) {
					resultOK = deCryptCurrentFile(iFile, destSonDir);
				}
			}
			
		}
		
		return resultOK;
	}
	
	// use this to recognize folder containing STEG/COMPLETE folders that contains result instead of generic folder
	private boolean deCryptDirSTEG(File src, File destinationDir) throws IOException {
		boolean resultOK = false;
		
		if (src.isDirectory()) {
			// create new folder in destination
			File destSonDir = new File(destinationDir.getCanonicalPath()+"/"+src.getName());
			destSonDir = FileStuff.renameJavaObjFile(destSonDir);
			// if dir to create inside destination exists create unique name
			
			// scan source folder content
			for(File iFile : src.listFiles()) {
				if(iFile.isDirectory()) {
					
					destSonDir.mkdir();
					// now check if sons are files or dir
					File[] retrieved = iFile.listFiles(new FileFilter() {
						
						@Override // don't scan contained files further if they are not directories
						public boolean accept(File sonFile) {
							if(sonFile.isFile()) {
								return false;
							}
							else return true;
						}
					});
					
					if(retrieved.length!=0 && retrieved[0].isDirectory()) {
						// if generic folder it contains other folders
						resultOK = deCryptDirSTEG(iFile, destSonDir);
					}else {
						// if STEG/COMPLETE crypted only png files are contained, then decript current folder
						resultOK = deCryptCurrentFile(iFile, destSonDir);
					}
					
				}else {
					// if STEG/COMPLETE crypted only png files are contained, then decript current folder
					resultOK = deCryptCurrentFile(src, destinationDir);
					break;
				}
			}
			
		}
		
		return resultOK;
	}

///////////////////////////////////////////////////////// GUI-FILE SELECTOR METHODS /////////////////////////////////////////////////////////	
	private void useAsKeyImage(File thisFile) {
		
    	// if a file is selected
		if (thisFile.exists()) {
			try {
				if(FilenameUtils.getExtension(thisFile.getAbsolutePath()).equalsIgnoreCase("png") && ImageIO.read(thisFile)!=null) {
					textAreaLog.setText("");
					String msg = " .png Key recognized ->" + thisFile.getName()+"\n\n ## SIZE: "+FileStuff.coolFileSize(thisFile)+"\n\n ## Path: "+thisFile.getAbsolutePath();
					logger.info(msg);
					textAreaLog.append(msg);	
					
					String newdisplayed = HandleString.fitToMaxLentgh(thisFile.getName(), labelLengthLimit);
					
					lblKeyImageSelected.setForeground(SystemColor.GREEN);
					lblKeyImageSelected.setText(newdisplayed);
					
					srcKeyImageFile = thisFile;
					JOptionPane.showMessageDialog(thisPanel, "Key Image file correctly loaded and selected :)","Key Image Selected :)", JOptionPane.INFORMATION_MESSAGE);
				}else {
					String msg = " File not recognized as Key: \n\n => "+thisFile.getName();
					textAreaLog.append(msg);
					JOptionPane.showMessageDialog(thisPanel, "Key Image file not recognized :(","Key Image not recognized :(", JOptionPane.ERROR_MESSAGE);
				}
			} catch (IOException e) {
				String msg = "Some Error Occurred :(";
				textAreaLog.append(msg);
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
			Image scaledImage = ImageWorker.scaleImage(image, image.getType(), panelKeyImgDisp.getWidth(), panelKeyImgDisp.getHeight());
		
			refreshImage(scaledImage);
			
			if (BufferedImageType.checkIfAdmittedType(image.getType())) {
				
				String msg = "\n\n### Image values ###\n\n Width: " + img.getWidth() + " Px\n Height: "
						+ img.getHeight() + " Px\n\n=> File Size: " + FileStuff.coolFileSize(thisFile)
						+ "\n\n => ImgType: " + img.toString();
				textAreaLog.append(msg);
				logger.info(msg);
				
				String newdisplayed = HandleString.fitToMaxLentgh(thisFile.getName(), labelLengthLimit);

				lblDisplayedKey.setText("Displayed: " + newdisplayed);
			}else {
				
				String msg = "Image type not supported by cripter: ";
				textAreaLog.append(msg);
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
			textAreaLog.append(msg);
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

	private boolean resetAfterException(String msg, Exception e) {
		logger.error(msg, e);
		return false;
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
		btnSelectSourceFile.setEnabled(false);
		btnDispoKeyImg.setEnabled(false);
	}
	
	private void enableSelectionButtons() {
		btnDispoKeyImg.setEnabled(true);
		btnSelectSourceFile.setEnabled(true);
		btnDispoKeyImg.setEnabled(true);
	}
	
	private void appendMessageTxtAreas(String msg) {
		String out = "\n\n## "+msg+" ##\n\n";
		textAreaLog.setSelectionStart(textAreaLog.getText().length()-1);
		textAreaMain.setSelectionStart(textAreaMain.getText().length()-1);
		
		textAreaLog.append(out);
		textAreaMain.append(out);
		tabbedPane.setSelectedIndex(0);
	}
	
	// getters and setters
	
	public JTextArea getTextAreaMain() {
		return textAreaMain;
	}

	public void setTextAreaMain(JTextArea textAreaMain) {
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
}

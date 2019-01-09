package gUI.panels;

import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;

import javax.swing.JTextPane;
import javax.swing.JTree;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import crypt.aes.EncryptDecrypt;
import crypt.manager.CryptoMode;
import gUI.utils.filedialog.GenericFileChooserDialog;
import gUI.utils.tree.FileTreeModel;
import gUI.utils.tree.TreeCellRendererDark;
import gUI.utils.tree.TreeFileMouseListenerCrypter;
import iNIT.INItializer;
import net.miginfocom.swing.MigLayout;
import uTILS.FileStuff;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.border.LineBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.Highlighter;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import java.awt.SystemColor;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.swing.SwingConstants;
import javax.swing.UIDefaults;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTabbedPane;
import javax.swing.JSeparator;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.FlowLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import javax.swing.JButton;
import javax.swing.JProgressBar;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class CrypterPanel extends JPanel {

	// TextFileWriter logger creation
	static Logger logger = Logger.getLogger(CrypterPanel.class);
	
	// static path of keyDictionary
	public static final String KEY_DICTIONARY = KeyGenPanel.KEY_DICTIONARY;
		
	// file representing dictionary
	public static final File KEY_DICTIONARY_FILE = new File(KEY_DICTIONARY);
		
	// Global reference to current configuration
	private INItializer currentConf;
	
	// gui elements
	private JPanel panelHeader;
	private JLabel lblScreeptHeader;
	
	private JTextArea textAreaMain;
	private JTextArea textAreaLog;
	
	private JScrollPane scrollPaneText;
	private JScrollPane scrollPaneLog;
	private JScrollPane scrollPanelTab;
	
	// info tabbed pane
	private JTabbedPane tabbedPane;

	//Jtree used to represent dictionary
	private JTree tree;
	// scroll panel for jtree
	private JScrollPane scrollPaneTree;

	// listener used to refresh file options displayed
	private TreeSelectionListener treeListener;
	private JPanel panelContainerKey;
	
	// tree model to specify root
	private TreeModel treeModel;
	private JLabel lblSelectedModeStatic;
	private JLabel lblAesKeyStatic;
	private JLabel lblSelectedInputStatic;
	private JLabel lblKeyImageSelected;
	private JComboBox comboModeCrypto;
	private JTextField AESKEYtext;
	private JLabel labelFlagCodeReady;
	private JPanel panelStatus;	
	private JFrame parentFrame;
	private JPanel panelUserActions;
	private JLabel lblDisplayedKey;
	private JPanel panelKeyImgDisp;
	private JButton btnDeCreept;
	private JButton btnCreept;
	private JButton btnSelectSourceFile;
	private JButton btnDispoKeyImg;
	private JLabel lblSourceFileStatic;
	private JLabel lblSrcFileSel;
	
	// public field representing current Panel
	public static CrypterPanel thisPanel; 
	
	// field representing the source file loaded by user, this is the file to crypt/decrypt
	private File srcFile;
	// fied representing the img key file selected by user
	private File srcKeyImageFile;

	/**
	 * Create the application.
	 */
	public CrypterPanel(INItializer currentConfiguration, JFrame parent) {
		
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
					EncryptDecrypt.check(AESKEYtext);
					// set the label text according to selected mode
					checkIfReadyToCode();
					
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						logger.error(e.getMessage());
					}

				}
			}
		}).start();
		
		logger.debug("### ScreepT module started");
	}
	
	private void initializeHeader() {
		setLayout(new MigLayout("", "[200:200:200][200:200:200][:10px:10px][130,grow][:80:130,grow][140][120,grow][200,grow 300][70,grow]", "[:40:50px][10px:10px:10px][25][30][100][140,grow][:10px:10px][280,grow]"));
		
		panelHeader = new JPanel();
		panelHeader.setForeground(SystemColor.menu);
		panelHeader.setBorder(new LineBorder(Color.BLACK, 4, true));
		panelHeader.setBackground(new Color(100, 149, 237));
		add(panelHeader, "cell 0 0 9 1,grow");
		panelHeader.setLayout(new MigLayout("", "[50%][50%,grow]", "[50][]"));
		
		lblScreeptHeader = new JLabel("ScreepT - File Crypt & Decrypt");
		lblScreeptHeader.setHorizontalAlignment(SwingConstants.CENTER);
		lblScreeptHeader.setForeground(new Color(0, 0, 0));
		lblScreeptHeader.setFont(new Font("Lithos Pro Regular", Font.BOLD, 24));
		lblScreeptHeader.setBackground(new Color(112, 128, 144));
		panelHeader.add(lblScreeptHeader, "cell 0 0,alignx center");
		
		panelUserActions = new JPanel();
		panelHeader.add(panelUserActions, "cell 1 0,growx");
		panelUserActions.setBorder(new LineBorder(new Color(0, 0, 0), 0));
		panelUserActions.setBackground(new Color(100, 149, 237));
		GridBagLayout gbl_panelUserActions = new GridBagLayout();
		gbl_panelUserActions.columnWidths = new int[]{0, 0, 0, 0, 0};
		gbl_panelUserActions.rowHeights = new int[]{30, 0};
		gbl_panelUserActions.columnWeights = new double[]{1.0, 1.0, 1.0, 1.0, Double.MIN_VALUE};
		gbl_panelUserActions.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		panelUserActions.setLayout(gbl_panelUserActions);
		
		btnSelectSourceFile = new JButton("Select Source File");
		btnSelectSourceFile.setRequestFocusEnabled(false);
		btnSelectSourceFile.setBorder(new LineBorder(new Color(0, 0, 0), 4, true));
		btnSelectSourceFile.setForeground(new Color(255, 99, 71));
		btnSelectSourceFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// read input file with fileChooser
				File selected = GenericFileChooserDialog.fileRead(thisPanel, currentConf);
				if (selected!=null) {
					srcFile = selected;
					
					// update gui
					lblSrcFileSel.setForeground(Color.GREEN);
					lblSrcFileSel.setText(selected.getName());
					
					JOptionPane.showMessageDialog(thisPanel, "Source file correctly loaded",
							"File Loaded :)", JOptionPane.INFORMATION_MESSAGE);
					logger.debug("File :" + selected.getName() + " correctly loaded");
			
				}
			}
		});
		btnSelectSourceFile.setFont(new Font("Lithos Pro Regular", Font.PLAIN, 18));
		btnSelectSourceFile.setBackground(new Color(0, 0, 0));
		GridBagConstraints gbc_btnSelectSourceFile = new GridBagConstraints();
		gbc_btnSelectSourceFile.fill = GridBagConstraints.BOTH;
		gbc_btnSelectSourceFile.insets = new Insets(0, 0, 0, 5);
		gbc_btnSelectSourceFile.gridx = 0;
		gbc_btnSelectSourceFile.gridy = 0;
		panelUserActions.add(btnSelectSourceFile, gbc_btnSelectSourceFile);
		
		btnDispoKeyImg = new JButton("Select Disposable Key Image");
		btnDispoKeyImg.setRequestFocusEnabled(false);
		btnDispoKeyImg.setBorder(new LineBorder(new Color(0, 0, 0), 4, true));
		btnDispoKeyImg.setForeground(Color.BLACK);
		GridBagConstraints gbc_btnDispoKeyImg = new GridBagConstraints();
		gbc_btnDispoKeyImg.fill = GridBagConstraints.BOTH;
		gbc_btnDispoKeyImg.insets = new Insets(0, 0, 0, 5);
		gbc_btnDispoKeyImg.gridx = 1;
		gbc_btnDispoKeyImg.gridy = 0;
		panelUserActions.add(btnDispoKeyImg, gbc_btnDispoKeyImg);
		btnDispoKeyImg.setFont(new Font("Lithos Pro Regular", Font.PLAIN, 18));
		btnDispoKeyImg.setBackground(new Color(105, 105, 105));
		
		btnCreept = new JButton("Creept");
		btnCreept.setRequestFocusEnabled(false);
		btnCreept.setForeground(new Color(240, 255, 240));
		btnCreept.setBorder(new LineBorder(new Color(0, 0, 0), 4, true));
		btnCreept.setFont(new Font("Lithos Pro Regular", Font.PLAIN, 18));
		btnCreept.setBackground(new Color(139, 0, 0));
		GridBagConstraints gbc_btnCreept = new GridBagConstraints();
		gbc_btnCreept.fill = GridBagConstraints.BOTH;
		gbc_btnCreept.insets = new Insets(0, 0, 0, 5);
		gbc_btnCreept.gridx = 2;
		gbc_btnCreept.gridy = 0;
		panelUserActions.add(btnCreept, gbc_btnCreept);
		
		btnDeCreept = new JButton("DeCreept");
		btnDeCreept.setRequestFocusEnabled(false);
		btnDeCreept.setForeground(new Color(240, 255, 240));
		btnDeCreept.setBorder(new LineBorder(new Color(0, 0, 0), 4, true));
		btnDeCreept.setBackground(new Color(139, 0, 0));
		btnDeCreept.setFont(new Font("Lithos Pro Regular", Font.PLAIN, 18));
		GridBagConstraints gbc_btnDeCreept = new GridBagConstraints();
		gbc_btnDeCreept.fill = GridBagConstraints.BOTH;
		gbc_btnDeCreept.gridx = 3;
		gbc_btnDeCreept.gridy = 0;
		panelUserActions.add(btnDeCreept, gbc_btnDeCreept);
		scrollPanelTab = new JScrollPane();
		scrollPanelTab.setBorder(new LineBorder(new Color(0, 0, 0)));
		scrollPanelTab.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPanelTab.setViewportBorder(new LineBorder(new Color(0, 0, 0), 2));
		scrollPanelTab.getViewport().setBackground(Color.DARK_GRAY);
		
		panelStatus = new JPanel();
		panelStatus.setBorder(new LineBorder(new Color(0, 0, 0), 2));
		panelStatus.setBackground(Color.DARK_GRAY);
		add(panelStatus, "cell 3 2 6 1,grow");
		GridBagLayout gbl_panelStatus = new GridBagLayout();
		gbl_panelStatus.columnWidths = new int[]{100, 0, 120, 140, 70, 150, 70, 100, 100, 0};
		gbl_panelStatus.rowHeights = new int[]{32, 0};
		gbl_panelStatus.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
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
		lblSrcFileSel.setForeground(new Color(135, 206, 235));
		lblSrcFileSel.setFont(new Font("SansSerif", Font.BOLD, 13));
		GridBagConstraints gbc_lblSrcFileSel = new GridBagConstraints();
		gbc_lblSrcFileSel.insets = new Insets(0, 0, 0, 5);
		gbc_lblSrcFileSel.gridx = 1;
		gbc_lblSrcFileSel.gridy = 0;
		panelStatus.add(lblSrcFileSel, gbc_lblSrcFileSel);
		
		lblSelectedInputStatic = new JLabel("Key Image:");
		lblSelectedInputStatic.setForeground(new Color(255, 239, 213));
		GridBagConstraints gbc_lblSelectedInputStatic = new GridBagConstraints();
		gbc_lblSelectedInputStatic.insets = new Insets(0, 0, 0, 5);
		gbc_lblSelectedInputStatic.gridx = 2;
		gbc_lblSelectedInputStatic.gridy = 0;
		panelStatus.add(lblSelectedInputStatic, gbc_lblSelectedInputStatic);
		lblSelectedInputStatic.setFont(new Font("Segoe UI", Font.BOLD, 14));
		
		lblKeyImageSelected = new JLabel("No file selected");
		GridBagConstraints gbc_lblKeyImageSelected = new GridBagConstraints();
		gbc_lblKeyImageSelected.insets = new Insets(0, 0, 0, 5);
		gbc_lblKeyImageSelected.gridx = 3;
		gbc_lblKeyImageSelected.gridy = 0;
		panelStatus.add(lblKeyImageSelected, gbc_lblKeyImageSelected);
		lblKeyImageSelected.setFont(new Font("SansSerif", Font.BOLD, 13));
		lblKeyImageSelected.setForeground(new Color(135, 206, 235));
		
		lblAesKeyStatic = new JLabel("AES key:");
		lblAesKeyStatic.setForeground(new Color(255, 239, 213));
		GridBagConstraints gbc_lblAesKeyStatic = new GridBagConstraints();
		gbc_lblAesKeyStatic.insets = new Insets(0, 0, 0, 5);
		gbc_lblAesKeyStatic.gridx = 4;
		gbc_lblAesKeyStatic.gridy = 0;
		panelStatus.add(lblAesKeyStatic, gbc_lblAesKeyStatic);
		lblAesKeyStatic.setFont(new Font("Segoe UI", Font.BOLD, 14));
		
		AESKEYtext = new JTextField();
		AESKEYtext.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc_AESKEYtext = new GridBagConstraints();
		gbc_AESKEYtext.fill = GridBagConstraints.HORIZONTAL;
		gbc_AESKEYtext.insets = new Insets(0, 0, 0, 5);
		gbc_AESKEYtext.gridx = 5;
		gbc_AESKEYtext.gridy = 0;
		panelStatus.add(AESKEYtext, gbc_AESKEYtext);
		AESKEYtext.setColumns(10);
		
		lblSelectedModeStatic = new JLabel("Mode:");
		lblSelectedModeStatic.setForeground(new Color(255, 239, 213));
		GridBagConstraints gbc_lblSelectedModeStatic = new GridBagConstraints();
		gbc_lblSelectedModeStatic.insets = new Insets(0, 0, 0, 5);
		gbc_lblSelectedModeStatic.gridx = 6;
		gbc_lblSelectedModeStatic.gridy = 0;
		panelStatus.add(lblSelectedModeStatic, gbc_lblSelectedModeStatic);
		lblSelectedModeStatic.setFont(new Font("Segoe UI", Font.BOLD, 14));
		
		comboModeCrypto = new JComboBox();
		GridBagConstraints gbc_comboModeCrypto = new GridBagConstraints();
		gbc_comboModeCrypto.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboModeCrypto.insets = new Insets(0, 0, 0, 5);
		gbc_comboModeCrypto.gridx = 7;
		gbc_comboModeCrypto.gridy = 0;
		panelStatus.add(comboModeCrypto, gbc_comboModeCrypto);
		comboModeCrypto.setModel(new DefaultComboBoxModel(CryptoMode.values()));
		
		labelFlagCodeReady = new JLabel("");
		labelFlagCodeReady.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc_labelFlagCodeReady = new GridBagConstraints();
		gbc_labelFlagCodeReady.fill = GridBagConstraints.VERTICAL;
		gbc_labelFlagCodeReady.gridx = 8;
		gbc_labelFlagCodeReady.gridy = 0;
		panelStatus.add(labelFlagCodeReady, gbc_labelFlagCodeReady);
		labelFlagCodeReady.setBorder(null);
		labelFlagCodeReady.setFont(new Font("SansSerif", Font.BOLD, 15));
		add(scrollPanelTab, "cell 0 2 2 4,grow");
		
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBorder(new LineBorder(new Color(0, 0, 0), 0));
		tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		scrollPanelTab.setViewportView(tabbedPane);
	}


	private void initializeTextAreas() {
		scrollPaneLog = new JScrollPane();
		scrollPaneLog.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		tabbedPane.addTab("Screept Log", null, scrollPaneLog, null);
		tabbedPane.setBackgroundAt(0, new Color(216, 191, 216));
		scrollPaneLog.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		
		textAreaLog = new JTextArea();
		textAreaLog.setRequestFocusEnabled(false);
		textAreaLog.setLineWrap(true);
		textAreaLog.setMargin(new Insets(10, 35, 10, 35));
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
		scrollPaneText.setBorder(new LineBorder(new Color(0, 0, 0), 2));
		add(scrollPaneText, "cell 3 3 6 5,grow");
		
		textAreaMain = new JTextArea();
		textAreaMain.setRequestFocusEnabled(false);
		textAreaMain.setFont(new Font("SansSerif", Font.PLAIN, 15));
		textAreaMain.setBackground(SystemColor.menu);
		textAreaMain.setEditable(false);
		textAreaMain.setMargin(new Insets(10, 35, 10, 35));
		scrollPaneText.setViewportView(textAreaMain);
		textAreaMain.setText("### WELCOME TO ScreepT :) ###\r\n\r\n Are you ready to crypt/decrypt your files?\r\n\r\n Nobody knows how to crypt them better than me ;)"); 

	}
	
	public void configureNstartTree() {
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
		// initialize tree 
		
		scrollPaneTree = new JScrollPane();
		scrollPaneTree.setBorder(new LineBorder(new Color(0, 0, 0), 2));
		add(scrollPaneTree, "cell 0 7 2 1,grow");
		tree= new JTree(treeModel);
		tree.setRequestFocusEnabled(false);
		tree.setFont(new Font("SansSerif", Font.BOLD, 14));
	}
	
	public void initializeTree() {
		
		tree.setVisibleRowCount(10);		
		// set custom graphic renderer to jtree
	    // tree.setCellRenderer(new TreeCellRendererDark());
		
		tree.setBackground(Color.DARK_GRAY);
		UIDefaults paneDefaults = new UIDefaults();
	    paneDefaults.put("Tree.selectionBackground",Color.BLACK);
		tree.putClientProperty("Nimbus.Overrides",paneDefaults);
		tree.putClientProperty("Nimbus.Overrides.InheritDefaults",false);
		scrollPaneTree.setViewportView(tree);
		tree.addTreeSelectionListener(treeListener);
		TreeFileMouseListenerCrypter menuListener = new TreeFileMouseListenerCrypter(currentConf, thisPanel);
	}
	
	private void initialize(INItializer config) {
		currentConf = config;
		
		AESKEYtext.setText(config.getAesOpt().getAesKey());

	}
	
	private synchronized boolean checkIfReadyToCode() {
		CryptoMode selectedMode = (CryptoMode)comboModeCrypto.getSelectedItem();
		
		
		// check the operation requested according to combo box value     
		//(see enum custom type)
		boolean ready = true;
		String checkEsit = "";
		
		if(srcFile == null || lblSrcFileSel.getText().equals("No file selected")) {
			// MISSING source file
			labelFlagCodeReady.setForeground(Color.orange);
			checkEsit += " - Source File missing - ";
			ready = false;
		}
		
		if(srcKeyImageFile==null && selectedMode.isSTEG()) {
			// INVALID FILE FORMAT
			labelFlagCodeReady.setForeground(Color.orange);
			checkEsit += " - Missing IMGkey - ";
			ready = false;
		}
		
		if(AESKEYtext.getText().equals("") && selectedMode.isAES()) {
			// MISSING AES KEY
			labelFlagCodeReady.setForeground(Color.orange);
			checkEsit += " - AES missing - ";
			ready = false;
		}
		
		if(ready) {
			// ready!
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
}

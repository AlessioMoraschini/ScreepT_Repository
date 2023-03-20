/**
 *
 * =========================================================================================
 *  Copyright (C) 2019-2020
 *
 *  AM-Design-Development - (Alessio Moraschini) - All Rights Reserved
 * =========================================================================================
 *
 * You should have received a copy of the license with this file.
 * If not, please write to: info@am-design-development.com, or visit : https://www.am-design-development.com
 */
package screept.gui.frames;

import resources.GeneralConfig;
import javax.swing.JFrame;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import gui.commons.dialogutils.JOptionHelperExtended;
import gui.commons.frameutils.frame.arch.ParentFrame;
import gui.commons.frameutils.utils.GuiUtilsExtended;
import gui.commons.treeutils.om.FileTreeModel;
import initializer.configs.impl.INItializer;
import maingUI.MainGUI;
import net.miginfocom.swing.MigLayout;
import resources.IconsPathConfigurator;
import screept.gui.utils.tree.keydictionary.TreeFileMouseListenerDictionary;
import various.common.light.files.FileWorker;
import various.common.light.om.BufferedImageType;
import various.common.light.utility.manipulation.ConversionUtils;

import javax.swing.JPanel;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.swing.border.LineBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import java.awt.Color;

import javax.swing.JLabel;

import java.awt.Font;
import javax.swing.SwingConstants;
import javax.swing.UIDefaults;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.JCheckBox;
import javax.swing.ScrollPaneConstants;
import java.awt.Cursor;
import java.awt.Dimension;

public class KeyDictionaryFrame extends ParentFrame{
	private static final long serialVersionUID = -2183390950352993867L;
	
	public static KeyDictionaryFrame singletonInstance = null;

	// KeyGenPanel logger
	static Logger logger = Logger.getLogger(KeyDictionaryFrame.class); 
	
	// static path of keyDictionary
	public static final String KEY_DICTIONARY = KeyGenFrame.KEY_DICTIONARY;
	
	// file representing dictionary
	public static final File KEY_DICTIONARY_FILE = new File(KEY_DICTIONARY);
	
	//INItializer confuguration
	public INItializer currentConf;
	
	// listener used to refresh file options displayed
	public TreeSelectionListener treeListener;
	//Jtree used to represent dictionary
	private JTree tree;
	
	// same for scrollpane
	public JScrollPane scrollPane;
	
	JFrame thisPanel;
	
	private JOptionHelperExtended dialogHelper;
	
	// constructor
	public KeyDictionaryFrame(INItializer currentConfiguration) {
		setBackground(new Color(255, 228, 225));
		
		logger.info(this.getClass().getName() + " Starting...");
		
		isActive.set(true);
		
		dialogHelper = new JOptionHelperExtended(this);
		
		// SW window icon
		GuiUtilsExtended.setFrameIcon(IconsPathConfigurator.ICON_KEY_DICT, this);
		GuiUtilsExtended.showOnSameScreen(this, MainGUI.mainFrameScreept);
		
		thisPanel = this;
		singletonInstance = this;
		
		// initialize configuration
		currentConf = currentConfiguration;
		
		logger.debug("## KeyDictionary panel started!");
		getContentPane().setBackground(new Color(211, 211, 211));
		getContentPane().setLayout(new MigLayout(GeneralConfig.DEBUG_GRAPHICS+"", "[88.00px][93.00][89.00px][10.00px][10px:100:500,grow 500][:100:10%][:100:8%][:100:8%][:100:14%]", "[35px][:25:25px][40%,grow]"));
		
		JPanel panel = new JPanel();
		panel.setForeground(SystemColor.menu);
		panel.setBorder(new LineBorder(new Color(0, 0, 0), 3));
		panel.setBackground(new Color(176, 196, 222));
		getContentPane().add(panel, "cell 0 0 9 1,growx,aligny center");
		panel.setLayout(new MigLayout(GeneralConfig.DEBUG_GRAPHICS+"", "[][][][][][][][][][grow][][][]", "[]"));
		
		JLabel lblKeyDictionary = new JLabel("Key Dictionary");
		lblKeyDictionary.setHorizontalAlignment(SwingConstants.CENTER);
		lblKeyDictionary.setForeground(Color.BLACK);
		lblKeyDictionary.setFont(new Font("Lithos Pro Regular", Font.BOLD, 22));
		lblKeyDictionary.setBackground(SystemColor.activeCaptionBorder);
		panel.add(lblKeyDictionary, "cell 1 0 12 1,alignx center");
		
		JLabel labelPathAd = new JLabel("");
		getContentPane().add(labelPathAd, "cell 0 1 9 1,alignx center");
		labelPathAd.setFont(new Font("SansSerif", Font.PLAIN, 13));
		labelPathAd.setForeground(new Color(0, 0, 128));
		labelPathAd.setText("Dictionary Path: ..\\ScreepT_Folder\\"+KEY_DICTIONARY_FILE.getPath());
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new LineBorder(new Color(0, 0, 0), 2));
		panel_1.setBackground(Color.DARK_GRAY);
		getContentPane().add(panel_1, "cell 0 2 9 1,grow");
		panel_1.setLayout(new MigLayout(GeneralConfig.DEBUG_GRAPHICS+"", "[:10px:10px][:50:50px][:100:60%,grow][:10px:10px][][10][][5%][20px:12.5%,grow]", "[:20px:30px][20][20][20][20][10,grow]"));
		
		
		scrollPane = new JScrollPane();
		panel_1.add(scrollPane, "cell 4 1 5 5,grow");
		
		
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		panel_1.add(scrollPane_1, "cell 0 5 4 1,grow");
		
		JTextArea textArea = new JTextArea();
		textArea.setLineWrap(true);
		textArea.setEditable(false);
		textArea.setText("\n\n ### WELCOME TO KEY DICTIONARY ###\n\n"
				+ " Select a file from the list to see details ;)");
		textArea.setFont(new Font("SansSerif", Font.PLAIN, currentConfiguration.getGuiOpt().getLogFontSize()));
		textArea.setBackground(new Color(255, 222, 173));
		scrollPane_1.setViewportView(textArea);
		
		JCheckBox chkOverwrite = new JCheckBox("Overwrite Dictionary files");
		chkOverwrite.setOpaque(false);
		chkOverwrite.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		chkOverwrite.setFont(new Font("SansSerif", Font.BOLD, 12));
		chkOverwrite.setForeground(Color.LIGHT_GRAY);
		chkOverwrite.setSelected(currentConf.getFileOpt().isFlagOverwrite());
		chkOverwrite.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(chkOverwrite.isSelected()) {
					currentConf.getFileOpt().setFlagOverwrite(true);
				}else {
					currentConf.getFileOpt().setFlagOverwrite(false);
				}
			}
		});
		
		TreeModel treeModel = new FileTreeModel(KEY_DICTIONARY_FILE);
		tree = new JTree(treeModel);
		tree.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		tree.setBackground(Color.GRAY);
		tree.setVisibleRowCount(10);
		scrollPane.setViewportView(tree);
		treeListener = new TreeSelectionListener() {
			
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				String msg;
				File thisFile = (File)tree.getLastSelectedPathComponent();
				if (thisFile!=null) {
					if (thisFile.isDirectory()) {

					} else {
						if (FilenameUtils.getExtension(thisFile.getAbsolutePath()).equals("png")) {
							msg = " .png Key recognized ->" + thisFile.getName()+"\n\n ## SIZE: "+ConversionUtils.coolFileSize(thisFile)+"\n\n ## Path: "+thisFile.getAbsolutePath();
							textArea.setText(msg);

						} else {
							msg = "File not recognized as Key: \n\n => " + thisFile.getName();
							textArea.setText(msg);
						}
					} 
				}
			}
		};
		tree.addTreeSelectionListener(treeListener);
		
		JButton btnReloadImgTree = new JButton("");
		btnReloadImgTree.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnReloadImgTree.setBackground(Color.BLACK);
		btnReloadImgTree.setIcon(new ImageIcon(IconsPathConfigurator.ICON_RELOAD));
		btnReloadImgTree.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tree.setModel(treeModel);
				
				// refresh tree gui
				refreshJTree();
			}
		});
		panel_1.add(btnReloadImgTree, "cell 1 4,alignx center,aligny center");
		panel_1.add(chkOverwrite, "cell 2 4,alignx center");
		
		JButton btnViewInSystem = new JButton("View in System explorer");
		btnViewInSystem.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		panel_1.add(btnViewInSystem, "cell 1 0 2 1,growx");
		btnViewInSystem.setForeground(new Color(211, 211, 211));
		btnViewInSystem.setFont(new Font("SansSerif", Font.BOLD, 15));
		btnViewInSystem.setBackground(new Color(0, 0, 0));
		
		
		btnViewInSystem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				GuiUtilsExtended.openInFileSystem(KEY_DICTIONARY_FILE);
			}
		});
		
		JLabel lblNewLabel = new JLabel("Current files in Dictionary");
		lblNewLabel.setFont(new Font("Dialog", Font.BOLD, 12));
		lblNewLabel.setForeground(Color.WHITE);
		panel_1.add(lblNewLabel, "cell 4 0 5 1,alignx center");
	
		JButton btnLoadKey = new JButton("Add Key Image");
		btnLoadKey.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnLoadKey.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// show input dialog to select the file(gives null in case of no file selected)
				File selected = fileRead(thisPanel);
				if (selected!=null && selected.getName().toLowerCase().endsWith("png")) {
					
					try {
						BufferedImage tempImg = ImageIO.read(selected);
						if(BufferedImageType.checkIfAdmittedType(tempImg.getType())) {
							
							if (FileWorker.copyFile(selected, new File(KEY_DICTIONARY + selected.getName()),currentConf.getFileOpt().isFlagOverwrite()) != null) {
								dialogHelper.info("File correctly saved in " + KEY_DICTIONARY, "File added to dictionary :)");
								logger.debug("File :" + selected.getName() + " added in the dictionary -> " + KEY_DICTIONARY);
							}else{
								erorrLogNmsg();
							}
							refreshJTree();
							
						}else {
							erorrLogNmsg();
						}
					} catch (IOException e1) {
						logger.error("IO error retrieving image from selected file", e1);
						erorrLogNmsg();
					}
				} else {
					erorrLogNmsg();
				}
			}
		});
		panel_1.add(btnLoadKey, "cell 1 1 2 1,grow");
		btnLoadKey.setBackground(Color.GRAY);
		btnLoadKey.setFont(new Font("SansSerif", Font.BOLD, 13));
		
		JButton btnImportDictionary = new JButton("Import Dictionary");
		btnImportDictionary.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		panel_1.add(btnImportDictionary, "cell 1 2 2 1,growx");
		btnImportDictionary.setBackground(Color.GRAY);
		btnImportDictionary.setFont(new Font("SansSerif", Font.BOLD, 13));
		btnImportDictionary.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				File selectedImportDir = directoRead(thisPanel);
				
				if (selectedImportDir!=null) {
					try {
						FileWorker.copyDirectoryContents(selectedImportDir, KEY_DICTIONARY_FILE, currentConf.getFileOpt().isFlagOverwrite(), "png");
						dialogHelper.info(
								"Directory :" + selectedImportDir.getName() + " added in the dictionary -> " + KEY_DICTIONARY,
								"Directory added to dictionary :)");
						logger.debug("Directory :" + selectedImportDir.getName() + " added in the dictionary -> " + KEY_DICTIONARY);
					} catch (IOException e1) {
						dialogHelper.error("Directory not imported in " + KEY_DICTIONARY, ". Some error occurred :(");
						logger.error("Directory not imported, some error occurred :( ");
					} 
				}
			}
		});
		
		JButton btnExportDictionary = new JButton("Export Dictionary");
		btnExportDictionary.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		panel_1.add(btnExportDictionary, "cell 1 3 2 1,growx");
		btnExportDictionary.setBackground(Color.GRAY);
		btnExportDictionary.setFont(new Font("SansSerif", Font.BOLD, 13));
		btnExportDictionary.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				File selectedExportDir = directoRead(thisPanel);
				
				if (selectedExportDir!=null) {
					// if input fonder not exists then create it
					if(!selectedExportDir.exists()) {
						selectedExportDir.mkdirs();
					}
					try {
						String msg = "Export completed in Target Directory ->" + selectedExportDir.getAbsolutePath();
						FileWorker.copyDirectoryContents(KEY_DICTIONARY_FILE,selectedExportDir, false, "png");
						dialogHelper.info(msg,"Dictionary exported");
						logger.debug(msg);
					} catch (IOException e1) {
						String msg = "Directory not exported"+e1.toString();
						dialogHelper.error( msg,". Some error occurred :(");
						logger.error(msg);
					} 
				}
			}
		});
		
		// new thread that updates jtree periodically
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				while (true) {
					String backupText = textArea.getText();
					TreePath[] backup = tree.getSelectionPaths();
					tree.setModel(treeModel);
					// refresh tree gui
					refreshJTree();
					tree.setSelectionPaths(backup);
					textArea.setText(backupText);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
		
		addHandlers();
		
		new TreeFileMouseListenerDictionary(currentConfiguration, thisPanel, tree, textArea, null);
		logger.debug("## KEY DICTIONARY started");

	}
	
	private void addHandlers() {
		// DEFAULT CLOSE ACTIONS 
		addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		    	
		    	isActive.set(false);
		    	singletonInstance = null;
		    	logger.debug("Closing "+getClass().getName());
		    	dispose();
		    }
	    });
	}
	
	/**
	 * this method select a file from the user (only png files),if nothing is selected returns null
	 */
	private File fileRead(JFrame parent) {

		JFileChooser fileChooser = new JFileChooser();		
		fileChooser.setDialogTitle("Load .png File");
		fileChooser.setCurrentDirectory(new File(currentConf.getFileOpt().getLastSrcFolderPath()));		
		File correctFile = null;		
		
		int returnVal = fileChooser.showOpenDialog(parent);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            
        	File selectedFile = fileChooser.getSelectedFile();
        	String givenExtension = FilenameUtils.getExtension(selectedFile.getName());
        	if(selectedFile==null||!givenExtension.equalsIgnoreCase("png")) {
        		dialogHelper.error("Please select a valid '.png' file!", "Wrong File format");
        	}else {
        		correctFile = selectedFile;
        		currentConf.getFileOpt().setLastSrcFolderPath(correctFile.getAbsolutePath());

        	}
        }else {
        	dialogHelper.warn("Skipped after user request.", null);
        }
        return correctFile;
	}
	
	/**
	 * this method select a directory from the user (only directories),if nothing is selected returns null
	 */
	private File directoRead(JFrame parent) {

		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Choose Directory");
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fileChooser.setCurrentDirectory(new File(currentConf.getFileOpt().getLastSrcFolderPath()));		
		File correctFile = null;	
		
		int returnVal = fileChooser.showOpenDialog(parent);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            
        	File selectedFile = fileChooser.getSelectedFile();
        	if(selectedFile==null||!selectedFile.isDirectory()) {
        		dialogHelper.error("Please select a directory!", "Select a directory, please");
        	}else {
        		correctFile = selectedFile;
        		currentConf.getFileOpt().setLastSrcFolderPath(correctFile.getAbsolutePath());
        	}
        }else {
        	dialogHelper.warn("Skipped after user request.", null);
        }
        return correctFile;
	}
	
	/**
	 * this method select a directory from the user (only directories)to write a copy of current
	 * dictionary, if nothing is selected returns null
	 */
	@Deprecated
	protected File directoWrite(JFrame parent) {

		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fileChooser.setCurrentDirectory(new File(currentConf.getFileOpt().getLastDstFolderPath()));		
		File correctFile = null;		
		
		int returnVal = fileChooser.showSaveDialog(parent);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            
        	File selectedFile = fileChooser.getSelectedFile();
        	if(selectedFile==null||!selectedFile.isDirectory()) {
        		dialogHelper.error("Please select a directory!", "Select a directory, please");
        	}else {
        		correctFile = selectedFile;
        		currentConf.getFileOpt().setLastDstFolderPath(correctFile.getAbsolutePath());
        	}
        }else {
        	dialogHelper.warn("Skipped after user request.", null);
        }
        return correctFile;
	}
	
	private void erorrLogNmsg() {
		dialogHelper.error( "File not saved in " + KEY_DICTIONARY,
				"Some error occurred :(");
		logger.error("File not saved, some error occurred :( ");
	}
	
	private void refreshJTree() {

		UIDefaults paneDefaults = new UIDefaults();
	    paneDefaults.put("Tree.selectionBackground",Color.BLACK);
		tree.putClientProperty("Nimbus.Overrides",paneDefaults);
		tree.putClientProperty("Nimbus.Overrides.InheritDefaults",false);
		scrollPane.setViewportView(tree);
		tree.setBackground(Color.GRAY);
		tree.setVisibleRowCount(10);
		tree.addTreeSelectionListener(treeListener);
		tree.revalidate();
		tree.repaint();
	}
	
	@Override
	public Dimension getDefaultDimension() {
		return new Dimension(800, 560);
	}
	
	public static KeyDictionaryFrame getInstance(INItializer currentConfiguration) {
		return singletonInstance == null ? new KeyDictionaryFrame(currentConfiguration): singletonInstance;
	}
}

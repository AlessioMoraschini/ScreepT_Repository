package gUI.panels;

import javax.swing.JFrame;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import gUI.mainGUI;
import gUI.om.BufferedImageType;
import gUI.utils.iconHandler.IconHandler;
import gUI.utils.tree.FileTreeModel;
import gUI.utils.tree.TreeFileMouseListenerDictionary;
import iNIT.INItializer;
import net.miginfocom.swing.MigLayout;
import uTILS.local.FileStuff;

import javax.swing.JPanel;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.swing.border.LineBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import java.awt.Color;
import java.awt.Desktop;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

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
import java.awt.Button;
import javax.swing.JCheckBox;
import javax.swing.ScrollPaneConstants;

public class KeyDictionaryPanel extends JFrame{

	// KeyGenPanel logger
	static Logger logger = Logger.getLogger(KeyDictionaryPanel.class); 
	
	// static path of keyDictionary
	public static final String KEY_DICTIONARY = KeyGenPanel.KEY_DICTIONARY;
	
	// file representing dictionary
	public static final File KEY_DICTIONARY_FILE = new File(KEY_DICTIONARY);
	
	//INItializer confuguration
	public INItializer currentConf;
	
	// listener used to refresh file options displayed
	public static TreeSelectionListener treeListener;
	//Jtree used to represent dictionary
	private JTree tree;
	
	// same for scrollpane
	public JScrollPane scrollPane;
	
	JFrame thisPanel;
	// flag to signal active
	public static boolean active = false;
	
	// constructor
	public KeyDictionaryPanel(INItializer currentConfiguration) {
		
		logger.info(this.getClass().getName() + " Starting...");
		
		active = true;
		
		// SW window icon
		IconHandler.setFrameIcon(mainGUI.SW_ICON, this);
		
		thisPanel = this;
		// initialize configuration
		currentConf = currentConfiguration;
		
		logger.debug("## KeyDictionary panel started!");
		getContentPane().setBackground(new Color(250, 240, 230));
		getContentPane().setLayout(new MigLayout("", "[88.00px][93.00][89.00px][10.00px][10px:100:500,grow 500][:100:10%][:100:8%][:100:8%][:100:14%]", "[35px][:25:25px][40%,grow]"));
		
		JPanel panel = new JPanel();
		panel.setForeground(SystemColor.menu);
		panel.setBorder(new LineBorder(new Color(0, 0, 0), 3));
		panel.setBackground(SystemColor.activeCaption);
		getContentPane().add(panel, "cell 0 0 9 1,growx,aligny center");
		panel.setLayout(new MigLayout("", "[][][][][][][][][][grow][][][]", "[]"));
		
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
		labelPathAd.setText("Dictionary Path: ..\\ScrYpt_Folder\\"+KEY_DICTIONARY_FILE.getPath());
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new LineBorder(new Color(0, 0, 0), 2));
		panel_1.setBackground(Color.DARK_GRAY);
		getContentPane().add(panel_1, "cell 0 2 9 1,grow");
		panel_1.setLayout(new MigLayout("", "[:10px:10px][:50:50px][:100:60%,grow][:10px:10px][][10][][5%][20px:12.5%,grow]", "[:20px:30px][20][20][20][20][10,grow]"));
		
		
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
		tree.setBackground(Color.GRAY);
		tree.setVisibleRowCount(10);
		scrollPane.setViewportView(tree);
		treeListener = new TreeSelectionListener() {
			
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				TreeFileMouseListenerDictionary menuListener = new TreeFileMouseListenerDictionary(currentConfiguration, thisPanel, tree, textArea, null);
				String msg;
				File thisFile = (File)tree.getLastSelectedPathComponent();
				if (thisFile!=null) {
					if (thisFile.isDirectory()) {

					} else {
						if (FilenameUtils.getExtension(thisFile.getAbsolutePath()).equals("png")) {
							msg = " .png Key recognized ->" + thisFile.getName()+"\n\n ## SIZE: "+FileStuff.coolFileSize(thisFile)+"\n\n ## Path: "+thisFile.getAbsolutePath();
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
		btnReloadImgTree.setBackground(Color.BLACK);
		btnReloadImgTree.setIcon(new ImageIcon(ChargeIniLog.ICON_RELOAD_BLUE));
		btnReloadImgTree.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tree=new JTree(treeModel);
				
				// refresh tree gui
				refreshJTree();
			}
		});
		panel_1.add(btnReloadImgTree, "cell 1 4,alignx center,aligny center");
		panel_1.add(chkOverwrite, "cell 2 4,alignx center");
		
		JButton btnViewInSystem = new JButton("View in System explorer");
		panel_1.add(btnViewInSystem, "cell 1 0 2 1,growx");
		btnViewInSystem.setForeground(new Color(211, 211, 211));
		btnViewInSystem.setFont(new Font("SansSerif", Font.BOLD, 15));
		btnViewInSystem.setBackground(new Color(0, 0, 0));
		
		
		btnViewInSystem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					Desktop.getDesktop().open(KEY_DICTIONARY_FILE);
					logger.debug(KEY_DICTIONARY_FILE.getAbsolutePath()+" Opened in system explorer");
					
					// alternative way to open in system explorer
					//Runtime.getRuntime().exec("explorer.exe /select," + KEY_DICTIONARY_FILE.getAbsolutePath());
				} catch (IOException e1) {
				logger.error("Error opening folder in system explorer !"+e1.toString());
				}				
			}
		});
		
		JLabel lblNewLabel = new JLabel("Current files in Dictionary");
		lblNewLabel.setFont(new Font("Dialog", Font.BOLD, 12));
		lblNewLabel.setForeground(Color.WHITE);
		panel_1.add(lblNewLabel, "cell 4 0 5 1,alignx center");
	
		JButton btnLoadKey = new JButton("Add Key Image");
		btnLoadKey.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// show input dialog to select the file(gives null in case of no file selected)
				File selected = fileRead(thisPanel);
				if (selected!=null && selected.getName().toLowerCase().endsWith("png")) {
					
					try {
						BufferedImage tempImg = ImageIO.read(selected);
						if(BufferedImageType.checkIfAdmittedType(tempImg.getType())) {
							
							if (FileStuff.copyFile(selected, new File(KEY_DICTIONARY + selected.getName()),currentConf.getFileOpt().isFlagOverwrite())) {
								JOptionPane.showMessageDialog(thisPanel, "File correctly saved in " + KEY_DICTIONARY,
										"File added to dictionary :)", JOptionPane.INFORMATION_MESSAGE);
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
		panel_1.add(btnImportDictionary, "cell 1 2 2 1,growx");
		btnImportDictionary.setBackground(Color.GRAY);
		btnImportDictionary.setFont(new Font("SansSerif", Font.BOLD, 13));
		btnImportDictionary.addActionListener(new ActionListener() {
			
			// TODO check image type and extension of file before to load! 
			
			@Override
			public void actionPerformed(ActionEvent e) {
				File selectedImportDir = directoRead(thisPanel);
				
				if (selectedImportDir!=null) {
					try {
						FileStuff.copyDirectoryContents(selectedImportDir, KEY_DICTIONARY_FILE, currentConf.getFileOpt().isFlagOverwrite(), "png");
						JOptionPane.showMessageDialog(thisPanel,
								"Directory :" + selectedImportDir.getName() + " added in the dictionary -> "
										+ KEY_DICTIONARY,
								"Directory added to dictionary :)", JOptionPane.INFORMATION_MESSAGE);
						logger.debug("Directory :" + selectedImportDir.getName() + " added in the dictionary -> "
								+ KEY_DICTIONARY);
					} catch (IOException e1) {
						JOptionPane.showMessageDialog(thisPanel, "Directory not imported in " + KEY_DICTIONARY,
								". Some error occurred :(", JOptionPane.ERROR_MESSAGE);
						logger.error("Directory not imported, some error occurred :( ");
					} 
				}
			}
		});
		
		JButton btnExportDictionary = new JButton("Export Dictionary");
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
						FileStuff.copyDirectoryContents(KEY_DICTIONARY_FILE,selectedExportDir, false, "png");
						JOptionPane.showMessageDialog(thisPanel,msg,"Dictionary exported", JOptionPane.INFORMATION_MESSAGE);
						logger.debug(msg);
					} catch (IOException e1) {
						String msg = "Directory not exported"+e1.toString();
						JOptionPane.showMessageDialog(thisPanel, msg,". Some error occurred :(", JOptionPane.ERROR_MESSAGE);
						logger.error(msg);
					} 
				}
			}
		});
		
		
		// window on close operations
		addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		            active = false;
		        	logger.debug("Closing "+getClass().getName());
		        	dispose();
		    }
		});
		
		// new thread that updates jtree periodically
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				while (true) {
					String backupText = textArea.getText();
					TreePath[] backup = tree.getSelectionPaths();
					tree = new JTree(treeModel);
					// refresh tree gui
					refreshJTree();
					tree.setSelectionPaths(backup);
					textArea.setText(backupText);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}).start();
		
		logger.debug("## KEY DICTIONARY started");

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
        		JOptionPane.showMessageDialog(parent, "Please select a valid '.png' file!", "Wrong File format", JOptionPane.ERROR_MESSAGE);
        	}else {
        		correctFile = selectedFile;
        		currentConf.getFileOpt().setLastSrcFolderPath(correctFile.getAbsolutePath());

        	}
        }else {
        	JOptionPane.showMessageDialog(parent, "Skipped after user request.");
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
        		JOptionPane.showMessageDialog(parent, "Please select a directory!", "Select a directory, please", JOptionPane.ERROR_MESSAGE);
        	}else {
        		correctFile = selectedFile;
        		currentConf.getFileOpt().setLastSrcFolderPath(correctFile.getAbsolutePath());
        	}
        }else {
        	JOptionPane.showMessageDialog(parent, "Skipped after user request.");
        }
        return correctFile;
	}
	
	/**
	 * this method select a directory from the user (only directories)to write a copy of current
	 * dictionary, if nothing is selected returns null
	 */
	private File directoWrite(JFrame parent) {

		
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fileChooser.setCurrentDirectory(new File(currentConf.getFileOpt().getLastDstFolderPath()));		
		File correctFile = null;		
		
		int returnVal = fileChooser.showSaveDialog(parent);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            
        	File selectedFile = fileChooser.getSelectedFile();
        	if(selectedFile==null||!selectedFile.isDirectory()) {
        		JOptionPane.showMessageDialog(parent, "Please select a directory!", "Select a directory, please", JOptionPane.ERROR_MESSAGE);
        	}else {
        		correctFile = selectedFile;
        		currentConf.getFileOpt().setLastDstFolderPath(correctFile.getAbsolutePath());
        	}
        }else {
        	JOptionPane.showMessageDialog(parent, "Skipped after user request.");
        }
        return correctFile;
	}
	
	private void erorrLogNmsg() {
		JOptionPane.showMessageDialog(thisPanel, "File not saved in " + KEY_DICTIONARY,
				"Some error occurred :(", JOptionPane.ERROR_MESSAGE);
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
}



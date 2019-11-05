package gUI.utils.tree.crypter;

import java.awt.Color;
import java.awt.Image;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.swing.tree.TreePath;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import amfileutils.FileStuff;
import dialogutils.JOptionHelper;
import dialogutils.TestUpdateProgress;
import gUI.frames.KeyGenFrame;
import gUI.panels.CrypterPanel;
import mainobj.INItializer;
import media.ImageGeneralWorker;
import menusettings.MenuSetter;
import resources.GeneralConfig;
import resources.IconsPathConfigurator;
import variousutils.HandleString;

public class TreeFileMouseListenerCrypter extends mouseutils.TreeFileMouseListenerBasic{
	
	// KeyGenPanel logger
	static Logger logger = Logger.getLogger(TreeFileMouseListenerCrypter.class); 
	
	// gui elements
	private JTextArea TEXT_LOG = new JTextArea();
	private JFrame mainFrame;
	private INItializer currentConfig;
	private JPanel imgKeyPanel;
	private JLabel lblKeyDisp;
	private JLabel imgKeySelected;
	
	private CrypterPanel cryptoPanel;
	
	// object that display wait dialog
	private volatile TestUpdateProgress progUpdater;
	
	private JOptionHelper dialogHelper;
	
	// constructor
	public TreeFileMouseListenerCrypter(INItializer currentConf, CrypterPanel cryptPanel){
		
		cryptoPanel = cryptPanel;
		this.imgKeyPanel = cryptPanel.getPanelKeyImgDisp();
		lblKeyDisp = cryptPanel.getLblDisplayedKey();
		imgKeySelected = cryptPanel.getLblKeyImageSelected();
		currentConfig = currentConf;
		mainFrame = cryptPanel.getParentFrame();
		TEXT_LOG = cryptPanel.getTextAreaLog();
		tree = cryptPanel.getTree();
		tree.setComponentPopupMenu(getPopUpMenu());
		tree.addMouseListener(getMouseListener());
		
		dialogHelper = new JOptionHelper(cryptPanel);
	}
	
	public MouseListener getMouseListener() {
	    return new MouseAdapter() {

	        @Override
	        public void mousePressed(MouseEvent arg0) {
	            if(arg0.getButton() == MouseEvent.BUTTON3){
	                TreePath pathForLocation = tree.getPathForLocation(arg0.getPoint().x, arg0.getPoint().y);
	                if(pathForLocation != null){
	                    selectedNode = (File) pathForLocation.getLastPathComponent();
	                } else{
	                    selectedNode = null;
	                }

	            }
	            super.mousePressed(arg0);
	        }
	    };
	}

	@Override
	public JPopupMenu getPopUpMenu() {
	    JPopupMenu menu = new JPopupMenu();
	    
	    JMenuItem itemReadKey = new JMenuItem("Load Image & Details");
	    itemReadKey.setFont(GeneralConfig.DEFAULT_POP_UP_MENUS_FONT);
	    itemReadKey.addActionListener(getInfoActionListener());
	    menu.add(itemReadKey);
	    
	    JMenuItem itemUseKey = new JMenuItem("Use as Key");
	    itemUseKey.setFont(GeneralConfig.DEFAULT_POP_UP_MENUS_FONT);
	    itemUseKey.addActionListener(getSelectKeyImgListener());
	    menu.add(itemUseKey);
	    
	    JMenuItem itemAddKey = new JMenuItem("Add key Image to Dictionary");
	    itemAddKey.setFont(GeneralConfig.DEFAULT_POP_UP_MENUS_FONT);
	    itemAddKey.addActionListener(addKeyActionListener());
	    menu.add(itemAddKey);
	    
	    JMenuItem itemExportKeys = new JMenuItem("Export selected key Images");
	    itemExportKeys.setFont(GeneralConfig.DEFAULT_POP_UP_MENUS_FONT);
	    itemExportKeys.addActionListener(exportKeyActionListener());
	    menu.add(itemExportKeys);
	    
	    JMenuItem itemDelete = new JMenuItem("Delete selected Key Images");
	    itemDelete.setFont(GeneralConfig.DEFAULT_POP_UP_MENUS_FONT);
	    itemDelete.addActionListener(getDeleteActionListener());
	    menu.add(itemDelete);
	    
	    JMenuItem sysExplorerItem = new JMenuItem("View in system explorer");
	    sysExplorerItem.setFont(GeneralConfig.DEFAULT_POP_UP_MENUS_FONT);
	    sysExplorerItem.addActionListener(openInSystemExplorer());
	    menu.add(sysExplorerItem);
	    
	    JMenuItem propertiesItem = new JMenuItem("File Properties");
	    propertiesItem.setFont(GeneralConfig.DEFAULT_POP_UP_MENUS_FONT);
	    propertiesItem.addActionListener(propertiesListener());
	    menu.add(propertiesItem);
	    
	    // set menu icons
	    MenuSetter.setIconTextGap(menu, 12);
	    
	    itemUseKey.setIcon(new ImageIcon(IconsPathConfigurator.ICON_CHECKMARK));
	    itemReadKey.setIcon(new ImageIcon(IconsPathConfigurator.ICON_LOOK_INFO));
	    sysExplorerItem.setIcon(new ImageIcon(IconsPathConfigurator.iconFolderPath));
	    itemAddKey.setIcon(new ImageIcon(IconsPathConfigurator.ICON_IMPORT));
	    itemExportKeys.setIcon(new ImageIcon(IconsPathConfigurator.ICON_EXPORT_CONF));
	    itemDelete.setIcon(new ImageIcon(IconsPathConfigurator.ICON_DELETE));
	    propertiesItem.setIcon(new ImageIcon(IconsPathConfigurator.ICON_ABOUT));

	    return menu;
	}
	
	// select this file Key image in crypter panel
	private ActionListener getSelectKeyImgListener() {
		return new ActionListener() {

	        @Override
	        public void actionPerformed(ActionEvent arg0) {
	        	
	        	useAsKeyImage();
	        	loadInfo();

	        }
	    };
	}
	
	private ActionListener getInfoActionListener() {
	    return new ActionListener() {

	        @Override
	        public void actionPerformed(ActionEvent arg0) {
	        	
	        	loadInfo();
	        	
	        }
	    };
	}

	@Override
	public ActionListener getDeleteActionListener() {
	    return new ActionListener() {

	        @Override
	        public void actionPerformed(ActionEvent arg0) {

	            if (!cryptoPanel.isCoderUsed()) {
					TreePath[] treeSelArr = tree.getSelectionPaths();
					int i = 0;
					if (treeSelArr.length != 0 && JOptionPane.showConfirmDialog(mainFrame,
							"Are you sure to delete?") == JOptionPane.OK_OPTION) {
						for (TreePath tp : treeSelArr) {
							//		            	String path = tp.toString().replaceAll("\\]| |\\[|", "").replaceAll(",", File.separator);
							String path = tp.getLastPathComponent().toString();
							File selectedFile = new File(path);
							//		        	File selectedFile = (File)tree.getLastSelectedPathComponent(); // only for one file
							logger.debug(path + " Deleted: " + selectedFile.delete());
						}
						String msg = "Files deleted!";
						logger.debug(msg + Arrays.toString(treeSelArr));
						TEXT_LOG.setText("\n\n ### Completed ###\n\n  " + msg);

					} else {
						String msg = "Skipped after user request!";
						JOptionPane.showMessageDialog(mainFrame, msg, "Skipped", JOptionPane.WARNING_MESSAGE);
						logger.debug(msg);
					} 
				}

	        }
	    };
	}
	
	private ActionListener addKeyActionListener() {
		return new ActionListener() {

	        @Override
	        public void actionPerformed(ActionEvent arg0) {

				// show input dialog to select the file(gives null in case of no file selected)
				File selected = fileRead(TEXT_LOG);
				if (selected!=null) {
					
					if(!selected.getName().toLowerCase().endsWith("png")) {
						// If not a png image convert a temporary png file, and use it as source for import
						selected = FileStuff.renameJavaObjFile(new File(GeneralConfig.TEMP_DIRECTORY + selected.getName()));
						ImageGeneralWorker worker;
						try {
							worker = new ImageGeneralWorker(selected);
							BufferedImage image = worker.getImage();
							progUpdater = new TestUpdateProgress(currentConfig, false);
							progUpdater.setUndefinedDuration();
							progUpdater.update("Format not supported as key, Converting image to png...", 10);
							ImageGeneralWorker.convertAndWrite(image, "png", selected);
							progUpdater.update("Completed!", 100);
							progUpdater.forceClose();
						} catch (IOException e) {
							logger.error("An error occurred when trying to convert "+selected+" to png", e);
							dialogHelper.error("An error occurred when trying to convert "+selected+" to png", "An error occurred :(");
							return;
						}
					}
					
					if (FileStuff.copyFile(selected, new File(KeyGenFrame.KEY_DICTIONARY + selected.getName()),currentConfig.getFileOpt().isFlagOverwrite())) {
						dialogHelper.info("File correctly saved in " + KeyGenFrame.KEY_DICTIONARY, "File added to dictionary :)");
						logger.debug("File :" + selected.getName() + " added in the dictionary -> " + KeyGenFrame.KEY_DICTIONARY);
					} else {
						dialogHelper.error("File not saved in " + KeyGenFrame.KEY_DICTIONARY, "An error occurred :(");
						logger.error("File not saved, some error occurred :( ");
					}
				}

	        }
	    };
	}
	
	private ActionListener exportKeyActionListener() {
		return new ActionListener() {

	        @Override
	        public void actionPerformed(ActionEvent arg0) {
	        	
				if (!cryptoPanel.isCoderUsed()) {
					// show input dialog to select the file(gives null in case of no file selected)
					File selected = directoWrite(TEXT_LOG);
					new Thread(new Runnable() {

						@Override
						public void run() {

							if (selected != null) {
								progUpdater = new TestUpdateProgress(currentConfig);
								progUpdater.getPb().setValue(5);
								progUpdater.getLabel().setText("Exporting files...");

								if (!selected.exists()) {
									selected.mkdirs();
								}
								TreePath[] treeSelArr = tree.getSelectionPaths();
								int i = 0;
								if (treeSelArr.length != 0) {
									for (TreePath tp : treeSelArr) {
										i++;
										progUpdater.getPb().setValue((int) ((float) i / (float) treeSelArr.length));
										String path = tp.getLastPathComponent().toString();
										File selectedFile = new File(path);
										//		        	File selectedFile = (File)tree.getLastSelectedPathComponent(); // only for one file
										FileStuff.copyFile(selectedFile,
												new File(selected.getAbsolutePath() + "\\" + selectedFile.getName()),
												false);
										logger.debug(
												selectedFile.getName() + " exported to " + selected.getAbsolutePath());
									}
									String msg = "Files exported!";
									logger.debug(msg + Arrays.toString(treeSelArr));
									TEXT_LOG.setText("\n\n ### Completed ###\n\n  " + msg);
									progUpdater.getPb().setValue(100);
									JOptionPane.showMessageDialog(TEXT_LOG, msg, "Files exported!",
											JOptionPane.INFORMATION_MESSAGE);

								} else {
									progUpdater.getPb().setValue(100);
									String msg = "Skipped after user request!";
									JOptionPane.showMessageDialog(tree, msg, "Skipped", JOptionPane.WARNING_MESSAGE);
									logger.debug(msg);
								}
							}
						}
					}).start();
				}       	
	        }
		};
	}
	
	private void loadInfo() {

        if(selectedNode != null){
			File thisFile = (File)tree.getLastSelectedPathComponent();
			try {
				BufferedImage img = ImageIO.read(thisFile);
				// check if file is an image or not
				if(img == null ) {
					throw new IOException();
				}
				String msg = " .png Key recognized ->" + thisFile.getName()+"\n\n ## SIZE: "+FileStuff.coolFileSize(thisFile)+"\n\n ## Path: "+thisFile.getPath()
							+"\n\n### Image values ###\n\n Width: "+img.getWidth()+" Px\n Height: "+img.getHeight()+" Px\n\n=> File Size: "
							+FileStuff.coolFileSize(thisFile)+"\n\n => ImgType: "+img.toString();
				logger.debug(msg);
				TEXT_LOG.setText(msg);
				
				// read image from file
				BufferedImage image = ImageIO.read(thisFile);
				
				// way with default method from ImageIO
//			    Image scaledImage = image.getScaledInstance(imgKeyPanel.getWidth()-13, imgKeyPanel.getHeight()-13,Image.SCALE_FAST);

				// way with custom repainter (keep type)
				Image scaledImage = ImageGeneralWorker.scaleImage(image, image.getType(), imgKeyPanel.getWidth(), imgKeyPanel.getHeight());
			
				refreshImage(scaledImage);
				
				String newdisplayed = HandleString.fitToMaxLentgh(thisFile.getAbsolutePath(), CrypterPanel.labelLengthLimit);

				lblKeyDisp.setText("Displayed: " + newdisplayed);
				
				cryptoPanel.getTabbedPane().setSelectedIndex(1);

				
			} catch (IOException e1) {
				String msg = "Error reading the image, something went wrong :(";
				logger.debug(msg);
				TEXT_LOG.setText(msg);
				if (cryptoPanel.getSrcKeyImageFile() == null) {
					lblKeyDisp.setText(msg);
					imgKeyPanel.removeAll();
					JLabel errorMsg = new JLabel("Error opening file :(");
					errorMsg.setForeground(Color.white);
					errorMsg.setHorizontalAlignment(JLabel.CENTER);
					errorMsg.setVerticalAlignment(JLabel.CENTER);
					imgKeyPanel.add(errorMsg);
					imgKeyPanel.revalidate();
					imgKeyPanel.repaint();
				}	
			}
        }
	}
	
	private void useAsKeyImage() {
		File thisFile = (File)tree.getLastSelectedPathComponent();
    	// if a file is selected
		if (tree.getLastSelectedPathComponent()!=null && !cryptoPanel.isCoderUsed()) {
			try {
				if(FilenameUtils.getExtension(thisFile.getAbsolutePath()).equalsIgnoreCase("png")&&ImageIO.read(thisFile)!=null) {
					String msg = " .png Key recognized ->" + thisFile.getName()+"\n\n ## SIZE: "+FileStuff.coolFileSize(thisFile)+"\n\n ## Path: "+thisFile.getAbsolutePath();
					TEXT_LOG.setText(msg);	
					imgKeySelected.setForeground(SystemColor.GREEN);
					
					String newdisplayed = HandleString.fitToMaxLentgh(thisFile.getName(), CrypterPanel.labelLengthLimit);

					imgKeySelected.setText(newdisplayed);
					cryptoPanel.setSrcKeyImageFile(thisFile);
					JOptionPane.showMessageDialog(mainFrame, "Key Image file correctly loaded and selected :)",
							"Key Image Selected :)", JOptionPane.INFORMATION_MESSAGE);
				}else {
					String msg = "File not recognized as Key: \n\n => "+thisFile.getName();
					TEXT_LOG.append(msg);
					cryptoPanel.showMessageImageNotSupported();
				}
			} catch (IOException e) {
				String msg = "Some Error Occurred... please reload key image!";
				TEXT_LOG.append(msg);
				imgKeySelected.setForeground(Color.RED);
				imgKeySelected.setText(msg);
				cryptoPanel.setSrcKeyImageFile(null);
				JOptionPane.showMessageDialog(mainFrame, msg, msg, JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	/**
	 * This method is used only locally to refresh image with the given one
	 */
	private void refreshImage(Image image) {
		
		JLabel picLabel = new JLabel(new ImageIcon(image));
		imgKeyPanel.removeAll();
		imgKeyPanel.add(picLabel);
		picLabel.setHorizontalAlignment(JLabel.CENTER);
		picLabel.setVerticalAlignment(JLabel.CENTER);
		imgKeyPanel.revalidate(); 
		imgKeyPanel.repaint();	
	}
	
	/**
	 * this method select a file from the user (only png files),if nothing is selected returns null
	 */
	private File fileRead(JComponent parent) {

		
		JFileChooser fileChooser = new JFileChooser();		
		fileChooser.setDialogTitle("Load .png File");
		File correctFile = null;
		JOptionPane optPane = new JOptionPane();
		
		int returnVal = fileChooser.showOpenDialog(parent);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            
        	File selectedFile = fileChooser.getSelectedFile();
        	String givenExtension = FilenameUtils.getExtension(selectedFile.getName());
        	if(selectedFile==null||!givenExtension.equalsIgnoreCase("png")) {
        		optPane.showMessageDialog(parent, "Please select a valid '.png' file!", "Wrong File format", JOptionPane.ERROR_MESSAGE);
        	}else {
        		correctFile = selectedFile;
        	}
        }else {
        	optPane.showMessageDialog(parent, "Skipped after user request.");
        }
        return correctFile;
	}
	
	/**
	 * this method select a directory from the user (only directories)to write a copy of current
	 * dictionary, if nothing is selected returns null
	 */
	private File directoWrite(JComponent parent) {

		
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fileChooser.setCurrentDirectory(new File(currentConfig.getFileOpt().getLastDstFolderPath()));		
		File correctFile = null;
		JOptionPane optPane = new JOptionPane();
		
		
		int returnVal = fileChooser.showSaveDialog(parent);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            
        	File selectedFile = fileChooser.getSelectedFile();
        	if(selectedFile==null||!selectedFile.isDirectory()) {
        		optPane.showMessageDialog(parent, "Please select a directory!", "Select a directory, please", JOptionPane.ERROR_MESSAGE);
        	}else {
        		correctFile = selectedFile;
        		currentConfig.getFileOpt().setLastDstFolderPath(correctFile.getAbsolutePath());
        	}
        }else {
        	optPane.showMessageDialog(parent, "Skipped after user request.");
        }
        return correctFile;
	}
	
}

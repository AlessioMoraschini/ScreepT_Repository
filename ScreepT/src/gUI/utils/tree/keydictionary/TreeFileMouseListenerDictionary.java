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
package gUI.utils.tree.keydictionary;

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
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import dialogutils.JOptionHelperExtended;
import files.FileWorker;
import gUI.frames.KeyGenFrame;
import gui.MenuSetter;
import gui.UpdateProgressPanel;
import mainobj.INItializer;
import resources.GeneralConfig;
import resources.IconsPathConfigurator;
import utility.ConversionUtils;

public class TreeFileMouseListenerDictionary {
	
	// KeyGenPanel logger
	static Logger logger = Logger.getLogger(TreeFileMouseListenerDictionary.class); 
	
	// gui elements
	private JTree tree;
	private File selectedNode;
	private JTextArea TEXT_LOG = new JTextArea();
	private INItializer currentConfig;
	
	private JOptionHelperExtended dialogHelper;
	
	// object that display wait dialog
	private volatile UpdateProgressPanel progUpdater;

	// constructor
	public TreeFileMouseListenerDictionary(INItializer currentConf, JFrame parent, JTree treeIn, JTextArea log, JTextArea mainTxtArea) {
		currentConfig = currentConf;
		TEXT_LOG = log;
		tree = treeIn;
		tree.setComponentPopupMenu(getPopUpMenu());
		tree.addMouseListener(getMouseListener());
		dialogHelper = new JOptionHelperExtended(parent);
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

	private JPopupMenu getPopUpMenu() {
	    JPopupMenu menu = new JPopupMenu();
	    
	    JMenuItem itemReadKey = new JMenuItem("Read Image & Details");
	    itemReadKey.setFont(GeneralConfig.DEFAULT_POP_UP_MENUS_FONT);
	    itemReadKey.addActionListener(getInfoActionListener());
	    menu.add(itemReadKey);
	    
	    JMenuItem itemAddKey = new JMenuItem("Add key Image to Dictionary");
	    itemAddKey.setFont(GeneralConfig.DEFAULT_POP_UP_MENUS_FONT);
	    itemAddKey.addActionListener(addKeyActionListener());
	    menu.add(itemAddKey);
	    
	    JMenuItem itemExport = new JMenuItem("Export selected key Images");
	    itemExport.setFont(GeneralConfig.DEFAULT_POP_UP_MENUS_FONT);
	    itemExport.addActionListener(exportKeyActionListener());
	    menu.add(itemExport);
	    
	    JMenuItem itemDelete = new JMenuItem("Delete selected Key Images");
	    itemDelete.setFont(GeneralConfig.DEFAULT_POP_UP_MENUS_FONT);
	    itemDelete.addActionListener(getDeleteActionListener());
	    menu.add(itemDelete);

	    // set menu icons
	    MenuSetter.setIconTextGap(menu, 12);
	    
	    itemReadKey.setIcon(new ImageIcon(IconsPathConfigurator.ICON_LOOK_INFO));
	    itemAddKey.setIcon(new ImageIcon(IconsPathConfigurator.ICON_IMPORT));
	    itemExport.setIcon(new ImageIcon(IconsPathConfigurator.ICON_EXPORT_CONF));
	    itemDelete.setIcon(new ImageIcon(IconsPathConfigurator.ICON_DELETE));
	    
	    return menu;
	}

	private ActionListener getInfoActionListener() {
	    return new ActionListener() {

	        @Override
	        public void actionPerformed(ActionEvent arg0) {
	            if(selectedNode != null){
					File thisFile = (File)tree.getLastSelectedPathComponent();
					try {
						BufferedImage img = ImageIO.read(thisFile);
						if(img == null ) {
							throw new IOException();
						}
						String msg = "\n\n### Image values ###\n\n Width: "+img.getWidth()+" Px\n Height: "+img.getHeight()+" Px\n\n=> File Size: "
									+ConversionUtils.coolFileSize(thisFile)+"\n\n => ImgType: "+img.toString();
						logger.debug(msg);
						TEXT_LOG.setText(msg);
					} catch (IOException e1) {
						String msg = "Error reading the image, something went wrong :(";
						logger.debug(msg);
						TEXT_LOG.setText(msg);
					}
	            }
	        }
	    };
	}

	private ActionListener getDeleteActionListener() {
	    return new ActionListener() {

	        @Override
	        public void actionPerformed(ActionEvent arg0) {

	            TreePath[] treeSelArr = tree.getSelectionPaths();
            	if(treeSelArr.length!= 0 && dialogHelper.yesOrNo("Are you sure to delete?")){	        		
		            for(TreePath tp : treeSelArr) {
//		            	String path = tp.toString().replaceAll("\\]| |\\[|", "").replaceAll(",", File.separator);
		            	String path = tp.getLastPathComponent().toString();
		            	File selectedFile = new File(path);
						if (selectedFile.isFile()) {
							logger.debug(path + " Deleted: " + selectedFile.delete());
						}		        		
			        }
		            String msg = "Files deleted!";
	            	logger.debug(msg+Arrays.toString(treeSelArr));
	            	TEXT_LOG.setText("\n\n ### Completed ###\n\n  "+msg);

	            }else {
	            	String msg = "Skipped after user request!";
	            	dialogHelper.info(msg, "Skipped");
			    	logger.debug(msg);
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
					if (FileWorker.copyFile(selected, new File(KeyGenFrame.KEY_DICTIONARY + selected.getName()),currentConfig.getFileOpt().isFlagOverwrite()) != null) {
						dialogHelper.info("File correctly saved in " + KeyGenFrame.KEY_DICTIONARY, "File added to dictionary :)");
						logger.debug("File :" + selected.getName() + " added in the dictionary -> " + KeyGenFrame.KEY_DICTIONARY);
					} else {
						dialogHelper.error("File not saved in " + KeyGenFrame.KEY_DICTIONARY, "An error occurred");
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

	        	// show input dialog to select the file(gives null in case of no file selected)
				File selected = directoWrite(TEXT_LOG);
	        	
	        	new Thread(new Runnable() {
					
					@Override
					public void run() {
						
			    		if (selected!= null) {
								
							if (!selected.exists()) {
								selected.mkdirs();
							}
							TreePath[] treeSelArr = tree.getSelectionPaths();
							int i = 0;
							if (treeSelArr.length != 0) {
								try {
									progUpdater = new UpdateProgressPanel(false);
									progUpdater.getPb().setValue(5);
									for (TreePath tp : treeSelArr) {
										i++;
										progUpdater.getPb().setValue((int)((float)i/(float)treeSelArr.length));
										
										String path = tp.getLastPathComponent().toString();
										File selectedFile = new File(path);
										FileWorker.copyFile(selectedFile,
												new File(selected.getAbsolutePath() + "\\" + selectedFile.getName()), false);
										logger.debug(selectedFile.getName() + " exported to " + selected.getAbsolutePath());
									}
									String msg = "Files exported!";
									logger.debug(msg + Arrays.toString(treeSelArr));
									TEXT_LOG.setText("\n\n ### Completed ###\n\n  " + msg);
									progUpdater.getPb().setValue(100);
									dialogHelper.info(msg, "Files exported!");
								
								} catch (Exception e) {
									logger.error("There was an Exception", e);
								} finally {
									UpdateProgressPanel.forceDisposeSafe(progUpdater);
								}

							} else {
								String msg = "Skipped after user request!";
								dialogHelper.info(msg, "Skipped");
								logger.debug(msg);
							}
						}
					}
				}).start();
	        }
	    };
	}
	
	/**
	 * this method select a file from the user (only png files),if nothing is selected returns null
	 */
	private File fileRead(JComponent parent) {

		
		JFileChooser fileChooser = new JFileChooser();		
		fileChooser.setDialogTitle("Load .png File");
		File correctFile = null;
		
		int returnVal = fileChooser.showOpenDialog(parent);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            
        	File selectedFile = fileChooser.getSelectedFile();
        	String givenExtension = FilenameUtils.getExtension(selectedFile.getName());
        	
        	if(selectedFile==null || !givenExtension.equalsIgnoreCase("png")) {
        		dialogHelper.error("Please select a valid '.png' file!", "Wrong File format");
        	}else {
        		correctFile = selectedFile;

        	}
        }else {
        	dialogHelper.info("Skipped after user request.");
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
		
		int returnVal = fileChooser.showSaveDialog(parent);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            
        	File selectedFile = fileChooser.getSelectedFile();
        	if(selectedFile==null||!selectedFile.isDirectory()) {
        		dialogHelper.error("Please select a directory!", "Select a directory, please");
        	}else {
        		correctFile = selectedFile;
        		currentConfig.getFileOpt().setLastDstFolderPath(correctFile.getAbsolutePath());
        	}
        }else {
        	dialogHelper.info("Skipped after user request.");
        }
        return correctFile;
	}
	
}

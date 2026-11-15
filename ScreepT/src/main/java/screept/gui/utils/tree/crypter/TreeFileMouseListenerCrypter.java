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
package screept.gui.utils.tree.crypter;

import various.common.light.i18n.I18n;
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
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.swing.tree.TreePath;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import gui.commons.dialogutils.NativeFileChooser;
import gui.commons.dialogutils.JOptionHelperExtended;
import gui.commons.frameutils.utils.GuiUtilsExtended;
import gui.commons.utils.ImageWorkerExtended;
import initializer.configs.impl.INItializer;
import resources.GeneralConfig;
import resources.IconsPathConfigurator;
import screept.gui.frames.KeyGenFrame;
import screept.gui.panels.CrypterPanel;
import various.common.light.files.FileVarious;
import various.common.light.files.FileWorker;
import various.common.light.gui.MenuSetter;
import various.common.light.gui.UpdateProgressPanel;
import various.common.light.utility.manipulation.ConversionUtils;
import various.common.light.utility.string.StringWorker;

public class TreeFileMouseListenerCrypter extends gui.commons.mouseutils.TreeFileMouseListenerBasic{
	
	// KeyGenPanel logger
	static Logger logger = Logger.getLogger(TreeFileMouseListenerCrypter.class); 
	
	// gui elements
	private JTextArea TEXT_LOG = new JTextArea();
	private INItializer currentConfig;
	private JPanel imgKeyPanel;
	private JLabel lblKeyDisp;
	private JLabel imgKeySelected;
	
	private CrypterPanel cryptoPanel;
	
	// object that display wait dialog
	private volatile UpdateProgressPanel progUpdater;
	
	private JOptionHelperExtended dialogHelper;
	
	// constructor
	public TreeFileMouseListenerCrypter(INItializer currentConf, CrypterPanel cryptPanel){
		
		cryptoPanel = cryptPanel;
		this.imgKeyPanel = cryptPanel.getPanelKeyImgDisp();
		lblKeyDisp = cryptPanel.getLblDisplayedKey();
		imgKeySelected = cryptPanel.getLblKeyImageSelected();
		currentConfig = currentConf;
		TEXT_LOG = cryptPanel.getTextAreaLog();
		tree = cryptPanel.getTree();
		tree.setComponentPopupMenu(getPopUpMenu());
		tree.addMouseListener(getMouseListener());
		
		dialogHelper = new JOptionHelperExtended(cryptPanel);
	}
	
	public MouseListener getMouseListener() {
	    return new MouseAdapter() {

	        @Override
	        public void mousePressed(MouseEvent arg0) {
                TreePath pathForLocation = tree.getPathForLocation(arg0.getPoint().x, arg0.getPoint().y);
                if(pathForLocation != null && pathForLocation.getLastPathComponent() != null){
                	boolean exists = ((File)pathForLocation.getLastPathComponent()).exists();
                	if(exists) {
                		selectedNode = (File) pathForLocation.getLastPathComponent();
                	} else{
                		selectedNode = null;
                	}
                } else {
                	selectedNode = null;
                }

	            super.mousePressed(arg0);
	        }
	    };
	}

	@Override
	public JPopupMenu getPopUpMenu() {
	    JPopupMenu menu = new JPopupMenu();
	    
	    JMenuItem itemReadKey = new JMenuItem(I18n.t("app.load_image_details"));
	    itemReadKey.setFont(GeneralConfig.DEFAULT_POP_UP_MENUS_FONT);
	    itemReadKey.addActionListener(getInfoActionListener());
	    menu.add(itemReadKey);
	    
	    JMenuItem itemUseKey = new JMenuItem(I18n.t("app.use_as_key"));
	    itemUseKey.setFont(GeneralConfig.DEFAULT_POP_UP_MENUS_FONT);
	    itemUseKey.addActionListener(getSelectKeyImgListener());
	    menu.add(itemUseKey);
	    
	    JMenuItem itemAddKey = new JMenuItem(I18n.t("app.add_key_image_to_dictionary"));
	    itemAddKey.setFont(GeneralConfig.DEFAULT_POP_UP_MENUS_FONT);
	    itemAddKey.addActionListener(addKeyActionListener());
	    menu.add(itemAddKey);
	    
	    JMenuItem itemExportKeys = new JMenuItem(I18n.t("app.export_selected_key_images"));
	    itemExportKeys.setFont(GeneralConfig.DEFAULT_POP_UP_MENUS_FONT);
	    itemExportKeys.addActionListener(exportKeyActionListener());
	    menu.add(itemExportKeys);
	    
	    JMenuItem itemDelete = new JMenuItem(I18n.t("app.delete_selected_key_images"));
	    itemDelete.setFont(GeneralConfig.DEFAULT_POP_UP_MENUS_FONT);
	    itemDelete.addActionListener(getDeleteActionListener());
	    menu.add(itemDelete);
	    
	    JMenuItem sysExplorerItem = new JMenuItem(I18n.t("common.view_in_system_explorer"));
	    sysExplorerItem.setFont(GeneralConfig.DEFAULT_POP_UP_MENUS_FONT);
	    sysExplorerItem.addActionListener(openInSystemExplorer());
	    menu.add(sysExplorerItem);
	    
	    JMenuItem propertiesItem = new JMenuItem(I18n.t("common.file_properties"));
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
	
	@Override
	public ActionListener openInSystemExplorer() {
		return (e) -> {
			if(selectedNode == null) {
				selectedNode = (File)tree.getModel().getRoot();
			}
			GuiUtilsExtended.openInFileSystem(selectedNode);
		};
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

					if (treeSelArr.length != 0 && dialogHelper.yesOrNo(I18n.t("common.are_you_sure_to_delete"))) {
						for (TreePath tp : treeSelArr) {
							//		            	String path = tp.toString().replaceAll("\\]| |\\[|", "").replaceAll(",", File.separator);
							String path = tp.getLastPathComponent().toString();
							File selectedFile = new File(path);
							//		        	File selectedFile = (File)tree.getLastSelectedPathComponent(); // only for one file
							logger.debug(path + " Deleted: " + selectedFile.delete());
						}
						String msg = I18n.t("crypter.files_deleted");
						logger.debug(msg + Arrays.toString(treeSelArr));
						TEXT_LOG.setText("\n\n ### Completed ###\n\n  " + msg);

					} else {
						String msg = I18n.t("crypter.skipped_after_user_request");
						// cancelled by user: no message needed
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

	        	if(cryptoPanel.isCoderUsed())
	    	    	return;
	        	
				// show input dialog to select the file(gives null in case of no file selected)
				File selected = fileRead(TEXT_LOG);
				if (selected!=null) {
					
					if(!selected.getName().toLowerCase().endsWith("png")) {
						// If not a png image convert a temporary png file, and use it as source for import
						selected = FileVarious.uniqueJavaObjFile(new File(GeneralConfig.TEMP_DIRECTORY + selected.getName()));
						ImageWorkerExtended worker;
						try {
							worker = new ImageWorkerExtended(selected);
							BufferedImage image = worker.getImage();
							progUpdater = new UpdateProgressPanel(false);
							progUpdater.setUndefinedDuration();
							progUpdater.update(I18n.t("crypter.format_not_supported_as_key_converting"), 10);
							ImageWorkerExtended.convertAndWrite(image, "png", selected);
						} catch (Exception e) {
							logger.error("An error occurred when trying to convert "+selected+" to png", e);
							dialogHelper.error(I18n.t("crypter.an_error_occurred_when_trying_to")+selected+I18n.t("crypter.to_png"), I18n.t("app.an_error_occurred"));
							return;
						} finally {
							UpdateProgressPanel.forceDisposeSafe(progUpdater);
						}
					}
					
					if (FileWorker.copyFile(selected, new File(KeyGenFrame.KEY_DICTIONARY + selected.getName()),currentConfig.getFileOpt().isFlagOverwrite()) != null) {
						dialogHelper.info(I18n.t("crypter.file_correctly_saved_in") + KeyGenFrame.KEY_DICTIONARY, I18n.t("crypter.file_added_to_dictionary"));
						logger.debug("File :" + selected.getName() + " added in the dictionary -> " + KeyGenFrame.KEY_DICTIONARY);
					} else {
						dialogHelper.error(I18n.t("crypter.file_not_saved_in") + KeyGenFrame.KEY_DICTIONARY, I18n.t("app.an_error_occurred"));
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
	        	
	        	if(cryptoPanel.isCoderUsed())
	    	    	return;
	        	
				// show input dialog to select the file(gives null in case of no file selected)
				File selected = directoWrite(TEXT_LOG);
				new Thread(new Runnable() {

					@Override
					public void run() {

						try {
							if (selected != null) {
								progUpdater = new UpdateProgressPanel(false);
								progUpdater.getPb().setValue(5);
								progUpdater.getLabel().setText(I18n.t("crypter.exporting_files"));

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
										FileWorker.copyFile(selectedFile,
												new File(selected.getAbsolutePath() + "\\" + selectedFile.getName()),
												false);
										logger.debug(
												selectedFile.getName() + I18n.t("crypter.exported_to") + selected.getAbsolutePath());
									}
									String msg = I18n.t("crypter.files_exported");
									logger.debug(msg + Arrays.toString(treeSelArr));
									TEXT_LOG.setText("\n\n ### Completed ###\n\n  " + msg);
									progUpdater.getPb().setValue(100);
									dialogHelper.info(msg, I18n.t("crypter.files_exported"));

								} else {
									progUpdater.getPb().setValue(100);
									String msg = I18n.t("crypter.skipped_after_user_request");
									// cancelled by user: no message needed
									logger.debug(msg);
								}
							}
						} catch (Exception e) {
							logger.error("There was an exception while exporting.", e);
						} finally {
							UpdateProgressPanel.forceDisposeSafe(progUpdater);
						}
					}
				}).start();
			}       	
		};
	}
	
	private void loadInfo() {
		
		if(cryptoPanel.isCoderUsed())
	    	return;

        if(selectedNode != null){
			File thisFile = (File)tree.getLastSelectedPathComponent();
			try {
				BufferedImage img = ImageIO.read(thisFile);
				// check if file is an image or not
				if(img == null ) {
					throw new IOException();
				}
				String msg = I18n.t("crypter.png_key_recognized") + thisFile.getName()+I18n.t("crypter.size")+ConversionUtils.coolFileSize(thisFile)+I18n.t("crypter.path")+thisFile.getPath()
							+I18n.t("crypter.image_values_width")+img.getWidth()+I18n.t("crypter.px_height")+img.getHeight()+I18n.t("crypter.px_file_size")
							+ConversionUtils.coolFileSize(thisFile)+I18n.t("crypter.imgtype")+img.toString();
				logger.debug(msg);
				TEXT_LOG.setText(msg);
				
				// read image from file
				BufferedImage image = ImageIO.read(thisFile);
				
				// way with default method from ImageIO
//			    Image scaledImage = image.getScaledInstance(imgKeyPanel.getWidth()-13, imgKeyPanel.getHeight()-13,Image.SCALE_FAST);

				// way with custom repainter (keep type)
				Image scaledImage = ImageWorkerExtended.scaleImage(image, image.getType(), imgKeyPanel.getWidth(), imgKeyPanel.getHeight());
			
				refreshImage(scaledImage);
				
				String newdisplayed = StringWorker.fitToMaxLentgh(thisFile.getAbsolutePath(), CrypterPanel.labelLengthLimit);

				lblKeyDisp.setText(I18n.t("crypter.displayed") + newdisplayed);
				
				cryptoPanel.getTabbedPane().setSelectedIndex(1);

				
			} catch (IOException e1) {
				String msg = I18n.t("crypter.error_reading_the_image_something_went");
				logger.debug(msg);
				TEXT_LOG.setText(msg);
				if (cryptoPanel.getSrcKeyImageFile() == null) {
					lblKeyDisp.setText(msg);
					imgKeyPanel.removeAll();
					JLabel errorMsg = new JLabel(I18n.t("app.error_opening_file"));
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
		
		if(cryptoPanel.isCoderUsed())
	    	return;
		
		File thisFile = (File)tree.getLastSelectedPathComponent();
    	// if a file is selected
		if (tree.getLastSelectedPathComponent()!=null && !cryptoPanel.isCoderUsed()) {
			try {
				if(FilenameUtils.getExtension(thisFile.getAbsolutePath()).equalsIgnoreCase("png")&&ImageIO.read(thisFile)!=null) {
					String msg = I18n.t("crypter.png_key_recognized") + thisFile.getName()+I18n.t("crypter.size")+ConversionUtils.coolFileSize(thisFile)+I18n.t("crypter.path")+thisFile.getAbsolutePath();
					TEXT_LOG.setText(msg);	
					imgKeySelected.setForeground(SystemColor.GREEN);
					
					String newdisplayed = StringWorker.fitToMaxLentgh(thisFile.getName(), CrypterPanel.labelLengthLimit);

					imgKeySelected.setText(newdisplayed);
					cryptoPanel.setSrcKeyImageFile(thisFile);
					dialogHelper.info(I18n.t("crypter.key_image_file_correctly_loaded_and"), I18n.t("crypter.key_image_selected"));
				}else {
					String msg = I18n.t("crypter.file_not_recognized_as_key")+thisFile.getName();
					TEXT_LOG.append(msg);
					cryptoPanel.showMessageImageNotSupported();
				}
			} catch (IOException e) {
				String msg = I18n.t("crypter.some_error_occurred_please_reload_key");
				TEXT_LOG.append(msg);
				imgKeySelected.setForeground(Color.RED);
				imgKeySelected.setText(msg);
				cryptoPanel.setSrcKeyImageFile(null);
				dialogHelper.error(msg, msg);
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

		
		JFileChooser fileChooser = new NativeFileChooser();		
		fileChooser.setDialogTitle(I18n.t("crypter.load_png_file"));
		File correctFile = null;

		int returnVal = fileChooser.showOpenDialog(parent);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            
        	File selectedFile = fileChooser.getSelectedFile();
        	String givenExtension = FilenameUtils.getExtension(selectedFile.getName());
        	if(selectedFile==null||!givenExtension.equalsIgnoreCase("png")) {
        		dialogHelper.error(I18n.t("app.please_select_a_valid_png_file"), I18n.t("crypter.wrong_file_format"));
        	}else {
        		correctFile = selectedFile;
        	}
        }else {
        	// cancelled by user: no message needed
        }
        return correctFile;
	}
	
	/**
	 * this method select a directory from the user (only directories)to write a copy of current
	 * dictionary, if nothing is selected returns null
	 */
	private File directoWrite(JComponent parent) {
		
		JFileChooser fileChooser = new NativeFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fileChooser.setCurrentDirectory(new File(currentConfig.getFileOpt().getLastDstFolderPath()));		
		File correctFile = null;
		
		int returnVal = fileChooser.showSaveDialog(parent);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            
        	File selectedFile = fileChooser.getSelectedFile();
        	if(selectedFile==null||!selectedFile.isDirectory()) {
        		dialogHelper.error(I18n.t("common.please_select_a_directory"), I18n.t("crypter.select_a_directory_please"));
        	}else {
        		correctFile = selectedFile;
        		currentConfig.getFileOpt().setLastDstFolderPath(correctFile.getAbsolutePath());
        	}
        }else {
        	// cancelled by user: no message needed
        }
        return correctFile;
	}
	
}

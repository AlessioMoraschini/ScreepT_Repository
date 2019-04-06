package gUI.utils.tree;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.peer.PanelPeer;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import amfileutils.FileStuff;
import gUI.panels.CrypterPanel;
import gUI.panels.KeyDictionaryPanel;
import gUI.panels.KeyGenPanel;
import gUI.utils.progressDialog.TestUpdateProgress;
import iNIT.INItializer;

public class TreeFileMouseListenerDictionary {
	
	// KeyGenPanel logger
	static Logger logger = Logger.getLogger(TreeFileMouseListenerDictionary.class); 
	
	// gui elements
	private JTree tree;
	private File selectedNode;
	private JTextArea TEXT_LOG = new JTextArea();
	private JTextArea TEXT_MAIN = new JTextArea();
	private JFrame mainFrame;
	private INItializer currentConfig;
	
	// object that display wait dialog
	private volatile TestUpdateProgress progUpdater;

	// constructor
	public TreeFileMouseListenerDictionary(INItializer currentConf, JFrame parent, JTree treeIn, JTextArea log, JTextArea mainTxtArea) {
		currentConfig = currentConf;
		mainFrame = parent;
		TEXT_LOG = log;
		TEXT_MAIN = mainTxtArea;
		tree = treeIn;
		tree.setComponentPopupMenu(getPopUpMenu());
		tree.addMouseListener(getMouseListener());
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
	    JMenuItem item2 = new JMenuItem("Read Image & Details");
	    item2.addActionListener(getInfoActionListener());
	    menu.add(item2);
	    
	    JMenuItem item3 = new JMenuItem("Add key Image to Dictionary");
	    item3.addActionListener(addKeyActionListener());
	    menu.add(item3);
	    
	    JMenuItem item4 = new JMenuItem("Export selected key Images");
	    item4.addActionListener(exportKeyActionListener());
	    menu.add(item4);
	    
	    JMenuItem item1 = new JMenuItem("Delete selected Key Images");
	    item1.addActionListener(getDeleteActionListener());
	    menu.add(item1);

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
									+FileStuff.coolFileSize(thisFile)+"\n\n => ImgType: "+img.toString();
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
	            int i = 0;
            	if(treeSelArr.length!= 0 && JOptionPane.showConfirmDialog(mainFrame, "Are you sure to delete?")==JOptionPane.OK_OPTION){	        		
		            for(TreePath tp : treeSelArr) {
//		            	String path = tp.toString().replaceAll("\\]| |\\[|", "").replaceAll(",", File.separator);
		            	String path = tp.getLastPathComponent().toString();
		            	File selectedFile = new File(path);
	//		        	File selectedFile = (File)tree.getLastSelectedPathComponent(); // only for one file
		            	logger.debug(path+" Deleted: "+selectedFile.delete());		        		
			        }
		            String msg = "Files deleted!";
	            	logger.debug(msg+Arrays.toString(treeSelArr));
	            	TEXT_LOG.setText("\n\n ### Completed ###\n\n  "+msg);

	            }else {
	            	String msg = "Skipped after user request!";
			    	JOptionPane.showMessageDialog(mainFrame, msg, "Skipped", JOptionPane.WARNING_MESSAGE);
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
					if (FileStuff.copyFile(selected, new File(KeyGenPanel.KEY_DICTIONARY + selected.getName()),currentConfig.getFileOpt().isFlagOverwrite())) {
						JOptionPane.showMessageDialog(TEXT_LOG, "File correctly saved in " + KeyGenPanel.KEY_DICTIONARY,
								"File added to dictionary :)", JOptionPane.INFORMATION_MESSAGE);
						logger.debug("File :" + selected.getName() + " added in the dictionary -> " + KeyGenPanel.KEY_DICTIONARY);
					} else {
						JOptionPane.showMessageDialog(TEXT_LOG, "File not saved in " + KeyGenPanel.KEY_DICTIONARY,
								". Some error occurred :(", JOptionPane.ERROR_MESSAGE);
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
			    			progUpdater = new TestUpdateProgress(currentConfig);
				    		progUpdater.getPb().setValue(5);
				    		
							if (!selected.exists()) {
								selected.mkdirs();
							}
							TreePath[] treeSelArr = tree.getSelectionPaths();
							int i = 0;
							if (treeSelArr.length != 0) {
								for (TreePath tp : treeSelArr) {
									i++;
									progUpdater.getPb().setValue((int)((float)i/(float)treeSelArr.length));
									
									String path = tp.getLastPathComponent().toString();
									File selectedFile = new File(path);
									FileStuff.copyFile(selectedFile,
											new File(selected.getAbsolutePath() + "\\" + selectedFile.getName()), false);
									logger.debug(selectedFile.getName() + " exported to " + selected.getAbsolutePath());
								}
								String msg = "Files exported!";
								logger.debug(msg + Arrays.toString(treeSelArr));
								TEXT_LOG.setText("\n\n ### Completed ###\n\n  " + msg);
					    		progUpdater.getPb().setValue(100);
						    	JOptionPane.showMessageDialog(TEXT_LOG, msg, "Files exported!", JOptionPane.INFORMATION_MESSAGE);

							} else {
								String msg = "Skipped after user request!";
					    		progUpdater.getPb().setValue(100);
								JOptionPane.showMessageDialog(tree, msg, "Skipped", JOptionPane.WARNING_MESSAGE);
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
        	if(selectedFile==null||!givenExtension.equalsIgnoreCase("png")) {
        		JOptionPane.showMessageDialog(parent, "Please select a valid '.png' file!", "Wrong File format", JOptionPane.ERROR_MESSAGE);
        	}else {
        		correctFile = selectedFile;

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

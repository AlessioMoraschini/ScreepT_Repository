package gUI.utils.tree.iniconfmanager;

import java.awt.Color;
import java.awt.Desktop;
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
import org.ini4j.Wini;

import amfileutils.FileStuff;
import gUI.mainGUI;
import gUI.panels.ChargeIniLog;
import gUI.panels.CrypterPanel;
import gUI.panels.KeyDictionaryPanel;
import gUI.panels.KeyGenPanel;
import gUI.utils.filedialog.INIfileChoosER;
import gUI.utils.progressDialog.TestUpdateProgress;
import gUI.utils.tree.FileTreeModel;
import iNIT.INItializer;
import uTILS.local.ImageWorker;

public class TreeFileMouseListenerIni {
	
	// KeyGenPanel logger
	static Logger logger = Logger.getLogger(TreeFileMouseListenerIni.class); 
	
	// gui elements
	private JTree tree;
	private File selectedNode;
	private INItializer currentConfig;
	private ChargeIniLog confManagerPanel;

	// constructor
	public TreeFileMouseListenerIni(INItializer currentConf, ChargeIniLog parent, JTree treeIn) {
		currentConfig = currentConf;
		confManagerPanel = parent;
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
	    
	    JMenuItem item1 = new JMenuItem("Load Selected configuration");
	    item1.addActionListener(loadSelectedConfiguration());
	    menu.add(item1);
	    
	    JMenuItem item2 = new JMenuItem("View in system explorer");
	    item2.addActionListener(openInSystemExplorer());
	    menu.add(item2);
	    
	    JMenuItem item3 = new JMenuItem("Delete selected Configuration files");
	    item3.addActionListener(getDeleteActionListener());
	    menu.add(item3);
	    
	    JMenuItem item4 = new JMenuItem("Import configuration file");
	    item4.addActionListener(null);
	    menu.add(item4);
	    
	    JMenuItem item5 = new JMenuItem("Refresh tree");
	    item5.addActionListener(refreshListener());
	    menu.add(item5);

	    return menu;
	}
	
	private ActionListener openInSystemExplorer() {
		return new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					Desktop.getDesktop().open(new File(mainGUI.DEFAULT_INI_FOLDER));
					
				} catch (IOException e1) {
				logger.error("Error opening folder in system explorer !"+e1.toString());
				}		
			}
		};
	}

	private ActionListener getDeleteActionListener() {
	    return new ActionListener() {

	        @Override
	        public void actionPerformed(ActionEvent arg0) {

	            TreePath[] treeSelArr = tree.getSelectionPaths();
            	if(treeSelArr.length!= 0 && JOptionPane.showConfirmDialog(confManagerPanel, "Are you sure to delete?")==JOptionPane.OK_OPTION){	        		
		            for(TreePath tp : treeSelArr) {
		            	
	            		String path = tp.getLastPathComponent().toString();
		            	File selectedFile = new File(path);
		            	if(!selectedFile.getAbsolutePath().equals(currentConfig.getIniFile().getFile().getAbsolutePath()) && selectedFile.isFile()) {	
		            		logger.debug(path+" Deleted: "+selectedFile.delete());	
		            	}else {
		            		logger.info(path + " Not deleted beacuse it's in usage");
		            	}
			        }

	            }else {
	            	String msg = "Skipped after user request!";
			    	logger.debug(msg);
	            }
            	
            	refreshTree();
	        }
	    };
	}
	
	private ActionListener loadSelectedConfiguration() {
		return new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				Object lastSelected = tree.getLastSelectedPathComponent();
				String path = lastSelected.toString();
				
				if (lastSelected != null && path.toLowerCase().endsWith(".ini")) {
					loadIni(new File(path));					
				}
			}
		};
	}
	
	private ActionListener refreshListener() {
		return new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				refreshTree();
			}
		};
	}
	
	// utility methods //
	
	private void refreshTree() {
		tree.setModel(new FileTreeModel(new File(mainGUI.DEFAULT_INI_FOLDER)));
	}
	
	private void loadIni(File iniFile) {
		try {
			currentConfig.setIniFile(new Wini(iniFile));
			currentConfig.setIniReadFile(new Wini(iniFile));
			currentConfig.readConfigIniFile();
			
			confManagerPanel.lblAvviso.setForeground(ImageWorker.randomColorRGB(4f));
			confManagerPanel.lblAvviso.setText("[Configuration Loaded!]");
			confManagerPanel.config_PATH.setText(currentConfig.getIniFile().getFile().getPath());
			confManagerPanel.textArea.setText("");
			confManagerPanel.textArea.append("chosen: "+iniFile);
			confManagerPanel.textArea.append("\n\n\n### NEW INI CONFIGURATION LOADED ###\n\n -> ");
			confManagerPanel.textArea.append( currentConfig.toStringa());
	        
		} catch (IOException e) {
			logger.error(e);
		}
	}
	
}

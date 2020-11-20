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
package gUI.utils.tree.iniconfmanager;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;
import org.ini4j.Wini;

import dialogutils.JOptionHelperExtended;
import files.FileWorker;
import files.FileVarious;
import frameutils.utils.GuiUtilsExtended;
import gUI.panels.ConfigurationManagerPanel;
import gUI.utils.filedialog.INIfileChoosER;
import gui.MenuSetter;
import maingUI.MainGUI;
import maingUI.MainGuiGeneralAppStarter;
import mainobj.INItializer;
import resources.GeneralConfig;
import resources.IconsPathConfigurator;
import treeutils.om.FileTreeModel;
import utils.ImageWorkerExtended;

public class TreeFileMouseListenerIni extends mouseutils.TreeFileMouseListenerBasic{
	
	// KeyGenPanel logger
	public static Logger logger = Logger.getLogger(TreeFileMouseListenerIni.class); 
	
	// gui elements
	private INItializer currentConfig;
	private ConfigurationManagerPanel confManagerPanel;
	
	public static JOptionHelperExtended dialogHelper = new JOptionHelperExtended(null);

	public TreeFileMouseListenerIni(Component parent) {
		super(parent);
	};
	
	// constructor
	public TreeFileMouseListenerIni(INItializer currentConf, ConfigurationManagerPanel parent, JTree treeIn) {
		currentConfig = currentConf;
		confManagerPanel = parent;
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
		           
		            super.mousePressed(arg0);
	            }
	        }
	    };
	}

	@Override
	public JPopupMenu getPopUpMenu() {
	    JPopupMenu menu = new JPopupMenu();
	    
	    JMenuItem previewFileItem = new JMenuItem("Preview selected configuration");
	    previewFileItem.setFont(GeneralConfig.DEFAULT_POP_UP_MENUS_FONT);
	    previewFileItem.addActionListener(previewSelected());
	    menu.add(previewFileItem);
	    
	    JMenuItem loadItem = new JMenuItem("Load Selected configuration");
	    loadItem.setFont(GeneralConfig.DEFAULT_POP_UP_MENUS_FONT);
	    loadItem.addActionListener(loadSelectedConfiguration());
	    menu.add(loadItem);
	    
	    JMenuItem sysExplorerItem = new JMenuItem("View in system explorer");
	    sysExplorerItem.setFont(GeneralConfig.DEFAULT_POP_UP_MENUS_FONT);
	    sysExplorerItem.addActionListener(openInSystemExplorer());
	    menu.add(sysExplorerItem);
	    
	    // Implementation inherited From parent
	    JMenuItem addCurrentItem = new JMenuItem("Add current configuration to this list");
	    addCurrentItem.setFont(GeneralConfig.DEFAULT_POP_UP_MENUS_FONT);
	    addCurrentItem.addActionListener(addCurrentConfig());
	    menu.add(addCurrentItem);
	    
	    JMenuItem importItem = new JMenuItem("Import configuration file or directory content");
	    importItem.setFont(GeneralConfig.DEFAULT_POP_UP_MENUS_FONT);
	    importItem.addActionListener(importFilesConfig());
	    menu.add(importItem);

	    JMenuItem importFromScreepTItem = new JMenuItem("Import configuration from other installation");
	    importFromScreepTItem.setFont(GeneralConfig.DEFAULT_POP_UP_MENUS_FONT);
	    importFromScreepTItem.addActionListener((e) -> {
	    	INIfileChoosER.loadDefaultsFromOtherInstallation(true);
	    });
	    menu.add(importFromScreepTItem);
	    
	    JMenuItem exportItem = new JMenuItem("Export selected configurations");
	    exportItem.setFont(GeneralConfig.DEFAULT_POP_UP_MENUS_FONT);
	    exportItem.addActionListener(exportSelected());
	    menu.add(exportItem);
	    
	    // Implementation inherited From parent
	    JMenuItem deleteItem = new JMenuItem("Delete selected Configuration files");
	    deleteItem.setFont(GeneralConfig.DEFAULT_POP_UP_MENUS_FONT);
	    deleteItem.addActionListener(getDeleteActionListener());
	    menu.add(deleteItem);
	    
	    // Implementation inherited From parent
	    JMenuItem refreshItem = new JMenuItem("Refresh tree");
	    refreshItem.setFont(GeneralConfig.DEFAULT_POP_UP_MENUS_FONT);
	    refreshItem.addActionListener(refreshListener());
	    menu.add(refreshItem);
	    
	    // Implementation inherited From parent
	    JMenuItem propertiesItem = new JMenuItem("File Properties");
	    propertiesItem.setFont(GeneralConfig.DEFAULT_POP_UP_MENUS_FONT);
	    propertiesItem.addActionListener(propertiesListener());
	    menu.add(propertiesItem);

	    // set menu icons
	    MenuSetter.setIconTextGap(menu, 12);
	    
	    previewFileItem.setIcon(new ImageIcon(IconsPathConfigurator.ICON_LOOK_INFO));
	    loadItem.setIcon(new ImageIcon(IconsPathConfigurator.ICON_LOAD));
	    sysExplorerItem.setIcon(new ImageIcon(IconsPathConfigurator.iconFolderPath));
	    addCurrentItem.setIcon(new ImageIcon(IconsPathConfigurator.ICON_ADD));
	    deleteItem.setIcon(new ImageIcon(IconsPathConfigurator.ICON_DELETE));
	    importFromScreepTItem.setIcon(new ImageIcon(IconsPathConfigurator.ICON_IMPORT_FROM_SCREEPT));
	    importItem.setIcon(new ImageIcon(IconsPathConfigurator.ICON_IMPORT));
	    exportItem.setIcon(new ImageIcon(IconsPathConfigurator.ICON_EXPORT_CONF));
	    refreshItem.setIcon(new ImageIcon(IconsPathConfigurator.ICON_RELOAD_CONF));
	    propertiesItem.setIcon(new ImageIcon(IconsPathConfigurator.ICON_ABOUT));
	    
	    return menu;
	}
	
	private ActionListener previewSelected() {
		 return new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// update textArea with preview of content
		        confManagerPanel.textArea.setText("Previewing: ["+selectedNode.getAbsolutePath()+"]\n\n");
		        FileWorker.appendFileTxtArea(selectedNode, confManagerPanel.textArea, false);
			}
		};
	}
	
	private ActionListener addCurrentConfig() {
		return new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				File currentIniFile = currentConfig.getIniFile().getFile();
				File defaultFolder = new File(GeneralConfig.DEFAULT_INI_FOLDER);
				
				String newName = "";
				newName = new JOptionHelperExtended(parent).askForString("Choose a name for the configuration to save");
				if (newName.trim().equals("")) {
					return;
				}
				
				newName = FileVarious.ensureValidExtension(newName, "ini");
				
				File newFile = FileVarious.uniqueJavaObjFile(new File(defaultFolder.getAbsolutePath() + "/" + newName));
				
				FileWorker.copyFile(currentIniFile, newFile, true);
				
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
					MainGUI.updateGuiFromCurrConfig();
				}
			}
		};
	}
	
	private ActionListener importFilesConfig() {
		return new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				INIfileChoosER.importIniFileOrDir();
				refreshTree();
			}
		};
	}
	
	private ActionListener exportSelected() {
	    return new ActionListener() {

	        @Override
	        public void actionPerformed(ActionEvent arg0) {
	        	
	        	Date now = new Date();
	    		SimpleDateFormat df = new SimpleDateFormat("yyyy_MM_dd_@_HH-mm-ss");
	        	File exportDir = MainGuiGeneralAppStarter.fileDialogHelper.directoWrite(confManagerPanel.parent.getMainFrame());
	        	if(exportDir == null)
	        		return; // to avoid null pointer
	        	exportDir = new File(exportDir.getAbsolutePath()+"/"+df.format(now));
	        	exportDir.mkdirs();
	        	
	            TreePath[] treeSelArr = tree.getSelectionPaths();
            	if(treeSelArr.length!= 0){	        		
		            for(TreePath tp : treeSelArr) {
		            	
	            		String path = tp.getLastPathComponent().toString();
		            	File selectedFile = new File(path);
		            	if(selectedFile.isFile() && selectedFile.exists()) {	
		            		FileWorker.copyFileIntoDir(selectedFile, exportDir, false, GeneralConfig.TEMP_DIRECTORY);
		            	}else {
		            		logger.warn(path + "Not a File or Not existing: out of sync");
		            	}
			        }
	            }
            	GuiUtilsExtended.openInFileSystem(exportDir);
	        }
	    };
	}
	
	// utility methods //
	
	@Override
	public void refreshTree() {
		tree.setModel(new FileTreeModel(new File(GeneralConfig.DEFAULT_INI_FOLDER)));
	}
	
	private void loadIni(File iniFile) {
		try {
			currentConfig.setIniFile(new Wini(iniFile));
			currentConfig.setIniReadFile(new Wini(iniFile));
			currentConfig.readConfigIniFile();
			
			confManagerPanel.lblAvviso.setForeground(ImageWorkerExtended.randomColorRGB(4f));
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

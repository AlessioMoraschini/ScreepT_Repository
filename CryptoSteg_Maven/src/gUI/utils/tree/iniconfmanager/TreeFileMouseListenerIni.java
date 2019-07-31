package gUI.utils.tree.iniconfmanager;

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
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;
import org.ini4j.Wini;

import amfileutils.FileStuff;
import frameutils.GuiUtils;
import gUI.panels.ChargeIniLog;
import gUI.utils.filedialog.GenericFileChooserDialog;
import gUI.utils.tree.FileTreeModel;
import maingUI.mainGUI;
import mainobj.INItializer;
import media.ImageGeneralWorker;
import menusettings.MenuSetter;
import resources.IconsPathConfigurator;

public class TreeFileMouseListenerIni extends mouseutils.TreeFileMouseListenerBasic{
	
	// KeyGenPanel logger
	static Logger logger = Logger.getLogger(TreeFileMouseListenerIni.class); 
	
	// gui elements
	private INItializer currentConfig;
	private ChargeIniLog confManagerPanel;

	public TreeFileMouseListenerIni() {
		super();
	};
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
		           
		            super.mousePressed(arg0);
	            }
	        }
	    };
	}

	private JPopupMenu getPopUpMenu() {
	    JPopupMenu menu = new JPopupMenu();
	    
	    JMenuItem previewFileItem = new JMenuItem("Preview selected configuration");
	    previewFileItem.addActionListener(previewSelected());
	    menu.add(previewFileItem);
	    
	    JMenuItem loadItem = new JMenuItem("Load Selected configuration");
	    loadItem.addActionListener(loadSelectedConfiguration());
	    menu.add(loadItem);
	    
	    JMenuItem sysExplorerItem = new JMenuItem("View in system explorer");
	    sysExplorerItem.addActionListener(openInSystemExplorer());
	    menu.add(sysExplorerItem);
	    
	    JMenuItem addCurrentItem = new JMenuItem("Add current configuration to this list");
	    addCurrentItem.addActionListener(addCurrentConfig());
	    menu.add(addCurrentItem);
	    
	    JMenuItem importItem = new JMenuItem("Import configuration file or directory content");
	    importItem.addActionListener(importFilesConfig());
	    menu.add(importItem);
	    
	    JMenuItem exportItem = new JMenuItem("Export selected configurations");
	    exportItem.addActionListener(exportSelected());
	    menu.add(exportItem);
	    
	    JMenuItem deleteItem = new JMenuItem("Delete selected Configuration files");
	    deleteItem.addActionListener(getDeleteActionListener());
	    menu.add(deleteItem);
	    
	    JMenuItem refreshItem = new JMenuItem("Refresh tree");
	    refreshItem.addActionListener(refreshListener());
	    menu.add(refreshItem);
	    
	    JMenuItem propertiesItem = new JMenuItem("File Properties");
	    propertiesItem.addActionListener(propertiesListener());
	    menu.add(propertiesItem);

	    // set menu icons
	    MenuSetter.setIconTextGap(menu, 12);
	    
	    previewFileItem.setIcon(new ImageIcon(IconsPathConfigurator.ICON_LOOK_INFO));
	    loadItem.setIcon(new ImageIcon(IconsPathConfigurator.ICON_LOAD));
	    sysExplorerItem.setIcon(new ImageIcon(IconsPathConfigurator.iconFolderPath));
	    addCurrentItem.setIcon(new ImageIcon(IconsPathConfigurator.ICON_ADD));
	    deleteItem.setIcon(new ImageIcon(IconsPathConfigurator.ICON_DELETE));
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
		        FileStuff.appendFileTxtArea(selectedNode, confManagerPanel.textArea, false);
			}
		};
	}
	
	private ActionListener openInSystemExplorer() {
		return new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				GuiUtils.openInFileSystem(new File(mainGUI.DEFAULT_INI_FOLDER));
			}
		};
	}
	
	private ActionListener addCurrentConfig() {
		return new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				File currentIniFile = currentConfig.getIniFile().getFile();
				File defaultFolder = new File(mainGUI.DEFAULT_INI_FOLDER);
				
				String newName = "";
				newName = JOptionPane.showInputDialog("Choose a name for the configuration to save");
				if (newName.equals("")) {
					return;
				}
				
				File newFile = FileStuff.renameJavaObjFile(new File(defaultFolder.getAbsolutePath() + "/" + newName));
				
				FileStuff.copyFile(currentIniFile, newFile, true);
				
				refreshTree();
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
					confManagerPanel.parent.updateGuiFromCurrConfig();
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
	
	private ActionListener importFilesConfig() {
		return new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				importIniFileOrDir();
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
	        	File exportDir = GenericFileChooserDialog.directoWrite(confManagerPanel.parent.getMainFrame(), currentConfig);
	        	if(exportDir == null)
	        		return; // to avoid null pointer
	        	exportDir = new File(exportDir.getAbsolutePath()+"/"+df.format(now));
	        	exportDir.mkdirs();
	        	
	            TreePath[] treeSelArr = tree.getSelectionPaths();
            	if(treeSelArr.length!= 0 && JOptionPane.showConfirmDialog(confManagerPanel, "Are you sure to delete?")==JOptionPane.OK_OPTION){	        		
		            for(TreePath tp : treeSelArr) {
		            	
	            		String path = tp.getLastPathComponent().toString();
		            	File selectedFile = new File(path);
		            	if(selectedFile.isFile() && selectedFile.exists()) {	
		            		FileStuff.copyFileIntoDir(selectedFile, exportDir, false);
		            	}else {
		            		logger.warn(path + " Not existing: out of sync");
		            	}
			        }
	            }
            	GuiUtils.openInFileSystem(exportDir);
	        }
	    };
	}
	
	// utility methods //
	
	private void refreshTree() {
		tree.setModel(new FileTreeModel(new File(mainGUI.DEFAULT_INI_FOLDER)));
	}
	
	private void importIniFileOrDir() {
		File selectedImportDir = GenericFileChooserDialog.fileAndDirRead(confManagerPanel, currentConfig);
		if (selectedImportDir!=null && selectedImportDir.isDirectory()) {
			try {
				FileStuff.copyDirectoryContents(selectedImportDir, new File(mainGUI.DEFAULT_INI_FOLDER), false, "ini");
				JOptionPane.showMessageDialog(confManagerPanel,"Directory :" + selectedImportDir.getName() + " added in the configurations -> "
								+ mainGUI.DEFAULT_INI_FOLDER, "Directory added to config pool :)", JOptionPane.INFORMATION_MESSAGE);
				logger.debug("Directory :" + selectedImportDir.getName() + " added to the configurations -> " + mainGUI.DEFAULT_INI_FOLDER);
			} catch (IOException e1) {
				JOptionPane.showMessageDialog(confManagerPanel, "Directory not imported in " + mainGUI.DEFAULT_INI_FOLDER,	". Some error occurred :(", JOptionPane.ERROR_MESSAGE);
				logger.error("Directory not imported, some error occurred :( ");
			} 
		}else if(selectedImportDir!=null && selectedImportDir.isFile() && selectedImportDir.getName().toLowerCase().endsWith(".ini")) {
			FileStuff.copyFileIntoDir(selectedImportDir, new File(mainGUI.DEFAULT_INI_FOLDER), false);
			logger.debug("Directory :" + selectedImportDir.getName() + " added to the configurations -> " + mainGUI.DEFAULT_INI_FOLDER);
		}
	}
	
	private void loadIni(File iniFile) {
		try {
			currentConfig.setIniFile(new Wini(iniFile));
			currentConfig.setIniReadFile(new Wini(iniFile));
			currentConfig.readConfigIniFile();
			
			confManagerPanel.lblAvviso.setForeground(ImageGeneralWorker.randomColorRGB(4f));
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

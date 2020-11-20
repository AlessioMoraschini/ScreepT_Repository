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
package gUI.utils.filedialog;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.ini4j.Ini;
import org.ini4j.Wini;

import dialogutils.ExtensionFileFilter;
import dialogutils.GenericFileChooserDialog;
import dialogutils.JOptionHelperExtended;
import files.FileVarious;
import files.FileWorker;
import files.search.FileSearchAdvanced;
import gui.GuiUtils;
import impl.INItializer;
import main.MainStandAloneUpdater;
import main.om.PropertiesDTO;
import maingUI.MainGUI;
import maingUI.MainGuiGeneralAppStarter;
import resources.GeneralConfig;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class INIfileChoosER {

	// INItializer logger creation		
	static Logger logger = Logger.getLogger(INIfileChoosER.class); 
	
	private static JOptionHelperExtended dialogHelper = new JOptionHelperExtended(MainGUI.mainAPP != null ? MainGUI.mainAPP.getMainFrame() : null);
	
	/**
	 * Read file INI updating textArea and label
	 * */
	public static void readNloadIniWithTextAreaOutput(INItializer fileINI, JTextArea textA, JLabel lblAvviso) {
		
		final INItializer backupINI = fileINI;
    	GenericFileChooserDialog chooserDialogHelper = new GenericFileChooserDialog(MainGUI.currentConfiguration);
    	chooserDialogHelper.defaultDirectory = new File("./INI_FILES");
    	File iniRead = chooserDialogHelper.fileRead(MainGUI.mainAPP.getMainFrame(), new FileNameExtensionFilter("Configuration file (.ini)", "ini"));
		
		dialogHelper = new JOptionHelperExtended(MainGUI.mainAPP.getMainFrame());
		
        if (iniRead != null) {
        	String fileExtension = FilenameUtils.getExtension(iniRead.getName());
           
        	if(fileExtension.equalsIgnoreCase("ini")) {
            	loadIniIntoTarget(fileINI, iniRead);
            	lblAvviso.setText("[Configuration Loaded!]");
    	    	textA.setText("");
    	    	textA.append("chosen: " + iniRead.getAbsolutePath());
    	    	textA.append("\n\n\n### NEW INI CONFIGURATION LOADED ###\n\n -> ");
    			textA.append( fileINI.toStringa());
    	        
			} else {
				dialogHelper.error("Cannot load configuration file with extension different from '.ini'. \n Back to "+ backupINI.getIniFile().getFile().getName());
				fileINI = backupINI;
			}
            
        }else {
        	textA.setText("");
	    	textA.append("=> No File selected! ");
	    	textA.append("\n\n\n=> Same INI CONFIGURATION maintained \n\n -> ");
			textA.append( fileINI.toStringa());
		}
    }
	
	/**
	 * Read file INI
	 * */
	public static void readNLoadIniFile(INItializer fileINI, String defDir) {
		
		final INItializer backupINI = fileINI;
		
    	GenericFileChooserDialog chooserDialogHelper = new GenericFileChooserDialog(MainGUI.currentConfiguration);
    	chooserDialogHelper.defaultDirectory = new File(defDir);
    	File iniRead = chooserDialogHelper.fileRead(MainGUI.mainAPP.getMainFrame(), new FileNameExtensionFilter("Configuration file (.ini)", "ini"));
		
		dialogHelper = new JOptionHelperExtended(MainGUI.mainAPP.getMainFrame());
		
        if (iniRead != null) {
        	String fileExtension = FilenameUtils.getExtension(iniRead.getName());
            if(fileExtension.equalsIgnoreCase("ini")) {
            	loadIniIntoTarget(fileINI, iniRead);
    	    	logger.debug("chosen: " + iniRead.getAbsolutePath());
    	    	logger.debug("\n\n\n### NEW INI CONFIGURATION LOADED ###\n\n -> "+fileINI.toStringa());
    	    	dialogHelper.info("Configuration Loaded!");
    	        
			} else {
				dialogHelper.error("Cannot load configuration file with extension different from '.ini'. \n Back to "+ backupINI.getIniFile().getFile().getName());
			}
            
        }else {
        	logger.debug("=> No File selected! ");
        	logger.debug("\n\n\n=> INI CONFIGURATION maintained \n\n -> ");
		}
    }
	
	/**
	 * Import from other installation directory: search CONFIG.ini and copy to current + copy into INI_FILES
	 * @param fileINI
	 */
	public static boolean loadDefaultsFromOtherInstallation(boolean importCopy) {
		
		GenericFileChooserDialog chooserDialogHelper = new GenericFileChooserDialog(MainGUI.currentConfiguration);
    	chooserDialogHelper.defaultDirectory = GeneralConfig.USER_DESKTOP_FILE;
    	File sourceFolder = chooserDialogHelper.directoRead(MainGUI.mainAPP.getMainFrame(), "Select other's installation root folder");
		
    	boolean ok = false;
		
		if (sourceFolder != null && sourceFolder.exists()) {
			File defaultIni = new File(GeneralConfig.DEFAULT_INI);
			String defaultIniParentDir = defaultIni.getParentFile().getName();
			List<File> matchedFolders = FileSearchAdvanced.findOnlyFoldersByMatch(sourceFolder, defaultIniParentDir, true, true, false);
			for (File extFolder : matchedFolders) {
				List<File> matchedIniConf = FileSearchAdvanced.findOnlyFilesByMatch(extFolder, defaultIni.getName(), true, true, false);
				if (!matchedIniConf.isEmpty()) {
					boolean confirmation = MainGuiGeneralAppStarter.dialogHelper.yesOrNo(
							GuiUtils.encapsulateInHtml("Are you sure to overwrite default configuration with the following?<br> " + matchedIniConf.get(0)), "Overwrite current?");
					
					if(confirmation) {
						ok = copyNloadIniAsDefault(matchedIniConf.get(0));
						updateFromExternalGeneralConfig(extFolder.getParentFile());
						if(importCopy) {
							try {
								FileUtils.copyFileToDirectory(matchedIniConf.get(0), new File(GeneralConfig.DEFAULT_INI_FOLDER));
								logger.info("Configuration imported from another installation: " + matchedIniConf.get(0));
							} catch (IOException e) {
								logger.error("Cannot import configuration's copy from " + matchedIniConf.get(0), e);
							}
						}
					}
					
					if(ok)
						MainGuiGeneralAppStarter.dialogHelper.info(GuiUtils.encapsulateInHtml(matchedIniConf.get(0) + "<br> <b>Imported succesfully!</b>"));
					break;
				}
			} 
		} else if(sourceFolder != null) {
			MainGuiGeneralAppStarter.dialogHelper.warn("Nothing selected, Import skipped.");
			ok = true;
		} 
		
		if(!ok) {
			MainGuiGeneralAppStarter.dialogHelper.warn("Import skipped, invalid selection: " + sourceFolder);
		}

		return ok;
	}
	
	public static boolean loadIniIntoTarget(INItializer fileINI, File iniRead) {
		try {
			fileINI.setIniFile(new Wini(iniRead));
			fileINI.setIniReadFile(new Wini(iniRead));
			fileINI.readConfigIniFile();
			
			logger.info(iniRead + " -> configuration loaded!");
			return true;
		} catch (Exception e) {
			logger.error("Cannot load ini file: " + iniRead);
			return false;
		}
	}

	public static boolean loadIniAsDefault(File iniRead) {
		try {
			MainGUI.currentConfiguration.setIniFile(new Wini(iniRead));
			MainGUI.currentConfiguration.setIniReadFile(new Wini(iniRead));
			MainGUI.currentConfiguration.readConfigIniFile();
			MainGUI.updateGuiFromCurrConfig();
		
			logger.info(iniRead + " -> configuration loaded!");
			return true;
		} catch (Exception e) {
			logger.error("Cannot load ini file: " + iniRead);
			return false;
		}
	}
        
	public static boolean copyNloadIniAsDefault(INItializer iniFileToLoad) {
		try {
			FileUtils.copyFile(iniFileToLoad.getIniFile().getFile(), MainGUI.currentConfiguration.getIniFile().getFile());
			MainGUI.currentConfiguration.readConfigIniFile();
			MainGUI.updateGuiFromCurrConfig();
			
			logger.info(iniFileToLoad.getIniFile().getFile() + " -> configuration copied and loaded as default!");
		} catch (IOException e) {
			logger.error("Cannot load external ini file: " + iniFileToLoad.getIniFile().getFile(), e);
			return false;
		}
		
		return true;
	}

	public static boolean copyNloadIniAsDefault(File iniFileToLoad) {
		try {
			String defaultIniPath = MainGUI.currentConfiguration.getIniFile().getFile().getAbsolutePath();
			
			FileUtils.copyFile(iniFileToLoad, MainGUI.currentConfiguration.getIniFile().getFile());
			MainGUI.currentConfiguration.setIniFile(new Wini(new File(defaultIniPath)));
			MainGUI.currentConfiguration.setIniReadFile(new Wini(new File(defaultIniPath)));
			MainGUI.currentConfiguration.readConfigIniFile();
			MainGUI.updateGuiFromCurrConfig();
		
			logger.info(iniFileToLoad + " -> configuration copied and loaded as default!");
		} catch (Exception e) {
			logger.error("Cannot load external ini file: " + iniFileToLoad, e);
			return false;
		}
		
		return true;
	}
	
	/**
	 * Export file INI updating label (use null for label if you don't want to use it)
	 */
    public static void exportIniFile(INItializer fileINI, JLabel lblAvviso) {
		
    	GenericFileChooserDialog chooserDialogHelper = new GenericFileChooserDialog(MainGUI.currentConfiguration);
    	File selectedFile = chooserDialogHelper.fileWrite(MainGUI.mainAPP.getMainFrame(), "Config_Export.ini", new File("./INI_FILES"), new ExtensionFileFilter[] {new ExtensionFileFilter(new String[] {"ini"}, "Configuration file (.ini)")});

    	File backupIni = fileINI.getIniFile().getFile();
		boolean done = false;

		dialogHelper = new JOptionHelperExtended(MainGUI.mainAPP.getMainFrame());
		
        if (selectedFile != null) {
            try {
            	selectedFile = FileVarious.changeExtension(selectedFile, ".ini");
            	if(selectedFile.exists()) {
            		if(dialogHelper.yesOrNo("File "+selectedFile.getName()+" exists, overwrite it?")) {
            			selectedFile.delete();	
                       	done = selectedFile.createNewFile();
            		}else {
            			dialogHelper.info("Operation cancelled");
            		}
            	}else {
                   	done = selectedFile.createNewFile();
            	}
            		   		
               	if(done) {
               		dialogHelper.info("File "+selectedFile.getName()+" succesfully created!");
               		loadIniIntoTarget(fileINI, selectedFile);
    				if(lblAvviso != null) lblAvviso.setText("[Configuration Exported!]");
               	}

               	fileINI.setIniFile(new Wini(backupIni));
               	fileINI.setIniReadFile(new Ini(backupIni));
				
			} catch (IOException e) {
				logger.error(e);
			}  			
            
        }else{
        	dialogHelper.warn("Operation cancelled");
        }
		
	}
    
    public static void updateFromExternalGeneralConfig(File generalConfigRootSearch) {
    	
    	List<File> matchedGeneralConf = FileSearchAdvanced.findOnlyFilesByMatch(generalConfigRootSearch, GeneralConfig.resourcesMappingDefPath, true, true, false);
    	
    	if (!matchedGeneralConf.isEmpty()) {
    		
    		try {
    			PropertiesDTO propertiesToRestore = MainStandAloneUpdater.getPropertiesToKeep(GeneralConfig.resourcesFilePathsMappings);
    			MainStandAloneUpdater.restorePropertiesToKeep(propertiesToRestore, GeneralConfig.resourcesMappingDefPath);
    			GeneralConfig.reloadProperties();
    			logger.info("General config file imported and reloaded from " + matchedGeneralConf.get(0));
    		} catch (Exception e) {
    			logger.error("Cannot import general config file from " + matchedGeneralConf.get(0));
    		}
    	} else {
    		logger.warn(GeneralConfig.resourcesMappingDefPath +	" not found into given root: " + generalConfigRootSearch);
    	}
    }

    public static void copyGeneralConfig(File generalConfigRootSearch) {
    	
		List<File> matchedGeneralConf = FileSearchAdvanced.findOnlyFilesByMatch(generalConfigRootSearch, GeneralConfig.resourcesMappingDefPath, true, true, false);
		
		if (!matchedGeneralConf.isEmpty()) {
			
			try {
				FileUtils.copyFile(matchedGeneralConf.get(0), new File(GeneralConfig.resourcesMappingDefPath));
				GeneralConfig.reloadProperties();
				logger.info("General config file imported and reloaded from " + matchedGeneralConf.get(0));
			} catch (IOException e) {
				logger.error("Cannot import general config file from " + matchedGeneralConf.get(0));
			}
		} else {
			logger.warn(GeneralConfig.resourcesMappingDefPath +	" not found into given root: " + generalConfigRootSearch);
		}
		
    }

	public static void importIniFileOrDir() {
		File selectedImportDir = MainGuiGeneralAppStarter.fileDialogHelper.fileAndDirRead(MainGUI.mainAPP.getMainFrame());
		if (selectedImportDir!=null && selectedImportDir.isDirectory()) {
			try {
				FileWorker.copyDirectoryContents(selectedImportDir, new File(GeneralConfig.DEFAULT_INI_FOLDER), false, "ini");
				MainGuiGeneralAppStarter.dialogHelper.info("Directory :" + selectedImportDir.getName() + " added in the configurations -> " + GeneralConfig.DEFAULT_INI_FOLDER, "Directory added to config pool :)");
				logger.debug("Directory :" + selectedImportDir.getName() + " added to the configurations -> " + GeneralConfig.DEFAULT_INI_FOLDER);
			} catch (IOException e1) {
				dialogHelper.error("Directory not imported in " + GeneralConfig.DEFAULT_INI_FOLDER,	". Some error occurred :(");
				logger.error("Directory not imported, some error occurred :( ");
			} 
		}else if(selectedImportDir!=null && selectedImportDir.isFile() && selectedImportDir.getName().toLowerCase().endsWith(".ini")) {
			try {
				FileUtils.copyFileToDirectory(selectedImportDir, new File(GeneralConfig.DEFAULT_INI_FOLDER));
				logger.debug("Directory :" + selectedImportDir.getName() + " added to the configurations -> " + GeneralConfig.DEFAULT_INI_FOLDER);
			} catch (IOException e) {
				logger.error("An error occurred during import :(");
				dialogHelper.error("Ooooops... An Error occurred during Import!", "An Error Occurred");
			}
		}
	}

	public static boolean saveCurrentConf(INItializer configuration, JFrame parent) {
		JOptionHelperExtended dialogHelper = new JOptionHelperExtended(parent);
		if(dialogHelper.yesOrNo("Are you sure to overwrite current configuration?", "Overwrite current Config?")) {
			configuration.writeCurrentConfigIniFile();
			dialogHelper.info(configuration.getIniFile().getFile().getAbsolutePath() + " -> Saved", "Configuration saved");
			return true;
		}
		return false; 
	}

}

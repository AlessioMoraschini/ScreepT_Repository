/**
 *
 * =========================================================================================
 *  Copyright (C) 2019-2020
 *
 *  AM-Design-Development - (Alessio Moraschini)
 * =========================================================================================
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package screept.gui.utils.filedialog;

import various.common.light.i18n.I18n;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.ini4j.Ini;
import org.ini4j.Wini;

import gui.commons.dialogutils.ExtensionFileFilter;
import gui.commons.dialogutils.GenericFileChooserDialog;
import gui.commons.dialogutils.JOptionHelperExtended;
import initializer.configs.impl.INItializer;
import main.MainStandAloneUpdater;
import main.om.PropertiesDTO;
import maingUI.MainGUI;
import maingUI.MainGuiGeneralAppStarter;
import resources.GeneralConfig;
import various.common.light.files.FileVarious;
import various.common.light.files.FileWorker;
import various.common.light.files.search.FileNameSearchAdvanced;
import various.common.light.gui.GuiUtils;

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
            	lblAvviso.setText(I18n.t("cfg.status_loaded_new"));
    	    	textA.setText("");
    	    	textA.append("chosen: " + iniRead.getAbsolutePath());
    	    	textA.append("\n\n\n### NEW INI CONFIGURATION LOADED ###\n\n -> ");
    			textA.append( fileINI.toStringa());
    	        
			} else {
				dialogHelper.error(I18n.t("cfg.error_extension")+ backupINI.getIniFile().getFile().getName());
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
    	    	dialogHelper.info(I18n.t("app.configuration_loaded"));
    	        
			} else {
				dialogHelper.error(I18n.t("cfg.error_extension")+ backupINI.getIniFile().getFile().getName());
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
			File defaultLauncherIni = new File(GeneralConfig.DEFAULT_LAUNCHER_INI_PATH);
			String defaultIniParentDir = defaultIni.getParentFile().getName();
			String defaultLauncherIniParentDir = defaultLauncherIni.getParentFile().getName();
			List<File> matchedResourcesFolders = FileNameSearchAdvanced.findOnlyFoldersByMatch(sourceFolder, defaultIniParentDir, true, true, false, false, Long.MAX_VALUE);
			List<File> matchedLauncherFolders = FileNameSearchAdvanced.findOnlyFoldersByMatch(sourceFolder, defaultLauncherIniParentDir, true, true, false, false, Long.MAX_VALUE);
			
			for(File launcherFolder : matchedLauncherFolders) {
				List<File> matchedIniLauncherConf = FileNameSearchAdvanced.findOnlyFilesByMatch(launcherFolder, defaultLauncherIni.getName(), true, true, false, false, Long.MAX_VALUE);
				try {
					FileUtils.copyFileToDirectory(matchedIniLauncherConf.get(0), defaultLauncherIni.getParentFile(), true);
				} catch (Exception e1) {
					String safeFrom = matchedIniLauncherConf.isEmpty() ? " NULL " : matchedIniLauncherConf.get(0).getAbsolutePath();
					logger.error("Can't copy configuration from " + safeFrom + " to " + defaultLauncherIni.getParentFile());
				}
			}
			
			for (File resourcesFolder : matchedResourcesFolders) {
				List<File> matchedIniConf = FileNameSearchAdvanced.findOnlyFilesByMatch(resourcesFolder, defaultIni.getName(), true, true, false, false, Long.MAX_VALUE);
				if (!matchedIniConf.isEmpty()) {
					boolean confirmation = MainGuiGeneralAppStarter.dialogHelper.yesOrNo(
							GuiUtils.encapsulateInHtml("Are you sure to overwrite default configuration with the following?<br> " + matchedIniConf.get(0)), "Overwrite current?");
					
					if(confirmation) {
						ok = copyNloadIniAsDefault(matchedIniConf.get(0));
						updateFromExternalGeneralConfig(resourcesFolder.getParentFile());
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
			// cancelled by user: no message needed
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
		dialogHelper = new JOptionHelperExtended(MainGUI.mainAPP.getMainFrame());
        if (selectedFile != null) {
            try {
            	selectedFile = FileVarious.changeExtension(selectedFile, ".ini");
            	boolean write = true;
            	if (selectedFile.exists()) {
            		write = dialogHelper.yesOrNo(I18n.t("cfg.file_2") + selectedFile.getName() + " " + I18n.t("cfg.export_file_exists"));
            	}
            	if (write) {
            		// the file in use is copied as it is on disk: the configuration in memory is not touched
            		FileUtils.copyFile(fileINI.getIniFile().getFile(), selectedFile);
            		dialogHelper.info(I18n.t("cfg.file_2") + selectedFile.getName() + " " + I18n.t("cfg.export_file_created"));
            		if (lblAvviso != null) lblAvviso.setText(I18n.t("cfg.status_exported"));
            	}
			} catch (IOException e) {
				logger.error(e);
			}
        }
    }

    public static void updateFromExternalGeneralConfig(File generalConfigRootSearch) {
    	
    	List<File> matchedGeneralConf = FileNameSearchAdvanced.findOnlyFilesByMatch(generalConfigRootSearch, GeneralConfig.resourcesMappingDefPath, true, true, false, false, Long.MAX_VALUE);
    	
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
    	
		List<File> matchedGeneralConf = FileNameSearchAdvanced.findOnlyFilesByMatch(generalConfigRootSearch, GeneralConfig.resourcesMappingDefPath, true, true, false, false, Long.MAX_VALUE);
		
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
		if(dialogHelper.yesOrNo(I18n.t("app.are_you_sure_to_overwrite_current"), "Overwrite current Config?")) {
			configuration.writeCurrentConfigIniFile();
			dialogHelper.info(configuration.getIniFile().getFile().getAbsolutePath() + " -> Saved", "Configuration saved");
			return true;
		}
		return false; 
	}

}

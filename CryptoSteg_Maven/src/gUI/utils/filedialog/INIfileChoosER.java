package gUI.utils.filedialog;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextArea;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.ini4j.Ini;
import org.ini4j.Wini;

import amfileutils.FileStuff;
import dialogutils.JOptionHelperExtended;
import mainobj.INItializer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.HeadlessException;
import java.io.File;
import java.io.IOException;

public class INIfileChoosER extends JDialog{
	private static final long serialVersionUID = 606461817724105154L;

	// INItializer logger creation		
	static Logger logger = Logger.getLogger(INIfileChoosER.class); 
	
	private JOptionHelperExtended dialogHelper;
	
	/**
	 * Read file INI updating textArea and label
	 * */
	public INIfileChoosER(INItializer fileINI, JTextArea textA, JLabel lblAvviso) {
		
		JFileChooser fileChooser = new CustomFileChooserOnTop();			
		fileChooser.setDialogTitle("Load Configuration .INI File");
		fileChooser.setCurrentDirectory(new File("INI_FILES"));		
		final INItializer backupINI = fileINI;
		int returnVal = fileChooser.showOpenDialog(this.getParent());
		
		dialogHelper = new JOptionHelperExtended(this);
		
        if (returnVal == JFileChooser.APPROVE_OPTION) {
        	String fileExtension = FilenameUtils.getExtension(fileChooser.getSelectedFile().getName());
            if(fileChooser.getSelectedFile()!=null && fileExtension.equalsIgnoreCase("ini"))try {
				fileINI.setIniFile(new Wini(fileChooser.getSelectedFile()));
				fileINI.setIniReadFile(new Wini(fileChooser.getSelectedFile()));
//            	fileINI = new INItializer(new Wini(fileChooser.getSelectedFile())); -> bug, loose reference on main
            	fileINI.readConfigIniFile();
            	lblAvviso.setText("[Configuration Loaded!]");
    	    	textA.setText("");
    	    	textA.append("chosen: "+fileChooser.getSelectedFile().getAbsolutePath());
    	    	textA.append("\n\n\n### NEW INI CONFIGURATION LOADED ###\n\n -> ");
    			textA.append( fileINI.toStringa());
    	        
			} catch (IOException e) {
				logger.error(e);
			}else {
				if(!fileExtension.equalsIgnoreCase("ini")) {
					dialogHelper.error("Cannot load configuration file with extension different from '.ini'. \n Back to "+ backupINI.getIniFile().getFile().getName());
				}
				if(fileChooser.getSelectedFile()==null) {
					dialogHelper.warn("No file was selected, load aborted. :(");
				}
				fileINI=backupINI;
			}
            //fileINI.writeCurrentConfigIniFile();
            
            
        }else {
        	textA.setText("");
	    	textA.append("=> No File selected! ");
	    	textA.append("\n\n\n=> Same INI CONFIGURATION maintained \n\n -> ");
			textA.append( fileINI.toStringa());
//			fileChooser.setCurrentDirectory(new File(fileINI.DEFAULT_INI_PATH).getParentFile());
		}
            
            //This is where a real application would open the file.
    }
	
	/**
	 * Read file INI
	 * */
	public INIfileChoosER(INItializer fileINI, String defDir) {
		
		JFileChooser fileChooser = new CustomFileChooserOnTop();
		fileChooser.setDialogTitle("Load .INI File");
		fileChooser.setCurrentDirectory(new File(defDir));		
		final INItializer backupINI = fileINI;
		int returnVal = fileChooser.showOpenDialog(this.getParent());
		
		dialogHelper = new JOptionHelperExtended(this);
		
        if (returnVal == JFileChooser.APPROVE_OPTION) {
        	String fileExtension = FilenameUtils.getExtension(fileChooser.getSelectedFile().getName());
            if(fileChooser.getSelectedFile()!=null && fileExtension.equalsIgnoreCase("ini"))try {
				fileINI.setIniFile(new Wini(fileChooser.getSelectedFile()));
				fileINI.setIniReadFile(new Wini(fileChooser.getSelectedFile()));
            	fileINI.readConfigIniFile();
    	    	logger.debug("chosen: "+fileChooser.getSelectedFile().getAbsolutePath());
    	    	logger.debug("\n\n\n### NEW INI CONFIGURATION LOADED ###\n\n -> "+fileINI.toStringa());
    	    	dialogHelper.info("Configuration Loaded!");
    	        
			} catch (IOException e) {
				logger.error("File not exist or cannot be read! :("+"IO error\n");
				dialogHelper.error("File not exist or cannot be read! :(", "IO error");
			}else {
				if(!fileExtension.equalsIgnoreCase("ini")) {
					dialogHelper.error("Cannot load configuration file with extension different from '.ini'. \n Back to "+ backupINI.getIniFile().getFile().getName());
				}
				if(fileChooser.getSelectedFile()==null) {
					dialogHelper.warn("No file was selected, load aborted. :(");
				}
				fileINI=backupINI;
			}
            //fileINI.writeCurrentConfigIniFile();
            
            
        }else {
        	logger.debug("=> No File selected! ");
        	logger.debug("\n\n\n=> INI CONFIGURATION maintained \n\n -> ");
		}
    }
        
	/**
	 * Export file INI updating label (use null for label if you don't want to use it)
	 */
    public INIfileChoosER(INItializer fileINI, JLabel lblAvviso) {
		
    	JFileChooser fileChooser = new CustomFileChooserOnTop();	
		fileChooser.setDialogTitle("Save Configuration .INI File");;
		fileChooser.setCurrentDirectory(new File("INI_FILES"));		
		File selectedFile;
		File backupIni = fileINI.getIniFile().getFile();
		boolean done = false;
		int returnVal = fileChooser.showSaveDialog(this.getParent());
		dialogHelper = new JOptionHelperExtended(this);
		
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            try {
            	selectedFile = fileChooser.getSelectedFile();
            	selectedFile = FileStuff.changeExtension(selectedFile, ".ini");
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
    				fileINI.setIniFile(new Wini(selectedFile));
    				fileINI.setIniReadFile(new Ini(selectedFile));
    				fileINI.writeCurrentConfigIniFile();
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
		
		getContentPane().add(fileChooser, BorderLayout.NORTH);
	}
	
	private class CustomFileChooserOnTop extends JFileChooser{
		private static final long serialVersionUID = 7362401687903210645L;

		@Override
		protected JDialog createDialog(Component parent) throws HeadlessException {
			// intercept the dialog created by JFileChooser
			JDialog dialog = super.createDialog(parent);
			dialog.setAlwaysOnTop(true);
			dialog.setModal(true); // set modality (or setModalityType)
			return dialog;
		}
	}
}

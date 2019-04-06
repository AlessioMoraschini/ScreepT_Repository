package gUI.utils.filedialog;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.ini4j.Ini;
import org.ini4j.Wini;
import org.omg.CORBA.INITIALIZE;

import amfileutils.FileStuff;
import iNIT.INItializer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.TextArea;
import java.io.File;
import java.io.IOException;

public class INIfileChoosER extends JDialog{
	
	// INItializer logger creation		
	static Logger logger = Logger.getLogger(INIfileChoosER.class); 
	
	private JFileChooser fileChooser;
	
	/**
	 * Costruttore per lettura file INI
	 * */
	public INIfileChoosER(INItializer fileINI, JTextArea textA) {
		
		fileChooser = new JFileChooser();			
		fileChooser.setDialogTitle("Load Configuration .INI File");
		fileChooser.setCurrentDirectory(new File("INI_FILES"));		
		final INItializer backupINI = fileINI;
		int returnVal = fileChooser.showOpenDialog(this.getParent());
		
        if (returnVal == JFileChooser.APPROVE_OPTION) {
        	String fileExtension = FilenameUtils.getExtension(fileChooser.getSelectedFile().getName());
            if(fileChooser.getSelectedFile()!=null && fileExtension.equalsIgnoreCase("ini"))try {
				fileINI.setIniFile(new Wini(fileChooser.getSelectedFile()));
				fileINI.setIniReadFile(new Wini(fileChooser.getSelectedFile()));
//            	fileINI = new INItializer(new Wini(fileChooser.getSelectedFile())); -> bug, loose reference on main
            	fileINI.readConfigIniFile();
    	    	textA.setText("");
    	    	textA.append("chosen: "+fileChooser.getSelectedFile().getAbsolutePath());
    	    	textA.append("\n\n\n### NEW INI CONFIGURATION LOADED ###\n\n -> ");
    			textA.append( fileINI.toStringa());
    	        
			} catch (IOException e) {
				e.printStackTrace();
			}else {
				if(!fileExtension.equalsIgnoreCase("ini")) {
					JOptionPane.showMessageDialog(fileChooser.getParent(), "Cannot load configuration file with extension different from '.ini'. \n Back to "+ backupINI.getIniFile().getFile().getName());
				}
				if(fileChooser.getSelectedFile()==null) {
					JOptionPane.showMessageDialog(fileChooser.getParent(), "No file was selected, load aborted. :(");
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
	 * Costruttore per lettura file INI
	 * */
	public INIfileChoosER(INItializer fileINI, String defDir) {
		
		fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Load .INI File");
		fileChooser.setCurrentDirectory(new File(defDir));		
		final INItializer backupINI = fileINI;
		int returnVal = fileChooser.showOpenDialog(this.getParent());
		
        if (returnVal == JFileChooser.APPROVE_OPTION) {
        	String fileExtension = FilenameUtils.getExtension(fileChooser.getSelectedFile().getName());
            if(fileChooser.getSelectedFile()!=null && fileExtension.equalsIgnoreCase("ini"))try {
				fileINI.setIniFile(new Wini(fileChooser.getSelectedFile()));
				fileINI.setIniReadFile(new Wini(fileChooser.getSelectedFile()));
            	fileINI.readConfigIniFile();
    	    	logger.debug("chosen: "+fileChooser.getSelectedFile().getAbsolutePath());
    	    	logger.debug("\n\n\n### NEW INI CONFIGURATION LOADED ###\n\n -> "+fileINI.toStringa());
    	    	JOptionPane.showMessageDialog(fileChooser.getParent(),"Configuration Loaded!");
    	        
			} catch (IOException e) {
				logger.error("File not exist or cannot be read! :("+"IO error\n");
				JOptionPane.showMessageDialog(fileChooser.getParent(), "File not exist or cannot be read! :(", "IO error", JOptionPane.ERROR_MESSAGE);
			}else {
				if(!fileExtension.equalsIgnoreCase("ini")) {
					JOptionPane.showMessageDialog(fileChooser.getParent(), "Cannot load configuration file with extension different from '.ini'. \n Back to "+ backupINI.getIniFile().getFile().getName());
				}
				if(fileChooser.getSelectedFile()==null) {
					JOptionPane.showMessageDialog(fileChooser.getParent(), "No file was selected, load aborted. :(");
				}
				fileINI=backupINI;
			}
            //fileINI.writeCurrentConfigIniFile();
            
            
        }else {
        	logger.debug("=> No File selected! ");
        	logger.debug("\n\n\n=> INI CONFIGURATION maintained \n\n -> ");
		}
    }
        
	// esporta backup file ini
    public INIfileChoosER(INItializer fileINI) {
		
		fileChooser = new JFileChooser();	
		fileChooser.setDialogTitle("Save Configuration .INI File");;
		fileChooser.setCurrentDirectory(new File("INI_FILES"));		
		File selectedFile;
		boolean done = false;
		int returnVal = fileChooser.showSaveDialog(this.getParent());
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            try {
            	selectedFile = fileChooser.getSelectedFile();
            	selectedFile = FileStuff.changeExtension(selectedFile, ".ini");
            	if(selectedFile.exists()) {
            		if(JOptionPane.showConfirmDialog(this.getContentPane(), "File "+selectedFile.getName()+" exists, overwrite it?")==JOptionPane.OK_OPTION) {
            			selectedFile.delete();	
                       	done = selectedFile.createNewFile();
            		}else {
        				JOptionPane.showMessageDialog(this.getContentPane(), "Operation cancelled");
            		}
            	}else {
                   	done = selectedFile.createNewFile();
            	}
            		   		
               	if(done) {
    				JOptionPane.showMessageDialog(this.getContentPane(), "File "+selectedFile.getName()+" succesfully created!");
    				fileINI.setIniFile(new Wini(selectedFile));
    				fileINI.setIniReadFile(new Ini(selectedFile));
    				fileINI.writeCurrentConfigIniFile();
//    		    	fileINI.readConfigIniFile();  -> cause of a bug
               	}
				
			} catch (IOException e) {
				e.printStackTrace();
			}  			
            
        }else{
        	JOptionPane.showMessageDialog(this.getContentPane(), "Operation cancelled");
//			fileChooser.setCurrentDirectory(new File(fileINI.DEFAULT_INI_PATH).getParentFile());	
        }
		
		getContentPane().add(fileChooser, BorderLayout.NORTH);
	}

	public JFileChooser getFileChooser(){
		return fileChooser;
	}
	
	
}

package gUI.utils.filedialog;

import java.awt.Component;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import uTILS.local.FileStuff;

public class DirectoryManagementGUI {
	
	// KeyGenPanel logger
	static Logger logger = Logger.getLogger(DirectoryManagementGUI.class); 
	
	public static String renameFirstLetterDirNames(Component parent, String defaultVisualizedDir) {
		int errors=0;
		File selectedFile;
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Select source dir/file to make uppercase");
		fileChooser.setCurrentDirectory(new File(defaultVisualizedDir));
		fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		int returnVal = fileChooser.showOpenDialog(parent);
		if (returnVal == JFileChooser.APPROVE_OPTION && fileChooser.getSelectedFile()!=null) {
            try {
            	selectedFile = fileChooser.getSelectedFile();
            	if(selectedFile.isFile()) {
            		if(JOptionPane.showConfirmDialog(fileChooser, "Are you sure to rename file: '"+selectedFile.getName()+
	            			"'?")==JOptionPane.OK_OPTION) {
            			defaultVisualizedDir = selectedFile.getParentFile().getAbsolutePath();
		            	errors=FileStuff.upperCaseSubFolder(selectedFile);
		            	JOptionPane.showMessageDialog(parent, "Renaming completed! \n\nCompleted with: "+errors+" Errors.");
            		}
            	}//END FILE
            	else if(selectedFile.isDirectory()) {
            		if(JOptionPane.showConfirmDialog(fileChooser, "Are you sure to rename folder: '"+selectedFile.getName()+
	            			"' and all files/directory in it?")==JOptionPane.OK_OPTION) {
            			defaultVisualizedDir = selectedFile.getAbsolutePath();
		            	errors=FileStuff.upperCaseSubFolder(selectedFile);
		            	JOptionPane.showMessageDialog(parent, "Renaming completed! \n\nCompleted with: "+errors+" Errors.");
            		}	
            	}// END DIRECTORY
            	if(!selectedFile.exists()) {
	            	JOptionPane.showMessageDialog(parent, "File does not exist! \nChoose an existing File or Directory ;)", "Error Executin' requested Action", JOptionPane.WARNING_MESSAGE);
            	}
            }catch(Exception exc) {
				JOptionPane.showMessageDialog(parent, "Renaming cancelled! \n An error occurred.", "Error Executin' requested Action", JOptionPane.ERROR_MESSAGE);
				logger.error("Exception happened!", exc);
            }
		}else {
			JOptionPane.showMessageDialog(parent, "Renaming cancelled! ", "Skipped by user", JOptionPane.INFORMATION_MESSAGE);
		}
		
		return defaultVisualizedDir;
	}
}

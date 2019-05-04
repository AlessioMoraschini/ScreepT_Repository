package gUI.utils.filedialog;

import java.awt.Component;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import amfileutils.FileStuff;
import iNIT.INItializer;

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
	
	public static void makeDirsFromPath(JFrame parent,INItializer currentConf) {
		
		String dirToMake = JOptionPane.showInputDialog("Insert the complete Path of the directories to create (accepted separators=['\\','/'])");
		if (!dirToMake.equals("")) {
			File givenDir = new File(dirToMake);
			// TODO check for root existing
			boolean res = givenDir.mkdirs();
			if (res) {
				JOptionPane.showMessageDialog(parent, "Completed, Directories created!", "Directories created!", JOptionPane.INFORMATION_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(parent, "Some error occurred creating folders :(", "Some Error occurred!", JOptionPane.ERROR_MESSAGE);
			} 
		}else {
			JOptionPane.showMessageDialog(parent, "Void path, exiting.", "Some error occurred!", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	public static void deleteContentOfDir(JFrame parent,INItializer currentConf) {
		File selected = GenericFileChooserDialog.directoRead(parent, currentConf);
		if (selected!=null) {
			boolean res = FileStuff.deleteDirContentRecursive(selected);
			if (res) {
				JOptionPane.showMessageDialog(parent, "Completed, content deleted :)", "Completed, content deleted", JOptionPane.INFORMATION_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(parent, "Some error occurred, some files could be still present :(", "Some Error occurred!", JOptionPane.ERROR_MESSAGE);
			} 
		}else {
			JOptionPane.showMessageDialog(parent, "No directory selected, exiting.", "No directory selected!", JOptionPane.INFORMATION_MESSAGE);
		}
	}
	
	public static void deleteDirContentFiles(JFrame parent,INItializer currentConf) {
		File selected = GenericFileChooserDialog.directoRead(parent, currentConf);
		if (selected != null) {
			boolean res = FileStuff.emptyDirContentFolderOnly(selected);
			if (res) {
				JOptionPane.showMessageDialog(parent, "Completed, folders content deleted :)", "Completed, folders content deleted", JOptionPane.INFORMATION_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(parent, "Some error occurred, some files could be still present :(", "Some Error occurred!", JOptionPane.ERROR_MESSAGE);
			} 
		}else {
			JOptionPane.showMessageDialog(parent, "No directory selected, exiting.", "No directory selected!", JOptionPane.INFORMATION_MESSAGE);
		}
	}
	
	public static void deleteContentOfDirByExt(JFrame parent,INItializer currentConf) {
		File selected = GenericFileChooserDialog.directoRead(parent, currentConf);
		if (selected != null) {
			String deleteExtensionMatch = JOptionPane.showInputDialog("Specify wich extensions you want to delete, separeted by a comma (ex. 'jpg, txt, png')");
			boolean res = FileStuff.emptyDirContentByExt(selected, deleteExtensionMatch);
			if (res) {
				JOptionPane.showMessageDialog(parent, "Completed, folders content deleted :)", "Completed, folders content deleted", JOptionPane.INFORMATION_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(parent, "Some error occurred, some files could be still present :(", "Some Error occurred!", JOptionPane.ERROR_MESSAGE);
			} 
		}else {
			JOptionPane.showMessageDialog(parent, "No directory selected, exiting.", "No directory selected!", JOptionPane.INFORMATION_MESSAGE);
		}
	}
	
	public static void copyDirStructure(JFrame parent,INItializer currentConf) {
		File selected = GenericFileChooserDialog.directoRead(parent, currentConf);
		if (selected != null) {
			File destination = GenericFileChooserDialog.directoWrite(parent, currentConf);
			if (destination != null) {
				boolean res = false;
				try {
					res = FileStuff.copyDirStructure(selected, destination, true);
				} catch (IOException e) {
					logger.error(e);
					JOptionPane.showMessageDialog(parent, "IO Error: you cannot put destination inside source, choose another destination",
							"IO Error: cannot put destination inside source", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (res) {
					JOptionPane.showMessageDialog(parent, "Completed, folder directories content copied :)",
							"Completed, folders directories content copied", JOptionPane.INFORMATION_MESSAGE);
				} else {
					JOptionPane.showMessageDialog(parent, "Some error occurred, some folders could be missing :(",
							"Some Error occurred!", JOptionPane.ERROR_MESSAGE);
				} 
			}else {
				JOptionPane.showMessageDialog(parent, "No destination directory selected, exiting.", "Destination directory missing!", JOptionPane.INFORMATION_MESSAGE);
			}
		}else {
			JOptionPane.showMessageDialog(parent, "No source directory selected, exiting.", "Source directory missing!", JOptionPane.INFORMATION_MESSAGE);
		}
	}
}

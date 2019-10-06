package gUI.utils.filedialog;

import java.awt.Component;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import amfileutils.FileStuff;
import amfileutils.om.DeleteFilesByExtensionDTO;
import dialogutils.JOptionHelper;
import mainobj.INItializer;

public class DirectoryManagementGUI {
	
	// KeyGenPanel logger
	static Logger logger = Logger.getLogger(DirectoryManagementGUI.class); 
			
	public static String renameFirstLetterDirNames(Component parent, String defaultVisualizedDir) {
		JOptionHelper dialog = new JOptionHelper(parent);

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
		            	dialog.info("Renaming completed! \n\nCompleted with: "+errors+" Errors.", null);
            		}
            	}//END FILE
            	else if(selectedFile.isDirectory()) {
            		if(JOptionPane.showConfirmDialog(fileChooser, "Are you sure to rename folder: '"+selectedFile.getName()+
	            			"' and all files/directory in it?")==JOptionPane.OK_OPTION) {
            			defaultVisualizedDir = selectedFile.getAbsolutePath();
		            	errors=FileStuff.upperCaseSubFolder(selectedFile);
		            	dialog.info("Renaming completed! \n\nCompleted with: "+errors+" Errors.", null);
            		}	
            	}// END DIRECTORY
            	if(!selectedFile.exists()) {
            		dialog.warn("File does not exist! \nChoose an existing File or Directory ;)", "Error Executin' requested Action");
            	}
            }catch(Exception exc) {
				dialog.error("Renaming cancelled! \n An error occurred.", "Error Executin' requested Action");
				logger.error("Exception happened!", exc);
            }
		}else {
			dialog.info("Renaming cancelled! ", "Skipped by user");
		}
		
		return defaultVisualizedDir;
	}
	
	public static void makeDirsFromPath(JFrame parent,INItializer currentConf) {
		JOptionHelper dialog = new JOptionHelper(parent);

		String dirToMake = JOptionPane.showInputDialog("Insert the complete Path of the directories to create (accepted separators=['\\','/'])");
		if (!dirToMake.equals("")) {
			File givenDir = new File(dirToMake);
			// TODO check for root existing
			boolean res = givenDir.mkdirs();
			if (res) {
				dialog.info("Completed, Directories created!", "Directories created!");
			} else {
				dialog.error("Some error occurred creating folders :(", "Some Error occurred!");
			} 
		}else {
			dialog.error("Void path, exiting.", "Some error occurred!");
		}
	}
	
	public static void deleteContentOfDir(JFrame parent,INItializer currentConf) {
		JOptionHelper dialog = new JOptionHelper(parent);

		File selected = GenericFileChooserDialog.directoRead(parent, currentConf);
		if (selected!=null) {
			boolean res = FileStuff.deleteDirContentRecursive(selected);
			if (res) {
				dialog.info("Completed, content deleted :)", "Completed, content deleted");
			} else {
				dialog.error("Some error occurred, some files could be still present :(", "Some Error occurred!");
			} 
		}else {
			dialog.info("No directory selected, exiting.", "No directory selected!");
		}
	}
	
	public static void deleteDirContentFiles(JFrame parent,INItializer currentConf) {
		JOptionHelper dialog = new JOptionHelper(parent);

		File selected = GenericFileChooserDialog.directoRead(parent, currentConf);
		if (selected != null) {
			boolean res = FileStuff.emptyDirContentFolderOnly(selected);
			if (res) {
				dialog.info("Completed, folders content deleted :)", "Completed, folders content deleted");
			} else {
				dialog.error("Some error occurred, some files could be still present :(", "Some Error occurred!");
			} 
		}else {
			dialog.warn("No directory selected, exiting.", "No directory selected!");
		}
	}
	
	public static void deleteContentOfDirByExt(JFrame parent,INItializer currentConf) {
		JOptionHelper dialog = new JOptionHelper(parent);

		File selected = GenericFileChooserDialog.directoRead(parent, currentConf);
		DeleteFilesByExtensionDTO response;
		String notFoundExtensions = "";

		if (selected != null) {
			String deleteExtensionMatch = JOptionPane.showInputDialog("Specify wich extensions you want to delete, separated by a comma (ex. 'jpg, txt, png')");
			response = FileStuff.emptyDirContentByExt(selected, deleteExtensionMatch);
			if (response.getNotFoundExtensionResult().size() != 0) {
				notFoundExtensions = " These extensions have 0 matches: " + response.getNotFoundExtensionResult();
			}
			if (response.isEsit() && !response.isNothingFound()) {
				dialog.info("Completed, folders content deleted :)" + notFoundExtensions, "Completed, folders content deleted");
			} else if(response.isNothingFound()){
				dialog.warn("Nothing found!" + notFoundExtensions, "Nothing found!");
			} else {
				dialog.error("Some error occurred, some files could be still present :(" + notFoundExtensions, "Some Error occurred!");
			}
			
		}else {
			dialog.warn("No directory selected, exiting.", "No directory selected!");
		}
	}
	
	public static void copyDirStructure(JFrame parent,INItializer currentConf) {
		JOptionHelper dialog = new JOptionHelper(parent);

		File selected = GenericFileChooserDialog.directoRead(parent, currentConf);
		if (selected != null) {
			File destination = GenericFileChooserDialog.directoWrite(parent, currentConf);
			if (destination != null) {
				boolean res = false;
				try {
					res = FileStuff.copyDirStructure(selected, destination, true);
				} catch (IOException e) {
					logger.error(e);
					dialog.error("IO Error: you cannot put destination inside source, choose another destination",
							"IO Error: cannot put destination inside source");
					return;
				}
				if (res) {
					dialog.info("Completed, folder directories content copied :)",
							"Completed, folders directories content copied");
				} else {
					dialog.error("Some error occurred, some folders could be missing :(",
							"Some Error occurred!");
				} 
			}else {
				dialog.warn("No destination directory selected, exiting.", "Destination directory missing!");
			}
		}else {
			dialog.warn("No source directory selected, exiting.", "Source directory missing!");
		}
	}
}

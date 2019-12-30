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
import dialogutils.JOptionHelperExtended;
import maingUI.MainGUI;
import maingUI.MainGuiGeneralAppStarter;
import mainobj.INItializer;

public class FilesAndDirectoriesManagerGUI {
	
	// KeyGenPanel logger
	static Logger logger = Logger.getLogger(FilesAndDirectoriesManagerGUI.class); 
			
	public static String renameFirstLetterDirNames(Component parent, String defaultVisualizedDir) {
		JOptionHelperExtended dialog = new JOptionHelperExtended(parent);

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
            		if(JOptionPane.showConfirmDialog(fileChooser, "Are you sure to rename file: '"+selectedFile.getName()+ "'?")==JOptionPane.OK_OPTION) {
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
			skippedAdvice();
		}
		
		return defaultVisualizedDir;
	}
	
	public static void makeDirsFromPath(JFrame parent,INItializer currentConf) {
		JOptionHelperExtended dialog = new JOptionHelperExtended(parent);

		String dirToMake = dialog.askForStringNullable("Insert the complete Path of the directories to create (accepted separators=['\\','/'])", "specify path for folder to create");
		if(dirToMake == null) {
			return;
			
		}else if (!dirToMake.equals("")) {
			
			if(!askConfirmation(dirToMake + " will be created, continue?", null)) {
				skippedAdvice();
				return;
			}
			
			File givenDir = new File(dirToMake);
			
			if(!FileStuff.isPathRootValid(dirToMake)) {
				return;
			}
			
			boolean res = givenDir.mkdirs();
			if (res) {
				dialog.info("Completed, Directories created!", "Directories created!");
			} else {
				dialog.error("Some error occurred creating folders :(", "Some Error occurred!");
			} 
		}else {
			noSelectionAdvice("Invalid Path");
		}
	}
	
	public static void deleteAllContent(JFrame parent,INItializer currentConf) {
		JOptionHelperExtended dialog = new JOptionHelperExtended(parent);

		File selected = MainGuiGeneralAppStarter.fileDialogHelper.directoRead(parent, "Delete All content of directory");
		if (selected!=null) {
			
			if(!askConfirmation("Are you sure to delete all content of [" + selected.getName() + "] ?", null)) {
				skippedAdvice();
				return;
			}
			
			boolean res = FileStuff.deleteDirContentRecursive(selected);
			if (res) {
				dialog.info("Completed, content deleted :)", "Completed, content deleted");
			} else {
				dialog.error("Some error occurred, some files could be still present :(", "Some Error occurred!");
			} 
		}else {
			noSelectionAdvice(null);
		}
	}
	
	public static void deleteOnlyFilesInFolder(JFrame parent,INItializer currentConf) {
		JOptionHelperExtended dialog = new JOptionHelperExtended(parent);

		File selected = MainGuiGeneralAppStarter.fileDialogHelper.directoRead(parent, "Delete All directory's content (Files only)");
		if (selected != null) {
			
			if(!askConfirmation("Are you sure to empty all folders contained in [" + selected.getName() + "] ?", null)) {
				skippedAdvice();
				return;
			}
			
			boolean res = FileStuff.emptyDirContentFolderOnly(selected);
			if (res) {
				dialog.info("Completed, folders content deleted :)", "Completed, folders content deleted");
			} else {
				dialog.error("Some error occurred, some files should be still present :(", "Some Error occurred!");
			} 
		}else {
			noSelectionAdvice(null);
		}
	}
	
	public static void deleteContentByExtension(JFrame parent,INItializer currentConf) {
		JOptionHelperExtended dialog = new JOptionHelperExtended(parent);

		File selected = MainGuiGeneralAppStarter.fileDialogHelper.directoRead(parent, "Delete directory content by Extension");
		DeleteFilesByExtensionDTO response;
		String notFoundExtensions = "";

		if (selected != null) {
			String deleteExtensionMatch = JOptionPane.showInputDialog("Specify wich extensions you want to delete, separated by a comma (ex. 'jpg, txt, png')");
			response = FileStuff.emptyDirContentByExt(selected, deleteExtensionMatch);
			
			if(!askConfirmation("Are you sure to delete all matches ["+ deleteExtensionMatch +"] in [" + selected.getName() + "] ?", null)) {
				skippedAdvice();
				return;
			}
			
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
			noSelectionAdvice(null);
		}
	}
	
	public static void copyDirStructure(JFrame parent,INItializer currentConf) {

		File selected = MainGuiGeneralAppStarter.fileDialogHelper.directoRead(parent, "Copy Directory structure");
		if (selected != null) {
			File destination = MainGuiGeneralAppStarter.fileDialogHelper.directoWrite(parent);
			if (destination != null) {
				boolean res = false;
				try {
					res = FileStuff.copyDirStructure(selected, destination, true);
				} catch (IOException e) {
					logger.error(e);
					MainGUI.dialogHelper.error("IO Error: you cannot put destination inside source, choose another destination",
							"IO Error: cannot put destination inside source");
					return;
				}
				if (res) {
					MainGUI.dialogHelper.info("Completed, folder directories content copied :)",
							"Completed, folders directories content copied");
				} else {
					MainGUI.dialogHelper.warn("Some error occurred, some folders could be missing in result!",
							"Some Error occurred!");
				} 
			}else {
				noSelectionAdvice("Destination directory missing");
			}
		}else {
			noSelectionAdvice("Source selection is empty");
		}
	}
	
	private static void skippedAdvice() {
		MainGUI.dialogHelper.info("Operation canceled!", "Skipped by user");
	}

	private static void noSelectionAdvice(String title) {
		title = title == null || title.isEmpty() ? "Empty selection" : title;
		MainGUI.dialogHelper.warn("Nothing selected, exiting.", title);
	}
	
	private static boolean askConfirmation(String msg, String title) {
		title = title == null || title.isEmpty() ? "Continue?" : title;
		return MainGUI.dialogHelper.yesOrNo(msg, title);
	}
}

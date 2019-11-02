package dialogutils;

import java.awt.Component;
import java.io.File;
import java.util.Arrays;

import javax.activation.UnsupportedDataTypeException;
import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.io.FilenameUtils;

import general.MyFileView;
import resources.GeneralConfig;
import variousutils.HandleString;

public class GenericFileChooser {
	
	public static String writerNames[] = ImageIO.getWriterFileSuffixes();
	public static String DEFAULT_HOME_PATH = "../";
	public static MyFileView fileView = new MyFileView();
	
	/**JFRAME - only img supported extension
	 * this method select a file from the user (only supported images files),if nothing is selected returns null
	 */
	public static File fileImgRead(JFrame parent) {

		JFileChooser fileChooser = new JFileChooser();		
		fileChooser.setDialogTitle("Select input Image File");
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setCurrentDirectory(new File(DEFAULT_HOME_PATH));
		fileChooser.setFileView(fileView);
		fileChooser.setPreferredSize(GeneralConfig.FILE_CHOOSER_SIZE);
		File correctFile = null;		
		
		int returnVal = fileChooser.showOpenDialog(parent);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            
        	File selectedFile = fileChooser.getSelectedFile();
        	String givenExtension = FilenameUtils.getExtension(selectedFile.getName());
        	if(selectedFile==null||!Arrays.asList(HandleString.getUpperCaseArray(writerNames)).contains(givenExtension.toUpperCase())) {
        		JOptionPane.showMessageDialog(parent, "Please select a valid file between "+Arrays.toString(writerNames), "Wrong File format", JOptionPane.ERROR_MESSAGE);
        	}else {
        		correctFile = selectedFile;
        		DEFAULT_HOME_PATH = correctFile.getAbsolutePath();
        	}
        }
        return correctFile;
	}
	
	/**JFRAME - only img supported extension
	 * this method select a file from the user (only supported images files),if nothing is selected returns null
	 * @throws UnsupportedDataTypeException if image type is not supported 
	 */
	public static File fileImgRead(JFrame parent, String ext) throws UnsupportedDataTypeException {

		JFileChooser fileChooser = new JFileChooser();		
		fileChooser.setDialogTitle("Select input Image File");
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setFileView(fileView);
		fileChooser.setFileFilter(new FileFilter() {
			
			@Override
			public String getDescription() {
				return "Only images of "+ext+" format";
			}
			
			@Override
			public boolean accept(File file) {
				if(file.getName().toLowerCase().endsWith(ext) || file.isDirectory()) {
					return true;
				}else return false;
			}
		});
		fileChooser.setCurrentDirectory(new File(DEFAULT_HOME_PATH));
		fileChooser.setPreferredSize(GeneralConfig.FILE_CHOOSER_SIZE);
		File correctFile = null;		
		
		int returnVal = fileChooser.showOpenDialog(parent);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            
        	File selectedFile = fileChooser.getSelectedFile();
        	String givenExtension = FilenameUtils.getExtension(selectedFile.getName());
        	if(selectedFile==null || !ext.equalsIgnoreCase(givenExtension)) {
        		throw new UnsupportedDataTypeException("File extension not supported! Please select a valid file between "+Arrays.toString(writerNames));
        	}else {
        		correctFile = selectedFile;
        		DEFAULT_HOME_PATH = correctFile.getAbsolutePath();
        	}
        }
        return correctFile;
	}
	
	/**
	 * JPANEL - every extension
	 * this method select a source file from the user,if nothing is selected returns null
	 */
	public static File fileRead(JFrame parent) {
		
		JFileChooser fileChooser = new JFileChooser();		
		fileChooser.setDialogTitle("Select input File");
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setCurrentDirectory(new File(DEFAULT_HOME_PATH));
		fileChooser.setFileView(fileView);
		fileChooser.setPreferredSize(GeneralConfig.FILE_CHOOSER_SIZE);
		File correctFile = null;
		
		int returnVal = fileChooser.showOpenDialog(parent);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            
        	File selectedFile = fileChooser.getSelectedFile();
//        	String givenExtension = FilenameUtils.getExtension(selectedFile.getName());
        	if(selectedFile==null) {
        		JOptionPane.showMessageDialog(parent, "Please select a file!", "File not selected", JOptionPane.ERROR_MESSAGE);
        	}else {
        		correctFile = selectedFile;
        		DEFAULT_HOME_PATH = correctFile.getAbsolutePath();
        	}
        }
        return correctFile;
	}
	
	/**
	 * JPANEL - every extension
	 * this method select a source file from the user,if nothing is selected returns null
	 */
	public static File fileRead(JPanel parent) {
		JOptionHelper dialog = new JOptionHelper(parent);
		
		JFileChooser fileChooser = new JFileChooser();		
		fileChooser.setDialogTitle("Select input File");
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setFileView(fileView);
		fileChooser.setPreferredSize(GeneralConfig.FILE_CHOOSER_SIZE);
		File correctFile = null;
		
		int returnVal = fileChooser.showOpenDialog(parent);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            
        	File selectedFile = fileChooser.getSelectedFile();
//        	String givenExtension = FilenameUtils.getExtension(selectedFile.getName());
        	if(selectedFile==null) {
        		dialog.error("Please select a file!", "File not selected");
        	}else {
        		correctFile = selectedFile;
        	}
        }
        return correctFile;
	}
	
	/**
	 * JCOMPONENT - every extension and every typer of source
	 * this method select a source file from the user,if nothing is selected returns null
	 */
	public static File fileAndDirRead(JComponent parent) {
		JOptionHelper dialog = new JOptionHelper(parent);

		JFileChooser fileChooser = new JFileChooser();		
		fileChooser.setDialogTitle("Select input File or Directory");
		fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		fileChooser.setCurrentDirectory(new File(DEFAULT_HOME_PATH));		
		fileChooser.setFileView(fileView);
		fileChooser.setPreferredSize(GeneralConfig.FILE_CHOOSER_SIZE);
		
		File correctFile = null;
		
		int returnVal = fileChooser.showOpenDialog(parent);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            
        	File selectedFile = fileChooser.getSelectedFile();
//        	String givenExtension = FilenameUtils.getExtension(selectedFile.getName());
        	if(selectedFile==null) {
        		dialog.error("Please select a file!", "File not selected");
        	}else {
        		correctFile = selectedFile;
        		DEFAULT_HOME_PATH = correctFile.getParentFile().getAbsolutePath();
        	}
        }
        return correctFile;
	}
	
	/**JFRAME - only img supported extension
	 * this method select a file from the user (only supported Image files),if nothing is selected returns null
	 */
	public static File fileImgWrite(JFrame parent, String defaultName) {
		JOptionHelper dialog = new JOptionHelper(parent);

		JFileChooser fileChooser = new JFileChooser();		
		fileChooser.setDialogTitle("Save Image File");
		fileChooser.setCurrentDirectory(new File(DEFAULT_HOME_PATH));	
		fileChooser.setSelectedFile(new File(defaultName));
		fileChooser.setFileView(fileView);
		fileChooser.setPreferredSize(GeneralConfig.FILE_CHOOSER_SIZE);
		
		File correctFile = null;
		
		int returnVal = fileChooser.showSaveDialog(parent);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            
        	File selectedFile = fileChooser.getSelectedFile();
        	String givenExtension = FilenameUtils.getExtension(selectedFile.getName());
        	if(selectedFile==null||!Arrays.asList(HandleString.getUpperCaseArray(writerNames)).contains(givenExtension.toUpperCase())) {
        		dialog.error("Please select a valid file between "+Arrays.toString(writerNames), "Wrong File format");
        	}else {
        		correctFile = selectedFile;
        		DEFAULT_HOME_PATH = correctFile.getParentFile().getAbsolutePath();
        	}
        }else {
        	dialog.info("Skipped after user request.", null);
        }
        return correctFile;
	}
	
	/**JFRAME - only directories src
	 * this method select a directory from the user (only directories),if nothing is selected returns null
	 */
	public static File directoRead(JFrame parent) {
		JOptionHelper dialog = new JOptionHelper(parent);

		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Choose Input Directory");
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fileChooser.setCurrentDirectory(new File(DEFAULT_HOME_PATH));
		fileChooser.setFileView(fileView);
		fileChooser.setPreferredSize(GeneralConfig.FILE_CHOOSER_SIZE);
		
		File correctFile = null;
		
		int returnVal = fileChooser.showOpenDialog(parent);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            
        	File selectedFile = fileChooser.getSelectedFile();
        	if(selectedFile==null||!selectedFile.isDirectory()) {
        		dialog.error("Please select a directory!", "Select a directory, please");
        	}else {
        		correctFile = selectedFile;
        		DEFAULT_HOME_PATH = correctFile.getParentFile().getAbsolutePath();
        	}
        }
        return correctFile;
	}
	
	/**JFRAME - only directories src
	 * this method select a directory from the user (only directories),if nothing is selected returns null
	 */
	public static File directoRead(JFrame parent, String title) {
		
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle(title);
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fileChooser.setCurrentDirectory(new File(DEFAULT_HOME_PATH));
		fileChooser.setFileView(fileView);
		fileChooser.setPreferredSize(GeneralConfig.FILE_CHOOSER_SIZE);
		
		File correctFile = null;
		
		int returnVal = fileChooser.showOpenDialog(parent);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            
        	File selectedFile = fileChooser.getSelectedFile();
        	if(selectedFile==null||!selectedFile.isDirectory()) {
        		JOptionPane.showMessageDialog(parent, "Please select a directory!", "Select a directory, please", JOptionPane.ERROR_MESSAGE);
        	}else {
        		correctFile = selectedFile;
        		DEFAULT_HOME_PATH = correctFile.getParentFile().getAbsolutePath();
        	}
        }
        return correctFile;
	}
	
	/**JFRAME - only directories dest
	 * this method select a directory from the user (only directories)to write a copy of current
	 * folder, if nothing is selected returns null
	 */
	public static File directoWrite(JFrame parent) {
		JOptionHelper dialog = new JOptionHelper(parent);

		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fileChooser.setDialogTitle("Choose output Directory");
		fileChooser.setCurrentDirectory(new File(DEFAULT_HOME_PATH));
		fileChooser.setFileView(fileView);
		fileChooser.setPreferredSize(GeneralConfig.FILE_CHOOSER_SIZE);
		
		File correctFile = null;
		//JOptionPane optPane = new JOptionPane();
		
		
		int returnVal = fileChooser.showSaveDialog(parent);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            
        	File selectedFile = fileChooser.getSelectedFile();
        	if(selectedFile==null||!selectedFile.isDirectory()) {
        		dialog.error("Please select a directory!", "Select a directory, please");
        	}else {
        		correctFile = selectedFile;
        		DEFAULT_HOME_PATH = correctFile.getAbsolutePath();
        	}
        }
        return correctFile;
	}
	
	/**
	 * open dialog in parent JComponent, displaying msg
	 * Check first precondition, if true ask to user else return false.
	 * After, if OK executes actionOK.
	 * 
	 * @return if OK return true, NO return false, else return null if cancel is pressed or window closed
	 */
	public static Boolean askYorN(String title, String msg, Component parent, boolean precondition) {
			int askSave = JOptionPane.CANCEL_OPTION;
			
			if (precondition) {
				askSave = JOptionPane.showConfirmDialog(parent, msg, title, JOptionPane.YES_NO_CANCEL_OPTION);
			}else {
				return false;
			}
			
			if(askSave == JOptionPane.OK_OPTION){
				return true;
				
			}else if(askSave == JOptionPane.NO_OPTION) {
				return false;
			}
			
			return null;
	}

	public static String getDEFAULT_HOME_PATH() {
		return DEFAULT_HOME_PATH;
	}

	public static void setDEFAULT_HOME_PATH(String dEFAULT_HOME_PATH) {
		DEFAULT_HOME_PATH = dEFAULT_HOME_PATH;
	}
}

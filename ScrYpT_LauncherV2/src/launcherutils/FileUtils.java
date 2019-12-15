package launcherutils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

public class FileUtils {
	
	static Logger logger = Logger.getLogger(FileUtils.class); 
	
	/**
	 * This method is meant to delete all folder content, deleting recursively also the folders inside it.
	 * the given folder won't be deleted, only it's content will be.
	 * 
	 * @param folder the source folder to be emptied
	 * @return deleted: false if at least one deletion have incurred in some errors
	 */
	public static boolean deleteDirContentRecursive(File folder) {
		boolean deleted = true;
		long startTime = System.currentTimeMillis();
		logger.info("deleteDirContentRecursive -> Start...");
		
		File[] files = folder.listFiles();
	    if(files!=null) { //some JVMs return null for empty dirs
	        for(File f: files) {
	            if(f.isDirectory()) {
	                boolean del = deleteDirContentInclRoot(f);
	                if(del==false) {
	                	deleted = false;
	                }
	            } else {
	                boolean del = f.delete();
	                if(del==false) {
	                	deleted = false;
	                }
	            }
	        }
	    }

	    logger.info("deleteDirContentRecursive -> Finished in " + String.valueOf(System.currentTimeMillis()-startTime) + " ms");
	    return deleted;	
	}
	
	/**
	 * same as deleteDirContentRecursive but this one deletes also the root folder
	 * @param folder
	 * @return
	 */
	public static boolean deleteDirContentInclRoot(File folder) {
		boolean deleted = true;
		long startTime = System.currentTimeMillis();
		logger.info("deleteDirContentInclRoot -> Start...");
		
		File[] files = folder.listFiles();
	    if(files!=null) { //some JVMs return null for empty dirs
	        for(File f: files) {
	            if(f.isDirectory()) {
	                boolean del = deleteDirContentInclRoot(f);
	                if(del==false) {
	                	deleted = false;
	                }
	            } else {
	                boolean del = f.delete();
	                if(del==false) {
	                	deleted = false;
	                }
	            }
	        }
	        // at the end delete given container folder too
	        folder.delete();
	    }

	   logger.info("deleteDirContentInclRoot -> Finished in " + String.valueOf(System.currentTimeMillis()-startTime) + " ms");
	    return deleted;	
	}
	
	public static boolean writeStringToFile(String source, File outFile, boolean overwrite) throws IOException {
		if(outFile.exists()) {
			if (overwrite) {
				boolean deleted = outFile.delete();
				if(!deleted) {
					return false;
				}
			}else {
				return false;
			}
		}

		boolean recreated = outFile.createNewFile();
		if(!recreated) {
			return false;
		}
		
		OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(outFile), System.getProperty("file.encoding"));
		PrintWriter out = new PrintWriter(outputStreamWriter);
		out.write(source);
		
		out.close();
		outputStreamWriter.close();
		
		return true;
	}
	
	public static File directoRead(JFrame parent, String title) {
		
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle(title);
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		File correctFile = null;
		
		int returnVal = fileChooser.showOpenDialog(parent);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            
        	File selectedFile = fileChooser.getSelectedFile();
        	if(selectedFile==null||!selectedFile.isDirectory()) {
        		JOptionPane.showMessageDialog(parent, "Please select a directory!", "Select a directory, please", JOptionPane.ERROR_MESSAGE);
        	}else {
        		correctFile = selectedFile;
        	}
        }else {
        	JOptionPane.showMessageDialog(parent, "Skipped after user request.");
        }
        return correctFile;
	}
}

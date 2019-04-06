package amfileutils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DecimalFormat;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

public class FileStuff {

	// FileStuff logger creation		
	static Logger logger = Logger.getLogger(FileStuff.class); 
		
	/**
	 * Change extension to the given one and return the new file reference. Be careful, the returned file is not physically created!
	 * NB the extension must be preceeded by a dot
	 */
	public static File changeExtension(File file, String newExtension) {
		int extensionLenght = (FilenameUtils.getExtension(file.getName())).length();
		String noExt="";
		if(extensionLenght==0) {
			noExt = file.getAbsolutePath().substring(0, file.getAbsolutePath().length()-(extensionLenght));
		}else {
			noExt = file.getAbsolutePath().substring(0, file.getAbsolutePath().length()-(extensionLenght+1));
		}
		
		file= new File(noExt + newExtension);
		logger.debug("NEW NAME: "+file.getAbsolutePath());
		return file;
	}
	
	/**
	 * Change extension to the given one and return the new String name.
	 * NB the extension must be preceeded by a dot
	 */
	public static String changeExtension(String originalName, String newExtension) {
		int extensionLenght = (FilenameUtils.getExtension(originalName)).length();
		String noExt="";
		if(extensionLenght==0) {
			noExt = originalName.substring(0, originalName.length()-(extensionLenght));
		}else {
			noExt = originalName.substring(0, originalName.length()-(extensionLenght+1));
		}
		
		String newName = noExt + newExtension;
		logger.debug("NEW NAME: "+newName);
		return (newName);

	}
	
	/**
	 * This method is meant to delete all folder content, deleting recursively also the folders inside it.
	 * the given folder won't be deleted, only it's content will be.
	 * 
	 * @param folder the source folder to be emptied
	 * @return deleted: false if at least one deletion have incurred in some errors
	 */
	public static boolean deleteDirContentRecursive(File folder) {
		boolean deleted = true;
		
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
	    return deleted;	
	}
	
	/**
	 * same as deleteDirContentRecursive but this one deletes also the root folder
	 * @param folder
	 * @return
	 */
	public static boolean deleteDirContentInclRoot(File folder) {
		boolean deleted = true;
		
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
	    return deleted;	
	}
	
	/**
	 * This method is meant to delete all folder content, excluding sub-directories and their content.
	 * the given folder won't be deleted, only it's content files will be.
	 * 
	 * @param folder the source folder, which content files have to be deleted
	 * @return deleted: false if at least one deletion have incurred in some errors
	 */
	public static boolean deleteDirContent(File folder) {
		boolean deleted = false;
		
		File[] files = folder.listFiles();
	    if(files!=null) { //some JVMs return null for empty dirs
	        for(File f: files) {
	            if(f.isFile()) {
	            	deleted = f.delete();
	            } 
	        }
	    }
	    return deleted;	
	}
	
	/**
	 * get number of files and directories in a given folder, including subdirectories if countDirAsOne is true
	 * @param directory the root directory to search in
	 * @return
	 */
	public static int countFilesInDirectory(File directory, boolean countDirAsOne) {
	  int count = 0;
	  for (File file : directory.listFiles()) {
	      if (file.isFile()) {
	          count++;
	      }
	      if (file.isDirectory()) {
	    	  if(countDirAsOne) {
	    		  count++;
	    	  }
	          count += countFilesInDirectory(file, countDirAsOne);
	      }
	  }
	  return count;
	}
	
	/**
	 * This method rename every single file or directory contained in the given one as parameter,
	 * making the name starting with an uppercase letter.
	 * @param rootDir the source directory
	 * @return success the number of the renamed elements
	 */
	public static int upperCaseSubFolder(File rootDir) {
		int total=0;
		boolean esit=false;
		if (rootDir!=null) {
			if (rootDir.isFile()) {
				esit = renameFileUpperFirst(rootDir);
				if (esit == false)
					total++;
			} else {
				renameFileUpperFirst(rootDir);
				for (File file : rootDir.listFiles()) {
					// se � una directory uso la ricorsione
					if (file.isDirectory()) {
						esit = renameFileUpperFirst(file);
						upperCaseSubFolder(file);
					} else if (file.isFile()) {
						esit = renameFileUpperFirst(file);
					}
					if (esit == false)
						total++;
				}
			} 
		}
		return total;
	}
	
	/**
	 * This method rename single file or directory, making the name starting with an uppercase letter
	 * @param rootDir
	 * @return success the boolean value representing the esit of the operation
	 */
	public static boolean renameFileUpperFirst(File rootDir) {
		String name="";
		String newName="";
		name=rootDir.getName();
		// ottengo la prima lettera e la rendo maiuscola
		String firstLetter = String.valueOf(name.charAt(0)).toUpperCase();
		//tolgo il primo carattere
		name = name.substring(1, name.length());
		newName=firstLetter+name;
		File fileWithNewName = new File(rootDir.getParent(), newName);
		boolean success = rootDir.renameTo(fileWithNewName);
	    if (!success) {
	    	logger.warn("Not renamed! "+newName);
	    }else {
			logger.debug("First letter: "+firstLetter+" - New name: "+newName);
	    }
	    return success;
	}
	
	/**
	 * this method change name of java File variable until the result is a non existent file
	 * It do that adding (i) numbers at the end of the file name, just before the start of the
	 * extension . THIS DOES NOT ACTUALLY CHANGE PHYSIC FILE'S NAME! (it changes only java object 
	 * until it represents a non-existing file in the same parent directory as the given one.
	 * 
	 * @param file
	 * @return the new variable representing non existing file, useful to call before create a new file
	 * 	to ensure that it is a not existing one.
	 */
	public static File renameJavaObjFile(File file) {
		String fileName=file.getPath();

		String extension = "";
		String name = "";

		int idxOfDot = fileName.lastIndexOf('.');   //Get the last index of . to separate extension
		if(idxOfDot>=0) {
			extension = fileName.substring(idxOfDot + 1);
			name = fileName.substring(0, idxOfDot);
		}else {
			name = fileName;
		}		

		Path path = Paths.get(fileName);
		int counter = 1;
		File f = null;
		while(Files.exists(path)){
			// if already with parentesis number
			if(name.substring(name.length()-1, name.length()).equals(")")) {
				idxOfDot = fileName.lastIndexOf('(');
				name = fileName.substring(0, idxOfDot);
			}
		    fileName = name+"("+counter+")."+extension;
		    path = Paths.get(fileName);
		    counter++;
		}
		f = new File(fileName);
		
		return f;
	}
	
	/**
	 * this method change name of java File variable until the result is a non existent file
	 * It do that adding (i) numbers at the end of the file name, just before the start of the
	 * extension . 
	 * 
	 * @param file
	 * @return the new unique name in String format
	 */
	public static String getUniqueNameFile(File file) {
		String fileName=file.getPath();

		String extension = "";
		String name = "";

		int idxOfDot = fileName.lastIndexOf('.');   //Get the last index of . to separate extension
		extension = fileName.substring(idxOfDot + 1);
		name = fileName.substring(0, idxOfDot);

		Path path = Paths.get(fileName);
		int counter = 1;
		while(Files.exists(path)){
			// if already with parentesis number
			if(name.substring(name.length()-1, name.length()).equals(")")) {
				idxOfDot = fileName.lastIndexOf('(');
				name = fileName.substring(0, idxOfDot);
			}
		    fileName = name+"("+counter+")."+extension;
		    path = Paths.get(fileName);
		    counter++;
		}
		return fileName;
	}
	
	/**
	 * this function rename a file until it has a unique name not existing yet, putting incrementally 
	 * an integer value starting to 0 (even if the file does not exist but have no index at the beginning )
	 * at the start of the fileName, placing a string separator between it and the original fileName
	 * 
	 * @param file the source file to check for unique name
	 * @param separator the string separator to put after file incremental number
	 * @return the file renamed - NB the original File parameter will not be renamed !
	 */
	public static File renameJavaObjFileAtStart(File file, String separator) {

		String fileName=file.getPath();
		
		String first = fileName.split(separator)[0];
		try {
			Integer.parseInt(first);
		}catch(NumberFormatException e) {
			first = "0";
			logger.error("Exception happened!", e);
		}
		
		int counter = 1;
		
		File f = null;
		
		while(file.exists()){
			
			counter++;
		}
		
		f = new File(counter+separator+fileName);
		
		return f;
	}
	
	/**
	 * this method is used to physically copy the content of a folder inside a distination folder
	 * (works also for a source file instead of a source folder). 
	 * @param source Directory or file
	 * @param destination Directory container / target file, to replace if exists
	 * @param extFilter is the extension to filter and mantain // ex: "png", "txt" 
	 * if it's "" then every extension will be kept
	 * @throws IOException 
	 */
	public static void copyDirectoryContents(File source, File destination, boolean overwrite, String extFilter) throws IOException{


		if(source.isDirectory()) {
			File[] srcFiles = source.listFiles();
			if (srcFiles!=null) {
				for (File file : srcFiles) {

					if (extFilter.equals("") || FilenameUtils.getExtension(file.getName()).equals(extFilter)) {
						String destinationPathString = destination.getPath() + "\\" + file.getName();
						File newFile = new File(destinationPathString);
						if (newFile.exists() && !overwrite) {
							// if file i exists and overwrite specified as false, then use the function to 
							// correct the name until it's not unique, and then update path string
							destinationPathString = destination.getPath() + "\\" + renameJavaObjFile(newFile).getName();
						}
						Path destinationPath = Paths.get(destinationPathString);
						Files.copy(file.toPath(), destinationPath, StandardCopyOption.REPLACE_EXISTING);
					}
				} 
			}
		}

	}
	
	/**
	 * this method append the content of a txt file (as a string) in the given text area, updating also the given label with description
	 * @param txtFile the source .txt file
	 * @param textArea_1 the text area to be filled with file content
	 * @param lblCurrentFile the label that will contain the info about operation
	 * @return
	 */
	public static boolean loadFileTxtArea(File txtFile, JTextArea textArea_1, JLabel lblCurrentFile) {

    	if(!FilenameUtils.getExtension(txtFile.getAbsolutePath()).equals("txt")) {
			JOptionPane.showMessageDialog(null, "Please select a valid txt file!");
    	}
    	else {
    		BufferedReader reader;
    		textArea_1.setText("");

    		try {
				reader = new BufferedReader(new FileReader(txtFile.getAbsolutePath()));

				String line = reader.readLine();
				while(line!=null) {
					textArea_1.append(line);
					textArea_1.append("\n");
					line = reader.readLine();
				}
				reader.close();
				lblCurrentFile.setText(txtFile.getParentFile().getName()+"\\"+txtFile.getName());
				
			} catch (Exception e1) {
				JOptionPane.showMessageDialog(null, "[Error loading the selected file]");
				logger.error("Exception happened!", e1);
				return false;
			}
    
    	}
    	return true;
	}
	
	/**
	 * This method read from a file and give a String representation of content
	 * @param toRead the source file to read
	 * @return
	 * @throws IOException
	 */
	public static String readFileAsString(File toRead) throws IOException {
		FileInputStream readerStream = new FileInputStream(toRead);
		BufferedReader reader = new BufferedReader(new InputStreamReader(readerStream, System.getProperty("file.encoding")));
		String read = "";
		String line = reader.readLine();
		while (line != null) {
			read += line;
			read += "\n";
			line = reader.readLine();
		}
		reader.close();
		return read;
	}
	
	/**
	 * This method writes a string to a file 
	 * @param source the source string to write on file
	 * @param outFile the ouput file to write in
	 * @param overwrite if true overwrite existing file
	 * @return
	 * @throws IOException
	 */
	public static boolean writeStringToFile(String source, File outFile, boolean overwrite) throws IOException {
		if(outFile.exists()) {
			if (overwrite) {
				boolean deleted = outFile.delete();
				boolean recreated = outFile.createNewFile();
				if(!recreated || !deleted) {
					return false;
				}
			}else {
				return false;
			}
		}else {
			boolean recreated = outFile.createNewFile();
			if(!recreated) {
				return false;
			}
		}
		
		OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(outFile), System.getProperty("file.encoding"));
		PrintWriter out = new PrintWriter(outputStreamWriter);
		out.write(source);
		
		out.close();
		outputStreamWriter.close();
		
		return true;
	}
	
	/**
	 * this method append the content of a txt file (as a string) in the given text area 
	 * @param txtFile the source .txt file
	 * @param textArea_1 the text area to be filled with file content
	 * @return
	 */
	public static boolean appendFileTxtArea(File txtFile, JTextArea textArea_1) {

    	if(!FilenameUtils.getExtension(txtFile.getAbsolutePath()).equals("txt")) {
			JOptionPane.showMessageDialog(null, "Please select a valid txt file!");
    	}
    	else {
    		BufferedReader reader;

    		try {
				reader = new BufferedReader(new InputStreamReader(new FileInputStream(txtFile), System.getProperty("file.encoding")));

				String line = reader.readLine();
				while(line!=null) {
					textArea_1.append(line);
					textArea_1.append("\n");
					line = reader.readLine();
				}
				reader.close();
				
			} catch (Exception e1) {
				JOptionPane.showMessageDialog(null, "[Error loading the selected file]");
				logger.error("Exception happened!", e1);
				return false;
			}
    		new Thread(new Runnable() {
				
				@Override
				public void run() {
					try {
						Thread.sleep(4000);
						//lblAdvice.setText("");
					} catch (InterruptedException e) {
						logger.error("Exception happened!", e);
					}
				}
			}).start();
    
    	}
    	return true;
	}
	
	/**
	 *  return a string containing the size of the file formatted correctly according to his size
	 * 
	 * @return cool formatted string representing the file size with correct measure unit
	 */
	public static String coolFileSize(File file){
		long size = file.length();
		if(size <= 0) return "0";
	    final String[] units = new String[] { "B", "kB", "MB", "GB", "TB" };
	    int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
	    return new DecimalFormat("#,##0.###").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
	}
	
	/**
	 *  return a string containing the size formatted correctly according to his size
	 * @param size is the size in bytes to represent in cool way
	 * @return cool formatted string representing the file size with correct measure unit
	 */
	public static String coolFileSize(Long size){
		if(size <= 0) return "0";
	    final String[] units = new String[] { "B", "kB", "MB", "GB", "TB" };
	    int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
	    return new DecimalFormat("#,##0.###").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
	}
	
	/**
	 *  return a string containing the size formatted correctly in bits according to his size
	 * @param size is the size in bytes to represent in cool way
	 * @return cool formatted string representing the file size in bits with correct measure unit
	 */
	public static String coolFileSizeBits(File file){
		long size = file.length();
		if(size <= 0) return "0";
	    final String[] units = new String[] { "b", "kb", "Mb", "Gb", "Tb" };
	    int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
	    return new DecimalFormat("#,##0.###").format(size*8/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
	}
	
	/**
	 *  return a string containing the size formatted correctly in bits according to his size
	 * @param size is the size in bytes to represent in cool way
	 * @return cool formatted string representing the file size in bits with correct measure unit
	 */
	public static String coolFileSizeBits(Long size){
		if(size <= 0) return "0";
	    final String[] units = new String[] { "b", "kb", "Mb", "Gb", "Tb" };
	    int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
	    return new DecimalFormat("#,##0.###").format(size*8/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
	}
	
	public static boolean copyFile(File source, File dest, boolean overwrite) {
		
		boolean esit = false;
		
		// if dir not existing or dest file not existing
		if(!dest.exists()) {
			dest.getParentFile().mkdirs();
			try {
				dest.createNewFile();
			} catch (IOException e) {
				logger.error("Exception happened!", e);
				esit = false;
			}
		}else {
			if(overwrite) {
				logger.debug("File existing: user allowed to overwrite.");
				dest.delete();
				try {
					dest.createNewFile();
				} catch (IOException e) {
					logger.error("Exception happened!", e);
					esit = false;
				}
			}else {
				// else give it unique name
				dest = renameJavaObjFile(dest);
			}
		}
		try {
		    FileUtils.copyFile(source, dest);
		    logger.debug("File written: "+dest.getAbsolutePath()+"\nSize: " + coolFileSize(dest));
		    esit = true;
		} catch (IOException e) {
			logger.error("Exception happened!", e);
		   esit = false;
		}
		
		return esit;
	}
	
	/**
	 * function used to update safely the progressbar in asynchronous way
	 * @param i
	 */
	private static void updateProgressBar(int i, JProgressBar progBar) {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				
				@Override
				public void run() {
					progBar.setValue(20);				
				}
			});
		} catch (InvocationTargetException | InterruptedException e1) {
			logger.error("Exception happened!", e1);
		}
	}
	

}

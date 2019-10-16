package amfileutils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import amfileutils.om.DeleteFilesByExtensionDTO;

public class FileStuff {
	
	public static final String END_ARGS_SECTION = "###_END_SECTION";

	// FileStuff logger creation		
	static Logger logger = Logger.getLogger(FileStuff.class); 
	
	public static boolean isFileSonOfDir(File file, File dir) {
		
		if(file == null || dir == null) return false;
		
		File tempUpperDir;
		try {
			tempUpperDir = file.getCanonicalFile().getParentFile();
			dir = dir.getCanonicalFile();
		} catch (IOException e) {
			tempUpperDir = file.getParentFile();
		}

		while(tempUpperDir != null) {
		    
			if(dir.equals(tempUpperDir)) {
		    	return true;
		    }

		    tempUpperDir = tempUpperDir.getParentFile();
		}

		return false;
	}
	
	public static File getNlevelsAncestor(File son, int levelsUp) {
		int originalLevels = levelsUp;
		try {
			File result = null;
			if (son != null) {
				result = son.getParentFile().getAbsoluteFile();
				if (levelsUp > 0) {
					if (levelsUp == 0) {
						levelsUp = 1;
					}

					while (levelsUp > 1) {
						if (result.getParentFile() == null || !result.getParentFile().getAbsoluteFile().exists()) {
							break;
						}
						result = result.getParentFile().getAbsoluteFile();
						levelsUp--;
					}
				} 
				
				logger.debug("\n\n=> Parent file search result [son:"+son.getCanonicalPath()
				+", levelsUp:"+originalLevels+"]: "+result.getCanonicalPath()+"\n");
				
			}else {
				logger.warn("\n\n=> Parent file search result FAILED! [son:"+son
				+", levelsUp:"+originalLevels+"]: "+result+"\n");
				return son;
			}
			
			return result;
		} catch (Exception e) {
			logger.error("Error finding parent file, returning given son", e);
			return son;
		}
	}
	
	public static String getNlevelsPathString(File son, int levelsUp) {
		int originalLevels = levelsUp;
		String result = "";
		try {
			if (son != null && son.exists()) {
				File temp = son.getCanonicalFile();
				if (levelsUp > 0) {
					if (levelsUp == 0) {
						levelsUp = 1;
					}

					while (levelsUp > 0) {
						result = temp.getName() + File.separator + result;
						if (temp.getParentFile() == null || !temp.getParentFile().exists()) {
							break;
						}
						temp = temp.getParentFile();
						levelsUp--;
					}
					if (result.length() > 0) {
						result = result.substring(0, result.length() - 1);
					}
				}
				logger.debug("\n\n=> Parent file toStringPath search result [son:" + son.getCanonicalPath() + ", levelsUp:" + originalLevels + "]: " + result + "\n");
			}
			return result;
		} catch (Exception e) {
			logger.error("Error finding parent file, returning given son", e);
			return result;
		}
	}
		
	public static File[] getFileArrayFromStrings(String[] filePaths){
		logger.debug("ARGUMENT FILES:" + Arrays.toString(filePaths));
		
		File[] argumentsFiles = new File[filePaths.length];
		int j = 0;
		for(String s : filePaths) {
			if (!END_ARGS_SECTION.equals(s)) {
				argumentsFiles[j] = new File(s);
			}else {
				break;
			}
			j++;
		}
		return argumentsFiles;
	}
	
	/**
	 * This method delete all the content from the file located at given String path, without deleting the file.
	 * @param filePath this is the path - relative or absolute - used to locate target file
	 * @return true if file is successfully emptied, false in the other cases
	 */
	public static boolean clearFileContent(String filePath) {
		PrintWriter writer;
		boolean esit = false;
		try {
			writer = new PrintWriter(filePath);
			writer.print("");
			esit = true;
			writer.close();
			
		} catch (FileNotFoundException e) {
			esit = false;
			logger.error("FILE NOT CLEARED!", e);
		}
		return esit;
	}
	
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
	
	public static String getNAncestorsPath(File file, int nLevels) {
		String result = "";
		if (file != null) {
			result = result.concat(file.getName());
			File clone = new File(file.getAbsolutePath());
			while ((nLevels -1) >= 0 && clone.exists()) {
				String father = clone.getParentFile().getName();
				int separatorPos = clone.getAbsolutePath().length() - clone.getName().length();
				String separator = clone.getAbsolutePath().substring(separatorPos-1, separatorPos);
				result = father.concat(separator).concat(result);
				clone = clone.getParentFile();
				nLevels--;
			} 
		}
		return result;
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
	 * This method copy the structure of the source dir, copying any found folder inside it (comprising source dir),
	 * and creates the same structure in the given destination dir. If inside dest dir there is a folder with same name
	 * as source dir, it creates a unique name.
	 * @param sourceDir
	 * @param destinationRootDir
	 * @return true if every folder is created, false in the other case (some folder may be missing)
	 * @throws IOException 
	 */
	public static boolean copyDirStructure(File sourceDir, File destinationRootDir, boolean recreateRoot) throws IOException {
		
		if(!sourceDir.exists() || checkIfContainedInDir(destinationRootDir, sourceDir)) {
			throw new IOException();
		}
		
		boolean esit = true;
		String destRootPath = destinationRootDir.getAbsolutePath();
		
		// only first round recreates the source dir - if all the time cycle never ends
		if(recreateRoot) {
			destRootPath = destRootPath+"/"+sourceDir.getName();
			destRootPath = FileStuff.getUniqueNameFile(new File(destRootPath));
			new File(destRootPath).mkdir();
		}
		
		File[] foundDirectories = sourceDir.listFiles(CustomFileFilters.customTypeFilter(CustomFileFilters.DIR_ONLY));
		for(File current : foundDirectories) {
			File destCurrent = new File(destRootPath + "/" + current.getName());
			
			// recursive repeat for current source folder inside root
			if(!copyDirStructure(current, destCurrent, false) && !destCurrent.exists()) {
				esit = false;
			};

			// copy current directory
			if(!destCurrent.mkdirs() && !destCurrent.exists()) {
				esit = false;
			}
			
		}
		
		return esit;
	}
	
	/**
	 * check if a file/folder is contained in a given suspect parent dir.
	 * @param fileToCheck
	 * @param suspectParentFolder
	 * @return true if fileToCheck is contained suspectParentFolder, false if not
	 */
	public static boolean checkIfContainedInDir(File fileToCheck, File suspectParentFolder) {
		boolean areRelated = true;
		try {
			areRelated = fileToCheck.getCanonicalPath().contains(suspectParentFolder.getCanonicalPath() + File.separator);
		} catch (IOException e) {
			logger.error(e);
		}
	    return areRelated;
	}
	
	/**
	 * List all files in directory and his subdirectories. 
	 * Use CustomFileFilters class and his parameters to select type of search.
	 * @param sourceDir
	 * @param filterType
	 * @return
	 */
	public static ArrayList<File> getAllContent(File sourceDir, String filterType) {
		ArrayList<File> result = new ArrayList<>();
		for(File found : sourceDir.listFiles(CustomFileFilters.customTypeFilter(filterType))) {
			if(found.isDirectory()) {
				result.addAll(getAllContent(found, filterType));
			}else {
				result.add(found);
			}
		}
		return result;
	}
	
	/**
	 * this method delete recursively the given directory's content, without deleting the folders in it.
	 * @param dirToEmpy
	 * @return true if all is deleted, false if something is still present
	 */
	public static boolean emptyDirContentFolderOnly(File dirToEmpy) {
		if(!dirToEmpy.exists()) {
			return false;
		}
		
		boolean esit = true;
		
		File[] foundContent = dirToEmpy.listFiles(CustomFileFilters.customTypeFilter(CustomFileFilters.ALL_FOUND));
		for(File current : foundContent) {
			
			// delete current if file
			if(current.isFile() && !FileUtils.deleteQuietly(current)) {
				esit = false;
			}
			
			// recursive delete content if folder
			if(current.isDirectory() && !emptyDirContentFolderOnly(current)) {
				esit = false;
			};
		}
		
		return esit;
	}
	
	/**
	 * this method delete recursively the given directory's content, deleting only files that match the given extensions.
	 * @param rootDir
	 * @param extListCommaSeparated list of extension to delete separated by a comma (spaces will be removed)
	 * @return true if all is deleted, false if something is still present
	 */
	public static DeleteFilesByExtensionDTO emptyDirContentByExt(File rootDir, String extListCommaSeparated) {
		DeleteFilesByExtensionDTO response = new DeleteFilesByExtensionDTO();

		// validate input before start
		if(rootDir== null || !rootDir.exists()) {
			response.setEsit(false);
			return response;
		}
		
		boolean esit = true;
		String[] extensionsList = extListCommaSeparated.split(",");
		for(String currExtension : extensionsList) {
			currExtension = currExtension.trim(); // remove whitespaces at start and at end
			currExtension = removeIllegalCharsFromExtension(currExtension);
			
			// delete content of current cleaned extension
			esit = true;
			File[] foundCurrExtContent = rootDir.listFiles(CustomFileFilters.customTypeFilter(CustomFileFilters.FILE_ONLY, currExtension, true));
			if(foundCurrExtContent.length != 0) {
				response.setNothingFound(false);
			}
			
			for(File current : foundCurrExtContent) {
				// delete current if file
				if(!FileUtils.deleteQuietly(current)) {
					esit = false;
				}
			}
			
			// if current extension has 0 results
			if(foundCurrExtContent.length == 0) {
				response.getNotFoundExtensionResult().add(currExtension);
			}
			
			// recursion for found folders inside current
			File[] foundCurrDirs = rootDir.listFiles(CustomFileFilters.customTypeFilter(CustomFileFilters.DIR_ONLY));
			for(File currentFoundDir : foundCurrDirs) {
				DeleteFilesByExtensionDTO subResponse = emptyDirContentByExt(currentFoundDir, currExtension);
				esit = (subResponse.isEsit() == false)? false : esit;
				response.setNothingFound(response.isNothingFound() && subResponse.isNothingFound());
				for (String subExt : subResponse.getNotFoundExtensionResult()) {
					if (!response.getNotFoundExtensionResult().contains(subExt)) {
						response.getNotFoundExtensionResult().add(subExt);
					} 
				}
			}
		}
		
		response.setEsit(esit);
		
		return response;
	}
	
	/**
	 * Get a string without unsupported charachters for files extensions. This actually means only a-z or 0-9 digits are admitted (ignoring case)
	 * extension can both contain part of the end of file's name, method will recognize to mantain dot or not based on last dot index
	 * @param extension the given extension to filter out removing illegal chars
	 * @return the cleaned string
	 */
	public static String removeIllegalCharsFromExtension(String extension) {
		String extensionIncipit = "";
		String dot = ".";
		String extensionAfterDot = "";
		int dotIndex = extension.lastIndexOf(".");
		if(dotIndex < 0) {
			dotIndex = 0;
			dot = "";
		}
		extensionIncipit = extension.substring(0, dotIndex);
		extensionAfterDot = extension.substring(dotIndex).replaceAll("[^a-zA-Z0-9]", "");
		
		return extensionIncipit.concat(dot).concat(extensionAfterDot);
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
	 * this method change name from the given file absolute path, until the result is a non existent file
	 * It do that adding (i) numbers at the end of the file name, just before the start of the
	 * extension . 
	 * <Strong>Note that the given File variable won't change</Strong>
	 * 
	 * @param file
	 * @return the new unique path name in String format
	 */
	public static String getUniqueNameFile(File file) {
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
	 * this method writes the content of a given extension file (as a string) in the given text area, updating also the given label with description
	 * @param txtFile the source .txt file
	 * @param textArea_1 the text area to be filled with file content
	 * @param lblCurrentFile the label that will contain the info about operation, this can be null
	 * @param extension filter can be null, or specified if you want to filter only a type of file admitted
	 * @return
	 */
	public static boolean loadFileTxtArea(File txtFile, JTextArea textArea_1, JLabel lblCurrentFile, String extensionFilter) {
		
		if(txtFile == null) {
			return false;
		}
		
    	if(extensionFilter != null && !FilenameUtils.getExtension(txtFile.getAbsolutePath()).equals(extensionFilter)) {
			JOptionPane.showMessageDialog(null, "Please select a valid "+ extensionFilter +" file!");
    	}
    	
    	else {
    		BufferedReader reader;
    		StringBuilder builder = new StringBuilder();
    		textArea_1.setText("");

    		try {
				reader = new BufferedReader(new FileReader(txtFile.getAbsolutePath()));

				String line = reader.readLine();
				line = (line != null)? line : "";
				builder.append(line);
				line = reader.readLine();
				
				while(line!=null) {
					builder.append("\n");
					builder.append(line);
					line = reader.readLine();
				}
				
				textArea_1.append(builder.toString());
				reader.close();
				if (lblCurrentFile != null) {
					lblCurrentFile.setText(txtFile.getParentFile().getName() + "\\" + txtFile.getName());
				}
				
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
		if(toRead == null || !toRead.exists()) {
			throw new IOException();
		}
		StringBuilder builder = new StringBuilder(100000);
		FileInputStream readerStream = new FileInputStream(toRead);
		BufferedReader reader = new BufferedReader(new InputStreamReader(readerStream, System.getProperty("file.encoding")));
		try {
			String line = reader.readLine();
			builder.append(line);
			line = reader.readLine();
			
			while (line != null) {
				builder.append("\n").append(line);
				line = reader.readLine();
			}
		} catch (IOException e) {
			throw e;
		}finally {
			reader.close();			
		}
		
		return builder.toString();
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
			boolean deleted = true;
			boolean recreated = false;
			if (overwrite) {
				deleted = outFile.delete();
				recreated = outFile.createNewFile();
			}else {
				outFile = renameJavaObjFile(outFile);
				recreated = outFile.createNewFile();
			}

			if(!recreated || !deleted) {
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
	public static boolean appendFileTxtArea(File txtFile, JTextArea textArea_1, boolean goToStart) {
		
		int originalPosition = textArea_1.getCaretPosition();
		
		if(txtFile == null) {
			return false;
		}else {
				StringBuilder builder = new StringBuilder(100000);
				try {
					FileInputStream readerStream = new FileInputStream(txtFile);
					BufferedReader reader;
					reader = new BufferedReader(new InputStreamReader(readerStream, System.getProperty("file.encoding")));
					try {
						String line = reader.readLine();
						line = (line != null)? line : "";
						builder.append(line);
						line = reader.readLine();
						
						while (line != null) {
							builder.append("\n").append(line);
							line = reader.readLine();
						}
						reader.close(); 
						readerStream.close();
					} catch (IOException e) {
						logger.error(e);
						try {reader.close(); readerStream.close();} 
						catch (IOException e1) {}			
					}
				} catch (UnsupportedEncodingException | FileNotFoundException e2) {
					logger.error(e2);
				}finally {
					textArea_1.append(builder.toString());
				}
				textArea_1.requestFocus();
				if (goToStart) {
					textArea_1.setCaretPosition(0);
				}else try{
					if(originalPosition > textArea_1.getText().length()) {
						textArea_1.setCaretPosition(textArea_1.getDocument().getLength()-1);						
					}else {
						textArea_1.setCaretPosition(originalPosition);
					}
				}catch (Exception e) {
					// avoided outOfBounds
				}
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
	    final String[] units = new String[] { "B", "kB", "MB", "GB", "TB", "ExaB" };
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
	    final String[] units = new String[] { "B", "kB", "MB", "GB", "TB", "ExaB" };
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
	    final String[] units = new String[] { "b", "kb", "Mb", "Gb", "Tb", "Exab" };
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
	    final String[] units = new String[] { "b", "kb", "Mb", "Gb", "Tb", "Exab" };
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
	
	public static boolean copyFileIntoDir(File source, File destdir, boolean overwrite) {
		
		boolean esit = false;
		
		// if dir not existing or dest file not existing
		if(!destdir.exists()) {
			destdir.getParentFile().mkdirs();
			try {
				destdir.createNewFile();
			} catch (IOException e) {
				logger.error("Exception happened!", e);
				esit = false;
			}
		}else {
			if(overwrite) {
				logger.debug("File existing: user allowed to overwrite.");
				destdir.delete();
				try {
					destdir.createNewFile();
				} catch (IOException e) {
					logger.error("Exception happened!", e);
					esit = false;
				}
			}else {
				// else give it unique name
				destdir = renameJavaObjFile(destdir);
			}
		}
		try {
		    FileUtils.copyFileToDirectory(source, destdir, true);
		    logger.debug("File written: "+destdir.getAbsolutePath()+"\nSize: " + coolFileSize(destdir));
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
	@Deprecated
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

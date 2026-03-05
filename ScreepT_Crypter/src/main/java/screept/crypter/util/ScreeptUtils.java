/**
 *
 * =========================================================================================
 *  Copyright (C) 2019-2020
 *
 *  AM-Design-Development - (Alessio Moraschini) - All Rights Reserved
 * =========================================================================================
 *
 * You should have received a copy of the license with this file.
 * If not, please write to: info@am-design-development.com, or visit : https://www.am-design-development.com
 */
package screept.crypter.util;

import java.awt.Container;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import javax.swing.JPasswordField;

import screept.crypter.impl.aes.AesCipher;
import various.common.light.gui.dialogs.msg.JOptionHelper;
import various.common.light.om.SecurityLevel;
import various.common.light.utility.log.SafeLogger;

public class ScreeptUtils {
	
	public static JOptionHelper dialogHelper = new JOptionHelper(null);
	
	public static boolean checkPasswordStrengthDynamic(SecurityLevel level, JPasswordField passwordField, int minLengthIncluded) {
		if(level.equals(SecurityLevel.MAX)) {
			return checkPasswordStrengthHighSecurity(passwordField, minLengthIncluded);
			
		} else if(level.equals(SecurityLevel.MID)) {
			return checkPasswordStrengthMidSecurity(passwordField, minLengthIncluded);
			
		} else {
			return true;
		}
	}
	
	public static boolean checkPasswordStrengthMidSecurity(JPasswordField passwordField, int minLengthIncluded) {
		boolean useAnyway = false; 
		String advice = AesCipher.isPasswordSafeNoSpecials(passwordField.getPassword(), minLengthIncluded);
				
		Container parent = passwordField != null ? passwordField.getTopLevelAncestor() : null;
		
		if("".equals(advice)) {
			useAnyway = true;
		} else {
			useAnyway = dialogHelper.yesOrNo(advice, "Unsafe Password");
		}
		
		return useAnyway;
	}
	
	public static boolean checkPasswordStrengthHighSecurity(JPasswordField passwordField, int minLengthIncluded) {
		boolean problemFound = true; 
		String advice = AesCipher.isPasswordSafeBestSecurity(passwordField.getPassword(), minLengthIncluded);
		
		Container parent = passwordField != null ? passwordField.getTopLevelAncestor() : null;
		
		if("".equals(advice)) {
			problemFound = false;
		} else {
			problemFound = dialogHelper.yesOrNo(advice, "Unsafe Password");
		}
		
		return problemFound;
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
	 * This method is meant to delete all folder content, deleting recursively also the folders inside it.
	 * the given folder won't be deleted, only it's content will be.
	 * 
	 * @param folder the source folder to be emptied
	 * @return deleted: false if at least one deletion have incurred in some errors
	 */
	public static boolean deleteDirContentRecursive(File folder, SafeLogger logger) {
		boolean deleted = true;
		long startTime = System.currentTimeMillis();
		logger.info("deleteDirContentRecursive -> Start...");
		
		File[] files = folder.listFiles();
	    if(files!=null) { //some JVMs return null for empty dirs
	        for(File f: files) {
	            if(f.isDirectory()) {
	                boolean del = deleteDirContentInclRoot(f, logger);
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
	public static boolean deleteDirContentInclRoot(File folder, SafeLogger logger) {
		boolean deleted = true;
		long startTime = System.currentTimeMillis();
		logger.info("deleteDirContentInclRoot -> Start...");
		
		File[] files = folder.listFiles();
	    if(files!=null) { //some JVMs return null for empty dirs
	        for(File f: files) {
	            if(f.isDirectory()) {
	                boolean del = deleteDirContentInclRoot(f, logger);
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
	
	/**
	 * check if a file/folder is contained in a given suspect parent dir.
	 * @param fileToCheck
	 * @param suspectParentFolder
	 * @return true if fileToCheck is contained suspectParentFolder, false if not
	 */
	public static boolean checkIfContainedInDir(File fileToCheck, File suspectParentFolder, SafeLogger logger) {
		boolean areRelated = true;
		try {
			areRelated = fileToCheck.getCanonicalPath().contains(suspectParentFolder.getCanonicalPath() + File.separator);
		} catch (IOException e) {
			logger.error("Cannot check if file is contained in directory", e);
		}
	    return areRelated;
	}
	
	/**
	 * get number of files in a given folder and inside its sub-directories recursively, counting folders if countDirectories is true
	 * @param directory the root directory to search in
	 * @return
	 */
	public static int countFilesInDirectory(File directory, boolean countDirectories) {
		int count = 0;
		
		try {
			for (File file : directory.listFiles()) {
				if (file.isFile()) {
					count++;
				}
				if (file.isDirectory()) {
					if (countDirectories) {
						count++;
					}
					count += countFilesInDirectory(file, countDirectories);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	  
	  return count;
	}

	/**
	 * get number of files in a given folder and inside its sub-directories recursively, counting folders if countDirectories is true
	 * @param directory the root directory to search in
	 * @param extensionsToCount these extension will be counted when a match is found
	 * @return
	 */
	public static int countFilesInDirectory(File directory, boolean countDirectories, ArrayList<String> extensionsToCount) {
		int count = 0;
		for (File file : directory.listFiles()) {
			if (file.isFile()){
				String name = file.getName().toLowerCase().trim();
				for(String acceptedExtension : extensionsToCount) {
					if(name.endsWith(acceptedExtension.toLowerCase())) {
						count++;
					}
				}
			}
			if (file.isDirectory()) {
				if(countDirectories) {
					count++;
				}
				count += countFilesInDirectory(file, countDirectories);
			}
		}
		return count;
	}
}

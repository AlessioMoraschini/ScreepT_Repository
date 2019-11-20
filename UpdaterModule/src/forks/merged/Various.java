package forks.merged;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.Arrays;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;

import static java.lang.System.out;

public class Various {
	
	public static Dimension getScreenSize() {
		return Toolkit.getDefaultToolkit().getScreenSize();
	}

	public static Dimension getScreenSize(Component frame) {
		DisplayMode mode = frame.getGraphicsConfiguration().getDevice().getDisplayMode();
		return new Dimension(mode.getWidth(), mode.getHeight());
	}
	
	public static Dimension getScreenSizePerc(int factorPerc, JFrame frame) {
		Dimension screen = getScreenSize(frame);
		int w = (screen.width*factorPerc)/100;
		int h = (screen.height*factorPerc)/100;
		return new Dimension(w, h);
	}
	
	public static Dimension getScreenSize(int marginLeftRight) {
		Dimension screen = getScreenSize();
		return new Dimension(screen.width-(marginLeftRight+marginLeftRight), screen.height);
	}
	
	public static void centerComponent(Component targetComponent, int percentSize) {
		float percent = (float)percentSize/100;
		Dimension screenSize = (targetComponent instanceof JFrame)? getScreenSize((JFrame)targetComponent): getScreenSize();
		int W = (int)(screenSize.width * percent);
		int H = (int)(screenSize.height * percent);
		targetComponent.setSize(W, H);
		targetComponent.setBounds(screenSize.width/2-W/2, screenSize.height/2-H/2, W, H);
	}
	
	public static void centerComponent(Component targetFrame, int W, int H) {
		Dimension screenSize = (targetFrame instanceof JFrame)? getScreenSize((JFrame)targetFrame): getScreenSize();
		targetFrame.setSize(W, H);
		targetFrame.setBounds(screenSize.width/2-W/2, screenSize.height/2-H/2, W, H);
	}
	
	public static void centerComponent(Component targetFrame) {
		if(targetFrame == null) {
			return;
		}
		Dimension screenSize = (targetFrame instanceof JFrame)? getScreenSize((JFrame)targetFrame): getScreenSize();
		Dimension frameSize = null;
		frameSize = targetFrame.getSize();
		int W = frameSize.width;
		int H = frameSize.height;
		
		targetFrame.setBounds(screenSize.width/2-W/2, screenSize.height/2-H/2, W, H);
	}
	
	public static void moveFrame(Component element, int xDiff, int yDiff) {
		int totX = (element.getX() + xDiff);
		int totY = (element.getY() + yDiff);
		int newX = (totX < 0)? 0 : totX;
		int newY = (totY < 0)? 0 : totY;
		Dimension screenSize = (element instanceof JFrame)? getScreenSize((JFrame)element): getScreenSize();
		int maxDeltaX = screenSize.width - newX - element.getWidth();
		int maxDeltaY = screenSize.height - newY - element.getHeight();
		
		newX = (maxDeltaX < 0)? screenSize.width - element.getWidth() : newX;
		newY = (maxDeltaY < 0)? screenSize.height - element.getHeight() : newY;
		
		element.setBounds(newX, newY, element.getWidth(), element.getHeight());
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
	    return new DecimalFormat("#,##0.##").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
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
	    return new DecimalFormat("#,##0.##").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
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
	    return new DecimalFormat("#,##0.##").format(size*8/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
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
	    return new DecimalFormat("#,##0.##").format(size*8/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
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
	 * same as deleteDirContentRecursive but this one deletes also the root folder
	 * @param folder
	 * @return
	 */
	public static boolean deleteDirContentInclRoot(File folder) {
		boolean deleted = true;
		long startTime = System.currentTimeMillis();
		out.println("deleteDirContentInclRoot -> Start...");
		
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

	    out.println("deleteDirContentInclRoot -> Finished in " + String.valueOf(System.currentTimeMillis()-startTime) + " ms");
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
		long startTime = System.currentTimeMillis();
		out.println("deleteDirContentRecursive -> Start...");
		
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

	    out.println("deleteDirContentRecursive -> Finished in " + String.valueOf(System.currentTimeMillis()-startTime) + " ms");
	    return deleted;	
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
	public static void copyDirectoryContents(File source, File destination) throws IOException{

		long startTime = System.currentTimeMillis();
		out.println("copyDirectoryContents -> Start...");

		if(source.isDirectory()) {
			for (File curr : source.listFiles()) {
				out.println("Copying "+curr+" \n  => into "+destination);
				try {
					FileUtils.copyToDirectory(curr, destination);
				} catch (Exception e) {
					out.println(e.getClass() + Arrays.deepToString(e.getStackTrace()));
				}
			}
		}

		out.println("copyDirectoryContents -> Finished in " + String.valueOf(System.currentTimeMillis()-startTime) + " ms");
	}
	
	/**JFRAME - only directories src
	 * this method select a directory from the user (only directories),if nothing is selected returns null
	 */
	public static File directoRead(JFrame parent, String title, File initialDir) {
		
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle(title);
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fileChooser.setCurrentDirectory(initialDir);		
		fileChooser.setPreferredSize(new Dimension(800, 450));
		File correctFile = null;
		
		int returnVal = fileChooser.showOpenDialog(parent);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			
			File selectedFile = fileChooser.getSelectedFile();
			if(selectedFile==null||!selectedFile.isDirectory()) {
				JOptionPane.showMessageDialog(parent, "Please select a directory!", "Select a directory, please", JOptionPane.ERROR_MESSAGE);
			}else {
				correctFile = selectedFile;
			}
		}
		return correctFile;
	}
	
	// PRINT UTILS METHODS
		/**
		 * Retrieve a String separator n-times charachter
		 */
		public static String getSeparator(String character, int n){
			int curr = 0;
			String output = "\n  ";
			while (curr < n) {
				output = output.concat(String.valueOf(character));
				curr++;
			}
			
			return output;
		}
		
		/**
		 * Create a string of n spaces
		 * @param n
		 */
		public static String getNspacesString(int n) {
			String result = "";
			for(int i = 0; i<n; i++) {
				result = result.concat(" ");
			}
			
			return result;
		}
		
		/**
		 * Retrieve a header String specifying charachter separator (may be null), header width in charachters, and title
		 */
		public static String getHeaderString(String character, int n, String title){
			String result = "";
			
			int newN = (n >= title.length()+4)? n : title.length()+4;
			newN = newN*character.length();
			String leftSpaces = getNspacesString((newN - title.length() -2)/2);
			boolean hasToPad = (leftSpaces.length()*2 + 2 + title.length()) < newN;
			String rightPadding = (hasToPad)? " " : "";
			String rightSpaces = leftSpaces + rightPadding;
			
			result += getSeparator(character, newN);
			result += "\n  " + character + leftSpaces + title + rightSpaces + character ;
			result += getSeparator(character, newN);
			result += "\n";
			
			return result;
		}
}

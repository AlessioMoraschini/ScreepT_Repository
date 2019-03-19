package launcherutils;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import launcherutils.om.JVMInfo;
import launcherutils.om.OSinfo;

public class SysUtils {
	
	// KeyGenPanel logger
	static Logger logger = Logger.getLogger(SysUtils.class); 
	
	// STATIC METHODS
	
	public static OSinfo retrieveOS() {
		return new OSinfo();
	}
	
	public static JVMInfo getJVMinfo() {
		return new JVMInfo();
	}
	
	// OS independent commands
	
	public static String startJarCommand(String jvmArgs, String jarPath) {
		String command = "java " + jvmArgs + " -jar " + jarPath;
		return command;
	}
	
	/**
	 * According to given OS, search for installed JVM paths
	 * @param OS
	 * @return the list of JVMs home directory (EX. "jdk1.8.0_201")
	 */
	public static ArrayList<String> findJvmVersionsInstalled(OSinfo OS){
		ArrayList<String> foundJVMS = new ArrayList<String>();
		
		if(OS.isLinux()) {
			
			// END LINUX
		}else if(OS.isMac()) {
			
			// END MAC
		}else if(OS.isWindows()) {
			
			ArrayList<String> foundJavaRootFolders = new ArrayList<String>();
			foundJavaRootFolders = findWindowsProgramPath("java");
			for(String currFolder : foundJavaRootFolders) {
				ArrayList<String> foundVersions = getAllJavaWinVersionFromRoot(currFolder);
				for (String currVersionPath : foundVersions) {
					foundJVMS.add(currVersionPath);
				}
			}
		} // END WINDOWS
		
		return foundJVMS;
	}
	
	public static ArrayList<String> findWindowsProgramPath(String programName) {
		ArrayList<String> foundList = new ArrayList<String>();
		
		String path32 = "C:/Program Files (x86)/";
		String path64 = "C:/Program Files/";
		
		File sw32 = new File(path32, programName);
		File sw64 = new File(path64, programName);
		
		if(sw64.exists()) {
			foundList.add(sw64.getAbsolutePath());
		}
		if(sw32.exists()) {
			foundList.add(sw32.getAbsolutePath());
		}
		
		return foundList;
	}
	
	public static ArrayList<String> getAllJavaWinVersionFromRoot(String root) {
		ArrayList<String> stringRetrievedPaths = new ArrayList<String>();
		
		File currentRootDir = new File(root);
		currentRootDir.list(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				
				if(!(name.toLowerCase().indexOf("jdk")<0) || !(name.toLowerCase().indexOf("jre")<0)) {
					if (dir.isDirectory()) {
						String currentPath = dir.getPath()+"/"+name;
						// if name contains (case insensitive) jdk or jre then it's a java install directory
						if (new File(currentPath+"/bin").exists()) {
							stringRetrievedPaths.add(currentPath);
							return true;
						}
					}
				}
				return false;
			}
		}); // END FILE FILTER
		
		return stringRetrievedPaths;
	}
	
	public static String getJavaVersionFromBin(String javaBinDirPath) throws IOException {
	    File parentOfBin = new File(javaBinDirPath).getParentFile();
		File releaseFile = new File(parentOfBin.getAbsolutePath(), "release");
	    BufferedReader bufferedReader = new BufferedReader(new FileReader(releaseFile));
	    String version = bufferedReader.readLine();
	    bufferedReader.close();
	    return version.substring(version.indexOf("\"") + 1, version.lastIndexOf("\""));
	}
	
	/**
	 * Use this method to remove every charachter from given string, except "." and digits 0-9
	 * Note that eventual undescores will become dots.
	 * @param source
	 * @return
	 */
	public static String keepOnlyDotsAndDigits(String source) {
		String dotsOnly = source.replaceAll("_",".");
		dotsOnly = dotsOnly.replaceAll("-","");
		// remove non dots chars
		return dotsOnly.replaceAll("[a-zA-Z!#$%&'*+=?^`{|}+]|[_]\\-","");
	}
	
	/**
	 * Dotted version format compare - if version not matches format <Strong>IllegalArgumentException</Strong> is thrown
	 * 
	 * @param version_A
	 * @param version_B
	 * @return -1 if A less than B, 1 if A more than B, 0 if equals
	 */
	public static int compareVersions(String version_A, String version_B) throws IllegalArgumentException{
		
		if(version_A == null || version_B == null) {
			throw new IllegalArgumentException("Version can not be null");
		}
		if(!version_A.matches("[0-9]+(\\.[0-9]+)*") || !version_B.matches("[0-9]+(\\.[0-9]+)*")) {
			throw new IllegalArgumentException("Invalid version format");
		}
		
        String[] AParts = version_A.split("\\.");
        String[] BParts = version_B.split("\\.");
        int length = Math.max(AParts.length, BParts.length);
        for(int i = 0; i < length; i++) {
            int ACurrPart = i < AParts.length ? Integer.parseInt(AParts[i]) : 0;
            int BCurrPart = i < BParts.length ? Integer.parseInt(BParts[i]) : 0;
            if(ACurrPart < BCurrPart) {
            	return -1;
            }
            if(ACurrPart > BCurrPart) {
            	return 1;
            }
        }
        return 0;
	}
	
	/**
	 * @return the max memory usable by Java software in MB
	 */
	public static int getMaxMemoryAdmitted() {
		
		if(is64arch()) {
			return 4092;
		}else{
			return 2048;
		}
	}
	
	/**
	 * return true if current architecture is 64 bits, false is 32 bits.
	 * @return
	 */
	public static boolean is64arch() {
		String arch = System.getenv("PROCESSOR_ARCHITECTURE");
		if(arch.endsWith("64")) {
			return true;
		}else return false;
	}
	
	public static int findIf32or64() {
		if(is64arch()) {
			return 64;
		}else {
			return 32;
		}
	}
	
	public static boolean openUrlInBrowser(String URL) {
		
		if(Desktop.isDesktopSupported()){
			Desktop desktop = Desktop.getDesktop();
			try {
				desktop.browse(new URI(URL));
				return true;
			} catch (IOException | URISyntaxException e) {
				logger.error("Error opening multiplatform browser.\n\n", e);
			}
		}else {
			OSinfo OS = retrieveOS();
			Runtime runtime = Runtime.getRuntime();
			
			if(OS.isLinux()){
				try {
					runtime.exec("xdg-open " + URL);
					return true;
				} catch (IOException e) {
					logger.error("Error opening URL with Linux Native command\n\n", e);
				}
			}else if(OS.isMac()) {
				try {
					runtime.exec("open " + URL);
					return true;
				} catch (IOException e) {
					logger.error("Error opening URL with Mac-OS Native command\n\n", e);
				}
			}else if(OS.isWindows()) {
				try {
					runtime.exec("rundll32 url.dll,FileProtocolHandler " + URL);
					return true;
				} catch (IOException e) {
					logger.error("Error opening URL with Windows Native command\n\n", e);
				}
			}else {
				return false;
			}
		}
		return false;
	}
}

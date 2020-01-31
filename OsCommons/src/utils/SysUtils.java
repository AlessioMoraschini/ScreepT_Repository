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
package utils;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import files.FileSearchAdvanced;
import om.JVMInfo;
import om.OSinfo;
import utility.SafeLogger;
import utility.string.StringWorker;


public class SysUtils {
	
	// SysUtils logger
	static SafeLogger logger = new SafeLogger(SysUtils.class); 
	
	// STATIC METHODS
	public static final OSinfo OS_INFO_LAZY = new OSinfo();
	
	public static final String winPath32 = "C:/Program Files (x86)/";
	public static final String winPath64 = "C:/Program Files/";

	public static final String winJavaExecutable = "java.exe";
	
	public static OSinfo retrieveOS() {
		return new OSinfo();
	}
	
	public static JVMInfo getJVMinfo() {
		return new JVMInfo();
	}
	

///////////////////////////////// GENERIC STUFF ////////////////////////////////////////////////
	
	/**
  	 * @param main class must be a full package and class name (Ex => [Java.Lang.String])
	 * 
	 * other params must not be null
	 */
	private static String startJarCommand(String jvmArgs, String jarPath, boolean viewShell) {
		String javaLaunchKeyWord = viewShell ? "java " : "java ";
		return javaLaunchKeyWord + jvmArgs + " -jar " + jarPath;
	}
	
	/**
	 * SIGNATURE2 : START .JAR with ARGS FILES
	 * @param main class must be a full package and class name (Ex => [Java.Lang.String])
	 * 
	 * filteredArgsFiles can be null
	 * 
	 * params must not be null
	 */
	public static String startJarCommand(String jvmArgs, String jarPath, File[] filteredArgsFiles, boolean viewShell) {
		
		String javaLaunchKeyWord = viewShell ? "java " : "java ";
		if(filteredArgsFiles == null || filteredArgsFiles.length == 0) {
			return startJarCommand(jvmArgs, jarPath, viewShell);
		}
		
		String filesParams = "";
		StringBuilder builder = new StringBuilder();
		
		for(File file : filteredArgsFiles) {
			builder.append(" \"").append(file.getAbsolutePath()).append("\"");
		}
		filesParams = builder.toString();
		
		builder = new StringBuilder();
		builder.append(javaLaunchKeyWord).append(jvmArgs).append(" -jar ").append(jarPath).append(filesParams);
		String command = builder.toString();
		return command;
	}
	
	
	/**
  	 * @param main class must be a full package and class name (Ex => [Java.Lang.String])
	 * 
	 * params must not be null
	 */
	private static String startClassCommand(String jvmArgs, String classPath, String mainClass, boolean viewShell) {
		String javaLaunchKeyWord = viewShell ? "java " : "java ";
		return javaLaunchKeyWord + jvmArgs + " -cp " + classPath + " " + mainClass;
	}

	/**
	 * SIGNATURE2 : START .CLASS with ARGS FILES
	 * @param main class must be a full package and class name (Ex => [Java.Lang.String])
	 * 
	 * filteredArgsFiles can be null
	 * 
	 * other params must not be null
	 */
	public static String startClassCommand(String jvmArgs, String classPath, String mainClass, File[] filteredArgsFiles, boolean viewShell) {
		
		String javaLaunchKeyWord = viewShell ? "java " : "java ";
		if(filteredArgsFiles == null || filteredArgsFiles.length == 0) {
			return startClassCommand(jvmArgs, classPath, mainClass, viewShell);
		}
		
		String filesParams = "";
		StringBuilder builder = new StringBuilder();
		
		for(File file : filteredArgsFiles) {
			builder.append(" \"").append(file.getAbsolutePath()).append("\"");
		}
		filesParams = builder.toString();
		
		builder = new StringBuilder();
		builder.append(javaLaunchKeyWord).append(jvmArgs)
			   .append(" -cp ").append(classPath)
			   .append(" ").append(mainClass)
			   .append(filesParams);
		String command = builder.toString();
		return command;
	}
	
	public static File getCurrentJavaHome() {
		return new File(System.getProperty("java.home"));
	}
	
	/**
	 * According to given OS, search for installed JVM paths
	 * @param OS
	 * @return the list of JVMs home directory (EX. "jdk1.8.0_201")
	 */
	public static ArrayList<String> findJvmVersionsInstalled(OSinfo OS){
		ArrayList<String> foundJVMS = new ArrayList<String>();
		
		if(OS.isLinux()) {
			
			try {
				foundJVMS.addAll(LinuxFindJvmVersions.CommandLineJavaAlternativesPaths());
			} catch (IOException e) {
				logger.error("Cannot retrieve linux java versions", e);
			}
		}else if(OS.isMac()) {
			
			// TODO MacOS
		
		}else if(OS.isWindows()) {
			
			// Add oracle's java list
			ArrayList<String> foundJavaRootFolders = findWindowsProgramPath("java");
			for(String currFolder : foundJavaRootFolders) {
				ArrayList<String> foundVersions = getAllJavaWinVersionFromRoot(currFolder);
				for (String currVersionPath : foundVersions) {
					foundJVMS.add(currVersionPath);
				}
			}

			// Add open JDK java list
			List<String> foundOpenJdkRootFolders = findWindowsProgramPathByNamePart("openjdk");
			for(String currFolder : foundOpenJdkRootFolders) {
				ArrayList<String> foundVersions = getAllDirsWithJavaExeInside(currFolder, winJavaExecutable);
				for (String currVersionPath : foundVersions) {
					foundJVMS.add(currVersionPath);
				}
			}
		} // END WINDOWS
		
		// MULTI-PLATFORM : Add current if not yet present into list
		String canonicalCurrentPath;
		canonicalCurrentPath = getCurrentJavaHome().getAbsolutePath();
		logger.info("Current jvm is : " + canonicalCurrentPath);
		if(!foundJVMS.contains(canonicalCurrentPath) && !foundJVMS.contains(new File(canonicalCurrentPath).getParentFile().getAbsolutePath())) {
			foundJVMS.add(canonicalCurrentPath);
			logger.info(canonicalCurrentPath + " not found in previously retrieved list -> added to JVM list");
		}
		
		return foundJVMS;
	}
	
	public static String getCurrentJVMversion() {
		return System.getProperty("java.version");
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
	 * Use this method to remove every character from given string, except "." and
	 * digits 0-9 Note that eventual undescores will become dots.
	 * 
	 * @param source
	 * @return
	 */
	public static String keepOnlyDotsAndDigits(String source) {
		String dotsOnly = source.replaceAll("_", ".");
		dotsOnly = dotsOnly.replaceAll("-", "");
		// remove non dots chars
		return dotsOnly.replaceAll("[a-zA-Z!#$%&'*+=?^`{|}+]|[_]\\-", "");
	}

	/**
	 * Dotted version format compare - if version not matches format
	 * <Strong>IllegalArgumentException</Strong> is thrown
	 * 
	 * @param version_A
	 * @param version_B
	 * @return -1 if A less than B, 1 if A more than B, 0 if equals
	 */
	public static int compareVersions(String version_A, String version_B) throws IllegalArgumentException {

		if (version_A == null || version_B == null) {
			throw new IllegalArgumentException("Version can not be null");
		}
		if (!version_A.matches("[0-9]+(\\.[0-9]+)*") || !version_B.matches("[0-9]+(\\.[0-9]+)*")) {
			throw new IllegalArgumentException("Invalid version format");
		}

		String[] AParts = version_A.split("\\.");
		String[] BParts = version_B.split("\\.");
		int length = Math.max(AParts.length, BParts.length);
		for (int i = 0; i < length; i++) {
			int ACurrPart = i < AParts.length ? Integer.parseInt(AParts[i]) : 0;
			int BCurrPart = i < BParts.length ? Integer.parseInt(BParts[i]) : 0;
			if (ACurrPart < BCurrPart) {
				return -1;
			}
			if (ACurrPart > BCurrPart) {
				return 1;
			}
		}
		return 0;
	}
	
	public static String getCurrentSystemEncoding() {
		return System.getProperty("file.encoding");
	}

	/**
	 * @return the max memory usable by Java software in MB
	 */
	public static int getMaxMemoryAdmitted() {

		if (is64arch()) {
			return 4092;
		} else {
			return 2048;
		}
	}
	
	public static boolean isCurrentOpenJdk() {
		return System.getProperty("java.home").toLowerCase().contains("openjdk");
	}

	/**
	 * return true if current architecture is 64 bits, false is 32 bits.
	 * 
	 * @return
	 */
	public static boolean is64arch() {
		String arch = System.getProperty("os.arch");
		if (arch != null && arch.contains("64")) {
			return true;
		} else
			return false;
	}
	
	/**
	 * return true if current architecture is 32 bits, false is 64 bits.
	 * 
	 * @return
	 */
	public static boolean is32arch() {
		String arch = System.getProperty("os.arch");
		if (arch != null && arch.contains("86")) {
			return true;
		} else
			return false;
	}

	public static int findIf32or64() {
		if (is64arch()) {
			return 64;
		} else {
			return 32;
		}
	}
	
	public static String getShellOsBased() {
		if(OS_INFO_LAZY.isWindows()) {
			return "cmd";
		} else {
			return "bash";
		}
	}
	
	public static boolean openUrlWithSpecificBrowser(String URL, Browser targetBrowser) throws IOException, InterruptedException {
		Runtime runtime = Runtime.getRuntime();
		String shell = getShellOsBased();
		String argument1 = OS_INFO_LAZY.isWindows()? " /c " : OS_INFO_LAZY.isMac()? " -c " : " -c ";
		
		Process process = runtime.exec(shell + argument1 + targetBrowser.getOsLaunchSwName() + "\"" + URL + "\"" );
		return process != null && process.waitFor() == 0;
	}
	
	public static void openUrlInBrowserOrdered(String URL) {
		boolean opened = false;
		for(Browser browser : Browser.defaultBrowserOrder) {
			try {
				opened = openUrlWithSpecificBrowser(URL, browser);
			} catch (Exception e) {
				logger.warn("Cannot open " + URL + " with browser: " + browser, e);
			}
			
			if(opened) {
				logger.info(URL + " opened with " + browser);
				return;
			}
		}
		
		openUrlInBrowser(URL);
		logger.info(URL + " opened with system default bind application.");
	}

	public static boolean openUrlInBrowser(String URL) {

		if (Desktop.isDesktopSupported()) {
			Desktop desktop = Desktop.getDesktop();
			try {
				desktop.browse(new URI(URL));
				return true;
			} catch (IOException | URISyntaxException e) {
				logger.error("Error opening multiplatform browser.\n\n", e);
			}
		} else {
			OSinfo OS = retrieveOS();
			Runtime runtime = Runtime.getRuntime();

			if (OS.isLinux()) {
				try {
					runtime.exec("xdg-open " + URL);
					return true;
				} catch (IOException e) {
					logger.error("Error opening URL with Linux Native command\n\n", e);
				}
			} else if (OS.isMac()) {
				try {
					runtime.exec("open " + URL);
					return true;
				} catch (IOException e) {
					logger.error("Error opening URL with Mac-OS Native command\n\n", e);
				}
			} else if (OS.isWindows()) {
				try {
					runtime.exec("rundll32 url.dll,FileProtocolHandler " + URL);
					return true;
				} catch (IOException e) {
					logger.error("Error opening URL with Windows Native command\n\n", e);
				}
			} else {
				return false;
			}
		}
		return false;
	}

	public static boolean openUrlInBrowserNative(String URL) {
		OSinfo OS = retrieveOS();
		Runtime runtime = Runtime.getRuntime();

		if (OS.isLinux()) {
			try {
				runtime.exec("xdg-open " + URL);
				return true;
			} catch (IOException e) {
				logger.error("Error opening URL with Linux Native command\n\n", e);
			}
		} else if (OS.isMac()) {
			try {
				runtime.exec("open " + URL);
				return true;
			} catch (IOException e) {
				logger.error("Error opening URL with Mac-OS Native command\n\n", e);
			}
		} else if (OS.isWindows()) {
			try {
				runtime.exec("rundll32 url.dll,FileProtocolHandler " + URL);
				return true;
			} catch (IOException e) {
				logger.error("Error opening URL with Windows Native command\n\n", e);
			}
		} 
		
		return false;
	}
	
	/**
	 * Launch a system runnable file as .sh or .bat, reading it's output and waiting for a specific string in output
	 * @Param wait timeout is in milliseconds, after it's elapsed String will be returned even if signal was not retrieved from process output
	 * @return String output of process
	 * @throws IOException 
	 */
	public static String launchBatchOrBashAndWaitForOutput(String execPath, String signal, int waitTimeout) throws IOException {
		
		if(execPath == null || execPath.equals("") || signal == null || signal.equals("")) {return "";}
		
		long startTimeMillisec = System.currentTimeMillis();
		long updatedTime = startTimeMillisec;
		Runtime runtime = Runtime.getRuntime();
		StringBuilder builder = new StringBuilder();
		
		Process proc = runtime.exec(execPath);
		InputStream os = proc.getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(os));
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				builder.append(line+System.getProperty("line.separator"));
				updatedTime = System.currentTimeMillis();
				if(line.contains("HWINFOPANEL_STARTED_OK")) {
					logger.info("Signal found in output, returning string - Signal=" + signal);
					break;
				}
				
				if( (updatedTime-startTimeMillisec) > waitTimeout) {
					logger.info("Timeout occurred - " + waitTimeout + " elapsed without finding output: " + signal);
					break;
				}
			}
			reader.close();
		} catch (IOException e) {
			logger.error("There was an error reading process output from: " + execPath);
		}
		
		return builder.toString();
	}
	
	public static String readProcessOutput(InputStream inputStream) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		StringBuilder builder = new StringBuilder();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				builder.append(line);
				builder.append(System.getProperty("line.separator"));
			}
			reader.close();
		} catch (IOException e) {
			logger.error("Error reading process output:\n\n", e);
		}

		return builder.toString();
	}
	
	public static ArrayList<String> readProcessOutputLines(InputStream inputStream) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		ArrayList<String> lines = new ArrayList<>();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				lines.add(line);
			}
			reader.close();
		} catch (IOException e) {
			logger.error("Error reading process output:\n\n", e);
		}
		
		return lines;
	}
	
	public static String logProcessOutput(Process process) {
		String output = SysUtils.readProcessOutput(process.getInputStream());
		logger.info(output);
		
		return "Process output: " + output;
	}
	
	public static boolean cdIntoCommandLine(File directory) {
		OSinfo OS = retrieveOS();
			
		if(directory != null && directory.exists()) {
			directory = (directory.isDirectory())? directory : directory.getParentFile();
			if(OS.isWindows()) {
				try {
					Runtime rt = Runtime.getRuntime();
					rt.exec("cmd.exe /c start dir /p", null, directory);
					return true;
				}catch(Exception e) {
					logger.error("", e);
					return false;
				}
			}else{
				try {
					ProcessBuilder builder = new ProcessBuilder();
					builder.command("sh", "-c", "ls");
					builder.directory(directory);
					builder.start();
					
					return true;
				} catch (IOException e) {
					logger.error("", e);
					return false;
				}
			}
		}			
		
		return false;
	}
	

	public static File findDirectoryInsideRoot(File parentDirectory, String directoryToFind, boolean caseSensitive) {
       
		if(directoryToFind == null) {
			return null;
		}
		
		File[] files = parentDirectory.listFiles();
        for (File file : files) {
            if (file.isFile()) {
                continue;
            }
            if (caseSensitive && file.getName().equals(directoryToFind.trim())) {
                return file;
            } else if (!caseSensitive && file.getName().toLowerCase().equals(directoryToFind.trim().toLowerCase())) {
                return file;
            }
            
            if(file.isDirectory()) {
            	return findDirectoryInsideRoot(file, directoryToFind, caseSensitive);
            }
        }

        return null;
	}
	
	public static File findFileInsideRoot(File parentDirectory, String fileToFind, boolean caseSensitive) {
		
		if(fileToFind == null) {
			return null;
		}
		
		File[] files = parentDirectory.listFiles();
		for (File file : files) {
			if (caseSensitive && file.isFile() && file.getName().equals(fileToFind.trim())) {
				return file;
			
			} else if (!caseSensitive && file.isFile() && file.getName().toLowerCase().equals(fileToFind.trim().toLowerCase())) {
				return file;
			
			} else if(file.isDirectory()) {
				return findFileInsideRoot(file, fileToFind, caseSensitive);
			}
		}
		
		return null;
	}
	
/////////////////////////////  WINDOWS ONLY  //////////////////////////////////////////
	
	public static ArrayList<String> findWindowsProgramPath(String programName) {
		ArrayList<String> foundList = new ArrayList<String>();
		
		if(programName == null) {
			return foundList;
		}
		
		File sw32 = new File(winPath32, programName);
		File sw64 = new File(winPath64, programName);
		
		if(sw64.exists()) {
			foundList.add(sw64.getAbsolutePath());
		}
		if(sw32.exists()) {
			foundList.add(sw32.getAbsolutePath());
		}
		
		return foundList;
	}

	public static List<String> findWindowsProgramPathByNamePart(String programNamePart) {
		ArrayList<String> foundList = new ArrayList<String>();
		
		if(programNamePart == null) {
			return foundList;
		}
		
		File[] sw32 = new File(winPath32).listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().contains(programNamePart);
			}
		});
		
		File[] sw64 = new File(winPath64).listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().contains(programNamePart.toLowerCase());
			}
		});
		
		for(File dir : sw64) {
			foundList.add(dir.getAbsolutePath());
		}
		for(File dir : sw32) {
			foundList.add(dir.getAbsolutePath());
		}
		
		return foundList;
	}
	
	public static String getClassPathFromAllJarsFoundInRoot(String root, boolean isRootCurrentDir) {
		String classPath = "";
		String separator = getOsInfoLazy().isWindows() ? ";" : ":";
		
		for (File found : FileSearchAdvanced.findOnlyFilesByMatch(new File(root), ".jar", true)) {
			classPath += found + separator;
		}
		
		classPath = StringWorker.removeLastChar(classPath);
		
		String relativePrefix = isRootCurrentDir ? "./" : "";
		return "\"" + relativePrefix + classPath + "\"";
	}
	
	public static ArrayList<String> getAllDirsWithJavaExeInside(String root, String fileToFind) {
		ArrayList<String> stringRetrievedPaths = new ArrayList<String>();
		
		File currentRootDir = new File(root);
		currentRootDir.list(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				File currentFile = new File(dir.getPath() + File.separator + name);
				if(findFileInsideRoot(currentFile, fileToFind, true) != null) {
					stringRetrievedPaths.add(currentFile.getAbsolutePath());
				}
				return false;
			}
		});
				
		return stringRetrievedPaths;
	}
	
	public static ArrayList<String> getAllJavaWinVersionFromRoot(String root) {
		ArrayList<String> stringRetrievedPaths = new ArrayList<String>();
		
		File currentRootDir = new File(root);
		currentRootDir.list(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				
				if(!(name.toLowerCase().indexOf("jdk")<0) || !(name.toLowerCase().indexOf("jre")<0)) {
					if (dir.isDirectory()) {
						String currentPath = dir.getPath() + File.separator + name;
						File binFolder = new File(currentPath+"/bin");
						File exeJvm = new File(binFolder.getAbsolutePath() + File.separator + winJavaExecutable);
						// if name contains (case insensitive) jdk or jre then it's a java install directory
						if (exeJvm.exists()) {
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
	
	
////////////////////////////////  MAC-OS ONLY  /////////////////////////////////////////////////
	
	public static ArrayList<String> getMacOsJavaVersions() {
		ArrayList<String> stringRetrievedPaths = new ArrayList<String>();
		
		// TODO implement MAcOs java version
		
		return stringRetrievedPaths;
	}

	public static OSinfo getOsInfoLazy() {
		return (OS_INFO_LAZY!=null)? OS_INFO_LAZY : retrieveOS();
	}

	public enum Browser {
		CHROME("Google Chrome", "start chrome ", "google-chrome ", "open -a google-chrome "),
		FIREFOX("Mozilla Firefox", "start firefox ", "firefox ", "open -a firefox "),
		SAFARI("Safari", "start safari ", "safari ", "open -a safari "),
		EDGE("Microsoft Edge", "start microsoft-edge ", "microsoft-edge ", "open -a microsoft-edge "),
		IE("Internet Explorer", "start iexplore ", "iexplore ", "open -a iexplore "),
		OPERA("Opera", "start opera ", "opera ", "open -a opera ");
		
		public static Browser[] defaultBrowserOrder = new Browser[] {Browser.CHROME, Browser.FIREFOX, Browser.IE};
		
		public String browserName;
		public String winProgamName;
		public String bashProgamName;
		public String macProgamName;
		
		private Browser(String browserName, String winOsSwName, String bashOsSwName, String macProgamName) {
			this.browserName = browserName;
			this.winProgamName = winOsSwName;
			this.bashProgamName = bashOsSwName;
			this.macProgamName = macProgamName;
		}
		
		public String getOsLaunchSwName() {
			
			if(OS_INFO_LAZY.isWindows()) {
				return this.winProgamName;
			
			} else if(OS_INFO_LAZY.isMac()) {
				return this.macProgamName;
			
			} else {
				return this.bashProgamName;
			}
		}
	}
}


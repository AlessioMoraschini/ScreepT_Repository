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

import org.apache.log4j.Logger;

import om.JVMInfo;
import om.OSinfo;


public class SysUtils {
	
	// SysUtils logger
	static Logger logger = Logger.getLogger(SysUtils.class); 
	
	// STATIC METHODS
	private static final OSinfo OS_INFO_LAZY = new OSinfo();
	
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
	private static String startJarCommand(String jvmArgs, String jarPath) {
		return "java " + jvmArgs + " -jar " + jarPath;
	}
	
	/**
	 * SIGNATURE2 : START .JAR with ARGS FILES
	 * @param main class must be a full package and class name (Ex => [Java.Lang.String])
	 * 
	 * filteredArgsFiles can be null
	 * 
	 * params must not be null
	 */
	public static String startJarCommand(String jvmArgs, String jarPath, File[] filteredArgsFiles) {
		
		if(filteredArgsFiles == null || filteredArgsFiles.length == 0) {
			return startJarCommand(jvmArgs, jarPath);
		}
		
		String filesParams = "";
		StringBuilder builder = new StringBuilder();
		
		for(File file : filteredArgsFiles) {
			builder.append(" \"").append(file.getAbsolutePath()).append("\"");
		}
		filesParams = builder.toString();
		
		builder = new StringBuilder();
		builder.append("java ").append(jvmArgs).append(" -jar ").append(jarPath).append(filesParams);
		String command = builder.toString();
		return command;
	}
	
	
	/**
  	 * @param main class must be a full package and class name (Ex => [Java.Lang.String])
	 * 
	 * params must not be null
	 */
	private static String startClassCommand(String jvmArgs, String classPath, String mainClass) {
		return "java " + jvmArgs + " -cp " + classPath + " " + mainClass;
	}

	/**
	 * SIGNATURE2 : START .CLASS with ARGS FILES
	 * @param main class must be a full package and class name (Ex => [Java.Lang.String])
	 * 
	 * filteredArgsFiles can be null
	 * 
	 * other params must not be null
	 */
	public static String startClassCommand(String jvmArgs, String classPath, String mainClass, File[] filteredArgsFiles) {
		
		if(filteredArgsFiles == null || filteredArgsFiles.length == 0) {
			return startClassCommand(jvmArgs, classPath, mainClass);
		}
		
		String filesParams = "";
		StringBuilder builder = new StringBuilder();
		
		for(File file : filteredArgsFiles) {
			builder.append(" \"").append(file.getAbsolutePath()).append("\"");
		}
		filesParams = builder.toString();
		
		builder = new StringBuilder();
		builder.append("java ").append(jvmArgs)
			   .append(" -cp ").append(classPath)
			   .append(" ").append(mainClass)
			   .append(filesParams);
		String command = builder.toString();
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
			// TODO
			// END LINUX
		}else if(OS.isMac()) {
			// TODO
			// END MAC
		}else if(OS.isWindows()) {
			
			ArrayList<String> foundJavaRootFolders = findWindowsProgramPath("java");
			for(String currFolder : foundJavaRootFolders) {
				ArrayList<String> foundVersions = getAllJavaWinVersionFromRoot(currFolder);
				for (String currVersionPath : foundVersions) {
					foundJVMS.add(currVersionPath);
				}
			}
		} // END WINDOWS
		
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
	 * Use this method to remove every charachter from given string, except "." and
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

	/**
	 * return true if current architecture is 64 bits, false is 32 bits.
	 * 
	 * @return
	 */
	public static boolean is64arch() {
		String arch = System.getenv("PROCESSOR_ARCHITECTURE");
		if (arch.endsWith("64")) {
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
		String arch = System.getenv("PROCESSOR_ARCHITECTURE");
		if (arch.endsWith("32")) {
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
					logger.error(e);
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
					logger.error(e);
					return false;
				}
			}
		}			
		
		return false;
	}
	
/////////////////////////////  WINDOWS ONLY  //////////////////////////////////////////
	
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
						File binFolder = new File(currentPath+"/bin");
						File exeJvm = new File(binFolder.getAbsolutePath()+"/java.exe");
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
	
////////////////////////////////  LINUX ONLY  ///////////////////////////////////////////////// sudo update-alternatives --config java
	
	public static ArrayList<String> getLinuxJavaVersions() {
		ArrayList<String> stringRetrievedPaths = new ArrayList<String>();
		
		// TODO implement this
		
		return stringRetrievedPaths;
		
	}

	public static OSinfo getOsInfoLazy() {
		return (OS_INFO_LAZY!=null)? OS_INFO_LAZY : retrieveOS();
	}
}

package launcherutils;

import java.util.ArrayList;

import launcherutils.om.JVMInfo;
import launcherutils.om.OSinfo;

public class SysUtils {
	
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
			
		}else if(OS.isMac()) {
			
		}else if(OS.isWindows()) {
			
		}
		
		return foundJVMS;
	}
	
}

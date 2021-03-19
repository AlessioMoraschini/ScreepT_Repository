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
package launcherutils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import mainlauncher.ScreepTLauncher;
import om.OSinfo;
import utility.string.StringWorker;
import utils.SysUtils;

public class JvmHelper {
	
	// JvmHelper logger
	static Logger logger = Logger.getLogger(JvmHelper.class);
	
	// FINAL STRINGS STATICS PARAMS
	/**
	 * Means that current JVM which has loaded launcher is ok, and will use that
	 */
	public static final String JVM_OK = "JVMOK";
	public static final String NOT_FOUND_JVM = "";
	public static final String JVM_IGNORE_WARN = "JVM_IGNORE_WARNINGS";
	public static final String JVM_URL_INSTALL =
//			"https://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html";
			"https://adoptopenjdk.net/releases.html";
	
	// FLag used to open customize options dialog at start, when warning icon will signal to user about the problem
	public static boolean warningJVM = false;
	
	// USER MANUALLY LOCATED DEPENDENCY PATHS
	public String manualPath_Jvm = NOT_FOUND_JVM;
	public String javaPath = NOT_FOUND_JVM;
	
	// INSTANCE FIELDS
	OSinfo currentOS;
	String MAX_JRE_VERSION;
	String MIN_JRE_VERSION;
	
	// CONSTRUCTOR //
	public JvmHelper(String MAX_JRE_VERSION, String MIN_JRE_VERSION) {
		currentOS = SysUtils.getOsInfoLazy();
		this.MIN_JRE_VERSION = MIN_JRE_VERSION;
		this.MAX_JRE_VERSION = MAX_JRE_VERSION;
	}
	
	// SOME UTILS
	
	/**
	 * Find right versions of JVM installed on this machine (According to OS), and set its path to javaPath field in this class.
	 * 
	 * @return If found return true, else return false
	 */
	public String retrieveCorrectJVMS(boolean ignoreWarnings) {
		
		logger.info("Started java installed versions check!");
		String currJvmVersion = SysUtils.getCurrentJVMversion();
		
		// Search and compare JVM versions
		ArrayList<String> foundJvmHomeList = SysUtils.findJvmVersionsInstalled(currentOS);
		String foundCorrect = compareWithAdmittedJVMS(foundJvmHomeList);
		String foundCorrectBin = (!foundCorrect.equals(NOT_FOUND_JVM))? foundCorrect+"/bin": NOT_FOUND_JVM;
		
		// if not found and current is correct return it and exit method
		if("".equals(StringWorker.trimToEmpty(foundCorrectBin)) && checkSingleJavaVersionOK(currJvmVersion)) {
			logger.info("Current JVM("+currJvmVersion+") is OK -> selected and skipping search");
			foundCorrectBin = JVM_OK;
			// adding ; to path does not modify current retrieved and escapes NOT_FOUND_JVM controls as path won't be ""
		}

		// Update JVM Warning wignal
		String foundCorrectName = new File(foundCorrect).getName();
		warningJVM = foundCorrect.equals(NOT_FOUND_JVM) 
				|| StringWorker.trimToEmpty(foundCorrect).equals("") 
				|| !checkSingleJavaVersionOK(foundCorrectName, IniFileManager.MAX_JRE_VERSION_FULL_COMPATIBILITY, IniFileManager.MIN_JRE_VERSION);
		
		// case of ignore warnings specified : use current JVM
		if(ignoreWarnings) {
			// if user selected to ignore given warnings
			logger.info("User have chosen to ignore warnings: Current JVM("+currJvmVersion+") will be used.");
			return JVM_IGNORE_WARN;
		}

		logger.info("\n\n => Versions found: " + foundJvmHomeList + "\n\n => Found correct one: " + foundCorrectBin + "\nWARNING: "+warningJVM);
		
		return foundCorrectBin;
	}
	
	/**
	 * Compare a list of JVM versions to find the best match
	 * @param foundJVMS
	 * @return the path of the current JVM
	 */
	private String compareWithAdmittedJVMS(List<String> foundJVMS) {
		
		// first check user located JVM if it exist
		if(!manualPath_Jvm.equals(NOT_FOUND_JVM) && isOKManualJVM()) {
			File binPath = new File(manualPath_Jvm);
			return binPath.getParentFile().getAbsolutePath();
		}
		
		// PREFERRED BY USER IS DEFINED -> Use that
		if(foundJVMS.contains(ScreepTLauncher.iniFileManager.preferredJvm)) {
			String currentPath = ScreepTLauncher.iniFileManager.preferredJvm;
			String currentPathName = new File(currentPath).getName();
			if(checkSingleJavaVersionOK(currentPathName)) {
				return currentPath;
			}
		}
		
		// PREFERRED BY USER IS NOT DEFINED -> Try default
		// Search first if there is one that match APPLICATION_DEFAULT preferred Jvm
		for(String currentPath : foundJVMS) {
			
			// find name from path
			String versionDirName = new File(currentPath).getName();
			
			try {
				// MATCH PREFERRED DEFAULT JVM CONDITION
				if (StringWorker.keepOnlyDotsAndDigits(versionDirName).startsWith(ScreepTLauncher.iniFileManager.preferredJvmVersionIfNotSpecified)) {
					if (checkSingleJavaVersionOK(versionDirName)) {
						return currentPath;
					} 
				}
			} catch (IllegalArgumentException e) {
				logger.error("Error comparing java version for " + currentPath, e);
			}
			
		}
		
		// USER PREFERRED AND APP DEFAULT ARE NOT FOUND: search automatically for one that is between max and min
		for(String currentPath : foundJVMS) {
			
			// find name from path
			String versionDirName = new File(currentPath).getName();
			
			try {
					
				if(checkSingleJavaVersionOK(versionDirName)) {
					return currentPath;
				}
			} catch (IllegalArgumentException e) {
				logger.error("Error comparing java version for " + currentPath, e);
			}
			
		}
		
		// if not found rech this point and return 
		return NOT_FOUND_JVM;
	}

	/**
	 * check if a given java version (which can contain letters or underscores) respect
	 * min and max values : if less than max and more(or equal) to min then is OK.
	 * @param version
	 * @return
	 */
	public boolean checkSingleJavaVersionOK(String version) {
		
		// remove non dots chars and transform undescores to dots
		String noLettersDotsOnly = StringWorker.keepOnlyDotsAndDigits(version);
		
		boolean lessThanMax;
		boolean moreThanMin;
		try {
			lessThanMax = StringWorker.compareVersions(noLettersDotsOnly, MAX_JRE_VERSION) < 0;
			moreThanMin = StringWorker.compareVersions(noLettersDotsOnly, MIN_JRE_VERSION) >= 0;
		} catch (IllegalArgumentException e) {
			logger.error("Error comparing version: illegal format of ["+version+"]", e);
			return false;
		}
	
		return (lessThanMax && moreThanMin);
	}

	/**
	 * check if a given java version (which can contain letters or underscores) respect
	 * min and max values : if less than max and more(or equal) to min then is OK.
	 * @param version
	 * @return
	 */
	public boolean checkSingleJavaVersionOK(String version, String max, String min) {
		
		// remove non dots chars and transform undescores to dots
		String noLettersDotsOnly = StringWorker.keepOnlyDotsAndDigits(version);
		
		if(!noLettersDotsOnly.startsWith("1.")) {
			noLettersDotsOnly = "1.".concat(noLettersDotsOnly);
		}
		
		boolean lessThanMax;
		boolean moreThanMin;
		try {
			lessThanMax = StringWorker.compareVersions(noLettersDotsOnly, max) < 0;
			moreThanMin = StringWorker.compareVersions(noLettersDotsOnly, min) >= 0;
		} catch (IllegalArgumentException e) {
			logger.error("Error comparing version: illegal format of ["+version+"]", e);
			return false;
		}
		
		return (lessThanMax && moreThanMin);
	}
	
	public boolean isOKManualJVM() { // TODO improve and make multiOS
		File manualJvmBin = new File(manualPath_Jvm);
		boolean errorCondition1 = !manualJvmBin.exists();
		String version ="";
		try {
			version = SysUtils.getJavaVersionFromBin(manualPath_Jvm);
		} catch (IOException e) {
			logger.error("Error checking java version from release file in /bin directory", e);
			return false;
		}
		boolean errorCondition2 = !checkSingleJavaVersionOK(version) ;
		if(errorCondition1 || errorCondition2) {
			return false;
		}else {
			return true;
		}
	}
}

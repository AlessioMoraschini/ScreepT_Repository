package launcherutils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import om.OSinfo;
import utils.SysUtils;

public class JvmHelper {
	
	// JvmHelper logger
	static Logger logger = Logger.getLogger(JvmHelper.class);
	
	// FINAL STRINGS STATICS PARAMS
	public static final String NOT_FOUND_JVM = "";
	public static final String JVM_OK = "JVMOK";
	public static final String JVM_URL_INSTALL =
			"https://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html";
	
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
	 * Find all versions of JVM installed on this machine (According to OS), and find if there is one in the 
	 * admitted range, and set its path to javaPath field in this class.
	 * 
	 * @return If found return true, else return false
	 */
	public String retrieveCorrectJVMS(boolean ignoreWarnings) {
		
		logger.info("Started java installed versions check!");
		String currJvmVersion = SysUtils.getCurrentJVMversion();
		
		// if current is correct return it and exit method
		if(checkSingleJavaVersionOK(currJvmVersion)) {
			logger.info("Current JVM("+currJvmVersion+") is OK -> selected and skipping search");
			return JVM_OK;
			// adding ; to path does not modify current retrieved and escapes NOT_FOUND_JVM controls as path won't be ""
		}
		
		if(ignoreWarnings) {
			// if user selected to ignore given warnings
			logger.info("User have chosen to ignore warnings: Current JVM("+currJvmVersion+") will be used.");
			return JVM_OK;
		}
		
		ArrayList<String> foundJvmHomeList = SysUtils.findJvmVersionsInstalled(currentOS);
		String foundCorrect = compareWithAdmittedJVMS(foundJvmHomeList);
		foundCorrect = (!foundCorrect.equals(NOT_FOUND_JVM))? foundCorrect+"/bin": NOT_FOUND_JVM;
		
		logger.info("\n\n => Versions found: " + foundJvmHomeList + "\n\n => Found correct one: " + foundCorrect + "\n");
		
		return foundCorrect;
	}
	
	/**
	 * Compare a list of JVM versions with the admitted one
	 * @param foundJVMS
	 * @return the path of the current JVM
	 */
	private String compareWithAdmittedJVMS(List<String> foundJVMS) {
		
		// first check used located JVM if it exist
		if(!manualPath_Jvm.equals(NOT_FOUND_JVM) && isOKManualJVM()) {
			File binPath = new File(manualPath_Jvm);
			return binPath.getParentFile().getAbsolutePath();
		}
		
		// if unspecified manual search automatically
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
		String noLettersDotsOnly = SysUtils.keepOnlyDotsAndDigits(version);
		
		boolean lessThanMax;
		boolean moreThanMin;
		try {
			lessThanMax = SysUtils.compareVersions(noLettersDotsOnly, MAX_JRE_VERSION) < 0;
			moreThanMin = SysUtils.compareVersions(noLettersDotsOnly, MIN_JRE_VERSION) >= 0;
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

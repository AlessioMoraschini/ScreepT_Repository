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
package logger;

import java.util.Date;

import org.apache.log4j.Level;

import resources.GeneralConfig;

public class LoggerUtils {
	
	public final static Level DEFAULT_LOG_LEVEL = Level.ERROR;

	public static String getStartString(String appName, boolean printDate) {
		
		Date now = new Date();
		String dateString = (printDate)? now.toString() : "" ;
		
		String startString = "\n\n##################################################\n\n"
							   + "Build N°: " + GeneralConfig.BUILD_INCREMENTAL_NUMBER + "\n\n"
							   + "Build HASH: " + GeneralConfig.BUILD_COMMIT_HASH + "\n\n"
							   + dateString +"\n\n" + appName + " Started now!\n\n"
							   + "##################################################\n\n";
				
		return startString;
	}
	
	public static String getExitString(String appName, boolean printDate) {
		
		Date now = new Date();
		String dateString = (printDate)? now.toString() : "" ;
		
		String startString = "\n\n##################################################\n\n"
				   + "Build N°: " + GeneralConfig.BUILD_INCREMENTAL_NUMBER + "\n\n"
				   + "Build HASH: " + GeneralConfig.BUILD_COMMIT_HASH + "\n\n"
				   + dateString +"\n\n" + appName + " Closing Application!\n"
				   + "##################################################\n\n";
				
		return startString;
	}
	
	public static String getGeneralErrorExitMsg(String appName, boolean printDate) {
		
		Date now = new Date();
		String dateString = (printDate)? now.toString() : "" ;
		
		String startString = "\n\n##################################################\n\n"
						+ dateString + "\n\n" + appName + " HAD AN UNEXPECTED ERROR - FORCING CLOSE\n\n"
						+ "##################################################\n\n";
				
		return startString;
	}
	
	public static Level getLogLevelFromString(String stringLevel) {
		if(stringLevel == null) {
			return DEFAULT_LOG_LEVEL;
		}else if(stringLevel.equals("OFF")) {
			return Level.OFF;
		}else if(stringLevel.equals("FATAL")) {
			return Level.FATAL;
		}else if(stringLevel.equals("ERROR")) {
			return Level.ERROR;
		}else if(stringLevel.equals("WARN")) {
			return Level.WARN;
		}else if(stringLevel.equals("DEBUG")) {
			return Level.DEBUG;
		}else if(stringLevel.equals("INFO")) {
			return Level.INFO;
		}else if(stringLevel.equals("ALL")) {
			return Level.ALL;
		}else {
			return DEFAULT_LOG_LEVEL;
		}
	}
}

/**
 *
 * =========================================================================================
 *  Copyright (C) 2019-2020
 *
 *  AM-Design-Development - (Alessio Moraschini)
 * =========================================================================================
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package screept.launcher.gui.utils;

import java.util.Date;

import org.apache.log4j.Level;

public class LauncherLoggerUtils {
	
	public final static Level DEFAULT_LOG_LEVEL = Level.ERROR;

	public static String getStartString(String appName, boolean printDate) {
		
		Date now = new Date();
		String dateString = (printDate)? now.toString() : "" ;
		
		String startString = "\n\n##################################################\n\n"
							   + dateString +"\n\n" + appName + " Started now!\n\n"
							   + "##################################################\n\n";
				
		return startString;
	}
	
	public static String getExitString(String appName, boolean printDate) {
		
		Date now = new Date();
		String dateString = (printDate)? now.toString() : "" ;
		
		String startString = "\n\n##################################################\n\n"
				   + dateString +"\n\n" + appName + " Closing Application!\n\n"
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

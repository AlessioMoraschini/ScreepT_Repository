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
package screept.launcher.utils.om;

import os.commons.om.OSinfo;
import os.commons.utils.SysUtils;

public class DependencyLocalPath {
	
	public String winPath;
	public String linuxPath;
	public String macPath;
	
	public DependencyLocalPath(String winPth, String linPth, String macPth){
		winPath = (winPth == null)? "" : winPth;
		linuxPath = (linPth == null)? "" : linPth;
		macPath = (macPth == null)? "" :  macPth;
	}
	
	public String getCurrentOSPath() {
		OSinfo OS = SysUtils.retrieveOS();
		if(OS.isWindows()) {
			return this.winPath;
		}else if(OS.isMac()) {
			return this.macPath;
		}else if(OS.isLinux()) {
			return this.linuxPath;
		}else {
			return "";
		}
	}
}

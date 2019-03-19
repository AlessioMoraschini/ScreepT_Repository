package launcherutils.om;

import launcherutils.SysUtils;

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

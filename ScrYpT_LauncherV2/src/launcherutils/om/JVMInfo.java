package launcherutils.om;

import java.util.ArrayList;

public class JVMInfo {	
	
	private String javaHome;
	private String javaCurrVersion;
	private ArrayList<String> javaVersionsFound;
	
	public JVMInfo() {
		
	}

	public String getJavaHome() {
		return javaHome;
	}

	public String getJavaCurrVersion() {
		return javaCurrVersion;
	}
	
	public ArrayList<String> getJavaVersionsFound() {
		return javaVersionsFound;
	}

}

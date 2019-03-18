package launcherutils.om;

import java.awt.event.ActionListener;

public class DependencyModel {
	
	public String dependencyName;
	public String solution;
	public boolean isCritical;
	public ActionListener downloadAndInstall;
	public ActionListener locateManually;
	
	public DependencyModel(String name, String sol, boolean critical, ActionListener downInstall, ActionListener locate) {
		dependencyName = name;
		solution = sol;
		isCritical = critical;
		downloadAndInstall = downInstall;
		locateManually = locate;
	}

	@Override
	public String toString() {
		String critical = (isCritical)? "YES" : "NO";
		return "DependencyModel [dependencyName=" + dependencyName + ", Critical : " + critical + "]";
	}
	
	
}

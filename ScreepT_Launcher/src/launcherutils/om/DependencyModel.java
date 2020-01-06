package launcherutils.om;

import java.awt.event.ActionListener;

public class DependencyModel {
	
	public final static String INSTALLERS_PATH = "Dependencies_Installers/";
	
	public String dependencyName; // cannot be null
	public String solution; // can be null
	public boolean isCritical;
	public ActionListener downloadAndInstall; // cannot be null
	public String URL; // can be null
	public DependencyLocalPath relativePathToInstaller; // can be null
	public ActionListener locateManually; // cannot be null
	
	public DependencyModel(String name, String sol, boolean critical, String url, DependencyLocalPath localPath, ActionListener downInstall, ActionListener locate) {
		dependencyName = name;
		solution = (sol == null)? "" : sol;
		isCritical = critical;
		downloadAndInstall = downInstall;
		locateManually = locate;
		URL = url;
		relativePathToInstaller = (localPath == null)?  new DependencyLocalPath("", "", ""): localPath;
	}

	@Override
	public String toString() {
		String critical = (isCritical)? "YES" : "NO";
		return "DependencyModel [dependencyName=" + dependencyName + ", Critical : " + critical + "]";
	}
	
	
}

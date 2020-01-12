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

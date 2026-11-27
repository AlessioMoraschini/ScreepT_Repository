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
package screept.launcher.utils.om;

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

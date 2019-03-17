package launcherutils.om;

public class DependencyModel {
	
	public String dependencyName;
	public String dependencyPath;
	public boolean isCritical;
	
	public DependencyModel(String name, String path, boolean critical) {
		dependencyName = name;
		dependencyPath = path;
		isCritical = critical;
	}

	@Override
	public String toString() {
		String critical = (isCritical)? "YES" : "NO";
		return "DependencyModel [dependencyName=" + dependencyName + ", Critical : " + critical + "]";
	}
	
	
}

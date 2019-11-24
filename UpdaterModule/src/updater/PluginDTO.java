package updater;

import java.io.File;

public class PluginDTO {

	public File localFile;
	public String completeURL;
	public String name;
	public String checkSum;
	
	public PluginDTO(String completeURL, String name, String checkSum) {
		this.completeURL = completeURL;
		this.name = name;
		this.checkSum = checkSum;
		this.localFile = null;
	}

	public String getCompleteURL() {
		return completeURL;
	}

	public void setCompleteURL(String completeURL) {
		this.completeURL = completeURL;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCheckSum() {
		return checkSum;
	}

	public void setCheckSum(String checkSum) {
		this.checkSum = checkSum;
	}
	
	public File getLocalFile() {
		return localFile;
	}

	public void setLocalFile(File localFile) {
		this.localFile = localFile;
	}

	@Override
	public String toString() {
		return "PluginDTO [localPath=" + localFile + ", completeURL=" + completeURL + ", name=" + name + ", checkSum=" + checkSum + "]";
	}

}

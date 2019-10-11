package amfileutils.filechangewatcher;
import java.io.File;
import java.util.ArrayList;

public class ChangeDTO {

	public ArrayList<File> changedFiles;
	public ArrayList<File> addedFiles;
	public ArrayList<File> removedFiles;
	
	public ChangeDTO(){
		changedFiles = new ArrayList<File>();
		addedFiles = new ArrayList<File>();
		removedFiles = new ArrayList<File>();
	}

	@Override
	public String toString() {
		return "ChangeDTO [changedFiles=" + str(changedFiles) + ", addedFiles=" + str(addedFiles) + ", removedFiles=" + str(removedFiles) + "]";
	}
	
	private String str(ArrayList<File> list) {
		StringBuilder sb = new StringBuilder();
		for(File curr : list) {
			sb.append(curr.getAbsolutePath());
		}
		
		return sb.toString();
	}
	
}

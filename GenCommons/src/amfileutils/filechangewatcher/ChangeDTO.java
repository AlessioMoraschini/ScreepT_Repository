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
}

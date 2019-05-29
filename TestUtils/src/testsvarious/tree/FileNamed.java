package testsvarious.tree;

import java.io.File;

public class FileNamed {

	public File fileObj;
	
	public FileNamed(File file){
		fileObj = file;
	}
	
	public String toString(){
		return fileObj.getName();
	}
}

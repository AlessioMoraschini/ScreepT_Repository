package gUI.utils.tree.dragndrop.om;

import java.io.File;

public class CrypterDragFileModel {
	public File realFile;
	public String name;
	
	public CrypterDragFileModel(File source) {
		this.name = source.getName();
		this.realFile = source;
	}
	
	public String toString() {
		return realFile.getName();
	}
}

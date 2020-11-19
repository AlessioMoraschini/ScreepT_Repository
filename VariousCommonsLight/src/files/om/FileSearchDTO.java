package files.om;

import java.io.File;

import files.search.FileSearchAdvanced.SearchType;


public class FileSearchDTO {

	public File root;
	public String matchNamePart;
	public boolean exactMatch;
	public SearchType filter;
	public boolean recursiveDeep;
	public boolean ignoreCase;
	
	public FileSearchDTO(
			File root,
			String matchNamePart,
			boolean exactMatch,
			SearchType filter, 
			boolean recursiveDeep,
			boolean ignoreCase) {
		
		super();
		this.root = root;
		this.matchNamePart = matchNamePart;
		this.exactMatch = exactMatch;
		this.filter = filter;
		this.recursiveDeep = recursiveDeep;
		this.ignoreCase = ignoreCase;
	}
}

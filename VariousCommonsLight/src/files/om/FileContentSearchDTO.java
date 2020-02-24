package files.om;

import utility.string.StringWorker;

public class FileContentSearchDTO {
	
	public boolean matchCase;
	public boolean wholeWord;
	public boolean regex;
	public String searchString;
	
	
	public FileContentSearchDTO(String searchString) {
		
		this.searchString = StringWorker.trimToEmpty(searchString);
		this.matchCase = false;
		this.wholeWord = false;
		this.regex = false;
	}
	
	public FileContentSearchDTO(String searchString, boolean matchCase, boolean wholeWord, boolean regex) {
		this.searchString = StringWorker.trimToEmpty(searchString);
		this.matchCase = matchCase;
		this.wholeWord = wholeWord;
		this.regex = regex;
	}

	@Override
	public String toString() {
		return "FileContentSearchDTO [matchCase=" + matchCase + ", wholeWord=" + wholeWord + ", regex=" + regex + ", searchString=" + searchString + "]";
	}
	
}

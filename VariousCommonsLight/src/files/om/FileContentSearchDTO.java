package files.om;

import utility.string.StringWorker;

public class FileContentSearchDTO {
	
	public boolean matchCase;
	public boolean wholeWord;
	public boolean regex;
	public boolean binariesIncluded;
	public String searchString;
	
	
	public FileContentSearchDTO(String searchString) {
		
		this.searchString = StringWorker.trimToEmpty(searchString);
		this.matchCase = false;
		this.wholeWord = false;
		this.regex = false;
		this.binariesIncluded = false;
	}
	
	public FileContentSearchDTO(String searchString, boolean matchCase, boolean wholeWord, boolean regex, boolean binariesIncluded) {
		this.searchString = StringWorker.trimToEmpty(searchString);
		this.matchCase = matchCase;
		this.wholeWord = wholeWord;
		this.regex = regex;
		this.binariesIncluded = binariesIncluded;
	}

	@Override
	public String toString() {
		return "FileContentSearchDTO [matchCase=" + matchCase + ", wholeWord=" + wholeWord + ", regex=" + regex + ", binariesIncluded=" + binariesIncluded
				+ ", searchString=" + searchString + "]";
	}

}

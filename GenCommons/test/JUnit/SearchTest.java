package JUnit;

import java.io.File;
import java.util.ArrayList;

import org.junit.Test;

import amfileutils.FileSearch;

public class SearchTest {

	String fileNameMatch = "java";
	String folderMatch = "java";
	File rootDir = new File("C:/Users/xx/Desktop/Documenti&Utili/Informatica varie/CryptoSteg/Test/SearchTest");
	
	@Test
	public void fileSearchTest() {
		ArrayList<File> matchesFound = new ArrayList<File>();
		
		// search in subdirs
		matchesFound = FileSearch.searchFoldersInRootFolder(rootDir, folderMatch, true);
		printFound(matchesFound);
		
		matchesFound = FileSearch.searchFilesInRootFolder(rootDir, folderMatch, true);
		printFound(matchesFound);
	}
	
	private void printFound(ArrayList<File> fileList) {
		for (File iFile : fileList) {
			System.out.println(" => Found: " + iFile.getAbsolutePath());
		}
	}
}

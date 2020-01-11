package files;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import files.FileSearchAdvanced.SearchType;

public class TestSearchAdvanced {

//	private static final String MATCH = "MATCH_TEST";
//	private static final File rootFolder = new File("TEST_ROOT_FOLDER");

	private static final String MATCH = "WL";
	private static final File rootFolder = new File("D:/ibnx0Domain - file backup");
	
	@Test
	@Ignore
	public void testFolderSearchDeep() throws IOException {
		
		System.out.print("TEST_SEARCH FOLDER (DEEP SEARCH) INSIDE: " + rootFolder.getCanonicalPath());
		searchTest(SearchType.FOLDERS_ONLY, true);
	}

	@Test
	@Ignore
	public void testFolderSearch() throws IOException {
		
		System.out.print("TEST_SEARCH FOLDER (NON DEEP) INSIDE: " + rootFolder.getCanonicalPath());
		searchTest(SearchType.FOLDERS_ONLY, false);
	}
	
	@Test
	public void testFileSearchDeep() throws IOException {
		
		System.out.print("TEST_SEARCH FILE (DEEP SEARCH) INSIDE: " + rootFolder.getCanonicalPath());
		searchTest(SearchType.FILE_ONLY, true);
	}
	
	@Test
	public void testFileSearch() throws IOException {
		
		System.out.print("TEST_SEARCH FILE (NON DEEP) INSIDE: " + rootFolder.getCanonicalPath());
		searchTest(SearchType.FILE_ONLY, false);
	}
	
	@Test
	@Ignore
	public void testAllSearchDeep() throws IOException {
		
		System.out.print("TEST_SEARCH ALL (DEEP SEARCH) INSIDE: " + rootFolder.getCanonicalPath());
		searchTest(SearchType.ALL, true);
	}
	
	@Test
	@Ignore
	public void testAllSearch() throws IOException {
		
		System.out.print("TEST_SEARCH ALL (NON DEEP) INSIDE: " + rootFolder.getCanonicalPath());
		searchTest(SearchType.ALL, false);
	}
	
	
	// ######## UTILITY ###########
	private void searchTest(SearchType type, boolean deepSearch) {
		long start = System.currentTimeMillis();
		
		List<File> result = FileSearchAdvanced.findByMatch(rootFolder, MATCH, type, deepSearch);
		
		long end = System.currentTimeMillis();
		printElapsed(result.size(), start, end);
		
		int i = 1;
		for(File res : result) {
			System.out.println("RESULT N� " + i + " -> " +  res);
			i++;
		}
		
		System.out.println("\n\n ############################# END ##################################\n\n");
	}
	
	private void printElapsed(int i, long start, long end) {
		System.out.println ("   => TEST COMPLETED with " 
				+ i + " results (" + "Elapsed milliseconds: " + (end-start) + ")\n\n");
	}
}

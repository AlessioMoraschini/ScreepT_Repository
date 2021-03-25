package utils;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import test.util.utils.TestUtils;
import util.ScreeptUtils;

public class CountTest {

	private File ROOT_FOLDER = new File("TEST_FILES/TestFolder");
//	private File ROOT_FOLDER = new File("TEST_FILES");
	
	@Test
	public void testCount() throws IOException {
		TestUtils.printHeader("#", 50, "COUNTING ALL INTO " + ROOT_FOLDER.getCanonicalPath());
		int total = ScreeptUtils.countFilesInDirectory(ROOT_FOLDER, true);
		TestUtils.printl("TOTAL N°: " + total);

		TestUtils.printHeader("#", 50, "COUNTING FILES ONLY INTO " + ROOT_FOLDER.getCanonicalPath());
		int totalFiles = ScreeptUtils.countFilesInDirectory(ROOT_FOLDER, false);
		TestUtils.printl("TOTAL FILES N°: " + totalFiles);
	}
}

package files;

import java.io.File;

import org.junit.Test;

import testconfig.TestConstants;

public class FilesGenericTest {

	String outPathMKDIRS = TestConstants.destFolderPath + "testFolder_MKDIRS/test.txt";
	@Test
	public void testMkDirs() {
		File out = new File(outPathMKDIRS);
		out.getParentFile().mkdirs();
	}
}

package utils;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

public class TestCopyDirContentIntoTarget {
	
	public static final File sourceDir = new File("DownloadTest/SourceFolder");
	public static final File destDir = new File("DownloadTest/DestFolder");
	
	@Test
	public void testCopyUpdates() throws IOException {
		Various.copyDirectoryContents(sourceDir, destDir);
	}

}

package files;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import testconfig.TestConstants;


public class UnzipTest {

	@Test
	public void unzipTest() throws Exception {
		assertNotNull(ZipWorker.unzipFile(TestConstants.sourceFolderPath + "test.zip", TestConstants.destFolderPath + "unzippedTest", true));
	}
}

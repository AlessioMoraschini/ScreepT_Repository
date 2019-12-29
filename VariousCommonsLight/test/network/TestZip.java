package network;
import java.io.File;

import static testconfig.TestConstants.*;

import org.junit.Test;

import network.DownloadUtils;

public class TestZip {

	@Test
	public void testZipFolder() throws Exception {
		File zipped = DownloadUtils.zipFolder(sourceFolderPath + "FolderC", destFolder, true, true);
		System.out.println(zipped);
	}
}

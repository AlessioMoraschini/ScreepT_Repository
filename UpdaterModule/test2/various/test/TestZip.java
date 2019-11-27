package various.test;
import java.io.File;

import org.junit.Test;

import updater.DownloadUtils;

public class TestZip {

	public static File sourceFolder = new File("UPDATES/updates");
	
	@Test
	public void testZip() throws Exception {
		File zipped = DownloadUtils.zipFolder("UPDATES/updates", new File("UPDATES/"), true);
		System.out.println(zipped);
	}
}

package various.test;
import java.io.File;
import java.io.IOException;

import org.junit.Test;

import forks.merged.Various;

public class TestRelativePath {

	@Test
	public void testRelativePath() throws IOException {
		File A = new File("UPDATES/updates/LAUNCHER/Config");
		File B = new File("UPDATES/updates");
		
		System.out.println(Various.getPathRelativeToOtherFile(A, B));
		
		System.out.println("".split(","));
		
	}
}

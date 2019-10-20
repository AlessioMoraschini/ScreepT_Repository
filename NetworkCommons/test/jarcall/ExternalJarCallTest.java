package jarcall;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import utils.SysUtils;

public class ExternalJarCallTest {

	Runtime runtime;
	String batchPath = "LauncherNetInfoTool.bat";
	
	@Before
	public void init() {
		runtime = Runtime.getRuntime();
	}
	
	@Test
	public void testJarBatchCall() throws IOException {
		System.out.print("Starting...");
		
		String res = SysUtils.launchBatchOrBashAndWaitForOutput(batchPath, "OK_NETWORKPANEL_STARTED", 2000);
		
		System.out.print(res+"\n\n => Launched :)");
	}
	
}

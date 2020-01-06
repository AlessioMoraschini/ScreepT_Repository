
import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;

import network.NetworkUtils;
import testUtils.TestUtils;
import utils.SysUtils;

public class NetworkUtilsTest {
	
	@Test
	@Ignore
	public void testDns() throws Exception {
		TestUtils.printArray(NetworkUtils.getDnsServers().toArray());
		TestUtils.print(NetworkUtils.getIpNetstatGateway());
		
	}
	
	@Test
	public void testOutputConsole() throws IOException {
		Process result = Runtime.getRuntime().exec("netstat -rn");
		TestUtils.print(SysUtils.readProcessOutput(result.getInputStream()));
	}
}

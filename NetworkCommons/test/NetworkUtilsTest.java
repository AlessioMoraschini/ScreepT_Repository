
import org.junit.Test;

import network.NetworkUtils;
import testUtils.TestUtils;

public class NetworkUtilsTest {
	
	@Test
	public void testDns() throws Exception {
		TestUtils.printArray(NetworkUtils.getDnsServers().toArray());
		TestUtils.print(NetworkUtils.getIpNetstatGateway());
		
	}
}

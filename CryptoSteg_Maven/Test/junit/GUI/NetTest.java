package junit.GUI;

import java.io.IOException;
import java.util.Arrays;

import org.hyperic.sigar.SigarException;
import org.junit.Test;

import network.NetworkUtils;


public class NetTest {

	@Test
	public void testNetInfo() throws IOException {
		
		NetworkUtils utils = new NetworkUtils();
		try {
			print(Arrays.toString(utils.getSysINFO().getNetInterfaceList()));
			print(utils.getSysINFO().getNetInterfaceConfig());
		} catch (SigarException e) {
			print("error");
		}
	}
	
	// lazy printer
	private void print(Object src) {
		System.out.println(src);
	}
	
	private void separatorPrint() {
		print("\n   ###################################################\n");
	}
}

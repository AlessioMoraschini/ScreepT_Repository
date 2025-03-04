package junit.GUI;

import java.io.IOException;
import java.util.Arrays;

import org.junit.Test;

import network.NetworkUtils;


public class NetTest {

	@Test
	public void testNetInfo() throws IOException {
		
		NetworkUtils utils = new NetworkUtils();
		try {
			print(Arrays.toString(utils.getAdapters().toArray()));
		} catch (Exception e) {
			print("error");
		}
	}
	
	// lazy printer
	private void print(Object src) {
		System.out.println(src);
	}
	
	protected void separatorPrint() {
		print("\n   ###################################################\n");
	}
}

package junit.netscanner;

import java.util.ArrayList;
import java.util.List;

import org.jnetpcap.Pcap;
import org.jnetpcap.PcapIf;
import org.junit.Test;

import testUtils.Printer;

public class DllClassOKTest {
	
	@Test
	public void ExistingDllTest() {
		
		try {
			List<PcapIf> alldevs = new ArrayList<PcapIf>();  
//			StringBuilder errbuf = new StringBuilder();  
//			int r = Pcap.findAllDevs(alldevs, errbuf);
			Pcap.libVersion();
			Printer.print("=> OK");
		} catch (UnsatisfiedLinkError e) {
			e.printStackTrace();
		}  
		
	}
}

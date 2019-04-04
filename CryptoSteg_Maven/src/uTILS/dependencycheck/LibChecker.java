package uTILS.dependencycheck;

import org.apache.log4j.Logger;
import org.jnetpcap.Pcap;

public class LibChecker {
	
	// mainGUI logger
	static Logger logger = Logger.getLogger(LibChecker.class);
		
	public static boolean checkJnetPcapDll() {
		try {
			Pcap.libVersion();
			return true;
		} catch (UnsatisfiedLinkError e) {
			logger.error("! JnetPcap dll link error ! \n\n", e);
			return false;
		}
	}
}

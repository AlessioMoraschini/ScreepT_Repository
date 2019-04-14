package uTILS.dependencycheck;

import org.apache.log4j.Logger;

public class LibChecker {
	
	// mainGUI logger
	static Logger logger = Logger.getLogger(LibChecker.class);
		
	public static boolean checkJnetPcap() {
		try {
			org.jnetpcap.Pcap.libVersion();
			return true;
		} catch (UnsatisfiedLinkError e) {
			logger.error("! JnetPcap dll link error ! \n\n", e);
			return false;
		} catch (NoClassDefFoundError e) {
			logger.error("! JnetPcap class def missing ! \n\n", e);
			return false;
		}
	}
}
